// ============================================================================
//
// Copyright (C) 2006-2019 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.designer.core.ui.editor.cmd;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.eclipse.gef.commands.Command;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.talend.commons.ui.gmf.util.DisplayUtils;
import org.talend.commons.utils.threading.ExecutionLimiter;
import org.talend.commons.utils.threading.ExecutionLimiterImproved;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.PluginChecker;
import org.talend.core.model.components.EComponentType;
import org.talend.core.model.components.IComponent;
import org.talend.core.model.metadata.IMetadataColumn;
import org.talend.core.model.metadata.IMetadataTable;
import org.talend.core.model.process.EParameterFieldType;
import org.talend.core.model.process.IConnection;
import org.talend.core.model.process.IContext;
import org.talend.core.model.process.IElement;
import org.talend.core.model.process.IElementParameter;
import org.talend.core.model.process.IElementParameterDefaultValue;
import org.talend.core.model.process.INode;
import org.talend.core.model.process.IProcess;
import org.talend.core.model.process.IProcess2;
import org.talend.core.model.process.ProcessUtils;
import org.talend.core.model.properties.ProcessItem;
import org.talend.core.model.properties.Project;
import org.talend.core.model.properties.Property;
import org.talend.core.model.update.UpdatesConstants;
import org.talend.core.ui.IJobletProviderService;
import org.talend.core.ui.process.IGraphicalNode;
import org.talend.core.utils.TalendQuoteUtils;
import org.talend.designer.core.IDbMapDesignerService;
import org.talend.designer.core.i18n.Messages;
import org.talend.designer.core.model.components.EParameterName;
import org.talend.designer.core.model.components.EmfComponent;
import org.talend.designer.core.model.components.Expression;
import org.talend.designer.core.model.process.jobsettings.JobSettingsConstants;
import org.talend.designer.core.ui.editor.connections.Connection;
import org.talend.designer.core.ui.editor.nodes.Node;
import org.talend.designer.core.ui.views.CodeView;
import org.talend.designer.core.ui.views.jobsettings.JobSettings;
import org.talend.designer.core.ui.views.properties.ComponentSettings;
import org.talend.designer.core.utils.DesignerUtilities;
import org.talend.designer.core.utils.JobSettingVersionUtil;
import org.talend.designer.core.utils.ValidationRulesUtil;
import org.talend.designer.runprocess.ItemCacheManager;
import org.talend.repository.ProjectManager;

/**
 * Command that changes a given property. It will call the set or get property value in an element. This element can be
 * either a node, a connection or a process. <br/>
 *
 * $Id$
 *
 */
public class PropertyChangeCommand extends Command {

    private final IElement elem;

    private final String propName;

    private Object newValue;

    private Object oldValue;

    private boolean repositoryValueWasUsed;

    private boolean toUpdate;

    private final Map<IElementParameter, Object> oldElementValues;

    // private ChangeMetadataCommand changeMetadataCommand;
    private List<ChangeMetadataCommand> changeMetadataCommands;

    private Command updateELTMapComponentCommand;

    private String propertyTypeName;

    private final String updataComponentParamName;

    private static ExecutionLimiter codeViewUpdater = new ExecutionLimiter(500, true) {

        @Override
        protected void execute(boolean isFinalExecution, Object data) {
            if (isFinalExecution && data instanceof IElement) {
                CodeView.refreshCodeView((IElement) data);
            }
        }
    };

    // for bug TDI-32692
    private static ExecutionLimiterImproved checkProcess = new ExecutionLimiterImproved(500, true) {

        @Override
        protected void execute(boolean isFinalExecution, final Object data) {
            if (isFinalExecution) {
                Display.getDefault().asyncExec(new Runnable() {

                    @Override
                    public void run() {
                        if (data instanceof IGraphicalNode) {
                            ((IGraphicalNode) data).checkAndRefreshNode();
                        }
                        if (data instanceof IConnection) {
                            IProcess process = ((IConnection) data).getSource().getProcess();
                            if (process instanceof IProcess2) {
                                ((IProcess2) process).checkProcess();
                            }
                        }
                        if (data instanceof IProcess) {
                            IProcess process = (IProcess) data;
                            List<? extends INode> sparkConfigList = process.getNodesOfType("tSparkConfiguration"); //$NON-NLS-1$
                            if (sparkConfigList != null && sparkConfigList.size() > 0) {
                                // The process is a Spark or a Spark Streaming Process
                                ((IProcess2) process).checkProcess();
                            }
                        }
                    }
                });
            }
        }
    };

    /**
     * The property is defined in an element, which can be either a node or a connection.
     *
     * @param elem
     * @param propName
     * @param propValue
     */
    public PropertyChangeCommand(IElement elem, String propName, Object propValue) {
        this.elem = elem;
        this.propName = propName;
        newValue = propValue;
        toUpdate = false;
        oldElementValues = new HashMap<IElementParameter, Object>();
        changeMetadataCommands = new ArrayList<ChangeMetadataCommand>();
        setLabel(Messages.getString("PropertyChangeCommand.Label")); //$NON-NLS-1$
        // for job settings extra (feature 2710)
        // if (JobSettingsConstants.isExtraParameter(propName) ||
        // propName.equals(EParameterName.IMPLICIT_TCONTEXTLOAD.getName())) {
        // propertyTypeName = JobSettingsConstants.getExtraParameterName(EParameterName.PROPERTY_TYPE.getName());
        // updataComponentParamName =
        // JobSettingsConstants.getExtraParameterName(EParameterName.UPDATE_COMPONENTS.getName());
        // } else {

        IElementParameter currentParam = elem.getElementParameter(propName);
        propertyTypeName = EParameterName.PROPERTY_TYPE.getName();
        for (IElementParameter param : elem.getElementParameters()) {
            if (param.getFieldType() != null && param.getFieldType().equals(EParameterFieldType.PROPERTY_TYPE)
                    && param.getCategory().equals(currentParam.getCategory())) {
                propertyTypeName = param.getName() + ":" + EParameterName.PROPERTY_TYPE.getName(); //$NON-NLS-1$
                break;
            }
        }
        updataComponentParamName = EParameterName.UPDATE_COMPONENTS.getName();
        // }
    }

