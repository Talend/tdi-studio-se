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
package org.talend.designer.core.ui.editor.properties.macrowidgets.tableeditor;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.talend.core.model.process.EParameterFieldType;
import org.talend.core.model.process.IElementParameter;
import org.talend.designer.core.i18n.Messages;

/**
 * DOC hcw class global comment. Detailled comment
 */
public class PromptDefaultValueDialog extends Dialog {

    private Table table;

    private List<TableEditor> editors;

    /**
     * DOC hcw PromptDefaultValueDialog constructor comment.
     *
     * @param parentShell
     */
    protected PromptDefaultValueDialog(Shell parentShell, List<ColumnInfo> inputs) {
        super(parentShell);
        setShellStyle(SWT.TITLE | SWT.RESIZE | SWT.APPLICATION_MODAL | getDefaultOrientation());
        super.create();
        getShell().setText(Messages.getString("PromptDefaultValueDialog.columnValue")); //$NON-NLS-1$
        setInput(inputs);
    }

    // @Override
    // protected void createButtonsForButtonBar(Composite parent) {
    //        createButton(parent, IDialogConstants.OK_ID, "OK", true); //$NON-NLS-1$
    // }
    @Override
    protected Control createDialogArea(Composite parent) {
        Composite composite = (Composite) super.createDialogArea(parent);
        initializeDialogUnits(composite);

        GridData data = new GridData(GridData.FILL_BOTH);
        // size of dialog
        data.heightHint = 240;
        data.widthHint = 260;
        composite.setLayoutData(data);
        createTable(composite);

        return composite;
    }

    /**
     * DOC hcw Comment method "createTableViewer".
     *
     * @param parent
     */
    private void createTable(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        composite.setLayout(new GridLayout());
        composite.setLayoutData(new GridData(GridData.FILL_BOTH));

        table = new Table(composite, SWT.BORDER);
        GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);

        table.setLayoutData(gd);

        table.setLinesVisible(true);
        table.setHeaderVisible(true);

        TableColumn nameColumn = new TableColumn(table, SWT.NONE);
        nameColumn.setWidth(120);
        nameColumn.setText(Messages.getString("PromptDefaultValueDialog.column")); //$NON-NLS-1$

        TableColumn defaultColumn = new TableColumn(table, SWT.NONE);
        defaultColumn.setWidth(108);
        defaultColumn.setText(Messages.getString("PromptDefaultValueDialog.defaultValue")); //$NON-NLS-1$
        editors = new ArrayList<TableEditor>();
    }

    public void setInput(List<ColumnInfo> inputs) {
        table.setRedraw(false);
        for (ColumnInfo row : inputs) {
            final TableItem item = new TableItem(table, SWT.NONE);
            item.setText(new String[] { row.name, "" }); //$NON-NLS-1$
            IElementParameter param = row.parameter;
            Object defaultValue = param.getValue();
            item.setData(row);

            if (param.getFieldType() == EParameterFieldType.CHECK) {
                // create check box
                createCheckboxEditor(item, 1, (Boolean) defaultValue);
            } else if (param.getFieldType() == EParameterFieldType.TEXT) {
                // create text input
                createTextEditor(item, 1, (String) defaultValue);
            } else if (param.getFieldType() == EParameterFieldType.CLOSED_LIST
                    || param.getFieldType() == EParameterFieldType.PREV_COLUMN_LIST) {
                // create combo
                int selectIndex = 0;
                Object[] values = param.getListItemsValue();
                for (int i = 0; i < values.length; i++) {
                    if (values[i].equals(param.getDefaultClosedListValue())) {
                        selectIndex = i;
                        break;

                    }
                }
                createComboEditor(item, 1, param.getListItemsDisplayName(), selectIndex);

            }
        }
        table.setRedraw(true);

    }

    private void createTextEditor(TableItem item, int column, String displayText) {
        TableEditor editor = new TableEditor(table);
        final Text text = new Text(table, SWT.NONE);
        text.setText(displayText);
        editor.setEditor(text, item, column);
        editor.grabHorizontal = true;
        editor.layout();
        editors.add(editor);
    }

    private void createCheckboxEditor(TableItem item, int column, boolean checked) {
        TableEditor editor = new TableEditor(table);
        // bgColorCheck = new Color(check.getDisplay(), 28, 81, 128);
        final Button check = new Button(table, SWT.CHECK);
        check.setSelection(checked);
        check.setBackground(table.getBackground());
        editor.setEditor(check, item, column);
        editor.grabHorizontal = true;
        editor.layout();
        editors.add(editor);
    }

    private void createComboEditor(TableItem item, int column, String[] displayText, int selectIndex) {
        TableEditor editor = new TableEditor(table);
        final CCombo combo = new CCombo(table, SWT.READ_ONLY);
        for (String text : displayText) {
            combo.add(text);
        }
        combo.select(selectIndex);
        editor.grabHorizontal = true;
        editor.setEditor(combo, item, column);
        // editor.minimumWidth = combo.computeSize(SWT.DEFAULT, SWT.DEFAULT).x;

        editor.layout();
        editors.add(editor);
    }

    @Override
    protected void okPressed() {
        for (TableEditor editor : editors) {
            Control control = editor.getEditor();
            TableItem item = editor.getItem();
            ColumnInfo row = (ColumnInfo) item.getData();

            EParameterFieldType field = row.parameter.getFieldType();
            if (field == EParameterFieldType.CHECK) {
                Button button = (Button) control;
                row.defaultValue = button.getSelection();
            } else if (field == EParameterFieldType.TEXT) {
                Text text = (Text) control;
                row.defaultValue = text.getText();
            } else if (field == EParameterFieldType.CLOSED_LIST || field == EParameterFieldType.PREV_COLUMN_LIST) {
                CCombo combo = (CCombo) control;
                int index = combo.getSelectionIndex();
                Object[] values = row.parameter.getListItemsValue();
                if (values.length > index && index != -1) {
                    row.defaultValue = values[index];
                }
            }
        }
        super.okPressed();
    }

    @Override
    public boolean close() {
        for (TableEditor editor : editors) {
            Control control = editor.getEditor();
            if (control != null && !control.isDisposed()) {
                control.dispose();
            }
            editor.dispose();
        }
        editors.clear();
        return super.close();
    }

    /**
     *
     * DOC chuang ErrorDetailDialog class global comment. Detailled comment
     */
    static class ColumnInfo {

        String name;

        IElementParameter parameter;

        Object defaultValue;
    }
}
