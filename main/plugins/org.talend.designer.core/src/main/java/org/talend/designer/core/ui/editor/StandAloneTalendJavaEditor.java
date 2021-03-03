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
package org.talend.designer.core.ui.editor;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;

import org.eclipse.core.internal.events.ResourceChangeEvent;
import org.eclipse.core.internal.resources.Workspace;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.notify.impl.AdapterImpl;
import org.eclipse.emf.common.util.EList;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.internal.corext.refactoring.rename.JavaRenameProcessor;
import org.eclipse.jdt.internal.corext.refactoring.rename.RenameCompilationUnitProcessor;
import org.eclipse.jdt.internal.ui.javaeditor.CompilationUnitEditor;
import org.eclipse.jdt.ui.actions.IJavaEditorActionDefinitionIds;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.ltk.core.refactoring.CheckConditionsOperation;
import org.eclipse.ltk.core.refactoring.PerformRefactoringOperation;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.RefactoringStatusEntry;
import org.eclipse.ltk.core.refactoring.participants.RenameRefactoring;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.part.FileEditorInput;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;
import org.talend.commons.exception.ExceptionHandler;
import org.talend.commons.exception.LoginException;
import org.talend.commons.exception.PersistenceException;
import org.talend.commons.runtime.model.repository.ERepositoryStatus;
import org.talend.commons.ui.runtime.image.ECoreImage;
import org.talend.commons.ui.runtime.image.ImageProvider;
import org.talend.commons.utils.VersionUtils;
import org.talend.commons.utils.workbench.resources.ResourceUtils;
import org.talend.core.CorePlugin;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.model.general.Project;
import org.talend.core.model.properties.ByteArray;
import org.talend.core.model.properties.FileItem;
import org.talend.core.model.properties.Information;
import org.talend.core.model.properties.Item;
import org.talend.core.model.properties.Property;
import org.talend.core.model.properties.RoutineItem;
import org.talend.core.model.properties.SQLPatternItem;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.model.repository.Folder;
import org.talend.core.model.repository.IRepositoryViewObject;
import org.talend.core.model.routines.RoutinesUtil;
import org.talend.core.model.utils.RepositoryManagerHelper;
import org.talend.core.repository.constants.Constant;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.core.repository.ui.editor.RepositoryEditorInput;
import org.talend.core.runtime.CoreRuntimePlugin;
import org.talend.core.runtime.process.ITalendProcessJavaProject;
import org.talend.core.services.IUIRefresher;
import org.talend.core.ui.ILastVersionChecker;
import org.talend.core.ui.branding.IBrandingService;
import org.talend.core.utils.CodesJarResourceCache;
import org.talend.designer.codegen.ICodeGeneratorService;
import org.talend.designer.codegen.ISQLPatternSynchronizer;
import org.talend.designer.codegen.ITalendSynchronizer;
import org.talend.designer.core.DesignerPlugin;
import org.talend.designer.core.i18n.Messages;
import org.talend.designer.core.ui.action.SaveAsRoutineAction;
import org.talend.designer.core.ui.action.SaveAsSQLPatternAction;
import org.talend.designer.core.ui.views.problems.Problems;
import org.talend.designer.core.utils.DesignerColorUtils;
import org.talend.designer.runprocess.IRunProcessService;
import org.talend.repository.ProjectManager;
import org.talend.repository.RepositoryWorkUnit;
import org.talend.repository.model.IProxyRepositoryFactory;
import org.talend.repository.model.IRepositoryService;
import org.talend.repository.model.RepositoryNode;
import org.talend.repository.ui.views.IRepositoryView;

/**
 * Stand alone Perl editor.<br/>
 *
 */
public class StandAloneTalendJavaEditor extends CompilationUnitEditor implements IUIRefresher, ILastVersionChecker {

    public static final String ID = "org.talend.designer.core.ui.editor.StandAloneTalendJavaEditor"; //$NON-NLS-1$

