package org.talend.designer.dbmap.language.generation;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.talend.commons.utils.VersionUtils;
import org.talend.core.CorePlugin;
import org.talend.core.context.Context;
import org.talend.core.context.RepositoryContext;
import org.talend.core.model.components.ComponentCategory;
import org.talend.core.model.components.IComponent;
import org.talend.core.model.context.JobContextManager;
import org.talend.core.model.metadata.IMetadataColumn;
import org.talend.core.model.metadata.IMetadataTable;
import org.talend.core.model.metadata.MetadataColumn;
import org.talend.core.model.metadata.MetadataTable;
import org.talend.core.model.process.EConnectionType;
import org.talend.core.model.process.IConnection;
import org.talend.core.model.process.INodeConnector;
import org.talend.core.model.properties.PropertiesFactory;
import org.talend.core.model.properties.Property;
import org.talend.core.model.utils.TalendTextUtils;
import org.talend.core.ui.component.ComponentsFactoryProvider;
import org.talend.designer.core.model.components.ElementParameter;
import org.talend.designer.core.model.components.NodeConnector;
import org.talend.designer.core.ui.editor.connections.Connection;
import org.talend.designer.core.ui.editor.nodes.Node;
import org.talend.designer.core.ui.editor.process.Process;
import org.talend.designer.dbmap.DbMapComponent;
import org.talend.designer.dbmap.external.data.ExternalDbMapData;
import org.talend.designer.dbmap.external.data.ExternalDbMapEntry;
import org.talend.designer.dbmap.external.data.ExternalDbMapTable;

public class DbGenerationManagerTest extends DbGenerationManagerTestHelper {

    private DbGenerationManager dbManager;

    private ExternalDbMapEntry extMapEntry;

    private List<ExternalDbMapEntry> tableEntries;

    private DbMapComponent dbMapComponent4SubQuery;

    private static IComponent sourceComponent;

    private static IComponent targetComponent;

    @Before
    public void setUp() throws Exception {
        dbMapComponent = new DbMapComponent();

        ExternalDbMapData externalDbMapData = new ExternalDbMapData();
        dbMapComponent.setExternalData(externalDbMapData);

        List<ExternalDbMapTable> inputTables = new ArrayList<>();
        externalDbMapData.setInputTables(inputTables);

        ExternalDbMapTable externalDbMapTable = new ExternalDbMapTable();
        inputTables.add(externalDbMapTable);
        externalDbMapTable.setName("t1");
        externalDbMapTable.setAlias("t1");
        externalDbMapTable.setTableName("t1");
        externalDbMapTable.setJoinType("NO_JOIN");

        tableEntries = new ArrayList<>();
        externalDbMapTable.setMetadataTableEntries(tableEntries);
        extMapEntry = new ExternalDbMapEntry("id", "t1.id");
        tableEntries.add(extMapEntry);

        List<IConnection> incomingConnections = new ArrayList<IConnection>();
        String[] columns = new String[] { "\\\"id\\\"" };
        incomingConnections.add(createConnection("t1", "t1", "id", columns));
        dbMapComponent.setIncomingConnections(incomingConnections);

        if (dbMapComponent.getElementParameters() == null) {
            dbMapComponent.setElementParameters(Collections.EMPTY_LIST);
        }
        dbManager = new GenericDbGenerationManager();

        //
        dbMapComponent4SubQuery = new DbMapComponent();
        ExternalDbMapData externalDbMapData4SubQuery = new ExternalDbMapData();
        dbMapComponent4SubQuery.setExternalData(externalDbMapData4SubQuery);

        List<ExternalDbMapTable> inputTables4SubQuery = new ArrayList<>();
        externalDbMapData.setInputTables(inputTables4SubQuery);

        ExternalDbMapTable externalDbMapTable4SubQuery = new ExternalDbMapTable();
        inputTables.add(externalDbMapTable4SubQuery);
        externalDbMapTable4SubQuery.setName("t5");
        externalDbMapTable4SubQuery.setAlias("t5");
        externalDbMapTable4SubQuery.setTableName("t5");
        externalDbMapTable4SubQuery.setJoinType("NO_JOIN");

        List<ExternalDbMapEntry> tableEntries4SubQuery = new ArrayList<>();
        externalDbMapTable4SubQuery.setMetadataTableEntries(tableEntries4SubQuery);
        ExternalDbMapEntry extMapEntry4SubQuery = new ExternalDbMapEntry("id", "t5.id");
        tableEntries4SubQuery.add(extMapEntry4SubQuery);

        List<IConnection> incomingConnections4SubQuery = new ArrayList<IConnection>();
        String[] columns4SubQuery = new String[] { "\\\"id\\\"" };
        incomingConnections4SubQuery.add(createConnection("t5", "t5", "id", columns4SubQuery));
        dbMapComponent4SubQuery.setIncomingConnections(incomingConnections4SubQuery);

        if (dbMapComponent4SubQuery.getElementParameters() == null) {
            dbMapComponent4SubQuery.setElementParameters(Collections.EMPTY_LIST);
        }

        sourceComponent = ComponentsFactoryProvider.getInstance().get("tELTOracleMap", ComponentCategory.CATEGORY_4_DI.getName());
        targetComponent = ComponentsFactoryProvider.getInstance().get("tELTOracleMap", ComponentCategory.CATEGORY_4_DI.getName());
    }

