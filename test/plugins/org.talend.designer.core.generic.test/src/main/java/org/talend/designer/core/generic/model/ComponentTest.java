// ============================================================================
//
// Copyright (C) 2006-2016 Talend Inc. - www.talend.com
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

import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.talend.components.api.component.ComponentDefinition;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.api.properties.ComponentReferenceProperties;
import org.talend.core.model.components.IComponent;
import org.talend.core.model.process.IElementParameter;
import org.talend.core.model.process.INode;
import org.talend.core.model.repository.FakePropertyImpl;
import org.talend.core.ui.component.ComponentsFactoryProvider;
import org.talend.daikon.properties.Properties;
import org.talend.daikon.properties.presentation.Form;
import org.talend.designer.core.generic.constants.IGenericConstants;
import org.talend.designer.core.generic.model.Component.CodegenPropInfo;
import org.talend.designer.core.generic.utils.TestComponentDefinition;
import org.talend.designer.core.generic.utils.TestProperties;
import org.talend.designer.core.ui.editor.nodes.Node;
import org.talend.designer.core.ui.editor.process.Process;

/**
 * created by hcyi on Feb 17, 2016 Detailled comment
 *
 */
public class ComponentTest {

    private Component component;

    @Before
    public void setUp() throws Exception {
        ComponentDefinition componentDefinition = new TestComponentDefinition();
        component = new Component(componentDefinition);
    }

    @Test
    public void testGetElementParameterValueFromComponentProperties() {
        IComponent sfComponent = ComponentsFactoryProvider.getInstance().get("tSalesforceInput", "DI"); //$NON-NLS-1$ //$NON-NLS-2$
        INode node = new Node(sfComponent, new Process(new FakePropertyImpl()));
        ComponentProperties props = node.getComponentProperties();
        Form form = props.getForm(Form.MAIN);
        IElementParameter param = new GenericElementParameter(node, node.getComponentProperties(), form,
                form.getWidget("condition"), null); //$NON-NLS-1$
        Object obj = component.getElementParameterValueFromComponentProperties(node, param);
        Assert.assertNotNull(obj);
    }

    @Test
    public void testGetCodegenPropInfos() {
        ComponentProperties props = (ComponentProperties) new TestProperties("test").init(); //$NON-NLS-1$
        List<CodegenPropInfo> propInfos = component.getCodegenPropInfos(props);
        for (CodegenPropInfo propInfo : propInfos) {
            Properties properties = propInfo.props;
            if (properties instanceof ComponentReferenceProperties) {
                ComponentReferenceProperties crp = (ComponentReferenceProperties) properties;
                assertEquals(Boolean.TRUE, crp.componentInstanceId.getTaggedValue(IGenericConstants.ADD_QUOTES));
                assertEquals(Boolean.TRUE, crp.referenceDefinitionName.getTaggedValue(IGenericConstants.ADD_QUOTES));
            }
        }
    }

}
