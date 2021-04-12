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
package org.talend.sdk.component.studio.metadata.provider;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.ui.navigator.CommonNavigator;
import org.eclipse.ui.navigator.CommonViewer;
import org.talend.commons.exception.ExceptionHandler;
import org.talend.commons.runtime.model.repository.ERepositoryStatus;
import org.talend.commons.runtime.service.ITaCoKitService;
import org.talend.commons.utils.data.container.Container;
import org.talend.commons.utils.data.container.RootContainer;
import org.talend.core.model.properties.ConnectionItem;
import org.talend.core.model.properties.Property;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.model.repository.Folder;
import org.talend.core.model.repository.IRepositoryViewObject;
import org.talend.core.repository.model.ProjectRepositoryNode;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.repository.model.IProxyRepositoryFactory;
import org.talend.repository.model.IRepositoryNode;
import org.talend.repository.model.RepositoryNode;
import org.talend.repository.model.nodes.IProjectRepositoryNode;
import org.talend.repository.navigator.RepoViewCommonNavigator;
import org.talend.repository.navigator.RepoViewCommonViewer;
import org.talend.repository.view.di.metadata.content.AbstractMetadataContentProvider;
import org.talend.repository.viewer.content.VisitResourceHelper;
import org.talend.repository.viewer.content.listener.ResourceCollectorVisitor;
import org.talend.sdk.component.server.front.model.ConfigTypeNode;
import org.talend.sdk.component.studio.Lookups;
import org.talend.sdk.component.studio.i18n.Messages;
import org.talend.sdk.component.studio.metadata.model.TaCoKitConfigurationItemModel;
import org.talend.sdk.component.studio.metadata.model.TaCoKitConfigurationModel;
import org.talend.sdk.component.studio.metadata.node.ITaCoKitRepositoryNode;
import org.talend.sdk.component.studio.metadata.node.TaCoKitConfigurationRepositoryNode;
import org.talend.sdk.component.studio.metadata.node.TaCoKitFamilyRepositoryNode;
import org.talend.sdk.component.studio.metadata.node.TaCoKitFolderRepositoryNode;
import org.talend.sdk.component.studio.metadata.node.TaCoKitLeafRepositoryNode;
import org.talend.sdk.component.studio.util.TaCoKitConst;
import org.talend.sdk.component.studio.util.TaCoKitUtil;

public class TaCoKitMetadataContentProvider extends AbstractMetadataContentProvider {

    private final Object[] EMPTY_ARRAY = new Object[0];

    private MetadataTaCoKitChildrenNodeVisitor testVisitor;

    private Map<RepositoryNode, Set<RepositoryNode>> familyNodesMapCache;

    public TaCoKitMetadataContentProvider() {
        familyNodesMapCache = new HashMap<>();
    }

    @Override
    protected RepositoryNode getTopLevelNodeFromProjectRepositoryNode(final ProjectRepositoryNode projectRepositoryNode) {
        return projectRepositoryNode.getRootRepositoryNode(TaCoKitConst.METADATA_TACOKIT);
    }

    @Override
    public boolean hasChildren(final Object element) {
        RepositoryNode theRootNode = null;
        // store the root node
        if (isRootNodeType(element)) {
            theRootNode = extractPotentialRootNode(element);
        }
        if (theRootNode != null) {
            getAndStoreTopLevelNode(theRootNode);
        }
        if (element instanceof ITaCoKitRepositoryNode) {
            ITaCoKitRepositoryNode tacokitNode = (ITaCoKitRepositoryNode) element;
            if (tacokitNode.isFamilyNode()) {
                return true;
            }
        }
        return super.hasChildren(element);
    }

