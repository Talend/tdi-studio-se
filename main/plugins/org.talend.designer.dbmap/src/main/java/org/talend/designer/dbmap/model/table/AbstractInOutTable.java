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

import org.talend.core.model.metadata.IMetadataColumn;
import org.talend.core.model.metadata.IMetadataTable;
import org.talend.designer.dbmap.external.data.ExternalDbMapTable;
import org.talend.designer.dbmap.managers.MapperManager;
import org.talend.designer.dbmap.model.tableentry.AbstractInOutTableEntry;

/**
 * DOC amaumont class global comment. Detailled comment <br/>
 *
 * $Id: AbstractInOutTable.java 1744 2007-01-31 13:05:30Z amaumont $
 *
 */
public abstract class AbstractInOutTable extends AbstractDataMapTable {

    protected IMetadataTable metadataTable;

    private boolean activateColumnNameFilter;

    private String columnNameFilter;
    /**
     *
     * DOC amaumont AbstractInOutTable constructor comment.
     *
     * @param metadataTable
     * @param externalMapperTable can be null
     * @param name
     */
    public AbstractInOutTable(MapperManager mapperManager, IMetadataTable metadataTable, String name) {
        super(mapperManager, name);
        this.metadataTable = metadataTable;
    }

    protected void initFromExternalData(ExternalDbMapTable externalMapperTable) {
        super.initFromExternalData(externalMapperTable);
        if (externalMapperTable != null) {
            this.activateColumnNameFilter = externalMapperTable.isActivateColumnNameFilter();
            this.setColumnNameFilter(externalMapperTable.getColumnNameFilter());
        }
    }

    protected abstract AbstractInOutTableEntry getNewTableEntry(IMetadataColumn metadataColumn);

    public IMetadataTable getMetadataTable() {
        return this.metadataTable;
    }

    protected void setMetadataTable(IMetadataTable metadataTable) {
        this.metadataTable = metadataTable;
    }

    public boolean isActivateColumnNameFilter() {
        return this.activateColumnNameFilter;
    }

    public void setActiveColumnNameFilter(boolean activateColumnNameFilter) {
        this.activateColumnNameFilter = activateColumnNameFilter;
    }

    public String getColumnNameFilter() {
        return this.columnNameFilter;
    }

    public void setColumnNameFilter(String columnNameFilter) {
        this.columnNameFilter = columnNameFilter;
    }
}
