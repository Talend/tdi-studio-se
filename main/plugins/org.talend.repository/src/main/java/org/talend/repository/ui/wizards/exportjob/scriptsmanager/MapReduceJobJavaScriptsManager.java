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
package org.talend.repository.ui.wizards.exportjob.scriptsmanager;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.talend.commons.exception.CommonExceptionHandler;
import org.talend.core.hadoop.version.EHadoopDistributions;
import org.talend.core.model.properties.ProcessItem;
import org.talend.core.model.utils.JavaResourcesHelper;
import org.talend.core.repository.constants.FileConstants;
import org.talend.core.runtime.repository.build.BuildExportManager;
import org.talend.designer.runprocess.ProcessorException;
import org.talend.repository.documentation.ExportFileResource;

/**
 * Created by Marvin Wang on Mar 23, 2013.
 */
public class MapReduceJobJavaScriptsManager extends JobJavaScriptsManager {

    private boolean isHDI;

    /**
     * DOC marvin MapReduceJobJavaScriptsManager constructor comment.
     *
     * @param exportChoiceMap
     * @param contextName
     * @param launcher
     * @param statisticPort
     * @param tracePort
     */
    public MapReduceJobJavaScriptsManager(Map<ExportChoice, Object> exportChoiceMap, String contextName, String launcher,
            int statisticPort, int tracePort) {
        super(exportChoiceMap, contextName, launcher, statisticPort, tracePort);
    }

    @Override
    protected List<URL> getJobScripts(ProcessItem process, String version, boolean needJob) {
        setHDI(isHDInsight(process));
        return super.getJobScripts(process, version, needJob);
    }

    /**
     * Overrides the parent method to do a special handling, that is to include "context" file in job.jar.
     *
     * @return
     */
    @Override
    protected List<URL> getJobScripts(ProcessItem process, String projectName, String jobName, String jobVersion, boolean needJob) {
        List<URL> list = new ArrayList<URL>(1);
        if (!needJob) {
            return list;
        }

        try {
            String jobFolderName = JavaResourcesHelper.getJobFolderName(jobName, jobVersion);

            File jarFile = new File(getTmpFolder() + File.separatorChar + jobFolderName + FileConstants.JAR_FILE_SUFFIX);
            // Exports the jar file
            File classRootFileLocation = getJobClassRootFileLocation(process.getProperty());
            if (classRootFileLocation == null) {
                return Collections.emptyList();
            }
            JarBuilder jarbuilder = new JarBuilder(classRootFileLocation, jarFile);

            // builds the jar file of the job classes,needContext specifies whether inclucdes the context.
            // add the job
            String jobPath = projectName + PATH_SEPARATOR + jobFolderName;
            jarbuilder.setIncludeDir(Collections.singleton(jobPath));

            // Do not remove the context from job.jar.
            jarbuilder.setExcludeDir(null);
            if (isHDI) {
                jarbuilder.setLibPath(getLibPath(true));
            }
            jarbuilder.buildJar();
            list.add(jarFile.toURI().toURL());
        } catch (IOException e) {
            CommonExceptionHandler.process(e);
        }

        return list;
    }

    public void setHDI(boolean isHDI) {
        this.isHDI = isHDI;
    }

    private boolean isHDInsight(ProcessItem process) {
        Object distribution = getProcessParameterValue(process, "DISTRIBUTION");//$NON-NLS-1$
        if (EHadoopDistributions.MICROSOFT_HD_INSIGHT.getName().equals(distribution)) {
            return true;
        }
        return false;
    }

    @Override
    public List<ExportFileResource> getExportResources(ExportFileResource[] processes, String... codeOptions)
            throws ProcessorException {
        List<ExportFileResource> exportResources = super.getExportResources(processes, codeOptions);

        ProcessItem processItem = null;
        for (ExportFileResource process : processes) {
            if (process.getItem() instanceof ProcessItem) {
                // Explicitly add TDM's resources to the MapReduce process item.
                BuildExportManager.getInstance().exportDependencies(process, process.getItem());
            }
        }

        return exportResources;
    }

}
