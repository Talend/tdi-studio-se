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
package org.talend.designer.business.diagram.custom.edit.policies;

import org.eclipse.gef.commands.Command;
import org.eclipse.gef.commands.UnexecutableCommand;
import org.eclipse.gmf.runtime.diagram.ui.commands.ICommandProxy;
import org.eclipse.gmf.runtime.diagram.ui.editparts.IGraphicalEditPart;
import org.eclipse.gmf.runtime.diagram.ui.editpolicies.DiagramDragDropEditPolicy;
import org.eclipse.gmf.runtime.diagram.ui.requests.DropObjectsRequest;
import org.talend.designer.business.diagram.custom.commands.CreateAssignmentCommand;
import org.talend.designer.business.diagram.custom.util.ElementHelper;
import org.talend.designer.business.model.business.BusinessItem;

/**
 * DOC mhelleboid class global comment. Detailled comment <br/>
 *
 * $Id$
 *
 */
public class BusinessItemDragDropEditPolicy extends DiagramDragDropEditPolicy {

    /*
     * (non-Javadoc)
     *
     * @see
     * org.eclipse.gmf.runtime.diagram.ui.editpolicies.DiagramDragDropEditPolicy#getDropObjectsCommand(org.eclipse.gmf
     * .runtime.diagram.ui.requests.DropObjectsRequest)
     */
    @Override
    public Command getDropObjectsCommand(DropObjectsRequest dropObjectsRequest) {
        BusinessItem businessItem = new ElementHelper().getElement(getHost());

        if (businessItem != null) {
            CreateAssignmentCommand createAssignmentCommand = new CreateAssignmentCommand(((IGraphicalEditPart) getHost())
                    .getEditingDomain());
            createAssignmentCommand.setBusinessItem(businessItem);

            createAssignmentCommand.setItems(dropObjectsRequest.getObjects());

            return new ICommandProxy(createAssignmentCommand);
        }

        return UnexecutableCommand.INSTANCE;
    }
}
