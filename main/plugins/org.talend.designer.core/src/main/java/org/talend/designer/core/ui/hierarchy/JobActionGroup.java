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
package org.talend.designer.core.ui.hierarchy;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.actions.ActionGroup;
import org.talend.commons.ui.swt.actions.ITreeContextualAction;
import org.talend.core.model.process.IProcess2;
import org.talend.core.model.repository.IRepositoryObject;
import org.talend.core.ui.actions.ActionsHelper;
import org.talend.repository.model.RepositoryNode;
import org.talend.repository.model.RepositoryNodeUtilities;

/**
 * Action group that adds the actions opening a new editor to the context menu and the action bar's navigate menu.
 *
 * <p>
 * This class may be instantiated; it is not intended to be subclassed.
 * </p>
 *
 * @since 2.0
 *
 * @noextend This class is not intended to be subclassed by clients.
 */
public class JobActionGroup extends ActionGroup {

    /**
     * Creates a new <code>OpenActionGroup</code>. The group requires that the selection provided by the part's
     * selection provider is of type <code>
	 * org.eclipse.jface.viewers.IStructuredSelection</code>.
     *
     * @param part the view part that owns this action group
     */
    public JobActionGroup() {

    }

    /*
     * (non-Javadoc) Method declared in ActionGroup
     */
    public void fillContextMenu(IMenuManager menu) {
        IStructuredSelection selection = (IStructuredSelection) getContext().getSelection();

        SelectionProviderAdapter selectionProvider = new SelectionProviderAdapter(selection);
        List<ITreeContextualAction> contextualsActions = ActionsHelper.getRepositoryContextualsActions();
        for (ITreeContextualAction action : contextualsActions) {
            if (action.isReadAction() || action.isEditAction() || action.isPropertiesAction()) {
                action.init(null, (IStructuredSelection) selectionProvider.getSelection());
                if (action.isVisible()) {
                    action.setSpecialSelection(selectionProvider);
                    menu.add(action);
                }
            }
        }
        // for (int i = 0; i < fActions.length; i++) {
        // SelectionDispatchAction action = fActions[i];
        // if (action == fCutAction && !fCutAction.isEnabled())
        // continue;
        // menu.appendToGroup(ICommonMenuConstants.GROUP_EDIT, action);
        // }
    }

    /**
     * DOC bqian JobActionGroup class global comment. Detailled comment
     */
    class SelectionProviderAdapter implements ISelectionProvider {

        IStructuredSelection selection = null;

        IStructuredSelection newSelection = null;

        public SelectionProviderAdapter(IStructuredSelection selection) {
            this.selection = selection;
            adaptProcessToRepositoryNode(selection);
        }

        /**
         * because all AContextualAction can only process the RepositoryNode, so adapt IProcess2 to RepositoryNode of
         * job type
         *
         * @param selection2
         */
        private void adaptProcessToRepositoryNode(IStructuredSelection inputSelection) {

            List<RepositoryNode> list = new ArrayList<RepositoryNode>();

            for (Iterator iterator = inputSelection.iterator(); iterator.hasNext();) {
                Object o = iterator.next();
                RepositoryNode repositoryNode = null;
                if (o instanceof IProcess2) {
                    repositoryNode = RepositoryNodeUtilities.getRepositoryNode(((IProcess2) o).getId());
                } else if (o instanceof IRepositoryObject) {
                    repositoryNode = RepositoryNodeUtilities.getRepositoryNode((IRepositoryObject) o);
                }
                if (repositoryNode != null) {
                    list.add(repositoryNode);
                }
            }

            newSelection = new StructuredSelection(list);
        }

        public void addSelectionChangedListener(ISelectionChangedListener listener) {

        }

        public ISelection getSelection() {
            return newSelection;

        }

        public void removeSelectionChangedListener(ISelectionChangedListener listener) {

        }

        public void setSelection(ISelection selection) {

        }

    }

}
