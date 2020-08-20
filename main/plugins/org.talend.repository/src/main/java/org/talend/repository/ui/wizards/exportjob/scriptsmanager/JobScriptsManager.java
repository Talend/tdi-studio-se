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
package org.talend.repository.ui.wizards.exportjob.scriptsmanager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.BooleanUtils;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.talend.commons.exception.ExceptionHandler;
import org.talend.core.CorePlugin;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.ITDQItemService;
import org.talend.core.PluginChecker;
import org.talend.core.model.general.Project;
import org.talend.core.model.process.IContext;
import org.talend.core.model.process.IProcess;
import org.talend.core.model.process.ProcessUtils;
import org.talend.core.model.properties.Item;
import org.talend.core.model.properties.ProcessItem;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.model.repository.FakePropertyImpl;
import org.talend.core.model.repository.IRepositoryViewObject;
import org.talend.core.model.repository.ResourceModelUtils;
import org.talend.core.model.utils.PerlResourcesHelper;
import org.talend.core.repository.constants.FileConstants;
import org.talend.core.runtime.process.ITalendProcessJavaProject;
import org.talend.core.runtime.process.LastGenerationInfo;
import org.talend.core.runtime.process.TalendProcessArgumentConstant;
import org.talend.core.runtime.repository.build.BuildExportManager;
import org.talend.core.service.ITransformService;
import org.talend.core.utils.TalendQuoteUtils;
import org.talend.designer.core.IDesignerCoreService;
import org.talend.designer.core.model.utils.emf.talendfile.ContextParameterType;
import org.talend.designer.runprocess.IProcessor;
import org.talend.designer.runprocess.ProcessorException;
import org.talend.designer.runprocess.ProcessorUtilities;
import org.talend.model.bridge.ReponsitoryContextBridge;
import org.talend.repository.ProjectManager;
import org.talend.repository.RepositoryPlugin;
import org.talend.repository.documentation.ExportFileResource;
import org.talend.repository.i18n.Messages;
import org.talend.repository.local.ExportItemUtil;

/**
 * Manages the job scripts to be exported. <br/>
 *
 * $Id: JobScriptsManager.java 1 2006-12-14 下�?�05:06:49 bqian
 *
 */
public abstract class JobScriptsManager {

    protected static final String UNIX_LAUNCHER = "run.sh"; //$NON-NLS-1$

    protected static final String WINDOWS_LAUNCHER = "run.bat"; //$NON-NLS-1$

    protected static final String LIBRARY_FOLDER_NAME = IMavenProperties.LIBRARY_FOLDER_NAME;

    protected static final String PROVIDED_LIB_FOLDER = IMavenProperties.PROVIDED_LIB_FOLDER_NAME;

    protected static final String PATH_SEPARATOR = "/"; //$NON-NLS-1$

    protected static final String JOBINFO_FILE = "jobInfo.properties";//$NON-NLS-1$

    public static final String ALL_ENVIRONMENTS = Messages.getString("JobPerlScriptsManager.allInterpreter").trim(); //$NON-NLS-1$ ;

    public static final String UNIX_ENVIRONMENT = "Unix"; //$NON-NLS-1$

    public static final String WINDOWS_ENVIRONMENT = "Windows"; //$NON-NLS-1$

    protected static final String JOB_SOURCE_FOLDER_NAME = "src"; //$NON-NLS-1$

    protected static final String JOB_ITEMS_FOLDER_NAME = "items"; //$NON-NLS-1$

    public static final String CMDFORWIN = "%*"; //$NON-NLS-1$

    public static final String CMDFORUNIX = "\"$@\""; //$NON-NLS-1$

    public static final String LAUNCHER_ALL = "all"; //$NON-NLS-1$

    protected String selectedJobVersion;

    private String bundleVersion;

    protected IProgressMonitor progressMonitor; // achen added to fix bug
                                                // 0006222

    protected List<ContextParameterType> contextEditableResultValuesList;

    protected Map<ExportChoice, Object> exportChoice;

    protected String contextName;

    protected String launcher;

    protected int statisticPort;

    protected int tracePort;

    private File tempExportFolder;

    private List<Item> exportCaculatedItems = new ArrayList<Item>();

    private List<Item> exportCaculatedProject = new ArrayList<Item>();

    protected String topFolderName;

    public JobScriptsManager(Map<ExportChoice, Object> exportChoiceMap, String contextName, String launcher, int statisticPort,
            int tracePort) {
        this.exportChoice = exportChoiceMap;
        this.contextName = contextName;
        this.launcher = launcher;
        this.statisticPort = statisticPort;
        this.tracePort = tracePort;
    }

