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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import org.apache.commons.collections.list.GrowthList;
import org.talend.commons.utils.data.map.MultiLazyValuesMap;

/**
 * DOC amaumont class global comment. Detailled comment <br/>
 *
 * @param <V> value
 */
public class AdvancedMemoryLookup<V> implements IMemoryLookup<V, V>, Cloneable {

    protected MultiLazyValuesMap mapOfCol;

    private Map<V, V> uniqueHash;

    private boolean countValuesForEachKey;

    private Map<V, Integer> counterHash;

    private List<V> list = new ArrayList<V>();

    private Object[] arrayValues;

    protected boolean arrayIsDirty = true;

    private List<V> listResult;

    private V objectResult;

    private boolean keepAllValues;

    private MATCHING_MODE matchingMode;

    private static final int ZERO = 0;

    private static final int ONE = 1;

    int currentIndex = 0;

    private int sizeResultList;

    private boolean hasResult;

    /**
     *
     * <code>AdvancedLookup</code> can be configured to store values in different modes.
     *
     * @param useHashKeys use <code>equals()</code> and <code>hashCode()</code> methods by storing objects in hash maps
     * @param matchingMode to optimize storing and searching, and to specify which matching mode should used
     * @param uniqueMatch keep in the lookup only the last put object, but store the current number of same values for
     * each key
     * @param keepAllValues keep all identical values (with same key values) in each list of each key
     * @param countValuesForEachKey force internal count of values
     */
    public AdvancedMemoryLookup(MATCHING_MODE matchingMode, boolean keepAllValues, boolean countValuesForEachKey) {
        super();
        this.keepAllValues = keepAllValues;
        this.matchingMode = matchingMode == null ? MATCHING_MODE.UNIQUE_MATCH : matchingMode;
        this.countValuesForEachKey = countValuesForEachKey; // || this.matchingMode == MATCHING_MODE.UNIQUE_MATCH;
        if (matchingMode == MATCHING_MODE.UNIQUE_MATCH && !keepAllValues) {
            uniqueHash = new HashMap<V, V>();
        }
        if (this.countValuesForEachKey) {
            counterHash = new HashMap<V, Integer>();
        }
        mapOfCol = new MultiLazyValuesMap(new HashMap()) {

            @Override
            public Collection instanciateNewCollection() {
                return new GrowthList(3);
            }

        };
    }

    public AdvancedMemoryLookup() {

    }

    public static <V> AdvancedMemoryLookup<V> getLookup(MATCHING_MODE matchingMode) {
        return new AdvancedMemoryLookup<V>(matchingMode, false, false);
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

    @Override
    public void initPut() {

    }

    @Override
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

    @Override
    public void endPut() {

    }

    @Override
    public void initGet() {

    }

    @Override
    public void lookup(V key) {
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

    @Override
    public boolean hasNext() {
        if (objectResult != null) {
            return true;
        } else if (listResult != null && currentIndex != sizeResultList) {
            return true;
        }
        return false;
    }

    @Override
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

    @Override
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

    public boolean isEmpty() {
        if (matchingMode == MATCHING_MODE.UNIQUE_MATCH && !keepAllValues) {
            return uniqueHash.isEmpty();
        } else if (matchingMode == MATCHING_MODE.ALL_ROWS) {
            return list.isEmpty();
        } else {
            return mapOfCol.isEmpty();
        }
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

    /**
     * Getter for list.
     *
     * @return the list
     */
    public List<V> getList() {
        return this.list;
    }

}
