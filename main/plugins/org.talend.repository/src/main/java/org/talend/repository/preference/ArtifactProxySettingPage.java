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
package org.talend.repository.preference;

import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.PluginChecker;
import org.talend.core.ui.services.IHadoopUiService;
import org.talend.core.ui.services.IPreferenceForm;
import org.talend.repository.preference.AbstractArtifactProxySettingForm.ICheckListener;

public class ArtifactProxySettingPage extends ProjectSettingPage {

    private IPreferenceForm dynamicDistributionPrefForm;

    private AbstractArtifactProxySettingForm proxySettingForm;

    @Override
    public void refresh() {
        // TODO Auto-generated method stub

    }

    @Override
    protected Control createContents(Composite parent) {
        Composite container = new Composite(parent, SWT.NONE);
        container.setLayout(new GridLayout(1, true));
        container.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

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
         
        // only show in tis product
        GridData layoutData = new GridData(SWT.FILL, SWT.FILL, true, false);
        if (PluginChecker.isTIS()) {
            ArtifactProxySettingForm existingConfigForm = new ArtifactProxySettingForm(container, SWT.NONE);
            existingConfigForm.setLayoutData(layoutData);
            existingConfigForm.setCheckListener(checkListener);
            setCurrentForm(existingConfigForm);
        }
        // dynamic distribution group begin
        GlobalServiceRegister serviceRegister = GlobalServiceRegister.getDefault();
        if (serviceRegister.isServiceRegistered(IHadoopUiService.class)) {
            Composite dynamicDistriutionGroup = new Composite(container, SWT.NONE);
            // dynamicDistriutionGroup.setText("Dynamic Distribution Proxy Settings");
            dynamicDistriutionGroup.setLayout(new FillLayout());

            IHadoopUiService hadoopUiService = serviceRegister.getService(IHadoopUiService.class);
            dynamicDistributionPrefForm = hadoopUiService
                    .createDynamicDistributionPrefForm(dynamicDistriutionGroup, this);
            layoutData = new GridData(SWT.FILL, SWT.FILL, true, false);
            dynamicDistriutionGroup.setLayoutData(layoutData);
            dynamicDistributionPrefForm.isComplete();
        }

        if (proxySettingForm != null) {
            boolean isValid = getCurrentForm().isComplete();
            setValid(isValid);
        }

        Composite placeHolder = new Composite(parent, SWT.NONE);
        layoutData = new GridData(SWT.FILL, SWT.FILL, true, true);
        placeHolder.setLayoutData(layoutData);
        return container;
    }

    @Override
    protected void performApply() {
        AbstractArtifactProxySettingForm currentForm = getCurrentForm();
        if (currentForm != null) {
            currentForm.performApply();
        }
        if (dynamicDistributionPrefForm != null) {
            dynamicDistributionPrefForm.performApply();
        }
        super.performApply();
    }

    @Override
    protected void performDefaults() {
        if (dynamicDistributionPrefForm != null) {
            if (!dynamicDistributionPrefForm.performDefaults()) {
                return;
            }
        }
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
        if (dynamicDistributionPrefForm != null) {
            dynamicDistributionPrefForm.performApply();
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
