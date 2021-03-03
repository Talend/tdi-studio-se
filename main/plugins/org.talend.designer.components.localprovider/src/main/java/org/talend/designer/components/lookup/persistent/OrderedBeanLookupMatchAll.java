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

package org.talend.designer.components.lookup.persistent;

import java.io.IOException;
import java.util.NoSuchElementException;

import org.talend.designer.components.persistent.IRowProvider;

import routines.system.IPersistableLookupRow;

/**
 * Ordered bean lookup for "All matches" matching mode.
 *
 * @param <B> bean
 */
public class OrderedBeanLookupMatchAll<B extends Comparable<B> & IPersistableLookupRow<B>> extends AbstractOrderedBeanLookup<B> {

    protected boolean nextFromCache;

    private int previousValuesSize;

    private boolean previousKeyLoaded;

    /**
     *
     * DOC amaumont OrderedBeanLookupMatchAll constructor comment.
     *
     * @param keysFilePath
     * @param valuesFilePath
     * @param fileIndex
     * @param rowProvider
     * @param skipBytesEnabled
     * @param keysManagement
     * @throws IOException
     */
    public OrderedBeanLookupMatchAll(String keysFilePath, String valuesFilePath, int fileIndex, IRowProvider<B> rowProvider,
            boolean skipBytesEnabled) throws IOException {
        super(keysFilePath, valuesFilePath, fileIndex, rowProvider, skipBytesEnabled);
        lookupInstance = rowProvider.getFreeInstance();
    }

    public void lookup(B key) throws IOException {

        currentSearchedKey = key;

        nextDirty = true;

        if (previousKeyLoaded && previousAskedKey.compareTo(key) == 0) {
            startWithNewKey = false;
            nextFromCache = true;
            rowProvider.resetInstanceIndex();
        } else {
            rowProvider.resetFreeIndex();
            lookupInstance = rowProvider.getFreeInstance();
            if (previousLookupInstance == null) {
                previousLookupInstance = rowProvider.createInstance();
            } else {
                previousLookupInstance.copyKeysDataTo(lookupInstance);
            }
            startWithNewKey = true;
            nextFromCache = false;
        }

        key.copyKeysDataTo(previousAskedKey);
        previousKeyLoaded = true;

    }

    /**
     * DOC slanglois Comment method "hasNext".
     *
     * @return
     * @throws IOException
     */
    public boolean hasNext() throws IOException {

        if (currentSearchedKey == null) {
            return false;
        }

        if (nextFromCache) {
            return rowProvider.hasNext();
        }

        if (nextDirty) {

            int compareResult = -1;

            int localSkip = 0;

            if (atLeastOneLoadkeys && startWithNewKey) {
                compareResult = lookupInstance.compareTo(currentSearchedKey);
                if (compareResult == 0) {
                    localSkip -= previousValuesSize;
                    if (previousValuesSize > 0) {
                        countBeansToSkip--;
                    }
                    // lookupInstance = previousLookupInstance;
                    if (previousCompareResultMatch) {
                        remainingSkip = 0;
                    }
                }
                if (compareResult > 0) {
                    // localSkip += currentValuesSize;
                }

            }

            if (compareResult < 0 || !atLeastOneLoadkeys) {

                if (isEndOfKeysFile()) {
                    noMoreNext = true;
                    return false;
                }

                do {

                    loadDataKeys(lookupInstance);
                    compareResult = lookupInstance.compareTo(currentSearchedKey);
                    if (compareResult > 0) {
                        // remainingSkip = currentValuesSize;
                        // localSkip += currentValuesSize;
                    }
                    if (compareResult >= 0 || isEndOfKeysFile()) {
                        if (compareResult == 0) {
                            if (!startWithNewKey) {
                                // localSkip -= currentValuesSize;
                            }
                        } else {
                            lookupInstance.copyKeysDataTo(previousLookupInstance);
                            previousValuesSize = currentValuesSize;
                            localSkip += currentValuesSize;
                            if (currentValuesSize > 0) {
                                countBeansToSkip++;
                            }
                        }
                        sizeDataToRead = currentValuesSize;
                        // previousLookupInstance = lookupInstance;
                        break;
                    }
                    if (compareResult < 0) {
                        localSkip += currentValuesSize;
                        if (currentValuesSize > 0) {
                            countBeansToSkip++;
                        }
                    }
                } while (true);
            }
            startWithNewKey = false;
            if (compareResult == 0) {
                previousCompareResultMatch = true;
                // skipValuesSize -= remainingSkip;
                skipValuesSize += localSkip;
                hasNext = true;
                noMoreNext = false;
                nextDirty = false;
                return true;
            } else if (compareResult < 0) {
                previousCompareResultMatch = false;
                skipValuesSize += localSkip;
                nextDirty = true;
                noMoreNext = true;
                hasNext = false;
                return false;
            } else {
                previousCompareResultMatch = false;
                skipValuesSize += localSkip;
                nextDirty = true;
                noMoreNext = false;
                hasNext = false;
                return false;
            }
        } else {
            return hasNext;
        }
    }

    /**
     * DOC slanglois Comment method "next".
     *
     * @return
     * @throws IOException
     */
    public B next() throws IOException {

        // previousCompareResultMatch = true;
        if (nextFromCache) {
            return rowProvider.next();
        }

        if (noMoreNext || nextDirty) {
            throw new NoSuchElementException();
        }

        loadDataValues(lookupInstance, sizeDataToRead);

        // lookupInstance.copyKeysDataTo(previousLookupInstance);

        nextDirty = true;

        B row = lookupInstance;
        lookupInstance = rowProvider.getFreeInstance();

        // previousLookupInstance.copyKeysDataTo(lookupInstance);

        atLeastOneLoadkeys = false;

        return row;
    }

    /**
     * DOC slanglois Comment method "close".
     *
     * @throws IOException
     */
//    public void close() throws IOException {
//        if (keysObjectInStream != null) {
//            keysObjectInStream.close();
//        }
//        if (valuesDataInStream != null) {
//            valuesDataInStream.close();
//        }
//    }

}
