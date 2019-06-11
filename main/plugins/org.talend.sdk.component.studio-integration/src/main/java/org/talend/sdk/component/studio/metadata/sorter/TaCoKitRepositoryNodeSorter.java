/**
 * Copyright (C) 2006-2019 Talend Inc. - www.talend.com
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
package org.talend.sdk.component.studio.metadata.sorter;

import org.talend.repository.model.RepositoryNode;
import org.talend.repository.view.sorter.RepositoryNodeCompareSorter;
import org.talend.sdk.component.studio.metadata.node.ITaCoKitRepositoryNode;

public class TaCoKitRepositoryNodeSorter extends RepositoryNodeCompareSorter {

    @Override
    protected void sortChildren(final RepositoryNode parent, final Object[] children) {
        if (parent instanceof ITaCoKitRepositoryNode) {
            sortChildren(children);
        }
    }

    @Override
    protected int compareNode(final RepositoryNode n1, final RepositoryNode n2) {
        if (n1 instanceof ITaCoKitRepositoryNode && n2 instanceof ITaCoKitRepositoryNode) {
            ITaCoKitRepositoryNode tacokitNode1 = (ITaCoKitRepositoryNode) n1;
            ITaCoKitRepositoryNode tacokitNode2 = (ITaCoKitRepositoryNode) n2;
            if (tacokitNode1.isFamilyNode() || tacokitNode2.isFamilyNode()) {
                if (tacokitNode1.isFamilyNode() && tacokitNode2.isFamilyNode()) {
                    return tacokitNode1.getLabel().compareTo(tacokitNode2.getLabel());
                }
                if (tacokitNode1.isFamilyNode()) {
                    return -1;
                }
                return 1;
            }
            if (tacokitNode1.isConfigNode() || tacokitNode2.isConfigNode()) {
                if (tacokitNode1.isConfigNode() && tacokitNode2.isConfigNode()) {
                    return tacokitNode1.getLabel().compareTo(tacokitNode2.getLabel());
                }
                if (tacokitNode1.isConfigNode()) {
                    return -1;
                }
                return 1;
            }
            if (tacokitNode1.isFolderNode() || tacokitNode2.isFolderNode()) {
                if (tacokitNode1.isFolderNode() && tacokitNode2.isFolderNode()) {
                    return tacokitNode1.getLabel().compareTo(tacokitNode2.getLabel());
                }
                if (tacokitNode1.isFolderNode()) {
                    return -1;
                }
                return 1;
            }

        }
        if (n1 != null && n2 != null) {
            return n1.getLabel().compareTo(n2.getLabel());
        }
        return 0;
    }

}
