package org.talend.designer.dbmap.language.mssql;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.talend.core.model.components.ComponentCategory;
import org.talend.core.model.components.IComponent;
import org.talend.core.model.context.JobContextManager;
import org.talend.core.model.context.JobContextParameter;
import org.talend.core.model.metadata.IMetadataColumn;
import org.talend.core.model.metadata.IMetadataTable;
import org.talend.core.model.metadata.MetadataColumn;
import org.talend.core.model.metadata.MetadataTable;
import org.talend.core.model.process.EConnectionType;
import org.talend.core.model.process.IConnection;
import org.talend.core.model.process.IContextParameter;
import org.talend.core.model.process.IElementParameter;
import org.talend.core.ui.component.ComponentsFactoryProvider;
import org.talend.designer.core.model.components.ElementParameter;
import org.talend.designer.core.model.process.DataConnection;
import org.talend.designer.core.model.process.DataNode;
import org.talend.designer.core.ui.editor.connections.Connection;
import org.talend.designer.core.ui.editor.nodes.Node;
import org.talend.designer.core.ui.editor.process.Process;
import org.talend.designer.dbmap.DbMapComponent;
import org.talend.designer.dbmap.external.data.ExternalDbMapData;
import org.talend.designer.dbmap.external.data.ExternalDbMapEntry;
import org.talend.designer.dbmap.external.data.ExternalDbMapTable;
import org.talend.designer.dbmap.language.AbstractDbLanguage.JOIN;
import org.talend.designer.dbmap.language.generation.DbGenerationManager;
import org.talend.designer.dbmap.language.generation.DbGenerationManagerTestHelper;
import org.talend.designer.dbmap.language.generation.GenericDbGenerationManager;

public class MssqlGenerationManagerTest extends DbGenerationManagerTestHelper {

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

    /**
     * inner join with Explicit checked DOC hzhao Comment method "testELTMapJoinWithUpdate".
     */
     @Test
    public void testELTMapJoinWithUpdate() {
        dbManager = new MssqlGenerationManager();
        String schema = "dbo";
        String inputTable1 = "src1";
        String inputTable2 = "src2";
        String outTable1 = "tar";

        dbMapComponent = new DbMapComponent();

        List<IMetadataTable> metadataList = new ArrayList<IMetadataTable>();

        MetadataTable metadataTable = getMetadataTable(new String[] { "newColumn", "newColumn1" }, new String[] { "id", "name" });
        metadataTable.setLabel(schema + "." + outTable1);
        metadataList.add(metadataTable);

        dbMapComponent.setMetadataList(metadataList);

        // main table
        ExternalDbMapData externalData = new ExternalDbMapData();
        List<ExternalDbMapTable> inputs = new ArrayList<ExternalDbMapTable>();
        List<ExternalDbMapTable> outputs = new ArrayList<ExternalDbMapTable>();
        // main table
        ExternalDbMapTable inputTable = new ExternalDbMapTable();
        inputTable.setTableName(schema + "." + inputTable1);
        inputTable.setName(schema + "." + inputTable1);
        inputTable.setAlias("A");
        List<ExternalDbMapEntry> entities = getMetadataEntities(new String[] { "newColumn", "newColumn1" }, new String[2]);
        inputTable.setMetadataTableEntries(entities);
        inputs.add(inputTable);

        // lookup table
        inputTable = new ExternalDbMapTable();
        inputTable.setTableName(schema + "." + inputTable2);
        inputTable.setName(schema + "." + inputTable2);
        inputTable.setAlias("B");
        entities = getMetadataEntities(new String[] { "newColumn", "newColumn1" }, new String[2]);
        ExternalDbMapEntry newColumn = entities.get(0);
        newColumn.setExpression("A.newColumn");
        newColumn.setOperator("=");
        inputTable.setJoinType("INNER_JOIN");
        newColumn.setJoin(true);
        inputTable.setMetadataTableEntries(entities);
        inputs.add(inputTable);

        // output
        ExternalDbMapTable outputTable = new ExternalDbMapTable();
        outputTable.setName(schema + "." + outTable1);
        outputTable.setTableName(schema + "." + outTable1);
        String[] names = new String[] { "tarColumn", "tarColumn1" };
        String[] expressions = new String[] { "A.newColumn", "A.newColumn1" };
        outputTable.setMetadataTableEntries(getMetadataEntities(names, expressions));
        outputs.add(outputTable);

        externalData.setInputTables(inputs);
        externalData.setOutputTables(outputs);
        dbMapComponent.setExternalData(externalData);

        List<IConnection> incomingConnections = new ArrayList<IConnection>();
        incomingConnections.add(
                mockConnection(schema, inputTable1, new String[] { "newColumn", "newColumn1" }, new String[] { "id", "name" }));
        incomingConnections.add(
                mockConnection(schema, inputTable2, new String[] { "newColumn", "newColumn1" }, new String[] { "id", "name" }));
        dbMapComponent.setIncomingConnections(incomingConnections);

        List<IConnection> outputConnections = new ArrayList<IConnection>();
        Node map1 = mockNode(dbMapComponent);
        IConnection connection = mockConnection(map1, schema, inputTable1, new String[] { "id", "name" });
        targetComponent = ComponentsFactoryProvider.getInstance().get("tELTMSSqlOutput",
                ComponentCategory.CATEGORY_4_DI.getName());
        connection.getMetadataTable().getColumn("id").setLabel("newColumn");
        connection.getMetadataTable().getColumn("name").setLabel("newColumn1");
        // add target
        DataNode output = new DataNode();
        List<IElementParameter> paraList = new ArrayList<IElementParameter>();
        ElementParameter param = new ElementParameter(output);
        param.setName("USE_UPDATE_STATEMENT"); //$NON-NLS-1$
        param.setValue("true"); //$NON-NLS-1$
        paraList.add(param);
        output.setElementParameters(paraList);
        output.setComponent(targetComponent);

        DataConnection dataConnection = new DataConnection();
        dataConnection.setName(schema + "." + outTable1);
        dataConnection.setActivate(true);
        dataConnection.setLineStyle(EConnectionType.FLOW_MAIN);
        dataConnection.setTarget(output);
        IMetadataTable table = new MetadataTable();
        table.setLabel(outTable1);
        table.setTableName(outTable1);
        List<IMetadataColumn> listColumns = new ArrayList<IMetadataColumn>();
        for (String columnName : new String[] { "id", "name" }) {
            IMetadataColumn column = new MetadataColumn();
            column.setLabel(columnName);
            column.setOriginalDbColumnName(columnName);
            listColumns.add(column);
        }
        table.setListColumns(listColumns);
        dataConnection.setMetadataTable(table);
        // List<DataConnection> dataConnections = new ArrayList<>();
        outputConnections.add(dataConnection);
        outputConnections.add(connection);
        dbMapComponent.setOutgoingConnections(outputConnections);

        Process process = mock(Process.class);
        when(process.getContextManager()).thenReturn(new JobContextManager());
        dbMapComponent.setProcess(process);

        IContextParameter lookupTableContext = new JobContextParameter();
        lookupTableContext.setName("lookup");
        lookupTableContext.setValue("lookupTable");
        lookupTableContext.setType("String");
        String query = dbManager.buildSqlSelect(dbMapComponent, schema + "." + outTable1);
        String expectedQuery = "\"UPDATE dbo.tar\n" + "SET tarColumn = A.id,\n" + "tarColumn1 = A.name\n"
                + "FROM\n \" +dbo+\".\"+src1+ \" A INNER JOIN  \" +dbo+\".\"+src2+ \" B ON(  B.id = A.id )\"";
        assertEquals(expectedQuery, query);
    }

