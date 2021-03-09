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

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.talend.core.database.EDatabase4DriverClassName;
import org.talend.core.database.EDatabaseTypeName;
import org.talend.core.model.metadata.IMetadataConnection;
import org.talend.core.model.metadata.builder.ConvertionHelper;
import org.talend.core.model.metadata.builder.connection.DatabaseConnection;
import org.talend.core.model.metadata.builder.database.DriverShim;
import org.talend.core.model.metadata.builder.database.ExtractMetaDataUtils;
import org.talend.repository.model.RepositoryNode;
import org.talend.sqlbuilder.Messages;
import org.talend.sqlbuilder.SqlBuilderPlugin;
import org.talend.sqlbuilder.dbstructure.nodes.INode;
import org.talend.sqlbuilder.sessiontree.model.SessionTreeModel;
import org.talend.sqlbuilder.sessiontree.model.SessionTreeNode;

import net.sourceforge.sqlexplorer.IdentifierFactory;
import net.sourceforge.sqlexplorer.SQLAlias;
import net.sourceforge.squirrel_sql.fw.sql.ISQLAlias;
import net.sourceforge.squirrel_sql.fw.sql.SQLConnection;

/**
 * Detailled comment for this class. <br/>
 * $Id: SessionTreeNodeUtils.java,v 1.24 2006/11/08 09:58:55 peiqin.hou Exp $
 *
 * @author phou
 *
 */
public class SessionTreeNodeUtils {

    private static Set connections = new HashSet();

    // private static List<SessionTreeNode> sessionTreeNodes = new ArrayList<SessionTreeNode>();

    private static List<INode> catalogNodes = new ArrayList<INode>();

    private static List<INode> tableNodes = new ArrayList<INode>();

    private static List<INode> cachedAllNodes = new ArrayList<INode>();

    private static List<INode> cachedCurrentNodes = new ArrayList<INode>();

    /**
     * Get all CatelogNodes.
     *
     * @return CatelogNodes List.
     */
    public static List<INode> getCatalogNodes() {
        return catalogNodes;
    }

    /**
     * Get all TableNodes.
     *
     * @return TableNodes List.
     */
    public static List<INode> getTableNodes() {
        return tableNodes;
    }

    /**
     * dispose.
     */
    public static void dispose() {
        cachedAllNodes.clear();
        cachedCurrentNodes.clear();
        catalogNodes.clear();
        tableNodes.clear();
        disposeConnections();
    }

    public static SessionTreeNode getSessionTreeNode(DatabaseConnection dbconnection, RepositoryNode repositoryNode,
            String selectedContext) throws Exception {
        // hyWang add for bug 0007014
        IMetadataConnection iMetadataConnection = null;
        iMetadataConnection = ConvertionHelper.convert(dbconnection, false, selectedContext);
        iMetadataConnection.setAdditionalParams(ConvertionHelper.convertAdditionalParameters(dbconnection));
        String url = dbconnection.getURL();
        if (url == null || url.equals("")) {
            url = iMetadataConnection.getUrl();
        }

        // bug 17980
        SQLConnection connection = null;
        DriverShim wapperDriver = null;
        List list = createSQLConnection(dbconnection, selectedContext, iMetadataConnection);
        if (list != null && list.size() > 0) {
            for (int i = 0; i < list.size(); i++) {
                if (list.get(i) instanceof SQLConnection) {
                    connection = (SQLConnection) list.get(i);
                }
                if (list.get(i) instanceof DriverShim) {
                    wapperDriver = (DriverShim) list.get(i);
                }
            }
        }

        ISQLAlias alias = createSQLAlias("Repository Name", url, dbconnection.getUsername(), dbconnection //$NON-NLS-1$
                .getRawPassword(),
        // fix bug for 7014,added by hyWang
                dbconnection.getSID() == null || dbconnection.getSID().length() == 0 ? (dbconnection.getDatasourceName() == null
                        || dbconnection.getDatasourceName().length() == 0 ? "Database" //$NON-NLS-1$
                        : dbconnection.getDatasourceName()) : dbconnection.getSID());
        SessionTreeModel stm = new SessionTreeModel();
        SessionTreeNode session;
        if (wapperDriver != null
                && (iMetadataConnection.getDriverClass().equals(EDatabase4DriverClassName.JAVADB_EMBEDED.getDriverClass())
                        || iMetadataConnection.getDbType().equals(EDatabaseTypeName.JAVADB_EMBEDED.getDisplayName())
                        || iMetadataConnection.getDbType().equals(EDatabaseTypeName.JAVADB_DERBYCLIENT.getDisplayName())
                        || iMetadataConnection.getDbType().equals(EDatabaseTypeName.JAVADB_JCCJDBC.getDisplayName()) || iMetadataConnection
                        .getDbType().equals(EDatabaseTypeName.HSQLDB_IN_PROGRESS.getDisplayName()))) {
            session = stm.createSessionTreeNode(new SQLConnection[] { connection, connection }, alias, null,
                    dbconnection.getRawPassword(), repositoryNode, wapperDriver);
        } else {
            session = stm.createSessionTreeNode(new SQLConnection[] { connection, connection }, alias, null,
                    dbconnection.getRawPassword(), repositoryNode);
        }
        return session;
    }

