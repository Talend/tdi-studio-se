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
package org.talend.designer.dbmap.utils.problems;

import org.junit.Assert;
import org.junit.Test;

/**
 * created by wchen on Nov 24, 2017 Detailled comment
 *
 */
public class ProblemsAnalyserTest {

    @Test
    public void testNeedAlias() {
        ProblemsAnalyser analyser = new ProblemsAnalyser(null);
        boolean needAlias = analyser.needAlias("abc");
        Assert.assertFalse(needAlias);

        needAlias = analyser.needAlias("\"schema\"");
        Assert.assertFalse(needAlias);

        needAlias = analyser.needAlias("\"schema+table\"");
        Assert.assertFalse(needAlias);

        needAlias = analyser.needAlias("((String)globalMap.get(\"source+source1\")).((String)globalMap.get(\"test\"))");
        Assert.assertFalse(needAlias);

        needAlias = analyser.needAlias("\"schema\"+\"schema\"");
        Assert.assertTrue(needAlias);

        needAlias = analyser.needAlias("\"constant\"+context.schema");
        Assert.assertTrue(needAlias);

        needAlias = analyser.needAlias("context.schema+\"constant\"");
        Assert.assertTrue(needAlias);

        needAlias = analyser.needAlias("context.schema+context.table");
        Assert.assertTrue(needAlias);

        needAlias = analyser.needAlias("\"constant\"+((String)globalMap.get(\"source\"))");
        Assert.assertTrue(needAlias);

        needAlias = analyser.needAlias("((String)globalMap.get(\"source\"))+\"constant\"");
        Assert.assertTrue(needAlias);

        needAlias = analyser.needAlias("((String)globalMap.get(\"source\"))+((String)globalMap.get(\"test\"))");
        Assert.assertTrue(needAlias);

        needAlias = analyser.needAlias("((String)globalMap.get(\"schema\"))+context.xx.my_table");
        Assert.assertTrue(needAlias);

    }

}
