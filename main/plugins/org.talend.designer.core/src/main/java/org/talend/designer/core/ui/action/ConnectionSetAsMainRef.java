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
package org.talend.designer.core.ui.action;

import java.util.List;

import org.eclipse.gef.ui.actions.SelectionAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.IWorkbenchPart;
import org.talend.commons.ui.gmf.util.DisplayUtils;
import org.talend.core.model.process.EConnectionType;
import org.talend.core.model.process.IConnection;
import org.talend.core.model.process.INode;
import org.talend.core.model.utils.NodeUtil;
import org.talend.designer.core.i18n.Messages;
import org.talend.designer.core.ui.editor.cmd.ChangeConnectionStatusCommand;
import org.talend.designer.core.ui.editor.connections.ConnLabelEditPart;
import org.talend.designer.core.ui.editor.connections.Connection;
import org.talend.designer.core.ui.editor.connections.ConnectionPart;

/**
 * Action to set a connection to main or to ref.
 *
 * $Id$
 *
 */
public class ConnectionSetAsMainRef extends SelectionAction {

    public static final String ID = "org.talend.designer.core.ui.editor.action.ConnectionSetAsMainRef"; //$NON-NLS-1$

    private static final String TEXT_SET_MAIN = Messages.getString("ConnectionSetAsMainRef.mainLabel"); //$NON-NLS-1$

    private static final String TEXT_SET_REF = Messages.getString("ConnectionSetAsMainRef.lookupLabel"); //$NON-NLS-1$

    Connection connection;

    public ConnectionSetAsMainRef(IWorkbenchPart part) {
        super(part);
        setId(ID);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.gef.ui.actions.WorkbenchPartAction#calculateEnabled()
     */
    @Override
    protected boolean calculateEnabled() {
        if (getSelectedObjects().isEmpty()) {
            return false;
        }
        List parts = getSelectedObjects();
        if (parts.size() == 1) {
            Object o = parts.get(0);
            /*
             * if (!(o instanceof ConnectionPart) && !(o instanceof ConnLabelEditPart)) { return false; }
             */
            ConnectionPart part = null;
            if (o instanceof ConnectionPart) {
                part = (ConnectionPart) o;
            } else {
                if (o instanceof ConnLabelEditPart) {
                    part = (ConnectionPart) ((ConnLabelEditPart) o).getParent();
                } else {
                    return false;
                }
            }

            if (!(part.getModel() instanceof Connection)) {
                return false;
            }
            connection = (Connection) part.getModel();

            if (!connection.isActivate()) {
                return false;
            }

            if (connection.getLineStyle().equals(EConnectionType.FLOW_MAIN)) {
                int nbTargetFlowIn = 0;
                INode node = connection.getTarget();
                for (IConnection currentConnec : node.getIncomingConnections()) {
                    if (currentConnec.getLineStyle().equals(EConnectionType.FLOW_MAIN)
                            || currentConnec.getLineStyle().equals(EConnectionType.FLOW_REF)) {
                        nbTargetFlowIn++;
                    }
                }
                if (nbTargetFlowIn <= 1) {
                    return false;
                }
                setText(TEXT_SET_REF);
            } else {
                if (connection.getLineStyle().equals(EConnectionType.FLOW_REF)) {
                    setText(TEXT_SET_MAIN);
                } else {
                    return false;
                }
            }

            return true;
        }
        return false;
    }

    public void run() {
        if (connection.getSource().getDesignSubjobStartNode().getOutgoingConnections(EConnectionType.ON_SUBJOB_OK).size() != 0
                || connection.getSource().getDesignSubjobStartNode().getOutgoingConnections(EConnectionType.ON_SUBJOB_ERROR)
                        .size() != 0) {
            MessageDialog
                    .openError(
                            DisplayUtils.getDefaultShell(false),
                            Messages.getString("ConnectionSetAsMainRef.ERROR"), //$NON-NLS-1$
                            Messages.getString("ConnectionSetAsMainRef.ConnectionModifyError2")); //$NON-NLS-1$
            return;
        }
        if (checkConnectionStatus(connection.getTarget())) {
            MessageDialog
                    .openError(
                            DisplayUtils.getDefaultShell(false),
                            Messages.getString("ConnectionSetAsMainRef.ERROR"), //$NON-NLS-1$
                            Messages.getString("ConnectionSetAsMainRef.ConnectionModifyError2")); //$NON-NLS-1$
            return;
        }
        ChangeConnectionStatusCommand cmd = new ChangeConnectionStatusCommand(connection);
        execute(cmd);
    }

    public boolean checkConnectionStatus(INode node) {
        if (node != null) {
            if (!node.isDesignSubjobStartNode()) {
                return checkConnectionStatus(NodeUtil.getMainIncomingConnections(node));
            } else {
                return node.getIncomingConnections(EConnectionType.ON_SUBJOB_OK).size() != 0
                        || node.getIncomingConnections(EConnectionType.ON_SUBJOB_ERROR).size() != 0;
            }
        }
        return false;
    }
}
