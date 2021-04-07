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
package org.talend.designer.runprocess.java;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IBuildConfiguration;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IJavaProject;
import org.talend.commons.exception.ExceptionHandler;
import org.talend.commons.exception.PersistenceException;
import org.talend.commons.utils.generation.JavaUtils;
import org.talend.core.model.general.Project;
import org.talend.core.model.properties.Property;
import org.talend.core.model.repository.IRepositoryViewObject;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.core.runtime.process.ITalendProcessJavaProject;
import org.talend.core.runtime.process.TalendProcessArgumentConstant;
import org.talend.designer.maven.launch.MavenPomCommandLauncher;
import org.talend.designer.maven.model.MavenSystemFolders;
import org.talend.designer.maven.model.TalendMavenConstants;
import org.talend.designer.maven.tools.MavenPomSynchronizer;
import org.talend.designer.maven.utils.PomUtil;
import org.talend.repository.ProjectManager;
import org.talend.utils.io.FilesUtils;

/**
 * created by ggu on 26 Jan 2015 Detailled comment
 *
 */
public class TalendProcessJavaProject implements ITalendProcessJavaProject {

    private IJavaProject javaProject;

    private final MavenPomSynchronizer synchronizer;

    private boolean useTempPom;

    private String projectTechName;

    private String id;

    private String version;

    public TalendProcessJavaProject(IJavaProject javaProject) {
        super();
        this.javaProject = javaProject;
        this.synchronizer = new MavenPomSynchronizer(this);

    }

    public TalendProcessJavaProject(IJavaProject javaProject, Property property) {
        this(javaProject);
        this.id = property.getId();
        this.version = property.getVersion();
        projectTechName = ProjectManager.getInstance().getProject(property).getTechnicalLabel();
    }

    @Override
    public Property getPropery() {
        ProxyRepositoryFactory factory = ProxyRepositoryFactory.getInstance();
        IRepositoryViewObject object;
        try {
            Project project = ProjectManager.getInstance().getProjectFromProjectTechLabel(projectTechName);
            object = factory.getSpecificVersion(project, id, version, true);
        } catch (PersistenceException e) {
            ExceptionHandler.process(e);
            return null;
        }
        if (object == null) {
            return null;
        }
        return object.getProperty();
    }

    @Override
    public IJavaProject getJavaProject() {
        try {// try to open project.
            tryOpenProject(null, this.javaProject.getProject());
        } catch (CoreException e) {
            //
        }
        return this.javaProject;
    }

    @Override
    public boolean exists() {
        return javaProject != null && javaProject.exists();
    }

    @Override
    public IFile getProjectPom() {
        return getProject().getFile(TalendMavenConstants.POM_FILE_NAME);
    }

    @Override
    public IProject getProject() {
        return getJavaProject().getProject();
    }

    private IFolder createFolder(String path) {
        IProject p = getProject();
        if (p != null) {
            try {
                IFolder folder = p.getFolder(path);
                createSubFolder(null, folder);
                return folder;
            } catch (CoreException e) {
                ExceptionHandler.process(e);
            }
        }
        return null;
    }

    @Override
    public IFolder getSrcFolder() {
        return createFolder(MavenSystemFolders.JAVA.getPath());
    }

    @Override
    public IFolder getTestSrcFolder() {
        return createFolder(MavenSystemFolders.JAVA_TEST.getPath());
    }

    @Override
    public IFolder getExternalResourcesFolder() {
        return createFolder(MavenSystemFolders.EXT_RESOURCES.getPath());
    }

    @Override
    public IFolder getBundleResourcesFolder() {
        return createFolder(MavenSystemFolders.BUNDLE_RESOURCES.getPath());
    }

    @Override
    public IFolder getResourcesFolder() {
        return createFolder(MavenSystemFolders.RESOURCES.getPath());
    }

    @Override
    public IFolder getTestResourcesFolder() {
        return createFolder(MavenSystemFolders.RESOURCES_TEST.getPath());
    }

    @Override
    public IFolder getLibFolder() {
        return createFolder(JavaUtils.JAVA_LIB_DIRECTORY);
    }

    @Override
    public IFolder getOutputFolder() {
        return createFolder(MavenSystemFolders.JAVA.getOutputPath());
    }

    @Override
    public IFolder getTestOutputFolder() {
        return createFolder(MavenSystemFolders.JAVA_TEST.getOutputPath());
    }

    @Override
    public IFolder getAssembliesFolder() {
        return createFolder(MavenSystemFolders.ASSEMBLIES.getPath());
    }

