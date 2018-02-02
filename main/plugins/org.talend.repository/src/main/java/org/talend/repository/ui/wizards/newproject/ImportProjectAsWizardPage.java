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
package org.talend.repository.ui.wizards.newproject;

import java.io.File;
import java.io.IOException;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.internal.wizards.datatransfer.ArchiveFileManipulations;
import org.eclipse.ui.internal.wizards.datatransfer.TarException;
import org.eclipse.ui.internal.wizards.datatransfer.TarFile;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.model.general.Project;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.core.ui.branding.IBrandingService;
import org.talend.core.utils.ProjectUtils;
import org.talend.repository.RepositoryPlugin;
import org.talend.repository.i18n.Messages;
import org.talend.repository.model.IProxyRepositoryFactory;
import org.talend.repository.ui.actions.importproject.ImportProjectHelper;

/**
 * Page for new project details. <br/>
 * 
 * $Id: NewProjectWizardPage.java 1877 2007-02-06 17:16:43Z amaumont $
 * 
 */
public class ImportProjectAsWizardPage extends WizardPage {

    /** Name field. */
    private Text nameText;

    /** Technical Name. */
    private Text technicalNameText;

    // private Text filePath;

    /** Browse button. */
    // private Button browseBtn;
    private IStatus nameStatus;

    private IStatus fileNameStatus;

    private IStatus fileContentStatus;

    private Button projectFromDirectoryRadio;

    private Text directoryPathField;

    private Button browseDirectoriesButton;

    private Button projectFromArchiveRadio;

    private Text archivePathField;

    private Button browseArchivesButton;

    private Button importManyProjectsButton;

    private String previouslyBrowsedDirectory = ""; //$NON-NLS-1$

    private String previouslyBrowsedArchive = ""; //$NON-NLS-1$

    private String lastPath;

    // constant from WizardArchiveFileResourceImportPage1
    private static final String[] FILE_IMPORT_MASK = { "*.jar;*.zip;*.tar;*.tar.gz;*.tgz", "*.*" }; //$NON-NLS-1$ //$NON-NLS-2$

    /**
     * Constructs a new NewProjectWizardPage.
     * 
     * @param server
     * @param password
     * @param author
     */
    public ImportProjectAsWizardPage() {
        super("WizardPage"); //$NON-NLS-1$

        IBrandingService brandingService = (IBrandingService) GlobalServiceRegister.getDefault().getService(
                IBrandingService.class);
        setTitle(Messages.getString("ImportProjectAsWizardPage.title")); //$NON-NLS-1$
        setDescription(Messages.getString("ImportProjectAsWizardPage.description", brandingService.getShortProductName())); //$NON-NLS-1$

        nameStatus = createOkStatus();
        fileNameStatus = createOkStatus();
        fileContentStatus = createOkStatus();
    }

    /**
     * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
     */
    public void createControl(Composite parent) {
        Composite container = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout();
        container.setLayoutData(new GridData(GridData.FILL_BOTH | GridData.GRAB_HORIZONTAL | GridData.GRAB_VERTICAL));
        container.setLayout(layout);

        createNamesFields(container);
        createProjectsRoot(container);
        createAdditionnalsButtons(container);

        setControl(container);
        addListeners();
        setPageComplete(false);
    }

    private void createNamesFields(Composite workArea) {
        Composite projectGroup = new Composite(workArea, SWT.NONE);
        GridLayout layout = new GridLayout();
        layout.numColumns = 2;
        layout.makeColumnsEqualWidth = false;
        layout.marginWidth = 0;
        projectGroup.setLayout(layout);
        projectGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        // Name
        Label nameLab = new Label(projectGroup, SWT.NONE);
        nameLab.setText(Messages.getString("NewProjectWizardPage.name")); //$NON-NLS-1$

        nameText = new Text(projectGroup, SWT.BORDER);
        nameText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        // TechnicalName (for information only)
        Label technicalNameLab = new Label(projectGroup, SWT.NONE);
        technicalNameLab.setText(Messages.getString("NewProjectWizardPage.technicalName")); //$NON-NLS-1$

        technicalNameText = new Text(projectGroup, SWT.BORDER);
        technicalNameText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        technicalNameText.setEnabled(false);
    }