    public List<ContextParameterType> getContextEditableResultValuesList() {
        return this.contextEditableResultValuesList;
    }

    public void setContextEditableResultValuesList(List<ContextParameterType> contextEditableResultValuesList) {
        this.contextEditableResultValuesList = contextEditableResultValuesList;
    }

    public void setProgressMonitor(IProgressMonitor progressMonitor) {
        this.progressMonitor = progressMonitor;
    }

    // bug 8720
    protected boolean isOptionChoosed(Object key) {
        if (key != null) {
            final Object object = exportChoice.get(key);
            if (object instanceof Boolean) {
                return BooleanUtils.isTrue((Boolean) object);
            }
        }
        return false;
    }

    public Map<ExportChoice, Object> getExportChoice() {
        return this.exportChoice;
    }

    /**
     *
     * DOC Represent exportchoice <br/>
     * .
     *
     * $Id: JobScriptsExportWizardPage.java 1 2007-1-31下�?�06:14:19 +0000 ylv $
     *
     */
    public enum ExportChoice {
        needMetaInfo,
        needWEBXML,
        needCONFIGFILE,
        needAXISLIB,
        needWSDD,
        needWSDL,
        needLauncher,
        launcherName,
        needSystemRoutine,
        needUserRoutine,
        needPigudf,
        needTalendLibraries,
        needJobItem,
        needJobScript,
        needSourceCode,
        includeLibs,
        includeTestSource,
        executeTests,
        binaries,
        needContext,
        contextName,
        onlyDefautContext,
        applyToChildren,
        applyLog4jToChildren,
        needLog4jLevel,
        log4jLevel,
        addStatistics, // for feature 11031
        addTracs,
        doNotCompileCode,
        needDependencies,
        needParameterValues,
        parameterValuesList,
        needAntScript,
        needMavenScript,
        jobType,
        esbQueueMessageName,
        esbServiceName,
        esbCategory,
        esbExportType,
        esbMetrics,
        properties,
        needAssembly,
        buildImage,
        pushImage,
        dockerHost,
        imageName,
        imageTag,
        pushRegistry,
        registryUsername,
        registryPassword,
        avoidBranchName,
        mainJobOnly,
        clearPasswordContextParameters
    }

    /**
     * qian Gets the export resources.
     *
     * @param process
     * @param needLauncher
     * @param needSystemRoutine
     * @param needUserRoutine
     * @param needModel
     * @param needJob
     * @param needContext
     * @return
     */

    public abstract List<ExportFileResource> getExportResources(ExportFileResource[] process, IContext context,
            String... codeOptions) throws ProcessorException;

    public abstract List<ExportFileResource> getExportResources(ExportFileResource[] process, String... codeOptions)
            throws ProcessorException;


    protected String getTmpFolder() {
        String tmpFold = getTmpFolderPath();
        File f = new File(tmpFold);
        if (!f.exists()) {
            f.mkdir();
        }
        return tmpFold;
    }

    private String getTmpFolderPath() {
        Project project = ProjectManager.getInstance().getCurrentProject();
        String tmpFolder;
        try {
            IProject physProject = ResourceModelUtils.getProject(project);
            tmpFolder = physProject.getFolder("temp").getLocation().toPortableString(); //$NON-NLS-1$
        } catch (Exception e) {
            tmpFolder = System.getProperty("user.dir"); //$NON-NLS-1$
        }
        tmpFolder = tmpFolder + "/talendExporter"; //$NON-NLS-1$
        return tmpFolder;
    }

    /**
     * Gets the perl launcher location.
     *
     * @return
     */
    public static String[] getLauncher() {
        String[] launchers = { ALL_ENVIRONMENTS, UNIX_ENVIRONMENT, WINDOWS_ENVIRONMENT };
        return launchers;
    }

