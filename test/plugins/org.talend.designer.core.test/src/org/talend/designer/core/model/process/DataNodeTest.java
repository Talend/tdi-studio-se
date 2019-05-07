// ============================================================================
//
// Copyright (C) 2006-2018 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.designer.core.model.process;

import java.util.List;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.talend.core.model.components.ComponentCategory;
import org.talend.core.model.components.IComponent;
import org.talend.core.model.process.EConnectionType;
import org.talend.core.model.process.IConnection;
import org.talend.core.model.process.INode;
import org.talend.core.ui.component.ComponentsFactoryProvider;

/**
 * DOC jding class global comment. Detailled comment
 */
public class DataNodeTest {

    private static final String TEST_COMPONENT_NAME = "tMysqlInput";

    private DataNode node1;

    private DataNode node2;

    private DataNode node3;

    private DataNode tmapNode1;

    private DataNode tmapNode2;

    private DataNode logRowNode;

    @Before
    public void before() {
        IComponent testComponent = ComponentsFactoryProvider.getInstance().get(TEST_COMPONENT_NAME,
                ComponentCategory.CATEGORY_4_DI.getName());
        IComponent tmapComponent = ComponentsFactoryProvider.getInstance().get("tMap", ComponentCategory.CATEGORY_4_DI.getName());
        IComponent logrowComponent = ComponentsFactoryProvider.getInstance().get("tLogRow",
                ComponentCategory.CATEGORY_4_DI.getName());
        node1 = new DataNode(testComponent, TEST_COMPONENT_NAME + "_1");
        node2 = new DataNode(testComponent, TEST_COMPONENT_NAME + "_2");
        node3 = new DataNode(testComponent, TEST_COMPONENT_NAME + "_3");
        tmapNode1 = new DataNode(tmapComponent, "tMap_1");
        tmapNode2 = new DataNode(tmapComponent, "tMap_2");
        logRowNode = new DataNode(logrowComponent, "tLogRow_1");

        DataConnection line1 = createDataConnection(node1, tmapNode1, EConnectionType.FLOW_MAIN);
        ((List<IConnection>) node1.getOutgoingConnections()).add(line1);
        ((List<IConnection>) tmapNode1.getIncomingConnections()).add(line1);

        DataConnection line2 = createDataConnection(node2, tmapNode1, EConnectionType.FLOW_REF);
        ((List<IConnection>) node2.getOutgoingConnections()).add(line2);
        ((List<IConnection>) tmapNode1.getIncomingConnections()).add(line2);

        DataConnection line3 = createDataConnection(node3, tmapNode2, EConnectionType.FLOW_MAIN);
        ((List<IConnection>) node3.getOutgoingConnections()).add(line3);
        ((List<IConnection>) tmapNode2.getIncomingConnections()).add(line3);

        DataConnection line4 = createDataConnection(tmapNode1, tmapNode2, EConnectionType.FLOW_REF);
        ((List<IConnection>) tmapNode1.getOutgoingConnections()).add(line4);
        ((List<IConnection>) tmapNode2.getIncomingConnections()).add(line4);

        DataConnection runAfter1 = createDataConnection(node3, node1, EConnectionType.RUN_AFTER);
        ((List<IConnection>) node3.getOutgoingConnections()).add(runAfter1);
        ((List<IConnection>) node1.getIncomingConnections()).add(runAfter1);

        DataConnection runAfter2 = createDataConnection(node1, node2, EConnectionType.RUN_AFTER);
        ((List<IConnection>) node1.getOutgoingConnections()).add(runAfter2);
        ((List<IConnection>) node2.getIncomingConnections()).add(runAfter2);

        DataConnection line5 = createDataConnection(tmapNode2, logRowNode, EConnectionType.FLOW_MAIN);
        ((List<IConnection>) tmapNode2.getOutgoingConnections()).add(line5);
        ((List<IConnection>) logRowNode.getIncomingConnections()).add(line5);

    }

    @Test
    public void testGetSubProcessStartNode() {
        INode startNode = node1.getSubProcessStartNode(false);
        Assert.assertEquals(startNode, node3);
        startNode = node2.getSubProcessStartNode(false);
        Assert.assertEquals(startNode, node3);
        startNode = node3.getSubProcessStartNode(false);
        Assert.assertEquals(startNode, node3);
        startNode = tmapNode1.getSubProcessStartNode(false);
        Assert.assertEquals(startNode, node3);
        startNode = tmapNode2.getSubProcessStartNode(false);
        Assert.assertEquals(startNode, node3);
        startNode = logRowNode.getSubProcessStartNode(false);
        Assert.assertEquals(startNode, node3);
    }

    @After
    public void after() {
        node1 = null;
        node2 = null;
        node3 = null;
        tmapNode1 = null;
        tmapNode2 = null;
        logRowNode = null;
    }

    private DataConnection createDataConnection(DataNode source, DataNode target, EConnectionType lineStyle) {
        DataConnection dataConnec = new DataConnection();
        dataConnec.setActivate(true);
        dataConnec.setLineStyle(lineStyle);
        dataConnec.setConnectorName("FLOW");
        dataConnec.setName("row_" + source.getUniqueName());
        dataConnec.setSource(source);
        dataConnec.setTarget(target);
        return dataConnec;
    }

}
