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

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.talend.core.model.properties.Item;
import org.talend.core.model.properties.ProcessItem;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.runtime.process.IBuildJobHandler;
import org.talend.core.runtime.process.TalendProcessArgumentConstant;
import org.talend.core.runtime.repository.build.AbstractBuildProvider;
import org.talend.core.runtime.repository.build.BuildExportManager;
import org.talend.core.runtime.repository.build.IBuildExportHandler;
import org.talend.core.runtime.repository.build.IBuildJobParameters;
import org.talend.core.runtime.repository.build.IBuildParametes;
import org.talend.designer.core.model.utils.emf.talendfile.NodeType;
import org.talend.repository.constants.BuildJobConstants;
import org.talend.repository.ui.wizards.exportjob.JavaJobScriptsExportWSWizardPage.JobExportType;
import org.talend.repository.ui.wizards.exportjob.handler.BuildJobHandler;
import org.talend.repository.ui.wizards.exportjob.scriptsmanager.JobScriptsManager.ExportChoice;

/**
 * created by ycbai on 2015年5月15日 Detailled comment
 *
 */
public class BuildJobFactory {

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
    public static IBuildJobHandler createBuildJobHandler(Item processItem, String contextName, String version,
            Map<ExportChoice, Object> exportChoiceMap, JobExportType jobExportType) {

        if (jobExportType != null) {
            switch (jobExportType) {
            case POJO:
                break; // continue
            case OSGI:
                break; // continue
            case MSESB:
                break; // continue
            default:
                jobExportType = null; // try the first one by default
                break;
            }
        }

        String buildType = null;
        if (jobExportType != null) {
            final String newType = BuildJobConstants.oldBuildTypeMap.get(jobExportType);

            if (newType == null) {// not valid type
                return null;
            }
            buildType = newType;
        }

        IBuildJobHandler buildJobHandler = createBuildJobHandler(processItem, contextName, version, exportChoiceMap, buildType);
        if (buildJobHandler == null && processItem instanceof ProcessItem) {
            // default
            buildJobHandler = new BuildJobHandler((ProcessItem)processItem, version, contextName, exportChoiceMap);
        }
        return buildJobHandler;
    }

    public static IBuildJobHandler createBuildJobHandler(Item processItem, String contextName, String version,
            Map<ExportChoice, Object> exportChoiceMap, String buildType) {

        // if null, will try to find the type from item for build type.
        if (StringUtils.isEmpty(buildType)) {
            Object type = processItem.getProperty().getAdditionalProperties()
                    .get(TalendProcessArgumentConstant.ARG_BUILD_TYPE);
            boolean esb = false;

            if (processItem instanceof ProcessItem) {

                ERepositoryObjectType repositoryObjectType = ERepositoryObjectType.getItemType(processItem);
                if (repositoryObjectType == ERepositoryObjectType.PROCESS_ROUTE && "ROUTE_MICROSERVICE".equals(type)) {
                    esb = true;
                } else if ("REST_MS".equals(type)) {
                    esb = true;
                }else {
                    for (Object o : ((ProcessItem) processItem).getProcess().getNode()) {
                        if (o instanceof NodeType) {
                            NodeType currentNode = (NodeType) o;
                            if (BuildJobConstants.esbComponents.contains(currentNode.getComponentName())) {
                                esb = true;
                                if (type == null || "STANDALONE".equals(type)) {
                                    type = "OSGI";
                                }
                                break;
                            }
                        }
                    }
                }
            }

            if (type != null) {
                if (!esb) {
                    buildType = null;
                } else {
                    buildType = type.toString();
                }

            } // else{ // if didn't set, should use default provider to create it.
        }

        Map<String, Object> parameters = new HashMap<>();
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

    public static IBuildJobHandler createBuildJobHandler(Item processItem, String contextName, String version,
            Map<ExportChoice, Object> exportChoiceMap) {
        // according to the export type from additional properties setting.
        return createBuildJobHandler(processItem, contextName, version, exportChoiceMap, (String) null);
    }

}
