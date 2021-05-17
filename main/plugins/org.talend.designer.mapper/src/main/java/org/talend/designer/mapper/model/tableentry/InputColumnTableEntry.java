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

import org.talend.core.model.metadata.IMetadataColumn;
import org.talend.designer.abstractmap.model.table.IDataMapTable;

/**
 * DOC amaumont class global comment. Detailled comment <br/>
 *
 * $Id$
 *
 */
public class InputColumnTableEntry extends AbstractInOutTableEntry {

    private String operator;

    public InputColumnTableEntry(IDataMapTable abstractDataMapTable, IMetadataColumn metadataColumn, String expression) {
        super(abstractDataMapTable, metadataColumn, expression);
    }

    public InputColumnTableEntry(IDataMapTable abstractDataMapTable, IMetadataColumn metadataColumn) {
        super(abstractDataMapTable, metadataColumn);
    }

    /**
     * Getter for operator.
     *
     * @return the operator
     */
    public String getOperator() {
        return this.operator;
    }

    /**
     * Sets the operator.
     *
     * @param operator the operator to set
     */
    public void setOperator(String operator) {
        this.operator = operator;
    }

}
