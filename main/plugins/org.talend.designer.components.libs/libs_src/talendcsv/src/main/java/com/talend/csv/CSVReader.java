package com.talend.csv;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CSVReader {

    private final static Logger log = LoggerFactory.getLogger(CSVReader.class);

    private Reader reader;
    
    private char separator = ',';

    private char quotechar = '"';
    
    private char escapechar = '"';

    private String lineEnd;

    private boolean skipEmptyRecords = false;
    
    private boolean trimWhitespace = true;
    
    private static final int BUFFER_SIZE = 4 * 1024;
    
    private static final int FETCH_SIZE = 10 * 50;
    
    private char[] buffer = new char[FETCH_SIZE]; 
    private int currentPosition = 0;
    private int bufferCount = 0;
    
    private boolean hasMoreData = true;
    
    private boolean hasNext = false;
    
    private boolean inColumn = false;
    
    private boolean escaping = false;
    
    private char previousChar = '\0';
    
    private String[] values = new String[10];
    
    private HeadersReader headersReader = new HeadersReader();
    
    private int columnCount = 0;
    
    private boolean inQuote = false; 
    
    private StringBuilder sb = new StringBuilder(16);
    
    private boolean storeRawRecord = false;
    private StringBuilder stringBuilder = new StringBuilder(16 * 10);
    private String rawRecord = "";
    
    public CSVReader(final String filename, final char separator, final String charset) throws IOException {
        this(new FileInputStream(filename), separator, charset);
    }
    
    public CSVReader(final InputStream inputStream, final char separator, final String charset) throws IOException {
        this(new UnicodeReader(inputStream, charset), separator);
    }

    public CSVReader(final Reader reader, final char separator) {
        this.reader = new BufferedReader(reader,BUFFER_SIZE);
        this.separator = separator;
    }
    
    public static CSVReader parse(final String content) {
        if (content == null) {
            throw new IllegalArgumentException(
                    "Parameter content can not be null.");
        }

        return new CSVReader(new StringReader(content),',');
    }
    
    public CSVReader setLineEnd(final String lineEnd) {
        if ("\n".equals(lineEnd) || "\r\n".equals(lineEnd)) {
            // classic line end (linux or windows), treat by default.
            this.lineEnd = null;
        }
        else {
            this.lineEnd = lineEnd;
            if (lineEnd != null && lineEnd.length() != 1) {
                log.warn("Line end params {} must be single character", lineEnd);
            }
        }
        return this;
    }

    public CSVReader setSeparator(final char separator) {
        this.separator = separator;
        return this;
    }

    public CSVReader setEscapeChar(final char escapechar) {
        this.escapechar = escapechar;
        return this;
    }
    
    public CSVReader setQuoteChar(final char quotechar) {
        this.quotechar = quotechar;
        return this;
    }
    
    public char getQuoteChar() {
        return this.quotechar;
    }
    
    public CSVReader setTrimWhitespace(final boolean trimWhitespace) {
        this.trimWhitespace = trimWhitespace;
        return this;
    }
    
    public CSVReader setSkipEmptyRecords(final boolean skipEmptyRecords) {
        this.skipEmptyRecords = skipEmptyRecords;
        return this;
    }
    
    public CSVReader setStoreRawRecord(final boolean storeRawRecord) {
        this.storeRawRecord = storeRawRecord;
        return this;
    }
    
    public String getRawRecord() {
        return this.rawRecord;
    }
    
    public void endRecord() {
        this.hasNext = true;
    }
    
    public void endColumn() {
        this.inColumn = false;
        
        String currentValue = this.sb.toString();
        
        if(this.trimWhitespace && !this.inQuote) {
            currentValue = this.trimTail(currentValue);
        }
        
        if (this.columnCount == this.values.length) {
            final int newLength = this.values.length * 2;

            final String[] holder = new String[newLength];

            System.arraycopy(this.values, 0, holder, 0, this.values.length);

            this.values = holder;
        }

        this.values[this.columnCount] = currentValue;

        this.columnCount++;

        this.sb.setLength(0);
    }
    
    public boolean readNext() throws IOException {
        this.columnCount = 0;
        this.hasNext = false;

        this.rawRecord = "";
        boolean warningNonRFC = false;
        
        if(!this.hasMoreData) {
            return false;
        }
        
        while(this.hasMoreData && !this.hasNext) {
            if(this.arriveEnd()) {
                this.fill();
                continue;
            }
            
            char currentChar = this.buffer[this.currentPosition];

            this.inQuote = false;
            
            if(this.quotechar !='\0' && currentChar == this.quotechar) {//quote char as start of column
                this.inColumn = true;
                this.inQuote = true;
                this.currentPosition++;
                this.escaping = false;
                
                boolean previousCharAsQuote = false;
                boolean deleteTrailNoUseChars = false;
                
                if(this.storeRawRecord) {
                    this.stringBuilder.append(currentChar);
                }
                
                while(this.hasMoreData && this.inColumn) {
                    if(this.arriveEnd()) {
                        this.fill();
                        continue;
                    }
                    
                    currentChar = this.buffer[this.currentPosition];
                    if(deleteTrailNoUseChars){
                        if(currentChar == this.separator) {
                            this.endColumn();
                            
                            if(this.storeRawRecord) {
                                this.stringBuilder.append(currentChar);
                            }
                        } else if((this.lineEnd == null && (currentChar == '\n' || currentChar == '\r'))
                                || (this.lineEnd !=null && currentChar == this.lineEnd.charAt(0))) {
                            this.endColumn();
                            this.endRecord();
                        } else {
                            if ((!warningNonRFC) && currentChar != ' ' && currentChar != '\t' && currentChar != '\r') {
                                log.warn("CSV source '{}' is not conform to RFC, some data will be ignored.", this.sb.toString());
                                warningNonRFC = true;
                            }
                            if(this.storeRawRecord) {
                                this.stringBuilder.append(currentChar);
                            }
                        }
                    } else if(currentChar == this.quotechar) {
                        if(this.escaping) {//quote char as text
                            this.sb.append(currentChar);
                            this.escaping = false;
                            previousCharAsQuote = false;
                        } else {//quote char as escape or end of column 
                            if(this.escapechar !='\0' && currentChar == this.escapechar) {
                                this.escaping = true;
                            } 
                            previousCharAsQuote = true;
                        }
                        
                        if(this.storeRawRecord) {
                            this.stringBuilder.append(currentChar);
                        }
                    } else if(this.escapechar !='\0' && this.escapechar != this.quotechar && this.escaping) {
                        switch (currentChar) {
                        case 'n':
                            this.sb.append('\n');
                            break;
                        case 'r':
                            this.sb.append('\r');
                            break;
                        case 't':
                            this.sb.append('\t');
                            break;
                        case 'b':
                            this.sb.append('\b');
                            break;
                        case 'f':
                            this.sb.append('\f');
                            break;
                        case 'e':
                            this.sb.append('\u001B');
                            break;
                        case 'v':
                            this.sb.append('\u000B');
                            break;
                        case 'a':
                            this.sb.append('\u0007');
                            break;
                        default :
                            this.sb.append(currentChar);
                            break;
                        }

                        this.escaping = false;
                        
                        if(this.storeRawRecord) {
                            this.stringBuilder.append(currentChar);
                        }
                    } else if(this.escapechar !='\0' && currentChar == this.escapechar) {
                        this.escaping =  true;
                        
                        if(this.storeRawRecord) {
                            this.stringBuilder.append(currentChar);
                        }
                    } else if(previousCharAsQuote) {//quote char as end of column
                        if(currentChar == this.separator) {
                            this.endColumn();
                            
                            if(this.storeRawRecord) {
                                this.stringBuilder.append(currentChar);
                            }
                        } else if((this.lineEnd == null && (currentChar == '\n' || currentChar == '\r'))
                                || (this.lineEnd !=null && currentChar == this.lineEnd.charAt(0))) {
                            this.endColumn();
                            this.endRecord();
                        } else {
                            deleteTrailNoUseChars = true;
                            if ((!warningNonRFC) && currentChar != ' ' && currentChar != '\t' && currentChar != '\r') {
                                log.warn("CSV source '{}' is not conform to RFC, some data will be ignored.", this.sb.toString());
                                warningNonRFC = true;
                            }
                            if(this.storeRawRecord) {
                                this.stringBuilder.append(currentChar);
                            }
                        }
                        
                        previousCharAsQuote = false;
                    } else {
                        this.sb.append(currentChar);
                        
                        if(this.storeRawRecord) {
                            this.stringBuilder.append(currentChar);
                        }
                    }

                    this.previousChar = currentChar;

                    this.currentPosition++;
                }
            } else if(currentChar == this.separator) {
                this.previousChar = currentChar;
                this.endColumn();
                this.currentPosition++;
                
                if(this.storeRawRecord) {
                    this.stringBuilder.append(currentChar);
                }
            } else if (this.lineEnd !=null && currentChar == this.lineEnd.charAt(0)) {
                if (this.inColumn || this.columnCount > 0 || !this.skipEmptyRecords) {
                    this.endColumn();
                    this.endRecord();
                }

                this.currentPosition++;
                this.previousChar = currentChar;
            } else if(this.lineEnd ==null && (currentChar == '\r' || currentChar == '\n')) {
                if (this.inColumn || this.columnCount > 0 || (!this.skipEmptyRecords && (currentChar == '\r' ||
                        this.previousChar !='\r'))) {
                    this.endColumn();
                    this.endRecord();
                }

                this.currentPosition++;
                this.previousChar = currentChar;
            } else if(this.trimWhitespace && (currentChar == ' ' || currentChar == '\t')) {
                this.inColumn = true;
                this.currentPosition++;
                
                if(this.storeRawRecord) {
                    this.stringBuilder.append(currentChar);
                }
            } else {
                this.inColumn = true;
                this.escaping = false;
                
                while(this.hasMoreData && this.inColumn) {
                    if(this.arriveEnd()) {
                        this.fill();
                        continue;
                    }
                    
                    currentChar = this.buffer[this.currentPosition];
                    
                    if(this.quotechar == '\0' && this.escapechar != '\0' && currentChar == this.escapechar) {
                        if(this.escaping) {
                            this.sb.append(currentChar);
                            this.escaping = false;
                        } else {
                            this.escaping = true;
                        }
                        
                        if(this.storeRawRecord) {
                            this.stringBuilder.append(currentChar);
                        }
                    } else if(this.escapechar !='\0' && this.escapechar != this.quotechar && this.escaping) {
                        switch (currentChar) {
                        case 'n':
                            this.sb.append('\n');
                            break;
                        case 'r':
                            this.sb.append('\r');
                            break;
                        case 't':
                            this.sb.append('\t');
                            break;
                        case 'b':
                            this.sb.append('\b');
                            break;
                        case 'f':
                            this.sb.append('\f');
                            break;
                        case 'e':
                            this.sb.append('\u001B');
                            break;
                        case 'v':
                            this.sb.append('\u000B');
                            break;
                        case 'a':
                            this.sb.append('\u0007');
                            break;
                        default :
                            this.sb.append(currentChar);
                            break;
                        }

                        this.escaping = false;
                        
                        if(this.storeRawRecord) {
                            this.stringBuilder.append(currentChar);
                        }
                    } else if(currentChar == this.separator) {
                        this.endColumn();
                        
                        if(this.storeRawRecord) {
                            this.stringBuilder.append(currentChar);
                        }
                    } else if((this.lineEnd == null && (currentChar == '\n' || currentChar == '\r'))
                            || (this.lineEnd !=null && currentChar == this.lineEnd.charAt(0))) {
                        this.endColumn();
                        this.endRecord();
                    } else {
                        this.sb.append(currentChar);
                        
                        if(this.storeRawRecord) {
                            this.stringBuilder.append(currentChar);
                        }
                    }

                    this.previousChar = currentChar;
                    this.currentPosition++;
                    
                }
            }
            
        }
        
        if(this.inColumn || this.previousChar == this.separator) {
            this.endColumn();
            this.endRecord();
        }
        
        if(this.storeRawRecord) {
            this.rawRecord = this.stringBuilder.toString();
            this.stringBuilder.setLength(0);
        }
        
        return this.hasNext;
        
    }
    
    public String get(final int index) {
        if (index > -1 && index < this.columnCount) {
            return this.values[index];
        } else {
            return "";
        }
    }
    
    public String[] getValues() {
        final String[] result = new String[this.columnCount];
        System.arraycopy(this.values, 0, result, 0, this.columnCount);
        return result;
    }
    
    private void fill() throws IOException {
        final int count = this.reader.read(this.buffer, 0, this.buffer.length);
        this.currentPosition = 0;
        this.bufferCount = count;
        if(count == -1) {
            this.hasMoreData = false;
        }
    }
    
    private boolean arriveEnd() {
        return this.currentPosition == this.bufferCount;
    }
    
    private String trimTail(String content) {
        final int len = content.length();
        int newLen = len;
        
        while (newLen > 0) {
            final char tail = content.charAt(newLen - 1);
            if(tail != ' ' && tail != '\t') {
                break;
            }
            newLen--;
        }
        
        if(newLen != len) {
            content = content.substring(0,newLen);
        }
        
        return content;
    }
    
    public void close() throws IOException {
        this.reader.close();
        this.headersReader.clear();
    }

    //Added 20141016 TDQ-9496
    public int getCurrentRecord(){
        return this.currentPosition;
    }
    
    public char getSeperator(){
        return this.separator;
    }
    
    /**
     * Read the first record of data as the column headers. Added 20141016 TDQ-9496
     * 
     * @return If the header was successfully read or not.
     */
    public boolean readHeaders() throws IOException {
        final boolean result = this.readNext();

        this.headersReader.length = this.columnCount;

        this.headersReader.headers = new String[this.columnCount];

        for (int i = 0; i < this.headersReader.length; i++) {
            final String columnValue = this.get(i);
            this.headersReader.headers[i] = columnValue;
            this.headersReader.indexByHeaderName.put(columnValue, new Integer(i));
        }

        if (result) {
            this.currentPosition--;
        }

        this.columnCount = 0;
        return result;
    }
    /**
     * Returns the current column value for a given column header name.
     */
    public String get(final String headerName) throws IOException {
        return this.get(this.getIndex(headerName));
    }

    private int getIndex(final String headerName) throws IOException {
        if(this.headersReader.indexByHeaderName==null){
            return -1;
        }
        final Object indexValue = this.headersReader.indexByHeaderName.get(headerName);

        if (indexValue != null) {
            return ((Integer) indexValue).intValue();
        } else {
            return -1;
        }
    }
    
    public String[] getHeaders() throws IOException {
        if (this.headersReader.headers == null) {
            return null;
        } else {
            final String[] clone = new String[this.headersReader.length];
            System.arraycopy(this.headersReader.headers, 0, clone, 0, this.headersReader.length);
            return clone;
        }
    }  
    
    private class HeadersReader {
        private String[] headers;

        private int length;

        private HashMap indexByHeaderName;

        public HeadersReader() {
            this.headers = null;
            this.length = 0;
            this.indexByHeaderName = new HashMap();
        }
        
        public void clear(){
            this.headers = null;
            this.indexByHeaderName = null;
        }
    }
    /**End of added by TDQ-9496 **/
}
