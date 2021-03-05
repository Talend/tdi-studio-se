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
package org.talend.repository.ui.dialog;

import java.util.List;

import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.model.utils.RepositoryManagerHelper;
import org.talend.repository.i18n.Messages;
import org.talend.repository.ui.views.IRepositoryView;

/**
 * A job selection dialog used for opening jobs.
 */
public class OpenJobSelectionDialog extends RepositoryReviewDialog {

    private static final int SELECTINREPOSITORY = 99;

    public OpenJobSelectionDialog(Shell parentShell, List<ERepositoryObjectType> repObjectTypes) {
        super(parentShell, repObjectTypes, null, true);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.repository.ui.dialog.RepositoryReviewDialog#configureShell(org.eclipse.swt.widgets.Shell)
     */
    @Override
    protected void configureShell(Shell shell) {
        super.configureShell(shell);
        shell.setText(Messages.getString("OpenJobSelectionDialog.findJob")); //$NON-NLS-1$
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.talend.repository.ui.dialog.RepositoryReviewDialog#createButtonsForButtonBar(org.eclipse.swt.widgets.Composite
     * )
     */
    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        createButton(parent, SELECTINREPOSITORY, "Link Repository", false); //$NON-NLS-1$
        super.createButtonsForButtonBar(parent);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.jface.dialogs.Dialog#buttonPressed(int)
     */
    @Override
    protected void buttonPressed(int buttonId) {
        if (SELECTINREPOSITORY == buttonId) {
            IStructuredSelection selection = (IStructuredSelection) getRepositoryTreeViewer().getSelection();
            // RepositoryNode node = (RepositoryNode) selection.getFirstElement();
            //
            // RepositoryView.show().expand(node);
            final IRepositoryView repositoryView = RepositoryManagerHelper.findRepositoryView();
            if (repositoryView != null) {
                repositoryView.getViewer().setSelection(selection, true);
            }
        } else {
            super.buttonPressed(buttonId);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.repository.ui.dialog.RepositoryReviewDialog#createDialogArea(org.eclipse.swt.widgets.Composite)
     */
    @Override
    protected Control createDialogArea(Composite parent) {
        Control control = super.createDialogArea(parent);
        getRepositoryTreeViewer().addSelectionChangedListener(new ISelectionChangedListener() {

            @Override
            public void selectionChanged(SelectionChangedEvent event) {
                boolean highlightOKButton = isSelectionValid(event);
                getButton(SELECTINREPOSITORY).setEnabled(highlightOKButton);
            }

        });

        return control;
    }

}
