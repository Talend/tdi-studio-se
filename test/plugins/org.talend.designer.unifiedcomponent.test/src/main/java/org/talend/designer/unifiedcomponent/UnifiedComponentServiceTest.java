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
package org.talend.designer.unifiedcomponent;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.database.EDatabaseTypeName;
import org.talend.core.model.components.ComponentCategory;
import org.talend.core.model.components.IComponent;
import org.talend.core.model.components.IComponentsService;
import org.talend.core.model.process.EParameterFieldType;
import org.talend.core.model.process.IElementParameter;
import org.talend.core.model.properties.PropertiesFactory;
import org.talend.core.model.properties.Property;
import org.talend.core.repository.RepositoryComponentSetting;
import org.talend.designer.core.IUnifiedComponentService;
import org.talend.designer.core.ui.editor.nodes.Node;
import org.talend.designer.core.ui.editor.process.Process;

/**
 * created by wchen on Dec 13, 2017 Detailled comment
 *
 */
public class UnifiedComponentServiceTest {

    private IUnifiedComponentService unifiedCompservice = (IUnifiedComponentService) GlobalServiceRegister.getDefault()
            .getService(IUnifiedComponentService.class);

    private IComponentsService compService = (IComponentsService) GlobalServiceRegister.getDefault().getService(
            IComponentsService.class);

    /**
     * Test method for
     * {@link org.talend.designer.unifiedcomponent.UnifiedComponentService#isUnifiedComponent(org.talend.core.model.components.IComponent)}
     * .
     */
    @Test
    public void testIsUnifiedComponent() {

        IComponent tRunJob = compService.getComponentsFactory().get("tRunJob", ComponentCategory.CATEGORY_4_DI.getName());
        Assert.assertFalse(unifiedCompservice.isUnifiedComponent(tRunJob));

        IComponent tMysqlConnection = compService.getComponentsFactory().get("tMysqlConnection",
                ComponentCategory.CATEGORY_4_DI.getName());
        Assert.assertTrue(unifiedCompservice.isUnifiedComponent(tMysqlConnection));

        Assert.assertFalse(unifiedCompservice.isUnifiedComponent(null));
    }

    /**
     * Test method for
     * {@link org.talend.designer.unifiedcomponent.UnifiedComponentService#getUnifiedComponetName4DndFromRepository(org.talend.core.model.utils.IComponentName, org.talend.core.model.components.IComponent)}
     * .
     */
    @Test
    public void testGetUnifiedComponetName4DndFromRepository() {
        RepositoryComponentSetting settings = new RepositoryComponentSetting();
        settings.setInputComponent("tMysqlInput");
        settings.setOutputComponent("tMysqlOutput");

        IComponent delegateComponent = unifiedCompservice.getDelegateComponent("tDBInput",
                ComponentCategory.CATEGORY_4_DI.getName());
        String emfComponentName = unifiedCompservice.getUnifiedComponetName4DndFromRepository(settings, delegateComponent);
        Assert.assertEquals(emfComponentName, "tMysqlInput");

        delegateComponent = unifiedCompservice.getDelegateComponent("tDBOutput", ComponentCategory.CATEGORY_4_DI.getName());
        emfComponentName = unifiedCompservice.getUnifiedComponetName4DndFromRepository(settings, delegateComponent);
        Assert.assertEquals(emfComponentName, "tMysqlOutput");

        delegateComponent = unifiedCompservice.getDelegateComponent("tDBConnection", ComponentCategory.CATEGORY_4_DI.getName());
        emfComponentName = unifiedCompservice.getUnifiedComponetName4DndFromRepository(settings, delegateComponent);
        Assert.assertEquals(emfComponentName, "tMysqlConnection");

        delegateComponent = unifiedCompservice.getDelegateComponent("tDBRollback", ComponentCategory.CATEGORY_4_DI.getName());
        emfComponentName = unifiedCompservice.getUnifiedComponetName4DndFromRepository(settings, delegateComponent);
        Assert.assertEquals(emfComponentName, "tMysqlRollback");
    }

    /**
     * Test method for
     * {@link org.talend.designer.unifiedcomponent.UnifiedComponentService#getDelegateComponents(java.lang.String)}.
     */
    @Test
    public void testGetDelegateComponents() {
        Collection<IComponent> delegateComponents = unifiedCompservice.getDelegateComponents(ComponentCategory.CATEGORY_4_DI
                .getName());
        Assert.assertTrue(delegateComponents.size() > 0);

        delegateComponents = unifiedCompservice.getDelegateComponents("test");
        Assert.assertTrue(delegateComponents.size() == 0);

    }

