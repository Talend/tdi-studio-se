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
package org.talend.designer.core.ui.editor.update.cmd;

import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.eclipse.gef.commands.Command;
import org.talend.commons.exception.PersistenceException;
import org.talend.commons.runtime.model.components.IComponentConstants;
import org.talend.commons.ui.runtime.exception.ExceptionHandler;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.PluginChecker;
import org.talend.core.model.components.ComponentCategory;
import org.talend.core.model.components.EComponentType;
import org.talend.core.model.components.IComponent;
import org.talend.core.model.metadata.IMetadataColumn;
import org.talend.core.model.metadata.IMetadataTable;
import org.talend.core.model.metadata.MetadataToolHelper;
import org.talend.core.model.process.EParameterFieldType;
import org.talend.core.model.process.IConnection;
import org.talend.core.model.process.IElementParameter;
import org.talend.core.model.process.INode;
import org.talend.core.model.process.INodeConnector;
import org.talend.core.model.process.IProcess;
import org.talend.core.model.process.IProcess2;
import org.talend.core.model.update.EUpdateItemType;
import org.talend.core.model.update.IUpdateItemType;
import org.talend.core.model.update.UpdateResult;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.core.ui.IJobletProviderService;
import org.talend.core.ui.component.ComponentsFactoryProvider;
import org.talend.designer.core.model.components.EParameterName;
import org.talend.designer.core.model.process.AbstractProcessProvider;
import org.talend.designer.core.ui.editor.cmd.ChangeMetadataCommand;
import org.talend.designer.core.ui.editor.jobletcontainer.AbstractJobletContainer;
import org.talend.designer.core.ui.editor.nodes.Node;
import org.talend.designer.core.ui.editor.process.Process;
import org.talend.designer.core.ui.editor.update.UpdateManagerUtils;

/**
 * ggu class global comment. Detailled comment
 *
 * this function is moved from the class AbstractTalendEditor.
 */
public class UpdateJobletNodeCommand extends Command {

    private UpdateResult result;

    public UpdateJobletNodeCommand(UpdateResult result) {
        super();
        this.result = result;
    }

    /**
     * this function is moved from the method updateGraphicalNodes.
     */
    @SuppressWarnings("unchecked")
    @Override
    public void execute() {
        Object job = result.getJob();
        if (job == null) {
            return;
        }
        if (job instanceof Process) {

            Process process = (Process) job;
            Object parameter = result.getParameter();

            // update property change events
            if (parameter instanceof PropertyChangeEvent) {
                PropertyChangeEvent evt = (PropertyChangeEvent) parameter;
                updatePropertyChangeEvents(process, evt);
            } else {
                //
                IUpdateItemType updateType = result.getUpdateType();
                if (updateType instanceof EUpdateItemType) {
                    switch ((EUpdateItemType) updateType) {
                    case JOBLET_RENAMED:
                        if (!(parameter instanceof List)) {
                            return;
                        }
                        List<Object> params = (List<Object>) parameter;
                        if (params.size() != 3) {
                            return;
                        }
                        final String oldName = (String) params.get(1);
                        final String newName = (String) params.get(2);

                        updateRenaming(process, oldName, newName);
                        break;
                    case RELOAD:
                        List<Node> jobletNodes = (List<Node>) result.getUpdateObject();
                        if (jobletNodes != null && !jobletNodes.isEmpty()) {
                            for (Node node : jobletNodes) {
                                IComponent newComponent = ComponentsFactoryProvider.getInstance().get(
                                        node.getComponent().getName(), process.getComponentsType());
                                if (newComponent == null) {
                                    continue;
                                }
                                // node loaded is from loading in the check.
                                // need to get the instance from process, or this process will be done with wrong
                                // instance
                                // of node.
                                Node currentNode = getOriginalNodeFromProcess(node);
                                boolean neesPro = needPropagate(currentNode);
                                if (currentNode.isJoblet() || currentNode.isMapReduce()) {// maybe no need modify
                                    List<IElementParameter> tempList = new ArrayList<IElementParameter>(currentNode.getElementParameters());
                                    if (result.isNeedReloadJoblet()) {
                                        reloadNode(currentNode, newComponent);
                                    }
                                    if (currentNode.getNodeContainer() instanceof AbstractJobletContainer) {
                                        for(IElementParameter tempParam : tempList) {
                                            IElementParameter param = currentNode.getElementParameter(tempParam.getName());
                                            if (param != null) {
                                                param.setValue(tempParam.getValue());
                                            }
                                        }
                                        ((AbstractJobletContainer) currentNode.getNodeContainer()).updateJobletNodes(true);
                                    }
                                } else {
                                    reloadNode(currentNode, newComponent);
                                }
                                propagate(currentNode, neesPro);
                            }
                            process.checkProcess();
                        }
                        break;
                    case JOBLET_SCHEMA:
                        updateSchema(process, (Node) result.getUpdateObject());
                        break;
                    default:
                    }
                } // else { // for extension

            }
        }
    }

