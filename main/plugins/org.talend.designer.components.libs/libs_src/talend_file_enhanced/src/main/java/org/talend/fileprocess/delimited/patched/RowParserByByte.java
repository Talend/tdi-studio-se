package org.talend.fileprocess.delimited.patched;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.zip.ZipInputStream;

public class RowParserByByte {
	private byte[] buffer;

	private int row_length;

	private BufferedInputStream in = null;

	private boolean safetySwitch = false;

	public final int MAX_SIZE_FOR_SAFTY = 100000;

	private boolean closed = false;

	private long currentRecord = 0;

	private int header = 0;

	public RowParserByByte(String filename, int row_length) throws FileNotFoundException {
		this.row_length = row_length;

		File f = new File(filename);
		if (!f.exists()) {
			throw new FileNotFoundException(filename);
		}

		in = new BufferedInputStream(new FileInputStream(f));
	}

	public RowParserByByte(ZipInputStream zis, int row_length) throws FileNotFoundException {
		this.row_length = row_length;
		in = new BufferedInputStream(zis);
	}

	public boolean readRecord() throws Exception {

		return toByteArray();

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
	 * DOC ke Comment method "setSafetySwitch". When reading a delmited text, if
	 * safetySwitch is true, and if a column's content length is larger than
	 * 100,000 or a row's column count is larger than 100,000, an Exception will
	 * be thrown, and the process of reading delimited data will be terminated.
	 * 
	 * @param safetySwitch
	 */
	public void setSafetySwitch(boolean safetySwitch) {
		this.safetySwitch = safetySwitch;
	}

	/**
	 * DOC ke Comment method "checkClosed". This will be call before read new
	 * record or get content of the column
	 * 
	 * @throws IOException
	 *             Thrown if this object has already been closed.
	 */
	protected void checkClosed() throws IOException {
		if (closed) {
			throw new IOException("This instance of the RowRaeserByByte class has already been closed.");
		}
	}

	/**
	 * DOC ke Comment method "skipHeaders". This is used to skip headers in the
	 * delimited data
	 * 
	 * @param header
	 *            rows count that will be skipped as header
	 * @throws IOException
	 */
	public void skipHeaders(int header) throws Exception {
		checkClosed();
		if (header <= 0) {
			return;
		}
		this.header = header;
		for (int i = 0; i < header; i++) {
			readRecord();
		}
		currentRecord = 0;

	}

	public byte[] getBuffer() {
		return buffer;
	}

	public String getRowRecord() {
		return new String(buffer);
	}

	public boolean toByteArray() throws IOException {

		this.buffer = new byte[row_length];
		if (-1 != in.read(buffer, 0, row_length)) {

			if (safetySwitch && currentRecord - header > this.MAX_SIZE_FOR_SAFTY) {
				close();

				throw new IOException("Maximum row record length of 100,000 exceeded in row in record "
						+ NumberFormat.getIntegerInstance().format(currentRecord)
						+ ". Set the safetySwitch property to false"
						+ " if you're expecting row record lengths greater than 100,000 characters to"
						+ " avoid this error.");
			}

			currentRecord++;
			return true;
		} else {
			return false;
		}

	}

	/**
	 * DOC ke Comment method "getAvailableRowCount".
	 * 
	 * @param footer
	 *            the record count that will not used, just as footer for the
	 *            delimited data. It will also be skipped
	 * @return the count of Available row count
	 * @throws Exception
	 */
	public long getAvailableRowCount(int footer) throws Exception {
		checkClosed();
		boolean flag = true;
		do {
			flag = readRecord();
		} while (flag);
		return currentRecord - footer;
	}

	/**
	 * Closes and releases all related resources.
	 */

	public void close() {
		try {
			in.close();
		} catch (IOException e) {
			// just eat the exception
		}

		closed = true;
	}

}
