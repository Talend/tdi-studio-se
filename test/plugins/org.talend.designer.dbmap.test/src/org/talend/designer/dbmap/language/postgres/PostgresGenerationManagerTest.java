package org.talend.designer.dbmap.language.postgres;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.talend.core.model.context.JobContext;
import org.talend.core.model.context.JobContextManager;
import org.talend.core.model.context.JobContextParameter;
import org.talend.core.model.metadata.IMetadataColumn;
import org.talend.core.model.metadata.IMetadataTable;
import org.talend.core.model.metadata.MetadataColumn;
import org.talend.core.model.metadata.MetadataTable;
import org.talend.core.model.process.IConnection;
import org.talend.core.model.process.IContextParameter;
import org.talend.designer.core.model.components.ElementParameter;
import org.talend.designer.core.ui.editor.connections.Connection;
import org.talend.designer.core.ui.editor.nodes.Node;
import org.talend.designer.core.ui.editor.process.Process;
import org.talend.designer.dbmap.DbMapComponent;
import org.talend.designer.dbmap.external.data.ExternalDbMapData;
import org.talend.designer.dbmap.external.data.ExternalDbMapEntry;
import org.talend.designer.dbmap.external.data.ExternalDbMapTable;

public class PostgresGenerationManagerTest {

    DbMapComponent component = new DbMapComponent();

    @Before
    public void setUp() throws Exception {
    }

    private void init(String schema, String main_table, String lookup_table) {
        List<IConnection> incomingConnections = new ArrayList<IConnection>();
        String[] mainTableEntities = new String[] { "id", "name", "classNum" };
        String[] lookupEndtities = new String[] { "id", "score" };
        incomingConnections.add(mockConnection(schema, main_table, mainTableEntities));
        incomingConnections.add(mockConnection(schema, lookup_table, lookupEndtities));
        component.setIncomingConnections(incomingConnections);

        ExternalDbMapData externalData = new ExternalDbMapData();
        List<ExternalDbMapTable> inputs = new ArrayList<ExternalDbMapTable>();
        List<ExternalDbMapTable> outputs = new ArrayList<ExternalDbMapTable>();
        // main table
        ExternalDbMapTable inputTable = new ExternalDbMapTable();
        inputTable.setTableName(schema + "." + main_table);
        List<ExternalDbMapEntry> entities = getMetadataEntities(mainTableEntities, new String[3]);
        inputTable.setMetadataTableEntries(entities);
        inputs.add(inputTable);
        // lookup table
        inputTable = new ExternalDbMapTable();
        inputTable.setTableName(schema + "." + lookup_table);
        entities = getMetadataEntities(lookupEndtities, new String[2]);
        inputTable.setMetadataTableEntries(entities);
        inputs.add(inputTable);

        // output
        ExternalDbMapTable outputTable = new ExternalDbMapTable();
        outputTable.setName("grade");
        List<ExternalDbMapEntry> conditions = new ArrayList<ExternalDbMapEntry>();
        conditions.add(new ExternalDbMapEntry(schema + "." + main_table + ".id =3"));
        outputTable.setCustomWhereConditionsEntries(conditions);
        String[] names = new String[] { "id", "name", "classNum", "score" };
        String[] expressions = new String[] { schema + "." + main_table + ".id", schema + "." + main_table + ".name",
                schema + "." + main_table + ".classNum", schema + "." + lookup_table + ".score" };
        outputTable.setMetadataTableEntries(getMetadataEntities(names, expressions));
        outputs.add(outputTable);

        externalData.setInputTables(inputs);
        externalData.setOutputTables(outputs);
        component.setExternalData(externalData);
        List<IMetadataTable> metadataList = new ArrayList<IMetadataTable>();
        MetadataTable metadataTable = getMetadataTable(names);
        metadataTable.setLabel("grade");
        metadataList.add(metadataTable);
        component.setMetadataList(metadataList);
        Process process = mock(Process.class);
        when(process.getContextManager()).thenReturn(new JobContextManager());
        component.setProcess(process);
    }

