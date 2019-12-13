package com.talend.csv;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.Consumer;

public class CSVReader implements AutoCloseable {
    
    private final Source source;

    private final CSVConfig config = new CSVConfig();

    private boolean hasNext = false;

    private String[] values = new String[10];
    
    private HeadersReader headersReader = new HeadersReader();
    
    private int columnCount = 0;

    private boolean storeRawRecord = false;

    private String rawRecord = "";
    
    public CSVReader(String filename,char separator,String charset) throws IOException {
        this(new FileInputStream(filename), separator, charset);
    }
    
    public CSVReader(InputStream inputStream,char separator,String charset) throws IOException {
        this(new UnicodeReader(inputStream, charset), separator);
    }

    public CSVReader(Reader reader,char separator) {
        this.source = new Source(reader);
        this.config.setSeparator(separator);
    }
    
    public static CSVReader parse(String content) {
        if (content == null) {
            throw new IllegalArgumentException(
                    "Parameter content can not be null.");
        }

        return new CSVReader(new StringReader(content),',');
    }
    
    public CSVReader setLineEnd(String lineEnd) {
        this.config.setLineEnd(lineEnd);
        return this;
    }

    public CSVReader setSeparator(char separator) {
        this.config.setSeparator(separator);
        return this;
    }

    public CSVReader setEscapeChar(char escapechar) {
        this.config.setEscapechar(escapechar);
        return this;
    }
    
    public CSVReader setQuoteChar(char quotechar) {
        this.config.setQuotechar(quotechar);
        return this;
    }
    
    public char getQuoteChar() {
        return this.config.getQuotechar();
    }
    
    public CSVReader setTrimWhitespace(boolean trimWhitespace) {
        this.config.setTrimWhitespace(trimWhitespace);
        return this;
    }
    
    public CSVReader setSkipEmptyRecords(boolean skipEmptyRecords) {
        this.config.setSkipEmptyRecords(skipEmptyRecords);
        return this;
    }
    
    public CSVReader setStoreRawRecord(boolean storeRawRecord) {
        this.storeRawRecord = storeRawRecord;
        return this;
    }
    
    public String getRawRecord() {
        return rawRecord;
    }


    private State state = new StartState(null);

    private void toRecord(List<String> fields) {
        this.values = fields.toArray(new String[fields.size()]);
        this.hasNext = true;
    }

    private CSVReader.ResultAction result = new CSVReader.ResultAction(this::toRecord);

