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

import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.ui.actions.SelectionProviderAction;
import org.talend.designer.hl7.i18n.Messages;
import org.talend.designer.hl7.ui.HL7UI;
import org.talend.designer.hl7.ui.data.Element;
import org.talend.designer.hl7.ui.data.HL7TreeNode;
import org.talend.metadata.managment.ui.wizard.AbstractForm;

/**
 * bqian Create a xml node. <br/>
 *
 * $Id: CreateElementAction.java,v 1.1 2007/06/12 07:20:38 gke Exp $
 *
 */
public class SetRepetableAction extends SelectionProviderAction {

    /**
     *
     */
    private static final String REMOVE_REPEATABLE = Messages.getString("SetRepetableAction_removeRepeatable"); //$NON-NLS-1$

    /**
     *
     */
    private static final String SET_AS_REPEATABLE_ELEMENT = Messages.getString("SetRepetableAction_setAsRepeatable"); //$NON-NLS-1$

    // the xml viewer, see HL7UI.
    private TreeViewer xmlViewer;

    public SetRepetableAction(TreeViewer xmlViewer, AbstractForm form) {
        super(xmlViewer, SET_AS_REPEATABLE_ELEMENT);
        this.xmlViewer = xmlViewer;
    }

    /**
     *
     * SetForLoopAction constructor comment.
     *
     * @param xmlViewer
     * @param text
     * @param hl7ui
     */
    public SetRepetableAction(TreeViewer xmlViewer, HL7UI hl7ui, String text, boolean value) {
        super(xmlViewer, text);
        this.xmlViewer = xmlViewer;
    }

    public void init() {
        setText(SET_AS_REPEATABLE_ELEMENT);
        HL7TreeNode node = (HL7TreeNode) this.getStructuredSelection().getFirstElement();
        if (node == null) {
            this.setEnabled(false);
            return;
        }
        if (node.isRepetable()) {
            this.setEnabled(true);
            this.setText(REMOVE_REPEATABLE);
            return;
        }
        if (node.getLabel().length() != 3) {
            this.setEnabled(false);
            return;
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

        if (!node.isRepetable()) {
            if (node.isGroup()) {
                node.setGroup(false);
            }
            node.setRepetable(true);
            node.setMain(true);
            upsetMainNode(node);
        } else {
            node.setRepetable(false);
        }
        xmlViewer.refresh();
    }

    public void upsetMainNode(HL7TreeNode node) {
        if (node instanceof Element) {
            HL7TreeNode parent = node;
            while (parent != null) {
                parent.setMain(true);
                parent = parent.getParent();
            }

        }
    }
}
