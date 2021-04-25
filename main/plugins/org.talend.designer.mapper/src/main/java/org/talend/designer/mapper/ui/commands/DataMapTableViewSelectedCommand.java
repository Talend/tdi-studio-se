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
import org.talend.designer.mapper.managers.UIManager;
import org.talend.designer.mapper.ui.visualmap.table.DataMapTableView;

/**
 * DOC amaumont class global comment. Detailled comment <br/>
 *
 * $Id$
 *
 */
public class DataMapTableViewSelectedCommand extends Command {

    UIManager uiManager;

    DataMapTableView previousSelectedTableView;

    DataMapTableView newSelectedDataMapTableView;

    /**
     * DOC amaumont DataMapTableViewSelectedCommand constructor comment.
     *
     * @param manager
     * @param previousSelectedTableView
     * @param dataMapTableView
     */
    public DataMapTableViewSelectedCommand(UIManager uiManager, DataMapTableView previousSelectedTableView,
            DataMapTableView newSelectedDataMapTableView) {
        this.uiManager = uiManager;
        this.previousSelectedTableView = previousSelectedTableView;
        this.newSelectedDataMapTableView = newSelectedDataMapTableView;
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
     * @see org.eclipse.gef.commands.Command#execute()
     */
    @Override
    public void execute() {
        // nothing
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.gef.commands.Command#redo()
     */
    @Override
    public void redo() {
        uiManager.selectDataMapTableView(newSelectedDataMapTableView, false, false);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.gef.commands.Command#undo()
     */
    @Override
    public void undo() {
        if (previousSelectedTableView != null) {
            uiManager.selectDataMapTableView(previousSelectedTableView, false, false);
        }
    }

}
