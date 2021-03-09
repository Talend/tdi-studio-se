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
package org.talend.designer.scd.ui;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.talend.commons.ui.runtime.exception.ExceptionHandler;
import org.talend.core.language.ECodeLanguage;
import org.talend.core.language.LanguageManager;
import org.talend.designer.scd.ScdManager;

/**
 * DOC hcw class global comment. Detailled comment
 */
public class ScdUI {

    private ScdManager scdManager;

    /**
     * DOC hcw Comment method "createUI".
     *
     * @param display
     * @param scdManager
     */
    public void createUI(Composite parent, ScdManager scdManager) {
        // load values from element parameters
        scdManager.restoreUIData();
        Shell shell = parent.getShell();

        // Shell shell = new Shell(display, SWT.APPLICATION_MODAL | SWT.BORDER | SWT.RESIZE | SWT.CLOSE | SWT.MIN |
        // SWT.MAX | SWT.TITLE);
        this.scdManager = scdManager;
        createControls(shell);
    }

    /**
     * DOC hcw Comment method "createControls".
     *
     * @param shell
     */
    private void createControls(Shell shell) {
        AbstractScdDialog dialog = null;
        if (LanguageManager.getCurrentLanguage() == ECodeLanguage.JAVA) {
            dialog = new JavaScdDialog(shell, scdManager);
        } else {
            dialog = new PerlScdDialog(shell, scdManager);
        }

        int dialogResponse = dialog.open();
        if (dialogResponse == IDialogConstants.OK_ID) {
            scdManager.setDialogResponse(SWT.OK);
            try {
                scdManager.createOutputSchema();
            } catch (Exception e) {
                ExceptionHandler.process(e);
            }
            scdManager.updateElementParameters();
        } else {
            scdManager.setDialogResponse(SWT.CANCEL);
        }
    }
}