    /**
     *
     * Create launcher(s) and get url(s).
     *
     * @param needLauncher
     * @param process
     * @param contextName
     * @param environment use JobScriptsManager.ALL_ENVIRONMENTS, JobScriptsManager.UNIX_ENVIRONMENT or
     * JobScriptsManager.WINDOWS_ENVIRONMENT
     * @param statisticPort TODO
     * @param tracePort TODO
     * @param codeOptions TODO
     * @return
     */
    protected List<URL> getLauncher(boolean needLauncher, boolean setParameterValues, boolean needContext, IProcess process,
            ProcessItem processItem, String contextName, String environment, int statisticPort, int tracePort,
            String... codeOptions) {

        List<URL> list = new ArrayList<URL>();
        if (!needLauncher) {
            return list;
        }
        String windowsCmd;
        String unixCmd;
        if (process == null) {
            windowsCmd = getCommandByTalendJob(Platform.OS_WIN32, processItem, contextName, needContext, statisticPort, tracePort,
                    codeOptions);
            unixCmd = getCommandByTalendJob(Platform.OS_LINUX, processItem, contextName, needContext, statisticPort, tracePort,
                    codeOptions);
        } else {
            windowsCmd = getCommandByTalendJob(Platform.OS_WIN32, process, contextName, needContext, statisticPort, tracePort,
                    codeOptions);
            unixCmd = getCommandByTalendJob(Platform.OS_LINUX, process, contextName, needContext, statisticPort, tracePort,
                    codeOptions);
        }
        if (setParameterValues) {
            String contextParameterValues = getSettingContextParametersValue();
            if (windowsCmd.contains(CMDFORWIN) && windowsCmd.indexOf(CMDFORWIN) > 2) {
                windowsCmd = windowsCmd.substring(0, windowsCmd.indexOf(CMDFORWIN) - 1) + contextParameterValues + CMDFORWIN;
            }
            if (unixCmd.contains(CMDFORUNIX) && unixCmd.indexOf(CMDFORUNIX) > 2) {
                unixCmd = unixCmd.substring(0, unixCmd.indexOf(CMDFORUNIX) - 1) + contextParameterValues + CMDFORUNIX;
            }
        }

        String tmpFold = getTmpFolder();

        if (environment.equals(ALL_ENVIRONMENTS)) {
            createLauncherFile(processItem, list, unixCmd, UNIX_LAUNCHER, tmpFold);
            createLauncherFile(processItem, list, windowsCmd, WINDOWS_LAUNCHER, tmpFold);
        } else if (environment.equals(UNIX_ENVIRONMENT)) {
            createLauncherFile(processItem, list, unixCmd, UNIX_LAUNCHER, tmpFold);
        } else if (environment.equals(WINDOWS_ENVIRONMENT)) {
            createLauncherFile(processItem, list, windowsCmd, WINDOWS_LAUNCHER, tmpFold);
        }

        return list;
    }

    private String getSettingContextParametersValue() {
        String contextParameterValues = "";//$NON-NLS-1$
        List<ContextParameterType> jobContextValues = getContextEditableResultValuesList();
        for (int i = 0; i < jobContextValues.size(); i++) {
            ContextParameterType contextParameterType = jobContextValues.get(i);
            String name = contextParameterType.getName();
            String value = contextParameterType.getRawValue();
            // name = TalendTextUtils.removeQuotes(name);
            // value = TalendTextUtils.removeQuotes(value);
            if (value == null) {
                contextParameterValues += " " + TalendProcessArgumentConstant.CMD_ARG_CONTEXT_PARAMETER + " " + name + "=" + null;//$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
            } else if (value != null && !"".equals(value)) {//$NON-NLS-1$
                if (value.contains(" ") && !value.startsWith("\"")) { //$NON-NLS-1$ //$NON-NLS-2$
                    // Changed by Marvin Wang on Nov.13, 2012 for bug TDI-23253 to add double quotation marks for value.
                    contextParameterValues += " " + TalendProcessArgumentConstant.CMD_ARG_CONTEXT_PARAMETER + " " + name + "="//$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
                            + TalendQuoteUtils.addQuotes(value);
                } else {
                    contextParameterValues += " " + TalendProcessArgumentConstant.CMD_ARG_CONTEXT_PARAMETER + " " + name + "="//$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
                            + value;
                }
            }
        }
        contextParameterValues = contextParameterValues + " ";//$NON-NLS-1$

        return contextParameterValues;
    }

