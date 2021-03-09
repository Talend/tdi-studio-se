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
package org.talend.spagic.engines.client.ui.wizards;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.ui.internal.wizards.datatransfer.WizardFileSystemResourceExportPage1;
import org.talend.commons.exception.PersistenceException;
import org.talend.commons.ui.runtime.exception.ExceptionHandler;
import org.talend.commons.ui.runtime.exception.MessageBoxExceptionHandler;
import org.talend.core.CorePlugin;
import org.talend.core.context.Context;
import org.talend.core.context.RepositoryContext;
import org.talend.core.model.process.JobInfo;
import org.talend.core.model.properties.ProcessItem;
import org.talend.core.model.repository.IRepositoryViewObject;
import org.talend.core.model.repository.job.JobResource;
import org.talend.core.model.repository.job.JobResourceManager;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.core.ui.export.ArchiveFileExportOperationFullPath;
import org.talend.designer.runprocess.IProcessor;
import org.talend.designer.runprocess.ProcessorException;
import org.talend.designer.runprocess.ProcessorUtilities;
import org.talend.repository.documentation.ExportFileResource;
import org.talend.repository.model.IRepositoryNode.ENodeType;
import org.talend.repository.model.IRepositoryNode.EProperties;
import org.talend.repository.model.RepositoryNode;
import org.talend.repository.ui.wizards.exportjob.scriptsmanager.JobScriptsManager;
import org.talend.repository.ui.wizards.exportjob.scriptsmanager.JobScriptsManager.ExportChoice;
import org.talend.spagic.engines.client.i18n.Messages;

/**
 * Page of the Job Scripts Export Wizard. <br/>
 *
 * @referto WizardArchiveFileResourceExportPage1 $Id: JobScriptsExportWizardPage.java 1 2006-12-13 涓嬪�?3:09:07 bqian
 *
 */
public abstract class SpagicDeployWizardPage extends WizardFileSystemResourceExportPage1 {

    // widgets
    protected Button shellLauncherButton;

    protected Button systemRoutineButton;

    protected Button userRoutineButton;

    protected Button modelButton;

    protected Button jobItemButton;

    protected Button contextButton;

    protected Button jobScriptButton;

    protected ExportFileResource[] process;

    protected Combo contextCombo;

    protected Combo launcherCombo;

    protected JobScriptsManager manager;

    private IWorkspace workspace;

    protected Button applyToChildrenButton;

    /**
     * Create an instance of this class.
     *
     * @param name java.lang.String
     */
    protected SpagicDeployWizardPage(String name, IStructuredSelection selection) {
        super(name, null);
        RepositoryNode[] nodes = (RepositoryNode[]) selection.toList().toArray(new RepositoryNode[selection.size()]);

        List<ExportFileResource> list = new ArrayList<ExportFileResource>();
        for (RepositoryNode node : nodes) {
            if (node.getType() == ENodeType.SYSTEM_FOLDER || node.getType() == ENodeType.SIMPLE_FOLDER) {
                addTreeNode(node, node.getProperties(EProperties.LABEL).toString(), list);
            }
            if (node.getType() == ENodeType.REPOSITORY_ELEMENT) {
                IRepositoryViewObject repositoryObject = node.getObject();
                if (repositoryObject.getProperty().getItem() instanceof ProcessItem) {
                    ProcessItem processItem = (ProcessItem) repositoryObject.getProperty().getItem();
                    ExportFileResource resource = new ExportFileResource(processItem, processItem.getProperty().getLabel());
                    resource.setNode(node);
                    list.add(resource);
                }
            }
        }

        process = list.toArray(new ExportFileResource[list.size()]);
    }

    private void addTreeNode(RepositoryNode node, String path, List<ExportFileResource> list) {
        if (node != null && node.getType() == ENodeType.REPOSITORY_ELEMENT) {
            IRepositoryViewObject repositoryObject = node.getObject();
            if (repositoryObject.getProperty().getItem() instanceof ProcessItem) {
                ProcessItem processItem = (ProcessItem) repositoryObject.getProperty().getItem();
                ExportFileResource resource = new ExportFileResource(processItem, path);
                resource.setNode(node);
                list.add(resource);
            }
        }
        Object[] nodes = node.getChildren().toArray();
        if (nodes.length <= 0) {
            return;
        }
        for (Object node2 : nodes) {
            addTreeNode((RepositoryNode) node2, path + "/" //$NON-NLS-1$
                    + ((RepositoryNode) node2).getProperties(EProperties.LABEL).toString(), list);
        }
    }

