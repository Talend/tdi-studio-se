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
package org.talend.designer.runprocess.bigdata;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.talend.commons.exception.CommonExceptionHandler;
import org.talend.commons.utils.generation.JavaUtils;
import org.talend.commons.utils.resource.FileExtensions;
import org.talend.core.model.general.ModuleNeeded;
import org.talend.core.model.process.IProcess;
import org.talend.core.model.process.IProcess2;
import org.talend.core.model.properties.ProcessItem;
import org.talend.core.model.properties.Property;
import org.talend.core.runtime.repository.build.IMavenPomCreator;
import org.talend.designer.maven.tools.creator.CreateMavenJobPom;
import org.talend.designer.maven.utils.PomUtil;
import org.talend.designer.runprocess.ProcessorConstants;
import org.talend.designer.runprocess.ProcessorException;
import org.talend.designer.runprocess.ProcessorUtilities;
import org.talend.designer.runprocess.java.JavaProcessorUtilities;
import org.talend.designer.runprocess.maven.MavenJavaProcessor;
import org.talend.repository.ui.wizards.exportjob.scriptsmanager.JobScriptsManager;
import org.talend.repository.ui.wizards.exportjob.scriptsmanager.JobScriptsManager.ExportChoice;
import org.talend.utils.files.FileUtils;
import org.talend.utils.files.FilterInfo;

public abstract class BigDataJavaProcessor extends MavenJavaProcessor {

    protected String windowsAddition, unixAddition;

    /**
     * DOC rdubois BigDataJavaProcessor constructor comment.
     * 
     * @param process
     * @param property
     * @param filenameFromLabel
     */
    public BigDataJavaProcessor(IProcess process, Property property, boolean filenameFromLabel) {
        super(process, property, filenameFromLabel);
    }

    protected abstract JobScriptsManager createJobScriptsManager(ProcessItem processItem,
            Map<ExportChoice, Object> exportChoiceMap);

    /**
     * A Big Data job requires the PACKAGE Maven goal to be executed.
     * 
     * @return true
     */
    @Override
    protected boolean requirePackaging() {
        if (this.requirePackaging == null) {
            this.requirePackaging = true;
        }
        return this.requirePackaging;
    }

    @Override
    public boolean shouldRunAsExport() {
        return false;
    }

    @Override
    protected String getLibFolderInWorkingDir() {
        // ../lib/
        String libRelativePath = ProcessorConstants.CMD_KEY_WORD_TWO_DOT + ProcessorConstants.CMD_KEY_WORD_SLASH
                + ProcessorConstants.CMD_KEY_WORD_LIB + ProcessorConstants.CMD_KEY_WORD_SLASH;

        // if linux, "$ROOT_PATH/../lib/";
        libRelativePath = getRootWorkingDir(true) + libRelativePath;
        return libRelativePath;
    }

    /**
     * This method gets the command list by the method {@link #makeUpCommandSegments()}.
     */
    @Override
    public String[] getCommandLine() throws ProcessorException {
        List<String> commandList = makeUpCommandSegments();
        return commandList.toArray(new String[commandList.size()]);
    }

    /**
     * <pre>
     * Makes all command segments up, like ahead commands, jvm commands, cp commands, main-class command, and others.
     * This method should be invoked by {@link #getCommandLine()}. The following is about some methods invoked by this
     * method. 
     * <li>{@link #extractAheadCommandSegments()} to extract ahead command segments like "cd `dirname $0`\n".
     * <li>{@link #extractJavaCommandSegments()} to extract java command segments like "java -Xms256M ". 
     * <li>{@link #extractCPCommandSegments()} to extract cp command segments like "cp ../a.jar:../b.jar..". 
     * <li>{@link #extractMainClassSegments()} to extract the main-class command segment. 
     * <li>{@link #extractArgumentSegments()} to extract other arguments for commands.
     * </pre>
     * 
     * Added by Marvin Wang on Mar 22, 2013.
     * 
     * @return
     */
    @Override
    protected List<String> makeUpCommandSegments() {
        return super.makeUpCommandSegments();
    }

    @Override
    public List<String> extractAheadCommandSegments() {
        if (!ProcessorUtilities.isExportConfig() && isExternalUse()) {
            return new ArrayList<>();
        }
        return super.extractAheadCommandSegments();
    }

    @Override
    public List<String> extractJavaCommandSegments() {
        List<String> commandSegments = new ArrayList<>();
        String command = ProcessorConstants.CMD_KEY_WORD_JAVA;
        try {
            command = getInterpreter();
        } catch (ProcessorException e1) {
            CommonExceptionHandler.process(e1);
        }

        commandSegments.add(command);
        commandSegments.addAll(extractJavaVMArguments());
        return commandSegments;
    }

