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
package org.talend.designer.core.model.process;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.draw2d.geometry.Point;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.talend.commons.utils.VersionUtils;
import org.talend.core.CorePlugin;
import org.talend.core.context.Context;
import org.talend.core.context.RepositoryContext;
import org.talend.core.model.components.ComponentCategory;
import org.talend.core.model.components.IComponent;
import org.talend.core.model.process.EConnectionType;
import org.talend.core.model.properties.PropertiesFactory;
import org.talend.core.model.properties.Property;
import org.talend.core.ui.component.ComponentsFactoryProvider;
import org.talend.designer.core.ui.editor.connections.Connection;
import org.talend.designer.core.ui.editor.nodes.Node;
import org.talend.designer.core.ui.editor.process.Process;

/**
 * DOC ycbai class global comment. Detailled comment
 */
public class ConnectionManagerTest {

    private static Connection connection;

    private static Connection newConnection;

    private static Node source;

    private static Node newSource;

    private static IComponent componentSource;

    private static IComponent componentTarget;

    private static Process sourceProcess;

    private static Process targetProcess;

    private static Node target;

    private static Node newTarget;

    private static Property createProperty() {
        Property property = PropertiesFactory.eINSTANCE.createProperty();
        property.setAuthor(((RepositoryContext) CorePlugin.getContext().getProperty(Context.REPOSITORY_CONTEXT_KEY)).getUser());
        property.setVersion(VersionUtils.DEFAULT_VERSION);
        property.setStatusCode(""); //$NON-NLS-1$

        return property;
    }

    /**
     * Changed by Marvin Wang on Feb.22 for bug TDI-19166.
     *
     * @throws Exception
     */
    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        componentSource = ComponentsFactoryProvider.getInstance().get("tFileInputDelimited",
                ComponentCategory.CATEGORY_4_DI.getName());
        componentTarget = ComponentsFactoryProvider.getInstance().get("tFileOutputDelimited",
                ComponentCategory.CATEGORY_4_DI.getName());

        sourceProcess = new Process(createProperty());
        targetProcess = new Process(createProperty());

