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
package org.talend.designer.runprocess.maven;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.ArrayUtils;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.talend.commons.exception.ExceptionHandler;
import org.talend.commons.utils.generation.JavaUtils;
import org.talend.commons.utils.resource.FileExtensions;
import org.talend.core.model.process.IProcess;
import org.talend.core.model.process.JobInfo;
import org.talend.core.model.process.ProcessUtils;
import org.talend.core.model.properties.ProcessItem;
import org.talend.core.model.properties.Property;
import org.talend.core.model.utils.JavaResourcesHelper;
import org.talend.core.repository.utils.ItemResourceUtil;
import org.talend.core.runtime.process.ITalendProcessJavaProject;
import org.talend.core.runtime.process.LastGenerationInfo;
import org.talend.core.runtime.process.TalendProcessArgumentConstant;
import org.talend.core.runtime.process.TalendProcessOptionConstants;
import org.talend.core.runtime.repository.build.AbstractBuildProvider;
import org.talend.core.runtime.repository.build.BuildExportManager;
import org.talend.core.runtime.repository.build.IBuildParametes;
import org.talend.core.runtime.repository.build.IBuildPomCreatorParameters;
import org.talend.core.runtime.repository.build.IMavenPomCreator;
import org.talend.core.utils.BitwiseOptionUtils;
import org.talend.designer.maven.model.TalendMavenConstants;
import org.talend.designer.maven.tools.BuildCacheManager;
import org.talend.designer.maven.tools.MavenPomSynchronizer;
import org.talend.designer.maven.tools.creator.CreateMavenJobPom;
import org.talend.designer.maven.utils.MavenProjectUtils;
import org.talend.designer.maven.utils.PomUtil;
import org.talend.designer.runprocess.ProcessorException;
import org.talend.designer.runprocess.ProcessorUtilities;
import org.talend.designer.runprocess.java.JavaProcessor;
import org.talend.designer.runprocess.java.TalendJavaProjectManager;
import org.talend.repository.i18n.Messages;

/**
 * created by ggu on 2 Feb 2015 Detailled comment
 *
 */
public class MavenJavaProcessor extends JavaProcessor {

    protected String windowsClasspath, unixClasspath;

    public MavenJavaProcessor(IProcess process, Property property, boolean filenameFromLabel) {
        super(process, property, filenameFromLabel);
        if (isStandardJob() && getContext() != null && getTalendJavaProject().isUseTempPom()) {
            // for remote project when jobs have no chance to generate/update pom
            generatePom(0);
        }
    }

    @Override
    public void generateCode(boolean statistics, boolean trace, boolean javaProperties, int option) throws ProcessorException {
        super.generateCode(statistics, trace, javaProperties, option);
        if (isStandardJob()) {
            int options = ProcessUtils.getOptionValue(getArguments(), TalendProcessArgumentConstant.ARG_GENERATE_OPTION, 0);
            if (isExportConfig() && !BitwiseOptionUtils.containOption(options, TalendProcessOptionConstants.GENERATE_WITHOUT_COMPILING)) {
                PomUtil.backupPomFile(getTalendJavaProject());
                generatePom(option);
            }
        } else {
            // for Shadow Process/Data Preview
            try {
                PomUtil.updatePomDependenciesFromProcessor(this);
                new MavenPomSynchronizer(this).syncRoutinesPom(null, true);
            } catch (Exception e) {
                throw new ProcessorException(e);
            }
        }
    }

    @Override
    public Set<JobInfo> getBuildChildrenJobs() {
        if (buildChildrenJobs == null || buildChildrenJobs.isEmpty()) {
            buildChildrenJobs = new HashSet<>();

            if (property != null && property.getItem() != null) {
                Set<JobInfo> infos = ProcessorUtilities.getChildrenJobInfo((ProcessItem) property.getItem());
                for (JobInfo jobInfo : infos) {
                    if (jobInfo.isTestContainer()
                            && !ProcessUtils.isOptionChecked(getArguments(), TalendProcessArgumentConstant.ARG_GENERATE_OPTION,
                                    TalendProcessOptionConstants.GENERATE_TESTS)) {
                        continue;
                    }
                    buildChildrenJobs.add(jobInfo);
                }
            }
        }
        return this.buildChildrenJobs;
    }

