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

import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.ui.actions.SelectionProviderAction;
import org.talend.designer.fileoutputxml.i18n.Messages;
import org.talend.designer.fileoutputxml.ui.FOXUI;
import org.talend.metadata.managment.ui.wizard.metadata.xml.node.Element;
import org.talend.metadata.managment.ui.wizard.metadata.xml.node.FOXTreeNode;
import org.talend.metadata.managment.ui.wizard.metadata.xml.utils.StringUtil;

/**
 *
 * DOC zli class global comment. Detailled comment
 *
 */
public class FixValueAction extends SelectionProviderAction {

    private TreeViewer xmlViewer;

    private FOXUI foxui;

    /**
     * Create constructor comment.
     *
     * @param provider
     * @param text
     */
    public FixValueAction(TreeViewer xmlViewer, String text) {
        super(xmlViewer, text);
        this.xmlViewer = xmlViewer;
    }

    public FixValueAction(TreeViewer xmlViewer, FOXUI foxui, String text) {
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
        FOXTreeNode node = (FOXTreeNode) this.getStructuredSelection().getFirstElement();
        setFixValue(node);

    }

    private void setFixValue(FOXTreeNode node) {
        String label = null;
        while (!StringUtil.validateLabelForFixedValue(label)) {
            InputDialog dialog = new InputDialog(null, Messages.getString("FixValueAction.1"), //$NON-NLS-1$
                    Messages.getString("FixValueAction.2"), "", null); //$NON-NLS-1$ //$NON-NLS-2$
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
        this.xmlViewer.refresh(node);
        this.xmlViewer.expandToLevel(node, 1);
        foxui.redrawLinkers();
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
