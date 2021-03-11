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
package org.talend.designer.dbmap.managers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.swt.widgets.Table;
import org.talend.commons.utils.data.list.ListUtils;
import org.talend.core.model.metadata.IMetadataTable;
import org.talend.designer.abstractmap.model.table.IDataMapTable;
import org.talend.designer.abstractmap.model.tableentry.IColumnEntry;
import org.talend.designer.dbmap.external.connection.IOConnection;
import org.talend.designer.dbmap.model.table.AbstractInOutTable;
import org.talend.designer.dbmap.model.table.InputTable;
import org.talend.designer.dbmap.model.table.OutputTable;
import org.talend.designer.dbmap.model.table.VarsTable;
import org.talend.designer.dbmap.model.tableentry.FilterTableEntry;
import org.talend.designer.dbmap.ui.visualmap.table.DataMapTableView;

/**
 * DOC amaumont class global comment. Detailled comment <br/>
 *
 * $Id: TableManager.java 1782 2007-02-03 07:57:38Z bqian $
 *
 */
public class TableManager {

    private Map<IDataMapTable, DataMapTableView> abstractDataMapTableToView = new HashMap<IDataMapTable, DataMapTableView>();

    private Map<Table, DataMapTableView> swtTableToView = new HashMap<Table, DataMapTableView>();

    private ArrayList<OutputTable> listOutputsTables = new ArrayList<OutputTable>();

    private ArrayList<InputTable> listInputsTables = new ArrayList<InputTable>();

    private ArrayList<VarsTable> listVarsTables = new ArrayList<VarsTable>();

    private Map<IMetadataTable, AbstractInOutTable> metadataTableToIDataMapTable = new HashMap<IMetadataTable, AbstractInOutTable>();

    private HashMap<String, InputTable> aliasToInputTable = new HashMap<String, InputTable>();

    private MapperManager mapperManager;

    public TableManager(MapperManager mapperManager) {
        super();
        this.mapperManager = mapperManager;
    }

    /**
     * DOC amaumont Comment method "addTable".
     *
     * @param view
     * @param tableData
     */
    @SuppressWarnings("unchecked")
    void addTable(DataMapTableView view, IDataMapTable tableData) {

        if (tableData instanceof AbstractInOutTable) {
            AbstractInOutTable data = (AbstractInOutTable) tableData;
            metadataTableToIDataMapTable.put(data.getMetadataTable(), data);
        }
        getMatchedList(tableData).add(tableData);
        swtTableToView.put(view.getTableViewerCreatorForColumns().getTable(), view);
        if (view.getTableViewerCreatorForWhereFilters() != null) {
            swtTableToView.put(view.getTableViewerCreatorForWhereFilters().getTable(), view);
        }
        if (view.getTableViewerCreatorForOtherFilters() != null) {
            swtTableToView.put(view.getTableViewerCreatorForOtherFilters().getTable(), view);
        }
        abstractDataMapTableToView.put(tableData, view);
    }

    /**
     * DOC amaumont Comment method "getTable".
     */
    IDataMapTable getData(DataMapTableView view) {
        return view.getDataMapTable();
    }

    /**
     * DOC amaumont Comment method "getTable".
     */
    DataMapTableView getView(IDataMapTable data) {
        return abstractDataMapTableToView.get(data);
    }

    /**
     * DOC amaumont Comment method "getTable".
     */
    DataMapTableView getView(Table swtTable) {
        return swtTableToView.get(swtTable);
    }

    /**
     * DOC amaumont Comment method "removeTable".
     *
     * @param view
     */
    IDataMapTable removeTable(DataMapTableView view) {
        IDataMapTable abstractDataMapTable = getData(view);
        removeTable(abstractDataMapTable);
        return abstractDataMapTable;
    }

    /**
     * DOC amaumont Comment method "removeTable".
     *
     * @param dataTable
     */
    Object removeTable(IDataMapTable dataTable) {
        List<IColumnEntry> dataMapTableEntries = dataTable.getColumnEntries();
        TableEntriesManager tableEntriesManager = mapperManager.getTableEntriesManager();

        tableEntriesManager.removeAll(dataMapTableEntries, isPhysicalInputTable(dataTable.getName()));
        if (dataTable instanceof OutputTable) {
            List<FilterTableEntry> whereConstraintEntries = ((OutputTable) dataTable).getWhereFilterEntries();
            tableEntriesManager.removeAll(whereConstraintEntries, false);
            List<FilterTableEntry> otherConstraintEntries = ((OutputTable) dataTable).getOtherFilterEntries();
            tableEntriesManager.removeAll(otherConstraintEntries, false);
        }
        getMatchedList(dataTable).remove(dataTable);
        DataMapTableView view = abstractDataMapTableToView.remove(dataTable);
        swtTableToView.remove(view.getTableViewerCreatorForColumns().getTable());
        if (view.getTableViewerCreatorForWhereFilters() != null) {
            swtTableToView.remove(view.getTableViewerCreatorForWhereFilters().getTable());
        }
        if (view.getTableViewerCreatorForOtherFilters() != null) {
            swtTableToView.remove(view.getTableViewerCreatorForOtherFilters().getTable());
        }
        return view;
    }

    /**
     *
     * Return a copy.
     *
     * @return
     */
    Collection<DataMapTableView> getTablesView() {
        return abstractDataMapTableToView.values();
    }

    List<DataMapTableView> getInputsTablesView() {
        return getTablesView(listInputsTables, null);
    }

