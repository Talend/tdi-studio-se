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
package org.talend.designer.core.model.process.jobsettings;

import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Test;
import org.talend.core.model.metadata.IMetadataColumn;
import org.talend.core.model.metadata.IMetadataTable;
import org.talend.core.model.metadata.types.JavaTypesManager;
import org.talend.core.model.process.IProcess2;
import org.talend.core.model.properties.PropertiesFactory;
import org.talend.core.model.properties.Property;
import org.talend.designer.core.model.components.EParameterName;
import org.talend.designer.core.model.process.DataNode;
import org.talend.designer.core.ui.editor.process.Process;

import junit.framework.Assert;

/**
 * created by wchen on 2016年1月21日 Detailled comment
 *
 */
public class JobSettingsManagerTest {

    public static final String QUOTE_MARK = "\"";

    /**
     * Test method for
     * {@link org.talend.designer.core.model.process.jobsettings.JobSettingsManager#createExtraContextLoadNodes(org.talend.core.model.process.IProcess)}
     * .
     */
    @Test
    public void testCreateExtraContextLoadNodes() {
        // junit for TUP-3972
        Property property = PropertiesFactory.eINSTANCE.createProperty();
        IProcess2 process = new Process(property);
        process.getElementParameter(EParameterName.IMPLICIT_TCONTEXTLOAD.getName()).setValue(true);
        process.getElementParameter(EParameterName.IMPLICIT_TCONTEXTLOAD_FILE.getName()).setValue("the test file");
        process.getElementParameter("FROM_FILE_FLAG_IMPLICIT_CONTEXT").setValue(true);
        final List<DataNode> createContextLoadNodes = JobSettingsManager.createExtraContextLoadNodes(process);
        assertNotEquals(createContextLoadNodes.size(), 0);
        final DataNode dataNode = createContextLoadNodes.get(0);
        final IMetadataTable metadataTable = dataNode.getMetadataList().get(0);
        for (IMetadataColumn column : metadataTable.getListColumns()) {
            assertNotNull(column.getDefault());
            assertNotNull(JavaTypesManager.getDefaultValueFromJavaType(column.getTalendType(), column.getDefault()));
        }
    }

    @Test
    public void testGetSeparatorsRegexp4Null() {
        Assert.assertEquals("\"^([^\"+\"\"+\"]*)\"+\"\"+\"(.*)$\"", JobSettingsManager.FileSeparator.getSeparatorsRegexp(null));
    }

    @Test
    public void testGetSeparatorsRegexp4EmptyCharacter() {
        String separator = "";
        Assert.assertEquals("\"^([^\"+\"\"+\"]*)\"+\"\"+\"(.*)$\"",
                JobSettingsManager.FileSeparator.getSeparatorsRegexp(separator));
    }

    @Test
    public void testGetSeparatorsRegexp1() {
        String separator = QUOTE_MARK + "\\" + QUOTE_MARK;
        Assert.assertEquals("\"^([^\"+\"\\\\\\\\\"+\"]*)\"+\"\\\\\\\\\"+\"(.*)$\"",
                JobSettingsManager.FileSeparator.getSeparatorsRegexp(separator));
    }

    @Test
    public void testGetSeparatorsRegexp2() {
        String separator = QUOTE_MARK + "^" + QUOTE_MARK;
        Assert.assertEquals("\"^([^\"+\"\\\\^\"+\"]*)\"+\"\\\\^\"+\"(.*)$\"",
                JobSettingsManager.FileSeparator.getSeparatorsRegexp(separator));
    }

    @Test
    public void testGetSeparatorsRegexp3() {
        String separator = QUOTE_MARK + "$" + QUOTE_MARK;
        Assert.assertEquals("\"^([^\"+\"\\\\$\"+\"]*)\"+\"\\\\$\"+\"(.*)$\"",
                JobSettingsManager.FileSeparator.getSeparatorsRegexp(separator));
    }

    @Test
    public void testGetSeparatorsRegexp4() {
        String separator = QUOTE_MARK + "." + QUOTE_MARK;
        Assert.assertEquals("\"^([^\"+\"\\\\.\"+\"]*)\"+\"\\\\.\"+\"(.*)$\"",
                JobSettingsManager.FileSeparator.getSeparatorsRegexp(separator));
    }

