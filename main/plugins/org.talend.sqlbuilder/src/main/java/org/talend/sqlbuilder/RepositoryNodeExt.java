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
package org.talend.sqlbuilder;

import org.talend.core.model.repository.IRepositoryObject;
import org.talend.repository.model.RepositoryNode;

/**
 * qzhang class global comment. Detailled comment <br/>
 *
 */
public class RepositoryNodeExt extends RepositoryNode {

    /**
     * qzhang RepositoryNodeExt constructor comment.
     *
     * @param object
     * @param parent
     * @param type
     */
    public RepositoryNodeExt(IRepositoryObject object, RepositoryNode parent, ENodeType type) {
        super(object, parent, type);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.repository.model.RepositoryNode#hashCode()
     */
    @Override
    public int hashCode() {
        return super.hashCode();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.repository.model.RepositoryNode#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        return (this == obj);
    }

}
