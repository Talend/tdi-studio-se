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

import org.apache.commons.lang3.StringUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.talend.repository.i18n.Messages;


/**
 * DOC cmeng  class global comment. Detailled comment
 */
public class NetworkSettingForm extends Composite {

    private Text connectTimeoutText;

    private Text readTimeoutText;

    private ICheckListener checkListener;

    private NetworkConfiguration networkConfiguration;

    public NetworkSettingForm(Composite parent, int style, ICheckListener checkListener) {
        super(parent, style);
        this.checkListener = checkListener;
        FormLayout formLayout = new FormLayout();
        formLayout.marginWidth = 10;
        formLayout.marginHeight = 10;
        this.setLayout(formLayout);
        createContent(this);
        initData();
        addListeners();
    }

    private void createContent(Composite parent) {

        final int ALIGN_HORIZON = 5;
        final int ALGN_VERTICAL = 10;

        Label connectLabel = new Label(parent, SWT.NONE);
        connectLabel.setText(Messages.getString("NetworkSettingForm.connectTimeout.label")); //$NON-NLS-1$
        Point connectLabelPoint = connectLabel.computeSize(SWT.DEFAULT, SWT.DEFAULT);

        connectTimeoutText = new Text(parent, SWT.BORDER);

        Label readLabel = new Label(parent, SWT.NONE);
        readLabel.setText(Messages.getString("NetworkSettingForm.readTimeout.label")); //$NON-NLS-1$
        Point readLabelPoint = readLabel.computeSize(SWT.DEFAULT, SWT.DEFAULT);

        readTimeoutText = new Text(parent, SWT.BORDER);

        int labelWidth = connectLabelPoint.x;
        if (connectLabelPoint.x < readLabelPoint.x) {
            labelWidth = readLabelPoint.x;
        }

        FormData formData = new FormData();
        formData.top = new FormAttachment(0);
        formData.left = new FormAttachment(connectLabel, ALIGN_HORIZON, SWT.RIGHT);
        formData.right = new FormAttachment(100);
        connectTimeoutText.setLayoutData(formData);

        formData = new FormData();
        formData.top = new FormAttachment(connectTimeoutText, 0, SWT.CENTER);
        formData.left = new FormAttachment(0);
        formData.width = labelWidth;
        connectLabel.setLayoutData(formData);

        formData = new FormData();
        formData.left = new FormAttachment(connectTimeoutText, 0, SWT.LEFT);
        formData.right = new FormAttachment(connectTimeoutText, 0, SWT.RIGHT);
        formData.top = new FormAttachment(connectTimeoutText, ALGN_VERTICAL, SWT.BOTTOM);
        readTimeoutText.setLayoutData(formData);

        formData = new FormData();
        formData.left = new FormAttachment(connectLabel, 0, SWT.LEFT);
        formData.right = new FormAttachment(connectLabel, 0, SWT.RIGHT);
        formData.top = new FormAttachment(readTimeoutText, 0, SWT.CENTER);
        readLabel.setLayoutData(formData);

    }

    private void initData() {
        networkConfiguration = NetworkConfiguration.getInstance();

        int connectionTimeout = networkConfiguration.getConnectionTimeout();
        String timeoutStr = String.valueOf(connectionTimeout);
        connectTimeoutText.setText(timeoutStr);
        connectTimeoutText.setSelection(timeoutStr.length());

        int readTimeout = networkConfiguration.getReadTimeout();
        String readTimeoutStr = String.valueOf(readTimeout);
        readTimeoutText.setText(readTimeoutStr);
        readTimeoutText.setSelection(readTimeoutStr.length());
    }

    private void addListeners() {

        connectTimeoutText.addVerifyListener(new VerifyListener() {

            @Override
            public void verifyText(VerifyEvent e) {
                checkInteger(connectTimeoutText, e);
            }
        });

        connectTimeoutText.addModifyListener(new ModifyListener() {

            @Override
            public void modifyText(ModifyEvent e) {
                updateButtons();
            }
        });

        readTimeoutText.addVerifyListener(new VerifyListener() {

            @Override
            public void verifyText(VerifyEvent e) {
                checkInteger(readTimeoutText, e);
            }
        });

        readTimeoutText.addModifyListener(new ModifyListener() {

            @Override
            public void modifyText(ModifyEvent e) {
                updateButtons();
            }
        });
    }

