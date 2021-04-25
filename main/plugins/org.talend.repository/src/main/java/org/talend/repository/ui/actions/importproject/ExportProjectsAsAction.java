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
package org.talend.repository.ui.actions.importproject;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.undo.CreateFileOperation;
import org.eclipse.ui.ide.undo.DeleteResourcesOperation;
import org.eclipse.ui.ide.undo.WorkspaceUndoUtil;
import org.talend.commons.exception.PersistenceException;
import org.talend.commons.ui.runtime.exception.ExceptionHandler;
import org.talend.commons.utils.data.container.RootContainer;
import org.talend.commons.utils.workbench.resources.ResourceUtils;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.language.ECodeLanguage;
import org.talend.core.model.general.Project;
import org.talend.core.model.properties.RoutineItem;
import org.talend.core.model.repository.IRepositoryViewObject;
import org.talend.core.model.repository.ResourceModelUtils;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.core.ui.branding.IBrandingService;
import org.talend.designer.core.model.utils.emf.component.IMPORTType;
import org.talend.repository.i18n.Messages;
import org.talend.repository.model.IProxyRepositoryFactory;
import org.talend.repository.ui.wizards.newproject.copyfromeclipse.TalendZipFileExportWizard;

/**
 * Action used to refresh a repository view.<br/>
 *
 * $Id: RefreshAction.java 824 2006-12-01 15:49:55 +0000 (ven., 01 déc. 2006) smallet $
 *
 */
public class ExportProjectsAsAction extends Action implements IWorkbenchWindowActionDelegate {

    private static final String LIB = "lib"; //$NON-NLS-1$

    private static final String CODE = "code"; //$NON-NLS-1$

    private static final String TYPE = "FOLDER"; //$NON-NLS-1$

    private IWorkbenchWindow window;

    public ExportProjectsAsAction() {
        super();
    }

    @Override
    public void run() {

        // Refresh Navigator view before export operation, see bug 4595
        Job job = new Job(Messages.getString("ExportCommandAction.refreshWorkspace")) { //$NON-NLS-1$

            @Override
            protected IStatus run(IProgressMonitor monitor) {
                monitor.beginTask(ExportProjectsAsAction.this.getToolTipText(), IProgressMonitor.UNKNOWN);
                try {
                    ResourcesPlugin.getWorkspace().getRoot().refreshLocal(IResource.DEPTH_INFINITE, null);
                } catch (CoreException e) {
                    e.printStackTrace();
                }
                monitor.done();
                return Status.OK_STATUS;
            }
        };
        job.setUser(true);
        job.setPriority(Job.BUILD);
        job.schedule();
        initializeExternalLibraries();
        Shell activeShell = Display.getCurrent().getActiveShell();
        TalendZipFileExportWizard docWizard = new TalendZipFileExportWizard();
        WizardDialog dialog = new WizardDialog(activeShell, docWizard);
        IBrandingService brandingService = (IBrandingService) GlobalServiceRegister.getDefault().getService(
                IBrandingService.class);
        if (brandingService.isPoweredbyTalend()) {
            docWizard.setWindowTitle(Messages.getString("ExportProjectsAsAction.actionTitle")); //$NON-NLS-1$
        } else {
            docWizard.setWindowTitle(Messages.getString(
                    "ExportProjectsAsAction.actionTitleForOthers", brandingService.getShortProductName())); //$NON-NLS-1$
        }
        dialog.create();
        dialog.open();

        clearExternalLibraries();

    }

    /**
     * DOC zwang Comment method "clearExternalLibraries".
     */
    private void clearExternalLibraries() {
        List<IResource> resourcesToDelete = new ArrayList<IResource>();
        IResource[] resourceArray = null;

        try {
            IProxyRepositoryFactory repositoryFactory = ProxyRepositoryFactory.getInstance();
            // fix for bug 15454
            Project[] projects = repositoryFactory.readProject(true);
            for (Project project : projects) {
                IProject fsProject = ResourceUtils.getProject(project);
                IFolder libJavaFolder = fsProject.getFolder(ExportProjectsAsAction.LIB);
                if (!libJavaFolder.exists()) {
                    continue;
                }

                // final DeleteResourcesOperation operation = new DeleteResourcesOperation(new IResource[] {
                // libJavaFolder },
                // IDEWorkbenchMessages.DeleteResourceAction_operationLabel, true);
                final DeleteResourcesOperation operation = new DeleteResourcesOperation(new IResource[] { libJavaFolder },
                        Messages.getString("IDEWorkbenchMessages.DeleteResourceAction_operationLabel"), true); //$NON-NLS-1$
                PlatformUI.getWorkbench().getOperationSupport().getOperationHistory()
                        .execute(operation, null, WorkspaceUndoUtil.getUIInfoAdapter(window.getShell()));
            }
        } catch (Exception e) {
            // e.printStackTrace();
            ExceptionHandler.process(e);
        }
    }

