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

import static org.talend.designer.maven.model.TalendJavaProjectConstants.*;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.talend.commons.exception.ExceptionHandler;
import org.talend.commons.utils.generation.JavaUtils;
import org.talend.core.CorePlugin;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.IESBService;
import org.talend.core.PluginChecker;
import org.talend.core.model.general.Project;
import org.talend.core.model.process.IContext;
import org.talend.core.model.process.IProcess;
import org.talend.core.model.process.ProcessUtils;
import org.talend.core.model.properties.Item;
import org.talend.core.model.properties.JobletProcessItem;
import org.talend.core.model.properties.ProcessItem;
import org.talend.core.model.properties.ProjectReference;
import org.talend.core.model.properties.Property;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.model.repository.IRepositoryViewObject;
import org.talend.core.model.routines.RoutinesUtil;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.core.repository.utils.ItemResourceUtil;
import org.talend.core.runtime.process.ITalendProcessJavaProject;
import org.talend.core.runtime.process.LastGenerationInfo;
import org.talend.core.runtime.process.TalendProcessOptionConstants;
import org.talend.core.ui.ITestContainerProviderService;
import org.talend.core.utils.BitwiseOptionUtils;
import org.talend.designer.core.IDesignerCoreService;
import org.talend.designer.maven.model.MavenSystemFolders;
import org.talend.designer.maven.model.TalendMavenConstants;
import org.talend.designer.maven.tools.AggregatorPomsHelper;
import org.talend.designer.maven.tools.BuildCacheManager;
import org.talend.designer.maven.tools.CodesJarM2CacheManager;
import org.talend.designer.maven.tools.MavenPomSynchronizer;
import org.talend.designer.maven.tools.creator.CreateMavenCodeProject;
import org.talend.designer.maven.utils.MavenProjectUtils;
import org.talend.designer.maven.utils.PomUtil;
import org.talend.designer.maven.utils.TalendCodeProjectUtil;
import org.talend.designer.runprocess.IProcessor;
import org.talend.designer.runprocess.ProcessorUtilities;
import org.talend.designer.runprocess.maven.MavenJavaProcessor;
import org.talend.repository.ProjectManager;
import org.talend.repository.RepositoryWorkUnit;
import org.talend.utils.io.FilesUtils;

/**
 * DOC zwxue class global comment. Detailled comment
 */
public class TalendJavaProjectManager {

    private static Map<String, ITalendProcessJavaProject> talendCodeJavaProjects = new HashMap<>();

    private static Map<String, ITalendProcessJavaProject> talendCodesJarJavaProjects = new HashMap<>();

    private static Map<String, ITalendProcessJavaProject> talendJobJavaProjects = new HashMap<>();

    private static ITalendProcessJavaProject tempJavaProject;

