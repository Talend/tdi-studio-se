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
import org.talend.designer.core.ui.editor.process.Process;

/**
 * created by wchen on Apr 17, 2017 Detailled comment
 *
 */
public class NodeTest {

    @Test
    public void test() {
        Process process = new Process(new FakePropertyImpl());
        IComponent sourceCom = ComponentsFactoryProvider.getInstance().get("tMysqlInput",
                ComponentCategory.CATEGORY_4_DI.getName());
        Node node = new Node(sourceCom, process);
        Assert.assertNotNull(node.getConnectorFromName(EConnectionType.FLOW_MAIN.getName()));
        Assert.assertNotNull(node.getConnectorFromType(EConnectionType.FLOW_MAIN));
        Assert.assertEquals(node.getConnectorsFromType(EConnectionType.FLOW_MAIN).size(), 1);

        // change parameter and connector and test
        IElementParameter param = new ElementParameter(node);
        param.setName("SHOW_FLOW_CONNECTOR");
        param.setCategory(EComponentCategory.TECHNICAL);
        param.setFieldType(EParameterFieldType.CHECK);
        param.setDisplayName("show flow  connector");
        param.setNumRow(99);
        param.setShow(false);
        // make connector show if to 'false'
        param.setValue(false);
        node.addElementParameter(param);
        INodeConnector connectorFromType = node.getConnectorFromType(EConnectionType.FLOW_MAIN);
        connectorFromType.setShowIf("SHOW_FLOW_CONNECTOR == 'true'");

        Assert.assertNull(node.getConnectorFromName(EConnectionType.FLOW_MAIN.getName()));
        Assert.assertNull(node.getConnectorFromType(EConnectionType.FLOW_MAIN));
        Assert.assertEquals(node.getConnectorsFromType(EConnectionType.FLOW_MAIN).size(), 0);

        // make connector show if to 'true'
        param.setValue(true);
        Assert.assertNotNull(node.getConnectorFromName(EConnectionType.FLOW_MAIN.getName()));
        Assert.assertNotNull(node.getConnectorFromType(EConnectionType.FLOW_MAIN));
        Assert.assertEquals(node.getConnectorsFromType(EConnectionType.FLOW_MAIN).size(), 1);
    }
}
