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
package org.talend.designer.components.hashfile.memory;

import java.util.Collections;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.talend.designer.components.hashfile.common.IteratorHashFile;
import org.talend.designer.components.hashfile.common.MATCHING_MODE;

public class AdvancedMemoryHashFile<V> {

    // it is for the MATCHING_MODE.FIRST_MATCH
    private Map<V, V> firstHash;

    // it is for the MATCHING_MODE.KEEP_ALL
    private List<V> allList;

    private MATCHING_MODE matchingMode;

    public AdvancedMemoryHashFile(MATCHING_MODE matchingMode) {
        this.matchingMode = matchingMode;
        if (matchingMode == MATCHING_MODE.KEEP_ALL) {
            allList = Collections.synchronizedList(new ArrayList<V>());
        } else if (matchingMode == MATCHING_MODE.KEEP_FIRST) {
            firstHash = Collections.synchronizedMap(new HashMap<V, V>());
        }
    }

    /**
     * DOC if put successfully, return the value itself. if put failly, return null.
     * 
     * @param value
     * @return
     */
    public V put(V value) {
        if (value != null) {
            if (matchingMode == MATCHING_MODE.KEEP_ALL) {
                allList.add(value);
                return value;
            } else if (matchingMode == MATCHING_MODE.KEEP_FIRST) {
                if (!firstHash.containsKey(value)) {
                    firstHash.put(value, value);
                    return value;
                }
            }
        }
        return null;
    }

    public Iterator<V> iterator() {
        return new IteratorHashFile(firstHash, allList, matchingMode);
    }

    public static void main(String[] args) {
        MATCHING_MODE matchingMode1 = MATCHING_MODE.KEEP_ALL;
        MATCHING_MODE matchingMode2 = MATCHING_MODE.KEEP_FIRST;
        MATCHING_MODE matchingMode = matchingMode2;
        AdvancedMemoryHashFile<String> instance = new AdvancedMemoryHashFile<String>(matchingMode);
        instance.put("A");
        instance.put("B");
        instance.put("C");
        // /////////
        instance.put("A");
        instance.put("B");
        instance.put("C");
        Iterator<String> iterator = instance.iterator();
        while (iterator.hasNext()) {
            String next = iterator.next();
            System.out.println("outer:" + next);

            Iterator itor = instance.iterator();
            while (itor.hasNext()) {
                Object nt = itor.next();
                System.out.println("---inner:" + nt);
            }
        }

    }
}
