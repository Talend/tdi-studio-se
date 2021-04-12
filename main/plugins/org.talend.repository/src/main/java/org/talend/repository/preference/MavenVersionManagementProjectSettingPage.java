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
package org.talend.repository.preference;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.gef.commands.Command;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.navigator.NavigatorDecoratingLabelProvider;
import org.talend.commons.exception.ExceptionHandler;
import org.talend.commons.exception.LoginException;
import org.talend.commons.exception.PersistenceException;
import org.talend.commons.runtime.model.repository.ERepositoryStatus;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.IESBService;
import org.talend.core.model.process.IProcess2;
import org.talend.core.model.properties.Item;
import org.talend.core.model.properties.Property;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.model.repository.Folder;
import org.talend.core.model.repository.RepositoryManager;
import org.talend.core.runtime.maven.MavenConstants;
import org.talend.core.runtime.projectsetting.ProjectPreferenceManager;
import org.talend.core.ui.images.CoreImageProvider;
import org.talend.core.ui.process.IGEFProcess;
import org.talend.core.ui.services.IDesignerCoreUIService;
import org.talend.designer.core.IMultiPageTalendEditor;
import org.talend.designer.maven.DesignerMavenPlugin;
import org.talend.designer.maven.tools.AggregatorPomsHelper;
import org.talend.repository.RepositoryWorkUnit;
import org.talend.repository.i18n.Messages;
import org.talend.repository.model.IRepositoryNode.EProperties;
import org.talend.repository.model.ItemVersionObject;
import org.talend.repository.model.RepositoryNode;
import org.talend.repository.utils.MavenVersionUtils;

public class MavenVersionManagementProjectSettingPage extends AbstractVersionManagementProjectSettingPage {

    private final Color COLOR_RED = Display.getDefault().getSystemColor(SWT.COLOR_RED);

    private final Color COLOR_WHITE = Display.getDefault().getSystemColor(SWT.COLOR_WHITE);

    // Should be the same with CamelRepositoryNodeType.repositoryRouteDesinsType
    private final String NODENAME_ROUTE_DESIGNS = "ROUTE_DESIGNS"; //$NON-NLS-1$

    // Should be the same with ESBRepositoryNodeType.SERVICES
    private final String NODENAME_SERVICES = "SERVICES"; //$NON-NLS-1$

    private Button applyVersionButton;

    private String appliedFixedVersion;

    private Text projectVersionText;

    private Text newVersionText;

    private Button useSnapshot;

    private Button applyDetail;

    private ProjectPreferenceManager projectPreferenceManager;

    private IESBService esbService;

    private String oldProjectVersion;

    private boolean oldUseSnapshot;

    private List<Item> modifiedJobs = new ArrayList<>();

    private boolean appliedFlag = false;

    public MavenVersionManagementProjectSettingPage() {
        super();
        this.noDefaultAndApplyButton();
        projectPreferenceManager = DesignerMavenPlugin.getPlugin().getProjectPreferenceManager();
        appliedFixedVersion = projectPreferenceManager.getValue(MavenConstants.PROJECT_VERSION);
        if (GlobalServiceRegister.getDefault().isServiceRegistered(IESBService.class)) {
            esbService = (IESBService) GlobalServiceRegister.getDefault().getService(IESBService.class);
        }
    }

    @Override
    protected boolean filterRepositoryNode(RepositoryNode node) {
        if (node == null) {
            return false;
        }
        if (node.isBin()) {
            return false;
        }
        if (node.getObject() != null) {
            ERepositoryStatus status = FACTORY.getStatus(node.getObject());
            if (status == ERepositoryStatus.LOCK_BY_OTHER) {
                return false;
            }
            if (node.getObject().isDeleted()) {
                return false;
            }
        }
        ERepositoryObjectType type = node.getObjectType();
        if (type == null) {
            if (node.getProperties(EProperties.LABEL) instanceof ERepositoryObjectType) {
                type = (ERepositoryObjectType) node.getProperties(EProperties.LABEL);
            }
            if (type == null) {
                return false;
            }
        }
        if (type == ERepositoryObjectType.FOLDER) {
            if (node.getObject() instanceof Folder) {
                type = ((Folder) node.getObject()).getContentType();
            }
        }
        if (type == ERepositoryObjectType.PROCESS || type == ERepositoryObjectType.PROCESS_MR
                || type == ERepositoryObjectType.TEST_CONTAINER
                || type == ERepositoryObjectType.valueOf(NODENAME_ROUTE_DESIGNS)
                || type == ERepositoryObjectType.valueOf(NODENAME_SERVICES) || type == ERepositoryObjectType.PROCESS_ROUTE
                || type == ERepositoryObjectType.PROCESS_SPARK || type == ERepositoryObjectType.PROCESS_SPARKSTREAMING
                || type == ERepositoryObjectType.PROCESS_STORM) {
            return true;
        }
        return false;
    }

