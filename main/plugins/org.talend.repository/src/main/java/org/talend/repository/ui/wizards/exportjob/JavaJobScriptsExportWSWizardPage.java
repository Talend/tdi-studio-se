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
package org.talend.repository.ui.wizards.exportjob;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.util.BidiUtils;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.ui.internal.wizards.datatransfer.DataTransferMessages;
import org.talend.commons.exception.PersistenceException;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.PluginChecker;
import org.talend.core.model.process.IContext;
import org.talend.core.model.properties.ProcessItem;
import org.talend.core.model.repository.IRepositoryViewObject;
import org.talend.core.repository.constants.FileConstants;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.core.runtime.process.TalendProcessArgumentConstant;
import org.talend.core.service.IESBMicroService;
import org.talend.core.ui.branding.IBrandingService;
import org.talend.designer.core.model.utils.emf.talendfile.NodeType;
import org.talend.designer.maven.model.TalendMavenConstants;
import org.talend.designer.maven.tools.AggregatorPomsHelper;
import org.talend.designer.maven.utils.PomIdsHelper;
import org.talend.designer.maven.utils.PomUtil;
import org.talend.designer.runprocess.IProcessor;
import org.talend.repository.constants.BuildJobConstants;
import org.talend.repository.i18n.Messages;
import org.talend.repository.model.RepositoryNode;
import org.talend.repository.ui.utils.Log4jPrefsSettingManager;
import org.talend.repository.ui.wizards.exportjob.scriptsmanager.JobScriptsManager;
import org.talend.repository.ui.wizards.exportjob.scriptsmanager.JobScriptsManager.ExportChoice;
import org.talend.repository.ui.wizards.exportjob.scriptsmanager.JobScriptsManagerFactory;
import org.talend.repository.ui.wizards.exportjob.util.ExportJobUtil;
import org.talend.repository.utils.EmfModelUtils;

/**
 * DOC x class global comment. Detailled comment <br/>
 *
 */
public class JavaJobScriptsExportWSWizardPage extends JavaJobScriptsExportWizardPage {

    /**
     * type of job exports.
     */
    public static enum JobExportType {

        POJO(Messages.getString("JavaJobScriptsExportWSWizardPage.POJO"), false), //$NON-NLS-1$
        OSGI(Messages.getString("JavaJobScriptsExportWSWizardPage.OSGI"), false), //$NON-NLS-1$
        MSESB(Messages.getString("JavaJobScriptsExportWSWizardPage.MSESB"), false), //$NON-NLS-1$
        MSESB_IMAGE(Messages.getString("JavaJobScriptsExportWSWizardPage.MSESB_IMAGE"), false), //$NON-NLS-1$
        IMAGE(Messages.getString("JavaJobScriptsExportWSWizardPage.IMAGE"), false), //$NON-NLS-1$
        ROUTE(Messages.getString("JavaJobScriptsExportWSWizardPage.ROUTE"), false), //$NON-NLS-1$
        SERVICE(Messages.getString("JavaJobScriptsExportWSWizardPage.SERVICE"), false); //$NON-NLS-1$

        public final String label;

        public final boolean deprecate;

        private JobExportType(String label, boolean deprecate) {
            this.label = label;
            this.deprecate = deprecate;
        }

        /**
         * return the type according to the label or the POJO type if no match.
         */
        public static JobExportType getTypeFromLabel(String label) {
            for (JobExportType type : JobExportType.values()) {
                if (type.label.equals(label)) {
                    return type;
                }
            }
            return POJO;
        }

        /**
         * return the type according to the type string, then try the label string or the POJO type if no match
         */
        public static JobExportType getTypeFromString(String str) {
            if (str == null) {
                return POJO;
            } else {
                try {
                    return JobExportType.valueOf(str);
                } catch (IllegalArgumentException iae) {// for compatibility try the label also
                    return JobExportType.getTypeFromLabel(str);
                }
            }
        }

        public static boolean isImageType(JobExportType type) {
            return type == IMAGE || type == MSESB_IMAGE;
        }

    }

    public static final String ESBTYPE_JBOSS_MQ = "JBoss MQ"; //$NON-NLS-1$

    public static final String ESBTYPE_JBOSS_MESSAGING = "JBoss Messaging"; //$NON-NLS-1$

    protected Combo exportTypeCombo;

    protected Combo esbTypeCombo;

    protected ScrolledComposite scrolledComposite;

    protected Composite pageComposite;

    protected Composite optionsGroupComposite;

    protected Button webXMLButton;

    protected Button configFileButton;

    protected Button axisLibButton;

    protected Button wsddButton;

    protected Button wsdlButton;

    protected Button chkButton;

    protected Button singletonButton;

    protected Button generateEndpointButton;

    protected Button sourceButton;

    protected Button validateByWsdlButton;

    protected Text esbQueueMessageName;

    protected Text esbServiceName;

    protected Text esbCategory;

    public static final String STORE_EXPORTTYPE_ID = "JavaJobScriptsExportWizardPage.STORE_EXPORTTYPE_ID"; //$NON-NLS-1$

    public static final String STORE_WEBXML_ID = "JavaJobScriptsExportWizardPage.STORE_WEBXML_ID"; //$NON-NLS-1$

    public static final String STORE_CONFIGFILE_ID = "JavaJobScriptsExportWizardPage.STORE_CONFIGFILE_ID"; //$NON-NLS-1$

    public static final String STORE_AXISLIB_ID = "JavaJobScriptsExportWizardPage.STORE_AXISLIB_ID"; //$NON-NLS-1$

    public static final String STORE_WSDD_ID = "JavaJobScriptsExportWizardPage.STORE_WSDD_ID"; //$NON-NLS-1$

    public static final String STORE_WSDL_ID = "JavaJobScriptsExportWizardPage.STORE_WSDL_ID"; //$NON-NLS-1$

    public static final String EXTRACT_ZIP_FILE = "JavaJobScriptsExportWizardPage.EXTRACT_ZIP_FILE"; //$NON-NLS-1$

    protected JobExportType exportTypeFixed;

    public static final String PETALS_EXPORT_DESTINATIONS = "org.ow2.petals.esbexport.destinations"; //$NON-NLS-1$

    JavaJobScriptsExportWSWizardPagePresenter presenter = new JavaJobScriptsExportWSWizardPagePresenter(this);

    private Label destinationLabel;

    private Combo destinationNameField;

    private Button destinationBrowseButton;

    private Button localRadio, remoteRadio;

    private Text hostText, imageText, tagText;

    private Label hostLabel, imageLabel, tagLabel;

    private boolean isValid;

    public JavaJobScriptsExportWSWizardPage(IStructuredSelection selection, String exportType) {
        super(selection);
        // there assign the manager again
        exportTypeFixed = exportType != null ? JobExportType.getTypeFromString(exportType) : null;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.jface.wizard.WizardPage#setWizard(org.eclipse.jface.wizard.IWizard)
     */
    @Override
    public void setWizard(IWizard newWizard) {
        super.setWizard(newWizard);
        initialiseDefaultDialogSettings();
    }

    /**
     * this set default dialog settings if none already exists.
     */
    private void initialiseDefaultDialogSettings() {
        IDialogSettings dialogSettings = getDialogSettings();
        if (dialogSettings != null) {
            // set default export type according to system properties
            String exportType = dialogSettings.get(STORE_EXPORTTYPE_ID);
            String defaultExportType = System.getProperty("talend.export.job.default.type"); //$NON-NLS-1$
            if ((exportType == null || exportType.equals("")) && defaultExportType != null //$NON-NLS-1$
                    && !defaultExportType.equals("")) { //$NON-NLS-1$
                dialogSettings.put(STORE_EXPORTTYPE_ID, defaultExportType);
            }
        } // else ignors it
    }

    @Override
    protected void createDestinationGroup(Composite parent) {
        Font font = parent.getFont();
        // destination specification group
        Composite destinationSelectionGroup = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout();
        layout.numColumns = 3;
        destinationSelectionGroup.setLayout(layout);
        destinationSelectionGroup
                .setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_FILL));
        destinationSelectionGroup.setFont(font);

        destinationLabel = new Label(destinationSelectionGroup, SWT.NONE);
        destinationLabel.setText(getDestinationLabel());
        destinationLabel.setFont(font);

