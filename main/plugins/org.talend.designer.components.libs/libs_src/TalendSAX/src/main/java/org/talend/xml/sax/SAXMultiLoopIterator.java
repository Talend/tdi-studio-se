package org.talend.xml.sax;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class SAXMultiLoopIterator implements Iterator<Map<String, Map<String, String>>> {

    private SAXLoopIterator[] iterators;

    private LoopEntry[] endEntries;

    private Iterator<String> iters;

    public SAXMultiLoopIterator(ComplexSAXLooper saxLooper, LoopEntry[] entries) {
        this.iterators = new SAXLoopIterator[entries.length];
        this.endEntries = new LoopEntry[entries.length]; // store all the last entries here.
        for (int i = 0; i < entries.length; i++) {
            this.iterators[i] = new SAXLoopIterator(entries[i]);
            LoopEntry tmpEntry = entries[i];
            while (tmpEntry.getSubLoop() != null) {
                tmpEntry = tmpEntry.getSubLoop();
            }
            endEntries[i] = tmpEntry;
        }

        this.iters = saxLooper.getLoopOrders().iterator();
    }

    public boolean hasNext() {
        // TODO Auto-generated method stub
        for (SAXLoopIterator iter : this.iterators) {
            if (iter.hasNext())
                return true;
        }
        return false;
    }

    public Map<String, Map<String, String>> next() {
        // TODO Auto-generated method stub
        if (iters.hasNext()) {

            String originalPath = iters.next();

            for (int j = 0; j < endEntries.length; j++) {
                if (originalPath.equals(endEntries[j].getOriginalLoopPath())) {
                    if (iterators[j].hasNext()) {
                        HashMap<String, Map<String, String>> map = new HashMap<String, Map<String, String>>();
                        // map.put("path", endEntries[j].getOriginalLoopPath());
                        map.put(endEntries[j].getOriginalLoopPath(), iterators[j].next());
                        return map;
                    }
                    break;
                }
            }
        }
        return null;
    }

    public void remove() {
        throw new UnsupportedOperationException();
    }

}
