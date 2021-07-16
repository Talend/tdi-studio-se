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
package org.talend.repository.ui.login;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.talend.core.model.general.ConnectionBean;
import org.talend.repository.i18n.Messages;
import org.talend.repository.model.RepositoryConstants;

/**
 * 
 * created by hcyi on Jul 8, 2021 Detailled comment
 *
 */
public class StoreCredentialDialog extends Dialog {

    private Text user;

    private Text password;

    private ConnectionBean selectedConnBean;

    public StoreCredentialDialog(Shell shell, ConnectionBean selectedConnBean) {
        super(shell);
        this.selectedConnBean = selectedConnBean;
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        final Composite composite = (Composite) super.createDialogArea(parent);
        composite.setLayout(new GridLayout(2, false));
        getShell().setText(Messages.getString("StoreCredentialDialog.Login")); //$NON-NLS-1$
        Control focusCtrl = null;

        Label uriLabel = new Label(composite, SWT.NONE);
        uriLabel.setText(Messages.getString("StoreCredentialDialog.repository")); //$NON-NLS-1$
        Text uriText = new Text(composite, SWT.READ_ONLY);
        String tacURL = selectedConnBean.getDynamicFields().get(RepositoryConstants.REPOSITORY_URL);
        if (StringUtils.isNotBlank(tacURL)) {
            uriText.setText(tacURL);
        }

        Label userLabel = new Label(composite, SWT.NONE);
        userLabel.setText(Messages.getString("StoreCredentialDialog.user")); //$NON-NLS-1$

        user = new Text(composite, SWT.BORDER);
        focusCtrl = user;
        String userStr = selectedConnBean.getUser();
        if (StringUtils.isNotBlank(userStr)) {
            user.setText(userStr);
            // name has already been filled, just need to fill password now
            focusCtrl = null;
        }
        GridDataFactory.fillDefaults().grab(true, false).applyTo(user);

        Label passwordLabel = new Label(composite, SWT.NONE);
        passwordLabel.setText(Messages.getString("StoreCredentialDialog.password")); //$NON-NLS-1$
        password = new Text(composite, SWT.PASSWORD | SWT.BORDER);
        GridDataFactory.fillDefaults().grab(true, false).applyTo(password);
        if (focusCtrl == null) {
            focusCtrl = password;
        }
        if (focusCtrl != null) {
            focusCtrl.setFocus();
        }
        return composite;
    }

    @Override
    protected void okPressed() {
        if (user.getText().length() > 0) {
            selectedConnBean.setUser(user.getText());
            selectedConnBean.setCredentials(password.getText());
        }
        super.okPressed();
    }

    public ConnectionBean getSelectedConnBean() {
        return this.selectedConnBean;
    }
}
