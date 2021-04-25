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
package org.talend.repository.preference;

import java.util.Collections;
import java.util.List;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.talend.repository.i18n.Messages;

/**
 * DOC tguiu class global comment. Detailled comment <br/>
 *
 * $Id$
 *
 */
public class StatusDialog extends Dialog {

    private String label = ""; //$NON-NLS-1$

    private String code = ""; //$NON-NLS-1$

    private Button okButton;

    private Text labelText;

    private Text codeText;

    private Label errorMessageText;

    private boolean creation = false;

    private final List existingCodes;

    public StatusDialog(Shell parentShell, List existingCodes) {
        this(parentShell, existingCodes, null, null);
        creation = true;
    }

    public StatusDialog(Shell parentShell, List existingCodes, String initialCode, String initialLabel) {
        super(parentShell);
        this.existingCodes = existingCodes == null ? Collections.EMPTY_LIST : existingCodes;
        code = initialCode == null ? "" : initialCode; //$NON-NLS-1$
        label = initialLabel == null ? "" : initialLabel; //$NON-NLS-1$
    }

    @Override
    protected void buttonPressed(int buttonId) {
        if (buttonId == IDialogConstants.OK_ID) {
            code = codeText.getText();
            label = labelText.getText();
        } else {
            code = ""; //$NON-NLS-1$
            label = ""; //$NON-NLS-1$
        }
        super.buttonPressed(buttonId);
    }

    @Override
    protected void configureShell(Shell shell) {
        super.configureShell(shell);
        if (creation) {
            shell.setText(Messages.getString("StatusDialog.shellText.createNewStatus")); //$NON-NLS-1$
        } else {
            shell.setText(Messages.getString("StatusDialog.shellText.editStatus")); //$NON-NLS-1$
        }
    }

    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        // create OK and Cancel buttons by default
        okButton = createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
        createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
        // do this here because setting the text will set enablement on the ok
        // button
        if (code != null) {
            codeText.setText(code);
            codeText.selectAll();
        }
        if (label != null) {
            labelText.setText(label);
        }
        if (creation) {
            codeText.setFocus();
        } else {
            codeText.setEnabled(false);
            labelText.setFocus();
        }
    }

    /*
     * (non-Javadoc) Method declared on Dialog.
     */
    @Override
    protected Control createDialogArea(Composite parent) {
        // create composite
        Composite composite = (Composite) super.createDialogArea(parent);
        // create message
        // if (message != null) {
        // Label label = new Label(composite, SWT.WRAP);
        // label.setText(message);
        // GridData data = new GridData(GridData.GRAB_HORIZONTAL
        // | GridData.GRAB_VERTICAL | GridData.HORIZONTAL_ALIGN_FILL
        // | GridData.VERTICAL_ALIGN_CENTER);
        // data.widthHint = convertHorizontalDLUsToPixels(IDialogConstants.MINIMUM_MESSAGE_AREA_WIDTH);
        // label.setLayoutData(data);
        // label.setFont(parent.getFont());
        // }
        GridLayout layout = new GridLayout();
        layout.numColumns = 3;
        composite.setLayout(layout);
        Label label1 = new Label(composite, SWT.None);
        label1.setText(Messages.getString("StatusDialog.labelText.code")); //$NON-NLS-1$
        // label.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_BEGINNING));
        GridData data = new GridData(GridData.GRAB_VERTICAL | GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_CENTER);
        label1.setLayoutData(data);
        label1.setFont(parent.getFont());
        codeText = new Text(composite, SWT.SINGLE | SWT.BORDER);
        data = new GridData();
        data.widthHint = convertHorizontalDLUsToPixels(30);
        codeText.setLayoutData(data);
        codeText.addModifyListener(new ModifyListener() {

            public void modifyText(ModifyEvent e) {
                validateInput();
            }
        });
        label1 = new Label(composite, SWT.WRAP);
        data = new GridData(GridData.FILL_HORIZONTAL);
        label1.setLayoutData(data);

        label1 = new Label(composite, SWT.WRAP);
        label1.setText(Messages.getString("StatusDialog.labelText.label")); //$NON-NLS-1$
        label1.setFont(parent.getFont());
        labelText = new Text(composite, SWT.SINGLE | SWT.BORDER);
        data = new GridData(GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL);
        data.horizontalSpan = 2;
        labelText.setLayoutData(data);
        labelText.addModifyListener(new ModifyListener() {

            public void modifyText(ModifyEvent e) {
                validateInput();
            }
        });
        errorMessageText = new Label(composite, SWT.NONE);
        errorMessageText.setLayoutData(data);
        errorMessageText.setBackground(errorMessageText.getDisplay().getSystemColor(SWT.COLOR_WIDGET_BACKGROUND));

        applyDialogFont(composite);
        return composite;
    }

    protected Button getOkButton() {
        return okButton;
    }

    protected void validateInput() {
        String errorMessage = null;
        if (creation && (codeText.getText().length() < 1 || codeText.getText().length() > 5)) {
            errorMessage = Messages.getString("StatusDialog.errorMessage.codeLetters1"); //$NON-NLS-1$
        } else if (creation && existingCodes.contains(codeText.getText())) {
            errorMessage = Messages.getString("StatusDialog.errorMessage.codeUsed"); //$NON-NLS-1$
        } else if (labelText.getText().equals("")) { //$NON-NLS-1$
            errorMessage = "Label cannot be empty."; //$NON-NLS-1$
        }
        setErrorMessage(errorMessage);
    }

    public void setErrorMessage(String errorMessage) {
        if (errorMessageText != null && !errorMessageText.isDisposed()) {
            errorMessageText.setText(errorMessage == null ? "" : errorMessage); //$NON-NLS-1$
            errorMessageText.getParent().update();
            // Access the ok button by id, in case clients have overridden button creation.
            // See https://bugs.eclipse.org/bugs/show_bug.cgi?id=113643
            Control button = getButton(IDialogConstants.OK_ID);
            if (button != null) {
                button.setEnabled(errorMessage == null);
            }
        }
    }

    /**
     * Getter for code.
     *
     * @return the code
     */
    public String getCode() {
        return this.code;
    }

    /**
     * Getter for label.
     *
     * @return the label
     */
    public String getLabel() {
        return this.label;
    }
}
