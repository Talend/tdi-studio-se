// ============================================================================
//
// Copyright (C) 2006-2016 Talend Inc. - www.talend.com
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;
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
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.talend.commons.exception.ExceptionHandler;
import org.talend.commons.utils.generation.JavaUtils;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.PluginChecker;
import org.talend.core.model.general.Project;
import org.talend.core.model.process.ProcessUtils;
import org.talend.core.model.properties.JobletProcessItem;
import org.talend.core.model.properties.ProcessItem;
import org.talend.core.model.properties.Property;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.core.repository.utils.ItemResourceUtil;
import org.talend.core.runtime.process.ITalendProcessJavaProject;
import org.talend.core.ui.ITestContainerProviderService;
import org.talend.designer.maven.model.TalendMavenConstants;
import org.talend.designer.maven.tools.AggregatorPomsHelper;
import org.talend.designer.maven.tools.MavenPomSynchronizer;
import org.talend.designer.maven.tools.creator.CreateMavenCodeProject;
import org.talend.designer.maven.utils.PomIdsHelper;
import org.talend.designer.maven.utils.TalendCodeProjectUtil;
import org.talend.repository.ProjectManager;
import org.talend.repository.RepositoryWorkUnit;

/**
 * DOC zwxue class global comment. Detailled comment
 */
public class TalendJavaProjectManager {

    private static Map<ERepositoryObjectType, ITalendProcessJavaProject> talendCodeJavaProjects = new HashMap<>();

    private static Map<String, ITalendProcessJavaProject> talendJobJavaProjects = new HashMap<>();

    private static ITalendProcessJavaProject tempJavaProject;

    public static void initJavaProjects(IProgressMonitor monitor, Project project) {

        RepositoryWorkUnit<Object> workUnit = new RepositoryWorkUnit<Object>("create aggregator poms") { //$NON-NLS-1$

            @Override
            protected void run() {
                try {
                    // create aggregator poms
                    AggregatorPomsHelper helper = new AggregatorPomsHelper(project);
                    // create poms folder.
                    IFolder poms = createFolderIfNotExist(helper.getProjectPomsFolder(), monitor);

                    // codes
                    IFolder code = createFolderIfNotExist(poms.getFolder(DIR_CODES), monitor);
                    // routines
                    createFolderIfNotExist(code.getFolder(DIR_ROUTINES), monitor);
                    // pigudfs
                    if (ProcessUtils.isRequiredPigUDFs(null)) {
                        createFolderIfNotExist(code.getFolder(DIR_PIGUDFS), monitor);
                    }
                    // beans
                    if (ProcessUtils.isRequiredBeans(null)) {
                        createFolderIfNotExist(code.getFolder(DIR_BEANS), monitor);
                    }

                    // jobs
                    IFolder jobs = createFolderIfNotExist(poms.getFolder(DIR_JOBS), monitor);
                    // process
                    IFolder process = createFolderIfNotExist(jobs.getFolder(DIR_PROCESS), monitor);
                    // process_mr
                    IFolder process_mr = null;
                    if (PluginChecker.isMapReducePluginLoader()) {
                        process_mr = createFolderIfNotExist(jobs.getFolder(DIR_PROCESS_MR), monitor);
                    }
                    // process_storm
                    IFolder process_storm = null;
                    if (PluginChecker.isStormPluginLoader()) {
                        process_storm = createFolderIfNotExist(jobs.getFolder(DIR_PROCESS_STORM), monitor);
                    }
                    // routes
                    IFolder routes = null;
                    if (PluginChecker.isRouteLoaded()) {
                        routes = createFolderIfNotExist(jobs.getFolder(DIR_PROCESS_ROUTES), monitor);
                    }
                    // services
                    IFolder services = null;
                    if (PluginChecker.isServiceLoaded()) {
                        services = createFolderIfNotExist(jobs.getFolder(DIR_PROCESS_SERVICES), monitor);
                    }

                    String jobGroupId = PomIdsHelper.getJobGroupId(project.getTechnicalLabel());
                    helper.createAggregatorFolderPom(process, DIR_PROCESS, jobGroupId, monitor);
                    helper.createAggregatorFolderPom(process_mr, DIR_PROCESS_MR, jobGroupId, monitor);
                    helper.createAggregatorFolderPom(process_storm, DIR_PROCESS_STORM, jobGroupId, monitor);
                    helper.createAggregatorFolderPom(routes, DIR_PROCESS_ROUTES, jobGroupId, monitor);
                    helper.createAggregatorFolderPom(services, DIR_PROCESS_SERVICES, jobGroupId, monitor);
                    helper.createAggregatorFolderPom(jobs, DIR_JOBS, jobGroupId, monitor);
                    helper.createAggregatorFolderPom(code, DIR_CODES, "org.talend.codes." + project.getTechnicalLabel(), monitor); //$NON-NLS-1$

                    helper.createRootPom(poms, monitor);
                } catch (Exception e) {
                    ExceptionHandler.process(e);
                }
            }
        };
        workUnit.setAvoidUnloadResources(true);
        ProxyRepositoryFactory.getInstance().executeRepositoryWorkUnit(workUnit);
    }

