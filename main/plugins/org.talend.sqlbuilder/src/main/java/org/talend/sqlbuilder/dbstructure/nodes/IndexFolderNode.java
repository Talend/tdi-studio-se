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

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.sourceforge.squirrel_sql.fw.sql.ITableInfo;

import org.talend.sqlbuilder.Messages;
import org.talend.sqlbuilder.SqlBuilderPlugin;

/**
 * DOC dev class global comment. Detailled comment <br/>
 *
 * $Id: IndexFolderNode.java,v 1.4 2006/11/01 05:40:59 peiqin.hou Exp $
 *
 */
public class IndexFolderNode extends AbstractFolderNode {

    private static final int COLUMN_INDEX = 6;

    private ITableInfo ptableInfo;

    /**
     * Create new database table node.
     *
     * @param parent node
     * @param name of this node
     * @param sessionNode session for this node
     */
    public IndexFolderNode(INode parent, ITableInfo tableInfo) {

        ptableInfo = tableInfo;
        psessionNode = parent.getSession();
        pparent = parent;
        pname = Messages.getString("DatabaseStructureView.node.Indexes"); //$NON-NLS-1$
    }

    /**
     * @return List of column names for this table.
     */
    @SuppressWarnings("unchecked")//$NON-NLS-1$
    public List getIndexNames() {

        List indexNames = new ArrayList();

        try {
            ResultSet resultSet = psessionNode.getMetaData().getIndexInfo(ptableInfo);
            while (resultSet.next()) {
                String name = resultSet.getString(COLUMN_INDEX);
                if (!(name == null || indexNames.contains(name))) {
                    indexNames.add(name);
                }
            }

        } catch (Exception e) {
            SqlBuilderPlugin.log(Messages.getString("IndexFolderNode.logMessage1"), e); //$NON-NLS-1$
        }
        return indexNames;
    }

    /**
     * @return Name.
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

        return "index_folder"; //$NON-NLS-1$
    }

    /**
     *
     *
     * @see org.talend.sqlbuilder.dbstructure.nodes.AbstractNode#loadChildren()
     */
    public void loadChildren() {

        try {
            Iterator it = getIndexNames().iterator();
            while (it.hasNext()) {
                addChildNode(new IndexNode(this, (String) it.next(), psessionNode, (TableNode) getParent()));
            }
        } catch (Exception e) {
            SqlBuilderPlugin.log(Messages.getString("IndexFolderNode.logMessage2", getName()), e); //$NON-NLS-1$
        }
    }

}
