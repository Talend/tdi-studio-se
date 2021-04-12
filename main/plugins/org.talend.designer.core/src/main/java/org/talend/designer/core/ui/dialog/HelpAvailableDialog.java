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
package org.talend.designer.core.ui.dialog;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TrayDialog;
import org.eclipse.jface.window.IShellProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.help.IWorkbenchHelpSystem;


/**
 * This Dialog is similar to TraDialog both support helpAvailable feature, but this one override the UI of Help button
 * to a normal labeled button instead of a "<img src="http://www.eclipse.org/mylyn/images/icon-help.gif">" image button.
 * Help button is visible by default(can be set by {@link #setHelpAvailable(boolean)}).
 *
 * @author GaoZone
 * @version 1.1
 */
public abstract class HelpAvailableDialog extends TrayDialog {

    public HelpAvailableDialog(IShellProvider parentShell) {
        super(parentShell);
    }

    public HelpAvailableDialog(Shell shell) {
        super(shell);
    }

    @Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        setHelpAvailable(true);
        IWorkbenchHelpSystem helpSystem = PlatformUI.getWorkbench().getHelpSystem();
        helpSystem.setHelp(newShell, getHelpContextId());
    }

    /**
     * Gets the help context id. Override when necessary.</br> Triggered in {@link #helpPressed()} by default, use to
     * open specify help context.
     *
     * @return the help context id
     */
    protected String getHelpContextId() {
        return null;
    }

    @Override
    protected Control createButtonBar(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        // create a layout with spacing and margins appropriate for the font
        // size.
        GridLayout layout = new GridLayout();
        layout.numColumns = 0; // this is incremented by createButton
        layout.makeColumnsEqualWidth = true;
        layout.marginWidth = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_MARGIN);
        layout.marginHeight = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_MARGIN);
        layout.horizontalSpacing = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_SPACING);
        layout.verticalSpacing = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_SPACING);
        composite.setLayout(layout);
        GridData data = new GridData(GridData.HORIZONTAL_ALIGN_END | GridData.VERTICAL_ALIGN_CENTER);
        composite.setLayoutData(data);
        composite.setFont(parent.getFont());

        // Add the buttons to the button bar.
        createButtonsForButtonBar(composite);
        return composite;
    }

    /**
     * Add create Help Button if {@link #isHelpAvailable()} = true.
     *
     * @see org.eclipse.jface.dialogs.Dialog#createButtonsForButtonBar(org.eclipse.swt.widgets.Composite)
     */
    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        if (isHelpAvailable()) {
            createButton(parent, IDialogConstants.HELP_ID, IDialogConstants.HELP_LABEL, false);
        }
        // create OK and Cancel buttons by default
        createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
        createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
    }

    @Override
    protected void buttonPressed(int buttonId) {
        if (IDialogConstants.HELP_ID == buttonId) {
            if (getShell() != null) {
                Control c = getShell().getDisplay().getFocusControl();
                while (c != null) {
                    if (c.isListening(SWT.Help)) {
                        c.notifyListeners(SWT.Help, new Event());
                        break;
                    }
                    c = c.getParent();
                }
            }
        } else {
            super.buttonPressed(buttonId);
        }
    }
}
