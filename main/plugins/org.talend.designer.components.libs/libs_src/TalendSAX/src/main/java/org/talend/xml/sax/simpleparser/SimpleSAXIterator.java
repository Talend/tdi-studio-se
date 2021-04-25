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
package org.talend.xml.sax.simpleparser;

import java.util.Iterator;
import java.util.Map;

/**
 * DOC Administrator class global comment. Detailled comment
 */
public class SimpleSAXIterator implements Iterator<Map<String, String>> {

    private DataBufferCache cache;

    public SimpleSAXIterator(DataBufferCache bcache) {
        this.cache = bcache;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.util.Iterator#hasNext()
     */
    public boolean hasNext() {
        // TODO Auto-generated method stub
        return cache.hasData();
    }

    /*
     * (non-Javadoc)
     *
     * @see java.util.Iterator#next()
     */
    public Map<String, String> next() {
        // TODO Auto-generated method stub
        return cache.readData();
    }

    /*
     * (non-Javadoc)
     *
     * @see java.util.Iterator#remove()
     */
    public void remove() {
        throw new UnsupportedOperationException();

    }

}
