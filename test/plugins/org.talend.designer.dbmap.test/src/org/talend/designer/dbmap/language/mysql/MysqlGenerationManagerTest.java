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
package org.talend.designer.dbmap.language.mysql;

import static org.junit.Assert.*;

import java.util.Collections;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.talend.designer.dbmap.DbMapComponent;
import org.talend.designer.dbmap.language.generation.DbGenerationManagerTestHelper;

/**
 * created by wchen on Nov 23, 2017 Detailled comment
 *
 */
public class MysqlGenerationManagerTest extends DbGenerationManagerTestHelper {

    @Before
    public void setUp() throws Exception {
        dbMapComponent = new DbMapComponent();
        if (dbMapComponent.getElementParameters() == null) {
            dbMapComponent.setElementParameters(Collections.EMPTY_LIST);
        }
    }

    @After
    public void tearDown() throws Exception {
        dbMapComponent = null;
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
        MysqlGenerationManager manager = new MysqlGenerationManager();
        String query = manager.buildSqlSelect(dbMapComponent, "grade");
        assertEquals(expectedQuery, query);

        // schema.((String)globalMap.get("tableName")).columnName
        init(schema, main_table, null, lookup_table, null);
        expectedQuery = "\"SELECT\n"
                + "\" +((String)globalMap.get(\"schema\"))+ \".\" +((String)globalMap.get(\"main_table\"))+ \".id, \" +((String)globalMap.get(\"schema\"))+ \".\" +((String)globalMap.get(\"main_table\"))+ \".name,"
                + " \" +((String)globalMap.get(\"schema\"))+ \".\" +((String)globalMap.get(\"main_table\"))+ \".age, \""
                + " +((String)globalMap.get(\"schema\"))+ \".\" +((String)globalMap.get(\"lookup_table\"))+ \".score\n"
                + "FROM\n"
                + " \" +((String)globalMap.get(\"schema\"))+ \".\" +((String)globalMap.get(\"main_table\"))+ \" , \" +((String)globalMap.get(\"schema\"))+ \".\" +((String)globalMap.get(\"lookup_table\"))";
        query = manager.buildSqlSelect(dbMapComponent, "grade");
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
        query = manager.buildSqlSelect(dbMapComponent, "grade");
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
        query = manager.buildSqlSelect(dbMapComponent, "grade");
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
        MysqlGenerationManager manager = new MysqlGenerationManager();
        String query = manager.buildSqlSelect(dbMapComponent, "grade");
        assertEquals(expectedQuery, query);

        main_table = "((String)globalMap.get(\"#main\\table)\"))";
        lookup_table = "((String)globalMap.get(\"@lookup\\\\table*\"))";
        init("", main_table, null, lookup_table, null);
        expectedQuery = "\"SELECT\n"
                + "\" +((String)globalMap.get(\"#main\\table)\"))+ \".id, \" +((String)globalMap.get(\"#main\\table)\"))+ \".name,"
                + " \" +((String)globalMap.get(\"#main\\table)\"))+ \".age, \" +((String)globalMap.get(\"@lookup\\\\table*\"))+ \".score\n"
                + "FROM\n"
                + " \" +((String)globalMap.get(\"#main\\table)\"))+ \" , \" +((String)globalMap.get(\"@lookup\\\\table*\"))";
        manager = new MysqlGenerationManager();
        query = manager.buildSqlSelect(dbMapComponent, "grade");
        assertEquals(expectedQuery, query);

    }

    @Test
    public void testBuildSqlSelectWithAlias() {
        String schema = "";
        String main_table = "((String)globalMap.get(\"main_table\"))";
        String main_alias = "main_table";
        String lookup_table = "((String)globalMap.get(\"lookup_table\"))";
        String lookup_alias = "";
        init(schema, main_table, main_alias, lookup_table, lookup_alias);
        String expectedQuery = "\"SELECT\n"
                + "main_table.id, main_table.name, main_table.age, \" +((String)globalMap.get(\"lookup_table\"))+ \".score\n"
                + "FROM\n"
                + " \" +((String)globalMap.get(\"main_table\"))+ \" main_table , \" +((String)globalMap.get(\"lookup_table\"))";
        MysqlGenerationManager manager = new MysqlGenerationManager();
        String query = manager.buildSqlSelect(dbMapComponent, "grade");
        assertEquals(expectedQuery, query);

        main_table = "((String)globalMap.get(\"main_table\"))+context.main_table";
        main_alias = "main_table";
        lookup_table = "((String)globalMap.get(\"lookup_table\"))";
        lookup_alias = "lookup_table";
        init(schema, main_table, main_alias, lookup_table, lookup_alias);
        expectedQuery = "\"SELECT\n"
                + "main_table.id, main_table.name, main_table.age, lookup_table.score\n"
                + "FROM\n"
                + " \" +((String)globalMap.get(\"main_table\"))+context.main_table+ \" main_table , \" +((String)globalMap.get(\"lookup_table\"))+ \" lookup_table\"";
        manager = new MysqlGenerationManager();
        query = manager.buildSqlSelect(dbMapComponent, "grade");
        assertEquals(expectedQuery, query);

    }
}
