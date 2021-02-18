// ============================================================================
//
// Copyright (C) 2006-2019 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.designer.dbmap.language.oracle;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.Collections;
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
import org.talend.designer.dbmap.DbMapComponent;
import org.talend.designer.dbmap.language.generation.DbGenerationManagerTestHelper;

/**
 * created by ggu on Jun 25, 2012 Detailled comment
 *
 */

public class OracleGenerationManagerTest extends DbGenerationManagerTestHelper {

    private IConnection conn;

    private OracleGenerationManager oracleManager;

    @Before
    public void setUp() throws Exception {
        dbMapComponent = new DbMapComponent();

        List<IConnection> incomingConnections = new ArrayList<IConnection>();

        conn = mock(IConnection.class);
        incomingConnections.add(conn);
        dbMapComponent.setIncomingConnections(incomingConnections);

        if (dbMapComponent.getElementParameters() == null) {
            dbMapComponent.setElementParameters(Collections.EMPTY_LIST);
        }

        oracleManager = new OracleGenerationManager();

    }

    @After
    public void tearDown() throws Exception {
        dbMapComponent = null;
        conn = null;
        oracleManager = null;
    }

    /**
     *
     * for TDI-21413
     *
     */
    @Test
    public void testAddQuoteForSpecialChar_NPE() throws Exception {
        // only test the conn.getMetadataTable() for NPE
        checkValue("test.ABC", "test.ABC"); //$NON-NLS-1$ //$NON-NLS-2$
    }

    /**
     *
     * for TDI-21413
     *
     */
    @Test
    public void testAddQuoteForSpecialChar_Order1() throws Exception {
        // positive-sequence
        when(conn.getMetadataTable()).thenReturn(createMetadataTable(new String[] { "都市名", "国", "国(コード)",//$NON-NLS-1$ //$NON-NLS-2$
                "国(コード)123", "abc$1234", "abc$1234xyz" })); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

        testMore();
        //
    }

    @Test
    public void testAddQuoteForSpecialChar_Order2() throws Exception {
        // reverse order
        when(conn.getMetadataTable()).thenReturn(createMetadataTable(new String[] { "都市名", "国(コード)123", "国(コード)",//$NON-NLS-1$ //$NON-NLS-2$
                "国", "abc$1234", "abc$1234xyz" })); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

        testMore();
        //
    }

    @Test
    public void testAddQuoteForSpecialChar_Order3() throws Exception {

        when(conn.getMetadataTable()).thenReturn(createMetadataTable(new String[] { "都市名", "国(コード)", "国(コード)123",//$NON-NLS-1$ //$NON-NLS-2$
                "国", "abc$1234", "abc$1234xyz" })); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

        testMore();
        //
    }

    @Test
    public void testAddQuoteForSpecialChar_Order4() throws Exception {

        when(conn.getMetadataTable()).thenReturn(createMetadataTable(new String[] { "都市名", "国", "国(コード)123",//$NON-NLS-1$ //$NON-NLS-2$
                "国(コード)", "abc$1234", "abc$1234xyz" })); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

        testMore();
        //
    }

    @Test
    public void testAddQuoteForSpecialChar_Escape5() throws Exception {
        when(conn.getMetadataTable()).thenReturn(
                createMetadataTable(new String[] { "\\\"id\\\"", "\\\"name\\\"", "$acount", "_age", "email" }));

        testEscape();
        //
    }

    public void testEscape() throws Exception {

        checkValue("A.\\\"id\\\"", "A.\\\"id\\\"");
        checkValue("A.\\\"name\\\"", "A.\\\"name\\\"");
        checkValue("A.$acount", "A.\\\"$acount\\\"");
        checkValue("A.\\\"_age\\\"", "A.\\\"_age\\\"");
        checkValue("A.email", "A.email");
        // context
        checkValue("context.schema.\\\"id\\\"", "context.schema.\\\"id\\\"");
        checkValue("context.schema.\\\"name\\\"", "context.schema.\\\"name\\\"");
        checkValue("context.schema.$acount", "context.schema.\\\"$acount\\\"");
        checkValue("context.schema._age", "context.schema._age");
        checkValue("context.schema.email", "context.schema.email");
    }