    /**
     * DOC bqian Comment method "test".
     */
    private void initializeExternalLibraries() {
        initializeLibPath();
        // final InputStream initialContents = null;
        final Map<Project, List<LinkTargetStore>> map = getProjectAndRelatedLinks();

        IRunnableWithProgress op = new IRunnableWithProgress() {

            @Override
            public void run(IProgressMonitor monitor) {
                Set<Project> projects = map.keySet();
                monitor.beginTask(Messages.getString("ExportProjectsAsAction.createLiberariesLink"), projects.size()); //$NON-NLS-1$

                for (Project project : projects) {
                    monitor.setTaskName(Messages.getString("ExportProjectsAsAction.processProject") + project.getLabel()); //$NON-NLS-1$
                    List<LinkTargetStore> links = map.get(project);

                    for (LinkTargetStore store : links) {
                        // CreateFileOperation op = new CreateFileOperation(store.file, store.uri, null,
                        // IDEWorkbenchMessages.WizardNewFileCreationPage_title);
                        CreateFileOperation op = new CreateFileOperation(store.file, store.uri, null,
                                Messages.getString("IDEWorkbenchMessages.WizardNewFileCreationPage_title")); //$NON-NLS-1$
                        try {
                            PlatformUI.getWorkbench().getOperationSupport().getOperationHistory()
                                    .execute(op, monitor, WorkspaceUndoUtil.getUIInfoAdapter(window.getShell()));
                        } catch (final ExecutionException e) {
                            ExceptionHandler.process(e);
                        }
                    }
                    monitor.worked(1);
                }
                monitor.done();
            }
        };
        try {
            PlatformUI.getWorkbench().getActiveWorkbenchWindow().run(true, true, op);
        } catch (Exception e) {
            // e.printStackTrace();
            ExceptionHandler.process(e);
        }
    }

    /**
     * DOC bqian Comment method "getProjectAndRelatedLinks".
     *
     * @return
     */
    private Map<Project, List<LinkTargetStore>> getProjectAndRelatedLinks() {
        Map<Project, List<LinkTargetStore>> map = new HashMap<Project, List<LinkTargetStore>>();
        IProxyRepositoryFactory repositoryFactory = ProxyRepositoryFactory.getInstance();
        try {
            // fix for bug 15454
            Project[] projects = repositoryFactory.readProject(true);
            for (Project project : projects) {
                IProject fsProject = ResourceModelUtils.getProject(project);
                // IFolder libJavaFolder = fsProject.getFolder(ExportProjectsAsAction.CODE);
                // if (libJavaFolder.exists()) {
                // List<LinkTargetStore> links = getLinksFromProject(project);
                // map.put(project, links);
                // }
            }
        } catch (Exception e) {
            // e.printStackTrace();
            ExceptionHandler.process(e);
        }

        return map;
    }

