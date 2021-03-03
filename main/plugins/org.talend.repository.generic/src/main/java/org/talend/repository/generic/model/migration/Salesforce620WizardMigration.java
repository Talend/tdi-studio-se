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
package org.talend.repository.generic.model.migration;

import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.emf.common.util.EList;
import org.talend.commons.exception.ExceptionHandler;
import org.talend.commons.runtime.model.components.IComponentConstants;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.api.service.ComponentService;
import org.talend.components.api.wizard.ComponentWizard;
import org.talend.core.model.metadata.builder.connection.MetadataTable;
import org.talend.core.model.migration.AbstractItemMigrationTask;
import org.talend.core.model.properties.Item;
import org.talend.core.model.utils.ContextParameterUtils;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.core.runtime.util.GenericTypeUtils;
import org.talend.core.utils.ReflectionUtils;
import org.talend.core.utils.TalendQuoteUtils;
import org.talend.cwm.helper.PackageHelper;
import org.talend.daikon.NamedThing;
import org.talend.daikon.properties.Properties;
import org.talend.daikon.properties.property.EnumProperty;
import org.talend.daikon.properties.property.Property;
import org.talend.daikon.properties.property.PropertyValueEvaluator;
import org.talend.daikon.serialize.PostDeserializeSetup;
import org.talend.designer.core.generic.utils.ComponentsUtils;
import org.talend.repository.generic.model.genericMetadata.GenericConnection;
import org.talend.repository.generic.model.genericMetadata.GenericConnectionItem;
import org.talend.repository.generic.util.GenericWizardServiceFactory;
import orgomg.cwm.objectmodel.core.TaggedValue;

/**
 * created by nrousseau on May 30, 2016 Detailled comment
 *
 */
public class Salesforce620WizardMigration extends AbstractItemMigrationTask {

