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
import java.util.HashMap;
import java.util.Map;

import org.eclipse.emf.common.util.EList;
import org.talend.commons.exception.PersistenceException;
import org.talend.commons.ui.runtime.exception.ExceptionHandler;
import org.talend.core.model.general.Project;
import org.talend.core.model.migration.AbstractProjectMigrationTask;
import org.talend.core.model.properties.MigrationTask;
import org.talend.core.model.utils.MigrationUtil;
import org.talend.core.repository.model.ProxyRepositoryFactory;

/**
 * DOC ycbai class global comment. Detailled comment
 */
public class UpdateExistentMigrationTasksToAdaptNewMigrationSystemMigrationTask extends AbstractProjectMigrationTask {

    public ExecutionResult execute(Project project, boolean doSave) {
        try {
            boolean modified = updateMigrationTask(project, doSave);
            if (modified) {
                return ExecutionResult.SUCCESS_NO_ALERT;
            } else {
                return ExecutionResult.NOTHING_TO_DO;
            }
        } catch (PersistenceException e) {
            ExceptionHandler.process(e);
            return ExecutionResult.FAILURE;
        }

    }

    public ExecutionResult execute(Project project) {
        return execute(project, true);
    }

    private boolean updateMigrationTask(Project project, boolean doSave) throws PersistenceException {
        boolean modified = false;
        org.talend.core.model.properties.Project emfProject = project.getEmfProject();
        EList<String> oldMigrationTasks = emfProject.getMigrationTasks();
        EList<MigrationTask> newMigrationTasks = emfProject.getMigrationTask();
        Map<String, MigrationTask> newMigrationTasksMap = new HashMap<String, MigrationTask>();
        for (MigrationTask migrationTask : newMigrationTasks) {
            newMigrationTasksMap.put(migrationTask.getId(), migrationTask);
        }
        for (String taskId : oldMigrationTasks) {
            if (newMigrationTasksMap.get(taskId) == null) {
                newMigrationTasks.add(MigrationUtil.createDefaultMigrationTask(taskId));
                modified = true;
            }
        }
        oldMigrationTasks.clear();
        if (doSave) {
            ProxyRepositoryFactory.getInstance().saveProject(project);
        }

        return modified;
    }

    public Date getOrder() {
        GregorianCalendar gc = new GregorianCalendar(2012, 7, 12, 12, 0, 0);
        return gc.getTime();
    }

}
