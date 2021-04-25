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
import org.talend.sqlbuilder.Messages;
import org.talend.sqlbuilder.dbstructure.SqlBuilderRepositoryObject;
import org.talend.sqlbuilder.util.ImageUtil;

/**
 * This class is used for saving current editor's sql text into dbstructure composite. <br/>
 *
 * $Id: SaveSQLAction,v 1.0 2006/11/15 05:38:28 ftang Exp $
 *
 */
public class SaveAsSQLAction extends AbstractEditorAction {

    private ImageDescriptor image = ImageUtil.getDescriptor("Images.SaveAsSQLIcon"); //$NON-NLS-1$

    private RepositoryNode repositoryNode;

    private Query query;

    public SaveAsSQLAction(RepositoryNode repositoryNodeInput, ConnectionParameters query) {
        this.query = query.getQueryObject();
        this.repositoryNode = repositoryNodeInput;
        SqlBuilderRepositoryObject o = (SqlBuilderRepositoryObject) repositoryNode.getObject();
        boolean isBuildin = o.isBuildIn() || query.isNodeReadOnly();
        setEnabled(!isBuildin);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.jface.action.Action#getImageDescriptor()
     */
    public ImageDescriptor getImageDescriptor() {
        return image;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.sqlbuilder.actions.AbstractEditorAction#getText()
     */
    public String getText() {
        return Messages.getString("SQLEditor.Actions.SaveSQL"); //$NON-NLS-1$
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.sqlbuilder.actions.AbstractEditorAction#getToolTipText()
     */
    public String getToolTipText() {
        return Messages.getString("SQLEditor.Actions.SaveSQLToolTip"); //$NON-NLS-1$
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.sqlbuilder.actions.AbstractEditorAction#run()
     */
    public void run() {
        editor.doSaveSQL(query, true);
    };
}