    private void testMore() throws Exception {

        checkValue("A.都市名", "A.\\\"都市名\\\""); //$NON-NLS-1$ //$NON-NLS-2$
        checkValue("A.国(コード)", "A.\\\"国(コード)\\\""); //$NON-NLS-1$ //$NON-NLS-2$
        checkValue("A.国(コード)123", "A.\\\"国(コード)123\\\""); //$NON-NLS-1$ //$NON-NLS-2$
        checkValue("A.国", "A.\\\"国\\\""); //$NON-NLS-1$ //$NON-NLS-2$
        checkValue("A.abc$1234", "A.\\\"abc$1234\\\""); //$NON-NLS-1$ //$NON-NLS-2$
        checkValue("A.abc$1234xyz", "A.\\\"abc$1234xyz\\\""); //$NON-NLS-1$ //$NON-NLS-2$
        // context
        // not good context
        checkValue("context.schema.国", "context.schema.\\\"国\\\""); //$NON-NLS-1$ //$NON-NLS-2$
        checkValue("context.schema.国(コード)", "context.schema.\\\"国(コード)\\\""); //$NON-NLS-1$ //$NON-NLS-2$
        checkValue("context.schema.国(コード)123", "context.schema.\\\"国(コード)123\\\""); //$NON-NLS-1$ //$NON-NLS-2$
        // should be context model
        checkValue("context.schema+\".国\"", "context.schema+\".\\\"国\\\"\""); //$NON-NLS-1$ //$NON-NLS-2$
        checkValue("context.schema+\".国(コード)\"", "context.schema+\".\\\"国(コード)\\\"\""); //$NON-NLS-1$ //$NON-NLS-2$
        checkValue("context.schema+\".国(コード)123\"", "context.schema+\".\\\"国(コード)123\\\"\""); //$NON-NLS-1$ //$NON-NLS-2$
    }

    private void checkValue(String expression, String expected) throws Exception {

        Assert.assertEquals(expected, oracleManager.addQuoteForSpecialChar(expression, dbMapComponent));
    }

    private IMetadataTable createMetadataTable(String... columnLabels) {
        IMetadataTable metadataTable = new MetadataTable();

        // will be error, strange
        // List<IMetadataColumn> columns = mock(List.class);
        List<IMetadataColumn> columns = new ArrayList<IMetadataColumn>();
        if (columnLabels != null) {
            for (String columnlabel : columnLabels) {
                columns.add(createColumn(columnlabel));
            }
        }
        // when(metadataTable.getListColumns()).thenReturn(columns);
        metadataTable.setListColumns(columns);
        return metadataTable;
    }

    private IMetadataColumn createColumn(String label) {
        IMetadataColumn column = new MetadataColumn();

        // will be error, strange
        // when(column.getLabel()).thenReturn(label);
        column.setLabel(label);
        column.setOriginalDbColumnName(label);
        return column;
    }

