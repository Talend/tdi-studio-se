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

import org.talend.core.model.metadata.IMetadataColumn;
import org.talend.designer.abstractmap.model.table.IDataMapTable;
import org.talend.designer.dbmap.managers.MapperManager;

/**
 * DOC amaumont class global comment. Detailled comment <br/>
 *
 * $Id: InputColumnTableEntry.java 898 2006-12-07 11:06:17Z amaumont $
 *
 */
public class InputColumnTableEntry extends AbstractInOutTableEntry {

    private String operator;

    private boolean join;

    /** unmatch entry with metadata entries */
    private boolean unmatchingEntry;

    private String originalExpression;

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

    /**
     * Getter for join.
     *
     * @return the join
     */
    public boolean isJoin() {
        return this.join;
    }

    /**
     * Sets the join.
     *
     * @param join the join to set
     */
    public void setJoin(boolean join) {
        this.join = join;
    }

    /**
     * Getter for originalExpression.
     *
     * @return the originalExpression
     */
    public String getOriginalExpression() {
        return this.originalExpression;
    }

    /**
     * Sets the originalExpression. Used to save the original expression while operator is not accepted, so expression
     * will be restored if operator has not changed.
     *
     * @param expressionBackup the originalExpression to set
     */
    public void setOriginalExpression(String originalExpression) {
        this.originalExpression = originalExpression;
    }

    public boolean isUnmatchingEntry() {
        return ((MapperManager) getParent().getMapperManager()).isUnmatchingEntry(this);
    }

}
