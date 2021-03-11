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
package org.talend.designer.core.ui.editor.cmd;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.talend.core.model.metadata.IMetadataColumn;
import org.talend.core.model.metadata.IMetadataTable;
import org.talend.core.model.metadata.MetadataColumn;
import org.talend.core.model.metadata.MetadataTable;
import org.talend.core.model.process.IConnection;
import org.talend.core.model.process.INodeConnector;
import org.talend.core.model.process.IProcess2;
import org.talend.core.model.properties.PropertiesFactory;
import org.talend.core.model.properties.Property;
import org.talend.designer.core.test.util.NodeTestCreator;
import org.talend.designer.core.ui.editor.nodes.Node;
import org.talend.designer.core.ui.editor.process.Process;

/**
 * created by nrousseau on Jun 27, 2012 Detailled comment
 *
 */
public class ConnectionCreateCommandTest {

    private IMetadataTable createSimpleMetadata1() {
        IMetadataTable table = new MetadataTable();

        IMetadataColumn column1 = new MetadataColumn();
        column1.setLabel("C1"); //$NON-NLS-1$
        column1.setTalendType("id_String"); //$NON-NLS-1$
        table.getListColumns().add(column1);

        IMetadataColumn column2 = new MetadataColumn();
        column2.setLabel("C2"); //$NON-NLS-1$
        column2.setTalendType("id_String"); //$NON-NLS-1$
        table.getListColumns().add(column2);

        IMetadataColumn column3 = new MetadataColumn();
        column3.setLabel("C3"); //$NON-NLS-1$
        column3.setTalendType("id_Integer"); //$NON-NLS-1$
        table.getListColumns().add(column3);

        return table;
    }

    private IMetadataTable createSimpleMetadata2() {
        IMetadataTable table = new MetadataTable();

        IMetadataColumn column2 = new MetadataColumn();
        column2.setLabel("C2"); //$NON-NLS-1$
        column2.setTalendType("id_String"); //$NON-NLS-1$
        table.getListColumns().add(column2);

        IMetadataColumn column3 = new MetadataColumn();
        column3.setLabel("C3"); //$NON-NLS-1$
        column3.setTalendType("id_Integer"); //$NON-NLS-1$
        table.getListColumns().add(column3);

        return table;
    }

    private void assertMetadataIsSame(IMetadataTable table1, IMetadataTable table2) {
        assertEquals(table1.getListColumns().size(), table2.getListColumns().size());

        for (int i = 0; i < table1.getListColumns().size(); i++) {
            IMetadataColumn col1 = table1.getListColumns().get(i);
            IMetadataColumn col2 = table2.getListColumns().get(i);

            assertEquals(col1.getLabel(), col2.getLabel());
            assertEquals(col1.getTalendType(), col2.getTalendType());
        }
    }

    private IProcess2 getFakeProcess() {
        Property property = PropertiesFactory.eINSTANCE.createProperty();
        property.setId("junitId"); //$NON-NLS-1$
        property.setVersion("0.1"); //$NON-NLS-1$
        property.setLabel("test");//$NON-NLS-1$

        return new Process(property);
    }

    /**
     * Test method for {@link org.talend.designer.core.ui.editor.cmd.ConnectionCreateCommand#canExecute()}.
     */
    @Test
    public void testCanExecute() {
        IProcess2 process = getFakeProcess();
        Node simpleInputNode = NodeTestCreator.createSimpleInputNode(process);
        // simple tests only since it's simply using the class ConnectionManager which already have JUnits.

        List<Object> args = new ArrayList<Object>();
        args.add("metaName"); //$NON-NLS-1$
        args.add("connectionName"); //$NON-NLS-1$
        args.add(mock(IMetadataTable.class));

        ConnectionCreateCommand ccc = new ConnectionCreateCommand(simpleInputNode, "FLOW", args); //$NON-NLS-1$

        ccc.setTarget(NodeTestCreator.createSimpleOutputNode(process));

        assertTrue(ccc.canExecute());

        ccc.setTarget(NodeTestCreator.createSimpleInputNode(process));

        assertFalse(ccc.canExecute());
    }

    /**
     * Test method for {@link org.talend.designer.core.ui.editor.cmd.ConnectionCreateCommand#execute()}.
     */
    @Test
    public void testExecute() {
        // simple input to simple output with no metadata and schema auto propagate
        executeSimpleInputToSimpleOutput1();
        // simple input to simple output with metadata already existing and schema auto propagate
        executeSimpleInputToSimpleOutput2();
        // simple input to simple output with no metadata and without schema auto propagate
        executeSimpleInputToSimpleOutput3();
    }

