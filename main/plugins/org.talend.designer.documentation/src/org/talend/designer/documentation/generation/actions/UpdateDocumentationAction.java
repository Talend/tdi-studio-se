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

import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;
import org.talend.commons.ui.runtime.image.ECoreImage;
import org.talend.commons.ui.runtime.image.ImageProvider;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.metadata.managment.ui.wizard.documentation.DocumentationUpdateWizard;
import org.talend.repository.i18n.Messages;
import org.talend.repository.model.IProxyRepositoryFactory;
import org.talend.repository.model.IRepositoryNode.ENodeType;
import org.talend.repository.model.RepositoryNode;
import org.talend.repository.model.RepositoryNodeUtilities;
import org.talend.repository.ui.actions.AContextualAction;

/**
 * Action used to update an existing documentation.<br/>
 *
 * $Id: UpdateDocumentationAction.java 77219 2012-01-24 01:14:15Z mhirt $
 *
 */
public class UpdateDocumentationAction extends AContextualAction {

    /**
     * Constructs a new UpdateDocumentationAction.
     */
    public UpdateDocumentationAction() {
        super();

        setText(Messages.getString("UpdateDocumentationAction.updateDocActionText.updateDoc")); //$NON-NLS-1$
        setToolTipText(Messages.getString("UpdateDocumentationAction.updateDocActionTipText.updateDoc")); //$NON-NLS-1$
        setImageDescriptor(ImageProvider.getImageDesc(ECoreImage.DOCUMENTATION_ICON));
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.repository.ui.actions.ITreeContextualAction#init(org.eclipse.jface.viewers.TreeViewer,
     * org.eclipse.jface.viewers.IStructuredSelection)
     */
    @Override
    public void init(TreeViewer viewer, IStructuredSelection selection) {
        boolean canWork = !selection.isEmpty() && selection.size() == 1;
        if (canWork) {
            IProxyRepositoryFactory factory = ProxyRepositoryFactory.getInstance();
            RepositoryNode node = (RepositoryNode) selection.getFirstElement();
            canWork = (node.getType() == ENodeType.REPOSITORY_ELEMENT
                    && node.getObject().getRepositoryObjectType() == ERepositoryObjectType.DOCUMENTATION && factory
                    .isPotentiallyEditable(node.getObject()));
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
        RepositoryNode node = (RepositoryNode) ((IStructuredSelection) getSelection()).getFirstElement();

        DocumentationUpdateWizard docWizard = new DocumentationUpdateWizard(PlatformUI.getWorkbench(), node.getObject(),
                getPath());
        WizardDialog dlg = new WizardDialog(Display.getCurrent().getActiveShell(), docWizard);
        dlg.open();

    }

    private IPath getPath() {
        RepositoryNode node = (RepositoryNode) ((IStructuredSelection) getSelection()).getFirstElement();

        IPath path;
        if (node.getType() == ENodeType.SIMPLE_FOLDER || node.getType() == ENodeType.SYSTEM_FOLDER) {
            path = RepositoryNodeUtilities.getPath(node);
        } else {
            path = RepositoryNodeUtilities.getPath(node);
        }
        return path;
    }
}
