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
package org.talend.repository.ui.views.link;

import org.eclipse.ui.IEditorInput;
import org.talend.core.model.properties.Item;
import org.talend.core.model.properties.SQLPatternItem;
import org.talend.core.model.repository.IRepositoryEditorInput;
import org.talend.core.repository.link.AbstractRepositoryEditorInputLinker;
import org.talend.core.repository.ui.editor.RepositoryEditorInput;

/**
 * DOC ggu class global comment. Detailled comment <br/>
 *
 * $Id: talend.epf 55206 2011-02-15 17:32:14Z mhirt $
 *
 */
public class SQLTemplateRepoViewLinker extends AbstractRepositoryEditorInputLinker {

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.repository.link.AbstractRepositoryEditorInputLinker#getRepoEditorInputClass()
     */
    @Override
    protected Class<? extends IRepositoryEditorInput> getRepoEditorInputClass() {
        return RepositoryEditorInput.class; // SQL template use the normal RepositoryEditorInput
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.repository.link.AbstractRepositoryEditorInputLinker#isRelation(org.eclipse.ui.IEditorInput,
     * java.lang.String)
     */
    @Override
    public boolean isRelation(IEditorInput editorInput, String repoNodeProjectLabel, String repoNodeId) {
        if (editorInput instanceof RepositoryEditorInput) {
            RepositoryEditorInput repoEditorInput = (RepositoryEditorInput) editorInput;
            Item item = repoEditorInput.getItem();
            if (item instanceof SQLPatternItem) { // must be SQL template item
                return super.isRelation(editorInput, repoNodeProjectLabel, repoNodeId);
            }

        }
        return false;
    }

}
