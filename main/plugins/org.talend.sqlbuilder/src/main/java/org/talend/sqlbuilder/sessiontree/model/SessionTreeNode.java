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
package org.talend.sqlbuilder.sessiontree.model;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.sourceforge.sqlexplorer.IdentifierFactory;
import net.sourceforge.squirrel_sql.fw.id.IIdentifier;
import net.sourceforge.squirrel_sql.fw.sql.ISQLAlias;
import net.sourceforge.squirrel_sql.fw.sql.SQLConnection;
import net.sourceforge.squirrel_sql.fw.sql.SQLDatabaseMetaData;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.swt.widgets.Table;
import org.talend.commons.ui.runtime.exception.ExceptionHandler;
import org.talend.core.model.metadata.builder.connection.DatabaseConnection;
import org.talend.core.model.metadata.builder.database.DriverShim;
import org.talend.core.model.properties.ConnectionItem;
import org.talend.repository.model.IRepositoryNode.EProperties;
import org.talend.repository.model.RepositoryNode;
import org.talend.sqlbuilder.Messages;
import org.talend.sqlbuilder.SqlBuilderPlugin;
import org.talend.sqlbuilder.dbdetail.DetailTabManager;
import org.talend.sqlbuilder.dbstructure.DatabaseModel;
import org.talend.sqlbuilder.dbstructure.nodes.DatabaseNode;
import org.talend.sqlbuilder.dbstructure.nodes.INode;
import org.talend.sqlbuilder.sessiontree.model.utility.Dictionary;

/**
 * The SessionTreeNode represents one active database session.
 *
 * @modified Davy Vanherbergen
 */
public class SessionTreeNode implements ISessionTreeNode {

    private static final int CAPACITY = 10;

    private ISQLAlias alias;

    boolean assistanceEnabled;

    private SQLConnection backgroundConnection;

    private SQLConnection interactiveConnection;

    private boolean backgroundConnectionInUse = false;

    private List connectionNumberQueue = new ArrayList();

    private Dictionary dictionary = new Dictionary();

    private IIdentifier id = IdentifierFactory.getInstance().createIdentifier();

    private ListenerList listeners = new ListenerList();

    private SQLDatabaseMetaData metaData = null;

    private SessionTreeModel model;

    private int nextConnectionNumber = 0;

    private RootSessionTreeNode parent;

    private String password;

    private DatabaseModel dbModel;

    private ArrayList ls = new ArrayList(CAPACITY);

    Table table;

    private static final int COMMIT_REQUEST = -1;

    private static final int ROLLBACK_REQUEST = -2;

    private static final int CATALOG_CHANGE_REQUEST = -3;

    private String newCatalog;

    private RepositoryNode repositoryNode;

    DriverShim wapperDriver = null;

    /**
     *
     * @param conn
     * @param alias
     * @param md
     * @param monitor
     * @param password
     * @param repositoryNode
     * @throws InterruptedException
     */
    public SessionTreeNode(final SQLConnection[] conn, ISQLAlias alias, SessionTreeModel md, IProgressMonitor monitor,
            final String password, RepositoryNode repositoryNode) {
        interactiveConnection = conn[0];
        backgroundConnection = conn[1];
        this.alias = alias;
        dbModel = new DatabaseModel(this);
        this.model = md;
        this.parent = md.getRoot();
        parent.add(this);
        this.password = password;
        this.repositoryNode = repositoryNode;
    }

    /**
     * bug 17980 DOC SessionTreeNode constructor comment.
     *
     * @param conn
     * @param alias
     * @param md
     * @param monitor
     * @param password
     * @param repositoryNode
     * @param wapperDriver
     */
    public SessionTreeNode(final SQLConnection[] conn, ISQLAlias alias, SessionTreeModel md, IProgressMonitor monitor,
            final String password, RepositoryNode repositoryNode, DriverShim wapperDriver) {
        interactiveConnection = conn[0];
        backgroundConnection = conn[1];
        this.alias = alias;
        dbModel = new DatabaseModel(this);
        this.model = md;
        this.parent = md.getRoot();
        parent.add(this);
        this.password = password;
        this.repositoryNode = repositoryNode;
        this.wapperDriver = wapperDriver;
    }

    /**
     * Get DatabaseConnection from repositoryNode.
     *
     * @return database connection.
     */
    public DatabaseConnection getDatabaseConnection() {
        if (repositoryNode == null || repositoryNode.getObject() == null || repositoryNode.getObject().getProperty() == null
                || repositoryNode.getObject().getProperty().getItem() == null) {
            return null;
        }
        DatabaseConnection connection = (DatabaseConnection) ((ConnectionItem) repositoryNode.getObject().getProperty().getItem())
                .getConnection();

        return connection;
    }

    /**
     * @return dbModel.
     */
    public DatabaseModel getDbModel() {
        return dbModel;
    }

