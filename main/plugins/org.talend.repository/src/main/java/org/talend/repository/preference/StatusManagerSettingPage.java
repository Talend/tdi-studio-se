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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.preference.IPreferenceStore;
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
import org.eclipse.swt.custom.CCombo;
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
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.internal.navigator.NavigatorContentServiceContentProvider;
import org.eclipse.ui.internal.navigator.NavigatorDecoratingLabelProvider;
import org.talend.commons.exception.PersistenceException;
import org.talend.commons.runtime.model.repository.ERepositoryStatus;
import org.talend.commons.ui.gmf.util.DisplayUtils;
import org.talend.commons.ui.runtime.exception.ExceptionHandler;
import org.talend.commons.ui.runtime.image.EImage;
import org.talend.commons.ui.runtime.image.IImage;
import org.talend.commons.ui.runtime.image.ImageProvider;
import org.talend.commons.ui.swt.advanced.composite.ThreeCompositesSashForm;
import org.talend.commons.ui.swt.dialogs.ErrorDialogWidthDetailArea;
import org.talend.commons.utils.VersionUtils;
import org.talend.core.CorePlugin;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.model.general.Project;
import org.talend.core.model.properties.Item;
import org.talend.core.model.properties.Property;
import org.talend.core.model.properties.helper.StatusHelper;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.model.repository.RepositoryManager;
import org.talend.core.model.repository.RepositoryObject;
import org.talend.core.repository.model.ProjectRepositoryNode;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.core.ui.images.CoreImageProvider;
import org.talend.repository.ProjectManager;
import org.talend.repository.RepositoryPlugin;
import org.talend.repository.i18n.Messages;
import org.talend.repository.model.IProxyRepositoryFactory;
import org.talend.repository.model.IRepositoryNode;
import org.talend.repository.model.IRepositoryNode.ENodeType;
import org.talend.repository.model.IRepositoryService;
import org.talend.repository.model.RepositoryConstants;
import org.talend.repository.model.RepositoryNode;
import org.talend.repository.model.RepositoryPreferenceStore;
import org.talend.repository.model.nodes.IProjectRepositoryNode;
import org.talend.repository.ui.dialog.StatusConfirmSettingDialog;
import org.talend.repository.viewer.ui.provider.RepoCommonViewerProvider;
import org.talend.repository.viewer.ui.viewer.CheckboxRepositoryTreeViewer;

/**
 * DOC gldu class global comment. Detailled comment
 */
public class StatusManagerSettingPage extends ProjectSettingPage {

    private boolean isApplied;

    private static final IProxyRepositoryFactory FACTORY = CorePlugin.getDefault().getProxyRepositoryFactory();

    private static final String ITEM_EDITOR_KEY = "ITEM_EDITOR_KEY"; //$NON-NLS-1$

    private Map<IImage, Image> cacheItemImages = new HashMap<IImage, Image>();

    private Project project = ProjectManager.getInstance().getCurrentProject();

    private CheckboxRepositoryTreeViewer treeViewer;

    private Button removeBtn;

    private Table itemTable;

    private Button techinalButton;

    private Button documentStatusButton;

    private Combo statusCombo;

    private Button revertBtn;

    private Button eachStatusBtn;

    private List<RepositoryObject> checkedObjects = new ArrayList<RepositoryObject>();

    private org.talend.repository.ui.properties.StatusHelper statusHelper;

    List technicalStatusList = null;

    List documentStatusList = null;

    IRepositoryService service = (IRepositoryService) GlobalServiceRegister.getDefault().getService(IRepositoryService.class);

    private static final String TITLE = Messages.getString("StatusManagementDialog.Title"); //$NON-NLS-1$

    public StatusManagerSettingPage() {
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

        statusHelper = new org.talend.repository.ui.properties.StatusHelper(service.getProxyRepositoryFactory());

        return composite;
    }

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
                List<RepositoryObject> objects = new ArrayList<RepositoryObject>();
                processItems(objects, node);
                if (!objects.isEmpty()) {
                    if (event.getChecked()) {
                        checkedObjects.addAll(objects);
                    } else {
                        checkedObjects.removeAll(objects);
                        removeItemElements(objects);
                    }
                    // researchMaxVersion();
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

        expandSomeNodes(provider.getProjectRepositoryNode());
    }

