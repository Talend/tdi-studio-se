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

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Test;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.core.model.metadata.builder.connection.ConnectionFactory;
import org.talend.core.model.metadata.builder.connection.DatabaseConnection;
import org.talend.daikon.NamedThing;
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

    @Test
    public void testUpdateCompPropertiesForContextMode() {
        DatabaseConnection dbconnection = ConnectionFactory.eINSTANCE.createDatabaseConnection();
        String jdbcConnectProperties = getJDBCConnectProperties();
        dbconnection.setCompProperties(jdbcConnectProperties);
        ComponentProperties componentProperties = ComponentsUtils.getComponentPropertiesFromSerialized(jdbcConnectProperties,
                dbconnection);
        setComponentProperty(componentProperties, "name", "context.test_connection_name");
        setComponentProperty(componentProperties, "jdbcUrl", "context.test_connection_jdbcUrl");
        setComponentProperty(componentProperties, "drivers", "context.test_connection_drivers");
        setComponentProperty(componentProperties, "userId", "context.test_connection_userId");
        setComponentProperty(componentProperties, "password", "context.test_connection_password");

        String serializedProperties = componentProperties.toSerialized();
        dbconnection.setCompProperties(serializedProperties);

        Map<String, String> changeMap = new HashMap<String, String>();
        changeMap.put("context.test_connection_name", "context.test_connection_changedname");
        changeMap.put("context.test_connection_jdbcUrl", "context.test_connection_changedjdbcUrl");
        changeMap.put("context.test_connection_drivers", "context.test_connection_changeddrivers");
        changeMap.put("context.test_connection_userId", "context.test_connection_changeduserId");
        changeMap.put("context.test_connection_password", "context.test_connection_changedpassword");

        GenericContextUtil.updateCompPropertiesForContextMode(dbconnection, changeMap);
        ComponentProperties compProperties = ComponentsUtils
                .getComponentPropertiesFromSerialized(dbconnection.getCompProperties(), dbconnection);
        assertEquals("context.test_connection_changedname", getComponentProperty(compProperties, "name").getStoredValue());
        assertEquals("context.test_connection_changedjdbcUrl", getComponentProperty(compProperties, "jdbcUrl").getStoredValue());
        assertEquals("context.test_connection_changeddrivers", getComponentProperty(compProperties, "drivers").getStoredValue());
        assertEquals("context.test_connection_changeduserId", getComponentProperty(compProperties, "userId").getStoredValue());
        assertEquals("context.test_connection_changedpassword",
                getComponentProperty(compProperties, "password").getStoredValue());
    }

    private static Property getComponentProperty(ComponentProperties componentProperties, String propertyname) {
        List<Property> propertyList = new ArrayList<Property>();
        findOutProperty(componentProperties, propertyname, propertyList);
        return propertyList.get(0);
    }

    private static void setComponentProperty(ComponentProperties componentProperties, String propertyname, String propertyvalue) {
        List<Property> propertyList = new ArrayList<Property>();
        findOutProperty(componentProperties, propertyname, propertyList);
        Property property = propertyList.get(0);
        property.setStoredValue(propertyvalue);
    }

    private static void findOutProperty(Properties componentProperties, String propertyname, List<Property> result) {
        for (NamedThing namedThing : componentProperties.getProperties()) {
            if (namedThing instanceof Property) {
                Property property = (Property) namedThing;
                if (property.getName().equals(propertyname)) {
                    result.add(property);
                    return;
                }
            } else if (namedThing instanceof Properties) {
                Properties compProp = (Properties) namedThing;
                findOutProperty(compProp, propertyname, result);
            }
        }
    }

    private static String getJDBCConnectProperties() {
        String compProperties = "{\"@type\":\"org.talend.components.jdbc.wizard.JDBCConnectionWizardProperties\","
                + "\"name\":{\"@type\":\"org.talend.daikon.properties.property.StringProperty\",\"possibleValues2\":null,\"flags\":null,\"storedValue\":null,\"storedDefaultValue\":null,\""
                + "children\":{\"@type\":\"java.util.ArrayList\"},\"taggedValues\":{\"@type\":\"java.util.HashMap\",\"SUPPORT_CONTEXT\":false,\"IS_DYNAMIC\":false,\"REPOSITORY_VALUE\":\"name\"},\"size\":-1,\"occurMinTimes\":1,\"occurMaxTimes\":1,"
                + "\"precision\":0,\"pattern\":null,\"nullable\":false,\"possibleValues\":null,\"currentType\":\"java.lang.String\",\"name\":\"name\",\"tags\":null},\"repositoryLocation\":\"_W-ztsIcyEeqy_c-Mq47LSw\",\""
                + "connection\":{\"jdbcUrl\":{\"@type\":\"org.talend.daikon.properties.property.StringProperty\",\"possibleValues2\":null,\"flags\":null,\"storedValue\":\"jdbc:\",\"storedDefaultValue\":null,\"children\":{\"@type\":\"java.util.ArrayList\"},"
                + "\"taggedValues\":{\"@type\":\"java.util.HashMap\",\"SUPPORT_CONTEXT\":true,\"IS_DYNAMIC\":false,\"REPOSITORY_VALUE\":\"jdbcUrl\"},\"size\":-1,\"occurMinTimes\":1,\"occurMaxTimes\":1,\"precision\":0,\"pattern\":null,\"nullable\":false,"
                + "\"possibleValues\":null,\"currentType\":\"java.lang.String\",\"name\":\"jdbcUrl\",\"tags\":null},\"driverTable\":{\"drivers\":{\"flags\":null,\"storedValue\":null,\"storedDefaultValue\":null,\"children\":{\"@type\":\"java.util.ArrayList\"},"
                + "\"taggedValues\":{\"@type\":\"java.util.HashMap\",\"SUPPORT_CONTEXT\":true,\"IS_DYNAMIC\":false,\"REPOSITORY_VALUE\":\"drivers\"},\"size\":-1,\"occurMinTimes\":0,\"occurMaxTimes\":0,\"precision\":0,\"pattern\":null,\"nullable\":false,\"possibleValues\":null,"
                + "\"currentType\":\"java.util.List&lt;java.lang.String&gt;\",\"name\":\"drivers\",\"tags\":null},\"name\":\"driverTable\",\"validationResult\":null,\"tags\":null},\"driverClass\":{\"@type\":\"org.talend.daikon.properties.property.StringProperty\",\"possibleValues2\":null,"
                + "\"flags\":null,\"storedValue\":null,\"storedDefaultValue\":null,\"children\":{\"@type\":\"java.util.ArrayList\"},\"taggedValues\":{\"@type\":\"java.util.HashMap\",\"SUPPORT_CONTEXT\":true,\"IS_DYNAMIC\":false,\"REPOSITORY_VALUE\":\"driverClass\"},\"size\":-1,"
                + "\"occurMinTimes\":1,\"occurMaxTimes\":1,\"precision\":0,\"pattern\":null,\"nullable\":false,\"possibleValues\":null,\"currentType\":\"java.lang.String\",\"name\":\"driverClass\",\"tags\":null},\"userPassword\":{\"useAuth\":{\"flags\":null,\"storedValue\":{\"@type\":\"boolean\","
                + "\"value\":false},\"storedDefaultValue\":null,\"children\":{\"@type\":\"java.util.ArrayList\"},\"taggedValues\":{\"@type\":\"java.util.HashMap\",\"REPOSITORY_VALUE\":\"useAuth\"},\"size\":-1,\"occurMinTimes\":0,\"occurMaxTimes\":0,\"precision\":0,\"pattern\":null,\"nullable\":false,"
                + "\"possibleValues\":null,\"currentType\":\"java.lang.Boolean\",\"name\":\"useAuth\",\"tags\":null},\"userId\":{\"@type\":\"org.talend.daikon.properties.property.StringProperty\",\"possibleValues2\":null,\"flags\":null,\"storedValue\":null,\"storedDefaultValue\":null,\"children\":{\"@type\":\"java.util.ArrayList\"},"
                + "\"taggedValues\":{\"@type\":\"java.util.HashMap\",\"SUPPORT_CONTEXT\":true,\"IS_DYNAMIC\":false,\"REPOSITORY_VALUE\":\"userId\"},\"size\":-1,\"occurMinTimes\":1,\"occurMaxTimes\":1,\"precision\":0,\"pattern\":null,\"nullable\":false,\"possibleValues\":null,\"currentType\":\"java.lang.String\",\"name\":\"userId\","
                + "\"tags\":null},\"password\":{\"@type\":\"org.talend.daikon.properties.property.StringProperty\",\"possibleValues2\":null,\"flags\":{\"@type\":\"java.util.RegularEnumSet\",\"@items\":[{\"@type\":\"org.talend.daikon.properties.property.Property$Flags\",\"name\":\"ENCRYPT\"},{\"@type\":\"org.talend.daikon.properties.property.Property$Flags\","
                + "\"name\":\"SUPPRESS_LOGGING\"}]},\"storedValue\":null,\"storedDefaultValue\":null,\"children\":{\"@type\":\"java.util.ArrayList\"},\"taggedValues\":{\"@type\":\"java.util.HashMap\",\"SUPPORT_CONTEXT\":true,\"IS_DYNAMIC\":false,\"REPOSITORY_VALUE\":\"password\"},\"size\":-1,\"occurMinTimes\":1,\"occurMaxTimes\":1,"
                + "\"precision\":0,\"pattern\":null,\"nullable\":false,\"possibleValues\":null,\"currentType\":\"java.lang.String\",\"name\":\"password\",\"tags\":null},\"needSwitch\":false,\"name\":\"userPassword\",\"validationResult\":null,\"tags\":null},\"useInWizard\":true,\"name\":\"connection\",\"validationResult\":null,"
                + "\"tags\":null},\"mappingFile\":{\"@type\":\"org.talend.daikon.properties.property.StringProperty\",\"possibleValues2\":null,\"flags\":null,\"storedValue\":\"mysql_id\",\"storedDefaultValue\":null,\"children\":{\"@type\":\"java.util.ArrayList\"},\"taggedValues\":{\"@type\":\"java.util.HashMap\","
                + "\"SUPPORT_CONTEXT\":true,\"IS_DYNAMIC\":false,\"REPOSITORY_VALUE\":\"mappingFile\"},\"size\":-1,\"occurMinTimes\":0,\"occurMaxTimes\":0,\"precision\":0,\"pattern\":null,\"nullable\":false,\"possibleValues\":null,\"currentType\":\"java.lang.String\",\"name\":\"mappingFile\",\"tags\":null},\"org.talend.daikon.properties.PropertiesImpl.name\":\"connection\",\"validationResult\":null,\"tags\":null}";
        return compProperties;
    }

}
