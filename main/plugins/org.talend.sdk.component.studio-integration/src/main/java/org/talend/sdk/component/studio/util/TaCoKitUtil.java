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
package org.talend.sdk.component.studio.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.Stack;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.emf.common.util.EList;
import org.eclipse.m2e.core.MavenPlugin;
import org.talend.commons.utils.data.container.Container;
import org.talend.commons.utils.system.EnvironmentUtils;
import org.talend.core.model.components.ComponentCategory;
import org.talend.core.model.components.IComponent;
import org.talend.core.model.general.Project;
import org.talend.core.model.process.IElement;
import org.talend.core.model.process.IElementParameter;
import org.talend.core.model.process.INode;
import org.talend.core.model.process.IProcess;
import org.talend.core.model.properties.ConnectionItem;
import org.talend.core.model.properties.Item;
import org.talend.core.model.properties.ProcessItem;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.model.repository.IRepositoryViewObject;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.core.runtime.maven.MavenArtifact;
import org.talend.core.runtime.maven.MavenConstants;
import org.talend.core.runtime.maven.MavenUrlHelper;
import org.talend.core.ui.component.ComponentsFactoryProvider;
import org.talend.designer.core.model.utils.emf.talendfile.NodeType;
import org.talend.designer.core.utils.UnifiedComponentUtil;
import org.talend.repository.ProjectManager;
import org.talend.sdk.component.server.front.model.ActionItem;
import org.talend.sdk.component.server.front.model.ActionList;
import org.talend.sdk.component.server.front.model.ComponentIndex;
import org.talend.sdk.component.server.front.model.ConfigTypeNode;
import org.talend.sdk.component.server.front.model.ConfigTypeNodes;
import org.talend.sdk.component.server.front.model.SimplePropertyDefinition;
import org.talend.sdk.component.studio.ComponentModel;
import org.talend.sdk.component.studio.Lookups;
import org.talend.sdk.component.studio.VirtualComponentModel;
import org.talend.sdk.component.studio.VirtualComponentModel.VirtualComponentModelType;
import org.talend.sdk.component.studio.metadata.TaCoKitCache;
import org.talend.sdk.component.studio.metadata.WizardRegistry;
import org.talend.sdk.component.studio.metadata.model.TaCoKitConfigurationModel;
import org.talend.sdk.component.studio.model.action.SuggestionsAction;
import org.talend.sdk.component.studio.model.parameter.PropertyDefinitionDecorator;
import org.talend.sdk.component.studio.model.parameter.PropertyNode;
import org.talend.sdk.component.studio.model.parameter.ValueSelectionParameter;
import org.talend.updates.runtime.utils.PathUtils;
import org.talend.utils.io.FilesUtils;

/**
 * DOC cmeng class global comment. Detailled comment
 */
public class TaCoKitUtil {
    /**
     * Get ConnectionItem from specified project
     *
     * @param project {@link Project} only search from the given project
     * @param itemId  item id
     *
     * @return stored item of the given parameters, or null
     *
     * @throws Exception unexpected exception occured during searching
     */
    public static ConnectionItem getLatestTaCoKitConnectionItem(final Project project, final String itemId)
            throws Exception {
        IRepositoryViewObject lastVersion = ProxyRepositoryFactory.getInstance().getLastVersion(project, itemId, null,
                TaCoKitConst.METADATA_TACOKIT);
        if (lastVersion != null) {
            return (ConnectionItem) lastVersion.getProperty().getItem();
        }
        return null;
    }

    /**
     * Get ConnectionItem from main project or it's reference project
     *
     * @param itemId item id
     *
     * @return stored item of the given parameters, or null
     *
     * @throws Exception unexpected exception occured during searching
     */
    public static ConnectionItem getLatestTaCoKitConnectionItem(final String itemId) throws Exception {
        ConnectionItem item = getLatestTaCoKitConnectionItem(ProjectManager.getInstance().getCurrentProject(), itemId);
        if (item != null) {
            return item;
        }
        List<Project> allReferencedProjects = ProjectManager.getInstance().getAllReferencedProjects();
        if (allReferencedProjects != null && !allReferencedProjects.isEmpty()) {
            for (Project referenceProject : allReferencedProjects) {
                item = getLatestTaCoKitConnectionItem(referenceProject, itemId);
                if (item != null) {
                    return item;
                }
            }
        }
        return null;
    }

