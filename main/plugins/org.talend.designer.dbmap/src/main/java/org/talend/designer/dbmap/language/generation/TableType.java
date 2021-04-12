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
package org.talend.designer.dbmap.language.generation;

import org.talend.designer.dbmap.model.table.VarsTable;

/**
 *
 * DOC amaumont MapperDataTestGenerator class global comment. Detailled comment <br/>
 *
 * $Id: TableType.java 1782 2007-02-03 07:57:38Z bqian $
 *
 */
public enum TableType {
    INPUT("in", 10), //$NON-NLS-1$
    OUTPUT("out", 10), //$NON-NLS-1$
    VARS(VarsTable.PREFIX_VARS_TABLE_NAME, 1);

    String baseTableName;

    int nTables;

    TableType(String baseTableName, int nTables) {
        this.baseTableName = baseTableName;
        this.nTables = nTables;
    }

    public String getBaseTableName() {
        return this.baseTableName;
    }

    public int getNTables() {
        return this.nTables;
    }

};