    /**
     * simple input to simple output with no metadata and schema auto propagate.
     */
    private ConnectionCreateCommand executeSimpleInputToSimpleOutput1() {
        IProcess2 process = getFakeProcess();
        Node simpleInputNode = NodeTestCreator.createSimpleInputNode(process);
        // simple tests only since it's simply using the class ConnectionManager which already have JUnits.

        List<Object> args = new ArrayList<Object>();
        args.add(simpleInputNode.getUniqueName());
        args.add("connectionName"); //$NON-NLS-1$

        simpleInputNode.getMetadataList().clear();
        IMetadataTable table = createSimpleMetadata1();
        table.setTableName(simpleInputNode.getUniqueName());
        table.setLabel(simpleInputNode.getUniqueName());
        table.setAttachedConnector("FLOW"); //$NON-NLS-1$
        simpleInputNode.getMetadataList().add(table);

        args.add(null); // set null, the command should take the schema from the component directly

        ConnectionCreateCommand ccc = new ConnectionCreateCommand(simpleInputNode, "FLOW", args); //$NON-NLS-1$
        ConnectionCreateCommand.setCreatingConnection(true);

        Node simpleOutputNode = NodeTestCreator.createSimpleOutputNode(process);

        ccc.setTarget(simpleOutputNode);

        ccc.execute();

        assertEquals(simpleInputNode.getOutgoingConnections().size(), 1);
        assertEquals(simpleOutputNode.getIncomingConnections().size(), 1);
        assertEquals(simpleOutputNode.getMetadataList().size(), 1);

        IMetadataTable outputMetadata = simpleOutputNode.getMetadataList().get(0);
        assertMetadataIsSame(table, outputMetadata); // metadata should have been propagated automatically

        IConnection connection = simpleInputNode.getOutgoingConnections().get(0);
        assertEquals(simpleOutputNode.getIncomingConnections().get(0), connection);
        assertEquals(connection.getSource(), simpleInputNode);
        assertEquals(connection.getTarget(), simpleOutputNode);
        assertEquals(connection.getMetaName(), simpleInputNode.getUniqueName());
        assertEquals(connection.getConnectorName(), "FLOW"); //$NON-NLS-1$
        INodeConnector inputConnector = simpleInputNode.getConnectorFromName("FLOW"); //$NON-NLS-1$
        INodeConnector outputConnector = simpleOutputNode.getConnectorFromName("FLOW"); //$NON-NLS-1$
        assertEquals(inputConnector.getCurLinkNbOutput(), 1);
        assertEquals(outputConnector.getCurLinkNbInput(), 1);

        return ccc;
    }

    /**
     * simple input to simple output with metadata already existing and schema auto propagate.
     */
    private ConnectionCreateCommand executeSimpleInputToSimpleOutput2() {
        IProcess2 process = getFakeProcess();
        Node simpleInputNode = NodeTestCreator.createSimpleInputNode(process);
        // simple tests only since it's simply using the class ConnectionManager which already have JUnits.

        List<Object> args = new ArrayList<Object>();
        args.add(simpleInputNode.getUniqueName());
        args.add("connectionName"); //$NON-NLS-1$

        simpleInputNode.getMetadataList().clear();
        IMetadataTable table1 = createSimpleMetadata1();
        table1.setTableName(simpleInputNode.getUniqueName());
        table1.setLabel(simpleInputNode.getUniqueName());
        table1.setAttachedConnector("FLOW"); //$NON-NLS-1$
        simpleInputNode.getMetadataList().add(table1);

        args.add(null); // set null, the command should take the schema from the component directly

        ConnectionCreateCommand ccc = new ConnectionCreateCommand(simpleInputNode, "FLOW", args); //$NON-NLS-1$
        ConnectionCreateCommand.setCreatingConnection(true);

        Node simpleOutputNode = NodeTestCreator.createSimpleOutputNode(process);

        simpleOutputNode.getMetadataList().clear();
        IMetadataTable table2 = createSimpleMetadata2();
        table2.setTableName(simpleOutputNode.getUniqueName());
        table2.setLabel(simpleOutputNode.getUniqueName());
        table2.setAttachedConnector("FLOW"); //$NON-NLS-1$
        simpleOutputNode.getMetadataList().add(table2);

        ccc.setTarget(simpleOutputNode);

        ccc.execute();

        assertEquals(simpleInputNode.getOutgoingConnections().size(), 1);
        assertEquals(simpleOutputNode.getIncomingConnections().size(), 1);
        assertEquals(simpleOutputNode.getMetadataList().size(), 1);

        IMetadataTable outputMetadata = simpleOutputNode.getMetadataList().get(0);
        assertMetadataIsSame(table1, outputMetadata); // metadata should have been propagated automatically

        IConnection connection = simpleInputNode.getOutgoingConnections().get(0);
        assertEquals(simpleOutputNode.getIncomingConnections().get(0), connection);
        assertEquals(connection.getSource(), simpleInputNode);
        assertEquals(connection.getTarget(), simpleOutputNode);
        assertEquals(connection.getMetaName(), simpleInputNode.getUniqueName());
        assertEquals(connection.getConnectorName(), "FLOW"); //$NON-NLS-1$
        INodeConnector inputConnector = simpleInputNode.getConnectorFromName("FLOW"); //$NON-NLS-1$
        INodeConnector outputConnector = simpleOutputNode.getConnectorFromName("FLOW"); //$NON-NLS-1$
        assertEquals(inputConnector.getCurLinkNbOutput(), 1);
        assertEquals(outputConnector.getCurLinkNbInput(), 1);

        return ccc;
    }

