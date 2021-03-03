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
package org.talend.designer.core.ui.editor.notes;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.gef.commands.Command;
import org.eclipse.gef.editpolicies.ComponentEditPolicy;
import org.eclipse.gef.requests.GroupRequest;
import org.talend.designer.core.ui.editor.cmd.DeleteNoteCommand;
import org.talend.designer.core.ui.editor.process.Process;

/**
 */
public class NoteEditPolicy extends ComponentEditPolicy {

    @Override
    protected Command createDeleteCommand(GroupRequest request) {
        Object parent = getHost().getParent().getModel();
        if (!(parent instanceof Process)) {
            return null;
        }

        Note note = (Note) getHost().getModel();
        if (note.isReadOnly()) {
            return null;
        }

        List<Note> noteList = new ArrayList<Note>();
        for (int i = 0; i < request.getEditParts().size(); i++) {
            if (request.getEditParts().get(i) instanceof NoteEditPart) {
                noteList.add(((Note) ((NoteEditPart) request.getEditParts().get(i)).getModel()));
            }
        }

        DeleteNoteCommand deleteCommand = new DeleteNoteCommand((Process) parent, noteList);
        return deleteCommand;
    }
}
