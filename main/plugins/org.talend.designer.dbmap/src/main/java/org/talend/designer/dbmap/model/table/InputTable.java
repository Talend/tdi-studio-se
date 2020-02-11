// ============================================================================
//
// Copyright (C) 2006-2019 Talend Inc. - www.talend.com
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.talend.core.model.metadata.IMetadataColumn;
import org.talend.core.model.metadata.IMetadataTable;
import org.talend.core.model.metadata.MetadataColumn;
import org.talend.designer.dbmap.external.connection.IOConnection;
import org.talend.designer.dbmap.external.data.ExternalDbMapEntry;
import org.talend.designer.dbmap.external.data.ExternalDbMapTable;
import org.talend.designer.dbmap.language.IJoinType;
import org.talend.designer.dbmap.managers.MapperManager;
import org.talend.designer.dbmap.model.tableentry.AbstractInOutTableEntry;
import org.talend.designer.dbmap.model.tableentry.InputColumnTableEntry;

/**
 * DOC amaumont class global comment. Detailled comment <br/>
 *
 * $Id: InputTable.java 1601 2007-01-25 16:46:15Z amaumont $
 *
 */
public class InputTable extends AbstractInOutTable {

    private IOConnection connection;

    /**
     */
    private IJoinType joinType;

    private String alias;

    private String tableName;

    /**
     * DOC amaumont InputTable constructor comment.
     *
     * @param mapperManager
     *
     * @param metadataTable
     * @param externalMapperTable can be null
     * @param mainConnection
     */
    public InputTable(MapperManager mapperManager, IOConnection connection, String name) {
        this(mapperManager, (IMetadataTable) null, name);
        this.connection = connection;
    }

    /**
     * DOC amaumont InputTable constructor comment.
     *
     * @param manager
     * @param metadataTable
     * @param alias2
     */
    public InputTable(MapperManager mapperManager, IMetadataTable metadataTable, String name) {
        super(mapperManager, metadataTable, name);
        joinType = mapperManager.getCurrentLanguage().getAvailableJoins()[0];
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.talend.designer.dbmap.model.table.AbstractInOutTable#initFromExternalData(org.talend.designer.dbmap.external
     * .data.ExternalMapperTable)
     */
    @Override
    public void initFromExternalData(ExternalDbMapTable externalMapperTable) {
        if(connection != null) {
            setMetadataTable(connection.getTable());
        }else {
            setMetadataTable(this.metadataTable);
        }
        super.initFromExternalData(externalMapperTable);

        List<IMetadataColumn> columns = getMetadataTable().getListColumns();
        Map<String, ExternalDbMapEntry> nameToPerTabEntry = new HashMap<String, ExternalDbMapEntry>();
        if (externalMapperTable != null && externalMapperTable.getMetadataTableEntries() != null) {
            for (ExternalDbMapEntry perTableEntry : externalMapperTable.getMetadataTableEntries()) {
                nameToPerTabEntry.put(perTableEntry.getName(), perTableEntry);
            }
        }

        for (IMetadataColumn column : columns) {
            InputColumnTableEntry inputEntry = (InputColumnTableEntry) getNewTableEntry(column);
            ExternalDbMapEntry externalMapperTableEntry = nameToPerTabEntry.get(inputEntry.getMetadataColumn().getLabel());
            // Entry match with current column
            if (externalMapperTableEntry != null) {
                fillInputEntry(inputEntry, externalMapperTableEntry);
                nameToPerTabEntry.remove(externalMapperTableEntry.getName());
            }
            dataMapTableEntries.add(inputEntry);
        }

        // create unmatching entries
        for (ExternalDbMapEntry perTableEntry : nameToPerTabEntry.values()) {
            MetadataColumn column = new MetadataColumn();
            column.setLabel(perTableEntry.getName());
            InputColumnTableEntry inputEntry = (InputColumnTableEntry) getNewTableEntry(column);
            ExternalDbMapEntry externalMapperTableEntry = nameToPerTabEntry.get(inputEntry.getMetadataColumn().getLabel());
            fillInputEntry(inputEntry, externalMapperTableEntry);
            dataMapTableEntries.add(inputEntry);
            columns.add(column);
        }

        if (externalMapperTable != null) {
            joinType = mapperManager.getCurrentLanguage().getJoin(externalMapperTable.getJoinType());
            if (joinType == null) {
                joinType = mapperManager.getCurrentLanguage().getAvailableJoins()[0];
            }
            alias = externalMapperTable.getAlias();
            tableName = externalMapperTable.getTableName() != null ? externalMapperTable.getTableName() : connection.getName();
        }
    }

    /**
     * DOC amaumont Comment method "fillIputEntry".
     *
     * @param columnEntry
     * @param externalMapperTableEntry
     */
    private void fillInputEntry(InputColumnTableEntry columnEntry, ExternalDbMapEntry externalMapperTableEntry) {
        columnEntry.setExpression(externalMapperTableEntry.getExpression());
        columnEntry.setOperator(externalMapperTableEntry.getOperator());
        columnEntry.setJoin(externalMapperTableEntry.isJoin());
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.talend.designer.dbmap.model.table.DataMapTable#getNewTableEntry(org.talend.core.model.metadata.IMetadataColumn
     * )
     */
    @Override
    protected AbstractInOutTableEntry getNewTableEntry(IMetadataColumn column) {
        return new InputColumnTableEntry(this, column);
    }

    public boolean isMainConnection() {
        return false;
    }

    /**
     * Getter for joinType.
     *
     * @return the joinType
     */
    public IJoinType getJoinType() {
        if (this.joinType == null) {
            this.joinType = mapperManager.getCurrentLanguage().getJoin(null);
        }
        return this.joinType;
    }

    /**
     * Sets the joinType.
     *
     * @param joinType the joinType to set
     */
    public void setJoinType(IJoinType joinType) {
        this.joinType = joinType;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.dbmap.model.table.IDataMapTable#getName()
     */
    @Override
    public String getName() {
        if (alias != null) {
            return alias;
        }
        if (tableName != null) {
            return tableName;
        }
        return super.getName();
    }

    /**
     * Getter for alias.
     *
     * @return the alias
     */
    public String getAlias() {
        return this.alias;
    }

    /**
     * Sets the alias.
     *
     * @param alias the alias to set
     */
    public void setAlias(String alias) {
        this.alias = alias;
    }

    /**
     * Getter for tableName.
     *
     * @return the tableName
     */
    public String getTableName() {
        return this.tableName;
    }

    /**
     * Sets the tableName.
     *
     * @param tableName the tableName to set
     */
    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    @Override
    public String getTitle() {
        String alias = this.getAlias();
        String tableName = this.getTableName();
        if (alias != null) {
            return alias + "  (alias of table '" + tableName + "')"; //$NON-NLS-1$ //$NON-NLS-2$
        } else {
            return tableName + "  (table)"; //$NON-NLS-1$
        }
    }

}
