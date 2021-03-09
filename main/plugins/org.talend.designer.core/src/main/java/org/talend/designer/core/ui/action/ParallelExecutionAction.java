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
package org.talend.designer.core.ui.action;

import java.util.List;

import org.eclipse.gef.commands.Command;
import org.eclipse.gef.ui.actions.SelectionAction;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchPart;
import org.talend.commons.ui.swt.formtools.LabelledText;
import org.talend.core.model.process.IConnection;
import org.talend.core.model.process.IConnectionCategory;
import org.talend.core.model.process.IElementParameter;
import org.talend.core.model.process.IProcess;
import org.talend.core.ui.proposal.TalendProposalUtils;
import org.talend.designer.core.i18n.Messages;
import org.talend.designer.core.model.components.EParameterName;
import org.talend.designer.core.ui.editor.cmd.PropertyChangeCommand;
import org.talend.designer.core.ui.editor.nodes.Node;
import org.talend.designer.core.ui.editor.nodes.NodePart;

/**
 *
 * DOC YeXiaowei class global comment. Detailled comment
 */
public class ParallelExecutionAction extends SelectionAction {

    public static final String ID = "org.talend.designer.core.ui.editor.action.ParallelExecutionAction"; //$NON-NLS-1$

    private static final String TEXT_PARALLEL = Messages.getString("ParallelExecutionCommand.Parallel"); //$NON-NLS-1$

    private boolean parallelEnable;

    private String numberParallel = "0"; //$NON-NLS-1$

    private Node node;

    public ParallelExecutionAction(IWorkbenchPart part) {
        super(part);
        setId(ID);
        setText(TEXT_PARALLEL);
    }

    @Override
    protected boolean calculateEnabled() {
        if (getSelectedObjects() == null || getSelectedObjects().isEmpty()) {
            return false;
        }
        node = getCurrentNode();
        if (node == null) {
            return false;
        }
        IElementParameter enableParallelizeParameter = node.getElementParameter(EParameterName.PARALLELIZE.getName());
        if (enableParallelizeParameter != null) {
            parallelEnable = (Boolean) enableParallelizeParameter.getValue();
        }
        return node.getComponent().canParallelize();

    }

    protected Node getCurrentNode() {
        List parts = getSelectedObjects();
        if (parts.size() != 1) {
            return null;
        }

        Object o = parts.get(0);
        if (o instanceof NodePart) {
            NodePart nodePart = (NodePart) o;
            node = (Node) nodePart.getModel();
            return node;
        }
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.jface.action.Action#run()
     */
    @Override
    public void run() {
        boolean hasFlowConnection = false;
        for (IConnection connection : node.getOutgoingConnections()) {
            if (connection.getLineStyle().hasConnectionCategory(IConnectionCategory.FLOW)) {
                hasFlowConnection = true;
                break;
            }
        }
        if (hasFlowConnection) {
            MessageDialog.openError(getWorkbenchPart().getSite().getShell(), Messages
                    .getString("ParallelExecutionAction.gotLink"), //$NON-NLS-1$
                    Messages.getString("ParallelExecutionAction.noOutputLink")); //$NON-NLS-1$
            return;
        }

        IElementParameter enableParallelizeParameter = node.getElementParameter(EParameterName.PARALLELIZE.getName());
        if (enableParallelizeParameter != null) {
            parallelEnable = (Boolean) enableParallelizeParameter.getValue();
        }
        IElementParameter numberParallelizeParameter = node.getElementParameter(EParameterName.PARALLELIZE_NUMBER.getName());
        if (numberParallelizeParameter != null) {
            numberParallel = (String) numberParallelizeParameter.getValue();
        }

        Dialog dialog = new ParallelDialog(getWorkbenchPart().getSite().getShell());
        if (dialog.open() == Dialog.OK) {
            Command command = new PropertyChangeCommand(node, EParameterName.PARALLELIZE.getName(), parallelEnable);
            execute(command);
        }
    }

    /**
     *
     * DOC YeXiaowei ParallelExecutionAction class global comment. Detailled comment
     */
    class ParallelDialog extends Dialog {

        private Button enableButton;

        private LabelledText numberText;

        protected ParallelDialog(Shell parentShell) {
            super(parentShell);
        }

        /*
         * (non-Javadoc)
         *
         * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
         */
        @Override
        protected Control createDialogArea(Composite parent) {

            Composite bgComposite = new Composite(parent, SWT.NULL);

            GridData gData = new GridData(GridData.FILL_BOTH);
            gData.minimumHeight = 50;
            gData.heightHint = 50;
            gData.minimumWidth = 350;
            gData.widthHint = 350;
            bgComposite.setLayoutData(gData);

            GridLayout gridLayout = new GridLayout();
            gridLayout.numColumns = 3;
            bgComposite.setLayout(gridLayout);

            enableButton = new Button(bgComposite, SWT.CHECK);
            GridData data = new GridData(GridData.FILL_HORIZONTAL);
            data.horizontalSpan = 2;
            enableButton.setLayoutData(data);
            enableButton.setText(EParameterName.PARALLELIZE.getDisplayName());

            Label label = new Label(bgComposite, SWT.NONE);

            numberText = new LabelledText(bgComposite, EParameterName.PARALLELIZE_NUMBER.getDisplayName(), true);

            enableButton.addSelectionListener(new SelectionAdapter() {

                /*
                 * (non-Javadoc)
                 *
                 * @see org.eclipse.swt.events.SelectionAdapter#widgetSelected(org.eclipse.swt.events.SelectionEvent)
                 */
                @Override
                public void widgetSelected(SelectionEvent e) {
                    numberText.setEditable(enableButton.getSelection());
                }
            });

            enableButton.setSelection(parallelEnable);
            numberText.setText(numberParallel);
            numberText.setEditable(parallelEnable);
            // for feature 12372
            IProcess process = node.getProcess();
            TalendProposalUtils.installOn(numberText.getTextControl(), process);

            return bgComposite;
        }

        /*
         * (non-Javadoc)
         *
         * @see org.eclipse.jface.dialogs.Dialog#okPressed()
         */
        @Override
        protected void okPressed() {
            if (numberTextValid()) {
                setParametersValue();
                super.okPressed();
            } else {
                MessageDialog
                        .openError(
                                null,
                                Messages.getString("ParallelExecutionAction.talend"), Messages.getString("ParallelExecutionCommand.numberInvalid")); //$NON-NLS-1$ //$NON-NLS-2$
            }
        }

        /**
         * DOC YeXiaowei Comment method "setParametersValue".
         */
        private void setParametersValue() {
            IElementParameter enableParallelizeParameter = node.getElementParameter(EParameterName.PARALLELIZE.getName());
            if (enableParallelizeParameter != null) {
                enableParallelizeParameter.setValue(enableButton.getSelection());
                parallelEnable = enableButton.getSelection();
            }
            IElementParameter numberParallelizeParameter = node.getElementParameter(EParameterName.PARALLELIZE_NUMBER.getName());
            if (numberParallelizeParameter != null) {
                numberParallelizeParameter.setValue(numberText.getText());
            }
        }

        /**
         * DOC YeXiaowei Comment method "numberTextValid".
         *
         * @return
         */

        private boolean numberTextValid() {
            String text = numberText.getText().trim();
            if (text == null || text.equals("")) { //$NON-NLS-1$
                return false;
            }
            return true;
        }
    }
}
