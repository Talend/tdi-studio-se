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
package org.talend.sdk.component.studio.metadata.model;

import static org.talend.sdk.component.studio.metadata.model.TaCoKitConfigurationModel.BuiltInKeys.*;
import static org.talend.sdk.component.studio.model.parameter.PropertyDefinitionDecorator.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.talend.commons.exception.ExceptionHandler;
import org.talend.core.model.metadata.builder.connection.Connection;
import org.talend.core.model.process.EParameterFieldType;
import org.talend.sdk.component.server.front.model.ConfigTypeNode;
import org.talend.sdk.component.server.front.model.SimplePropertyDefinition;
import org.talend.sdk.component.studio.Lookups;
import org.talend.sdk.component.studio.model.parameter.PropertyDefinitionDecorator;
import org.talend.sdk.component.studio.model.parameter.TaCoKitElementParameter;
import org.talend.sdk.component.studio.model.parameter.ValueConverter;
import org.talend.sdk.component.studio.model.parameter.WidgetTypeMapper;
import org.talend.sdk.component.studio.util.TaCoKitUtil;
import org.talend.utils.security.StudioEncryption;

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

    private boolean printEncryptionException = true;

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
            final Map<String, PropertyDefinitionDecorator> tree = buildPropertyTree();
            final Optional<String> configPath = findConfigPath(tree, key);
            final String modelRoot = findModelRoot();
            String keyStr = key;
            if (configPath.isPresent()) {
                keyStr = modelRoot + "." + key.substring(configPath.get().length() + 1, key.length()); //$NON-NLS-1$
            }
            ValueModel modelValue = parentModel.getValue(keyStr);
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

    private SimplePropertyDefinition getDefinition(String key) throws Exception {
        return getConfigTypeNode().getProperties().stream()
                .filter(propertyDefinition -> TaCoKitUtil.equals(key, propertyDefinition.getPath())).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("wrong key: " + key));
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

    public String getValueOfSelf(final String key) {
        String value = (String) getAllProperties().get(key);
        String decryptedValue = value;
        try {
            if (!TaCoKitUtil.isBlank(value) && contains(key)
                    && PropertyDefinitionDecorator.wrap(getDefinition(key)).isCredential()) {
                decryptedValue = StudioEncryption.getStudioEncryption(StudioEncryption.EncryptionKeyName.SYSTEM).decrypt(value);
                if (decryptedValue == null) {
                    // if null, means error occurs, just reuse the original value
                    decryptedValue = value;
                }
            }
        } catch (Exception e) {
            if (printEncryptionException) {
                ExceptionHandler.process(e);
            }
        }
        return decryptedValue;
    }

    public String computeKey(String key) throws Exception {
        final Map<String, PropertyDefinitionDecorator> tree = buildPropertyTree();
        final Optional<String> configPath = findConfigPath(tree, key);
        final String modelRoot = findModelRoot();

        if (configPath.isPresent()) {
            return key.replace(configPath.get(), modelRoot);
        } else {
            return key;
        }
    }

    public Map<String, PropertyDefinitionDecorator> buildPropertyTree() {
        final Map<String, PropertyDefinitionDecorator> tree = new HashMap<>();
        final Collection<PropertyDefinitionDecorator> properties = PropertyDefinitionDecorator
                .wrap(getConfigTypeNode().getProperties());
        properties.forEach(p -> tree.put(p.getPath(), p));
        return tree;
    }

    public String findModelRoot() {
        final Map<String, String> values = getProperties();
        List<String> possibleRoots = values.keySet().stream().filter(key -> key.contains(PATH_SEPARATOR))
                .map(key -> key.substring(0, key.indexOf(PATH_SEPARATOR))).distinct().collect(Collectors.toList());

        if (possibleRoots.size() != 1) {
            throw new IllegalStateException("Multiple roots found. Can't guess correct one: " + possibleRoots); //$NON-NLS-1$
        }
        return possibleRoots.get(0);
    }

    public Optional<String> findConfigPath(final Map<String, PropertyDefinitionDecorator> tree, final String key)
            throws Exception {
        TaCoKitConfigurationModel parentModel = getParentConfigurationModel();
        if (parentModel != null && parentModel.getConfigTypeNode() != null) {
            final String configType = parentModel.getConfigTypeNode().getConfigurationType();
            final String configName = parentModel.getConfigTypeNode().getName();
            if (configType != null && configName != null) {
                for (PropertyDefinitionDecorator current = tree.get(key); current != null; current = tree
                        .get(current.getParentPath())) {
                    if (configType.equals(current.getConfigurationType())
                            && configName.equals(current.getConfigurationTypeName())) {
                        return Optional.of(current.getPath());
                    }
                }
            }
        }
        return Optional.empty();
    }

    public Object convertParameterValue(String currentKey, String parentKey, String objectValue) {
        if (objectValue == null || StringUtils.isEmpty(currentKey) || StringUtils.isEmpty(parentKey)) {
            return objectValue;
        }
        boolean update = false;
        final List<Map<String, Object>> tableValues = ValueConverter.toTable(objectValue);
        final List<Map<String, Object>> converted = new ArrayList<>(tableValues.size());
        for (Object current : tableValues) {
            if (current != null && current instanceof Map) {
                Map<String, Object> line = (Map<String, Object>) current;
                Map<String, Object> convertedLine = new HashMap<>();
                for (String key : line.keySet()) {
                    if (key.startsWith(parentKey)) {
                        final String newKey = key.replace(parentKey, currentKey);
                        convertedLine.put(newKey, line.get(key));
                        update = true;
                    }
                }
                converted.add(convertedLine);
            }
        }
        if (update) {
            return converted.toString();
        }
        return objectValue;
    }

    public EParameterFieldType getEParameterFieldType(String key) {
        Map<String, PropertyDefinitionDecorator> tree = buildPropertyTree();
        PropertyDefinitionDecorator property = tree.get(key);
        if (property != null) {
            return new WidgetTypeMapper().getFieldType(property);
        }
        return EParameterFieldType.TEXT;
    }

    @SuppressWarnings("unchecked")
    public void setValue(final TaCoKitElementParameter parameter) {
        Objects.requireNonNull(parameter, "Parameter should not be null");
        setValue(parameter.getName(), parameter.getStringValue());
    }

    @SuppressWarnings("unchecked")
    public void setValue(String key, String value) {
        String originalValue = value;
        if (originalValue == null) {
            getAllProperties().remove(key);
        } else {
            String storeValue = originalValue;

            try {
                if (contains(key) && PropertyDefinitionDecorator.wrap(getDefinition(key)).isCredential()) {
                    storeValue = StudioEncryption.getStudioEncryption(StudioEncryption.EncryptionKeyName.SYSTEM)
                            .encrypt(originalValue);
                }
            } catch (Exception e) {
                ExceptionHandler.process(e);
            }

            getAllProperties().put(key, storeValue);
        }
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

    public void setPrintEncryptionException(boolean print) {
        this.printEncryptionException = print;
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
