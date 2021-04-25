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

/**
 * DOC amaumont class global comment. Detailled comment <br/>
 *
 */
public class ExpressionFilterEntry extends DataMapTableEntry {

    /**
     * DOC amaumont ExpressionFilterEntry constructor comment.
     *
     * @param abstractDataMapTable
     * @param name
     * @param expression
     */
    public ExpressionFilterEntry(IDataMapTable abstractDataMapTable, String name, String expression) {
        super(abstractDataMapTable, name, expression);
    }

    /**
     * DOC amaumont ExpressionFilterEntry constructor comment.
     *
     * @param abstractDataMapTable
     * @param expression
     */
    public ExpressionFilterEntry(IDataMapTable abstractDataMapTable, String expression) {
        super(abstractDataMapTable, expression);
    }

    /**
     * DOC amaumont ExpressionFilterEntry constructor comment.
     *
     * @param abstractDataMapTable
     */
    public ExpressionFilterEntry(IDataMapTable abstractDataMapTable) {
        super(abstractDataMapTable);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.mapper.model.tableentry.IDataMapTableEntry#isTableEntry()
     */
    public boolean isTableEntry() {
        return false;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.mapper.model.tableentry.IDataMapTableEntry#isColumnEntry()
     */
    public boolean isColumnEntry() {
        return false;
    }

}
