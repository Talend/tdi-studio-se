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

import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.gef.commands.Command;
import org.talend.designer.core.ui.editor.connections.ConnectionTrace;

/**
 * Command that moves the label of a connection. <br/>
 *
 * $Id: MoveConnTextCommand.java 6924 2007-11-12 13:16:18Z plegall $
 *
 */
public class MoveConnTraceCommand extends Command {

    ConnectionTrace label = null;

    Point location = null;

    Figure parent = null;

    Point oldOffset = new Point();

    /**
     * Initialisation of the command. ConnectionLabel, Figure that will be the parent to calculate the position of the
     * label, delta of the move of the label after drag it
     *
     * @param label
     * @param parent
     * @param delta
     */
    public MoveConnTraceCommand(ConnectionTrace label, Figure parent, Point delta) {
        this.label = label;
        this.parent = parent;
        this.location = delta;
    }

    public void execute() {
        oldOffset = label.getOffset();
        Point newOffset = label.getOffset().getCopy();
        parent.translateToAbsolute(newOffset);
        newOffset.translate(location);
        parent.translateToRelative(newOffset);
        label.setOffset(newOffset);
    }

    public void redo() {
        execute();
    }

    public void undo() {
        label.setOffset(oldOffset);
    }
}