    private List<INode> getRelativeNodes(List<? extends IElementParameter> elementParameters) {
        List<INode> retList = null;
        if (elementParameters == null || elementParameters.size() == 0) {
            return retList;
        }
        IElement element = elementParameters.get(0).getElement();
        if (element instanceof Node) {
            Node operatingNode = (Node) element;
            IProcess process = operatingNode.getProcess();
            if (process == null) {
                return retList;
            }
            List<? extends INode> graphicNodes = process.getGraphicalNodes();
            if (graphicNodes == null || graphicNodes.size() == 0) {
                return retList;
            }
            String nodeName = operatingNode.getLabel();
            retList = new ArrayList<INode>();
            for (INode node : graphicNodes) {
                List<? extends IElementParameter> nodeElementParameters = node.getElementParameters();
                if (nodeElementParameters == null || nodeElementParameters.size() == 0) {
                    continue;
                }
                for (IElementParameter elementParameter : nodeElementParameters) {
                    if (elementParameter.getFieldType() == EParameterFieldType.COMPONENT_LIST) {
                        Object objName = elementParameter.getValue();
                        if (objName == null) {
                            continue;
                        }
                        String relatedComponentsName = objName.toString();
                        if (relatedComponentsName.equals(nodeName)) {
                            retList.add(node);
                        }
                    }
                }
            }
        }
        return retList;
    }

