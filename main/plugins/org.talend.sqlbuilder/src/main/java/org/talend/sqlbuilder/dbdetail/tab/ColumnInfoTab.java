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
package org.talend.sqlbuilder.dbdetail.tab;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import net.sourceforge.squirrel_sql.fw.sql.ITableInfo;

import org.talend.core.model.metadata.builder.database.ExtractMetaDataFromDataBase;
import org.talend.core.model.metadata.builder.database.ExtractMetaDataUtils;
import org.talend.sqlbuilder.Messages;
import org.talend.sqlbuilder.dataset.dataset.DataSet;
import org.talend.sqlbuilder.dbstructure.nodes.INode;
import org.talend.sqlbuilder.dbstructure.nodes.TableNode;
import org.talend.sqlbuilder.sessiontree.model.SessionTreeNode;

/**
 * @author Davy Vanherbergen
 *
 */
public class ColumnInfoTab extends AbstractDataSetTab {

    public String getLabelText() {
        return Messages.getString("DatabaseDetailView.Tab.ColumnInfo"); //$NON-NLS-1$
    }

    public DataSet getDataSet() throws Exception {

        INode node = getNode();

        if (node == null) {
            return null;
        }
        if (node.getSession() == null) {
            return null;
        }

        if (node instanceof TableNode) {
            TableNode tableNode = (TableNode) node;
            ITableInfo ti = tableNode.getTableInfo();
            if (tableNode.getTableInfo() == null) {
                return null;
            }
            ResultSet resultSet = null;
            SessionTreeNode treeNode = node.getSession();

            // For synonym table, should get the corresponding table.
            if (ti.getType().equals("SYNONYM")) { //$NON-NLS-1$

                String realTableName = ExtractMetaDataFromDataBase.getTableNameBySynonym(treeNode.getInteractiveConnection()
                        .getConnection(), ti.getSimpleName());

                resultSet = treeNode.getMetaData().getJDBCMetaData()
                        .getColumns(ti.getCatalogName(), ti.getSchemaName(), realTableName, "%"); //$NON-NLS-1$

            } else {
                // https://jira.talendforge.org/browse/TDI-28578
                String tableName = ti.getSimpleName();

                if (tableName.contains("/")) {
                    tableName = tableName.replaceAll("/", "//");
                }
                resultSet = node.getSession().getMetaData().getJDBCMetaData()
                        .getColumns(ti.getCatalogName(), ti.getSchemaName(), tableName, "%");
                // resultSet = node.getSession().getMetaData().getColumns(tableNode.getTableInfo());
            }

            DataSet dataSet = new DataSet(null, resultSet, new int[] { 4, 5, 6, 7, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18 });
            resultSet.close();
            return dataSet;
        }

        return null;
    }

    public String getStatusMessage() {
        return Messages.getString("DatabaseDetailView.Tab.ColumnInfo.status", getNode().getQualifiedName()); //$NON-NLS-1$
    }

    /**
     * Gets table name base on synonym.
     *
     * @param conn Connection
     * @param name synonym
     * @return a string representing real table name
     */
    public String getTableNameBySynonym(Connection conn, String name) {
        try {
            // This query is used for getting real table name from system tables, it is used only for Oracle.
            String sql = "select TABLE_NAME from USER_SYNONYMS where SYNONYM_NAME = '" + name + "'"; //$NON-NLS-1$ //$NON-NLS-2$
            Statement sta;
            sta = conn.createStatement();
            ExtractMetaDataUtils.getInstance().setQueryStatementTimeout(sta);
            ResultSet resultSet = sta.executeQuery(sql);
            while (resultSet.next()) {
                return resultSet.getString("TABLE_NAME"); //$NON-NLS-1$
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

}
