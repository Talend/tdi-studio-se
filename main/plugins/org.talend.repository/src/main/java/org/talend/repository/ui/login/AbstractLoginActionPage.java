// ============================================================================
//
// Copyright (C) 2006-2019 Talend Inc. - www.talend.com
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

import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.talend.commons.exception.CommonExceptionHandler;
import org.talend.repository.i18n.Messages;

/**
 * created by cmeng on May 12, 2015 Detailled comment
 *
 */
public abstract class AbstractLoginActionPage extends AbstractActionPage {

    protected int TAB_VERTICAL_PADDING_LEVEL_1 = LoginDialogV2.TAB_VERTICAL_PADDING_LEVEL_1;

    protected int TAB_VERTICAL_PADDING_LEVEL_2 = LoginDialogV2.TAB_VERTICAL_PADDING_LEVEL_2;

    protected int TAB_HORIZONTAL_PADDING_LEVEL_1 = LoginDialogV2.TAB_HORIZONTAL_PADDING_LEVEL_1 + 5;

    protected int TAB_HORIZONTAL_PADDING_LEVEL_2 = LoginDialogV2.TAB_HORIZONTAL_PADDING_LEVEL_2;

    protected int TAB_HORIZONTAL_PADDING_LEVEL_3 = LoginDialogV2.TAB_HORIZONTAL_PADDING_LEVEL_1;

    protected LoginDialogV2 loginDialog;

    protected StackLayout stackLayout;

    /**
     * Background color for radio button
     */
    protected Color backgroundRadioColor;

    protected Color backgroundBtnColor;

    protected ErrorManager errorManager;

    protected Composite navigateArea;

    protected Label navigatorSeperatorLine;

    protected Button alwaysAsk;

    protected Button previousButton;

    protected Button finishButton;

    public AbstractLoginActionPage(Composite parent, LoginDialogV2 dialog, int style) {
        super(parent, style);
        stackLayout = (StackLayout) parent.getLayout();
        this.loginDialog = dialog;
        this.setFont(JFaceResources.getFont(LoginDialogV2.FONT_TALEND_FOR_LOGIN_UI));
        this.backgroundRadioColor = JFaceResources.getColorRegistry().get(LoginDialogV2.COLOR_LOGON_DIALOG_BACKGROUND);
        // this.backgroundBtnColor = JFaceResources.getColorRegistry().get(LoginDialogV2.COLOR_LOGON_DIALOG_BACKGROUND);
    }

    @Override
    public void init() throws Throwable {
        super.init();
        errorManager = createErrorManager();
    }

    @Override
    protected void createControl() {
        super.createControl();
        GridData data = new GridData(GridData.FILL_BOTH);
        this.setLayoutData(data);
    }

    @Override
    public void createControl(Composite parentCtrl) {
        Composite container = new Composite(parentCtrl, SWT.NONE);
        container.setLayout(new FormLayout());
        instantiateControl(container);
        layoutControl();
    }

    /**
     * This method can keep the tab orders for all the controls, and normally should call this method at the end when
     * override it
     *
     * @param container
     */
    protected void instantiateControl(Composite container) {
        instantiateNavigatorArea(container);
    }

    /**
     * Normally should call it first when override it
     */
    protected void layoutControl() {
        layoutNavigatorArea();
    }

    @Override
    public void showPage() {
        stackLayout.topControl = this;
        this.setEnabled(true);
        getParent().layout();
        errorManager.showErrorMessage();
    }

    protected void instantiateNavigatorArea(Composite container) {
        // Navigate Area
        navigateArea = new Composite(container, SWT.NONE);
        navigatorSeperatorLine = new Label(navigateArea, SWT.SEPARATOR | SWT.HORIZONTAL);
        alwaysAsk = new Button(navigateArea, SWT.CHECK);
        alwaysAsk.setFont(LoginDialogV2.fixedFont);
        alwaysAsk.setBackground(backgroundRadioColor);
        alwaysAsk.setText(Messages.getString("LoginProjectPage.alwaysAskMe")); //$NON-NLS-1$
        alwaysAsk.setToolTipText(Messages.getString("LoginProjectPage.alwaysAskMe.toolTip")); //$NON-NLS-1$
        previousButton = new Button(navigateArea, SWT.NONE);
        previousButton.setFont(LoginDialogV2.fixedFont);
        previousButton.setBackground(backgroundBtnColor);
        previousButton.setText(Messages.getString("LoginProjectPage.previous")); //$NON-NLS-1$
        finishButton = new Button(navigateArea, SWT.NONE);
        finishButton.setFont(LoginDialogV2.fixedFont);
        finishButton.setBackground(backgroundBtnColor);
        finishButton.setText(Messages.getString("LoginProjectPage.finish")); //$NON-NLS-1$
    }

