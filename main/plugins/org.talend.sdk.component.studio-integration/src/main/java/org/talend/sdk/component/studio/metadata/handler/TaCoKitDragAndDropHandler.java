/**
 * Copyright (C) 2006-2019 Talend Inc. - www.talend.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package org.talend.sdk.component.studio.metadata.handler;

import static org.talend.core.model.metadata.designerproperties.RepositoryToComponentProperty.addQuotesIfNecessary;
import static org.talend.sdk.component.studio.util.TaCoKitUtil.isEmpty;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.talend.commons.exception.ExceptionHandler;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.model.components.IComponent;
import org.talend.core.model.components.IComponentsService;
import org.talend.core.model.metadata.IMetadataTable;
import org.talend.core.model.metadata.builder.connection.Connection;
import org.talend.core.model.process.EParameterFieldType;
import org.talend.core.model.process.IElement;
import org.talend.core.model.process.IElementParameter;
import org.talend.core.model.process.INode;
import org.talend.core.model.properties.ConnectionItem;
import org.talend.core.model.properties.Item;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.model.utils.AbstractDragAndDropServiceHandler;
import org.talend.core.model.utils.IComponentName;
import org.talend.core.repository.RepositoryComponentSetting;
import org.talend.repository.model.RepositoryNode;
import org.talend.sdk.component.server.front.model.ComponentDetail;
import org.talend.sdk.component.server.front.model.ComponentDetailList;
import org.talend.sdk.component.server.front.model.ComponentId;
import org.talend.sdk.component.server.front.model.ComponentIndex;
import org.talend.sdk.component.server.front.model.ComponentIndices;
import org.talend.sdk.component.server.front.model.ConfigTypeNode;
import org.talend.sdk.component.server.front.model.SimplePropertyDefinition;
import org.talend.sdk.component.studio.ComponentModel;
import org.talend.sdk.component.studio.Lookups;
import org.talend.sdk.component.studio.metadata.model.TaCoKitConfigurationModel;
import org.talend.sdk.component.studio.metadata.model.TaCoKitConfigurationModel.ValueModel;
import org.talend.sdk.component.studio.metadata.node.ITaCoKitRepositoryNode;
import org.talend.sdk.component.studio.model.parameter.PropertyDefinitionDecorator;
import org.talend.sdk.component.studio.util.TaCoKitConst;
import org.talend.sdk.component.studio.util.TaCoKitUtil;
import org.talend.sdk.component.studio.websocket.WebSocketClient.V1Component;
import org.talend.sdk.studio.process.TaCoKitNode;

public class TaCoKitDragAndDropHandler extends AbstractDragAndDropServiceHandler {

    private static final String TACOKIT = TaCoKitConst.METADATA_TACOKIT.name();

    @Override
    public boolean canHandle(final Connection connection) {
        if (connection == null) {
            return false;
        }
        try {
            if (TaCoKitConfigurationModel.isTacokit(connection)) {
                return true;
            }
        } catch (Exception e) {
            ExceptionHandler.process(e);
        }
        return false;
    }

    /**
     * Retrieves persisted value (value stored in repository) by {@code repositoryKey}
     *
     * @param connection object, which stores persisted values
     * @param repositoryKey repository value key
     * @param table component schema value
     * @param targetComponent name of a target component
     * @return persisted value
     */
    @Override
    public Object getComponentValue(final Connection connection, final String repositoryKey, final IMetadataTable table,
            final String targetComponent, Map<Object, Object> contextMap) {
        Objects.requireNonNull(targetComponent, "targetComponent should not be null");
        if (connection == null || isEmpty(repositoryKey)) {
            return null;
        }
        ValueModel valueModel = null;
        TaCoKitConfigurationModel model = null;
        String key = null;
        try {
            model = new TaCoKitConfigurationModel(connection);
            if (TaCoKitNode.TACOKIT_METADATA_TYPE_ID.equals(repositoryKey)) {
                return model.getConfigurationId();
            }
            key = computeKey(model, repositoryKey, targetComponent);
            valueModel = model.getValue(key);
        } catch (Exception e) {
            ExceptionHandler.process(e);
        }
        if (valueModel == null || valueModel.getValue() == null) {
            return null;
        }
        EParameterFieldType fieldType = null;
        if (model != null) {
            fieldType = model.getEParameterFieldType(key);
        }
        if (TaCoKitConst.TYPE_STRING.equalsIgnoreCase(valueModel.getType())
                && !EParameterFieldType.CLOSED_LIST.equals(fieldType)) {
            return addQuotesIfNecessary(connection, valueModel.getValue());
        } else if (EParameterFieldType.TABLE.equals(fieldType)
                || EParameterFieldType.TACOKIT_SUGGESTABLE_TABLE.equals(fieldType)) {
            return model.convertParameterValue(repositoryKey, key, valueModel.getValue());
        } else {
            return valueModel.getValue();
        }
    }

    /**
     * Computes stored key (a key which is used to store specific parameter value) from {@code parameterId} of specified {@code component}
     *
     * @param model object which stores persisted values
     * @param parameterId parameter id
     * @param component component name
     * @return stored value key
     * @throws Exception Exception should be handled by ExceptionHandler
     */
    private String computeKey(final TaCoKitConfigurationModel model, String parameterId, String component) throws Exception {
        if (Lookups.taCoKitCache().isVirtualComponentName(component)) {
            List<SimplePropertyDefinition> propertiesList = model.getConfigTypeNode().getProperties();
            if (TaCoKitConst.CONFIG_NODE_ID_DATASTORE.equalsIgnoreCase(model.getConfigTypeNode().getConfigurationType())) {
                for (SimplePropertyDefinition p : propertiesList) {
                    if (StringUtils.equals(parameterId, p.getPath())) {
                        return parameterId;
                    }
                }
            } else {
                ComponentDetail detail = retrieveDetail(component);
                String configPath = Lookups.taCoKitCache().getConfigurationPath(component, detail.getProperties());
                String datastorePath = Lookups.taCoKitCache().getDatastorePath(component, detail.getProperties());
                if (datastorePath != null && configPath != null) {
                    String parameterIdInDateset = parameterId.replace(configPath, datastorePath);
                    for (SimplePropertyDefinition p : propertiesList) {
                        if (StringUtils.equals(parameterIdInDateset, p.getPath())) {
                            return parameterIdInDateset;
                        }
                    }
                }
            }
            return null;
        } else {
            final Map<String, PropertyDefinitionDecorator> tree = retrieveProperties(component);
            Optional<String> configPath = findConfigPath(tree, model, parameterId);
            String modelRoot = TaCoKitUtil.findModelRoot(model.getProperties());
            if (configPath.isPresent()) {
                return parameterId.replace(configPath.get(), modelRoot);
            } else {
                return null;
            }
        }
    }

    private Map<String, PropertyDefinitionDecorator> retrieveProperties(final String component) {
        final ComponentDetail detail = retrieveDetail(component);
        return buildPropertyTree(detail);
    }

    private Optional<String> findConfigPath(final Map<String, PropertyDefinitionDecorator> tree, final TaCoKitConfigurationModel model, final String parameterId) throws Exception {
        final ConfigTypeNode configTypeNode = model.getConfigTypeNode();
        final String configType = configTypeNode.getConfigurationType();
        final String configName = configTypeNode.getName();

        for (PropertyDefinitionDecorator current = tree.get(parameterId); current != null; current = tree.get(current.getParentPath())) {
            if (configType.equals(current.getConfigurationType())
                    && configName.equals(current.getConfigurationTypeName())) {
                return Optional.of(current.getPath());
            }
        }
        return Optional.empty();
    }

    private ComponentDetail retrieveDetail(final String component) {
        if (Lookups.taCoKitCache().isVirtualComponentName(component)) {
            return Lookups.taCoKitCache().getComponentDetailByName(component);
        }
        final ComponentIndices indices = client().getIndex(language());
        final ComponentId id = indices.getComponents().stream().map(ComponentIndex::getId)
                .filter(i -> component.equals(TaCoKitUtil.getFullComponentName(i.getFamily(), i.getName())))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(component + " not found"));

        final ComponentDetailList detailList = client()
                .getDetail(language(), new String[] { id.getId() });
        if (detailList.getDetails().size() != 1) {
            throw new IllegalArgumentException("No single detail for " + component);
        }
        return detailList.getDetails().get(0);
    }

    private Map<String, PropertyDefinitionDecorator> buildPropertyTree(final ComponentDetail detail) {
        final Map<String, PropertyDefinitionDecorator> tree = new HashMap<>();
        final Collection<PropertyDefinitionDecorator> properties = PropertyDefinitionDecorator.wrap(detail.getProperties());
        properties.forEach(p -> tree.put(p.getPath(), p));
        return tree;
    }

    private V1Component client() {
        return Lookups.client().v1().component();
    }

    private String language() {
        return Locale.getDefault().getLanguage();
    }

    @Override
    public List<IComponent> filterNeededComponents(final Item item, final RepositoryNode selectedNode,
            final ERepositoryObjectType type) {

        List<IComponent> neededComponents = new ArrayList<IComponent>();
        if (selectedNode instanceof ITaCoKitRepositoryNode) {
            if (!((ITaCoKitRepositoryNode) selectedNode).isLeafNode()) {
                return neededComponents;
            }
        } else {
            return neededComponents;
        }
        ITaCoKitRepositoryNode tacokitNode = (ITaCoKitRepositoryNode) selectedNode;
        ConfigTypeNode configTypeNode = tacokitNode.getConfigTypeNode();
        ConfigTypeNode familyTypeNode = Lookups.taCoKitCache().getFamilyNode(configTypeNode);
        String familyName = familyTypeNode.getName();
        String configType = configTypeNode.getConfigurationType();
        String configName = configTypeNode.getName();

        IComponentsService service =
                GlobalServiceRegister.getDefault().getService(IComponentsService.class);
        Collection<IComponent> components = service.getComponentsFactory().readComponents();
        for (IComponent component : components) {
            if (component instanceof ComponentModel) {
                if (((ComponentModel) component).supports(familyName, configType, configName)) {
                    neededComponents.add(component);
                }
            }
        }

        return neededComponents;

    }

    @Override
    public IComponentName getCorrespondingComponentName(final Item item, final ERepositoryObjectType type) {
        RepositoryComponentSetting setting = null;
        if (item instanceof ConnectionItem) {
            try {
                TaCoKitConfigurationModel configurationModel = new TaCoKitConfigurationModel(((ConnectionItem) item).getConnection());
                if (configurationModel == null || TaCoKitUtil.isEmpty(configurationModel.getConfigurationId())) {
                    return setting;
                }
                setting = new RepositoryComponentSetting();
                setting.setName(TACOKIT);
                setting.setRepositoryType(TACOKIT);
                setting.setWithSchema(true);
                // setting.setInputComponent(INPUT);
                // setting.setOutputComponent(OUTPUT);
                List<Class<Item>> list = new ArrayList<Class<Item>>();
                Class clazz = null;
                try {
                    clazz = Class.forName(ConnectionItem.class.getName());
                } catch (ClassNotFoundException e) {
                    ExceptionHandler.process(e);
                }
                list.add(clazz);
                setting.setClasses(list.toArray(new Class[0]));
            } catch (Exception e) {
                // nothing to do
            }
        }

        return setting;
    }

    @Override
    public void setComponentValue(final Connection connection, final INode node, final IElementParameter param) {
        // System.out.println("setComponentValue: " + param);
    }

    @Override
    public ERepositoryObjectType getType(final String repositoryType) {
        ERepositoryObjectType repObjType = ERepositoryObjectType.valueOf(repositoryType);
        if (repObjType != null && TaCoKitUtil.isTaCoKitType(repObjType)) {
            return repObjType;
        }
        return null;
    }

    @Override
    public void handleTableRelevantParameters(final Connection connection, final IElement ele,
            final IMetadataTable metadataTable) {
        // nothing to do
    }

}
