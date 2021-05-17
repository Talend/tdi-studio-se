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
package org.talend.designer.unifiedcomponent.controller;

import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.database.EDatabaseTypeName;
import org.talend.core.model.components.ComponentCategory;
import org.talend.core.model.components.IComponent;
import org.talend.core.model.components.IComponentsService;
import org.talend.core.model.metadata.IMetadataTable;
import org.talend.core.model.metadata.MetadataColumn;
import org.talend.core.model.process.EConnectionType;
import org.talend.core.model.process.EParameterFieldType;
import org.talend.core.model.process.IConnection;
import org.talend.core.model.process.IElementParameter;
import org.talend.core.model.properties.PropertiesFactory;
import org.talend.core.model.properties.Property;
import org.talend.designer.core.IUnifiedComponentService;
import org.talend.designer.core.model.components.EmfComponent;
import org.talend.designer.core.ui.editor.connections.Connection;
import org.talend.designer.core.ui.editor.nodes.Node;
import org.talend.designer.core.ui.editor.process.Process;
import org.talend.designer.unifiedcomponent.component.DelegateComponent;
import org.talend.designer.unifiedcomponent.component.UnifiedObject;

/**
 * created by wchen on Dec 15, 2017 Detailled comment
 *
 */
public class ChangeComponentCommandTest {

    private IComponentsService compService = (IComponentsService) GlobalServiceRegister.getDefault()
            .getService(IComponentsService.class);

    private IUnifiedComponentService unifiedCompservice = (IUnifiedComponentService) GlobalServiceRegister.getDefault()
            .getService(IUnifiedComponentService.class);

    public void testCommand() {
        IComponent tDBOutput = unifiedCompservice.getDelegateComponent("tDBOutput", ComponentCategory.CATEGORY_4_DI.getName());
        Property property1 = PropertiesFactory.eINSTANCE.createProperty();
        property1.setId("property1"); //$NON-NLS-1$
        property1.setVersion("0.1"); //$NON-NLS-1$
        property1.setLabel("test1");//$NON-NLS-1$
        Process process1 = new Process(property1);
        Node node = new Node(tDBOutput, process1);

        Assert.assertNull(node.getElementParameter("HOST"));
        Assert.assertNull(node.getElementParameter("DBNAME"));
        Assert.assertNull(node.getElementParameter("USER"));
        Assert.assertNull(node.getElementParameter("PASS"));

        IElementParameter unifiedParam = node.getElementParameterFromField(EParameterFieldType.UNIFIED_COMPONENTS);
        ChangeComponentCommand command = new ChangeComponentCommand(node, unifiedParam,
                EDatabaseTypeName.TERADATA.getDisplayName());
        command.execute();

        Assert.assertNotNull(node.getElementParameter("HOST"));
        Assert.assertNotNull(node.getElementParameter("DBNAME"));
        Assert.assertNotNull(node.getElementParameter("USER"));
        Assert.assertNotNull(node.getElementParameter("PASS"));

    }

    @Test
    public void testCommand1() {
        IComponent tMysqlInput = compService.getComponentsFactory().get("tMysqlInput", ComponentCategory.CATEGORY_4_DI.getName());
        Property property1 = PropertiesFactory.eINSTANCE.createProperty();
        property1.setId("property1"); //$NON-NLS-1$
        property1.setVersion("0.1"); //$NON-NLS-1$
        property1.setLabel("test1");//$NON-NLS-1$
        Process process1 = new Process(property1);
        Node node = new Node(tMysqlInput, process1);
        String uniqueName = node.getUniqueName();
        node.getElementParameter("HOST").setValue("192.168.30.10");
        node.getElementParameter("DBNAME").setValue("test");
        node.getElementParameter("USER").setValue("my_user");
        node.getElementParameter("PASS").setValue("my_password");

        IElementParameter unifiedParam = node.getElementParameterFromField(EParameterFieldType.UNIFIED_COMPONENTS);

        ChangeComponentCommand command = new ChangeComponentCommand(node, unifiedParam, "Oracle");
        command.execute();

        Assert.assertEquals("tOracleInput", node.getComponent().getName());
        Assert.assertEquals(uniqueName, node.getUniqueName());
        Assert.assertEquals(node.getElementParameter("HOST").getValue(), "192.168.30.10");
        Assert.assertEquals(node.getElementParameter("DBNAME").getValue(), "test");
        Assert.assertEquals(node.getElementParameter("USER").getValue(), "my_user");
        Assert.assertEquals(node.getElementParameter("PASS").getValue(), "my_password");
        Assert.assertEquals(node.getElementParameter("PORT").getValue(), "\"1521\"");

        command.undo();

        Assert.assertEquals("tMysqlInput", node.getComponent().getName());
        Assert.assertEquals(uniqueName, node.getUniqueName());
        Assert.assertEquals(node.getElementParameter("HOST").getValue(), "192.168.30.10");
        Assert.assertEquals(node.getElementParameter("DBNAME").getValue(), "test");
        Assert.assertEquals(node.getElementParameter("USER").getValue(), "my_user");
        Assert.assertEquals(node.getElementParameter("PASS").getValue(), "my_password");
        Assert.assertEquals(node.getElementParameter("PORT").getValue(), "\"3306\"");

    }

