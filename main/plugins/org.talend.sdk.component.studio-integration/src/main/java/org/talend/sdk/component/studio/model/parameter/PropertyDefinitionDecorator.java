/**
 * Copyright (C) 2006-2018 Talend Inc. - www.talend.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.talend.sdk.component.studio.model.parameter;

import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static org.talend.sdk.component.studio.model.parameter.Metadatas.ACTION_HEALTHCHECK;
import static org.talend.sdk.component.studio.model.parameter.Metadatas.ACTION_SUGGESTIONS_NAME;
import static org.talend.sdk.component.studio.model.parameter.Metadatas.ACTION_SUGGESTIONS_PARAMETERS;
import static org.talend.sdk.component.studio.model.parameter.Metadatas.ACTION_UPDATABLE_STRATEGY;
import static org.talend.sdk.component.studio.model.parameter.Metadatas.ACTION_UPDATABLE_TARGET;
import static org.talend.sdk.component.studio.model.parameter.Metadatas.ACTION_VALIDATION_NAME;
import static org.talend.sdk.component.studio.model.parameter.Metadatas.ACTION_VALIDATION_PARAMETERS;
import static org.talend.sdk.component.studio.model.parameter.Metadatas.CONDITION_IF_EVALUTIONSTRATEGY;
import static org.talend.sdk.component.studio.model.parameter.Metadatas.CONDITION_IF_NEGATE;
import static org.talend.sdk.component.studio.model.parameter.Metadatas.CONDITION_IF_TARGET;
import static org.talend.sdk.component.studio.model.parameter.Metadatas.CONDITION_IF_VALUE;
import static org.talend.sdk.component.studio.model.parameter.Metadatas.CONFIG_NAME;
import static org.talend.sdk.component.studio.model.parameter.Metadatas.CONFIG_TYPE;
import static org.talend.sdk.component.studio.model.parameter.Metadatas.MAIN_FORM;
import static org.talend.sdk.component.studio.model.parameter.Metadatas.ORDER_SEPARATOR;
import static org.talend.sdk.component.studio.model.parameter.Metadatas.PARAMETER_INDEX;
import static org.talend.sdk.component.studio.model.parameter.Metadatas.UI_GRIDLAYOUT_PREFIX;
import static org.talend.sdk.component.studio.model.parameter.Metadatas.UI_GRIDLAYOUT_SUFFIX;
import static org.talend.sdk.component.studio.model.parameter.Metadatas.UI_OPTIONS_ORDER;
import static org.talend.sdk.component.studio.model.parameter.Metadatas.VALUE_SEPARATOR;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import javax.json.bind.annotation.JsonbCreator;
import javax.json.bind.annotation.JsonbProperty;

import org.talend.sdk.component.server.front.model.PropertyValidation;
import org.talend.sdk.component.server.front.model.SimplePropertyDefinition;
import org.talend.sdk.component.studio.model.parameter.condition.ConditionGroup;
import org.talend.sdk.component.studio.model.parameter.resolver.AbsolutePathResolver;

/**
 * Extends functionality of {@link SimplePropertyDefinition}
 * It doesn't allow to change <code>delegate</code> state
 */
public class PropertyDefinitionDecorator extends SimplePropertyDefinition {
    private static final AbsolutePathResolver PATH_RESOLVER = new AbsolutePathResolver();

    /**
     * Separator in property path
     */
    public static final String PATH_SEPARATOR = ".";

    /**
     * Denotes that some property has no parent property
     */
    static final String NO_PARENT_ID = "";

    /**
     * Suffix used in id ({@link SimplePropertyDefinition#getPath()}), which denotes Array typed property
     * (which is Table property in Studio)
     */
    private static final String ARRAY_PATH = "[]";

    /**
     * Regex, which contains 2 separators used in ui:gridlayout value: ',' and '|'
     * It used to split ui:gridlayout value
     */
    private static final String GRIDLAYOUT_SEPARATOR = ",|\\|";

    private final SimplePropertyDefinition delegate;

    private volatile String parentPath;

