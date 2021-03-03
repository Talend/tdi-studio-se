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
package org.talend.designer.filemultischemas.ui.dialog;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

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
import org.talend.designer.filemultischemas.data.SchemasKeyData;

/**
 * DOC hywang class global comment. Detailled comment
 */
public class AddRowDialog extends Dialog {

    private Label keyLbl, recoredLbl, sepLbl;

    private Text key, record, sep;

    private String keyValue, recordValue, sepValue;

    private Composite main;

    private SchemasKeyData root;

    private List<String> keyNames;

    private Button OK, CANCEL;

    private static String TITLE = "Add new schema row";

    private Shell parentShell;

    public AddRowDialog(Shell parentShell, SchemasKeyData root) {
        super(parentShell);
        this.root = root;
        keyNames = new ArrayList<String>();
        getAllKeysNames(this.root);
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        this.parentShell = parent.getShell();
        parent.getShell().setText(TITLE);
        main = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout();
        GridData data = new GridData(GridData.CENTER);
        layout.numColumns = 2;
        main.setLayout(layout);
        main.setLayoutData(data);
        createControls(main);
        return main;
    }

    private void createControls(Composite main) {
        keyLbl = new Label(main, SWT.NONE);
        keyLbl.setText("Schema:"); //$NON-NLS-1$
        key = new Text(main, SWT.BORDER);
        key.addModifyListener(new ModifyListener() {

            public void modifyText(ModifyEvent e) {
                keyValue = key.getText();
                validate(keyValue);
            }
        });

        recoredLbl = new Label(main, SWT.NONE);
        recoredLbl.setText("Record:"); //$NON-NLS-1$
        record = new Text(main, SWT.BORDER);
        record.addModifyListener(new ModifyListener() {

            public void modifyText(ModifyEvent e) {
                recordValue = record.getText();
            }
        });

        sepLbl = new Label(main, SWT.NONE);
        sepLbl.setText("Seperator:"); //$NON-NLS-1$
        sep = new Text(main, SWT.BORDER);
        sep.addModifyListener(new ModifyListener() {

            public void modifyText(ModifyEvent e) {
                sepValue = sep.getText();
            }
        });
    }

    public String getKeyValue() {
        if (this.keyValue == null) {
            return ""; //$NON-NLS-1$
        }
        return this.keyValue;
    }

    public String getRecordValue() {
        if (this.recordValue == null) {
            return ""; //$NON-NLS-1$
        }
        return this.recordValue;
    }

    public String getSepValue() {
        if (this.sepValue == null) {
            return ""; //$NON-NLS-1$
        }
        return this.sepValue;
    }

    private void getAllKeysNames(SchemasKeyData root) {
        if (root != null) {
            if (root.getUniqueRecord() != null) {
                keyNames.add(root.getUniqueRecord().toUpperCase());
            }
            if (root.getChildren() != null) {
                List<SchemasKeyData> children = root.getChildren();
                for (SchemasKeyData child : children) {
                    getAllKeysNames(child);
                }
            }
        }
    }

    private void validate(String currentName) {
        boolean enabled = false;
        String msg = null;
        if (!Pattern.matches("^[a-zA-Z0-9\\_]+[a-zA-Z0-9\\_]*$", currentName)) { //$NON-NLS-1$
            msg = "Invalid value";
        } else if (currentName != null && !keyNames.contains(currentName.toUpperCase())) {
            enabled = true;
            msg = TITLE;
        } else {
            msg = "Name already exsist";
        }
        this.OK.setEnabled(enabled);
        this.parentShell.setText(msg);
        this.parentShell.pack();
    }

    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        OK = createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
        CANCEL = createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
    }

}
