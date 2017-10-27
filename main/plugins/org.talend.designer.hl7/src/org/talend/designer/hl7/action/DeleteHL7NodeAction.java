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

import java.util.List;

import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.ui.actions.SelectionProviderAction;
import org.talend.designer.hl7.ui.HL7UI;
import org.talend.designer.hl7.ui.data.Element;
import org.talend.designer.hl7.ui.data.HL7TreeNode;
import org.talend.metadata.managment.ui.wizard.AbstractForm;

/**
 * bqian Create a xml node. <br/>
 *
 * $Id: DeleteNodeAction.java,v 1.1 2007/06/12 07:20:38 gke Exp $
 *
 */
public class DeleteHL7NodeAction extends SelectionProviderAction {

    // the xml viewer, see HL7UI.
    private TreeViewer xmlViewer;

    private HL7UI hl7ui;

    private AbstractForm form;

    /**
     * CreateNode constructor comment.
     *
     * @param provider
     * @param text
     */
    public DeleteHL7NodeAction(TreeViewer xmlViewer, String text) {
        super(xmlViewer, text);
        this.xmlViewer = xmlViewer;
    }

    public DeleteHL7NodeAction(TreeViewer xmlViewer, String text, AbstractForm form) {
        super(xmlViewer, text);
        this.xmlViewer = xmlViewer;
        this.form = form;
    }

    public DeleteHL7NodeAction(TreeViewer xmlViewer, HL7UI hl7ui, String text) {
        super(xmlViewer, text);
        this.xmlViewer = xmlViewer;
        this.hl7ui = hl7ui;
    }

    public void init() {
        HL7TreeNode node = (HL7TreeNode) this.getStructuredSelection().getFirstElement();
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

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.action.Action#run()
     */
    @Override
    public void run() {
        HL7TreeNode node = (HL7TreeNode) this.getStructuredSelection().getFirstElement();
        if (node == null) {
            return;
        }

        HL7TreeNode parent = node.getParent();
        if (parent == null) {
            return;
        }
        if (node instanceof Element) {
            disconnectSubTree(node);
        }
        parent.removeChild(node);
        xmlViewer.refresh(parent);
    }

    /**
     * DOC ke Comment method "disconnectSubTree".
     *
     * @param node
     */
    private void disconnectSubTree(HL7TreeNode node) {
        if (node == null) {
            return;
        }
        if (node.hasLink()) {
            node.setColumn(null);
        }
        List<HL7TreeNode> children = node.getChildren();
        for (HL7TreeNode child : children) {
            disconnectSubTree(child);
        }
    }

}
