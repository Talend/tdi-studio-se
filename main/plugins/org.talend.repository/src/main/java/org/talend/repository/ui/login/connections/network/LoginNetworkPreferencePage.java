// ============================================================================
//
// Copyright (C) 2006-2020 Talend Inc. - www.talend.com
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

import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.talend.repository.i18n.Messages;


/**
 * DOC cmeng  class global comment. Detailled comment
 */
public class LoginNetworkPreferencePage extends PreferencePage {

    private NetworkErrorRetryForm retryForm;

    @Override
    protected Control createContents(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        GridData data = new GridData(GridData.FILL_BOTH);
        data.horizontalSpan = 2;
        composite.setLayoutData(data);

        FillLayout layout = new FillLayout();
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        composite.setLayout(layout);

        retryForm = new NetworkErrorRetryForm(composite, SWT.NONE, new ICheckListener() {

            @Override
            public void updateButtons() {
                validate();
            }

        }, Messages.getString("LoginNetworkPreferencePage.info"), null, null, false);

        return composite;
    }

    @Override
    public boolean performOk() {
        if (this.isControlCreated()) {
            retryForm.performFinish();
        }
        return super.performOk();
    }

    @Override
    protected void performDefaults() {
        if (this.isControlCreated()) {
            retryForm.performDefault();
        }
        super.performDefaults();
    }

    private void validate() {
        this.updateApplyButton();
        this.getContainer().updateButtons();
    }

    @Override
    public boolean isValid() {
        return super.isValid() && (this.isControlCreated() && retryForm.isComplete());
    }

}