    /**
     * (non-Javadoc) Method declared on IDialogPage.
     */
    /**
     * (non-Javadoc) Method declared on IDialogPage.
     */
    @Override
    public void createControl(Composite parent) {

        initializeDialogUnits(parent);

        Composite composite = new Composite(parent, SWT.NULL);
        composite.setLayout(new GridLayout());
        composite.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_FILL | GridData.HORIZONTAL_ALIGN_FILL));
        composite.setFont(parent.getFont());

        createDestinationGroup(composite);

        createOptionsGroup(composite);

        restoreResourceSpecificationWidgetValues(); // ie.- local
        restoreWidgetValues(); // ie.- subclass hook

        updateWidgetEnablements();
        setPageComplete(determinePageCompletion());
        setErrorMessage(null); // should not initially have error message

        setControl(composite);
        giveFocusToDestination();
    }

    /*
     * It's not a good method to resovle the problem of null pointer, which is led by commenting the //
     * createResourcesGroup(composite); and createButtonsGroup(composite); (non-Javadoc)
     *
     * @see org.eclipse.ui.internal.wizards.datatransfer.WizardFileSystemResourceExportPage1#validateSourceGroup()
     */
    @Override
    protected boolean validateSourceGroup() {
        return true;
    }

    /**
     * Create the export options specification widgets.
     *
     */
    @Override
    protected void createOptionsGroupButtons(Group optionsGroup) {
        Font font = optionsGroup.getFont();
        optionsGroup.setLayout(new GridLayout(1, true));

        Composite left = new Composite(optionsGroup, SWT.NONE);
        left.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, true, false));
        left.setLayout(new GridLayout(3, true));

        createOptions(left, font);

        // Composite right = new Composite(optionsGroup, SWT.NONE);
        // right.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, true, false));
        // right.setLayout(new GridLayout(1, true));
    }

    /**
     * Create the buttons for the group that determine if the entire or selected directory structure should be created.
     *
     * @param optionsGroup
     * @param font
     */
    protected void createOptions(Composite optionsGroup, Font font) {
        // create directory structure radios
        shellLauncherButton = new Button(optionsGroup, SWT.CHECK | SWT.LEFT);
        shellLauncherButton.setText(Messages.getString("SpagicDeployWizardPage.shellLauncher")); //$NON-NLS-1$
        shellLauncherButton.setSelection(true);
        shellLauncherButton.setFont(font);

        launcherCombo = new Combo(optionsGroup, SWT.PUSH);
        GridData gd = new GridData();
        gd.horizontalSpan = 2;
        launcherCombo.setLayoutData(gd);
        // laucherText = new Text(optionsGroup, SWT.BORDER);
        // laucherText.setEditable(false);

        // create directory structure radios
        systemRoutineButton = new Button(optionsGroup, SWT.CHECK | SWT.LEFT);
        systemRoutineButton.setText(Messages.getString("SpagicDeployWizardPage.systemRoutines")); //$NON-NLS-1$
        systemRoutineButton.setSelection(true);
        systemRoutineButton.setFont(font);

        userRoutineButton = new Button(optionsGroup, SWT.CHECK | SWT.LEFT);
        userRoutineButton.setText(Messages.getString("SpagicDeployWizardPage.userRoutines")); //$NON-NLS-1$
        userRoutineButton.setSelection(true);
        userRoutineButton.setFont(font);
        gd = new GridData();
        gd.horizontalSpan = 2;
        userRoutineButton.setLayoutData(gd);

        modelButton = new Button(optionsGroup, SWT.CHECK | SWT.LEFT);
        modelButton.setText(Messages.getString("SpagicDeployWizardPage.requiredTalendPerlModules")); //$NON-NLS-1$
        modelButton.setSelection(true);
        modelButton.setFont(font);
        gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.horizontalSpan = 3;
        modelButton.setLayoutData(gd);

        jobItemButton = new Button(optionsGroup, SWT.CHECK | SWT.LEFT);
        jobItemButton.setText(Messages.getString("SpagicDeployWizardPage.jobPerlScripts")); //$NON-NLS-1$
        jobItemButton.setSelection(true);
        jobItemButton.setFont(font);
        gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.horizontalSpan = 3;
        jobItemButton.setLayoutData(gd);

        jobScriptButton = new Button(optionsGroup, SWT.CHECK | SWT.LEFT);
        jobScriptButton.setText(Messages.getString("SpagicDeployWizardPage.sourceFiles")); //$NON-NLS-1$
        jobScriptButton.setSelection(true);
        jobScriptButton.setFont(font);
        gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.horizontalSpan = 3;
        jobScriptButton.setLayoutData(gd);

        contextButton = new Button(optionsGroup, SWT.CHECK | SWT.LEFT);
        contextButton.setText(Messages.getString("SpagicDeployWizardPage.contextPerlScripts")); //$NON-NLS-1$
        contextButton.setSelection(true);
        contextButton.setFont(font);

        contextCombo = new Combo(optionsGroup, SWT.PUSH);

        applyToChildrenButton = new Button(optionsGroup, SWT.CHECK | SWT.LEFT);
        applyToChildrenButton.setText(Messages.getString("SpagicDeployWizardPage.ApplyToChildren")); //$NON-NLS-1$
        applyToChildrenButton.setSelection(true);
        // genCodeButton = new Button(optionsGroup, SWT.CHECK | SWT.LEFT);
        // genCodeButton.setText(Messages.getString("JobScriptsExportWizardPage.generatePerlFiles")); //$NON-NLS-1$
        // genCodeButton.setSelection(true);
        // genCodeButton.setFont(font);
    }

    /**
     * Returns a boolean indicating whether the directory portion of the passed pathname is valid and available for use.
     */
    protected boolean ensureTargetDirectoryIsValid(String fullPathname) {
        int separatorIndex = fullPathname.lastIndexOf(File.separator);

        if (separatorIndex == -1) {
            return true;
        }

        return ensureTargetIsValid(new File(fullPathname.substring(0, separatorIndex)));
    }

    /**
     * Returns a boolean indicating whether the passed File handle is is valid and available for use.
     */
    protected boolean ensureTargetFileIsValid(File targetFile) {
        if (targetFile.exists() && targetFile.isDirectory()) {
            // displayErrorDialog(DataTransferMessages.ZipExport_mustBeFile);
            displayErrorDialog(Messages.getString("DataTransferMessages.ZipExport_mustBeFile")); //$NON-NLS-1$
            giveFocusToDestination();
            return false;
        }

        if (targetFile.exists()) {
            if (targetFile.canWrite()) {
                // if (!queryYesNoQuestion(DataTransferMessages.ZipExport_alreadyExists)) {
                if (!queryYesNoQuestion(Messages.getString("DataTransferMessages.ZipExport_alreadyExists"))) { //$NON-NLS-1$
                    return false;
                }
            } else {
                // displayErrorDialog(DataTransferMessages.ZipExport_alreadyExistsError);
                displayErrorDialog(Messages.getString("DataTransferMessages.ZipExport_alreadyExistsError")); //$NON-NLS-1$
                giveFocusToDestination();
                return false;
            }
        }

        return true;
    }

    /**
     * Ensures that the target output file and its containing directory are both valid and able to be used. Answer a
     * boolean indicating validity.
     */
    protected boolean ensureTargetIsValid() {
        String targetPath = getDestinationValue();

        if (!ensureTargetDirectoryIsValid(targetPath)) {
            return false;
        }

        if (!ensureTargetFileIsValid(new File(targetPath))) {
            return false;
        }

        return true;
    }

    /**
     * Export the passed resource and recursively export all of its child resources (iff it's a container). Answer a
     * boolean indicating success.
     */
    protected boolean executeExportOperation(ArchiveFileExportOperationFullPath op) {
        op.setCreateLeadupStructure(true);
        op.setUseCompression(true);

        try {
            getContainer().run(true, true, op);
        } catch (InterruptedException e) {
            return false;
        } catch (InvocationTargetException e) {
            displayErrorDialog(e.getTargetException());
            return false;
        }

        IStatus status = op.getStatus();
        if (!status.isOK()) {
            ErrorDialog.openError(getContainer().getShell(), "", null, // no //$NON-NLS-1$
                    // special
                    // message
                    status);
            return false;
        }

        return true;
    }

    protected JobScriptsManager getManager() {
        if (manager == null) {
            Map<ExportChoice, Object> exportChoiceMap = getExportChoiceMap();
            return new SpagicJavaDeployManager(exportChoiceMap, contextCombo.getText(), "all", IProcessor.NO_STATISTICS, //$NON-NLS-1$
                    IProcessor.NO_TRACES);
        }
        return manager;
    }

    /**
     * The Finish button was pressed. Try to do the required work now and answer a boolean indicating success. If false
     * is returned then the wizard will not close.
     *
     * @returns boolean
     */
    @Override
    public boolean finish() {
        Map<ExportChoice, Object> exportChoiceMap = getExportChoiceMap();
        boolean canExport = false;
        for (ExportChoice choice : ExportChoice.values()) {
            if (exportChoiceMap.get(choice) != null && exportChoiceMap.get(choice) instanceof Boolean
                    && (Boolean) exportChoiceMap.get(choice)) {
                canExport = true;
                break;
            }
        }
        if (!canExport) {
            MessageDialog.openInformation(getContainer().getShell(),
                    Messages.getString("SpagicDeployWizardPage.exportResourceError"), //$NON-NLS-1$
                    Messages.getString("SpagicDeployWizardPage.chooseResource")); //$NON-NLS-1$
            return false;
        }

        if (!ensureTargetIsValid()) {
            return false;
        }
        manager = new SpagicJavaDeployManager(exportChoiceMap, contextCombo.getText(), "all", IProcessor.NO_STATISTICS, //$NON-NLS-1$
                IProcessor.NO_TRACES);

        String topFolder = getRootFolderName();

        List<ExportFileResource> resourcesToExport = null;
        try {
            resourcesToExport = getExportResources();
        } catch (ProcessorException e) {
            MessageBoxExceptionHandler.process(e);
            return false;
        }

        setTopFolder(resourcesToExport, topFolder);

        // Save dirty editors if possible but do not stop if not all are saved
        saveDirtyEditors();
        // about to invoke the operation so save our state
        saveWidgetValues();
        // boolean ok =executeExportOperation(new ArchiveFileExportOperationFullPath(process));
        // File file = createSapgicProperty();
        ArchiveFileExportOperationFullPath exporterOperation = getExporterOperation(resourcesToExport);

        // exportResource(topFolder, "", "", 1);
        boolean ok = executeExportOperation(exporterOperation);

        // path can like name/name
        manager.deleteTempFiles();
        ProcessorUtilities.resetExportConfig();

        String projectName = ((RepositoryContext) CorePlugin.getContext().getProperty(Context.REPOSITORY_CONTEXT_KEY))
                .getProject().getLabel();

        List<JobResource> jobResources = new ArrayList<JobResource>();

        for (ExportFileResource proces : process) {
            try {
                proces.setProcess((ProcessItem) ProxyRepositoryFactory.getInstance()
                        .getUptodateProperty(proces.getItem().getProperty()).getItem());
            } catch (PersistenceException e) {
                e.printStackTrace();
            }
            ProcessItem processItem = (ProcessItem) proces.getItem();
            JobInfo jobInfo = new JobInfo(processItem, processItem.getProcess().getDefaultContext());
            jobResources.add(new JobResource(projectName, jobInfo));

            Set<JobInfo> jobInfos = ProcessorUtilities.getChildrenJobInfo(processItem);
            for (JobInfo subjobInfo : jobInfos) {
                jobResources.add(new JobResource(projectName, subjobInfo));
            }
        }

        JobResourceManager reManager = JobResourceManager.getInstance();
        for (JobResource r : jobResources) {
            if (reManager.isProtected(r)) {
                try {
                    ProcessorUtilities.generateCode(r.getJobInfo().getJobId(), r.getJobInfo().getContextName(), r.getJobInfo()
                            .getJobVersion(), false, false);
                } catch (ProcessorException e) {
                    ExceptionHandler.process(e);
                }
            } else {
                reManager.deleteResource(r);
            }
        }

        // create sapgicdeploy property file

        return ok;
    }

    /**
     * Get the export operation.
     *
     * @param resourcesToExport
     * @return
     */
    protected ArchiveFileExportOperationFullPath getExporterOperation(List<ExportFileResource> resourcesToExport) {
        ArchiveFileExportOperationFullPath exporterOperation = new ArchiveFileExportOperationFullPath(resourcesToExport,
                getDestinationValue());
        return exporterOperation;
    }

    /**
     * Returns the root folder name.
     *
     * @return
     */
    private String getRootFolderName() {
        IPath path = new Path(this.getDestinationValue());
        String subjectString = path.lastSegment();
        Pattern regex = Pattern.compile("(.*)(?=(\\.(tar|zip))\\b)", Pattern.CANON_EQ | Pattern.CASE_INSENSITIVE //$NON-NLS-1$
                | Pattern.UNICODE_CASE);
        Matcher regexMatcher = regex.matcher(subjectString);
        if (regexMatcher.find()) {
            subjectString = regexMatcher.group(0);
        }

        return subjectString.trim();
    }

    protected void setTopFolder(List<ExportFileResource> resourcesToExport, String topFolder) {
        for (ExportFileResource fileResource : resourcesToExport) {
            String directory = fileResource.getDirectoryName();
            fileResource.setDirectoryName(topFolder + "/" + directory); //$NON-NLS-1$
        }
    }

    /**
     * Answer the string to display in self as the destination type.
     *
     * @return java.lang.String
     */
    @Override
    protected String getDestinationLabel() {
        // return DataTransferMessages.ArchiveExport_destinationLabel;
        return Messages.getString("SapgicDeployWizard.FileLabel"); //$NON-NLS-1$
    }

    /**
     * Returns resources to be exported. This returns file - for just the files use getSelectedResources.
     *
     * @return a collection of resources currently selected for export (element type: <code>IResource</code>)
     * @throws ProcessorException
     */
    protected List<ExportFileResource> getExportResources() throws ProcessorException {
        return manager.getExportResources(process);

    }

    protected Map<ExportChoice, Object> getExportChoiceMap() {
        Map<ExportChoice, Object> exportChoiceMap = new EnumMap<ExportChoice, Object>(ExportChoice.class);
        exportChoiceMap.put(ExportChoice.needLauncher, shellLauncherButton.getSelection());
        exportChoiceMap.put(ExportChoice.needSystemRoutine, systemRoutineButton.getSelection());
        exportChoiceMap.put(ExportChoice.needUserRoutine, userRoutineButton.getSelection());
        exportChoiceMap.put(ExportChoice.needTalendLibraries, modelButton.getSelection());
        exportChoiceMap.put(ExportChoice.needJobItem, jobItemButton.getSelection());
        exportChoiceMap.put(ExportChoice.needJobScript, Boolean.TRUE);
        exportChoiceMap.put(ExportChoice.needContext, contextButton.getSelection());
        exportChoiceMap.put(ExportChoice.applyToChildren, applyToChildrenButton.getSelection());
        // exportChoiceMap.put(ExportChoice.needGenerateCode, genCodeButton.getSelection());
        return exportChoiceMap;
    }

    /**
     * Answer the contents of self's destination specification widget. If this value does not have a suffix then add it
     * first.
     */
    @Override
    protected String getDestinationValue() {
        String idealSuffix = getOutputSuffix();
        String destinationText = super.getDestinationValue();

        // only append a suffix if the destination doesn't already have a . in
        // its last path segment.
        // Also prevent the user from selecting a directory. Allowing this will
        // create a ".zip" file in the directory
        if (destinationText.length() != 0 && !destinationText.endsWith(File.separator)) {
            int dotIndex = destinationText.lastIndexOf('.');
            if (dotIndex != -1) {
                // the last path seperator index
                int pathSepIndex = destinationText.lastIndexOf(File.separator);
                if (pathSepIndex != -1 && dotIndex < pathSepIndex) {
                    destinationText += idealSuffix;
                }
            } else {
                destinationText += idealSuffix;
            }
        }

        return destinationText;
    }

    /**
     * Answer the suffix that files exported from this wizard should have. If this suffix is a file extension (which is
     * typically the case) then it must include the leading period character.
     *
     */
    protected String getOutputSuffix() {
        return ".zip"; //$NON-NLS-1$
    }

    /**
     * Open an appropriate destination browser so that the user can specify a source to import from.
     */
    @Override
    protected void handleDestinationBrowseButtonPressed() {
        FileDialog dialog = new FileDialog(getContainer().getShell(), SWT.SAVE);
        dialog.setFilterExtensions(new String[] { "*.zip", "*.*" }); //$NON-NLS-1$ //$NON-NLS-2$
        dialog.setText(""); //$NON-NLS-1$
        String currentSourceString = getDestinationValue();
        int lastSeparatorIndex = currentSourceString.lastIndexOf(File.separator);
        if (lastSeparatorIndex != -1) {
            dialog.setFilterPath(currentSourceString.substring(0, lastSeparatorIndex));
        }
        String selectedFileName = dialog.open();

        if (selectedFileName != null) {
            setErrorMessage(null);
            setDestinationValue(selectedFileName);
        }
    }

    /**
     * Hook method for saving widget values for restoration by the next instance of this class.
     */
    @Override
    protected void internalSaveWidgetValues() {
    }

    /**
     * Hook method for restoring widget values to the values that they held last time this wizard was used to
     * completion.
     */
    @Override
    protected void restoreWidgetValues() {
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.ui.wizards.datatransfer.WizardFileSystemResourceExportPage1#destinationEmptyMessage()
     */
    @Override
    protected String destinationEmptyMessage() {
        return ""; //$NON-NLS-1$
    }

}
