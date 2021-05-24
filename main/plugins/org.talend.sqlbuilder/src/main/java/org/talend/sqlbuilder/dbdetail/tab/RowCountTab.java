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

import org.talend.sqlbuilder.Messages;
import org.talend.sqlbuilder.dataset.dataset.DataSet;
import org.talend.sqlbuilder.dbstructure.nodes.INode;
import org.talend.sqlbuilder.dbstructure.nodes.TableNode;

/**
 * @author Davy Vanherbergen
 *
 */
public class RowCountTab extends AbstractDataSetTab {

    public String getLabelText() {
        return Messages.getString("DatabaseDetailView.Tab.RowCount"); //$NON-NLS-1$
    }

    public DataSet getDataSet() throws Exception {

        String nodeName = getNode().toString();
        INode node = getNode();

        if (node == null) {
            return null;
        }
        if (node.getSession() == null) {
            return null;
        }
        if (node instanceof TableNode) {
            TableNode tableNode = (TableNode) getNode();
            nodeName = tableNode.getQualifiedName();
        }

        return new DataSet(null, "select count(*) from " + nodeName, null, getNode().getSession().getInteractiveConnection()); //$NON-NLS-1$
    }

    public String getStatusMessage() {
        return Messages.getString("DatabaseDetailView.Tab.RowCount.status", getNode().getQualifiedName()); //$NON-NLS-1$ //$NON-NLS-2$
    }
}
