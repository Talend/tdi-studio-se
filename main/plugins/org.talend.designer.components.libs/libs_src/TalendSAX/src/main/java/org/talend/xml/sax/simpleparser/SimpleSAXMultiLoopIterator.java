package org.talend.xml.sax.simpleparser;

import java.util.Iterator;
import java.util.Map;

public class SimpleSAXMultiLoopIterator implements Iterator<Map<String, Map<String, String>>> {

    private DataBufferCache2 cache;

    public SimpleSAXMultiLoopIterator(DataBufferCache2 bcache) {
        this.cache = bcache;
    }

    public boolean hasNext() {
        return cache.hasData();
    }

    public Map<String,Map<String, String>> next() {
        return cache.readData();
    }

    public void remove() {
        throw new UnsupportedOperationException();
    }

}
