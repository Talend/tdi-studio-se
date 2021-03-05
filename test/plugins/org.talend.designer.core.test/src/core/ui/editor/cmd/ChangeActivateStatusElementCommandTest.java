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
package core.ui.editor.cmd;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.talend.core.model.components.ComponentCategory;
import org.talend.core.model.components.IComponent;
import org.talend.core.model.process.EConnectionType;
import org.talend.core.model.process.IConnection;
import org.talend.core.model.process.INode;
import org.talend.core.model.properties.PropertiesFactory;
import org.talend.core.model.properties.Property;
import org.talend.core.ui.component.ComponentsFactoryProvider;
import org.talend.designer.core.ui.editor.cmd.ChangeActivateStatusElementCommand;
import org.talend.designer.core.ui.editor.connections.Connection;
import org.talend.designer.core.ui.editor.nodes.Node;
import org.talend.designer.core.ui.editor.process.Process;

/**
 * DOC jding  class global comment. Detailled comment
 */
public class ChangeActivateStatusElementCommandTest {

    @Test
    public void testGetExactConnectionsBetweenNodes() {
        String nodestr = "tMysqlInput";
        String nodestr1 = "tLogRow";
        IComponent tMysqlComponent = ComponentsFactoryProvider.getInstance().get(nodestr,
                ComponentCategory.CATEGORY_4_DI.getName());
        IComponent tLogRowComponent = ComponentsFactoryProvider.getInstance().get(nodestr1,
                ComponentCategory.CATEGORY_4_DI.getName());
        Property property = PropertiesFactory.eINSTANCE.createProperty();
        Process process = new Process(property);

        // Flow1 BaseNode->Node1->Node2
        Node baseNode = new Node(tMysqlComponent, process);
        Node node1 = new Node(tLogRowComponent, process);
        Node node2 = new Node(tLogRowComponent, process);
        IConnection conn1 = createConnection(baseNode, node1);
        ((List<IConnection>) baseNode.getOutgoingConnections()).add(conn1);
        ((List<IConnection>) node1.getIncomingConnections()).add(conn1);
        IConnection conn2 = createConnection(node1, node2);
        ((List<IConnection>) node1.getOutgoingConnections()).add(conn2);
        ((List<IConnection>) node2.getIncomingConnections()).add(conn2);

        // Flow2 BaseNode->Node3->Node4
        Node node3 = new Node(tLogRowComponent, process);
        Node node4 = new Node(tLogRowComponent, process);
        IConnection conn3 = createConnection(baseNode, node3);
        ((List<IConnection>) baseNode.getOutgoingConnections()).add(conn3);
        ((List<IConnection>) node3.getIncomingConnections()).add(conn3);
        IConnection conn4 = createConnection(node3, node4);
        ((List<IConnection>) node3.getOutgoingConnections()).add(conn4);
        ((List<IConnection>) node4.getIncomingConnections()).add(conn4);

        // Flow3 BaseNode->Node5->Node6
        Node node5 = new Node(tLogRowComponent, process);
        Node node6 = new Node(tLogRowComponent, process);
        IConnection conn5 = createConnection(baseNode, node5);
        ((List<IConnection>) baseNode.getOutgoingConnections()).add(conn5);
        ((List<IConnection>) node5.getIncomingConnections()).add(conn5);
        IConnection conn6 = createConnection(node5, node6);
        ((List<IConnection>) node5.getOutgoingConnections()).add(conn6);
        ((List<IConnection>) node6.getIncomingConnections()).add(conn6);

        // case1 All activate
        List<INode> nodeList = new ArrayList<INode>();
        nodeList.add(baseNode);
        nodeList.add(node1);
        nodeList.add(node3);
        nodeList.add(node5);
        List<IConnection> connList = new ArrayList<IConnection>();
        // connList conn1,conn3,conn5
        connList.add(conn1);
        connList.add(conn3);
        connList.add(conn5);
        List<IConnection> exactConnection = ChangeActivateStatusElementCommand.getExactConnectionsBetweenNodes(baseNode, nodeList,
                connList, true);
        Assert.assertTrue(exactConnection.size() == 3);
        Assert.assertTrue(exactConnection.contains(conn1));
        Assert.assertTrue(exactConnection.contains(conn3));
        Assert.assertTrue(exactConnection.contains(conn5));

        // case2 Flow1 Flow3 deactivate
        nodeList = new ArrayList<INode>();
        nodeList.add(baseNode);
        nodeList.add(node3);
        // connList conn1,conn2,conn3,conn5,conn6
        connList.add(conn2);
        connList.add(conn6);
        List<IConnection> exactConnection1 = ChangeActivateStatusElementCommand.getExactConnectionsBetweenNodes(baseNode,
                nodeList,
                connList, true);
        Assert.assertTrue(exactConnection1.size() == 1);
        Assert.assertTrue(exactConnection1.contains(conn3));

        // case3 Flow1 deactivate, Node3 & Node5 deactivate
        nodeList = new ArrayList<INode>();
        nodeList.add(baseNode);
        nodeList.add(node4);
        nodeList.add(node6);
        // connList conn1,conn2,conn3,conn4,conn5,conn6
        connList.add(conn4);
        List<IConnection> exactConnection2 = ChangeActivateStatusElementCommand.getExactConnectionsBetweenNodes(baseNode,
                nodeList, connList, true);
        Assert.assertTrue(exactConnection2.size() == 4);
        Assert.assertTrue(exactConnection2.contains(conn3));
        Assert.assertTrue(exactConnection2.contains(conn4));
        Assert.assertTrue(exactConnection2.contains(conn5));
        Assert.assertTrue(exactConnection2.contains(conn6));

        // case4 Flow2 inComing, Node3 deactivate
        nodeList = new ArrayList<INode>();
        nodeList.add(node4);
        nodeList.add(baseNode);
        List<IConnection> exactConnection3 = ChangeActivateStatusElementCommand.getExactConnectionsBetweenNodes(node4, nodeList,
                connList, false);
        Assert.assertTrue(exactConnection3.size() == 2);
        Assert.assertTrue(exactConnection2.contains(conn3));
        Assert.assertTrue(exactConnection2.contains(conn4));

    }

    private Connection createConnection(Node sourceNode, Node targetNode) {
        Connection connection = new Connection(sourceNode, targetNode, EConnectionType.FLOW_MAIN, false);
        return connection;
    }

}
