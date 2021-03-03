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
package org.talend.designer.core.ui.editor.dependencies.service;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.Path;
import org.eclipse.gef.commands.Command;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.talend.commons.exception.ExceptionHandler;
import org.talend.commons.exception.PersistenceException;
import org.talend.commons.utils.workbench.resources.ResourceUtils;
import org.talend.core.model.context.JobContextManager;
import org.talend.core.model.general.Project;
import org.talend.core.model.process.IProcess;
import org.talend.core.model.process.IProcess2;
import org.talend.core.model.properties.Item;
import org.talend.core.model.properties.Property;
import org.talend.core.model.relationship.Relation;
import org.talend.core.model.relationship.RelationshipItemBuilder;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.model.repository.IRepositoryViewObject;
import org.talend.core.model.resources.ResourceItem;
import org.talend.core.model.utils.JavaResourcesHelper;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.core.service.IResourcesDependenciesService;
import org.talend.designer.core.ui.AbstractMultiPageTalendEditor;
import org.talend.designer.core.ui.editor.dependencies.dialog.DependenciesResourceSelectionDialog;
import org.talend.designer.core.ui.editor.dependencies.model.JobResourceDependencyModel;
import org.talend.designer.core.ui.editor.dependencies.util.ResourceDependenciesUtil;
import org.talend.designer.maven.tools.BuildCacheManager;
import org.talend.designer.runprocess.ProcessorUtilities;
import org.talend.repository.ProjectManager;

public class ResourcesDependenciesService implements IResourcesDependenciesService {

    @Override
    public String openResourcesDialogForContext(Shell parentShell) {
        DependenciesResourceSelectionDialog dialog = new DependenciesResourceSelectionDialog(parentShell);
        if (Dialog.OK == dialog.open()) {
            Item item = dialog.getResult().getObject().getProperty().getItem();
            if (item instanceof ResourceItem) {
                String value = item.getProperty().getId() + "|" + RelationshipItemBuilder.LATEST_VERSION;
                return value;
            }
        }
        return "";
    }

    @Override
    public void copyToExtResourceFolder(IRepositoryViewObject repoObject, String jobId, String jobVersion, String version,
            String rootJobLabel) {
        ResourceDependenciesUtil.copyToExtResourceFolder(repoObject, jobId, jobVersion, version, rootJobLabel);
    }

    @Override
    public String getResourcePathForContext(IProcess process, String resourceContextValue) {
        String resPath = null;
        if (process instanceof IProcess2) {
            IProcess2 process2 = (IProcess2) process;
            Property property = process2.getProperty();
            if (StringUtils.isBlank(resourceContextValue)) {
                return null;
            }
            try {
                String[] parts = resourceContextValue.split("\\|");
                if (parts.length > 1) {
                    IRepositoryViewObject repoObject = null;
                    if (RelationshipItemBuilder.LATEST_VERSION.equals(parts[1])) {
                        repoObject = ProxyRepositoryFactory.getInstance().getLastVersion(parts[0]);
                    } else {
                        repoObject = ProxyRepositoryFactory.getInstance().getSpecificVersion(parts[0], parts[1], true);
                    }
                    if (repoObject != null) {
                        JobResourceDependencyModel model = new JobResourceDependencyModel(
                                (ResourceItem) repoObject.getProperty().getItem());
                        String jobLabel = JavaResourcesHelper.getJobFolderName(property.getLabel(), property.getVersion());
                        if (ProcessorUtilities.isExportConfig()) {
                            resPath = ResourceDependenciesUtil.getResourcePath(model, jobLabel, parts[1]);
                        }else {
                            resPath = ResourceDependenciesUtil.getJobExecuteResourceFilePath(model, property, jobLabel, parts[1]);
                        }
                        // to check if file exist, if not copy it
                        ResourceDependenciesUtil.copyToExtResourceFolder(model, property.getId(), property.getVersion(), parts[1],
                                null);
                    }

                }

            } catch (PersistenceException e) {
                ExceptionHandler.process(e);
            }
        }
        return resPath;
    }

