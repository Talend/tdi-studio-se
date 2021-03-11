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
package org.talend.designer.runprocess.storm;

import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.talend.core.model.process.IProcess;
import org.talend.core.model.properties.ProcessItem;
import org.talend.core.model.properties.Property;
import org.talend.designer.runprocess.IProcessor;
import org.talend.designer.runprocess.bigdata.BigDataJavaProcessor;
import org.talend.repository.ui.wizards.exportjob.scriptsmanager.JobScriptsManager;
import org.talend.repository.ui.wizards.exportjob.scriptsmanager.JobScriptsManager.ExportChoice;
import org.talend.repository.ui.wizards.exportjob.scriptsmanager.StormJobJavaScriptsManager;

/**
 * <pre>
 * This is a java processor for map/reduce job to run. For map/reduce job the way to run is not the same as common job.
 * The following is the steps to run map/reduce job:
 * <li>1. Build a zip exported with m/r job name like "mrJob.zip" by {@link #buildExportZip(ProcessItem, IProgressMonitor)}.
 * <li>2. Unzip the zip file by {@link #unzipAndDeploy(IProcess, String)}.
 * <li>3. Get the commands by {@link #getCommandLine()} to execute job.
 * </pre>
 *
 * Created by Marvin Wang on Mar 5, 2013.
 */
public class StormJavaProcessor extends BigDataJavaProcessor {

    private final static String FILEPATH_PREFIX = "storm_export"; //$NON-NLS-1$

    /**
     * DOC marvin RemoteMapReduceJavaProcessor constructor comment.
     *
     * @param process
     * @param property
     * @param filenameFromLabel
     */
    public StormJavaProcessor(IProcess process, Property property, boolean filenameFromLabel) {
        super(process, property, filenameFromLabel);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.talend.designer.runprocess.bigdata.BigDataJavaProcessor#getJobScriptsManager(org.talend.core.model.properties
     * .ProcessItem, java.util.Map)
     */
    @Override
    protected JobScriptsManager createJobScriptsManager(ProcessItem processItem, Map<ExportChoice, Object> exportChoiceMap) {
        return new StormJobJavaScriptsManager(exportChoiceMap, processItem.getProcess().getDefaultContext(),
                JobScriptsManager.ALL_ENVIRONMENTS, IProcessor.NO_STATISTICS, IProcessor.NO_TRACES);
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