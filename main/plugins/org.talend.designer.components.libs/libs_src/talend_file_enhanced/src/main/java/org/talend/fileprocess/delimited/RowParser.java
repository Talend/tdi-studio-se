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
import java.text.NumberFormat;
import java.util.Scanner;

/**
 * DOC ibmuser class global comment. Detailled comment <br/>
 *
 */
public class RowParser extends DelimitedDataReader {

    // private char[] fieldDelimiter;

    private char[] rowSeparator;

    private StreamBuffer streamBuffer = null;

    private ColumnBuffer rowBuffer = null;

    private String rowRecord;

    // private boolean simpleMode = false;

    private int mode = 1;

    private int rowLength = -1;

    private char[] buffer;

    private Scanner scanner = null;

    /**
     * DOC ibmuser RowParser constructor comment.
     *
     * @param inputStream
     * @param rowSeparator
     * @param skipEmptyRecords
     * @throws IOException
     */
    public RowParser(BufferedReader inputStream, String rowSeparator, boolean skipEmptyRecords) throws IOException {

        super(inputStream, skipEmptyRecords);

        columnBuffer = null;

        values = null;

        if ("\n".equals(rowSeparator) || "\r\n".equals(rowSeparator)) {
            // simpleMode = true;
            mode = 0;
            if("\r\n".equals(rowSeparator)){
            	scanner = new Scanner(inputStream);
            	scanner.useDelimiter(rowSeparator);
            }
        } else if(rowSeparator!=null) {
            streamBuffer = new StreamBuffer();

            rowBuffer = new ColumnBuffer();

            this.rowSeparator = rowSeparator.toCharArray();

            try {
                streamBuffer.count = inputStream.read(streamBuffer.buffer, 0, streamBuffer.buffer.length);
            } catch (IOException ex) {
                close();
                throw ex;
            }
            streamBuffer.currentPosition = 0;
            streamBuffer.rowStart = 0;
            streamBuffer.delimiterLength = rowSeparator.length();
            streamBuffer.lastIndexToRead = streamBuffer.count - streamBuffer.delimiterLength;
            streamBuffer.streamEndMeet = (streamBuffer.count < streamBuffer.buffer.length);
        }
    }

    /**
     * DOC ibmuser RowParser constructor comment.
     *
     * @param inputStream
     * @param rowLength
     * @throws IOException
     */
    public RowParser(BufferedReader inputStream, int rowLength) throws IOException {

        super(inputStream, true);

        columnBuffer = null;

        values = null;

        mode = 2;

        this.rowLength = rowLength;

        buffer = new char[rowLength];
    }

