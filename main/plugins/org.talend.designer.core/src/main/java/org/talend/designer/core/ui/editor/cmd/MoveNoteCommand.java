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
import org.eclipse.gef.commands.Command;
import org.talend.designer.core.i18n.Messages;
import org.talend.designer.core.ui.editor.notes.Note;

/**
 * Move a given node to another location. <br/>
 *
 * $Id: MoveNodeCommand.java 2102 2007-02-22 08:33:22 +0000 (jeu., 22 févr. 2007) cantoine $
 *
 */
public class MoveNoteCommand extends Command {

    private Note note;

    private Point newPos;

    private Point oldPos;

    /**
     * Move the given node to another location.
     *
     * @param node
     * @param newPos
     */
    public MoveNoteCommand(Note note, Point newPos) {
        this.note = note;
        this.newPos = newPos;
        setLabel(Messages.getString("MoveNodeCommand.Label")); //$NON-NLS-1$
    }

    public void execute() {
        oldPos = note.getLocation();
        note.setLocation(newPos);
    }

    public void undo() {
        note.setLocation(oldPos);
    }

    public void redo() {
        note.setLocation(newPos);
    }
}
