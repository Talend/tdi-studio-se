// ============================================================================
//
// Copyright (C) 2006-2017 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.componentdesigner.rcp;

import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialogWithToggle;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.application.ActionBarAdvisor;
import org.eclipse.ui.application.IActionBarConfigurer;
import org.eclipse.ui.application.IWorkbenchWindowConfigurer;
import org.eclipse.ui.application.WorkbenchWindowAdvisor;
import org.eclipse.ui.internal.ide.IDEWorkbenchPlugin;
import org.talend.componentdesigner.ComponentDesigenerPlugin;
import org.talend.componentdesigner.rcp.i18n.Messages;
import org.talend.componentdesigner.ui.ProjectSelectionDialog;

/**
 * DOC rli class global comment. Detailled comment <br/>
 * 
 * $Id: talend.epf 1 2006-09-29 17:06:40 +0000 (ææäº, 29 ä¹æ 2006) nrousseau $
 * 
 */
@SuppressWarnings("restriction")
public class ApplicationWorkbenchWindowAdvisor extends WorkbenchWindowAdvisor {

    /**
     * constant from org.eclipse.ui.internal.ide.IDEInternalPreferences;
     */
    public class IDEInternalPreferences {

        // (boolean) Prompt for exit confirmation when last window closed.
        public static final String EXIT_PROMPT_ON_CLOSE_LAST_WINDOW = "EXIT_PROMPT_ON_CLOSE_LAST_WINDOW"; //$NON-NLS-1$
    }

    public ApplicationWorkbenchWindowAdvisor(IWorkbenchWindowConfigurer configurer) {
        super(configurer);
    }

    public ActionBarAdvisor createActionBarAdvisor(IActionBarConfigurer configurer) {
        return new ApplicationActionBarAdvisor(configurer);
    }

    public void preWindowOpen() {
        IWorkbenchWindowConfigurer configurer = getWindowConfigurer();
        configurer.setInitialSize(new Point(1000, 750));
        configurer.setShowCoolBar(false);
        configurer.setShowStatusLine(false);
        configurer.setTitle(Messages.getString("ApplicationWorkbenchWindowAdvisor.rcpApp")); //$NON-NLS-1$
        chooseProjectLocation();
    }

    private void chooseProjectLocation() {
        boolean isOK = true;
        if (!ComponentDesigenerPlugin.getDefault().isUsed()) {
            isOK = popSelectionDiaLog(PlatformUI.getWorkbench().getDisplay().getActiveShell());
        }
        if (!isOK) {
            Platform.endSplash();
            System.exit(0);
        }
    }

    private boolean popSelectionDiaLog(Shell activeShell) {
        ProjectSelectionDialog dialog = new ProjectSelectionDialog(activeShell);
        return IDialogConstants.OK_ID == dialog.open();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.application.WorkbenchWindowAdvisor#postWindowOpen()
     */
    @Override
    public void postWindowOpen() {
        ComponentDesigenerPlugin.getDefault().checkProject();
    }

    public boolean preWindowShellClose() {
        if (PlatformUI.getWorkbench().getWorkbenchWindowCount() > 1) {
            return true;
        }
        // the user has asked to close the last window, while will cause the
        // workbench to close in due course - prompt the user for confirmation
        IPreferenceStore store = IDEWorkbenchPlugin.getDefault().getPreferenceStore();
        boolean promptOnExit = store.getBoolean(IDEInternalPreferences.EXIT_PROMPT_ON_CLOSE_LAST_WINDOW);

        if (promptOnExit) {
            String message;

            // String productName = null;
            //
            // if (productName == null) {
            // // message = IDEWorkbenchMessages.PromptOnExitDialog_message0;
            // message = Messages.getString("ApplicationWorkbenchWindowAdvisor.PromptOnExitDialog_message0");
            // }
            // else {
            // // message = NLS.bind(IDEWorkbenchMessages.PromptOnExitDialog_message1, productName);
            // message = Messages.getString("ApplicationWorkbenchWindowAdvisor.PromptOnExitDialog_message1",
            // new Object[] { productName });
            // }
            message = Messages.getString("ApplicationWorkbenchWindowAdvisor.PromptOnExitDialog_message0"); //$NON-NLS-1$

            MessageDialogWithToggle dlg = MessageDialogWithToggle.openOkCancelConfirm(getWindowConfigurer().getWindow()
                    .getShell(), Messages.getString("ApplicationWorkbenchWindowAdvisor.PromptOnExitDialog_shellTitle"), message, //$NON-NLS-1$
                    Messages.getString("ApplicationWorkbenchWindowAdvisor.PromptOnExitDialog_choice"), false, null, null); //$NON-NLS-1$
            if (dlg.getReturnCode() != IDialogConstants.OK_ID) {
                return false;
            }
            if (dlg.getToggleState()) {
                store.setValue(IDEInternalPreferences.EXIT_PROMPT_ON_CLOSE_LAST_WINDOW, false);
                IDEWorkbenchPlugin.getDefault().savePluginPreferences();
            }
        }

        return true;
    }

}
