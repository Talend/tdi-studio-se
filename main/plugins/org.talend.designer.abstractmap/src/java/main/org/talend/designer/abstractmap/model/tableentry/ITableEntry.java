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
package org.talend.designer.abstractmap.model.tableentry;

import java.util.List;

import org.talend.core.model.process.Problem;
import org.talend.designer.abstractmap.model.table.IDataMapTable;

/**
 * DOC amaumont class global comment. Detailled comment <br/>
 *
 * $Id: IDataMapTableEntry.java 3869 2007-06-15 14:52:12Z amaumont $
 *
 */
public interface ITableEntry {

    public String getName();

    public void setName(String name);

    public String getParentName();

    public IDataMapTable getParent();

    public String getExpression();

    public void setExpression(String expression);

    public List<Problem> getProblems();

    public void setProblems(List<Problem> errorMessage);

    /**
     * Return true if this entry is displayed in a SWT Table.
     *
     * @return true if this entry is displayed in a SWT Table.
     */
    public boolean isTableEntry();

    /**
     *
     * Return true if this entry represents a column.
     *
     * @return true if this entry represents a column.
     */
    public boolean isColumnEntry();

}
