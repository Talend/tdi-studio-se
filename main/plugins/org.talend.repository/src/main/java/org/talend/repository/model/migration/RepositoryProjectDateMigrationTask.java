// ============================================================================
//
// Copyright (C) 2006-2017 Talend Inc. - www.talend.com
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

import org.apache.commons.lang3.StringUtils;
import org.talend.core.model.general.Project;
import org.talend.core.model.migration.AbstractProjectMigrationTask;
import org.talend.core.runtime.projectsetting.ProjectPreferenceManager;
import org.talend.repository.RepositoryPlugin;
import org.talend.utils.security.CryptoHelper;

/**
 * DOC ggu class global comment. Detailled comment
 */
public class RepositoryProjectDateMigrationTask extends AbstractProjectMigrationTask {

    @Override
    public Date getOrder() {
        GregorianCalendar gc = new GregorianCalendar(2018, 1, 16, 12, 0, 0);
        return gc.getTime();
    }

    @Override
    public ExecutionResult execute(Project project) {
        ProjectPreferenceManager projectPrefManager = new ProjectPreferenceManager(project, RepositoryPlugin.PLUGIN_ID, false);
        final String key = "repository.project.id"; //$NON-NLS-1$
        final String value = projectPrefManager.getValue(key);
        if (StringUtils.isBlank(value)) {
            projectPrefManager.setValue(key, CryptoHelper.getDefault().encrypt(String.valueOf(System.currentTimeMillis())));
            projectPrefManager.save();
            return ExecutionResult.SUCCESS_NO_ALERT;
        }
        return ExecutionResult.NOTHING_TO_DO;
    }

}
