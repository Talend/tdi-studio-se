// ============================================================================
//
// Copyright (C) 2006-2017 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.designer.hl7.action;

import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.ui.actions.SelectionProviderAction;
import org.talend.designer.hl7.ui.HL7UI;
import org.talend.designer.hl7.ui.data.HL7TreeNode;
import org.talend.metadata.managment.ui.wizard.AbstractForm;

/**
 * 
 * DOC zli class global comment. Detailled comment
 * 
 */
public class HL7FixValueAction extends SelectionProviderAction {

    private TreeViewer xmlViewer;

    private HL7UI hl7ui;

    private AbstractForm form;

    /**
     * Create constructor comment.
     * 
     * @param provider
     * @param text
     */
    public HL7FixValueAction(TreeViewer xmlViewer, String text, AbstractForm form) {
        super(xmlViewer, text);
        this.xmlViewer = xmlViewer;
        this.form = form;
    }

    public HL7FixValueAction(TreeViewer xmlViewer, HL7UI hl7ui, String text) {
        super(xmlViewer, text);
        this.xmlViewer = xmlViewer;
        this.hl7ui = hl7ui;
    }

    public void init() {
        this.setEnabled(false);

        HL7TreeNode node = (HL7TreeNode) this.getStructuredSelection().getFirstElement();
        if (node == null) {
            this.setEnabled(false);
            return;
        }
        if (node.getParent() == null) {
            this.setEnabled(false);
            return;
        }
        if (node.getChildren() != null && node.getChildren().size() > 0) {
            this.setEnabled(false);
            return;
        }
        if (node.getColumn() == null) {
            this.setEnabled(false);
        }
        this.setEnabled(true);

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.action.Action#run()
     */
    @Override
    public void run() {
        HL7TreeNode node = (HL7TreeNode) this.getStructuredSelection().getFirstElement();
        setFixValue(node);

    }

    private void setFixValue(HL7TreeNode node) {
        String label = null; //$NON-NLS-1$
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
        this.xmlViewer.refresh();
        xmlViewer.expandAll();
        if (hl7ui != null) {
            hl7ui.redrawLinkers();
        } else if (form != null) {
            form.refreshLinks();
        }

    }

}
