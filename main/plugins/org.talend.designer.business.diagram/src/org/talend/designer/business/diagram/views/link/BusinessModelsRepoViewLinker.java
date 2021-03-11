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
package org.talend.designer.business.diagram.views.link;

import org.eclipse.gmf.runtime.diagram.ui.resources.editor.ide.document.FileEditorInputProxy;
import org.eclipse.ui.IEditorInput;
import org.talend.core.model.repository.IRepositoryEditorInput;
import org.talend.core.repository.link.AbstractRepositoryEditorInputLinker;
import org.talend.core.repository.ui.editor.RepositoryEditorInput;
import org.talend.repository.model.RepositoryNode;

/**
 * DOC ggu class global comment. Detailled comment <br/>
 *
 * $Id: talend.epf 55206 2011-02-15 17:32:14Z mhirt $
 *
 */
public class BusinessModelsRepoViewLinker extends AbstractRepositoryEditorInputLinker {

    /*
     * (non-Javadoc)
     *
     * @see
     * org.talend.core.repository.link.AbstractRepositoryEditorInputLinker#getRelationNode(org.eclipse.ui.IEditorInput)
     */
    @Override
    public RepositoryNode getRelationNode(IEditorInput editorInput) {
        return super.getRelationNode(getRealEditorInput(editorInput));
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.repository.link.AbstractRepositoryEditorInputLinker#isRelation(org.eclipse.ui.IEditorInput,
     * java.lang.String)
     */
    @Override
    public boolean isRelation(IEditorInput editorInput, String repoNodeProjectTechLabel, String repoNodeId) {
        return super.isRelation(getRealEditorInput(editorInput), repoNodeProjectTechLabel, repoNodeId);
    }

    private RepositoryEditorInput getRealEditorInput(IEditorInput editorInput) {
        IEditorInput repoEditorInput = null;
        if (editorInput != null && editorInput instanceof FileEditorInputProxy) {
            repoEditorInput = (IEditorInput) editorInput.getAdapter(getRepoEditorInputClass());
        }
        if (repoEditorInput != null && getRepoEditorInputClass().equals(repoEditorInput.getClass())) {
            return (RepositoryEditorInput) repoEditorInput;
        }
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.repository.link.AbstractRepositoryEditorInputLinker#getRepoEditorInputClass()
     */
    @Override
    protected Class<? extends IRepositoryEditorInput> getRepoEditorInputClass() {
        return RepositoryEditorInput.class;
    }

}
