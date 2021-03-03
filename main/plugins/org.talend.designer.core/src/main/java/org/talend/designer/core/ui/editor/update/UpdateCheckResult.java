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
package org.talend.designer.core.ui.editor.update;

import java.beans.PropertyChangeEvent;
import java.util.Collection;
import java.util.List;

import org.talend.core.GlobalServiceRegister;
import org.talend.core.PluginChecker;
import org.talend.core.model.process.EComponentCategory;
import org.talend.core.model.process.IContext;
import org.talend.core.model.process.INode;
import org.talend.core.model.process.IProcess;
import org.talend.core.model.process.IProcess2;
import org.talend.core.model.properties.Item;
import org.talend.core.model.properties.JobletProcessItem;
import org.talend.core.model.properties.Property;
import org.talend.core.model.update.EUpdateItemType;
import org.talend.core.model.update.EUpdateResult;
import org.talend.core.model.update.IUpdateItemType;
import org.talend.core.model.update.RepositoryUpdateManager;
import org.talend.core.model.update.UpdateManagerHelper;
import org.talend.core.model.update.UpdateResult;
import org.talend.core.model.update.UpdatesConstants;
import org.talend.core.model.update.extension.UpdateManagerProviderDetector;
import org.talend.core.service.IEBCDICProviderService;
import org.talend.core.service.IMRProcessService;
import org.talend.core.service.IStormProcessService;
import org.talend.core.ui.ISparkJobletProviderService;
import org.talend.core.ui.ISparkStreamingJobletProviderService;
import org.talend.designer.core.model.components.EParameterName;
import org.talend.designer.core.model.process.AbstractProcessProvider;
import org.talend.designer.core.model.utils.emf.talendfile.ElementParameterType;
import org.talend.designer.core.model.utils.emf.talendfile.NodeType;
import org.talend.designer.core.ui.editor.nodes.Node;
import org.talend.designer.core.ui.views.jobsettings.JobSettingsView;

/**
 * ggu class global comment. Detailled comment
 */
public class UpdateCheckResult extends UpdateResult {

    /**
     * ggu UpdateCheckResult constructor comment.
     *
     * @param item
     */
    public UpdateCheckResult(Object item) {
        super(item);
    }

