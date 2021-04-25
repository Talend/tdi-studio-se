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
package org.talend.sqlbuilder.actions;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.actions.SelectionProviderAction;
import org.talend.core.model.metadata.builder.connection.Query;
import org.talend.repository.model.RepositoryNode;
import org.talend.repository.model.IRepositoryNode.EProperties;
import org.talend.sqlbuilder.Messages;
import org.talend.sqlbuilder.dbstructure.RepositoryNodeType;
import org.talend.sqlbuilder.dbstructure.DBTreeProvider.QueryRepositoryObject;
import org.talend.sqlbuilder.repository.utility.SQLBuilderRepositoryNodeManager;
import org.talend.sqlbuilder.ui.ISQLBuilderDialog;

/**
 * DOC qianbing class global comment. Detailled comment <br/>
 *
 * $Id: talend-code-templates.xml,v 1.3 2006/11/01 05:38:28 nicolas Exp $
 *
 */
public class DeleteQueryAction extends SelectionProviderAction {

    ISQLBuilderDialog dialog;

    /**
     * DOC qianbing DeleteQueryAction constructor comment.
     */
    public DeleteQueryAction(ISelectionProvider provider, ISQLBuilderDialog d) {
        super(provider, Messages.getString("DBStructureComposite.DeleteQuery")); //$NON-NLS-1$
        dialog = d;
    }

    @Override
    public void selectionChanged(IStructuredSelection selection) {
        boolean enabled = true;
        if (selection.size() != 1) {
            enabled = false;
        } else {
            RepositoryNode node = (RepositoryNode) selection.getFirstElement();
            RepositoryNodeType type = (RepositoryNodeType) node.getProperties(EProperties.CONTENT_TYPE);
            if (type != RepositoryNodeType.QUERY) {
                enabled = false;
            }
        }
        enabled = enabled && !dialog.getConnParameters().isNodeReadOnly();
        setEnabled(enabled);
    }

    SQLBuilderRepositoryNodeManager repositoryNodeManager = new SQLBuilderRepositoryNodeManager();

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.jface.action.Action#run()
     */
    @Override
    public void run() {

        RepositoryNode node = (RepositoryNode) getStructuredSelection().getFirstElement();

        QueryRepositoryObject object = (QueryRepositoryObject) node.getObject();
        Query query = object.getQuery();

        // Finds the root
        node = SQLBuilderRepositoryNodeManager.getRoot(node);
        List<Query> queries = new ArrayList<Query>();
        queries.add(query);
        repositoryNodeManager.deleteQueries(node, queries);

        dialog.refreshNode(node);
        dialog.notifySQLBuilder(node.getObject());
    }
}
