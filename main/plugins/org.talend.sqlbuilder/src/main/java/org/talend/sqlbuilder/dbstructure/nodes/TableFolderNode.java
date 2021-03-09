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

import net.sourceforge.squirrel_sql.fw.sql.ITableInfo;

import org.talend.sqlbuilder.Messages;
import org.talend.sqlbuilder.SqlBuilderPlugin;
import org.talend.sqlbuilder.sessiontree.model.SessionTreeNode;

/**
 * TableTypeNode can represents a parent node for VIEW, TABLE, .. depending on what the database supports.
 *
 * @author Davy Vanherbergen
 *
 */
public class TableFolderNode extends AbstractFolderNode {

    private ITableInfo[] pallTables;

    private String porigName;

    /**
     * Create new database table object type node (view, table, etc...).
     *
     * @param parent node
     * @param name of this node
     * @param sessionNode session for this node
     */
    public TableFolderNode(INode parent, String name, SessionTreeNode sessionNode, ITableInfo[] tables) {

        pallTables = tables;
        psessionNode = sessionNode;
        pparent = parent;
        porigName = name;

        // cleanup the names a little
        String[] words = porigName.split(" "); //$NON-NLS-1$
        pname = ""; //$NON-NLS-1$
        for (int i = 0; i < words.length; i++) {
            pname = pname + words[i].substring(0, 1).toUpperCase() + words[i].substring(1).toLowerCase() + " "; //$NON-NLS-1$
        }
        pname = pname.trim();

        if (pname.equals("View")) { //$NON-NLS-1$
            pname = Messages.getString("DatabaseStructureView.view"); //$NON-NLS-1$
        }
        if (pname.equals("Table")) { //$NON-NLS-1$
            pname = Messages.getString("DatabaseStructureView.table"); //$NON-NLS-1$
        }
        if (pname.equals("Synonym")) { //$NON-NLS-1$
            pname = Messages.getString("DatabaseStructureView.synonym"); //$NON-NLS-1$
        }
    }

    /**
     * @return Name.
     */
    public String getName() {

        return pname;
    }

    /**
     * @return QualifiedName.
     */
    public String getQualifiedName() {

        return porigName;
    }

    /**
     * Returns the type for this node. The type is always suffixed with "_FOLDER".
     *
     * @return Type.
     * @see org.talend.sqlbuilder.dbstructure.nodes.INode#getType()
     */
    public String getType() {

        return porigName + "_FOLDER"; //$NON-NLS-1$
    }

    /**
     * Load all the children of this table type.
     *
     * @see org.talend.sqlbuilder.dbstructure.nodes.AbstractNode#loadChildren()
     */
    public void loadChildren() {

        try {

            // add child nodes for all relevant tables
            for (int i = 0; i < pallTables.length; i++) {
                if (!isExcludedByFilter(pallTables[i].getSimpleName())) {
                    addChildNode(new TableNode(this, pallTables[i].getSimpleName(), psessionNode, pallTables[i]));
                }
            }

        } catch (Throwable e) {
            SqlBuilderPlugin.log(Messages.getString("TableFolderNode.logMessage") + pname, e); //$NON-NLS-1$
        }
    }

    // /**
    // * @return ChildNodes.
    // */
    // @Override
    // public INode[] getChildNodes() {
    // INode[] nodesInDB = super.getChildNodes();
    // RepositoryNode root = psessionNode.getRepositoryNode();
    // if (root == null) {
    // return nodesInDB;
    // }
    // List<RepositoryNode> repositoryNodes = root.getChildren();
    //
    // Map<String, INode> allNodes = new HashMap<String, INode>();
    //
    // //add db nodes.
    // if (nodesInDB != null) {
    // for (INode node : nodesInDB) {
    // allNodes.put(node.getLabelText(), node);
    // }
    // }
    //
    // // Set<String> repositoryTableNames = new HashSet<String>();
    // for (RepositoryNode repositoryNode : repositoryNodes) {
    // String repositoryName = repositoryNode.getProperties(EProperties.LABEL).toString();
    // String tableSourceName = TableNode.getMetadataTable(repositoryNode).getSourceName();
    // if (tableSourceName != null) {
    // tableSourceName = tableSourceName.replaceAll("_", "-");
    // }
    // if (!allNodes.keySet().contains(repositoryNode.getProperties(EProperties.LABEL))
    // && !allNodes.keySet().contains(tableSourceName)) {
    // allNodes.put(repositoryName, convert2TableNode(repositoryNode));
    // } else {
    // TableNode tNode = (TableNode) allNodes.get(tableSourceName);
    // tNode.setRepositoryName(repositoryName);
    // tNode.setCurrentRepositoryNode(repositoryNode);
    // tNode.setSourceName(tableSourceName);
    // SessionTreeNodeUtils.getTableNodes().add(tNode);
    // }
    // }
    //
    // return allNodes.values().toArray(new INode[]{});
    // }

}
