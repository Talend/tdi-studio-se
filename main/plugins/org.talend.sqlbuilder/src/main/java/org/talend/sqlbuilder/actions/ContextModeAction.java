// ============================================================================
//
// Copyright (C) 2006-2019 Talend Inc. - www.talend.com
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

import org.talend.core.model.metadata.builder.connection.Query;
import org.talend.core.sqlbuilder.util.ConnectionParameters;
import org.talend.repository.model.RepositoryNode;
import org.talend.sqlbuilder.dbstructure.SqlBuilderRepositoryObject;

/**
 * DOC Administrator class global comment. Detailled comment
 */
public class ContextModeAction extends AbstractEditorAction {

    // ImageDescriptor img = ImageUtil.getDescriptor("Images.ExecSQLIcon");

    private RepositoryNode repositoryNode;

    private Query query;

    private boolean checked;

    // add by hyWang
    private ConnectionParameters parameters;

    public RepositoryNode getRepositoryNode() {
        return this.repositoryNode;
    }

    public void setRepositoryNode(RepositoryNode repositoryNode) {
        this.repositoryNode = repositoryNode;
    }

    public Query getQuery() {
        return this.query;
    }

    public void setQuery(Query query) {
        this.query = query;
    }

    public ContextModeAction(RepositoryNode repositoryNode, ConnectionParameters query) {
        this.repositoryNode = repositoryNode;
        this.parameters = query;
        this.query = query.getQueryObject();
        // modified by hyWang,for fix a bug which was found in development
        if (this.query == null) {
            if (this.parameters != null) {
                checked = this.parameters.getIfContextButtonCheckedFromBuiltIn();
            } else {
                checked = false;
            }
        } else {
            checked = this.query.isContextMode();
        }

        SqlBuilderRepositoryObject o = (SqlBuilderRepositoryObject) repositoryNode.getObject();
        boolean isBuildin = o.isBuildIn() || query.isNodeReadOnly();
        setEnabled(!isBuildin);
        this.setToolTipText(getText());
    }

    public ConnectionParameters getParameters() {
        return this.parameters;
    }

    @Override
    public String getText() {
        return "context mode"; //$NON-NLS-1$
    }

    @Override
    public boolean isChecked() {
        return this.checked;
    }

    @Override
    public void setChecked(boolean checked) {
        this.checked = checked;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.sqlbuilder.actions.AbstractEditorAction#run()
     */
    @Override
    public void run() {
        // do nothing
    }

}