    @Test
    public void testCommand2() {
        IComponent tAS400Connection = compService.getComponentsFactory().get("tAS400Connection",
                ComponentCategory.CATEGORY_4_DI.getName());
        Property property1 = PropertiesFactory.eINSTANCE.createProperty();
        property1.setId("property1"); //$NON-NLS-1$
        property1.setVersion("0.1"); //$NON-NLS-1$
        property1.setLabel("test1");//$NON-NLS-1$
        Process process1 = new Process(property1);
        Node node = new Node(tAS400Connection, process1);
        node.getElementParameter("HOST").setValue("192.168.30.10");
        node.getElementParameter("DBNAME").setValue("test");
        node.getElementParameter("USER").setValue("my_user");
        node.getElementParameter("PASS").setValue("my_password");
        IElementParameter elementParameter = node.getElementParameterFromField(EParameterFieldType.PROPERTY_TYPE);
        elementParameter.getChildParameters().get("PROPERTY_TYPE").setValue(EmfComponent.REPOSITORY);
        elementParameter.getChildParameters().get("REPOSITORY_PROPERTY_TYPE").setValue("_zchgQdyyEeePQYjilYBwZg");

        IElementParameter unifiedParam = node.getElementParameterFromField(EParameterFieldType.UNIFIED_COMPONENTS);

        ChangeComponentCommand command = new ChangeComponentCommand(node, unifiedParam,
                EDatabaseTypeName.VERTICA.getDisplayName());
        command.execute();

        Assert.assertEquals("tVerticaConnection", node.getComponent().getName());
        Assert.assertEquals(node.getElementParameter("HOST").getValue(), "192.168.30.10");
        Assert.assertEquals(node.getElementParameter("DBNAME").getValue(), "test");
        Assert.assertEquals(node.getElementParameter("USER").getValue(), "my_user");
        Assert.assertEquals(node.getElementParameter("PASS").getValue(), "my_password");
        elementParameter = node.getElementParameterFromField(EParameterFieldType.PROPERTY_TYPE);
        Assert.assertEquals(EmfComponent.BUILTIN, elementParameter.getChildParameters().get("PROPERTY_TYPE").getValue());

        command.undo();

        Assert.assertEquals("tAS400Connection", node.getComponent().getName());
        Assert.assertEquals(node.getElementParameter("HOST").getValue(), "192.168.30.10");
        Assert.assertEquals(node.getElementParameter("DBNAME").getValue(), "test");
        Assert.assertEquals(node.getElementParameter("USER").getValue(), "my_user");
        Assert.assertEquals(node.getElementParameter("PASS").getValue(), "my_password");
        elementParameter = node.getElementParameterFromField(EParameterFieldType.PROPERTY_TYPE);
        Assert.assertEquals(EmfComponent.REPOSITORY, elementParameter.getChildParameters().get("PROPERTY_TYPE").getValue());
        Assert.assertEquals("_zchgQdyyEeePQYjilYBwZg",
                elementParameter.getChildParameters().get("REPOSITORY_PROPERTY_TYPE").getValue());
    }

