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

import org.apache.commons.lang.StringUtils;
import org.eclipse.jdt.core.JavaCore;
import org.talend.commons.utils.generation.JavaUtils;
import org.talend.core.model.general.Project;
import org.talend.core.model.migration.AbstractProjectMigrationTask;
import org.talend.core.runtime.CoreRuntimePlugin;
import org.talend.core.runtime.projectsetting.ProjectPreferenceManager;

/**
 * created by nrousseau on Sep 12, 2017 Detailled comment
 *
 */
public class UpdateToJava8VersionMigrationTask extends AbstractProjectMigrationTask {

    @Override
    public ExecutionResult execute(Project project) {
        ProjectPreferenceManager manager = new ProjectPreferenceManager(project, CoreRuntimePlugin.PLUGIN_ID, false);
        String javaVersion = manager.getValue(JavaUtils.PROJECT_JAVA_VERSION_KEY);
        if (StringUtils.isBlank(javaVersion) || !JavaCore.VERSION_1_8.equals(javaVersion)) {
            manager.setValue(JavaUtils.PROJECT_JAVA_VERSION_KEY, JavaCore.VERSION_1_8);
            manager.save();
            return ExecutionResult.SUCCESS_NO_ALERT;
        }
        return ExecutionResult.NOTHING_TO_DO;
    }

    @Override
    public Date getOrder() {
        GregorianCalendar gc = new GregorianCalendar(2017, 9, 12, 12, 0, 0);
        return gc.getTime();
    }

}
