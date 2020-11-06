// ============================================================================
//
// Copyright (C) 2006-2019 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.designer.dbmap.ui.dialog;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.talend.core.model.process.Problem;
import org.talend.core.utils.KeywordsValidator;
import org.talend.designer.abstractmap.model.table.IDataMapTable;
import org.talend.designer.dbmap.i18n.Messages;
import org.talend.designer.dbmap.managers.MapperManager;
import org.talend.designer.dbmap.managers.UIManager;
import org.talend.designer.dbmap.model.table.InputTable;
import org.talend.designer.dbmap.ui.visualmap.table.InputDataMapTableView;
import org.talend.designer.dbmap.utils.problems.ProblemsAnalyser;

/**
 * DOC amaumont class global comment. Detailled comment <br/>
 *
 * $Id$
 *
 */
public class AliasDialog {

    private String tableName;

    private MapperManager mapperManager;

    private String alias;

    private String[] physicalTables;

    private AliasInternalDialog aliasInternalDialog;

    private String[] aliases;

    private String[] visibleTables;

    private boolean update = false;

    ProblemsAnalyser problemsAnalyser;

    /**
     * DOC amaumont AliasDialog constructor comment.
     *
     * @param manager
     */
    public AliasDialog(MapperManager manager, String[] physicalTables, String[] aliases, String[] visibleTables, boolean update) {
        this.mapperManager = manager;
        this.physicalTables = physicalTables;
        this.aliases = aliases;
        this.visibleTables = visibleTables;
        this.update = update;
        problemsAnalyser = new ProblemsAnalyser(mapperManager);
    }

    public boolean open() {

        UIManager uiManager = this.mapperManager.getUiManager();
        // String inputTableName = getCurrentSelectedInputTableView().getDataMapTable().getName();
        //
        String proposedAlias = ""; //$NON-NLS-1$

        IInputValidator inputValidator = new IInputValidator() {

            @Override
            public String isValid(String newText) {

                String selectedPhysicalTable = aliasInternalDialog.getTableName();

                if (newText.length() == 0 && isSameAsVisibleTableName(selectedPhysicalTable)) {// tableManager.
                    // getInputTableFromAlias
                    // (newText)
                    // != null) {
                    // return Messages.getString("AliasDialog.aliasIsInvalid"); //$NON-NLS-1$
                    return Messages.getString("AliasDialog.aliasAlreadyExists1", new Object[] { selectedPhysicalTable }); //$NON-NLS-1$
                }
                if (selectedPhysicalTable == null || selectedPhysicalTable.length() == 0) {
                    return Messages.getString("AliasDialog.tableMustBeSelected1"); //$NON-NLS-1$
                }
                if (isSameAsVisibleTableName(newText.trim())) {
                    if (update && newText.equalsIgnoreCase(newText.trim())) {
                        return null;
                    }
                    return Messages.getString("AliasDialog.aliasAlreadyExists1", new Object[] { newText }); //$NON-NLS-1$
                }
                if (KeywordsValidator.isKeyword(newText) || KeywordsValidator.isSqlKeyword(newText)
                        || newText.trim().contains(" ")) { //$NON-NLS-1$
                    return Messages.getString("AliasDialog.inputValid"); //$NON-NLS-1$
                }
                return null;
            }

        };
        aliasInternalDialog = new AliasInternalDialog(mapperManager.getUiManager().getShell(),
                update ? Messages.getString("AliasDialog.renameAlias") : Messages.getString("AliasDialog.addTable"), //$NON-NLS-1$ //$NON-NLS-2$
                Messages.getString("AliasDialog.alias"), proposedAlias, inputValidator); //$NON-NLS-1$

        int response = aliasInternalDialog.open();
        if (response == InputDialog.OK) {
            this.tableName = aliasInternalDialog.getTableName();
            this.alias = aliasInternalDialog.getValue();
            return true;
        }
        return false;
    }

    public String getAlias() {
        return this.alias;
    }

    /**
     * Getter for tableName.
     *
     * @return the tableName
     */
    public String getTableName() {
        return this.tableName;
    }