    /**
     * @deprecated seems never used, so deprecated first.
     */
    @Deprecated
    protected List<URL> getLauncher(boolean needLauncher, boolean setParameterValues, ProcessItem process, String contextName,
            String environment, int statisticPort, int tracePort, String... codeOptions) {

        List<URL> list = new ArrayList<URL>();
        if (!needLauncher) {
            return list;
        }
        String processId = process.getProperty().getId();
        String windowsCmd = getCommandByTalendJob(Platform.OS_WIN32, processId, contextName, process.getProperty().getVersion(),
                statisticPort, tracePort, codeOptions);
        String unixCmd = getCommandByTalendJob(Platform.OS_LINUX, processId, contextName, process.getProperty().getVersion(),
                statisticPort, tracePort, codeOptions);
        if (setParameterValues) {
            String contextParameterValues = getSettingContextParametersValue();
            if (windowsCmd.contains(CMDFORWIN) && windowsCmd.indexOf(CMDFORWIN) > 2) {
                windowsCmd = windowsCmd.substring(0, windowsCmd.indexOf(CMDFORWIN) - 1) + contextParameterValues + CMDFORWIN;
            }
            if (unixCmd.contains(CMDFORUNIX) && unixCmd.indexOf(CMDFORUNIX) > 2) {
                unixCmd = unixCmd.substring(0, unixCmd.indexOf(CMDFORUNIX) - 1) + contextParameterValues + CMDFORUNIX;
            }
        }

        String tmpFold = getTmpFolder();

        if (environment.equals(ALL_ENVIRONMENTS)) {
            createLauncherFile(process, list, unixCmd, UNIX_LAUNCHER, tmpFold);
            createLauncherFile(process, list, windowsCmd, WINDOWS_LAUNCHER, tmpFold);
        } else if (environment.equals(UNIX_ENVIRONMENT)) {
            createLauncherFile(process, list, unixCmd, UNIX_LAUNCHER, tmpFold);
        } else if (environment.equals(WINDOWS_ENVIRONMENT)) {
            createLauncherFile(process, list, windowsCmd, WINDOWS_LAUNCHER, tmpFold);
        }

        return list;
    }

    /**
     * @deprecated <br>
     * seems never used. Call instead the function with IProcess.<br>
     * This avoids to reload the ProcessItem another time.
     */
    @Deprecated
    protected String getCommandByTalendJob(String targetPlatform, String processId, String context, String processVersion,
            int statisticPort, int tracePort, String... codeOptions) {
        String[] cmd = new String[] {};
        try {
            cmd = ProcessorUtilities.getCommandLine(targetPlatform, true, processId, context, processVersion, statisticPort,
                    tracePort, codeOptions);
        } catch (ProcessorException e) {
            ExceptionHandler.process(e);
        }
        return ProcessorUtilities.generateCmdByTalendJob(cmd);
    }

    protected String getCommandByTalendJob(String targetPlatform, ProcessItem processItem, String context, boolean needContext,
            int statisticPort, int tracePort, String... codeOptions) {
        String[] cmd = new String[] {};
        try {
            IDesignerCoreService service = CorePlugin.getDefault().getDesignerCoreService();
            IProcess currentProcess = service.getProcessFromProcessItem(processItem);

            cmd = ProcessorUtilities.getCommandLine(true, targetPlatform, true, currentProcess, processItem.getProperty(),
                    context, needContext, statisticPort, tracePort, codeOptions);
        } catch (ProcessorException e) {
            ExceptionHandler.process(e);
        }
        return ProcessorUtilities.generateCmdByTalendJob(cmd);
    }

    protected String getCommandByTalendJob(String targetPlatform, IProcess process, String context, boolean needContext,
            int statisticPort, int tracePort, String... codeOptions) {
        String[] cmd = new String[] {};
        try {
            cmd = ProcessorUtilities.getCommandLine(true, targetPlatform, true, process, null, context, needContext,
                    statisticPort, tracePort, codeOptions);
        } catch (ProcessorException e) {
            ExceptionHandler.process(e);
        }
        return ProcessorUtilities.generateCmdByTalendJob(cmd);
    }

    /**
     * DOC Administrator Comment method "createLauncherFile".
     *
     * @param process
     * @param list
     * @param cmdPrimary
     * @param cmdSecondary
     * @param tmpFold
     */
    private void createLauncherFile(ProcessItem process, List<URL> list, String cmd, String fileName, String tmpFold) {
        PrintWriter pw = null;
        try {
            String cmdPrimary = cmd;
            // add for bug TDI-24935, add a string on comPrimary's start position.
            if (cmdPrimary != null && UNIX_LAUNCHER.equals(fileName)) {
                StringBuffer strBuffer = new StringBuffer(cmdPrimary);
                strBuffer.insert(0, "#!/bin/sh\n");
                cmdPrimary = strBuffer.toString();
            }
            File file = new File(tmpFold, process.getProperty().getLabel() + "_" + fileName); //$NON-NLS-1$
            file.createNewFile();
            pw = new PrintWriter(new FileOutputStream(file));
            pw.print(cmdPrimary);
            pw.flush();
            list.add(file.toURL());
            pw.close();
        } catch (Exception e) {
            ExceptionHandler.process(e);
        } finally {
            try {
                if (pw != null) {
                    pw.close();
                }
            } catch (Exception e) {
                // do nothing here
            }
        }
    }

