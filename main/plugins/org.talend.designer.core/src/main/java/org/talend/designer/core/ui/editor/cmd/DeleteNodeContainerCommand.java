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
package org.talend.designer.core.ui.editor.cmd;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections.map.MultiKeyMap;
import org.eclipse.gef.commands.Command;
import org.talend.core.CorePlugin;
import org.talend.core.model.metadata.IMetadataTable;
import org.talend.core.model.process.EConnectionType;
import org.talend.core.model.process.IConnection;
import org.talend.core.model.process.IConnectionCategory;
import org.talend.core.model.process.IExternalNode;
import org.talend.core.model.process.INode;
import org.talend.core.model.process.INodeConnector;
import org.talend.core.model.process.IProcess2;
import org.talend.designer.core.i18n.Messages;
import org.talend.designer.core.model.components.ExternalUtilities;
import org.talend.designer.core.ui.editor.connections.Connection;
import org.talend.designer.core.ui.editor.jobletcontainer.AbstractJobletContainer;
import org.talend.designer.core.ui.editor.nodecontainer.NodeContainer;
import org.talend.designer.core.ui.editor.nodes.Node;
import org.talend.designer.core.ui.editor.process.Process;

/**
 * Delete a list of nodes in the process. <br/>
 *
 * $Id$
 *
 */
public class DeleteNodeContainerCommand extends Command {

    private IProcess2 process;

    private List<INode> nodeList;

    private List<String> joinTableNames = new ArrayList<String>();

    private MultiKeyMap connectionDeletedInfosMap;

    public DeleteNodeContainerCommand(IProcess2 process, List<INode> nodeList) {
        this.process = process;
        this.nodeList = nodeList;
        setLabel(Messages.getString("DeleteNodeCommand.Label")); //$NON-NLS-1$
    }

