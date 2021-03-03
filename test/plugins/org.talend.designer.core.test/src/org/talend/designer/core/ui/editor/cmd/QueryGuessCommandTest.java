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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.eclipse.core.runtime.Path;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.talend.commons.exception.PersistenceException;
import org.talend.commons.utils.VersionUtils;
import org.talend.core.context.Context;
import org.talend.core.context.RepositoryContext;
import org.talend.core.database.EDatabaseTypeName;
import org.talend.core.model.metadata.IMetadataTable;
import org.talend.core.model.metadata.builder.ConvertionHelper;
import org.talend.core.model.metadata.builder.connection.Connection;
import org.talend.core.model.metadata.builder.connection.ConnectionFactory;
import org.talend.core.model.metadata.builder.connection.DatabaseConnection;
import org.talend.core.model.process.EParameterFieldType;
import org.talend.core.model.process.INode;
import org.talend.core.model.properties.ConnectionItem;
import org.talend.core.model.properties.PropertiesFactory;
import org.talend.core.model.properties.Property;
import org.talend.core.model.repository.IRepositoryViewObject;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.core.runtime.CoreRuntimePlugin;
import org.talend.cwm.relational.RelationalFactory;
import org.talend.cwm.relational.TdColumn;
import org.talend.cwm.relational.TdTable;
import org.talend.designer.core.model.components.EParameterName;
import org.talend.designer.core.model.components.ElementParameter;

/**
 * DOC PLV class global comment. Detailled comment
 */
public class QueryGuessCommandTest {

    private IMetadataTable metadataTable;

    private Connection connection;

    private INode node;

    @Test
    public void generateNewQuery() throws NoSuchMethodException, SecurityException, IllegalAccessException,
            IllegalArgumentException, InvocationTargetException {
        connection = ConnectionFactory.eINSTANCE.createConnection();
        node = Mockito.mock(INode.class);
        connection.setContextMode(true);
        TdTable table = RelationalFactory.eINSTANCE.createTdTable();
        table.setName("tableName");
        table.setLabel("tableLabel");
        TdColumn column1 = RelationalFactory.eINSTANCE.createTdColumn();
        column1.setId("ID1");
        column1.setName("cName1");
        TdColumn column2 = RelationalFactory.eINSTANCE.createTdColumn();
        column2.setId("ID2");
        column2.setName("cName2");
        table.getColumns().add(column1);
        table.getColumns().add(column2);
        metadataTable = ConvertionHelper.convert(table);
        ElementParameter parameter = new ElementParameter(node);
        parameter.setName("DBTABLE");
        parameter.setValue(table.getName());
        Mockito.when(node.getPropertyValue(EParameterName.PROPERTY_TYPE.getName())).thenReturn("REPOSITORY");
        Mockito.when(node.getElementParameterFromField(EParameterFieldType.DBTABLE)).thenReturn(parameter);
        String schema = "schema";
        // add test for PostgreSQL
        String dbType = EDatabaseTypeName.PSQL.getDisplayName();
        QueryGuessCommand command = new QueryGuessCommand(node, metadataTable, schema, dbType, connection);
        Method method = command.getClass().getDeclaredMethod("generateNewQuery");
        method.setAccessible(true);
        String queryString = (String) method.invoke(command);
        Assert.assertNotNull(queryString);
        Assert.assertTrue(queryString.contains(schema));
        Assert.assertTrue(queryString.contains(metadataTable.getTableName()));
        Assert.assertTrue(queryString.contains("cName1"));
        Assert.assertTrue(queryString.contains("cName2"));

        // add test for MySQL
        dbType = EDatabaseTypeName.MYSQL.getDisplayName();
        command = new QueryGuessCommand(node, metadataTable, schema, dbType, connection);
        method = command.getClass().getDeclaredMethod("generateNewQuery");
        method.setAccessible(true);
        queryString = (String) method.invoke(command);
        Assert.assertNotNull(queryString);
        Assert.assertFalse(queryString.contains(schema));
        Assert.assertTrue(queryString.contains(metadataTable.getTableName()));
        Assert.assertTrue(queryString.contains("cName1"));
        Assert.assertTrue(queryString.contains("cName2"));
    }

