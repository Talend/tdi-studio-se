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
package org.talend.sdk.component.studio.model.update;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.talend.commons.exception.ExceptionHandler;
import org.talend.commons.exception.PersistenceException;
import org.talend.core.model.properties.ConnectionItem;
import org.talend.core.model.properties.Item;
import org.talend.core.model.relationship.Relation;
import org.talend.core.model.relationship.RelationshipItemBuilder;
import org.talend.core.model.repository.IRepositoryViewObject;
import org.talend.core.model.update.IUpdateItemType;
import org.talend.core.model.update.RepositoryUpdateManager;
import org.talend.core.model.update.UpdateResult;
import org.talend.core.model.update.extension.UpdateManagerProviderDetector;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.repository.model.IProxyRepositoryFactory;
import org.talend.repository.model.IRepositoryNode;
import org.talend.sdk.component.studio.metadata.model.TaCoKitConfigurationModel;
import org.talend.sdk.component.studio.metadata.model.TaCoKitConfigurationModel.ValueModel;
import org.talend.sdk.component.studio.metadata.node.ITaCoKitRepositoryNode;
import org.talend.sdk.component.studio.model.parameter.PropertyDefinitionDecorator;
import org.talend.sdk.component.studio.ui.wizard.TaCoKitConfigurationRuntimeData;
import org.talend.sdk.component.studio.util.TaCoKitUtil;

/**
 * created by hcyi on Jun 14, 2019
 * Detailled comment
 *
 */
public class TaCoKitUpdateManager extends RepositoryUpdateManager {

    public TaCoKitUpdateManager(ConnectionItem connectionItem, List<Relation> relations) {
        super(connectionItem, relations);
    }

    public static boolean updateTaCoKitConnection(ConnectionItem connectionItem) {
        return updateTaCoKitConnection(connectionItem, true, false);
    }

    public static boolean updateTaCoKitConnection(ConnectionItem connectionItem, boolean show, final boolean onlySimpleShow) {
        return updateTaCoKitConnection(connectionItem, RelationshipItemBuilder.LATEST_VERSION, show, onlySimpleShow);
    }

    public static boolean updateTaCoKitConnection(ConnectionItem connectionItem, String version, boolean show,
            final boolean onlySimpleShow) {
        List<Relation> relations = RelationshipItemBuilder.getInstance().getItemsRelatedTo(connectionItem.getProperty().getId(),
                version, RelationshipItemBuilder.PROPERTY_RELATION);
        TaCoKitUpdateManager taCoKitUpdateManager = new TaCoKitUpdateManager(connectionItem, relations);
        return taCoKitUpdateManager.doWork(show, false);
    }

    @Override
    public Set<? extends IUpdateItemType> getTypes() {
        IUpdateItemType[] allUpdateItemTypes = UpdateManagerProviderDetector.INSTANCE.getAllUpdateItemTypes();
        Set<IUpdateItemType> types = new HashSet<IUpdateItemType>(Arrays.asList(allUpdateItemTypes));
        return types;
    }

    @Override
    public boolean filterForType(UpdateResult result) {
        if (result == null || parameter == null) {
            return false;
        }
        Object object = result.getParameter();
        if (object == null) {
            return false;
        }
        if (object == parameter) {
            return true;
        }
        if (object instanceof ConnectionItem && parameter instanceof ConnectionItem) {
            ConnectionItem parentConnItem = (ConnectionItem) parameter;
            ConnectionItem childConnItem = (ConnectionItem) object;
            TaCoKitConfigurationModel configuration = new TaCoKitConfigurationModel(childConnItem.getConnection());
            if (configuration != null && parentConnItem.getProperty() != null) {
                String parentItemId = parentConnItem.getProperty().getId();
                if (TaCoKitUtil.equals(parentItemId, configuration.getParentItemId())) {
                    return true;
                }
            }
        }
        return super.filterForType(result);
    }

    public static void updateTaCoKitSubConnection(TaCoKitConfigurationRuntimeData runtimeData) throws Exception {
        if (runtimeData != null) {
            ITaCoKitRepositoryNode repositoryNode = runtimeData.getTaCoKitRepositoryNode();
            if (repositoryNode != null && repositoryNode.isLeafNode()) {
                List<IRepositoryNode> listNodes = new ArrayList<IRepositoryNode>();
                getChildTaCoKitRepositoryNode(listNodes, repositoryNode.getChildren());
                for (IRepositoryNode subNode : listNodes) {
                    IRepositoryViewObject repObj = subNode.getObject();
                    if (repObj != null && repObj.getProperty() != null) {
                        boolean update = false;
                        Item subItem = repObj.getProperty().getItem();
                        if (subItem != null && subItem instanceof ConnectionItem) {
                            ConnectionItem subConnItem = (ConnectionItem) subItem;
                            if (subConnItem != null && subConnItem.getConnection() != null) {
                                //
                                TaCoKitConfigurationModel module = new TaCoKitConfigurationModel(subConnItem.getConnection());
                                final Map<String, PropertyDefinitionDecorator> tree = module.buildPropertyTree();
                                Map<String, String> properties = module.getProperties();
                                for (String key : tree.keySet()) {
                                    final Optional<String> configPath = module.findConfigPath(tree, key);
                                    if (configPath.isPresent() && !configPath.get().equals(key)) {
                                        ValueModel valueModel = module.getValue(key);
                                        if (valueModel != null) {
                                            Map<String, String> connProperties = subConnItem.getConnection().getProperties();
                                            if (StringUtils.isEmpty(valueModel.getValue())) {
                                                if (connProperties.containsKey(key)) {
                                                    connProperties.remove(key);
                                                    update = true;
                                                }
                                            } else if (!valueModel.getValue().equals(properties.get(key))) {
                                                connProperties.put(key, valueModel.getValue());
                                                update = true;
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        // save
                        if (update) {
                            final IProxyRepositoryFactory factory = ProxyRepositoryFactory.getInstance();
                            try {
                                factory.save(subItem);
                            } catch (PersistenceException e) {
                                ExceptionHandler.process(e);
                            }
                        }
                    }
                }
            }
        }
    }

    public static void getChildTaCoKitRepositoryNode(List<IRepositoryNode> listNodes, List<IRepositoryNode> childNodes) {
        for (IRepositoryNode childNode : childNodes) {
            if (childNode != null && childNode instanceof ITaCoKitRepositoryNode) {
                ITaCoKitRepositoryNode taCoKitNode = (ITaCoKitRepositoryNode) childNode;
                if (taCoKitNode.isLeafNode()) {
                    listNodes.add(taCoKitNode);
                }
                if (!taCoKitNode.getChildren().isEmpty()) {
                    getChildTaCoKitRepositoryNode(listNodes, childNode.getChildren());
                }
            }
        }
    }
}
