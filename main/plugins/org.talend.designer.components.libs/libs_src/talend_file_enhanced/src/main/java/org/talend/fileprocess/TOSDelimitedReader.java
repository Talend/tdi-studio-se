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
package org.talend.fileprocess;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;

/**
 *
 * @author xtan
 *
 */
public class TOSDelimitedReader {

    private boolean debug = false;

    /* --------------1------------------- */
    private BufferedReader inputStream = null;

    private StreamBuffer streamBuffer = null;

    private ColumnBuffer4Joiner columnBuffer = null;

    /* --------------2------------------- */

    private String[] values = new String[StaticSettings.INITIAL_COLUMN_COUNT];

    private int columnsCount = 0;

    private long currentRecord = 0;

    private boolean skipEmptyRecord = false;

    /* --------------3------------------- */
    private boolean hasReadRecord = false;

    private boolean autoReallocateForHuge = true;

    private boolean initialized = false;

    private boolean closed = false;

    /* --------------4------------------- */

    public boolean splitRecord = false;

    /* --------------5------------------- */
    private int header = 0;

    /*--------------6--------------------- */

    public TOSDelimitedReader(java.io.InputStream is, String encoding, String fieldDelimiter, String recordDelimiter,
            boolean needSkipEmptyRecord) throws IOException {

    	UnicodeReader inputStreamReader = new UnicodeReader(is, encoding);

        init(inputStreamReader, fieldDelimiter, recordDelimiter, needSkipEmptyRecord);
    }

    public TOSDelimitedReader(String fileName, String encoding, String fieldDelimiter, String recordDelimiter,
            boolean needSkipEmptyRecord) throws IOException {
        if (fileName == null || fieldDelimiter == null || recordDelimiter == null) {
            throw new IllegalArgumentException("Parameter can't be null.");
        }

        UnicodeReader inputStreamReader = new UnicodeReader(new FileInputStream(fileName), encoding);

        init(inputStreamReader, fieldDelimiter, recordDelimiter, needSkipEmptyRecord);

    }

    public TOSDelimitedReader(Reader reader, String fieldDelimiter, String recordDelimiter, boolean needSkipEmptyRecord)
            throws IOException {
        if (reader == null || fieldDelimiter == null || recordDelimiter == null) {
            throw new IllegalArgumentException("Parameter can't be null.");
        }

        init(reader, fieldDelimiter, recordDelimiter, needSkipEmptyRecord);
    }

    public void init(Reader reader, String fieldDelimiter, String recordDelimiter, boolean needSkipEmptyRecord)
            throws IOException {
        if (reader == null || fieldDelimiter == null || recordDelimiter == null) {
            throw new IllegalArgumentException("Parameter can't be null.");
        }

        inputStream = new BufferedReader(reader);

        columnBuffer = new ColumnBuffer4Joiner();
        streamBuffer = new StreamBuffer(fieldDelimiter, recordDelimiter);

        skipEmptyRecord = needSkipEmptyRecord;

        initialized = true;

    }

    public boolean readRecord() throws IOException {
        if (splitRecord) {
            return readRecord_SplitRecord();

        } else {
            return readRecord_SplitField();
        }
    }

    private boolean readRecord_SplitField() throws IOException {

        checkClosed();

        boolean in = false;

        hasReadRecord = false;

        columnsCount = 0;
        streamBuffer.columnStart = streamBuffer.currentPosition;

        while (streamBuffer.hasMoreData() && !hasReadRecord) {

            if (debug) {
                System.out.println(streamBuffer);
            }
            in = true;

            if (streamBuffer.needJoinReadNextBuffer()) {
                joinAndRead();
            }

            if (streamBuffer.isStartFieldDelimited()) {
                endColumn();
                streamBuffer.skipFieldDelimiter();
            } else if (streamBuffer.isStartRecordDelimited()) {
                endColumn();
                endRecord();
                if (!streamBuffer.hasMoreData()) {
                    streamBuffer.fileEndWithRecordDelimiter = true;
                }

            } else {

                streamBuffer.currentPosition++;

                // for checking one column can support the max number of chars
                if (!autoReallocateForHuge
                        && streamBuffer.currentPosition - streamBuffer.columnStart + columnBuffer.position > StaticSettings.MAX_CHARS_IN_ONE_COLUMN) {
                    close();
                    throw new IOException("The maximum chars of one column is " + StaticSettings.MAX_CHARS_IN_ONE_COLUMN
                            + ". There is over this limit.");
                }
            }

        }

        if (!hasReadRecord) {
            if (!StaticSettings.ignoreFileEndWithOneRecordDelimiter && streamBuffer.fileEndWithRecordDelimiter) {// aaa;
                // bbb
                // #111
                // ;
                // 222#
                streamBuffer.fileEndWithRecordDelimiter = false;
                endColumn();
                endRecord();
            } else if (in) {// aaa;bbb#111;222
                endColumn();
                endRecord();
            }
        }

        return hasReadRecord;

    }

