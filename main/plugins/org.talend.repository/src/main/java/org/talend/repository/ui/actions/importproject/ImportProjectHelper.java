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

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
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
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.IOverwriteQuery;
import org.eclipse.ui.internal.wizards.datatransfer.ArchiveFileManipulations;
import org.eclipse.ui.internal.wizards.datatransfer.ILeveledImportStructureProvider;
import org.eclipse.ui.internal.wizards.datatransfer.TarException;
import org.eclipse.ui.internal.wizards.datatransfer.TarFile;
import org.eclipse.ui.internal.wizards.datatransfer.TarLeveledStructureProvider;
import org.eclipse.ui.internal.wizards.datatransfer.ZipLeveledStructureProvider;
import org.eclipse.ui.wizards.datatransfer.IImportStructureProvider;
import org.eclipse.ui.wizards.datatransfer.ImportOperation;
import org.talend.commons.exception.ExceptionHandler;
import org.talend.commons.exception.PersistenceException;
import org.talend.core.model.properties.Project;
import org.talend.core.model.properties.PropertiesPackage;
import org.talend.core.model.properties.Property;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.model.repository.IRepositoryViewObject;
import org.talend.core.repository.constants.FileConstants;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.core.repository.utils.TalendResourceSet;
import org.talend.core.repository.utils.URIHelper;
import org.talend.core.repository.utils.XmiResourceManager;
import org.talend.core.runtime.repository.item.ItemProductValuesHelper;
import org.talend.repository.i18n.Messages;
import org.talend.repository.items.importexport.ui.managers.TalendZipLeveledStructureProvider;
import org.talend.repository.ui.utils.AfterImportProjectUtil;

/**
 * DOC ggu class global comment. Detailled comment
 */
@SuppressWarnings({ "restriction", "rawtypes", "unchecked" })
public class ImportProjectHelper {

    public static final String IMPORT_FLAG_FILE = ".import";//$NON-NLS-1$

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

    public void importProjectAs(Shell shell, String newName, String technicalName, String sourcePath, boolean isArchive,
            IProgressMonitor monitor) throws Exception {
        if (isArchive) {
            importArchiveProjectAs(shell, newName, technicalName, sourcePath, monitor);
        } else {
            importProjectAs(shell, newName, technicalName, sourcePath, monitor);
        }
    }

    protected void importProjectAs(Shell shell, String newName, String technicalName, String sourcePath, IProgressMonitor monitor)
            throws Exception {
        IImportStructureProvider provider = FilterFileSystemStructureProvider.INSTANCE;

        importProject(shell, provider, new File(sourcePath), new Path(technicalName), true, false, monitor);

        ImportProjectBean projectBean = afterImportAs(newName, technicalName);

        // do additional actions after importing projects
        AfterImportProjectUtil.runAfterImportProjectActions(projectBean);
    }

