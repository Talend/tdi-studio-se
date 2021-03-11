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
package org.talend.designer.xmlmap.ui.expressionutil;

import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;

/**
 * DOC talend class global comment. Detailled comment
 */
public class XmlMapExpressionManagerTest {

    /**
     * Test method for
     * {@link org.talend.designer.xmlmap.ui.expressionutil.XmlMapExpressionManager#parseTableEntryLocation(java.lang.String)}
     * .
     */
    @Test
    public void testParseTableEntryLocation() {
        String exp0 = "[row1.newColumn:/schema/column/originalDbColumnName] ";
        String exp1 = "row1.newColumn ";
        String exp2 = "[row1.newColumn:/schema/column/xmlns:talend] ";
        String exp3 = "[row1.newColumn:/schema/column/@comment] ";

        String expressions = exp0 + exp1 + exp2 + exp3;

        XmlMapExpressionManager manager = new XmlMapExpressionManager();
        List<TableEntryLocation> entries = manager.parseTableEntryLocation(expressions);

        assertTrue(entries.size() == 4);

        for (int i = 0; i < 4; i++) {
            TableEntryLocation entry = entries.get(i);
            if (i == 0) {
                assertTrue(exp0.trim().equals(entry.toString().trim()));
            } else if (i == 1) {
                assertTrue(exp1.trim().equals(entry.toString().trim()));
            } else if (i == 2) {
                assertTrue(exp2.trim().equals(entry.toString().trim()));
            } else if (i == 3) {
                assertTrue(exp3.trim().equals(entry.toString().trim()));
            }
        }

        String exp4 = "wrong expression";
        expressions = exp0 + exp1 + exp2 + exp3 + exp4;
        entries = manager.parseTableEntryLocation(expressions);
        assertTrue(entries.size() == 4);
    }

    /**
     * Test method for
     * {@link org.talend.designer.xmlmap.ui.expressionutil.XmlMapExpressionManager#replaceExpression(java.lang.String,TableEntryLocation,TableEntryLocation)}
     * .
     */
    @Test
    public void testReplaceExpression() {
        String exp0 = "[row1.newColumn:/schema/column/originalDbColumnName] ";
        String newexp0 = "[row1.newColumn:/schemaTest/column/originalDbColumnName] ";
        String exp1 = "row1.newColumn ";

        String oldExpression = exp0 + exp1;
        XmlMapExpressionManager manager = new XmlMapExpressionManager();

        TableEntryLocation oldLoction = new TableEntryLocation("[", "row1", "newColumn:/schema/column/originalDbColumnName", "]");
        TableEntryLocation newLocation = new TableEntryLocation("[", "row1", "newColumn:/schemaTest/column/originalDbColumnName",
                "]");
        String expectedExpression = newexp0 + exp1;
        String newExpression = manager.replaceExpression(oldExpression, oldLoction, newLocation);
        assertTrue(newExpression != null);
        assertTrue(newExpression.equals(expectedExpression));

    }

}
