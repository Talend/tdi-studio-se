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
package org.talend.repository.ui.actions.importproject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipFile;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.IOverwriteQuery;
import org.eclipse.ui.internal.wizards.datatransfer.ArchiveFileManipulations;
import org.eclipse.ui.internal.wizards.datatransfer.TarException;
import org.eclipse.ui.internal.wizards.datatransfer.TarFile;
import org.eclipse.ui.internal.wizards.datatransfer.TarLeveledStructureProvider;
import org.eclipse.ui.internal.wizards.datatransfer.ZipLeveledStructureProvider;
import org.eclipse.ui.wizards.datatransfer.IImportStructureProvider;
import org.eclipse.ui.wizards.datatransfer.ImportOperation;
import org.osgi.framework.Bundle;
import org.talend.commons.exception.PersistenceException;
import org.talend.core.model.properties.Project;
import org.talend.core.repository.utils.XmiResourceManager;
import org.talend.repository.ProjectManager;
import org.talend.repository.i18n.Messages;
import org.talend.repository.ui.utils.AfterImportProjectUtil;

/**
 * DOC smallet class global comment. Detailled comment <br/>
 * 
 * TODO SML Order methods
 * 
 * $Id$
 * 
 */
public class ImportProjectsUtilities {

    public static void importProjectAs(Shell shell, String newName, String technicalName, String sourcePath,
            IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
        IImportStructureProvider provider = FilterFileSystemStructureProvider.INSTANCE;

        importProject(shell, provider, new File(sourcePath), new Path(technicalName), true, false, monitor);

        Project project = afterImportAs(newName, technicalName);

        // do additional actions after importing projects
        AfterImportProjectUtil.runAfterImportProjectActions(new org.talend.core.model.general.Project(project));
    }

    public static void importProject(String path)
            throws CoreException {
    	IProjectDescription description = ResourcesPlugin.getWorkspace().loadProjectDescription(new Path(path+File.separator+".project"));
    	IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(description.getName());
    	if(!project.isOpen() && !project.isLinked())	{
    		project.create(description, null);
    		project.open(null);
    		project.refreshLocal(IResource.DEPTH_INFINITE, null);
    	}
    }
    
