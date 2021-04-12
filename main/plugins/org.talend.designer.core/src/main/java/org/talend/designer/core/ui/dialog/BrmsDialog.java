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
package org.talend.designer.core.ui.dialog;

import org.apache.commons.collections.BidiMap;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.SelectionDialog;
import org.talend.core.model.properties.ConnectionItem;
import org.talend.designer.core.ui.editor.nodes.Node;

/**
 * DOC hwang class global comment. Detailled comment
 */
public abstract class BrmsDialog extends SelectionDialog {

    protected Node node;

    protected String propertyName;

    protected BidiMap hashCurControls;

    protected ConnectionItem connectionItem;

    protected boolean isRepository;

    /**
     * DOC Administrator BrmsDialog constructor comment.
     *
     * @param parentShell
     */
    public BrmsDialog(Shell parentShell) {
        super(parentShell);
    }

    public void initialize(Node node, String propertyName, BidiMap hashCurControls) {
        this.propertyName = propertyName;
        this.node = node;
        this.hashCurControls = hashCurControls;
    }

    public ConnectionItem getConnectionItem() {
        return this.connectionItem;
    }

    public void setConnectionItem(ConnectionItem connectionItem) {
        this.connectionItem = connectionItem;
    }

    public boolean isRepository() {
        return this.isRepository;
    }

    public void setRepository(boolean isRepository) {
        this.isRepository = isRepository;
    }

    public abstract String getFile();

}
