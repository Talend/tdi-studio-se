// ============================================================================
//
// Copyright (C) 2006-2017 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.componentdesigner.manager;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResourceStatus;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.undo.CreateProjectOperation;
import org.eclipse.ui.ide.undo.WorkspaceUndoUtil;
import org.eclipse.ui.statushandlers.StatusAdapter;
import org.eclipse.ui.statushandlers.StatusManager;
import org.talend.commons.utils.generation.JavaUtils;
import org.talend.componentdesigner.ComponentDesigenerPlugin;
import org.talend.componentdesigner.PluginConstant;
import org.talend.componentdesigner.i18n.internal.Messages;
import org.talend.componentdesigner.ui.progress.ProgressUI;
import org.talend.core.ui.utils.PluginUtil;

/**
 * @author rli
 * 
 */
public final class ComponentProjectManager {

    // cache of newly-created project
    private IProject project;

    private final String projDir = PluginConstant.EMPTY_STRING;

    private static ComponentProjectManager manager = new ComponentProjectManager();

    public static ComponentProjectManager getInstance() {
        return manager;
    }

    private ComponentProjectManager() {

    }

    /**
     * Creates a new project resource with the selected name.
     * <p>
     * In normal usage, this method is invoked after the user has pressed Finish on the wizard; the enablement of the
     * Finish button implies that all controls on the pages currently contain valid values.
     * </p>
     * <p>
     * Note that this wizard caches the new project once it has been successfully created; subsequent invocations of
     * this method will answer the same project resource without attempting to create it again.
     * </p>
     * 
     * @return the created project resource, or <code>null</code> if the project was not created
     */
    public IProject createNewProject(String directroy, String projectName, Shell shell) {
        if (projDir.equals(directroy)) {
            return project;
        }

        final Shell currentShell = shell;

        // get a project handle
        final IProject newProjectHandle = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
        if (newProjectHandle.getRawLocation() != null) {
            if (newProjectHandle.getRawLocation().equals(directroy)) {
                return newProjectHandle;
            } else {
                try {
                    newProjectHandle.delete(false, true, null);
                } catch (CoreException e) {
                    // e.printStackTrace();
                    org.talend.componentdesigner.exception.ExceptionHandler.process(e);
                }
            }
        }

        // final IJavaProject javaProjHandle = JavaCore.create(newProjectHandle);
        // get a project descriptor
        URI location = null;
        if (directroy == null || directroy.equals(PluginConstant.EMPTY_STRING)) {
            return null;
        } else {
            location = new File(directroy).toURI();
        }

        IWorkspace workspace = ResourcesPlugin.getWorkspace();
        final IProjectDescription description = workspace.newProjectDescription(newProjectHandle.getName());
        description.setLocationURI(location);

        // create the new project operation
        IRunnableWithProgress op = new IRunnableWithProgress() {

            public void run(IProgressMonitor monitor) throws InvocationTargetException {
                CreateProjectOperation op = new CreateProjectOperation(description,
                        Messages.getString("ComponentProjectManager.NewProject")); //$NON-NLS-1$
                try {
                    PlatformUI.getWorkbench().getOperationSupport().getOperationHistory()
                            .execute(op, monitor, WorkspaceUndoUtil.getUIInfoAdapter(currentShell));
                } catch (ExecutionException e) {
                    throw new InvocationTargetException(e);
                }
            }
        };

        // run the new project creation o`peration
        try {
            ProgressUI.popProgressDialog(op, shell);
        } catch (InterruptedException e) {
            return null;
        } catch (InvocationTargetException e) {
            Throwable t = e.getTargetException();
            if (t instanceof ExecutionException && t.getCause() instanceof CoreException) {
                CoreException cause = (CoreException) t.getCause();
                StatusAdapter status;
                if (cause.getStatus().getCode() == IResourceStatus.CASE_VARIANT_EXISTS) {
                    status = new StatusAdapter(

                    new Status(IStatus.WARNING, ComponentDesigenerPlugin.PLUGIN_ID, IStatus.WARNING, Messages.getString(
                            "ComponentProjectManager.WarningMsg", newProjectHandle.getName()) //$NON-NLS-1$
                            , cause));
                } else {
                    status = new StatusAdapter(new Status(cause.getStatus().getSeverity(), ComponentDesigenerPlugin.PLUGIN_ID,
                            cause.getStatus().getSeverity(),
                            Messages.getString("ComponentProjectManager.CreationProblems"), cause)); //$NON-NLS-1$
                }
                status.setProperty(StatusAdapter.TITLE_PROPERTY, Messages.getString("ComponentProjectManager.CreationProblems")); //$NON-NLS-1$
                StatusManager.getManager().handle(status, StatusManager.BLOCK);
            } else {
                StatusAdapter status = new StatusAdapter(new Status(IStatus.WARNING, ComponentDesigenerPlugin.PLUGIN_ID, 0,
                        Messages.getString("ComponentProjectManager.InternalErrorMsg", t.getMessage()), t)); //$NON-NLS-1$
                status.setProperty(StatusAdapter.TITLE_PROPERTY, Messages.getString("ComponentProjectManager.CreationProblems")); //$NON-NLS-1$
                StatusManager.getManager().handle(status, StatusManager.LOG | StatusManager.BLOCK);
            }
            return null;
        }

        project = newProjectHandle;

        return project;
    }

