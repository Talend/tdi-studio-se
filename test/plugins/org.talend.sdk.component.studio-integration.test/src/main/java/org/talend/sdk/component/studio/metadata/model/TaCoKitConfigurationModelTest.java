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
package org.talend.sdk.component.studio.metadata.model;

import static org.mockito.Mockito.*;
import static org.talend.sdk.component.studio.metadata.model.TaCoKitConfigurationModel.BuiltInKeys.*;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.talend.commons.utils.VersionUtils;
import org.talend.core.CorePlugin;
import org.talend.core.context.Context;
import org.talend.core.context.RepositoryContext;
import org.talend.core.model.metadata.builder.connection.Connection;
import org.talend.core.model.metadata.builder.connection.ConnectionFactory;
import org.talend.core.model.properties.ConnectionItem;
import org.talend.core.model.properties.PropertiesFactory;
import org.talend.core.model.properties.Property;
import org.talend.core.runtime.CoreRuntimePlugin;
import org.talend.repository.model.IProxyRepositoryFactory;
import org.talend.sdk.component.server.front.model.ConfigTypeNode;
import org.talend.sdk.component.studio.metadata.TaCoKitCache;

/**
 * created by hcyi on Jul 23, 2019
 * Detailled comment
 *
 */
public class TaCoKitConfigurationModelTest {

    private static IProxyRepositoryFactory factory = CoreRuntimePlugin.getInstance().getProxyRepositoryFactory();

    private static TaCoKitConfigurationModel configurationModel = null;

    @BeforeClass
    public static void init() throws Exception {
        ConnectionItem connectionItem = createConnectionItem();
        ConfigTypeNode configTypeNode = mock(ConfigTypeNode.class);
        TaCoKitCache taCoKitCache = new TaCoKitCache();
        taCoKitCache.getConfigTypeNodeMap().put("cWEjUUEjZGF0YXNldCNidWlsdEluU2V0", configTypeNode); //$NON-NLS-1$
        configurationModel = new TaCoKitConfigurationModel(connectionItem.getConnection(), configTypeNode);
    }

    @Test
    public void testConvertParameterValue4NULL() throws Exception {
        Object obj = configurationModel.convertParameterValue(null, null, null);
        Assert.assertEquals(null, obj);
    }

    @Test
    public void testConvertParameterValue4Empty1() throws Exception {
        Object obj = configurationModel.convertParameterValue("", "", "");//$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        Assert.assertEquals("", obj);//$NON-NLS-1$
    }

    @Test
    public void testConvertParameterValue4Empty2() throws Exception {
        Object obj = configurationModel.convertParameterValue("currentKey", "parentKey", "[]");//$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        Assert.assertEquals("[]", obj);//$NON-NLS-1$
    }

    @Test
    public void testConvertParameterValue4Empty3() throws Exception {
        Object obj = configurationModel.convertParameterValue("currentKey", "parentKey", "[{}]");//$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        Assert.assertEquals("[{}]", obj);//$NON-NLS-1$
    }

    @Test
    public void testConvertParameterValue4SingleKey() throws Exception {
        Object obj = configurationModel.convertParameterValue("currentKey", "parentKey", "[{test=1}]");//$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        Assert.assertEquals("[{test=1}]", obj);//$NON-NLS-1$
    }

    @Test
    public void testConvertParameterValue4TableSingleColumn() throws Exception {
        Object obj = configurationModel.convertParameterValue("config.customMultiple", "configuration.customMultiple", //$NON-NLS-1$ //$NON-NLS-2$
                "[{configuration.customMultiple[]=\"a1\"}, {configuration.customMultiple[]=\"a2\"}, {configuration.customMultiple[]=\"a3\"}]");//$NON-NLS-1$
        Assert.assertEquals(
                "[{config.customMultiple[]=\"a1\"}, {config.customMultiple[]=\"a2\"}, {config.customMultiple[]=\"a3\"}]", //$NON-NLS-1$
                obj);
    }

    @Test
    public void testConvertParameterValue4TableMultiColumn() throws Exception {
        Object obj = configurationModel.convertParameterValue("config.table", "configuration.table", //$NON-NLS-1$ //$NON-NLS-2$
                "[{configuration.table[].operation=b1, configuration.table[].inputColumn=a1}, {configuration.table[].operation=b2, configuration.table[].inputColumn=a2}]");//$NON-NLS-1$
        Assert.assertEquals(
                "[{config.table[].operation=b1, config.table[].inputColumn=a1}, {config.table[].operation=b2, config.table[].inputColumn=a2}]", //$NON-NLS-1$
                obj);
    }

    private static ConnectionItem createConnectionItem() throws Exception {
        Connection connection = ConnectionFactory.eINSTANCE.createConnection();
        connection.setName("test"); //$NON-NLS-1$
        connection.setId(factory.getNextId());
        connection.getProperties().put(TACOKIT_CONFIG_ID, "cWEjUUEjZGF0YXNldCNidWlsdEluU2V0");//$NON-NLS-1$

        Property property = PropertiesFactory.eINSTANCE.createProperty();
        property.setAuthor(((RepositoryContext) CorePlugin.getContext().getProperty(Context.REPOSITORY_CONTEXT_KEY)).getUser());
        property.setVersion(VersionUtils.DEFAULT_VERSION);
        property.setStatusCode(""); //$NON-NLS-1$

        ConnectionItem connectionItem = PropertiesFactory.eINSTANCE.createConnectionItem();
        connectionItem.setConnection(connection);
        connectionItem.setProperty(property);
        connectionItem.setTypeName("testItem");//$NON-NLS-1$
        return connectionItem;
    }
}
