// ============================================================================
//
// Copyright (C) 2006-2019 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.repository.local;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.talend.commons.CommonsPlugin;
import org.talend.commons.exception.ExceptionHandler;
import org.talend.commons.exception.PersistenceException;
import org.talend.commons.runtime.model.emf.EmfHelper;
import org.talend.commons.utils.generation.JavaUtils;
import org.talend.commons.utils.io.FilesUtils;
import org.talend.commons.utils.time.TimeMeasure;
import org.talend.core.CorePlugin;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.ILibraryManagerService;
import org.talend.core.model.context.link.ContextLinkService;
import org.talend.core.model.properties.Item;
import org.talend.core.model.properties.Project;
import org.talend.core.model.properties.PropertiesPackage;
import org.talend.core.model.properties.Property;
import org.talend.core.model.properties.RoutineItem;
import org.talend.core.model.properties.RoutinesJarItem;
import org.talend.core.model.repository.FakePropertyImpl;
import org.talend.core.model.repository.IRepositoryViewObject;
import org.talend.core.model.repository.ResourceModelUtils;
import org.talend.core.repository.constants.FileConstants;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.core.repository.utils.URIHelper;
import org.talend.core.repository.utils.XmiResourceManager;
import org.talend.core.service.ITransformService;
import org.talend.core.ui.ITestContainerProviderService;
import org.talend.core.ui.export.IFileExporterFullPath;
import org.talend.core.ui.export.TarFileExporterFullPath;
import org.talend.core.ui.export.ZipFileExporterFullPath;
import org.talend.designer.core.model.utils.emf.component.impl.IMPORTTypeImpl;
import org.talend.repository.ProjectManager;
import org.talend.repository.i18n.Messages;

/***/
public class ExportItemUtil {

    private Project project;

    private ProjectManager pManager = ProjectManager.getInstance();

    private boolean projectNameLowerCase;

    Map<IPath, Resource> projectResourcMap = new HashMap<IPath, Resource>();

    public ExportItemUtil() {
        project = pManager.getCurrentProject().getEmfProject();
    }

    public ExportItemUtil(Project project) {
        this.project = project;
    }

    public void setProjectNameAsLowerCase(boolean projectNameLowerCase) {
        this.projectNameLowerCase = projectNameLowerCase;
    }

    private IFileExporterFullPath exporter = null;

