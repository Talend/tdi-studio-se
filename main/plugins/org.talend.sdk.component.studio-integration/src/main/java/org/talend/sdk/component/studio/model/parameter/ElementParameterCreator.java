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

import static java.util.Collections.emptyList;
import static org.talend.core.model.process.EComponentCategory.ADVANCED;
import static org.talend.core.model.process.EComponentCategory.BASIC;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.talend.core.PluginChecker;
import org.talend.core.model.components.ComponentCategory;
import org.talend.core.model.components.IComponent;
import org.talend.core.model.process.EComponentCategory;
import org.talend.core.model.process.EConnectionType;
import org.talend.core.model.process.EParameterFieldType;
import org.talend.core.model.process.IConnectionCategory;
import org.talend.core.model.process.IElementParameter;
import org.talend.core.model.process.INode;
import org.talend.core.model.process.INodeConnector;
import org.talend.designer.core.DesignerPlugin;
import org.talend.designer.core.model.components.AbstractBasicComponent;
import org.talend.designer.core.model.components.EParameterName;
import org.talend.designer.core.model.components.ElementParameter;
import org.talend.designer.core.ui.preferences.TalendDesignerPrefConstants;
import org.talend.sdk.component.server.front.model.ComponentDetail;
import org.talend.sdk.component.server.front.model.ConfigTypeNode;
import org.talend.sdk.component.server.front.model.SimplePropertyDefinition;
import org.talend.sdk.component.studio.ComponentModel;
import org.talend.sdk.component.studio.Lookups;
import org.talend.sdk.component.studio.VirtualComponentModel;
import org.talend.sdk.component.studio.i18n.Messages;
import org.talend.sdk.component.studio.model.connector.ConnectorCreatorFactory;
import org.talend.sdk.component.studio.util.TaCoKitConst;
import org.talend.sdk.component.studio.util.TaCoKitUtil;
import org.talend.sdk.studio.process.TaCoKitNode;

/**
 * Creates {@link ComponentModel} {@link ElementParameter} list
 */
public class ElementParameterCreator {

    private final INode node;

    private final ComponentModel component;

    private final ComponentDetail detail;

    private final List<IElementParameter> parameters = new ArrayList<>();

    private final String reportPath;

    private final boolean isCatcherAvailable;

    /**
     * Stores Component Property Definitions
     */
    private Collection<PropertyDefinitionDecorator> properties;

    /**
     * This parameter is checked during refresh to decide whether UI should be redrawn
     * If UI should be redrawn, when some Parameter is changed, such Parameter should set this
     * Parameter to {@code true}
     */
    private ElementParameter updateComponentsParameter;

    public ElementParameterCreator(final ComponentModel component, final ComponentDetail detail, final INode node,
            final String reportPath, final boolean isCatcherAvailable) {
        this.component = component;
        this.detail = detail;
        this.node = node;
        this.isCatcherAvailable = isCatcherAvailable;
        this.reportPath = reportPath;
        if (!detail.getProperties().isEmpty()) {
            this.properties = PropertyDefinitionDecorator.wrap(detail.getProperties());
        } else {
            this.properties = emptyList();
        }
    }

    public ElementParameterCreator(final ComponentModel component, final ComponentDetail detail,
            Collection<SimplePropertyDefinition> properties, final INode node, final String reportPath,
            final boolean isCatcherAvailable) {
        this.component = component;
        this.detail = detail;
        this.node = node;
        this.isCatcherAvailable = isCatcherAvailable;
        this.reportPath = reportPath;
        if (properties != null) {
            this.properties = PropertyDefinitionDecorator.wrap(properties);
        } else if (!detail.getProperties().isEmpty()) {
            this.properties = PropertyDefinitionDecorator.wrap(detail.getProperties());
        } else {
            this.properties = emptyList();
        }
    }

    public List<? extends IElementParameter> createParameters() {
        addCommonParameters();
        addSettings();
        return parameters;
    }

