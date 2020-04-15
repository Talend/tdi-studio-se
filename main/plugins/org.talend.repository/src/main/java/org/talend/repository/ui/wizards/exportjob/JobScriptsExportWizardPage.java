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
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.ide.IDEWorkbenchMessages;
import org.eclipse.ui.internal.wizards.datatransfer.WizardFileSystemResourceExportPage1;
import org.talend.commons.ui.gmf.util.DisplayUtils;
import org.talend.commons.ui.runtime.exception.ExceptionHandler;
import org.talend.commons.ui.runtime.exception.MessageBoxExceptionHandler;
import org.talend.commons.utils.PasswordEncryptUtil;
import org.talend.core.PluginChecker;
import org.talend.core.model.metadata.MetadataTalendType;
import org.talend.core.model.process.IContext;
import org.talend.core.model.properties.Item;
import org.talend.core.model.properties.ProcessItem;
import org.talend.core.model.relationship.RelationshipItemBuilder;
import org.talend.core.model.repository.IRepositoryPrefConstants;
import org.talend.core.model.repository.IRepositoryViewObject;
import org.talend.core.model.utils.JavaResourcesHelper;
import org.talend.core.repository.constants.FileConstants;
import org.talend.core.runtime.CoreRuntimePlugin;
import org.talend.core.ui.context.nattableTree.ContextNatTableUtils;
import org.talend.core.ui.export.ArchiveFileExportOperationFullPath;
import org.talend.core.ui.export.FileSystemExporterFullPath;
import org.talend.core.views.IComponentSettingsView;
import org.talend.designer.core.model.utils.emf.talendfile.ContextParameterType;
import org.talend.designer.core.model.utils.emf.talendfile.TalendFileFactory;
import org.talend.repository.ProjectManager;
import org.talend.repository.documentation.ExportFileResource;
import org.talend.repository.i18n.Messages;
import org.talend.repository.model.IRepositoryNode.ENodeType;
import org.talend.repository.model.IRepositoryNode.EProperties;
import org.talend.repository.model.RepositoryNode;
import org.talend.repository.ui.utils.Log4jPrefsSettingManager;
import org.talend.repository.ui.utils.ZipToFile;
import org.talend.repository.ui.wizards.exportjob.JavaJobScriptsExportWSWizardPage.JobExportType;
import org.talend.repository.ui.wizards.exportjob.action.JobExportAction;
import org.talend.repository.ui.wizards.exportjob.scriptsmanager.BuildJobManager;
import org.talend.repository.ui.wizards.exportjob.scriptsmanager.JobScriptsManager;
import org.talend.repository.ui.wizards.exportjob.scriptsmanager.JobScriptsManager.ExportChoice;
import org.talend.repository.ui.wizards.exportjob.util.ExportJobUtil;
import org.talend.repository.utils.EmfModelUtils;
import org.talend.repository.utils.JobVersionUtils;

/**
 * Page of the Job Scripts Export Wizard. <br/>
 *
 * @referto WizardArchiveFileResourceExportPage1 $Id: JobScriptsExportWizardPage.java 1 2006-12-13 ä¸‹å�ˆ03:09:07 bqian
 *
 */
public abstract class JobScriptsExportWizardPage extends WizardFileSystemResourceExportPage1 {

    protected static final String DESTINATION_FILE = "destinationFile";//$NON-NLS-1$

    protected static final String ESB_EXPORT_TYPE = "esbExportType";//$NON-NLS-1$

    protected static final String ESB_SERVICE_NAME = "serviceName";//$NON-NLS-1$

    protected static final String ESB_CATEGORY = "category";//$NON-NLS-1$

    protected static final String QUERY_MESSAGE_NAME = "queryMessageName";//$NON-NLS-1$

    protected static final String OUTPUT_FILE_SUFFIX = FileConstants.ZIP_FILE_SUFFIX;

    private static final String BINARIES = Messages.getString("JavaJobScriptsExportWSWizardPage.POJO.optionType.binaries"); //$NON-NLS-1$

    private static final String SOURCES = Messages.getString("JavaJobScriptsExportWSWizardPage.POJO.optionType.sources") + " (Deprecated)"; //$NON-NLS-1$

    private static final String[] OPTION_TYPES = new String[] { BINARIES, SOURCES };

    protected boolean isEnterprise;

    // widgets
    protected Button shellLauncherButton;

    protected Button jobItemButton;

    protected Button contextButton;

    protected Button exportMSAsZipButton;

    protected Button jobScriptButton;

    protected Button log4jButton;

    protected Button executeTestsButton;

    protected Button addTestSourcesButton;

    protected ProcessItem processItem = null;

    protected Combo optionTypeCombo;

    protected Combo contextCombo;

    protected Combo launcherCombo;

    protected JobScriptsManager manager;

    protected Button applyToChildrenButton;

    protected Button setParametersValueButton;

    protected Button setParametersValueButton2;

    protected Combo log4jLevelCombo;

    protected RepositoryNode[] nodes;

    protected String zipOption;

    protected Button chkButton;

    String selectedJobVersion = "0.1"; //$NON-NLS-1$

    private String originalRootFolderName;

    protected Button addBSButton;

    protected Button addAntBSButton;

    protected Button addMavenBSButton;

    protected Button includeLibsButton;

    protected IStructuredSelection selection;

    protected ExportTreeViewer treeViewer;

    Collection<RepositoryNode> repositoryNodes = new ArrayList<RepositoryNode>();

    Set<RepositoryNode> checkedNodes = new HashSet<RepositoryNode>();

    Set<RepositoryNode> allNode = new HashSet<RepositoryNode>();

    /**
     * Create an instance of this class.
     *
     * @param name java.lang.String
     */
    @SuppressWarnings("unchecked")
    public JobScriptsExportWizardPage(String name, IStructuredSelection selection) {
        super(name, null);
        this.selection = selection;
        manager = null;
        if (selection != null) {
            nodes = (RepositoryNode[]) selection.toList().toArray(new RepositoryNode[selection.size()]);
        }
        isEnterprise = PluginChecker.isTIS();
    }

    protected RepositoryNode[] getCheckNodes() {
        return treeViewer.getCheckNodes();
    }

    protected ProcessItem getProcessItem() {
        if ((processItem == null) && (nodes != null) && (nodes.length >= 1)) {
            processItem = ExportJobUtil.getProcessItem(Arrays.asList(nodes));
        }
        return processItem;
    }

    protected void setProcessItem(ProcessItem value) {
        processItem = value;
    }

    public abstract JobScriptsManager createJobScriptsManager();

    /**
     * Create an instance of this class.
     *
     * @param selection the selection
     */
    public JobScriptsExportWizardPage(IStructuredSelection selection) {
        this("jobscriptsExportPage1", selection); //$NON-NLS-1$
        setDescription(Messages.getString("JobScriptsExportWizardPage.ExportJob")); //$NON-NLS-1$
        // setTitle(DataTransferMessages.ArchiveExport_exportTitle);
        setTitle(Messages.getString("DataTransferMessages.ArchiveExport_exportTitle")); //$NON-NLS-1$
    }

