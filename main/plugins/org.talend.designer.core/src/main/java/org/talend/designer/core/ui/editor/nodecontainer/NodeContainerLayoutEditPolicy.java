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
package org.talend.designer.core.ui.editor.nodecontainer;

import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPolicy;
import org.eclipse.gef.Request;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.editpolicies.XYLayoutEditPolicy;
import org.eclipse.gef.requests.CreateRequest;
import org.talend.designer.core.ui.editor.cmd.MoveNodeCommand;
import org.talend.designer.core.ui.editor.cmd.MoveNodeLabelCommand;
import org.talend.designer.core.ui.editor.nodes.Node;
import org.talend.designer.core.ui.editor.nodes.NodeLabel;
import org.talend.designer.core.ui.editor.nodes.NodeLabelEditPart;
import org.talend.designer.core.ui.editor.nodes.NodePart;

/**
 * Edit policy of the Diagram that will allow to move the objects on it and create nodes. <br/>
 *
 * $Id$
 *
 */
public class NodeContainerLayoutEditPolicy extends XYLayoutEditPolicy {

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.gef.editpolicies.ConstrainedLayoutEditPolicy#createChildEditPolicy(org.eclipse.gef.EditPart)
     */
    @Override
    protected EditPolicy createChildEditPolicy(final EditPart child) {
        NodeContainerResizableEditPolicy policy = new NodeContainerResizableEditPolicy();
        policy.setResizeDirections(0);
        return policy;
    }

    // ------------------------------------------------------------------------
    // Abstract methods from ConstrainedLayoutEditPolicy

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.gef.editpolicies.ConstrainedLayoutEditPolicy#createAddCommand(org.eclipse.gef.EditPart,
     * java.lang.Object)
     */
    @Override
    protected Command createAddCommand(final EditPart child, final Object constraint) {
        // only work for moving node. bug 6615, by cli
        if (child instanceof NodePart && (!((Node) child.getModel()).isReadOnly() || ((Node) child.getModel()).isForceReadOnly())) {

            MoveNodeCommand locationCommand = new MoveNodeCommand((Node) child.getModel(), ((Rectangle) constraint).getLocation());
            return locationCommand;
        }
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.eclipse.gef.editpolicies.ConstrainedLayoutEditPolicy#createChangeConstraintCommand(org.eclipse.gef.EditPart,
     * java.lang.Object)
     */
    @Override
    protected Command createChangeConstraintCommand(final EditPart child, final Object constraint) {
        // return a command to move the part to the location given by the constraint

        if (child instanceof NodeLabelEditPart) {
            boolean nodeSelected;
            // if (((NodeLabelEditPart) child).getNodePart().getSelected() != 0) {
            // nodeSelected = true;
            // } else {
            nodeSelected = false;
            // }
            MoveNodeLabelCommand locationCommand = new MoveNodeLabelCommand((NodeLabel) child.getModel(),
                    ((Rectangle) constraint).getLocation(), nodeSelected);
            return locationCommand;
        } else if (child instanceof NodePart) {
            if (((Node) child.getModel()).isReadOnly() && !((Node) child.getModel()).isForceReadOnly()) {
                return null;
            }
            MoveNodeCommand locationCommand = new MoveNodeCommand((Node) child.getModel(), ((Rectangle) constraint).getLocation());
            return locationCommand;
        } else {
            return null;
        }
    }

    // ------------------------------------------------------------------------
    // Abstract methods from LayoutEditPolicy

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.gef.editpolicies.LayoutEditPolicy#getCreateCommand(org.eclipse.gef.requests.CreateRequest)
     */
    @Override
    protected Command getCreateCommand(final CreateRequest request) {
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.gef.editpolicies.LayoutEditPolicy#getDeleteDependantCommand(org.eclipse.gef.Request)
     */
    @Override
    protected Command getDeleteDependantCommand(final Request request) {
        return null; // no support for deleting a dependant
    }
}
