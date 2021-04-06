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
package org.talend.designer.core.ui.wizards;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.texteditor.AbstractDecoratedTextEditor;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.talend.commons.exception.BusinessException;
import org.talend.commons.exception.ExceptionHandler;
import org.talend.commons.exception.LoginException;
import org.talend.commons.exception.PersistenceException;
import org.talend.commons.exception.SystemException;
import org.talend.commons.runtime.model.repository.ERepositoryStatus;
import org.talend.commons.ui.runtime.exception.MessageBoxExceptionHandler;
import org.talend.commons.utils.workbench.resources.ResourceUtils;
import org.talend.core.CorePlugin;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.context.Context;
import org.talend.core.context.RepositoryContext;
import org.talend.core.language.ECodeLanguage;
import org.talend.core.model.properties.BusinessProcessItem;
import org.talend.core.model.properties.Item;
import org.talend.core.model.properties.JobScriptItem;
import org.talend.core.model.properties.ProcessItem;
import org.talend.core.model.properties.Property;
import org.talend.core.model.properties.RoutineItem;
import org.talend.core.model.properties.SQLPatternItem;
import org.talend.core.model.relationship.RelationshipItemBuilder;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.model.repository.IRepositoryViewObject;
import org.talend.core.model.repository.RepositoryManager;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.core.repository.ui.editor.RepositoryEditorInput;
import org.talend.core.ui.services.IOpenJobScriptActionService;
import org.talend.core.ui.token.RepositoryOldItemsTokenCollector;
import org.talend.designer.codegen.ICodeGeneratorService;
import org.talend.designer.codegen.ISQLPatternSynchronizer;
import org.talend.designer.codegen.ITalendSynchronizer;
import org.talend.designer.core.i18n.Messages;
import org.talend.designer.core.ui.MultiPageTalendEditor;
import org.talend.designer.core.ui.editor.ProcessEditorInput;
import org.talend.expressionbuilder.ExpressionPersistance;
import org.talend.repository.ProjectManager;
import org.talend.repository.model.IProxyRepositoryFactory;
import org.talend.repository.model.RepositoryNode;
import org.talend.repository.ui.actions.routines.RoutineEditorInput;

/**
 * DOC xye class global comment. Detailled comment
 */
public class OpenExistVersionProcessWizard extends Wizard {

    protected OpenExistVersionProcessPage mainPage = null;

    protected IRepositoryViewObject processObject;

    protected boolean alreadyEditedByUser = false;

    private String originaleObjectLabel = null;

    private String originalVersion = null;

    public OpenExistVersionProcessWizard(IRepositoryViewObject processObject) {
        this.processObject = processObject;
        originaleObjectLabel = processObject.getProperty().getLabel();
        originalVersion = processObject.getProperty().getVersion();

        ERepositoryStatus status = processObject.getRepositoryStatus();
        if (status == ERepositoryStatus.READ_ONLY || status == ERepositoryStatus.LOCK_BY_OTHER
                || status.equals(ERepositoryStatus.LOCK_BY_USER) && RepositoryManager.isOpenedItemInEditor(processObject)) {
            alreadyEditedByUser = true;
        }
    }

    @Override
    public void addPages() {
        mainPage = new OpenExistVersionProcessPage(alreadyEditedByUser, processObject);
        addPage(mainPage);
        setWindowTitle(Messages.getString("OpenExistVersionProcessWizard.windowTitle1")); //$NON-NLS-1$
    }

    /**
     * Returns the currently active page for this workbench window.
     *
     * @return the active page, or <code>null</code> if none
     */
    public IWorkbenchPage getActivePage() {
        return PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
    }

    @Override
    public boolean performCancel() {
        return super.performCancel();
    }