    /**
     * Creates instance by wrapping existing {@link SimplePropertyDefinition} instance
     * All calls to {@link SimplePropertyDefinition} API will be delegated to wrapped instance
     *
     * @param property {@link SimplePropertyDefinition} to wrap
     */
    @JsonbCreator
    public PropertyDefinitionDecorator(@JsonbProperty("delegate") final SimplePropertyDefinition property) {
        this.delegate = property;
    }

    /**
     * Wraps {@link Collection} or {@link SimplePropertyDefinition} to {@link Collection} of
     * {@link PropertyDefinitionDecorator}
     *
     * @param properties original properties collection
     * @return wrapped properties
     */
    public static Collection<PropertyDefinitionDecorator> wrap(final Collection<SimplePropertyDefinition> properties) {
        return properties.stream()
                .map(PropertyDefinitionDecorator::new)
                .collect(toList());
    }
    
    /**
     * Wraps {@link SimplePropertyDefinition} in {@link PropertyDefinitionDecorator}
     * 
     * @param property original property
     * @return wrapped property
     */
    public static PropertyDefinitionDecorator wrap(final SimplePropertyDefinition property) {
        return new PropertyDefinitionDecorator(property);
    }

    /**
     * Returns path for parent {@link SimplePropertyDefinition}
     *
     * @return parent path
     */
    public String getParentPath() {
        if (parentPath != null) {
            return parentPath;
        }
        synchronized (this) {
            if (parentPath != null) {
                return parentPath;
            }
            String path = delegate.getPath();
            if (!path.contains(PATH_SEPARATOR)) {
                return NO_PARENT_ID;
            }
            if (path.endsWith(ARRAY_PATH)) {
                return path.substring(0, path.length() - ARRAY_PATH.length());
            }
            // object case
            path = path.substring(0, path.lastIndexOf('.'));
            if (path.endsWith(ARRAY_PATH)) {
                path = path.substring(0, path.length() - ARRAY_PATH.length());
            }
            parentPath = path;
            return parentPath;
        }
    }

    /**
     * Returns children names specified in ui::gridlayout metadata value.
     * Note, if Property has no any grid layout value, then such Property
     * has Main form by default. Such Main form contains either Properties
     * specified in ui::optionsorder metadata value or all Properties, when
     * ui::optionsorder is also absent
     *
     * @param form Name of UI form
     * @return children names specified in ui::gridlayout metadata value
     */
    Set<String> getChildrenNames(final String form) {
        if (hasGridLayout(form)) {
            final String gridLayout = getGridLayout(form);
            final String[] names = gridLayout.split(GRIDLAYOUT_SEPARATOR);
            return Stream.of(names).collect(toSet());
        }
        return Collections.emptySet();
    }

    /**
     * Returns children names specified in ui:optionsorder metadata value.
     *
     * @return children names specified in ui:optionsorder metadata value
     */
    Set<String> getOptionsOrderNames() {
        if (hasOptionsOrder()) {
            final String optionsOrder = delegate.getMetadata().get(UI_OPTIONS_ORDER);
            final String[] names = optionsOrder.split(ORDER_SEPARATOR);
            return new HashSet<>(Arrays.asList(names));
        }
        return Collections.emptySet();
    }

    /**
     * Computes order in which children should appear on UI
     * Order is represented as a map, which key is child name and value is child order
     *
     * @param form Name of UI form
     * @return order map or null, if there is no order for specified form
     */
    HashMap<String, Integer> getChildrenOrder(final String form) {
        if (MAIN_FORM.equals(form)) {
            return getMainChildrenOrder();
        }
        return getGridLayoutOrder(form);
    }

    /**
     * Computes order in which Main children should appear on UI.
     * It is computes either according ui:gridlayout or ui:optionsorder.
     * If both metadata is absent, then there is no order for Main form and
     * null is returned. </br>
     * Order is represented as a map, which key is child name and value is child order
     *
     * @return order map or null, if there is no order for Main form
     */
    private HashMap<String, Integer> getMainChildrenOrder() {
        if (hasGridLayout(MAIN_FORM)) {
            return getGridLayoutOrder(MAIN_FORM);
        }
        if (hasOptionsOrder()) {
            return getOptionsOrder();
        }
        return null;
    }

