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
package org.talend.repository.ui.actions.toolbar;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.talend.repository.i18n.Messages;
import org.talend.repository.ui.actions.AContextualAction;
import org.talend.repository.ui.dialog.ProjectSettingDialog;

/**
 * DOC qwei class global comment. Detailled comment
 */
public class ProjectSettingsAction extends AContextualAction implements IWorkbenchWindowActionDelegate {

    public ProjectSettingsAction() {
        super();
        setText(Messages.getString("ProjectSettingsAction_editProject")); //$NON-NLS-1$
        setToolTipText(""); //$NON-NLS-1$
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.commons.ui.swt.actions.ITreeContextualAction#init(org.eclipse.jface.viewers.TreeViewer,
     * org.eclipse.jface.viewers.IStructuredSelection)
     */
    public void init(TreeViewer viewer, IStructuredSelection selection) {
        // TODO Auto-generated method stub

    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.ui.IWorkbenchWindowActionDelegate#dispose()
     */
    public void dispose() {
        // TODO Auto-generated method stub

    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.ui.IWorkbenchWindowActionDelegate#init(org.eclipse.ui.IWorkbenchWindow)
     */
    public void init(IWorkbenchWindow window) {
        // TODO Auto-generated method stub

    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
     */
    public void run(IAction action) {
        // TODO Auto-generated method stub
        run();

    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction,
     * org.eclipse.jface.viewers.ISelection)
     */
    public void selectionChanged(IAction action, ISelection selection) {

    }

    protected void doRun() {
        // User user = ((RepositoryContext)
        // CorePlugin.getContext().getProperty(Context.REPOSITORY_CONTEXT_KEY)).getUser();
        // UserRole role = user.getRole();
        //
        // // TODO decide if the current user can execute this action, you can also use the method selectionChanged() to
        // // set the IAction enable or not
        //
        // // if(isTIS && !isAdmin){
        // // tell the user he has no right to execute this;
        // // return;
        // // }
        //
        // ProjectSettingsWizard wizard = new ProjectSettingsWizard();
        // WizardDialog dialog = new WizardDialog(Display.getCurrent().getActiveShell(), wizard);
        // dialog.open();

        // modify by achen fix bug 0005990
        run2();
    }

    /**
     *
     * DOC aimingchen Comment method "run2".
     */
    public void run2() {
        ProjectSettingDialog dialog = new ProjectSettingDialog();
        dialog.open();
    }
}
