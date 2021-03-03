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
package core.performance;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.collections.FastHashMap;
import org.apache.commons.collections.keyvalue.MultiKey;
import org.apache.commons.collections.map.HashedMap;
import org.apache.commons.collections.map.MultiKeyMap;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.talend.commons.utils.time.TimeMeasure;

/**
 * DOC amaumont class global comment. Detailled comment <br/>
 *
 * $Id$
 *
 */
@Ignore
public class PerformanceJavaTHash {

    private ArrayList<RowStruct> rowsLookup;

    private ArrayList<Integer> integers;

    private ArrayList<String> strings;

    private static final int TOTAL_ROWS = 2000000;

    // private static final int TOTAL_ROWS = 10;

    private static final int ITERATIONS_FOR_QUICK = TOTAL_ROWS - 1;

    // private static final int ITERATIONS_FOR_SLOW = 50000;
    private static final int ITERATIONS_FOR_SLOW = 10000;

    // private static final int ITERATIONS_FOR_SLOW = 50;

    private static final Object[] ARRAY_ONE_KEY = new Object[1];

    private static final Object[] ARRAY_TWO_KEYS = new Object[2];

    private static final Object[] ARRAY_THREE_KEYS = new Object[3];

    private static final Object[] ARRAY_FOUR_KEYS = new Object[4];

    /**
     * DOC amaumont Comment method "init".
     */
    @Before
    public void init() {

        System.out.println("\n########################################################\n"); //$NON-NLS-1$

        if (rowsLookup == null) {

            ArrayList<RowStruct> localRowsLookup = new ArrayList<RowStruct>();

            integers = new ArrayList<Integer>();
            strings = new ArrayList<String>();

            for (int i = 0; i < TOTAL_ROWS; i++) {
                localRowsLookup.add(new RowStruct(i, i, String.valueOf(i), String.valueOf(i), "line" + i)); //$NON-NLS-1$
                integers.add(new Integer(i));
                strings.add(String.valueOf(i));
            }
            rowsLookup = localRowsLookup;
        }

    }

    @Test
    public void testWithIntegerKeys() {

        // TimeMeasure.begin("testWithIntegerKeys LoadReadExecutorWithoutMultiKey");
        // LoadReadExecutorWithoutMultiKey executorWithout = new LoadReadExecutorWithoutMultiKey(ITERATIONS_FOR_QUICK);
        //
        // {
        // for (int i = 0; i < 10; i++) {
        // TimeMeasure.begin("executeWithIntegerAndMapAndMultiThreading with HashMap" + i);
        // executorWithout.executeWithIntegerAndMapAndMultiThreading(new HashMap(ITERATIONS_FOR_QUICK));
        // TimeMeasure.end("executeWithIntegerAndMapAndMultiThreading with HashMap" + i);
        // }
        //
        // TimeMeasure.begin("executeWithIntegerAndMapAndMultiThreading with HashedMap");
        // executorWithout.executeWithIntegerAndMapAndMultiThreading(new HashedMap(ITERATIONS_FOR_QUICK));
        // TimeMeasure.end("executeWithIntegerAndMapAndMultiThreading with HashedMap");
        //
        // TimeMeasure.begin("executeWithIntegerAndMapAndMultiThreading with FastHashMap");
        // executorWithout.executeWithIntegerAndMapAndMultiThreading(new FastHashMap(ITERATIONS_FOR_QUICK));
        // TimeMeasure.end("executeWithIntegerAndMapAndMultiThreading with FastHashMap");
        //
        // TimeMeasure.begin("executeWithIntegerAndMapAndMultiThreading with TreeMap");
        // executorWithout.executeWithIntegerAndMapAndMultiThreading(new TreeMap());
        // TimeMeasure.end("executeWithIntegerAndMapAndMultiThreading with TreeMap");
        //
        // System.out.println("executeWithIntegerAndFastHashMap");
        // executorWithout.executeWithIntegerAndFastHashMap();
        //
        // System.out.println("executeWithIntegerAndHashMap");
        // executorWithout.executeWithIntegerAndHashMap();
        //
        // System.out.println("executeWithIntegerAndHashedMap");
        // executorWithout.executeWithIntegerAndHashedMap();
        // }
        // TimeMeasure.end("testWithIntegerKeys LoadReadExecutorWithoutMultiKey");

        TimeMeasure.begin("testWithIntegerKeys LoadReadExecutorWithEqualsHashCode"); //$NON-NLS-1$
        LoadReadExecutorWithEqualsHashCode executorEqualsHashCode = new LoadReadExecutorWithEqualsHashCode(ITERATIONS_FOR_QUICK);

        {
            for (int i = 0; i < 5; i++) {
                System.out.println("executeWithIntegerAndHashMap " + i); //$NON-NLS-1$
                executorEqualsHashCode.executeWithIntegerAndHashMap();
            }

        }
        TimeMeasure.end("testWithIntegerKeys LoadReadExecutorWithEqualsHashCode"); //$NON-NLS-1$

        System.gc();

        TimeMeasure.begin("testWithIntegerKeys LoadReadExecutorWithEqualsHashCode TreeMap"); //$NON-NLS-1$
        LoadReadExecutorWithEqualsHashCodeTreeMap executorEqualsHashCodeTreeMap = new LoadReadExecutorWithEqualsHashCodeTreeMap(
                ITERATIONS_FOR_QUICK);

        {
            for (int i = 0; i < 5; i++) {
                System.out.println("executeWithIntegerAndTreeMap  " + i); //$NON-NLS-1$
                executorEqualsHashCodeTreeMap.executeWithIntegerAndTreeMap();
            }

        }
        TimeMeasure.end("testWithIntegerKeys LoadReadExecutorWithEqualsHashCode TreeMap"); //$NON-NLS-1$

        System.gc();

        TimeMeasure.begin("testWithIntegerKeys executeWithMultiKey"); //$NON-NLS-1$
        LoadReadExecutorWithMultiKey executor = new LoadReadExecutorWithMultiKey(ITERATIONS_FOR_QUICK) {

            @Override
            protected Object[] getKeysFromRow(RowStruct row) {
                return new Object[] { row.integerKey };
            }

            @Override
            protected Object[] getKeysFromIndex(int i) {
                return new Object[] { integers.get(i), };
            }

        };
        executeWithMultiKey(executor);

        TimeMeasure.end("testWithIntegerKeys executeWithMultiKey"); //$NON-NLS-1$

    }

