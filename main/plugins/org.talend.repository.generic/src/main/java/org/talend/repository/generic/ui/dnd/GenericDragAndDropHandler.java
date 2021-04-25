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

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.talend.commons.exception.ExceptionHandler;
import org.talend.components.api.component.ComponentDefinition;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.api.wizard.ComponentWizard;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.model.components.EComponentType;
import org.talend.core.model.components.IComponent;
import org.talend.core.model.components.IComponentsService;
import org.talend.core.model.metadata.IMetadataTable;
import org.talend.core.model.metadata.MetadataToolHelper;
import org.talend.core.model.metadata.builder.connection.Connection;
import org.talend.core.model.metadata.builder.connection.DatabaseConnection;
import org.talend.core.model.metadata.builder.connection.MetadataTable;
import org.talend.core.model.param.EConnectionParameterName;
import org.talend.core.model.process.IElement;
import org.talend.core.model.process.IElementParameter;
import org.talend.core.model.process.INode;
import org.talend.core.model.properties.ConnectionItem;
import org.talend.core.model.properties.Item;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.model.repository.IRepositoryViewObject;
import org.talend.core.model.repository.RepositoryViewObject;
import org.talend.core.model.utils.AbstractDragAndDropServiceHandler;
import org.talend.core.model.utils.ContextParameterUtils;
import org.talend.core.model.utils.IComponentName;
import org.talend.core.repository.RepositoryComponentSetting;
import org.talend.core.repository.model.repositoryObject.MetadataColumnRepositoryObject;
import org.talend.core.repository.model.repositoryObject.MetadataTableRepositoryObject;
import org.talend.core.runtime.services.IGenericDBService;
import org.talend.core.runtime.services.IGenericWizardService;
import org.talend.core.runtime.util.GenericTypeUtils;
import org.talend.core.utils.TalendQuoteUtils;
import org.talend.daikon.NamedThing;
import org.talend.daikon.properties.Properties;
import org.talend.daikon.properties.property.Property;
import org.talend.designer.core.generic.constants.IGenericConstants;
import org.talend.designer.core.generic.model.Component;
import org.talend.designer.core.generic.model.GenericElementParameter;
import org.talend.designer.core.generic.utils.ComponentsUtils;
import org.talend.designer.core.generic.utils.SchemaUtils;
import org.talend.repository.generic.internal.IGenericWizardInternalService;
import org.talend.repository.generic.internal.service.GenericWizardInternalService;
import org.talend.repository.model.RepositoryNode;

import orgomg.cwm.objectmodel.core.ModelElement;

/**
 *
 * created by hcyi on Oct 26, 2015 Detailled comment
 *
 */
public class GenericDragAndDropHandler extends AbstractDragAndDropServiceHandler {

    public static final String COMPONENT_PREFIX = "t"; //$NON-NLS-1$

    public static final String INPUT = "Input"; //$NON-NLS-1$

    public static final String OUTPUT = "Output"; //$NON-NLS-1$

    @Override
    public boolean canHandle(Connection connection) {
        if (connection == null) {
            return false;
        }
        return connection.getCompProperties() != null;
    }

    @Override
    public Object getComponentValue(Connection connection, String value, IMetadataTable table, String targetComponent,
            Map<Object, Object> contextMap) {
        if (value != null && canHandle(connection)) {
            if (GlobalServiceRegister.getDefault().isServiceRegistered(IGenericWizardService.class)) {
                IGenericWizardService wizardService = (IGenericWizardService) GlobalServiceRegister.getDefault()
                        .getService(IGenericWizardService.class);
                if (wizardService != null && wizardService.isGenericConnection(connection)) {
                    List<ComponentProperties> componentPropertiesList = wizardService.getAllComponentProperties(connection,
                            getSeletetedMetadataTableName(table), true, true, contextMap);
                    return getGenericRepositoryValue(connection, componentPropertiesList, value, table);
                }
            }
        }
        return null;
    }

