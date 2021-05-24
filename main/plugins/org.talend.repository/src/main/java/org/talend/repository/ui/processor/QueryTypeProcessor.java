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
package org.talend.repository.ui.processor;

import org.eclipse.jface.viewers.Viewer;
import org.talend.core.model.metadata.MetadataTable;
import org.talend.core.model.metadata.Query;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.repository.model.RepositoryNode;

/**
 * bqian TypeProcessor for Query. <br/>
 *
 * $Id: talend.epf 1 2006-09-29 17:06:40 +0000 (ææäº, 29 ä¹æ 2006) nrousseau $
 *
 */
public class QueryTypeProcessor extends SingleTypeProcessor {

    /**
     * bqian RepositoryTypeProcessor constructor comment.
     *
     * @param repositoryType
     */
    public QueryTypeProcessor(String repositoryType) {
        super(repositoryType);
    }

    @Override
    protected ERepositoryObjectType getType() {
        return ERepositoryObjectType.METADATA_CONNECTIONS;
    }

    @Override
    public boolean isSelectionValid(RepositoryNode node) {
        if (node.getObject() instanceof Query) {
            return true;
        }
        return false;
    }

    @Override
    protected boolean selectRepositoryNode(Viewer viewer, RepositoryNode parentNode, RepositoryNode node) {
        if (node.getObject() != null && (node.getObject() instanceof MetadataTable)) {
            return false;
        }
        if (isCDCConnection(node)) {
            return false;
        }
        if (node.getObject() == null && node.getParent() != null) {
            if (ERepositoryObjectType.METADATA.equals(node.getParent().getContentType())) {
                return false;
            }
        }
        return true;
    }

}
