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
package org.talend.repository.ui.login.connections.network;

import java.net.SocketTimeoutException;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.talend.repository.i18n.Messages;


/**
 * DOC cmeng  class global comment. Detailled comment
 */
public class NetworkErrorRetryDialog extends MessageDialog {

    private static final int BUTTON_DEFAULT_INDEX = 0;

    public static final int BUTTON_RETRY_INDEX = BUTTON_DEFAULT_INDEX;

    private NetworkErrorRetryForm form;

    private Throwable ex;

    private String exceptionString;

    private String information;

    private boolean donnotRetryByCancel = false;

    public NetworkErrorRetryDialog(Shell parentShell, Throwable ex) {
        super(parentShell, Messages.getString("NetworkErrorRetryDialog.title"), null, null, MessageDialog.INFORMATION, //$NON-NLS-1$
                new String[] { IDialogConstants.RETRY_LABEL, IDialogConstants.NO_LABEL }, BUTTON_DEFAULT_INDEX);
        this.ex = ex;
        if (this.ex instanceof SocketTimeoutException) {
            information = Messages.getString("NetworkErrorRetryDialog.message.default.timeout"); //$NON-NLS-1$
        } else {
            information = Messages.getString("NetworkErrorRetryDialog.message.default"); //$NON-NLS-1$
        }
        setDonnotRetryByCancel(false);
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

        form = new NetworkErrorRetryForm(composite, SWT.NONE, new ICheckListener() {

            @Override
            public void updateButtons() {
                validate();
            }
        }, information, ex, exceptionString);

        return composite;
    }

    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        super.createButtonsForButtonBar(parent);
        validate();
    }

    private void validate() {
        getButton(BUTTON_RETRY_INDEX).setEnabled(form.isComplete());
    }

    @Override
    protected void buttonPressed(int buttonId) {
        if (buttonId == BUTTON_RETRY_INDEX) {
            form.performFinish();
        } else {
            setDonnotRetryByCancel(true);
        }
        super.buttonPressed(buttonId);
    }

    @Override
    protected boolean isResizable() {
        return true;
    }


    public Throwable getEx() {
        return this.ex;
    }

    public void setEx(Throwable ex) {
        this.ex = ex;
    }

    public String getExceptionString() {
        return this.exceptionString;
    }

    public void setExceptionString(String exceptionString) {
        this.exceptionString = exceptionString;
    }

    public String getInformation() {
        return this.information;
    }

    public void setInformation(String information) {
        this.information = information;
    }

    public boolean isDonnotRetryByCancel() {
        return donnotRetryByCancel;
    }

    public void setDonnotRetryByCancel(boolean donnotRetryByCancel) {
        this.donnotRetryByCancel = donnotRetryByCancel;
    }

    // @Override
    // protected Point getInitialSize() {
    // Point size = new Point(400, 230);
    // Point defaultSize = super.getInitialSize();
    //
    // if (size.x < defaultSize.x) {
    // size.x = defaultSize.x;
    // }
    // if (size.y < defaultSize.y) {
    // size.y = defaultSize.y;
    // }
    //
    // return size;
    // }

}