    /**
     * Adds Basic and Advanced settings
     */
    private void addSettings() {
        if (!properties.isEmpty()) {
            final PropertyNode root = new PropertyTreeCreator(new WidgetTypeMapper()).createPropertyTree(properties);
            final SettingVisitor4Component settingVisitor = new SettingVisitor4Component(node, updateComponentsParameter, detail);
            if (isShowPropertyParameter()) {
                root.accept(settingVisitor.withCategory(BASIC), Metadatas.MAIN_FORM);
                root.accept(settingVisitor.withCategory(ADVANCED), Metadatas.ADVANCED_FORM);
            }
            parameters.addAll(settingVisitor.getSettings());
            addLayoutParameter(root, Metadatas.MAIN_FORM);
            addLayoutParameter(root, Metadatas.ADVANCED_FORM);
            // create config type version param
            properties.stream().filter(p -> p.getConfigurationType() != null && p.getConfigurationTypeName() != null)
                    .forEach(p -> parameters.add(new VersionParameter(node, p.getPath(), String.valueOf(TaCoKitUtil
                            .getConfigTypeVersion(p, component.getConfigTypeNodes(), component.getId().getFamilyId())))));
        }

        checkSchemaProperties(new SettingVisitor(node, updateComponentsParameter, detail).withCategory(BASIC));
    }

    private void addLayoutParameter(final PropertyNode root, final String form) {
        final Layout layout = root.getLayout(form);
        if (TaCoKitUtil.isSupportUseExistConnection(component) && Metadatas.MAIN_FORM.equals(form)) {
            if (isShowPropertyParameter()) {
                updateParameterForComponentList();
            } else {
                addParameterForClose();
            }
        }
        final EComponentCategory category = Metadatas.MAIN_FORM.equals(form) ? BASIC : ADVANCED;
        final LayoutParameter layoutParameter = new LayoutParameter(node, layout, category);
        parameters.add(layoutParameter);
    }

    /**
     * Check whether all required schema settings are created and create ones we still need depending on connectors we
     * have.
     *
     * @param mainSettingsCreator SettingsCreator for Basic settings
     */
    protected void checkSchemaProperties(final SettingVisitor mainSettingsCreator) {
        // Get all schema parameters created for current component
        final Set<String> schemasPresent = parameters
                .stream()
                .filter(p -> EParameterFieldType.SCHEMA_TYPE.equals(p.getFieldType()))
                .map(IElementParameter::getContext)
                .collect(Collectors.toSet());
        // Get all connectors without schema parameter for them
        final List<? extends INodeConnector> connectorNames = component
                // Use null instead of node here, because connector.getMaxLinkOutput() would try to retrieve
                // element parameters list from non-null node, which will be null.
                .createConnectors(null)
                .stream()
                .filter(c -> c.getDefaultConnectionType().hasConnectionCategory(IConnectionCategory.FLOW)
                        && !schemasPresent.contains(c.getName()))
                .sorted((c1, c2) -> {
                    String c1Name = c1.getName();
                    String c2Name = c2.getName();
                    if (c1Name == null) {
                        c1Name = "";
                    }
                    if (c2Name == null) {
                        c2Name = "";
                    }
                    final int nameCompare = c1Name.compareTo(c2Name);
                    final boolean c1HasOutput = (0 < c1.getMaxLinkOutput());
                    final boolean c2HasOutput = (0 < c2.getMaxLinkOutput());
                    if (c1HasOutput) {
                        if (c2HasOutput) {
                            return nameCompare;
                        } else {
                            return 1;
                        }
                    } else {
                        if (c2HasOutput) {
                            return -1;
                        } else {
                            return nameCompare;
                        }
                    }
                })
                .collect(Collectors.toList());
        // Create schema parameter for each connector without schema parameter
        for (final INodeConnector connectorWithoutSchema : connectorNames) {
            parameters.add(mainSettingsCreator.createSchemaParameter(connectorWithoutSchema.getName(),
                    "SCHEMA_" + connectorWithoutSchema.getName(),
                    "default",
                    showSchema(connectorWithoutSchema)));
        }
    }

    private boolean showSchema(final INodeConnector connector) {
        if (component instanceof VirtualComponentModel) {
            return false;
        }
        return (connector.getDefaultConnectionType() == EConnectionType.FLOW_MAIN
                || connector.getDefaultConnectionType() == EConnectionType.REJECT)
                && (connector.getMaxLinkInput() != 0 || connector.getMaxLinkOutput() != 0);
    }