    @Test
    public void testWithIntAndMultiKeys() {

        TimeMeasure.begin("testWithIntAndMultiKeys"); //$NON-NLS-1$
        LoadReadExecutorWithMultiKey executor = new LoadReadExecutorWithMultiKey(ITERATIONS_FOR_QUICK) {

            @Override
            protected Object[] getKeysFromRow(RowStruct row) {
                return new Object[] { row.intKey };
            }

            @Override
            protected Object[] getKeysFromIndex(int i) {
                return new Object[] { i, };
            }

        };
        executeWithMultiKey(executor);

        TimeMeasure.end("testWithIntAndMultiKeys"); //$NON-NLS-1$

    }

    @Test
    public void testWithIntKey() {

        TimeMeasure.begin("testWithIntKeys"); //$NON-NLS-1$
        LoadReadExecutorWithMultiKey executor = new LoadReadExecutorWithMultiKey(ITERATIONS_FOR_QUICK) {

            @Override
            protected Object[] getKeysFromRow(RowStruct row) {
                return new Object[] { row.intKey };
            }

            @Override
            protected Object[] getKeysFromIndex(int i) {
                return new Object[] { i, };
            }

        };
        executeWithMultiKey(executor);

        TimeMeasure.end("testWithIntKeys"); //$NON-NLS-1$

    }

    @Test
    public void testWithStringIntKeys() {

        TimeMeasure.begin("testWithStringIntKeys"); //$NON-NLS-1$
        LoadReadExecutorWithMultiKey executor = new LoadReadExecutorWithMultiKey(ITERATIONS_FOR_QUICK) {

            @Override
            protected Object[] getKeysFromRow(RowStruct row) {
                return new Object[] { row.stringKey, row.intKey, };
            }

            @Override
            protected Object[] getKeysFromIndex(int i) {
                return new Object[] { String.valueOf(i), i, };
            }

        };
        executeWithMultiKey(executor);

        TimeMeasure.end("testWithStringIntKeys"); //$NON-NLS-1$

    }

    @Test
    public void testWithIntStringKeys() {

        TimeMeasure.begin("testWithIntStringKeys"); //$NON-NLS-1$
        LoadReadExecutorWithMultiKey executor = new LoadReadExecutorWithMultiKey(ITERATIONS_FOR_QUICK) {

            @Override
            protected Object[] getKeysFromRow(RowStruct row) {
                return new Object[] { row.intKey, row.stringKey, };
            }

            @Override
            protected Object[] getKeysFromIndex(int i) {
                return new Object[] { i, String.valueOf(i), };
            }

        };
        executeWithMultiKey(executor);

        TimeMeasure.end("testWithIntStringKeys"); //$NON-NLS-1$

    }

    @Test
    public void testWithIntIntegerKeysAndDefinedArray() {

        TimeMeasure.begin("testWithIntIntegerKeysAndDefinedArray"); //$NON-NLS-1$
        LoadReadExecutorWithMultiKey executor = new LoadReadExecutorWithMultiKey(ITERATIONS_FOR_SLOW) {

            @Override
            protected Object[] getKeysFromRow(RowStruct row) {
                Object[] array = ARRAY_TWO_KEYS;
                array[0] = row.intKey;
                array[1] = row.integerKey;
                return array;
            }

            @Override
            protected Object[] getKeysFromIndex(int i) {
                Object[] array = ARRAY_TWO_KEYS;
                array[0] = i;
                array[1] = integers.get(i);
                return array;
            }

        };
        executeWithMultiKey(executor);

        TimeMeasure.end("testWithIntIntegerKeysAndDefinedArray"); //$NON-NLS-1$

    }

    @Test
    public void testWithIntIntegerKeys() {

        TimeMeasure.begin("testWithIntIntegerKeys"); //$NON-NLS-1$
        LoadReadExecutorWithMultiKey executor = new LoadReadExecutorWithMultiKey(ITERATIONS_FOR_SLOW) {

            @Override
            protected Object[] getKeysFromRow(RowStruct row) {
                return new Object[] { row.intKey, row.integerKey, };
            }

            @Override
            protected Object[] getKeysFromIndex(int i) {
                return new Object[] { i, integers.get(i), };
            }

        };
        executeWithMultiKey(executor);

        TimeMeasure.end("testWithIntIntegerKeys"); //$NON-NLS-1$

    }

    @Test
    public void testWithIntegerIntegerKeys() {

        TimeMeasure.begin("testWithIntegerIntegerKeys"); //$NON-NLS-1$
        LoadReadExecutorWithMultiKey executor = new LoadReadExecutorWithMultiKey(ITERATIONS_FOR_SLOW) {

            @Override
            protected Object[] getKeysFromRow(RowStruct row) {
                return new Object[] { row.integerKey, row.integerKey, };
            }

            @Override
            protected Object[] getKeysFromIndex(int i) {
                Integer integer = integers.get(i);
                return new Object[] { integer, integer, };
            }

        };
        executeWithMultiKey(executor);

        TimeMeasure.end("testWithIntegerIntegerKeys"); //$NON-NLS-1$

    }

    @Test
    public void testWithIntIntKeys() {

        TimeMeasure.begin("testWithIntIntKeys"); //$NON-NLS-1$
        LoadReadExecutorWithMultiKey executor = new LoadReadExecutorWithMultiKey(ITERATIONS_FOR_SLOW) {

            @Override
            protected Object[] getKeysFromRow(RowStruct row) {
                return new Object[] { row.intKey, row.intKey, };
            }

            @Override
            protected Object[] getKeysFromIndex(int i) {
                return new Object[] { i, i, };
            }

        };
        executeWithMultiKey(executor);

        TimeMeasure.end("testWithIntIntKeys"); //$NON-NLS-1$

    }

    @Test
    public void testWithIntegerIntKeys() {

        TimeMeasure.begin("testWithIntegerIntKeys"); //$NON-NLS-1$
        LoadReadExecutorWithMultiKey executor = new LoadReadExecutorWithMultiKey(ITERATIONS_FOR_SLOW) {

            @Override
            protected Object[] getKeysFromRow(RowStruct row) {
                return new Object[] { row.integerKey, row.intKey, };
            }

            @Override
            protected Object[] getKeysFromIndex(int i) {
                return new Object[] { new Integer(i), i, };
            }

        };
        executeWithMultiKey(executor);

        TimeMeasure.end("testWithIntegerIntKeys"); //$NON-NLS-1$

    }

