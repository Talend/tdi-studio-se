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

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.ui.IWorkbench;
import org.talend.commons.exception.ExceptionHandler;
import org.talend.commons.ui.runtime.exception.ExceptionMessageDialog;
import org.talend.core.model.properties.ConnectionItem;
import org.talend.metadata.managment.ui.wizard.CheckLastVersionRepositoryWizard;
import org.talend.metadata.managment.ui.wizard.metadata.connection.Step0WizardPage;
import org.talend.repository.model.IRepositoryNode.ENodeType;
import org.talend.repository.model.RepositoryNodeUtilities;
import org.talend.sdk.component.server.front.model.ConfigTypeNode;
import org.talend.sdk.component.studio.i18n.Messages;
import org.talend.sdk.component.studio.metadata.node.ITaCoKitRepositoryNode;
import org.talend.sdk.component.studio.model.parameter.Metadatas;
import org.talend.sdk.component.studio.model.parameter.PropertyNode;
import org.talend.sdk.component.studio.model.parameter.PropertyTreeCreator;
import org.talend.sdk.component.studio.ui.wizard.page.TaCoKitConfigurationWizardPage;
import org.talend.sdk.component.studio.ui.wizard.page.TaCoKitPageBuildHelper;
import org.talend.sdk.component.studio.ui.wizard.page.WizardTypeMapper;
import org.talend.sdk.component.studio.util.TaCoKitConst;

/**
 * Base class for Repository Metadata wizards
 */
public abstract class TaCoKitConfigurationWizard extends CheckLastVersionRepositoryWizard {

    private final TaCoKitConfigurationRuntimeData runtimeData;

    private Step0WizardPage wizardPropertiesPage;

    private TaCoKitConfigurationWizardPage mainPage;

    private TaCoKitConfigurationWizardPage advancedPage;

    protected abstract boolean isNew();

    public TaCoKitConfigurationWizard(final IWorkbench workbench, final TaCoKitConfigurationRuntimeData runtimeData) {
        super(workbench, runtimeData.isCreation(), runtimeData.isReadonly());
        this.runtimeData = runtimeData;
        this.existingNames = runtimeData.getExistingNames();
        creation = runtimeData.isCreation();
        connectionItem = runtimeData.getConnectionItem();
        this.runtimeData.setReadonly(runtimeData.isReadonly() || !isRepositoryObjectEditable());
        init();
    }

    /**
     * Part of constructor
     */
    private void init() {
        setPathToSave();
        lockNode();
        setWindowTitle();
        setHelpAvailable(false);
        // setRepositoryLocation(wizard, location, connectionItem.getProperty().getId());
    }

    /**
     * Part of constructor
     *
     * Sets {@code pathToSave} if repository node has one of following types:
     *
     * <ul>
     *
     * <li>SIMPLE_FOLDER</li>
     *
     * <li>SYSTEM_FOLDER</li>
     *
     * <li>REPOSITORY_ELEMENT</li>
     *
     * </ul>
     */
    private void setPathToSave() {
        ITaCoKitRepositoryNode repositoryNode = runtimeData.getTaCoKitRepositoryNode();
        ENodeType nodeType = repositoryNode.getType();
        switch (nodeType) {
        case STABLE_SYSTEM_FOLDER:

        case SIMPLE_FOLDER:

        case REPOSITORY_ELEMENT:
            if (repositoryNode.isLeafNode()) {
                pathToSave = new Path(""); //$NON-NLS-1$
            } else {
                pathToSave = RepositoryNodeUtilities.getPath(repositoryNode);
            }
            break;

        case SYSTEM_FOLDER:
            pathToSave = new Path(""); //$NON-NLS-1$
            break;

        default:
        }
        // nothing todo
    }

    /**
     * Part of constructor
     *
     * Initializes lock strategy if repository node type is REPOSITORY_ELEMENT
     *
     * If needed, locks the repository node
     */
    private void lockNode() {
        ITaCoKitRepositoryNode repositoryNode = runtimeData.getTaCoKitRepositoryNode();
        ENodeType nodeType = repositoryNode.getType();
        if (ENodeType.REPOSITORY_ELEMENT.equals(nodeType)) {
            setRepositoryObject(repositoryNode.getObject());
            isRepositoryObjectEditable();
            initLockStrategy();
        }
    }

    /**
     * Part of constructor, sets window title
     */
    protected abstract void setWindowTitle();

