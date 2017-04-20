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
package org.talend.designer.core.ui.editor.nodes;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.talend.core.model.components.ComponentCategory;
import org.talend.core.model.components.IComponent;
import org.talend.core.model.process.EComponentCategory;
import org.talend.core.model.process.EConnectionType;
import org.talend.core.model.process.EParameterFieldType;
import org.talend.core.model.process.IElementParameter;
import org.talend.core.model.process.INodeConnector;
import org.talend.core.model.repository.FakePropertyImpl;
import org.talend.core.ui.component.ComponentsFactoryProvider;
import org.talend.designer.core.model.components.ElementParameter;
import org.talend.designer.core.model.components.NodeConnector;
import org.talend.designer.core.ui.editor.process.Process;

/**
 * created by wchen on Apr 17, 2017 Detailled comment
 *
 */
public class NodeTest {

    @Test
    public void testGetNodeConnectorsShowIf1FlowConnector() {
        Process process = new Process(new FakePropertyImpl());
        IComponent sourceCom = ComponentsFactoryProvider.getInstance().get("tMysqlInput",
                ComponentCategory.CATEGORY_4_DI.getName());
        Node node = new Node(sourceCom, process);
        // create node connector for test
        List<INodeConnector> listConnector = new ArrayList<>();
        NodeConnector connector = new NodeConnector(node);
        connector.setDefaultConnectionType(EConnectionType.FLOW_MAIN);
        connector.setName(EConnectionType.FLOW_MAIN.getName());
        connector.setMaxLinkOutput(1);
        connector.setShowIf("SHOW_FLOW_CONNECTOR == 'true'");
        listConnector.add(connector);
        node.setListConnector(listConnector);
        // create a test param with default value 'false'
        IElementParameter param = addShowIfParam(node);

        Assert.assertNull(node.getConnectorFromName(EConnectionType.FLOW_MAIN.getName()));
        Assert.assertNull(node.getConnectorFromType(EConnectionType.FLOW_MAIN));
        Assert.assertEquals(node.getConnectorsFromType(EConnectionType.FLOW_MAIN).size(), 0);

        // make connector show if to 'true'
        param.setValue(true);
        Assert.assertNotNull(node.getConnectorFromName(EConnectionType.FLOW_MAIN.getName()));
        Assert.assertNotNull(node.getConnectorFromType(EConnectionType.FLOW_MAIN));
        Assert.assertEquals(node.getConnectorsFromType(EConnectionType.FLOW_MAIN).size(), 1);
    }

    @Test
    public void testGetNodeConnectorsShowIf2FlowConnector() {
        Process process = new Process(new FakePropertyImpl());
        IComponent sourceCom = ComponentsFactoryProvider.getInstance().get("tMysqlInput",
                ComponentCategory.CATEGORY_4_DI.getName());
        Node node = new Node(sourceCom, process);
        // create node connector for test
        List<INodeConnector> listConnector = new ArrayList<>();
        NodeConnector connector1 = new NodeConnector(node);
        connector1.setDefaultConnectionType(EConnectionType.FLOW_MAIN);
        connector1.setName(EConnectionType.FLOW_MAIN.getName());
        connector1.setMaxLinkOutput(1);
        listConnector.add(connector1);
        connector1.setShowIf("SHOW_FLOW_CONNECTOR == 'true'");
        NodeConnector connector2 = new NodeConnector(node);
        connector2.setDefaultConnectionType(EConnectionType.FLOW_MAIN);
        connector2.setName(EConnectionType.FLOW_MAIN.getName());
        connector2.setMaxLinkInput(-1);
        connector2.setMaxLinkOutput(-1);
        connector2.setShowIf("SHOW_FLOW_CONNECTOR == 'false'");
        listConnector.add(connector2);
        node.setListConnector(listConnector);
        // create a test param with default value 'false'
        IElementParameter param = addShowIfParam(node);

        Assert.assertTrue(node.getConnectorFromName(EConnectionType.FLOW_MAIN.getName()) == connector2);
        Assert.assertTrue(node.getConnectorFromType(EConnectionType.FLOW_MAIN) == connector2);
        Assert.assertEquals(node.getConnectorsFromType(EConnectionType.FLOW_MAIN).size(), 1);

        // make connector show if to 'true'
        param.setValue(true);
        Assert.assertTrue(node.getConnectorFromName(EConnectionType.FLOW_MAIN.getName()) == connector1);
        Assert.assertTrue(node.getConnectorFromType(EConnectionType.FLOW_MAIN) == connector1);
        Assert.assertEquals(node.getConnectorsFromType(EConnectionType.FLOW_MAIN).size(), 1);

        // connector with same name should have the same current input/output
        connector1.setCurLinkNbInput(10);
        connector1.setCurLinkNbOutput(100);
        Assert.assertEquals(connector1.getCurLinkNbInput(), 10);
        Assert.assertEquals(connector1.getCurLinkNbOutput(), 100);
        Assert.assertEquals(connector2.getCurLinkNbInput(), 10);
        Assert.assertEquals(connector2.getCurLinkNbOutput(), 100);
    }

    @Test
    public void testGetNodeConnectorsShowIf2FlowConnectorWithName() {
        Process process = new Process(new FakePropertyImpl());
        IComponent sourceCom = ComponentsFactoryProvider.getInstance().get("tMysqlInput",
                ComponentCategory.CATEGORY_4_DI.getName());
        Node node = new Node(sourceCom, process);
        // create node connector for test
        List<INodeConnector> listConnector = new ArrayList<>();
        NodeConnector connector1 = new NodeConnector(node);
        connector1.setDefaultConnectionType(EConnectionType.FLOW_MAIN);
        connector1.setName("FILTER");
        listConnector.add(connector1);
        connector1.setShowIf("SHOW_FLOW_CONNECTOR == 'true'");
        NodeConnector connector2 = new NodeConnector(node);
        connector2.setDefaultConnectionType(EConnectionType.FLOW_MAIN);
        connector2.setName("REJECT");
        connector2.setShowIf("SHOW_FLOW_CONNECTOR == 'true'");
        listConnector.add(connector2);
        node.setListConnector(listConnector);
        // create a test param with default value 'false'
        IElementParameter param = addShowIfParam(node);

        Assert.assertEquals(node.getConnectorsFromType(EConnectionType.FLOW_MAIN).size(), 0);

        // make connector show if to 'true'
        param.setValue(true);
        Assert.assertTrue(node.getConnectorFromName("FILTER") == connector1);
        Assert.assertTrue(node.getConnectorFromName("REJECT") == connector2);
        Assert.assertEquals(node.getConnectorsFromType(EConnectionType.FLOW_MAIN).size(), 2);

        // set CurLinkNbInput/Output should not affact other connector with same type but different name
        connector1.setCurLinkNbInput(10);
        connector1.setCurLinkNbOutput(100);
        Assert.assertEquals(connector1.getCurLinkNbInput(), 10);
        Assert.assertEquals(connector1.getCurLinkNbOutput(), 100);
        Assert.assertEquals(connector2.getCurLinkNbInput(), 0);
        Assert.assertEquals(connector2.getCurLinkNbOutput(), 0);
    }

    private IElementParameter addShowIfParam(Node node) {
        IElementParameter param = new ElementParameter(node);
        param.setName("SHOW_FLOW_CONNECTOR");
        param.setCategory(EComponentCategory.TECHNICAL);
        param.setFieldType(EParameterFieldType.CHECK);
        param.setDisplayName("show flow  connector");
        param.setNumRow(99);
        param.setShow(false);
        param.setValue(false);
        node.addElementParameter(param);
        return param;
    }
}
