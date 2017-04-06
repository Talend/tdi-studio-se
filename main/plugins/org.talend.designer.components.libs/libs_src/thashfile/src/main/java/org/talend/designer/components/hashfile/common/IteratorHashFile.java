// ============================================================================
//
// Copyright (C) 2006-2016 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.designer.components.hashfile.common;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

public class IteratorHashFile<V> implements Iterator<V> {

    // it is for the MATCHING_MODE.FIRST_MATCH
    private Map<V, V> firstHash;

    // it is for the MATCHING_MODE.KEEP_ALL
    private List<V> allList;

    private MATCHING_MODE matchingMode;

    int currentIndex = 0;

    Iterator<V> iterator;

    public IteratorHashFile(Map<V, V> firstHash, List<V> allList, MATCHING_MODE matchingMode) {
        this.firstHash = firstHash;
        this.allList = allList;
        this.matchingMode = matchingMode;
        this.currentIndex = 0;

        if (matchingMode == MATCHING_MODE.KEEP_ALL) {

        } else if (matchingMode == MATCHING_MODE.KEEP_FIRST) {
            this.iterator = firstHash.keySet().iterator();
        }
    }

    public boolean hasNext() {
        if (matchingMode == MATCHING_MODE.KEEP_ALL) {
            return (currentIndex < allList.size());
        } else if (matchingMode == MATCHING_MODE.KEEP_FIRST) {

            if (iterator == null) {
                iterator = firstHash.keySet().iterator();
            }
            return currentIndex < firstHash.size();
        }

        return false;
    }

    public V next() {
        if (matchingMode == MATCHING_MODE.KEEP_ALL) {
            V next = allList.get(currentIndex);
            currentIndex++;
            return next;
        } else if (matchingMode == MATCHING_MODE.KEEP_FIRST) {
            iterator.hasNext();
            V next = iterator.next();
            currentIndex++;
            return next;
        }

        throw new NoSuchElementException();
    }

    public void remove() {
    }

}