    @Override
    public Object[] getChildren(final Object element) {
        try {
            if (!ITaCoKitService.getInstance().isStarted()) {
                return EMPTY_ARRAY;
            }
            if (familyNodesMapCache.isEmpty()) {
                getTopLevelNodes();
            }
            if (isRootNodeType(element)) {
                return getTopLevelNodes((ProjectRepositoryNode) RepositoryNode.class.cast(element).getRoot()).toArray();
            }
            if (element instanceof ITaCoKitRepositoryNode) {
                RepositoryNode repNode = (RepositoryNode) element;
                ITaCoKitRepositoryNode tacoNode = (ITaCoKitRepositoryNode) repNode;
                if (!repNode.isInitialized() && (tacoNode.isFamilyNode() || tacoNode.isLeafNode())) {
                    try {
                        cleanChildren(tacoNode);

                        IProjectRepositoryNode rootNode = repNode.getRoot();
                        RootContainer<String, IRepositoryViewObject> tacokitRootContainer = ProxyRepositoryFactory.getInstance()
                                .getMetadata(rootNode.getProject(), TaCoKitConst.METADATA_TACOKIT, true);
                        initTaCoKitNode(repNode, new HashSet<>(), new HashSet<>(), tacokitRootContainer, true);
                        repNode.setInitialized(true);

                        try {
                            List<IRepositoryNode> repNodes = new ArrayList<>();
                            repNodes.add(repNode);
                            ProjectRepositoryNode.class.cast(rootNode).collectRepositoryNodes(repNodes);
                        } catch (Exception e) {
                            ExceptionHandler.process(e);
                        }
                    } catch (Exception e) {
                        ExceptionHandler.process(e);
                    }
                }
                return ((RepositoryNode) element).getChildren().toArray();
            }
        } catch (Exception e) {
            ExceptionHandler.process(e);
        }
        return EMPTY_ARRAY;
    }

    private void cleanChildren(IRepositoryNode child) {
        if (child == null) {
            return;
        }
        List<IRepositoryNode> children = child.getChildren();
        if (children != null) {
            for (IRepositoryNode c : children) {
                cleanChildren(c);
            }
            children.clear();
        }
    }

    @Override
    public Set<RepositoryNode> getTopLevelNodes() {
        try {
            if (!ITaCoKitService.getInstance().isStarted()) {
                return Collections.EMPTY_SET;
            }
            final ProjectRepositoryNode rootNode = ProjectRepositoryNode.getInstance();
            return getTopLevelNodes(rootNode);
        } catch (Exception e) {
            ExceptionHandler.process(e);
        }
        return Collections.EMPTY_SET;
    }

    public Set<RepositoryNode> getTopLevelNodes(final ProjectRepositoryNode rootNode) {
        try {
            RepositoryNode tacokitNode = rootNode.getRootRepositoryNode(TaCoKitConst.METADATA_TACOKIT);
            if (tacokitNode != null) {
                tacokitNode.setInitialized(true);
            }
            RepositoryNode repoNode = rootNode.getRootRepositoryNode(ERepositoryObjectType.METADATA);
            Set<RepositoryNode> familyNodesCache = familyNodesMapCache.get(repoNode);
            if (!repoNode.isInitialized()) {
                if (familyNodesCache != null && !familyNodesCache.isEmpty()) {
                    repoNode.getChildren().removeAll(familyNodesCache);
                } else if (familyNodesCache == null) {
                    familyNodesCache = getTaCoKitFamilyNodes(repoNode, false);
                }
                clearCache();
                familyNodesCache.stream().forEach(n -> initilizeContentProviderWithTopLevelNode(n));
                rootNode.collectRepositoryNodes(new ArrayList<>(familyNodesCache));
            }
            familyNodesMapCache.put(repoNode, familyNodesCache);
            if (familyNodesCache == null) {
                return Collections.EMPTY_SET;
            } else {
                return familyNodesCache;
            }
        } catch (Exception e) {
            ExceptionHandler.process(e);
        }
        return Collections.EMPTY_SET;
    }

