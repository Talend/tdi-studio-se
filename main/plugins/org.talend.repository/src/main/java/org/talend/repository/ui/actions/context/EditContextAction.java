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
package org.talend.repository.ui.actions.context;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;
import org.talend.commons.ui.runtime.image.ECoreImage;
import org.talend.commons.ui.runtime.image.ImageProvider;
import org.talend.core.model.properties.ContextItem;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.metadata.managment.ui.wizard.context.ContextWizard;
import org.talend.repository.ProjectManager;
import org.talend.repository.i18n.Messages;
import org.talend.repository.model.IProxyRepositoryFactory;
import org.talend.repository.model.RepositoryNode;

/**
 * DOC nrousseau class global comment. Detailled comment <br/>
 *
 * $Id: talend-code-templates.xml 1 2006-09-29 17:06:40 +0000 (ven., 29 sept. 2006) nrousseau $
 *
 */
public class EditContextAction extends AbstractConextAction {

    private static final String EDIT_LABEL = Messages.getString("EditContextAction.editContext"); //$NON-NLS-1$

    private RepositoryNode node = null;

    /**
     * DOC nrousseau EditContextAction constructor comment.
     */
    public EditContextAction() {
        super();

        this.setText(EDIT_LABEL);
        this.setToolTipText(EDIT_LABEL);
        this.setImageDescriptor(ImageProvider.getImageDesc(ECoreImage.CONTEXT_ICON));
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.commons.ui.swt.actions.ITreeContextualAction#init(org.eclipse.jface.viewers.TreeViewer,
     * org.eclipse.jface.viewers.IStructuredSelection)
     */
    @Override
    public void init(TreeViewer viewer, IStructuredSelection selection) {
        IProxyRepositoryFactory factory = ProxyRepositoryFactory.getInstance();
        super.init(viewer, selection);
        boolean canWork = !selection.isEmpty() && selection.size() == 1;
        if (canWork) {
            Object o = selection.getFirstElement();
            node = (RepositoryNode) o;
            switch (node.getType()) {
            case REPOSITORY_ELEMENT:
                if (node.getObjectType() != ERepositoryObjectType.CONTEXT) {
                    canWork = false;
                }
                break;
            default:
                canWork = false;
            }
            RepositoryNode parent = node.getParent();
            if (canWork && parent != null && parent.isBin()) {
                canWork = false;
            }
            if (canWork && (!ProjectManager.getInstance().isInCurrentMainProject(node) || !isLastVersion(node))) {
                canWork = false;
            }
        }
        setEnabled(canWork);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.jface.action.Action#run()
     */
    @Override
    protected void doRun() {
        if (repositoryNode == null) {
            repositoryNode = getCurrentRepositoryNode();
        }
        ContextWizard contextWizard = new ContextWizard(PlatformUI.getWorkbench(), false, repositoryNode, false);
        WizardDialog dlg = new WizardDialog(Display.getCurrent().getActiveShell(), contextWizard);

        dlg.open();

    }

    @Override
    public Class getClassForDoubleClick() {
        return ContextItem.class;
    }

}
