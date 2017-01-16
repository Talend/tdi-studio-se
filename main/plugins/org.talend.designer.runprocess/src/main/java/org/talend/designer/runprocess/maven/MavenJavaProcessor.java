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
package org.talend.designer.runprocess.maven;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.ArrayUtils;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.talend.commons.exception.ExceptionHandler;
import org.talend.commons.utils.generation.JavaUtils;
import org.talend.commons.utils.resource.FileExtensions;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.model.process.IProcess;
import org.talend.core.model.process.JobInfo;
import org.talend.core.model.process.ProcessUtils;
import org.talend.core.model.properties.ProcessItem;
import org.talend.core.model.properties.Property;
import org.talend.core.model.utils.JavaResourcesHelper;
import org.talend.core.repository.utils.ItemResourceUtil;
import org.talend.core.runtime.process.ITalendProcessJavaProject;
import org.talend.core.runtime.process.TalendProcessArgumentConstant;
import org.talend.core.runtime.process.TalendProcessOptionConstants;
import org.talend.core.ui.ITestContainerProviderService;
import org.talend.designer.maven.model.TalendMavenConstants;
import org.talend.designer.maven.tools.ProjectPomManager;
import org.talend.designer.maven.tools.creator.CreateMavenBundleTemplatePom;
import org.talend.designer.maven.tools.creator.CreateMavenJobPom;
import org.talend.designer.maven.tools.creator.CreateMavenTestPom;
import org.talend.designer.maven.utils.PomUtil;
import org.talend.designer.runprocess.ProcessorException;
import org.talend.designer.runprocess.ProcessorUtilities;
import org.talend.designer.runprocess.java.JavaProcessor;
import org.talend.designer.runprocess.java.JavaProcessorUtilities;

/**
 * created by ggu on 2 Feb 2015 Detailled comment
 *
 */
public class MavenJavaProcessor extends JavaProcessor {

    protected String windowsClasspath, unixClasspath;

    public MavenJavaProcessor(IProcess process, Property property, boolean filenameFromLabel) {
        super(process, property, filenameFromLabel);
    }

    @Override
    public void generateCode(boolean statistics, boolean trace, boolean javaProperties, int option) throws ProcessorException {
        super.generateCode(statistics, trace, javaProperties, option);
        // only job, now for Shadow Process/Data Preview.
        if (isStandardJob()) {
            generatePom();
        }

        updateProjectPom(null);
    }

    @Override
    public Set<JobInfo> getBuildChildrenJobs() {
        if (buildChildrenJobs == null || buildChildrenJobs.isEmpty()) {
            buildChildrenJobs = new HashSet<>();

            if (property != null) {
                Set<JobInfo> infos = ProcessorUtilities.getChildrenJobInfo((ProcessItem) property.getItem());
                for (JobInfo jobInfo : infos) {
                    if (jobInfo.isTestContainer() && !ProcessUtils.isOptionChecked(getArguments(),
                            TalendProcessArgumentConstant.ARG_GENERATE_OPTION, TalendProcessOptionConstants.GENERATE_TESTS)) {
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
            setPlatformValues(Platform.OS_WIN32, contextName);
            setPlatformValues(Platform.OS_LINUX, contextName);
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
            String[] cmds = ProcessorUtilities.getCommandLine(false, tp, true, process, null, contextName, false, -1, -1);
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
            String pomFileName = PomUtil.getPomFileName(this.getProperty().getLabel(), this.getProperty().getVersion());
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
            String assemblyFileName = PomUtil.getAssemblyFileName(this.getProperty().getLabel(), this.getProperty().getVersion());
            return this.getTalendJavaProject().getAssembliesFolder().getFile(assemblyFileName);
        } else { // not standard job, won't have assembly file.
            return null;
        }
    }

    protected void generatePom() {
        initJobClasspath();

        try {

            CreateMavenBundleTemplatePom createTemplatePom = createMavenTemplatePom();
            if (createTemplatePom != null) {
                createTemplatePom.setOverwrite(true);
                boolean previousValue = ProcessUtils.isHDInsight();
                ProcessUtils.setHDInsight(ProcessUtils.isDistributionExist((ProcessItem) property.getItem()));
                createTemplatePom.create(null);
                ProcessUtils.setHDInsight(previousValue);
            }
        } catch (Exception e) {
            ExceptionHandler.process(e);
        }

    }

    protected CreateMavenBundleTemplatePom createMavenTemplatePom() {
        boolean isTestContainer = false;
        if (GlobalServiceRegister.getDefault().isServiceRegistered(ITestContainerProviderService.class)) {
            ITestContainerProviderService testContainerService = (ITestContainerProviderService) GlobalServiceRegister
                    .getDefault().getService(ITestContainerProviderService.class);
            if (testContainerService != null) {
                isTestContainer = testContainerService.isTestContainerItem(this.getProperty().getItem());
            }
        }

        CreateMavenBundleTemplatePom createMavenPom = null;
        if (!isTestContainer) {
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

            createMavenPom = createTemplatePom;
        } else {
            createMavenPom = new CreateMavenTestPom(this, getPomFile());
        }
        createMavenPom.setArgumentsMap(getArguments());
        return createMavenPom;
    }

    /**
     * update the project pom, and make sure the standard or fake(Preview/Data view) job can be compiled still.
     */
    protected void updateProjectPom(IProgressMonitor monitor) {
        try {
            if (monitor == null) {
                monitor = new NullProgressMonitor();
            }
            JavaProcessorUtilities.checkJavaProjectLib(getNeededModules());

            ProjectPomManager pomManager = new ProjectPomManager(getTalendJavaProject().getProject()) {

                @Override
                protected boolean isStandardJob() {
                    return MavenJavaProcessor.this.isStandardJob();
                }

                @Override
                protected IFile getBasePomFile() {
                    return MavenJavaProcessor.this.getPomFile();
                }

            };

            pomManager.setUpdateModules(isStandardJob()); // won't update module for fake job.
            if (getArguments() != null) {
                pomManager.setArgumentsMap(getArguments());
            }
            pomManager.update(monitor, this);
        } catch (Exception e) {
            ExceptionHandler.process(e);
        }
    }

    @Override
    public void build(IProgressMonitor monitor) throws Exception {
        final ITalendProcessJavaProject talendJavaProject = getTalendJavaProject();
        // compile with JDT first in order to make the maven packaging work with a JRE.
        if (TalendMavenConstants.GOAL_PACKAGE.equals(getGoals())) {
            talendJavaProject.buildModules(monitor, null, null);
        }

        final Map<String, Object> argumentsMap = new HashMap<>();
        argumentsMap.put(TalendProcessArgumentConstant.ARG_GOAL, getGoals());
        argumentsMap.put(TalendProcessArgumentConstant.ARG_PROGRAM_ARGUMENTS, "-Dmaven.main.skip=true -P !" //$NON-NLS-1$
                + TalendMavenConstants.PROFILE_PACKAGING_AND_ASSEMBLY);
        talendJavaProject.buildModules(monitor, null, argumentsMap);
    }

    protected String getGoals() {
        if (isTestJob) {
            return TalendMavenConstants.GOAL_TEST_COMPILE;
        }

        if (!ProcessorUtilities.isExportConfig() && requirePackaging()) {
            // We return the PACKAGE goal if the main job and/or one of its recursive job is a Big Data job.
            return TalendMavenConstants.GOAL_PACKAGE;
        } else {
            // Else, a simple compilation is needed.
            return TalendMavenConstants.GOAL_COMPILE;
        }
    }
}