    List<DataMapTableView> getOutputsTablesView() {
        return getTablesView(listOutputsTables, null);
    }

    List<DataMapTableView> getVarsTablesView() {
        return getTablesView(listVarsTables, null);
    }

    private <T> List<T> castList(List<? super T> lstToCast) {
        ArrayList<T> list = new ArrayList<T>();
        for (Object object : lstToCast) {
            list.add((T) object);
        }
        return list;
    }

    private List<DataMapTableView> getTablesView(List<? extends IDataMapTable> listIDataMapTables, IMetadataTable metadataTable) {
        ArrayList<DataMapTableView> list = new ArrayList<DataMapTableView>();
        for (IDataMapTable data : listIDataMapTables) {
            if (metadataTable == null || metadataTable != null && data instanceof AbstractInOutTable
                    && metadataTable == ((AbstractInOutTable) data).getMetadataTable()) {
                list.add(abstractDataMapTableToView.get(data));
            }
        }
        return list;
    }

    /**
     *
     * Return an unmodifiable collection.
     *
     * @return
     */
    Collection<IDataMapTable> getTablesData() {
        return Collections.unmodifiableCollection(abstractDataMapTableToView.keySet());
    }

    /**
     *
     * Return an unmodifiable list.
     *
     * @return
     */
    List<InputTable> getInputTables() {
        return Collections.unmodifiableList(listInputsTables);
    }

    /**
     *
     * Return an unmodifiable list.
     *
     * @return
     */
    List<OutputTable> getOutputTables() {
        return Collections.unmodifiableList(listOutputsTables);
    }

    /**
     *
     * Return an unmodifiable list.
     *
     * @return
     */
    List<VarsTable> getVarsTables() {
        return Collections.unmodifiableList(listVarsTables);
    }

    AbstractInOutTable getData(IMetadataTable metadataTable) {
        return metadataTableToIDataMapTable.get(metadataTable);
    }

    DataMapTableView getView(IMetadataTable metadataTable) {
        IDataMapTable abstractDataMapTable = getData(metadataTable);
        if (abstractDataMapTable != null) {
            return getView(abstractDataMapTable);
        }
        return null;
    }

    /**
     * DOC amaumont Comment method "swapTableInputTables".
     *
     * @param dataMapTable
     * @param dataMapTable2
     */
    boolean swapWithNextTable(IDataMapTable dataMapTable) {
        List currentList = null;
        currentList = getMatchedList(dataMapTable);
        int indexTable = currentList.indexOf(dataMapTable);
        if (indexTable + 1 <= currentList.size() - 1) {
            Object nextTable = currentList.get(indexTable + 1);
            ListUtils.swap(currentList, dataMapTable, nextTable);
            return true;
        } else {
            return false;
        }
    }

    /**
     * DOC amaumont Comment method "swapTableInputTables".
     *
     * @param dataMapTable
     * @param dataMapTable2
     */
    boolean swapWithPreviousTable(IDataMapTable dataMapTable) {
        List currentList = null;
        currentList = getMatchedList(dataMapTable);
        int indexTable = currentList.indexOf(dataMapTable);
        if (indexTable - 1 >= 0) {
            Object nextTable = currentList.get(indexTable - 1);
            ListUtils.swap(currentList, dataMapTable, nextTable);
            return true;
        } else {
            return false;
        }
    }

    private List getMatchedList(IDataMapTable dataMapTable) {
        List currentList = null;
        if (dataMapTable instanceof InputTable) {
            currentList = listInputsTables;
        } else if (dataMapTable instanceof OutputTable) {
            currentList = listOutputsTables;
        } else if (dataMapTable instanceof VarsTable) {
            currentList = listVarsTables;
        }
        return currentList;
    }

    InputTable getInputTableFromAlias(String alias) {
        return aliasToInputTable.get(alias);
    }

    InputTable addAlias(String alias, InputTable inputTable) {
        return aliasToInputTable.put(alias, inputTable);
    }

    /**
     * DOC amaumont Comment method "getPhysicalTableNames".
     */
    public String[] getPhysicalInputTableNames() {
        List<IOConnection> inputConnections = mapperManager.getComponent().getMapperMain().getIoInputConnections();
        ArrayList<String> names = new ArrayList<String>();
        for (IOConnection connection : inputConnections) {
            String name = connection.getName();
            if (name != null) {
                names.add(name);
            }
        }
        return names.toArray(new String[0]);
    }

    public boolean isPhysicalInputTable(String tableName) {
        String[] physicalInputTableNames = getPhysicalInputTableNames();
        for (String tableNameLoop : physicalInputTableNames) {
            if (tableNameLoop.equals(tableName)) {
                return true;
            }
        }
        return false;
    }

    /**
     * DOC amaumont Comment method "getAliases".
     */
    public String[] getAliases() {
        List<InputTable> inputTables = getInputTables();
        ArrayList<String> names = new ArrayList<String>();
        for (InputTable table : inputTables) {
            String alias = table.getAlias();
            if (alias != null) {
                names.add(alias);
            }
        }
        return names.toArray(new String[0]);
    }

    /**
     * DOC amaumont Comment method "getVisibleTables".
     */
    public String[] getVisibleTables() {
        List<InputTable> inputTables = getInputTables();
        ArrayList<String> names = new ArrayList<String>();
        for (InputTable table : inputTables) {
            names.add(table.getName());
        }
        return names.toArray(new String[0]);
    }

}