    @Test
    public void testWithStringStringKeys() {

        TimeMeasure.begin("testWithStringStringKeys LoadReadExecutorWithEqualsHashCode"); //$NON-NLS-1$
        LoadReadExecutorWithEqualsHashCode executorEqualsHashCode = new LoadReadExecutorWithEqualsHashCode(ITERATIONS_FOR_QUICK);

        {
            for (int i = 0; i < 10; i++) {
                System.out.println("executeWithStringStringAndHashMap " + i); //$NON-NLS-1$
                executorEqualsHashCode.executeWithStringStringAndHashMap();
            }

        }
        TimeMeasure.end("testWithStringStringKeys LoadReadExecutorWithEqualsHashCode"); //$NON-NLS-1$

        // TimeMeasure.begin("testWithStringStringKeys executeWithMultiKey");
        // LoadReadExecutorWithMultiKey executor = new LoadReadExecutorWithMultiKey(ITERATIONS_FOR_QUICK) {
        //
        // @Override
        // protected Object[] getKeysFromRow(RowStruct row) {
        // return new Object[] { row.stringKey, row.stringKey2, };
        // }
        //
        // @Override
        // protected Object[] getKeysFromIndex(int i) {
        // String str = strings.get(i);
        //
        // return new Object[] { str, str, };
        // }
        //
        // };
        // executeWithMultiKey(executor);
        //
        // TimeMeasure.end("testWithStringStringKeys executeWithMultiKey");

        TimeMeasure.begin("testWithStringStringKeys executeWithoutMultiKey"); //$NON-NLS-1$
        LoadReadExecutorWithoutMultiKey executorWithout = new LoadReadExecutorWithoutMultiKey(ITERATIONS_FOR_QUICK);

        System.out.println("executeStringStringAndMultiKeyMap"); //$NON-NLS-1$
        executorWithout.executeStringStringAndMultiKeyMap();

        TimeMeasure.end("testWithStringStringKeys executeWithoutMultiKey"); //$NON-NLS-1$

    }

    // @Test
    // public void testPerformanceBetweenNewInstanceAndGetFromList() {
    //
    // // int iterations = 297850;
    // int iterations = 50000000;
    //
    // ArrayList<Integer> localIntegers = new ArrayList<Integer>();
    //
    // for (int i = 0; i < iterations; i++) {
    // localIntegers.add(new Integer(i));
    // }
    //
    // TimeMeasure.begin("testPerformanceBetweenNewInstanceAndGetFromList new instance");
    // for (int i = 0; i < iterations; i++) {
    // new Integer(i);
    // }
    // TimeMeasure.end("testPerformanceBetweenNewInstanceAndGetFromList new instance");
    //
    // TimeMeasure.begin("testPerformanceBetweenNewInstanceAndGetFromList get from list");
    // for (int i = 0; i < iterations; i++) {
    // localIntegers.get(i);
    // }
    // TimeMeasure.end("testPerformanceBetweenNewInstanceAndGetFromList get from list");
    //
    // }

    /**
     * DOC amaumont Comment method "execute".
     *
     * @param executor
     */
    private void executeWithMultiKey(LoadReadExecutorWithMultiKey executor) {

        System.out.println("\nWithMultiKeyAndHashMap :"); //$NON-NLS-1$
        executor.executeWithMultiKeyAndHashMap();

        System.out.println("\nWithMultiKey :"); //$NON-NLS-1$
        executor.executeWithMultiKey();

        // System.out.println("\nWith reuse :");
        // executor.executeWithReuse();

    }

    private void executeWithoutMultiKey(LoadReadExecutorWithoutMultiKey executor) {

    }

    /**
     * DOC amaumont Comment method "executeLoadReadTest".
     */
    private static Object[] getKeysArray(short value) {
        switch (value) {
        case 1:
            return ARRAY_ONE_KEY;
        case 2:
            return ARRAY_TWO_KEYS;
        case 3:
            return ARRAY_THREE_KEYS;
        case 4:
            return ARRAY_FOUR_KEYS;
        default:
            throw new IllegalArgumentException();
        }
    }

    /**
     *
     * DOC amaumont PerfsJavaHash class global comment. Detailled comment <br/>
     *
     * $Id$
     *
     */
    private abstract class LoadReadExecutorWithMultiKey {

        private int nIterations;

        /**
         * DOC amaumont LoadReadExecutor constructor comment.
         */
        public LoadReadExecutorWithMultiKey(int nIterations) {
            super();
            this.nIterations = nIterations;
        }

        // public void executeWithReuse() {
        // ReusableMultiKeyMap tHash = new ReusableMultiKeyMap();
        //
        // TimeMeasure.measureActive = true;
        //
        // int lstSize = nIterations;
        // TimeMeasure.begin("loading");
        // for (int i = 0; i < lstSize; i++) {
        // RowStruct row = (RowStruct) rowsLookup.get(i);
        //
        // ReusableMultiKey multiKey = new ReusableMultiKey(getKeysFromRow(row));
        //
        // tHash.put(multiKey, row);
        // }
        // TimeMeasure.end("loading");
        //
        // TimeMeasure.begin("reading");
        // int nRowsFound = 0;
        // ReusableMultiKey multiKey = new ReusableMultiKey();
        // for (int i = 0; i < lstSize; i++) {
        //
        // multiKey.setKeys(getKeysFromIndex(i));
        //
        // RowStruct row = (RowStruct) tHash.get(multiKey);
        // if (row != null) {
        // nRowsFound++;
        // }
        // // System.out.println(row.name);
        // }
        // TimeMeasure.end("reading");
        // tHash.clear();
        //
        // System.out.println("nRowsFound=" + nRowsFound);
        //
        // }

        public void executeWithMultiKey() {
            MultiKeyMap tHash = new MultiKeyMap();

            TimeMeasure.measureActive = true;

            int lstSize = nIterations;
            TimeMeasure.begin("loading"); //$NON-NLS-1$
            for (int i = 0; i < lstSize; i++) {
                RowStruct row = (RowStruct) rowsLookup.get(i);

                MultiKey multiKey = new MultiKey(getKeysFromRow(row));

                tHash.put(multiKey, row);

            }
            TimeMeasure.end("loading"); //$NON-NLS-1$

            TimeMeasure.begin("reading"); //$NON-NLS-1$
            int nRowsFound = 0;
            for (int i = 0; i < lstSize; i++) {

                MultiKey multiKey = new MultiKey(getKeysFromIndex(i));

                RowStruct row = (RowStruct) tHash.get(multiKey);
                if (row != null) {
                    nRowsFound++;
                }
                // System.out.println(row.name);
            }
            TimeMeasure.end("reading"); //$NON-NLS-1$
            tHash.clear();

            System.out.println("nRowsFound=" + nRowsFound); //$NON-NLS-1$

        }

