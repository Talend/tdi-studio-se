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
import org.talend.core.model.components.ComponentCategory;
import org.talend.core.model.components.IComponent;
import org.talend.core.model.process.EParameterFieldType;
import org.talend.core.model.process.IElementParameter;
import org.talend.core.model.process.INode;
import org.talend.core.ui.component.ComponentsFactoryProvider;
import org.talend.designer.core.model.components.EParameterName;
import org.talend.designer.core.model.components.EmfComponent;
import org.talend.designer.core.test.util.NodeTestCreator;
import org.talend.designer.core.ui.editor.nodes.Node;
import org.talend.designer.core.ui.editor.process.Process;
import org.talend.designer.core.utils.TestProperties;
import org.talend.designer.core.utils.TestUtils;

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
        process = new Process(TestUtils.createDefaultProperty());
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

    @Test
    public void testCheckUseHadoopConfs() throws Exception {
        Node simpleInputNode = NodeTestCreator.createSimpleInputNode(process);
        TestUtils.invokePrivateMethod(dataProcess, "initialize", new Object[0]); //$NON-NLS-1$

        // Built in mode
        TestUtils.invokePrivateMethod(dataProcess, "checkUseHadoopConfs", new Object[] { simpleInputNode }, INode.class); //$NON-NLS-1$
        assertFalse(containsHadoopConfsNode());

        // Repository mode but repository value is null
        IElementParameter propertyElementParameter = simpleInputNode
                .getElementParameterFromField((EParameterFieldType.PROPERTY_TYPE));
        propertyElementParameter.getChildParameters().get(EParameterName.PROPERTY_TYPE.getName())
                .setValue(EmfComponent.REPOSITORY);
        propertyElementParameter.getChildParameters().get(EParameterName.REPOSITORY_PROPERTY_TYPE.getName()).setValue(null);
        TestUtils.invokePrivateMethod(dataProcess, "checkUseHadoopConfs", new Object[] { simpleInputNode }, INode.class); //$NON-NLS-1$
        assertFalse(containsHadoopConfsNode());
    }

    private boolean containsHadoopConfsNode() {
        for (INode node : dataProcess.getNodeList()) {
            String compName = node.getComponent().getName();
            if ("tHadoopConfManager".equals(compName)) { //$NON-NLS-1$
                return true;
            }
        }
        return false;
    }

}