    @Override
    protected void addItemTableViewer(Composite rightComposite) {

        Composite composite = new Composite(rightComposite, SWT.NONE);
        composite.setLayout(new GridLayout());
        GridDataFactory.fillDefaults().grab(true, true).applyTo(composite);

        addItemsOption(composite);

        Composite tableComposite = new Composite(composite, SWT.BORDER);
        tableComposite.setLayout(new GridLayout(2, false));
        GridDataFactory.fillDefaults().grab(true, true).applyTo(tableComposite);
        itemTable = new Table(tableComposite, SWT.MULTI | SWT.BORDER);
        GridDataFactory.fillDefaults().grab(true, true).applyTo(itemTable);
        itemTable.setHeaderVisible(true);
        itemTable.setLinesVisible(true);
        itemTable.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                if (fixedVersionButton.getSelection()) {
                    updateFixedVersionSelection();
                } else if (eachVersionButton.getSelection()) {
                    updateEachVersionSelection();
                } else if (useJobVersionButton.getSelection()) {
                    updateUseJobVersionSelection();
                }
            }

        });

        MenuManager manager = new MenuManager();
        manager.add(new RemoveAction());
        Menu menu = manager.createContextMenu(itemTable);
        itemTable.setMenu(menu);

        Composite detailComposite = new Composite(tableComposite, SWT.NONE);
        detailComposite.setLayout(new GridLayout(2, false));
        GridDataFactory.fillDefaults().grab(true, true).applyTo(detailComposite);
        Label newVersionLabel = new Label(detailComposite, SWT.NONE);
        newVersionLabel.setText(Messages.getString("VersionManagementDialog.NewVersion")); //$NON-NLS-1$
        newVersionText = new Text(detailComposite, SWT.BORDER);
        newVersionText.setText(appliedFixedVersion);
        newVersionText.setEnabled(false);
        GridDataFactory.fillDefaults().grab(true, false).applyTo(newVersionText);
        useSnapshot = new Button(detailComposite, SWT.CHECK);
        useSnapshot.setText(Messages.getString("VersionManagementDialog.useSnapshot")); //$NON-NLS-1$
        GridData gridData = new GridData();
        gridData.horizontalSpan = 2;
        useSnapshot.setLayoutData(gridData);
        useSnapshot.setSelection(false);
        useSnapshot.setVisible(false);

        applyDetail = new Button(detailComposite, SWT.NONE);
        applyDetail.setText(Messages.getString("VersionManagementDialog.applyDetail")); //$NON-NLS-1$
        applyDetail.setVisible(false);
        createItemTableColumns();

        newVersionText.addModifyListener(new ModifyListener() {

            @Override
            public void modifyText(ModifyEvent e) {
                String version = newVersionText.getText();
                if (newVersionText.isEnabled()) {
                    applyDetail.setEnabled(checkVersionPattern(newVersionText, version));
                }
            }
        });

        applyDetail.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                for (TableItem tableItem : itemTable.getSelection()) {
                    ItemVersionObject object = (ItemVersionObject) tableItem.getData();
                    object.setNewVersion(newVersionText.getText());
                    object.setUseSnapshotNew(useSnapshot.getSelection());
                    tableItem.setText(1, newVersionText.getText());
                }
                itemTable.layout();
                checkValid();
            }
        });

    }

    @Override
    protected void addItemsOption(Composite parent) {
        Group option = new Group(parent, SWT.NONE);
        option.setLayout(new GridLayout(3, false));
        option.setText(Messages.getString("VersionManagementDialog.Options")); //$NON-NLS-1$
        GridDataFactory.swtDefaults().align(SWT.FILL, SWT.CENTER).applyTo(option);

        Composite projectVersionComposite = new Composite(option, SWT.NONE);
        GridLayout projectVersionLayout = new GridLayout(8, false);
        projectVersionLayout.horizontalSpacing = 1;
        projectVersionLayout.verticalSpacing = 0;
        projectVersionLayout.marginHeight = 0;
        projectVersionLayout.marginWidth = 0;
        projectVersionComposite.setLayout(projectVersionLayout);

        GridData projectVersionCompositeData = new GridData();
        projectVersionCompositeData.horizontalSpan = 3;
        projectVersionComposite.setLayoutData(projectVersionCompositeData);

        Label projectVersionlabel = new Label(projectVersionComposite, SWT.NONE);
        projectVersionlabel.setText(Messages.getString("VersionManagementDialog.projectVersion")); //$NON-NLS-1$
        GridData projectVersionlabelData = new GridData();
        projectVersionlabelData.widthHint = 100;
        projectVersionlabelData.minimumWidth = 80;
        projectVersionlabelData.horizontalIndent = 20;
        projectVersionlabel.setLayoutData(projectVersionlabelData);

        projectVersionText = new Text(projectVersionComposite, SWT.BORDER);
        GridData projectVersionTextData = new GridData();
        projectVersionTextData.widthHint = 100;
        projectVersionTextData.horizontalIndent = 5;
        projectVersionText.setLayoutData(projectVersionTextData);
        oldProjectVersion = projectPreferenceManager.getValue(MavenConstants.PROJECT_VERSION);
        projectVersionText.setText(oldProjectVersion);

        globalSnapshotCheckbox = new Button(projectVersionComposite, SWT.CHECK);
        globalSnapshotCheckbox.setText(Messages.getString("VersionManagementDialog.useSnapshot")); //$NON-NLS-1$
        GridData globalSnapshotCheckboxData = new GridData();
        globalSnapshotCheckboxData.horizontalIndent = 10;
        globalSnapshotCheckbox.setLayoutData(globalSnapshotCheckboxData);
        oldUseSnapshot = projectPreferenceManager.getBoolean(MavenConstants.NAME_PUBLISH_AS_SNAPSHOT);
        globalSnapshotCheckbox.setSelection(oldUseSnapshot);

        fixedVersionButton = new Button(option, SWT.RADIO);
        fixedVersionButton.setText(Messages.getString("VersionManagementDialog.useProjectVersion")); //$NON-NLS-1$
        fixedVersionButton.setSelection(true); // default

        applyVersionButton = new Button(option, SWT.NONE);
        applyVersionButton.setText(Messages.getString("VersionManagementDialog.applyVersion")); //$NON-NLS-1$

        subjobs = new Button(option, SWT.NONE);
        subjobs.setText(Messages.getString("VersionManagementDialog.subjob2")); //$NON-NLS-1$
        subjobs.setEnabled(false);

        eachVersionButton = new Button(option, SWT.RADIO);
        eachVersionButton.setText(Messages.getString("VersionManagementDialog.eachMavenVersion")); //$NON-NLS-1$
        GridData eachVersionButtonData = new GridData();
        eachVersionButtonData.horizontalSpan = 3;
        eachVersionButton.setLayoutData(eachVersionButtonData);

        useJobVersionButton = new Button(option, SWT.RADIO);
        useJobVersionButton.setText(Messages.getString("VersionManagementDialog.useJobVersion")); //$NON-NLS-1$
        GridData useJobVersionButtonData = new GridData();
        useJobVersionButtonData.horizontalSpan = 3;
        useJobVersionButton.setLayoutData(useJobVersionButtonData);
        // event
        fixedVersionButton.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                if (fixedVersionButton.getSelection()) {
                    updateFixedVersionSelection();
                    for (TableItem tableItem : itemTable.getItems()) {
                        if (tableItem.getData() instanceof ItemVersionObject) {
                            ItemVersionObject data = (ItemVersionObject) tableItem.getData();
                            if (null != data) {
                                tableItem.setText(1,
                                        appliedFlag && StringUtils.isNotEmpty(appliedFixedVersion) ? appliedFixedVersion
                                                : data.getNewVersion());
                            }
                        }
                    }
                } else {
                    applyVersionButton.setEnabled(false);
                }
            }
        });

        projectVersionText.addModifyListener(new ModifyListener() {

            @Override
            public void modifyText(ModifyEvent e) {
                String version = projectVersionText.getText();
                if (MavenVersionUtils.isValidMavenVersion(version, useSnapshot())) {
                    if (fixedVersionButton.getSelection()) {
                        applyVersionButton.setEnabled(true);
                    }
                    projectVersionText.setBackground(COLOR_WHITE);
                    projectVersionText.setToolTipText(""); //$NON-NLS-1$
                } else {
                    if (fixedVersionButton.getSelection()) {
                        applyVersionButton.setEnabled(false);
                    }
                    projectVersionText.setBackground(COLOR_RED);
                    projectVersionText.setToolTipText(Messages.getString("VersionManagementDialog.valueWarning")); //$NON-NLS-1$
                }
                checkValid();
            }
        });

        applyVersionButton.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                String version = projectVersionText.getText();
                if (itemTable.getItemCount() > 0 && !StringUtils.isBlank(version)) {
                    appliedFixedVersion = version;
                    newVersionText.setText(appliedFixedVersion);
                    appliedFlag = true;
                    for (TableItem tableItem : itemTable.getItems()) {
                        tableItem.setText(1, appliedFixedVersion);
                    }
                    itemTable.layout();
                }
            }
        });

        subjobs.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                selectSubjob();
            }
        });

        eachVersionButton.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                useSnapshot.setVisible(eachVersionButton.getSelection());
                applyDetail.setVisible(eachVersionButton.getSelection());
                if (eachVersionButton.getSelection()) {
                    updateEachVersionSelection();
                    for (TableItem tableItem : itemTable.getItems()) {
                        if (tableItem.getData() instanceof ItemVersionObject) {
                            ItemVersionObject data = (ItemVersionObject) tableItem.getData();
                            if (null != data) {
                                tableItem.setText(1, data.getNewVersion());
                            }
                        }
                    }
                }
            }
        });

        useJobVersionButton.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                if (useJobVersionButton.getSelection()) {
                    updateUseJobVersionSelection();
                    for (TableItem tableItem : itemTable.getItems()) {
                        ItemVersionObject object = (ItemVersionObject) tableItem.getData();
                        String jobDefaultVersion = MavenVersionUtils
                                .getDefaultVersion(object.getItem().getProperty().getVersion());
                        tableItem.setText(1, jobDefaultVersion);
                    }
                    itemTable.layout();
                }
            }
        });

    }

    private void updateFixedVersionSelection() {
        applyVersionButton.setEnabled(true);
        newVersionText.setEnabled(false);
        if (itemTable.getSelectionCount() > 0) {
            newVersionText.setText(appliedFixedVersion);
        }
    }

    private void updateUseJobVersionSelection() {
        newVersionText.setEnabled(false);
        if (itemTable.getSelectionCount() > 0) {
            if (itemTable.getSelectionCount() == 1) {
                ItemVersionObject object = getSelectedTableItem().get(0);
                String jobDefaultVersion = MavenVersionUtils.getDefaultVersion(object.getItem().getProperty().getVersion());
                newVersionText.setText(jobDefaultVersion);
            } else {
                newVersionText.setText(""); //$NON-NLS-1$
            }
        }
    }

    private void updateEachVersionSelection() {
        if (itemTable.getSelectionCount() > 0) {
            newVersionText.setEnabled(true);
            useSnapshot.setEnabled(true);
            if (itemTable.getSelectionCount() == 1) {
                ItemVersionObject object = getSelectedTableItem().get(0);
                String newVersion = object.getNewVersion();
                if (newVersion.equals(object.getOldVersion())) {
                    newVersion = MavenVersionUtils.increaseVersion(newVersion);
                }
                newVersionText.setText(newVersion);
                useSnapshot.setSelection(object.isUseSnapshotNew());
            } else {
                newVersionText.setText(""); //$NON-NLS-1$
                useSnapshot.setSelection(false);
            }
        } else {
            newVersionText.setEnabled(false);
            useSnapshot.setEnabled(false);
            applyDetail.setEnabled(false);
            newVersionText.setText(""); //$NON-NLS-1$
            useSnapshot.setSelection(false);
        }
    }

    @Override
    protected void createItemTableColumns() {
        TableColumn itemColumn = new TableColumn(itemTable, SWT.CENTER);
        itemColumn.setText(Messages.getString("VersionManagementDialog.Items")); //$NON-NLS-1$
        itemColumn.setWidth(100);

        TableColumn oldVersionColumn = new TableColumn(itemTable, SWT.CENTER);
        oldVersionColumn.setText(Messages.getString("VersionManagementDialog.Version")); //$NON-NLS-1$
        oldVersionColumn.setWidth(100);
    }

    @Override
    protected void addItemElements(List<ItemVersionObject> elements) {
        if (elements == null || elements.isEmpty()) {
            return;
        }
        itemTable.setRedraw(false);

        for (final ItemVersionObject object : elements) {
            final TableItem tableItem = new TableItem(itemTable, SWT.CENTER);
            tableItem.setData(object);
            Item item = object.getItem();
            IBaseLabelProvider labelProvider = treeViewer.getLabelProvider();
            Image itemsImage = null;
            if (labelProvider instanceof NavigatorDecoratingLabelProvider) {
                NavigatorDecoratingLabelProvider navigatorProvider = (NavigatorDecoratingLabelProvider) labelProvider;
                itemsImage = navigatorProvider.getImage(object.getRepositoryNode());
            } else {
                ERepositoryObjectType itemType = ERepositoryObjectType.getItemType(item);
                itemsImage = getItemsImage(CoreImageProvider.getIcon(itemType));
            }
            tableItem.setImage(itemsImage);
            tableItem.setText(item.getProperty().getLabel());
            // version
            if (fixedVersionButton.getSelection()) {
                tableItem.setText(1, appliedFlag && StringUtils.isNotEmpty(appliedFixedVersion) ? appliedFixedVersion
                        : object.getNewVersion());
            } else if (eachVersionButton.getSelection()) {
                updateEachVersionSelection();
                tableItem.setText(1, object.getNewVersion());
            } else if (useJobVersionButton.getSelection()) {
                    String jobDefaultVersion = MavenVersionUtils
                        .getDefaultVersion(object.getItem().getProperty().getVersion());
                tableItem.setText(1, jobDefaultVersion);
            } else {
                tableItem.setText(1, object.getNewVersion());
            }
        }
        itemTable.setRedraw(true);
    }

    private boolean useSnapshot() {
        return globalSnapshotCheckbox.getSelection();
    }

    private void checkValid() {
        if (!MavenVersionUtils.isValidMavenVersion(projectVersionText.getText(), useSnapshot())) {
            setValid(false);
            return;
        }
        for (ItemVersionObject object : checkedObjects) {
            String version = object.getNewVersion();
            if (version.endsWith(MavenConstants.SNAPSHOT)) {
                version = version.replace(MavenConstants.SNAPSHOT, ""); //$NON-NLS-1$
            }
            if (!MavenVersionUtils.isValidMavenVersion(version, useSnapshot())) {
                setValid(false);
                return;
            }
        }
        setValid(true);
    }

    private boolean checkVersionPattern(Text text, String version) {
        boolean isValid = MavenVersionUtils.isValidMavenVersion(version, useSnapshot());
        if (isValid) {
            text.setBackground(COLOR_WHITE);
            text.setToolTipText(""); //$NON-NLS-1$
        } else {
            text.setBackground(COLOR_RED);
            text.setToolTipText(Messages.getString("VersionManagementDialog.valueWarning")); //$NON-NLS-1$
        }
        return isValid;
    }

    protected String getNewVersionWithOption(ItemVersionObject object) {
        String newVersion;
        if (fixedVersionButton.getSelection()) {
            newVersion = appliedFixedVersion;
        } else if (eachVersionButton.getSelection()) {
            newVersion = object.getNewVersion();
        } else {
            newVersion = MavenVersionUtils.getDefaultVersion(object.getItem().getProperty().getVersion());
        }
        return newVersion;
    }

    @Override
    public boolean performOk() {
        if (super.performOk()) {
            if (projectPreferenceManager != null && projectVersionText != null && globalSnapshotCheckbox != null) {
                projectPreferenceManager.setValue(MavenConstants.PROJECT_VERSION, projectVersionText.getText());
                projectPreferenceManager.setValue(MavenConstants.NAME_PUBLISH_AS_SNAPSHOT, useSnapshot());
                projectPreferenceManager.save();
                boolean isProjectVersionModified = !projectVersionText.getText().equals(oldProjectVersion)
                        || useSnapshot() != oldUseSnapshot;
                try {
                    AggregatorPomsHelper helper = new AggregatorPomsHelper();
                    if (isProjectVersionModified) {
                        helper.syncAllPoms();
                    } else if (!modifiedJobs.isEmpty()) {
                        helper.syncJobPoms(modifiedJobs);
                    }
                } catch (Exception e) {
                    ExceptionHandler.process(e);
                }
            }
        }
        return true;
    }

    protected void updateItemsVersion() {

        List<ItemVersionObject> JobsOpenedInEditor = new ArrayList<ItemVersionObject>();
        List<ItemVersionObject> closedJobs = new ArrayList<ItemVersionObject>();

        boolean hasJobOpenedInEditor = false;
        StringBuilder builder = new StringBuilder();
        modifiedJobs.clear();
        for (ItemVersionObject object : checkedObjects) {
            modifiedJobs.add(object.getItem());
            if (RepositoryManager.isOpenedItemInEditor(object.getRepositoryNode().getObject())) {
                hasJobOpenedInEditor = true;
                JobsOpenedInEditor.add(object);
                builder.append(object.getRepositoryNode().getObject().getLabel() + ", "); //$NON-NLS-1$
            } else {
                closedJobs.add(object);
            }
        }
        if (builder.length() > 0) {
            builder.delete(builder.length() - 2, builder.length());
        }
        if (hasJobOpenedInEditor) {
            MessageDialog.openWarning(Display.getCurrent().getActiveShell(),
                    Messages.getString("VersionManagementDialog.WarningTitle2"), //$NON-NLS-1$
                    Messages.getString("VersionManagementDialog.openedInEditorMessage", builder.toString())); //$NON-NLS-1$
            IWorkbenchWindow workBench = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
            if (workBench != null) {
                IWorkbenchPage page = workBench.getActivePage();
                IEditorReference[] editorRefs = page.getEditorReferences();
                if (GlobalServiceRegister.getDefault().isServiceRegistered(IDesignerCoreUIService.class)) {
                    IDesignerCoreUIService service = (IDesignerCoreUIService) GlobalServiceRegister.getDefault()
                            .getService(IDesignerCoreUIService.class);
                    if (service != null) {
                        for (IEditorReference editorRef : editorRefs) {
                            Object object = null;
                            String id = null;

                            String defaultVersion = null;
                            IEditorPart editor = editorRef.getEditor(false);
                            if (editor instanceof IMultiPageTalendEditor) {
                                IProcess2 process = ((IMultiPageTalendEditor) editor).getProcess();
                                object = process;
                                id = process.getId();
                                defaultVersion = process.getProperty().getVersion();
                            } else if (esbService != null && esbService.isWSDLEditor(editor)) {
                                Item serviceItem = esbService.getWSDLEditorItem(editor);
                                object = serviceItem.getProperty();
                                id = serviceItem.getProperty().getId();
                                defaultVersion = serviceItem.getProperty().getVersion();
                            }
                            String newVersion = null;
                            boolean useSnapshotOld = false;
                            Boolean useSnapshotNew = null;
                            for (ItemVersionObject itemVersionObject : JobsOpenedInEditor) {
                                if (itemVersionObject.getItem().getProperty().getId().equals(id)) {
                                    newVersion = itemVersionObject.getNewVersion();
                                    useSnapshotOld = itemVersionObject.isUseSnapshotOld();
                                    useSnapshotNew = itemVersionObject.isUseSnapshotNew();
                                    break;
                                }
                            }
                            if (newVersion != null) {
                                defaultVersion = MavenVersionUtils.getDefaultVersion(defaultVersion);
                                if (newVersion.equals(defaultVersion)) {
                                    // if default, set null to remove key from property.
                                    newVersion = null;
                                }
                                Command command1 = service.createMavenDeploymentValueChangeCommand(object,
                                        MavenConstants.NAME_USER_VERSION, newVersion);

                                Command command2 = null;
                                if (useSnapshotOld != useSnapshotNew) {
                                    String useSnapshotStr = null;
                                    if (!useSnapshotOld && useSnapshotNew) {
                                        useSnapshotStr = Boolean.toString(Boolean.TRUE);
                                    }
                                    command2 = service.createMavenDeploymentValueChangeCommand(object,
                                            MavenConstants.NAME_PUBLISH_AS_SNAPSHOT, useSnapshotStr);
                                }
                                if (object instanceof IGEFProcess) {
                                    service.executeCommand((IGEFProcess) object, command1);
                                    if (command2 != null) {
                                        service.executeCommand((IGEFProcess) object, command2);
                                    }
                                } else if (object instanceof Property) {
                                    esbService.executeCommand(editor, command1);
                                    if (command2 != null) {
                                        esbService.executeCommand(editor, command2);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        final IWorkspaceRunnable runnable = new IWorkspaceRunnable() {

            @Override
            public void run(final IProgressMonitor monitor) throws CoreException {
                RepositoryWorkUnit<Object> rwu = new RepositoryWorkUnit<Object>(project, "Update items Maven version") { //$NON-NLS-1$

                    @Override
                    protected void run() throws LoginException, PersistenceException {
                        monitor.beginTask("Update items Maven version", closedJobs.size()); //$NON-NLS-1$
                        for (ItemVersionObject object : closedJobs) {
                            final Item item = object.getItem();
                            Property itemProperty = item.getProperty();
                            MavenVersionUtils.setItemMavenVersion(itemProperty, object.getNewVersion());

                            if (object.isUseSnapshotNew() != object.isUseSnapshotOld()) {
                                String useSnapshotStr = null;
                                if (!object.isUseSnapshotOld() && object.isUseSnapshotNew()) {
                                    useSnapshotStr = Boolean.toString(Boolean.TRUE);
                                }
                                MavenVersionUtils.setItemUseSnapshot(itemProperty, useSnapshotStr);
                            }
                            monitor.subTask(itemProperty.getLabel());
                            FACTORY.save(project, itemProperty);
                            monitor.worked(1);
                        }
                        try {
                            FACTORY.saveProject(project);
                        } catch (PersistenceException e) {
                            ExceptionHandler.process(e);
                        }
                    }
                };
                rwu.setAvoidUnloadResources(true);
                rwu.executeRun();
                monitor.done();
            }
        };

        IRunnableWithProgress iRunnableWithProgress = new IRunnableWithProgress() {

            @Override
            public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
                IWorkspace workspace = ResourcesPlugin.getWorkspace();
                try {
                    ISchedulingRule schedulingRule = workspace.getRoot();
                    // the update the project files need to be done in the workspace runnable to avoid all notification
                    // of changes before the end of the modifications.
                    workspace.run(runnable, schedulingRule, IWorkspace.AVOID_UPDATE, monitor);
                } catch (CoreException e) {
                    ExceptionHandler.process(e);
                }

            }
        };

        final ProgressMonitorDialog dialog = new ProgressMonitorDialog(null);
        try {
            dialog.run(false, false, iRunnableWithProgress);
        } catch (InvocationTargetException | InterruptedException e) {
            ExceptionHandler.process(e);
        }
    }

    @Override
    protected ItemVersionObject createItemVersionObject(RepositoryNode node, Property property) {
        String version = MavenVersionUtils.getItemMavenVersion(property);
        boolean useSnapshot = MavenVersionUtils.isItemUseSnapshot(property);
        return new ItemVersionObject(property, node, version, useSnapshot);
    }

    @Override
    protected void checkButtonsState() {
        // do nothing.
    }

    @Override
    protected void checkButtonsState(boolean isEnable) {
        super.checkButtonsState(isEnable);
        if (itemTable.getSelectionCount() == 0) {
            if (eachVersionButton.getSelection()) {
                newVersionText.setEnabled(false);
                useSnapshot.setEnabled(false);
                applyDetail.setEnabled(false);
            }
            newVersionText.setText(""); //$NON-NLS-1$
        }
    }

    private List<ItemVersionObject> getSelectedTableItem() {
        List<ItemVersionObject> selectedObjects = new ArrayList<>();
        if (itemTable.getSelectionCount() > 0) {
            for (TableItem item : itemTable.getSelection()) {
                selectedObjects.add((ItemVersionObject) item.getData());
            }
        }
        return selectedObjects;
    }

    class RemoveAction extends Action {

        public RemoveAction() {
            this.setText(Messages.getString("VersionManagementDialog.remove")); //$NON-NLS-1$
        }

        @Override
        public void run() {
            if (itemTable.getSelection().length > 0) {
                for (TableItem tableItem : itemTable.getSelection()) {
                    checkedObjects.remove((ItemVersionObject) tableItem.getData());
                    removeTableItem(tableItem);
                }
            }
            refreshCheckedTreeView();
            checkButtonsState(false);
        }
    }

}