    /**
     * Creates and adds {@link ElementParameter} common for all components
     */
    private void addCommonParameters() {
        addUniqueNameParameter();
        addComponentNameParameter();
        addTacokitComponentIdParameter();
        addTaCoKitMetadataTypeIdParameter();
        addVersionParameter();
        addFamilyParameter();
        addStartParameter();
        addStartableParameter();
        addSubtreeStartParameter();
        addEndOfFlowParameter();
        addActivateParameter();
        addHelpParameter();
        addUpdateComponentsParameter();
        addIReportPathParameter();
        if (isShowPropertyParameter()) {
            addPropertyParameter();
        }
        addStatCatcherParameter();
        // following parameters are added only in TIS
        addParallelizeParameter();
        addParallelizeNumberParameter();
        addParallelizeKeepEmptyParameter();

        addViewParameters();
        addDocParameters();
    }

    private boolean isShowPropertyParameter() {
        if (this.component instanceof VirtualComponentModel) {
            return ((VirtualComponentModel) component).isShowPropertyParameter();
        }
        return true;
    }

    /**
     * Creates and adds {@link EParameterName#FAMILY} parameter This parameter is used during code generation to know
     * which component runtime to use
     */
    private void addFamilyParameter() {
        final ElementParameter parameter = new ElementParameter(node);
        parameter.setName(EParameterName.FAMILY.getName());
        parameter.setValue(detail.getId().getFamily());
        parameter.setDisplayName(EParameterName.FAMILY.getDisplayName());
        parameter.setFieldType(EParameterFieldType.TEXT);
        parameter.setCategory(EComponentCategory.TECHNICAL);
        parameter.setNumRow(3);
        parameter.setReadOnly(true);
        parameter.setRequired(false);
        parameter.setShow(false);
        parameters.add(parameter);
    }

    /**
     * Creates and adds {@link EParameterName#START} parameter
     * See TUP-4142
     */
    private void addStartParameter() {
        if (isStartable()) {
            final ElementParameter parameter = new ElementParameter(node);
            parameter.setName(EParameterName.START.getName());
            parameter.setValue(false);
            parameter.setDisplayName(EParameterName.START.getDisplayName());
            parameter.setFieldType(EParameterFieldType.CHECK);
            parameter.setCategory(EComponentCategory.TECHNICAL);
            parameter.setNumRow(5);
            parameter.setReadOnly(true);
            parameter.setRequired(false);
            parameter.setShow(false);
            parameters.add(parameter);
        }
    }

    /**
     * Checks whether this component is startable, i.e. whether this component is StandAlone or Input
     *
     * @return true if component is startable
     */
    private boolean isStartable() {
        return ConnectorCreatorFactory.isStandAlone(detail) || ConnectorCreatorFactory.isInput(detail);
    }

    /**
     * Creates and adds {@link EParameterName#STARTABLE} parameter
     * See TUP-4142
     */
    private void addStartableParameter() {
        final ElementParameter parameter = new ElementParameter(node);
        parameter.setName(EParameterName.STARTABLE.getName());
        parameter.setValue(isStartable());
        parameter.setDisplayName(EParameterName.STARTABLE.getDisplayName());
        parameter.setFieldType(EParameterFieldType.CHECK);
        parameter.setCategory(EComponentCategory.TECHNICAL);
        parameter.setNumRow(5);
        parameter.setReadOnly(true);
        parameter.setRequired(false);
        parameter.setShow(false);
        parameters.add(parameter);
    }

    /**
     * Creates and adds {@link EParameterName#SUBTREE_START} parameter
     * See TUP-4142
     */
    private void addSubtreeStartParameter() {
        final ElementParameter parameter = new ElementParameter(node);
        parameter.setName(EParameterName.SUBTREE_START.getName());
        parameter.setValue(isStartable());
        parameter.setDisplayName(EParameterName.SUBTREE_START.getDisplayName());
        parameter.setFieldType(EParameterFieldType.CHECK);
        parameter.setCategory(EComponentCategory.TECHNICAL);
        parameter.setNumRow(5);
        parameter.setReadOnly(true);
        parameter.setRequired(false);
        parameter.setShow(false);
        parameters.add(parameter);
    }

    /**
     * Creates and adds {@link EParameterName#END_OF_FLOW} parameter See TUP-4142
     */
    private void addEndOfFlowParameter() {
        final ElementParameter parameter = new ElementParameter(node);
        parameter.setName(EParameterName.END_OF_FLOW.getName());
        parameter.setValue(isStartable());
        parameter.setDisplayName(EParameterName.END_OF_FLOW.getDisplayName());
        parameter.setFieldType(EParameterFieldType.CHECK);
        parameter.setCategory(EComponentCategory.TECHNICAL);
        parameter.setNumRow(5);
        parameter.setReadOnly(true);
        parameter.setRequired(false);
        parameter.setShow(false);
        parameters.add(parameter);
    }

