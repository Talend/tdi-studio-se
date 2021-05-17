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
package org.talend.repository.json.ui;

import org.talend.core.repository.model.ProjectRepositoryNode;
import org.talend.repository.json.node.JSONRepositoryNodeType;
import org.talend.repository.model.RepositoryNode;
import org.talend.repository.view.di.metadata.content.AbstractMetadataContentProvider;

/**
 * DOC wanghong class global comment. Detailled comment
 */
public class MetadataJSONContentProvider extends AbstractMetadataContentProvider {

    @Override
    protected RepositoryNode getTopLevelNodeFromProjectRepositoryNode(ProjectRepositoryNode projectNode) {
        return projectNode.getRootRepositoryNode(JSONRepositoryNodeType.JSON);
    }

}
