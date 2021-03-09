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

import org.eclipse.gef.commands.Command;
import org.eclipse.gef.editpolicies.DirectEditPolicy;
import org.eclipse.gef.requests.DirectEditRequest;
import org.talend.designer.core.ui.editor.cmd.ChangeNodeTextCommand;

/**
 * Edit policy that will allow to edit the text of the label. <br/>
 *
 * $Id$
 *
 */
public class NodeTextEditPolicy extends DirectEditPolicy {

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.gef.editpolicies.DirectEditPolicy#getDirectEditCommand(org.eclipse.gef.requests.DirectEditRequest)
     */
    protected Command getDirectEditCommand(DirectEditRequest edit) {
        String labelText = (String) edit.getCellEditor().getValue();
        NodeLabelEditPart labelPart = (NodeLabelEditPart) getHost();
        ChangeNodeTextCommand command = new ChangeNodeTextCommand((NodeLabel) labelPart.getModel(), labelText);
        return command;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.gef.editpolicies.DirectEditPolicy#showCurrentEditValue(org.eclipse.gef.requests.DirectEditRequest)
     */
    protected void showCurrentEditValue(DirectEditRequest request) {

    }

}
