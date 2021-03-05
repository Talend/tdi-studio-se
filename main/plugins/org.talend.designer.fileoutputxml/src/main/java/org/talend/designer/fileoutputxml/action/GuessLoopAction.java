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
package org.talend.designer.fileoutputxml.action;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.ui.actions.SelectionProviderAction;
import org.talend.metadata.managment.ui.wizard.metadata.xml.node.FOXTreeNode;
import org.talend.metadata.managment.ui.wizard.metadata.xml.utils.TreeUtil;

/**
 * bqian Create a xml node. <br/>
 *
 * $Id: CreateElementAction.java,v 1.1 2007/06/12 07:20:38 gke Exp $
 *
 */
public class GuessLoopAction extends SelectionProviderAction {

    // the xml viewer, see FOXUI.
    private TreeViewer xmlViewer;

    /**
     * CreateNode constructor comment.
     *
     * @param provider
     * @param text
     */
    public GuessLoopAction(TreeViewer xmlViewer, String text) {
        super(xmlViewer, text);
        this.xmlViewer = xmlViewer;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.jface.action.Action#run()
     */
    @Override
    public void run() {
        TreeUtil.clearLoopNode((FOXTreeNode) xmlViewer.getTree().getItem(0).getData());
        if (!TreeUtil.guessLoopWithGroup((FOXTreeNode) xmlViewer.getTree().getItem(0).getData())) {
            TreeUtil.guessAndSetLoopNode((FOXTreeNode) xmlViewer.getTree().getItem(0).getData());
        }
        xmlViewer.refresh();
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.eclipse.ui.actions.SelectionProviderAction#selectionChanged(org.eclipse.jface.viewers.IStructuredSelection)
     */
    @Override
    public void selectionChanged(IStructuredSelection selection) {

    }
}