    public static IPath getTaCoKitBaseFolder(final ConfigTypeNode configNode) {
        if (configNode == null) {
            return null;
        }
        IPath baseFolderPath = new Path(""); //$NON-NLS-1$
        String parentId = configNode.getParentId();
        if (!isEmpty(parentId)) {
            ConfigTypeNode parentTypeNode = Lookups.taCoKitCache().getConfigTypeNodeMap().get(parentId);
            if (parentTypeNode == null) {
                throw new NullPointerException("Can't find parent node: " + parentId);
            }
            baseFolderPath = getTaCoKitBaseFolder(parentTypeNode);
        }
        // better to use lowercase, since different OS support different path name
        String configName = getTaCoKitFolderName(configNode);
        baseFolderPath = baseFolderPath.append(configName);
        return baseFolderPath;
    }

    public static String getTaCoKitFolderName(final ConfigTypeNode configNode) {
        return configNode.getName().toLowerCase();
    }

    public static Container<String, IRepositoryViewObject> getContainer(
            Container<String, IRepositoryViewObject> tacokitRootContainer, final ConfigTypeNode configNode) {
        if (tacokitRootContainer == null) {
            return null;
        }
        if (configNode == null) {
            return null;
        }
        String parentId = configNode.getParentId();
        if (parentId != null) {
            ConfigTypeNode parentConfigTypeNode = Lookups.taCoKitCache().getConfigTypeNodeMap().get(parentId);
            Container<String, IRepositoryViewObject> container = getContainer(tacokitRootContainer, parentConfigTypeNode);
            if (container == null) {
                return null;
            } else {
                return container.getSubContainer(getTaCoKitFolderName(configNode));
            }
        } else {
            return tacokitRootContainer.getSubContainer(getTaCoKitFolderName(configNode));
        }
    }

    public static TaCoKitConfigurationModel getTaCoKitConfigurationModel(final String itemId) throws Exception {
        ConnectionItem item = getLatestTaCoKitConnectionItem(itemId);
        if (item != null) {
            return new TaCoKitConfigurationModel(item.getConnection());
        }
        return null;
    }

    public static String getConfigTypePath(final ConfigTypeNode configTypeNode) {
        IPath tacokitPath = new Path(TaCoKitConst.METADATA_TACOKIT.getFolder());
        IPath path = tacokitPath.append(getTaCoKitBaseFolder(configTypeNode));
        return path.toPortableString();
    }

    public static String getTaCoKitRepositoryKey(final ConfigTypeNode configTypeNode) {
        String configTypePath = getConfigTypePath(configTypeNode);
        /**
         * Keep the prefix: "repository.", since there are some codes like: <br/>
         * objectType.getKey().toString().startsWith("repository.metadata") <br/>
         * For example: DeleteAction
         */
        return TaCoKitConst.METADATA_PREFIX + configTypePath.replaceAll("/", "."); //$NON-NLS-1$ //$NON-NLS-2$
    }