    @Override
    public void execute() {
        IElementParameter currentParam = elem.getElementParameter(propName);
        changeMetadataCommands.clear();
        oldElementValues.clear();
        if (currentParam == null) {
            return;
        }

        if (currentParam.isRepositoryValueUsed()) {
            if (currentParam.getFieldType() == EParameterFieldType.MEMO_SQL) {
                Object queryStoreValue = elem.getPropertyValue(EParameterName.QUERYSTORE_TYPE.getName());
                if (!EmfComponent.BUILTIN.equals(queryStoreValue) || !EmfComponent.TNS_FILE.equals(queryStoreValue)) {
                    elem.setPropertyValue(EParameterName.QUERYSTORE_TYPE.getName(), EmfComponent.BUILTIN);
                }
                currentParam.setRepositoryValueUsed(false);
            } else {
                toUpdate = true;
                Object value = elem.getPropertyValue(propName);
                if (value == null || (!value.toString().endsWith("xsd") && !value.toString().endsWith("xsd\""))) {
                    elem.setPropertyValue(propertyTypeName, EmfComponent.BUILTIN);

                    /**
                     * For tCreateTable, DbType changed need to clean repository connection id stored, or it will get
                     * original DbType repository connection when click to Repository property type from built-in
                     */
                    if ("tCreateTable".equals(elem.getPropertyValue(EParameterName.COMPONENT_NAME.getName()))
                            && "DBTYPE".equals(propName)) {
                        elem.setPropertyValue(EParameterName.REPOSITORY_PROPERTY_TYPE.getName(), "");
                    }
                }
                for (IElementParameter param : elem.getElementParameters()) {
                    if (param.getRepositoryProperty() == null || param.getRepositoryProperty().equals(currentParam.getName())) {
                        param.setRepositoryValueUsed(false);
                    }
                }
            }

            repositoryValueWasUsed = true;
        } else {
            if ("table.main.schema:SCHEMA_TYPE".equals(propName)) {
                elem.getElementParameter("table.tableName").setRepositoryValueUsed(false);
            } else if ("module.main.schema:SCHEMA_TYPE".equals(propName)) {
                elem.getElementParameter("module.moduleName").setRepositoryValueUsed(false);
            }
            repositoryValueWasUsed = false;
        }

        oldValue = elem.getPropertyValue(propName);
        elem.setPropertyValue(propName, newValue);
        if (currentParam.getFieldType().equals(EParameterFieldType.CONNECTION_LIST) && currentParam.getContext() != null
                && (elem instanceof Node)) {
            String connParaname = currentParam.getContext() + ":" + currentParam.getName();
            if (connParaname.equals(propName)) {
                IConnection selectedConn = null;
                for (IConnection conn : ((Node) elem).getIncomingConnections()) {
                    if (conn.getUniqueName().equals(newValue)) {
                        selectedConn = conn;
                        break;
                    }
                }
                if (selectedConn != null && getTakeSchema()) {
                    ((Node) selectedConn.getSource()).takeSchemaFrom((Node) elem, currentParam.getContext());
                }
            }
        }
        if ("ELT_TABLE_NAME".equals(propName) || "ELT_SCHEMA_NAME".equals(propName)) { //$NON-NLS-1$ //$NON-NLS-2$
            String oldELTValue = ""; //$NON-NLS-1$
            String newELTValue = ""; //$NON-NLS-1$
            String oldParamValue = TalendQuoteUtils.removeQuotes((String) oldValue);
            String newParamValue = TalendQuoteUtils.removeQuotes((String) newValue);
            if ("ELT_TABLE_NAME".equals(propName)) { //$NON-NLS-1$
                String schemaName = TalendQuoteUtils.removeQuotes((String) elem.getPropertyValue("ELT_SCHEMA_NAME")); //$NON-NLS-1$
                if (schemaName == null || "".equals(schemaName.trim())) { //$NON-NLS-1$
                    oldELTValue = oldParamValue;
                    newELTValue = newParamValue;
                } else {
                    oldELTValue = schemaName + "." + oldParamValue; //$NON-NLS-1$
                    newELTValue = schemaName + "." + newParamValue; //$NON-NLS-1$
                }
            } else {
                String tableName = TalendQuoteUtils.removeQuotes((String) elem.getPropertyValue("ELT_TABLE_NAME")); //$NON-NLS-1$
                if (oldParamValue != null && !"".equals(oldParamValue.trim())) {
                    oldELTValue = oldParamValue + "."; //$NON-NLS-1$
                }
                if (newParamValue != null && !"".equals(newParamValue.trim())) {
                    newELTValue = newParamValue + "."; //$NON-NLS-1$
                }
                oldELTValue = oldELTValue + tableName; // $NON-NLS-1$
                newELTValue = newELTValue + tableName; // $NON-NLS-1$
            }
            List<? extends IConnection> connections = ((Node) elem).getOutgoingConnections();
            for (IConnection connection : connections) {
                if (!connection.getName().equals(oldELTValue)) {
                    // Update if use custom connection name.
                    ChangeConnTextCommand command = new ChangeConnTextCommand(connection, newELTValue);
                    command.execute();
                    continue;
                }
                INode targetNode = connection.getTarget();
                String componentName = targetNode.getComponent().getName();
                if (componentName.matches("tELT.*Map")) { //$NON-NLS-1$
                    if (GlobalServiceRegister.getDefault().isServiceRegistered(IDbMapDesignerService.class)) {
                        IDbMapDesignerService service = GlobalServiceRegister.getDefault().getService(
                                IDbMapDesignerService.class);
                        updateELTMapComponentCommand = service.getUpdateELTMapComponentCommand(targetNode, connection,
                                oldELTValue, newELTValue);
                        updateELTMapComponentCommand.execute();
                    }
                }
            }
        }

        if ("tCreateTable".equals(elem.getPropertyValue(EParameterName.COMPONENT_NAME.getName())) && "DBTYPE".equals(propName)) {//$NON-NLS-1$ //$NON-NLS-2$
            IElementParameter propertyParam = elem.getElementParameter("PROPERTY");//$NON-NLS-1$
            propertyParam.setRepositoryValue("DATABASE:" + newValue);//$NON-NLS-1$
        }
        // add for bug TDI-26632 by fwang in 11 July, 2013. can't edit parameters if use repository connection.
        IElementParameter propertyTypeParam = elem.getElementParameter(EParameterName.PROPERTY_TYPE.getName());
        IElementParameter repositoryTypeParam = elem.getElementParameter(EParameterName.REPOSITORY_PROPERTY_TYPE.getName());
        if (("USE_EXISTING_CONNECTION").equals(propName) && elem instanceof Node && propertyTypeParam != null
                && "REPOSITORY".equals(propertyTypeParam.getValue()) && repositoryTypeParam != null
                && !("").equals(repositoryTypeParam.getValue())) {
            Node node = (Node) elem;
            for (IElementParameter param : node.getElementParameters()) {
                String repositoryValue = param.getRepositoryValue();
                if ((repositoryValue != null)
                        && (!param.getName().equals(EParameterName.PROPERTY_TYPE.getName()))
                        && param.getFieldType() != EParameterFieldType.MEMO_SQL
                        && !("tMDMReceive".equals(node.getComponent().getName()) && "XPATH_PREFIX".equals(param //$NON-NLS-1$ //$NON-NLS-2$
                                .getRepositoryValue()))
                        && !("tSAPOutput".equals(node.getComponent().getName()) && param.getName().equals(
                                UpdatesConstants.MAPPING))
                        && !("tFileInputEBCDIC".equals(node.getComponent().getName()) && "DATA_FILE".equals(repositoryValue))) {
                    param.setRepositoryValueUsed(true);
                    if (!(EParameterName.DB_VERSION.getName()).equals(param.getName())) {
                        param.setReadOnly(true);
                    }
                }
            }
        }

        // feature 19312
        if (propName.contains(EParameterName.USE_DYNAMIC_JOB.getName()) && newValue.equals(false)) {
            IElementParameter processParam = elem.getElementParameter(EParameterName.PROCESS.getName());
            IElementParameter processTypeParameter = elem.getElementParameter(EParameterName.PROCESS_TYPE_PROCESS.getName());
            final String parentName = processParam.getName() + ":"; //$NON-NLS-1$
            elem.setPropertyValue(parentName + processTypeParameter.getName(), ""); //$NON-NLS-1$
            elem.setPropertyValue(processParam.getName(), ""); //$NON-NLS-1$
        }
        if (propName.contains(EParameterName.PROCESS_TYPE_PROCESS.getName())) {
            boolean isSelectUseDynamic = false;
            IElementParameter useDynamicJobParameter = elem.getElementParameter(EParameterName.USE_DYNAMIC_JOB.getName());
            if (useDynamicJobParameter != null && useDynamicJobParameter instanceof IElementParameter) {
                Object useDynamicJobValue = useDynamicJobParameter.getValue();
                if (useDynamicJobValue != null && useDynamicJobValue instanceof Boolean) {
                    isSelectUseDynamic = (Boolean) useDynamicJobValue;
                }
            }
            if (isSelectUseDynamic) {
                StringBuffer labels = new StringBuffer("");
                if (newValue != null) {
                    String[] strValues = newValue.toString().split(";");
                    for (int i = 0; i < strValues.length; i++) {
                        String strValue = strValues[i];
                        // newValue is the id of the job
                        ProcessItem processItem = ItemCacheManager.getProcessItem(strValue);
                        if (processItem != null) {
                            String label = processItem.getProperty().getLabel();
                            if (i > 0) {
                                labels.append(";");
                            }
                            labels.append(label);
                        }
                    }
                }
                currentParam.getParentParameter().setValue(labels.toString());
            } else {
                // newValue is the id of the job
                ProcessItem processItem = ItemCacheManager.getProcessItem((String) newValue);
                if (processItem != null) {
                    org.talend.core.model.properties.Project project = ProjectManager.getInstance().getProject(processItem.getProperty());
                    currentParam.getParentParameter().setValue(ProcessUtils.getProjectProcessLabel(project.getTechnicalLabel(),processItem.getProperty().getLabel()));
                }
            }
        }
        if (propName.contains(EParameterName.PROCESS_TYPE_VERSION.getName())) {
            // newValue is the id of the job

            // hywang add for feature 6549
            // 1.to see current component if is a jobletComponent by "elem"
            boolean isJobletComponent = false;
            // Node jobletNode = null;
            IJobletProviderService service = null;
            if (PluginChecker.isJobLetPluginLoaded()) {
                service = GlobalServiceRegister.getDefault().getService(IJobletProviderService.class);
            }
            if (elem instanceof Node) {
                // jobletNode = (Node) elem;
                if (service != null) {
                    isJobletComponent = service.isJobletComponent((Node) elem);
                }
            }
            if (isJobletComponent) {
                // 2.if it is a jobletcomponent,reload the component by the version
                Property jobletProperty = service.getJobletComponentItem((Node) elem);
                Project project = ProjectManager.getInstance().getProject(jobletProperty);
                String projTechLabel = null;
                if (project != null) {
                    projTechLabel = project.getTechnicalLabel();
                }
                String id = jobletProperty.getId();
                String version = (String) newValue;
                IComponent newComponent = service.setPropertyForJobletComponent(projTechLabel, id, version);
                reloadNode((Node) elem, newComponent);
            } else {

                IElementParameter processIdParam = currentParam.getParentParameter().getChildParameters()
                        .get(EParameterName.PROCESS_TYPE_PROCESS.getName());
                ProcessItem processItem = ItemCacheManager.getProcessItem((String) processIdParam.getValue(), (String) newValue);
                if (processItem != null) {
                    org.talend.core.model.properties.Project project = ProjectManager.getInstance().getProject(processItem.getProperty());
                    currentParam.getParentParameter().setValue(ProcessUtils.getProjectProcessLabel(project.getTechnicalLabel(),processItem.getProperty().getLabel()));
                }
            }

        }
        if (propName.contains(EParameterName.PROCESS_TYPE_CONTEXT.getName())) {
            if (elem instanceof Node) {
                Node node = (Node) elem;
                List<IContext> listContext = node.getProcess().getContextManager().getListContext();
                List<String> values = new ArrayList<String>();
                for (IContext context : listContext) {
                    values.add(context.getName());
                }
                currentParam.setListItemsDisplayName(values.toArray(new String[0]));
                currentParam.setListItemsValue(values.toArray(new String[0]));
                currentParam.setValue(newValue);
            }

        }
        if (propName.equals(EParameterName.VALIDATION_RULES.getName())) {
            if (elem instanceof INode) {
                ValidationRulesUtil.createRejectConnector((INode) elem);
                ValidationRulesUtil.updateRejectMetatable((INode) elem, null);
                if (newValue != null && (!(Boolean) newValue)) {
                    ValidationRulesUtil.removeRejectConnector((INode) elem);
                    ValidationRulesUtil.removeRejectConnection((INode) elem);
                }
            }
        }
        String dbType = "";
        if (newValue instanceof String) {
            dbType = (String) newValue;
        }
        IElementParameter schemaParameter = null;
        if (propName.equals(EParameterName.DB_TYPE.getName())) {
            IElementParameter elementParameter = elem.getElementParameter(EParameterName.DB_VERSION.getName());
            schemaParameter = elem.getElementParameter(EParameterName.SCHEMA_DB.getName());
            JobSettingVersionUtil.setDbVersion(elementParameter, dbType, true);
            DesignerUtilities.setSchemaDB(schemaParameter, newValue);
        } else if (propName.equals(JobSettingsConstants.getExtraParameterName(EParameterName.DB_TYPE.getName()))) {
            IElementParameter elementParameter = elem.getElementParameter(JobSettingsConstants
                    .getExtraParameterName(EParameterName.DB_VERSION.getName()));
            schemaParameter = elem.getElementParameter(JobSettingsConstants.getExtraParameterName(EParameterName.SCHEMA_DB
                    .getName()));
            JobSettingVersionUtil.setDbVersion(elementParameter, dbType, true);
            DesignerUtilities.setSchemaDB(schemaParameter, newValue);
        }
        // Some DB not need fill the schema parameter for the JobSetting View "Extra" ,"Stats&Logs"
        if (schemaParameter != null && !schemaParameter.isShow(elem.getElementParameters())
                && !schemaParameter.getValue().equals("")) {
            schemaParameter.setValue("");
        }
        if (!toUpdate
                && (currentParam.getFieldType().equals(EParameterFieldType.RADIO)
                        || currentParam.getFieldType().equals(EParameterFieldType.CLOSED_LIST)
                        || currentParam.getFieldType().equals(EParameterFieldType.OPENED_LIST)
                        || currentParam.getFieldType().equals(EParameterFieldType.CHECK)
                        || currentParam.getFieldType().equals(EParameterFieldType.AS400_CHECK) || currentParam.getFieldType()
                        .equals(EParameterFieldType.COMPONENT_LIST))) {
            toUpdate = false;
            setDefaultValues(currentParam, elem);
        }

        if (currentParam.getName().equals(EParameterName.PROCESS_TYPE_PROCESS.getName())) {
            toUpdate = true;
        }
        if (currentParam.getName().equals(EParameterName.MAPPING.getName())) {
            toUpdate = true;
        }
        // TUP-18405, need update module list
        if (currentParam.getFieldType() == EParameterFieldType.MODULE_LIST) {
            toUpdate = true;
        }

        if (toUpdate) {
            elem.setPropertyValue(updataComponentParamName, Boolean.TRUE);
        }
        // see bug 9151:100% CPU when typing text.
        if (getNewValue() instanceof String && elem instanceof INode) {
            INode curNode = (INode) elem;
            String uniqueName = curNode.getUniqueName();
            IProcess process = curNode.getProcess();
            if (process != null && process instanceof IProcess2) {
                IProcess2 process2 = (IProcess2) process;
                List<? extends INode> generatingNodes = null;
                if (process2.isProcessModified()) {
                    process2.setProcessModified(false);
                    generatingNodes = process2.getGeneratingNodes();
                    if (generatingNodes != null) {
                        for (INode genNode : new ArrayList<INode>(generatingNodes)) {
                            if (genNode.getUniqueName().equals(uniqueName)) {
                                IElementParameter genParam = genNode.getElementParameter(propName);
                                if (genParam != null) {
                                    genParam.setValue(newValue);
                                    break;
                                }
                            }
                        }
                    }

                    process2.setProcessModified(true);
                    codeViewUpdater.startIfExecutable(elem);
                }
            }
        }
        updateRelativeNodesIfNeeded(currentParam);

        checkProcess.startIfExecutable(elem);

        // See feature 3902
        if (needUpdateMonitorConnection()) {
            ((Connection) elem).setMonitorConnection((Boolean) currentParam.getValue());
        }
        refreshMR(propName);
    }

