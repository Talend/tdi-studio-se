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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.core.model.process.EComponentCategory;
import org.talend.core.model.process.EConnectionType;
import org.talend.core.model.process.IElement;
import org.talend.core.model.process.IElementParameter;
import org.talend.designer.core.model.FakeElement;
import org.talend.designer.core.model.components.ElementParameter;
import org.talend.designer.core.model.process.DataNode;
import org.talend.designer.core.ui.editor.nodes.Node;
import org.talend.sdk.component.server.front.model.ActionReference;
import org.talend.sdk.component.server.front.model.ComponentDetail;
import org.talend.sdk.component.server.front.model.ConfigTypeNode;
import org.talend.sdk.component.server.front.model.PropertyValidation;
import org.talend.sdk.component.studio.Lookups;
import org.talend.sdk.component.studio.model.action.Action;
import org.talend.sdk.component.studio.model.action.SuggestionsAction;
import org.talend.sdk.component.studio.model.action.update.UpdateAction;
import org.talend.sdk.component.studio.model.action.update.UpdateResolver;
import org.talend.sdk.component.studio.model.parameter.condition.ConditionGroup;
import org.talend.sdk.component.studio.model.parameter.listener.ActiveIfListener;
import org.talend.sdk.component.studio.model.parameter.listener.ValidationListener;
import org.talend.sdk.component.studio.model.parameter.listener.ValidatorFactory;
import org.talend.sdk.component.studio.model.parameter.resolver.HealthCheckResolver;
import org.talend.sdk.component.studio.model.parameter.resolver.ParameterResolver;
import org.talend.sdk.component.studio.model.parameter.resolver.SuggestionsResolver;
import org.talend.sdk.component.studio.model.parameter.resolver.ValidationResolver;

import static java.util.Collections.emptyMap;
import static java.util.Collections.unmodifiableList;
import static java.util.Optional.ofNullable;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

/**
 * Creates properties from leafs
 */
public class SettingVisitor implements PropertyVisitor {

    private final static Logger LOGGER = LoggerFactory.getLogger(SettingVisitor.class.getName());

    private ConfigTypeNode rootConfigNode;

    /**
     * Stores created component parameters.
     * Key is parameter name (which is also its path)
     */
    private final Map<String, IElementParameter> settings = new LinkedHashMap<>();

    /**
     * Element(Node) for which parameters are created. It is required to set {@link TaCoKitElementParameter} constructor
     */
    private final IElement element;

    /**
     * Defines {@link EComponentCategory} to be set in created {@link TaCoKitElementParameter}
     * It may be {@link EComponentCategory#BASIC} or {@link EComponentCategory#ADVANCED}
     * for Basic and Advanced view correspondingly
     */
    private EComponentCategory category;

    /**
     * Defines a Form name, for which properties are built. E.g. "Main" or "Advanced"
     */
    private String form;

    private String family;

    /**
     * {@link ElementParameter} which defines whether UI should be redrawn
     */
    private final ElementParameter redrawParameter;

    private final Collection<ActionReference> actions;

    /**
     * A cache of ConditionGroups, which is used to collect them and create ActiveIfListeners
     * after all ElementParameters are created.
     * A key is a path of controlled ElementParameter (i.e. ElementParameter which shown/hidden according ActiveIf logic).
     * Value is a List of ConditionGroups.
     * Each ConditionGroup corresponds to one ActiveIfs annotation.
     * ElementProperty may have several ConditionGroups because its visibility is controlled not only by its own
     * ActiveIfs annotation, but also by its parents' ActiveIfs annotations
     */
    private final Map<String, List<ConditionGroup>> activations =
            new LinkedHashMap<>();

    private final List<ParameterResolver> parameterResolvers = new ArrayList<>();

    public SettingVisitor(final IElement iNode,
            final ElementParameter redrawParameter, final ConfigTypeNode config) {
        this(iNode, redrawParameter, config.getActions());
        this.rootConfigNode = config;
    }

    public SettingVisitor(final IElement iNode,
            final ElementParameter redrawParameter, final ComponentDetail detail) {
        this(iNode, redrawParameter, detail.getActions());
    }

