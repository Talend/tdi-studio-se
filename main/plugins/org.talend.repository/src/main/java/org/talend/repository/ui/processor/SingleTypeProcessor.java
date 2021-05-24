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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.Viewer;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.repository.model.RepositoryNode;

/**
 * ggu class global comment. Detailled comment
 */
public abstract class SingleTypeProcessor extends MultiTypesProcessor {

    public SingleTypeProcessor(String repositoryType) {
        super(new String[] { repositoryType });
    }

    protected String getRepositoryType() {
        return getRepositoryTypes()[0];
    }

    @Override
    protected List<ERepositoryObjectType> getTypes() {
        List<ERepositoryObjectType> types = new ArrayList<ERepositoryObjectType>();
        ERepositoryObjectType type = getType();
        if (type != null) {
            types.add(type);
        }
        return types;
    }

    protected abstract ERepositoryObjectType getType();

    @Override
    public boolean isSelectionValid(RepositoryNode node) {
        if (node.getObjectType() == getType()) {
            return true;
        }
        return false;
    }

    @Override
    protected boolean selectRepositoryNode(Viewer viewer, RepositoryNode parentNode, RepositoryNode node) {
        if (node == null) {
            return false;
        }
        if (node.getContentType() == getType()) {
            return false;
        }
        if (isCDCConnection(node)) {
            return false;
        }
        return true;
    }

}
