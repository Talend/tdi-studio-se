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
package org.talend.designer.core.ui.editor.properties.controllers;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.powermock.reflect.Whitebox;
import org.talend.core.model.components.ComponentCategory;
import org.talend.core.model.components.IComponent;
import org.talend.core.model.process.EParameterFieldType;
import org.talend.core.model.process.Element;
import org.talend.core.model.process.IElementParameter;
import org.talend.core.model.properties.PropertiesFactory;
import org.talend.core.model.properties.Property;
import org.talend.core.ui.component.ComponentsFactoryProvider;
import org.talend.core.ui.properties.tab.IDynamicProperty;
import org.talend.designer.core.model.FakeElement;
import org.talend.designer.core.model.components.EParameterName;
import org.talend.designer.core.model.components.ElementParameter;
import org.talend.designer.core.model.process.DataNode;
import org.talend.designer.core.ui.editor.nodes.Node;
import org.talend.designer.core.ui.editor.process.Process;

/**
 * created by hcyi on Aug 1, 2019
 * Detailled comment
 *
 */
public class SqlMemoControllerTest {

    private Process process;

    @Before
    public void setUp() throws Exception {
        Property property = PropertiesFactory.eINSTANCE.createProperty();
        process = new Process(property);
    }

    @Test
    public void testVisibleOpenSQLEditorButton4Mysql() throws Exception {
        SqlMemoController instance = Whitebox.newInstance(SqlMemoController.class);
        IComponent component = ComponentsFactoryProvider.getInstance().get("tMysqlInput",
                ComponentCategory.CATEGORY_4_DI.getName());
        Node elem = new Node(component, process);
        DataNode element = new DataNode(component, "tMysqlInput_1");
        List<IElementParameter> listParams = new ArrayList<IElementParameter>();
        ElementParameter param = new ElementParameter(element);
        param.setFieldType(EParameterFieldType.TEXT);
        param.setValue("Mysql");
        param.setRepositoryValue("TYPE");
        param.setName("TYPE");
        listParams.add(param);
        element.setElementParameters(listParams);
        instance.setElem(elem);

        assertTrue(instance.visibleOpenSQLEditorButton());
    }

    @Test
    public void testVisibleOpenSQLEditorButton4Hive() throws Exception {
        SqlMemoController instance = Whitebox.newInstance(SqlMemoController.class);
        IComponent component = ComponentsFactoryProvider.getInstance().get("tHiveInput",
                ComponentCategory.CATEGORY_4_DI.getName());
        Node elem = new Node(component, process);
        DataNode element = new DataNode(component, "tHiveInput_1");
        List<IElementParameter> listParams = new ArrayList<IElementParameter>();
        ElementParameter param = new ElementParameter(element);
        param.setFieldType(EParameterFieldType.TEXT);
        param.setValue("Hive");
        param.setRepositoryValue("TYPE");
        param.setName("TYPE");
        listParams.add(param);
        element.setElementParameters(listParams);
        instance.setElem(elem);

        assertFalse(instance.visibleOpenSQLEditorButton());
    }

    @Test
    public void testVisibleOpenSQLEditorButton4Impala() throws Exception {
        SqlMemoController instance = Whitebox.newInstance(SqlMemoController.class);
        IComponent component = ComponentsFactoryProvider.getInstance().get("tImpalaInput",
                ComponentCategory.CATEGORY_4_DI.getName());
        Node elem = new Node(component, process);
        DataNode element = new DataNode(component, "tImpalaInput_1");
        List<IElementParameter> listParams = new ArrayList<IElementParameter>();
        ElementParameter param = new ElementParameter(element);
        param.setFieldType(EParameterFieldType.TEXT);
        param.setValue("Impala");
        param.setRepositoryValue("TYPE");
        param.setName("TYPE");
        listParams.add(param);
        element.setElementParameters(listParams);
        instance.setElem(elem);

        assertFalse(instance.visibleOpenSQLEditorButton());
    }

    @Test
    public void testVisibleOpenSQLEditorButton4TacokitInWizard() throws Exception {
        SqlMemoController instance = Whitebox.newInstance(SqlMemoController.class);
        IDynamicProperty dynamicProperty = mock(IDynamicProperty.class);

        Element elem = new FakeElement("testTacokitQueryInWizard");
        ElementParameter param = new ElementParameter(elem);
        param.setFieldType(EParameterFieldType.TEXT);
        param.setName("TYPE");
        param.setTaggedValue("org.talend.sdk.component.source", "tacokit");

        instance.setElem(elem);
        instance.curParameter = param;
        instance.dynamicProperty = dynamicProperty;
        when(dynamicProperty.getElement()).thenReturn(elem);

        assertFalse(instance.visibleOpenSQLEditorButton());
    }

    @Test
    public void testVisibleOpenSQLEditorButton4TacokitComponents() throws Exception {
        SqlMemoController instance = Whitebox.newInstance(SqlMemoController.class);
        // Here use mysql since no tacomkit component query now
        IComponent component = ComponentsFactoryProvider.getInstance().get("tMysqlInput",
                ComponentCategory.CATEGORY_4_DI.getName());
        Node elem = new Node(component, process);
        DataNode element = new DataNode(component, "tMysqlInput_1");
        List<IElementParameter> listParams = new ArrayList<IElementParameter>();
        ElementParameter param = new ElementParameter(element);
        param.setFieldType(EParameterFieldType.TEXT);
        param.setValue("jdbc");
        param.setRepositoryValue(EParameterName.FAMILY.getName());
        param.setName(EParameterName.FAMILY.getName());
        param.setTaggedValue("org.talend.sdk.component.source", "tacokit");
        listParams.add(param);
        element.setElementParameters(listParams);
        instance.setElem(elem);

        elem.setPropertyValue(EParameterName.FAMILY.getName(), "jdbc");
        instance.curParameter = param;

        assertFalse(instance.visibleOpenSQLEditorButton());
    }
}
