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
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.talend.commons.utils.resource.FileExtensions;
import org.talend.core.CorePlugin;
import org.talend.core.model.process.IContext;
import org.talend.core.model.process.IContextParameter;
import org.talend.core.model.properties.ProcessItem;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.prefs.ITalendCorePrefConstants;
import org.talend.core.runtime.process.TalendProcessArgumentConstant;
import org.talend.designer.core.model.utils.emf.talendfile.ContextParameterType;
import org.talend.designer.core.model.utils.emf.talendfile.ContextType;
import org.talend.designer.core.runprocess.Processor;
import org.talend.designer.runprocess.IProcessor;
import org.talend.designer.runprocess.ProcessorException;
import org.talend.repository.ui.wizards.exportjob.JavaJobScriptsExportWSWizardPage.JobExportType;
import org.talend.repository.ui.wizards.exportjob.scriptsmanager.BuildJobManager;
import org.talend.repository.ui.wizards.exportjob.scriptsmanager.JobScriptsManager.ExportChoice;
import org.talend.repository.ui.wizards.exportjob.scriptsmanager.JobScriptsManagerFactory;
import org.talend.utils.files.FileUtils;

/**
 * created by Administrator on 2013-3-15 Detailled comment
 *
 */
public class ExportProcessorHelper {

    protected static final String NAME_PREFIX = "Remote"; //$NON-NLS-1$

    protected static final String NAME_SUFFIX = "Export"; //$NON-NLS-1$

    protected String processName = null;

    private File tempExportFolder;

    protected File getTmpExportFolder() {
        if (tempExportFolder == null) {
            tempExportFolder = createTmpFolder();
        }
        return tempExportFolder;
    }

    protected File createTmpFolder() {
        File tempFolder = null;
        String tempFolderName = null;
        try {
            tempFolder = File.createTempFile(NAME_PREFIX, NAME_SUFFIX);
            tempFolder.delete();
            tempFolderName = tempFolder.getName();
        } catch (IOException e) {
            //
        }
        if (tempFolderName == null) {
            tempFolderName = NAME_PREFIX + System.currentTimeMillis() + NAME_SUFFIX;
        }

        IPath studioTmpPath = Path.fromOSString(CorePlugin.getDefault().getPreferenceStore()
                .getString(ITalendCorePrefConstants.FILE_PATH_TEMP));
        File studioTmpFolder = studioTmpPath.toFile();
        if (!studioTmpFolder.exists()) {
            studioTmpFolder.mkdirs();
        }
        if (studioTmpFolder.exists()) {
            File workFolder = new File(studioTmpFolder, tempFolderName);
            workFolder.mkdirs();
            if (workFolder.exists()) {// if enable to create, set it as temp export folder
                tempFolder = workFolder;
            }

        }

        if (tempFolder == null) { // can't create temp folder still
            tempFolder = FileUtils.createUserTmpFolder(tempFolderName);
        }
        tempFolder.mkdirs();

        return tempFolder;
    }

    public String exportJob(Processor processor, int statisticsPort, int tracePort, String watchParam, String log4jLevel,
            final IProgressMonitor progressMonitor) throws ProcessorException {
        ProcessItem processItem = (ProcessItem) processor.getProperty().getItem();
        processName = processor.getProperty().getLabel();

        File archiveFile = new File(getTmpExportFolder(), "remote_run_export-" + processName + FileExtensions.ZIP_FILE_SUFFIX); //$NON-NLS-1$

        Properties prop = new Properties();
        if (watchParam != null) {
            prop.setProperty(TalendProcessArgumentConstant.ARG_ENABLE_WATCH, watchParam);
        }
        // FIXME, maybe should try another way. it's not good, I think.
        // update directly the .item (without save it) in case of prompt
        // then the generation will be correct automatically in the .properties
        IContext context = processor.getContext();
        if (context != null) {
            List<IContextParameter> contextParameterList = context.getContextParameterList();
            if (contextParameterList != null && contextParameterList.size() > 0) {
                for (IContextParameter contextParameter : contextParameterList) {
                    if (!contextParameter.isPromptNeeded()) {
                        continue;
                    }
                    for (Object curCType : processItem.getProcess().getContext()) {
                        ContextType cType = (ContextType) curCType;
                        if (context.getName().equals(cType.getName())) {
                            for (Object curParam : cType.getContextParameter()) {
                                ContextParameterType cParamType = (ContextParameterType) curParam;
                                if (contextParameter.getName().equals(cParamType.getName())) {
                                    cParamType.setRawValue(contextParameter.getValue());
                                }
                            }
                        }
                    }
                }
            }
        }

        export(progressMonitor, processItem, ERepositoryObjectType.getItemType(processItem), processor.getContext().getName(),
                archiveFile.toString(), log4jLevel, false, statisticsPort, tracePort, prop);

        return archiveFile.toString();
    }

