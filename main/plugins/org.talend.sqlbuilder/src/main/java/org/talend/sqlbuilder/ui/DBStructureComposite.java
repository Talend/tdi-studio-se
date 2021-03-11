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
package org.talend.sqlbuilder.ui;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeViewerListener;
import org.eclipse.jface.viewers.TreeExpansionEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.actions.SelectionProviderAction;
import org.talend.commons.ui.runtime.image.EImage;
import org.talend.commons.ui.runtime.image.ImageProvider;
import org.talend.core.model.properties.DatabaseConnectionItem;
import org.talend.core.model.repository.IRepositoryViewObject;
import org.talend.core.sqlbuilder.util.ConnectionParameters;
import org.talend.repository.RepositoryChangedEvent;
import org.talend.repository.model.IRepositoryNode;
import org.talend.repository.model.IRepositoryNode.ENodeType;
import org.talend.repository.model.IRepositoryNode.EProperties;
import org.talend.repository.model.RepositoryNode;
import org.talend.sqlbuilder.Messages;
import org.talend.sqlbuilder.actions.DeleteQueryAction;
import org.talend.sqlbuilder.actions.GenerateSelectSQLAction;
import org.talend.sqlbuilder.actions.MetadataRefreshAction;
import org.talend.sqlbuilder.actions.OpenNewEditorAction;
import org.talend.sqlbuilder.actions.OpenQueryAction;
import org.talend.sqlbuilder.actions.ShowQueryPropertyAction;
import org.talend.sqlbuilder.dbstructure.DBTreeProvider;
import org.talend.sqlbuilder.dbstructure.RepositoryNodeType;
import org.talend.sqlbuilder.dbstructure.SqlBuilderRepositoryObject;
import org.talend.sqlbuilder.repository.utility.SQLBuilderRepositoryNodeManager;
import org.talend.sqlbuilder.util.UIUtils;

/**
 * Detailled comment for this class. <br/>
 * $Id: DBStructureComposite.java,v 1.40 2006/11/09 07:29:18 peiqin.hou Exp $
 *
 * @author phou
 *
 */

public class DBStructureComposite extends Composite {

    private Action doubleClickAction;

    private static final int COLUMN_REPOSITORY_WIDTH = 100;

    private static final int COLUMN_DATABASE_WIDTH = 170;

    private TreeViewer treeViewer;

    private SQLBuilderDialog builderDialog;

    private IProgressMonitor progressMonitor;

    private boolean isShowAllConnections;

    private Action openNewEditorAction;

    private Action refreshAllConnectionsAction;

    private Action refreshConnectionAction;

    private Action listAllConnectionsAction;

    private Action metadataRefreshAction;

    private Action generateSelectAction;

    private ShowQueryPropertyAction showQueryPropertyAction;

    private OpenQueryAction openQueryAction;

    private DeleteQueryAction deleteQueryAction;

    private Separator separator = new Separator(IWorkbenchActionConstants.MB_ADDITIONS);

    private SQLBuilderRepositoryNodeManager repositoryNodeManager = new SQLBuilderRepositoryNodeManager();

    private DBTreeProvider treeLabelProvider;

    /**
     * Create the composite.
     *
     * @param parent parent
     * @param style style
     */
    public DBStructureComposite(Composite parent, int style) {
        super(parent, style);
        GridLayout layout = new GridLayout();
        layout.numColumns = 2;
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        layout.marginTop = 0;
        layout.verticalSpacing = 0;
        layout.horizontalSpacing = 0;
        setLayout(layout);
        //
    }

    /**
     * @return ProgressMonitor
     */
    public IProgressMonitor getProgressMonitor() {
        return progressMonitor;
    }

    /**
     * @param progressMonitor ProgressMonitor.
     */
    public void setProgressMonitor(IProgressMonitor progressMonitor) {
        this.progressMonitor = progressMonitor;
    }

    public DBStructureComposite(SashForm sashFormStructureAndEditor, int none, SQLBuilderDialog dialog) {
        this(sashFormStructureAndEditor, none);
        this.builderDialog = dialog;

        createToolbar();
        createDBTree();
        makeActions();
    }

