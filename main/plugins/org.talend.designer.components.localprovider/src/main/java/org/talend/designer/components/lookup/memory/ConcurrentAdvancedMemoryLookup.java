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

package org.talend.designer.components.lookup.memory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Class added and implemented to resolve thread safety issues surrounding the AdvancedMemoryLookup class where across
 * multiple partitions we would get various exceptions when multiple threads would try to use the same lookup to save
 * memory.
 *
 * @param <V> V
 * @author rbaldwin
 */
public class ConcurrentAdvancedMemoryLookup<V> extends AdvancedMemoryLookup<V> implements IMemoryLookup<V, V>, Cloneable {

    /**
     * Concurrent multi lazy values map
     */
    private class CMLVM extends java.util.HashMap {

        private ConcurrentHashMap map;

        public CMLVM(ConcurrentHashMap map) {
            super();
            this.map = map;
        }

        public void clear() {
            map.clear();
        }

        public boolean containsKey(Object key) {
            return map.containsKey(key);
        }

        public boolean containsValue(Object value) {
            return map.containsValue(value);
        }

        public Set<ConcurrentHashMap.Entry> entrySet() {
            return map.entrySet();
        }

        public Object get(Object key) {
            return map.get(key);
        }

        public boolean isEmpty() {
            return map.isEmpty();
        }

        public Set keySet() {
            return map.keySet();
        }

        public Object put(Object key, Object value) {
            Object v = map.get(key);
            if (v != null) {
                if (v instanceof List) {
                    ((List) v).add(value);
                } else {
                    Collection list = instanciateNewCollection();
                    list.add(v);
                    list.add(value);
                    map.put(key, list);
                }
            } else {
                return map.put(key, value);
            }
            return v;
        }

        /**
         * DOC amaumont Comment method "instanciateNewList".
         *
         * @return
         */
        public Collection instanciateNewCollection() {
            return new CopyOnWriteArrayList();
        }

        public void putAll(Map t) {
            map.putAll(t);
        }

        public Object remove(Object key) {
            return map.remove(key);
        }

        public int size() {
            return map.size();
        }

        public Collection values() {
            return map.values();
        }

        public Object removeValue(Object key, Object value) {
            Object v = map.get(key);
            if (v != null) {
                if (v instanceof List) {
                    ((List) v).remove(value);
                    return value;
                } else if (value.equals(v)) {
                    remove(key);
                    return value;
                }
                return null;
            }
            return null;
        }

        public Collection getCollection(Object key) {
            Object v = map.get(key);
            if (v != null) {
                if (v instanceof List) {
                    return (Collection) v;
                } else {
                    Collection list = instanciateNewCollection();
                    list.add(v);
                    map.put(key, list);
                    return list;
                }
            } else {
                Collection list = instanciateNewCollection();
                map.put(key, list);
                return list;
            }
        }
    }

    private CMLVM mapOfCol;

    private Map<V, V> uniqueHash;

    private boolean countValuesForEachKey;

    private Map<V, Integer> counterHash;

    private List<V> list = new CopyOnWriteArrayList<V>();

    private Object[] arrayValues;

    private boolean arrayIsDirty = true;

    private List<V> listResult;

    private V objectResult;

    private boolean keepAllValues;

    private MATCHING_MODE matchingMode;

    private static final int ZERO = 0;

    private static final int ONE = 1;

    int currentIndex = 0;

    // ThreadLocal<java.util.concurrent.atomic.AtomicInteger> currentIndex;

    private int sizeResultList;

    private boolean hasResult;

    private V lastCheckedKey;

    public ConcurrentAdvancedMemoryLookup(MATCHING_MODE matchingMode, boolean keepAllValues, boolean countValuesForEachKey) {
        super();
        this.keepAllValues = keepAllValues;
        this.matchingMode = matchingMode == null ? MATCHING_MODE.UNIQUE_MATCH : matchingMode;
        this.countValuesForEachKey = countValuesForEachKey; // || this.matchingMode == MATCHING_MODE.UNIQUE_MATCH;
        if (matchingMode != MATCHING_MODE.ALL_ROWS) {
            if (matchingMode == MATCHING_MODE.UNIQUE_MATCH && !keepAllValues) {
                uniqueHash = new ConcurrentHashMap<V, V>(1000, .75f, 1);
            }
            if (this.countValuesForEachKey) {
                counterHash = new ConcurrentHashMap<V, Integer>(1000, .75f, 1);
            }
            mapOfCol = new CMLVM(new ConcurrentHashMap(1000, .75f, 1));
        }
    }

