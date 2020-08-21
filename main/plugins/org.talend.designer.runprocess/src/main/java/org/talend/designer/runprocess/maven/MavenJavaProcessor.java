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
package org.talend.designer.runprocess.maven;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.talend.commons.exception.ExceptionHandler;
import org.talend.commons.exception.PersistenceException;
import org.talend.commons.ui.runtime.CommonUIPlugin;
import org.talend.commons.utils.generation.JavaUtils;
import org.talend.commons.utils.resource.FileExtensions;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.IESBService;
import org.talend.core.model.process.IProcess;
import org.talend.core.model.process.JobInfo;
import org.talend.core.model.process.ProcessUtils;
import org.talend.core.model.properties.Property;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.model.repository.IRepositoryViewObject;
import org.talend.core.model.utils.JavaResourcesHelper;
import org.talend.core.repository.model.ProxyRepositoryFactory;
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
import org.talend.designer.core.model.process.IGeneratingProcess;
import org.talend.designer.core.ui.editor.process.Process;
import org.talend.designer.maven.model.TalendMavenConstants;
import org.talend.designer.maven.tools.AggregatorPomsHelper;
import org.talend.designer.maven.tools.BuildCacheManager;
import org.talend.designer.maven.tools.creator.CreateMavenJobPom;
import org.talend.designer.maven.utils.MavenProjectUtils;
import org.talend.designer.maven.utils.PomUtil;
import org.talend.designer.runprocess.ProcessorException;
import org.talend.designer.runprocess.ProcessorUtilities;
import org.talend.designer.runprocess.java.JavaProcessor;
import org.talend.repository.i18n.Messages;
import org.talend.repository.model.IProxyRepositoryFactory;

/**
 * created by ggu on 2 Feb 2015 Detailled comment
 *
 */
public class MavenJavaProcessor extends JavaProcessor {

    private static final Logger LOGGER = Logger.getLogger(MavenJavaProcessor.class);

    protected String windowsClasspath, unixClasspath;

