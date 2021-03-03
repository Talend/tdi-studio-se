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
package org.talend.designer.components.hashfile.memory;

import java.util.Collections;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.talend.designer.components.hashfile.common.MATCHING_MODE;

public class AdvancedMemoryHashFile<V> {

    // it is for the MATCHING_MODE.KEEP_FIRST or MATCHING_MODE.KEEP_LAST
    private Map<V, V> firstOrLastHash;

    // it is for the MATCHING_MODE.KEEP_ALL
    private List<V> allList;

    private MATCHING_MODE matchingMode;

    public AdvancedMemoryHashFile(MATCHING_MODE matchingMode) {
        this.matchingMode = matchingMode;
        if (matchingMode == MATCHING_MODE.KEEP_ALL) {
            allList = Collections.synchronizedList(new ArrayList<V>());
        } else {
            firstOrLastHash = Collections.synchronizedMap(new HashMap<V, V>());
        }
    }

    /**
     * Place record to Cache.
     * <ul>
     * <li> MATCHING_MODE.KEEP_ALL: all records are stored</li>
     * <li> MATCHING_MODE.KEEP_FIRST: if several records have the same key, then only first record is stored</li>
     * <li> MATCHING_MODE.KEEP_LAST: if several records have the same key, then only last record is stored</li>
     * </ul>
     *
     * @param value to be stored in Cache.
     * @return stored value or null if value shouldn't be placed in Cache.
     */
    public V put(V value) {
        if (value == null) {
            return null;
        }

        if (matchingMode == MATCHING_MODE.KEEP_ALL) {
            allList.add(value);
            return value;
        }
        if (matchingMode == MATCHING_MODE.KEEP_LAST) {
            firstOrLastHash.put(value, value);
            return value;
        }
        if (!firstOrLastHash.containsKey(value)) {
            firstOrLastHash.put(value, value);
            return value;
        }

        return null;
    }

    public Iterator<V> iterator() {
        if (matchingMode == MATCHING_MODE.KEEP_ALL) {
            return allList.iterator();
        }
        return firstOrLastHash.values().iterator();
    }

}
