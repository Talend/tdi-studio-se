package org.talend.designer.core.ui.dialog.mergeorder;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/*
 * gcui:this dialog only used on tELTInput components.
 * 
 * In this dialog include TableName and SchemaName.
 */
public class ConnectionTableAndSchemaNameDialog extends Dialog {

    private String dialogTitle;

    private String dialogMessage;

    private Text tableName;

    private Text schemaName;

    private String tableNameValue = "\"\""; //$NON-NLS-1$

    private String schemaNameValue = "\"\""; //$NON-NLS-1$

    private Button okButton;

    protected ConnectionTableAndSchemaNameDialog(Shell parentShell) {
        super(parentShell);
        // TODO Auto-generated constructor stub
    }

    public ConnectionTableAndSchemaNameDialog(Shell parentShell, String dialogTitle, String dialogMessage,
            String schemaInitialValue) {
        this(parentShell);
        this.dialogTitle = dialogTitle;
        this.dialogMessage = dialogMessage;
        if (schemaInitialValue != null) {
            schemaNameValue = schemaInitialValue;
        }
    }

    @Override
    protected void configureShell(Shell shell) {
        super.configureShell(shell);
        if (dialogTitle != null) {
            shell.setText(dialogTitle);
        }
    }

    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        okButton = createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
        createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);

        // okButton.setEnabled(!tableNameValue.equals(""));
        if (tableNameValue != null && schemaNameValue != null) {
            tableName.setText(tableNameValue);
            tableName.selectAll();
            schemaName.setText(schemaNameValue);
            schemaName.selectAll();
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
     */
    @Override
    protected Control createDialogArea(Composite parent) {
        Composite composite = (Composite) super.createDialogArea(parent);
        // create message
        if (dialogMessage != null) {
            Label label = new Label(composite, SWT.WRAP);
            label.setText(dialogMessage);
            GridData data = new GridData(GridData.GRAB_HORIZONTAL
            /* | GridData.GRAB_VERTICAL */| GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_CENTER);
            data.widthHint = convertHorizontalDLUsToPixels(IDialogConstants.MINIMUM_MESSAGE_AREA_WIDTH);
            label.setLayoutData(data);
            label.setFont(parent.getFont());
        }

        Label tableNamelabel = new Label(composite, SWT.WRAP);
        tableNamelabel.setText("Default Table Name: "); //$NON-NLS-1$
        tableName = createText(composite);
        Label schemaNamelabel = new Label(composite, SWT.WRAP);
        schemaNamelabel.setText("Default Schema Name: "); //$NON-NLS-1$
        schemaName = createText(composite);

        this.addListener();
        applyDialogFont(composite);
        return composite;
    }

    protected Text createText(Composite composite) {
        Text text = new Text(composite, SWT.SINGLE | SWT.BORDER);
        text.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL));
        return text;
    }

    @Override
    protected void buttonPressed(int buttonId) {
        if (buttonId == IDialogConstants.OK_ID) {
            tableNameValue = tableName.getText();
            schemaNameValue = schemaName.getText();
        } else {
            tableNameValue = null;
            schemaNameValue = null;
        }
        super.buttonPressed(buttonId);
    }

    private void addListener() {
        tableName.addModifyListener(new ModifyListener() {

            @Override
            public void modifyText(ModifyEvent e) {
                String result = tableName.getText();
                if (result.length() != 0) {
                    ConnectionTableAndSchemaNameDialog.this.getButton(IDialogConstants.OK_ID).setEnabled(true);
                } else {
                    ConnectionTableAndSchemaNameDialog.this.getButton(IDialogConstants.OK_ID).setEnabled(false);
                }
            }
        });
    }

    public String getTableName() {
        return tableNameValue;
    }

    public String getSchemaName() {
        return schemaNameValue;
    }

    public void isOkButton() {
        Control button = getButton(IDialogConstants.OK_ID);
        if (button != null) {
            button.setEnabled(tableName.getText() != null);
        }
    }
}
