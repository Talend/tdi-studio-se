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
package org.talend.sqlbuilder.dbstructure;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.viewers.ITableColorProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.talend.commons.exception.BusinessException;
import org.talend.commons.exception.ExceptionHandler;
import org.talend.commons.exception.PersistenceException;
import org.talend.commons.runtime.model.repository.ERepositoryStatus;
import org.talend.commons.utils.data.container.Container;
import org.talend.commons.utils.data.container.RootContainer;
import org.talend.core.model.general.Project;
import org.talend.core.model.metadata.IMetadataConnection;
import org.talend.core.model.metadata.builder.ConvertionHelper;
import org.talend.core.model.metadata.builder.connection.Connection;
import org.talend.core.model.metadata.builder.connection.DatabaseConnection;
import org.talend.core.model.metadata.builder.connection.MetadataColumn;
import org.talend.core.model.metadata.builder.connection.MetadataTable;
import org.talend.core.model.metadata.builder.connection.QueriesConnection;
import org.talend.core.model.metadata.builder.connection.Query;
import org.talend.core.model.metadata.builder.connection.impl.MetadataTableImpl;
import org.talend.core.model.properties.ConnectionItem;
import org.talend.core.model.properties.DatabaseConnectionItem;
import org.talend.core.model.properties.Property;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.model.repository.Folder;
import org.talend.core.model.repository.IRepositoryViewObject;
import org.talend.core.model.repository.RepositoryObject;
import org.talend.core.model.repository.RepositoryViewObject;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.core.sqlbuilder.util.ConnectionParameters;
import org.talend.cwm.helper.ConnectionHelper;
import org.talend.cwm.helper.SubItemHelper;
import org.talend.cwm.helper.TableHelper;
import org.talend.repository.ProjectManager;
import org.talend.repository.model.IRepositoryNode.ENodeType;
import org.talend.repository.model.IRepositoryNode.EProperties;
import org.talend.repository.model.RepositoryNode;
import org.talend.sqlbuilder.Messages;
import org.talend.sqlbuilder.RepositoryNodeExt;
import org.talend.sqlbuilder.SqlBuilderPlugin;
import org.talend.sqlbuilder.repository.utility.SQLBuilderRepositoryNodeManager;
import org.talend.sqlbuilder.ui.DBStructureComposite;
import org.talend.sqlbuilder.util.ImageUtil;

/**
 * Detailled comment for this class. <br/>
 * $Id: DBTreeProvider.java Version 1.0 Nov 6, 2006 2:29:19 AM $
 *
 * @author phou
 *
 */
public class DBTreeProvider extends LabelProvider implements ITableLabelProvider, ITreeContentProvider, ITableColorProvider {

    private static final String IMAGES_CLOSED_FOLDER = "Images.closedFolder"; //$NON-NLS-1$

    public static final String BUILT_IN = "Built-In"; //$NON-NLS-1$

    private static final String IMAGES_DATABASE_ICON = "Images.DatabaseIcon"; //$NON-NLS-1$

    private static final String IMAGES_CONNECTION_ICON = "Images.ConnectionIcon"; //$NON-NLS-1$

    private static final String IMAGES_APPEND_TO_EDITOR = "Images.AppendToEditor"; //$NON-NLS-1$

    private static final String IMAGES_SQL_EDITOR_ICON = "Images.SqlEditorIcon"; //$NON-NLS-1$

    private static final String IMAGES_TABLE_NODE_ICON = "Images.TableNodeIcon"; //$NON-NLS-1$

    private static final String IMAGES_COLUMN_NODE_ICON = "Images.ColumnNodeIcon"; //$NON-NLS-1$

    private static final String IMAGES_REFRESH_ICON = "Images.RefreshIcon"; //$NON-NLS-1$

    private static final String IMAGES_RED_ICON = "Images.RedIcon"; //$NON-NLS-1$

    private static final String IMAGES_GRAY_ICON = "Images.GrayIcon"; //$NON-NLS-1$

