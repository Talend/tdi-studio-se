// ============================================================================
//
// Copyright (C) 2006-2019 Talend Inc. - www.talend.com
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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.talend.commons.utils.VersionUtils;
import org.talend.core.CorePlugin;
import org.talend.core.context.Context;
import org.talend.core.context.RepositoryContext;
import org.talend.core.model.metadata.builder.connection.Connection;
import org.talend.core.model.metadata.builder.connection.ConnectionFactory;
import org.talend.core.model.properties.ConnectionItem;
import org.talend.core.model.properties.PropertiesFactory;
import org.talend.core.model.properties.Property;

/**
 * created by hcyi on Jul 23, 2019
 * Detailled comment
 *
 */
public class TaCoKitConfigurationModelTest {

    @Test
    public void testConvertParameterValue4Empty() throws Exception {
        ConnectionItem connectionItem = createConnectionItem();
        TaCoKitConfigurationModel configurationModel = new TaCoKitConfigurationModel(connectionItem.getConnection());
        Object obj = configurationModel.convertParameterValue("", "", "[{}]");
        Assertions.assertEquals("", obj);
    }

    @Test
    public void testConvertParameterValue4TableSingleColumn() throws Exception {
        ConnectionItem connectionItem = createConnectionItem();
        TaCoKitConfigurationModel configurationModel = new TaCoKitConfigurationModel(connectionItem.getConnection());
        Object obj = configurationModel.convertParameterValue("config.customMultiple", "configuration.customMultiple",
                "[{configuration.customMultiple[]=\"a1\"}, {configuration.customMultiple[]=\"a2\"}, {configuration.customMultiple[]=\"a3\"}]");
        Assertions.assertEquals(
                "[{configuration.customMultiple[]=\"a1\"}, {config.customMultiple[]=\"a2\"}, {config.customMultiple[]=\"a3\"}]",
                obj);
    }

    @Test
    public void testConvertParameterValue4TableMultiColumn() throws Exception {
        ConnectionItem connectionItem = createConnectionItem();
        TaCoKitConfigurationModel configurationModel = new TaCoKitConfigurationModel(connectionItem.getConnection());
        Object obj = configurationModel.convertParameterValue("config.table", "configuration.table",
                "[{configuration.table[].operation=b1, configuration.table[].inputColumn=a1}, {configuration.table[].operation=b2, configuration.table[].inputColumn=a2}]");
        Assertions.assertEquals(
                "[{config.table[].operation=b1, config.table[].inputColumn=a1}, {config.table[].operation=b2, config.table[].inputColumn=a2}]",
                obj);
    }

    private ConnectionItem createConnectionItem() throws Exception {
        Connection connection = ConnectionFactory.eINSTANCE.createConnection();

        Property property = PropertiesFactory.eINSTANCE.createProperty();
        property.setAuthor(((RepositoryContext) CorePlugin.getContext().getProperty(Context.REPOSITORY_CONTEXT_KEY)).getUser());
        property.setVersion(VersionUtils.DEFAULT_VERSION);
        property.setStatusCode(""); //$NON-NLS-1$

        ConnectionItem connectionItem = PropertiesFactory.eINSTANCE.createConnectionItem();
        connectionItem.setConnection(connection);
        connectionItem.setProperty(property);
        connectionItem.setTypeName("test");
        return connectionItem;
    }
}