    public SettingVisitor(final IElement iNode,
            final ElementParameter redrawParameter, final Collection<ActionReference> actions) {
        this.element = iNode;
        this.redrawParameter = redrawParameter;
        this.actions = ofNullable(actions).orElseGet(Collections::emptyList);
        this.actions.stream().findFirst().ifPresent(a -> this.family = a.getFamily());
    }

    public SettingVisitor withCategory(final EComponentCategory category) {
        this.category = category;
        this.form = category == EComponentCategory.ADVANCED ? Metadatas.ADVANCED_FORM : Metadatas.MAIN_FORM;
        return this;
    }

    private void buildActivationCondition(final PropertyNode node, final PropertyNode origin) {
        if (node == null) {
            return;
        }

        final ConditionGroup group = node.getProperty().getConditions();
        if (!group.getConditions().isEmpty()) {
            if (rootConfigNode != null) { // wizard context. filter condition to keep only valid ones
                rootConfigNode.getProperties().stream()
                        .filter(p -> p.getPath().equals(p.getName()))
                        .findFirst()
                        .map(root -> group.getConditions().stream().filter(c -> c.getTargetPath().startsWith(root.getPath())))
                        .map(c -> c.collect(toList()))
                        .filter(conditions -> !conditions.isEmpty())
                        .ifPresent(validConditions -> activations.computeIfAbsent(origin.getProperty().getPath(), key -> new ArrayList<>())
                                .add(new ConditionGroup(validConditions, group.getAggregator())));
            } else {
                activations.computeIfAbsent(origin.getProperty().getPath(), key -> new ArrayList<>()).add(group);
            }
        }

        buildActivationCondition(node.getParent(), origin);

    }

    /**
     * Registers created Listeners in {@link TaCoKitElementParameter} and returns list of created parameters.
     * Also setup initial visibility according initial value of target parameters
     *
     * @return created parameters
     */
    public List<IElementParameter> getSettings() {
        activations.forEach((path, conditionGroups) -> {
            final TaCoKitElementParameter param = (TaCoKitElementParameter) settings.get(path);
            if (param == null) {
                throw new RuntimeException("ElementParameter not found. Path: " + path);
            }
            param.setRedrawParameter(redrawParameter);

            final Map<String, TaCoKitElementParameter> targetParams = conditionGroups.stream()
                    .flatMap(it -> it.getConditions().stream())
                    .map(PropertyDefinitionDecorator.Condition::getTargetPath)
                    .peek((String key) -> {
                        if (!this.settings.containsKey(key)) {
                            LOGGER.error("Path " + path + " not found in settings for form " + this.form);
                        }
                    })
                    .map(this.settings::get)
                    .filter(Objects::nonNull).filter(TaCoKitElementParameter.class::isInstance)
                    .map(TaCoKitElementParameter.class::cast)
                    .collect(toMap(ElementParameter::getName, identity()));

            final ActiveIfListener activationListener = new ActiveIfListener(conditionGroups, param, targetParams);

            targetParams.forEach((name, p) -> {
                p.registerListener("value", activationListener);
                //Sends initial event to listener to set initial visibility
                activationListener.propertyChange(
                        new PropertyChangeEvent(p, "value", p.getValue(), p.getValue()));
            });
        });

        parameterResolvers.forEach(resolver -> resolver.resolveParameters(Collections.unmodifiableMap(settings)));
        return unmodifiableList(new ArrayList<>(settings.values()));
    }