    public static final String COLOR_GRAY = "COLOR_GRAY"; //$NON-NLS-1$

    public static final String COLOR_RED = "COLOR_RED"; //$NON-NLS-1$

    private SQLBuilderRepositoryNodeManager repositoryNodeManager = new SQLBuilderRepositoryNodeManager();

    private ConnectionParameters connectionParameters;

    private boolean isRefresh;

    private Map<String, Color> colors = new HashMap<String, Color>();

    private RepositoryNode selectedExtReposiotryNode;

    // public DBTreeProvider(RepositoryView repositoryView, ConnectionParameters connectionParameters) {
    // this(connectionParameters);
    // }

    public DBTreeProvider(ConnectionParameters connectionParameters) {
        this.connectionParameters = connectionParameters;
        colors.put(COLOR_RED, Display.getDefault().getSystemColor(SWT.COLOR_RED));
        colors.put(COLOR_GRAY, Display.getDefault().getSystemColor(SWT.COLOR_GRAY));
    }

    public DBTreeProvider(DBStructureComposite composite, ConnectionParameters connectionParameters) {
        this(connectionParameters);
    }

    public boolean isRefresh() {
        return isRefresh;
    }

    public void setRefresh(boolean refreshValue) {
        this.isRefresh = refreshValue;
    }

    public Image getColumnImage(Object element, int columnIndex) {
        RepositoryNode node = (RepositoryNode) element;
        SqlBuilderRepositoryObject repositoryObject = (SqlBuilderRepositoryObject) node.getObject();
        if (columnIndex == 1) {
            return null;
        } else if (columnIndex == 2) {
            if ((repositoryObject).getDiffImage() == null) {
                return null;
            }
            return ImageUtil.getImage((repositoryObject).getDiffImage());
        }
        return ImageUtil.getImage((repositoryObject).getImage());
    }

    public String getColumnText(Object element, int columnIndex) {
        RepositoryNode node = (RepositoryNode) element;
        if (columnIndex == 0) {
            return ((SqlBuilderRepositoryObject) node.getObject()).getSourceName();
        } else if (columnIndex == 1) {
            return ((SqlBuilderRepositoryObject) node.getObject()).getRepositoryName();
        }

        return null;
    }

    @SuppressWarnings("static-access")//$NON-NLS-1$
    public Object[] getChildren(Object parentElement) {
        RepositoryNode repositoryNode = (RepositoryNode) parentElement;
        RepositoryNode rootNode = repositoryNodeManager.getRoot(repositoryNode);
        boolean isBuildIn = ((SqlBuilderRepositoryObject) rootNode.getObject()).isBuildIn();
        if (isRefresh) {
            refreshRootNode(rootNode);
            rootNode.getChildren().clear();
            DatabaseConnection metadataConnection = (DatabaseConnection) ((ConnectionItem) rootNode.getObject().getProperty()
                    .getItem()).getConnection();
            createTables(rootNode, rootNode.getObject(), metadataConnection, isBuildIn);
            createQueries(rootNode, rootNode.getObject(), metadataConnection, isBuildIn);
            isRefresh = false;
            return repositoryNode.getChildren().toArray();

        } else {
            // if intialized already > same as now, retrieve the childrens
            if (!repositoryNode.isInitialized()) {
                // retrieve columns of the specified table
                RepositoryNodeType type = (RepositoryNodeType) repositoryNode.getProperties(EProperties.CONTENT_TYPE);
                if (type == RepositoryNodeType.TABLE) {
                    try {
                        MetadataTable table = ((MetadataTableRepositoryObject) repositoryNode.getObject()).getTable();
                        DatabaseConnectionItem connItem = repositoryNodeManager.getItem(repositoryNode);
                        DatabaseConnection dbConn = (DatabaseConnection) connItem.getConnection();
                        IMetadataConnection iMetadataConnection = ConvertionHelper.convert(dbConn);
                        repositoryNode.getChildren().clear();
                        repositoryNodeManager.modifyOldRepositoryNode(dbConn, iMetadataConnection, repositoryNode);
                        createColumns(repositoryNode, rootNode.getObject(), table, isBuildIn);
                        // set node intialized to true.
                        repositoryNode.setInitialized(true);
                    } catch (Exception e) {
                        ExceptionHandler.process(e);
                    }
                }
            }
            return repositoryNode.getChildren().toArray();
        }
    }

