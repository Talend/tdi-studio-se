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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.gef.Request;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.editpolicies.ComponentEditPolicy;
import org.eclipse.gef.requests.GroupRequest;
import org.talend.core.model.process.INode;
import org.talend.core.model.process.IProcess2;
import org.talend.designer.core.ui.editor.cmd.DeleteNodeContainerCommand;

/**
 * Edit policy that will manage the deletion of a node and the changement of status. <br/>
 *
 * $Id$
 *
 */
public class NodeEditPolicy extends ComponentEditPolicy {

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.gef.editpolicies.ComponentEditPolicy#getCommand(org.eclipse.gef.Request)
     */
    @Override
    public Command getCommand(Request request) {
        return super.getCommand(request);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.gef.editpolicies.ComponentEditPolicy#createDeleteCommand(org.eclipse.gef.requests.GroupRequest)
     */
    @Override
    protected Command createDeleteCommand(GroupRequest request) {
        if (((Node) getHost().getModel()).isReadOnly()) {
            return null;
        }
        List<INode> nodeList = new ArrayList<INode>();
        for (int i = 0; i < request.getEditParts().size(); i++) {
            if (request.getEditParts().get(i) instanceof NodePart) {
                INode node = ((INode) ((NodePart) request.getEditParts().get(i)).getModel());
                if (node.isReadOnly()) {
                    continue;
                }
                if (!nodeList.contains(node)) {
                    nodeList.add(node);
                }
            }
        }
        this.getHost().getViewer().deselectAll();

        DeleteNodeContainerCommand deleteCommand = new DeleteNodeContainerCommand((IProcess2) nodeList.get(0).getProcess(),
                nodeList);
        return deleteCommand;
    }
}
