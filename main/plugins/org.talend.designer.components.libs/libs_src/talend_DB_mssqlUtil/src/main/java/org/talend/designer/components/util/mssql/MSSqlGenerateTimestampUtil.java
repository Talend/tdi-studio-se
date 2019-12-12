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
package org.talend.designer.components.util.mssql;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;
import java.text.SimpleDateFormat;
import java.text.ParseException;

/**
 * DOC Administrator class global comment. Detailled comment
 */
public class MSSqlGenerateTimestampUtil {

    private Map<Integer, Integer> columnDBTypeMapping = new HashMap<Integer, Integer>();

    public static final int STRING_TO_JAVA_SQL_DATE = 0x001;

    public static final int STRING_TO_JAVA_SQL_TIME = 0x002;

    public static final int STRING_TO_JAVA_SQL_TIMESTAMP = 0x003;

    public static final int JAVA_SQL_DATE = 0x101;

    private static final String DATE_STRING_FOR_TIME = "1970-01-01 ";

    private static final int DB_TYPE_DATETIMEOFFSET = 0x004;

    /**
     * DOC jliu Comment method "getDate". <br>
     * DB Type ---------> jtds driver return type<br>
     * DATE -------------> nvarchar<br>
     * DATETIME ---------> datetime<br>
     * DATETIME2 --------> nvarchar<br>
     * DTIME ------------> nvarchar<br>
     * SMALLDATETIME ----> smalldatetime<br>
     * DATETIMEOFFSET ---> nvarchar<br>
     * TIMESTAMP --------> timestamp<br>
     *
     * @param rsmd
     * @param rs
     * @param index
     * @return the java.util.Date convert from db
     * @throws SQLException
     */
    public java.util.Date getDate(final java.sql.ResultSetMetaData rsmd, final java.sql.ResultSet rs, int index)
            throws SQLException {
        if (rs.getObject(index) == null) {
            return null;
        }
        if (columnDBTypeMapping.get(index) != null) {
            switch (columnDBTypeMapping.get(index)) {
            case STRING_TO_JAVA_SQL_DATE:
                return new java.util.Date(java.sql.Date.valueOf(rs.getString(index)).getTime());
            case STRING_TO_JAVA_SQL_TIMESTAMP:
                return new java.util.Date(java.sql.Timestamp.valueOf(rs.getString(index)).getTime());
            case STRING_TO_JAVA_SQL_TIME:
                return new java.util.Date(java.sql.Timestamp.valueOf(DATE_STRING_FOR_TIME + rs.getString(index)).getTime());
            case JAVA_SQL_DATE:
                return new java.util.Date(rs.getTimestamp(index).getTime());
            case DB_TYPE_DATETIMEOFFSET:
                return parseDatetimeOffset(rs.getString(index));
            default:
                return null;
            }
        } else {
            // this code just run one times.
            if ("nvarchar".equals(rsmd.getColumnTypeName(index))) {
                String dateString = rs.getString(index);
                try {// if DB DATE
                    columnDBTypeMapping.put(index, STRING_TO_JAVA_SQL_DATE);
                    return new java.util.Date(java.sql.Date.valueOf(dateString).getTime());
                } catch (java.lang.IllegalArgumentException iae1) {
                    try {// if DB DATETIME2
                        columnDBTypeMapping.put(index, STRING_TO_JAVA_SQL_TIMESTAMP);
                        return new java.util.Date(java.sql.Timestamp.valueOf(dateString).getTime());
                    } catch (java.lang.IllegalArgumentException iae2) {
                        try {// if DB TIME
                            columnDBTypeMapping.put(index, STRING_TO_JAVA_SQL_TIME);
                            return new java.util.Date(java.sql.Timestamp.valueOf(DATE_STRING_FOR_TIME + dateString).getTime());
                        } catch (java.lang.IllegalArgumentException iae3) {
                            // if DB DATETIMEOFFSET
                            columnDBTypeMapping.put(index, DB_TYPE_DATETIMEOFFSET);
                            return parseDatetimeOffset(dateString);
                        }
                    }
                }

            } else if ("datetime".equals(rsmd.getColumnTypeName(index))
                    || "smalldatetime".equals(rsmd.getColumnTypeName(index))) {
                columnDBTypeMapping.put(index, JAVA_SQL_DATE);
                return new java.util.Date(rs.getTimestamp(index).getTime());
            } else if ("time".equals(rsmd.getColumnTypeName(index))) {
                columnDBTypeMapping.put(index, STRING_TO_JAVA_SQL_TIME);
                return new java.util.Date(rs.getTime(index).getTime());
            } else if ("date".equals(rsmd.getColumnTypeName(index))) {
                columnDBTypeMapping.put(index, STRING_TO_JAVA_SQL_DATE);
                return new java.util.Date(rs.getDate(index).getTime());
            } else if ("datetimeoffset".equals(rsmd.getColumnTypeName(index))) {
                columnDBTypeMapping.put(index, DB_TYPE_DATETIMEOFFSET);
                return parseDatetimeOffset(rs.getString(index));
            } else if ("datetime2".equals(rsmd.getColumnTypeName(index))) {
                columnDBTypeMapping.put(index, STRING_TO_JAVA_SQL_TIMESTAMP);
                return new java.util.Date(rs.getTimestamp(index).getTime());
            }
        }
        return null;
    }

    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    
    {
        sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
    }
    
    /**
     * parse datetimeoffset string to date.
     * datetimeoffset string show as YYYY-MM-DD hh:mm:ss[.nnnnnnn] [+|-]hh:mm
     *
     * @param datetimeOffsetString
     * @return
     */
    private java.util.Date parseDatetimeOffset(String datetimeOffsetString) {
        int idx = datetimeOffsetString.lastIndexOf(' ');
        String datetimeString = datetimeOffsetString.substring(0, idx);
        String offsetString = datetimeOffsetString.substring(idx + 1);
        int offset = TimeZone.getTimeZone("GMT" + offsetString).getRawOffset();

        long millisecondsWithOffset = 0l;
        try {
            millisecondsWithOffset = sdf.parse(datetimeString).getTime();
        } catch(ParseException e) {
            throw new RuntimeException(e.getMessage());
        }
        
        //get GMT's millisecond number since GMT 1970_01_01
        long milliseconds4GMT_to_GMT_1970_01_01 = millisecondsWithOffset - offset;
        
        return new java.util.Date(milliseconds4GMT_to_GMT_1970_01_01);
    }

}