    /**
     * simple input to simple output with no metadata and without schema auto propagate.
     */
    private ConnectionCreateCommand executeSimpleInputToSimpleOutput3() {
        IProcess2 process = getFakeProcess();
        Node simpleInputNode = NodeTestCreator.createSimpleInputNode(process);
        // simple tests only since it's simply using the class ConnectionManager which already have JUnits.

        List<Object> args = new ArrayList<Object>();
        args.add(simpleInputNode.getUniqueName());
        args.add("connectionName"); //$NON-NLS-1$

        simpleInputNode.getMetadataList().clear();
        IMetadataTable table = createSimpleMetadata1();
        table.setTableName(simpleInputNode.getUniqueName());
        table.setLabel(simpleInputNode.getUniqueName());
        table.setAttachedConnector("FLOW"); //$NON-NLS-1$
        simpleInputNode.getMetadataList().add(table);

        args.add(null); // set null, the command should take the schema from the component directly

        ConnectionCreateCommand ccc = new ConnectionCreateCommand(simpleInputNode, "FLOW", args); //$NON-NLS-1$
        ConnectionCreateCommand.setCreatingConnection(true);

        Node simpleOutputNode = NodeTestCreator.createSimpleOutputNodeNoPropagate(process);

        ccc.setTarget(simpleOutputNode);

        ccc.execute();

        assertEquals(simpleInputNode.getOutgoingConnections().size(), 1);
        assertEquals(simpleOutputNode.getIncomingConnections().size(), 1);
        assertEquals(simpleOutputNode.getMetadataList().size(), 1);

        IMetadataTable outputMetadata = simpleOutputNode.getMetadataList().get(0);
        assertEquals(outputMetadata.getListColumns().size(), 0); // no propagation

        IConnection connection = simpleInputNode.getOutgoingConnections().get(0);
        assertEquals(simpleOutputNode.getIncomingConnections().get(0), connection);
        assertEquals(connection.getSource(), simpleInputNode);
        assertEquals(connection.getTarget(), simpleOutputNode);
        assertEquals(connection.getMetaName(), simpleInputNode.getUniqueName());
        assertEquals(connection.getConnectorName(), "FLOW"); //$NON-NLS-1$
        INodeConnector inputConnector = simpleInputNode.getConnectorFromName("FLOW"); //$NON-NLS-1$
        INodeConnector outputConnector = simpleOutputNode.getConnectorFromName("FLOW"); //$NON-NLS-1$
        assertEquals(inputConnector.getCurLinkNbOutput(), 1);
        assertEquals(outputConnector.getCurLinkNbInput(), 1);

        return ccc;
    }

    /**
     * Test method for {@link org.talend.designer.core.ui.editor.cmd.ConnectionCreateCommand#undo()}.
     */
    @Test
    public void testUndo() {
        // simple input to simple output with no metadata and schema auto propagate
        undoSimpleInputToSimpleOutput1();
        // simple input to simple output with metadata already existing and schema auto propagate
        undoSimpleInputToSimpleOutput2();
        // simple input to simple output with no metadata and without schema auto propagate
        undoSimpleInputToSimpleOutput3();
    }

    /**
     * DOC nrousseau Comment method "undoSimpleInputToSimpleOutput1".
     */
    private ConnectionCreateCommand undoSimpleInputToSimpleOutput1() {
        ConnectionCreateCommand ccc = executeSimpleInputToSimpleOutput1();

        ccc.undo();

        assertEquals(ccc.source.getOutgoingConnections().size(), 0);
        assertEquals(ccc.target.getOutgoingConnections().size(), 0);

        INodeConnector inputConnector = ccc.source.getConnectorFromName("FLOW"); //$NON-NLS-1$
        INodeConnector outputConnector = ccc.target.getConnectorFromName("FLOW"); //$NON-NLS-1$
        assertEquals(inputConnector.getCurLinkNbOutput(), 0);
        assertEquals(outputConnector.getCurLinkNbInput(), 0);

        return ccc;
    }

