// ============================================================================
//
// Copyright (C) 2006-2017 Talend Inc. - www.talend.com
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

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.talend.components.api.service.ComponentService;
import org.talend.core.model.components.IComponent;
import org.talend.core.model.process.INode;
import org.talend.core.model.repository.FakePropertyImpl;
import org.talend.core.ui.component.ComponentsFactoryProvider;
import org.talend.daikon.properties.presentation.Form;
import org.talend.designer.core.generic.utils.ComponentsUtils;
import org.talend.designer.core.generic.utils.TestProperties;
import org.talend.designer.core.ui.editor.nodes.Node;
import org.talend.designer.core.ui.editor.process.Process;

/**
 * created by ycbai on 2017年4月26日 Detailled comment
 *
 */
public class GenericElementParameterTest {

    private GenericElementParameter parameter;

    @Before
    public void before() {
        ComponentService componentService = ComponentsUtils.getComponentService();
        TestProperties props = (TestProperties) new TestProperties("test").init(); //$NON-NLS-1$
        Form form = props.getForm(Form.MAIN);
        IComponent component = ComponentsFactoryProvider.getInstance().get("tSalesforceConnection", "DI"); //$NON-NLS-1$ //$NON-NLS-2$
        INode node = new Node(component, new Process(new FakePropertyImpl()));
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

}