    @After
    public void tearDown() throws Exception {
        dbMapComponent = null;
        dbManager = null;
        extMapEntry = null;
        tableEntries = null;

        dbMapComponent4SubQuery = null;
        sourceComponent = null;
        targetComponent = null;
    }

    @Test
    public void testInitExpression() {
        checkValue("t1.\\\"id\\\"");
    }

    public void checkValue(String expression) {
        Assert.assertEquals(expression, dbManager.initExpression(dbMapComponent, extMapEntry));
    }

    private IConnection createConnection(String schemaName, String tableName, String label, String[] columns) {
        Connection connection = mock(Connection.class);
        when(connection.getName()).thenReturn(tableName);
        IMetadataTable metadataTable = new MetadataTable();
        metadataTable.setLabel(tableName);
        metadataTable.setTableName(tableName);
        List<IMetadataColumn> listColumns = new ArrayList<IMetadataColumn>();
        for (String columnName : columns) {
            IMetadataColumn column = new MetadataColumn();
            column.setLabel(label);
            column.setOriginalDbColumnName(columnName);
            listColumns.add(column);
        }
        metadataTable.setListColumns(listColumns);
        when(connection.getMetadataTable()).thenReturn(metadataTable);

        return connection;
    }

    @Test
    public void testGetHandledTableName() {
        dbManager = new GenericDbGenerationManager();
        String schema = "";
        String main_table = "\"mainTable\"";
        String main_alias = null;
        init(schema, main_table, main_alias, "", "");
        String tableName = "";
        if (!"".equals(schema)) {
            tableName = TalendTextUtils.removeQuotes(schema) + ".";
        }
        tableName = TalendTextUtils.removeQuotes(main_table);
        String expectedValue = "mainTable";
        String handledTableName = dbManager.getHandledTableName(dbMapComponent, tableName, main_alias);
        Assert.assertEquals(expectedValue, handledTableName);

        main_table = "((String)globalMap.get(\"#main_table%\"))";
        main_alias = null;
        init(schema, main_table, main_alias, "", "");
        tableName = "";
        if (!"".equals(schema)) {
            tableName = TalendTextUtils.removeQuotes(schema) + ".";
        }
        tableName = TalendTextUtils.removeQuotes(main_table);
        expectedValue = "\" +((String)globalMap.get(\"#main_table%\"))+ \"";
        handledTableName = dbManager.getHandledTableName(dbMapComponent, tableName, main_alias);
        Assert.assertEquals(expectedValue, handledTableName);

        main_table = "((String)globalMap.get(\"#main_table%\"))";
        main_alias = "main_table";
        init(schema, main_table, main_alias, "", "");
        tableName = "";
        if (!"".equals(schema)) {
            tableName = TalendTextUtils.removeQuotes(schema) + ".";
        }
        tableName = TalendTextUtils.removeQuotes(main_table);
        expectedValue = "\" +((String)globalMap.get(\"#main_table%\"))+ \"";
        handledTableName = dbManager.getHandledTableName(dbMapComponent, tableName, main_alias);
        Assert.assertEquals(expectedValue, handledTableName);

        main_table = "context.main_table";
        main_alias = "main_table";
        init(schema, main_table, main_alias, "", "");
        tableName = "";
        if (!"".equals(schema)) {
            tableName = TalendTextUtils.removeQuotes(schema) + ".";
        }
        tableName = TalendTextUtils.removeQuotes(main_table);
        expectedValue = "\" +context.main_table+ \"";
        handledTableName = dbManager.getHandledTableName(dbMapComponent, tableName, main_alias);
        Assert.assertEquals(expectedValue, handledTableName);

        main_table = "\"abc\"+context.main_table";
        main_alias = "main_table";
        init(schema, main_table, main_alias, "", "");
        tableName = "";
        if (!"".equals(schema)) {
            tableName = TalendTextUtils.removeQuotes(schema) + ".";
        }
        tableName = TalendTextUtils.removeQuotes(main_table);
        expectedValue = "\" +\"abc\"+context.main_table+ \"";
        handledTableName = dbManager.getHandledTableName(dbMapComponent, tableName, main_alias);
        Assert.assertEquals(expectedValue, handledTableName);

    }