    /**
     * DOC nrousseau Comment method "undoSimpleInputToSimpleOutput1".
     */
    private ConnectionCreateCommand undoSimpleInputToSimpleOutput2() {
        ConnectionCreateCommand ccc = executeSimpleInputToSimpleOutput2();

        ccc.undo();

        assertEquals(ccc.source.getOutgoingConnections().size(), 0);
        assertEquals(ccc.target.getOutgoingConnections().size(), 0);

        INodeConnector inputConnector = ccc.source.getConnectorFromName("FLOW"); //$NON-NLS-1$
        INodeConnector outputConnector = ccc.target.getConnectorFromName("FLOW"); //$NON-NLS-1$
        assertEquals(inputConnector.getCurLinkNbOutput(), 0);
        assertEquals(outputConnector.getCurLinkNbInput(), 0);

        return ccc;
    }

    /**
     * DOC nrousseau Comment method "undoSimpleInputToSimpleOutput1".
     */
    private ConnectionCreateCommand undoSimpleInputToSimpleOutput3() {
        ConnectionCreateCommand ccc = executeSimpleInputToSimpleOutput3();

        ccc.undo();

        assertEquals(ccc.source.getOutgoingConnections().size(), 0);
        assertEquals(ccc.target.getOutgoingConnections().size(), 0);

        INodeConnector inputConnector = ccc.source.getConnectorFromName("FLOW"); //$NON-NLS-1$
        INodeConnector outputConnector = ccc.target.getConnectorFromName("FLOW"); //$NON-NLS-1$
        assertEquals(inputConnector.getCurLinkNbOutput(), 0);
        assertEquals(outputConnector.getCurLinkNbInput(), 0);

        return ccc;
    }

    /**
     * Test method for {@link org.talend.designer.core.ui.editor.cmd.ConnectionCreateCommand#redo()}.
     */
    @Test
    public void testRedo() {
        // simple input to simple output with no metadata and schema auto propagate
        redoSimpleInputToSimpleOutput1();
        // simple input to simple output with metadata already existing and schema auto propagate
        redoSimpleInputToSimpleOutput2();
        // simple input to simple output with no metadata and without schema auto propagate
        redoSimpleInputToSimpleOutput3();
    }

    /**
     * DOC nrousseau Comment method "redoSimpleInputToSimpleOutput1".
     */
    private void redoSimpleInputToSimpleOutput1() {
        ConnectionCreateCommand ccc = undoSimpleInputToSimpleOutput1();

        ccc.redo();

        INodeConnector inputConnector = ccc.source.getConnectorFromName("FLOW"); //$NON-NLS-1$
        INodeConnector outputConnector = ccc.target.getConnectorFromName("FLOW"); //$NON-NLS-1$
        assertEquals(inputConnector.getCurLinkNbOutput(), 1);
        assertEquals(outputConnector.getCurLinkNbInput(), 1);
    }

    /**
     * DOC nrousseau Comment method "redoSimpleInputToSimpleOutput1".
     */
    private void redoSimpleInputToSimpleOutput2() {
        ConnectionCreateCommand ccc = undoSimpleInputToSimpleOutput2();

        ccc.redo();

        INodeConnector inputConnector = ccc.source.getConnectorFromName("FLOW"); //$NON-NLS-1$
        INodeConnector outputConnector = ccc.target.getConnectorFromName("FLOW"); //$NON-NLS-1$
        assertEquals(inputConnector.getCurLinkNbOutput(), 1);
        assertEquals(outputConnector.getCurLinkNbInput(), 1);
    }

    /**
     * DOC nrousseau Comment method "redoSimpleInputToSimpleOutput1".
     */
    private void redoSimpleInputToSimpleOutput3() {
        ConnectionCreateCommand ccc = undoSimpleInputToSimpleOutput3();

        ccc.redo();

        INodeConnector inputConnector = ccc.source.getConnectorFromName("FLOW"); //$NON-NLS-1$
        INodeConnector outputConnector = ccc.target.getConnectorFromName("FLOW"); //$NON-NLS-1$
        assertEquals(inputConnector.getCurLinkNbOutput(), 1);
        assertEquals(outputConnector.getCurLinkNbInput(), 1);
    }
}
