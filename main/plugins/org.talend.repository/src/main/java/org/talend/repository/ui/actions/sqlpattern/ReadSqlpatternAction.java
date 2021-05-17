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
import org.talend.commons.ui.runtime.exception.ExceptionHandler;
import org.talend.commons.ui.runtime.exception.MessageBoxExceptionHandler;
import org.talend.commons.ui.runtime.image.EImage;
import org.talend.commons.ui.runtime.image.ImageProvider;
import org.talend.core.model.general.Project;
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
 * $Id: EditRoutineAction.java 1863 2007-02-06 06:01:36Z yzhang $
 *
 */
public class ReadSqlpatternAction extends AbstractSqlpatternAction {

    public ReadSqlpatternAction() {
        super();

        setText("Read SQLPattern"); //$NON-NLS-1$
        setToolTipText("Read SQLPattern"); //$NON-NLS-1$
        setImageDescriptor(ImageProvider.getImageDesc(EImage.READ_ICON));
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
        if (canWork) {
            RepositoryNode node = (RepositoryNode) selection.getFirstElement();
            if (node.getObjectType() != ERepositoryObjectType.SQLPATTERNS) {
                canWork = false;
            }

        }
        setEnabled(canWork);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.jface.action.Action#run()
     */
    protected void doRun() {
        RepositoryNode node = (RepositoryNode) ((IStructuredSelection) getSelection()).getFirstElement();
        Property property = (Property) node.getObject().getProperty();
        Property updatedProperty = null;
        try {
            updatedProperty = ProxyRepositoryFactory.getInstance().getLastVersion(
                    new Project(ProjectManager.getInstance().getProject(property.getItem())), property.getId()).getProperty();
        } catch (PersistenceException e) {
            ExceptionHandler.process(e);
        }
        // update the property of the node repository object
        // node.getObject().setProperty(updatedProperty);
        SQLPatternItem sqlpatternItem = (SQLPatternItem) node.getObject().getProperty().getItem();

        try {
            openSQLPatternEditor(sqlpatternItem, true);
        } catch (PartInitException e) {
            MessageBoxExceptionHandler.process(e);
        } catch (SystemException e) {
            MessageBoxExceptionHandler.process(e);
        }
    }

}