    public void exportItems(File destination, final Collection<Item> items, List<String> folders, boolean exportAllVersions,
            IProgressMonitor progressMonitor) throws Exception {

        // bug 11301 :export 0 items
        Collection<Item> workItems = items;
        if (workItems == null) {
            workItems = new ArrayList<Item>();
        }

        Collection<Item> otherVersions = new ArrayList<Item>();
        // get all versions of the exported items if wanted
        if (exportAllVersions) {
            otherVersions = getOtherVersions(workItems);
            workItems.addAll(otherVersions);
            otherVersions.clear();
        }// else keep current items version only
        try {

            File tmpDirectory = null;
            Map<File, IPath> toExport;

            // TDI-27660:if not give the path of export file,get its default parent path.
            File parentDesFile = checkAndGetDesParentFile(destination);

            if (destination.getName().endsWith(FileConstants.TAR_FILE_SUFFIX)) {
                createFolder(parentDesFile);
                exporter = new TarFileExporterFullPath(destination.getAbsolutePath(), false);
            } else if (destination.getName().endsWith(FileConstants.TAR_GZ_FILE_SUFFIX)) {
                createFolder(parentDesFile);
                exporter = new TarFileExporterFullPath(destination.getAbsolutePath(), true);
            } else if (destination.getName().endsWith(FileConstants.ZIP_FILE_SUFFIX)) {
                createFolder(parentDesFile);
                exporter = new ZipFileExporterFullPath(destination.getAbsolutePath(), true);
            } else {
                createFolder(destination);
            }

            if (exporter != null) {
                tmpDirectory = createTmpDirectory();
            }

            try {
                if (exporter != null) {
                    toExport = exportItems2(workItems, tmpDirectory, true, progressMonitor);

                    // in case of .tar.gz we remove extension twice
                    // IPath rootPath = new Path(destination.getName()).removeFileExtension().removeFileExtension();
                    for (File file : toExport.keySet()) {
                        IPath path = toExport.get(file);
                        // exporter.write(file.getAbsolutePath(), rootPath.append(path).toString());
                        exporter.write(file.getAbsolutePath(), path.toString());
                    }
                    if (folders != null) {
                        // filter folders that already created
                        List<String> toRemove = new ArrayList<String>();
                        for (String folderPath : folders) {
                            for (IPath resourcePath : toExport.values()) {
                                if (resourcePath.toPortableString().contains(folderPath)) {
                                    toRemove.add(folderPath);
                                    break;
                                }
                            }
                        }
                        folders.removeAll(toRemove);
                        for (String folderPath : folders) {
                            exporter.writeFolder(folderPath);
                        }
                    }
                } else {
                    toExport = exportItems2(workItems, destination, true, progressMonitor);
                    if (folders != null) {
                        // filter folders that already created
                        List<String> toRemove = new ArrayList<String>();
                        for (String folderPath : folders) {
                            for (IPath resourcePath : toExport.values()) {
                                if (resourcePath.toPortableString().contains(folderPath)) {
                                    toRemove.add(folderPath);
                                    break;
                                }
                            }
                        }
                        folders.removeAll(toRemove);
                        for (String folderPath : folders) {
                            IPath fullPath = new Path(destination.getAbsolutePath());
                            fullPath = fullPath.append(folderPath);
                            FilesUtils.createFoldersIfNotExists(fullPath.toPortableString(), false);
                        }
                    }

                }
            } catch (Exception e) {
                throw e;
            } finally {
                if (exporter != null) {
                    deleteTmpDirectory(tmpDirectory);
                }
            }
        } finally {
            if (exporter != null) {
                try {
                    exporter.finished();
                } catch (Exception e) {
                    ExceptionHandler.process(e);
                }
            }

        }

    }

    /**
     * export the sected TOS model elements to the destination
     *
     * @param destination zip file or folder to store the exported elements
     * @param items, the items to be exported
     * @param exportAllVersions whether all the versions are export or only the selected once
     * @param progressMonitor, to show the progress during export
     * @throws Exception in case of problem
     */
    public void exportItems(File destination, final Collection<Item> items, boolean exportAllVersions,
            IProgressMonitor progressMonitor) throws Exception {
        exportItems(destination, items, null, exportAllVersions, progressMonitor);
    }

    /**
     * return a collection of items that have the same id as the input items params and a different version. <br>
     * WARNING : when calling this method the global TOS model will be updated with all the items versions
     *
     * @param items all the items to get the different version of.
     * @return list of all the items with same id as input items and different versions
     * @throws PersistenceException if could not load some items //MOD sgandon 31/03/2010 bug 12229: changed
     * getAllVersions into getOtherVersions.
     */
    private Collection<Item> getOtherVersions(Collection<Item> items) throws PersistenceException {
        Collection<Item> itemsVersions = new ArrayList<Item>();
        for (Item item : items) {
            org.talend.core.model.general.Project itemProject = new org.talend.core.model.general.Project(
                    pManager.getProject(item));
            List<IRepositoryViewObject> allVersion = ProxyRepositoryFactory.getInstance().getAllVersion(itemProject,
                    item.getProperty().getId(), false);
            for (IRepositoryViewObject repositoryObject : allVersion) {
                Item anyVersionItem = repositoryObject.getProperty().getItem();
                if (!anyVersionItem.equals(item)) {
                    itemsVersions.add(anyVersionItem);
                }// else same item so ignor it
            }
        }
        return itemsVersions;
    }