    public void refreshRootNode(RepositoryNode rootNode) {
        SqlBuilderRepositoryObject repositoryObject = (SqlBuilderRepositoryObject) rootNode.getObject();
        boolean[] isDiffs = repositoryNodeManager.isDiff(rootNode);
        if (isDiffs[0]) {
            repositoryObject.setDiffImage(IMAGES_GRAY_ICON);
        }
        if (isDiffs[1]) {
            repositoryObject.setDiffImage(IMAGES_RED_ICON);
        }
        if (isDiffs[2]) {
            repositoryObject.setDiffImage(IMAGES_REFRESH_ICON);
        }
    }

    public Object getParent(Object element) {
        RepositoryNode node = (RepositoryNode) element;
        final RepositoryNode parent = node.getParent();
        if (parent != null) {
            return parent;
        } else {
            return node;
        }
    }

    public boolean hasChildren(Object element) {
        // if it's a table
        // return true.
        RepositoryNodeType type = (RepositoryNodeType) ((RepositoryNode) element).getProperties(EProperties.CONTENT_TYPE);
        if (type == RepositoryNodeType.TABLE) {
            return true;
        }
        return getChildren(element).length > 0;
    }

    public Object[] getElements(Object inputElement) {
        if (!(inputElement instanceof RepositoryNode)) {
            return new Object[0];
        }
        RepositoryNode treeRoot = (RepositoryNode) inputElement;
        if (!isInitialized) {
            initialize(treeRoot);
            isInitialized = true;
        }
        return treeRoot.getChildren().toArray();
    }

    private boolean isInitialized = false;

    /**
     * Sets the isInitialized.
     *
     * @param isInitialized the isInitialized to set
     */
    public void setInitialized(boolean isInitialized) {
        this.isInitialized = isInitialized;
    }

    private void initialize(RepositoryNode treeRoot) {
        if (!connectionParameters.isRepository()) {
            addNode(treeRoot, repositoryNodeManager.getRepositoryNodeByBuildIn(treeRoot, connectionParameters).getObject(), true,
                    null);
        } else {
            Container metadataConnection = getMetadataConnection();
            convert(metadataConnection, treeRoot, ERepositoryObjectType.METADATA_CONNECTIONS);
        }
    }

    /**
     * @return MetadataConnection
     */
    @SuppressWarnings("unchecked")
    private Container getMetadataConnection() {
        ProxyRepositoryFactory factory = ProxyRepositoryFactory.getInstance();
        ProjectManager pManager = ProjectManager.getInstance();
        Container container = null;
        try {
            container = factory.getMetadata(pManager.getCurrentProject(), ERepositoryObjectType.METADATA_CONNECTIONS);
            pManager.retrieveReferencedProjects();
            for (Project p : pManager.getAllReferencedProjects()) {
                RootContainer rContainer = factory.getMetadata(p, ERepositoryObjectType.METADATA_CONNECTIONS);
                if (container == null) {
                    container = rContainer;
                } else if (rContainer != null) {
                    Iterator iterator = rContainer.absoluteKeySet().iterator();
                    while (iterator.hasNext()) {
                        Object id = iterator.next();
                        container.addMember(id, rContainer.getAbsoluteMember(id));
                    }
                }
            }

        } catch (PersistenceException e) {
            SqlBuilderPlugin.log(Messages.getString("DBTreeProvider.logMessage"), e); //$NON-NLS-1$
        } catch (BusinessException e) {
            SqlBuilderPlugin.log(Messages.getString("DBTreeProvider.logMessage"), e); //$NON-NLS-1$
        }
        return container;
    }

    private boolean isCleared = false;

