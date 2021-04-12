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
package org.talend.designer.core.ui.editor.dependencies.dialog;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.designer.core.i18n.Messages;
import org.talend.repository.model.IRepositoryNode;
import org.talend.repository.viewer.ui.provider.RepositoryViewerProvider;
import org.talend.repository.viewer.ui.viewer.RepositoryTreeViewer;

public class DependenciesResourceSelectionDialog extends Dialog {

    private RepositoryTreeViewer repositoryTreeViewer;

    private String selectedNodeId;

    private IRepositoryNode result;

    public DependenciesResourceSelectionDialog(final Shell parentShell) {
        super(parentShell);
    }

    protected void configureShell(Shell shell) {
        super.configureShell(shell);
        shell.setText(Messages.getString("DependenciesResourceSelectionDialog.selectResource")); //$NON-NLS-1$
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        final Composite container = (Composite) super.createDialogArea(parent);

        final RepositoryViewerProvider provider = new RepositoryViewerProvider() {

            @Override
            protected TreeViewer createTreeViewer(Composite parent, int style) {
                return new RepositoryTreeViewer(parent, style);
            }

            @Override
            protected ERepositoryObjectType getCheckingType() {
                return ERepositoryObjectType.valueOf(ERepositoryObjectType.class, "RESOURCES");
            }

            @Override
            protected int getStyle() {
                // http://jira.talendforge.org/browse/TESB-6582 Xiaopeng Li
                return SWT.BORDER | SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL;
            }

        };

        repositoryTreeViewer = (RepositoryTreeViewer) provider.createViewer(container);
        repositoryTreeViewer.expandAll();

        repositoryTreeViewer.addSelectionChangedListener(new ISelectionChangedListener() {

            public void selectionChanged(SelectionChangedEvent event) {
                getButton(IDialogConstants.OK_ID).setEnabled(validateSelection((IStructuredSelection) event.getSelection()));
            }
        });
        repositoryTreeViewer.addDoubleClickListener(new IDoubleClickListener() {

            public void doubleClick(DoubleClickEvent event) {
                if (getButton(IDialogConstants.OK_ID).isEnabled()) {
                    okPressed();
                }
            }
        });

        return container;
    }

    @Override
    protected Control createContents(Composite parent) {
        final Control c = super.createContents(parent);

        getButton(IDialogConstants.OK_ID).setEnabled(false);

        // avoid NPE in ISelectionChangedListener
        if (null != selectedNodeId) {
            final IRepositoryNode node = getNodeById((IRepositoryNode) repositoryTreeViewer.getInput(), selectedNodeId);
            if (null != node) {
                repositoryTreeViewer.setSelection(new StructuredSelection(node), true);
            }
        }

        return c;
    }

    @Override
    protected Point getInitialSize() {
        return new Point(400, 500);
    }

    public void setSelectedNodeId(String selectionNode) {
        this.selectedNodeId = selectionNode;
    }

    public IRepositoryNode getResult() {
        return result;
    }

    private static IRepositoryNode getNodeById(IRepositoryNode node, String id) {
        if (id.equals(node.getId())) {
            return node;
        }
        for (IRepositoryNode child : node.getChildren()) {
            IRepositoryNode result = getNodeById(child, id);
            if (null != result) {
                return result;
            }
        }
        return null;
    }

    private boolean validateSelection(final IStructuredSelection selection) {
        if (selection.size() == 1) {
            result = (IRepositoryNode) selection.getFirstElement();
            if (result.getObject() != null
                    && result.getObject().getRepositoryObjectType() == ERepositoryObjectType.valueOf(ERepositoryObjectType.class,
                            "RESOURCES")) {
                return true;
            }
        }
        return false;
    }

}
