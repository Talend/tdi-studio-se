// ============================================================================
//
// Copyright (C) 2006-2021 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.designer.runprocess.maven.listener;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.ltk.core.refactoring.resource.RenameResourceChange;
import org.talend.commons.exception.ExceptionHandler;
import org.talend.commons.utils.generation.JavaUtils;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.model.general.Project;
import org.talend.core.model.properties.Item;
import org.talend.core.model.properties.Property;
import org.talend.core.model.properties.RoutineItem;
import org.talend.core.model.properties.RoutinesJarItem;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.model.repository.IRepositoryViewObject;
import org.talend.core.model.routines.CodesJarInfo;
import org.talend.core.model.routines.RoutinesUtil;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.core.repository.utils.XmiResourceManager;
import org.talend.core.runtime.process.ITalendProcessJavaProject;
import org.talend.core.utils.CodesJarResourceCache;
import org.talend.designer.maven.tools.AggregatorPomsHelper;
import org.talend.designer.maven.tools.CodesJarM2CacheManager;
import org.talend.designer.maven.utils.CodesJarMavenUtil;
import org.talend.designer.runprocess.IRunProcessService;
import org.talend.designer.runprocess.java.TalendJavaProjectManager;
import org.talend.repository.ProjectManager;
import org.talend.repository.RepositoryWorkUnit;
import org.talend.repository.documentation.ERepositoryActionName;

public class CodesJarChangeListener implements PropertyChangeListener {

    @Override
    public void propertyChange(PropertyChangeEvent event) {
        String propertyName = event.getPropertyName();
        Object oldValue = event.getOldValue();
        Object newValue = event.getNewValue();
        RepositoryWorkUnit<Object> workUnit = new RepositoryWorkUnit<Object>("update codesjar by " + propertyName) { //$NON-NLS-1$

            @Override
            protected void run() {
                try {
                    if (propertyName.equals(ERepositoryActionName.PROPERTIES_CHANGE.getName())) {
                        casePropertiesChange(oldValue, newValue);
                    } else if (propertyName.equals(ERepositoryActionName.DELETE_FOREVER.getName())
                            || propertyName.equals(ERepositoryActionName.DELETE_TO_RECYCLE_BIN.getName())) {
                        caseDelete(propertyName, newValue);
                    } else if (propertyName.equals(ERepositoryActionName.SAVE.getName())
                            || propertyName.equals(ERepositoryActionName.CREATE.getName())) {
                        caseCreateOrSave(newValue);
                    } else if (propertyName.equals(ERepositoryActionName.IMPORT.getName())) {
                        caseImport(propertyName, newValue);
                    } else if (propertyName.equals(ERepositoryActionName.RESTORE.getName())) {
                        caseRestore(newValue);
                    }
                } catch (Exception e) {
                    ExceptionHandler.process(e);
                }
            }
        };
        workUnit.setAvoidUnloadResources(true);
        ProxyRepositoryFactory.getInstance().executeRepositoryWorkUnit(workUnit);
    }

    private void casePropertiesChange(Object oldValue, Object newValue) throws Exception {
        if (oldValue instanceof String[] && newValue instanceof Property) {
            Property property = (Property) newValue;
            if (!needUpdate(property.getItem())) {
                return;
            }
            String[] oldFields = (String[]) oldValue;
            String oldName = oldFields[0];
            String oldVersion = oldFields[1];
            CodesJarResourceCache.updateCache(null, oldName, oldVersion, property);
            ERepositoryObjectType type = ERepositoryObjectType.getItemType(property.getItem());
            IFolder folder = new AggregatorPomsHelper().getCodeFolder(type).getFolder(oldName);
            RenameResourceChange change = new RenameResourceChange(folder.getFullPath(), property.getLabel());
            change.perform(new NullProgressMonitor());
            TalendJavaProjectManager.deleteTalendCodesJarProject(type,
                    ProjectManager.getInstance().getProject(property).getTechnicalLabel(), oldName, true);
            boolean isLabelChanged = !property.getLabel().equals(oldName);
            if (isLabelChanged) {
                CodesJarM2CacheManager.deleteCodesJarProjectCache(CodesJarInfo.create(property));
            }
            CodesJarM2CacheManager.updateCodesJarProject(property, isLabelChanged);
        }
    }

