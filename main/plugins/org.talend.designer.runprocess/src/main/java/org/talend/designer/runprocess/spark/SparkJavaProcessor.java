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
package org.talend.designer.runprocess.spark;

import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.talend.core.model.process.IProcess;
import org.talend.core.model.properties.ProcessItem;
import org.talend.core.model.properties.Property;
import org.talend.designer.runprocess.IProcessor;
import org.talend.designer.runprocess.JobErrorsChecker;
import org.talend.designer.runprocess.RunProcessPlugin;
import org.talend.designer.runprocess.bigdata.BigDataJavaProcessor;
import org.talend.repository.ui.wizards.exportjob.JavaJobScriptsExportWSWizardPage.JobExportType;
import org.talend.repository.ui.wizards.exportjob.scriptsmanager.JobScriptsManager;
import org.talend.repository.ui.wizards.exportjob.scriptsmanager.JobScriptsManager.ExportChoice;
import org.talend.repository.ui.wizards.exportjob.scriptsmanager.MapReduceJobJavaScriptsManager;

/**
 * created by rdubois on 27 janv. 2015 Detailled comment
 *
 */
public class SparkJavaProcessor extends BigDataJavaProcessor {

    private final static String FILEPATH_PREFIX = "spark_export"; //$NON-NLS-1$

    /**
     * DOC rdubois SparkJavaProcessor constructor comment.
     *
     * @param process
     * @param property
     * @param filenameFromLabel
     */
    public SparkJavaProcessor(IProcess process, Property property, boolean filenameFromLabel) {
        super(process, property, filenameFromLabel);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.talend.designer.runprocess.bigdata.BigDataJavaProcessor#createJobScriptsManager(org.talend.core.model.properties
     * .ProcessItem, java.util.Map)
     */
    @Override
    protected JobScriptsManager createJobScriptsManager(ProcessItem processItem, Map<ExportChoice, Object> exportChoiceMap) {
        boolean stats = SparkJavaProcessorUtil.isStatistics();
        return new MapReduceJobJavaScriptsManager(exportChoiceMap, processItem.getProcess().getDefaultContext(),
                JobScriptsManager.ALL_ENVIRONMENTS, stats ? RunProcessPlugin.getDefault().getRunProcessContextManager()
                        .getPortForStatistics() : IProcessor.NO_STATISTICS, IProcessor.NO_TRACES);
    }

    @Override
    protected void buildJob(String destinationPath, ProcessItem processItem, String version, String ctx,
            Map<ExportChoice, Object> exportChoiceMap, JobExportType jobExportType, IProgressMonitor monitor) throws Exception {
        exportChoiceMap.put(ExportChoice.addStatistics, SparkJavaProcessorUtil.isStatistics());

        super.buildJob(destinationPath, processItem, version, ctx, exportChoiceMap, jobExportType, monitor);
        JobErrorsChecker.checkLastGenerationHasCompilationError(false);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.runprocess.bigdata.BigDataJavaProcessor#getFilePathPrefix()
     */
    @Override
    protected String getFilePathPrefix() {
        return FILEPATH_PREFIX;
    }

}
