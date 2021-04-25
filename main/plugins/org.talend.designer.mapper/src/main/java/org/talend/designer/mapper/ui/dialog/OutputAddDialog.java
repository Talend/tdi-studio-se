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
package org.talend.designer.mapper.ui.dialog;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.talend.core.model.process.IProcess;
import org.talend.core.utils.KeywordsValidator;
import org.talend.designer.mapper.i18n.Messages;
import org.talend.designer.mapper.managers.MapperManager;
import org.talend.designer.mapper.model.table.OutputTable;

/**
 * DOC wchen class global comment. Detailled comment <br/>
 *
 * $Id$
 *
 */
public class OutputAddDialog extends Dialog {

    private MapperManager mapperManager;

    private String[] physicalTables;

    private String tableName;

    private String joinTableName;

    private String errorMessage;

    private boolean isCreatingJoinTable;

    private IInputValidator validator;

    private Button okButton;

    private Text joinTableTxt;

    private Text newOutputTxt;

    private Text errorMessageText;

    private Combo combo;

    private Button newOutputBtn;

    private Button joinTableBtn;

    public OutputAddDialog(Shell parentShell, MapperManager manager, String tableName) {
        super(parentShell);
        setShellStyle(getShellStyle() | SWT.RESIZE);
        this.tableName = tableName;
        this.mapperManager = manager;
        final IProcess process = manager.getAbstractMapComponent().getProcess();

        this.validator = new IInputValidator() {

            public String isValid(String newText) {
                // text
                if (!process.checkValidConnectionName(newText)) {
                    return Messages.getString("UIManager.tableNameIsNotValid"); //$NON-NLS-1$
                }

                if (mapperManager.ERROR_REJECT.equals(newText)) {
                    return "Input is invalid.";
                }

                // combo
                String selectedPhysicalTable = getTableName();
                if ((selectedPhysicalTable == null || selectedPhysicalTable.length() == 0)) {
                    return "A table must be selected!";
                }

                if (KeywordsValidator.isKeyword(newText) || KeywordsValidator.isSqlKeyword(newText)) {
                    return "Input is invalid.";
                }
                return null;
            }

        };
    }

    /*
     * (non-Javadoc) Method declared on Dialog.
     */
    protected void buttonPressed(int buttonId) {
        if (buttonId == IDialogConstants.OK_ID) {
            // value = joinTableTxt.getText();
        } else {
            // value = null;
        }
        super.buttonPressed(buttonId);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.jface.window.Window#configureShell(org.eclipse.swt.widgets.Shell)
     */
    protected void configureShell(Shell shell) {
        super.configureShell(shell);
        shell.setText("Add a output");

    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.jface.dialogs.Dialog#createButtonsForButtonBar(org.eclipse.swt.widgets.Composite)
     */
    protected void createButtonsForButtonBar(Composite parent) {
        // create OK and Cancel buttons by default
        okButton = createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
        createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
        // do this here because setting the text will set enablement on the ok
        // button
        // combo.setFocus();
        // if (value != null) {
        // joinTableTxt.setText(value);
        // joinTableTxt.selectAll();
        // }
        if (validator != null) {
            validator.isValid("");
        }
    }

    /*
     * (non-Javadoc) Method declared on Dialog.
     */
    protected Control createDialogArea(Composite parent) {
        // create composite
        Composite composite = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout();
        layout.numColumns = 5;
        composite.setLayout(layout);
        GridData layoutData = new GridData(GridData.FILL_BOTH);
        layoutData.widthHint = 400;
        composite.setLayoutData(layoutData);

        newOutputBtn = new Button(composite, SWT.RADIO);

        newOutputBtn.setSelection(true);
        Label label = new Label(composite, SWT.WRAP);
        label.setText("New output"); //$NON-NLS-1$

        newOutputTxt = new Text(composite, SWT.SINGLE | SWT.BORDER);
        layoutData = new GridData(GridData.FILL_HORIZONTAL);
        layoutData.horizontalSpan = 3;
        newOutputTxt.setLayoutData(layoutData);
        newOutputTxt.setText(tableName);

        joinTableBtn = new Button(composite, SWT.RADIO);

        Label label1 = new Label(composite, SWT.WRAP);
        label1.setText("Create join table from"); //$NON-NLS-1$
        combo = new Combo((Composite) composite, SWT.DROP_DOWN | SWT.READ_ONLY);
        combo.setItems(getPhysicalTables());
        combo.setEnabled(false);

        Label label2 = new Label(composite, SWT.WRAP);
        label2.setText("Named");
        label2.setFont(parent.getFont());

        joinTableTxt = new Text(composite, SWT.SINGLE | SWT.BORDER);
        layoutData = new GridData(GridData.FILL_HORIZONTAL);
        joinTableTxt.setLayoutData(layoutData);
        joinTableTxt.setText(tableName);

        joinTableTxt.setEnabled(false);

        errorMessageText = new Text(composite, SWT.READ_ONLY | SWT.SINGLE);
        layoutData = new GridData(GridData.FILL_HORIZONTAL);
        layoutData.horizontalSpan = 5;
        errorMessageText.setLayoutData(layoutData);
        errorMessageText.setBackground(errorMessageText.getDisplay().getSystemColor(SWT.COLOR_WIDGET_BACKGROUND));

        setErrorMessage(errorMessage);
        applyDialogFont(composite);

        newOutputBtn.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                adaptToEnable();
                tableName = newOutputTxt.getText();
                validateInput(newOutputTxt.getText());
                isCreatingJoinTable = false;
            }

        });
        newOutputTxt.addModifyListener(new ModifyListener() {

            public void modifyText(ModifyEvent e) {
                tableName = newOutputTxt.getText();
                validateInput(newOutputTxt.getText());
            }
        });