    protected ImportProjectBean afterImportAs(String newName, String technicalName) throws InvocationTargetException {
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
                Project oldProject = EcoreUtil.copy(loadProject);
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

                checkProjectItems(new org.talend.core.model.general.Project(loadProject));

                return new ImportProjectBean(loadProject, oldProject);
            } catch (PersistenceException e) {
                ExceptionHandler.process(e);
            }
        } catch (IOException e) {
            throw new InvocationTargetException(e);
        }
        return null;
    }

    protected void replaceInFile(String regex, String fileName, String replacement) throws IOException {
        BufferedReader reader = null;
        StringBuffer buffer = new StringBuffer();
        try {
            Pattern p = Pattern.compile(regex);
            reader = new BufferedReader(new InputStreamReader(new FileInputStream(fileName)));
            String line;
            while ((line = reader.readLine()) != null) {
                Matcher matcher = p.matcher(line);
                if (matcher.find()) {
                    line = matcher.replaceAll(replacement);
                }
                buffer.append(line).append("\n"); //$NON-NLS-1$
            }
        } catch (IOException e) {
            ExceptionHandler.process(e);
        } finally {
            if (reader != null)
                reader.close();
        }
        StringBuffer sb = new StringBuffer();
        String temFilename = sb.append(fileName).insert(fileName.lastIndexOf("."), "temp").toString();
        File oldfile = new File(fileName);
        oldfile.delete();
        OutputStream os = new FileOutputStream(temFilename);
        os.write(buffer.toString().getBytes());
        os.close();
        File newfile = new File(temFilename);
        newfile.renameTo(new File(fileName));
    }

    protected void importArchiveProjectAs(Shell shell, String newName, String technicalName, String sourcePath,
            IProgressMonitor monitor) throws Exception {
        importArchiveProject(shell, technicalName, sourcePath, monitor);

        ImportProjectBean projectBean = afterImportAs(newName, technicalName);

        // do additional actions after importing projects
        AfterImportProjectUtil.runAfterImportProjectActions(projectBean);
    }

    protected void importArchiveProject(Shell shell, String technicalName, String sourcePath, IProgressMonitor monitor)
            throws InvocationTargetException, InterruptedException, TarException, IOException {

        IImportStructureProvider provider;
        Object source;

        if (ArchiveFileManipulations.isZipFile(sourcePath)) {
            TalendZipLeveledStructureProvider zipProvider = new TalendZipLeveledStructureProvider(new ZipFile(sourcePath));
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

    protected void importProject(Shell shell, IImportStructureProvider provider, Object source, IPath path,
            boolean overwriteResources, boolean createContainerStructure, IProgressMonitor monitor)
            throws InvocationTargetException, InterruptedException {
        monitor.beginTask(Messages.getString("ImportProjectsUtilities.task.importingProject"), 100); //$NON-NLS-1$

        ArrayList fileSystemObjects = new ArrayList();
        getFilesForProject(fileSystemObjects, provider, source);

        boolean exists = Platform.getLocation().append(path).toFile().exists();
        IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(path.toOSString());
        if(project.exists() && !exists) {
        	try {
        		project.delete(true, true, new NullProgressMonitor());
			} catch (CoreException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			};
        }

        ImportOperation operation = new ImportOperation(path, source, provider, new MyOverwriteQuery(), fileSystemObjects);
        operation.setContext(shell);
        operation.setOverwriteResources(overwriteResources);
        operation.setCreateContainerStructure(createContainerStructure);
        operation.run(new SubProgressMonitor(monitor, 95));
        monitor.worked(5);
        monitor.done();
        if (provider instanceof ILeveledImportStructureProvider) {
            ((ILeveledImportStructureProvider) provider).closeArchive();
        }
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
    protected void getFilesForProject(Collection files, IImportStructureProvider provider, Object entry) {
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
    public boolean collectProjectFilesFromDirectory(Collection files, File directory, IProgressMonitor monitor,
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
    public boolean collectProjectFilesFromProvider(Collection files, IImportStructureProvider provider, Object entry, int level,
            IProgressMonitor monitor, String searchFileName) {

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
                if (provider.isFolder(child) && !".svnlog".equals(provider.getLabel(child))) { // $NON-NLS-1$
                    collectProjectFilesFromProvider(files, provider, child, level + 1, monitor, searchFileName);
                }
            }
            String elementLabel = provider.getLabel(child);
            if (elementLabel.equals(searchFileName)) {
                files.add(child);
            }
        }
        return true;
    }

    public Project retrieveProjectFilesFromProvider(IProgressMonitor monitor, File file) {
        List projectFiles = new ArrayList();
        List talendProjectFiles = new ArrayList();
        ILeveledImportStructureProvider provider = null;
        try {
            if (file.isDirectory()) {
                provider = new DirImportStructureProvider(file);
            } else if (file.isFile()) {
                if (ArchiveFileManipulations.isTarFile(file.getAbsolutePath())) {
                    provider = new TarLeveledStructureProvider(new TarFile(file));
                } else if (ArchiveFileManipulations.isZipFile(file.getAbsolutePath())) {
                    provider = new ZipLeveledStructureProvider(new ZipFile(file));
                }
            }

            if (monitor != null && monitor.isCanceled()) {
                throw new OperationCanceledException();// cancel
            }
            if (provider != null) {
                Object child = provider.getRoot();
                if (!collectProjectFilesFromProvider(projectFiles, provider, child, 0, monitor,
                        IProjectDescription.DESCRIPTION_FILE_NAME)) {
                    throw new OperationCanceledException();// cancel
                }
                if (!collectProjectFilesFromProvider(talendProjectFiles, provider, child, 0, monitor,
                        FileConstants.LOCAL_PROJECT_FILENAME)) {
                    throw new OperationCanceledException();// cancel
                }

                if (projectFiles.size() == 1 && talendProjectFiles.size() == 1) {
                    if (monitor != null && monitor.isCanceled()) {
                        throw new OperationCanceledException();// cancel
                    }

                    InputStream contents = provider.getContents(talendProjectFiles.get(0));
                    if (contents != null) {
                        BufferedOutputStream bos = null;
                        try {
                            URI uri = URIHelper.convert(new Path(String.valueOf(System.currentTimeMillis()))
                                    .append(FileConstants.LOCAL_PROJECT_FILENAME));
                            Resource resource = new TalendResourceSet().createResource(uri);
                            resource.load(contents, null);
                            Project emfProject = (Project) EcoreUtil.getObjectByType(resource.getContents(),
                                    PropertiesPackage.eINSTANCE.getProject());
                            if (emfProject != null) {
                                return emfProject;
                            }
                        } finally {
                            if (bos != null) {
                                try {
                                    bos.close();
                                } catch (IOException e1) {
                                    //
                                }
                            }
                            if (contents != null) {
                                try {
                                    contents.close();
                                } catch (IOException e1) {
                                    //
                                }
                            }
                        }
                    }
                }
            }
        } catch (IOException | TarException e) {
            ExceptionHandler.process(e);
        } finally {
            if (provider != null) {
                provider.closeArchive();
            }
        }
        return null;
    }

    public void checkProjectItems(org.talend.core.model.general.Project p) {
        // only use for local
        ProxyRepositoryFactory factory = ProxyRepositoryFactory.getInstance();

        try {
            if (!factory.isLocalConnectionProvider()) {
                return;
            }
        } catch (PersistenceException e1) {
            //
        }
        Map<String, String> productValues = ItemProductValuesHelper.getProductValues(p.getEmfProject());
        if (productValues.isEmpty()) {
            return;
        }
        final String datetime = ItemProductValuesHelper.getCurDateTime();
        final String fullname = productValues.keySet().iterator().next();
        final String version = productValues.get(fullname);

        try {
            for (ERepositoryObjectType curTyp : (ERepositoryObjectType[]) ERepositoryObjectType.values()) {
                if (curTyp != null && curTyp.isResourceItem()) {
                    List<IRepositoryViewObject> all = factory.getAll(p, curTyp, true);
                    for (IRepositoryViewObject obj : all) {
                        // try to update all versions
                        List<IRepositoryViewObject> allVersion = factory.getAllVersion(p, obj.getId(), true);
                        for (IRepositoryViewObject one : allVersion) {
                            Property property = one.getProperty();
                            // read all items, when import file existed
                            boolean set = ItemProductValuesHelper.setImportValues(property, fullname, version, datetime);
                            if (set) {
                                factory.save(p, property);
                            }
                        }
                    }
                }
            }
        } catch (PersistenceException e) {
            ExceptionHandler.process(e);
        }
    }
}
