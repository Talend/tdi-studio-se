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

import org.eclipse.gef.EditPart;
import org.eclipse.gef.ui.actions.SelectionAction;
import org.eclipse.ui.IWorkbenchPart;
import org.talend.designer.core.ui.editor.cmd.ZorderCommand;
import org.talend.designer.core.ui.editor.notes.NoteEditPart;
import org.talend.designer.core.ui.editor.subjobcontainer.SubjobContainerPart;

/**
 */
public abstract class ZorderAction extends SelectionAction {

    protected ZorderCommand zorderCommand;

    protected EditPart editPart;

    public ZorderAction(IWorkbenchPart part) {
        super(part);
    }

    @Override
    protected boolean calculateEnabled() {
        List editparts = getSelectedObjects();
        if (editparts.size() == 1) {
            if (editparts.get(0) instanceof NoteEditPart || editparts.get(0) instanceof SubjobContainerPart) {
                editPart = (EditPart) editparts.get(0);
                createZOrderCommand();
                return zorderCommand.canExecute();
            }
        }
        return false;
    }

    @Override
    public void run() {
        execute(zorderCommand);
    }

    protected abstract void createZOrderCommand();
}
