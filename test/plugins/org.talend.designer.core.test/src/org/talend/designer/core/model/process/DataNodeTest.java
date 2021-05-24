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
 * DOC jding  class global comment. Detailled comment
 */
public class DataNodeTest {

    private static final String TEST_COMPONENT_NAME = "tMysqlInput";

    private DataNode mainStartNode;

    private DataNode lookupNode;

    private DataNode tmapNode;

    private DataNode logRowNode;

    @Before
    public void before() {
        IComponent testComponent = ComponentsFactoryProvider.getInstance().get(TEST_COMPONENT_NAME,
                ComponentCategory.CATEGORY_4_DI.getName());
        IComponent tmapComponent = ComponentsFactoryProvider.getInstance().get("tMap", ComponentCategory.CATEGORY_4_DI.getName());
        IComponent logrowComponent = ComponentsFactoryProvider.getInstance().get("tLogRow",
                ComponentCategory.CATEGORY_4_DI.getName());
        mainStartNode = new DataNode(testComponent, TEST_COMPONENT_NAME + "_1");
        lookupNode = new DataNode(testComponent, TEST_COMPONENT_NAME + "_2");
        tmapNode = new DataNode(tmapComponent, "tMap_1");
        logRowNode = new DataNode(logrowComponent, "tLogRow_1");

        // mainStartNode income mainStartNode->tmapNode (main row)
        ((List<IConnection>) mainStartNode.getOutgoingConnections())
                .add(createDataConnection(mainStartNode, tmapNode, EConnectionType.FLOW_MAIN));
        // lookupNode outcome lookupNode->tmapNode (lookup row)
        ((List<IConnection>) lookupNode.getOutgoingConnections())
                .add(createDataConnection(lookupNode, tmapNode, EConnectionType.FLOW_REF));
        ((List<IConnection>) lookupNode.getIncomingConnections())
                .add(createDataConnection(mainStartNode, lookupNode, EConnectionType.RUN_AFTER));
        // tmapNode income mainStartNode->tmapNode (main row) lookupNode->tmapNode (lookup row)
        ((List<IConnection>) tmapNode.getIncomingConnections())
                .add(createDataConnection(mainStartNode, tmapNode, EConnectionType.FLOW_MAIN));
        ((List<IConnection>) tmapNode.getIncomingConnections())
                .add(createDataConnection(lookupNode, tmapNode, EConnectionType.FLOW_REF));
        // tmapNode outcome tmapNode->logRowNode (main row)
        ((List<IConnection>) tmapNode.getOutgoingConnections())
                .add(createDataConnection(tmapNode, logRowNode, EConnectionType.FLOW_MAIN));
        // logRowNode income tmapNode->logRowNode (main row)
        ((List<IConnection>) logRowNode.getIncomingConnections())
                .add(createDataConnection(tmapNode, logRowNode, EConnectionType.FLOW_MAIN));

    }

    @Test
    public void testGetSubProcessStartNode() {
        INode startOftmap = tmapNode.getSubProcessStartNode(false);
        Assert.assertEquals(startOftmap, mainStartNode);
        INode startOflogrow = logRowNode.getSubProcessStartNode(false);
        Assert.assertEquals(startOflogrow, mainStartNode);
        INode startOflookup = lookupNode.getSubProcessStartNode(false);
        Assert.assertEquals(startOflookup, mainStartNode);
    }

    @After
    public void after() {
        mainStartNode = null;
        lookupNode = null;
        tmapNode = null;
        logRowNode = null;
    }

    private DataConnection createDataConnection(DataNode source, DataNode target, EConnectionType lineStyle) {
        DataConnection dataConnec = new DataConnection();
        dataConnec.setActivate(true);
        dataConnec.setLineStyle(EConnectionType.FLOW_MAIN);
        dataConnec.setConnectorName("FLOW");
        dataConnec.setName("row_" + source.getUniqueName());
        dataConnec.setSource(source);
        dataConnec.setTarget(target);
        return dataConnec;
    }

}
