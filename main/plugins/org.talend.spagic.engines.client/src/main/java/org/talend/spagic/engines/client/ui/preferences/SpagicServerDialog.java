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
package org.talend.spagic.engines.client.ui.preferences;

import java.util.Collections;
import java.util.List;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.talend.spagic.engines.client.i18n.Messages;

/**
 * DOC cantoine class global comment. Detailled comment <br/>
 *
 * $Id: SpagoBiServerDialog.java 2738 2007-03-28 13:12:27Z cantoine $
 *
 */
public class SpagicServerDialog extends Dialog {

    private String engineName = ""; //$NON-NLS-1$

    private String shortDescription = ""; //$NON-NLS-1$

    private String host = ""; //$NON-NLS-1$

    private String port = ""; //$NON-NLS-1$

    private String login = ""; //$NON-NLS-1$

    private String password = ""; //$NON-NLS-1$

    // private String applicationContext = ""; //$NON-NLS-1$

    private Button okButton;

    private Text engineNameText;

    private Text shortDescriptionText;

    private Text hostText;

    private Text portText;

    private Text loginText;

    private Text passwordText;

    // private Text applicationContextText;

    private Label errorMessageText;

    private boolean creation = false;

    private final List existingServers;

    public SpagicServerDialog(Shell parentShell, List existingServers) {
        this(parentShell, existingServers, null, null, null, null, null, null, null);
        creation = true;
    }

    public SpagicServerDialog(Shell parentShell, List existingServers, String initialEngine, String initialDescription,
            String initialHost, String initialLogin, String initialPassword, String initialPort, String initialContext) {
        super(parentShell);
        this.existingServers = existingServers == null ? Collections.EMPTY_LIST : existingServers;
        engineName = initialEngine == null ? "" : initialEngine; //$NON-NLS-1$
        shortDescription = initialDescription == null ? "" : initialDescription; //$NON-NLS-1$
        host = initialHost == null ? "" : initialHost; //$NON-NLS-1$
        port = initialPort == null ? "" : initialPort; //$NON-NLS-1$
        login = initialLogin == null ? "" : initialLogin; //$NON-NLS-1$
        password = initialPassword == null ? "" : initialPassword; //$NON-NLS-1$
        // applicationContext = initialContext == null ? "" : initialContext; //$NON-NLS-1$
    }

    @Override
    protected void buttonPressed(int buttonId) {
        if (buttonId == IDialogConstants.OK_ID) {
            shortDescription = shortDescriptionText.getText();
            engineName = engineNameText.getText();
            host = hostText.getText();
            port = portText.getText();
            login = loginText.getText();
            password = passwordText.getText();
            // applicationContext = applicationContextText.getText();

        } else {
            shortDescription = ""; //$NON-NLS-1$
            engineName = ""; //$NON-NLS-1$
            host = ""; //$NON-NLS-1$
            port = ""; //$NON-NLS-1$
            login = ""; //$NON-NLS-1$
            password = ""; //$NON-NLS-1$
            // applicationContext = ""; //$NON-NLS-1$
        }
        super.buttonPressed(buttonId);
    }

