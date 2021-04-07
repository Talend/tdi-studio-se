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
package org.talend.designer.dbmap.managers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.map.MultiKeyMap;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.talend.commons.ui.swt.extended.table.IExtendedControlEventType;
import org.talend.core.model.metadata.IMetadataColumn;
import org.talend.core.model.metadata.IMetadataTable;
import org.talend.designer.abstractmap.model.table.IDataMapTable;
import org.talend.designer.abstractmap.model.tableentry.IColumnEntry;
import org.talend.designer.abstractmap.model.tableentry.ITableEntry;
import org.talend.designer.dbmap.external.connection.IOConnection;
import org.talend.designer.dbmap.i18n.Messages;
import org.talend.designer.dbmap.model.table.InputTable;
import org.talend.designer.dbmap.model.table.OutputTable;
import org.talend.designer.dbmap.model.tableentry.FilterTableEntry;
import org.talend.designer.dbmap.model.tableentry.InputColumnTableEntry;
import org.talend.designer.dbmap.model.tableentry.TableEntryLocation;
import org.talend.designer.dbmap.ui.visualmap.TableEntryProperties;
import org.talend.designer.dbmap.ui.visualmap.table.DataMapTableView;

/**
 * DOC amaumont class global comment. Detailled comment <br/>
 *
 * $Id: TableEntriesManager.java 1795 2007-02-04 04:15:56Z bqian $
 *
 */
public class TableEntriesManager {

    private Map<TableEntryLocation, ITableEntry> tableEntries;

    private Map<ITableEntry, TableEntryProperties> dataMapTableEntryToProperties;

    private MultiKeyMap dbTableName$ColumnNameToColumn = new MultiKeyMap();

    MapperManager mapperManager;

    static final int HEIGHT_REACTION = 2;

    /**
     *
     * Event type. <br/>
     *
     * $Id: TableEntriesManager.java 1795 2007-02-04 04:15:56Z bqian $
     *
     */
    public enum EVENT_TYPE implements IExtendedControlEventType {
        REMOVE_ALL,
        REMOVE,
        ADD,
        ADD_ALL,
    };

    TableEntriesManager(MapperManager mapperManager) {
        super();
        this.tableEntries = new HashMap<TableEntryLocation, ITableEntry>();
        this.dataMapTableEntryToProperties = new HashMap<ITableEntry, TableEntryProperties>();
        this.mapperManager = mapperManager;
    }

    public void loadDbTableNameColumnNameToMetadaColumns(List<IOConnection> connections) {
        for (IOConnection connection : connections) {
            IMetadataTable metadataTable = connection.getTable();
            if(metadataTable == null) {
                continue;
            }
            List<IMetadataColumn> listColumns = metadataTable.getListColumns();
            for (IMetadataColumn column : listColumns) {
                addMetadataColumnFromDbTable(connection.getName(), column.getLabel(), column);
            }
        }

    }

    void removeAll(List<? extends ITableEntry> dataMapTableEntriesGroup, boolean removingPhysicalInputTable) {

        for (ITableEntry dataMapTableEntry : new ArrayList<ITableEntry>(dataMapTableEntriesGroup)) {
            remove(dataMapTableEntry, removingPhysicalInputTable);
        }

        // TableEntriesManagerEvent event = new TableEntriesManagerEvent(EVENT_TYPE.REMOVE_ALL);
        // event.entries = new ArrayList<ITableEntry>(dataMapTableEntriesGroup);
        // fireEvent(event);

    }

    /**
     * DOC amaumont Comment method "load".
     *
     * @param tableEntries2
     */
    void addAll(List<? extends ITableEntry> dataMapTableEntriesGroup) {
        for (ITableEntry dataMapTableEntry : dataMapTableEntriesGroup) {
            addInternal(dataMapTableEntry);
        }

        // TableEntriesManagerEvent event = new TableEntriesManagerEvent(EVENT_TYPE.ADD_ALL);
        // event.entries = new ArrayList<ITableEntry>(dataMapTableEntriesGroup);
        // fireEvent(event);
    }

    void addTableEntry(ITableEntry dataMapTableEntry) {
        addTableEntry(dataMapTableEntry, null);
    }

