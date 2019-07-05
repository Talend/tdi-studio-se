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
import org.talend.sdk.component.server.front.model.ConfigTypeNode;
import org.talend.sdk.component.studio.GAV;
import org.talend.sdk.component.studio.i18n.Messages;
import org.talend.sdk.component.studio.model.update.TaCoKitUpdateManager;

/**
 * Wizard which is called on Edit action
 */
public class TaCoKitEditWizard extends TaCoKitConfigurationWizard {

    @Override protected boolean isNew() {
        return false;
    }

    public TaCoKitEditWizard(final IWorkbench workbench, final TaCoKitConfigurationRuntimeData runtimeData) {
        super(workbench, runtimeData);
    }

    /**
     * Part of constructor
     * Sets window title depending on whether it is {@code creation} wizard or editing
     */
    @Override
    protected void setWindowTitle() {
        ConfigTypeNode configTypeNode = getRuntimeData().getConfigTypeNode();
        setWindowTitle(
                Messages.getString("TaCoKitConfiguration.wizard.title.edit.str", configTypeNode.getDisplayName(), //$NON-NLS-1$
                        configTypeNode.getConfigurationType()));
    }

    /**
     * Creates operation, which is performed, when Finish button is pushed.
     * Creates different operations depending on whether it is Create or Edit wizard
     *
     * @return operation to perform on finish
     */
    @Override
    protected IWorkspaceRunnable createFinishOperation() {
        return monitor -> {
            try {
                updateConfigurationItem();
            } catch (Exception e) {
                throw new CoreException(new Status(IStatus.ERROR, GAV.INSTANCE.getArtifactId(), e.getMessage(), e));
            }
        };
    }

    private void updateConfigurationItem() throws Exception {
        updateConnectionItem();
        refreshInFinish(getWizardPropertiesPage().isNameModifiedByUser());
        TaCoKitUpdateManager.updateTaCoKitSubConnection(getRuntimeData());
        TaCoKitUpdateManager.updateTaCoKitConnection(connectionItem);
    }

}
