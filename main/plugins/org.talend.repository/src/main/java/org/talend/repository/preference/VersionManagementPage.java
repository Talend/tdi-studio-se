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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
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
import org.eclipse.ui.internal.navigator.NavigatorDecoratingLabelProvider;
import org.talend.commons.exception.ExceptionHandler;
import org.talend.commons.exception.LoginException;
import org.talend.commons.exception.PersistenceException;
import org.talend.commons.runtime.model.repository.ERepositoryStatus;
import org.talend.commons.ui.runtime.image.EImage;
import org.talend.commons.ui.runtime.image.ImageProvider;
import org.talend.commons.utils.VersionUtils;
import org.talend.core.CorePlugin;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.model.properties.Item;
import org.talend.core.model.properties.ItemState;
import org.talend.core.model.properties.Property;
import org.talend.core.model.relationship.Relation;
import org.talend.core.model.relationship.RelationshipItemBuilder;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.model.repository.IRepositoryViewObject;
import org.talend.core.model.repository.RepositoryManager;
import org.talend.core.model.routines.RoutinesUtil;
import org.talend.core.ui.branding.IBrandingService;
import org.talend.core.ui.images.CoreImageProvider;
import org.talend.repository.RepositoryWorkUnit;
import org.talend.repository.i18n.Messages;
import org.talend.repository.model.IProxyRepositoryFactory;
import org.talend.repository.model.IRepositoryNode.ENodeType;
import org.talend.repository.model.ItemVersionObject;
import org.talend.repository.model.RepositoryConstants;
import org.talend.repository.model.RepositoryNode;

/**
 * DOC aimingchen class global comment. Detailled comment
 */
public class VersionManagementPage extends AbstractVersionManagementProjectSettingPage {

    private boolean allowVerchange = true;

    private Button majorBtn;

    private Button minorBtn;

    private Button revertBtn;

    private Button versionLatest;

    public VersionManagementPage() {
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
            if (status == ERepositoryStatus.LOCK_BY_OTHER
                    || (status == ERepositoryStatus.LOCK_BY_USER && RepositoryManager.isOpenedItemInEditor(node.getObject()))) {
                return false;
            }
            // table
            if (node.getObject() instanceof org.talend.core.model.metadata.MetadataTable) {
                return false;
            }
            // opened items
            // TODO

            ERepositoryObjectType objectType = node.getObjectType();
            if (objectType == ERepositoryObjectType.SQLPATTERNS || objectType == ERepositoryObjectType.ROUTINES) {
                RepositoryNode systemNode = node.getParent();
                if (systemNode != null) {
                    // for system folder
                    if (systemNode.getType() == ENodeType.STABLE_SYSTEM_FOLDER
                            && systemNode.getLabel().equalsIgnoreCase(RepositoryConstants.SYSTEM_DIRECTORY)) {
                        return false;
                    }
                }
            }

            if (node.getObject().isDeleted()) {
                return false;
            }
        }
        ERepositoryObjectType contentType = node.getContentType();
        if (contentType != null) {
            if (contentType == ERepositoryObjectType.REFERENCED_PROJECTS || contentType == ERepositoryObjectType.GENERATED) {
                return false;
            } else if (contentType == ERepositoryObjectType.SQLPATTERNS || contentType == ERepositoryObjectType.ROUTINES) {
                // for system folder
                if (node.getType() == ENodeType.STABLE_SYSTEM_FOLDER
                        && node.getLabel().equalsIgnoreCase(RepositoryConstants.SYSTEM_DIRECTORY)) {
                    return false;
                }
            }
            // filter tdm items
            else if (contentType == ERepositoryObjectType.valueOf(ERepositoryObjectType.class, "HIERARCHICAL_MAPPER")) {
                return false;
            }
        }

