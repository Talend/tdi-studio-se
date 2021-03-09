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
package org.talend.sbi.engines.client.ui.wizards;

import it.eng.spagobi.engines.talend.client.ISpagoBITalendEngineClient;
import it.eng.spagobi.engines.talend.client.JobDeploymentDescriptor;
import it.eng.spagobi.engines.talend.client.SpagoBITalendEngineClient;
import it.eng.spagobi.engines.talend.client.exception.AuthenticationFailedException;
import it.eng.spagobi.engines.talend.client.exception.EngineUnavailableException;
import it.eng.spagobi.engines.talend.client.exception.ServiceInvocationFailedException;
import it.eng.spagobi.engines.talend.client.exception.UnsupportedEngineVersionException;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
import org.talend.commons.ui.runtime.exception.ExceptionHandler;
import org.talend.commons.ui.runtime.exception.MessageBoxExceptionHandler;
import org.talend.commons.ui.swt.formtools.LabelledCombo;
import org.talend.commons.ui.swt.formtools.LabelledText;
import org.talend.core.CorePlugin;
import org.talend.core.context.Context;
import org.talend.core.context.RepositoryContext;
import org.talend.core.model.general.Project;
import org.talend.core.model.properties.ProcessItem;
import org.talend.core.model.properties.SpagoBiServer;
import org.talend.core.model.repository.IRepositoryViewObject;
import org.talend.core.prefs.ITalendCorePrefConstants;
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
import org.talend.sbi.engines.client.i18n.Messages;
import org.talend.sbi.engines.client.ui.preferences.SpagoBiPreferencePage;

/**
 * Page of the Job Scripts Export Wizard. <br/>
 *
 * $Id: PublishOnSpagoExportWizardPage.java 1 2007-04-26 11:29:00 cantoine
 *
 */
public abstract class PublishOnSpagoExportWizardPage extends WizardFileSystemResourceExportPage1 {

    // widgets
    protected Button contextButton;

    protected ExportFileResource[] process;

    protected LabelledCombo serverSpagoBi;

    protected Combo contextCombo;

    protected JobScriptsManager manager;

    protected LabelledText jobLabel;

    protected LabelledText jobName;

    protected LabelledText jobDescription;

    // protected Button spagoVisible;

    protected String jobLabelName;

    protected String jobPurposeDescription;

    /**
     * Create an instance of this class.
     *
     * @param name java.lang.String
     */
    protected PublishOnSpagoExportWizardPage(String name, IStructuredSelection selection) {
        super(name, null);
        RepositoryNode[] nodes = (RepositoryNode[]) selection.toList().toArray(new RepositoryNode[selection.size()]);

        List<ExportFileResource> list = new ArrayList<ExportFileResource>();
        for (int i = 0; i < nodes.length; i++) {
            RepositoryNode node = nodes[i];
            if (node.getType() == ENodeType.SYSTEM_FOLDER || node.getType() == ENodeType.SIMPLE_FOLDER) {
                addTreeNode(node, node.getProperties(EProperties.LABEL).toString(), list);
            }
            if (node.getType() == ENodeType.REPOSITORY_ELEMENT) {
                IRepositoryViewObject repositoryObject = node.getObject();
                if (repositoryObject.getProperty().getItem() instanceof ProcessItem) {
                    ProcessItem processItem = (ProcessItem) repositoryObject.getProperty().getItem();
                    ExportFileResource resource = new ExportFileResource(processItem, processItem.getProperty().getLabel());
                    resource.setNode(node);
                    jobLabelName = processItem.getProperty().getLabel();
                    jobPurposeDescription = processItem.getProperty().getPurpose();
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
        for (int i = 0; i < nodes.length; i++) {
            addTreeNode((RepositoryNode) nodes[i], path + "/" //$NON-NLS-1$
                    + ((RepositoryNode) nodes[i]).getProperties(EProperties.LABEL).toString(), list);
        }
    }

    /**
     * Create an instance of this class.
     *
     * @param selection the selection
     */
    public PublishOnSpagoExportWizardPage(IStructuredSelection selection) {
        this("publishOnSpagoExportPage1", selection); //$NON-NLS-1$
        setDescription(Messages.getString("PublishOnSpagoExportWizardPage.publishJob")); //$NON-NLS-1$
        // setTitle(DataTransferMessages.ArchiveExport_exportTitle);
        setTitle(Messages.getString("DataTransferMessages.ArchiveExport_exportTitle")); //$NON-NLS-1$
    }

    /**
     * (non-Javadoc) Method declared on IDialogPage.
     */
    /**
     * (non-Javadoc) Method declared on IDialogPage.
     */
    public void createControl(Composite parent) {

        initializeDialogUnits(parent);

        Composite composite = new Composite(parent, SWT.NULL);
        composite.setLayout(new GridLayout());
        composite.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_FILL | GridData.HORIZONTAL_ALIGN_FILL));
        composite.setFont(parent.getFont());

        // createDestinationGroup(composite);

        createOptionsGroup(composite);

        restoreResourceSpecificationWidgetValues(); // ie.- local
        restoreWidgetValues(); // ie.- subclass hook

        updateWidgetEnablements();
        setPageComplete(determinePageCompletion());
        setErrorMessage(null); // should not initially have error message

        setControl(composite);
        // giveFocusToDestination();
    }

    /*
     * It's not a good method to resovle the problem of null pointer, which is led by commenting the //
     * createResourcesGroup(composite); and createButtonsGroup(composite); (non-Javadoc)
     *
     * @see org.eclipse.ui.internal.wizards.datatransfer.WizardFileSystemResourceExportPage1#validateSourceGroup()
     */
    protected boolean validateSourceGroup() {
        return true;
    }

    /**
     * Create the export options specification widgets.
     *
     */
    protected void createOptionsGroupButtons(Group optionsGroup) {
        Font font = optionsGroup.getFont();
        optionsGroup.setLayout(new GridLayout(1, true));

        Composite left = new Composite(optionsGroup, SWT.NONE);
        left.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, true, false));
        left.setLayout(new GridLayout(2, true));

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
        List<SpagoBiServer> listServerSapgo = null;
        List<String> listEngine = new ArrayList<String>();