    protected File getSystemTempFolder() {
        if (tempExportFolder == null) {
            try {
                tempExportFolder = File.createTempFile("Talend", null); //$NON-NLS-1$
                if (tempExportFolder.exists() && tempExportFolder.isFile()) {
                    tempExportFolder.delete();
                }
                tempExportFolder.mkdirs(); // use the same tmp file to make the tmp folder.

            } catch (IOException e) {
                ExceptionHandler.process(e);
                tempExportFolder = new File(getTmpFolder());
            }

        }
        return tempExportFolder;
    }

    /**
     * Deletes the temporary files.
     */
    public void deleteTempFiles() {
        String tmpFold = getTmpFolderPath();
        File file = new File(tmpFold);
        if (!file.exists() && !file.isDirectory()) {
            return;
        }
        deleteDirectory(file);
    }

    public void deleteDirectory(File dir) {
        File[] entries = dir.listFiles();
        int sz = entries.length;
        for (int i = 0; i < sz; i++) {
            if (entries[i].isDirectory()) {
                deleteDirectory(entries[i]);
            } else {
                entries[i].delete();
            }
        }
        dir.delete();
    }

    /**
     * ftang Comment method "escapeFileNameSpace".
     *
     * @param processItem
     * @return
     */
    protected String escapeFileNameSpace(ProcessItem processItem) {
        String jobName = processItem.getProperty().getLabel();
        return escapeSpace(jobName);
    }

    /**
     * ftang Comment method "escapeSpace".
     *
     * @param name
     * @return
     */
    public String escapeSpace(String name) {
        return PerlResourcesHelper.escapeSpace(name);
    }

    /**
     * Generates the perl files.
     *
     * @param needGenerateCode
     * @param contextName
     * @param process
     * @throws ProcessorException
     */
    protected IProcess generateJobFiles(ProcessItem process, String contextName, boolean statistics, boolean trace,
            boolean applyContextToChildren) throws ProcessorException {
        LastGenerationInfo.getInstance().getUseDynamicMap().clear();
        return ProcessorUtilities.generateCode(process, contextName, statistics, trace, applyContextToChildren).getProcess();
    }

    /**
     * Generates the job files.
     *
     * @param needGenerateCode
     * @param context
     * @param process
     * @throws ProcessorException
     */
    protected IProcess generateJobFiles(ProcessItem process, IContext context, String version, boolean statistics, boolean trace,
            boolean applyContextToChildren, IProgressMonitor monitor) throws ProcessorException {
        LastGenerationInfo.getInstance().getUseDynamicMap().clear();
        return ProcessorUtilities.generateCode(process, context, version, statistics, trace, applyContextToChildren, monitor)
                .getProcess();
    }

    /**
     * Generates the job files.
     *
     * @param needGenerateCode
     * @param contextName
     * @param process
     * @throws ProcessorException
     */
    protected IProcess generateJobFiles(ProcessItem process, String contextName, String version, boolean statistics,
            boolean trace, boolean applyContextToChildren, IProgressMonitor monitor) throws ProcessorException {
        LastGenerationInfo.getInstance().getUseDynamicMap().clear();
        IProcessor processor = ProcessorUtilities.generateCode(process, contextName, version, statistics, trace,
                applyContextToChildren, isOptionChoosed(ExportChoice.needContext), monitor);
        return processor.getProcess();
    }

    protected IResource[] sourceResouces = null;

    private boolean isMultiNodes;

    private String destinationPath;

    private String log4jLevel;

    protected void addNodeToResource(IResource[] resources, List<IResource> sourceFile) throws CoreException {

        for (IResource resource : resources) {
            if (resource instanceof IFolder) {
                IFolder folder = (IFolder) resource;
                addNodeToResource(folder.members(), sourceFile);
            }
            if (resource instanceof IFile) {
                sourceFile.add(resource);
            }

        }
    }

    protected void addJobItem(ExportFileResource[] resources, ProcessItem processItem, boolean needChoice,
            ExportFileResource curResource, String... selectedJobVersion) {
        List<URL> list = new ArrayList<URL>();
        if (needChoice) {
            try {
                Set<File> files = new ExportItemUtil().createLocalResources(new File(getTmpFolder()), processItem);
                for (File file : files) {
                    list.add(file.toURI().toURL());
                }
                curResource.addResources(JOB_SOURCE_FOLDER_NAME, list);
            } catch (Exception e) {
                ExceptionHandler.process(e);
            }
        }
    }