    public static ERepositoryObjectType getOrCreateERepositoryObjectType(final ConfigTypeNode configTypeNode)
            throws Exception {
        if (configTypeNode == null) {
            return null;
        }
        String label = configTypeNode.getDisplayName();
        String folderPathStr = getConfigTypePath(configTypeNode);

        String type = getTaCoKitRepositoryKey(configTypeNode);
        String alias = folderPathStr.replaceAll("/", "_"); //$NON-NLS-1$//$NON-NLS-2$

        ERepositoryObjectType eType = ERepositoryObjectType.valueOf(type);
        if (eType == null) {
            eType = new WizardRegistry().createRepositoryObjectType(type, label, alias, folderPathStr, 1,
                    new String[]{ ERepositoryObjectType.PROD_DI });
            TaCoKitCache taCoKitCache = Lookups.taCoKitCache();
            ConfigTypeNode parentTypeNode = taCoKitCache.getConfigTypeNodeMap().get(configTypeNode.getParentId());
            if (parentTypeNode == null) {
                eType.setAParent(TaCoKitConst.METADATA_TACOKIT);
            } else {
                eType.setAParent(getOrCreateERepositoryObjectType(parentTypeNode));
            }
            taCoKitCache.getRepositoryObjectType2ConfigTypeNodeMap().put(eType, configTypeNode);
        }
        return eType;
    }

    public static boolean isTaCoKitType(final ERepositoryObjectType repObjType) {
        if (repObjType == null) {
            return false;
        }
        if (TaCoKitConst.METADATA_TACOKIT.equals(repObjType)) {
            return true;
        }
        ERepositoryObjectType[] parentTypesArray = repObjType.getParentTypesArray();
        if (parentTypesArray == null || parentTypesArray.length <= 0) {
            return false;
        }
        for (ERepositoryObjectType parentType : parentTypesArray) {
            if (isTaCoKitType(parentType)) {
                return true;
            }
        }
        return false;
    }

    public static boolean equals(final String str1, final String str2) {
        return str1 == null ? str2 == null : str1.equals(str2);
    }

    public static boolean isEmpty(final String str) {
        return str == null || str.length() == 0;
    }

    public static boolean isBlank(final String str) {
        return StringUtils.isBlank(str);
    }

    /**
     * Method to create component name from component's family name and component's name itself.
     *
     * @param familyName    component's family name
     * @param componentName component's name
     *
     * @return full component name
     */
    public static String getFullComponentName(final String familyName, final String componentName) {
        return familyName + TaCoKitConst.COMPONENT_NAME_SEPARATOR + componentName;
    }

    public static Collection<ConfigTypeNode> filterTopLevelNodes(Collection<ConfigTypeNode> nodes) {
        Collection<ConfigTypeNode> filteredNodes = new ArrayList<>();
        if (nodes != null && !nodes.isEmpty()) {
            for (ConfigTypeNode node : nodes) {
                String parentId = node.getParentId();
                String configType = node.getConfigurationType();
                if (StringUtils.isNotBlank(parentId) || StringUtils.isNotBlank(configType)) {
                    continue;
                }
                filteredNodes.add(node);
            }
        }
        return filteredNodes;
    }

    public static String getInstalledComponentsString(IProgressMonitor progress) throws Exception {
        File studioConfigFile = PathUtils.getStudioConfigFile();
        Properties configProps = PathUtils.readProperties(studioConfigFile);
        return configProps.getProperty(TaCoKitConst.PROP_COMPONENT);
    }

    public static List<GAV> getInstalledComponents(IProgressMonitor progress) throws Exception {
        String tckCompConfString = getInstalledComponentsString(progress);
        if (StringUtils.isNotBlank(tckCompConfString)) {
            return TaCoKitUtil.convert2GAV(tckCompConfString);
        }
        return Collections.EMPTY_LIST;
    }

    public static List<GAV> convert2GAV(String gavString) {
        List<GAV> gavs = new ArrayList<>();
        String[] componentsStr = gavString.split(","); //$NON-NLS-1$
        for (String componentStr : componentsStr) {
            String[] component = componentStr.split(":"); //$NON-NLS-1$
            GAV gav = new GAV();
            gav.setGroupId(component[0]);
            gav.setArtifactId(component[1]);
            gav.setVersion(component[2]);
            if (3 < component.length) {
                gav.setClassifier(component[3]);
            }
            if (4 < component.length) {
                gav.setType(component[4]);
            }
            gavs.add(gav);
        }
        return gavs;
    }

    public static void checkMonitor(IProgressMonitor monitor) throws Exception {
        if (monitor != null) {
            if (monitor.isCanceled()) {
                throw new InterruptedException("progress.cancel"); //$NON-NLS-1$
            }
        }
    }