    public boolean isComplete() {
        boolean checkConnectionTimeout = checkConnectionTimeout();
        boolean checkReadTimeout = checkReadTimeout();
        return checkConnectionTimeout && checkReadTimeout;
    }

    public void performFinish() {
        networkConfiguration.setConnectionTimeout(Integer.valueOf(connectTimeoutText.getText()));
        networkConfiguration.setReadTimeout(Integer.valueOf(readTimeoutText.getText()));
    }

    private boolean checkConnectionTimeout() {
        connectTimeoutText.setBackground(null);
        String timeoutStr = connectTimeoutText.getText();
        if (StringUtils.isEmpty(timeoutStr)) {
            showError(connectTimeoutText, Messages.getString("NetworkSettingForm.connectTimeout.error.bound", //$NON-NLS-1$
                    NetworkConfiguration.CONNECTION_TIMEOUT_MIN, NetworkConfiguration.CONNECTION_TIMEOUT_MAX));
            return false;
        }
        try {
            int timeout = Integer.valueOf(timeoutStr);
            if (NetworkConfiguration.CONNECTION_TIMEOUT_MAX < timeout || timeout < NetworkConfiguration.CONNECTION_TIMEOUT_MIN) {
                showError(connectTimeoutText, Messages.getString("NetworkSettingForm.connectTimeout.error.bound", //$NON-NLS-1$
                        NetworkConfiguration.CONNECTION_TIMEOUT_MIN, NetworkConfiguration.CONNECTION_TIMEOUT_MAX));
                return false;
            }
        } catch (NumberFormatException e) {
            showError(connectTimeoutText, Messages.getString("NetworkSettingForm.connectTimeout.error.bound", //$NON-NLS-1$
                    NetworkConfiguration.CONNECTION_TIMEOUT_MIN, NetworkConfiguration.CONNECTION_TIMEOUT_MAX));
            return false;
        }
        return true;
    }

    private boolean checkReadTimeout() {
        readTimeoutText.setBackground(null);
        String timeoutStr = readTimeoutText.getText();
        if (StringUtils.isEmpty(timeoutStr)) {
            showError(readTimeoutText, Messages.getString("NetworkSettingForm.readTimeout.error.bound", //$NON-NLS-1$
                    NetworkConfiguration.READ_TIMEOUT_MIN, NetworkConfiguration.READ_TIMEOUT_MAX));
            return false;
        }
        try {
            int timeout = Integer.valueOf(timeoutStr);
            if (NetworkConfiguration.READ_TIMEOUT_MAX < timeout || timeout < NetworkConfiguration.READ_TIMEOUT_MIN) {
                showError(readTimeoutText, Messages.getString("NetworkSettingForm.readTimeout.error.bound", //$NON-NLS-1$
                        NetworkConfiguration.READ_TIMEOUT_MIN, NetworkConfiguration.READ_TIMEOUT_MAX));
                return false;
            }
        } catch (NumberFormatException e) {
            showError(readTimeoutText, Messages.getString("NetworkSettingForm.readTimeout.error.bound", //$NON-NLS-1$
                    NetworkConfiguration.READ_TIMEOUT_MIN, NetworkConfiguration.READ_TIMEOUT_MAX));
            return false;
        }
        return true;
    }

    private void showError(Text ctrl, String message) {
        ctrl.setToolTipText(message);
        ctrl.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_RED));
    }

    private void updateButtons() {
        checkListener.updateButtons();
    }

    private void checkInteger(Text textCtrl, VerifyEvent e) {
        if (e.character != 0 && e.keyCode != SWT.BS && e.keyCode != SWT.DEL && !Character.isDigit(e.character)) {
            e.doit = false;
        } else {
            if (e.character == '0' && e.start == 0) {
                e.doit = false;
            } else {
                e.doit = true;
            }
        }
    }

}
