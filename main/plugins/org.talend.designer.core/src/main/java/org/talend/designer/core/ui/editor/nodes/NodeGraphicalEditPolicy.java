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
package org.talend.designer.core.ui.editor.nodes;

import java.util.List;

import org.eclipse.draw2d.ConnectionRouter;
import org.eclipse.gef.Request;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.editpolicies.GraphicalNodeEditPolicy;
import org.eclipse.gef.requests.CreateConnectionRequest;
import org.eclipse.gef.requests.ReconnectRequest;
import org.talend.core.model.process.ProcessUtils;
import org.talend.designer.core.DesignerPlugin;
import org.talend.designer.core.ui.editor.cmd.ConnectionCreateCommand;
import org.talend.designer.core.ui.editor.cmd.ConnectionReconnectCommand;
import org.talend.designer.core.ui.editor.connections.Connection;
import org.talend.designer.core.ui.editor.connections.TalendBorderItemRectilinearRouter;
import org.talend.designer.core.ui.editor.connections.TalendDummyConnection;
import org.talend.designer.core.ui.preferences.TalendDesignerPrefConstants;

/**
 * Edit policy that will allow connections to connect to the node. <br/>
 *
 * $Id$
 *
 */
public class NodeGraphicalEditPolicy extends GraphicalNodeEditPolicy {

    @Override
    protected Command getConnectionCompleteCommand(CreateConnectionRequest request) {
        ConnectionCreateCommand cmd = (ConnectionCreateCommand) request.getStartCommand();
        cmd.setTarget((Node) getHost().getModel());
        return cmd;
    }

    @Override
    @SuppressWarnings("unchecked")
    protected Command getConnectionCreateCommand(CreateConnectionRequest request) {
        Node source = (Node) getHost().getModel();
        if (checkConnectionStatus(source)) {
            return null;
        }
        String style = (String) request.getNewObjectType();
        ConnectionCreateCommand cmd = new ConnectionCreateCommand(source, style, (List<Object>) request.getNewObject());
        request.setStartCommand(cmd);
        return cmd;
    }

    @Override
    protected Command getReconnectSourceCommand(ReconnectRequest request) {
        Connection conn = (Connection) request.getConnectionEditPart().getModel();
        Node newSource = (Node) getHost().getModel();
        if (checkConnectionStatus(newSource)) {
            return null;
        }
        ConnectionReconnectCommand cmd = new ConnectionReconnectCommand(conn);
        cmd.setNewSource(newSource);
        return cmd;
    }

    @Override
    protected Command getReconnectTargetCommand(ReconnectRequest request) {
        Connection conn = (Connection) request.getConnectionEditPart().getModel();
        Node newTarget = (Node) getHost().getModel();
        if (checkConnectionStatus(newTarget)) {
            return null;
        }

        ConnectionReconnectCommand cmd = new ConnectionReconnectCommand(conn);
        cmd.setNewTarget(newTarget);
        return cmd;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.gef.editpolicies.GraphicalNodeEditPolicy#createDummyConnection(org.eclipse.gef.Request)
     */
    @Override
    protected org.eclipse.draw2d.Connection createDummyConnection(Request req) {
        if (DesignerPlugin.getDefault().getPreferenceStore().getBoolean(TalendDesignerPrefConstants.EDITOR_LINESTYLE)) {
            TalendDummyConnection dummyConn = new TalendDummyConnection();
            dummyConn.setRoundedBendpointsRadius(32);
            return dummyConn;
        }
        return super.createDummyConnection(req);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.gef.editpolicies.GraphicalNodeEditPolicy#getDummyConnectionRouter(org.eclipse.gef.requests.
     * CreateConnectionRequest)
     */
    @Override
    protected ConnectionRouter getDummyConnectionRouter(CreateConnectionRequest request) {
        if (DesignerPlugin.getDefault().getPreferenceStore().getBoolean(TalendDesignerPrefConstants.EDITOR_LINESTYLE)) {
            return new TalendBorderItemRectilinearRouter(request);
        }
        return super.getDummyConnectionRouter(request);
    }

    private boolean checkConnectionStatus(Node node) {
        if (node.isReadOnly() && !ProcessUtils.isTestContainer(node.getProcess())) {
            return true;
        }
        return false;
    }
}
