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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.gef.commands.Command;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.model.metadata.IMetadataTable;
import org.talend.core.model.process.IExternalNode;
import org.talend.core.model.process.INode;
import org.talend.core.model.process.INodeConnector;
import org.talend.core.service.IDbMapService;
import org.talend.designer.core.i18n.Messages;
import org.talend.designer.core.model.components.ExternalUtilities;
import org.talend.designer.core.model.utils.emf.talendfile.AbstractExternalData;
import org.talend.designer.core.ui.editor.connections.Connection;
import org.talend.designer.core.ui.editor.jobletcontainer.AbstractJobletContainer;
import org.talend.designer.core.ui.editor.nodes.Node;
import org.talend.designer.core.ui.editor.process.Process;

/**
 * Command that will delete a given connection. <br/>
 *
 * $Id$
 *
 */
public class ConnectionDeleteCommand extends Command {

    private List<Connection> connectionList;

    private Map<Connection, ConnectionDeletedInfo> connectionDeletedInfosMap;


    private AbstractExternalData externalEMFData;


    /**
     * Initialisation of the command that will delete the given connection.
     *
     * @param connection connection to delete
     */
    public ConnectionDeleteCommand(List<Connection> connectionList) {
        setLabel(Messages.getString("ConnectionDeleteCommand.Label")); //$NON-NLS-1$
        this.connectionList = connectionList;
    }

    @Override
    public void execute() {
        connectionDeletedInfosMap = new HashMap<Connection, ConnectionDeleteCommand.ConnectionDeletedInfo>();
        Process process = (Process) connectionList.get(0).getSource().getProcess();
        for (Connection connection : connectionList) {
            boolean re = deleteExpendNode(connection);
            if (re) {
                return;
            }
            ConnectionDeletedInfo deletedInfo = new ConnectionDeletedInfo();
            connectionDeletedInfosMap.put(connection, deletedInfo);

            INode source = connection.getSource();
            if (source != null && source instanceof Node && !(((Node) source).isELTMapComponent())) {
                deletedInfo.metadataTable = connection.getMetadataTable();
                List<IMetadataTable> metaList = source.getMetadataList();
                if (metaList != null && deletedInfo.metadataTable != null) {
                    deletedInfo.metadataTableIndex = metaList.indexOf(deletedInfo.metadataTable);
                }
            }

            connection.disconnect();
            final INode target = connection.getTarget();
            if (source.isExternalNode()) {
                IExternalNode externalNode = ExternalUtilities.getExternalNodeReadyToOpen((Node)source);
                externalNode.removeOutput(connection);
                ExternalNodeChangeCommand cmd = new ExternalNodeChangeCommand((Node) source, externalNode);
                cmd.execute();
            }
            if (target.isExternalNode()) {
                IExternalNode externalNode = ExternalUtilities.getExternalNodeReadyToOpen((Node)target);
                externalEMFData = externalNode.getExternalEmfData();
                externalNode.removeInput(connection);
                ExternalNodeChangeCommand cmd = new ExternalNodeChangeCommand((Node) target, externalNode);
                cmd.execute();
            }
            INodeConnector nodeConnectorSource, nodeConnectorTarget;
            nodeConnectorSource = connection.getSourceNodeConnector();
            if (nodeConnectorSource != null) {
                nodeConnectorSource.setCurLinkNbOutput(nodeConnectorSource.getCurLinkNbOutput() - 1);
            }
            nodeConnectorTarget = connection.getTargetNodeConnector();
            nodeConnectorTarget.setCurLinkNbInput(nodeConnectorTarget.getCurLinkNbInput() - 1);
        }
        process.checkStartNodes();
        process.checkProcess();
    }

    @Override
    public void undo() {
        Process process = (Process) connectionList.get(0).getSource().getProcess();
        for (Connection connection : connectionList) {
            collpseJoblet(connection);
            ConnectionDeletedInfo deletedInfo = connectionDeletedInfosMap.get(connection);
            INode source = connection.getSource();
            if (deletedInfo != null) {
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
            connection.reconnect();
            INode target = connection.getTarget();
            if (source.isExternalNode()) {
                IExternalNode externalNode = ExternalUtilities.getExternalNodeReadyToOpen((Node)source);
                externalNode.addOutput(connection);
                ExternalNodeChangeCommand cmd = new ExternalNodeChangeCommand((Node) source, externalNode);
                cmd.execute();
            }
            if (target.isExternalNode()) {
                IExternalNode externalNode = ExternalUtilities.getExternalNodeReadyToOpen((Node)target);
                if (GlobalServiceRegister.getDefault().isServiceRegistered(IDbMapService.class)) {
                    IDbMapService service = (IDbMapService) GlobalServiceRegister.getDefault().getService(IDbMapService.class);
                    service.undoConnectionDelete(target.getExternalNode(), externalEMFData, connection.getName());
                }
                externalNode.addInput(connection);
                ExternalNodeChangeCommand cmd = new ExternalNodeChangeCommand((Node) target, externalNode);
                cmd.execute();
            }
            INodeConnector nodeConnectorSource, nodeConnectorTarget;
            nodeConnectorSource = connection.getSourceNodeConnector();
            if (nodeConnectorSource != null) {
                nodeConnectorSource.setCurLinkNbOutput(nodeConnectorSource.getCurLinkNbOutput() + 1);
            }

            nodeConnectorTarget = connection.getTargetNodeConnector();
            if (nodeConnectorTarget != null) {
                nodeConnectorTarget.setCurLinkNbInput(nodeConnectorTarget.getCurLinkNbInput() + 1);
            }

        }
        process.checkStartNodes();
        process.checkProcess();
    }

    private boolean deleteExpendNode(Connection connection) {
        final INode target = connection.getTarget();
        if (target instanceof Node && ((Node) target).getNodeContainer() != null) {
            if (((Node) target).getJobletNode() != null) {
                MessageDialog.openWarning(Display.getCurrent().getActiveShell(),
                        Messages.getString("ConnectionDeleteCommand.COLLAPSE"), //$NON-NLS-1$
                        Messages.getString("ConnectionDeleteCommand.COLLAPSEJOBLET")); //$NON-NLS-1$
                return true;
            }
            ((Node) target).getNodeContainer().getInputs().remove(connection);
        }
        final INode source = connection.getSource();
        if (source instanceof Node && ((Node) source).getNodeContainer() != null) {
            if (((Node) source).getJobletNode() != null) {
                MessageDialog.openWarning(Display.getCurrent().getActiveShell(),
                        Messages.getString("ConnectionDeleteCommand.COLLAPSE"), //$NON-NLS-1$
                        Messages.getString("ConnectionDeleteCommand.COLLAPSEJOBLET")); //$NON-NLS-1$
                return true;
            }
            ((Node) source).getNodeContainer().getOutputs().remove(connection);
        }
        return false;
    }

    private void collpseJoblet(Connection conn) {
        INode source = conn.getSource();
        if ((source instanceof Node) && ((Node) source).isJoblet()
                && (((Node) source).getNodeContainer() instanceof AbstractJobletContainer)) {
            ((AbstractJobletContainer) ((Node) source).getNodeContainer()).setCollapsed(true);
        }
        INode target = conn.getTarget();
        if ((target instanceof Node) && ((Node) target).isJoblet()
                && (((Node) target).getNodeContainer() instanceof AbstractJobletContainer)) {
            ((AbstractJobletContainer) ((Node) target).getNodeContainer()).setCollapsed(true);
        }
    }

    private class ConnectionDeletedInfo {

        public int metadataTableIndex = -1;

        public IMetadataTable metadataTable = null;

    }
}
