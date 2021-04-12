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
package org.talend.designer.mapper.model.tableentry;

import org.talend.designer.abstractmap.model.tableentry.ITableEntry;

/**
 * DOC amaumont class global comment. Detailled comment <br/>
 *
 * $Id$
 *
 */
public class TableEntryLocation {

    public String tableName;

    public String columnName;

    public TableEntryLocation() {
        super();
    }

    /**
     * DOC amaumont Couple constructor comment.
     *
     * @param tableName
     * @param columnName
     */
    public TableEntryLocation(String tableName, String columnName) {
        this.tableName = tableName;
        this.columnName = columnName;
    }

    /**
     * DOC amaumont Couple constructor comment.
     *
     * @param tableName
     * @param columnName
     */
    public TableEntryLocation(TableEntryLocation tableEntryLocation) {
        this.tableName = tableEntryLocation.tableName;
        this.columnName = tableEntryLocation.columnName;
    }

    public String toString() {
        return "{tableName=" + this.tableName + ", columnName=" + this.columnName + "}"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
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

}