    public static void initJavaProjects(IProgressMonitor monitor, Project project) {

        RepositoryWorkUnit<Object> workUnit = new RepositoryWorkUnit<Object>("create aggregator poms") { //$NON-NLS-1$

            @Override
            protected void run() {
                final IWorkspaceRunnable op = new IWorkspaceRunnable() {

                    @Override
                    public void run(IProgressMonitor monitor) throws CoreException {
                        try {
                            // create aggregator poms
                            AggregatorPomsHelper helper = new AggregatorPomsHelper(project.getTechnicalLabel());
                            // create poms folder.
                            IFolder poms = createFolderIfNotExist(helper.getProjectPomsFolder(), monitor);

                            // codes
                            IFolder code = createFolderIfNotExist(poms.getFolder(DIR_CODES), monitor);
                            // routines
                            createFolderIfNotExist(code.getFolder(DIR_ROUTINES), monitor);

                            // beans
                            if (ProcessUtils.isRequiredBeans(null)) {
                                createFolderIfNotExist(code.getFolder(DIR_BEANS), monitor);
                            }

                            // jobs
                            IFolder jobs = createFolderIfNotExist(poms.getFolder(DIR_JOBS), monitor);
                            // process
                            createFolderIfNotExist(jobs.getFolder(DIR_PROCESS), monitor);
                            // process_mr
                            if (PluginChecker.isMapReducePluginLoader()) {
                                createFolderIfNotExist(jobs.getFolder(DIR_PROCESS_MR), monitor);
                            }
                            // process_storm
                            if (PluginChecker.isStormPluginLoader()) {
                                createFolderIfNotExist(jobs.getFolder(DIR_PROCESS_STORM), monitor);
                            }
                            // routes
                            if (PluginChecker.isRouteLoaded()) {
                                createFolderIfNotExist(jobs.getFolder(DIR_PROCESS_ROUTES), monitor);
                            }
                            // services
                            if (PluginChecker.isServiceLoaded()) {
                                createFolderIfNotExist(jobs.getFolder(DIR_PROCESS_SERVICES), monitor);
                            }
                            // joblets
                            if (PluginChecker.isJobLetPluginLoaded()) {
                                createFolderIfNotExist(jobs.getFolder(DIR_JOBLETS), monitor);
                            }
                            // joblets_spark
                            if (PluginChecker.isSparkJobLetPluginLoaded()) {
                                createFolderIfNotExist(jobs.getFolder(DIR_JOBLETS_SPARK), monitor);
                            }
                            // joblets_spark_streaming
                            if (PluginChecker.isSparkStreamingJobLetPluginLoaded()) {
                                createFolderIfNotExist(jobs.getFolder(DIR_JOBLETS_SPARK_STREAMING), monitor);
                            }
                            // routelets
                            if (PluginChecker.isRouteletLoaded()) {
                                createFolderIfNotExist(jobs.getFolder(DIR_ROUTELETS), monitor);
                            }
                            helper.createRootPom(monitor);
                            List<ProjectReference> references = project.getProjectReferenceList(true);
                            for (ProjectReference ref : references) {
                                createRefRootPoms(monitor, new Project(ref.getReferencedProject()));
                            }
                        } catch (Exception e) {
                            ExceptionHandler.process(e);
                        }
                    }
                };
                try {
                    IWorkspace workspace = ResourcesPlugin.getWorkspace();
                    ISchedulingRule schedulingRule = workspace.getRoot();
                    // the update the project files need to be done in the workspace runnable to
                    // avoid all notification of changes before the end of the modifications.
                    workspace.run(op, schedulingRule, IWorkspace.AVOID_UPDATE, monitor);
                } catch (CoreException e) {
                    ExceptionHandler.process(e);
                }
            }
        };
        workUnit.setAvoidUnloadResources(true);
        ProxyRepositoryFactory.getInstance().executeRepositoryWorkUnit(workUnit);
    }

    public static ITalendProcessJavaProject getTalendCodeJavaProject(ERepositoryObjectType type) {
        return getTalendCodeJavaProject(type, ProjectManager.getInstance().getCurrentProject().getTechnicalLabel());
    }

    public static ITalendProcessJavaProject getTalendCodeJavaProject(ERepositoryObjectType type, String projectTechName) {
        String codeProjectId = getCodeProjectId(type, projectTechName);
        ITalendProcessJavaProject talendCodeJavaProject = talendCodeJavaProjects.get(codeProjectId);
        if (talendCodeJavaProject == null || talendCodeJavaProject.getProject() == null
                || !talendCodeJavaProject.getProject().exists()) {
            try {
                IProgressMonitor monitor = new NullProgressMonitor();
                IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
                AggregatorPomsHelper helper = new AggregatorPomsHelper(projectTechName);
                IFolder codeProjectFolder = helper.getProjectPomsFolder().getFolder(type.getFolder());
                cleanUpCodeProject(monitor, codeProjectFolder);
                IProject codeProject = root.getProject((projectTechName + "_" + type.name()).toUpperCase()); //$NON-NLS-1$
                if (!codeProject.exists() || TalendCodeProjectUtil.needRecreate(monitor, codeProject)) {
                    // always enable maven nature for code projects.
                    createMavenJavaProject(monitor, codeProject, null, codeProjectFolder, true);
                }
                IJavaProject javaProject = JavaCore.create(codeProject);
                if (!javaProject.isOpen()) {
                    javaProject.open(monitor);
                }
                // only update code pom for main project.
                if (ProjectManager.getInstance().getCurrentProject().getTechnicalLabel().equals(projectTechName)) {
                    if (ERepositoryObjectType.ROUTINES == type) {
                        PomUtil.checkExistingLog4j2Dependencies4RoutinePom(projectTechName,
                                codeProject.getFile(TalendMavenConstants.POM_FILE_NAME));
                    }
                    helper.updateCodeProjectPom(monitor, type, codeProject.getFile(TalendMavenConstants.POM_FILE_NAME));
                }
                talendCodeJavaProject = new TalendProcessJavaProject(javaProject);
                BuildCacheManager.getInstance().clearCodesCache(type);
                talendCodeJavaProjects.put(codeProjectId, talendCodeJavaProject);
            } catch (Exception e) {
                ExceptionHandler.process(e);
            }
        }

        MavenPomSynchronizer.addChangeLibrariesListener();

        return talendCodeJavaProject;
    }

