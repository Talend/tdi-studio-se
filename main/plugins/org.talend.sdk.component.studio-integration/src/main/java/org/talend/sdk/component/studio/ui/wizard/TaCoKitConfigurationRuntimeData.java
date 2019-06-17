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
package org.talend.sdk.component.studio.ui.wizard;

import java.util.LinkedHashSet;
import java.util.Set;

import org.talend.core.model.properties.ConnectionItem;
import org.talend.sdk.component.server.front.model.ConfigTypeNode;
import org.talend.sdk.component.studio.metadata.node.ITaCoKitRepositoryNode;
import org.talend.sdk.component.studio.ui.composite.problemmanager.WizardProblemManager;

public class TaCoKitConfigurationRuntimeData {

    private ITaCoKitRepositoryNode taCoKitRepositoryNode;

    private ConnectionItem connectionItem;

    private ConfigTypeNode configTypeNode;

    private Set<WizardProblemManager> problemManagers;

    private boolean isCreation = true;

    private boolean isReadonly = false;

    private boolean isAddContextFields = false;

    private String[] existingNames;

    public TaCoKitConfigurationRuntimeData() {
        problemManagers = new LinkedHashSet<>();
    }

    public ITaCoKitRepositoryNode getTaCoKitRepositoryNode() {
        return this.taCoKitRepositoryNode;
    }

    public ConnectionItem getConnectionItem() {
        return this.connectionItem;
    }

    public ConfigTypeNode getConfigTypeNode() {
        return this.configTypeNode;
    }

    public boolean isCreation() {
        return this.isCreation;
    }

    public boolean isReadonly() {
        return this.isReadonly;
    }

    public boolean isAddContextFields() {
        return this.isAddContextFields;
    }

    public String[] getExistingNames() {
        return this.existingNames;
    }

    public void setTaCoKitRepositoryNode(final ITaCoKitRepositoryNode taCoKitRepositoryNode) {
        this.taCoKitRepositoryNode = taCoKitRepositoryNode;
    }

    public void setConnectionItem(final ConnectionItem connectionItem) {
        this.connectionItem = connectionItem;
    }

    public void setConfigTypeNode(final ConfigTypeNode configTypeNode) {
        this.configTypeNode = configTypeNode;
    }

    public void setCreation(final boolean isCreation) {
        this.isCreation = isCreation;
    }

    public void setReadonly(final boolean isReadonly) {
        this.isReadonly = isReadonly;
    }

    public void setAddContextFields(final boolean isAddContextFields) {
        this.isAddContextFields = isAddContextFields;
    }

    public void setExistingNames(final String[] existingNames) {
        this.existingNames = existingNames;
    }

    public Set<WizardProblemManager> getProblemManagers() {
        return this.problemManagers;
    }

    public void registProblemManager(WizardProblemManager problemManager) {
        this.problemManagers.add(problemManager);
    }

    public void unregistProblemManager(WizardProblemManager problemManager) {
        this.problemManagers.remove(problemManager);
    }

    @Override
    public boolean equals(final Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof TaCoKitConfigurationRuntimeData)) {
            return false;
        }
        final TaCoKitConfigurationRuntimeData other = (TaCoKitConfigurationRuntimeData) o;
        if (!other.canEqual(this)) {
            return false;
        }
        final Object this$taCoKitRepositoryNode = this.getTaCoKitRepositoryNode();
        final Object other$taCoKitRepositoryNode = other.getTaCoKitRepositoryNode();
        if (this$taCoKitRepositoryNode == null ? other$taCoKitRepositoryNode != null
                : !this$taCoKitRepositoryNode.equals(other$taCoKitRepositoryNode)) {
            return false;
        }
        final Object this$connectionItem = this.getConnectionItem();
        final Object other$connectionItem = other.getConnectionItem();
        if (this$connectionItem == null ? other$connectionItem != null : !this$connectionItem.equals(other$connectionItem)) {
            return false;
        }
        final Object this$configTypeNode = this.getConfigTypeNode();
        final Object other$configTypeNode = other.getConfigTypeNode();
        if (this$configTypeNode == null ? other$configTypeNode != null : !this$configTypeNode.equals(other$configTypeNode)) {
            return false;
        }
        if (this.isCreation() != other.isCreation()) {
            return false;
        }
        if (this.isReadonly() != other.isReadonly()) {
            return false;
        }
        if (this.isAddContextFields() != other.isAddContextFields()) {
            return false;
        }
        if (!java.util.Arrays.deepEquals(this.getExistingNames(), other.getExistingNames())) {
            return false;
        }
        return true;
    }

    protected boolean canEqual(final Object other) {
        return other instanceof TaCoKitConfigurationRuntimeData;
    }

    @Override
    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final Object $taCoKitRepositoryNode = this.getTaCoKitRepositoryNode();
        result = result * PRIME + ($taCoKitRepositoryNode == null ? 43 : $taCoKitRepositoryNode.hashCode());
        final Object $connectionItem = this.getConnectionItem();
        result = result * PRIME + ($connectionItem == null ? 43 : $connectionItem.hashCode());
        final Object $configTypeNode = this.getConfigTypeNode();
        result = result * PRIME + ($configTypeNode == null ? 43 : $configTypeNode.hashCode());
        result = result * PRIME + (this.isCreation() ? 79 : 97);
        result = result * PRIME + (this.isReadonly() ? 79 : 97);
        result = result * PRIME + (this.isAddContextFields() ? 79 : 97);
        result = result * PRIME + java.util.Arrays.deepHashCode(this.getExistingNames());
        return result;
    }

    @Override
    public String toString() {
        return "TaCoKitConfigurationRuntimeData(taCoKitRepositoryNode=" + this.getTaCoKitRepositoryNode() + ", connectionItem="
                + this.getConnectionItem() + ", configTypeNode=" + this.getConfigTypeNode() + ", isCreation=" + this.isCreation()
                + ", isReadonly=" + this.isReadonly() + ", isAddContextFields=" + this.isAddContextFields() + ", existingNames="
                + java.util.Arrays.deepToString(this.getExistingNames()) + ")";
    }
}