    /**
     * Computes order in which children should appear on UI according ui:gridlayout metadata value
     * Order is represented as a map, which key is child name and value is child order
     *
     * @param form Name of UI form
     * @return order map or null, if there is no grid layout for specified form
     */
    private HashMap<String, Integer> getGridLayoutOrder(final String form) {
        if (!hasGridLayout(form)) {
            return null;
        }
        final HashMap<String, Integer> order = new HashMap<>();
        final String gridLayout = getGridLayout(form);
        final String[] children = gridLayout.split(GRIDLAYOUT_SEPARATOR);
        for (int i = 0; i < children.length; i++) {
            order.put(children[i], i);
        }
        return order;
    }

    /**
     * Computes order in which children should appear on UI according ui:optionsorder metadata value
     * Order is represented as a map, which key is child name and value is child order
     *
     * @return order map or null, if there is no grid layout for specified form
     */
    private HashMap<String, Integer> getOptionsOrder() {
        final HashMap<String, Integer> order = new HashMap<>();
        final String optionsOrder = delegate.getMetadata().get(UI_OPTIONS_ORDER);
        final String[] children = optionsOrder.split(ORDER_SEPARATOR);
        for (int i = 0; i < children.length; i++) {
            order.put(children[i], i);
        }
        return order;
    }

    /**
     * Checks whether it has grid layout metadata for specified <code>form</code>
     *
     * @param form Name of UI form
     * @return true, if is has requested grid layout; false - otherwise
     */
    boolean hasGridLayout(final String form) {
        if (form == null) {
            return false;
        }
        final String gridLayout = delegate.getMetadata().get(buildGridLayoutKey(form));
        return gridLayout != null && !gridLayout.isEmpty();
    }

    /**
     * Checks whether it has any ui:gridlayout metadata
     *
     * @return true, if it has any grid layout; false - otherwise
     */
    boolean hasGridLayouts() {
        final Set<String> keys = delegate.getMetadata().keySet();
        return keys.stream()
                .filter(key -> key.startsWith(UI_GRIDLAYOUT_PREFIX))
                .map(key -> delegate.getMetadata().get(key))
                .anyMatch(gridLayout -> gridLayout != null && !gridLayout.isEmpty());
    }

    /**
     * Checks whether it has options order metadata
     *
     * @return true, if it has; false - otherwise
     */
    boolean hasOptionsOrder() {
        return delegate.getMetadata().containsKey(UI_OPTIONS_ORDER);
    }

    /**
     * Checks whether it has configurationtype::type metadata
     *
     * @return true, it has configuration type; false - otherwise
     */
    boolean hasConfigurationType() {
        return delegate.getMetadata().containsKey(CONFIG_TYPE);
    }

    /**
     * Returns configurationtype::type metadata value or null if it is absent
     *
     * @return configuration type
     */
    public String getConfigurationType() {
        return delegate.getMetadata().get(CONFIG_TYPE);
    }

    /**
     * Returns configurationtype::name metadata value or null if it is absent
     *
     * @return configuration type name
     */
    public String getConfigurationTypeName() {
        return delegate.getMetadata().get(CONFIG_NAME);
    }

    /**
     * Builds full grid layout metadata key with specified <code>formName</code>
     *
     * @param formName Name of UI form
     * @return grid layout metadata key
     */
    private String buildGridLayoutKey(final String formName) {
        return UI_GRIDLAYOUT_PREFIX + formName + UI_GRIDLAYOUT_SUFFIX;
    }

