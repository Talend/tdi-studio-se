/**
 * Copyright (C) 2006-2018 Talend Inc. - www.talend.com
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.talend.commons.exception.ExceptionHandler;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.model.components.IComponent;
import org.talend.core.model.components.IComponentsService;
import org.talend.core.model.metadata.IMetadataTable;
import org.talend.core.model.metadata.builder.connection.Connection;
import org.talend.core.model.metadata.designerproperties.RepositoryToComponentProperty;
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
import org.talend.sdk.component.server.front.model.ConfigTypeNode;
import org.talend.sdk.component.server.front.model.SimplePropertyDefinition;
import org.talend.sdk.component.studio.ComponentModel;
import org.talend.sdk.component.studio.Lookups;
import org.talend.sdk.component.studio.metadata.model.TaCoKitConfigurationItemModel;
import org.talend.sdk.component.studio.metadata.model.TaCoKitConfigurationModel;
import org.talend.sdk.component.studio.metadata.model.TaCoKitConfigurationModel.ValueModel;
import org.talend.sdk.component.studio.metadata.node.ITaCoKitRepositoryNode;
import org.talend.sdk.component.studio.util.TaCoKitConst;
import org.talend.sdk.component.studio.util.TaCoKitUtil;

public class TaCoKitDragAndDropHandler extends AbstractDragAndDropServiceHandler {

    private static final String TACOKIT = TaCoKitConst.METADATA_TACOKIT.name();

    public TaCoKitDragAndDropHandler() {
        // nothing to do
    }

    @Override
    public boolean canHandle(final Connection connection) {
        if (connection == null) {
            return false;
        }
        try {
            TaCoKitConfigurationModel model = new TaCoKitConfigurationModel(connection);
            if (!TaCoKitUtil.isEmpty(model.getConfigurationId())) {
                return true;
            }
        } catch (Exception e) {
            ExceptionHandler.process(e);
        }
        return false;
    }

    @Override
    public Object getComponentValue(final Connection connection, final String value, final IMetadataTable table,
            final String targetComponent) {
        try {
            if (connection == null) {
                return null;
            }
            if (TaCoKitUtil.isEmpty(value)) {
                return null;
            }
            TaCoKitConfigurationModel model = new TaCoKitConfigurationModel(connection);
            ValueModel valueModel = model.getValue(value);
            if (valueModel != null) {
                Object result = valueModel.getValue();
                if (result == null) {
                    return null;
                } else {
                    String type = null;
                    try {
                        List<SimplePropertyDefinition> properties =
                                valueModel.getConfigurationModel().getConfigTypeNode().getProperties();
                        if (properties != null) {
                            for (SimplePropertyDefinition property : properties) {
                                if (value.equals(property.getPath())) {
                                    type = property.getType();
                                    break;
                                }
                            }
                        }
                    } catch (Exception e) {
                        ExceptionHandler.process(e);
                    }
                    if (TaCoKitConst.TYPE_STRING.equalsIgnoreCase(type)) {
                        return RepositoryToComponentProperty.addQuotesIfNecessary(connection,
                                String.class.cast(result));
                    } else {
                        return valueModel.getValue();
                    }
                }
            }
        } catch (Exception e) {
            ExceptionHandler.process(e);
        }
        return null;
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
                (IComponentsService) GlobalServiceRegister.getDefault().getService(IComponentsService.class);
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
                TaCoKitConfigurationItemModel itemModel = new TaCoKitConfigurationItemModel((ConnectionItem) item);
                TaCoKitConfigurationModel configurationModel = itemModel.getConfigurationModel();
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
        System.out.println("setComponentValue: " + param);
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