    @Test
    public void testGetSeparatorsRegexp5() {
        String separator = QUOTE_MARK + "?" + QUOTE_MARK;
        Assert.assertEquals("\"^([^\"+\"\\\\?\"+\"]*)\"+\"\\\\?\"+\"(.*)$\"",
                JobSettingsManager.FileSeparator.getSeparatorsRegexp(separator));
    }

    @Test
    public void testGetSeparatorsRegexp6() {
        String separator = QUOTE_MARK + "|" + QUOTE_MARK;
        Assert.assertEquals("\"^([^\"+\"\\\\|\"+\"]*)\"+\"\\\\|\"+\"(.*)$\"",
                JobSettingsManager.FileSeparator.getSeparatorsRegexp(separator));
    }

    @Test
    public void testGetSeparatorsRegexp7() {
        String separator = QUOTE_MARK + "[" + QUOTE_MARK;
        Assert.assertEquals("\"^([^\"+\"\\\\[\"+\"]*)\"+\"\\\\[\"+\"(.*)$\"",
                JobSettingsManager.FileSeparator.getSeparatorsRegexp(separator));
    }

    @Test
    public void testGetSeparatorsRegexp8() {
        String separator = QUOTE_MARK + "+" + QUOTE_MARK;
        Assert.assertEquals("\"^([^\"+\"\\\\+\"+\"]*)\"+\"\\\\+\"+\"(.*)$\"",
                JobSettingsManager.FileSeparator.getSeparatorsRegexp(separator));
    }

    @Test
    public void testGetSeparatorsRegexp9() {
        String separator = QUOTE_MARK + "*" + QUOTE_MARK;
        Assert.assertEquals("\"^([^\"+\"\\\\*\"+\"]*)\"+\"\\\\*\"+\"(.*)$\"",
                JobSettingsManager.FileSeparator.getSeparatorsRegexp(separator));
    }

    @Test
    public void testGetSeparatorsRegexp10() {
        String separator = QUOTE_MARK + "{" + QUOTE_MARK;
        Assert.assertEquals("\"^([^\"+\"\\\\{\"+\"]*)\"+\"\\\\{\"+\"(.*)$\"",
                JobSettingsManager.FileSeparator.getSeparatorsRegexp(separator));
    }

    @Test
    public void testGetSeparatorsRegexp11() {
        String separator = QUOTE_MARK + "(" + QUOTE_MARK;
        Assert.assertEquals("\"^([^\"+\"\\\\(\"+\"]*)\"+\"\\\\(\"+\"(.*)$\"",
                JobSettingsManager.FileSeparator.getSeparatorsRegexp(separator));
    }

    @Test
    public void testGetSeparatorsRegexp12() {
        String separator = QUOTE_MARK + ")" + QUOTE_MARK;
        Assert.assertEquals("\"^([^\"+\"\\\\)\"+\"]*)\"+\"\\\\)\"+\"(.*)$\"",
                JobSettingsManager.FileSeparator.getSeparatorsRegexp(separator));
    }

    @Test
    public void testGetSeparatorsRegexp13() {
        String separator = QUOTE_MARK + "}" + QUOTE_MARK;
        Assert.assertEquals("\"^([^\"+\"\\\\}\"+\"]*)\"+\"\\\\}\"+\"(.*)$\"",
                JobSettingsManager.FileSeparator.getSeparatorsRegexp(separator));
    }

    @Test
    public void testGetSeparatorsRegexp14() {
        String separator = QUOTE_MARK + "]" + QUOTE_MARK;
        Assert.assertEquals("\"^([^\"+\"\\\\]\"+\"]*)\"+\"\\\\]\"+\"(.*)$\"",
                JobSettingsManager.FileSeparator.getSeparatorsRegexp(separator));
    }

    @Test
    public void testDoRegexpQuote4Null() {
        Assert.assertEquals("\"\"", JobSettingsManager.FileSeparator.doRegexpQuote(null));
    }

    @Test
    public void testDoRegexpQuote4EmptyCharacter() {
        String separator = "";
        Assert.assertEquals("\"\"", JobSettingsManager.FileSeparator.doRegexpQuote(separator));
    }

    @Test
    public void testDoRegexpQuote1() {
        String separator = QUOTE_MARK + "\\" + QUOTE_MARK;
        Assert.assertEquals("\"\\\\\\\\\"", JobSettingsManager.FileSeparator.doRegexpQuote(separator));
    }

    @Test
    public void testDoRegexpQuote2() {
        String separator = QUOTE_MARK + "^" + QUOTE_MARK;
        Assert.assertEquals("\"\\\\^\"", JobSettingsManager.FileSeparator.doRegexpQuote(separator));
    }

