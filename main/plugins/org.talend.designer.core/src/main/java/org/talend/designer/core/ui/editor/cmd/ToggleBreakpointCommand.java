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
import org.talend.core.CorePlugin;
import org.talend.core.context.Context;
import org.talend.core.model.process.INode;
import org.talend.core.model.process.IProcess;

/**
 * Command that toggle a breakpoint on a node. <br/>
 *
 * $Id$
 *
 */
public class ToggleBreakpointCommand extends Command {

    private IProcess process;

    private INode node;

    /**
     * Constructs a new ToggleBreakpointCommand.
     */
    public ToggleBreakpointCommand(IProcess process, INode node) {
        super();

        this.process = process;
        this.node = node;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.gef.commands.Command#execute()
     */
    @Override
    public void execute() {
        Context context = CorePlugin.getContext();
        if (context.getBreakpointNodes(process).contains(node)) {
            context.removeBreakpoint(process, node);
        } else {
            context.addBreakpoint(process, node);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.gef.commands.Command#undo()
     */
    @Override
    public void undo() {
        execute();
    }
}
