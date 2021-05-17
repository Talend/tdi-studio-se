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
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.swt.widgets.Control;
import org.talend.commons.utils.data.list.ListUtils;
import org.talend.core.model.metadata.IMetadataTable;
import org.talend.designer.abstractmap.model.table.IDataMapTable;
import org.talend.designer.mapper.model.table.AbstractDataMapTable;
import org.talend.designer.mapper.model.table.AbstractInOutTable;
import org.talend.designer.mapper.model.table.InputTable;
import org.talend.designer.mapper.model.table.OutputTable;
import org.talend.designer.mapper.model.table.VarsTable;
import org.talend.designer.mapper.ui.visualmap.table.DataMapTableView;
import org.talend.designer.mapper.ui.visualmap.zone.Zone;

/**
 * DOC amaumont class global comment. Detailled comment <br/>
 *
 * $Id$
 *
 */
public class TableManager {

    private Map<IDataMapTable, DataMapTableView> abstractDataMapTableToView = new HashMap<IDataMapTable, DataMapTableView>();

    private Map<Control, DataMapTableView> swtTableToView = new HashMap<Control, DataMapTableView>();

    private ArrayList<OutputTable> listOutputsTables = new ArrayList<OutputTable>();

    private ArrayList<InputTable> listInputsTables = new ArrayList<InputTable>();

    private ArrayList<VarsTable> listVarsTables = new ArrayList<VarsTable>();

    private Map<IMetadataTable, AbstractInOutTable> metadataTableToAbstractDataMapTable = new HashMap<IMetadataTable, AbstractInOutTable>();

    public TableManager() {
        super();
    }

    /**
     * DOC amaumont Comment method "addTable".
     *
     * @param view
     * @param tableData
     */
    @SuppressWarnings("unchecked")//$NON-NLS-1$
    void addTable(DataMapTableView view, IDataMapTable tableData) {

        if (tableData instanceof AbstractInOutTable) {
            AbstractInOutTable data = (AbstractInOutTable) tableData;
            metadataTableToAbstractDataMapTable.put(data.getMetadataTable(), data);
        }
        if (tableData.getMapperManager() != null)
            if (tableData instanceof OutputTable && ((OutputTable) tableData).isErrorRejectTable()) {
                getMatchedList(tableData).add(0, tableData);
            } else {
                getMatchedList(tableData).add(tableData);
            }
        if (view.getZone() == Zone.INPUTS || view.getZone() == Zone.OUTPUTS) {
            swtTableToView.put(view.getExpressionFilterText(), view);
            swtTableToView.put(view.getColumnNameFilterText(),view);
        }
        swtTableToView.put(view.getTableViewerCreatorForColumns().getTable(), view);
        if (view.getTableViewerCreatorForFilters() != null) {
            swtTableToView.put(view.getTableViewerCreatorForFilters().getTable(), view);
        }
        if (view.getTableViewerCreatorForGlobalMap() != null) {
            swtTableToView.put(view.getTableViewerCreatorForGlobalMap().getTable(), view);
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
    DataMapTableView getView(Control swtControl) {
        return swtTableToView.get(swtControl);
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
     * @param data
     */
    Object removeTable(IDataMapTable data) {
        getMatchedList(data).remove(data);
        DataMapTableView view = abstractDataMapTableToView.remove(data);
        swtTableToView.remove(view.getTableViewerCreatorForColumns().getTable());
        swtTableToView.remove(view.getExpressionFilterText());
        swtTableToView.remove(view.getColumnNameFilterText());
        if (view.getTableViewerCreatorForFilters() != null) {
            swtTableToView.remove(view.getTableViewerCreatorForFilters().getTable());
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
        return getTablesView(listInputsTables);
    }

    List<DataMapTableView> getOutputsTablesView() {
        return getTablesView(listOutputsTables);
    }

    List<DataMapTableView> getVarsTablesView() {
        return getTablesView(listVarsTables);
    }

    private List<DataMapTableView> getTablesView(List<? extends AbstractDataMapTable> listAbstractDataMapTables) {
        ArrayList<DataMapTableView> list = new ArrayList<DataMapTableView>();
        for (IDataMapTable data : listAbstractDataMapTables) {
            list.add(abstractDataMapTableToView.get(data));
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
        return metadataTableToAbstractDataMapTable.get(metadataTable);
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

}
