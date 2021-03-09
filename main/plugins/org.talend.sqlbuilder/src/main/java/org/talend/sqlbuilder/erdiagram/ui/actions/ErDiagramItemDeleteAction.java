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
package org.talend.sqlbuilder.erdiagram.ui.actions;

import java.util.List;

import org.eclipse.gef.EditPart;
import org.eclipse.gef.RequestConstants;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.requests.GroupRequest;
import org.eclipse.gef.ui.actions.DeleteAction;
import org.eclipse.ui.IWorkbenchPart;
import org.talend.sqlbuilder.erdiagram.ui.editor.ErdiagramDiagramEditor;
import org.talend.sqlbuilder.erdiagram.ui.parts.RelationPart;
import org.talend.sqlbuilder.erdiagram.ui.parts.TablePart;

/**
 * DOC qzhang class global comment. Detailled comment <br/>
 *
 * $Id: talend.epf 1 2006-09-29 17:06:40 +0000 (ææäº, 29 ä¹æ 2006) nrousseau $
 *
 */
public class ErDiagramItemDeleteAction extends DeleteAction {

    private IWorkbenchPart part;

    /**
     * DOC admin TableDeleteAction constructor comment.
     *
     * @param part
     */
    public ErDiagramItemDeleteAction(IWorkbenchPart part) {
        super(part);
        this.part = part;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.gef.ui.actions.DeleteAction#calculateEnabled()
     */
    @Override
    protected boolean calculateEnabled() {
        List objects = getSelectedObjects();
        if (objects.isEmpty()) {
            return false;
        }
        if (!(objects.get(0) instanceof TablePart || objects.get(0) instanceof RelationPart)) {
            return false;
        }
        return true;
    }

    public Command createDeleteCommand(List objects) {
        if (objects.isEmpty()) {
            return null;
        }
        if (!(objects.get(0) instanceof EditPart)) {
            return null;
        }
        GroupRequest deleteReq = new GroupRequest(RequestConstants.REQ_DELETE);
        deleteReq.setEditParts(objects);

        EditPart object = (EditPart) objects.get(0);
        Command cmd = object.getCommand(deleteReq);
        return cmd;

    }

    public void update() {
        setSelection(((ErdiagramDiagramEditor) part).getViewer().getSelection());
    }

}