    public static boolean hideConfigFolderOnSingleEdge() {
        return true;
    }

    public static void registAllTaCoKitRepositoryTypes() throws Exception {
        Map<String, ConfigTypeNode> nodes = Lookups.taCoKitCache().getConfigTypeNodeMap();
        if (nodes != null) {
            for (ConfigTypeNode node : nodes.values()) {
                TaCoKitUtil.getOrCreateERepositoryObjectType(node);
            }
        }
    }

    public static String getDisplayName(final ComponentIndex index) {
        if (index != null) {
            String componentName = getFullComponentName(index.getId().getFamily(), index.getId().getName());
            if (isTaCoKitComponentMadeByTalend(index)) {
                return TaCoKitConst.COMPONENT_NAME_PREFIX + componentName;
            }
            return componentName;
        }
        return null;
    }

    public static PropertyNode getSamePropertyNode(PropertyNode propertyNode, ConfigTypeNode configTypeNode) throws Exception {
        return getSamePropertyNode(new Stack<>(), getRootPropertyNode(propertyNode), configTypeNode);
    }

    private static PropertyNode getSamePropertyNode(Stack<Object> visited, PropertyNode propertyNode,
                                                    ConfigTypeNode configTypeNode) {
        if (propertyNode == null || visited.contains(propertyNode)) {
            return null;
        }
        PropertyDefinitionDecorator property = propertyNode.getProperty();
        if (property == null) {
            return null;
        }
        if (StringUtils.equals(property.getConfigurationType(), configTypeNode.getConfigurationType())
                && StringUtils.equals(property.getConfigurationTypeName(), configTypeNode.getName())) {
            return propertyNode;
        }
        try {
            visited.push(propertyNode);
            List<PropertyNode> children = propertyNode.getChildren();
            if (children != null) {
                for (PropertyNode c : children) {
                    PropertyNode pn = getSamePropertyNode(visited, c, configTypeNode);
                    if (pn != null) {
                        return pn;
                    }
                }
            }

            return null;
        } finally {
            visited.pop();
        }
    }

    public static PropertyNode getRootPropertyNode(PropertyNode propertyNode) throws Exception {
        Set<Object> visited = new HashSet<>();
        PropertyNode node = propertyNode;
        PropertyNode parentNode = node;
        while (node != null) {
            if (visited.contains(node)) {
                throw new IllegalArgumentException("dead loop detected from input parameter");
            } else {
                visited.add(node);
            }
            parentNode = node;
            node = node.getParent();
        }
        return parentNode;
    }

    public static boolean isTaCoKitComponentMadeByTalend(final ComponentIndex index) {
        if (index != null) {
            String location = index.getId().getPluginLocation().trim();
            if (StringUtils.isNotBlank(location) && location.startsWith(MavenConstants.DEFAULT_GROUP_ID)) {
                return true;
            }
        }
        return false;
    }

    public static int getConfigTypeVersion(final PropertyDefinitionDecorator p, final ConfigTypeNodes configTypeNodes,
                                           final String familyId) {
        final String type = p.getMetadata().get("configurationtype::type");
        final String name = p.getMetadata().get("configurationtype::name");
        return configTypeNodes.getNodes().values().stream()
                .filter(c -> c.getConfigurationType() != null && c.getName() != null)
                .filter(c -> c.getConfigurationType().equals(type) && c.getName().equals(name))
                .filter(c -> familyId.equals(getPropertyFamilyId(c, configTypeNodes))).findFirst()
                .map(ConfigTypeNode::getVersion)
                .orElse(-1);
    }

    public static String getPropertyFamilyId(final ConfigTypeNode it, final ConfigTypeNodes nodes) {
        if (it.getParentId() == null) {
            return null;
        }
        String parent = it.getParentId();
        while (nodes.getNodes().get(parent) != null && nodes.getNodes().get(parent).getParentId() != null) {
            parent = nodes.getNodes().get(parent).getParentId();
        }
        return parent;
    }

