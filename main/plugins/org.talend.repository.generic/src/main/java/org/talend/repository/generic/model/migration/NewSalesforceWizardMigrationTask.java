// ============================================================================
//
// Copyright (C) 2006-2016 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.repository.generic.model.migration;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.apache.avro.Schema;
import org.eclipse.core.runtime.Path;
import org.talend.commons.exception.ExceptionHandler;
import org.talend.commons.runtime.model.components.IComponentConstants;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.api.service.ComponentService;
import org.talend.components.api.wizard.ComponentWizard;
import org.talend.core.model.metadata.builder.connection.Connection;
import org.talend.core.model.metadata.builder.connection.MetadataTable;
import org.talend.core.model.properties.ConnectionItem;
import org.talend.core.model.properties.Item;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.model.repository.IRepositoryViewObject;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.core.utils.ReflectionUtils;
import org.talend.cwm.helper.ConnectionHelper;
import org.talend.daikon.NamedThing;
import org.talend.daikon.exception.TalendRuntimeException;
import org.talend.daikon.properties.PropertiesDynamicMethodHelper;
import org.talend.daikon.properties.Property;
import org.talend.designer.core.generic.constants.IGenericConstants;
import org.talend.designer.core.generic.utils.ComponentsUtils;
import org.talend.designer.core.generic.utils.SchemaUtils;
import org.talend.repository.generic.internal.IGenericWizardInternalService;
import org.talend.repository.generic.internal.service.GenericWizardInternalService;
import org.talend.repository.generic.model.genericMetadata.GenericConnection;
import org.talend.repository.generic.model.genericMetadata.GenericConnectionItem;

import orgomg.cwm.objectmodel.core.CoreFactory;
import orgomg.cwm.objectmodel.core.TaggedValue;

/**
 * created by hcyi on Apr 11, 2016 Detailled comment
 *
 */
public class NewSalesforceWizardMigrationTask extends NewGenericWizardMigrationTask {

    public static final String SCHEMA_SCHEMA = "main.schema"; //$NON-NLS-1$

    public static final String REFLECTION_SALESFORCE_MODULE_PROPERTIES = "org.talend.components.salesforce.SalesforceModuleProperties"; //$NON-NLS-1$

    public static final String TYPE_NAME = "salesforce"; //$NON-NLS-1$

    public static final String CONNECTION_COMPONENT_NAME = "tSalesforceConnection"; //$NON-NLS-1$

    @Override
    public List<ERepositoryObjectType> getTypes() {
        List<ERepositoryObjectType> toReturn = new ArrayList<>();
        toReturn.add(ERepositoryObjectType.METADATA_SALESFORCE_SCHEMA);
        return toReturn;
    }

    @Override
    public ExecutionResult execute(Item item) {
        ComponentService service = ComponentsUtils.getComponentService();
        Properties props = getPropertiesFromFile();
        if (item instanceof ConnectionItem) {
            boolean modify = false;
            GenericConnectionItem genericConnectionItem = null;
            ConnectionItem connectionItem = (ConnectionItem) item;
            Connection connection = connectionItem.getConnection();
            // Init
            genericConnectionItem = initGenericConnectionItem(connectionItem);
            genericConnectionItem.setTypeName(TYPE_NAME);
            GenericConnection genericConnection = initGenericConnection(connection);
            initProperty(connectionItem, genericConnectionItem);

            ComponentWizard componentWizard = service.getComponentWizard(TYPE_NAME, genericConnectionItem.getProperty().getId());            
            ComponentProperties componentProperties = (ComponentProperties) componentWizard.getForms().get(0).getProperties();
            componentProperties.init();

            // Update
            modify = updateComponentProperties(connection, componentProperties, props);
            NamedThing nt = componentProperties.getProperty("loginType"); //$NON-NLS-1$
            if (nt instanceof Property) {
                Property property = (Property) nt;
                if ("OAuth2".equals(property.getValue())) { //$NON-NLS-1$
                    property.setValue("OAuth"); //$NON-NLS-1$
                    componentProperties.setValue("endpoint", "https://login.salesforce.com/services/oauth2"); //$NON-NLS-1$//$NON-NLS-2$
                }
            }
            // set empty value instead of default null value, this will add automatically the double quotes in the job when drag&drop metadata
            componentProperties.setValue("userPassword.securityKey", ""); //$NON-NLS-1$ //$NON-NLS-2$
            Property property = componentProperties.getValuedProperty("userPassword.securityKey"); //$NON-NLS-1$
            property.setTaggedValue(IGenericConstants.REPOSITORY_VALUE, "securityKey"); //$NON-NLS-1$
            genericConnection.setCompProperties(componentProperties.toSerialized());
            genericConnectionItem.setConnection(genericConnection);
            updateMetadataTable(connection, genericConnection, componentProperties);
            if (modify) {
                try {
                    ProxyRepositoryFactory factory = ProxyRepositoryFactory.getInstance();
                    IRepositoryViewObject object = factory.getLastVersion(item.getProperty().getId(),
                            ERepositoryObjectType.METADATA_SALESFORCE_SCHEMA.getFolder(),
                            ERepositoryObjectType.METADATA_SALESFORCE_SCHEMA);
                    if (object != null) {
                        factory.deleteObjectPhysical(object);
                    }
                    if (genericConnectionItem != null && connectionItem != null) {
                        factory.create(genericConnectionItem, new Path(connectionItem.getState().getPath()), true);
                    }
                    return ExecutionResult.SUCCESS_WITH_ALERT;
                } catch (Exception e) {
                    ExceptionHandler.process(e);
                    return ExecutionResult.FAILURE;
                }
            }
        }
        return ExecutionResult.NOTHING_TO_DO;
    }

