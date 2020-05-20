package com.talend.csv;

/**
 * Copyright 2005 Bytecode Pty Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */

import java.io.Closeable;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.List;

/**
 * A very simple CSV writer released under a commercial-friendly license.
 *
 * @author Glen Smith
 *
 */
public class CSVWriter implements Closeable {

	public static final int INITIAL_STRING_SIZE = 128;

	private Writer rawWriter;

	private PrintWriter pw;

	private char separator = ',';

	private char quotechar = '"';

	private char escapechar = '"';

	private String lineEnd;

	public enum QuoteStatus {
    	FORCE,
    	AUTO,
    	NO
    }

	private QuoteStatus quotestatus = QuoteStatus.AUTO;

    /**
     * Constructs CSVWriter using a comma for the separator.
     *
     * @param writer the writer to an underlying CSV source.
     */
    public CSVWriter(Writer writer) {
    	this.rawWriter = writer;
    	this.pw = new PrintWriter(writer);
    }

    /**
     * Writes the entire list to a CSV file. The list is assumed to be a String[]
     *
     * @param allLines a List of String[], with each String[] representing a line of the file.
     */
    public void writeAll(List<String[]> allLines) {
        for (String[] line : allLines) {
            writeNext(line);
        }
    }

    public CSVWriter setLineEnd(String lineEnd) {
        this.lineEnd = lineEnd;
        return this;
    }

    public CSVWriter setSeparator(char separator) {
        this.separator = separator;
        return this;
    }

    public CSVWriter setEscapeChar(char escapechar) {
        this.escapechar = escapechar;
        if(this.escapechar == '\0') {
        	throw new RuntimeException("unvalid escape char");
        }
        return this;
    }

    public CSVWriter setQuoteChar(char quotechar) {
        this.quotechar = quotechar;
        if(this.quotechar == '\0') {
        	throw new RuntimeException("unvalid quote char");
        }
        return this;
    }

    public CSVWriter setQuoteStatus(QuoteStatus quotestatus) {
    	this.quotestatus = quotestatus;
    	return this;
    }

    /**
     * Writes the next line to the file.
     *
     * @param nextLine a string array with each comma-separated element as a separate entry.
     */
    public void writeNext(String[] nextLine) {

        if (nextLine == null) {
            return;
        }

        StringBuilder sb = new StringBuilder(INITIAL_STRING_SIZE);
        for (int i = 0; i < nextLine.length; i++) {

            if (i != 0) {
                sb.append(separator);
            }

            String nextElement = nextLine[i];
            if (nextElement == null) {
            	nextElement = "";
            }

            boolean quote = false;

            if(this.quotestatus == QuoteStatus.AUTO) {
            	quote = needQuote(nextElement,i);
            } else if(this.quotestatus == QuoteStatus.FORCE) {
            	quote = true;
            }

            if(quote) {
            	sb.append(quotechar);
            }

            StringBuilder escapeResult = escape(nextElement,quote);
            if(escapeResult!=null) {
            	sb.append(escapeResult);
            } else {
            	sb.append(nextElement);
            }

            if(quote) {
            	sb.append(quotechar);
            }
        }

        if(lineEnd!=null) {
        	sb.append(lineEnd);
        	pw.write(sb.toString());
        } else {
        	pw.println(sb.toString());
        }

    }

