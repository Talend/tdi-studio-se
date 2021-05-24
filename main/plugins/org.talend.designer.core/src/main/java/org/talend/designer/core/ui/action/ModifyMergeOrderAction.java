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
import org.talend.designer.core.i18n.Messages;
import org.talend.designer.core.ui.dialog.mergeorder.MergeOrderDialog;
import org.talend.designer.core.ui.editor.cmd.ChangeMergeOrderCommand;
import org.talend.designer.core.ui.editor.connections.ConnLabelEditPart;
import org.talend.designer.core.ui.editor.connections.Connection;
import org.talend.designer.core.ui.editor.connections.ConnectionPart;
import org.talend.designer.core.ui.editor.nodes.Node;
import org.talend.designer.core.ui.editor.nodes.NodePart;

/**
 * DOC nrousseau class global comment. Detailled comment <br/>
 *
 */
public class ModifyMergeOrderAction extends SelectionAction {

    public static final String ID = "org.talend.designer.core.ui.editor.action.ModifyMergeOrderAction"; //$NON-NLS-1$

    private static final String TEXT_MERGE_ORDER = Messages.getString("ModifyMergeOrderAction.actionText"); //$NON-NLS-1$

    private INode mergeComponent;

    public ModifyMergeOrderAction(IWorkbenchPart part) {
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

        INode node = null;
        if (o instanceof ConnectionPart) {
            ConnectionPart part = (ConnectionPart) o;
            IConnection connection = (IConnection) part.getModel();
            node = connection.getTarget();
        } else if (o instanceof ConnLabelEditPart) {
            ConnectionPart part = (ConnectionPart) ((ConnLabelEditPart) o).getParent();
            Connection connection = (Connection) part.getModel();
            node = connection.getTarget();
        } else if (o instanceof NodePart) {
            node = (Node) ((NodePart) o).getModel();
        }

        if ((node == null) || (!node.getComponent().useMerge())) {
            return false;
        }
        mergeComponent = node;
        setText(TEXT_MERGE_ORDER);

        return true;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.jface.action.Action#run()
     */
    @Override
    public void run() {
        super.run();

        MergeOrderDialog dialog = new MergeOrderDialog(this.getWorkbenchPart().getSite().getShell(), mergeComponent);
        if (dialog.open() == MergeOrderDialog.OK) {
            for (IConnection connection : mergeComponent.getIncomingConnections()) {
                if (connection.getSource().isStart()
                        && (connection.getSource().getOutgoingConnections(EConnectionType.ON_SUBJOB_ERROR).size() != 0 || connection
                                .getSource().getOutgoingConnections(EConnectionType.ON_SUBJOB_OK).size() != 0)) {
                    MessageDialog.openError(DisplayUtils.getDefaultShell(false),
                            Messages.getString("ModifyMergeOrderAction.ERROE"), //$NON-NLS-1$
                            Messages.getString("ModifyMergeOrderAction.ConnectionModifyError2")); //$NON-NLS-1$
                    return;
                }
                if (connection.getSource().getIncomingConnections(EConnectionType.ON_SUBJOB_ERROR).size() != 0
                        || connection.getSource().getIncomingConnections(EConnectionType.ON_SUBJOB_OK).size() != 0) {
                    MessageDialog.openError(DisplayUtils.getDefaultShell(false),
                            Messages.getString("ModifyMergeOrderAction.ERROE"), //$NON-NLS-1$
                            Messages.getString("ModifyMergeOrderAction.ConnectionModifyError2")); //$NON-NLS-1$
                    return;
                }
            }
            ChangeMergeOrderCommand cmd = new ChangeMergeOrderCommand(mergeComponent, dialog.getConnectionList());
            execute(cmd);
        }
    }

}
