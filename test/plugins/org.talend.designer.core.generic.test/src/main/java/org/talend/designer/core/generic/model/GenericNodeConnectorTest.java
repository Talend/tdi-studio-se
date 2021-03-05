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

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.talend.commons.utils.VersionUtils;
import org.talend.core.CorePlugin;
import org.talend.core.context.Context;
import org.talend.core.context.RepositoryContext;
import org.talend.core.model.components.ComponentCategory;
import org.talend.core.model.components.IComponent;
import org.talend.core.model.process.INode;
import org.talend.core.model.properties.PropertiesFactory;
import org.talend.core.model.properties.Property;
import org.talend.core.ui.component.ComponentsFactoryProvider;
import org.talend.designer.core.generic.utils.TestProperties;
import org.talend.designer.core.ui.editor.nodes.Node;
import org.talend.designer.core.ui.editor.process.Process;

/**
 * created by ycbai on 2016年10月12日 Detailled comment
 *
 */
public class GenericNodeConnectorTest {

    private static final String TEST_COMPONENT_NAME = "tSalesforceInput"; //$NON-NLS-1$

    private static IComponent testComponent;

    private Process process;

    private INode testNode;

    @BeforeClass
    public static void beforeClass() {
        testComponent = ComponentsFactoryProvider.getInstance().get(TEST_COMPONENT_NAME,
                ComponentCategory.CATEGORY_4_DI.getName());
    }

    @Before
    public void before() {
        process = new Process(createProperty());
        testNode = new Node(testComponent, process);
        TestProperties testProps = (TestProperties) new TestProperties("test").init(); //$NON-NLS-1$
        testNode.setComponentProperties(testProps);
    }

    @Test
    public void testGetComponentProperties() {
        TestProperties testProps2 = (TestProperties) new TestProperties("test2").init(); //$NON-NLS-1$

        GenericNodeConnector connector = new GenericNodeConnector(testNode, false);
        connector.setComponentProperties(testProps2);
        assertEquals(testNode.getComponentProperties(), connector.getComponentProperties());

        connector = new GenericNodeConnector(null, false);
        connector.setComponentProperties(testProps2);
        assertEquals(testProps2, connector.getComponentProperties());
    }

    private static Property createProperty() {
        Property property = PropertiesFactory.eINSTANCE.createProperty();
        property.setAuthor(((RepositoryContext) CorePlugin.getContext().getProperty(Context.REPOSITORY_CONTEXT_KEY)).getUser());
        property.setVersion(VersionUtils.DEFAULT_VERSION);

        return property;
    }

}
