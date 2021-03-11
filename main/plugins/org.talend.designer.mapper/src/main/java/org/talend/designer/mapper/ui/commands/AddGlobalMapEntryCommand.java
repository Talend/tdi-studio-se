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
package org.talend.designer.mapper.ui.commands;

import org.eclipse.gef.commands.Command;
import org.talend.designer.mapper.managers.TableEntriesManager;
import org.talend.designer.mapper.model.tableentry.GlobalMapEntry;

/**
 * DOC amaumont class global comment. Detailled comment <br/>
 *
 * $Id$
 *
 */
public class AddGlobalMapEntryCommand extends Command {

    private GlobalMapEntry entry;

    private TableEntriesManager tableEntriesManager;

    private Integer index;

    /**
     * DOC amaumont AddTableEntryCommand constructor comment.
     *
     * @param tableEntriesManager
     */
    public AddGlobalMapEntryCommand(TableEntriesManager tableEntriesManager, GlobalMapEntry tableEntry, Integer index) {
        super();
        this.entry = tableEntry;
        this.tableEntriesManager = tableEntriesManager;
        this.index = index;
    }

    /**
     * DOC amaumont AddTableEntryCommand constructor comment.
     *
     * @param label
     */
    public AddGlobalMapEntryCommand(String label) {
        super(label);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.gef.commands.Command#execute()
     */
    @Override
    public void execute() {

        tableEntriesManager.addTableEntry(entry, index);

    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.gef.commands.Command#canExecute()
     */
    @Override
    public boolean canExecute() {
        return true;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.gef.commands.Command#canUndo()
     */
    @Override
    public boolean canUndo() {
        return true;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.gef.commands.Command#redo()
     */
    @Override
    public void redo() {
        execute();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.gef.commands.Command#undo()
     */
    @Override
    public void undo() {
        tableEntriesManager.remove(entry);
    }

}
