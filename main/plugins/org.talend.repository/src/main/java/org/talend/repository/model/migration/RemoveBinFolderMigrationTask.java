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
package org.talend.repository.model.migration;

import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.talend.commons.exception.PersistenceException;
import org.talend.commons.runtime.model.emf.EmfHelper;
import org.talend.commons.ui.runtime.exception.ExceptionHandler;
import org.talend.commons.utils.io.FilesUtils;
import org.talend.commons.utils.workbench.resources.ResourceUtils;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.PluginChecker;
import org.talend.core.model.general.Project;
import org.talend.core.model.migration.AbstractProjectMigrationTask;
import org.talend.core.model.properties.FolderItem;
import org.talend.core.model.properties.Item;
import org.talend.core.model.properties.Property;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.repository.utils.XmiResourceManager;
import org.talend.repository.model.IProxyRepositoryFactory;
import org.talend.repository.model.IRepositoryService;

/**
 * DOC nrousseau class global comment. Detailled comment
 */
public class RemoveBinFolderMigrationTask extends AbstractProjectMigrationTask {

    public List<ERepositoryObjectType> getTypes() {
        List<ERepositoryObjectType> toReturn = new ArrayList<ERepositoryObjectType>();
        toReturn.add(ERepositoryObjectType.BUSINESS_PROCESS);
        toReturn.add(ERepositoryObjectType.SVG_BUSINESS_PROCESS);
        toReturn.add(ERepositoryObjectType.DOCUMENTATION);
        toReturn.add(ERepositoryObjectType.METADATA_CONNECTIONS);
        toReturn.add(ERepositoryObjectType.METADATA_SAPCONNECTIONS);
        toReturn.add(ERepositoryObjectType.SQLPATTERNS);
        toReturn.add(ERepositoryObjectType.METADATA_FILE_DELIMITED);
        toReturn.add(ERepositoryObjectType.METADATA_FILE_POSITIONAL);
        toReturn.add(ERepositoryObjectType.METADATA_FILE_REGEXP);
        toReturn.add(ERepositoryObjectType.METADATA_FILE_XML);
        toReturn.add(ERepositoryObjectType.METADATA_FILE_EXCEL);
        toReturn.add(ERepositoryObjectType.METADATA_FILE_LDIF);
        toReturn.add(ERepositoryObjectType.METADATA_LDAP_SCHEMA);
        toReturn.add(ERepositoryObjectType.METADATA_GENERIC_SCHEMA);
        toReturn.add(ERepositoryObjectType.METADATA_WSDL_SCHEMA);
        toReturn.add(ERepositoryObjectType.METADATA_SALESFORCE_SCHEMA);
        toReturn.add(ERepositoryObjectType.PROCESS);
        toReturn.add(ERepositoryObjectType.ROUTINES);
        toReturn.add(ERepositoryObjectType.SNIPPETS);
        toReturn.add(ERepositoryObjectType.JOBLET);
        toReturn.add(ERepositoryObjectType.CONTEXT);
        if (PluginChecker.isDocumentationPluginLoaded()) {
            toReturn.add(ERepositoryObjectType.JOB_DOC);
        }
        if (PluginChecker.isJobLetPluginLoaded()) {
            toReturn.add(ERepositoryObjectType.JOBLET_DOC);
        }
        if (PluginChecker.isEBCDICPluginLoaded()) {
            toReturn.add(ERepositoryObjectType.METADATA_FILE_EBCDIC);
        }
        if (PluginChecker.isHL7PluginLoaded()) {
            toReturn.add(ERepositoryObjectType.METADATA_FILE_HL7);
        }
        if (PluginChecker.isRulesPluginLoaded()) {
            toReturn.add(ERepositoryObjectType.METADATA_FILE_RULES);
        }
        if (PluginChecker.isMDMPluginLoaded()) {
            toReturn.add(ERepositoryObjectType.METADATA_MDMCONNECTION);
        }
        toReturn.remove(null); // remove all null;
        return toReturn;
    }

