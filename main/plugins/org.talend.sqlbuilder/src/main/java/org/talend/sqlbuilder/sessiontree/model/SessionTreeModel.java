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

import net.sourceforge.squirrel_sql.fw.sql.ISQLAlias;
import net.sourceforge.squirrel_sql.fw.sql.SQLConnection;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.ListenerList;
import org.talend.core.model.metadata.builder.database.DriverShim;
import org.talend.repository.model.RepositoryNode;
import org.talend.sqlbuilder.Messages;
import org.talend.sqlbuilder.SqlBuilderPlugin;

/**
 * Session Tree Model. <br/>
 *
 * $Id: SessionTreeModel.java,v 1.4 2006/11/01 06:45:49 peiqin.hou Exp $
 *
 */
public class SessionTreeModel implements ISessionTreeNode {

    RootSessionTreeNode root = new RootSessionTreeNode();

    private ListenerList listeners = new ListenerList();

    public SessionTreeModel() {

    }

    /**
     * @return RootSessionTreeNode
     */
    public RootSessionTreeNode getRoot() {

        return root;
    }

    /**
     * @return Object[]
     */
    public Object[] getChildren() {
        return new Object[] { root };
    }

    /**
     * @return Parent.
     */
    public Object getParent() {
        return root.getParent();
    }

    /**
     * @param conn Connection
     * @param alias SQLAlias.
     * @param monitor IProgressMonitor
     * @param pswd String
     * @param repositoryNode RepositoryNode
     * @return SessionTreeNode SessionTreeNode
     * @exception InterruptedException .
     */
    public SessionTreeNode createSessionTreeNode(SQLConnection[] conn, ISQLAlias alias, IProgressMonitor monitor, String pswd,
            RepositoryNode repositoryNode) throws InterruptedException {

        SessionTreeNode node = null;
        try {
            node = new SessionTreeNode(conn, alias, this, monitor, pswd, repositoryNode);
        } finally {
            modelChanged(node);
        }
        return node;
    }

    public SessionTreeNode createSessionTreeNode(SQLConnection[] conn, ISQLAlias alias, IProgressMonitor monitor, String pswd,
            RepositoryNode repositoryNode, DriverShim wapperDriver) throws InterruptedException {

        SessionTreeNode node = null;
        try {
            node = new SessionTreeNode(conn, alias, this, monitor, pswd, repositoryNode, wapperDriver);
        } finally {
            modelChanged(node);
        }
        return node;
    }

    /**
     * @param listener SessionTreeModelChangedListener
     */
    public void addListener(SessionTreeModelChangedListener listener) {
        listeners.add(listener);
    }

    /**
     * @param listener SessionTreeModelChangedListener
     */
    public void removeListener(SessionTreeModelChangedListener listener) {
        listeners.remove(listener);
    }

    /**
     * @param stn SessionTreeNode
     */
    public void modelChanged(SessionTreeNode stn) {

        Object[] ls = listeners.getListeners();

        for (int i = 0; i < ls.length; ++i) {
            try {
                ((SessionTreeModelChangedListener) ls[i]).modelChanged(stn);
            } catch (Throwable e) {
                SqlBuilderPlugin.log(Messages.getString("SessionTreeModel.logMessage"), e); //$NON-NLS-1$
            }

        }
    }
}
