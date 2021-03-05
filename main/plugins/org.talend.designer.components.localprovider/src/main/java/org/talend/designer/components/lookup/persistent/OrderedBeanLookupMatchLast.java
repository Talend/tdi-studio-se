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
 * Ordered bean lookup for "Last match" or "Unique match" matching mode.
 * @param <B> bean
 */
public class OrderedBeanLookupMatchLast<B extends Comparable<B> & IPersistableLookupRow<B>> extends AbstractOrderedBeanLookup<B> {

    private boolean previousKeyLoaded;

    private int previousValuesSize;

    private boolean resultIsObsolete = true;

    public OrderedBeanLookupMatchLast(String keysFilePath, String valuesFilePath, int fileIndex, IRowProvider<B> rowProvider,
            boolean skipBytesEnabled)
            throws IOException {
        super(keysFilePath, valuesFilePath, fileIndex, rowProvider, skipBytesEnabled);
        lookupInstance = rowProvider.createInstance();
        previousLookupInstance = rowProvider.createInstance();
        resultLookupInstance = rowProvider.createInstance();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.components.persistent.TestA#lookup(B)
     */
    public void lookup(B key) throws IOException {

        currentSearchedKey = key;

        // System.out.println("currentSearchedKey=" + currentSearchedKey);

        if (!resultIsObsolete && previousKeyLoaded && previousAskedKey.compareTo(key) == 0) {
            nextWithPreviousLookup = true;
            nextDirty = false;
            hasNext = true;
            noMoreNext = false;
        } else {
            resultIsObsolete = true;
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

        if (nextDirty) {
            int compareResult = -1;

            long localSkip = 0;

            boolean endOfFile = isEndOfKeysFile();

            boolean previousCompareHasMatched = false;

            boolean previousValuesSizeAlreadyAdded = false;

            if (atLeastOneLoadkeys) {
                compareResult = lookupInstance.compareTo(currentSearchedKey);

                if (compareResult == 0) {
                    if (endOfFile) {
                        sizeDataToRead = currentValuesSize;
                        lookupInstance.copyKeysDataTo(resultLookupInstance);
                    } else {
                        localSkip += currentValuesSize;
                        if (currentValuesSize > 0) {
                            countBeansToSkip++;
                        }
                        previousValuesSize = currentValuesSize;
                        compareResult = -1;
                        previousCompareHasMatched = true;
                    }

                } else if (compareResult < 0) {
                    previousValuesSizeAlreadyAdded = true;
                    localSkip += previousValuesSize;
                    if (previousValuesSize > 0) {
                        countBeansToSkip++;
                    }
                }
            }
            startWithNewKey = false;

            if (!endOfFile && (compareResult < 0 || !atLeastOneLoadkeys)) {

                boolean searchingNextNotMatchAfterMatchFound = false;
                do {

                    loadDataKeys(lookupInstance);
                    // System.out.println("Loaded keys:" + lookupInstance);

                    compareResult = lookupInstance.compareTo(currentSearchedKey);

                    endOfFile = isEndOfKeysFile();
                    if (compareResult >= 0 || endOfFile) {

                        if (!searchingNextNotMatchAfterMatchFound && compareResult == 0 && !endOfFile) {
                            searchingNextNotMatchAfterMatchFound = true;
                            previousCompareHasMatched = true;
                        } else if (compareResult > 0 || endOfFile) {

                            if (endOfFile) {
                                if (compareResult == 0) {
                                    sizeDataToRead = currentValuesSize;
                                    lookupInstance.copyKeysDataTo(resultLookupInstance);

                                    if (!previousCompareHasMatched && !previousValuesSizeAlreadyAdded) {
                                        localSkip += previousValuesSize;
                                        if (previousValuesSize > 0) {
                                            countBeansToSkip++;
                                        }
                                    }

                                } else if (compareResult > 0) {
                                    if (previousCompareHasMatched) {
                                        previousLookupInstance.copyKeysDataTo(resultLookupInstance);
                                        compareResult = 0;
                                        sizeDataToRead = previousValuesSize;
                                        localSkip -= previousValuesSize;
                                        if (previousValuesSize > 0) {
                                            countBeansToSkip--;
                                        }
                                    } else if (!previousValuesSizeAlreadyAdded) {
                                        localSkip += previousValuesSize;
                                        if (previousValuesSize > 0) {
                                            countBeansToSkip++;
                                        }
                                    }
                                }

                            } else {
                                sizeDataToRead = previousValuesSize;
                                if (previousCompareHasMatched) {
                                    localSkip -= previousValuesSize;
                                    if (previousValuesSize > 0) {
                                        countBeansToSkip--;
                                    }
                                    compareResult = 0;
                                } else if (!previousValuesSizeAlreadyAdded) {
                                    localSkip += previousValuesSize;
                                    if (previousValuesSize > 0) {
                                        countBeansToSkip++;
                                    }
                                }
                                previousLookupInstance.copyKeysDataTo(resultLookupInstance);
                            }

                            remainingSkip = 0;
                            lookupInstance.copyKeysDataTo(previousLookupInstance);
                            previousValuesSize = currentValuesSize;
                            break;
                        }
                        lookupInstance.copyKeysDataTo(previousLookupInstance);
                        localSkip += currentValuesSize;
                        if (currentValuesSize > 0) {
                            countBeansToSkip++;
                        }
                        previousValuesSizeAlreadyAdded = false;
                        previousValuesSize = currentValuesSize;
                    }
                    if (compareResult < 0 && !searchingNextNotMatchAfterMatchFound) {
                        localSkip += currentValuesSize;
                        if (currentValuesSize > 0) {
                            countBeansToSkip++;
                        }
                    }
                } while (true);
            }
            if (compareResult == 0) {
                resultIsObsolete = false;
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
