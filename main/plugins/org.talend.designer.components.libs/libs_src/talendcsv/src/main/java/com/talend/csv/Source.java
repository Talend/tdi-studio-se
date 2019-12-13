package com.talend.csv;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;

public class Source implements AutoCloseable {

    private static final int FETCH_SIZE = 10 * 50;

    private static final int BUFFER_SIZE = 4 * 1024;

    private char[] buffer = new char[FETCH_SIZE];

    private int currentPosition = 0;

    private int bufferCount = 0;

    private boolean hasMoreData = true;

    private final Reader reader;

    private char previousChar = '\0';

    public Source(Reader reader) {
        if (!(reader instanceof BufferedReader)) {
            this.reader = new BufferedReader(reader, BUFFER_SIZE);
        }
        else {
            this.reader = reader;
        }
    }

    @Override
    public void close() throws IOException {
        this.reader.close();
    }

    public char currentChar() throws IOException {
        if (this.currentPosition >= this.buffer.length || bufferCount == 0) {
            this.fill();
            if (!this.hasMoreData) {
                throw new IOException("Has no more data.");
            }
        }
        return this.buffer[ this.currentPosition ];
    }

    public char previousChar() throws IOException {
        return this.previousChar;
    }

    public boolean next()  throws IOException {
        this.previousChar = this.currentChar();
        this.currentPosition++;
        if (this.currentPosition >= this.buffer.length || bufferCount == 0) {
            this.fill();
        }
        if (this.currentPosition >= this.bufferCount) {
            this.hasMoreData = false;
        }
        return this.hasMoreData;
    }

    public boolean hasMoreData() {
        return hasMoreData;
    }

    public int getCurrentPosition() {
        return currentPosition;
    }

    private void fill() throws IOException {
        int count = reader.read(buffer, 0, buffer.length);
        currentPosition = 0;
        bufferCount = count;
        if(count == -1) {
            hasMoreData = false;
        }
    }
}
