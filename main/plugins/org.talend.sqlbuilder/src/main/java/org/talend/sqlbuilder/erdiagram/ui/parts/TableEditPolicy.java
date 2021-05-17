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
package org.talend.sqlbuilder.erdiagram.ui.parts;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.gef.commands.Command;
import org.eclipse.gef.editpolicies.ComponentEditPolicy;
import org.eclipse.gef.requests.GroupRequest;
import org.talend.sqlbuilder.erdiagram.ui.commands.DeleteTableCommand;
import org.talend.sqlbuilder.erdiagram.ui.nodes.ErDiagram;
import org.talend.sqlbuilder.erdiagram.ui.nodes.Table;

/**
 * DOC qzhang class global comment. Detailled comment <br/>
 *
 * $Id: talend.epf 1 2006-09-29 17:06:40 +0000 (ææäº, 29 ä¹æ 2006) nrousseau $
 *
 */
public class TableEditPolicy extends ComponentEditPolicy {

    @Override
    protected Command createDeleteCommand(GroupRequest deleteRequest) {
        Object parent = getHost().getParent().getModel();
        if (!(parent instanceof ErDiagram)) {
            return null;
        }
        List<Table> nodeList = new ArrayList<Table>();
        for (int i = 0; i < deleteRequest.getEditParts().size(); i++) {
            if (deleteRequest.getEditParts().get(i) instanceof TablePart) {
                nodeList.add((Table) ((TablePart) deleteRequest.getEditParts().get(i)).getModel());
            }
        }
        DeleteTableCommand command = new DeleteTableCommand((ErDiagram) parent, nodeList);
        return command;
    }

}
