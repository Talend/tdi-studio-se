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

import org.eclipse.gef.commands.CommandStack;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.talend.commons.ui.runtime.image.EImage;
import org.talend.commons.ui.runtime.image.ImageProvider;
import org.talend.core.model.process.IProcess2;
import org.talend.core.ui.context.model.table.ContextTableTabChildModel;
import org.talend.core.ui.context.model.table.ContextTableTabParentModel;
import org.talend.designer.core.i18n.Messages;
import org.talend.designer.core.ui.editor.dependencies.model.JobContextTreeNode;
import org.talend.designer.core.ui.editor.dependencies.model.JobResourceDependencyModel;
import org.talend.designer.core.ui.editor.dependencies.provider.JobContextViewerProvider;
import org.talend.designer.core.ui.editor.dependencies.util.ResourceContextHelper;
import org.talend.repository.viewer.ui.viewer.RepositoryTreeViewer;

public class DependenciesContextSelectionDialog extends Dialog {

    private RepositoryTreeViewer repositoryTreeViewer;

    private Label messageErrorImage;

    private Label messageLabel;

    private Button addButton;

    private String selectedNodeId;

    private JobContextTreeNode result;

    private IProcess2 process;

    private CommandStack commandStack;

    private JobResourceDependencyModel model;

    public DependenciesContextSelectionDialog(final Shell parentShell, IProcess2 process, CommandStack commandStack,
            JobResourceDependencyModel model) {
        super(parentShell);
        this.process = process;
        this.commandStack = commandStack;
        this.model = model;
    }

    protected void configureShell(Shell shell) {
        super.configureShell(shell);
        shell.setText(Messages.getString("DependenciesContextSelectionDialog.selectContext")); //$NON-NLS-1$
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        ResourceContextHelper contextHelper = new ResourceContextHelper(process, null);
        initErrorMessageComposite(parent);

        final Composite container = (Composite) super.createDialogArea(parent);
        final JobContextViewerProvider provider = new JobContextViewerProvider(contextHelper);

        repositoryTreeViewer = (RepositoryTreeViewer) provider.createViewer(container);
        repositoryTreeViewer.expandAll();

        repositoryTreeViewer.addSelectionChangedListener(new ISelectionChangedListener() {

            public void selectionChanged(SelectionChangedEvent event) {
                IStructuredSelection selection = (IStructuredSelection) event.getSelection();
                JobContextTreeNode node = (JobContextTreeNode) selection.getFirstElement();
                boolean okFlag = false;
                if (node != null) {
                    String sourceId = null;
                    if (node.getParent() == null) {
                        sourceId = ((ContextTableTabParentModel) node.getTreeData()).getSourceId();
                    } else {
                        sourceId = ((ContextTableTabChildModel) node.getTreeData()).getSourceId();
                    }
                    boolean checkIfContextVarIsInUse = contextHelper.checkIfContextVarIsInUse(node.getName(), sourceId,
                            model.getPathUrl());
                    if (checkIfContextVarIsInUse) {
                        setMessageVisible(true);
                    }
                    if (sourceId != null && !checkIfContextVarIsInUse && validateSelection(selection)) {
                        setMessageVisible(false);
                        okFlag = true;
                    }
                }
                getButton(IDialogConstants.OK_ID).setEnabled(okFlag);
            }
        });
        repositoryTreeViewer.addDoubleClickListener(new IDoubleClickListener() {

            public void doubleClick(DoubleClickEvent event) {
                if (getButton(IDialogConstants.OK_ID).isEnabled()) {
                    okPressed();
                }
            }
        });

        addButton = createAddPushButton(container);

        return container;
    }

    private void initErrorMessageComposite(Composite parent) {
        Composite container = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout(2, false);
        container.setLayout(layout);
        messageErrorImage = new Label(container, SWT.LEFT);
        messageErrorImage.setImage(JFaceResources.getImage("dialog_message_error_image"));
        messageLabel = new Label(container, SWT.LEFT);
        messageLabel.setText(Messages.getString("DependenciesContextSelectionDialog.contextInUseError"));
        setMessageVisible(false);
    }

    private void setMessageVisible(boolean isVisible) {
        messageErrorImage.setVisible(isVisible);
        messageLabel.setVisible(isVisible);
    }

    private Button createAddPushButton(final Composite parent) {
        Button addPushButton = new Button(parent, SWT.PUSH);
        addPushButton.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                ResourceContextHelper contextHelper = new ResourceContextHelper(process, commandStack);
                contextHelper.addContextParameterForResource();
                repositoryTreeViewer.setInput(contextHelper.createRootJobContextTreeNode());
                repositoryTreeViewer.refresh();
            }

        });
        Image image = ImageProvider.getImage(EImage.ADD_ICON);
        addPushButton.setImage(image);
        addPushButton.setText(Messages.getString("DependenciesContextSelectionDialog.addContext")); //$NON-NLS-1$
        return addPushButton;
    }

    @Override
    protected Control createContents(Composite parent) {
        final Control c = super.createContents(parent);

        getButton(IDialogConstants.OK_ID).setEnabled(false);

        // avoid NPE in ISelectionChangedListener
        if (null != selectedNodeId) {
            final JobContextTreeNode node = getNodeById((JobContextTreeNode) repositoryTreeViewer.getInput(), selectedNodeId);
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

    public JobContextTreeNode getResult() {
        return result;
    }

    private static JobContextTreeNode getNodeById(JobContextTreeNode node, String id) {
        if (id.equals(node.getOrderId())) {
            return node;
        }
        for (JobContextTreeNode child : node.getChildren()) {
            JobContextTreeNode result = getNodeById(child, id);
            if (null != result) {
                return result;
            }
        }
        return null;
    }

    private boolean validateSelection(final IStructuredSelection selection) {
        if (selection.size() == 1) {
            result = (JobContextTreeNode) selection.getFirstElement();
            return true;
        }
        return false;
    }

}