    public static String getCodeProjectId(ERepositoryObjectType codeType, String projectTechName) {
        return projectTechName + "|" + codeType.name(); //$NON-NLS-1$
    }

    public static String getCodesJarProjectId(Property codesJarProperty) {
        return getCodesJarProjectId(ERepositoryObjectType.getItemType(codesJarProperty.getItem()),
                ProjectManager.getInstance().getProject(codesJarProperty).getTechnicalLabel(), codesJarProperty.getLabel());
    }

    public static String getCodesJarProjectId(ERepositoryObjectType codeType, String projectTechName, String codesJarName) {
        return projectTechName + "|" + codeType.name() + "|" + codesJarName.toLowerCase(); //$NON-NLS-1$ //$NON-NLS-2$
    }

    private static void cleanUpCodeProject(IProgressMonitor monitor, IFolder codeProjectFolder) throws CoreException {
        // empty the src/main/java...
        IFolder srcFolder = codeProjectFolder.getFolder(MavenSystemFolders.JAVA.getPath());
        cleanFolder(monitor, srcFolder);

        // empty src/main/ext-resources
        IFolder extResourcesFolder = codeProjectFolder.getFolder(MavenSystemFolders.EXT_RESOURCES.getPath());
        cleanFolder(monitor, extResourcesFolder);

        // empty src/main/resources
        IFolder resourcesFolder = codeProjectFolder.getFolder(MavenSystemFolders.RESOURCES.getPath());
        cleanFolder(monitor, resourcesFolder);

        // empty the src/test/java
        IFolder testSrcFolder = codeProjectFolder.getFolder(MavenSystemFolders.JAVA_TEST.getPath());
        cleanFolder(monitor, testSrcFolder);

        // empty the src/test/resources
        IFolder testResourcesFolder = codeProjectFolder.getFolder(MavenSystemFolders.RESOURCES_TEST.getPath());
        cleanFolder(monitor, testResourcesFolder);

        // empty target
        IFolder targetFolder = codeProjectFolder.getFolder(MavenSystemFolders.TARGET.getPath());
        cleanFolder(monitor, targetFolder);
    }

    private static void cleanFolder(IProgressMonitor monitor, IFolder folder) throws CoreException {
        if (folder != null && folder.exists()) {
            IResource[] childrenRecs = folder.members();
            for (IResource child : childrenRecs) {
                child.delete(true, monitor);
            }
        }
    }

    public static ITalendProcessJavaProject getTalendCodesJarJavaProject(Property property) {
        String projectTechName = ProjectManager.getInstance().getProject(property).getTechnicalLabel();
        ERepositoryObjectType type = RoutinesUtil.getInnerCodeType(property);
        String codesJarName = null;
        if (type == null) {
            type = ERepositoryObjectType.getItemType(property.getItem());
            codesJarName = property.getLabel();
        } else {
            codesJarName = RoutinesUtil.getCodesJarLabelByInnerCode(property.getItem());
        }
        String codeProjectId = getCodesJarProjectId(type, projectTechName, codesJarName);
        ITalendProcessJavaProject codesJarJavaProject = talendCodesJarJavaProjects.get(codeProjectId);
        if (codesJarJavaProject == null || codesJarJavaProject.getProject() == null
                || !codesJarJavaProject.getProject().exists()) {
            try {
                IProgressMonitor monitor = new NullProgressMonitor();
                IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
                AggregatorPomsHelper helper = new AggregatorPomsHelper(projectTechName);
                IFolder codeProjectFolder = helper.getCodesJarFolder(property);
                cleanUpCodeProject(monitor, codeProjectFolder);
                IProject codeProject = root.getProject((projectTechName + "_" + type.name() + "_" + codesJarName).toUpperCase()); //$NON-NLS-1$ //$NON-NLS-2$
                if (!codeProject.exists() || TalendCodeProjectUtil.needRecreate(monitor, codeProject)) {
                    // always enable maven nature for code projects.
                    createMavenJavaProject(monitor, codeProject, null, codeProjectFolder, true);
                }
                IJavaProject javaProject = JavaCore.create(codeProject);
                if (!javaProject.isOpen()) {
                    javaProject.open(monitor);
                }
                // only update for main project.
                if (ProjectManager.getInstance().getCurrentProject().getTechnicalLabel().equals(projectTechName)) {
                    CodesJarM2CacheManager.updateCodesJarProjectPom(monitor, property,
                            codeProject.getFile(TalendMavenConstants.POM_FILE_NAME));
                }
                codesJarJavaProject = new TalendProcessJavaProject(javaProject);
                talendCodesJarJavaProjects.put(codeProjectId, codesJarJavaProject);
            } catch (Exception e) {
                ExceptionHandler.process(e);
            }
        }

        MavenPomSynchronizer.addChangeLibrariesListener();

        return codesJarJavaProject;
    }

