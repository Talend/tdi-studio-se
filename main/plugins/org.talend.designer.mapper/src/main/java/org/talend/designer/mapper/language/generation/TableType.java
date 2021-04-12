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
package org.talend.designer.mapper.language.generation;

import org.talend.designer.mapper.model.table.VarsTable;

/**
 *
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