    /**
     * Gets resources' URL.
     *
     * @param resources
     * @param fileNames
     * @return
     */
    protected List<URL> getResourcesURL(IResource[] resources, List<String> fileNames) {
        List<URL> list = new ArrayList<URL>();

        for (String name : fileNames) {
            for (IResource resource : resources) {
                if (resource.getName().equals(name)) {
                    try {
                        URL url = resource.getLocation().toFile().toURL();
                        list.add(FileLocator.toFileURL(url));
                        break;
                    } catch (Exception e) {
                        ExceptionHandler.process(e);
                    }
                }
            }
        }
        return list;
    }

    protected void addToList(List<String> list, String o) {
        if (!list.contains(o)) {
            list.add(o);
        }
    }

    /**
     * ftang Comment method "setJobVersion".
     *
     * @param selectedJobVersion
     */
    public void setJobVersion(String selectedJobVersion) {
        this.selectedJobVersion = selectedJobVersion;
    }

    /**
     * ftang Comment method "getSelectedJobVersion".
     *
     * @return
     */
    public String getSelectedJobVersion() {
        return this.selectedJobVersion;
    }

    /**
     * ftang Comment method "setJobVersion".
     *
     * @param selectedJobVersion
     */
    public void setBundleVersion(String bundleVersion) {
        this.bundleVersion = bundleVersion;
    }

    /**
     * ftang Comment method "getSelectedJobVersion".
     *
     * @return
     */
    public String getBundleVersion() {
        return this.bundleVersion == null ? getSelectedJobVersion() : bundleVersion;
    }

    /**
     * ftang Comment method "setMultiNodes".
     *
     * @param b
     */
    public void setMultiNodes(boolean isMultiNodes) {
        this.isMultiNodes = isMultiNodes;
    }

    /**
     * ftang Comment method "setMultiNodes".
     *
     * @param b
     */
    public boolean isMultiNodes() {
        return this.isMultiNodes;
    }

    protected IPath getEmfFileRootPath(Item item) throws Exception {
        IPath root = getCorrespondingProjectRootPath(item)
                .append(ERepositoryObjectType.getFolderName(ERepositoryObjectType.getItemType(item)));
        return root;
    }

    /**
     * ggu Comment method "getCorrespondingProjectRootPath".
     *
     * if item is null, will return currrent probject path.
     */
    protected IPath getCorrespondingProjectRootPath(Item item) throws Exception {
        // for bug 17685
        org.talend.core.model.properties.Project p = ProjectManager.getInstance().getProject(item);
        IProject project = null;
        if (p != null) {
            project = ResourcesPlugin.getWorkspace().getRoot().getProject(p.getTechnicalLabel().toUpperCase());
            if (project != null) {
                return project.getLocation();
            }
        }
        // maybe, not used
        ITalendProcessJavaProject talendProcessJavaProject = RepositoryPlugin.getDefault().getRunProcessService()
                .getTalendJobJavaProject(item.getProperty());
        if (talendProcessJavaProject == null) {
            return new Path(""); //$NON-NLS-1$
        }
        project = talendProcessJavaProject.getProject();
        IPath root = project.getParent().getLocation().append(getCorrespondingProjectName(item).toUpperCase());
        return root;
    }

    /**
     * Gets current project name.
     *
     * @param item TODO
     *
     * @return
     */
    protected abstract String getCorrespondingProjectName(Item item);