        // destination name entry field
        destinationNameField = new Combo(destinationSelectionGroup, SWT.SINGLE | SWT.BORDER);
        destinationNameField.addListener(SWT.Modify, this);
        destinationNameField.addListener(SWT.Selection, this);
        GridData data = new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL);
        data.widthHint = SIZING_TEXT_FIELD_WIDTH;
        destinationNameField.setLayoutData(data);
        destinationNameField.setFont(font);
        BidiUtils.applyBidiProcessing(destinationNameField, "file"); //$NON-NLS-1$

        // destination browse button
        destinationBrowseButton = new Button(destinationSelectionGroup, SWT.PUSH);
        destinationBrowseButton.setText(DataTransferMessages.DataTransfer_browse);
        destinationBrowseButton.addListener(SWT.Selection, this);
        destinationBrowseButton.setFont(font);
        setButtonLayoutData(destinationBrowseButton);

        new Label(parent, SWT.NONE); // vertical spacer
    }

    /**
     * Extracts all types of job for exporting, about the exporting job type, please refer to {@link JobExportType}.
     * Subclasses can override this method to return the types that job requires for exporting. Added by Marvin Wang on
     * Mar 6, 2013.
     *
     * @return
     */
    protected List<JobExportType> extractExportJobTypes() {
        // Feature TDI-29084:put the Deprecated build type at last
        List<JobExportType> deprecateTypeList = new ArrayList<>();
        List<JobExportType> typeList = new ArrayList<>();
        for (JobExportType type : JobExportType.values()) {
            if (!type.deprecate) {
                typeList.add(type);
            } else {
                deprecateTypeList.add(type);
            }
        }
        typeList.addAll(deprecateTypeList);
        return typeList;
    }

    @Override
    public JobExportType getCurrentExportType1() {
        if (exportTypeCombo != null && !exportTypeCombo.getText().equals("")) { //$NON-NLS-1$
            return JobExportType.getTypeFromLabel(exportTypeCombo.getText());
        } else {
            IDialogSettings settings = getDialogSettings();
            if (settings != null && settings.get(STORE_EXPORTTYPE_ID) != null) {
                JobExportType type = JobExportType.getTypeFromString(settings.get(STORE_EXPORTTYPE_ID));
                for (JobExportType exportType : extractExportJobTypes()) {
                    if (!Boolean.getBoolean("talend.export.job.2." + exportType.toString() + ".hide")) { //$NON-NLS-1$//$NON-NLS-2$
                        if (type.equals(exportType)) {
                            return type; // check if at least the type is included in the list or not, if not, just get
                                         // the default one (POJO)
                        }
                    }
                }
            }
        }
        return JobExportType.POJO;
    }

    @Override
    protected ExportTreeViewer getExportTree() {
        return new ExportTreeViewer(selection, this) {

            /*
             * (non-Javadoc)
             *
             * @see org.talend.repository.ui.wizards.exportjob.ExportTreeViewer#checkSelection()
             */
            @Override
            protected void checkSelection() {
                checkExport();
            }
        };
    }

    @Override
    protected boolean validateDestinationGroup() {
        boolean superValidationResult = super.validateDestinationGroup();
        if (!superValidationResult) {
            return false;
        }
        boolean additionalValidationResult = true;
        String fName = this.getDestinationValue().trim();
        String jobName = new Path(fName).removeFileExtension().lastSegment();

        @SuppressWarnings("restriction")
        IStatus nameStauts = JavaPlugin.getWorkspace().validateName(jobName, IResource.FILE);
        if (!nameStauts.isOK()) {
            setErrorMessage(nameStauts.getMessage());
            setPageComplete(false);
            additionalValidationResult = false;
        }
        return superValidationResult && additionalValidationResult;
    }

    @Override
    public void createControl(Composite parent) {

        initializeDialogUnits(parent);
        SashForm sash = createExportTree(parent);

        // Added a scrolled composite by Marvin Wang on Feb. 27, 2012 for bug TDI-19198.
        scrolledComposite = new ScrolledComposite(sash, SWT.H_SCROLL | SWT.V_SCROLL);
        scrolledComposite.setExpandHorizontal(true);
        scrolledComposite.setExpandVertical(true);
        GridDataFactory.fillDefaults().grab(true, true).applyTo(scrolledComposite);

        pageComposite = new Composite(scrolledComposite, SWT.BORDER);
        GridDataFactory.fillDefaults().grab(true, true).applyTo(pageComposite);

        GridLayout gdlPageComposite = new GridLayout();
        pageComposite.setLayout(gdlPageComposite);
        pageComposite.setFont(parent.getFont());

        createDestinationGroup(pageComposite);

        // this.getDestinationValue()
        // createExportTree(pageComposite);
        if (!isMultiNodes()) {
            IBrandingService brandingService =
                    GlobalServiceRegister.getDefault().getService(IBrandingService.class);
            boolean allowVerchange = brandingService.getBrandingConfiguration().isAllowChengeVersion();
            if (allowVerchange) {
                createJobVersionGroup(pageComposite);
            } else {
                selectedJobVersion = "0.1";
            }
        }

        createExportTypeGroup(pageComposite);

        createOptionsGroupButtons(pageComposite);

        restoreResourceSpecificationWidgetValues(); // ie.- local

        updateWidgetEnablements();
        setPageComplete(determinePageCompletion());

        setControl(sash);
        sash.setWeights(new int[] { 0, 1, 23 });

        giveFocusToDestination();

        pageComposite.setSize(pageComposite.computeSize(SWT.DEFAULT, SWT.DEFAULT));
        scrolledComposite.setMinSize(pageComposite.getSize());
        scrolledComposite.setContent(pageComposite);
    }

    protected void createExportTypeGroup(Composite parent) {
        // options group
        Group optionsGroup = new Group(parent, SWT.NONE);

        GridLayout layout = new GridLayout();
        optionsGroup.setLayout(layout);
        optionsGroup.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL));
        optionsGroup.setText(Messages.getString("JavaJobScriptsExportWSWizardPage.BuildType")); //$NON-NLS-1$
        optionsGroup.setFont(parent.getFont());

        optionsGroup.setLayout(new GridLayout(1, true));

        Composite left = new Composite(optionsGroup, SWT.NONE);
        left.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, true, false));

        GridLayout gdlLeft = new GridLayout(3, false);
        gdlLeft.marginHeight = 0;
        gdlLeft.marginWidth = 0;
        left.setLayout(gdlLeft);

        Label label = new Label(left, SWT.NONE);
        label.setText(Messages.getString("JavaJobScriptsExportWSWizardPage.BuildLabel")); //$NON-NLS-1$

        exportTypeCombo = new Combo(left, SWT.PUSH);
        exportTypeCombo.setLayoutData(new GridData());

        boolean canESBMicroServiceJob = EmfModelUtils.getComponentByName(getProcessItem(), "tRESTRequest") != null;
        boolean isESBJob = false;
        
        boolean canESBMicroServiceDockerImage = PluginChecker.isDockerPluginLoaded();

        for (Object o : ((ProcessItem) processItem).getProcess().getNode()) {
            if (o instanceof NodeType) {
                NodeType currentNode = (NodeType) o;
                if (BuildJobConstants.esbComponents.contains(currentNode.getComponentName())) {
                    isESBJob = true;
                    break;
                }
            }
        }

        for (JobExportType exportType : extractExportJobTypes()) {
            if (!Boolean.getBoolean("talend.export.job.2." + exportType.toString() + ".hide")) { //$NON-NLS-1$//$NON-NLS-2$
                // TESB-20767 Microservice should not be display with TDI license
                if (exportType == JobExportType.ROUTE || exportType == JobExportType.SERVICE) {
                    continue;
                } else if (exportType.equals(JobExportType.OSGI)) {
                    if (isESBJob) {
                        exportTypeCombo.add(exportType.label);
                    }
                } else if (exportType.equals(JobExportType.MSESB)) {
                    if (GlobalServiceRegister.getDefault().isServiceRegistered(IESBMicroService.class) && canESBMicroServiceJob) {
                        exportTypeCombo.add(exportType.label);
                    } else {
                        // reset export type to POJO
                        if (getCurrentExportType1().equals(JobExportType.MSESB)) {
                            getDialogSettings().put(STORE_EXPORTTYPE_ID, JobExportType.POJO.label);
                        }
                    }
                } else if (exportType.equals(JobExportType.MSESB_IMAGE)) {
                    if (GlobalServiceRegister.getDefault().isServiceRegistered(IESBMicroService.class) && canESBMicroServiceJob) {
                        if(canESBMicroServiceDockerImage) {
                            exportTypeCombo.add(exportType.label);
                        }

                    } else {
                        // reset export type to POJO
                        if (getCurrentExportType1().equals(JobExportType.MSESB)) {
                            getDialogSettings().put(STORE_EXPORTTYPE_ID, JobExportType.POJO.label);
                        }
                    }
                } else if (exportType.equals(JobExportType.IMAGE)) {

                    if (canESBMicroServiceJob) {
                        continue;
                    }

                    if (PluginChecker.isDockerPluginLoaded()) {
                        exportTypeCombo.add(exportType.label);
                    } else {
                        if (getCurrentExportType1().equals(JobExportType.IMAGE)) {
                            getDialogSettings().put(STORE_EXPORTTYPE_ID, JobExportType.POJO.label);
                        }
                    }
                } else {

                    if ((canESBMicroServiceJob && exportType == JobExportType.POJO)) {
                        continue;
                    }

                    exportTypeCombo.add(exportType.label);
                }
            }
        }
        String label2 = getCurrentExportType1().label;
        // if the build type was set, try to select by default
        if (nodes != null && nodes.length == 1) { // deal with one node only.
            ProcessItem item = getProcessItem();
            final Object buildType =
                    item.getProperty().getAdditionalProperties().get(TalendProcessArgumentConstant.ARG_BUILD_TYPE);
            if (buildType != null) {
                Map<JobExportType, String> map = BuildJobConstants.oldBuildTypeMap;
                for (JobExportType t : map.keySet()) {
                    if (buildType.toString().equals(map.get(t))) { // same build type
                        label2 = t.label;
                        break;
                    }
                }
            }
        }

        exportTypeCombo.setText(label2);
        if (exportTypeFixed != null) {
            left.setVisible(false);
            optionsGroup.setVisible(false);
            exportTypeCombo.setText(exportTypeFixed.label);
        }

        chkButton = new Button(left, SWT.CHECK);
        chkButton.setText(Messages.getString("JavaJobScriptsExportWSWizardPage.extractZipFile")); //$NON-NLS-1$
        JobExportType comboType = JobExportType.getTypeFromString(exportTypeCombo.getText());
        if (comboType != JobExportType.POJO) {
            chkButton.setVisible(false);
            zipOption = null;
        } else {
            chkButton.setVisible(true);
            zipOption = String.valueOf(chkButton.getSelection());
        }

        chkButton.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                chkButton.setSelection(chkButton.getSelection());
                zipOption = String.valueOf(chkButton.getSelection());
            }
        });

        exportTypeCombo.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                optionsGroupComposite.dispose();
                createOptionsGroupButtons(pageComposite);
                pageComposite.setSize(pageComposite.computeSize(SWT.DEFAULT, SWT.DEFAULT));
                pageComposite.layout();
                JobExportType comboType = JobExportType.getTypeFromString(exportTypeCombo.getText());
                if (comboType != JobExportType.POJO) {
                    chkButton.setVisible(false);
                    zipOption = null;
                } else {
                    chkButton.setVisible(true);
                    zipOption = String.valueOf(chkButton.getSelection());
                }
                updateDestinationGroup(comboType == JobExportType.IMAGE || comboType == JobExportType.MSESB_IMAGE);
                checkExport();
            }
        });
    }

    protected void updateDestinationGroup(boolean isImage) {
        destinationLabel.setEnabled(!isImage);
        destinationBrowseButton.setEnabled(!isImage);
        destinationNameField.setEnabled(!isImage);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.repository.ui.wizards.exportjob.JavaJobScriptsExportWizardPage#createJobScriptsManager()
     */
    @Override
    public JobScriptsManager createJobScriptsManager() {
        Map<ExportChoice, Object> exportChoiceMap = getExportChoiceMap();
        String log4jLevel = "";
        String launcher = (getCurrentExportType1() == JobExportType.POJO) ? launcherCombo.getText() : "all";
        String context =
                (contextCombo == null || contextCombo.isDisposed()) ? IContext.DEFAULT : contextCombo.getText();

        JobScriptsManager manager = JobScriptsManagerFactory
                .createManagerInstance(exportChoiceMap, context, launcher, IProcessor.NO_STATISTICS,
                        IProcessor.NO_TRACES, getCurrentExportType1());
        manager.setDestinationPath(getDestinationValue());
        if (log4jLevelCombo != null && !log4jLevelCombo.isDisposed()) {
            if (log4jLevelCombo.isEnabled()) {
                log4jLevel = log4jLevelCombo.getText();
            } else {
                log4jLevel = null;
            }
        } else {
            log4jLevel = null;
        }
        manager.setLog4jLevel(log4jLevel);
        return manager;
    }

    @Override
    protected String getOutputSuffix() {
        switch (getCurrentExportType1()) {
        case OSGI:
            return FileConstants.JAR_FILE_SUFFIX;
        case MSESB:
            return FileConstants.JAR_FILE_SUFFIX;
        default:
            return FileConstants.ZIP_FILE_SUFFIX;
        }
    }

    protected String getPetalsDefaultSaName() {
        return "sa-talend-" + getDefaultFileName().get(0) + "Service-provide.zip"; //$NON-NLS-1$ //$NON-NLS-2$
    }

    /**
     * Open an appropriate destination browser so that the user can specify a source to import from.
     */
    @Override
    protected void handleDestinationBrowseButtonPressed() {
        FileDialog dialog = new FileDialog(getContainer().getShell(), SWT.SAVE);
        JobExportType jobExportType = getCurrentExportType1();
        switch (jobExportType) {
        case OSGI:
            dialog.setFilterExtensions(new String[] { "*" + FileConstants.JAR_FILE_SUFFIX, "*.*" }); //$NON-NLS-1$ //$NON-NLS-2$
            break;
        case MSESB:
            dialog.setFilterExtensions(new String[] { "*" + FileConstants.JAR_FILE_SUFFIX, "*.*" }); //$NON-NLS-1$ //$NON-NLS-2$
            break;
        default:
            dialog.setFilterExtensions(new String[] { "*" + FileConstants.ZIP_FILE_SUFFIX, "*.*" }); //$NON-NLS-1$ //$NON-NLS-2$
        }

        dialog.setText(""); //$NON-NLS-1$
        // this is changed by me shenhaize
        dialog.setFileName(getDefaultFileName().get(0));
        String currentSourceString = getDestinationValue();
        int lastSeparatorIndex = currentSourceString.lastIndexOf(File.separator);
        if (lastSeparatorIndex != -1) {
            dialog.setFilterPath(currentSourceString.substring(0, lastSeparatorIndex));
        }

        String selectedFileName = dialog.open();
        if (selectedFileName == null) {
            return;
        }
        String idealSuffix;
        idealSuffix = getOutputSuffix();
        if (!selectedFileName.endsWith(idealSuffix)) {
            selectedFileName += idealSuffix;
        }
        // when user change the name of job,will add the version auto
        if (selectedFileName != null && !selectedFileName.endsWith(getSelectedJobVersion() + idealSuffix)) {
            String b = selectedFileName.substring(0, (selectedFileName.length() - 4));
            File file = new File(b);

            String str = file.getName();

            String s = getDefaultFileName().get(0);

            if (str.equals(s)) {
                if (getDefaultFileName().get(1) != null && !"".equals(getDefaultFileName().get(1))) {
                    selectedFileName = b + ((JobExportType.OSGI.equals(jobExportType)) ? "-" : "_")
                            + getDefaultFileName().get(1) + idealSuffix;
                } else {
                    selectedFileName = b + idealSuffix;
                }
            } else {
                selectedFileName = b + idealSuffix;
            }

        }
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
    protected void addDestinationItem(String value) {
        destinationNameField.add(value);
    }

    @Override
    protected String getDestinationValue() {
        return destinationNameField.getText().trim();
    }

    @Override
    protected void setDestinationValue(String value) {
        destinationNameField.setText(value);
    }

    @Override
    protected void giveFocusToDestination() {
        destinationNameField.setFocus();
    }

    @Override
    protected boolean ensureTargetIsValid() {
        if (JobExportType.getTypeFromString(exportTypeCombo.getText()) == JobExportType.IMAGE) {
            return true;
        }
        return super.ensureTargetIsValid();
    }

    @Override
    public void handleEvent(Event e) {
        Widget source = e.widget;
        if (source == destinationBrowseButton) {
            handleDestinationBrowseButtonPressed();
        }
        updatePageCompletion();

        if (source instanceof Combo) {
            String destination = ((Combo) source).getText();
            if (getDialogSettings() != null) {
                IDialogSettings section = getDialogSettings().getSection(DESTINATION_FILE);
                if (section == null) {
                    section = getDialogSettings().addNewSection(DESTINATION_FILE);
                }
                section.put(DESTINATION_FILE, destination);
            }
            if (destination != null) {
                if (!destination.endsWith(getOutputSuffix())) {
                    destination += getOutputSuffix();
                }
            }
        }
    }

    protected void restoreWidgetValuesForPetalsESB() {

        IDialogSettings settings = getDialogSettings();
        if (settings != null) {
            String[] directoryNames = settings.getArray(STORE_DESTINATION_NAMES_ID);
            String saName = getPetalsDefaultSaName();
            if (directoryNames != null && directoryNames.length > 0) {
                // destination
                for (int i = 0; i < directoryNames.length; i++) {
                    if (directoryNames[i].toLowerCase().endsWith(FileConstants.ZIP_FILE_SUFFIX)) {
                        directoryNames[i] =
                                (directoryNames[i].charAt(0) + "").toUpperCase() + directoryNames[i].substring(1);//$NON-NLS-1$
                        addDestinationItem(directoryNames[i]);
                    }
                }
                File file = new File(directoryNames[0]);
                File dest = new File(file.getParentFile(), saName);
                setDestinationValue(dest.getAbsolutePath());
            } else {
                String userDir = System.getProperty("user.dir"); //$NON-NLS-1$
                IPath path = new Path(userDir).append(saName);
                setDestinationValue(path.toOSString());
            }
            sourceButton.setSelection(settings.getBoolean(STORE_SOURCE_ID));
            zipOption = "false"; // Do not extract the ZIP //$NON-NLS-1$
        }

        if (getProcessItem() != null) {
            List<String> contextNames = ExportJobUtil.getJobContexts(getProcessItem());
            contextCombo.setItems(contextNames.toArray(new String[contextNames.size()]));
            contextCombo.setVisibleItemCount(contextNames.size());
            if (contextNames.size() > 0) {
                contextCombo.select(0);
            }
        }
    }

    protected void restoreWidgetValuesForESB() {
        IDialogSettings settings = getDialogSettings();
        if (settings != null) {
            String[] directoryNames = settings.getArray(STORE_DESTINATION_NAMES_ID);
            if (directoryNames != null && directoryNames.length > 0) {
                String fileName = getDefaultFileNameWithType();
                // destination
                for (int i = 0; i < directoryNames.length; i++) {
                    if (directoryNames[i].toLowerCase().endsWith(FileConstants.ESB_FILE_SUFFIX)) {
                        directoryNames[i] =
                                (directoryNames[i].charAt(0) + "").toUpperCase() + directoryNames[i].substring(1);//$NON-NLS-1$
                        addDestinationItem(directoryNames[i]);
                    }
                }
                File dest = new File(new File(directoryNames[0]).getParentFile(), fileName);
                setDestinationValue(dest.getAbsolutePath());
            } else {
                setDefaultDestination();
            }

            IDialogSettings section = getDialogSettings().getSection(DESTINATION_FILE);
            if (section == null) {
                section = getDialogSettings().addNewSection(DESTINATION_FILE);
            }
            if (jobScriptButton != null && !jobScriptButton.isDisposed()) {
                jobScriptButton.setSelection(settings.getBoolean(STORE_SOURCE_ID));
            }
            if (contextButton != null && !contextButton.isDisposed()) {
                contextButton.setSelection(settings.getBoolean(STORE_CONTEXT_ID));
            }
            if (applyToChildrenButton != null && !applyToChildrenButton.isDisposed()) {
                applyToChildrenButton.setSelection(settings.getBoolean(APPLY_TO_CHILDREN_ID));
            }
            if (jobItemButton != null && !jobItemButton.isDisposed()) {
                jobItemButton.setSelection(settings.getBoolean(STORE_JOB_ID));
            }

            if (section.get(ESB_EXPORT_TYPE) != null) {
                esbTypeCombo.setText(section.get(ESB_EXPORT_TYPE));
                if (section.get(ESB_SERVICE_NAME) != null) {
                    esbServiceName.setText(section.get(ESB_SERVICE_NAME));
                }
                if (section.get(ESB_CATEGORY) != null) {
                    esbCategory.setText(section.get(ESB_CATEGORY));
                }
                if (section.get(QUERY_MESSAGE_NAME) != null) {
                    esbQueueMessageName.setText(section.get(QUERY_MESSAGE_NAME));
                }
            }
        }

        if (getProcessItem() != null && contextCombo != null) {
            try {
                setProcessItem((ProcessItem) ProxyRepositoryFactory
                        .getInstance()
                        .getUptodateProperty(getProcessItem().getProperty())
                        .getItem());
            } catch (PersistenceException e) {
                e.printStackTrace();
            }
            List<String> contextNames = ExportJobUtil.getJobContexts(getProcessItem());
            contextCombo.setItems(contextNames.toArray(new String[contextNames.size()]));
            if (contextNames.size() > 0) {
                contextCombo.select(0);
            }
        }
    }

    protected void restoreWidgetValuesForOSGI() {
        IDialogSettings settings = getDialogSettings();
        if (settings != null) {
            String[] directoryNames = settings.getArray(STORE_DESTINATION_NAMES_ID);
            if (directoryNames != null && directoryNames.length > 0) {
                String fileName = getDefaultFileNameWithType();
                for (int i = 0; i < directoryNames.length; i++) {
                    if (directoryNames[i].toLowerCase().endsWith(FileConstants.JAR_FILE_SUFFIX)) {
                        directoryNames[i] =
                                (directoryNames[i].charAt(0) + "").toUpperCase() + directoryNames[i].substring(1);//$NON-NLS-1$
                        addDestinationItem(directoryNames[i]);
                    }
                }
                File dest = new File(new File(directoryNames[0]).getParentFile(), fileName);
                setDestinationValue(dest.getAbsolutePath());
            } else {
                setDefaultDestinationForOSGI();
            }
        } else {
            setDefaultDestinationForOSGI();
        }
    }

    protected void restoreWidgetValuesForPOJO() {
        IDialogSettings settings = getDialogSettings();

        if (settings != null) {
            String[] directoryNames = settings.getArray(STORE_DESTINATION_NAMES_ID);
            String fileName = getDefaultFileNameWithType();
            if (!fileName.endsWith(FileConstants.ZIP_FILE_SUFFIX)) {
                fileName = fileName + FileConstants.ZIP_FILE_SUFFIX;
            }
            if (directoryNames != null && directoryNames.length > 0) {
                // destination
                for (int i = 0; i < directoryNames.length; i++) {
                    // String destination;
                    // Path dirPath = new Path(directoryNames[i]);
                    // if (dirPath.segmentCount() == 0) {
                    // destination = dirPath.toOSString() + File.separator + fileName;
                    // } else {
                    // destination = dirPath.append(fileName).toOSString();
                    // }
                    if (directoryNames[i].toLowerCase().endsWith(FileConstants.ZIP_FILE_SUFFIX)) {
                        directoryNames[i] =
                                (directoryNames[i].charAt(0) + "").toUpperCase() + directoryNames[i].substring(1);//$NON-NLS-1$
                        addDestinationItem(directoryNames[i]);
                    }
                }
                String sepa = java.io.File.separator;
                setDestinationValue(
                        directoryNames[0].substring(0, (directoryNames[0].lastIndexOf(sepa) + 1)) + fileName);// $NON-NLS-1$
            } else {
                setDefaultDestination();
            }
            this.destinationNameField.pack(true);

            shellLauncherButton.setSelection(settings.getBoolean(STORE_SHELL_LAUNCHER_ID));
            // TDQ-15391: when have tDqReportRun, must always export items.
            if (EmfModelUtils.getComponentByName(getProcessItem(), "tDqReportRun") != null) { //$NON-NLS-1$
                jobItemButton.setSelection(true);
            } else {
                jobItemButton.setSelection(settings.getBoolean(STORE_JOB_ID));
            }
            // TDQ-15391~
            jobScriptButton.setSelection(settings.getBoolean(STORE_SOURCE_ID));
            contextButton.setSelection(settings.getBoolean(STORE_CONTEXT_ID));
            applyToChildrenButton.setSelection(settings.getBoolean(APPLY_TO_CHILDREN_ID));
            chkButton.setSelection(settings.getBoolean(EXTRACT_ZIP_FILE));
            zipOption = String.valueOf(chkButton.getSelection());
            // genCodeButton.setSelection(settings.getBoolean(STORE_GENERATECODE_ID));
        }

        launcherCombo.setItems(JobScriptsManager.getLauncher());
        if (JobScriptsManager.getLauncher().length > 0) {
            launcherCombo.select(0);
        }
        if (getProcessItem() != null && contextCombo != null) {
            // don't update the property, this one will be automatically updated if needed when call the getItem()

            // try {
            // process[0].setProcess((ProcessItem) ProxyRepositoryFactory.getInstance().getUptodateProperty(
            // getProcessItem().getProperty()).getItem());
            // } catch (PersistenceException e) {
            // ExceptionHandler.process(e);
            // }
            ProcessItem item = getProcessItem();
            try {
                String id = item.getProperty().getId();
                IRepositoryViewObject lastVersion = ProxyRepositoryFactory.getInstance().getLastVersion(id);
                item = (ProcessItem) lastVersion.getProperty().getItem();
            } catch (PersistenceException e) {
                throw new RuntimeException(e);
            }
            List<String> contextNames;
            contextNames = ExportJobUtil.getJobContexts(item);

            contextCombo.setItems(contextNames.toArray(new String[contextNames.size()]));
            if (contextNames.size() > 0) {
                contextCombo.select(0);
            }
        }

        if (log4jLevelCombo != null) {
            log4jLevelCombo.setItems(Log4jPrefsSettingManager.getLevel());
            if (Log4jPrefsSettingManager.getLevel().length > 0) {
                log4jLevelCombo.select(2);
            }
        }
        // contextCombo applyToChildrenButton control by contextbutton
        contextCombo.setEnabled(contextButton.getSelection());
        applyToChildrenButton.setEnabled(contextButton.getSelection());
    }

    private void restoreWidgetValuesForImage() {
        IDialogSettings settings = getDialogSettings();
        if (settings != null) {
            String[] directoryNames = settings.getArray(STORE_DESTINATION_NAMES_ID);
            String fileName = getDefaultFileNameWithType();
            if (!fileName.endsWith(FileConstants.ZIP_FILE_SUFFIX)) {
                fileName = fileName + FileConstants.ZIP_FILE_SUFFIX;
            }
            if (directoryNames != null && directoryNames.length > 0) {
                for (int i = 0; i < directoryNames.length; i++) {
                    if (directoryNames[i].toLowerCase().endsWith(FileConstants.ZIP_FILE_SUFFIX)) {
                        directoryNames[i] =
                                (directoryNames[i].charAt(0) + "").toUpperCase() + directoryNames[i].substring(1);//$NON-NLS-1$
                        addDestinationItem(directoryNames[i]);
                        break;
                    }
                }
                setDestinationValue(
                        directoryNames[0].substring(0, (directoryNames[0].lastIndexOf(File.separator) + 1)) + fileName);// $NON-NLS-1$
            } else {
                setDefaultDestination();
            }
            updateDestinationGroup(true);
            contextButton.setSelection(settings.getBoolean(STORE_CONTEXT_ID));
            if (getCurrentExportType1() == JobExportType.IMAGE) {
                applyToChildrenButton.setSelection(settings.getBoolean(APPLY_TO_CHILDREN_ID));
            }
        }
        if (getProcessItem() != null && contextCombo != null && !contextCombo.isDisposed()) {
            ProcessItem item = getProcessItem();
            try {
                String id = item.getProperty().getId();
                IRepositoryViewObject lastVersion = ProxyRepositoryFactory.getInstance().getLastVersion(id);
                item = (ProcessItem) lastVersion.getProperty().getItem();
            } catch (PersistenceException e) {
                throw new RuntimeException(e);
            }
            List<String> contextNames = ExportJobUtil.getJobContexts(item);
            contextCombo.setItems(contextNames.toArray(new String[contextNames.size()]));
            if (contextNames.size() > 0) {
                contextCombo.select(0);
            }
        }
        if (log4jLevelCombo != null && !log4jLevelCombo.isDisposed()) {
            log4jLevelCombo.setItems(Log4jPrefsSettingManager.getLevel());
            if (Log4jPrefsSettingManager.getLevel().length > 0) {
                log4jLevelCombo.select(2);
            }
        }
        String remoteHost = settings.get(STORE_DOCKER_REMOTE_HOST);
        if (StringUtils.isNotBlank(remoteHost)) {
            hostText.setText(remoteHost);
        }
        boolean isRemote = settings.getBoolean(STORE_DOCKER_IS_REMOTE_HOST);
        localRadio.setSelection(!isRemote);
        remoteRadio.setSelection(isRemote);
        hostText.setEnabled(isRemote);
    }

    @Override
    protected void internalSaveWidgetValues() {
        // update directory names history
        IDialogSettings settings = getDialogSettings();
        if (settings != null) {
            String[] directoryNames = settings.getArray(STORE_DESTINATION_NAMES_ID);
            if (directoryNames == null) {
                directoryNames = new String[0];
            }
            String destinationValue = getDestinationValue();
            directoryNames = addToHistory(directoryNames, destinationValue);
            // String[] directoryNames = new String[1];
            // String destinationValue = getDestinationValue();
            // if (destinationValue != null) {
            // destinationValue = destinationValue.substring(0, destinationValue.lastIndexOf(File.separator));
            // }
            // directoryNames[0] = destinationValue;

            settings.put(STORE_EXPORTTYPE_ID, getCurrentExportType1().toString());
            settings.put(STORE_DESTINATION_NAMES_ID, directoryNames);
            if (getCurrentExportType1().equals(JobExportType.OSGI)) {
                return;
            }
            if (getCurrentExportType1().equals(JobExportType.MSESB)) {
                return;
            }
            if (getCurrentExportType1().equals(JobExportType.MSESB_IMAGE)
                    || getCurrentExportType1().equals(JobExportType.IMAGE)) {
                settings.put(STORE_DOCKER_IS_REMOTE_HOST, remoteRadio.getSelection());
                if (remoteRadio.getSelection() && StringUtils.isNotBlank(hostText.getText())) {
                    settings.put(STORE_DOCKER_REMOTE_HOST, hostText.getText());
                }
                return;
            }

            if (contextButton != null) {
                settings.put(STORE_CONTEXT_ID, contextButton.getSelection());
            }
            if (jobScriptButton != null && !jobScriptButton.isDisposed()) {
                settings.put(STORE_SOURCE_ID, jobScriptButton.getSelection());
            }
            if (applyToChildrenButton != null) {
                settings.put(APPLY_TO_CHILDREN_ID, applyToChildrenButton.getSelection());
            }
            if (jobItemButton != null && !jobItemButton.isDisposed()) {
                settings.put(STORE_JOB_ID, jobItemButton.getSelection());
            }

            if (log4jLevelCombo != null && !log4jLevelCombo.isDisposed()) {
                settings.put(LOG4J_LEVEL_ID, log4jLevelCombo.getText());
            }

            if (getCurrentExportType1().equals(JobExportType.POJO)) {
                settings.put(STORE_SHELL_LAUNCHER_ID, shellLauncherButton.getSelection());
                settings.put(EXTRACT_ZIP_FILE, chkButton.getSelection());
                return;
            }
        }
    }

    @Override
    protected Map<ExportChoice, Object> getExportChoiceMap() {
        JobExportType comboType = JobExportType.getTypeFromString(exportTypeCombo.getText());
        if (comboType.equals(JobExportType.POJO)) {
            return JavaJobScriptsExportWSWizardPage.super.getExportChoiceMap();
        }
        Map<ExportChoice, Object> exportChoiceMap = new EnumMap<>(ExportChoice.class);
        exportChoiceMap.put(ExportChoice.needJobItem, false);
        exportChoiceMap.put(ExportChoice.needSourceCode, false);

        if (comboType.equals(JobExportType.OSGI)) {
            exportChoiceMap.put(ExportChoice.needMetaInfo, true);
            exportChoiceMap.put(ExportChoice.needContext, true);
            exportChoiceMap.put(ExportChoice.needJobItem, false);
            exportChoiceMap.put(ExportChoice.needSourceCode, false);
            exportChoiceMap.put(ExportChoice.binaries, true);

            return exportChoiceMap;
        }

        if (comboType.equals(JobExportType.MSESB)) {
            exportChoiceMap.put(ExportChoice.needMetaInfo, true);
            exportChoiceMap.put(ExportChoice.needContext, true);
            exportChoiceMap.put(ExportChoice.needJobItem, false);
            exportChoiceMap.put(ExportChoice.needSourceCode, false);
            exportChoiceMap.put(ExportChoice.binaries, true);
            if (exportMSAsZipButton != null) {
                exportChoiceMap.put(ExportChoice.needAssembly, exportMSAsZipButton.getSelection());
                exportChoiceMap.put(ExportChoice.needLauncher, exportMSAsZipButton.getSelection());
            }

            return exportChoiceMap;
        }

        if (comboType.equals(JobExportType.MSESB_IMAGE)) {
            return getExportChoiceMapForMSESBImage();
        }

        if (comboType.equals(JobExportType.IMAGE)) {
            return getExportChoiceMapForImage();
        }

        // fix bug 9150, export items and code source, added by nma
        exportChoiceMap.put(ExportChoice.needJobItem, jobScriptButton.getSelection());
        exportChoiceMap.put(ExportChoice.needSourceCode, jobScriptButton.getSelection());

        exportChoiceMap.put(ExportChoice.needWEBXML, webXMLButton.getSelection());
        exportChoiceMap.put(ExportChoice.needCONFIGFILE, configFileButton.getSelection());
        exportChoiceMap.put(ExportChoice.needAXISLIB, axisLibButton.getSelection());
        exportChoiceMap.put(ExportChoice.needWSDD, wsddButton.getSelection());
        exportChoiceMap.put(ExportChoice.needWSDL, wsdlButton.getSelection());
        exportChoiceMap.put(ExportChoice.needJobScript, Boolean.TRUE);
        exportChoiceMap.put(ExportChoice.needContext, contextButton.getSelection());
        exportChoiceMap.put(ExportChoice.applyToChildren, applyToChildrenButton.getSelection());
        exportChoiceMap.put(ExportChoice.needMetaInfo, false);
        return exportChoiceMap;
    }

    private Map<ExportChoice, Object> getExportChoiceMapForMSESBImage() {
        Map<ExportChoice, Object> exportChoiceMap = new EnumMap<>(ExportChoice.class);
        exportChoiceMap.put(ExportChoice.buildImage, Boolean.TRUE);
        exportChoiceMap.put(ExportChoice.needLauncher, Boolean.TRUE);
        exportChoiceMap.put(ExportChoice.launcherName, JobScriptsManager.UNIX_ENVIRONMENT);
        exportChoiceMap.put(ExportChoice.needSystemRoutine, Boolean.TRUE);
        exportChoiceMap.put(ExportChoice.needUserRoutine, Boolean.TRUE);
        exportChoiceMap.put(ExportChoice.needTalendLibraries, Boolean.TRUE);
        exportChoiceMap.put(ExportChoice.needMetaInfo, true);
        exportChoiceMap.put(ExportChoice.binaries, true);
        // TDQ-15391: when have tDqReportRun, must always export items.
        if (EmfModelUtils.getComponentByName(getProcessItem(), "tDqReportRun") != null) { //$NON-NLS-1$
            exportChoiceMap.put(ExportChoice.needJobItem, Boolean.TRUE);
        } else {
            exportChoiceMap.put(ExportChoice.needJobItem, Boolean.FALSE);
        }
        // TDQ-15391~
        exportChoiceMap.put(ExportChoice.needSourceCode, Boolean.FALSE);
        exportChoiceMap.put(ExportChoice.needDependencies, Boolean.TRUE);
        exportChoiceMap.put(ExportChoice.needJobScript, Boolean.FALSE);
        exportChoiceMap.put(ExportChoice.needAssembly, Boolean.TRUE);
        exportChoiceMap.put(ExportChoice.needContext, true);
        exportChoiceMap.put(ExportChoice.contextName, getContextName());

        if (remoteRadio.getSelection()) {
            String host = hostText.getText();
            if (!StringUtils.isBlank(host)) {
                exportChoiceMap.put(ExportChoice.dockerHost, host);
            }
        }
        String imageName = imageText.getText();
        if (!StringUtils.isBlank(imageName)) {
            exportChoiceMap.put(ExportChoice.imageName, imageName);
        }
        String imageTag = tagText.getText();
        if (!StringUtils.isBlank(imageTag)) {
            exportChoiceMap.put(ExportChoice.imageTag, imageTag);
        }

        return exportChoiceMap;
    }

    private Map<ExportChoice, Object> getExportChoiceMapForImage() {
        Map<ExportChoice, Object> exportChoiceMap = new EnumMap<>(ExportChoice.class);
        exportChoiceMap.put(ExportChoice.buildImage, Boolean.TRUE);
        exportChoiceMap.put(ExportChoice.needLauncher, Boolean.TRUE);
        exportChoiceMap.put(ExportChoice.launcherName, JobScriptsManager.UNIX_ENVIRONMENT);
        exportChoiceMap.put(ExportChoice.needSystemRoutine, Boolean.TRUE);
        exportChoiceMap.put(ExportChoice.needUserRoutine, Boolean.TRUE);
        exportChoiceMap.put(ExportChoice.needTalendLibraries, Boolean.TRUE);
        // TDQ-15391: when have tDqReportRun, must always export items.
        if (EmfModelUtils.getComponentByName(getProcessItem(), "tDqReportRun") != null) { //$NON-NLS-1$
            exportChoiceMap.put(ExportChoice.needJobItem, Boolean.TRUE);
        } else {
            exportChoiceMap.put(ExportChoice.needJobItem, Boolean.FALSE);
        }
        // TDQ-15391~
        exportChoiceMap.put(ExportChoice.needSourceCode, Boolean.FALSE);
        exportChoiceMap.put(ExportChoice.needDependencies, Boolean.TRUE);
        exportChoiceMap.put(ExportChoice.needJobScript, Boolean.FALSE);
        exportChoiceMap.put(ExportChoice.needAssembly, Boolean.TRUE);
        exportChoiceMap.put(ExportChoice.needContext, isNeedConext());
        exportChoiceMap.put(ExportChoice.contextName, getContextName());

        if (remoteRadio.getSelection()) {
            String host = hostText.getText();
            if (!StringUtils.isBlank(host)) {
                exportChoiceMap.put(ExportChoice.dockerHost, host);
            }
        }
        String imageName = imageText.getText();
        if (!StringUtils.isBlank(imageName)) {
            exportChoiceMap.put(ExportChoice.imageName, imageName);
        }
        String imageTag = tagText.getText();
        if (!StringUtils.isBlank(imageTag)) {
            exportChoiceMap.put(ExportChoice.imageTag, imageTag);
        }

        if (applyToChildrenButton != null) {
            exportChoiceMap.put(ExportChoice.applyToChildren, applyToChildrenButton.getSelection());
        }
        if (setParametersValueButton2 != null) {
            exportChoiceMap.put(ExportChoice.needParameterValues, setParametersValueButton2.getSelection());
            if (setParametersValueButton2.getSelection()) {
                exportChoiceMap.put(ExportChoice.parameterValuesList, manager.getContextEditableResultValuesList());
            }
        }

        exportChoiceMap.put(ExportChoice.binaries, Boolean.TRUE);
        exportChoiceMap.put(ExportChoice.executeTests, Boolean.FALSE);
        exportChoiceMap.put(ExportChoice.includeTestSource, Boolean.FALSE);
        exportChoiceMap.put(ExportChoice.includeLibs, Boolean.TRUE);

        exportChoiceMap.put(ExportChoice.needLog4jLevel, isNeedLog4jLevel());
        exportChoiceMap.put(ExportChoice.log4jLevel, getLog4jLevel());

        return exportChoiceMap;
    }

    protected void createOptionsGroupButtons(Composite parent) {

        optionsGroupComposite = new Composite(parent, SWT.NONE);
        GridDataFactory.fillDefaults().grab(true, false).applyTo(optionsGroupComposite);

        GridLayout gdlOptionsGroupComposite = new GridLayout();
        gdlOptionsGroupComposite.marginHeight = 0;
        gdlOptionsGroupComposite.marginWidth = 0;
        optionsGroupComposite.setLayout(gdlOptionsGroupComposite);

        // options group
        Group optionsGroup = new Group(optionsGroupComposite, SWT.NONE);
        GridDataFactory.fillDefaults().grab(true, false).applyTo(optionsGroup);

        optionsGroup.setText(Messages.getString("IDEWorkbenchMessages.WizardExportPage_options")); //$NON-NLS-1$
        optionsGroup.setFont(parent.getFont());

        Font font = optionsGroup.getFont();
        optionsGroup.setLayout(new GridLayout());

        Composite left = new Composite(optionsGroup, SWT.NONE);
        GridDataFactory.fillDefaults().grab(true, false).applyTo(left);

        GridLayout gdlLeft = new GridLayout();
        gdlLeft.marginHeight = 0;
        gdlLeft.marginWidth = 0;
        left.setLayout(gdlLeft);

        switch (getCurrentExportType1()) {
        case POJO:
            createOptions(left, font);
            restoreWidgetValuesForPOJO();
            break;
        case OSGI:
            createOptionsForOSGIESB(left, font);
            restoreWidgetValuesForOSGI();
            break;
        case MSESB:
            createOptionsForMSESB(left, font);
            restoreWidgetValuesForOSGI();
            break;
        case MSESB_IMAGE:
            createOptionsForMSESB(left, font);
            createDockerOptions();
            restoreWidgetValuesForImage();

            if (checkExport()) {
                addDockerOptionsListener();
            }

            contextButton.setSelection(false);
            break;
        case IMAGE:
            createOptionForDockerImage(left, font);
            createDockerOptions();
            restoreWidgetValuesForImage();
            addDockerOptionsListener();
            break;
        default:
            createOptionsForWS(left, font);
            break;
        }
    }

    @Override
    protected void restoreWidgetValues() {

        IDialogSettings settings = getDialogSettings();
        if (settings != null) {
            String[] directoryNames = settings.getArray(PETALS_EXPORT_DESTINATIONS);
            if (directoryNames == null || directoryNames.length == 0) {
                return;
            }

            if (directoryNames[0].endsWith(getPetalsDefaultSaName())) {
                setDestinationValue(directoryNames[0]);
            }

            for (String directoryName : directoryNames) {
                addDestinationItem(directoryName);
            }
        }
    }

    @Override
    protected boolean validateOptionsGroup() {
        setErrorMessage(null);
        isValid = super.validateOptionsGroup();

        // TESB-13867 Export limitations for ESB 'Jobs'
        // add extra checks.
        if (isValid) {
            String errorMsg = presenter.extraCheck(getCurrentExportType1(), getCheckNodes());
            if (errorMsg != null) {
                setErrorMessage(errorMsg);
                isValid = false;
                // return false;
            }
        }
        setPageComplete(isValid);
        return isValid;
    }

    private void createOptionsForMSESB(Composite optionsComposite, Font font) {
        if (!PluginChecker.isTIS()) {
            return;
        }

        if (getCurrentExportType1() != JobExportType.MSESB_IMAGE) {
            addBSButton = new Button(optionsComposite, SWT.CHECK | SWT.LEFT);
            addBSButton.setText("Add maven script"); //$NON-NLS-1$
            addBSButton.setFont(font);

            addBSButton.addSelectionListener(new SelectionAdapter() {

                @Override
                public void widgetSelected(SelectionEvent e) {
                    String destinationValue = getDestinationValue();
                    if (destinationValue.endsWith(OUTPUT_FILE_SUFFIX)) {
                        destinationValue = destinationValue.substring(0, destinationValue.indexOf(OUTPUT_FILE_SUFFIX))
                                + getOutputSuffix();
                    }
                    setDestinationValue(destinationValue);
                }
            });
        }

        contextButton = new Button(optionsComposite, SWT.CHECK | SWT.LEFT);
        contextButton.setText("Only export the default context"); //$NON-NLS-1$
        contextButton.setFont(font);
        // contextButton.setEnabled(false);
        contextButton.setVisible(PluginChecker.isTIS());
        contextButton.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                // onlyExportDefaultContext = contextButton.getSelection();
            }
        });

        if (getCurrentExportType1() != JobExportType.MSESB_IMAGE) {
            exportMSAsZipButton = new Button(optionsComposite, SWT.CHECK | SWT.LEFT);
            exportMSAsZipButton.setText("Export as ZIP"); //$NON-NLS-1$
            exportMSAsZipButton.setFont(getFont());
            // exportAsZipButton.setEnabled(false);
            exportMSAsZipButton.setVisible(PluginChecker.isTIS());
            exportMSAsZipButton.addSelectionListener(new SelectionAdapter() {

                @Override
                public void widgetSelected(SelectionEvent e) {
                    boolean selectContext = exportMSAsZipButton.getSelection();
                    // exportAsZip = selectContext;

                    String destinationValue = getDestinationValue();
                    if (destinationValue.endsWith(getOutputSuffix())) {
                        if (selectContext) {
                            destinationValue =
                                    destinationValue.substring(0, destinationValue.indexOf(getOutputSuffix()))
                                            + OUTPUT_FILE_SUFFIX;
                        }
                    } else if (destinationValue.endsWith(OUTPUT_FILE_SUFFIX)) {
                        if (!selectContext) {
                            destinationValue =
                                    destinationValue.substring(0, destinationValue.indexOf(OUTPUT_FILE_SUFFIX))
                                            + getOutputSuffix();
                        }
                    }
                    setDestinationValue(destinationValue);

                }
            });
        }
    }

    private void createOptionsForOSGIESB(Composite optionsComposite, Font font) {
        if (!PluginChecker.isPluginLoaded(PluginChecker.EXPORT_JOB_PLUGIN_ID)) {
            return;
        }

        addBSButton = new Button(optionsComposite, SWT.CHECK | SWT.LEFT);
        addBSButton.setText("Add maven script"); //$NON-NLS-1$
        addBSButton.setFont(font);

        addBSButton.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                String destinationValue = getDestinationValue();
                if (destinationValue.endsWith(OUTPUT_FILE_SUFFIX)) {
                    destinationValue = destinationValue.substring(0, destinationValue.indexOf(OUTPUT_FILE_SUFFIX))
                            + getOutputSuffix();
                }
                setDestinationValue(destinationValue);
            }
        });

    }

    private void createOptionForDockerImage(Composite optionsGroup, Font font) {
        Composite optionsComposite = new Composite(optionsGroup, SWT.NONE);
        GridDataFactory.fillDefaults().grab(true, false).span(3, 1).applyTo(optionsComposite);

        GridLayout gdlOptionsComposite = new GridLayout(3, false);
        gdlOptionsComposite.marginHeight = 0;
        gdlOptionsComposite.marginWidth = 0;
        optionsComposite.setLayout(gdlOptionsComposite);

        createContextOptions(font, optionsComposite);
        new Label(optionsComposite, SWT.NONE);
        new Label(optionsComposite, SWT.NONE);
        createLog4jOption(font, optionsComposite);

    }

    private void createDockerOptions() {
        Group optionsGroup = new Group(optionsGroupComposite, SWT.NONE);
        GridDataFactory.fillDefaults().grab(true, false).applyTo(optionsGroup);

        optionsGroup.setText(Messages.getString("JavaJobScriptsExportWSWizardPage.DOCKER.optionGroup")); //$NON-NLS-1$
        optionsGroup.setLayout(new GridLayout());

        Composite dockeOptionsComposite = new Composite(optionsGroup, SWT.NONE);
        GridDataFactory.fillDefaults().grab(true, false).span(3, 1).applyTo(dockeOptionsComposite);
        GridLayout dockerOptionsLayout = new GridLayout(3, false);
        dockerOptionsLayout.marginHeight = 0;
        dockerOptionsLayout.marginWidth = 0;
        dockeOptionsComposite.setLayout(dockerOptionsLayout);

        hostLabel = new Label(dockeOptionsComposite, SWT.NONE);
        hostLabel.setText(Messages.getString("JavaJobScriptsExportWSWizardPage.DOCKER.dockerHost")); //$NON-NLS-1$
        Composite hostComposite = new Composite(dockeOptionsComposite, SWT.NONE);
        hostComposite.setLayout(new GridLayout(2, false));

        localRadio = new Button(hostComposite, SWT.RADIO);
        localRadio.setText(Messages.getString("JavaJobScriptsExportWSWizardPage.DOCKER.localHost")); //$NON-NLS-1$
        remoteRadio = new Button(hostComposite, SWT.RADIO);
        remoteRadio.setText(Messages.getString("JavaJobScriptsExportWSWizardPage.DOCKER.remoteHost")); //$NON-NLS-1$
        hostText = new Text(dockeOptionsComposite, SWT.BORDER);
        GridData hostData = new GridData(GridData.FILL_HORIZONTAL);
        hostText.setLayoutData(hostData);

        imageLabel = new Label(dockeOptionsComposite, SWT.NONE);
        imageLabel.setText(Messages.getString("JavaJobScriptsExportWSWizardPage.DOCKER.imageLabel")); //$NON-NLS-1$
        imageText = new Text(dockeOptionsComposite, SWT.BORDER);
        // imageText.setText("${talend.project.name.lowercase}/${talend.job.folder}%a"); //$NON-NLS-1$
        GridDataFactory.fillDefaults().span(2, 1).grab(true, false).applyTo(imageText);

        tagLabel = new Label(dockeOptionsComposite, SWT.NONE);
        tagLabel.setText(Messages.getString("JavaJobScriptsExportWSWizardPage.DOCKER.tagLabel")); //$NON-NLS-1$
        tagText = new Text(dockeOptionsComposite, SWT.BORDER);
        // tagText.setText("${talend.job.version}"); //$NON-NLS-1$
        GridDataFactory.fillDefaults().span(2, 1).grab(true, false).applyTo(tagText);

        updateOptionBySelection();

        // Label additionalLabel = new Label(dockeOptionsComposite, SWT.NONE);
        // additionalLabel.setText("Additional properties");
        // Text additionalText = new Text(dockeOptionsComposite, SWT.BORDER | SWT.MULTI | SWT.WRAP | SWT.V_SCROLL);
        // GridData data = new GridData(GridData.FILL_HORIZONTAL);
        // data.heightHint = 60;
        // additionalText.setLayoutData(data);

    }

    private void addDockerOptionsListener() {
        ModifyListener optionListener = new ModifyListener() {

            @Override
            public void modifyText(ModifyEvent e) {
                if (remoteRadio.getSelection() && !isOptionValid(hostText, hostLabel.getText())) {
                    return;
                }
                if (!isOptionValid(imageText, imageLabel.getText())) {
                    return;
                }
                if (!isOptionValid(tagText, tagLabel.getText())) {
                    return;
                }
            }
        };

        hostText.addModifyListener(optionListener);
        imageText.addModifyListener(optionListener);
        tagText.addModifyListener(optionListener);

        remoteRadio.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                hostText.setEnabled(remoteRadio.getSelection());
                if (remoteRadio.getSelection() && !isOptionValid(hostText, hostLabel.getText())) {
                    return;
                }
                if (!isOptionValid(imageText, imageLabel.getText())) {
                    return;
                }
                if (!isOptionValid(tagText, tagLabel.getText())) {
                    return;
                }
            }

        });
    }

    private String getDefaultImageName(ProcessItem procesItem) {
        IFile pomFile = AggregatorPomsHelper
                .getItemPomFolder(procesItem.getProperty())
                .getFile(TalendMavenConstants.POM_FILE_NAME);
        String projectName = PomUtil.getPomProperty(pomFile, "talend.project.name.lowercase"); //$NON-NLS-1$
        String jobFolderPath = PomUtil.getPomProperty(pomFile, "talend.job.folder"); //$NON-NLS-1$
        String jobName = PomUtil.getPomProperty(pomFile, "talend.job.name").toLowerCase(); //$NON-NLS-1$
        return projectName + "/" + jobFolderPath + jobName; //$NON-NLS-1$
    }

    private String getDefaultImageNamePattern() {
        return "${talend.project.name.lowercase}/${talend.job.folder}%a"; //$NON-NLS-1$
    }

    private String getDefaultImageTag(ProcessItem procesItem) {
        return PomIdsHelper.getJobVersion(procesItem.getProperty());
    }

    private String getDefaultImageTagPattern() {
        return "${talend.docker.tag}"; //$NON-NLS-1$
    }

    private boolean isOptionValid(Text text, String label) {
        boolean isDockerValid = false;

        // If no error message is already displayed
        if (isValid) {
            if (StringUtils.isBlank(text.getText())) {
                setErrorMessage(Messages.getString("JavaJobScriptsExportWSWizardPage.DOCKER.errorMsg", label)); //$NON-NLS-1$
                setPageComplete(false);
                isDockerValid = false;
            } else {
                setErrorMessage(null);
                setPageComplete(true);
                isDockerValid = true;
            }
        }
        return isDockerValid;
    }

    protected void createOptionsForWS(Composite optionsGroup, Font font) {

        webXMLButton = new Button(optionsGroup, SWT.CHECK | SWT.LEFT);
        webXMLButton.setText(Messages.getString("JavaJobScriptsExportWSWizardPage.WEBXML")); //$NON-NLS-1$
        webXMLButton.setFont(font);
        GridData gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.horizontalSpan = 1;
        webXMLButton.setLayoutData(gd);
        webXMLButton.setSelection(true);

        configFileButton = new Button(optionsGroup, SWT.CHECK | SWT.LEFT);
        configFileButton.setText(Messages.getString("JavaJobScriptsExportWSWizardPage.ServerConfigFile")); //$NON-NLS-1$
        configFileButton.setFont(font);
        gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.horizontalSpan = 2;
        configFileButton.setLayoutData(gd);
        configFileButton.setSelection(true);

        wsddButton = new Button(optionsGroup, SWT.CHECK | SWT.LEFT);
        wsddButton.setText(Messages.getString("JavaJobScriptsExportWSWizardPage.WSDDFile")); //$NON-NLS-1$
        wsddButton.setFont(font);
        gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.horizontalSpan = 1;
        wsddButton.setLayoutData(gd);
        wsddButton.setSelection(true);

        wsdlButton = new Button(optionsGroup, SWT.CHECK | SWT.LEFT);
        wsdlButton.setText(Messages.getString("JavaJobScriptsExportWSWizardPage.WSDLFile")); //$NON-NLS-1$
        wsdlButton.setFont(font);
        gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.horizontalSpan = 2;
        wsdlButton.setLayoutData(gd);
        wsdlButton.setSelection(true);

        jobScriptButton = new Button(optionsGroup, SWT.CHECK | SWT.LEFT);
        jobScriptButton.setText(Messages.getString("JobScriptsExportWizardPage.sourceFiles")); //$NON-NLS-1$
        jobScriptButton.setSelection(true);
        jobScriptButton.setFont(font);
        gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.horizontalSpan = 1;
        jobScriptButton.setLayoutData(gd);

        axisLibButton = new Button(optionsGroup, SWT.CHECK | SWT.LEFT);
        axisLibButton.setText(Messages.getString("JavaJobScriptsExportWSWizardPage.AxisLib")); //$NON-NLS-1$
        axisLibButton.setFont(font);
        gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.horizontalSpan = 2;
        axisLibButton.setLayoutData(gd);
        axisLibButton.setSelection(true);

        contextButton = new Button(optionsGroup, SWT.CHECK | SWT.LEFT);
        contextButton.setText(Messages.getString("JobScriptsExportWizardPage.contextPerlScripts")); //$NON-NLS-1$
        contextButton.setSelection(true);
        contextButton.setFont(font);

        contextCombo = new Combo(optionsGroup, SWT.PUSH);

        applyToChildrenButton = new Button(optionsGroup, SWT.CHECK | SWT.LEFT);
        applyToChildrenButton.setText(Messages.getString("JavaJobScriptsExportWSWizardPage.ApplyToChildren")); //$NON-NLS-1$
        applyToChildrenButton.setSelection(true);

        if (Log4jPrefsSettingManager.getInstance().isLog4jEnable()) {
            log4jButton = new Button(optionsGroup, SWT.CHECK | SWT.LEFT);
            log4jButton.setText(Messages.getString("JavaJobScriptsExportWSWizardPage.LOG4jLEVEL")); //$NON-NLS-1$
            log4jButton.setSelection(true);
            log4jButton.setFont(font);
            log4jButton.setSelection(false);
            log4jButton.addSelectionListener(new SelectionAdapter() {

                @Override
                public void widgetSelected(SelectionEvent e) {
                    if (log4jButton.getSelection()) {
                        log4jLevelCombo.setEnabled(true);
                    } else {
                        log4jLevelCombo.setEnabled(false);
                    }
                }
            });

            log4jLevelCombo = new Combo(optionsGroup, SWT.PUSH);
            gd = new GridData();
            gd.horizontalSpan = 2;
            log4jLevelCombo.setLayoutData(gd);
            log4jLevelCombo.setEnabled(false);
        }
    }

    public String getExtractOption() {
        if (chkButton != null && !chkButton.isDisposed()) {
            return String.valueOf(chkButton.getSelection());
        } else {
            return null;
        }
    }

    public boolean isAddMavenScript() {
        if (addBSButton != null && !addBSButton.isDisposed()) {
            return addBSButton.getSelection();
        }

        return false;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.repository.ui.wizards.exportjob.JobScriptsExportWizardPage#checkExport()
     */
    @Override
    public boolean checkExport() {
        setErrorMessage(null);
        if (!super.checkExport()) {
            return false;
        }
        if (getCurrentExportType1().equals(JobExportType.OSGI) || getCurrentExportType1().equals(JobExportType.MSESB)) {
            if (isMultiNodes()) {
                setErrorMessage("This type of export support actually only a single job export.");
            }
        }
        if (getCheckNodes().length == 0) {
            setErrorMessage(Messages.getString("JavaJobScriptsExportWSWizardPage.needOneJobSelected"));
        }
        boolean noError = getErrorMessage() == null;

        // TESB-13867 Export limitations for ESB 'Jobs'
        // add extra checks.
        if (noError) {
            String errorMsg = presenter.extraCheck(getCurrentExportType1(), getCheckNodes());
            if (errorMsg != null) {
                setErrorMessage(errorMsg);
                noError = false;
            }
        }

        setPageComplete(noError);
        return noError;
    }

    @Override
    protected void updateOptionBySelection() {
        RepositoryNode[] selectedNodes = treeViewer.getCheckNodes();
        if (selectedNodes.length > 1) {
            if (imageText != null) {
                imageText.setText(getDefaultImageNamePattern());
                imageText.setEnabled(false);
            }
            if (tagText != null) {
                tagText.setText(getDefaultImageTagPattern());
                tagText.setEnabled(false);
            }
        } else if (selectedNodes.length == 1) {
            ProcessItem selectedProcessItem = ExportJobUtil.getProcessItem(Arrays.asList(selectedNodes));
            if (imageText != null) {
                imageText.setText(getDefaultImageName(selectedProcessItem));
                imageText.setEnabled(true);
            }
            if (tagText != null) {
                tagText.setText(getDefaultImageTag(selectedProcessItem));
                tagText.setEnabled(true);
            }
        }
    }

    @Override
    public boolean finish() {
        saveWidgetValues();

        return super.finish();
    }

}