    private MetadataTable getMetadataTable(String[] entitiesName) {
        MetadataTable table = new MetadataTable();
        for (String element : entitiesName) {
            MetadataColumn column = new MetadataColumn();
            column.setLabel(element);
            table.getListColumns().add(column);
        }
        return table;
    }

    private List<ExternalDbMapEntry> getMetadataEntities(String[] entitiesName, String[] expressions) {
        List<ExternalDbMapEntry> entities = new ArrayList<ExternalDbMapEntry>();
        for (int i = 0; i < entitiesName.length; i++) {
            ExternalDbMapEntry entity = new ExternalDbMapEntry();
            entity.setName(entitiesName[i]);
            if (i < expressions.length && !"".equals(expressions[i]) && expressions[i] != null) {
                entity.setExpression(expressions[i]);
            }
            entities.add(entity);
        }
        return entities;
    }

    private IConnection mockConnection(String schemaName, String tableName, String[] columns) {
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
        for (String columnName : columns) {
            IMetadataColumn column = new MetadataColumn();
            column.setLabel(columnName);
            column.setOriginalDbColumnName(columnName);
            listColumns.add(column);
        }
        table.setListColumns(listColumns);
        when(connection.getMetadataTable()).thenReturn(table);

        return connection;
    }

    @Test
    public void testBuildSqlSelect() {
        // without context
        String schema = "school";
        String main_table = "classInfo";
        String lookup_table = "scoreInfo";
        init(schema, main_table, lookup_table);
        PostgresGenerationManager manager = new PostgresGenerationManager();
        String query = manager.buildSqlSelect(component, "grade");
        assertNotNull(query);
        query = query.replaceAll("[\\s]", "");
        String expectedQuery = "\"SELECT"
                + "\\\"school\\\".\\\"classInfo\\\".\\\"id\\\",\\\"school\\\".\\\"classInfo\\\".\\\"name\\\",\\\"school\\\".\\\"classInfo\\\".\\\"classNum\\\",\\\"school\\\".\\\"scoreInfo\\\".\\\"score\\\""
                + "FROM\\\"school\\\".\\\"classInfo\\\",\\\"school\\\".\\\"scoreInfo\\\""
                + "WHERE\\\"school\\\".\\\"classInfo\\\".\\\"id\\\"=3\"";
        assertEquals(expectedQuery, query);

        // with context
        schema = "context.schema";
        main_table = "context.main_table";
        lookup_table = "context.lookup";
        init(schema, main_table, lookup_table);
        JobContext newContext = new JobContext("Default");
        List<IContextParameter> newParamList = new ArrayList<IContextParameter>();
        newContext.setContextParameterList(newParamList);
        JobContextParameter param = new JobContextParameter();
        param.setName("schema");
        newParamList.add(param);
        param = new JobContextParameter();
        param.setName("main_table");
        newParamList.add(param);
        param = new JobContextParameter();
        param.setName("lookup");
        newParamList.add(param);
        component.getProcess().getContextManager().setDefaultContext(newContext);
        query = manager.buildSqlSelect(component, "grade");
        query = query.replaceAll("[\\s]", "");
        expectedQuery = "\"SELECT\"+context.schema+\".\"+context.main_table+\".\\\"id\\\",\"+context.schema+\".\"+"
                + "context.main_table+\".\\\"name\\\",\"+context.schema+\".\"+context.main_table+\".\\\"classNum\\\",\"+"
                + "context.schema+\".\"+context.lookup+\".\\\"score\\\"FROM\"+context.schema+\".\"+"
                + "context.main_table+\",\"+context.schema+\".\"+context.lookup+\"WHERE\"+context.schema+\".\"+"
                + "context.main_table+\".\\\"id\\\"=3\"";
        assertEquals(expectedQuery, query);
    }

}
