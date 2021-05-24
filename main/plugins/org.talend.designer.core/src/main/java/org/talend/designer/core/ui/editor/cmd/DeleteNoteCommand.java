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

import java.util.List;

import org.eclipse.gef.commands.Command;
import org.talend.designer.core.i18n.Messages;
import org.talend.designer.core.ui.editor.notes.Note;
import org.talend.designer.core.ui.editor.process.Process;

/**
 */
public class DeleteNoteCommand extends Command {

    private List<Note> noteList;

    private Process process;

    public DeleteNoteCommand(Process process, List<Note> noteList) {
        this.process = process;
        this.noteList = noteList;
        setLabel(Messages.getString("DeleteNoteCommand.Name")); //$NON-NLS-1$
    }

    @Override
    public void execute() {
        for (Note note : noteList) {
            process.removeNote(note);
        }
    }

    @Override
    public void undo() {
        for (Note note : noteList) {
            process.addNote(note);
        }
    }
}
