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

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;

import net.sourceforge.squirrel_sql.fw.sql.SQLConnection;

/**
 * Generic DataSet to hold values for TableViewer.
 *
 * @author Davy Vanherbergen
 */
public class DataSet {

    public static final int TYPE_DATE = 3;

    public static final int TYPE_DATETIME = 4;

    public static final int TYPE_DOUBLE = 1;

    public static final int TYPE_INTEGER = 2;

    public static final int TYPE_LONG = 6;

    public static final int TYPE_STRING = 0;

    public static final int TYPE_TIME = 5;

    private String[] pcolumnLabels;

    private int[] pcolumnTypes;

    private DataSetRow[] prows;

    private DataSetTableSorter psorter;

    /**
     * Hidden default constructor.
     */
    private DataSet() {

    }

    /**
     * Create a new dataSet based on an existing ResultSet.
     *
     * @param columnLabels String[] of column labels [mandatory]
     * @param resultSet ResultSet with values [mandatory]
     * @param relevantIndeces int[] of all columns to add to the dataSet, use null if all columns should be included.
     * @throws Exception if the dataset could not be created
     */
    public DataSet(String[] columnLabels, ResultSet resultSet, int[] relevantIndeces) throws Exception {

        initialize(columnLabels, resultSet, relevantIndeces);
    }

    /**
     * Create new dataset based on sql query.
     *
     * @param columnLabels string[] of columnLabels, use null if the column name can be used as label
     * @param sql query string
     * @param relevantIndeces int[] of all columns to add to the dataSet, use null if all columns should be included.
     * @param connection An open SQLConnection [mandatory]
     * @throws Exception if dataSet could not be created
     */
    public DataSet(String[] columnLabels, String sql, int[] relevantIndeces, SQLConnection connection) throws Exception {

        Statement statement = connection.createStatement();

        statement.execute(sql);
        ResultSet resultSet = statement.getResultSet();

        initialize(columnLabels, resultSet, relevantIndeces);

        statement.close();

    }

    /**
     * Create new dataset based on String[][].
     *
     * @param columnLabels string[] of columnLabels [mandatory]
     * @param data string[][] with values for dataset [mandatory]
     * @param columnTypes int[] with valid column types (e.g. DataSet.TYPE_STRING) [mandatory]
     * @throws Exception if dataSet could not be created
     */
    public DataSet(String[] columnLabels, String[][] data, int[] columnTypes) throws Exception {

        pcolumnLabels = columnLabels;
        pcolumnTypes = columnTypes;

        prows = new DataSetRow[data.length];

        for (int i = 0; i < data.length; i++) {
            prows[i] = new DataSetRow(data[i]);
        }
    }

    /**
     * Get the column index for a given column name.
     *
     * @param name
     * @return index of column whose name matches or 0 if none found
     */
    public int getColumnIndex(String name) {
        for (int i = 0; i < pcolumnLabels.length; i++) {
            if (pcolumnLabels[i].equalsIgnoreCase(name)) {
                return i;
            }
        }
        return 0;
    }

    /**
     * @return String[] with all column labels
     */
    public String[] getColumnLabels() {
        return pcolumnLabels;
    }

    /**
     * @return int[] with all column types
     */
    public int[] getColumnTypes() {
        return pcolumnTypes;
    }

    /**
     * @return all rows in this dataset
     */
    public DataSetRow[] getRows() {
        return prows;
    }

