// ============================================================================
//
// Copyright (C) 2006-2021 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package com.talend.excel.xssf.event;

import java.util.LinkedList;
import java.util.List;

/**
 * DOC Administrator class global comment. Detailled comment
 */
public class DataBufferCache {

    // private static DataBufferCache instance = new DataBufferCache();

    private long bufferSize = 5000;

    private boolean isEnd = false;

    private int bufferNum = 10;

    private LinkedList<Buffer> buffers = new LinkedList<Buffer>();

    private Buffer currentReadBuff = null;

    private Buffer currentWriteBuff = null;

    private DataBufferCache() {
    }

    public static DataBufferCache getInstance() {
        return new DataBufferCache();
    }

    public void setIsEnd() {
        synchronized (buffers) {
            try {
                if (!isEnd && currentWriteBuff != null) {
                    buffers.add(currentWriteBuff);
                }
                this.isEnd = true;
            } finally {
                this.buffers.notifyAll();
            }
        }
    }

    public void setBufferNum(int num) {
        if (num <= 0) {
            return;
        }
        this.bufferNum = num;
    }

    public void setBufferSize(long size) {
        if (size <= 0) {
            return;
        }
        this.bufferSize = size;
    }

    public boolean hasData() {
        getCurrReadBuffer();
        if (currentReadBuff == null) {
            return false;
        }
        return this.currentReadBuff.hasNext();
    }

    public List<String> readData() {
        return this.currentReadBuff.Next();
    }

    private void getCurrReadBuffer() {
        if (currentReadBuff != null && currentReadBuff.hasNext()) {
            return;
        }
        synchronized (this.buffers) {
            try {
                while (!this.isEnd && buffers.size() < 1) {
                    try {
                        buffers.wait();
                    } catch (InterruptedException ex) {
                        System.err.println(ex.getMessage());
                    }
                }
                if (buffers.size() > 0) {
                    currentReadBuff = buffers.remove();
                }
            } finally {
                this.buffers.notifyAll();
            }
        }
    }

    public void writeData(List<String> record) {
        if (currentWriteBuff == null) {
            currentWriteBuff = new Buffer();
        }
        if (currentWriteBuff.size() < this.bufferSize) {
            currentWriteBuff.add(record);
        } else {
            currentWriteBuff.add(record);
            synchronized (buffers) {
                try {
                    while (buffers.size() > bufferNum) {
                        try {
                            buffers.wait();
                        } catch (InterruptedException ex) {
                            System.err.println(ex.getMessage());
                        }
                    }
                    if (currentWriteBuff.size() > 0) {
                        this.buffers.add(currentWriteBuff);
                    }
                    currentWriteBuff = null;
                } finally {
                    this.buffers.notifyAll();
                }
            }

        }

    }

    public void notifyErrorOccurred() {
        setIsEnd();
    }

    class Buffer {

        private LinkedList<List<String>> buffer = null;

        private boolean isForRead = false; // false for write, true for read

        public Buffer() {
            buffer = createNewBuffer();
        }

        public void setIsForRead(boolean isForRead) {
            this.isForRead = isForRead;
        }

        public boolean isForRead() {
            return this.isForRead;
        }

        public boolean hasNext() {
            return buffer.size() > 0;
        }

        public List<String> Next() {
            return buffer.remove();
        }

        public void add(List<String> o) {
            this.buffer.add(o);
        }

        public int size() {
            return this.buffer.size();
        }

        public void clear() {
            this.buffer.clear();
            buffer = null;
        }

        private LinkedList<List<String>> createNewBuffer() {
            return new LinkedList<List<String>>();
        }
    }
}
