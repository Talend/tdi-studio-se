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
package org.talend.designer.unifiedcomponent.component;

import java.util.Set;

import org.junit.Assert;
import org.junit.Test;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.model.components.ComponentCategory;
import org.talend.designer.core.IUnifiedComponentService;

/**
 * created by wchen on Dec 15, 2017 Detailled comment
 *
 */
public class DelegateComponentTest {

    private IUnifiedComponentService unifiedCompservice = (IUnifiedComponentService) GlobalServiceRegister.getDefault()
            .getService(IUnifiedComponentService.class);

    @Test
    public void testGetUnifiedObjectsByPalette() {
        String testPalette = ComponentCategory.CATEGORY_4_DI.getName();
        DelegateComponent delegateComponent = (DelegateComponent) unifiedCompservice
                .getDelegateComponent("tDBInput", testPalette);
        Set<UnifiedObject> unifiedObjectsByPalette = delegateComponent.getUnifiedObjectsByPalette(testPalette);
        for (UnifiedObject obj : unifiedObjectsByPalette) {
            Assert.assertTrue(obj.getSupportedCategories().contains(testPalette));
        }

        unifiedObjectsByPalette = delegateComponent.getUnifiedObjectsByPalette("testGetUnifiedObjectsByPalette");
        Assert.assertEquals(unifiedObjectsByPalette.size(), 0);

        DelegateComponent testComponent = new DelegateComponent("testFamily", "testComponent");
        UnifiedObject object = new UnifiedObject();
        object.setComponentName("tMysqlComponent");
        object.setDatabase("Mysql");
        object.getSupportedCategories().add("myPalette_1");
        testComponent.getUnifiedObjects().add(object);
        object = new UnifiedObject();
        object.setComponentName("tOracleComponent");
        object.setDatabase("Oracle");
        object.getSupportedCategories().add("myPalette_2");
        testComponent.getUnifiedObjects().add(object);
        unifiedObjectsByPalette = testComponent.getUnifiedObjectsByPalette("myPalette_1");
        Assert.assertEquals(unifiedObjectsByPalette.size(), 1);

        object.getSupportedCategories().add("myPalette_1");
        unifiedObjectsByPalette = testComponent.getUnifiedObjectsByPalette("myPalette_1");
        Assert.assertEquals(unifiedObjectsByPalette.size(), 2);

    }

    @Test
    public void testGetUnifiedObjectByName() {
        String testPalette = ComponentCategory.CATEGORY_4_DI.getName();
        DelegateComponent delegateComponent = (DelegateComponent) unifiedCompservice.getDelegateComponent("tDBRow", testPalette);
        UnifiedObject unifiedObjectByName = delegateComponent.getUnifiedObjectByName("tMysqlRow");
        Assert.assertNotNull(unifiedObjectByName);
        Assert.assertEquals(unifiedObjectByName.getComponentName(), "tMysqlRow");

        DelegateComponent testComponent = new DelegateComponent("testFamily", "testComponent");
        testComponent.setPaletteType(testPalette);
        UnifiedObject object = new UnifiedObject();
        object.setComponentName("tMysqlComponent");
        object.setDatabase("Mysql");
        object.getSupportedCategories().add(testPalette);
        testComponent.getUnifiedObjects().add(object);
        object = new UnifiedObject();
        object.setComponentName("tOracleComponent");
        object.setDatabase("Oracle");
        object.getSupportedCategories().add(testPalette);
        testComponent.getUnifiedObjects().add(object);
        unifiedObjectByName = testComponent.getUnifiedObjectByName("tMysqlComponent");
        Assert.assertNotNull(unifiedObjectByName);
        Assert.assertEquals(unifiedObjectByName.getComponentName(), "tMysqlComponent");

        unifiedObjectByName = testComponent.getUnifiedObjectByName("tOracleComponent");
        Assert.assertNotNull(unifiedObjectByName);
        Assert.assertEquals(unifiedObjectByName.getComponentName(), "tOracleComponent");

    }

    @Test
    public void testGetUnifiedObjectByDatabase() {
        String testPalette = ComponentCategory.CATEGORY_4_DI.getName();
        DelegateComponent delegateComponent = (DelegateComponent) unifiedCompservice
                .getDelegateComponent("tDBClose", testPalette);
        UnifiedObject unifiedObjectByName = delegateComponent.getUnifiedObjectByDatabase("Oracle");
        Assert.assertNotNull(unifiedObjectByName);
        Assert.assertEquals(unifiedObjectByName.getDatabase(), "Oracle");

        DelegateComponent testComponent = new DelegateComponent("testFamily", "testComponent");
        testComponent.setPaletteType(testPalette);
        UnifiedObject object = new UnifiedObject();
        object.setComponentName("tMysqlComponent");
        object.setDatabase("MyDatabase");
        object.getSupportedCategories().add(testPalette);
        testComponent.getUnifiedObjects().add(object);
        object = new UnifiedObject();
        object.setComponentName("tOracleComponent");
        object.setDatabase("Oracle");
        object.getSupportedCategories().add(testPalette);
        testComponent.getUnifiedObjects().add(object);
        unifiedObjectByName = testComponent.getUnifiedObjectByDatabase("MyDatabase");
        Assert.assertNotNull(unifiedObjectByName);
        Assert.assertEquals(unifiedObjectByName.getDatabase(), "MyDatabase");

        unifiedObjectByName = testComponent.getUnifiedObjectByDatabase("Oracle");
        Assert.assertNotNull(unifiedObjectByName);
        Assert.assertEquals(unifiedObjectByName.getDatabase(), "Oracle");

    }

}
