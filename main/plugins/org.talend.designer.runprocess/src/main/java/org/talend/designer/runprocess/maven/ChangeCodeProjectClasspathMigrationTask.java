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
package org.talend.designer.runprocess.maven;

import java.util.Date;
import java.util.GregorianCalendar;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.talend.designer.maven.model.TalendMavenConstants;
import org.talend.designer.maven.utils.MavenProjectUtils;
import org.talend.migration.AbstractMigrationTask;
import org.talend.migration.IWorkspaceMigrationTask;

/**
 * DOC ggu class global comment. Detailled comment
 */
public class ChangeCodeProjectClasspathMigrationTask extends AbstractMigrationTask implements IWorkspaceMigrationTask {

    @Override
    public Date getOrder() {
        GregorianCalendar gc = new GregorianCalendar(2015, 6, 11, 12, 0, 0);
        return gc.getTime();
    }

    @Override
    public boolean execute() {
        IProject javaProject = ResourcesPlugin.getWorkspace().getRoot().getProject(TalendMavenConstants.PROJECT_NAME);
        if (javaProject.exists()) {
            // 1. check the .Java project, if existed, and non-Maven, just try to covert it ?
            // it have done for TalendCodeProjectUtil.initCodeProject, so no need here.

            // 2. check the .classpath (specially after RC1, for test-classes with 'excluding="**"' to remove)
            MavenProjectUtils.changeClasspath(new NullProgressMonitor(), javaProject);
            return true;
        }
        return false;
    }

}
