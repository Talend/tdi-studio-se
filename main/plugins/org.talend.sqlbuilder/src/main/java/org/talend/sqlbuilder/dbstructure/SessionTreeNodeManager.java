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

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.talend.core.database.EDatabaseTypeName;
import org.talend.core.model.metadata.builder.connection.DatabaseConnection;
import org.talend.core.model.metadata.builder.connection.MetadataTable;
import org.talend.core.model.properties.ConnectionItem;
import org.talend.repository.model.IRepositoryNode.EProperties;
import org.talend.repository.model.RepositoryNode;
import org.talend.sqlbuilder.dbstructure.DBTreeProvider.MetadataTableRepositoryObject;
import org.talend.sqlbuilder.dbstructure.nodes.CatalogNode;
import org.talend.sqlbuilder.dbstructure.nodes.DatabaseNode;
import org.talend.sqlbuilder.dbstructure.nodes.INode;
import org.talend.sqlbuilder.dbstructure.nodes.TableNode;
import org.talend.sqlbuilder.repository.utility.SQLBuilderRepositoryNodeManager;
import org.talend.sqlbuilder.sessiontree.model.SessionTreeNode;

/**
 *
 * This is a SessionTreeNode manager,It is used to process the creation, convertion,store of the SessionTreeNode. It can
 * convert DatabaseConnect to SessionTreeNode. SessionTreeNode is used in sql editor,sql result and database detail
 * viewer.
 *
 * $Id: talend-code-templates.xml,v 1.3 2006/11/01 05:38:28 bqian Exp $
 *
 */
public class SessionTreeNodeManager {

    // Used for SessionTreeNode cache
    private static Map<DatabaseConnection, SessionTreeNode> map = Collections
            .synchronizedMap(new HashMap<DatabaseConnection, SessionTreeNode>());

    /**
     * DOC qianbing Comment method "clear". Clears the cache.
     */
    public void clear() {
        map = Collections.synchronizedMap(new HashMap<DatabaseConnection, SessionTreeNode>());
    }

    /**
     * Converts the DatabaseConnection to SessionTreeNode, and stores the SessionTreeNode.
     *
     * @param repositoryNode RepositoryNode
     * @param selectedContext
     * @return SessionTreeNode
     */
    public SessionTreeNode getSessionTreeNode(RepositoryNode repositoryNode, String selectedContext) throws Exception {
        // Gets the root RepositoryNode
        RepositoryNode root = getRoot(repositoryNode);
        // Gets the DatabaseConnection
        DatabaseConnection connection = (DatabaseConnection) ((ConnectionItem) root.getObject().getProperty().getItem())
                .getConnection();

        if (EDatabaseTypeName.ACCESS.getDisplayName().equals(connection.getDatabaseType())) {
            if (connection.getURL().lastIndexOf("=") != connection.getURL().length() - 1) { //$NON-NLS-1$
                connection.setDatasourceName(connection.getURL().substring(connection.getURL().lastIndexOf(File.separator) + 1,
                        connection.getURL().length()));
                connection.setSID(connection.getURL().substring(connection.getURL().lastIndexOf(File.separator) + 1,
                        connection.getURL().length()));
            }
        }

        SessionTreeNode sessionTreeNode = map.get(connection);

        // hyWang modified for bug 0007062
        if (sessionTreeNode != null && !sessionTreeNode.isConnectionClosed()) {
            return sessionTreeNode;
        }
        // If the node is not existent,creates one and cache it.
        sessionTreeNode = SessionTreeNodeUtils.getSessionTreeNode(connection, root, selectedContext);
        map.put(connection, sessionTreeNode);
        return sessionTreeNode;
    }

    /**
     * DOC qianbing Comment method "convert2INode". Converts the RepositoryNode input to INode. INode is used for the
     * sql editor,result viewer and the detail viewer.
     *
     * @param repositoryNode RepositoryNode
     * @param selectedContext
     * @return INode
     */
    public INode convert2INode(RepositoryNode repositoryNode, String selectedContext, SessionTreeNode sessionTreeNode)
            throws Exception {
        // Creates the SessionTreeNode.
        RepositoryNodeType nodeType = getRepositoryType(repositoryNode);
        if (nodeType.equals(RepositoryNodeType.DATABASE)) {
            // processes the database
            DatabaseModel model = sessionTreeNode.getDbModel();
            INode[] nodes = model.getChildNodes();
            DatabaseNode dn = (DatabaseNode) nodes[0];
            return dn;
        } else if (nodeType.equals(RepositoryNodeType.TABLE)) {
            // processes the table
            MetadataTableRepositoryObject tableObject = (MetadataTableRepositoryObject) repositoryNode.getObject();
            MetadataTable table = tableObject.getTable();
            String realName = table.getSourceName();

            DatabaseModel model = sessionTreeNode.getDbModel();
            INode[] nodes = model.getChildNodes();
            DatabaseNode dn = (DatabaseNode) nodes[0];
            nodes = dn.getChildNodes();
            CatalogNode cn = (CatalogNode) nodes[0];
            nodes = cn.getChildNodes();
            if (nodes != null && nodes.length > 0) {
                for (INode node : nodes) {
                    TableNode tableNode = (TableNode) node;
                    if (tableNode.getName().equals(realName)) {
                        return node;
                    }
                }
            }
        } else if (nodeType.equals(RepositoryNodeType.COLUMN)) {
            // Processes the column.
            // Gets the repositoryNode's parent,should be the repositoryNode of table infomation.
            repositoryNode = repositoryNode.getParent();
            return convert2INode(repositoryNode, selectedContext, sessionTreeNode);
        }
        return null;
    }

    /**
     * DOC qianbing Comment method "getRepositoryType". Gets the type of the RepositoryNode.
     *
     * @param repositoryNode RepositoryNode
     * @return RepositoryNodeType
     * @see RepositoryNodeType
     */
    private RepositoryNodeType getRepositoryType(RepositoryNode repositoryNode) {
        return (RepositoryNodeType) repositoryNode.getProperties(EProperties.CONTENT_TYPE);
    }

    /**
     * DOC qianbing Comment method "getRoot". Gets the root RepositoryNode of the input RepositoryNode. The root should
     * be a RepositoryNode with database infomation.
     *
     * @param repositoryNode RepositoryNode
     * @return RepositoryNode
     */
    private RepositoryNode getRoot(RepositoryNode repositoryNode) {
        return SQLBuilderRepositoryNodeManager.getRoot(repositoryNode);
    }

}
