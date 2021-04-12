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
package org.talend.repository.json.ui.wizards.view;

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
import org.talend.commons.ui.swt.tableviewer.IModifiedBeanListener;
import org.talend.commons.ui.swt.tableviewer.ModifiedBeanEvent;
import org.talend.core.model.metadata.builder.connection.MetadataColumn;
import org.talend.core.model.metadata.builder.connection.MetadataTable;
import org.talend.core.ui.metadata.editor.AbstractMetadataTableEditorView;
import org.talend.core.ui.metadata.editor.MetadataEmfTableEditor;
import org.talend.core.ui.metadata.editor.MetadataEmfTableEditorView;
import org.talend.repository.json.ui.wizards.AbstractJSONStepForm;

/**
 * wzhang class global comment. Detailled comment
 */
public class JSONFileSchemaDialog extends Dialog {

    protected AbstractJSONStepForm form;

    protected MetadataEmfTableEditor tableEditor;

    protected MetadataEmfTableEditorView tableEditorView;

    protected Map<MetadataColumn, String> changedNameColumns = new HashMap<MetadataColumn, String>();

    public JSONFileSchemaDialog(Shell parent, AbstractJSONStepForm form) {
        super(parent);
        this.form = form;

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
        MetadataTable metadataTable = form.getMetadataTable();
        tableEditor.setMetadataTable(metadataTable);
        // tableEditor.removeAll();
        List<MetadataColumn> list = (List<MetadataColumn>) form.getSchemaViewer().getInput();
        tableEditor.addAll(list);

        // if (list.size() > 0) {
        // for (MetadataColumn column : list) {
        // boolean toAdd = true;
        // for (int i = 0; i < currentColumnList.size(); i++) {
        // String label = column.getLabel();
        // String curLabel = currentColumnList.get(i).getLabel();
        // if (label.equals(curLabel)) {
        // toAdd = false;
        // break;
        // }
        // }
        // if (toAdd) {
        // tableEditor.add(column);
        // }
        // }
        // }
        tableEditorView = new MetadataEmfTableEditorView(composite, SWT.NONE);
        tableEditorView.setMetadataEditor(tableEditor);
        addListeners();
        return composite;
    }

    protected void addListeners() {
        tableEditor.addModifiedBeanListener(new IModifiedBeanListener<MetadataColumn>() {

            public void handleEvent(ModifiedBeanEvent<MetadataColumn> event) {
                if (AbstractMetadataTableEditorView.ID_COLUMN_NAME.equals(event.column.getId())) {
                    MetadataColumn modifiedColumn = event.bean;
                    if (modifiedColumn != null) {
                        String originalLabel = changedNameColumns.get(modifiedColumn);
                        if (originalLabel == null) {
                            changedNameColumns.put(modifiedColumn, (String) event.previousValue);
                        }
                    }
                }
            }
        });
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