    @Test
    public void testBuildSqlSelectForGlobalMap() {
        String schema = "((String)globalMap.get(\"schema\"))";
        String main_table = "((String)globalMap.get(\"main_table\"))";
        String lookup_table = "((String)globalMap.get(\"lookup_table\"))";

        // ((String)globalMap.get("tableName")).columnName
        init("", main_table, null, lookup_table, null);
        String expectedQuery = "\"SELECT\n"
                + "\" +((String)globalMap.get(\"main_table\"))+ \".id, \" +((String)globalMap.get(\"main_table\"))+ \".name,"
                + " \" +((String)globalMap.get(\"main_table\"))+ \".age, \" +((String)globalMap.get(\"lookup_table\"))+ \".score\n"
                + "FROM\n" + " \" +((String)globalMap.get(\"main_table\"))+ \" , \" +((String)globalMap.get(\"lookup_table\"))";
        OracleGenerationManager oManager = new OracleGenerationManager();
        String query = oManager.buildSqlSelect(dbMapComponent, "grade");
        assertEquals(expectedQuery, query);

        // schema.((String)globalMap.get("tableName")).columnName
        init(schema, main_table, null, lookup_table, null);
        expectedQuery = "\"SELECT\n"
                + "\" +((String)globalMap.get(\"schema\"))+ \".\" +((String)globalMap.get(\"main_table\"))+ \".id, \" +((String)globalMap.get(\"schema\"))+ \".\" +((String)globalMap.get(\"main_table\"))+ \".name,"
                + " \" +((String)globalMap.get(\"schema\"))+ \".\" +((String)globalMap.get(\"main_table\"))+ \".age, \""
                + " +((String)globalMap.get(\"schema\"))+ \".\" +((String)globalMap.get(\"lookup_table\"))+ \".score\n"
                + "FROM\n"
                + " \" +((String)globalMap.get(\"schema\"))+ \".\" +((String)globalMap.get(\"main_table\"))+ \" , \" +((String)globalMap.get(\"schema\"))+ \".\" +((String)globalMap.get(\"lookup_table\"))";
        query = oManager.buildSqlSelect(dbMapComponent, "grade");
        assertEquals(expectedQuery, query);

        // schema.((String)globalMap.get("tableName")).columnName
        schema = "my_schema";
        init(schema, main_table, null, lookup_table, null);
        expectedQuery = "\"SELECT\n"
                + "my_schema.\" +((String)globalMap.get(\"main_table\"))+ \".id, my_schema.\" +((String)globalMap.get(\"main_table\"))+ \".name,"
                + " my_schema.\" +((String)globalMap.get(\"main_table\"))+ \".age,"
                + " my_schema.\" +((String)globalMap.get(\"lookup_table\"))+ \".score\n"
                + "FROM\n"
                + " my_schema.\" +((String)globalMap.get(\"main_table\"))+ \" , my_schema.\" +((String)globalMap.get(\"lookup_table\"))";
        query = oManager.buildSqlSelect(dbMapComponent, "grade");
        assertEquals(expectedQuery, query);

        // ((String)globalMap.get("schema")).tableName.columnName
        schema = "((String)globalMap.get(\"schema\"))";
        main_table = "main_table";
        init(schema, main_table, null, lookup_table, null);
        expectedQuery = "\"SELECT\n"
                + "\" +((String)globalMap.get(\"schema\"))+ \".main_table.id, \" +((String)globalMap.get(\"schema\"))+ \".main_table.name,"
                + " \" +((String)globalMap.get(\"schema\"))+ \".main_table.age, \""
                + " +((String)globalMap.get(\"schema\"))+ \".\" +((String)globalMap.get(\"lookup_table\"))+ \".score\n"
                + "FROM\n"
                + " \" +((String)globalMap.get(\"schema\"))+ \".main_table , \" +((String)globalMap.get(\"schema\"))+ \".\" +((String)globalMap.get(\"lookup_table\"))";
        query = oManager.buildSqlSelect(dbMapComponent, "grade");
        assertEquals(expectedQuery, query);

    }

    @Test
    public void testBuildSqlSelectForGlobalMapForSpecialCharacters() {
        // test special charactor in globalmap
        String main_table = "((String)globalMap.get(\"#main_table%\"))";
        String lookup_table = "((String)globalMap.get(\"@lookup_table-\"))";
        init("", main_table, null, lookup_table, null);
        String expectedQuery = "\"SELECT\n"
                + "\" +((String)globalMap.get(\"#main_table%\"))+ \".id, \" +((String)globalMap.get(\"#main_table%\"))+ \".name,"
                + " \" +((String)globalMap.get(\"#main_table%\"))+ \".age, \" +((String)globalMap.get(\"@lookup_table-\"))+ \".score\n"
                + "FROM\n"
                + " \" +((String)globalMap.get(\"#main_table%\"))+ \" , \" +((String)globalMap.get(\"@lookup_table-\"))";

        OracleGenerationManager oManager = new OracleGenerationManager();
        String query = oManager.buildSqlSelect(dbMapComponent, "grade");
        assertEquals(expectedQuery, query);

        main_table = "((String)globalMap.get(\"#main\\*table%\"))";
        lookup_table = "((String)globalMap.get(\"@lookup+table-\"))";
        init("", main_table, null, lookup_table, null);
        expectedQuery = "\"SELECT\n"
                + "\" +((String)globalMap.get(\"#main\\*table%\"))+ \".id, \" +((String)globalMap.get(\"#main\\*table%\"))+ \".name,"
                + " \" +((String)globalMap.get(\"#main\\*table%\"))+ \".age, \" +((String)globalMap.get(\"@lookup+table-\"))+ \".score\n"
                + "FROM\n"
                + " \" +((String)globalMap.get(\"#main\\*table%\"))+ \" , \" +((String)globalMap.get(\"@lookup+table-\"))";

        oManager = new OracleGenerationManager();
        query = oManager.buildSqlSelect(dbMapComponent, "grade");
        assertEquals(expectedQuery, query);
    }

