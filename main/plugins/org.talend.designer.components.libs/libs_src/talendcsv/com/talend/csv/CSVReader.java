package com.talend.csv;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.util.HashMap;

public class CSVReader {
    
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
    
    public CSVReader(String filename,char separator,String charset) throws IOException {
        this(new FileInputStream(filename), separator, charset);
    }
    
    public CSVReader(InputStream inputStream,char separator,String charset) throws IOException {
        this(new UnicodeReader(inputStream, charset), separator);
    }

    public CSVReader(Reader reader,char separator) {
        this.reader = new BufferedReader(reader,BUFFER_SIZE);
        this.separator = separator;
    }
    
    public static CSVReader parse(String content) {
        if (content == null) {
            throw new IllegalArgumentException(
                    "Parameter content can not be null.");
        }

        return new CSVReader(new StringReader(content),',');
    }
    
    public CSVReader setLineEnd(String lineEnd) {
        this.lineEnd = lineEnd;
        return this;
    }

    public CSVReader setSeparator(char separator) {
        this.separator = separator;
        return this;
    }

    public CSVReader setEscapeChar(char escapechar) {
        this.escapechar = escapechar;
        return this;
    }
    
    public CSVReader setQuoteChar(char quotechar) {
        this.quotechar = quotechar;
        return this;
    }
    
    public char getQuoteChar() {
        return this.quotechar;
    }
    
    public CSVReader setTrimWhitespace(boolean trimWhitespace) {
        this.trimWhitespace = trimWhitespace;
        return this;
    }
    
    public CSVReader setSkipEmptyRecords(boolean skipEmptyRecords) {
        this.skipEmptyRecords = skipEmptyRecords;
        return this;
    }
    
    public CSVReader setStoreRawRecord(boolean storeRawRecord) {
        this.storeRawRecord = storeRawRecord;
        return this;
    }
    
    public String getRawRecord() {
        return rawRecord;
    }
    
    public void endRecord() {
        hasNext = true;
    }
    
    public void endColumn() {
        inColumn = false;
        
        String currentValue = sb.toString();
        
        if(trimWhitespace && !inQuote) {
            currentValue = trimTail(currentValue);
        }
        
        if (columnCount == values.length) {
            int newLength = values.length * 2;

            String[] holder = new String[newLength];

            System.arraycopy(values, 0, holder, 0, values.length);

            values = holder;
        }

        values[columnCount] = currentValue;
        
        columnCount++;
        
        sb.setLength(0);
    }
    
