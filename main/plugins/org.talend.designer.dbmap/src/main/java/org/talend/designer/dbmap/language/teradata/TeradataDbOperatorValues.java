// ============================================================================
//
// Copyright (C) 2006-2017 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.designer.dbmap.language.teradata;

import org.talend.designer.dbmap.language.operator.DbOperator;
import org.talend.designer.dbmap.language.operator.DbOperatorValues;
import org.talend.designer.dbmap.language.operator.IDbOperator;

/**
 * ggu class global comment. Detailled comment
 */
public class TeradataDbOperatorValues extends DbOperatorValues {

    protected void init1() {
        operators.add(getDefaultOperator());
        //operators.add(new DbOperator("EQUAL", "EQ")); //$NON-NLS-1$
        //
        operators.add(new DbOperator("DIFFERENT", "NE")); //$NON-NLS-1$ //$NON-NLS-2$
        operators.add(new DbOperator("STRICTLY_INFERIOR", "LT")); //$NON-NLS-1$ //$NON-NLS-2$
        operators.add(new DbOperator("INFERIOR_OR_EQUAL", "LE")); //$NON-NLS-1$ //$NON-NLS-2$
        operators.add(new DbOperator("SUPERIOR", "GT")); //$NON-NLS-1$ //$NON-NLS-2$
        operators.add(new DbOperator("SUPERIOR_OR_EQUAL", "GE")); //$NON-NLS-1$ //$NON-NLS-2$

    }

    public IDbOperator getDefaultOperator() {
        return new DbOperator("EQUAL", "EQ"); //$NON-NLS-1$  //$NON-NLS-2$ 
    }
}
