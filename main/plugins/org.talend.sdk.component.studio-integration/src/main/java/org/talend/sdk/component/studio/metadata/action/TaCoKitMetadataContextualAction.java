/**
 * Copyright (C) 2006-2021 Talend Inc. - www.talend.com
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
package org.talend.sdk.component.studio.metadata.action;

import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.graphics.Image;
import org.talend.core.model.properties.ConnectionItem;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.core.repository.ui.actions.metadata.AbstractCreateAction;
import org.talend.repository.ProjectManager;
import org.talend.repository.model.RepositoryNode;
import org.talend.repository.ui.views.IRepositoryView;
import org.talend.sdk.component.server.front.model.ConfigTypeNode;
import org.talend.sdk.component.studio.i18n.Messages;
import org.talend.sdk.component.studio.metadata.node.ITaCoKitRepositoryNode;

/**
 * Base class for TaCoKit Metadata contextual actions.
 * Contextual action is action which may be launched from context menu (it appears, when user clicks right mouse
 * button).
 * Metadata is part of Studio Repository. Metadata stores Component configuration, in particular for Datastores and
 * Datasets.
 * Create Datastore/Dataset and edit Datastore/Dataset actions should be available for Tacokit component families.
 * Component family potentially may have several different types of Datasets. E.g. Azure family has blob, queue and
 * table Datasets.
 */
public abstract class TaCoKitMetadataContextualAction extends AbstractCreateAction {

    private static final int DEFAULT_WIZARD_WIDTH = 700;

    private static final int DEFAULT_WIZARD_HEIGHT = 400;

    protected ITaCoKitRepositoryNode repositoryNode;

    protected ConfigTypeNode configTypeNode;

    private boolean isReadonly;

    /**
     * Creates {@link WizardDialog}, opens it and refreshes repository node if result is ok
     */
    @Override
    protected void doRun() {
        WizardDialog wizardDialog = createWizardDialog();
        openWizardDialog(wizardDialog);
    }

    protected abstract WizardDialog createWizardDialog();

    private void openWizardDialog(final WizardDialog wizardDialog) {
        if (Platform.getOS().equals(Platform.OS_LINUX)) {
            wizardDialog.setPageSize(getWizardWidth(), getWizardHeight() + 80);
        }
        wizardDialog.create();
        int result = wizardDialog.open();
        if (result == WizardDialog.OK) {
            IRepositoryView viewPart = getViewPart();
            if (viewPart != null) {
                viewPart.setFocus();
                refresh(repositoryNode);
            }
        }
    }

    protected int getWizardWidth() {
        return DEFAULT_WIZARD_WIDTH;
    }

    protected int getWizardHeight() {
        return DEFAULT_WIZARD_HEIGHT;
    }

    protected String getCreateLabel() {
        return Messages.getString("TaCoKitConfiguration.action.create.Label", configTypeNode.getDisplayName(), //$NON-NLS-1$
                configTypeNode.getConfigurationType());
    }

    protected String getEditLabel() {
        return Messages.getString("TaCoKitConfiguration.action.edit.Label", configTypeNode.getDisplayName(), //$NON-NLS-1$
                configTypeNode.getConfigurationType());
    }

    protected String getOpenLabel() {
        return Messages.getString("TaCoKitConfiguration.action.open.Label", configTypeNode.getDisplayName(), //$NON-NLS-1$
                configTypeNode.getConfigurationType());
    }

    protected String getNodeLabel() {
        return repositoryNode.getDisplayText();
    }

    @Override
    public Class getClassForDoubleClick() {
        return ConnectionItem.class;
    }

    /**
     * TODO implement it
     *
     * Returns image shown near contextual menu item name. It should be family icon
     *
     *
     *
     * @return metadata contextual action image
     */
    protected Image getNodeImage() {
        return null;
    }

    /**
     * Checks whether user has only read permission. If it is true, user can't create or edit repository node,
     *
     * so action should be disabled for him
     *
     *
     *
     * @return true, is user has ReadOnly rights
     */
    protected boolean isUserReadOnly() {
        return ProxyRepositoryFactory.getInstance().isUserReadOnlyOnCurrentProject();
    }

    /**
     * Checks whether repository node belongs to current project. If it doesn't, then action should be disabled
     *
     *
     *
     * @param node repository node
     *
     * @return true, it node belongs to current project
     */
    protected boolean belongsToCurrentProject(final RepositoryNode node) {
        return ProjectManager.getInstance().isInCurrentMainProject(node);
    }

    /**
     * Checks whether node is deleted. If node is deleted create action should be disabled
     *
     *
     *
     * @param node repository node
     *
     * @return true, if it is deleted
     */
    protected boolean isDeleted(final RepositoryNode node) {
        return node.getObject() != null && node.getObject().getProperty().getItem().getState().isDeleted();
    }

    public boolean isReadonly() {
        return this.isReadonly;
    }

    public void setRepositoryNode(final ITaCoKitRepositoryNode repositoryNode) {
        this.repositoryNode = repositoryNode;
    }

    public void setConfigTypeNode(final ConfigTypeNode configTypeNode) {
        this.configTypeNode = configTypeNode;
    }

    public void setReadonly(final boolean isReadonly) {
        this.isReadonly = isReadonly;
    }
}
