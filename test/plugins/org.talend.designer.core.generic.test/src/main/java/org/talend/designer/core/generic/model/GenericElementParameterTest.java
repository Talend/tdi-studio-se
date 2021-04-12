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
package org.talend.designer.core.generic.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Before;
import org.junit.Test;
import org.talend.components.api.service.ComponentService;
import org.talend.core.model.components.IComponent;
import org.talend.core.model.process.INode;
import org.talend.core.model.repository.FakePropertyImpl;
import org.talend.core.ui.component.ComponentsFactoryProvider;
import org.talend.daikon.properties.presentation.Form;
import org.talend.daikon.properties.property.Property;
import org.talend.designer.core.generic.utils.ComponentsUtils;
import org.talend.designer.core.generic.utils.TestProperties;
import org.talend.designer.core.ui.editor.nodes.Node;
import org.talend.designer.core.ui.editor.process.Process;

/**
 * created by ycbai on 2017年4月26日 Detailled comment
 *
 */
public class GenericElementParameterTest {

    private ComponentService componentService;

    private INode node;

    private GenericElementParameter parameter;

    @Before
    public void before() {
        componentService = ComponentsUtils.getComponentService();
        TestProperties props = (TestProperties) new TestProperties("test").init(); //$NON-NLS-1$
        Form form = props.getForm(Form.MAIN);
        IComponent component = ComponentsFactoryProvider.getInstance().get("tSalesforceConnection", "DI"); //$NON-NLS-1$ //$NON-NLS-2$
        node = new Node(component, new Process(new FakePropertyImpl()));
        node.setComponentProperties(props);
        parameter = new GenericElementParameter(node, props, form, form.getWidget(props.userId), componentService);
    }

    @Test
    public void testTaggedValues() {
        assertNull(parameter.getTaggedValue("foo")); //$NON-NLS-1$

        parameter.setTaggedValue("foo", "fooValue"); //$NON-NLS-1$ //$NON-NLS-2$
        assertEquals("fooValue", parameter.getTaggedValue("foo")); //$NON-NLS-1$//$NON-NLS-2$
        assertEquals("fooValue", parameter.getProperty().getTaggedValue("foo")); //$NON-NLS-1$//$NON-NLS-2$

        assertNull(parameter.getTaggedValue("bar")); //$NON-NLS-1$

        parameter.getProperty().setTaggedValue("bar", "barValue"); //$NON-NLS-1$//$NON-NLS-2$
        assertEquals("barValue", parameter.getProperty().getTaggedValue("bar")); //$NON-NLS-1$//$NON-NLS-2$
        assertEquals("barValue", parameter.getTaggedValue("bar")); //$NON-NLS-1$//$NON-NLS-2$
    }

    @Test
    public void testSetValue_updateProperty() {
        TestProperties nodeCompProperties = (TestProperties) node.getComponentProperties();
        Property testProperty = nodeCompProperties.userId;

        // isFirstCall is true, so wont update property value.
        parameter.setValue("id1");
        assertNull(testProperty.getValue());

        // isFirstCall is false, so will update property value.
        parameter.setValue("id1");
        assertEquals("id1", testProperty.getValue());

        // if property of widget is not the same instance of the one in component properties of node, will update the
        // latter too.
        TestProperties newProps = (TestProperties) new TestProperties("test2").init(); //$NON-NLS-1$
        Form newForm = newProps.getForm(Form.MAIN);
        parameter = new GenericElementParameter(node, nodeCompProperties, newForm, newForm.getWidget(newProps.userId),
                componentService);
        parameter.setValue(""); // set isFirstCall as false.
        parameter.setValue("id2");
        assertEquals("id2", testProperty.getValue());

        // the property of widget will update too.
        Property wProperty = parameter.getProperty();
        assertEquals("id2", wProperty.getValue());

        // check if the property of widget is update to date so that all properties can be updated correctly.
        parameter.setValue("id3");
        parameter.setValue("id2");
        assertEquals("id2", testProperty.getValue());
    }

}