    private Set<RepositoryNode> buildTaCoKitFamilies(final RepositoryNode repositoryNode) {
        try {
            Map<String, ConfigTypeNode> nodes = Lookups.taCoKitCache().getConfigTypeNodeMap();
            Set<RepositoryNode> familyNodes = new HashSet<>();
            if (nodes != null) {
                Collection<ConfigTypeNode> topLevelNodes = TaCoKitUtil.filterTopLevelNodes(nodes.values());
                for (ConfigTypeNode node : topLevelNodes) {
                    TaCoKitFamilyRepositoryNode familyRepositoryNode = createFamilyRepositoryNode(repositoryNode, node);
                    repositoryNode.getChildren().add(familyRepositoryNode);
                    familyNodes.add(familyRepositoryNode);
                    familyRepositoryNode.setInitialized(false);
                }
            }
            return familyNodes;
        } catch (Exception e) {
            ExceptionHandler.process(e);
        }
        return Collections.EMPTY_SET;
    }

    private Set<RepositoryNode> getTaCoKitFamilyNodes(final RepositoryNode repositoryNode, final boolean collectStorage)
            throws Exception {
        Set<RepositoryNode> taCoKitFamilies = buildTaCoKitFamilies(repositoryNode);
        if (taCoKitFamilies == null || taCoKitFamilies.isEmpty()) {
            return Collections.EMPTY_SET;
        }

        // RootContainer<String, IRepositoryViewObject> metadata = ProxyRepositoryFactory.getInstance()
        // .getMetadata(repositoryNode.getRoot().getProject(), TaCoKitConst.METADATA_TACOKIT, true);
        RootContainer<String, IRepositoryViewObject> metadata = null;

        Set<IRepositoryViewObject> unknownObjs = new HashSet<>();
        Set<IRepositoryViewObject> usedObjs = new HashSet<>();
        for (RepositoryNode familyRepoNode : taCoKitFamilies) {
            initTaCoKitNode(familyRepoNode, unknownObjs, usedObjs, metadata, collectStorage);
        }

        return taCoKitFamilies;
    }

