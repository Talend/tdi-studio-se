package com.talend.csv;

public class CSVConfig {

    private char separator = ',';

    private char quotechar = '"';

    private char escapechar = '"';

    private String lineEnd = null;

    private boolean skipEmptyRecords = false;

    private boolean trimWhitespace = true;

    public char getSeparator() {
        return separator;
    }

    public void setSeparator(char separator) {
        this.separator = separator;
    }

    public boolean isSeparator(char value) {
        return value == this.separator;
    }

    public boolean isQuoteChar(char value) {
        return this.quotechar == value;
    }

    public char getQuotechar() {
        return quotechar;
    }

    public void setQuotechar(char quotechar) {
        this.quotechar = quotechar;
    }

    public char getEscapechar() {
        return escapechar;
    }

    public void setEscapechar(char escapechar) {
        this.escapechar = escapechar;
    }

    public boolean isEscapechar(char value) {
        return this.escapechar != '\0'
                && value == this.escapechar
                && this.escapechar != this.quotechar; // mean no escape char.
    }

    public boolean isSkipEmptyRecords() {
        return skipEmptyRecords;
    }

    public void setSkipEmptyRecords(boolean skipEmptyRecords) {
        this.skipEmptyRecords = skipEmptyRecords;
    }

    public boolean isTrimWhitespace() {
        return trimWhitespace;
    }

    public void setTrimWhitespace(boolean trimWhitespace) {
        this.trimWhitespace = trimWhitespace;
    }

    public boolean isLineEnd(char elem, int pos) {
        if (this.lineEnd == null) {
            if (pos == 0) {
                return elem == '\n' || elem == '\r';
            }
            if (pos == 1) {
                return elem == '\n';
            }
            return false;
        }
        if (pos >= this.lineEnd.length()) {
            return false;
        }
        return this.lineEnd.charAt(pos) == elem;
    }

    public boolean isLineSep(String token) {
        if (this.lineEnd == null) {
            return "\n".equals(token) || "\r\n".equals(token);
        }
        return this.lineEnd.equals(token);
    }

    public void setLineEnd(String lineEnd) {
        this.lineEnd = lineEnd;
    }
}
