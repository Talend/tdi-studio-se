// ============================================================================
//
// Copyright (C) 2006-2017 Talend Inc. - www.talend.com
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

import java.util.HashSet;
import java.util.Set;

import org.junit.Test;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.daikon.properties.Properties;
import org.talend.daikon.properties.property.Property;
import org.talend.daikon.serialize.SerializerDeserializer;
import org.talend.designer.core.generic.constants.IGenericConstants;
import org.talend.designer.core.generic.utils.ComponentsUtils;
import org.talend.designer.core.model.utils.emf.talendfile.ContextParameterType;
import org.talend.designer.core.model.utils.emf.talendfile.ContextType;
import org.talend.designer.core.model.utils.emf.talendfile.impl.TalendFileFactoryImpl;
import org.talend.metadata.managment.ui.model.IConnParamName;
import org.talend.metadata.managment.ui.utils.GenericConnParamName;
import org.talend.repository.generic.model.genericMetadata.GenericConnection;
import org.talend.repository.generic.model.genericMetadata.GenericMetadataFactory;

/**
 * created by ycbai on 2016年2月16日 Detailled comment
 *
 */
public class GenericContextUtilTest {

    @Test
    public void testExportAndRevertContextForComponentProperties() {
        String prefix = "testConn"; //$NON-NLS-1$
        GenericConnection connection = GenericMetadataFactory.eINSTANCE.createGenericConnection();

        ComponentProperties props = ComponentsUtils.getComponentProperties("tSalesforceConnection"); //$NON-NLS-1$
        props.setValue("endpoint", "sf_endpoint"); //$NON-NLS-1$ //$NON-NLS-2$
        props.setValue("userPassword.userId", "sf_user"); //$NON-NLS-1$ //$NON-NLS-2$
        connection.setCompProperties(props.toSerialized());

        Set<IConnParamName> contextParams = new HashSet<>();
        contextParams.add(createConnParamName("endpoint")); //$NON-NLS-1$
        contextParams.add(createConnParamName("userPassword.userId")); //$NON-NLS-1$

        // Test export context
        GenericContextUtil.setPropertiesForContextMode(prefix, connection, contextParams);

        ComponentProperties deserProps = getPropertiesFromConnection(connection);
        Property<?> endpointProperty = deserProps.getValuedProperty("endpoint"); //$NON-NLS-1$
        Property<?> userIdProperty = deserProps.getValuedProperty("userPassword.userId"); //$NON-NLS-1$
        assertEquals("context.testConn_endpoint", endpointProperty.getValue()); //$NON-NLS-1$
        assertEquals("context.testConn_userPassword_userId", userIdProperty.getValue()); //$NON-NLS-1$
        assertEquals(endpointProperty.getTaggedValue(IGenericConstants.IS_CONTEXT_MODE), true);
        assertEquals(userIdProperty.getTaggedValue(IGenericConstants.IS_CONTEXT_MODE), true);

        // Test revert context
        ContextType contextType = TalendFileFactoryImpl.eINSTANCE.createContextType();
        contextType.getContextParameter().add(createContextParameterType("testConn_endpoint", "sf_endpoint")); //$NON-NLS-1$ //$NON-NLS-2$
        contextType.getContextParameter().add(createContextParameterType("testConn_userPassword_userId", "sf_user")); //$NON-NLS-1$ //$NON-NLS-2$

        GenericContextUtil.revertPropertiesForContextMode(connection, contextType);

        deserProps = getPropertiesFromConnection(connection);
        endpointProperty = deserProps.getValuedProperty("endpoint"); //$NON-NLS-1$
        userIdProperty = deserProps.getValuedProperty("userPassword.userId"); //$NON-NLS-1$
        assertEquals("sf_endpoint", endpointProperty.getValue()); //$NON-NLS-1$
        assertEquals("sf_user", userIdProperty.getValue()); //$NON-NLS-1$
        assertEquals(endpointProperty.getTaggedValue(IGenericConstants.IS_CONTEXT_MODE), false);
        assertEquals(userIdProperty.getTaggedValue(IGenericConstants.IS_CONTEXT_MODE), false);
    }

    private ComponentProperties getPropertiesFromConnection(GenericConnection connection) {
        SerializerDeserializer.Deserialized<ComponentProperties> d = Properties.Helper
                .fromSerializedPersistent(connection.getCompProperties(), ComponentProperties.class);
        ComponentProperties deserProps = d.object;
        return deserProps;
    }

    private GenericConnParamName createConnParamName(String paramName) {
        GenericConnParamName connParamName = new GenericConnParamName();
        connParamName.setName(paramName);
        connParamName.setContextVar(getValidContextVarName(paramName));
        return connParamName;
    }

    private ContextParameterType createContextParameterType(String paramName, String paramValue) {
        ContextParameterType contextParameter = TalendFileFactoryImpl.eINSTANCE.createContextParameterType();
        contextParameter.setName(paramName);
        contextParameter.setRawValue(paramValue);
        return contextParameter;
    }

    private String getValidContextVarName(String paramName) {
        return paramName.replace(IGenericConstants.EXP_SEPARATOR, IGenericConstants.UNDERLINE_SEPARATOR);
    }

}
