// ============================================================================
//
// Copyright (C) 2006-2019 Talend Inc. - www.talend.com
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

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Path;
import org.eclipse.ltk.core.refactoring.resource.MoveResourceChange;
import org.eclipse.ltk.core.refactoring.resource.RenameResourceChange;
import org.talend.commons.exception.ExceptionHandler;
import org.talend.commons.exception.PersistenceException;
import org.talend.commons.utils.workbench.resources.ResourceUtils;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.model.properties.Item;
import org.talend.core.model.properties.Property;
import org.talend.core.model.properties.RoutineItem;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.model.repository.IRepositoryViewObject;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.core.runtime.process.TalendProcessOptionConstants;
import org.talend.core.ui.ITestContainerProviderService;
import org.talend.designer.maven.model.TalendMavenConstants;
import org.talend.designer.maven.tools.AggregatorPomsHelper;
import org.talend.designer.maven.tools.BuildCacheManager;
import org.talend.designer.maven.utils.PomIdsHelper;
import org.talend.designer.runprocess.java.TalendJavaProjectManager;
import org.talend.repository.ProjectManager;
import org.talend.repository.RepositoryWorkUnit;
import org.talend.repository.documentation.ERepositoryActionName;

/**
 * DOC zwxue class global comment. Detailled comment
 */
public class ProcessChangeListener implements PropertyChangeListener {

    private AggregatorPomsHelper helper;

    private List<ERepositoryObjectType> allProcessTypes;

