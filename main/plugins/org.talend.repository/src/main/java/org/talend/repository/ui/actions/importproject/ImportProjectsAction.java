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
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.wizards.datatransfer.ExternalProjectImportWizard;
import org.talend.commons.ui.runtime.image.ImageProvider;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.ui.branding.IBrandingService;
import org.talend.repository.i18n.Messages;
import org.talend.repository.ui.ERepositoryImages;
import org.talend.repository.ui.wizards.newproject.copyfromeclipse.TalendExternalProjectImportWizard;

/**
 * Action used to refresh a repository view.<br/>
 *
 * $Id: RefreshAction.java 824 2006-12-01 15:49:55 +0000 (ven., 01 déc. 2006) smallet $
 *
 */
public final class ImportProjectsAction extends Action implements IWorkbenchWindowActionDelegate {

    private static final String ACTION_TITLE = Messages.getString("ImportProjectsAction.actionTitle"); //$NON-NLS-1$

    private static final String ACTION_TOOLTIP = Messages.getString("ImportProjectsAction.actionTooltip", //$NON-NLS-1$
            ((IBrandingService) GlobalServiceRegister.getDefault().getService(IBrandingService.class)).getShortProductName()); //$NON-NLS-1$

    private static final String STORE_COPY_PROJECT = "WizardProjectsImportPage.STORE_COPY_PROJECT_ID"; //$NON-NLS-1$

    private static ImportProjectsAction singleton;

    public ImportProjectsAction() {
        super();
        this.setText(ACTION_TITLE);
        this.setToolTipText(ACTION_TOOLTIP);
        this.setImageDescriptor(ImageProvider.getImageDesc(ERepositoryImages.IMPORT_PROJECTS_ACTION));
        singleton = this;
    }

    public static ImportProjectsAction getInstance() {
        if (singleton == null) {
            singleton = new ImportProjectsAction();
        }
        return singleton;
    }

    public void run() {
        ExternalProjectImportWizard processWizard = new TalendExternalProjectImportWizard();

        // Set the "Copy projects into workspace" value default as true:
        IDialogSettings dialogSettings = processWizard.getDialogSettings();
        if (dialogSettings.get(STORE_COPY_PROJECT) == null) {
            dialogSettings.put(STORE_COPY_PROJECT, true);
        }

        Shell activeShell = Display.getCurrent().getActiveShell();
        WizardDialog dialog = new WizardDialog(activeShell, processWizard);
        processWizard.setWindowTitle(ACTION_TITLE);
        dialog.create();

        dialog.open();
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