        public void executeWithMultiKeyAndHashMap() {
            Map<MultiKey, RowStruct> tHash = new HashMap<MultiKey, RowStruct>();

            TimeMeasure.measureActive = true;

            int lstSize = nIterations;
            TimeMeasure.begin("loading"); //$NON-NLS-1$
            for (int i = 0; i < lstSize; i++) {
                RowStruct row = (RowStruct) rowsLookup.get(i);

                MultiKey multiKey = new MultiKey(getKeysFromRow(row));

                tHash.put(multiKey, row);

            }
            TimeMeasure.end("loading"); //$NON-NLS-1$

            TimeMeasure.begin("reading"); //$NON-NLS-1$
            int nRowsFound = 0;
            for (int i = 0; i < lstSize; i++) {

                MultiKey multiKey = new MultiKey(getKeysFromIndex(i));

                RowStruct row = (RowStruct) tHash.get(multiKey);
                if (row != null) {
                    nRowsFound++;
                }
                // System.out.println(row.name);
            }
            TimeMeasure.end("reading"); //$NON-NLS-1$
            tHash.clear();

            System.out.println("nRowsFound=" + nRowsFound); //$NON-NLS-1$

        }

        public void executeWithRemove() {
            MultiKeyMap tHash = new MultiKeyMap();

            TimeMeasure.measureActive = true;

            int lstSize = nIterations;
            ArrayList<RowStruct> localRowsLookup = rowsLookup;
            TimeMeasure.begin("loading"); //$NON-NLS-1$
            for (int i = 0; i < lstSize; i++) {
                RowStruct row = localRowsLookup.get(i);

                tHash.put(row.stringKey, row.stringKey, row);

            }
            TimeMeasure.end("loading"); //$NON-NLS-1$

            TimeMeasure.begin("reading"); //$NON-NLS-1$
            int nRowsFound = 0;
            for (int i = 0; i < lstSize; i++) {

                String str = String.valueOf(i);

                RowStruct row = (RowStruct) tHash.remove(str, str);
                if (row != null) {
                    nRowsFound++;
                }
                // System.out.println(row.name);
            }
            TimeMeasure.end("reading"); //$NON-NLS-1$
            tHash.clear();

            System.out.println("nRowsFound=" + nRowsFound); //$NON-NLS-1$

        }

        public void executeWithLocalRowsForeach() {
            MultiKeyMap tHash = new MultiKeyMap();

            TimeMeasure.measureActive = true;

            int lstSize = nIterations;
            ArrayList<RowStruct> localRowsLookup = rowsLookup;
            TimeMeasure.begin("executeWithoutMultiKeyWithLocalRowsForeach loading"); //$NON-NLS-1$
            for (RowStruct row : localRowsLookup) {
                tHash.put(row.stringKey, row.stringKey, row);
                if (row.intKey == 10000) {
                    break;
                }
            }
            TimeMeasure.end("executeWithoutMultiKeyWithLocalRowsForeach loading"); //$NON-NLS-1$

            TimeMeasure.begin("reading"); //$NON-NLS-1$
            int nRowsFound = 0;
            for (int i = 0; i < lstSize; i++) {

                String str = String.valueOf(i);

                RowStruct row = (RowStruct) tHash.get(str, str);
                if (row != null) {
                    nRowsFound++;
                }
                // System.out.println(row.name);
            }
            TimeMeasure.end("reading"); //$NON-NLS-1$
            tHash.clear();

            System.out.println("nRowsFound=" + nRowsFound); //$NON-NLS-1$

        }

        /**
         * DOC amaumont Comment method "getKeysFromIndex".
         *
         * @param index
         * @return
         */
        protected abstract Object[] getKeysFromIndex(int index);

        /**
         * DOC amaumont Comment method "getKeysFromRow".
         *
         * @param row
         * @return
         */
        protected abstract Object[] getKeysFromRow(RowStruct row);

    }

    /**
     *
     * DOC amaumont PerformanceJavaTHash class global comment. Detailled comment <br/>
     *
     * $Id$
     *
     */
    private class LoadReadExecutorWithEqualsHashCode {

        private int nIterations;

        /**
         * DOC amaumont LoadReadExecutor constructor comment.
         */
        public LoadReadExecutorWithEqualsHashCode(int nIterations) {
            super();
            this.nIterations = nIterations;
        }

        /**
         * DOC amaumont Comment method "executeWithStringStringAndHashMap".
         */
        public void executeWithStringStringAndHashMap() {
            int lstSize = nIterations;
            Map<RowStruct, RowStruct> tHash = new HashMap<RowStruct, RowStruct>(lstSize);

            TimeMeasure.measureActive = true;

            ArrayList<RowStruct> localRowsLookup = rowsLookup;

            TimeMeasure.begin("loading"); //$NON-NLS-1$
            for (int i = 0; i < lstSize; i++) {
                RowStruct row = (RowStruct) localRowsLookup.get(i);

                if (RowStruct.integerKeyIsKey || RowStruct.intKeyIsKey || !RowStruct.stringKeyIsKey || !RowStruct.stringKey2IsKey) {
                    RowStruct.integerKeyIsKey = false;
                    RowStruct.intKeyIsKey = false;
                    RowStruct.stringKeyIsKey = true;
                    RowStruct.stringKey2IsKey = true;
                }
                tHash.put(row, row);
                // TimeMeasure.step("loading", "end of loop block");
            }
            TimeMeasure.end("loading"); //$NON-NLS-1$

            TimeMeasure.begin("reading"); //$NON-NLS-1$
            int nRowsFound = 0;

            RowStruct rowStructKey = new RowStruct();

            ArrayList<Integer> localIntegers = integers;
            ArrayList<String> localStrings = strings;

            for (int i = 0; i < lstSize; i++) {

                if (RowStruct.integerKeyIsKey || RowStruct.intKeyIsKey || !RowStruct.stringKeyIsKey || !RowStruct.stringKey2IsKey) {
                    RowStruct.integerKeyIsKey = false;
                    RowStruct.intKeyIsKey = false;
                    RowStruct.stringKeyIsKey = true;
                    RowStruct.stringKey2IsKey = true;
                }

                rowStructKey.hashCodeDirty = true;
                rowStructKey.stringKey = localStrings.get(i);
                rowStructKey.stringKey2 = rowStructKey.stringKey;
                // rowStructKey.intKey = i;
                // rowStructKey.stringKey = localStrings.get(i);
                RowStruct row = (RowStruct) tHash.get(rowStructKey);
                if (row != null) {
                    nRowsFound++;
                    // System.out.println(row.name);
                }
            }
            TimeMeasure.end("reading"); //$NON-NLS-1$
            tHash.clear();

            System.out.println("nRowsFound=" + nRowsFound); //$NON-NLS-1$

        }

