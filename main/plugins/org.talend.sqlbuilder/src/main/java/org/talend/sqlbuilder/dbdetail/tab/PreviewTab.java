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

import java.sql.ResultSet;
import java.sql.Statement;

import org.talend.sqlbuilder.IConstants;
import org.talend.sqlbuilder.Messages;
import org.talend.sqlbuilder.SqlBuilderPlugin;
import org.talend.sqlbuilder.dataset.dataset.DataSet;
import org.talend.sqlbuilder.dbstructure.nodes.INode;
import org.talend.sqlbuilder.dbstructure.nodes.TableNode;

/**
 * @author Davy Vanherbergen
 *
 */
public class PreviewTab extends AbstractDataSetTab {

    public String getLabelText() {
        return Messages.getString("DatabaseDetailView.Tab.Preview"); //$NON-NLS-1$
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
            if (tableNode.getTableInfo() == null) {
                return null;
            }
            int maxResults = SqlBuilderPlugin.getDefault().getPluginPreferences().getInt(IConstants.PRE_ROW_COUNT);
            if (maxResults == 0) {
                maxResults = 50;
            }

            Statement statement = tableNode.getSession().getInteractiveConnection().createStatement();
            statement.setMaxRows(maxResults);
            statement.execute("select * from " + tableNode.getQualifiedName()); //$NON-NLS-1$
            ResultSet resultSet = statement.getResultSet();

            DataSet dataSet = new DataSet(null, resultSet, null);

            resultSet.close();
            statement.close();

            return dataSet;
        }

        return null;
    }

    public String getStatusMessage() {
        return Messages.getString("DatabaseDetailView.Tab.Preview.status", getNode().getQualifiedName()); //$NON-NLS-1$
    }
}
