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
package org.talend.repository.json.ui.wizards.action;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.ui.actions.SelectionProviderAction;
import org.talend.metadata.managment.ui.wizard.metadata.xml.node.Attribute;
import org.talend.metadata.managment.ui.wizard.metadata.xml.node.Element;
import org.talend.metadata.managment.ui.wizard.metadata.xml.node.FOXTreeNode;
import org.talend.metadata.managment.ui.wizard.metadata.xml.node.NameSpaceNode;
import org.talend.repository.json.ui.wizards.AbstractJSONStepForm;

/**
 * hwang class global comment. Detailled comment
 */
public class DeleteJSONNodeAction extends SelectionProviderAction {

    private TreeViewer jsonViewer;

    private AbstractJSONStepForm form;

    public DeleteJSONNodeAction(TreeViewer jsonViewer, String text) {
        super(jsonViewer, text);
        this.jsonViewer = jsonViewer;
    }

    public DeleteJSONNodeAction(TreeViewer jsonViewer, AbstractJSONStepForm form, String text) {
        super(jsonViewer, text);
        this.jsonViewer = jsonViewer;
        this.form = form;
    }

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
                form.updateStatus();
            }

            jsonViewer.refresh(parent);
        }
        jsonViewer.expandAll();
        form.updateConnection();
        form.redrawLinkers();

    }

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
