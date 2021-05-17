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

import org.eclipse.core.resources.IProject;
import org.talend.commons.exception.ExceptionHandler;
import org.talend.commons.utils.workbench.resources.ResourceUtils;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.model.general.Project;
import org.talend.core.model.migration.AbstractProjectMigrationTask;
import org.talend.core.services.IGITProviderService;

/**
 * DOC zwxue class global comment. Detailled comment
 */
public class UpdateGitIgnoreMigrationTask extends AbstractProjectMigrationTask {

    @Override
    public ExecutionResult execute(Project project) {
        if (GlobalServiceRegister.getDefault().isServiceRegistered(IGITProviderService.class)) {
            IGITProviderService service = (IGITProviderService) GlobalServiceRegister.getDefault()
                    .getService(IGITProviderService.class);
            try {
                if (service.isGITProject(project)) {
                    IProject eclipseProject = ResourceUtils.getProject(project);
                    service.createOrUpdateGitIgnoreFile(eclipseProject);
                }
                return ExecutionResult.SUCCESS_NO_ALERT;
            } catch (Exception e) {
                ExceptionHandler.process(e);
                return ExecutionResult.FAILURE;
            }
        }
        return ExecutionResult.NOTHING_TO_DO;
    }

    @Override
    public Date getOrder() {
        GregorianCalendar gc = new GregorianCalendar(2017, 12, 11, 16, 0, 0);
        return gc.getTime();
    }

}
