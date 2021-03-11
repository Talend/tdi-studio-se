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
package org.talend.designer.webservice.managers;

import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TreeEditor;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;

/**
 * gcui class global comment. Detailled comment
 */
public class UIManager {

    private WebServiceManager webServiceManager;

    private TreeEditor columnTreeEditor;

    private MouseListener columnMouseListener;

    private int dialogResponse = SWT.NONE;

    public UIManager(WebServiceManager webSchemaManager) {
        super();
        this.webServiceManager = webServiceManager;
    }

    public int getDialogResponse() {
        return this.dialogResponse;
    }

    public void setDialogResponse(int dialogResponse) {
        this.dialogResponse = dialogResponse;
    }

    public void changeSchemasDetailView(final TreeViewer schemaDetailsViewer, boolean model) {
        if (schemaDetailsViewer == null || schemaDetailsViewer.getTree().isDisposed()) {
            return;
        }
        final Tree tree = schemaDetailsViewer.getTree();
        // removed all columns
        final TreeColumn[] columns = tree.getColumns();
        for (TreeColumn col : columns) {
            col.dispose();
        }
        if (columnTreeEditor != null) {
            columnTreeEditor.dispose();
            final Control editor = columnTreeEditor.getEditor();
            if (editor != null && !editor.isDisposed()) {
                editor.setVisible(false);
                editor.dispose();
            }
            columnTreeEditor = null;
        }
        if (columnMouseListener != null) {
            tree.removeMouseListener(columnMouseListener);
            columnMouseListener = null;
        }

    }

    public void updateColumns(final Tree tree, int size) {
        TreeColumn[] columns = tree.getColumns();
        for (int i = 1; i < columns.length; i++) {
            TreeColumn treeColumn = columns[i];
            if (i <= size) {
                // resize the columns.
                treeColumn.pack();
            } else {
                // dispose the unused columns.
                treeColumn.dispose();
            }
        }
    }

    public void packSchemaTreeFirstColumn(TreeViewer schemaTreeViewer) {
        if (schemaTreeViewer == null) {
            return;
        }
        schemaTreeViewer.getTree().getColumn(0).pack();
    }

}
