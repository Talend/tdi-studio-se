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
package org.talend.designer.mapper.utils;

import static org.junit.Assert.assertEquals;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.talend.designer.mapper.language.java.JavaLanguage;
import org.talend.designer.mapper.model.tableentry.TableEntryLocation;
import org.talend.designer.mapper.utils.DataMapExpressionParser;

/**
 * DOC amaumont class global comment. Detailled comment <br/>
 *
 * $Id: DataMapExpressionParserTest.java 38013 2010-03-05 14:21:59Z mhirt $
 *
 */
public class DataMapExpressionParserTest {

    DataMapExpressionParser expressionParser;

    @Before
    public void setup() {
        expressionParser = new DataMapExpressionParser(new JavaLanguage());
    }

    @After
    public void clean() {
        expressionParser = null;
    }

    @Test
    public void testReplaceLocation_renamingTable_sameSuffix() {
        String result = expressionParser.replaceLocation("YYY.columnA == XXXYYY.columnA",
                new TableEntryLocation("YYY", "columnA"), new TableEntryLocation("YYYZZZ", "columnA"));
        assertEquals("YYYZZZ.columnA == XXXYYY.columnA", result);

        // underline
        result = expressionParser.replaceLocation("YYY.columnA == XXX_YYY.columnA", new TableEntryLocation("YYY", "columnA"),
                new TableEntryLocation("YYYZZZ", "columnA"));
        assertEquals("YYYZZZ.columnA == XXX_YYY.columnA", result);
    }

    @Test
    public void testReplaceLocation_renamingTable_samePreffix() {
        String result = expressionParser.replaceLocation("YYY.columnA == YYYZZZ.columnA",
                new TableEntryLocation("YYY", "columnA"), new TableEntryLocation("XXXYYY", "columnA"));
        assertEquals("XXXYYY.columnA == YYYZZZ.columnA", result);

        result = expressionParser.replaceLocation("YYY.columnA == YYY_ZZZ.columnA", new TableEntryLocation("YYY", "columnA"),
                new TableEntryLocation("XXXYYY", "columnA"));
        assertEquals("XXXYYY.columnA == YYY_ZZZ.columnA", result);
    }

    @Test
    public void testReplaceLocation_renamingTable_middle() {
        String result = expressionParser.replaceLocation("YYY.columnA == XXXYYYZZZ.columnA", new TableEntryLocation("YYY",
                "columnA"), new TableEntryLocation("XXXYYY", "columnA"));
        assertEquals("XXXYYY.columnA == XXXYYYZZZ.columnA", result);

        result = expressionParser.replaceLocation("YYY.columnA == XXX_YYY_ZZZ.columnA", new TableEntryLocation("YYY", "columnA"),
                new TableEntryLocation("XXXYYY", "columnA"));
        assertEquals("XXXYYY.columnA == XXX_YYY_ZZZ.columnA", result);

        result = expressionParser.replaceLocation("YYY.columnA == XXXYYY_ZZZ.columnA", new TableEntryLocation("YYY", "columnA"),
                new TableEntryLocation("XXXYYY", "columnA"));
        assertEquals("XXXYYY.columnA == XXXYYY_ZZZ.columnA", result);

        result = expressionParser.replaceLocation("YYY.columnA == XXX_YYYZZZ.columnA", new TableEntryLocation("YYY", "columnA"),
                new TableEntryLocation("XXXYYY", "columnA"));
        assertEquals("XXXYYY.columnA == XXX_YYYZZZ.columnA", result);
    }

    @Test
    public void testReplaceLocation_renamingTable_underlineInTable() {
        String result = expressionParser.replaceLocation("YY_Y.columnA == XXXYY_Y.columnA", new TableEntryLocation("YY_Y",
                "columnA"), new TableEntryLocation("YY_YZ", "columnA"));
        assertEquals("YY_YZ.columnA == XXXYY_Y.columnA", result);

        result = expressionParser.replaceLocation("YY_Y.columnA == XXX_YY_Y.columnA", new TableEntryLocation("YY_Y", "columnA"),
                new TableEntryLocation("YY_YZ", "columnA"));
        assertEquals("YY_YZ.columnA == XXX_YY_Y.columnA", result);

        result = expressionParser.replaceLocation("Y_Y_Y.columnA == Y_Y_YZZZ.columnA",
                new TableEntryLocation("Y_Y_Y", "columnA"), new TableEntryLocation("XY_Y_Y", "columnA"));
        assertEquals("XY_Y_Y.columnA == Y_Y_YZZZ.columnA", result);

        result = expressionParser.replaceLocation("Y_Y_Y.columnA == Y_Y_Y_ZZZ.columnA",
                new TableEntryLocation("Y_Y_Y", "columnA"), new TableEntryLocation("XY_Y_Y", "columnA"));
        assertEquals("XY_Y_Y.columnA == Y_Y_Y_ZZZ.columnA", result);

        result = expressionParser.replaceLocation("Y_Y_Y.columnA == XXXY_Y_YZZZ.columnA", new TableEntryLocation("Y_Y_Y",
                "columnA"), new TableEntryLocation("XY_Y_Y", "columnA"));
        assertEquals("XY_Y_Y.columnA == XXXY_Y_YZZZ.columnA", result);

        result = expressionParser.replaceLocation("Y_Y_Y.columnA == XXX_Y_Y_Y_ZZZ.columnA", new TableEntryLocation("Y_Y_Y",
                "columnA"), new TableEntryLocation("XY_Y_Y", "columnA"));
        assertEquals("XY_Y_Y.columnA == XXX_Y_Y_Y_ZZZ.columnA", result);
    }

