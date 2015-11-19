// ============================================================================
//
// Copyright (C) 2006-2015 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.designer.codegen.components.model;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.preference.IPreferenceStore;
import org.osgi.framework.Bundle;
import org.talend.commons.ui.runtime.exception.ExceptionHandler;
import org.talend.commons.utils.io.FilesUtils;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.PluginChecker;
import org.talend.core.i18n.Messages;
import org.talend.core.model.components.AbstractComponentsProvider;
import org.talend.core.model.components.ComponentUtilities;
import org.talend.core.model.components.IComponentsFactory;
import org.talend.core.model.general.Project;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.ui.branding.IBrandingService;
import org.talend.designer.codegen.CodeGeneratorActivator;
import org.talend.designer.codegen.components.ui.IComponentPreferenceConstant;
import org.talend.repository.ProjectManager;

/***/
public class UserComponentsProvider extends AbstractComponentsProvider {

    private static Logger logger = Logger.getLogger(UserComponentsProvider.class);

    /***/
    public UserComponentsProvider() {
    }

    @Override
    protected File getExternalComponentsLocation() {
        IPreferenceStore prefStore = CodeGeneratorActivator.getDefault().getPreferenceStore();
        String path = prefStore.getString(IComponentPreferenceConstant.USER_COMPONENTS_FOLDER);
        return (path == null || path.length() == 0 ? null : new File(path));
    }

    public void setUserComponentPath(String path) {
        if (path == null) {
            path = ""; //$NON-NLS-1$
        }
        IPreferenceStore prefStore = CodeGeneratorActivator.getDefault().getPreferenceStore();
        File userPath = new File(path.trim());
        if (userPath.exists()) {
            prefStore.setValue(IComponentPreferenceConstant.USER_COMPONENTS_FOLDER, path);
        } else {
            prefStore.setValue(IComponentPreferenceConstant.USER_COMPONENTS_FOLDER, ""); //$NON-NLS-1$
        }
    }

    public String getUserComponentPath() {
        File path = getExternalComponentsLocation();
        if (path == null) {
            return ""; //$NON-NLS-1$
        }
        return path.toString();
    }

    @Override
    public void preComponentsLoad() throws IOException {
        File installationFolder = getInstallationFolder();
        if (installationFolder.exists()) {
            FilesUtils.removeFolder(installationFolder, true);
        }
        FilesUtils.createFoldersIfNotExists(installationFolder.getAbsolutePath(), false);
        FileFilter ff = new FileFilter() {

            @Override
            public boolean accept(File pathname) {
                if (FilesUtils.isSVNFolder(pathname)) {
                    return false;
                }
                return true;
            }

        };

        // synchroniz shared custom component
        if (PluginChecker.isSVNProviderPluginLoaded()) {
            Set<Project> allProjects = new HashSet<Project>();
            allProjects.add(ProjectManager.getInstance().getCurrentProject());
            allProjects.addAll(ProjectManager.getInstance().getReferencedProjects());
            for (Project project : allProjects) {
                String projectLabel = project.getTechnicalLabel();
                IProject eclipseProject = ResourcesPlugin.getWorkspace().getRoot().getProject(projectLabel);
                String sourcePath = eclipseProject.getLocation().toString() + "/"
                        + ERepositoryObjectType.getFolderName(ERepositoryObjectType.COMPONENTS);
                File source = new File(sourcePath);
                if (source.exists()) {
                    for (File file : source.listFiles(ff)) {
                        if (file.isDirectory())
                            FilesUtils.copyFolder(file,
                                    new File(installationFolder.getAbsolutePath() + File.separator + file.getName()), true, ff,
                                    null, true, false);
                    }
                }
            }
        }

        // if components in user component path include some shared components , replace it
        File externalComponentsLocation = getExternalComponentsLocation();
        if (externalComponentsLocation != null) {
            if (externalComponentsLocation.exists()) {
                try {
                    FilesUtils.copyFolder(externalComponentsLocation, installationFolder, false, ff, null, true, false);
                } catch (IOException e) {
                    ExceptionHandler.process(e);
                }

            } else {
                logger.warn(Messages
                        .getString("AbstractComponentsProvider.folderNotExist", externalComponentsLocation.toString())); //$NON-NLS-1$
            }
        }
    }

    @Override
    public String getComponentsLocation() {
        return new Path(IComponentsFactory.COMPONENTS_INNER_FOLDER).append(IComponentsFactory.EXTERNAL_COMPONENTS_INNER_FOLDER)
                .append(ComponentUtilities.getExtFolder(getFolderName())).toString();
    }

    @Override
    public File getInstallationFolder() throws IOException {
        String componentsPath = IComponentsFactory.COMPONENTS_LOCATION;
        IBrandingService breaningService = (IBrandingService) GlobalServiceRegister.getDefault().getService(
                IBrandingService.class);
        if (breaningService.isPoweredOnlyCamel()) {
            componentsPath = IComponentsFactory.CAMEL_COMPONENTS_LOCATION;
        }
        Bundle b = Platform.getBundle(componentsPath);

        File installationFolder = null;
        IPath nullPath = new Path(""); //$NON-NLS-1$
        URL url = FileLocator.find(b, nullPath, null);
        URL fileUrl = FileLocator.toFileURL(url);
        File bundleFolder = new File(fileUrl.getPath());

        IPath path = new Path(IComponentsFactory.COMPONENTS_INNER_FOLDER)
                .append(IComponentsFactory.EXTERNAL_COMPONENTS_INNER_FOLDER);
        path = path.append(ComponentUtilities.getExtFolder(getFolderName()));

        installationFolder = new File(bundleFolder, path.toOSString());

        return installationFolder;
    }

    @Override
    public boolean isUseLocalProvider() {
        return true;
    }
}
