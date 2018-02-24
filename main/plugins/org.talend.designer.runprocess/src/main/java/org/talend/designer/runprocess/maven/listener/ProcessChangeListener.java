// ============================================================================
//
// Copyright (C) 2006-2018 Talend Inc. - www.talend.com
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
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.ltk.core.refactoring.resource.MoveResourceChange;
import org.eclipse.ltk.core.refactoring.resource.RenameResourceChange;
import org.talend.commons.exception.ExceptionHandler;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.model.properties.Item;
import org.talend.core.model.properties.ProcessItem;
import org.talend.core.model.properties.Property;
import org.talend.core.model.properties.RoutineItem;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.model.repository.IRepositoryViewObject;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.core.ui.ITestContainerProviderService;
import org.talend.designer.maven.tools.AggregatorPomsHelper;
import org.talend.designer.maven.tools.BuildCacheManager;
import org.talend.designer.runprocess.java.TalendJavaProjectManager;
import org.talend.repository.RepositoryWorkUnit;
import org.talend.repository.documentation.ERepositoryActionName;

/**
 * DOC zwxue class global comment. Detailled comment
 */
public class ProcessChangeListener implements PropertyChangeListener {

    private AggregatorPomsHelper helper;

    private List<ERepositoryObjectType> allProcessTypes = ERepositoryObjectType.getAllTypesOfProcess();

    @Override
    public void propertyChange(PropertyChangeEvent event) {
        String propertyName = event.getPropertyName();
        Object oldValue = event.getOldValue();
        Object newValue = event.getNewValue();
        RepositoryWorkUnit<Object> workUnit = new RepositoryWorkUnit<Object>("update poms by " + propertyName) { //$NON-NLS-1$

            @Override
            protected void run() {
                if (propertyName.equals(ERepositoryActionName.PROPERTIES_CHANGE.getName())) {
                    casePropertiesChange(oldValue, newValue);
                } else if (propertyName.equals(ERepositoryActionName.MOVE.getName())) {
                    caseMove(oldValue, newValue);
                } else if (propertyName.equals(ERepositoryActionName.DELETE_FOREVER.getName())) {
                    caseDeleteForever(newValue);
                } else if (propertyName.equals(ERepositoryActionName.FOLDER_RENAME.getName())) {
                    caseFolderRename(oldValue, newValue);
                } else if (propertyName.equals(ERepositoryActionName.FOLDER_MOVE.getName())) {
                    caseFolderMove(oldValue, newValue);
                } else if (propertyName.equals(ERepositoryActionName.FOLDER_DELETE.getName())) {
                    caseFolderDelete(oldValue, newValue);
                } else if (propertyName.equals(ERepositoryActionName.SAVE.getName())
                        || propertyName.equals(ERepositoryActionName.CREATE.getName())) {
                    caseCreateAndSave(propertyName, newValue);
                } else if (propertyName.equals(ERepositoryActionName.IMPORT.getName())) {
                    caseImport(propertyName, newValue);
                }
            }
        };
        workUnit.setAvoidUnloadResources(true);
        ProxyRepositoryFactory.getInstance().executeRepositoryWorkUnit(workUnit);
    }

    private void casePropertiesChange(Object oldValue, Object newValue) {
        // oldValue: [oldName, oldVersion], newValue: property
        if (oldValue instanceof String[] && newValue instanceof Property) {
            Property property = (Property) newValue;
            if (property.getItem() instanceof ProcessItem) {
                ITestContainerProviderService service = getTestContainerProviderService();
                boolean isTestCase = false;
                if (service != null) {
                    isTestCase = service.isTestContainerItem(property.getItem());
                }
                if (isTestCase) {
                    // FIXME
                } else {
                    String[] oldFields = (String[]) oldValue;
                    if (oldFields.length == 0) {
                        // Do nothing case import project
                        return;
                    }
                    String oldName = oldFields[0];
                    String oldVersion = oldFields[1];
                    if (!oldName.equals(property.getLabel())) {
                        // job name change, will change all old version job name
                        // delete all version old job projects physically(need to recreate pom), and create new for all.
                        TalendJavaProjectManager.deleteAllVersionTalendJobProject(property.getId(), oldName, true);
                        TalendJavaProjectManager.createAllVersionTalendJobProject(property.getId());
                    } else if (!oldVersion.equals(property.getVersion())) {
                        // version change, will create new item
                        // create new job project.
                        TalendJavaProjectManager.generatePom((ProcessItem) property.getItem());
                    }
                }
            }
        }
    }

    private void caseMove(Object oldValue, Object newValue) {
        // oldValue: IRepositoryViewObject, newValue: [sourcePath, targetPath]
        if (oldValue instanceof IRepositoryViewObject && newValue instanceof IPath[]) {
            // delete all version job project, create all version new projects
            IRepositoryViewObject obj = (IRepositoryViewObject) oldValue;
            if (obj.getProperty().getItem() instanceof ProcessItem) {
                // TalendJavaProjectManager.deleteAllVersionTalendJobProject(obj.getId(), null, false);
                IPath sourcePath = ((IPath[]) newValue)[0];
                IPath targetPath = ((IPath[]) newValue)[1];
                IFolder processTypeFolder = getAggregatorPomsHelper().getProcessFolder(obj.getRepositoryObjectType());
                IFolder targetFolder = processTypeFolder.getFolder(targetPath);
                try {
                    if (!targetFolder.exists()) {
                        targetFolder.create(true, true, null);
                    }
                    List<IRepositoryViewObject> allVersions = ProxyRepositoryFactory.getInstance().getAllVersion(obj.getId());
                    for (IRepositoryViewObject objs : allVersions) {
                        Property property = objs.getProperty();
                        String jobProjectFolderName = AggregatorPomsHelper.getJobProjectFolderName(property);
                        IFolder sourceFolder = processTypeFolder.getFolder(sourcePath).getFolder(jobProjectFolderName);
                        if (sourceFolder.exists()) {
                            MoveResourceChange change = new MoveResourceChange(sourceFolder, targetFolder);
                            change.perform(new NullProgressMonitor());
                        }
                    }
                } catch (Exception e) {
                    ExceptionHandler.process(e);
                }
            }
        }
    }

