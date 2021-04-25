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

import org.eclipse.gef.commands.Command;
import org.talend.core.model.process.EConnectionType;
import org.talend.core.model.process.INode;
import org.talend.designer.core.i18n.Messages;
import org.talend.designer.core.ui.editor.connections.Connection;
import org.talend.designer.core.ui.editor.process.Process;

/**
 * Command that will set or remove the start status on a node. <br/>
 *
 * $Id$
 *
 */
public class ChangeConnectionStatusCommand extends Command {

    private Connection connection;

    private Connection oldMainConnection = null;

    /**
     * Gives the node where the status will be set or removed.
     *
     * @param newStartNode
     */
    public ChangeConnectionStatusCommand(Connection connection) {
        this.connection = connection;
        if (connection.getLineStyle().equals(EConnectionType.FLOW_MAIN)) {
            setLabel(Messages.getString("ChangeConnectionStatusCommand.setLinkRef")); //$NON-NLS-1$
        } else {
            setLabel(Messages.getString("ChangeConnectionStatusCommand.setLinkMain")); //$NON-NLS-1$
        }
    }

    public void execute() {
        INode target = connection.getTarget();
        if (connection.getLineStyle().equals(EConnectionType.FLOW_MAIN)) {
            connection.setLineStyle(EConnectionType.FLOW_REF);
        } else {
            boolean found = false;
            for (int i = 0; i < target.getIncomingConnections().size() && !found; i++) {
                Connection currentConnection = (Connection) target.getIncomingConnections().get(i);
                if (currentConnection.getLineStyle().equals(EConnectionType.FLOW_MAIN)) {
                    oldMainConnection = currentConnection;
                    oldMainConnection.setLineStyle(EConnectionType.FLOW_REF);
                }
            }
            connection.setLineStyle(EConnectionType.FLOW_MAIN);
        }

        if (target.getExternalNode() != null) {
            target.getExternalNode().connectionStatusChanged(connection.getLineStyle(), connection.getUniqueName());
        }

        ((Process) connection.getSource().getProcess()).checkStartNodes();
        ((Process) connection.getSource().getProcess()).checkProcess();
    }

    public void undo() {
        if (connection.getLineStyle().equals(EConnectionType.FLOW_MAIN)) {
            if (oldMainConnection != null) {
                oldMainConnection.setLineStyle(EConnectionType.FLOW_MAIN);
            }
            connection.setLineStyle(EConnectionType.FLOW_REF);
        } else {
            connection.setLineStyle(EConnectionType.FLOW_MAIN);
        }
        ((Process) connection.getSource().getProcess()).checkStartNodes();
        ((Process) connection.getSource().getProcess()).checkProcess();
    }

    public void redo() {
        this.execute();
    }
}
