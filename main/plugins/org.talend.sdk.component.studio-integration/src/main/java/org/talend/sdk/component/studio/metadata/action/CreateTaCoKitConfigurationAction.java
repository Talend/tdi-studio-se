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
package org.talend.sdk.component.studio.metadata.action;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;
import org.talend.commons.exception.ExceptionHandler;
import org.talend.commons.ui.runtime.image.EImage;
import org.talend.commons.ui.runtime.image.ImageProvider;
import org.talend.commons.utils.VersionUtils;
import org.talend.core.CorePlugin;
import org.talend.core.context.Context;
import org.talend.core.context.RepositoryContext;
import org.talend.core.model.metadata.builder.connection.Connection;
import org.talend.core.model.metadata.builder.connection.ConnectionFactory;
import org.talend.core.model.properties.ConnectionItem;
import org.talend.core.model.properties.PropertiesFactory;
import org.talend.core.model.properties.Property;
import org.talend.repository.model.RepositoryNode;
import org.talend.sdk.component.server.front.model.ConfigTypeNode;
import org.talend.sdk.component.studio.metadata.model.TaCoKitConfigurationModel;
import org.talend.sdk.component.studio.metadata.node.ITaCoKitRepositoryNode;
import org.talend.sdk.component.studio.ui.wizard.TaCoKitConfigurationRuntimeData;
import org.talend.sdk.component.studio.ui.wizard.TaCoKitCreateWizard;

/**
 * Metadata contextual action which creates WizardDialog used to create Component configuration
 * Some Repository nodes may have several create actions. E.g. Existing Datastore node may have 1 create action for
 * Dataset it may create.
 * Thus, this action is registered programmatically in NodeActionProvider class. Extension point creates only 1 action
 * for each registered extension class
 */
public class CreateTaCoKitConfigurationAction extends TaCoKitMetadataContextualAction {

    public CreateTaCoKitConfigurationAction(final ConfigTypeNode configTypeNode) {
        super();
        this.configTypeNode = configTypeNode;
        setImageDescriptor(ImageProvider.getImageDesc(EImage.ADD_ICON));
    }

    @Override
    public void init(final RepositoryNode node) {
        setRepositoryNode((ITaCoKitRepositoryNode) node);
        setText(getCreateLabel());
        setToolTipText(getEditLabel());
        Image nodeImage = getNodeImage();
        if (nodeImage != null) {
            this.setImageDescriptor(ImageDescriptor.createFromImage(nodeImage));
        }
        switch (node.getType()) {
        case STABLE_SYSTEM_FOLDER:
        case SIMPLE_FOLDER:
        case SYSTEM_FOLDER:
        case REPOSITORY_ELEMENT:
            if (isUserReadOnly() || !belongsToCurrentProject(node) || isDeleted(node)) {
                setEnabled(false);
                return;
            } else {
                collectChildNames(node);
                setEnabled(true);
            }
            break;
        default:
            return;
        }
    }

    @Override
    protected WizardDialog createWizardDialog() {
        try {
            IWizard wizard = createWizard(PlatformUI.getWorkbench());
            return new WizardDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), wizard);
        } catch (Exception e) {
            ExceptionHandler.process(e);
        }
        return null;
    }

    public TaCoKitCreateWizard createWizard(final IWorkbench wb) throws Exception {
        return new TaCoKitCreateWizard(wb, createRuntimeData());
    }

    private TaCoKitConfigurationRuntimeData createRuntimeData() throws Exception {
        TaCoKitConfigurationRuntimeData runtimeData = new TaCoKitConfigurationRuntimeData();
        runtimeData.setTaCoKitRepositoryNode(repositoryNode);
        runtimeData.setConfigTypeNode(configTypeNode);
        runtimeData.setCreation(true);
        runtimeData.setReadonly(false);
        runtimeData.setConnectionItem(createConnectionItem());
        return runtimeData;
    }

    private ConnectionItem createConnectionItem() throws Exception {
        Connection connection = ConnectionFactory.eINSTANCE.createConnection();

        Property property = PropertiesFactory.eINSTANCE.createProperty();
        property.setAuthor(
                ((RepositoryContext) CorePlugin.getContext().getProperty(Context.REPOSITORY_CONTEXT_KEY)).getUser());
        property.setVersion(VersionUtils.DEFAULT_VERSION);
        property.setStatusCode(""); //$NON-NLS-1$

        ConnectionItem connectionItem = PropertiesFactory.eINSTANCE.createConnectionItem();
        connectionItem.setConnection(connection);
        connectionItem.setProperty(property);
        connectionItem.setTypeName(configTypeNode.getId());

        TaCoKitConfigurationModel configurationModel = new TaCoKitConfigurationModel(connection, configTypeNode);
        String id = null;
        ITaCoKitRepositoryNode parentNode = repositoryNode;
        do {
            if (parentNode == null) {
                break;
            }
            if (parentNode.isLeafNode()) {
                id = parentNode.getObject().getId();
                break;
            }
            parentNode = parentNode.getParentTaCoKitNode();
        } while (true);
        if (id != null) {
            configurationModel.setParentItemId(id);
        }
        return connectionItem;
    }

}