    private void convert(Container fromModel, RepositoryNode parent, ERepositoryObjectType type) {
        if (fromModel.isEmpty()) {
            return;
        }

        for (Object obj : fromModel.getSubContainer()) {
            Container container = (Container) obj;
            Folder folderRepositoryObject = new Folder(container.getId(), container.getLabel());
            FolderRepositoryObject oFolder = new FolderRepositoryObject(folderRepositoryObject);
            oFolder.setImage(IMAGES_CLOSED_FOLDER);
            oFolder.setSourceName(oFolder.getLabel());
            oFolder.setRepositoryName(null);
            RepositoryNode folder = new RepositoryNodeExt(oFolder, parent, ENodeType.SIMPLE_FOLDER);
            folder.setProperties(EProperties.LABEL, container.getLabel());
            // ERepositoryObjectType.FOLDER);
            folder.setProperties(EProperties.CONTENT_TYPE, RepositoryNodeType.FOLDER);
            parent.getChildren().add(folder);
            convert(container, folder, type);
        }
        if (!isCleared) {
            maps.clear();
            isCleared = true;
        }
        for (Object obj : fromModel.getMembers()) {
            RepositoryObject obj2 = (RepositoryObject) obj;
            if (!connectionParameters.getRepositoryId().equals(obj2.getProperty().getId())) {
                continue;
            }
            RepositoryViewObject viewObject = new RepositoryViewObject(obj2.getProperty());
            maps.put((obj2).getId(), viewObject);
            addNode(parent, viewObject, false, null);
        }
    }

    private static Map<String, IRepositoryViewObject> maps = new HashMap<String, IRepositoryViewObject>();

    private void addNode(RepositoryNode parent, IRepositoryViewObject repositoryObject, boolean isBuildIn, Integer index) {
        ProxyRepositoryFactory factory = ProxyRepositoryFactory.getInstance();
        DatabaseConnection connection = (DatabaseConnection) ((ConnectionItem) repositoryObject.getProperty().getItem())
                .getConnection();
        String sid = connection.getSID();
        MetadataConnectionRepositoryObject connectionRepositoryObject = new MetadataConnectionRepositoryObject(repositoryObject);
        if (isBuildIn) {
            connectionRepositoryObject.setRepositoryName(BUILT_IN);
        } else {
            connectionRepositoryObject.setRepositoryName(repositoryObject.getLabel());
        }
        connectionRepositoryObject.setSourceName((sid == null || sid.trim().equals("")) ? connection.getDatasourceName() : sid); //$NON-NLS-1$
        if (!isBuildIn) {
            connectionRepositoryObject.setImage(IMAGES_CONNECTION_ICON);
        } else {
            connectionRepositoryObject.setImage(IMAGES_DATABASE_ICON);
        }
        connectionRepositoryObject.setBuildIn(isBuildIn);

        RepositoryNode node = new RepositoryNodeExt(connectionRepositoryObject, parent, ENodeType.REPOSITORY_ELEMENT);
        node.setProperties(EProperties.CONTENT_TYPE, RepositoryNodeType.DATABASE);
        node.setProperties(EProperties.LABEL, repositoryObject.getLabel());
        boolean[] isDiffs = repositoryNodeManager.isDiff(node);
        if (isDiffs[0]) {
            connectionRepositoryObject.setDiffImage(IMAGES_GRAY_ICON);
        }
        if (isDiffs[1]) {
            connectionRepositoryObject.setDiffImage(IMAGES_RED_ICON);
        }
        if (isDiffs[2]) {
            connectionRepositoryObject.setDiffImage(IMAGES_REFRESH_ICON);
        }

        if (factory.getStatus(repositoryObject) == ERepositoryStatus.DELETED) {
            // ignore recycle node
        } else {
            if (index == null) {
                parent.getChildren().add(node);
            } else {
                parent.getChildren().add(index.intValue(), node);
            }
            repositoryNodeManager.addRepositoryNode(node);
            DatabaseConnection metadataConnection = (DatabaseConnection) ((ConnectionItem) repositoryObject.getProperty()
                    .getItem()).getConnection();
            createTables(node, repositoryObject, metadataConnection, isBuildIn);
            createQueries(node, repositoryObject, metadataConnection, isBuildIn);
        }

    }

