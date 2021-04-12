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
package org.talend.xml.sax;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * DOC s class global comment. Detailled comment
 *
 * $Id: SAXLoopIterator.java,v 1.1 2008/03/21 07:20:39 xzhang Exp $
 */
public class SAXLoopIterator implements Iterator<Map<String, String>> {

    // loop index
    private int index = 0;

    // loop size
    private int rowsSize = 0;

    private List<EntryResult> list = new LinkedList<EntryResult>();

    /**
     * DOC s SAXLoopIterator constructor comment.
     *
     * @param entry
     */
    public SAXLoopIterator(LoopEntry entry) {
        LoopEntry tmpEntry = entry;
        while (tmpEntry != null) {
            rowsSize = tmpEntry.getRows().size();
            list.add(0, new EntryResult(tmpEntry));
            tmpEntry = tmpEntry.getSubLoop();
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see java.util.Iterator#hasNext()
     */
    public boolean hasNext() {
        return index < rowsSize;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.util.Iterator#next()
     */
    public Map<String, String> next() {
        if (!hasNext())
            throw new NoSuchElementException();

        int subIndex = index;
        Map<String, String> resultRow = new HashMap<String, String>();
        for (EntryResult tmpEntry : list) {
            subIndex = tmpEntry.addRowTo(resultRow, subIndex);
        }
        index++;
        return resultRow;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.util.Iterator#remove()
     */
    public void remove() {
        throw new UnsupportedOperationException();
    }

    /**
     * DOC s SAXLoopIterator class global comment. Detailled comment
     *
     *
     */
    private static class EntryResult {

        // the current index of rows
        private int indx;

        //
        private int count;

        // the result from LoopEntry
        private List<String[]> rows;

        // for addRowTo method
        private List<String> keys;

        public EntryResult(LoopEntry loopEntry) {
            this.rows = loopEntry.getRows();
            this.keys = loopEntry.getOriginalPaths();
            this.count = rows.isEmpty() ? 0 : Integer.valueOf(rows.get(0)[keys.size()]);
        }

        /**
         *
         * DOC s Comment method "addRowTo".
         *
         * @param result
         * @param subIndex
         * @return
         */
        public int addRowTo(Map<String, String> result, int subIndex) {
            String[] tmpStrs = getRow(subIndex);

            for (int i = 0; i < tmpStrs.length - 1; i++) {
                result.put(keys.get(i), tmpStrs[i]);
            }
            return indx;
        }

        private String[] getRow(int subIndex) {
            while (count <= subIndex) {
                indx++;
                count += getCountOfRow(rows.get(indx));
            }
            return rows.get(indx);
        }

        private int getCountOfRow(String[] rows) {
            return Integer.valueOf(rows[rows.length - 1]);
        }
    }
}
