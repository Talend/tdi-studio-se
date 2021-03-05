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
package org.talend.designer.dbmap.model.table;

import java.util.ArrayList;
import java.util.List;

import org.talend.commons.ui.swt.extended.table.ExtendedTableModel;
import org.talend.designer.abstractmap.model.table.IDataMapTable;
import org.talend.designer.abstractmap.model.tableentry.IColumnEntry;
import org.talend.designer.dbmap.external.data.ExternalDbMapTable;
import org.talend.designer.dbmap.managers.MapperManager;

/**
 * DOC amaumont class global comment. Detailled comment <br/>
 *
 * $Id: IDataMapTable.java 1782 2007-02-03 07:57:38Z bqian $
 *
 */
public abstract class AbstractDataMapTable implements IDataMapTable {

    protected List<IColumnEntry> dataMapTableEntries = new ArrayList<IColumnEntry>();

    protected boolean minimized;

    private String name;

    private ExtendedTableModel<IColumnEntry> tableColumnsEntriesModel;

    protected MapperManager mapperManager;

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
            throw new IllegalArgumentException("Name of IDataMapTable can't be null"); //$NON-NLS-1$
        }
        this.tableColumnsEntriesModel = new ExtendedTableModel<IColumnEntry>(
                name + " : model for Columns", dataMapTableEntries); //$NON-NLS-1$

    }

    /**
     * DOC amaumont Comment method "init".
     */
    protected void initFromExternalData(ExternalDbMapTable externalMapperTable) {
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

    public String getTitle() {
        return this.getName();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.abstractmap.model.table.IDataMapTable#getSizeState()
     */
    public SIZE_STATE getSizeState() {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.abstractmap.model.table.IDataMapTable#isReadOnly()
     */
    public boolean isReadOnly() {
        // TODO Auto-generated method stub
        return false;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.abstractmap.model.table.IDataMapTable#setReadOnly(boolean)
     */
    public void setReadOnly(boolean readOnly) {
        // TODO Auto-generated method stub

    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.abstractmap.model.table.IDataMapTable#setSizeState(org.talend.designer.abstractmap.model.table.IDataMapTable.SIZE_STATE)
     */
    public void setSizeState(SIZE_STATE sizeState) {
        // TODO Auto-generated method stub

    }

}