    /**
     * DOC cmeng Comment method "updateRelativeNodesIfNeeded".
     *
     * @param currentParam
     */
    private void updateRelativeNodesIfNeeded(IElementParameter currentParam) {
        boolean originalUpdateValue = toUpdate;
        List<INode> relativeNodes = getRelativeNodes(elem.getElementParameters());
        if (relativeNodes != null && 0 < relativeNodes.size()) {
            for (INode node : relativeNodes) {
                toUpdate = false;
                setDefaultValues(currentParam, node);
                if (toUpdate) {
                    node.setPropertyValue(updataComponentParamName, new Boolean(true));
                }
            }
        }
        toUpdate = originalUpdateValue;
    }

    /**
     * DOC cmeng Comment method "setDefaultValues".
     *
     * @param currentParam
     */
    private void setDefaultValues(IElementParameter currentParam, IElement node) {
        List<? extends IElementParameter> elementParameters = node.getElementParameters();
        if (elementParameters == null) {
            return;
        }
        for (int i = 0; i < elementParameters.size(); i++) {
            IElementParameter testedParam = elementParameters.get(i);

            String showIf = testedParam.getShowIf();
            String notShowIf = testedParam.getNotShowIf();

            if (showIf != null) {
                if (showIf.contains(currentParam.getName())) {
                    toUpdate = true;
                }
            } else {
                if (notShowIf != null) {
                    if (notShowIf.contains(currentParam.getName())) {
                        toUpdate = true;
                    }
                }
            }
            String[] listItemsShowIf = testedParam.getListItemsShowIf();
            if (listItemsShowIf != null && listItemsShowIf.length > 0) {
                for (String itemShowIf : listItemsShowIf) {
                    if (itemShowIf != null && itemShowIf.contains(currentParam.getName())) {
                        toUpdate = true;
                        break;
                    }
                }
            }
            String[] listItemsNotShowIf = testedParam.getListItemsNotShowIf();
            if (listItemsNotShowIf != null && listItemsNotShowIf.length > 0) {
                for (String itemNotShowIf : listItemsNotShowIf) {
                    if (itemNotShowIf != null && itemNotShowIf.contains(currentParam.getName())) {
                        toUpdate = true;
                        break;
                    }
                }
            }
            if (testedParam.getFieldType() == EParameterFieldType.TABLE) {
                String[] tmpShowIfs = testedParam.getListItemsShowIf();
                if (tmpShowIfs != null) {
                    for (String show : tmpShowIfs) {
                        if (show != null && show.contains(currentParam.getName())) {
                            toUpdate = true;
                        }
                    }
                }
                tmpShowIfs = testedParam.getListItemsNotShowIf();
                if (tmpShowIfs != null) {
                    for (String show : tmpShowIfs) {
                        if (show != null && show.contains(currentParam.getName())) {
                            toUpdate = true;
                        }
                    }
                }
            }
            if (currentParam.getFieldType().equals(EParameterFieldType.CLOSED_LIST)) {
                /*
                 * TUP-968, In order to refresh for missing modules top messages.
                 */
                if (EParameterName.DB_VERSION.getName().equals(currentParam.getName())
                        || "HBASE_VERSION".equals(currentParam.getName()) //$NON-NLS-1$
                        || "HIVE_VERSION".equals(currentParam.getName()) //$NON-NLS-1$
                        || "HCAT_VERSION".equals(currentParam.getName()) //$NON-NLS-1$
                        || "DISTRIBUTION".equals(currentParam.getName())) {//$NON-NLS-1$
                    toUpdate = true;
                } else if (testedParam.getListItemsShowIf() != null) {
                    for (int j = 0; j < testedParam.getListItemsShowIf().length && !toUpdate; j++) {
                        showIf = testedParam.getListItemsShowIf()[j];
                        notShowIf = testedParam.getListItemsNotShowIf()[j];
                        if (showIf != null) {
                            if (showIf.contains(currentParam.getName())) {
                                toUpdate = true;
                            }
                        } else {
                            if (notShowIf != null) {
                                if (notShowIf.contains(currentParam.getName())) {
                                    toUpdate = true;
                                }
                            }
                        }
                    }
                }
            }
            setDefaultValues(currentParam, testedParam, node);
        }
    }

