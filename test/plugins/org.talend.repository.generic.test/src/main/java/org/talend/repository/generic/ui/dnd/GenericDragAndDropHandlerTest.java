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
package org.talend.repository.generic.ui.dnd;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.core.model.metadata.builder.connection.Connection;
import org.talend.core.model.utils.AbstractDragAndDropServiceHandler;
import org.talend.repository.generic.model.genericMetadata.GenericConnection;
import org.talend.repository.generic.model.genericMetadata.GenericMetadataFactory;

/**
 * created by hcyi on Feb 22, 2016 Detailled comment
 *
 */
public class GenericDragAndDropHandlerTest {

    @Before
    public void setUp() throws Exception {
    }

    @Test
    public void testIsGenericRepositoryValue() {
        Connection connection = GenericMetadataFactory.eINSTANCE.createGenericConnection();
        GenericDragAndDropHandler genericDragAndDropHandler = new GenericDragAndDropHandler();
        assertEquals(false, genericDragAndDropHandler.canHandle(connection));
        connection.setCompProperties("test");
        assertEquals(true, genericDragAndDropHandler.canHandle(connection));
        boolean isGenericRepositoryValue = genericDragAndDropHandler.isGenericRepositoryValue(null, "paramName2");//$NON-NLS-1$
        assertEquals(false, isGenericRepositoryValue);

        // PowerMockito.mockStatic(ComponentsUtils.class);
        // ComponentProperties mockComponentProperties = mock(ComponentProperties.class);
        // when(ComponentsUtils.getComponentPropertiesFromSerialized(null)).thenReturn(mockComponentProperties);
        // List<Property> propertyValues = new ArrayList<Property>();
        // Property element = ComponentPropertyFactory.newReturnsProperty();
        // propertyValues.add(element);
        // when(ComponentsUtils.getAllValuedProperties(mockComponentProperties)).thenReturn(propertyValues);
        //        isGenericRepositoryValue = genericDragAndDropHandler.isGenericRepositoryValue(connection, "QueryMode");//$NON-NLS-1$
        // assertEquals(true, isGenericRepositoryValue);
    }
}
