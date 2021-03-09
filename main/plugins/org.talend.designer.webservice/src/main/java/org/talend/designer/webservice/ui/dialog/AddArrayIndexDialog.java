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
package org.talend.designer.webservice.ui.dialog;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.talend.designer.webservice.i18n.Messages;
import org.talend.designer.webservice.ws.wsdlinfo.ParameterInfo;

/**
 * DOC Administrator class global comment. Detailled comment
 */
public class AddArrayIndexDialog extends Dialog {

    private Label titleLable;

    private Text text;

    private ParameterInfo para;

    private ParameterInfo selectedParaInfo;

    private int arraySize = 0;

    private Shell parentShell;

    private String indexText = "-1";

    private String title;

    private Button arrayButton;

    private Button arrayToItemButton;

    private boolean maximized;

    private int index;

    private Rectangle size;

    /**
     * DOC Administrator ErrorMessageDialog constructor comment.
     *
     * @param parentShell
     */
    protected AddArrayIndexDialog(Shell parentShell) {
        super(parentShell);
        setShellStyle(getShellStyle() | SWT.RESIZE);
        this.parentShell = parentShell;
    }

    public AddArrayIndexDialog(Shell parentShell, ParameterInfo para) {
        this(parentShell);
        this.parentShell = parentShell;
        this.para = para;
        this.arraySize = para.getArraySize();

    }

    protected Control createDialogArea(Composite parent) {
        Composite createDialogArea = (Composite) super.createDialogArea(parent);
        GridLayout layout = new GridLayout();
        layout.horizontalSpacing = 1;
        layout.numColumns = 2;
        createDialogArea.setLayout(layout);

        GridData data = new GridData(GridData.FILL_HORIZONTAL);

        GridData layoutData = new GridData(GridData.FILL_HORIZONTAL);
        layoutData.horizontalSpan = 2;
        arrayButton = new Button(createDialogArea, SWT.RADIO);
        arrayButton.setText("Get all the list element.");
        arrayButton.setLayoutData(layoutData);
        arrayButton.setSelection(true);
        arrayButton.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                text.setEnabled(false);
            }
        });

        arrayToItemButton = new Button(createDialogArea, SWT.RADIO);
        arrayToItemButton.setText("Get one element of the list.");
        arrayToItemButton.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                text.setEnabled(true);
            }
        });

        data = new GridData(GridData.FILL_HORIZONTAL);
        data.horizontalSpan = 1;
        text = new Text(createDialogArea, SWT.BORDER);
        text.setLayoutData(data);
        text.setEnabled(false);
        text.addModifyListener(new ModifyListener() {

            public void modifyText(ModifyEvent e) {
                indexText = text.getText();
                // String com = text.getText();
                // int index = Integer.valueOf(com);
                // if(index<0||index>arraySize){
                //
                // }
            }

        });

        return createDialogArea;
    }

    public ParameterInfo getSelectedParaInfo() {
        return selectedParaInfo;
    }

    protected void okPressed() {
        String com = text.getText();
        index = Integer.valueOf(indexText);

        if (text.getEnabled() == true && ("".equals(text.getText()) || text.getText() == null)) {
            MessageBox box = new MessageBox(parentShell, SWT.ICON_ERROR | SWT.OK);
            box.setText(Messages.getString("AddArrayIndexDialog.Error")); //$NON-NLS-1$
            box.setMessage(Messages.getString("AddArrayIndexDialog.Input_Index")); //$NON-NLS-1$
            box.open();
            return;
        }

        if (arraySize != -1 && (index < 0 || index > arraySize)) {
            MessageBox box = new MessageBox(parentShell, SWT.ICON_ERROR | SWT.OK | SWT.CANCEL);
            box.setText(Messages.getString("AddArrayIndexDialog.Error")); //$NON-NLS-1$
            box.setMessage(Messages.getString("AddArrayIndexDialog.check_size")); //$NON-NLS-1$
            box.open();
            text.setText(""); //$NON-NLS-1$
            return;
        }
        super.okPressed();
    }

    @Override
    protected void cancelPressed() {
        // TODO Auto-generated method stub
        super.cancelPressed();
    }

    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setBounds(500, 300, 350, 150);
        if (para.getName() != null) {
            newShell.setText(para.getName() + "is an Array Type Element.");
        } else {
            newShell.setText(Messages.getString("AddArrayIndexDialog.ArrayORElement"));
        }
    }

    public void setTitle(String title) {
        this.title = title;

    }

    public void setMaximized(boolean maximized) {
        this.maximized = maximized;
    }

    public void setSize(Rectangle size) {
        this.size = size;
    }

    public String getIndexText() {
        return this.indexText;
    }

    public int getIndex() {
        return this.index;
    }

    public int returnIndex() {
        int index = -1;
        if (indexText != null && !"".equals(indexText)) { //$NON-NLS-1$
            index = Integer.valueOf(indexText);
            if (arraySize != -1 && (index < 0 || index > arraySize)) {
                index = -1;
            }
        }
        return index;
    }
}