    private boolean readRecord_SplitRecord() throws IOException {

        checkClosed();

        boolean in = false;

        hasReadRecord = false;

        columnsCount = 0;
        streamBuffer.columnStart = streamBuffer.currentPosition;

        while (streamBuffer.hasMoreData() && !hasReadRecord) {

            if (debug) {
                System.out.println(streamBuffer);
            }
            in = true;

            if (streamBuffer.needJoinReadNextBuffer()) {
                joinAndRead();
            }

            if (streamBuffer.isStartRecordDelimited()) {
                endColumn();
                endRecord();
                if (!streamBuffer.hasMoreData()) {
                    streamBuffer.fileEndWithRecordDelimiter = true;
                }

            } else if (streamBuffer.isStartFieldDelimited()) {
                endColumn();
                streamBuffer.skipFieldDelimiter();
            } else {

                streamBuffer.currentPosition++;

                // for checking one column can support the max number of chars
                if (!autoReallocateForHuge
                        && streamBuffer.currentPosition - streamBuffer.columnStart + columnBuffer.position > StaticSettings.MAX_CHARS_IN_ONE_COLUMN) {
                    close();
                    throw new IOException("The maximum chars of one column is " + StaticSettings.MAX_CHARS_IN_ONE_COLUMN
                            + ". There is over this limit.");
                }
            }

        }

        if (!hasReadRecord) {
            if (!StaticSettings.ignoreFileEndWithOneRecordDelimiter && streamBuffer.fileEndWithRecordDelimiter) {// aaa;
                // bbb
                // #111
                // ;
                // 222#
                streamBuffer.fileEndWithRecordDelimiter = false;
                endColumn();
                endRecord();
            } else if (in) {// aaa;bbb#111;222
                endColumn();
                endRecord();
            }
        }

        return hasReadRecord;
    }

    private void joinAndRead() throws IOException {
        columnBuffer.saveCharInJoiner();
        streamBuffer.joinReadNextBuffer();
    }

    private void endRecord() throws IOException {
        streamBuffer.skipRecordDelimiter();
        if(this.header>0){
            this.header--;
        }else if (skipEmptyRecord) {
            if (columnsCount == 0 || (columnsCount == 1 && values[0].equals(""))) {
                columnsCount = 0;// reset the columnsCount = 0 is a must
                streamBuffer.columnStart = streamBuffer.currentPosition;
                return;
            }
        }

        // this flag is used as a loop exit condition during parsing
        hasReadRecord = true;

        currentRecord++;
    }

    /**
     * @exception IOException Thrown if a very rare extreme exception occurs during parsing, normally resulting from
     * improper data format.
     */
    private void endColumn() throws IOException {
        String currentValue = "";

        if (columnBuffer.position == 0) {
            currentValue = new String(streamBuffer.buffer, streamBuffer.columnStart, streamBuffer.currentPosition
                    - streamBuffer.columnStart);
        } else {
            // add the areadly datas in buffer
            columnBuffer.saveCharInJoiner();
            currentValue = new String(columnBuffer.buffer, 0, columnBuffer.position);
        }

        columnBuffer.position = 0;

        // for checking one record can support the max number of columns
        if (!autoReallocateForHuge && columnsCount >= StaticSettings.MAX_COLUMNS_IN_ONE_RECORD) {
            close();
            throw new IOException("The maximum column number of one record is " + StaticSettings.MAX_COLUMNS_IN_ONE_RECORD
                    + ". There is over this limit.");
        }

        if (columnsCount == values.length) {

            int newLength = values.length * 2;

            String[] holder = new String[newLength];

            System.arraycopy(values, 0, holder, 0, values.length);

            values = holder;
        }

        values[columnsCount] = currentValue;

        columnsCount++;
    }

    private void close(boolean closing) {
        if (!closed) {
            if (closing) {
                streamBuffer.buffer = null;
                columnBuffer.buffer = null;
            }

            try {
                if (initialized) {
                    inputStream.close();
                }
            } catch (Exception e) {
                // just ignore the exception
            }

            inputStream = null;

            closed = true;
        }
    }