    public ConcurrentAdvancedMemoryLookup(ConcurrentAdvancedMemoryLookup<V> other) {
        super();
        this.keepAllValues = other.keepAllValues;
        this.matchingMode = other.matchingMode == null ? MATCHING_MODE.UNIQUE_MATCH : other.matchingMode;
        this.countValuesForEachKey = other.countValuesForEachKey;
        if (matchingMode != MATCHING_MODE.ALL_ROWS) {
            if (matchingMode == MATCHING_MODE.UNIQUE_MATCH && !keepAllValues) {
                uniqueHash = new ConcurrentHashMap<V, V>(other.uniqueHash.size(), .75f, 1);
                uniqueHash.putAll(other.uniqueHash);// new ConcurrentHashMap<V, V>(1000,.9f, 1);
            }
            if (this.countValuesForEachKey) {
                counterHash = new ConcurrentHashMap<V, Integer>(1000, .9f, 1);
                counterHash.putAll(other.counterHash);
            }
            mapOfCol = new CMLVM(new ConcurrentHashMap(1000, .9f, 1));
            mapOfCol.putAll(other.mapOfCol);
        }
    }

    public static synchronized <V> ConcurrentAdvancedMemoryLookup<V> copyLookup(ConcurrentAdvancedMemoryLookup<V> other) {
        ConcurrentAdvancedMemoryLookup<V> tmp = new ConcurrentAdvancedMemoryLookup<V>(other.matchingMode, other.keepAllValues,
                other.countValuesForEachKey);
        tmp.uniqueHash = other.uniqueHash;

        tmp.counterHash = other.counterHash;
        if (tmp.counterHash != null) {
            int tCHS = tmp.counterHash.size();
            int oCHS = other.counterHash.size();
        }
        tmp.mapOfCol = other.mapOfCol;
        if (tmp.mapOfCol != null) {
            int tMOCS = tmp.mapOfCol.size();
            int oMOCS = other.mapOfCol.size();
        }

        tmp.list = other.list;
        if (tmp.list != null) {
            int tls = tmp.list.size();
            int ols = other.list.size();
        }
        tmp.arrayValues = other.arrayValues;
        tmp.arrayIsDirty = other.arrayIsDirty;
        tmp.listResult = other.listResult;
        tmp.objectResult = other.objectResult;
        if (tmp.uniqueHash != null) {
            int tUHS = tmp.uniqueHash.size();
            int oUHS = other.uniqueHash.size();
        }

        return tmp;
    }

    public String getSnapshot() {
        String rc = "";
        rc += "arrayValues = [" + arrayValues + "]";
        rc += "\tarrIsDirts = [" + arrayIsDirty + "]";
        rc += "\tcounterHash = [" + counterHash + "]";
        rc += "\tlist = [" + list + "]";
        rc += "\tobjectResult = [" + objectResult + "]";
        rc += "\tlastCheckedKey = [" + lastCheckedKey + "]";
        return rc;
    }

    public static <V> ConcurrentAdvancedMemoryLookup<V> getLookup(MATCHING_MODE matchingMode) {
        return new ConcurrentAdvancedMemoryLookup<V>(matchingMode, false, false);
    }

    public Object[] getResultArray() {
        if (matchingMode == MATCHING_MODE.ALL_ROWS) {
            if (listResult == null) {
                listResult = list;
            }
            if (arrayIsDirty) {
                arrayValues = listResult.toArray();
                arrayIsDirty = false;
            }
            return arrayValues;
        } else {
            return listResult.toArray();
        }
    }

    public List<V> getResultList() {
        return listResult;
    }

    public V getResultObject() {
        return objectResult;
    }

    public boolean resultIsObject() {
        return objectResult != null;
    }

    public boolean resultIsList() {
        return listResult != null;
    }

    public void initPut() {

    }

    public V put(V value) {
        if (value != null) {
            if (matchingMode == MATCHING_MODE.UNIQUE_MATCH && !keepAllValues) {
                V previousValue = uniqueHash.put(value, value);
                incrementCountValues(value, previousValue);
                return previousValue;
            } else {
                if (matchingMode == MATCHING_MODE.ALL_ROWS) {
                    list.add(value);
                    return null;
                } else {
                    arrayIsDirty = true;
                    V previousValue = (V) mapOfCol.put(value, value);
                    incrementCountValues(value, previousValue);
                    return previousValue;
                }
            }
        }
        return null;
    }

    public void endPut() {

    }

    public void initGet() {

    }

    private static ThreadLocal<AtomicInteger> initializeCurIndex() {
        return new ThreadLocal<AtomicInteger>() {

            protected AtomicInteger initialValue() {
                return new AtomicInteger(0);
            }
        };
    }