    public Set<File> createLocalResources(File destinationDirectory, Item item) throws Exception {
        List<Item> items = new ArrayList<Item>();
        items.add(item);

        Map<File, IPath> exportItems = exportItems2(items, destinationDirectory, false, new NullProgressMonitor());

        return exportItems.keySet();
    }

    /**
     * DOC chuang Comment method "sortItemsByProject".
     *
     * @param items
     * @param itemProjectMap
     * @return
     */
    private Collection<Item> sortItemsByProject(Collection<Item> items, Map<Item, Project> itemProjectMap) {
        Map<Project, List<Item>> projectItems = new HashMap<Project, List<Item>>();
        for (Item item : items) {
            // get project corresponding to item
            Project p = pManager.getProject(item);
            // store for further lookup
            itemProjectMap.put(item, p);

            // items in the same list belongs to same project
            List<Item> list = projectItems.get(p);
            if (list == null) {
                list = new ArrayList<Item>();
                projectItems.put(p, list);
            }
            list.add(item);
        }
        // merge items from different projects
        Collection<Item> workItems = new ArrayList<Item>(items.size());
        for (List<Item> list : projectItems.values()) {
            workItems.addAll(list);
        }
        return workItems;
    }

    private Map<File, IPath> exportItems2(Collection<Item> items, File destinationDirectory, boolean projectFolderStructure,
            IProgressMonitor progressMonitor) throws Exception {
        Map<File, IPath> toExport = new HashMap<File, IPath>();

        progressMonitor.beginTask("Export Items", items.size() + 1); //$NON-NLS-1$

        final boolean oldMeasureActived = TimeMeasure.measureActive;
        if (!oldMeasureActived) { // not active before.
            TimeMeasure.display = TimeMeasure.displaySteps = TimeMeasure.measureActive = CommonsPlugin.isDebugMode();
        }

        final String idTimer = "exportItems"; //$NON-NLS-1$
        TimeMeasure.begin(idTimer);
        try {

            // store item and its corresponding project
            Map<Item, Project> itemProjectMap = new HashMap<Item, Project>();

            Collection<Item> allItems = new ArrayList<Item>(items);
            items.clear();

            // ycbai added for TDI-21387
            if (allItems.isEmpty()) {
                addTalendProjectFile(toExport, destinationDirectory);
                return toExport;
            }

            allItems = sortItemsByProject(allItems, itemProjectMap);

            ITransformService tdmService = null;
            if (GlobalServiceRegister.getDefault().isServiceRegistered(ITransformService.class)) {
                tdmService = (ITransformService) GlobalServiceRegister.getDefault().getService(ITransformService.class);
            }
            itemProjectMap.clear();
            Map<String,String> jarNameAndUrl = new HashMap<String,String>();
            Iterator<Item> iterator = allItems.iterator();
            Set<String> projectHasTdm = new HashSet<String>();
            while (iterator.hasNext()) {
                Item item = iterator.next();

                project = pManager.getProject(item);

                String label = item.getProperty().getLabel();
                // project
                addTalendProjectFile(toExport, destinationDirectory);
                // Item context link file
                addItemContextLinkFile(item.getProperty().getId(), toExport, destinationDirectory);

                // tdm .settings/com.oaklandsw.base.projectProps
                String technicalLabel = project.getTechnicalLabel();

                if (tdmService != null && !projectHasTdm.contains(technicalLabel) && tdmService.isTransformItem(item)) {
                    projectHasTdm.add(technicalLabel);
                    IPath propsSourcePath = getProjectLocationPath(technicalLabel).append(FileConstants.TDM_PROPS_PATH);
                    IPath tdmPropsPath = getProjectOutputPath().append(FileConstants.TDM_PROPS_PATH);
                    IPath propsTargetPath = new Path(destinationDirectory.getAbsolutePath()).append(tdmPropsPath);
                    File source = new File(propsSourcePath.toPortableString());
                    if (source.exists()) {
                        copyAndAddResource(toExport, propsSourcePath, propsTargetPath, tdmPropsPath);
                    }

                }
                // tdm simple files
                if (item.getProperty() instanceof FakePropertyImpl) {
                    FakePropertyImpl fakeProperty = (FakePropertyImpl) item.getProperty();
                    IPath itemResPath = fakeProperty.getItemPath().makeRelative();
                    IPath itemSourcePath = getProjectLocationPath(technicalLabel).removeLastSegments(1).append(itemResPath);
                    // replace the project segment
                    IPath outputRelativeItemPath = getProjectOutputPath().append(itemResPath.removeFirstSegments(1));
                    IPath itemTargetPath = new Path(destinationDirectory.getAbsolutePath()).append(outputRelativeItemPath);
                    copyAndAddResource(toExport, itemSourcePath, itemTargetPath, outputRelativeItemPath);
                    continue;
                }

                // property and related resources eg:item, reference files
                XmiResourceManager localRepositoryManager = ProxyRepositoryFactory.getInstance()
                        .getRepositoryFactoryFromProvider().getResourceManager();
                IPath propertyPath = null;
                Property property = item.getProperty();
                List<Resource> resources = localRepositoryManager.getAffectedResources(property);
                if (resources.isEmpty()) {
                    IRepositoryViewObject obj = ProxyRepositoryFactory.getInstance().getSpecificVersion(property.getId(),
                            property.getVersion(), true);
                    resources = localRepositoryManager.getAffectedResources(obj.getProperty());
                }
                for (Resource curResource : resources) {
                    URI uri = curResource.getURI();
                    IPath relativeItemPath = URIHelper.convert(uri).makeRelative();
                    Project project = ProjectManager.getInstance().getProject(item);
                    IPath sourcePath = getProjectLocationPath(project.getTechnicalLabel()).removeLastSegments(1).append(
                            relativeItemPath);
                    // replace the project segment
                    IPath outputRelativeItemPath = getProjectOutputPath().append(relativeItemPath.removeFirstSegments(1));
                    IPath targetPath = new Path(destinationDirectory.getAbsolutePath()).append(outputRelativeItemPath);
                    copyAndAddResource(toExport, sourcePath, targetPath, outputRelativeItemPath);
                    if (uri.lastSegment() != null && uri.lastSegment().endsWith(FileConstants.PROPERTIES_FILE_SUFFIX)) {
                        propertyPath = targetPath;
                    }
                }

                if (GlobalServiceRegister.getDefault().isServiceRegistered(ITestContainerProviderService.class)) {
                    ITestContainerProviderService testContainerService = (ITestContainerProviderService) GlobalServiceRegister
                            .getDefault().getService(ITestContainerProviderService.class);
                    if (testContainerService != null) {
                        List<IResource> dataFileList = testContainerService.getDataFiles(item);
                        for (IResource dataFile : dataFileList) {
                            IPath relativeItemPath = dataFile.getFullPath();
                            IPath sourcePath = getProjectLocationPath(project.getTechnicalLabel()).removeLastSegments(1).append(
                                    relativeItemPath);
                            // replace the project segment
                            IPath outputRelativeItemPath = getProjectOutputPath().append(relativeItemPath.removeFirstSegments(1));
                            IPath targetPath = new Path(destinationDirectory.getAbsolutePath()).append(outputRelativeItemPath);
                            copyAndAddResource(toExport, sourcePath, targetPath, outputRelativeItemPath);
                        }
                    }
                }

                if (propertyPath == null) {
                    return toExport;
                }

                List moduleList = null;
                if (item instanceof RoutineItem) {
                    moduleList = ((RoutineItem) item).getImports();
                } else if (item instanceof RoutinesJarItem) {
                    moduleList = ((RoutinesJarItem) item).getRoutinesJarType().getImports();
                }
                if (moduleList != null) {
                    for (int i = 0; i < moduleList.size(); i++) {
                        String jarName = ((IMPORTTypeImpl) moduleList.get(i)).getMODULE();
                        jarNameAndUrl.put(jarName, ((IMPORTTypeImpl) moduleList.get(i)).getMVN());
                    }
                }

                boolean needChangeItem = false;
                needChangeItem = needChangeItem || item.getState().isLocked();
                // keep the same as function fixItem()
                needChangeItem = needChangeItem
                        || !item.getProperty().getLabel().replace(' ', '_').equals(item.getProperty().getLabel());
                if (needChangeItem) {
                    // load in memory, fix the item and save it
                    XmiResourceManager xmiMamanger = new XmiResourceManager();

                    // loadProject
                    IPath proRelativePath = getProjectOutputPath().append(FileConstants.LOCAL_PROJECT_FILENAME);
                    IPath proTargetPath = new Path(destinationDirectory.getAbsolutePath()).append(proRelativePath);
                    Resource loadProject = projectResourcMap.get(proTargetPath);
                    if (loadProject == null) {
                        URI projectUri = URI.createFileURI(proTargetPath.toPortableString());
                        loadProject = xmiMamanger.resourceSet.getResource(projectUri, true);
                        projectResourcMap.put(proTargetPath, loadProject);
                    }
                    URI propertyUri = URI.createFileURI(propertyPath.toPortableString());
                    Resource propertyResource = xmiMamanger.resourceSet.getResource(propertyUri, true);
                    Property loadProperty = (Property) EcoreUtil.getObjectByType(propertyResource.getContents(),
                            PropertiesPackage.eINSTANCE.getProperty());
                    Item newItem = loadProperty.getItem();
                    fixItem(newItem);
                    fixItemLockState(newItem);
                    saveResources(xmiMamanger.resourceSet);
                }

                iterator.remove();
                TimeMeasure.step(idTimer, "export item: " + label);
                progressMonitor.worked(1);
            }

            ILibraryManagerService repositoryBundleService = CorePlugin.getDefault().getRepositoryBundleService();

            // add the routines of the jars at the end, to add them only once in the export.
            IPath libPath = getProjectOutputPath().append(JavaUtils.JAVA_LIB_DIRECTORY);
            String libAbsPath = new Path(destinationDirectory.toString()).append(libPath.toString()).toPortableString();
            for (String jarName : jarNameAndUrl.keySet()) {
                if (repositoryBundleService.contains(jarName)) {
                	String mavenUri =  jarNameAndUrl.get(jarName);
                    repositoryBundleService.retrieve(jarName, mavenUri, libAbsPath, new NullProgressMonitor());
                    toExport.put(new File(libAbsPath, jarName), libPath.append(jarName));
                }
            }

        } catch (Exception e) {
            ExceptionHandler.process(e);
        }

        finally {
            TimeMeasure.end(idTimer);
            // if active before, not disable and active still.
            if (!oldMeasureActived) {
                TimeMeasure.display = TimeMeasure.displaySteps = TimeMeasure.measureActive = false;
            }
        }

        return toExport;
    }