    @Override
    @SuppressWarnings("unchecked")
    public void execute() {
        connectionDeletedInfosMap = new MultiKeyMap();
        process.setActivate(false);
        List uniqueNameList = new ArrayList();
        for (INode node : nodeList) {
            if (node.getJobletNode() != null) {
                continue;
            }
            uniqueNameList.add(node.getUniqueName());
            NodeContainer nodeContainer = ((Node) node).getNodeContainer();
            ((Process) process).removeNodeContainer(nodeContainer);
            List<IConnection> inputList = (List<IConnection>) node.getIncomingConnections();
            List<IConnection> outputList = (List<IConnection>) node.getOutgoingConnections();
            boolean builtIn = node.getConnectorFromType(EConnectionType.FLOW_MAIN).isMultiSchema()
                    | node.getConnectorFromType(EConnectionType.TABLE).isMultiSchema();

            for (IConnection connection : inputList) {
                // see bug 0002633: "rejects" link disappears at times.
                if (connection != null && connection.getSourceNodeConnector() != null) {
                    connection.getSourceNodeConnector().setCurLinkNbOutput(
                            connection.getSourceNodeConnector().getCurLinkNbOutput() - 1);
                }

                INode prevNode = connection.getSource();
                if ((prevNode instanceof Node) && ((Node) prevNode).getJobletNode() != null) {
                    Node jobletnode = (Node) prevNode.getJobletNode();
                    ((AbstractJobletContainer) jobletnode.getNodeContainer()).getOutputs().remove(connection);
                    if (!nodeList.contains(jobletnode)) {
                        boolean builtInJobletNode = jobletnode.getConnectorFromType(EConnectionType.FLOW_MAIN).isMultiSchema()
                                | node.getConnectorFromType(EConnectionType.TABLE).isMultiSchema();
                        storeMetadata(connection, jobletnode, true);
                        jobletnode.removeOutput(connection);
                        if (!builtInJobletNode) {
                            process.removeUniqueConnectionName(connection.getUniqueName());
                        }
                    }
                }
                if (!nodeList.contains(prevNode)) {
                    boolean builtInPrevNode = prevNode.getConnectorFromType(EConnectionType.FLOW_MAIN).isMultiSchema()
                            | node.getConnectorFromType(EConnectionType.TABLE).isMultiSchema();
                    boolean remove = true;
                    if ((prevNode instanceof Node) && ((Node) prevNode).isELTMapComponent()) {
                        remove = false;
                    }
                    storeMetadata(connection, prevNode, remove);
                    prevNode.removeOutput(connection);
					if (prevNode.isExternalNode()) {
						IExternalNode externalNode = ExternalUtilities.getExternalNodeReadyToOpen((Node) prevNode);
						externalNode.removeOutput(connection);
						ExternalNodeChangeCommand cmd = new ExternalNodeChangeCommand((Node) prevNode, externalNode);
						cmd.execute();
					}
                    if (!builtInPrevNode && remove) {
                        process.removeUniqueConnectionName(connection.getUniqueName());
                    }
                }
            }
            for (IConnection connection : outputList) {
                INode nextNode = connection.getTarget();
                if ((nextNode instanceof Node) && ((Node) nextNode).getJobletNode() != null) {
                    Node jobletnode = (Node) nextNode.getJobletNode();
                    ((AbstractJobletContainer) jobletnode.getNodeContainer()).getInputs().remove(connection);
                    if (!nodeList.contains(jobletnode)) {
                        jobletnode.removeInput(connection);
                        boolean builtInJobletNode = jobletnode.getConnectorFromType(EConnectionType.FLOW_MAIN).isMultiSchema()
                                | node.getConnectorFromType(EConnectionType.TABLE).isMultiSchema();
                        if (!builtInJobletNode) {
                            process.removeUniqueConnectionName(connection.getUniqueName());
                        }
                    }
                }
                if (!nodeList.contains(nextNode)) {
                    INodeConnector nodeConnector = nextNode.getConnectorFromType(connection.getLineStyle());
                    nodeConnector.setCurLinkNbInput(nodeConnector.getCurLinkNbInput() - 1);
                    nextNode.removeInput(connection);
                    if (nextNode != null) {
                        for (int i = 0; i < nextNode.getIncomingConnections().size(); i++) {
                            Connection nextNodeConnection = (Connection) nextNode.getIncomingConnections().get(i);
                            nextNodeConnection.updateName();
                        }
                    }
					if (nextNode.isExternalNode()) {
						IExternalNode externalNode = ExternalUtilities.getExternalNodeReadyToOpen((Node) nextNode);
						externalNode.removeInput(connection);
						ExternalNodeChangeCommand cmd = new ExternalNodeChangeCommand((Node) nextNode, externalNode);
						cmd.execute();
					}
                }
                if (!builtIn) {
                    process.removeUniqueConnectionName(connection.getUniqueName());
                }
            }
            if (builtIn) {
                for (IMetadataTable meta : node.getMetadataList()) {
                    String metaName = meta.getTableName();
                    process.removeUniqueConnectionName(metaName);
                }
                // for tmap remove join table names
                final List<String> names = CorePlugin.getDefault().getMapperService().getJoinTableNames(node.getExternalData());
                if (!names.isEmpty()) {
                    joinTableNames.addAll(names);
                    for (String name : joinTableNames) {
                        process.removeUniqueConnectionName(name);
                    }
                }

            }
        }

        process.setActivate(true);
        process.checkStartNodes();
        process.checkProcess();
    }

