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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.ITreeViewerListener;
import org.eclipse.jface.viewers.TreeExpansionEvent;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.internal.navigator.NavigatorContentServiceContentProvider;
import org.eclipse.ui.internal.navigator.NavigatorDecoratingLabelProvider;
import org.talend.commons.exception.LoginException;
import org.talend.commons.exception.PersistenceException;
import org.talend.commons.runtime.model.repository.ERepositoryStatus;
import org.talend.commons.ui.runtime.exception.ExceptionHandler;
import org.talend.commons.ui.runtime.image.EImage;
import org.talend.commons.ui.runtime.image.IImage;
import org.talend.commons.ui.runtime.image.ImageProvider;
import org.talend.commons.ui.swt.advanced.composite.ThreeCompositesSashForm;
import org.talend.commons.utils.VersionUtils;
import org.talend.core.CorePlugin;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.model.general.Project;
import org.talend.core.model.properties.Item;
import org.talend.core.model.properties.ItemState;
import org.talend.core.model.properties.Property;
import org.talend.core.model.relationship.Relation;
import org.talend.core.model.relationship.RelationshipItemBuilder;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.model.repository.IRepositoryViewObject;
import org.talend.core.model.repository.RepositoryManager;
import org.talend.core.model.routines.RoutinesUtil;
import org.talend.core.repository.model.ProjectRepositoryNode;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.core.ui.branding.IBrandingService;
import org.talend.core.ui.images.CoreImageProvider;
import org.talend.repository.ProjectManager;
import org.talend.repository.RepositoryWorkUnit;
import org.talend.repository.i18n.Messages;
import org.talend.repository.model.IProxyRepositoryFactory;
import org.talend.repository.model.IRepositoryNode;
import org.talend.repository.model.IRepositoryNode.ENodeType;
import org.talend.repository.model.ItemVersionObject;
import org.talend.repository.model.RepositoryConstants;
import org.talend.repository.model.RepositoryNode;
import org.talend.repository.model.nodes.IProjectRepositoryNode;
import org.talend.repository.ui.dialog.ItemsVersionConfirmDialog;
import org.talend.repository.viewer.ui.provider.RepoCommonViewerProvider;
import org.talend.repository.viewer.ui.viewer.CheckboxRepositoryTreeViewer;

/**
 * DOC aimingchen class global comment. Detailled comment
 */
public class VersionManagementPage extends ProjectSettingPage {

    private boolean isApplied;

    private boolean allowVerchange = true;

