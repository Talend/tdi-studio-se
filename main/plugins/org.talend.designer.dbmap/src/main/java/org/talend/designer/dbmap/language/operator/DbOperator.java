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
package org.talend.designer.dbmap.language.operator;

/**
 * ggu class global comment. Detailled comment
 */
public class DbOperator implements IDbOperator {

    private String name;

    private String operator;

    private String associatedExpression;

    private boolean monoOperand;

    /**
     * amaumont DbOperator constructor comment.
     *
     * @param operator
     * @param expressionPattern
     * @param monoOperand
     */
    public DbOperator(String name, String operator, String expressionPattern, boolean monoOperand) {
        this.name = name;
        this.operator = operator;
        this.associatedExpression = expressionPattern;
        this.monoOperand = monoOperand;
    }

    /**
     * amaumont DbOperator constructor comment.
     *
     * @param operator
     * @param associatedExpression
     * @param monoOperand
     */
    public DbOperator(String name, String operator) {
        this(name, operator, null, false);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.dbmap.language.IDbOperator#getValue()
     */
    public String getOperator() {
        return operator;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.dbmap.language.IDbOperator#getAssociatedExpression()
     */
    public String getAssociatedExpression() {
        return associatedExpression;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.dbmap.language.IDbOperator#isMonoOperand()
     */
    public boolean isMonoOperand() {
        return monoOperand;
    }
    
    public String getName() {
    	return this.name;
    }

}