    @Test
    public void testBuildSqlSelect() {
        String schema = "";
        String main_table = "((String)globalMap.get(\"main_table\"))";
        String main_alias = "main1";
        String lookup_table = "((String)globalMap.get(\"lookup_table\"))";
        String lookup_alias = "lookup1";
        init(schema, main_table, main_alias, lookup_table, lookup_alias);
        String expectedQuery = "\"SELECT\n"
                + "main1.id, main1.name, main1.age, lookup1.score\n"
                + "FROM\n"
                + " \" +((String)globalMap.get(\"main_table\"))+ \" main1 , \" +((String)globalMap.get(\"lookup_table\"))+ \" lookup1\"";
        OracleGenerationManager manager = new OracleGenerationManager();
        String query = manager.buildSqlSelect(dbMapComponent, "grade");
        assertEquals(expectedQuery, query);

        schema = "context.schema";
        main_table = "((String)globalMap.get(\"main_table\"))+((String)globalMap.get(\"main_table1\"))";
        main_alias = "main_table";
        lookup_table = "((String)globalMap.get(\"lookup_table\"))";
        lookup_alias = "";
        init(schema, main_table, main_alias, lookup_table, lookup_alias);
        expectedQuery = "\"SELECT\n"
                + "main_table.id, main_table.name, main_table.age, \""
                + " +context.schema+ \".\" +((String)globalMap.get(\"lookup_table\"))+ \".score\n"
                + "FROM\n"
                + " \" +context.schema+\".\"+((String)globalMap.get(\"main_table\"))+((String)globalMap.get(\"main_table1\"))+ \" main_table , \""
                + " +context.schema+ \".\" +((String)globalMap.get(\"lookup_table\"))";
        manager = new OracleGenerationManager();
        query = manager.buildSqlSelect(dbMapComponent, "grade");
        assertEquals(expectedQuery, query);
    }

    @Test
    public void testBuildSqlSelectWithAlias() {
        String schema = "";
        String main_table = "((String)globalMap.get(\"main_table\"))";
        String main_alias = "main1";
        String lookup_table = "((String)globalMap.get(\"lookup_table\"))";
        String lookup_alias = "lookup1";
        init(schema, main_table, main_alias, lookup_table, lookup_alias);
        String expectedQuery = "\"SELECT\n" + "main1.id, main1.name, main1.age, lookup1.score\n"
                + "FROM\n"
                + " \" +((String)globalMap.get(\"main_table\"))+ \" main1 , \" +((String)globalMap.get(\"lookup_table\"))+ \" lookup1\"";
        OracleGenerationManager manager = new OracleGenerationManager();
        manager.setUseAliasInOutputTable(true);
        String query = manager.buildSqlSelect(dbMapComponent, "grade");
        assertEquals(expectedQuery, query);

        schema = "context.schema";
        main_table = "((String)globalMap.get(\"main_table\"))+((String)globalMap.get(\"main_table1\"))";
        main_alias = "main_table";
        lookup_table = "((String)globalMap.get(\"lookup_table\"))";
        lookup_alias = "";
        init(schema, main_table, main_alias, lookup_table, lookup_alias);
        expectedQuery = "\"SELECT\n" + "main_table.id, main_table.name, main_table.age, \""
                + " +context.schema+ \".\" +((String)globalMap.get(\"lookup_table\"))+ \".score AS score\n" + "FROM\n"
                + " \" +context.schema+\".\"+((String)globalMap.get(\"main_table\"))+((String)globalMap.get(\"main_table1\"))+ \" main_table , \""
                + " +context.schema+ \".\" +((String)globalMap.get(\"lookup_table\"))";
        manager = new OracleGenerationManager();
        manager.setUseAliasInOutputTable(true);
        query = manager.buildSqlSelect(dbMapComponent, "grade");
        assertEquals(expectedQuery, query);
    }

    @Test
    public void testBuildSqlSelectWithColumnsAliasIfChecked() {
        OracleGenerationManager manager = new OracleGenerationManager();
        manager.setUseAliasInOutputTable(true);
        String schema = "";
        String main_table = "main_table";
        String main_alias = "";
        String lookup_table = "lookup_table";
        String lookup_alias = "";
        init4ColumnAlias(schema, main_table, main_alias, lookup_table, lookup_alias);
        String expectedQuery = "\"SELECT\n"
                + "main_table.id, main_table.name_alias AS name, main_table.age_alias AS age, lookup_table.score\n"
                + "FROM\n" + " main_table , lookup_table\"";
        String query = manager.buildSqlSelect(dbMapComponent, "grade");
        assertEquals(expectedQuery, query);
    }

    @Test
    public void testBuildSqlSelectWithColumnsAliasIfunChecked() {
        OracleGenerationManager manager = new OracleGenerationManager();
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