        source = new Node(componentSource, sourceProcess);
        source.setLocation(new Point(1, 10));
        target = new Node(componentTarget, targetProcess);
        target.setLocation(new Point(100, 2));
        newTarget = new Node(componentTarget, targetProcess);
        newTarget.setLocation(new Point(150, 200));
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
    }

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
        if (connection != null) {
            connection.disconnect();
        }
        if (newConnection != null) {
            newConnection.disconnect();
        }
    }

    /**
     * Test method for
     * {@link org.talend.designer.core.model.process.ConnectionManager#canConnectToSource(org.talend.designer.core.ui.editor.nodes.Node, org.talend.designer.core.ui.editor.nodes.Node, org.talend.designer.core.ui.editor.nodes.Node, org.talend.core.model.process.EConnectionType, java.lang.String, java.lang.String)}
     * .
     */
    @Test
    public void testCanConnectToSource() {
        try {
            connection = new Connection(source, target, EConnectionType.FLOW_MAIN, EConnectionType.FLOW_MAIN.getName(), "test",
                    "test", "test", false);
            // Check the circle, certainly, you can try to test with some other conditions.
            boolean canConnect = ConnectionManager.canConnectToSource(source, target, target, EConnectionType.FLOW_MAIN,
                    EConnectionType.FLOW_MAIN.getName(), "test");
            // The right result is can't be connected.
            assertTrue(!canConnect);
        } catch (Exception e) {
            e.printStackTrace();
            fail("Test CanConnectToSource() method failure.");
        }
    }

    /**
     * Test method for
     * {@link org.talend.designer.core.model.process.ConnectionManager#canConnectToTarget(org.talend.designer.core.ui.editor.nodes.Node, org.talend.designer.core.ui.editor.nodes.Node, org.talend.designer.core.ui.editor.nodes.Node, org.talend.core.model.process.EConnectionType, java.lang.String, java.lang.String)}
     * .
     */
    @Test
    public void testCanConnectToTarget() {
        try {
            // First time, check the situation of creating the connection.
            boolean canConnect = ConnectionManager.canConnectToTarget(source, null, target, EConnectionType.FLOW_MAIN,
                    EConnectionType.FLOW_MAIN.getName(), "test");
            assertTrue(canConnect);
            // Second time, check the situation of reconnect the connection.
            connection = new Connection(source, target, EConnectionType.FLOW_MAIN, EConnectionType.FLOW_MAIN.getName(), "test",
                    "test", "test", false);
            canConnect = ConnectionManager.canConnectToTarget(source, target, newTarget, EConnectionType.FLOW_MAIN,
                    EConnectionType.FLOW_MAIN.getName(), "test");
            assertTrue(canConnect);
            newConnection = new Connection(target, newTarget, EConnectionType.FLOW_MAIN, EConnectionType.FLOW_MAIN.getName(),
                    "test2", "test2", "test2", false);
            canConnect = ConnectionManager.canConnectToTarget(target, newTarget, target, EConnectionType.FLOW_MAIN,
                    EConnectionType.FLOW_MAIN.getName(), "test2");
            assertTrue(!canConnect);
            canConnect = ConnectionManager.canConnectToTarget(target, newTarget, newTarget, EConnectionType.FLOW_MAIN,
                    EConnectionType.FLOW_MAIN.getName(), "test2");
            assertTrue(!canConnect);
        } catch (Exception e) {
            e.printStackTrace();
            fail("Test CanConnectToSource() method failure.");
        }
    }

    /**
     * Test method for
     * {@link org.talend.designer.core.model.process.ConnectionManager#canConnectToTarget(org.talend.designer.core.ui.editor.nodes.Node, org.talend.designer.core.ui.editor.nodes.Node, org.talend.designer.core.ui.editor.nodes.Node, org.talend.core.model.process.EConnectionType, java.lang.String, java.lang.String)}
     * .
     */
    @Test
    public void testCanConnectToTargetWithLoop1() {
        try {
            IComponent component = ComponentsFactoryProvider.getInstance()
                    .get("tJava", ComponentCategory.CATEGORY_4_DI.getName());
            Node source1 = new Node(component, sourceProcess);
            Node middle1 = new Node(component, sourceProcess);
            Node target1 = new Node(component, sourceProcess);

            Connection connection1 = new Connection(source1, middle1, EConnectionType.FLOW_MAIN,
                    EConnectionType.FLOW_MAIN.getName(), "test3", "test3", "test3", false);
            Connection connection2 = new Connection(middle1, target1, EConnectionType.FLOW_MAIN,
                    EConnectionType.FLOW_MAIN.getName(), "test4", "test4", "test4", false);

            boolean canConnect = ConnectionManager.canConnectToTarget(target1, null, source1, EConnectionType.FLOW_MAIN,
                    EConnectionType.FLOW_MAIN.getName(), "test_3_4");
            assertTrue(!canConnect);
        } catch (Exception e) {
            e.printStackTrace();
            fail("Test CanConnectToSource() method failure.");
        }
    }

    /**
     * Test method for
     * {@link org.talend.designer.core.model.process.ConnectionManager#canConnectToTarget(org.talend.designer.core.ui.editor.nodes.Node, org.talend.designer.core.ui.editor.nodes.Node, org.talend.designer.core.ui.editor.nodes.Node, org.talend.core.model.process.EConnectionType, java.lang.String, java.lang.String)}
     * .
     */
    @Test
    public void testCanConnectToTargetWithLoop2() {
        try {
            IComponent component = ComponentsFactoryProvider.getInstance()
                    .get("tJava", ComponentCategory.CATEGORY_4_DI.getName());
            Node source2 = new Node(component, sourceProcess);
            Node middle2 = new Node(component, sourceProcess);
            Node target2 = new Node(component, sourceProcess);
            Connection connection3 = new Connection(source2, middle2, EConnectionType.ON_SUBJOB_OK,
                    EConnectionType.ON_SUBJOB_OK.getName(), "test5", "test5", "test5", false);
            Connection connection4 = new Connection(middle2, target2, EConnectionType.ON_SUBJOB_ERROR,
                    EConnectionType.ON_SUBJOB_ERROR.getName(), "test6", "test6", "test6", false);
            boolean canConnect = ConnectionManager.canConnectToTarget(target2, null, source2, EConnectionType.RUN_IF,
                    EConnectionType.RUN_IF.getName(), "test_5_6");
            assertTrue(!canConnect);
        } catch (Exception e) {
            e.printStackTrace();
            fail("Test CanConnectToSource() method failure.");
        }
    }

    /**
     * Test method for
     * {@link org.talend.designer.core.model.process.ConnectionManager#canConnectToTarget(org.talend.designer.core.ui.editor.nodes.Node, org.talend.designer.core.ui.editor.nodes.Node, org.talend.designer.core.ui.editor.nodes.Node, org.talend.core.model.process.EConnectionType, java.lang.String, java.lang.String)}
     * .
     */
    @Test
    public void testCanConnectToTargetWithLoop3() {
        try {
            IComponent component = ComponentsFactoryProvider.getInstance()
                    .get("tJava", ComponentCategory.CATEGORY_4_DI.getName());
            Node source2 = new Node(component, sourceProcess);
            Node middle2 = new Node(component, sourceProcess);
            Node target2 = new Node(component, sourceProcess);
            Connection connection3 = new Connection(middle2, source2, EConnectionType.ON_SUBJOB_OK,
                    EConnectionType.ON_SUBJOB_OK.getName(), "test5", "test5", "test5", false);
            Connection connection4 = new Connection(middle2, target2, EConnectionType.ON_SUBJOB_ERROR,
                    EConnectionType.ON_SUBJOB_ERROR.getName(), "test6", "test6", "test6", false);
            boolean canConnect = ConnectionManager.canConnectToTarget(target2, null, source2, EConnectionType.RUN_IF,
                    EConnectionType.RUN_IF.getName(), "test_5_6");
            assertTrue(canConnect);
        } catch (Exception e) {
            e.printStackTrace();
            fail("Test CanConnectToSource() method failure.");
        }
    }

    /**
     * Test method for
     * {@link org.talend.designer.core.model.process.ConnectionManager#canConnectToTarget(org.talend.designer.core.ui.editor.nodes.Node, org.talend.designer.core.ui.editor.nodes.Node, org.talend.designer.core.ui.editor.nodes.Node, org.talend.core.model.process.EConnectionType, java.lang.String, java.lang.String)}
     * .
     */
    @Test
    public void testCanConnectToTargetELTMap() {
        try {
            IComponent component = ComponentsFactoryProvider.getInstance().get("tELTMysqlMap",
                    ComponentCategory.CATEGORY_4_DI.getName());

            Node targetELT = new Node(component, targetProcess);
            targetELT.setLocation(new Point(200, 2));

            // First time, check the situation of creating the connection.
            boolean canConnect = ConnectionManager.canConnectToTarget(source, null, targetELT, EConnectionType.ON_SUBJOB_OK,
                    EConnectionType.ON_SUBJOB_OK.getName(), "test");
            assertTrue(canConnect);
        } catch (Exception e) {
            e.printStackTrace();
            fail("Test CanConnectToSource() method failure.");
        }
    }

    /**
     * Test method for
     * {@link org.talend.designer.core.model.process.ConnectionManager#canRename(org.talend.designer.core.ui.editor.nodes.Node, org.talend.designer.core.ui.editor.nodes.Node, org.talend.core.model.process.EConnectionType, java.lang.String)}
     * .
     */
    @Test
    public void testCanRename() {
        try {
            connection = new Connection(source, target, EConnectionType.FLOW_MAIN, EConnectionType.FLOW_MAIN.getName(), "test",
                    "test", "test", false);
            boolean canRename = ConnectionManager.canRename(source, target, EConnectionType.FLOW_MAIN, "test1");
            assertTrue(canRename);
            canRename = ConnectionManager.canRename(source, target, EConnectionType.FLOW_MAIN, "test");
            assertTrue(canRename);
        } catch (Exception e) {
            e.printStackTrace();
            fail("Test CanConnectToSource() method failure.");
        }
    }

    @Test
    public void testCanConnectToSourceSHOW_IF() {
        // the NOT_SHOW_IF should only work with node's output connector required by DQ
        IComponent compMatchGroup = ComponentsFactoryProvider.getInstance().get("tMatchGroup",
                ComponentCategory.CATEGORY_4_DI.getName());

        Process process = new Process(createProperty());
        Node matchGroupNode = new Node(compMatchGroup, process);
        Node oldSourceNode = new Node(componentSource, process);
        Node outputNode = new Node(componentTarget, process);

        boolean canConnect = ConnectionManager.canConnectToSource(oldSourceNode, matchGroupNode, outputNode,
                EConnectionType.FLOW_MAIN, "FLOW_OUTPUT", "test");
        Assert.assertTrue(canConnect);
        canConnect = ConnectionManager.canConnectToSource(oldSourceNode, matchGroupNode, outputNode, EConnectionType.FLOW_MAIN,
                "UNIQUE_ROWS", "test");
        Assert.assertFalse(canConnect);
        canConnect = ConnectionManager.canConnectToSource(oldSourceNode, matchGroupNode, outputNode, EConnectionType.FLOW_MAIN,
                "CONFIDENT_GROUPS", "test");
        Assert.assertFalse(canConnect);
        canConnect = ConnectionManager.canConnectToSource(oldSourceNode, matchGroupNode, outputNode, EConnectionType.FLOW_MAIN,
                "UNCERTAIN_GROUPS", "test");
        Assert.assertFalse(canConnect);

        // change the NOT_SHOW_IF condition as true to change show output connector for tMatchGroup
        matchGroupNode.getElementParameter("SEPARATE_OUTPUT").setValue(true);
        canConnect = ConnectionManager.canConnectToSource(oldSourceNode, matchGroupNode, outputNode, EConnectionType.FLOW_MAIN,
                "FLOW_OUTPUT", "test");
        Assert.assertFalse(canConnect);

        canConnect = ConnectionManager.canConnectToSource(oldSourceNode, matchGroupNode, outputNode, EConnectionType.FLOW_MAIN,
                "UNIQUE_ROWS", "test");
        Assert.assertTrue(canConnect);
        canConnect = ConnectionManager.canConnectToSource(oldSourceNode, matchGroupNode, outputNode, EConnectionType.FLOW_MAIN,
                "CONFIDENT_GROUPS", "test");
        Assert.assertTrue(canConnect);
        canConnect = ConnectionManager.canConnectToSource(oldSourceNode, matchGroupNode, outputNode, EConnectionType.FLOW_MAIN,
                "UNCERTAIN_GROUPS", "test");
        Assert.assertTrue(canConnect);
    }

    @Test
    public void testCanConnectToTargetSHOW_IF() {
        // the NOT_SHOW_IF should only work with node's output connector but not affact input conector required by DQ
        IComponent compMatchGroup = ComponentsFactoryProvider.getInstance().get("tMatchGroup",
                ComponentCategory.CATEGORY_4_DI.getName());

        Process process = new Process(createProperty());
        Node sourceNode = new Node(componentSource, process);
        Node matchGroupNode = new Node(compMatchGroup, process);

        boolean canConnect = ConnectionManager.canConnectToTarget(sourceNode, matchGroupNode, matchGroupNode,
                EConnectionType.FLOW_MAIN, EConnectionType.FLOW_MAIN.getName(), "test");
        Assert.assertTrue(canConnect);

        // change the NOT_SHOW_IF condition as true won't affect tMatchGroup's input connector
        matchGroupNode.getElementParameter("SEPARATE_OUTPUT").setValue(true);
        canConnect = ConnectionManager.canConnectToTarget(sourceNode, matchGroupNode, matchGroupNode, EConnectionType.FLOW_MAIN,
                EConnectionType.FLOW_MAIN.getName(), "test");
        Assert.assertTrue(canConnect);

        IComponent compMatchModel = ComponentsFactoryProvider.getInstance().get("tMatchModel",
                ComponentCategory.CATEGORY_4_SPARK.getName());
        Node matchModelNode = new Node(compMatchModel, process);
        canConnect = ConnectionManager.canConnectToTarget(sourceNode, matchModelNode, matchModelNode, EConnectionType.FLOW_MAIN,
                EConnectionType.FLOW_MAIN.getName(), "test");
        Assert.assertTrue(canConnect);

        matchModelNode.getElementParameter("IS_USING_TDS").setValue(true);
        canConnect = ConnectionManager.canConnectToTarget(sourceNode, matchModelNode, matchModelNode, EConnectionType.FLOW_MAIN,
                EConnectionType.FLOW_MAIN.getName(), "test");
        Assert.assertFalse(canConnect);

    }

    @Test
    public void testCheckCircle() {
        Process process = new Process(createProperty());
        IComponent component = ComponentsFactoryProvider.getInstance().get("tJava", ComponentCategory.CATEGORY_4_DI.getName());
        Node sourceNode = new Node(componentSource, process);
        Node targetNode = new Node(componentTarget, process);
        Node middleNode_1 = new Node(component, process);
        Node middleNode_2 = new Node(component, process);
        Node middleNode_3 = new Node(component, process);

        // sourceNode -> middleNode_1 -> targetNode
        resetNodeConnection(sourceNode);
        resetNodeConnection(middleNode_1);
        resetNodeConnection(targetNode);
        ((List<Connection>) middleNode_1.getIncomingConnections()).add(new Connection(sourceNode, middleNode_1,
                EConnectionType.FLOW_MAIN, EConnectionType.FLOW_MAIN.getName(), "testRow", "testRow", "testRow", false));
        ((List<Connection>) targetNode.getIncomingConnections()).add(new Connection(middleNode_1, targetNode,
                EConnectionType.FLOW_MAIN, EConnectionType.FLOW_MAIN.getName(), "testRow", "testRow", "testRow", false));
        Assert.assertFalse(ConnectionManager.checkCircle(middleNode_1, targetNode));

        // TUP-22620 source==target, no incomming connections
        Assert.assertTrue(ConnectionManager.checkCircle(sourceNode, sourceNode));

        // for circle connection, test checkCircle by canConnectToTrget
        // sourceNode -> targetNode -> sourceNode
        resetNodeConnection(sourceNode);
        resetNodeConnection(middleNode_1);
        resetNodeConnection(targetNode);
        ((List<Connection>) targetNode.getIncomingConnections()).add(new Connection(sourceNode, targetNode,
                EConnectionType.FLOW_MAIN, EConnectionType.FLOW_MAIN.getName(), "testRow", "testRow", "testRow", false));
        Assert.assertFalse(ConnectionManager.canConnectToTarget(targetNode, null, sourceNode, EConnectionType.FLOW_MAIN,
                EConnectionType.FLOW_MAIN.getName(), "testRow"));

        // sourceNode -> middleNode_1 -> middleNode_2 -> middleNode_3 -> middleNode_1 -> targetNode
        resetNodeConnection(sourceNode);
        resetNodeConnection(targetNode);
        ((List<Connection>) middleNode_1.getIncomingConnections()).add(new Connection(sourceNode, middleNode_1,
                EConnectionType.FLOW_MAIN, EConnectionType.FLOW_MAIN.getName(), "testRow", "testRow", "testRow", false));
        ((List<Connection>) middleNode_2.getIncomingConnections()).add(new Connection(middleNode_1, middleNode_2,
                EConnectionType.FLOW_MAIN, EConnectionType.FLOW_MAIN.getName(), "testRow", "testRow", "testRow", false));

        ((List<Connection>) middleNode_1.getIncomingConnections()).add(new Connection(middleNode_3, middleNode_1,
                EConnectionType.FLOW_MAIN, EConnectionType.FLOW_MAIN.getName(), "testRow", "testRow", "testRow", false));
        ((List<Connection>) targetNode.getIncomingConnections()).add(new Connection(middleNode_1, targetNode,
                EConnectionType.FLOW_MAIN, EConnectionType.FLOW_MAIN.getName(), "testRow", "testRow", "testRow", false));
        Assert.assertFalse(ConnectionManager.canConnectToTarget(middleNode_2, null, middleNode_3, EConnectionType.FLOW_MAIN,
                EConnectionType.FLOW_MAIN.getName(), "testRow"));

    }

    private static void resetNodeConnection(Node node) {
        List<Connection> inconnections = new ArrayList<Connection>();
        node.setIncomingConnections(inconnections);
        List<Connection> outconnections = new ArrayList<Connection>();
        node.setOutgoingConnections(outconnections);
    }

}
