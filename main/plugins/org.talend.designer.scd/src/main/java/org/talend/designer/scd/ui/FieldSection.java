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
package org.talend.designer.scd.ui;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.ComboBoxCellEditor;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.talend.designer.scd.ScdManager;
import org.talend.designer.scd.ScdParameterConstants;
import org.talend.designer.scd.util.DragDropManager;

/**
 * DOC hcw class global comment. Detailled comment
 */
public class FieldSection extends ScdSection implements IDragDropDelegate {

    private TableViewer tableViewer;

    private DragDropManager dragDropManager;

    private List<String> tableModel;

    private boolean editable;

    private int type;

    /**
     * DOC hcw FieldSection constructor comment.
     *
     * @param parent
     */
    public FieldSection(Composite parent, ScdManager scdManager, boolean toolbarNeeded, boolean editable) {
        super(parent, scdManager, toolbarNeeded);
        dragDropManager = new DragDropManager();
        dragDropManager.addDragSupport(tableViewer.getTable(), this);
        dragDropManager.addDropSupport(tableViewer.getTable(), this);
        tableModel = new ArrayList<String>();
        this.editable = editable;
    }

    /**
     * DOC hcyi FieldSection constructor comment.
     *
     * @param parent
     */
    public FieldSection(Composite parent, ScdManager scdManager, boolean toolbarNeeded, boolean editable, int type) {
        super(parent, scdManager, toolbarNeeded);
        dragDropManager = new DragDropManager();
        dragDropManager.addDragSupport(tableViewer.getTable(), this);
        dragDropManager.addDropSupport(tableViewer.getTable(), this);
        tableModel = new ArrayList<String>();
        this.editable = editable;
        this.type = type;
    }

    @Override
    protected void createContents(Composite composite) {
        tableViewer = new TableViewer(composite, SWT.FULL_SELECTION | SWT.MULTI | SWT.V_SCROLL);
        final Table table = tableViewer.getTable();
        table.setLinesVisible(true);
        table.setHeaderVisible(false);

        GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
        gd.horizontalAlignment = SWT.FILL;
        gd.verticalAlignment = SWT.FILL;
        table.setLayoutData(gd);

        TableViewerColumn column = new TableViewerColumn(tableViewer, SWT.NONE);
        table.addListener(SWT.Resize, new Listener() {

			public void handleEvent(Event event) {
				TableColumn[] columns = table.getColumns();
                int clientWidth = table.getBounds().width;
                columns[0].setWidth(clientWidth);

			}
        });

        if (editable) {
            column.setEditingSupport(new FieldEditingSupport(tableViewer, 0));
        }

        tableViewer.setLabelProvider(new TableLabelProvider());
        tableViewer.setContentProvider(new ContentProvider());
        // sorter
        // tableViewer.setSorter(new ViewerSorter());

    }

    public void setSortable(boolean sortable) {
        if (sortable) {
            tableViewer.setSorter(new ViewerSorter());
        } else {
            tableViewer.setSorter(null);
        }
    }

    public void setTableInput(List<String> input) {
        if (input == null) {
            tableModel.clear();
            return;
        }
        tableModel = input;
        tableViewer.setInput(input);
        tableViewer.refresh();
    }

    public TableViewer getTableViewer() {
        return tableViewer;
    }

