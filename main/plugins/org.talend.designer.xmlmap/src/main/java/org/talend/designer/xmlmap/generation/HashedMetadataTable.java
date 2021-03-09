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
package org.talend.designer.xmlmap.generation;

import java.util.HashMap;
import java.util.List;

import org.talend.core.model.metadata.IMetadataColumn;
import org.talend.core.model.metadata.IMetadataTable;

/**
 *
 * DOC rdubois JavaGenerationManager class global comment. Detailled comment <br/>
 *
 */
public class HashedMetadataTable {

    private IMetadataTable metadataTable;

    private HashMap<String, IMetadataColumn> hashMetataColumns;

    /**
     * DOC rdubois JavaGenerationManager.HashedMetadataTable constructor comment.
     */
    public HashedMetadataTable(IMetadataTable metadataTable) {
        this.metadataTable = metadataTable;
        hashMetataColumns = new HashMap<String, IMetadataColumn>();
        List<IMetadataColumn> listColumns = metadataTable.getListColumns();
        int listColumnsListSize = listColumns.size();
        for (int i = 0; i < listColumnsListSize; i++) {
            IMetadataColumn column = listColumns.get(i);
            hashMetataColumns.put(column.getLabel(), column);
        }
    }

    public IMetadataColumn getColumn(String columnName) {
        return hashMetataColumns.get(columnName);
    }

    /**
     * Getter for metadataTable.
     *
     * @return the metadataTable
     */
    public IMetadataTable getMetadataTable() {
        return this.metadataTable;
    }
}
