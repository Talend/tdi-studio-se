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
package org.talend.designer.runprocess.java;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.ArrayUtils;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.talend.commons.CommonsPlugin;
import org.talend.commons.exception.ExceptionHandler;
import org.talend.commons.exception.PersistenceException;
import org.talend.commons.utils.resource.FileExtensions;
import org.talend.core.CorePlugin;
import org.talend.core.model.process.IElementParameter;
import org.talend.core.model.process.INode;
import org.talend.core.model.process.IProcess;
import org.talend.core.model.properties.ProcessItem;
import org.talend.core.model.properties.Property;
import org.talend.core.model.repository.IRepositoryViewObject;
import org.talend.core.prefs.ITalendCorePrefConstants;
import org.talend.designer.core.runprocess.Processor;
import org.talend.designer.runprocess.IProcessMessageManager;
import org.talend.designer.runprocess.ProcessorConstants;
import org.talend.designer.runprocess.ProcessorException;
import org.talend.designer.runprocess.ProcessorUtilities;
import org.talend.designer.runprocess.RunProcessPlugin;
import org.talend.repository.ui.utils.ZipToFile;
import org.talend.repository.ui.wizards.exportjob.JavaJobScriptsExportWSWizardPage.JobExportType;
import org.talend.repository.ui.wizards.exportjob.scriptsmanager.BuildJobManager;
import org.talend.repository.ui.wizards.exportjob.scriptsmanager.JobScriptsManager.ExportChoice;
import org.talend.repository.ui.wizards.exportjob.scriptsmanager.JobScriptsManagerFactory;
import org.talend.utils.io.FilesUtils;

/**
 * Created by Marvin Wang on Mar 22, 2013.
 */
public abstract class AbstractJavaProcessor extends Processor implements IJavaProcessor {

    private final static String FILEPATH_PREFIX = "job_export"; //$NON-NLS-1$

    private static Logger log = Logger.getLogger(AbstractJavaProcessor.class);

    /** main class of job */
    protected String mainClass;

    protected String unzipFolder;

    protected String archive;

    private Boolean runAsExport;

    protected Boolean requirePackaging;

    /**
     * DOC marvin AbstractJavaProcessor constructor comment.
     *
     * @param process
     */
    public AbstractJavaProcessor(IProcess process) {
        super(process);
    }

    /**
     * if preview or such will be false.
     */
    protected boolean isStandardJob() {
        return true;
    }

    /**
     * If a DI job calls somehow a Big Data job, then the PACKAGE Maven goal must be called on all the jobs.
     *
     * @return true if the job or its recursive childs contain a tRunJob which points to a Big Data job
     */
    protected boolean requirePackaging() {
        if (this.requirePackaging == null) {
            List<? extends INode> generatedNodes = process.getGeneratingNodes();
            try {
                for (INode node : generatedNodes) {
                    if (node.getComponent() != null && "tRunJob".equals(node.getComponent().getName())) {//$NON-NLS-1$
                        IElementParameter elementParameter = node.getElementParameter("PROCESS:PROCESS_TYPE_PROCESS");//$NON-NLS-1$
                        if (elementParameter != null) {
                            Object value = elementParameter.getValue();
                            if (value != null && !"".equals(value)) {//$NON-NLS-1$
                                IRepositoryViewObject lastVersion = RunProcessPlugin.getDefault().getRepositoryService()
                                        .getProxyRepositoryFactory().getLastVersion(value.toString());
                                if (lastVersion != null) {
                                    boolean hasBatchOrStreamingSubProcess = JavaProcessorUtilities.hasBatchOrStreamingSubProcess(lastVersion
                                            .getProperty().getItem());
                                    if (hasBatchOrStreamingSubProcess) {
                                        return true;
                                    }
                                }
                            }
                        }
                    }
                }

            } catch (PersistenceException e) {
                return false;
            }
            return false;
        }
        return this.requirePackaging;
    }

    /**
     *
     * Should be more like ProcessorUtilities.isExportConfig() is true.
     */
    protected boolean isRunAsExport() {
        if (this.runAsExport == null) {
            this.runAsExport = shouldRunAsExport();
        }
        return this.runAsExport;
    }

    protected boolean isExportConfig() {
        return ProcessorUtilities.isExportConfig();
    }