    @Test
    public void testReplaceLocation_renamingTable_multiExpresions() {
        String result = expressionParser.replaceLocation("YYY.columnA != null && YYY.columnA == XXXYYY.columnA",
                new TableEntryLocation("YYY", "columnA"), new TableEntryLocation("YYYZZZ", "columnA"));
        assertEquals("YYYZZZ.columnA != null && YYYZZZ.columnA == XXXYYY.columnA", result);

        // no space
        result = expressionParser.replaceLocation("YYY.columnA!=null && YYY.columnA == XXXYYY.columnA", new TableEntryLocation(
                "YYY", "columnA"), new TableEntryLocation("YYYZZZ", "columnA"));
        assertEquals("YYYZZZ.columnA!=null && YYYZZZ.columnA == XXXYYY.columnA", result);

        result = expressionParser.replaceLocation("YYY.columnA!=null&&YYY.columnA == XXXYYY.columnA", new TableEntryLocation(
                "YYY", "columnA"), new TableEntryLocation("YYYZZZ", "columnA"));
        assertEquals("YYYZZZ.columnA!=null&&YYYZZZ.columnA == XXXYYY.columnA", result);
    }

    @Test
    public void testReplaceLocation_renamingTable_multiExpresions_diffColumns() {
        String result = expressionParser.replaceLocation("YYY.columnA == XXXYYY.columnA && YYY.columnB == XXXYYY.columnB",
                new TableEntryLocation("YYY", "columnA"), new TableEntryLocation("YYYZZZ", "columnA"));
        assertEquals("YYYZZZ.columnA == XXXYYY.columnA && YYY.columnB == XXXYYY.columnB", result);

        // no space
        result = expressionParser.replaceLocation("YYY.columnA == XXXYYY.columnA &&YYY.columnB == XXXYYY.columnB",
                new TableEntryLocation("YYY", "columnA"), new TableEntryLocation("YYYZZZ", "columnA"));
        assertEquals("YYYZZZ.columnA == XXXYYY.columnA &&YYY.columnB == XXXYYY.columnB", result);

        result = expressionParser.replaceLocation("YYY.columnA == XXXYYY.columnA&&YYY.columnB == XXXYYY.columnB",
                new TableEntryLocation("YYY", "columnA"), new TableEntryLocation("YYYZZZ", "columnA"));
        assertEquals("YYYZZZ.columnA == XXXYYY.columnA&&YYY.columnB == XXXYYY.columnB", result);

        // replace again for renaming table
        result = expressionParser.replaceLocation(result, new TableEntryLocation("YYY", "columnB"), new TableEntryLocation(
                "YYYZZZ", "columnB"));
        assertEquals("YYYZZZ.columnA == XXXYYY.columnA&&YYYZZZ.columnB == XXXYYY.columnB", result);
    }

    @Test
    public void testReplaceLocation_renamingTable_none() {
        // there is no table to be existed in expression
        final String expression = "YYYZ.columnA == XXXYYY.columnA";
        String result = expressionParser.replaceLocation(expression, new TableEntryLocation("YYY", "columnA"),
                new TableEntryLocation("YYYZZZ", "columnA"));
        assertEquals(expression, result);
    }

    @Test
    public void testReplaceLocation_renamingColumn_same() {
        String result = expressionParser.replaceLocation("YYY.columnA == XXXYYY.columnA",
                new TableEntryLocation("YYY", "columnA"), new TableEntryLocation("YYY", "columnB"));
        assertEquals("YYY.columnB == XXXYYY.columnA", result);

        result = expressionParser.replaceLocation("YYY.column_A == XXXYYY.column_A", new TableEntryLocation("YYY", "column_A"),
                new TableEntryLocation("YYY", "column_B"));
        assertEquals("YYY.column_B == XXXYYY.column_A", result);
    }

    @Test
    public void testReplaceLocation_renamingColumn_sameSuffix() {
        String result = expressionParser.replaceLocation("YYY.columnA == XXXYYY.mycolumnA", new TableEntryLocation("YYY",
                "columnA"), new TableEntryLocation("YYY", "columnB"));
        assertEquals("YYY.columnB == XXXYYY.mycolumnA", result);

        result = expressionParser.replaceLocation("YYY.columnA == XXXYYY.my_columnA", new TableEntryLocation("YYY", "columnA"),
                new TableEntryLocation("YYY", "columnB"));
        assertEquals("YYY.columnB == XXXYYY.my_columnA", result);
    }

