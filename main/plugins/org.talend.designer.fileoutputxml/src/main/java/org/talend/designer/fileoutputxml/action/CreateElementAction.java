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
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.ui.actions.SelectionProviderAction;
import org.talend.designer.fileoutputxml.i18n.Messages;
import org.talend.designer.fileoutputxml.ui.FOXUI;
import org.talend.metadata.managment.ui.wizard.metadata.xml.node.Attribute;
import org.talend.metadata.managment.ui.wizard.metadata.xml.node.Element;
import org.talend.metadata.managment.ui.wizard.metadata.xml.node.FOXTreeNode;
import org.talend.metadata.managment.ui.wizard.metadata.xml.node.NameSpaceNode;
import org.talend.metadata.managment.ui.wizard.metadata.xml.utils.StringUtil;

/**
 * bqian Create a xml node. <br/>
 *
 * $Id: CreateElementAction.java,v 1.1 2007/06/12 07:20:38 gke Exp $
 *
 */
public class CreateElementAction extends SelectionProviderAction {

    // the xml viewer, see FOXUI.
    private TreeViewer xmlViewer;

    private FOXUI foxui;

    /**
     * CreateNode constructor comment.
     *
     * @param provider
     * @param text
     */
    public CreateElementAction(TreeViewer xmlViewer, String text) {
        super(xmlViewer, text);
        this.xmlViewer = xmlViewer;
    }

    public CreateElementAction(TreeViewer xmlViewer, FOXUI foxui, String text) {
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
        if (node != null && createChildNode(node)) {
            foxui.redrawLinkers();
        }
    }

    /**
     * Create the child node of the input node
     *
     * @param node
     */
    private boolean createChildNode(FOXTreeNode node) {
        if (node.getColumn() != null) {
            if (!MessageDialog.openConfirm(xmlViewer.getControl().getShell(), Messages.getString("CreateElementAction.0"), //$NON-NLS-1$
                    Messages.getString("CreateElementAction.1") //$NON-NLS-1$
                            + node.getLabel() + "\"?")) { //$NON-NLS-1$
                return false;
            }
            node.setColumn(null);
        }
        String label = ""; //$NON-NLS-1$
        while (!StringUtil.validateLabelForXML(label)) {
            InputDialog dialog = new InputDialog(null,
                    Messages.getString("CreateElementAction.4"), Messages.getString("CreateElementAction.5"), //$NON-NLS-1$ //$NON-NLS-2$
                    "", null); //$NON-NLS-1$
            int status = dialog.open();
            if (status == InputDialog.OK) {
                label = dialog.getValue().trim();
            }
            if (status == InputDialog.CANCEL) {
                return false;
            }
        }
        FOXTreeNode child = new Element(label);
        // add by wzhang. set the row name
        child.setRow(node.getRow());
        node.addChild(child);
        this.xmlViewer.refresh();
        this.xmlViewer.expandToLevel(node, 1);
        return true;
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
        if (node instanceof Attribute) {
            this.setEnabled(false);
            return;
        }

        if (node instanceof NameSpaceNode) {
            this.setEnabled(false);
            return;
        }
        // let user can add more children to a root.
        // Element e = (Element) node;
        // if (e.getParent() == null) {
        // if (e.getElementChildren().size() >= 1) {
        // this.setEnabled(false);
        // return;
        // }
        // }
        this.setEnabled(true);

    }
}
