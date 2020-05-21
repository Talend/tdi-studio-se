// ============================================================================
//
// Copyright (C) 2006-2020 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.designer.core.utils;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.talend.core.model.components.ComponentCategory;
import org.talend.core.model.components.IComponent;
import org.talend.core.model.process.EConnectionType;
import org.talend.core.model.process.IConnection;
import org.talend.core.model.process.IElementParameter;
import org.talend.core.model.properties.PropertiesFactory;
import org.talend.core.model.properties.Property;
import org.talend.core.ui.component.ComponentsFactoryProvider;
import org.talend.designer.core.model.components.EParameterName;
import org.talend.designer.core.model.components.ElementParameter;
import org.talend.designer.core.ui.editor.connections.Connection;
import org.talend.designer.core.ui.editor.nodes.Node;
import org.talend.designer.core.ui.editor.process.Process;

/**
 * DOC class global comment. Detailled comment
 */
public class ParallelExecutionUtilsTest {

    @Test
    public void testIsExistPreviousParCo() {

        IComponent tFixedFlowInputComponent = ComponentsFactoryProvider.getInstance().get("tFixedFlowInput",
                ComponentCategory.CATEGORY_4_DI.getName());
        IComponent tMysqlComponent = ComponentsFactoryProvider.getInstance().get("tMysqlInput",
                ComponentCategory.CATEGORY_4_DI.getName());
        IComponent tLogRowComponent = ComponentsFactoryProvider.getInstance().get("tLogRow",
                ComponentCategory.CATEGORY_4_DI.getName());
        IComponent tJavaComponent = ComponentsFactoryProvider.getInstance().get("tJava",
                ComponentCategory.CATEGORY_4_DI.getName());

        Property property = PropertiesFactory.eINSTANCE.createProperty();
        Process process = new Process(property);
        // case1 tJava->tFixedFlowInput->tLogRow
        Node c1_Node = new Node(tJavaComponent, process);
        Node c1_Node1 = new Node(tFixedFlowInputComponent, process);
        Node c1_Node2 = new Node(tLogRowComponent, process);
        Connection c1_conn = createConnection(c1_Node, c1_Node1, EConnectionType.FLOW_MAIN);
        Connection c1_conn1 = createConnection(c1_Node1, c1_Node2, EConnectionType.FLOW_MAIN);
        ((List<IConnection>) c1_Node1.getIncomingConnections()).add(c1_conn);
        ((List<IConnection>) c1_Node2.getIncomingConnections()).add(c1_conn1);
        Assert.assertFalse(ParallelExecutionUtils.isExistPreviousParCon(c1_Node2));

        IElementParameter elementParameter = new ElementParameter(null);
        elementParameter.setName(EParameterName.PARTITIONER.getName());
        elementParameter.setValue(true);
        ((List<IElementParameter>) c1_conn1.getElementParameters()).add(elementParameter);
        Assert.assertTrue(ParallelExecutionUtils.isExistPreviousParCon(c1_Node2));

        // case2 assume tJava-(runIf)->tFixedFlowInput tJava-(onSubjobok)->tMysqlInput
        // tMysqlInput-(onComponentOK)->tFixedFlowInput
        // tMysqlInput-(main)->tLogRowComponent
        // tLogRowComponent-(onComponentOk)->tJava
        Node c2_Node = new Node(tJavaComponent, process);
        Node c2_Node1 = new Node(tFixedFlowInputComponent, process);
        Node c2_Node2 = new Node(tMysqlComponent, process);
        Node c2_Node3 = new Node(tLogRowComponent, process);
        Connection c2_conn = createConnection(c2_Node, c2_Node1, EConnectionType.RUN_IF);
        Connection c2_conn1 = createConnection(c2_Node, c2_Node2, EConnectionType.ON_SUBJOB_OK);
        Connection c2_conn2 = createConnection(c2_Node2, c2_Node1, EConnectionType.ON_COMPONENT_OK);
        Connection c2_conn3 = createConnection(c2_Node2, c2_Node3, EConnectionType.FLOW_MAIN);
        Connection c2_conn4 = createConnection(c2_Node3, c2_Node, EConnectionType.ON_COMPONENT_OK);

        ((List<IConnection>) c2_Node1.getIncomingConnections()).add(c2_conn);
        ((List<IConnection>) c2_Node2.getIncomingConnections()).add(c2_conn1);
        ((List<IConnection>) c2_Node1.getIncomingConnections()).add(c2_conn2);
        ((List<IConnection>) c2_Node3.getIncomingConnections()).add(c2_conn3);
        ((List<IConnection>) c2_Node.getIncomingConnections()).add(c2_conn4);

        Assert.assertFalse(ParallelExecutionUtils.isExistPreviousParCon(c2_Node1));

        ((List<IElementParameter>) c2_conn3.getElementParameters()).add(elementParameter);
        Assert.assertTrue(ParallelExecutionUtils.isExistPreviousParCon(c2_Node1));

    }

    private Connection createConnection(Node sourceNode, Node targetNode, EConnectionType connType) {
        Connection connection = new Connection(sourceNode, targetNode, connType, false);
        return connection;
    }

}
