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

import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.gef.commands.Command;
import org.talend.designer.core.i18n.Messages;
import org.talend.designer.core.ui.editor.TalendEditor;
import org.talend.designer.core.ui.editor.nodes.Node;

/**
 */
public class ResizeNodeCommand extends Command {

    private final Node node;

    private Dimension oldSize;

    private final Dimension newSize;

    public ResizeNodeCommand(Node node, Dimension newSize) {
        super(Messages.getString("ResizeNodeCommand.Name")); //$NON-NLS-1$
        this.node = node;
        this.newSize = new Dimension((newSize.width / TalendEditor.GRID_SIZE) * TalendEditor.GRID_SIZE,
                (newSize.height / TalendEditor.GRID_SIZE) * TalendEditor.GRID_SIZE);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.gef.commands.Command#canExecute()
     */
    @Override
    public boolean canExecute() {
        if (newSize.height <= 0 || newSize.width <= 0) {
            return false;
        }
        return true;
    }

    @Override
    public void execute() {
        oldSize = node.getSize();
        node.setSize(newSize);
    }

    @Override
    public void undo() {
        node.setSize(oldSize);
    }
}