    @Override
    public void addPages() {
        wizardPropertiesPage = new Step0WizardPage(runtimeData.getConnectionItem().getProperty(), pathToSave,
                TaCoKitConst.METADATA_TACOKIT, runtimeData.isReadonly(), creation);
        final ConfigTypeNode configTypeNode = runtimeData.getConfigTypeNode();
        wizardPropertiesPage.setTitle(Messages.getString("TaCoKitConfiguration.wizard.title.str", //$NON-NLS-1$
                configTypeNode.getDisplayName(), configTypeNode.getConfigurationType()));
        wizardPropertiesPage.setDescription(""); //$NON-NLS-1$
        addPage(wizardPropertiesPage);
        final PropertyNode root = new PropertyTreeCreator(new WizardTypeMapper()).createPropertyTree(configTypeNode);
        TaCoKitPageBuildHelper helper = new TaCoKitPageBuildHelper(this.runtimeData);
        if (root.hasLeaves(Metadatas.MAIN_FORM)) {
            helper.infer(Metadatas.MAIN_FORM);
        }
        if (root.hasLeaves(Metadatas.ADVANCED_FORM)) {
            helper.infer(Metadatas.ADVANCED_FORM);
        }
        helper.terminate();
        if (root.hasLeaves(Metadatas.MAIN_FORM)) {

            mainPage = new TaCoKitConfigurationWizardPage(runtimeData,
                    Metadatas.MAIN_FORM,
                    isNew(),
                    helper.getParameters(Metadatas.MAIN_FORM));
            addPage(mainPage);
        }
        if (root.hasLeaves(Metadatas.ADVANCED_FORM)) {
            advancedPage = new TaCoKitConfigurationWizardPage(runtimeData,
                    Metadatas.ADVANCED_FORM,
                    isNew(),
                    helper.getParameters(Metadatas.ADVANCED_FORM));
            addPage(advancedPage);
        }
    }

    /*public Iterable<? extends IElementParameter> getElementsParameters() {
        return Iterables.concat(
                this.mainPage != null ? this.mainPage.getElementsParameters() : Collections.emptyList(),
                this.advancedPage != null ? this.advancedPage.getElementsParameters() : Collections.emptyList());
    }*/

    @Override
    public boolean performFinish() {
        // if both are null, then nothing to do and "perform finish" operation is always accepted
        if (mainPage == null && advancedPage == null) {
            return true;
        }
        // if main page is present, then it controls "perform finish" operation acceptence/refuse
        if (mainPage != null) {
            return mainPage.isPageComplete() && doPerformFinish();
        // otherwise advanced page controls "perform finish" operation
        } else {
            return advancedPage.isPageComplete() && doPerformFinish();
        }
    }

    private boolean doPerformFinish() {
        try {
            IWorkspace workspace = ResourcesPlugin.getWorkspace();
            IWorkspaceRunnable operation = createFinishOperation();
            ISchedulingRule schedulingRule = workspace.getRoot();
            workspace.run(operation, schedulingRule, IWorkspace.AVOID_UPDATE, new NullProgressMonitor());
            closeLockStrategy();
            return true;
        } catch (Exception e) {
            ExceptionHandler.process(e);
            String message = e.getLocalizedMessage();
            if (StringUtils.isEmpty(message)) {
                message = Messages.getString("TaCoKitConfiguration.wizard.exception.message.default"); //$NON-NLS-1$
            }
            ExceptionMessageDialog.openError(getShell(), Messages.getString("TaCoKitConfiguration.wizard.exception.title"), //$NON-NLS-1$
                    message, e);
        }
        return false;
    }

    /**
     * Creates operation, which is performed, when Finish button is pushed.
     *
     * Creates different operations depending on whether it is Create or Edit wizard
     *
     *
     *
     * @return operation to perform on finish
     */
    protected abstract IWorkspaceRunnable createFinishOperation();

    @Override
    public boolean canFinish() {
        if (getRuntimeData().isReadonly()) {
            return false;
        }
        IWizardPage currentPage = this.getContainer().getCurrentPage();
        if (currentPage instanceof Step0WizardPage) {
            return false;
        }
        return super.canFinish();
    }

    @Override
    public ConnectionItem getConnectionItem() {
        return connectionItem;
    }

    protected TaCoKitConfigurationRuntimeData getRuntimeData() {
        return this.runtimeData;
    }

    protected Step0WizardPage getWizardPropertiesPage() {
        return this.wizardPropertiesPage;
    }

    protected TaCoKitConfigurationWizardPage getMainPage() {
        return this.mainPage;
    }

    protected TaCoKitConfigurationWizardPage getAdvancedPage() {
        return this.advancedPage;
    }

}
