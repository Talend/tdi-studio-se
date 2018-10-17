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
package org.talend.sdk.component.studio.metadata.model;

import static org.talend.sdk.component.studio.metadata.model.TaCoKitConfigurationModel.BuiltInKeys.TACOKIT_CONFIG_ID;
import static org.talend.sdk.component.studio.metadata.model.TaCoKitConfigurationModel.BuiltInKeys.TACOKIT_CONFIG_PARENT_ID;
import static org.talend.sdk.component.studio.metadata.model.TaCoKitConfigurationModel.BuiltInKeys.TACOKIT_PARENT_ITEM_ID;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.talend.core.model.metadata.builder.connection.Connection;
import org.talend.sdk.component.server.front.model.ConfigTypeNode;
import org.talend.sdk.component.server.front.model.SimplePropertyDefinition;
import org.talend.sdk.component.studio.Lookups;
import org.talend.sdk.component.studio.model.parameter.TaCoKitElementParameter;
import org.talend.sdk.component.studio.util.TaCoKitUtil;

/**
 * DOC cmeng class global comment. Detailled comment
 *
 * Provides convenient API for updating {@link Connection} properties
 */
public class TaCoKitConfigurationModel {

    private final Connection connection;

    private final ConfigTypeNode configType;

    private TaCoKitConfigurationModel parentConfigurationModel;

    private String parentConfigurationModelItemId;

    public TaCoKitConfigurationModel(final Connection connection) {
        this(connection, Lookups.taCoKitCache().getConfigTypeNode(getConfigId(connection)));
    }

    public TaCoKitConfigurationModel(final Connection connection, final ConfigTypeNode configType) {
        this.connection = connection;
        this.configType = configType;
        setConfigurationId(configType.getId());
        setParentConfigurationId(configType.getParentId());
    }

    @SuppressWarnings("unchecked")
    private void setConfigurationId(final String id) {
        getAllProperties().put(TACOKIT_CONFIG_ID, id);
    }

    @SuppressWarnings("unchecked")
    private void setParentConfigurationId(final String parentId) {
        getAllProperties().put(TACOKIT_CONFIG_PARENT_ID, parentId);
    }

    @SuppressWarnings("deprecation")
    private static String getConfigId(final Connection connection) {
        return (String) connection.getProperties().get(TACOKIT_CONFIG_ID);
    }

    public static boolean isTacokit(final Connection connection) {
        return !StringUtils.isEmpty(getConfigId(connection));
    }

    public int getVersion() {
        final String version = Optional.ofNullable(getProperties().get(configType.getProperties().stream()
                .filter(p -> p.getName().equals(p.getPath()))
                .findFirst().map(SimplePropertyDefinition::getPath).orElse("configuration") + ".__version"))
                .orElse("-1");
        return Integer.parseInt(version);
    }

    public int getConfigurationVersion() {
        return configType.getVersion();
    }

    public String getConfigurationId() {
        return configType.getId();
    }

    public String getParentConfigurationId() {
        return (String) getAllProperties().get(TACOKIT_CONFIG_PARENT_ID);
    }

    public String getParentItemId() {
        return (String) getAllProperties().get(TACOKIT_PARENT_ITEM_ID);
    }

    @SuppressWarnings("unchecked")
    public void setParentItemId(final String parentItemId) {
        getAllProperties().put(TACOKIT_PARENT_ITEM_ID, parentItemId);
    }

    public ValueModel getValue(final String key) throws Exception {
        TaCoKitConfigurationModel parentModel = getParentConfigurationModel();
        if (parentModel != null) {
            ValueModel modelValue = parentModel.getValue(key);
            if (modelValue == null) {
                if (parentModel.contains(key)) {
                    return new ValueModel(parentModel, null, getValueType(parentModel.getConfigTypeNode(), key));
                }
            } else {
                return modelValue;
            }
        }
        String value = getValueOfSelf(key);
        if (value != null || contains(key)) {
            return new ValueModel(this, value, getValueType(getConfigTypeNode(), key));
        }
        return null;
    }