    /**
     * @param url Connection Url
     * @param userName DB username
     * @param password DB password
     * @param databaseName databaseName
     * @param name RepositoryName
     * @return ISQLAlias
     */
    private static ISQLAlias createSQLAlias(String name, String url, String userName, String password, String databaseName) {
        SQLAlias alias = new SQLAlias(IdentifierFactory.getInstance().createIdentifier());
        try {
            alias.setName(databaseName);
            alias.setUrl(url);
            alias.setUserName(userName);
            alias.setPassword(password);
            alias.setSchemaFilterExpression(databaseName);
            alias.setFolderFilterExpression("Tables,Views,Synonyms"); //$NON-NLS-1$
        } catch (Exception e) {
            SqlBuilderPlugin.log(e.getMessage(), e);
        }
        return alias;
    }

    /**
     * DOC qianbing Comment method "createSQLConnection".
     *
     * @param dbType database Type
     * @param url url
     * @param userName userName
     * @param password password
     * @return SQLConnection
     * @throws Exception Exception
     */
    protected static SQLConnection createSQLConnection(String dbType, String url, String userName, String password)
            throws Exception {
        Class.forName(ExtractMetaDataUtils.getInstance().getDriverClassByDbType(dbType)).newInstance();
        ExtractMetaDataUtils.getInstance().checkDBConnectionTimeout();
        Connection connection = DriverManager.getConnection(url, userName, password);
        SQLConnection sqlConnection = new SQLConnection(connection, null);
        return sqlConnection;
    }

    private static List createSQLConnection(DatabaseConnection con, String selectedContext,
            IMetadataConnection iMetadataConnection) throws Exception {
        // bug 17980
        ExtractMetaDataUtils extractMeta = ExtractMetaDataUtils.getInstance();
        List list = extractMeta.getConnection(iMetadataConnection.getDbType(), iMetadataConnection.getUrl(),
                iMetadataConnection.getUsername(), iMetadataConnection.getPassword(), iMetadataConnection.getDatabase(),
                iMetadataConnection.getSchema(), iMetadataConnection.getDriverClass(), iMetadataConnection.getDriverJarPath(),
                iMetadataConnection.getDbVersionString(), iMetadataConnection.getAdditionalParams());
        SQLConnection sqlConnection = new SQLConnection(extractMeta.getConn(), null);
        if (sqlConnection != null) {
            list.add(sqlConnection);
        }
        return list;
    }

    /**
     * disposeConnections.
     */
    private static void disposeConnections() {
        SQLConnection connection = null;
        for (Iterator it = connections.iterator(); it.hasNext();) {
            connection = (SQLConnection) it.next();
            try {
                connection.close();
            } catch (Exception e) {
                SqlBuilderPlugin.log(Messages.getString("SessionTreeNodeUtils.logMessage"), e); //$NON-NLS-1$
            }
        }
        connections.clear();
    }
}