    public VersionManagementPage() {
        super();
        this.noDefaultAndApplyButton();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.preference.PreferencePage#createContents(org.eclipse.swt.widgets.Composite)
     */
    @Override
    protected Control createContents(Composite parent) {
        Composite composite = new Composite(parent, 0);
        ThreeCompositesSashForm compositesSachForm = new ThreeCompositesSashForm(composite, SWT.NONE);
        GridLayout gridLayout = new GridLayout(1, false);
        gridLayout.marginHeight = 0;
        gridLayout.marginWidth = 0;
        gridLayout.horizontalSpacing = 0;
        composite.setLayout(gridLayout);
        GridData gridData = new GridData(GridData.FILL_BOTH);
        gridData.widthHint = 570;
        composite.setLayoutData(gridData);
        IProxyRepositoryFactory factory = ProxyRepositoryFactory.getInstance();
        if (factory.isUserReadOnlyOnCurrentProject()) {
            compositesSachForm.setEnabled(false);
        }

        //
        addRepositoryTreeViewer(compositesSachForm.getLeftComposite());

        addButtons(compositesSachForm.getMidComposite());

        addItemTableViewer(compositesSachForm.getRightComposite());

        return composite;
    }

    private static final String TITLE = Messages.getString("VersionManagementDialog.Title"); //$NON-NLS-1$

    private static final IProxyRepositoryFactory FACTORY = CorePlugin.getDefault().getProxyRepositoryFactory();

    private static final String ITEM_EDITOR_KEY = "ITEM_EDITOR_KEY"; //$NON-NLS-1$

    private Map<IImage, Image> cacheItemImages = new HashMap<IImage, Image>();

    private Project project = ProjectManager.getInstance().getCurrentProject();

    private CheckboxRepositoryTreeViewer treeViewer;

    private Button removeBtn;

    private Table itemTable;

    private Button fixedVersionBtn;

    private Text maxVersionText;

    private Button majorBtn;

    private Button minorBtn;

    private Button revertBtn;

    private Button alldependcies;

    private Button subjobs;

    private Button eachVersionBtn;

    private Button versionLatest;

    private List<ItemVersionObject> versionObjects = new ArrayList<ItemVersionObject>();

    private List<ItemVersionObject> checkedObjects = new ArrayList<ItemVersionObject>();

    private IProxyRepositoryFactory factory = CorePlugin.getDefault().getProxyRepositoryFactory();

    private RelationshipItemBuilder builder = RelationshipItemBuilder.getInstance();

    /**
     * repository tree viewer.
     */
    private void addRepositoryTreeViewer(Composite leftComposite) {
        GridData gridData = new GridData(GridData.FILL_BOTH);
        gridData.widthHint = 210;
        gridData.heightHint = 400;
        leftComposite.setLayoutData(gridData);

        RepoCommonViewerProvider provider = RepoCommonViewerProvider.CHECKBOX;
        treeViewer = (CheckboxRepositoryTreeViewer) provider.createViewer(leftComposite);

        IProjectRepositoryNode projectRepositoryNode = provider.getProjectRepositoryNode();
        processItems(versionObjects, (RepositoryNode) projectRepositoryNode);
        // filter
        treeViewer.addFilter(new ViewerFilter() {

            @Override
            public boolean select(Viewer viewer, Object parentElement, Object element) {
                RepositoryNode node = (RepositoryNode) element;
                return filterRepositoryNode(node);
            }
        });
        // event
        treeViewer.addCheckStateListener(new ICheckStateListener() {

            @Override
            public void checkStateChanged(CheckStateChangedEvent event) {
                RepositoryNode node = (RepositoryNode) event.getElement();
                List<ItemVersionObject> objects = new ArrayList<ItemVersionObject>();
                processItems(objects, node);
                if (!objects.isEmpty()) {
                    if (event.getChecked()) {
                        checkedObjects.addAll(objects);
                    } else {
                        checkedObjects.removeAll(objects);
                        removeItemElements(objects);
                    }
                    // add fro bug TDI-22379
                    if (node != null && (ERepositoryObjectType.PROCESS).equals(node.getContentType())) {
                        boolean isEnable = false;
                        if (event.getChecked()) {
                            isEnable = true;
                        }
                        checkButtonsState(isEnable);
                    }
                    researchMaxVersion();
                    refreshTableItems();
                }
            }
        });
        treeViewer.addTreeListener(new ITreeViewerListener() {

            @Override
            public void treeCollapsed(TreeExpansionEvent event) {
                //
            }

            @Override
            public void treeExpanded(TreeExpansionEvent event) {
                // refreshCheckedTreeView();
            }
        });

        expandSomeNodes(projectRepositoryNode);
    }

    private void expandSomeNodes(final IProjectRepositoryNode rootNode) {

        // metadata
        IRepositoryNode metadataConNode = rootNode.getRootRepositoryNode(ERepositoryObjectType.METADATA);
        if (metadataConNode != null) {
            treeViewer.expandToLevel(metadataConNode, 1);
        }
        // code
        IRepositoryNode codeNode = rootNode.getRootRepositoryNode(ERepositoryObjectType.CODE);
        if (codeNode != null) {
            treeViewer.expandToLevel(codeNode, 1);
        }
    }

    private boolean filterRepositoryNode(RepositoryNode node) {
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

    private void processItems(List<ItemVersionObject> objects, RepositoryNode node) {
        if (node == null) {
            return;
        }
        // if the root node of type is not init, force init.
        IProjectRepositoryNode root = node.getRoot();
        if (root instanceof ProjectRepositoryNode) {
            ((ProjectRepositoryNode) root).initNode(node);
        }
        if (node.getType() == ENodeType.REPOSITORY_ELEMENT) {
            if (node.getObject() != null) {
                Property property = node.getObject().getProperty();
                Item item = property.getItem();
                if (item != null && filterRepositoryNode(node)) { // must be item
                    ItemVersionObject object = new ItemVersionObject(property, node, property.getVersion());
                    objects.add(object);
                }
            }
        } else {
            IContentProvider contentProvider = treeViewer.getContentProvider();
            if (contentProvider instanceof NavigatorContentServiceContentProvider) {
                NavigatorContentServiceContentProvider navigatorProvider = (NavigatorContentServiceContentProvider) contentProvider;
                Object[] children = navigatorProvider.getChildren(node);
                for (Object child : children) {
                    if (child instanceof RepositoryNode) {
                        processItems(objects, (RepositoryNode) child);
                    }
                }
            } else {
                for (IRepositoryNode child : node.getChildren()) {
                    processItems(objects, (RepositoryNode) child);
                }
            }
        }
    }

    private void addButtons(Composite middleComposite) {
        Composite btnComposite = new Composite(middleComposite, SWT.NONE);
        btnComposite.setLayout(new GridLayout(1, true));
        GridData data = new GridData(GridData.FILL_BOTH);
        data.verticalAlignment = GridData.CENTER;
        btnComposite.setLayoutData(data);

        removeBtn = new Button(btnComposite, SWT.NONE);
        removeBtn.setImage(ImageProvider.getImage(EImage.LEFT_ICON));
        removeBtn.setToolTipText(Messages.getString("VersionManagementDialog.RemoveTip")); //$NON-NLS-1$
        removeBtn.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                TableItem[] selections = itemTable.getSelection();
                itemTable.setRedraw(false);
                for (TableItem item : selections) {
                    Object data = item.getData();
                    checkedObjects.remove(data);
                    removeTableItem(item);
                }
                itemTable.setRedraw(true);
                refreshCheckedTreeView();
                refreshTableItems();
                checkButtonsState(false);
            }
        });