    private String getValueType(final ConfigTypeNode typeNode, final String key) throws Exception {
        return typeNode.getProperties().stream()
                .filter(propertyDefinition -> TaCoKitUtil.equals(key, propertyDefinition.getPath()))
                .map(SimplePropertyDefinition::getType).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("wrong key: " + key));
    }

    private boolean contains(final String key) {
        List<SimplePropertyDefinition> properties = getConfigTypeNode().getProperties();
        if (key == null || key.isEmpty() || properties == null || properties.isEmpty()) {
            return false;
        }
        for (SimplePropertyDefinition property : properties) {
            if (TaCoKitUtil.equals(key, property.getPath())) {
                return true;
            }
        }
        return false;
    }

    private String getValueOfSelf(final String key) {
        return (String) getAllProperties().get(key);
    }

    @SuppressWarnings("unchecked")
    public void setValue(final TaCoKitElementParameter parameter) {
        Objects.requireNonNull(parameter, "Parameter should not be null");
        getAllProperties().put(parameter.getName(), parameter.getStringValue());
    }

    public ConfigTypeNode getConfigTypeNode() {
        return configType;
    }

    public TaCoKitConfigurationModel getParentConfigurationModel() throws Exception {
        if (parentConfigurationModel == null
                || !TaCoKitUtil.equals(parentConfigurationModelItemId, getParentItemId())) {
            parentConfigurationModel = null;
            parentConfigurationModelItemId = getParentItemId();
            if (!TaCoKitUtil.isEmpty(parentConfigurationModelItemId)) {
                parentConfigurationModel = TaCoKitUtil.getTaCoKitConfigurationModel(parentConfigurationModelItemId);
            }
        }
        return parentConfigurationModel;
    }

    /**
     * Retrieves {@link Connection} properties holder.
     * Properties should contain only String values as they values are not converted further during serialization
     *
     * @return map of connection properties
     */
    @SuppressWarnings({ "deprecation", "rawtypes" })
    private Map getAllProperties() {
        return connection.getProperties();
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private Map getBuiltInProperties() {
        final Map builtIn = new HashMap();
        for (final Object o : getAllProperties().entrySet()) {
            final Entry entry = (Entry) o;
            final String key = (String) entry.getKey();
            if (BuiltInKeys.isBuiltIn(key)) {
                builtIn.put(key, entry.getValue());
            }
        }
        return builtIn;
    }

    /**
     * Returns properties view, which doesn't include built-in (service) properties
     *
     * @return properties view
     */
    @SuppressWarnings("rawtypes")
    public Map<String, String> getProperties() {
        Map<String, String> properties = new HashMap<>();
        for (final Object o : getAllProperties().entrySet()) {
            final Entry entry = (Entry) o;
            final String key = (String) entry.getKey();
            if (!BuiltInKeys.isBuiltIn(key)) {
                properties.put(key, (String) entry.getValue());
            }
        }
        return Collections.unmodifiableMap(properties);
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private void clear() {
        final Map builtIn = getBuiltInProperties();
        getAllProperties().clear();
        getAllProperties().putAll(builtIn);
    }

    public Connection getConnection() {
        return connection;
    }

    @SuppressWarnings("unchecked")
    public void migrate(final Map<String, String> migratedProperties) {
        clear();
        getAllProperties().putAll(migratedProperties);
    }

    /**
     * Checks whether Configuration requires migration. Configuration requires
     * migration in case current Configuration version is greater than persisted
     * version. In case versions are equal migration is not required. If persisted
     * version is greater than current version, then IllegalStateException is thrown
     *
     * @return true, if migration is required
     */
    public boolean needsMigration() {
        final int currentVersion = getConfigurationVersion();
        final int persistedVersion = getVersion();
        if (currentVersion < persistedVersion) {
            throw new IllegalStateException(
                    "current version: " + currentVersion + " persisted version: " + persistedVersion);
        }
        return currentVersion != persistedVersion;
    }

    public static class ValueModel {

        private final TaCoKitConfigurationModel configurationModel;

        private final String value;

        private final String type;

        public ValueModel(final TaCoKitConfigurationModel configurationModel, final String value, final String type) {
            this.configurationModel = configurationModel;
            this.value = value;
            this.type = type;
        }

        public TaCoKitConfigurationModel getConfigurationModel() {
            return this.configurationModel;
        }

        public String getValue() {
            return this.value;
        }

        public String getType() {
            return type;
        }

        @Override
        public String toString() {
            return "TaCoKitConfigurationModel.ValueModel(configurationModel=" + this.getConfigurationModel() + ", value="
                    + this.getValue() + ", type=" + this.getType() + ")";
        }

    }

    public static class BuiltInKeys {

        static final String TACOKIT_CONFIG_ID = "__TACOKIT_CONFIG_ID"; //$NON-NLS-1$

        static final String TACOKIT_CONFIG_PARENT_ID = "__TACOKIT_CONFIG_PARENT_ID"; //$NON-NLS-1$

        static final String TACOKIT_PARENT_ITEM_ID = "__TACOKIT_PARENT_ITEM_ID"; //$NON-NLS-1$

        private static final Set<String> keys = new HashSet<>(Arrays.asList(TACOKIT_CONFIG_ID,
                TACOKIT_CONFIG_PARENT_ID, TACOKIT_PARENT_ITEM_ID));

        static boolean isBuiltIn(final String key) {
            return keys.contains(key);
        }

    }
}
