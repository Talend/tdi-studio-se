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
package org.talend.sdk.component.studio.metadata.seeker;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.TreeViewer;
import org.talend.commons.exception.ExceptionHandler;
import org.talend.commons.exception.PersistenceException;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.model.repository.IRepositoryViewObject;
import org.talend.core.repository.model.ProjectRepositoryNode;
import org.talend.core.runtime.CoreRuntimePlugin;
import org.talend.repository.metadata.seeker.AbstractMetadataRepoViewSeeker;
import org.talend.repository.model.IRepositoryNode;
import org.talend.repository.model.IRepositoryNode.EProperties;
import org.talend.repository.model.RepositoryNode;
import org.talend.repository.model.nodes.IProjectRepositoryNode;
import org.talend.sdk.component.studio.metadata.node.ITaCoKitRepositoryNode;
import org.talend.sdk.component.studio.metadata.provider.TaCoKitMetadataContentProvider;
import org.talend.sdk.component.studio.util.TaCoKitUtil;

/**
 * DOC cmeng class global comment. Detailled comment
 */
public class TaCoKitMetadataRepoViewSeeker extends AbstractMetadataRepoViewSeeker {

    @Override
    protected List<ERepositoryObjectType> getValidationTypes() {
        throw new RuntimeException(
                "Normally this method is not used since it is only used by method validType(...), please implement it if needed in someday."); //$NON-NLS-1$
    }

    @Override
    protected boolean validType(final ERepositoryObjectType itemType) {
        return TaCoKitUtil.isTaCoKitType(itemType);
    }

    @Override
    protected List<IRepositoryNode> getRootTypeRepositoryNodes(final IProjectRepositoryNode root,
            final ERepositoryObjectType itemType) {
        List<IRepositoryNode> rootTypeNodes = new ArrayList<IRepositoryNode>();
        if (root != null && itemType != null) {
            List<IRepositoryNode> findedTaCoKitTypeNodes = findTaCoKitTypeNodes(root);
            if (findedTaCoKitTypeNodes != null && !findedTaCoKitTypeNodes.isEmpty()) {
                rootTypeNodes.addAll(findedTaCoKitTypeNodes);
            }
            IRepositoryNode referenceProjectNode =
                    root.getRootRepositoryNode(ERepositoryObjectType.REFERENCED_PROJECTS, true);
            if (referenceProjectNode != null) {
                List<IRepositoryNode> refProjects = referenceProjectNode.getChildren();
                if (refProjects != null && !refProjects.isEmpty()) {
                    for (IRepositoryNode repositoryNode : refProjects) {
                        if (repositoryNode instanceof IProjectRepositoryNode) {
                            IProjectRepositoryNode refProjectNode = (IProjectRepositoryNode) repositoryNode;
                            List<IRepositoryNode> findedRefTaCoKitTypeNodes = findTaCoKitTypeNodes(refProjectNode);
                            if (findedRefTaCoKitTypeNodes != null && !findedRefTaCoKitTypeNodes.isEmpty()) {
                                rootTypeNodes.addAll(findedRefTaCoKitTypeNodes);
                            }
                        }
                    }
                }
            }
        }
        return rootTypeNodes;
    }

    private List<IRepositoryNode> findTaCoKitTypeNodes(final IProjectRepositoryNode root) {
        IRepositoryNode rootTypeRepoNode = root.getRootRepositoryNode(ERepositoryObjectType.METADATA);
        List<IRepositoryNode> children = rootTypeRepoNode.getChildren();
        List<IRepositoryNode> findedNodes = new ArrayList<>();
        for (IRepositoryNode child : children) {
            Object repoObjType = child.getProperties(EProperties.CONTENT_TYPE);
            if (repoObjType instanceof ERepositoryObjectType
                    && TaCoKitUtil.isTaCoKitType((ERepositoryObjectType) repoObjType)) {
                findedNodes.add(child);
            }
        }
        return findedNodes;
    }

    @Override
    public IRepositoryNode searchNode(final TreeViewer viewer, final String itemId) {
        if (itemId != null) {
            try {
                IRepositoryViewObject lastVersion =
                        CoreRuntimePlugin.getInstance().getProxyRepositoryFactory().getLastVersion(itemId);
                if (lastVersion != null) {
                    final ERepositoryObjectType itemType = lastVersion.getRepositoryObjectType();
                    if (validType(itemType)) {
                        TaCoKitMetadataContentProvider cp = new TaCoKitMetadataContentProvider();
                        cp.getTopLevelNodes();
                        ProjectRepositoryNode projectRepositoryNode = ProjectRepositoryNode.getInstance();
                        List<IRepositoryNode> rootTypeRepoNodes =
                                getRootTypeRepositoryNodes(projectRepositoryNode, itemType);
                        for (IRepositoryNode rootNode : rootTypeRepoNodes) {
                            if (rootNode instanceof RepositoryNode) {
                                if (!((RepositoryNode) rootNode).isInitialized()) {
                                    if (cp.hasChildren(rootNode)) {
                                        cp.getChildren(rootNode);
                                    }
                                }
                            }
                            IRepositoryNode searchedRepoNode = searchRepositoryNode(rootNode, itemId, itemType);
                            // in fact, will search the main project first.
                            if (searchedRepoNode != null) {
                                cp.clearCache();
                                return searchedRepoNode;
                            }

                        }
                    }
                }
            } catch (PersistenceException e) {
                ExceptionHandler.process(e);
            }
        }
        return null;
    }

    @Override
    protected IRepositoryNode searchRepositoryNode(IRepositoryNode rootNode, String itemId,
            final ERepositoryObjectType itemType) {
        if (rootNode != null && itemId != null && itemType != null) {
            // in the first, search the current folder/child is leaf
            List<IRepositoryNode> searchChildren = new ArrayList<IRepositoryNode>();

            List<IRepositoryNode> children = rootNode.getChildren();
            for (IRepositoryNode childNode : children) {
                if (isRepositoryFolder(childNode)) {
                    searchChildren.add(childNode);
                } else if (validNode(childNode, itemId, itemType)) {
                    return childNode;
                } else if (childNode instanceof ITaCoKitRepositoryNode && ((ITaCoKitRepositoryNode) childNode).isLeafNode()
                        && ((ITaCoKitRepositoryNode) childNode).hasChildren()) {
                    searchChildren.add(childNode);
                }
            }
            // search in the folders/child is leaf
            for (IRepositoryNode childNode : searchChildren) {
                IRepositoryNode searchedRepoNode = searchRepositoryNode(childNode, itemId, itemType);
                if (searchedRepoNode != null) {
                    return searchedRepoNode;
                }
            }
        }
        return null;
    }

}