    @Override
    protected void close(boolean closing) {
        if (!closed) {
            if (closing && mode == 1) {
                streamBuffer.buffer = null;
                rowBuffer.buffer = null;
            }

            try {
                if (initialized && scanner != null) {
                    scanner.close();
                } else if(initialized) {
                	inputStream.close();
                }
            } catch (Exception e) {
                // just eat the exception
            }

            inputStream = null;

            closed = true;
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.fileprocess.delimited.DelimitedDataReader#readRecord()
     */
    public boolean readRecord() throws IOException {
        checkClosed();
        if (mode == 0) {
            if (skipEmptyRecord) {
                do {
                	rowRecord = null;
                	if(scanner != null){
	                    if (!scanner.hasNext()) {
	                        return false;
	                    }
	                    rowRecord = scanner.next();
                	}else{
                		rowRecord = inputStream.readLine();
                		if (rowRecord == null) {
                			 return false;
	                    }
                	}
                    if (!rowRecord.equals("")) {
                        currentRecord++;
                        return true;
                    }
                } while (true);
            } else {
            	rowRecord = null;
            	if (scanner != null) {
					if (!scanner.hasNext()) {
						return false;
					} else {
						rowRecord = scanner.next();
						currentRecord++;
						return true;
					}
				}else{
					rowRecord = inputStream.readLine();
					if (rowRecord == null) {
						return false;
					} else {
						currentRecord++;
						return true;
					}
				}
            }
        } else if (mode == 1) {
            hasReadRecord = false;
            if (streamBuffer.hasMoreData()) {
                while (streamBuffer.hasMoreData() && !hasReadRecord) {
                    if (streamBuffer.noDataForDelimter()) {
                        checkDataLength();
                    } else {
                        if (isStartRecordDelimited()) {
                            if (!skipEmptyRecord) {
                                endRecord();
                            }
                            streamBuffer.skipDelimiter();

                        } else {
                            startedRow = true;
                            streamBuffer.rowStart = streamBuffer.currentPosition;

                            boolean firstLoop = true;

                            while (streamBuffer.hasMoreData() && startedRow) {
                                if (!firstLoop && streamBuffer.noDataForDelimter()) {
                                    checkDataLength();
                                } else {
                                    if (!firstLoop) {
                                        if (isStartRecordDelimited()) {
                                            endRecord();
                                            streamBuffer.skipDelimiter();
                                        }
                                    } else {
                                        firstLoop = false;
                                    }

                                    if (startedRow) {
                                        streamBuffer.currentPosition++;

                                        if (safetySwitch
                                                && streamBuffer.currentPosition - streamBuffer.rowStart
                                                        + rowBuffer.position > StaticSettings.MAX_SIZE_FOR_SAFTY) {
                                            close();

                                            throw new IOException(
                                                    "Maximum row record length of 100,000 exceeded in row in record "
                                                            + NumberFormat.getIntegerInstance().format(currentRecord)
                                                            + ". Set the safetySwitch property to false"
                                                            + " if you're expecting row record lengths greater than 100,000 characters to"
                                                            + " avoid this error.");
                                        }
                                    }
                                } // end else
                            }
                        }
                    } // end else
                }
            }
            if (!hasReadRecord) {
                if (!streamBuffer.noData()) {
                    if (!startedRow) {
                        startedRow = true;
                        streamBuffer.rowStart = streamBuffer.currentPosition;
                        streamBuffer.currentPosition = streamBuffer.count;
                        endRecord();
                        return true;
                    }
                    if (startedRow) {
                        streamBuffer.currentPosition = streamBuffer.count;
                        endRecord();
                        return true;
                    }
                } else {
                    // if (columnsCount > 0) {
                    // endRecord();
                    // return true;
                    // } else {
                    return false;
                    // }
                }
            }

            return hasReadRecord;
        } else {
            int readNumber = inputStream.read(buffer, 0, rowLength);
            if (readNumber == -1) {
                return false;
            } else {
                this.rowRecord = new String(buffer, 0, readNumber);
                currentRecord++;
                return true;
            }
        }
    }

    /**
     * @exception IOException Thrown if an error occurs while reading data from the source stream.
     */
    private void checkDataLength() throws IOException {
        updateCurrentValue();
        streamBuffer.moveTailToHead();
        int count = 0;
        try {
            count = inputStream.read(streamBuffer.buffer, streamBuffer.count, streamBuffer.buffer.length
                    - streamBuffer.count);
        } catch (IOException ex) {
            close();

            throw ex;
        }
        streamBuffer.count += count;
        streamBuffer.lastIndexToRead = streamBuffer.count - streamBuffer.delimiterLength;
        if (streamBuffer.count < streamBuffer.buffer.length) {
            streamBuffer.streamEndMeet = true;
        }
    }

    private void updateCurrentValue() {
        if (startedRow && streamBuffer.rowStart < streamBuffer.currentPosition) {
            if (rowBuffer.buffer.length - rowBuffer.position < streamBuffer.currentPosition - streamBuffer.rowStart) {
                int newLength = rowBuffer.buffer.length
                        + Math.max(streamBuffer.currentPosition - streamBuffer.rowStart, rowBuffer.buffer.length);

                char[] holder = new char[newLength];

                System.arraycopy(rowBuffer.buffer, 0, holder, 0, rowBuffer.position);

                rowBuffer.buffer = holder;
            }

            System.arraycopy(streamBuffer.buffer, streamBuffer.rowStart, rowBuffer.buffer, rowBuffer.position,
                    streamBuffer.currentPosition - streamBuffer.rowStart);

            rowBuffer.position += streamBuffer.currentPosition - streamBuffer.rowStart;
        }
    }

    private boolean isStartRecordDelimited() {
        for (int i = 0; i < rowSeparator.length; i++) {
            if (streamBuffer.buffer[streamBuffer.currentPosition + i] != rowSeparator[i]) {
                return false;
            }
        }
        return true;
    }

    @Override
    protected void endRecord() throws IOException {

        String currentValue = "";

        // must be called before setting startedColumn = false
        if (startedRow) {
            if (rowBuffer.position == 0) {
                if (streamBuffer.rowStart < streamBuffer.currentPosition) {
                    currentValue = new String(streamBuffer.buffer, streamBuffer.rowStart, streamBuffer.currentPosition
                            - streamBuffer.rowStart);
                }
            } else {
                updateCurrentValue();
                currentValue = new String(rowBuffer.buffer, 0, rowBuffer.position);
            }
        }

        rowBuffer.position = 0;

        if (startedRow) {
            startedRow = false;
        }

        rowRecord = currentValue;

        super.endRecord();
    }

    /**
     * A buffer structure that used to load data from stream for processing.
     *
     * @author gke
     */
    private class StreamBuffer {

        char[] buffer;

        int currentPosition;

        // lastIndexToRead is the last index of letter in the buffer for
        // processing, this will be equal to count - delimiterLegnth. This
        // is needed because evertime we process a letter, when we need to read
        // the proceeding letters in order to make sure if currentPosition is
        // the begin of field or record delmiter
        int lastIndexToRead;

        int delimiterLength;

        // count indicates how much usable data has been read into the stream,
        // which will not always be as long as Buffer.Length.
        int count;

        // columnStart is the position in the buffer when the
        // current column was started or the last time data
        // was moved out to the column buffer.
        int rowStart;

        // streamEndMeet is also very important, when we loag data from stream,
        // if the count is smaller than buffer.length, it indcates that there
        // must be no more data in the stream, the stream end is meet.
        boolean streamEndMeet = false;

        public StreamBuffer() {
            buffer = new char[StaticSettings.MAX_BUFFER_SIZE];
            currentPosition = 0;
            count = 0;
            rowStart = 0;
        }

        public void moveTailToHead() {
            count = count - currentPosition;
            for (int i = 0; i < count; i++) {
                buffer[i] = buffer[currentPosition + i];
            }
            lastIndexToRead = count - delimiterLength;
            currentPosition = 0;
            rowStart = 0;
        }

        public void skipDelimiter() {
            currentPosition += delimiterLength;
        }

        public boolean noDataForDelimter() {
            return currentPosition > lastIndexToRead;
        }

        public boolean hasMoreData() {
            return (currentPosition <= lastIndexToRead) || (currentPosition > lastIndexToRead && !streamEndMeet);
        }

        public boolean noData() {
            return currentPosition >= count;
        }
    }

    public String getRowRecord() {
        return rowRecord;
    }

}
