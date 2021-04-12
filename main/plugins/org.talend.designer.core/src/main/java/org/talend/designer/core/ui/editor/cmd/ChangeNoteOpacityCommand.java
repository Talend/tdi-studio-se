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

import org.eclipse.gef.commands.Command;
import org.talend.designer.core.i18n.Messages;
import org.talend.designer.core.ui.editor.notes.Note;

/**
 */
public class ChangeNoteOpacityCommand extends Command {

    private Note note;

    private boolean oldState;

    private boolean newState;

    public ChangeNoteOpacityCommand(Note note, boolean newState) {
        super(Messages.getString("ChangeNoteOpacityCommand.Name")); //$NON-NLS-1$
        this.note = note;
        this.newState = newState;
    }

    @Override
    public void execute() {
        oldState = note.isOpaque();
        note.setOpaque(newState);
    }

    @Override
    public void undo() {
        note.setOpaque(oldState);
    }
}
