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
package org.talend.designer.core.ui.editor.cmd;

import org.eclipse.gef.EditPart;
import org.eclipse.gef.commands.Command;
import org.talend.core.model.process.Element;
import org.talend.designer.core.ui.editor.process.Process;

/**
 */
public abstract class ZorderCommand extends Command {

    private EditPart editPart;

    protected Element element;
    protected Process process;
    protected int oldIndex = -1;
    protected int size;

    public ZorderCommand(EditPart editPart) {
        this.editPart = editPart;

        element = (Element) editPart.getModel();
        process = (Process) editPart.getParent().getModel();
        oldIndex = process.getElements().indexOf(element);
        size = process.getElements().size();

        if (oldIndex == -1) {
            throw new IllegalStateException();
        }
    }

    @Override
    public void execute() {
        process.getElements().remove(element);
        process.getElements().add(getNewIndex(), element);

        editPart.getParent().refresh();
    }

    @Override
    public void undo() {
        Element element = (Element) editPart.getModel();
        Process process = (Process) editPart.getParent().getModel();

        process.getElements().remove(element);
        process.getElements().add(oldIndex, element);

        editPart.getParent().refresh();
    }

    protected abstract int getNewIndex();

    @Override
    public boolean canExecute() {
        return !process.isReadOnly() && subCanExecute();
    }

    protected abstract boolean subCanExecute();

}