    private List<Query> displayQueries = new ArrayList<Query>();

    private void createQueries(RepositoryNode node, final IRepositoryViewObject repObj, DatabaseConnection metadataConnection,
            boolean isBuildIn) {
        QueriesConnection queriesConnection = metadataConnection.getQueries();

        if (queriesConnection != null) {
            QueriesConnectionRepositoryObject repositoryObject = new QueriesConnectionRepositoryObject(repObj, queriesConnection);
            repositoryObject.setImage(IMAGES_APPEND_TO_EDITOR);
            repositoryObject.setSourceName(Messages.getString("DBTreeProvider.sourceName")); //$NON-NLS-1$
            RepositoryNode queriesConnectionNode = new RepositoryNodeExt(repositoryObject, node, ENodeType.REPOSITORY_ELEMENT);
            queriesConnectionNode.setProperties(EProperties.CONTENT_TYPE, RepositoryNodeType.QUERIESCONNECTION);
            node.getChildren().add(queriesConnectionNode);
            createQuery(queriesConnectionNode, repObj, queriesConnection);
        }
    }

    private void createQuery(RepositoryNode queriesConnectionNode, IRepositoryViewObject repObj,
            QueriesConnection queriesConnection) {
        displayQueries.clear();
        for (Iterator iter = queriesConnection.getQuery().iterator(); iter.hasNext();) {
            Query query = (Query) iter.next();
            boolean isDelete = SubItemHelper.isDeleted(query);
            boolean isReadOnly = connectionParameters.isNodeReadOnly();
            if (isDelete && !isReadOnly) {
                continue;
            }
            // if (!TableHelper.isDeleted(query)) {
            QueryRepositoryObject repositoryObject = new QueryRepositoryObject(repObj, query);
            repositoryObject.setImage(IMAGES_SQL_EDITOR_ICON);
            repositoryObject.setSourceName(query.getLabel());
            RepositoryNode node = new RepositoryNodeExt(repositoryObject, queriesConnectionNode, ENodeType.REPOSITORY_ELEMENT);
            node.setProperties(EProperties.CONTENT_TYPE, RepositoryNodeType.QUERY);
            queriesConnectionNode.getChildren().add(node);
            if (connectionParameters.getQueryObject() != null
                    && query.getLabel().equals(connectionParameters.getQueryObject().getLabel())) {
                this.selectedExtReposiotryNode = node;
            }
            displayQueries.add(query);
            // }
        }
    }

    /**
     * tguiu Comment method "createTables".
     *
     * @param node
     * @param iMetadataConnection
     * @param metadataConnection
     */
    private void createTables(RepositoryNode node, final IRepositoryViewObject repObj, Connection metadataConnection,
            boolean isBuildIn) {
        for (Object currentTable : ConnectionHelper.getTables(metadataConnection)) {
            org.talend.core.model.metadata.builder.connection.MetadataTable metadataTable = (org.talend.core.model.metadata.builder.connection.MetadataTable) currentTable;
            if (metadataTable.getSourceName() != null) {
                RepositoryNode tableNode = createMetatable(node, repObj, metadataTable, isBuildIn);
                if (TableHelper.isDeleted(metadataTable)) {
                    // ignore recycle node
                } else if (metadataTable.getSourceName().equals(" ")) {
                    // TDI-23826:ignore drop table in the query
                } else {
                    node.getChildren().add(tableNode);
                }
                if (connectionParameters.getMetadataTable() != null
                        && metadataTable.getLabel().equals(connectionParameters.getMetadataTable().getLabel())) {
                    this.selectedExtReposiotryNode = tableNode;
                }

                // create columns
                createColumns(tableNode, repObj, currentTable, isBuildIn);
            }
        }
    }