    @Override
    @SuppressWarnings("unchecked")
    public String getName() {
        IUpdateItemType updateType = getUpdateType();
        String displayName = updateType.getDisplayLabel();
        if (updateType instanceof EUpdateItemType) {
            switch ((EUpdateItemType) updateType) {
            case NODE_SCHEMA:
                if (getResultType() == EUpdateResult.RENAME) {
                    List<Object> param = (List<Object>) getParameter();
                    String[] oldSourceIdAndChildName = UpdateManagerUtils.getSourceIdAndChildName((String) param.get(1));
                    String[] newSourceIdAndChildName = UpdateManagerUtils.getSourceIdAndChildName((String) param.get(2));

                    String display = UpdateManagerHelper
                            .getRenamedDisplay(oldSourceIdAndChildName[1], newSourceIdAndChildName[1]);
                    if (display != null) {
                        displayName = display;
                    }
                } else if (getResultType() == EUpdateResult.DELETE) { // table delete by deselect
                    List<Object> param = (List<Object>) getParameter();
                    String tableDeleted = (String) param.get(0);
                    EUpdateResult status = (EUpdateResult) param.get(1);
                    String display = getDeleteOrReloadDisplay(tableDeleted, status);
                    if (display != null) {
                        displayName = display;
                    }
                } else if (getResultType() == EUpdateResult.RELOAD) { // table reload by deselect and reselect
                    List<Object> param = (List<Object>) getParameter();
                    String tableReload = (String) param.get(0);
                    EUpdateResult status = (EUpdateResult) param.get(1);
                    String display = getDeleteOrReloadDisplay(tableReload, status);
                    if (display != null) {
                        displayName = display;
                    }
                } else {
                    if (getUpdateObject() instanceof INode && getParameter() instanceof List
                            && PluginChecker.isEBCDICPluginLoaded()) {
                        IEBCDICProviderService service = (IEBCDICProviderService) GlobalServiceRegister.getDefault().getService(
                                IEBCDICProviderService.class);
                        if (service != null && service.isEbcdicNode((INode) getUpdateObject())) {
                            List<Object> paramObjs = (List<Object>) getParameter();
                            if (paramObjs.size() >= 2) {
                                Object schemaName = paramObjs.get(1);
                                if (schemaName instanceof String) {
                                    displayName = displayName + UpdateManagerUtils.addBrackets((String) schemaName);
                                }
                            }
                        }
                    }
                }
                break;
            case NODE_PROPERTY:
            case NODE_QUERY:
            case JOBLET_SCHEMA:
            case NODE_VALIDATION_RULE:
                break;
            case JOB_PROPERTY_EXTRA:
                displayName = displayName + UpdateManagerUtils.addBrackets(EComponentCategory.EXTRA.getTitle());
                break;
            case JOB_PROPERTY_STATS_LOGS:
                displayName = displayName + UpdateManagerUtils.addBrackets(EComponentCategory.STATSANDLOGS.getTitle());
                break;
            case JOB_PROPERTY_HEADERFOOTER:
                displayName = displayName + UpdateManagerUtils.addBrackets(EComponentCategory.HEADERFOOTER.getTitle());
                break;
            case CONTEXT_GROUP:
                if (getUpdateObject() != null && getUpdateObject() instanceof IContext) {
                    displayName = ((IContext) getUpdateObject()).getName();
                }
                break;
            case CONTEXT:
            // case JOBLET_CONTEXT:
            {
                String display = null;
                switch (getResultType()) {
                case RENAME:
                    List<Object> param = (List<Object>) getParameter();
                    display = UpdateManagerHelper.getRenamedDisplay((String) param.get(1), (String) param.get(2));
                    break;
                default:
                    if (getUpdateObject() instanceof Collection) {
                        display = UpdateManagerHelper.getCollectionsDisplay(getUpdateObject(), false);
                    }
                    break;
                }

                if (display != null) {
                    displayName = display;
                }
                break;
            }
            case JOBLET_RENAMED: {
                List<Object> param = (List<Object>) getParameter();
                String display = UpdateManagerHelper.getRenamedDisplay((String) param.get(1), (String) param.get(2));
                if (display != null) {
                    displayName = display;
                }
                break;
            }
            case RELOAD:
            case JUNIT_RELOAD:
                if (getParameter() != null && getParameter() instanceof PropertyChangeEvent) {
                    PropertyChangeEvent event = (PropertyChangeEvent) getParameter();
                    // reload all compoennts.
                    if (event.getSource() != null && !(event.getSource() instanceof IProcess)) {
                        displayName = updateType.getDisplayLabel();
                        break;
                    }
                }
                break;
            default:
            }
            return displayName;
        } else {
            return UpdateManagerProviderDetector.INSTANCE.getResultName(this);
        }

    }

    private String getDeleteOrReloadDisplay(final String tableName, EUpdateResult status) {
        if (tableName == null) {
            return null;
        }
        return tableName + UpdatesConstants.RENAME_SIGN + status.toString();
    }