    public static void installRootPom(boolean current) {
        AggregatorPomsHelper helper = new AggregatorPomsHelper(ProjectManager.getInstance().getCurrentProject());
        try {
            helper.installRootPom(current);
        } catch (Exception e) {
            ExceptionHandler.process(e);
        }
    }

    public static ITalendProcessJavaProject getTalendCodeJavaProject(ERepositoryObjectType type) {
        Project project = ProjectManager.getInstance().getCurrentProject();
        AggregatorPomsHelper helper = new AggregatorPomsHelper(project);
        ITalendProcessJavaProject talendCodeJavaProject = talendCodeJavaProjects.get(type);
        if (talendCodeJavaProject == null || talendCodeJavaProject.getProject() == null
                || !talendCodeJavaProject.getProject().exists()) {
            try {
                IProgressMonitor monitor = new NullProgressMonitor();
                IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
                IFolder codeProjectFolder = helper.getProjectPomsFolder().getFolder(type.getFolder());
                IProject codeProject = root.getProject(project.getTechnicalLabel() + "_" + type.name());
                if (!codeProject.exists() || TalendCodeProjectUtil.needRecreate(monitor, codeProject)) {
                    createMavenJavaProject(monitor, codeProject, codeProjectFolder);
                }
                IJavaProject javaProject = JavaCore.create(codeProject);
                if (!javaProject.isOpen()) {
                    javaProject.open(monitor);
                }
                AggregatorPomsHelper.updateCodeProjectPom(monitor, type, codeProject.getFile(TalendMavenConstants.POM_FILE_NAME));
                talendCodeJavaProject = new TalendProcessJavaProject(javaProject);
                talendCodeJavaProject.cleanMavenFiles(monitor);
                talendCodeJavaProjects.put(type, talendCodeJavaProject);
            } catch (Exception e) {
                ExceptionHandler.process(e);
            }
        }
        return talendCodeJavaProject;
    }

