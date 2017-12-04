// ============================================================================
//
// Copyright (C) 2006-2017 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.designer.hl7.ui.footer;

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
import org.talend.designer.hl7.i18n.Messages;
import org.talend.designer.hl7.managers.HL7Manager;

/**
 * amaumont class global comment. Detailled comment <br/>
 * 
 * $Id: FooterComposite.java,v 1.1 2007/06/12 07:20:39 gke Exp $
 * 
 */
public class FooterComposite extends Composite {

    private HL7Manager hl7Manager;

    private Composite composite;

    public FooterComposite(Composite parent, int style, HL7Manager generatorManager) {
        super(parent, style);
        this.hl7Manager = generatorManager;
        createComponents();
        this.composite = parent;
    }

    public FooterComposite(Composite parent, int style) {
        this(parent, style, null);
        this.composite = parent;
    }

    private void createComponents() {

        GridData footerCompositeGridData = new GridData(GridData.FILL_HORIZONTAL);
        this.setLayoutData(footerCompositeGridData);

        FormLayout formLayout = new FormLayout();
        this.setLayout(formLayout);

        Button okButton = new Button(this, SWT.NONE);
        okButton.setText(Messages.getString("FooterComposite.0")); //$NON-NLS-1$
        FormData okFormData = new FormData();
        Point minSize = okButton.computeSize(SWT.DEFAULT, SWT.DEFAULT, true);
        okFormData.width = Math.max(IDialogConstants.BUTTON_WIDTH, minSize.x);

        okButton.setLayoutData(okFormData);
        okButton.addSelectionListener(new SelectionListener() {

            public void widgetDefaultSelected(SelectionEvent e) {
            }

            public void widgetSelected(SelectionEvent e) {
                // if (hl7Manager.getUiManager().validateRootElement()) {
                hl7Manager.getUiManager().closeHL7(SWT.OK);
                // } else {
                // MessageDialog warningMessageDialog = new MessageDialog(composite.getShell(), Messages
                //                            .getString("FooterComposite.RootElementError.Title"), null, //$NON-NLS-1$
                //                            Messages.getString("FooterComposite.RootElementError.Message"), MessageDialog.ERROR, //$NON-NLS-1$
                //                            new String[] { Messages.getString("FooterComposite.0") }, 0); //$NON-NLS-1$
                // warningMessageDialog.open();
                // }
            }

        });

        Button cancelButton = new Button(this, SWT.NONE);
        cancelButton.setText(Messages.getString("FooterComposite.1")); //$NON-NLS-1$
        cancelButton.addSelectionListener(new SelectionListener() {

            public void widgetDefaultSelected(SelectionEvent e) {
            }

            public void widgetSelected(SelectionEvent e) {
                hl7Manager.getUiManager().closeHL7(SWT.CANCEL);
            }

        });

        FormData cancelFormData = new FormData();
        minSize = cancelButton.computeSize(SWT.DEFAULT, SWT.DEFAULT, true);
        cancelFormData.width = Math.max(IDialogConstants.BUTTON_WIDTH, minSize.x);
        cancelButton.setLayoutData(cancelFormData);

        Button autoMapButton = new Button(this, SWT.NONE);
        if (hl7Manager != null) {
            boolean canModify = hl7Manager.getHl7Component().getProcess().isReadOnly();
            if (hl7Manager.getHl7Component().getOriginalNode().getJobletNode() != null) {
                canModify = hl7Manager.getHl7Component().getOriginalNode().isReadOnly();
            }
            if (canModify) {
                autoMapButton.setEnabled(false);
            }
        }
        autoMapButton.setToolTipText(Messages.getString("FooterComposite.AutoMapTip")); //$NON-NLS-1$
        autoMapButton.setText(Messages.getString("FooterComposite.AutoMap")); //$NON-NLS-1$
        autoMapButton.addSelectionListener(new SelectionListener() {

            public void widgetDefaultSelected(SelectionEvent e) {
            }

            public void widgetSelected(SelectionEvent e) {
                hl7Manager.getUiManager().autoMap(hl7Manager.getCurrentSchema(false));
            }

        });
        FormData autoMapFormData = new FormData();
        minSize = autoMapButton.computeSize(SWT.DEFAULT, SWT.DEFAULT, true);
        autoMapFormData.width = Math.max(IDialogConstants.BUTTON_WIDTH, minSize.x);
        autoMapButton.setLayoutData(autoMapFormData);

        cancelFormData.right = new FormAttachment(100, -5);
        okFormData.right = new FormAttachment(cancelButton, -5);
        // autoMapFormData.left = new FormAttachment(0, 5);

    }
}
