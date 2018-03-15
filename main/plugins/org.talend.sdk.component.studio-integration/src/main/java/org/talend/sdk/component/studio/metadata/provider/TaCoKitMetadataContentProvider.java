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
package org.talend.sdk.component.studio.metadata.provider;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.ui.navigator.CommonNavigator;
import org.eclipse.ui.navigator.CommonViewer;
import org.talend.commons.exception.ExceptionHandler;
import org.talend.commons.runtime.model.repository.ERepositoryStatus;
import org.talend.commons.ui.runtime.image.ECoreImage;
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
import org.talend.sdk.component.studio.compatibility.Compatibility;
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
    protected RepositoryNode
            getTopLevelNodeFromProjectRepositoryNode(final ProjectRepositoryNode projectRepositoryNode) {
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
        if (isRootNodeType(element)) {
            return getTopLevelNodes((ProjectRepositoryNode) RepositoryNode.class.cast(element).getRoot()).toArray();
        }
        if (element instanceof ITaCoKitRepositoryNode) {
            RepositoryNode repNode = (RepositoryNode) element;
            if (!repNode.isInitialized()) {
                try {
                    ITaCoKitRepositoryNode tacoNode = (ITaCoKitRepositoryNode) repNode;
                    tacoNode.getChildren().clear();
                    Map<String, IRepositoryViewObject> repoViewObjMap = new HashMap<>();
                    Map<String, ITaCoKitRepositoryNode> repoNodeMap = new HashMap<>();
                    Set<IRepositoryViewObject> visitedCollection = new HashSet<>();
                    IProjectRepositoryNode rootNode = repNode.getRoot();
                    RootContainer<String, IRepositoryViewObject> metadata =
                            ProxyRepositoryFactory.getInstance().getMetadata(rootNode.getProject(),
                                    TaCoKitUtil.getOrCreateERepositoryObjectType(tacoNode.getConfigTypeNode()), true);
                    getConfigurations(tacoNode, metadata, repoViewObjMap, repoNodeMap, visitedCollection, true);
                    repNode.setInitialized(true);
                    try {
                        List<IRepositoryNode> repNodes = new ArrayList<>();
                        repNodes.add(repNode);
                        Compatibility.collectRepositoryNodes(ProjectRepositoryNode.class.cast(rootNode), repNodes);
                    } catch (Exception e) {
                        ExceptionHandler.process(e);
                    }
                } catch (Exception e) {
                    ExceptionHandler.process(e);
                }
            }
            return ((RepositoryNode) element).getChildren().toArray();
        }
        return EMPTY_ARRAY;
    }

    @Override
    public Set<RepositoryNode> getTopLevelNodes() {
        final ProjectRepositoryNode rootNode = ProjectRepositoryNode.getInstance();
        return getTopLevelNodes(rootNode);
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
                }
                familyNodesCache = getTaCoKitFamilies(repoNode, true, false);
                if (familyNodesCache != null && !familyNodesCache.isEmpty()) {
                    try {
                        Compatibility.collectRepositoryNodes(rootNode, new ArrayList<>(familyNodesCache));
                    } catch (Exception e) {
                        ExceptionHandler.process(e);
                    }
                }
            }
            familyNodesMapCache.put(repoNode, familyNodesCache);
            return familyNodesCache;
        } catch (Exception e) {
            ExceptionHandler.process(e);
        }
        return Collections.EMPTY_SET;
    }

    private boolean isAllInitialized(final Set<RepositoryNode> repoNodes) {
        if (repoNodes == null || repoNodes.isEmpty()) {
            return false;
        }
        // for (RepositoryNode repoNode : repoNodes) {
        // if (!repoNode.isInitialized()) {
        // return false;
        // }
        // }
        return true;
    }

    private Object[] getConfigurations(final ITaCoKitRepositoryNode tacoNode,
            final Container<String, IRepositoryViewObject> itemObjs,
            final Map<String, IRepositoryViewObject> repoViewObjMap,
            final Map<String, ITaCoKitRepositoryNode> repoNodeMap, final Set<IRepositoryViewObject> visitedCollection,
            final boolean collectStorage) throws Exception {
        ConfigTypeNode configTypeNode = tacoNode.getConfigTypeNode();

        Set<String> edges = null;
        if (!tacoNode.isFolderNode() && !tacoNode.isLeafNode()) {
            edges = configTypeNode.getEdges();
        }
        Set<String> configFolderNames = new HashSet<>();
        if (edges != null && !edges.isEmpty()) {
            Map<String, ConfigTypeNode> nodes = Lookups.taCoKitCache().getConfigTypeNodeMap();
            for (String edge : edges) {
                ConfigTypeNode edgeNode = nodes.get(edge);
                String folderName = TaCoKitUtil.getTaCoKitFolderName(edgeNode);
                configFolderNames.add(folderName);
            }
        }

        // 1. create nodes from storage
        if (collectStorage && !tacoNode.isFamilyNode() && itemObjs != null) {
            IProxyRepositoryFactory factory = ProxyRepositoryFactory.getInstance();
            // 2.1 create folders and their children
            List<Container<String, IRepositoryViewObject>> subContainers = itemObjs.getSubContainer();
            if (subContainers != null && !subContainers.isEmpty()) {
                Iterator<Container<String, IRepositoryViewObject>> subContainerIter = subContainers.iterator();
                while (subContainerIter.hasNext()) {
                    Container<String, IRepositoryViewObject> subContainer = subContainerIter.next();
                    String label = subContainer.getLabel();
                    if (configFolderNames.contains(label)) {
                        continue;
                    }
                    Folder oFolder = new Folder((Property) subContainer.getProperty(), TaCoKitConst.METADATA_TACOKIT);
                    if (factory.getStatus(oFolder) != ERepositoryStatus.DELETED) {
                        ITaCoKitRepositoryNode parentTaCoKitNode = null;
                        if (tacoNode.isFolderNode()) {
                            parentTaCoKitNode = tacoNode.getParentTaCoKitNode();
                        } else if (tacoNode.isLeafNode()) {
                            parentTaCoKitNode = tacoNode;
                        } else {
                            parentTaCoKitNode = tacoNode;
                        }
                        TaCoKitFolderRepositoryNode folderNode = new TaCoKitFolderRepositoryNode(oFolder,
                                (RepositoryNode) tacoNode, parentTaCoKitNode, label, configTypeNode);
                        tacoNode.getChildren().add(folderNode);
                        getConfigurations(folderNode, subContainer, repoViewObjMap, repoNodeMap, visitedCollection,
                                collectStorage);
                        folderNode.setInitialized(true);
                    }
                }
            }
            // 2.2 create nodes
            List<IRepositoryViewObject> members = itemObjs.getMembers();
            if (members != null && !members.isEmpty()) {
                Iterator<IRepositoryViewObject> iter = members.iterator();
                while (iter.hasNext()) {
                    IRepositoryViewObject viewObject = iter.next();
                    repoViewObjMap.put(viewObject.getId(), viewObject);
                }
                iter = members.iterator();
                while (iter.hasNext()) {
                    IRepositoryViewObject viewObject = iter.next();
                    if (visitedCollection.contains(viewObject)) {
                        continue;
                    }
                    if (factory.getStatus(viewObject) != ERepositoryStatus.DELETED) {
                        try {
                            createLeafNode(tacoNode, viewObject, repoViewObjMap, repoNodeMap, visitedCollection);
                        } catch (Exception e) {
                            ExceptionHandler.process(e);
                        }
                    }
                    visitedCollection.add(viewObject);
                }
            }
        }

        // 2. create sub configuration
        if (edges != null && !edges.isEmpty()) {
            Map<String, ConfigTypeNode> nodes = Lookups.taCoKitCache().getConfigTypeNodeMap();
            for (String edge : edges) {
                ConfigTypeNode edgeNode = nodes.get(edge);
                TaCoKitConfigurationRepositoryNode configurationRepositoryNode =
                        createConfigurationRepositoryNode((RepositoryNode) tacoNode, tacoNode, edgeNode);
                tacoNode.getChildren().add(0, configurationRepositoryNode);
                String name = TaCoKitUtil.getTaCoKitFolderName(edgeNode);
                Container<String, IRepositoryViewObject> subContainer = null;
                if (itemObjs != null) {
                    subContainer = itemObjs.getSubContainer(name);
                }
                getConfigurations(configurationRepositoryNode, subContainer, repoViewObjMap, repoNodeMap,
                        visitedCollection, collectStorage);
                configurationRepositoryNode.setInitialized(collectStorage);
            }
        }
        return tacoNode.getChildren().toArray();
    }

    private void createLeafNode(final ITaCoKitRepositoryNode tacoNode, final IRepositoryViewObject viewObject,
            final Map<String, IRepositoryViewObject> repoViewObjMap,
            final Map<String, ITaCoKitRepositoryNode> repoNodeMap, final Set<IRepositoryViewObject> visitedCollection)
            throws Exception {
        if (visitedCollection.contains(viewObject)) {
            return;
        } else {
            visitedCollection.add(viewObject);
        }
        RepositoryNode parentNode = (RepositoryNode) tacoNode;
        ITaCoKitRepositoryNode parentTaCoKitNode = tacoNode;
        ConnectionItem item = (ConnectionItem) viewObject.getProperty().getItem();
        TaCoKitConfigurationItemModel itemModule = new TaCoKitConfigurationItemModel(item);
        TaCoKitConfigurationModel module = itemModule.getConfigurationModel();
        String parentItemId = module.getParentItemId();
        if (StringUtils.isNotEmpty(parentItemId)) {
            IRepositoryViewObject parentViewObj = repoViewObjMap.get(parentItemId);
            if (parentViewObj == null) {
                // maybe parentViewObj is deleted to recyclebin
                // throw new Exception("Can't find parent node: " + parentItemId);
                return;
            }
            parentTaCoKitNode = repoNodeMap.get(parentItemId);
            if (parentTaCoKitNode == null) {
                createLeafNode(tacoNode, parentViewObj, repoViewObjMap, repoNodeMap, visitedCollection);
                parentTaCoKitNode = repoNodeMap.get(parentItemId);
            }
            if (parentTaCoKitNode == null) {
                // not created, means don't need to create
                return;
            }
        }
        if (parentTaCoKitNode.isFolderNode()) {
            parentTaCoKitNode = tacoNode.getParentTaCoKitNode();
            if (!StringUtils.equals(module.getConfigurationId(), parentTaCoKitNode.getConfigTypeNode().getId())) {
                return;
            }
        } else if (parentTaCoKitNode.isLeafNode()) {
            if (!StringUtils.equals(module.getParentConfigurationId(), parentTaCoKitNode.getConfigTypeNode().getId())) {
                return;
            }
            parentNode = (RepositoryNode) parentTaCoKitNode;
        } else {
            if (!StringUtils.equals(module.getConfigurationId(), parentTaCoKitNode.getConfigTypeNode().getId())) {
                return;
            }
        }
        TaCoKitLeafRepositoryNode leafRepositoryNode = createLeafRepositoryNode(parentNode, parentTaCoKitNode,
                itemModule, Lookups.taCoKitCache().getConfigTypeNodeMap().get(module.getConfigurationId()), viewObject);
        parentNode.getChildren().add(leafRepositoryNode);
        leafRepositoryNode.setInitialized(true);
        repoNodeMap.put(leafRepositoryNode.getId(), leafRepositoryNode);
    }

    private Set<RepositoryNode> getTaCoKitFamilies(final RepositoryNode repositoryNode, final boolean createChildren,
            final boolean collectStorage) {
        try {
            Map<String, ConfigTypeNode> nodes = Lookups.taCoKitCache().getConfigTypeNodeMap();
            Set<RepositoryNode> familyNodes = new HashSet<>();
            if (nodes != null) {
                Map<String, IRepositoryViewObject> repoViewObjMap = new HashMap<>();
                Map<String, ITaCoKitRepositoryNode> repoNodeMap = new HashMap<>();
                Set<IRepositoryViewObject> visitedCollection = new HashSet<>();
                Collection<ConfigTypeNode> topLevelNodes = TaCoKitUtil.filterTopLevelNodes(nodes.values());
                for (ConfigTypeNode node : topLevelNodes) {
                    TaCoKitFamilyRepositoryNode familyRepositoryNode = createFamilyRepositoryNode(repositoryNode, node);
                    initilizeContentProviderWithTopLevelNode(familyRepositoryNode);
                    repositoryNode.getChildren().add(familyRepositoryNode);
                    familyNodes.add(familyRepositoryNode);
                    if (createChildren) {
                        String name = TaCoKitUtil.getTaCoKitFolderName(node);
                        Container<String, IRepositoryViewObject> subContainer = null;
                        if (collectStorage) {
                            // improve performance
                            RootContainer<String, IRepositoryViewObject> metadata = ProxyRepositoryFactory.getInstance()
                                    .getMetadata(repositoryNode.getRoot().getProject(), TaCoKitConst.METADATA_TACOKIT, true);
                            subContainer = metadata.getSubContainer(name);
                        }
                        getConfigurations(familyRepositoryNode, subContainer, repoViewObjMap, repoNodeMap,
                                visitedCollection, collectStorage);
                        familyRepositoryNode.setInitialized(collectStorage);
                    } else {
                        familyRepositoryNode.setInitialized(false);
                    }
                }
            }
            return familyNodes;
        } catch (Exception e) {
            ExceptionHandler.process(e);
        }
        return Collections.EMPTY_SET;
    }

    private TaCoKitFamilyRepositoryNode createFamilyRepositoryNode(final RepositoryNode parentNode,
            final ConfigTypeNode tacokitFamilyNode) throws Exception {
        TaCoKitFamilyRepositoryNode familyRepositoryNode = new TaCoKitFamilyRepositoryNode(parentNode,
                tacokitFamilyNode.getDisplayName(), ECoreImage.FOLDER_CLOSE_ICON, tacokitFamilyNode);
        familyRepositoryNode.setChildrenObjectType(TaCoKitUtil.getOrCreateERepositoryObjectType(tacokitFamilyNode));
        return familyRepositoryNode;
    }

    private TaCoKitConfigurationRepositoryNode createConfigurationRepositoryNode(final RepositoryNode parentNode,
            final ITaCoKitRepositoryNode parentTaCoKitNode, final ConfigTypeNode configurationNode) throws Exception {
        TaCoKitConfigurationRepositoryNode configurationRepositoryNode = new TaCoKitConfigurationRepositoryNode(null,
                parentNode, parentTaCoKitNode, configurationNode.getDisplayName(), configurationNode); // $NON-NLS-1$
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
        Lookups.taCoKitCache().clearCache();

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
                if (topLevelNodeWorkspaceRelativePath != null
                        && topLevelNodeWorkspaceRelativePath.isPrefixOf(fullPath)) {
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
