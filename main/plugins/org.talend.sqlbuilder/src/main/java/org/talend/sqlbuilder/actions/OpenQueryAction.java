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

import java.util.List;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.talend.core.model.metadata.builder.connection.Query;
import org.talend.core.model.repository.IRepositoryViewObject;
import org.talend.core.sqlbuilder.util.ConnectionParameters;
import org.talend.repository.model.IRepositoryNode.EProperties;
import org.talend.repository.model.RepositoryNode;
import org.talend.sqlbuilder.Messages;
import org.talend.sqlbuilder.dbstructure.DBTreeProvider.QueryRepositoryObject;
import org.talend.sqlbuilder.dbstructure.RepositoryNodeType;
import org.talend.sqlbuilder.repository.utility.SQLBuilderRepositoryNodeManager;
import org.talend.sqlbuilder.ui.ISQLBuilderDialog;

/**
 * DOC qianbing class global comment. Detailled comment <br/>
 *
 * $Id: talend-code-templates.xml,v 1.3 2006/11/01 05:38:28 nicolas Exp $
 *
 */
public class OpenQueryAction extends OpenNewEditorAction {

    private final ISQLBuilderDialog dialog;

    /**
     * DOC qianbing OpenQueryAction constructor comment.
     */
    public OpenQueryAction(TreeViewer selectionProvider, ISQLBuilderDialog d, ConnectionParameters connParam) {
        super(selectionProvider, d, connParam, false);
        this.dialog = d;
        setText(Messages.getString("DBStructureComposite.OpenQuery")); //$NON-NLS-1$
        setToolTipText(Messages.getString("DBStructureComposite.OpenQuery")); //$NON-NLS-1$
    }

    SQLBuilderRepositoryNodeManager repositoryNodeManager = new SQLBuilderRepositoryNodeManager();

    @Override
    public void run() {
        RepositoryNode node = (RepositoryNode) getStructuredSelection().getFirstElement();
        if (node == null) {
            return;
        }
        if (node.getProperties(EProperties.CONTENT_TYPE) == RepositoryNodeType.QUERY) {
            List<String> repositoryName = repositoryNodeManager.getALLReposotoryNodeNames();
            ConnectionParameters connectionParameters = new ConnectionParameters();
            IRepositoryViewObject repViewObject = node.getObject();
            if (repViewObject instanceof QueryRepositoryObject) {
                QueryRepositoryObject queryRepObj = (QueryRepositoryObject) repViewObject;
                Query query = queryRepObj.getQuery();
                connectionParameters.setQueryObject(query);
                dialog.setConnParameters(connectionParameters);
            }
            connectionParameters.setQuery(dialog.getConnParameters().getQuery());
            connectionParameters.setShowDesignerPage(false);
            dialog.openEditor(node, repositoryName, connectionParameters, false);
        }
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
        setEnabled(enabled);
    }

}
