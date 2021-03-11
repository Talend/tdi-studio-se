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
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.talend.commons.ui.runtime.image.EImage;
import org.talend.commons.ui.runtime.image.ImageProvider;
import org.talend.commons.ui.swt.actions.ITreeContextualAction;
import org.talend.repository.view.di.metadata.action.MetedataNodeActionProvier;
import org.talend.sdk.component.server.front.model.ConfigTypeNode;
import org.talend.sdk.component.studio.Lookups;
import org.talend.sdk.component.studio.metadata.TaCoKitCache;
import org.talend.sdk.component.studio.metadata.action.CreateTaCoKitConfigurationAction;
import org.talend.sdk.component.studio.metadata.action.TaCoKitCreateFolderAction;
import org.talend.sdk.component.studio.metadata.node.ITaCoKitRepositoryNode;
import org.talend.sdk.component.studio.metadata.node.TaCoKitConfigurationRepositoryNode;
import org.talend.sdk.component.studio.util.TaCoKitUtil;

public class NodeActionProvider extends MetedataNodeActionProvier {

    private static final String EDITOR_ID = "org.talend.sdk.component.metadata.action.EditTaCoKitConfigurationAction"; //$NON-NLS-1$

    @Override
    public void fillContextMenu(final IMenuManager manager) {
        final IStructuredSelection sel = IStructuredSelection.class.cast(getContext().getSelection());
        if (1 < sel.size()) {
            super.fillContextMenu(manager);
            return;
        }
        final Object selObj = sel.getFirstElement();
        List<ITreeContextualAction> actions = new ArrayList<>();
        if (selObj instanceof ITaCoKitRepositoryNode) {
            ITaCoKitRepositoryNode tacokitNode = (ITaCoKitRepositoryNode) selObj;
            ConfigTypeNode configTypeNode = tacokitNode.getConfigTypeNode();
            if (configTypeNode != null) {
                if (tacokitNode.isFamilyNode() || tacokitNode.isLeafNode()) {
                    Set<String> edges = configTypeNode.getEdges();
                    if (edges != null && !edges.isEmpty()) {
                        TaCoKitCache cache = Lookups.taCoKitCache();
                        Map<String, ConfigTypeNode> configTypeNodeMap = cache.getConfigTypeNodeMap();
                        List<String> edgeArray = new LinkedList<String>(edges);
                        Collections.sort(edgeArray);
                        for (String edge : edgeArray) {
                            ConfigTypeNode subTypeNode = configTypeNodeMap.get(edge);
                            ITreeContextualAction createAction = new CreateTaCoKitConfigurationAction(subTypeNode);
                            createAction.init((TreeViewer) getActionSite().getStructuredViewer(), sel);
                            createAction.setImageDescriptor(ImageProvider.getImageDesc(EImage.ADD_ICON));
                            actions.add(createAction);
                        }
                        if (tacokitNode.isFamilyNode()) {
                            if (TaCoKitUtil.hideConfigFolderOnSingleEdge() && edgeArray.size() == 1) {
                                TaCoKitCreateFolderAction createFolderAction = new TaCoKitCreateFolderAction(
                                        configTypeNodeMap.get(edges.iterator().next()));
                                createFolderAction.init((TreeViewer) getActionSite().getStructuredViewer(), sel);
                                actions.add(createFolderAction);
                            }
                        }
                    }
                } else if (tacokitNode.isConfigNode() || tacokitNode.isFolderNode()) {
                    if (tacokitNode.isConfigNode() && ((TaCoKitConfigurationRepositoryNode) tacokitNode).isDeprecated()) {
                        return;
                    }
                    ITreeContextualAction createAction = new CreateTaCoKitConfigurationAction(configTypeNode);
                    createAction.init((TreeViewer) getActionSite().getStructuredViewer(), sel);
                    createAction.setImageDescriptor(ImageProvider.getImageDesc(EImage.ADD_ICON));
                    actions.add(createAction);
                }
            }
            // manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
        }
        super.fillContextMenu(manager);

        if (actions.isEmpty()) {
            return;
        }
        IContributionItem[] items = manager.getItems();
        boolean exist = existItem(items, EDITOR_ID);
        if (exist) {
            int length = actions.size();
            for (int i = length - 1; 0 <= i; --i) {
                manager.insertAfter(EDITOR_ID, actions.get(i));
            }
        } else {
            if (items.length <= 0) {
                actions.stream().forEach(a -> manager.add(a));
            } else {
                String id = items[0].getId();
                for (int i = 0; i < actions.size(); ++i) {
                    manager.insertBefore(id, actions.get(i));
                }
            }
        }
    }

    private boolean existItem(IContributionItem[] items, String id) {
        for (IContributionItem item : items) {
            if (TaCoKitUtil.equals(EDITOR_ID, item.getId())) {
                return true;
            }
        }
        return false;
    }
}