    public static ITalendProcessJavaProject getTalendJobJavaProject(Property property) {
        return getTalendJobJavaProject(property, false);
    }

    public static ITalendProcessJavaProject getTalendJobJavaProject(Property property, boolean enbleMavenNature) {
        if (property == null || property.getItem() == null) {
            return getTempJavaProject();
        }
        if (property.getItem() instanceof JobletProcessItem) {
            return getTempJavaProject();
        }
        boolean isService = false;
        if (GlobalServiceRegister.getDefault().isServiceRegistered(IESBService.class)) {
            IESBService service = GlobalServiceRegister.getDefault().getService(IESBService.class);
            isService = service.isServiceItem(property.getItem().eClass().getClassifierID());
        }
        if (!(property.getItem() instanceof ProcessItem) && !isService) {
            return null;
        }
        ITalendProcessJavaProject talendJobJavaProject = null;
        try {
            if (GlobalServiceRegister.getDefault().isServiceRegistered(ITestContainerProviderService.class)) {
                ITestContainerProviderService testContainerService = GlobalServiceRegister
                        .getDefault().getService(ITestContainerProviderService.class);
                if (testContainerService.isTestContainerItem(property.getItem())) {
                    property = testContainerService.getParentJobItem(property.getItem()).getProperty();
                }
            }
            String jobProjectId = AggregatorPomsHelper.getJobProjectId(property);
            talendJobJavaProject = talendJobJavaProjects.get(jobProjectId);
            IProgressMonitor monitor = new NullProgressMonitor();
            if (talendJobJavaProject == null || talendJobJavaProject.getProject() == null
                    || !talendJobJavaProject.getProject().exists()) {
                String projectTechName = ProjectManager.getInstance().getProject(property).getTechnicalLabel();
                IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
                AggregatorPomsHelper helper = new AggregatorPomsHelper(projectTechName);
                IProject jobProject = root.getProject(helper.getJobProjectName(property));
                IFolder jobFolder = AggregatorPomsHelper.getItemPomFolder(property);
                if (!jobProject.exists() || TalendCodeProjectUtil.needRecreate(monitor, jobProject)) {
                    createMavenJavaProject(monitor, jobProject, property, jobFolder, enbleMavenNature);
                }
                IJavaProject javaProject = JavaCore.create(jobProject);
                talendJobJavaProject = new TalendProcessJavaProject(javaProject, property);
                if (talendJobJavaProject != null) {
                    AggregatorPomsHelper.checkJobPomCreation(talendJobJavaProject);
                    MavenPomSynchronizer pomSynchronizer = new MavenPomSynchronizer(talendJobJavaProject);
                    pomSynchronizer.cleanMavenFiles(monitor);
                }
                talendJobJavaProjects.put(jobProjectId, talendJobJavaProject);
            } else {
                if (enbleMavenNature) {
                    MavenProjectUtils.enableMavenNature(monitor, talendJobJavaProject.getProject());
                } else {
                    // MavenProjectUtils.disableMavenNature(monitor, talendJobJavaProject.getProject());
                }
            }
        } catch (Exception e) {
            ExceptionHandler.process(e);
        }

        MavenPomSynchronizer.addChangeLibrariesListener();

        return talendJobJavaProject;
    }