    private Object getGenericRepositoryValue(Connection connection, List<ComponentProperties> componentProperties, String value,
            IMetadataTable table) {
        if (componentProperties != null && value != null) {
            if (EConnectionParameterName.USERNAME.getName().equals(value)) {
                value = EConnectionParameterName.GENERIC_USERNAME.getDisplayName();
            }
            if (EConnectionParameterName.PASSWORD.getName().equals(value)) {
                value = EConnectionParameterName.GENERIC_PASSWORD.getDisplayName();
            }
            for (ComponentProperties compPro : componentProperties) {
                if (isGenericPropertiesValue(value)) {
                    Properties properties = compPro.getProperties(value);
                    if (properties != null) {
                        return getPropertiesValue(connection, properties, value);
                    }
                } else {
                    Property<?> property = compPro.getValuedProperty(value);
                    if (property != null) {
                        Object paramValue = property.getStoredValue();
                        if (GenericTypeUtils.isStringType(property) || GenericTypeUtils.isObjectType(property)) {
                            if (paramValue != null) {
                                if (property.getName().equals("password")) {
                                    return getPassword(connection, paramValue.toString());
                                }
                                return getRepositoryValueOfStringType(connection, paramValue.toString());
                            } else {
                                return TalendQuoteUtils.addQuotes(""); //$NON-NLS-1$
                            }
                        } else if (GenericTypeUtils.isEnumType(property) && paramValue != null) {
                            return paramValue.toString();
                        }
                        return paramValue;
                    }
                }
            }
            if (value.indexOf(IGenericConstants.EXP_SEPARATOR) != -1) {
                return getGenericRepositoryValue(connection, componentProperties,
                        value.substring(value.indexOf(IGenericConstants.EXP_SEPARATOR) + 1), table);
            }
        }
        if ((connection instanceof DatabaseConnection) && value.equals("TYPE")) {
            return ((DatabaseConnection) connection).getDatabaseType();
        }
        return null;
    }

    private String getPassword(Connection connection, String value) {
        String pass = connection.getValue(value, false);
        if (ContextParameterUtils.isContextMode(connection, value)) {
            return pass;
        }
        return TalendQuoteUtils.addQuotesIfNotExist(pass);
    }

    private Object getPropertiesValue(Connection connection, Properties properties, String value) {
        List<Map> lines = new ArrayList<Map>();
        for (NamedThing nameThing : properties.getProperties()) {
            if (nameThing != null && nameThing instanceof Property) {
                Property property = (Property) nameThing;
                Object paramValue = property.getStoredValue();
                if (GenericTypeUtils.isListStringType(property) && paramValue != null) {
                    List<String> listString = (List<String>) paramValue;
                    for (String pv : listString) {
                        Map<String, Object> line = new LinkedHashMap<String, Object>();
                        if (pv != null) {
                            line.put(property.getName(), getRepositoryValueOfStringType(connection, pv));
                        } else {
                            line.put(property.getName(), TalendQuoteUtils.addQuotes(""));
                        }
                        lines.add(line);
                    }
                } else if (GenericTypeUtils.isStringType(property) || GenericTypeUtils.isObjectType(property)) {
                    Map<String, Object> line = new LinkedHashMap<String, Object>();
                    if (paramValue != null) {
                        line.put(property.getName(), getRepositoryValueOfStringType(connection, paramValue.toString()));
                    } else {
                        line.put(property.getName(), TalendQuoteUtils.addQuotes(""));
                    }
                    lines.add(line);
                } else if (GenericTypeUtils.isEnumType(property) && paramValue != null) {
                    Map<String, Object> line = new LinkedHashMap<String, Object>();
                    line.put(property.getName(), paramValue.toString());
                    lines.add(line);
                }
            }
        }
        return lines;
    }

    private String getSeletetedMetadataTableName(IMetadataTable table) {
        if (table != null) {
            if (table instanceof MetadataTableRepositoryObject) {
                MetadataTableRepositoryObject metaTableRepObj = (MetadataTableRepositoryObject) table;
                if (metaTableRepObj.getTable() != null) {
                    return metaTableRepObj.getTable().getName();
                }
            } else if (table instanceof org.talend.core.model.metadata.MetadataTable) {
                return table.getLabel();
            }
        }
        return null;
    }

