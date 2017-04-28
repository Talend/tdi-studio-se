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

import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
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
import org.talend.core.model.process.IProcess2;
import org.talend.core.model.properties.Item;
import org.talend.core.model.properties.Property;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.model.repository.Folder;
import org.talend.core.model.repository.RepositoryManager;
import org.talend.core.runtime.maven.MavenConstants;
import org.talend.core.ui.images.CoreImageProvider;
import org.talend.core.ui.process.IGEFProcess;
import org.talend.core.ui.services.IDesignerCoreUIService;
import org.talend.designer.core.IMultiPageTalendEditor;
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

    private Button applyVersionButton;

    private Button eachVersionButton;

    private Button useJobVersionButton;

    private String appliedFixedVersion = MavenVersionUtils.DEFAULT_MAVEN_VERSION;

    public MavenVersionManagementProjectSettingPage() {
        super();
        this.noDefaultAndApplyButton();
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
                || type == ERepositoryObjectType.valueOf(NODENAME_ROUTE_DESIGNS) || type == ERepositoryObjectType.PROCESS_ROUTE
                || type == ERepositoryObjectType.PROCESS_SPARK || type == ERepositoryObjectType.PROCESS_SPARKSTREAMING
                || type == ERepositoryObjectType.PROCESS_STORM) {
            return true;
        }
        return false;
    }

    @Override
    protected void addItemsOption(Composite parent) {
        Group option = new Group(parent, SWT.NONE);
        option.setLayout(new GridLayout());
        option.setText(Messages.getString("VersionManagementDialog.Options")); //$NON-NLS-1$
        GridDataFactory.swtDefaults().align(SWT.FILL, SWT.CENTER).applyTo(option);
        fixedVersionButton = new Button(option, SWT.RADIO);
        fixedVersionButton.setText(Messages.getString("VersionManagementDialog.FixedVersion")); //$NON-NLS-1$
        fixedVersionButton.setSelection(true); // default

        Composite versionComposit = new Composite(option, SWT.NONE);
        GridLayout layout = new GridLayout(8, false);
        layout.horizontalSpacing = 1;
        layout.verticalSpacing = 0;
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        versionComposit.setLayout(layout);
        GridDataFactory.swtDefaults().align(SWT.FILL, SWT.BEGINNING).applyTo(versionComposit);

        fixedVersionText = new Text(versionComposit, SWT.BORDER);
        GridData data = new GridData();
        data.widthHint = 120;
        data.minimumWidth = 80;
        data.horizontalIndent = 20;
        fixedVersionText.setLayoutData(data);
        fixedVersionText.setText(MavenVersionUtils.DEFAULT_MAVEN_VERSION);

        applyVersionButton = new Button(versionComposit, SWT.NONE);
        applyVersionButton.setText(Messages.getString("VersionManagementDialog.applyVersion")); //$NON-NLS-1$

        subjobs = new Button(versionComposit, SWT.NONE);
        subjobs.setText(Messages.getString("VersionManagementDialog.subjob")); //$NON-NLS-1$
        subjobs.setEnabled(false);

        eachVersionButton = new Button(option, SWT.RADIO);
        eachVersionButton.setText(Messages.getString("VersionManagementDialog.EachVersion")); //$NON-NLS-1$

        useJobVersionButton = new Button(option, SWT.RADIO);
        useJobVersionButton.setText(Messages.getString("VersionManagementDialog.useJobVersion")); //$NON-NLS-1$
        // event
        fixedVersionButton.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                fixedVersionText.setEnabled(fixedVersionButton.getSelection());
                applyVersionButton.setEnabled(fixedVersionButton.getSelection());
                refreshTableItems();
            }
        });

        fixedVersionText.addModifyListener(new ModifyListener() {

            @Override
            public void modifyText(ModifyEvent e) {
                String version = fixedVersionText.getText();
                if (MavenVersionUtils.isValidMavenVersion(version)) {
                    applyVersionButton.setEnabled(true);
                    fixedVersionText.setBackground(COLOR_WHITE);
                    fixedVersionText.setToolTipText(""); //$NON-NLS-1$
                } else {
                    applyVersionButton.setEnabled(false);
                    fixedVersionText.setBackground(COLOR_RED);
                    fixedVersionText.setToolTipText(Messages.getString("VersionManagementDialog.valueWarning")); //$NON-NLS-1$
                }

            }
        });

        applyVersionButton.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                String version = fixedVersionText.getText();
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

            if (fixedVersionButton.getSelection()) {
                tableItem.setText(2, appliedFixedVersion);
            } else if (useJobVersionButton.getSelection()) {
                String jobDefaultVersion = MavenVersionUtils.getDefaultVersion(item.getProperty().getVersion());
                tableItem.setText(2, jobDefaultVersion);
                object.setNewVersion(jobDefaultVersion);
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

                final Text text = new Text(versionComposite, SWT.CENTER | SWT.BORDER);

                GridData data = new GridData(GridData.FILL_BOTH);
                text.setLayoutData(data);
                String newVersion = object.getNewVersion();
                if (newVersion == null || "".equals(newVersion.trim())) { //$NON-NLS-1$
                    newVersion = appliedFixedVersion;
                    object.setNewVersion(newVersion);
                }
                text.setText(newVersion);
                checkVersionPattern(text, newVersion);

                text.addModifyListener(new ModifyListener() {

                    @Override
                    public void modifyText(ModifyEvent e) {
                        String version = text.getText();
                        checkVersionPattern(text, version);
                        object.setNewVersion(version);
                        checkValid();
                    }

                });

                versionEditor.minimumWidth = itemTable.getColumn(2).getWidth();
                versionEditor.setEditor(versionComposite, tableItem, 2);
            }
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
            delEditor.setEditor(delLabel, tableItem, 3);
            if (fixedVersionButton.getSelection() || useJobVersionButton.getSelection()) {
                tableItem.setData(ITEM_EDITOR_KEY, new TableEditor[] { delEditor });
            } else if (versionEditor != null) {
                tableItem.setData(ITEM_EDITOR_KEY, new TableEditor[] { versionEditor, delEditor });
            }
            itemTable.setRedraw(true);
        }
    }

    private void checkValid() {
        for (ItemVersionObject object : checkedObjects) {
            if (!MavenVersionUtils.isValidMavenVersion(object.getNewVersion())) {
                setValid(false);
                return;
            }
        }
        setValid(true);
    }

    private void checkVersionPattern(Text text, String version) {
        if (MavenVersionUtils.isValidMavenVersion(version)) {
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
        } else if (eachVersionButton.getSelection()) {
            newVersion = object.getNewVersion();
        } else {
            newVersion = MavenVersionUtils.getDefaultVersion(object.getItem().getProperty().getVersion());
        }
        return newVersion;
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
                            IEditorPart editor = editorRef.getEditor(false);
                            if (editor instanceof IMultiPageTalendEditor) {
                                IProcess2 process = ((IMultiPageTalendEditor) editor).getProcess();
                                String version = null;
                                for (ItemVersionObject object : JobsOpenedInEditor) {
                                    if (object.getItem().getProperty().getId().equals(process.getId())) {
                                        version = object.getNewVersion();
                                        break;
                                    }
                                }
                                if (version != null) {
                                    String jobDefaultVersion = MavenVersionUtils.getDefaultVersion(process.getProperty().getVersion());
                                    if (version.equals(jobDefaultVersion)) {
                                        // if default, set null to remove key from property.
                                        version = null;
                                    }
                                    Command command = service.crateMavenDeploymentValueChangeCommand(process,
                                            MavenConstants.NAME_USER_VERSION, version);
                                    if (process instanceof IGEFProcess) {
                                        service.executeCommand((IGEFProcess) process, command);
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
        ItemVersionObject object = new ItemVersionObject(property, node, version);
        return object;
    }

}