    /**
     * Extracts all JVM arguments from job or preference store. It invokes by {@link #extractJavaCommandSegments()}.
     * Added by Marvin Wang on Mar 20, 2013.
     * 
     * @return
     */
    protected List<String> extractJavaVMArguments() {
        List<String> vmArgsSegments = new ArrayList<>();
        String[] vmArgs = getSettingsJVMArguments();
        vmArgsSegments.addAll(Arrays.asList(vmArgs));
        return vmArgsSegments;
    }

    @Override
    public List<String> extractCPCommandSegments() {
        List<String> cpCommandSegments = new ArrayList<>();
        cpCommandSegments.add(ProcessorConstants.CMD_KEY_WORD_CP);
        cpCommandSegments.add(makeUpClassPathString());
        return cpCommandSegments;
    }

    @Override
    public String extractMainClassSegments() {
        return super.extractMainClassSegments();
    }

    @Override
    public List<String> extractArgumentSegments() {
        List<String> list = new ArrayList<>();
        list.add(ProcessorConstants.CMD_KEY_WORD_LIBJAR);
        StringBuffer libJars = new StringBuffer();
        Set<String> libNames = null;
        boolean isExport = isExportConfig() || isRunAsExport();
        if (isExport) {
            // In an export mode, all the dependencies and the routines/beans/udfs are packaged in the lib folder.
            libNames = JavaProcessorUtilities.extractLibNamesOnlyForMapperAndReducer(process);
        } else {
            // In the local mode, all the dependencies are packaged in the lib folder. The routines/beans/udfs are not.
            // We will
            // handle them separetely.
            libNames = JavaProcessorUtilities.extractLibNamesOnlyForMapperAndReducerWithoutRoutines(process);
        }

        File libDir = JavaProcessorUtilities.getJavaProjectLibFolder();
        File targetDir = new File(JavaProcessorUtilities.getTalendJavaProject().getTargetFolder().getLocationURI());

        String libFolder = ""; //$NON-NLS-1$
        if (libDir != null) {
            libFolder = new Path(libDir.getAbsolutePath()).toPortableString();
        }

        // We iterate over the depencendies, and for each of them, we get its path and append it to the libjars
        // StringBuffer.
        if (libNames != null && libNames.size() > 0) {
            Iterator<String> itLibNames = libNames.iterator();
            while (itLibNames.hasNext()) {
                if (isExport) {
                    // In an export mode, the path is relative to the working directory.
                    libJars.append(getLibFolderInWorkingDir() + itLibNames.next()).append(',');
                } else {
                    // In a local mode, the path is absolute.
                    libJars.append(libFolder + "/" + itLibNames.next()).append(','); //$NON-NLS-1$
                }
            }
        }
        if (isExport) {
            // In an export mode, we add the job jar which is located in the current working directory
            libJars.append("./" + makeupJobJarName()); //$NON-NLS-1$
        } else {
            // In a local mode,we must append the routines/beans/udfs jars which are located in the target directory.
            Set<FilterInfo> codeJars = new HashSet<>();
            codeJars.add(new FilterInfo(JavaUtils.ROUTINE_JAR_NAME, FileExtensions.JAR_FILE_SUFFIX));
            codeJars.add(new FilterInfo(JavaUtils.BEANS_JAR_NAME, FileExtensions.JAR_FILE_SUFFIX));
            codeJars.add(new FilterInfo(JavaUtils.PIGUDFS_JAR_NAME, FileExtensions.JAR_FILE_SUFFIX));

            List<File> files = FileUtils.getAllFilesFromFolder(targetDir, codeJars);
            boolean routinesHaveBeenFound = false;
            for (File f : files) {
                if (!routinesHaveBeenFound && f.getName().startsWith(JavaUtils.ROUTINE_JAR_NAME)) {
                    routinesHaveBeenFound = true;
                }
                libJars.append(new Path(f.getAbsolutePath()).toPortableString() + ","); //$NON-NLS-1$
            }
            // If routines are not found, try to guess their future location. This case happens when the target folder
            // is clean and when a DI job tRunJob is asking for the child bigdata command line at generation time, while
            // the routines have
            // not been built yet. Nevertheless, we make the assumption that they are going to be located in the target
            // folder when the job is going to be running.
            if (!routinesHaveBeenFound) {
                File routinesJar = new File(targetDir + "/" + JavaUtils.ROUTINE_JAR_NAME + "-" + PomUtil.getDefaultMavenVersion() //$NON-NLS-1$ //$NON-NLS-2$
                        + FileExtensions.JAR_FILE_SUFFIX);
                libJars.append(new Path(routinesJar.getAbsolutePath()).toPortableString() + ","); //$NON-NLS-1$
            }

            // ... and add the jar of the job itself also located in the target directory/
            if (targetDir != null) {
                libJars.append(new Path(targetDir.getAbsolutePath()).toPortableString() + "/" + makeupJobJarName()); //$NON-NLS-1$
            }

        }
        list.add(libJars.toString());
        return list;
    }