    private void addTalendProjectFile(Map<File, IPath> toExport, File destinationDirectory) throws IOException {
        IPath proSourcePath = getProjectLocationPath(project.getTechnicalLabel()).append(FileConstants.LOCAL_PROJECT_FILENAME);
        IPath proRelativePath = getProjectOutputPath().append(FileConstants.LOCAL_PROJECT_FILENAME);
        IPath proTargetPath = new Path(destinationDirectory.getAbsolutePath()).append(proRelativePath);

        copyAndAddResource(toExport, proSourcePath, proTargetPath, proRelativePath);

        IPath proSettingFolderPath = this.getProjectSettingLocationPath(project.getTechnicalLabel());
        File proSettingFolder = new File(proSettingFolderPath.toPortableString());
        if (proSettingFolder.exists()) {
            IPath proSettingSourcePath = proSettingFolderPath.append(File.separator).append(FileConstants.PROJECTSETTING_FILE_NAME);
            File proSettingSourcefile = new File(proSettingSourcePath.toPortableString());
            if (proSettingSourcefile.exists()) {
                IPath projectSettingFolderPath = getProjectSettingFolderPath();

                IPath proSettingRelativePath = projectSettingFolderPath.append(File.separator)
                        .append(FileConstants.PROJECTSETTING_FILE_NAME);
                IPath proSettingTargetPath = new Path(destinationDirectory.getAbsolutePath()).append(proSettingRelativePath);
                File projectSetttingTargetFolder = proSettingTargetPath.toFile().getParentFile();
                if (!projectSetttingTargetFolder.exists()) {
                    projectSetttingTargetFolder.mkdirs();
                }
                copyAndAddResource(toExport, proSettingSourcePath, proSettingTargetPath, proSettingRelativePath);

                IPath proRelationShipSourcePath = proSettingFolderPath.append(File.separator).append(FileConstants.RELATIONSHIP_FILE_NAME);
                File proRelationShipSourceFile = new File(proRelationShipSourcePath.toPortableString());
                if (proRelationShipSourceFile.exists()) {
                    IPath proRelationShipRelativePath = projectSettingFolderPath.append(File.separator).append(FileConstants.RELATIONSHIP_FILE_NAME);
                    IPath proRelationShipTargetPath = new Path(destinationDirectory.getAbsolutePath()).append(proRelationShipRelativePath);
                    copyAndAddResource(toExport, proRelationShipSourcePath, proRelationShipTargetPath, proRelationShipRelativePath);
                }

                IPath proMigrationTaskSourcePath = proSettingFolderPath.append(File.separator).append(FileConstants.MIGRATION_TASK_FILE_NAME);
                File proMigrationTaskSourceFile = new File(proMigrationTaskSourcePath.toPortableString());
                if (proMigrationTaskSourceFile.exists()) {
                    IPath proMigrationTaskRelativePath = projectSettingFolderPath.append(File.separator)
                            .append(FileConstants.MIGRATION_TASK_FILE_NAME);
                    IPath proMigrationTargetPath = new Path(destinationDirectory.getAbsolutePath())
                            .append(proMigrationTaskRelativePath);
                    copyAndAddResource(toExport, proMigrationTaskSourcePath, proMigrationTargetPath, proMigrationTaskRelativePath);
                }
            }
        }
    }

