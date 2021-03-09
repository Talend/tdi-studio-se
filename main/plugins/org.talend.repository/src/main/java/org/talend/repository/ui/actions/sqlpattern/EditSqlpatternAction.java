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
package org.talend.repository.ui.actions.sqlpattern;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.ui.PartInitException;
import org.talend.commons.exception.PersistenceException;
import org.talend.commons.exception.SystemException;
import org.talend.commons.runtime.model.repository.ERepositoryStatus;
import org.talend.commons.ui.runtime.exception.ExceptionHandler;
import org.talend.commons.ui.runtime.exception.MessageBoxExceptionHandler;
import org.talend.commons.ui.runtime.image.ECoreImage;
import org.talend.commons.ui.runtime.image.ImageProvider;
import org.talend.core.model.general.Project;
import org.talend.core.model.properties.Item;
import org.talend.core.model.properties.Property;
import org.talend.core.model.properties.SQLPatternItem;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.repository.ProjectManager;
import org.talend.repository.model.IProxyRepositoryFactory;
import org.talend.repository.model.RepositoryNode;

/**
 * Action that will edit routines.
 *
 * $Id: EditRoutineAction.java 7038 2007-11-15 14:05:48Z plegall $
 *
 */
public class EditSqlpatternAction extends AbstractSqlpatternAction {

    public EditSqlpatternAction() {
        super();

        setText("Edit SQLTemplate"); //$NON-NLS-1$
        setToolTipText("Edit SQLTemplate"); //$NON-NLS-1$
        setImageDescriptor(ImageProvider.getImageDesc(ECoreImage.METADATA_SQLPATTERN_ICON));
    }

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
        RepositoryNode node = (RepositoryNode) selection.getFirstElement();
        if (canWork) {
            if (node.getObjectType() != ERepositoryObjectType.SQLPATTERNS
                    || !ProjectManager.getInstance().isInCurrentMainProject(node) || !isLastVersion(node)) {
                canWork = false;
            } else {
                Item item = node.getObject().getProperty().getItem();
                if (item instanceof SQLPatternItem) {
                    canWork = !((SQLPatternItem) item).isSystem();
                }
            }
        }
        if (canWork) {
            canWork = (factory.getStatus(node.getObject()) != ERepositoryStatus.DELETED);
        }
        setEnabled(canWork);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.jface.action.Action#run()
     */
    @Override
    protected void doRun() {
        RepositoryNode node = (RepositoryNode) ((IStructuredSelection) getSelection()).getFirstElement();
        Property property = node.getObject().getProperty();
        Property updatedProperty = null;
        try {
            updatedProperty = ProxyRepositoryFactory.getInstance()
                    .getLastVersion(new Project(ProjectManager.getInstance().getProject(property.getItem())), property.getId())
                    .getProperty();
        } catch (PersistenceException e) {
            ExceptionHandler.process(e);
        }
        // update the property of the node repository object
        // node.getObject().setProperty(updatedProperty);
        SQLPatternItem sqlPatternItem = (SQLPatternItem) node.getObject().getProperty().getItem();
        IProxyRepositoryFactory factory = ProxyRepositoryFactory.getInstance();
        boolean readonly = factory.getStatus(sqlPatternItem) == ERepositoryStatus.LOCK_BY_OTHER;
        try {
            openSQLPatternEditor(sqlPatternItem, readonly);
            // TDI-21143 : Studio repository view : remove all refresh call to repo view
            // IRepositoryView view = getViewPart();
            // if (view != null) {
            // view.refresh(node);
            // }
        } catch (PartInitException e) {
            MessageBoxExceptionHandler.process(e);
        } catch (SystemException e) {
            MessageBoxExceptionHandler.process(e);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.repository.ui.actions.AContextualView#getClassForDoubleClick()
     */
    @Override
    public Class getClassForDoubleClick() {
        return SQLPatternItem.class;
    }

}