    private boolean needUpdateMonitorConnection() {

        if (elem instanceof Connection) {
            if (propName.equals(EParameterName.MONITOR_CONNECTION.getName())) {
                return true;
            }
        }

        return false;
    }

    private boolean needClearOldColumns() {
        if (elem instanceof Node) {
            if (propName.equals("ACTION")) {
                return true;
            }
        }

        return false;
    }

    public void setUpdate(boolean update) {
        toUpdate = update;
    }

    /**
     * Set the values to default if needed.
     *
     * @param currentParam Current parameter that has been modified in the interface
     * @param testedParam Tested parameter, to know if there is a link for the default values between this parameter and
     * the current.
     */
    private void setDefaultValues(IElementParameter currentParam, IElementParameter testedParam, IElement referenceNode) {
        List<? extends IElementParameter> elementParameters = referenceNode.getElementParameters();
        if (elementParameters == null) {
            return;
        }
        boolean contains = false;
        // zli
        for (IElementParameterDefaultValue value : testedParam.getDefaultValues()) {
            if (value.getIfCondition() != null) {
                if (value.getIfCondition().contains(currentParam.getName())) {
                    contains = true;
                    break;
                }
            }
            if (value.getNotIfCondition() != null) {
                if (value.getNotIfCondition().contains(currentParam.getName())) {
                    contains = true;
                    break;
                }
            }
        }
        if (!contains && testedParam.getFieldType().equals(EParameterFieldType.CLOSED_LIST)) {
            // check if current parameter for combo is valid for new parameters value
            // if not, it will try to choose another value from combo box list.
            boolean isCurrentComboValid = true;
            if (testedParam.getListItemsShowIf() != null || testedParam.getListItemsNotShowIf() != null) {
                String value = (String) testedParam.getValue();
                int index = ArrayUtils.indexOf(testedParam.getListItemsValue(), value);
                // TUP-671:if find this testParam's value,just do show if
                if (index != -1) {
                    if (testedParam.getListItemsShowIf() != null) {
                        String conditionShowIf = testedParam.getListItemsShowIf()[index];
                        if (conditionShowIf != null) {
                            isCurrentComboValid = Expression.evaluate(conditionShowIf, elementParameters);
                        }
                    }
                    if (testedParam.getListItemsNotShowIf() != null) {
                        String conditionNotShowIf = testedParam.getListItemsNotShowIf()[index];
                        if (conditionNotShowIf != null) {
                            isCurrentComboValid = !Expression.evaluate(conditionNotShowIf, elementParameters);
                        }
                    }
                }
            }
            if (!isCurrentComboValid && testedParam.getListItemsShowIf() != null) {
                for (String condition : testedParam.getListItemsShowIf()) {
                    if (condition != null && condition.contains(currentParam.getName())) {
                        boolean isValid = Expression.evaluate(condition, elementParameters);
                        if (isValid) {
                            int index = ArrayUtils.indexOf(testedParam.getListItemsShowIf(), condition);
                            testedParam.setValue(testedParam.getListItemsValue()[index]);
                            isCurrentComboValid = true;
                            break;
                        }
                    }
                }
            }
            if (!isCurrentComboValid && !contains && testedParam.getListItemsNotShowIf() != null) {
                for (String condition : testedParam.getListItemsNotShowIf()) {
                    if (condition != null && condition.contains(currentParam.getName())) {
                        boolean isValid = !Expression.evaluate(condition, elementParameters);
                        if (isValid) {
                            int index = ArrayUtils.indexOf(testedParam.getListItemsNotShowIf(), condition);
                            testedParam.setValue(testedParam.getListItemsValue()[index]);
                            break;
                        }
                    } else if (condition == null) {
                        int index = ArrayUtils.indexOf(testedParam.getListItemsNotShowIf(), condition);
                        testedParam.setValue(testedParam.getListItemsValue()[index]);
                        break;
                    }
                }
            }
        } else

        if (testedParam.getDefaultValues().size() > 0 && contains) {
            oldElementValues.put(testedParam, testedParam.getValue());

            // if the field is not a schema type, then use standard "set value".
            if (!(testedParam.getFieldType().equals(EParameterFieldType.SCHEMA_TYPE) || testedParam.getFieldType().equals(
                    EParameterFieldType.SCHEMA_REFERENCE))) {
                String oldValue = ""; //$NON-NLS-1$
                if (!testedParam.getFieldType().equals(EParameterFieldType.CHECK)
                        && !testedParam.getFieldType().equals(EParameterFieldType.RADIO)) {
                    oldValue = (String) testedParam.getValue();
                }
                testedParam.setValueToDefault(elementParameters);
                if (testedParam.getFieldType().equals(EParameterFieldType.MAPPING_TYPE)) {
                    String newMapping = (String) testedParam.getValue();
                    if (!oldValue.equals(newMapping)) {
                        Node node = (Node) referenceNode;
                        if (node.getMetadataList().size() > 0) {
                            // to change with:
                            // IMetadataTable metadataTable = node.getMetadataFromConnector(testedParam.getContext());
                            IMetadataTable metadataTable = node.getMetadataList().get(0);
                            metadataTable.setDbms(newMapping);
                        }
                    }
                } else if (testedParam.getFieldType().equals(EParameterFieldType.TEXT)) {
                    String newValue = (String) testedParam.getValue();
                    if (StringUtils.isNotEmpty(oldValue) && !oldValue.equals(newValue)
                            && !isDefaultValue(testedParam, oldValue)) {
                        Node node = (Node) referenceNode;
                        String label = node.getLabel();
                        if (label != null && label.startsWith("tHDFS")) { //$NON-NLS-1$
                            testedParam.setValue(oldValue);
                        }
                    }
                }
            } else {
                // See issue 975, update the schema.
                Node node = (Node) referenceNode;
                if (node.getMetadataList().size() > 0) {
                    IMetadataTable metadataTable = null;
                    IMetadataTable newMetadataTable = null;
                    // for feature 0014652
                    boolean isBuiltIn = false;

                    final IElementParameter elementParameter = testedParam.getChildParameters().get(
                            EParameterFieldType.SCHEMA_TYPE.getName());
                    if (elementParameter != null) {
                        Object value = elementParameter.getValue();
                        if ("BUILT_IN".equals(value.toString())) {//$NON-NLS-1$
                            isBuiltIn = true;
                        }
                    }
                    if (isBuiltIn || elementParameter == null) {
                        metadataTable = node.getMetadataFromConnector(testedParam.getContext());
                        testedParam.setValueToDefault(node.getElementParameters());
                        newMetadataTable = ((IMetadataTable) testedParam.getValue()).clone(true);

                        if (metadataTable != null && newMetadataTable != null) {
                            newMetadataTable.setTableName(metadataTable.getTableName());
                            newMetadataTable.setAttachedConnector(metadataTable.getAttachedConnector());

                            // condition to know if it needs to clear the table or not to get the new table:
                            // if table have any non-custom column or if "newMetadataTable" is empty, then clear
                            boolean needClearTargetTable = newMetadataTable.getListColumns().isEmpty();
                            if (!needClearTargetTable) {
                                needClearTargetTable = false;
                                for (IMetadataColumn column : newMetadataTable.getListColumns()) {
                                    if (!column.isCustom()) {
                                        needClearTargetTable = true;
                                        break;
                                    }
                                }
                            }
                            if (needClearTargetTable) {
                                metadataTable.getListColumns().clear();
                            }
                            // remove all custom columns first, since new custom columns from default table will be
                            // added
                            // automatically
                            List<IMetadataColumn> columnsToRemove = new ArrayList<IMetadataColumn>();
                            for (IMetadataColumn column : metadataTable.getListColumns()) {
                                if (column.isCustom() || column.isReadOnly()) {
                                    columnsToRemove.add(column);
                                }
                            }
                            metadataTable.getListColumns().removeAll(columnsToRemove);

                            boolean onlyHaveCustomInDefault = true;
                            List<IMetadataColumn> customColumnsFromDefault = new ArrayList<IMetadataColumn>();
                            for (IMetadataColumn column : newMetadataTable.getListColumns()) {
                                if (!column.isCustom() && !column.isReadOnly()) {
                                    onlyHaveCustomInDefault = false;
                                } else {
                                    customColumnsFromDefault.add(column);
                                }
                            }
                            metadataTable.getListColumns().addAll(customColumnsFromDefault);
                            if (onlyHaveCustomInDefault) {
                                metadataTable.setReadOnly(newMetadataTable.isReadOnly());
                                newMetadataTable = metadataTable;
                            }
                            ChangeMetadataCommand changeMetadataCommand = new ChangeMetadataCommand(node, null, null,
                                    newMetadataTable);
                            changeMetadataCommands.add(changeMetadataCommand);
                            changeMetadataCommand.execute(true);
                        }
                    } else {
                        metadataTable = node.getMetadataFromConnector(testedParam.getContext());
                        if (testedParam.getName().equals("SCHEMA")) {//$NON-NLS-1$
                            newMetadataTable = metadataTable;
                        }
                    }
                }
            }
        }
    }

