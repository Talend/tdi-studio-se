package org.talend.designer.dbmap.language.generation;

import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.talend.core.model.metadata.IMetadataColumn;
import org.talend.core.model.metadata.IMetadataTable;
import org.talend.core.model.metadata.MetadataColumn;
import org.talend.core.model.metadata.MetadataTable;
import org.talend.core.model.process.IConnection;
import org.talend.core.model.utils.TalendTextUtils;
import org.talend.designer.core.ui.editor.connections.Connection;
import org.talend.designer.dbmap.DbMapComponent;
import org.talend.designer.dbmap.external.data.ExternalDbMapData;
import org.talend.designer.dbmap.external.data.ExternalDbMapEntry;
import org.talend.designer.dbmap.external.data.ExternalDbMapTable;

public class DbGenerationManagerTest extends DbGenerationManagerTestHelper {

    private DbGenerationManager dbManager;

    private ExternalDbMapEntry extMapEntry;

    private List<ExternalDbMapEntry> tableEntries;

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

        dbManager = new GenericDbGenerationManager();

    }

    @After
    public void tearDown() throws Exception {
        dbMapComponent = null;
        dbManager = null;
        extMapEntry = null;
        tableEntries = null;
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

}