        // TODO SML remove unused lines if this fabulous code works fine
        // ProxyRepositoryFactory proxyRepositoryFactory = ProxyRepositoryFactory.getInstance();
        // try {
        // listServerSapgo = proxyRepositoryFactory.getSpagoBiServer();
        listServerSapgo = SpagoBiServerHelper.parse(new SpagoBiPreferencePage().getPreferenceStore().getString(
                SpagoBiServer.SPAGOBI_SERVER));
        if (listServerSapgo != null && !listServerSapgo.isEmpty()) {
            Iterator<SpagoBiServer> iterator = listServerSapgo.iterator();
            while (iterator.hasNext()) {
                SpagoBiServer spagoBiServer = iterator.next();
                listEngine.add(spagoBiServer.getEngineName());
            }
        }
        // } catch (PersistenceException e) {
        // displayErrorDialog(e.getMessage());
        // }

        serverSpagoBi = new LabelledCombo(
                optionsGroup,
                Messages.getString("PublishOnSpagoExportWizardPage.SpagoBI.Server"), Messages.getString("PublishOnSpagoExportWizardPage.SpecifyServer.PublishJob"), listEngine); //$NON-NLS-1$ //$NON-NLS-2$
        serverSpagoBi.select(0);

        jobLabel = new LabelledText(optionsGroup, Messages.getString("PublishOnSpagoExportWizardPage.jobLabel"), true); //$NON-NLS-1$
        jobLabel.setText(jobLabelName);

        jobName = new LabelledText(optionsGroup, Messages.getString("PublishOnSpagoExportWizardPage.jobName"), true); //$NON-NLS-1$
        jobName.setText(jobLabelName);

        jobDescription = new LabelledText(optionsGroup, Messages.getString("PublishOnSpagoExportWizardPage.jobDescription"), true); //$NON-NLS-1$
        jobDescription.setText(jobPurposeDescription);

        contextButton = new Button(optionsGroup, SWT.CHECK | SWT.LEFT);
        contextButton.setText(Messages.getString("JobScriptsExportWizardPage.contextPerlScripts")); //$NON-NLS-1$
        contextButton.setSelection(true);
        contextButton.setFont(font);

        contextCombo = new Combo(optionsGroup, SWT.PUSH);