    private Node getOriginalNodeFromProcess(Node nodeFromUpdateChecker) {
        // instance of node before might not be good (loaded while check updates needed)
        // so get the instance of the node of the current job in this object.
        IProcess process = (IProcess) result.getJob();
        for (INode node : process.getGraphicalNodes()) {
            if (node.getUniqueName().equals(nodeFromUpdateChecker.getUniqueName())) {
                return (Node) node;
            }
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private void updateRenaming(Process process, final String oldName, final String newName) {
        if (process == null || oldName == null || newName == null) {
            return;
        }
        IComponent newComponent = ComponentsFactoryProvider.getInstance().get(newName, process.getComponentsType());
        if (newComponent == null) {
            return;
        }

        for (Node node : (List<Node>) process.getGraphicalNodes()) {
            if (node.getComponent().getName().equals(newName) || node.getComponent().getName().equals(oldName)) {
                reloadNode(node, newComponent);
            }
        }
        process.checkProcess();
    }

    private void updateSchema(Process process, Node node) {
        if (process == null || node == null) {
            return;
        }
        // node loaded is from loading in the check.
        // need to get the instance from process, or this process will be done with wrong instance
        // of node.
        Node currentNode = getOriginalNodeFromProcess(node);

        String componentName = currentNode.getComponent().getName();
        IComponent newComponent = ComponentsFactoryProvider.getInstance().get(componentName,
                process.getComponentsType());
        if (newComponent == null) {
            return;
        }
        reloadNode(currentNode, newComponent);

        process.checkProcess();
    }

    private void reloadNode(Node node, IComponent newComponent) {
        if (node == null || newComponent == null) {
            return;
        }
        Map<String, Object> parameters = createParameters(node, newComponent);
        if (!parameters.isEmpty()) {
            node.reloadComponent(newComponent, parameters, true);
        }
    }

    private Map<String, Object> createParameters(Node node, IComponent newComponent) {
        Map<String, Object> parameters = new HashMap<String, Object>();
        if (node.getComponent().getComponentType() != EComponentType.JOBLET) {
            if (node.getExternalData() != null) {
                parameters.put(INode.RELOAD_PARAMETER_EXTERNAL_BYTES_DATA, node.getExternalData());
            }
            parameters.put(INode.RELOAD_PARAMETER_METADATA_LIST, node.getMetadataList());

        }
        // set some parameter from joblet
        if (PluginChecker.isJobLetPluginLoaded()) {
            IJobletProviderService service = (IJobletProviderService) GlobalServiceRegister.getDefault().getService(
                    IJobletProviderService.class);
            if (service != null) {
                service.updateParametersFromJoblet(node, newComponent);
            }
        }
        parameters.put(INode.RELOAD_PARAMETER_ELEMENT_PARAMETERS, node.getElementParameters());

        List<? extends INodeConnector> listConnectors = node.getListConnector();
        List<? extends INodeConnector> newConnectors = newComponent.createConnectors(new Node(newComponent, (Process) node
                .getProcess()));
        for (INodeConnector connector : newConnectors) {
            for (INodeConnector oldConnector : listConnectors) {
                if (connector.getName().equals(oldConnector.getName())) {
                    connector.setCurLinkNbInput(oldConnector.getCurLinkNbInput());
                    connector.setCurLinkNbOutput(oldConnector.getCurLinkNbOutput());
                }
            }
        }
        parameters.put(INode.RELOAD_PARAMETER_CONNECTORS, newConnectors);

        return parameters;
    }

    @SuppressWarnings("unchecked")
    private void updatePropertyChangeEvents(Process process, PropertyChangeEvent evt) {
        if (process == null || evt == null) {
            return;
        }
        String propertyName = evt.getPropertyName();
        Object updateObject = result.getUpdateObject();

        Set<String> jobletNodeNames = new HashSet<String>();
        if (updateObject != null) {
            if (!(updateObject instanceof List)) {
                return;
            }
            for (INode jobletNode : (List<INode>) updateObject) {
                jobletNodeNames.add(jobletNode.getComponent().getName());
            }
        }

        if (propertyName.equals(IComponentConstants.NORMAL)) {
            for (Node node : (List<Node>) process.getGraphicalNodes()) {
                /*
                 * if jobletNodes==null, will reload all component. Or, olny reload the fixed node.
                 */
                if (updateObject != null && !jobletNodeNames.contains(node.getComponent().getName())) {
                    continue;
                }

                IComponent newComponent = ComponentsFactoryProvider.getInstance().get(node.getComponent().getName(),
                        process.getComponentsType());
                if (newComponent == null) {
                    continue;
                }
                reloadNode(node, newComponent);
            }
            process.checkProcess();
        }
    }

    /**
     * qzhang Comment method "updateGraphicalNodesSchema".
     *
     * this method is moved from class AbstractTalendEditor.
     *
     * @param evt
     */
    @SuppressWarnings("unchecked")
    private void updateGraphicalNodesSchema(Process process, PropertyChangeEvent evt) {
        if (!(evt.getSource() instanceof INode)) {
            return;
        }
        INode sourceNode = (INode) evt.getSource();

        String componentName = sourceNode.getComponent().getName();
        IComponent newComponent = ComponentsFactoryProvider.getInstance().get(componentName,
                process.getComponentsType());
        if (newComponent == null) {
            return;
        }
        Object[] newMetadataTables = (Object[]) evt.getNewValue();
        List<IMetadataTable> newInputTableList = (List<IMetadataTable>) newMetadataTables[0];

        List<IMetadataTable> newOutputTableList = (List<IMetadataTable>) newMetadataTables[1];

        for (Node node : (List<Node>) process.getGraphicalNodes()) {
            if (node.getComponent().getName().equals(componentName)) {
                List<IElementParameter> outputElemParams = new ArrayList<IElementParameter>();

                IElementParameter outputElemParam = null;

                List<? extends IElementParameter> elementParameters = node.getElementParameters();
                for (IElementParameter elementParameter : elementParameters) {
                    if (EParameterFieldType.SCHEMA_TYPE.equals(elementParameter.getFieldType())) {
                        outputElemParams.add(elementParameter);
                    }
                }
                ChangeMetadataCommand command;
                List<? extends IConnection> incomingConnections = node.getIncomingConnections();
                if (incomingConnections.size() <= 1) {
                    for (int i = 0; i < incomingConnections.size(); i++) {
                        IConnection connection = incomingConnections.get(i);
                        Node source = (Node) connection.getSource();
                        IMetadataTable metadataTable = connection.getMetadataTable();
                        if (metadataTable != null) {
                            IMetadataTable newInputMetadataTable = UpdateManagerUtils.getNewInputTableForConnection(
                                    newInputTableList, metadataTable.getAttachedConnector());
                            if (newInputMetadataTable != null && !metadataTable.sameMetadataAs(newInputMetadataTable)) {
                                IElementParameter elementParam = source.getElementParameterFromField(EParameterFieldType.SCHEMA_TYPE);
                                command = new ChangeMetadataCommand(source, elementParam, metadataTable, newInputMetadataTable);
                                command.execute(Boolean.FALSE);
                            }
                        }
                    }
                } else {
                    for (IElementParameter param : node.getElementParameters()) {
                        if (param.isShow(node.getElementParameters())
                                && param.getFieldType().equals(EParameterFieldType.SCHEMA_TYPE)) {
                            IMetadataTable table = node.getMetadataFromConnector(param.getContext());
                            IElementParameter connParam = param.getChildParameters().get(EParameterName.CONNECTION.getName());
                            if (table != null && connParam != null && !StringUtils.isEmpty((String) connParam.getValue())) {
                                for (IConnection connection : incomingConnections) {
                                    if (connection.isActivate() && connection.getName().equals(connParam.getValue())) {
                                        if (!table.sameMetadataAs(connection.getMetadataTable(),
                                                IMetadataColumn.OPTIONS_IGNORE_KEY | IMetadataColumn.OPTIONS_IGNORE_NULLABLE
                                                        | IMetadataColumn.OPTIONS_IGNORE_COMMENT
                                                        | IMetadataColumn.OPTIONS_IGNORE_PATTERN
                                                        | IMetadataColumn.OPTIONS_IGNORE_DBCOLUMNNAME
                                                        | IMetadataColumn.OPTIONS_IGNORE_DBTYPE
                                                        | IMetadataColumn.OPTIONS_IGNORE_DEFAULT
                                                        | IMetadataColumn.OPTIONS_IGNORE_BIGGER_SIZE)) {
                                            Node source = (Node) connection.getSource();
                                            IMetadataTable metadataTable = connection.getMetadataTable();
                                            IElementParameter elementParam = source
                                                    .getElementParameterFromField(EParameterFieldType.SCHEMA_TYPE);
                                            command = new ChangeMetadataCommand(source, elementParam, metadataTable, table);
                                            command.execute(Boolean.FALSE);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                List<? extends IConnection> outgoingConnections = node.getOutgoingConnections();
                for (int i = 0; i < outgoingConnections.size(); i++) {
                    IConnection connection = outgoingConnections.get(i);
                    Node target = (Node) connection.getTarget();
                    IMetadataTable metadataTable = connection.getMetadataTable();
                    if (metadataTable != null) {
                        IMetadataTable newOutputMetadataTable = UpdateManagerUtils.getNewOutputTableForConnection(
                                newOutputTableList, metadataTable.getAttachedConnector());
                        if (newOutputMetadataTable != null && !metadataTable.sameMetadataAs(newOutputMetadataTable)) {
                            IElementParameter elementParam = target.getElementParameterFromField(EParameterFieldType.SCHEMA_TYPE);
                            command = new ChangeMetadataCommand(target, elementParam,
                                    target.getMetadataFromConnector(metadataTable.getAttachedConnector()), newOutputMetadataTable);
                            command.execute(Boolean.FALSE);
                        }
                    }
                }

                List<IMetadataTable> metadataList = node.getMetadataList();
                for (IMetadataTable metadataTable : metadataList) {
                    IMetadataTable newInputMetadataTable = UpdateManagerUtils.getNewInputTableForConnection(newInputTableList,
                            metadataTable.getAttachedConnector());
                    IMetadataTable newOutputMetadataTable = UpdateManagerUtils.getNewOutputTableForConnection(newOutputTableList,
                            metadataTable.getAttachedConnector());
                    outputElemParam = UpdateManagerUtils.getElemParam(outputElemParams, metadataTable.getAttachedConnector());

                    if (outputElemParam != null && newInputMetadataTable != null) {
                        command = new ChangeMetadataCommand(node, outputElemParam, (IMetadataTable) outputElemParam.getValue(),
                                newInputMetadataTable);
                        command.execute(Boolean.FALSE);
                        IMetadataTable metadataFromConnector = node.getMetadataFromConnector(outputElemParam.getContext());
                        MetadataToolHelper.copyTable(newInputMetadataTable, metadataFromConnector);
                    } else if (outputElemParam != null && newOutputMetadataTable != null) {
                        command = new ChangeMetadataCommand(node, outputElemParam, (IMetadataTable) outputElemParam.getValue(),
                                newOutputMetadataTable);
                        command.execute(Boolean.FALSE);
                        IMetadataTable metadataFromConnector = node.getMetadataFromConnector(outputElemParam.getContext());
                        MetadataToolHelper.copyTable(newOutputMetadataTable, metadataFromConnector);
                    }
                }
            }
        }
    }

    private boolean needPropagate(Node jobletNode) {
        List<IProcess2> openedProcesses = UpdateManagerUtils.getOpenedProcess();
        boolean opened = false;
        for (IProcess2 process : openedProcesses) {
            if (process.getId().equals(jobletNode.getProcess().getId())) {
                opened = true;
            }
        }
        if (!opened) {
            return false;
        }
        AbstractProcessProvider jobletProcessProvider = AbstractProcessProvider.findProcessProviderFromPID(IComponent.JOBLET_PID);
        if (jobletProcessProvider != null) {
            List<UpdateResult> resultList = jobletProcessProvider.checkJobletNodeSchema(jobletNode.getProcess());
            if (resultList.size() <= 0) {
                return false;
            } else {
                return true;
            }
        }
        return false;
    }

    private void propagate(Node jobletNode, boolean needPro) {
        if (!needPro) {
            return;
        }

        for (IConnection outConn : jobletNode.getOutgoingConnections()) {
            IMetadataTable tab = jobletNode.getMetadataFromConnector(outConn.getConnectorName());
            IMetadataTable tmpClone = tab.clone(true);
            IMetadataTable newOutputMetadata = jobletNode.getMetadataTable(outConn.getConnectorName());
            IMetadataTable toCopy = newOutputMetadata.clone();
            Node targetNode = (Node) outConn.getTarget();

            String dbmsId = null;
            IMetadataTable copy = null;
            if (targetNode.getMetadataFromConnector(outConn.getConnectorName()) != null) {
                dbmsId = targetNode.getMetadataFromConnector(outConn.getConnectorName()).getDbms();
                MetadataToolHelper.copyTable(dbmsId, toCopy, tmpClone);
                toCopy = tmpClone;

                // only if the target node have exactly the same connector
                copy = targetNode.getMetadataFromConnector(outConn.getConnectorName()).clone(true);
            } else {
                final String mainConnector = "FLOW"; // can only be FLOW right now for this case. //$NON-NLS-1$

                dbmsId = targetNode.getMetadataFromConnector(mainConnector).getDbms();
                MetadataToolHelper.copyTable(dbmsId, toCopy, tmpClone);
                toCopy = tmpClone;
                // if don't have the same connector, take the main connector of the component.

                copy = targetNode.getMetadataFromConnector(mainConnector).clone(true);
            }

            MetadataToolHelper.copyTable(dbmsId, toCopy, copy);
            ChangeMetadataCommand cmd = new ChangeMetadataCommand(targetNode, null, null, copy, null);// inputSchemaParam);
            cmd.execute(true);
        }

        try {
            ProxyRepositoryFactory.getInstance().save(((Process) jobletNode.getProcess()).getProperty().getItem());
        } catch (PersistenceException e) {
            ExceptionHandler.process(e);
        }

    }

}
