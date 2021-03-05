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
package org.talend.designer.dbmap.language.operator;

/**
 * ggu class global comment. Detailled comment
 */
public abstract class AbstractDbOperatorManager implements IDbOperatorManager {

    private DbOperatorValues operatorValues;

    public AbstractDbOperatorManager() {
        operatorValues = new ComplexDbOperatorValues();
    }

    public AbstractDbOperatorManager(DbOperatorValues operatorValues) {
        this.operatorValues = operatorValues;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.dbmap.language.operator.IDbOperatorManager#getDefaultEqualOperator()
     */
    public IDbOperator getDefaultEqualOperator() {
        return operatorValues.getDefaultOperator();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.dbmap.language.IDbOperatorManager#getOperatorFromValue(java.lang.String)
     */
    public IDbOperator getOperatorFromValue(String operatorValue) {
        IDbOperator[] operators = getOperators();
        for (int i = 0; i < operators.length; i++) {
            if (operators[i].getOperator().equals(operatorValue)) {
                return operators[i];
            }
        }
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.dbmap.language.operator.IDbOperatorManager#getOperators()
     */
    public IDbOperator[] getOperators() {
        return operatorValues.getValues();
    }

}
