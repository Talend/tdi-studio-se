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
package org.talend.designer.tutorials.dialog;

import java.io.IOException;

import org.eclipse.jface.dialogs.TrayDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.talend.commons.exception.CommonExceptionHandler;
import org.talend.commons.ui.gmf.util.DisplayUtils;
import org.talend.commons.ui.html.BrowserDynamicPartLocationListener;
import org.talend.designer.tutorials.i18n.Messages;
import org.talend.designer.tutorials.ui.htmlcontent.TutorialsHelper;

/**
 *
 * created by hcyi on Mar 20, 2017 Detailled comment
 *
 */
public class TutorialsDialog extends TrayDialog {

    protected static TutorialsDialog currentTutorialsDialog;

    public static final String FONT_ARIAL = "Arial"; //$NON-NLS-1$

    public static final Color YELLOW_GREEN_COLOR = new Color(null, 159, 181, 38);

    private Font font = new Font(null, FONT_ARIAL, 9, SWT.NORMAL);

    private Font fontMac = new Font(null, FONT_ARIAL, 12, SWT.NORMAL);

    private final String osName = System.getProperties().getProperty("os.name"); //$NON-NLS-1$

    protected Browser browser;

    public TutorialsDialog() {
        super(DisplayUtils.getDefaultShell());
        this.setShellStyle(getShellStyle() | SWT.RESIZE | SWT.MIN | SWT.APPLICATION_MODAL | SWT.MAX);
        currentTutorialsDialog = this;
        if (osName.contains("Mac")) { //$NON-NLS-1$
            font = fontMac;
        }
    }

    @Override
    protected void initializeBounds() {
        super.initializeBounds();
        Point location = getInitialLocation(getShell().getSize());
        getShell().setLocation(location.x, location.y);
    }

    @Override
    protected void configureShell(final Shell newShell) {
        super.configureShell(newShell);
        newShell.setSize(730, 930);
        newShell.setText(Messages.getString("TutorialsDialog.title")); //$NON-NLS-1$
    }

    @Override
    protected Control createContents(Composite parent) {
        SashForm sash = new SashForm(parent, SWT.NONE);
        GridData layoutData = new GridData(GridData.FILL_BOTH);
        layoutData.widthHint = 800;
        layoutData.heightHint = 700;
        sash.setLayoutData(layoutData);
        sash.setLayout(new GridLayout());
        sash.setBackground(new Color(null, 255, 255, 255));
        // create the dialog area and button bar
        dialogArea = createDialogArea(sash);
        return sash;
    }

    @Override
    protected Control createDialogArea(final Composite parent) {
        browser = new Browser(parent, SWT.NONE);
        try {
            browser.setText(TutorialsHelper.getHelper().getHtmlContent());
        } catch (IOException e) {
            CommonExceptionHandler.process(e);
        }
        browser.addLocationListener(new BrowserDynamicPartLocationListener());
        return parent;
    }



    @Override
    public void okPressed() {
        super.okPressed();
        font.dispose();
    }


    public Browser getBrowser() {
        return this.browser;
    }

    public static TutorialsDialog getCurrentTalendForgeDialog() {
        return currentTutorialsDialog;
    }
}