    /**
     * DOC amaumont Comment method "addTableEntry".
     *
     * @param dataMapTableEntry
     * @param index
     */
    public void addTableEntry(ITableEntry dataMapTableEntry, Integer index) {
        if (dataMapTableEntry == null) {
            throw new IllegalArgumentException(
                    Messages.getString("TableEntriesManager.exceptionMessage.dataMapTableEntryCannotNull")); //$NON-NLS-1$
        }
        addInternal(dataMapTableEntry);
        IDataMapTable dataMapTable = dataMapTableEntry.getParent();
        if (dataMapTableEntry instanceof IColumnEntry) {
            if (index == null) {
                dataMapTable.addColumnEntry((IColumnEntry) dataMapTableEntry);
            } else {
                dataMapTable.addColumnEntry((IColumnEntry) dataMapTableEntry, index);
            }
        } else if (dataMapTableEntry instanceof FilterTableEntry) {
            if (FilterTableEntry.WHERE_FILTER.equals(((FilterTableEntry) dataMapTableEntry).getFilterKind())) {
                if (index == null) {
                    ((OutputTable) dataMapTable).addWhereFilterEntry((FilterTableEntry) dataMapTableEntry);
                } else {
                    ((OutputTable) dataMapTable).addWhereFilterEntry((FilterTableEntry) dataMapTableEntry, index);
                }
            } else if (FilterTableEntry.OTHER_FILTER.equals(((FilterTableEntry) dataMapTableEntry).getFilterKind())) {
                if (index == null) {
                    ((OutputTable) dataMapTable).addOtherFilterEntry((FilterTableEntry) dataMapTableEntry);
                } else {
                    ((OutputTable) dataMapTable).addOtherFilterEntry((FilterTableEntry) dataMapTableEntry, index);
                }
            }
        } else {
            String exceptionMessage = Messages.getString("TableEntriesManager.exceptionMessage.typeIsNotValid", dataMapTableEntry //$NON-NLS-1$
                    .getClass().toString());
            throw new IllegalArgumentException(exceptionMessage);
        }
        // TableEntriesManagerEvent event = new TableEntriesManagerEvent(EVENT_TYPE.ADD);
        // event.entry = dataMapTableEntry;
        // fireEvent(event);
    }

    /**
     * DOC amaumont Comment method "add".
     *
     * @param dataMapTableEntry
     */
    private void addInternal(ITableEntry dataMapTableEntry) {
        tableEntries.put(TableEntryLocation.getNewInstance(dataMapTableEntry), dataMapTableEntry);
        IDataMapTable dataMapTable = dataMapTableEntry.getParent();
        InputTable inputTable = isPhysicalTable(dataMapTable);
        if (inputTable != null) {
            IMetadataColumn metadataColumn = ((InputColumnTableEntry) dataMapTableEntry).getMetadataColumn();
            addMetadataColumnFromDbTable(inputTable.getTableName(), metadataColumn.getLabel(), metadataColumn);
        }

    }

    /**
     * DOC amaumont Comment method "addMetadataColumnFromDbTable".
     *
     * @param dbTableName
     * @param metadataColumn
     */
    private void addMetadataColumnFromDbTable(String dbTableName, String columnName, IMetadataColumn metadataColumn) {

        dbTableName$ColumnNameToColumn.put(dbTableName, columnName, metadataColumn);

    }

    /**
     * DOC amaumont Comment method "addMetadataColumnFromDbTable".
     *
     * @param dbTableName
     * @param metadataColumn
     */
    private void removeMetadataColumnFromDbTable(String dbTableName, String columnName) {

        dbTableName$ColumnNameToColumn.remove(dbTableName, columnName);

    }

    /**
     * DOC amaumont Comment method "addMetadataColumnFromDbTable".
     *
     * @param dbTableName
     * @param metadataColumn
     */
    private boolean isColumnExists(String dbTableName, String columnName) {
        if (dbTableName$ColumnNameToColumn.size() == 0) {
            return true;
        }
        return dbTableName$ColumnNameToColumn.containsKey(dbTableName, columnName);

    }

    /**
     * DOC amaumont Comment method "addMetadataColumnFromDbTable".
     *
     * @param dbTableName
     * @param metadataColumn
     */
    private IMetadataColumn getColumnFromDbTable(String dbTableName, String columnName) {

        return (IMetadataColumn) dbTableName$ColumnNameToColumn.get(dbTableName, columnName);

    }

