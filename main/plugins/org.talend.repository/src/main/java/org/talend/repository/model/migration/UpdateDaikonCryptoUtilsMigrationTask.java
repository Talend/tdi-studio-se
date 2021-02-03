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

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.logging.Logger;

import org.apache.commons.lang3.StringUtils;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.m2e.core.MavenPlugin;
import org.talend.commons.exception.ExceptionHandler;
import org.talend.core.model.general.Project;
import org.talend.core.model.migration.AbstractProjectMigrationTask;
import org.talend.designer.maven.tools.AggregatorPomsHelper;

/*
 * Created by bhe on Sep 29, 2020
 */
public class UpdateDaikonCryptoUtilsMigrationTask extends AbstractProjectMigrationTask {

    private static final Logger LOGGER = Logger.getLogger(UpdateDaikonCryptoUtilsMigrationTask.class.getCanonicalName());

    @Override
    public Date getOrder() {
        GregorianCalendar gc = new GregorianCalendar(2021, 2, 3, 12, 00, 00);
        return gc.getTime();
    }

    @Override
    public ExecutionResult execute(Project project) {
        AggregatorPomsHelper pomHelper = new AggregatorPomsHelper();
        IFile rootPomFile = pomHelper.getProjectRootPom();
        if (rootPomFile.exists()) {
            try {
                boolean isRegeneratePoms = false;
                Model model = MavenPlugin.getMaven().readModel(rootPomFile.getLocation().toFile());
                if (model.getModules() != null) {
                    for (String module : model.getModules()) {
                        Path modPath = Paths.get(rootPomFile.getLocation().toFile().getParent(), module, "pom.xml");
                        Model modModel = MavenPlugin.getMaven().readModel(modPath.toFile());
                        isRegeneratePoms = doMigration(modModel.getDependencies());
                        if (isRegeneratePoms) {
                            break;
                        }
                    }
                }
                LOGGER.info("modules: " + model.getModules() + ", isRegeneratePoms: " + isRegeneratePoms);
                if (isRegeneratePoms) {
                    pomHelper.syncAllPomsWithoutProgress(new NullProgressMonitor());
                    return ExecutionResult.SUCCESS_NO_ALERT;
                }
                return ExecutionResult.NOTHING_TO_DO;
            } catch (Exception e) {
                ExceptionHandler.process(e);
                return ExecutionResult.FAILURE;
            }
        }
        return ExecutionResult.NOTHING_TO_DO;
    }

    private static boolean doMigration(List<Dependency> deps) {
        if (deps == null || deps.isEmpty()) {
            return false;
        }
        for (Dependency dep : deps) {
            if (StringUtils.equals("org.talend.daikon", dep.getGroupId())) {
                if (StringUtils.equals("daikon", dep.getArtifactId())) {
                    return true;
                }
            }
        }
        return false;
    }
}
