/**
 * Copyright (C) 2006-2019 Talend Inc. - www.talend.com
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

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;
import static org.talend.sdk.component.studio.model.parameter.Metadatas.ACTION_HEALTHCHECK;
import static org.talend.sdk.component.studio.model.parameter.Metadatas.ACTION_SUGGESTIONS_NAME;
import static org.talend.sdk.component.studio.model.parameter.Metadatas.ACTION_SUGGESTIONS_PARAMETERS;
import static org.talend.sdk.component.studio.model.parameter.Metadatas.ACTION_UPDATABLE_AFTER;
import static org.talend.sdk.component.studio.model.parameter.Metadatas.ACTION_UPDATABLE_PARAMETERS;
import static org.talend.sdk.component.studio.model.parameter.Metadatas.ACTION_UPDATABLE_VALUE;
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
import static org.talend.sdk.component.studio.model.parameter.Metadatas.UI_CREDENTIAL;
import static org.talend.sdk.component.studio.model.parameter.Metadatas.UI_GRIDLAYOUT_PREFIX;
import static org.talend.sdk.component.studio.model.parameter.Metadatas.UI_GRIDLAYOUT_SUFFIX;
import static org.talend.sdk.component.studio.model.parameter.Metadatas.UI_OPTIONS_ORDER;
import static org.talend.sdk.component.studio.model.parameter.Metadatas.VALUE_SEPARATOR;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import javax.json.bind.annotation.JsonbCreator;
import javax.json.bind.annotation.JsonbProperty;

import org.talend.core.model.process.EParameterFieldType;
import org.talend.sdk.component.form.internal.converter.impl.widget.path.AbsolutePathResolver;
import org.talend.sdk.component.server.front.model.PropertyValidation;
import org.talend.sdk.component.server.front.model.SimplePropertyDefinition;
import org.talend.sdk.component.studio.model.parameter.condition.ConditionGroup;

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
    List<String> getChildrenNames(final String form) {
        if (hasGridLayout(form)) {
            final String gridLayout = getGridLayout(form);
            return asList(gridLayout.split(GRIDLAYOUT_SEPARATOR));
        }
        return Collections.emptyList();
    }

    /**
     * Returns children names specified in ui:optionsorder metadata value.
     *
     * @return children names specified in ui:optionsorder metadata value
     */
    List<String> getOptionsOrderNames() {
        if (hasOptionsOrder()) {
            final String optionsOrder = delegate.getMetadata().get(UI_OPTIONS_ORDER);
            final String[] names = optionsOrder.split(ORDER_SEPARATOR);
            return Arrays.asList(names);
        }
        return Collections.emptyList();
    }

    /**
     * Computes order in which children should appear on UI
     * Order is represented as a map, which key is child name and value is child order
     *
     * @param form Name of UI form
     * @return order map or null, if there is no order for specified form
     */
    Map<String, Integer> getChildrenOrder(final String form) {
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
                    final String absoluteTargetPath = PATH_RESOLVER.resolveProperty(delegate.getPath(), meta.getValue());
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

    // TODO: all these trigger specific methods are quit duplicating the same code,
    // we should try to align it since all trigger use the same kind of API
    // TODO: avoid NPE here as not every property has action
    public List<String> getParameters(final String actionType) {
        return Arrays.asList(delegate.getMetadata().get("action::" + actionType + "::parameters").split(VALUE_SEPARATOR));
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
        final String discoverSchema = delegate.getMetadata().get(Metadatas.UI_STRUCTURE_DISCOVERSCHEMA);
        if (type == null || value == null) {
            throw new IllegalStateException("property has no structure");
        }
        return new Connection(Connection.Type.valueOf(type), value, discoverSchema);
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
        final String name = delegate.getMetadata().get(ACTION_UPDATABLE_VALUE);
        final String parameters = delegate.getMetadata().get(ACTION_UPDATABLE_PARAMETERS);
        final String after = delegate.getMetadata().get(ACTION_UPDATABLE_AFTER);
        if (name != null) {
            return Optional.of(new Updatable(
                    name, parameters != null && !parameters.trim().isEmpty() ?
                        asList(parameters.split(VALUE_SEPARATOR)): emptyList(),
                    after != null ? after : ""));
        } else {
            return Optional.empty();
        }
    }

    public Parameter getParameter() {
        return ofNullable(delegate.getMetadata().get(PARAMETER_INDEX))
                .map(s -> new Parameter(Integer.parseInt(s)))
                .orElse(new Parameter());
    }

    /**
     * Checks whether widget type is {@link EParameterFieldType#PASSWORD}
     *
     * @return check result
     */
    public boolean isCredential() {
        return getMetadata().containsKey(UI_CREDENTIAL);
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
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
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
        private final String actionName;
        private final Collection<String> parameters;
        private final String previousProperty;

        private Updatable(final String actionName, final Collection<String> parameters, final String previousProperty) {
            this.actionName = actionName;
            this.parameters = parameters;
            this.previousProperty = previousProperty;
        }

        public Collection<String> getParameters() {
            return parameters;
        }

        public String getPreviousProperty() {
            return previousProperty;
        }

        public String getActionName() {
            return actionName;
        }
    }

    public static class Connection {

        public static final String DEFAULT = "__default__";

        private final Type type;

        private final String value;

        private final String discoverSchema;

        public enum Type {
            IN,
            OUT;
        }

        public Connection(final Type type, final String value, final String discoverSchema) {
            this.type = type;
            this.value = value;
            this.discoverSchema = discoverSchema;
        }

        public Type getType() {
            return this.type;
        }

        public String getValue() {
            return this.value;
        }

        public String getDiscoverSchema() {
            return this.discoverSchema;
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
