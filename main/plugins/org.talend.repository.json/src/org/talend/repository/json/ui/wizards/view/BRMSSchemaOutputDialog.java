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

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.talend.core.model.metadata.builder.connection.MetadataTable;
import org.talend.core.ui.metadata.editor.MetadataEmfTableEditor;
import org.talend.core.ui.metadata.editor.MetadataEmfTableEditorView;
import org.talend.repository.json.ui.wizards.AbstractJSONStepForm;

/**
 * DOC hywang class global comment. Detailled comment
 */
public class BRMSSchemaOutputDialog extends JSONFileSchemaDialog {

    public BRMSSchemaOutputDialog(Shell parent, AbstractJSONStepForm form) {
        super(parent, form);
        this.form = form;
    }

    protected Control createDialogArea(Composite parent) {

        Composite composite = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout();
        layout.marginHeight = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_MARGIN);
        layout.marginWidth = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_MARGIN);
        layout.verticalSpacing = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_SPACING);
        layout.horizontalSpacing = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_SPACING);
        composite.setLayout(layout);
        composite.setLayoutData(new GridData(GridData.FILL_BOTH));
        applyDialogFont(composite);

        // Composite composite = (Composite) super.createDialogArea(parent);
        composite.setLayout(new FillLayout());

        tableEditor = new MetadataEmfTableEditor("");
        MetadataTable metadataTable = form.getMetadataOutputTable();
        tableEditor.setMetadataTable(metadataTable);
        tableEditor.removeAll();
        // List<MetadataColumn> list = (List<MetadataColumn>) metadataTable.getColumns();
        // tableEditor.addAll(list);
        // if (list.size() > 0) {
        // for (MetadataColumn column : list) {
        // boolean toAdd = true;
        // for (int i = 0; i < list.size(); i++) {
        // String label = column.getLabel();
        // String curLabel = list.get(i).getLabel();
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

    @Override
    public MetadataTable getMetadataTable() {
        return tableEditor.getMetadataTable();
    }

}