    /**
     * Find the maven repository path.
     *
     * @return the configured m2 repository path
     */
    public static java.nio.file.Path findM2Path() {
        return Optional.ofNullable(System.getProperty("talend.component.manager.m2.repository"))
                .map(Paths::get)
                .orElseGet(() -> {
                    // check if we are in the studio process if so just grab the the studio config
                    final String m2Repo = System.getProperty("maven.repository");
                    if (!"global".equals(m2Repo)) {
                        final String m2StudioRepo = EnvironmentUtils.isWindowsSystem()
                                ? System.getProperty("osgi.configuration.area", "").replaceAll("^file:/", "")
                                : System.getProperty("osgi.configuration.area", "").replaceAll("^file:", "");
                        final java.nio.file.Path localM2 = Paths.get(m2StudioRepo, ".m2/repository");
                        if (Files.exists(localM2)) {
                            return localM2;
                        }
                    }
                    // defaults to user m2
                    return Paths.get(System.getProperty("user.home", "")).resolve(".m2/repository");
                });
    }

    /**
     * Translates a GAV (ie com.tutorial:tutorial-component:0.0.1) to a maven repository path (ie com/tutorial/tutorial-component/0.0.1/tutorial-component-0.0.1.jar).
     *
     * @param gav GroupId ArtifactId Version. The GAV may have the following forms:
     *            com.tutorial:tutorial-component:0.0.1
     *            or
     *            com.tutorial:tutorial-component:jar:0.0.1:compile
     *
     * @return a translated maven path
     */
    public static String gavToMvnPath(String gav) {
        final String jarPathFmt = "%s/%s/%s/%s-%s.jar";
        final String[] segments = gav.split(":");
        if (segments.length < 3) {
            throw new IllegalArgumentException("Bad GAV given!"); // TODO improve message
        }
        String group = segments[0].replaceAll("\\.", "/");
        String artifact = segments[1];
        String version = "";
        if (segments.length == 3) {
            version = segments[2];
        } else {
            version = segments[3];
        }
        return String.format(jarPathFmt, group, artifact, version, artifact, version);
    }


    /**
     * Get all components defined in <code>item</code>.
     *
     * @param item the currently processed <code>ProcessItem</code> during job build export
     *
     * @return a non-null stream of {@link IComponent}
     */
    public static Stream<IComponent> getJobComponents(final Item item) {
        final EList<?> nodes = ProcessItem.class.cast(item).getProcess().getNode();
        final String DI = ComponentCategory.CATEGORY_4_DI.getName();
        return nodes.stream().map(node -> {
            final String componentName = ((NodeType) node).getComponentName();
            IComponent component = ComponentsFactoryProvider.getInstance().get(componentName, DI);
            if (component == null) {
                component = UnifiedComponentUtil.getDelegateComponent(componentName, DI);
            }
            return component;
        }).filter(Objects::nonNull);
    }

    /**
     * Get component-runtime components from <code>components</code>.
     *
     * @param components <code>{@link IComponent}</code>
     *
     * @return a non-null stream of {@link ComponentModel}
     */
    public static Stream<ComponentModel> getTaCoKitComponents(final Stream<IComponent> components) {
        return components
                .filter(ComponentModel.class::isInstance)
                .map(ComponentModel.class::cast);
    }

    /**
     * Check if <code>components</code> holds component-runtime components.
     *
     * @param components <code>IComponent</code>
     *
     * @return true if item has some component-runtime components
     */
    public static boolean hasTaCoKitComponents(final Stream<IComponent> components) {
        return components.anyMatch(ComponentModel.class::isInstance);
    }
    
