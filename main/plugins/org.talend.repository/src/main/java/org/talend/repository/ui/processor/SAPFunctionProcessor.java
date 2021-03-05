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

import java.util.List;

import org.eclipse.jface.viewers.Viewer;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.repository.model.repositoryObject.MetadataColumnRepositoryObject;
import org.talend.repository.model.IRepositoryNode;
import org.talend.repository.model.RepositoryNode;

/**
 * xye TypeProcessor for Query. <br/>
 *
 * $Id: talend.epf 1 2006-09-29 17:06:40 +0000 (ææäº, 29 ä¹æ 2006) nrousseau $
 *
 */
public class SAPFunctionProcessor extends SingleTypeProcessor {

    /**
     * bqian RepositoryTypeProcessor constructor comment.
     *
     * @param repositoryType
     */
    public SAPFunctionProcessor(String repositoryType) {
        super(repositoryType);
    }

    @Override
    protected ERepositoryObjectType getType() {
        return ERepositoryObjectType.METADATA_SAPCONNECTIONS;
    }

    /**
     * Modified by Marvin Wang on Jun. 19, 2012. Only table nodes can be selected.
     */
    @Override
    public boolean isSelectionValid(RepositoryNode node) {
        if (node.getObject().getRepositoryObjectType() == ERepositoryObjectType.METADATA_CON_TABLE) {
            return true;
        }
        return false;
    }

    @Override
    protected boolean selectRepositoryNode(Viewer viewer, RepositoryNode parentNode, RepositoryNode node) {
        // if (node.getObject() != null && (node.getObject() instanceof MetadataTable)) {
        // return false;
        // }
        return filterColumnFolderAndColumns(node);
    }

    /**
     * Added by Marvin Wang on Jun 19, 2012 for filtering the columns, do not show columns in tree. Refer to the subtask
     * TDI-21657.
     *
     * @param node
     * @return
     */
    private boolean filterColumnFolderAndColumns(RepositoryNode node) {
        if (node.getObject() != null && node.getObject() instanceof MetadataColumnRepositoryObject) {
            return false;
        }
        if (node.getObject() == null) {
            List<IRepositoryNode> nodes = node.getChildren();
            if (nodes != null && nodes.size() > 0) {
                for (IRepositoryNode child : nodes) {
                    if (child.getObject() instanceof MetadataColumnRepositoryObject) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

}
