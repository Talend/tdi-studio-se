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
package org.talend.repository.ui.actions.metadata;

import org.talend.commons.exception.PersistenceException;
import org.talend.commons.runtime.model.repository.ERepositoryStatus;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.repository.model.IProxyRepositoryFactory;
import org.talend.repository.model.RepositoryNode;

/**
 * DOC tguiu class global comment. Detailled comment <br/>
 *
 * $Id$
 *
 */
public class DeletionHelper {

    /**
     * DOC tguiu Comment method "isDeleted".
     *
     * @param repObj
     * @return
     * @throws PersistenceException
     */
    public static boolean isDeleted(RepositoryNode node) throws PersistenceException {
        IProxyRepositoryFactory repFactory = ProxyRepositoryFactory.getInstance();
        return (repFactory.getStatus(node.getObject()) == ERepositoryStatus.DELETED);
    }

    /**
     * DOC tguiu DeletionHelper constructor comment.
     */
    private DeletionHelper() {
        // TODO Auto-generated constructor stub
    }

}