    /**
     * Writes the next line to the file.
     *
     * @param nextLine a string array with each comma-separated element as a separate entry.
     */
    public void writeNextEnhance(String[] nextLine,String str4Nil) {

        if (nextLine == null) {
            return;
        }

        if(str4Nil == null){
        	writeNext(nextLine);
        	return;
        }

        StringBuilder sb = new StringBuilder(INITIAL_STRING_SIZE);
        for (int i = 0; i < nextLine.length; i++) {
            boolean isNil = false;
            if (i != 0) {
                sb.append(separator);
            }

            String nextElement = nextLine[i];
            if (nextElement == null) {
            	nextElement = str4Nil;
                isNil = true;
            }

            boolean quote = false;

            if(this.quotestatus == QuoteStatus.AUTO) {
            	quote = needQuote(nextElement,i);
            } else if(this.quotestatus == QuoteStatus.FORCE) {
            	quote = true;
            }

            if(quote && !isNil){
               quote = true;
            }else{
               quote = false;
            }

            if(quote) {
                sb.append(quotechar);
            }

            StringBuilder escapeResult = escape(nextElement,quote);
            if(escapeResult!=null) {
            	sb.append(escapeResult);
            } else {
            	sb.append(nextElement);
            }

            if(quote) {
            	sb.append(quotechar);
            }
        }

        if(lineEnd!=null) {
        	sb.append(lineEnd);
        	pw.write(sb.toString());
        } else {
        	pw.println(sb.toString());
        }

    }

	private boolean needQuote(String field, int fieldIndex) {
		boolean need =  field.indexOf(quotechar) > -1
				|| field.indexOf(separator) > -1
				|| (lineEnd == null && (field.indexOf('\n') > -1 || field.indexOf('\r') > -1))
				|| (lineEnd != null && field.indexOf(lineEnd) > -1)
				|| (fieldIndex == 0 && field.length() == 0);

		if(!need && field.length() > 0) {
			char first = field.charAt(0);

			if (first == ' ' || first == '\t') {
				need = true;
			}

			if (!need && field.length() > 1) {
				char last = field.charAt(field.length() - 1);

				if (last == ' ' || last == '\t') {
					need = true;
				}
			}
		}

		return need;
	}

	private StringBuilder escape(String field, boolean quote) {
		if (quote) {
			return processLine(field);
		} else if (escapechar!=quotechar) {
			return processLine2(field);
		}

		return null;
	}

	/**
	 * escape when text quote
	 * @param nextElement
	 * @return
	 */
	protected StringBuilder processLine(String nextElement) {
        StringBuilder sb = new StringBuilder(INITIAL_STRING_SIZE);
        for (int j = 0; j < nextElement.length(); j++) {
            char nextChar = nextElement.charAt(j);
            if (nextChar == quotechar) {
                sb.append(escapechar).append(nextChar);
            } else if (nextChar == escapechar) {
                sb.append(escapechar).append(nextChar);
            } else {
                sb.append(nextChar);
            }
        }

        return sb;
    }

	/**
	 * escape when no text quote
	 * @param nextElement
	 * @return
	 */
	protected StringBuilder processLine2(String nextElement) {
        StringBuilder sb = new StringBuilder(INITIAL_STRING_SIZE);
        for (int j = 0; j < nextElement.length(); j++) {
            char nextChar = nextElement.charAt(j);
            if (nextChar == escapechar) {
                sb.append(escapechar).append(nextChar);
            } else if (nextChar == separator) {
                sb.append(escapechar).append(nextChar);
            } else if(lineEnd==null && (nextChar=='\r' || nextChar=='\n')){
            	sb.append(escapechar).append(nextChar);
            } else if(lineEnd!=null && lineEnd.indexOf(nextChar) > -1) {
            	sb.append(escapechar).append(nextChar);
            	//TODO how to escape char sequence that contain more than one char without text quote?
            } else {
                sb.append(nextChar);
            }
        }

        return sb;
    }

    /**
     * Flush underlying stream to writer.
     *
     * @throws IOException if bad things happen
     */
    public void flush() throws IOException {

        pw.flush();

    }

    /**
     * Close the underlying stream writer flushing any buffered content.
     *
     * @throws IOException if bad things happen
     *
     */
    @Override
    public void close() throws IOException {
        flush();
        pw.close();
        rawWriter.close();
    }

    /**
     * Checks to see if the there has been an error in the printstream.
     */
    public boolean checkError() {
        return pw.checkError();
    }

}
