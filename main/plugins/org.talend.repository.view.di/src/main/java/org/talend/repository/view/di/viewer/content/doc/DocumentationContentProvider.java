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
package org.talend.repository.view.di.viewer.content.doc;

import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.repository.model.ProjectRepositoryNode;
import org.talend.repository.model.RepositoryNode;
import org.talend.repository.viewer.content.ProjectRepoDirectChildrenNodeContentProvider;

/**
 * DOC ggu class global comment. Detailled comment
 */
public class DocumentationContentProvider extends ProjectRepoDirectChildrenNodeContentProvider {

    /*
     * (non-Javadoc)
     *
     * @see
     * org.talend.repository.viewer.content.ProjectRepoChildrenNodeContentProvider#getTopLevelNodeFromProjectRepositoryNode
     * (org.talend.repository.model.ProjectRepositoryNode)
     */
    @Override
    protected RepositoryNode getTopLevelNodeFromProjectRepositoryNode(ProjectRepositoryNode projectNode) {
        return projectNode.getRootRepositoryNode(getTopLevelNodeType());
    }

    protected ERepositoryObjectType getTopLevelNodeType() {
        return ERepositoryObjectType.DOCUMENTATION;
    }

    @Override
    protected void resetTopLevelNode(RepositoryNode topLevelNode) {
        super.resetTopLevelNode(topLevelNode);
    }

    // @Override
    // protected void initializeChildren(RepositoryNode parent) {
    // try {
    // Project currentProject = ProjectManager.getInstance().getCurrentProject();
    //
    // RootContainer<String, IRepositoryViewObject> jobsContainer = getFactory().getMetadata(currentProject,
    // getTopLevelNodeType(), true);
    //
    // convert(currentProject, jobsContainer, parent, getTopLevelNodeType(), (RepositoryNode) parent.getRoot()
    // .getRecBinNode());
    // } catch (PersistenceException e) {
    // ExceptionHandler.process(e);
    // }
    // }
    //
    // @Override
    // protected boolean ignoreFolders(Container container, RepositoryNode parent) {
    // if (super.ignoreFolders(container, parent)) {
    // return true;
    // }
    // if (ERepositoryObjectType.GENERATED.name().equalsIgnoreCase(container.getLabel())) {
    // return true;
    // }
    // return false;
    // }
}