        removeBtn.setEnabled(false);
    }

    private void addItemTableViewer(Composite rightComposite) {
        Composite composite = new Composite(rightComposite, SWT.NONE);
        composite.setLayout(new GridLayout());
        GridDataFactory.fillDefaults().grab(true, true).applyTo(composite);

        addItemsOption(composite);

        itemTable = new Table(composite, SWT.MULTI | SWT.BORDER);
        GridDataFactory.fillDefaults().grab(true, true).applyTo(itemTable);
        itemTable.setHeaderVisible(true);
        itemTable.setLinesVisible(true);

        //
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

        versionColumn.addControlListener(new ControlListener() {

            @Override
            public void controlMoved(ControlEvent e) {
                //
            }

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

    private void addItemsOption(Composite parent) {
        Group option = new Group(parent, SWT.NONE);
        option.setLayout(new GridLayout());
        option.setText(Messages.getString("VersionManagementDialog.Options")); //$NON-NLS-1$
        GridDataFactory.swtDefaults().align(SWT.FILL, SWT.CENTER).applyTo(option);
        fixedVersionBtn = new Button(option, SWT.RADIO);
        fixedVersionBtn.setText(Messages.getString("VersionManagementDialog.FixedVersion")); //$NON-NLS-1$
        fixedVersionBtn.setSelection(true); // default

        IBrandingService brandingService = (IBrandingService) GlobalServiceRegister.getDefault().getService(
                IBrandingService.class);
        allowVerchange = brandingService.getBrandingConfiguration().isAllowChengeVersion();

        Composite versionComposit = new Composite(option, SWT.NONE);
        GridLayout layout = new GridLayout(8, false);
        layout.horizontalSpacing = 1;
        layout.verticalSpacing = 0;
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        versionComposit.setLayout(layout);
        GridDataFactory.swtDefaults().align(SWT.FILL, SWT.BEGINNING).applyTo(versionComposit);

        maxVersionText = new Text(versionComposit, SWT.BORDER | SWT.READ_ONLY);
        GridData data = new GridData();
        data.widthHint = 50;
        data.minimumWidth = 50;
        maxVersionText.setLayoutData(data);
        maxVersionText.setEnabled(false);
        maxVersionText.setText(VersionUtils.DEFAULT_VERSION);

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
        alldependcies.setText(Messages.getString("VersionManagementDialog.AllDependencies"));
        alldependcies.setEnabled(false);

        subjobs = new Button(versionComposit, SWT.NONE);
        subjobs.setText(Messages.getString("VersionManagementDialog.Subjob"));
        subjobs.setEnabled(false);

        eachVersionBtn = new Button(option, SWT.RADIO);
        eachVersionBtn.setText(Messages.getString("VersionManagementDialog.EachVersion")); //$NON-NLS-1$
        eachVersionBtn.setEnabled(allowVerchange);

        versionLatest = new Button(option, SWT.CHECK);
        versionLatest.setText(Messages.getString("VersionManagementDialog.FixVersion"));
        versionLatest.setToolTipText(Messages.getString("VersionManagementDialog.FixLastVersion"));
        // event
        fixedVersionBtn.addSelectionListener(new SelectionAdapter() {

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
                String version = maxVersionText.getText();
                version = VersionUtils.upMajor(version);
                maxVersionText.setText(version);
                refreshTableItems();
            }
        });
        minorBtn.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                String version = maxVersionText.getText();
                version = VersionUtils.upMinor(version);
                maxVersionText.setText(version);
                refreshTableItems();
            }
        });
        revertBtn.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                maxVersionText.setText(VersionUtils.DEFAULT_VERSION); // set min version
                researchMaxVersion();
                refreshTableItems();
            }
        });
        eachVersionBtn.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                checkFixedButtons();
                refreshTableItems();
            }
        });
        checkFixedButtons();
    }

    private void selectAllDependencies() {
        List<ItemVersionObject> tableList = new ArrayList<ItemVersionObject>();
        tableList.addAll(getModifiedVersionItems());
        IProxyRepositoryFactory factory = CorePlugin.getDefault().getProxyRepositoryFactory();
        RelationshipItemBuilder builder = RelationshipItemBuilder.getInstance();
        for (ItemVersionObject object : getModifiedVersionItems()) {
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
        getModifiedVersionItems().clear();
        getModifiedVersionItems().addAll(tableList);
        refreshTableItems();
        refreshCheckedTreeView();

    }

    private void versionLatest() {
        List<ItemVersionObject> tableList = new ArrayList<ItemVersionObject>();
        tableList.addAll(getModifiedVersionItems());

        for (ItemVersionObject object : getModifiedVersionItems()) {
            if (object.getRepositoryNode() != null) {
                List<Relation> relations = builder.getItemsJobRelatedTo(object.getRepositoryNode().getId(),
                        object.getOldVersion(), RelationshipItemBuilder.JOB_RELATION);
                for (Relation relation : relations) {
                    try {
                        IRepositoryViewObject obj = factory.getLastVersion(relation.getId());
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
        getModifiedVersionItems().clear();
        getModifiedVersionItems().addAll(tableList);
        refreshTableItems();
        refreshCheckedTreeView();
    }

    private void checkAllVerSionLatest(List<ItemVersionObject> tableList, ItemVersionObject object) {
        // List<ItemVersionObject> tableList = new ArrayList<ItemVersionObject>();
        IProxyRepositoryFactory factory = CorePlugin.getDefault().getProxyRepositoryFactory();
        RelationshipItemBuilder builder = RelationshipItemBuilder.getInstance();
        // for (ItemVersionObject object : getModifiedVersionItems()) {
        if (object.getRepositoryNode() != null) {
            List<Relation> relations = builder.getItemsJobRelatedTo(object.getRepositoryNode().getId(), object.getOldVersion(),
                    RelationshipItemBuilder.JOB_RELATION);
            for (Relation relation : relations) {
                try {
                    IRepositoryViewObject obj = factory.getLastVersion(relation.getId());
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
        // }
    }

    private void selectSubjob() {
        List<ItemVersionObject> tableList = new ArrayList<ItemVersionObject>();
        List<ItemVersionObject> jobList = new ArrayList<ItemVersionObject>();

        for (ItemVersionObject object : getModifiedVersionItems()) {
            if (ERepositoryObjectType.getItemType(object.getItem()).equals(ERepositoryObjectType.PROCESS)) {
                jobList.add(object);
            }
        }

        tableList.addAll(jobList);
        IProxyRepositoryFactory factory = CorePlugin.getDefault().getProxyRepositoryFactory();
        RelationshipItemBuilder builder = RelationshipItemBuilder.getInstance();
        for (ItemVersionObject object : jobList) {
            if (object.getRepositoryNode() != null) {
                List<Relation> relations = builder.getItemsJobRelatedTo(object.getRepositoryNode().getId(),
                        object.getOldVersion(), RelationshipItemBuilder.JOB_RELATION);
                for (Relation relation : relations) {
                    try {
                        IRepositoryViewObject obj = factory.getLastVersion(relation.getId());
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
        getModifiedVersionItems().clear();
        getModifiedVersionItems().addAll(tableList);
        refreshTableItems();
        refreshCheckedTreeView();
    }

    private void refreshTableItems() {
        removeItemElements(checkedObjects);
        addItemElements(checkedObjects);
        // if (checkedObjects.isEmpty()) {
        // maxVersionText.setText(VersionUtils.DEFAULT_VERSION);
        // }
        checkButtonsState();
    }

    private void researchMaxVersion() {
        String version = maxVersionText.getText();
        if ("".equals(version.trim())) { //$NON-NLS-1$
            version = VersionUtils.DEFAULT_VERSION;
        }
        for (ItemVersionObject object : checkedObjects) {
            if (VersionUtils.compareTo(version, object.getOldVersion()) < 0) {
                version = object.getOldVersion();
            }
        }
        maxVersionText.setText(version);
    }

    private void checkButtonsState() {
        TableItem[] selections = itemTable.getSelection();
        if (selections != null && selections.length > 0) {
            removeBtn.setEnabled(true);
        } else {
            removeBtn.setEnabled(false);
        }
    }

    private void checkButtonsState(boolean isEnable) {
        boolean flag = isEnable;
        TableItem[] items = itemTable.getItems();
        for (TableItem item : items) {
            if (item.getData() instanceof ItemVersionObject) {
                ItemVersionObject itemVersionObj = (ItemVersionObject) item.getData();
                RepositoryNode repositoryNode = itemVersionObj.getRepositoryNode();
                if (repositoryNode != null && (ERepositoryObjectType.PROCESS).equals(repositoryNode.getContentType())) {
                    flag = true;
                }
            }
        }
        alldependcies.setEnabled(flag);
        subjobs.setEnabled(flag);
    }

    private boolean isFixedVersion() {
        return fixedVersionBtn.getSelection();
    }

    private void checkFixedButtons() {
        majorBtn.setEnabled(isFixedVersion() && allowVerchange);
        minorBtn.setEnabled(isFixedVersion() && allowVerchange);
        revertBtn.setEnabled(isFixedVersion());
        // versionLatest.setEnabled(isFixedVersion());
    }

    private void removeTableItem(TableItem item) {
        if (item == null) {
            return;
        }
        TableEditor[] editors = (TableEditor[]) item.getData(ITEM_EDITOR_KEY);
        if (editors != null) {
            for (TableEditor editor : editors) {
                editor.getEditor().dispose();
                editor.dispose();
            }
        }
        item.dispose();
    }

    private void removeItemElements(List<ItemVersionObject> objects) {
        itemTable.setRedraw(false);
        TableItem[] items = itemTable.getItems();
        for (TableItem item : items) {
            if (objects.contains(item.getData())) {
                removeTableItem(item);
            }
        }
        itemTable.setRedraw(true);
    }

    private void refreshCheckedTreeView() {
        List<RepositoryNode> nodes = new ArrayList<RepositoryNode>();

        for (TableItem item : itemTable.getItems()) {
            Object data = item.getData();
            if (data instanceof ItemVersionObject) {
                nodes.add(((ItemVersionObject) data).getRepositoryNode());
            }
        }
        treeViewer.setCheckedElements(nodes.toArray());
        // treeViewer.refresh();
    }

    private Image getItemsImage(IImage iImage) {
        if (iImage == null) {
            iImage = EImage.DEFAULT_IMAGE;
        }
        Image image = cacheItemImages.get(iImage);
        if (image == null) {
            Image oImage = ImageProvider.getImage(iImage);
            ImageData imageData = oImage.getImageData();
            // enlarge image
            final int larger = 4;
            ImageData newData = imageData.scaledTo(imageData.width + larger, imageData.height + larger);
            image = new Image(oImage.getDevice(), newData);
            cacheItemImages.put(iImage, image);
        }
        return image;
    }

    private void addItemElements(List<ItemVersionObject> elements) {
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
                String version = maxVersionText.getText();
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

    public List<ItemVersionObject> getModifiedVersionItems() {
        return this.checkedObjects;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.preference.PreferencePage#performApply()
     */
    @Override
    protected void performApply() {
        super.performApply();
        isApplied = true;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.preference.PreferencePage#performOk()
     */
    @Override
    public boolean performOk() {
        okPressed();
        return super.performOk();
    }

    protected void okPressed() {
        if (maxVersionText == null) {
            return;
        }
        boolean modified = false;
        String newVersion = maxVersionText.getText();
        for (ItemVersionObject object : getModifiedVersionItems()) {
            if (!isFixedVersion()) {
                newVersion = object.getNewVersion();
            }
            IRepositoryViewObject repositoryObject = object.getRepositoryNode().getObject();
            if (repositoryObject != null && repositoryObject.getProperty() != null) {
                if (!newVersion.equals(repositoryObject.getVersion())) {
                    isApplied = false;
                    modified = true;
                    break;
                }
            }
        }
        if (modified) {
            boolean confirm = false;
            if (isFixedVersion()) {
                confirm = MessageDialog.openConfirm(getShell(), Messages.getString("VersionManagementDialog.ConfirmTitle"), //$NON-NLS-1$
                        Messages.getString("VersionManagementDialog.ConfirmMessage", newVersion)); //$NON-NLS-1$
                if (confirm) {
                    // set all items for new version
                    for (ItemVersionObject object : getModifiedVersionItems()) {
                        object.setNewVersion(newVersion);
                    }
                }
            } else {
                ItemsVersionConfirmDialog chanedDialog = new ItemsVersionConfirmDialog(getShell(), getModifiedVersionItems());
                confirm = (chanedDialog.open() == Window.OK);
            }

            if (confirm) {
                updateItemsVersion();

            }
        } else {
            if (!getModifiedVersionItems().isEmpty() && !isApplied) {
                MessageDialog.openWarning(getShell(), Messages.getString("VersionManagementDialog.WarningTitle"), //$NON-NLS-1$
                        Messages.getString("VersionManagementDialog.WarningMessages")); //$NON-NLS-1$
            }

        }

    }

    private void updateItemsVersion() {
        final IWorkspaceRunnable runnable = new IWorkspaceRunnable() {

            @Override
            public void run(final IProgressMonitor monitor) throws CoreException {
                RepositoryWorkUnit<Object> rwu = new RepositoryWorkUnit<Object>(project, "Update items version") {

                    @Override
                    protected void run() throws LoginException, PersistenceException {
                        monitor.beginTask("Update items version", getModifiedVersionItems().size()); //$NON-NLS-1$
                        Map<String, String> versions = new HashMap<String, String>();
                        for (int i = 0; i < getModifiedVersionItems().size(); i++) {
                            ItemVersionObject object = getModifiedVersionItems().get(i);
                            versions.put(object.getItem().getProperty().getId(), object.getOldVersion());
                        }
                        RelationshipItemBuilder builder = RelationshipItemBuilder.getInstance();
                        Set<ERepositoryObjectType> types = new HashSet<ERepositoryObjectType>();
                        for (ItemVersionObject object : getModifiedVersionItems()) {
                            IRepositoryViewObject repositoryObject = object.getRepositoryNode().getObject();
                            if (repositoryObject != null && repositoryObject.getProperty() != null) {
                                if (!object.getNewVersion().equals(repositoryObject.getVersion())) {
                                    final Item item = object.getItem();
                                    Property itemProperty = item.getProperty();
                                    itemProperty.setVersion(object.getNewVersion());
                                    monitor.subTask(itemProperty.getLabel());

                                    types.add(object.getRepositoryNode().getObjectType());

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
                        // the following is commented cause the refresh is now automatically done when file are changed
                        // on the file system.
                        // for bug 20256,first open studio,don't expand repositoryTree,
                        // open projectSetting,change item Verson,then expand Tree,routine,jobscript and metadata always
                        // use old version.must refresh all tree.
                        // RepositoryManager.refresh(types);
                        // IRepositoryView view = RepositoryManagerHelper.findRepositoryView();
                        // if (view != null) {
                        // view.refresh();
                        // }
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

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.repository.preference.ProjectSettingPage#refresh()
     */
    @Override
    public void refresh() {
        // TODO Auto-generated method stub

    }
}
