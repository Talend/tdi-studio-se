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
package org.talend.sqlbuilder.dbstructure.nodes;

import java.util.Comparator;
import java.util.Iterator;

import net.sourceforge.squirrel_sql.fw.sql.ITableInfo;

import org.talend.sqlbuilder.Messages;
import org.talend.sqlbuilder.SqlBuilderPlugin;

/**
 * DOC dev class global comment. Detailled comment <br/>
 *
 * $Id: ColumnFolderNode.java,v 1.4 2006/11/01 05:40:59 peiqin.hou Exp $
 *
 */
public class ColumnFolderNode extends AbstractFolderNode {

    /**
     * Create new database table node.
     *
     * @param parent node
     * @param name of this node
     * @param sessionNode session for this node
     */
    public ColumnFolderNode(INode parent, ITableInfo tableInfo) {

        psessionNode = parent.getSession();
        pparent = parent;
        pname = Messages.getString("DatabaseStructureView.node.Columns"); //$NON-NLS-1$
    }

    /**
     * Sort columns: PK - FK - Name..
     *
     * @return Comparator.
     */
    public Comparator getComparator() {

        return new Comparator() {

            public int compare(Object arg0, Object arg1) {

                if (arg0 == null || arg1 == null) {
                    return 0;
                }
                ColumnNode node1 = (ColumnNode) arg0;
                ColumnNode node2 = (ColumnNode) arg1;

                if (node1.isPrimaryKey() && !node2.isPrimaryKey()) {
                    return -1;
                }
                if (!node1.isPrimaryKey() && node2.isPrimaryKey()) {
                    return 1;
                }
                if (node1.isForeignKey() && !node2.isForeignKey()) {
                    return -1;
                }
                if (!node1.isForeignKey() && node2.isForeignKey()) {
                    return 1;
                }

                return node1.getName().compareTo(node2.getName());
            }

        };
    }

    /**
     * @return name.
     */
    public String getName() {

        return pname;
    }

    /**
     * @return Qualified table name
     */
    public String getQualifiedName() {

        return getParent().getQualifiedName() + "." + getType(); //$NON-NLS-1$
    }

    /**
     * Returns the table info type as the type for this node.
     *
     * @return Type.
     * @see org.talend.sqlbuilder.dbstructure.nodes.INode#getType()
     */
    public String getType() {

        return "column_folder"; //$NON-NLS-1$
    }

    /**
     *
     *
     * @see org.talend.sqlbuilder.dbstructure.nodes.AbstractNode#loadChildren()
     */
    public void loadChildren() {

        try {
            Iterator it = ((TableNode) pparent).getColumnNames().iterator();
            while (it.hasNext()) {
                addChildNode(new ColumnNode(this, (String) it.next(), psessionNode, (TableNode) pparent, true));
            }
        } catch (Exception e) {
            SqlBuilderPlugin.log(Messages.getString("ColumnFolderNode.logMessage", getName()), e); //$NON-NLS-1$
        }
    }

}
