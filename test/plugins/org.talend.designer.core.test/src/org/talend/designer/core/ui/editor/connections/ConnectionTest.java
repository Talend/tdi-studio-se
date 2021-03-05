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
package org.talend.designer.core.ui.editor.connections;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.talend.core.model.components.ComponentCategory;
import org.talend.core.model.components.IComponent;
import org.talend.core.model.metadata.IMetadataTable;
import org.talend.core.model.process.EConnectionType;
import org.talend.core.model.process.INode;
import org.talend.core.model.process.IProcess2;
import org.talend.core.model.process.TraceData;
import org.talend.core.model.properties.PropertiesFactory;
import org.talend.core.model.properties.Property;
import org.talend.core.ui.component.ComponentsFactoryProvider;
import org.talend.designer.core.ui.editor.nodes.Node;
import org.talend.designer.core.ui.editor.process.Process;

/**
 * DOC Administrator class global comment. Detailled comment <br/>
 *
 * $Id: talend.epf 55206 2011-02-15 17:32:14Z mhirt $
 *
 */
public class ConnectionTest {

    private IProcess2 process;

    private Connection connection = null;

    private INode source;

    private INode target;

    private IMetadataTable inputTable;

    /**
     * DOC Administrator Comment method "setUp".
     *
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
        Property property = PropertiesFactory.eINSTANCE.createProperty();
        process = new Process(property);
        IComponent sourceCom = ComponentsFactoryProvider.getInstance().get("tMysqlInput",
                ComponentCategory.CATEGORY_4_DI.getName());
        IComponent targetCom = ComponentsFactoryProvider.getInstance().get("tMysqlOutput",
                ComponentCategory.CATEGORY_4_DI.getName());
        source = new Node(sourceCom, process);
        target = new Node(targetCom, process);

        connection = new Connection(source, target, EConnectionType.FLOW_MAIN, EConnectionType.FLOW_MAIN.getName(), "test",
                "test", "test", false);

        inputTable = source.getMetadataList().get(0);
    }

    /**
     * DOC Administrator Comment method "tearDown".
     *
     * @throws java.lang.Exception
     */
    @After
    public void tearDown() throws Exception {
    }

    /**
     * Test method for {@link org.talend.designer.core.ui.editor.connections.Connection#setName()}.
     */
    @Test
    public void testSetName() {
        connection.setName("newTest");
        assertEquals("newTest", connection.getName());

    }

    /**
     * Test method for {@link org.talend.designer.core.ui.editor.connections.Connection#reconnect()}.
     */
    @Test
    public void testReconnect() {
        connection.reconnect();
        assertTrue(source.getOutgoingConnections().contains(connection));
        assertTrue(target.getIncomingConnections().contains(connection));
        assertEquals(connection.getSource(), source);
        assertEquals(connection.getTarget(), target);
    }

    /**
     * Test method for {@link org.talend.designer.core.ui.editor.connections.Connection#disconnect()}.
     */
    @Test
    public void testDisconnect() {
        connection.disconnect();
        assertFalse(source.getOutgoingConnections().contains(connection));
        assertFalse(target.getIncomingConnections().contains(connection));
    }

    @Test
    public void testSetTraceData() {
        Map<String, TraceData> map = new HashMap<String, TraceData>();
        TraceData data = new TraceData();
        map.put("key", data);
        connection.setTraceData(map);
        TraceData value = connection.getTraceData().get("key");
        assertEquals(data, value);

        connection.setTraceData(null);
        assertNull(connection.getTraceData());
    }

    @Test
    public void testCreateParallelizeParameters() {
        IComponent componentPar = ComponentsFactoryProvider.getInstance().get("tPartitioner",
                ComponentCategory.CATEGORY_4_DI.getName());
        INode node = new Node(componentPar, (Process) source.getProcess());
        assertEquals(node.getPropertyValue("NUM_PARTITIONS").toString(), connection.getPropertyValue("NUM_PARTITIONS").toString());
        assertEquals(node.getPropertyValue("HASH_PARTITION").toString(), connection.getPropertyValue("HASH_PARTITION").toString());
        assertEquals(node.getPropertyValue("HASH_KEYS").toString(), connection.getPropertyValue("HASH_KEYS").toString());

        IComponent componentCol = ComponentsFactoryProvider.getInstance().get("tRecollector",
                ComponentCategory.CATEGORY_4_DI.getName());
        node = new Node(componentCol, (Process) source.getProcess());
        assertEquals(node.getPropertyValue("IS_SORTING").toString(), connection.getPropertyValue("IS_SORTING").toString());
    }

}