    private void caseDelete(String propertyName, Object newValue) throws Exception {
        if (newValue instanceof IRepositoryViewObject) {
            Property property = ((IRepositoryViewObject) newValue).getProperty();
            if (propertyName.equals(ERepositoryActionName.DELETE_FOREVER.getName())) {
                if (RoutinesUtil.isInnerCodes(property)) {
                    updateAndReSyncForInnerCode(property);
                    updateModifiedDateForCodesJar(property.getItem());
                } else if (needUpdate(property.getItem())) {
                    CodesJarResourceCache.removeCache(property);
                    CodesJarM2CacheManager.deleteCodesJarProjectCache(CodesJarInfo.create(property));
                    TalendJavaProjectManager.deleteTalendCodesJarProject(property, true);
                }
            }
        }
    }

    private void caseCreateOrSave(Object newValue) throws Exception {
        if (newValue instanceof Item) {
            Item item = (Item) newValue;
            if (needUpdate(item)) {
                CodesJarResourceCache.addToCache(item.getProperty());
            } else if (RoutinesUtil.isInnerCodes(item.getProperty())) {
                updateModifiedDateForCodesJar(item);
            }
        }
    }

    private void caseImport(String propertyName, Object newValue) {
        if (newValue instanceof Set) {
            Set<Item> importItems = (Set<Item>) newValue;
            importItems.stream().filter(item -> needUpdate(item))
                    .forEach(item -> CodesJarResourceCache.addToCache(item.getProperty()));
        }
    }

    private void caseRestore(Object newValue) throws Exception {
        if (newValue instanceof IRepositoryViewObject) {
            IRepositoryViewObject object = (IRepositoryViewObject) newValue;
            Property property = object.getProperty();
            if (needUpdate(property.getItem())) {
                CodesJarResourceCache.addToCache(property);
            } else if (RoutinesUtil.isInnerCodes(property)) {
                updateModifiedDateForCodesJar(property.getItem());
            }
        }
    }

    private boolean needUpdate(Item item) {
        return item instanceof RoutinesJarItem;
    }

    private void updateAndReSyncForInnerCode(Property property) throws Exception {
        if (!RoutinesUtil.isInnerCodes(property)) {
            return;
        }

        RoutineItem codeItem = (RoutineItem) property.getItem();
        CodesJarInfo info = CodesJarResourceCache.getCodesJarByInnerCode(codeItem);
        if (GlobalServiceRegister.getDefault().isServiceRegistered(IRunProcessService.class)) {
            IRunProcessService runProcessService = GlobalServiceRegister.getDefault().getService(IRunProcessService.class);
            if (runProcessService != null) {
                ITalendProcessJavaProject talendCodesJarJavaProject = runProcessService.getTalendCodesJarJavaProject(info);
                if (talendCodesJarJavaProject == null) {
                    return;
                }
                IFolder routineFolder = talendCodesJarJavaProject.getSrcSubFolder(null,
                        CodesJarMavenUtil.getCodesJarPackageByInnerCode(codeItem));
                IFile originalRoutineFile = routineFolder.getFile(property.getLabel() + JavaUtils.JAVA_EXTENSION);
                if (originalRoutineFile == null || !originalRoutineFile.exists()) {
                    return;
                }
                originalRoutineFile.delete(true, false, null);
            }
        }

        if (info.getProperty() != null) {
            CodesJarM2CacheManager.updateCodesJarProject(info.getProperty());
        }

    }

    private void updateModifiedDateForCodesJar(Item item) throws Exception {
        if (item instanceof RoutineItem) {
            RoutineItem innerCodeItem = (RoutineItem) item;
            CodesJarInfo info = CodesJarResourceCache.getCodesJarByInnerCode(innerCodeItem);
            Project project = ProjectManager.getInstance().getProjectFromProjectTechLabel(info.getProjectTechName());
            IRepositoryViewObject obj = ProxyRepositoryFactory.getInstance().getLastVersion(project, info.getProperty().getId());
            Property codesJarProperty = obj.getProperty();
            new XmiResourceManager().saveResource(codesJarProperty.eResource());
            CodesJarResourceCache.addToCache(codesJarProperty);
        }
    }

}