    @Override
    public Date getOrder() {
        GregorianCalendar gc = new GregorianCalendar(2010, 3, 23, 12, 0, 0);
        return gc.getTime();
    }

    @Override
    public ExecutionResult execute(Project project) {
        XmiResourceManager xmiResourceManager = new XmiResourceManager();
        IRepositoryService service = (IRepositoryService) GlobalServiceRegister.getDefault().getService(IRepositoryService.class);
        IProxyRepositoryFactory factory = service.getProxyRepositoryFactory();

        for (ERepositoryObjectType type : getTypes()) {
            IFolder folder = null;
            if (type != null && type.hasFolder()) {
                try {
                    IProject fsProject = ResourceUtils.getProject(project.getTechnicalLabel());
                    if (!fsProject.getFolder(ERepositoryObjectType.getFolderName(type)).exists()) {
                        continue;
                    }
                    folder = ResourceUtils.getFolder(fsProject, ERepositoryObjectType.getFolderName(type), true);
                    for (IResource current : ResourceUtils.getMembers(folder)) {
                        if ((current instanceof IFolder) && ((IFolder) current).getName().equals("bin")) {
                            for (IResource fileCurrent : ResourceUtils.getMembers((IFolder) current)) {
                                if (fileCurrent instanceof IFile) {
                                    if (xmiResourceManager.isPropertyFile((IFile) fileCurrent)) {
                                        Property property = null;
                                        try {
                                            property = xmiResourceManager.loadProperty(fileCurrent);
                                        } catch (RuntimeException e) {
                                            // property will be null
                                            ExceptionHandler.process(e);
                                        }
                                        if (property != null) {
                                            // restore folder if doesn't exist anymore.
                                            Item propertyItem = property.getItem();
                                            propertyItem.getState().setDeleted(true);

                                            EmfHelper.saveResource(propertyItem.eResource());

                                            String oldPath = propertyItem.getState().getPath();
                                            IPath path = new Path(oldPath);

                                            factory.createParentFoldersRecursively(project, type, path, true);

                                            FolderItem folderItem = factory.getFolderItem(project, type, path);
                                            propertyItem.setParent(folderItem);

                                            String name = fileCurrent.getName().replace(".properties", "");
                                            // take all the files starting by the same name
                                            IFolder typeRootFolder = ResourceUtils.getFolder(fsProject,
                                                    ERepositoryObjectType.getFolderName(type), true);
                                            for (IResource filesToMove : ResourceUtils.getMembers((IFolder) current)) {
                                                if (filesToMove.getName().startsWith(name)) {
                                                    IPath originalPath = filesToMove.getFullPath();
                                                    IPath finalPath = typeRootFolder.getFullPath().append(path)
                                                            .append(originalPath.lastSegment());

                                                    if (fsProject.getWorkspace().getRoot().getFile(finalPath).exists()) {
                                                        // if the file exists in both bin and target directory, delete
                                                        // the one from bin.
                                                        filesToMove.delete(true, new NullProgressMonitor());
                                                    } else {
                                                        ResourceUtils.moveResource(filesToMove, finalPath);
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            IResource[] binFolder = ResourceUtils.getMembers((IFolder) current);

                            if (binFolder.length == 0 || (binFolder.length == 1 && FilesUtils.isSVNFolder(binFolder[0]))) {
                                try {
                                    ((IFolder) current).delete(true, null);
                                } catch (CoreException e) {
                                    // not catched, not important if can delete or not
                                }
                            }
                        }
                    }
                } catch (PersistenceException e) {
                    ExceptionHandler.process(e);
                    return ExecutionResult.FAILURE;
                } catch (CoreException e) {
                    ExceptionHandler.process(e);
                    return ExecutionResult.FAILURE;
                }
            }

        }
        // }
        return ExecutionResult.SUCCESS_NO_ALERT;
    }

}