    private void createAdditionnalsButtons(Composite workArea) {

        Composite projectGroup = new Composite(workArea, SWT.NONE);
        GridLayout layout = new GridLayout();
        layout.numColumns = 3;
        layout.makeColumnsEqualWidth = false;
        layout.marginWidth = 0;
        projectGroup.setLayout(layout);
        projectGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        // importManyProjectsButton = new Button(projectGroup, SWT.PUSH);
        // ImportProjectsAction ipa = ImportProjectsAction.getInstance();
        // importManyProjectsButton.setText(ipa.getText());
        // importManyProjectsButton.setToolTipText(ipa.getToolTipText());
        // importManyProjectsButton.setImage(ImageProvider.getImage(ipa.
        // getImageDescriptor()));
        //
        // importManyProjectsButton.addSelectionListener(new SelectionAdapter()
        // {
        //
        // @Override
        // public void widgetSelected(SelectionEvent e) {
        // ImportProjectsAction.getInstance().run();
        // }
        // });
    }

    @Override
    public boolean canFlipToNextPage() {
        return true;
    }

    /**
     * Create the area where you select the root directory for the projects.
     * 
     * @param workArea Composite
     */
    private void createProjectsRoot(Composite workArea) {

        // project specification group
        Composite projectGroup = new Composite(workArea, SWT.NONE);
        GridLayout layout = new GridLayout();
        layout.numColumns = 3;
        layout.makeColumnsEqualWidth = false;
        layout.marginWidth = 0;
        projectGroup.setLayout(layout);
        projectGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        // new project from directory radio button
        projectFromDirectoryRadio = new Button(projectGroup, SWT.RADIO);
        projectFromDirectoryRadio.setText(Messages.getString("ImportProjectAsWizardPage.form.selectDirectory")); //$NON-NLS-1$

        // project location entry field
        directoryPathField = new Text(projectGroup, SWT.BORDER);
        directoryPathField.setEditable(false);

        directoryPathField.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL));

        // browse button
        browseDirectoriesButton = new Button(projectGroup, SWT.PUSH);
        browseDirectoriesButton.setText(Messages.getString("ImportProjectAsWizardPage.form.browse")); //$NON-NLS-1$
        setButtonLayoutData(browseDirectoriesButton);

        // new project from archive radio button
        projectFromArchiveRadio = new Button(projectGroup, SWT.RADIO);
        projectFromArchiveRadio.setText(Messages.getString("ImportProjectAsWizardPage.form.selectArchive")); //$NON-NLS-1$

        // project location entry field
        archivePathField = new Text(projectGroup, SWT.BORDER);
        archivePathField.setEditable(false);

        archivePathField.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL));
        // browse button
        browseArchivesButton = new Button(projectGroup, SWT.PUSH);
        browseArchivesButton.setText(Messages.getString("ImportProjectAsWizardPage.form.browse")); //$NON-NLS-1$
        setButtonLayoutData(browseArchivesButton);

        projectFromDirectoryRadio.setSelection(true);
        archivePathField.setEnabled(false);
        browseArchivesButton.setEnabled(false);

        browseDirectoriesButton.addSelectionListener(new SelectionAdapter() {

            /*
             * (non-Javadoc)
             * 
             * @see org.eclipse.swt.events.SelectionAdapter#widgetS elected(org.eclipse.swt.events.SelectionEvent)
             */
            @Override
            public void widgetSelected(SelectionEvent e) {
                handleLocationDirectoryButtonPressed();
            }

        });

        browseArchivesButton.addSelectionListener(new SelectionAdapter() {

            /*
             * (non-Javadoc)
             * 
             * @see org.eclipse.swt.events.SelectionAdapter#widgetSelected(org.eclipse .swt.events.SelectionEvent)
             */
            @Override
            public void widgetSelected(SelectionEvent e) {
                handleLocationArchiveButtonPressed();
            }

        });

        projectFromDirectoryRadio.addSelectionListener(new SelectionAdapter() {

            /*
             * (non-Javadoc)
             * 
             * @see org.eclipse.swt.events.SelectionListener#widgetSelected(org.eclipse .swt.events.SelectionEvent)
             */
            @Override
            public void widgetSelected(SelectionEvent e) {
                directoryRadioSelected();
            }
        });

        projectFromArchiveRadio.addSelectionListener(new SelectionAdapter() {

            /*
             * (non-Javadoc)
             * 
             * @see org.eclipse.swt.events.SelectionListener#widgetSelected(org.eclipse .swt.events.SelectionEvent)
             */
            @Override
            public void widgetSelected(SelectionEvent e) {
                archiveRadioSelected();
            }
        });
    }

    private void archiveRadioSelected() {
        if (projectFromArchiveRadio.getSelection()) {
            directoryPathField.setEnabled(false);
            browseDirectoriesButton.setEnabled(false);
            archivePathField.setEnabled(true);
            browseArchivesButton.setEnabled(true);
            evaluateSpecifiedPath(archivePathField.getText());
            browseArchivesButton.setFocus();
        }
    }

    private void directoryRadioSelected() {
        if (projectFromDirectoryRadio.getSelection()) {
            directoryPathField.setEnabled(true);
            browseDirectoriesButton.setEnabled(true);
            archivePathField.setEnabled(false);
            browseArchivesButton.setEnabled(false);
            evaluateSpecifiedPath(directoryPathField.getText());
            browseDirectoriesButton.setFocus();
        }
    }

    private void handleLocationDirectoryButtonPressed() {
        DirectoryDialog dialog = new DirectoryDialog(directoryPathField.getShell());
        dialog.setMessage(Messages.getString("ImportProjectAsWizardPage.dialog.selectDirectory.desc")); //$NON-NLS-1$

        String dirName = directoryPathField.getText().trim();
        if (dirName.length() == 0) {
            dirName = previouslyBrowsedDirectory;
        }

        if (dirName.length() == 0) {
            dialog.setFilterPath(ResourcesPlugin.getWorkspace().getRoot().getLocation().toOSString());
        } else {
            File path = new File(dirName);
            if (path.exists()) {
                dialog.setFilterPath(new Path(dirName).toOSString());
            }
        }

        String selectedDirectory = dialog.open();
        if (selectedDirectory != null) {
            previouslyBrowsedDirectory = selectedDirectory;
            directoryPathField.setText(previouslyBrowsedDirectory);
            evaluateSpecifiedPath(selectedDirectory);
        }

    }

    private void handleLocationArchiveButtonPressed() {
        FileDialog dialog = new FileDialog(archivePathField.getShell());
        dialog.setFilterExtensions(FILE_IMPORT_MASK);
        dialog.setText(Messages.getString("ImportProjectAsWizardPage.dialog.selectArchive.title")); //$NON-NLS-1$

        String fileName = archivePathField.getText().trim();
        if (fileName.length() == 0) {
            fileName = previouslyBrowsedArchive;
        }

        if (fileName.length() == 0) {
            dialog.setFilterPath(ResourcesPlugin.getWorkspace().getRoot().getLocation().toOSString());
        } else {
            File path = new File(fileName);
            if (path.exists()) {
                dialog.setFilterPath(new Path(fileName).toOSString());
            }
        }

        String selectedArchive = dialog.open();
        if (selectedArchive != null) {
            previouslyBrowsedArchive = selectedArchive;
            archivePathField.setText(previouslyBrowsedArchive);
            evaluateSpecifiedPath(selectedArchive);
        }

    }

    @SuppressWarnings("restriction")
    private void evaluateSpecifiedPath(String path) {

        // on an empty path empty selectedProjects
        if (path == null || path.length() == 0) {
            checkFieldsValue();
            return;
        }

        final boolean dirSelected = this.projectFromDirectoryRadio.getSelection();
        if (!dirSelected && ArchiveFileManipulations.isTarFile(path)) {
            TarFile sourceTarFile = getSpecifiedTarSourceFile(path);
            if (sourceTarFile == null) {
                return;
            }

        } else if (!dirSelected && ArchiveFileManipulations.isZipFile(path)) {
            ZipFile sourceFile = getSpecifiedZipSourceFile(path);
            if (sourceFile == null) {
                return;
            }
        }
        lastPath = path;

        org.talend.core.model.properties.Project project = null;
        ImportProjectHelper helper = new ImportProjectHelper();
        try {
            project = helper.retrieveProjectFilesFromProvider(null, new File(path));
        } catch (OperationCanceledException e) {
            return;
        }
        boolean valid = true;
        IBrandingService brandingService = (IBrandingService) GlobalServiceRegister.getDefault().getService(
                IBrandingService.class);
        if (project == null) {
            valid = false;
            String string = Messages.getString("ImportProjectAsWizardPage.error.notATalendProject", //$NON-NLS-1$
                    brandingService.getShortProductName());
            fileContentStatus = new Status(IStatus.ERROR, RepositoryPlugin.PLUGIN_ID, IStatus.OK, string, null);
        }

        if (valid) {
            fileContentStatus = createOkStatus();

        }
        checkFieldsValue();
    }

    /**
     * Answer a handle to the zip file currently specified as being the source. Return null if this file does not exist
     * or is not of valid format.
     */
    private TarFile getSpecifiedTarSourceFile(String fileName) {
        if (fileName.length() == 0) {
            return null;
        }

        try {
            return new TarFile(fileName);
        } catch (TarException e) {
            displayErrorDialog(Messages.getString("ImportProjectAsWizardPage.error.zip")); //$NON-NLS-1$
        } catch (IOException e) {
            displayErrorDialog(Messages.getString("ImportProjectAsWizardPage.error.zip")); //$NON-NLS-1$
        }

        archivePathField.setFocus();
        return null;
    }

    /**
     * Answer a handle to the zip file currently specified as being the source. Return null if this file does not exist
     * or is not of valid format.
     */
    private ZipFile getSpecifiedZipSourceFile(String fileName) {
        if (fileName.length() == 0) {
            return null;
        }

        try {
            return new ZipFile(fileName);
        } catch (ZipException e) {
            displayErrorDialog(Messages.getString("ImportProjectAsWizardPage.error.zip")); //$NON-NLS-1$
        } catch (IOException e) {
            displayErrorDialog(Messages.getString("ImportProjectAsWizardPage.error.zip")); //$NON-NLS-1$
        }

        archivePathField.setFocus();
        return null;
    }

    /**
     * Display an error dialog with the specified message.
     * 
     * @param message the error message
     */
    protected void displayErrorDialog(String message) {
        MessageDialog.openError(getContainer().getShell(), getErrorDialogTitle(), message);
    }

    /**
     * Get the title for an error dialog. Subclasses should override.
     */
    protected String getErrorDialogTitle() {
        return Messages.getString("ImportProjectAsWizardPage.error.title"); //$NON-NLS-1$
    }

    Project[] projects;

    private boolean isProjectNameAlreadyUsed(String newProjectName) {
        IProxyRepositoryFactory repositoryFactory = ProxyRepositoryFactory.getInstance();
        if (projects == null) {
            try {
                projects = repositoryFactory.readProject();
            } catch (Exception e) {
                return true;
            }
        }
        for (Project project : projects) {
            if (Project.createTechnicalName(newProjectName).compareTo(project.getTechnicalLabel()) == 0) {
                return true;
            }
        }
        return false;
    }

    private void addListeners() {
        nameText.addModifyListener(new ModifyListener() {

            public void modifyText(ModifyEvent e) {
                checkFieldsValue();
            }
        });
    }

    protected void checkFieldsValue() {
        // Field Name
        if (nameText.getText().length() == 0) {
            nameStatus = new Status(IStatus.ERROR, RepositoryPlugin.PLUGIN_ID, IStatus.OK,
                    Messages.getString("NewProjectWizardPage.nameEmpty"), null); //$NON-NLS-1$
        } else {
            technicalNameText.setText(Project.createTechnicalName(nameText.getText()));
            if (ProjectUtils.isNotValidProjectName(nameText.getText())) {
                nameStatus = new Status(IStatus.ERROR, RepositoryPlugin.PLUGIN_ID, IStatus.OK,
                        Messages.getString("NewProjectWizardPage.illegalCharacter"), null); //$NON-NLS-1$
            } else {
                if (isProjectNameAlreadyUsed(nameText.getText())) {
                    nameStatus = new Status(IStatus.ERROR, RepositoryPlugin.PLUGIN_ID, IStatus.OK,
                            Messages.getString("NewProjectWizardPage.projectNameAlredyExists"), null); //$NON-NLS-1$
                } else {
                    nameStatus = createOkStatus();
                }
            }
        }

        if (lastPath == null || lastPath.length() == 0) {
            fileNameStatus = new Status(IStatus.ERROR, RepositoryPlugin.PLUGIN_ID, IStatus.OK,
                    Messages.getString("ImportProjectAsWizardPage.error.pathRequired"), null); //$NON-NLS-1$
        } else {
            fileNameStatus = createOkStatus();
        }

        updatePageStatus();
    }

    private void updatePageStatus() {
        IStatus findMostSevere = findMostSevere();
        setMessage(findMostSevere);
        setPageComplete(findMostSevere.getSeverity() != IStatus.ERROR);
    }

    private IStatus findMostSevere() {
        IStatus status = createOkStatus();
        if (nameStatus.getSeverity() == IStatus.ERROR) {
            status = nameStatus;
        } else if (fileNameStatus.getSeverity() == IStatus.ERROR) {
            status = fileNameStatus;
        } else if (fileContentStatus.getSeverity() == IStatus.ERROR) {
            status = fileContentStatus;
        }
        return status;
    }

    private void setMessage(IStatus status) {
        if (IStatus.ERROR == status.getSeverity()) {
            setErrorMessage(status.getMessage());
            setMessage(""); //$NON-NLS-1$
        } else {
            setMessage(this.getDescription());
            setErrorMessage(null);
        }
    }

    @Override
    public String getName() {
        return nameText.getText();
    }

    public String getTechnicalName() {
        return technicalNameText.getText();
    }

    public String getSourcePath() {
        return lastPath;
    }

    public boolean isArchive() {
        return projectFromArchiveRadio.getSelection();
    }

    private static IStatus createOkStatus() {
        return new Status(IStatus.OK, RepositoryPlugin.PLUGIN_ID, IStatus.OK, "", null); //$NON-NLS-1$
    }
}