    protected void layoutNavigatorArea() {
        FormData formData = null;
        formData = new FormData();
        formData.bottom = new FormAttachment(100, 0);
        formData.left = new FormAttachment(0, 0);
        formData.right = new FormAttachment(100, 0);
        navigateArea.setLayoutData(formData);
        navigateArea.setLayout(new FormLayout());

        formData = new FormData();
        formData.right = new FormAttachment(100, 0);
        formData.width = LoginDialogV2.getNewButtonSize(finishButton).x;
        formData.bottom = new FormAttachment(100, 0);
        finishButton.setLayoutData(formData);

        if (previousButton != null) {
            formData = new FormData();
            formData.top = new FormAttachment(finishButton, 0, SWT.CENTER);
            formData.bottom = new FormAttachment(finishButton, 0, SWT.CENTER);
            formData.right = new FormAttachment(finishButton, -1 * TAB_HORIZONTAL_PADDING_LEVEL_2, SWT.LEFT);
            formData.width = LoginDialogV2.getNewButtonSize(previousButton).x;
            previousButton.setLayoutData(formData);
            if (previousPage == null) {
                previousButton.setVisible(false);
            }
        }

        formData = new FormData();
        formData.left = new FormAttachment(0, 0);
        formData.top = new FormAttachment(finishButton, 0, SWT.CENTER);
        formData.bottom = new FormAttachment(finishButton, 0, SWT.CENTER);
        alwaysAsk.setLayoutData(formData);

        formData = new FormData();
        formData.left = new FormAttachment(0, 0);
        formData.right = new FormAttachment(100, 0);
        formData.bottom = new FormAttachment(finishButton, -1 * TAB_VERTICAL_PADDING_LEVEL_1, SWT.TOP);
        navigatorSeperatorLine.setLayoutData(formData);
    }

    @Override
    protected void addListeners() {
        if (previousButton != null) {
            previousButton.addSelectionListener(new SelectionListener() {

                @Override
                public void widgetSelected(SelectionEvent e) {
                    try {
                        gotoPreviousPage();
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
    }

    @Override
    public void gotoNextPage() throws Throwable {
        this.setEnabled(false);
        errorManager.hideAllMessages();
        BusyIndicator.showWhile(getDisplay(), new Runnable() {

            @Override
            public void run() {
                gotoNextPageWithCatch();
            }
        });
        if (this.getException() != null) {
            throw this.getException();
        }
    }

    private void gotoNextPageWithCatch() {
        try {
            super.gotoNextPage();
        } catch (Throwable e) {
            this.setException(e);
        }
    }

    @Override
    public void gotoPreviousPage() throws Throwable {
        this.setEnabled(false);
        errorManager.hideAllMessages();
        BusyIndicator.showWhile(getDisplay(), new Runnable() {

            @Override
            public void run() {
                gotoPreviousPageWithCatch();
            }
        });
        if (this.getException() != null) {
            throw this.getException();
        }
    }

    private void gotoPreviousPageWithCatch() {
        try {
            super.gotoPreviousPage();
        } catch (Throwable e) {
            this.setException(e);
        }
    }

    protected ErrorManager createErrorManager() {
        return new ErrorManager();
    }

    protected class ErrorManager {

        protected String errMessage;

        protected String warnMessage;

        protected String infoMessage;

        protected List<StyleRange> infoStyleRange;

        protected List<StyleRange> warnStyleRange;

        protected List<StyleRange> errStyleRange;

        protected Throwable authException;

        protected boolean hasAuthException = false;

        public void clearAllMessages() {
            errMessage = null;
            warnMessage = null;
            infoMessage = null;
            errStyleRange = null;
            warnStyleRange = null;
            infoStyleRange = null;
            Display.getDefault().syncExec(() -> loginDialog.clearErrorMessage());
            authException = null;
            hasAuthException = false;
        }

        public void hideAllMessages() {
            loginDialog.clearErrorMessage();
        }

        public String getErrMessage() {
            return this.errMessage;
        }

        public void setErrMessage(String errMessage, List<StyleRange> errStyleRange) {
            this.errMessage = errMessage;
            this.errStyleRange = errStyleRange;
            showErrorMessage();
        }

        public void setErrMessage(String errMessage) {
            setErrMessage(errMessage, null);
        }

        public String getWarnMessage() {
            return this.warnMessage;
        }

        public void setWarnMessage(String warnMessage, List<StyleRange> warnStyleRange) {
            this.warnMessage = warnMessage;
            this.warnStyleRange = warnStyleRange;
            showErrorMessage();
        }

        public void setWarnMessage(String warnMessage) {
            setWarnMessage(warnMessage, null);
        }

        public String getInfoMessage() {
            return this.infoMessage;
        }

        public void setInfoMessage(String infoMessage, List<StyleRange> infoStyleRange) {
            this.infoMessage = infoMessage;
            this.infoStyleRange = infoStyleRange;
            showErrorMessage();
        }

        public void setInfoMessage(String infoMessage) {
            setInfoMessage(infoMessage, null);
        }

        public boolean showErrorMessage() {
            if (this.errMessage != null && !this.errMessage.isEmpty()) {
                loginDialog.setErrorMessage(errMessage, infoStyleRange);
                return true;
            } else if (this.warnMessage != null && !this.warnMessage.isEmpty()) {
                loginDialog.setWarnMessage(warnMessage, warnStyleRange);
                return true;
            } else if (this.infoMessage != null && !this.infoMessage.isEmpty()) {
                loginDialog.setInfoMessage(infoMessage, infoStyleRange);
                return true;
            }
            return false;
        }

        public boolean hasError() {
            boolean hasError = false;
            if (this.errMessage != null && !this.errMessage.isEmpty()) {
                hasError = true;
            }
            return hasError;
        }

        public boolean hasWarn() {
            boolean hasWarn = false;
            if (this.warnMessage != null && !this.warnMessage.isEmpty()) {
                hasWarn = true;
            }
            return hasWarn;
        }

        public Throwable getAuthException() {
            return authException;
        }

        public void setAuthException(Throwable authException) {
            this.authException = authException;
        }

        public boolean isHasAuthException() {
            return hasAuthException;
        }

        public void setHasAuthException(boolean hasAuthException) {
            this.hasAuthException = hasAuthException;
        }

    }
}
