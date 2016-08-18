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
package org.talend.repository.generic.util;

import static org.junit.Assert.*;

import org.junit.Test;
import org.talend.commons.utils.VersionUtils;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.core.model.properties.PropertiesFactory;
import org.talend.core.model.properties.Property;
import org.talend.designer.core.generic.constants.IGenericConstants;
import org.talend.designer.core.generic.utils.ComponentsUtils;
import org.talend.repository.generic.model.genericMetadata.GenericConnection;
import org.talend.repository.generic.model.genericMetadata.GenericConnectionItem;
import org.talend.repository.generic.model.genericMetadata.GenericMetadataFactory;

/**
 * created by ycbai on 2016年8月18日 Detailled comment
 *
 */
public class GenericConnectionUtilTest {

    @Test
    public void testSynNamePropertyWithItem() {
        String namePropertyValue = "test"; //$NON-NLS-1$
        TestProperties props = (TestProperties) new TestProperties("test").init(); //$NON-NLS-1$
        props.name.setValue(namePropertyValue);
        String serializedProps = props.toSerialized();
        GenericConnectionItem testItem = createTestItem(namePropertyValue, serializedProps);
        GenericConnection testConn = (GenericConnection) testItem.getConnection();
        boolean itemChanged = GenericConnectionUtil.synNamePropertyWithItem(testItem);
        assertFalse(itemChanged);
        ComponentProperties componentProperties = ComponentsUtils.getComponentPropertiesFromSerialized(serializedProps, testConn);
        assertEquals(namePropertyValue, componentProperties.getValuedProperty(IGenericConstants.NAME_PROPERTY).getValue());

        String newPropertyValue = "test1"; //$NON-NLS-1$
        testItem.getProperty().setLabel(newPropertyValue);
        itemChanged = GenericConnectionUtil.synNamePropertyWithItem(testItem);
        assertTrue(itemChanged);
        componentProperties = ComponentsUtils.getComponentPropertiesFromSerialized(testConn.getCompProperties(), testConn);
        assertEquals(newPropertyValue, componentProperties.getValuedProperty(IGenericConstants.NAME_PROPERTY).getValue());
    }

    private GenericConnectionItem createTestItem(String label, String serializedProps) {
        GenericConnection connection = GenericMetadataFactory.eINSTANCE.createGenericConnection();
        connection.setCompProperties(serializedProps);
        Property connectionProperty = PropertiesFactory.eINSTANCE.createProperty();
        connectionProperty.setId("testId"); //$NON-NLS-1$
        connectionProperty.setLabel(label);
        connectionProperty.setVersion(VersionUtils.DEFAULT_VERSION);
        connectionProperty.setStatusCode(""); //$NON-NLS-1$
        GenericConnectionItem connectionItem = GenericMetadataFactory.eINSTANCE.createGenericConnectionItem();
        connectionItem.setProperty(connectionProperty);
        connectionItem.setConnection(connection);
        return connectionItem;
    }

}
