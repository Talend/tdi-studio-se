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
import org.talend.designer.fileoutputxml.managers.FOXManager;
import org.talend.designer.fileoutputxml.ui.FOXUI;
import org.talend.metadata.managment.ui.wizard.metadata.xml.node.Attribute;
import org.talend.metadata.managment.ui.wizard.metadata.xml.node.FOXTreeNode;
import org.talend.metadata.managment.ui.wizard.metadata.xml.node.NameSpaceNode;
import org.talend.metadata.managment.ui.wizard.metadata.xml.utils.TreeUtil;

/**
 * bqian Create a xml node. <br/>
 *
 * $Id: CreateElementAction.java,v 1.1 2007/06/12 07:20:38 gke Exp $
 *
 */
public class SetGroupAction extends SelectionProviderAction {

    // the xml viewer, see FOXUI.
    private TreeViewer xmlViewer;

    private FOXUI foxui;

    private Boolean value;

    /**
     * CreateNode constructor comment.
     *
     * @param provider
     * @param text
     */
    public SetGroupAction(TreeViewer xmlViewer, FOXUI foxui, String text) {
        super(xmlViewer, text);
        this.xmlViewer = xmlViewer;
        this.foxui = foxui;
    }

    public SetGroupAction(TreeViewer xmlViewer, FOXUI foxui, String text, Boolean value) {
        super(xmlViewer, text);
        this.xmlViewer = xmlViewer;
        this.foxui = foxui;
        this.value = value;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.jface.action.Action#run()
     */
    @Override
    public void run() {
        FOXTreeNode node = (FOXTreeNode) this.getStructuredSelection().getFirstElement();
        if (node.isGroup()) {
            return;
        }
        FOXManager foxManager = foxui.getFoxManager();

        FOXTreeNode rootTreeData = foxManager.getRootFOXTreeNode(node);
        TreeUtil.clearSubGroupNode(rootTreeData);
        // old TreeUtil.clearLoopNode((FOXTreeNode) xmlViewer.getTree().getItem(0).getData());
        node.setGroup(true);
        // old TreeUtil.guessLoopWithGroup(node);
        foxui.updateStatus();
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
        FOXTreeNode node = (FOXTreeNode) this.getStructuredSelection().getFirstElement();
        if (node == null) {
            this.setEnabled(false);
            return;
        }
        if (((node instanceof Attribute) || node.hasLink()) && this.value) {
            this.setEnabled(TreeUtil.checkTreeGoupNode(node));
            return;

        }

        if (node instanceof NameSpaceNode) {
            this.setEnabled(false);
            return;
        }
        this.setEnabled(TreeUtil.checkTreeGoupNode(node));
    }
}
