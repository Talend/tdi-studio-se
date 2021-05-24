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
package org.talend.sqlbuilder.dataset.dataset;

/**
 * DataSetRow, represents one row in a dataSet.
 *
 * @author Davy Vanherbergen
 */
public class DataSetRow {

    private Object[] pvalues;

    /**
     * Create new DataSetRow with columnCount values.
     *
     * @param columnCount number of columns
     */
    public DataSetRow(int columnCount) {
        pvalues = new Object[columnCount];
    }

    /**
     * Create initialized dataSetRow.
     *
     * @param values
     */
    public DataSetRow(String[] values) {
        pvalues = values;
    }

    /**
     * Returns value of given column.
     *
     * @param column first column is 0
     */
    public Object getObjectValue(int column) {

        Object tmp = pvalues[column];
        if (tmp != null) {
            return tmp;
        }
        return "<null>"; //$NON-NLS-1$
    }

    /**
     * Set the value for a given column.
     *
     * @param column first column is 0
     * @param value
     */
    public void setValue(int column, Object value) {
        pvalues[column] = value;
    }

    /**
     * @return number of columns in this row
     */
    public int length() {
        if (pvalues == null) {
            return 0;
        }
        return pvalues.length;
    }
}