    @Test
    public void testMysqlAndFlowflakSchema() {
        IComponent tMysqlInput = compService.getComponentsFactory().get("tMysqlInput", ComponentCategory.CATEGORY_4_DI.getName());
        IComponent tLogRow = compService.getComponentsFactory().get("tLogRow", ComponentCategory.CATEGORY_4_DI.getName());
        Property property1 = PropertiesFactory.eINSTANCE.createProperty();
        property1.setId("property1"); //$NON-NLS-1$
        property1.setVersion("0.1"); //$NON-NLS-1$
        property1.setLabel("test1");//$NON-NLS-1$
        Process process1 = new Process(property1);
        Node node = new Node(tMysqlInput, process1);
        IMetadataTable metadataTable = node.getMetadataFromConnector("FLOW");
        MetadataColumn column = new MetadataColumn();
        column.setLabel("column1");
        column.setTalendType("id_String");
        metadataTable.getListColumns().add(column);
        Node logRowNode = new Node(tLogRow, process1);
        logRowNode.getMetadataList().get(0).getListColumns().add(column.clone());

        Connection connection = new Connection(node, logRowNode, EConnectionType.FLOW_MAIN, EConnectionType.FLOW_MAIN.getName(),
                metadataTable.getTableName(), "row1", metadataTable.getTableName(), false);
        connection.getTargetNodeConnector().setCurLinkNbInput(1);
        connection.getSourceNodeConnector().setCurLinkNbOutput(1);

        IElementParameter unifiedParam = node.getElementParameterFromField(EParameterFieldType.UNIFIED_COMPONENTS);
        ChangeComponentCommand command = new ChangeComponentCommand(node, unifiedParam, "Snowflake");
        command.execute();

        IMetadataTable flowTable = node.getMetadataFromConnector("FLOW");
        Assert.assertNull(flowTable);

        IMetadataTable mainTable = node.getMetadataFromConnector("MAIN");
        Assert.assertNotNull(mainTable);
        Assert.assertTrue(mainTable.sameMetadataAs(metadataTable));
        IConnection newConnection = node.getOutgoingConnections().get(0);
        Assert.assertEquals(newConnection.getConnectorName(), "MAIN");
        Assert.assertEquals(newConnection.getMetaName(), mainTable.getTableName());

        unifiedParam = node.getElementParameterFromField(EParameterFieldType.UNIFIED_COMPONENTS);
        command = new ChangeComponentCommand(node, unifiedParam, "MySQL");
        command.execute();

        flowTable = node.getMetadataFromConnector("FLOW");
        Assert.assertNotNull(flowTable);
        Assert.assertTrue(flowTable.sameMetadataAs(metadataTable));

        mainTable = node.getMetadataFromConnector("MAIN");
        Assert.assertNull(mainTable);

        newConnection = node.getOutgoingConnections().get(0);
        Assert.assertEquals(newConnection.getConnectorName(), "FLOW");
        Assert.assertEquals(newConnection.getMetaName(), flowTable.getTableName());

    }

    @Test
    public void testTUP_19802ForSchema() {
        IComponent tMysqlRow = compService.getComponentsFactory().get("tMysqlRow", ComponentCategory.CATEGORY_4_DI.getName());
        Property property1 = PropertiesFactory.eINSTANCE.createProperty();
        property1.setId("property1"); //$NON-NLS-1$
        property1.setVersion("0.1"); //$NON-NLS-1$
        property1.setLabel("test1");//$NON-NLS-1$
        Process process1 = new Process(property1);
        Node node = new Node(tMysqlRow, process1);
        IMetadataTable schema = node.getMetadataFromConnector("FLOW");
        IMetadataTable reject = node.getMetadataFromConnector("REJECT");

        IElementParameter unifiedParam = node.getElementParameterFromField(EParameterFieldType.UNIFIED_COMPONENTS);
        ChangeComponentCommand command = new ChangeComponentCommand(node, unifiedParam, "MySQL");
        command.execute();

        IMetadataTable schemaNew = node.getMetadataFromConnector("FLOW");
        IMetadataTable rejectNew = node.getMetadataFromConnector("REJECT");

        Assert.assertTrue(schema.sameMetadataAs(schemaNew));
        Assert.assertTrue(reject.getListColumns().size() == 2);
        Assert.assertTrue(reject.sameMetadataAs(rejectNew));

        MetadataColumn column = new MetadataColumn();
        column.setLabel("column1");
        column.setTalendType("id_String");
        schemaNew.getListColumns().add(column);
        rejectNew.getListColumns().add(0, column.clone());

        unifiedParam = node.getElementParameterFromField(EParameterFieldType.UNIFIED_COMPONENTS);
        command = new ChangeComponentCommand(node, unifiedParam, "MySQL");
        command.execute();

        IMetadataTable schemaNew1 = node.getMetadataFromConnector("FLOW");
        IMetadataTable rejectNew1 = node.getMetadataFromConnector("REJECT");

        Assert.assertTrue(schemaNew1.sameMetadataAs(schemaNew));
        Assert.assertTrue(rejectNew1.getListColumns().size() == 3);
        Assert.assertTrue(rejectNew1.sameMetadataAs(rejectNew));

    }