    private void createColumns(RepositoryNode tableNode, IRepositoryViewObject repObj, Object currentTable, boolean isBuildIn) {
        for (Object currentColumn : ((MetadataTable) currentTable).getColumns()) {
            MetadataColumn metadataColumn = (MetadataColumn) currentColumn;
            RepositoryNode columnNode = createMetacolumn(tableNode, repObj, metadataColumn, isBuildIn);

            tableNode.getChildren().add(columnNode);
        }
    }

    private RepositoryNode createMetacolumn(RepositoryNode tableNode, IRepositoryViewObject repObj,
            MetadataColumn metadataColumn, boolean isBuildIn) {
        MetadataColumnRepositoryObject modelObj = new MetadataColumnRepositoryObject(repObj, metadataColumn);
        modelObj.setRepositoryName(metadataColumn.getLabel());
        // statusCode use for source table name
        modelObj.setSourceName(metadataColumn.getOriginalField());
        // purpose use for Image text.
        if (metadataColumn.isSynchronised()) {
            modelObj.setImage(IMAGES_REFRESH_ICON);
        } else {
            modelObj.setImage(IMAGES_COLUMN_NODE_ICON);
        }
        // description use for color.
        if (modelObj.getColumn().isDivergency() && !isBuildIn) {
            modelObj.setColor(COLOR_RED);
        }
        if (modelObj.getRepositoryName() == null || modelObj.getRepositoryName().trim().equals("")) { //$NON-NLS-1$
            modelObj.setColor(COLOR_GRAY);
        }
        modelObj.setBuildIn(isBuildIn);

        RepositoryNode columnNode = new RepositoryNodeExt(modelObj, tableNode, ENodeType.REPOSITORY_ELEMENT);
        columnNode.setProperties(EProperties.LABEL, metadataColumn.getLabel());
        columnNode.setProperties(EProperties.CONTENT_TYPE, RepositoryNodeType.COLUMN);
        return columnNode;
    }

    /**
     * tguiu Comment method "createMetatable".
     *
     * @param node
     * @param iMetadataFileDelimited
     * @param table
     * @return
     */
    private RepositoryNode createMetatable(RepositoryNode node, IRepositoryViewObject repObj,
            final org.talend.core.model.metadata.builder.connection.MetadataTable table, boolean isBuildIn) {
        MetadataTableRepositoryObject modelObj = new MetadataTableRepositoryObject(repObj, table);
        if (table instanceof MetadataTableImpl) {
            modelObj.setRepositoryName(((MetadataTableImpl) table).getOriginalLabel());
        } else {
            modelObj.setRepositoryName(table.getLabel());
        }

        // statusCode use for source table name
        modelObj.setSourceName(table.getSourceName());
        // purpose use for Image text.
        modelObj.setImage(IMAGES_TABLE_NODE_ICON);
        // description use for color.
        if (modelObj.getTable().isDivergency() && !isBuildIn && !modelObj.getTable().isSynchronised()) {
            modelObj.setColor(COLOR_RED);
        }
        if (modelObj.getRepositoryName() == null || modelObj.getRepositoryName().trim().equals("")) { //$NON-NLS-1$
            modelObj.setColor(COLOR_GRAY);
        }
        modelObj.setBuildIn(isBuildIn);

        RepositoryNode tableNode = new RepositoryNodeExt(modelObj, node, ENodeType.REPOSITORY_ELEMENT);
        tableNode.setProperties(EProperties.LABEL, table.getLabel());
        tableNode.setProperties(EProperties.CONTENT_TYPE, RepositoryNodeType.TABLE);

        boolean[] isDiffs = repositoryNodeManager.isDiff(tableNode);
        if (isDiffs[0]) {
            modelObj.setDiffImage(IMAGES_GRAY_ICON);
        }
        if (isDiffs[1]) {
            modelObj.setDiffImage(IMAGES_RED_ICON);
        }
        if (isDiffs[2]) {
            modelObj.setDiffImage(IMAGES_REFRESH_ICON);
        }

        return tableNode;
    }

    /**
     */
    public static class QueriesConnectionRepositoryObject extends SqlBuilderRepositoryObject {