    /**
     * Sets the tableName.
     *
     * @param tableName the tableName to set
     */
    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    /**
     *
     * DOC amaumont AliasDialog class global comment. Detailled comment <br/>
     *
     * $Id$
     *
     */
    class AliasInternalDialog extends Dialog {

        /**
         * The title of the dialog.
         */
        private String title;

        /**
         * The message to display, or <code>null</code> if none.
         */
        private String message;

        /**
         * The input value; the empty string by default.
         */
        private String value = "";//$NON-NLS-1$

        /**
         * The input validator, or <code>null</code> if none.
         */
        private IInputValidator validator;

        /**
         * Ok button widget.
         */
        private Button okButton;

        private Label warnMessageText;

        /**
         * Input text widget.
         */
        private Text text;

        /**
         * Error message label widget.
         */
        private Label errorMessageText;

        /**
         * Error message string.
         */
        private String errorMessage;

        private String tableName;

        private String internalTableName;

        private Combo combo;

        /**
         * Creates an input dialog with OK and Cancel buttons. Note that the dialog will have no visual representation
         * (no widgets) until it is told to open.
         * <p>
         * Note that the <code>open</code> method blocks for input dialogs.
         * </p>
         *
         * @param parentShell the parent shell, or <code>null</code> to create a top-level shell
         * @param dialogTitle the dialog title, or <code>null</code> if none
         * @param dialogMessage the dialog message, or <code>null</code> if none
         * @param initialValue the initial input value, or <code>null</code> if none (equivalent to the empty string)
         * @param validator an input validator, or <code>null</code> if none
         */
        public AliasInternalDialog(Shell parentShell, String dialogTitle, String dialogMessage, String initialValue,
                IInputValidator validator) {
            super(parentShell);
            setShellStyle(getShellStyle() | SWT.APPLICATION_MODAL | SWT.RESIZE);
            this.title = dialogTitle;
            message = dialogMessage;
            if (initialValue == null) {
                value = "";//$NON-NLS-1$
            } else {
                value = initialValue;
            }
            this.validator = validator;
        }

        /*
         * (non-Javadoc) Method declared on Dialog.
         */
        @Override
        protected void buttonPressed(int buttonId) {
            if (buttonId == IDialogConstants.OK_ID) {
                value = text.getText();
            } else {
                value = null;
            }
            super.buttonPressed(buttonId);
        }

        /*
         * (non-Javadoc)
         *
         * @see org.eclipse.jface.window.Window#configureShell(org.eclipse.swt.widgets.Shell)
         */
        @Override
        protected void configureShell(Shell shell) {
            super.configureShell(shell);
            shell.setMinimumSize(300, 300);
            if (title != null) {
                shell.setText(title);
            }
        }

