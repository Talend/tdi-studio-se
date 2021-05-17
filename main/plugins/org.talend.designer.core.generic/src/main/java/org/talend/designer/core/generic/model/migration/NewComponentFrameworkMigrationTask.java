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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import org.apache.avro.Schema;
import org.eclipse.emf.common.util.EList;
import org.talend.commons.exception.ExceptionHandler;
import org.talend.commons.exception.PersistenceException;
import org.talend.components.api.component.Connector;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.api.properties.ComponentReferenceProperties;
import org.talend.core.model.components.ComponentCategory;
import org.talend.core.model.components.IComponent;
import org.talend.core.model.components.ModifyComponentsAction;
import org.talend.core.model.components.conversions.IComponentConversion;
import org.talend.core.model.components.filters.IComponentFilter;
import org.talend.core.model.components.filters.NameComponentFilter;
import org.talend.core.model.metadata.IMetadataTable;
import org.talend.core.model.metadata.MetadataTable;
import org.talend.core.model.metadata.builder.ConvertionHelper;
import org.talend.core.model.migration.AbstractJobMigrationTask;
import org.talend.core.model.process.AbstractNode;
import org.talend.core.model.process.EConnectionType;
import org.talend.core.model.process.EParameterFieldType;
import org.talend.core.model.process.IElementParameter;
import org.talend.core.model.properties.Item;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.core.runtime.util.GenericTypeUtils;
import org.talend.core.ui.component.ComponentsFactoryProvider;
import org.talend.daikon.NamedThing;
import org.talend.daikon.properties.property.Property;
import org.talend.designer.core.generic.constants.IGenericConstants;
import org.talend.designer.core.generic.model.GenericElementParameter;
import org.talend.designer.core.generic.model.GenericTableUtils;
import org.talend.designer.core.generic.utils.ComponentsUtils;
import org.talend.designer.core.generic.utils.ParameterUtilTool;
import org.talend.designer.core.generic.utils.SchemaUtils;
import org.talend.designer.core.model.components.EParameterName;
import org.talend.designer.core.model.metadata.MetadataEmfFactory;
import org.talend.designer.core.model.utils.emf.talendfile.ConnectionType;
import org.talend.designer.core.model.utils.emf.talendfile.ElementParameterType;
import org.talend.designer.core.model.utils.emf.talendfile.ElementValueType;
import org.talend.designer.core.model.utils.emf.talendfile.MetadataType;
import org.talend.designer.core.model.utils.emf.talendfile.NodeType;
import org.talend.designer.core.model.utils.emf.talendfile.ProcessType;

/**
 * created by hcyi on Nov 18, 2015 Detailled comment
 *
 */
public abstract class NewComponentFrameworkMigrationTask extends AbstractJobMigrationTask {