        public void executeWithIntegerAndHashMap() {
            int lstSize = nIterations;
            Map<RowStruct, RowStruct> tHash = new HashMap<RowStruct, RowStruct>(lstSize);

            TimeMeasure.measureActive = true;

            ArrayList<RowStruct> localRowsLookup = rowsLookup;

            RowStruct.integerKeyIsKey = true;
            RowStruct.intKeyIsKey = false;
            RowStruct.stringKeyIsKey = false;
            RowStruct.stringKey2IsKey = false;

            TimeMeasure.begin("loading"); //$NON-NLS-1$
            for (int i = 0; i < lstSize; i++) {
                RowStruct row = (RowStruct) localRowsLookup.get(i);

                tHash.put(row, row);
                // TimeMeasure.step("loading", "end of loop block");
            }
            TimeMeasure.end("loading"); //$NON-NLS-1$

            TimeMeasure.begin("reading"); //$NON-NLS-1$
            int nRowsFound = 0;

            RowStruct rowStructKey = new RowStruct();

            ArrayList<Integer> localIntegers = integers;
            ArrayList<String> localStrings = strings;

            RowStruct.integerKeyIsKey = true;
            RowStruct.intKeyIsKey = false;
            RowStruct.stringKeyIsKey = false;
            RowStruct.stringKey2IsKey = false;

            for (int i = 0; i < lstSize; i++) {

                rowStructKey.hashCodeDirty = true;
                rowStructKey.integerKey = localIntegers.get(i);
                // rowStructKey.intKey = i;
                // rowStructKey.stringKey = localStrings.get(i);
                RowStruct row = (RowStruct) tHash.get(rowStructKey);
                if (row != null) {
                    nRowsFound++;
                    // System.out.println(row.name);
                }
            }
            TimeMeasure.end("reading"); //$NON-NLS-1$
            tHash.clear();

            System.out.println("nRowsFound=" + nRowsFound); //$NON-NLS-1$

        }

        public void executeWithIntegerAndMapAndMultiThreading(Map tHash) {

            /**
             *
             * DOC amaumont PerformanceJavaTHash.LoadReadExecutorWithoutMultiKey class global comment. Detailled comment <br/>
             *
             * $Id$
             *
             */
            class PutThread extends Thread {

                private int length;

                private int start;

                private ArrayList rows;

                private Map map;

                private boolean ended;

                /**
                 * DOC amaumont PutThread constructor comment.
                 *
                 * @param localRowsLookup
                 * @param i
                 * @param j
                 */
                public PutThread(Map map, ArrayList rows, int start, int length) {
                    this.map = map;
                    this.rows = rows;
                    this.start = start;
                    this.length = length;
                }

                /*
                 * (non-Javadoc)
                 *
                 * @see java.lang.Thread#run()
                 */
                @Override
                public void run() {

                    ArrayList localRows = this.rows;
                    int start = this.start;
                    int length = this.length;
                    Map map = this.map;

                    for (int i = start; i < length; i++) {
                        RowStruct row = (RowStruct) localRows.get(i);
                        map.put(row.integerKey, row);
                    }
                }

            }
            ;

            ArrayList<RowStruct> localRowsLookup = rowsLookup;
            ArrayList<Integer> localIntegers = integers;

            int lstSize = nIterations;
            PutThread putThread1 = new PutThread(tHash, localRowsLookup, 0, nIterations / 2);
            PutThread putThread2 = new PutThread(tHash, localRowsLookup, nIterations / 2, nIterations);
            TimeMeasure.begin("loading"); //$NON-NLS-1$

            putThread1.start();
            putThread2.start();

            while (putThread1.isAlive() || putThread2.isAlive())
                ;

            TimeMeasure.end("loading"); //$NON-NLS-1$

            TimeMeasure.begin("reading"); //$NON-NLS-1$
            int nRowsFound = 0;
            for (int i = 0; i < lstSize; i++) {

                RowStruct row = (RowStruct) tHash.get(localIntegers.get(i));
                if (row != null) {
                    nRowsFound++;
                }
                // System.out.println(row.name);
            }
            TimeMeasure.end("reading"); //$NON-NLS-1$
            tHash.clear();

            System.out.println("nRowsFound=" + nRowsFound); //$NON-NLS-1$

        }

    }

    /**
     *
     * DOC amaumont PerformanceJavaTHash class global comment. Detailled comment <br/>
     *
     * $Id$
     *
     */
    private class LoadReadExecutorWithEqualsHashCodeTreeMap {

        private int nIterations;

        /**
         * DOC amaumont LoadReadExecutor constructor comment.
         */
        public LoadReadExecutorWithEqualsHashCodeTreeMap(int nIterations) {
            super();
            this.nIterations = nIterations;
        }

        /**
         * DOC amaumont Comment method "executeWithStringStringAndHashMap".
         */
        public void executeWithStringStringAndTreeMap() {
            int lstSize = nIterations;
            Map<RowStruct, RowStruct> tHash = new TreeMap<RowStruct, RowStruct>();

            TimeMeasure.measureActive = true;

            ArrayList<RowStruct> localRowsLookup = rowsLookup;

            TimeMeasure.begin("loading"); //$NON-NLS-1$
            for (int i = 0; i < lstSize; i++) {
                RowStruct row = (RowStruct) localRowsLookup.get(i);

                if (RowStruct.integerKeyIsKey || RowStruct.intKeyIsKey || !RowStruct.stringKeyIsKey || !RowStruct.stringKey2IsKey) {
                    RowStruct.integerKeyIsKey = false;
                    RowStruct.intKeyIsKey = false;
                    RowStruct.stringKeyIsKey = true;
                    RowStruct.stringKey2IsKey = true;
                }
                tHash.put(row, row);
                // TimeMeasure.step("loading", "end of loop block");
            }
            TimeMeasure.end("loading"); //$NON-NLS-1$

            TimeMeasure.begin("reading"); //$NON-NLS-1$
            int nRowsFound = 0;

            RowStruct rowStructKey = new RowStruct();

            ArrayList<Integer> localIntegers = integers;
            ArrayList<String> localStrings = strings;

            for (int i = 0; i < lstSize; i++) {

                if (RowStruct.integerKeyIsKey || RowStruct.intKeyIsKey || !RowStruct.stringKeyIsKey || !RowStruct.stringKey2IsKey) {
                    RowStruct.integerKeyIsKey = false;
                    RowStruct.intKeyIsKey = false;
                    RowStruct.stringKeyIsKey = true;
                    RowStruct.stringKey2IsKey = true;
                }

                rowStructKey.hashCodeDirty = true;
                rowStructKey.stringKey = localStrings.get(i);
                rowStructKey.stringKey2 = rowStructKey.stringKey;
                // rowStructKey.intKey = i;
                // rowStructKey.stringKey = localStrings.get(i);
                RowStruct row = (RowStruct) tHash.get(rowStructKey);
                if (row != null) {
                    nRowsFound++;
                    // System.out.println(row.name);
                }
            }
            TimeMeasure.end("reading"); //$NON-NLS-1$
            tHash.clear();

            System.out.println("nRowsFound=" + nRowsFound); //$NON-NLS-1$

        }

