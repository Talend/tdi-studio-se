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
package org.talend.repository.ui.login.connections.network;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.talend.repository.i18n.Messages;


/**
 * DOC cmeng  class global comment. Detailled comment
 */
public class NetworkSettingDialog extends MessageDialog {

    private static final int BUTTON_OK_INDEX = 0;

    private NetworkSettingForm form;

    public NetworkSettingDialog(Shell parentShell) {
        super(parentShell, Messages.getString("NetworkSettingDialog.title"), null, null, MessageDialog.INFORMATION, //$NON-NLS-1$
                new String[] { IDialogConstants.OK_LABEL, IDialogConstants.CANCEL_LABEL }, BUTTON_OK_INDEX);
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        GridData data = new GridData(GridData.FILL_BOTH);
        data.horizontalSpan = 2;
        composite.setLayoutData(data);

        FillLayout layout = new FillLayout();
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        composite.setLayout(layout);

        form = new NetworkSettingForm(composite, SWT.NONE, new ICheckListener() {

            @Override
            public void updateButtons() {
                validate();
            }
        });

        return composite;
    }

    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        super.createButtonsForButtonBar(parent);
        validate();
    }

    private void validate() {
        getButton(BUTTON_OK_INDEX).setEnabled(form.isComplete());
    }

    @Override
    protected void buttonPressed(int buttonId) {
        if (buttonId == BUTTON_OK_INDEX) {
            form.performFinish();
        }
        super.buttonPressed(buttonId);
    }

    @Override
    protected boolean isResizable() {
        return true;
    }

    @Override
    protected Point getInitialSize() {
        Point size = new Point(400, 230);
        Point defaultSize = super.getInitialSize();

        if (size.x < defaultSize.x) {
            size.x = defaultSize.x;
        }
        if (size.y < defaultSize.y) {
            size.y = defaultSize.y;
        }

        return size;
    }
}
