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

import org.talend.designer.abstractmap.model.table.IDataMapTable;

/**
 * DOC amaumont class global comment. Detailled comment <br/>
 *
 * $Id: FilterTableEntry.java 898 2006-12-07 11:06:17Z amaumont $
 *
 */
public class FilterTableEntry extends TableEntry {

    public static final String WHERE_FILTER = "WHERE_FILTER"; //$NON-NLS-1$

    public static final String OTHER_FILTER = "OTHER_FILTER"; //$NON-NLS-1$

    private String filterKind;

    public FilterTableEntry(IDataMapTable abstractDataMapTable, String name, String expression) {
        super(abstractDataMapTable, name, expression);
        this.filterKind = WHERE_FILTER;
    }

    public FilterTableEntry(IDataMapTable abstractDataMapTable, String name, String expression, String _filterKind) {
        super(abstractDataMapTable, name, expression);
        this.filterKind = _filterKind;
    }

    /**
     * Getter for filterKind.
     *
     * @return the filterKind
     */
    public String getFilterKind() {
        return this.filterKind;
    }

    /**
     * Sets the filterKind.
     *
     * @param filterKind the filterKind to set, can be :<br>
     * FilterTableEntry.WHERE_FILTER<br>
     * FilterTableEntry.OTHER_FILTER
     */
    public void setFilterKind(String filterKind) {
        this.filterKind = filterKind;
    }

}
