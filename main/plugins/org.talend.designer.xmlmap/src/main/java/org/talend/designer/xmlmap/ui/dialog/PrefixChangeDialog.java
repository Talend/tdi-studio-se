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
package org.talend.designer.xmlmap.ui.dialog;

import org.eclipse.jface.dialogs.TitleAreaDialog;
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

/**
 *
 * created by wchen on 2015年7月9日 Detailled comment
 *
 */
public class PrefixChangeDialog extends TitleAreaDialog {

    private Button applyAllBtn;

    private Button cancelAllBtn;

    private boolean applyAll = true;

    private boolean cancelAll;

    private String rootNodeName;

    private String prefix;

    private String sourceExpression;

    private String targetExpression;

    /**
     * wchen NameSpaceDialog constructor comment.
     *
     * @param parentShell
     */
    public PrefixChangeDialog(Shell parentShell) {
        super(parentShell);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.jface.dialogs.TitleAreaDialog#createDialogArea(org.eclipse.swt.widgets.Composite)
     */
    @Override
    protected Control createDialogArea(Composite parent) {
        super.createDialogArea(parent);
        this.getShell().setText("Root Node Prefix Changed");
        this.setTitle("Imported root Node Prefix Changed");
        this.setMessage("Imported document root node prefix changed , do you want to change output expression and keep the mapping ?");
        Composite composite = new Composite(parent, SWT.BORDER);
        GridLayout layout = new GridLayout();
        layout.marginLeft = 20;
        composite.setLayout(layout);
        composite.setLayoutData(new GridData(GridData.FILL_BOTH));
        Label label = new Label(composite, SWT.NONE);
        label.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        label.setText("Imported document root  has  changed from: ");
        Label text1 = new Label(composite, SWT.NONE);
        text1.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        text1.setText(rootNodeName);
        text1.setForeground(text1.getDisplay().getSystemColor(SWT.COLOR_RED));
        Label text2 = new Label(composite, SWT.NONE);
        text2.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        text2.setText(" to: ");
        Label text3 = new Label(composite, SWT.NONE);
        text3.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        text3.setText(prefix + ":" + rootNodeName);
        text3.setForeground(text1.getDisplay().getSystemColor(SWT.COLOR_RED));
        Label text4 = new Label(composite, SWT.NONE);
        text4.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        text4.setText("Do you wan't to propagate " + prefix + ":" + rootNodeName + " for expression : ");
        Label text5 = new Label(composite, SWT.NONE);
        text5.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        text5.setText(sourceExpression);
        text5.setForeground(text1.getDisplay().getSystemColor(SWT.COLOR_RED));
        Label text6 = new Label(composite, SWT.NONE);
        text6.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        text6.setText(" to keep the mapping ? ");

        applyAllBtn = new Button(composite, SWT.CHECK);
        applyAllBtn.setText("Apply for all expressions");
        applyAllBtn.addSelectionListener(new SelectionAdapter() {

            /*
             * (non-Javadoc)
             *
             * @see org.eclipse.swt.events.SelectionAdapter#widgetSelected(org.eclipse.swt.events.SelectionEvent)
             */
            @Override
            public void widgetSelected(SelectionEvent e) {
                applyAll = applyAllBtn.getSelection();
                if (applyAll) {
                    cancelAllBtn.setSelection(false);
                    cancelAll = false;
                }
            }
        });
        applyAllBtn.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        applyAllBtn.setSelection(true);

        cancelAllBtn = new Button(composite, SWT.CHECK);
        cancelAllBtn.setText("Cancel for all expressions");
        cancelAllBtn.addSelectionListener(new SelectionAdapter() {

            /*
             * (non-Javadoc)
             *
             * @see org.eclipse.swt.events.SelectionAdapter#widgetSelected(org.eclipse.swt.events.SelectionEvent)
             */
            @Override
            public void widgetSelected(SelectionEvent e) {
                cancelAll = cancelAllBtn.getSelection();
                if (cancelAll) {
                    applyAllBtn.setSelection(false);
                    applyAll = false;
                }
            }
        });
        cancelAllBtn.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        return parent;

    }

    public boolean isApplyAll() {
        return this.applyAll;
    }

    public boolean isCancelAll() {
        return this.cancelAll;
    }

    /**
     * Getter for rootNodeName.
     *
     * @return the rootNodeName
     */
    public String getRootNodeName() {
        return this.rootNodeName;
    }

    /**
     * Sets the rootNodeName.
     *
     * @param rootNodeName the rootNodeName to set
     */
    public void setRootNodeName(String rootNodeName) {
        this.rootNodeName = rootNodeName;
    }

    /**
     * Getter for prefix.
     *
     * @return the prefix
     */
    public String getPrefix() {
        return this.prefix;
    }

    /**
     * Sets the prefix.
     *
     * @param prefix the prefix to set
     */
    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    /**
     * Getter for sourceExpression.
     *
     * @return the sourceExpression
     */
    public String getSourceExpression() {
        return this.sourceExpression;
    }

    /**
     * Sets the sourceExpression.
     *
     * @param sourceExpression the sourceExpression to set
     */
    public void setSourceExpression(String sourceExpression) {
        this.sourceExpression = sourceExpression;
    }

    /**
     * Getter for targetExpression.
     *
     * @return the targetExpression
     */
    public String getTargetExpression() {
        return this.targetExpression;
    }

    /**
     * Sets the targetExpression.
     *
     * @param targetExpression the targetExpression to set
     */
    public void setTargetExpression(String targetExpression) {
        this.targetExpression = targetExpression;
    }

}