    private RepositoryEditorInput rEditorInput;

    private Boolean isEditable = true;

    private Color bgColorForReadOnlyItem;

    private Color bgColorForEditabeItem;

    private ServiceRegistration lockService;

    /**
     * DOC smallet Comment method "getRepositoryFactory".
     */
    private IProxyRepositoryFactory getRepositoryFactory() {
        return DesignerPlugin.getDefault().getRepositoryService().getProxyRepositoryFactory();
    }

    @Override
    public boolean isSaveAsAllowed() {
        IProxyRepositoryFactory factory = ProxyRepositoryFactory.getInstance();
        if (factory.isUserReadOnlyOnCurrentProject()) {
            return false;
        }
        return getRepositoryFactory().getStatus(item).isEditable();
    }

    @Override
    public boolean isEditable() {
        if (!getRepositoryFactory().getStatus(item).isEditable()) {
            ISourceViewer sourceViewer = getSourceViewer();
            if (sourceViewer != null) {
                sourceViewer.getTextWidget().setDragDetect(false);
            }
            isEditable = false;
        }
        return !rEditorInput.isReadOnly() && getRepositoryFactory().getStatus(item).isEditable() && isLastVersion(item);
    }

    @Override
    public void init(IEditorSite site, IEditorInput input) throws PartInitException {
        super.init(site, input);
        Bundle bundle = FrameworkUtil.getBundle(StandAloneTalendJavaEditor.class);
        lockService = bundle.getBundleContext().registerService(EventHandler.class.getName(), new EventHandler() {

            @Override
            public void handleEvent(Event event) {
                String lockTopic = Constant.REPOSITORY_ITEM_EVENT_PREFIX + Constant.ITEM_LOCK_EVENT_SUFFIX;
                if (lockTopic.equals(event.getTopic())) {
                    if (!isEditable) {
                        Object o = event.getProperty(Constant.ITEM_EVENT_PROPERTY_KEY);
                        if (o instanceof FileItem) {
                            boolean isTheCorrectEditor = false;
                            Property property = ((FileItem) o).getProperty();
                            if (property != null) {
                                String eventItemId = property.getId();
                                String currentOpenedItemId = ""; //$NON-NLS-1$
                                if (rEditorInput != null) {
                                    currentOpenedItemId = rEditorInput.getId();
                                }
                                isTheCorrectEditor = currentOpenedItemId.equals(eventItemId);
                            }
                            if (!isTheCorrectEditor) {
                                return;
                            }

                            item.getProperty().eAdapters().remove(dirtyListener);
                            item = (FileItem) o;
                            item.getProperty().eAdapters().add(dirtyListener);
                            if (isEditable()) {
                                isEditable = true;
                                rEditorInput.getFile().setReadOnly(false);
                                getSite().getShell().getDisplay().asyncExec(new Runnable() {

                                    @Override
                                    public void run() {
                                        setFocus();
                                        ISourceViewer viewer = getViewer();
                                        if (viewer != null) {
                                            StyledText styledText = viewer.getTextWidget();
                                            if (styledText != null) {
                                                styledText.setBackground(bgColorForEditabeItem);
                                                styledText.setDragDetect(true);
                                            }
                                        }
                                    }
                                });

                                try {
                                    ICodeGeneratorService service = (ICodeGeneratorService) GlobalServiceRegister.getDefault()
                                            .getService(ICodeGeneratorService.class);
                                    if (o instanceof RoutineItem) {
                                        ITalendSynchronizer routineSynchronizer = service.createJavaRoutineSynchronizer();
                                        routineSynchronizer.syncRoutine((RoutineItem) o, true);
                                    } else if (o instanceof SQLPatternItem) {
                                        ISQLPatternSynchronizer sqlPatternSynchronizer = service.getSQLPatternSynchronizer();
                                        sqlPatternSynchronizer.syncSQLPattern((SQLPatternItem) o, true);
                                    } else {
                                        org.talend.commons.exception.ExceptionHandler.process(new Exception("Uncatched case")); //$NON-NLS-1$
                                    }
                                    setName();
                                } catch (Exception e) {
                                    org.talend.commons.exception.ExceptionHandler.process(e);
                                }
                            }
                        }
                    }
                }
            }
        }, new Hashtable<String, String>(
                Collections.singletonMap(EventConstants.EVENT_TOPIC, Constant.REPOSITORY_ITEM_EVENT_PREFIX + "*"))); //$NON-NLS-1$
    }

