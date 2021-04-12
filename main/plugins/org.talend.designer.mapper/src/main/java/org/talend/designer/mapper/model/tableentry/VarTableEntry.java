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

import org.talend.designer.abstractmap.model.table.IDataMapTable;
import org.talend.designer.abstractmap.model.tableentry.IColumnEntry;

/**
 * DOC amaumont class global comment. Detailled comment <br/>
 *
 * $Id$
 *
 */
public class VarTableEntry extends DataMapTableEntry implements IColumnEntry {

    private String type;

    private boolean nullable;

    public VarTableEntry(IDataMapTable abstractDataMapTable, String name, String expression, String type) {
        super(abstractDataMapTable, name, expression);
        this.type = type;
    }

    public VarTableEntry(IDataMapTable abstractDataMapTable, String name) {
        this(abstractDataMapTable, name, null, null);
    }

    /**
     * Getter for type.
     *
     * @return the type
     */
    public String getType() {
        return this.type;
    }

    /**
     * Sets the type.
     *
     * @param type the type to set
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * Getter for nullable.
     *
     * @return the nullable
     */
    public boolean isNullable() {
        return this.nullable;
    }

    /**
     * Sets the nullable.
     *
     * @param nullable the nullable to set
     */
    public void setNullable(boolean nullable) {
        this.nullable = nullable;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.mapper.model.tableentry.IDataMapTableEntry#isTableEntry()
     */
    public boolean isTableEntry() {
        return true;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.mapper.model.tableentry.IDataMapTableEntry#isColumnEntry()
     */
    public boolean isColumnEntry() {
        return true;
    }

}
