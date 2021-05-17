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
package org.talend.designer.components.exchange.ui.dialog;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.talend.core.prefs.ITalendCorePrefConstants;
import org.talend.designer.components.exchange.ui.actions.DownloadComponenentsAction;

/**
 *
 * DOC hcyi class global comment. Detailled comment
 */
public class DownloadCheckUpdatesDialog extends Dialog {

    /**
     * Create the dialog.
     *
     * @param parentShell
     */
    public DownloadCheckUpdatesDialog(Shell parentShell) {
        super(parentShell);
    }

    public void configureShell(Shell shell) {
        super.configureShell(shell);
        shell.setText("Talend Exchange Updates");
    }

    /**
     * Create contents of the dialog.
     *
     * @param parent
     */
    @Override
    protected Control createDialogArea(Composite parent) {
        Composite container = (Composite) super.createDialogArea(parent);

        Composite composite = new Composite(container, SWT.NONE);
        composite.setLayout(new GridLayout(2, false));
        GridData gd_composite = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
        gd_composite.heightHint = 188;
        gd_composite.widthHint = 423;
        composite.setLayoutData(gd_composite);

        Label lblNewLabel = new Label(composite, SWT.NONE);
        GridData gd_lblNewLabel = new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1);
        gd_lblNewLabel.heightHint = 92;
        gd_lblNewLabel.widthHint = 406;
        lblNewLabel.setLayoutData(gd_lblNewLabel);
        lblNewLabel.setText("\nThere are newer versions of some items you have downloaded\nfrom the Talend Exchange.");

        Label lblNewLabel_1 = new Label(composite, SWT.NONE);
        lblNewLabel_1.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1));
        lblNewLabel_1.setText("Total available updates : ");
        new Label(composite, SWT.NONE);
        new Label(composite, SWT.NONE);
        new Label(composite, SWT.NONE);
        new Label(composite, SWT.NONE);

        Button btnCheckButton = new Button(composite, SWT.CHECK);
        btnCheckButton.setText("Don't ask again ");
        new Label(composite, SWT.NONE);

        btnCheckButton.addSelectionListener(new SelectionAdapter() {

            public void widgetSelected(SelectionEvent event) {
                IPreferenceStore prefStore = PlatformUI.getPreferenceStore();
                prefStore.setValue(ITalendCorePrefConstants.EXCHANGE_DOWNLOADED_CHECK_UPDATES, true);
                cancelPressed();
            }
        });

        return container;
    }

    /**
     * Create contents of the button bar.
     *
     * @param parent
     */
    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        createButton(parent, IDialogConstants.CANCEL_ID, "Later", false);
        createButton(parent, IDialogConstants.OK_ID, "Update Now ", true);
    }

    protected void okPressed() {
        DownloadComponenentsAction downloadAction = new DownloadComponenentsAction();
        if (downloadAction != null) {
            downloadAction.run();
        }
        close();
    }
}
