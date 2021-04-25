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
package org.talend.designer.scd.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.talend.designer.scd.ui.ColorCombo;

/**
 * Helper for creating editor on table.
 */
public class TableEditorManager {

    private Map<TableItem, List<TableEditor>> editorMap = new HashMap<TableItem, List<TableEditor>>();

    public TableEditor createTextEditor(Table table, String displayText, TableItem item, int column,
            final IPropertySetter<String> accessor) {
        TableEditor editor = new TableEditor(table);
        createTextEditor(editor, table, displayText, item, column, accessor);

        editor.grabHorizontal = true;
        addEditor(item, column, editor);
        return editor;
    }

    public TableEditor createTextEditor(TableEditor editor, Table table, String displayText, TableItem item, int column,
            final IPropertySetter<String> accessor) {
        if (editor == null) {
            return createTextEditor(table, displayText, item, column, accessor);
        }
        final Text text = new Text(table, SWT.NONE);

        if (displayText != null) {
            text.setText(displayText);
        }
        // update model when ui input has changed
        if (accessor != null) {
            text.addModifyListener(new ModifyListener() {

                public void modifyText(ModifyEvent e) {
                    accessor.set(text.getText());
                }
            });
        }
        editor.setEditor(text, item, column);
        return editor;
    }

    public TableEditor createLabelEditor(TableEditor editor, Table table, String displayText, TableItem item, int column) {
        if (editor == null) {
            editor = new TableEditor(table);
            editor.grabHorizontal = true;
            addEditor(item, column, editor);
        }
        final Label label = new Label(table, SWT.NONE);

        if (displayText != null) {
            label.setText(displayText);
        }

        editor.setEditor(label, item, column);
        addEditor(item, column, editor);
        return editor;
    }

    public static int getComboIndex(List<String> items, String text) {
        if (StringUtils.isEmpty(text)) {
            return 0;
        } else {
            return items.indexOf(text);
        }
    }

    public TableEditor createComboEditor(Table table, String[] displayText, TableItem item, int column, int selectIndex,
            final IPropertySetter<Integer> accessor) {
        TableEditor editor = new TableEditor(table);
        final ColorCombo combo = new ColorCombo(table, SWT.READ_ONLY);
        for (String text : displayText) {
            combo.add(text);
        }
        if (accessor != null) {
            combo.addSelectionListener(new SelectionAdapter() {

                @Override
                public void widgetSelected(SelectionEvent e) {
                    accessor.set(combo.getSelectionIndex());
                }

            });
        }
        combo.select(selectIndex);
        editor.grabHorizontal = true;
        editor.setEditor(combo, item, column);
        editor.minimumWidth = combo.computeSize(SWT.DEFAULT, SWT.DEFAULT).x;
        table.getColumn(column).setWidth(editor.minimumWidth);
        addEditor(item, column, editor);
        return editor;
    }

    public TableEditor createCheckboxEditor(Table table, boolean checked, TableItem item, int column,
            final IPropertySetter<Boolean> accessor) {
        TableEditor editor = new TableEditor(table);
        final Button button = new Button(table, SWT.CHECK);
        button.setSelection(checked);
        if (accessor != null) {
            button.addSelectionListener(new SelectionAdapter() {

                @Override
                public void widgetSelected(SelectionEvent e) {
                    accessor.set(button.getSelection());
                }

            });
        }
        editor.grabHorizontal = true;
        editor.setEditor(button, item, column);
        addEditor(item, column, editor);
        return editor;
    }

    public void addEditor(TableItem item, int column, TableEditor editor) {
        ArrayList<TableEditor> editors = (ArrayList<TableEditor>) editorMap.get(item);
        if (editors == null) {
            editors = new ArrayList<TableEditor>();
            editorMap.put(item, editors);
        }

        if (editors.size() <= column) {
            for (int i = editors.size(); i <= column; i++) {
                // add place holder to prevent index out of bound exception
                editors.add(null);
            }
        }
        editors.set(column, editor);
    }

    public TableEditor getEditor(TableItem item, int column) {
        ArrayList<TableEditor> editors = (ArrayList<TableEditor>) editorMap.get(item);
        TableEditor editor = null;
        if (editors != null && column < editors.size()) {
            editor = editors.get(column);
        }
        return editor;
    }

    public void removeEditors(TableItem item) {
        ArrayList<TableEditor> editors = (ArrayList<TableEditor>) editorMap.get(item);
        if (editors != null) {
            for (TableEditor editor : editors) {
                disposeEditor(editor);
                if (editor != null) {
                    editor.dispose();
                }
            }
            editors.clear();
        }
    }

    public void disposeEditor(TableItem item, int column) {
        disposeEditor(getEditor(item, column));
    }

    public void disposeEditor(TableEditor editor) {
        // dispose editor
        if (editor != null) {
            Control control = editor.getEditor();
            if (control != null) {
                control.dispose();
            }
        }
    }

    public void setComboColor(TableItem item, int column, Color color) {
        ColorCombo combo = (ColorCombo) getEditor(item, column).getEditor();
        combo.setTextBackground(color);
    }

}
