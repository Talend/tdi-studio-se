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

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Display;
import org.talend.commons.ui.runtime.exception.ExceptionHandler;
import org.talend.commons.ui.runtime.image.ImageProvider;
import org.talend.commons.utils.workbench.resources.ResourceUtils;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.model.general.Project;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.core.ui.branding.IBrandingService;
import org.talend.repository.i18n.Messages;
import org.talend.repository.model.IProxyRepositoryFactory;
import org.talend.repository.ui.ERepositoryImages;
import org.talend.repository.ui.wizards.newproject.ImportProjectAsWizard;
import org.talend.repository.ui.wizards.newproject.ImportProjectWizardDialog;

/**
 * Action used to refresh a repository view.<br/>
 *
 * $Id: RefreshAction.java 824 2006-12-01 15:49:55 +0000 (ven., 01 déc. 2006) smallet $
 *
 */
public final class ImportProjectAsAction extends Action {

    private static final String LIB = "lib"; //$NON-NLS-1$

    private static final String ACTION_TITLE = Messages.getString("ImportProjectAsAction.actionTitle"); //$NON-NLS-1$

    private static final String ACTION_TOOLTIP = Messages.getString("ImportProjectAsAction.actionTooltip", //$NON-NLS-1$
            ((IBrandingService) GlobalServiceRegister.getDefault().getService(IBrandingService.class)).getShortProductName());

    private static ImportProjectAsAction singleton;

    public static ImportProjectAsAction getInstance() {
        if (singleton == null) {
            singleton = new ImportProjectAsAction();
        }
        return singleton;
    }

    private String project;

    protected boolean isImportedSeveralProjects;

    private ImportProjectAsAction() {
        super();
        this.setText(ACTION_TITLE);
        this.setToolTipText(ACTION_TOOLTIP);
        this.setImageDescriptor(ImageProvider.getImageDesc(ERepositoryImages.IMPORT_PROJECTS_ACTION));
    }

    @Override
    public void run() {
        ImportProjectAsWizard docWizard = new ImportProjectAsWizard();
        WizardDialog dlg = new ImportProjectWizardDialog(Display.getCurrent().getActiveShell(), docWizard);

        if (dlg.open() == IDialogConstants.OK_ID) {
            project = docWizard.getProjectName();
            isImportedSeveralProjects = docWizard.isImportedSeveralProjects();
        } else {
            project = null;
            isImportedSeveralProjects = false;
        }

        clearExternalLibraries();

    }

    /**
     * DOC zwang Comment method "clearExternalLibraries".
     */
    private void clearExternalLibraries() {

        try {
            IProxyRepositoryFactory repositoryFactory = ProxyRepositoryFactory.getInstance();
            Project[] projects = repositoryFactory.readProject();
            for (Project project : projects) {
                IProject fsProject = ResourceUtils.getProject(project);
                IFolder libJavaFolder = fsProject.getFolder(ImportProjectAsAction.LIB);
                if (!libJavaFolder.exists()) {
                    continue;
                }
                libJavaFolder.delete(true, null);
            }
        } catch (Exception e) {
            // e.printStackTrace();
            ExceptionHandler.process(e);
        }
    }

    public String getProjectName() {
        return project;
    }

    public boolean isImportedSeveralProjects() {
        return isImportedSeveralProjects;
    }
}
