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
package org.talend.designer.core.runprocess;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.ArrayUtils;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.talend.commons.exception.ExceptionHandler;
import org.talend.commons.exception.SystemException;
import org.talend.commons.ui.runtime.exception.MessageBoxExceptionHandler;
import org.talend.commons.utils.generation.JavaUtils;
import org.talend.core.model.process.IContext;
import org.talend.core.model.process.IProcess;
import org.talend.core.model.process.ITargetExecutionConfig;
import org.talend.core.model.runprocess.IEclipseProcessor;
import org.talend.core.runtime.process.TalendProcessArgumentConstant;
import org.talend.core.runtime.process.TalendProcessOptionConstants;
import org.talend.designer.codegen.ICodeGenerator;
import org.talend.designer.core.ISyntaxCheckableEditor;
import org.talend.designer.core.i18n.Messages;
import org.talend.designer.runprocess.IProcessMessageManager;
import org.talend.designer.runprocess.IProcessor;
import org.talend.designer.runprocess.ProcessorException;
import org.talend.designer.runprocess.ProcessorUtilities;
import org.talend.repository.ui.wizards.exportjob.scriptsmanager.JobScriptsManager;
import org.talend.utils.StudioKeysFileCheck;

/**
 * DOC nrousseau class global comment. Detailled comment <br/>
 *
 * $Id: Processor.java 52559 2010-12-13 04:14:06Z nrousseau $
 *
 *
 */
public abstract class Processor implements IProcessor, IEclipseProcessor, TalendProcessOptionConstants {

    private static Logger log = Logger.getLogger(Processor.class);

    public static final String CTX_ARG = TalendProcessArgumentConstant.CMD_ARG_CONTEXT_NAME;

    public static final String DEBUG_ROUTE_ID_ARG = "DEBUG_ROUTE_ID_ARG"; //$NON-NLS-1$

    private static final String STAT_PORT_ARG = TalendProcessArgumentConstant.CMD_ARG_STATS_PORT;

    private static final String TRACE_PORT_ARG = TalendProcessArgumentConstant.CMD_ARG_TRACE_PORT;

    private static boolean externalUse = false;

    protected IContext context;

    private ITargetExecutionConfig targetExecutionConfig;

    private String libraryPath;

    private String interpreter;

    private String codeLocation;

    protected IProcess process;

    protected IProject project;

    /** Path to generated context code. */
    protected IPath contextPath;

    /** Path to testContainer dataSet code. */
    protected IPath dataFilePath;

    /** Path to generated job java code. */
    protected IPath codePath;

    protected String targetPlatform;

    private boolean codeGenerated; // will say if the code has been generated at

    private String[] proxyParameters;

    /**
     * only work for the old build job for wizard, because the lib folder is out of project, but after maven, it's in
     * the project. If use Maven way to unify ,can be removed this flag totally. false, by default.
     */
    private boolean oldBuildJob = false;

    private Map<String, Object> argumentsMap;

    private boolean skipClasspathJar;

    // least once

