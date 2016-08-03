package org.talend.designer.core.ui.editor.jobletcontainer;

import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.requests.CreateRequest;
import org.talend.designer.core.ui.editor.cmd.CreateNodeContainerCommand;
import org.talend.designer.core.ui.editor.cmd.CreateNoteCommand;
import org.talend.designer.core.ui.editor.nodecontainer.NodeContainer;
import org.talend.designer.core.ui.editor.nodecontainer.NodeContainerLayoutEditPolicy;
import org.talend.designer.core.ui.editor.nodes.Node;
import org.talend.designer.core.ui.editor.notes.Note;
import org.talend.designer.core.ui.editor.process.Process;

public class JobletContainerLayoutEditPolicy extends NodeContainerLayoutEditPolicy {

    @Override
    protected Command getCreateCommand(CreateRequest request) {
        Rectangle constraint = (Rectangle) getConstraintFor(request);
        Process linkedProcess = (Process) ((JobletContainer) getHost().getModel()).getNode().getProcess();

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

}
