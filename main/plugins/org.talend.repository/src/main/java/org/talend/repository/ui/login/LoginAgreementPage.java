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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.eclipse.core.runtime.Platform;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.talend.commons.exception.BusinessException;
import org.talend.commons.exception.CommonExceptionHandler;
import org.talend.commons.ui.gmf.util.DisplayUtils;
import org.talend.commons.ui.swt.dialogs.ErrorDialogWidthDetailArea;
import org.talend.registration.RegistrationPlugin;
import org.talend.registration.license.LicenseManagement;
import org.talend.repository.i18n.Messages;

/**
 * created by cmeng on May 12, 2015 Detailled comment
 *
 */
public class LoginAgreementPage extends AbstractLoginActionPage {

    protected static final String LICENSE_FILE_PATH = "/license.txt"; //$NON-NLS-1$

    protected static final String LICENSE_FILE_PATH_HTML = "/license.html"; //$NON-NLS-1$

    /** CLUF field. */
    private Text clufText;

    private Browser clufBrowser;

    private Button acceptButton;

    public LoginAgreementPage(Composite parent, LoginDialogV2 dialog, int style) {
        super(parent, dialog, style);
    }

    @Override
    public void createControl(Composite parentCtrl) {
        Composite container = new Composite(parentCtrl, SWT.NONE);
        container.setLayout(new FormLayout());
        Label titleLabel = new Label(container, SWT.WRAP);
        titleLabel.setFont(LoginDialogV2.fixedFont);
        titleLabel.setText(Messages.getString("LoginAgreementPage.title")); //$NON-NLS-1$
        FormData titleLabelFormData = new FormData();
        titleLabelFormData.left = new FormAttachment(0, 0);
        titleLabelFormData.top = new FormAttachment(0, 0);
        titleLabel.setLayoutData(titleLabelFormData);

        acceptButton = new Button(container, SWT.CENTER);
        acceptButton.setBackground(backgroundBtnColor);
        acceptButton.setFont(LoginDialogV2.fixedFont);
        acceptButton.setText(Messages.getString("LoginAgreementPage.accept")); //$NON-NLS-1$

        FormData acceptButtonFormLayoutData = new FormData();
        acceptButtonFormLayoutData.bottom = new FormAttachment(100, 0);
        acceptButtonFormLayoutData.right = new FormAttachment(100, 0);
        acceptButtonFormLayoutData.left = new FormAttachment(100, -1 * LoginDialogV2.getNewButtonSize(acceptButton).x);
        acceptButton.setLayoutData(acceptButtonFormLayoutData);

        boolean haveHtmlDesc = false;
        FileInputStream licenseInputStream = null;
        String licenseFileBasePath = Platform.getInstallLocation().getURL().getPath();
        if (Boolean.parseBoolean(System.getProperty("USE_BROWSER"))) { //$NON-NLS-1$
            File htmlFile = new File(licenseFileBasePath + LICENSE_FILE_PATH_HTML);
            if (htmlFile.exists()) {
                try {
                    licenseInputStream = new FileInputStream(htmlFile);
                    if (licenseInputStream != null) {
                        haveHtmlDesc = true;
                    }
                } catch (FileNotFoundException e) {
                    CommonExceptionHandler.process(e);
                }
            }
        }
        if (licenseInputStream == null) {
            try {
                licenseInputStream = new FileInputStream(licenseFileBasePath + LICENSE_FILE_PATH);
            } catch (FileNotFoundException e) {
                CommonExceptionHandler.process(e);
            }
        }

        FormData clufLayoutData = new FormData();
        clufLayoutData.top = new FormAttachment(titleLabel, LoginDialogV2.TAB_VERTICAL_PADDING_LEVEL_1, SWT.BOTTOM);
        clufLayoutData.left = new FormAttachment(0, 0);
        clufLayoutData.right = new FormAttachment(100, 0);
        clufLayoutData.bottom = new FormAttachment(acceptButton, -1 * LoginDialogV2.TAB_VERTICAL_PADDING_LEVEL_1, SWT.TOP);

        if (haveHtmlDesc) {
            clufBrowser = new Browser(container, SWT.BORDER);
            clufBrowser.setText(getLicense(licenseInputStream));
            clufBrowser.setLayoutData(clufLayoutData);
        } else {
            clufText = new Text(container, SWT.MULTI | SWT.WRAP | SWT.V_SCROLL | SWT.H_SCROLL | SWT.LEFT | SWT.BORDER);
            clufText.setBackground(new Color(null, 255, 255, 255));
            Font font = new Font(DisplayUtils.getDisplay(), "courier", 10, SWT.NONE); //$NON-NLS-1$
            clufText.setFont(font);
            clufText.setEditable(false);
            clufText.setText(getLicense(licenseInputStream));
            clufText.setLayoutData(clufLayoutData);
        }
    }

    private String getLicense(InputStream inputStream) {
        String licenseNotFound = Messages.getString("LoginAgreementPage.agreementFileNotFound"); //$NON-NLS-1$
        if (inputStream == null) {
            return licenseNotFound;
        }
        String license = ""; //$NON-NLS-1$
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(inputStream));

            String licenseLine = ""; //$NON-NLS-1$
            while ((licenseLine = in.readLine()) != null) {
                license += licenseLine + "\n"; //$NON-NLS-1$
            }

        } catch (FileNotFoundException e) {
            CommonExceptionHandler.process(e);
        } catch (IOException e) {
            CommonExceptionHandler.process(e);
        }
        if (license.isEmpty()) {
            license = licenseNotFound;
        }
        return license;
    }

    @Override
    public AbstractActionPage getNextPage() {
        AbstractActionPage iNextPage = super.getNextPage();

        if (iNextPage == null) {
            iNextPage = loginDialog.getFirstTimeStartupPageIfNeeded();
            if (iNextPage == null) {
                iNextPage = new LoginProjectPage(getParent(), loginDialog, SWT.NONE);
            }
            setNextPage(iNextPage);
        }

        return iNextPage;
    }

    @Override
    public void addListeners() {
        acceptButton.addSelectionListener(new SelectionListener() {

            @Override
            public void widgetSelected(SelectionEvent event) {
                try {
                    LicenseManagement.acceptLicense();
                } catch (BusinessException e) {
                    ErrorDialogWidthDetailArea errorDialog = new ErrorDialogWidthDetailArea(getShell(),
                            RegistrationPlugin.PLUGIN_ID, "", e.getMessage()); //$NON-NLS-1$
                    System.exit(0);
                }

                AbstractActionPage iNextPage = getNextPage();
                if (iNextPage == null) {
                    return;
                }
                try {
                    gotoNextPage();
                } catch (Throwable e1) {
                    CommonExceptionHandler.process(e1);
                }
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
                // nothing need to do
            }
        });
    }

    @Override
    public void preCreateControl() {
        // nothing need to do
    }

    @Override
    public void afterCreateControl() {
        // nothing need to do
    }

    @Override
    public void refreshUIData() {
        acceptButton.getShell().setDefaultButton(acceptButton);
    }

    @Override
    public void check() {
        // nothing need to do
    }

    @Override
    public Object getCheckedErrors() {
        return null;
    }

}
