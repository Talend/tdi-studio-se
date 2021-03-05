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
package org.talend.designer.runprocess.ui.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PlatformUI;
import org.talend.commons.ui.runtime.image.ImageProvider;
import org.talend.designer.core.ui.views.properties.ComponentSettingsView;
import org.talend.designer.runprocess.i18n.Messages;
import org.talend.designer.runprocess.ui.ERunprocessImages;
import org.talend.designer.runprocess.ui.ProcessComposite;
import org.talend.designer.runprocess.ui.views.ProcessView;

/**
 * DOC smallet class global comment. Detailled comment <br/>
 *
 * $Id: ShowRunProcessViewAction.java 219 2006-10-24 13:45:54 +0000 (mar., 24 oct. 2006) smallet $
 *
 */
public class RunProcessAction extends Action implements IWorkbenchWindowActionDelegate {

    public RunProcessAction() {
        super();
        this.setText(Messages.getString("ProcessComposite.exec")); //$NON-NLS-1$
        this.setToolTipText(Messages.getString("ProcessComposite.execHint")); //$NON-NLS-1$
        this.setImageDescriptor(ImageProvider.getImageDesc(ERunprocessImages.RUN_PROCESS_ACTION));
        this.setActionDefinitionId("showAndRunProcessNotForShortcut"); //$NON-NLS-1$
    }

    @Override
    public void run() {

        if ((ProcessComposite.getProcessContext() != null && !ProcessComposite.getProcessContext().isRunning())) {

            IWorkbench workbench = PlatformUI.getWorkbench();
            IWorkbenchPage page = workbench.getActiveWorkbenchWindow().getActivePage();
            ComponentSettingsView compSettings = (ComponentSettingsView) page.findView(ComponentSettingsView.ID);
            if (compSettings != null) {
                compSettings.cleanDisplay();
            }
            // TODO SML Use getInstance
            ShowRunProcessViewAction action = new ShowRunProcessViewAction();
            action.run();

            // TODO SML Optimize
            ProcessView view = (ProcessView) page.getActivePart();
            view.runAction.run();

        }
    }

    public void dispose() {
    }

    public void init(IWorkbenchWindow window) {
    }

    public void run(IAction action) {
        run();
    }

    public void selectionChanged(IAction action, ISelection selection) {
    }

}
