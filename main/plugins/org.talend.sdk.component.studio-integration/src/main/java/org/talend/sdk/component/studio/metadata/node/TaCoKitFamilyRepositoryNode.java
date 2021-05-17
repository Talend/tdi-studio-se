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
package org.talend.sdk.component.studio.metadata.node;

import org.eclipse.swt.graphics.Image;
import org.talend.repository.model.RepositoryNode;
import org.talend.sdk.component.server.front.model.ConfigTypeNode;

/**
 * DOC cmeng class global comment. Detailled comment
 */
public class TaCoKitFamilyRepositoryNode extends AbsTaCoKitRepositoryNode {

    public TaCoKitFamilyRepositoryNode(final RepositoryNode parent, final String label, final ConfigTypeNode configTypeNode)
            throws Exception {
        super(null, parent, null, label, null, configTypeNode);
        this.setImage(getTaCoKitImage(configTypeNode));
        this.setType(ENodeType.STABLE_SYSTEM_FOLDER);
    }

    @Override
    protected Image buildTaCoKitImage(final String id) throws Exception {
        byte[] icon = requestFamilyIcon(id);
        return buildTaCoKitImage(icon);
    }

    @Override
    public boolean isLeafNode() {
        return false;
    }

    @Override
    public boolean isFolderNode() {
        return false;
    }

    @Override
    public boolean isFamilyNode() {
        return true;
    }

    @Override
    public boolean isConfigNode() {
        return false;
    }

    // todo when studio is redeployed @Override
    @Override
    public boolean shouldCollectRepositoryNode() {
        return true;
    }

    @Override
    public boolean equals(final Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof TaCoKitFamilyRepositoryNode)) {
            return false;
        }
        final TaCoKitFamilyRepositoryNode other = (TaCoKitFamilyRepositoryNode) o;
        if (!other.canEqual(this)) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }
        final Object this$configTypeNode = this.getConfigTypeNode();
        final Object other$configTypeNode = other.getConfigTypeNode();
        if (this$configTypeNode == null ? other$configTypeNode != null : !this$configTypeNode.equals(other$configTypeNode)) {
            return false;
        }
        return true;
    }

    @Override
    protected boolean canEqual(final Object other) {
        return other instanceof TaCoKitFamilyRepositoryNode;
    }

    @Override
    public int hashCode() {
        final int PRIME = 59;
        int result = super.hashCode();
        final Object $configTypeNode = this.getConfigTypeNode();
        result = result * PRIME + ($configTypeNode == null ? 43 : $configTypeNode.hashCode());
        return result;
    }
}
