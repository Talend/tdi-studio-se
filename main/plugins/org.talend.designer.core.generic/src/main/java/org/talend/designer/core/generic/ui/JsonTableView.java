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
package org.talend.designer.core.generic.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.gef.commands.Command;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.talend.commons.ui.swt.advanced.dataeditor.AbstractDataTableEditorView;
import org.talend.commons.ui.swt.advanced.dataeditor.ExtendedToolbarView;
import org.talend.commons.ui.swt.advanced.dataeditor.button.AddPushButtonForExtendedTable;
import org.talend.commons.ui.swt.advanced.dataeditor.button.PastePushButton;
import org.talend.commons.ui.swt.advanced.dataeditor.button.PastePushButtonForExtendedTable;
import org.talend.commons.ui.swt.advanced.dataeditor.commands.ExtendedTablePasteCommand;
import org.talend.commons.ui.swt.extended.table.ExtendedTableModel;
import org.talend.commons.ui.swt.tableviewer.TableViewerCreator;
import org.talend.commons.ui.swt.tableviewer.TableViewerCreatorColumn;
import org.talend.commons.utils.data.bean.IBeanPropertyAccessors;
import org.talend.commons.utils.data.list.ListenableListEvent;

/**
 *
 * created by ycbai on 2015年1月4日 Detailled comment
 *
 */
public class JsonTableView extends AbstractDataTableEditorView<Map<String, Object>> {

    private List<String> columnTitles;

    public JsonTableView(Composite parent, JsonFieldModel model, boolean readonly, boolean toolbarVisible) {
        super(parent, SWT.NONE, model, readonly, toolbarVisible, false);
    }

    @Override
    protected void handleBeforeListenableListOperationEvent(ListenableListEvent<Map<String, Object>> event) {
        super.handleBeforeListenableListOperationEvent(event);
    }

    @Override
    protected void handleAfterListenableListOperationEvent(ListenableListEvent<Map<String, Object>> event) {
        super.handleAfterListenableListOperationEvent(event);
    }

    @Override
    protected void setTableViewerCreatorOptions(TableViewerCreator<Map<String, Object>> newTableViewerCreator) {
        super.setTableViewerCreatorOptions(newTableViewerCreator);
    }

    @Override
    protected void createColumns(TableViewerCreator<Map<String, Object>> creator, Table table) {
        columnTitles = getColumnTitles();
        if (columnTitles == null || columnTitles.size() == 0) {
            return;
        }
        for (String columnTitle : columnTitles) {
            createColumn(creator, columnTitle);
        }
    }

    protected List<String> getColumnTitles() {
        return new ArrayList<>();
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private TableViewerCreatorColumn createColumn(TableViewerCreator<Map<String, Object>> tableViewerCreator, String title) {
        TableViewerCreatorColumn column = new TableViewerCreatorColumn(tableViewerCreator);
        column.setTitle(title);
        column.setBeanPropertyAccessors(new IBeanPropertyAccessors<Map<String, Object>, String>() {

            @Override
            public String get(Map<String, Object> bean) {
                return (String) bean.get(title);
            }

            @Override
            public void set(Map<String, Object> bean, String value) {
                bean.put(title, value);
            }

        });
        column.setCellEditor(new TextCellEditor(tableViewerCreator.getTable()));

        column.setModifiable(!isReadOnly());
        column.setWeight(30);
        column.setMinimumWidth(50);
        column.setDefaultInternalValue(""); //$NON-NLS-1$
        return column;
    }

    private JsonFieldModel getModel() {
        return (JsonFieldModel) getExtendedTableModel();
    }

    @Override
    protected ExtendedToolbarView initToolBar() {
        return new ExtendedToolbarView(getMainComposite(), SWT.NONE, getExtendedTableViewer()) {

            @Override
            protected AddPushButtonForExtendedTable createAddPushButton() {
                return new AddPushButtonForExtendedTable(this.toolbar, getExtendedTableViewer()) {

                    @Override
                    protected Object getObjectToAdd() {
                        Map<String, Object> row = getModel().createJsonRow();
                        if (columnTitles != null && columnTitles.size() > 0) {
                            for (int i = 0; i < columnTitles.size(); i++) {
                                String columnTitle = columnTitles.get(i);
                                if (i == 0) {
                                    row.put(columnTitle, "new line"); //$NON-NLS-1$
                                } else {
                                    row.put(columnTitle, ""); //$NON-NLS-1$
                                }
                            }
                        }
                        return row;
                    }
                };
            }

            @Override
            protected PastePushButton createPastePushButton() {
                return new PastePushButtonForExtendedTable(toolbar, extendedTableViewer) {

                    @Override
                    protected Command getCommandToExecute(ExtendedTableModel tableModel, Integer indexWhereInsert) {
                        return new ExtendedTablePasteCommand(tableModel, indexWhereInsert) {

                            @SuppressWarnings("unchecked")
                            @Override
                            public List<Map<String, Object>> createPastableBeansList(ExtendedTableModel model,
                                    List copiedObjectsList) {
                                List<Map<String, Object>> list = new ArrayList<>();
                                JsonFieldModel fieldsModel = (JsonFieldModel) model;
                                for (Object current : copiedObjectsList) {
                                    if (current instanceof Map) {
                                        Map<String, Object> original = (Map<String, Object>) current;
                                        Map<String, Object> copy = fieldsModel.createJsonRow();
                                        copy.putAll(original);
                                        list.add(copy);
                                    }
                                }
                                return list;
                            }
                        };
                    }

                };
            }
        };
    }

}