    /**
     * Creates and adds {@link EParameterName#ACTIVATE} parameter
     * See TUP-4142
     */
    private void addActivateParameter() {
        final ElementParameter parameter = new ElementParameter(node);
        parameter.setName(EParameterName.ACTIVATE.getName());
        parameter.setValue(true);
        parameter.setDisplayName(EParameterName.ACTIVATE.getDisplayName());
        parameter.setFieldType(EParameterFieldType.CHECK);
        parameter.setCategory(EComponentCategory.TECHNICAL);
        parameter.setNumRow(5);
        parameter.setReadOnly(false);
        parameter.setRequired(false);
        parameter.setDefaultValue(parameter.getValue());
        parameter.setShow(true);
        parameters.add(parameter);
    }

    private void addParameterForClose() {
        final ElementParameter connectionParameter = new ElementParameter(node);
        connectionParameter.setName(TaCoKitConst.PARAMETER_CONNECTION);
        connectionParameter.setValue("");
        connectionParameter.setDisplayName("Component List");
        connectionParameter.setFieldType(EParameterFieldType.COMPONENT_LIST);
        connectionParameter.setCategory(EComponentCategory.BASIC);
        connectionParameter.setNumRow(1);
        connectionParameter.setFilter(VirtualComponentModel.getDefaultConnectionName(component.getIndex()));
        connectionParameter.setReadOnly(false);
        connectionParameter.setRequired(false);
        connectionParameter.setShow(true);
        parameters.add(connectionParameter);
    }

    private void updateParameterForComponentList() {
        for (IElementParameter param : parameters) {
            if (TaCoKitUtil.isDataStorePath(component, param.getName())) {
                updateShowIfValue4ComponentList(param);
            }
        }
        final ElementParameter parameter = new ElementParameter(node);
        parameter.setName(TaCoKitConst.PARAMETER_USE_EXISTING_CONNECTION);
        parameter.setValue(false);
        parameter.setDisplayName(Messages.getString("ElementParameterCreator.userExistConnectionLabel"));
        parameter.setFieldType(EParameterFieldType.CHECK);
        parameter.setCategory(EComponentCategory.BASIC);
        parameter.setNumRow(1);
        parameter.setReadOnly(false);
        parameter.setRequired(false);
        parameter.setShow(true);
        parameter.setDefaultValue(false);
        parameters.add(parameter);

        final ElementParameter connectionParameter = new ElementParameter(node);
        connectionParameter.setName(TaCoKitConst.PARAMETER_CONNECTION);
        connectionParameter.setDisplayName(Messages.getString("ElementParameterCreator.connectionLabel"));
        connectionParameter.setFieldType(EParameterFieldType.COMPONENT_LIST);
        connectionParameter.setCategory(EComponentCategory.BASIC);
        connectionParameter.setFilter(VirtualComponentModel.getDefaultConnectionName(component.getIndex()));
        connectionParameter.setReadOnly(false);
        connectionParameter.setRequired(true);
        connectionParameter.setNumRow(1);
        connectionParameter.setShow(true);
        connectionParameter.setValue("");
        connectionParameter.setDynamicSettings(true);
        connectionParameter.setShowIf("USE_EXISTING_CONNECTION == 'true'");
        parameters.add(connectionParameter);
    }
    
    private void updateShowIfValue4ComponentList(IElementParameter parameter) {
        String value = parameter.getShowIf();
        if (StringUtils.isEmpty(value)) {
            value = "USE_EXISTING_CONNECTION == 'false'";
        } else {
            value = "(" + value + ")" + " && USE_EXISTING_CONNECTION == 'false'";
        }
        parameter.setShowIf(value);
    }

    /**
     * Creates and adds {@link EParameterName#HELP} parameter See TUP-4143
     */
    private void addHelpParameter() {
        final ElementParameter parameter = new ElementParameter(node);
        parameter.setName(EParameterName.HELP.getName());
        parameter.setValue(IComponent.PROP_HELP);
        parameter.setDisplayName(EParameterName.HELP.getDisplayName());
        parameter.setFieldType(EParameterFieldType.TEXT);
        parameter.setCategory(EComponentCategory.TECHNICAL);
        parameter.setNumRow(6);
        parameter.setReadOnly(true);
        parameter.setRequired(false);
        parameter.setShow(false);
        parameters.add(parameter);
    }