        /*
         * (non-Javadoc)
         *
         * @see org.eclipse.jface.dialogs.Dialog#createButtonsForButtonBar(org.eclipse.swt.widgets.Composite)
         */
        @Override
        protected void createButtonsForButtonBar(Composite parent) {
            // create OK and Cancel buttons by default
            okButton = createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
            createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
            // do this here because setting the text will set enablement on the ok
            // button
            combo.setFocus();
            if (value != null) {
                if (!update) {
                    text.setText(value);
                }
                text.selectAll();
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

            Label label = new Label(composite, SWT.WRAP);
            label.setText(Messages.getString("AliasDialog.table")); //$NON-NLS-1$

            combo = new Combo(composite, SWT.DROP_DOWN | SWT.READ_ONLY);
            combo.setItems(physicalTables);
            combo.addSelectionListener(new SelectionListener() {

                @Override
                public void widgetDefaultSelected(SelectionEvent e) {
                }

                @Override
                public void widgetSelected(SelectionEvent e) {
                    internalTableName = combo.getText();
                    validateInput(combo.getText(), text.getText());
                }

            });

            GridData gridDataCombo = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
            gridDataCombo.horizontalSpan = 2;
            combo.setLayoutData(gridDataCombo);

            if (message != null) {
                label = new Label(composite, SWT.WRAP);
                label.setText(message);
                GridData data = new GridData(GridData.GRAB_HORIZONTAL | GridData.GRAB_VERTICAL | GridData.HORIZONTAL_ALIGN_FILL
                        | GridData.VERTICAL_ALIGN_CENTER);
                data.widthHint = convertHorizontalDLUsToPixels(IDialogConstants.MINIMUM_MESSAGE_AREA_WIDTH);
                label.setLayoutData(data);
                label.setFont(parent.getFont());
            }
            warnMessageText = new Label(composite, SWT.WRAP);
            warnMessageText.setText(Messages.getString("AliasDialog.warnMessage")); //$NON-NLS-1$

            text = new Text(composite, SWT.SINGLE | SWT.BORDER);
            text.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL));
            text.addModifyListener(new ModifyListener() {

                @Override
                public void modifyText(ModifyEvent e) {
                    validateInput(combo.getText(), text.getText());

                }
            });
            errorMessageText = new Label(composite, SWT.WRAP);
            errorMessageText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL));
            // Set the error message text
            // See https://bugs.eclipse.org/bugs/show_bug.cgi?id=66292
            setErrorMessage(errorMessage);

            applyDialogFont(composite);
            init();
            return composite;
        }

        protected void init() {
            if (update) {
                UIManager uiManager = mapperManager.getUiManager();
                InputDataMapTableView currentSelectedInputTable = uiManager.getCurrentSelectedInputTableView();
                if (currentSelectedInputTable != null) {
                    IDataMapTable inputTable = currentSelectedInputTable.getDataMapTable();
                    if (inputTable != null && inputTable instanceof InputTable) {
                        String inputTableName = ((InputTable) inputTable).getTableName();
                        String alias = ((InputTable) inputTable).getAlias();
                        aliasInternalDialog.getCombo().setText(inputTableName);
                        aliasInternalDialog.getCombo().setEnabled(false);
                        internalTableName = inputTableName;
                        if (StringUtils.isNotBlank(alias)) {
                            aliasInternalDialog.getText().setText(alias);
                        }
                    }
                }
            }
        }

        /**
         * Returns the error message label.
         *
         * @return the error message label
         * @deprecated use setErrorMessage(String) instead
         */
        @Deprecated
        protected Label getErrorMessageLabel() {
            return null;
        }

        /**
         * Returns the ok button.
         *
         * @return the ok button
         */
        protected Button getOkButton() {
            return okButton;
        }

        public Combo getCombo() {
            return this.combo;
        }

        /**
         * Returns the text area.
         *
         * @return the text area
         */
        protected Text getText() {
            return text;
        }

        /**
         * Returns the validator.
         *
         * @return the validator
         */
        protected IInputValidator getValidator() {
            return validator;
        }

        /**
         * Returns the string typed into this input dialog.
         *
         * @return the input string
         */
        public String getValue() {
            return value;
        }

        /**
         * Validates the input.
         * <p>
         * The default implementation of this framework method delegates the request to the supplied input validator
         * object; if it finds the input invalid, the error message is displayed in the dialog's message line. This hook
         * method is called whenever the text changes in the input field.
         * </p>
         */
        protected void validateInput(String tableName, String alias) {
            String errorMessage = null;
            if (validator != null) {
                errorMessage = validator.isValid(alias);
            }
            if (errorMessage == null) {
                Problem needAliasProblem = problemsAnalyser.getNeedAliasProblem(mapperManager.getComponent(), combo.getText(),
                        text.getText());
                if (needAliasProblem != null) {
                    errorMessage = needAliasProblem.getDescription();
                }
            }
            // Bug 16256: important not to treat "" (blank error) the same as null
            // (no error)
            setErrorMessage(errorMessage);
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
                errorMessageText.getParent().layout();
                // Access the ok button by id, in case clients have overridden button creation.
                // See https://bugs.eclipse.org/bugs/show_bug.cgi?id=113643
                Control button = getButton(IDialogConstants.OK_ID);
                if (button != null) {
                    button.setEnabled(errorMessage == null);
                }
            }
        }

        // /**
        // * DOC amaumont AliasInternalDialog constructor comment.
        // *
        // * @param parentShell
        // * @param dialogTitle
        // * @param dialogMessage
        // * @param initialValue
        // * @param validator
        // */
        // public AliasInternalDialog(Shell parentShell, String dialogTitle, String dialogMessage, String initialValue,
        // IInputValidator validator) {
        // super(parentShell, dialogTitle, dialogMessage, initialValue, validator);
        // }
        //
        // String tableName;
        //
        // String internalTableName;
        //
        // Combo combo;
        //
        // /*
        // * (non-Javadoc)
        // *
        // * @see org.eclipse.jface.dialogs.InputDialog#createDialogArea(org.eclipse.swt.widgets.Composite)
        // */
        // @Override
        // protected Control createDialogArea(Composite parent) {
        // Control control = super.createDialogArea(parent);
        // Composite composite = (Composite) control;
        //
        // GridLayout gridLayout = (GridLayout) composite.getLayout();
        // gridLayout.numColumns = 4;
        //
        // Control[] children = composite.getChildren();
        // GridData gridDataLabel = (GridData) children[0].getLayoutData();
        // gridDataLabel.horizontalSpan = 4;
        //
        // GridData gridDataText = (GridData) getText().getLayoutData();
        // gridDataText.horizontalSpan = 4;
        // gridDataText.grabExcessHorizontalSpace = false;
        //
        // Text error = (Text) children[2];
        // GridData gridDataError = (GridData) children[2].getLayoutData();
        // gridDataError.horizontalSpan = 4;
        // gridDataError.horizontalAlignment = SWT.FILL;
        // gridDataError.grabExcessHorizontalSpace = true;
        //
        // Label label = new Label(composite, SWT.WRAP);
        // label.setText(" for this table :");
        // GridData data = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
        // data.horizontalSpan = 2;
        // label.setLayoutData(data);
        // label.setFont(parent.getFont());
        //
        // combo = new Combo((Composite) control, SWT.DROP_DOWN | SWT.READ_ONLY);
        // combo.setItems(tables);
        // combo.addSelectionListener(new SelectionListener() {
        //
        // public void widgetDefaultSelected(SelectionEvent e) {
        // }
        //
        // public void widgetSelected(SelectionEvent e) {
        // internalTableName = combo.getText();
        // validateInput();
        // }
        //
        // });
        //
        // GridData gridDataCombo = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
        // gridDataCombo.horizontalSpan = 2;
        // combo.setLayoutData(gridDataCombo);
        //
        // composite.layout();
        //
        // return control;
        // }
        //
        // /*
        // * (non-Javadoc)
        // *
        // * @see org.eclipse.jface.dialogs.InputDialog#createButtonsForButtonBar(org.eclipse.swt.widgets.Composite)
        // */
        // @Override
        // protected void createButtonsForButtonBar(Composite parent) {
        // super.createButtonsForButtonBar(parent);
        //
        // getText().selectAll();
        //
        // }

        public String getTableName() {
            return internalTableName;
        }

    };

    /**
     * DOC amaumont Comment method "isNotAlreadyExist".
     *
     * @param tableName
     * @return
     */
    public boolean isSameAsVisibleTableName(String tableName) {
        boolean alreadyExists = false;
        for (String table : visibleTables) {
            if (table.equalsIgnoreCase(tableName)) {
                alreadyExists = true;
                break;
            }
        }
        return alreadyExists;
    }

    /**
     * DOC amaumont Comment method "isSameAsPhysicalTable".
     *
     * @param name
     */
    public boolean isSameAsPhysicalTable(String name) {
        boolean newTextIsSameAsTable = false;
        for (String table : physicalTables) {
            if (table.equals(name)) {
                newTextIsSameAsTable = true;
                break;
            }
        }
        return newTextIsSameAsTable;
    }

}
