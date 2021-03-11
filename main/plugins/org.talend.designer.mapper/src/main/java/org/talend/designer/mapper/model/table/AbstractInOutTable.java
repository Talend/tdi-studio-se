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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.talend.commons.ui.swt.extended.table.ExtendedTableModel;
import org.talend.core.model.metadata.IMetadataColumn;
import org.talend.core.model.metadata.IMetadataTable;
import org.talend.core.model.metadata.MetadataToolHelper;
import org.talend.designer.mapper.external.connection.IOConnection;
import org.talend.designer.mapper.external.data.ExternalMapperTable;
import org.talend.designer.mapper.external.data.ExternalMapperTableEntry;
import org.talend.designer.mapper.managers.MapperManager;
import org.talend.designer.mapper.model.tableentry.AbstractInOutTableEntry;
import org.talend.designer.mapper.model.tableentry.ExpressionFilterEntry;
import org.talend.designer.mapper.model.tableentry.GlobalMapEntry;
import org.talend.designer.mapper.model.tableentry.InputColumnTableEntry;

/**
 * DOC amaumont class global comment. Detailled comment <br/>
 *
 * $Id$
 *
 */
public abstract class AbstractInOutTable extends AbstractDataMapTable {

    protected IMetadataTable metadataTable;

    private IOConnection connection;

    private ExpressionFilterEntry expressionFilterEntry;

    private ExtendedTableModel<GlobalMapEntry> tableMapSettingEntriesModel;

    private boolean activateExpressionFilter;

    private boolean activateColumnNameFilter;

    private boolean activateCondensedTool;

    private String columnNameFilter;

    protected List<GlobalMapEntry> mapSettingEntries = new ArrayList<GlobalMapEntry>();

    private boolean isRepository;

    private String id; // used for saving schema id of table.

    /**
     *
     * DOC amaumont AbstractInOutTable constructor comment.
     *
     * @param metadataTable
     * @param externalMapperTable can be null
     * @param name
     */
    public AbstractInOutTable(MapperManager mapperManager, IMetadataTable metadataTable, String name) {
        super(mapperManager, name);
        this.metadataTable = metadataTable;
    }

    /**
     * DOC amaumont AbstractInOutTable constructor comment.
     *
     * @param mapperManager
     * @param connection2
     * @param name
     */
    public AbstractInOutTable(MapperManager mapperManager, IOConnection connection, String name) {
        this(mapperManager, connection.getTable(), name);
        this.connection = connection;
    }

    /**
     *
     * DOC wchen AbstractInOutTable constructor comment.
     *
     * @param metadataTable
     * @param externalMapperTable can be null
     * @param name
     */
    public AbstractInOutTable(MapperManager mapperManager, IMetadataTable metadataTable, IOConnection connection, String name) {
        super(mapperManager, name);
        this.metadataTable = metadataTable;
        this.connection = connection;
    }

    @Override
    protected void initFromExternalData(ExternalMapperTable externalMapperTable) {
        super.initFromExternalData(externalMapperTable);
        expressionFilterEntry = new ExpressionFilterEntry(this);
        tableMapSettingEntriesModel = new ExtendedTableModel<GlobalMapEntry>("Model for map setting", mapSettingEntries);
        if (externalMapperTable != null) {
            this.expressionFilterEntry.setExpression(externalMapperTable.getExpressionFilter());
            this.setColumnNameFilter(externalMapperTable.getColumnNameFilter());
            this.activateExpressionFilter = externalMapperTable.isActivateExpressionFilter();
            this.activateCondensedTool = externalMapperTable.isActivateCondensedTool();
            this.activateColumnNameFilter = externalMapperTable.isActivateColumnNameFilter();
            this.id = externalMapperTable.getId();
            this.isRepository = this.id == null ? false : true;
        }
        List<IMetadataColumn> columns = new ArrayList<IMetadataColumn>();
        Map<String, ExternalMapperTableEntry> nameToPerTabEntry = new HashMap<String, ExternalMapperTableEntry>();
        if (externalMapperTable != null && externalMapperTable.getMetadataTableEntries() != null) {
            for (ExternalMapperTableEntry perTableEntry : externalMapperTable.getMetadataTableEntries()) {
                nameToPerTabEntry.put(perTableEntry.getName(), perTableEntry);
            }
        }

        String originalTableName = null;
        if (metadataTable != null) {
            originalTableName = metadataTable.getTableName();
        }

        if (isRepository) {
            IMetadataTable table = MetadataToolHelper.getMetadataFromRepository(id);
            if (table == null) {
                this.id = null;
                this.isRepository = false;
            } else {
                this.metadataTable = table;
                this.metadataTable.setTableName(originalTableName);
            }
        }

        if (metadataTable != null) {
            columns = this.metadataTable.getListColumns();
        }

        updateTableEntries(columns, nameToPerTabEntry);
    }