    /**
     * Checks whether specified <code>child</code> is a column on specified <code>form</code>
     * It is a column, when it is second property on the same line.
     * Consider following ui::gridlayout value: "p1|p2,p3,p4".
     * p1 and p2 are not columns and p3, p4 are columns.
     *
     * @param child Child Property name
     * @param form  Name of form
     * @return true, if it is column; false - otherwise
     */
    boolean isColumn(final String child, final String form) {
        if (!hasGridLayout(form)) {
            return false;
        }
        final String gridLayout = getGridLayout(form);
        final String[] rows = gridLayout.split("\\|");
        for (final String row : rows) {
            final String[] columns = row.split(",");
            for (int i = 1; i < columns.length; i++) {
                if (child.equals(columns[i])) {
                    return true;
                }
            }
        }
        return false;
    }

    String getGridLayout(final String form) {
        if (!hasGridLayout(form)) {
            throw new IllegalArgumentException("no gridlayout for form " + form);
        }
        return delegate.getMetadata().get(buildGridLayoutKey(form));
    }

    ConditionGroup getConditions() {
        return new ConditionGroup(delegate.getMetadata()
                .entrySet()
                .stream()
                .filter(meta -> meta.getKey().startsWith(CONDITION_IF_TARGET))
                .map(meta -> {
                    final String[] split = meta.getKey().split("::");
                    final String index = split.length == 4 ? "::" + split[split.length - 1] : "";
                    final String valueKey = CONDITION_IF_VALUE + index;
                    final String negateKey = CONDITION_IF_NEGATE + index;
                    final String evaluationStrategyKey = CONDITION_IF_EVALUTIONSTRATEGY + index;
                    final String absoluteTargetPath = PATH_RESOLVER.resolvePath(delegate.getPath(), meta.getValue());
                    return new Condition(
                            delegate.getMetadata().getOrDefault(valueKey, "true").split(VALUE_SEPARATOR),
                            absoluteTargetPath,
                            Boolean.parseBoolean(delegate.getMetadata().getOrDefault(negateKey, "false")),
                            delegate.getMetadata().getOrDefault(evaluationStrategyKey, "DEFAULT"));
                }).collect(toList()), "AND".equalsIgnoreCase(delegate.getMetadata().getOrDefault("condition::ifs::operator", "AND")));
    }

    /**
     * Checks whether it has one of constraints
     *
     * @return true, if it has constraint; false - otherwise
     */
    boolean hasConstraint() {
        final PropertyValidation validation = delegate.getValidation();
        if (validation == null) {
            return false;
        }
        if (validation.getRequired() != null) {
            return true;
        }
        if (validation.getUniqueItems() != null) {
            return true;
        }
        if (validation.getMax() != null) {
            return true;
        }
        if (validation.getMaxItems() != null) {
            return true;
        }
        if (validation.getMaxLength() != null) {
            return true;
        }
        if (validation.getMin() != null) {
            return true;
        }
        if (validation.getMinItems() != null) {
            return true;
        }
        if (validation.getMinLength() != null) {
            return true;
        }
        if (validation.getPattern() != null) {
            return true;
        }
        return false;
    }

    boolean isRequired() {
        final PropertyValidation validation = delegate.getValidation();
        if (validation == null) {
            return false;
        }
        final Boolean isRequired = validation.getRequired();
        if (isRequired != null && isRequired) {
            return true;
        }
        return false;
    }

    /**
     * Checks whether it has action::validation metadata
     *
     * @return true, it it has action::validation metadata; false otherwise
     */
    public boolean hasValidation() {
        return delegate.getMetadata().containsKey(ACTION_VALIDATION_NAME)
                && delegate.getMetadata().containsKey(ACTION_VALIDATION_PARAMETERS);
    }

    public String getValidationName() {
        if (!hasValidation()) {
            throw new IllegalStateException("Property has no validation");
        }
        return delegate.getMetadata().get(ACTION_VALIDATION_NAME);
    }

    public List<String> getValidationParameters() {
        if (!hasValidation()) {
            throw new IllegalStateException("Property has no validation");
        }
        final String parametersValue = delegate.getMetadata().get(ACTION_VALIDATION_PARAMETERS);
        return Arrays.asList(parametersValue.split(VALUE_SEPARATOR));
    }

