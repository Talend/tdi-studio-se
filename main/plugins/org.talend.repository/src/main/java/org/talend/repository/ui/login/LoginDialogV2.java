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

import java.util.List;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.jface.dialogs.TrayDialog;
import org.eclipse.jface.resource.ColorRegistry;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.talend.commons.exception.CommonExceptionHandler;
import org.talend.commons.utils.system.EnvironmentUtils;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.model.general.ConnectionBean;
import org.talend.core.model.general.Project;
import org.talend.core.ui.branding.IBrandingConfiguration;
import org.talend.core.ui.branding.IBrandingService;
import org.talend.registration.license.LicenseManagement;
import org.talend.repository.i18n.Messages;
import org.talend.repository.ui.login.connections.ConnectionUserPerReader;

/**
 * Login dialog. <br/>
 *
 * $Id$
 *
 */
public class LoginDialogV2 extends TrayDialog {

    public static final String FONT_TALEND_FOR_LOGIN_UI = "FONT_TALEND_FOR_LOGIN_UI"; //$NON-NLS-1$

    public static final String COLOR_LOGON_DIALOG_BACKGROUND = "COLOR_LOGON_DIALOG_BACKGROUND"; //$NON-NLS-1$

    public static final String FONT_ARIAL = "Arial"; //$NON-NLS-1$

    public static final int BUTTON_CONTENT_HORIZONTAL_PADDING = 12;

    public static final int TAB_HORIZONTAL_PADDING_LEVEL_1 = 12;

    public static final int TAB_HORIZONTAL_PADDING_LEVEL_2 = 6;

    public static final int TAB_HORIZONTAL_PADDING_LEVEL_3 = 2;

    public static final int TAB_VERTICAL_PADDING_LEVEL_1 = 10;

    public static final int TAB_VERTICAL_PADDING_LEVEL_2 = 3;

    /**
     * Colors used for Remote Object background when enabled.
     */
    public static final Color WHITE_COLOR = org.talend.commons.ui.runtime.ColorConstants.WHITE_COLOR;

    public static final Color GREY_COLOR = org.talend.commons.ui.runtime.ColorConstants.GREY_COLOR;

    public static final Color YELLOW_GREEN_COLOR = org.talend.commons.ui.runtime.ColorConstants.YELLOW_GREEN_COLOR;

    public static final Color YELLOW_COLOR = org.talend.commons.ui.runtime.ColorConstants.YELLOW_COLOR;

    public static final Color RED_COLOR = org.talend.commons.ui.runtime.ColorConstants.RED_COLOR;

    public static final Color VERTICAL_SEPERATOR_LINE_COLOR = org.talend.commons.ui.runtime.ColorConstants.VERTICAL_SEPERATOR_LINE_COLOR;

    protected double realHeightRate;

    protected double realWidthRate;

    protected static Font errorFont;

    protected static Font errorFontBorder;

    protected static Font fixedFont;

    protected Color backgroundColor;

    protected Composite brandingArea;

    protected Composite errorMessageArea;

    protected StyledText errorTextLabel;

    private ConnectionUserPerReader perReader;

    private Composite base;

    private StackLayout stackLayout;

    protected int logonInfoAreaWidth;

    /**
     * Construct a new LoginDialog.
     *
     * @param parentShell Parent shell.
     */
    public LoginDialogV2(Shell parentShell) {
        super(parentShell);
        perReader = ConnectionUserPerReader.getInstance();
        setHelpAvailable(false);

        ColorRegistry colorRegistry = JFaceResources.getColorRegistry();
        backgroundColor = colorRegistry.get(COLOR_LOGON_DIALOG_BACKGROUND);
        if (backgroundColor == null) {
            backgroundColor = ColorConstants.white;
            // backgroundColor = new Color(null, 215, 215, 215);
            colorRegistry.put(COLOR_LOGON_DIALOG_BACKGROUND, backgroundColor.getRGB());
        }
    }