    public boolean readNext() throws IOException {
        columnCount = 0;
        hasNext = false;
        
        rawRecord = "";
        
        if(!hasMoreData) {
            return false;
        }
        
        while(hasMoreData && !hasNext) {
            if(arriveEnd()) {
                fill();
                continue;
            }
            
            char currentChar = buffer[currentPosition];
            
            inQuote = false;
            
            if(quotechar!='\0' && currentChar == quotechar) {//quote char as start of column
                inColumn = true;
                inQuote = true;
                currentPosition++;
                escaping = false;
                
                boolean previousCharAsQuote = false;
                boolean deleteTrailNoUseChars = false;
                
                if(storeRawRecord) {
                    stringBuilder.append(currentChar);
                }
                
                while(hasMoreData && inColumn) {
                    if(arriveEnd()) {
                        fill();
                        continue;
                    }
                    
                    currentChar = buffer[currentPosition];
                    if(deleteTrailNoUseChars){
                        if(currentChar == separator) {
                            endColumn();
                            
                            if(storeRawRecord) {
                                stringBuilder.append(currentChar);
                            }
                        } else if((lineEnd == null && (currentChar == '\n' || currentChar == '\r'))
                                || (lineEnd!=null && currentChar == lineEnd.charAt(0))) {
                            endColumn();
                            endRecord();
                        } else {
                            if(storeRawRecord) {
                                stringBuilder.append(currentChar);
                            }
                        }
                    } else if(currentChar == quotechar) {
                        if(escaping) {//quote char as text
                            sb.append(currentChar);
                            escaping = false;
                            previousCharAsQuote = false;
                        } else {//quote char as escape or end of column 
                            if(escapechar!='\0' && currentChar == escapechar) {
                                escaping = true;
                            } 
                            previousCharAsQuote = true;
                        }
                        
                        if(storeRawRecord) {
                            stringBuilder.append(currentChar);
                        }
                    } else if(escapechar!='\0' && escapechar!=quotechar && escaping) {
                        switch (currentChar) {
                        case 'n':
                            sb.append('\n');
                            break;
                        case 'r':
                            sb.append('\r');
                            break;
                        case 't':
                            sb.append('\t');
                            break;
                        case 'b':
                            sb.append('\b');
                            break;
                        case 'f':
                            sb.append('\f');
                            break;
                        case 'e':
                            sb.append('\u001B');
                            break;
                        case 'v':
                            sb.append('\u000B');
                            break;
                        case 'a':
                            sb.append('\u0007');
                            break;
                        default : 
                            sb.append(currentChar);
                            break;
                        }
                        
                        escaping = false;
                        
                        if(storeRawRecord) {
                            stringBuilder.append(currentChar);
                        }
                    } else if(escapechar!='\0' && currentChar == escapechar) {
                        escaping =  true;
                        
                        if(storeRawRecord) {
                            stringBuilder.append(currentChar);
                        }
                    } else if(previousCharAsQuote) {//quote char as end of column
                        if(currentChar == separator) {
                            endColumn();
                            
                            if(storeRawRecord) {
                                stringBuilder.append(currentChar);
                            }
                        } else if((lineEnd == null && (currentChar == '\n' || currentChar == '\r'))
                                || (lineEnd!=null && currentChar == lineEnd.charAt(0))) {
                            endColumn();
                            endRecord();
                        } else {
                            deleteTrailNoUseChars = true;
                            
                            if(storeRawRecord) {
                                stringBuilder.append(currentChar);
                            }
                        }
                        
                        previousCharAsQuote = false;
                    } else {
                        sb.append(currentChar);
                        
                        if(storeRawRecord) {
                            stringBuilder.append(currentChar);
                        }
                    }
                    
                    previousChar = currentChar;
                    
                    currentPosition++;
                }
            } else if(currentChar == separator) {
                previousChar = currentChar;
                endColumn();
                currentPosition++;
                
                if(storeRawRecord) {
                    stringBuilder.append(currentChar);
                }
            } else if (lineEnd!=null && currentChar == lineEnd.charAt(0)) {
                if (inColumn || columnCount > 0 || !skipEmptyRecords) {
                    endColumn();
                    endRecord();
                }
                
                currentPosition++;
                previousChar = currentChar;
            } else if(lineEnd==null && (currentChar == '\r' || currentChar == '\n')) {
                if (inColumn || columnCount > 0 || (!skipEmptyRecords && (currentChar == '\r' || previousChar!='\r'))) {
                    endColumn();
                    endRecord();
                }
                
                currentPosition++;
                previousChar = currentChar;
            } else if(trimWhitespace && (currentChar == ' ' || currentChar == '\t')) {
                inColumn = true;
                currentPosition++;
                
                if(storeRawRecord) {
                    stringBuilder.append(currentChar);
                }
            } else {
                inColumn = true;
                escaping = false;
                
                while(hasMoreData && inColumn) {
                    if(arriveEnd()) {
                        fill();
                        continue;
                    }
                    
                    currentChar = buffer[currentPosition];
                    
                    if(quotechar == '\0' && escapechar != '\0' && currentChar == escapechar) {
                        if(escaping) {
                            sb.append(currentChar);
                            escaping = false;
                        } else {
                            escaping = true;
                        }
                        
                        if(storeRawRecord) {
                            stringBuilder.append(currentChar);
                        }
                    } else if(escapechar!='\0' && escapechar!=quotechar && escaping) {
                        switch (currentChar) {
                        case 'n':
                            sb.append('\n');
                            break;
                        case 'r':
                            sb.append('\r');
                            break;
                        case 't':
                            sb.append('\t');
                            break;
                        case 'b':
                            sb.append('\b');
                            break;
                        case 'f':
                            sb.append('\f');
                            break;
                        case 'e':
                            sb.append('\u001B');
                            break;
                        case 'v':
                            sb.append('\u000B');
                            break;
                        case 'a':
                            sb.append('\u0007');
                            break;
                        default : 
                            sb.append(currentChar);
                            break;
                        }
                        
                        escaping = false;
                        
                        if(storeRawRecord) {
                            stringBuilder.append(currentChar);
                        }
                    } else if(currentChar == separator) {
                        endColumn();
                        
                        if(storeRawRecord) {
                            stringBuilder.append(currentChar);
                        }
                    } else if((lineEnd == null && (currentChar == '\n' || currentChar == '\r'))
                            || (lineEnd!=null && currentChar == lineEnd.charAt(0))) {
                        endColumn();
                        endRecord();
                    } else {
                        sb.append(currentChar);
                        
                        if(storeRawRecord) {
                            stringBuilder.append(currentChar);
                        }
                    }
                    
                    previousChar = currentChar;
                    currentPosition++;
                    
                }
            }
            
        }
        
        if(inColumn || previousChar == separator) {
            endColumn();
            endRecord();
        }
        
        if(storeRawRecord) {
            rawRecord = stringBuilder.toString();
            stringBuilder.setLength(0);
        }
        
        return hasNext;
        
    }
    
