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
package org.talend.designer.dbmap.ui.footer;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.talend.designer.dbmap.i18n.Messages;
import org.talend.designer.dbmap.managers.MapperManager;
import org.talend.designer.dbmap.managers.UIManager;

/**
 * DOC amaumont class global comment. Detailled comment <br/>
 *
 * $Id: FooterComposite.java 1782 2007-02-03 07:57:38Z bqian $
 *
 */
public class FooterComposite extends Composite {

    private MapperManager mapperManager;

    /**
     * DOC amaumont FooterComposite constructor comment.
     *
     * @param parent
     * @param style
     * @param mapperManager
     */
    public FooterComposite(Composite parent, int style, MapperManager mapperManager) {
        super(parent, style);
        this.mapperManager = mapperManager;
        createComponents();
    }

    /**
     * DOC amaumont Comment method "createComponents".
     */
    private void createComponents() {

        final UIManager uiManager = mapperManager.getUiManager();

        GridData footerCompositeGridData = new GridData(GridData.FILL_HORIZONTAL);
        this.setLayoutData(footerCompositeGridData);

        FormLayout formLayout = new FormLayout();
        formLayout.spacing = 15;
        this.setLayout(formLayout);

        Button okButton = new Button(this, SWT.NONE);
        okButton.setText(Messages.getString("FooterComposite.button.OK")); //$NON-NLS-1$
        FormData okFormData = new FormData();
        okFormData.width = 100;
        okButton.setLayoutData(okFormData);
        okButton.setEnabled(!mapperManager.componentIsReadOnly());
        okButton.addSelectionListener(new SelectionListener() {

            public void widgetDefaultSelected(SelectionEvent e) {
            }

            public void widgetSelected(SelectionEvent e) {
                uiManager.closeMapper(SWT.OK);
            }

        });

        // final Button applyButton = new Button(this, SWT.NONE);
        // applyButton.setText("Apply");
        // FormData applyFormData = new FormData();
        // applyButton.setLayoutData(applyFormData);
        // applyButton.addSelectionListener(new SelectionListener() {
        //
        // public void widgetDefaultSelected(SelectionEvent e) {
        // }
        //
        // public void widgetSelected(SelectionEvent e) {
        // MessageBox messageBox = new MessageBox(uiManager.retrieveShellParent(footerComposite));
        // messageBox.setText("Info");
        // messageBox.setMessage("Not implemented yet !");
        // messageBox.open();
        // }
        //
        // });

        Button cancelButton = new Button(this, SWT.NONE);
        cancelButton.setText(Messages.getString("FooterComposite.button.Cancel")); //$NON-NLS-1$
        cancelButton.addSelectionListener(new SelectionListener() {

            public void widgetDefaultSelected(SelectionEvent e) {
            }

            public void widgetSelected(SelectionEvent e) {
                uiManager.closeMapper(SWT.CANCEL);
            }

        });
        FormData cancelFormData = new FormData();
        cancelFormData.width = 100;
        cancelButton.setLayoutData(cancelFormData);

        // applyFormData.right = new FormAttachment(100);
        // cancelFormData.right = new FormAttachment(applyButton, -5);
        cancelFormData.right = new FormAttachment(100, -5);
        okFormData.right = new FormAttachment(cancelButton, -5);

    }

}