    @Test
    public void testChangeComponentBetweentcomp() {
        IComponent tJDBCInout = compService.getComponentsFactory().get("tJDBCInput", ComponentCategory.CATEGORY_4_DI.getName());
        IComponent tSnowflake = compService.getComponentsFactory().get("tSnowflakeInput",
                ComponentCategory.CATEGORY_4_DI.getName());
        if (tJDBCInout == null || tSnowflake == null) {
            fail("Can not load test components");
        }
        Property property1 = PropertiesFactory.eINSTANCE.createProperty();
        property1.setId("property1"); //$NON-NLS-1$
        property1.setVersion("0.1"); //$NON-NLS-1$
        property1.setLabel("test1");//$NON-NLS-1$
        Process process1 = new Process(property1);
        Node node = new Node(tJDBCInout, process1);

        IElementParameter jdbcParam = node.getElementParameter("connection.driverTable");
        IElementParameter snowflakeParam = node.getElementParameter("connection.account");
        Assert.assertNotNull(jdbcParam);
        Assert.assertNull(snowflakeParam);

        IElementParameter unifiedParam = node.getElementParameterFromField(EParameterFieldType.UNIFIED_COMPONENTS);
        ChangeComponentCommand command = new ChangeComponentCommand(node, unifiedParam, "Snowflake");
        command.execute();

        jdbcParam = node.getElementParameter("connection.driverTable");
        snowflakeParam = node.getElementParameter("connection.account");
        Assert.assertNull(jdbcParam);
        Assert.assertNotNull(snowflakeParam);

        unifiedParam = node.getElementParameterFromField(EParameterFieldType.UNIFIED_COMPONENTS);
        command = new ChangeComponentCommand(node, unifiedParam, "JDBC");
        command.execute();

        jdbcParam = node.getElementParameter("connection.driverTable");
        snowflakeParam = node.getElementParameter("connection.account");
        Assert.assertNotNull(jdbcParam);
        Assert.assertNull(snowflakeParam);

    }