    /**
     * Test method for
     * {@link org.talend.designer.unifiedcomponent.UnifiedComponentService#getDelegateComponent(org.talend.core.model.components.IComponent)}
     * .
     */
    @Test
    public void testGetDelegateComponentIComponent() {
        IComponent tRunJob = compService.getComponentsFactory().get("tRunJob", ComponentCategory.CATEGORY_4_DI.getName());
        IComponent delegateComponent = unifiedCompservice.getDelegateComponent(tRunJob);
        Assert.assertTrue(delegateComponent == tRunJob);

        IComponent tAS400Commit = compService.getComponentsFactory().get("tAS400Commit",
                ComponentCategory.CATEGORY_4_DI.getName());
        IComponent as400Delegate = unifiedCompservice.getDelegateComponent(tAS400Commit);
        Assert.assertTrue(tAS400Commit != as400Delegate);
        Assert.assertEquals(as400Delegate.getName(), "tDBCommit");

        IComponent tDBClose = unifiedCompservice.getDelegateComponent("tDBClose", ComponentCategory.CATEGORY_4_DI.getName());
        IComponent tDBCloseDelegate = unifiedCompservice.getDelegateComponent(tDBClose);
        Assert.assertTrue(tDBClose == tDBCloseDelegate);

    }

    /**
     * Test method for
     * {@link org.talend.designer.unifiedcomponent.UnifiedComponentService#getDelegateComponent(java.lang.String, java.lang.String)}
     * .
     */
    @Test
    public void testGetDelegateComponentStringString() {
        IComponent delegateComponent = unifiedCompservice.getDelegateComponent("testGetDelegateComponent_IComponent",
                ComponentCategory.CATEGORY_4_DI.getName());
        Assert.assertNull(delegateComponent);

        delegateComponent = unifiedCompservice.getDelegateComponent("tDBColumnList", ComponentCategory.CATEGORY_4_DI.getName());
        Assert.assertNotNull(delegateComponent);

        delegateComponent = unifiedCompservice.getDelegateComponent("tDBRow", ComponentCategory.CATEGORY_4_DI.getName());
        Assert.assertNotNull(delegateComponent);
    }

    /**
     * Test method for
     * {@link org.talend.designer.unifiedcomponent.UnifiedComponentService#isDelegateComponent(org.talend.core.model.components.IComponent)}
     * .
     */
    @Test
    public void testIsDelegateComponent() {
        IComponent tFixedFlowInput = compService.getComponentsFactory().get("tFixedFlowInput",
                ComponentCategory.CATEGORY_4_DI.getName());
        Assert.assertFalse(unifiedCompservice.isDelegateComponent(tFixedFlowInput));

        IComponent tDBOutput = unifiedCompservice.getDelegateComponent("tDBOutput", ComponentCategory.CATEGORY_4_DI.getName());
        Assert.assertTrue(unifiedCompservice.isDelegateComponent(tDBOutput));

        Assert.assertFalse(unifiedCompservice.isDelegateComponent(null));

    }

    /**
     * Test method for
     * {@link org.talend.designer.unifiedcomponent.UnifiedComponentService#createParameters(org.talend.core.model.process.INode, java.util.List, org.talend.core.model.components.IComponent, org.talend.core.model.components.IComponent)}
     * .
     */
    @Test
    public void testCreateParameters() {
        IComponent tMysqlInput = compService.getComponentsFactory().get("tMysqlConnection",
                ComponentCategory.CATEGORY_4_DI.getName());
        IComponent tDBConnection = unifiedCompservice.getDelegateComponent("tDBConnection",
                ComponentCategory.CATEGORY_4_DI.getName());
        Property property1 = PropertiesFactory.eINSTANCE.createProperty();
        property1.setId("property1"); //$NON-NLS-1$
        property1.setVersion("0.1"); //$NON-NLS-1$
        property1.setLabel("test1");//$NON-NLS-1$
        Process process1 = new Process(property1);
        Node node = new Node(tMysqlInput, process1);
        List<IElementParameter> paramList = new ArrayList<IElementParameter>();
        unifiedCompservice.createParameters(node, paramList, tDBConnection, tMysqlInput);
        Assert.assertEquals(paramList.size(), 1);
        IElementParameter unifiedParameter = getUnifiedParameter(paramList);
        Assert.assertEquals(unifiedParameter.getValue(), "tMysqlConnection");

        Node node1 = new Node(tDBConnection, process1);
        paramList = new ArrayList<IElementParameter>();
        unifiedCompservice.createParameters(node1, paramList, tDBConnection, tDBConnection);
        Assert.assertTrue(paramList.size() > 1);
        unifiedParameter = getUnifiedParameter(paramList);
        Assert.assertEquals(unifiedParameter.getValue(), "");

    }

