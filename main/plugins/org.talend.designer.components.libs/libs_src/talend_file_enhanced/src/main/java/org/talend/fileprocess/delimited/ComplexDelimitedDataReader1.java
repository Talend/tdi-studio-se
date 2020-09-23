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

/**
 * A stream based parser for parsing delimited text data from a reader. this
 * class only works when the record delimiter is a String which length is > 1,
 * and the field delimiter is a single character.<br/>
 *
 * @author gke
 *
 */
final class ComplexDelimitedDataReader1 extends DelimitedDataReader {

	private StreamBuffer streamBuffer = new StreamBuffer();

	private char fieldDelimiter;

	private char[] recordDelimiter;

	public ComplexDelimitedDataReader1(BufferedReader inputStream, char delimiter,
			String recordDelimiter, boolean skipEmptyRecords)
			throws IOException {
		super(inputStream, skipEmptyRecords);

		this.fieldDelimiter = delimiter;

		this.recordDelimiter = recordDelimiter.toCharArray();

		streamBuffer.recordDelimiterLength = this.recordDelimiter.length;
		try {
			streamBuffer.count = inputStream.read(streamBuffer.buffer, 0,
					streamBuffer.buffer.length);
		} catch (IOException ex) {
			close();

			throw ex;
		}
		streamBuffer.currentPosition = 0;
		streamBuffer.columnStart = 0;
		streamBuffer.lastIndexToRead = streamBuffer.count
				- streamBuffer.recordDelimiterLength;
		streamBuffer.streamEndMeet = (streamBuffer.count < streamBuffer.buffer.length);
	}

	@Override
	public boolean readRecord() throws IOException {
		checkClosed();

		columnsCount = 0;

		hasReadRecord = false;

		// check to see if we've already found the end of data

		if (streamBuffer.hasMoreData()) {
			// loop over the data stream until the end of data is found
			// or the end of the record is found

			// char currentLetter = '\0';

			while (streamBuffer.hasMoreData() && !hasReadRecord) {
				if (streamBuffer.noDataRecordDelimter()) {
					checkDataLength();
				} else {
					// grab the current letter as a char

					char currentLetter = streamBuffer.buffer[streamBuffer.currentPosition];

					if (currentLetter == fieldDelimiter) {
						// we encountered a column with no data, so
						// just send the end column

						endColumn();
						streamBuffer.currentPosition++;
					} else if (isStartRecordDelimited()) {
						if (columnsCount > 0 || !skipEmptyRecord) {
							endColumn();
							endRecord();
						}
						streamBuffer.skipRecordDelimiter();

					} else {
						// since the letter wasn't a special letter, this
						// will be the first letter of our current column

						startedRow = true;
						streamBuffer.columnStart = streamBuffer.currentPosition;

						boolean firstLoop = true;

						while (streamBuffer.hasMoreData() && startedRow) {
							if (!firstLoop
									&& streamBuffer.noDataRecordDelimter()) {
								checkDataLength();
							} else {
								if (!firstLoop) {
									// grab the current letter as a char
									currentLetter = streamBuffer.buffer[streamBuffer.currentPosition];
									if (currentLetter == fieldDelimiter) {
										endColumn();
										streamBuffer.currentPosition++;
									} else if (isStartRecordDelimited()) {
										endColumn();

										endRecord();

										streamBuffer.skipRecordDelimiter();
									}
								} else {
									firstLoop = false;
								}

								if (startedRow) {
									streamBuffer.currentPosition++;

									if (safetySwitch
											&& streamBuffer.currentPosition
													- streamBuffer.columnStart
													+ columnBuffer.position > StaticSettings.MAX_SIZE_FOR_SAFTY) {
										close();

										throw new IOException(
												"Maximum column length of 100,000 exceeded in column "
														+ NumberFormat
																.getIntegerInstance()
																.format(
																		columnsCount)
														+ " in record "
														+ NumberFormat
																.getIntegerInstance()
																.format(
																		currentRecord)
														+ ". Set the SafetySwitch property to false"
														+ " if you're expecting column lengths greater than 100,000 characters to"
														+ " avoid this error.");
									}
								}
							} // end else
						}
					}
				} // end else
			}
		}

		// check to see if we hit the end of the file
		// without processing the current record
		if (!hasReadRecord && !streamBuffer.noData()) {
			while (!streamBuffer.noData()) {
				if (streamBuffer.buffer[streamBuffer.currentPosition] == fieldDelimiter) {
					endColumn();
					streamBuffer.currentPosition++;
					startedRow = true;
					streamBuffer.columnStart = streamBuffer.currentPosition;
				} else {
					if (!startedRow) {
						startedRow = true;
						streamBuffer.columnStart = streamBuffer.currentPosition;
					}
					streamBuffer.currentPosition++;
				}
			}
			if (startedRow) {
				endColumn();
			}
			endRecord();
		}

		return hasReadRecord;
	}

	private boolean isStartRecordDelimited() {
		for (int i = 0; i < recordDelimiter.length; i++) {
			if (streamBuffer.buffer[streamBuffer.currentPosition + i] != recordDelimiter[i]) {
				return false;
			}
		}
		return true;
	}