    public void remove(ITableEntry dataMapTableEntry, boolean removingPhysicalInputTable) {
        if (dataMapTableEntry != null) {
            mapperManager.removeLinksOf(dataMapTableEntry);
            tableEntries.remove(TableEntriesManager.buildLocation(dataMapTableEntry));
            dataMapTableEntryToProperties.remove(dataMapTableEntry);
            IDataMapTable dataMapTable = dataMapTableEntry.getParent();
            if (dataMapTableEntry instanceof IColumnEntry) {
                dataMapTableEntry.getParent().removeColumnEntry((IColumnEntry) dataMapTableEntry);
                InputTable inputTable = isPhysicalTable(dataMapTable);
                if (inputTable != null && !removingPhysicalInputTable) {
                    removeMetadataColumnFromDbTable(inputTable.getTableName(), dataMapTableEntry.getName());
                }
            } else if (dataMapTableEntry instanceof FilterTableEntry) {
                if (dataMapTable instanceof OutputTable) {
                    if (FilterTableEntry.WHERE_FILTER.equals(((FilterTableEntry) dataMapTableEntry).getFilterKind())) {
                        ((OutputTable) dataMapTable).removeWhereFilterEntry((FilterTableEntry) dataMapTableEntry);
                    } else if (FilterTableEntry.OTHER_FILTER.equals(((FilterTableEntry) dataMapTableEntry).getFilterKind())) {
                        ((OutputTable) dataMapTable).removeOtherFilterEntry((FilterTableEntry) dataMapTableEntry);
                    }
                }
            } else {
                String exceptionMessage = Messages.getString("TableEntriesManager.exceptionMessage.typeIsNotValid", //$NON-NLS-1$
                        dataMapTableEntry.getClass().toString());

                throw new IllegalArgumentException(exceptionMessage);
            }
        }
    }

    /**
     * DOC amaumont Comment method "isPhysicalTable".
     *
     * @param dataMapTable
     */
    private InputTable isPhysicalTable(IDataMapTable dataMapTable) {
        if (dataMapTable instanceof InputTable) {
            InputTable inputTable = (InputTable) dataMapTable;
            if (inputTable.getAlias() == null) {
                return inputTable;
            }
        }
        return null;
    }

    /**
     * DOC amaumont Comment method "searchTableEntry".
     *
     * @param tableName
     * @param columnName
     */
    ITableEntry retrieveTableEntry(TableEntryLocation location) {
        return tableEntries.get(location);
    }

    /**
     * DOC amaumont Comment method "getTableEntryProperties".
     *
     * @param dataMapTableEntry
     * @return
     */
    TableEntryProperties getTableEntryProperties(ITableEntry dataMapTableEntry) {
        TableEntryProperties tableEntryProperties = dataMapTableEntryToProperties.get(dataMapTableEntry);
        if (tableEntryProperties == null) {
            tableEntryProperties = new TableEntryProperties();
            dataMapTableEntryToProperties.put(dataMapTableEntry, tableEntryProperties);
        }
        return tableEntryProperties;
    }

    TableItem retrieveTableItem(ITableEntry dataMapTableEntry) {
        DataMapTableView dataMapTableView = this.mapperManager.retrieveIDataMapTableView(dataMapTableEntry.getParent());
        TableItem[] tableItems = new TableItem[0];
        List inputList = null;
        if (dataMapTableEntry instanceof IColumnEntry) {
            tableItems = dataMapTableView.getTableViewerCreatorForColumns().getTable().getItems();
            inputList = dataMapTableView.getTableViewerCreatorForColumns().getInputList();
        } else if (dataMapTableEntry instanceof FilterTableEntry) {
            if (FilterTableEntry.OTHER_FILTER.equals(((FilterTableEntry) dataMapTableEntry).getFilterKind())) {
                tableItems = dataMapTableView.getTableViewerCreatorForOtherFilters().getTable().getItems();
                inputList = dataMapTableView.getTableViewerCreatorForOtherFilters().getInputList();
            } else {
                tableItems = dataMapTableView.getTableViewerCreatorForWhereFilters().getTable().getItems();
                inputList = dataMapTableView.getTableViewerCreatorForWhereFilters().getInputList();
            }
        } else {
            throw new IllegalArgumentException(Messages.getString("TableEntriesManager.exceptionMessage.caseNotFound")); //$NON-NLS-1$
        }
        TableItem tableItem = null;
        if (inputList != null) {
            int index = inputList.indexOf(dataMapTableEntry);
            if (0 <= index && index < tableItems.length) {
                tableItem = tableItems[index];
            }
        }
        if (tableItem == null) {
            for (TableItem tableItem2 : tableItems) {
                if (tableItem2.getData() == dataMapTableEntry) {
                    tableItem = tableItem2;
                    break;
                }
            }
        }
        getTableEntryProperties(dataMapTableEntry).setTableItem(tableItem);
        return tableItem;
    }

