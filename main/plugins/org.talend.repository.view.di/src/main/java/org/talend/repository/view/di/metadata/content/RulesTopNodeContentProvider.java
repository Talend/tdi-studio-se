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
package org.talend.repository.view.di.metadata.content;

import org.eclipse.jface.viewers.Viewer;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.repository.model.ProjectRepositoryNode;
import org.talend.repository.model.RepositoryNode;

public class RulesTopNodeContentProvider extends AbstractMetadataContentProvider {

    /*
     * (non-Javadoc)
     *
     * @see
     * org.talend.repository.viewer.content.ProjectRepoChildrenNodeContentProvider#getTopLevelNodeFromProjectRepositoryNode
     * (org.talend.repository.model.ProjectRepositoryNode)
     */
    @Override
    protected RepositoryNode getTopLevelNodeFromProjectRepositoryNode(ProjectRepositoryNode projectRepositoryNode) {
        return projectRepositoryNode.getRootRepositoryNode(ERepositoryObjectType.METADATA_RULES_MANAGEMENT);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.talend.repository.viewer.content.ProjectRepoAbstractContentProvider#inputChanged(org.eclipse.jface.viewers
     * .Viewer, java.lang.Object, java.lang.Object)
     */
    @Override
    public void inputChanged(Viewer arg0, Object arg1, Object arg2) {
        // do nothing cause the is an empty node
    }

    @Override
    protected Object[] getRepositoryNodeChildren(RepositoryNode repositoryNode) {
        return NO_CHILDREN;
    }

    @Override
    protected void setupDeleteFolderListener(ProjectRepositoryNode projRepo) {
        // do nothing caus no need to refresh anything
    }

    @Override
    public void resetTopLevelNode(RepositoryNode aTopLevelNode) {
        // not need re-init it.
        // super.initAndClear();
    }
}
