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
package org.talend.designer.mapper.ui.footer;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.talend.designer.mapper.i18n.Messages;
import org.talend.designer.mapper.managers.MapperManager;
import org.talend.designer.mapper.managers.MapperSettingsManager;
import org.talend.designer.mapper.managers.UIManager;

/**
 * DOC amaumont class global comment. Detailled comment <br/>
 *
 * $Id$
 *
 */
public class FooterComposite extends Composite {

    /**
     * Image registry key for help image (value <code>"dialog_help_image"</code>).
     *
     * @since 3.2
     */
    public static final String DLG_IMG_HELP = "dialog_help_image"; //$NON-NLS-1$

    private MapperManager mapperManager;

    private StatusBar statusBar;

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

        statusBar = new StatusBar(this, SWT.NONE);

        Button applyButton = new Button(this, SWT.NONE);
        applyButton.setEnabled(!mapperManager.componentIsReadOnly());
        applyButton.setText(Messages.getString("FooterComposite.button.APPLY")); //$NON-NLS-1$
        FormData applyFormData = new FormData();
        applyFormData.width = 100;
        applyButton.setLayoutData(applyFormData);
        applyButton.addSelectionListener(new SelectionListener() {

            public void widgetDefaultSelected(SelectionEvent e) {
            }

            public void widgetSelected(SelectionEvent e) {
                MapperSettingsManager.getInstance(mapperManager).saveCurrentSettings();
                uiManager.closeMapper(SWT.APPLICATION_MODAL);
            }

        });

        Button okButton = new Button(this, SWT.NONE);
        okButton.setEnabled(!mapperManager.componentIsReadOnly());
        okButton.setText(Messages.getString("FooterComposite.button.OK")); //$NON-NLS-1$
        FormData okFormData = new FormData();

        okFormData.width = 100;
        okButton.setLayoutData(okFormData);
        okButton.addSelectionListener(new SelectionListener() {

            public void widgetDefaultSelected(SelectionEvent e) {
            }

            public void widgetSelected(SelectionEvent e) {
                MapperSettingsManager.getInstance(mapperManager).saveCurrentSettings();
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

        cancelFormData.right = new FormAttachment(100, -5);
        okFormData.right = new FormAttachment(cancelButton, -5);
        applyFormData.right = new FormAttachment(okButton, -5);

    }

    public StatusBar getStatusBar() {
        return statusBar;
    }

}
