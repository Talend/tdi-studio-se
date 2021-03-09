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
import org.talend.metadata.managment.ui.wizard.metadata.xml.node.Element;
import org.talend.metadata.managment.ui.wizard.metadata.xml.node.FOXTreeNode;
import org.talend.metadata.managment.ui.wizard.metadata.xml.utils.StringUtil;
import org.talend.repository.json.ui.wizards.AbstractJSONStepForm;

/**
 * hwang class global comment. Detailled comment
 */
public class FixValueAction extends SelectionProviderAction {

    private TreeViewer jsonViewer;

    private AbstractJSONStepForm form;

    public FixValueAction(TreeViewer jsonViewer, String text) {
        super(jsonViewer, text);
        this.jsonViewer = jsonViewer;
    }

    public FixValueAction(TreeViewer jsonViewer, AbstractJSONStepForm form, String text) {
        super(jsonViewer, text);
        this.jsonViewer = jsonViewer;
        this.form = form;
    }

    @Override
    public void run() {
        FOXTreeNode node = (FOXTreeNode) this.getStructuredSelection().getFirstElement();
        if (node != null) {
            setFixValue(node);
        }
    }

    private void setFixValue(FOXTreeNode node) {
        String label = null;
        while (!StringUtil.validateLabelForFixedValue(label)) {
            InputDialog dialog = new InputDialog(null, "Input a fix value", "Input the default value' valid label", "", null);
            int status = dialog.open();
            if (status == InputDialog.OK) {
                label = dialog.getValue().trim();
                if (label != null && label.length() == 0) {
                    break;
                }
            }
            if (status == InputDialog.CANCEL) {
                return;
            }
        }
        node.setDefaultValue(label);
        this.jsonViewer.refresh();
        jsonViewer.expandAll();
        form.redrawLinkers();
        form.updateConnection();
    }

    @Override
    public void selectionChanged(IStructuredSelection selection) {
        FOXTreeNode node = (FOXTreeNode) this.getStructuredSelection().getFirstElement();
        if (node == null) {
            this.setEnabled(false);
        } else {
            if (node.getParent() == null) {
                this.setEnabled(false);
            } else if (node.getColumn() != null) {
                this.setEnabled(false);
            } else if (node.getChildren() != null && !node.getChildren().isEmpty()) {
                // this.setEnabled(false);
                boolean haveElementChild = false;
                for (FOXTreeNode child : node.getChildren()) {
                    if (child instanceof Element) {
                        haveElementChild = true;
                        break;
                    }
                }
                if (haveElementChild) {
                    setEnabled(false);
                } else {
                    setEnabled(true);
                }
            } else {
                this.setEnabled(true);
            }
        }
    }
}