    public void cleanWorkingDirectory() {
        File tmpExportFolder = getTmpExportFolder();
        if (tmpExportFolder != null) {
            org.talend.utils.io.FilesUtils.deleteFolder(tmpExportFolder, true);
        }
    }


    /**
     *
     * DOC ggu Comment method "export".
     *
     * export for execute or deploy the job on job server.
     */
    public void export(IProgressMonitor monitor, ProcessItem item, ERepositoryObjectType type, String context,
            String archiveFile, String log4jLevel, boolean applyContextToChildren, int statisticsPort, int tracePort,
            Properties properties) throws ProcessorException {
        if (item == null) {
            throw new ProcessorException("Can't find the job");
        }
        if (monitor == null) {
            monitor = new NullProgressMonitor();
        }

        // old way
        // export(destDir, archiveFileName, context, log4jLevel, applyContextToChildren, statisticsPort, tracePort,
        // statisticsPort > IProcessor.NO_STATISTICS, name, version, null, false, false, false);

        // mostly, should same as BuildJobExecuteCommand
        Map<ExportChoice, Object> exportChoiceMap = JobScriptsManagerFactory.getDefaultExportChoiceMap();
        exportChoiceMap.put(ExportChoice.doNotCompileCode, false);
        exportChoiceMap.put(ExportChoice.needDependencies, true);
        exportChoiceMap.put(ExportChoice.addStatistics, (statisticsPort > IProcessor.NO_STATISTICS));
        exportChoiceMap.put(ExportChoice.addTracs, (tracePort > IProcessor.NO_TRACES));
        exportChoiceMap.put(ExportChoice.needAntScript, false);
        exportChoiceMap.put(ExportChoice.needMavenScript, false);
        exportChoiceMap.put(ExportChoice.applyToChildren, applyContextToChildren);
        exportChoiceMap.put(ExportChoice.needContext, true);
        exportChoiceMap.put(ExportChoice.binaries, true);
        exportChoiceMap.put(ExportChoice.needSourceCode, false);
        exportChoiceMap.put(ExportChoice.executeTests, false);
        exportChoiceMap.put(ExportChoice.includeTestSource, false);
        exportChoiceMap.put(ExportChoice.includeLibs, true);
        exportChoiceMap.put(ExportChoice.needLog4jLevel, log4jLevel != null);
        exportChoiceMap.put(ExportChoice.log4jLevel, log4jLevel);

        // set like the method export(...) for buildJob
        exportChoiceMap.put(ExportChoice.jobType, type);
        if (context == null) {
            context = item.getProcess().getDefaultContext();
        }
        exportChoiceMap.put(ExportChoice.contextName, context);

        // add some other addition arguments in "properties"
        Properties prop = new Properties();
        if (properties != null) { // add init properties.
            Enumeration<Object> keys = properties.keys();
            while (keys.hasMoreElements()) {
                String key = keys.nextElement().toString();
                String value = properties.getProperty(key);
                prop.put(key, value);
            }
        }
        // prop.put(TalendProcessArgumentConstant.ARG_PORT_STATS, statisticsPort);
        // prop.put(TalendProcessArgumentConstant.ARG_PORT_TRACS, tracePort);
        exportChoiceMap.put(ExportChoice.properties, prop);

        if (monitor.isCanceled()) {
            throw new ProcessorException(new InterruptedException());
        }
        try {
            BuildJobManager.getInstance().buildJob(archiveFile, item, item.getProperty().getVersion(), context, exportChoiceMap,
                    JobExportType.POJO, monitor);
        } catch (InvocationTargetException e) {
            if (e.getTargetException() != null) {
                throw new ProcessorException(e.getTargetException());
            } else {
                throw new ProcessorException(e);
            }
        } catch (Exception e) {
            throw new ProcessorException(e);
        }
    }
}
