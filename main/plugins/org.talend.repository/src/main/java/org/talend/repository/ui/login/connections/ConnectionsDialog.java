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
package org.talend.repository.ui.login.connections;

import java.util.List;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.jface.preference.PreferenceManager;
import org.eclipse.jface.preference.PreferenceNode;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Shell;
import org.talend.commons.ui.runtime.image.ImageProvider;
import org.talend.commons.utils.network.NetworkUtil;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.PluginChecker;
import org.talend.core.model.general.ConnectionBean;
import org.talend.core.ui.branding.IBrandingService;
import org.talend.repository.i18n.Messages;
import org.talend.repository.ui.login.connections.network.LoginNetworkPreferencePage;
import org.talend.repository.ui.login.connections.network.proxy.LoginProxyPreferencePage;

/**
 * DOC smallet class global comment. Detailled comment <br/>
 *
 * $Id: talend.epf 1 2006-09-29 17:06:40 +0000 (ven., 29 sept. 2006) nrousseau $
 *
 */
public class ConnectionsDialog extends TitleAreaDialog {

    private static final int FORM_COMPOSITE_HEIGHT = 350;

    private static final int FORM_COMPOSITE_WIDTH = 420;

    private static final int LIST_COMPOSITE_WIDTH = 220;

    private ConnectionsListComposite listComposite;

    private ConnectionFormComposite formComposite;

    public static final int STANDARD_LABEL_WIDTH = 102;

    public static final int STANDARD_TEXT_WIDTH = 260;

    public static final int VSPACE = 5;

    public static final int HSPACE = 5;

    private ConnectionBean defaultConnectionSelected;

    private boolean hasAuthorizationError = false;

    public ConnectionsDialog(Shell parentShell) {
        super(parentShell);
        setShellStyle(getShellStyle() | SWT.RESIZE);
        // RGB rgb = parentShell.getBackground().getRGB();
        // setTitleAreaColor(rgb);
        setTitleImage();
        setTitleAreaColor(new RGB(255, 255, 255));
    }

    public ConnectionsDialog(Shell parentShell, ConnectionBean defaultConnectionSelected, boolean hasAuthorizationError) {
        super(parentShell);
        setShellStyle(getShellStyle() | SWT.RESIZE);
        setTitleImage();
        setTitleAreaColor(new RGB(255, 255, 255));
        this.defaultConnectionSelected = defaultConnectionSelected;
        this.hasAuthorizationError = hasAuthorizationError;
    }

    protected void setTitleImage() {
        IBrandingService brandingService = (IBrandingService) GlobalServiceRegister.getDefault().getService(
                IBrandingService.class);
        ImageDescriptor imgDesc = brandingService.getLoginHImage();
        if (imgDesc == null) {
            return;
        }
        Image originalImage = ImageProvider.getImage(imgDesc);
        if (originalImage == null) {
            return;
        }
        Rectangle originalImageSize = originalImage.getBounds();
        Image scaled = new Image(Display.getDefault(), originalImageSize.width + 10, originalImageSize.height);
        GC gc = new GC(scaled);
        gc.setAntialias(SWT.ON);
        gc.setInterpolation(SWT.HIGH);
        gc.drawImage(originalImage, 0, 0, originalImage.getBounds().width, originalImage.getBounds().height, 0, 0,
                originalImageSize.width, originalImageSize.height);
        gc.dispose();
        originalImage.dispose();
        setTitleImage(scaled);
    }

    @Override
    public Button getOKButton() {
        return this.getButton(OK);
    }

    @Override
    protected void configureShell(final Shell newShell) {
        super.configureShell(newShell);
        IBrandingService brandingService = (IBrandingService) GlobalServiceRegister.getDefault().getService(
                IBrandingService.class);
        newShell.setText(Messages.getString("LoginDialog.title", brandingService.getFullProductName())); //$NON-NLS-1$
    }

    @Override
    protected Control createDialogArea(final Composite parent) {
        Composite composite = (Composite) super.createDialogArea(parent);

        Composite container = new Composite(composite, SWT.NONE);
        container.setBackground(ColorConstants.white);
        GridDataFactory.fillDefaults().grab(true, true).applyTo(container);

        GridLayout layout = new GridLayout(2, false);
        layout.marginWidth = 0;
        layout.marginHeight = 0;
        layout.horizontalSpacing = 0;
        layout.verticalSpacing = 0;
        container.setLayout(layout);

        listComposite = new ConnectionsListComposite(container, SWT.NONE, this);
        GridDataFactory.fillDefaults().hint(LIST_COMPOSITE_WIDTH, SWT.DEFAULT).grab(false, true).span(1, 2)
                .applyTo(listComposite);

        formComposite = new ConnectionFormComposite(container, SWT.NONE, listComposite, this);
        GridDataFactory.fillDefaults().hint(FORM_COMPOSITE_WIDTH, FORM_COMPOSITE_HEIGHT).grab(true, true).applyTo(formComposite);
        listComposite.setConnectionsFormComposite(formComposite);

        createNetworkSettings(container);

        Label titleBarSeparator = new Label(composite, SWT.HORIZONTAL | SWT.SEPARATOR);
        titleBarSeparator.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        if (defaultConnectionSelected != null && hasAuthorizationError) {
            setErrorMessage(Messages.getString("ConnectionsDialog.usernameOrPasswordInvalid"));
        }
        return composite;
    }

    private void createNetworkSettings(Composite container) {
        if (!PluginChecker.isTIS()) {
            return;
        }
        Composite bottomPart = new Composite(container, SWT.NONE);
        bottomPart.setBackground(ColorConstants.white);
        GridDataFactory.fillDefaults().align(SWT.LEFT, SWT.BOTTOM).applyTo(bottomPart);
        FormLayout bottomPartLayout = new FormLayout();
        bottomPartLayout.marginLeft = HSPACE;
        bottomPartLayout.marginBottom = VSPACE;
        bottomPart.setLayout(bottomPartLayout);

        Link networkSettings = new Link(bottomPart, SWT.NONE);
        networkSettings.setBackground(ColorConstants.white);
        networkSettings.setText("<a>" //$NON-NLS-1$
                + Messages.getString("ConnectionsDialog.ui.networkSettings") //$NON-NLS-1$
                + "</a>");//$NON-NLS-1$

        networkSettings.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                onNetworkSettingsClicked();
            }

        });
    }

    private void onNetworkSettingsClicked() {
        PreferenceDialog pd = new PreferenceDialog(getShell(), new PreferenceManager());

        LoginProxyPreferencePage prefPage = new LoginProxyPreferencePage();
        prefPage.setTitle(Messages.getString("ConnectionsDialog.ui.networkSettings.proxy.title"));
        pd.getPreferenceManager().addToRoot(new PreferenceNode("proxyPage", prefPage));

        LoginNetworkPreferencePage networkPage = new LoginNetworkPreferencePage();
        networkPage.setTitle(Messages.getString("ConnectionsDialog.ui.networkSettings.timeout.title"));
        pd.getPreferenceManager().addToRoot(new PreferenceNode("timeoutPage", networkPage));

        int open = pd.open();
        if (Window.OK == open) {
            NetworkUtil.loadAuthenticator();
        }
    }

    public List<ConnectionBean> getConnections() {
        return listComposite.getList();
    }

    public ConnectionBean getDefaultConnectionSelected() {
        return defaultConnectionSelected;
    }

}