    private void initTaCoKitNode(RepositoryNode repoNode, Set<IRepositoryViewObject> allObjs, Set<IRepositoryViewObject> usedObjs,
            Container<String, IRepositoryViewObject> tacokitRootContainer,
            boolean collectStorage) throws Exception {
        ITaCoKitRepositoryNode tacokitNode = (ITaCoKitRepositoryNode) repoNode;
        ConfigTypeNode configTypeNode = tacokitNode.getConfigTypeNode();
        Set<String> edges = configTypeNode.getEdges();
        if (edges != null && !edges.isEmpty()) {
            Map<String, ConfigTypeNode> nodes = Lookups.taCoKitCache().getConfigTypeNodeMap();
            boolean hideConfigFolderMode = (edges.size() <= 1) && TaCoKitUtil.hideConfigFolderOnSingleEdge();
            for (String edge : edges) {
                try {
                    ConfigTypeNode edgeNode = nodes.get(edge);
                    ITaCoKitRepositoryNode parentNode = tacokitNode;
                    ProjectRepositoryNode projectRootNode = ProjectRepositoryNode.class.cast(repoNode.getRoot());
                    IRepositoryNode mapRootNode = null;
                    if (!hideConfigFolderMode) {
                        parentNode = createConfigurationRepositoryNode(repoNode, tacokitNode, edgeNode);
                        repoNode.getChildren().add(parentNode);
                        ((RepositoryNode) parentNode).setInitialized(false);
                        mapRootNode = parentNode;
                    } else {
                        ITaCoKitRepositoryNode familyNode = null;
                        ITaCoKitRepositoryNode testNode = tacokitNode;
                        do {
                            if (testNode == null) {
                                break;
                            }
                            if (testNode.isFamilyNode()) {
                                familyNode = testNode;
                                break;
                            }
                            testNode = testNode.getParentTaCoKitNode();
                        } while (true);
                        mapRootNode = familyNode;
                    }
                    projectRootNode.mapRootRepositoryNode(TaCoKitUtil.getOrCreateERepositoryObjectType(edgeNode), parentNode);
                    Set<String> childEdges = edgeNode.getEdges();
                    if (childEdges != null) {
                        for (String childEdge : childEdges) {
                            mapRootNode(projectRootNode, nodes, childEdge, mapRootNode);
                        }
                    }

                    if (collectStorage) {
                        Set<IRepositoryViewObject> objs = allObjs;
                        Set<IRepositoryViewObject> usedSet = usedObjs;

                        boolean isRootConfigType = false;
                        if (parentNode.isFamilyNode()) {
                            isRootConfigType = true;
                        } else if (parentNode.isConfigNode()) {
                            ITaCoKitRepositoryNode parentTaCoKitNode = parentNode.getParentTaCoKitNode();
                            if (parentTaCoKitNode != null && parentTaCoKitNode.isFamilyNode()) {
                                isRootConfigType = true;
                            }
                        }
                        if (isRootConfigType) {
                            objs = new HashSet<>();
                            usedSet = new HashSet<>();
                        }

                        Container<String, IRepositoryViewObject> container = TaCoKitUtil.getContainer(tacokitRootContainer,
                                edgeNode);
                        loadFromStorage(parentNode, objs, usedSet, container, tacokitRootContainer);
                        ((RepositoryNode) parentNode).setInitialized(true);
                        TaCoKitConfigurationRepositoryNode deprecatedNode = null;
                        if (isRootConfigType) {
                            objs.removeAll(usedSet);
                            if (!objs.isEmpty()) {
                                deprecatedNode = createConfigurationRepositoryNode((RepositoryNode) parentNode, parentNode,
                                        configTypeNode);
                                deprecatedNode.setLabel(Messages.getString("repository.node.missingparent")); //$NON-NLS-1$
                                deprecatedNode.setDeprecated(true);
                                boolean add = false;
                                Map<String, IRepositoryViewObject> objectMap = new HashMap<>();
                                for (IRepositoryViewObject repObj : objs) {
                                    ConnectionItem item = (ConnectionItem) repObj.getProperty().getItem();
                                    TaCoKitConfigurationItemModel itemModule = new TaCoKitConfigurationItemModel(item);
                                    TaCoKitConfigurationModel module = new TaCoKitConfigurationModel(item.getConnection());
                                    // Not be add if parent object was deleted
                                    if (checkParentObjectDeleted(module.getParentItemId(), module.getConfigTypeNode(),
                                            objectMap)) {
                                        continue;
                                    }
                                    TaCoKitLeafRepositoryNode deprecatedLeafNode = createLeafRepositoryNode(deprecatedNode,
                                            deprecatedNode, itemModule, module.getConfigTypeNode(), repObj);
                                    initTaCoKitNode(deprecatedLeafNode, new HashSet<>(), new HashSet<>(), tacokitRootContainer,
                                            true);
                                    deprecatedNode.getChildren().add(deprecatedLeafNode);
                                    add = true;
                                }
                                if (add) {
                                    List<IRepositoryNode> children = parentNode.getChildren();
                                    children.add(children.size(), deprecatedNode);
                                    ((RepositoryNode) deprecatedNode).setInitialized(true);
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    ExceptionHandler.process(e);
                }
            }
        }
        repoNode.setInitialized(true);
    }

    private boolean checkParentObjectDeleted(String requiredParentId, ConfigTypeNode configTypeNode,
            Map<String, IRepositoryViewObject> objectMap) throws Exception {
        if (!TaCoKitUtil.isBlank(requiredParentId)) {
            IRepositoryViewObject parentObj = null;
            if (objectMap.containsKey(requiredParentId)) {
                parentObj = objectMap.get(requiredParentId);
            } else {
                ERepositoryObjectType objectType = TaCoKitUtil.getOrCreateERepositoryObjectType(configTypeNode);
                parentObj = ProxyRepositoryFactory.getInstance().getLastVersion(requiredParentId, null, objectType);
                if (parentObj != null) {
                    objectMap.put(requiredParentId, parentObj);
                }
            }
            if (parentObj != null) {
                if (parentObj.isDeleted()) {
                    return true;
                } else {
                    ConnectionItem item = (ConnectionItem) parentObj.getProperty().getItem();
                    TaCoKitConfigurationModel module = new TaCoKitConfigurationModel(item.getConnection());
                    return checkParentObjectDeleted(module.getParentItemId(), module.getConfigTypeNode(), objectMap);
                }
            }
        }
        return false;
    }

    private void mapRootNode(ProjectRepositoryNode projectRootNode, Map<String, ConfigTypeNode> nodes, String nodeId,
            IRepositoryNode mapRootNode) throws Exception {
        ConfigTypeNode configTypeNode = nodes.get(nodeId);
        if (configTypeNode != null) {
            projectRootNode.mapRootRepositoryNode(TaCoKitUtil.getOrCreateERepositoryObjectType(configTypeNode), mapRootNode);
            Set<String> edges = configTypeNode.getEdges();
            if (edges != null) {
                for (String edge : edges) {
                    mapRootNode(projectRootNode, nodes, edge, mapRootNode);
                }
            }
        }
    }

    private void loadFromStorage(ITaCoKitRepositoryNode parentNode, Set<IRepositoryViewObject> allObjs,
            Set<IRepositoryViewObject> usedObjs, Container<String, IRepositoryViewObject> container,
            Container<String, IRepositoryViewObject> tacokitRootContainer) throws Exception {
        if (container == null) {
            return;
        }
        ConfigTypeNode configTypeNode = parentNode.getConfigTypeNode();
        Map<String, ConfigTypeNode> configTypeNodeMap = Lookups.taCoKitCache().getConfigTypeNodeMap();
        Set<String> edges = configTypeNode.getEdges();
        Set<String> typeFolders = new HashSet<>();
        if (parentNode.isFamilyNode() || parentNode.isLeafNode()) {
            if (edges.size() == 1) {
                configTypeNode = configTypeNodeMap.get(edges.iterator().next());
                edges = configTypeNode.getEdges();
            }
        }
        for (String edge : edges) {
            typeFolders.add(TaCoKitUtil.getTaCoKitFolderName(configTypeNodeMap.get(edge)));
        }

        ERepositoryObjectType repObjType = TaCoKitUtil.getOrCreateERepositoryObjectType(configTypeNode);
        IProxyRepositoryFactory factory = ProxyRepositoryFactory.getInstance();
        List<Container<String, IRepositoryViewObject>> subContainers = container.getSubContainer();
        if (subContainers != null) {
            for (Container<String, IRepositoryViewObject> subContainer : subContainers) {
                try {
                    String folderName = subContainer.getLabel();
                    
                    Folder oFolder = new Folder((Property) subContainer.getProperty(), repObjType);
                    if (factory.getStatus(oFolder) != ERepositoryStatus.DELETED) {
                        TaCoKitFolderRepositoryNode folderNode = new TaCoKitFolderRepositoryNode(oFolder,
                                (RepositoryNode) parentNode, parentNode, folderName, configTypeNode);
                        if (!typeFolders.contains(folderName)) {
                            // ignore type folders, since they store sub types
                            parentNode.getChildren().add(folderNode);
                        }
                       
                        loadFromStorage(folderNode, allObjs, usedObjs, subContainer, tacokitRootContainer);
                        folderNode.setInitialized(true);
                    }
                } catch (Exception e) {
                    ExceptionHandler.process(e);
                }
            }
        }
        List<IRepositoryViewObject> members = container.getMembers();
        if (members != null) {
            ITaCoKitRepositoryNode parentLeafNode = null;
            ITaCoKitRepositoryNode testNode = parentNode;
            while (true) {
                if (testNode == null) {
                    break;
                }
                if (testNode.isFamilyNode()) {
                    break;
                }

                if (testNode.isLeafNode()) {
                    parentLeafNode = testNode;
                    break;
                }
                testNode = testNode.getParentTaCoKitNode();
            }
            String parentId = null;
            if (parentLeafNode != null) {
                parentId = parentLeafNode.getId();
            }
            for (IRepositoryViewObject member : members) {
                if (factory.getStatus(member) != ERepositoryStatus.DELETED) {
                    try {
                        allObjs.add(member);
                        ConnectionItem item = (ConnectionItem) member.getProperty().getItem();
                        TaCoKitConfigurationItemModel itemModule = new TaCoKitConfigurationItemModel(item);
                        TaCoKitConfigurationModel module = new TaCoKitConfigurationModel(item.getConnection());

                        String requiredParentId = module.getParentItemId();
                        if (requiredParentId == null || requiredParentId.isEmpty()) {
                            // if parent type is family, then create node; else continue
                            ConfigTypeNode moduleTypeNode = itemModule.getConfigTypeNode();
                            String mPId = moduleTypeNode.getParentId();
                            if (mPId != null) {
                                ConfigTypeNode parentTypeNode = configTypeNodeMap.get(mPId);
                                // if it's family node, then no parent anymore, else it's not a family node
                                if (parentTypeNode != null && !TaCoKitUtil.isBlank(parentTypeNode.getParentId())) {
                                    continue;
                                }
                            }
                        } else if (!requiredParentId.equals(parentId)) {
                            continue;
                        }
                        usedObjs.add(member);

                        TaCoKitLeafRepositoryNode leafRepositoryNode = createLeafRepositoryNode((RepositoryNode) parentNode,
                                parentNode, itemModule, configTypeNodeMap.get(module.getConfigurationId()), member);
                        parentNode.getChildren().add(leafRepositoryNode);
                        initTaCoKitNode(leafRepositoryNode, allObjs, usedObjs, tacokitRootContainer, true);
                        leafRepositoryNode.setInitialized(true);
                    } catch (Exception e) {
                        ExceptionHandler.process(e);
                    }
                }
            }
        }

    }

    private TaCoKitFamilyRepositoryNode createFamilyRepositoryNode(final RepositoryNode parentNode,
            final ConfigTypeNode tacokitFamilyNode) throws Exception {
        TaCoKitFamilyRepositoryNode familyRepositoryNode = new TaCoKitFamilyRepositoryNode(parentNode,
                tacokitFamilyNode.getDisplayName(), tacokitFamilyNode);
        return familyRepositoryNode;
    }

    private TaCoKitConfigurationRepositoryNode createConfigurationRepositoryNode(final RepositoryNode parentNode,
            final ITaCoKitRepositoryNode parentTaCoKitNode, final ConfigTypeNode configurationNode) throws Exception {
        TaCoKitConfigurationRepositoryNode configurationRepositoryNode = new TaCoKitConfigurationRepositoryNode(null, parentNode,
                parentTaCoKitNode, configurationNode.getDisplayName(), configurationNode); // $NON-NLS-1$
        return configurationRepositoryNode;
    }

    private TaCoKitLeafRepositoryNode createLeafRepositoryNode(final RepositoryNode parentNode,
            final ITaCoKitRepositoryNode parentTaCoKitNode, final TaCoKitConfigurationItemModel model,
            final ConfigTypeNode configurationTypeNode, final IRepositoryViewObject viewObject) throws Exception {
        TaCoKitLeafRepositoryNode leafNode = new TaCoKitLeafRepositoryNode(viewObject, parentNode, parentTaCoKitNode,
                model.getDisplayLabel(), configurationTypeNode);
        return leafNode;
    }

    @Override
    protected IPath getWorkspaceTopNodePath(final RepositoryNode topLevelNode) {
        IPath workspaceRelativePath = null;
        IProjectRepositoryNode root = topLevelNode.getRoot();
        if (root != null) {
            String projectName = root.getProject().getTechnicalLabel();
            if (projectName != null) {
                workspaceRelativePath = Path.fromPortableString('/' + projectName); // $NON-NLS-1$
            }
        }
        if (topLevelNode instanceof ITaCoKitRepositoryNode) {
            try {
                ERepositoryObjectType repObjType = TaCoKitUtil
                        .getOrCreateERepositoryObjectType(((ITaCoKitRepositoryNode) topLevelNode).getConfigTypeNode());
                workspaceRelativePath = workspaceRelativePath.append(repObjType.getFolder());
            } catch (Exception e) {
                ExceptionHandler.process(e);
            }
        }
        if (workspaceRelativePath == null) {
            return super.getWorkspaceTopNodePath(topLevelNode);
        }
        return workspaceRelativePath;
    }

    @Override
    public void clearCache() {
        super.clearCache();
        // Lookups.taCoKitCache().clearCache();

        if (familyNodesMapCache != null && !familyNodesMapCache.isEmpty()) {
            familyNodesMapCache.clear();
        }
    }

    @Override
    public void dispose() {
        super.dispose();
        clearCache();
    }

    @Override
    protected void addResourceVisitor(final CommonViewer v) {
        // super.addResourceVisitor(v);
        if (v == null) {
            return;
        }
        RepoViewCommonNavigator navigator = null;
        if (v instanceof RepoViewCommonViewer) {
            CommonNavigator commonNavigator = ((RepoViewCommonViewer) v).getCommonNavigator();
            if (commonNavigator instanceof RepoViewCommonNavigator) {
                navigator = ((RepoViewCommonNavigator) commonNavigator);
            }
        }
        if (navigator == null) {
            return;
        }
        if (this.testVisitor != null) {
            navigator.removeVisitor(this.testVisitor);
        }
        this.testVisitor = new MetadataTaCoKitChildrenNodeVisitor();
        navigator.addVisitor(this.testVisitor);

    }

    private final class MetadataTaCoKitChildrenNodeVisitor extends ResourceCollectorVisitor {

        @Override
        protected Set<RepositoryNode> getTopNodes() {
            return getTopLevelNodes();
        }

        @Override
        protected IPath getTopLevelNodePath(final RepositoryNode repoNode) {
            IPath topLevelNodeWorkspaceRelativePath = getProjectRelativePath(repoNode);
            ERepositoryObjectType repObjType = null;
            if (repoNode instanceof ITaCoKitRepositoryNode) {
                try {
                    repObjType = TaCoKitUtil
                            .getOrCreateERepositoryObjectType(((ITaCoKitRepositoryNode) repoNode).getConfigTypeNode());
                } catch (Exception e) {
                    ExceptionHandler.process(e);
                }
            } else {
                repObjType = repoNode.getContentType();
            }
            if (topLevelNodeWorkspaceRelativePath != null && repObjType != null) {
                topLevelNodeWorkspaceRelativePath = topLevelNodeWorkspaceRelativePath.append(repObjType.getFolder());
            }
            return topLevelNodeWorkspaceRelativePath;
        }

        private IPath getProjectRelativePath(final RepositoryNode repoNode) {
            IProjectRepositoryNode root = repoNode.getRoot();
            if (root != null) {
                String projectName = root.getProject().getTechnicalLabel();
                return Path.fromPortableString("/" + projectName); //$NON-NLS-1$
            }
            return null;
        }

        @Override
        protected IRepositoryNode getTopNodeFromResourceDelta(final IResourceDelta delta) {
            Set<RepositoryNode> topLevelNodes = getTopNodes();
            IPath rootPath = new Path(TaCoKitConst.METADATA_TACOKIT_PATH);
            IPath fullPath = delta.getFullPath();
            if (fullPath == null || fullPath.segmentCount() <= 1) {
                return ProjectRepositoryNode.getInstance();
            } else if (fullPath.removeFirstSegments(1).isPrefixOf(rootPath)) {
                return ProjectRepositoryNode.getInstance().getRootRepositoryNode(ERepositoryObjectType.METADATA);
            }
            for (final RepositoryNode repoNode : topLevelNodes) {
                IPath topLevelNodeWorkspaceRelativePath = getTopLevelNodePath(repoNode);
                if (topLevelNodeWorkspaceRelativePath != null && topLevelNodeWorkspaceRelativePath.isPrefixOf(fullPath)) {
                    return repoNode;
                }
            }

            // handle recyle of folder, since recycle status is only recorded in talend.project
            VisitResourceHelper visitHelper = new VisitResourceHelper(delta);
            boolean merged = ProjectRepositoryNode.getInstance().getMergeRefProject();
            IPath metadataPath = new Path(ERepositoryObjectType.METADATA.getFolder());
            if (metadataPath != null && visitHelper.valid(metadataPath, merged)) {
                return ProjectRepositoryNode.getInstance().getRootRepositoryNode(ERepositoryObjectType.METADATA);
            }
            // this visitor doesn't handle the current folder
            return null;
        }
    }
}
