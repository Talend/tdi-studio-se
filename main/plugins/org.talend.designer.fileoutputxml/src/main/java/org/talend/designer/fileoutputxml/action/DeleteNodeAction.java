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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.ui.actions.SelectionProviderAction;
import org.talend.designer.fileoutputxml.ui.FOXUI;
import org.talend.metadata.managment.ui.wizard.metadata.xml.node.Attribute;
import org.talend.metadata.managment.ui.wizard.metadata.xml.node.Element;
import org.talend.metadata.managment.ui.wizard.metadata.xml.node.FOXTreeNode;
import org.talend.metadata.managment.ui.wizard.metadata.xml.node.NameSpaceNode;

/**
 * bqian Create a xml node. <br/>
 *
 * $Id: DeleteNodeAction.java,v 1.1 2007/06/12 07:20:38 gke Exp $
 *
 */
public class DeleteNodeAction extends SelectionProviderAction {

    // the xml viewer, see FOXUI.
    private TreeViewer xmlViewer;

    private FOXUI foxui;

    /**
     * CreateNode constructor comment.
     *
     * @param provider
     * @param text
     */
    public DeleteNodeAction(TreeViewer xmlViewer, String text) {
        super(xmlViewer, text);
        this.xmlViewer = xmlViewer;
    }

    public DeleteNodeAction(TreeViewer xmlViewer, FOXUI foxui, String text) {
        super(xmlViewer, text);
        this.xmlViewer = xmlViewer;
        this.foxui = foxui;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.jface.action.Action#run()
     */
    @Override
    public void run() {
        List<FOXTreeNode> objectToRemove = new ArrayList<FOXTreeNode>();
        final Iterator iterator = this.getStructuredSelection().iterator();
        while (iterator.hasNext()) {
            FOXTreeNode selectedNode = (FOXTreeNode) iterator.next();
            objectToRemove.add(selectedNode);
        }

        for (FOXTreeNode node : objectToRemove) {
            if (node == null) {
                return;
            }

            FOXTreeNode parent = node.getParent();
            if (parent == null) {
                return;
            }
            if (node instanceof Element) {
                disconnectSubTree(node);
            }
            parent.removeChild(node);

            if (node.isLoop() || node.isGroup()) {
                foxui.updateStatus();
            }

            xmlViewer.refresh(parent);
            xmlViewer.expandToLevel(parent, 1);
        }
        foxui.redrawLinkers();

    }

    /**
     * DOC ke Comment method "disconnectSubTree".
     *
     * @param node
     */
    private void disconnectSubTree(FOXTreeNode node) {
        if (node == null) {
            return;
        }
        if (node.hasLink()) {
            node.setColumn(null);
        }
        if (node instanceof Attribute) {
            return;
        }
        if (node instanceof NameSpaceNode) {
            return;
        }
        List<FOXTreeNode> children = node.getChildren();
        for (FOXTreeNode child : children) {
            disconnectSubTree(child);
        }
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
        } else {
            if (node.getParent() == null) {
                this.setEnabled(false);
            } else {
                this.setEnabled(true);
            }
        }
    }

}
