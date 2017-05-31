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
package org.talend.repository.generic.ui.dnd;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

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
import org.talend.core.model.metadata.builder.connection.MetadataTable;
import org.talend.core.model.process.IElement;
import org.talend.core.model.process.IElementParameter;
import org.talend.core.model.process.INode;
import org.talend.core.model.properties.ConnectionItem;
import org.talend.core.model.properties.Item;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.model.repository.IRepositoryViewObject;
import org.talend.core.model.repository.RepositoryViewObject;
import org.talend.core.model.utils.AbstractDragAndDropServiceHandler;
import org.talend.core.model.utils.IComponentName;
import org.talend.core.repository.RepositoryComponentSetting;
import org.talend.core.repository.model.repositoryObject.MetadataColumnRepositoryObject;
import org.talend.core.repository.model.repositoryObject.MetadataTableRepositoryObject;
import org.talend.core.runtime.services.IGenericWizardService;
import org.talend.core.runtime.util.GenericTypeUtils;
import org.talend.core.utils.TalendQuoteUtils;
import org.talend.daikon.properties.property.Property;
import org.talend.designer.core.generic.constants.IGenericConstants;
import org.talend.designer.core.generic.model.Component;
import org.talend.designer.core.generic.model.GenericElementParameter;
import org.talend.designer.core.generic.utils.ComponentsUtils;
import org.talend.designer.core.generic.utils.SchemaUtils;
import org.talend.repository.generic.internal.IGenericWizardInternalService;
import org.talend.repository.generic.internal.service.GenericWizardInternalService;
import org.talend.repository.generic.model.genericMetadata.GenericConnection;
import org.talend.repository.generic.model.genericMetadata.GenericConnectionItem;
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
        return connection instanceof GenericConnection;
    }

    @Override
    public Object getComponentValue(Connection connection, String value, IMetadataTable table, String targetComponent) {
        if (value != null && canHandle(connection)) {
            if (GlobalServiceRegister.getDefault().isServiceRegistered(IGenericWizardService.class)) {
                IGenericWizardService wizardService = (IGenericWizardService) GlobalServiceRegister.getDefault().getService(
                        IGenericWizardService.class);
                if (wizardService != null && wizardService.isGenericConnection(connection)) {
                    return getGenericRepositoryValue((GenericConnection) connection,
                            wizardService.getAllComponentProperties(connection, getSeletetedMetadataTableName(table)), value,
                            table);
                }
            }
        }
        return null;
    }

    private Object getGenericRepositoryValue(GenericConnection connection, List<ComponentProperties> componentProperties,
            String value, IMetadataTable table) {
        if (componentProperties != null && value != null) {
            for (ComponentProperties compPro : componentProperties) {
                Property<?> property = compPro.getValuedProperty(value);
                if (property != null) {
                    Object paramValue = property.getStoredValue();
                    if (GenericTypeUtils.isStringType(property) || GenericTypeUtils.isObjectType(property)) {
                        if (paramValue != null) {
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
            if (value.indexOf(IGenericConstants.EXP_SEPARATOR) != -1) {
                return getGenericRepositoryValue(connection, componentProperties,
                        value.substring(value.indexOf(IGenericConstants.EXP_SEPARATOR) + 1), table);
            }
        }
        return null;
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
                Property property = compPro.getValuedProperty(paramName);
                if (property != null) {
                    return property.getTaggedValue(IGenericConstants.REPOSITORY_VALUE);
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
    public List<IComponent> filterNeededComponents(Item item, RepositoryNode seletetedNode, ERepositoryObjectType type) {
        // TUP-4151
        List<IComponent> neededComponents = new ArrayList<>();
        if (!(item instanceof GenericConnectionItem)) {
            return neededComponents;
        }
        IComponentsService service = (IComponentsService) GlobalServiceRegister.getDefault().getService(IComponentsService.class);
        Set<IComponent> components = service.getComponentsFactory().getComponents();
        for (IComponent component : components) {
            if (EComponentType.GENERIC.equals(component.getComponentType())) {
                if (!neededComponents.contains(component) && isValid(seletetedNode, component)) {
                    neededComponents.add(component);
                }
            }
        }
        return neededComponents;
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
                GenericConnection genericConnection = (GenericConnection) connection;
                currentComponentProperties = ComponentsUtils.getComponentPropertiesFromSerialized(
                        genericConnection.getCompProperties(), connection);
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
                List<ComponentDefinition> possibleComponents = ComponentsUtils.getComponentService().getPossibleComponents(
                        currentComponentProperties);
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
        if (item instanceof GenericConnectionItem) {
            GenericConnection connection = (GenericConnection) ((GenericConnectionItem) item).getConnection();
            setting = new RepositoryComponentSetting();
            setting.setWithSchema(true);
            String componentMainName = getComponentMainName(connection);
            setting.setInputComponent(getInputComponentName(componentMainName));
            setting.setOutputComponent(getOutputComponentName(componentMainName));
            Class clazz = null;
            try {
                clazz = Class.forName(GenericConnectionItem.class.getName());
            } catch (ClassNotFoundException e) {
                ExceptionHandler.process(e);
            }
            list.add(clazz);
            setting.setClasses(list.toArray(new Class[0]));
        }
        return setting;
    }

    private String getComponentMainName(GenericConnection connection) {
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

    private void setGenericRepositoryValue(GenericConnection connection, INode node, IElementParameter param) {
        if (connection != null && param != null && param instanceof GenericElementParameter) {
            if (connection.getCompProperties() == null) {
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
            setGenericRepositoryValue((GenericConnection) connection, node, param);
        }
    }

    @Override
    public void handleTableRelevantParameters(Connection connection, IElement ele, IMetadataTable metadataTable) {
    }
}