    private void expandSomeNodes(IProjectRepositoryNode root) {
        if (root instanceof IProjectRepositoryNode) {
            final IProjectRepositoryNode rootNode = root;
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

    private void processItems(List<RepositoryObject> objects, IRepositoryNode node) {
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
                if (item != null && filterRepositoryNode((RepositoryNode) node)) { // must be item
                    RepositoryObject object = new RepositoryObject(node.getObject().getProperty());
                    object.setRepositoryNode(node);
                    // ItemVersionObject object = new ItemVersionObject(node, property.getVersion());
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
                    processItems(objects, child);
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
            }
        });

        removeBtn.setEnabled(false);
    }

    private void addItemTableViewer(Composite rightComposite) {

        Composite composite = new Composite(rightComposite, SWT.NONE);
        GridLayout gdlComposite = new GridLayout();
        gdlComposite.marginHeight = 0;
        gdlComposite.marginWidth = 0;
        composite.setLayout(gdlComposite);

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
        oldVersionColumn.setText(Messages.getString("StatusManagementDialog.Status")); //$NON-NLS-1$
        oldVersionColumn.setWidth(90);

        TableColumn versionColumn = new TableColumn(itemTable, SWT.CENTER);
        versionColumn.setText(Messages.getString("StatusManagementDialog.NewStatus")); //$NON-NLS-1$
        versionColumn.setWidth(92);

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
                if (!isFixedstatus()) {
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
        techinalButton = new Button(option, SWT.RADIO);
        techinalButton.setText(Messages.getString("StatusManagementDialog.FixedStatus")); //$NON-NLS-1$
        techinalButton.setSelection(true); // default

        documentStatusButton = new Button(option, SWT.RADIO);
        documentStatusButton.setText(Messages.getString("StatusManagementDialog.FixedDocumentStatus"));

        Composite versionComposit = new Composite(option, SWT.NONE);
        GridLayout layout = new GridLayout(5, false);
        layout.horizontalSpacing = 1;
        layout.verticalSpacing = 0;
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        versionComposit.setLayout(layout);
        GridDataFactory.swtDefaults().align(SWT.FILL, SWT.BEGINNING).applyTo(versionComposit);

        statusCombo = new Combo(versionComposit, SWT.BORDER | SWT.READ_ONLY);
        GridData data = new GridData();
        data.widthHint = 90;
        data.minimumWidth = 50;
        statusCombo.setLayoutData(data);
        // String s = doGetPreferenceStore().getString("org.talend.repository.properties.status.technical");
        // if (s != null && !"".equals(s)) {
        // String[] tmp = s.split(";");
        // statusCombo.setItems(tmp);
        // }
        try {
            technicalStatusList = service.getProxyRepositoryFactory().getTechnicalStatus();
            documentStatusList = service.getProxyRepositoryFactory().getDocumentationStatus();
        } catch (PersistenceException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        statusCombo.setItems(toArray(technicalStatusList));

        statusCombo.select(0);
        // org.talend.repository.properties.status.documentation
        // majorBtn = new Button(versionComposit, SWT.NONE);
        //        majorBtn.setText("M"); //$NON-NLS-1$
        //        majorBtn.setToolTipText(Messages.getString("VersionManagementDialog.MajorVersionTip")); //$NON-NLS-1$
        // minorBtn = new Button(versionComposit, SWT.NONE);
        //        minorBtn.setText("m"); //$NON-NLS-1$
        //        minorBtn.setToolTipText(Messages.getString("VersionManagementDialog.MinorVersionTip")); //$NON-NLS-1$

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

        eachStatusBtn = new Button(option, SWT.RADIO);
        eachStatusBtn.setText(Messages.getString("StatusManagementDialog.EachStatus")); //$NON-NLS-1$

        // event
        techinalButton.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                statusCombo.removeAll();
                checkFixedButtons();
                documentStatusButton.setSelection(false);
                techinalButton.setSelection(true);
                eachStatusBtn.setSelection(false);
                statusCombo.setEnabled(true);
                itemTable.clearAll();
                String s = doGetPreferenceStore().getString("org.talend.repository.properties.status.technical");
                statusCombo.setItems(toArray(technicalStatusList));
                statusCombo.select(0);
                // researchMaxVersion();
                refreshTableItems();
            }
        });
        statusCombo.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                String version = statusCombo.getText();
                refreshTableItems();
            }
        });
        // minorBtn.addSelectionListener(new SelectionAdapter() {
        //
        // public void widgetSelected(SelectionEvent e) {
        // String version = maxVersionText.getText();
        // version = VersionUtils.upMinor(version);
        // maxVersionText.setText(version);
        // refreshTableItems();
        // }
        // });
        documentStatusButton.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                statusCombo.removeAll();
                documentStatusButton.setSelection(true);
                eachStatusBtn.setSelection(false);
                techinalButton.setSelection(false);
                statusCombo.setEnabled(true);
                itemTable.clearAll();
                statusCombo.setItems(toArray(documentStatusList));
                statusCombo.select(0);
                refreshTableItems();
            }
        });
        revertBtn.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                statusCombo.setText(VersionUtils.DEFAULT_VERSION); // set min version
                // researchMaxVersion();
                refreshTableItems();
            }
        });
        eachStatusBtn.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                techinalButton.setSelection(false);
                documentStatusButton.setSelection(false);
                eachStatusBtn.setSelection(true);
                statusCombo.setEnabled(false);
                checkFixedButtons();
                refreshTableItems();
            }
        });
        checkFixedButtons();
    }

    @Override
    protected IPreferenceStore doGetPreferenceStore() {
        RepositoryPreferenceStore preferenceStore = new RepositoryPreferenceStore(ProxyRepositoryFactory.getInstance());
        try {
            preferenceStore.load();
        } catch (PersistenceException e) {
            String detailError = e.getMessage();
            new ErrorDialogWidthDetailArea(DisplayUtils.getDefaultShell(false), RepositoryPlugin.PLUGIN_ID,
                    Messages.getString("CommonWizard.persistenceException"), detailError); //$NON-NLS-1$
        }
        return preferenceStore;
    }

    protected List<String> readString(String stringList) {
        return StatusHelper.readString(stringList);
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
        String version = statusCombo.getText();
        if ("".equals(version.trim())) { //$NON-NLS-1$
            version = VersionUtils.DEFAULT_VERSION;
        }
        for (RepositoryObject object : checkedObjects) {
            // if (VersionUtils.compareTo(version, object.getOldVersion()) < 0) {
            // version = object.getOldVersion();
            // }
        }
        statusCombo.setText(version);
    }

    private void checkButtonsState() {
        TableItem[] selections = itemTable.getSelection();
        if (selections != null && selections.length > 0) {
            removeBtn.setEnabled(true);
        } else {
            removeBtn.setEnabled(false);
        }
    }

    private boolean isFixedstatus() {
        return techinalButton.getSelection() || documentStatusButton.getSelection();
    }

    private void checkFixedButtons() {
        // majorBtn.setEnabled(isFixedVersion());
        // minorBtn.setEnabled(isFixedVersion());
        revertBtn.setEnabled(isFixedstatus());
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

    private void removeItemElements(List<RepositoryObject> objects) {
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
            if (data instanceof RepositoryObject) {
                nodes.add((RepositoryNode) ((RepositoryObject) data).getRepositoryNode());
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

    public String[] toArray(List<org.talend.core.model.properties.Status> status) {
        String[] res = new String[status.size()];
        int i = 0;
        for (org.talend.core.model.properties.Status s : status) {
            res[i++] = s.getLabel();
        }
        return res;
    }

    private Boolean isDocumentStatus() {
        return documentStatusButton.getSelection();
    }

    private Boolean isTechinalStatus() {
        return techinalButton.getSelection();
    }

    private void addItemElements(List<RepositoryObject> elements) {
        if (elements == null || elements.isEmpty()) {
            return;
        }

        final Color redColor = Display.getDefault().getSystemColor(SWT.COLOR_RED);
        for (final RepositoryObject object : elements) {
            if (object.isDeleted()) {
                continue;
            }
            TableItem tableItem = null;

            // for bug 17692
            ERepositoryObjectType objectType = object.getRepositoryNode().getObjectType();
            ERepositoryObjectType type = object.getRepositoryNode().getContentType();
            if (!objectType.equals(ERepositoryObjectType.JOB_DOC) && !objectType.equals(ERepositoryObjectType.JOBLET_DOC)) {
                if (isTechinalStatus()) {
                    // Modified by Marvin Wang on Jan.7, 2011 for bug Talend DI TDI-19154, should not use
                    // obj.equals(constant) that will cause NPE.
                    if (!type.equals(ERepositoryObjectType.DOCUMENTATION) && !type.equals(ERepositoryObjectType.BUSINESS_PROCESS)
                            && !type.equals(ERepositoryObjectType.JOBLET_DOC)) {
                        // if (!type.equals(ERepositoryObjectType.DOCUMENTATION) &&
                        // !type.equals(ERepositoryObjectType.BUSINESS_PROCESS)
                        // && !type.equals(ERepositoryObjectType.JOBLETS)) {
                        itemTable.setRedraw(false);
                        tableItem = new TableItem(itemTable, SWT.NONE);
                    }
                } else if (isDocumentStatus()) {
                    if (type.equals(ERepositoryObjectType.DOCUMENTATION) || type.equals(ERepositoryObjectType.BUSINESS_PROCESS)) {
                        itemTable.setRedraw(false);
                        tableItem = new TableItem(itemTable, SWT.NONE);
                    }
                }
                // bug 20078
                else {
                    itemTable.setRedraw(false);
                    tableItem = new TableItem(itemTable, SWT.NONE);
                }
            } else {
                if (isDocumentStatus()) {
                    if (type.equals(ERepositoryObjectType.JOB_DOC) || type.equals(ERepositoryObjectType.JOBLET_DOC)) {
                        itemTable.setRedraw(false);
                        tableItem = new TableItem(itemTable, SWT.NONE);
                    }
                }
            }

            if (tableItem != null) {
                tableItem.setData(object);
                try {
                    statusHelper.getStatusList(object.getProperty());
                } catch (PersistenceException e1) {
                    // TODO Auto-generated catch block
                }
                object.getProperty().setOldStatusCode(statusHelper.getStatusCode(object.getStatusCode()));
                Image itemsImage = null;
                IBaseLabelProvider labelProvider = treeViewer.getLabelProvider();
                if (labelProvider instanceof NavigatorDecoratingLabelProvider) {
                    NavigatorDecoratingLabelProvider navigatorProvider = (NavigatorDecoratingLabelProvider) labelProvider;
                    itemsImage = navigatorProvider.getImage(object.getRepositoryNode());
                } else {
                    ERepositoryObjectType itemType = object.getRepositoryObjectType();
                    itemsImage = getItemsImage(CoreImageProvider.getIcon(itemType));
                }
                tableItem.setImage(itemsImage);
                tableItem.setText(object.getLabel());
                // old version
                tableItem.setText(1, statusHelper.getStatusLabel(object.getStatusCode()));

                TableEditor versionEditor = null;

                if (isFixedstatus()) {
                    String version = statusCombo.getText();
                    tableItem.setText(2, version);

                    if (!object.getProperty().getOldStatusCode().equals(statusHelper.getStatusCode(version))) {
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

                    final CCombo statusItemCombo = new CCombo(versionComposit, SWT.BORDER | SWT.READ_ONLY);
                    GridData data = new GridData(GridData.FILL_HORIZONTAL);
                    statusItemCombo.setLayoutData(data);
                    statusItemCombo.setEditable(false);
                    if (!type.equals(ERepositoryObjectType.DOCUMENTATION) && !type.equals(ERepositoryObjectType.BUSINESS_PROCESS)) {
                        statusItemCombo.setItems(toArray(technicalStatusList));
                        statusItemCombo.select(0);
                        if (!object.getProperty().getOldStatusCode().equals("DEV")) {
                            statusItemCombo.setForeground(redColor);
                        } else {
                            statusItemCombo.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_BLUE));
                        }
                    } else {
                        statusItemCombo.setItems(toArray(documentStatusList));
                        statusItemCombo.select(0);
                        if (!object.getProperty().getOldStatusCode().equals("UCK")) {
                            statusItemCombo.setForeground(redColor);
                        } else {
                            statusItemCombo.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_BLUE));
                        }
                    }
                    statusItemCombo.addSelectionListener(new SelectionAdapter() {

                        @Override
                        public void widgetSelected(SelectionEvent e) {
                            String version = statusItemCombo.getText();
                            String status = statusHelper.getStatusCode(version);
                            if (!status.equals(object.getProperty().getOldStatusCode())) {
                                statusItemCombo.setForeground(redColor);
                            }
                            object.getProperty().setStatusCode(statusHelper.getStatusCode(version));
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
                addLabelMouseListener(delLabel, object, tableItem);
                delEditor.minimumWidth = 25;
                delEditor.horizontalAlignment = SWT.CENTER;
                delEditor.setEditor(delLabel, tableItem, 3);
                if (isFixedstatus()) {
                    tableItem.setData(ITEM_EDITOR_KEY, new TableEditor[] { delEditor });
                } else if (versionEditor != null) {
                    tableItem.setData(ITEM_EDITOR_KEY, new TableEditor[] { versionEditor, delEditor });
                }
                itemTable.setRedraw(true);
            }

        }
    }

    public void addLabelMouseListener(Label delLabel, final RepositoryObject object, final TableItem tableItem) {
        delLabel.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseDown(MouseEvent e) {
                checkedObjects.remove(object);
                removeTableItem(tableItem);
                refreshCheckedTreeView();
                checkButtonsState();
            }

        });
    }

    public List<RepositoryObject> getModifiedVersionItems() {
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
        if (statusCombo == null) {
            return;
        }

        boolean modified = false;
        String newStatus = null;
        if (getModifiedVersionItems().size() > 0) {
            newStatus = statusHelper.getStatusCode(statusCombo.getText());
        }
        for (RepositoryObject object : getModifiedVersionItems()) {
            if (!isFixedstatus()) {
                newStatus = object.getStatusCode();
            }
            if (object != null && object.getProperty() != null) {
                if (!newStatus.equals(object.getProperty().getOldStatusCode())) {
                    isApplied = false;
                    modified = true;
                    break;
                }
            }
        }
        if (modified) {
            boolean confirm = false;
            if (isFixedstatus()) {
                confirm = MessageDialog.openConfirm(getShell(), Messages.getString("VersionManagementDialog.ConfirmTitle"), //$NON-NLS-1$
                        Messages.getString("VersionManagementDialog.ConfirmMessage", newStatus)); //$NON-NLS-1$
                if (confirm) {
                    // set all items for new version
                    for (RepositoryObject object : getModifiedVersionItems()) {
                        if (techinalButton.getSelection()) {
                            ERepositoryObjectType type = object.getRepositoryNode().getContentType();
                            if (!type.equals(ERepositoryObjectType.DOCUMENTATION)
                                    && !type.equals(ERepositoryObjectType.BUSINESS_PROCESS)) {
                                object.setStatusCode(newStatus);
                            }
                        } else {
                            ERepositoryObjectType type = object.getRepositoryNode().getContentType();
                            if (type.equals(ERepositoryObjectType.DOCUMENTATION)
                                    || type.equals(ERepositoryObjectType.BUSINESS_PROCESS)) {
                                object.setStatusCode(newStatus);
                            }
                        }

                    }
                }
            } else {
                StatusConfirmSettingDialog chanedDialog = new StatusConfirmSettingDialog(getShell(), getModifiedVersionItems(),
                        statusHelper);
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
        IRunnableWithProgress runnable = new IRunnableWithProgress() {

            @Override
            public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
                monitor.beginTask("", getModifiedVersionItems().size() * 100); //$NON-NLS-1$
                Set<ERepositoryObjectType> types = new HashSet<ERepositoryObjectType>();
                for (RepositoryObject object : getModifiedVersionItems()) {

                    // IRepositoryViewObject repositoryObject = object.getRepositoryNode().getObject();
                    if (object != null && object.getProperty() != null) {
                        if (object.getStatusCode().equals(object.getProperty().getStatusCode())) {
                            final Item item = object.getProperty().getItem();
                            item.getProperty().setOldStatusCode(object.getStatusCode());
                            // types.add(object.getRepositoryNode().getObjectType());
                            try {
                                // for bug 12853 ,version management doesn't work for joblet because eResource is null
                                // IRepositoryViewObject obj = null;
                                // if (item.getProperty().eResource() == null) {
                                // ItemState state = item.getState();
                                // if (state != null && state.getPath() != null) {
                                // obj = FACTORY.getLastVersion(project, item.getProperty().getId(), state.getPath(),
                                // object
                                // .getRepositoryNode().getObjectType());
                                // } else {
                                // obj = FACTORY.getLastVersion(project, item.getProperty().getId());
                                // }
                                // }
                                if (object != null) {
                                    // obj.setVersion(object.getNewVersion());
                                    FACTORY.save(project, object.getProperty());
                                } else {
                                    // FACTORY.save(project, item.getProperty());
                                }
                            } catch (PersistenceException e) {
                                ExceptionHandler.process(e);
                            }
                        }
                    }
                    monitor.worked(100);
                }
                // RepositoryManager.refresh(types);
                monitor.done();
            }
        };

        // final ProgressMonitorJobsDialog dialog = new ProgressMonitorJobsDialog(null);
        final ProgressMonitorDialog dialog = new ProgressMonitorDialog(null);
        try {
            dialog.run(false, false, runnable);
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
