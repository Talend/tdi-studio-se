// ============================================================================
//
// Copyright (C) 2006-2015 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.designer.core.model.process;

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
import org.talend.designer.core.ui.editor.nodes.Node;
import org.talend.designer.core.ui.editor.process.Process;
import org.talend.designer.core.utils.TestProperties;

/**
 * created by ycbai on 2016年3月23日 Detailled comment
 *
 */
public class DataProcessTest {

    private static final String TEST_COMPONENT_NAME = "tSalesforceInput"; //$NON-NLS-1$

    private static IComponent testComponent;

    private Process process;

    private DataProcess dataProcess;

    private INode testNode;

    @BeforeClass
    public static void beforeClass() {
        testComponent = ComponentsFactoryProvider.getInstance().get(TEST_COMPONENT_NAME,
                ComponentCategory.CATEGORY_4_DI.getName());
    }

    @Before
    public void before() {
        process = new Process(createProperty());
        dataProcess = new DataProcess(process);
        testNode = new Node(testComponent, process);
        TestProperties testProps = (TestProperties) new TestProperties("test").init(); //$NON-NLS-1$
        testNode.setComponentProperties(testProps);
    }

    @Test
    public void testBuildDataNodeFromNodeForComponentProperties() {
        INode dataNode = dataProcess.buildDataNodeFromNode(testNode);
        assertEquals(testNode.getComponentProperties(), dataNode.getComponentProperties());
    }

    @Test
    public void testBuildNodeFromNodeForComponentProperties() {
        INode node = dataProcess.buildNodeFromNode(testNode, process);
        assertEquals(testNode.getComponentProperties(), node.getComponentProperties());
    }

    private static Property createProperty() {
        Property property = PropertiesFactory.eINSTANCE.createProperty();
        property.setAuthor(((RepositoryContext) CorePlugin.getContext().getProperty(Context.REPOSITORY_CONTEXT_KEY)).getUser());
        property.setVersion(VersionUtils.DEFAULT_VERSION);

        return property;
    }

}