    @Override
    @SuppressWarnings("unchecked")
    public void undo() {
        process.setActivate(false);

        for (INode node : nodeList) {
            if (node.getJobletNode() != null) {
                continue;
            }
            NodeContainer nodeContainer = ((Node) node).getNodeContainer();
            this.process.addUniqueNodeName(node.getUniqueName());
            ((Process) process).addNodeContainer(nodeContainer);

            List<Connection> inputList = (List<Connection>) node.getIncomingConnections();
            List<Connection> outputList = (List<Connection>) node.getOutgoingConnections();
            boolean builtIn = node.getConnectorFromType(EConnectionType.FLOW_MAIN).isMultiSchema()
                    | node.getConnectorFromType(EConnectionType.TABLE).isMultiSchema();
            for (Connection connection : inputList) {
                // see bug 0004514: need to undo for 0002633
                if (connection != null && connection.getSourceNodeConnector() != null) {
                    connection.getSourceNodeConnector().setCurLinkNbOutput(
                            connection.getSourceNodeConnector().getCurLinkNbOutput() + 1);
                }

                INode prevNode = connection.getSource();
                if ((prevNode instanceof Node) && ((Node) prevNode).getJobletNode() != null) {
                    Node jobletnode = (Node) prevNode.getJobletNode();
                    ((AbstractJobletContainer) jobletnode.getNodeContainer()).getOutputs().add(connection);
                    restoreMetadata(connection, jobletnode);
                }
				if (!nodeList.contains(prevNode)) {
					boolean isAddOutput = false;
					if (!prevNode.getOutgoingConnections().contains(connection)) {
						prevNode.addOutput(connection);
						isAddOutput = true;
					}
					restoreMetadata(connection, prevNode);
					connection.reconnect();
					connection.updateAllId();
					if (isAddOutput && prevNode.isExternalNode()) {
						IExternalNode externalNode = ExternalUtilities.getExternalNodeReadyToOpen((Node) prevNode);
						externalNode.addOutput(connection);
						ExternalNodeChangeCommand cmd = new ExternalNodeChangeCommand((Node) prevNode, externalNode);
						cmd.execute();
					}
					boolean builtInPrevNode = prevNode.getConnectorFromType(EConnectionType.FLOW_MAIN).isMultiSchema()
							| node.getConnectorFromType(EConnectionType.TABLE).isMultiSchema();
					if (connection.getLineStyle().hasConnectionCategory(IConnectionCategory.UNIQUE_NAME)
							&& !builtInPrevNode) {
						// for bug 10024
						// see 10583
						String name = connection.getUniqueName();
						if (connection.getConnectorName().startsWith("TRIGGER_OUTPUT")) {
							if (process.checkValidConnectionName(name)) {
								process.addUniqueConnectionName(name);
							}
						} else {
							process.addUniqueConnectionName(name);
						}
					}
				}
            }
			for (IConnection connection : outputList) {
				INode nextNode = connection.getTarget();
				if ((nextNode instanceof Node) && ((Node) nextNode).getJobletNode() != null) {
					Node jobletnode = (Node) nextNode.getJobletNode();
					((AbstractJobletContainer) jobletnode.getNodeContainer()).getInputs().add(connection);
				}
				boolean isAddInput = false;
				if (!nodeList.contains(nextNode)) {
					if (!nextNode.getIncomingConnections().contains(connection)) {
						nextNode.addInput(connection);
						isAddInput = true;
					}
					INodeConnector nodeConnector = nextNode.getConnectorFromType(connection.getLineStyle());
					nodeConnector.setCurLinkNbInput(nodeConnector.getCurLinkNbInput() + 1);
					connection.reconnect();
					if (isAddInput && nextNode.isExternalNode()) {
						IExternalNode externalNode = ExternalUtilities.getExternalNodeReadyToOpen((Node) nextNode);
						externalNode.addInput(connection);
						ExternalNodeChangeCommand cmd = new ExternalNodeChangeCommand((Node) nextNode, externalNode);
						cmd.execute();
					}
				}
				if (!builtIn) {
					if (connection.getLineStyle().hasConnectionCategory(IConnectionCategory.UNIQUE_NAME)) {
						// for bug 10024
						// see 10583
						String name = connection.getUniqueName();
						// name = process.generateUniqueConnectionName(name);
						process.addUniqueConnectionName(name);
					}
				}
			}
            if (builtIn) {
                for (IMetadataTable meta : node.getMetadataList()) {
                    String metaName = meta.getTableName();
                    process.addUniqueConnectionName(metaName);
                }

                // tmap join table
                for (String name : joinTableNames) {
                    process.addUniqueConnectionName(name);
                }
            }
        }

        process.setActivate(true);
        process.checkStartNodes();
        process.checkProcess();
    }

    @Override
    public void redo() {
        this.execute();
    }

    private void storeMetadata(IConnection connection, INode node, boolean remove) {
        ConnectionDeletedInfo deletedInfo = new ConnectionDeletedInfo();
        connectionDeletedInfosMap.put(connection, node, deletedInfo);

        INode source = connection.getSource();
        if (source != null && remove) {
            deletedInfo.metadataTable = connection.getMetadataTable();
            List<IMetadataTable> metaList = source.getMetadataList();
            if (metaList != null && deletedInfo.metadataTable != null) {
                deletedInfo.metadataTableIndex = metaList.indexOf(deletedInfo.metadataTable);
            }
        }
    }

	private void restoreMetadata(IConnection connection, INode node) {
		ConnectionDeletedInfo deletedInfo = (ConnectionDeletedInfo) connectionDeletedInfosMap.get(connection, node);
		if (deletedInfo != null) {
			INode source = connection.getSource();
			if (source != null && deletedInfo.metadataTable != null) {
				List<IMetadataTable> metaList = source.getMetadataList();
				boolean isFind = false;
				for (IMetadataTable table : metaList) {
					if (table.getTableName().equals(deletedInfo.metadataTable.getTableName())) {
						isFind = true;
						break;
					}
				}
				if (!isFind) {
					metaList.add(deletedInfo.metadataTableIndex, deletedInfo.metadataTable);
				}
			}
		}
	}

    private class ConnectionDeletedInfo {

        public int metadataTableIndex = -1;

        public IMetadataTable metadataTable = null;

    }
}
