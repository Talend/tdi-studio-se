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
package org.talend.designer.documentation.generation.actions;

import java.util.List;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbench;
import org.talend.commons.runtime.model.repository.ERepositoryStatus;
import org.talend.commons.ui.runtime.image.EImage;
import org.talend.commons.ui.runtime.image.ImageProvider;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.designer.core.ICamelDesignerCoreService;
import org.talend.designer.documentation.generation.wizards.GenerateDocAsHTMLWizard;
import org.talend.repository.ProjectManager;
import org.talend.repository.i18n.Messages;
import org.talend.repository.model.IRepositoryNode.EProperties;
import org.talend.repository.model.RepositoryNode;
import org.talend.repository.ui.actions.AContextualAction;

/**
 * ftang class global comment. Detailed comment
 */
public class GenerateDocAsHTMLAction extends AContextualAction {

    private static final String GENERATE_DOC_AS_HTML = Messages.getString("GenerateDocAsHTMLAction.GenerateDocAsHTML"); //$NON-NLS-1$

    /*
     * (non-Javadoc)
     *
     * @see org.talend.repository.ui.actions.ITreeContextualAction#init(org.eclipse.jface.viewers.TreeViewer,
     * org.eclipse.jface.viewers.IStructuredSelection)
     */
    @Override
    public void init(TreeViewer viewer, IStructuredSelection selection) {
        ProjectManager instance = ProjectManager.getInstance();
        boolean canWork = false;
        List<RepositoryNode> nodes = selection.toList();

        for (RepositoryNode node : nodes) {
            if (ERepositoryObjectType.PROCESS_MR != null || ERepositoryObjectType.PROCESS_STORM != null) {
                if (node.getProperties(EProperties.CONTENT_TYPE) == ERepositoryObjectType.PROCESS_MR
                        || node.getProperties(EProperties.CONTENT_TYPE) == ERepositoryObjectType.PROCESS_STORM) {
                    if (node.getObject() != null && instance.isInCurrentMainProject(node)) {
                        canWork = true;
                    }
                }
            }

            Object contentType = node.getProperties(EProperties.CONTENT_TYPE);
            if (contentType == ERepositoryObjectType.PROCESS || contentType == ERepositoryObjectType.PROCESS_ROUTE) {
                if (node.getObject() != null && instance.isInCurrentMainProject(node)) {
                    canWork = true;
                }
            }
            if (canWork && node.getObject() != null
                    && ProxyRepositoryFactory.getInstance().getStatus(node.getObject()) == ERepositoryStatus.DELETED) {
                canWork = false;
                break;
            }
        }
        setEnabled(canWork);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.repository.ui.actions.AContextualAction#isVisible()
     */
    @Override
    public boolean isVisible() {
        return isEnabled();
    }

    /**
     * Default constructor.
     */
    public GenerateDocAsHTMLAction() {
        super();
        this.setText(GENERATE_DOC_AS_HTML);
        this.setToolTipText(GENERATE_DOC_AS_HTML);
        this.setImageDescriptor(ImageProvider.getImageDesc(EImage.EXPORT_HTML_ICON));
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.jface.action.Action#run()
     */
    @Override
    protected void doRun() {
        GenerateDocAsHTMLWizard processWizard = new GenerateDocAsHTMLWizard();
        IWorkbench workbench = getWorkbench();
        processWizard.setWindowTitle(GENERATE_DOC_AS_HTML);
        processWizard.init(workbench, (IStructuredSelection) this.getSelection());

        Shell activeShell = Display.getCurrent().getActiveShell();
        WizardDialog dialog = new WizardDialog(activeShell, processWizard);
        dialog.open();
    }
}