    private void hookDoubleClickAction() {
        treeViewer.addDoubleClickListener(new IDoubleClickListener() {

            @Override
            public void doubleClick(DoubleClickEvent event) {
                doubleClickAction.run();
            }
        });
    }

    public void openNewEditor() {
        treeViewer.getTree().setSelection(treeViewer.getTree().getItem(0));
        Action tempOpenNewEditorAction = new OpenNewEditorAction(treeViewer, builderDialog, builderDialog.getConnParameters(),
                false);
        tempOpenNewEditorAction.run();
        treeViewer.getTree().deselectAll();
    }

    private List<RepositoryNode> expandNodes;

    /**
     * qzhang Comment method "openNewQueryEditor".
     */
    public void openNewQueryEditor() {
        final ConnectionParameters connParameters = builderDialog.getConnParameters();
        final RepositoryNode selectQuery = this.treeLabelProvider.getSelectedExtReposiotryNode();
        expandNodes = new ArrayList<RepositoryNode>();
        getNeedExpandedNodes(selectQuery);
        if (expandNodes.size() > 0) {
            treeViewer.setExpandedElements(expandNodes.toArray(new Object[0]));
        }
        if (treeViewer.getTree().getItemCount() > 0) {
            treeViewer.getTree().setSelection(treeViewer.getTree().getItem(0));
        }
        Action tempOpenNewEditorAction = new OpenNewEditorAction(treeViewer, builderDialog, connParameters, true);
        tempOpenNewEditorAction.run();
    }

    /**
     * yzhang Comment method "openNewTableEditor".
     */
    public void openNewTableEditor() {
        final RepositoryNode selectedMetadataTableNode = this.treeLabelProvider.getSelectedExtReposiotryNode();
        ((GenerateSelectSQLAction) generateSelectAction).setSelectedNode(selectedMetadataTableNode);
        generateSelectAction.run();
    }

    /**
     * qzhang Comment method "getNeedExpandedNodes".
     *
     * @return
     */
    private boolean getNeedExpandedNodes(RepositoryNode selectQuery) {
        expandNodes.add(selectQuery);
        final RepositoryNode parent2 = selectQuery.getParent();
        if (parent2 != null) {
            return getNeedExpandedNodes(parent2);
        } else {
            return false;
        }
    }

    /**
     * Updates the structure view to reflect the change of Repository View.
     *
     * @see SQLBuilderDialog.repositoryChanged(RepositoryChangedEvent event)
     * @param event
     */
    protected void updateStructureView(RepositoryChangedEvent event) {
        if (!isShowAllConnections) {
            DatabaseConnectionItem originalConnection = getDisplayedConnection();
            IRepositoryViewObject repositoryObject = event.getDelta().getRepositoryObject();
            if (repositoryObject != null) {
                DatabaseConnectionItem newConnection = (DatabaseConnectionItem) repositoryObject.getProperty().getItem();

                if (!originalConnection.getProperty().getId().equals(newConnection.getProperty().getId())) {
                    return;
                }
                if (!originalConnection.getProperty().getLabel().equals(newConnection.getProperty().getLabel())) {
                    String newRepositoryName = newConnection.getProperty().getLabel();
                    this.builderDialog.getConnParameters().setRepositoryName(newRepositoryName);
                    this.builderDialog.getConnParameters().setRepositoryId(newConnection.getProperty().getId());
                }
            }
        }
        ((DBTreeProvider) treeViewer.getContentProvider()).setInitialized(false);
        treeViewer.setInput(new RepositoryNode(null, null, ENodeType.SYSTEM_FOLDER));
        doExpand();
    }

    private DatabaseConnectionItem getDisplayedConnection() {
        List<RepositoryNode> list = repositoryNodeManager.getAllDisplayedConnection();

        if (list.isEmpty()) {
            throw new RuntimeException(Messages.getString("DBStructureComposite.exceptionMessage")); //$NON-NLS-1$
        }
        RepositoryNode node = list.get(0);

        IRepositoryViewObject o = node.getObject();
        DatabaseConnectionItem item = (DatabaseConnectionItem) o.getProperty().getItem();
        return item;

    }