    /**
     * Check the component whether support use exist connection or not
     * @param component
     * @return
     */
    public static boolean isSupportUseExistConnection(ComponentModel component) {
        boolean isSupport = false;
        ActionList actionList = Lookups.taCoKitCache().getActionList(component.getIndex().getFamilyDisplayName());
        if (actionList != null) {
            for (ActionItem action : actionList.getItems()) {
                if (TaCoKitConst.CREATE_CONNECTION_ATCION_NAME.equals(action.getType())
                        || TaCoKitConst.CLOSE_CONNECTION_ATCION_NAME.equals(action.getType())) {
                    isSupport = true;
                    break;
                }
            }
        }
        if (isSupport && component instanceof VirtualComponentModel) {
            if (((VirtualComponentModel) component).getModelType() == VirtualComponentModelType.CONNECTION) {
                isSupport = false;
            }
        }
        return isSupport;
    }
    
    /**
     *  Get component datasotre properties
     * @param component
     * @return
     */
    public static Map<String, PropertyDefinitionDecorator> getComponentDataStoreProperties(ComponentModel component) {
        final Map<String, PropertyDefinitionDecorator> tree = new HashMap<>();
        TaCoKitCache cache = Lookups.taCoKitCache();
        ConfigTypeNode configTypeNode = cache.findDatastoreConfigTypeNodeByName(component.getDetail().getId().getFamily());
        if (configTypeNode != null && configTypeNode.getProperties() != null) {
            final Collection<PropertyDefinitionDecorator> properties = PropertyDefinitionDecorator
                    .wrap(configTypeNode.getProperties());
            properties.forEach(p -> tree.put(p.getPath(), p));
        }
        return tree;
    }
    