    /**
     * Creates ElementParameters only from leafs
     */
    @Override
    public void visit(final PropertyNode node) {
        // skip 'configuration.dataSet.csvConfiguration.csvSchema' field in 'azure-dls-gen2' component
        if (isNodeAzureDlsGen2CsvSchema(node)) {
            return;
        }

        if (node.isLeaf() && !PropertyTypes.OBJECT.equalsIgnoreCase(node.getProperty().getType())) {
            switch (node.getFieldType()) {
            case CHECK:
                final CheckElementParameter check = visitCheck(node);
                settings.put(check.getName(), check);
                break;
            case CLOSED_LIST:
                final TaCoKitElementParameter closedList = visitClosedList(node);
                settings.put(closedList.getName(), closedList);
                break;
            case TABLE:
                final TaCoKitElementParameter table = visitTable((ListPropertyNode) node);
                settings.put(table.getName(), table);
                break;
            case TACOKIT_SUGGESTABLE_TABLE:
                final TaCoKitElementParameter suggestableTable = visitSuggestableTable((ListPropertyNode) node);
                settings.put(suggestableTable.getName(), suggestableTable);
                break;
            case SCHEMA_TYPE:
                final TaCoKitElementParameter outSchema = visitOutSchema(node);
                settings.put(outSchema.getName(), outSchema);
                break;
            case TACOKIT_INPUT_SCHEMA:
                final TaCoKitElementParameter inSchema = visitInSchema(node);
                settings.put(inSchema.getName(), inSchema);
                break;
            case TACOKIT_VALUE_SELECTION:
                final TaCoKitElementParameter textAreaSelection = visitValueSelection(node);
                settings.put(textAreaSelection.getName(), textAreaSelection);
                break;
            case PREV_COLUMN_LIST:
                final TaCoKitElementParameter prevColumnList = visitPrevColumnList(node);
                settings.put(prevColumnList.getName(), prevColumnList);
                break;
            case TACOKIT_TEXT_AREA_SELECTION:
                final TaCoKitElementParameter valueSelection = visitTextAreaSelection(node);
                settings.put(valueSelection.getName(), valueSelection);
                break;
            default:
                final IElementParameter text;
                if (node.getProperty().getPlaceholder() == null) {
                    text = new TaCoKitElementParameter(element);
                } else {
                    final TextElementParameter advancedText = new TextElementParameter(element);
                    advancedText.setMessage(node.getProperty().getPlaceholder());
                    text = advancedText;
                }
                commonSetup(text, node);
                settings.put(text.getName(), text);

                break;
            }
        } else {
            if (Metadatas.MAIN_FORM.equalsIgnoreCase(form)) {
                buildHealthCheck(node);
            }
            buildUpdate(node);
        }
    }

    /**
     * Checks if propery is an Azure DLS Gen2 csvSchema field
     * This is a temporary workaround introduced by TDI-43010 and TDI-43851
     *
     * @param node current PropertyNode
     *
     * @return true if propery is an Azure DLS Gen2 csvSchema field
     */
    private boolean isNodeAzureDlsGen2CsvSchema(final PropertyNode node) {
        return element != null && (//
                // components
                (element instanceof Node && ((Node) element).getComponent() != null &&
                  (((Node) element).getComponent().getName().equals("AzureAdlsGen2Input") ||
                   ((Node) element).getComponent().getName().equals("AzureAdlsGen2Output")) &&
                  node.getProperty().getPath().equals("configuration.dataSet.csvConfiguration.csvSchema"))
                ||
                // metadata editor
                (element instanceof DataNode && ((DataNode) element).getComponent() != null &&
                  ((DataNode) element).getComponent().getName().equals("") &&
                  node.getProperty().getPath().equals("configuration.csvConfiguration.csvSchema"))
        );
    }

    /**
     * Checks whether HealthCheck button should be added
     *
     * @param node current PropertyNode
     * @return true if HealthCheck button should be added
     */
    private boolean hasHealthCheck(final PropertyNode node) {
        return node.getProperty().isCheckable() && !node.getChildren(form).isEmpty();
    }

    /**
     * Builds HealthCheck button
     *
     * @param node current PropertyNode
     */
    protected void buildHealthCheck(final PropertyNode node) {
        if (hasHealthCheck(node)) {
            final ActionReference action = actions
                    .stream()
                    .filter(a -> Action.Type.HEALTHCHECK.toString().equals(a.getType()))
                    .filter(a -> a.getName().equals(node.getProperty().getHealthCheckName()))
                    .findFirst()
                    .get();
            final Layout checkableLayout = node.getLayout(form);
            final Optional<Layout> buttonLayout =
                    checkableLayout.getChildLayout(checkableLayout.getPath() + PropertyNode.CONNECTION_BUTTON);
            if (buttonLayout.isPresent()) {
                new HealthCheckResolver(element, family, node, action, category, buttonLayout.get().getPosition())
                        .resolveParameters(this.form, this.settings);
            } else {
                LOGGER.debug("Button layout {} not found for form {}", checkableLayout.getPath() + PropertyNode.CONNECTION_BUTTON, form);
            }
        }
    }