    @Override
    public boolean isGenericRepositoryValue(List<ComponentProperties> componentProperties, String paramName) {
        return getGenericRepositoryValue(componentProperties, paramName) != null;
    }

    @Override
    public Object getGenericRepositoryValue(List<ComponentProperties> componentProperties, String paramName) {
        if (componentProperties != null && paramName != null) {
            for (ComponentProperties compPro : componentProperties) {
                if (isGenericPropertiesValue(paramName)) {
                    Properties properties = compPro.getProperties(paramName);
                    if (properties != null) {
                        for (NamedThing nameThing : properties.getProperties()) {
                            if (nameThing != null && nameThing instanceof Property) {
                                Property property = (Property) nameThing;
                                return property.getTaggedValue(IGenericConstants.REPOSITORY_VALUE);
                            }
                        }
                    }
                } else {
                    Property property = compPro.getValuedProperty(paramName);
                    if (property != null) {
                        return property.getTaggedValue(IGenericConstants.REPOSITORY_VALUE);
                    }
                }
            }
            if (paramName.indexOf(IGenericConstants.EXP_SEPARATOR) != -1) {
                return getGenericRepositoryValue(componentProperties,
                        paramName.substring(paramName.indexOf(IGenericConstants.EXP_SEPARATOR) + 1));
            }
        }
        return null;
    }

    @Override
    public boolean isGenericPropertiesValue(String paramName) {
        if (EConnectionParameterName.GENERIC_DRIVER_JAR.getDisplayName().equals(paramName)) {
            return true;
        }
        return false;
    }

    @Override
    public List<IComponent> filterNeededComponents(Item item, RepositoryNode seletetedNode, ERepositoryObjectType type) {
        // TUP-4151
        List<IComponent> neededComponents = new ArrayList<>();
        if (!(item instanceof ConnectionItem)) {
            return neededComponents;
        }
        if (((ConnectionItem) item).getConnection().getCompProperties() == null) {
            return neededComponents;
        }
        IComponentsService service = (IComponentsService) GlobalServiceRegister.getDefault().getService(IComponentsService.class);
        Collection<IComponent> components = service.getComponentsFactory().readComponents();
        for (IComponent component : components) {
            if (EComponentType.GENERIC.equals(component.getComponentType())) {
                if (isExtraTypeMetadata(seletetedNode, type)) {
                    seletetedNode = seletetedNode.getParent().getParent();
                }
                if (!neededComponents.contains(component) && isValid(seletetedNode, component)) {
                    neededComponents.add(component);
                }
            }
        }
        return neededComponents;
    }

    private boolean isExtraTypeMetadata(RepositoryNode seletetedNode, ERepositoryObjectType type) {
        if (type == ERepositoryObjectType.METADATA_CON_TABLE || type == ERepositoryObjectType.METADATA_CON_VIEW) {
            if (seletetedNode.getParent().getParent() == null) {
                return false;
            }
            RepositoryNode parent = seletetedNode.getParent().getParent();
            if (parent.getObjectType() == null) {
                return false;
            }
            IGenericDBService dbService = null;
            if (GlobalServiceRegister.getDefault().isServiceRegistered(IGenericDBService.class)) {
                dbService = (IGenericDBService) GlobalServiceRegister.getDefault().getService(IGenericDBService.class);
            }
            if (dbService != null && dbService.getExtraTypes().contains(parent.getObjectType())) {
                return true;
            }
        }
        return false;
    }

