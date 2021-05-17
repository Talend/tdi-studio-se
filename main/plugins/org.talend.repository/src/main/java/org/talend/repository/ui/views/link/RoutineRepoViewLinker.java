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

import org.talend.core.model.repository.IRepositoryEditorInput;
import org.talend.core.repository.link.AbstractRepositoryEditorInputLinker;
import org.talend.repository.ui.actions.routines.RoutineEditorInput;

/**
 * DOC ggu class global comment. Detailled comment <br/>
 *
 * $Id: talend.epf 55206 2011-02-15 17:32:14Z mhirt $
 *
 */
public class RoutineRepoViewLinker extends AbstractRepositoryEditorInputLinker {

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.repository.link.AbstractRepositoryEditorInputLinker#getRepoEditorInputClass()
     */
    @Override
    protected Class<? extends IRepositoryEditorInput> getRepoEditorInputClass() {
        return RoutineEditorInput.class;
    }

}