        public void executeWithIntegerAndTreeMap() {
            int lstSize = nIterations;
            Map<RowStruct, RowStruct> tHash = new TreeMap<RowStruct, RowStruct>();

            TimeMeasure.measureActive = true;

            ArrayList<RowStruct> localRowsLookup = rowsLookup;

            if (!RowStruct.integerKeyIsKey || RowStruct.intKeyIsKey || RowStruct.stringKeyIsKey || RowStruct.stringKey2IsKey) {
                RowStruct.integerKeyIsKey = true;
                RowStruct.intKeyIsKey = false;
                RowStruct.stringKeyIsKey = false;
                RowStruct.stringKey2IsKey = false;
            }

            TimeMeasure.begin("loading"); //$NON-NLS-1$
            for (int i = 0; i < lstSize; i++) {
                RowStruct row = (RowStruct) localRowsLookup.get(i);

                tHash.put(row, row);
                // TimeMeasure.step("loading", "end of loop block");
            }
            TimeMeasure.end("loading"); //$NON-NLS-1$

            TimeMeasure.begin("reading"); //$NON-NLS-1$
            int nRowsFound = 0;

            RowStruct rowStructKey = new RowStruct();

            ArrayList<Integer> localIntegers = integers;
            ArrayList<String> localStrings = strings;

            for (int i = 0; i < lstSize; i++) {

                if (!RowStruct.integerKeyIsKey || RowStruct.intKeyIsKey || RowStruct.stringKeyIsKey || RowStruct.stringKey2IsKey) {
                    RowStruct.integerKeyIsKey = true;
                    RowStruct.intKeyIsKey = false;
                    RowStruct.stringKeyIsKey = false;
                    RowStruct.stringKey2IsKey = false;
                }

                rowStructKey.hashCodeDirty = true;
                rowStructKey.integerKey = localIntegers.get(i);
                // rowStructKey.intKey = i;
                // rowStructKey.stringKey = localStrings.get(i);
                RowStruct row = (RowStruct) tHash.get(rowStructKey);
                if (row != null) {
                    nRowsFound++;
                    // System.out.println(row.name);
                }
            }
            TimeMeasure.end("reading"); //$NON-NLS-1$
            tHash.clear();

            System.out.println("nRowsFound=" + nRowsFound); //$NON-NLS-1$

        }

        public void executeWithIntegerAndMapAndMultiThreading(Map tHash) {

            /**
             *
             * DOC amaumont PerformanceJavaTHash.LoadReadExecutorWithoutMultiKey class global comment. Detailled comment <br/>
             *
             * $Id$
             *
             */
            class PutThread extends Thread {

                private int length;

                private int start;

                private ArrayList rows;

                private Map map;

                private boolean ended;

                /**
                 * DOC amaumont PutThread constructor comment.
                 *
                 * @param localRowsLookup
                 * @param i
                 * @param j
                 */
                public PutThread(Map map, ArrayList rows, int start, int length) {
                    this.map = map;
                    this.rows = rows;
                    this.start = start;
                    this.length = length;
                }

                /*
                 * (non-Javadoc)
                 *
                 * @see java.lang.Thread#run()
                 */
                @Override
                public void run() {

                    ArrayList localRows = this.rows;
                    int start = this.start;
                    int length = this.length;
                    Map map = this.map;

                    for (int i = start; i < length; i++) {
                        RowStruct row = (RowStruct) localRows.get(i);
                        map.put(row.integerKey, row);
                    }
                }

            }
            ;

            ArrayList<RowStruct> localRowsLookup = rowsLookup;
            ArrayList<Integer> localIntegers = integers;

            int lstSize = nIterations;
            PutThread putThread1 = new PutThread(tHash, localRowsLookup, 0, nIterations / 2);
            PutThread putThread2 = new PutThread(tHash, localRowsLookup, nIterations / 2, nIterations);
            TimeMeasure.begin("loading"); //$NON-NLS-1$

            putThread1.start();
            putThread2.start();

            while (putThread1.isAlive() || putThread2.isAlive())
                ;

            TimeMeasure.end("loading"); //$NON-NLS-1$

            TimeMeasure.begin("reading"); //$NON-NLS-1$
            int nRowsFound = 0;
            for (int i = 0; i < lstSize; i++) {

                RowStruct row = (RowStruct) tHash.get(localIntegers.get(i));
                if (row != null) {
                    nRowsFound++;
                }
                // System.out.println(row.name);
            }
            TimeMeasure.end("reading"); //$NON-NLS-1$
            tHash.clear();

            System.out.println("nRowsFound=" + nRowsFound); //$NON-NLS-1$

        }

    }

    /**
     *
     * DOC amaumont PerformanceJavaTHash class global comment. Detailled comment <br/>
     *
     * $Id$
     *
     */
    private class LoadReadExecutorWithoutMultiKey {

        private int nIterations;

        /**
         * DOC amaumont LoadReadExecutor constructor comment.
         */
        public LoadReadExecutorWithoutMultiKey(int nIterations) {
            super();
            this.nIterations = nIterations;
        }