    private void addItemContextLinkFile(String itemId, Map<File, IPath> toExport, File destinationDirectory) throws IOException {
        IPath proSourcePath = getProjectLocationPath(project.getTechnicalLabel());
        IPath itemLinkRelatedPath = getProjectSettingFolderPath().append(File.separator)
                .append(ContextLinkService.LINKS_FOLDER_NAME).append(File.separator)
                .append(ContextLinkService.getLinkFileName(itemId));
        File sourceItemLinkFile = new File(ContextLinkService.calLinksFilePath(proSourcePath.toPortableString(), itemId));
        if (sourceItemLinkFile.exists()) {
            IPath proTargetPath = new Path(destinationDirectory.getAbsolutePath()).append(getProjectOutputPath());
            File targetItemLinkFile = new File(ContextLinkService.calLinksFilePath(proTargetPath.toPortableString(), itemId));
            IPath sourceItemLinkPath = new Path(sourceItemLinkFile.getAbsolutePath());
            copyAndAddResource(toExport, sourceItemLinkPath, new Path(targetItemLinkFile.getAbsolutePath()), itemLinkRelatedPath);
        }
    }

    private void copyAndAddResource(Map<File, IPath> toExport, IPath sourcePath, IPath targetPath, IPath relativeItemPath)
            throws IOException {
        FilesUtils.copyFile(new File(sourcePath.toPortableString()), new File(targetPath.toPortableString()));
        toExport.put(targetPath.toFile(), relativeItemPath);
    }

