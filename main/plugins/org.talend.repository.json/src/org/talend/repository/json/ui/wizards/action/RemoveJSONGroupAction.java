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
import org.talend.metadata.managment.ui.wizard.metadata.xml.node.Attribute;
import org.talend.metadata.managment.ui.wizard.metadata.xml.node.FOXTreeNode;
import org.talend.metadata.managment.ui.wizard.metadata.xml.node.NameSpaceNode;
import org.talend.repository.json.ui.wizards.AbstractJSONStepForm;

/**
 * hwang class global comment. Detailled comment
 */
public class RemoveJSONGroupAction extends SelectionProviderAction {

    private TreeViewer jsonViewer;

    private AbstractJSONStepForm form;

    public RemoveJSONGroupAction(TreeViewer jsonViewer, String text) {
        super(jsonViewer, text);
        this.jsonViewer = jsonViewer;
    }

    public RemoveJSONGroupAction(TreeViewer jsonViewer, String text, AbstractJSONStepForm form) {
        super(jsonViewer, text);
        this.jsonViewer = jsonViewer;
        this.form = form;
    }

    @Override
    public void run() {
        FOXTreeNode node = (FOXTreeNode) this.getStructuredSelection().getFirstElement();
        node.setGroup(false);
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
        if (node instanceof Attribute) {
            this.setEnabled(false);
            return;
        }

        if (node instanceof NameSpaceNode) {
            this.setEnabled(false);
            return;
        }
        this.setEnabled(node.isGroup());
    }
}