        public void executeWithIntAndHashMap() {
            Map<Integer, RowStruct> tHash = new HashMap<Integer, RowStruct>();

            TimeMeasure.measureActive = true;

            int lstSize = nIterations;
            ArrayList<RowStruct> localRowsLookup = rowsLookup;

            TimeMeasure.begin("loading"); //$NON-NLS-1$
            for (int i = 0; i < lstSize; i++) {
                RowStruct row = (RowStruct) localRowsLookup.get(i);

                tHash.put(i, row);

            }
            TimeMeasure.end("loading"); //$NON-NLS-1$

            TimeMeasure.begin("reading"); //$NON-NLS-1$
            int nRowsFound = 0;
            for (int i = 0; i < lstSize; i++) {

                RowStruct row = (RowStruct) tHash.get(i);
                if (row != null) {
                    nRowsFound++;
                }
                // System.out.println(row.name);
            }
            TimeMeasure.end("reading"); //$NON-NLS-1$
            tHash.clear();

            System.out.println("nRowsFound=" + nRowsFound); //$NON-NLS-1$

        }

        public void executeWithIntegerAndHashMap() {
            int lstSize = nIterations;
            Map<Integer, RowStruct> tHash = new HashMap<Integer, RowStruct>(lstSize);

            TimeMeasure.measureActive = true;

            ArrayList<RowStruct> localRowsLookup = rowsLookup;
            ArrayList<Integer> localIntegers = integers;

            TimeMeasure.begin("loading"); //$NON-NLS-1$
            for (int i = 0; i < lstSize; i++) {
                RowStruct row = (RowStruct) localRowsLookup.get(i);
                tHash.put(row.integerKey, row);

            }
            TimeMeasure.end("loading"); //$NON-NLS-1$

            TimeMeasure.begin("reading"); //$NON-NLS-1$
            int nRowsFound = 0;
            for (int i = 0; i < lstSize; i++) {

                RowStruct row = (RowStruct) tHash.get(localIntegers.get(i));
                if (row != null) {
                    nRowsFound++;
                }
                // System.out.println(row.name);
            }
            TimeMeasure.end("reading"); //$NON-NLS-1$
            tHash.clear();

            System.out.println("nRowsFound=" + nRowsFound); //$NON-NLS-1$

        }

        public void executeWithIntegerAndHashedMap() {
            int lstSize = nIterations;
            Map tHash = new HashedMap(lstSize);

            TimeMeasure.measureActive = true;

            ArrayList<RowStruct> localRowsLookup = rowsLookup;
            ArrayList<Integer> localIntegers = integers;

            TimeMeasure.begin("loading"); //$NON-NLS-1$
            for (int i = 0; i < lstSize; i++) {
                RowStruct row = (RowStruct) localRowsLookup.get(i);
                tHash.put(row.integerKey, row);

            }
            TimeMeasure.end("loading"); //$NON-NLS-1$

            TimeMeasure.begin("reading"); //$NON-NLS-1$
            int nRowsFound = 0;
            for (int i = 0; i < lstSize; i++) {

                RowStruct row = (RowStruct) tHash.get(localIntegers.get(i));
                if (row != null) {
                    nRowsFound++;
                }
                // System.out.println(row.name);
            }
            TimeMeasure.end("reading"); //$NON-NLS-1$
            tHash.clear();

            System.out.println("nRowsFound=" + nRowsFound); //$NON-NLS-1$

        }

        public void executeWithIntegerAndFastHashMap() {
            int lstSize = nIterations;
            Map tHash = new FastHashMap(lstSize);

            TimeMeasure.measureActive = true;

            ArrayList<RowStruct> localRowsLookup = rowsLookup;
            ArrayList<Integer> localIntegers = integers;

            TimeMeasure.begin("loading"); //$NON-NLS-1$
            for (int i = 0; i < lstSize; i++) {
                RowStruct row = (RowStruct) localRowsLookup.get(i);
                tHash.put(row.integerKey, row);

            }
            TimeMeasure.end("loading"); //$NON-NLS-1$

            TimeMeasure.begin("reading"); //$NON-NLS-1$
            int nRowsFound = 0;
            for (int i = 0; i < lstSize; i++) {

                RowStruct row = (RowStruct) tHash.get(localIntegers.get(i));
                if (row != null) {
                    nRowsFound++;
                }
                // System.out.println(row.name);
            }
            TimeMeasure.end("reading"); //$NON-NLS-1$
            tHash.clear();

            System.out.println("nRowsFound=" + nRowsFound); //$NON-NLS-1$

        }

        public void executeWithIntegerAndMapAndMultiThreading(Map tHash) {

            /**
             *
             * DOC amaumont PerformanceJavaTHash.LoadReadExecutorWithoutMultiKey class global comment. Detailled comment <br/>
             *
             * $Id$
             *
             */
            class PutThread extends Thread {

                private int length;

                private int start;

                private ArrayList rows;

                private Map map;

                private boolean ended;

                /**
                 * DOC amaumont PutThread constructor comment.
                 *
                 * @param localRowsLookup
                 * @param i
                 * @param j
                 */
                public PutThread(Map map, ArrayList rows, int start, int length) {
                    this.map = map;
                    this.rows = rows;
                    this.start = start;
                    this.length = length;
                }

                /*
                 * (non-Javadoc)
                 *
                 * @see java.lang.Thread#run()
                 */
                @Override
                public void run() {

                    ArrayList localRows = this.rows;
                    int start = this.start;
                    int length = this.length;
                    Map map = this.map;

                    for (int i = start; i < length; i++) {
                        RowStruct row = (RowStruct) localRows.get(i);
                        map.put(row.integerKey, row);
                    }
                }

            }
            ;

            ArrayList<RowStruct> localRowsLookup = rowsLookup;
            ArrayList<Integer> localIntegers = integers;

            int lstSize = nIterations;
            PutThread putThread1 = new PutThread(tHash, localRowsLookup, 0, nIterations / 2);
            PutThread putThread2 = new PutThread(tHash, localRowsLookup, nIterations / 2, nIterations);
            TimeMeasure.begin("loading"); //$NON-NLS-1$

            putThread1.start();
            putThread2.start();

            while (putThread1.isAlive() || putThread2.isAlive())
                ;

            TimeMeasure.end("loading"); //$NON-NLS-1$

            TimeMeasure.begin("reading"); //$NON-NLS-1$
            int nRowsFound = 0;
            for (int i = 0; i < lstSize; i++) {

                RowStruct row = (RowStruct) tHash.get(localIntegers.get(i));
                if (row != null) {
                    nRowsFound++;
                }
                // System.out.println(row.name);
            }
            TimeMeasure.end("reading"); //$NON-NLS-1$
            tHash.clear();

            System.out.println("nRowsFound=" + nRowsFound); //$NON-NLS-1$

        }

