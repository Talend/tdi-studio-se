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

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.talend.commons.exception.LoginException;
import org.talend.commons.exception.PersistenceException;
import org.talend.core.model.update.RepositoryUpdateManager;
import org.talend.core.runtime.CoreRuntimePlugin;
import org.talend.repository.RepositoryWorkUnit;
import org.talend.repository.model.IProxyRepositoryFactory;

/**
 * ggu class global comment. Detailled comment
 */
public class UpdateModificationsAction extends Action implements IWorkbenchWindowActionDelegate {

    private IWorkbenchWindow window;

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.ui.IWorkbenchWindowActionDelegate#dispose()
     */
    @Override
    public void dispose() {

    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.ui.IWorkbenchWindowActionDelegate#init(org.eclipse.ui.IWorkbenchWindow)
     */
    @Override
    public void init(IWorkbenchWindow window) {
        this.window = window;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
     */
    @Override
    public void run(IAction action) {
        // TDI-26387
        final IProxyRepositoryFactory proxyRepositoryFactory = CoreRuntimePlugin.getInstance().getProxyRepositoryFactory();
        proxyRepositoryFactory.executeRepositoryWorkUnit(new RepositoryWorkUnit("") { //$NON-NLS-1$

                    @Override
                    protected void run() throws LoginException, PersistenceException {
                        // nothing to do, only force doing svn update first.
                    }
                });
        RepositoryUpdateManager.updateAllJob(true);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction,
     * org.eclipse.jface.viewers.ISelection)
     */
    @Override
    public void selectionChanged(IAction action, ISelection selection) {

    }

}
