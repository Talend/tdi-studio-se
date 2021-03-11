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
import org.talend.commons.exception.PersistenceException;
import org.talend.commons.ui.runtime.exception.ExceptionHandler;
import org.talend.commons.utils.workbench.resources.ResourceUtils;
import org.talend.core.model.general.Project;
import org.talend.core.model.migration.AbstractProjectMigrationTask;

/**
 * DOC smallet class global comment. Detailled comment <br/>
 *
 * $Id: talend.epf 1 2006-09-29 17:06:40 +0000 (ven., 29 sept. 2006) nrousseau $
 *
 */
public class RemoveRoutineFolderTask extends AbstractProjectMigrationTask {

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.migration.IMigrationTask#execute()
     */
    @Override
    public ExecutionResult execute(Project project) {
        if (!project.isLocal()) {
            return ExecutionResult.NOTHING_TO_DO;
        }

        try {
            IProject iproject = ResourceUtils.getProject(project);
            String oldRoutinesPath = "routines"; // Routines path as it was in talend v1.0.n and until 1.1.m2 //$NON-NLS-1$
            // //$NON-NLS-1$
            IFolder f2 = ResourceUtils.getFolder(iproject, oldRoutinesPath, false);
            ResourceUtils.deleteResource(f2);
            return ExecutionResult.SUCCESS_NO_ALERT;
        } catch (PersistenceException e) {
            ExceptionHandler.process(e);
            return ExecutionResult.FAILURE;
        }
    }

    @Override
    public Date getOrder() {
        GregorianCalendar gc = new GregorianCalendar(2008, 2, 17, 12, 0, 0);
        return gc.getTime();
    }
}
