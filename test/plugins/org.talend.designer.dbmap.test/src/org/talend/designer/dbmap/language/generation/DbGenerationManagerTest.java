package org.talend.designer.dbmap.language.generation;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
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
import org.talend.core.model.context.JobContext;
import org.talend.core.model.context.JobContextManager;
import org.talend.core.model.context.JobContextParameter;
import org.talend.core.model.metadata.IMetadataColumn;
import org.talend.core.model.metadata.IMetadataTable;
import org.talend.core.model.metadata.MetadataColumn;
import org.talend.core.model.metadata.MetadataTable;
import org.talend.core.model.process.EConnectionType;
import org.talend.core.model.process.IConnection;
import org.talend.core.model.process.IContextParameter;
import org.talend.core.model.process.INodeConnector;
import org.talend.core.model.properties.PropertiesFactory;
import org.talend.core.model.properties.Property;
import org.talend.core.model.utils.TalendTextUtils;
import org.talend.core.ui.component.ComponentsFactoryProvider;
import org.talend.designer.core.model.components.EParameterName;
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
        String[] columns = new String[] { "\\\"id\\\"",  "\\\"name\\\""};
        String[] labels = new String[] { "id",  "name"};
        incomingConnections.add(createConnection("t1", "t1", labels, columns));
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
        String[] labelSubQuery = new String[] { "id" };
        incomingConnections4SubQuery.add(createConnection("t5", "t5", labelSubQuery, columns4SubQuery));
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
    	checkValue("t1.\\\"id\\\"", extMapEntry);
        
        ExternalDbMapEntry extMapEntry2 = new ExternalDbMapEntry("multiple", "t1.id + t1.name");
        tableEntries.add(extMapEntry2);
        checkValue("t1.\\\"id\\\" + t1.\\\"name\\\"", extMapEntry2);
        
        testWithQuote();
    }
    
    @Test
    public void testReplaceAuotes() {
        String quote = "\"";
        String quoParser = "[\\\\]?\\" + quote; //$NON-NLS-1$
        DbGenerationManager dbManager = new GenericDbGenerationManager();
        
        String expression = "'\"" + "+" + "(String)globalMap.get(\"BusinessDateStr\")" + "+" + "\"' BETWEEN PROD_GRP_DA.EFF_FRM_DT AND PROD_GRP_DA.EFF_TO_DT";
        
        assertEquals(expression.trim(), dbManager.replaceAuotes(expression, quoParser, quote).trim());
        
        expression = "'\"" + "+" + "(String)globalMap.get(\"BusinessDateStr\")" + "+" + "\" BETWEEN PROD_GRP_DA.EFF_FRM_DT AND PROD_GRP_DA.EFF_TO_DT";
        String expect = "'\\\"" + "+" + "(String)globalMap.get(\\\"BusinessDateStr\\\")" + "+" + "\\\" BETWEEN PROD_GRP_DA.EFF_FRM_DT AND PROD_GRP_DA.EFF_TO_DT";
        
        assertEquals(expect.trim(), dbManager.replaceAuotes(expression, quoParser, quote).trim());
        
        expression = "BETWEEN PROD_GRP_DA.EFF_FRM_DT AND PROD_GRP_DA.EFF_TO_DT";
        expect = "BETWEEN PROD_GRP_DA.EFF_FRM_DT AND PROD_GRP_DA.EFF_TO_DT";
        assertEquals(expect.trim(), dbManager.replaceAuotes(expression, quoParser, quote).trim());
        
        expression = "'" +"+"+"context.param1"+"+"+ "aaa";
        expect = "'" +"+"+"context.param1"+"+"+ "aaa";
        
        assertEquals(expect.trim(), dbManager.replaceAuotes(expression, quoParser, quote).trim());
        
        expression = "'\"" +"+"+"context.param1"+"+"+ "\"'aaa";
        expect = "'\"" +"+"+"context.param1"+"+"+ "\"'aaa";
        
        assertEquals(expect.trim(), dbManager.replaceAuotes(expression, quoParser, quote).trim());
        
        expression = "'context.param1'";
        assertEquals(expression, dbManager.replaceAuotes(expression, quoParser, quote).trim());
        
        expression = "table1.name = 'context.param2'";
        assertEquals(expression, dbManager.replaceAuotes(expression, quoParser, quote).trim());
        
        expression = "table1.name = 'context.param2' aa";
        assertEquals(expression, dbManager.replaceAuotes(expression, quoParser, quote).trim());
    }
    
    private void testWithQuote(){
    	dbManager.setUseDelimitedIdentifiers(true);
    	List<IConnection> incomingConnections = new ArrayList<IConnection>();
        String[] columns = new String[] { "id",  "name"};
        String[] labels = new String[] { "id",  "name"};
        incomingConnections.add(createConnection("t1", "t1", labels, columns));
        dbMapComponent.setIncomingConnections(incomingConnections);
        
        ElementParameter param = new ElementParameter(dbMapComponent);
    	param.setName(EParameterName.MAPPING.getName());
    	param.setValue("mysql_id");
    	List<ElementParameter> list = new ArrayList<>();
    	list.add(param);
    	dbMapComponent.setElementParameters(list);
    	checkValue("t1.`id`", extMapEntry);
    	
    	ExternalDbMapEntry extMapEntry3 = new ExternalDbMapEntry("multiple", "if(t1.id<2500,\"<2500\",\">=2500\")");
        tableEntries.add(extMapEntry3);
        checkValue("if(t1.`id`<2500,\\\"<2500\\\",\\\">=2500\\\")", extMapEntry3);
    	
    	ExternalDbMapEntry extMapEntry2 = new ExternalDbMapEntry("multiple", "t1.id + t1.name");
        tableEntries.add(extMapEntry2);
        checkValue("t1.`id` + t1.`name`", extMapEntry2);
    	
    	param.setValue("oracle_id");
    	checkValue("t1.\\\"id\\\"", extMapEntry);
    	checkValue("t1.\\\"id\\\" + t1.\\\"name\\\"", extMapEntry2);
    	
    	param.setValue("mssql_id");
    	checkValue("t1.\\\"id\\\"", extMapEntry);
    	checkValue("t1.\\\"id\\\" + t1.\\\"name\\\"", extMapEntry2);
    }

    public void checkValue(String expression, ExternalDbMapEntry entry) {
        Assert.assertEquals(expression, dbManager.initExpression(dbMapComponent, entry));
    }

    private IConnection createConnection(String schemaName, String tableName, String[] labels, String[] columns) {
        Connection connection = mock(Connection.class);
        when(connection.getName()).thenReturn(tableName);
        IMetadataTable metadataTable = new MetadataTable();
        metadataTable.setLabel(tableName);
        metadataTable.setTableName(tableName);
        List<IMetadataColumn> listColumns = new ArrayList<IMetadataColumn>();
        for(int i =0; i < columns.length; i++){
        	IMetadataColumn column = new MetadataColumn();
            column.setLabel(labels[i]);
            column.setOriginalDbColumnName(columns[i]);
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
    
    @Test
    public void testBuildSqlSelect() {
        runBuildSql("","table1","table2","table3");
        
    }
    
    private void runBuildSql(String schema, String table1, String table2, String table3) {
        List<IConnection> incomingConnections = new ArrayList<IConnection>();
        String[] mainTableEntities = new String[] { "id", "name" };
        incomingConnections.add(mockConnection(schema, table1, mainTableEntities));
        dbMapComponent.setIncomingConnections(incomingConnections);

        ExternalDbMapData externalData = new ExternalDbMapData();
        List<ExternalDbMapTable> inputs = new ArrayList<ExternalDbMapTable>();
        List<ExternalDbMapTable> outputs = new ArrayList<ExternalDbMapTable>();
        // main table
        ExternalDbMapTable inputTable = new ExternalDbMapTable();
        String mainTableName = "".equals(schema) ? table1 : schema + "." + table1;
        // quote will be removed in the ui for connections ,so we do the same for test
        String mainTableNameNoQuote = TalendTextUtils.removeQuotes(mainTableName);
        inputTable.setTableName(mainTableNameNoQuote);
        inputTable.setName(mainTableName);
        List<ExternalDbMapEntry> entities = getMetadataEntities(mainTableEntities, new String[3]);
        inputTable.setMetadataTableEntries(entities);
        inputs.add(inputTable);

        // output
        ExternalDbMapTable outputTable = new ExternalDbMapTable();
        mainTableName = "".equals(schema) ? table2 : schema + "." + table2;
        // quote will be removed in the ui for connections ,so we do the same for test
        mainTableNameNoQuote = TalendTextUtils.removeQuotes(mainTableName);
        outputTable.setTableName(mainTableNameNoQuote);
        outputTable.setName("table2");
        String[] names = new String[] { "id", "name" };
        String mainTable = mainTableName;
        String[] expressions = new String[] { "table1.id", "table1.name"};
        outputTable.setMetadataTableEntries(getMetadataEntities(names, expressions));
        
        String[] whereNames = new String[]{"whereFilter"};
        String[] whereExps = new String[]{"table1.name = " + "'context.param2'"};
        outputTable.setCustomWhereConditionsEntries(getMetadataEntities(whereNames, whereExps));
        
        String[] otherNames = new String[]{"otherFilter"};
        String[] otherExps = new String[]{"table1.id = " + "'context.param1'"};
        outputTable.setCustomOtherConditionsEntries(getMetadataEntities(otherNames, otherExps));
        outputs.add(outputTable);

        externalData.setInputTables(inputs);
        externalData.setOutputTables(outputs);
        dbMapComponent.setExternalData(externalData);
        List<IMetadataTable> metadataList = new ArrayList<IMetadataTable>();
        MetadataTable metadataTable = getMetadataTable(names);
        metadataTable.setLabel("table2");
        metadataList.add(metadataTable);
        dbMapComponent.setMetadataList(metadataList);
        JobContext newContext = new JobContext("Default");
        List<IContextParameter> newParamList = new ArrayList<IContextParameter>();
        newContext.setContextParameterList(newParamList);
        JobContextParameter param = new JobContextParameter();
        param.setName("schema");
        newParamList.add(param);
        process = mock(Process.class);
        JobContextManager contextManger = new JobContextManager();
        contextManger.setDefaultContext(newContext);
        when(process.getContextManager()).thenReturn(contextManger);
        dbMapComponent.setProcess(process);
        ExternalDbMapData externalData2 = new ExternalDbMapData();
        DbMapComponent dbMapComponent2 = new DbMapComponent();
        dbMapComponent2.setExternalData(externalData2);
        mainTableEntities = new String[] {"id", "name"};
        
        List<IConnection> outgoingConnections = new ArrayList<IConnection>();
        Node map1 = mockNode(dbMapComponent);
        outgoingConnections.add(mockConnection(map1, schema, table2, mainTableEntities));
        dbMapComponent.setOutgoingConnections(outgoingConnections);
        dbMapComponent2.setIncomingConnections(outgoingConnections);

        inputs = new ArrayList<ExternalDbMapTable>();
        outputs = new ArrayList<ExternalDbMapTable>();
        // main table
        inputTable = new ExternalDbMapTable();
        mainTableName = "".equals(schema) ? table2 : schema + "." + table2;
        // quote will be removed in the ui for connections ,so we do the same for test
        mainTableNameNoQuote = TalendTextUtils.removeQuotes(mainTableName);
        inputTable.setTableName(mainTableNameNoQuote);
        inputTable.setName("table2");
        entities = getMetadataEntities(mainTableEntities, new String[3]);
        inputTable.setMetadataTableEntries(entities);
        inputs.add(inputTable);

        // output
        outputTable = new ExternalDbMapTable();
        mainTableName = "".equals(schema) ? table3 : schema + "." + table3;
        // quote will be removed in the ui for connections ,so we do the same for test
        mainTableNameNoQuote = TalendTextUtils.removeQuotes(mainTableName);
        outputTable.setTableName(mainTableNameNoQuote);
        outputTable.setName("table3");
        names = new String[] { "id", "name"};
        mainTable = mainTableName;
        expressions = new String[] { "table2.id", "table2.name"};
        outputTable.setMetadataTableEntries(getMetadataEntities(names, expressions));
        outputs.add(outputTable);

        externalData2.setInputTables(inputs);
        externalData2.setOutputTables(outputs);
        dbMapComponent2.setExternalData(externalData2);
        metadataList = new ArrayList<IMetadataTable>();
        metadataTable = getMetadataTable(names);
        metadataTable.setLabel("table3");
        metadataList.add(metadataTable);
        dbMapComponent2.setMetadataList(metadataList);
        if (dbMapComponent2.getElementParameters() == null) {
            dbMapComponent2.setElementParameters(Collections.EMPTY_LIST);
        }
        JobContextParameter param1 = new JobContextParameter();
        param1.setName("param1");
        newParamList.add(param1);
        
        JobContextParameter param2 = new JobContextParameter();
        param2.setName("param2");
        newParamList.add(param2);
        dbMapComponent2.setProcess(process);
        outgoingConnections = new ArrayList<IConnection>();
        outgoingConnections.add(mockConnection(schema, table3, mainTableEntities));
        dbMapComponent2.setOutgoingConnections(outgoingConnections);
        
        ElementParameter comName = new ElementParameter(dbMapComponent);
        comName.setName("COMPONENT_NAME");
        comName.setValue("tELTMap");
        List<ElementParameter> list = new ArrayList<>();
        list.add(comName);
        dbMapComponent.setElementParameters(list);
        
        dbManager = new GenericDbGenerationManager();
        String query = dbManager.buildSqlSelect(dbMapComponent2, "table3").replaceAll("\n", "");
        String exceptQuery = "\"SELECT\n"
                + "table2.id, table2.name\n"
                + "FROM\n"
                + " (\n"
                +"  SELECT\n"
                +"  table1.id AS id, table1.name AS name\n"
                +"  FROM\n"
                +"   table1\n"
                +"  WHERE table1.name = '\" +context.param2+ \"'\n"
                +"  table1.id = '\" +context.param1+ \"'\n"
                +"  \n"
                +" ) table2\"";
        assertEquals(exceptQuery.replaceAll("\n", "").trim(), query.trim());
    }

    @Test
    public void testBuildSqlSelect_endWithContext() {

        String schema = "";
        String table1 = "table1";
        String table2 = "table2";
        String table3 = "table3";
        List<IConnection> incomingConnections = new ArrayList<IConnection>();
        String[] mainTableEntities = new String[] { "id", "column1", "column2" };
        incomingConnections.add(mockConnection(schema, table1, mainTableEntities));
        dbMapComponent.setIncomingConnections(incomingConnections);

        ExternalDbMapData externalData = new ExternalDbMapData();
        List<ExternalDbMapTable> inputs = new ArrayList<ExternalDbMapTable>();
        List<ExternalDbMapTable> outputs = new ArrayList<ExternalDbMapTable>();
        // main table
        ExternalDbMapTable inputTable = new ExternalDbMapTable();
        String mainTableName = "".equals(schema) ? table1 : schema + "." + table1;
        // quote will be removed in the ui for connections ,so we do the same for test
        String mainTableNameNoQuote = TalendTextUtils.removeQuotes(mainTableName);
        inputTable.setTableName(mainTableNameNoQuote);
        inputTable.setName(mainTableName);
        List<ExternalDbMapEntry> entities = getMetadataEntities(mainTableEntities, new String[3]);
        inputTable.setMetadataTableEntries(entities);
        inputs.add(inputTable);

        // output
        ExternalDbMapTable outputTable = new ExternalDbMapTable();
        mainTableName = "".equals(schema) ? table2 : schema + "." + table2;
        // quote will be removed in the ui for connections ,so we do the same for test
        mainTableNameNoQuote = TalendTextUtils.removeQuotes(mainTableName);
        outputTable.setTableName(mainTableNameNoQuote);
        outputTable.setName("table2");
        String[] names = new String[] { "id", "column1", "column2" };
        String mainTable = mainTableName;
        String[] expressions = new String[] { "table1.id",
                "CASE WHEN table1.column1 IS NULL THEN context.param1 ELSE table1.column1 END", "table1.column2" };
        outputTable.setMetadataTableEntries(getMetadataEntities(names, expressions));

        String[] whereNames = new String[] { "whereFilter" };
        String[] whereExps = new String[] { "t.column2 = context.param2" };
        outputTable.setCustomWhereConditionsEntries(getMetadataEntities(whereNames, whereExps));
        outputs.add(outputTable);
        externalData.setInputTables(inputs);
        externalData.setOutputTables(outputs);
        dbMapComponent.setExternalData(externalData);
        List<IMetadataTable> metadataList = new ArrayList<IMetadataTable>();
        MetadataTable metadataTable = getMetadataTable(names);
        metadataTable.setLabel("table2");
        metadataList.add(metadataTable);
        dbMapComponent.setMetadataList(metadataList);
        JobContext newContext = new JobContext("Default");
        List<IContextParameter> newParamList = new ArrayList<IContextParameter>();
        newContext.setContextParameterList(newParamList);
        JobContextParameter param = new JobContextParameter();
        param.setName("schema");
        newParamList.add(param);
        process = mock(Process.class);
        JobContextManager contextManger = new JobContextManager();
        contextManger.setDefaultContext(newContext);
        when(process.getContextManager()).thenReturn(contextManger);
        dbMapComponent.setProcess(process);

        ExternalDbMapData externalData2 = new ExternalDbMapData();
        DbMapComponent dbMapComponent2 = new DbMapComponent();
        dbMapComponent2.setExternalData(externalData2);
        mainTableEntities = new String[] { "id", "column1", "column2" };

        List<IConnection> outgoingConnections = new ArrayList<IConnection>();
        Node map1 = mockNode(dbMapComponent);
        outgoingConnections.add(mockConnection(map1, schema, table2, mainTableEntities));
        dbMapComponent.setOutgoingConnections(outgoingConnections);
        dbMapComponent2.setIncomingConnections(outgoingConnections);

        inputs = new ArrayList<ExternalDbMapTable>();
        outputs = new ArrayList<ExternalDbMapTable>();
        // main table
        inputTable = new ExternalDbMapTable();
        mainTableName = "".equals(schema) ? table2 : schema + "." + table2;
        // quote will be removed in the ui for connections ,so we do the same for test
        mainTableNameNoQuote = TalendTextUtils.removeQuotes(mainTableName);
        inputTable.setTableName(mainTableNameNoQuote);
        inputTable.setName("table2");
        entities = getMetadataEntities(mainTableEntities, new String[3]);
        inputTable.setMetadataTableEntries(entities);
        inputs.add(inputTable);

        // output
        outputTable = new ExternalDbMapTable();
        mainTableName = "".equals(schema) ? table3 : schema + "." + table3;
        // quote will be removed in the ui for connections ,so we do the same for test
        mainTableNameNoQuote = TalendTextUtils.removeQuotes(mainTableName);
        outputTable.setTableName(mainTableNameNoQuote);
        outputTable.setName("table3");
        names = new String[] { "id", "column1", "column2" };
        mainTable = mainTableName;
        expressions = new String[] { "table2.id", "table2.column1", "table2.column2" };
        outputTable.setMetadataTableEntries(getMetadataEntities(names, expressions));
        whereNames = new String[] { "whereFilter" };
        whereExps = new String[] { "t.column2A = context.param2A" };
        outputTable.setCustomWhereConditionsEntries(getMetadataEntities(whereNames, whereExps));
        outputs.add(outputTable);

        externalData2.setInputTables(inputs);
        externalData2.setOutputTables(outputs);
        dbMapComponent2.setExternalData(externalData2);
        metadataList = new ArrayList<IMetadataTable>();
        metadataTable = getMetadataTable(names);
        metadataTable.setLabel("table3");
        metadataList.add(metadataTable);
        dbMapComponent2.setMetadataList(metadataList);
        if (dbMapComponent2.getElementParameters() == null) {
            dbMapComponent2.setElementParameters(Collections.EMPTY_LIST);
        }
        JobContextParameter param1 = new JobContextParameter();
        param1.setName("param1");
        newParamList.add(param1);

        JobContextParameter param2 = new JobContextParameter();
        param2.setName("param2");
        newParamList.add(param2);
        dbMapComponent2.setProcess(process);
        outgoingConnections = new ArrayList<IConnection>();
        outgoingConnections.add(mockConnection(schema, table3, mainTableEntities));
        dbMapComponent2.setOutgoingConnections(outgoingConnections);

        ElementParameter comName = new ElementParameter(dbMapComponent);
        comName.setName("COMPONENT_NAME");
        comName.setValue("tELTMap");
        List<ElementParameter> list = new ArrayList<>();
        list.add(comName);
        dbMapComponent.setElementParameters(list);

        dbManager = new GenericDbGenerationManager() {

            @Override
            protected java.util.List<String> getContextList(DbMapComponent component) {
                return Arrays.asList("context.param1", "context.param2", "context.param2A");
            };
        };

        String query = dbManager.buildSqlSelect(dbMapComponent2, "table3");
        String exceptQuery = "\"SELECT\n" + 
                "table2.id, table2.column1, table2.column2\n" + 
                "FROM\n" + 
                " (\n" + 
                "  SELECT\n" + 
                "  table1.id AS id, CASE WHEN table1.column1 IS NULL THEN \" +context.param1+ \" ELSE table1.column1 END AS column1, table1.column2 AS column2\n" + 
                "  FROM\n" + 
                "   table1\n" + 
                "  WHERE t.column2 = \" +context.param2+ \"\n" + 
                " ) table2\n" + 
                "WHERE t.column2A = \" +context.param2A";
        assertEquals(exceptQuery.trim(), query.trim());

    }

    @Test
    public void testSubQueryBuildSqlSelect() {
        String schema = "";
        String table1 = "table1";
        String table2 = "table2";
        String table3 = "table3";
        List<IConnection> incomingConnections = new ArrayList<IConnection>();
        String[] mainTableEntities = new String[] { "id", "column1", "column2" };
        incomingConnections.add(mockConnection(schema, table1, mainTableEntities));
        dbMapComponent.setIncomingConnections(incomingConnections);

        ExternalDbMapData externalData = new ExternalDbMapData();
        List<ExternalDbMapTable> inputs = new ArrayList<ExternalDbMapTable>();
        List<ExternalDbMapTable> outputs = new ArrayList<ExternalDbMapTable>();
        // main table
        ExternalDbMapTable inputTable = new ExternalDbMapTable();
        String mainTableName = "".equals(schema) ? table1 : schema + "." + table1;
        // quote will be removed in the ui for connections ,so we do the same for test
        String mainTableNameNoQuote = TalendTextUtils.removeQuotes(mainTableName);
        inputTable.setTableName(mainTableNameNoQuote);
        inputTable.setName(mainTableName);
        List<ExternalDbMapEntry> entities = getMetadataEntities(mainTableEntities, new String[3]);
        inputTable.setMetadataTableEntries(entities);
        inputs.add(inputTable);

        // output
        ExternalDbMapTable outputTable = new ExternalDbMapTable();
        mainTableName = "".equals(schema) ? table2 : schema + "." + table2;
        // quote will be removed in the ui for connections ,so we do the same for test
        mainTableNameNoQuote = TalendTextUtils.removeQuotes(mainTableName);
        outputTable.setTableName(mainTableNameNoQuote);
        outputTable.setName("table2");
        String[] names = new String[] { "id", "column1", "column2" };
        String mainTable = mainTableName;
        String[] expressions = new String[] { "table1.id",
                "CASE WHEN table1.column1 IS NULL THEN context.param1 ELSE table1.column1 END", "table1.column2" };
        outputTable.setMetadataTableEntries(getMetadataEntities(names, expressions));

        String[] whereNames = new String[] { "whereFilter" };
        String[] whereExps = new String[] { "t.column2 = 'context.param2'" };
        outputTable.setCustomWhereConditionsEntries(getMetadataEntities(whereNames, whereExps));
        outputs.add(outputTable);
        externalData.setInputTables(inputs);
        externalData.setOutputTables(outputs);
        dbMapComponent.setExternalData(externalData);
        List<IMetadataTable> metadataList = new ArrayList<IMetadataTable>();
        MetadataTable metadataTable = getMetadataTable(names);
        metadataTable.setLabel("table2");
        metadataList.add(metadataTable);
        dbMapComponent.setMetadataList(metadataList);
        JobContext newContext = new JobContext("Default");
        List<IContextParameter> newParamList = new ArrayList<IContextParameter>();
        newContext.setContextParameterList(newParamList);
        JobContextParameter param = new JobContextParameter();
        param.setName("schema");
        newParamList.add(param);
        process = mock(Process.class);
        JobContextManager contextManger = new JobContextManager();
        contextManger.setDefaultContext(newContext);
        when(process.getContextManager()).thenReturn(contextManger);
        dbMapComponent.setProcess(process);

        ExternalDbMapData externalData2 = new ExternalDbMapData();
        DbMapComponent dbMapComponent2 = new DbMapComponent();
        dbMapComponent2.setExternalData(externalData2);
        mainTableEntities = new String[] { "id", "column1", "column2" };
        
        List<IConnection> outgoingConnections = new ArrayList<IConnection>();
        Node map1 = mockNode(dbMapComponent);
        outgoingConnections.add(mockConnection(map1, schema, table2, mainTableEntities));
        dbMapComponent.setOutgoingConnections(outgoingConnections);
        dbMapComponent2.setIncomingConnections(outgoingConnections);

        inputs = new ArrayList<ExternalDbMapTable>();
        outputs = new ArrayList<ExternalDbMapTable>();
        // main table
        inputTable = new ExternalDbMapTable();
        mainTableName = "".equals(schema) ? table2 : schema + "." + table2;
        // quote will be removed in the ui for connections ,so we do the same for test
        mainTableNameNoQuote = TalendTextUtils.removeQuotes(mainTableName);
        inputTable.setTableName(mainTableNameNoQuote);
        inputTable.setName("table2");
        entities = getMetadataEntities(mainTableEntities, new String[3]);
        inputTable.setMetadataTableEntries(entities);
        inputs.add(inputTable);

        // output
        outputTable = new ExternalDbMapTable();
        mainTableName = "".equals(schema) ? table3 : schema + "." + table3;
        // quote will be removed in the ui for connections ,so we do the same for test
        mainTableNameNoQuote = TalendTextUtils.removeQuotes(mainTableName);
        outputTable.setTableName(mainTableNameNoQuote);
        outputTable.setName("table3");
        names = new String[] { "id", "column1", "column2" };
        mainTable = mainTableName;
        expressions = new String[] { "table2.id", "table2.column1", "table2.column2" };
        outputTable.setMetadataTableEntries(getMetadataEntities(names, expressions));
        outputs.add(outputTable);

        externalData2.setInputTables(inputs);
        externalData2.setOutputTables(outputs);
        dbMapComponent2.setExternalData(externalData2);
        metadataList = new ArrayList<IMetadataTable>();
        metadataTable = getMetadataTable(names);
        metadataTable.setLabel("table3");
        metadataList.add(metadataTable);
        dbMapComponent2.setMetadataList(metadataList);
        if (dbMapComponent2.getElementParameters() == null) {
            dbMapComponent2.setElementParameters(Collections.EMPTY_LIST);
        }
        JobContextParameter param1 = new JobContextParameter();
        param1.setName("param1");
        newParamList.add(param1);

        JobContextParameter param2 = new JobContextParameter();
        param2.setName("param2");
        newParamList.add(param2);
        dbMapComponent2.setProcess(process);
        outgoingConnections = new ArrayList<IConnection>();
        outgoingConnections.add(mockConnection(schema, table3, mainTableEntities));
        dbMapComponent2.setOutgoingConnections(outgoingConnections);

        ElementParameter comName = new ElementParameter(dbMapComponent);
        comName.setName("COMPONENT_NAME");
        comName.setValue("tELTMap");
        List<ElementParameter> list = new ArrayList<>();
        list.add(comName);
        dbMapComponent.setElementParameters(list);

        dbManager = new GenericDbGenerationManager();
        // should be:
        // "SELECT
        // table2.id, table2.column1, table2.column2
        // FROM
        // (
        // SELECT
        // table1.id AS id, CASE WHEN table1.column1 IS NULL THEN " +context.param1+ " ELSE table1.column1 END AS
        // column1, table1.column2 AS column2
        // FROM
        // table1
        // WHERE t.column2 = '" +context.param2+ "'
        // ) table2"

        String query = dbManager.buildSqlSelect(dbMapComponent2, "table3").replaceAll("\n", "");
        String exceptQuery = "\"SELECT\n" + "table2.id, table2.column1, table2.column2\n" + "FROM\n" + " (\n" + "  SELECT\n"
                + "  table1.id AS id, CASE WHEN table1.column1 IS NULL THEN \" +context.param1+ \" ELSE table1.column1 END AS column1, table1.column2 AS column2\n"
                + "  FROM\n" + "   table1\n" + "  WHERE t.column2 = '\" +context.param2+ \"'\n" + " ) table2\"";
        assertEquals(exceptQuery.replaceAll("\n", "").trim(), query.trim());
    }
}