    /**
     * out join with Explicit checked DOC hzhao Comment method "testELTMapJoinWithUpdate".
     */
    @Test
    public void testELTMapOutJoinWithUpdate() {
        dbManager = new MssqlGenerationManager();
        String schema = "dbo";
        String inputTable1 = "src1";
        String inputTable2 = "src2";
        String outTable1 = "tar";

        dbMapComponent = new DbMapComponent();

        List<IMetadataTable> metadataList = new ArrayList<IMetadataTable>();

        MetadataTable metadataTable = getMetadataTable(new String[] { "newColumn", "newColumn1" }, new String[] { "id", "name" });
        metadataTable.setLabel(schema + "." + outTable1);
        metadataList.add(metadataTable);

        dbMapComponent.setMetadataList(metadataList);

        // main table
        ExternalDbMapData externalData = new ExternalDbMapData();
        List<ExternalDbMapTable> inputs = new ArrayList<ExternalDbMapTable>();
        List<ExternalDbMapTable> outputs = new ArrayList<ExternalDbMapTable>();
        // main table
        ExternalDbMapTable inputTable = new ExternalDbMapTable();
        inputTable.setTableName(schema + "." + inputTable1);
        inputTable.setName(schema + "." + inputTable1);
        inputTable.setAlias("A");
        List<ExternalDbMapEntry> entities = getMetadataEntities(new String[] { "newColumn", "newColumn1" }, new String[2]);
        inputTable.setMetadataTableEntries(entities);
        inputs.add(inputTable);

        // lookup table
        inputTable = new ExternalDbMapTable();
        inputTable.setTableName(schema + "." + inputTable2);
        inputTable.setName(schema + "." + inputTable2);
        inputTable.setAlias("B");
        entities = getMetadataEntities(new String[] { "newColumn", "newColumn1" }, new String[2]);
        ExternalDbMapEntry newColumn = entities.get(0);
        newColumn.setExpression("A.newColumn");
        newColumn.setOperator("=");
        inputTable.setJoinType(JOIN.LEFT_OUTER_JOIN.name());
        newColumn.setJoin(true);
        inputTable.setMetadataTableEntries(entities);
        inputs.add(inputTable);

        // output
        ExternalDbMapTable outputTable = new ExternalDbMapTable();
        outputTable.setName(schema + "." + outTable1);
        outputTable.setTableName(schema + "." + outTable1);
        String[] names = new String[] { "tarColumn", "tarColumn1" };
        String[] expressions = new String[] { "A.newColumn", "A.newColumn1" };
        outputTable.setMetadataTableEntries(getMetadataEntities(names, expressions));
        outputs.add(outputTable);

        externalData.setInputTables(inputs);
        externalData.setOutputTables(outputs);
        dbMapComponent.setExternalData(externalData);

        List<IConnection> incomingConnections = new ArrayList<IConnection>();
        incomingConnections.add(
                mockConnection(schema, inputTable1, new String[] { "newColumn", "newColumn1" }, new String[] { "id", "name" }));
        incomingConnections.add(
                mockConnection(schema, inputTable2, new String[] { "newColumn", "newColumn1" }, new String[] { "id", "name" }));
        dbMapComponent.setIncomingConnections(incomingConnections);

        List<IConnection> outputConnections = new ArrayList<IConnection>();
        Node map1 = mockNode(dbMapComponent);
        IConnection connection = mockConnection(map1, schema, inputTable1, new String[] { "id", "name" });
        targetComponent = ComponentsFactoryProvider.getInstance().get("tELTMSSqlOutput",
                ComponentCategory.CATEGORY_4_DI.getName());
        connection.getMetadataTable().getColumn("id").setLabel("newColumn");
        connection.getMetadataTable().getColumn("name").setLabel("newColumn1");
        // add target
        DataNode output = new DataNode();
        List<IElementParameter> paraList = new ArrayList<IElementParameter>();
        ElementParameter param = new ElementParameter(output);
        param.setName("USE_UPDATE_STATEMENT"); //$NON-NLS-1$
        param.setValue("true"); //$NON-NLS-1$
        paraList.add(param);
        output.setElementParameters(paraList);
        output.setComponent(targetComponent);

        DataConnection dataConnection = new DataConnection();
        dataConnection.setName(schema + "." + outTable1);
        dataConnection.setActivate(true);
        dataConnection.setLineStyle(EConnectionType.FLOW_MAIN);
        dataConnection.setTarget(output);
        IMetadataTable table = new MetadataTable();
        table.setLabel(outTable1);
        table.setTableName(outTable1);
        List<IMetadataColumn> listColumns = new ArrayList<IMetadataColumn>();
        for (String columnName : new String[] { "id", "name" }) {
            IMetadataColumn column = new MetadataColumn();
            column.setLabel(columnName);
            column.setOriginalDbColumnName(columnName);
            listColumns.add(column);
        }
        table.setListColumns(listColumns);
        dataConnection.setMetadataTable(table);
        // List<DataConnection> dataConnections = new ArrayList<>();
        outputConnections.add(dataConnection);
        outputConnections.add(connection);
        dbMapComponent.setOutgoingConnections(outputConnections);

        Process process = mock(Process.class);
        when(process.getContextManager()).thenReturn(new JobContextManager());
        dbMapComponent.setProcess(process);

        IContextParameter lookupTableContext = new JobContextParameter();
        lookupTableContext.setName("lookup");
        lookupTableContext.setValue("lookupTable");
        lookupTableContext.setType("String");
        String query = dbManager.buildSqlSelect(dbMapComponent, schema + "." + outTable1);
        String expectedQuery = "\"UPDATE dbo.tar\n" + "SET tarColumn = A.id,\n" + "tarColumn1 = A.name\n"
                + "FROM\n \" +dbo+\".\"+src1+ \" A LEFT OUTER JOIN  \" +dbo+\".\"+src2+ \" B ON(  B.id = A.id )\"";
        assertEquals(expectedQuery, query);
    }

