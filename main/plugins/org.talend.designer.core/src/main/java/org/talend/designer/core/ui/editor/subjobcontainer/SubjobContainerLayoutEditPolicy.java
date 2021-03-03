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
package org.talend.designer.core.ui.editor.subjobcontainer;

import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPolicy;
import org.eclipse.gef.Request;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.editpolicies.XYLayoutEditPolicy;
import org.eclipse.gef.requests.CreateRequest;
import org.talend.designer.core.ui.editor.cmd.CreateNodeContainerCommand;
import org.talend.designer.core.ui.editor.cmd.CreateNoteCommand;
import org.talend.designer.core.ui.editor.cmd.MoveNodeCommand;
import org.talend.designer.core.ui.editor.cmd.MoveNodeLabelCommand;
import org.talend.designer.core.ui.editor.cmd.MoveNoteCommand;
import org.talend.designer.core.ui.editor.jobletcontainer.JobletContainer;
import org.talend.designer.core.ui.editor.nodecontainer.NodeContainer;
import org.talend.designer.core.ui.editor.nodes.Node;
import org.talend.designer.core.ui.editor.nodes.NodeLabel;
import org.talend.designer.core.ui.editor.nodes.NodeLabelEditPart;
import org.talend.designer.core.ui.editor.nodes.NodePart;
import org.talend.designer.core.ui.editor.notes.Note;
import org.talend.designer.core.ui.editor.notes.NoteEditPart;
import org.talend.designer.core.ui.editor.process.Process;

/**
 * Edit policy of the Diagram that will allow to move the objects on it and create nodes. <br/>
 *
 * $Id: NodeContainerLayoutEditPolicy.java 7038 2007-11-15 14:05:48Z plegall $
 *
 */
public class SubjobContainerLayoutEditPolicy extends XYLayoutEditPolicy {

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.gef.editpolicies.ConstrainedLayoutEditPolicy#createChildEditPolicy(org.eclipse.gef.EditPart)
     */
    protected EditPolicy createChildEditPolicy(final EditPart child) {
        SubjobContainerResizableEditPolicy policy = new SubjobContainerResizableEditPolicy();
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
    protected Command createAddCommand(final EditPart child, final Object constraint) {
        if (child instanceof NoteEditPart) {
            if (((Note) child.getModel()).isReadOnly()) {
                return null;
            }
            MoveNoteCommand locationCommand = new MoveNoteCommand((Note) child.getModel(), ((Rectangle) constraint).getLocation());
            return locationCommand;
        }
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
            if (((Node) child.getModel()).isReadOnly()) {
                return null;
            }
            MoveNodeCommand locationCommand = new MoveNodeCommand((Node) child.getModel(), ((Rectangle) constraint).getLocation());
            return locationCommand;
        } else {
            return null;
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.eclipse.gef.editpolicies.ConstrainedLayoutEditPolicy#createChangeConstraintCommand(org.eclipse.gef.EditPart,
     * java.lang.Object)
     */
    protected Command createChangeConstraintCommand(final EditPart child, final Object constraint) {
        // return a command to move the part to the location given by the constraint

        if (child instanceof NoteEditPart) {
            if (((Note) child.getModel()).isReadOnly()) {
                return null;
            }
            MoveNoteCommand locationCommand = new MoveNoteCommand((Note) child.getModel(), ((Rectangle) constraint).getLocation());
            return locationCommand;
        }

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
            if (((Node) child.getModel()).isReadOnly()) {
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
    protected Command getCreateCommand(final CreateRequest request) {
        if (((SubjobContainer) getHost().getModel()).isReadOnly()) {
            return null;
        }
        Rectangle constraint = (Rectangle) getConstraintFor(request);
        Process linkedProcess = (Process) ((SubjobContainer) getHost().getModel()).getProcess();

        Command command = null;
        if (Note.class.equals(request.getNewObjectType())) {
            command = new CreateNoteCommand(linkedProcess, (Note) request.getNewObject(), constraint.getLocation());
        } else if (request.getNewObject() instanceof Node) {
            Node node = (Node) request.getNewObject();
            NodeContainer nodeContainer = ((Process)node.getProcess()).loadNodeContainer(node, false);
            command = new CreateNodeContainerCommand(linkedProcess, nodeContainer, constraint.getLocation());
        }

        return command;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.gef.editpolicies.LayoutEditPolicy#getDeleteDependantCommand(org.eclipse.gef.Request)
     */
    protected Command getDeleteDependantCommand(final Request request) {
        return null; // no support for deleting a dependant
    }
}