    private File createTmpDirectory() throws IOException {
        File tmpDirectory = null;
        int suffix = 0;
        org.talend.core.model.general.Project project = ProjectManager.getInstance().getCurrentProject();
        IProject physProject;
        String tmpFolder = System.getProperty("user.dir"); //$NON-NLS-1$
        try {
            physProject = ResourceModelUtils.getProject(project);
            tmpFolder = physProject.getFolder("temp").getLocation().toPortableString(); //$NON-NLS-1$
        } catch (Exception e) {
            ExceptionHandler.process(e);
        }
        while (tmpDirectory == null || tmpDirectory.exists()) {
            tmpDirectory = new File(tmpFolder + File.separatorChar + "talendExportItems" + suffix); //$NON-NLS-1$
            suffix++;
        }

        if (!tmpDirectory.mkdir()) {
            throw new IOException(Messages.getString("ExportItemUtil.cannotCreate", tmpDirectory)); //$NON-NLS-1$
        }

        return tmpDirectory;
    }

    private void deleteTmpDirectory(File tmpDirectory) {
        if (tmpDirectory.isFile()) {
            tmpDirectory.delete();
        } else {
            for (File file : tmpDirectory.listFiles()) {
                deleteTmpDirectory(file);
            }
            tmpDirectory.delete();
        }
    }