    @Test
    public void testDoRegexpQuote3() {
        String separator = QUOTE_MARK + "$" + QUOTE_MARK;
        Assert.assertEquals("\"\\\\$\"", JobSettingsManager.FileSeparator.doRegexpQuote(separator));
    }

    @Test
    public void testDoRegexpQuote4() {
        String separator = QUOTE_MARK + "." + QUOTE_MARK;
        Assert.assertEquals("\"\\\\.\"", JobSettingsManager.FileSeparator.doRegexpQuote(separator));
    }

    @Test
    public void testDoRegexpQuote5() {
        String separator = QUOTE_MARK + "?" + QUOTE_MARK;
        Assert.assertEquals("\"\\\\?\"", JobSettingsManager.FileSeparator.doRegexpQuote(separator));
    }

    @Test
    public void testDoRegexpQuote6() {
        String separator = QUOTE_MARK + "|" + QUOTE_MARK;
        Assert.assertEquals("\"\\\\|\"", JobSettingsManager.FileSeparator.doRegexpQuote(separator));
        separator = "\"||\"";
        Assert.assertEquals("\"\\\\|\\\\|\"", JobSettingsManager.FileSeparator.doRegexpQuote(separator));
    }

    @Test
    public void testDoRegexpQuote7() {
        String separator = QUOTE_MARK + "[" + QUOTE_MARK;
        Assert.assertEquals("\"\\\\[\"", JobSettingsManager.FileSeparator.doRegexpQuote(separator));
    }

    @Test
    public void testDoRegexpQuote8() {
        String separator = QUOTE_MARK + "+" + QUOTE_MARK;
        Assert.assertEquals("\"\\\\+\"", JobSettingsManager.FileSeparator.doRegexpQuote(separator));
    }

    @Test
    public void testDoRegexpQuote9() {
        String separator = QUOTE_MARK + "*" + QUOTE_MARK;
        Assert.assertEquals("\"\\\\*\"", JobSettingsManager.FileSeparator.doRegexpQuote(separator));
    }

    @Test
    public void testDoRegexpQuote10() {
        String separator = QUOTE_MARK + "{" + QUOTE_MARK;
        Assert.assertEquals("\"\\\\{\"", JobSettingsManager.FileSeparator.doRegexpQuote(separator));
    }

    @Test
    public void testDoRegexpQuote11() {
        String separator = QUOTE_MARK + "(" + QUOTE_MARK;
        Assert.assertEquals("\"\\\\(\"", JobSettingsManager.FileSeparator.doRegexpQuote(separator));
    }

    @Test
    public void testDoRegexpQuote12() {
        String separator = QUOTE_MARK + ")" + QUOTE_MARK;
        Assert.assertEquals("\"\\\\)\"", JobSettingsManager.FileSeparator.doRegexpQuote(separator));
    }

    @Test
    public void testDoRegexpQuote13() {
        String separator = QUOTE_MARK + "}" + QUOTE_MARK;
        Assert.assertEquals("\"\\\\}\"", JobSettingsManager.FileSeparator.doRegexpQuote(separator));
    }

    @Test
    public void testDoRegexpQuote14() {
        String separator = QUOTE_MARK + "]" + QUOTE_MARK;
        Assert.assertEquals("\"\\\\]\"", JobSettingsManager.FileSeparator.doRegexpQuote(separator));
    }

    @Test
    public void testDoRegexpQuoteQuestion() {
        String separator = QUOTE_MARK + "?" + QUOTE_MARK;
        Assert.assertEquals("\"\\\\?\"", JobSettingsManager.FileSeparator.doRegexpQuote(separator));
    }

    @Test
    public void testDoRegexpQuoteQuotation() {
        String separator = QUOTE_MARK + "\"" + QUOTE_MARK;
        Assert.assertEquals("\"\\\\\\\"\"", JobSettingsManager.FileSeparator.doRegexpQuote(separator));
    }


    @Test
    public void testDoRegexpQuoteDoublePipe() {
        String separator = QUOTE_MARK + "||" + QUOTE_MARK;
        Assert.assertEquals("\"\\\\|\\\\|\"", JobSettingsManager.FileSeparator.doRegexpQuote(separator));
    }

