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
package org.talend.sqlbuilder.ui;

import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IProgressMonitorWithBlocking;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressIndicator;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.actions.SelectionProviderAction;
import org.talend.commons.ui.runtime.exception.ExceptionHandler;
import org.talend.core.CorePlugin;
import org.talend.core.database.EDatabase4DriverClassName;
import org.talend.core.database.EDatabaseTypeName;
import org.talend.core.model.context.ContextUtils;
import org.talend.core.model.metadata.IMetadataConnection;
import org.talend.core.model.metadata.builder.ConvertionHelper;
import org.talend.core.model.metadata.builder.connection.DatabaseConnection;
import org.talend.core.model.metadata.builder.connection.MetadataTable;
import org.talend.core.model.metadata.builder.connection.Query;
import org.talend.core.model.metadata.builder.database.DriverShim;
import org.talend.core.model.metadata.builder.database.ExtractMetaDataUtils;
import org.talend.core.model.process.EParameterFieldType;
import org.talend.core.model.properties.ConnectionItem;
import org.talend.core.model.properties.ContextItem;
import org.talend.core.model.properties.DatabaseConnectionItem;
import org.talend.core.model.properties.Item;
import org.talend.core.model.repository.IRepositoryViewObject;
import org.talend.core.model.repository.RepositoryElementDelta;
import org.talend.core.model.update.RepositoryUpdateManager;
import org.talend.core.model.utils.TalendTextUtils;
import org.talend.core.sqlbuilder.util.ConnectionParameters;
import org.talend.core.sqlbuilder.util.TextUtil;
import org.talend.cwm.helper.ConnectionHelper;
import org.talend.metadata.managment.ui.wizard.metadata.ContextSetsSelectionDialog;
import org.talend.repository.IRepositoryChangedListener;
import org.talend.repository.RepositoryChangedEvent;
import org.talend.repository.model.IRepositoryNode;
import org.talend.repository.model.IRepositoryNode.EProperties;
import org.talend.repository.model.RepositoryNode;
import org.talend.sqlbuilder.Messages;
import org.talend.sqlbuilder.SqlBuilderPlugin;
import org.talend.sqlbuilder.dbstructure.DBTreeProvider.MetadataTableRepositoryObject;
import org.talend.sqlbuilder.dbstructure.RepositoryNodeType;
import org.talend.sqlbuilder.dbstructure.SessionTreeNodeManager;
import org.talend.sqlbuilder.dbstructure.SessionTreeNodeUtils;
import org.talend.sqlbuilder.dbstructure.nodes.INode;
import org.talend.sqlbuilder.editors.MultiPageSqlBuilderEditor;
import org.talend.sqlbuilder.repository.utility.SQLBuilderRepositoryNodeManager;
import org.talend.sqlbuilder.sessiontree.model.SessionTreeNode;
import org.talend.sqlbuilder.util.UIUtils;
import org.talend.utils.sql.ConnectionUtils;

/**
 * This Dialog is used for building sql.
 *
 * $Id: SQLBuilderDialog.java,v 1.44 2006/11/09 08:44:09 tangfn Exp $
 *
 */
public class SQLBuilderDialog extends Dialog implements ISQLBuilderDialog, IRepositoryChangedListener {

    private Composite container;

    private boolean readOnly = false;

    // ends
    private boolean isFromRepositoryView = false;

    private DBDetailsComposite dbDetailsComposite;

    private DBStructureComposite structureComposite;

    private SQLBuilderTabComposite editorComposite;

    // Added by Tang Fengneng
    private ConnectionParameters connParameters;

    private RepositoryNode node;

    private RepositoryNode nodeInEditor;

    private DatabaseConnection connection;

    // Ends

    /**
     * The progress indicator control.
     */
    protected ProgressIndicator progressIndicator;

    /**
     * The progress monitor.
     */
    private ProgressMonitor progressMonitor = new ProgressMonitor();

    /**
     * SessionTreeNode Manager.
     */
    SessionTreeNodeManager nodeManager = new SessionTreeNodeManager();

    SQLBuilderRepositoryNodeManager manager = new SQLBuilderRepositoryNodeManager();

    private String selectedContext = null;

