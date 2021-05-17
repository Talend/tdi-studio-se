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
package org.talend.sqlbuilder.erdiagram.ui.commands;

import java.util.List;

import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.commands.Command;
import org.talend.sqlbuilder.erdiagram.ui.nodes.Table;

/**
 * qzhang class Move a given node to another location. <br/>
 *
 * $Id: MoveNodeCommand.java 1 2006-09-29 17:06:40 +0000 (星期五, 29 九月 2006) nrousseau $
 *
 */
public class EditTableCommand extends Command {

    private Table node;

    private Rectangle newPos = new Rectangle();

    private Rectangle oldPos = new Rectangle();

    /**
     * Move the given node to another location.
     *
     * @param node
     * @param newPos
     */
    public EditTableCommand(Table node, Rectangle newPos) {
        this.node = node;
        this.newPos = newPos;
        setLabel("MoveNodeCommand.0"); //$NON-NLS-1$
    }

    @SuppressWarnings("unchecked")//$NON-NLS-1$
    @Override
    public boolean canExecute() {
        for (Table currentNode : (List<Table>) node.getErDiagram().getTables()) {
            if ((currentNode.getLocation().x == newPos.x) && (currentNode.getLocation().y == newPos.y)
                    && (currentNode.getSize().height == newPos.height) && (currentNode.getSize().width == newPos.width)) {
                return false;
            }
        }
        return true;
    }

    public void execute() {
        oldPos.setLocation(this.node.getLocation());
        this.node.setLocation(newPos.getLocation());
        oldPos.setSize(this.node.getSize());
        this.node.setSize(newPos.getSize());
    }

    public void undo() {
        this.node.setLocation(oldPos.getLocation());
        this.node.setSize(oldPos.getSize());
    }

    public void redo() {
        this.node.setLocation(newPos.getLocation());
        this.node.setSize(newPos.getSize());
    }
}
