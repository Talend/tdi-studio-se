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
package org.talend.designer.components.exchange.ui.views;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.EditorPart;
import org.talend.commons.ui.runtime.exception.ExceptionHandler;
import org.talend.designer.components.exchange.ui.actions.RefreshComponenentsAction;
import org.talend.designer.components.exchange.ui.htmlcontent.MyExtensionLocationListener;

/**
 * DOC hcyi class global comment. Detailled comment
 */
public class ExchangeView extends EditorPart {

    public static final String ID = "org.talend.designer.components.exchange.ui.views.ExchangeView";

    private Browser browser;

    private ExchangeManager manager;

    public ExchangeView() {
        manager = ExchangeManager.getInstance();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.ui.part.WorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
     */
    @Override
    public void createPartControl(Composite parent) {
        try {
            browser = new Browser(parent, SWT.NONE);
            manager.setBrowser(browser);
            browser.addLocationListener(new MyExtensionLocationListener());
            browser.setLayoutData(new GridData(GridData.FILL_BOTH));
            if (manager.getDocument() != null) {
                RefreshComponenentsAction action = new RefreshComponenentsAction();
                action.run(new String[] { RefreshComponenentsAction.REFRESH_ALL });
            }
        } catch (Throwable t) {
            Exception ex = new Exception("The internal web browser can not be access,the exchange view won't be displayed");
            ExceptionHandler.process(ex);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.ui.part.WorkbenchPart#setFocus()
     */
    @Override
    public void setFocus() {
    }

    @Override
    public void dispose() {
        super.dispose();
        ExchangeManager.resteInstance();
    }

    @Override
    public void doSave(IProgressMonitor monitor) {
        // TODO Auto-generated method stub

    }

    @Override
    public void doSaveAs() {
        // TODO Auto-generated method stub

    }

    @Override
    public void init(IEditorSite site, IEditorInput input) throws PartInitException {
        setSite(site);
        setInput(input);
        setPartName("Exchange");
    }

    @Override
    public boolean isDirty() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isSaveAsAllowed() {
        // TODO Auto-generated method stub
        return false;
    }

}
