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
package org.talend.designer.core.ui.editor.connections;

import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Composite;
import org.talend.designer.core.model.process.ConnectionManager;

/**
 * DOC nrousseau class global comment. Detailled comment <br/>
 *
 * $Id: talend-code-templates.xml 1 2006-09-29 17:06:40 +0000 (ven., 29 sept. 2006) nrousseau $
 *
 */
public class NodeLabelCellEditor extends TextCellEditor {

    private static final Color ERROR_COLOR = new Color(null, new RGB(0xFF, 0, 0));

    private Connection connection;

    public NodeLabelCellEditor() {
        super();
        // TODO Auto-generated constructor stub
    }

    public NodeLabelCellEditor(Composite parent, int style) {
        super(parent, style);
        // TODO Auto-generated constructor stub
    }

    public NodeLabelCellEditor(Composite parent) {
        super(parent);
        // TODO Auto-generated constructor stub
    }

    public void setCurrentConnection(Connection connection) {
        this.connection = connection;
    }

    @Override
    protected void editOccured(ModifyEvent e) {
        super.editOccured(e);
        text.setBackground(null);
        if (connection != null) {
            if (text.getText() != null) {
                // if the user enter the same name as the current connection, we just ignore it.
                if (!connection.getName().equalsIgnoreCase(text.getText())) {
                    if (!ConnectionManager.canRename(connection.getSource(), connection.getTarget(), connection.getLineStyle(),
                            text.getText())) {
                        text.setBackground(ERROR_COLOR);
                    }
                }
            }
        }
    }

}
