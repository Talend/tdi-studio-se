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
package org.talend.sqlbuilder.dataset.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.swt.custom.TableCursor;
import org.eclipse.swt.widgets.Table;

/**
 * Abstract implementation for a context menu action of a DataSetTable. Extend this class to add new actions to the
 * DataSetTable.
 *
 * @author Davy Vanherbergen
 */
public abstract class AbstractDataSetTableContextAction extends Action {

    protected Table ptable;

    protected TableCursor pcursor;

    /**
     * Store table for use in the actions.
     *
     * @param table
     */
    public final void setTable(Table table) {
        ptable = table;
    }

    /**
     * Store table cursor for use in the actions.
     *
     * @param tableCursor
     */
    public final void setTableCursor(TableCursor cursor) {
        pcursor = cursor;
    }

    /**
     * Implement this method to return true when your action is available for the active table. When true, the action
     * will be included in the context menu, when false it will be ignored.
     *
     * @return true if the action should be included in the context menu
     */
    public boolean isAvailable() {
        return true;
    }
}
