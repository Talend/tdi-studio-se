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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.gef.ui.actions.SelectionAction;
import org.eclipse.ui.IWorkbenchPart;
import org.talend.designer.core.i18n.Messages;
import org.talend.designer.core.model.components.EParameterName;
import org.talend.designer.core.ui.editor.cmd.ChangeActivateStatusElementCommand;
import org.talend.designer.core.ui.editor.connections.ConnLabelEditPart;
import org.talend.designer.core.ui.editor.connections.Connection;
import org.talend.designer.core.ui.editor.connections.ConnectionLabel;
import org.talend.designer.core.ui.editor.connections.ConnectionPart;
import org.talend.designer.core.ui.editor.nodes.Node;
import org.talend.designer.core.ui.editor.nodes.NodePart;

/**
 * Action used to set the start status on a node with the context menu. <br/>
 *
 * $Id: NodeSetActivateAction.java 3351 2007-05-04 12:14:00 +0000 (ven., 04 mai 2007) plegall $
 *
 */
public class ActivateElementAction extends SelectionAction {

    public static final String ID = "org.talend.designer.core.ui.editor.action.ActivateElementAction"; //$NON-NLS-1$

    private static final String TEXT_SET_ACTIVATE = Messages.getString("NodeSetActivateAction.Activate"); //$NON-NLS-1$

    private static final String TEXT_REM_ACTIVATE = Messages.getString("NodeSetActivateAction.Deactivate"); //$NON-NLS-1$

    private List<Node> nodeList;

    private List<Connection> connectionList;

    private boolean activate = false;

    public ActivateElementAction(IWorkbenchPart part) {
        super(part);
        setId(ID);
    }

    @Override
    protected boolean calculateEnabled() {
        return canPerformAction();
    }

    /**
     * Test if the selected item is a node.
     *
     * @return true / false
     */
    private boolean canPerformAction() {
        if (getSelectedObjects().isEmpty()) {
            return false;
        }
        List parts = getSelectedObjects();
        if (parts.size() >= 1) {
            Object o = parts.get(0);

            nodeList = new ArrayList<Node>();
            for (int i = 0; i < parts.size(); i++) {
                if (parts.get(i) instanceof NodePart) {
                    Node node = ((Node) ((NodePart) parts.get(i)).getModel());
                    if ((!node.isReadOnly()) && (node.getPropertyValue(EParameterName.ACTIVATE.getName()) != null)) {
                        nodeList.add(node);
                    }
                }
            }

            connectionList = new ArrayList<Connection>();
            for (int i = 0; i < parts.size(); i++) {
                if (parts.get(i) instanceof ConnectionPart) {
                    Connection connection = ((Connection) ((ConnectionPart) parts.get(i)).getModel());
                    if (!connection.isReadOnly() && connection.getSource().isActivate() && connection.getTarget().isActivate()) {
                        connectionList.add(connection);
                    }
                }
                if (parts.get(i) instanceof ConnLabelEditPart) {
                    Connection connection = ((ConnectionLabel) ((ConnLabelEditPart) parts.get(i)).getModel()).getConnection();
                    if (!connection.isReadOnly() && connection.getSource().isActivate() && connection.getTarget().isActivate()) {
                        connectionList.add(connection);
                    }
                }
            }

            if ((nodeList.size() == 0) && (connectionList.size() == 0)) {
                return false;
            }

            for (Node node : nodeList) {
                if (node.getJobletNode() != null) {
                    return false;
                }
            }

            for (Connection conn : connectionList) {
                if (conn.getTarget().getJobletNode() != null && conn.getSource().getJobletNode() != null) {
                    return false;
                }
            }

            if (nodeList.size() != 0) {
                if (nodeList.get(0).isActivate()) {
                    if (nodeList.size() == 1) {
                        setText(TEXT_REM_ACTIVATE + " " + nodeList.get(0).getLabel()); //$NON-NLS-1$
                    } else {
                        setText(TEXT_REM_ACTIVATE);
                    }
                } else {
                    if (nodeList.size() == 1) {
                        setText(TEXT_SET_ACTIVATE + " " + nodeList.get(0).getLabel()); //$NON-NLS-1$
                    } else {
                        setText(TEXT_SET_ACTIVATE);
                    }
                    activate = true;
                }
            } else { // connections only
                if (connectionList.get(0).isActivate()) {
                    if (connectionList.size() == 1) {
                        setText(TEXT_REM_ACTIVATE + " " + connectionList.get(0).getName()); //$NON-NLS-1$
                    } else {
                        setText(TEXT_REM_ACTIVATE);
                    }
                } else {
                    if (connectionList.size() == 1) {
                        setText(TEXT_SET_ACTIVATE + " " + connectionList.get(0).getName()); //$NON-NLS-1$
                    } else {
                        setText(TEXT_SET_ACTIVATE);
                    }
                    activate = true;
                }
            }
            return true;
        }
        return false;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.jface.action.Action#run()
     */
    public void run() {
        List editparts = getSelectedObjects();
        if (editparts.size() >= 1) {
            ChangeActivateStatusElementCommand changeActivateStatusCommand = new ChangeActivateStatusElementCommand(activate,
                    nodeList, connectionList);
            execute(changeActivateStatusCommand);
        }
    }
}