    @Test
    public void testGetSeparatorsRegexpDoublePipe() {
        String separator = QUOTE_MARK + "||" + QUOTE_MARK;
        Assert.assertEquals("\"^([^\"+\"\\\\|\\\\|\"+\"]*)\"+\"\\\\|\\\\|\"+\"(.*)$\"",
                JobSettingsManager.FileSeparator.getSeparatorsRegexp(separator));
    }

    @Test
    public void testGetSeparatorsRegexpQuotation() {
        String separator = QUOTE_MARK + "\"" + QUOTE_MARK;
        Assert.assertEquals("\"^([^\"+\"\\\\\\\"\"+\"]*)\"+\"\\\\\\\"\"+\"(.*)$\"",
                JobSettingsManager.FileSeparator.getSeparatorsRegexp(separator));
    }

    @Test
    public void testSpecificCharacterInRegex() {
        String key = "Param";
        String value = "Param value";

        String separator = "~~#*#~~";
        String contextStr = key + separator + value;
        String realRegex = "^([^" + "~~#\\*#~~" + "]*)" + "~~#\\*#~~" + "(.*)$";
        String expect = "\"^([^\"+\"~~#\\\\*#~~\"+\"]*)\"+\"~~#\\\\*#~~\"+\"(.*)$\"";
        String regex = JobSettingsManager.FileSeparator.getSeparatorsRegexp(QUOTE_MARK + separator + QUOTE_MARK);
        Assert.assertEquals(expect, regex);
        Pattern compile = Pattern.compile(realRegex);
        Matcher matcher = compile.matcher(contextStr);
        Assert.assertTrue(matcher.find());
        Assert.assertEquals(key, matcher.group(1));
        Assert.assertEquals(value, matcher.group(2));

        separator = "***";
        contextStr = key + separator + value;
        realRegex = "^([^" + "\\*\\*\\*" + "]*)" + "\\*\\*\\*" + "(.*)$";
        expect = "\"^([^\"+\"\\\\*\\\\*\\\\*\"+\"]*)\"+\"\\\\*\\\\*\\\\*\"+\"(.*)$\"";
        regex = JobSettingsManager.FileSeparator.getSeparatorsRegexp(QUOTE_MARK + separator + QUOTE_MARK);
        Assert.assertEquals(expect, regex);
        compile = Pattern.compile(realRegex);
        matcher = compile.matcher(contextStr);
        Assert.assertTrue(matcher.find());
        Assert.assertEquals(key, matcher.group(1));
        Assert.assertEquals(value, matcher.group(2));

        // characters "\\", "^", "$", ".", "?", "|", "[", "+", "*", "{", "(", ")", "}", "]", "\""
        separator = "~*^$.?|[+{()}]";
        contextStr = key + separator + value;
        realRegex = "^([^" + "~\\*\\^\\$\\.\\?\\|\\[\\+\\{\\(\\)\\}\\]" + "]*)" + "~\\*\\^\\$\\.\\?\\|\\[\\+\\{\\(\\)\\}\\]"
                + "(.*)$";
        expect = "\"^([^\"+\"~\\\\*\\\\^\\\\$\\\\.\\\\?\\\\|\\\\[\\\\+\\\\{\\\\(\\\\)\\\\}\\\\]\"+\"]*)\"+\"~\\\\*\\\\^\\\\$\\\\.\\\\?\\\\|\\\\[\\\\+\\\\{\\\\(\\\\)\\\\}\\\\]\"+\"(.*)$\"";
        regex = JobSettingsManager.FileSeparator.getSeparatorsRegexp(QUOTE_MARK + separator + QUOTE_MARK);
        Assert.assertEquals(expect, regex);
        compile = Pattern.compile(realRegex);
        matcher = compile.matcher(contextStr);
        Assert.assertTrue(matcher.find());
        Assert.assertEquals(key, matcher.group(1));
        Assert.assertEquals(value, matcher.group(2));

        separator = "\\";
        contextStr = key + separator + value;
        realRegex = "^([^" + "\\\\" + "]*)" + "\\\\" + "(.*)$";
        expect = "\"^([^\"+\"\\\\\\\\\"+\"]*)\"+\"\\\\\\\\\"+\"(.*)$\"";
        regex = JobSettingsManager.FileSeparator.getSeparatorsRegexp(QUOTE_MARK + separator + QUOTE_MARK);
        Assert.assertEquals(expect, regex);
        compile = Pattern.compile(realRegex);
        matcher = compile.matcher(contextStr);
        Assert.assertTrue(matcher.find());
        Assert.assertEquals(key, matcher.group(1));
        Assert.assertEquals(value, matcher.group(2));

        separator = "\\\\\\";
        contextStr = key + separator + value;
        realRegex = "^([^" + "\\\\\\\\\\\\" + "]*)" + "\\\\\\\\\\\\" + "(.*)$";
        expect = "\"^([^\"+\"\\\\\\\\\\\\\\\\\\\\\\\\\"+\"]*)\"+\"\\\\\\\\\\\\\\\\\\\\\\\\\"+\"(.*)$\"";
        regex = JobSettingsManager.FileSeparator.getSeparatorsRegexp(QUOTE_MARK + separator + QUOTE_MARK);
        Assert.assertEquals(expect, regex);
        compile = Pattern.compile(realRegex);
        matcher = compile.matcher(contextStr);
        Assert.assertTrue(matcher.find());
        Assert.assertEquals(key, matcher.group(1));
        Assert.assertEquals(value, matcher.group(2));

    }

