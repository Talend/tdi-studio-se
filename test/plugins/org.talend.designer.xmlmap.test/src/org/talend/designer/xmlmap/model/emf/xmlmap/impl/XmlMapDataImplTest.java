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
package org.talend.designer.xmlmap.model.emf.xmlmap.impl;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.talend.designer.xmlmap.model.emf.xmlmap.Connection;
import org.talend.designer.xmlmap.model.emf.xmlmap.FilterConnection;
import org.talend.designer.xmlmap.model.emf.xmlmap.InputXmlTree;
import org.talend.designer.xmlmap.model.emf.xmlmap.OutputTreeNode;
import org.talend.designer.xmlmap.model.emf.xmlmap.OutputXmlTree;
import org.talend.designer.xmlmap.model.emf.xmlmap.TreeNode;
import org.talend.designer.xmlmap.model.emf.xmlmap.VarNode;
import org.talend.designer.xmlmap.model.emf.xmlmap.VarTable;
import org.talend.designer.xmlmap.model.emf.xmlmap.XmlMapData;
import org.talend.designer.xmlmap.model.emf.xmlmap.XmlmapFactory;

/**
 * DOC hwang  class global comment. Detailled comment
 */
public class XmlMapDataImplTest {

    @Test
    public void testEquals() {
        XmlMapData data1 = XmlmapFactory.eINSTANCE.createXmlMapData();
        XmlMapData data2 = XmlmapFactory.eINSTANCE.createXmlMapData();
        assertTrue(data1.equals(data2));
        test1();
        test2();
        test3();
        test4();
    }

    private void test1(){
        XmlMapData data1 = createSparkData("diff","diff", "diff");
        XmlMapData data2 = createSparkData("diff","diff", "diff");
        assertTrue(data1.equals(data2));
    }

    private void test2(){
        XmlMapData data1 = createSparkData("diff","diff", "diff");
        XmlMapData data2 = createSparkData("diff1","diff", "diff");
        assertFalse(data1.equals(data2));
    }

    private void test3(){
        XmlMapData data1 = createSparkData("diff","diff", "diff");
        XmlMapData data2 = createSparkData("diff","diff1", "diff");
        assertFalse(data1.equals(data2));
    }

    private void test4(){
        XmlMapData data1 = createSparkData("diff","diff", "diff");
        XmlMapData data2 = createSparkData("diff","diff", "diff1");
        assertFalse(data1.equals(data2));
    }

    private XmlMapData createSparkData(String inputDiff,String outputDiff, String varDiff){
        XmlMapData data = XmlmapFactory.eINSTANCE.createXmlMapData();
        //
        InputXmlTree inputTree = XmlmapFactory.eINSTANCE.createInputXmlTree();
        inputTree.setName("input_1");
        inputTree.setLookup(true);
        inputTree.setInnerJoin(false);
        inputTree.setLookupMode("LOAD_ONCE");
        inputTree.setMatchingMode("ALL_ROWS");
        inputTree.setMinimized(false);
        inputTree.setMultiLoops(false);
        inputTree.setPersistent(false);
        data.getInputTrees().add(inputTree);

        TreeNode treeNode = XmlmapFactory.eINSTANCE.createTreeNode();
        treeNode.setName("inputTreeNode");
        treeNode.setXpath("row1/newColumn");
        treeNode.setExpression(inputDiff);
        treeNode.setType("id_String");
        treeNode.setLoop(false);
        treeNode.setPattern(inputDiff);
        treeNode.setKey(false);
        treeNode.setGroup(false);
        treeNode.setMain(false);
        treeNode.setDefaultValue("value");
        treeNode.setNullable(false);
        treeNode.setChoice(false);
        treeNode.setSubstitution(false);
        treeNode.setOptional(false);
        treeNode.setNodeType(org.talend.designer.xmlmap.model.emf.xmlmap.NodeType.ATTRIBUT);
        inputTree.getNodes().add(treeNode);

        OutputXmlTree outputTree = XmlmapFactory.eINSTANCE.createOutputXmlTree();
        outputTree.setName("output_1");
        outputTree.setReject(false);
        outputTree.setRejectInnerJoin(false);
        outputTree.setErrorReject(false);
        outputTree.setAllInOne(false);
        outputTree.setEnableEmptyElement(false);
        data.getOutputTrees().add(outputTree);

        OutputTreeNode treeNode2 = XmlmapFactory.eINSTANCE.createOutputTreeNode();
        treeNode2.setName("outputTreeNode");
        treeNode2.setXpath("out/newColumn");
        treeNode2.setType("id_String");
        treeNode2.setAggregate(false);
        treeNode2.setLoop(false);
        treeNode2.setPattern(outputDiff);
        treeNode2.setExpression(outputDiff);
        treeNode2.setKey(false);
        treeNode2.setGroup(false);
        treeNode2.setMain(false);
        treeNode2.setDefaultValue("value");
        treeNode2.setNullable(false);
        treeNode2.setChoice(false);
        treeNode2.setSubstitution(false);
        treeNode2.setOptional(false);
        treeNode2.setNodeType(org.talend.designer.xmlmap.model.emf.xmlmap.NodeType.ATTRIBUT);
        outputTree.getNodes().add(treeNode2);

        VarTable vatTable = XmlmapFactory.eINSTANCE.createVarTable();
        vatTable.setName("varTable");
        vatTable.setMinimized(true);

        VarNode varNode = XmlmapFactory.eINSTANCE.createVarNode();
        varNode.setName("varNode");
        varNode.setNullable(false);
        varNode.setType("id_String");
        varNode.setExpression(varDiff);
        vatTable.getNodes().add(varNode);
        data.getVarTables().add(vatTable);

        Connection connection = XmlmapFactory.eINSTANCE.createConnection();
        connection.setSource(treeNode);
        connection.setTarget(treeNode2);
        data.getConnections().add(connection);
        treeNode.getOutgoingConnections().add(connection);
        treeNode2.getIncomingConnections().add(connection);

        FilterConnection fc = XmlmapFactory.eINSTANCE.createFilterConnection();
        fc.setSource(treeNode);
        fc.setTarget(outputTree);
        data.getConnections().add(fc);
        treeNode.getFilterOutGoingConnections().add(fc);
        outputTree.getFilterIncomingConnections().add(fc);
        return data;
    }
}
