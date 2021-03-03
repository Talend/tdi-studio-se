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
package org.talend.repository.model.migration;

import java.util.Date;
import java.util.GregorianCalendar;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.ltk.core.refactoring.CheckConditionsOperation;
import org.eclipse.ltk.core.refactoring.CreateChangeOperation;
import org.eclipse.ltk.core.refactoring.PerformChangeOperation;
import org.eclipse.ltk.core.refactoring.RefactoringCore;
import org.eclipse.ltk.core.refactoring.participants.RenameRefactoring;
import org.eclipse.ltk.internal.core.refactoring.resource.RenameResourceProcessor;
import org.talend.commons.exception.PersistenceException;
import org.talend.commons.ui.runtime.exception.ExceptionHandler;
import org.talend.core.model.general.TalendNature;
import org.talend.core.model.properties.Project;
import org.talend.core.repository.utils.XmiResourceManager;
import org.talend.migration.AbstractMigrationTask;
import org.talend.migration.IWorkspaceMigrationTask;
import org.talend.repository.ProjectManager;
import org.talend.utils.io.FilesUtils;

/**
 * DOC ggu class global comment. Detailled comment <br/>
 *
 * Only work for local project, TDI-22686.
 *
 * $Id: talend.epf 55206 2011-02-15 17:32:14Z mhirt $
 *
 */
public class ChangeProjectTechinicalNameMigrationTask extends AbstractMigrationTask implements IWorkspaceMigrationTask {

    protected final XmiResourceManager xmiResManager;

    /**
     * DOC ggu ChangeProjectTechinicalNameMigrationTask constructor comment.
     */
    public ChangeProjectTechinicalNameMigrationTask() {
        super();
        this.xmiResManager = new XmiResourceManager();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.migration.IMigrationTask#getOrder()
     */
    @Override
    public Date getOrder() {
        GregorianCalendar gc = new GregorianCalendar(2012, 9, 10, 0, 0, 0);
        return gc.getTime();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.migration.IWorkspaceMigrationTask#execute()
     */
    @Override
    public boolean execute() {
        if (!ProjectManager.enableSpecialTechnicalProjectName()) {
            return false; // no need to do migration.
        }
        try {
            IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
            try {
                root.refreshLocal(IResource.DEPTH_INFINITE, null);
            } catch (CoreException e) {
                ExceptionHandler.process(e);
            }
            IProject[] projects = root.getProjects();
            for (IProject localProject : projects) {
                try {
                    if (localProject.hasNature(TalendNature.ID)) { // only for talend project
                        Project talendProject = xmiResManager.loadProject(localProject);
                        if (needCheckProject(localProject, talendProject)) {
                            checkTalendProject(localProject, talendProject);
                        }
                    }

                } catch (Exception e) {
                    ExceptionHandler.process(e);
                    return false;
                }
            }

        } finally {
            xmiResManager.unloadResources();
        }
        // always true, no need do again.
        return true;
    }

    protected boolean needCheckProject(IProject localProject, Project talendProject) {
        return !isSvnProject(localProject, talendProject);// only check the local project
    }

    protected boolean checkTalendProject(IProject localProject, Project talendProject) {
        return migrateTalendProject(localProject, talendProject, xmiResManager);
    }

    /**
     *
     * default, only process the local talend project in this migration task.
     *
     */
    public boolean migrateTalendProject(IProject localProject, Project talendProject, XmiResourceManager xmiResourceManager) {
        try {
            String localProjectName = localProject.getName();
            String technicalLabel = talendProject.getTechnicalLabel();
            String technicalProjectName = ProjectManager.getLocalTechnicalProjectName(talendProject.getLabel());

            // need update the technical label or not. NOTE, must update it before rename project.
            if (!technicalLabel.equals(technicalProjectName)) {
                setAndSaveTalendProject(talendProject, technicalProjectName, xmiResourceManager);
            }

            // not same, need rename the local project name to match the technical name.
            if (!localProjectName.equals(technicalProjectName)) {
                renameProject(localProject, technicalProjectName);
            }
        } catch (Exception e) {
            ExceptionHandler.process(e);
            return false;
        }
        return true;
    }

    protected boolean isSvnProject(IProject localProject, Project talendProject) {
        for (String svnFolderName : FilesUtils.SVN_FOLDER_NAMES) {
            IFolder svnFolder = localProject.getFolder(svnFolderName);
            if (svnFolder.exists()) { // maybe, it's svn project
                return true;
            }
        }
        return false;
    }

    /**
     *
     * DOC ggu Comment method "setAndSaveTalendProject".
     *
     * @param talendProject
     * @param newTechnicalProjectName
     * @return
     */
    protected boolean setAndSaveTalendProject(Project talendProject, String newTechnicalProjectName,
            XmiResourceManager xmiResourceManager) {
        try {
            talendProject.setTechnicalLabel(newTechnicalProjectName);
            xmiResourceManager.saveResource(talendProject.eResource());
        } catch (PersistenceException e) {
            ExceptionHandler.process(e);
            return false;
        }
        return true;
    }

    /**
     *
     * DOC ggu Comment method "renameProject".
     *
     * @param localProject
     * @param newProjectName
     * @return
     */
    @SuppressWarnings("restriction")
    protected boolean renameProject(IProject localProject, String newProjectName) {

        RenameResourceProcessor renameProcessor = new RenameResourceProcessor(localProject);
        renameProcessor.setNewResourceName(newProjectName);

        CheckConditionsOperation condictionOperation = new CheckConditionsOperation(new RenameRefactoring(renameProcessor),
                CheckConditionsOperation.FINAL_CONDITIONS);
        CreateChangeOperation operation = new CreateChangeOperation(condictionOperation,
                RefactoringCore.getConditionCheckingFailedSeverity());

        PerformChangeOperation performChangeOp = new PerformChangeOperation(operation);

        try {
            performChangeOp.run(null);
        } catch (CoreException e) {
            ExceptionHandler.process(e);
            return false;
        }
        return true;
    }
}
