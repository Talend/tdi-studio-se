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
package org.talend.sqlbuilder.actions;

import org.talend.sqlbuilder.sqlcontrol.AbstractSQLExecution;

/**
 * DOC qianbing class global comment. Detailled comment <br/>
 *
 * $Id: IResultDisplayer.java,v 1.2 2006/10/25 21:05:23 qiang.zhang Exp $
 *
 */
public interface IResultDisplayer {

    public void addSQLExecution(AbstractSQLExecution exe) throws Exception;
}
