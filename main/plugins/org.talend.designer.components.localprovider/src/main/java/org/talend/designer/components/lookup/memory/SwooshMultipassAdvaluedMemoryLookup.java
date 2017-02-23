package org.talend.designer.components.lookup.memory;

public class SwooshMultipassAdvaluedMemoryLookup<V> extends AdvancedMemoryLookup<V> {

    public SwooshMultipassAdvaluedMemoryLookup(MATCHING_MODE matchingMode, boolean b, boolean c) {
        super(matchingMode, false, false);
        //
        // mapOfCol = new MultiLazyValuesMap(new HashMap()) {
        //
        // @Override
        // public Collection instanciateNewCollection() {
        // return new GrowthList(3);
        // }
        //
        // @Override
        // public Object put(Object key, Object value) {
        // Object v = get(key);
        // if (v != null) {
        // if (v instanceof List) {
        // ((List) v).add(value);
        // } else {
        // Collection list = instanciateNewCollection();
        // list.add(v);
        // list.add(value);
        // map.put(key, list);
        // }
        // } else {
        // return map.put(key, value);
        // }
        // return v;
        // }
        //
        //
        //
        // };

    }

    public static <V> SwooshMultipassAdvaluedMemoryLookup<V> getLookup(MATCHING_MODE matchingMode) {
        return new SwooshMultipassAdvaluedMemoryLookup<V>(matchingMode, false, false);
    }

    public V put(V master, V value) {
        if (value != null) {
            arrayIsDirty = true;
            V previousValue = (V) mapOfCol.put(master, value);
            incrementCountValues(value, previousValue);
            if (getMatchingMode() == MATCHING_MODE.ALL_ROWS) {
                getList().add(value);
            }
            return previousValue;
        }
        return null;
    }
}
