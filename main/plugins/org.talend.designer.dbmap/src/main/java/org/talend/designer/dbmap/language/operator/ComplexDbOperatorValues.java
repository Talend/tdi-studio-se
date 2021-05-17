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
public class ComplexDbOperatorValues extends DbOperatorValues {

    protected void init3() {
        operators.add(new DbOperator("EQUAL_ANY", "=", "( , )", false)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        operators.add(new DbOperator("DIFFERENT_ANY", "<> ANY", "( , )", false)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        operators.add(new DbOperator("STRICTLY_INFERIOR_ANY", "< ANY", "( , )", false)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        operators.add(new DbOperator("INFERIOR_OR_EQUAL_ANY", "<= ANY", "( , )", false)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        operators.add(new DbOperator("SUPERIOR_ANY", "> ANY", "( , )", false)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        operators.add(new DbOperator("SUPERIOR_OR_EQUAL_ANY", ">= ANY", "( , )", false)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

        operators.add(new DbOperator("EQUAL_SOME", "= SOME", "( , )", false)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        operators.add(new DbOperator("DIFFERENT_SOME", "<> SOME", "( , )", false)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        operators.add(new DbOperator("STRICTLY_INFERIOR_SOME", "< SOME", "( , )", false)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        operators.add(new DbOperator("INFERIOR_OR_EQUAL_SOME", "<= SOME", "( , )", false)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        operators.add(new DbOperator("SUPERIOR_SOME", "> SOME", "( , )", false)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        operators.add(new DbOperator("SUPERIOR_OR_EQUAL_SOME", ">= SOME", "( , )", false)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

        operators.add(new DbOperator("EQUAL_ALL", "= ALL", "( , )", false)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        operators.add(new DbOperator("DIFFERENT_ALL", "<> ALL", "( , )", false)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        operators.add(new DbOperator("STRICTLY_INFERIOR_ALL", "< ALL", "( , )", false)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        operators.add(new DbOperator("INFERIOR_OR_EQUAL_ALL", "<= ALL", "( , )", false)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        operators.add(new DbOperator("SUPERIOR_ALL", "> ALL", "( , )", false)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        operators.add(new DbOperator("SUPERIOR_OR_EQUAL_ALL", ">= ALL", "( , )", false)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }
}
