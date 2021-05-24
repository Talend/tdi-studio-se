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
package org.talend.designer.xmlmap.util;

import static org.junit.Assert.*;

import org.junit.Test;
import org.talend.core.model.metadata.builder.connection.ConnectionFactory;
import org.talend.core.model.metadata.builder.connection.MetadataColumn;
import org.talend.core.model.metadata.builder.connection.MetadataTable;
import org.talend.core.model.metadata.builder.connection.XMLFileNode;
import org.talend.designer.xmlmap.model.emf.xmlmap.AbstractNode;
import org.talend.designer.xmlmap.model.emf.xmlmap.Connection;
import org.talend.designer.xmlmap.model.emf.xmlmap.InputXmlTree;
import org.talend.designer.xmlmap.model.emf.xmlmap.NodeType;
import org.talend.designer.xmlmap.model.emf.xmlmap.OutputTreeNode;
import org.talend.designer.xmlmap.model.emf.xmlmap.OutputXmlTree;
import org.talend.designer.xmlmap.model.emf.xmlmap.TreeNode;
import org.talend.designer.xmlmap.model.emf.xmlmap.XmlMapData;
import org.talend.designer.xmlmap.model.emf.xmlmap.XmlmapFactory;
import org.talend.designer.xmlmap.ui.expressionutil.XmlMapExpressionManager;

/**
 * DOC talend class global comment. Detailled comment
 */
public class XmlMapUtilTest {

    /**
     * Test method for
     * {@link org.talend.designer.xmlmap.util.XmlMapUtil#getXPath(java.lang.String, java.lang.String, org.talend.designer.xmlmap.model.emf.xmlmap.NodeType)}
     * .
     */
    @Test
    public void testGetXPath() {
        String parentXpath = "row1.newColumn:/schema/column";
        String label = "originalDbColumnName";
        NodeType nodeType = NodeType.ELEMENT;
        String expectedXpath = "row1.newColumn:/schema/column/originalDbColumnName";
        String xpath = XmlMapUtil.getXPath(parentXpath, label, nodeType);
        assertTrue(expectedXpath.equals(xpath));

        nodeType = NodeType.ATTRIBUT;
        expectedXpath = "row1.newColumn:/schema/column/@originalDbColumnName";
        xpath = XmlMapUtil.getXPath(parentXpath, label, nodeType);
        assertTrue(expectedXpath.equals(xpath));

        nodeType = NodeType.NAME_SPACE;
        expectedXpath = "row1.newColumn:/schema/column/xmlns:originalDbColumnName";
        xpath = XmlMapUtil.getXPath(parentXpath, label, nodeType);
        assertTrue(expectedXpath.equals(xpath));
    }

    /**
     * Test method for {@link org.talend.designer.xmlmap.util.XmlMapUtil#convertToExpression(java.lang.String)}.
     */
    @Test
    public void testConvertToExpression() {
        String xpath = "row1.newColumn:/schema/column/@originalDbColumnName";
        String expectedExpression = "[row1.newColumn:/schema/column/@originalDbColumnName]";
        String expression = XmlMapUtil.convertToExpression(xpath);
        assertTrue(expectedExpression.equals(expression));
        xpath = "row1/newColumn";
        expectedExpression = "row1.newColumn";
        expression = XmlMapUtil.convertToExpression(xpath);
        assertTrue(expectedExpression.equals(expression));
    }

    /**
     * Test method for {@link org.talend.designer.xmlmap.util.XmlMapUtil#convertToXpath(java.lang.String)}.
     */
    @Test
    public void testConvertToXpath() {
        String expression = "[row1.newColumn:/schema/column/@originalDbColumnName]";
        String expectedXpath = "row1.newColumn:/schema/column/@originalDbColumnName";
        String xpath = XmlMapUtil.convertToXpath(expression);
        assertTrue(expectedXpath.equals(xpath));

        expression = "row1.newColumn";
        expectedXpath = "row1/newColumn";
        xpath = XmlMapUtil.convertToXpath(expression);
        assertTrue(expectedXpath.equals(xpath));
    }