    /**
     * DOC smallet Comment method "afterImportAs".
     * 
     * @param newName
     * @param technicalName
     * @throws InvocationTargetException
     */
    private static Project afterImportAs(String newName, String technicalName) throws InvocationTargetException {
        // Rename in ".project" and "talendProject" or "talend.project"
        // TODO SML Optimize
        final IWorkspace workspace = org.eclipse.core.resources.ResourcesPlugin.getWorkspace();
        IContainer containers = (IProject) workspace.getRoot().findMember(new Path(technicalName));
        IResource file2 = containers.findMember(IProjectDescription.DESCRIPTION_FILE_NAME);
        try {
            replaceInFile("<name>.*</name>", file2.getLocation().toOSString(), "<name>" + technicalName + "</name>"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            // TDI-19269
            final IProject project = workspace.getRoot().getProject(technicalName);
            XmiResourceManager xmiManager = new XmiResourceManager();
            try {
                final Project loadProject = xmiManager.loadProject(project);
                loadProject.setTechnicalLabel(technicalName);
                loadProject.setLabel(newName);
                loadProject.setLocal(true);
                loadProject.setId(0);
                loadProject.setUrl(null);
                loadProject.setCreationDate(null);
                loadProject.setDescription("");
                loadProject.setType(null);
                // ADD xqliu 2012-03-12 TDQ-4771 clear the list of Folders
                if (loadProject.getFolders() != null) {
                    loadProject.getFolders().clear();
                }
                // ~ TDQ-4771
                xmiManager.saveResource(loadProject.eResource());
                return loadProject;
            } catch (PersistenceException e) {
                //
            }
        } catch (IOException e) {
            throw new InvocationTargetException(e);
        }
        return null;
    }

    public static void replaceInFile(String regex, String fileName, String replacement) throws IOException {
        InputStream in = new FileInputStream(fileName);
        StringBuffer buffer = new StringBuffer();
        try {
            Pattern p = Pattern.compile(regex);
            InputStreamReader inR = new InputStreamReader(in);
            BufferedReader buf = new BufferedReader(inR);
            String line;
            while ((line = buf.readLine()) != null) {
                Matcher matcher = p.matcher(line);
                if (matcher.find()) {
                    line = matcher.replaceAll(replacement);
                }
                buffer.append(line).append("\n"); //$NON-NLS-1$
            }
        } catch (IOException e) {
            //
        } finally {
            in.close();
        }

        OutputStream os = new FileOutputStream(fileName);
        os.write(buffer.toString().getBytes());
        os.close();
    }

    public static void importArchiveProjectAs(Shell shell, String newName, String technicalName, String sourcePath,
            IProgressMonitor monitor) throws InvocationTargetException, InterruptedException, TarException, IOException {
        importArchiveProject(shell, technicalName, sourcePath, monitor);

        Project project = afterImportAs(newName, technicalName);

        // do additional actions after importing projects
        AfterImportProjectUtil.runAfterImportProjectActions(new org.talend.core.model.general.Project(project));
    }

    public static void importArchiveProject(Shell shell, String technicalName, String sourcePath, IProgressMonitor monitor)
            throws InvocationTargetException, InterruptedException, TarException, IOException {

        IImportStructureProvider provider;
        Object source;

        if (ArchiveFileManipulations.isZipFile(sourcePath)) {
            ZipLeveledStructureProvider zipProvider = new ZipLeveledStructureProvider(new ZipFile(sourcePath));
            source = zipProvider.getRoot();
            boolean ok = true;
            for (Object o : zipProvider.getChildren(source)) {
                String label = zipProvider.getLabel(o);
                if (!label.equals(IProjectDescription.DESCRIPTION_FILE_NAME) && ok) {
                    source = o;
                } else {
                    ok = false;
                }
            }
            if (!ok) {
                source = zipProvider.getRoot();
            }

            provider = zipProvider;
        } else if (ArchiveFileManipulations.isTarFile(sourcePath)) {
            TarLeveledStructureProvider tarProvider = new TarLeveledStructureProvider(new TarFile(sourcePath));
            source = tarProvider.getRoot();
            boolean ok = true;
            for (Object o : tarProvider.getChildren(source)) {
                String label = tarProvider.getLabel(o);
                if (!label.equals(IProjectDescription.DESCRIPTION_FILE_NAME) && ok) {
                    source = o;
                } else {
                    ok = false;
                }
            }
            if (!ok) {
                source = tarProvider.getRoot();
            }

            provider = tarProvider;
        } else {
            throw new IllegalArgumentException(Messages.getString("ImportProjectsUtilities.fileFormatError", sourcePath)); //$NON-NLS-1$
        }

        importProject(shell, provider, source, new Path(technicalName), false, false, monitor);
    }

    private static void importProject(Shell shell, IImportStructureProvider provider, Object source, IPath path,
            boolean overwriteResources, boolean createContainerStructure, IProgressMonitor monitor)
            throws InvocationTargetException, InterruptedException {
        monitor.beginTask(Messages.getString("ImportProjectsUtilities.task.importingProject"), 100); //$NON-NLS-1$

        ArrayList fileSystemObjects = new ArrayList();
        ImportProjectsUtilities.getFilesForProject(fileSystemObjects, provider, source);

        ImportOperation operation = new ImportOperation(path, source, provider, new MyOverwriteQuery(), fileSystemObjects);
        operation.setContext(shell);
        operation.setOverwriteResources(overwriteResources);
        operation.setCreateContainerStructure(createContainerStructure);
        operation.run(new SubProgressMonitor(monitor, 95));
        monitor.worked(5);
        monitor.done();
    }

    /**
     * Return a list of all files in the project
     * 
     * Method as taken in org.eclipse.ui.internal.wizards.datatransfer.WizardProjectsImportPage.
     * 
     * @param provider The provider for the parent file
     * @param entry The root directory of the project
     * @return A list of all files in the project
     */
    public static void getFilesForProject(Collection files, IImportStructureProvider provider, Object entry) {
        List children = provider.getChildren(entry);
        Iterator childrenEnum = children.iterator();

        while (childrenEnum.hasNext()) {

            Object child = childrenEnum.next();
            // Add the child, this way we get every files except the project
            // folder itself which we don't want
            files.add(child);
            // We don't have isDirectory for tar so must check for children
            // instead
            if (provider.isFolder(child)) {
                getFilesForProject(files, provider, child);
            }
        }
    }

    /**
     * Collect the list of .project files that are under directory into files.
     * 
     * <br/>
     * Method almost as taken in org.eclipse.ui.internal.wizards.datatransfer.WizardProjectsImportPage. Modifications
     * are:
     * <ol>
     * <li>no recusrive search</li>
     * <li>add searchFileName as parameter</li>
     * <li>checks if monitor is null</li>
     * </ol>
     * 
     * @param files
     * @param directory
     * @param monitor The monitor to report to
     * @param searchFileName
     * @return boolean <code>true</code> if the operation was completed.
     */
    public static boolean collectProjectFilesFromDirectory(Collection files, File directory, IProgressMonitor monitor,
            String searchFileName) {
        if (monitor != null && monitor.isCanceled()) {
            return false;
        }
        if (monitor != null) {
            monitor.subTask(Messages.getString("ImportProjectsUtilities.task.checkingFolder", directory.getPath())); //$NON-NLS-1$
        }
        File[] contents = directory.listFiles();
        // first look for project description files
        for (File file : contents) {
            if (file.isFile() && file.getName().equals(searchFileName)) {
                files.add(file);
                // don't search sub-directories since we can't have nested
                // projects
                return true;
            }
        }
        return true;
    }

    /**
     * Collect the list of .project files that are under directory into files.
     * 
     * <br/>
     * Method almost as taken in org.eclipse.ui.internal.wizards.datatransfer.WizardProjectsImportPage. Modifications
     * are:
     * <ol>
     * <li>no recusrive search</li>
     * <li>add searchFileName as parameter</li>
     * <li>checks if monitor is null</li>
     * </ol>
     * 
     * @param files
     * @param monitor The monitor to report to
     * @return boolean <code>true</code> if the operation was completed.
     */
    public static boolean collectProjectFilesFromProvider(Collection files, IImportStructureProvider provider, Object entry,
            int level, IProgressMonitor monitor, String searchFileName) {

        if (monitor != null && monitor.isCanceled()) {
            return false;
        }
        if (monitor != null) {
            monitor.subTask(Messages.getString("ImportProjectsUtilities.task.checkingFolder", provider.getLabel(entry))); //$NON-NLS-1$
        }
        List children = provider.getChildren(entry);
        if (children == null) {
            children = new ArrayList(1);
        }
        Iterator childrenEnum = children.iterator();
        while (childrenEnum.hasNext()) {
            Object child = childrenEnum.next();
            if (level < 1) {
                if (provider.isFolder(child)) {
                    collectProjectFilesFromProvider(files, provider, child, level + 1, monitor, searchFileName);
                }
            }
            String elementLabel = provider.getLabel(child);
            if (elementLabel.equals(searchFileName)) {
                files.add(elementLabel);
            }
        }
        return true;
    }

    /**
     * 
     * DOC smallet ImportDemoProjectAction class global comment. Detailled comment <br/>
     * 
     * $Id: talend.epf 1 2006-09-29 17:06:40 +0000 (ven., 29 sept. 2006) nrousseau $
     * 
     */
    private static class MyOverwriteQuery implements IOverwriteQuery {

        /*
         * (non-Javadoc)
         * 
         * @see org.eclipse.ui.dialogs.IOverwriteQuery#queryOverwrite(java.lang.String )
         */
        @Override
        public String queryOverwrite(String pathString) {
            return pathString;
        }

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
        Bundle bundle = Platform.getBundle(demoProjectBean.getPluginId());
        URL demoURL = FileLocator.find(bundle, new Path(demoProjectBean.getDemoProjectFilePath()), null);
        demoURL = FileLocator.toFileURL(demoURL);
        String filePath = new Path(demoURL.getFile()).toOSString();

        final String newTechName = ProjectManager.getLocalTechnicalProjectName(newProjectName);

        if (EDemoProjectFileType.FOLDER.equals(demoProjectBean.getDemoProjectFileType())) {
            ImportProjectsUtilities.importProjectAs(shell, newProjectName, newTechName, filePath, monitor);
        } else {// type.equalsIgnoreCase("archive")
            ImportProjectsUtilities.importArchiveProjectAs(shell, newProjectName, newTechName, filePath, monitor);

        }
    }
}