    @Override
    public void undo() {
        IElementParameter currentParam = elem.getElementParameter(propName);
        if (repositoryValueWasUsed) {
            if (currentParam.getFieldType() == EParameterFieldType.MEMO_SQL) {
                elem.setPropertyValue(EParameterName.QUERYSTORE_TYPE.getName(), EmfComponent.REPOSITORY);
            } else {
                elem.setPropertyValue(propertyTypeName, EmfComponent.REPOSITORY);
            }
            for (IElementParameter param : elem.getElementParameters()) {
                String repositoryValue = param.getRepositoryValue();
                if (param.isShow(elem.getElementParameters()) && (repositoryValue != null)
                        && param.getCategory().equals(currentParam.getCategory())) {
                    param.setRepositoryValueUsed(true);
                }
            }
        }
        elem.setPropertyValue(propName, oldValue);
        if (propName.contains(EParameterName.PROCESS_TYPE_PROCESS.getName())) {
            // oldValue is the id of the job
            ProcessItem processItem = ItemCacheManager.getProcessItem((String) oldValue);
            if (processItem != null) {
                currentParam.getParentParameter().setValue(processItem.getProperty().getLabel());
            }
        }

        for (IElementParameter param : oldElementValues.keySet()) {
            param.setValue(oldElementValues.get(param));
        }

        if (toUpdate) {
            elem.setPropertyValue(updataComponentParamName, Boolean.TRUE);
        }
        if (changeMetadataCommands != null) {
            int size = changeMetadataCommands.size();
            for (int i = size - 1; 0 <= i; i--) {
                ChangeMetadataCommand changeMetadataCommand = changeMetadataCommands.get(i);
                changeMetadataCommand.undo();
            }
        }
        if (updateELTMapComponentCommand != null) {
            updateELTMapComponentCommand.undo();
        }
        CodeView.refreshCodeView(elem);
        ComponentSettings.switchToCurComponentSettingsView();
        JobSettings.switchToCurJobSettingsView();
        refreshTraceConnections();
        refreshResumingConnections();
        if (elem instanceof Node) {
            ((Node) elem).checkAndRefreshNode();
        }
        refreshMR(propName);
    }