    protected void initFont() {
        if (errorFont == null || errorFont.isDisposed()) {
            errorFont = new Font(null, FONT_ARIAL, 9, SWT.BOLD);
        }

        if (errorFontBorder == null || errorFontBorder.isDisposed()) {
            errorFontBorder = new Font(null, FONT_ARIAL, 10, SWT.BOLD);
        }

        if (fixedFont == null || fixedFont.isDisposed()) {
            // fixedFont = FontDescriptor.createFrom(getShell().getDisplay().getSystemFont()).setHeight(9)
            // .createFont(getShell().getDisplay());
            fixedFont = getShell().getDisplay().getSystemFont();
            JFaceResources.getFontRegistry().put(FONT_TALEND_FOR_LOGIN_UI, fixedFont.getFontData());
        }
    }

    @Override
    protected void initializeBounds() {
        super.initializeBounds();
        Point location = getInitialLocation(getShell().getSize());
        getShell().setLocation(location.x, location.y);
    }

    /**
     * @see org.eclipse.jface.window.Window#configureShell(org.eclipse.swt.widgets.Shell)
     */
    @Override
    protected void configureShell(final Shell newShell) {
        super.configureShell(newShell);
        IBrandingService brandingService = (IBrandingService) GlobalServiceRegister.getDefault().getService(
                IBrandingService.class);
        newShell.setText(Messages.getString("LoginDialog.title", brandingService.getFullProductName())); //$NON-NLS-1$
    }

    @Override
    protected Control createContents(Composite parent) {
        Composite composite = new Composite(parent, 0);
        GridLayout layout = new GridLayout();
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        layout.verticalSpacing = 0;
        composite.setLayout(layout);
        composite.setLayoutData(new GridData(GridData.FILL_BOTH));
        applyDialogFont(composite);
        // initialize the dialog units
        initializeDialogUnits(composite);
        // create the dialog area and button bar
        dialogArea = createDialogArea(composite);
        return composite;
    }

    /**
     * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
     */
    @Override
    protected Control createDialogArea(final Composite parent) {
        if (!perReader.isHaveUserPer()) {
            perReader.createPropertyFile();
        }
        initFont();

        Composite container = new Composite(parent, SWT.NONE);

        calcFontRate(container);

        GridLayout layout = new GridLayout(3, false);
        layout.marginWidth = 0;
        layout.marginHeight = 0;
        layout.horizontalSpacing = 0;
        layout.verticalSpacing = 0;
        container.setLayout(layout);
        container.setBackground(backgroundColor);
        container.setBackgroundMode(SWT.INHERIT_DEFAULT);

        createBrandingArea(container);

        createLogonInfoArea(container);

        showFirstPage();

        return container;
    }

    /**
     * To fix the big font problem
     *
     * @param container
     */
    protected void calcFontRate(Composite container) {

        /**********************************************************************************
         * DO NOT MODIFY THEM! those size is calc from [testFontLabel.setText("Data_WM");] in Windows and Mac
         */
        final String TEST_FONT_STRING = "Data_WM"; //$NON-NLS-1$

        // for Windows and Linux
        int STANDARD_BASE_HEIGHT = 15;
        int STANDARD_BASE_WIDTH = 51;
        logonInfoAreaWidth = 390;

        // for Mac
        boolean isMac = EnvironmentUtils.isMacOsSytem();
        if (isMac) {
            STANDARD_BASE_HEIGHT = 14;
            STANDARD_BASE_WIDTH = 56;
        }
        /*********************************************************************************/

        Label testFontLabel = new Label(container, SWT.NONE);
        testFontLabel.setText(TEST_FONT_STRING);
        Point testFontSize = testFontLabel.computeSize(SWT.DEFAULT, SWT.DEFAULT);
        realWidthRate = 1.0 * testFontSize.x / STANDARD_BASE_WIDTH;
        realHeightRate = 1.0 * testFontSize.y / STANDARD_BASE_HEIGHT;
        testFontLabel.dispose();

        if (realWidthRate < 1.0 && realHeightRate < 1.0) {
            realWidthRate = 1.0;
            realHeightRate = 1.0;
        } else if (Math.abs(1.0 - realWidthRate) < 0.05 && Math.abs(1.0 - realHeightRate) < 0.05) {
            realWidthRate = 1.0;
            realHeightRate = 1.0;
        }
    }

