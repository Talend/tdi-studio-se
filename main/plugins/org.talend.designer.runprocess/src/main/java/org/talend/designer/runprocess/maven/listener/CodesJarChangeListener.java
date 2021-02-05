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
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.ltk.core.refactoring.resource.RenameResourceChange;
import org.talend.commons.exception.ExceptionHandler;
import org.talend.commons.utils.generation.JavaUtils;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.model.properties.Item;
import org.talend.core.model.properties.Property;
import org.talend.core.model.properties.RoutineItem;
import org.talend.core.model.properties.RoutinesJarItem;
import org.talend.core.model.relationship.Relation;
import org.talend.core.model.relationship.RelationshipItemBuilder;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.model.repository.IRepositoryViewObject;
import org.talend.core.model.routines.CodesJarInfo;
import org.talend.core.model.routines.RoutinesUtil;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.core.runtime.process.ITalendProcessJavaProject;
import org.talend.core.utils.CodesJarResourceCache;
import org.talend.designer.codegen.ICodeGeneratorService;
import org.talend.designer.codegen.ITalendSynchronizer;
import org.talend.designer.core.model.utils.emf.talendfile.RoutinesParameterType;
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
            if (RoutinesUtil.isInnerCodes(property)) {
                updateAndReSyncForInnerCode(property, ((String[]) oldValue)[0]);
                return;
            } else if (!needUpdate(property.getItem())) {
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
            CodesJarM2CacheManager.updateCodesJarProject(property, !property.getLabel().equals(oldName));
        }
    }

    private void caseDelete(String propertyName, Object newValue) throws Exception {
        if (newValue instanceof IRepositoryViewObject) {
            Property property = ((IRepositoryViewObject) newValue).getProperty();
            if (propertyName.equals(ERepositoryActionName.DELETE_FOREVER.getName())) {
                if (RoutinesUtil.isInnerCodes(property)) {
                    updateAndReSyncForInnerCode(property, property.getLabel());
                } else if (needUpdate(property.getItem())) {
                    CodesJarResourceCache.removeCache(property);
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

    private void caseRestore(Object newValue) {
        if (newValue instanceof IRepositoryViewObject) {
            IRepositoryViewObject object = (IRepositoryViewObject) newValue;
            if (needUpdate(object.getProperty().getItem())) {
                CodesJarResourceCache.addToCache(object.getProperty());
            }
        }
    }

    private boolean needUpdate(Item item) {
        return item instanceof RoutinesJarItem;
    }

    private void updateAndReSyncForInnerCode(Property property, String originalName) throws Exception {
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
                IFile originalRoutineFile = routineFolder.getFile(originalName + JavaUtils.JAVA_EXTENSION);
                if (originalRoutineFile == null || !originalRoutineFile.exists()) {
                    return;
                }
                originalRoutineFile.delete(true, false, null);
            }
        }

        if (!property.getLabel().equals(originalName)) {
            if (GlobalServiceRegister.getDefault().isServiceRegistered(ICodeGeneratorService.class)) {
                ICodeGeneratorService codeGenService = (ICodeGeneratorService) GlobalServiceRegister.getDefault()
                        .getService(ICodeGeneratorService.class);
                ITalendSynchronizer routineSynchronizer = codeGenService.createRoutineSynchronizer();
                routineSynchronizer.syncRoutine(codeItem, true, true);
            }
        }
        if (info.getProperty() != null) {
            CodesJarM2CacheManager.updateCodesJarProject(info.getProperty());
        }

    }

    public static void updateItemsRelatedToCodeJars(Property property, ERepositoryObjectType objectType, String actionType) {
        if (!ERepositoryObjectType.getAllTypesOfCodesJar().contains(objectType)) {
            return;
        }
        RelationshipItemBuilder relationshipItemBuilder = RelationshipItemBuilder.getInstance();
        ProxyRepositoryFactory repositoryFactory = ProxyRepositoryFactory.getInstance();
        String relationType = null;
        if (ERepositoryObjectType.ROUTINESJAR != null && ERepositoryObjectType.ROUTINESJAR.equals(objectType)) {
            relationType = relationshipItemBuilder.ROUTINES_JAR_RELATION;
        } else if (ERepositoryObjectType.BEANSJAR != null && ERepositoryObjectType.BEANSJAR.equals(objectType)) {
            relationType = relationshipItemBuilder.BEANS_JAR_RELATION;
        }
        if (StringUtils.isBlank(relationType)) {
            return;
        }
        List<Relation> relationList = relationshipItemBuilder.getAllVersionItemsRelatedTo(property.getId(), relationType, false);
        try {
            for (Relation relation : relationList) {
                IRepositoryViewObject relatedObj = repositoryFactory.getSpecificVersion(relation.getId(), relation.getVersion(),
                        true);
                if (relatedObj == null) {
                    continue;
                }
                boolean modified = false;
                Item item = relatedObj.getProperty().getItem();
                List<RoutinesParameterType> routinesParametersFromItem = RoutinesUtil.getRoutinesParametersFromItem(item);

                if (ERepositoryActionName.DELETE_FOREVER.getName().equals(actionType)) {
                    Iterator<RoutinesParameterType> iterator = routinesParametersFromItem.iterator();
                    while (iterator.hasNext()) {
                        RoutinesParameterType routinesParam = iterator.next();
                        if (StringUtils.isNotBlank(routinesParam.getType()) && routinesParam.getId().equals(property.getId())) {
                            iterator.remove();
                            modified = true;
                        }
                    }
                    RelationshipItemBuilder.getInstance().addOrUpdateItem(item);
                } else if (ERepositoryActionName.PROPERTIES_CHANGE.getName().equals(actionType)) {
                    for (RoutinesParameterType param : routinesParametersFromItem) {
                        if (StringUtils.isNotBlank(param.getType()) && param.getId().equals(property.getId())
                                && !param.getName().equals(property.getLabel())) {
                            param.setName(property.getLabel());
                            modified = true;
                        }
                    }
                }

                if (modified) {
                    repositoryFactory.save(item);
                }
            }
        } catch (Exception e) {
            ExceptionHandler.process(e);
        }
    }

}
