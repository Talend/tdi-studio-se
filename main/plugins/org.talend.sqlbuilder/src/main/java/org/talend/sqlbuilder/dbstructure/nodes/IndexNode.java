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
import java.util.Comparator;

import org.talend.sqlbuilder.Messages;
import org.talend.sqlbuilder.SqlBuilderPlugin;
import org.talend.sqlbuilder.sessiontree.model.SessionTreeNode;

/**
 * @author Davy Vanherbergen
 *
 */
public class IndexNode extends AbstractNode {

    private static final int SORT_INDEX = 10;

    private static final int COLUMN_NAME_INDEX = 9;

    private static final int INDEX_NAME_INDEX = 6;

    private TableNode pparentTable;

    public IndexNode(INode node, String name, SessionTreeNode session, TableNode parentTable) throws Exception {

        pparentTable = parentTable;
        pparent = node;
        psessionNode = session;
        pname = name;
        pimageKey = "Images.IndexIcon"; //$NON-NLS-1$
    }

    /**
     * @return Comparator.
     */
    public Comparator getComparator() {

        // we don't want any sorting here.
        return null;
    }

    /**
     * @return QualifierName.
     */
    public String getQualifiedName() {

        return pparent.getParent().getName() + "." + pname; //$NON-NLS-1$
    }

    /**
     * @return Type.
     */
    public String getType() {

        return "index"; //$NON-NLS-1$
    }

    /**
     * @return UniqueIdentifier.
     */
    public String getUniqueIdentifier() {

        return pparent.getParent().getQualifiedName() + "." + pname; //$NON-NLS-1$
    }

    /**
     * LoadChildren.
     */
    public void loadChildren() {

        try {
            ResultSet resultSet = psessionNode.getMetaData().getIndexInfo(pparentTable.getTableInfo());
            while (resultSet.next()) {

                String indexName = resultSet.getString(INDEX_NAME_INDEX);
                String columnName = resultSet.getString(COLUMN_NAME_INDEX);
                String sort = resultSet.getString(SORT_INDEX);

                if (indexName != null && indexName.equalsIgnoreCase(pname)) {
                    ColumnNode col = new ColumnNode(this, columnName, psessionNode, pparentTable, true);
                    if (sort == null || sort.equalsIgnoreCase("A")) { //$NON-NLS-1$
                        col.setLabelDecoration("ASC"); //$NON-NLS-1$
                    } else {
                        col.setLabelDecoration("DESC"); //$NON-NLS-1$
                    }
                    addChildNode(col);
                }
            }

        } catch (Exception e) {
            SqlBuilderPlugin.log(Messages.getString("IndexNode.logMessage"), e); //$NON-NLS-1$
        }

    }

}
