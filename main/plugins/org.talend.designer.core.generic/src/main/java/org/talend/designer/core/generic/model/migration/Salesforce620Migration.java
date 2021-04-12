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
package org.talend.designer.core.generic.model.migration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import org.apache.commons.lang.ArrayUtils;
import org.talend.commons.exception.ExceptionHandler;
import org.talend.commons.exception.PersistenceException;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.core.language.ECodeLanguage;
import org.talend.core.model.components.ComponentUtilities;
import org.talend.core.model.components.ModifyComponentsAction;
import org.talend.core.model.components.conversions.IComponentConversion;
import org.talend.core.model.components.filters.IComponentFilter;
import org.talend.core.model.components.filters.NameComponentFilter;
import org.talend.core.model.migration.AbstractJobMigrationTask;
import org.talend.core.model.properties.Item;
import org.talend.core.model.utils.ContextParameterUtils;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.core.runtime.util.GenericTypeUtils;
import org.talend.core.utils.TalendQuoteUtils;
import org.talend.daikon.NamedThing;
import org.talend.daikon.properties.Properties;
import org.talend.daikon.properties.property.EnumProperty;
import org.talend.daikon.properties.property.Property;
import org.talend.daikon.properties.property.PropertyValueEvaluator;
import org.talend.daikon.serialize.PostDeserializeSetup;
import org.talend.daikon.serialize.SerializerDeserializer;
import org.talend.designer.core.generic.utils.ComponentsUtils;
import org.talend.designer.core.model.utils.emf.talendfile.ElementParameterType;
import org.talend.designer.core.model.utils.emf.talendfile.NodeType;
import org.talend.designer.core.model.utils.emf.talendfile.ProcessType;

/**
 * created by nrousseau on May 25, 2016 Detailled comment
 *
 */
public class Salesforce620Migration extends AbstractJobMigrationTask {

    /*
     * (non-Javadoc)
     *
     * @see org.talend.migration.IMigrationTask#getOrder()
     */
    @Override
    public Date getOrder() {
        GregorianCalendar gc = new GregorianCalendar(2016, 05, 25, 12, 0, 0);
        return gc.getTime();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.migration.AbstractItemMigrationTask#execute(org.talend.core.model.properties.Item)
     */
    @Override
    public ExecutionResult execute(Item item) {
        ProcessType processType = getProcessType(item);
        if (getProject().getLanguage() != ECodeLanguage.JAVA || processType == null) {
            return ExecutionResult.NOTHING_TO_DO;
        }

        String[] componentsName = new String[] {
                "tSalesforceConnection", "tSalesforceBulkExec", "tSalesforceGetDeleted", "tSalesforceGetUpdated", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
                "tSalesforceInput", "tSalesforceOutput", "tSalesforceOutputBulk", "tSalesforceOutputBulkExec" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
        IComponentConversion changeJDBCDriverJarType = new IComponentConversion() {

            @Override
            public void transform(NodeType node) {
                ElementParameterType elemParamType = ComponentUtilities.getNodeProperty(node, "PROPERTIES");
                String propertiesString = elemParamType.getValue();
                SerializerDeserializer.Deserialized<ComponentProperties> fromSerialized = Properties.Helper.fromSerializedPersistent(propertiesString,
                        ComponentProperties.class, new PostDeserializeSetup() {

                            @Override
                            public void setup(Object properties) {
                                ((Properties)properties).setValueEvaluator(new PropertyValueEvaluator() {

                                    @Override
                                    public Object evaluate(Property property, Object storedValue) {
                                        if (storedValue instanceof String) {
                                            if (GenericTypeUtils.isEnumType(property)) {
                                                ComponentProperties newProperties = ComponentsUtils.getComponentProperties(node
                                                        .getComponentName());

                                                Property newProperty = (Property) newProperties.getProperty(property.getName());
                                                if (newProperty == null) {
                                                    newProperty = (Property) newProperties.getProperty("connection.loginType");
                                                }
                                                if (newProperty != null) {
                                                    List<?> propertyPossibleValues = ((Property<?>) newProperty)
                                                            .getPossibleValues();
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
                                        return storedValue;
                                    }

                                });
                            }
                        });
                ComponentProperties newProperties = ComponentsUtils.getComponentProperties(node.getComponentName());
                updateSubProperties(fromSerialized.object, newProperties);
                newProperties.copyValuesFrom(fromSerialized.object, true, false);
                NamedThing nt = newProperties.getProperty("module.moduleName");
                if (nt != null && nt instanceof Property) {
                    Property moduleNameProperty = (Property) nt;
                    String moduleName = (String) moduleNameProperty.getValue();
                    if (ContextParameterUtils.isContainContextParam(moduleName)) {
                        moduleName = TalendQuoteUtils.removeQuotes(moduleName);
                    } else {
                        moduleName = TalendQuoteUtils.addPairQuotesIfNotExist(moduleName);
                    }
                    moduleNameProperty.setStoredValue(moduleName);
                }
                nt = newProperties.getProperty("upsertRelationTable.columnName");
                if (nt != null && nt instanceof Property) {
                    Property moduleNameProperty = (Property) nt;
                    if (moduleNameProperty.getPossibleValues() == null || moduleNameProperty.getPossibleValues().isEmpty()) {
                        List<String> columns = new ArrayList<String>();
                        if (moduleNameProperty.getValue() instanceof String) {
                            String column = (String) moduleNameProperty.getValue();
                            columns.add(column);
                        } else if (moduleNameProperty.getValue() instanceof List) {
                            columns.addAll((Collection<? extends String>) moduleNameProperty.getValue());
                        }
                        moduleNameProperty.setPossibleValues(columns);
                    }
                }
                elemParamType.setValue(newProperties.toSerialized());
            }
        };

        boolean modified = false;
        for (Object obj : processType.getNode()) {
            if (obj != null && obj instanceof NodeType) {
                String componentName = ((NodeType) obj).getComponentName();
                if (ArrayUtils.contains(componentsName, componentName)) {
                    IComponentFilter filter = new NameComponentFilter(componentName);
                    modified = ModifyComponentsAction.searchAndModify((NodeType) obj, filter,
                            Arrays.<IComponentConversion> asList(changeJDBCDriverJarType))
                            || modified;
                }
            }
        }
        if (modified) {
            try {
                ProxyRepositoryFactory.getInstance().save(item, true);
                return ExecutionResult.SUCCESS_NO_ALERT;
            } catch (PersistenceException e) {
                ExceptionHandler.process(e);
                return ExecutionResult.FAILURE;
            }
        }
        return ExecutionResult.SUCCESS_WITH_ALERT;

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
                }
            } else if (nt instanceof ComponentProperties) {
                updateSubProperties((ComponentProperties) nt, (ComponentProperties) newProperties.getProperty(nt.getName()));
            }
        }
    }

}