    private void lockObject(IRepositoryViewObject object) {
        IProxyRepositoryFactory repositoryFactory = CorePlugin.getDefault().getRepositoryService().getProxyRepositoryFactory();
        try {
            repositoryFactory.lock(object);
        } catch (PersistenceException e) {
            ExceptionHandler.process(e);
        } catch (BusinessException e) {
            ExceptionHandler.process(e);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.jface.wizard.Wizard#performFinish()
     */
    @Override
    public boolean performFinish() {
        if (mainPage.isCreateNewVersionJob()) {
            try {
                ProxyRepositoryFactory.getInstance().updateLockStatus();
            } catch (PersistenceException e1) {
                ExceptionHandler.process(e1);
            }
            ERepositoryStatus repositoryStatus = ProxyRepositoryFactory.getInstance().getStatus(processObject);
            if ((repositoryStatus.equals(ERepositoryStatus.READ_ONLY)) || repositoryStatus == ERepositoryStatus.LOCK_BY_OTHER
                    || repositoryStatus.equals(ERepositoryStatus.LOCK_BY_USER)) {
                Display.getDefault().syncExec(new Runnable() {

                    @Override
                    public void run() {
                        MessageDialog.openWarning(getShell(), "Warning",
                                Messages.getString("OpenExistVersionProcessWizard.labelContent"));
                    }

                });
                return false;
            } else {
                IWorkspaceRunnable runnable = new IWorkspaceRunnable() {

                    @Override
                    public void run(final IProgressMonitor monitor) throws CoreException {
                        if (!alreadyEditedByUser) {
                            refreshNewJob();
                            RelationshipItemBuilder.getInstance().addOrUpdateItem(processObject.getProperty().getItem());
                            try {
                                ProxyRepositoryFactory.getInstance()
                                        .saveProject(ProjectManager.getInstance().getCurrentProject());
                            } catch (Exception e) {
                                ExceptionHandler.process(e);
                            }
                        }
                        try {
                            Item newCreated = null;
                            if (processObject.getProperty() != null && processObject.getProperty().getItem() != null) {
                                newCreated = processObject.getProperty().getItem();
                            }
                            if (!(newCreated instanceof BusinessProcessItem)) {
                                ProxyRepositoryFactory.getInstance().lock(processObject);
                            }
                        } catch (PersistenceException e) {
                            ExceptionHandler.process(e);
                        } catch (LoginException e) {
                            ExceptionHandler.process(e);
                        }
                        boolean locked = processObject.getRepositoryStatus().equals(ERepositoryStatus.LOCK_BY_USER);
                        openAnotherVersion((RepositoryNode) processObject.getRepositoryNode(), !locked);
                        try {
                            ProxyRepositoryFactory.getInstance().saveProject(ProjectManager.getInstance().getCurrentProject());
                        } catch (Exception e) {
                            ExceptionHandler.process(e);
                        }
                    }
                };
                IWorkspace workspace = ResourcesPlugin.getWorkspace();
                try {
                    ISchedulingRule schedulingRule = workspace.getRoot();
                    // the update the project files need to be done in the workspace
                    // runnable to avoid all notification
                    // of changes before the end of the modifications.
                    workspace.run(runnable, schedulingRule, IWorkspace.AVOID_UPDATE, null);
                } catch (CoreException e) {
                    MessageBoxExceptionHandler.process(e);
                }
            }
        } else {
            StructuredSelection selection = (StructuredSelection) mainPage.getSelection();
            RepositoryNode node = (RepositoryNode) selection.getFirstElement();
            boolean lastVersion = node.getObject().getVersion().equals(processObject.getVersion());
            // processObject.getProperty().setVersion(originalVersion);
            if (lastVersion) {
                lockObject(processObject);
            } else {
                RepositoryOldItemsTokenCollector.record(node.getObject().getRepositoryObjectType().getLabel());
            }
            ERepositoryStatus status = node.getObject().getRepositoryStatus();
            boolean isLocked = false;
            if (status == ERepositoryStatus.LOCK_BY_USER) {
                isLocked = true;
            }

            // Only latest version can be editted
            openAnotherVersion(node, !lastVersion || !isLocked);
        }
        return true;
    }

    protected boolean refreshNewJob() {
        if (alreadyEditedByUser) {
            return false;
        }
        boolean lastVersion = true;
        StructuredSelection selection = (StructuredSelection) mainPage.getSelection();
        if (selection != null && !selection.isEmpty()) {
            RepositoryNode node = (RepositoryNode) selection.getFirstElement();
            lastVersion = node.getObject().getVersion().equals(originalVersion);
            if (!lastVersion) {
                originalVersion = node.getObject().getVersion();
                String newVersion = mainPage.getNewVersion();
                processObject = node.getObject();
                processObject.getProperty().setVersion(newVersion);
            }
        }
        if (lastVersion) {
            getProperty().setVersion(mainPage.getNewVersion());
        }

        IProxyRepositoryFactory repositoryFactory = CorePlugin.getDefault().getRepositoryService().getProxyRepositoryFactory();
        try {
            repositoryFactory.save(getProperty(), this.originaleObjectLabel, this.originalVersion);
            ExpressionPersistance.getInstance().jobNameChanged(originaleObjectLabel, processObject.getLabel());
            ProxyRepositoryFactory.getInstance().saveProject(ProjectManager.getInstance().getCurrentProject());
            return true;
        } catch (PersistenceException e) {
            MessageBoxExceptionHandler.process(e);
            return false;
        }
    }

    protected void openAnotherVersion(final RepositoryNode node, final boolean readonly) {
        try {
            if (node.getObject() != null) {
                final Item item = node.getObject().getProperty().getItem();
                final IWorkbenchPage page = getActivePage();
                final RepositoryEditorInput fileEditorInput = getEditorInput(item, readonly, page);
                if (fileEditorInput != null) {
                    IEditorPart editorPart = page.findEditor(fileEditorInput);
                    if (editorPart == null) {
                        fileEditorInput.setRepositoryNode(node);
                        if (item instanceof ProcessItem) {
                            page.openEditor(fileEditorInput, MultiPageTalendEditor.ID, readonly);
                        } else if (item instanceof BusinessProcessItem) {
                            CorePlugin.getDefault().getDiagramModelService().openBusinessDiagramEditor(page, fileEditorInput);
                        } else {
                            ECodeLanguage lang = ((RepositoryContext) CorePlugin.getContext()
                                    .getProperty(Context.REPOSITORY_CONTEXT_KEY)).getProject().getLanguage();
                            String talendEditorID = "org.talend.designer.core.ui.editor.StandAloneTalend" + lang.getCaseName() //$NON-NLS-1$
                                    + "Editor"; //$NON-NLS-1$
                            page.openEditor(fileEditorInput, talendEditorID);
                        }
                    } else {
                        page.activate(editorPart);
                    }
                } else {
                    // TDI-19014:open another version of jobScript
                    if (item instanceof JobScriptItem) {
                        IProject fsProject = ResourceUtils.getProject(ProjectManager.getInstance().getCurrentProject());
                        openXtextEditor(node, fsProject, readonly);
                    }
                }
            }
        } catch (PartInitException e) {
            MessageBoxExceptionHandler.process(e);
        } catch (SystemException e) {
            MessageBoxExceptionHandler.process(e);
        }
    }

    protected RepositoryEditorInput getEditorInput(final Item item, final boolean readonly, final IWorkbenchPage page)
            throws SystemException {
        if (item instanceof ProcessItem) {
            ProcessItem processItem = (ProcessItem) item;
            return new ProcessEditorInput(processItem, true, false, readonly);
        } else if (item instanceof BusinessProcessItem) {
            BusinessProcessItem businessProcessItem = (BusinessProcessItem) item;
            IFile file = CorePlugin.getDefault().getDiagramModelService().getDiagramFileAndUpdateResource(page,
                    businessProcessItem);
            return new RepositoryEditorInput(file, businessProcessItem);
        } else if (item instanceof RoutineItem) {
            final RoutineItem routineItem = (RoutineItem) item;
            final ICodeGeneratorService codeGenService = (ICodeGeneratorService) GlobalServiceRegister.getDefault()
                    .getService(ICodeGeneratorService.class);
            ITalendSynchronizer routineSynchronizer = codeGenService.createRoutineSynchronizer();
            ProxyRepositoryFactory factory = ProxyRepositoryFactory.getInstance();
            String lastVersion = factory.getLastVersion(routineItem.getProperty().getId()).getVersion();
            String curVersion = routineItem.getProperty().getVersion();
            // routineSynchronizer.syncRoutine(routineItem, true, true);
            final IFile file;
            if (curVersion != null && curVersion.equals(lastVersion)) {
                file = routineSynchronizer.getFile(routineItem);
            } else {
                file = routineSynchronizer.getRoutinesFile(routineItem);
            }
            if (file != null) {
                return new RoutineEditorInput(file, routineItem);
            }
        } else if (item instanceof SQLPatternItem) {
            SQLPatternItem patternItem = (SQLPatternItem) item;
            final ICodeGeneratorService codeGenService = (ICodeGeneratorService) GlobalServiceRegister.getDefault()
                    .getService(ICodeGeneratorService.class);
            ISQLPatternSynchronizer SQLPatternSynchronizer = codeGenService.getSQLPatternSynchronizer();
            SQLPatternSynchronizer.syncSQLPattern(patternItem, true);
            IFile file = SQLPatternSynchronizer.getSQLPatternFile(patternItem);
            if (file != null) {
                return new RepositoryEditorInput(file, patternItem);
            }
        }
        return null;
    }

    private Property getProperty() {
        return processObject.getProperty();
    }

    public String getOriginVersion() {
        return this.originalVersion;
    }

    private void openXtextEditor(RepositoryNode repositoryNode, IProject fsProject, boolean readonly) {
        try {
            if (ProjectManager.getInstance().isInCurrentMainProject(repositoryNode)) {
                IFile linkedFile = null;
                IOpenJobScriptActionService openJobScriptActionService = (IOpenJobScriptActionService) GlobalServiceRegister
                        .getDefault().getService(IOpenJobScriptActionService.class);
                if (openJobScriptActionService != null) {
                    linkedFile = openJobScriptActionService.createWorkspaceLink(fsProject,
                            repositoryNode.getObject().getProperty().getItem());
                } else {
                    linkedFile = createWorkspaceLink(fsProject,
                            fsProject.getFolder(ERepositoryObjectType.getFolderName(ERepositoryObjectType.JOB_SCRIPT))
                                    .getFolder(repositoryNode.getParent().getRepositoryPath())
                                    .getFile(repositoryNode.getObject().getProperty().getLabel()).getLocation(),
                            repositoryNode.getObject().getProperty().getVersion());
                }

                IWorkbenchPage page = getActivePage();
                IEditorPart editor = IDE.openEditor(page, linkedFile);
                if (readonly) {
                    IDocumentProvider provider = ((AbstractDecoratedTextEditor) editor).getDocumentProvider();
                    Class p = provider.getClass();
                    Class[] type = new Class[1];
                    type[0] = Boolean.TYPE;
                    Object[] para = new Object[1];
                    para[0] = Boolean.TRUE;
                    Method method = p.getMethod("setReadOnly", type);
                    method.invoke(provider, para);
                }

            } else {
                JobScriptItem jobScriptItem = (JobScriptItem) repositoryNode.getObject().getProperty().getItem();
                IFile file = ResourcesPlugin.getWorkspace().getRoot().getFile(
                        new Path(jobScriptItem.eResource().getURI().path()).removeFirstSegments(1).removeFileExtension());
                IFile linkedFile = createWorkspaceLink(fsProject, file.getLocation(), "");
                IWorkbenchPage page = getActivePage();
                IEditorPart editor = IDE.openEditor(page, linkedFile);
                if (readonly) {
                    IDocumentProvider provider = ((AbstractDecoratedTextEditor) editor).getDocumentProvider();
                    Class p = provider.getClass();
                    Class[] type = new Class[1];
                    type[0] = Boolean.TYPE;
                    Object[] para = new Object[1];
                    para[0] = Boolean.TRUE;
                    Method method = p.getMethod("setReadOnly", type);
                    method.invoke(provider, para);
                }
            }
        } catch (CoreException e) {
            ExceptionHandler.process(e);
        } catch (SecurityException e) {
            ExceptionHandler.process(e);
        } catch (NoSuchMethodException e) {
            ExceptionHandler.process(e);
        } catch (IllegalArgumentException e) {
            ExceptionHandler.process(e);
        } catch (IllegalAccessException e) {
            ExceptionHandler.process(e);
        } catch (InvocationTargetException e) {
            ExceptionHandler.process(e);
        }
    }

    public static IFile createWorkspaceLink(IProject fsProject, IPath realLocation, String version) throws CoreException {
        String fileName = realLocation.lastSegment();
        IFile linkedFile;
        if (version.equals("")) {
            realLocation = new Path(realLocation.toOSString() + ".item");
            linkedFile = fsProject.getFolder("temp").getFile(fileName + ".jobscript");
        } else {
            realLocation = new Path(realLocation.toOSString() + "_" + version + ".item");
            linkedFile = fsProject.getFolder("temp").getFile(fileName + "_" + version + ".jobscript");
        }
        while (!linkedFile.exists() || !linkedFile.getRawLocation().equals(realLocation)) {
            // creates a linked file if it does not exists
            if (!linkedFile.exists()) {
                linkedFile.createLink(realLocation, IResource.HIDDEN, null);
            } else if (!linkedFile.getRawLocation().equals(realLocation)) {// if linked file exist but does not
                // point to the same file then
                // creates a new one
                linkedFile.delete(true, null);
                linkedFile.createLink(realLocation, IResource.HIDDEN, null);
            }
        }
        return linkedFile;
    }
}
