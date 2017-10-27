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
package org.talend.componentdesigner.ui.action.provider;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.navigator.CommonActionProvider;
import org.eclipse.ui.navigator.ICommonActionExtensionSite;
import org.eclipse.ui.navigator.ICommonViewerWorkbenchSite;
import org.talend.componentdesigner.i18n.internal.Messages;
import org.talend.componentdesigner.util.XSDValidator;

/**
 * DOC gke class global comment. Detailled comment
 */
public class ValidateComponentXMLActionProvider extends CommonActionProvider {

    private IAction validateAction;

    private IFile selectedFile;

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.actions.ActionGroup#fillContextMenu(org.eclipse.jface.action.IMenuManager)
     */
    public void fillContextMenu(IMenuManager menu) {
        Object obj = ((TreeSelection) this.getContext().getSelection()).getFirstElement();
        if (obj instanceof IFile) {
            selectedFile = (IFile) obj;
        }
        if (((String) selectedFile.getFileExtension()).equalsIgnoreCase("xml")) { //$NON-NLS-1$
            menu.insertBefore("common.new.menu", validateAction);//$NON-NLS-1$
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.navigator.CommonActionProvider#init(org.eclipse.ui.navigator.ICommonActionExtensionSite)
     */
    public void init(ICommonActionExtensionSite site) {
        if (site.getViewSite() instanceof ICommonViewerWorkbenchSite) {
            validateAction = new ValidateComponentXMLAction();
        }
    }

    /**
     * DOC gke ValidateComponentXMLActionProvider class global comment. Detailled comment
     */
    class ValidateComponentXMLAction extends Action {

        public ValidateComponentXMLAction() {
            super(Messages.getString("ValidateComponentXMLActionProvider.validate")); //$NON-NLS-1$
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.eclipse.jface.action.Action#run()
         */
        public void run() {
            //
            try {
                String message = new XSDValidator().checkXSD(selectedFile.getRawLocation().toString());

                if (message.length() > 0) {
                    message = Messages.getString("ValidateComponentXMLActionProvider.invalid") + message; //$NON-NLS-1$
                } else {
                    message = Messages.getString("ValidateComponentXMLActionProvider.valid"); //$NON-NLS-1$
                }

                // show the dialog for the validate result.
                MessageDialog messageDialog = new MessageDialog(
                        PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
                        Messages.getString("ValidateComponentXMLActionProvider.result"), null, message, MessageDialog.INFORMATION, new String[] { Messages.getString("ValidateComponentXMLActionProvider.ok") }, 0); //$NON-NLS-1$ //$NON-NLS-2$
                messageDialog.open();

            } catch (Exception e) {
                org.talend.componentdesigner.exception.ExceptionHandler.process(e);
            }

        }
    }
}
