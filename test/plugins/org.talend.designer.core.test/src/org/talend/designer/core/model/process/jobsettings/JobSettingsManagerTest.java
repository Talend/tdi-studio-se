// ============================================================================
//
// Copyright (C) 2006-2014 Talend Inc. - www.talend.com
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

import static org.junit.Assert.*;

import java.util.List;

import junit.framework.Assert;

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

/**
 * created by wchen on 2016年1月21日 Detailled comment
 *
 */
public class JobSettingsManagerTest {

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
        String separator = "\\";
        Assert.assertEquals("\"^([^\"+\"\\\\\\\\\"+\"]*)\"+\"\\\\\\\\\"+\"(.*)$\"",
                JobSettingsManager.FileSeparator.getSeparatorsRegexp(separator));
        separator = "\\\\";
        Assert.assertEquals("\"^([^\"+\"\\\\\\\\\\\\\\\\\"+\"]*)\"+\"\\\\\\\\\\\\\\\\\"+\"(.*)$\"",
                JobSettingsManager.FileSeparator.getSeparatorsRegexp(separator));
    }

    @Test
    public void testGetSeparatorsRegexp2() {
        String separator = "^";
        Assert.assertEquals("\"^([^\"+\"\\\\^\"+\"]*)\"+\"\\\\^\"+\"(.*)$\"",
                JobSettingsManager.FileSeparator.getSeparatorsRegexp(separator));
        separator = "^^";
        Assert.assertEquals("\"^([^\"+\"\\\\^\\\\^\"+\"]*)\"+\"\\\\^\\\\^\"+\"(.*)$\"",
                JobSettingsManager.FileSeparator.getSeparatorsRegexp(separator));
    }

    @Test
    public void testGetSeparatorsRegexp3() {
        String separator = "$";
        Assert.assertEquals("\"^([^\"+\"\\\\$\"+\"]*)\"+\"\\\\$\"+\"(.*)$\"",
                JobSettingsManager.FileSeparator.getSeparatorsRegexp(separator));
        separator = "$$";
        Assert.assertEquals("\"^([^\"+\"\\\\$\\\\$\"+\"]*)\"+\"\\\\$\\\\$\"+\"(.*)$\"",
                JobSettingsManager.FileSeparator.getSeparatorsRegexp(separator));
    }

    @Test
    public void testGetSeparatorsRegexp4() {
        String separator = ".";
        Assert.assertEquals("\"^([^\"+\"\\\\.\"+\"]*)\"+\"\\\\.\"+\"(.*)$\"",
                JobSettingsManager.FileSeparator.getSeparatorsRegexp(separator));
        separator = "..";
        Assert.assertEquals("\"^([^\"+\"\\\\.\\\\.\"+\"]*)\"+\"\\\\.\\\\.\"+\"(.*)$\"",
                JobSettingsManager.FileSeparator.getSeparatorsRegexp(separator));
    }

    @Test
    public void testGetSeparatorsRegexp5() {
        String separator = "?";
        Assert.assertEquals("\"^([^\"+\"\\\\?\"+\"]*)\"+\"\\\\?\"+\"(.*)$\"",
                JobSettingsManager.FileSeparator.getSeparatorsRegexp(separator));
        separator = "??";
        Assert.assertEquals("\"^([^\"+\"\\\\?\\\\?\"+\"]*)\"+\"\\\\?\\\\?\"+\"(.*)$\"",
                JobSettingsManager.FileSeparator.getSeparatorsRegexp(separator));
    }

    @Test
    public void testGetSeparatorsRegexp6() {
        String separator = "|";
        Assert.assertEquals("\"^([^\"+\"\\\\|\"+\"]*)\"+\"\\\\|\"+\"(.*)$\"",
                JobSettingsManager.FileSeparator.getSeparatorsRegexp(separator));
        separator = "||";
        Assert.assertEquals("\"^([^\"+\"\\\\|\\\\|\"+\"]*)\"+\"\\\\|\\\\|\"+\"(.*)$\"",
                JobSettingsManager.FileSeparator.getSeparatorsRegexp(separator));
    }

    @Test
    public void testGetSeparatorsRegexp7() {
        String separator = "[";
        Assert.assertEquals("\"^([^\"+\"\\\\[\"+\"]*)\"+\"\\\\[\"+\"(.*)$\"",
                JobSettingsManager.FileSeparator.getSeparatorsRegexp(separator));
        separator = "[[";
        Assert.assertEquals("\"^([^\"+\"\\\\[\\\\[\"+\"]*)\"+\"\\\\[\\\\[\"+\"(.*)$\"",
                JobSettingsManager.FileSeparator.getSeparatorsRegexp(separator));
    }

    @Test
    public void testGetSeparatorsRegexp8() {
        String separator = "+";
        Assert.assertEquals("\"^([^\"+\"\\\\+\"+\"]*)\"+\"\\\\+\"+\"(.*)$\"",
                JobSettingsManager.FileSeparator.getSeparatorsRegexp(separator));
        separator = "++";
        Assert.assertEquals("\"^([^\"+\"\\\\+\\\\+\"+\"]*)\"+\"\\\\+\\\\+\"+\"(.*)$\"",
                JobSettingsManager.FileSeparator.getSeparatorsRegexp(separator));
    }

    @Test
    public void testGetSeparatorsRegexp9() {
        String separator = "*";
        Assert.assertEquals("\"^([^\"+\"\\\\*\"+\"]*)\"+\"\\\\*\"+\"(.*)$\"",
                JobSettingsManager.FileSeparator.getSeparatorsRegexp(separator));
        separator = "**";
        Assert.assertEquals("\"^([^\"+\"\\\\*\\\\*\"+\"]*)\"+\"\\\\*\\\\*\"+\"(.*)$\"",
                JobSettingsManager.FileSeparator.getSeparatorsRegexp(separator));
    }

    @Test
    public void testGetSeparatorsRegexp10() {
        String separator = "{";
        Assert.assertEquals("\"^([^\"+\"\\\\{\"+\"]*)\"+\"\\\\{\"+\"(.*)$\"",
                JobSettingsManager.FileSeparator.getSeparatorsRegexp(separator));
        separator = "{{";
        Assert.assertEquals("\"^([^\"+\"\\\\{\\\\{\"+\"]*)\"+\"\\\\{\\\\{\"+\"(.*)$\"",
                JobSettingsManager.FileSeparator.getSeparatorsRegexp(separator));
    }

    @Test
    public void testGetSeparatorsRegexp11() {
        String separator = "(";
        Assert.assertEquals("\"^([^\"+\"\\\\(\"+\"]*)\"+\"\\\\(\"+\"(.*)$\"",
                JobSettingsManager.FileSeparator.getSeparatorsRegexp(separator));
        separator = "((";
        Assert.assertEquals("\"^([^\"+\"\\\\(\\\\(\"+\"]*)\"+\"\\\\(\\\\(\"+\"(.*)$\"",
                JobSettingsManager.FileSeparator.getSeparatorsRegexp(separator));
    }

    @Test
    public void testGetSeparatorsRegexp12() {
        String separator = ")";
        Assert.assertEquals("\"^([^\"+\"\\\\)\"+\"]*)\"+\"\\\\)\"+\"(.*)$\"",
                JobSettingsManager.FileSeparator.getSeparatorsRegexp(separator));
        separator = "))";
        Assert.assertEquals("\"^([^\"+\"\\\\)\\\\)\"+\"]*)\"+\"\\\\)\\\\)\"+\"(.*)$\"",
                JobSettingsManager.FileSeparator.getSeparatorsRegexp(separator));
    }

    @Test
    public void testGetSeparatorsRegexp13() {
        String separator = "}";
        Assert.assertEquals("\"^([^\"+\"\\\\}\"+\"]*)\"+\"\\\\}\"+\"(.*)$\"",
                JobSettingsManager.FileSeparator.getSeparatorsRegexp(separator));
        separator = "}}";
        Assert.assertEquals("\"^([^\"+\"\\\\}\\\\}\"+\"]*)\"+\"\\\\}\\\\}\"+\"(.*)$\"",
                JobSettingsManager.FileSeparator.getSeparatorsRegexp(separator));
    }

    @Test
    public void testGetSeparatorsRegexp14() {
        String separator = "]";
        Assert.assertEquals("\"^([^\"+\"\\\\]\"+\"]*)\"+\"\\\\]\"+\"(.*)$\"",
                JobSettingsManager.FileSeparator.getSeparatorsRegexp(separator));
        separator = "]]";
        Assert.assertEquals("\"^([^\"+\"\\\\]\\\\]\"+\"]*)\"+\"\\\\]\\\\]\"+\"(.*)$\"",
                JobSettingsManager.FileSeparator.getSeparatorsRegexp(separator));
    }

    @Test
    public void testDoRegexpQuote4Null() {
        Assert.assertNull(JobSettingsManager.FileSeparator.doRegexpQuote(null));
    }

    @Test
    public void testDoRegexpQuote4EmptyCharacter() {
        String separator = "";
        Assert.assertEquals("", JobSettingsManager.FileSeparator.doRegexpQuote(separator));
    }

    @Test
    public void testDoRegexpQuote1() {
        String separator = "\\";
        Assert.assertEquals("\\\\\\\\", JobSettingsManager.FileSeparator.doRegexpQuote(separator));
        separator = "\\\\";
        Assert.assertEquals("\\\\\\\\\\\\\\\\", JobSettingsManager.FileSeparator.doRegexpQuote(separator));
    }

    @Test
    public void testDoRegexpQuote2() {
        String separator = "^";
        Assert.assertEquals("\\\\^", JobSettingsManager.FileSeparator.doRegexpQuote(separator));
        separator = "^^";
        Assert.assertEquals("\\\\^\\\\^", JobSettingsManager.FileSeparator.doRegexpQuote(separator));
    }

    @Test
    public void testDoRegexpQuote3() {
        String separator = "$";
        Assert.assertEquals("\\\\$", JobSettingsManager.FileSeparator.doRegexpQuote(separator));
        separator = "$$";
        Assert.assertEquals("\\\\$\\\\$", JobSettingsManager.FileSeparator.doRegexpQuote(separator));
    }

    @Test
    public void testDoRegexpQuote4() {
        String separator = ".";
        Assert.assertEquals("\\\\.", JobSettingsManager.FileSeparator.doRegexpQuote(separator));
        separator = "..";
        Assert.assertEquals("\\\\.\\\\.", JobSettingsManager.FileSeparator.doRegexpQuote(separator));
    }

    @Test
    public void testDoRegexpQuote5() {
        String separator = "?";
        Assert.assertEquals("\\\\?", JobSettingsManager.FileSeparator.doRegexpQuote(separator));
        separator = "??";
        Assert.assertEquals("\\\\?\\\\?", JobSettingsManager.FileSeparator.doRegexpQuote(separator));
    }

    @Test
    public void testDoRegexpQuote6() {
        String separator = "|";
        Assert.assertEquals("\\\\|", JobSettingsManager.FileSeparator.doRegexpQuote(separator));
        separator = "||";
        Assert.assertEquals("\\\\|\\\\|", JobSettingsManager.FileSeparator.doRegexpQuote(separator));
    }

    @Test
    public void testDoRegexpQuote7() {
        String separator = "[";
        Assert.assertEquals("\\\\[", JobSettingsManager.FileSeparator.doRegexpQuote(separator));
        separator = "[[";
        Assert.assertEquals("\\\\[\\\\[", JobSettingsManager.FileSeparator.doRegexpQuote(separator));
    }

    @Test
    public void testDoRegexpQuote8() {
        String separator = "+";
        Assert.assertEquals("\\\\+", JobSettingsManager.FileSeparator.doRegexpQuote(separator));
        separator = "++";
        Assert.assertEquals("\\\\+\\\\+", JobSettingsManager.FileSeparator.doRegexpQuote(separator));
    }

    @Test
    public void testDoRegexpQuote9() {
        String separator = "*";
        Assert.assertEquals("\\\\*", JobSettingsManager.FileSeparator.doRegexpQuote(separator));
        separator = "**";
        Assert.assertEquals("\\\\*\\\\*", JobSettingsManager.FileSeparator.doRegexpQuote(separator));
    }

    @Test
    public void testDoRegexpQuote10() {
        String separator = "{";
        Assert.assertEquals("\\\\{", JobSettingsManager.FileSeparator.doRegexpQuote(separator));
        separator = "{{";
        Assert.assertEquals("\\\\{\\\\{", JobSettingsManager.FileSeparator.doRegexpQuote(separator));
    }

    @Test
    public void testDoRegexpQuote11() {
        String separator = "(";
        Assert.assertEquals("\\\\(", JobSettingsManager.FileSeparator.doRegexpQuote(separator));
        separator = "((";
        Assert.assertEquals("\\\\(\\\\(", JobSettingsManager.FileSeparator.doRegexpQuote(separator));
    }

    @Test
    public void testDoRegexpQuote12() {
        String separator = ")";
        Assert.assertEquals("\\\\)", JobSettingsManager.FileSeparator.doRegexpQuote(separator));
        separator = "))";
        Assert.assertEquals("\\\\)\\\\)", JobSettingsManager.FileSeparator.doRegexpQuote(separator));
    }

    @Test
    public void testDoRegexpQuote13() {
        String separator = "}";
        Assert.assertEquals("\\\\}", JobSettingsManager.FileSeparator.doRegexpQuote(separator));
        separator = "}}";
        Assert.assertEquals("\\\\}\\\\}", JobSettingsManager.FileSeparator.doRegexpQuote(separator));
    }

    @Test
    public void testDoRegexpQuote14() {
        String separator = "]";
        Assert.assertEquals("\\\\]", JobSettingsManager.FileSeparator.doRegexpQuote(separator));
        separator = "]]";
        Assert.assertEquals("\\\\]\\\\]", JobSettingsManager.FileSeparator.doRegexpQuote(separator));
    }
}
