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
package org.talend.designer.rowgenerator.ui.footer;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.talend.designer.rowgenerator.i18n.Messages;
import org.talend.designer.rowgenerator.managers.RowGeneratorManager;
import org.talend.designer.rowgenerator.managers.UIManager;
import org.talend.designer.rowgenerator.ui.tabs.FunParaTableView2;

/**
 * amaumont class global comment. Detailled comment <br/>
 *
 * $Id: FooterComposite.java,v 1.3 2007/01/31 06:17:55 pub Exp $
 *
 */
public class FooterComposite extends Composite {

    private final RowGeneratorManager generatorManager;

    /**
     * amaumont FooterComposite constructor comment.
     *
     * @param parent
     * @param style
     * @param mapperManager
     */
    public FooterComposite(Composite parent, int style, RowGeneratorManager generatorManager) {
        super(parent, style);
        this.generatorManager = generatorManager;
        createComponents();
    }

    /**
     * amaumont Comment method "createComponents".
     */
    private void createComponents() {

        final UIManager uiManager = generatorManager.getUiManager();

        GridData footerCompositeGridData = new GridData(GridData.FILL_HORIZONTAL);
        this.setLayoutData(footerCompositeGridData);

        FormLayout formLayout = new FormLayout();
        this.setLayout(formLayout);

        Button okButton = new Button(this, SWT.NONE);
        okButton.setText(Messages.getString("FooterComposite.OkButton.Text")); //$NON-NLS-1$
        FormData okFormData = new FormData();
        Point minSize = okButton.computeSize(SWT.DEFAULT, SWT.DEFAULT, true);
        okFormData.width = Math.max(IDialogConstants.BUTTON_WIDTH, minSize.x);

        okButton.setLayoutData(okFormData);
        okButton.addSelectionListener(new SelectionListener() {

            public void widgetDefaultSelected(SelectionEvent e) {
            }

            public void widgetSelected(SelectionEvent e) {
                FunParaTableView2 editor = uiManager.getGeneratorUI().getTabFolderEditors().getParameterEditor();
                editor.notifyOkPressed();
                uiManager.closeRowGenerator(SWT.OK, false);
            }

        });

        Button cancelButton = new Button(this, SWT.NONE);
        cancelButton.setText(Messages.getString("FooterComposite.CancelButton.Text")); //$NON-NLS-1$
        cancelButton.addSelectionListener(new SelectionListener() {

            public void widgetDefaultSelected(SelectionEvent e) {
            }

            public void widgetSelected(SelectionEvent e) {
                uiManager.closeRowGenerator(SWT.CANCEL, false);
            }

        });
        FormData cancelFormData = new FormData();
        minSize = cancelButton.computeSize(SWT.DEFAULT, SWT.DEFAULT, true);
        cancelFormData.width = Math.max(IDialogConstants.BUTTON_WIDTH, minSize.x);
        cancelButton.setLayoutData(cancelFormData);

        cancelFormData.right = new FormAttachment(100, -5);
        okFormData.right = new FormAttachment(cancelButton, -5);

    }
}
