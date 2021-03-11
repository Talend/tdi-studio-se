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

import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Comparator;

import org.eclipse.swt.SWT;

/**
 * DataSetTableSorter.
 *
 * @author Davy Vanherbergen
 */
public class DataSetTableSorter implements Comparator {

    public DataSetTableSorter(DataSet dataSet) {

        pcolumnTypes = dataSet.getColumnTypes();
        ppriorities = new int[pcolumnTypes.length];
        pdirections = new int[pcolumnTypes.length];

        for (int i = 0; i < pcolumnTypes.length; i++) {
            pdirections[i] = SWT.NONE;
            ppriorities[i] = i;
        }

    }

    protected int[] ppriorities;

    protected int[] pdirections;

    protected int[] pcolumnTypes;

    /**
     * @param column
     */
    public void setTopPriority(int priority, int direction) {

        if (priority < 0 || priority >= ppriorities.length) {
            return;
        }
        int index = -1;
        for (int i = 0; i < ppriorities.length; i++) {
            if (ppriorities[i] == priority) {
                index = i;
                break;
            }
        }

        if (index == -1) {
            return;
        }

        // shift the array
        for (int i = index; i > 0; i--) {
            ppriorities[i] = ppriorities[i - 1];
        }
        ppriorities[0] = priority;
        pdirections[priority] = direction;
    }

    public int compare(Object e1, Object e2) {
        return compareColumnValue((DataSetRow) e1, (DataSetRow) e2, 0);
    }

    private int compareColumnValue(DataSetRow m1, DataSetRow m2, int depth) {
        if (depth >= ppriorities.length) {
            return 0;
        }
        int columnNumber = ppriorities[depth];
        int direction = pdirections[columnNumber];
        int result = 0;

        // check for null values
        Object o1 = m1.getObjectValue(columnNumber);
        Object o2 = m2.getObjectValue(columnNumber);

        // sort based on null values
        if (o1 == null || o2 == null) {
            if (o1 == null && o2 != null) {
                result = 1;
            } else if (o1 != null && o2 == null) {
                result = -1;
            } else {
                result = 0;
            }

            if (result == 0) {
                return compareColumnValue(m1, m2, depth + 1);
            }

            if (direction == SWT.DOWN) {
                return result * -1;
            }
            return result;
        }

        // sort on non-null values

        switch (pcolumnTypes[columnNumber]) {

        case DataSet.TYPE_STRING:
            result = ((String) o1).compareTo((String) o2);
            break;

        case DataSet.TYPE_DOUBLE:
            result = ((Double) o1).compareTo((Double) o2);
            break;

        case DataSet.TYPE_LONG:
        case DataSet.TYPE_INTEGER:
            result = ((Long) o1).compareTo((Long) o2);
            break;

        case DataSet.TYPE_DATE:
            result = ((Date) o1).compareTo((Date) o2);
            break;

        case DataSet.TYPE_DATETIME:
            result = ((Timestamp) o1).compareTo((Timestamp) o2);
            break;

        case DataSet.TYPE_TIME:
            result = ((Time) o1).compareTo((Time) o2);
            break;
        default:
        }

        if (result == 0) {
            return compareColumnValue(m1, m2, depth + 1);
        }

        if (direction == SWT.DOWN) {
            return result * -1;
        }
        return result;
    }

}