    /**
     * Internal progress monitor implementation.
     */
    private class ProgressMonitor implements IProgressMonitorWithBlocking {

        private boolean fIsCanceled;

        protected boolean forked = false;

        protected boolean locked = false;

        @Override
        public void beginTask(String name, int totalWork) {
            if (progressIndicator == null) {
                return;
            }
            if (progressIndicator.isDisposed()) {
                return;
            }
            if (totalWork == UNKNOWN) {
                progressIndicator.beginAnimatedTask();
            } else {
                progressIndicator.beginTask(totalWork);
            }
        }

        @Override
        public void done() {
            if (progressIndicator == null) {
                return;
            }
            if (!progressIndicator.isDisposed()) {
                progressIndicator.sendRemainingWork();
                progressIndicator.done();
            }
        }

        @Override
        public void setTaskName(String name) {
        }

        @Override
        public boolean isCanceled() {
            return fIsCanceled;
        }

        @Override
        public void setCanceled(boolean b) {
            fIsCanceled = b;
            if (locked) {
                clearBlocked();
            }
        }

        @Override
        public void subTask(String name) {
        }

        @Override
        public void worked(int work) {
            internalWorked(work);
        }

        @Override
        public void internalWorked(double work) {
            if (!progressIndicator.isDisposed()) {
                progressIndicator.worked(work);
            }
        }

        /*
         * (non-Javadoc)
         *
         * @see org.eclipse.core.runtime.IProgressMonitorWithBlocking#clearBlocked()
         */
        @Override
        public void clearBlocked() {
        }

        /*
         * (non-Javadoc)
         *
         * @see org.eclipse.core.runtime.IProgressMonitorWithBlocking#setBlocked(org.eclipse.core.runtime.IStatus)
         */
        @Override
        public void setBlocked(IStatus reason) {
            locked = true;
        }
    }

    private String dialogTitle;

    /**
     * Create the dialog.
     *
     * @param parentShell
     */
    public SQLBuilderDialog(Shell parentShell, String title) {
        this(parentShell);
    }

    public SQLBuilderDialog(Shell parentShell) {
        super(parentShell);
        setShellStyle(SWT.DIALOG_TRIM | SWT.RESIZE | SWT.RESIZE | SWT.MIN | SWT.MAX | SWT.APPLICATION_MODAL);
        SqlBuilderPlugin.getDefault().getRepositoryService().registerRepositoryChangedListener(this);
    }