    private void updateTableEntries(List<IMetadataColumn> columns, Map<String, ExternalMapperTableEntry> nameToPerTabEntry) {
        for (IMetadataColumn column : columns) {
            AbstractInOutTableEntry columnEntry = getNewTableEntry(column);
            ExternalMapperTableEntry externalMapperTableEntry = nameToPerTabEntry.get(columnEntry.getMetadataColumn().getLabel());
            if (externalMapperTableEntry != null) {
                columnEntry.setExpression(externalMapperTableEntry.getExpression());
                if (columnEntry instanceof InputColumnTableEntry) {
                    InputColumnTableEntry entry = (InputColumnTableEntry) columnEntry;
                    entry.setOperator(externalMapperTableEntry.getOperator());
                }
                // mapperManager.getProblemsManager().checkProblemsForTableEntry(columnEntry, false);
            }
            dataMapTableEntries.add(columnEntry);
        }
    }

    protected abstract AbstractInOutTableEntry getNewTableEntry(IMetadataColumn metadataColumn);

    public IMetadataTable getMetadataTable() {
        return this.metadataTable;
    }

    public abstract boolean hasReadOnlyMetadataColumns();

    /**
     * Getter for connection.
     *
     * @return the connection
     */
    public IOConnection getConnection() {
        return this.connection;
    }

    /**
     * Getter for expressionFilter.
     *
     * @return the expressionFilter
     */
    public ExpressionFilterEntry getExpressionFilter() {
        return this.expressionFilterEntry;
    }

    public ExtendedTableModel<GlobalMapEntry> getTableMapSettingEntriesModel() {
        return this.tableMapSettingEntriesModel;
    }

    /**
     * Getter for activateExpressionFilter.
     *
     * @return the activateExpressionFilter
     */
    public boolean isActivateExpressionFilter() {
        return this.activateExpressionFilter;
    }

    /**
     * Sets the activateExpressionFilter.
     *
     * @param activateExpressionFilter the activateExpressionFilter to set
     */
    public void setActivateExpressionFilter(boolean activateExpressionFilter) {
        this.activateExpressionFilter = activateExpressionFilter;
    }

    public boolean isActivateColumnNameFilter() {
        return this.activateColumnNameFilter;
    }

    public void setActiveColumnNameFilter(boolean activateColumnNameFilter) {
        this.activateColumnNameFilter = activateColumnNameFilter;
    }

    public String getColumnNameFilter() {
        return this.columnNameFilter;
    }

    public void setColumnNameFilter(String columnNameFilter) {
        this.columnNameFilter = columnNameFilter;
    }

    public boolean isActivateCondensedTool() {
        return this.activateCondensedTool;
    }

    public void setActivateCondensedTool(boolean activatecondensedTool) {
        this.activateCondensedTool = activatecondensedTool;
    }

    /**
     * Getter for id.
     *
     * @return the id
     */
    public String getId() {
        return this.id;
    }

    /**
     * Sets the id.
     *
     * @param id the id to set
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Getter for isRepository.
     *
     * @return the isRepository
     */
    public boolean isRepository() {
        return this.isRepository;
    }

    /**
     * Sets the isRepository.
     *
     * @param isRepository the isRepository to set
     */
    public void setRepository(boolean isRepository) {
        this.isRepository = isRepository;
    }
}