    /**
     * yzhang Comment method "setDefaultDestination".
     */
    protected void setDefaultDestination() {

        String userDir = System.getProperty("user.dir"); //$NON-NLS-1$
        IPath path = new Path(userDir);
        int length = nodes.length;
        String destinationFile = ""; //$NON-NLS-1$
        if (getDialogSettings() != null) {
            IDialogSettings section = getDialogSettings().getSection(DESTINATION_FILE);
            if (section != null) {
                destinationFile = section.get(DESTINATION_FILE);
            }
        }
        if (destinationFile == null || "".equals(destinationFile)) { //$NON-NLS-1$
            if (length == 1) {
                // TODOthis is changed by shenhaize first open ,it show contains in the combo
                path = path.append(getDefaultFileNameWithType());
            } else if (length > 1) {
                // i changed here ..
                path = path.append(getDefaultFileNameWithType());
            }
        } else {
            // path = new Path(destinationFile);
            if (CoreRuntimePlugin.getInstance().getDesignerCoreService()
                    .getPreferenceStoreBooleanValue(IRepositoryPrefConstants.USE_EXPORT_SAVE)) {
                path = new Path(destinationFile);
            } else {
                path = path.append(getDefaultFileNameWithType());
            }
        }
        setDestinationValue(path.toOSString());
    }

    protected void setDefaultDestinationForOSGI() {
        String bundleName = getDefaultFileNameWithType();
        String userDir = System.getProperty("user.dir"); //$NON-NLS-1$
        IPath path = new Path(userDir).append(bundleName);
        setDestinationValue(path.toOSString());
    }

    protected List<String> getDefaultFileName() {
        List<String> list = new ArrayList<String>();
        if (nodes.length >= 1) {
            String label = "";
            String version = "";
            if (nodes.length > 1) {
                label = ProjectManager.getInstance().getCurrentProject().getLabel();
            } else {
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
            }
            list.add(label);
            list.add(version);
            // return label;
            return list;
        }
        return null;

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
        SashForm sash = createExportTree(parent);

        GridLayout layout = new GridLayout();
        layout.marginHeight = 0;
        layout.marginBottom = 0;

        Composite composite = new Composite(sash, SWT.BORDER);
        composite.setLayout(layout);
        GridDataFactory.fillDefaults().grab(true, true).applyTo(composite);

        composite.setFont(parent.getFont());

        createDestinationGroup(composite);

        if (!isMultiNodes()) {
            createJobVersionGroup(composite);
        }

        createUnzipOptionGroup(composite);
        createOptionsGroup(composite);

        restoreResourceSpecificationWidgetValues(); // ie.- local
        restoreWidgetValues(); // ie.- subclass hook

        updateWidgetEnablements();
        setPageComplete(determinePageCompletion());
        setErrorMessage(null); // should not initially have error message

        setControl(sash);
        sash.setWeights(new int[] { 0, 2, 23 });
        giveFocusToDestination();

    }

    ICheckStateListener checkStateListener = new ICheckStateListener() {

        @Override
        public void checkStateChanged(CheckStateChangedEvent event) {
            checkExport();
            updateOptionBySelection();
        }

    };

