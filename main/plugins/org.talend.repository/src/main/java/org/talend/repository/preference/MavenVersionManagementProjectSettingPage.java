// ============================================================================
//
// Copyright (C) 2006-2016 Talend Inc. - www.talend.com
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
import java.util.Map;

import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.emf.common.util.EMap;
import org.eclipse.gef.commands.Command;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
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
import org.talend.commons.ui.runtime.image.EImage;
import org.talend.commons.ui.runtime.image.ImageProvider;
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

    private Button eachVersionButton;

    private String appliedFixedVersion;

    private Text projectVersionText;

    private Button globalSnapshotCheckbox;
    
    private ProjectPreferenceManager projectPreferenceManager;
    
    private IESBService esbService;

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
                || type == ERepositoryObjectType.valueOf(NODENAME_ROUTE_DESIGNS)
                || type == ERepositoryObjectType.valueOf(NODENAME_SERVICES) || type == ERepositoryObjectType.PROCESS_ROUTE
                || type == ERepositoryObjectType.PROCESS_SPARK || type == ERepositoryObjectType.PROCESS_SPARKSTREAMING
                || type == ERepositoryObjectType.PROCESS_STORM) {
            return true;
        }
        return false;
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
        projectVersionText.setText(projectPreferenceManager.getValue(MavenConstants.PROJECT_VERSION));

        globalSnapshotCheckbox = new Button(projectVersionComposite, SWT.CHECK);
        globalSnapshotCheckbox.setText(Messages.getString("VersionManagementDialog.useSnapshot")); //$NON-NLS-1$
        GridData globalSnapshotCheckboxData = new GridData();
        globalSnapshotCheckboxData.horizontalIndent = 10;
        globalSnapshotCheckbox.setLayoutData(globalSnapshotCheckboxData);
        globalSnapshotCheckbox.setSelection(projectPreferenceManager.getBoolean(MavenConstants.NAME_PUBLISH_AS_SNAPSHOT));

        fixedVersionButton = new Button(option, SWT.RADIO);
        fixedVersionButton.setText(Messages.getString("VersionManagementDialog.useProjectVersion")); //$NON-NLS-1$
        fixedVersionButton.setSelection(true); // default

        applyVersionButton = new Button(option, SWT.NONE);
        applyVersionButton.setText(Messages.getString("VersionManagementDialog.applyVersion")); //$NON-NLS-1$

        subjobs = new Button(option, SWT.NONE);
        subjobs.setText(Messages.getString("VersionManagementDialog.subjob")); //$NON-NLS-1$
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
                applyVersionButton.setEnabled(fixedVersionButton.getSelection());
                refreshTableItems();
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
                if (version != null && !"".equals(version.trim())) { //$NON-NLS-1$
                    appliedFixedVersion = version;
                    refreshTableItems();
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
                refreshTableItems();
            }
        });

        useJobVersionButton.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                refreshTableItems();
            }
        });

    }

    @Override
    protected void createItemTableColumns() {
        TableColumn itemColumn = new TableColumn(itemTable, SWT.CENTER);
        itemColumn.setText(Messages.getString("VersionManagementDialog.Items")); //$NON-NLS-1$
        itemColumn.setWidth(100);

        TableColumn oldVersionColumn = new TableColumn(itemTable, SWT.CENTER);
        oldVersionColumn.setText(Messages.getString("VersionManagementDialog.Version")); //$NON-NLS-1$
        oldVersionColumn.setWidth(100);

        TableColumn versionColumn = new TableColumn(itemTable, SWT.CENTER);
        versionColumn.setText(Messages.getString("VersionManagementDialog.NewVersion")); //$NON-NLS-1$
        versionColumn.setWidth(100);

        TableColumn snapshotColumn = new TableColumn(itemTable, SWT.CENTER);
        snapshotColumn.setText(Messages.getString("VersionManagementDialog.snapshot")); //$NON-NLS-1$
        snapshotColumn.setWidth(80);

        final TableColumn delColumn = new TableColumn(itemTable, SWT.CENTER);
        delColumn.setText(""); //$NON-NLS-1$
        delColumn.setWidth(26);
        delColumn.setResizable(false);

        versionColumn.addControlListener(new ControlAdapter() {

            @Override
            public void controlResized(ControlEvent e) {
                if (eachVersionButton.getSelection()) {
                    refreshTableItems();
                }
            }

        });
        itemTable.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                checkButtonsState();
            }
        });

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
            // old version
            tableItem.setText(1, MavenVersionUtils.getItemMavenVersion(item.getProperty()));

            TableEditor versionEditor = null;
            final Text text;
            if (fixedVersionButton.getSelection()) {
                tableItem.setText(2, appliedFixedVersion);
                text = null;
            } else if (useJobVersionButton.getSelection()) {
                String jobDefaultVersion = MavenVersionUtils.getDefaultVersion(item.getProperty().getVersion());
                tableItem.setText(2, jobDefaultVersion);
                text = null;
            } else {
                // new version
                versionEditor = new TableEditor(itemTable);

                Composite versionComposite = new Composite(itemTable, SWT.NONE);
                GridLayout layout = new GridLayout(1, false);
                layout.marginHeight = 0;
                layout.marginWidth = 0;
                layout.marginLeft = 0;
                layout.marginRight = 1;
                versionComposite.setLayout(layout);

                text = new Text(versionComposite, SWT.CENTER | SWT.BORDER);

                GridData data = new GridData(GridData.FILL_BOTH);
                text.setLayoutData(data);
                String newVersion = object.getNewVersion();
                if (newVersion == null || "".equals(newVersion.trim())) { //$NON-NLS-1$
                    newVersion = appliedFixedVersion;
                } else if (newVersion.endsWith(MavenConstants.SNAPSHOT)) {
                    newVersion = newVersion.replace(MavenConstants.SNAPSHOT, ""); //$NON-NLS-1$
                }
                if (newVersion.equals(object.getOldVersion().replace(MavenConstants.SNAPSHOT, ""))) { //$NON-NLS-1$
                    newVersion = MavenVersionUtils.increaseVersion(newVersion);
                }
                object.setNewVersion(newVersion);
                text.setText(newVersion);
                checkVersionPattern(text, newVersion);

                versionEditor.minimumWidth = itemTable.getColumn(2).getWidth();
                versionEditor.setEditor(versionComposite, tableItem, 2);
            }
            
            TableEditor snapshotEditor = new TableEditor(itemTable);
            final Button snapshotCheckbox = new Button(itemTable, SWT.CHECK);
            snapshotCheckbox.pack();
            if (eachVersionButton.getSelection()) {
                snapshotCheckbox.setSelection(object.getOldVersion().endsWith(MavenConstants.SNAPSHOT));
                snapshotCheckbox.setEnabled(true);
            } else {
                snapshotCheckbox.setSelection(false);
                snapshotCheckbox.setEnabled(false);
            }
            if (text != null) {
                text.addModifyListener(new ModifyListener() {

                    @Override
                    public void modifyText(ModifyEvent e) {
                        String version = text.getText();
                        checkVersionPattern(text, version);
                        if (snapshotCheckbox.getSelection()) {
                            if (!version.endsWith(MavenConstants.SNAPSHOT)) {
                                version += MavenConstants.SNAPSHOT; 
                            }
                        } else {
                            if (version.endsWith(MavenConstants.SNAPSHOT)) {
                                version = version.replace(MavenConstants.SNAPSHOT, ""); //$NON-NLS-1$
                            }
                        }
                        object.setNewVersion(version);
                        checkValid();
                    }
                });
            }
            
            snapshotCheckbox.addSelectionListener(new SelectionAdapter() {

                @Override
                public void widgetSelected(SelectionEvent e) {
                    String version = object.getNewVersion();
                    if (snapshotCheckbox.getSelection()) {
                        if (!version.endsWith(MavenConstants.SNAPSHOT)) {
                            version += MavenConstants.SNAPSHOT;
                        }
                    } else {
                        if (version.endsWith(MavenConstants.SNAPSHOT)) {
                            version = version.replace(MavenConstants.SNAPSHOT, ""); //$NON-NLS-1$
                        }
                    }
                    object.setNewVersion(version);
                }
                
            });
            snapshotEditor.minimumWidth = snapshotCheckbox.getSize().x;
            snapshotEditor.setEditor(snapshotCheckbox, tableItem, 3);
            
            TableEditor delEditor = new TableEditor(itemTable);
            Label delLabel = new Label(itemTable, SWT.CENTER);
            delLabel.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_WHITE));
            delLabel.setImage(ImageProvider.getImage(EImage.DELETE_ICON));
            delLabel.setToolTipText(Messages.getString("VersionManagementDialog.DeletedTip")); //$NON-NLS-1$
            delLabel.pack();
            delLabel.addMouseListener(new MouseAdapter() {

                @Override
                public void mouseDown(MouseEvent e) {
                    checkedObjects.remove(object);
                    removeTableItem(tableItem);
                    refreshCheckedTreeView();
                    checkButtonsState();
                    checkButtonsState(false);
                }

            });

            delEditor.minimumWidth = 25;
            delEditor.horizontalAlignment = SWT.CENTER;
            delEditor.setEditor(delLabel, tableItem, 4);
            if (fixedVersionButton.getSelection() || useJobVersionButton.getSelection()) {
                tableItem.setData(ITEM_EDITOR_KEY, new TableEditor[] { snapshotEditor, delEditor });
            } else if (versionEditor != null) {
                tableItem.setData(ITEM_EDITOR_KEY, new TableEditor[] { versionEditor, snapshotEditor, delEditor });
            }
            itemTable.setRedraw(true);
        }
    }
    
    private boolean useSnapshot() {
        return globalSnapshotCheckbox.getSelection();
    }

    private void checkValid() {
        if(!MavenVersionUtils.isValidMavenVersion(projectVersionText.getText(), useSnapshot())) {
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

    private void checkVersionPattern(Text text, String version) {
        if (MavenVersionUtils.isValidMavenVersion(version, useSnapshot())) {
            text.setBackground(COLOR_WHITE);
            text.setToolTipText(""); //$NON-NLS-1$
        } else {
            text.setBackground(COLOR_RED);
            text.setToolTipText(Messages.getString("VersionManagementDialog.valueWarning")); //$NON-NLS-1$
        }
    }

    protected String getNewVersionWithOption(ItemVersionObject object) {
        String newVersion;
        if (fixedVersionButton.getSelection()) {
            newVersion = appliedFixedVersion;
            if (useSnapshot()) {
                newVersion += MavenConstants.SNAPSHOT;
            }
        } else if (eachVersionButton.getSelection()) {
            newVersion = object.getNewVersion();
        } else {
            newVersion = MavenVersionUtils.getDefaultVersion(object.getItem().getProperty().getVersion());
            if (useSnapshot()) {
                newVersion += MavenConstants.SNAPSHOT;
            }
        }
        return newVersion;
    }

    
    
    @Override
    public boolean performOk() {
        if (super.performOk()) {
            projectPreferenceManager.setValue(MavenConstants.PROJECT_VERSION, projectVersionText.getText());
            projectPreferenceManager.setValue(MavenConstants.NAME_PUBLISH_AS_SNAPSHOT, useSnapshot());
            projectPreferenceManager.save();
            return true;
        }
        return false;
    }

    protected void updateItemsVersion() {

        List<ItemVersionObject> JobsOpenedInEditor = new ArrayList<ItemVersionObject>();
        List<ItemVersionObject> closedJobs = new ArrayList<ItemVersionObject>();
        
        boolean hasJobOpenedInEditor = false;
        StringBuilder builder = new StringBuilder();
        for (ItemVersionObject object : checkedObjects) {
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
                            boolean useSnapshotDefault = false;
                            IEditorPart editor = editorRef.getEditor(false);
                            if (editor instanceof IMultiPageTalendEditor) {
                                IProcess2 process = ((IMultiPageTalendEditor) editor).getProcess();
                                object = process;
                                id = process.getId();
                                defaultVersion = process.getProperty().getVersion();
                                Map<Object, Object> additionalProperties = process.getAdditionalProperties();
                                if (additionalProperties != null && !additionalProperties.isEmpty()) {
                                    if (additionalProperties.containsKey(MavenConstants.NAME_PUBLISH_AS_SNAPSHOT)) {
                                        useSnapshotDefault = true;
                                    }
                                }
                            } else if (esbService != null && esbService.isWSDLEditor(editor)) {
                                Item serviceItem = esbService.getWSDLEditorItem(editor);
                                object = serviceItem.getProperty();
                                id = serviceItem.getProperty().getId();
                                defaultVersion = serviceItem.getProperty().getVersion();
                                EMap additionalProperties = serviceItem.getProperty().getAdditionalProperties();
                                if (additionalProperties != null && !additionalProperties.isEmpty()) {
                                    if (additionalProperties.containsKey(MavenConstants.NAME_PUBLISH_AS_SNAPSHOT)) {
                                        useSnapshotDefault = true;
                                    }
                                }
                            }
                            String version = null;
                            for (ItemVersionObject itemVersionObject : JobsOpenedInEditor) {
                                if (itemVersionObject.getItem().getProperty().getId().equals(id)) {
                                    version = itemVersionObject.getNewVersion();
                                    break;
                                }
                            }
                            if (version != null) {
                                defaultVersion = MavenVersionUtils
                                        .getDefaultVersion(defaultVersion);
                                if (useSnapshotDefault) {
                                    defaultVersion += MavenConstants.SNAPSHOT;
                                }
                                Boolean useSnapshotNew = null;
                                if (version.equals(defaultVersion)) {
                                    // if default, set null to remove key from property.
                                    version = null;
                                } else {
                                    if (version.endsWith(MavenConstants.SNAPSHOT)) {
                                        version = version.replace(MavenConstants.SNAPSHOT, ""); //$NON-NLS-1$
                                        useSnapshotNew = true;
                                    }
                                }
                                Command command1 = service.createMavenDeploymentValueChangeCommand(object,
                                        MavenConstants.NAME_USER_VERSION, version);
                                Command command2 = service.createMavenDeploymentValueChangeCommand(object,
                                        MavenConstants.NAME_PUBLISH_AS_SNAPSHOT,
                                        useSnapshotNew == null ? null : Boolean.toString(useSnapshotNew));
                                if (object instanceof IGEFProcess) {
                                    service.executeCommand((IGEFProcess) object, command1);
                                    service.executeCommand((IGEFProcess) object, command2);
                                } else if (object instanceof Property) {
                                    esbService.executeCommand(editor, command1);
                                    esbService.executeCommand(editor, command2);
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
                            String newVersion = object.getNewVersion();
                            String useSnapshot = null;
                            if (newVersion.endsWith(MavenConstants.SNAPSHOT)) {
                                useSnapshot = Boolean.toString(Boolean.TRUE);
                                newVersion = newVersion.replace(MavenConstants.SNAPSHOT, ""); //$NON-NLS-1$
                            }
                            MavenVersionUtils.setItemMavenVersion(itemProperty, newVersion);
                            MavenVersionUtils.setItemUseSnapshot(itemProperty, useSnapshot);
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
        if (version != null && MavenVersionUtils.isItemUseSnapshot(property)) {
            version += MavenConstants.SNAPSHOT;
        }
        ItemVersionObject object = new ItemVersionObject(property, node, version);
        return object;
    }

}
