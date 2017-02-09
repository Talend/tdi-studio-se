package org.talend.designer.components.lookup.memory;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.collections.list.GrowthList;
import org.talend.commons.utils.data.map.MultiLazyValuesMap;
import org.talend.designer.components.lookup.common.ICommonLookup.MATCHING_MODE;


public class SwooshMultipassAdvaluedMemoryLookup<V> extends AdvancedMemoryLookup<V> {

    public SwooshMultipassAdvaluedMemoryLookup(MATCHING_MODE matchingMode, boolean b, boolean c) {
        super(matchingMode, false, false);
//        
//        mapOfCol = new MultiLazyValuesMap(new HashMap()) {
//
//            @Override
//            public Collection instanciateNewCollection() {
//                return new GrowthList(3);
//            }
//
//            @Override
//            public Object put(Object key, Object value) {
//                Object v = get(key);
//                if (v != null) {
//                    if (v instanceof List) {
//                        ((List) v).add(value);
//                    } else {
//                        Collection list = instanciateNewCollection();
//                        list.add(v);
//                        list.add(value);
//                        map.put(key, list);
//                    }
//                } else {
//                    return map.put(key, value);
//                }
//                return v;
//            }
//            
//            
//
//        };

    }

    public static <V> SwooshMultipassAdvaluedMemoryLookup<V> getLookup(MATCHING_MODE matchingMode) {
        return new SwooshMultipassAdvaluedMemoryLookup<V>(matchingMode, false, false);
    }

    public V put(V master,V value) {
        if (value != null) {
                    arrayIsDirty = true;
                    V previousValue = (V) mapOfCol.put(master, value);
                    incrementCountValues(value, previousValue);
                    return previousValue;
        }
        return null;
    }
}