    @Override
    public void doSetInput(IEditorInput input) throws CoreException {
        // Lock the process :
        IRepositoryService service = DesignerPlugin.getDefault().getRepositoryService();
        IProxyRepositoryFactory repFactory = service.getProxyRepositoryFactory();

        if (input instanceof RepositoryEditorInput) {
            rEditorInput = (RepositoryEditorInput) input;
        } else {
            FileEditorInput fileInput = (FileEditorInput) input;
            rEditorInput = new RepositoryEditorInput(fileInput.getFile(), rEditorInput.getItem());
        }
        if (rEditorInput.getRepositoryNode() == null) {
            rEditorInput.setRepositoryNode(null); // retrieve node
        }

        try {
            // see bug 1321
            item = (FileItem) rEditorInput.getItem();
            if (!rEditorInput.isReadOnly()) {
                if (getRepositoryFactory().getStatus(item).isPotentiallyEditable()) {
                    item.getProperty().eAdapters().add(dirtyListener);
                    repFactory.lock(item);
                }
            } else {
                rEditorInput.getFile().setReadOnly(true);
            }
        } catch (Exception e) {
            ExceptionHandler.process(e);
        }
        rEditorInput.getFile().refreshLocal(IResource.DEPTH_ONE, null);
        super.doSetInput(rEditorInput);
        setName();
        // only for sql template
        if (item instanceof SQLPatternItem) {
            Workspace ws = (Workspace) ResourcesPlugin.getWorkspace();
            ws.broadcastBuildEvent(item, IResourceChangeEvent.POST_CHANGE, 1);
        }
    }

    private void setName() {
        IRepositoryView repoView = RepositoryManagerHelper.findRepositoryView();
        if (repoView != null) {
            RepositoryNode repositoryNode = rEditorInput.getRepositoryNode();
            if (repositoryNode != null) {
                setTitleImage(getTitleImage());
                setPartName(getTitleText(repositoryNode.getObject()));
            }
        }
    }

