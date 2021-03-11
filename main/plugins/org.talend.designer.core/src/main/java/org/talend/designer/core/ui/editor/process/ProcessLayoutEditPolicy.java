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
package org.talend.designer.core.ui.editor.process;

import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPolicy;
import org.eclipse.gef.GraphicalViewer;
import org.eclipse.gef.Request;
import org.eclipse.gef.RequestConstants;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.commands.CommandStack;
import org.eclipse.gef.commands.CompoundCommand;
import org.eclipse.gef.editpolicies.XYLayoutEditPolicy;
import org.eclipse.gef.requests.CreateConnectionRequest;
import org.eclipse.gef.requests.CreateRequest;
import org.talend.core.model.components.IComponent;
import org.talend.core.model.process.IProcess2;
import org.talend.core.ui.component.ComponentsFactoryProvider;
import org.talend.core.ui.process.IGraphicalNode;
import org.talend.designer.core.assist.TalendEditorComponentCreationUtil;
import org.talend.designer.core.assist.TalendEditorConnectionTargetAssist;
import org.talend.designer.core.ui.editor.cmd.ConnectionCreateCommand;
import org.talend.designer.core.ui.editor.cmd.CreateNodeContainerCommand;
import org.talend.designer.core.ui.editor.cmd.CreateNoteCommand;
import org.talend.designer.core.ui.editor.cmd.MoveNodeCommand;
import org.talend.designer.core.ui.editor.cmd.MoveNoteCommand;
import org.talend.designer.core.ui.editor.jobletcontainer.JobletContainer;
import org.talend.designer.core.ui.editor.nodecontainer.NodeContainer;
import org.talend.designer.core.ui.editor.nodes.Node;
import org.talend.designer.core.ui.editor.nodes.NodePart;
import org.talend.designer.core.ui.editor.notes.Note;
import org.talend.designer.core.ui.editor.notes.NoteEditPart;
import org.talend.designer.core.ui.editor.subjobcontainer.SubjobContainer;
import org.talend.designer.core.ui.editor.subjobcontainer.SubjobContainerPart;

/**
 * Edit policy of the Diagram that will allow to move the objects on it and create nodes. <br/>
 *
 * $Id$
 *
 */
public class ProcessLayoutEditPolicy extends XYLayoutEditPolicy {