    @Test
    public void testUpdateXPathAndExpression() {
        XmlMapData data = XmlmapFactory.eINSTANCE.createXmlMapData();
        InputXmlTree inputTree = XmlmapFactory.eINSTANCE.createInputXmlTree();
        inputTree.setName("row1");
        OutputXmlTree outputTree = XmlmapFactory.eINSTANCE.createOutputXmlTree();
        outputTree.setName("out1");
        data.getInputTrees().add(inputTree);
        data.getOutputTrees().add(outputTree);

        TreeNode doc = XmlmapFactory.eINSTANCE.createTreeNode();
        initTreeNode(doc, "doc", "id_Document", NodeType.ELEMENT, "row1/doc", "");
        inputTree.getNodes().add(doc);

        TreeNode school = XmlmapFactory.eINSTANCE.createTreeNode();
        initTreeNode(school, "school", "id_String", NodeType.ELEMENT, "row1.doc:/school", "");
        doc.getChildren().add(school);

        TreeNode student = XmlmapFactory.eINSTANCE.createTreeNode();
        initTreeNode(student, "student", "id_String", NodeType.ELEMENT, "row1.doc:/school/student", "");
        school.getChildren().add(student);

        TreeNode name = XmlmapFactory.eINSTANCE.createTreeNode();
        initTreeNode(name, "name", "id_String", NodeType.ATTRIBUT, "row1.doc:/school/student/@name", "");
        student.getChildren().add(name);

        OutputTreeNode outColumn = XmlmapFactory.eINSTANCE.createOutputTreeNode();
        initTreeNode(outColumn, "outColumn", "id_String", NodeType.ELEMENT, "out1/outColumn", "[row1.doc:/school/student/@name]");
        outputTree.getNodes().add(outColumn);
        createConnection(data, name, outColumn);

        // rename schema column
        doc.setName("document");
        XmlMapUtil.updateXPathAndExpression(data, new XmlMapExpressionManager(), doc, "document", 2, true);
        assertTrue(doc.getXpath().equals("row1/document"));
        assertTrue(school.getXpath().equals("row1.document:/school"));
        assertTrue(student.getXpath().equals("row1.document:/school/student"));
        assertTrue(name.getXpath().equals("row1.document:/school/student/@name"));
        assertTrue(outColumn.getExpression().equals("[row1.document:/school/student/@name]"));
        // rename doc child
        school.setName("kindergarten");
        XmlMapUtil.updateXPathAndExpression(data, new XmlMapExpressionManager(), school, "kindergarten", 3, true);
        assertTrue(doc.getXpath().equals("row1/document"));
        assertTrue(school.getXpath().equals("row1.document:/kindergarten"));
        assertTrue(student.getXpath().equals("row1.document:/kindergarten/student"));
        assertTrue(name.getXpath().equals("row1.document:/kindergarten/student/@name"));
        assertTrue(outColumn.getExpression().equals("[row1.document:/kindergarten/student/@name]"));

    }

    private void createConnection(XmlMapData data, AbstractNode sourceNode, AbstractNode targetNode) {
        Connection conn = XmlmapFactory.eINSTANCE.createConnection();
        conn.setSource(sourceNode);
        conn.setTarget(targetNode);
        targetNode.getIncomingConnections().add(conn);
        sourceNode.getOutgoingConnections().add(conn);
        if (data != null) {
            data.getConnections().add(conn);
        }
    }

    private void initTreeNode(TreeNode doc, String name, String type, NodeType nodeType, String xpath, String expression) {
        doc.setName(name);
        doc.setType(type);
        doc.setNodeType(nodeType);
        doc.setXpath(xpath);
        doc.setExpression(expression);
    }

    @Test
    public void testGetColumnPatternFromMetadataTable() {
        XMLFileNode node = ConnectionFactory.eINSTANCE.createXMLFileNode();
        MetadataTable testTable = createMetaTable("testTable"); //$NON-NLS-1$

        String columnPattern = XmlMapUtil.getColumnPatternFromMetadataTable(node, testTable);
        assertNull(columnPattern);

        MetadataColumn column1 = createMetaColumn("C1"); //$NON-NLS-1$
        MetadataColumn column2 = createMetaColumn("C2"); //$NON-NLS-1$
        testTable.getColumns().add(column1);
        testTable.getColumns().add(column2);
        columnPattern = XmlMapUtil.getColumnPatternFromMetadataTable(node, testTable);
        assertNull(columnPattern);

        node.setRelatedColumn("C1"); //$NON-NLS-1$
        columnPattern = XmlMapUtil.getColumnPatternFromMetadataTable(node, testTable);
        assertNull(columnPattern);

        column1.setPattern(""); //$NON-NLS-1$
        columnPattern = XmlMapUtil.getColumnPatternFromMetadataTable(node, testTable);
        assertNull(columnPattern);

        column1.setPattern("P1"); //$NON-NLS-1$
        columnPattern = XmlMapUtil.getColumnPatternFromMetadataTable(node, testTable);
        assertEquals("P1", columnPattern); //$NON-NLS-1$

        node.setRelatedColumn("C3"); //$NON-NLS-1$
        columnPattern = XmlMapUtil.getColumnPatternFromMetadataTable(node, testTable);
        assertNull(columnPattern);
    }

    private MetadataTable createMetaTable(String tableName) {
        MetadataTable table = ConnectionFactory.eINSTANCE.createMetadataTable();
        table.setName(tableName);
        table.setLabel(tableName);
        return table;
    }

    private MetadataColumn createMetaColumn(String columnName) {
        MetadataColumn column = ConnectionFactory.eINSTANCE.createMetadataColumn();
        column.setName(columnName);
        column.setLabel(columnName);
        column.setTalendType("id_String"); //$NON-NLS-1$
        return column;
    }

}