    public void lookup(V key) {
        lastCheckedKey = key;
        if (matchingMode == MATCHING_MODE.UNIQUE_MATCH) {
            listResult = null;
            objectResult = uniqueHash.get(key);
        } else {
            if (matchingMode != MATCHING_MODE.ALL_ROWS && key != null) {
                Object v = mapOfCol.get(key);
                if (v instanceof List) {
                    List<V> localList = (List<V>) v;
                    if (matchingMode == MATCHING_MODE.ALL_MATCHES) {
                        listResult = localList;
                        currentIndex = 0;
                        sizeResultList = localList.size();
                        objectResult = null;
                    } else if (matchingMode == MATCHING_MODE.FIRST_MATCH) {
                        objectResult = localList.get(ZERO);
                    } else if (matchingMode == MATCHING_MODE.LAST_MATCH) {
                        hasResult = false;
                        listResult = null;
                        objectResult = localList.get(localList.size() - ONE);
                    }
                } else {
                    hasResult = false;
                    objectResult = (V) v;
                    listResult = null;
                }
            } else {
                hasResult = false;
                listResult = list;
                currentIndex = 0;
                sizeResultList = list.size();
                objectResult = null;
            }
        }

    }

    public boolean hasNext() {
        if (objectResult != null) {
            return true;
        } else if (listResult != null && currentIndex != sizeResultList) {
            return true;
        }
        return false;
    }

    public V next() {
        if (objectResult != null) {

            hasResult = true;
            V object = objectResult;
            objectResult = null;
            return object;
        } else if (listResult != null) {
            hasResult = true;
            return listResult.get(currentIndex++);
        }
        throw new NoSuchElementException();
    }

    public void endGet() {
        clear();
    }

    /**
     * DOC amaumont Comment method "incrementCountValues".
     *
     * @param value
     * @param previousValue
     */
    protected void incrementCountValues(V value, V previousValue) {
        if (countValuesForEachKey) {
            Integer count;
            if (previousValue == null) {
                count = ONE;
            } else {
                count = counterHash.get(value);
                count++;
            }
            counterHash.put(value, count);
        }
    }

    public void clear() {
        if (mapOfCol != null) {
            mapOfCol.clear();
        }
        if (uniqueHash != null) {
            uniqueHash.clear();
        }
        if (counterHash != null) {
            counterHash.clear();
        }
        arrayValues = null;
        if (list != null) {
            list.clear();
        }
        listResult = null;
    }

    /**
     * DOC amaumont Comment method "hasResult".
     *
     * @return
     */
    public boolean hasResult() {
        return hasResult;
    }

    /**
     * Getter for hasHashKeys.
     *
     * @return the hasHashKeys
     */
    public boolean isUseHashKeys() {
        return matchingMode != MATCHING_MODE.ALL_ROWS;
    }

    /**
     * Getter for countValuesForEachKey.
     *
     * @return the countValuesForEachKey
     */
    public boolean isCountValuesForEachKey() {
        return this.countValuesForEachKey;
    }

    /**
     * Getter for keepAllValues.
     *
     * @return the keepAllValues
     */
    public boolean isKeepAllValues() {
        return this.keepAllValues;
    }

    /**
     * Getter for uniqueMatch.
     *
     * @return the uniqueMatch
     */
    public boolean isUniqueMatch() {
        return matchingMode == MATCHING_MODE.UNIQUE_MATCH;
    }

    /**
     * Getter for uniqueMatch.
     *
     * @return the uniqueMatch
     */
    public boolean isOnlyOneMatchResult() {
        return matchingMode == MATCHING_MODE.UNIQUE_MATCH || matchingMode == MATCHING_MODE.FIRST_MATCH
                || matchingMode == MATCHING_MODE.LAST_MATCH;
    }

    public int getCount(V key) {
        if (countValuesForEachKey) {
            Integer count = counterHash.get(key);
            if (count == null) {
                return ZERO;
            } else {
                return count;
            }
        } else if (matchingMode == MATCHING_MODE.UNIQUE_MATCH && !keepAllValues) {
            if (uniqueHash.get(key) != null) {
                return ONE;
            } else {
                return ZERO;
            }

        } else if (matchingMode != MATCHING_MODE.ALL_ROWS) {
            Object v = mapOfCol.get(key);
            if (v instanceof List) {
                List<V> localList = (List<V>) v;
                return localList.size();
            } else {
                if (v != null) {
                    return ONE;
                } else {
                    return ZERO;
                }
            }
        } else {
            if (list.contains(key)) {
                return 1;
            } else {
                return ZERO;
            }
        }
    }

    /**
     * Getter for matchingMode.
     *
     * @return the matchingMode
     */
    public MATCHING_MODE getMatchingMode() {
        return matchingMode;
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    /**
     * Getter for id_Document lookup(for tXMLMap) Purpose : Get all value data storing in the lookup Object Use case :
     * When no basic lookup(not Document lookup) exists,but Document lookup exists for ALL,First Matching(When no basic
     * lookup,not override the hashCode(),equals() method,so no List<V> value,only V value)
     */
    public void lookup() {
        List<V> localList = new ArrayList<V>();
        if (matchingMode == MATCHING_MODE.UNIQUE_MATCH) {
            for (V value : uniqueHash.values()) {
                localList.add(value);
            }
        } else {
            for (Object value : mapOfCol.values()) {
                localList.add((V) value);
            }
        }
        listResult = localList;
        sizeResultList = localList.size();
        objectResult = null;
        currentIndex = 0;
    }
}