    @Override
    protected Properties getPropertiesFromFile() {
        Properties props = new Properties();
        InputStream in = getClass().getResourceAsStream("NewSalesforceWizardMigrationTask.properties");//$NON-NLS-1$
        try {
            props.load(in);
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return props;
    }

    @Override
    protected boolean updateMetadataTable(Connection oldConnection, Connection genericConnection,
            ComponentProperties componentProperties) {
        boolean modified = false;
        if (oldConnection == null || genericConnection == null || componentProperties == null) {
            return modified;
        }
        Set<MetadataTable> tables = ConnectionHelper.getTables(oldConnection);
        Set<MetadataTable> newTables = new HashSet<>();
        newTables.addAll(tables);
        for (MetadataTable metaTable : newTables) {
            try {
                Object object = ReflectionUtils.newInstance(REFLECTION_SALESFORCE_MODULE_PROPERTIES, componentProperties
                        .getClass().getClassLoader(), new Object[] { metaTable.getName() });
                if (object != null && object instanceof ComponentProperties) {
                    ComponentProperties salesforceModuleProperties = (ComponentProperties) object;
                    salesforceModuleProperties.getProperties("connection").copyValuesFrom(componentProperties); //$NON-NLS-1$
                    copyTaggedValues(componentProperties, salesforceModuleProperties.getProperties("connection")); //$NON-NLS-1$
                    NamedThing tmp = salesforceModuleProperties.getProperty("moduleName"); //$NON-NLS-1$
                    ((Property) tmp).setTaggedValue(IGenericConstants.REPOSITORY_VALUE, "moduleName"); //$NON-NLS-1$
                    ((Property) tmp).setValue(metaTable.getLabel());
                    
                    TaggedValue serializedPropsTV = CoreFactory.eINSTANCE.createTaggedValue();
                    serializedPropsTV.setTag(IComponentConstants.COMPONENT_PROPERTIES_TAG);
                    serializedPropsTV.setValue(salesforceModuleProperties.toSerialized());
                    metaTable.getTaggedValue().add(serializedPropsTV);
                    TaggedValue schemaPropertyTV = CoreFactory.eINSTANCE.createTaggedValue();
                    schemaPropertyTV.setTag(IComponentConstants.COMPONENT_SCHEMA_TAG);
                    schemaPropertyTV.setValue(SCHEMA_SCHEMA);
                    metaTable.getTaggedValue().add(schemaPropertyTV);
                    Schema schema = SchemaUtils.convertTalendSchemaIntoComponentSchema(metaTable);
                    salesforceModuleProperties.setValue(SCHEMA_SCHEMA, schema);
                    ((orgomg.cwm.objectmodel.core.Package) genericConnection).getOwnedElement().add(metaTable);
                    modified = true;
                }
            } catch (Exception e) {
                ExceptionHandler.process(e);
            }
        }
        return modified;
    }

    /**
     * Copy all of the values from the specified {@link Properties} object. This includes the values from any nested
     * objects. This can be used even if the {@code Properties} objects are not the same class. Fields that are not
     * present in the this {@code Properties} object are ignored.
     * 
     * @param props
     */
    public void copyTaggedValues(org.talend.daikon.properties.Properties source, org.talend.daikon.properties.Properties target) {
        for (NamedThing otherProp : source.getProperties()) {
            NamedThing thisProp = target.getProperty(otherProp.getName());
            if (thisProp == null) {
                try {
                    Class otherClass = otherProp.getClass();

                    if (Property.class.isAssignableFrom(otherClass)) {
                        Constructor c = otherClass.getConstructor(String.class);
                        thisProp = (NamedThing) c.newInstance(otherProp.getName());
                    } else if (org.talend.daikon.properties.Properties.class.isAssignableFrom(otherClass)) {
                        // Look for single arg String, but an inner class will have a Properties as first arg
                        Constructor constructors[] = otherClass.getConstructors();
                        for (Constructor c : constructors) {
                            Class pts[] = c.getParameterTypes();
                            if (pts.length == 1 && String.class.isAssignableFrom(pts[0])) {
                                thisProp = (NamedThing) c.newInstance(otherProp.getName());
                                break;
                            }
                            if (pts.length == 2 && org.talend.daikon.properties.Properties.class.isAssignableFrom(pts[0])
                                    && String.class.isAssignableFrom(pts[1])) {
                                thisProp = (NamedThing) c.newInstance(this, otherProp.getName());
                                break;
                            }
                        }
                    } else {
                        TalendRuntimeException.unexpectedException("Unexpected property class: " + otherProp.getClass()
                                + " prop: " + otherProp);
                    }

                    try {
                        Field f = getClass().getField(otherProp.getName());
                        f.set(this, thisProp);
                    } catch (NoSuchFieldException e) {
                        // A field exists in the other that's not in ours, just ignore it
                        continue;
                    }
                } catch (Exception e) {
                    TalendRuntimeException.unexpectedException(e);
                }
            }
            if (otherProp instanceof org.talend.daikon.properties.Properties) {
                copyTaggedValues((org.talend.daikon.properties.Properties) otherProp,
                        ((org.talend.daikon.properties.Properties) thisProp));
            } else {
                Object value = ((Property) otherProp).getTaggedValue(IGenericConstants.REPOSITORY_VALUE);
                if (value != null) {
                    ((Property) thisProp).setTaggedValue(IGenericConstants.REPOSITORY_VALUE, value);
                }
            }
        }

    }

    @Override
    public Date getOrder() {
        GregorianCalendar gc = new GregorianCalendar(2016, 4, 11, 12, 0, 0);
        return gc.getTime();
    }
}