    /**
     * 3tables join A inner join B out left join c DOC hzhao Comment method "testELTMapJoinWithUpdate".
     */
    @Test
    public void testELTMap3TablesJoin() {
        dbManager = new MssqlGenerationManager();
        String schema = "dbo";
        String inputTable1 = "src1";
        String inputTable2 = "src2";
        String inputTable3 = "src3";
        String outTable1 = "tar";

        dbMapComponent = new DbMapComponent();

        List<IMetadataTable> metadataList = new ArrayList<IMetadataTable>();

        MetadataTable metadataTable = getMetadataTable(new String[] { "newColumn", "newColumn1" }, new String[] { "id", "name" });
        metadataTable.setLabel(schema + "." + outTable1);
        metadataList.add(metadataTable);

        dbMapComponent.setMetadataList(metadataList);

        // main table
        ExternalDbMapData externalData = new ExternalDbMapData();
        List<ExternalDbMapTable> inputs = new ArrayList<ExternalDbMapTable>();
        List<ExternalDbMapTable> outputs = new ArrayList<ExternalDbMapTable>();
        // main table
        ExternalDbMapTable inputTable = new ExternalDbMapTable();
        inputTable.setTableName(schema + "." + inputTable1);
        inputTable.setName(schema + "." + inputTable1);
        inputTable.setAlias("A");
        List<ExternalDbMapEntry> entities = getMetadataEntities(new String[] { "newColumn", "newColumn1" }, new String[2]);
        inputTable.setMetadataTableEntries(entities);
        inputs.add(inputTable);

        // lookup table
        inputTable = new ExternalDbMapTable();
        inputTable.setTableName(schema + "." + inputTable2);
        inputTable.setName(schema + "." + inputTable2);
        inputTable.setAlias("B");
        entities = getMetadataEntities(new String[] { "newColumn", "newColumn1" }, new String[2]);
        ExternalDbMapEntry newColumn = entities.get(0);
        newColumn.setExpression("A.newColumn");
        newColumn.setOperator("=");
        inputTable.setJoinType(JOIN.INNER_JOIN.name());
        newColumn.setJoin(true);
        inputTable.setMetadataTableEntries(entities);
        inputs.add(inputTable);
        // lookup talbe no.2
        inputTable = new ExternalDbMapTable();
        inputTable.setTableName(schema + "." + inputTable3);
        inputTable.setName(schema + "." + inputTable3);
        inputTable.setAlias("C");
        entities = getMetadataEntities(new String[] { "id", "name" }, new String[2]);
        ExternalDbMapEntry newColumn2 = entities.get(0);
        newColumn2.setExpression("B.id");
        newColumn2.setOperator("=");
        inputTable.setJoinType(JOIN.LEFT_OUTER_JOIN.name());
        newColumn2.setJoin(true);
        inputTable.setMetadataTableEntries(entities);
        inputs.add(inputTable);
        // output
        ExternalDbMapTable outputTable = new ExternalDbMapTable();
        outputTable.setName(schema + "." + outTable1);
        outputTable.setTableName(schema + "." + outTable1);
        String[] names = new String[] { "tarColumn", "tarColumn1" };
        String[] expressions = new String[] { "A.newColumn", "A.newColumn1" };
        outputTable.setMetadataTableEntries(getMetadataEntities(names, expressions));
        outputs.add(outputTable);

        externalData.setInputTables(inputs);
        externalData.setOutputTables(outputs);
        dbMapComponent.setExternalData(externalData);

        List<IConnection> incomingConnections = new ArrayList<IConnection>();
        incomingConnections.add(
                mockConnection(schema, inputTable1, new String[] { "newColumn", "newColumn1" }, new String[] { "id", "name" }));
        incomingConnections.add(
                mockConnection(schema, inputTable2, new String[] { "newColumn", "newColumn1" }, new String[] { "id", "name" }));
        incomingConnections.add(
                mockConnection(schema, inputTable3, new String[] { "newColumn", "newColumn1" }, new String[] { "id", "name" }));
        dbMapComponent.setIncomingConnections(incomingConnections);

        List<IConnection> outputConnections = new ArrayList<IConnection>();
        Node map1 = mockNode(dbMapComponent);
        IConnection connection = mockConnection(map1, schema, inputTable1, new String[] { "id", "name" });
        targetComponent = ComponentsFactoryProvider.getInstance().get("tELTMSSqlOutput",
                ComponentCategory.CATEGORY_4_DI.getName());
        connection.getMetadataTable().getColumn("id").setLabel("newColumn");
        connection.getMetadataTable().getColumn("name").setLabel("newColumn1");
        // add target
        DataNode output = new DataNode();
        List<IElementParameter> paraList = new ArrayList<IElementParameter>();
        ElementParameter param = new ElementParameter(output);
        param.setName("USE_UPDATE_STATEMENT"); //$NON-NLS-1$
        param.setValue("true"); //$NON-NLS-1$
        paraList.add(param);
        output.setElementParameters(paraList);
        output.setComponent(targetComponent);

        DataConnection dataConnection = new DataConnection();
        dataConnection.setName(schema + "." + outTable1);
        dataConnection.setActivate(true);
        dataConnection.setLineStyle(EConnectionType.FLOW_MAIN);
        dataConnection.setTarget(output);
        IMetadataTable table = new MetadataTable();
        table.setLabel(outTable1);
        table.setTableName(outTable1);
        List<IMetadataColumn> listColumns = new ArrayList<IMetadataColumn>();
        for (String columnName : new String[] { "id", "name" }) {
            IMetadataColumn column = new MetadataColumn();
            column.setLabel(columnName);
            column.setOriginalDbColumnName(columnName);
            listColumns.add(column);
        }
        table.setListColumns(listColumns);
        dataConnection.setMetadataTable(table);
        // List<DataConnection> dataConnections = new ArrayList<>();
        outputConnections.add(dataConnection);
        outputConnections.add(connection);
        dbMapComponent.setOutgoingConnections(outputConnections);

        Process process = mock(Process.class);
        when(process.getContextManager()).thenReturn(new JobContextManager());
        dbMapComponent.setProcess(process);

        IContextParameter lookupTableContext = new JobContextParameter();
        lookupTableContext.setName("lookup");
        lookupTableContext.setValue("lookupTable");
        lookupTableContext.setType("String");
        String query = dbManager.buildSqlSelect(dbMapComponent, schema + "." + outTable1);
        String expectedQuery = "\"UPDATE dbo.tar\n" + "SET tarColumn = A.id,\n" + "tarColumn1 = A.name\n"
                + "FROM\n \" +dbo+\".\"+src1+ \" A INNER JOIN  \" +dbo+\".\"+src2+ \" B ON(  B.id = A.id )"
                + " LEFT OUTER JOIN  \" +dbo+\".\"+src3+ \" C ON(  C.id = B.id )\"";
        assertEquals(expectedQuery, query);
    }