    @Override
    protected void configureShell(Shell shell) {
        super.configureShell(shell);
        if (creation) {
            shell.setText(Messages.getString("SpagicServerDialog.shellText.createNewServer")); //$NON-NLS-1$
        } else {
            shell.setText(Messages.getString("SpagicServerDialog.shellText.editServer")); //$NON-NLS-1$
        }
    }

    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        // create OK and Cancel buttons by default
        okButton = createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
        createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
        // do this here because setting the text will set enablement on the ok
        // button
        if (engineName != null) {
            engineNameText.setText(engineName);
            engineNameText.selectAll();
        }
        if (shortDescription != null) {
            shortDescriptionText.setText(shortDescription);
        }
        if (creation) {
            engineNameText.setFocus();
        } else {
            engineNameText.setEnabled(false);
            shortDescriptionText.setFocus();
        }
    }

    /*
     * (non-Javadoc) Method declared on Dialog.
     */
    @Override
    protected Control createDialogArea(Composite parent) {
        // create composite
        Composite composite = (Composite) super.createDialogArea(parent);
        // ((GridData) composite.getLayoutData()).widthHint = 400;
        // ((GridData) composite.getLayoutData()).heightHint = 400;
        // create message
        // if (message != null) {
        // Label label = new Label(composite, SWT.WRAP);
        // label.setText(message);
        // GridData data = new GridData(GridData.GRAB_HORIZONTAL
        // | GridData.GRAB_VERTICAL | GridData.HORIZONTAL_ALIGN_FILL
        // | GridData.VERTICAL_ALIGN_CENTER);
        // data.widthHint = convertHorizontalDLUsToPixels(IDialogConstants.MINIMUM_MESSAGE_AREA_WIDTH);
        // label.setLayoutData(data);
        // label.setFont(parent.getFont());
        // }
        GridLayout layout = new GridLayout();
        layout.numColumns = 3;
        composite.setLayout(layout);
        Label engineNameLabel = new Label(composite, SWT.None);
        engineNameLabel.setText(Messages.getString("SpagicServerDialog.engineNameText")); //$NON-NLS-1$
        // GridData data = new GridData(GridData.GRAB_VERTICAL | GridData.HORIZONTAL_ALIGN_FILL |
        // GridData.VERTICAL_ALIGN_CENTER);
        // engineNameLabel.setLayoutData(data);
        engineNameLabel.setFont(parent.getFont());
        engineNameText = new Text(composite, SWT.SINGLE | SWT.BORDER);
        GridData data1 = new GridData(GridData.GRAB_HORIZONTAL);
        data1.minimumWidth = 150;
        engineNameText.setLayoutData(data1);
        engineNameText.addModifyListener(new ModifyListener() {

            public void modifyText(ModifyEvent e) {
                validateInput();
            }
        });

        Label shortDescriptionLabel = new Label(composite, SWT.WRAP);
        // data = new GridData(GridData.FILL_HORIZONTAL);
        // shortDescriptionLabel.setLayoutData(data);

        shortDescriptionLabel = new Label(composite, SWT.WRAP);
        shortDescriptionLabel.setText(Messages.getString("SpagicServerDialog.shortDescription")); //$NON-NLS-1$
        shortDescriptionLabel.setFont(parent.getFont());
        shortDescriptionText = new Text(composite, SWT.SINGLE | SWT.BORDER);
        shortDescriptionText.setLayoutData(data1);
        shortDescriptionText.addModifyListener(new ModifyListener() {

            public void modifyText(ModifyEvent e) {
                validateInput();
            }
        });

        Label hostLabel = new Label(composite, SWT.WRAP);
        // data = new GridData(GridData.FILL_HORIZONTAL);
        // hostLabel.setLayoutData(data);

        hostLabel = new Label(composite, SWT.WRAP);
        hostLabel.setText(Messages.getString("SpagicServerDialog.host")); //$NON-NLS-1$
        hostLabel.setFont(parent.getFont());
        hostText = new Text(composite, SWT.SINGLE | SWT.BORDER);
        hostText.setLayoutData(data1);
        hostText.addModifyListener(new ModifyListener() {

            public void modifyText(ModifyEvent e) {
                validateInput();
            }
        });

        Label portLabel = new Label(composite, SWT.WRAP);
        // data = new GridData(GridData.FILL_HORIZONTAL);
        // portLabel.setLayoutData(data);

        portLabel = new Label(composite, SWT.WRAP);
        portLabel.setText(Messages.getString("SpagicServerDialog.port")); //$NON-NLS-1$
        portLabel.setFont(parent.getFont());
        portText = new Text(composite, SWT.SINGLE | SWT.BORDER);
        portText.setLayoutData(data1);
        portText.addModifyListener(new ModifyListener() {

            public void modifyText(ModifyEvent e) {
                validateInput();
            }
        });

        Label loginLabel = new Label(composite, SWT.WRAP);
        // data = new GridData(GridData.FILL_HORIZONTAL);
        // loginLabel.setLayoutData(data);

        loginLabel = new Label(composite, SWT.WRAP);
        loginLabel.setText(Messages.getString("SpagicServerDialog.login")); //$NON-NLS-1$
        loginLabel.setFont(parent.getFont());
        loginText = new Text(composite, SWT.SINGLE | SWT.BORDER);
        loginText.setLayoutData(data1);
        loginText.addModifyListener(new ModifyListener() {

            public void modifyText(ModifyEvent e) {
                validateInput();
            }
        });

        Label passwordLabel = new Label(composite, SWT.WRAP);
        // data = new GridData(GridData.FILL_HORIZONTAL);
        // passwordLabel.setLayoutData(data);

        passwordLabel = new Label(composite, SWT.WRAP);
        passwordLabel.setText(Messages.getString("SpagicServerDialog.password")); //$NON-NLS-1$
        passwordLabel.setFont(parent.getFont());
        passwordText = new Text(composite, SWT.SINGLE | SWT.BORDER | SWT.PASSWORD);
        passwordText.setLayoutData(data1);
        passwordText.addModifyListener(new ModifyListener() {

            public void modifyText(ModifyEvent e) {
                validateInput();
            }
        });

        // Label applicationContextLabel = new Label(composite, SWT.WRAP);
        // data = new GridData(GridData.FILL_HORIZONTAL);
        // applicationContextLabel.setLayoutData(data);
        //
        // applicationContextLabel = new Label(composite, SWT.WRAP);
        // applicationContextLabel.setText(Messages.getString("SpagoBiServerDialog.applicationContext")); //$NON-NLS-1$
        // applicationContextLabel.setFont(parent.getFont());
        // applicationContextText = new Text(composite, SWT.SINGLE | SWT.BORDER);
        // applicationContextText.addModifyListener(new ModifyListener() {
        //
        // public void modifyText(ModifyEvent e) {
        // validateInput();
        // }
        // });

        errorMessageText = new Label(composite, SWT.NONE);
        // GridData data = new GridData(GridData.FILL_HORIZONTAL);
        // errorMessageText.setLayoutData(data);
        // errorMessageText.setLayoutData(data);
        errorMessageText.setBackground(errorMessageText.getDisplay().getSystemColor(SWT.COLOR_WIDGET_BACKGROUND));

        applyDialogFont(composite);
        return composite;
    }

    protected Button getOkButton() {
        return okButton;
    }

    protected void validateInput() {
        String errorMessage = null;
        if (creation && engineNameText.getText().equals("")) { //$NON-NLS-1$
            errorMessage = Messages.getString("SpagicServerDialog.errorMessage.engineNameLetters"); //$NON-NLS-1$
        } else if (creation && existingServers.contains(engineNameText.getText())) {
            errorMessage = Messages.getString("SpagicServerDialog.errorMessage.engineNameUsed"); //$NON-NLS-1$
        } else if (shortDescriptionText.getText().equals("")) { //$NON-NLS-1$
            errorMessage = "Description cannot be empty."; //$NON-NLS-1$
        } else if (hostText.getText().equals("")) { //$NON-NLS-1$
            errorMessage = "Host cannot be empty."; //$NON-NLS-1$
        } else if (portText.getText().equals("")) { //$NON-NLS-1$
            errorMessage = "Port cannot be empty."; //$NON-NLS-1$
        } else if (loginText.getText().equals("")) { //$NON-NLS-1$
            errorMessage = "Login cannot be empty."; //$NON-NLS-1$
        } else if (passwordText.getText().equals("")) { //$NON-NLS-1$
            errorMessage = "Password cannot be empty."; //$NON-NLS-1$
        }
        // else if (applicationContextText.getText().equals("")) { //$NON-NLS-1$
        // errorMessage = "Application context cannot be empty."; //$NON-NLS-1$
        // }
        setErrorMessage(errorMessage);
    }

    public void setErrorMessage(String errorMessage) {
        if (errorMessageText != null && !errorMessageText.isDisposed()) {
            errorMessageText.setText(errorMessage == null ? "" : errorMessage); //$NON-NLS-1$
            errorMessageText.getParent().update();
            // Access the ok button by id, in case clients have overridden button creation.
            // See https://bugs.eclipse.org/bugs/show_bug.cgi?id=113643
            Control button = getButton(IDialogConstants.OK_ID);
            if (button != null) {
                button.setEnabled(errorMessage == null);
            }
        }
    }

    /**
     * Getter for engineName.
     *
     * @return the engineName
     */
    public String getEngineName() {
        return this.engineName;
    }

    /**
     * Getter for shortDescription.
     *
     * @return the shortDescription
     */
    public String getShortDescription() {
        return this.shortDescription;
    }

    /**
     * Getter for host.
     *
     * @return the host
     */
    public String getHost() {
        return this.host;
    }

    /**
     * Getter for port.
     *
     * @return the port
     */
    public String getPort() {
        return this.port;
    }

    /**
     * Getter for login.
     *
     * @return the login
     */
    public String getLogin() {
        return this.login;
    }

    /**
     * Getter for password.
     *
     * @return the password
     */
    public String getPassword() {
        return this.password;
    }

    /**
     * Getter for applicationContext.
     *
     * @return the applicationContext
     */
    // public String getApplicationContext() {
    // return this.applicationContext;
    // }
}
