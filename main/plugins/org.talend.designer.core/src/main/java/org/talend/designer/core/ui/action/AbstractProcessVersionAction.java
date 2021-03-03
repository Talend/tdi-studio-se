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
package org.talend.designer.core.ui.action;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.talend.commons.exception.PersistenceException;
import org.talend.commons.ui.runtime.exception.ExceptionHandler;
import org.talend.core.model.properties.ProcessItem;
import org.talend.core.model.properties.Property;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.designer.runprocess.ItemCacheManager;
import org.talend.repository.ProjectManager;
import org.talend.repository.model.IProxyRepositoryFactory;
import org.talend.repository.model.IRepositoryNode;
import org.talend.repository.model.RepositoryNode;
import org.talend.repository.ui.actions.AContextualAction;

/**
 * DOC xye class global comment. Detailled comment
 */
public abstract class AbstractProcessVersionAction extends AContextualAction {

    private ProcessItem processItem = null;

    private RepositoryNode repositoryNode = null;

    /*
     * (non-Javadoc)
     *
     * @see org.talend.repository.ui.actions.ITreeContextualAction#init(org.eclipse.jface.viewers.TreeViewer,
     * org.eclipse.jface.viewers.IStructuredSelection)
     */
    public void init(TreeViewer viewer, IStructuredSelection selection) {
        boolean canWork = !selection.isEmpty() && selection.size() == 1;
        IProxyRepositoryFactory factory = ProxyRepositoryFactory.getInstance();
        if (factory.isUserReadOnlyOnCurrentProject()) {
            canWork = false;
        }
        if (canWork) {
            Object o = selection.getFirstElement();
            repositoryNode = (RepositoryNode) o;
            switch (repositoryNode.getType()) {
            case REPOSITORY_ELEMENT:
                canWork = repositoryNode.getObjectType() == ERepositoryObjectType.PROCESS;
                break;
            default:
                canWork = false;
            }
            if (canWork && !ProjectManager.getInstance().isInCurrentMainProject(repositoryNode)) {
                canWork = false;
            }
        }

        setEnabled(canWork);

        if (canWork) {
            Property property = (Property) repositoryNode.getObject().getProperty();

            ItemCacheManager.clearCache();
            Assert.isTrue(property.getItem() instanceof ProcessItem);

            Property updatedProperty = null;
            try {
                updatedProperty = ProxyRepositoryFactory.getInstance().getUptodateProperty(property);
            } catch (PersistenceException e) {
                ExceptionHandler.process(e);
            }
            // update the property of the node repository object
            // repositoryNode.getObject().setProperty(updatedProperty);
            processItem = (ProcessItem) updatedProperty.getItem();
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.repository.ui.actions.AContextualView#getClassForDoubleClick()
     */
    @Override
    public Class<?> getClassForDoubleClick() {
        return ProcessItem.class;
    }

    public ProcessItem getProcessItem() {
        return this.processItem;
    }

    public IRepositoryNode getRepositoryNode() {
        return this.repositoryNode;
    }

}
