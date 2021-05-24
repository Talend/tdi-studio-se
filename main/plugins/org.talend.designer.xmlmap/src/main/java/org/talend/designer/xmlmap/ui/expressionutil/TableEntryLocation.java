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
package org.talend.designer.xmlmap.ui.expressionutil;

/**
 * wchen class global comment. Detailled comment
 */
public class TableEntryLocation {

    public TableEntryLocation(String prefix, String tableName, String columnValue, String sufix) {
        this.prefix = prefix.trim();
        this.tableName = tableName.trim();
        this.columnValue = columnValue.trim();
        this.sufix = sufix.trim();
    }

    public TableEntryLocation(String tableName, String columnValue) {
        this("", tableName, columnValue, "");
    }

    String prefix;

    String sufix;

    String tableName;

    String columnValue;

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public String getSufix() {
        return sufix;
    }

    public void setSufix(String sufix) {
        this.sufix = sufix;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getColumnValue() {
        return columnValue;
    }

    public void setColumnValue(String columnValue) {
        this.columnValue = columnValue;
    }

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
        if (this.columnValue == null) {
            if (other.columnValue != null) {
                return false;
            }
        } else if (!this.columnValue.equals(other.columnValue)) {
            return false;
        }
        if (this.tableName == null) {
            if (other.tableName != null) {
                return false;
            }
        } else if (!this.tableName.equals(other.tableName)) {
            return false;
        }

        if (this.prefix == null) {
            if (other.prefix != null) {
                return false;
            }
        } else if (!this.prefix.equals(other.prefix)) {
            return false;
        }

        if (this.sufix == null) {
            if (other.sufix != null) {
                return false;
            }
        } else if (!this.sufix.equals(other.sufix)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuffer buffer = new StringBuffer();
        buffer.append(prefix);
        buffer.append(tableName);
        buffer.append(".");
        buffer.append(columnValue);
        buffer.append(sufix);
        return buffer.toString();
    }

}
