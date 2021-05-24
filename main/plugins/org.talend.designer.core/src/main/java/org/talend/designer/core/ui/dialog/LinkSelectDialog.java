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

import java.util.List;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.ListDialog;
import org.talend.designer.core.ui.editor.connections.Connection;

/**
 * wzhang class global comment. Detailled comment
 */
public class LinkSelectDialog extends ListDialog {

    public LinkSelectDialog(Shell parent, List<Connection> input) {
        super(parent);
        setTitle("Choose Link");
        setMessage("Target reference several links, please choose one to create.");
        setInput(input);
        setContentProvider(ArrayContentProvider.getInstance());
        setLabelProvider(new LabelProvider() {

            @Override
            public String getText(Object element) {
                Connection conn = (Connection) element;
                return conn.getName();
            }

        });

    }

    protected Control createContents(Composite parent) {
        Control control = super.createContents(parent);
        getTableViewer().getTable().showSelection();
        return control;
    }

    public Connection getSelectLink() {
        return (Connection) getResult()[0];
    }
}
