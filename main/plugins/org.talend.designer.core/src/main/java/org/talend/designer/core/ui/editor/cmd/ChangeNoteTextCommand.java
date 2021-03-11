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
public class ChangeNoteTextCommand extends Command {

    private Note note;

    private String newText;

    private String oldText;

    public ChangeNoteTextCommand(Note note, String newText) {
        super(Messages.getString("ChangeNoteTextCommand.Name")); //$NON-NLS-1$
        this.note = note;
        this.newText = newText;
    }

    @Override
    public void execute() {
        oldText = note.getText();
        note.setText(newText);
    }

    @Override
    public void undo() {
        note.setText(oldText);
    }
}