    /**
     * Builds Update button, which triggers call to Update component action
     *
     * @param node current PropertyNode
     */
    private void buildUpdate(final PropertyNode node) {
        node.getProperty().getUpdatable().ifPresent(updatable -> {
            final Layout formLayout = node.getLayout(form);
            final Optional<Layout> buttonLayout = formLayout.getChildLayout(formLayout.getPath() + PropertyNode.UPDATE_BUTTON);
            if (buttonLayout.isPresent()) {
                final int buttonPosition = buttonLayout.get().getPosition();
                final UpdateAction action = new UpdateAction(updatable.getActionName(), family);
                final UpdateResolver resolver = new UpdateResolver(element, category, buttonPosition, action, node,
                        actions, redrawParameter, settings, () -> hasVisibleChild(formLayout.getPath()));
                resolver.getButton().setForm(this.form);
                parameterResolvers.add(resolver);
            } else {
                LOGGER.debug("Button layout {} not found for form {}", formLayout.getPath() + PropertyNode.UPDATE_BUTTON, form);
            }
        });
    }

    private boolean hasVisibleChild(final String root) { // TODO: to enhance to support object visibility and not fake it!
        return ofNullable(settings.get(root))
                .map(this::isVisible)
                .orElseGet(() -> settings.entrySet().stream()
                    .filter(it -> it.getKey().startsWith(root) && !it.getKey().equals(root) && !ButtonParameter.class.isInstance(it.getValue()))
                    .map(Map.Entry::getValue)
                    .anyMatch(this::isVisible));
    }

    private boolean isVisible(final IElementParameter parameter) {
        try {
            return parameter.isShow(Collections.emptyList());
        } catch (final Exception e) {
            // unlikely but call context is unsafe and we have internal params with showIf set, see buildUpdate
            return false;
        }
    }

    IElement getNode() {
        return this.element;
    }

    /**
     * Creates {@link TaCoKitElementParameter} for Check field type
     * Converts default value from String to Boolean and sets it
     */
    private CheckElementParameter visitCheck(final PropertyNode node) {
        final CheckElementParameter parameter = new CheckElementParameter(element);
        commonSetup(parameter, node);
        return parameter;
    }

    /**
     * Creates {@link TaCoKitElementParameter} for Closed List field type
     * Sets Closed List possible values and sets 1st element as default
     */
    private TaCoKitElementParameter visitClosedList(final PropertyNode node) {
        final TaCoKitElementParameter parameter = new TaCoKitElementParameter(element);
        commonSetup(parameter, node);
        final PropertyValidation validation = node.getProperty().getValidation();

        final boolean isEnum = node.getProperty().getType().equalsIgnoreCase("enum");
        if (isEnum && (validation == null || validation.getEnumValues() == null)) {
            throw new IllegalArgumentException("No values for enum " + node.getProperty().getPath());
        }

        if (validation == null || validation.getEnumValues() == null || validation.getEnumValues().isEmpty()) {
            final ActionReference dynamicValuesAction =
                    ofNullable(node.getProperty().getMetadata().get("action::dynamic_values"))
                            .flatMap(ref -> actions
                                    .stream()
                                    .filter(a -> "dynamic_values".equals(a.getType()) && ref.equals(a.getName()))
                                    .findFirst())

                            .orElseThrow(() -> new IllegalArgumentException(
                                    "No values for list " + node.getProperty().getPath()));

            // todo: should we make this retryable?
            final Map<String, Object> values =
                    Lookups.client().v1().action().execute(Map.class, dynamicValuesAction.getFamily(),
                            dynamicValuesAction.getType(), dynamicValuesAction.getName(), emptyMap());

            final Object rawItems = values.get("items");
            if (rawItems == null) {
                throw new IllegalStateException("No proposals for " + node.getProperty().getPath());
            }
            final Collection<Map<String, String>> items = Collection.class.cast(rawItems);
            final String[] ids = items.stream().map(m -> m.get("id")).toArray(String[]::new);
            final String[] labels =
                    items.stream().map(m -> m.getOrDefault("label", m.get("id"))).toArray(String[]::new);
            parameter.setListItemsValue(ids);
            parameter.setListItemsDisplayName(labels);
            parameter.setListItemsDisplayCodeName(labels);
        } else {
            final List<String> possibleValues = new ArrayList<>(validation.getEnumValues());
            final int valuesCount = possibleValues.size();

            final String[] valuesArray = possibleValues.toArray(new String[valuesCount]);
            parameter.setListItemsValue(valuesArray);
            parameter.setListItemsDisplayName(
                    new TreeMap<String, String>(Comparator.comparingInt(possibleValues::indexOf)) {{
                        putAll(node.getProperty().getProposalDisplayNames());
                    }}.values().toArray(new String[0]));
            parameter.setListItemsDisplayCodeName(valuesArray);
        }

        String defaultValue = node.getProperty().getDefaultValue();
        if (defaultValue == null && node.getProperty().getMetadata() != null) {
            defaultValue = node.getProperty().getMetadata().get("ui::defaultvalue::value");
        }
        parameter.setDefaultClosedListValue(defaultValue);
        parameter.setDefaultValue(defaultValue);
        parameter.setValue(defaultValue);
        return parameter;
    }

