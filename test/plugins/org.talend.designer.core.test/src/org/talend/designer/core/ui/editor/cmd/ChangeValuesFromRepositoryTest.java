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
import static org.junit.Assert.assertNotSame;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.Path;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.talend.commons.exception.PersistenceException;
import org.talend.core.model.components.ComponentCategory;
import org.talend.core.model.components.IComponent;
import org.talend.core.model.metadata.IMetadataColumn;
import org.talend.core.model.metadata.IMetadataTable;
import org.talend.core.model.metadata.MetadataColumn;
import org.talend.core.model.metadata.builder.connection.ConnectionFactory;
import org.talend.core.model.metadata.builder.connection.DatabaseConnection;
import org.talend.core.model.metadata.builder.connection.MetadataTable;
import org.talend.core.model.process.EComponentCategory;
import org.talend.core.model.process.EConnectionType;
import org.talend.core.model.process.EParameterFieldType;
import org.talend.core.model.process.IElementParameter;
import org.talend.core.model.process.INode;
import org.talend.core.model.process.IProcess2;
import org.talend.core.model.properties.DatabaseConnectionItem;
import org.talend.core.model.properties.ItemState;
import org.talend.core.model.properties.PropertiesFactory;
import org.talend.core.model.properties.Property;
import org.talend.core.model.utils.TalendTextUtils;
import org.talend.core.runtime.CoreRuntimePlugin;
import org.talend.core.ui.component.ComponentsFactoryProvider;
import org.talend.cwm.helper.ConnectionHelper;
import org.talend.cwm.helper.PackageHelper;
import org.talend.designer.core.i18n.Messages;
import org.talend.designer.core.model.components.EParameterName;
import org.talend.designer.core.model.components.ElementParameter;
import org.talend.designer.core.ui.editor.connections.Connection;
import org.talend.designer.core.ui.editor.nodes.Node;
import org.talend.designer.core.ui.editor.process.Process;
import org.talend.repository.model.IProxyRepositoryFactory;

import orgomg.cwm.resource.record.RecordFactory;
import orgomg.cwm.resource.record.RecordFile;

/**
 * DOC hwang class global comment. Detailled comment <br/>
 *
 * $Id: talend.epf 55206 2011-02-15 17:32:14Z mhirt $
 *
 */
public class ChangeValuesFromRepositoryTest {

    private INode elem;

    private INode target;

    private static org.talend.core.model.metadata.builder.connection.Connection connection;

    private static DatabaseConnectionItem databaseConnItem;

    private static MetadataTable inputTable;

