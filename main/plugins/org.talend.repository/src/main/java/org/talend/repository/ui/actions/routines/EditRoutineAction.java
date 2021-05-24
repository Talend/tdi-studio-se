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
import org.talend.commons.exception.SystemException;
import org.talend.commons.runtime.model.repository.ERepositoryStatus;
import org.talend.commons.ui.runtime.exception.MessageBoxExceptionHandler;
import org.talend.commons.ui.runtime.image.ECoreImage;
import org.talend.commons.ui.runtime.image.ImageProvider;
import org.talend.core.CorePlugin;
import org.talend.core.model.properties.Item;
import org.talend.core.model.properties.RoutineItem;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.repository.ProjectManager;
import org.talend.repository.i18n.Messages;
import org.talend.repository.model.IProxyRepositoryFactory;
import org.talend.repository.model.RepositoryNode;

/**
 * Action that will edit routines.
 *
 * $Id$
 *
 */
public class EditRoutineAction extends AbstractRoutineAction {

    public EditRoutineAction() {
        super();

        setText(Messages.getString("EditRoutineAction.text.editRoutine")); //$NON-NLS-1$
        setToolTipText(Messages.getString("EditRoutineAction.toolTipText.editRoutine")); //$NON-NLS-1$
        setImageDescriptor(ImageProvider.getImageDesc(ECoreImage.ROUTINE_ICON));
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
        IProxyRepositoryFactory factory = ProxyRepositoryFactory.getInstance();
        if (factory.isUserReadOnlyOnCurrentProject()) {
            canWork = false;
        }
        RepositoryNode node = (RepositoryNode) selection.getFirstElement();
        if (canWork) {
            if (node.getObjectType() != ERepositoryObjectType.ROUTINES
                    || !ProjectManager.getInstance().isInCurrentMainProject(node) || !isLastVersion(node)) {
                canWork = false;
            } else {
                Item item = node.getObject().getProperty().getItem();
                if (item instanceof RoutineItem) {
                    canWork = !((RoutineItem) item).isBuiltIn();
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
        if (repositoryNode == null) {
            repositoryNode = (RepositoryNode) ((IStructuredSelection) getSelection()).getFirstElement();
        }
        RoutineItem routineItem = (RoutineItem) repositoryNode.getObject().getProperty().getItem();

        // Property updatedProperty = null;
        // try {
        // updatedProperty = ProxyRepositoryFactory.getInstance().getLastVersion(
        // new Project(ProjectManager.getInstance().getProject(routineItem)), routineItem.getProperty().getId())
        // .getProperty();
        //
        // // repositoryNode.getObject().setProperty(updatedProperty);
        // } catch (PersistenceException e) {
        // ExceptionHandler.process(e);
        // }
        // routineItem = (RoutineItem) repositoryNode.getObject().getProperty().getItem();

        try {
            openRoutineEditor(routineItem, false);
            refresh(repositoryNode);
            // CorePlugin.getDefault().getLibrariesService().resetModulesNeeded();
            CorePlugin.getDefault().getRunProcessService().updateLibraries(routineItem);
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
        return RoutineItem.class;
    }

}
