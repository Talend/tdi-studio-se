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
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.ArrayUtils;
import org.apache.log4j.Level;
import org.eclipse.core.runtime.IProgressMonitor;
import org.talend.commons.exception.PersistenceException;
import org.talend.core.model.process.IElementParameter;
import org.talend.core.model.process.INode;
import org.talend.core.model.process.IProcess;
import org.talend.core.model.properties.Property;
import org.talend.core.model.repository.IRepositoryViewObject;
import org.talend.core.runtime.process.TalendProcessArgumentConstant;
import org.talend.designer.runprocess.IProcessMessageManager;
import org.talend.designer.runprocess.ProcessorException;
import org.talend.designer.runprocess.RunProcessPlugin;
import org.talend.designer.runprocess.maven.MavenJavaProcessor;
import org.talend.repository.ui.utils.ZipToFile;

/**
 * Created by wchen on Mar 5, 2013.
 */
public class ExportModelJavaProcessor extends MavenJavaProcessor {

    /**
     * DOC wchen ExportModelJavaProcessor constructor comment.
     *
     * @param process
     * @param property
     * @param filenameFromLabel
     */
    public ExportModelJavaProcessor(IProcess process, Property property, boolean filenameFromLabel) {
        super(process, property, filenameFromLabel);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.core.runprocess.Processor#run(java.lang.String[], int, int,
     * org.eclipse.core.runtime.IProgressMonitor, org.talend.designer.runprocess.IProcessMessageManager)
     */
    @Override
    public Process run(String[] optionsParam, int statisticsPort, int tracePort, IProgressMonitor monitor,
            IProcessMessageManager processMessageManager) throws ProcessorException {
        ExportProcessorHelper helper = new ExportProcessorHelper();

        // export job
        String watchParam = ArrayUtils.contains(optionsParam, TalendProcessArgumentConstant.CMD_ARG_WATCH) ? TalendProcessArgumentConstant.CMD_ARG_WATCH
                : null;
        String log4jLevel = null;
        if (ArrayUtils.contains(optionsParam, TalendProcessArgumentConstant.CMD_ARG_LOG4J_LEVEL)) {
            for (String param : optionsParam) {
                if (param != null && param.indexOf(TalendProcessArgumentConstant.CMD_ARG_LOG4J_LEVEL) >= 0) {
                    log4jLevel = param;
                    break;
                }
            }
        }
        archive = helper.exportJob(this, statisticsPort, tracePort, watchParam, log4jLevel, monitor);
        unzipFolder = unzipAndDeploy(process, archive);

        Process process = super.execFrom(unzipFolder + File.separatorChar + this.process.getName(), Level.INFO, statisticsPort,
                tracePort, optionsParam);

        return process;
    }

    @Override
    public Process run(int statisticsPort, int tracePort, String watchParam, String log4jLevel, IProgressMonitor monitor,
            IProcessMessageManager processMessageManager) throws ProcessorException {
        return run(new String[] { watchParam, log4jLevel }, statisticsPort, tracePort, monitor, processMessageManager);
    }

    @Override
    protected String getLibFolderInWorkingDir() {
        return unzipFolder + File.separator + "lib";
    }

    protected String unzipAndDeploy(IProcess process, String archiveZipFileStr) {
        String unzipFolder = unzipProcess(process, archiveZipFileStr);
        return unzipFolder;
    }

    protected String unzipProcess(IProcess process, String archiveZipFileStr) {
        // throws OozieJobDeployException {
        String jobName = process.getName();
        String tempFolder = null;
        if (archiveZipFileStr != null && !"".equals(archiveZipFileStr)) {
            File file = new File(archiveZipFileStr);
            tempFolder = file.getParentFile().getPath() + File.separator + jobName;
            try {
                ZipToFile.unZipFile(archiveZipFileStr, tempFolder);
            } catch (Exception e) {
                // throw new OozieJobDeployException("Can not unzip a file!", e);
            }
        }
        return tempFolder;
    }

    @Override
    public String[] getCommandLine() throws ProcessorException {
        return super.getCommandLine();
    }

    protected List<String> extractJobJar() {
        List<String> list = new ArrayList<String>();
        String jobFolderString = unzipFolder + File.separator + process.getName();
        File jobFolder = new File(jobFolderString);
        File[] jarFiles = jobFolder.listFiles(new FileFilter() {

            @Override
            public boolean accept(File pathname) {
                if (pathname.getName().endsWith(".jar")) {//$NON-NLS-1$
                    return true;
                }
                return false;
            }
        });
        if (jarFiles != null && jarFiles.length > 0) {
            for (File tmpFile : jarFiles) {
                list.add(tmpFile.getAbsolutePath() + extractClassPathSeparator());
            }
        }
        return list;
    }

    public boolean shouldRunAsExport() {
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
                                boolean hasBatchOrStreamingSubProcess = JavaProcessorUtilities.hasBatchOrStreamingSubProcess(lastVersion.getProperty()
                                        .getItem());
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
}
