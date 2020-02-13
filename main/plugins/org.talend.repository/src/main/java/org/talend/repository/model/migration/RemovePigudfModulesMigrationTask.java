// ============================================================================
//
// Copyright (C) 2006-2020 Talend Inc. - www.talend.com
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
import java.util.ListIterator;

import org.apache.commons.lang.StringUtils;
import org.apache.maven.model.Model;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.m2e.core.MavenPlugin;
import org.talend.commons.exception.ExceptionHandler;
import org.talend.core.model.general.Project;
import org.talend.core.model.migration.AbstractProjectMigrationTask;
import org.talend.designer.maven.tools.AggregatorPomsHelper;
import org.talend.designer.maven.utils.PomUtil;

/**
 * Remove pigudf code project and modules from rootpom for the removal of pigudf DOC tifa class global comment.
 * Detailled comment
 */
public class RemovePigudfModulesMigrationTask extends AbstractProjectMigrationTask {

    @Override
    public ExecutionResult execute(Project project) {
        AggregatorPomsHelper helper = new AggregatorPomsHelper(project.getTechnicalLabel());
        IFolder projectPomsFolder = helper.getProjectPomsFolder();
        String relativePath = "code/pigudf"; //$NON-NLS-1$
        IPath pigudfPath = new Path(relativePath);
        if (!projectPomsFolder.exists() || !projectPomsFolder.exists(pigudfPath)) {
            return ExecutionResult.NOTHING_TO_DO;
        }
        try {
            IFile projectRootPom = helper.getProjectRootPom();
            Model model = MavenPlugin.getMavenModelManager().readMavenModel(helper.getProjectRootPom());
            ListIterator<String> iterator = model.getModules().listIterator();
            while (iterator.hasNext()) {
                String next = iterator.next();
                if (StringUtils.isNotBlank(next) && relativePath.equals(next)) {
                    iterator.remove();
                }
            }
            PomUtil.savePom(null, model, projectRootPom);

            IResource member = projectPomsFolder.findMember(pigudfPath);
            if (member != null) {
                member.delete(true, null);
            }
        } catch (Exception e) {
            ExceptionHandler.process(e);
            return ExecutionResult.FAILURE;
        }

        return ExecutionResult.SUCCESS_NO_ALERT;
    }

    @Override
    public Date getOrder() {
        GregorianCalendar gc = new GregorianCalendar(2020, 2, 12, 12, 0, 0);
        return gc.getTime();
    }

}