    public SQLBuilderDialog(Shell parentShell, RepositoryNode node, String selectedContext) {
        super(parentShell);
        this.node = node;
        this.selectedContext = selectedContext;
        setShellStyle(SWT.DIALOG_TRIM | SWT.RESIZE | SWT.RESIZE | SWT.MIN | SWT.MAX | SWT.APPLICATION_MODAL);
        SqlBuilderPlugin.getDefault().getRepositoryService().registerRepositoryChangedListener(this);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.jface.window.Window#configureShell(org.eclipse.swt.widgets.Shell)
     */
    @Override
    public void configureShell(Shell shell) {
        super.configureShell(shell);
        // Set the title bar text
        shell.setText(TextUtil.getDialogTitle());
    }

    /**
     * Create contents of the dialog.
     *
     * @param parent
     */
    @Override
    protected Control createDialogArea(Composite parent) {
        container = (Composite) super.createDialogArea(parent);
        // container.setLayout(new GridLayout());

        final SashForm mainSashForm = new SashForm(container, SWT.NONE | SWT.VERTICAL);
        mainSashForm.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        final SashForm sashFormStructureAndEditor = new SashForm(mainSashForm, SWT.NONE);

        final SashForm sashFormResultAndDetail = new SashForm(mainSashForm, SWT.NONE);
        mainSashForm.setWeights(new int[] { 3, 1 });

        createResult(sashFormResultAndDetail);
        createDetail(sashFormResultAndDetail);
        sashFormResultAndDetail.setWeights(new int[] { 4, 3 });

        createDatabaseStructure(sashFormStructureAndEditor);
        createSQLEditor(sashFormStructureAndEditor);
        sashFormStructureAndEditor.setWeights(new int[] { 4, 6 });

        if (connParameters.isFromRepository() && connParameters.getQueryObject() != null) {
            structureComposite.openNewQueryEditor();
        } else if (connParameters.isFromRepository() && connParameters.getMetadataTable() != null) {
            structureComposite.openNewTableEditor();
        } else {
            structureComposite.openNewEditor();
        }

        // RefreshDetailCompositeAction refreshAction =
        new RefreshDetailCompositeAction(structureComposite.getTreeViewer(), this.getShell());

        return container;

    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.sqlbuilder.ui.ISQLBuilderDialog#openEditor(org.talend.repository.model.RepositoryNode,
     * java.util.List, org.talend.sqlbuilder.util.ConnectionParameters, boolean)
     */
    @Override
    public void openEditor(RepositoryNode node, List<String> repositoryName, ConnectionParameters connParam,
            boolean isDefaultEditor) {
        this.nodeInEditor = node;
        editorComposite.openNewEditor(node, repositoryName, connParam, isDefaultEditor);
    }

    /**
     * Creates the sql detail composite.
     *
     * @param sashFormResultAndDetail
     */
    private void createDetail(SashForm sashFormResultAndDetail) {
        dbDetailsComposite = new DBDetailsComposite(sashFormResultAndDetail, SWT.BORDER);
    }

    /**
     * Creates the composite to display sql execution result.
     *
     * @param sashFormResultAndDetail
     */
    private void createResult(SashForm sashFormResultAndDetail) {
        // SQLResultComposite resultView =
        new SQLResultComposite(sashFormResultAndDetail, SWT.BORDER);

    }

    /**
     * Creates the sql editor composite.
     *
     * @param sashFormStructureAndEditor
     */
    private void createSQLEditor(SashForm sashFormStructureAndEditor) {

        editorComposite = new SQLBuilderTabComposite(sashFormStructureAndEditor, SWT.BORDER, this);
        editorComposite.setReadOnly(readOnly);
    }

    /**
     * Creates composite to display database structure.
     *
     * @param sashFormStructureAndEditor
     */
    private void createDatabaseStructure(SashForm sashFormStructureAndEditor) {
        // if (connParameters.getMetadataTable() == null || (connParameters.isRepository() &&
        // connParameters.isSchemaRepository())) {
        // structureComposite = new DBStructureComposite(sashFormStructureAndEditor, SWT.BORDER, this);
        // } else {
        BuildInDBStructure buildInDBStructure = new BuildInDBStructure(sashFormStructureAndEditor, SWT.NONE | SWT.VERTICAL, this,
                connParameters);
        structureComposite = buildInDBStructure.getDbstructureCom();
        // }
        structureComposite.setProgressMonitor(this.getProgressMonitor());
    }

    /**
     * Create contents of the button bar.
     *
     * @param parent
     */
    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        GridData data = new GridData(GridData.FILL_HORIZONTAL);
        parent.setLayoutData(data);

        // increment the number of columns in the button bar
        GridLayout layout = (GridLayout) parent.getLayout();
        layout.makeColumnsEqualWidth = false;
        layout.numColumns = 4;

        GridData gd = new GridData(GridData.FILL_HORIZONTAL);
        progressIndicator = new ProgressIndicator(parent);
        progressIndicator.setLayoutData(gd);

        gd = new GridData();
        gd.widthHint = 200;
        Label l = new Label(parent, SWT.NONE);
        l.setLayoutData(gd);

        // OK and Cancel buttons
        createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, false);

        createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
    }

    @Override
    protected Button createButton(Composite parent, int id, String label, boolean defaultButton) {
        Button button = new Button(parent, SWT.PUSH);
        button.setText(label);
        button.setFont(JFaceResources.getDialogFont());
        button.setData(new Integer(id));
        button.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent event) {
                buttonPressed(((Integer) event.widget.getData()).intValue());
            }
        });
        if (defaultButton) {
            Shell shell = parent.getShell();
            if (shell != null) {
                shell.setDefaultButton(button);
            }
        }
        GridData data = new GridData(GridData.HORIZONTAL_ALIGN_END);
        int widthHint = convertHorizontalDLUsToPixels(IDialogConstants.BUTTON_WIDTH);
        Point minSize = button.computeSize(SWT.DEFAULT, SWT.DEFAULT, true);
        data.widthHint = Math.max(widthHint, minSize.x);
        button.setLayoutData(data);
        return button;
    }

    /**
     * Return the initial size of the dialog.
     */
    @Override
    protected Point getInitialSize() {
        return new Point(800, 600);
    }

    public SQLBuilderTabComposite getEditorComposite() {
        return editorComposite;
    }

    @Override
    public boolean close() {
        try {
            SqlBuilderPlugin.getDefault().getRepositoryService().removeRepositoryChangedListener(this);
            if (this.nodeInEditor != null) {
                RepositoryNode root = SQLBuilderRepositoryNodeManager.getRoot(this.nodeInEditor);
                if (root != null) {
                    DatabaseConnection connection = (DatabaseConnection) ((ConnectionItem) root.getObject().getProperty()
                            .getItem()).getConnection();
                    shutDownDb(connection);
                }
            }
            clean();
            SQLBuilderRepositoryNodeManager.removeAllRepositoryNodes();
        } catch (Exception e) {
            ExceptionHandler.process(e);
        } finally {
            super.close();
        }
        return true;
    }

    private void clean() {
        SessionTreeNodeUtils.dispose();
        nodeManager.clear();
    }

    private void shutDownDb(DatabaseConnection databaseConnection) {
        IMetadataConnection iMetadataConnection = null;
        if (selectedContext == null) {
            selectedContext = databaseConnection.getContextName();
        }
        iMetadataConnection = ConvertionHelper.convert(databaseConnection, false, selectedContext);
        String dbType = iMetadataConnection.getDbType();
        String driverClassName = iMetadataConnection.getDriverClass();
        if (driverClassName.equals(EDatabase4DriverClassName.JAVADB_EMBEDED.getDriverClass())
                || dbType.equals(EDatabaseTypeName.JAVADB_EMBEDED.getDisplayName())
                || dbType.equals(EDatabaseTypeName.JAVADB_DERBYCLIENT.getDisplayName())
                || dbType.equals(EDatabaseTypeName.JAVADB_JCCJDBC.getDisplayName())
                || dbType.equals(EDatabaseTypeName.HSQLDB_IN_PROGRESS.getDisplayName())) {

            String username = iMetadataConnection.getUsername();
            String pwd = iMetadataConnection.getPassword();
            String dbVersion = iMetadataConnection.getDbVersionString();
            String url = iMetadataConnection.getUrl();

            Connection connection = null;
            DriverShim wapperDriver = null;
            try {
                List list = ExtractMetaDataUtils.getInstance().connect(dbType, url, username, pwd, driverClassName,
                        iMetadataConnection.getDriverJarPath(), dbVersion, iMetadataConnection.getAdditionalParams());
                if (list != null && list.size() > 0) {
                    for (int i = 0; i < list.size(); i++) {
                        if (list.get(i) instanceof Connection) {
                            connection = (Connection) list.get(i);
                        }
                        if (list.get(i) instanceof DriverShim) {
                            wapperDriver = (DriverShim) list.get(i);
                        }
                    }
                }
            } catch (Exception e) {
                ExceptionHandler.process(e);
            } finally {
                try {
                    // for derby
                    if (wapperDriver != null) {
                        wapperDriver.connect("jdbc:derby:;shutdown=true", null); //$NON-NLS-1$
                    }
                    if (connection != null) {
                        // for hsqldb in-process
                        ConnectionUtils.closeConnection(connection);
                    }

                } catch (SQLException e) {
                    // exception of shutdown success. no need to catch.
                }

            }
        }

    }

    /**
     * Returns the progress monitor to use for operations run in this progress dialog.
     *
     * @return the progress monitor
     */
    @Override
    public IProgressMonitor getProgressMonitor() {
        return progressMonitor;
    }

    /**
     * Added by Tang Fengneng Sets the connParameters.
     *
     * @param connParameters the connParameters to set
     */
    @Override
    public void setConnParameters(ConnectionParameters connParameters) {
        this.connParameters = connParameters;
    }

    @Override
    public ConnectionParameters getConnParameters() {
        return connParameters;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.jface.dialogs.Dialog#cancelPressed()
     */
    @Override
    public void cancelPressed() {
        super.cancelPressed();
        SQLBuilderRepositoryNodeManager.tList = null;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.jface.dialogs.Dialog#okPressed()
     */
    @Override
    public void okPressed() {
        // gain the contextmode from sqlbuilder,and set it in connParameters,add by hyWang
        MultiPageSqlBuilderEditor editor = null;
        CTabFolder folder = getEditorComposite().getTabFolder();
        CTabItem[] a = folder.getItems();
        for (CTabItem itm : a) {
            Object obj = itm.getData("KEY"); //$NON-NLS-1$
            if (obj instanceof MultiPageSqlBuilderEditor) {
                editor = (MultiPageSqlBuilderEditor) obj;
            }
            if (editor != null) {
                if (itm.getData() instanceof Query) {
                    Query q = (Query) itm.getData();
                    connParameters.setIfContextButtonCheckedFromBuiltIn(q.isContextMode());
                }

            }
        }

        if (EParameterFieldType.DBTABLE.equals(connParameters.getFieldType())) {
            final IStructuredSelection selection = (IStructuredSelection) structureComposite.getTreeViewer().getSelection();
            final Object firstElement = selection.getFirstElement();
            if (firstElement instanceof RepositoryNode) {
                RepositoryNode node = (RepositoryNode) firstElement;
                boolean is = node.getProperties(EProperties.CONTENT_TYPE).equals(RepositoryNodeType.TABLE);
                if (is) {
                    MetadataTableRepositoryObject object = (MetadataTableRepositoryObject) node.getObject();
                    connParameters.setSelectDBTable(object.getSourceName());
                }
            }
        } else {
            String sql = ""; //$NON-NLS-1$
            // sql = editorComposite.getDefaultTabSql();
            sql = editorComposite.getCurrentTabSql();
            // if (ConnectionParameters.isJavaProject()) {
            // sql = sql.replace("\"", "\\" + "\"");
            // } else {
            // sql = sql.replace("'", "\\'");
            // }

            // sql = QueryUtil.checkAndAddQuotes(sql);
            if (!editorComposite.isIfcontext()) {
                sql = TalendTextUtils.addStrInQuery(sql);
            }

            connParameters.setQuery(sql, true);

            if (connParameters.isFromRepository() && !connParameters.isNodeReadOnly()) {
                List<Query> qs = new ArrayList<Query>();
                boolean isInfo = false;
                final CTabFolder tabFolder = getEditorComposite().getTabFolder();
                final CTabItem[] items = tabFolder.getItems();

                for (CTabItem item : items) {
                    final String text = item.getText();
                    boolean isInfo2 = text.length() > 1 && text.substring(0, 1).equals("*"); //$NON-NLS-1$
                    if (isInfo2) {
                        isInfo = true;
                    }
                }
                if (isInfo) {
                    String title = Messages.getString("SQLBuilderDialog.SaveAllQueries.Title"); //$NON-NLS-1$
                    String info = Messages.getString("SQLBuilderDialog.SaveAllQueries.Info"); //$NON-NLS-1$
                    boolean openQuestion = MessageDialog.openQuestion(getShell(), title, info);
                    if (openQuestion) {
                        for (CTabItem item : items) {
                            final String text = item.getText();
                            boolean isInfo2 = text.length() > 1 && text.substring(0, 1).equals("*"); //$NON-NLS-1$
                            if (isInfo2) {
                                MultiPageSqlBuilderEditor meditor = null;
                                Object control = item.getData("KEY"); //$NON-NLS-1$
                                if (control instanceof MultiPageSqlBuilderEditor) {
                                    meditor = (MultiPageSqlBuilderEditor) control;
                                }
                                if (meditor != null) {
                                    RepositoryNode node = null;
                                    node = meditor.getActivePageRepositoryNode();
                                    if (text.substring(1).startsWith(AbstractSQLEditorComposite.QUERY_PREFIX)) {
                                        if (item.getData() instanceof Query) {
                                            Query q = (Query) item.getData();
                                            q.setValue(meditor.getActivePageSqlString());
                                            // add by hyWang
                                            q.setContextMode(meditor.getActiveEditors().getContextmode().getContextmodeaction()
                                                    .isChecked());
                                            qs.add(q);
                                            if (node != null && q != null) {
                                                manager.saveQuery(node, q, null);
                                            }
                                        }
                                    } else {
                                        meditor.getActivePageSaveAsSQLAction().run();
                                    }
                                }
                            }
                        }
                    }
                }
                if (connParameters.getQueryObject() != null) {
                    RepositoryUpdateManager.updateQuery(connParameters.getQueryObject(), node);
                }
            }
        }
        deleteNoUseTable();
        super.okPressed();
    }

    private void deleteNoUseTable() {

        // add for bug TDI-17097
        connection = (DatabaseConnection) SQLBuilderRepositoryNodeManager.getItem(
                SQLBuilderRepositoryNodeManager.getRoot(nodeInEditor)).getConnection();
        if (SQLBuilderRepositoryNodeManager.tList instanceof List) {
            if (SQLBuilderRepositoryNodeManager.tList.size() == 0) {
                SQLBuilderRepositoryNodeManager.tList.addAll(ConnectionHelper.getTables(connection));
            }
        } else {
            SQLBuilderRepositoryNodeManager.tList = new ArrayList<MetadataTable>();
            SQLBuilderRepositoryNodeManager.tList.addAll(ConnectionHelper.getTables(connection));
        }

        if (SQLBuilderRepositoryNodeManager.tList == null || SQLBuilderRepositoryNodeManager.tList.size() == 0) {
            return;
        }

        // changed for bug TDI-19892
        if (node == null) {
            SQLBuilderRepositoryNodeManager.tList = null;
            return;
        }

        IRepositoryViewObject repositoryObject = node.getObject();
        Item item = repositoryObject.getProperty().getItem();
        if (item instanceof DatabaseConnectionItem) {
            manager.deleteNouseTables(((DatabaseConnectionItem) item).getConnection());
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.sqlbuilder.ui.ISQLBuilderDialog#refreshNode(org.talend.repository.model.RepositoryNode)
     */
    @Override
    public void refreshNode(RepositoryNode node) {
        structureComposite.doRefresh(node);
    }

    /**
     * qianbing class global comment. Refreshes Detail Composite according to selection changing of the database
     * structure viewer. <br/>
     *
     * $Id: talend-code-templates.xml,v 1.3 2006/11/01 05:38:28 nicolas Exp $
     *
     */
    public class RefreshDetailCompositeAction extends SelectionProviderAction {

        private Shell shell;

        SessionTreeNode sessionTreeNode;

        DriverShim wapperDriver = null;

        /**
         * qianbing RefreshDetailCompositeAction constructor comment.
         *
         * @param provider
         */
        public RefreshDetailCompositeAction(ISelectionProvider provider, Shell shell) {
            super(provider, "Refresh DetailComposite"); //$NON-NLS-1$
            this.shell = shell;
        }

        /*
         * (non-Java)
         *
         * @see
         * org.eclipse.ui.actions.SelectionProviderAction#selectionChanged(org.eclipse.jface.viewers.IStructuredSelection
         * )
         */
        @Override
        public void selectionChanged(final IStructuredSelection selection) {
            if (!selection.isEmpty() && selectedContext == null) {
                RepositoryNode repositoryNode = (RepositoryNode) selection.getFirstElement();
                if (repositoryNode.getObject() != null) {
                    Item item = repositoryNode.getObject().getProperty().getItem();
                    if (item instanceof DatabaseConnectionItem) {
                        org.talend.core.model.metadata.builder.connection.Connection connection = ((DatabaseConnectionItem) item)
                                .getConnection();
                        if (connection.isContextMode()) {
                            ContextItem contextItem = ContextUtils.getContextItemById2(connection.getContextId());
                            if (contextItem != null && connection.isContextMode()) {

                                ContextSetsSelectionDialog setsDialog = new ContextSetsSelectionDialog(shell, contextItem, false);
                                setsDialog.open();
                                selectedContext = setsDialog.getSelectedContext();
                            }
                        }
                    }
                }
            }
            IRunnableWithProgress progress = new IRunnableWithProgress() {

                @Override
                public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
                    monitor.beginTask("", IProgressMonitor.UNKNOWN); //$NON-NLS-1$

                    try {
                        Display.getDefault().asyncExec(new Runnable() {

                            @Override
                            public void run() {
                                INode node = null;
                                String msg = null;
                                if (!selection.isEmpty()) {
                                    try {
                                        final RepositoryNode repositoryNode = (RepositoryNode) selection.getFirstElement();
                                        if (SQLBuilderRepositoryNodeManager.getRepositoryType(repositoryNode) == RepositoryNodeType.FOLDER) {
                                            return;
                                        }
                                        sessionTreeNode = nodeManager.getSessionTreeNode(repositoryNode, selectedContext);
                                        node = nodeManager.convert2INode(repositoryNode, selectedContext, sessionTreeNode);
                                    } catch (Exception e) {
                                        msg = e.getMessage();
                                        SqlBuilderPlugin.log(msg, e);
                                    }
                                    final INode argNode = node;
                                    final String argMsg = msg;
                                    dbDetailsComposite.setSelectedNode(argNode, argMsg);
                                    if(sessionTreeNode!=null) {
                                    // bug 17980
                                    wapperDriver = sessionTreeNode.getWapperDriver();

                                    // bug 20892:specify for derby need close connection,no need to close connection for
                                    // other dbs
                                    // bug 22619 improved by 20892
                                    String dbType = sessionTreeNode.getDatabaseConnection().getDatabaseType();

                                    String driverClassName = sessionTreeNode.getDatabaseConnection().getDriverClass();
                                    if (wapperDriver != null
                                            && (driverClassName.equals(EDatabase4DriverClassName.JAVADB_EMBEDED.getDriverClass())
                                                    || dbType.equals(EDatabaseTypeName.JAVADB_EMBEDED.getDisplayName())
                                                    || dbType.equals(EDatabaseTypeName.JAVADB_DERBYCLIENT.getDisplayName()) || dbType
                                                        .equals(EDatabaseTypeName.JAVADB_JCCJDBC.getDisplayName()))) {
                                        try {
                                            wapperDriver.connect("jdbc:derby:;shutdown=true", null); //$NON-NLS-1$
                                        } catch (SQLException e) {
                                            // exception of shutdown success. no need to catch.
                                        }
                                    }
                                }
                                }
                            }
                        });
                    } finally {
                        monitor.done();
                    }
                }
            };

            UIUtils.runWithProgress(progress, true, getProgressMonitor(), getShell());

        }
    }

    public boolean isFromRepositoryView() {
        return this.isFromRepositoryView;
    }

    public void setFromRepositoryView(boolean isFromRepositoryView) {
        this.isFromRepositoryView = isFromRepositoryView;
    }

    @Override
    public void repositoryChanged(RepositoryChangedEvent event) {
        clean();
        if (structureComposite != null) {
            structureComposite.updateStructureView(event);
        }
        manager.synchronizeAllSqlEditors(this);
    }

    @Override
    public void notifySQLBuilder(IRepositoryViewObject o) {
        CorePlugin.getDefault().getRepositoryService().removeRepositoryChangedListener(this);
        CorePlugin.getDefault().getRepositoryService().repositoryChanged(new RepositoryElementDelta(o));
        CorePlugin.getDefault().getRepositoryService().registerRepositoryChangedListener(this);
    }

    /*
     * (non-Java)
     *
     * @see org.talend.sqlbuilder.ui.ISQLBuilderDialog#openEditor(org.talend.repository.model.RepositoryNode,
     * java.util.List, org.talend.sqlbuilder.util.ConnectionParameters, boolean, java.util.List)
     */
    @Override
    public void openEditor(RepositoryNode node, List<String> repositoryName, ConnectionParameters connParam,
            boolean isDefaultEditor, List<IRepositoryNode> nodeSel) {
        this.nodeInEditor = node;
        editorComposite.setNodesSel(nodeSel);
        editorComposite.openNewEditor(node, repositoryName, connParam, isDefaultEditor);

    }

    public DBStructureComposite getStructureComposite() {
        return this.structureComposite;
    }

    public void setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.sqlbuilder.ui.ISQLBuilderDialog#getSelectedContext()
     */
    @Override
    public String getSelectedContext() {
        return this.selectedContext;
    }

}
