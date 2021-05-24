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
package org.talend.designer.abstractmap.ui.dnd;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.widgets.TableItem;
import org.talend.core.model.metadata.IMetadataColumn;
import org.talend.designer.abstractmap.model.tableentry.ITableEntry;
import org.talend.designer.abstractmap.ui.IDataMapTableView;
import org.talend.designer.abstractmap.ui.visualmap.zone.IMapZone;

/**
 * DOC amaumont class global comment. Detailled comment <br/>
 *
 * $Id: DraggedData.java 898 2006-12-07 11:06:17Z amaumont $
 *
 */
public class DraggedData {

    private List<TransferableEntry> transferableEntryList = new ArrayList<TransferableEntry>();

    private IDataMapTableView dataMapTableViewSource;

    private TableItem tableItemSource;

    public DraggedData() {
        super();
    }

    /**
     * DOC amaumont Comment method "addEntry".
     */
    public void addEntry(ITableEntry tableEntry, IMetadataColumn metadataColumn, IMapZone zoneSourceEntry) {
        transferableEntryList.add(new TransferableEntry(tableEntry, metadataColumn, zoneSourceEntry));
    }

    public List<TransferableEntry> getTransferableEntryList() {
        return this.transferableEntryList;
    }

    /**
     * DOC amaumont Comment method "setDataMapTableViewSource".
     *
     * @param dataMapTableViewSource
     */
    public void setDataMapTableViewSource(IDataMapTableView dataMapTableViewSource) {
        this.dataMapTableViewSource = dataMapTableViewSource;
    }

    public IDataMapTableView getDataMapTableViewSource() {
        return this.dataMapTableViewSource;
    }

    /**
     * DOC amaumont Comment method "setTableItemSource".
     *
     * @param tableItemSource
     */
    public void setTableItemSource(TableItem tableItemSource) {
        this.tableItemSource = tableItemSource;
    }

    public TableItem getTableItemSource() {
        return this.tableItemSource;
    }

}