    @Test
    public void testBuildSqlSelectForMiddleTable() {
        String schema = "school";
        String main_table = "classInfo";
        String lookup_table = "scoreInfo";
        init(schema, main_table, lookup_table);
        dbManager = new GenericDbGenerationManager();
        String expectedQuery = "\"SELECT\n"
                + "school.classInfo.id, school.classInfo.name, school.classInfo.classNum, school.scoreInfo.score\n"
                + "FROM\n school.classInfo , school.scoreInfo\n" + "WHERE school.classInfo\"";
        String query = dbManager.buildSqlSelect(dbMapComponent, "grade");
        assertEquals(expectedQuery, query);
    }

    private void init(String schema, String main_table, String lookup_table) {
        Process process = new Process(createProperty());
        process.setContextManager(new JobContextManager());
        dbMapComponent.setProcess(process);

        List<IConnection> incomingConnections = new ArrayList<IConnection>();
        String[] mainTableEntities = new String[] { "id", "name", "classNum" };
        String[] lookupEndtities = new String[] { "id", "score" };

        incomingConnections.add(createConnection(process, schema, lookup_table, lookupEndtities, lookupEndtities));
        incomingConnections.add(mockConnection(schema, lookup_table, lookupEndtities, lookupEndtities));
        dbMapComponent.setIncomingConnections(incomingConnections);

        ExternalDbMapData externalData = new ExternalDbMapData();
        List<ExternalDbMapTable> inputs = new ArrayList<ExternalDbMapTable>();
        List<ExternalDbMapTable> outputs = new ArrayList<ExternalDbMapTable>();
        // main table
        ExternalDbMapTable inputTable = new ExternalDbMapTable();
        inputTable.setName(schema + "." + main_table);
        inputTable.setTableName(schema + "." + main_table);
        String[] mainExpressions = new String[] { schema + "." + lookup_table + ".id", schema + "." + lookup_table + ".name",
                schema + "." + lookup_table + ".classNum" };
        List<ExternalDbMapEntry> entities = getMetadataEntities(mainTableEntities, mainExpressions);
        inputTable.setMetadataTableEntries(entities);
        inputs.add(inputTable);
        // lookup table
        inputTable = new ExternalDbMapTable();
        inputTable.setName(schema + "." + lookup_table);
        inputTable.setTableName(schema + "." + lookup_table);
        String[] lookupExpressions = new String[] { schema + "." + main_table + ".id", schema + "." + main_table + ".name" };
        entities = getMetadataEntities(lookupEndtities, lookupExpressions);
        inputTable.setMetadataTableEntries(entities);
        inputs.add(inputTable);

        // output
        ExternalDbMapTable outputTable = new ExternalDbMapTable();
        outputTable.setName("grade");
        List<ExternalDbMapEntry> conditions = new ArrayList<ExternalDbMapEntry>();
        conditions.add(new ExternalDbMapEntry(schema + "." + main_table));
        outputTable.setCustomWhereConditionsEntries(conditions);
        String[] names = new String[] { "id", "name", "classNum", "score" };
        String[] expressions = new String[] { schema + "." + main_table + ".id", schema + "." + main_table + ".name",
                schema + "." + main_table + ".classNum", schema + "." + lookup_table + ".score" };
        outputTable.setMetadataTableEntries(getMetadataEntities(names, expressions));
        outputs.add(outputTable);

        externalData.setInputTables(inputs);
        externalData.setOutputTables(outputs);
        dbMapComponent.setExternalData(externalData);
        List<IMetadataTable> metadataList = new ArrayList<IMetadataTable>();
        MetadataTable metadataTable = getMetadataTable(names, names);
        metadataTable.setLabel("grade");
        metadataList.add(metadataTable);
        dbMapComponent.setMetadataList(metadataList);
    }

