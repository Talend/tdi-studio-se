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

import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;

/**
 * created by cmeng on May 12, 2015 Detailled comment
 *
 */
public abstract class AbstractActionPage extends Composite {

    protected AbstractActionPage previousPage;

    protected AbstractActionPage nextPage;

    protected Throwable exception;

    protected boolean isNeedReinit = true;

    protected boolean isNeedRecreateControl = true;

    protected boolean isNeedRefreshUIData = true;

    protected boolean isNeedCheck = true;

    public AbstractActionPage(Composite parent, int style) {
        super(parent, style);
    }

    public void init() throws Throwable {
        isNeedReinit = false;
    }

    public abstract void preCreateControl();

    protected void createControl() {
        isNeedRecreateControl = false;
        this.setLayout(new FillLayout());
        createControl(this);
    }

    public abstract void createControl(Composite parentCtrl);

    public abstract void afterCreateControl();

    public abstract void refreshUIData();

    public abstract void check();

    protected abstract void addListeners();

    public abstract void showPage();

    public void gotoNextPage() throws Throwable {
        try {
            AbstractActionPage iNextPage = getNextPage();
            if (iNextPage.getPreviousPage() != this) {
                iNextPage.setPreviousPage(this);
            }
            iNextPage.preShowPage();
            iNextPage.showPage();
        } catch (Throwable e) {
            resetAll();
            throw e;
        }
    }

    public void preShowPage() throws Throwable {
        if (isNeedReinit()) {
            init();
        }
        boolean recreated = false;
        if (isNeedRecreateControl()) {
            recreated = true;
            preCreateControl();
            createControl();
            afterCreateControl();
        }
        if (isNeedRefreshUIData()) {
            refreshUIData();
        }
        if (isNeedCheck()) {
            check();
        }
        if (recreated) {
            addListeners();
        }
    }

    public void gotoPreviousPage() throws Throwable {
        try {
            AbstractActionPage iPreviousPage = getPreviousPage();
            iPreviousPage.preShowPage();
            iPreviousPage.showPage();
        } catch (Throwable e) {
            resetAll();
            throw e;
        }
    }

    public void resetAll() {
        isNeedReinit = true;
        isNeedRecreateControl = true;
        isNeedRefreshUIData = true;
        isNeedCheck = true;
    }

    public abstract Object getCheckedErrors();

    public void setException(Throwable exception) {
        this.exception = exception;
    }

    public Throwable getException() {
        return this.exception;
    }

    public boolean isNeedCheck() {
        return this.isNeedCheck;
    }

    public void setNeedCheck(boolean isNeedCheck) {
        this.isNeedCheck = isNeedCheck;
    }

    public boolean isNeedReinit() {
        return this.isNeedReinit;
    }

    public void setNeedReinit(boolean isNeedReinit) {
        this.isNeedReinit = isNeedReinit;
    }

    public boolean isNeedRecreateControl() {
        return this.isNeedRecreateControl;
    }

    public boolean isNeedRefreshUIData() {
        return this.isNeedRefreshUIData;
    }

    public void setNeedRefreshUIData(boolean isNeedRefreshUIData) {
        this.isNeedRefreshUIData = isNeedRefreshUIData;
    }

    public void setNeedRecreateControl(boolean isNeedRecreateControl) {
        this.isNeedRecreateControl = isNeedRecreateControl;
    }

    public void setPreviousPage(AbstractActionPage prePage) {
        this.previousPage = prePage;
    }

    public AbstractActionPage getPreviousPage() {
        return previousPage;
    }

    protected void setNextPage(AbstractActionPage nextpage) {
        this.nextPage = nextpage;
    }

    public AbstractActionPage getNextPage() {
        return nextPage;
    }
}