    public static ITalendProcessJavaProject getTempJavaProject() {
        NullProgressMonitor monitor = new NullProgressMonitor();
        if (tempJavaProject == null || tempJavaProject.getProject() == null || !tempJavaProject.getProject().exists()) {
            try {
                IProject project = TalendCodeProjectUtil.initCodeProject(monitor);
                if (project != null) {
                    IJavaProject javaProject = JavaCore.create(project);
                    if (!javaProject.isOpen()) {
                        javaProject.open(monitor);
                    }
                    tempJavaProject = new TalendProcessJavaProject(javaProject);
                    tempJavaProject.cleanMavenFiles(monitor);
                    tempJavaProject.createSubFolder(monitor, tempJavaProject.getSrcFolder(), JavaUtils.JAVA_INTERNAL_DIRECTORY);
                }
            } catch (Exception e) {
                ExceptionHandler.process(e);
            }
        }
        return tempJavaProject;
    }

    public static ITalendProcessJavaProject getExistingTalendJobProject(Property property) {
        return talendJobJavaProjects.get(AggregatorPomsHelper.getJobProjectId(property));
    }

    public static ITalendProcessJavaProject getExistingTalendCodeProject(ERepositoryObjectType codeType, String projectTechName) {
        return talendCodeJavaProjects.get(getCodeProjectId(codeType, projectTechName));
    }

    public static ITalendProcessJavaProject getExistingTalendCodesJarProject(Property property) {
        return talendCodesJarJavaProjects.get(getCodesJarProjectId(property));
    }

    public static void removeFromCodeJavaProjects(ERepositoryObjectType codeType, String projectTechName) {
        talendCodeJavaProjects.remove(getCodeProjectId(codeType, projectTechName));
    }

    public static void removeFromCodesJarJavaProjects(Property Property) {
        talendCodesJarJavaProjects.remove(getCodesJarProjectId(Property));
    }

    public static void deleteTalendJobProjectsUnderFolder(ERepositoryObjectType processType, IPath folderPath,
            boolean deleteContent) {
        try {
            // delete exist projects
            Iterator<String> iterator = talendJobJavaProjects.keySet().iterator();
            while (iterator.hasNext()) {
                String key = iterator.next();
                ITalendProcessJavaProject project = talendJobJavaProjects.get(key);
                Property property = project.getPropery();
                if (property != null) {
                    IPath jobPath = ItemResourceUtil.getItemRelativePath(property);
                    if (folderPath.isPrefixOf(jobPath)) {
                        project.getProject().delete(deleteContent, true, null);
                        iterator.remove();
                    }
                }
            }
            if (deleteContent) {
                // delete folder
                AggregatorPomsHelper helper = new AggregatorPomsHelper();
                IFolder folder = helper.getProcessFolder(processType).getFolder(folderPath);
                folder.delete(true, false, null);
            }
        } catch (CoreException e) {
            ExceptionHandler.process(e);
        }
    }

    public static void doDeleteAllVersionTalendJobProject(String id, String oldName, boolean deleteContent, boolean avoidRefresh)
            throws Exception {
        List<IRepositoryViewObject> allVersionObjects = ProxyRepositoryFactory.getInstance().getAllVersion(id);
        for (IRepositoryViewObject object : allVersionObjects) {
            String realVersion = object.getVersion();
            Property property = object.getProperty();
            String currentName = property.getLabel();
            try {
                if (oldName != null) {
                    property.setLabel(oldName);
                }
                if (property.getItem() instanceof JobletProcessItem) {
                    BuildCacheManager.getInstance().removeJobletCache(property);
                } else {
                    BuildCacheManager.getInstance().preRemoveJobCache(property);
                }
                IFile pomFile = AggregatorPomsHelper.getItemPomFolder(property, realVersion)
                        .getFile(TalendMavenConstants.POM_FILE_NAME);
                AggregatorPomsHelper.removeFromParentModules(pomFile);
            } finally {
                if (oldName != null) {
                    property.setLabel(currentName);
                }
            }
        }
        Set<String> removedVersions = new HashSet<>();
        Iterator<String> iterator = talendJobJavaProjects.keySet().iterator();
        // delete exist project
        while (iterator.hasNext()) {
            String key = iterator.next();
            if (key.contains(id)) {
                ITalendProcessJavaProject projectToDelete = talendJobJavaProjects.get(key);
                projectToDelete.getProject().delete(deleteContent, true, null);
                String version = key.split("\\|")[1]; //$NON-NLS-1$
                removedVersions.add(version);
                iterator.remove();
            }
        }
        if (deleteContent) {
            // for logically deleted project, delete the folder directly
            AggregatorPomsHelper helper = new AggregatorPomsHelper();
            for (IRepositoryViewObject object : allVersionObjects) {
                String realVersion = object.getVersion();
                Property property = object.getProperty();
                String currentName = property.getLabel();
                try {
                    if (oldName != null) {
                        property.setLabel(oldName);
                    }
                    IPath jobPath = AggregatorPomsHelper.getItemPomFolder(property, realVersion).getLocation();
                    if (!removedVersions.contains(realVersion)) {
                        File projectFolder = jobPath.toFile();
                        if (projectFolder.exists()) {
                            FilesUtils.deleteFolder(projectFolder, true);
                        }
                    }
                } finally {
                    if (oldName != null) {
                        property.setLabel(currentName);
                    }
                }
            }
            if (!avoidRefresh) {
                helper.getProjectPomsFolder().refreshLocal(IResource.DEPTH_INFINITE, null);
            }
        }

    }