    /**
     * DOC Administrator Comment method "setUp".
     *
     * @throws java.lang.Exception
     */
    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        init();
    }

    /**
     * DOC Administrator Comment method "setUp".
     *
     * @throws java.lang.Exception
     */
     @Before
    public void setUp() throws Exception {
        Property property = PropertiesFactory.eINSTANCE.createProperty();
        IProcess2 process = new Process(property);
        IComponent sourceCom = ComponentsFactoryProvider.getInstance().get("tMysqlInput",
                ComponentCategory.CATEGORY_4_DI.getName());
        IComponent targetCom = ComponentsFactoryProvider.getInstance().get("tMysqlOutput",
                ComponentCategory.CATEGORY_4_DI.getName());
        elem = new Node(sourceCom, process);
        elem.setLabel("tMysqlInput_1");
        target = new Node(targetCom, process);
        IMetadataTable table = createSimpleMetadata();
        table.setAttachedConnector("FLOW");
        List<IMetadataTable> metadataList = new ArrayList<IMetadataTable>();
        metadataList.add(table);
        elem.setMetadataList(metadataList);
        Connection conn = new Connection(elem, target, EConnectionType.FLOW_MAIN, "FLOW", "metaName", "row1", false);
        List<Connection> connList = new ArrayList<Connection>();
        connList.add(conn);
        target.setIncomingConnections(connList);
        elem.setOutgoingConnections(connList);
    }

    private static void init() throws PersistenceException {
        connection = ConnectionFactory.eINSTANCE.createDatabaseConnection();
        connection.setName("ChangeValues");
        ((DatabaseConnection) connection).setDatabaseType("MySQL");
        ((DatabaseConnection) connection).setUsername("root");
        ((DatabaseConnection) connection).setRawPassword("root");
        ((DatabaseConnection) connection).setPort("4000");
        ((DatabaseConnection) connection).setDatasourceName("test");
        ((DatabaseConnection) connection).setServerName("localhost");

        IProxyRepositoryFactory factory = CoreRuntimePlugin.getInstance().getProxyRepositoryFactory();
        connection.setId(factory.getNextId());
        databaseConnItem = PropertiesFactory.eINSTANCE.createDatabaseConnectionItem();
        Property myProperty = PropertiesFactory.eINSTANCE.createProperty();
        myProperty.setId(factory.getNextId());
        myProperty.setLabel("ChangeValues");
        myProperty.setVersion("0.1");

        ItemState itemState = PropertiesFactory.eINSTANCE.createItemState();
        itemState.setPath("");

        databaseConnItem.setProperty(myProperty);
        databaseConnItem.setState(itemState);
        databaseConnItem.setConnection(connection);

        RecordFile record = (RecordFile) ConnectionHelper.getPackage(connection.getName(), connection, RecordFile.class);
        inputTable = ConnectionFactory.eINSTANCE.createMetadataTable();

        inputTable.setId(factory.getNextId());
        inputTable.setLabel("Input");

        if (record != null) {
            PackageHelper.addMetadataTable(inputTable, record);
        } else {
            RecordFile newrecord = RecordFactory.eINSTANCE.createRecordFile();
            newrecord.setName(connection.getName());
            ConnectionHelper.addPackage(newrecord, connection);
            PackageHelper.addMetadataTable(inputTable, newrecord);
        }

        factory.create(databaseConnItem, new Path(""));
        factory.save(databaseConnItem);
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
     * Test method for {@link org.talend.designer.core.ui.editor.cmd.ChangeValuesFromRepository#execute()}.
     */
    @Test
    public void testExecute() {
        ChangeValuesFromRepository changeValuesFromRepository = new ChangeValuesFromRepository(elem, connection,
                "PROPERTY:PROPERTY_TYPE", "REPOSITORY");
        changeValuesFromRepository.execute();
        assertEquals(elem.getPropertyValue("PROPERTY:PROPERTY_TYPE"), "REPOSITORY");

        changeValuesFromRepository = new ChangeValuesFromRepository(elem, connection, "PROPERTY:REPOSITORY_PROPERTY_TYPE",
                databaseConnItem.getProperty().getId());
        changeValuesFromRepository.execute();
        DatabaseConnection dbConn = (DatabaseConnection) connection;
        assertEquals(elem.getPropertyValue("PROPERTY:REPOSITORY_PROPERTY_TYPE"), databaseConnItem.getProperty().getId());

        assertEquals(elem.getPropertyValue("USER"), TalendTextUtils.addQuotes(dbConn.getUsername()));
        assertEquals(elem.getPropertyValue("PASS"), TalendTextUtils.addQuotes(dbConn.getRawPassword()));
        assertEquals(elem.getPropertyValue("HOST"), TalendTextUtils.addQuotes(dbConn.getServerName()));
        assertEquals(elem.getPropertyValue("PORT"), TalendTextUtils.addQuotes(dbConn.getPort()));
        assertEquals(elem.getPropertyValue("DBNAME"), TalendTextUtils.addQuotes(dbConn.getDatasourceName()));
    }



    /**
     * Test method for {@link org.talend.designer.core.ui.editor.cmd.ChangeValuesFromRepository#undo()}.
     */
    @Test
    public void testUndo() {
        String oldValue = (String) elem.getPropertyValue("PROPERTY:PROPERTY_TYPE");
        String newValue = "REPOSITORY";
        if (oldValue.equals(newValue)) {
            newValue = "BUILT_IN";
        }
        ChangeValuesFromRepository changeValuesFromRepository = new ChangeValuesFromRepository(elem, connection,
                "PROPERTY:PROPERTY_TYPE", newValue);
        changeValuesFromRepository.execute();
        changeValuesFromRepository.undo();
        assertEquals(elem.getPropertyValue("PROPERTY:PROPERTY_TYPE"), oldValue);
    }

    /**
     * Test method for
     * {@link org.talend.designer.core.ui.editor.cmd.ChangeValuesFromRepository#getRealSourceNode(org.talend.core.model.process.INode)}
     * .
     */
    @Test
    public void testGetRealSourceNode() {
        ChangeValuesFromRepository changeValuesFromRepository = new ChangeValuesFromRepository(elem, connection,
                "PROPERTY:PROPERTY_TYPE", "REPOSITORY");

        assertEquals(changeValuesFromRepository.getRealSourceNode(target), elem);
    }

    private IMetadataTable createSimpleMetadata() {
        IMetadataTable table = new org.talend.core.model.metadata.MetadataTable();

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

    public static final String BUILTIN = "BUILT_IN"; //$NON-NLS-1$

    public static final String REPOSITORY = "REPOSITORY"; //$NON-NLS-1$

    public static final String TEXT_BUILTIN = Messages.getString("EmfComponent.builtIn"); //$NON-NLS-1$

    public static final String TEXT_REPOSITORY = Messages.getString("EmfComponent.repository"); //$NON-NLS-1$

    @Test
    public void testChangeValuesMultipleProperty() {
        Property property = PropertiesFactory.eINSTANCE.createProperty();
        IProcess2 process = new Process(property);
        IComponent sourceCom = ComponentsFactoryProvider.getInstance().get("tMysqlInput",
                ComponentCategory.CATEGORY_4_DI.getName());
        Node node = new Node(sourceCom, process);
        node.setLabel("tMysqlInput_1");
        IMetadataTable table = createSimpleMetadata();
        table.setAttachedConnector("FLOW");
        List<IMetadataTable> metadataList = new ArrayList<IMetadataTable>();
        metadataList.add(table);
        node.setMetadataList(metadataList);

        ElementParameter parentParam = new ElementParameter(node);
        parentParam.setCategory(EComponentCategory.BASIC);
        parentParam.setName("PROPERTY2");
        parentParam.setFieldType(EParameterFieldType.PROPERTY_TYPE);

        ElementParameter newParam = new ElementParameter(node);
        newParam.setCategory(EComponentCategory.BASIC);
        newParam.setName(EParameterName.PROPERTY_TYPE.getName());
        newParam.setDisplayName(EParameterName.PROPERTY_TYPE.getDisplayName());
        newParam.setListItemsDisplayName(new String[] { TEXT_BUILTIN, TEXT_REPOSITORY });
        newParam.setListItemsDisplayCodeName(new String[] { BUILTIN, REPOSITORY });
        newParam.setListItemsValue(new String[] { BUILTIN, REPOSITORY });

        newParam.setValue(BUILTIN);
        newParam.setNumRow(1);
        newParam.setFieldType(EParameterFieldType.TECHNICAL);
        newParam.setRepositoryValue("TEST");
        newParam.setShow(true);
        newParam.setParentParameter(parentParam);
        // listParam.add(newParam);

        newParam = new ElementParameter(node);
        newParam.setCategory(EComponentCategory.BASIC);
        newParam.setName(EParameterName.REPOSITORY_PROPERTY_TYPE.getName());
        newParam.setDisplayName(EParameterName.REPOSITORY_PROPERTY_TYPE.getDisplayName());
        newParam.setListItemsDisplayName(new String[] {});
        newParam.setListItemsValue(new String[] {});
        newParam.setNumRow(1);
        newParam.setFieldType(EParameterFieldType.TECHNICAL);
        newParam.setValue(""); //$NON-NLS-1$
        newParam.setShow(false);
        newParam.setRequired(true);
        newParam.setParentParameter(parentParam);

        ((List<IElementParameter>) node.getElementParameters()).add(parentParam);

        IElementParameter param = node.getElementParameter("USER");
        param.setRepositoryProperty("PROPERTY");
        param = node.getElementParameter("PASS");
        param.setRepositoryProperty("PROPERTY");
        param = node.getElementParameter("HOST");
        param.setRepositoryProperty("PROPERTY");
        param = node.getElementParameter("PORT");
        param.setRepositoryProperty("PROPERTY2");
        param = node.getElementParameter("DBNAME");
        param.setRepositoryProperty("PROPERTY2");

        ChangeValuesFromRepository changeValuesFromRepository = new ChangeValuesFromRepository(node, connection,
                "PROPERTY:REPOSITORY_PROPERTY_TYPE", databaseConnItem.getProperty().getId());
        changeValuesFromRepository.execute();

        DatabaseConnection dbConn = (DatabaseConnection) connection;
        assertEquals(databaseConnItem.getProperty().getId(), node.getPropertyValue("PROPERTY:REPOSITORY_PROPERTY_TYPE"));

        assertEquals(TalendTextUtils.addQuotes(dbConn.getUsername()), node.getPropertyValue("USER"));
        assertEquals(TalendTextUtils.addQuotes(dbConn.getRawPassword()), node.getPropertyValue("PASS"));
        assertEquals(TalendTextUtils.addQuotes(dbConn.getServerName()), node.getPropertyValue("HOST"));
        assertNotSame(TalendTextUtils.addQuotes(dbConn.getPort()), node.getPropertyValue("PORT"));
        assertNotSame(TalendTextUtils.addQuotes(dbConn.getDatasourceName()), node.getPropertyValue("DBNAME"));

        dbConn.setUsername("user2");
        dbConn.setPort("5002");
        dbConn.setDatasourceName("Test2");

        changeValuesFromRepository = new ChangeValuesFromRepository(node, connection, "PROPERTY2:REPOSITORY_PROPERTY_TYPE",
                databaseConnItem.getProperty().getId());
        changeValuesFromRepository.execute();

        assertNotSame(TalendTextUtils.addQuotes(dbConn.getUsername()), node.getPropertyValue("USER"));
        assertEquals(TalendTextUtils.addQuotes(dbConn.getPort()), node.getPropertyValue("PORT"));
        assertEquals(TalendTextUtils.addQuotes(dbConn.getDatasourceName()), node.getPropertyValue("DBNAME"));
    }

}
