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
package org.talend.repository.preference;

import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.talend.repository.preference.AbstractArtifactProxySettingForm.ICheckListener;

public class ArtifactProxySettingPage extends ProjectSettingPage {

    private AbstractArtifactProxySettingForm proxySettingForm;

    @Override
    public void refresh() {
        // TODO Auto-generated method stub

    }

    @Override
    protected Control createContents(Composite parent) {

        
        AbstractArtifactProxySettingForm.ICheckListener checkListener = new ICheckListener() {

            @Override
            public String getMessage() {
                return ArtifactProxySettingPage.this.getMessage();
            }

            @Override
            public void showMessage(String message, int level) {
                setMessage(message, level);
            }

            @Override
            public void updateButtons() {
                boolean isValid = getCurrentForm().isComplete();
                setValid(isValid);
            }

            @Override
            public void run(boolean fork, boolean cancelable, IRunnableWithProgress runnable) throws Exception {
                throw new Exception("Please implement it if needed"); //$NON-NLS-1$
            }

        };
         

        ArtifactProxySettingForm existingConfigForm = new ArtifactProxySettingForm(parent, SWT.NONE, this);
        existingConfigForm.setCheckListener(checkListener);
        setCurrentForm(existingConfigForm);
        boolean isValid = getCurrentForm().isComplete();
        setValid(isValid);

        return existingConfigForm;
    }

    @Override
    protected void performApply() {
        AbstractArtifactProxySettingForm currentForm = getCurrentForm();
        if (currentForm != null) {
            currentForm.performApply();
        }
        super.performApply();
    }

    @Override
    protected void performDefaults() {
        AbstractArtifactProxySettingForm currentForm = getCurrentForm();
        if (currentForm != null) {
            currentForm.performDefaults();
        }
        super.performDefaults();
    }

    @Override
    public boolean performOk() {
        AbstractArtifactProxySettingForm currentForm = getCurrentForm();
        if (currentForm != null) {
            boolean isOk = currentForm.performOk();
            if (!isOk) {
                return false;
            }
        }
        return super.performOk();
    }

    private void setCurrentForm(AbstractArtifactProxySettingForm proxySettingForm) {
        this.proxySettingForm = proxySettingForm;
    }

    private AbstractArtifactProxySettingForm getCurrentForm() {
        return this.proxySettingForm;
    }

}
