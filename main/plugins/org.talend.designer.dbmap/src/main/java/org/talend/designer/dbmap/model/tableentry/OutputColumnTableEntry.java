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
import org.talend.designer.dbmap.model.table.OutputTable;

/**
 * DOC amaumont class global comment. Detailled comment <br/>
 *
 * $Id: OutputColumnTableEntry.java 898 2006-12-07 11:06:17Z amaumont $
 *
 */
public class OutputColumnTableEntry extends AbstractInOutTableEntry {

    public OutputColumnTableEntry(IDataMapTable abstractDataMapTable, IMetadataColumn metadataColumn, String expression) {
        super(abstractDataMapTable, metadataColumn, expression);
    }

    public OutputColumnTableEntry(IDataMapTable abstractDataMapTable, IMetadataColumn metadataColumn) {
        super(abstractDataMapTable, metadataColumn);
    }

    @Override
    public String getParentName() {
        return ((OutputTable) getParent()).getUniqueName();
    }

}
