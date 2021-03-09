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

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.talend.commons.ui.runtime.exception.ExceptionHandler;
import org.talend.core.CorePlugin;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.repository.i18n.Messages;
import org.talend.repository.model.IProxyRepositoryFactory;

/**
 * DOC wzhang class global comment. Detailled comment
 */
public class SecurityProjectSettingPage extends ProjectSettingPage {

    private Button button;

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.jface.preference.PreferencePage#createContents(org.eclipse.swt.widgets.Composite)
     */
    @Override
    protected Control createContents(Composite parent) {
        Composite composite = new Composite(parent, SWT.NULL);
        GridLayout layout = new GridLayout(2, false);
        composite.setLayout(layout);

        button = new Button(composite, SWT.CHECK | SWT.TOP);
        button.setText(Messages.getString("SecurityProjectSettingPage.hidePass")); //$NON-NLS-1$

        button.setSelection(pro.getEmfProject().isHidePassword());
        IProxyRepositoryFactory factory = ProxyRepositoryFactory.getInstance();
        if (factory.isUserReadOnlyOnCurrentProject()) {
            composite.setEnabled(false);
        }
        return composite;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.jface.preference.PreferencePage#performApply()
     */
    @Override
    protected void performApply() {
        super.performApply();
        apply();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.jface.preference.PreferencePage#performOk()
     */
    @Override
    public boolean performOk() {
        apply();
        return super.performOk();
    }

    protected void apply() {
        if (button != null) {
            if (button.getSelection() == pro.getEmfProject().isHidePassword()) {
                return;
            }
            pro.getEmfProject().setHidePassword(button.getSelection());
        }
        IProxyRepositoryFactory prf = CorePlugin.getDefault().getProxyRepositoryFactory();

        try {
            prf.saveProject(pro);
        } catch (Exception ex) {
            ExceptionHandler.process(ex);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.repository.preference.ProjectSettingPage#refresh()
     */
    @Override
    public void refresh() {
        if (button != null && !button.isDisposed()) {
            button.setSelection(pro.getEmfProject().isHidePassword());
        }
    }

    @Override
    protected void performDefaults() {
        super.performDefaults();
        button.setSelection(false);
    }
}
