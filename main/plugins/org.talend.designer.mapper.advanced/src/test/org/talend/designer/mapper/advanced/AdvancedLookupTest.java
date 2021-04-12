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
package org.talend.designer.mapper.advanced;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.talend.commons.utils.time.TimeMeasure;
import org.talend.designer.components.commons.AdvancedLookup;
import org.talend.designer.components.commons.AdvancedLookup.MATCHING_MODE;

/**
 * DOC amaumont class global comment. Detailled comment <br/>
 *

 */
public class AdvancedLookupTest {

    private ArrayList<RowStruct3> rowsLookup;

    private ArrayList<Integer> integers;

    private ArrayList<String> strings;

     private static final int TOTAL_ROWS = 2000000;
//    private static final int TOTAL_ROWS = 30;

    // private static final int TOTAL_ROWS = 20000;

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

    }

    /**
     * Test method for {@link org.talend.designer.components.commons.AdvancedLookup#get(java.lang.Object)}.
     */
    @Test
    public void testGetPut() {
        AdvancedLookup<RowStruct3> lookup = AdvancedLookup.<RowStruct3>getLookup(MATCHING_MODE.ALL_MATCHES);

        RowStruct3.startRangeIsKey = true;
        RowStruct3.endRangeIsKey = true;

        for (int i = 0; i < 10; i++) {
            lookup.put(new RowStruct3(i, i + 4, String.valueOf(i), String.valueOf(i), "name A" + String.valueOf(i)));
            lookup.put(new RowStruct3(i, i + 4, String.valueOf(i), String.valueOf(i), "name B" + String.valueOf(i)));
            lookup.put(new RowStruct3(i + 10, i + 10, String.valueOf(i + 10), String.valueOf(i + 10), "name C"
                    + String.valueOf(i + 10)));
        }

        lookup.get(new RowStruct3(1, 1 + 4, null, null, null));
        if (lookup.resultIsList()) {
            System.out.println("List: " + lookup.getResultList());
        } else if (lookup.resultIsObject()) {
            System.out.println("Object: " + lookup.getResultObject());

        }

        lookup.get(new RowStruct3(11, 11, null, null, null));
        if (lookup.resultIsList()) {
            System.out.println("List: " + lookup.getResultList());
        } else if (lookup.resultIsObject()) {
            System.out.println("Object: " + lookup.getResultObject());
        }

    }

    /**
     * Test method for
     * {@link org.talend.designer.components.commons.AdvancedLookup#put(java.lang.Object, java.lang.Object)}.
     */
    @Test
    public void testPerfomance() {

        System.out.println("\n########################################################\n");

        if (rowsLookup == null) {

            ArrayList<RowStruct3> localRowsLookup = new ArrayList<RowStruct3>();

            integers = new ArrayList<Integer>();
            strings = new ArrayList<String>();

            TimeMeasure.begin("Loading data");
            int step = 0;
            for (int i = 0; i < TOTAL_ROWS; i++) {
                step = i % 10 == 0 ? i : step;
                localRowsLookup.add(new RowStruct3(i, step + 5, String.valueOf(i), String.valueOf(i), "line" + step));
                integers.add(new Integer(i));
                strings.add(String.valueOf(i));
            }
            TimeMeasure.end("Loading data");
            rowsLookup = localRowsLookup;
        }

        TimeMeasure.begin("testPerfomance executeWithOneStringHashKeyAndRange");
        LoadReadExecutorWithLookup executeWithOneStringHashKeyAndRange = new LoadReadExecutorWithLookup(
                ITERATIONS_FOR_QUICK);
        for (int i = 0; i < 5; i++) {
            executeWithOneStringHashKeyAndRange.executeWithOneStringHashKeyAndRange();
            TimeMeasure.step("testPerfomance executeWithOneStringHashKeyAndRange", "Execution " + i);
        }
        TimeMeasure.end("testPerfomance executeWithOneStringHashKeyAndRange");

        System.gc();

        TimeMeasure.begin("testPerfomance executeWithTwoIntegersHashKeys");
        LoadReadExecutorWithLookup executeWithTwoIntegersHashKeys = new LoadReadExecutorWithLookup(ITERATIONS_FOR_QUICK);
        for (int i = 0; i < 5; i++) {
            executeWithTwoIntegersHashKeys.executeWithTwoIntegersHashKeys();
            TimeMeasure.step("testPerfomance executeWithTwoIntegersHashKeys", "Execution " + i);
        }
        TimeMeasure.end("testPerfomance executeWithTwoIntegersHashKeys");

        System.gc();

    }

    /**
     *
     * DOC amaumont PerformanceJavaTHash class global comment. Detailled comment <br/>
     *
     * $Id$
     *
     */
    private class LoadReadExecutorWithLookup {

        private int nIterations;

        /**
         * DOC amaumont LoadReadExecutor constructor comment.
         */
        public LoadReadExecutorWithLookup(int nIterations) {
            super();
            this.nIterations = nIterations;
        }

        public void executeWithTwoIntegersHashKeys() {

            int lstSize = nIterations;
            AdvancedLookup lookup = AdvancedLookup.<RowStruct3>getLookup(MATCHING_MODE.ALL_MATCHES);

            TimeMeasure.measureActive = true;

            ArrayList<RowStruct3> localRowsLookup = rowsLookup;

            RowStruct3.startRangeIsKey = true;
            RowStruct3.endRangeIsKey = false;
            RowStruct3.stringKeyIsKey = false;
            RowStruct3.stringKey2IsKey = false;
            RowStruct3.nameIsKey = false;

            TimeMeasure.begin("loading");
            for (int i = 0; i < lstSize; i++) {
                RowStruct3 row = (RowStruct3) localRowsLookup.get(i);
                row.hashCodeDirty = true;

                lookup.put(row);
                // TimeMeasure.step("loading", "end of loop block");
            }
            TimeMeasure.end("loading");

            TimeMeasure.begin("reading");
            int nRowsFound = 0;

            RowStruct3 rowStruct3Key = new RowStruct3();

            ArrayList<Integer> localIntegers = integers;
            ArrayList<String> localStrings = strings;

            for (int i = 0; i < lstSize; i++) {

                rowStruct3Key.hashCodeDirty = true;
                rowStruct3Key.startRange = localIntegers.get(i);
//                rowStruct3Key.endRange = i;
                // RowStruct3Key.stringKey = localStrings.get(i);
                lookup.get(rowStruct3Key);
                RowStruct3 row = (RowStruct3) lookup.getResultObject();
                if (row != null) {
                    nRowsFound++;
                    // System.out.println(row.name);
                }
            }
            TimeMeasure.end("reading");
            lookup.clear();

            System.out.println("nRowsFound=" + nRowsFound);

        }

        public void executeWithOneStringHashKeyAndRange() {

            int lstSize = nIterations;
            AdvancedLookup<RowStruct3> lookup = AdvancedLookup.<RowStruct3>getLookup(MATCHING_MODE.ALL_MATCHES);

            TimeMeasure.measureActive = true;

            ArrayList<RowStruct3> localRowsLookup = rowsLookup;

            RowStruct3.startRangeIsKey = false;
            RowStruct3.endRangeIsKey = false;
            RowStruct3.stringKeyIsKey = false;
            RowStruct3.stringKey2IsKey = false;
            RowStruct3.nameIsKey = true;

            TimeMeasure.begin("loading");
            for (int i = 0; i < lstSize; i++) {
                RowStruct3 row = (RowStruct3) localRowsLookup.get(i);

                lookup.put(row);
                // TimeMeasure.step("loading", "end of loop block");
            }
            TimeMeasure.end("loading");

            TimeMeasure.begin("reading");
            int nRowsFound = 0;

            RowStruct3 rowStruct3Key = new RowStruct3();

            ArrayList<Integer> localIntegers = integers;
            ArrayList<String> localStrings = strings;

            for (int i = 0; i < lstSize; i++) {

                rowStruct3Key.hashCodeDirty = true;
                // rowStruct3Key.integerKey = localIntegers.get(i);
                rowStruct3Key.name = "line" + i;
                // RowStruct3Key.stringKey = localStrings.get(i);
                lookup.get(rowStruct3Key);
                if (lookup.hasResult()) {
                    if (lookup.resultIsObject()) {
                        RowStruct3 row = (RowStruct3) lookup.getResultObject();
                        if (row != null) {
                            if (i > row.startRange && i < row.endRange) {
                                System.out.println("ok: " + row);
                                nRowsFound++;
                            }
                            // System.out.println(row.name);
                        }
                    } else if (lookup.resultIsList()) {
                        List<RowStruct3> list = lookup.getResultList();
                        int listListSize = list.size();
                        for (int j = 0; j < listListSize; j++) {
                            RowStruct3 row = (RowStruct3) list.get(j);
                            if (i >= row.startRange && i <= row.endRange) {
                                System.out.println("ok: " + row);
                                nRowsFound++;
                            }
                        }
                    }
                }
            }
            TimeMeasure.end("reading");
            lookup.clear();

            System.out.println("nRowsFound=" + nRowsFound);

        }
    }

}
