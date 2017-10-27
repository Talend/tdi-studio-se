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

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.GroupMarker;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.window.Window;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.actions.ActionFactory.IWorkbenchAction;
import org.eclipse.ui.application.ActionBarAdvisor;
import org.eclipse.ui.application.IActionBarConfigurer;
import org.eclipse.ui.internal.dialogs.ShowViewDialog;
import org.eclipse.ui.statushandlers.StatusManager;
import org.eclipse.ui.views.IViewDescriptor;
import org.talend.componentdesigner.rcp.i18n.Messages;

/**
 * DOC rli class global comment. Detailled comment <br/>
 * 
 * $Id: talend.epf 1 2006-09-29 17:06:40 +0000 (ææäº, 29 ä¹æ 2006) nrousseau $
 * 
 */
public class ApplicationActionBarAdvisor extends ActionBarAdvisor {

    private IWorkbenchAction exitAction;

    private IWorkbenchAction preferenceAction;

    private IWorkbenchAction aboutAction;

    private IActionBarConfigurer actionBarConfigurer;

    private IWorkbenchWindow window;

    public ApplicationActionBarAdvisor(IActionBarConfigurer configurer) {
        super(configurer);
        actionBarConfigurer = configurer;
    }

    protected void makeActions(IWorkbenchWindow window) {

        this.window = window;

        IAction refreshAction = ActionFactory.REFRESH.create(window);
        register(refreshAction);

        IAction renameAction = ActionFactory.RENAME.create(window);
        register(renameAction);

        exitAction = ActionFactory.QUIT.create(window);
        register(exitAction);

        preferenceAction = ActionFactory.PREFERENCES.create(window);
        register(preferenceAction);

        aboutAction = ActionFactory.ABOUT.create(window);
        register(aboutAction);

        registerGlobalActions();
    }

    private void registerGlobalActions() {
        actionBarConfigurer.registerGlobalAction(ActionFactory.SAVE.create(window));
        actionBarConfigurer.registerGlobalAction(ActionFactory.UNDO.create(window));
        actionBarConfigurer.registerGlobalAction(ActionFactory.REDO.create(window));
        actionBarConfigurer.registerGlobalAction(ActionFactory.CUT.create(window));
        actionBarConfigurer.registerGlobalAction(ActionFactory.COPY.create(window));
        actionBarConfigurer.registerGlobalAction(ActionFactory.PASTE.create(window));
        actionBarConfigurer.registerGlobalAction(ActionFactory.DELETE.create(window));
        actionBarConfigurer.registerGlobalAction(ActionFactory.SELECT_ALL.create(window));
    }

    protected void fillMenuBar(IMenuManager menuBar) {
        MenuManager fileMenu = new MenuManager(
                Messages.getString("ApplicationActionBarAdvisor.fileMenu"), IWorkbenchActionConstants.M_FILE); //$NON-NLS-1$
        MenuManager windowMenu = new MenuManager(
                Messages.getString("ApplicationActionBarAdvisor.windowMenu"), IWorkbenchActionConstants.M_WINDOW); //$NON-NLS-1$
        MenuManager helpMenu = new MenuManager(
                Messages.getString("ApplicationActionBarAdvisor.helpMenu"), IWorkbenchActionConstants.M_HELP); //$NON-NLS-1$

        menuBar.add(fileMenu);
        menuBar.add(windowMenu);
        // Add a group marker indicating where action set menus will appear.
        menuBar.add(new GroupMarker(IWorkbenchActionConstants.MB_ADDITIONS));
        menuBar.add(helpMenu);

        // File
        fileMenu.add(new Separator());
        fileMenu.add(ActionFactory.SAVE.create(window));
        fileMenu.add(exitAction);

        // Window
        windowMenu.add(preferenceAction);
        windowMenu.add(new ShowViewAction());
        // Help
        helpMenu.add(aboutAction);
    }

    /**
     * Displays a window for view selection. <br/>
     * 
     * $Id: ShowViewAction.java,v 1.1 2007/03/07 05:08:59 pub Exp $
     * 
     */
    public class ShowViewAction extends Action {

        /**
         * Constructs a new ShowViewAction.
         */
        public ShowViewAction() {
            super(Messages.getString("ShowViewAction.actionLabel")); //$NON-NLS-1$
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.eclipse.jface.action.Action#run()
         */
        @Override
        public void run() {
            IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
            final IWorkbenchPage page = window.getActivePage();
            if (page == null) {
                return;
            }

            final ShowViewDialog dialog = new ShowViewDialog(window, PlatformUI.getWorkbench().getViewRegistry());
            dialog.open();

            if (dialog.getReturnCode() == Window.CANCEL) {
                return;
            }

            final IViewDescriptor[] descriptors = dialog.getSelection();
            for (int i = 0; i < descriptors.length; ++i) {
                try {
                    page.showView(descriptors[i].getId());
                } catch (PartInitException e) {
                    // StatusUtil.handleStatus(e.getStatus(),
                    //                            Messages.getString("ApplicationActionBarAdvisor.ShowView_errorTitle") + ": " + e.getMessage(), //$NON-NLS-1$
                    // StatusManager.SHOW);
                    IStatus istatus = e.getStatus();
                    StatusManager
                            .getManager()
                            .handle(new Status(
                                    istatus.getSeverity(),
                                    istatus.getPlugin(),
                                    istatus.getCode(),
                                    Messages.getString("ApplicationActionBarAdvisor.ShowView_errorTitle") + ": " + e.getMessage(), //$NON-NLS-1$ //$NON-NLS-2$
                                    istatus.getException()), StatusManager.SHOW);
                }
            }
        }
    }

}