    // Add junit test for TUP-20612
    @Test
    public void testChangeComponentDBInputMappingType() {
        IComponent tAS400Input = compService.getComponentsFactory().get("tAS400Input", ComponentCategory.CATEGORY_4_DI.getName());
        IComponent tAccessInput = compService.getComponentsFactory().get("tAccessInput",
                ComponentCategory.CATEGORY_4_DI.getName());
        IComponent tAmazonAuroraInput = compService.getComponentsFactory().get("tAmazonAuroraInput",
                ComponentCategory.CATEGORY_4_DI.getName());
        IComponent tAmazonMysqlInput = compService.getComponentsFactory().get("tAmazonMysqlInput",
                ComponentCategory.CATEGORY_4_DI.getName());
        IComponent tAmazonOracleInput = compService.getComponentsFactory().get("tAmazonOracleInput",
                ComponentCategory.CATEGORY_4_DI.getName());
        IComponent tRedshiftInput = compService.getComponentsFactory().get("tRedshiftInput",
                ComponentCategory.CATEGORY_4_DI.getName());
        IComponent tExasolInput = compService.getComponentsFactory().get("tExasolInput",
                ComponentCategory.CATEGORY_4_DI.getName());
        IComponent tFirebirdInput = compService.getComponentsFactory().get("tFirebirdInput",
                ComponentCategory.CATEGORY_4_DI.getName());
        IComponent tGreenplumInput = compService.getComponentsFactory().get("tGreenplumInput",
                ComponentCategory.CATEGORY_4_DI.getName());
        IComponent tDB2Input = compService.getComponentsFactory().get("tDB2Input", ComponentCategory.CATEGORY_4_DI.getName());
        IComponent tInformixInput = compService.getComponentsFactory().get("tInformixInput",
                ComponentCategory.CATEGORY_4_DI.getName());
        IComponent tIngresInput = compService.getComponentsFactory().get("tIngresInput",
                ComponentCategory.CATEGORY_4_DI.getName());
        IComponent tInterbaseInput = compService.getComponentsFactory().get("tInterbaseInput",
                ComponentCategory.CATEGORY_4_DI.getName());
        IComponent tMSSqlInput = compService.getComponentsFactory().get("tMSSqlInput", ComponentCategory.CATEGORY_4_DI.getName());
        IComponent tMysqlInput = compService.getComponentsFactory().get("tMysqlInput", ComponentCategory.CATEGORY_4_DI.getName());
        IComponent tNetezzaInput = compService.getComponentsFactory().get("tNetezzaInput",
                ComponentCategory.CATEGORY_4_DI.getName());
        IComponent tOracleInput = compService.getComponentsFactory().get("tOracleInput",
                ComponentCategory.CATEGORY_4_DI.getName());
        IComponent tParAccelInput = compService.getComponentsFactory().get("tParAccelInput",
                ComponentCategory.CATEGORY_4_DI.getName());
        IComponent tPostgresqlInput = compService.getComponentsFactory().get("tPostgresqlInput",
                ComponentCategory.CATEGORY_4_DI.getName());
        IComponent tPostgresPlusInput = compService.getComponentsFactory().get("tPostgresPlusInput",
                ComponentCategory.CATEGORY_4_DI.getName());
        IComponent tSAPHanaInput = compService.getComponentsFactory().get("tSAPHanaInput",
                ComponentCategory.CATEGORY_4_DI.getName());
        IComponent tSQLiteInput = compService.getComponentsFactory().get("tSQLiteInput",
                ComponentCategory.CATEGORY_4_DI.getName());
        IComponent tSybaseInput = compService.getComponentsFactory().get("tSybaseInput",
                ComponentCategory.CATEGORY_4_DI.getName());
        IComponent tTeradataInput = compService.getComponentsFactory().get("tTeradataInput",
                ComponentCategory.CATEGORY_4_DI.getName());
        IComponent tVectorWiseInput = compService.getComponentsFactory().get("tVectorWiseInput",
                ComponentCategory.CATEGORY_4_DI.getName());
        IComponent tVerticaInput = compService.getComponentsFactory().get("tVerticaInput",
                ComponentCategory.CATEGORY_4_DI.getName());

        Property property1 = PropertiesFactory.eINSTANCE.createProperty();
        property1.setId("property1"); //$NON-NLS-1$
        property1.setVersion("0.1"); //$NON-NLS-1$
        property1.setLabel("test1");//$NON-NLS-1$
        Process process1 = new Process(property1);

        List<IComponent> tDBInputs = new ArrayList<IComponent>();
        tDBInputs.add(tAS400Input);
        tDBInputs.add(tAccessInput);
        tDBInputs.add(tAmazonAuroraInput);
        tDBInputs.add(tAmazonMysqlInput);
        tDBInputs.add(tAmazonOracleInput);
        tDBInputs.add(tRedshiftInput);
        tDBInputs.add(tExasolInput);
        tDBInputs.add(tFirebirdInput);
        tDBInputs.add(tGreenplumInput);
        tDBInputs.add(tDB2Input);
        tDBInputs.add(tInformixInput);
        tDBInputs.add(tIngresInput);
        tDBInputs.add(tInterbaseInput);
        tDBInputs.add(tMSSqlInput);
        tDBInputs.add(tMysqlInput);
        tDBInputs.add(tNetezzaInput);
        tDBInputs.add(tOracleInput);
        tDBInputs.add(tParAccelInput);
        tDBInputs.add(tPostgresqlInput);
        tDBInputs.add(tPostgresPlusInput);
        tDBInputs.add(tSAPHanaInput);
        tDBInputs.add(tSQLiteInput);
        tDBInputs.add(tSybaseInput);
        tDBInputs.add(tTeradataInput);
        tDBInputs.add(tVectorWiseInput);
        tDBInputs.add(tVerticaInput);

        for (IComponent component : tDBInputs) {
            Assert.assertNotNull(component);
            Node node = new Node(component, process1);
            DelegateComponent delegateComponent = (DelegateComponent) node.getDelegateComponent();
            Assert.assertNotNull(delegateComponent);
            IElementParameter newUnifiedParam = node.getElementParameterFromField(EParameterFieldType.UNIFIED_COMPONENTS);
            Assert.assertNotNull(newUnifiedParam);
            String unifiedComp = String.valueOf(newUnifiedParam.getValue());
            Assert.assertNotNull(unifiedComp);
            UnifiedObject unifiedObject = delegateComponent.getUnifiedObjectByName(unifiedComp);
            Assert.assertNotNull(unifiedObject);
            Set<String> mappingExelude = new HashSet<String>();
            Assert.assertTrue(mappingExelude.isEmpty());
            mappingExelude.addAll(unifiedObject.getParamMappingExclude());
            Assert.assertTrue(mappingExelude.contains("TYPE"));
            mappingExelude.clear();
        }
    }
}