    @Override
    public void redo() {
        IElementParameter currentParam = elem.getElementParameter(propName);
        if (repositoryValueWasUsed) {
            if (currentParam.getFieldType() == EParameterFieldType.MEMO_SQL) {
                elem.setPropertyValue(EParameterName.QUERYSTORE_TYPE.getName(), EmfComponent.BUILTIN);
            } else {
                elem.setPropertyValue(propertyTypeName, EmfComponent.BUILTIN);
            }

            for (IElementParameter param : elem.getElementParameters()) {
                boolean paramFlag = JobSettingsConstants.isExtraParameter(param.getName());
                boolean extraFlag = JobSettingsConstants.isExtraParameter(propertyTypeName);
                if (paramFlag == extraFlag) {
                    // for job settings extra.(feature 2710)
                    param.setRepositoryValueUsed(false);
                }
            }
        }

        elem.setPropertyValue(propName, newValue);
        if (propName.contains(EParameterName.PROCESS_TYPE_PROCESS.getName())) {
            // newValue is the id of the job
            ProcessItem processItem = ItemCacheManager.getProcessItem((String) newValue);
            if (processItem != null) {
                currentParam.getParentParameter().setValue(processItem.getProperty().getLabel());
            }
        }

        if (currentParam.getFieldType().equals(EParameterFieldType.CLOSED_LIST)) {
            for (int i = 0; i < elem.getElementParameters().size(); i++) {
                IElementParameter param = elem.getElementParameters().get(i);
                if (param.getDefaultValues().size() > 0) {
                    param.setValueToDefault(elem.getElementParameters());
                }
            }
        }

        if (toUpdate) {
            elem.setPropertyValue(updataComponentParamName, new Boolean(true));
        }

        if (changeMetadataCommands != null) {
            for (ChangeMetadataCommand changeMetadataCommand : changeMetadataCommands) {
                changeMetadataCommand.redo();
            }
        }
        if (updateELTMapComponentCommand != null) {
            updateELTMapComponentCommand.redo();
        }
        CodeView.refreshCodeView(elem);
        ComponentSettings.switchToCurComponentSettingsView();
        JobSettings.switchToCurJobSettingsView();
        refreshTraceConnections();
        refreshResumingConnections();
        if (elem instanceof Node) {
            ((Node) elem).checkAndRefreshNode();
        }
        refreshMR(propName);
    }