        // for sub folder in db connection
        if (node.getType() == ENodeType.STABLE_SYSTEM_FOLDER) {
            RepositoryNode parentNode = node.getParent();
            if (parentNode != null) {
                if (parentNode.getObjectType() == ERepositoryObjectType.METADATA_CONNECTIONS) {
                    return false;
                }
            }
        }
        return true;
    }

    protected void addItemsOption(Composite parent) {
        Group option = new Group(parent, SWT.NONE);
        option.setLayout(new GridLayout());
        option.setText(Messages.getString("VersionManagementDialog.Options")); //$NON-NLS-1$
        GridDataFactory.swtDefaults().align(SWT.FILL, SWT.CENTER).applyTo(option);
        fixedVersionButton = new Button(option, SWT.RADIO);
        fixedVersionButton.setText(Messages.getString("VersionManagementDialog.FixedVersion")); //$NON-NLS-1$
        fixedVersionButton.setSelection(true); // default

        IBrandingService brandingService = (IBrandingService) GlobalServiceRegister.getDefault()
                .getService(IBrandingService.class);
        allowVerchange = brandingService.getBrandingConfiguration().isAllowChengeVersion();

        Composite versionComposit = new Composite(option, SWT.NONE);
        GridLayout layout = new GridLayout(8, false);
        layout.horizontalSpacing = 1;
        layout.verticalSpacing = 0;
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        versionComposit.setLayout(layout);
        GridDataFactory.swtDefaults().align(SWT.FILL, SWT.BEGINNING).applyTo(versionComposit);

        fixedVersionText = new Text(versionComposit, SWT.BORDER | SWT.READ_ONLY);
        GridData data = new GridData();
        data.widthHint = 50;
        data.minimumWidth = 50;
        fixedVersionText.setLayoutData(data);
        fixedVersionText.setEnabled(false);
        fixedVersionText.setText(VersionUtils.DEFAULT_VERSION);

        majorBtn = new Button(versionComposit, SWT.NONE);
        majorBtn.setText("M"); //$NON-NLS-1$
        majorBtn.setToolTipText(Messages.getString("VersionManagementDialog.MajorVersionTip")); //$NON-NLS-1$
        majorBtn.setEnabled(allowVerchange);
        minorBtn = new Button(versionComposit, SWT.NONE);
        minorBtn.setText("m"); //$NON-NLS-1$
        minorBtn.setToolTipText(Messages.getString("VersionManagementDialog.MinorVersionTip")); //$NON-NLS-1$
        minorBtn.setEnabled(allowVerchange);

        Label label = new Label(versionComposit, SWT.NONE);
        label.setText(""); //$NON-NLS-1$
        data = new GridData();
        data.minimumWidth = 20;
        data.widthHint = 20;
        label.setLayoutData(data);
        label.setVisible(false);

        revertBtn = new Button(versionComposit, SWT.NONE);
        revertBtn.setText(Messages.getString("VersionManagementDialog.Revert")); //$NON-NLS-1$
        revertBtn.setToolTipText(Messages.getString("VersionManagementDialog.RevertTip")); //$NON-NLS-1$
        Label bLabel = new Label(versionComposit, SWT.NONE);
        bLabel.setText(""); //$NON-NLS-1$
        data = new GridData();
        data.minimumWidth = 20;
        data.widthHint = 20;
        bLabel.setLayoutData(data);
        bLabel.setVisible(false);

        alldependcies = new Button(versionComposit, SWT.NONE);
        alldependcies.setText(Messages.getString("VersionManagementDialog.allDependencies"));
        alldependcies.setEnabled(false);

        subjobs = new Button(versionComposit, SWT.NONE);
        subjobs.setText(Messages.getString("VersionManagementDialog.subjob"));
        subjobs.setEnabled(false);

        eachVersionButton = new Button(option, SWT.RADIO);
        eachVersionButton.setText(Messages.getString("VersionManagementDialog.EachVersion")); //$NON-NLS-1$
        eachVersionButton.setEnabled(allowVerchange);

        versionLatest = new Button(option, SWT.CHECK);
        versionLatest.setText(Messages.getString("VersionManagementDialog.FixVersion"));
        versionLatest.setToolTipText(Messages.getString("VersionManagementDialog.FixLastVersion"));
        // event
        fixedVersionButton.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                checkFixedButtons();
                researchMaxVersion();
                refreshTableItems();
            }
        });
        alldependcies.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                selectAllDependencies();
            }
        });

        subjobs.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                selectSubjob();
            }
        });
        versionLatest.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                versionLatest();
            }
        });
        majorBtn.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                String version = fixedVersionText.getText();
                version = VersionUtils.upMajor(version);
                fixedVersionText.setText(version);
                refreshTableItems();
            }
        });
        minorBtn.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                String version = fixedVersionText.getText();
                version = VersionUtils.upMinor(version);
                fixedVersionText.setText(version);
                refreshTableItems();
            }
        });
        revertBtn.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                fixedVersionText.setText(VersionUtils.DEFAULT_VERSION); // set min version
                researchMaxVersion();
                refreshTableItems();
            }
        });
        eachVersionButton.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                checkFixedButtons();
                refreshTableItems();
            }
        });
        checkFixedButtons();
    }

    @Override
    protected void createItemTableColumns() {
        TableColumn itemColumn = new TableColumn(itemTable, SWT.CENTER);
        itemColumn.setText(Messages.getString("VersionManagementDialog.Items")); //$NON-NLS-1$
        itemColumn.setWidth(110);

        TableColumn oldVersionColumn = new TableColumn(itemTable, SWT.CENTER);
        oldVersionColumn.setText(Messages.getString("VersionManagementDialog.Version")); //$NON-NLS-1$
        oldVersionColumn.setWidth(60);

        TableColumn versionColumn = new TableColumn(itemTable, SWT.CENTER);
        versionColumn.setText(Messages.getString("VersionManagementDialog.NewVersion")); //$NON-NLS-1$
        versionColumn.setWidth(82);

        final TableColumn delColumn = new TableColumn(itemTable, SWT.CENTER);
        delColumn.setText(""); //$NON-NLS-1$
        delColumn.setWidth(26);
        delColumn.setResizable(false);

        versionColumn.addControlListener(new ControlAdapter() {

            @Override
            public void controlResized(ControlEvent e) {
                if (!isFixedVersion()) {
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

    private void selectAllDependencies() {
        List<ItemVersionObject> tableList = new ArrayList<ItemVersionObject>();
        tableList.addAll(checkedObjects);
        IProxyRepositoryFactory factory = CorePlugin.getDefault().getProxyRepositoryFactory();
        for (ItemVersionObject object : checkedObjects) {
            if (object.getRepositoryNode() != null) {
                List<Relation> relations = builder.getItemsRelatedTo(object.getRepositoryNode().getId(), object.getOldVersion(),
                        RelationshipItemBuilder.JOB_RELATION);

                for (Relation relation : relations) {
                    IRepositoryViewObject obj = null;
                    try {
                        if (RelationshipItemBuilder.ROUTINE_RELATION.equals(relation.getType())) {
                            obj = RoutinesUtil.getRoutineFromName(relation.getId());
                        } else {
                            obj = factory.getLastVersion(relation.getId());
                        }
                        if (obj != null) {
                            for (ItemVersionObject obj2 : versionObjects) {
                                if (obj2.getItem() == obj.getProperty().getItem()) {
                                    ItemVersionObject relat = obj2;
                                    if (!tableList.contains(relat)) {
                                        tableList.add(relat);
                                        checkAllVerSionLatest(tableList, relat);
                                    }
                                    break;
                                }
                            }
                        }
                    } catch (PersistenceException et) {
                        ExceptionHandler.process(et);
                    }
                }
            }
        }
        removeItemElements(checkedObjects);
        checkedObjects.clear();
        checkedObjects.addAll(tableList);
        refreshTableItems();
        refreshCheckedTreeView();

    }

    private void versionLatest() {
        List<ItemVersionObject> tableList = new ArrayList<ItemVersionObject>();
        tableList.addAll(checkedObjects);

        for (ItemVersionObject object : checkedObjects) {
            if (object.getRepositoryNode() != null) {
                List<Relation> relations = builder.getItemsJobRelatedTo(object.getRepositoryNode().getId(),
                        object.getOldVersion(), RelationshipItemBuilder.JOB_RELATION);
                for (Relation relation : relations) {
                    try {
                        IRepositoryViewObject obj = FACTORY.getLastVersion(relation.getId());
                        if (obj != null) {
                            for (ItemVersionObject obj2 : versionObjects) {
                                if (obj2.getItem() == obj.getProperty().getItem()) {
                                    ItemVersionObject relat = obj2;
                                    if (!tableList.contains(relat)) {
                                        tableList.add(relat);
                                        checkAllVerSionLatest(tableList, relat);
                                    }
                                    break;
                                }
                            }
                        }
                    } catch (PersistenceException et) {
                        ExceptionHandler.process(et);
                    }
                }
            }
        }
        removeItemElements(checkedObjects);
        checkedObjects.clear();
        checkedObjects.addAll(tableList);
        refreshTableItems();
        refreshCheckedTreeView();
    }

    protected void researchMaxVersion() {
        String version = fixedVersionText.getText();
        if ("".equals(version.trim())) { //$NON-NLS-1$
            version = VersionUtils.DEFAULT_VERSION;
        }
        for (ItemVersionObject object : checkedObjects) {
            if (VersionUtils.compareTo(version, object.getOldVersion()) < 0) {
                version = object.getOldVersion();
            }
        }
        fixedVersionText.setText(version);
    }

    private boolean isFixedVersion() {
        return fixedVersionButton.getSelection();
    }

    private void checkFixedButtons() {
        majorBtn.setEnabled(isFixedVersion() && allowVerchange);
        minorBtn.setEnabled(isFixedVersion() && allowVerchange);
        revertBtn.setEnabled(isFixedVersion());
        // versionLatest.setEnabled(isFixedVersion());
    }

    @Override
    protected void addItemElements(List<ItemVersionObject> elements) {
        if (elements == null || elements.isEmpty()) {
            return;
        }
        itemTable.setRedraw(false);
        final Color redColor = Display.getDefault().getSystemColor(SWT.COLOR_RED);

        for (final ItemVersionObject object : elements) {
            final TableItem tableItem = new TableItem(itemTable, SWT.NONE);
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
            tableItem.setText(1, object.getOldVersion());

            TableEditor versionEditor = null;

            if (isFixedVersion()) {
                String version = fixedVersionText.getText();
                tableItem.setText(2, version);
                if (VersionUtils.compareTo(version, object.getOldVersion()) > 0) {
                    tableItem.setForeground(2, redColor);
                } else {
                    tableItem.setForeground(2, Display.getDefault().getSystemColor(SWT.COLOR_BLACK));
                }
            } else {
                // new version
                versionEditor = new TableEditor(itemTable);
                Composite versionComposit = new Composite(itemTable, SWT.NONE);
                GridLayout layout = new GridLayout(3, false);
                layout.horizontalSpacing = 1;
                layout.verticalSpacing = 0;
                layout.marginHeight = 0;
                layout.marginWidth = 0;
                versionComposit.setLayout(layout);

                final Text text = new Text(versionComposit, SWT.BORDER | SWT.READ_ONLY);
                GridData data = new GridData(GridData.FILL_HORIZONTAL);
                text.setLayoutData(data);
                text.setEditable(false);
                text.setText(object.getNewVersion());
                if (VersionUtils.compareTo(object.getNewVersion(), object.getOldVersion()) > 0) {
                    text.setForeground(redColor);
                } else {
                    text.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_BLUE));
                }

                Button tableMajorBtn = new Button(versionComposit, SWT.NONE);
                tableMajorBtn.setText("M"); //$NON-NLS-1$
                tableMajorBtn.setToolTipText(Messages.getString("VersionManagementDialog.MajorVersionTip")); //$NON-NLS-1$
                Button tableMinorBtn = new Button(versionComposit, SWT.NONE);
                tableMinorBtn.setText("m"); //$NON-NLS-1$
                tableMinorBtn.setToolTipText(Messages.getString("VersionManagementDialog.MinorVersionTip")); //$NON-NLS-1$

                tableMajorBtn.addSelectionListener(new SelectionAdapter() {

                    @Override
                    public void widgetSelected(SelectionEvent e) {
                        String version = object.getNewVersion();
                        version = VersionUtils.upMajor(version);
                        text.setText(version);
                        text.setForeground(redColor);
                        object.setNewVersion(version);
                    }
                });
                tableMinorBtn.addSelectionListener(new SelectionAdapter() {

                    @Override
                    public void widgetSelected(SelectionEvent e) {
                        String version = object.getNewVersion();
                        version = VersionUtils.upMinor(version);
                        text.setText(version);
                        text.setForeground(redColor);
                        object.setNewVersion(version);
                    }
                });

                versionEditor.minimumWidth = itemTable.getColumn(2).getWidth();
                versionEditor.setEditor(versionComposit, tableItem, 2);
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
            if (isFixedVersion()) {
                tableItem.setData(ITEM_EDITOR_KEY, new TableEditor[] { delEditor });
            } else if (versionEditor != null) {
                tableItem.setData(ITEM_EDITOR_KEY, new TableEditor[] { versionEditor, delEditor });
            }
            itemTable.setRedraw(true);
        }
    }

    @Override
    protected String getNewVersionWithOption(ItemVersionObject object) {
        if (!isFixedVersion()) {
            return object.getNewVersion();
        }
        return fixedVersionText.getText();
    }

    @Override
    protected void updateItemsVersion() {
        final IWorkspaceRunnable runnable = new IWorkspaceRunnable() {

            @Override
            public void run(final IProgressMonitor monitor) throws CoreException {
                RepositoryWorkUnit<Object> rwu = new RepositoryWorkUnit<Object>(project, "Update items version") {

                    @Override
                    protected void run() throws LoginException, PersistenceException {
                        monitor.beginTask("Update items version", checkedObjects.size()); //$NON-NLS-1$
                        Map<String, String> versions = new HashMap<String, String>();
                        for (int i = 0; i < checkedObjects.size(); i++) {
                            ItemVersionObject object = checkedObjects.get(i);
                            versions.put(object.getItem().getProperty().getId(), object.getOldVersion());
                        }
                        for (ItemVersionObject object : checkedObjects) {
                            IRepositoryViewObject repositoryObject = object.getRepositoryNode().getObject();
                            if (repositoryObject != null && repositoryObject.getProperty() != null) {
                                if (!object.getNewVersion().equals(repositoryObject.getVersion())) {
                                    final Item item = object.getItem();
                                    Property itemProperty = item.getProperty();
                                    itemProperty.setVersion(object.getNewVersion());
                                    monitor.subTask(itemProperty.getLabel());

                                    try {
                                        // for bug 12853 ,version management doesn't work for joblet because eResource
                                        // is null
                                        IRepositoryViewObject obj = null;
                                        if (itemProperty.eResource() == null) {
                                            ItemState state = item.getState();
                                            if (state != null && state.getPath() != null) {
                                                obj = FACTORY.getLastVersion(project, itemProperty.getId(), state.getPath(),
                                                        object.getRepositoryNode().getObjectType());
                                            } else {
                                                obj = FACTORY.getLastVersion(project, itemProperty.getId());
                                            }
                                        }
                                        if (obj != null) {
                                            // obj.setVersion(object.getNewVersion());
                                            FACTORY.save(project, obj.getProperty());
                                            builder.addOrUpdateItem(obj.getProperty().getItem(), true);
                                        } else {
                                            String id = itemProperty.getId();
                                            FACTORY.save(project, itemProperty);
                                            if (versionLatest.getSelection()) {
                                                builder.updateItemVersion(item, object.getOldVersion(), id, versions, true);
                                            }
                                            builder.addOrUpdateItem(item, true);
                                        }
                                    } catch (PersistenceException e) {
                                        ExceptionHandler.process(e);
                                    }
                                }
                            }
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
        // final ProgressMonitorJobsDialog dialog = new ProgressMonitorJobsDialog(null);
        final ProgressMonitorDialog dialog = new ProgressMonitorDialog(null);
        try {
            dialog.run(false, false, iRunnableWithProgress);
        } catch (InvocationTargetException e) {
            ExceptionHandler.process(e);
        } catch (InterruptedException e) {
            ExceptionHandler.process(e);
        }
    }

    @Override
    protected ItemVersionObject createItemVersionObject(RepositoryNode node, Property property) {
        ItemVersionObject object = new ItemVersionObject(property, node, property.getVersion());
        return object;
    }

}