    /*
     * (non-Javadoc)
     *
     * @see org.talend.migration.IMigrationTask#getOrder()
     */
    @Override
    public Date getOrder() {
        GregorianCalendar gc = new GregorianCalendar(2016, 5, 30, 12, 0, 0);
        return gc.getTime();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.migration.AbstractItemMigrationTask#execute(org.talend.core.model.properties.Item)
     */
    @Override
    public ExecutionResult execute(Item item) {
        if (GenericWizardServiceFactory.getGenericWizardService().isGenericItem(item)) {
            try {
                GenericConnectionItem connectionItem = (GenericConnectionItem) item;
                GenericConnection connection = (GenericConnection) connectionItem.getConnection();
                String serialized = connection.getCompProperties();
                ComponentService service = ComponentsUtils.getComponentService();
                ComponentWizard componentWizard = service.getComponentWizard(NewSalesforceWizardMigrationTask.TYPE_NAME, item
                        .getProperty().getId());
                ComponentProperties newProperties = (ComponentProperties) componentWizard.getForms().get(0).getProperties();
                newProperties.init();
                ComponentProperties properties = loadProperties(serialized, newProperties);
                updateSubProperties(properties, newProperties);
                newProperties.copyValuesFrom(properties, true, false);
                connection.setCompProperties(newProperties.toSerialized());
                Set<MetadataTable> tables = new HashSet<MetadataTable>();
                PackageHelper.getAllTables(connection, tables);
                for (MetadataTable table : tables) {
                    EList<TaggedValue> values = table.getTaggedValue();
                    for (TaggedValue value : values) {
                        if (IComponentConstants.COMPONENT_PROPERTIES_TAG.equals(value.getTag())) {
                            Object object = ReflectionUtils.newInstance(
                                    NewSalesforceWizardMigrationTask.REFLECTION_SALESFORCE_MODULE_PROPERTIES, newProperties
                                            .getClass().getClassLoader(), new Object[] { table.getName() });
                            if (object != null && object instanceof ComponentProperties) {
                                ComponentProperties newSalesforceModuleProperties = (ComponentProperties) object;
                                ComponentProperties moduleProperties = loadProperties(value.getValue(),
                                        newSalesforceModuleProperties);
                                updateSubProperties(moduleProperties, newSalesforceModuleProperties);
                                newSalesforceModuleProperties.copyValuesFrom(moduleProperties, true, false);
                                value.setValue(newSalesforceModuleProperties.toSerialized());
                            }

                        }
                    }
                }
                ProxyRepositoryFactory.getInstance().save(connectionItem, true);
                return ExecutionResult.SUCCESS_NO_ALERT;
            } catch (Exception e) {
                ExceptionHandler.process(e);
                return ExecutionResult.FAILURE;

            }
        }
        return ExecutionResult.NOTHING_TO_DO;
    }

    /**
     * DOC nrousseau Comment method "loadProperties".
     *
     * @param serialized
     * @param newProperties
     * @return
     */
    private ComponentProperties loadProperties(String serialized, ComponentProperties newProperties) {
        return Properties.Helper.fromSerializedPersistent(serialized, ComponentProperties.class,
                new PostDeserializeSetup() {

                    @Override
                    public void setup(Object properties) {
                        ((Properties)properties).setValueEvaluator(new PropertyValueEvaluator() {

                            @Override
                            public Object evaluate(Property property, Object storedValue) {
                                Property newProperty = (Property) newProperties.getProperty(property.getName());
                                if (newProperty == null) {
                                    newProperty = newProperties.getValuedProperty("connection."+property.getName()); //$NON-NLS-1$
                                    if (newProperty == null) {
                                        if ("useProxy".equals(property.getName())) { //$NON-NLS-1$
                                            return false;
                                        }
                                        return null;
                                    }
                                }
                                if (storedValue instanceof String) {

                                    if (GenericTypeUtils.isEnumType(newProperty)) {
                                        if (newProperty != null) {
                                            List<?> propertyPossibleValues = ((Property<?>) newProperty).getPossibleValues();
                                            if (propertyPossibleValues != null) {
                                                for (Object possibleValue : propertyPossibleValues) {
                                                    if (possibleValue.toString().equals(storedValue)) {
                                                        property.setStoredValue(possibleValue);
                                                        return possibleValue;
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                                if (GenericTypeUtils.isBooleanType(newProperty)) {
                                    return false;
                                }
                                return storedValue;
                            }

                        });
                    }
                }).object;
    }

    /**
     * DOC nrousseau Comment method "updateSubProperties".
     *
     * @param properties
     * @param newProperties
     */
    private void updateSubProperties(ComponentProperties properties, ComponentProperties newProperties) {
        if (newProperties == null) {
            return;
        }
        for (NamedThing nt : properties.getProperties()) {
            if (nt instanceof Property) {
                Property property = (Property) nt;
                Object storedValue = property.getStoredValue();
                if (storedValue instanceof String) {
                    String stringValue = (String) storedValue;
                    if (ContextParameterUtils.isContainContextParam(stringValue)) {
                        continue;
                    }
                    Property newProperty = (Property) newProperties.getProperty(property.getName());
                    if (newProperty != null) {
                        if (GenericTypeUtils.isBooleanType(newProperty)) {
                            if (stringValue.isEmpty()) {
                                property.setValue(Boolean.FALSE);
                            } else {
                                property.setValue(new Boolean(stringValue));
                            }
                        } else if (GenericTypeUtils.isEnumType(newProperty) && (!(newProperty instanceof EnumProperty))) {
                            property.setStoredValue(TalendQuoteUtils.removeQuotes(stringValue));
                        } else if (GenericTypeUtils.isEnumType(newProperty)) {
                            List<?> propertyPossibleValues = ((Property<?>) newProperty).getPossibleValues();
                            if (propertyPossibleValues != null) {
                                for (Object possibleValue : propertyPossibleValues) {
                                    if (possibleValue.toString().equals(storedValue)) {
                                        property.setStoredValue(possibleValue);
                                        break;
                                    }
                                }
                            }
                        }

                    }
                } else {
                    Property newProperty = (Property) newProperties.getProperty(property.getName());
                    if (GenericTypeUtils.isBooleanType(newProperty)) {
                        // value can only be null (so false)
                        property.setValue(Boolean.FALSE);
                    }
                }
            } else if (nt instanceof ComponentProperties) {
                updateSubProperties((ComponentProperties) nt, (ComponentProperties) newProperties.getProperty(nt.getName()));
            }
        }
    }

}