    boolean isCheckable() {
        return delegate.getMetadata().containsKey(ACTION_HEALTHCHECK);
    }

    public String getHealthCheckName() {
        if (!isCheckable()) {
            throw new IllegalArgumentException("It is not checkable");
        }
        return delegate.getMetadata().get(ACTION_HEALTHCHECK);
    }

    Connection getConnection() {
        final String type = delegate.getMetadata().get(Metadatas.UI_STRUCTURE_TYPE);
        final String value = delegate.getMetadata().get(Metadatas.UI_STRUCTURE_VALUE);
        if (type == null || value == null) {
            throw new IllegalStateException("property has no structure");
        }
        return new Connection(Connection.Type.valueOf(type), value);
    }

    String getSchemaName() {
        final Connection.Type connectionType = getConnection().getType();
        return connectionType + "$$" + getPath();
    }

    @Override
    public String getPath() {
        return delegate.getPath();
    }

    @Override
    public void setPath(final String path) {
        delegate.setPath(path);
    }

    @Override
    public String getName() {
        return delegate.getName();
    }

    @Override
    public void setName(final String name) {
        delegate.setName(name);
    }

    @Override
    public String getDisplayName() {
        return delegate.getDisplayName();
    }

    @Override
    public void setDisplayName(final String displayName) {
        delegate.setDisplayName(displayName);
    }

    @Override
    public String getType() {
        return delegate.getType();
    }

    @Override
    public void setType(final String type) {
        delegate.setType(type);
    }

    @Override
    public String getDefaultValue() {
        return delegate.getDefaultValue();
    }

    @Override
    public void setDefaultValue(final String defaultValue) {
        delegate.setDefaultValue(defaultValue);
    }

    @Override
    public PropertyValidation getValidation() {
        return delegate.getValidation();
    }

    @Override
    public void setValidation(final PropertyValidation validation) {
        delegate.setValidation(validation);
    }

    @Override
    public Map<String, String> getMetadata() {
        return delegate.getMetadata();
    }

    @Override
    public void setMetadata(final Map<String, String> metadata) {
        delegate.setMetadata(metadata);
    }

    @Override
    public String getPlaceholder() {
        return delegate.getPlaceholder();
    }

    @Override
    public void setPlaceholder(final String placeholder) {
        delegate.setPlaceholder(placeholder);
    }

    @Override
    public LinkedHashMap<String, String> getProposalDisplayNames() {
        return delegate.getProposalDisplayNames();
    }
    
    boolean hasSuggestions() {
        return delegate.getMetadata().containsKey(ACTION_SUGGESTIONS_NAME)
                && delegate.getMetadata().containsKey(ACTION_SUGGESTIONS_PARAMETERS);
    }
    
    public Suggestions getSuggestions() {
        if (!hasSuggestions()) {
            throw new IllegalStateException("Property has no suggestions");
        }
        final String name = delegate.getMetadata().get(ACTION_SUGGESTIONS_NAME);
        final String parametersValue = delegate.getMetadata().get(ACTION_SUGGESTIONS_PARAMETERS);
        final List<String> parameters = Arrays.asList(parametersValue.split(VALUE_SEPARATOR));
        return new Suggestions(name, parameters);
    }

    public Optional<Updatable> getUpdatable() {
        final String strategy = delegate.getMetadata().get(ACTION_UPDATABLE_STRATEGY);
        final String target = delegate.getMetadata().get(ACTION_UPDATABLE_TARGET);
        if (target != null && strategy != null) {
            return Optional.of(new Updatable(strategy, target));
        } else {
            return Optional.empty();
        }
    }
    
    public Parameter getParameter() {
        return ofNullable(delegate.getMetadata().get(PARAMETER_INDEX))
                .map(s -> new Parameter(Integer.parseInt(s)))
                .orElse(new Parameter());
    }

    @Override
    public int hashCode() {
        return delegate.hashCode();
    }

    @Override
    public String toString() {
        return delegate.toString();
    }

    public static class Condition {