    protected String makeUpClassPathString() {
        StringBuffer sb = new StringBuffer();
        try {
            sb.append(getLibsClasspath());
        } catch (ProcessorException e) {
            e.printStackTrace();
        }
        return sb.toString();
    }

    /**
     * Returns all libs required by classpath, refer to this method {@link #extractCPCommandSegments()}. Added by Marvin
     * Wang on Mar 20, 2013.
     * 
     * @return
     */
    protected Set<String> extractAllLibs() {
        Set<String> libsRequiredByJob = new HashSet<>();
        Set<ModuleNeeded> neededModules = JavaProcessorUtilities.getNeededModulesForProcess(process);
        if (!ProcessorUtilities.isExportConfig()) {
            JavaProcessorUtilities.addLog4jToModuleList(neededModules);
        }
        JavaProcessorUtilities.checkJavaProjectLib(neededModules);

        for (ModuleNeeded neededModule : neededModules) {
            libsRequiredByJob.add(neededModule.getModuleName());
        }
        libsRequiredByJob.addAll(PomUtil.getCodesExportJars(this.getProcess()));
        return libsRequiredByJob;
    }

    /**
     * Makes up a job jar name that should be like "Test-0.1.jar" or "Test.jar". Added by Marvin Wang on Mar 21, 2013.
     * 
     * @return
     */
    protected String makeupJobJarName() {
        String version = process.getVersion();
        String jobJarName = process.getName().toLowerCase() + "_" + version.replace(".", "_") + ".jar"; //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$ //$NON-NLS-4$
        return jobJarName;
    }

    @Override
    protected void setValuesFromCommandline(String tp, String[] cmds) {
        super.setValuesFromCommandline(tp, cmds);

        String libJarStr = null;
        int libJarIndex = ArrayUtils.indexOf(cmds, ProcessorConstants.CMD_KEY_WORD_LIBJAR);
        if (libJarIndex > -1) { // found
            // return the cp values in the next index.
            libJarStr = cmds[libJarIndex + 1];
        }
        if (Platform.OS_WIN32.equals(tp)) {
            this.windowsAddition = libJarStr;
        } else {
            String oldTargetPlatform = this.getTargetPlatform();
            try {
                // because the cmds are from another processor, so must set the target platform same.
                setTargetPlatform(tp);
                // reuse the same api
                String unixRootPath = getRootWorkingDir(true);
                if (libJarStr != null) {
                    this.unixAddition = libJarStr.replace(unixRootPath, ""); // remove the Path root string //$NON-NLS-1$
                }
            } finally {
                setTargetPlatform(oldTargetPlatform);
            }
        }
    }

    @Override
    protected IMavenPomCreator createMavenPomCreator() {
        IMavenPomCreator createMavenTemplatePom = super.createMavenPomCreator();
        if (createMavenTemplatePom instanceof CreateMavenJobPom) {
            CreateMavenJobPom createMavenJobPom = (CreateMavenJobPom) createMavenTemplatePom;

            if (!StringUtils.isBlank(this.windowsAddition)) {
                String windowsScriptAddition = createMavenJobPom.getWindowsScriptAddition();
                if (windowsScriptAddition != null && windowsScriptAddition.length() > 0) {
                    windowsScriptAddition += ' '; // add one space to separate existed additions.
                } else {
                    windowsScriptAddition = ""; //$NON-NLS-1$
                }
                windowsScriptAddition += ProcessorConstants.CMD_KEY_WORD_LIBJAR + ' ' + this.windowsAddition;
                createMavenJobPom.setWindowsScriptAddition(windowsScriptAddition);
            }
            if (!StringUtils.isBlank(this.unixAddition)) {
                String unixScriptAddition = createMavenJobPom.getUnixScriptAddition();
                if (unixScriptAddition != null && unixScriptAddition.length() > 0) {
                    unixScriptAddition += ' '; // add one space to separate existed additions.
                } else {
                    unixScriptAddition = ""; //$NON-NLS-1$
                }
                unixScriptAddition += ProcessorConstants.CMD_KEY_WORD_LIBJAR + ' ' + this.unixAddition;
                createMavenJobPom.setUnixCcriptAddition(unixScriptAddition);
            }
        }
        return createMavenTemplatePom;
    }

    @Override
    public void cleanBeforeGenerate(int options) throws ProcessorException {
        super.cleanBeforeGenerate(options);
        if (needDoClean() && this.getProcess() instanceof IProcess2) {
            // need re-generate the "getGeneratingNodes" to make sure generate the record struct
            // (IRecordStructGenerator) again.
            ((IProcess2) this.getProcess()).setProcessModified(true);
        }
    }

    @Override
    protected String[] checkExecutingCommands(String path, String[] cmds) {
        String[] checkedcmds = super.checkExecutingCommands(path, cmds);
        // also, need check the "-libjars" for BD jobs.
        return checkExecutingCommandsForRootPath(path, checkedcmds, ProcessorConstants.CMD_KEY_WORD_LIBJAR);
    }

}