    @Override
    protected void addToolbarListener() {
        addEntryItem.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                List<String> names = scdManager.getInputColumnNames();
                if (names.size() > 0) {
                    String value = names.get(0);
                    tableModel.add(value);
                    tableViewer.setInput(tableModel);
                    // tableViewer.refresh();
                }
                scdManager.fireFieldChange();
            }
        });

        removeEntryItem.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                int[] selection = tableViewer.getTable().getSelectionIndices();
                if (selection == null || selection.length == 0) {
                    return;
                }
                tableViewer.cancelEditing();
                for (int i = selection.length - 1; i >= 0; i--) {
                    tableModel.remove(selection[i]);
                }
                tableViewer.setInput(tableModel);
                scdManager.fireFieldChange();
            }
        });

        moveUpEntryItem.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                int[] selection = tableViewer.getTable().getSelectionIndices();
                if (selection == null || selection.length == 0) {
                    return;
                }
                tableViewer.cancelEditing();
                List<Integer> updateIndices = new ArrayList<Integer>(); // indices of item which will be updated
                int[] newSelectionIndices = adjustIndicesUp(selection, tableModel, updateIndices);
                // refresh
                tableViewer.setInput(tableModel);
                // tableViewer.getTable().setSelection(newSelectionIndices);
            }
        });

        moveDownEntryItem.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                int[] selection = tableViewer.getTable().getSelectionIndices();
                if (selection == null || selection.length == 0) {
                    return;
                }
                tableViewer.cancelEditing();
                List<Integer> updateIndices = new ArrayList<Integer>(); // indices of item which will be updated
                int[] newSelectionIndices = adjustIndicesDown(selection, tableModel, updateIndices);
                // refresh
                tableViewer.setInput(tableModel);
                // tableViewer.getTable().setSelection(newSelectionIndices);
            }
        });

    }

    @Override
    public void onUnusedFieldsChange(List<String> fields) {
        setTableInput(fields);
    }

    @Override
    public List<String> getUsedFields() {
        return new ArrayList<String>(tableModel);
    }

    public List<String> getTableData() {
        // Table table = tableViewer.getTable();
        // List<String> result = new ArrayList<String>(table.getItemCount());
        // for (TableItem item : table.getItems()) {
        // result.add(item.getText());
        // }
        // return result;
        return tableModel;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.scd.ui.IDragDrop#getDragItemsAsText()
     */
    public String getDragItemsAsText() {
        tableViewer.cancelEditing();
        Table table = tableViewer.getTable();
        TableItem[] selection = table.getSelection();

        StringBuffer buf = new StringBuffer();
        // number of selected elements
        buf.append(selection.length);
        for (TableItem element : selection) {
            buf.append('|');
            buf.append(element.getText());
        }

        return buf.toString();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.scd.ui.IDragDrop#onDropItems(java.lang.String, org.eclipse.swt.graphics.Point)
     */
    public void onDropItems(String data, Point position) {
        String[] items = data.split("\\|"); //$NON-NLS-1$
        // skip items[0], which is the number of selected elements
        for (int i = 1; i < items.length; i++) {
            switch (type) {
            case ScdParameterConstants.DROP_COPY_TYPE1FIELDS:
            case ScdParameterConstants.DROP_COPY_TYPE2FIELDS:
                if (!scdManager.getTypeTable().contains(items[i])) {
                    scdManager.getTypeTable().add(items[i]);
                } else {
                    continue;
                }
                break;
            case ScdParameterConstants.DROP_COPY_SOURCEKEYS:
                if (!scdManager.getSourceKeys().contains(items[i])) {
                    scdManager.getSourceKeys().add(items[i]);
                } else {
                    continue;
                }
                break;
            }
            if (type != ScdParameterConstants.DROP_COPY_SOURCEKEYS) {
                tableModel.add(items[i]);
            }
        }
        tableViewer.setInput(tableModel);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.scd.ui.IDragDrop#removeDragItems()
     */
    public void removeDragItems(String data) {
        Table table = tableViewer.getTable();
        table.getSelection();
        TableItem[] items = table.getItems();
        String[] toRemove = data.split("\\|"); //$NON-NLS-1$

        for (int i = 1; i < toRemove.length; i++) { // skip items[0], which is the number of selected elements
            for (TableItem item : items) {
                if (toRemove[i].equals(item.getText())) {
                    tableModel.remove(item.getData());
                    switch (type) {
                    case ScdParameterConstants.DROP_COPY_TYPE1FIELDS:
                    case ScdParameterConstants.DROP_COPY_TYPE2FIELDS:
                        if (scdManager.getTypeTable().contains(item.getData())) {
                            scdManager.getTypeTable().remove(item.getData());
                        }
                        break;
                    case ScdParameterConstants.DROP_COPY_SOURCEKEYS:
                        if (scdManager.getSourceKeys().contains(item.getData())) {
                            scdManager.getSourceKeys().remove(item.getData());
                        }
                        break;
                    }
                    break;
                }
            }

        }
        table.remove(table.getSelectionIndices());
        tableViewer.refresh();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.scd.ui.IDragDropDelegate#isDropAllowed(java.lang.String)
     */
    public boolean isDropAllowed(String data) {
        switch (type) {
        case ScdParameterConstants.DROP_COPY_TYPE1FIELDS:
        case ScdParameterConstants.DROP_COPY_TYPE2FIELDS:
            List<String> typeTable = scdManager.getTypeTable();
            String[] items = data.split("\\|");
            // skip items[0], which is the number of selected elements
            for (int i = 1; i < items.length; i++) {
                // drop to current field
                if (typeTable.contains(items[i])) {
                    return false;
                }
            }
            break;
        case ScdParameterConstants.DROP_COPY_SOURCEKEYS:
            List<String> sourceKeys = scdManager.getSourceKeys();
            String[] sourceKeyItems = data.split("\\|");
            // skip items[0], which is the number of selected elements
            for (int i = 1; i < sourceKeyItems.length; i++) {
                // drop to current field
                if (sourceKeys.contains(sourceKeyItems[i])) {
                    return false;
                }
            }
            break;
        }
        return true;
    }

    static class TableLabelProvider extends LabelProvider implements ITableLabelProvider {

        public String getColumnText(Object element, int columnIndex) {
            return element.toString();
        }

        public Image getColumnImage(Object element, int columnIndex) {
            return null;
        }
    }

    static class ContentProvider implements IStructuredContentProvider {

        public Object[] getElements(Object inputElement) {
            return ((List) inputElement).toArray();
        }

        public void dispose() {
        }

        public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
        }
    }

    class FieldEditingSupport extends EditingSupport {

        private CellEditor editor;

        private int column;

        private ColumnViewer viewer;

        private List<String> columnNames;

        public FieldEditingSupport(ColumnViewer viewer, int column) {
            super(viewer);
            this.column = column;
            this.viewer = viewer;
            columnNames = scdManager.getInputColumnNames();
            editor = new ComboBoxCellEditor(((TableViewer) viewer).getTable(),
                    columnNames.toArray(new String[columnNames.size()]), SWT.READ_ONLY);
        }

        /*
         * (non-Javadoc)
         *
         * @see org.eclipse.jface.viewers.EditingSupport#canEdit(java.lang.Object)
         */
        @Override
        protected boolean canEdit(Object element) {
            return true;
        }

        /*
         * (non-Javadoc)
         *
         * @see org.eclipse.jface.viewers.EditingSupport#getCellEditor(java.lang.Object)
         */
        @Override
        protected CellEditor getCellEditor(Object element) {
            return editor;
        }

        /*
         * (non-Javadoc)
         *
         * @see org.eclipse.jface.viewers.EditingSupport#getValue(java.lang.Object)
         */
        @Override
        protected Object getValue(Object element) {
            return columnNames.indexOf(element);
        }

        /*
         * (non-Javadoc)
         *
         * @see org.eclipse.jface.viewers.EditingSupport#setValue(java.lang.Object, java.lang.Object)
         */
        @Override
        protected void setValue(Object element, Object value) {
        }

        @Override
        protected void saveCellEditorValue(CellEditor cellEditor, ViewerCell cell) {
            Integer value = (Integer) cellEditor.getValue();
            int index = tableViewer.getTable().getSelectionIndex();
            if (index < 0 || index >= tableModel.size() || value < 0) {
                return;
            }
            tableModel.set(index, columnNames.get(value));
            tableViewer.refresh();
            scdManager.fireFieldChange();
        }
    }

}
