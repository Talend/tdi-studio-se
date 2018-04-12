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

import static java.util.Collections.emptyMap;
import static java.util.Collections.unmodifiableList;
import static java.util.Optional.ofNullable;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;
import static org.talend.sdk.component.studio.model.action.Action.HEALTH_CHECK;
import static org.talend.sdk.component.studio.model.parameter.TaCoKitElementParameter.guessButtonName;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

import org.talend.core.model.process.EComponentCategory;
import org.talend.core.model.process.EConnectionType;
import org.talend.core.model.process.EParameterFieldType;
import org.talend.core.model.process.IElement;
import org.talend.core.model.process.IElementParameter;
import org.talend.core.model.process.INodeConnector;
import org.talend.designer.core.model.FakeElement;
import org.talend.designer.core.model.components.EParameterName;
import org.talend.designer.core.model.components.ElementParameter;
import org.talend.designer.core.ui.editor.nodes.Node;
import org.talend.sdk.component.server.front.model.ActionReference;
import org.talend.sdk.component.server.front.model.ComponentDetail;
import org.talend.sdk.component.server.front.model.ConfigTypeNode;
import org.talend.sdk.component.server.front.model.PropertyValidation;
import org.talend.sdk.component.studio.Lookups;
import org.talend.sdk.component.studio.i18n.Messages;
import org.talend.sdk.component.studio.model.parameter.listener.ActiveIfListener;
import org.talend.sdk.component.studio.model.parameter.listener.ValidationListener;
import org.talend.sdk.component.studio.model.parameter.listener.ValidatorFactory;
import org.talend.sdk.component.studio.util.TaCoKitConst;
import org.talend.sdk.component.studio.util.TaCoKitUtil;

/**
 * Creates properties from leafs
 */
public class SettingVisitor implements PropertyVisitor {

    /**
     * Specifies row number, on which schema properties (schema widget and guess schema button) should be displayed
     * On the 1st row Repository switch widget is located
     */
    private static final int SCHEMA_ROW_NUMBER = 2;

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

    private final Map<String, Map<Integer, List<PropertyDefinitionDecorator.Condition>>> activations =
            new LinkedHashMap<>();

    private final List<ValidationResolver> actionResolvers = new ArrayList<>();