    /**
     * @param dbModel DatabaseModel.
     */
    public void setDbModel(DatabaseModel dbModel) {
        this.dbModel = dbModel;
    }

    /**
     * @return RepositoryNode.
     */
    public RepositoryNode getRepositoryNode() {
        return repositoryNode;
    }

    /**
     * @param n SessionTreeNode.
     */
    @SuppressWarnings("unchecked")//$NON-NLS-1$
    public void add(ISessionTreeNode n) {
        ls.add(n);
    }

    /**
     * Returns an SQLConnection. This connection should only be used to execute statements in the UI thread.
     *
     * @return SQLConnection.
     */
    public SQLConnection getInteractiveConnection() {

        return interactiveConnection;
    }

    /**
     * @param listener ISessionTreeClosedListener
     */
    public void addListener(ISessionTreeClosedListener listener) {
        listeners.add(listener);
    }

    /**
     * Close.
     */
    public void close() {

        // store dictionary
        dictionary.store();

        // clear detail tab cache
        DetailTabManager.clearCacheForSession(this);

        parent.remove(this);

        Object[] listensers = listeners.getListeners();

        for (int i = 0; i < listensers.length; ++i) {
            try {
                ((ISessionTreeClosedListener) listensers[i]).sessionTreeClosed();
            } catch (Throwable e) {
                SqlBuilderPlugin.log(e.getMessage(), e);
            }

        }
        model.modelChanged(null);
        try {
            interactiveConnection.close();
        } catch (Throwable e) {
            SqlBuilderPlugin.log(Messages.getString("SessionTreeNode.logMessage1"), e); //$NON-NLS-1$
        }
        try {
            backgroundConnection.close();
        } catch (Throwable e) {
            SqlBuilderPlugin.log(Messages.getString("SessionTreeNode.logMessage2"), e); //$NON-NLS-1$
        }

    }

    /**
     * Commit.
     */
    @SuppressWarnings("unchecked")//$NON-NLS-1$
    public synchronized void commit() {
        try {

            if (connectionNumberQueue.size() == 0 && !backgroundConnectionInUse) {
                // nothing is happening, so we can commit immediately
                backgroundConnection.commit();
            } else {
                // there are still queries in the queue, so we add the commit
                // request to the end of the queue.
                connectionNumberQueue.add(new Integer(COMMIT_REQUEST));
            }

        } catch (Throwable e) {
            SqlBuilderPlugin.log(Messages.getString("SessionTreeNode.logMessage3"), e); //$NON-NLS-1$
        }

    }

    // hyWang added for bug 0007062
    public synchronized boolean isConnectionClosed() {
        if (backgroundConnection != null) {
            try {
                return backgroundConnection.getConnection().isClosed();
            } catch (SQLException e) {
                ExceptionHandler.process(e);
            }
        }
        return true;
    }

    /**
     * @return SQLAlias.
     */
    public ISQLAlias getAlias() {
        return alias;
    }

    /**
     * @return Catalog.
     */
    public String getCatalog() {
        String cat = ""; //$NON-NLS-1$
        try {
            cat = interactiveConnection.getCatalog();
        } catch (Throwable e) {
            SqlBuilderPlugin.log(e.getMessage(), e);
        }
        return cat;
    }

    /**
     * @return Catalogs.
     */
    public String[] getCatalogs() {

        List catalogs = ((DatabaseNode) dbModel.getChildNodes()[0]).getCatalogs();
        String[] catalogNames = new String[catalogs.size()];

        Iterator it = catalogs.iterator();
        int i = 0;

        while (it.hasNext()) {
            INode node = (INode) it.next();
            if (node != null) {
                catalogNames[i] = node.toString();
                i++;
            }
        }

        return catalogNames;
    }

    /**
     * @see org.gnu.amaz.ISessionTreeNode#getChildren()
     * @return Children.
     */
    public Object[] getChildren() {
        return ls.toArray();
    }

    /**
     * @return CurrentConnectionPassword.
     */
    public String getCurrentConnectionPassword() {
        return password;
    }

    /**
     * @return Dictionary.
     */
    public Dictionary getDictionary() {
        return dictionary;
    }

    /**
     * @return wapperDriver
     */
    public DriverShim getWapperDriver() {
        return this.wapperDriver;
    }

    /**
     * @return Identifier.
     */
    public IIdentifier getIdentifier() {
        return id;
    }

    /**
     * @return SQLDatabaseMetaData
     */
    public SQLDatabaseMetaData getMetaData() {
        if (interactiveConnection == null) {
            return null;
        }

        if (metaData == null) {
            metaData = interactiveConnection.getSQLMetaData();
        }

        return metaData;
    }

    /**
     * @see org.gnu.amaz.ISessionTreeNode#getParent()
     * @return Parent.
     */
    public Object getParent() {
        return parent;
    }