    /**
     * when read a new record or get content of the column, there should check the stream first.
     */
    private void checkClosed() throws IOException {
        if (closed) {
            throw new IOException("This instance of the DelimitedFileReader class has already been closed.");
        }
    }

    /**
     * get one column value with a given column index for the current record.
     */
    public String get(int columnIndex) throws IOException {
        checkClosed();

        if (columnIndex > -1 && columnIndex < columnsCount) {
            return values[columnIndex];
        } else {
            return "";
        }
    }

    public String getRowRecord() {
        // here can fast for the common record seperator function with fieldDelimiter=""
        // becase there is only one field.
        if (columnsCount == 1) {
            return values[0];
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < columnsCount; i++) {
            if (i > 0) {
                sb.append(streamBuffer.fieldDelimiter);// add the fieldDelimiter
            }
            sb.append(values[i]);
        }
        return sb.toString();
    }

    /**
     * get the result how many records have been read.
     */
    public long getProcessedRecordCount() {
        return currentRecord;
    }

    /**
     * <p>
     * when skip the some records of the beginning and some records of the end in the file, then compute how many
     * records are there.
     * </p>
     * <p>
     * it can use after the skipHeaders();
     * </p>
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
     * skip the some records of the beginning in the file, and set the "currentRecord = 0"
     *
     *
     */
    public void skipHeaders(int header) throws IOException {
        this.header = header;
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
     * <p>
     * the default limit is:
     * </p>
     * <li>public static final int MAX_CHARS_IN_ONE_COLUMN = 100000;</li> <li>public static final int
     * MAX_COLUMNS_IN_ONE_RECORD = 100000;</li>
     *
     */
    public void setAutoReallocateForHuge(boolean autoReallocateForHuge) {
        this.autoReallocateForHuge = autoReallocateForHuge;
    }

    /**
     * <p>
     * if the input datas like this: 111;222;#aaa;bbb;# (row separator: ;# field separator: ; )
     * </p>
     * <li>if FirstSplit_RecordSeparator, there will get (2 records, 2 columns)</li> <li>if FirstSplit_FieldSeparator,
     * there will get (2 records, 3 columns)</li>
     * <p>
     * The default value is false, it means split the field first.
     * </p>
     */
    public void setSplitRecord(boolean splitRecord) {
        this.splitRecord = splitRecord;
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

    @Override
    protected void finalize() {
        close(false);
    }

    /**
     * a buffer: save the end chars of the last buffer and begin chars of the current buffer in the memory
     *
     * @author xtan
     *
     */
    private class ColumnBuffer4Joiner {

        char[] buffer;

        int position;

        public ColumnBuffer4Joiner() {
            buffer = new char[StaticSettings.INITIAL_COLUMN_BUFFER_SIZE];
            position = 0;
        }

        /**
         * join TWO buffer together, save the end chars of the last buffer and begin chars of the current buffer in one
         * place
         */
        public void saveCharInJoiner() {

            int columnBufferBlankSpaceNum = columnBuffer.buffer.length - columnBuffer.position;
            int streamBufferFieldCharNum = 0;
            if (streamBuffer.count > 0) {// a must
                streamBufferFieldCharNum = streamBuffer.currentPosition - streamBuffer.columnStart;
            }
            // check and expand more memory for buffer
            if (columnBufferBlankSpaceNum < streamBufferFieldCharNum) {
                int newLength = columnBuffer.buffer.length + Math.max(streamBufferFieldCharNum, columnBuffer.buffer.length);

                char[] holder = new char[newLength];

                System.arraycopy(columnBuffer.buffer, 0, holder, 0, columnBuffer.position);

                columnBuffer.buffer = holder;
            }

            // copy datas from streamBuffer to columnBuffer for save it temporarily
            System.arraycopy(streamBuffer.buffer, streamBuffer.columnStart, columnBuffer.buffer, columnBuffer.position,
                    streamBufferFieldCharNum);

            columnBuffer.position += streamBufferFieldCharNum;
        }
    }

    /**
     * <b> a buffer with funtion: join the last data and read the next buffer, for supporting the multi-separator</b>
     * Notice: "count, currentPosition, currentPosition, columnStart", they are import here
     *
     * @author xtan
     */
    private class StreamBuffer {

        public boolean needJoinReadNextBuffer() {
            // 5-2=3, when currentPostion=3; or 5-0=5, when currentPosition=5, and file not end, so read next buffer
            return currentPosition >= lastIndexToRead && !streamEndMeet;
            /* notice: here is >=, not > */
        }

        // when count=5, currentPosition=4 ===> hasMoreData()=true, it mean buffer[4] still need process
        public boolean hasMoreData() {
            return !streamEndMeet || currentPosition < count;
        }

        private void moveTailToHead() {
            count = count - currentPosition;
            for (int i = 0; i < count; i++) {
                buffer[i] = buffer[currentPosition + i];
            }

            lastIndexToRead = count - maxLimit;
            currentPosition = 0;
            columnStart = 0;
        }

        public void joinReadNextBuffer() throws IOException {
            moveTailToHead();
            int readCount = 0;
            int maxReadLength = buffer.length - count;
            try {
                readCount = inputStream.read(buffer, count, maxReadLength);
            } catch (IOException ex) {
                close();
                throw ex;
            }

            if (debug) {
                System.out.println("##maxReadLength=" + maxReadLength + "   newReadCount=" + readCount);
                System.out.println(streamBuffer);
            }

            setStreamEnd(readCount, maxReadLength);

            if (readCount > -1) {
                count += readCount;
                lastIndexToRead = count - maxLimit;
            }
        }

        private void setStreamEnd(int readCount, int maxReadLength) throws IOException {
            /* @see bug:http://talendforge.org/bugs/view.php?id=4554 */
            //https://jira.talendforge.org/browse/TDI-44745
            if (readCount < maxReadLength) {
                if (readCount == -1) {
                    streamEndMeet = true;
                } else {
                    if(inputStream.markSupported()) {
                        inputStream.mark(1);
                        if(inputStream.read() == -1) {
                            streamEndMeet = true;
                        }
                        inputStream.reset();
                    }
                }
            }
        }

        /* --------------1------------------- */
        private char[] buffer;

        private char[] fieldDelimiter;

        private char[] recordDelimiter;

        // aaa;bbb#111;222#
        private boolean fileEndWithRecordDelimiter = false;

        /* --------------2------------------- */

        private boolean lineFeedAll = false;

        // if LineMode.LINEFEED_ALL, if "\r\n", variableLineFeed = 2, if "\r" or "\n", variableLineFeed = 1
        // only used to adjust the skipRecordDelimiter()
        private int variableLineFeed = 0;

        /* --------------3------------------- */

        private int maxLimit;// maxLimit = Math.max(fieldDelimiter.length, recordDelimiter.length);

        private int currentPosition;

        // for join read
        private int lastIndexToRead;// lastIndexToRead = count - maxLimit, special: 5 = 5 - 0

        private int count;

        private int columnStart;

        /* --------------4------------------- */
        // end of the file
        private boolean streamEndMeet = false;

        public StreamBuffer(String fieldDelimiterPara, String recordDelimiterPara) throws IOException {
            buffer = new char[StaticSettings.MAX_BUFFER_SIZE];

            if (recordDelimiterPara.equals("\n")) {
                if (StaticSettings.lineMode == LineMode.LINEFEED_ALL) {
                    // notice: here we set it "\r\n", neither "\n" nor "\r", becase there want max length
                    recordDelimiter = "\r\n".toCharArray();
                    lineFeedAll = true;
                } else if (StaticSettings.lineMode == LineMode.LINEFEED_JRE) {
                    String lineSeparator = (String) java.security.AccessController
                            .doPrivileged(new sun.security.action.GetPropertyAction("line.separator"));
                    recordDelimiter = lineSeparator.toCharArray();
                } else {
                    recordDelimiter = recordDelimiterPara.toCharArray();
                }

            } else {
                recordDelimiter = recordDelimiterPara.toCharArray();
            }

            fieldDelimiter = fieldDelimiterPara.toCharArray();

            maxLimit = Math.max(fieldDelimiter.length, recordDelimiter.length);

            // read one buffer datas first when initial
            try {
                count = inputStream.read(buffer, 0, buffer.length);
            } catch (IOException ex) {
                close();
                throw ex;
            }

            currentPosition = 0;
            columnStart = 0;
            lastIndexToRead = count - maxLimit;
            setStreamEnd(count, buffer.length);
        }

        public boolean isStartFieldDelimited() {
            int maxLengthCanCheck = count - currentPosition;

            if (fieldDelimiter.length != 0 && fieldDelimiter.length <= maxLengthCanCheck) {// test here is a must
                for (int i = 0; i < fieldDelimiter.length; i++) {
                    if (buffer[currentPosition + i] != fieldDelimiter[i]) {
                        return false;
                    }
                }
            } else {
                return false;
            }

            return true;
        }

        public boolean isStartRecordDelimited() {

            int maxLengthCanCheck = count - currentPosition;
            if (lineFeedAll) {// maxLengthCanCheck > 0 is must
                if (maxLengthCanCheck > 0) {
                    if (buffer[currentPosition] == '\n') {
                        variableLineFeed = 1;
                        return true;
                    } else if (buffer[currentPosition] == '\r') {
                        variableLineFeed = 1;
                        if (buffer[currentPosition + 1] == '\n' && maxLengthCanCheck > 1) {
                            variableLineFeed = 2;
                        }
                        return true;
                    }
                }

                return false;

            } else {

                if (recordDelimiter.length != 0 && recordDelimiter.length <= maxLengthCanCheck) {// test here is a
                    // must
                    for (int i = 0; i < recordDelimiter.length; i++) {
                        if (buffer[currentPosition + i] != recordDelimiter[i]) {
                            return false;
                        }
                    }
                } else {
                    return false;
                }
            }

            return true;
        }

        public void skipFieldDelimiter() {
            currentPosition += fieldDelimiter.length;
            columnStart = currentPosition;// means: start a new Column
        }

        public void skipRecordDelimiter() {
            if (lineFeedAll) {
                currentPosition += variableLineFeed;
            } else {
                currentPosition += recordDelimiter.length;
            }
        }

        @Override
        public String toString() {
            StringBuffer sb = new StringBuffer();
            sb.append("count=").append(count).append("\n");
            sb.append("maxLimit=").append(maxLimit).append("\n");
            sb.append("lastIndexToRead=").append(lastIndexToRead).append("\n");
            sb.append("currentPosition=").append(currentPosition).append("\n");
            sb.append("columnStart=").append(columnStart).append("\n");
            sb.append("streamEndMeet=").append(streamEndMeet).append("\n");

            sb.append("overMaxLimit()=").append(needJoinReadNextBuffer()).append("\n");
            sb.append("hasMoreData()=").append(hasMoreData()).append("\n");

            sb.append("char[]=").append(buffer).append("\n");
            sb.append("char[").append(currentPosition).append("]=").append(buffer[currentPosition]).append("\n");

            sb.append("fieldDelimiterLength=").append(fieldDelimiter.length).append("\n");
            sb.append("recordDelimiterLength=").append(recordDelimiter.length).append("\n");

            return sb.toString();
        }
    }

    public enum LineMode {
        // it means, if input recordDelimiter is "\n",
        // if JRE on windows, it will be "\r\n",
        // if JRE on linux, it will be "\n",
        // if JRE on Mac, it will be "\r"

        LINEFEED_JRE,

        // it means, if input recordDelimiter is "\n", it will treat all the "\r", "\n", "\r\n" as recordDelimiter
        LINEFEED_ALL,

        // it means, if input recordDelimiter is "\n", just treat it as "\n"
        LINEFEED_NORMAL;
    }

    private enum SplitWay {

        FIRSTSPLIT_RECORDSEPARATOR,

        FIRSTSPLIT_FIELDSEPARATOR;
    }

    /**
     * StaticSettings for the DelimitedDataReader. they can be changed in unit test.
     */
    private static class StaticSettings {

        public static final int MAX_BUFFER_SIZE = 1024;

        public static final int INITIAL_COLUMN_COUNT = 10;

        public static final int INITIAL_COLUMN_BUFFER_SIZE = 50;

        public static final int MAX_CHARS_IN_ONE_COLUMN = 100000;

        public static final int MAX_COLUMNS_IN_ONE_RECORD = 100000;

        // how do we process the "\n" as recordDelimiter
        public static final LineMode lineMode = LineMode.LINEFEED_ALL;

        // 1. how do we process the case, file end with one recordDelimiter, like the following case, 2 records or 3
        // records?aaa;bbb#111;222#
        // 2. notice: there only process the last one recordDelimiter, if this case: aaa;bbb#111;222####, we still focus
        // on the last recordDelimiter
        public static final boolean ignoreFileEndWithOneRecordDelimiter = true;

        // public static final boolean autoReallocateForHuge = true;
    }

    public static void main(String[] args) throws IOException {
        TOSDelimitedReader fid = new TOSDelimitedReader("D:\\talend\\talendFID\\in.csv", "ISO-8859-15", "", "\n", false);
        int rowNum = 0;
        while (fid.readRecord()) {
            System.out.println("*********Row" + rowNum + "***********");
            System.out.println("------Row------\n" + fid.getRowRecord());
            int fieldNum = fid.getAvailableColumnsCount();
            for (int k = 0; k < fieldNum; k++) {
                System.out.println("------" + k + "------\n" + fid.get(k));
            }
            rowNum++;
            System.out.println("\n\n");
        }
    }

}