    public void initJobClasspath() {
        String oldInterpreter = ProcessorUtilities.getInterpreter();
        String oldCodeLocation = ProcessorUtilities.getCodeLocation();
        String oldLibraryPath = ProcessorUtilities.getLibraryPath();
        boolean oldExportConfig = ProcessorUtilities.isExportConfig();
        Date oldExportTimestamp = ProcessorUtilities.getExportTimestamp();
        try {
            // FIXME, must make sure the exportConfig is true, and the classpath is same as export.
            String routinesJarPath = getBaseLibPath() + JavaUtils.PATH_SEPARATOR + JavaUtils.ROUTINES_JAR
                    + ProcessorUtilities.TEMP_JAVA_CLASSPATH_SEPARATOR;
            ProcessorUtilities.setExportConfig(JavaUtils.JAVA_APP_NAME, routinesJarPath, getBaseLibPath());

            String contextName = JavaResourcesHelper.getJobContextName(this.context);
            String oldTarget = this.getTargetPlatform();
            boolean oldBuild = this.isOldBuildJob(); 
            setPlatformValues(Platform.OS_WIN32, contextName);
            setPlatformValues(Platform.OS_LINUX, contextName);
            this.setTargetPlatform(oldTarget);
            this.setOldBuildJob(oldBuild);
        } finally {
            ProcessorUtilities.setExportConfig(oldInterpreter, oldCodeLocation, oldLibraryPath, oldExportConfig,
                    oldExportTimestamp);
        }
    }

    /**
     * 
     * copied from JobScriptsManager.getCommandByTalendJob
     */
    protected void setPlatformValues(String tp, String contextName) {
        try {
            // maybe should just reuse current processor's getCommandLine method.
            // use always use new way.
            String[] cmds = ProcessorUtilities.getCommandLine(false, tp, true, this, null, contextName, false, -1, -1);
            setValuesFromCommandline(tp, cmds);
        } catch (ProcessorException e) {
            ExceptionHandler.process(e);
        }
    }

    protected void setValuesFromCommandline(String tp, String[] cmds) {
        if (cmds == null || cmds.length == 0) {
            return;
        }
        String cpStr = null;
        int cpIndex = ArrayUtils.indexOf(cmds, JavaUtils.JAVA_CP);
        if (cpIndex > -1) { // found
            // return the cp values in the next index.
            cpStr = cmds[cpIndex + 1];
        }

        if (Platform.OS_WIN32.equals(tp)) {
            this.windowsClasspath = cpStr;
        } else {
            this.unixClasspath = cpStr;
        }
    }

    @Override
    protected String getBasePathClasspath() throws ProcessorException {
        final boolean exportingJob = ProcessorUtilities.isExportConfig();
        String basePathClasspath = super.getBasePathClasspath();

        if (!exportingJob && isTestJob) { // for test job, need add the test-classes in classpath.
            final String classPathSeparator = extractClassPathSeparator();

            ITalendProcessJavaProject tProcessJvaProject = this.getTalendJavaProject();
            IFolder testClassesFolder = tProcessJvaProject.getTestOutputFolder();
            String testOutputPath = testClassesFolder.getLocation().toPortableString();
            basePathClasspath = testOutputPath + classPathSeparator + basePathClasspath;
        }
        return basePathClasspath;
    }

    @Override
    protected String getExportJarsStr() {
        if (isOldBuildJob()) {
            return super.getExportJarsStr();
        }
        // use the maven way for jar
        final String libPrefixPath = getRootWorkingDir(true);
        final String classPathSeparator = extractClassPathSeparator();

        // Test-0.1
        String jarName = JavaResourcesHelper.getJobJarName(process.getName(), process.getVersion());
        String exportJar = libPrefixPath + jarName + FileExtensions.JAR_FILE_SUFFIX;

        Set<JobInfo> infos = getBuildChildrenJobs();
        for (JobInfo jobInfo : infos) {
            if (jobInfo.isTestContainer()) {
                continue;
            }
            String childJarName = JavaResourcesHelper.getJobJarName(jobInfo.getJobName(), jobInfo.getJobVersion());
            exportJar += classPathSeparator + libPrefixPath + childJarName + FileExtensions.JAR_FILE_SUFFIX;
        }
        return exportJar;
    }

    /**
     * .Java/pom_TestJob_0.1.xml
     */
    protected IFile getPomFile() {
        if (isStandardJob()) {
            String pomFileName = TalendMavenConstants.POM_FILE_NAME;
            return this.getTalendJavaProject().getProject().getFile(pomFileName);
        } else { // not standard job, won't have pom file.
            return null;
        }
    }

