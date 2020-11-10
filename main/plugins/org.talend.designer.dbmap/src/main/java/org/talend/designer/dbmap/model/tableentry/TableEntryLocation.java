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
package org.talend.designer.dbmap.model.tableentry;

import org.talend.designer.abstractmap.model.tableentry.ITableEntry;
import org.talend.designer.dbmap.language.DbLanguageConstants;

/**
 * DOC amaumont class global comment. Detailled comment <br/>
 *
 * $Id: TableEntryLocation.java 1782 2007-02-03 07:57:38Z bqian $
 *
 */
public class TableEntryLocation {

    public String tableName;

    public String columnName;

    public TableEntryLocation() {
        super();
    }

    public TableEntryLocation(TableEntryLocation tableEntryLocation) {
        this.tableName = tableEntryLocation.tableName;
        this.columnName = tableEntryLocation.columnName;
    }

    public TableEntryLocation(String tableName, String columnName) {
        this.tableName = (tableName == null ? null : tableName.trim());
        this.columnName = (columnName == null ? null : columnName.trim());
    }

    @Override
    public String toString() {
        StringBuffer buffer = new StringBuffer();
        buffer.append(tableName);
        buffer.append(DbLanguageConstants.PREFIX_FIELD_NAME);
        buffer.append(columnName);
        return buffer.toString();
    }

    public static TableEntryLocation getNewInstance(ITableEntry dataMapTableEntry) {
        return new TableEntryLocation(dataMapTableEntry.getParent().getName(), dataMapTableEntry.getName());
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((this.columnName == null) ? 0 : this.columnName.hashCode());
        result = prime * result + ((this.tableName == null) ? 0 : this.tableName.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final TableEntryLocation other = (TableEntryLocation) obj;
        if (this.columnName == null) {
            if (other.columnName != null) {
                return false;
            }
        } else if (!this.columnName.equals(other.columnName)) {
            return false;
        }
        if (this.tableName == null) {
            if (other.tableName != null) {
                return false;
            }
        } else if (!this.tableName.equals(other.tableName)) {
            return false;
        }

        return true;
    }

    public String getTableName() {
        return this.tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getColumnName() {
        return this.columnName;
    }

    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }
}
