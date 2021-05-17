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
package org.talend.designer.dbmap.model.tableentry;

import org.talend.core.model.metadata.IMetadataColumn;
import org.talend.designer.abstractmap.model.table.IDataMapTable;
import org.talend.designer.abstractmap.model.tableentry.IColumnEntry;

/**
 * DOC amaumont class global comment. Detailled comment <br/>
 *
 * $Id: AbstractInOutTableEntry.java 898 2006-12-07 11:06:17Z amaumont $
 *
 */
public class AbstractInOutTableEntry extends TableEntry implements IColumnEntry {

    private IMetadataColumn metadataColumn;

    public AbstractInOutTableEntry(IDataMapTable abstractDataMapTable, IMetadataColumn metadataColumn, String expression) {
        super(abstractDataMapTable, expression);
        this.metadataColumn = metadataColumn;
    }

    public AbstractInOutTableEntry(IDataMapTable abstractDataMapTable, IMetadataColumn metadataColumn) {
        super(abstractDataMapTable);
        this.metadataColumn = metadataColumn;
    }

    public AbstractInOutTableEntry(IDataMapTable abstractDataMapTable, String name, String expression) {
        super(abstractDataMapTable, name, expression);
    }

    public IMetadataColumn getMetadataColumn() {
        return this.metadataColumn;
    }

    @Override
    public String getName() {
        return this.metadataColumn.getLabel();
    }

    @Override
    public void setName(String name) {
        this.metadataColumn.setLabel(name);
    }

}
