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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.talend.commons.runtime.model.expressionbuilder.Variable;
import org.talend.commons.ui.runtime.expressionbuilder.IExpressionDataBean;
import org.talend.core.model.process.INode;
import org.talend.expressionbuilder.ExpressionPersistance;
import org.talend.expressionbuilder.i18n.Messages;

/**
 * DOC hcyi class global comment. Detailled comment
 */
public class PigExpressionBuilderDialog extends ExpressionBuilderDialog {

    private Composite container;

    public PigExpressionBuilderDialog(Shell parentShell, IExpressionDataBean dataBean, INode component, List<Variable> vars,
            boolean hasPigDataFuCategory) {
        super(parentShell, dataBean, component);
        setShellStyle(this.getShellStyle() | SWT.RESIZE);
        this.defaultVariables = vars;
        manager.setHasPigDataFuCategory(hasPigDataFuCategory);
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

        final Composite upperComposite = new Composite(sashForm, SWT.NONE);
        upperComposite.setLayout(new FillLayout());

        final SashForm upperSashform = new SashForm(upperComposite, SWT.NONE);

        expressionComposite = new PigExpressionComposite(this, upperSashform, SWT.NONE, dataBean);
        expressionComposite.setExpression(defaultExpression, true);

        testComposite = new PigInputTableComposite(upperSashform, SWT.NONE);
        testComposite.addVariables(defaultVariables);
        upperSashform.setWeights(new int[] { 3, 2 });

        final Composite lowerComposite = new Composite(sashForm, SWT.NONE);
        lowerComposite.setLayout(new FillLayout());

        categoryComposite = new PigCategoryComposite(lowerComposite, SWT.NONE, manager);
        final GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
        sashForm.setLayoutData(gridData);
        sashForm.setWeights(new int[] { 3, 2 });
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
            List<Variable> vars = new ArrayList<Variable>();
            IExpressionDataBean bean = (IExpressionDataBean) obj;
            setDefaultExpression(bean.getExpression());
            if (bean.getVariables() != null) {
                vars.addAll(bean.getVariables());
            }
            ExpressionPersistance persistance = ExpressionPersistance.getInstance();
            persistance.setOwnerId(bean.getOwnerId());
            persistance.setPath(getExpressionStorePath());
            for (Variable var1 : persistance.loadExpression().getVariables()) {
                boolean needAdd = true;
                for (Variable var2 : vars) {
                    if (var1.getName() != null && var1.getName().equals(var2.getName())) {
                        needAdd = false;
                        break;
                    }
                }
                if (var1.getName() != null && needAdd) {
                    vars.add(var1);
                }
            }
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