    /**
     * 3tables join A inner join B out left join c But b&c not check explicit join DOC hzhao Comment method
     * "testELTMapJoinWithUpdate".
     */
    @Test
    public void testELTMapUnckeckExplicit() {
        dbManager = new MssqlGenerationManager();
        String schema = "dbo";
        String inputTable1 = "src1";
        String inputTable2 = "src2";
        String inputTable3 = "src3";
        String outTable1 = "tar";

        dbMapComponent = new DbMapComponent();

        List<IMetadataTable> metadataList = new ArrayList<IMetadataTable>();

        MetadataTable metadataTable = getMetadataTable(new String[] { "newColumn", "newColumn1" }, new String[] { "id", "name" });
        metadataTable.setLabel(schema + "." + outTable1);
        metadataList.add(metadataTable);

        dbMapComponent.setMetadataList(metadataList);

        // main table
        ExternalDbMapData externalData = new ExternalDbMapData();
        List<ExternalDbMapTable> inputs = new ArrayList<ExternalDbMapTable>();
        List<ExternalDbMapTable> outputs = new ArrayList<ExternalDbMapTable>();
        // main table
        ExternalDbMapTable inputTable = new ExternalDbMapTable();
        inputTable.setTableName(schema + "." + inputTable1);
        inputTable.setName(schema + "." + inputTable1);
        inputTable.setAlias("A");
        List<ExternalDbMapEntry> entities = getMetadataEntities(new String[] { "newColumn", "newColumn1" }, new String[2]);
        inputTable.setMetadataTableEntries(entities);
        inputs.add(inputTable);

        // lookup table
        inputTable = new ExternalDbMapTable();
        inputTable.setTableName(schema + "." + inputTable2);
        inputTable.setName(schema + "." + inputTable2);
        inputTable.setAlias("B");
        entities = getMetadataEntities(new String[] { "newColumn", "newColumn1" }, new String[2]);
        ExternalDbMapEntry newColumn = entities.get(0);
        newColumn.setExpression("A.newColumn");
        newColumn.setOperator("=");
        inputTable.setJoinType(JOIN.INNER_JOIN.name());
        newColumn.setJoin(true);
        inputTable.setMetadataTableEntries(entities);
        inputs.add(inputTable);
        // lookup talbe no.2
        inputTable = new ExternalDbMapTable();
        inputTable.setTableName(schema + "." + inputTable3);
        inputTable.setName(schema + "." + inputTable3);
        inputTable.setAlias("C");
        entities = getMetadataEntities(new String[] { "id", "name" }, new String[2]);
        ExternalDbMapEntry newColumn2 = entities.get(0);
        newColumn2.setExpression("B.id");
        newColumn2.setOperator("=");
        inputTable.setJoinType(JOIN.LEFT_OUTER_JOIN.name());
        newColumn2.setJoin(false);
        inputTable.setMetadataTableEntries(entities);
        inputs.add(inputTable);
        // output
        ExternalDbMapTable outputTable = new ExternalDbMapTable();
        outputTable.setName(schema + "." + outTable1);
        outputTable.setTableName(schema + "." + outTable1);
        String[] names = new String[] { "tarColumn", "tarColumn1" };
        String[] expressions = new String[] { "A.newColumn", "A.newColumn1" };
        outputTable.setMetadataTableEntries(getMetadataEntities(names, expressions));
        outputs.add(outputTable);

        externalData.setInputTables(inputs);
        externalData.setOutputTables(outputs);
        dbMapComponent.setExternalData(externalData);

        List<IConnection> incomingConnections = new ArrayList<IConnection>();
        incomingConnections.add(
                mockConnection(schema, inputTable1, new String[] { "newColumn", "newColumn1" }, new String[] { "id", "name" }));
        incomingConnections.add(
                mockConnection(schema, inputTable2, new String[] { "newColumn", "newColumn1" }, new String[] { "id", "name" }));
        incomingConnections.add(
                mockConnection(schema, inputTable3, new String[] { "newColumn", "newColumn1" }, new String[] { "id", "name" }));
        dbMapComponent.setIncomingConnections(incomingConnections);

        List<IConnection> outputConnections = new ArrayList<IConnection>();
        Node map1 = mockNode(dbMapComponent);
        IConnection connection = mockConnection(map1, schema, inputTable1, new String[] { "id", "name" });
        targetComponent = ComponentsFactoryProvider.getInstance().get("tELTMSSqlOutput",
                ComponentCategory.CATEGORY_4_DI.getName());
        connection.getMetadataTable().getColumn("id").setLabel("newColumn");
        connection.getMetadataTable().getColumn("name").setLabel("newColumn1");
        // add target
        DataNode output = new DataNode();
        List<IElementParameter> paraList = new ArrayList<IElementParameter>();
        ElementParameter param = new ElementParameter(output);
        param.setName("USE_UPDATE_STATEMENT"); //$NON-NLS-1$
        param.setValue("true"); //$NON-NLS-1$
        paraList.add(param);
        output.setElementParameters(paraList);
        output.setComponent(targetComponent);

        DataConnection dataConnection = new DataConnection();
        dataConnection.setName(schema + "." + outTable1);
        dataConnection.setActivate(true);
        dataConnection.setLineStyle(EConnectionType.FLOW_MAIN);
        dataConnection.setTarget(output);
        IMetadataTable table = new MetadataTable();
        table.setLabel(outTable1);
        table.setTableName(outTable1);
        List<IMetadataColumn> listColumns = new ArrayList<IMetadataColumn>();
        for (String columnName : new String[] { "id", "name" }) {
            IMetadataColumn column = new MetadataColumn();
            column.setLabel(columnName);
            column.setOriginalDbColumnName(columnName);
            listColumns.add(column);
        }
        table.setListColumns(listColumns);
        dataConnection.setMetadataTable(table);
        // List<DataConnection> dataConnections = new ArrayList<>();
        outputConnections.add(dataConnection);
        outputConnections.add(connection);
        dbMapComponent.setOutgoingConnections(outputConnections);

        Process process = mock(Process.class);
        when(process.getContextManager()).thenReturn(new JobContextManager());
        dbMapComponent.setProcess(process);

        IContextParameter lookupTableContext = new JobContextParameter();
        lookupTableContext.setName("lookup");
        lookupTableContext.setValue("lookupTable");
        lookupTableContext.setType("String");
        String query = dbManager.buildSqlSelect(dbMapComponent, schema + "." + outTable1);
        String expectedQuery = "\"UPDATE dbo.tar\n" + "SET tarColumn = A.id,\n" + "tarColumn1 = A.name\n"
                + "FROM\n \" +dbo+\".\"+src1+ \" A INNER JOIN  \" +dbo+\".\"+src2+ \" B ON(  B.id = A.id )"
                + "  , \" +dbo+\".\"+src3+ \" C" + "\nWHERE" + "\n  C.id = B.id\"";
        assertEquals(expectedQuery, query);
    }