    protected static boolean isExportAsOSGI() {
        return ProcessorUtilities.isExportAsOSGI();
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
    protected List<String> makeUpCommandSegments() {
        List<String> commands = new ArrayList<String>();
        commands.addAll(extractAheadCommandSegments());
        commands.addAll(extractJavaCommandSegments());
        commands.addAll(extractCPCommandSegments());
        commands.add(extractMainClassSegments() == null ? "" : extractMainClassSegments()); //$NON-NLS-1$
        commands.addAll(extractArgumentSegments());
        return commands;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.runprocess.java.IJavaProcessor#extractAheadCommandSegments()
     */
    @Override
    public List<String> extractAheadCommandSegments() {
        return new ArrayList<String>();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.runprocess.java.IJavaProcessor#extractJavaCommandSegments()
     */
    @Override
    public List<String> extractJavaCommandSegments() {
        return new ArrayList<String>();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.runprocess.java.IJavaProcessor#extractCPCommandSegments()
     */
    @Override
    public List<String> extractCPCommandSegments() {
        return new ArrayList<String>();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.runprocess.java.IJavaProcessor#extractMainClassSegments()
     */
    @Override
    public String extractMainClassSegments() {
        return getMainClass();
    }

    @Override
    public String getMainClass() {
        return this.mainClass;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.runprocess.java.IJavaProcessor#extractArgumentSegments()
     */
    @Override
    public List<String> extractArgumentSegments() {
        return new ArrayList<String>();
    }

    @Override
    public Process run(String[] optionsParam, int statisticsPort, int tracePort, IProgressMonitor monitor,
            IProcessMessageManager processMessageManager) throws ProcessorException {
        if (isStandardJob()) {
            Property property = this.getProperty();
            if (property != null) {
                // use the same function with ExportModelJavaProcessor, but will do for maven
                ProcessItem processItem = (ProcessItem) property.getItem();
                if (isRunAsExport()) {
                    // Step 1: Export job
                    archive = buildExportZip(processItem, monitor);
                    // Step 2: Deploy in local(Maybe just unpack)
                    unzipFolder = unzipAndDeploy(process, archive);
                    // Step 3: Run job from given folder.
                    return execFrom(unzipFolder + File.separatorChar + process.getName(), Level.INFO, statisticsPort, tracePort,
                            optionsParam);
                }
            }
        }
        return super.run(optionsParam, statisticsPort, tracePort, monitor, processMessageManager);
    }

    @Override
    protected Process execFrom(String path, Level level, int statOption, int traceOption, String... codeOptions)
            throws ProcessorException {
        String[] cmds = getCommandLine(true, isRunAsExport(), statOption, traceOption, codeOptions);

        checkExecutingCommands(path, cmds);

        cmds = addEncryptionFilePathParameter(cmds);

        logCommandLine(cmds, level);

        return exec(cmds, path);
    }

    /**
     *
     * If need, will correct the path for commands.
     */
    protected String[] checkExecutingCommands(String path, String[] cmds) {
        // for classpath
        return checkExecutingCommandsForRootPath(path, cmds, ProcessorConstants.CMD_KEY_WORD_CP);
    }

    protected String[] checkExecutingCommandsForRootPath(String path, String[] cmds, String argName) {
        if (isRunAsExport()) {
            // replace the variables like $ROOT_PATH
            int cpIndex = ArrayUtils.indexOf(cmds, argName);
            if (cpIndex > -1 && cmds.length > cpIndex + 1) { // found
                String cpStr = cmds[cpIndex + 1];

                Path runDir = null;
                // current path by default
                String userDir = System.getProperty("user.dir"); //$NON-NLS-1$
                if (userDir != null) {
                    runDir = new Path(userDir);
                }
                if (path != null && new File(path).exists()) {
                    runDir = new Path(path); // unify via Path
                }
                if (runDir != null) {
                    cpStr = cpStr.replace(ProcessorConstants.CMD_KEY_WORD_ROOTPATH, runDir.toString());
                    cmds[cpIndex + 1] = cpStr; // set back
                }
            }
        }
        return cmds;
    }

    protected abstract String getRootWorkingDir(boolean withSep);

    /**
     * copied from ExportModelJavaProcessor
     */
    @Override
    public boolean shouldRunAsExport() {
        return false;
    }

    /**
     * copied from ExportModelJavaProcessor
     */
    protected String unzipAndDeploy(IProcess process, String archiveZipFileStr) {
        String unzipFolder = unzipProcess(process, archiveZipFileStr);
        return unzipFolder;
    }

    /**
     * copied from ExportModelJavaProcessor
     */
    protected String unzipProcess(IProcess process, String archiveZipFileStr) {
        String jobName = process.getName();
        String tempFolder = null;
        if (archiveZipFileStr != null && !"".equals(archiveZipFileStr)) { //$NON-NLS-1$
            File file = new File(archiveZipFileStr);
            File tempWorkFolder = new File(file.getParentFile(), jobName);
            tempFolder = tempWorkFolder.getAbsolutePath();
            FilesUtils.deleteFolder(tempWorkFolder, false); // if existed, clean up
            try {
                ZipToFile.unZipFile(archiveZipFileStr, tempFolder);
            } catch (Exception e) {
                ExceptionHandler.process(e);
            }
        }
        return tempFolder;
    }

    protected String buildExportZip(ProcessItem processItem, IProgressMonitor progressMonitor) throws ProcessorException {
        Map<ExportChoice, Object> exportChoiceMap = JobScriptsManagerFactory.getDefaultExportChoiceMap();
        exportChoiceMap.put(ExportChoice.needLauncher, false);
        exportChoiceMap.put(ExportChoice.needJobItem, true);
        // exportChoiceMap.put(ExportChoice.needJobScript, true); //?? for old build? no need
        if (CommonsPlugin.isDebugMode()) {
            exportChoiceMap.put(ExportChoice.needSourceCode, true);
        } else {
            exportChoiceMap.put(ExportChoice.needSourceCode, false);
        }
        exportChoiceMap.put(ExportChoice.binaries, true);
        exportChoiceMap.put(ExportChoice.includeLibs, true);
        exportChoiceMap.put(ExportChoice.needAssembly, true);

        if (progressMonitor.isCanceled()) {
            throw new ProcessorException(new InterruptedException());
        }
        final String archiveFilePath = Path
                .fromOSString(CorePlugin.getDefault().getPreferenceStore().getString(ITalendCorePrefConstants.FILE_PATH_TEMP))
                .append(getFilePathPrefix() + '_' + process.getName() + FileExtensions.ZIP_FILE_SUFFIX).toString();

        try {
            exportChoiceMap.put(ExportChoice.needContext, true);
            String contextName = processItem.getProcess().getDefaultContext();
            exportChoiceMap.put(ExportChoice.contextName, contextName);

            buildJob(archiveFilePath, processItem, processItem.getProperty().getVersion(), contextName, exportChoiceMap,
                    JobExportType.POJO, progressMonitor);
        } catch (Exception e) {
            throw new ProcessorException(e);
        } finally {
            ProcessorUtilities.resetExportConfig();
        }
        return archiveFilePath;
    }

    protected String getFilePathPrefix() {
        return FILEPATH_PREFIX;
    }

    protected void buildJob(String destinationPath, ProcessItem processItem, String version, String ctx,
            Map<ExportChoice, Object> exportChoiceMap, JobExportType jobExportType, IProgressMonitor monitor) throws Exception {
        BuildJobManager.getInstance().buildJob(destinationPath, processItem, version, ctx, exportChoiceMap, jobExportType,
                monitor);
    }

    @Override
    public void cleanWorkingDirectory() throws SecurityException {
        log.debug("Job archive to clean: " + archive); //$NON-NLS-1$
        if (archive != null) {
            File archiveFile = new File(archive);
            if (archiveFile != null && archiveFile.exists() && !CommonsPlugin.isDebugMode()) {
                boolean success = archiveFile.delete();
                if (success) {
                    log.debug("The job archive '" + archive + "' has been deleted successfully"); //$NON-NLS-1$ //$NON-NLS-2$
                }
            }
        }
        log.debug("Job folder to clean: " + unzipFolder); //$NON-NLS-1$
        if (unzipFolder != null) {
            boolean success = FilesUtils.removeFolder(unzipFolder, true);
            if (success) {
                log.debug("The job folder '" + unzipFolder + "' has been deleted successfully"); //$NON-NLS-1$ //$NON-NLS-2$
            }
        }
    }
}
