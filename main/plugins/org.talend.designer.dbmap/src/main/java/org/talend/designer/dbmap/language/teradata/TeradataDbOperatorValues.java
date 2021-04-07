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
package org.talend.designer.dbmap.language.teradata;

import java.util.List;

import org.talend.designer.dbmap.language.operator.DbOperator;
import org.talend.designer.dbmap.language.operator.DbOperatorValues;
import org.talend.designer.dbmap.language.operator.IDbOperator;

/**
 * ggu class global comment. Detailled comment
 */
public class TeradataDbOperatorValues extends DbOperatorValues {

    protected void init1() {
        initOperators(operators);
    }
    
    protected void initOperators(List<IDbOperator> operators) {
    	operators.add(getDefaultOperator());
        //operators.add(new DbOperator("EQUAL", "EQ")); //$NON-NLS-1$
        //
        operators.add(new DbOperator("DIFFERENT", "<>")); //$NON-NLS-1$ //$NON-NLS-2$
        operators.add(new DbOperator("STRICTLY_INFERIOR", "<")); //$NON-NLS-1$ //$NON-NLS-2$
        operators.add(new DbOperator("INFERIOR_OR_EQUAL", "<=")); //$NON-NLS-1$ //$NON-NLS-2$
        operators.add(new DbOperator("SUPERIOR", ">")); //$NON-NLS-1$ //$NON-NLS-2$
        operators.add(new DbOperator("SUPERIOR_OR_EQUAL", ">=")); //$NON-NLS-1$ //$NON-NLS-2$
    }

    public IDbOperator getDefaultOperator() {
        return new DbOperator("EQUAL", "="); //$NON-NLS-1$  //$NON-NLS-2$
    }
}