    private boolean isValid(RepositoryNode seletetedNode, IComponent component) {
        // TUP-4151
        IRepositoryViewObject object = seletetedNode.getObject();
        if (component == null || object == null) {
            return false;
        }
        ComponentProperties currentComponentProperties = null;
        if (object instanceof RepositoryViewObject) {
            RepositoryViewObject repositoryViewObj = (RepositoryViewObject) object;
            Connection connection = ((ConnectionItem) repositoryViewObj.getProperty().getItem()).getConnection();
            if (canHandle(connection)) {
                currentComponentProperties = ComponentsUtils.getComponentPropertiesFromSerialized(connection.getCompProperties(),
                        connection);
            }
        } else if (object instanceof MetadataTableRepositoryObject) {
            MetadataTableRepositoryObject metaTableRepObj = (MetadataTableRepositoryObject) object;
            currentComponentProperties = SchemaUtils.getCurrentComponentProperties(metaTableRepObj);
        } else if (object instanceof MetadataColumnRepositoryObject) {
            MetadataColumnRepositoryObject metaColumnRepObj = (MetadataColumnRepositoryObject) object;
            ModelElement element = metaColumnRepObj.getTdColumn();
            if (element != null && element.eContainer() instanceof MetadataTable) {
                MetadataTable metadataTable = (MetadataTable) element.eContainer();
                IMetadataTable newTable = MetadataToolHelper.convert(metadataTable);
                currentComponentProperties = SchemaUtils.getCurrentComponentProperties(newTable);
            }
        }
        if (currentComponentProperties != null) {
            try {
                List<ComponentDefinition> possibleComponents = ComponentsUtils.getComponentService()
                        .getPossibleComponents(currentComponentProperties);
                if (component instanceof Component) {
                    ComponentDefinition componentDefinition = ((Component) component).getComponentDefinition();
                    return possibleComponents.contains(componentDefinition);
                }
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    @Override
    public IComponentName getCorrespondingComponentName(Item item, ERepositoryObjectType type) {
        RepositoryComponentSetting setting = null;
        List<Class<Item>> list = new ArrayList<Class<Item>>();
        if (item instanceof ConnectionItem) {
            Connection connection = ((ConnectionItem) item).getConnection();
            if (canHandle(connection)) {
                setting = new RepositoryComponentSetting();
                setting.setWithSchema(true);
                String componentMainName = getComponentMainName(connection);
                setting.setInputComponent(getInputComponentName(componentMainName));
                setting.setOutputComponent(getOutputComponentName(componentMainName));
                Class clazz = null;
                try {
                    clazz = Class.forName(ConnectionItem.class.getName());
                } catch (ClassNotFoundException e) {
                    ExceptionHandler.process(e);
                }
                list.add(clazz);
                setting.setClasses(list.toArray(new Class[0]));
            }
        }
        return setting;
    }

    private String getComponentMainName(Connection connection) {
        IGenericWizardInternalService internalService = new GenericWizardInternalService();
        ComponentWizard componentWizard = null;
        String compPropertiesStr = connection.getCompProperties();
        if (compPropertiesStr != null) {
            ComponentProperties properties = ComponentsUtils.getComponentPropertiesFromSerialized(compPropertiesStr, connection);
            if (properties != null) {
                componentWizard = internalService.getTopLevelComponentWizard(properties, null);
            }
        }
        if (componentWizard != null) {
            return StringUtils.capitalize(componentWizard.getDefinition().getName());
        }
        return null;
    }

    private String getInputComponentName(String componentMainName) {
        return getInputOrOutputComponentName(componentMainName, true);
    }

    private String getOutputComponentName(String componentMainName) {
        return getInputOrOutputComponentName(componentMainName, false);
    }

    private String getInputOrOutputComponentName(String componentMainName, boolean isInput) {
        if (isInput) {
            return COMPONENT_PREFIX.concat(componentMainName).concat(INPUT);
        }
        return COMPONENT_PREFIX.concat(componentMainName).concat(OUTPUT);
    }

    private void setGenericRepositoryValue(Connection connection, INode node, IElementParameter param) {
        if (connection != null && param != null && param instanceof GenericElementParameter) {
            if ((connection.getCompProperties() == null) || connection.getCompProperties().length() <= 0) {
                GenericElementParameter genericParam = (GenericElementParameter) param;
                connection.setCompProperties(genericParam.getRootProperties().toSerialized());
            }
        }
    }

    @Override
    public ERepositoryObjectType getType(String repositoryType) {
        return null;
    }

    @Override
    public void setComponentValue(Connection connection, INode node, IElementParameter param) {
        if (node != null && canHandle(connection)) {
            setGenericRepositoryValue(connection, node, param);
        }
    }

    @Override
    public void handleTableRelevantParameters(Connection connection, IElement ele, IMetadataTable metadataTable) {
    }
}