    private IPath getProjectOutputPath() {
        if (projectNameLowerCase) {
            return new Path(project.getTechnicalLabel().toLowerCase());
        }
        return new Path(project.getTechnicalLabel());
    }

    private IPath getProjectLocationPath(String technicalLabel) {
        return getEclipseProject(technicalLabel).getLocation();
    }

    private IPath getProjectSettingLocationPath(String technicalLabel) {
        IPath projectPath = getProjectLocationPath(technicalLabel);
        IPath projectSettingFolderPath = projectPath.append(File.separator).append(FileConstants.SETTINGS_FOLDER_NAME);
        return projectSettingFolderPath;
    }

    private IPath getProjectSettingFolderPath () {
        IPath projectOutPath = getProjectOutputPath();
        IPath projectSettingFolderPath = projectOutPath.append(File.separator).append(FileConstants.SETTINGS_FOLDER_NAME);
        return projectSettingFolderPath;
    }

    // For fix TDI-34281
    protected IProject getEclipseProject(String technicalLabel) {
        IWorkspace workspace = ResourcesPlugin.getWorkspace();
        IProject eclipseProject = workspace.getRoot().getProject(technicalLabel);
        return eclipseProject;
    }

    private void fixItem(Item item) {
        item.getProperty().setLabel(item.getProperty().getLabel().replace(' ', '_'));
    }

    private void saveResources(ResourceSet resourceSet) throws IOException, PersistenceException {
        for (Resource resource : resourceSet.getResources()) {
            if (resource.getURI().isFile()) {
                EmfHelper.saveResource(resource);
            }
        }
    }

    private void fixItemLockState(Item item) {
        // Item item = (Item) EcoreUtil.getObjectByType(propertyResource.getContents(),
        // PropertiesPackage.eINSTANCE.getItem());
        item.getState().setLocker(null);
        item.getState().setLockDate(null);
        item.getState().setLocked(false);
    }

    private void createFolder(File folder) throws IOException {
        folder.mkdirs();
        if (!folder.exists()) {
            throw new IOException(Messages.getString("ExportItemUtil.cannotCreateDir", folder)); //$NON-NLS-1$
        }
    }

    private File checkAndGetDesParentFile(File destinationFile) throws IOException {
        IPath defaultDesPath = null;
        if (destinationFile.getParentFile() == null) {
            defaultDesPath = new Path(destinationFile.getAbsolutePath());
        }
        if (defaultDesPath != null) {
            return new File(defaultDesPath.toPortableString()).getParentFile();
        }
        return destinationFile.getParentFile();
    }

    public IFileExporterFullPath getExporter() {
        return exporter;
    }
}