    /**
     * Creates and adds {@link EParameterName#UPDATE_COMPONENTS} parameter
     * This parameter is used to decide whether UI should be redrawn during Composite refresh
     */
    private void addUpdateComponentsParameter() {
        final ElementParameter parameter = new ElementParameter(node);
        parameter.setName(EParameterName.UPDATE_COMPONENTS.getName());
        parameter.setValue(false);
        parameter.setDisplayName(EParameterName.UPDATE_COMPONENTS.getDisplayName());
        parameter.setFieldType(EParameterFieldType.CHECK);
        parameter.setCategory(EComponentCategory.TECHNICAL);
        parameter.setNumRow(5);
        parameter.setReadOnly(true);
        parameter.setRequired(false);
        parameter.setShow(false);
        parameters.add(parameter);
        updateComponentsParameter = parameter;
    }

    /**
     * Creates and adds {@link EParameterName#IREPORT_PATH} parameter
     */
    private void addIReportPathParameter() {
        final ElementParameter parameter = new ElementParameter(node);
        parameter.setName(EParameterName.IREPORT_PATH.getName());
        parameter.setCategory(EComponentCategory.TECHNICAL);
        parameter.setFieldType(EParameterFieldType.DIRECTORY);
        parameter.setDisplayName(EParameterName.IREPORT_PATH.getDisplayName());
        parameter.setNumRow(99);
        parameter.setShow(false);
        parameter.setValue(reportPath);
        parameter.setReadOnly(true);
        parameters.add(parameter);
    }

    /**
     * Creates and adds "PROPERTY" parameter.
     * This parameter represents a switch between repository properties values and component own values.
     * "Property" parameter has 2 subproperties: property type and repository type.
     * Property type is a switch between repository value (REPOSITORY option) and component own values (BUILT-IN
     * option).
     * Repository type allows to choose a name of repository node to use.
     * This property is shown in the 1st row of component properties, before component schema
     */
    private void addPropertyParameter() {
        final ElementParameter parameter = new ElementParameter(node);
        parameter.setName("PROPERTY");
        parameter.setCategory(BASIC);
        parameter.setDisplayName(EParameterName.PROPERTY_TYPE.getDisplayName());
        parameter.setFieldType(EParameterFieldType.PROPERTY_TYPE);
        parameter.setRepositoryValue(computeRepositoryNodeType());
        parameter.setValue("");
        parameter.setNumRow(1);
        parameter.setShow(!findConfigurationTypes().isEmpty());
        if (TaCoKitUtil.isSupportUseExistConnection(component)) {
            updateShowIfValue4ComponentList(parameter);
        }

        final ElementParameter propertyType = new ElementParameter(node);
        propertyType.setCategory(EComponentCategory.BASIC);
        propertyType.setName(EParameterName.PROPERTY_TYPE.getName());
        propertyType.setDisplayName(EParameterName.PROPERTY_TYPE.getDisplayName());
        propertyType.setListItemsDisplayName(
                new String[] { AbstractBasicComponent.TEXT_BUILTIN, AbstractBasicComponent.TEXT_REPOSITORY });
        propertyType.setListItemsDisplayCodeName(
                new String[] { AbstractBasicComponent.BUILTIN, AbstractBasicComponent.REPOSITORY });
        propertyType
                .setListItemsValue(new String[] { AbstractBasicComponent.BUILTIN, AbstractBasicComponent.REPOSITORY });
        propertyType.setValue(AbstractBasicComponent.BUILTIN);
        propertyType.setNumRow(parameter.getNumRow());
        propertyType.setFieldType(EParameterFieldType.TECHNICAL);
        propertyType.setShow(true);
        propertyType.setShowIf(parameter.getName() + " =='" + AbstractBasicComponent.REPOSITORY + "'");
        propertyType.setReadOnly(parameter.isReadOnly());
        propertyType.setNotShowIf(parameter.getNotShowIf());
        // TODO find out whether it is required
        propertyType.setContext("FLOW");
        propertyType.setSerialized(true);
        propertyType.setParentParameter(parameter);
        if (TaCoKitUtil.isSupportUseExistConnection(component)) {
            updateShowIfValue4ComponentList(parameter);
        }

        final ElementParameter repositoryType = new ElementParameter(node);
        repositoryType.setCategory(EComponentCategory.BASIC);
        repositoryType.setName(EParameterName.REPOSITORY_PROPERTY_TYPE.getName());
        repositoryType.setDisplayName(EParameterName.REPOSITORY_PROPERTY_TYPE.getDisplayName());
        repositoryType.setListItemsDisplayName(new String[] {});
        repositoryType.setListItemsValue(new String[] {});
        repositoryType.setNumRow(parameter.getNumRow());
        repositoryType.setFieldType(EParameterFieldType.TECHNICAL);
        repositoryType.setValue("");
        repositoryType.setShow(true);
        repositoryType.setRequired(true);
        repositoryType.setReadOnly(parameter.isReadOnly());
        repositoryType.setShowIf(parameter.getName() + " =='" + AbstractBasicComponent.REPOSITORY + "'");
        repositoryType.setNotShowIf(parameter.getNotShowIf());
        // TODO find out whether it is required
        repositoryType.setContext("FLOW");
        repositoryType.setSerialized(true);
        repositoryType.setParentParameter(parameter);
        if (TaCoKitUtil.isSupportUseExistConnection(component)) {
            updateShowIfValue4ComponentList(parameter);
        }
        parameters.add(parameter);
    }

