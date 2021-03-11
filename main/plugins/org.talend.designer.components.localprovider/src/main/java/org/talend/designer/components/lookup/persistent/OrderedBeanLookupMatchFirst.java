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
 * Ordered bean lookup for "First match" matching mode.
 * @param <B> bean
 */
public class OrderedBeanLookupMatchFirst<B extends Comparable<B> & IPersistableLookupRow<B>> extends AbstractOrderedBeanLookup<B> {

    private boolean previousKeyLoaded;

    private int previousValuesSize;

    public OrderedBeanLookupMatchFirst(String keysFilePath, String valuesFilePath, int fileIndex, IRowProvider<B> rowProvider,
            boolean skipBytesEnabled)
            throws IOException {
        super(keysFilePath, valuesFilePath, fileIndex, rowProvider, skipBytesEnabled);
        lookupInstance = rowProvider.createInstance();
        resultLookupInstance = rowProvider.createInstance();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.components.persistent.TestA#lookup(B)
     */
    public void lookup(B key) throws IOException {

        currentSearchedKey = key;

        if (previousKeyLoaded && previousAskedKey.compareTo(key) == 0) {
            nextWithPreviousLookup = true;
            nextDirty = false;
            hasNext = true;
            noMoreNext = false;
        } else {
            hasNext = false;
            nextDirty = true;
            startWithNewKey = true;
            nextWithPreviousLookup = false;
        }

        key.copyKeysDataTo(previousAskedKey);
        previousKeyLoaded = true;

    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.components.persistent.TestA#hasNext()
     */
    public boolean hasNext() throws IOException {

        if (currentSearchedKey == null) {
            return false;
        }

        if(nextWithPreviousLookup) {
            return previousCompareResultMatch;
        }

        if (nextDirty) {
            int compareResult = -1;

            int localSkip = 0;

            boolean endOfFile = isEndOfKeysFile();

            if (atLeastOneLoadkeys) {
                compareResult = lookupInstance.compareTo(currentSearchedKey);

                if (compareResult == 0) {
                    localSkip -= previousValuesSize;
                    sizeDataToRead = currentValuesSize;
                    lookupInstance.copyKeysDataTo(resultLookupInstance);
                }
            }
            startWithNewKey = false;

            if (!endOfFile && compareResult < 0) {

                do {

                    loadDataKeys(lookupInstance);

                    compareResult = lookupInstance.compareTo(currentSearchedKey);

                    endOfFile = isEndOfKeysFile();
                    if (compareResult >= 0 || endOfFile) {

                        if (compareResult == 0) {
                            sizeDataToRead = currentValuesSize;
                            lookupInstance.copyKeysDataTo(resultLookupInstance);
                            previousValuesSize = 0;
                            currentValuesSize = 0;
                        } else {
                            previousValuesSize = currentValuesSize;
                            localSkip += currentValuesSize;
                        }

                        break;

                    }
                    localSkip += currentValuesSize;
                    if (currentValuesSize > 0) {
                        countBeansToSkip++;
                    }
                    previousValuesSize = currentValuesSize;
                } while (true);
            }
            if (compareResult == 0) {
                previousCompareResultMatch = true;
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

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.components.persistent.TestA#next()
     */
    public B next() throws IOException {

        previousCompareResultMatch = true;

        if (noMoreNext || nextDirty) {
            throw new NoSuchElementException();
        }

        nextDirty = true;
        B row = null;
        if (nextWithPreviousLookup) {
            row = resultLookupInstance;
        } else {
            loadDataValues(resultLookupInstance, sizeDataToRead);
            row = resultLookupInstance;
        }

        return row;
    }

}
