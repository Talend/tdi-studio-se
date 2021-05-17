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
package org.talend.expressionbuilder.ui;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.talend.commons.ui.runtime.expressionbuilder.IExpressionDataBean;
import org.talend.core.model.process.INode;
import org.talend.expressionbuilder.ExpressionPersistance;
import org.talend.expressionbuilder.i18n.Messages;

/**
 * created by hcyi on Feb 15, 2017 Detailled comment
 *
 */
public class BatchExpressionBuilderDialog extends ExpressionBuilderDialog {

    private Composite container;

    public BatchExpressionBuilderDialog(Shell parentShell, IExpressionDataBean dataBean, INode component) {
        super(parentShell, dataBean, component);
        setShellStyle(this.getShellStyle() | SWT.RESIZE);
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        container = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout();
        layout.marginHeight = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_MARGIN);
        layout.marginWidth = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_MARGIN);
        layout.verticalSpacing = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_SPACING);
        layout.horizontalSpacing = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_SPACING);
        container.setLayout(layout);
        container.setLayoutData(new GridData(GridData.FILL_BOTH));
        applyDialogFont(container);

        final SashForm sashForm = new SashForm(container, SWT.NONE);
        sashForm.setOrientation(SWT.VERTICAL);
        final Composite lowerComposite = new Composite(sashForm, SWT.NONE);
        lowerComposite.setLayout(new FillLayout());
        categoryComposite = new BatchCategoryComposite(lowerComposite, SWT.NONE, manager);

        final Composite upperComposite = new Composite(sashForm, SWT.NONE);
        upperComposite.setLayout(new FillLayout());
        final SashForm upperSashform = new SashForm(upperComposite, SWT.NONE);
        expressionComposite = new BatchExpressionComposite(this, upperSashform, SWT.NONE, dataBean);
        expressionComposite.setExpression(defaultExpression, true);

        final GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
        sashForm.setLayoutData(gridData);
        sashForm.setWeights(new int[] { 4, 1 });
        return container;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.expressionbuilder.ui.IExpressionBuilderDialogController#openDialog()
     */
    @Override
    public void openDialog(Object obj) {
        if (obj instanceof IExpressionDataBean) {
            IExpressionDataBean bean = (IExpressionDataBean) obj;
            setDefaultExpression(bean.getExpression());
            ExpressionPersistance persistance = ExpressionPersistance.getInstance();
            persistance.setOwnerId(bean.getOwnerId());
            persistance.setPath(getExpressionStorePath());
        }
        open();
        setBlockOnOpen(true);
    }

    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        ((GridLayout) parent.getLayout()).numColumns++;
        createButton(parent, IDialogConstants.OK_ID, Messages.getString("ExpressionBuilderDialog.ok.button"), true); //$NON-NLS-1$
        createButton(parent, IDialogConstants.CANCEL_ID, Messages.getString("ExpressionBuilderDialog.cancel.button"), false); //$NON-NLS-1$
    }

    @Override
    public void create() {
        super.create();
        addUndoOperationListener(container);
        isESCClose = true;
    }
}