    /**
     * 3tables join A implicit join B out left join c DOC hzhao Comment method "testELTMapJoinWithUpdate".
     */
    @Test
    public void testELTMapImplicitAndLeftJoin() {
        dbManager = new MssqlGenerationManager();
        String schema = "dbo";
        String inputTable1 = "src1";
        String inputTable2 = "src2";
        String inputTable3 = "src3";
        String outTable1 = "tar";

        dbMapComponent = new DbMapComponent();

        List<IMetadataTable> metadataList = new ArrayList<IMetadataTable>();

        MetadataTable metadataTable = getMetadataTable(new String[] { "newColumn", "newColumn1" }, new String[] { "id", "name" });
        metadataTable.setLabel(schema + "." + outTable1);
        metadataList.add(metadataTable);

        dbMapComponent.setMetadataList(metadataList);

        // main table
        ExternalDbMapData externalData = new ExternalDbMapData();
        List<ExternalDbMapTable> inputs = new ArrayList<ExternalDbMapTable>();
        List<ExternalDbMapTable> outputs = new ArrayList<ExternalDbMapTable>();
        // main table
        ExternalDbMapTable inputTable = new ExternalDbMapTable();
        inputTable.setTableName(schema + "." + inputTable1);
        inputTable.setName(schema + "." + inputTable1);
        inputTable.setAlias("A");
        List<ExternalDbMapEntry> entities = getMetadataEntities(new String[] { "newColumn", "newColumn1" }, new String[2]);
        inputTable.setMetadataTableEntries(entities);
        inputs.add(inputTable);

        // lookup table
        inputTable = new ExternalDbMapTable();
        inputTable.setTableName(schema + "." + inputTable2);
        inputTable.setName(schema + "." + inputTable2);
        inputTable.setAlias("B");
        entities = getMetadataEntities(new String[] { "newColumn", "newColumn1" }, new String[2]);
        ExternalDbMapEntry newColumn = entities.get(0);
        newColumn.setExpression("A.newColumn");
        newColumn.setOperator("=");
        inputTable.setJoinType(JOIN.NO_JOIN.name());
        newColumn.setJoin(false);
        inputTable.setMetadataTableEntries(entities);
        inputs.add(inputTable);
        // lookup talbe no.2
        inputTable = new ExternalDbMapTable();
        inputTable.setTableName(schema + "." + inputTable3);
        inputTable.setName(schema + "." + inputTable3);
        inputTable.setAlias("C");
        entities = getMetadataEntities(new String[] { "id", "name" }, new String[2]);
        ExternalDbMapEntry newColumn2 = entities.get(0);
        newColumn2.setExpression("B.id");
        newColumn2.setOperator("=");
        inputTable.setJoinType(JOIN.LEFT_OUTER_JOIN.name());
        newColumn2.setJoin(true);
        inputTable.setMetadataTableEntries(entities);
        inputs.add(inputTable);
        // output
        ExternalDbMapTable outputTable = new ExternalDbMapTable();
        outputTable.setName(schema + "." + outTable1);
        outputTable.setTableName(schema + "." + outTable1);
        String[] names = new String[] { "tarColumn", "tarColumn1" };
        String[] expressions = new String[] { "A.newColumn", "A.newColumn1" };
        outputTable.setMetadataTableEntries(getMetadataEntities(names, expressions));
        outputs.add(outputTable);

        externalData.setInputTables(inputs);
        externalData.setOutputTables(outputs);
        dbMapComponent.setExternalData(externalData);

        List<IConnection> incomingConnections = new ArrayList<IConnection>();
        incomingConnections.add(
                mockConnection(schema, inputTable1, new String[] { "newColumn", "newColumn1" }, new String[] { "id", "name" }));
        incomingConnections.add(
                mockConnection(schema, inputTable2, new String[] { "newColumn", "newColumn1" }, new String[] { "id", "name" }));
        incomingConnections.add(
                mockConnection(schema, inputTable3, new String[] { "newColumn", "newColumn1" }, new String[] { "id", "name" }));
        dbMapComponent.setIncomingConnections(incomingConnections);

        List<IConnection> outputConnections = new ArrayList<IConnection>();
        Node map1 = mockNode(dbMapComponent);
        IConnection connection = mockConnection(map1, schema, inputTable1, new String[] { "id", "name" });
        targetComponent = ComponentsFactoryProvider.getInstance().get("tELTMSSqlOutput",
                ComponentCategory.CATEGORY_4_DI.getName());
        connection.getMetadataTable().getColumn("id").setLabel("newColumn");
        connection.getMetadataTable().getColumn("name").setLabel("newColumn1");
        // add target
        DataNode output = new DataNode();
        List<IElementParameter> paraList = new ArrayList<IElementParameter>();
        ElementParameter param = new ElementParameter(output);
        param.setName("USE_UPDATE_STATEMENT"); //$NON-NLS-1$
        param.setValue("true"); //$NON-NLS-1$
        paraList.add(param);
        output.setElementParameters(paraList);
        output.setComponent(targetComponent);

        DataConnection dataConnection = new DataConnection();
        dataConnection.setName(schema + "." + outTable1);
        dataConnection.setActivate(true);
        dataConnection.setLineStyle(EConnectionType.FLOW_MAIN);
        dataConnection.setTarget(output);
        IMetadataTable table = new MetadataTable();
        table.setLabel(outTable1);
        table.setTableName(outTable1);
        List<IMetadataColumn> listColumns = new ArrayList<IMetadataColumn>();
        for (String columnName : new String[] { "id", "name" }) {
            IMetadataColumn column = new MetadataColumn();
            column.setLabel(columnName);
            column.setOriginalDbColumnName(columnName);
            listColumns.add(column);
        }
        table.setListColumns(listColumns);
        dataConnection.setMetadataTable(table);
        // List<DataConnection> dataConnections = new ArrayList<>();
        outputConnections.add(dataConnection);
        outputConnections.add(connection);
        dbMapComponent.setOutgoingConnections(outputConnections);

        Process process = mock(Process.class);
        when(process.getContextManager()).thenReturn(new JobContextManager());
        dbMapComponent.setProcess(process);

        IContextParameter lookupTableContext = new JobContextParameter();
        lookupTableContext.setName("lookup");
        lookupTableContext.setValue("lookupTable");
        lookupTableContext.setType("String");
        String query = dbManager.buildSqlSelect(dbMapComponent, schema + "." + outTable1);
        String expectedQuery = "\"UPDATE dbo.tar\n" + "SET tarColumn = A.id,\n" + "tarColumn1 = A.name\n"
                + "FROM\n \" +dbo+\".\"+src1+ \" A , \" +dbo+\".\"+src2+ \" B LEFT OUTER JOIN  \" +dbo+\".\"+src3+ \" C ON(  C.id = B.id )"
                + "\nWHERE" + "\n  B.id = A.id\"";
        assertEquals(expectedQuery, query);
    }