    public boolean readNext() throws IOException {

        hasNext = false;
        if (!this.source.hasMoreData()) {
            return false;
        }

        while(this.source.hasMoreData() && !hasNext) {
            char currentChar = this.source.currentChar();
            this.state = this.state.accept(currentChar, this.config, result);
            this.source.next();
        }
        if (!this.source.hasMoreData()) {
            this.state = this.state.accept('\0', this.config, result); // end of file.
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
        String[] result = new String[values.length];
        System.arraycopy(values, 0, result, 0, values.length);
        return result;
    }


    @Override
    public void close() throws IOException {
        this.source.close();
        headersReader.clear();
    }

    //Added 20141016 TDQ-9496
    public int getCurrentRecord(){
        return this.source.getCurrentPosition();
    }
    
    public char getSeperator(){
        return this.config.getSeparator();
    }
    
    /**
     * Read the first record of data as the column headers. Added 20141016 TDQ-9496
     * 
     * @return If the header was successfully read or not.
     */
    public boolean readHeaders() throws IOException {
        boolean result = readNext();

        columnCount = this.values.length;

        headersReader.length = columnCount;

        headersReader.headers = new String[columnCount];

        for (int i = 0; i < headersReader.length; i++) {
            String columnValue = get(i);
            headersReader.headers[i] = columnValue;
            headersReader.indexByHeaderName.put(columnValue, new Integer(i));
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


    static class ResultAction {

        private final List<String> fields = new ArrayList<>();

        private final StringBuilder field = new StringBuilder();

        private final Consumer<List<String>> recordConsumer;

        private boolean doTrimTail;

        public ResultAction(Consumer<List<String>> recordConsumer) {
            this.recordConsumer = recordConsumer;
        }

        public void addToCurrentField(char c) {
            this.field.append(c);
        }

        public void addToCurrentField(String c) {
            this.field.append(c);
        }

        public void endField() {
            if (this.doTrimTail) {
                this.trimTail();
            }
            this.fields.add(this.field.toString());
            this.field.setLength(0);
        }

        public void endRecord(boolean skipEmpty) {
            if (!skipEmpty || this.fields.size() > 0) {
                this.recordConsumer.accept(this.fields);
            }
            this.fields.clear();
        }

        public void setDoTrimTail(boolean doTrimTail) {
            this.doTrimTail = doTrimTail;
        }

        public List<String> getFields() {
            return fields;
        }

        private void trimTail() {
            boolean doTrim = true;
            while (doTrim) {
                doTrim = this.field.length() > 0;
                if (doTrim) {
                    char lastChar = this.field.charAt(this.field.length() - 1);
                    doTrim = lastChar == ' ' || lastChar == '\t';
                }
                if (doTrim) {
                    this.field.setLength(this.field.length() - 1);
                }
            }
        }
    }

    static abstract class State {
        protected final State preceding;

        public State(State preceding) {
            this.preceding = preceding;
        }

        public State backToStart() {
            // back to start.
            State prec = this.preceding;
            while (prec != null
                    && !(StartState.class.isInstance(prec))
                    && prec.preceding != null) {
                prec = prec.preceding;
            }
            return prec;
        }

        public abstract State accept(char newChar, CSVConfig config, ResultAction action);
    }

    static class EscapeState extends State {
        public EscapeState(State preceding) {
            super(preceding);
        }

        @Override
        public State accept(char currentChar, CSVConfig config, ResultAction action) {
            char real = currentChar;

            switch (currentChar) {
                case 'n':
                    real = '\n';
                    break;
                case 'r':
                    real = '\r';
                    break;
                case 't':
                    real = '\t';
                    break;
                case 'b':
                    real = '\b';
                    break;
                case 'f':
                    real = '\f';
                    break;
                case 'e':
                    real = '\u001B';
                    break;
                case 'v':
                    real = '\u000B';
                    break;
                case 'a':
                    real = '\u0007';
                    break;
                default:
                    break;
            }

            action.addToCurrentField(real);
            return this.preceding;
        }
    }

    static class StartState extends State {

        private final StringBuilder blanksElement = new StringBuilder();

        public StartState(State preceding) {
            super(preceding);
        }

        @Override
        public State accept(char newChar, CSVConfig config, ResultAction action) {
            if (newChar == '\t' || newChar == ' ') {
                blanksElement.append(newChar);
                return this;
            }
            if (newChar == '\0') {
                this.blanksElement.setLength(0);
                action.endRecord(config.isSkipEmptyRecords());
                return this;
            }
            if (newChar == config.getQuotechar()) {
                this.blanksElement.setLength(0);
                return new QuotedFieldState(this);
            }
            if (config.isSeparator(newChar)) {
                action.addToCurrentField(this.blanksElement.toString()); // blank field.
                action.setDoTrimTail(config.isTrimWhitespace());
                action.endField();
                this.blanksElement.setLength(0);
                return this;
            }
            if (config.isLineEnd(newChar, 0)) {
                EndLineState state = new EndLineState(this);
                return state.accept(newChar, config, action);
            }

            action.addToCurrentField(this.blanksElement.toString()); // blank field.
            this.blanksElement.setLength(0);
            UnQuotedFieldState nextStep = new UnQuotedFieldState(this);
            nextStep.accept(newChar, config, action);
            return nextStep;

        }
    }

    static class QuotedFieldState extends State {

        private final StringBuilder next = new StringBuilder();

        private boolean quoteClosed = false;

        public QuotedFieldState(State preceding) {
            super(preceding);
        }

        @Override
        public State accept(char newChar, CSVConfig config, ResultAction action) {
            action.setDoTrimTail(false);
            if (config.isQuoteChar(newChar)) {
                if (!quoteClosed) {
                    quoteClosed = true;
                    this.next.append(newChar);
                } else if (config.isEscapechar(newChar)) { // double quote and quote is also escape char.
                    quoteClosed = false;
                    this.next.append(newChar);
                }
                return this;
            }
            if (!quoteClosed) {
                if (config.isEscapechar(newChar)) {
                    return new EscapeState(this);
                }

                action.addToCurrentField(newChar);
                return this;
            }
            if (newChar == '\0') {
                next.setLength(0);
                action.setDoTrimTail(false);
                action.endField();
                action.endRecord(config.isSkipEmptyRecords());
                return this.preceding;
            }
            if (newChar == ' ' || newChar == '\t') {
                this.next.append(newChar);
                return this;
            }
            if (config.isSeparator(newChar)) {
                next.setLength(0);
                action.endField();
                return this.preceding;
            }
            if (config.isLineEnd(newChar, 0)) {
                next.setLength(0);
                quoteClosed = false;
                action.setDoTrimTail(false);
                EndLineState state = new EndLineState(this);
                return state.accept(newChar, config, action);
            }

            // field continue
            action.addToCurrentField(next.toString());
            action.addToCurrentField(newChar);
            next.setLength(0);
            quoteClosed = false;
            return this;
        }
    }


    static class EndLineState extends State {
        private int pos = 0;
        private final StringBuilder builder = new StringBuilder(4);

        public EndLineState(State preceding) {
            super(preceding);
        }

        @Override
        public State accept(char newChar, CSVConfig config, ResultAction action) {

            // end of line continue
            this.builder.append(newChar);

            if (config.isLineSep(this.builder.toString())) {
                // end of line complete
                action.endField();
                action.endRecord(config.isSkipEmptyRecords());

                return this.backToStart();
            }

            if (config.isLineEnd(newChar, pos)) {
                this.pos++;
                return this;
            }
            // not end of line.
            action.addToCurrentField(builder.toString());
            //this.preceding.accept(newChar, config, action);
            this.pos = 0;
            this.builder.setLength(0);
            return this.preceding;
        }
    }

    static class UnQuotedFieldState extends State {

        public UnQuotedFieldState(State preceding) {
            super(preceding);
        }

        @Override
        public State accept(char newChar, CSVConfig config, ResultAction action) {

            action.setDoTrimTail(config.isTrimWhitespace());
            if (config.isEscapechar(newChar)) {
                return new EscapeState(this);
            }
            if (config.isSeparator(newChar)) {
                action.endField();
                return this.preceding;
            }
            if (newChar == '\0') {
                action.endField();
                action.endRecord(config.isSkipEmptyRecords());
                return this.preceding;
            }
            if (config.isLineEnd(newChar, 0)) {
                EndLineState state = new EndLineState(this);
                return state.accept(newChar, config, action);
            }
            action.addToCurrentField(newChar);
            return this;
        }
    }


}