    /**
     * Computes and returns types of suitable repository nodes.
     * They are represented as a String separated by '|'
     *
     * @return suitable repository node types
     */
    private String computeRepositoryNodeType() {
        return findConfigurationTypes().stream().map(p -> {
            final String configName = p.getConfigurationTypeName();
            final String configType = p.getConfigurationType();
            final ConfigTypeNode configNode = Lookups.taCoKitCache().getConfigTypeNode(detail.getId().getFamily(), configName,
                    configType);
            if (configNode == null) {
                return "";
            }
            return TaCoKitUtil.getTaCoKitRepositoryKey(configNode);
        }).collect(Collectors.joining("|"));
    }

    private List<PropertyDefinitionDecorator> findConfigurationTypes() {
        return properties.stream().filter(PropertyDefinitionDecorator::hasConfigurationType).collect(
                Collectors.toList());
    }

    /**
     * Creates and adds {@link EParameterName#TSTATCATCHER_STATS} parameter
     */
    private void addStatCatcherParameter() {
        if (ComponentCategory.CATEGORY_4_DI.getName().equals(component.getPaletteType())) {
            final ElementParameter parameter = new ElementParameter(node);
            parameter.setName(EParameterName.TSTATCATCHER_STATS.getName());
            parameter.setValue(Boolean.FALSE);
            parameter.setDisplayName(EParameterName.TSTATCATCHER_STATS.getDisplayName());
            parameter.setFieldType(EParameterFieldType.CHECK);
            parameter.setCategory(ADVANCED);
            parameter.setNumRow(199);
            parameter.setReadOnly(false);
            parameter.setRequired(false);
            parameter.setDefaultValue(parameter.getValue());
            parameter.setShow(isCatcherAvailable);
            parameters.add(parameter);
        }
    }

    /**
     * Creates and adds {@link EParameterName#PARALLELIZE} parameter
     * See TESB-4279
     */
    private void addParallelizeParameter() {
        if (isCamelCategory()) {
            final ElementParameter parameter = new ElementParameter(node);
            parameter.setReadOnly(true);
            parameter.setName(EParameterName.PARALLELIZE.getName());
            parameter.setValue(Boolean.FALSE);
            parameter.setDisplayName(EParameterName.PARALLELIZE.getDisplayName());
            parameter.setFieldType(EParameterFieldType.CHECK);
            parameter.setCategory(ADVANCED);
            parameter.setNumRow(200);
            parameter.setShow(true);
            parameter.setDefaultValue(parameter.getValue());
            parameters.add(parameter);
        }
    }

    /**
     * Creates and adds {@link EParameterName#PARALLELIZE_NUMBER} parameter
     * See TESB-4279
     */
    private void addParallelizeNumberParameter() {
        if (isCamelCategory()) {
            final ElementParameter parameter = new ElementParameter(node);
            parameter.setReadOnly(true);
            parameter.setName(EParameterName.PARALLELIZE_NUMBER.getName());
            parameter.setValue(2);
            parameter.setDisplayName(EParameterName.PARALLELIZE_NUMBER.getDisplayName());
            parameter.setFieldType(EParameterFieldType.TEXT);
            parameter.setCategory(ADVANCED);
            parameter.setNumRow(200);
            parameter.setShowIf(EParameterName.PARALLELIZE.getName() + " == 'true'");
            parameter.setDefaultValue(parameter.getValue());
            parameters.add(parameter);
        }
    }

