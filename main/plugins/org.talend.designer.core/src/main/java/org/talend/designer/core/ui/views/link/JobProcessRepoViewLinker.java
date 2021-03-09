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
package org.talend.designer.core.ui.views.link;

import org.talend.core.model.repository.IRepositoryEditorInput;
import org.talend.core.repository.link.AbstractRepositoryEditorInputLinker;
import org.talend.designer.core.ui.editor.ProcessEditorInput;

/**
 * DOC ggu class global comment. Detailled comment
 */
public class JobProcessRepoViewLinker extends AbstractRepositoryEditorInputLinker {

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.repository.link.AbstractRepositoryEditorInputLinker#getRepoEditorInputClass()
     */
    @Override
    protected Class<? extends IRepositoryEditorInput> getRepoEditorInputClass() {
        return ProcessEditorInput.class;
    }

}
