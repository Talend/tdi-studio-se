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
package org.talend.repository.ui.actions.importproject;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.internal.wizards.datatransfer.TarException;
import org.eclipse.ui.wizards.datatransfer.IImportStructureProvider;
import org.osgi.framework.Bundle;
import org.talend.commons.runtime.model.emf.provider.EmfResourcesFactoryReader;
import org.talend.commons.runtime.model.emf.provider.ResourceOption;
import org.talend.repository.ProjectManager;
import org.talend.repository.ui.exception.ImportInvalidObjectException;

/**
 * DOC smallet class global comment. Detailled comment <br/>
 *
 * TODO SML Order methods
 *
 * $Id$
 *
 */
public class ImportProjectsUtilities {

    /**
     *
     * @deprecated use ImportProjectHelper instead
     */
    public static void importProjectAs(Shell shell, String newName, String technicalName, String sourcePath,
            IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
        try {
            try {
                new ImportProjectHelper().importProjectAs(shell, newName, technicalName, sourcePath, false, monitor);
            } catch (ImportInvalidObjectException ex) {
                // Ignore here
            }
        } catch (InvocationTargetException | InterruptedException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void importProject(String path) throws CoreException {
        IProjectDescription description = ResourcesPlugin.getWorkspace().loadProjectDescription(
                new Path(path + File.separator + ".project"));
        IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(description.getName());
        if (!project.isOpen() && !project.isLinked()) {
            project.create(description, null);
            project.open(null);
            project.refreshLocal(IResource.DEPTH_INFINITE, null);
        }
    }

    /**
     *
     * @deprecated use ImportProjectHelper instead
     */
    public static void replaceInFile(String regex, String fileName, String replacement) throws IOException {
        new ImportProjectHelper().replaceInFile(regex, fileName, replacement);
    }

    /**
     *
     * @deprecated use ImportProjectHelper instead
     */
    public static void importArchiveProjectAs(Shell shell, String newName, String technicalName, String sourcePath,
            IProgressMonitor monitor) throws InvocationTargetException, InterruptedException, TarException, IOException {
        try {
            try {
                new ImportProjectHelper().importProjectAs(shell, newName, technicalName, sourcePath, true, monitor);
            } catch (ImportInvalidObjectException ex) {
                // Ignore here
            }

        } catch (InvocationTargetException | InterruptedException | TarException | IOException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    /**
     *
     * @deprecated use ImportProjectHelper instead
     */
    public static void importArchiveProject(Shell shell, String technicalName, String sourcePath, IProgressMonitor monitor)
            throws InvocationTargetException, InterruptedException, TarException, IOException {
        new ImportProjectHelper().importArchiveProject(shell, technicalName, sourcePath, monitor);
    }

    /**
     *
     * @deprecated use ImportProjectHelper instead
     */
    public static void getFilesForProject(Collection files, IImportStructureProvider provider, Object entry) {
        new ImportProjectHelper().getFilesForProject(files, provider, entry);
    }

    /**
     *
     * @deprecated use ImportProjectHelper instead
     */
    public static boolean collectProjectFilesFromDirectory(Collection files, File directory, IProgressMonitor monitor,
            String searchFileName) {
        return new ImportProjectHelper().collectProjectFilesFromDirectory(files, directory, monitor, searchFileName);
    }

    /**
     *
     * @deprecated use ImportProjectHelper instead
     */
    public static boolean collectProjectFilesFromProvider(Collection files, IImportStructureProvider provider, Object entry,
            int level, IProgressMonitor monitor, String searchFileName) {
        return new ImportProjectHelper().collectProjectFilesFromProvider(files, provider, entry, level, monitor, searchFileName);
    }

    /**
     * Gets all demo projects information.
     *
     * @return a list of <code>DemoProjectBean</code>
     */
    public static List<DemoProjectBean> getAllDemoProjects() {
        return Arrays.asList(DemoProjectsProvider.getInstance().getDemoProjects());
    }

    public static void importDemoProject(Shell shell, String newProjectName, DemoProjectBean demoProjectBean,
            IProgressMonitor monitor) throws Exception {
        if (demoProjectBean == null || newProjectName == null) {
            return;
        }
        ImportProjectHelper helper = new ImportProjectHelper();

        Bundle bundle = Platform.getBundle(demoProjectBean.getPluginId());
        URL demoURL = FileLocator.find(bundle, new Path(demoProjectBean.getDemoProjectFilePath()), null);
        demoURL = FileLocator.toFileURL(demoURL);
        String filePath = new Path(demoURL.getFile()).toOSString();

        final String newTechName = ProjectManager.getLocalTechnicalProjectName(newProjectName);

        ResourceOption demoImportOption = ResourceOption.DEMO_IMPORTATION;
        try {
            EmfResourcesFactoryReader.INSTANCE.addOption(demoImportOption, false);

            boolean isFolder = EDemoProjectFileType.FOLDER.equals(demoProjectBean.getDemoProjectFileType());
            helper.importProjectAs(shell, newProjectName, newTechName, filePath, !isFolder, monitor);
        } finally {
            EmfResourcesFactoryReader.INSTANCE.removOption(demoImportOption, false);
        }
    }

    public static void importProjectAs(Shell shell, String newName, String technicalName, String sourcePath, boolean isArchive,
            IProgressMonitor monitor) throws Exception {
        final ResourceOption importOption = ResourceOption.ITEM_IMPORTATION;
        try {
            EmfResourcesFactoryReader.INSTANCE.addOption(importOption, false);

            ImportProjectHelper helper = new ImportProjectHelper();
            helper.importProjectAs(shell, newName, technicalName, sourcePath, isArchive, monitor);
        } finally {
            EmfResourcesFactoryReader.INSTANCE.removOption(importOption, false);
        }
    }
}
