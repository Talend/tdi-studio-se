package org.talend.repository.preference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.ITreeViewerListener;
import org.eclipse.jface.viewers.TreeExpansionEvent;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.internal.navigator.NavigatorContentServiceContentProvider;
import org.talend.commons.exception.ExceptionHandler;
import org.talend.commons.exception.PersistenceException;
import org.talend.commons.ui.runtime.image.EImage;
import org.talend.commons.ui.runtime.image.IImage;
import org.talend.commons.ui.runtime.image.ImageProvider;
import org.talend.commons.ui.swt.advanced.composite.ThreeCompositesSashForm;
import org.talend.core.CorePlugin;
import org.talend.core.model.general.Project;
import org.talend.core.model.properties.Item;
import org.talend.core.model.properties.Property;
import org.talend.core.model.relationship.Relation;
import org.talend.core.model.relationship.RelationshipItemBuilder;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.model.repository.IRepositoryViewObject;
import org.talend.core.repository.model.ProjectRepositoryNode;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.repository.ProjectManager;
import org.talend.repository.i18n.Messages;
import org.talend.repository.model.IProxyRepositoryFactory;
import org.talend.repository.model.IRepositoryNode;
import org.talend.repository.model.IRepositoryNode.ENodeType;
import org.talend.repository.model.ItemVersionObject;
import org.talend.repository.model.RepositoryNode;
import org.talend.repository.model.nodes.IProjectRepositoryNode;
import org.talend.repository.ui.dialog.ItemsVersionConfirmDialog;
import org.talend.repository.utils.MavenVersionUtils;
import org.talend.repository.viewer.ui.provider.RepoCommonViewerProvider;
import org.talend.repository.viewer.ui.viewer.CheckboxRepositoryTreeViewer;

public abstract class AbstractVersionManagementProjectSettingPage extends ProjectSettingPage {

    protected static final String ITEM_EDITOR_KEY = "ITEM_EDITOR_KEY"; //$NON-NLS-1$

    protected static final IProxyRepositoryFactory FACTORY = CorePlugin.getDefault().getProxyRepositoryFactory();

    protected Project project = ProjectManager.getInstance().getCurrentProject();

    protected RelationshipItemBuilder builder = RelationshipItemBuilder.getInstance();

    protected boolean isApplied;

    protected CheckboxRepositoryTreeViewer treeViewer;

    protected Button removeBtn;

    protected Table itemTable;

    protected Button fixedVersionButton;

    protected Text fixedVersionText;

    protected Button alldependcies;

    protected Button subjobs;

    protected Button eachVersionButton;

    private Map<IImage, Image> cacheItemImages = new HashMap<IImage, Image>();

    protected List<ItemVersionObject> versionObjects = new ArrayList<ItemVersionObject>();

    protected List<ItemVersionObject> checkedObjects = new ArrayList<ItemVersionObject>();