    /**
     * Create contents of the dialog.
     */
    protected void createDBTree() {
        treeViewer = new TreeViewer(this, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION);
        //
        treeViewer.getControl().setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, true, 2, 1));
        treeViewer.setUseHashlookup(true);

        Tree tree = treeViewer.getTree();
        TreeColumn database = new TreeColumn(tree, SWT.LEFT);
        database.setText(Messages.getString("DBStructureComposite.Databases")); //$NON-NLS-1$
        database.setWidth(COLUMN_DATABASE_WIDTH);

        TreeColumn repository = new TreeColumn(tree, SWT.LEFT);
        repository.setText(Messages.getString("DBStructureComposite.Repository")); //$NON-NLS-1$
        repository.setWidth(COLUMN_REPOSITORY_WIDTH);

        TreeColumn diff = new TreeColumn(tree, SWT.LEFT);
        diff.setText("Diff"); //$NON-NLS-1$
        diff.setWidth(30);

        tree.setHeaderVisible(true);

        treeLabelProvider = new DBTreeProvider(this, builderDialog.getConnParameters());
        treeViewer.setContentProvider(treeLabelProvider);
        treeViewer.setLabelProvider(treeLabelProvider);
        treeViewer.addFilter(filter);
        treeViewer.setInput(new RepositoryNode(null, null, ENodeType.SYSTEM_FOLDER));
        doExpand();
        treeViewer.setSorter(new DBTreeViewerSorter());
        treeViewer.addTreeListener(new ITreeViewerListener() {

            @Override
            public void treeCollapsed(TreeExpansionEvent event) {
                // doSetColorOrNot(event);
            }

            @Override
            public void treeExpanded(TreeExpansionEvent event) {
                // doSetColorOrNot(event);
            }

        });
        hookDoubleClickAction();
        addContextMenu();

    }

    // see bug 8621:direct refresh Db metadata in sqlbuilder with folder.
    private void doExpand() {
        treeViewer.expandToLevel(2);
        treeViewer.collapseAll();
    }

    private ViewerFilter filter = new ViewerFilter() {

        private List<String> names = new ArrayList<String>();

        @Override
        public boolean select(Viewer viewer, Object parentElement, Object element) {
            if (isShowAllConnections) {
                return true;
            } else {
                removeOtherRepositoryNodeFromCache(builderDialog.getConnParameters());
            }

            RepositoryNode node = (RepositoryNode) element;
            final String repositoryName = builderDialog.getConnParameters().getRepositoryName();
            if (node.getProperties(EProperties.CONTENT_TYPE) == RepositoryNodeType.FOLDER) {
                names.clear();
                getRepositoryNodeNames(node);
                return names.contains(repositoryName);
            } else if (node.getProperties(EProperties.CONTENT_TYPE) == RepositoryNodeType.DATABASE) {
                if (builderDialog.getConnParameters().isRepository()) {
                    if (node.getProperties(RepositoryNode.EProperties.LABEL).equals(repositoryName)) {
                        return true;
                    }
                } else {
                    if (node.getProperties(EProperties.LABEL) == null || node.getProperties(EProperties.LABEL).equals("")) { //$NON-NLS-1$
                        return true;
                    }
                }
            } else {
                return true;
            }
            return false;
        }

        private void removeOtherRepositoryNodeFromCache(ConnectionParameters connectionParameters) {
            if (connectionParameters.isRepository()) {
                repositoryNodeManager.removeRepositoryNodeExceptNodeByName(connectionParameters.getRepositoryName());
            } else {
                repositoryNodeManager.removeRepositoryNodeExceptNodeByName("Built-In"); //$NON-NLS-1$
            }
        }

        private void getRepositoryNodeNames(RepositoryNode folderNode) {
            List<IRepositoryNode> nodes = folderNode.getChildren();
            for (IRepositoryNode node : nodes) {
                if (node.getProperties(EProperties.CONTENT_TYPE) == RepositoryNodeType.FOLDER) {
                    getRepositoryNodeNames((RepositoryNode) node);
                } else if (node.getProperties(EProperties.CONTENT_TYPE) == RepositoryNodeType.DATABASE) {
                    names.add(node.getObject().getLabel());
                }
            }
        }
    };

    /**
     * qianbing Comment method "makeActions". Makes actions
     */
    private void makeActions() {
        generateSelectAction = new GenerateSelectSQLAction(treeViewer, builderDialog, false);

        openNewEditorAction = new OpenNewEditorAction(treeViewer, builderDialog, builderDialog.getConnParameters(), false);

        refreshConnectionAction = new RefreshConnectionAction(treeViewer, Messages.getString("DBStructureComposite.Refresh")); //$NON-NLS-1$

        metadataRefreshAction = new MetadataRefreshAction(treeViewer, builderDialog);

        openQueryAction = new OpenQueryAction(treeViewer, builderDialog, builderDialog.getConnParameters());
        deleteQueryAction = new DeleteQueryAction(treeViewer, builderDialog);
        showQueryPropertyAction = new ShowQueryPropertyAction(treeViewer, builderDialog);
        doubleClickAction = new OpenQueryAction(treeViewer, builderDialog, builderDialog.getConnParameters());
    }

    /**
     * Add context menu.
     *
     * method description.
     *
     * @param
     * @return
     * @exception
     */
    private void addContextMenu() {
        MenuManager menuMgr = new MenuManager("Menu"); //$NON-NLS-1$
        menuMgr.setRemoveAllWhenShown(true);
        menuMgr.addMenuListener(new IMenuListener() {

            @Override
            public void menuAboutToShow(IMenuManager manager) {
                fillContextMenu(manager);
            }

            @SuppressWarnings("unchecked")
            private void fillContextMenu(IMenuManager manager) {
                // GenerateSelectSQL
                manager.add(generateSelectAction);

                // open editor
                builderDialog.getConnParameters().setQueryObject(null);
                manager.add(openNewEditorAction);

                // Separator
                manager.add(separator);

                // refresh

                ((RefreshConnectionAction) refreshConnectionAction).init();
                manager.add(refreshConnectionAction);
                manager.add(separator);

                // metadata refresh
                manager.add(metadataRefreshAction);
                manager.add(separator);

                manager.add(openQueryAction);
                manager.add(deleteQueryAction);
                manager.add(showQueryPropertyAction);
            }
        });
        Menu contextMenu = menuMgr.createContextMenu(treeViewer.getTree());
        treeViewer.getTree().setMenu(contextMenu);
    }

    /**
     * create toobar.
     */
    private void createToolbar() {
        Label label = new Label(this, SWT.NONE);
        label.setText("  " + Messages.getString("DBStructureComposite.DatabaseStructure")); //$NON-NLS-1$ //$NON-NLS-2$
        label.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING));
        ToolBarManager toolBarMgr = new ToolBarManager(SWT.FLAT);
        toolBarMgr.createControl(this);
        if (listAllConnectionsAction == null) {
            listAllConnectionsAction = new ListAllConnectionAction(SWT.TOGGLE);
        }
        toolBarMgr.add(listAllConnectionsAction);
        if (refreshAllConnectionsAction == null) {
            refreshAllConnectionsAction = new RefreshAllConnectionAction();
        }
        toolBarMgr.add(refreshAllConnectionsAction);
        toolBarMgr.update(true);
        toolBarMgr.getControl().setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));
    }

    /**
     * RefreshAction.
     */
    private class ListAllConnectionAction extends Action {

        public ListAllConnectionAction(int style) {
            super("", style); //$NON-NLS-1$
            setImageDescriptor(ImageProvider.getImageDesc(EImage.HIERARCHY_ICON));
            setToolTipText(Messages.getString("DBStructureComposite.ShowAllConnections")); //$NON-NLS-1$
        }

        @Override
        public void run() {
            final IRunnableWithProgress r = new IRunnableWithProgress() {

                @Override
                public void run(final IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
                    monitor.beginTask(Messages.getString("DBStructureComposite.RefreshConnections"), -1); //$NON-NLS-1$

                    if (isChecked()) {
                        isShowAllConnections = true;
                        repositoryNodeManager.addAllRepositoryNodes();
                    } else {
                        isShowAllConnections = false;
                    }

                    try {
                        Display.getDefault().asyncExec(new Runnable() {

                            @Override
                            public void run() {
                                treeViewer.refresh();
                            }
                        });
                    } finally {
                        monitor.done();
                    }
                }
            };

            UIUtils.runWithProgress(r, true, getProgressMonitor(), getShell());
        }
    }

    /**
     * RefreshAction.
     */
    private class RefreshAllConnectionAction extends Action {

        public RefreshAllConnectionAction() {
            super(""); //$NON-NLS-1$
            setImageDescriptor(ImageProvider.getImageDesc(EImage.REFRESH_ICON));
            setToolTipText(Messages.getString("DBStructureComposite.Refresh")); //$NON-NLS-1$
        }

        @Override
        public void run() {
            //            if (!MessageDialog.openConfirm(getShell(), Messages.getString("DBStructureComposite.Refresh"), Messages //$NON-NLS-1$
            //                    .getString("DBStructureComposite.TakeALongTime"))) { //$NON-NLS-1$
            // return;
            // }
            final IRunnableWithProgress r = new IRunnableWithProgress() {

                @Override
                public void run(final IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
                    monitor.beginTask(Messages.getString("DBStructureComposite.RefreshConnections"), -1); //$NON-NLS-1$

                    try {
                        RepositoryNode root = (RepositoryNode) treeViewer.getInput();
                        refreshChildren(null);
                    } finally {
                        monitor.done();
                    }

                }

            };

            UIUtils.runWithProgress(r, true, getProgressMonitor(), getShell());
        }
    }

    private void refreshChildren(RepositoryNode root) {
        if (root == null) {
            List<RepositoryNode> dnodes = repositoryNodeManager.getAllDisplayedConnection();
            for (RepositoryNode node : dnodes) {
                doRefresh(repositoryNodeManager.getRepositoryNodeFromDB(node, builderDialog.getSelectedContext()));
            }
        } else {
            List<IRepositoryNode> repositoryNodes = root.getChildren();
            for (IRepositoryNode node : repositoryNodes) {
                if (node.getProperties(EProperties.CONTENT_TYPE) == RepositoryNodeType.FOLDER) {
                    refreshChildren((RepositoryNode) node);
                } else {
                    doRefresh(repositoryNodeManager.getRepositoryNodeFromDB((RepositoryNode) node,
                            builderDialog.getSelectedContext()));
                }
            }
        }
    }

    /**
     * Refresh RepositoryNode.
     *
     * @param
     * @return
     * @exception
     */
    public void doRefresh(final RepositoryNode refreshNode) {
        final RepositoryNode rootNode = SQLBuilderRepositoryNodeManager.getRoot(refreshNode);
        Display.getDefault().asyncExec(new Runnable() {

            @Override
            public void run() {
                if (treeViewer != null && treeViewer.getTree() != null && !treeViewer.getTree().isDisposed()) {
                    ((DBTreeProvider) treeViewer.getContentProvider()).setRefresh(true);
                    treeViewer.refresh(rootNode, true);
                    ((DBTreeProvider) treeViewer.getLabelProvider()).refreshRootNode(rootNode);
                    treeViewer.update(rootNode, null);
                }
            }

        });

    }

    /**
     * RefreshAction.
     */
    private class RefreshConnectionAction extends SelectionProviderAction {

        protected RefreshConnectionAction(ISelectionProvider provider, String text) {
            super(provider, text);
            setImageDescriptor(ImageProvider.getImageDesc(EImage.REFRESH_ICON));
            setToolTipText(Messages.getString("Refresh")); //$NON-NLS-1$
            init();
        }

        @Override
        public void selectionChanged(ISelection selection) {
            init();
        }

        @SuppressWarnings("unchecked")
        public void init() {
            IStructuredSelection selection = (IStructuredSelection) getSelectionProvider().getSelection();
            if (selection.isEmpty()) {
                this.setEnabled(false);
                return;
            }
            boolean flag = false;
            for (Object object : selection.toList()) {
                if (!"".equals(((RepositoryNode) object).getObject().getLabel())) { //$NON-NLS-1$
                    flag = true;
                }
            }
            this.setEnabled(flag);
        }

        @Override
        public void run() {
            final IStructuredSelection selection = (IStructuredSelection) treeViewer.getSelection();
            final IRunnableWithProgress r = new IRunnableWithProgress() {

                @Override
                @SuppressWarnings("unchecked")
                public void run(final IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
                    monitor.beginTask(Messages.getString("DBStructureComposite.RefreshConnections"), -1); //$NON-NLS-1$
                    try {
                        RepositoryNode[] nodes = (RepositoryNode[]) selection.toList().toArray(new RepositoryNode[] {});

                        nodes = retrieveFromDB(nodes);
                    } finally {
                        monitor.done();
                    }
                }

                private RepositoryNode[] retrieveFromDB(RepositoryNode[] nodes) {
                    for (RepositoryNode node : nodes) {
                        if (node.getProperties(EProperties.CONTENT_TYPE) == RepositoryNodeType.FOLDER) {
                            refreshChildren(node);
                        }
                        if ("".equals(node.getObject().getLabel())) { //$NON-NLS-1$
                            continue;
                        }
                        node = repositoryNodeManager.getRepositoryNodeFromDB(node, builderDialog.getSelectedContext());
                        doRefresh(node);
                    }
                    return null;
                }

            };

            UIUtils.runWithProgress(r, true, getProgressMonitor(), getShell());
        }

        protected RepositoryNode getConnectionNode(RepositoryNode node) {
            if (node.getProperties(EProperties.CONTENT_TYPE) == RepositoryNodeType.DATABASE) {
                return node;
            }
            return getConnectionNode(node.getParent());
        }
    }

    /**
     * Close the connection.
     *
     * @Override
     */
    @Override
    public void dispose() {
        super.dispose();
    }

    /**
     * checkSubclass.
     */
    @Override
    protected void checkSubclass() {
    }

    /**
     * @return TreeViewer.s
     */
    public TreeViewer getTreeViewer() {
        return treeViewer;
    }

    public boolean isShowAllConnections() {
        return this.isShowAllConnections;
    }

    public DBTreeProvider getTreeLabelProvider() {
        return this.treeLabelProvider;
    }

    /**
     * Changed by Marvin Wang on Feb.23, 2012 for bug TDI-19196 to sort by the display source name.
     */
    class DBTreeViewerSorter extends ViewerSorter {

        @Override
        public int compare(Viewer viewer, Object e1, Object e2) {
            int comparedResult = 0;
            if (e1 instanceof IRepositoryNode && e2 instanceof IRepositoryNode) {
                IRepositoryNode t1 = (IRepositoryNode) e1;
                IRepositoryNode t2 = (IRepositoryNode) e2;
                IRepositoryViewObject repViewObj1 = t1.getObject();
                IRepositoryViewObject repViewObj2 = t2.getObject();
                if (repViewObj1 instanceof SqlBuilderRepositoryObject && repViewObj2 instanceof SqlBuilderRepositoryObject) {
                    SqlBuilderRepositoryObject repositoryObject1 = (SqlBuilderRepositoryObject) repViewObj1;
                    SqlBuilderRepositoryObject repositoryObject2 = (SqlBuilderRepositoryObject) repViewObj2;

                    String sourceName1 = repositoryObject1.getSourceName();
                    String sourceName2 = repositoryObject2.getSourceName();

                    comparedResult = sourceName1.compareToIgnoreCase(sourceName2);
                }
            }
            return comparedResult;
        }
    }
}
