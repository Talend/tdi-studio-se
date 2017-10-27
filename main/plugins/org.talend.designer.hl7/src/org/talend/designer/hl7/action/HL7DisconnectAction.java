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
import org.talend.designer.hl7.ui.HL7UI;
import org.talend.designer.hl7.ui.data.HL7TreeNode;
import org.talend.metadata.managment.ui.wizard.AbstractForm;

/**
 * bqian Disconnect the schema to xml tree. <br/>
 * 
 * $Id: DisconnectAction.java,v 1.1 2007/06/12 07:20:38 gke Exp $
 * 
 */
public class HL7DisconnectAction extends SelectionProviderAction {

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
    public HL7DisconnectAction(TreeViewer xmlViewer, String text) {
        super(xmlViewer, text);
        this.xmlViewer = xmlViewer;
    }

    public HL7DisconnectAction(TreeViewer xmlViewer, String text, AbstractForm form) {
        super(xmlViewer, text);
        this.xmlViewer = xmlViewer;
        this.form = form;
    }

    public HL7DisconnectAction(TreeViewer xmlViewer, HL7UI hl7ui, String text) {
        super(xmlViewer, text);
        this.xmlViewer = xmlViewer;
        this.hl7ui = hl7ui;
    }

    public void init() {
        HL7TreeNode node = (HL7TreeNode) this.getStructuredSelection().getFirstElement();
        this.setEnabled(node != null && node.hasLink());
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
        node.setColumn(null);
        node.setColumnName(null);
        xmlViewer.refresh(node);
        xmlViewer.expandAll();
        if (hl7ui != null) {
            hl7ui.redrawLinkers();
        } else if (form != null) {
            form.refreshLinks();
        }
    }

}
