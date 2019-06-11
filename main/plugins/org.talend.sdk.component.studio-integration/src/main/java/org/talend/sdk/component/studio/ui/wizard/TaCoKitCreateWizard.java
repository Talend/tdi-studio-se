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
package org.talend.sdk.component.studio.ui.wizard;

import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.IWorkbench;
import org.talend.core.model.properties.ConnectionItem;
import org.talend.core.model.repository.RepositoryManager;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.repository.model.IProxyRepositoryFactory;
import org.talend.sdk.component.server.front.model.ConfigTypeNode;
import org.talend.sdk.component.studio.GAV;
import org.talend.sdk.component.studio.i18n.Messages;
import org.talend.sdk.component.studio.metadata.model.TaCoKitConfigurationModel;
import org.talend.sdk.component.studio.metadata.node.ITaCoKitRepositoryNode;
import org.talend.sdk.component.studio.util.TaCoKitConst;

/**
 * Wizard which is called on Create action
 */
public class TaCoKitCreateWizard extends TaCoKitConfigurationWizard {

    public TaCoKitCreateWizard(final IWorkbench workbench, final TaCoKitConfigurationRuntimeData runtimeData) {
        super(workbench, runtimeData);
    }

    @Override protected boolean isNew() {
        return true;
    }

    /**
     * Part of constructor
     * Sets window title depending on whether it is {@code creation} wizard or editing
     */
    @Override
    protected void setWindowTitle() {
        ConfigTypeNode configTypeNode = getRuntimeData().getConfigTypeNode();
        setWindowTitle(Messages.getString("TaCoKitConfiguration.wizard.title.create.str", //$NON-NLS-1$
                configTypeNode.getDisplayName(), configTypeNode.getConfigurationType()));
    }

    /**
     * Creates operation, which is performed, when Finish button is pushed.
     * This operation creates ConfigurationItem
     *
     * @return operation to perform on finish
     */
    @Override
    protected IWorkspaceRunnable createFinishOperation() {
        return monitor -> {
            try {
                createConfigurationItem();
            } catch (Exception e) {
                throw new CoreException(new Status(IStatus.ERROR, GAV.INSTANCE.getArtifactId(), e.getMessage(), e));
            }
        };
    }

    private void createConfigurationItem() throws Exception {
        IProxyRepositoryFactory factory = ProxyRepositoryFactory.getInstance();
        String nextId = factory.getNextId();
        ITaCoKitRepositoryNode taCoKitRepositoryNode = getRuntimeData().getTaCoKitRepositoryNode();
        ConfigTypeNode configTypeNode = getRuntimeData().getConfigTypeNode();
        ConnectionItem connectionItem = getRuntimeData().getConnectionItem();

        connectionItem.getProperty().setId(nextId);
        TaCoKitConfigurationModel model = new TaCoKitConfigurationModel(connectionItem.getConnection(), configTypeNode);
        ITaCoKitRepositoryNode parentNode = null;
        ITaCoKitRepositoryNode tempNode = taCoKitRepositoryNode;
        while (true) {
            if (tempNode == null) {
                break;
            }
            if (tempNode.isLeafNode()) {
                parentNode = tempNode;
                break;
            }
            if (tempNode.isFamilyNode()) {
                break;
            }
            tempNode = tempNode.getParentTaCoKitNode();
        }
        if (parentNode != null) {
            model.setParentItemId(parentNode.getObject().getId());
        }
        factory.create(connectionItem, getWizardPropertiesPage().getDestinationPath());
        RepositoryManager.refreshCreatedNode(TaCoKitConst.METADATA_TACOKIT);
        // RepositoryUpdateManager.updateFileConnection(connectionItem);
    }
}
