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

import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.ui.actions.SelectionProviderAction;
import org.talend.metadata.managment.ui.wizard.metadata.xml.node.Attribute;
import org.talend.metadata.managment.ui.wizard.metadata.xml.node.Element;
import org.talend.metadata.managment.ui.wizard.metadata.xml.node.FOXTreeNode;
import org.talend.repository.json.ui.wizards.AbstractJSONStepForm;
import org.talend.repository.json.util.JSONUtil;

/**
 * hwang class global comment. Detailled comment
 */
public class CreateJSONAttributeAction extends SelectionProviderAction {

    private TreeViewer jsonViewer;

    private AbstractJSONStepForm form;

    public CreateJSONAttributeAction(TreeViewer jsonViewer, String text) {
        super(jsonViewer, text);
        this.jsonViewer = jsonViewer;
    }

    public CreateJSONAttributeAction(TreeViewer jsonViewer, AbstractJSONStepForm form, String text) {
        super(jsonViewer, text);
        this.jsonViewer = jsonViewer;
        this.form = form;
    }

    @Override
    public void run() {
        FOXTreeNode node = (FOXTreeNode) this.getStructuredSelection().getFirstElement();
        if (node != null) {
            createChildNode(node);
        }
        form.updateConnection();
    }

    private void createChildNode(FOXTreeNode node) {
        String label = "";
        while (!JSONUtil.validateLabelForJSON(label)) {
            InputDialog dialog = new InputDialog(null, "Input attribute's label", "Input the new attribute's valid label", "",
                    null);
            int status = dialog.open();
            if (status == InputDialog.OK) {
                label = dialog.getValue().trim();
            }
            if (status == InputDialog.CANCEL) {
                return;
            }
        }
        FOXTreeNode child = new Attribute(label);
        // child.setRow(node.getRow());
        node.addChild(child);
        this.jsonViewer.refresh();
        jsonViewer.expandAll();
        form.redrawLinkers();
    }

    @Override
    public void selectionChanged(IStructuredSelection selection) {
        FOXTreeNode node = (FOXTreeNode) this.getStructuredSelection().getFirstElement();
        if (node != null && node.getClass() != Element.class) {
            this.setEnabled(false);
        } else {
            // let user can add the attribute to root.
            if (node == null) {
                this.setEnabled(false);
            } else {
                this.setEnabled(true);
            }
        }
    }

}