    /**
     * DOC zwang Comment method "getLinksFromProject".
     *
     * @param project
     * @return
     */
    private List<LinkTargetStore> getLinksFromProject(Project project) {
        List<IMPORTType> imports = null;
        String linkTarget = null;

        RootContainer<String, IRepositoryViewObject> routines = getRoutineFromProject(project);
        List<LinkTargetStore> paths = new ArrayList<LinkTargetStore>();
        String language = project.getLanguage().getName().trim();

        for (IRepositoryViewObject obj : routines.getMembers()) {
            RoutineItem routine = (RoutineItem) obj.getProperty().getItem();
            imports = routine.getImports();

            if (language != null) {
                if (project != null && ECodeLanguage.JAVA.getName().equals(language)) {

                    IPath containerPath = getContainerFullPath(project.getTechnicalLabel());
                    for (IMPORTType importType : imports) {
                        try {
                            LinkTargetStore store = new LinkTargetStore();
                            IPath newFilePath = containerPath.append(ExportProjectsAsAction.LIB + java.io.File.separatorChar
                                    + ECodeLanguage.JAVA.getName() + java.io.File.separatorChar + importType.getMODULE());

                            linkTarget = EXTERNAL_LIB_JAVA_PATH + java.io.File.separatorChar + importType.getMODULE();

                            URI path = new URI(linkTarget.replace(java.io.File.separatorChar, '/'));
                            store.file = ResourcesPlugin.getWorkspace().getRoot().getFile(newFilePath);
                            store.uri = path;
                            paths.add(store);
                        } catch (Exception e) {
                            ExceptionHandler.process(e);
                        }
                    }
                }
            }
        }
        return paths;
    }

    /**
     * DOC bqian Comment method "getRouineFromProject".
     *
     * @param project
     */
    private RootContainer<String, IRepositoryViewObject> getRoutineFromProject(Project project) {
        ProxyRepositoryFactory factory = ProxyRepositoryFactory.getInstance();

        RootContainer<String, IRepositoryViewObject> routines = null;
        try {
            routines = factory.getRoutineFromProject(project);
        } catch (PersistenceException e) {
            // TODO Auto-generated catch block
            // e.printStackTrace();
            ExceptionHandler.process(e);
        }
        return routines;

    }

    public final static String EXTERNAL_LIB_JAVA_PATH = "external_lib_java_path"; //$NON-NLS-1$

    public final static String EXTERNAL_LIB_PERL_PATH = "external_lib_perl_path"; //$NON-NLS-1$

    /**
     * DOC bqian Comment method "initializeLibPath".
     */
    private void initializeLibPath() {
        // IPathVariableManager pathVariableManager = ResourcesPlugin.getWorkspace().getPathVariableManager();
        // ILibrariesService libService = CorePlugin.getDefault().getLibrariesService();
        // String libPath = libService.getLibrariesPath();
        // try {
        //            Pattern javaPattern = Pattern.compile("(.*)java$"); //$NON-NLS-1$
        //            Pattern perlPattern = Pattern.compile("(.*)perl$"); //$NON-NLS-1$
        // Matcher javaMatcher = javaPattern.matcher(libPath);
        // Matcher perlMatcher = perlPattern.matcher(libPath);
        // if (javaMatcher.find()) {
        // pathVariableManager.setValue(EXTERNAL_LIB_JAVA_PATH, new Path(libPath));
        // pathVariableManager.setValue(EXTERNAL_LIB_PERL_PATH,
        // new Path(javaMatcher.group(1) + ECodeLanguage.PERL.getName()));
        // } else if (perlMatcher.find()) {
        // pathVariableManager.setValue(EXTERNAL_LIB_PERL_PATH, new Path(libPath));
        // pathVariableManager.setValue(EXTERNAL_LIB_JAVA_PATH,
        // new Path(perlMatcher.group(1) + ECodeLanguage.JAVA.getName()));
        // }
        // } catch (Exception e) {
        // ExceptionHandler.process(e);
        // }
    }

    /**
     * Returns the currently entered container name. Null if the field is empty. Note that the container may not exist
     * yet if the user entered a new container name in the field.
     *
     * @return IPath
     */
    public IPath getContainerFullPath(String pathName) {
        // TODO the name of current project
        if (pathName == null || pathName.length() < 1) {
            return null;
        }
        // The user may not have made this absolute so do it for them
        return (new Path(pathName)).makeAbsolute();
    }

    @Override
    public void dispose() {
    }

    @Override
    public void init(IWorkbenchWindow window) {
        this.window = window;
    }

    @Override
    public void run(IAction action) {
        run();
    }

    @Override
    public void selectionChanged(IAction action, ISelection selection) {
    }

    /**
     * use to store the file and the file's corresponding link target temporarily. <br/>
     *
     * $Id: talend.epf 1 2006-09-29 17:06:40 +0000 (ææäº, 29 ä¹æ 2006) nrousseau $
     *
     */
    class LinkTargetStore {

        IFile file;

        URI uri;
    }

}