    /**
     * Creates and adds {@link EParameterName#PARALLELIZE_KEEP_EMPTY} parameter
     * See TESB-4279
     */
    private void addParallelizeKeepEmptyParameter() {
        if (isCamelCategory()) {
            final ElementParameter parameter = new ElementParameter(node);
            parameter.setReadOnly(true);
            parameter.setName(EParameterName.PARALLELIZE_KEEP_EMPTY.getName());
            parameter.setValue(Boolean.FALSE);
            parameter.setDisplayName(EParameterName.PARALLELIZE_KEEP_EMPTY.getDisplayName());
            parameter.setFieldType(EParameterFieldType.CHECK);
            parameter.setCategory(ADVANCED);
            parameter.setNumRow(200);
            parameter.setShow(false);
            parameter.setDefaultValue(parameter.getValue());
            parameters.add(parameter);
        }
    }

    private boolean isCamelCategory() {
        return PluginChecker.isTeamEdition()
                && !ComponentCategory.CATEGORY_4_CAMEL.getName().equals(component.getPaletteType());
    }

    /**
     * Creates and adds {@link EParameterName#UNIQUE_NAME} parameter
     * This parameter stores unique id of component instance in current job/process
     * It's value is like following "tJiraInput_1", "tJiraInput_2"
     * Value is computed later and can't be computed here as {@link ComponentModel}
     * doesn't now how many component instances were created before
     */
    private void addUniqueNameParameter() {
        final ElementParameter parameter = new ElementParameter(node);
        parameter.setName(EParameterName.UNIQUE_NAME.getName());
        parameter.setValue("");
        parameter.setDisplayName(EParameterName.UNIQUE_NAME.getDisplayName());
        parameter.setFieldType(EParameterFieldType.TEXT);
        parameter.setCategory(ADVANCED);
        parameter.setNumRow(1);
        parameter.setReadOnly(true);
        parameter.setShow(false);
        parameters.add(parameter);
    }

    /**
     * Creates and adds {@link EParameterName#COMPONENT_NAME} parameter
     * It is used in code generation to know which component to load for runtime
     */
    private void addComponentNameParameter() {
        final ElementParameter parameter = new ElementParameter(node);
        parameter.setName(EParameterName.COMPONENT_NAME.getName());
        if (component instanceof VirtualComponentModel) {
            parameter.setValue(((VirtualComponentModel) component).getComponentName());
        } else {
            parameter.setValue(detail.getId().getName());
        }
        parameter.setDisplayName(EParameterName.COMPONENT_NAME.getDisplayName());
        parameter.setFieldType(EParameterFieldType.TEXT);
        parameter.setCategory(EComponentCategory.TECHNICAL);
        parameter.setNumRow(1);
        parameter.setReadOnly(true);
        parameter.setShow(false);
        parameters.add(parameter);
    }

    /**
     * Creates and adds TACOKIT_COMPONENT_ID parameter. It is used in serialization/deserialization to know whether it
     * is Tacokit component and to find ComponentDetail quickly
     */
    private void addTacokitComponentIdParameter() {
        final ElementParameter parameter = new ElementParameter(node);
        parameter.setName(TaCoKitNode.TACOKIT_COMPONENT_ID);
        if (component instanceof VirtualComponentModel) {
            parameter.setValue(((VirtualComponentModel) component).getComponentId());
        } else {
            parameter.setValue(detail.getId().getId());
        }
        parameter.setDisplayName(TaCoKitNode.TACOKIT_COMPONENT_ID);
        parameter.setFieldType(EParameterFieldType.TECHNICAL);
        parameter.setCategory(EComponentCategory.BASIC);
        parameter.setNumRow(1);
        parameter.setReadOnly(true);
        parameter.setShow(false);
        parameters.add(parameter);
    }

    private void addTaCoKitMetadataTypeIdParameter() {
        final ElementParameter parameter = new ElementParameter(node);
        parameter.setName(TaCoKitNode.TACOKIT_METADATA_TYPE_ID);
        parameter.setFieldType(EParameterFieldType.TEXT);
        parameter.setValue(null);
        parameter.setRepositoryValue(TaCoKitNode.TACOKIT_METADATA_TYPE_ID);
        parameter.setDisplayName(TaCoKitNode.TACOKIT_METADATA_TYPE_ID);
        parameter.setFieldType(EParameterFieldType.TECHNICAL);
        parameter.setCategory(EComponentCategory.BASIC);
        parameter.setNumRow(1);
        parameter.setReadOnly(false);
        parameter.setShow(false);
        parameters.add(parameter);
    }