    @Override
    @SuppressWarnings("unchecked")
    public String getCategory() {
        String category = null;
        if (getUpdateType() instanceof EUpdateItemType) {
            switch ((EUpdateItemType) getUpdateType()) {
            case NODE_PROPERTY:
            case NODE_SCHEMA:
            case NODE_QUERY:
            case NODE_VALIDATION_RULE:
            case JOBLET_SCHEMA:
                if (getUpdateObject() != null) {
                    if (getUpdateObject() instanceof Node) {
                        Node node = (Node) getUpdateObject();
                        if (node.getUniqueName().equals(node.getLabel())) {
                            category = node.getUniqueName();
                        } else {
                            category = node.getLabel() + UpdateManagerUtils.addBrackets(node.getUniqueName());
                        }
                    }
                    if (getUpdateObject() instanceof NodeType) {
                        NodeType node = (NodeType) getUpdateObject();
                        String uniqueName = null;
                        for (ElementParameterType param : (List<ElementParameterType>) node.getElementParameter()) {
                            if (EParameterName.UNIQUE_NAME.getName().equals(param.getName())) {
                                uniqueName = param.getValue();
                                break;
                            }

                        }
                        if (uniqueName != null) {
                            category = uniqueName;
                        }
                    }
                }
                break;
            case JOB_PROPERTY_EXTRA:
            case JOB_PROPERTY_STATS_LOGS:
            case JOB_PROPERTY_HEADERFOOTER:
            case JOB_PROPERTY_MAPREDUCE:
                boolean isJoblet = false;
                boolean isMR = false;
                if (getUpdateObject() != null) {
                    if (getUpdateObject() instanceof org.talend.designer.core.ui.editor.process.Process) {
                        if (AbstractProcessProvider.isExtensionProcessForJoblet((IProcess) getUpdateObject())) {
                            isJoblet = true;
                        } else if (GlobalServiceRegister.getDefault().isServiceRegistered(IMRProcessService.class)) {
                            IMRProcessService mrProcessService = (IMRProcessService) GlobalServiceRegister.getDefault()
                                    .getService(IMRProcessService.class);
                            org.talend.core.model.properties.Item item = ((org.talend.designer.core.ui.editor.process.Process) getUpdateObject())
                                    .getProperty().getItem();
                            isMR = mrProcessService.isMapReduceItem(item);
                        }
                    }
                }
                if (isMR) {
                    category = JobSettingsView.VIEW_NAME_BATCH;//
                } else if (isJoblet) {
                    category = JobSettingsView.VIEW_NAME_JOBLET; // joblet
                } else {
                    category = JobSettingsView.getViewNameLable();
                }
                break;
            case JOB_PROPERTY_STORM:
                boolean isStreaming = false;
                if (getUpdateObject() != null && getUpdateObject() instanceof org.talend.designer.core.ui.editor.process.Process) {
                    if (GlobalServiceRegister.getDefault().isServiceRegistered(IStormProcessService.class)) {
                        IStormProcessService streamingService = (IStormProcessService) GlobalServiceRegister.getDefault()
                                .getService(IStormProcessService.class);
                        org.talend.core.model.properties.Item item = ((org.talend.designer.core.ui.editor.process.Process) getUpdateObject())
                                .getProperty().getItem();
                        isStreaming = streamingService.isStormItem(item);
                    }
                }
                if (isStreaming) {
                    category = JobSettingsView.VIEW_NAME_STREAMING;//
                } else {
                    category = JobSettingsView.getViewNameLable();
                }
                break;
            case CONTEXT:
                // case JOBLET_CONTEXT:
                category = UpdatesConstants.CONTEXT;
                break;
            case CONTEXT_GROUP:
                category = UpdatesConstants.CONTEXT_GROUP;
                break;
            case JOBLET_RENAMED:
            case RELOAD:
            case JUNIT_RELOAD:
                if (getUpdateObject() != null && getUpdateObject() instanceof List) {
                    String display = UpdateManagerHelper.getCollectionsDisplay(getUpdateObject(), true);
                    if (display != null) {
                        category = display;
                    }
                } else if (getParameter() != null && getParameter() instanceof PropertyChangeEvent) {
                    PropertyChangeEvent event = (PropertyChangeEvent) getParameter();
                    // reload all compoennts.
                    if (event.getSource() != null && !(event.getSource() instanceof IProcess)) {
                        category = UpdatesConstants.COMPONENT;
                        break;
                    }
                } else {
                    category = UpdatesConstants.JOBLET;
                }
                break;
            default:
            }
        } else {
            category = UpdateManagerProviderDetector.INSTANCE.getDisplayCategory(this);
        }

        return category == null ? UpdatesConstants.EMPTY : category;
    }

