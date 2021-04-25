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
package org.talend.repository.generic.util;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.talend.commons.utils.VersionUtils;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.api.properties.ComponentReferenceProperties;
import org.talend.core.model.components.IComponent;
import org.talend.core.model.process.INode;
import org.talend.core.model.properties.PropertiesFactory;
import org.talend.core.model.properties.Property;
import org.talend.core.model.repository.FakePropertyImpl;
import org.talend.core.ui.component.ComponentsFactoryProvider;
import org.talend.designer.core.generic.constants.IGenericConstants;
import org.talend.designer.core.generic.utils.ComponentsUtils;
import org.talend.designer.core.ui.editor.nodes.Node;
import org.talend.designer.core.ui.editor.process.Process;
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
        ComponentProperties props = ComponentsUtils.getComponentProperties("tSalesforceConnection"); //$NON-NLS-1$
        props.setValue(IGenericConstants.NAME_PROPERTY, namePropertyValue);
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

    @Test
    public void testSynRefProperties() throws Exception {
        Process process = new Process(new FakePropertyImpl());
        IComponent connComponent = ComponentsFactoryProvider.getInstance().get("tSalesforceConnection", "DI");
        INode connNode = new Node(connComponent, process);

        IComponent inputComponent = ComponentsFactoryProvider.getInstance().get("tSalesforceInput", "DI");
        INode inputNode = new Node(inputComponent, process);

        List graphicalNodes = process.getGraphicalNodes();
        graphicalNodes.add(connNode);
        graphicalNodes.add(inputNode);
        process.getGeneratingNodes();

        ComponentProperties inputComponentProperties = inputNode.getComponentProperties();
        GenericConnectionUtil.synRefProperties(inputComponentProperties, process);

        // there is not any reference.
        ComponentReferenceProperties<?> refProperties = (ComponentReferenceProperties<?>) inputComponentProperties
                .getProperties("connection.referencedComponent");
        assertNull(refProperties.componentInstanceId.getValue());
        assertNull(refProperties.getReference());

        // there is a componentInstanceId but the component with it does not exist.
        refProperties.componentInstanceId.setValue("doesnotexistcomponent");
        GenericConnectionUtil.synRefProperties(inputComponentProperties, process);
        assertNull(refProperties.getReference());

        // there is a component with componentInstanceId but reference is null.
        refProperties.componentInstanceId.setValue(connNode.getUniqueName());
        GenericConnectionUtil.synRefProperties(inputComponentProperties, process);
        assertEquals(connNode.getComponentProperties(), refProperties.getReference());

        // there is a component with componentInstanceId and reference is not null but reference is wrong.
        refProperties.componentInstanceId.setValue(connNode.getUniqueName());
        TestProperties wrongProps = (TestProperties) new TestProperties("test").init();
        refProperties.setReference(wrongProps);
        GenericConnectionUtil.synRefProperties(inputComponentProperties, process);
        assertEquals(connNode.getComponentProperties(), refProperties.getReference());

        // there is a component with componentInstanceId and reference is right.
        refProperties.componentInstanceId.setValue(connNode.getUniqueName());
        refProperties.setReference(connNode.getComponentProperties());
        GenericConnectionUtil.synRefProperties(inputComponentProperties, process);
        assertEquals(connNode.getComponentProperties(), refProperties.getReference());
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