    private TaCoKitElementParameter visitPrevColumnList(final PropertyNode node) {
        final TaCoKitElementParameter parameter = new TaCoKitElementParameter(element);
        commonSetup(parameter, node);
        return parameter;
    }

    /**
     * Creates {@link TaCoKitElementParameter} for Table field type
     * Sets special fields specific for Table parameter
     * Based on schema field controls whether table toolbox (buttons under table) is shown.
     * If parameter is based on schema, then toolbox is not shown
     */
    private TaCoKitElementParameter visitTable(final ListPropertyNode tableNode) {
        final TaCoKitElementParameter parameter = new TableElementParameter(element, createTableParameters(tableNode));
        commonSetup(parameter, tableNode);
        return parameter;
    }

    /**
     * Creates {@link TaCoKitElementParameter} for TACOKIT_SUGGESTABLE_TABLE field type
     * Sets special fields specific for Table parameter
     * Based on schema field controls whether table toolbox (buttons under table) is shown.
     * If parameter is based on schema, then toolbox is not shown
     */
    private TaCoKitElementParameter visitSuggestableTable(final ListPropertyNode tableNode) {
        final TaCoKitElementParameter parameter = new SuggestableTableParameter(element, createTableParameters(tableNode));
        commonSetup(parameter, tableNode);
        return parameter;
    }

    private TaCoKitElementParameter visitOutSchema(final PropertyNode node) {
        final String connectionName = getConnectionName(node);
        final String discoverSchemaAction = node.getProperty().getConnection().getDiscoverSchema();
        return new OutputSchemaParameter(getNode(), node.getProperty().getPath(), connectionName, discoverSchemaAction,
                true);
    }

    private TaCoKitElementParameter visitInSchema(final PropertyNode node) {
        return new InputSchemaParameter(getNode(), node.getProperty().getPath(), getConnectionName(node));
    }

    /**
     * Computes Node connection name. This name is used to get connection by name and then get schema from
     * connection.
     *
     * @param node Schema PropertyNode
     */
    private String getConnectionName(final PropertyNode node) {
        final String connectorName = node.getProperty().getConnection().getValue();
        if (PropertyDefinitionDecorator.Connection.DEFAULT.equals(connectorName)) {
            return EConnectionType.FLOW_MAIN.getName();
        } else {
            return connectorName;
        }
    }

    private ValueSelectionParameter visitValueSelection(final PropertyNode node) {
        final SuggestionsAction action = createSuggestionsAction(node);
        final ValueSelectionParameter parameter = new ValueSelectionParameter(element, action);
        commonSetup(parameter, node);
        return parameter;
    }

    private SuggestionsAction createSuggestionsAction(final PropertyNode node) {
        final SuggestionsAction action = new SuggestionsAction(node.getProperty().getSuggestions().getName(), family);
        final SuggestionsResolver resolver = new SuggestionsResolver(action, node, actions);
        parameterResolvers.add(resolver);
        return action;
    }

    private TextAreaSelectionParameter visitTextAreaSelection(final PropertyNode node) {
        final SuggestionsAction action = createSuggestionsAction(node);
        final TextAreaSelectionParameter parameter = new TextAreaSelectionParameter(element, action);
        commonSetup(parameter, node);
        return parameter;
    }

