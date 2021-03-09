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
import org.eclipse.ui.IWorkbenchPart;
import org.talend.core.model.process.EConnectionType;
import org.talend.core.model.process.IConnectionCategory;
import org.talend.core.model.process.INode;
import org.talend.designer.core.i18n.Messages;
import org.talend.designer.core.ui.dialog.mergeorder.ModifyOutputOrderDialog;
import org.talend.designer.core.ui.editor.cmd.ChangeOutputConnectionOrderCommand;
import org.talend.designer.core.ui.editor.connections.ConnLabelEditPart;
import org.talend.designer.core.ui.editor.connections.Connection;
import org.talend.designer.core.ui.editor.connections.ConnectionPart;
import org.talend.designer.core.ui.editor.nodes.Node;
import org.talend.designer.core.ui.editor.nodes.NodePart;

/**
 * yzhang class global comment. Detailled comment
 */
public class ModifyOutputOrderAction extends SelectionAction {

    public static final String ID = "org.talend.designer.core.ui.editor.action.ModifyOutputOrderAction"; //$NON-NLS-1$

    private INode multipleOutputNode;

    private boolean usedConnType = false;

    /**
     * yzhang ModifyOutputOrderAction constructor comment.
     *
     * @param part
     * @param style
     */
    public ModifyOutputOrderAction(IWorkbenchPart part) {
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
        if (parts.size() != 1) {
            return false;
        }
        Object o = parts.get(0);
        EConnectionType tmpConnType = null;
        INode node = null;
        if (o instanceof ConnectionPart) {
            ConnectionPart part = (ConnectionPart) o;
            Connection connection = (Connection) part.getModel();
            node = connection.getSource();
            tmpConnType = connection.getLineStyle();
        } else if (o instanceof ConnLabelEditPart) {
            ConnectionPart part = (ConnectionPart) ((ConnLabelEditPart) o).getParent();
            Connection connection = (Connection) part.getModel();
            node = connection.getSource();
            tmpConnType = connection.getLineStyle();
        } else if (o instanceof NodePart) {
            node = (Node) ((NodePart) o).getModel();
        }

        if (node == null) {
            return false;
        }
        usedConnType = false;
        String midStr = "output"; //$NON-NLS-1$
        int nb = 0;
        for (Connection connection : (List<Connection>) node.getOutgoingConnections()) {
            if (getConnectionCategory() != null && connection.getLineStyle().hasConnectionCategory(getConnectionCategory())) {
                // avoid the not useful action, on connection.
                if (tmpConnType == null || tmpConnType != null && tmpConnType.hasConnectionCategory(getConnectionCategory())) {
                    nb++;
                }
            } else
            // feature 4505 & 8087
            if (getConnectionType() != null && connection.getLineStyle() == getConnectionType()) {
                midStr = getConnectionType().getDefaultLinkName();
                usedConnType = true;
                // avoid the not useful action, on connection.
                if (tmpConnType == null || tmpConnType != null && tmpConnType == getConnectionType()) {
                    nb++;
                }
            }
        }

        if (nb < 2) {
            return false;
        }
        multipleOutputNode = node;
        setText(Messages.getString("ModifyOutputOrderAction.text", midStr)); //$NON-NLS-1$
        return true;
    }

    protected Integer getConnectionCategory() {
        return IConnectionCategory.DATA;
    }

    protected EConnectionType getConnectionType() {
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.jface.action.Action#run()
     */
    @Override
    public void run() {
        super.run();
        ModifyOutputOrderDialog dialog = null;
        if (usedConnType && getConnectionType() != null) {
            dialog = new ModifyOutputOrderDialog(this.getWorkbenchPart().getSite().getShell(), multipleOutputNode,
                    getConnectionType());
        } else if (getConnectionCategory() != null) {
            dialog = new ModifyOutputOrderDialog(this.getWorkbenchPart().getSite().getShell(), multipleOutputNode,
                    getConnectionCategory());

        }

        if (dialog != null && dialog.open() == ModifyOutputOrderDialog.OK) {
            ChangeOutputConnectionOrderCommand cmd = new ChangeOutputConnectionOrderCommand(multipleOutputNode, dialog
                    .getConnectionList());
            execute(cmd);
        }
    }
}