    @Test
    public void generateNewTeradataQuery() throws NoSuchMethodException, SecurityException, IllegalAccessException,
            IllegalArgumentException, InvocationTargetException, PersistenceException {
        connection = ConnectionFactory.eINSTANCE.createConnection();
        node = Mockito.mock(INode.class);
        connection.setContextMode(true);
        TdTable table = RelationalFactory.eINSTANCE.createTdTable();
        table.setName("tableName");
        table.setLabel("tableLabel");
        TdColumn column1 = RelationalFactory.eINSTANCE.createTdColumn();
        column1.setName("id");
        TdColumn column2 = RelationalFactory.eINSTANCE.createTdColumn();
        column2.setName("name");
        table.getColumns().add(column1);
        table.getColumns().add(column2);
        metadataTable = ConvertionHelper.convert(table);
        ElementParameter parameter = new ElementParameter(node);
        parameter.setName("DBTABLE");
        parameter.setValue(table.getName());
        Mockito.when(node.getPropertyValue(EParameterName.PROPERTY_TYPE.getName())).thenReturn("REPOSITORY");
        Mockito.when(node.getElementParameterFromField(EParameterFieldType.DBTABLE)).thenReturn(parameter);
        // test case 1
        String schema = "myschema";
        String dbType = EDatabaseTypeName.TERADATA.getDisplayName();

        String expectedQuery = "\"SELECT myschema.tableName.\\\"id\\\", myschema.tableName.name FROM myschema.tableName\"";
        QueryGuessCommand command = new QueryGuessCommand(node, metadataTable, schema, dbType, connection);
        Method method = command.getClass().getDeclaredMethod("generateNewQuery");
        method.setAccessible(true);
        String queryString = (String) method.invoke(command);
        Assert.assertEquals(expectedQuery, queryString);
    }

    @Test
    public void generateNewTeradataQuery2() throws NoSuchMethodException, SecurityException, IllegalAccessException,
            IllegalArgumentException, InvocationTargetException, PersistenceException {
        ProxyRepositoryFactory repFactory = ProxyRepositoryFactory.getInstance();
        String propertyId = repFactory.getNextId();
        try {
            DatabaseConnection connection = ConnectionFactory.eINSTANCE.createDatabaseConnection();
            connection.setSID("myschema");
            Property connectionProperty = PropertiesFactory.eINSTANCE.createProperty();
            connectionProperty.setAuthor(((RepositoryContext) CoreRuntimePlugin.getInstance().getContext()
                    .getProperty(Context.REPOSITORY_CONTEXT_KEY)).getUser());
            connectionProperty.setVersion(VersionUtils.DEFAULT_VERSION);
            connectionProperty.setStatusCode(""); //$NON-NLS-1$
            connectionProperty.setId(propertyId);
            connectionProperty.setLabel("test_connection");

            ConnectionItem connectionItem = PropertiesFactory.eINSTANCE.createDatabaseConnectionItem();
            connectionItem.setProperty(connectionProperty);
            connectionItem.setConnection(connection);
            repFactory.create(connectionItem, new Path(""));

            node = Mockito.mock(INode.class);
            connection.setContextMode(true);
            TdTable table = RelationalFactory.eINSTANCE.createTdTable();
            table.setName("tableName");
            table.setLabel("tableLabel");
            TdColumn column1 = RelationalFactory.eINSTANCE.createTdColumn();
            column1.setName("id");
            TdColumn column2 = RelationalFactory.eINSTANCE.createTdColumn();
            column2.setName("name");
            table.getColumns().add(column1);
            table.getColumns().add(column2);
            metadataTable = ConvertionHelper.convert(table);
            ElementParameter parameter = new ElementParameter(node);
            parameter.setName("DBTABLE");
            parameter.setValue(table.getName());
            Mockito.when(node.getPropertyValue(EParameterName.PROPERTY_TYPE.getName())).thenReturn("REPOSITORY");
            Mockito.when(node.getElementParameterFromField(EParameterFieldType.DBTABLE)).thenReturn(parameter);
            ElementParameter connectionId = new ElementParameter(node);
            connectionId.setValue(connectionProperty.getId());
            Mockito.when(node.getElementParameter(EParameterName.REPOSITORY_PROPERTY_TYPE.getName())).thenReturn(connectionId);
            // test case 1
            String schema = "";
            String dbType = EDatabaseTypeName.TERADATA.getDisplayName();

            String expectedQuery = "\"SELECT myschema.tableName.\\\"id\\\", myschema.tableName.name FROM myschema.tableName\"";
            QueryGuessCommand command = new QueryGuessCommand(node, metadataTable, schema, dbType, connection);
            Method method = command.getClass().getDeclaredMethod("generateNewQuery");
            method.setAccessible(true);
            String queryString = (String) method.invoke(command);
            Assert.assertEquals(expectedQuery, queryString);
        } catch (Exception e) {
            throw e;
        } finally {
            IRepositoryViewObject lastVersion = repFactory.getLastVersion(propertyId);
            if (lastVersion != null) {
                repFactory.deleteObjectPhysical(lastVersion);
            }
        }

    }
}