    private boolean isMainJob = false;

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
        if (this.getProcess() instanceof Process) {
            IGeneratingProcess generatingProcess = (((Process)this.getProcess()).getGeneratingProcess());
            generatingProcess.generateAdditionalCode();
        }
        generateCodeAfter(statistics, trace, javaProperties, option);
    }

    protected void generateCodeAfter(boolean statistics, boolean trace, boolean javaProperties, int option)
            throws ProcessorException {
        if (isStandardJob()) {
            int options = ProcessUtils.getOptionValue(getArguments(), TalendProcessArgumentConstant.ARG_GENERATE_OPTION, 0);
            if (!BitwiseOptionUtils.containOption(options, TalendProcessOptionConstants.GENERATE_WITHOUT_COMPILING)) {
                PomUtil.backupPomFile(getTalendJavaProject());
            }

            // we need to generate the pom everytime since we have a classpath adjuster
            // means classpath can be changed during the code generation itself by anything using the IClasspathAdjuster
            // (currently TDM)
            generatePom(option);
        } else {
            // for Shadow Process/Data Preview
            try {
                PomUtil.updatePomDependenciesFromProcessor(this);
                new AggregatorPomsHelper().createRoutinesPom(getPomFile(), null);
            } catch (Exception e) {
                throw new ProcessorException(e);
            }
        }
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
        String jobName = process.getName();
        String jobVersion = process.getVersion();
        if (ProcessUtils.isTestContainer(process)) {
            IProcess basePrcess = ProcessUtils.getTestContainerBaseProcess(process);
            jobName = basePrcess.getName();
            jobVersion = basePrcess.getVersion();
        }
        String jarName = JavaResourcesHelper.getJobJarName(jobName, jobVersion);
        String exportJar = libPrefixPath + jarName + FileExtensions.JAR_FILE_SUFFIX;

        if (!isMainJob || isMainJob && !ProcessorUtilities.hasLoopDependency()) {
            Set<JobInfo> infos = getBuildChildrenJobs();
            for (JobInfo jobInfo : infos) {
                if (jobInfo.isTestContainer()) {
                    continue;
                }
                String childJarName = JavaResourcesHelper.getJobJarName(jobInfo.getJobName(), jobInfo.getJobVersion());
                if (!childJarName.equals(jarName)) {
                    exportJar += classPathSeparator + libPrefixPath + childJarName + FileExtensions.JAR_FILE_SUFFIX;
                }
            }
        }
        // for loop dependency, add main classPath
        JobInfo mainJobInfo = ProcessorUtilities.getMainJobInfo();
        if (!isMainJob && ProcessorUtilities.hasLoopDependency() && mainJobInfo != null) {
            String mainJobName = JavaResourcesHelper.getJobJarName(mainJobInfo.getJobName(), mainJobInfo.getJobVersion());
            exportJar += classPathSeparator + libPrefixPath + mainJobName + FileExtensions.JAR_FILE_SUFFIX;
        }
        return exportJar;
    }

    /**
     * .Java/pom_TestJob_0.1.xml
     */
    protected IFile getPomFile() {
        if (isStandardJob()) {
            String pomFileName = TalendMavenConstants.POM_FILE_NAME;
            if (this.getTalendJavaProject() == null) {
                try {
                    return AggregatorPomsHelper.getItemPomFolder(property).getFile(pomFileName);
                } catch (Exception e) {
                    ExceptionHandler.process(e);
                    return null;
                }
            } else {
                return this.getTalendJavaProject().getProject().getFile(pomFileName);
            }
        } else { // not standard job, won't have pom file.
            return null;
        }
    }

    /**
     * .Java/src/main/assemblies/assembly_TestJob_0.1.xml
     */
    protected IFile getAssemblyFile() {
        if (isStandardJob() && !ProcessorUtilities.isGeneratePomOnly()) {
            String assemblyFileName = TalendMavenConstants.ASSEMBLY_FILE_NAME;
            return this.getTalendJavaProject().getAssembliesFolder().getFile(assemblyFileName);
        } else { // not standard job, won't have assembly file.
            return null;
        }
    }

    public void generatePom(int option) {
        if (buildChildrenJobs != null) {
            buildChildrenJobs.clear();
        }
        isMainJob = BitwiseOptionUtils.containOption(option, TalendProcessOptionConstants.GENERATE_IS_MAINJOB);
        if (ProcessorUtilities.isGeneratePomOnly()) {
            ProcessorUtilities.resetExportConfig();
        } else {
            initJobClasspath();
        }
        try {
            IMavenPomCreator createTemplatePom = createMavenPomCreator();
            if (createTemplatePom != null) {
                createTemplatePom.setSyncCodesPoms(isMainJob);
                if (isMainJob) {
                    createTemplatePom.setHasLoopDependency(ProcessorUtilities.hasLoopDependency());
                }
                createTemplatePom.create(null);
                if (getTalendJavaProject() != null) {
                    getTalendJavaProject().setUseTempPom(false);
                }
            }
        } catch (Exception e) {
            ExceptionHandler.process(e);
        }
    }

    protected IMavenPomCreator createMavenPomCreator() {
        final Property itemProperty = this.getProperty();
        String buildTypeName = null;
        // FIXME, better use the arguments directly for run/export/build/..., and remove this flag later.
        // if (ProcessorUtilities.isExportConfig()) {
        // // final Object exportType = itemProperty.getAdditionalProperties().get(MavenConstants.NAME_EXPORT_TYPE);
        // final Object exportType = getArguments().get(TalendProcessArgumentConstant.ARG_BUILD_TYPE);
        // buildTypeName = exportType != null ? exportType.toString() : null;
        // } // else { //if run job, will be null (use Standalone by default)

        Object exportType = getArguments() == null ? null : getArguments().get(TalendProcessArgumentConstant.ARG_BUILD_TYPE);

        if (exportType == null && !ERepositoryObjectType.getType(itemProperty).equals(ERepositoryObjectType.PROCESS) ) {
            exportType = itemProperty.getAdditionalProperties().get(TalendProcessArgumentConstant.ARG_BUILD_TYPE);
        }

        buildTypeName = exportType != null ? exportType.toString() : null;

        if (StringUtils.isBlank(buildTypeName) && GlobalServiceRegister.getDefault().isServiceRegistered(IESBService.class)) {
            List<IRepositoryViewObject> serviceRepoList = null;

            IESBService service = (IESBService) GlobalServiceRegister.getDefault().getService(IESBService.class);

            try {
                IProxyRepositoryFactory factory = ProxyRepositoryFactory.getInstance();
                serviceRepoList = factory.getAll(ERepositoryObjectType.valueOf(ERepositoryObjectType.class, "SERVICES"));

                for (IRepositoryViewObject serviceItem : serviceRepoList) {
                    if (service != null) {
                        List<String> jobIds = service.getSerivceRelatedJobIds(serviceItem.getProperty().getItem());
                        if (jobIds.contains(itemProperty.getId())) {
                            buildTypeName = "OSGI";
                            break;
                        }
                    }
                }

            } catch (PersistenceException e) {
                ExceptionHandler.process(e);
            }
        }

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

    protected boolean packagingAndAssembly() {
        return false;
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
                String buildType = getBuildType(getProperty());
                if (("ROUTE".equalsIgnoreCase(buildType) || "OSGI".equalsIgnoreCase(buildType)) && project != null &&
                		ERepositoryObjectType.PROCESS.equals(ERepositoryObjectType.getType(getProperty()))) {
                    // TESB-23870
                    // child routes job project must be compiled explicitly for
                    // correct child job manifest generation during OSGi packaging
                    if (!MavenProjectUtils.hasMavenNature(project)) {
                        MavenProjectUtils.enableMavenNature(monitor, project);
                    }
                    talendJavaProject.buildWholeCodeProject();
                }
                buildCacheManager.putJobCache(getProperty());
            } else {
                // for already installed sub jobs, can restore pom here directly
                PomUtil.restorePomFile(getTalendJavaProject());
            }
            return;
        }
        if (isMainJob) {
            String threadParam = "-T 1C";
            if (buildCacheManager.containsMultipleVersionModules()) {
                threadParam = "-T 1";
            }
            if (LOGGER.isInfoEnabled()) {
                LOGGER.info("build, threadParam: " + threadParam);
            }
            final Map<String, Object> argumentsMap = new HashMap<String, Object>();
            argumentsMap.put(TalendProcessArgumentConstant.ARG_GOAL, TalendMavenConstants.GOAL_INSTALL);
            argumentsMap.put(TalendProcessArgumentConstant.ARG_PROGRAM_ARGUMENTS, threadParam + " -f " // $NON-NLS-1$
                    + BuildCacheManager.BUILD_AGGREGATOR_POM_NAME + " -P " + (packagingAndAssembly() ? "" : "!")
                    + TalendMavenConstants.PROFILE_PACKAGING_AND_ASSEMBLY + ",!" + TalendMavenConstants.PROFILE_SIGNATURE); // $NON-NLS-1$  //$NON-NLS-2$
            // install all subjobs
            buildCacheManager.build(monitor, argumentsMap);

            if (!MavenProjectUtils.hasMavenNature(project)) {
                // enable maven nature in case project not create yet.
                MavenProjectUtils.enableMavenNature(monitor, project);
            } else {
                // FIXME should update when maven project already created if:
                // 1. children jar installed.
                // 2. install new jar(for node or config) when editing job.
                // 3. automatically download and install jars in commandline.
                if (buildCacheManager.needTempAggregator() || !CommonUIPlugin.isFullyHeadless()) {
                    MavenProjectUtils.updateMavenProject(monitor, talendJavaProject.getProject());
                }
            }

            buildCacheManager.buildAllSubjobMavenProjects();

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
            argumentsMap.put(TalendProcessArgumentConstant.ARG_PROGRAM_ARGUMENTS,
                    "-Dmaven.main.skip=true -Dmaven.test.skip=true  -P !" //$NON-NLS-1$
                            + TalendMavenConstants.PROFILE_PACKAGING_AND_ASSEMBLY);
        }
        talendJavaProject.buildModules(monitor, null, argumentsMap);
        if (isGoalPackage) {
            if (jobJarFile != null) {
                jobJarFile.refreshLocal(IResource.DEPTH_ONE, null);
            }
            if (jobJarFile == null || !jobJarFile.exists()) {
                String mvnLogFilePath = talendJavaProject.getProject().getFile("lastGenerated.log").getLocation() //$NON-NLS-1$
                        .toPortableString();
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
                // We return the PACKAGE goal if the main job and/or one of its recursive job is a Big Data job.
                return TalendMavenConstants.GOAL_PACKAGE;
            }
        }
        // Else, a simple compilation is needed.
        return TalendMavenConstants.GOAL_COMPILE;
    }

    private String getBuildType(Property property) {
        if (property != null && property.getAdditionalProperties() != null) {
            return (String) property.getAdditionalProperties().get(TalendProcessArgumentConstant.ARG_BUILD_TYPE);
        }
        return null;
    }
}