    private void refreshTraceConnections() {
        if (propName.equals(EParameterName.TRACES_CONNECTION_ENABLE.getName()) && this.elem instanceof Connection) {
            // TDI-8003:if the connection's style is RunIf,its trace should be null here
            if (((Connection) this.elem).getConnectionTrace() != null && !propName.equals(EParameterName.CONDITION)) {
                ((Connection) this.elem).getConnectionTrace().setPropertyValue(EParameterName.TRACES_SHOW_ENABLE.getName(), true);
            }
        }
    }

    private void refreshResumingConnections() {
        if (propName.equals(EParameterName.RESUMING_CHECKPOINT.getName()) && this.elem instanceof Connection) {
            if (((Connection) this.elem).getConnectionTrace() != null && !propName.equals(EParameterName.CONDITION)) {
                ((Connection) this.elem).getConnectionTrace()
                        .setPropertyValue(EParameterName.RESUMING_CHECKPOINT.getName(), true);
            }
        }
    }

    public String getPropName() {
        return this.propName;
    }

    public IElement getElement() {
        return this.elem;
    }

    public Object getOldValue() {
        return this.oldValue;
    }

    public Object getNewValue() {
        return this.newValue;
    }

    private Map<String, Object> createParameters(Node node) {
        if (node == null) {
            Collections.emptyMap();
        }
        Map<String, Object> parameters = new HashMap<String, Object>();
        if (node.getComponent().getComponentType() != EComponentType.JOBLET) {
            if (node.getExternalData() != null) {
                parameters.put(INode.RELOAD_PARAMETER_EXTERNAL_BYTES_DATA, node.getExternalData());
            }
            parameters.put(INode.RELOAD_PARAMETER_METADATA_LIST, node.getMetadataList());
        }
        parameters.put(INode.RELOAD_PARAMETER_ELEMENT_PARAMETERS, node.getElementParameters());
        parameters.put(INode.RELOAD_PARAMETER_CONNECTORS, node.getListConnector());

        return parameters;
    }

    private void reloadNode(Node node, IComponent newComponent) {
        if (node == null || newComponent == null) {
            return;
        }
        Map<String, Object> parameters = createParameters(node);
        if (!parameters.isEmpty()) {
            node.reloadComponent(newComponent, parameters, false);
        }
    }

    private void refreshMR(String propName) {
        if (elem instanceof Node) {
            Node node = (Node) elem;
            if (!node.isMapReduce()) {
                return;
            }
            if (!propName.equals("MAP_ONLY") && !propName.equals("REPLICATED_JOIN")
                    && !propName.equals(EParameterName.GROUPBYS.getName())) {
                return;
            }

            ((IProcess2) node.getProcess()).getGeneratingNodes();
            node.refreshNodeContainer();
        } else if (elem instanceof IProcess2) {
            IProcess2 process = (IProcess2) elem;
            for (INode inode : process.getGraphicalNodes()) {
                Node node = (Node) inode;
                if (!node.isMapReduceStart()) {
                    return;
                }
                if (!propName.equals("DISTRIBUTION")) {
                    return;
                }
                node.refreshNodeContainer();
            }
        }

    }

    private boolean getTakeSchema() {
        return MessageDialog.openQuestion(DisplayUtils.getDefaultShell(false), "", Messages.getString("Node.getSchemaOrNot")); //$NON-NLS-1$ //$NON-NLS-2$
    }

    private boolean isDefaultValue(IElementParameter param, String paraValue) {
        if (StringUtils.isEmpty(paraValue)) {
            return false;
        }
        for (IElementParameterDefaultValue defaultValue : param.getDefaultValues()) {
            if (paraValue.equals(defaultValue.getDefaultValue())) {
                return true;
            }
        }
        return false;
    }
}
