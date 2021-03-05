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

import java.io.PrintWriter;
import java.io.StringWriter;

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
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.events.ExpansionEvent;
import org.eclipse.ui.forms.events.IExpansionListener;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.talend.repository.i18n.Messages;


/**
 * DOC cmeng  class global comment. Detailled comment
 */
public class NetworkErrorRetryForm extends Composite {

    private Label informationLabel;

    private Text connectTimeoutText;

    private Text detailMessageText;

    private ICheckListener checkListener;

    private NetworkConfiguration networkConfiguration;

    private String information;

    private String exceptionString;

    private Throwable ex;

    private boolean showDetails;

    public NetworkErrorRetryForm(Composite parent, int style, ICheckListener checkListener, String information, Throwable ex,
            String exMessage) {
        this(parent, style, checkListener, information, ex, exMessage, true);
    }

    public NetworkErrorRetryForm(Composite parent, int style, ICheckListener checkListener, String information, Throwable ex,
            String exMessage, boolean showDetails) {
        super(parent, style);
        this.checkListener = checkListener;
        this.information = information;
        this.ex = ex;
        this.exceptionString = exMessage;
        this.showDetails = showDetails;
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
        final int ALIGN_VERTICAL = 10;

        informationLabel = new Label(parent, SWT.NONE);
        FormData formData = new FormData();
        formData.top = new FormAttachment(0);
        formData.left = new FormAttachment(0);
        formData.right = new FormAttachment(100);
        informationLabel.setLayoutData(formData);

        Label connectLabel = new Label(parent, SWT.NONE);
        connectLabel.setText(Messages.getString("NetworkErrorRetryForm.connectTimeout.label")); //$NON-NLS-1$

        connectTimeoutText = new Text(parent, SWT.BORDER);

        formData = new FormData();
        formData.top = new FormAttachment(informationLabel, ALIGN_VERTICAL, SWT.BOTTOM);
        formData.left = new FormAttachment(connectLabel, ALIGN_HORIZON, SWT.RIGHT);
        formData.right = new FormAttachment(100);
        connectTimeoutText.setLayoutData(formData);

        formData = new FormData();
        formData.top = new FormAttachment(connectTimeoutText, 0, SWT.CENTER);
        formData.left = new FormAttachment(0);
        connectLabel.setLayoutData(formData);

        if (this.showDetails) {
            ExpandableComposite errorComposite = new ExpandableComposite(parent, ExpandableComposite.COMPACT);
            errorComposite.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, true, 1, 1));
            errorComposite.setText(Messages.getString("NetworkErrorRetryForm.details")); //$NON-NLS-1$
            errorComposite.addExpansionListener(new IExpansionListener() {

                @Override
                public void expansionStateChanged(ExpansionEvent e) {
                    int delta = 300;
                    if (!e.getState()) {
                        delta = -delta;
                    }
                    Point shellSize = getShell().getSize();
                    Point newSize = new Point(shellSize.x, shellSize.y + delta);
                    getShell().setSize(newSize);
                }

                @Override
                public void expansionStateChanging(ExpansionEvent e) {
                }

            });

            formData = new FormData();
            formData.left = new FormAttachment(0);
            formData.top = new FormAttachment(connectTimeoutText, ALIGN_VERTICAL, SWT.BOTTOM);
            formData.right = new FormAttachment(100);
            formData.bottom = new FormAttachment(100);
            errorComposite.setLayoutData(formData);

            detailMessageText = new Text(errorComposite, SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL);
            detailMessageText.setEditable(false);
            errorComposite.setClient(detailMessageText);
        }

    }

    private void initData() {
        networkConfiguration = NetworkConfiguration.getInstance();

        informationLabel.setText(information);

        int connectionTimeout = networkConfiguration.getConnectionTimeout();
        String timeoutStr = String.valueOf(connectionTimeout);
        connectTimeoutText.setText(timeoutStr);
        connectTimeoutText.setSelection(timeoutStr.length());

        if ((exceptionString == null || exceptionString.isEmpty()) && ex != null) {
            StringWriter stringWriter = new StringWriter();
            ex.printStackTrace(new PrintWriter(stringWriter));
            exceptionString = stringWriter.toString();
        } else {
            exceptionString = "";
        }

        if (this.detailMessageText != null) {
            detailMessageText.setText(exceptionString);
        }
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

    }

    public boolean isComplete() {
        boolean checkConnectionTimeout = checkConnectionTimeout();
        return checkConnectionTimeout;
    }


    public void performFinish() {
        int timeout = Integer.valueOf(connectTimeoutText.getText());
        networkConfiguration.setConnectionTimeout(timeout);
        networkConfiguration.setReadTimeout(timeout);
    }

    public void performDefault() {
        networkConfiguration.reset();
        initData();
    }

    private boolean checkConnectionTimeout() {
        connectTimeoutText.setBackground(null);
        String timeoutStr = connectTimeoutText.getText();
        if (StringUtils.isEmpty(timeoutStr)) {
            showError(connectTimeoutText, Messages.getString("NetworkErrorRetryForm.connectTimeout.error.bound", //$NON-NLS-1$
                    NetworkConfiguration.CONNECTION_TIMEOUT_MIN, NetworkConfiguration.CONNECTION_TIMEOUT_MAX));
            return false;
        }
        try {
            int timeout = Integer.valueOf(timeoutStr);
            if (NetworkConfiguration.CONNECTION_TIMEOUT_MAX < timeout || timeout < NetworkConfiguration.CONNECTION_TIMEOUT_MIN) {
                showError(connectTimeoutText, Messages.getString("NetworkErrorRetryForm.connectTimeout.error.bound", //$NON-NLS-1$
                        NetworkConfiguration.CONNECTION_TIMEOUT_MIN, NetworkConfiguration.CONNECTION_TIMEOUT_MAX));
                return false;
            }
        } catch (NumberFormatException e) {
            showError(connectTimeoutText, Messages.getString("NetworkErrorRetryForm.connectTimeout.error.bound", //$NON-NLS-1$
                    NetworkConfiguration.CONNECTION_TIMEOUT_MIN, NetworkConfiguration.CONNECTION_TIMEOUT_MAX));
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
