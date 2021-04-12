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
package org.talend.designer.mapper.language.java;

import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * DOC hwang class global comment. Detailled comment <br/>
 *
 * $Id: JavaLanguageTest.java 38013 2017-02-27 14:21:59Z mhirt $
 *
 */
public class JavaLanguageTest {

    JavaLanguage javaLan;

    @Before
    public void setup() {
        javaLan = new JavaLanguage();
    }

    @After
    public void clean() {
        javaLan = null;
    }

    /**
     * Test method for
     * {@link org.talend.designer.mapper.language.java.JavaLanguage#getLocationPattern()}.
     */
    @Test
    public void testGetLocationPattern() {

        java.util.regex.Pattern locationPattern = java.util.regex.Pattern.compile(javaLan.getLocationPattern());
        String expression = "row1.name";
        java.util.regex.Matcher matcher = locationPattern.matcher(expression);
        assertTrue(matcher.matches());

        expression = "row1.name_1";
        matcher = locationPattern.matcher(expression);
        assertTrue(matcher.matches());

        expression = "row1.中文";
        matcher = locationPattern.matcher(expression);
        assertTrue(matcher.matches());

        expression = "row1.中文1";
        matcher = locationPattern.matcher(expression);
        assertTrue(matcher.matches());

        expression = "row1.中文1abc1";
        matcher = locationPattern.matcher(expression);
        assertTrue(matcher.matches());

        expression = "row1.中文abc1";
        matcher = locationPattern.matcher(expression);
        assertTrue(matcher.matches());

        expression = "row1.name中文";
        matcher = locationPattern.matcher(expression);
        assertTrue(matcher.matches());

        expression = "row1.name1中文";
        matcher = locationPattern.matcher(expression);
        assertTrue(matcher.matches());

        expression = "row1.中文1中文";
        matcher = locationPattern.matcher(expression);
        assertTrue(matcher.matches());

        expression = "row1.中文1abc1中文";
        matcher = locationPattern.matcher(expression);
        assertTrue(matcher.matches());

        expression = "row1.中文_中文";
        matcher = locationPattern.matcher(expression);
        assertTrue(matcher.matches());

        expression = "row1.中文_1";
        matcher = locationPattern.matcher(expression);
        assertTrue(matcher.matches());

        expression = "row1.中文_abc1";
        matcher = locationPattern.matcher(expression);
        assertTrue(matcher.matches());

        expression = "row1._valid＿end＿dtime";
        matcher = locationPattern.matcher(expression);
        assertTrue(matcher.matches());

        expression = "row1._中文_";
        matcher = locationPattern.matcher(expression);
        assertTrue(matcher.matches());

        expression = "row1.テスト＿フラグ";
        matcher = locationPattern.matcher(expression);
        assertTrue(matcher.matches());

    }

}