    @Override
    public IFolder getTemplatesFolder() {
        return createFolder(MavenSystemFolders.TEMPLATES.getPath());
    }

    @Override
    public IFolder getItemsFolder() {
        return createFolder(MavenSystemFolders.ITEMS.getPath());
    }

    @Override
    public IFolder getTestsFolder() {
        return createFolder(MavenSystemFolders.TEST_REPORTS.getPath());
    }

    @Override
    public IFolder getTargetFolder() {
        return createFolder(MavenSystemFolders.TARGET.getPath());
    }

    @Override
    public IFolder getTempFolder() {
        return createFolder(MavenSystemFolders.TEMP.getPath());
    }

    @Override
    public IFolder getSrcSubFolder(IProgressMonitor monitor, String path) {
        return createSubFolder(monitor, getSrcFolder(), path);
    }

    @Override
    public IFolder getResourceSubFolder(IProgressMonitor monitor, String path) {
        return createSubFolder(monitor, getExternalResourcesFolder(), path);
    }

    /**
     *
     * will try to create the sub folders which under the base folder.
     */
    @Override
    public IFolder createSubFolder(IProgressMonitor monitor, IFolder baseFolder, String path) {
        if (baseFolder != null && path != null && path.length() > 0) {
            try {
                IFolder subFolder = baseFolder.getFolder(path);
                createSubFolder(monitor, subFolder);
                return subFolder;
            } catch (CoreException e) {
                ExceptionHandler.process(e);
                return null;
            }
        }
        return null;
    }

    /**
     *
     * will try to create the sub folders hierarchically.
     */
    private void createSubFolder(IProgressMonitor monitor, IFolder subFolder) throws CoreException {
        if (subFolder != null) {
            tryOpenProject(monitor, subFolder); // before creating, try to open project.

            IContainer parent = subFolder.getParent();
            if (parent != null && parent.getType() == IResource.FOLDER) {
                IFolder parentFolder = (IFolder) parent;
                if (!parentFolder.exists()) {
                    createSubFolder(monitor, parentFolder);
                }
            }
            if (!subFolder.exists()) {
                subFolder.create(true, true, monitor);
            }
        }
    }

    @Override
    public boolean cleanFolder(IProgressMonitor monitor, IFolder folder) throws CoreException {
        if (folder != null && folder.exists()) {
            tryOpenProject(monitor, folder); // before clean, try to open project.

            IResource[] childrenRecs = folder.members();
            if (childrenRecs.length > 0) {
                for (IResource child : childrenRecs) {
                    child.delete(true, monitor);
                }
            }
            return true;
        }
        return false;
    }

