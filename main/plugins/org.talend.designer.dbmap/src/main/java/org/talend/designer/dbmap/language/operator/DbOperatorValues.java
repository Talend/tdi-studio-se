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

import java.util.ArrayList;
import java.util.List;

/**
 * ggu class global comment. Detailled comment
 */
public class DbOperatorValues {

    protected List<IDbOperator> operators = new ArrayList<IDbOperator>();

    public DbOperatorValues() {
        init1();
        init2();
        init3();
    }

    protected void init1() {
        operators.add(getDefaultOperator());
        //operators.add(new DbOperator("EQUAL", "=")); //$NON-NLS-1$
        //
        operators.add(new DbOperator("DIFFERENT", "<>")); //$NON-NLS-1$ //$NON-NLS-2$
        operators.add(new DbOperator("STRICTLY_INFERIOR", "<")); //$NON-NLS-1$ //$NON-NLS-2$
        operators.add(new DbOperator("INFERIOR_OR_EQUAL", "<=")); //$NON-NLS-1$ //$NON-NLS-2$
        operators.add(new DbOperator("SUPERIOR", ">")); //$NON-NLS-1$ //$NON-NLS-2$
        operators.add(new DbOperator("SUPERIOR_OR_EQUAL", ">=")); //$NON-NLS-1$ //$NON-NLS-2$
    }

    protected void init2() {
        operators.add(new DbOperator("IS_NULL", "IS NULL", null, true)); //$NON-NLS-1$ //$NON-NLS-2$
        operators.add(new DbOperator("IS_NOT_NULL", "IS NOT NULL", null, true)); //$NON-NLS-1$ //$NON-NLS-2$
        operators.add(new DbOperator("BETWEEN", "BETWEEN", " AND ", false)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        operators.add(new DbOperator("NOT_BETWEEN", "NOT BETWEEN", " AND ", false)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        operators.add(new DbOperator("IN", "IN", "( , )", false)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        operators.add(new DbOperator(" NOT_IN", "NOT IN", "( , )", false)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        operators.add(new DbOperator("LIKE", "LIKE", "'%'", false)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        operators.add(new DbOperator("NOT_LIKE", "NOT LIKE", "'%'", false)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        operators.add(new DbOperator("EXISTS", "EXISTS", "(SELECT * FROM MyTable)", false)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }

    protected void init3() {
        //
    }

    public IDbOperator[] getValues() {
        return operators.toArray(new IDbOperator[0]);
    }

    public IDbOperator getDefaultOperator() {
        return new DbOperator("EQUAL", "="); //$NON-NLS-1$  //$NON-NLS-2$
    }
}
