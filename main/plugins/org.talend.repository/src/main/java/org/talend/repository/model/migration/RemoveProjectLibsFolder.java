// ============================================================================
//
// Copyright (C) 2006-2018 Talend Inc. - www.talend.com
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

import java.io.File;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.talend.commons.exception.ExceptionHandler;
import org.talend.commons.utils.workbench.resources.ResourceUtils;
import org.talend.core.model.general.ModuleNeeded;
import org.talend.core.model.general.Project;
import org.talend.core.model.migration.AbstractProjectMigrationTask;
import org.talend.core.runtime.maven.MavenArtifact;
import org.talend.core.runtime.maven.MavenUrlHelper;
import org.talend.librariesmanager.maven.ShareLibrareisHelper;

/**
 * created by Talend on 2015年7月30日 Detailled comment
 *
 */
public class RemoveProjectLibsFolder extends AbstractProjectMigrationTask {

    private File libsFolder;

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.migration.IProjectMigrationTask#execute(org.talend.core.model.general.Project)
     */
    @Override
    public ExecutionResult execute(Project project) {
        String libsFolderName = "libs";
        IFolder libsF = null;
        try {
            IProject fsProject = ResourceUtils.getProject(project.getTechnicalLabel());
            libsF = fsProject.getFolder(libsFolderName);
            if (!libsF.exists()) {
                return ExecutionResult.NOTHING_TO_DO;
            }
            final IPath location = libsF.getLocation();
            libsFolder = new File(location.toPortableString());
            ShareProjectLibsMigration migration = new ShareProjectLibsMigration();
            migration.shareLibs(null, new NullProgressMonitor());
        } catch (Exception e) {
            ExceptionHandler.process(e);
            return ExecutionResult.FAILURE;
        }
        try {
            libsF.delete(true, new NullProgressMonitor());
        } catch (CoreException e) {
            ExceptionHandler.process(e);
            return ExecutionResult.FAILURE;
        }
        return ExecutionResult.SUCCESS_NO_ALERT;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.migration.IMigrationTask#getOrder()
     */
    @Override
    public Date getOrder() {
        GregorianCalendar gc = new GregorianCalendar(2015, 7, 30, 12, 0, 0);
        return gc.getTime();
    }

    /**
     * share libs from project to local maven and svn lib or nexus server depends on TAC setup, created by wchen on
     * 2015年7月31日 Detailled comment
     *
     */
    class ShareProjectLibsMigration extends ShareLibrareisHelper {

        private final Logger LOGGER = Logger.getLogger(ShareProjectLibsMigration.class);
        @Override
        public Map<ModuleNeeded, File> getFilesToShare(IProgressMonitor monitor) {
            Map<ModuleNeeded, File> libsToShare = new HashMap<ModuleNeeded, File>();
            File[] listFiles = libsFolder.listFiles();
            for (File lib : listFiles) {
                ModuleNeeded module = new ModuleNeeded("", lib.getName(), "", true);
                libsToShare.put(module, lib);
            }
            LOGGER.info("ShareProjectLibsMigration, getFilesToShare, libsToShare size: " + libsToShare.size());
            return libsToShare;
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.talend.librariesmanager.maven.ShareLibrareisHelper#deployToLocalMavenOnExist(java.io.File,
         * org.talend.core.model.general.ModuleNeeded)
         */
        @Override
        public void shareToRepository(File jarFile, MavenArtifact artifact) throws Exception {
            String uri = MavenUrlHelper.generateMvnUrl(artifact.getGroupId(), artifact.getArtifactId(), artifact.getArtifactId(),
                    artifact.getClassifier(), artifact.getVersion());
            LOGGER.info("ShareProjectLibsMigration, shareToRepository, jarFile: " + jarFile);
            deployer.install(jarFile.getPath(), uri);
        }
    }

}
