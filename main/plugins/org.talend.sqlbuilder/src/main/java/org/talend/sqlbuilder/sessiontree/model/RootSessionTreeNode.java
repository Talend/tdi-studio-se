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

import java.util.ArrayList;

/**
 * The root node in the session tree; It shows the number of active sessions.
 */
public class RootSessionTreeNode implements ISessionTreeNode {

    private static final int CAPACITY = 10;

    private ArrayList ls = new ArrayList(CAPACITY);

    public RootSessionTreeNode() {
    }

    /**
     * @return SessionTreeNode[]
     */
    @SuppressWarnings("unchecked")//$NON-NLS-1$
    public SessionTreeNode[] getSessionTreeNodes() {
        return (SessionTreeNode[]) ls.toArray(new SessionTreeNode[0]);
    }

    /**
     * @return Children.
     */
    public Object[] getChildren() {
        return ls.toArray();
    }

    /**
     * @return Parent.
     * @see org.gnu.amaz.ISessionTreeNode#getParent()
     */
    public Object getParent() {
        return null;
    }

    /**
     * Override toString method.
     *
     * @return String.
     */
    public String toString() {
        int sz = ls.size();
        if (sz == 0) {
            return ""; //$NON-NLS-1$
        } else if (sz == 1) {
            return ""; //$NON-NLS-1$
        } else {
            return "" + ls.size() + ""; //$NON-NLS-1$ //$NON-NLS-2$
        }
    }

    /**
     * @param node SessionTreeNode.
     */
    @SuppressWarnings("unchecked")//$NON-NLS-1$
    public void add(ISessionTreeNode node) {
        ls.add(node);
    }

    /**
     * @param nd SessionTreeNode.
     */
    public void remove(SessionTreeNode nd) {
        ls.remove(nd);
    }

    /**
     * Close All Connections.
     */
    public void closeAllConnections() {
        int s = ls.size();
        Object[] obj = ls.toArray();
        for (int i = 0; i < s; i++) {
            ((SessionTreeNode) obj[i]).close();
        }
    }
}