    protected void showFirstPage() {
        AbstractLoginActionPage loginPage = null;
        boolean isAcceptAgreement = LicenseManagement.isLicenseValidated();
        if (!isAcceptAgreement) {
            // must accept agreement
            loginPage = new LoginAgreementPage(base, this, SWT.NONE);
        } else if (LoginHelper.isTalendLogonFirstTimeStartup()) {
            loginPage = getFirstTimeStartupPageIfNeeded();
        }

        if (loginPage == null) {
            loginPage = new LoginProjectPage(base, this, SWT.NONE);
        }

        try {
            loginPage.preShowPage();
        } catch (Throwable e) {
            CommonExceptionHandler.process(e);
        }
        loginPage.showPage();
    }

    protected AbstractLoginActionPage getFirstTimeStartupPageIfNeeded() {
        AbstractLoginActionPage loginPage = null;

        IBrandingService service = (IBrandingService) GlobalServiceRegister.getDefault().getService(IBrandingService.class);
        if (service != null) {
            IBrandingConfiguration brandingConfiguration = service.getBrandingConfiguration();
            if (brandingConfiguration != null && brandingConfiguration.isOnlyRemoteConnection()) {
                return null;
            }
        }

        // try to find if there are projects in workspace
        Project[] projects = LoginHelper.getInstance().getProjects(LoginHelper.createDefaultLocalConnection());
        if (projects == null || projects.length == 0) {
            List<ConnectionBean> storedConnections = LoginHelper.getInstance().getStoredConnections();
            if (storedConnections == null
                    || storedConnections.isEmpty()
                    || (storedConnections.size() == 1 && !LoginHelper.isRemotesConnection(storedConnections.get(0)) && LoginHelper
                            .isWorkspaceSame(storedConnections.get(0)))) {
                // for local license case
                loginPage = new LoginFirstTimeStartupActionPage(base, this, SWT.NONE);
            }
        }
        return loginPage;
    }

    protected void createLogonInfoArea(Composite container) {
        // vertical seperator line
        Composite verticalLine = new Composite(container, SWT.NONE);
        verticalLine.setBackground(VERTICAL_SEPERATOR_LINE_COLOR);
        GridData verticalLineGridData = new GridData(GridData.FILL_VERTICAL);
        verticalLineGridData.widthHint = 1;
        verticalLine.setLayoutData(verticalLineGridData);

        Composite loginInfoArea = new Composite(container, SWT.NONE);
        GridData brandingAreaGridData = (GridData) brandingArea.getLayoutData();
        GridData loginInfoAreaGridData = new GridData(GridData.FILL_BOTH);
        // loginInfoAreaGridData.minimumWidth = 350;
        // loginInfoAreaGridData.minimumHeight = brandingAreaGridData.minimumHeight;
        loginInfoAreaGridData.widthHint = (int) Math.ceil(realWidthRate * logonInfoAreaWidth);
        loginInfoAreaGridData.heightHint = brandingAreaGridData.heightHint;
        loginInfoArea.setLayoutData(loginInfoAreaGridData);
        loginInfoArea.setLayout(new FormLayout());

        base = new Composite(loginInfoArea, SWT.NONE);
        FormData baseFormData = new FormData();
        baseFormData.top = new FormAttachment(0, 0);
        baseFormData.bottom = new FormAttachment(100, 0);
        baseFormData.left = new FormAttachment(0, 10);
        baseFormData.right = new FormAttachment(100, 0);
        base.setLayoutData(baseFormData);
        base.setBackground(backgroundColor);

        stackLayout = new StackLayout();
        stackLayout.marginWidth = 10;
        stackLayout.marginHeight = 10;
        base.setLayout(stackLayout);
    }

