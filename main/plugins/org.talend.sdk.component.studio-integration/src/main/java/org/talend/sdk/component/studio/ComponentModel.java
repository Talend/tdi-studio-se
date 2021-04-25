/**
 * Copyright (C) 2006-2021 Talend Inc. - www.talend.com
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
package org.talend.sdk.component.studio;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;
import static org.talend.sdk.component.studio.model.ReturnVariables.AFTER;
import static org.talend.sdk.component.studio.model.ReturnVariables.RETURN_ERROR_MESSAGE;
import static org.talend.sdk.component.studio.model.ReturnVariables.RETURN_TOTAL_RECORD_COUNT;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.jface.resource.ImageDescriptor;
import org.talend.commons.exception.ExceptionHandler;
import org.talend.core.PluginChecker;
import org.talend.core.model.components.ComponentCategory;
import org.talend.core.model.components.EComponentType;
import org.talend.core.model.components.IComponent;
import org.talend.core.model.general.ModuleNeeded;
import org.talend.core.model.metadata.types.JavaTypesManager;
import org.talend.core.model.process.EConnectionType;
import org.talend.core.model.process.IConnection;
import org.talend.core.model.process.IConnectionCategory;
import org.talend.core.model.process.IElementParameter;
import org.talend.core.model.process.INode;
import org.talend.core.model.process.INodeConnector;
import org.talend.core.model.process.INodeReturn;
import org.talend.core.model.process.IProcess;
import org.talend.core.model.properties.ProcessItem;
import org.talend.core.model.temp.ECodePart;
import org.talend.core.runtime.IAdditionalInfo;
import org.talend.core.runtime.maven.MavenConstants;
import org.talend.core.runtime.util.ComponentReturnVariableUtils;
import org.talend.designer.core.model.components.AbstractBasicComponent;
import org.talend.designer.core.model.components.NodeReturn;
import org.talend.designer.core.model.process.DataNode;
import org.talend.designer.runprocess.ItemCacheManager;
import org.talend.sdk.component.server.front.model.ActionReference;
import org.talend.sdk.component.server.front.model.ComponentDetail;
import org.talend.sdk.component.server.front.model.ComponentId;
import org.talend.sdk.component.server.front.model.ComponentIndex;
import org.talend.sdk.component.server.front.model.ConfigTypeNodes;
import org.talend.sdk.component.server.front.model.SimplePropertyDefinition;
import org.talend.sdk.component.studio.enums.ETaCoKitComponentType;
import org.talend.sdk.component.studio.metadata.migration.TaCoKitMigrationManager;
import org.talend.sdk.component.studio.model.connector.ConnectorCreatorFactory;
import org.talend.sdk.component.studio.model.connector.TaCoKitNodeConnector;
import org.talend.sdk.component.studio.model.parameter.ElementParameterCreator;
import org.talend.sdk.component.studio.model.parameter.Metadatas;
import org.talend.sdk.component.studio.mvn.Mvn;
import org.talend.sdk.component.studio.service.ComponentService;
import org.talend.sdk.component.studio.util.TaCoKitUtil;

public class ComponentModel extends AbstractBasicComponent implements IAdditionalInfo {

    protected final ComponentIndex index;

    protected final ComponentDetail detail;

    protected final String reportPath;

    protected final boolean isCatcherAvailable;

    private final ImageDescriptor image;

    private final ImageDescriptor image24;

    private final ImageDescriptor image16;

    /**
     * All palette entries for component joined by "|" Component palette entry is
     * computed as category + "/" + familyName E.g.
     * "Business/Salesforce|Cloud/Salesforce", where "Business", "Cloud" are
     * categories, "Salesforce" - is familyName
     */
    private final String paletteValue;

    private volatile Set<ModuleNeeded> modulesNeeded;

    private Map<String, Object> additionalInfoMap = new HashMap<>();

    private Boolean useLookup = null;

    private ETaCoKitComponentType tacokitComponentType;

    private final ConfigTypeNodes configTypeNodes;

    protected final TaCoKitMigrationManager manager = Lookups.taCoKitCache().getMigrationManager();

    public ComponentModel(final ComponentIndex component, final ComponentDetail detail, final ConfigTypeNodes configTypeNodes, final ImageDescriptor image32,
            final String reportPath, final boolean isCatcherAvailable) {
        setPaletteType(ComponentCategory.CATEGORY_4_DI.getName());
        this.index = component;
        this.detail = detail;
        this.configTypeNodes = configTypeNodes;
        this.tacokitComponentType = ETaCoKitComponentType.valueOf(this.detail.getType().toLowerCase());
        this.paletteValue = computePaletteValue();
        this.codePartListX = createCodePartList();
        this.reportPath = reportPath;
        this.isCatcherAvailable = isCatcherAvailable;
        this.image = image32;
        this.image24 = ImageDescriptor.createFromImageData(image.getImageData().scaledTo(24, 24));
        this.image16 = ImageDescriptor.createFromImageData(image.getImageData().scaledTo(16, 16));
    }

    @Deprecated // to drop since it is not used at all in main code
    ComponentModel(final ComponentIndex component, final ComponentDetail detail) {
        setPaletteType("DI");
        this.index = component;
        this.detail = detail;
        this.tacokitComponentType = ETaCoKitComponentType.valueOf(this.detail.getType().toLowerCase());
        this.paletteValue = computePaletteValue();
        this.codePartListX = createCodePartList();
        this.image = null;
        this.image24 = null;
        this.image16 = null;
        this.reportPath = null;
        this.isCatcherAvailable = false;
        createCodePartList();
        this.configTypeNodes = null;
    }

    @Override // this is our binding of slf4j
    public boolean isLog4JEnabled() {
        return true;
    }

    /**
     * Computes palette value, which is used to define component location in Studio palette
     * Palette value has following format: "Category1/Family|Category2/Family"
     * Component may have several entries in palette (each entry is in different category)
     * Entries in palette value are separated with "|"
     * "/" separates categories, subcategories and family
     *
     * @return palette value
     */
    private String computePaletteValue() {
        return index.getCategories().stream().collect(Collectors.joining("|"));
    }

    /**
     * Creates unmodifiable list of code part templates (.javajet) All Tacokit
     * component have following 4 parts by default:
     * <ul>
     * <li>BEGIN</li>
     * <li>MAIN</li>
     * <li>END</li>
     * <li>FINALLYS</li>
     * </ul>
     *
     * @return
     */
    private List<ECodePart> createCodePartList() {
        return Collections.unmodifiableList(ETaCoKitComponentType.input.equals(getTaCoKitComponentType())
                ? Arrays.asList(ECodePart.BEGIN, ECodePart.END, ECodePart.FINALLY)
                : (useLookup()
                ?
                Arrays.asList(ECodePart.BEGIN, ECodePart.PROCESS_DATA_BEGIN, ECodePart.MAIN, ECodePart.PROCESS_DATA_END, ECodePart.PROCESS_RECORDS_END,
                        ECodePart.END_HEAD, ECodePart.END_BODY,
                        ECodePart.END_TAIL, ECodePart.FINALLY)
                :
                Arrays.asList(ECodePart.BEGIN, ECodePart.PROCESS_DATA_BEGIN, ECodePart.MAIN, ECodePart.PROCESS_DATA_END, ECodePart.PROCESS_RECORDS_END,
                        ECodePart.END, ECodePart.FINALLY)));
    }

    /**
     * @return component name (e.g. "tSalesforceInput")
     */
    @Override
    public String getName() {
        return TaCoKitUtil.getFullComponentName(index.getId().getFamily(), index.getId().getName());
    }

    /**
     * Looks the same as {@link #getName()}
     */
    @Override
    public String getOriginalName() {
        return getName();
    }

    @Override
    public String getDisplayName() {
        return TaCoKitUtil.getDisplayName(index);
    }

    /**
     * Returns long component name, aka title (e.g. "Salesforce Input"). It is i18n
     * title. In v0 component it is specified by "component.{compName}.title"
     * message key
     *
     * @return long component name, aka title (e.g. "") or translated
     */
    @Override
    public String getLongName() {
        return index.getDisplayName();
    }

    /**
     * Returns string which is concatenation of all component palette entries.
     * This string has following format: "Category1/Family|Category2/SubCategory2/Family"
     * Different entries are separated with "|".
     * "/" separates categories, subcategories and family inside one entry
     *
     * @return all palette entries for this component
     */
    @Override
    public String getOriginalFamilyName() {
        return paletteValue;
    }

    /**
     * Returns i18n family names
     */
    @Override
    public String getTranslatedFamilyName() {
        return getOriginalFamilyName();
    }

    /**
     * Returns short component name, which is obtained in following way All capital
     * letters are picked and converted to lower case E.g. the short name for
     * "tSalesforceInput" is "si"
     *
     * @return short component name
     */
    @Override
    public String getShortName() {
        return getName();
    }

    /**
     * @return original component icon
     */
    @Override
    public ImageDescriptor getIcon32() {
        return image;
    }

    /**
     * @return component icon scaled to 24x24
     */
    @Override
    public ImageDescriptor getIcon24() {
        return image24;
    }

    /**
     * @return component icon scaled to 16x16
     */
    @Override
    public ImageDescriptor getIcon16() {
        return image16;
    }

    /**
     * Returns Component configuration version
     * When Configuration is changed its version should be incremented and corresponding MigrationHandler implemented to
     * migrate from
     * older versions to newer during deserialization
     */
    @Override
    public String getVersion() {
        return Integer.toString(detail.getVersion());
    }

    /**
     * Creates component parameters aka Properties/Configuration
     */
    @Override // TODO This is dummy implementation. Correct impl should be added soon
    public List<? extends IElementParameter> createElementParameters(final INode node) {
        if (isNeedMigration() && node.getProcess() != null) {
            ProcessItem processItem = ItemCacheManager.getProcessItem(node.getProcess().getId());
            if (processItem != null) {
                manager.checkNodeMigration(processItem, getName());
            }
        }
        ElementParameterCreator creator = new ElementParameterCreator(this, detail, node, reportPath, isCatcherAvailable);
        List<IElementParameter> parameters = (List<IElementParameter>) creator.createParameters();
        return parameters;
    }

    /**
     * Creates component return variables For the moment it returns only
     * ERROR_MESSAGE and NB_LINE after variables
     *
     * @return list of component return variables
     */
    @Override
    public List<? extends INodeReturn> createReturns(final INode node) {
        List<NodeReturn> returnVariables = new ArrayList<>();

        NodeReturn errorMessage = new NodeReturn();
        errorMessage.setType(JavaTypesManager.STRING.getLabel());
        errorMessage.setDisplayName(
                ComponentReturnVariableUtils.getTranslationForVariable(RETURN_ERROR_MESSAGE, RETURN_ERROR_MESSAGE));
        errorMessage.setName(ComponentReturnVariableUtils.getStudioNameFromVariable(RETURN_ERROR_MESSAGE));
        errorMessage.setAvailability(AFTER);
        returnVariables.add(errorMessage);

        NodeReturn numberLinesMessage = new NodeReturn();
        numberLinesMessage.setType(JavaTypesManager.INTEGER.getId());
        numberLinesMessage.setDisplayName(ComponentReturnVariableUtils
                .getTranslationForVariable(RETURN_TOTAL_RECORD_COUNT, RETURN_TOTAL_RECORD_COUNT));
        numberLinesMessage.setName(ComponentReturnVariableUtils.getStudioNameFromVariable(RETURN_TOTAL_RECORD_COUNT));
        numberLinesMessage.setAvailability(AFTER);
        returnVariables.add(numberLinesMessage);

        return returnVariables;
    }

    /**
     * Creates component connectors. It creates all possible connector even if some
     * of them are not applicable for component. In such cases not applicable
     * connector has 0 outgoing and incoming links.
     *
     * @param node component node - object representing component instance on design
     *             canvas
     */
    @Override
    public List<? extends INodeConnector> createConnectors(final INode node) {
        return ConnectorCreatorFactory.create(this, detail, node).createConnectors();
    }

    /**
     * TODO decide about API for this feature
     */
    @Override
    public boolean isSchemaAutoPropagated() {
        return true;
    }

    /**
     * TODO decide about API for this feature
     */
    @Override
    public boolean isDataAutoPropagated() {
        return false;
    }

    /**
     * Get the default modules needed for the component.
     *
     * @return common v1 components Job dependencies
     */
    @Override
    public List<ModuleNeeded> getModulesNeeded() {
        return getModulesNeeded(null);
    }

    /**
     * get component required dependencies
     *
     * @param iNode
     * @return
     */
    @Override
    public List<ModuleNeeded> getModulesNeeded(final INode iNode) {
        if (modulesNeeded == null) {
            synchronized (this) {
                if (modulesNeeded == null) {
                    final ComponentService.Dependencies dependencies = getDependencies();
                    modulesNeeded = new LinkedHashSet<>(20);
                    modulesNeeded.addAll(dependencies
                            .getCommon()
                            .stream()
                            .map(s -> new ModuleNeeded(getName(), "", true, s))
                            .collect(toList()));
                    modulesNeeded.add(new ModuleNeeded(getName(), "", true, "mvn:org.talend.sdk.component/component-runtime-di/" + GAV.INSTANCE.getComponentRuntimeVersion()));
                    modulesNeeded.add(new ModuleNeeded(getName(), "", true, "mvn:org.talend.sdk.component/component-runtime-design-extension/" + GAV.INSTANCE.getComponentRuntimeVersion()));
                    modulesNeeded.add(new ModuleNeeded(getName(), "", true, "mvn:org.slf4j/slf4j-api/" + GAV.INSTANCE.getSlf4jVersion()));

                    if (!hasTcomp0Component(iNode)) {
                        if (!PluginChecker.isTIS()) {
                            modulesNeeded.add(new ModuleNeeded(getName(), "", true, "mvn:" + GAV.INSTANCE.getGroupId() + "/slf4j-standard/" + GAV.INSTANCE.getComponentRuntimeVersion()));
                        } else {
                            modulesNeeded.add(new ModuleNeeded(getName(), "", true, "mvn:org.slf4j/slf4j-log4j12/" + GAV.INSTANCE.getSlf4jVersion()));
                        }
                    }

                    final Map<String, ?> componentDependencies = !Lookups.configuration().isActive() ? null : Lookups.client().v1().component().dependencies(detail.getId().getId());
                    if (componentDependencies != null && componentDependencies.containsKey("dependencies")) {
                        final Collection<String> coordinates = Collection.class.cast(Map.class
                                .cast(Map.class.cast(componentDependencies.get("dependencies")).values().iterator().next())
                                .get("dependencies"));
                        if (coordinates != null) {
                            modulesNeeded.addAll(coordinates.stream()
                                    .map(coordinate -> new ModuleNeeded(getName(), "", true, Mvn.locationToMvn(coordinate).replace(MavenConstants.LOCAL_RESOLUTION_URL + '!', "")))
                                    .collect(Collectors.toList()));
                            if (coordinates.contains("org.apache.beam") || coordinates.contains(":beam-sdks-java-io")) {
                                modulesNeeded.addAll(dependencies
                                        .getBeam()
                                        .stream()
                                        .map(s -> new ModuleNeeded(getName(), "", true, s))
                                        .collect(toList()));
                            }
                        }
                    }

                    // We're assuming that pluginLocation has format of groupId:artifactId:version
                    final String location = index.getId().getPluginLocation().trim();
                    modulesNeeded.add(new ModuleNeeded(getName(), "", true, Mvn.locationToMvn(location).replace(MavenConstants.LOCAL_RESOLUTION_URL + '!', "")));
                }
            }
        }
        return new ArrayList<>(modulesNeeded);
    }

    protected boolean hasTcomp0Component(final INode iNode) {
        if (iNode == null) {
            return false;
        }
        IProcess process = iNode.getProcess();
        if (process == null) {
            return false;
        }
        List<? extends INode> graphicalNodes = process.getGraphicalNodes();
        if (graphicalNodes == null || graphicalNodes.isEmpty()) {
            return false;
        }
        boolean hasTcomp0Component = false;
        for (INode node : graphicalNodes) {
            if (node != null) {
                IComponent component = node.getComponent();
                if (component != null) {
                    if (component.getComponentType() == EComponentType.GENERIC && !getClass().isInstance(component)) {
                        hasTcomp0Component = true;
                        break;
                    }
                }
            }
        }
        return hasTcomp0Component;
    }

    protected ComponentService.Dependencies getDependencies() {
        return Lookups.service().getDependencies();
    }

    /**
     * Returns code parts (.javajet templates) for this component. All Tacokit
     * componenta have same set of common templates:
     * <ul>
     * <li>BEGIN</li>
     * <li>MAIN</li>
     * <li>END</li>
     * <li>FINALLYS</li>
     * </ul>
     */
    @Override
    public List<ECodePart> getAvailableCodeParts() {
        return this.codePartListX;
    }

    /**
     * Returns component type, which is EComponentType.GENERIC
     */
    @Override
    public EComponentType getComponentType() {
        return EComponentType.GENERIC;
    }

    @Override
    public String getTemplateFolder() {
        return "tacokit/jet_stub/generic/" + detail.getType().toLowerCase(Locale.ENGLISH);
    }

    @Override
    public String getTemplateNamePrefix() {
        return detail.getType().toLowerCase(Locale.ENGLISH);
    }

    /**
     * Check whether current component can use the given configuration
     *
     * @param familyNodeName family name
     * @param configType     configuration type
     * @param configName     configuration name
     * @return true if support, otherwise false
     */
    public boolean supports(final String familyNodeName, final String configType, final String configName) {
        Collection<SimplePropertyDefinition> properties = detail.getProperties();
        if (properties == null || properties.isEmpty()) {
            return false;
        }
        String expectedFamilyName = index.getId().getFamily();
        if (expectedFamilyName == null || !expectedFamilyName.equals(familyNodeName)) { // $NON-NLS-1$
            return false;
        }
        Iterator<SimplePropertyDefinition> iter = properties.iterator();
        while (iter.hasNext()) {
            SimplePropertyDefinition simplePropDefine = iter.next();
            Map<String, String> metadata = simplePropDefine.getMetadata();
            if (metadata == null) {
                continue;
            }
            String type = metadata.get(Metadatas.CONFIG_TYPE);
            if (type == null || !type.equals(configType)) {
                continue;
            }
            String name = metadata.get(Metadatas.CONFIG_NAME);
            if (name == null || !name.equals(configName)) {
                continue;
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean useLookup() {
        if (useLookup != null) {
            return useLookup;
        }
        useLookup = Boolean.FALSE;
        List<? extends INodeConnector> connectors = createConnectors(new DataNode(this, "checkLookup")); //$NON-NLS-1$
        if (connectors != null) {
            for (INodeConnector connector : connectors) {
                if (EConnectionType.FLOW_MAIN.equals(connector.getDefaultConnectionType())) {
                    if (1 < connector.getMaxLinkInput()) {
                        useLookup = Boolean.TRUE;
                        break;
                    }
                }
            }
        }
        return useLookup;
    }

    @Override
    public Object getInfo(final String key) {
        return additionalInfoMap.get(key);
    }

    @Override
    public void putInfo(final String key, final Object value) {
        additionalInfoMap.put(key, value);
    }

    @Override
    public void onEvent(final String event, final Object... parameters) {
        if (event == null) {
            return;
        }
        try {
            switch (event) {
            case IConnection.EVENT_UPDATE_INPUT_CONNECTION:
                onUpdateInputConnection(parameters);
                break;
            default:
                break;
            }
        } catch (Exception e) {
            ExceptionHandler.process(e);
        }
    }

    private void onUpdateInputConnection(final Object... parameters) {
        if (parameters == null || parameters.length < 2) {
            throw new IllegalArgumentException(
                    "Can only accept one node and one connection, please adapt it if needed.");
        }
        if (parameters[0] == null || parameters[1] == null) {
            return;
        }
        if (!INode.class.isInstance(parameters[0]) || !IConnection.class.isInstance(parameters[1])) {
            throw new IllegalArgumentException(
                    "Can only accept one node and one connection, please adapt it if needed.");
        }
        final String paramInputName = "INPUT_NAME"; //$NON-NLS-1$
        INode node = (INode) parameters[0];
        IConnection connection = (IConnection) parameters[1];
        // EConnectionType lineStyle = connection.getLineStyle();
        // if (EConnectionType.FLOW_MAIN == lineStyle) {
        // return;
        // }

        if (connection instanceof IAdditionalInfo) {
            String inputName = (String) IAdditionalInfo.class.cast(connection).getInfo(paramInputName);
            if (inputName != null && !inputName.trim().isEmpty()) {
                // if already set, just return
                return;
            }
            Set<String> usedInputSet = new HashSet<>();
            List<? extends IConnection> incomingConnections = node.getIncomingConnections();
            if (incomingConnections != null) {
                for (IConnection incomingConnection : incomingConnections) {
                    INodeConnector connector = incomingConnection.getTargetNodeConnector();
                    EConnectionType connType = (connector == null ? null : connector.getDefaultConnectionType());
                    if (connType != null && connType.hasConnectionCategory(IConnectionCategory.FLOW)) {
                        if (IAdditionalInfo.class.isInstance(incomingConnection)) {
                            IAdditionalInfo inconnInfo = (IAdditionalInfo) incomingConnection;
                            Object info = inconnInfo.getInfo(paramInputName);
                            if (info != null) {
                                usedInputSet.add(info.toString());
                            }
                        }
                    }
                }
            }
            List<String> availableInputs = new ArrayList<>();
            List<? extends INodeConnector> connectors = createConnectors(node);
            for (INodeConnector connector : connectors) {
                if (connector instanceof TaCoKitNodeConnector) {
                    if (((TaCoKitNodeConnector) connector).hasInput()) {
                        String connectorName = connector.getName();
                        if (!availableInputs.contains(connectorName)) {
                            availableInputs.add(connectorName);
                        }
                    }
                }
            }
            availableInputs.removeAll(usedInputSet);
            Collections.sort(availableInputs);
            if (!availableInputs.isEmpty()) {
                IAdditionalInfo.class.cast(connection).putInfo(paramInputName, availableInputs.get(0));
            }
        }
    }

    @Override
    public void cloneAddionalInfoTo(final IAdditionalInfo targetAdditionalInfo) {
        if (targetAdditionalInfo == null) {
            return;
        }
        for (Map.Entry<String, Object> entry : additionalInfoMap.entrySet()) {
            targetAdditionalInfo.putInfo(entry.getKey(), entry.getValue());
        }
    }

    @Override
    public boolean hasConditionalOutputs() {
        return detail.getOutputFlows().size() > 1;
    }

    public List<ActionReference> getDiscoverSchemaActions() {
        if (detail == null || detail.getActions() == null || detail.getActions().isEmpty()) {
            return emptyList();
        }

        return detail.getActions().stream()
                .filter(a -> "schema".equals(a.getType()))
                .collect(toList());
    }

    public String getPluginName() {
        if (index == null
                || index.getId() == null
                || index.getId().getPlugin() == null
                || index.getId().getPlugin().isEmpty()) {
            throw new IllegalStateException("No plugin id found for this node " + this);
        }

        return index.getId().getPlugin();
    }

    public ETaCoKitComponentType getTaCoKitComponentType() {
        if (this.tacokitComponentType == null) {
            this.tacokitComponentType = ETaCoKitComponentType.valueOf(this.detail.getType().toLowerCase());
        }
        return this.tacokitComponentType;
    }

    public ConfigTypeNodes getConfigTypeNodes() {
        return configTypeNodes;
    }

    public ComponentId getId(){
        return this.index.getId();
    }

    @Override
    public boolean isMadeByTalend() {
        return TaCoKitUtil.isTaCoKitComponentMadeByTalend(this.index);
    }

    @Override
    public boolean isVisibleInComponentDefinition() {
        return true;
    }

    public ComponentDetail getDetail() {
        return detail;
    }

    public ComponentIndex getIndex() {
        return index;
    }

}
