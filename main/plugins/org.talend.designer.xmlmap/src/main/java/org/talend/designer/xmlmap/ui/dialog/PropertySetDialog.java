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
package org.talend.designer.xmlmap.ui.dialog;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.talend.designer.xmlmap.i18n.Messages;

/**
 * DOC wchen class global comment. Detailled comment
 */
public class PropertySetDialog extends Dialog {

    private Button dieOnErrorButton;

    private final boolean initDieOnError;

    private boolean isValueChanged;

    private final Color color = new Color(Display.getDefault(), 238, 238, 0);

    /**
     * Create the dialog
     *
     * @param parentShell
     */
    public PropertySetDialog(Shell parentShell, boolean initDieOnError) {
        super(parentShell);
        this.initDieOnError = initDieOnError;

    }

    /**
     * Create contents of the dialog
     *
     * @param parent
     */
    @Override
    protected Control createDialogArea(Composite parent) {
        Composite container = (Composite) super.createDialogArea(parent);
        final GridLayout gridLayout = new GridLayout();
        gridLayout.marginLeft = 10;
        gridLayout.marginTop = 10;
        gridLayout.marginHeight = 10;
        container.setLayout(gridLayout);

        dieOnErrorButton = new Button(container, SWT.CHECK);
        dieOnErrorButton.setText("Die on error");
        dieOnErrorButton.setSelection(initDieOnError);

        return container;
    }

    /**
     * Return the initial size of the dialog
     */
    @Override
    protected Point getInitialSize() {
        return new Point(300, 200);
    }

    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText(Messages.getString("PropertySetDialog.Title"));
    }

    protected void okPressed() {
        if (initDieOnError != dieOnErrorButton.getSelection()) {
            isValueChanged = true;
        }

        super.okPressed();
        color.dispose();
    }

    public boolean isValueChanged() {
        return this.isValueChanged;
    }
}