    protected void createBrandingArea(Composite container) {
        brandingArea = new Composite(container, SWT.NONE);
        brandingArea.setLayout(new FormLayout());
        IBrandingService brandingService = (IBrandingService) GlobalServiceRegister.getDefault().getService(
                IBrandingService.class);
        ImageDescriptor imageDescriptor = brandingService.getLoginVImage();
        GridData brandingAreaGridData = new GridData(GridData.FILL_BOTH);
        if (imageDescriptor != null) {
            Image imageCanvas = imageDescriptor.createImage();
            // int width = (int) Math.ceil(realWidthRate * imageCanvas.getBounds().width);
            int width = (int) Math.ceil(realHeightRate * imageCanvas.getBounds().width); // maybe use the same rate for
                                                                                         // image width and height is
                                                                                         // better
            int height = (int) Math.ceil(realHeightRate * imageCanvas.getBounds().height);
            Image scaledImage = scaleImage(imageCanvas, width, height);
            brandingArea.setBackgroundImage(scaledImage);
            brandingAreaGridData.widthHint = scaledImage.getBounds().width;
            brandingAreaGridData.heightHint = scaledImage.getBounds().height;
        } else {
            brandingAreaGridData.widthHint = (int) Math.ceil(realWidthRate * 200);
            brandingAreaGridData.heightHint = (int) Math.ceil(realHeightRate * 280);
        }
        brandingArea.setLayoutData(brandingAreaGridData);

        errorMessageArea = new Composite(brandingArea, SWT.NONE);
        FormData formData = new FormData();
        formData.bottom = new FormAttachment(100, -5);
        formData.left = new FormAttachment(0, 5);
        formData.right = new FormAttachment(100, -5);
        errorMessageArea.setLayoutData(formData);
        GridLayout layout = new GridLayout(1, false);
        layout.marginHeight = 10;
        layout.marginWidth = 10;
        errorMessageArea.setLayout(layout);
        errorTextLabel = new StyledText(errorMessageArea, SWT.WRAP);
        errorTextLabel.setEditable(false);
        errorTextLabel.setCaret(null);
        GridData layoutData = new GridData(SWT.FILL, SWT.FILL, true, true);
        errorTextLabel.setLayoutData(layoutData);
    }

    protected Image scaleImage(Image image, int width, int height) {
        Image scaled = new Image(Display.getDefault(), width, height);
        GC gc = new GC(scaled);
        gc.setAntialias(SWT.ON);
        gc.setInterpolation(SWT.HIGH);
        gc.drawImage(image, 0, 0, image.getBounds().width, image.getBounds().height, 0, 0, width, height);
        gc.dispose();
        image.dispose();
        return scaled;
    }

    public void setErrorMessage(String errMsg, List<StyleRange> styleRange) {
        StyleRange[] styleRanges = null;
        if (styleRange != null && !styleRange.isEmpty()) {
            styleRanges = styleRange.toArray(new StyleRange[0]);
        } else {
            StyleRange range = new StyleRange();
            range.font = errorFont;
            range.foreground = WHITE_COLOR;
            range.start = 0;
            range.length = errMsg.length();
            styleRanges = new StyleRange[] { range };
        }
        errorTextLabel.setText(errMsg);
        errorTextLabel.setStyleRanges(styleRanges);
        errorTextLabel.setToolTipText(errMsg);
        adjustErrorMessageAreaSize();
        errorMessageArea.setBackground(RED_COLOR);
        brandingArea.layout();
        brandingArea.update();
    }