    /**
     * DOC qwei Comment method "addDepencies".
     */
    protected void addDependencies(ExportFileResource[] allResources, ProcessItem processItem, Boolean needDependencies,
            ExportFileResource resource) {
        if (!needDependencies) {
            return;
        }
        // export current job's dependencies.
        if (!exportCaculatedItems.contains(processItem)) {
            BuildExportManager.getInstance().exportDependencies(resource, processItem);
            exportCaculatedItems.add(processItem);
        }

        Collection<IRepositoryViewObject> allDependencies = ProcessUtils
                .getAllProcessDependencies(Arrays.asList(new Item[] { processItem }), false);

        ITransformService tdmService = null;
        if (GlobalServiceRegister.getDefault().isServiceRegistered(ITransformService.class)) {
            tdmService = (ITransformService) GlobalServiceRegister.getDefault().getService(ITransformService.class);
        }

        try {
            for (IRepositoryViewObject object : allDependencies) {
                Item item = object.getProperty().getItem();

                ERepositoryObjectType itemType = ERepositoryObjectType.getItemType(item);
                IPath typeFolderPath = new Path(ERepositoryObjectType.getFolderName(itemType));
                String itemName = item.getProperty().getLabel();
                String itemVersion = item.getProperty().getVersion();
                String itemPath = item.getState().getPath();

                itemPath = (itemPath == null || itemPath.equals("")) ? "" : itemPath; //$NON-NLS-1$ //$NON-NLS-2$
                IPath projectRootPath = getCorrespondingProjectRootPath(item);
                String projectName = getCorrespondingProjectName(item);
                String relativePath = JOB_ITEMS_FOLDER_NAME + PATH_SEPARATOR + projectName;

                // add .settings/com.oaklandsw.base.projectProps for tdm
                if (tdmService != null && !exportCaculatedProject.contains(projectName) && tdmService.isTransformItem(item)) {
                    IPath propsPath = getCorrespondingProjectRootPath(item).append(FileConstants.TDM_PROPS_PATH);
                    String propsRelativePath = relativePath;
                    propsRelativePath = propsRelativePath + PATH_SEPARATOR + FileConstants.TDM_PROPS_FOLDER;
                    resource.addResource(propsRelativePath, propsPath.toFile().toURL());
                }
                // project file
                IPath projectFilePath = getCorrespondingProjectRootPath(item).append(FileConstants.LOCAL_PROJECT_FILENAME);
                checkAndAddProjectResource(allResources, resource, relativePath,
                        FileLocator.toFileURL(projectFilePath.toFile().toURL()));

                // export simple file
                if (projectRootPath != null && item.getProperty() instanceof FakePropertyImpl) {
                    String basePath = relativePath + PATH_SEPARATOR + typeFolderPath.toString();
                    FakePropertyImpl fakeProperty = (FakePropertyImpl) item.getProperty();
                    IPath relativeItemPath = fakeProperty.getItemPath();
                    IPath absItemPath = projectRootPath.removeLastSegments(1).append(relativeItemPath.makeRelative());
                    resource.addResource(basePath, absItemPath.toFile().toURL());
                    continue;
                }

                IPath itemFilePath;
                String itemVersionString = (itemVersion == null) ? "" : "_" + itemVersion;

                if (itemPath.startsWith(typeFolderPath.toString())) {
                    itemPath = itemPath.substring(typeFolderPath.toString().length());
                }
                if (item.getFileExtension() == null || "".equals(item.getFileExtension())) { //$NON-NLS-1$
                    itemFilePath = projectRootPath.append(typeFolderPath).append(itemPath).append(
                            itemName + (item.isNeedVersion() ? itemVersionString : "") + "." + FileConstants.ITEM_EXTENSION); //$NON-NLS-1$
                } else {
                    itemFilePath = projectRootPath.append(typeFolderPath).append(itemPath)
                            .append(itemName + (item.isNeedVersion() ? itemVersionString : "") + "." + item.getFileExtension()); //$NON-NLS-1$
                }

                IPath propertiesFilePath = projectRootPath.append(typeFolderPath).append(itemPath)
                        .append(itemName + itemVersionString + "." //$NON-NLS-1$
                                + FileConstants.PROPERTIES_EXTENSION);

                List<URL> metadataNameFileUrls = new ArrayList<URL>();

                File itemFile = itemFilePath.toFile();
                if (itemFile.exists()) {
                    metadataNameFileUrls.add(FileLocator.toFileURL(itemFile.toURI().toURL()));
                } else {
                    ExceptionHandler.log(Messages.getString("JobScriptsManager.ResourceNotFoundForExport", itemFilePath));
                }

                File propertiesFile = propertiesFilePath.toFile();
                if (propertiesFile.exists()) {
                    metadataNameFileUrls.add(FileLocator.toFileURL(propertiesFile.toURI().toURL()));
                } else {
                    ExceptionHandler.log(Messages.getString("JobScriptsManager.ResourceNotFoundForExport", propertiesFilePath));
                }

                String basePath = relativePath + PATH_SEPARATOR + typeFolderPath.toString();
                if (itemPath != null && !"".equals(itemPath)) { //$NON-NLS-1$
                    basePath = basePath + PATH_SEPARATOR + itemPath;
                }
                resource.addResources(basePath, metadataNameFileUrls);

                // children dependencies
                if (!exportCaculatedItems.contains(item)) {
                    BuildExportManager.getInstance().exportDependencies(resource, item);
                    exportCaculatedItems.add(item);
                }
            }

            if (GlobalServiceRegister.getDefault().isServiceRegistered(ITDQItemService.class)) {
                ITDQItemService tdqItemService = (ITDQItemService) GlobalServiceRegister.getDefault()
                        .getService(ITDQItemService.class);
                if (tdqItemService != null
                        && tdqItemService.hasProcessItemDependencies(Arrays.asList(new Item[] { processItem }))) {
                    // add .Talend.definition file
                    String defIdxFolderName = "TDQ_Libraries"; //$NON-NLS-1$
                    String defIdxFileName = ".Talend.definition"; //$NON-NLS-1$
                    IProject project = ReponsitoryContextBridge.getRootProject();
                    IFile defIdxFile = project.getFile(defIdxFolderName + PATH_SEPARATOR + defIdxFileName);
                    if (defIdxFile.exists()) {
                        String defIdxBasePath = JOB_ITEMS_FOLDER_NAME + PATH_SEPARATOR + project.getName().toLowerCase()
                                + PATH_SEPARATOR + defIdxFolderName;
                        List<URL> defIdxUrls = new ArrayList<URL>();
                        defIdxUrls.add(project.getLocation().makeAbsolute().append(defIdxFolderName).append(defIdxFileName)
                                .toFile().toURI().toURL());
                        resource.addResources(defIdxBasePath, defIdxUrls);
                    }
                    // add report header image & template files
                    String reportingBundlePath = PluginChecker.getBundlePath("org.talend.dataquality.reporting"); //$NON-NLS-1$
                    List<URL> reportResourceUrls = new ArrayList<URL>();
                    File imageFolder = new File(reportingBundlePath + PATH_SEPARATOR + "images"); //$NON-NLS-1$
                    if (imageFolder.exists()) {
                        reportResourceUrls.add(imageFolder.toURI().toURL());
                    }
                    File templateFolder = new File(reportingBundlePath + PATH_SEPARATOR + "reports"); //$NON-NLS-1$
                    if (templateFolder.exists() && templateFolder.isDirectory()) {
                        reportResourceUrls.add(templateFolder.toURI().toURL());
                    }
                    // include all added resources
                    resource.addResources(JOB_ITEMS_FOLDER_NAME + PATH_SEPARATOR, reportResourceUrls);
                }
            }
        } catch (Exception e) {
            ExceptionHandler.process(e);
        }

    }