        private IRepositoryViewObject repObj;

        private QueriesConnection queriesConnection;

        public QueriesConnectionRepositoryObject(IRepositoryViewObject repObj, QueriesConnection queriesConnection) {
            super(repObj.getProperty());
            this.repObj = repObj;
            this.queriesConnection = queriesConnection;
        }

        public Property getProperty() {
            return repObj.getProperty();
        }

        public String getLabel() {
            return repObj.getLabel();
        }

        public QueriesConnection getQueriesConnection() {
            return this.queriesConnection;
        }
    }

    /**
     */
    public static class QueryRepositoryObject extends SqlBuilderRepositoryObject {

        private IRepositoryViewObject repObj;

        private Query query;

        public QueryRepositoryObject(IRepositoryViewObject repObj, Query query) {
            super(repObj.getProperty());
            this.repObj = repObj;
            this.query = query;
        }

        public Property getProperty() {
            return repObj.getProperty();
        }

        public Query getQuery() {
            return this.query;
        }
    }

    /**
     */
    public static class FolderRepositoryObject extends SqlBuilderRepositoryObject {

        private IRepositoryViewObject repObj;

        public FolderRepositoryObject(IRepositoryViewObject repObj) {
            super(null);
            this.repObj = repObj;
        }

        public Property getProperty() {
            return repObj.getProperty();
        }

        @Override
        public String getId() {
            return repObj.getId();
        }

        @Override
        public String getLabel() {
            return repObj.getLabel();
        }
    }

    /**
     */
    public static class MetadataConnectionRepositoryObject extends SqlBuilderRepositoryObject {

        private IRepositoryViewObject repObj;

        public MetadataConnectionRepositoryObject(IRepositoryViewObject repObj) {
            super(repObj.getProperty());
            this.repObj = repObj;
        }

        public Property getProperty() {
            return repObj.getProperty();
        }

        public DatabaseConnection getConnection() {
            DatabaseConnection metadataConnection = (DatabaseConnection) ((ConnectionItem) repObj.getProperty().getItem())
                    .getConnection();

            return metadataConnection;
        }
    }

    /**
     */
    public static class MetadataTableRepositoryObject extends SqlBuilderRepositoryObject {

        private IRepositoryViewObject repObj;

        private org.talend.core.model.metadata.builder.connection.MetadataTable table;

        public MetadataTableRepositoryObject(IRepositoryViewObject repObj,
                org.talend.core.model.metadata.builder.connection.MetadataTable table) {
            super(repObj.getProperty());
            this.repObj = repObj;
            this.table = table;
        }

        public Property getProperty() {
            return repObj.getProperty();
        }

        public org.talend.core.model.metadata.builder.connection.MetadataTable getTable() {
            return this.table;
        }
    }

    /**
     */
    public static class MetadataColumnRepositoryObject extends SqlBuilderRepositoryObject {

        private IRepositoryViewObject repObj;

        private MetadataColumn column;

        public MetadataColumnRepositoryObject(IRepositoryViewObject repObj, MetadataColumn column) {
            super(repObj.getProperty());
            this.repObj = repObj;
            this.column = column;
        }

        public Property getProperty() {
            return repObj.getProperty();
        }

        public MetadataColumn getColumn() {
            return this.column;
        }
    }

    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {

    }

    public Color getBackground(Object element, int columnIndex) {
        RepositoryNode repositoryNode = (RepositoryNode) element;
        SqlBuilderRepositoryObject repositoryObject = (SqlBuilderRepositoryObject) repositoryNode.getObject();
        if (columnIndex == 2) {
            return null;
        } else {
            return colors.get((repositoryObject).getColor());
        }

    }

    public Color getForeground(Object element, int columnIndex) {
        return null;
    }

    public static Map<String, IRepositoryViewObject> getMaps() {
        return maps;
    }

    public RepositoryNode getSelectedExtReposiotryNode() {
        return this.selectedExtReposiotryNode;
    }

    public List<Query> getDisplayQueries() {
        return this.displayQueries;
    }

}