    private void caseDeleteForever(Object newValue) {
        // newValue: obj
        if (newValue instanceof IRepositoryViewObject) {
            Property property = ((IRepositoryViewObject) newValue).getProperty();
            if (property != null) {
                ITestContainerProviderService service = getTestContainerProviderService();
                boolean isTestCase = false;
                if (service != null && property.getItem() != null) {
                    isTestCase = service.isTestContainerItem(property.getItem());
                }
                if (isTestCase) {
                    // FIXME testcase should regenerate pom to remove extra dependencies.
                } else {
                    // delete all version old job projects physically, won't check to remove parent folder
                    TalendJavaProjectManager.deleteAllVersionTalendJobProject(property.getId(), null, true);
                }
            }
        }
    }

    private void caseFolderRename(Object oldValue, Object newValue) {
        // oldValue: IPath, newValue: [label, type]
        if (oldValue instanceof IPath && newValue instanceof Object[]) {
            // delete all version old job projects under it logically, rename folder
            IPath folderPath = (IPath) oldValue;
            Object[] objects = (Object[]) newValue;
            String newName = (String) objects[0];
            ERepositoryObjectType processType = (ERepositoryObjectType) objects[1];
            if (allProcessTypes.contains(processType)) {
                // TalendJavaProjectManager.deleteTalendJobProjectsUnderFolder(processType, folderPath, false);
                IFolder sourceFolder = getAggregatorPomsHelper().getProcessFolder(processType).getFolder(folderPath);
                NullProgressMonitor monitor = new NullProgressMonitor();
                try {
                    sourceFolder.refreshLocal(IResource.DEPTH_INFINITE, monitor);
                    RenameResourceChange change = new RenameResourceChange(sourceFolder.getFullPath(), newName);
                    change.perform(monitor);
                } catch (CoreException e) {
                    ExceptionHandler.process(e);
                }
            }
        }
    }

    private void caseFolderMove(Object oldValue, Object newValue) {
        // oldValue: [sourcePath, targetPath], newValue: type
        if (oldValue instanceof IPath[] && newValue instanceof ERepositoryObjectType) {
            // delete all version old job projects under it logically, move folder
            IPath sourcePath = ((IPath[]) oldValue)[0];
            IPath targetPath = ((IPath[]) oldValue)[1];
            ERepositoryObjectType processType = (ERepositoryObjectType) newValue;
            // TalendJavaProjectManager.deleteTalendJobProjectsUnderFolder(processType, sourcePath, false);
            IFolder processTypeFolder = getAggregatorPomsHelper().getProcessFolder(processType);
            IFolder sourceFolder = processTypeFolder.getFolder(sourcePath);
            IFolder targetFolder = processTypeFolder.getFolder(targetPath);
            MoveResourceChange change = new MoveResourceChange(sourceFolder, targetFolder);
            try {
                change.perform(new NullProgressMonitor());
            } catch (OperationCanceledException | CoreException e) {
                ExceptionHandler.process(e);
            }
        }
    }

    private void caseFolderDelete(Object oldValue, Object newValue) {
        // oldValue: IPath, newValue: type
        if (oldValue instanceof IPath && newValue instanceof ERepositoryObjectType) {
            // delete all version job projects under it physically, remove this folder.
            IPath folderPath = (IPath) oldValue;
            ERepositoryObjectType processType = (ERepositoryObjectType) newValue;
            TalendJavaProjectManager.deleteTalendJobProjectsUnderFolder(processType, folderPath, true);
        }
    }

    private void caseCreateAndSave(String propertyName, Object newValue) {
        if (newValue instanceof Item) {
            if (newValue instanceof ProcessItem) {
                if (propertyName.equals(ERepositoryActionName.SAVE.getName())) {
                    TalendJavaProjectManager.generatePom((ProcessItem) newValue);
                }
            } else if (newValue instanceof RoutineItem) {
                updateCodesChange((RoutineItem) newValue);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void caseImport(String propertyName, Object newValue) {
        if (newValue instanceof Set) {
            Set<Item> importItems = (Set<Item>) newValue;
            for (Item item : importItems) {
                if (item instanceof ProcessItem) {
                    TalendJavaProjectManager.generatePom((ProcessItem) item);
                } else if (item instanceof RoutineItem) {
                    updateCodesChange((RoutineItem) item);
                }
            }
        }
    }

    private void updateCodesChange(RoutineItem codesItem) {
        ERepositoryObjectType type = ERepositoryObjectType.getItemType(codesItem);
        BuildCacheManager.getInstance().updateCodesLastChangeDate(type, codesItem.getProperty());
    }

    private ITestContainerProviderService getTestContainerProviderService() {
        if (GlobalServiceRegister.getDefault().isServiceRegistered(ITestContainerProviderService.class)) {
            return (ITestContainerProviderService) GlobalServiceRegister.getDefault()
                    .getService(ITestContainerProviderService.class);
        }
        return null;
    }

    private AggregatorPomsHelper getAggregatorPomsHelper() {
        if (helper == null) {
            helper = new AggregatorPomsHelper();
        }
        return helper;
    }

}