    public String get(int index) {
        if (index > -1 && index < columnCount) {
            return values[index];
        } else {
            return "";
        }
    }
    
    public String[] getValues() {
        String[] result = new String[columnCount];
        System.arraycopy(values, 0, result, 0, columnCount);
        return result;
    }
    
    private void fill() throws IOException {
        int count = reader.read(buffer, 0, buffer.length);
        currentPosition = 0;
        bufferCount = count;
        if(count == -1) {
            hasMoreData = false;
        }
    }
    
    private boolean arriveEnd() {
        return currentPosition == bufferCount;
    }
    
    private String trimTail(String content) {
        int len = content.length();
        int newLen = len;
        
        while (newLen > 0) {
            char tail = content.charAt(newLen - 1);
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
        reader.close();
        headersReader.clear();
    }

    //Added 20141016 TDQ-9496
    public int getCurrentRecord(){
        return this.currentPosition;
    }
    
    public char getSeperator(){
        return separator;
    }
    
    /**
     * Read the first record of data as the column headers. Added 20141016 TDQ-9496
     * 
     * @return If the header was successfully read or not.
     */
    public boolean readHeaders() throws IOException {
        boolean result = readNext();

        headersReader.length = columnCount;

        headersReader.headers = new String[columnCount];

        for (int i = 0; i < headersReader.length; i++) {
            String columnValue = get(i);
            headersReader.headers[i] = columnValue;
            headersReader.indexByHeaderName.put(columnValue, new Integer(i));
        }

        if (result) {
            currentPosition--;
        }

        columnCount = 0;
        return result;
    }
    /**
     * Returns the current column value for a given column header name.
     */
    public String get(String headerName) throws IOException {
        return get(getIndex(headerName));
    }

    private int getIndex(String headerName) throws IOException {
        if(headersReader.indexByHeaderName==null){
            return -1;
        }
        Object indexValue = headersReader.indexByHeaderName.get(headerName);

        if (indexValue != null) {
            return ((Integer) indexValue).intValue();
        } else {
            return -1;
        }
    }
    
    public String[] getHeaders() throws IOException {
        if (headersReader.headers == null) {
            return null;
        } else {
            String[] clone = new String[headersReader.length];
            System.arraycopy(headersReader.headers, 0, clone, 0,
                    headersReader.length);
            return clone;
        }
    }  
    
    private class HeadersReader {
        private String[] headers;

        private int length;

        private HashMap indexByHeaderName;

        public HeadersReader() {
            headers = null;
            length = 0;
            indexByHeaderName = new HashMap();
        }
        
        public void clear(){
            headers = null;
            indexByHeaderName = null;
        }
    }
    /**End of added by TDQ-9496 **/
}
