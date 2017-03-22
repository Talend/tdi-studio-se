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
package org.talend.repository.ui.wizards.exportjob.scriptsmanager;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.talend.core.model.properties.ProcessItem;
import org.talend.core.runtime.process.IBuildJobHandler;
import org.talend.core.runtime.process.TalendProcessArgumentConstant;
import org.talend.core.runtime.repository.build.AbstractBuildProvider;
import org.talend.core.runtime.repository.build.BuildExportManager;
import org.talend.core.runtime.repository.build.IBuildExportHandler;
import org.talend.core.runtime.repository.build.IBuildJobParameters;
import org.talend.core.runtime.repository.build.IBuildParametes;
import org.talend.repository.ui.wizards.exportjob.JavaJobScriptsExportWSWizardPage.JobExportType;
import org.talend.repository.ui.wizards.exportjob.handler.BuildJobHandler;
import org.talend.repository.ui.wizards.exportjob.scriptsmanager.JobScriptsManager.ExportChoice;

/**
 * created by ycbai on 2015年5月15日 Detailled comment
 *
 */
public class BuildJobFactory {

    public static final Map<JobExportType, String> oldBuildTypeMap = new HashMap<JobExportType, String>();
    static {
        // from the extension point
        oldBuildTypeMap.put(JobExportType.POJO, "STANDALONE");
        oldBuildTypeMap.put(JobExportType.OSGI, "OSGI");
    }

    /**
     * Create the build job handler according the job export type. Now only implement the handler of standalone job.
     * <p>
     * DOC ycbai Comment method "createBuildJobHandler".
     * 
     * @param exportChoiceMap
     * @param contextName
     * @param jobExportType
     * @return
     */
    public static IBuildJobHandler createBuildJobHandler(ProcessItem processItem, String contextName, String version,
            Map<ExportChoice, Object> exportChoiceMap, JobExportType jobExportType) {

        if (jobExportType != null)
            switch (jobExportType) {
            case POJO:
                break; // continue
            case WSWAR:
            case WSZIP:
            case OSGI: // TODO, later, when osgi pom is finished, will try to enable it.
                return null; // don't support others
            default:
                jobExportType = null; // try the first one by default
                break;
            }

        String buildType = null;
        if (jobExportType != null) {
            final String newType = oldBuildTypeMap.get(jobExportType);
            if (newType == null) {// not valid type
                return null;
            }
            buildType = newType;
        }

        IBuildJobHandler buildJobHandler = createBuildJobHandler(processItem, contextName, version, exportChoiceMap, buildType);
        if (buildJobHandler == null) {
            // default
            buildJobHandler = new BuildJobHandler(processItem, version, contextName, exportChoiceMap);
        }
        return buildJobHandler;
    }

    public static IBuildJobHandler createBuildJobHandler(ProcessItem processItem, String contextName, String version,
            Map<ExportChoice, Object> exportChoiceMap, String buildType) {

        // if null, will try to find the type from item for build type.
        if (StringUtils.isEmpty(buildType)) {
            final Object type = processItem.getProperty().getAdditionalProperties()
                    .get(TalendProcessArgumentConstant.ARG_BUILD_TYPE);
            if (type != null) {
                buildType = type.toString();
            }// else{ // if didn't set, should use default provider to create it.
        }

        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put(IBuildParametes.ITEM, processItem);
        parameters.put(IBuildParametes.VERSION, version);
        parameters.put(IBuildJobParameters.CONTEXT_GROUP, contextName);
        parameters.put(IBuildJobParameters.CHOICE_OPTION, exportChoiceMap);

        final AbstractBuildProvider buildProvider = BuildExportManager.getInstance().getBuildProvider(buildType, parameters);
        if (buildProvider != null) {
            IBuildExportHandler buildExportHandler = buildProvider.createBuildExportHandler(parameters);
            if (buildExportHandler instanceof IBuildJobHandler) {
                // if buildType is null, will get first one. so use exact provider one.
                buildExportHandler.getArguments().put(TalendProcessArgumentConstant.ARG_BUILD_TYPE,
                        buildProvider.getBuildType().getName());

                return (IBuildJobHandler) buildExportHandler;
            }
        }

        return null;
    }

    public static IBuildJobHandler createBuildJobHandler(ProcessItem processItem, String contextName, String version,
            Map<ExportChoice, Object> exportChoiceMap) {
        // according to the export type from additional properties setting.
        return createBuildJobHandler(processItem, contextName, version, exportChoiceMap, (String) null);
    }

}
