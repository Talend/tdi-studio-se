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
package org.talend.designer.dbmap.language.generation;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.talend.core.model.metadata.IMetadataColumn;
import org.talend.core.model.metadata.IMetadataTable;
import org.talend.core.model.metadata.MetadataColumn;
import org.talend.core.model.metadata.MetadataTable;
import org.talend.core.model.process.EParameterFieldType;
import org.talend.core.model.process.IConnection;
import org.talend.core.model.process.IElementParameter;
import org.talend.core.model.utils.TalendTextUtils;
import org.talend.designer.core.model.components.EParameterName;
import org.talend.designer.core.model.components.ElementParameter;
import org.talend.designer.core.ui.editor.connections.Connection;
import org.talend.designer.core.ui.editor.nodes.Node;
import org.talend.designer.dbmap.DbMapComponent;

/**
 * created by hcyi on Jan 14, 2021
 * Detailled comment
 *
 */
public class GenericDbGenerationManagerTest extends DbGenerationManagerTestHelper {

    private GenericDbGenerationManager manager;

    private IConnection conn;

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
        manager = new GenericDbGenerationManager();
    }

    @After
    public void tearDown() throws Exception {
        dbMapComponent = null;
        conn = null;
        manager = null;
    }

    @Test
    public void testBuildSqlSelect() {
        manager.setUseDelimitedIdentifiers(false);
        String schema = "";
        String main_table = "main_table";
        String main_alias = "";
        String lookup_table = "lookup_table";
        String lookup_alias = "";
        init(schema, main_table, main_alias, lookup_table, lookup_alias);
        String expectedQuery = "\"SELECT\n" + "main_table.id, main_table.name, main_table.age, lookup_table.score\n" + "FROM\n"
                + " main_table , lookup_table\"";
        String query = manager.buildSqlSelect(dbMapComponent, "grade");
        assertEquals(expectedQuery, query);
    }

    @Test
    public void testBuildSqlSelectWithUseDelimitedIdentifiers() {
        manager.setUseDelimitedIdentifiers(true);
        String schema = "";
        String main_table = "main_table";
        String main_alias = "";
        String lookup_table = "lookup_table";
        String lookup_alias = "";
        init(schema, main_table, main_alias, lookup_table, lookup_alias);
        String expectedQuery = "\"SELECT\n"
                + "main_table.\\\"id\\\", main_table.\\\"name\\\", main_table.\\\"age\\\", lookup_table.\\\"score\\\"\n"
                + "FROM\n" + " main_table , lookup_table\"";
        String query = manager.buildSqlSelect(dbMapComponent, "grade");
        assertEquals(expectedQuery, query);
    }

    @Test
    public void testBuildSqlSelectWithSnowflakeUseDelimitedIdentifiers() {
        manager.setUseDelimitedIdentifiers(true);
        String schema = "";
        String main_table = "main_table";
        String main_alias = "";
        String lookup_table = "lookup_table";
        String lookup_alias = "";
        init(schema, main_table, main_alias, lookup_table, lookup_alias);

        ElementParameter param = new ElementParameter(dbMapComponent);
        param.setFieldType(EParameterFieldType.MAPPING_TYPE);
        param.setName(EParameterName.MAPPING.getName());
        param.setValue("snowflake_id");
        ((List<IElementParameter>) dbMapComponent.getElementParameters()).add(param);

        String expectedQuery = "\"SELECT\n"
                + "\\\"main_table\\\".\\\"id\\\", \\\"main_table\\\".\\\"name\\\", \\\"main_table\\\".\\\"age\\\", \\\"lookup_table\\\".\\\"score\\\"\n"
                + "FROM\n"
                + " \" +\"\\\"main_table\\\"\"+ \" , \" +\"\\\"lookup_table\\\"\"";
        String query = manager.buildSqlSelect(dbMapComponent, "grade");
        assertEquals(expectedQuery, query);
    }

    @Test
    public void testBuildSqlSelectWithSnowflakeUseDelimitedIdentifiers4TableAlias() {
        manager.setUseDelimitedIdentifiers(true);
        String schema = "";
        String main_table = "main_table";
        String main_alias = "mt";
        String lookup_table = "lookup_table";
        String lookup_alias = "lt";
        init(schema, main_table, main_alias, lookup_table, lookup_alias);

        ElementParameter param = new ElementParameter(dbMapComponent);
        param.setFieldType(EParameterFieldType.MAPPING_TYPE);
        param.setName(EParameterName.MAPPING.getName());
        param.setValue("snowflake_id");
        ((List<IElementParameter>) dbMapComponent.getElementParameters()).add(param);

        String expectedQuery = "\"SELECT\n" + "mt.\\\"id\\\", mt.\\\"name\\\", mt.\\\"age\\\", lt.\\\"score\\\"\n" + "FROM\n"
                + " \" +\"\\\"main_table\\\"\"+ \" mt , \" +\"\\\"lookup_table\\\"\"+ \" lt\"";
        String query = manager.buildSqlSelect(dbMapComponent, "grade");
        assertEquals(expectedQuery, query);
    }

    @Override
    protected IConnection mockConnection(Node node, String schemaName, String tableName, String[] columns) {
        Connection connection = mock(Connection.class);
        if (node == null) {
            node = mock(Node.class);
        }
        ElementParameter param = new ElementParameter(node);
        param.setName("ELT_SCHEMA_NAME");
        param.setValue(schemaName);
        when(node.getElementParameter("ELT_SCHEMA_NAME")).thenReturn(param);
        param = new ElementParameter(node);
        param.setName("ELT_TABLE_NAME");
        param.setValue(tableName);
        when(node.getElementParameter("ELT_TABLE_NAME")).thenReturn(param);
        String tName = "".equals(schemaName) ? tableName : schemaName + "." + tableName;
        // quote will be removed in the ui for connections ,so we do the same for test
        tName = TalendTextUtils.removeQuotes(tName);
        when(connection.getName()).thenReturn(tName);
        when(connection.getSource()).thenReturn(node);
        when(connection.getMetaName()).thenReturn(tName);
        when(connection.getUniqueName()).thenReturn(tName);
        IMetadataTable table = new MetadataTable();
        table.setLabel(tableName);
        table.setTableName(tableName + "_t1"); //$NON-NLS-1$
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
}