    @Override
    public String getJobInfor() {
        return this.jobInfor;
    }

    @Override
    protected void updateJobInfor() {
        if (getJob() != null) {
            String jobInfor = null;
            if (getJob() instanceof IProcess2) {
                Property property = ((IProcess2) getJob()).getProperty();
                jobInfor = RepositoryUpdateManager.getUpdateJobInfor(property);
                org.talend.core.model.properties.Item item = property.getItem();
                if (item instanceof JobletProcessItem) {
                    handleJoblet(item);
                } else if (GlobalServiceRegister.getDefault().isServiceRegistered(IMRProcessService.class)) {
                    IMRProcessService mrProcessService = (IMRProcessService) GlobalServiceRegister.getDefault().getService(
                            IMRProcessService.class);
                    isMR = mrProcessService.isMapReduceItem(item);
                }
                if (GlobalServiceRegister.getDefault().isServiceRegistered(IStormProcessService.class)) {
                    IStormProcessService streamingService = (IStormProcessService) GlobalServiceRegister.getDefault().getService(
                            IStormProcessService.class);
                    isStreaming = streamingService.isStormItem(item);
                }
            }
            if (getJob() instanceof org.talend.core.model.properties.Item) {
                jobInfor = RepositoryUpdateManager.getUpdateJobInfor(((org.talend.core.model.properties.Item) getJob())
                        .getProperty());
                if (getJob() instanceof JobletProcessItem) {
                    handleJoblet((JobletProcessItem)getJob());
                } else if (GlobalServiceRegister.getDefault().isServiceRegistered(IMRProcessService.class)) {
                    IMRProcessService mrProcessService = (IMRProcessService) GlobalServiceRegister.getDefault().getService(
                            IMRProcessService.class);
                    isMR = mrProcessService.isMapReduceItem((org.talend.core.model.properties.Item) getJob());
                }
                if (GlobalServiceRegister.getDefault().isServiceRegistered(IStormProcessService.class)) {
                    IStormProcessService streamingService = (IStormProcessService) GlobalServiceRegister.getDefault().getService(
                            IStormProcessService.class);
                    isStreaming = streamingService.isStormItem((org.talend.core.model.properties.Item) getJob());
                }
            }
            String others = null;
            if (isFromItem()) { // update item
                others = UpdatesConstants.START;
            }
            if (jobInfor != null) {
                this.jobInfor = jobInfor + UpdatesConstants.SPACE + UpdateManagerUtils.addBrackets(others);
                return;
            }
        }

    }

    private void handleJoblet(Item item){
        boolean isSpark = false;
        boolean isSparkStreaming = false;
        if (GlobalServiceRegister.getDefault().isServiceRegistered(ISparkJobletProviderService.class)) {
            ISparkJobletProviderService sparkJobletService = (ISparkJobletProviderService) GlobalServiceRegister
                    .getDefault().getService(ISparkJobletProviderService.class);
            if (sparkJobletService != null && sparkJobletService.isSparkJobletItem(item)) {
                isSpark = true;
            }
        }
        if (GlobalServiceRegister.getDefault().isServiceRegistered(ISparkStreamingJobletProviderService.class)) {
            ISparkStreamingJobletProviderService sparkStreamingJobletService = (ISparkStreamingJobletProviderService) GlobalServiceRegister
                    .getDefault().getService(ISparkStreamingJobletProviderService.class);
            if (sparkStreamingJobletService != null && sparkStreamingJobletService.isSparkStreamingJobletItem(item)) {
                isSparkStreaming = true;
            }
        }
        if(isSpark){
            isSparkJoblet = true;
        }else if(isSparkStreaming){
            isSparkStreamingJoblet = true;
        }else{
            isJoblet = true;
        }
    }

}