        public void executeStringStringAndMultiKeyMap() {
            MultiKeyMap tHash = new MultiKeyMap();

            TimeMeasure.measureActive = true;

            int lstSize = nIterations;
            ArrayList<RowStruct> localRowsLookup = rowsLookup;
            TimeMeasure.begin("loading"); //$NON-NLS-1$
            for (int i = 0; i < lstSize; i++) {
                RowStruct row = localRowsLookup.get(i);
                tHash.put(row.stringKey, row.stringKey, row);
            }
            TimeMeasure.end("loading"); //$NON-NLS-1$

            ArrayList<String> localStrings = strings;

            TimeMeasure.begin("reading"); //$NON-NLS-1$
            int nRowsFound = 0;
            for (int i = 0; i < lstSize; i++) {

                // String str = String.valueOf(i);
                String str = localStrings.get(i);

                RowStruct row = (RowStruct) tHash.get(str, str);
                if (row != null) {
                    nRowsFound++;
                }
                // System.out.println(row.name);
            }
            TimeMeasure.end("reading"); //$NON-NLS-1$
            tHash.clear();

            System.out.println("nRowsFound=" + nRowsFound); //$NON-NLS-1$

        }

        public void executeWithLocalRowsInt() {
            MultiKeyMap tHash = new MultiKeyMap();

            TimeMeasure.measureActive = true;

            int lstSize = nIterations;
            ArrayList<RowStruct> localRowsLookup = rowsLookup;
            TimeMeasure.begin("loading"); //$NON-NLS-1$
            for (int i = 0; i < lstSize; i++) {
                RowStruct row = localRowsLookup.get(i);

                tHash.put(row.intKey, row);

            }
            TimeMeasure.end("loading"); //$NON-NLS-1$

            TimeMeasure.begin("reading"); //$NON-NLS-1$
            int nRowsFound = 0;
            for (int i = 0; i < lstSize; i++) {

                String str = String.valueOf(i);

                RowStruct row = (RowStruct) tHash.get(i);
                if (row != null) {
                    nRowsFound++;
                }
                // System.out.println(row.name);
            }
            TimeMeasure.end("reading"); //$NON-NLS-1$
            tHash.clear();

            System.out.println("nRowsFound=" + nRowsFound); //$NON-NLS-1$

        }

    }

    /**
     *
     * DOC amaumont PerformanceJavaTHash class global comment. Detailled comment <br/>
     *
     * $Id$
     *
     */
    public static class RowStruct implements Comparable {

        private static final int DEFAULT_HASHCODE = 1;

        private static final int PRIME = 31;

        public static boolean intKeyIsKey;

        public static boolean integerKeyIsKey;

        public static boolean stringKeyIsKey;

        public static boolean stringKey2IsKey;

        public static boolean nameIsKey;

        public static boolean ignoreCase = false;

        public static int ascendingInt = 1;

        private int hashCode = DEFAULT_HASHCODE;

        public boolean hashCodeDirty = true;

        public int intKey;

        public Integer integerKey;

        public String stringKey;

        public String stringKey2;

        public String name;

        /**
         * DOC amaumont BeanStruct constructor comment.
         *
         * @param id1
         * @param name
         * @param id2
         */
        public RowStruct(int intKey, Integer integerKey, String stringKey, String stringKey2, String name) {
            super();
            this.intKey = intKey;
            this.integerKey = integerKey;
            this.stringKey = stringKey;
            this.stringKey2 = stringKey2;
            this.name = name;
        }

        public RowStruct() {
            super();
        }

        /*
         * (non-Javadoc)
         *
         * @see java.lang.Object#hashCode()
         */
        @Override
        public int hashCode() {
            if (this.hashCodeDirty) {
                final int prime = PRIME;
                int result = 0;
                if (intKeyIsKey) {
                    result = prime * result + this.intKey;
                }
                if (integerKeyIsKey) {
                    result = prime * result + ((this.integerKey == null) ? 0 : this.integerKey.hashCode());
                }
                if (stringKeyIsKey) {
                    result = prime * result + ((this.stringKey == null) ? 0 : this.stringKey.hashCode());
                }
                if (stringKey2IsKey) {
                    result = prime * result + ((this.stringKey2 == null) ? 0 : this.stringKey2.hashCode());
                }
                this.hashCode = result;
                this.hashCodeDirty = false;
            }

            return this.hashCode;
        }

        /*
         * (non-Javadoc)
         *
         * @see java.lang.Object#equals(java.lang.Object)
         */
        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            final RowStruct other = (RowStruct) obj;
            if (intKeyIsKey && this.intKey != other.intKey)
                return false;
            if (integerKeyIsKey) {
                if (this.integerKey == null) {
                    if (other.integerKey != null)
                        return false;
                } else if (!this.integerKey.equals(other.integerKey))
                    return false;
            }
            if (stringKeyIsKey) {
                if (this.stringKey == null) {
                    if (other.stringKey != null)
                        return false;
                } else if (!this.stringKey.equals(other.stringKey))
                    return false;
            }
            if (stringKey2IsKey) {
                if (this.stringKey2 == null) {
                    if (other.stringKey2 != null)
                        return false;
                } else if (!this.stringKey2.equals(other.stringKey2))
                    return false;
            }
            return true;
        }

        private int checkNullsAndCompare(Object object1, Object object2) {
            int returnValue = 0;
            if (object1 instanceof String && object2 instanceof String) {
                returnValue = compareStrings((String) object1, (String) object2);
            } else if (object1 instanceof Comparable && object2 instanceof Comparable) {
                returnValue = ((Comparable) object1).compareTo(object2);
            } else if (object1 != null && object2 != null) {
                // si les objets sont diff�rents de null on les compare avec le
                // toString
                returnValue = compareStrings(object1.toString(), object2.toString());
            } else if (object1 == null && object2 != null) {
                returnValue = 1; // un des deux objet est null, on inverse le tri
            } else if (object1 != null && object2 == null) {
                returnValue = -1; // un des deux objet est null
            } else {
                // les deux objet sont null
                returnValue = 0;
            }

            return ascendingInt * returnValue;
        }

        private int compareStrings(String string1, String string2) {
            if (ignoreCase) {
                return string1.compareToIgnoreCase(string2);
            } else {
                return string1.compareTo(string2);
            }
        }

        /*
         * (non-Javadoc)
         *
         * @see java.lang.Comparable#compareTo(java.lang.Object)
         */
        public int compareTo(Object o) {
            return checkNullsAndCompare(this.integerKey, ((RowStruct) o).integerKey);
        }

    }

}
