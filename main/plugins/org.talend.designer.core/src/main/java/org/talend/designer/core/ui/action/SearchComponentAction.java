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

import java.util.List;

import org.eclipse.gef.ui.actions.SelectionAction;
import org.eclipse.ui.IWorkbenchPart;
import org.talend.core.ui.utils.PluginUtil;
import org.talend.designer.core.i18n.Messages;
import org.talend.designer.core.ui.editor.nodes.Node;
import org.talend.designer.core.ui.editor.nodes.NodePart;

/**
 * DOC hcw class global comment. Detailled comment
 */
public class SearchComponentAction extends SelectionAction {

    /**
     * DOC hcw SearchComponentAction constructor comment.
     *
     * @param part
     */
    public SearchComponentAction(IWorkbenchPart part) {
        super(part);
        setText(Messages.getString("SearchComponentInJobs.Title")); //$NON-NLS-1$
        setToolTipText(Messages.getString("SearchComponentInJobs.Tooltip")); //$NON-NLS-1$
        setDescription(Messages.getString("SearchComponentInJobs.Tooltip")); //$NON-NLS-1$
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.gef.ui.actions.WorkbenchPartAction#calculateEnabled()
     */
    @Override
    protected boolean calculateEnabled() {
        List parts = getSelectedObjects();
        if (parts.size() != 1) {
            return false;
        }
        // TESB-17218: Hide for route test case editor
        if (PluginUtil.isMediation() || PluginUtil.isRouteTestEditor()) {
            return false;
        }
        Object o = parts.get(0);
        if (o instanceof NodePart) {
            Node node = (Node) ((NodePart) o).getModel();
            if (node.getJobletNode() != null) {
                return false;
            }
            return true;
        }
        return false;
    }

    @Override
    public void run() {

        List selection = getSelectedObjects();

        Object input = selection.get(0);
        if (input instanceof NodePart) {
            NodePart part = (NodePart) input;
            Node node = (Node) part.getModel();
            final String nodeName = node.getComponent().getName();
            // System.out.println(nodeName);
            ComponentSearcher searcher = new ComponentSearcher(nodeName, getWorkbenchPart().getSite().getShell());
            searcher.run();

        }
    }

}
