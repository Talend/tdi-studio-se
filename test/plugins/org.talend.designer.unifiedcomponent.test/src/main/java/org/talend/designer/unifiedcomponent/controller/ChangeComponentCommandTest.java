// ============================================================================
//
// Copyright (C) 2006-2018 Talend Inc. - www.talend.com
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

import org.junit.Assert;
import org.junit.Test;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.database.EDatabaseTypeName;
import org.talend.core.model.components.ComponentCategory;
import org.talend.core.model.components.IComponent;
import org.talend.core.model.components.IComponentsService;
import org.talend.core.model.metadata.IMetadataTable;
import org.talend.core.model.metadata.MetadataColumn;
import org.talend.core.model.process.EParameterFieldType;
import org.talend.core.model.process.IElementParameter;
import org.talend.core.model.properties.PropertiesFactory;
import org.talend.core.model.properties.Property;
import org.talend.designer.core.IUnifiedComponentService;
import org.talend.designer.core.model.components.EmfComponent;
import org.talend.designer.core.ui.editor.nodes.Node;
import org.talend.designer.core.ui.editor.process.Process;

/**
 * created by wchen on Dec 15, 2017 Detailled comment
 *
 */
public class ChangeComponentCommandTest {

    private IComponentsService compService = (IComponentsService) GlobalServiceRegister.getDefault().getService(
            IComponentsService.class);

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
        Assert.assertEquals("_zchgQdyyEeePQYjilYBwZg", elementParameter.getChildParameters().get("REPOSITORY_PROPERTY_TYPE")
                .getValue());
    }

    @Test
    public void testMysqlAndFlowflakSchema() {
        IComponent tMysqlInput = compService.getComponentsFactory().get("tMysqlInput", ComponentCategory.CATEGORY_4_DI.getName());
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

        IElementParameter unifiedParam = node.getElementParameterFromField(EParameterFieldType.UNIFIED_COMPONENTS);
        ChangeComponentCommand command = new ChangeComponentCommand(node, unifiedParam, "Snowflake");
        command.execute();

        IMetadataTable flowTable = node.getMetadataFromConnector("FLOW");
        Assert.assertNull(flowTable);

        IMetadataTable mainTable = node.getMetadataFromConnector("MAIN");
        Assert.assertNotNull(mainTable);
        Assert.assertEquals(mainTable.getListColumns().get(0).getLabel(), "column1");

        unifiedParam = node.getElementParameterFromField(EParameterFieldType.UNIFIED_COMPONENTS);
        command = new ChangeComponentCommand(node, unifiedParam, "MySQL");
        command.execute();

        flowTable = node.getMetadataFromConnector("FLOW");
        Assert.assertNotNull(flowTable);

        mainTable = node.getMetadataFromConnector("MAIN");
        Assert.assertNull(mainTable);

    }

}
