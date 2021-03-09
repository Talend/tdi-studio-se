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
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Shell;
import org.talend.commons.ui.runtime.image.ImageProvider;
import org.talend.core.model.general.Project;
import org.talend.repository.i18n.Messages;
import org.talend.repository.ui.ERepositoryImages;

/**
 * Action used to refresh a repository view.<br/>
 *
 * $Id: RefreshAction.java 824 2006-12-01 15:49:55 +0000 (ven., 01 déc. 2006) smallet $
 *
 */
public final class ImportDemoProjectAction extends Action {

    public static final String DEMO_ALREADY_IMPORTED = "demoProjectAlreadyImported"; //$NON-NLS-1$

    private static final String ACTION_TITLE = Messages.getString("ImportDemoProjectAction.actionTitle"); //$NON-NLS-1$

    private static final String ACTION_TOOLTIP = Messages.getString("ImportDemoProjectAction.actionTooltip"); //$NON-NLS-1$

    private String lastImportedName;

    private static ImportDemoProjectAction singleton;

    public static ImportDemoProjectAction getInstance() {
        if (singleton == null) {
            singleton = new ImportDemoProjectAction();
        }
        return singleton;
    }

    private Shell shell;

    private Project[] projects;

    private ImportDemoProjectAction() {
        super();
        this.setText(ACTION_TITLE);
        this.setToolTipText(ACTION_TOOLTIP);
        this.setImageDescriptor(ImageProvider.getImageDesc(ERepositoryImages.IMPORT_PROJECTS_ACTION));
    }

    @Override
    public void run() {

        final List<DemoProjectBean> demoProjectList = ImportProjectsUtilities.getAllDemoProjects();

        ImportDemoProjectWizard demoProjectWizard = new ImportDemoProjectWizard(demoProjectList);

        WizardDialog dialog = new WizardDialog(shell, demoProjectWizard);
        if (dialog.open() == IDialogConstants.OK_ID) {
            this.lastImportedName = demoProjectWizard.getProjectName();
        } else {
            this.lastImportedName = null;
        }
    }

    public String getProjectName() {
        return lastImportedName;
    }

    public void setShell(Shell shell) {
        this.shell = shell;
    }

    public void setExistingProjects(Project[] projects) {
        this.projects = projects;
    }

    private boolean checkProjectIsExisting(String techName) {
        if (this.projects == null || this.projects.length == 0) {
            return false;
        }
        for (Project project : projects) {
            if (project.getTechnicalLabel().equalsIgnoreCase(techName)) {
                return true;
            }
        }
        return false;
    }
}