    public static ITalendProcessJavaProject getTalendJobJavaProject(Property property) {
        if (property == null) {
            return getTempJavaProject();
        }
        if (property.getItem() instanceof JobletProcessItem) {
            return getTempJavaProject();
        }
        if (!(property.getItem() instanceof ProcessItem)) {
            return null;
        }
        ITalendProcessJavaProject talendJobJavaProject = null;
        try {
            if (GlobalServiceRegister.getDefault().isServiceRegistered(ITestContainerProviderService.class)) {
                ITestContainerProviderService testContainerService = (ITestContainerProviderService) GlobalServiceRegister
                        .getDefault().getService(ITestContainerProviderService.class);
                if (testContainerService.isTestContainerItem(property.getItem())) {
                    property = testContainerService.getParentJobItem(property.getItem()).getProperty();
                }
            }
            String projectTechName = ProjectManager.getInstance().getProject(property).getTechnicalLabel();
            Project project = ProjectManager.getInstance().getProjectFromProjectTechLabel(projectTechName);
            AggregatorPomsHelper helper = new AggregatorPomsHelper(project);
            talendJobJavaProject = talendJobJavaProjects.get(property.getId());
            if (talendJobJavaProject == null || talendJobJavaProject.getProject() == null
                    || !talendJobJavaProject.getProject().exists()) {
                IProgressMonitor monitor = new NullProgressMonitor();
                IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
                IProject jobProject = root.getProject(project.getTechnicalLabel() + "_" + property.getLabel()); //$NON-NLS-1$
                IPath itemRelativePath = ItemResourceUtil.getItemRelativePath(property);
                String jobFolderName = "item_" + property.getLabel(); //$NON-NLS-1$
                ERepositoryObjectType type = ERepositoryObjectType.getItemType(property.getItem());
                IFolder jobFolder = helper.getProcessFolder(type).getFolder(itemRelativePath).getFolder(jobFolderName);
                if (!jobProject.exists() || TalendCodeProjectUtil.needRecreate(monitor, jobProject)) {
                    createMavenJavaProject(monitor, jobProject, jobFolder);
                    AggregatorPomsHelper.updatePomIfCreate(monitor, jobProject.getFile(TalendMavenConstants.POM_FILE_NAME),
                            property);
                }
                IJavaProject javaProject = JavaCore.create(jobProject);
                if (!javaProject.isOpen()) {
                    javaProject.open(monitor);
                }
                talendJobJavaProject = new TalendProcessJavaProject(javaProject, property);
                if (talendJobJavaProject != null) {
                    MavenPomSynchronizer pomSynchronizer = new MavenPomSynchronizer(talendJobJavaProject);
                    pomSynchronizer.syncTemplates(false);
                    pomSynchronizer.cleanMavenFiles(monitor);
                }
                talendJobJavaProjects.put(property.getId(), talendJobJavaProject);
            }
        } catch (Exception e) {
            ExceptionHandler.process(e);
        }

        return talendJobJavaProject;
    }

    public static ITalendProcessJavaProject getTempJavaProject() {
        NullProgressMonitor monitor = new NullProgressMonitor();
        if (tempJavaProject == null) {
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

    public static ITalendProcessJavaProject getExistingTalendProject(IProject project) {
        List<ITalendProcessJavaProject> talendProjects = new ArrayList<>();
        talendProjects.addAll(talendCodeJavaProjects.values());
        talendProjects.addAll(talendJobJavaProjects.values());
        talendProjects.add(tempJavaProject);
        for (ITalendProcessJavaProject talendProject : talendProjects) {
            if (project == talendProject.getProject()) {
                return talendProject;
            }
        }
        return null;
    }
    
    public static ITalendProcessJavaProject getExistingTalendJobProjectById(String id) {
        return talendJobJavaProjects.get(id);
    }
    
    public static void deleteTalendJobProject(Property property) {
        ITalendProcessJavaProject projectToDelete = talendJobJavaProjects.get(property.getId());
        if (projectToDelete != null) {
            RepositoryWorkUnit workUnit = new RepositoryWorkUnit<Object>("Delete job project") { //$NON-NLS-1$

                @Override
                protected void run() {
                    try {
                        projectToDelete.getProject().delete(true, true, null);
                    } catch (CoreException e) {
                        ExceptionHandler.process(e);
                    }
                }
            };
            workUnit.setAvoidUnloadResources(true);
            ProxyRepositoryFactory.getInstance().executeRepositoryWorkUnit(workUnit);
            
            talendJobJavaProjects.remove(property.getId());
        }
    }

    private static void createMavenJavaProject(IProgressMonitor monitor, IProject jobProject, IFolder projectFolder)
            throws CoreException, Exception {
        if (jobProject.exists()) {
            if (jobProject.isOpen()) {
                jobProject.close(monitor);
            }
            jobProject.delete(true, true, monitor);
        }
        CreateMavenCodeProject createProject = new CreateMavenCodeProject(jobProject);
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

    public static void createUserDefineFolder() {
        // TODO call it when create folders in repo, add aggregator pom, add it to parent modules.
    }

    public static void removeUserDefineFolder() {
        // TODO
    }

    public static void moveUserDefineFolder() {
        // TODO
    }

    public static void renameUserDefineFolder() {
        // TODO
    }

    public static void deleteEclipseProjectByNatureId(String natureId) throws CoreException {
        final IWorkspaceRunnable op = new IWorkspaceRunnable() {

            @Override
            public void run(IProgressMonitor monitor) throws CoreException {
                IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
                for (IProject project : projects) {
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

}
