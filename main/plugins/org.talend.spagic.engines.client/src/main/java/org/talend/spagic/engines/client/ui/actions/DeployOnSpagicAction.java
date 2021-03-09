// ============================================================================
//
// Copyright (C) 2006-2021 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.spagic.engines.client.ui.actions;

import java.util.List;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbench;
import org.talend.commons.ui.runtime.image.EImage;
import org.talend.commons.ui.runtime.image.ImageProvider;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.repository.model.IRepositoryNode.ENodeType;
import org.talend.repository.model.IRepositoryNode.EProperties;
import org.talend.repository.model.RepositoryNode;
import org.talend.repository.ui.actions.AContextualAction;
import org.talend.spagic.engines.client.Activator;
import org.talend.spagic.engines.client.i18n.Messages;
import org.talend.spagic.engines.client.ui.preferences.SpagicPreferenceInitializer;
import org.talend.spagic.engines.client.ui.wizards.SpagicDeployWizard;

/**
 * Action used to export job scripts. <br/>
 *
 * $Id: ExportJobScriptAction.java 1 2006-12-13 涓嬪�?3:12:05 bqian
 *
 */
public class DeployOnSpagicAction extends AContextualAction {

    protected static final String DEPLOYONSPAGIC = Messages.getString("DeployOnSpagicAction.actionLabel"); //$NON-NLS-1$

    /*
     * (non-Javadoc)
     *
     * @see org.talend.repository.ui.actions.ITreeContextualAction#init(org.eclipse.jface.viewers.TreeViewer,
     * org.eclipse.jface.viewers.IStructuredSelection)
     */
    public void init(TreeViewer viewer, IStructuredSelection selection) {
        boolean canWork = true;
        if (selection.isEmpty()) {
            setEnabled(false);
            return;
        }

        List<RepositoryNode> nodes = (List<RepositoryNode>) selection.toList();
        for (RepositoryNode node : nodes) {
            if (node.getType() != ENodeType.REPOSITORY_ELEMENT
                    || node.getProperties(EProperties.CONTENT_TYPE) != ERepositoryObjectType.PROCESS) {
                canWork = false;
                break;
            }
        }
        setEnabled(canWork);
    }

    public boolean isVisible() {
        return isEnabled() && Activator.getDefault().getPreferenceStore().getBoolean(SpagicPreferenceInitializer.SPAGIC_STATUS);
    }

    public DeployOnSpagicAction() {
        super();
        this.setText(DEPLOYONSPAGIC);
        this.setToolTipText(DEPLOYONSPAGIC);
        this.setImageDescriptor(ImageProvider.getImageDesc(EImage.EXPORT_ICON));
    }

    protected void doRun() {
        SpagicDeployWizard processWizard = new SpagicDeployWizard();
        IWorkbench workbench = getWorkbench();
        processWizard.setWindowTitle(DEPLOYONSPAGIC);
        processWizard.init(workbench, (IStructuredSelection) this.getSelection());

        Shell activeShell = Display.getCurrent().getActiveShell();
        WizardDialog dialog = new WizardDialog(activeShell, processWizard);
        workbench.saveAllEditors(true);
        dialog.open();
    }
}