    protected void checkAndAddProjectResource(ExportFileResource[] allResources, ExportFileResource curResource, String path,
            URL projectURL) {
        if (allResources == null || curResource == null || projectURL == null) {
            return;
        }
        String relativePath = path;
        if (relativePath == null) {
            relativePath = ""; //$NON-NLS-1$
        }
        /*
         * boolean found = false; for (ExportFileResource res : allResources) { Set<URL> urls =
         * res.getResourcesByRelativePath(relativePath); if (urls != null && urls.contains(projectURL)) { found = true;
         * break; } }
         */
        // for bug 13256
        List<URL> projectUrls = new ArrayList<URL>();
        projectUrls.add(projectURL);
        curResource.addResources(relativePath, projectUrls);
    }

    /**
     * DOC Administrator Comment method "getJobContextsComboValue".
     *
     * @param item
     * @return
     */
    public List<String> getJobContextsComboValue(ProcessItem item) {
        // TODO Auto-generated method stub
        return null;
    }

    public String getDestinationPath() {
        return destinationPath;
    }

    /**
     * @param destinationPath the destinationPath to set
     */
    public void setDestinationPath(String destinationPath) {
        this.destinationPath = destinationPath;
    }

    /**
     * Returns the root folder name.
     *
     * @return
     */
    public String getRootFolderName(String path) {
        String subjectString = new Path(path).lastSegment();
        Pattern regex = Pattern.compile("(.*)(?=(\\.(tar|zip|jar|kar))\\b)", Pattern.CANON_EQ | Pattern.CASE_INSENSITIVE //$NON-NLS-1$
                | Pattern.UNICODE_CASE);
        Matcher regexMatcher = regex.matcher(subjectString);
        if (regexMatcher.find()) {
            subjectString = regexMatcher.group(0);
        }
        return subjectString.trim();
    }

    public void setTopFolder(List<ExportFileResource> resourcesToExport) {
        return;
    }

    public String getOutputSuffix() {
        return FileConstants.ZIP_FILE_SUFFIX;
    }

    public String getLog4jLevel() {
        return log4jLevel;
    }

    public void setLog4jLevel(String log4jLevel) {
        this.log4jLevel = log4jLevel;
    }

    /**
     * Getter for topFolderName.
     *
     * @return the topFolderName
     */
    public String getTopFolderName() {
        return this.topFolderName;
    }

    /**
     * Sets the topFolderName.
     *
     * @param topFolderName the topFolderName to set
     */
    public void setTopFolderName(String topFolderName) {
        this.topFolderName = topFolderName;
    }

}
