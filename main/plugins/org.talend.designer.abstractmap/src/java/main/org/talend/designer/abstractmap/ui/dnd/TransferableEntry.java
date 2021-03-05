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

import org.talend.core.model.metadata.IMetadataColumn;
import org.talend.designer.abstractmap.model.tableentry.ITableEntry;
import org.talend.designer.abstractmap.ui.visualmap.zone.IMapZone;

/**
 * DOC amaumont class global comment. Detailled comment <br/>
 *
 * $Id: TransferableEntry.java 898 2006-12-07 11:06:17Z amaumont $
 *
 */
public class TransferableEntry {

    private IMetadataColumn metadataColumn;

    private ITableEntry tableEntrySource;

    private IMapZone zoneSourceEntry;

    public TransferableEntry(ITableEntry tableEntryLocation, IMetadataColumn metadataColumn, IMapZone zoneSourceEntry) {
        super();
        this.tableEntrySource = tableEntryLocation;
        this.metadataColumn = metadataColumn;
        this.zoneSourceEntry = zoneSourceEntry;
    }

    public void setTableEntryLocationSource(ITableEntry tableEntryLocation) {
        this.tableEntrySource = tableEntryLocation;
    }

    public ITableEntry getTableEntrySource() {
        return this.tableEntrySource;
    }

    public IMetadataColumn getMetadataColumn() {
        return this.metadataColumn;
    }

    public void setMetadataColumn(IMetadataColumn metadataColumn) {
        this.metadataColumn = metadataColumn;
    }

    public IMapZone getZoneSourceEntry() {
        return this.zoneSourceEntry;
    }

    public void setZoneSourceEntry(IMapZone zone) {
        this.zoneSourceEntry = zone;
    }

}