    public static void deleteAllVersionTalendJobProject(String id, String oldName, boolean deleteContent) {

        RepositoryWorkUnit workUnit = new RepositoryWorkUnit<Object>("Delete job projects") { //$NON-NLS-1$

            @Override
            protected void run() {
                try {
                    doDeleteAllVersionTalendJobProject(id, oldName, deleteContent, false);
                } catch (Exception e) {
                    ExceptionHandler.process(e);
                }
            }
        };
        workUnit.setAvoidUnloadResources(true);
        ProxyRepositoryFactory.getInstance().executeRepositoryWorkUnit(workUnit);

    }

    public static void batchDeleteAllVersionTalendJobProject(List<String> idList) {
        try {
            for (String id : idList) {
                doDeleteAllVersionTalendJobProject(id, null, true, true);
            }
            AggregatorPomsHelper helper = new AggregatorPomsHelper();
            helper.getProjectPomsFolder().refreshLocal(IResource.DEPTH_INFINITE, null);
        } catch (Exception e) {
            ExceptionHandler.process(e);
        }
    }

    public static void createAllVersionTalendJobProject(String id) {

        RepositoryWorkUnit workUnit = new RepositoryWorkUnit<Object>("Create job projects") { //$NON-NLS-1$

            @Override
            protected void run() {
                try {
                    List<IRepositoryViewObject> allVersionObjects = ProxyRepositoryFactory.getInstance().getAllVersion(id);
                    for (IRepositoryViewObject obj : allVersionObjects) {
                        generatePom(obj.getProperty().getItem());
                    }
                } catch (Exception e) {
                    ExceptionHandler.process(e);
                }
            }
        };
        workUnit.setAvoidUnloadResources(true);
        ProxyRepositoryFactory.getInstance().executeRepositoryWorkUnit(workUnit);
    }

    private static void createRefRootPoms(IProgressMonitor monitor, Project refProject) throws Exception {
        for (ProjectReference ref : refProject.getProjectReferenceList(true)) {
            createRefRootPoms(monitor, new Project(ref.getReferencedProject()));
        }
        new AggregatorPomsHelper(refProject.getTechnicalLabel()).createRootPom(monitor);
    }

    private static void createMavenJavaProject(IProgressMonitor monitor, IProject jobProject, Property property,
            IFolder projectFolder, boolean enbleMavenNature) throws CoreException, Exception {
        if (jobProject.exists()) {
            if (jobProject.isOpen()) {
                jobProject.close(monitor);
            }
            jobProject.delete(false, true, monitor);
        }
        CreateMavenCodeProject createProject = new CreateMavenCodeProject(jobProject, property, enbleMavenNature);
        createProject.setProjectLocation(projectFolder.getLocation());
        createProject.setPomFile(projectFolder.getFile(TalendMavenConstants.POM_FILE_NAME));
        createProject.create(monitor);
        jobProject = createProject.getProject();
        if (!jobProject.isOpen()) {
            jobProject.open(IProject.BACKGROUND_REFRESH, monitor);
        } else {
            if (!jobProject.isSynchronized(IProject.DEPTH_INFINITE)) {
                jobProject.refreshLocal(IProject.DEPTH_INFINITE, monitor);
            }
        }

    }