    @Test
    public void testSpecificCharacterWithContextInRegex() {
        Map<String, String> separatorhm = new HashMap<String, String>();
        separatorhm.put("context.mark", "context.mark");
        separatorhm.put("context.mark+context.mark", "context.mark+context.mark");
        // "~~#\\*#~~"+context.mark+"~~#\\*#~~"
        separatorhm.put("\"~~#*#~~\"+context.mark+\"~~#*#~~\"", "\"~~#\\\\*#~~\"+context.mark+\"~~#\\\\*#~~\"");
        // context.mark+"~~#\\*#~~+context.mark"
        separatorhm.put("context.mark+\"~~#*#~~\"+context.mark", "context.mark+\"~~#\\\\*#~~\"+context.mark");
        // "\\+"
        separatorhm.put("\"+\"", "\"\\\\+\"");
        // "\\+\\+\\+\\+\\+"
        separatorhm.put("\"+++++\"", "\"\\\\+\\\\+\\\\+\\\\+\\\\+\"");
        separatorhm.put("\" + + +\"", "\" \\\\+ \\\\+ \\\\+\"");
        // "#\\*\\+\\$\\?"+context.mark
        separatorhm.put("\"#*+$?\"+context.mark", "\"#\\\\*\\\\+\\\\$\\\\?\"+context.mark");
        // context.mark+"#\\*\\+\\$\\?"+context.mark
        separatorhm.put("context.mark+\"#*+$?\"+context.mark", "context.mark+\"#\\\\*\\\\+\\\\$\\\\?\"+context.mark");
        // "#\\*\\+\\$\\?"+context.mark+"#\\*\\+\\$\\?"
        separatorhm.put("\"#*+$?\"+context.mark+\"#*+$?\"", "\"#\\\\*\\\\+\\\\$\\\\?\"+context.mark+\"#\\\\*\\\\+\\\\$\\\\?\"");

        for (String separator : separatorhm.keySet()) {
            String expect = "\"^([^\"+" + separatorhm.get(separator) + "+\"]*)\"+" + separatorhm.get(separator) + "+\"(.*)$\"";
            String regex = JobSettingsManager.FileSeparator.getSeparatorsRegexp(separator);
            Assert.assertEquals("Resolve separator => " + separator + " <= not as expect", expect, regex);
        }
    }

    @Test
    public void testAddMarkWithChar() {
        String testStr = "***";
        String expect = "\\*\\*\\*";
        String result = JobSettingsManager.FileSeparator.addMarkWithChar(testStr, "*", "\\", true);
        Assert.assertEquals(expect, result);
        expect = "*\\*\\*\\";
        result = JobSettingsManager.FileSeparator.addMarkWithChar(testStr, "*", "\\", false);
        Assert.assertEquals(expect, result);

        testStr = "#*#";
        expect = "#\\*#";
        result = JobSettingsManager.FileSeparator.addMarkWithChar(testStr, "*", "\\", true);
        Assert.assertEquals(expect, result);
        expect = "#*\\#";
        result = JobSettingsManager.FileSeparator.addMarkWithChar(testStr, "*", "\\", false);
        Assert.assertEquals(expect, result);
    }

    // \t case
    @Test
    public void testSimpleCase() {
        String separator = QUOTE_MARK + "\\t" + QUOTE_MARK;
        Assert.assertEquals("\"\\t\"", JobSettingsManager.FileSeparator.doRegexpQuote(separator));

    }
}
