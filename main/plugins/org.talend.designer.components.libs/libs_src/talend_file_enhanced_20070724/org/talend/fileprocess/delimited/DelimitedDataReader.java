// ============================================================================
//
// Talend Community Edition
//
// Copyright (C) 2006-2019 Talend Inc. - www.talend.com
//
// This library is free software; you can redistribute it and/or
// modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either
// version 2.1 of the License.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
//
// ============================================================================
package org.talend.fileprocess.delimited;

import java.io.BufferedReader;
import java.io.IOException;

/**
 * DelimitedDataReader is a stream based API for reading any size of delimited data whith any type of field and record
 * delimiters. If you want to get an instance of this type please use the DelimitedDataReaderFactory<br/>
 *
 * @author gke
 */
public abstract class DelimitedDataReader {

    protected BufferedReader inputStream = null;

    protected ColumnBuffer columnBuffer = new ColumnBuffer();

    protected boolean startedRow = false;

    protected int columnsCount = 0;

    protected boolean hasReadRecord = false;

    protected String[] values = new String[StaticSettings.INITIAL_COLUMN_COUNT];

    protected boolean initialized = false;

    protected boolean closed = false;

    protected boolean skipEmptyRecord = true;

    protected long currentRecord = 0;

    protected boolean safetySwitch = true;

    public DelimitedDataReader(BufferedReader inputStream, boolean skipEmptyRecord) {
        if (inputStream == null) {
            throw new IllegalArgumentException("Parameter inputStream can not be null.");
        }
        this.inputStream = inputStream;
        this.skipEmptyRecord = skipEmptyRecord;
        initialized = true;
    }

    /**
     * DOC ke Comment method "checkClosed". This will be call before read new record or get content of the column
     *
     * @throws IOException Thrown if this object has already been closed.
     */
    protected void checkClosed() throws IOException {
        if (closed) {
            throw new IOException("This instance of the DelimitedFileReader class has already been closed.");
        }
    }

    /**
     * Reads another record. If this returns true, a new row data will be available using get(columnIndex)
     *
     * @return Whether another record was successfully read or not.
     * @throws IOException Thrown if an error occurs while reading data from the source stream.
     */
    public abstract boolean readRecord() throws IOException;

    /**
     * Returns the current columns value for a given column index.
     *
     * @param columnIndex The index of the column.
     * @return The current column value.
     * @throws IOException Thrown if this object has already been closed.
     */
    public String get(int columnIndex) throws IOException {
        checkClosed();

        if (columnIndex > -1 && columnIndex < columnsCount) {
            return values[columnIndex];
        } else {
            return "";
        }
    }

    /**
     * Gets the count of the records that have been read.
     *
     * @throws the count of the records that have been read.
     */
    public long getProcessedRecordCount() {
        return currentRecord;
    }

    /**
     * Closes and releases all related resources.
     */
    public void close() {
        if (!closed) {
            close(true);
            closed = true;
        }
    }

    /**
     * If closing is true, close and release all related resources.
     *
     * @param closing
     */
    protected abstract void close(boolean closing);

    /**
     * DOC ke Comment method "getAvailableRowCount".
     *
     * @param footer the record count that will not used, just as footer for the delimited data. It will also be skipped
     * @return the count of Available row count
     * @throws IOException
     */
    public long getAvailableRowCount(int footer) throws IOException {
        checkClosed();
        boolean flag = true;
        do {
            flag = readRecord();
        } while (flag);
        return currentRecord - footer;
    }

    public int getAvailableColumnsCount() throws IOException {
        return columnsCount;
    }

    /**
     * DOC ke Comment method "skipHeaders". This is used to skip headers in the delimited data
     *
     * @param header rows count that will be skipped as header
     * @throws IOException
     */
    public void skipHeaders(int header) throws IOException {
        checkClosed();
        if (header <= 0) {
            return;
        }
        for (int i = 0; i < header; i++) {
            readRecord();
        }
        currentRecord = 0;

    }

    /**
     * DOC ke Comment method "isSafetySwitch".
     *
     * @return the state of the safetySwitch
     */
    public boolean isSafetySwitch() {
        return safetySwitch;
    }

    /**
     * DOC ke Comment method "setSafetySwitch". When reading a delmited text, if safetySwitch is true, and if a column's
     * content length is larger than 100,000 or a row's column count is larger than 100,000, an Exception will be
     * thrown, and the process of reading delimited data will be terminated.
     *
     * @param safetySwitch
     */
    public void setSafetySwitch(boolean safetySwitch) {
        this.safetySwitch = safetySwitch;
    }

    /**
     * @exception IOException Thrown if an error occurs while reading data from the source stream.
     */
    protected void endRecord() throws IOException {
        // this flag is used as a loop exit condition
        // during parsing

        hasReadRecord = true;

        currentRecord++;
    }

    @Override
    protected void finalize() {
        close(false);
    }

    /**
     * StaticSettings for the DelimitedDataReader. they can be changed in unit test.
     */
    class StaticSettings {

        public static final int MAX_BUFFER_SIZE = 1024;

        public static final int INITIAL_COLUMN_COUNT = 10;

        public static final int INITIAL_COLUMN_BUFFER_SIZE = 50;

        public static final int MAX_SIZE_FOR_SAFTY = 100000;
    }

    /**
     * This is data buffer for storing the contents of a column. this is used in case that the size of a column's
     * content is bigger than MAX_BUFFER_SIZE
     *
     * @author Administrator
     *
     */
    class ColumnBuffer {

        char[] buffer;

        int position;

        public ColumnBuffer() {
            buffer = new char[StaticSettings.INITIAL_COLUMN_BUFFER_SIZE];
            position = 0;
        }
    }
}
