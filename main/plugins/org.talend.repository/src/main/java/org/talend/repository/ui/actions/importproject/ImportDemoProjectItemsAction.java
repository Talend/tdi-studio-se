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

import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Shell;
import org.talend.commons.ui.runtime.image.ImageProvider;
import org.talend.repository.i18n.Messages;
import org.talend.repository.ui.ERepositoryImages;

public final class ImportDemoProjectItemsAction extends Action {

    public static final String DEMO_ALREADY_IMPORTED = "demoProjectAlreadyImported"; //$NON-NLS-1$

    private static final String ACTION_TITLE = Messages.getString("ImportDemoProjectAction.actionTitle"); //$NON-NLS-1$

    private static final String ACTION_TOOLTIP = Messages.getString("ImportDemoProjectAction.actionTooltip"); //$NON-NLS-1$

    private String lastImportedName;

    private static ImportDemoProjectItemsAction singleton;

    public static ImportDemoProjectItemsAction getInstance() {
        if (singleton == null) {
            singleton = new ImportDemoProjectItemsAction();
        }
        return singleton;
    }

    private Shell shell;

    private ImportDemoProjectItemsAction() {
        super();
        this.setText(ACTION_TITLE);
        this.setToolTipText(ACTION_TOOLTIP);
        this.setImageDescriptor(ImageProvider.getImageDesc(ERepositoryImages.IMPORT_PROJECTS_ACTION));
    }

    @Override
    public void run() {

        final List<DemoProjectBean> demoProjectList = ImportProjectsUtilities.getAllDemoProjects();

        ImportDemoProjectItemsWizard demoProjectWizard = new ImportDemoProjectItemsWizard(demoProjectList);
        WizardDialog dialog = new WizardDialog(shell, demoProjectWizard);
        dialog.open();

    }

    public String getProjectName() {
        return lastImportedName;
    }

    public void setShell(Shell shell) {
        this.shell = shell;
    }
}