    /**
     * .Java/src/main/assemblies/assembly_TestJob_0.1.xml
     */
    protected IFile getAssemblyFile() {
        if (isStandardJob()) {
            String assemblyFileName = TalendMavenConstants.ASSEMBLY_FILE_NAME;
            return this.getTalendJavaProject().getAssembliesFolder().getFile(assemblyFileName);
        } else { // not standard job, won't have assembly file.
            return null;
        }
    }

    public void generatePom(int option) {
        initJobClasspath();
        try {
            IMavenPomCreator createTemplatePom = createMavenPomCreator();
            if (createTemplatePom != null) {
                createTemplatePom.setSyncCodesPoms(option == 0);
                boolean previousValue = ProcessUtils.jarNeedsToContainContext();
                ProcessUtils.setJarWithContext(ProcessUtils.needsToHaveContextInsideJar((ProcessItem) property.getItem()));
                createTemplatePom.create(null);
                ProcessUtils.setJarWithContext(previousValue);
                getTalendJavaProject().setUseTempPom(false);
            }
        } catch (Exception e) {
            ExceptionHandler.process(e);
        }
    }

    protected IMavenPomCreator createMavenPomCreator() {
        final Property itemProperty = this.getProperty();
        String buildTypeName = null;
        // FIXME, better use the arguments directly for run/export/build/..., and remove this flag later.
        if (ProcessorUtilities.isExportConfig()) {
            // final Object exportType = itemProperty.getAdditionalProperties().get(MavenConstants.NAME_EXPORT_TYPE);
            final Object exportType = getArguments().get(TalendProcessArgumentConstant.ARG_BUILD_TYPE);
            buildTypeName = exportType != null ? exportType.toString() : null;
        } // else { //if run job, will be null (use Standalone by default)

        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put(IBuildParametes.ITEM, itemProperty.getItem());
        parameters.put(IBuildPomCreatorParameters.PROCESSOR, this);
        parameters.put(IBuildPomCreatorParameters.FILE_POM, getPomFile());
        parameters.put(IBuildPomCreatorParameters.FILE_ASSEMBLY, getAssemblyFile());
        parameters.put(IBuildPomCreatorParameters.CP_LINUX, this.unixClasspath);
        parameters.put(IBuildPomCreatorParameters.CP_WIN, this.windowsClasspath);
        parameters.put(IBuildPomCreatorParameters.ARGUMENTS_MAP, getArguments());
        parameters.put(IBuildPomCreatorParameters.OVERWRITE_POM, Boolean.TRUE);

        AbstractBuildProvider foundBuildProvider = BuildExportManager.getInstance().getBuildProvider(buildTypeName, parameters);
        if (foundBuildProvider != null) {
            final IMavenPomCreator creator = foundBuildProvider.createPomCreator(parameters);
            if (creator != null) {
                return creator;
            }
        }

        // normally, won't be here, should return creator in font.
        CreateMavenJobPom createTemplatePom = new CreateMavenJobPom(this, getPomFile());

        createTemplatePom.setUnixClasspath(this.unixClasspath);
        createTemplatePom.setWindowsClasspath(this.windowsClasspath);

        createTemplatePom.setAssemblyFile(getAssemblyFile());

        IPath itemLocationPath = ItemResourceUtil.getItemLocationPath(this.getProperty());
        IFolder objectTypeFolder = ItemResourceUtil.getObjectTypeFolder(this.getProperty());
        if (itemLocationPath != null && objectTypeFolder != null) {
            IPath itemRelativePath = itemLocationPath.removeLastSegments(1).makeRelativeTo(objectTypeFolder.getLocation());
            createTemplatePom.setObjectTypeFolder(objectTypeFolder);
            createTemplatePom.setItemRelativePath(itemRelativePath);
        }

        return createTemplatePom;

    }

