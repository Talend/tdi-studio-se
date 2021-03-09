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
package org.talend.repository.ui.wizards.newproject.copyfromeclipse;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.internal.wizards.datatransfer.ArchiveFileManipulations;
import org.eclipse.ui.internal.wizards.datatransfer.ILeveledImportStructureProvider;
import org.eclipse.ui.internal.wizards.datatransfer.TarEntry;
import org.eclipse.ui.internal.wizards.datatransfer.TarException;
import org.eclipse.ui.internal.wizards.datatransfer.TarFile;
import org.eclipse.ui.internal.wizards.datatransfer.TarLeveledStructureProvider;
import org.eclipse.ui.statushandlers.StatusManager;
import org.talend.commons.exception.PersistenceException;
import org.talend.commons.runtime.model.emf.provider.EmfResourcesFactoryReader;
import org.talend.commons.runtime.model.emf.provider.ResourceOption;
import org.talend.commons.runtime.xml.XMLFileUtil;
import org.talend.commons.ui.runtime.exception.ExceptionHandler;
import org.talend.core.model.properties.Project;
import org.talend.core.prefs.IDEWorkbenchPlugin;
import org.talend.core.repository.constants.FileConstants;
import org.talend.core.repository.utils.XmiResourceManager;
import org.talend.repository.ProjectManager;
import org.talend.repository.i18n.Messages;
import org.talend.repository.items.importexport.ui.managers.TalendZipLeveledStructureProvider;
import org.talend.repository.model.migration.ChangeProjectTechinicalNameMigrationTask;
import org.talend.repository.ui.actions.importproject.ImportProjectBean;
import org.talend.repository.ui.actions.importproject.ImportProjectHelper;
import org.talend.repository.ui.utils.AfterImportProjectUtil;
import org.talend.repository.utils.ZipFileUtils;
import org.talend.utils.files.FileUtils;
import org.talend.utils.io.FilesUtils;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/**
 * DOC zhangchao.wang class global comment. Detailled comment
 */
public class TalendWizardProjectsImportPage extends AbstractWizardProjectsImportPage {

    public TalendWizardProjectsImportPage() {

    }

    private String sourcePath;

    private ILeveledImportStructureProvider structureProvider;

    private Map<String,String> oldToNewSource = new HashMap<>();

    /**
     *
     * DOC guanglong.du TalendWizardProjectsImportPage class global comment. Detailled comment
     */
    public class TalendProjectRecord {

        File projectSystemFile;

        Object projectArchiveFile;

        String projectName;

        Object parent;

        int level;

        boolean hasConflicts;

        IProjectDescription description;

        /**
         * Create a record for a project based on the info in the file.
         *
         * @param file
         */
        TalendProjectRecord(File file) {
            projectSystemFile = file;
            setProjectName();
        }

        /**
         * @param file The Object representing the .project file
         * @param parent The parent folder of the .project file
         * @param level The number of levels deep in the provider the file is
         */
        TalendProjectRecord(Object file, Object parent, int level) {
            this.projectArchiveFile = file;
            this.parent = parent;
            this.level = level;
            setProjectName();
        }

        /**
         * Set the name of the project based on the projectFile.
         */
        private void setProjectName() {
            try {
                if (projectArchiveFile != null) {
                    InputStream stream = structureProvider.getContents(projectArchiveFile);

                    // If we can get a description pull the name from there
                    if (stream == null) {
                        if (projectArchiveFile instanceof ZipEntry) {
                            IPath path = new Path(((ZipEntry) projectArchiveFile).getName());
                            projectName = path.segment(path.segmentCount() - 2);
                        } else if (projectArchiveFile instanceof TarEntry) {
                            IPath path = new Path(((TarEntry) projectArchiveFile).getName());
                            projectName = path.segment(path.segmentCount() - 2);
                        }
                    } else {
                        // description = IDEWorkbenchPlugin.getPluginWorkspace().loadProjectDescription(stream);
                        description = ResourcesPlugin.getWorkspace().loadProjectDescription(stream);
                        stream.close();
                        projectName = description.getName();
                    }

                }

                // If we don't have the project name try again
                if (projectName == null) {
                    IPath path = new Path(projectSystemFile.getPath());
                    // if the file is in the default location, use the directory
                    // name as the project name
                    if (isDefaultLocation(path)) {
                        projectName = path.segment(path.segmentCount() - 2);
                        // description = IDEWorkbenchPlugin.getPluginWorkspace().newProjectDescription(projectName);
                        description = ResourcesPlugin.getWorkspace().newProjectDescription(projectName);
                    } else {
                        // description = IDEWorkbenchPlugin.getPluginWorkspace().loadProjectDescription(path);
                        description = ResourcesPlugin.getWorkspace().loadProjectDescription(path);
                        projectName = description.getName();
                    }

                }
            } catch (CoreException e) {
                // no good couldn't get the name
            } catch (IOException e) {
                // no good couldn't get the name
            }
        }