    protected ICheckStateListener checkStateListener = new ICheckStateListener() {

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
                if (node != null && MavenVersionUtils.isVersioningType(node.getContentType())) {
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
    };

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

        addRepositoryTreeViewer(compositesSachForm.getLeftComposite());

        addButtons(compositesSachForm.getMidComposite());

        addItemTableViewer(compositesSachForm.getRightComposite());

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

        IProjectRepositoryNode projectRepositoryNode = provider.getProjectRepositoryNode();
        processItems(versionObjects, (RepositoryNode) projectRepositoryNode);
        addMenuAction(treeViewer.getTree());
        // filter
        treeViewer.addFilter(new ViewerFilter() {

            @Override
            public boolean select(Viewer viewer, Object parentElement, Object element) {
                RepositoryNode node = (RepositoryNode) element;
                return filterRepositoryNode(node);
            }
        });
        // event
        treeViewer.addCheckStateListener(checkStateListener);
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

    protected void refreshCheckedTreeView() {
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

    protected void addItemTableViewer(Composite rightComposite) {

        Composite composite = new Composite(rightComposite, SWT.NONE);
        composite.setLayout(new GridLayout());
        GridDataFactory.fillDefaults().grab(true, true).applyTo(composite);

        addItemsOption(composite);

        itemTable = new Table(composite, SWT.MULTI | SWT.BORDER);
        GridDataFactory.fillDefaults().grab(true, true).applyTo(itemTable);
        itemTable.setHeaderVisible(true);
        itemTable.setLinesVisible(true);

        createItemTableColumns();

    }

    abstract protected boolean filterRepositoryNode(RepositoryNode node);

    abstract protected void addItemsOption(Composite composite);

    abstract protected void createItemTableColumns();

    abstract protected void addItemElements(List<ItemVersionObject> checkedObjects);

    abstract protected ItemVersionObject createItemVersionObject(RepositoryNode node, Property property);

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

    protected void addMenuAction(Tree tree) {
        MenuManager manager = new MenuManager();
        manager.add(new SelectAllAction());
        Menu menu = manager.createContextMenu(tree);
        tree.setMenu(menu);
    }

    protected void checkButtonsState(boolean isEnable) {
        boolean flag = isEnable;
        TableItem[] items = itemTable.getItems();
        for (TableItem item : items) {
            if (item.getData() instanceof ItemVersionObject) {
                ItemVersionObject itemVersionObj = (ItemVersionObject) item.getData();
                RepositoryNode repositoryNode = itemVersionObj.getRepositoryNode();
                if (repositoryNode != null) {
                    ERepositoryObjectType type = repositoryNode.getContentType();
                    if (MavenVersionUtils.isVersioningType(type)) {
                        flag = true;
                        break;
                    }
                }
            }
        }
        if (alldependcies != null) {
            alldependcies.setEnabled(flag);
        }
        if (subjobs != null) {
            subjobs.setEnabled(flag);
        }

    }

    protected void selectSubjob() {
        List<ItemVersionObject> jobList = new ArrayList<ItemVersionObject>();

        for (ItemVersionObject object : checkedObjects) {
            if (MavenVersionUtils.isHasSubjobType(ERepositoryObjectType.getItemType(object.getItem()))) {
                jobList.add(object);
            }
        }

        for (ItemVersionObject object : jobList) {
            if (object.getRepositoryNode() != null) {
                List<Relation> relations = builder.getItemsJobRelatedTo(object.getRepositoryNode().getId(),
                        object.getItem().getProperty().getVersion(), RelationshipItemBuilder.JOB_RELATION);
                for (Relation relation : relations) {
                    try {
                        IRepositoryViewObject obj = FACTORY.getLastVersion(relation.getId());
                        if (obj != null) {
                            for (ItemVersionObject obj2 : versionObjects) {
                                if (obj2.getItem() == obj.getProperty().getItem()) {
                                    ItemVersionObject relat = obj2;
                                    if (!checkedObjects.contains(relat)) {
                                        checkedObjects.add(relat);
                                        checkAllVerSionLatest(checkedObjects, relat);
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
        refreshTableItems();
        refreshCheckedTreeView();
    }

    protected void checkAllVerSionLatest(List<ItemVersionObject> tableList, ItemVersionObject object) {
        if (object.getRepositoryNode() != null) {
            List<Relation> relations = builder.getItemsJobRelatedTo(object.getRepositoryNode().getId(),
                    object.getItem().getProperty().getVersion(), RelationshipItemBuilder.JOB_RELATION);
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

    protected void checkButtonsState() {
        TableItem[] selections = itemTable.getSelection();
        if (selections != null && selections.length > 0) {
            removeBtn.setEnabled(true);
        } else {
            removeBtn.setEnabled(false);
        }
    }

    protected void removeItemElements(List<ItemVersionObject> objects) {
        itemTable.setRedraw(false);
        TableItem[] items = itemTable.getItems();
        for (TableItem item : items) {
            if (objects.contains(item.getData())) {
                removeTableItem(item);
            }
        }
        itemTable.setRedraw(true);
    }

    protected void removeTableItem(TableItem item) {
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

    protected void refreshTableItems() {
        removeItemElements(checkedObjects);
        addItemElements(checkedObjects);
        // if (checkedObjects.isEmpty()) {
        // maxVersionText.setText(VersionUtils.DEFAULT_VERSION);
        // }
        checkButtonsState();
    }

    protected Image getItemsImage(IImage iImage) {
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

    protected void processItems(List<ItemVersionObject> objects, RepositoryNode node) {
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
                    ItemVersionObject object = createItemVersionObject(node, property);
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

    @Override
    protected void performApply() {
        super.performApply();
        isApplied = true;
    }

    @Override
    public boolean performOk() {
        okPressed();
        return super.performOk();
    }

    protected void okPressed() {
        if (fixedVersionText == null) {
            return;
        }
        boolean modified = false;
        String newVersion = null;
        for (ItemVersionObject object : checkedObjects) {
            newVersion = getNewVersionWithOption(object);
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
            if (fixedVersionButton.getSelection()) {
                confirm = MessageDialog.openConfirm(getShell(), Messages.getString("VersionManagementDialog.ConfirmTitle"), //$NON-NLS-1$
                        Messages.getString("VersionManagementDialog.ConfirmMessage", newVersion)); //$NON-NLS-1$
                if (confirm && newVersion != null) {
                    // set all items for new version
                    for (ItemVersionObject object : checkedObjects) {
                        object.setNewVersion(newVersion);
                    }
                }
            } else {
                ItemsVersionConfirmDialog chanedDialog = new ItemsVersionConfirmDialog(getShell(), checkedObjects);
                confirm = (chanedDialog.open() == Window.OK);
            }

            if (confirm) {
                updateItemsVersion();
            }
        } else {
            if (!checkedObjects.isEmpty() && !isApplied) {
                MessageDialog.openWarning(getShell(), Messages.getString("VersionManagementDialog.WarningTitle"), //$NON-NLS-1$
                        Messages.getString("VersionManagementDialog.WarningMessages")); //$NON-NLS-1$
            }
        }

    }

    abstract protected void updateItemsVersion();

    abstract protected String getNewVersionWithOption(ItemVersionObject object);

    @Override
    public void refresh() {
        // do nothing
    }

    protected void researchMaxVersion() {
        // do nothing
    }

    class SelectAllAction extends Action {

        public SelectAllAction() {
            this.setText(Messages.getString("VersionManagementDialog.selectAll")); //$NON-NLS-1$
        }

        @Override
        public void run() {
            treeViewer.removeCheckStateListener(checkStateListener);
            treeViewer.setAllChecked(true);
            treeViewer.addCheckStateListener(checkStateListener);
            checkedObjects.clear();
            checkedObjects.addAll(versionObjects);
            refreshTableItems();
            checkButtonsState(true);
        }

    }

}