        /**
         * Path to property to be evaluated (corresponds to ActiveIf.target())
         */
        private final String targetPath;

        private final boolean negation;

        private final String evaluationStrategy;

        private final String[] values;

        private final int hash;

        public Condition(final String[] values, final String targetPath,
                         final boolean negation, final String evaluationStrategy) {
            this.targetPath = targetPath;
            this.values = values;
            this.negation = negation;
            this.evaluationStrategy = evaluationStrategy == null || evaluationStrategy.isEmpty() ? "DEFAULT" : evaluationStrategy;
            this.hash = 31 * Objects.hash(targetPath, negation, evaluationStrategy) + Arrays.hashCode(values);
        }

        public String[] getValues() {
            return this.values;
        }

        public String getTargetPath() {
            return targetPath;
        }

        public boolean isNegation() {
            return negation;
        }

        public String getEvaluationStrategy() {
            return evaluationStrategy;
        }

        @Override
        public String toString() {
            return "PropertyDefinitionDecorator.Condition(target=" + this.getTargetPath() + ", values="
                    + Arrays.deepToString(this.getValues()) + ")";
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o)
                return true;
            if (o == null || getClass() != o.getClass())
                return false;
            final Condition condition = (Condition) o;
            return Objects.equals(targetPath, condition.targetPath) &&
                    Arrays.equals(values, condition.values);
        }

        @Override
        public int hashCode() {
            return hash;
        }
    }

    public static class Updatable {

        /**
         * Strategy defines, how resulting value is computed
         */
        private final String strategy;

        /**
         * Path to property, which triggers other property update on change.
         */
        private final String target;

        public Updatable(final String strategy, final String target) {
            this.strategy = strategy;
            this.target = target;
        }

        public String getTarget() {
            return target;
        }

        public String getStrategy() {
            return strategy;
        }
    }

    public static class Connection {

        private final Type type;

        private final String value;

        public enum Type {
            IN,
            OUT;
        }

        public Connection(final Type type, final String value) {
            this.type = type;
            this.value = value;
        }

        public Type getType() {
            return this.type;
        }

        public String getValue() {
            return this.value;
        }

        @Override
        public boolean equals(final Object o) {
            if (o == this)
                return true;
            if (!(o instanceof Connection))
                return false;
            final Connection other = (Connection) o;
            if (!other.canEqual(this))
                return false;
            final Object this$type = this.getType();
            final Object other$type = other.getType();
            if (this$type == null ? other$type != null : !this$type.equals(other$type))
                return false;
            final Object this$value = this.getValue();
            final Object other$value = other.getValue();
            if (this$value == null ? other$value != null : !this$value.equals(other$value))
                return false;
            return true;
        }

        protected boolean canEqual(final Object other) {
            return other instanceof Connection;
        }

        @Override
        public int hashCode() {
            final int PRIME = 59;
            int result = 1;
            final Object $type = this.getType();
            result = result * PRIME + ($type == null ? 43 : $type.hashCode());
            final Object $value = this.getValue();
            result = result * PRIME + ($value == null ? 43 : $value.hashCode());
            return result;
        }

        @Override
        public String toString() {
            return "PropertyDefinitionDecorator.Connection(type=" + this.getType() + ", value=" + this.getValue() + ")";
        }
    }

    public static class Suggestions {

        private final String name;

        private final List<String> parameters;

        Suggestions(final String name, final List<String> parameters) {
            this.name = name;
            this.parameters = parameters;
        }

        public String getName() {
            return name;
        }

        public List<String> getParameters() {
            return parameters;
        }

        @Override
        public String toString() {
            return "Suggestions(name=" + this.getName() + ", parameters=" + this.getParameters() + ")";
        }
    }
    
    public static class Parameter {
        
        private static int UNDEFINED = -1;
        
        private int index = UNDEFINED;
        
        Parameter() {
            // no-op
        }
        
        Parameter(final int index) {
            this.index = index;
        }
        
        public int getIndex() {
            return index;
        }
        
        public boolean isRoot() {
            return index != UNDEFINED;
        }
    }
}