    private IElementParameter getUnifiedParameter(List<IElementParameter> listParams) {
        for (IElementParameter param : listParams) {
            if (param.getFieldType() == EParameterFieldType.UNIFIED_COMPONENTS) {
                return param;
            }
        }
        return null;
    }

    /**
     * Test method for
     * {@link org.talend.designer.unifiedcomponent.UnifiedComponentService#getUnifiedCompDisplayName(org.talend.core.model.components.IComponent, java.lang.String)}
     * .
     */
    @Test
    public void testGetUnifiedCompDisplayName() {
        IComponent tDBConnection = unifiedCompservice.getDelegateComponent("tDBConnection",
                ComponentCategory.CATEGORY_4_DI.getName());

        String displayName = unifiedCompservice.getUnifiedCompDisplayName(tDBConnection, "tMysqlConnection");
        Assert.assertEquals(displayName, EDatabaseTypeName.MYSQL.getDisplayName());

        displayName = unifiedCompservice.getUnifiedCompDisplayName(tDBConnection, "tDB2Connection");
        Assert.assertEquals(displayName, EDatabaseTypeName.IBMDB2.getDisplayName());

        displayName = unifiedCompservice.getUnifiedCompDisplayName(tDBConnection, "tOracleConnection");
        Assert.assertEquals(displayName, "Oracle");

        IComponent tMysqlInput = compService.getComponentsFactory().get("tMysqlConnection",
                ComponentCategory.CATEGORY_4_DI.getName());

        displayName = unifiedCompservice.getUnifiedCompDisplayName(tMysqlInput, "tMysqlConnection");
        Assert.assertNull(displayName);
    }

    @Test
    public void testGetComponentDisplayNameForPalette() {
        IComponent tDBConnection = unifiedCompservice.getDelegateComponent("tDBConnection",
                ComponentCategory.CATEGORY_4_DI.getName());
        String displayName = unifiedCompservice.getComponentDisplayNameForPalette(tDBConnection, "mysql");
        String expected = "tDBConnection(MySQL)";
        Assert.assertEquals(expected, displayName);

        displayName = unifiedCompservice.getComponentDisplayNameForPalette(tDBConnection, "oracle");
        expected = "tDBConnection(Oracle)";
        Assert.assertEquals(expected, displayName);

        IComponent tDBInput = unifiedCompservice.getDelegateComponent("tDBInput", ComponentCategory.CATEGORY_4_DI.getName());
        displayName = unifiedCompservice.getComponentDisplayNameForPalette(tDBInput, "tmysqlinput");
        expected = "tDBInput(MySQL)";
        Assert.assertEquals(expected, displayName);

        displayName = unifiedCompservice.getComponentDisplayNameForPalette(tDBInput, "mysqlinput");
        expected = "tDBInput(MySQL)";
        Assert.assertEquals(expected, displayName);
    }

    @Test
    public void getUnifiedComponentByFilter() {
        IComponent tDBConnection = unifiedCompservice
                .getDelegateComponent("tDBOutput", ComponentCategory.CATEGORY_4_DI.getName());
        IComponent emfComponent = unifiedCompservice.getUnifiedComponentByFilter(tDBConnection, "mysql");
        Assert.assertEquals("tMysqlOutput", emfComponent.getName());

        IComponent tDBCDC = unifiedCompservice.getDelegateComponent("tDBCDC", ComponentCategory.CATEGORY_4_DI.getName());
        emfComponent = unifiedCompservice.getUnifiedComponentByFilter(tDBCDC, "Oracle");
        Assert.assertEquals("tOracleCDC", emfComponent.getName());
    }
}
