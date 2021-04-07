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
package org.talend.designer.components.tisprovider;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.junit.Test;
import org.talend.core.model.components.IComponent;
import org.talend.core.model.metadata.MetadataTable;
import org.talend.core.model.process.EConnectionType;
import org.talend.core.model.process.IConnection;
import org.talend.core.model.process.INode;
import org.talend.core.model.process.IReplaceNodeInProcess;
import org.talend.core.model.process.node.IExternalMapTable;
import org.talend.core.model.repository.FakePropertyImpl;
import org.talend.core.ui.component.ComponentsFactoryProvider;
import org.talend.designer.core.model.components.EParameterName;
import org.talend.designer.core.ui.editor.connections.Connection;
import org.talend.designer.core.ui.editor.nodes.Node;
import org.talend.designer.core.ui.editor.process.Process;
import org.talend.designer.mapper.external.data.ExternalMapperData;
import org.talend.designer.mapper.external.data.ExternalMapperTable;

/**
 * DOC jding  class global comment. Detailled comment
 */
public class ReplaceParallelizationNodeForTMapTest {

    private static final String ATTR_CLASS = "class";

    private static final String EXTENSION_ID = "org.talend.designer.core.replace_nodes";

    private static final String REPLACE_PARALLELIZATION_NODE = "org.talend.designer.components.tisprovider.ReplaceParallelizationNodeInProcess";

    @Test
    public void test_rebuildGraphicProcessFromNode_partitionAfterTMap() {
        List<INode> graphicalNodeList = new LinkedList<>();
        Process process = new Process(new FakePropertyImpl());
        Node tRowGenerator = createNode(process, "tRowGenerator");
        tRowGenerator.setStart(true);
        graphicalNodeList.add(tRowGenerator);
        Node tMap = createNode(process, "tMap");
        graphicalNodeList.add(tMap);
        Node tLogRow = createNode(process, "tLogRow");
        graphicalNodeList.add(tLogRow);

        Connection row1 = new Connection(tRowGenerator, tMap, EConnectionType.FLOW_MAIN, EConnectionType.FLOW_MAIN.getName(),
                "row1", "row1", "row1", false);
        row1.setMetaName("row1");
        tRowGenerator.addOutput(row1);
        tMap.addInput(row1);

        Connection row2 = new Connection(tMap, tLogRow, EConnectionType.FLOW_MAIN, EConnectionType.FLOW_MAIN.getName(),
                "outmap", "outmap", "outmap", false);
        row2.getElementParameter(EParameterName.PARTITIONER.getName()).setValue(true);
        row2.setMetaName("outmap");
        MetadataTable metadataTable = new MetadataTable();
        metadataTable.setTableName("outmap");
        metadataTable.setAttachedConnector(EConnectionType.FLOW_MAIN.getName());
        row2.getSource().getMetadataList().add(metadataTable);
        ExternalMapperTable extMapTable = new ExternalMapperTable();
        extMapTable.setName("outmap");
        ExternalMapperData externalData = (ExternalMapperData) row2.getSource().getExternalData();
        externalData.getOutputTables().add(extMapTable);
        tMap.addOutput(row2);
        tLogRow.addInput(row2);

        try {
            IReplaceNodeInProcess replaceNodeAction = getReplaceNodeActionByClassName(REPLACE_PARALLELIZATION_NODE);
            assertNotNull("Can't find the class " + REPLACE_PARALLELIZATION_NODE, replaceNodeAction);
            
            replaceNodeAction.rebuildGraphicProcessFromNode(tRowGenerator, graphicalNodeList);
            assertEquals(5, graphicalNodeList.size());

            // simulate the usage in tMap component
            Map<String, IConnection> nameToOutputConnection = new HashMap<String, IConnection>();
            for (IConnection outputConnection : tMap.getOutgoingConnections()) {
                nameToOutputConnection.put(outputConnection.getName(), outputConnection);
            }

            for (IExternalMapTable outputTable : tMap.getExternalData().getOutputTables()) {
                assertTrue(nameToOutputConnection.containsKey(outputTable.getName()));
            }

        } catch (Exception e) {
            fail("Exist exception when getReplaceNodeActionByClassName: " + e.getMessage());
            e.printStackTrace();
        }

    }

    private IReplaceNodeInProcess getReplaceNodeActionByClassName(String className) throws Exception {
        IReplaceNodeInProcess replaceNodeAction = null;
        IConfigurationElement[] elems = Platform.getExtensionRegistry()
                .getConfigurationElementsFor(EXTENSION_ID);
        for (IConfigurationElement elem : elems) {
            String attrValue = elem.getAttribute(ATTR_CLASS);
            if (StringUtils.isNotBlank(attrValue) && attrValue.equals(className)) {
                replaceNodeAction = (IReplaceNodeInProcess) elem.createExecutableExtension(ATTR_CLASS);
                break;
            }
        }
        return replaceNodeAction;

    }

    private Node createNode(Process process, String componentName) {
        IComponent parCom = ComponentsFactoryProvider.getInstance().get(componentName);
        Node parNode = new Node(parCom, process);
        parNode.setActivate(true);
        parNode.setStart(false);
        parNode.setComponent(parCom);
        parNode.setProcess(process);
        return parNode;
    }

}