    @Test
    public void testReplaceLocation_renamingColumn_samePreffix() {
        String result = expressionParser.replaceLocation("YYY.columnA == XXXYYY.columnAB", new TableEntryLocation("YYY",
                "columnA"), new TableEntryLocation("YYY", "columnB"));
        assertEquals("YYY.columnB == XXXYYY.columnAB", result);

        result = expressionParser.replaceLocation("YYY.columnA == XXXYYY.columnA_B", new TableEntryLocation("YYY", "columnA"),
                new TableEntryLocation("YYY", "columnB"));
        assertEquals("YYY.columnB == XXXYYY.columnA_B", result);
    }

    @Test
    public void testReplaceLocation_renamingColumn_middle() {
        String result = expressionParser.replaceLocation("YYY.columnA == XXXYYY.mycolumnAB", new TableEntryLocation("YYY",
                "columnA"), new TableEntryLocation("YYY", "columnB"));
        assertEquals("YYY.columnB == XXXYYY.mycolumnAB", result);

        result = expressionParser.replaceLocation("YYY.columnA == XXXYYY.my_columnAB", new TableEntryLocation("YYY", "columnA"),
                new TableEntryLocation("YYY", "columnB"));
        assertEquals("YYY.columnB == XXXYYY.my_columnAB", result);

        result = expressionParser.replaceLocation("YYY.columnA == XXXYYY.my_columnA_B", new TableEntryLocation("YYY", "columnA"),
                new TableEntryLocation("YYY", "columnB"));
        assertEquals("YYY.columnB == XXXYYY.my_columnA_B", result);
    }

    @Test
    public void testReplaceLocation_renamingColumn_underlineInColumn() {
        String result = expressionParser.replaceLocation("YYY.column_A == XXXYYY.column_A", new TableEntryLocation("YYY",
                "column_A"), new TableEntryLocation("YYY", "column_B"));
        assertEquals("YYY.column_B == XXXYYY.column_A", result);

        result = expressionParser.replaceLocation("YYY.column_A == XXXYYY.mycolumn_A", new TableEntryLocation("YYY", "column_A"),
                new TableEntryLocation("YYY", "column_B"));
        assertEquals("YYY.column_B == XXXYYY.mycolumn_A", result);

        result = expressionParser.replaceLocation("YYY.column_A == XXXYYY.my_column_A",
                new TableEntryLocation("YYY", "column_A"), new TableEntryLocation("YYY", "column_B"));
        assertEquals("YYY.column_B == XXXYYY.my_column_A", result);

        result = expressionParser.replaceLocation("YYY.column_A == XXXYYY.column_AB", new TableEntryLocation("YYY", "column_A"),
                new TableEntryLocation("YYY", "column_B"));
        assertEquals("YYY.column_B == XXXYYY.column_AB", result);

        result = expressionParser.replaceLocation("YYY.column_A == XXXYYY.column_A_B", new TableEntryLocation("YYY", "column_A"),
                new TableEntryLocation("YYY", "column_B"));
        assertEquals("YYY.column_B == XXXYYY.column_A_B", result);

        result = expressionParser.replaceLocation("YYY.column_A == XXXYYY.mycolumn_AB",
                new TableEntryLocation("YYY", "column_A"), new TableEntryLocation("YYY", "column_B"));
        assertEquals("YYY.column_B == XXXYYY.mycolumn_AB", result);

        result = expressionParser.replaceLocation("YYY.column_A == XXXYYY.my_column_A_B", new TableEntryLocation("YYY",
                "column_A"), new TableEntryLocation("YYY", "column_B"));
        assertEquals("YYY.column_B == XXXYYY.my_column_A_B", result);
    }

    @Test
    public void testReplaceLocation_renamingColumn_multExpresions() {
        String result = expressionParser.replaceLocation("YYY.column_A != null && YYY.column_A == XXXYYY.column_A",
                new TableEntryLocation("YYY", "column_A"), new TableEntryLocation("YYY", "column_B"));
        assertEquals("YYY.column_B != null && YYY.column_B == XXXYYY.column_A", result);

        // no space
        result = expressionParser.replaceLocation("YYY.column_A!=null&&YYY.column_A == XXXYYY.column_A", new TableEntryLocation(
                "YYY", "column_A"), new TableEntryLocation("YYY", "column_B"));
        assertEquals("YYY.column_B!=null&&YYY.column_B == XXXYYY.column_A", result);
    }

    @Test
    public void testReplaceLocation_renamingColumn_none() {
        final String expression = "YYY.columnC == XXXYYY.mycolumnAB";
        String result = expressionParser.replaceLocation(expression, new TableEntryLocation("YYY", "columnA"),
                new TableEntryLocation("YYY", "columnB"));
        assertEquals(expression, result);
    }
}