    /**
     * 3tables join A inner join B inner join c buc b&c explicit join uncheck DOC hzhao Comment method
     * "testELTMapJoinWithUpdate".
     */
    @Test
    public void testELTMapInnerJoinExplicitUncheck() {
        dbManager = new MssqlGenerationManager();
        String schema = "dbo";
        String inputTable1 = "src1";
        String inputTable2 = "src2";
        String inputTable3 = "src3";
        String outTable1 = "tar";

        dbMapComponent = new DbMapComponent();

        List<IMetadataTable> metadataList = new ArrayList<IMetadataTable>();

        MetadataTable metadataTable = getMetadataTable(new String[] { "newColumn", "newColumn1" }, new String[] { "id", "name" });
        metadataTable.setLabel(schema + "." + outTable1);
        metadataList.add(metadataTable);

        dbMapComponent.setMetadataList(metadataList);

        // main table
        ExternalDbMapData externalData = new ExternalDbMapData();
        List<ExternalDbMapTable> inputs = new ArrayList<ExternalDbMapTable>();
        List<ExternalDbMapTable> outputs = new ArrayList<ExternalDbMapTable>();
        // main table
        ExternalDbMapTable inputTable = new ExternalDbMapTable();
        inputTable.setTableName(schema + "." + inputTable1);
        inputTable.setName(schema + "." + inputTable1);
        inputTable.setAlias("A");
        List<ExternalDbMapEntry> entities = getMetadataEntities(new String[] { "newColumn", "newColumn1" }, new String[2]);
        inputTable.setMetadataTableEntries(entities);
        inputs.add(inputTable);

        // lookup table
        inputTable = new ExternalDbMapTable();
        inputTable.setTableName(schema + "." + inputTable2);
        inputTable.setName(schema + "." + inputTable2);
        inputTable.setAlias("B");
        entities = getMetadataEntities(new String[] { "newColumn", "newColumn1" }, new String[2]);
        ExternalDbMapEntry newColumn = entities.get(0);
        newColumn.setExpression("A.newColumn");
        newColumn.setOperator("=");
        inputTable.setJoinType(JOIN.INNER_JOIN.name());
        newColumn.setJoin(true);
        inputTable.setMetadataTableEntries(entities);
        inputs.add(inputTable);
        // lookup talbe no.2
        inputTable = new ExternalDbMapTable();
        inputTable.setTableName(schema + "." + inputTable3);
        inputTable.setName(schema + "." + inputTable3);
        inputTable.setAlias("C");
        entities = getMetadataEntities(new String[] { "id", "name" }, new String[2]);
        ExternalDbMapEntry newColumn2 = entities.get(0);
        newColumn2.setExpression("B.id");
        newColumn2.setOperator("=");
        inputTable.setJoinType(JOIN.INNER_JOIN.name());
        newColumn2.setJoin(false);
        inputTable.setMetadataTableEntries(entities);
        inputs.add(inputTable);
        // output
        ExternalDbMapTable outputTable = new ExternalDbMapTable();
        outputTable.setName(schema + "." + outTable1);
        outputTable.setTableName(schema + "." + outTable1);
        String[] names = new String[] { "tarColumn", "tarColumn1" };
        String[] expressions = new String[] { "A.newColumn", "A.newColumn1" };
        outputTable.setMetadataTableEntries(getMetadataEntities(names, expressions));
        outputs.add(outputTable);

        externalData.setInputTables(inputs);
        externalData.setOutputTables(outputs);
        dbMapComponent.setExternalData(externalData);

        List<IConnection> incomingConnections = new ArrayList<IConnection>();
        incomingConnections.add(
                mockConnection(schema, inputTable1, new String[] { "newColumn", "newColumn1" }, new String[] { "id", "name" }));
        incomingConnections.add(
                mockConnection(schema, inputTable2, new String[] { "newColumn", "newColumn1" }, new String[] { "id", "name" }));
        incomingConnections.add(
                mockConnection(schema, inputTable3, new String[] { "newColumn", "newColumn1" }, new String[] { "id", "name" }));
        dbMapComponent.setIncomingConnections(incomingConnections);

        List<IConnection> outputConnections = new ArrayList<IConnection>();
        Node map1 = mockNode(dbMapComponent);
        IConnection connection = mockConnection(map1, schema, inputTable1, new String[] { "id", "name" });
        targetComponent = ComponentsFactoryProvider.getInstance().get("tELTMSSqlOutput",
                ComponentCategory.CATEGORY_4_DI.getName());
        connection.getMetadataTable().getColumn("id").setLabel("newColumn");
        connection.getMetadataTable().getColumn("name").setLabel("newColumn1");
        // add target
        DataNode output = new DataNode();
        List<IElementParameter> paraList = new ArrayList<IElementParameter>();
        ElementParameter param = new ElementParameter(output);
        param.setName("USE_UPDATE_STATEMENT"); //$NON-NLS-1$
        param.setValue("true"); //$NON-NLS-1$
        paraList.add(param);
        output.setElementParameters(paraList);
        output.setComponent(targetComponent);

        DataConnection dataConnection = new DataConnection();
        dataConnection.setName(schema + "." + outTable1);
        dataConnection.setActivate(true);
        dataConnection.setLineStyle(EConnectionType.FLOW_MAIN);
        dataConnection.setTarget(output);
        IMetadataTable table = new MetadataTable();
        table.setLabel(outTable1);
        table.setTableName(outTable1);
        List<IMetadataColumn> listColumns = new ArrayList<IMetadataColumn>();
        for (String columnName : new String[] { "id", "name" }) {
            IMetadataColumn column = new MetadataColumn();
            column.setLabel(columnName);
            column.setOriginalDbColumnName(columnName);
            listColumns.add(column);
        }
        table.setListColumns(listColumns);
        dataConnection.setMetadataTable(table);
        // List<DataConnection> dataConnections = new ArrayList<>();
        outputConnections.add(dataConnection);
        outputConnections.add(connection);
        dbMapComponent.setOutgoingConnections(outputConnections);

        Process process = mock(Process.class);
        when(process.getContextManager()).thenReturn(new JobContextManager());
        dbMapComponent.setProcess(process);

        IContextParameter lookupTableContext = new JobContextParameter();
        lookupTableContext.setName("lookup");
        lookupTableContext.setValue("lookupTable");
        lookupTableContext.setType("String");
        String query = dbManager.buildSqlSelect(dbMapComponent, schema + "." + outTable1);
        String expectedQuery = "\"UPDATE dbo.tar\n" + "SET tarColumn = A.id,\n" + "tarColumn1 = A.name\n"
                + "FROM\n \" +dbo+\".\"+src1+ \" A INNER JOIN  \" +dbo+\".\"+src2+ \" B ON(  B.id = A.id )  , \" +dbo+\".\"+src3+ \" C"
                + "\nWHERE" + "\n  C.id = B.id\"";
        assertEquals(expectedQuery, query);
    }

