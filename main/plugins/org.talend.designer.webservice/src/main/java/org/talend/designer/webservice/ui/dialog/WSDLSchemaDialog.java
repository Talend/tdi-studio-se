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
package org.talend.designer.webservice.ui.dialog;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.talend.core.model.metadata.builder.connection.MetadataColumn;
import org.talend.core.model.metadata.builder.connection.MetadataTable;
import org.talend.core.ui.metadata.editor.MetadataEmfTableEditor;
import org.talend.core.ui.metadata.editor.MetadataEmfTableEditorView;

/**
 * DOC Administrator class global comment. Detailled comment
 */
public class WSDLSchemaDialog extends Dialog {

    /**
     * DOC Administrator WSDLSchemaDialog constructor comment.
     *
     * @param parentShell
     */

    private MetadataEmfTableEditor tableEditor;

    private MetadataEmfTableEditorView tableEditorView;

    private Map<MetadataColumn, String> changedNameColumns = new HashMap<MetadataColumn, String>();

    private MetadataTable metadataTable;

    public WSDLSchemaDialog(Shell parentShell, MetadataTable metadataTable) {
        super(parentShell);
        this.metadataTable = metadataTable;
        setShellStyle(getShellStyle() | SWT.DIALOG_TRIM | SWT.MIN | SWT.MAX | SWT.APPLICATION_MODAL | SWT.RESIZE);
    }

    @Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText("Schema");
        newShell.setSize(550, 400);
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        Composite composite = (Composite) super.createDialogArea(parent);
        composite.setLayout(new FillLayout());
        tableEditor = new MetadataEmfTableEditor("");

        tableEditor.setMetadataTable(metadataTable);

        List<MetadataColumn> list = metadataTable.getColumns();// (List<MetadataColumn>)
        // form.getSchemaViewer().getInput();
        tableEditor.addAll(list);
        tableEditorView = new MetadataEmfTableEditorView(composite, SWT.NONE);
        tableEditorView.setMetadataEditor(tableEditor);
        return composite;
    }

    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, false);
        createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
    }

    public MetadataTable getMetadataTable() {
        return tableEditor.getMetadataTable();
    }

    public Map<MetadataColumn, String> getChangedNameColumns() {
        return this.changedNameColumns;
    }
}
