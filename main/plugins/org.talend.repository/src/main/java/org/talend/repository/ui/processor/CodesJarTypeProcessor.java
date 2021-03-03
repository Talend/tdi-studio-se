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
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.repository.model.IRepositoryNode.EProperties;
import org.talend.repository.model.RepositoryNode;

public class CodesJarTypeProcessor extends SingleTypeProcessor {

    private ERepositoryObjectType type;

    public CodesJarTypeProcessor(String repositoryType, ERepositoryObjectType type) {
        super(repositoryType);
        this.type = type;
    }

    @Override
    protected ERepositoryObjectType getType() {
        return type;
    }

    @Override
    protected boolean selectRepositoryNode(Viewer viewer, RepositoryNode parentNode, RepositoryNode node) {
        return node.getProperties(EProperties.CONTENT_TYPE) == type;
    }

}
