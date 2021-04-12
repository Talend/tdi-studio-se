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
package org.talend.designer.dbmap.language.hive;

import org.talend.designer.dbmap.language.operator.DbOperator;
import org.talend.designer.dbmap.language.operator.DbOperatorValues;

/**
 * created by talend2 on 2012-8-16 Detailled comment
 *
 */
public class HiveOperatorValues extends DbOperatorValues {

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.dbmap.language.operator.DbOperatorValues#init1()
     */
    @Override
    protected void init1() {
        operators.add(getDefaultOperator());
        operators.add(new DbOperator("EQUAL_NOT_NULL", "<=>")); //$NON-NLS-1$ //$NON-NLS-2$
        operators.add(new DbOperator("DIFFERENT", "<>")); //$NON-NLS-1$ //$NON-NLS-2$
        operators.add(new DbOperator("STRICTLY_INFERIOR", "<")); //$NON-NLS-1$ //$NON-NLS-2$
        operators.add(new DbOperator("INFERIOR_OR_EQUAL", "<=")); //$NON-NLS-1$ //$NON-NLS-2$
        operators.add(new DbOperator("SUPERIOR", ">")); //$NON-NLS-1$ //$NON-NLS-2$
        operators.add(new DbOperator("SUPERIOR_OR_EQUAL", ">=")); //$NON-NLS-1$ //$NON-NLS-2$
        operators.add(new DbOperator("LIKE", "LIKE")); //$NON-NLS-1$ //$NON-NLS-2$
        operators.add(new DbOperator("RLIKE", "RLIKE")); //$NON-NLS-1$ //$NON-NLS-2$
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.dbmap.language.operator.DbOperatorValues#init2()
     */
    @Override
    protected void init2() {
        operators.add(new DbOperator("IS_NULL", "IS NULL", null, true)); //$NON-NLS-1$ //$NON-NLS-2$
        operators.add(new DbOperator("IS_NOT_NULL", "IS NOT NULL", null, true)); //$NON-NLS-1$ //$NON-NLS-2$
        operators.add(new DbOperator("BETWEEN", "BETWEEN", " AND ", false)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        operators.add(new DbOperator("NOT_BETWEEN", "NOT BETWEEN", " AND ", false)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }

}