    public void setInfoMessage(String infoMsg, List<StyleRange> styleRange) {
        StyleRange[] styleRanges = null;
        if (styleRange != null && !styleRange.isEmpty()) {
            styleRanges = styleRange.toArray(new StyleRange[0]);
        } else {
            StyleRange range = new StyleRange();
            range.font = errorFont;
            range.foreground = WHITE_COLOR;
            range.start = 0;
            range.length = infoMsg.length();
            styleRanges = new StyleRange[] { range };
        }
        errorTextLabel.setText(infoMsg);
        errorTextLabel.setStyleRanges(styleRanges);
        errorTextLabel.setToolTipText(infoMsg);
        adjustErrorMessageAreaSize();
        errorMessageArea.setBackground(YELLOW_GREEN_COLOR);
        brandingArea.layout();
        brandingArea.update();
    }

    public void setWarnMessage(String warnMsg, List<StyleRange> styleRange) {
        StyleRange[] styleRanges = null;
        if (styleRange != null && !styleRange.isEmpty()) {
            styleRanges = styleRange.toArray(new StyleRange[0]);
        } else {
            StyleRange range = new StyleRange();
            range.font = errorFont;
            range.foreground = WHITE_COLOR;
            range.start = 0;
            range.length = warnMsg.length();
            styleRanges = new StyleRange[] { range };
        }
        errorTextLabel.setText(warnMsg);
        errorTextLabel.setStyleRanges(styleRanges);
        errorTextLabel.setToolTipText(warnMsg);
        adjustErrorMessageAreaSize();
        errorMessageArea.setBackground(YELLOW_COLOR);
        brandingArea.layout();
        brandingArea.update();
    }

    public void clearErrorMessage() {
        errorTextLabel.setText(""); //$NON-NLS-1$
        errorTextLabel.setToolTipText(""); //$NON-NLS-1$
        errorMessageArea.setBackground(null);
        brandingArea.layout();
        brandingArea.update();
    }

    protected void adjustErrorMessageAreaSize() {
        errorMessageArea.pack();
    }

    @Override
    protected void okPressed() {
        // LoginDialog.getInstance().okPressed();
        LoginHelper.refreshTalendLogonStartupTimes();
        super.okPressed();
    }

    public static Point getNewButtonSize(Button btn) {
        return getNewButtonSize(btn, LoginDialogV2.BUTTON_CONTENT_HORIZONTAL_PADDING);
    }

    public static Point getNewButtonSize(Button btn, int hPadding) {
        Point btnSize = btn.computeSize(SWT.DEFAULT, SWT.DEFAULT);
        btnSize.x += hPadding * 2;
        return btnSize;
    }

    @Override
    public boolean close() {
        if (!LicenseManagement.isLicenseValidated()) {
            System.exit(0);
        }
        return super.close();
    }

    /**
     * Canvas displaying an image.<br/>
     *
     * $Id$
     *
     */
    private class ImageCanvas extends Canvas {

        private Image img;

        public ImageCanvas(Composite parent, ImageDescriptor imgDesc) {
            super(parent, SWT.NONE);

            if (imgDesc != null) {
                img = imgDesc.createImage();
                addPaintListener(new PaintListener() {

                    @Override
                    public void paintControl(PaintEvent e) {
                        e.gc.drawImage(img, 0, 0);
                    }
                });
            }
        }

        /*
         * @see org.eclipse.swt.widgets.Composite#computeSize(int, int, boolean)
         */
        @Override
        public Point computeSize(int wHint, int hHint, boolean changed) {
            Point size;
            if (img != null) {
                size = new Point(img.getBounds().width, img.getBounds().height);
            } else {
                size = super.computeSize(wHint, hHint, changed);
            }
            return size;
        }

        /*
         * (non-Javadoc)
         *
         * @see org.eclipse.swt.widgets.Widget#dispose()
         */
        @Override
        public void dispose() {
            if (img != null) {
                img.dispose();
                img = null;
            }

            super.dispose();
        }

    }

}