        /**
         * Returns whether the given project description file path is in the default location for a project
         *
         * @param path The path to examine
         * @return Whether the given path is the default location for a project
         */
        private boolean isDefaultLocation(IPath path) {
            // The project description file must at least be within the project,
            // which is within the workspace location
            if (path.segmentCount() < 2) {
                return false;
            }
            return path.removeLastSegments(2).toFile().equals(Platform.getLocation().toFile());
        }

        /**
         * Get the name of the project
         *
         * @return String
         */
        public String getProjectName() {
            return projectName;
        }

        /**
         * Gets the label to be used when rendering this project record in the UI.
         *
         * @return String the label
         * @since 3.4
         */
        public String getProjectLabel() {
            if (description == null) {
                return projectName;
            }

            String path = projectSystemFile == null ? structureProvider.getLabel(parent) : projectSystemFile.getParent();

            // return NLS.bind(DataTransferMessages.WizardProjectsImportPage_projectLabel, projectName, path);
            return NLS.bind(Messages.getString("DataTransferMessages.WizardProjectsImportPage_projectLabel"), projectName, path); //$NON-NLS-1$
        }

        /**
         * @return Returns the hasConflicts.
         */
        public boolean hasConflicts() {
            return hasConflicts;
        }
    }

    @Override
    public void createControl(Composite parent) {
        super.createControl(parent);

        try {
            // hide 'search for nested projects'
            Button nestedProjectsCheckbox = super.getNestedProjectsCheckbox();
            Object gridDataObj = nestedProjectsCheckbox.getLayoutData();
            GridData gridData = null;
            if (gridDataObj == null) {
                gridData = new GridData();
                nestedProjectsCheckbox.setLayoutData(gridData);
            } else {
                gridData = (GridData) gridDataObj;
            }
            gridData.exclude = true;
        } catch (SecurityException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        ViewerComparator comparator = new ViewerComparator(new Comparator<Object>() {

            @Override
            public int compare(Object s1, Object s2) {
                return ((String) s1).compareTo((String) s2);
            }
        });
        getProjectsList().setComparator(comparator);
    }

    @Override
    protected void addToWorkingSets() {
        // no need
    }

    @Override
    protected void createWorkingSetGroup(Composite workArea) {
        // hide workingSetGroup
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.ui.internal.wizards.datatransfer.WizardProjectsImportPage#updateProjectsList(java.lang.String)
     */
    @Override
    public void updateProjectsList(String sourcePath) {
        if (sourcePath == null || sourcePath.length() == 0) {
            return;
        }
        // String destinationJavaPath = null;
        // String destinationPerlPath = null;
        try {
            if (!("".equals(sourcePath))) { //$NON-NLS-1$
                String finalSource = sourcePath;
                if (oldToNewSource.containsKey(sourcePath)) {
                    finalSource = oldToNewSource.get(sourcePath);
                } else {
                    Button copyCheckbox = getCopyCheckbox();
                    if (copyCheckbox != null && copyCheckbox.getSelection()) {
                        finalSource = items2Projects(sourcePath);
                    }
                    oldToNewSource.put(sourcePath, finalSource);
                }
                this.sourcePath = finalSource;
                super.updateProjectsList(finalSource);
            }
        } catch (Exception e) {
            ExceptionHandler.process(e);
        }
    }
    /**
     *
     * DOC xlwang Comment method "items2Projects".
     */
    public String items2Projects(String sourcePath) throws Exception {
        Collection files = new ArrayList();
        // find the talend.project file
        collectProjectFilesFromDirectory(files, new File(sourcePath), null);
        File tmpPath = FileUtils.createTmpFolder("talendImportTmp", null);
        boolean flag = false;
        if (ArchiveFileManipulations.isZipFile(sourcePath)) {
            String tmpPathStr = tmpPath.getPath();
            FilesUtils.unzip(sourcePath, tmpPath.getPath());
            flag = true;
        }
        Iterator filesIterator = files.iterator();
        while (filesIterator.hasNext()) {
            File file = (File) filesIterator.next();
            String talendFilePath = file.getPath();
            String projectPath = talendFilePath.substring(0, talendFilePath.lastIndexOf(File.separator));
            FilesUtils.copyDirectory(new File(projectPath), tmpPath);
        }
        // loop the tmp file
        files = new ArrayList();
        collectProjectFilesFromDirectory(files, tmpPath, null);
        Iterator tmpFilesIterator = files.iterator();
        while (tmpFilesIterator.hasNext()) {
            File file = (File) tmpFilesIterator.next();
            String tmpTalendFilePath = file.getPath();
            String tmpProjectPath = tmpTalendFilePath.substring(0, tmpTalendFilePath.lastIndexOf(File.separator));
            String tmpProjectFileStr = tmpProjectPath + File.separator + ".project";
            File tmpProjectFile = new File(tmpProjectFileStr);
            String projectName = "";
            if (!tmpProjectFile.exists()) {
                Document document = XMLFileUtil.loadDoc(new File(tmpTalendFilePath));
                Node node = document.getChildNodes().item(0).getChildNodes().item(1);
                NamedNodeMap map = node.getAttributes();
                for (int i = 0; i < map.getLength(); i++) {
                    if ("technicalLabel".equals(map.item(i).getNodeName())) {
                        projectName = map.item(i).getNodeValue();
                    }
                }
                FileUtils.createProjectFile(projectName, tmpProjectFile);
            }
        }
        if (flag) {
            ZipFileUtils.zip(tmpPath.getPath(), tmpPath.getPath() + ".zip", false);
            return tmpPath.getPath() + ".zip";
        }
        return tmpPath.getPath();
    }
    @Override
    public ProjectRecord[] getProjectRecords() {
        if (sourcePath == null || sourcePath.length() == 0) {
            return new ProjectRecord[0];
        }
        ProjectRecord[] selectedProjects = super.getProjectRecords();
        Collection files = new ArrayList();
        List projects = new ArrayList();
        ProjectRecord[] selected = null;
        for (ProjectRecord selectedProject : selectedProjects) {
            projects.add(selectedProject);
        }
        final File directory = new File(sourcePath);
        if (ArchiveFileManipulations.isTarFile(sourcePath)) {
            try {
                TarFile sourceTarFile = new TarFile(sourcePath);
                if (sourceTarFile == null) {
                    return new ProjectRecord[0];
                }
                structureProvider = new TarLeveledStructureProvider(sourceTarFile);
                Object child = structureProvider.getRoot();
                collectProjectFilesFromProvider(files, child, 0);
                selected = new ProjectRecord[files.size()];
                Iterator filesIterator = files.iterator();
                int j = 0;
                while (filesIterator.hasNext()) {
                    TalendProjectRecord file = (TalendProjectRecord) filesIterator.next();
                    for (int i = 0; i < projects.size(); i++) {
                        if (file.getProjectName().equals(((ProjectRecord) projects.get(i)).getProjectName())) {
                            selected[j] = (ProjectRecord) projects.get(i);
                            j++;
                        }
                    }

                }
            } catch (TarException e) {
                // displayErrorDialog(DataTransferMessages.TarImport_badFormat);
                displayErrorDialog(Messages.getString("DataTransferMessages.TarImport_badFormat")); //$NON-NLS-1$
            } catch (IOException e) {
                // displayErrorDialog(DataTransferMessages.ZipImport_couldNotRead);
                displayErrorDialog(Messages.getString("DataTransferMessages.ZipImport_couldNotRead")); //$NON-NLS-1$
            }
        } else if (ArchiveFileManipulations.isZipFile(sourcePath)) {
            try {
                ZipFile sourceFile = new ZipFile(sourcePath);
                if (sourceFile == null) {
                    return new ProjectRecord[0];
                }
                structureProvider = new TalendZipLeveledStructureProvider(sourceFile);
                Object child = structureProvider.getRoot();
                collectProjectFilesFromProvider(files, child, 0);
                selected = new ProjectRecord[files.size()];
                Iterator filesIterator = files.iterator();
                int j = 0;
                while (filesIterator.hasNext()) {
                    TalendProjectRecord file = (TalendProjectRecord) filesIterator.next();
                    for (int i = 0; i < projects.size(); i++) {
                        if (file.getProjectName().equals(((ProjectRecord) projects.get(i)).getProjectName())) {
                            selected[j] = (ProjectRecord) projects.get(i);
                            j++;
                        }
                    }
                }
            } catch (IOException e) {
                // displayErrorDialog(DataTransferMessages.ZipImport_badFormat);
                displayErrorDialog(Messages.getString("DataTransferMessages.ZipImport_badFormat")); //$NON-NLS-1$
            }
        } else if (directory.isDirectory()) {
            collectProjectFilesFromDirectory(files, directory, null);
            selected = new ProjectRecord[files.size()];
            Iterator filesIterator = files.iterator();
            int j = 0;
            while (filesIterator.hasNext()) {
                File file = (File) filesIterator.next();
                for (int i = 0; i < projects.size(); i++) {
                    if (file.getParentFile().getName().equals(((ProjectRecord) projects.get(i)).getProjectName())) {
                        selected[j] = (ProjectRecord) projects.get(i);
                        j++;
                    }
                }
            }
        }

        return selected;
    }

    private boolean collectProjectFilesFromProvider(Collection files, Object entry, int level) {
        List children = structureProvider.getChildren(entry);
        if (children == null) {
            children = new ArrayList(1);
        }
        boolean isContainsFile = false;
        Iterator childrenEnum = children.iterator();
        for (int i = 0; i < children.size(); i++) {
            Object child = children.get(i);
            if (!structureProvider.isFolder(child)) {
                String elementLabel = structureProvider.getLabel(child);
                if (elementLabel.equals(FileConstants.LOCAL_PROJECT_FILENAME)) {
                    isContainsFile = true;
                }
            }
        }
        while (childrenEnum.hasNext()) {
            Object child = childrenEnum.next();
            if (structureProvider.isFolder(child)) {
                collectProjectFilesFromProvider(files, child, level + 1);
            }
            String elementLabel = structureProvider.getLabel(child);
            if (elementLabel.equals(".project") && isContainsFile) {
                files.add(new TalendProjectRecord(child, entry, level));
            }
        }
        return true;
    }

    /**
     * This method must not be called outside the workbench.
     *
     * Utility method for creating status.
     *
     * @param severity
     * @param message
     * @return {@link IStatus}
     */
    private static IStatus newStatus(int severity, String message, Throwable exception) {

        String statusMessage = message;
        if (message == null || message.trim().length() == 0) {
            if (exception.getMessage() == null) {
                statusMessage = exception.toString();
            } else {
                statusMessage = exception.getMessage();
            }
        }

        return new Status(severity, IDEWorkbenchPlugin.IDE_WORKBENCH, severity, statusMessage, exception);
    }

    public boolean collectProjectFilesFromDirectory(Collection files, File directory, Set directoriesVisited) {

        File[] contents = directory.listFiles();
        if (contents == null) {
            return false;
        }

        // Initialize recursion guard for recursive symbolic links
        if (directoriesVisited == null) {
            directoriesVisited = new HashSet();
            try {
                directoriesVisited.add(directory.getCanonicalPath());
            } catch (IOException exception) {
                // StatusManager.getManager()
                // .handle(StatusUtil.newStatus(IStatus.ERROR, exception.getLocalizedMessage(), exception));
                StatusManager.getManager().handle(newStatus(IStatus.ERROR, exception.getLocalizedMessage(), exception));
            }
        }

        // first look for project description files
        for (File file : contents) {
            if (file.isFile() && file.getName().equals(FileConstants.LOCAL_PROJECT_FILENAME)) {
                files.add(file);
                // don't search sub-directories since we can't have nested
                // projects
                return true;
            }
        }
        // no project description found, so recurse into sub-directories
        for (int i = 0; i < contents.length; i++) {
            if (contents[i].isDirectory()) {
                if (!contents[i].getName().equals(METADATA_FOLDER)) {
                    try {
                        String canonicalPath = contents[i].getCanonicalPath();
                        if (!directoriesVisited.add(canonicalPath)) {
                            // already been here --> do not recurse
                            continue;
                        }
                    } catch (IOException exception) {
                        // StatusManager.getManager().handle(
                        // StatusUtil.newStatus(IStatus.ERROR, exception.getLocalizedMessage(), exception));
                        StatusManager.getManager().handle(newStatus(IStatus.ERROR, exception.getLocalizedMessage(), exception));
                    }
                    collectProjectFilesFromDirectory(files, contents[i], directoriesVisited);
                }
            }
        }
        return true;
    }

    public final static String EXTERNAL_LIB_JAVA_PATH = "external_lib_java_path"; //$NON-NLS-1$

    public final static String EXTERNAL_LIB_PERL_PATH = "external_lib_perl_path"; //$NON-NLS-1$

    @SuppressWarnings("restriction")
    @Override
    public boolean createProjects() {
        // TDI-19269
        final boolean created = super.createProjects();
        //
        final Object[] selected = getProjectsList().getCheckedElements();
        XmiResourceManager xmiManager = new XmiResourceManager();
        final ResourceOption importOption = ResourceOption.ITEM_IMPORTATION;
        try {
            EmfResourcesFactoryReader.INSTANCE.addOption(importOption, false);
            final ImportProjectHelper importHelper = new ImportProjectHelper();
            List<ImportProjectBean> projectBeanList = new ArrayList<ImportProjectBean>();
            for (Object element : selected) {
                final ProjectRecord record = (ProjectRecord) element;
                String projectName = record.getProjectName();
                final IWorkspace workspace = ResourcesPlugin.getWorkspace();
                final IProject project = workspace.getRoot().getProject(projectName);

                try {
                    final Project loadProject = xmiManager.loadProject(project);
                    loadProject.setLocal(true);
                    loadProject.setId(0);
                    loadProject.setUrl(null);
                    loadProject.setCreationDate(null);
                    //loadProject.setDescription(""); //$NON-NLS-1$
                    loadProject.setType(null);
                    xmiManager.saveResource(loadProject.eResource());

                    // FIXME TDI-22786, migrate the project name.
                    if (ProjectManager.enableSpecialTechnicalProjectName()) {
                        ChangeProjectTechinicalNameMigrationTask migrationTask = new ChangeProjectTechinicalNameMigrationTask();
                        migrationTask.migrateTalendProject(project, loadProject, xmiManager);
                    }
                    importHelper.checkProjectItems(new org.talend.core.model.general.Project(loadProject));

                    try {
                        ImportProjectBean bean = new ImportProjectBean(new org.talend.core.model.general.Project(loadProject),
                                null);
                        projectBeanList.add(bean);
                        AfterImportProjectUtil.runAfterImportProjectActions(bean);
                    } catch (Exception ex) {
                        ExceptionHandler.process(ex);
                    }
                } catch (PersistenceException e) {
                    ExceptionHandler.process(e);
                }
            }
            List<String> invalidProjectLableList = new ArrayList<String>();
            for (ImportProjectBean projectBean : projectBeanList) {
                if (projectBean.getInvalidViewObjectList().size() > 0) {
                    invalidProjectLableList.add(projectBean.newProject.getTechnicalLabel());
                }
            }
            if (invalidProjectLableList.size() > 0) {
                AfterImportProjectUtil.deleteImprotedInvalidProject(invalidProjectLableList);
            }
        } finally {
            xmiManager.unloadResources();
            EmfResourcesFactoryReader.INSTANCE.removOption(importOption, false);
        }
        return created;
        //
    }
}
