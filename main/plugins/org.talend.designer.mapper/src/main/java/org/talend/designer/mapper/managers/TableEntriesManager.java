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
package org.talend.designer.mapper.managers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.talend.commons.ui.swt.extended.table.IExtendedControlEventType;
import org.talend.designer.abstractmap.model.table.IDataMapTable;
import org.talend.designer.abstractmap.model.tableentry.IColumnEntry;
import org.talend.designer.abstractmap.model.tableentry.ITableEntry;
import org.talend.designer.mapper.i18n.Messages;
import org.talend.designer.mapper.model.table.InputTable;
import org.talend.designer.mapper.model.table.OutputTable;
import org.talend.designer.mapper.model.tableentry.ExpressionFilterEntry;
import org.talend.designer.mapper.model.tableentry.FilterTableEntry;
import org.talend.designer.mapper.model.tableentry.GlobalMapEntry;
import org.talend.designer.mapper.model.tableentry.TableEntryLocation;
import org.talend.designer.mapper.ui.visualmap.TableEntryProperties;
import org.talend.designer.mapper.ui.visualmap.table.DataMapTableView;

/**
 * DOC amaumont class global comment. Detailled comment <br/>
 *
 * $Id$
 *
 */
public class TableEntriesManager {

    private static Logger log = Logger.getLogger(TableEntriesManager.class);

    private Map<TableEntryLocation, ITableEntry> tableEntries;

    private Map<ITableEntry, TableEntryProperties> dataMapTableEntryToProperties;

    MapperManager mapperManager;

    static final int HEIGHT_REACTION = 2;

    /**
     *
     * Event type. <br/>
     *
     * $Id$
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

    void removeAll(List<? extends ITableEntry> dataMapTableEntriesGroup) {

        for (ITableEntry dataMapTableEntry : new ArrayList<ITableEntry>(dataMapTableEntriesGroup)) {
            remove(dataMapTableEntry);
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
            add(dataMapTableEntry);
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
            throw new IllegalArgumentException(Messages
                    .getString("TableEntriesManager.exceptionMessage.dataMapTableEntryCannotNull")); //$NON-NLS-1$
        }
        add(dataMapTableEntry);
        IDataMapTable dataMapTable = dataMapTableEntry.getParent();
        if (dataMapTableEntry instanceof IColumnEntry) {
            if (index == null) {
                dataMapTable.addColumnEntry((IColumnEntry) dataMapTableEntry);
            } else {
                dataMapTable.addColumnEntry((IColumnEntry) dataMapTableEntry, index);
            }
        } else if (dataMapTableEntry instanceof FilterTableEntry) {
            if (index == null) {
                ((OutputTable) dataMapTable).addFilterEntry((FilterTableEntry) dataMapTableEntry);
            } else {
                ((OutputTable) dataMapTable).addFilterEntry((FilterTableEntry) dataMapTableEntry, index);
            }
        } else if (dataMapTableEntry instanceof GlobalMapEntry) {
            if (index == null) {
                ((InputTable) dataMapTable).addGlobalMapEntry((GlobalMapEntry) dataMapTableEntry);
            } else {
                ((InputTable) dataMapTable).addGlobalMapEntry((GlobalMapEntry) dataMapTableEntry, index);
            }
        } else {
            String exceptionMessage = Messages.getString(
                    "TableEntriesManager.exceptionMessage.typeIsNotValid", dataMapTableEntry //$NON-NLS-1$
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
    private void add(ITableEntry dataMapTableEntry) {
        tableEntries.put(TableEntryLocation.getNewInstance(dataMapTableEntry), dataMapTableEntry);
    }

    public void remove(ITableEntry dataMapTableEntry) {
        if (dataMapTableEntry != null) {
            mapperManager.removeLinksOf(dataMapTableEntry);
            tableEntries.remove(TableEntriesManager.buildLocation(dataMapTableEntry));
            dataMapTableEntryToProperties.remove(dataMapTableEntry);
            IDataMapTable dataMapTable = dataMapTableEntry.getParent();
            if (dataMapTableEntry instanceof IColumnEntry) {
                dataMapTableEntry.getParent().removeColumnEntry((IColumnEntry) dataMapTableEntry);
            } else if (dataMapTableEntry instanceof GlobalMapEntry) {
                if (dataMapTable instanceof OutputTable) {
                    ((InputTable) dataMapTable).removeGlobalMapEntry((GlobalMapEntry) dataMapTableEntry);
                }
            } else if (dataMapTableEntry instanceof FilterTableEntry) {
                if (dataMapTable instanceof OutputTable) {
                    ((OutputTable) dataMapTable).removeFilterEntry((FilterTableEntry) dataMapTableEntry);
                }
            } else if (dataMapTableEntry instanceof ExpressionFilterEntry) {
                // nothing
            } else {
                String exceptionMessage = Messages.getString("TableEntriesManager.exceptionMessage.typeIsNotValid", //$NON-NLS-1$
                        dataMapTableEntry.getClass().toString());

                throw new IllegalArgumentException(exceptionMessage);
            }
        }
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
        DataMapTableView dataMapTableView = this.mapperManager.retrieveAbstractDataMapTableView(dataMapTableEntry
                .getParent());
        TableItem[] tableItems = new TableItem[0];
        List inputList = null;
        if (dataMapTableEntry instanceof IColumnEntry) {
            tableItems = dataMapTableView.getTableViewerCreatorForColumns().getTable().getItems();
            inputList = dataMapTableView.getTableViewerCreatorForColumns().getInputList();
        } else if (dataMapTableEntry instanceof FilterTableEntry) {
            tableItems = dataMapTableView.getTableViewerCreatorForFilters().getTable().getItems();
            inputList = dataMapTableView.getTableViewerCreatorForFilters().getInputList();
        } else if (dataMapTableEntry instanceof GlobalMapEntry) {
            tableItems = dataMapTableView.getTableViewerCreatorForGlobalMap().getTable().getItems();
            inputList = dataMapTableView.getTableViewerCreatorForGlobalMap().getInputList();
        } else if (dataMapTableEntry instanceof ExpressionFilterEntry) {
            return null;
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
            for (int i = 0; i < tableItems.length; i++) {
                if (tableItems[i].getData() == dataMapTableEntry) {
                    tableItem = tableItems[i];
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
        // System.out.println(previousColumnName + " -> " + newColumnName);

        // ExceptionHandler.process(new RuntimeException("test : " + previousColumnName + " -> " + newColumnName));
        // log.info(previousColumnName + " -> " + newColumnName); //$NON-NLS-1$

        TableEntryLocation tableEntryLocationKey = new TableEntryLocation(dataMapTableEntry.getParentName(),
                previousColumnName);
        // TableEntriesManager.buildLocation(dataMapTableEntry);
        ITableEntry entry = tableEntries.get(tableEntryLocationKey);
        if (entry != dataMapTableEntry) {
            log.warn(Messages.getString("TableEntriesManager.exceptionMessage.tableEntriesNotSame")); //$NON-NLS-1$
            return;
        }
        tableEntries.remove(tableEntryLocationKey);
        tableEntryLocationKey.columnName = newColumnName;
        tableEntries.put(tableEntryLocationKey, dataMapTableEntry);
        dataMapTableEntry.setName(newColumnName);
    }

    public static TableEntryLocation buildLocation(ITableEntry dataMapTableEntry) {
        return new TableEntryLocation(dataMapTableEntry.getParentName(), dataMapTableEntry.getName());
    }

    /**
     *
     * Event for TableEntriesManager. <br/>
     *
     * $Id$
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