    protected TaCoKitElementParameter createSchemaParameter(final String connectionName, final String schemaName,
            final String discoverSchemaAction,
            final boolean show) {
        return new OutputSchemaParameter(getNode(), schemaName, connectionName, discoverSchemaAction, show);
    }

    /**
     * Setup common for all {@link TaCoKitElementParameter} fields
     *
     * @param parameter parameter to setup
     * @param node      property tree node
     */
    private void commonSetup(final IElementParameter parameter, final PropertyNode node) {
        parameter.setCategory(category);
        parameter.setDisplayName(node.getProperty().getDisplayName());
        parameter.setFieldType(node.getFieldType());
        parameter.setName(node.getProperty().getPath());
        // parameter.setRepositoryValue(node.getProperty().getPath());
        parameter.setNumRow(node.getLayout(form).getPosition());
        parameter.setShow(true);
        String defaultValue = node.getProperty().getDefaultValue();
        if (defaultValue == null && node.getProperty().getMetadata() != null) {
            defaultValue = node.getProperty().getMetadata().get("ui::defaultvalue::value");
        }

        parameter.setRequired(node.getProperty().isRequired());
        if (TaCoKitElementParameter.class.isInstance(parameter)) {
            final TaCoKitElementParameter taCoKitElementParameter = TaCoKitElementParameter.class.cast(parameter);
            taCoKitElementParameter.setPropertyNode(node);
            taCoKitElementParameter.updateValueOnly(defaultValue);
            taCoKitElementParameter.setForm(this.form);
            if (node.getProperty().hasConstraint() || node.getProperty().hasValidation()) {
                taCoKitElementParameter.setRegistValidatorCallback(() -> {
                        createValidationLabel(node, taCoKitElementParameter);
                        return null;
                });
            }
            buildActivationCondition(node, node);
        } else {
            parameter.setValue(defaultValue);
        }
    }

    /**
     * Creates table parameters (columns) for Table property
     *
     * @param tableNode {@link ListPropertyNode}
     * @return list of table parameters
     */
    private List<IElementParameter> createTableParameters(final ListPropertyNode tableNode) {
        final List<PropertyNode> columns = tableNode.getColumns(form);
        if (columns.size() == 1 && columns.get(0).getProperty().getDisplayName().endsWith("[${index}]")) {
            columns.iterator().next().getProperty().setDisplayName("Value");
        }
        columns.forEach(c -> {
            final Layout layout = new Layout(c.getProperty().getPath());
            layout.setHeight(1);
            layout.setPosition(tableNode.getLayout(form).getPosition());
            c.addLayout(form, layout);
        });
        final SettingVisitor creator =
                new SettingVisitor(new FakeElement("table"), redrawParameter, actions).withCategory(category);
        columns.forEach(creator::visit);
        return unmodifiableList(new ArrayList<>(creator.settings.values()));
    }

    /**
     * Creates LABEL ElementParameter which shows validation message in case validation fail.
     * It is shown on the next row, but may be shown in the next
     */
    private void createValidationLabel(final PropertyNode node, final TaCoKitElementParameter target) {
        final ValidationLabel label = new ValidationLabel(target);

        processConstraints(node, target, label);
        processValidations(node, target, label);
    }

    private void processConstraints(final PropertyNode node, final TaCoKitElementParameter target,
            final ValidationLabel label) {
        if (node.getProperty().hasConstraint()) {
            final PropertyValidation validation = node.getProperty().getValidation();
            final List<PropertyChangeListener> validators = new ValidatorFactory().createValidators(validation, label);
            if (!validators.isEmpty()) {
                target.setRedrawParameter(redrawParameter);
                validators.forEach(v -> target.registerListener("value", v));
            }
        }
    }

    private void processValidations(final PropertyNode node, final TaCoKitElementParameter target,
            final ValidationLabel label) {
        if (node.getProperty().hasValidation()) {
            final ValidationListener listener =
                    new ValidationListener(label, family, node.getProperty().getValidationName());
            target.registerListener("value", listener);
            final ValidationResolver resolver = new ValidationResolver(node, actions, listener, redrawParameter);
            parameterResolvers.add(resolver);
        }
    }

}
