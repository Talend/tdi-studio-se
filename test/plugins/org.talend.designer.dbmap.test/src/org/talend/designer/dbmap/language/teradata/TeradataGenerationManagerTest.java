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
package org.talend.designer.dbmap.language.teradata;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.talend.core.model.process.IConnection;
import org.talend.designer.dbmap.DbMapComponent;
import org.talend.designer.dbmap.language.generation.DbGenerationManagerTestHelper;

/**
 * 
 * created by hcyi on Nov 27, 2020 Detailled comment
 *
 */
public class TeradataGenerationManagerTest extends DbGenerationManagerTestHelper {

    private IConnection conn;

    private TeradataGenerationManager manager;

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
        manager = new TeradataGenerationManager();
    }

    @After
    public void tearDown() throws Exception {
        dbMapComponent = null;
        conn = null;
        manager = null;
    }

    @Test
    public void testBuildSqlSelectWithColumnsAlias4GlobalMapIfChecked() {
        // Add new option "Use alias in output table" in tELTTeradataMap ONLY now , uncheck by default to don't bring
        // regression issue.
        manager.setUseAliasInOutputTable(true);
        String schema = "";
        String main_table = "((String)globalMap.get(\"main_table\"))";
        String main_alias = "main1";
        String lookup_table = "((String)globalMap.get(\"lookup_table\"))";
        String lookup_alias = "lookup1";
        init(schema, main_table, main_alias, lookup_table, lookup_alias);
        String expectedQuery = "\"SELECT\n" + "main1.id AS id, main1.name AS name, main1.age AS age, lookup1.score AS score\n" + "FROM\n"
                + " \" +((String)globalMap.get(\"main_table\"))+ \" main1 , \" +((String)globalMap.get(\"lookup_table\"))+ \" lookup1\"";
        String query = manager.buildSqlSelect(dbMapComponent, "grade");
        assertEquals(expectedQuery, query);

        schema = "context.schema";
        main_table = "((String)globalMap.get(\"main_table\"))+((String)globalMap.get(\"main_table1\"))";
        main_alias = "main_table";
        lookup_table = "((String)globalMap.get(\"lookup_table\"))";
        lookup_alias = "";
        init(schema, main_table, main_alias, lookup_table, lookup_alias);
        expectedQuery = "\"SELECT\n" + "main_table.id AS id, main_table.name AS name, main_table.age AS age, \""
                + " +context.schema+ \".\" +((String)globalMap.get(\"lookup_table\"))+ \".score AS score\n" + "FROM\n"
                + " \" +context.schema+\".\"+((String)globalMap.get(\"main_table\"))+((String)globalMap.get(\"main_table1\"))+ \" main_table , \""
                + " +context.schema+ \".\" +((String)globalMap.get(\"lookup_table\"))";
        query = manager.buildSqlSelect(dbMapComponent, "grade");
        assertEquals(expectedQuery, query);
    }

    @Test
    public void testBuildSqlSelectWithColumnsAlias4GlobalMapIfunChecked() {
        manager.setUseAliasInOutputTable(false);
        String schema = "";
        String main_table = "((String)globalMap.get(\"main_table\"))";
        String main_alias = "main1";
        String lookup_table = "((String)globalMap.get(\"lookup_table\"))";
        String lookup_alias = "lookup1";
        init(schema, main_table, main_alias, lookup_table, lookup_alias);
        String expectedQuery = "\"SELECT\n" + "main1.id, main1.name, main1.age, lookup1.score\n" + "FROM\n"
                + " \" +((String)globalMap.get(\"main_table\"))+ \" main1 , \" +((String)globalMap.get(\"lookup_table\"))+ \" lookup1\"";
        String query = manager.buildSqlSelect(dbMapComponent, "grade");
        assertEquals(expectedQuery, query);

        schema = "context.schema";
        main_table = "((String)globalMap.get(\"main_table\"))+((String)globalMap.get(\"main_table1\"))";
        main_alias = "main_table";
        lookup_table = "((String)globalMap.get(\"lookup_table\"))";
        lookup_alias = "";
        init(schema, main_table, main_alias, lookup_table, lookup_alias);
        expectedQuery = "\"SELECT\n" + "main_table.id, main_table.name, main_table.age, \""
                + " +context.schema+ \".\" +((String)globalMap.get(\"lookup_table\"))+ \".score\n" + "FROM\n"
                + " \" +context.schema+\".\"+((String)globalMap.get(\"main_table\"))+((String)globalMap.get(\"main_table1\"))+ \" main_table , \""
                + " +context.schema+ \".\" +((String)globalMap.get(\"lookup_table\"))";
        query = manager.buildSqlSelect(dbMapComponent, "grade");
        assertEquals(expectedQuery, query);
    }

    @Test
    public void testBuildSqlSelectWithColumnsAliasIfChecked() {
        manager.setUseAliasInOutputTable(true);
        String schema = "";
        String main_table = "main_table";
        String main_alias = "";
        String lookup_table = "lookup_table";
        String lookup_alias = "";
        init4ColumnAlias(schema, main_table, main_alias, lookup_table, lookup_alias);
        String expectedQuery = "\"SELECT\n"
                + "main_table.id AS id, main_table.name_alias AS name, main_table.age_alias AS age, lookup_table.score AS score\n"
                + "FROM\n"
                + " main_table , lookup_table\"";
        String query = manager.buildSqlSelect(dbMapComponent, "grade");
        assertEquals(expectedQuery, query);
    }

    @Test
    public void testBuildSqlSelectWithColumnsAliasIfunChecked() {
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
