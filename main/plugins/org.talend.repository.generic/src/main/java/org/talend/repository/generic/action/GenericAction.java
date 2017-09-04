// ============================================================================
//
// Copyright (C) 2006-2017 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.repository.generic.action;

import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.PlatformUI;
import org.talend.components.api.wizard.ComponentWizard;
import org.talend.components.api.wizard.ComponentWizardDefinition;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.core.repository.ui.actions.metadata.AbstractCreateAction;
import org.talend.repository.ProjectManager;
import org.talend.repository.generic.i18n.Messages;
import org.talend.repository.generic.ui.GenericConnWizard;
import org.talend.repository.generic.ui.common.GenericWizardDialog;
import org.talend.repository.generic.util.GenericWizardServiceFactory;
import org.talend.repository.model.IProxyRepositoryFactory;
import org.talend.repository.model.IRepositoryNode.EProperties;
import org.talend.repository.model.RepositoryNode;

/**
 * created by ycbai on 2017年7月5日 Detailled comment
 *
 */
public class GenericAction extends AbstractCreateAction {

    private static final int DEFAULT_WIZARD_WIDTH = 700;

    private static final int DEFAULT_WIZARD_HEIGHT = 400;

    private ComponentWizard compWizard;

    private boolean creation = true;

    private ERepositoryObjectType repObjType;

    public GenericAction(ComponentWizard compWizard) {
        super();
        this.compWizard = compWizard;
    }

    @Override
    protected void doRun() {
        IWizard wizard = new GenericConnWizard(PlatformUI.getWorkbench(), creation, compWizard, repositoryNode,
                getExistingNames());
        WizardDialog wizardDialog = new GenericWizardDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
                wizard, GenericWizardServiceFactory.getGenericWizardInternalService().getComponentService());
        if (Platform.getOS().equals(Platform.OS_LINUX)) {
            wizardDialog.setPageSize(getWizardWidth(), getWizardHeight() + 80);
        }
        wizardDialog.create();
        wizardDialog.open();
    }

    @Override
    protected void init(RepositoryNode node) {
        repositoryNode = getCurrentRepositoryNode();
        repObjType = (ERepositoryObjectType) repositoryNode.getProperties(EProperties.CONTENT_TYPE);
        if (compWizard == null) {
            compWizard = GenericWizardServiceFactory.getGenericWizardInternalService().getComponentWizard(repObjType.getType(),
                    null);
        }
        if (compWizard == null) {
            return;
        }
        Image nodeImage = GenericWizardServiceFactory.getGenericWizardService().getNodeImage(repObjType.getType());
        if (nodeImage != null) {
            this.setImageDescriptor(ImageDescriptor.createFromImage(nodeImage));
        }
        IProxyRepositoryFactory factory = ProxyRepositoryFactory.getInstance();
        switch (node.getType()) {
        case SIMPLE_FOLDER:
        case SYSTEM_FOLDER:
            if (factory.isUserReadOnlyOnCurrentProject() || !ProjectManager.getInstance().isInCurrentMainProject(node)) {
                setEnabled(false);
                return;
            }
            if (node.getObject() != null && node.getObject().getProperty().getItem().getState().isDeleted()) {
                setEnabled(false);
                return;
            }
            collectChildNames(node);
            creation = true;
            break;
        case REPOSITORY_ELEMENT:
            if (factory.isPotentiallyEditable(node.getObject()) && isLastVersion(node)) {
                collectSiblingNames(node);
            }
            creation = false;
            break;
        default:
            return;
        }
        this.setText(getMenuItemName());
        setEnabled(true);
    }

    protected int getWizardWidth() {
        return DEFAULT_WIZARD_WIDTH;
    }

    protected int getWizardHeight() {
        return DEFAULT_WIZARD_HEIGHT;
    }

    private String getMenuItemName() {
        ComponentWizardDefinition wizardDefinition = compWizard.getDefinition();
        String menuItemName = wizardDefinition.getMenuItemName();
        if (wizardDefinition.isTopLevel()) {
            if (creation) {
                menuItemName = Messages.getString("CreateGenericConnectionAction.createLabel", menuItemName); //$NON-NLS-1$
            } else {
                menuItemName = Messages.getString("CreateGenericConnectionAction.editLabel", menuItemName); //$NON-NLS-1$
            }
        }
        return menuItemName;
    }

}