    @Override
    public String getResourceItemFilePath(String resourceContextValue) {
        String[] resParts = resourceContextValue.split("\\|");
        if (resParts.length > 1) {
            IRepositoryViewObject object = null;
            try {
                if (RelationshipItemBuilder.LATEST_VERSION.equals(resParts[1])) {
                    object = ProxyRepositoryFactory.getInstance().getLastVersion(resParts[0]);
                } else {
                    object = ProxyRepositoryFactory.getInstance().getSpecificVersion(resParts[0], resParts[1], true);
                }
                if (object != null) {
                    Item item = object.getProperty().getItem();
                    if (item instanceof ResourceItem) {
                        ResourceItem resItem = (ResourceItem) item;
                        String resouceClasspath = item.getState().getPath();
                        if (resouceClasspath != null && !resouceClasspath.isEmpty()) {
                            resouceClasspath += '/';
                        } else {
                            resouceClasspath = ""; //$NON-NLS-1$
                        }
                        String fileName = resouceClasspath + resItem.getProperty().getDisplayName() + "_"
                                + item.getProperty().getVersion() + "."
                                + resItem.getBindingExtension();
                        Project currentProject = ProjectManager.getInstance().getCurrentProject();
                        String itemFilePath = ResourceUtils.getProject(currentProject.getTechnicalLabel())
                                .getFile(new Path(ERepositoryObjectType.RESOURCES.getFolder() + "/" + fileName)).getLocation()
                                .toOSString();
                        return itemFilePath;
                    }
                }
            } catch (PersistenceException e) {
                ExceptionHandler.process(e);
            }

        }
        return null;
    }

    @Override
    public void refreshDependencyViewer() {
        IWorkbenchWindow activeWorkbenchWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        IWorkbenchPage activePage = activeWorkbenchWindow.getActivePage();
        IEditorReference[] editors = activePage.getEditorReferences();
        for (IEditorReference er : editors) {
            IEditorPart part = er.getEditor(false);
            if (part instanceof AbstractMultiPageTalendEditor) {
                int editorPage = ((AbstractMultiPageTalendEditor) part).getActivePage();
                if (editorPage == 3) {
                    ((AbstractMultiPageTalendEditor) part).getDependenciesEditor().setFocus();
                }
            }
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.service.IResourcesDependenciesService#setDesignerEditorDirtyManually()
     */
    @Override
    public void setContextParameterChangeDirtyManually() {
        IWorkbenchWindow activeWorkbenchWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        IWorkbenchPage activePage = activeWorkbenchWindow.getActivePage();
        IEditorPart activeEditor = activePage.getActiveEditor();
        if (activeEditor instanceof AbstractMultiPageTalendEditor) {
            boolean dirty = ((AbstractMultiPageTalendEditor) activeEditor).getDesignerEditor().isDirty();
            //if already dirty, no need
            if (dirty) {
                return;
            }
            JobContextManager contextManager = (JobContextManager) ((AbstractMultiPageTalendEditor) activeEditor)
                    .getDesignerEditor().getProcess().getContextManager();
            ((AbstractMultiPageTalendEditor) activeEditor).getDesignerEditor().getCommandStack().execute(new Command() {

                @Override
                public void execute() {
                    contextManager.setModified(true);
                    contextManager.fireContextsChangedEvent();
                }
            });

        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.service.IResourcesDependenciesService#removeBuildJobCacheForResource(java.lang.String)
     */
    @Override
    public void removeBuildJobCacheForResource(String resourceId) {
        ProxyRepositoryFactory factory = ProxyRepositoryFactory.getInstance();
        BuildCacheManager buildCacheManager = BuildCacheManager.getInstance();
        List<Relation> relations = RelationshipItemBuilder.getInstance().getAllVersionItemsRelatedTo(resourceId,
                RelationshipItemBuilder.RESOURCE_RELATION, true);
        try {
            for (Relation relation : relations) {
                IRepositoryViewObject object = factory.getSpecificVersion(relation.getId(), relation.getVersion(), false);
                if (object != null) {
                    buildCacheManager.preRemoveJobCache(object.getProperty());
                }
            }
        } catch (PersistenceException e) {
            ExceptionHandler.process(e);
        }
    }


}