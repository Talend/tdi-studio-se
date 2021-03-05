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
package org.talend.repository.ui.actions.routines;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.ui.PartInitException;
import org.talend.commons.exception.PersistenceException;
import org.talend.commons.exception.SystemException;
import org.talend.commons.runtime.model.repository.ERepositoryStatus;
import org.talend.commons.ui.runtime.exception.ExceptionHandler;
import org.talend.commons.ui.runtime.exception.MessageBoxExceptionHandler;
import org.talend.commons.ui.runtime.image.EImage;
import org.talend.commons.ui.runtime.image.ImageProvider;
import org.talend.core.CorePlugin;
import org.talend.core.model.general.Project;
import org.talend.core.model.properties.Property;
import org.talend.core.model.properties.RoutineItem;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.repository.ProjectManager;
import org.talend.repository.i18n.Messages;
import org.talend.repository.model.RepositoryNode;

/**
 * Action that will edit routines.
 *
 * $Id: EditRoutineAction.java 1863 2007-02-06 06:01:36Z yzhang $
 *
 */
public class ReadRoutineAction extends AbstractRoutineAction {

    public ReadRoutineAction() {
        super();

        setText(Messages.getString("EditRoutineAction.text.readRoutine")); //$NON-NLS-1$
        setToolTipText(Messages.getString("EditRoutineAction.toolTipText.readRoutine")); //$NON-NLS-1$
        setImageDescriptor(ImageProvider.getImageDesc(EImage.READ_ICON));
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.repository.ui.actions.ITreeContextualAction#init(org.eclipse.jface.viewers.TreeViewer,
     * org.eclipse.jface.viewers.IStructuredSelection)
     */
    @Override
    public void init(TreeViewer viewer, IStructuredSelection selection) {
        super.init(viewer, selection);
        boolean canWork = !selection.isEmpty() && selection.size() == 1;
        if (canWork) {
            RepositoryNode node = (RepositoryNode) selection.getFirstElement();
            if (node.getObjectType() != ERepositoryObjectType.ROUTINES) {
                canWork = false;
            }
            if (canWork && node.getObject() != null
                    && ProxyRepositoryFactory.getInstance().getStatus(node.getObject()) == ERepositoryStatus.LOCK_BY_USER) {
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
    @Override
    protected void doRun() {
        if (repositoryNode == null && getSelection() != null) {
            repositoryNode = (RepositoryNode) ((IStructuredSelection) getSelection()).getFirstElement();
        }
        RoutineItem routineItem = (RoutineItem) repositoryNode.getObject().getProperty().getItem();

        Property updatedProperty = null;
        try {
            updatedProperty = ProxyRepositoryFactory
                    .getInstance()
                    .getLastVersion(new Project(ProjectManager.getInstance().getProject(routineItem)),
                            routineItem.getProperty().getId()).getProperty();

            // repositoryNode.getObject().setProperty(updatedProperty);
        } catch (PersistenceException e) {
            ExceptionHandler.process(e);
        }
        routineItem = (RoutineItem) repositoryNode.getObject().getProperty().getItem();
        try {
            openRoutineEditor(routineItem, true);
            CorePlugin.getDefault().getRunProcessService().updateLibraries(routineItem);
        } catch (PartInitException e) {
            MessageBoxExceptionHandler.process(e);
        } catch (SystemException e) {
            MessageBoxExceptionHandler.process(e);
        }
    }

}