    /**
     * Construct a new Processor.
     *
     * @param process
     *
     * @param process Process to be run.
     */
    public Processor(IProcess process) {
        super();
        this.process = process;
        if (ProcessorUtilities.isExportConfig()) {
            setInterpreter(ProcessorUtilities.getInterpreter());
            setLibraryPath(ProcessorUtilities.getLibraryPath());
            setCodeLocation(ProcessorUtilities.getCodeLocation());
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.runprocess.IProcessor#run(int, int, java.lang.String)
     */
    @Deprecated
    @Override
    public Process run(int statisticsPort, int tracePort, String watchParam) throws ProcessorException {
        return run(new String[] { watchParam }, statisticsPort, tracePort, null, null);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.runprocess.IProcessor#run(java.lang.String[], int, int)
     */
    @Override
    public Process run(String[] optionsParam, int statisticsPort, int tracePort) throws ProcessorException {
        return run(optionsParam, statisticsPort, tracePort, new NullProgressMonitor(), null);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.runprocess.IProcessor#run(int, int, java.lang.String,
     * org.eclipse.core.runtime.IProgressMonitor, org.talend.designer.runprocess.IProcessMessageManager)
     */
    @Deprecated
    @Override
    public Process run(int statisticsPort, int tracePort, String watchParam, String log4jLevel, IProgressMonitor monitor,
            IProcessMessageManager processMessageManager) throws ProcessorException {
        return run(new String[] { watchParam, log4jLevel }, statisticsPort, tracePort, monitor, processMessageManager);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.runprocess.IProcessor#run(java.lang.String[], int, int,
     * org.eclipse.core.runtime.IProgressMonitor, org.talend.designer.runprocess.IProcessMessageManager)
     */
    @Override
    public Process run(String[] optionsParam, int statisticsPort, int tracePort, IProgressMonitor monitor,
            IProcessMessageManager processMessageManager) throws ProcessorException {
        if (context == null) {
            throw new IllegalStateException("Context is empty, context must be set before call"); //$NON-NLS-1$
        }

        setProcessorStates(STATES_RUNTIME);

        if (!codeGenerated) {
            codeGenerated = ProcessorUtilities.generateCode(process, context, statisticsPort != NO_STATISTICS,
                    tracePort != NO_TRACES, true) != null;

            // if the code can't be generated by the ProcessorUtilities, then it
            // will be generated by this way
            // this will be used for example for the shadow process.
            if (!codeGenerated) {
                generateCode(statisticsPort != NO_STATISTICS, tracePort != NO_TRACES, true);
                try {
                    getTalendJavaProject().buildModules(new NullProgressMonitor(), null, null);
                } catch (Exception e) {
                    throw new ProcessorException(e.getMessage());
                }
            }
        }
        if (optionsParam == null) {
            // only works with context name and remove context interpereter
            // option
            return exec(Level.INFO, statisticsPort, tracePort);
        }
        return exec(Level.INFO, statisticsPort, tracePort, optionsParam);
    }

    @Override
    public void cleanWorkingDirectory() throws SecurityException {
        // Nothing to clean.
    }

    /**
     * Debug the process using a given context.
     *
     * @param context Context to be used.
     * @return The configuration to be launched in debug mode.
     * @throws ProcessorException Process failed.
     * @throws CoreException
     * @throws ProcessorException
     */
    @Override
    public ILaunchConfiguration debug() throws ProcessorException {
        if (context == null) {
            throw new IllegalArgumentException("Context is empty, context must be set before call"); //$NON-NLS-1$
        }
        ILaunchConfiguration config = null;
        try {
            setProcessorStates(STATES_EDIT);
            config = (ILaunchConfiguration) saveLaunchConfiguration();
        } catch (CoreException ce) {
            throw new ProcessorException(ce);
        }
        return config;
    }

    /**
     *
     * DOC xzhang Comment method "getDebugConfiguration". For the bug 5430
     *
     * @param statOption
     * @param traceOption
     * @param codeOptions
     * @return
     * @throws ProcessorException
     */
    public ILaunchConfiguration getDebugConfiguration(int statOption, int traceOption, String... codeOptions)
            throws ProcessorException {
        if (context == null) {
            throw new IllegalArgumentException("Context is empty, context must be set before call"); //$NON-NLS-1$
        }

        StringBuilder parameterStr = new StringBuilder(" "); //$NON-NLS-1$
        if (codeOptions != null) {
            for (String string : codeOptions) {
                if (string != null) {
                    parameterStr.append(string).append(" "); //$NON-NLS-1$
                }
            }
        }
        if (statOption != -1) {
            parameterStr = parameterStr.append(STAT_PORT_ARG + statOption).append(" "); //$NON-NLS-1$
        }
        if (traceOption != -1) {
            parameterStr = parameterStr.append(TRACE_PORT_ARG + traceOption).append(" "); //$NON-NLS-1$
        }

        ILaunchConfiguration config = null;
        try {
            setProcessorStates(STATES_EDIT);
            config = (ILaunchConfiguration) saveLaunchConfigurationWithParam(parameterStr.toString());
        } catch (CoreException ce) {
            throw new ProcessorException(ce);
        }
        return config;
    }

    /**
     * Get the executable commandLine.
     *
     * @param contextName
     * @param statOption
     * @param traceOption
     * @param codeOptions
     * @return
     */
    @Override
    public String[] getCommandLine(boolean needContext, boolean externalUse, int statOption, int traceOption,
            String... codeOptions) {
        return getCommandLine(needContext, externalUse, statOption, traceOption, false,
                codeOptions);
    }
    
    @Override
    public String[] getCommandLine(boolean needContext, boolean externalUse, int statOption, int traceOption, boolean ignoreCustomJVMSetting,
            String... codeOptions) {
        setExternalUse(externalUse);
        String[] cmd = null;
        try {
            cmd = getCommandLine(ignoreCustomJVMSetting);

        } catch (ProcessorException e) {
            ExceptionHandler.process(e);
        }
        cmd = addCommmandLineAttch(needContext, cmd, context != null ? context.getName() : IContext.DEFAULT, statOption,
                traceOption, codeOptions);

        // (feature 4258)
        if (Platform.OS_LINUX.equals(getTargetPlatform())) {
            // original is $*
            cmd = (String[]) ArrayUtils.add(cmd, JobScriptsManager.CMDFORUNIX);
        } else if (Platform.OS_WIN32.equals(getTargetPlatform())) {
            cmd = (String[]) ArrayUtils.add(cmd, JobScriptsManager.CMDFORWIN);
        }
        return cmd;
    }

    /**
     * Add the attchment condition to commmandline .
     *
     * @param commandLine
     * @param contextName
     * @param statOption
     * @param traceOption
     * @param codeOptions
     * @return
     */
    protected static String[] addCommmandLineAttch(boolean needContext, String[] commandLine, String contextName, int statOption,
            int traceOption, String... codeOptions) {
        String[] cmd = commandLine;
        if (codeOptions != null) {
            for (String string : codeOptions) {
                if (string != null) {
                    cmd = (String[]) ArrayUtils.add(cmd, string);
                }
            }
        }
        if (needContext && contextName != null) {
            cmd = (String[]) ArrayUtils.add(cmd, CTX_ARG + contextName);
        }
        if (statOption != -1) {
            cmd = (String[]) ArrayUtils.add(cmd, STAT_PORT_ARG + statOption);
        }
        if (traceOption != -1) {
            cmd = (String[]) ArrayUtils.add(cmd, TRACE_PORT_ARG + traceOption);
        }
        return cmd;
    }

    /**
     * Code Execution, used, when you know where the code stands.
     *
     * @param Perl Absolute Code Path
     * @param Context Name
     * @param Port Statistics
     * @param Port Trace
     * @return Command Process Launched
     * @throws ProcessorException
     */
    protected Process exec(Level level, int statOption, int traceOption, String... codeOptions) throws ProcessorException {
        return execFrom(null, level, statOption, traceOption, codeOptions);
    }

    protected Process execFrom(String path, Level level, int statOption, int traceOption, String... codeOptions)
            throws ProcessorException {

        String[] cmd = getCommandLine(true, false, statOption, traceOption, codeOptions);
        cmd = addEncryptionFilePathParameter(cmd);
        logCommandLine(cmd, level);
        return exec(cmd, path);
    }

    protected Process exec(String[] cmd, String path) throws ProcessorException {
        try {
            if (path == null || !new File(path).exists()) {
                return Runtime.getRuntime().exec(cmd);
            } else {
                return Runtime.getRuntime().exec(cmd, null, new File(path));
            }
        } catch (IOException ioe) {
            throw new ProcessorException(Messages.getString("Processor.execFailed"), ioe); //$NON-NLS-1$
        }
    }

    protected String[] addEncryptionFilePathParameter(String[] cmds) {
        String encryptionFilePath = System.getProperty(StudioKeysFileCheck.ENCRYPTION_KEY_FILE_SYS_PROP);
        File encryptionFile = new File(encryptionFilePath);
        boolean isContained = false;
        for (String cmd : cmds) {
            if (cmd != null && cmd.trim().startsWith(StudioKeysFileCheck.ENCRYPTION_KEY_FILE_JVM_PARAM)) {
                isContained = true;
                break;
            }
        }
        if (!isContained) {
            List<String> cmdList = new ArrayList<String>();
            int cpIndex = ArrayUtils.indexOf(cmds, JavaUtils.JAVA_CP);
            for (int i = 0; i < cpIndex; i++) {
                cmdList.add(cmds[i]);
            }
            cmdList.add(StudioKeysFileCheck.ENCRYPTION_KEY_FILE_JVM_PARAM + "=" + encryptionFile.toURI().getPath());
            for (int i = cpIndex; i < cmds.length; i++) {
                cmdList.add(cmds[i]);
            }
            return cmdList.toArray(new String[0]);
        }
        return cmds;
    }

    public static Thread createProdConsThread(final InputStream input, final boolean isError, final int bufferSize,
            final StringBuffer out, final StringBuffer err) {
        Thread thread = new Thread() {

            @Override
            public void run() {
                try {
                    BufferedInputStream outStreamProcess = new BufferedInputStream(input);
                    byte[] buffer = new byte[bufferSize];
                    int count = 0;
                    while ((count = outStreamProcess.read(buffer, 0, buffer.length)) != -1) {
                        if (isError) {
                            err.append(new String(buffer, 0, count));
                        } else {
                            out.append(new String(buffer, 0, count));
                        }
                    }
                    outStreamProcess.close();
                } catch (IOException ioe) {
                    ExceptionHandler.process(ioe);
                } finally {
                    try {
                        input.close();
                    } catch (IOException e) {
                        ExceptionHandler.process(e);
                    }
                }
            }
        };
        return thread;
    }

    public static void logCommandLine(String[] cmd, Level level) {
        StringBuffer sb = new StringBuffer();
        sb.append(Messages.getString("Processor.commandLineLog")); //$NON-NLS-1$
        for (String s : cmd) {
            sb.append(' ').append(s);
        }
        log.log(level, sb.toString());
    }

    /**
     * Sets the externalUse.
     *
     * @param externalUse the externalUse to set
     */
    public static void setExternalUse(boolean externalUse) {
        Processor.externalUse = externalUse;
    }

    protected boolean isExternalUse() {
        return externalUse;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.runprocess.IProcessor#getInterpreter()
     */
    @Override
    public String getInterpreter() throws ProcessorException {
        return interpreter;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.runprocess.IProcessor#setInterpreter(java.lang.String )
     */
    @Override
    public void setInterpreter(String interpreter) {
        this.interpreter = interpreter;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.runprocess.IProcessor#setLibraryPath(java.lang.String )
     */
    @Override
    public void setLibraryPath(String libraryPath) {
        this.libraryPath = libraryPath;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.runprocess.IProcessor#getCodeLocation()
     */
    @Override
    public String getCodeLocation() throws ProcessorException {
        return codeLocation;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.runprocess.IProcessor#setCodeLocation(java.lang.String )
     */
    @Override
    public void setCodeLocation(String codeLocation) {
        this.codeLocation = codeLocation;
    }

    public boolean isOldBuildJob() {
        return oldBuildJob;
    }

    @Override
    public void setOldBuildJob(boolean oldBuildJob) {
        this.oldBuildJob = oldBuildJob;
    }

    @Override
    public abstract void setSyntaxCheckableEditor(ISyntaxCheckableEditor editor);

    public void cleanBeforeGenerate(int options) throws ProcessorException {
        // do something...
    }

    public void generateCode(boolean statistics, boolean trace, boolean properties, int option) throws ProcessorException {
        generateCode(statistics, trace, properties);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.runprocess.IProcessor#generateCode(org.talend.core .model.process.IContext, boolean,
     * boolean, boolean)
     */
    @Override
    public void generateCode(boolean statistics, boolean trace, boolean properties) throws ProcessorException {
        if (context == null) {
            throw new IllegalStateException("Context is empty, context must be set before call"); //$NON-NLS-1$
        }
        // Remove the synchronization of the routines when generate the code.
        // This shouldn't be needed anymore.

        // try {
        // DesignerPlugin.getDefault().getCodeGeneratorService().
        // createRoutineSynchronizer().syncAllRoutines();
        // } catch (SystemException e) {
        // throw new ProcessorException(e);
        // }

        codeGenerated = true; // set the flag to true to tell the code has been
        // generated at least once.
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.runprocess.IProcessor#getCodeContext()
     */
    @Override
    public abstract String getCodeContext();

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.runprocess.IProcessor#getCodePath()
     */
    @Override
    public abstract IPath getCodePath();

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.runprocess.IProcessor#getCodeProject()
     */
    @Override
    public abstract IProject getCodeProject();

    public abstract String[] getCommandLine() throws ProcessorException;
    
    public abstract String[] getCommandLine(boolean ignoreCustomJVMSetting) throws ProcessorException;

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.runprocess.IProcessor#getContextPath()
     */
    @Override
    public abstract IPath getContextPath();

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.runprocess.IProcessor#getDataSetPath()
     */
    @Override
    public abstract IPath getDataSetPath();

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.runprocess.IProcessor#getLineNumber(java.lang.String)
     */
    @Override
    public abstract int getLineNumber(String nodeName);

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.runprocess.IProcessor#getProcessorType()
     */
    @Override
    public abstract String getProcessorType();

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.runprocess.IProcessor#getTypeName()
     */
    @Override
    public abstract String getTypeName();

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.runprocess.IProcessor#initPaths(org.talend.core.model .process.IContext)
     */
    public abstract void initPaths(IContext context) throws ProcessorException;

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.runprocess.IProcessor#saveLaunchConfiguration()
     */
    @Override
    public abstract Object saveLaunchConfiguration() throws CoreException;

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.runprocess.Processor#saveLaunchConfigurationWithParam ()
     */
    public abstract Object saveLaunchConfigurationWithParam(String parameterStr) throws CoreException;

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.runprocess.IProcessor#setProcessorStates(java.lang .String)
     */
    @Override
    public abstract void setProcessorStates(int states);

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.runprocess.IProcessor#setContext(org.talend.core. model.process.IContext)
     */
    @Override
    public void setContext(IContext context) {
        if (context == null) {
            // usefull to generate a commandline only (from ProcessorUtilities)
            return;
        }
        try {
            initPaths(context);
        } catch (ProcessorException pe) {
            MessageBoxExceptionHandler.process(pe);
        }
        this.context = context;
    }

    protected void updateContextCode(ICodeGenerator codeGen) throws ProcessorException {
        if (codeGen == null) {
            return;
        }
        try {
            String processContext = "false"; //$NON-NLS-1$
            try {
                processContext = codeGen.generateContextCode(context);
            } catch (SystemException e) {
                throw new ProcessorException(Messages.getString("Processor.generationFailed"), e); //$NON-NLS-1$
            }

            IFile contextFile = this.getCodeProject().getFile(contextPath);
            InputStream contextStream = new ByteArrayInputStream(processContext.getBytes());

            if (!contextFile.exists()) {
                // see bug 0003592, detele file with different case in windows
                deleteFileIfExisted(contextFile);

                contextFile.create(contextStream, true, null);
            } else {
                contextFile.setContents(contextStream, true, false, null);
            }

        } catch (CoreException e1) {
            throw new ProcessorException(Messages.getString("Processor.tempFailed"), e1); //$NON-NLS-1$
        }
    }
    @Override
    public ITargetExecutionConfig getTargetExecutionConfig() {
        return this.targetExecutionConfig;
    }

    @Override
    public void setTargetExecutionConfig(ITargetExecutionConfig serverConfiguration) {
        this.targetExecutionConfig = serverConfiguration;
    }

    @Override
    public IProcess getProcess() {
        return this.process;
    }

    @Override
    public IContext getContext() {
        return this.context;
    }

    @Override
    public String getTargetPlatform() {
        if (this.targetPlatform == null) {
            this.targetPlatform = Platform.getOS();
        }
        return targetPlatform;
    }

    @Override
    public void setTargetPlatform(String targetPlatform) {
        this.targetPlatform = targetPlatform;
    }

    public boolean checkKillAllowed() {
        return true;
    }

    /**
     * Delete file from the file system if there is another file with different case (lowercase or uppercase) which may
     * cause problem in windows system. See bug 0003592 for more detail.
     *
     * @param codeFile The file that contains source codes that are generated by tos.
     * @throws CoreException
     */
    protected void deleteFileIfExisted(IFile codeFile) throws CoreException {
        File systemFile = codeFile.getLocation().toFile();
        if (systemFile.exists()) {
            systemFile.delete();
            codeFile.getParent().refreshLocal(IResource.DEPTH_INFINITE, null);
        }
    }

    /**
     * Check if the code has been generated at least once. Will be false if the code has never been generated.
     *
     * @return boolean to tell if any code has been generated already or not for this job.
     */
    @Override
    public boolean isCodeGenerated() {
        return this.codeGenerated;
    }

    /**
     * Add the possibility to force the flag for the code generated. <br>
     * This can be usefull to force to generate the code.
     *
     * @param codeGenerated boolean to tell if any code has been generated already or not for this job.
     */
    @Override
    public void setCodeGenerated(boolean codeGenerated) {
        this.codeGenerated = codeGenerated;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.runprocess.IProcessor#generateContextCode()
     */
    @Override
    abstract public void generateContextCode() throws ProcessorException;

    @Override
    public String[] getProxyParameters() {
        return this.proxyParameters;
    }

    @Override
    public void setProxyParameters(String[] proxyParameters) {
        if (proxyParameters != null) {
            this.proxyParameters = proxyParameters;
        }

    }

    @Override
    public Map<String, Object> getArguments() {
        return this.argumentsMap;
    }

    @Override
    public void setArguments(Map<String, Object> argumentsMap) {
        this.argumentsMap = argumentsMap;
    }

    public String[] getJVMArgs() {
        return new String[0];
    }

    protected boolean isWinTargetPlatform() {
        if (Platform.OS_WIN32.equals(targetPlatform) || targetPlatform == null && Platform.getOS().contains(Platform.WS_WIN32)) {
            return true;
        }
        return false;
    }

    public boolean shouldRunAsExport() {
        return false; // by default, for standard job, run in .Java project
    }

    @Override
    public void setSkipClasspathJar(boolean skipClasspathJar) {
        this.skipClasspathJar = skipClasspathJar;
    }

    @Override
    public boolean isSkipClasspathJar() {
        return skipClasspathJar;
    }

}
