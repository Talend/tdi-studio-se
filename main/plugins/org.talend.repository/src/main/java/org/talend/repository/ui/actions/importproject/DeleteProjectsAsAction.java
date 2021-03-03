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
package org.talend.repository.ui.actions.importproject;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.talend.commons.exception.CommonExceptionHandler;
import org.talend.commons.ui.runtime.image.ImageProvider;
import org.talend.core.CorePlugin;
import org.talend.repository.ui.ERepositoryImages;

/**
 * DOC qwei class global comment. Detailled comment
 */
public class DeleteProjectsAsAction extends Action implements IWorkbenchWindowActionDelegate {

    private boolean login;

    public DeleteProjectsAsAction() {
        super();
        this.setImageDescriptor(ImageProvider.getImageDesc(ERepositoryImages.DELETE_PROJECT_ACTION));
        this.login = false;
    }

    public DeleteProjectsAsAction(boolean login) {
        this();
        this.login = login;

    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.ui.IWorkbenchWindowActionDelegate#dispose()
     */
    @Override
    public void dispose() {
        // TODO Auto-generated method stub

    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.ui.IWorkbenchWindowActionDelegate#init(org.eclipse.ui.IWorkbenchWindow)
     */
    @Override
    public void init(IWorkbenchWindow window) {
        // TODO Auto-generated method stub

    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
     */
    @Override
    public void run(IAction action) {
        run();
    }

    @Override
    public void run() {
        try {
            Shell activeShell = Display.getCurrent().getActiveShell();
            SelectDeleteProjectDialog dialog = new SelectDeleteProjectDialog(activeShell, this.login);
            if (dialog.open() == Dialog.OK) {
                CorePlugin.getDefault().getRepositoryLocalProviderService().resetXmiResourceSet();
            }
        } catch (Throwable e) {
            CommonExceptionHandler.process(e);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction,
     * org.eclipse.jface.viewers.ISelection)
     */
    @Override
    public void selectionChanged(IAction action, ISelection selection) {
        // TODO Auto-generated method stub

    }
}
