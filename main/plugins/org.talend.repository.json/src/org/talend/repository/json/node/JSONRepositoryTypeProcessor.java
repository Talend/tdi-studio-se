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
package org.talend.repository.json.node;

import org.eclipse.jface.viewers.Viewer;
import org.talend.core.model.metadata.MetadataTable;
import org.talend.core.model.properties.FolderItem;
import org.talend.core.model.properties.Item;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.model.repository.IRepositoryViewObject;
import org.talend.repository.model.IRepositoryNode;
import org.talend.repository.model.RepositoryNode;
import org.talend.repository.model.IRepositoryNode.ENodeType;
import org.talend.repository.model.IRepositoryNode.EProperties;
import org.talend.repository.ui.processor.SingleTypeProcessor;

/**
 * DOC zwzhao class global comment. Detailled comment
 */
public class JSONRepositoryTypeProcessor extends SingleTypeProcessor {

    public JSONRepositoryTypeProcessor(String repositoryType) {
        super(repositoryType);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.repository.ui.processor.SingleTypeProcessor#getType()
     */
    @Override
    protected ERepositoryObjectType getType() {
        return JSONRepositoryNodeType.JSON;
    }

    @Override
    protected boolean selectRepositoryNode(Viewer viewer, RepositoryNode parentNode, RepositoryNode node) {
        final String repositoryType = getRepositoryType();
        if (node == null || repositoryType == null) {
            return false;
        }

        ERepositoryObjectType repObjType = (ERepositoryObjectType) node.getProperties(EProperties.CONTENT_TYPE);
        if (repObjType == ERepositoryObjectType.REFERENCED_PROJECTS) {
            // if (node.getContentType() == ERepositoryObjectType.REFERENCED_PROJECTS) {
            return true;
        }

        if (node.getType() == ENodeType.SYSTEM_FOLDER) {
            return true;
        }

        if (node.getType() == ENodeType.SIMPLE_FOLDER) {
            return isValidFolder(node);
        }

        IRepositoryViewObject object = node.getObject();
        if (object == null || object.getProperty().getItem() == null) {
            return false;
        }
        if (object instanceof MetadataTable) {
            return false;
        }
        Item item = object.getProperty().getItem();
        if (item instanceof FolderItem) {
            return true;
        }

        return true;
    }

    private boolean isValidFolder(IRepositoryNode node) {
        if (node.getContentType() != null && node.getContentType().getType().equals(JSONRepositoryNodeType.JSON.getType())) {
            return true;
        }
        return false;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.talend.repository.ui.processor.SingleTypeProcessor#isSelectionValid(org.talend.repository.model.RepositoryNode
     * )
     */
    @Override
    public boolean isSelectionValid(RepositoryNode node) {
        final String repositoryType = getRepositoryType();
        if (node == null || node.getObjectType() == null || repositoryType == null) {
            return false;
        }

        return repositoryType.contains(node.getObjectType().getType());
    }

}