        combo.addSelectionListener(new SelectionListener() {

            public void widgetDefaultSelected(SelectionEvent e) {
            }

            public void widgetSelected(SelectionEvent e) {
                tableName = combo.getText();
                validateInput(joinTableTxt.getText());
            }

        });

        joinTableBtn.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                adaptToEnable();
                tableName = combo.getText();
                joinTableName = joinTableTxt.getText();
                validateInput(joinTableTxt.getText());
                isCreatingJoinTable = true;
            }

        });

        joinTableTxt.addModifyListener(new ModifyListener() {

            public void modifyText(ModifyEvent e) {
                joinTableName = joinTableTxt.getText();
                validateInput(joinTableTxt.getText());

            }
        });
        return composite;
    }

    private void adaptToEnable() {
        if (newOutputBtn.getSelection()) {
            newOutputTxt.setEnabled(true);
            combo.setEnabled(false);
            joinTableTxt.setEnabled(false);
        } else if (joinTableBtn.getSelection()) {
            newOutputTxt.setEnabled(false);
            combo.setEnabled(true);
            joinTableTxt.setEnabled(true);
        }
    }

    /**
     * Validates the input.
     * <p>
     * The default implementation of this framework method delegates the request to the supplied input validator object;
     * if it finds the input invalid, the error message is displayed in the dialog's message line. This hook method is
     * called whenever the text changes in the input field.
     * </p>
     */
    protected boolean validateInput(String text) {
        String errorMessage = null;
        if (validator != null) {
            errorMessage = validator.isValid(text);
        }
        // Bug 16256: important not to treat "" (blank error) the same as null
        // (no error)
        setErrorMessage(errorMessage);
        return errorMessage == null;
    }

    /**
     * Sets or clears the error message. If not <code>null</code>, the OK button is disabled.
     *
     * @param errorMessage the error message, or <code>null</code> to clear
     * @since 3.0
     */
    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
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

    public String getTableName() {
        return this.tableName;
    }

    public String getJoinTableName() {
        return this.joinTableName;
    }

    /**
     * DOC amaumont Comment method "isSameAsPhysicalTable".
     *
     * @param name
     */
    public boolean isSameAsPhysicalTable(String name) {
        boolean newTextIsSameAsTable = false;
        for (String table : getPhysicalTables()) {
            if (table.equals(name)) {
                newTextIsSameAsTable = true;
                break;
            }
        }
        return newTextIsSameAsTable;
    }

    public String[] getPhysicalTables() {
        if (physicalTables == null) {
            List<OutputTable> outputTables = this.mapperManager.getOutputTables();
            List<String> names = new ArrayList<String>();
            for (OutputTable table : outputTables) {
                if (table.getIsJoinTableOf() == null && !((OutputTable) table).isErrorRejectTable()) {
                    names.add(table.getName());
                }
            }
            physicalTables = names.toArray(new String[names.size()]);
        }
        return physicalTables;
    }

    public boolean isCreatingJoinTable() {
        return this.isCreatingJoinTable;
    }
}
