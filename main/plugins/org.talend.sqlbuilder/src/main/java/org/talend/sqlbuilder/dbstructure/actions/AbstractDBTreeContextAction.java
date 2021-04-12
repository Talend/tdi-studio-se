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
package org.talend.sqlbuilder.dbstructure.actions;

import net.sourceforge.sqlexplorer.plugin.views.DatabaseStructureView;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.TreeViewer;
import org.talend.sqlbuilder.dbstructure.nodes.INode;

/**
 * Abstract implementation for a context menu action in the database structure view. Extend this class to add actions to
 * the structure view.
 *
 * @author Davy Vanherbergen
 */
public abstract class AbstractDBTreeContextAction extends Action {

    protected INode[] selectedNodes;

    protected TreeViewer treeViewer;

    protected DatabaseStructureView view;

    /**
     * Store nodes for use in the actions.
     *
     * @param nodes
     */
    public final void setSelectedNodes(INode[] nodes) {
        selectedNodes = nodes;
    }

    /**
     * Store treeViewer for use in the actions.
     *
     * @param treeViewer
     */
    public void setTreeViewer(TreeViewer treeViewer) {
        this.treeViewer = treeViewer;
    }

    /**
     * Store view for use in actions.
     */
    public void setView(DatabaseStructureView view) {
        this.view = view;
    }

    /**
     * Implement this method to return true when your action is available for the selected node(s). When true, the
     * action will be included in the context menu, when false it will be ignored.
     *
     *
     * @return true if the action should be included in the context menu
     */
    public boolean isAvailable() {
        return true;
    }
}
