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
package org.talend.designer.codegen.components.model;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.preference.IPreferenceStore;
import org.osgi.framework.Bundle;
import org.talend.commons.utils.io.FilesUtils;
import org.talend.commons.utils.resource.UpdatesHelper;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.model.components.AbstractCustomComponentsProvider;
import org.talend.core.model.components.ComponentUtilities;
import org.talend.core.model.components.IComponentsFactory;
import org.talend.core.model.general.Project;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.runtime.util.SharedStudioInfoProvider;
import org.talend.core.runtime.util.SharedStudioUtils;
import org.talend.core.ui.branding.IBrandingService;
import org.talend.designer.codegen.CodeGeneratorActivator;
import org.talend.designer.codegen.components.ui.IComponentPreferenceConstant;
import org.talend.repository.ProjectManager;

/***/
public class UserComponentsProvider extends AbstractCustomComponentsProvider implements SharedStudioInfoProvider{

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
        super.preComponentsLoad();

        // 2. copy old CF components from <project>/components
        final File installationFolder = getInstallationFolder();

        // synchroniz shared custom component
        Set<Project> allProjects = new HashSet<Project>();
        allProjects.add(ProjectManager.getInstance().getCurrentProject());
        allProjects.addAll(ProjectManager.getInstance().getAllReferencedProjects());
        for (Project project : allProjects) {
            String projectLabel = project.getTechnicalLabel();
            IProject eclipseProject = ResourcesPlugin.getWorkspace().getRoot().getProject(projectLabel);
            String sourcePath = eclipseProject.getLocation().toString() + "/"
                    + ERepositoryObjectType.getFolderName(ERepositoryObjectType.COMPONENTS);
            File source = new File(sourcePath);
            if (source.exists()) {
                final File[] listFiles = source.listFiles(ff);
                if (listFiles != null) {
                    for (File file : listFiles) {
                        if (file.isFile() && UpdatesHelper.isComponentUpdateSite(file)) {
                            // TUP-17680, won't support to install the new CF from project
                        } else if (UpdatesHelper.isOldComponent(file)) {
                            FilesUtils.copyFolder(file, new File(installationFolder.getAbsolutePath(), file.getName()), true, ff,
                                    null, true, false);
                        }
                    }
                }
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

    public boolean isCustom() {
        return true;
    }

    public String getComponentsBundle() {
        return IComponentsFactory.COMPONENTS_LOCATION;
    }
    
    public boolean isSupportCurrentMode() {
    	if (SharedStudioUtils.isSharedStudioMode()) {
    		return false;
    	}
    	return true;
    }
}
