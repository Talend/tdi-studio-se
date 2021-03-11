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
package org.talend.repository.plugin.integration;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PlatformUI;
import org.talend.core.CorePlugin;
import org.talend.repository.i18n.Messages;
import org.talend.repository.ui.actions.AContextualAction;

/**
 * DOC yzhang class global comment. Detailled comment
 */
public class BootTalendAction extends AContextualAction implements IWorkbenchWindowActionDelegate {

    public final static String TALEND_PERSPECTIVE_ID = "org.talend.rcp.perspective"; //$NON-NLS-1$

    public static final String LOGIN_COUNTER = "loginCounter"; //$NON-NLS-1$

    private final IPreferenceStore store;

    /**
     * yzhang BootTalendAction constructor comment.
     */
    public BootTalendAction() {

        store = CorePlugin.getDefault().getPreferenceStore();
    }

    public void dispose() {
    }

    public void init(IWorkbenchWindow window) {

    }

    public void run(IAction action) {

        IWorkbench workbench = PlatformUI.getWorkbench();
        if (store.getInt(LOGIN_COUNTER) > 1) {
            boolean openConfirm = MessageDialog.openConfirm(Display.getDefault().getActiveShell(), "Confirm",
                    "Are you want to switch to another project");
            if (!openConfirm) {
                return;
            }

            // workbench.restart();
        }

        int counter = store.getInt(LOGIN_COUNTER);
        store.setValue(LOGIN_COUNTER, ++counter);

        IWorkbenchWindow activeWorkbenchWindow = workbench.getActiveWorkbenchWindow();
        if (activeWorkbenchWindow == null) {
            return;
        }
        IWorkbenchPage activePage = activeWorkbenchWindow.getActivePage();
        if (activePage == null) {
            return;
        }
        IPerspectiveDescriptor pDescriptor = activePage.getPerspective();

        if (!pDescriptor.getId().equals(TALEND_PERSPECTIVE_ID)) {
            pDescriptor = getPerspective(TALEND_PERSPECTIVE_ID);
            activePage.setPerspective(pDescriptor);
        }

        SwitchProjectAction switchAction = new SwitchProjectAction();
        switchAction.run();
        // TDI-21143 : Studio repository view : remove all refresh call to repo view
        // IRepositoryView view = RepositoryManagerHelper.getRepositoryView();
        // if (view != null) {
        // view.refresh();
        // }

        return;
    }

    public void selectionChanged(IAction action, ISelection selection) {

    }

    /**
     * DOC bqian Comment method "getPerspective".
     *
     * @param id
     * @return
     */
    private IPerspectiveDescriptor getPerspective(String id) {
        IPerspectiveDescriptor[] pers = PlatformUI.getWorkbench().getPerspectiveRegistry().getPerspectives();
        for (IPerspectiveDescriptor perspectiveDescriptor : pers) {
            if (perspectiveDescriptor.getId().equals(id)) {
                return perspectiveDescriptor;
            }
        }
        throw new RuntimeException(Messages.getString("BootTalendAction.pluginNotLoaded")); //$NON-NLS-1$
    }

    /*
     * (non-Jsdoc)
     *
     * @see org.talend.commons.ui.swt.actions.ITreeContextualAction#init(org.eclipse.jface.viewers.TreeViewer,
     * org.eclipse.jface.viewers.IStructuredSelection)
     */
    public void init(TreeViewer viewer, IStructuredSelection selection) {
        // TODO Auto-generated method stub

    }

    /*
     * (non-Jsdoc)
     *
     * @see org.talend.repository.ui.actions.AContextualAction#doRun()
     */
    @Override
    protected void doRun() {
        // TODO Auto-generated method stub

    }

}