    public static boolean isUseExistConnection(INode node) {
        if (node != null) {
            for (IElementParameter ele : node.getElementParameters()) {
                if (TaCoKitConst.PARAMETER_USE_EXISTING_CONNECTION.equals(ele.getName())) {
                    if (ele.getValue() != null && Boolean.parseBoolean(ele.getValue().toString())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
    
    public static String getUseExistConnectionName(INode node) {
        if (node != null) {
            for (IElementParameter ele : node.getElementParameters()) {
                if (TaCoKitConst.PARAMETER_CONNECTION.equals(ele.getName())) {
                    if (ele.getValue() == null || StringUtils.isEmpty(ele.getValue().toString())) {
                        return null;
                    } else {
                        return ele.getValue().toString();
                    }
                }
            }
        }
        return null;
    }
    
    public static Object getParameterValueFromConnection(INode node, String parameterName) {
        String connectionName = getUseExistConnectionName(node);
        if (connectionName != null) {
            IProcess process = node.getProcess();
            INode connectionNode = process.getNodeByUniqueName(connectionName);
            if (connectionNode != null) {
                String datastoreName = TaCoKitUtil.getDataStorePath((ComponentModel) node.getComponent(), parameterName);
                IElementParameter param = connectionNode.getElementParameter(datastoreName);
                if (param != null) {
                    return param.getValue();
                } else {
                    throw new IllegalArgumentException("Can't find parameter:" + parameterName);
                }
            } else {
                throw new IllegalArgumentException("Can't find connection node:" + connectionName);
            }
        }
        return null;
    }
    
    public static boolean isDataStoreParameter(INode node, String parameterName) {
        if (node.getComponent() instanceof ComponentModel) {
            ComponentModel model = (ComponentModel) node.getComponent();
            if (TaCoKitUtil.isDataStorePath(model, parameterName)) {
                return true;
            }
        }
        return false;
    }

    /**
     *  Check the path is datastore path or not
     * @param model
     * @param path
     * @return
     */
    public static boolean isDataStorePath(ComponentModel model, String path) {
        return getDataStorePath(model, path) == null ? false : true;
    }

    /**
     *  Get current path in datastore
     * @param model
     * @param path
     * @return
     */
    public static String getDataStorePath(ComponentModel model, String path) {
        Map<String, PropertyDefinitionDecorator> datastoreProperties = TaCoKitUtil.getComponentDataStoreProperties(model);
        if (datastoreProperties.containsKey(path)) {
            return path;
        }
        String configPath = TaCoKitUtil.getConfigurationPath(model.getDetail().getProperties());
        String datastorePath = TaCoKitUtil.getDatastorePath(model.getDetail().getProperties());
        if (configPath != null && datastorePath != null) {
            String replacedPath = path.replaceFirst(datastorePath, configPath);
            if (datastoreProperties.containsKey(replacedPath)) {
                return replacedPath;
            }
        }
        return null;
    }
    
    public static String getDatastorePath(Collection<SimplePropertyDefinition> properties) {
        for (SimplePropertyDefinition p : properties) {
            if (StringUtils.equalsIgnoreCase(TaCoKitConst.CONFIG_NODE_ID_DATASTORE, p.getName())) {
                return p.getPath();
            }
        }
        for (SimplePropertyDefinition p : properties) {
            if (StringUtils.equalsIgnoreCase(TaCoKitConst.CONFIG_NODE_ID_CONNECTION, p.getName())) {
                return p.getPath();
            }
        }
        return null;
    }
    
    public static String getConfigurationPath(Collection<SimplePropertyDefinition> properties) {
        for (SimplePropertyDefinition p : properties) {
            if (StringUtils.equalsIgnoreCase(TaCoKitConst.CONFIG_NODE_ID_CONFIGURATION, p.getName())) {
                return p.getPath();
            }
        }
        return null;
    }
    
    public static void checkM2TacokitStatus() throws Exception {
        File studioConfigFile = PathUtils.getStudioConfigFile();
        Properties configuration = PathUtils.readProperties(studioConfigFile);
        String repositoryType = configuration.getProperty("maven.repository", ""); //$NON-NLS-1$ //$NON-NLS-2$
        String m2RootFromContext = System.getProperty("maven.local.repository"); //$NON-NLS-1$
        if (StringUtils.isBlank(m2RootFromContext) || !"global".equals(repositoryType)) {
            return;
        }
        // zero-install CI mode
        List<GAV> cars = TaCoKitUtil.getInstalledComponents(new NullProgressMonitor());
        java.nio.file.Path basePath = new File(MavenPlugin.getMaven().getLocalRepositoryPath()).toPath();
        java.nio.file.Path defaultBasePath = new File(System.getProperty("user.home"), ".m2/repository/").toPath(); //$NON-NLS-1$ //$NON-NLS-2$
        cars.stream().map(c -> {
            String mvnUrl = MavenUrlHelper.generateMvnUrl(c.getGroupId(), c.getArtifactId(), c.getVersion(), c.getType(),
                    c.getClassifier());
            MavenArtifact mavenArtifact = MavenUrlHelper.parseMvnUrl(mvnUrl);
            return MavenUrlHelper.getArtifactPath(mavenArtifact);
        }).forEach(s -> {
            File target = basePath.resolve(s).toFile();
            if (!target.exists()) {
                copyJar(defaultBasePath, basePath, s);
                try (JarFile jar = new JarFile(target)) {
                    JarEntry entry = jar.getJarEntry("TALEND-INF/dependencies.txt"); //$NON-NLS-1$
                    try (BufferedReader reader = new BufferedReader(new InputStreamReader(jar.getInputStream(entry)))) {
                        reader.lines().map(l -> gavToMvnPath(l)).forEach(dep -> copyJar(defaultBasePath, basePath, dep));
                    }
                } catch (IOException e) {
                    throw new IllegalArgumentException("Failed to read jar:", e); //$NON-NLS-1$
                }
            }
        });
    }

    private static void copyJar(java.nio.file.Path sourceBasePath, java.nio.file.Path targetBasePath, String mvnPath) {
        try {
            File source = sourceBasePath.resolve(mvnPath).toFile();
            if (source.exists()) {
                FilesUtils.copyFile(source, targetBasePath.resolve(mvnPath).toFile());
            }
        } catch (IOException e) {
            throw new IllegalArgumentException("Failed to re-deploy jar:", e);
        }
    }

    public static void updateElementParameter(final IElement element, final IElementParameter param, int rowNumber) {
        if (param instanceof ValueSelectionParameter) {
            ValueSelectionParameter vsParam = ((ValueSelectionParameter) param);
            Map<String, String> suggestedValues = new LinkedHashMap<>();
            SuggestionsAction action = vsParam.getAction();
            action.setRowNumber(rowNumber);
            if (!action.isMissingRequired()) {
                suggestedValues = vsParam.getSuggestionValues();
            }
            param.setListItemsDisplayCodeName(suggestedValues.keySet().toArray(new String[suggestedValues.size()]));
            param.setListItemsValue(suggestedValues.keySet().toArray(new String[suggestedValues.size()]));
            param.setListItemsDisplayName(suggestedValues.keySet().toArray(new String[suggestedValues.size()]));
            param.setListItemsNotShowIf(new String[suggestedValues.size()]);
            param.setListItemsShowIf(new String[suggestedValues.size()]);
        }
    }

    public static void fillDefaultItemsList(final IElementParameter param, Object value) {
        if (param instanceof ValueSelectionParameter) {
            List<String> itemsList = new ArrayList<String>();
            if (value != null && value instanceof String && StringUtils.isNotBlank((String) value)) {
                itemsList.add((String) value);
            }
            param.setListItemsDisplayName(itemsList.toArray(new String[0]));
            param.setListItemsDisplayCodeName(itemsList.toArray(new String[0]));
            param.setListItemsValue(itemsList.toArray(new String[0]));
            param.setListItemsNotShowIf(itemsList.toArray(new String[0]));
            param.setListItemsShowIf(itemsList.toArray(new String[0]));
            param.setDefaultClosedListValue(""); //$NON-NLS-1$
        }
    }

    public static class GAV {

        private String groupId;

        private String artifactId;

        private String version = ""; //$NON-NLS-1$

        private String classifier = ""; //$NON-NLS-1$

        private String type = ""; //$NON-NLS-1$

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((this.artifactId == null) ? 0 : this.artifactId.hashCode());
            result = prime * result + ((this.classifier == null) ? 0 : this.classifier.hashCode());
            result = prime * result + ((this.groupId == null) ? 0 : this.groupId.hashCode());
            result = prime * result + ((this.type == null) ? 0 : this.type.hashCode());
            result = prime * result + ((this.version == null) ? 0 : this.version.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            GAV other = (GAV) obj;
            if (this.artifactId == null) {
                if (other.artifactId != null) {
                    return false;
                }
            } else if (!this.artifactId.equals(other.artifactId)) {
                return false;
            }
            if (this.classifier == null) {
                if (other.classifier != null) {
                    return false;
                }
            } else if (!this.classifier.equals(other.classifier)) {
                return false;
            }
            if (this.groupId == null) {
                if (other.groupId != null) {
                    return false;
                }
            } else if (!this.groupId.equals(other.groupId)) {
                return false;
            }
            if (this.type == null) {
                if (other.type != null) {
                    return false;
                }
            } else if (!this.type.equals(other.type)) {
                return false;
            }
            if (this.version == null) {
                if (other.version != null) {
                    return false;
                }
            } else if (!this.version.equals(other.version)) {
                return false;
            }
            return true;
        }

        @SuppressWarnings("nls")
        @Override
        public String toString() {
            return "GAV [groupId=" + this.groupId + ", artifactId=" + this.artifactId + ", version=" + this.version
                    + ", classifier=" + this.classifier + ", type=" + this.type + "]";
        }

        public String getGroupId() {
            return this.groupId;
        }

        public void setGroupId(String groupId) {
            this.groupId = groupId;
        }

        public String getArtifactId() {
            return this.artifactId;
        }

        public void setArtifactId(String artifactId) {
            this.artifactId = artifactId;
        }

        public String getVersion() {
            return this.version;
        }

        public void setVersion(String version) {
            this.version = version;
        }

        public String getClassifier() {
            return this.classifier;
        }

        public void setClassifier(String classifier) {
            this.classifier = classifier;
        }

        public String getType() {
            return this.type;
        }

        public void setType(String type) {
            this.type = type;
        }

    }
}
