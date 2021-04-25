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
package org.talend.designer.runprocess;

import java.io.File;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.ui.IStartup;
import org.talend.commons.exception.ExceptionHandler;
import org.talend.commons.utils.generation.JavaUtils;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.runtime.process.ITalendProcessJavaProject;
import org.talend.designer.maven.model.TalendMavenConstants;

/**
 * Delete all the perl and java jobs when T.O.S start up.
 *
 * yzhang class global comment. Detailled comment <br/>
 *
 * $Id: DeleteAllJobWhenStartUp.java 下午02:50:26 2007-5-29 +0000 (2007-5-29) yzhang $
 *
 */
public class DeleteAllJobWhenStartUp implements IStartup {

    public static boolean executed;

    public void startup() {
        // won't do clean projects when startup
        // will do when every individual project created.
        // earlyStartup();
    }

    @Deprecated
    public void startup(boolean pluginModel) {
        startup();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.ui.IStartup#earlyStartup()
     */
    @Override
    public void earlyStartup() {
        if (!GlobalServiceRegister.getDefault().isServiceRegistered(IRunProcessService.class)) {
            return;
        }
        IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();

        IProject codeProject = root.getProject(TalendMavenConstants.PROJECT_NAME);
        if (!codeProject.exists() || !codeProject.isAccessible()) {
            return;
        }
        final IWorkspaceRunnable op = new IWorkspaceRunnable() {

            @Override
            public void run(IProgressMonitor monitor) throws CoreException {
                IRunProcessService processService = (IRunProcessService) GlobalServiceRegister.getDefault().getService(
                        IRunProcessService.class);
                ITalendProcessJavaProject talendJavaProject = processService.getTempJavaProject();
                if (talendJavaProject != null) {
                    IJavaProject jProject = talendJavaProject.getJavaProject();
                    if (!jProject.isOpen()) {
                        jProject.open(monitor);
                    }
                    // empty the src/main/java...
                    IFolder srcFolder = talendJavaProject.getSrcFolder();
                    talendJavaProject.cleanFolder(monitor, srcFolder);
                    // contexts
                    IFolder resourcesFolder = talendJavaProject.getExternalResourcesFolder();
                    emptyContexts(monitor, resourcesFolder, talendJavaProject);

                    // empty the outputs, target
                    IFolder targetFolder = talendJavaProject.getTargetFolder();
                    talendJavaProject.cleanFolder(monitor, targetFolder);

                    // empty the src/test/java
                    IFolder testSrcFolder = talendJavaProject.getTestSrcFolder();
                    talendJavaProject.cleanFolder(monitor, testSrcFolder);

                    // empty the src/test/java (main for contexts)
                    IFolder testResourcesFolder = talendJavaProject.getTestResourcesFolder();
                    talendJavaProject.cleanFolder(monitor, testResourcesFolder);

                    // empty temp
                    IFolder tempFolder = talendJavaProject.getTempFolder();
                    talendJavaProject.cleanFolder(monitor, tempFolder);

                    // empty lib/...
                    IFolder libFolder = talendJavaProject.getLibFolder();
                    talendJavaProject.cleanFolder(monitor, libFolder);

                    // rules
                    IFolder rulesResFolder = talendJavaProject.getResourceSubFolder(monitor, JavaUtils.JAVA_RULES_DIRECTORY);
                    talendJavaProject.cleanFolder(monitor, rulesResFolder);

                    // sqltempalte
                    IFolder sqlTemplateResFolder = talendJavaProject.getResourceSubFolder(monitor,
                            JavaUtils.JAVA_SQLPATTERNS_DIRECTORY);
                    talendJavaProject.cleanFolder(monitor, sqlTemplateResFolder);
                }
            };

        };
        IWorkspace workspace = ResourcesPlugin.getWorkspace();
        try {
            ISchedulingRule schedulingRule = workspace.getRoot();
            // the update the project files need to be done in the workspace runnable to avoid all
            // notification
            // of changes before the end of the modifications.
            workspace.run(op, schedulingRule, IWorkspace.AVOID_UPDATE, new NullProgressMonitor());
        } catch (CoreException e) {
            if (e.getCause() != null) {
                ExceptionHandler.process(e.getCause());
            } else {
                ExceptionHandler.process(e);
            }
        }

        executed = true;

    }

    private void emptyContexts(IProgressMonitor monitor, IContainer baseContainer, ITalendProcessJavaProject talendJavaProject)
            throws CoreException {
        IPath location = baseContainer.getLocation();
        File[] listFiles = location.toFile().listFiles();
        if (listFiles != null) { // for each project sub folder
            for (File child : listFiles) {
                File testContextFile = findTestContextFile(child);
                if (testContextFile != null) {
                    IPath testContextPath = new Path(testContextFile.toString());
                    IPath testRelativePath = testContextPath.makeRelativeTo(location);
                    String projectSegment = testRelativePath.segment(0);
                    IFolder folder = baseContainer.getFolder(new Path(projectSegment));
                    talendJavaProject.cleanFolder(monitor, folder);
                    if (folder.exists()) { // also delete it
                        folder.delete(true, monitor);
                    }
                }
            }
        }
    }

    private File findTestContextFile(File file) {
        if (file != null) {
            if (file.getName().endsWith(JavaUtils.JAVA_CONTEXT_EXTENSION)
                    && file.getParentFile().getName().equals(JavaUtils.JAVA_CONTEXTS_DIRECTORY)) {
                return file;
            }
            if (file.isDirectory()) {
                File[] listFiles = file.listFiles();
                if (listFiles != null) {
                    for (File f : listFiles) {
                        File contextFile = findTestContextFile(f);
                        if (contextFile != null) {
                            return contextFile;
                        }
                    }
                }
            }
        }
        return null;
    }
}
