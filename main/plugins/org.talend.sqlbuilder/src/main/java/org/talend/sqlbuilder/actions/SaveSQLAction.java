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

import org.eclipse.jface.resource.ImageDescriptor;
import org.talend.core.model.metadata.builder.connection.Query;
import org.talend.core.sqlbuilder.util.ConnectionParameters;
import org.talend.repository.model.RepositoryNode;
import org.talend.sqlbuilder.dbstructure.SqlBuilderRepositoryObject;
import org.talend.sqlbuilder.util.ImageUtil;

/**
 * qzhang class global comment. Detailled comment <br/>
 *
 */
public class SaveSQLAction extends AbstractEditorAction {

    private ImageDescriptor image = ImageUtil.getDescriptor("Images.SaveSQLIcon"); //$NON-NLS-1$

    private RepositoryNode repositoryNode;

    private Query query;

    /**
     * qzhang SaveSQLAction constructor comment.
     */
    public SaveSQLAction(RepositoryNode repositoryNodeInput, ConnectionParameters query) {
        this.query = query.getQueryObject();
        this.repositoryNode = repositoryNodeInput;
        SqlBuilderRepositoryObject o = (SqlBuilderRepositoryObject) repositoryNode.getObject();
        boolean isBuildin = o.isBuildIn() || query.isNodeReadOnly();
        setEnabled(!isBuildin);
        this.setImageDescriptor(image);
        this.setToolTipText(getText());
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.sqlbuilder.actions.AbstractEditorAction#getText()
     */
    @Override
    public String getText() {

        return "Save"; //$NON-NLS-1$
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.sqlbuilder.actions.AbstractEditorAction#run()
     */
    @Override
    public void run() {

        Query query2 = editor.doSaveSQL(query, false);
        if (query2 != null) {
            query = query2;
        }
        this.getEditor().getDialog().notifySQLBuilder(repositoryNode.getObject());
    }

    public void setQuery(Query query) {
        this.query = query;
    }

}