    /**
     * Get the connection with queue number 'number'. This method will return null until the queue number has been
     * reached and the connection is available.
     *
     * @return SQLConnection.
     * @param number number.
     */
    public synchronized SQLConnection getQueuedConnection(Integer number) {

        if (backgroundConnectionInUse || number == null) {
            return null;
        }

        Integer currentNumber = (Integer) connectionNumberQueue.get(0);

        if (currentNumber.intValue() == number.intValue()) {
            backgroundConnectionInUse = true;
            return backgroundConnection;
        }

        return null;

    }

    /**
     * @return QueuedConnectionNumber
     */
    @SuppressWarnings("unchecked")//$NON-NLS-1$
    public synchronized Integer getQueuedConnectionNumber() {

        Integer number = new Integer(nextConnectionNumber);
        connectionNumberQueue.add(number);
        nextConnectionNumber++;
        return number;

    }

    /**
     * @return DatabaseNode.
     */
    public DatabaseNode getRoot() {
        return dbModel.getRoot();
    }

    /**
     * @return isAutoCommitMode
     */
    public boolean isAutoCommitMode() {
        boolean result = false;
        try {
            result = interactiveConnection.getAutoCommit();
        } catch (Throwable e) {
            SqlBuilderPlugin.log(e.getMessage(), e);
        }
        return result;
    }

    /**
     * Release the currently active connection so we can move on to the next one.
     *
     * @param number number.
     */
    public synchronized void releaseQueuedConnection(Integer number) {

        if (number == null) {
            return;
        }

        if (connectionNumberQueue.indexOf(number) == 0) {
            // release current connection
            backgroundConnectionInUse = false;
            connectionNumberQueue.remove(0);

            // check for pending commit or rollback requests

            while (connectionNumberQueue.size() > 0) {
                int nextNumber = ((Integer) connectionNumberQueue.get(0)).intValue();
                try {
                    if (nextNumber == COMMIT_REQUEST) {
                        connectionNumberQueue.remove(0);
                        backgroundConnection.commit();
                    } else if (nextNumber == ROLLBACK_REQUEST) {
                        connectionNumberQueue.remove(0);
                        backgroundConnection.rollback();
                    } else if (nextNumber == CATALOG_CHANGE_REQUEST) {
                        connectionNumberQueue.remove(0);
                        backgroundConnection.setCatalog(newCatalog);
                    } else {
                        break;
                    }
                } catch (Exception e) {
                    SqlBuilderPlugin.log(Messages.getString("SessionTreeNode.logMessage4"), e); //$NON-NLS-1$
                }
            }

        } else {
            // remove pending queue number
            connectionNumberQueue.remove(number);
        }

    }

    /**
     * @param n SessionTreeNode.
     */
    public void remove(ISessionTreeNode n) {
        ls.remove(n);
    }

    /**
     * rollback.
     */
    @SuppressWarnings("unchecked")//$NON-NLS-1$
    public synchronized void rollback() {
        try {

            if (connectionNumberQueue.size() == 0 && !backgroundConnectionInUse) {
                // nothing is happening, so we can rollback immediately
                backgroundConnection.rollback();
            } else {
                // there are still queries in the queue, so we add the rollback
                // request to the end of the queue.
                connectionNumberQueue.add(new Integer(ROLLBACK_REQUEST));
            }

        } catch (Throwable e) {
            SqlBuilderPlugin.log(Messages.getString("SessionTreeNode.logMessage5"), e); //$NON-NLS-1$

        }
    }

    /**
     * @param cat Catalog.
     * @exception SQLException SQLException.
     */
    @SuppressWarnings("unchecked")//$NON-NLS-1$
    public synchronized void setCatalog(String cat) throws SQLException {

        interactiveConnection.setCatalog(cat);

        if (connectionNumberQueue.size() == 0 && !backgroundConnectionInUse) {
            // nothing is happening, so we can change immediately
            backgroundConnection.setCatalog(cat);
        } else {
            // there are still queries in the queue, so we add the rollback
            // request to the end of the queue.
            newCatalog = cat;
            connectionNumberQueue.add(new Integer(CATALOG_CHANGE_REQUEST));
        }

    }

    /**
     * @return support Catalog or not.
     */
    public boolean supportsCatalogs() {
        return getRoot().supportsCatalogs();
    }

    /**
     * Returns connection alias name.
     *
     * @see java.lang.Object#toString()
     * @return String.
     */
    public String toString() {
        try {
            return alias.getName();

        } catch (java.lang.Throwable e) {
            SqlBuilderPlugin.log(Messages.getString("SessionTreeNode.logMessage6"), e); //$NON-NLS-1$
            return ""; //$NON-NLS-1$
        }
    }

    public String getRepositoryName() {
        if (repositoryNode == null) {
            return toString();
        } else {
            return (String) repositoryNode.getProperties(EProperties.LABEL);
        }
    }

}