    private static IFolder createFolderIfNotExist(IFolder folder, IProgressMonitor monitor) throws CoreException {
        if (!folder.exists()) {
            folder.create(true, true, monitor);
        }
        return folder;
    }

    public static void deleteEclipseProjectByNatureId(String natureId) throws CoreException {
        final IWorkspaceRunnable op = new IWorkspaceRunnable() {

            @Override
            public void run(IProgressMonitor monitor) throws CoreException {
                IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
                for (IProject project : projects) {
                    if (project.hasNature("com.oaklandsw.transform.runtime.nature")) { //$NON-NLS-1$
                        // never delete TDM Builtin, Examples and Examples EDI project ref
                        // com.oaklandsw.data.transform.builtin
                        continue;
                    }
                    if (project.getLocation() == null || !project.getLocation().toFile().exists()
                            || !project.getFile(IProjectDescription.DESCRIPTION_FILE_NAME).getLocation().toFile().exists()) {
                        project.delete(false, true, monitor);
                        continue;
                    }
                    if (!project.isOpen()) {
                        project.open(monitor);
                    }
                    if (project.hasNature(natureId)) {
                        IFile eclipseClasspath = project.getFile(CLASSPATH_FILE_NAME);
                        if (eclipseClasspath.exists()) {
                            eclipseClasspath.delete(true, monitor);
                        }
                        IFile projectFile = project.getFile(PROJECT_FILE_NAME);
                        if (projectFile.exists()) {
                            projectFile.delete(true, monitor);
                        }
                        project.delete(false, true, monitor);
                    }
                }
                talendCodeJavaProjects.clear();
                talendJobJavaProjects.clear();
                tempJavaProject = null;
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

    }

    public static void generatePom(Item item) {
        generatePom(item, 0);
    }

    public static void generatePom(Item item, int option) {

        ProcessorUtilities.setGeneratePomOnly(true);
        option |= TalendProcessOptionConstants.GENERATE_POM_ONLY;
        option |= TalendProcessOptionConstants.GENERATE_IS_MAINJOB;
        try {
            IDesignerCoreService service = CorePlugin.getDefault().getDesignerCoreService();
            IProcess process = service.getProcessFromItem(item);
            if (process != null) {
                // avoid non-process item
                IContext context = process.getContextManager().getDefaultContext();
                IProcessor processor = ProcessorUtilities.getProcessor(process, item.getProperty(), context);
                generatePom(item, option, processor);
            } else {
                // SOAP service, when the process is null
                if (GlobalServiceRegister.getDefault().isServiceRegistered(IESBService.class)) {
                    IESBService soapService = GlobalServiceRegister.getDefault().getService(IESBService.class);
                    if (item != null && soapService.isServiceItem(item.eClass().getClassifierID())) {
                        IProcessor processor = ProcessorUtilities.getProcessor(process, item.getProperty());
                        generatePom(item, option, processor);
                    }
                }
            }
        } catch (Exception e) {
            String errorMsg = "Job [" + item.getProperty().getLabel() + "_" + item.getProperty().getVersion() //$NON-NLS-1$ //$NON-NLS-2$
                    + "] encountered problems while generating pom :"; //$NON-NLS-1$
            Exception exception = new Exception(errorMsg, e);
            ExceptionHandler.process(exception);
        } finally {
            ProcessorUtilities.setGeneratePomOnly(false);
        }
    }

    private static void generatePom(Item item, int option, IProcessor processor) throws Exception {
        if (processor instanceof MavenJavaProcessor) {
            LastGenerationInfo.getInstance().clearCaches();
            // Gen poms only
            ((MavenJavaProcessor) processor).generatePom(option);
        }
        boolean checkFilter = !BitwiseOptionUtils.containOption(option, TalendProcessOptionConstants.GENERATE_POM_NO_FILTER);
        AggregatorPomsHelper.addToParentModules(
                AggregatorPomsHelper.getItemPomFolder(item.getProperty()).getFile(TalendMavenConstants.POM_FILE_NAME),
                item.getProperty(), checkFilter);
    }

}
