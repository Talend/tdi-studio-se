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
package org.talend.designer.documentation.generation.wizards;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.ui.internal.wizards.datatransfer.WizardFileSystemResourceExportPage1;
import org.talend.commons.ui.swt.formtools.LabelledFileField;
import org.talend.core.CorePlugin;
import org.talend.core.model.properties.ProcessItem;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.model.repository.IRepositoryViewObject;
import org.talend.core.model.repository.documentation.generation.JobHTMLScriptsManager;
import org.talend.core.prefs.ITalendCorePrefConstants;
import org.talend.core.ui.export.ArchiveFileExportOperationFullPath;
import org.talend.designer.documentation.generation.HTMLDocGenerator;
import org.talend.repository.documentation.ExportFileResource;
import org.talend.repository.i18n.Messages;
import org.talend.repository.model.IRepositoryNode.ENodeType;
import org.talend.repository.model.IRepositoryNode.EProperties;
import org.talend.repository.model.RepositoryNode;

/**
 * Page of the Job Scripts Export Wizard. <br/>
 *
 * @referto WizardArchiveFileResourceExportPage1 $Id: JobScriptsExportWizardPage.java 1 2006-12-13 下午03:09:07 bqian
 *
 */
public class GenerateDocAsHTMLWizardPage extends WizardFileSystemResourceExportPage1 {

    private ExportFileResource[] process;

    private JobHTMLScriptsManager manager;

    private RepositoryNode[] nodes;

    private Button button;

    private LabelledFileField cssField;

    private String cssFilePath;

    // dialog store id constants
    private static final String STORE_DESTINATION_NAMES_ID = "GenerateDocAsHTMLWizardPage.STORE_DESTINATION_NAMES_ID"; //$NON-NLS-1$

    private static final String DESTINATION_FILE = "destinationFile";//$NON-NLS-1$

    /**
     * Create an instance of this class.
     *
     * @param name java.lang.String
     */
    protected GenerateDocAsHTMLWizardPage(String name, IStructuredSelection selection) {
        super(name, null);
        nodes = (RepositoryNode[]) selection.toList().toArray(new RepositoryNode[selection.size()]);
        if (nodes.length > 0) {

            manager = new JobHTMLScriptsManager(new HTMLDocGenerator(nodes[0].getRoot().getProject(),
                    ERepositoryObjectType.JOB_DOC, ERepositoryObjectType.PROCESS), true);
        }

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
     * Create an instance of this class.
     *
     * @param selection the selection
     */
    public GenerateDocAsHTMLWizardPage(IStructuredSelection selection) {
        this("generateDocAsHTMLPage1", selection); //$NON-NLS-1$
        setDescription(Messages.getString("GenerateDocAsHTMLWizardPage.generateDocAsHTML"));//$NON-NLS-1$
        // setTitle(DataTransferMessages.ArchiveExport_exportTitle);
        setTitle(Messages.getString("DataTransferMessages.ArchiveExport_exportTitle")); //$NON-NLS-1$
    }

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

        createCssDestinationGroup(composite);
        addDocumentListener();

        restoreResourceSpecificationWidgetValues(); // ie.- local
        restoreWidgetValues(); // ie.- subclass hook

        updateWidgetEnablements();
        setPageComplete(determinePageCompletion());
        setErrorMessage(null); // should not initially have error message

        setControl(composite);
        giveFocusToDestination();
    }

    protected void createCssDestinationGroup(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        composite.setLayout(new GridLayout());
        boolean isCheck = CorePlugin.getDefault().getPreferenceStore().getBoolean(ITalendCorePrefConstants.USE_CSS_TEMPLATE);
        button = new Button(composite, SWT.CHECK);
        button.setText(Messages.getString("GenerateDocAsHTMLWizardPage.custom_css")); //$NON-NLS-1$
        button.setSelection(isCheck);
        new Label(composite, SWT.NONE).setText(Messages.getString("GenerateDocAsHTMLWizardPage.default_css_template")); //$NON-NLS-1$

        Composite composite1 = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout();
        layout.numColumns = 3;
        composite1.setLayout(layout);
        composite1.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        String value = CorePlugin.getDefault().getPreferenceStore().getString(ITalendCorePrefConstants.CSS_FILE_PATH);
        if (isCheck && value != null && !value.equals("")) { //$NON-NLS-1$
            cssFilePath = value;
        }
        String[] cssExtensions = { "*.css", "*.*" }; //$NON-NLS-1$ //$NON-NLS-2$
        cssField = new LabelledFileField(composite1, Messages.getString("GenerateDocAsHTMLWizardPage.css_file"), cssExtensions); //$NON-NLS-1$
        cssField.setEditable(button.getSelection());
        cssField.setText(value);
    }