	/**
	 * @exception IOException
	 *                Thrown if an error occurs while reading data from the
	 *                source stream.
	 */
	private void checkDataLength() throws IOException {
		updateCurrentValue();
		streamBuffer.moveTailToHead();
		int count = 0;
		try {
			count = inputStream.read(streamBuffer.buffer, streamBuffer.count,
					streamBuffer.buffer.length - streamBuffer.count);
		} catch (IOException ex) {
			close();

			throw ex;
		}
		streamBuffer.count += count;
		streamBuffer.lastIndexToRead = streamBuffer.count
				- streamBuffer.recordDelimiterLength;
		if (streamBuffer.count < streamBuffer.buffer.length) {
			streamBuffer.streamEndMeet = true;
		}
	}

	/**
	 * @exception IOException
	 *                Thrown if a very rare extreme exception occurs during
	 *                parsing, normally resulting from improper data format.
	 */
	private void endColumn() throws IOException {
		String currentValue = "";

		// must be called before setting startedColumn = false
		if (startedRow) {
			if (columnBuffer.position == 0) {
				if (streamBuffer.columnStart < streamBuffer.currentPosition) {
					currentValue = new String(streamBuffer.buffer,
							streamBuffer.columnStart,
							streamBuffer.currentPosition
									- streamBuffer.columnStart);
				}
			} else {
				updateCurrentValue();
				currentValue = new String(columnBuffer.buffer, 0,
						columnBuffer.position);
			}
		}

		columnBuffer.position = 0;

		startedRow = false;

		if (columnsCount >= StaticSettings.MAX_SIZE_FOR_SAFTY && safetySwitch) {
			close();

			throw new IOException(
					"Maximum column count of 100,000 exceeded in record "
							+ NumberFormat.getIntegerInstance().format(
									currentRecord)
							+ ". Set the SafetySwitch property to false"
							+ " if you're expecting more than 100,000 columns per record to"
							+ " avoid this error.");
		}

		// check to see if our current holder array for
		// column chunks is still big enough to handle another
		// column chunk

		if (columnsCount == values.length) {
			// holder array needs to grow to be able to hold another column
			int newLength = values.length * 2;

			String[] holder = new String[newLength];

			System.arraycopy(values, 0, holder, 0, values.length);

			values = holder;
		}

		values[columnsCount] = currentValue;

		currentValue = "";

		columnsCount++;
	}

	private void updateCurrentValue() {
		if (startedRow
				&& streamBuffer.columnStart < streamBuffer.currentPosition) {
			if (columnBuffer.buffer.length - columnBuffer.position < streamBuffer.currentPosition
					- streamBuffer.columnStart) {
				int newLength = columnBuffer.buffer.length
						+ Math.max(streamBuffer.currentPosition
								- streamBuffer.columnStart,
								columnBuffer.buffer.length);

				char[] holder = new char[newLength];

				System.arraycopy(columnBuffer.buffer, 0, holder, 0,
						columnBuffer.position);

				columnBuffer.buffer = holder;
			}

			System.arraycopy(streamBuffer.buffer, streamBuffer.columnStart,
					columnBuffer.buffer, columnBuffer.position,
					streamBuffer.currentPosition - streamBuffer.columnStart);

			columnBuffer.position += streamBuffer.currentPosition
					- streamBuffer.columnStart;
		}
	}

	/**
	 *
	 */
	protected void close(boolean closing) {
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
				// just eat the exception
			}

			inputStream = null;

			closed = true;
		}
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
		// processing, this will be equal to count - recordDelimiterLegnth. This
		// is needed because evertime we process a letter, when we need to read
		// the proceeding letters in order to make sure if currentPosition is
		// the begin of record delmiter
		int lastIndexToRead;

		int recordDelimiterLength;

		// count indicates how much usable data has been read into the stream,
		// which will not always be as long as buffer.Length.
		int count;

		// columnStart is the position in the buffer when the
		// current column was started or the last time data
		// was moved out to the column buffer.
		int columnStart;

		// streamEndMeet is also very important, when we loag data from stream,
		// if the count is smaller than buffer.length, it indcates that there
		// must be no more data in the stream, the stream end is meet.
		boolean streamEndMeet = false;

		public StreamBuffer() {
			buffer = new char[StaticSettings.MAX_BUFFER_SIZE];
			currentPosition = 0;
			count = 0;
			columnStart = 0;
		}

		public void moveTailToHead() {
			count = count - currentPosition;
			for (int i = 0; i < count; i++) {
				buffer[i] = buffer[currentPosition + i];
			}
			lastIndexToRead = count - recordDelimiterLength;
			currentPosition = 0;
			columnStart = 0;
		}

		public void skipRecordDelimiter() {
			currentPosition += recordDelimiterLength;
		}

		public boolean noDataRecordDelimter() {
			return currentPosition > lastIndexToRead;
		}

		public boolean hasMoreData() {
			return (currentPosition <= lastIndexToRead)
					|| (currentPosition > lastIndexToRead && !streamEndMeet);
		}

		public boolean noData() {
			return currentPosition >= count;
		}

	}

}