    // ------------------------------------------------------------------------
    // Overridden from ConstrainedLayoutEditPolicy

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.gef.editpolicies.ConstrainedLayoutEditPolicy#createChildEditPolicy(org.eclipse.gef.EditPart)
     */
    @Override
    protected EditPolicy createChildEditPolicy(final EditPart child) {
        ProcessResizableEditPolicy policy = new ProcessResizableEditPolicy();
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
        if (child instanceof NodePart) {
            if (((Node) child.getModel()).isReadOnly() && !((Node) child.getModel()).isForceReadOnly()) {
                return null;
            }
            MoveNodeCommand locationCommand = new MoveNodeCommand((Node) child.getModel(), ((Rectangle) constraint).getLocation());
            return locationCommand;
        }
        // add for bug TDI-7706,when moving Node,the note can't move
        if (child instanceof NoteEditPart) {
            if (((Note) child.getModel()).isReadOnly()) {
                return null;
            }
            MoveNoteCommand locationCommand = new MoveNoteCommand((Note) child.getModel(), ((Rectangle) constraint).getLocation());
            return locationCommand;
        }

        return null;

        // Command moveContainer = new Command("test");
        // return moveContainer; // no support for adding
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.eclipse.gef.editpolicies.ConstrainedLayoutEditPolicy#createChangeConstraintCommand(org.eclipse.gef.EditPart,
     * java.lang.Object)
     */
    @Override
    public Command createChangeConstraintCommand(final EditPart child, final Object constraint) {
        // return a command to move the part to the location given by the constraint
        if (child instanceof NodePart) {
            if (((Node) child.getModel()).isReadOnly()) {
                return null;
            }
            MoveNodeCommand locationCommand = new MoveNodeCommand((Node) child.getModel(), ((Rectangle) constraint).getLocation());
            return locationCommand;
        }
        if (child instanceof NoteEditPart) {
            if (((Note) child.getModel()).isReadOnly()) {
                return null;
            }
            MoveNoteCommand locationCommand = new MoveNoteCommand((Note) child.getModel(), ((Rectangle) constraint).getLocation());
            return locationCommand;
        }
        if (child instanceof SubjobContainerPart) {
            SubjobContainer sjc = (SubjobContainer) child.getModel();
            Point sjcLocation = sjc.getSubjobContainerRectangle().getLocation();
            Point translationNeeded = new Point(((Rectangle) constraint).getLocation().x - sjcLocation.x,
                    ((Rectangle) constraint).getLocation().y - sjcLocation.y);
            CompoundCommand cc = new CompoundCommand();
            for (NodeContainer nc : sjc.getNodeContainers()) {
                if (nc.isReadOnly()) {
                    return null;
                }
                IGraphicalNode node = nc.getNode();
                Point nodeLocation = node.getLocation();
                MoveNodeCommand locationCommand = new MoveNodeCommand(nc.getNode(), nodeLocation.getTranslated(translationNeeded));
                cc.add(locationCommand);
            }
            return cc;
        }
        return null;
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
        if (((Process) getHost().getModel()).isReadOnly()) {
            return null;
        }
        Rectangle constraint = (Rectangle) getConstraintFor(request);

        Command command = null;
        if (Note.class.equals(request.getNewObjectType())) {
            command = new CreateNoteCommand((Process) getHost().getModel(), (Note) request.getNewObject(),
                    constraint.getLocation());
        } else if (request.getNewObject() instanceof Node) {
            Node node = (Node) request.getNewObject();
            NodeContainer nodeContainer = ((Process)node.getProcess()).loadNodeContainer(node, false);
            command = new CreateNodeContainerCommand((Process) getHost().getModel(), nodeContainer, constraint.getLocation());
        }

        return command;
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

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.gef.editpolicies.ConstrainedLayoutEditPolicy#getCommand(org.eclipse.gef.Request)
     */
    @Override
    public Command getCommand(Request request) {
        if (RequestConstants.REQ_CONNECTION_END.equals(request.getType()) && request instanceof CreateConnectionRequest) {
            return getConnectionAndEndCommands((CreateConnectionRequest) request);
        }
        return super.getCommand(request);
    }

    protected Command getConnectionAndEndCommands(CreateConnectionRequest request) {

        CompoundCommand cc = new CompoundCommand("CreateNodeCommand");

        ProcessPart processPart = (ProcessPart) this.getHost();

        final GraphicalViewer graphicalViewer = (GraphicalViewer) processPart.getViewer();
        final CommandStack commandStack = processPart.getViewer().getEditDomain().getCommandStack();
        final String categoryName = ComponentsFactoryProvider.getInstance().getComponentsHandler().extractComponentsCategory()
                .getName();
        final IProcess2 process = (IProcess2) processPart.getModel();
        TalendEditorConnectionTargetAssist assist = new TalendEditorConnectionTargetAssist(categoryName, graphicalViewer,
                commandStack, process);
        char start = '*';
        assist.showComponentCreationAssist(start);
        ConnectionCreateCommand cmd = (ConnectionCreateCommand) request.getStartCommand();
        if (assist.getComponentName() == null) {
            assist.releaseText();
            return cmd;
        }
        IComponent component = TalendEditorComponentCreationUtil.getComponentsInCategory(categoryName).get(
                assist.getComponentName());
        if (component == null) {
            assist.releaseText();
            return cmd;
        }
        assist.releaseText();
        Node newNode = new Node(component);
        NodeContainer nodeContainer = ((Process)newNode.getProcess()).loadNodeContainer(newNode, false);

        CreateNodeContainerCommand command = new CreateNodeContainerCommand(
                (org.talend.designer.core.ui.editor.process.Process) newNode.getProcess(), nodeContainer, request.getLocation());
        cc.add(command);

        cmd.setTarget(newNode);
        cc.add(cmd);

        return cc;
    }
}