    private void addDocumentListener() {
        button.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                cssField.setEditable(button.getSelection());
                if (button.getSelection()) {
                    cssFilePath = cssField.getText();
                }
                updatePageCompletion();
            }

        });
        cssField.addModifyListener(new ModifyListener() {

            @Override
            public void modifyText(ModifyEvent e) {
                cssFilePath = cssField.getText();
                updatePageCompletion();
            }
        });
    }

    @Override
    protected boolean validateDestinationGroup() {
        if (!button.getSelection()) {
            return super.validateDestinationGroup();
        } else {
            return checkCssFieldValue() && super.validateDestinationGroup();
        }
    }

    private boolean checkCssFieldValue() {
        if (cssFilePath == null || cssFilePath.equals("")) { //$NON-NLS-1$
            return true;
        } else {
            if (cssFilePath.contains(".")) { //$NON-NLS-1$
                int lastIndexOf = cssFilePath.lastIndexOf("."); //$NON-NLS-1$
                String extend = cssFilePath.substring(lastIndexOf + 1, cssFilePath.length());
                if (!extend.equalsIgnoreCase("css")) { //$NON-NLS-1$
                    setErrorMessage(Messages.getString("GenerateDocAsHTMLWizardPage.must_css_file")); //$NON-NLS-1$
                    return false;
                }
            }
            File cssFile = new File(cssFilePath);
            if (!cssFile.exists()) {
                setErrorMessage(Messages.getString("GenerateDocAsHTMLWizardPage.must_existing_file")); //$NON-NLS-1$
                return false;
            }

        }
        setErrorMessage(null);
        return true;

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

    /**
     * The Finish button was pressed. Try to do the required work now and answer a boolean indicating success. If false
     * is returned then the wizard will not close.
     *
     * @returns boolean
     */
    @Override
    public boolean finish() {
        String topFolder = getRootFolderName();
        String destinationFilename = getDestinationValue();
        // Save dirty editors if possible but do not stop if not all are saved
        saveDirtyEditors();
        // about to invoke the operation so save our state
        saveWidgetValues();
        File file = new File(destinationFilename);
        if (file.exists()) {
            Boolean open = MessageDialog.openConfirm(getShell(), Messages.getString("GenerateDocAsHTMLWizardPage.Warning"), //$NON-NLS-1$
                    Messages.getString("GenerateDocAsHTMLWizardPage.OverWrite")); //$NON-NLS-1$
            if (open) {
                file.delete();
            } else {
                return false;
            }
        }
        List<ExportFileResource> resourcesToExport = getExportResources();
        setTopFolder(resourcesToExport, topFolder);
        ArchiveFileExportOperationFullPath runnable = new ArchiveFileExportOperationFullPath(resourcesToExport,
                destinationFilename);
        // output everything
        runnable.setRegEx("*");//$NON-NLS-1$

        boolean ok = executeExportOperation(runnable);
        // add for bug TDI-21815
        saveLastDirectoryName(runnable);
        manager.deleteTempFiles();

        return ok;
    }

    /**
     * Save the last directoryName path .
     *
     * @param runnable
     */
    private void saveLastDirectoryName(ArchiveFileExportOperationFullPath runnable) {
        IDialogSettings settings = getDialogSettings();
        if (settings != null) {
            String[] directoryNames = settings.getArray(STORE_DESTINATION_NAMES_ID);
            if (directoryNames != null) {
                boolean isExist = false;
                for (String directoryName : directoryNames) {
                    if (directoryName.equals(runnable.getDestinationFilename())) {
                        isExist = true;
                    }
                }
                if (!isExist) {
                    String[] newDirectoryNames = Arrays.copyOf(directoryNames, directoryNames.length + 1);
                    newDirectoryNames[newDirectoryNames.length - 1] = runnable.getDestinationFilename();
                    settings.put(STORE_DESTINATION_NAMES_ID, newDirectoryNames);
                }
            } else {
                settings.put(STORE_DESTINATION_NAMES_ID, new String[] { runnable.getDestinationFilename() });
            }
        }
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
    @Override
    protected String getDestinationLabel() {
        // return DataTransferMessages.ArchiveExport_destinationLabel;
        return Messages.getString("DataTransferMessages.ArchiveExport_destinationLabel"); //$NON-NLS-1$
    }

    /**
     * Returns resources to be exported. This returns file - for just the files use getSelectedResources.
     *
     * @return a collection of resources currently selected for export (element type: <code>IResource</code>)
     */
    protected List<ExportFileResource> getExportResources() {
        return manager.getExportResourcesWithCss(process, cssFilePath);
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
        dialog.setFileName(getDefaultFileName());
        String currentSourceString = getDestinationValue();
        int lastSeparatorIndex = currentSourceString.lastIndexOf(File.separator);
        if (lastSeparatorIndex != -1) {
            dialog.setFilterPath(currentSourceString.substring(0, lastSeparatorIndex));
        }
        String selectedFileName = dialog.open();

        if (selectedFileName != null) {
            setErrorMessage(null);
            setDestinationValue(selectedFileName);
            if (getDialogSettings() != null) {
                IDialogSettings section = getDialogSettings().getSection(DESTINATION_FILE);
                if (section == null) {
                    section = getDialogSettings().addNewSection(DESTINATION_FILE);
                }
                section.put(DESTINATION_FILE, selectedFileName);
            }
        }
    }

    @Override
    public void handleEvent(Event e) {
        super.handleEvent(e);
        Widget source = e.widget;
        if (source instanceof Combo) {
            String destination = ((Combo) source).getText();
            if (getDialogSettings() != null) {
                IDialogSettings section = getDialogSettings().getSection(DESTINATION_FILE);
                if (section == null) {
                    section = getDialogSettings().addNewSection(DESTINATION_FILE);
                }
                section.put(DESTINATION_FILE, destination);
            }
        }
    }

    /**
     * Hook method for saving widget values for restoration by the next instance of this class.
     */
    @Override
    protected void internalSaveWidgetValues() {
        // update directory names history
        IDialogSettings settings = getDialogSettings();
        if (settings != null) {
            String[] directoryNames = settings.getArray(STORE_DESTINATION_NAMES_ID);
            if (directoryNames == null) {
                directoryNames = new String[0];
            }
            directoryNames = addToHistory(directoryNames, getDestinationValue());
        }
    }

    /**
     * Hook method for restoring widget values to the values that they held last time this wizard was used to
     * completion.
     */
    // protected void restoreWidgetValues() {
    // IDialogSettings settings = getDialogSettings();
    // if (settings != null) {
    // String[] directoryNames = settings.getArray(STORE_DESTINATION_NAMES_ID);
    // if (directoryNames != null) {
    // // destination
    // setDestinationValue(directoryNames[0]);
    // for (int i = 0; i < directoryNames.length; i++) {
    // addDestinationItem(directoryNames[i]);
    // }
    // }
    // }
    // }
    @Override
    protected void restoreWidgetValues() {
        IDialogSettings settings = getDialogSettings();
        if (settings != null) {
            String[] directoryNames = settings.getArray(STORE_DESTINATION_NAMES_ID);
            boolean destinationValid = false;
            if (directoryNames != null) {
                // destination
                boolean isFirstValid = false;
                String filterName = ".zip"; //$NON-NLS-1$
                String userDir = System.getProperty("user.dir"); //$NON-NLS-1$
                IPath path = new Path(userDir).append(getDefaultFileName() + getOutputSuffix());
                for (String directoryName : directoryNames) {
                    addDestinationItem(directoryName);
                    if (directoryName.substring(directoryName.lastIndexOf('.')).equalsIgnoreCase(filterName)
                            && path.toOSString().equals(directoryName)) {
                        if (!isFirstValid) {
                            setDestinationValue(directoryName);
                            isFirstValid = true;
                        }
                        destinationValid = true;
                    }
                }
            }

            if (!destinationValid || directoryNames == null) {
                setDefaultDestination();
            }

        }
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

    /**
     * yzhang Comment method "setDefaultDestination".
     */
    protected void setDefaultDestination() {

        String destinationFile = ""; //$NON-NLS-1$
        IPath path = null;
        // if (getDialogSettings() != null) {
        // IDialogSettings section = getDialogSettings().getSection(DESTINATION_FILE);
        // if (section != null) {
        // destinationFile = section.get(DESTINATION_FILE);
        // }
        // }
        if (destinationFile == null || "".equals(destinationFile)) { //$NON-NLS-1$
            if (nodes.length >= 1) {
                String userDir = System.getProperty("user.dir"); //$NON-NLS-1$
                path = new Path(userDir).append(getDefaultFileName() + getOutputSuffix());
            }
        } else {
            path = new Path(destinationFile);
        }
        if (path != null) {
            setDestinationValue(path.toOSString());
        }
    }

    /**
     * yzhang Comment method "getDefaultFileName".
     */
    protected String getDefaultFileName() {
        if (nodes.length >= 1) {
            String label = null;
            String version = null;
            RepositoryNode node = nodes[0];
            if (node.getType() == ENodeType.SYSTEM_FOLDER || node.getType() == ENodeType.SIMPLE_FOLDER) {
                label = node.getProperties(EProperties.LABEL).toString();
            } else if (node.getType() == ENodeType.REPOSITORY_ELEMENT) {
                IRepositoryViewObject repositoryObject = node.getObject();
                if (repositoryObject.getProperty().getItem() instanceof ProcessItem) {
                    ProcessItem processItem = (ProcessItem) repositoryObject.getProperty().getItem();
                    label = processItem.getProperty().getLabel();
                    version = processItem.getProperty().getVersion();
                }
            }
            if (version != null) {
                return label + "_" + version; //$NON-NLS-1$
            } else {
                return label;
            }
        }
        return ""; //$NON-NLS-1$
    }

    @Override
    public void setVisible(boolean visible) {
        super.setVisible(visible);
        if (visible && button.getSelection()) {
            checkCssFieldValue();
        }
    }
}