        // spagoVisible = new Button(optionsGroup, SWT.CHECK | SWT.LEFT);
        // spagoVisible.setText(Messages.getString("PublishOnSpagoExportWizardPage.spagoVisible")); //$NON-NLS-1$
        // spagoVisible.setSelection(true);
        // spagoVisible.setFont(font);

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
            if (!targetFile.canWrite()) {
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

    /**
     * The Finish button was pressed. Try to do the required work now and answer a boolean indicating success. If false
     * is returned then the wizard will not close.
     *
     * @returns boolean
     */
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
                    Messages.getString("PublishOnSpagoExportWizardPage.publishResourceError"), //$NON-NLS-1$
                    Messages.getString("PublishOnSpagoExportWizardPage.chooseResource")); //$NON-NLS-1$
            return false;
        }

        if (!ensureTargetIsValid()) {
            return false;
        }
        String topFolder = getRootFolderName();
        manager = new JobJavaScriptsManager(exportChoiceMap, contextCombo.getText(), "all", IProcessor.NO_STATISTICS, //$NON-NLS-1$
                IProcessor.NO_TRACES);

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
        // boolean ok = executeExportOperation(new ArchiveFileExportOperationFullPath(process));
        ArchiveFileExportOperationFullPath exporterOperation = getExporterOperation(resourcesToExport);
        boolean ok = executeExportOperation(exporterOperation);

        // path can like name/name
        manager.deleteTempFiles();

        ProcessorUtilities.resetExportConfig();
        for (int i = 0; i < process.length; i++) {
            ProcessItem processItem = (ProcessItem) process[i].getItem();
            try {
                ProcessorUtilities.generateCode(processItem, processItem.getProcess().getDefaultContext(), false, false);
            } catch (ProcessorException e) {
                ExceptionHandler.process(e);
            }

        }

        // cantoine : connection to SpagoBiEngineClient to publish Job.
        try {

            Project project = ((RepositoryContext) CorePlugin.getContext().getProperty(Context.REPOSITORY_CONTEXT_KEY))
                    .getProject();

            // retrieve user, password, host, port from selected SpagoBiServer

            String selectedSpagoBiEngineName = serverSpagoBi.getItem(serverSpagoBi.getSelectionIndex());
            SpagoBiServer spagoBiServer = null;

            List<SpagoBiServer> listServerSapgo = null;

            // ProxyRepositoryFactory proxyRepositoryFactory = ProxyRepositoryFactory.getInstance();
            // try {
            listServerSapgo = SpagoBiServerHelper.parse(new SpagoBiPreferencePage().getPreferenceStore().getString(
                    SpagoBiServer.SPAGOBI_SERVER));
            if (listServerSapgo != null && !listServerSapgo.isEmpty()) {
                Iterator<SpagoBiServer> iterator = listServerSapgo.iterator();
                while (iterator.hasNext()) {
                    spagoBiServer = iterator.next();
                    if (spagoBiServer.getEngineName().equals(selectedSpagoBiEngineName)) {
                        break;
                    }
                }
            }
            // } catch (PersistenceException e) {
            // displayErrorDialog(e.getMessage());
            // }

            String user = spagoBiServer.getLogin();// "biadmin";
            String password = spagoBiServer.getPassword();// "biadmin";
            String host = spagoBiServer.getHost();
            String port = spagoBiServer.getPort();

            // create the client
            ISpagoBITalendEngineClient client = new SpagoBITalendEngineClient(user, password, host, port, "SpagoBITalendEngine"); //$NON-NLS-1$

            // get some informations about the engine instance referenced by the client
            System.out.println("Engine version: " + client.getEngineVersion()); //$NON-NLS-1$
            System.out.println("Engine fullname: " + client.getEngineName()); //$NON-NLS-1$

            // prepare parameters used during deployment
            JobDeploymentDescriptor jobDeploymentDescriptor = new JobDeploymentDescriptor(project.getLabel(), project
                    .getLanguage().getName());
            File zipFile = new File(getDestinationValue());

            // deploy job on engine runtime
            boolean result = client.deployJob(jobDeploymentDescriptor, zipFile);
            if (result)
                System.out.println("Jobs deployed succesfully"); //$NON-NLS-1$
            else
                System.out.println("Jobs not deployed"); //$NON-NLS-1$

        } catch (EngineUnavailableException e) {
            System.err.println("ERROR: " + e.getMessage()); //$NON-NLS-1$
        } catch (AuthenticationFailedException e) {
            System.err.println("ERROR: " + e.getMessage()); //$NON-NLS-1$
        } catch (UnsupportedEngineVersionException e) {
            System.err.println("ERROR: Unsupported engine version"); //$NON-NLS-1$
            System.err.println("You are using TalendEngineClientAPI version " //$NON-NLS-1$
                    + SpagoBITalendEngineClient.CLIENTAPI_VERSION_NUMBER + ". " //$NON-NLS-1$
                    + "The TalendEngine instance you are trying to connect to require TalendEngineClientAPI version " //$NON-NLS-1$
                    + e.getComplianceVersion() + " or grater."); //$NON-NLS-1$
        } catch (ServiceInvocationFailedException e) {
            System.err.println("ERROR: " + e.getMessage()); //$NON-NLS-1$
            System.err.println("StatusLine: " + e.getStatusLine() + "responseBody: " + e.getResponseBody()); //$NON-NLS-1$ //$NON-NLS-2$
        }

        return ok;
        // return true;
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

    private void setTopFolder(List<ExportFileResource> resourcesToExport, String topFolder) {
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
    protected String getDestinationLabel() {
        // return DataTransferMessages.ArchiveExport_destinationLabel;
        return Messages.getString("DataTransferMessages.ArchiveExport_destinationLabel"); //$NON-NLS-1$
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
        // exportChoiceMap.put(ExportChoice.needLauncher, shellLauncherButton.getSelection());
        // exportChoiceMap.put(ExportChoice.needSystemRoutine, systemRoutineButton.getSelection());
        // exportChoiceMap.put(ExportChoice.needUserRoutine, userRoutineButton.getSelection());
        // exportChoiceMap.put(ExportChoice.needTalendLibraries, modelButton.getSelection());
        // exportChoiceMap.put(ExportChoice.needJob, jobButton.getSelection());
        // exportChoiceMap.put(ExportChoice.needSource, sourceButton.getSelection());
        // exportChoiceMap.put(ExportChoice.needContext, contextButton.getSelection());

        exportChoiceMap.put(ExportChoice.needLauncher, true);
        exportChoiceMap.put(ExportChoice.needSystemRoutine, true);
        exportChoiceMap.put(ExportChoice.needUserRoutine, true);
        exportChoiceMap.put(ExportChoice.needTalendLibraries, true);
        exportChoiceMap.put(ExportChoice.needJobItem, true);
        exportChoiceMap.put(ExportChoice.needJobScript, true);
        exportChoiceMap.put(ExportChoice.needSourceCode, false);

        exportChoiceMap.put(ExportChoice.needContext, contextButton.getSelection());

        return exportChoiceMap;
    }

    /**
     * Answer the contents of self's destination specification widget. If this value does not have a suffix then add it
     * first.
     */
    protected String getDestinationValue() {
        String idealSuffix = getOutputSuffix();

        String filename = "SpagoBi" + idealSuffix; //$NON-NLS-1$
        IPath tempPath;
        tempPath = Path.fromOSString(CorePlugin.getDefault().getPreferenceStore()
                .getString(ITalendCorePrefConstants.FILE_PATH_TEMP));
        tempPath = tempPath.append(filename);
        return tempPath.toOSString();
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
    protected void internalSaveWidgetValues() {
    }

    /**
     * Hook method for restoring widget values to the values that they held last time this wizard was used to
     * completion.
     */
    protected void restoreWidgetValues() {
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.ui.wizards.datatransfer.WizardFileSystemResourceExportPage1#destinationEmptyMessage()
     */
    protected String destinationEmptyMessage() {
        return ""; //$NON-NLS-1$
    }

    protected void createOptionsGroup(Composite parent) {
        // options group
        Group optionsGroup = new Group(parent, SWT.NONE);
        GridLayout layout = new GridLayout();
        optionsGroup.setLayout(layout);
        optionsGroup.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL));
        optionsGroup.setText(Messages.getString("PublishOnSpagoExportWizardPage.Settings")); //$NON-NLS-1$
        optionsGroup.setFont(parent.getFont());
        createOptionsGroupButtons(optionsGroup);

    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.ui.internal.wizards.datatransfer.WizardFileSystemResourceExportPage1#validateDestinationGroup()
     */
    @Override
    protected boolean validateDestinationGroup() {
        return true;
    }

}