    @Override
    public ExecutionResult execute(final Item item) {
        final ProcessType processType = getProcessType(item);
        ComponentCategory category = ComponentCategory.getComponentCategoryFromItem(item);
        Properties props = getPropertiesFromFile();
        IComponentConversion conversion = new IComponentConversion() {

            @Override
            public void transform(NodeType nodeType) {
                if (nodeType == null || props == null) {
                    return;
                }
                boolean modified = false;
                Map<String, String> schemaParamMap = new HashMap<>();
                String currComponentName = nodeType.getComponentName();
                String newComponentName = props.getProperty(currComponentName);
                nodeType.setComponentName(newComponentName);
                String label = ParameterUtilTool.getParameterValue(nodeType, "LABEL"); //$NON-NLS-1$
                if (label != null) {
                    ElementParameterType paraType = ParameterUtilTool.findParameterType(nodeType, label.replaceAll("_", "")); //$NON-NLS-1$ //$NON-NLS-2$
                    if (paraType != null) {
                        ParameterUtilTool.findParameterType(nodeType, "LABEL").setValue(paraType.getValue()); //$NON-NLS-1$
                        modified = true;
                    }
                }
                IComponent component = ComponentsFactoryProvider.getInstance().get(newComponentName, category.getName());
                ComponentProperties compProperties = ComponentsUtils.getComponentProperties(newComponentName);
                FakeNode fNode = new FakeNode(component);
                ElementParameterType propertyParamType = getParameterType(nodeType, "PROPERTY:PROPERTY_TYPE");
                boolean isRepository = false;
                if (propertyParamType != null && "REPOSITORY".equals(propertyParamType.getValue())) {
                    isRepository = true;
                }
                ElementParameterType sqlParamType = getParameterType(nodeType, "QUERYSTORE:QUERYSTORE_TYPE");
                boolean isSQLRepository = false;
                if (sqlParamType != null && "REPOSITORY".equals(sqlParamType.getValue())) {
                    isSQLRepository = true;
                }
                for (IElementParameter param : fNode.getElementParameters()) {
                    if (param instanceof GenericElementParameter) {
                        String paramName = param.getName();
                        NamedThing currNamedThing = ComponentsUtils.getGenericSchemaElement(compProperties, paramName);
                        String oldParamName = props.getProperty(currComponentName + IGenericConstants.EXP_SEPARATOR + paramName);
                        if (oldParamName != null && !(oldParamName = oldParamName.trim()).isEmpty()) {
                            if (currNamedThing instanceof Property
                                    && (GenericTypeUtils.isSchemaType((Property<?>) currNamedThing))) {
                                schemaParamMap.put(
                                        paramName,
                                        props.getProperty(currComponentName + IGenericConstants.EXP_SEPARATOR + paramName
                                                + IGenericConstants.EXP_SEPARATOR + "connector"));
                            }
                            ElementParameterType paramType = getParameterType(nodeType, oldParamName);
                            if (paramType != null) {
                                if (currNamedThing instanceof ComponentReferenceProperties) {
                                    ComponentReferenceProperties refProps = (ComponentReferenceProperties) currNamedThing;
                                    refProps.referenceType
                                            .setValue(ComponentReferenceProperties.ReferenceType.COMPONENT_INSTANCE);
                                    refProps.componentInstanceId.setStoredValue(ParameterUtilTool
                                            .convertParameterValue(paramType));
                                    refProps.componentInstanceId.setTaggedValue(IGenericConstants.ADD_QUOTES, true);
                                } else {
                                    processMappedElementParameter(props, nodeType, (GenericElementParameter) param, paramType,
                                            currNamedThing);
                                }
                                ParameterUtilTool.removeParameterType(nodeType, paramType);
                                modified = true;
                            }
                            if (EParameterFieldType.SCHEMA_REFERENCE.equals(param.getFieldType())) {
                                String schemaTypeName = ":" + EParameterName.SCHEMA_TYPE.getName();//$NON-NLS-1$
                                String repSchemaTypeName = ":" + EParameterName.REPOSITORY_SCHEMA_TYPE.getName();//$NON-NLS-1$
                                paramType = getParameterType(nodeType, oldParamName + schemaTypeName);
                                if (paramType != null) {
                                    paramType.setName(param.getName() + schemaTypeName);
                                }
                                paramType = getParameterType(nodeType, oldParamName + repSchemaTypeName);
                                if (paramType != null) {
                                    paramType.setName(param.getName() + repSchemaTypeName);
                                }
                            }
                        } else {
                            processUnmappedElementParameter(props, nodeType, (GenericElementParameter) param, currNamedThing);
                        }
                        if (isRepository && isRepositoryParam(paramName) || isSQLRepository && "sql".equals(paramName)) {
                            ComponentsUtils.setPropertiesPepositoryValue(compProperties, paramName);
                        }
                    } else {
                        if (EParameterFieldType.SCHEMA_REFERENCE.equals(param.getFieldType())) {
                            String paramName = param.getName();
                            schemaParamMap.put(
                                    paramName,
                                    props.getProperty(currComponentName + IGenericConstants.EXP_SEPARATOR + paramName
                                            + IGenericConstants.EXP_SEPARATOR + "connector"));

                            String oldParamName = props.getProperty(currComponentName + IGenericConstants.EXP_SEPARATOR
                                    + paramName);
                            String schemaTypeName = ":" + EParameterName.SCHEMA_TYPE.getName();//$NON-NLS-1$
                            String repSchemaTypeName = ":" + EParameterName.REPOSITORY_SCHEMA_TYPE.getName();//$NON-NLS-1$
                            ElementParameterType paramType = getParameterType(nodeType, oldParamName + schemaTypeName);
                            if (paramType != null) {
                                paramType.setName(param.getName() + schemaTypeName);
                            }
                            paramType = getParameterType(nodeType, oldParamName + repSchemaTypeName);
                            if (paramType != null) {
                                paramType.setName(param.getName() + repSchemaTypeName);
                            }
                        }
                    }

                }
                // Migrate schemas
                Map<String, MetadataType> metadatasMap = new HashMap<>();
                EList<MetadataType> metadatas = nodeType.getMetadata();
                for (MetadataType metadataType : metadatas) {
                    metadatasMap.put(metadataType.getConnector(), metadataType);
                }
                Iterator<Entry<String, String>> schemaParamIter = schemaParamMap.entrySet().iterator();
                String uniqueName = ParameterUtilTool.getParameterValue(nodeType, "UNIQUE_NAME"); //$NON-NLS-1$

                while (schemaParamIter.hasNext()) {
                    Entry<String, String> schemaParamEntry = schemaParamIter.next();
                    String newParamName = schemaParamEntry.getKey();
                    String connectorMapping = schemaParamEntry.getValue();
                    String oldConnector = connectorMapping.split("->")[0]; //$NON-NLS-1$
                    String newConnector = connectorMapping.split("->")[1]; //$NON-NLS-1$
                    MetadataType metadataType = metadatasMap.get(oldConnector);
                    if (metadataType != null) {
                        metadataType.setConnector(newConnector);
                        MetadataEmfFactory factory = new MetadataEmfFactory();
                        factory.setMetadataType(metadataType);
                        IMetadataTable metadataTable = factory.getMetadataTable();
                        Schema schema = SchemaUtils.convertTalendSchemaIntoComponentSchema(ConvertionHelper
                                .convert(metadataTable));
                        compProperties.setValue(newParamName, schema);
                    }
                    if (!oldConnector.equals(newConnector)) {
                        // if connector was changed, we should update the connections
                        for (Object connectionObj : processType.getConnection()) {
                            if (connectionObj instanceof ConnectionType) {
                                ConnectionType connectionType = (ConnectionType) connectionObj;
                                if (connectionType.getSource().equals(uniqueName)
                                        && connectionType.getConnectorName().equals(oldConnector)) {
                                    connectionType.setConnectorName(newConnector);
                                }
                            }
                        }
                    }
                }
                for (Object connectionObj : processType.getConnection()) {
                    ConnectionType connection = (ConnectionType) connectionObj;
                    if (connection.getSource() != null && connection.getSource().equals(uniqueName)) {
                        if (EConnectionType.FLOW_MAIN.getName().equals(connection.getConnectorName())) {
                            connection.setConnectorName(Connector.MAIN_NAME);
                        }
                    }
                }

                if (modified) {
                    String serializedProperties = compProperties.toSerialized();
                    if (serializedProperties != null) {
                        ElementParameterType pType = ParameterUtilTool.createParameterType(null, "PROPERTIES", //$NON-NLS-1$
                                serializedProperties);
                        nodeType.getElementParameter().add(pType);
                    }
                }
            }
        };

        if (processType != null) {
            boolean modified = false;
            for (Object obj : processType.getNode()) {
                if (obj != null && obj instanceof NodeType) {
                    String componentName = ((NodeType) obj).getComponentName();
                    String newComponentName = props.getProperty(componentName);
                    if (newComponentName == null) {
                        continue;
                    }
                    IComponentFilter filter = new NameComponentFilter(componentName);
                    modified = ModifyComponentsAction.searchAndModify((NodeType) obj, filter,
                            Arrays.<IComponentConversion> asList(conversion))
                            || modified;
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
        }
        return ExecutionResult.NOTHING_TO_DO;
    }

    protected boolean isRepositoryParam(String paramName) {
        return false;
    }

    protected void migrateComponent(String componentName) {
        // with default implementation
    }

    protected Properties getPropertiesFromFile() {
        // with default implementation
        return new Properties();
    }

    protected ElementParameterType getParameterType(NodeType node, String paramName) {
        return ParameterUtilTool.findParameterType(node, paramName);
    }

    public static List<Map<String, Object>> getTableValues(ElementParameterType pType, String tableMapping) {
        Map<String, String> columnsMapping = new HashMap<String, String>();
        String[] mappings = tableMapping.split(";");
        for (String curMapping : mappings) {
            String[] columnInfo = curMapping.split("=");
            columnsMapping.put(columnInfo[0], columnInfo[1]);
        }
        List<Map<String, Object>> tableValues = new ArrayList<Map<String, Object>>();
        Map<String, Object> lineValues = null;
        for (ElementValueType elementValue : (List<ElementValueType>) pType.getElementValue()) {
            String columnName = columnsMapping.get(elementValue.getElementRef());
            if ((lineValues == null) || (lineValues.get(columnName) != null)) {
                lineValues = new HashMap<String, Object>();
                tableValues.add(lineValues);
            }

            lineValues.put(columnName, elementValue.getValue());
        }
        return tableValues;
    }

    /**
     * Process element parameter which has mapping from old parameter to component property.
     *
     * <p>
     * Subclasses can override this method to customize migration of element parameters.
     *
     * @param props migration task properties
     * @param nodeType component node
     * @param param new parameter object
     * @param paramType old parameter object
     * @param currNamedThing component property object
     */
    protected void processMappedElementParameter(Properties props, NodeType nodeType, GenericElementParameter param,
            ElementParameterType paramType, NamedThing currNamedThing) {

        String currComponentName = nodeType.getComponentName();
        String paramName = param.getName();

        if (EParameterFieldType.TABLE.equals(param.getFieldType())) {
            String tableMapping = getTableMapping(props, currComponentName, paramName);
            GenericTableUtils.setTableValues(((ComponentProperties) currNamedThing), getTableValues(paramType, tableMapping),
                    param);
        } else {
            ((Property) currNamedThing).setValue(ParameterUtilTool.convertParameterValue(paramType));
        }
    }

    /**
     * Process element parameter which has no mapping from old parameter to component property.
     *
     * <p>
     * Subclasses can override this method to customize migration of element parameters.
     *
     * @param props migration task properties
     * @param nodeType component node
     * @param param new parameter object
     * @param currNamedThing component property object
     */
    protected void processUnmappedElementParameter(Properties props, NodeType nodeType, GenericElementParameter param,
            NamedThing currNamedThing) {

        if (currNamedThing instanceof Property) {
            if (((Property<?>) currNamedThing).isRequired()
                    && GenericTypeUtils.isStringType(((Property<?>) currNamedThing).getType())) {
                ((Property<?>) currNamedThing).setStoredValue("\"\""); //$NON-NLS-1$
            }
        }
    }

    protected static String getTableMapping(Properties props, String componentName, String paramName) {
        return props.getProperty(componentName + IGenericConstants.EXP_SEPARATOR + paramName + IGenericConstants.EXP_SEPARATOR
                + "mapping");
    }

    public static class FakeNode extends AbstractNode {

        public FakeNode(IComponent component) {
            super();
            setComponentName(component.getName());
            List<IMetadataTable> metaList = new ArrayList<IMetadataTable>();
            IMetadataTable metaTable = new MetadataTable();
            metaTable.setTableName("TableName_1"); //$NON-NLS-1$
            metaList.add(metaTable);
            setMetadataList(metaList);
            setComponent(component);
            setElementParameters(component.createElementParameters(this));
            setListConnector(component.createConnectors(this));
            setUniqueName("UniqueName_1"); //$NON-NLS-1$
            setHasConditionalOutputs(component.hasConditionalOutputs());
            setIsMultiplyingOutputs(component.isMultiplyingOutputs());
        }
    }
}
