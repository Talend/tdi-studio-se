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
package org.talend.sbi.engines.client.ui.actions;

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
import org.talend.sbi.engines.client.Activator;
import org.talend.sbi.engines.client.ui.preferences.SpagoPreferenceInitializer;
import org.talend.sbi.engines.client.ui.wizards.PublishOnSpagoExportWizard;

/**
 * Action used to export job scripts. <br/>
 *
 * $Id: PublishOnSpagoAction.java 1 2007-04-26 11:25:00 cantoine
 *
 */
public final class PublishOnSpagoAction extends AContextualAction {

    private static final String EXPORTJOBSCRIPTS = "Deploy on SpagoBI"; //$NON-NLS-1$

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
        return isEnabled() && Activator.getDefault().getPreferenceStore().getBoolean(SpagoPreferenceInitializer.SPAGO_STATUS);
    }

    public PublishOnSpagoAction() {
        super();
        this.setText(EXPORTJOBSCRIPTS);
        this.setToolTipText(EXPORTJOBSCRIPTS);
        this.setImageDescriptor(ImageProvider.getImageDesc(EImage.EXPORT_ICON));
    }

    protected void doRun() {
        PublishOnSpagoExportWizard publishWizard = new PublishOnSpagoExportWizard();
        IWorkbench workbench = getWorkbench();
        publishWizard.setWindowTitle(EXPORTJOBSCRIPTS + " (SpagoBI)"); //$NON-NLS-1$
        publishWizard.init(workbench, (IStructuredSelection) this.getSelection());

        Shell activeShell = Display.getCurrent().getActiveShell();
        WizardDialog dialog = new WizardDialog(activeShell, publishWizard);
        dialog.open();
    }
}