    /**
     * 3tables join A cross join B cross join c a&b explicit join checked b&c unchecked DOC hzhao Comment method
     * "testELTMapJoinWithUpdate".
     */
    @Test
    public void testELTMapCrossJoin() {
        dbManager = new MssqlGenerationManager();
        String schema = "dbo";
        String inputTable1 = "src1";
        String inputTable2 = "src2";
        String inputTable3 = "src3";
        String outTable1 = "tar";

        dbMapComponent = new DbMapComponent();

        List<IMetadataTable> metadataList = new ArrayList<IMetadataTable>();

        MetadataTable metadataTable = getMetadataTable(new String[] { "newColumn", "newColumn1" }, new String[] { "id", "name" });
        metadataTable.setLabel(schema + "." + outTable1);
        metadataList.add(metadataTable);

        dbMapComponent.setMetadataList(metadataList);

        // main table
        ExternalDbMapData externalData = new ExternalDbMapData();
        List<ExternalDbMapTable> inputs = new ArrayList<ExternalDbMapTable>();
        List<ExternalDbMapTable> outputs = new ArrayList<ExternalDbMapTable>();
        // main table
        ExternalDbMapTable inputTable = new ExternalDbMapTable();
        inputTable.setTableName(schema + "." + inputTable1);
        inputTable.setName(schema + "." + inputTable1);
        inputTable.setAlias("A");
        List<ExternalDbMapEntry> entities = getMetadataEntities(new String[] { "newColumn", "newColumn1" }, new String[2]);
        inputTable.setMetadataTableEntries(entities);
        inputs.add(inputTable);

        // lookup table
        inputTable = new ExternalDbMapTable();
        inputTable.setTableName(schema + "." + inputTable2);
        inputTable.setName(schema + "." + inputTable2);
        inputTable.setAlias("B");
        entities = getMetadataEntities(new String[] { "newColumn", "newColumn1" }, new String[2]);
        ExternalDbMapEntry newColumn = entities.get(0);
        newColumn.setExpression("A.newColumn");
        newColumn.setOperator("=");
        inputTable.setJoinType(JOIN.CROSS_JOIN.name());
        newColumn.setJoin(true);
        inputTable.setMetadataTableEntries(entities);
        inputs.add(inputTable);
        // lookup talbe no.2
        inputTable = new ExternalDbMapTable();
        inputTable.setTableName(schema + "." + inputTable3);
        inputTable.setName(schema + "." + inputTable3);
        inputTable.setAlias("C");
        entities = getMetadataEntities(new String[] { "id", "name" }, new String[2]);
        ExternalDbMapEntry newColumn2 = entities.get(0);
        newColumn2.setExpression("B.id");
        newColumn2.setOperator("=");
        inputTable.setJoinType(JOIN.CROSS_JOIN.name());
        newColumn2.setJoin(false);
        inputTable.setMetadataTableEntries(entities);
        inputs.add(inputTable);
        // output
        ExternalDbMapTable outputTable = new ExternalDbMapTable();
        outputTable.setName(schema + "." + outTable1);
        outputTable.setTableName(schema + "." + outTable1);
        String[] names = new String[] { "tarColumn", "tarColumn1" };
        String[] expressions = new String[] { "A.newColumn", "A.newColumn1" };
        outputTable.setMetadataTableEntries(getMetadataEntities(names, expressions));
        outputs.add(outputTable);

        externalData.setInputTables(inputs);
        externalData.setOutputTables(outputs);
        dbMapComponent.setExternalData(externalData);

        List<IConnection> incomingConnections = new ArrayList<IConnection>();
        incomingConnections.add(
                mockConnection(schema, inputTable1, new String[] { "newColumn", "newColumn1" }, new String[] { "id", "name" }));
        incomingConnections.add(
                mockConnection(schema, inputTable2, new String[] { "newColumn", "newColumn1" }, new String[] { "id", "name" }));
        incomingConnections.add(
                mockConnection(schema, inputTable3, new String[] { "newColumn", "newColumn1" }, new String[] { "id", "name" }));
        dbMapComponent.setIncomingConnections(incomingConnections);

        List<IConnection> outputConnections = new ArrayList<IConnection>();
        Node map1 = mockNode(dbMapComponent);
        IConnection connection = mockConnection(map1, schema, inputTable1, new String[] { "id", "name" });
        targetComponent = ComponentsFactoryProvider.getInstance().get("tELTMSSqlOutput",
                ComponentCategory.CATEGORY_4_DI.getName());
        connection.getMetadataTable().getColumn("id").setLabel("newColumn");
        connection.getMetadataTable().getColumn("name").setLabel("newColumn1");
        // add target
        DataNode output = new DataNode();
        List<IElementParameter> paraList = new ArrayList<IElementParameter>();
        ElementParameter param = new ElementParameter(output);
        param.setName("USE_UPDATE_STATEMENT"); //$NON-NLS-1$
        param.setValue("true"); //$NON-NLS-1$
        paraList.add(param);
        output.setElementParameters(paraList);
        output.setComponent(targetComponent);

        DataConnection dataConnection = new DataConnection();
        dataConnection.setName(schema + "." + outTable1);
        dataConnection.setActivate(true);
        dataConnection.setLineStyle(EConnectionType.FLOW_MAIN);
        dataConnection.setTarget(output);
        IMetadataTable table = new MetadataTable();
        table.setLabel(outTable1);
        table.setTableName(outTable1);
        List<IMetadataColumn> listColumns = new ArrayList<IMetadataColumn>();
        for (String columnName : new String[] { "id", "name" }) {
            IMetadataColumn column = new MetadataColumn();
            column.setLabel(columnName);
            column.setOriginalDbColumnName(columnName);
            listColumns.add(column);
        }
        table.setListColumns(listColumns);
        dataConnection.setMetadataTable(table);
        // List<DataConnection> dataConnections = new ArrayList<>();
        outputConnections.add(dataConnection);
        outputConnections.add(connection);
        dbMapComponent.setOutgoingConnections(outputConnections);

        Process process = mock(Process.class);
        when(process.getContextManager()).thenReturn(new JobContextManager());
        dbMapComponent.setProcess(process);

        IContextParameter lookupTableContext = new JobContextParameter();
        lookupTableContext.setName("lookup");
        lookupTableContext.setValue("lookupTable");
        lookupTableContext.setType("String");
        String query = dbManager.buildSqlSelect(dbMapComponent, schema + "." + outTable1);
        String expectedQuery = "\"UPDATE dbo.tar\n" + "SET tarColumn = A.id,\n" + "tarColumn1 = A.name\n"
                + "FROM\n \" +dbo+\".\"+src1+ \" A CROSS JOIN  \n\" +dbo+\".\"+src2+ \" B CROSS JOIN  \n\" +dbo+\".\"+src3+ \" C"
                + "\nWHERE" + "\n  B.id = A.id\n  AND  C.id = B.id\"";
        assertEquals(expectedQuery, query);
    }