    /**
     * DOC amaumont Comment method "retrieveTableFromTableEntry".
     *
     * @param dataMapTableEntry
     * @return
     */
    Table retrieveTable(ITableEntry dataMapTableEntry) {
        return retrieveTableItem(dataMapTableEntry).getParent();
    }

    /**
     * DOC amaumont Comment method "renameEntryName".
     *
     * @param dataMapTableEntry
     * @param newColumnName
     * @param newColumnName
     */
    public void renameEntryName(ITableEntry dataMapTableEntry, String previousColumnName, String newColumnName) {
        TableEntryLocation tableEntryLocationKey = new TableEntryLocation(dataMapTableEntry.getParentName(), previousColumnName);
        // TableEntriesManager.buildLocation(dataMapTableEntry);
        ITableEntry entry = tableEntries.get(tableEntryLocationKey);
        if (entry != dataMapTableEntry) {
            throw new IllegalStateException(Messages.getString("TableEntriesManager.exceptionMessage.tableEntriesNotSame")); //$NON-NLS-1$
        }
        tableEntries.remove(tableEntryLocationKey);

        tableEntryLocationKey.columnName = newColumnName;
        tableEntries.put(tableEntryLocationKey, dataMapTableEntry);

        // update matching column
        IMetadataColumn metadataColumn = null;
        InputTable inputTable = isPhysicalTable(dataMapTableEntry.getParent());
        if (inputTable != null) {
            metadataColumn = getColumnFromDbTable(inputTable.getName(), dataMapTableEntry.getName());
            removeMetadataColumnFromDbTable(inputTable.getTableName(), dataMapTableEntry.getName());
            addMetadataColumnFromDbTable(inputTable.getTableName(), newColumnName, metadataColumn);
        }

        dataMapTableEntry.setName(newColumnName);
    }

    public void updateTableEntryLocation(ITableEntry dataMapTableEntry, String oldName, String newName) {
        TableEntryLocation oldLocation = new TableEntryLocation(oldName, dataMapTableEntry.getName());
        ITableEntry entry = tableEntries.get(oldLocation);
        if (entry != dataMapTableEntry) {
            throw new IllegalStateException(Messages.getString("TableEntriesManager.exceptionMessage.tableEntriesNotSame")); //$NON-NLS-1$
        }
        tableEntries.remove(oldLocation);
        TableEntryLocation newLocation = new TableEntryLocation(newName, dataMapTableEntry.getName());
        tableEntries.put(newLocation, dataMapTableEntry);
    }

    public static TableEntryLocation buildLocation(ITableEntry dataMapTableEntry) {
        return new TableEntryLocation(dataMapTableEntry.getParentName(), dataMapTableEntry.getName());
    }

    /**
     * DOC amaumont Comment method "isUnmatchingEntry".
     *
     * @param inputEntry
     * @return
     */
    public boolean isUnmatchingEntryWithDbColumn(InputColumnTableEntry inputEntry) {
        InputTable parent = (InputTable) inputEntry.getParent();
        if (parent.getAlias() != null) {
            return !isColumnExists(parent.getTableName(), inputEntry.getName());
        }
        return false;
    }

    /**
     *
     * Event for TableEntriesManager. <br/>
     *
     * $Id: TableEntriesManager.java 1795 2007-02-04 04:15:56Z bqian $
     *
     */
    // public class TableEntriesManagerEvent extends ExtendedModelEvent {
    //
    // /**
    // * DOC amaumont TableEntriesManagerEvent constructor comment.
    // * @param type
    // */
    // public TableEntriesManagerEvent(IExtendedControlEventType type) {
    // super(type);
    // }
    //
    // public ITableEntry entry;
    // public List<ITableEntry> entries;
    //
    // }
}