    @Override
    public void build(IProgressMonitor monitor) throws Exception {
        BuildCacheManager buildCacheManager = BuildCacheManager.getInstance();
        final ITalendProcessJavaProject talendJavaProject = getTalendJavaProject();
        // compile with JDT first in order to make the maven packaging work with a JRE.
        String goal = getGoals();
        boolean isGoalPackage = TalendMavenConstants.GOAL_PACKAGE.equals(goal);
        boolean isGoalInstall = TalendMavenConstants.GOAL_INSTALL.equals(goal);
        boolean isMainJob = LastGenerationInfo.getInstance().isCurrentMainJob();
        if (!isMainJob && isGoalInstall) {
            if (!buildCacheManager.isJobBuild(getProperty())) {
                deleteExistedJobJarFile(talendJavaProject);
                buildCacheManager.putCache(getProperty());
            } else {
                // for already installed sub jobs, can restore pom here directly
                PomUtil.restorePomFile(getTalendJavaProject());
            }
            // TODO copy resources to main job project.
            return;
        }
        if (isMainJob) {
            final Map<String, Object> argumentsMap = new HashMap<String, Object>();
            argumentsMap.put(TalendProcessArgumentConstant.ARG_GOAL, TalendMavenConstants.GOAL_INSTALL);
            argumentsMap.put(TalendProcessArgumentConstant.ARG_PROGRAM_ARGUMENTS, "-T 1C -f build-aggregator.pom"); // $NON-NLS-N$
            // install all subjobs
            buildCacheManager.build(monitor, argumentsMap);

            if (!MavenProjectUtils.hasMavenNature(project)) {
                // enable maven nature in case project not create yet.
                MavenProjectUtils.enableMavenNature(monitor, project);
            } else {
                // FIXME should update when new dependencies installed.
                MavenProjectUtils.updateMavenProject(monitor, talendJavaProject.getProject());
            }
            // close all sub job's maven project to let main job project use dependencies in m2 instead of maven project.
            // FIXME should reopen those projects after execution.
            JobInfo mainJobInfo = LastGenerationInfo.getInstance().getLastMainJob();
            Set<JobInfo> allJobs = LastGenerationInfo.getInstance().getLastGeneratedjobs();
            for (JobInfo jobInfo : allJobs) {
                if (mainJobInfo != jobInfo) {
                    ITalendProcessJavaProject subJobProject = TalendJavaProjectManager
                            .getExistingTalendJobProject(jobInfo.getJobId(), jobInfo.getJobVersion());
                    if (subJobProject != null) {
                        IProject subProject = subJobProject.getProject();
                        if (MavenProjectUtils.hasMavenNature(subProject) && subProject.isOpen()) {
                            getTalendJavaProject().getJavaProject().close();
                            subProject.close(monitor);
                        }
                    }
                }
            }
        }
        IFile jobJarFile = null;
        if (!TalendMavenConstants.GOAL_COMPILE.equals(goal)) {
            if (isGoalPackage) {
                jobJarFile = deleteExistedJobJarFile(talendJavaProject);
            }
            talendJavaProject.buildModules(monitor, null, null);
        }

        final Map<String, Object> argumentsMap = new HashMap<>();
        argumentsMap.put(TalendProcessArgumentConstant.ARG_GOAL, goal);
        if (isGoalPackage) {
            argumentsMap.put(TalendProcessArgumentConstant.ARG_PROGRAM_ARGUMENTS, "-Dmaven.main.skip=true -Dmaven.test.skip=true  -P !" //$NON-NLS-1$
                    + TalendMavenConstants.PROFILE_PACKAGING_AND_ASSEMBLY);
        }
        talendJavaProject.buildModules(monitor, null, argumentsMap);
        if (isGoalPackage) {
            if (jobJarFile != null) {
                jobJarFile.refreshLocal(IResource.DEPTH_ONE, null);
            }
            if (jobJarFile == null || !jobJarFile.exists()) {
                String mvnLogFilePath = talendJavaProject.getProject().getFile("lastGenerated.log").getLocation().toPortableString(); //$NON-NLS-1$
                throw new Exception(Messages.getString("BuildJobManager.mavenErrorMessage", mvnLogFilePath)); //$NON-NLS-1$
            }
        }
    }

    private IFile deleteExistedJobJarFile(final ITalendProcessJavaProject talendJavaProject) throws CoreException {
        IFile jobJarFile;
        String jobJarName = JavaResourcesHelper.getJobJarName(property.getLabel(), property.getVersion())
                + FileExtensions.JAR_FILE_SUFFIX;
        jobJarFile = talendJavaProject.getTargetFolder().getFile(jobJarName);
        if (jobJarFile != null && jobJarFile.exists()) {
            jobJarFile.delete(true, null);
            jobJarFile.refreshLocal(IResource.DEPTH_ONE, null);
        }
        return jobJarFile;
    }

    protected String getGoals() {
        if (isTestJob) {
            return TalendMavenConstants.GOAL_TEST_COMPILE;
        }
        if (!LastGenerationInfo.getInstance().isCurrentMainJob()) {
            return TalendMavenConstants.GOAL_INSTALL;
        }
        if (!isExportConfig()) {
            if (requirePackaging()) {
                // We return the INSTALL goal if the main job and/or one of its recursive job is a Big Data job.
                return TalendMavenConstants.GOAL_PACKAGE;
            }
        }
        // Else, a simple compilation is needed.
        return TalendMavenConstants.GOAL_COMPILE;
    }
}