    /**
     * Creates and adds {@link EParameterName#VERSION} parameter
     * Its value is component version. More specifically it is a version of component configuration.
     * It is used for migration to check whether serialized configuration
     * has the same version as component in Studio. If version is smaller than component in Studio,
     * migration is launched.
     */
    private void addVersionParameter() {
        final ElementParameter parameter = new ElementParameter(node);
        parameter.setName(EParameterName.VERSION.getName());
        parameter.setValue(String.valueOf(detail.getVersion()));
        parameter.setDisplayName(EParameterName.VERSION.getDisplayName());
        parameter.setFieldType(EParameterFieldType.TEXT);
        parameter.setCategory(EComponentCategory.TECHNICAL);
        parameter.setNumRow(1);
        parameter.setReadOnly(true);
        parameter.setShow(false);
        parameters.add(parameter);
    }

    private void addViewParameters() {
        ElementParameter parameter = new ElementParameter(node);
        parameter.setName(EParameterName.LABEL.getName());
        parameter.setDisplayName(EParameterName.LABEL.getDisplayName());
        parameter.setFieldType(EParameterFieldType.TEXT);
        parameter.setCategory(EComponentCategory.VIEW);
        parameter.setNumRow(1);
        parameter.setReadOnly(false);
        parameter.setRequired(false);
        parameter.setShow(true);
        // in case label/format is not set in the preferences.
        String label = DesignerPlugin.getDefault().getPreferenceStore().getString(TalendDesignerPrefConstants.DEFAULT_LABEL);
        if (!"".equals(label)) { //$NON-NLS-1$
            parameter.setValue(label);
        }
        parameter.setDefaultValue(parameter.getValue());
        parameters.add(parameter);

        parameter = new ElementParameter(node);
        parameter.setName(EParameterName.HINT.getName());
        parameter.setDisplayName(EParameterName.HINT.getDisplayName());
        parameter.setFieldType(EParameterFieldType.TEXT);
        parameter.setCategory(EComponentCategory.VIEW);
        parameter.setNumRow(2);
        parameter.setReadOnly(false);
        parameter.setRequired(false);
        parameter.setShow(true);
        // in case hint/format is not set in the preferences.
        label = DesignerPlugin.getDefault().getPreferenceStore().getString(TalendDesignerPrefConstants.DEFAULT_HINT);
        if (!"".equals(label)) { //$NON-NLS-1$
            parameter.setValue(label);
        }
        parameter.setDefaultValue(parameter.getValue());
        parameters.add(parameter);

        parameter = new ElementParameter(node);
        parameter.setName(EParameterName.CONNECTION_FORMAT.getName());
        parameter.setDisplayName(EParameterName.CONNECTION_FORMAT.getDisplayName());
        parameter.setFieldType(EParameterFieldType.TEXT);
        parameter.setCategory(EComponentCategory.VIEW);
        parameter.setNumRow(3);
        parameter.setReadOnly(false);
        parameter.setRequired(false);
        parameter.setShow(true);
        // in case connection/format is not set in the preferences.
        label = DesignerPlugin.getDefault().getPreferenceStore().getString(TalendDesignerPrefConstants.DEFAULT_CONNECTION_FORMAT);
        if (!"".equals(label)) { //$NON-NLS-1$
            parameter.setValue(label);
        }
        parameter.setDefaultValue(parameter.getValue());
        parameters.add(parameter);
    }

    private void addDocParameters() {
        final ElementParameter parameter = new ElementParameter(node);
        parameter.setName(EParameterName.INFORMATION.getName());
        parameter.setValue(new Boolean(false));
        parameter.setDisplayName(EParameterName.INFORMATION.getDisplayName());
        parameter.setFieldType(EParameterFieldType.CHECK);
        parameter.setCategory(EComponentCategory.DOC);
        parameter.setNumRow(1);
        parameter.setReadOnly(false);
        parameter.setRequired(false);
        parameter.setShow(true);
        parameter.setDefaultValue(parameter.getValue());
        parameters.add(parameter);
    }
}
