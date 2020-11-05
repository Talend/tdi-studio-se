// ============================================================================
//
// Copyright (C) 2006-2019 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.designer.core.ui.action;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.widgets.Event;
import org.eclipse.ui.IActionDelegate2;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.talend.core.PluginChecker;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.designer.core.i18n.Messages;
import org.talend.designer.core.utils.DesignerUtilities;
import org.talend.repository.ui.dialog.OpenJobSelectionDialog;
import org.talend.repository.ui.dialog.RepositoryReviewDialog;

public class OpenJobAction extends Action implements IWorkbenchWindowActionDelegate, IActionDelegate2 {

    IWorkbenchWindow window = null;

    public OpenJobAction() {
        super();
        setDescription(Messages.getString("OpenJobAction.description")); //$NON-NLS-1$
        setToolTipText(Messages.getString("OpenJobAction.tipText")); //$NON-NLS-1$
    }

    @Override
    public void run() {
        List<ERepositoryObjectType> repObjectTypes = new ArrayList<ERepositoryObjectType>();
        repObjectTypes.add(ERepositoryObjectType.PROCESS);
        if (ERepositoryObjectType.PROCESS_ROUTE != null) {
            repObjectTypes.add(ERepositoryObjectType.PROCESS_ROUTE);
        }
        if (ERepositoryObjectType.PROCESS_ROUTELET != null) {
            repObjectTypes.add(ERepositoryObjectType.PROCESS_ROUTELET);
        }
        if (PluginChecker.isMapReducePluginLoader()) {
            repObjectTypes.add(ERepositoryObjectType.PROCESS_MR);
        }
        final OpenJobSelectionDialog dialog = new OpenJobSelectionDialog(window.getShell(), repObjectTypes);

        if (dialog.open() == RepositoryReviewDialog.OK) {
            IAction action = DesignerUtilities.getEditProcessAction(dialog.getResult());
            if (action != null) {
                action.run();
            }
        }
    }

    @Override
    public void run(IAction action) {
        run();
    }

    @Override
    public void dispose() {
        // do nothing.
    }

    @Override
    public void init(IWorkbenchWindow window) {
        this.window = window;
    }

    @Override
    public void selectionChanged(IAction action, ISelection selection) {
        // do nothing. Action doesn't depend on selection.
    }

    // ---- IActionDelegate2
    // ------------------------------------------------

    @Override
    public void runWithEvent(IAction action, Event event) {
        runWithEvent(event);
    }

    @Override
    public void init(IAction action) {
        // do nothing.
    }
}
