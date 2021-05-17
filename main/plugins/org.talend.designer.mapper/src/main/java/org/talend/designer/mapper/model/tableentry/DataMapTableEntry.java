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
package org.talend.designer.mapper.model.tableentry;

import java.util.List;
import java.util.Map;

import org.talend.core.model.process.IConnection;
import org.talend.core.model.process.Problem;
import org.talend.core.model.process.TraceData;
import org.talend.designer.abstractmap.model.table.IDataMapTable;
import org.talend.designer.abstractmap.model.tableentry.ITableEntry;
import org.talend.designer.mapper.model.table.AbstractInOutTable;
import org.talend.designer.mapper.model.table.OutputTable;

/**
 * DOC amaumont class global comment. Detailled comment <br/>
 *
 * $Id$
 *
 */
public abstract class DataMapTableEntry implements ITableEntry {

    private String expression;

    private IDataMapTable parent;

    private String name;

    private List<Problem> problems;

    public DataMapTableEntry(IDataMapTable abstractDataMapTable, String expression) {
        super();
        this.parent = abstractDataMapTable;
        this.expression = expression;
    }

    public DataMapTableEntry(IDataMapTable abstractDataMapTable) {
        this(abstractDataMapTable, null);
    }

    /**
     * DOC amaumont ITableEntry constructor comment.
     *
     * @param abstractDataMapTable
     * @param name, can't be null
     */
    public DataMapTableEntry(IDataMapTable abstractDataMapTable, String name, String expression) {
        super();
        this.parent = abstractDataMapTable;
        this.name = name;
        if (this.name == null) {
            throw new IllegalArgumentException("Name of the TableEntry must not be null !"); //$NON-NLS-1$
        }
        this.expression = expression;
    }

    public String getExpression() {
        return this.expression;
    }

    public void setExpression(String expression) {
        this.expression = expression;
    }

    public IDataMapTable getParent() {
        return this.parent;
    }

    public void setParent(IDataMapTable parent) {
        this.parent = parent;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getParentName() {
        if (parent != null) {
            return parent.getName();
        } else {
            throw new IllegalStateException("parent null"); //$NON-NLS-1$
        }
    }

    public List<Problem> getProblems() {
        return this.problems;
    }

    public void setProblems(List<Problem> problems) {
        this.problems = problems;
    }

    public TraceData getPreviewValue() {
        if (getParent() instanceof AbstractInOutTable) {
            AbstractInOutTable abstractTable = (AbstractInOutTable) getParent();
            if (abstractTable.getConnection() != null) {
                IConnection connection = abstractTable.getConnection().getConnecion();
                if (connection != null) {
                    Map<String, TraceData> traceData = connection.getTraceData();
                    if (traceData != null) {
                        if (abstractTable instanceof OutputTable) {
                            OutputTable output = (OutputTable) abstractTable;
                            String key = null;
                            if (output.getIsJoinTableOf() != null) {
                                key = output.getIsJoinTableOf() + ":" + output.getName();
                            } else if (hasJoinedTable(output)) {
                                key = output.getName() + "[MAIN]";
                            } else {
                                // single output
                                key = output.getName();
                            }
                            return traceData.get(key);

                        } else {
                            return traceData.get(abstractTable.getName());
                        }
                    }
                }
            }
        }

        return null;
    }

    private boolean hasJoinedTable(OutputTable currentTable) {
        List<OutputTable> outputTables = currentTable.getMapperManager().getOutputTables();
        for (OutputTable table : outputTables) {
            if (table.getIsJoinTableOf() != null && table.getIsJoinTableOf().equals(currentTable.getName())) {
                return true;
            }
        }
        return false;
    }

}
