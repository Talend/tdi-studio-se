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
package org.talend.designer.mapper.model.table;

import java.util.ArrayList;
import java.util.List;

import org.talend.commons.ui.swt.extended.table.ExtendedTableModel;
import org.talend.designer.abstractmap.model.table.IDataMapTable;
import org.talend.designer.abstractmap.model.tableentry.IColumnEntry;
import org.talend.designer.mapper.external.data.ExternalMapperTable;
import org.talend.designer.mapper.managers.MapperManager;

/**
 * DOC amaumont class global comment. Detailled comment <br/>
 *
 * $Id$
 *
 */
public abstract class AbstractDataMapTable implements IDataMapTable {

    private SIZE_STATE sizeState = SIZE_STATE.INTERMEDIATE;

    private boolean minimized;

    private String name;

    private ExtendedTableModel<IColumnEntry> tableColumnsEntriesModel;

    protected MapperManager mapperManager;

    private boolean readOnly;

    protected List<IColumnEntry> dataMapTableEntries = new ArrayList<IColumnEntry>();

    /**
     * DOC amaumont DataMapTable constructor comment.
     *
     * @param persistentTable
     *
     * @param metadataTable
     * @param externalMapperTable
     */
    public AbstractDataMapTable(MapperManager mapperManager, String name) {
        this.mapperManager = mapperManager;
        this.name = name;
        if (name == null) {
            throw new IllegalArgumentException("Name's AbstractDataMapTable can't be null"); //$NON-NLS-1$
        }
        this.tableColumnsEntriesModel = new ExtendedTableModel<IColumnEntry>(name + " : model for Columns", dataMapTableEntries); //$NON-NLS-1$

    }

    /**
     * DOC amaumont Comment method "init".
     */
    protected void initFromExternalData(ExternalMapperTable externalMapperTable) {
        if (externalMapperTable != null) {
            this.minimized = externalMapperTable.isMinimized();
        }
    }

    /**
     *
     * @return a <code>List</code> of entries.
     */
    public List<IColumnEntry> getColumnEntries() {
        return this.tableColumnsEntriesModel.getBeansList();
    }

    public void removeColumnEntry(IColumnEntry dataMapTableEntry) {
        this.tableColumnsEntriesModel.remove(dataMapTableEntry);
    }

    public void addColumnEntry(IColumnEntry dataMapTableEntry) {
        this.tableColumnsEntriesModel.add(dataMapTableEntry);
    }

    public void addColumnEntry(IColumnEntry dataMapTableEntry, int index) {
        this.tableColumnsEntriesModel.add(dataMapTableEntry, index);
    }

    public String getName() {
        return this.name;
    }

    public boolean isMinimized() {
        return this.minimized;
    }

    public void setMinimized(boolean minimized) {
        this.minimized = minimized;
    }

    /**
     * Getter for columnsEntriesModel.
     *
     * @return the columnsEntriesModel
     */
    public ExtendedTableModel<IColumnEntry> getTableColumnsEntriesModel() {
        return this.tableColumnsEntriesModel;
    }

    /**
     * DOC amaumont Comment method "swapElements".
     *
     * @param indicesOrigin
     * @param listIndexTarget
     */
    public void swapColumnElements(List<Integer> indicesOrigin, List<Integer> listIndexTarget) {
        this.tableColumnsEntriesModel.swapElements(indicesOrigin, listIndexTarget);
    }

    /**
     * Getter for mapperManager.
     *
     * @return the mapperManager
     */
    public MapperManager getMapperManager() {
        return this.mapperManager;
    }

    /**
     * DOC amaumont Comment method "isReadOnly".
     *
     * @return
     */
    public boolean isReadOnly() {
        return readOnly;
    }

    /**
     * Sets the readOnly.
     *
     * @param readOnly the readOnly to set
     */
    public void setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
    }

    /**
     * Getter for sizeState.
     *
     * @return the sizeState
     */
    public SIZE_STATE getSizeState() {
        return this.sizeState;
    }

    /**
     * Sets the sizeState.
     *
     * @param sizeState the sizeState to set
     */
    public void setSizeState(SIZE_STATE sizeState) {
        this.sizeState = sizeState;
    }

    public List<IColumnEntry> getDataMapTableEntries() {
        return dataMapTableEntries;
    }

}