    public boolean checkExport() {
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
            this.setErrorMessage(Messages.getString("JobScriptsExportWizardPage.chooseResource"));
        }
        return canExport;
    }

    protected void updateOptionBySelection() {

    }

    protected SashForm createExportTree(Composite parent) {
        // Using a protected method to provide the tree. LiXiaopeng 2011-9-21
        treeViewer = getExportTree();

        SashForm sashForm = treeViewer.createContents(parent);
        treeViewer.addCheckStateListener(checkStateListener);

        return sashForm;
    }

    /**
     * get ExportTreeViewer, subclass may override.
     */
    protected ExportTreeViewer getExportTree() {
        return new ExportTreeViewer(selection, this);
    }

    /**
     * ftang Comment method "createJobVersionGroup".
     *
     * @param composite
     */
    protected void createJobVersionGroup(Composite parent) {

        Group versionGroup = new Group(parent, SWT.NONE);
        GridDataFactory.fillDefaults().grab(true, false).applyTo(versionGroup);

        GridLayout layout = new GridLayout(2, false);
        versionGroup.setLayout(layout);

        versionGroup.setText(Messages.getString("JobScriptsExportWSWizardPage.newJobVersion", getProcessType())); //$NON-NLS-1$
        versionGroup.setFont(parent.getFont());

        Label label = new Label(versionGroup, SWT.NONE);
        label.setText(Messages.getString("JobScriptsExportWSWizardPage.newJobVersion.Label", getProcessType())); //$NON-NLS-1$

        final Combo versionCombo = new Combo(versionGroup, SWT.PUSH);


        String[] allVersions = JobVersionUtils.getAllVersions(nodes[0]);
        Arrays.sort(allVersions);
        String currentVersion = JobVersionUtils.getCurrentVersion(nodes[0]);
        versionCombo.setItems(allVersions);
        if (allVersions.length > 1) {
            versionCombo.add(RelationshipItemBuilder.LATEST_VERSION);
        }
        versionCombo.setText(currentVersion);
        selectedJobVersion = currentVersion;
        versionCombo.addSelectionListener(new SelectionListener() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                selectedJobVersion = versionCombo.getText();
                if(selectedJobVersion.equals(currentVersion) || selectedJobVersion.equals(RelationshipItemBuilder.LATEST_VERSION)){
                    executeTestsButton.setEnabled(true);
                }else{
                    executeTestsButton.setEnabled(false);
                    executeTestsButton.setSelection(false);
                }
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
                widgetSelected(e);
            }

        });
    }

    protected void createUnzipOptionGroup(Composite parent) {
        // options group
        Group optionsGroup = new Group(parent, SWT.NONE);
        GridDataFactory.fillDefaults().grab(true, false).applyTo(optionsGroup);
        optionsGroup.setLayout(new GridLayout());

        optionsGroup.setText("Extract zip file"); //$NON-NLS-1$
        optionsGroup.setFont(parent.getFont());

        chkButton = new Button(optionsGroup, SWT.CHECK);
        chkButton.setText(Messages.getString("JobScriptsExportWizardPage.extractZipFile")); //$NON-NLS-1$
        chkButton.setSelection(false);
        chkButton.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                chkButton.setSelection(chkButton.getSelection());
                zipOption = String.valueOf(chkButton.getSelection());
            }
        });
    }

    /*
     * It's not a good method to resovle the problem of null pointer, which is led by commenting the //
     * createResourcesGroup(composite); and createButtonsGroup(composite); (non-Javadoc)
     *
     * @see org.eclipse.ui.internal.wizards.datatransfer.WizardFileSystemResourceExportPage1#validateSourceGroup()
     */
    @Override
    public boolean validateSourceGroup() {
        return true;
    }

    /**
     * Create the export options specification widgets.
     *
     */
    @Override
    public void createOptionsGroupButtons(Group optionsGroup) {
        Font font = optionsGroup.getFont();
        optionsGroup.setLayout(new GridLayout(1, true));

        Composite left = new Composite(optionsGroup, SWT.NONE);
        GridDataFactory.fillDefaults().grab(true, false).applyTo(left);

        GridLayout gdlLeft = new GridLayout();
        gdlLeft.marginHeight = 0;
        gdlLeft.marginWidth = 0;
        left.setLayout(gdlLeft);

        createOptions(left, font);
    }

    /**
     * Create the buttons for the group that determine if the entire or selected directory structure should be created.
     *
     * @param optionsGroup
     * @param font
     */
    /**
     * DOC Administrator Comment method "createOptions".
     *
     * @param optionsGroup
     * @param font
     */
    public void createOptions(final Composite optionsGroup, Font font) {

        Composite optionsComposite = new Composite(optionsGroup, SWT.NONE);
        GridDataFactory.fillDefaults().grab(true, false).span(3, 1).applyTo(optionsComposite);

        GridLayout gdlOptionsComposite = new GridLayout(3, false);
        gdlOptionsComposite.marginHeight = 0;
        gdlOptionsComposite.marginWidth = 0;
        optionsComposite.setLayout(gdlOptionsComposite);

        optionTypeCombo = new Combo(optionsComposite, SWT.PUSH);
        GridDataFactory.swtDefaults().span(3, 1).applyTo(optionTypeCombo);

        optionTypeCombo.setItems(OPTION_TYPES);
        optionTypeCombo.select(0);
        optionTypeCombo.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                updateOptionStates();
            }
        });

        shellLauncherButton = new Button(optionsComposite, SWT.CHECK | SWT.LEFT);
        shellLauncherButton.setText(Messages.getString("JobScriptsExportWizardPage.shellLauncher")); //$NON-NLS-1$
        shellLauncherButton.setSelection(true);
        shellLauncherButton.setFont(font);

        launcherCombo = new Combo(optionsComposite, SWT.PUSH);
        GridData launcherGD = new GridData();
        launcherGD.horizontalSpan = 2;
        launcherCombo.setLayoutData(launcherGD);

        createContextOptions(font, optionsComposite);

        new Label(optionsComposite, SWT.NONE);
        new Label(optionsComposite, SWT.NONE);

        createLog4jOption(font, optionsComposite);

        jobItemButton = new Button(optionsComposite, SWT.CHECK | SWT.LEFT);
        jobItemButton.setText(Messages.getString("JobScriptsExportWizardPage.jobItems")); //$NON-NLS-1$
        jobItemButton.setFont(font);
        GridData jobItemGD = new GridData();
        jobItemGD.horizontalSpan = 3;
        jobItemButton.setLayoutData(jobItemGD);

        executeTestsButton = new Button(optionsComposite, SWT.CHECK | SWT.LEFT);
        executeTestsButton.setText(Messages.getString("JobScriptsExportWizardPage.executeTests")); //$NON-NLS-1$
        executeTestsButton.setFont(font);
        GridData executeTestsGD = new GridData();
        executeTestsGD.horizontalSpan = 3;
        executeTestsButton.setLayoutData(executeTestsGD);

        addTestSourcesButton = new Button(optionsComposite, SWT.CHECK | SWT.LEFT);
        addTestSourcesButton.setText(Messages.getString("JobScriptsExportWizardPage.addTestSources")); //$NON-NLS-1$
        addTestSourcesButton.setFont(font);
        GridData addTestSourcesGD = new GridData();
        addTestSourcesGD.horizontalSpan = 3;
        addTestSourcesButton.setLayoutData(addTestSourcesGD);
        addTestSourcesButton.setSelection(true);

        includeLibsButton = new Button(optionsComposite, SWT.CHECK | SWT.LEFT);
        includeLibsButton.setText(Messages.getString("JobScriptsExportWizardPage.includeLibs")); //$NON-NLS-1$
        includeLibsButton.setFont(font);
        GridData includeLibsGD = new GridData();
        includeLibsGD.horizontalSpan = 3;
        includeLibsButton.setLayoutData(includeLibsGD);

        jobScriptButton = new Button(optionsComposite, SWT.CHECK | SWT.LEFT);
        jobScriptButton.setText(Messages.getString("JobScriptsExportWizardPage.jobJavaSources")); //$NON-NLS-1$
        jobScriptButton.setFont(font);
        GridData jobScriptGD = new GridData();
        jobScriptGD.horizontalSpan = 3;
        jobScriptButton.setLayoutData(jobScriptGD);

        updateOptionStates();
    }

    protected void createContextOptions(Font font, Composite optionsComposite) {
        contextButton = new Button(optionsComposite, SWT.CHECK | SWT.LEFT);
        contextButton.setText(Messages.getString("JobScriptsExportWizardPage.contextPerlScripts")); //$NON-NLS-1$
        contextButton.setSelection(true);
        contextButton.setFont(font);
        contextButton.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                boolean selectContext = contextButton.getSelection();
                contextCombo.setEnabled(selectContext);
                applyToChildrenButton.setEnabled(selectContext);
            }
        });

        contextCombo = new Combo(optionsComposite, SWT.PUSH);
        contextCombo.setLayoutData(new GridData());

        Composite contextConfigComp = new Composite(optionsComposite, SWT.NONE);
        GridData contextConfigGridData = new GridData(GridData.FILL_HORIZONTAL);
        contextConfigGridData.verticalSpan = 2;
        contextConfigComp.setLayoutData(contextConfigGridData);
        GridLayout contextConfigLayout = new GridLayout(2, false);
        contextConfigComp.setLayout(contextConfigLayout);

        applyToChildrenButton = new Button(contextConfigComp, SWT.CHECK | SWT.LEFT);
        applyToChildrenButton.setText(Messages.getString("JobScriptsExportWizardPage.ApplyContextToChildren")); //$NON-NLS-1$
        GridData applyToChildrenGD = new GridData();
        applyToChildrenGD.horizontalSpan = 2;
        applyToChildrenButton.setLayoutData(applyToChildrenGD);

        setParametersValueButton = new Button(contextConfigComp, SWT.NONE);
        setParametersValueButton.setText(Messages.getString("JobScriptsExportWizardPage.OverrideParameterValues")); //$NON-NLS-1$
        setParametersValueButton.setSelection(false);

        setParametersValueButton2 = new Button(contextConfigComp, SWT.CHECK);
        setParametersValueButton2.setVisible(false);

        setParametersValueButton.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                if (manager == null) {
                    manager = createJobScriptsManager();
                }
                List<ContextParameterType> contextEditableResultValuesList = manager.getContextEditableResultValuesList();
                List<ContextParameterType> contextValueList = new ArrayList<ContextParameterType>();
                if (contextEditableResultValuesList == null) {
                    contextValueList = ExportJobUtil.getJobContextValues(getProcessItem(), contextCombo.getText());
                }
                ParametersValuesDialog dialog = new ParametersValuesDialog(getShell(), contextValueList,
                        contextEditableResultValuesList);
                int open = dialog.open();
                if (open == Dialog.OK) {
                    List<ContextParameterType> contextResultValuesList = dialog.getContextResultValuesList();
                    manager.setContextEditableResultValuesList(contextResultValuesList);
                    setParametersValueButton2.setSelection(true);
                } else {
                    setParametersValueButton2.setSelection(false);
                }
            }
        });
    }

    protected void createLog4jOption(Font font, Composite optionsComposite) {
        if (isEnterprise) {
            log4jButton = new Button(optionsComposite, SWT.CHECK | SWT.LEFT);
            log4jButton.setText(Messages.getString("JavaJobScriptsExportWSWizardPage.LOG4jLEVEL")); //$NON-NLS-1$
            log4jButton.setFont(font);
            log4jButton.setEnabled(Log4jPrefsSettingManager.getInstance().isLog4jEnable());
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
            log4jLevelCombo = new Combo(optionsComposite, SWT.PUSH);
            GridData log4jLevelGD = new GridData();
            log4jLevelGD.horizontalSpan = 2;
            log4jLevelCombo.setLayoutData(log4jLevelGD);
            log4jLevelCombo.setEnabled(false);
        }
    }

    private void updateOptionStates() {
        if (isEnterprise) {
            hideControl(optionTypeCombo, false);
            if (isBinaries()) {
                hideControl(executeTestsButton, false);
                hideControl(jobScriptButton, false);
                hideControl(addTestSourcesButton, true);
                hideControl(includeLibsButton, true);
                jobItemButton.setSelection(true);
            } else {
                hideControl(executeTestsButton, true);
                hideControl(jobScriptButton, true);
                hideControl(addTestSourcesButton, false);
                hideControl(includeLibsButton, false);
                jobItemButton.setSelection(false);
            }

            // TDQ-15391: when have tDqReportRun, must always export items.
            if (EmfModelUtils.getComponentByName(getProcessItem(), "tDqReportRun") != null) { //$NON-NLS-1$
                jobItemButton.setSelection(true);
                jobItemButton.setEnabled(false);
            }
            // TDQ-15391~
        } else {
            hideControl(optionTypeCombo, true);
            hideControl(executeTestsButton, true);
            hideControl(addTestSourcesButton, true);
            hideControl(includeLibsButton, true);
            hideControl(jobScriptButton, false);
        }
    }

    protected void hideControl(Control control, boolean hide) {
        Object layoutData = control.getLayoutData();
        if (layoutData instanceof GridData) {
            GridData data = (GridData) layoutData;
            data.exclude = hide;
            control.setLayoutData(data);
            control.setVisible(!hide);
            if (control.getParent() != null) {
                control.getParent().layout();
            }
        }
    }

    protected boolean isBinaries() {
        return optionTypeCombo != null && BINARIES.equals(optionTypeCombo.getText());
    }

    protected boolean isExecuteTests() {
        return isBinaries() && executeTestsButton != null && executeTestsButton.getSelection();
    }

    protected boolean isAddTestSources() {
        return !isBinaries() && addTestSourcesButton != null && addTestSourcesButton.getSelection();
    }

    protected boolean isIncludeLibs() {
        if (isBinaries()) {
            return true;
        }
        return includeLibsButton != null && includeLibsButton.getSelection();
    }

    protected boolean isAddJavaSources() {
        if (optionTypeCombo != null && optionTypeCombo.isVisible()) {
            return isBinaries() ? jobScriptButton.getSelection() : true;
        } else {
            return jobScriptButton == null ? false : jobScriptButton.getSelection();
        }
    }

    protected boolean isNeedLog4jLevel() {
        if (!Log4jPrefsSettingManager.getInstance().isLog4jEnable()) {
            return false;
        }
        if (log4jButton != null && !log4jButton.isDisposed()) {
            return log4jButton.getSelection();
        }
        return false;
    }

    protected String getLog4jLevel() {
        if (isNeedLog4jLevel() && log4jLevelCombo != null && !log4jLevelCombo.isDisposed()) {
            return log4jLevelCombo.getText();
        }
        return null;
    }

    protected boolean isNeedConext() {
        if (contextButton != null && !contextButton.isDisposed()) {
            return contextButton.getSelection();
        }
        return false;
    }

    protected String getContextName() {
        if (isNeedConext()) {
            if (contextCombo != null && !contextCombo.isDisposed()) {
                return contextCombo.getText();
            } else {
                return IContext.DEFAULT;
            }
        }
        return null;

    }

    protected String getLauncherName() {
        if (shellLauncherButton != null && !shellLauncherButton.isDisposed() && shellLauncherButton.getSelection()) {
            if (launcherCombo != null && !launcherCombo.isDisposed()) {
                return launcherCombo.getText();
            }
        }
        return null;
    }

    private void collectNodes(Map<String, Item> items, Object[] objects) {
        for (Object object : objects) {
            RepositoryNode repositoryNode = (RepositoryNode) object;
            collectNodes(items, repositoryNode);
        }
    }

    private void collectNodes(Map<String, Item> items, RepositoryNode repositoryNode) {
        IRepositoryViewObject repositoryObject = repositoryNode.getObject();
        if (repositoryObject != null) {
            if (repositoryObject.getRepositoryObjectType().isResourceItem()) {
                Item item = repositoryObject.getProperty().getItem();
                items.put(item.getProperty().getId(), item);
            }
        } else {
            if (repositoryNode.getParent() != null && repositoryNode.getParent().getObject() != null) {
                Item item = repositoryNode.getParent().getObject().getProperty().getItem();
                items.put(item.getProperty().getId(), item);
            }
        }
        if (this.treeViewer != null) {
            IContentProvider contentProvider = this.treeViewer.getFilteredCheckboxTree().getViewer().getContentProvider();
            if (contentProvider instanceof ITreeContentProvider) {
                Object[] children = ((ITreeContentProvider) contentProvider).getChildren(repositoryNode);
                collectNodes(items, children);
            }
        }
    }

    private static boolean isRepositoryFolder(RepositoryNode node) {
        final ENodeType type = node.getType();
        if (type == ENodeType.SIMPLE_FOLDER || type == ENodeType.STABLE_SYSTEM_FOLDER || type == ENodeType.SYSTEM_FOLDER) {
            return true;
        }
        return false;
    }

    /**
     * DOC zli JobScriptsExportWizardPage class global comment. Detailled comment
     */
    // for feature 11976
    class ParametersValuesDialog extends Dialog {

        private final String contextParameterName = Messages.getString("ParametersValuesDialog_Name"); //$NON-NLS-1$

        private final String contextParameterValue = Messages.getString("ParametersValuesDialog_Value"); //$NON-NLS-1$

        private TableViewer tableViewer;

        private Table table;

        private List<ContextParameterType> contextValueList;

        private List<ContextParameterType> contextEditableValuesList;

        private List<ContextParameterType> contextResultValuesList;

        private Button setContextButton;

        private Button addButton;

        private Button removeButton;

        private final String addParameterName = "new";

        /**
         * DOC zli ParametersValuesDialog constructor comment.
         *
         * @param parentShell
         */
        protected ParametersValuesDialog(Shell parentShell) {
            super(parentShell);
            setShellStyle(getShellStyle() | SWT.RESIZE | SWT.MAX | SWT.MIN);
        }

        protected ParametersValuesDialog(Shell parentShell, List<ContextParameterType> contextValueList,
                List<ContextParameterType> contextEditableResultValuesList) {
            super(parentShell);
            setShellStyle(getShellStyle() | SWT.RESIZE | SWT.MAX | SWT.MIN);
            this.contextValueList = contextValueList;
            // if set before, will use the set values. if not, only display the default parameters names.
            if (contextEditableResultValuesList == null) {
                contextEditableValuesList = initContextValues(contextValueList);
            } else {
                contextEditableValuesList = reviewContextValues(contextEditableResultValuesList);
            }
        }

        @Override
        protected Point getInitialSize() {
            Point p = super.getInitialSize();
            p.x = 600;
            p.y = 450;
            return p;
        }

        protected List<ContextParameterType> initContextValues(List<ContextParameterType> valueList) {

            List<ContextParameterType> list = new ArrayList<ContextParameterType>();

            for (int i = 0; i < valueList.size(); i++) {
                Object object = valueList.get(i);
                ContextParameterType contextParameterType = (ContextParameterType) object;
                list.add(createDuplicatedContextParameter(contextParameterType, true));
            }
            return list;
        }

        protected List<ContextParameterType> reviewContextValues(List<ContextParameterType> valueList) {

            List<ContextParameterType> list = new ArrayList<ContextParameterType>();

            for (int i = 0; i < valueList.size(); i++) {
                Object object = valueList.get(i);
                ContextParameterType contextParameterType = (ContextParameterType) object;
                list.add(createDuplicatedContextParameter(contextParameterType, false));
            }
            return list;
        }

        private ContextParameterType createDuplicatedContextParameter(ContextParameterType contextParameterType,
                boolean cleanValue) {
            // clone one from job
            ContextParameterType createContextParameterType = TalendFileFactory.eINSTANCE.createContextParameterType();
            createContextParameterType.setName(contextParameterType.getName());
            createContextParameterType.setType(contextParameterType.getType());
            createContextParameterType.setComment(contextParameterType.getComment());
            createContextParameterType.setRepositoryContextId(contextParameterType.getRepositoryContextId());
            if (cleanValue) {
                createContextParameterType.setValue(""); //$NON-NLS-1$
            } else {
                createContextParameterType.setValue(contextParameterType.getValue());
            }
            return createContextParameterType;
        }

        @Override
        protected Control createDialogArea(Composite parent) {
            Composite composite = (Composite) super.createDialogArea(parent);
            getShell().setText(Messages.getString("ParametersValuesDialog_Title")); //$NON-NLS-1$
            setTitle(Messages.getString("ParametersValuesDialog_Title")); //$NON-NLS-1$
            setMessage(Messages.getString("ParametersValuesDialog_Desc")); //$NON-NLS-1$

            tableViewer = new TableViewer(composite, SWT.BORDER | SWT.FULL_SELECTION);
            tableViewer.setContentProvider(new ContentProvider());
            tableViewer.setLabelProvider(new TableLabelProvider());
            tableViewer.setInput(contextEditableValuesList);
            table = tableViewer.getTable();
            TableLayout layout = new TableLayout();
            table.setLayout(layout);
            table.setLayoutData(new GridData(GridData.FILL_BOTH));
            table.setHeaderVisible(true);
            table.setLinesVisible(true);

            TableColumn column = new TableColumn(table, SWT.NONE);
            column.setText(contextParameterName);
            column.setWidth(150);

            column = new TableColumn(table, SWT.NONE);
            column.setText(contextParameterValue);
            column.setWidth(300);
            tableViewer.refresh();
            tableViewer.setColumnProperties(new String[] { contextParameterName, contextParameterValue });
            // set modifier
            tableViewer.setCellModifier(new ICellModifier() {

                @Override
                public void modify(Object element, String property, Object value) {
                    List<String> nameList = new ArrayList<String>();
                    for (int i = 0; i < contextEditableValuesList.size(); i++) {
                        String name = contextEditableValuesList.get(i).getName();
                        nameList.add(name);
                    }
                    TableItem tableItem = (TableItem) element;
                    ContextParameterType contextParamType = (ContextParameterType) tableItem.getData();
                    if (contextEditableValuesList.contains(contextParamType)) {
                        nameList.remove(contextParamType.getName());
                    }
                    if (property.equals(contextParameterName)) {
                        if (value == null || "".equals(value) || nameList.contains(value)) {
                            MessageDialog.openError(DisplayUtils.getDefaultShell(false),
                                    Messages.getString("ContextProcessSection.errorTitle"), //$NON-NLS-1$
                                    Messages.getString("ContextProcessSection.ParameterNameIsNotValid")); //$NON-NLS-1$
                        } else {
                            contextParamType.setName((String) value);
                        }
                    }
                    if (property.equals(contextParameterValue)) {
                        // if it's passord will encrypt.
                        contextParamType.setRawValue((String) value);
                    }
                    tableViewer.refresh(contextParamType);
                }

                @Override
                public Object getValue(Object element, String property) {
                    ContextParameterType contextParamType = (ContextParameterType) element;
                    if (property.equals(contextParameterName)) {
                        return contextParamType.getName();
                    }
                    if (property.equals(contextParameterValue)) {
                        return contextParamType.getRawValue();
                    }

                    return null;
                }

                @Override
                public boolean canModify(Object element, String property) {
                    return true;
                }
            });

            // set editor
            int columnCount = table.getColumnCount();
            CellEditor[] editors = new CellEditor[columnCount];
            editors[0] = new TextCellEditor(table);
            editors[1] = new TextCellEditor(table) {

                String beforeType = null;

                @Override
                protected void doSetValue(Object value) {
                    // record the style for password
                    int oldStyle = getStyle();
                    boolean changeControl = false;

                    Object obj = ((IStructuredSelection) tableViewer.getSelection()).getFirstElement();
                    if (obj != null && obj instanceof ContextParameterType) {
                        String type = ((ContextParameterType) obj).getType();
                        if (type != null && !type.equals(beforeType)) {
                            changeControl = true;
                            beforeType = type;
                        }
                        // if password
                        if (changeControl && PasswordEncryptUtil.isPasswordType(type)) {
                            setStyle(oldStyle | SWT.PASSWORD);
                        }
                        if (ContextNatTableUtils.isResourceType(type)) {
                            // to fix wrong display after click
                            setStyle(SWT.READ_ONLY);
                            String parameterValue = ((ContextParameterType) obj).getValue();
                            if (parameterValue.contains("|")) {
                                String[] part = parameterValue.split("\\|");
                                if (part.length > 1) {
                                    value = JavaResourcesHelper.getResouceClasspath(part[0], part[1]);
                                }
                            }
                        }
                    }

                    if (changeControl) {
                        // remove old control
                        dispose();
                        // re-create
                        create(table);
                    }

                    // reset the style
                    setStyle(oldStyle);

                    super.doSetValue(value);
                }
            };
            tableViewer.setCellEditors(editors);

            final Composite buttonsComposite = new Composite(composite, SWT.NONE);

            buttonsComposite.setLayout(new GridLayout(6, false));
            GridData gData = new GridData(GridData.FILL_HORIZONTAL);
            buttonsComposite.setLayoutData(gData);

            setContextButton = new Button(buttonsComposite, SWT.NONE);
            GridData gD = new GridData();
            gD.horizontalSpan = 2;
            setContextButton.setLayoutData(gD);
            setContextButton.setText("Values from selected context");
            setContextButton.addSelectionListener(new SelectionAdapter() {

                @Override
                public void widgetSelected(SelectionEvent e) {
                    for (ContextParameterType contextType : contextEditableValuesList) {
                        for (ContextParameterType context : contextValueList) {
                            if (contextType.getName().equals(context.getName())) {
                                contextType.setValue(context.getValue());
                            }
                        }
                    }
                    tableViewer.refresh(true);
                }
            });

            addButton = new Button(buttonsComposite, SWT.PUSH);
            addButton.setLayoutData(new GridData());
            addButton.setText("Add");
            addButton.addSelectionListener(new SelectionAdapter() {

                @Override
                public void widgetSelected(SelectionEvent e) {
                    ContextParameterType addContextParameterType = TalendFileFactory.eINSTANCE.createContextParameterType();
                    Integer numParam = new Integer(1);
                    boolean paramNameFound;
                    String paramName = null;
                    do { // look for a new name
                        paramNameFound = true;
                        paramName = addParameterName + numParam;
                        for (int i = 0; i < contextEditableValuesList.size(); i++) {
                            if (paramName.equals(contextEditableValuesList.get(i).getName())) {
                                paramNameFound = false;
                            }
                        }
                        if (!paramNameFound) {
                            numParam++;
                        }
                    } while (!paramNameFound);
                    addContextParameterType.setName(paramName);
                    addContextParameterType.setType(MetadataTalendType.getDefaultTalendType());
                    addContextParameterType.setValue("");
                    contextEditableValuesList.add(addContextParameterType);
                    tableViewer.refresh(true);
                }

            });

            removeButton = new Button(buttonsComposite, SWT.PUSH);
            removeButton.setLayoutData(new GridData());
            removeButton.setText("Remove");
            removeButton.addSelectionListener(new SelectionAdapter() {

                @Override
                public void widgetSelected(SelectionEvent e) {

                    TableItem[] items = tableViewer.getTable().getSelection();
                    if (items != null && items.length == 1) {
                        TableItem removeItem = items[0];
                        Object data = removeItem.getData();
                        if (data instanceof ContextParameterType) {
                            ContextParameterType removeContextType = (ContextParameterType) data;
                            contextEditableValuesList.remove(removeContextType);
                        }
                        tableViewer.refresh(true);
                    }
                }
            });

            return composite;
        }

        private List<ContextParameterType> getContextResultValuesList() {
            return this.contextResultValuesList;
        }

        private void setContextResultValuesList(List<ContextParameterType> contextResultValuesList) {
            this.contextResultValuesList = contextResultValuesList;
        }

        @Override
        protected void okPressed() {
            super.okPressed();
            setContextResultValuesList(contextEditableValuesList);
        }

        @Override
        protected void cancelPressed() {
            super.cancelPressed();
        }

    }

    /**
     * DOC zli JobScriptsExportWizardPage class global comment. Detailled comment
     */

    class TableLabelProvider extends LabelProvider implements ITableLabelProvider {

        @Override
        public String getColumnText(Object element, int columnIndex) {
            if (element instanceof ContextParameterType) {
                ContextParameterType contextParameter = (ContextParameterType) element;
                if (columnIndex == 0) {
                    return contextParameter.getName();
                }
                if (columnIndex == 1) {
                    String rawValue = contextParameter.getRawValue();
                    if (rawValue != null && PasswordEncryptUtil.isPasswordType(contextParameter.getType())) {
                        return PasswordEncryptUtil.getPasswordDisplay(rawValue);
                    }
                    if (ContextNatTableUtils.isResourceType(contextParameter.getType())) {
                        String parameterValue = contextParameter.getValue();
                        if (parameterValue.contains("|")) {
                            String[] part = parameterValue.split("\\|");
                            if (part.length > 1) {
                                String resource = JavaResourcesHelper.getResouceClasspath(part[0], part[1]);
                                if (resource != null) {
                                    return resource;
                                }
                            }
                        }
                    }
                    return rawValue;
                }
            }
            return ""; //$NON-NLS-1$
        }

        @Override
        public Image getColumnImage(Object element, int columnIndex) {
            return null;
        }

    }

    /**
     * DOC zli JobScriptsExportWizardPage class global comment. Detailled comment
     */
    class ContentProvider implements IStructuredContentProvider {

        @Override
        public Object[] getElements(Object inputElement) {
            if (inputElement instanceof List) {
                return ((List) inputElement).toArray();
            }
            return new Object[0];
        }

        @Override
        public void dispose() {
        }

        @Override
        public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
        }

        /*
         * (non-Javadoc)
         *
         * @see org.eclipse.jface.viewers.ITreeContentProvider#getChildren(java.lang.Object)
         */
        public Object[] getChildren(Object parentElement) {
            return getElements(parentElement);
        }

        /*
         * (non-Javadoc)
         *
         * @see org.eclipse.jface.viewers.ITreeContentProvider#getParent(java.lang.Object)
         */
        public Object getParent(Object element) {
            return null;
        }

        /*
         * (non-Javadoc)
         *
         * @see org.eclipse.jface.viewers.ITreeContentProvider#hasChildren(java.lang.Object)
         */
        public boolean hasChildren(Object element) {
            return false;
        }
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
     * Displays a Yes/No question to the user with the specified message and returns the user's response.
     *
     * @param message the question to ask
     * @return <code>true</code> for Yes, and <code>false</code> for No
     */
    @Override
    protected boolean queryYesNoQuestion(String message) {
        MessageDialog dialog = new MessageDialog(getContainer().getShell(), IDEWorkbenchMessages.Question, (Image) null, message,
                MessageDialog.NONE, 0, IDialogConstants.YES_LABEL, IDialogConstants.NO_LABEL) {

            @Override
            protected int getShellStyle() {
                return super.getShellStyle() | SWT.SHEET;
            }
        };
        // ensure yes is the default

        return dialog.open() == 0;
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
                if (!super.queryYesNoQuestion(Messages.getString("DataTransferMessages.ZipExport_alreadyExists"))) { //$NON-NLS-1$
                    // displayErrorDialog("Please enter another destination zip file.");
                    giveFocusToDestination();
                    return false;
                }
            } else {
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
        String targetPath = null;
        targetPath = getDestinationValue();
        if (this.selectedJobVersion != null && this.selectedJobVersion.equals(RelationshipItemBuilder.LATEST_VERSION)) {

            if (this.originalRootFolderName == null) {
                this.originalRootFolderName = manager.getRootFolderName(getDestinationValue());
            }
            String newFileName = this.originalRootFolderName + getSelectedJobVersion() + getOutputSuffix();
            targetPath = targetPath.substring(0, targetPath.lastIndexOf(File.separator) + 1) + newFileName;
            setDestinationValue(targetPath);
        }

        if (!ensureTargetDirectoryIsValid(targetPath)) {
            return false;
        }

        if (!ensureTargetFileIsValid(new File(targetPath))) {
            return false;
        }

        return true;
    }

    protected boolean ensureLog4jSettingIsValid() {
        return Log4jPrefsSettingManager.getInstance().isPreEnableAndStudioNot();
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
        } catch (InvocationTargetException e) {
            ExceptionHandler.process(e);
        } catch (InterruptedException e) {
            ExceptionHandler.process(e);

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

    // protected String getDestinationValueSU() {
    // return this.suDestinationFilePath != null ? this.suDestinationFilePath : ""; //$NON-NLS-1$
    //
    // }

    /**
     * The Finish button was pressed. Try to do the required work now and answer a boolean indicating success. If false
     * is returned then the wizard will not close.
     *
     * @returns boolean
     */
    @Override
    public boolean finish() {
        IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
        IComponentSettingsView compSettings = (IComponentSettingsView) page.findView(IComponentSettingsView.ID);
        if (compSettings != null) {
            compSettings.cleanDisplay();
        }
        if (treeViewer != null) {
            treeViewer.removeCheckStateListener(checkStateListener);
        }

        saveWidgetValues();

        if (manager == null) {
            manager = createJobScriptsManager();
        }

        if (!ensureTargetIsValid()) {
            return false;
        }

        JobExportType jobExportType = getCurrentExportType1();
        if (JobExportType.POJO.equals(jobExportType) || JobExportType.MSESB.equals(jobExportType)
                || JobExportType.OSGI.equals(jobExportType) || JobExportType.IMAGE.equals(jobExportType)
                || JobExportType.MSESB_IMAGE.equals(jobExportType)) {
            IRunnableWithProgress worker = new IRunnableWithProgress() {

                @Override
                public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
                    buildJobWithMaven(jobExportType, monitor);
                }
            };

            try {
                getContainer().run(true, true, worker);
            } catch (InvocationTargetException e) {
                MessageBoxExceptionHandler.process(e.getCause(), getShell());
                return false;
            } catch (InterruptedException e) {
                return false;
            }
        } else {
            List<ContextParameterType> contextEditableResultValuesList = null;
            if (manager != null) {
                contextEditableResultValuesList = manager.getContextEditableResultValuesList();
            }
            if (nodes.length == 1) {
                RepositoryNode node = nodes[0];
                if (node.getType() == ENodeType.SYSTEM_FOLDER) {
                    manager.setTopFolderName(ProjectManager.getInstance().getCurrentProject().getLabel());
                } else {
                    manager.setTopFolderName(getDefaultFileNameWithType());
                }
            } else {
                manager.setTopFolderName(getDefaultFileNameWithType());
            }

            // for feature:11976, recover back the old default manager value with ContextParameters
            if (contextEditableResultValuesList == null) {
                manager.setContextEditableResultValuesList(new ArrayList<ContextParameterType>());
            } else {
                manager.setContextEditableResultValuesList(contextEditableResultValuesList);
            }

            manager.setMultiNodes(isMultiNodes());
            // achen modify to fix bug 0006222

            IRunnableWithProgress worker = new JobExportAction(Arrays.asList(getCheckNodes()), getSelectedJobVersion(), manager,
                    originalRootFolderName, getProcessType());

            try {
                getContainer().run(false, true, worker);
            } catch (InvocationTargetException e) {
                MessageBoxExceptionHandler.process(e.getCause(), getShell());
                return false;
            } catch (InterruptedException e) {
                return false;
            }
        }

        // see bug 7181
        if (zipOption != null && zipOption.equals("true")) { //$NON-NLS-1$
            // unzip
            try {
                String zipFile;
                if (manager != null) {
                    zipFile = manager.getDestinationPath();
                } else {
                    zipFile = getDestinationValue();
                    int separatorIndex = zipFile.lastIndexOf(File.separator);
                    if (separatorIndex == -1) {
                        String userDir = System.getProperty("user.dir"); //$NON-NLS-1$
                        zipFile = userDir + File.separator + zipFile;
                    }
                }
                // Added by Marvin Wang on Feb.1, 2012 for bug TDI-18824
                File file = new File(zipFile);
                if (file.exists()) {
                    ZipToFile.unZipFile(zipFile, file.getParentFile().getAbsolutePath());
                }
            } catch (Exception e) {
                MessageBoxExceptionHandler.process(e, getShell());
                return false;
            }
        }

        if (treeViewer != null) {
            treeViewer.dispose();
        }

        // end
        return true;
    }

    protected boolean buildJobWithMaven(JobExportType jobExportType, IProgressMonitor monitor) {
        StringBuilder context = new StringBuilder();
        try {
            StringBuilder destination = new StringBuilder();

            Map<ExportChoice, Object> exportChoiceMap = new HashMap<>();
            List<RepositoryNode> checkedNodes = new ArrayList<>();
            Display.getDefault().syncExec(new Runnable() {

                @Override
                public void run() {
                    String contextTemp = (contextCombo == null || contextCombo.isDisposed())
                            ? processItem.getProcess().getDefaultContext()
                            : contextCombo.getText();
                    context.append(contextTemp);
                    String destinationValue = getDestinationValue();
                    destination.append(destinationValue);

                    exportChoiceMap.putAll(getExportChoiceMap());
                    checkedNodes.addAll(Arrays.asList(getCheckNodes()));
                }

            });
            int separatorIndex = destination.lastIndexOf(File.separator);
            String destinationStr = destination.toString();
            if (separatorIndex == -1) {
                String userDir = System.getProperty("user.dir"); //$NON-NLS-1$
                destinationStr = userDir + File.separator + destinationStr;
            }
            exportChoiceMap.put(ExportChoice.addStatistics, Boolean.TRUE);
            return BuildJobManager.getInstance().buildJobs(destinationStr, checkedNodes, getDefaultFileName(),
                    getSelectedJobVersion(), context.toString(), exportChoiceMap, jobExportType, monitor);

        } catch (Exception e) {
            Display.getDefault().asyncExec(new Runnable() {

                @Override
                public void run() {
                    MessageBoxExceptionHandler.process(e, getShell());
                }

            });
            return false;
        }
    }

    /**
     * Get the export operation.
     *
     * @param resourcesToExport
     * @return
     */
    public FileSystemExporterFullPath getUnzipExporterOperation(List<ExportFileResource> resourcesToExport) {
        String currentUnzipFile = getDestinationValue().replace("/", File.separator); //$NON-NLS-1$
        currentUnzipFile = currentUnzipFile.substring(0, currentUnzipFile.lastIndexOf(File.separator));
        FileSystemExporterFullPath exporterOperation = null;
        try {
            exporterOperation = new FileSystemExporterFullPath(resourcesToExport, currentUnzipFile);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            // e.printStackTrace();
            ExceptionHandler.process(e);
        }
        return exporterOperation;
    }

    /**
     * Comment method "setTopFolder".
     *
     * @param resourcesToExport
     * @param topFolder
     */
    public void setTopFolder(List<ExportFileResource> resourcesToExport, String topFolder) {
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

    protected Map<ExportChoice, Object> getExportChoiceMap() {
        Map<ExportChoice, Object> exportChoiceMap = new EnumMap<ExportChoice, Object>(ExportChoice.class);
        exportChoiceMap.put(ExportChoice.needLauncher, shellLauncherButton == null ? false : shellLauncherButton.getSelection());
        exportChoiceMap.put(ExportChoice.launcherName, getLauncherName());
        exportChoiceMap.put(ExportChoice.needSystemRoutine, Boolean.TRUE);
        exportChoiceMap.put(ExportChoice.needUserRoutine, Boolean.TRUE);
        exportChoiceMap.put(ExportChoice.needTalendLibraries, Boolean.TRUE);
        if (jobItemButton != null) {
            exportChoiceMap.put(ExportChoice.needJobItem, jobItemButton.getSelection());
        }
        exportChoiceMap.put(ExportChoice.needSourceCode, isAddJavaSources());
        exportChoiceMap.put(ExportChoice.needDependencies, Boolean.TRUE);
        exportChoiceMap.put(ExportChoice.needJobScript, Boolean.TRUE);
        exportChoiceMap.put(ExportChoice.needContext, isNeedConext());
        exportChoiceMap.put(ExportChoice.contextName, getContextName());
        if (applyToChildrenButton != null) {
            exportChoiceMap.put(ExportChoice.applyToChildren, applyToChildrenButton.getSelection());
        }
        // exportChoice.put(ExportChoice.needDependencies, exportDependencies.getSelection());
        if (setParametersValueButton2 != null) {
            exportChoiceMap.put(ExportChoice.needParameterValues, setParametersValueButton2.getSelection());
            if (setParametersValueButton2.getSelection()) {
                exportChoiceMap.put(ExportChoice.parameterValuesList, manager.getContextEditableResultValuesList());
            }
        }
        // exportChoice.put(ExportChoice.needGenerateCode, genCodeButton.getSelection());
        if (addAntBSButton != null) {
            exportChoiceMap.put(ExportChoice.needAntScript, addAntBSButton.getSelection());
        }
        if (addMavenBSButton != null) {
            exportChoiceMap.put(ExportChoice.needMavenScript, addMavenBSButton.getSelection());
        }
        exportChoiceMap.put(ExportChoice.binaries, isBinaries());
        exportChoiceMap.put(ExportChoice.executeTests, isExecuteTests());
        exportChoiceMap.put(ExportChoice.includeTestSource, isAddTestSources());
        exportChoiceMap.put(ExportChoice.includeLibs, isIncludeLibs());

        exportChoiceMap.put(ExportChoice.needLog4jLevel, isNeedLog4jLevel());
        exportChoiceMap.put(ExportChoice.log4jLevel, getLog4jLevel());

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
        // this is the entrance to the answer .. shenhaize.
        // System.out.println(destinationText);
        // String b = destinationText.substring(0, (destinationText.length() - 4));
        // return (b + destinationText.subSequence((destinationText.length() - 4), destinationText.length()));
        // System.out.println(destinationText + " " + idealSuffix);
        if (destinationText.endsWith(this.getSelectedJobVersion() + this.getOutputSuffix())) {
            return destinationText;
        }
        return destinationText;

    }

    /**
     * Answer the suffix that files exported from this wizard should have. If this suffix is a file extension (which is
     * typically the case) then it must include the leading period character.
     *
     */
    protected String getOutputSuffix() {
        return OUTPUT_FILE_SUFFIX;
    }

    /**
     * Open an appropriate destination browser so that the user can specify a source to import from.
     */
    @Override
    protected void handleDestinationBrowseButtonPressed() {
        FileDialog dialog = new FileDialog(getContainer().getShell(), SWT.SAVE);
        dialog.setFilterExtensions(new String[] { "*.zip", "*.*" }); //$NON-NLS-1$ //$NON-NLS-2$
        dialog.setText(""); //$NON-NLS-1$
        dialog.setFileName(this.getDefaultFileName().get(0));
        String currentSourceString = getDestinationValue();
        int lastSeparatorIndex = currentSourceString.lastIndexOf(File.separator);
        if (lastSeparatorIndex != -1) {
            dialog.setFilterPath(currentSourceString.substring(0, lastSeparatorIndex));
        }
        String selectedFileName = dialog.open();
        if (selectedFileName != null && !selectedFileName.endsWith(this.getOutputSuffix())) {
            selectedFileName += this.getOutputSuffix();
        }

        // when user change the name of job,will add the version auto
        if (selectedFileName != null && !selectedFileName.endsWith(this.getSelectedJobVersion() + this.getOutputSuffix())) {
            String b = selectedFileName.substring(0, (selectedFileName.length() - 4));
            File file = new File(b);

            String str = file.getName();

            String s = this.getDefaultFileName().get(0);

            if (str.equals(s)) {
                selectedFileName = b + "_" + this.getDefaultFileName().get(1) + this.getOutputSuffix(); //$NON-NLS-1$
            } else {
                selectedFileName = b + this.getOutputSuffix();
            }

        }
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

    /**
     * ftang Comment method "isMultiNodes".
     *
     * @return
     */
    public boolean isMultiNodes() {
        if (treeViewer == null) {
            return false;
        }
        return this.getCheckNodes().length > 1;
    }

    /**
     * ftang Comment method "getSelectedJobVersion".
     *
     * @return
     */
    public String getSelectedJobVersion() {
        return selectedJobVersion;
    }

    @Override
    protected boolean validateDestinationGroup() {
        return super.validateDestinationGroup() && this.checkExport();
    }

    protected String getDefaultFileNameWithType() {
        String version = "";
        List<String> defaultFileName = getDefaultFileName();
        if (defaultFileName.get(1) != null && !"".equals(defaultFileName.get(1))) {
            version = ((JobExportType.OSGI.equals(getCurrentExportType1())) ? "-" : "_") + defaultFileName.get(1);
        }
        String fileName = defaultFileName.get(0) + version + getOutputSuffix();
        return fileName;
    }

    public JobExportType getCurrentExportType1() {
        return JobExportType.POJO;
    }

    /**
     * GLIU add for fixing TESB-4975 default is "Job"
     *
     * @return
     */
    protected String getProcessType() {
        return "Job";
    }

}