    @Override
    public void propertyChange(PropertyChangeEvent event) {
        try {
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
                    } else if (propertyName.equals(ERepositoryActionName.AFTER_DELETE.getName())) {
                        caseAfterDelete(newValue);
                    } else if (propertyName.equals(ERepositoryActionName.FOLDER_RENAME.getName())
                            || propertyName.equals(ERepositoryActionName.JOBLET_FOLDER_RENAME.getName())) {
                        caseFolderRename(oldValue, newValue);
                    } else if (propertyName.equals(ERepositoryActionName.FOLDER_MOVE.getName())
                            || propertyName.equals(ERepositoryActionName.JOBLET_FOLDER_MOVE.getName())) {
                        caseFolderMove(oldValue, newValue);
                    } else if (propertyName.equals(ERepositoryActionName.FOLDER_DELETE.getName())
                            || propertyName.equals(ERepositoryActionName.JOBLET_FOLDER_DELETE.getName())) {
                        caseFolderDelete(oldValue, newValue);
                    } else if (propertyName.equals(ERepositoryActionName.SAVE.getName())
                            || propertyName.equals(ERepositoryActionName.CREATE.getName())) {
                        boolean avoidGeneratePom = false;
                        if (propertyName.equals(ERepositoryActionName.SAVE.getName())) {
                            avoidGeneratePom = Boolean.valueOf(String.valueOf(oldValue));
                        }
                        if (!avoidGeneratePom) {
                            caseCreateAndSave(propertyName, newValue);
                        }
                    } else if (propertyName.equals(ERepositoryActionName.IMPORT.getName())) {
                        caseImport(propertyName, newValue);
                    } else if (propertyName.equals(ERepositoryActionName.DELETE_TO_RECYCLE_BIN.getName())) {
                        caseDeleteToRecycleBin(newValue);
                    } else if (propertyName.equals(ERepositoryActionName.RESTORE.getName())) {
                        caseRestore(newValue);
                    }
                }
            };
            workUnit.setAvoidUnloadResources(true);
            ProxyRepositoryFactory.getInstance().executeRepositoryWorkUnit(workUnit);
        } catch (Exception e) {
            // Any exception here must be catched/logged, but it should not block the operations of user
            ExceptionHandler.process(e);
        }
    }

    private void casePropertiesChange(Object oldValue, Object newValue) {
        // oldValue: [oldName, oldVersion], newValue: property
        if (oldValue instanceof String[] && newValue instanceof Property) {
            Property property = (Property) newValue;
            ITestContainerProviderService service = getTestContainerProviderService();
            if (service != null && service.isTestContainerItem(property.getItem())) {
                // FIXME
            } else if (isNeedUpdateItem(property.getItem())) {
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
                    TalendJavaProjectManager.generatePom(property.getItem());
                    AggregatorPomsHelper helper = new AggregatorPomsHelper();
                    helper.syncParentJobPomsForPropertyChange(property);
                    PropertyProcessUpdater.updateRouteCode(oldName);
                }
            }
        }
    }

    private void caseMove(Object oldValue, Object newValue) {
        // oldValue: IRepositoryViewObject, newValue: [sourcePath, targetPath]
        if (oldValue instanceof IRepositoryViewObject && newValue instanceof IPath[]) {
            // delete all version job project, create all version new projects
            IRepositoryViewObject obj = (IRepositoryViewObject) oldValue;
            if (isNeedUpdateItem(obj.getProperty().getItem())) {
                // TalendJavaProjectManager.deleteAllVersionTalendJobProject(obj.getId(), null, false);
                IPath sourcePath = ((IPath[]) newValue)[0];
                IPath targetPath = ((IPath[]) newValue)[1];
                IFolder processTypeFolder = getAggregatorPomsHelper().getProcessFolder(obj.getRepositoryObjectType());
                IFolder targetFolder = processTypeFolder.getFolder(targetPath);
                try {
                    if (!targetFolder.exists()) {
                        ResourceUtils.createFolder(targetFolder);
                    }
                    List<IRepositoryViewObject> allVersions = ProxyRepositoryFactory.getInstance().getAllVersion(obj.getId());
                    for (IRepositoryViewObject objs : allVersions) {
                        Property property = objs.getProperty();
                        String jobProjectFolderName = AggregatorPomsHelper.getJobProjectFolderName(property);
                        IFolder sourceFolder = processTypeFolder.getFolder(sourcePath).getFolder(jobProjectFolderName);
                        if (sourceFolder.exists()) {
                            sourceFolder.getProject().refreshLocal(IResource.DEPTH_INFINITE, null);
                            AggregatorPomsHelper
                                    .removeFromParentModules(sourceFolder.getFile(TalendMavenConstants.POM_FILE_NAME));
                            MoveResourceChange change = new MoveResourceChange(sourceFolder, targetFolder);
                            change.perform(new NullProgressMonitor());
                            IFile pomFile = targetFolder.getFolder(sourceFolder.getName())
                                    .getFile(TalendMavenConstants.POM_FILE_NAME);
                            AggregatorPomsHelper.updateGroupIdAndRelativePath(pomFile, property);
                            AggregatorPomsHelper.addToParentModules(pomFile, property);
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
            if (property != null && property.getItem() != null) {
                ITestContainerProviderService service = getTestContainerProviderService();
                if (service != null && service.isTestContainerItem(property.getItem())) {
                    // done in caseAfterDelete()
                } else if (isNeedUpdateItem(property.getItem())) {
                    // delete all version old job projects physically, won't check to remove parent folder
                    if (ProjectManager.getInstance().getCurrentProject().isLocal()) {
                        TalendJavaProjectManager.deleteAllVersionTalendJobProject(property.getId(), null, true);
                    }
                }
                // check ref-project if exist same label routine
                checkCodesAfterDeleteForever(property);
            }
        }
    }

    private void caseAfterDelete(Object newValue) {
        // newValue: obj
        if (newValue instanceof IRepositoryViewObject) {
            Property property = ((IRepositoryViewObject) newValue).getProperty();
            if (property != null && property.getItem() != null) {
                ITestContainerProviderService service = getTestContainerProviderService();
                if (service != null && service.isTestContainerItem(property.getItem())) {
                    try {
                        Item jobItem = service.getParentJobItem(property.getItem());
                        if (jobItem != null) {
                            TalendJavaProjectManager.generatePom(jobItem);
                        }
                    } catch (PersistenceException e) {
                        ExceptionHandler.process(e);
                    }
                }
            }
        }
    }

    /**
     *
     * DOC jding Comment method "checkRoutinesAfterDeleteForever". After delete routines forever, need to check if
     * ref-project exist same routines, need to rebuild routines when next job build.
     */
    private void checkCodesAfterDeleteForever(Property property) {
        List<ERepositoryObjectType> allTypesOfCodes = ERepositoryObjectType.getAllTypesOfCodes();
        ERepositoryObjectType objectType = ERepositoryObjectType.getType(property);
        if (allTypesOfCodes.contains(objectType)) {
            // update last chage date, then will re-build routines at the next build time.
            BuildCacheManager.getInstance().updateCodesLastChangeDate(objectType);
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
            if (getAllProcessTypes().contains(processType)) {
                IFolder sourceFolder = getAggregatorPomsHelper().getProcessFolder(processType).getFolder(folderPath);
                NullProgressMonitor monitor = new NullProgressMonitor();
                try {
                    IContainer parent = sourceFolder.getParent();
                    sourceFolder.refreshLocal(IResource.DEPTH_INFINITE, monitor);
                    removeFromParentSourceFolder(sourceFolder);
                    if(sourceFolder.exists()) {
                    	RenameResourceChange change = new RenameResourceChange(sourceFolder.getFullPath(), newName);
                        change.perform(monitor);
                    }
                    IFolder newFolder = parent.getFolder(new Path(newName));
                    updatePomsInNewFolder(newFolder);
                } catch (CoreException e) {
                    ExceptionHandler.process(e);
                }
            }
        }
    }

    /**
     * DOC nrousseau Comment method "updatePomsInNewFolder". update all jobs' relative path and groupId add job to
     * parent modules
     *
     * @param newFolder
     * @throws CoreException
     */
    private void updatePomsInNewFolder(IFolder newFolder) throws CoreException {
    	if(!newFolder.exists()) {
    		return;
    	}
        for (IResource res : newFolder.members()) {
            if (res instanceof IFolder) {
                IFolder currentFolder = (IFolder) res;
                IFile pomFile = currentFolder.getFile(TalendMavenConstants.POM_FILE_NAME);
                if (pomFile.exists()) {
                    try {
                        AggregatorPomsHelper.updateGroupIdAndRelativePath(pomFile);
                        AggregatorPomsHelper.addToParentModules(pomFile);
                    } catch (Exception e) {
                        ExceptionHandler.process(e);
                    }
                } else {
                    updatePomsInNewFolder(currentFolder);
                }
            }
        }
    }

    /**
     * DOC nrousseau Comment method "removeFromParentSourceFolder".
     *
     * @param sourceFolder
     * @throws CoreException
     */
    private void removeFromParentSourceFolder(IFolder sourceFolder) throws CoreException {
    	if(!sourceFolder.exists()) {
    		return;
    	}
        for (IResource res : sourceFolder.members()) {
            if (res instanceof IFolder) {
                IFolder currentFolder = (IFolder) res;
                IFile pomFile = currentFolder.getFile(TalendMavenConstants.POM_FILE_NAME);
                if (pomFile.exists()) {
                    try {
                        AggregatorPomsHelper.removeFromParentModules(pomFile);
                    } catch (Exception e) {
                        ExceptionHandler.process(e);
                    }
                } else {
                    removeFromParentSourceFolder(currentFolder);
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
            if (getAllProcessTypes().contains(processType)) {
                IFolder processTypeFolder = getAggregatorPomsHelper().getProcessFolder(processType);
                IFolder sourceFolder = processTypeFolder.getFolder(sourcePath);
                if (!sourceFolder.exists()) {
                    return;
                }
                try {
                    sourceFolder.refreshLocal(IResource.DEPTH_INFINITE, new NullProgressMonitor());
                    removeFromParentSourceFolder(sourceFolder);
                    IFolder targetFolder = processTypeFolder.getFolder(targetPath);
                    if (!targetFolder.exists()) {
                        ResourceUtils.createFolder(targetFolder);
                    }
                    MoveResourceChange change = new MoveResourceChange(sourceFolder, targetFolder);
                    change.perform(new NullProgressMonitor());
                    updatePomsInNewFolder(targetFolder.getFolder(sourceFolder.getName()));
                } catch (OperationCanceledException | CoreException | PersistenceException e) {
                    ExceptionHandler.process(e);
                }
            }
        }
    }

    private void caseFolderDelete(Object oldValue, Object newValue) {
        // oldValue: IPath, newValue: type
        if (oldValue instanceof IPath && newValue instanceof ERepositoryObjectType) {
            // delete all version job projects under it physically, remove this folder.
            IPath folderPath = (IPath) oldValue;
            ERepositoryObjectType processType = (ERepositoryObjectType) newValue;
            if (getAllProcessTypes().contains(processType)) {
                TalendJavaProjectManager.deleteTalendJobProjectsUnderFolder(processType, folderPath, true);
            }
        }
    }

    private void caseCreateAndSave(String propertyName, Object newValue) {
        if (newValue instanceof Item) {
            Item item = (Item) newValue;
            ITestContainerProviderService service = getTestContainerProviderService();
            if (service != null && service.isTestContainerItem(item)) {
                try {
                    Item jobItem = service.getParentJobItem(item);
                    if (jobItem != null) {
                        item = jobItem;
                    }
                } catch (PersistenceException e) {
                    ExceptionHandler.process(e);
                }
            }
            if (isNeedUpdateItem(item)) {
                if (propertyName.equals(ERepositoryActionName.SAVE.getName())) {
                    TalendJavaProjectManager.generatePom((Item) newValue);
                }
            } else if (item instanceof RoutineItem) {
                updateCodesChange((RoutineItem) item);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void caseImport(String propertyName, Object newValue) {
        if (newValue instanceof Set) {
            Set<Item> importItems = (Set<Item>) newValue;
            for (Item item : importItems) {
                if (isNeedUpdateItem(item)) {
                    TalendJavaProjectManager.generatePom(item, TalendProcessOptionConstants.GENERATE_NO_CODEGEN);
                } else if (item instanceof RoutineItem) {
                    updateCodesChange((RoutineItem) item);
                }
            }
        }
    }

    private void caseDeleteToRecycleBin(Object newValue) {
        if (!PomIdsHelper.getIfExcludeDeletedItems()) {
            // if not checked Exclude deleted items then no need remove module from root pom
            return;
        }
        if (newValue instanceof IRepositoryViewObject) {
            IRepositoryViewObject object = (IRepositoryViewObject) newValue;
            if (!ERepositoryObjectType.TEST_CONTAINER.equals(object.getRepositoryObjectType())) {
                try {
                    AggregatorPomsHelper.removeAllVersionsFromParentModules(object.getProperty());
                } catch (Exception e) {
                    ExceptionHandler.process(e);
                }
            }
        }
    }

    private void caseRestore(Object newValue) {
        if (!PomIdsHelper.getIfExcludeDeletedItems()) {
            // if not checked Exclude deleted items then no need add back the module to root pom
            return;
        }
        if (newValue instanceof IRepositoryViewObject) {
            IRepositoryViewObject object = (IRepositoryViewObject) newValue;
            if (!ERepositoryObjectType.TEST_CONTAINER.equals(object.getRepositoryObjectType())) {
                try {
                    AggregatorPomsHelper.restoreAllVersionsFromParentModules(object.getProperty());
                } catch (Exception e) {
                    ExceptionHandler.process(e);
                }
            }
        }
    }

    private void updateCodesChange(RoutineItem codesItem) {
        ERepositoryObjectType type = ERepositoryObjectType.getItemType(codesItem);
        BuildCacheManager.getInstance().updateCodesLastChangeDate(type);
    }

    private boolean isNeedUpdateItem(Item item) {
        ERepositoryObjectType type = ERepositoryObjectType.getItemType(item);
        if (type != null) {
            return getAllProcessTypes().contains(type);
        }
        return false;
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

    private List<ERepositoryObjectType> getAllProcessTypes() {
        if (allProcessTypes == null) {
            allProcessTypes = ERepositoryObjectType.getAllTypesOfProcess2();
        }
        return allProcessTypes;
    }

}