    /**
     * 
     * DOC ycbai Convert the project to java project and initialize its classpath.
     * 
     * @param project
     * @param shell
     */
    public void configProject(final IProject project, Shell shell) {
        IRunnableWithProgress op = new IRunnableWithProgress() {

            public void run(IProgressMonitor monitor) throws InvocationTargetException {
                monitor.beginTask("Configure project...", 4);
                try {
                    IJavaProject javaProject = JavaCore.create(project);
                    JavaUtils.addJavaNature(project, new SubProgressMonitor(monitor, 1));
                    initializeClasspath(javaProject, new SubProgressMonitor(monitor, 3));
                } catch (OperationCanceledException e) {
                    e.printStackTrace();
                } catch (CoreException e) {
                    e.printStackTrace();
                } finally {
                    monitor.done();
                }
            }

        };

        try {
            ProgressUI.popProgressDialog(op, shell);
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * DOC ycbai Initialize classpath of project.
     * 
     * @param project
     * @param monitor
     * @throws OperationCanceledException
     * @throws CoreException
     */
    private void initializeClasspath(IJavaProject project, IProgressMonitor monitor) throws OperationCanceledException,
            CoreException {
        if (monitor != null && monitor.isCanceled()) {
            throw new OperationCanceledException();
        }
        if (project == null)
            return;
        IClasspathEntry[] entries = null;
        List<IClasspathEntry> cpEntries = new ArrayList<IClasspathEntry>();
        cpEntries.addAll(Arrays.asList(getDefaultJREClasspathEntries()));
        cpEntries.addAll(getDefaultUtilClasspathEntries());
        entries = (IClasspathEntry[]) cpEntries.toArray(new IClasspathEntry[cpEntries.size()]);
        if (monitor != null)
            monitor.worked(1);

        IPath output = getOutputLocation();
        IProgressMonitor subProgressMonitor = monitor == null ? new NullProgressMonitor() : new SubProgressMonitor(monitor, 2);
        project.setRawClasspath(entries, output, subProgressMonitor);
    }

    /**
     * DOC ycbai Get default jre classpath entries.
     * 
     * @return
     */
    private IClasspathEntry[] getDefaultJREClasspathEntries() {
        IPath path = new Path(
                "org.eclipse.jdt.launching.JRE_CONTAINER/org.eclipse.jdt.internal.debug.ui.launcher.StandardVMType/" //$NON-NLS-1$
                        + JavaUtils.getDefaultEEName());
        return new IClasspathEntry[] { JavaCore.newContainerEntry(path) };
    }

    /**
     * DOC ycbai Get default util classpath entries.
     * 
     * @return
     */
    private List<IClasspathEntry> getDefaultUtilClasspathEntries() {
        List<IClasspathEntry> ces = new ArrayList<IClasspathEntry>();
        addLibClasspathEntries(ces, "org.talend.core.runtime"); //$NON-NLS-1$
        addLibClasspathEntries(ces, "org.talend.metadata.managment"); //$NON-NLS-1$
        addLibClasspathEntries(ces, "org.talend.core"); //$NON-NLS-1$
        addLibClasspathEntries(ces, "org.talend.designer.codegen"); //$NON-NLS-1$
        return ces;
    }

    /**
     * DOC ycbai Add lib classpath entries.
     * 
     * @param libClasspaths
     * @param entryId
     */
    private void addLibClasspathEntries(List<IClasspathEntry> libClasspaths, String entryId) {
        String path = PluginUtil.getPluginInstallPath(entryId);
        if (StringUtils.isNotEmpty(path)) {
            libClasspaths.add(JavaCore.newLibraryEntry(new Path(path), null, null));
        }
    }

    /**
     * DOC ycbai Get the default output location.
     * 
     * @return
     */
    public IPath getOutputLocation() {
        IPath outputLocationPath = new Path(project.getName()).makeAbsolute();
        return outputLocationPath;
    }

    /**
     * Returns the newly created project.
     * 
     * @return the created project, or <code>null</code> if project not created
     */
    public IProject getProject() {
        return project;
    }

}
