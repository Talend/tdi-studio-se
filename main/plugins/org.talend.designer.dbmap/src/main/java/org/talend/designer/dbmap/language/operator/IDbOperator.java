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

import org.talend.designer.dbmap.language.IDbKeyWord;

/**
 * DOC amaumont class global comment. Detailled comment <br/>
 *
 * $Id$
 *
 */
public interface IDbOperator extends IDbKeyWord {

    public abstract boolean isMonoOperand();

    public abstract String getOperator();

    public abstract String getAssociatedExpression();
    
    public abstract String getName();
}
