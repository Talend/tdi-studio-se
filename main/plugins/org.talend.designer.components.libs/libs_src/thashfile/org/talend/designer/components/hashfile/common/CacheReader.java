// ============================================================================
//
// Copyright (C) 2006-2014 Talend Inc. - www.talend.com
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

import java.util.List;
import java.util.Map;

/**
 * created by bchen on Aug 1, 2014 Detailled comment
 * 
 */
public class CacheReader<V> {

    // it is for the MATCHING_MODE.FIRST_MATCH
    private Map<V, V> firstHash;

    // it is for the MATCHING_MODE.KEEP_ALL
    private List<V> allList;

    private MATCHING_MODE matchingMode;

    int currentIndex = 0;

    private CacheReader(Map<V, V> firstHash, List<V> allList, MATCHING_MODE matchingMode) {
        this.firstHash = firstHash;
        this.allList = allList;
        this.matchingMode = matchingMode;
        this.currentIndex = 0;

    }

    public static CacheReader getInstance(Map firstHash, List allList, MATCHING_MODE matchingMode) {
        return new CacheReader(firstHash, allList, matchingMode);
    }

    /*
     * throw IndexOutOfBoundsException when read in end
     */
    public synchronized V read() {
        V current = allList.get(currentIndex);
        currentIndex++;
        return current;
    }
}
