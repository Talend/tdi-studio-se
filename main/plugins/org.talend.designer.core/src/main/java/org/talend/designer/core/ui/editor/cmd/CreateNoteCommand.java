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

import org.eclipse.draw2d.geometry.Point;
import org.talend.designer.core.i18n.Messages;
import org.talend.designer.core.ui.editor.notes.Note;
import org.talend.designer.core.ui.editor.process.Process;

/**
 */
public class CreateNoteCommand extends CreateCommand {

    private Note note;

    public CreateNoteCommand(Process process, Note note, Point location) {
        super(Messages.getString("CreateNoteCommand.Name"), process, location); //$NON-NLS-1$
        this.note = note;
    }

    @Override
    public void execute() {
        if (location != null) {
            note.setLocation(this.location);
        }
        note.setProcess(process);
        process.addNote(note);
    }

    @Override
    public void undo() {
        process.removeNote(note);
    }
}
