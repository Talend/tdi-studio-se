// ============================================================================
//
// Copyright (C) 2006-2016 Talend Inc. - www.talend.com
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

import org.eclipse.jdt.core.JavaCore;
import org.talend.commons.utils.generation.JavaUtils;
import org.talend.core.model.general.Project;
import org.talend.core.model.migration.AbstractProjectMigrationTask;

public class AddJavaVersionMigrationTask extends AbstractProjectMigrationTask {

    @Override
    public ExecutionResult execute(Project project) {
        String javaVersion = JavaUtils.getProjectJavaVersion();
        if (javaVersion == null || !JavaUtils.AVAILABLE_VERSIONS.contains(javaVersion)) {
            JavaUtils.updateProjectJavaVersion(JavaCore.VERSION_1_7);
            return ExecutionResult.SUCCESS_NO_ALERT;
        }
        return ExecutionResult.NOTHING_TO_DO;
    }

    @Override
    public Date getOrder() {
        GregorianCalendar gc = new GregorianCalendar(2017, 4, 17, 12, 0, 0);
        return gc.getTime();
    }

}