    private String getTitleText(IRepositoryViewObject object) {
        StringBuffer string = new StringBuffer();
        string.append(object.getLabel());
        IBrandingService brandingService = (IBrandingService) GlobalServiceRegister.getDefault()
                .getService(IBrandingService.class);
        boolean allowVerchange = brandingService.getBrandingConfiguration().isAllowChengeVersion();
        if (!(object instanceof Folder) && allowVerchange) {
            string.append(" " + object.getVersion()); //$NON-NLS-1$
        }
        // nodes in the recycle bin
        if (object.isDeleted()) {
            String oldPath = object.getPath();
            string.append(" (" + oldPath + ")"); //$NON-NLS-1$ //$NON-NLS-2$
        }
        return string.toString();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.ui.part.WorkbenchPart#getTitleImage()
     */
    @Override
    public Image getTitleImage() {
        if (item != null) {
            IRepositoryView viewPart = RepositoryManagerHelper.findRepositoryView();
            if (viewPart != null) {
                RepositoryNode repositoryNode = rEditorInput.getRepositoryNode();
                if (repositoryNode != null) {
                    Image titleImage = null;
                    if (ERepositoryObjectType.SQLPATTERNS == repositoryNode.getObjectType()) {
                        titleImage = ImageProvider.getImage(ECoreImage.METADATA_SQLPATTERN_ICON_EDITOR);
                    } else if (ERepositoryObjectType.ROUTINES == repositoryNode.getObjectType()) {
                        titleImage = ImageProvider.getImage(ECoreImage.ROUTINE_EDITOR_ICON);
                    } else {
                        titleImage = ImageProvider.getImage(repositoryNode.getIcon());
                    }
                    return titleImage;
                }
            }
        }
        return super.getTitleImage();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.ui.part.WorkbenchPart#getPartName()
     */
    @Override
    public String getPartName() {
        String title = null;
        if (item != null) {
            IRepositoryView viewPart = RepositoryManagerHelper.findRepositoryView();
            if (viewPart != null) {
                RepositoryNode repositoryNode = rEditorInput.getRepositoryNode();
                if (repositoryNode != null) {
                    return getTitleText(repositoryNode.getObject());
                } else {
                    title = super.getPartName() + " " + rEditorInput.getItem().getProperty().getVersion();
                }
            }
        }
        return title;
    }

    @SuppressWarnings("restriction")
    @Override
    public void dispose() {
        // remove the Runtines .java file in the .Java Project.
        // try {
        // rEditorInput.getFile().delete(true, null);
        // } catch (CoreException e1) {
        // RuntimeExceptionHandler.process(e1);
        // }
        super.dispose();

        if (lockService != null) {
            lockService.unregister();
        }

        // Unlock the process :
        IRepositoryService service = DesignerPlugin.getDefault().getRepositoryService();
        IProxyRepositoryFactory repFactory = service.getProxyRepositoryFactory();
        try {
            item.getProperty().eAdapters().remove(dirtyListener);
            repFactory.unlock(item);
        } catch (PersistenceException e) {
            // e.printStackTrace();
            ExceptionHandler.process(e);
        } catch (LoginException e) {
            ExceptionHandler.process(e);
        }
        // RepositoryNode repositoryNode = rEditorInput.getRepositoryNode();
        // if (repositoryNode != null) {
        // if (repFactory.getStatus(item) == ERepositoryStatus.DELETED) {
        // RepositoryManager.refreshDeletedNode(null);
        // } else {
        // RepositoryManager.refresh(repositoryNode.getObjectType());
        // }
        // }
        if (!isEditable) {
            rEditorInput.getFile().setReadOnly(false);
        }

        // dispose custom color
        if (bgColorForReadOnlyItem != null) {
            bgColorForReadOnlyItem.dispose();
        }
        if (bgColorForEditabeItem != null) {
            bgColorForEditabeItem.dispose();
        }

        // only for sql template
        if (item instanceof SQLPatternItem) {
            ResourceChangeEvent event = new ResourceChangeEvent(item, IResourceChangeEvent.PRE_CLOSE, 1, null);
            try {
                IProject jProject = ResourceUtils.getProject(ProjectManager.getInstance().getCurrentProject());
                Field resourceField = event.getClass().getDeclaredField("resource"); //$NON-NLS-1$
                resourceField.setAccessible(true);
                resourceField.set(event, jProject);
            } catch (Exception e) {
                ExceptionHandler.process(e);
            }
            Workspace ws = (Workspace) ResourcesPlugin.getWorkspace();
            ws.getNotificationManager().broadcastChanges(ws.getElementTree(), event, false);
        }
    }

    @Override
    public boolean isDirty() {
        if (!isEditable()) {
            return false;
        }
        boolean dirty = super.isDirty();
        if (dirty) {
            checkSaveAsEnabled();
        }
        return dirty;
    }

    private void checkSaveAsEnabled() {
        if (!isSaveAsAllowed()) {
            return;
        }
        IAction action = getAction(ActionFactory.SAVE_AS.getId());
        if (action == null) {
            action = ActionFactory.SAVE_AS.create(PlatformUI.getWorkbench().getActiveWorkbenchWindow());
            setAction(ActionFactory.SAVE_AS.getId(), action);
        }
        IAction saveAsAction = getAction(ActionFactory.SAVE_AS.getId());
        if (saveAsAction != null && !getAction(ActionFactory.SAVE_AS.getId()).isEnabled()) {
            getAction(ActionFactory.SAVE_AS.getId()).setEnabled(true);
        }
    }


    @Override
    public boolean isEditorInputReadOnly() {
        return !isEditable();
    }

    @Override
    protected void editorSaved() {

    }

    public void resetItem() throws PersistenceException {
        if (item.getProperty().eResource() == null || item.eResource() == null) {
            IRepositoryService service = CoreRuntimePlugin.getInstance().getRepositoryService();
            IProxyRepositoryFactory factory = service.getProxyRepositoryFactory();
            //
            // Property updated = factory.getUptodateProperty(getItem().getProperty());
            Property updatedProperty = null;
            try {
                factory.initialize();
                IRepositoryViewObject repositoryViewObject = factory.getLastVersion(
                        new Project(ProjectManager.getInstance().getProject(item.getProperty().getItem())),
                        item.getProperty().getId());
                if (repositoryViewObject != null) {
                    updatedProperty = repositoryViewObject.getProperty();
                    item = (FileItem) updatedProperty.getItem();
                }
            } catch (PersistenceException e) {
                ExceptionHandler.process(e);
            }
        }
    }

    @Override
    public void doSave(final IProgressMonitor monitor) {
        IRepositoryService service = CorePlugin.getDefault().getRepositoryService();
        final IProxyRepositoryFactory repFactory = service.getProxyRepositoryFactory();
        try {
            repFactory.lock(item);
        } catch (Exception e) {
            ExceptionHandler.process(e);
        }
        ERepositoryStatus status = repFactory.getStatus(item);
        if (!status.equals(ERepositoryStatus.LOCK_BY_USER) && !repFactory.getRepositoryContext().isEditableAsReadOnly()) {
            MessageDialog.openWarning(getEditorSite().getShell(),
                    Messages.getString("AbstractMultiPageTalendEditor.canNotSaveTitle"),
                    Messages.getString("AbstractMultiPageTalendEditor.canNotSaveMessage.routine"));
            return;
        }
        EList adapters = item.getProperty().eAdapters();
        adapters.remove(dirtyListener);
        super.doSave(monitor);

        try {
            resetItem();
            ByteArray byteArray = item.getContent();
            byteArray.setInnerContentFromFile(((FileEditorInput) getEditorInput()).getFile());
            ITalendProcessJavaProject codeProject;
            if (RoutinesUtil.isInnerCodes(item.getProperty())) {
                codeProject = IRunProcessService.get()
                        .getTalendCodesJarJavaProject(CodesJarResourceCache.getCodesJarByInnerCode((RoutineItem) item));
            } else {
                codeProject = IRunProcessService.get().getTalendCodeJavaProject(ERepositoryObjectType.getItemType(item));
            }
            codeProject.buildModules(new NullProgressMonitor(), null, null);
            // check syntax error
            addProblems();
            String name = "Save Routine"; //$NON-NLS-1$
            RepositoryWorkUnit<Object> repositoryWorkUnit = new RepositoryWorkUnit<Object>(name, this) {

                @Override
                protected void run() throws LoginException, PersistenceException {
                    refreshJobAndSave(repFactory);
                }
            };
            repositoryWorkUnit.setAvoidSvnUpdate(true);
            repositoryWorkUnit.setAvoidUnloadResources(true);
            repFactory.executeRepositoryWorkUnit(repositoryWorkUnit);
            repositoryWorkUnit.throwPersistenceExceptionIfAny();
            // for bug 11930: Unable to save Routines.* in db project

            // repFactory.save(item);
            // startRefreshJob(repFactory);

        } catch (Exception e) {
            // e.printStackTrace();
            ExceptionHandler.process(e);
        }

    }

    private void refreshJobAndSave(final IProxyRepositoryFactory repFactory) throws PersistenceException {
        final IWorkspaceRunnable op = new IWorkspaceRunnable() {

            @Override
            public void run(IProgressMonitor monitor) throws CoreException {
                try {
                    repFactory.save(item);
                } catch (PersistenceException e) {
                    throw new CoreException(new Status(IStatus.ERROR, DesignerPlugin.ID, "Save Routine failed!", e));
                }

            };
        };

        IRunnableWithProgress iRunnableWithProgress = new IRunnableWithProgress() {

            @Override
            public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
                IWorkspace workspace = ResourcesPlugin.getWorkspace();
                try {
                    ISchedulingRule schedulingRule = workspace.getRoot();
                    // the update the project files need to be done in the workspace runnable to avoid all
                    // notification
                    // of changes before the end of the modifications.
                    workspace.run(op, schedulingRule, IWorkspace.AVOID_UPDATE, monitor);
                } catch (CoreException e) {
                    throw new InvocationTargetException(e);
                }

            }
        };

        try {
            PlatformUI.getWorkbench().getProgressService().run(false, false, iRunnableWithProgress);
        } catch (InvocationTargetException e) {
            throw new PersistenceException(e);
        } catch (InterruptedException e) {
            throw new PersistenceException(e);
        }

        setTitleImage(getTitleImage());

    }

    /**
     * add routine compilation errors into problems view.
     */
    private void addProblems() {
        List<Information> informations = Problems.addRoutineFile(rEditorInput.getFile(), item.getProperty());
        item.getProperty().getInformations().clear();
        item.getProperty().getInformations().addAll(informations);
        Problems.refreshProblemTreeView();
    }

    private FileItem item;


    private final AdapterImpl dirtyListener = new AdapterImpl() {

        @Override
        public void notifyChanged(Notification notification) {
            if (notification.getEventType() != Notification.REMOVING_ADAPTER) {
                Display.getDefault().syncExec(new Runnable() {

                    @Override
                    public void run() {
                        firePropertyChange(IEditorPart.PROP_DIRTY);
                    }
                });
            }
        }
    };

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.jdt.internal.ui.javaeditor.JavaEditor#getCorrespondingElement(org.eclipse.jdt.core.IJavaElement)
     */
    @Override
    protected IJavaElement getCorrespondingElement(IJavaElement element) {
        return element;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.jdt.internal.ui.javaeditor.JavaEditor#getElementAt(int)
     */
    @Override
    protected IJavaElement getElementAt(int offset) {
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.ui.IUIRefresher#refreshName()
     */
    @Override
    public void refreshName() {

        ICompilationUnit unit = (ICompilationUnit) this.getInputJavaElement();
        String newName = item.getProperty().getLabel();
        try {
            boolean noError = true;
            // String newName2 = newName + SuffixConstants.SUFFIX_STRING_java;
            String newName2 = newName + ".java"; //$NON-NLS-1$
            if (item instanceof RoutineItem && !unit.getElementName().equals(newName2)) {

                JavaRenameProcessor processor = new RenameCompilationUnitProcessor(unit);
                processor.setNewElementName(newName2);
                RenameRefactoring ref = new RenameRefactoring(processor);
                final PerformRefactoringOperation operation = new PerformRefactoringOperation(ref,
                        CheckConditionsOperation.ALL_CONDITIONS);

                IRunnableWithProgress r = new IRunnableWithProgress() {

                    @Override
                    public void run(final IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
                        Display.getDefault().asyncExec(new Runnable() {

                            @Override
                            public void run() {
                                try {
                                    operation.run(monitor);
                                } catch (CoreException e) {
                                    ExceptionHandler.process(e);
                                }
                            }
                        });

                    }
                };

                PlatformUI.getWorkbench().getProgressService().run(true, true, r);
                RefactoringStatus conditionStatus = operation.getConditionStatus();
                if (conditionStatus != null && conditionStatus.hasError()) {
                    String errorMessage = "Rename " + unit.getElementName() + " to " + newName + " has errors!"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                    RefactoringStatusEntry[] entries = conditionStatus.getEntries();
                    for (RefactoringStatusEntry entry : entries) {
                        errorMessage += "\n>>>" + entry.getMessage(); //$NON-NLS-1$
                    }
                    MessageDialog.openError(this.getSite().getShell(), "Warning", errorMessage); //$NON-NLS-1$
                    noError = false;
                }
            }
            if (noError) {
                doSave(null);
            }
            setName();
        } catch (Exception e) {
            ExceptionHandler.process(e);
        }
    }

    // reference:Process.isLastVersion(Item item)
    @Override
    public boolean isLastVersion(Item item) {
        if (item.getProperty() != null) {
            try {
                List<IRepositoryViewObject> allVersion = ProxyRepositoryFactory.getInstance()
                        .getAllVersion(item.getProperty().getId());

                if (allVersion == null || allVersion.isEmpty()) {
                    return false;
                }
                String lastVersion = VersionUtils.DEFAULT_VERSION;

                for (IRepositoryViewObject object : allVersion) {
                    if (VersionUtils.compareTo(object.getVersion(), lastVersion) > 0) {
                        lastVersion = object.getVersion();
                    }
                }
                if (VersionUtils.compareTo(item.getProperty().getVersion(), lastVersion) == 0) {
                    return true;
                }

            } catch (PersistenceException e) {
                ExceptionHandler.process(e);
            }
        }
        return false;
    }

    @Override
    public boolean isEditorInputModifiable() {
        return isEditable();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.ui.ILastVersionChecker#setLastVersion(java.lang.Boolean)
     */
    @Override
    public void setLastVersion(Boolean lastVersion) {
        // not used yet
    }

    @Override
    protected void createActions() {
        super.createActions();
        getAction(IJavaEditorActionDefinitionIds.SHOW_IN_BREADCRUMB).setEnabled(false);
    }

    @Override
    protected void initializeViewerColors(ISourceViewer viewer) {
        super.initializeViewerColors(viewer);

        StyledText styledText = viewer.getTextWidget();

        bgColorForReadOnlyItem = new Color(styledText.getDisplay(), DesignerColorUtils.getPreferenceReadonlyRGB(
                DesignerColorUtils.READONLY_BACKGROUND_COLOR_NAME, DesignerColorUtils.DEFAULT_READONLY_COLOR));
        bgColorForEditabeItem = new Color(styledText.getDisplay(), DesignerColorUtils.getPreferenceDesignerEditorRGB(
                DesignerColorUtils.JOBDESIGNER_EGITOR_BACKGROUND_COLOR_NAME, DesignerColorUtils.DEFAULT_EDITOR_COLOR));
        if (!isEditable()) {
            styledText.setBackground(bgColorForReadOnlyItem);
        } else {
            styledText.setBackground(bgColorForEditabeItem);
        }
    }

    @Override
    public void doSaveAs() {
        // for the EditorInput of new created RepositoryNode, need to reload
        if (this.rEditorInput.getRepositoryNode() == null) {
            this.rEditorInput.setRepositoryNode(null);
        }
        ERepositoryObjectType type = this.rEditorInput.getRepositoryNode().getObject().getRepositoryObjectType();
        if (type == ERepositoryObjectType.ROUTINES) {
            SaveAsRoutineAction saveAsAction = new SaveAsRoutineAction(this);
            saveAsAction.run();
        } else if (type == ERepositoryObjectType.SQLPATTERNS) {
            SaveAsSQLPatternAction saveAsAction = new SaveAsSQLPatternAction(this);
            saveAsAction.run();
        }

    }
}