    private IConnection mockConnection(String schemaName, String tableName, String[] columns, String[] dbColumns) {
        Connection connection = mock(Connection.class);
        Node node = mock(Node.class);
        ElementParameter param = new ElementParameter(node);
        param.setName("ELT_SCHEMA_NAME");
        param.setValue(schemaName);
        when(node.getElementParameter("ELT_SCHEMA_NAME")).thenReturn(param);
        param = new ElementParameter(node);
        param.setName("ELT_TABLE_NAME");
        param.setValue(tableName);
        when(node.getElementParameter("ELT_TABLE_NAME")).thenReturn(param);
        when(connection.getName()).thenReturn(schemaName + "." + tableName);
        when(connection.getSource()).thenReturn(node);
        IMetadataTable table = new MetadataTable();
        table.setLabel(tableName);
        table.setTableName(tableName);
        List<IMetadataColumn> listColumns = new ArrayList<IMetadataColumn>();
        for (int i = 0; i < columns.length; i++) {
            String columnName = columns[i];
            IMetadataColumn column = new MetadataColumn();
            column.setLabel(columnName);
            column.setOriginalDbColumnName(dbColumns[i]);
            listColumns.add(column);
        }
        table.setListColumns(listColumns);
        when(connection.getMetadataTable()).thenReturn(table);
        return connection;
    }

    private IConnection createConnection(Process process, String schemaName, String tableName, String[] columns,
            String[] dbColumns) {
        Node sourceNode = new Node(sourceComponent, process);
        INodeConnector connector = new NodeConnector(sourceNode);
        connector.setName("connector");
        connector.setDefaultConnectionType(EConnectionType.FLOW_MAIN);
        ArrayList<INodeConnector> connectors = new ArrayList<INodeConnector>();
        connectors.add(connector);
        sourceNode.setExternalNode(dbMapComponent4SubQuery);
        sourceNode.setListConnector(connectors);

        Node targetNode = new Node(targetComponent, process);
        targetNode.setExternalNode(dbMapComponent);
        IConnection connection = new Connection(sourceNode, targetNode, EConnectionType.FLOW_MAIN, "connector", "meta",
                "school.classInfo", true);
        List<IMetadataTable> metadataList = new ArrayList<IMetadataTable>();
        IMetadataTable table = new MetadataTable();
        table.setLabel(tableName);
        table.setTableName(tableName);
        table.setAttachedConnector("connector");
        List<IMetadataColumn> listColumns = new ArrayList<IMetadataColumn>();
        for (int i = 0; i < columns.length; i++) {
            String columnName = columns[i];
            IMetadataColumn column = new MetadataColumn();
            column.setLabel(columnName);
            column.setOriginalDbColumnName(dbColumns[i]);
            listColumns.add(column);
        }
        table.setListColumns(listColumns);
        metadataList.add(table);
        sourceNode.setMetadataList(metadataList);
        return connection;
    }

    private static Property createProperty() {
        Property property = PropertiesFactory.eINSTANCE.createProperty();
        property.setAuthor(((RepositoryContext) CorePlugin.getContext().getProperty(Context.REPOSITORY_CONTEXT_KEY)).getUser());
        property.setVersion(VersionUtils.DEFAULT_VERSION);
        return property;
    }
    
    @Test
    public void testAppendSqlQuery(){
    	StringBuilder sb = new StringBuilder();
    	dbManager.getQuerySegments().clear();
    	dbManager.appendSqlQuery(sb, "table1", true);
    	Assert.assertTrue(dbManager.getQuerySegments().size()==1);
    	
    	dbManager.getQuerySegments().clear();
    	dbManager.appendSqlQuery(sb, "table1", false);
    	Assert.assertTrue(dbManager.getQuerySegments().isEmpty());
    }
}
