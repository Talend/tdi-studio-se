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
package org.talend.repository.ui.actions;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.repository.i18n.Messages;
import org.talend.repository.model.IRepositoryNode;
import org.talend.repository.model.IRepositoryNode.EProperties;
import org.talend.repository.model.RepositoryNode;
import org.talend.repository.ui.views.IRepositoryView;

/**
 * Action use to expand a folder in repository. Many objects can be expand in the same time. System folder can also be
 * expand. All children are expand<br/>
 *
 * $Id$
 *
 */
public class ExpandAllAction extends AContextualAction {

    private static final String MOVE_TITLE = Messages.getString("ExpandAction.action.title"); //$NON-NLS-1$

    private static final String MOVE_TOOLTIP = Messages.getString("ExpandAction.action.toolTipText"); //$NON-NLS-1$

    public ExpandAllAction() {
        super();
        setText(MOVE_TITLE);
        setToolTipText(MOVE_TOOLTIP);
    }

    @Override
    protected void doRun() {
        IRepositoryView view = getViewPart();
        ISelection selection = getSelection();
        if (selection == null) {
            return;
        }
        Object objNode = ((IStructuredSelection) selection).getFirstElement();
        if (view != null) {
            if (objNode != null) {
                RepositoryNode node = (RepositoryNode) objNode;
                if ((node.getProperties(EProperties.CONTENT_TYPE) != null)
                        && (node.getProperties(EProperties.CONTENT_TYPE) == ERepositoryObjectType.SVN_ROOT)) {
                    view.expand(node);
                    view.getViewer().refresh();
                    return;
                }
            }

            Set<ERepositoryObjectType> types = new HashSet<ERepositoryObjectType>();
            for (Object obj : ((IStructuredSelection) selection).toArray()) {
                expand(view, (RepositoryNode) obj, !view.getExpandedState(obj));
                types.add(((RepositoryNode) obj).getContentType());
            }
            // TDI-21143 : Studio repository view : remove all refresh call to repo view
            // for (ERepositoryObjectType type : types) {
            // view.refresh(type);
            // }
        }
    }

    private void expand(IRepositoryView view, RepositoryNode obj, boolean state) {
        view.expand(obj, state);
        if (obj.hasChildren()) {
            for (IRepositoryNode currentChild : obj.getChildren()) {
                expand(view, (RepositoryNode) currentChild, state);
            }
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.repository.ui.actions.ITreeContextualAction#init(org.eclipse.jface.viewers.TreeViewer,
     * org.eclipse.jface.viewers.IStructuredSelection)
     */
    public void init(TreeViewer viewer, IStructuredSelection selection) {
        boolean canWork = !selection.isEmpty();

        for (Object o : (selection).toArray()) {
            if (canWork) {
                RepositoryNode node = (RepositoryNode) o;
                switch (node.getType()) {
                case STABLE_SYSTEM_FOLDER:
                case SYSTEM_FOLDER:
                case SIMPLE_FOLDER:
                    canWork = node.hasChildren();
                    break;
                default:
                    canWork = false;
                    break;
                }
            }
        }
        setEnabled(canWork);
    }
}