    public SettingVisitor(final IElement iNode,
            final ElementParameter redrawParameter, final ConfigTypeNode config) {
        this(iNode, redrawParameter, config.getActions());
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

    /**
     * Registers created Listeners in {@link TaCoKitElementParameter} and returns list of created parameters.
     * Also setup initial visibility according initial value of target parameters
     *
     * @return created parameters
     */
    public List<IElementParameter> getSettings() {
        activations.forEach((path, conditions) -> {
            settings.keySet().stream()
                    .filter(key -> key.equals(path))
                    .filter(p -> TaCoKitElementParameter.class.isInstance(settings.get(p)))
                    .map(setting -> TaCoKitElementParameter.class.cast(settings.get(setting)))
                    .forEach(param -> {
                        param.setRedrawParameter(redrawParameter);
                        final Map<String, TaCoKitElementParameter> targetParams =
                                conditions.values().stream().flatMap(Collection::stream)
                                        .map(c -> TaCoKitElementParameter.class.cast(settings.get(c.getTargetPath())))
                                        .collect(toMap(ElementParameter::getName, identity()));

                        final ActiveIfListener activationListener =
                                new ActiveIfListener(conditions, param, targetParams);

                        targetParams.forEach((name, p) -> {
                            p.setRedrawParameter(redrawParameter);
                            p.registerListener(name, activationListener);
                            //Sends initial event to listener to set initial visibility
                            activationListener.propertyChange(
                                    new PropertyChangeEvent(p, name, p.getValue(), p.getValue()));
                        });
                    });
        });

        actionResolvers.forEach(resolver -> resolver.resolveParameters(Collections.unmodifiableMap(settings)));
        return unmodifiableList(new ArrayList<>(settings.values()));
    }

    /**
     * Creates ElementParameters only from leafs
     */
    @Override
    public void visit(final PropertyNode node) {
        if (node.isLeaf()) {
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
            case SCHEMA_TYPE:
                final TaCoKitElementParameter schema = visitSchema(node);
                settings.put(schema.getName(), schema);
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
        } else if (node.getProperty().isCheckable() && !node.getChildren(form).isEmpty()) {
            final ActionReference action = actions
                    .stream()
                    .filter(a -> HEALTH_CHECK.equals(a.getType()))
                    .filter(a -> a.getName().equals(node.getProperty().getHealthCheckName()))
                    .findFirst()
                    .get();
            final Layout checkableLayout = node.getLayout(form);
            final Layout buttonLayout =
                    checkableLayout.getChildLayout(checkableLayout.getPath() + PropertyNode.CONNECTION_BUTTON);
            new HealthCheckResolver(element, family, node, action, category, buttonLayout.getPosition())
                    .resolveParameters(settings);
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
        final int valuesCount;
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
            valuesCount = items.size();
            final String[] ids = items.stream().map(m -> m.get("id")).toArray(String[]::new);
            final String[] labels =
                    items.stream().map(m -> m.getOrDefault("label", m.get("id"))).toArray(String[]::new);
            parameter.setListItemsValue(ids);
            parameter.setListItemsDisplayName(labels);
            parameter.setListItemsDisplayCodeName(labels);
        } else {
            final List<String> possibleValues = new ArrayList<>(validation.getEnumValues());
            valuesCount = possibleValues.size();

            final String[] valuesArray = possibleValues.toArray(new String[valuesCount]);
            parameter.setListItemsValue(valuesArray);
            parameter.setListItemsDisplayName(
                    new TreeMap<String, String>(Comparator.comparingInt(possibleValues::indexOf)) {{
                        putAll(node.getProperty().getProposalDisplayNames());
                    }}.values().toArray(new String[0]));
            parameter.setListItemsDisplayCodeName(valuesArray);
        }

        parameter.setListItemsReadOnlyIf(new String[valuesCount]);
        parameter.setListItemsNotReadOnlyIf(new String[valuesCount]);
        parameter.setListItemsShowIf(new String[valuesCount]);
        parameter.setListItemsNotShowIf(new String[valuesCount]);

        String defaultValue = node.getProperty().getDefaultValue();
        if (defaultValue == null && node.getProperty().getMetadata() != null) {
            defaultValue = node.getProperty().getMetadata().get("ui::defaultvalue::value");
        }
        parameter.setDefaultClosedListValue(defaultValue);
        parameter.setDefaultValue(defaultValue);
        return parameter;
    }

    /**
     * Creates {@link TaCoKitElementParameter} for Table field type
     * Sets special fields specific for Table parameter
     * Based on schema field controls whether table toolbox (buttons under table) is shown
     */
    private TaCoKitElementParameter visitTable(final ListPropertyNode tableNode) {
        final TaCoKitElementParameter parameter = createTableParameter(tableNode);

        final List<IElementParameter> tableParameters = createTableParameters(tableNode);
        final List<String> codeNames = new ArrayList<>(tableParameters.size());
        final List<String> displayNames = new ArrayList<>(tableParameters.size());
        for (final IElementParameter param : tableParameters) {
            codeNames.add(param.getName());
            displayNames.add(param.getDisplayName());
        }
        parameter.setListItemsDisplayName(displayNames.toArray(new String[0]));
        parameter.setListItemsDisplayCodeName(codeNames.toArray(new String[0]));
        parameter.setListItemsValue(tableParameters.toArray(new ElementParameter[0]));
        parameter.setListItemsShowIf(new String[tableParameters.size()]);
        parameter.setListItemsNotShowIf(new String[tableParameters.size()]);

        parameter.updateValueOnly(new ArrayList<Map<String, Object>>());
        // TODO change to real value
        parameter.setBasedOnSchema(false);
        return parameter;
    }

    private TaCoKitElementParameter visitSchema(final PropertyNode node) {
        final String connectorName = node.getProperty().getConnection().getValue();
        final String connectionName =
                connectorName.equals("__default__") ? EConnectionType.FLOW_MAIN.getName() : connectorName;
        final String schemaName = node.getProperty().getSchemaName();
        final String discoverSchemaAction = ofNullable(node.getProperty().getMetadata()).orElse(emptyMap())
                .entrySet().stream().filter(e -> "ui::structure::discoverSchema".equals(e.getKey()))
                .map(Map.Entry::getValue).filter(Objects::nonNull).findFirst().orElse(null);

        return createSchemaParameter(connectionName, schemaName, discoverSchemaAction, true);
    }

    // TODO i18n it
    private String schemaDisplayName(final String connectionName, final String schemaName) {
        final String connectorName = connectionName.equalsIgnoreCase(EConnectionType.FLOW_MAIN.getName())
                ? EConnectionType.FLOW_MAIN.getDefaultLinkName()
                : connectionName;
        if ("REJECT".equalsIgnoreCase(connectionName)) {
            return "Reject Schema";
        }
        if (schemaName.contains("$$")) {
            final String type = schemaName.substring(0, schemaName.indexOf("$$"));
            if ("OUT".equalsIgnoreCase(type)) {
                return "Output Schema" + "(" + connectorName + ")";
            }
            if ("IN".equalsIgnoreCase(type)) {
                return "Input Schema" + "(" + connectorName + ")";
            }
        }
        return "Schema" + "(" + connectorName + ")";
    }

    protected TaCoKitElementParameter createSchemaParameter(final String connectionName, final String schemaName,
            final String discoverSchemaAction,
            final boolean show) {
        String baseSchema = EConnectionType.FLOW_MAIN.getName();
        // Maybe need to find some other condition. this way we will show schema widget for main flow only.
        final TaCoKitElementParameter schema = new TaCoKitElementParameter(getNode());
        schema.setName(schemaName);
        schema.setDisplayName("!!!SCHEMA.NAME!!!");
        schema.setCategory(EComponentCategory.BASIC);
        schema.setFieldType(EParameterFieldType.SCHEMA_TYPE);
        schema.setNumRow(SCHEMA_ROW_NUMBER);
        schema.setShow(show);
        schema.setReadOnly(false);
        schema.setRequired(true);
        schema.setContext(connectionName);

        // add child parameters
        // defines whether schema is built-in or repository
        final ElementParameter childParameter1 = new ElementParameter(getNode());
        childParameter1.setCategory(EComponentCategory.BASIC);
        childParameter1.setContext(baseSchema);
        childParameter1.setDisplayName(schemaDisplayName(connectionName, schemaName));
        childParameter1.setFieldType(EParameterFieldType.TECHNICAL);
        childParameter1.setListItemsDisplayCodeName(new String[] { "BUILT_IN", "REPOSITORY" });
        childParameter1.setListItemsDisplayName(new String[] { "Built-In", "Repository" });
        childParameter1.setListItemsValue(new String[] { "BUILT_IN", "REPOSITORY" });
        childParameter1.setName(EParameterName.SCHEMA_TYPE.getName());
        childParameter1.setNumRow(1);
        childParameter1.setParentParameter(schema);
        childParameter1.setShow(show);
        childParameter1.setShowIf("SCHEMA =='REPOSITORY'");
        childParameter1.setValue("BUILT_IN");
        schema.getChildParameters().put(EParameterName.SCHEMA_TYPE.getName(), childParameter1);

        final ElementParameter childParameter2 = new ElementParameter(getNode());
        childParameter2.setCategory(EComponentCategory.BASIC);
        childParameter2.setContext(baseSchema);
        childParameter2.setDisplayName("Repository");
        childParameter2.setFieldType(EParameterFieldType.TECHNICAL);
        childParameter2.setListItemsDisplayName(new String[0]);
        childParameter2.setListItemsValue(new String[0]);
        childParameter2.setName(EParameterName.REPOSITORY_SCHEMA_TYPE.getName());
        childParameter2.setParentParameter(schema);
        childParameter2.setRequired(true);
        childParameter2.setShow(show);
        childParameter2.setShowIf("SCHEMA =='REPOSITORY'");
        childParameter2.setValue("");
        schema.getChildParameters().put(EParameterName.REPOSITORY_SCHEMA_TYPE.getName(), childParameter2);

        if (canAddGuessSchema(connectionName)) {
            final TaCoKitElementParameter guessSchemaParameter = new TaCoKitElementParameter(getNode());
            guessSchemaParameter.setCategory(EComponentCategory.BASIC);
            guessSchemaParameter.setContext(connectionName);
            guessSchemaParameter.updateValueOnly(discoverSchemaAction);
            guessSchemaParameter.setDisplayName(Messages.getString("guessSchema.button", connectionName));
            guessSchemaParameter.setFieldType(EParameterFieldType.TACOKIT_GUESS_SCHEMA);
            guessSchemaParameter.setListItemsDisplayName(new String[0]);
            guessSchemaParameter.setListItemsValue(new String[0]);
            guessSchemaParameter.setName(guessButtonName(schemaName));
            guessSchemaParameter.setNumRow(SCHEMA_ROW_NUMBER);
            guessSchemaParameter.setParentParameter(schema);
            guessSchemaParameter.setReadOnly(false);
            guessSchemaParameter.setRequired(false);
            guessSchemaParameter.setShow(show);
            guessSchemaParameter.putInfo(TaCoKitConst.ADDITIONAL_PARAM_METADATA_ELEMENT, schema);
        }

        return schema;
    }

    /**
     * Creates {@link TableElementParameter} and sets common state
     *
     * @param node Property tree node
     * @return created {@link TableElementParameter}
     */
    private TableElementParameter createTableParameter(final PropertyNode node) {
        final TableElementParameter parameter = new TableElementParameter(element);
        commonSetup(parameter, node);
        return parameter;
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
        node.getRepositoryKeys().ifPresent(parameter::setRepositoryValue);
        parameter.setNumRow(node.getLayout(form).getPosition());
        parameter.setShow(true);
        String defaultValue = node.getProperty().getDefaultValue();
        if (defaultValue == null && node.getProperty().getMetadata() != null) {
            defaultValue = node.getProperty().getMetadata().get("ui::defaultvalue::value");
        }

        if (TaCoKitElementParameter.class.isInstance(parameter)) {
            TaCoKitElementParameter.class.cast(parameter).updateValueOnly(defaultValue);
        } else {
            parameter.setValue(defaultValue);
        }
        parameter.setRequired(node.getProperty().isRequired());
        if (node.getProperty().hasConstraint() || node.getProperty().hasValidation()) {
            createValidationLabel(node, (TaCoKitElementParameter) parameter);
        }

        buildActivationCondition(node, node, 0);
    }

    private void buildActivationCondition(final PropertyNode node, final PropertyNode origin, final int level) {
        if (node == null) {
            return;
        }

        node.getProperty().getCondition()
                .forEach(c -> {
                    c.setTargetPath(PathResolver.resolve(node, c.getTarget()));
                    activations.computeIfAbsent(origin.getProperty().getPath(), (key) -> new HashMap<>());
                    activations.get(origin.getProperty().getPath()).computeIfAbsent(level, (k) -> new ArrayList<>());
                    activations.get(origin.getProperty().getPath()).get(level).add(c);
                });

        final int l = level + 1;
        buildActivationCondition(node.getParent(), origin, l);
    }

    /**
     * Creates table parameters (columns) for Table property
     *
     * @param tableNode {@link ListPropertyNode}
     * @return list of table parameters
     */
    private List<IElementParameter> createTableParameters(final ListPropertyNode tableNode) {
        final List<PropertyNode> columns = tableNode.getColumns();
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
        final ValidationLabel label = new ValidationLabel(element);
        label.setCategory(category);
        label.setName(node.getProperty().getPath() + PropertyNode.VALIDATION);
        label.setRedrawParameter(redrawParameter);
        // it is shown on the next row by default, but may be changed
        label.setNumRow(node.getLayout(form).getPosition() + 1);
        settings.put(label.getName(), label);

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
                validators.forEach(v -> target.registerListener(target.getName(), v));
            }
        }
    }

    private void processValidations(final PropertyNode node, final TaCoKitElementParameter target,
            final ValidationLabel label) {
        if (node.getProperty().hasValidation()) {
            final ValidationListener listener =
                    new ValidationListener(label, family, node.getProperty().getValidationName());
            final ValidationResolver resolver = new ValidationResolver(node, actions, listener, redrawParameter);
            actionResolvers.add(resolver);
        }
    }

    private boolean canAddGuessSchema(final String connectorName) {
        if (TaCoKitUtil.isBlank(connectorName)) {
            return false;
        }
        boolean canAddGuessSchema = false;
        final IElement node = getNode();
        if (node instanceof Node) {
            boolean hasOutputConnector = false;
            final List<? extends INodeConnector> listConnector = ((Node) node).getListConnector();
            if (listConnector != null) {
                for (final INodeConnector connector : listConnector) {
                    if (connectorName.equals(connector.getName())) {
                        if (0 < connector.getMaxLinkOutput()) {
                            hasOutputConnector = true;
                            // input and output connectors may have same name
                            break;
                        }
                    }
                }
            }
            canAddGuessSchema = hasOutputConnector;
        }
        return canAddGuessSchema;
    }
}
