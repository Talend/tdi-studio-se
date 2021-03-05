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
package org.talend.designer.abstractmap.model.table;

import java.util.List;

import org.talend.commons.ui.swt.extended.table.ExtendedTableModel;
import org.talend.designer.abstractmap.managers.AbstractMapperManager;
import org.talend.designer.abstractmap.model.tableentry.IColumnEntry;

/**
 * DOC amaumont class global comment. Detailled comment <br/>
 *
 */
public interface IDataMapTable {

    /**
     *
     * DOC amaumont AbstractDataMapTable class global comment. Detailled comment <br/>
     *
     */
    public enum SIZE_STATE {
        MINIMIZED,
        INTERMEDIATE,
        MAXIMIZED, ;
    }

    /**
     *
     * @return a <code>List</code> of entries.
     */
    public List<IColumnEntry> getColumnEntries();

    public void removeColumnEntry(IColumnEntry dataMapTableEntry);

    public void addColumnEntry(IColumnEntry dataMapTableEntry);

    public void addColumnEntry(IColumnEntry dataMapTableEntry, int index);

    public String getName();

    public boolean isMinimized();

    public void setMinimized(boolean minimized);

    /**
     * Getter for columnsEntriesModel.
     *
     * @return the columnsEntriesModel
     */
    public ExtendedTableModel<IColumnEntry> getTableColumnsEntriesModel();

    /**
     * DOC amaumont Comment method "swapElements".
     *
     * @param indicesOrigin
     * @param listIndexTarget
     */
    public void swapColumnElements(List<Integer> indicesOrigin, List<Integer> listIndexTarget);

    /**
     * Getter for mapperManager.
     *
     * @return the mapperManager
     */
    public AbstractMapperManager getMapperManager();

    /**
     * DOC amaumont Comment method "isReadOnly".
     *
     * @return
     */
    public boolean isReadOnly();

    /**
     * Sets the readOnly.
     *
     * @param readOnly the readOnly to set
     */
    public void setReadOnly(boolean readOnly);

    /**
     * Getter for sizeState.
     *
     * @return the sizeState
     */
    public SIZE_STATE getSizeState();

    /**
     * Sets the sizeState.
     *
     * @param sizeState the sizeState to set
     */
    public void setSizeState(SIZE_STATE sizeState);

}
