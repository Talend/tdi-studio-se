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
package org.talend.designer.core.model.process;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.talend.core.model.components.ComponentCategory;
import org.talend.core.model.components.IComponent;
import org.talend.core.model.context.JobContextManager;
import org.talend.core.model.context.JobContextParameter;
import org.talend.core.model.process.EParameterFieldType;
import org.talend.core.model.process.IContext;
import org.talend.core.model.process.IContextManager;
import org.talend.core.model.process.IContextParameter;
import org.talend.core.model.process.IElement;
import org.talend.core.model.process.IElementParameter;
import org.talend.core.model.process.INode;
import org.talend.core.model.process.IProcess;
import org.talend.core.ui.component.ComponentsFactoryProvider;
import org.talend.designer.core.model.components.EParameterName;
import org.talend.designer.core.model.components.EmfComponent;
import org.talend.designer.core.test.util.NodeTestCreator;
import org.talend.designer.core.ui.editor.nodes.Node;
import org.talend.designer.core.ui.editor.process.Process;
import org.talend.designer.core.utils.TestUtils;

/**
 * created by ycbai on 2016年3月23日 Detailled comment
 *
 */
public class DataProcessTest {

    private static final String TEST_COMPONENT_NAME = "tSalesforceInput"; //$NON-NLS-1$

    private static final String TEST_SALESFORCE_CONNECTION_NAME = "tSalesforceConnection"; //$NON-NLS-1$

    private static IComponent testComponent;

    private static IComponent testComponent_1;

    private Process process;

    private Process process_1;

    private Process process_2;

    private DataProcess dataProcess;

    private TestDataProcess testDataProcess;

    private INode testNode;

    private Node sourceNode;

    private Node targetNode;

    private static class TestDataProcess extends DataProcess {

        public TestDataProcess(IProcess process) {
            super(process);
        }

        @Override
        protected void copyElementParametersValue(IElement sourceElement, IElement targetElement) {
            super.copyElementParametersValue(sourceElement, targetElement);
        }
    }

    @BeforeClass
    public static void beforeClass() {
        testComponent = ComponentsFactoryProvider.getInstance().get(TEST_COMPONENT_NAME,
                ComponentCategory.CATEGORY_4_DI.getName());
        testComponent_1 = ComponentsFactoryProvider.getInstance().get(TEST_SALESFORCE_CONNECTION_NAME,
                ComponentCategory.CATEGORY_4_DI.getName());
    }

    @Before
    public void before() {
        process = new Process(TestUtils.createDefaultProperty());
        dataProcess = new DataProcess(process);
        testNode = new Node(testComponent, process);

        testDataProcess = new TestDataProcess(process_1);
        process_1 = new Process(TestUtils.createDefaultProperty());
        process_2 = new Process(TestUtils.createDefaultProperty());
        process_1.addUniqueNodeName("testProcess1"); //$NON-NLS-1$
        process_2.addUniqueNodeName("testProcess2"); //$NON-NLS-1$

        sourceNode = new Node(testComponent_1, process_1);
        targetNode = new Node(testComponent_1, process_2);

        IContextManager cm = process_1.getContextManager();
        if (cm == null) {
            cm = new JobContextManager();
        }

        for (IElementParameter param : sourceNode.getElementParameters()) {
            if (param.getClass().getName().equals("org.talend.designer.core.generic.model.GenericElementParameter")) //$NON-NLS-1$
            {
                if (param.getName().equals("loginType")) { //$NON-NLS-1$
                    param.setFieldType(EParameterFieldType.CLOSED_LIST);
                    param.setContextMode(true);
                    param.setName("context.salesForce_Connection_loginType"); //$NON-NLS-1$
                    param.setValue("OAuth"); //$NON-NLS-1$
                } else if (param.getName().equals("oauth2FlowType")) { //$NON-NLS-1$
                    param.setFieldType(EParameterFieldType.CLOSED_LIST);
                    param.setContextMode(true);
                    param.setName("context.salesForce_Connection_oauth2FlowType"); //$NON-NLS-1$
                    param.setValue("JWT_Flow"); //$NON-NLS-1$
                }
            }
        }

        IContext context = cm.getDefaultContext();
        context.setName("test"); //$NON-NLS-1$
        JobContextParameter connTypeParam = new JobContextParameter();
        connTypeParam.setName("context.salesForce_Connection_loginType"); //$NON-NLS-1$
        connTypeParam.setValue("OAuth"); //$NON-NLS-1$
        connTypeParam.setSource(EParameterName.REPOSITORY_PROPERTY_TYPE.getName());
        IContextParameter toAdd = connTypeParam.clone();
        context.getContextParameterList().add(toAdd);
        JobContextParameter oauth2FlowTypeParam = new JobContextParameter();
        oauth2FlowTypeParam.setName("context.salesForce_Connection_oauth2FlowType"); //$NON-NLS-1$
        oauth2FlowTypeParam.setValue("JWT_Flow"); //$NON-NLS-1$
        toAdd = oauth2FlowTypeParam.clone();
        context.getContextParameterList().add(toAdd);
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
        TestUtils.invokePrivateMethod(dataProcess, "checkUseHadoopConfs", new Object[] { simpleInputNode, "" }, INode.class, //$NON-NLS-1$
                String.class);
        assertFalse(containsHadoopConfsNode());

        // Repository mode but repository value is null
        IElementParameter propertyElementParameter = simpleInputNode
                .getElementParameterFromField((EParameterFieldType.PROPERTY_TYPE));
        propertyElementParameter.getChildParameters().get(EParameterName.PROPERTY_TYPE.getName())
                .setValue(EmfComponent.REPOSITORY);
        propertyElementParameter.getChildParameters().get(EParameterName.REPOSITORY_PROPERTY_TYPE.getName()).setValue(null);
        TestUtils.invokePrivateMethod(dataProcess, "checkUseHadoopConfs", new Object[] { simpleInputNode, "" }, INode.class, //$NON-NLS-1$
                String.class);
        assertFalse(containsHadoopConfsNode());
    }

    @Test
    public void testCopyElementParametersValue() {
        testDataProcess.copyElementParametersValue(sourceNode, targetNode);
        for (IElementParameter sourceParameter : sourceNode.getElementParameters()) {
            IElementParameter targetParam = targetNode.getElementParameter(sourceParameter.getName());
            if (!sourceParameter.getFieldType().equals(EParameterFieldType.CLOSED_LIST)) {
                assertEquals(sourceParameter.getValue(), targetParam.getValue());
            } else {
                if (sourceParameter.getName().equals("context.salesForce_Connection_loginType")) { //$NON-NLS-1$
                    assertEquals(targetParam.getName(), "context.salesForce_Connection_loginType"); //$NON-NLS-1$
                    assertEquals(targetParam.getValue(), "OAuth"); //$NON-NLS-1$
                }

                if (sourceParameter.getName().equals("context.salesForce_Connection_oauth2FlowType")) { //$NON-NLS-1$
                    assertEquals(targetParam.getName(), "context.salesForce_Connection_oauth2FlowType"); //$NON-NLS-1$
                    assertEquals(targetParam.getValue(), "JWT_Flow"); //$NON-NLS-1$
                }
            }
        }
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
