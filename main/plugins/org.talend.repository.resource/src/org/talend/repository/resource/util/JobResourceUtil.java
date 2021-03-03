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
package org.talend.repository.resource.util;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Path;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.talend.core.model.general.Project;
import org.talend.core.model.resources.ResourceItem;
import org.talend.repository.ProjectManager;

public class JobResourceUtil {

    /**
     * Get source file of Item.
     *
     * @param item
     * @return
     */
    public static IFile getSourceFile(ResourceItem item) {
        // the file may come from a reference project
        IFolder rrfolder = null;
        Resource eResource = item.eResource();
        if (eResource != null) {
            URI uri = eResource.getURI();
            if (uri != null && uri.isPlatformResource()) {
                String platformString = uri.toPlatformString(true);
                IContainer parentContainer = ResourcesPlugin.getWorkspace().getRoot().getFile(new Path(platformString))
                        .getParent();
                if (parentContainer instanceof IFolder) {
                    rrfolder = (IFolder) parentContainer;
                }
            }
        }
        if (rrfolder == null) {
            Project talendProject = ProjectManager.getInstance().getCurrentProject();
            String technicalLabel = talendProject.getTechnicalLabel();

            IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(technicalLabel);
            String folderPath = item.getState().getPath();
            rrfolder = project.getFolder("resources");
            if (folderPath != null && !folderPath.isEmpty()) {
                rrfolder = rrfolder.getFolder(folderPath);
            }
        }
        String itemName = item.getProperty().getLabel();
        String version = item.getProperty().getVersion();

        String fileExtension = item.getBindingExtension();
        String fileName = itemName + "_" + version + "." + fileExtension;
        IFile file = rrfolder.getFile(fileName);

        return file;
    }

}
