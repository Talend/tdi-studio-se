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

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.ui.actions.SelectionProviderAction;
import org.talend.metadata.managment.ui.wizard.metadata.xml.node.FOXTreeNode;
import org.talend.metadata.managment.ui.wizard.metadata.xml.utils.TreeUtil;
import org.talend.repository.json.ui.wizards.AbstractJSONStepForm;

/**
 * hwang class global comment. Detailled comment
 */
public class SetForJSONLoopAction extends SelectionProviderAction {

    private TreeViewer jsonViewer;

    private AbstractJSONStepForm form;

    public SetForJSONLoopAction(TreeViewer jsonViewer, String text) {
        super(jsonViewer, text);
        this.jsonViewer = jsonViewer;
    }

    public SetForJSONLoopAction(TreeViewer jsonViewer, AbstractJSONStepForm form, String text) {
        super(jsonViewer, text);
        this.jsonViewer = jsonViewer;
        this.form = form;
    }

    @Override
    public void run() {
        FOXTreeNode node = (FOXTreeNode) this.getStructuredSelection().getFirstElement();
        if (node.isLoop()) {
            return;
        }
        FOXTreeNode rootTreeData = TreeUtil.getRootFOXTreeNode(node);
        TreeUtil.clearSubGroupNode(node);
        // make sure group element is a ancestor of loop, or no group element.
        if (TreeUtil.findUpGroupNode(node) == null) {
            TreeUtil.clearSubGroupNode(rootTreeData);
        }
        TreeUtil.clearLoopNode(rootTreeData);
        TreeUtil.clearMainNode(rootTreeData);

        if (node.isGroup()) {
            node.setGroup(false);
        }
        node.setLoop(true);

        if (form != null) {
            form.updateStatus();
        }
        TreeUtil.upsetMainNode(node);
        jsonViewer.refresh();

        form.updateStatus();
        form.updateConnection();
    }

    @Override
    public void selectionChanged(IStructuredSelection selection) {
        FOXTreeNode node = (FOXTreeNode) this.getStructuredSelection().getFirstElement();
        if (node == null) {
            this.setEnabled(false);
            return;
        }
        // fix for TDI-18802
        this.setEnabled(TreeUtil.canSetAsLoop(node));
    }
}