    /**
     * Initialize dataSet based on an existing ResultSet.
     *
     * @param columnLabels String[] of column labels [mandatory]
     * @param resultSet ResultSet with values [mandatory]
     * @param relevantIndeces int[] of all columns to add to the dataSet, use null if all columns should be included.
     * @throws Exception if the dataset could not be created
     */
    @SuppressWarnings("unchecked")//$NON-NLS-1$
    private void initialize(String[] columnLabels, ResultSet resultSet, int[] relevantIndeces) throws Exception {

        ResultSetMetaData metadata = resultSet.getMetaData();

        // create default column indexes
        if (relevantIndeces == null || relevantIndeces.length == 0) {
            relevantIndeces = new int[metadata.getColumnCount()];
            for (int i = 1; i <= metadata.getColumnCount(); i++) {
                relevantIndeces[i - 1] = i;
            }
        }

        // create column labels
        if (columnLabels != null && columnLabels.length != 0) {
            pcolumnLabels = columnLabels;
        } else {
            pcolumnLabels = new String[relevantIndeces.length];
            for (int i = 0; i < relevantIndeces.length; i++) {
                pcolumnLabels[i] = metadata.getColumnName(relevantIndeces[i]);
            }
        }

        // create column types
        pcolumnTypes = new int[relevantIndeces.length];
        for (int i = 0; i < relevantIndeces.length; i++) {

            switch (metadata.getColumnType(relevantIndeces[i])) {

            case Types.CHAR:
            case Types.VARCHAR:
            case Types.LONGVARCHAR:
            case -9:
                pcolumnTypes[i] = TYPE_STRING;
                break;

            case Types.INTEGER:
            case Types.SMALLINT:
            case Types.TINYINT:
                pcolumnTypes[i] = TYPE_INTEGER;
                break;

            case Types.DECIMAL:
            case Types.NUMERIC:
            case Types.DOUBLE:
            case Types.FLOAT:
            case Types.REAL:
                pcolumnTypes[i] = TYPE_DOUBLE;
                break;

            case Types.DATE:
            case Types.TIMESTAMP:
                pcolumnTypes[i] = TYPE_DATETIME;
                break;

            case Types.TIME:
                pcolumnTypes[i] = TYPE_TIME;
                break;

            case Types.BIGINT:
                pcolumnTypes[i] = TYPE_LONG;
                break;

            default:
                pcolumnTypes[i] = TYPE_STRING;
            }
        }

        // create rows
        ArrayList rows = new ArrayList(100);
        while (resultSet.next()) {

            DataSetRow row = new DataSetRow(relevantIndeces.length);

            for (int i = 0; i < relevantIndeces.length; i++) {

                switch (pcolumnTypes[i]) {

                case TYPE_STRING:
                    try {
                        row.setValue(i, resultSet.getString(relevantIndeces[i]));
                    } catch (SQLException e) {
                        if (e.getMessage().equals("No data found")) {
                            row.setValue(i, "");
                        } else {
                            throw e;
                        }
                    }
                    break;
                case TYPE_INTEGER:
                    row.setValue(i, new Long(resultSet.getInt(relevantIndeces[i])));
                    break;
                case TYPE_DOUBLE:
                    row.setValue(i, new Double(resultSet.getDouble(relevantIndeces[i])));
                    break;
                case TYPE_DATE:
                    row.setValue(i, resultSet.getDate(relevantIndeces[i]));
                    break;
                case TYPE_DATETIME:
                    try {
                        row.setValue(i, resultSet.getTimestamp(relevantIndeces[i]));
                    } catch (SQLException e) {
                        // catch for the bug 0006779,when the value is '0000-00-00:00'
                        row.setValue(i, resultSet.getString(relevantIndeces[i]));
                    }
                    break;
                case TYPE_TIME:
                    row.setValue(i, resultSet.getTime(relevantIndeces[i]));
                    break;
                case TYPE_LONG:
                    row.setValue(i, new Long(resultSet.getLong(relevantIndeces[i])));
                    break;
                default:
                    row.setValue(i, resultSet.getString(relevantIndeces[i]));
                    break;
                }

                if (resultSet.wasNull()) {
                    row.setValue(i, null);
                }

            }
            rows.add(row);
        }

        prows = (DataSetRow[]) rows.toArray(new DataSetRow[] {});

    }

    /**
     * Resort the data using the given column and sortdirection.
     *
     * @param columnIndex primary sort column index
     * @param sortDirection SWT.UP | SWT.DOWN
     */
    @SuppressWarnings("unchecked")//$NON-NLS-1$
    public void sort(int columnIndex, int sortDirection) {

        if (psorter == null) {
            psorter = new DataSetTableSorter(this);
        }
        psorter.setTopPriority(columnIndex, sortDirection);

        Arrays.sort(prows, psorter);
    }

}
