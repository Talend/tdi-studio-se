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
package org.talend.repository.ui.actions;

import java.util.List;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.ISaveablePart;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.talend.commons.runtime.model.repository.ERepositoryStatus;
import org.talend.commons.ui.gmf.util.DisplayUtils;
import org.talend.commons.ui.runtime.image.EImage;
import org.talend.commons.ui.runtime.image.ImageProvider;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.repository.RepositoryPlugin;
import org.talend.repository.i18n.Messages;
import org.talend.repository.model.IRepositoryNode.ENodeType;
import org.talend.repository.model.IRepositoryNode.EProperties;
import org.talend.repository.model.RepositoryNode;
import org.talend.repository.preference.ExportJobTokenCollector;
import org.talend.repository.ui.wizards.exportjob.JobScriptsExportWizard;
import org.talend.repository.ui.wizards.exportjob.util.ExportJobUtil;

/**
 * Action used to export job scripts. <br/>
 *
 * $Id: ExportJobScriptAction.java 1 2006-12-13 下午03:12:05 bqian
 *
 */
public class ExportJobScriptAction extends AContextualAction {

    protected static final String EXPORTJOBSCRIPTS = Messages.getString("ExportJobScriptAction.buildJob"); //$NON-NLS-1$

    /*
     * (non-Javadoc)
     *
     * @see org.talend.repository.ui.actions.ITreeContextualAction#init(org.eclipse.jface.viewers.TreeViewer,
     * org.eclipse.jface.viewers.IStructuredSelection)
     */
    @Override
    public void init(TreeViewer viewer, IStructuredSelection selection) {
        boolean canWork = true;
        if (selection.isEmpty()) {
            setEnabled(false);
            return;
        }
        List<RepositoryNode> nodes = selection.toList();
        for (RepositoryNode node : nodes) {
            if (node.getProperties(EProperties.CONTENT_TYPE) != ERepositoryObjectType.PROCESS) {
                canWork = false;
                break;
            }
            if (node.getType() != ENodeType.REPOSITORY_ELEMENT && node.getChildren().isEmpty()) {
                canWork = false;
                break;
            }

            if (canWork && node.getObject() != null
                    && ProxyRepositoryFactory.getInstance().getStatus(node.getObject()) == ERepositoryStatus.DELETED) {
                canWork = false;
                break;
            }
            if (ExportJobUtil.getProcessItem(nodes) == null) {
                canWork = false;
                break;
            }
        }
        setEnabled(canWork);
    }

    @Override
    public boolean isVisible() {
        return isEnabled();
    }

    public ExportJobScriptAction() {
        super();
        this.setText(EXPORTJOBSCRIPTS);
        this.setToolTipText(EXPORTJOBSCRIPTS);
        this.setImageDescriptor(ImageProvider.getImageDesc(EImage.EXPORT_JOB_ICON));
    }

    @Override
    protected void doRun() {
        JobScriptsExportWizard processWizard = new JobScriptsExportWizard();
        IWorkbench workbench = getWorkbench();
        processWizard.setWindowTitle(EXPORTJOBSCRIPTS);
        processWizard.init(workbench, (IStructuredSelection) this.getSelection());

        Shell activeShell = Display.getCurrent().getActiveShell();
        WizardDialog dialog = new WizardDialog(activeShell, processWizard);

        if (checkDirtyPart(workbench)) {
            MessageDialog messageDialog = new MessageDialog(DisplayUtils.getDefaultShell(false), "", //$NON-NLS-1$
                    null, Messages.getString("ExportJobScriptAction.confirmMessage"), //$NON-NLS-1$
                    MessageDialog.CONFIRM, new String[] {
                            Messages.getString("ExportJobScriptAction.confirmContiune"), IDialogConstants.CANCEL_LABEL }, 0); //$NON-NLS-1$
            if (messageDialog.open() != 0) {
                return; // don't do anything
            }
        }
        dialog.setPageSize(830, 580);
        dialog.open();

        // collector
        IPreferenceStore preferenceStore = RepositoryPlugin.getDefault().getPreferenceStore();
        int num = preferenceStore.getInt(ExportJobTokenCollector.TOS_COUNT_JOB_EXPORTS.getPrefKey());
        preferenceStore.setValue(ExportJobTokenCollector.TOS_COUNT_JOB_EXPORTS.getPrefKey(), num + 1);

    }

    /**
     * DOC Administrator Comment method "checkDirtyPart".
     *
     * @param workbench
     */
    @SuppressWarnings("restriction")
    private boolean checkDirtyPart(IWorkbench workbench) {
        ISaveablePart[] parts = null;
        IWorkbenchWindow[] windows = workbench.getWorkbenchWindows();
        for (IWorkbenchWindow window : windows) {
            IWorkbenchPage[] pages = window.getPages();
            for (IWorkbenchPage page : pages) {
                parts = page.getDirtyEditors();
            }
        }
        if (parts != null && parts.length > 0) {
            return true;
        }
        return false;
    }
}