    @Test
    public void testBuildSqlSelectWithColumnsAliasIfChecked() {
        // Only support teradata columns alias now, others not add it .
        MssqlGenerationManager manager = new MssqlGenerationManager();
        manager.setUseAliasInOutputTable(true);
        String schema = "";
        String main_table = "main_table";
        String main_alias = "";
        String lookup_table = "lookup_table";
        String lookup_alias = "";
        init4ColumnAlias(schema, main_table, main_alias, lookup_table, lookup_alias);
        String expectedQuery = "\"SELECT\n"
                + "main_table.id AS id, main_table.name_alias AS name, main_table.age_alias AS age, lookup_table.score AS score\n"
                + "FROM\n" + " main_table , lookup_table\"";
        String query = manager.buildSqlSelect(dbMapComponent, "grade");
        assertEquals(expectedQuery, query);
    }

    @Test
    public void testBuildSqlSelectWithColumnsAliasIfunChecked() {
        MssqlGenerationManager manager = new MssqlGenerationManager();
        manager.setUseAliasInOutputTable(false);
        String schema = "";
        String main_table = "main_table";
        String main_alias = "";
        String lookup_table = "lookup_table";
        String lookup_alias = "";
        init4ColumnAlias(schema, main_table, main_alias, lookup_table, lookup_alias);
        String expectedQuery = "\"SELECT\n" + "main_table.id, main_table.name_alias, main_table.age_alias, lookup_table.score\n"
                + "FROM\n" + " main_table , lookup_table\"";
        String query = manager.buildSqlSelect(dbMapComponent, "grade");
        assertEquals(expectedQuery, query);
    }
}