    /**
     *
     * try to open the container's project.
     */
    private void tryOpenProject(IProgressMonitor monitor, IContainer container) throws CoreException {
        IProject project = container.getProject();
        if (!project.isOpen()) { // try to open project.
            project.open(monitor);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.runtime.process.ITalendProcessJavaProject#addChildModule(java.lang.String[])
     */
    @Override
    public void addChildModules(boolean removeOld, String... childModules) {
        try {
            synchronizer.addChildModules(removeOld, childModules);
        } catch (Exception e) {
            ExceptionHandler.process(e);
        }
    }

    @Override
    public void buildModules(IProgressMonitor monitor, String[] childrenModules, Map<String, Object> argumentsMap)
            throws Exception {
        if (argumentsMap == null) {
            argumentsMap = new HashMap<>();
        }
        String goals = (String) argumentsMap.get(TalendProcessArgumentConstant.ARG_GOAL);
        if (childrenModules == null) {
            if (goals != null && goals.trim().length() > 0) {
                mavenBuildCodeProjectPom(goals, TalendMavenConstants.CURRENT_PATH, argumentsMap, monitor);
            } else { // JDT build
                buildWholeCodeProject();
            }
        } else if (childrenModules.length > 0) {
            for (String module : childrenModules) {

                IPath modulePath = new Path(module);
                // remove pom.xml
                if (modulePath.lastSegment().equals(TalendMavenConstants.POM_FILE_NAME)) {
                    modulePath = modulePath.removeLastSegments(1);
                }

                // clean before classes for current job.
                String newModule = modulePath.toString();
                mavenBuildCodeProjectPom(goals, newModule, argumentsMap, monitor);
            }
        } else { // ==0
            // nothing do for empty modules.
        }
    }

    private void mavenBuildCodeProjectPom(String goals, String module, Map<String, Object> argumentsMap, IProgressMonitor monitor)
            throws Exception {
        // cleanBeforeBuilds(module);

        IFile childModulePomFile;
        if (TalendMavenConstants.CURRENT_PATH.equals(module)) {
            childModulePomFile = this.getProject().getFile(TalendMavenConstants.POM_FILE_NAME);
        } else {
            IFolder moduleFolder = this.getProject().getFolder(module);
            childModulePomFile = moduleFolder.getFile(TalendMavenConstants.POM_FILE_NAME);

        }
        if (childModulePomFile.getLocation().toFile().exists()) { // existed
            MavenPomCommandLauncher mavenLauncher = null;
            try {
                // by default is compile
                if (goals == null || goals.trim().length() == 0 || goals.equals(TalendMavenConstants.GOAL_COMPILE)
                        || goals.equals(TalendMavenConstants.GOAL_TEST_COMPILE)) {
                    buildWholeCodeProject();
                } else {
                    mavenLauncher = new MavenPomCommandLauncher(childModulePomFile, goals);
                    mavenLauncher.setArgumentsMap(argumentsMap);
                    mavenLauncher.execute(monitor);
                }
            } finally {
                // package, install and deploy
                if (TalendMavenConstants.GOAL_PACKAGE.equals(goals) || TalendMavenConstants.GOAL_INSTALL.equals(goals)
                        || TalendMavenConstants.GOAL_DEPLOY.equals(goals)) {
                    PomUtil.restorePomFile(this);
                    // in case of there has modified joblet's pom for loop dependencies
                    PomUtil.restoreJobletPoms();
                }
            }
        } else {
            throw new RuntimeException("The pom.xml is not existed. Can't build the job: " + module); //$NON-NLS-1$
        }
    }

    /**
     * DOC ggu Comment method "buildWholeCodeProject".
     */
    @Override
    public void buildWholeCodeProject() {
        try {
            IProgressMonitor monitor = new NullProgressMonitor();
            IProject project = getProject();
            if (!project.isSynchronized(IResource.DEPTH_INFINITE)) {
                project.refreshLocal(IResource.DEPTH_INFINITE, monitor);
            }
            ResourcesPlugin.getWorkspace().build(new IBuildConfiguration[] { getProject().getActiveBuildConfig() },
                    IncrementalProjectBuilder.FULL_BUILD, true, monitor);
            ResourcesPlugin.getWorkspace().build(new IBuildConfiguration[] { getProject().getActiveBuildConfig() },
                    IncrementalProjectBuilder.INCREMENTAL_BUILD, true, monitor);
            // project.build(IncrementalProjectBuilder.FULL_BUILD, monitor);
            // project.build(IncrementalProjectBuilder.INCREMENTAL_BUILD, monitor);
        } catch (CoreException e) {
            ExceptionHandler.process(e);
        }
    }

    private void cleanBeforeBuilds(String module) {
        IPath srcPath = this.getSrcFolder().getProjectRelativePath();
        IContainer outputContainer;
        if (TalendMavenConstants.CURRENT_PATH.equals(module)) {
            outputContainer = this.getOutputFolder();
        } else {
            IFolder moduleFolder = this.getProject().getFolder(module);
            IPath modulePath = moduleFolder.getProjectRelativePath().makeRelativeTo(srcPath);
            outputContainer = this.getOutputFolder().getFolder(modulePath);
        }
        try {
            File jobFolder = outputContainer.getLocation().toFile();
            if (jobFolder.exists()) {
                FilesUtils.deleteFile(jobFolder, true);
            }
            outputContainer.getParent().refreshLocal(IResource.DEPTH_INFINITE, null);
        } catch (CoreException e) {
            ExceptionHandler.process(e);
        }
    }

    @Override
    public void updateRoutinesPom(final boolean withBuild, boolean inBackgroud) {
        return;
    }

    private IStatus processRoutinesPom(boolean withBuild) {
        return Status.OK_STATUS;
    }

    @Override
    public void cleanMavenFiles(IProgressMonitor monitor) throws Exception {
        this.synchronizer.cleanMavenFiles(monitor);
    }

    @Override
    public void regenerateMainProjectPom(IProgressMonitor monitor) throws Exception {
        // this.synchronizer.regenerateMainProjectPom(monitor, null);
    }

    @Override
    public boolean isUseTempPom() {
        return useTempPom;
    }

    @Override
    public void setUseTempPom(boolean useTempPom) {
        this.useTempPom = useTempPom;
    }

}
