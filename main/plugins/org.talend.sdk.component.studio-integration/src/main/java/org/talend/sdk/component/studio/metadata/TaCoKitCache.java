/**
 * Copyright (C) 2006-2021 Talend Inc. - www.talend.com
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
package org.talend.sdk.component.studio.metadata;

import java.util.HashMap;
import java.util.Map;

import org.talend.core.model.components.IComponent;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.sdk.component.server.front.model.ConfigTypeNode;
import org.talend.sdk.component.server.front.model.ConfigTypeNodes;
import org.talend.sdk.component.studio.Lookups;
import org.talend.sdk.component.studio.metadata.migration.TaCoKitMigrationManager;

/**
 * DOC cmeng class global comment. Detailled comment
 */
public class TaCoKitCache {

    private IComponent tacokitGuessSchemaComponent;

    private ConfigTypeNodes configTypeNodesCache;

    private Map<String, ConfigTypeNode> configTypeNodeMapCache;

    private TaCoKitMigrationManager migrationManager;

    /**
     * Stores family ConfigTypeNode. A key represents family name. Value - family ConfigTypeNode
     * It is initialized and filled up lazily
     */
    private Map<String, ConfigTypeNode> familyConfigTypes;

    private Map<ERepositoryObjectType, ConfigTypeNode> repTypeNodeMap;

    public TaCoKitCache() {
        repTypeNodeMap = new HashMap<>();
    }

    public Map<ERepositoryObjectType, ConfigTypeNode> getRepositoryObjectType2ConfigTypeNodeMap() {
        return repTypeNodeMap;
    }

    public ConfigTypeNode getFamilyNode(final ConfigTypeNode configNode) {
        if (configNode == null) {
            return null;
        }
        final String parentId = configNode.getParentId();
        if (parentId == null) {
            return configNode;
        }
        return getFamilyNode(getConfigTypeNodeMap().get(parentId));
    }

    /**
     * Retrieves ConfigTypeNode from cache for specified parameters
     *
     * @param familyName ConfigTypeNode family name
     * @param nodeName ConfigTypeNode name
     * @param configurationType ConfigTypeNode type, either dataset or datastore
     * @return ConfigTypeNode for specified parameters
     */
    public ConfigTypeNode getConfigTypeNode(final String familyName, final String nodeName,
            final String configurationType) {
        if (configurationType == null) {
            throw new IllegalArgumentException("configurationType should not be null");
        }
        if (familyConfigTypes == null) {
            fillFamilyConfig();
        }
        final ConfigTypeNode familyConfig = familyConfigTypes.get(familyName);
        if (familyConfig == null) {
            return null;
        }
        return findConfigTypeNode(familyConfigTypes.get(familyName), nodeName, configurationType);
    }

    /**
     * Retrieves ConfigTypeNode from cache for specified {@code id}
     *
     * @param id ConfigTypeNode id
     * @return ConfigTypeNode for specified id
     */
    public ConfigTypeNode getConfigTypeNode(final String id) {
        if (id == null) {
            throw new IllegalArgumentException("id should not be null");
        }
        ConfigTypeNode configTypeNode = getConfigTypeNodeMap().get(id);
        if (configTypeNode == null) {
            throw new IllegalArgumentException("Config type not found for id: " + id);
        }
        return configTypeNode;
    }

    /**
     * Finds only family ConfigTypeNode and puts them to {@link #familyConfigTypes}
     */
    private void fillFamilyConfig() {
        final Map<String, ConfigTypeNode> allNodes = getConfigTypeNodeMap();
        familyConfigTypes = new HashMap<>();
        allNodes.values().stream().filter(c -> c.getParentId() == null).forEach(
                c -> familyConfigTypes.put(c.getName(), c));
    }

    /**
     * Finds required ConfigTypeNode by its name and configuration types (dataset or datastore) in ConfigTypeNode tree.
     *
     * @param current a node which is visited
     * @param nodeName a node name of required ConfigTypeNode
     * @param configurationType configuration type of required ConfigTypeNode. It makes sense,
     * when datastore and dataset may have the same name within one component family
     * @return required ConfigTypeNode
     */
    private ConfigTypeNode findConfigTypeNode(final ConfigTypeNode current, final String nodeName,
            final String configurationType) {
        // node is found
        if (current.getName().equals(nodeName) && configurationType.equals(current.getConfigurationType())) {
            return current;
        }
        // it is leaf, but node wasn't found in this branch
        if (current.getEdges().isEmpty()) {
            return null;
        }
        for (final String edge : current.getEdges()) {
            final ConfigTypeNode node =
                    findConfigTypeNode(getConfigTypeNodeMap().get(edge), nodeName, configurationType);
            if (node != null) {
                return node;
            }
        }
        // node wasn't found at all
        return null;
    }

    public ConfigTypeNodes getConfigTypeNodes() {
        if (configTypeNodesCache == null) {
            configTypeNodesCache = Lookups.client().v1().configurationType().getRepositoryModel();
        }
        return configTypeNodesCache;
    }

    public Map<String, ConfigTypeNode> getConfigTypeNodeMap() {
        if (configTypeNodeMapCache == null || configTypeNodeMapCache.isEmpty()) {
            configTypeNodeMapCache = getConfigTypeNodes().getNodes();
        }
        return configTypeNodeMapCache;
    }

    public IComponent getTaCoKitGuessSchemaComponent() {
        return this.tacokitGuessSchemaComponent;
    }

    public void setTaCoKitGuessSchemaComponent(final IComponent guessSchemaComponent) {
        this.tacokitGuessSchemaComponent = guessSchemaComponent;
    }

    public TaCoKitMigrationManager getMigrationManager() {
        if (this.migrationManager == null) {
            this.migrationManager = new TaCoKitMigrationManager();
        }
        return this.migrationManager;
    }

    public void reset() {
        configTypeNodesCache = null;
        migrationManager = null;
        familyConfigTypes = null;
        if (configTypeNodeMapCache != null) {
            configTypeNodeMapCache.clear();
        }
    }
}
