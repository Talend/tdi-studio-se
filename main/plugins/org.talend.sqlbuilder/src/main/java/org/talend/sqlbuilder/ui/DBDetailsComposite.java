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
package org.talend.sqlbuilder.ui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.talend.sqlbuilder.Messages;
import org.talend.sqlbuilder.dbdetail.DetailTabManager;
import org.talend.sqlbuilder.dbstructure.nodes.INode;

/**
 * Show the details informaiton of DB selected in the DB structure composite <br/>.
 *
 * $Id: DBDetailsComposite.java,v 1.13 2006/11/01 06:56:31 peiqin.hou Exp $
 *
 */

public class DBDetailsComposite extends Composite {

    private Composite panel;

    public DBDetailsComposite(Composite parent, int style) {
        super(parent, style);

        setLayout(new FillLayout());

        panel = new Composite(this, SWT.NONE);

        panel.setLayout(new FillLayout());
        setSelectedNode(null, null);
    }

    /**
     * DOC qianbing Comment method "setSelectedNode". Displays the message input,if inputs null,it will show default
     * message.
     *
     * @param node INode
     * @param message String
     */
    public void setSelectedNode(INode node, String message) {
        Composite parent = panel.getParent();
        panel.dispose();
        panel = new Composite(parent, SWT.NONE);
        panel.setLayout(new FillLayout());

        if (node == null) {
            Label label = new Label(panel, SWT.FILL | SWT.WRAP);
            if (message == null) {
                // add default message
                message = Messages.getString("DatabaseDetailView.NoSelection"); //$NON-NLS-1$
            }
            label.setText(message);
        } else {
            // add tabs
            DetailTabManager.createTabs(panel, node);
        }

        panel.layout();
        panel.getParent().layout();
        panel.redraw();
    }
}
