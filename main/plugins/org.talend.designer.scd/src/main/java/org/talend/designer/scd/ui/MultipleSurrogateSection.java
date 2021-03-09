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

import org.apache.commons.lang.StringUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.talend.core.ui.proposal.TalendProposalUtils;
import org.talend.designer.scd.ScdManager;
import org.talend.designer.scd.ScdPlugin;
import org.talend.designer.scd.model.SurrogateCreationType;
import org.talend.designer.scd.model.SurrogateKey;
import org.talend.designer.scd.util.DragDropManager;
import org.talend.designer.scd.util.IPropertySetter;
import org.talend.designer.scd.util.SWTResourceManager;
import org.talend.designer.scd.util.SurrogateKeyManager;
import org.talend.designer.scd.util.TableEditorManager;

/**
 * DOC chuang class global comment. Detailled comment
 */
public class MultipleSurrogateSection extends ScdSection {

    private static final int START_INDEX = 0;

    private static final int COMPLEMENT_INDEX = START_INDEX + 2;

    private static final int CREATION_INDEX = START_INDEX + 1;

    private static final int COLUMN_INDEX = START_INDEX;

    private static final Color ERROR_COLOR = SWTResourceManager.getColor(IColorConstants.RED);

    private Table table;

    private SurrogateKeyManager surrogateManager;

    private TableEditorManager editorManager;

    private List<SurrogateKey> tableModel = new ArrayList<SurrogateKey>();

    private DragDropManager dragDropManager;

    /**
     * DOC hcw MultipleSurrogateSection constructor comment.
     *
     * @param parent
     * @param width
     * @param height
     */
    public MultipleSurrogateSection(Composite parent, ScdManager scdManager) {
        super(parent, scdManager, false);
        surrogateManager = new SurrogateKeyManager();
        editorManager = new TableEditorManager();
        dragDropManager = new DragDropManager();
    }

    @Override
    protected void createContents(Composite composite) {
        table = new Table(composite, SWT.BORDER | SWT.MULTI | SWT.FULL_SELECTION);
        GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
        gd.horizontalAlignment = SWT.FILL;
        gd.verticalAlignment = SWT.FILL;
        table.setLayoutData(gd);
        table.setHeaderVisible(true);
        table.setLinesVisible(true);

        // TableColumn checkColumn = new TableColumn(table, SWT.NONE);
        // checkColumn.setWidth(15);

        TableColumn column = new TableColumn(table, SWT.NONE);
        column.setWidth(100);
        column.setText("column"); //$NON-NLS-1$

        TableColumn creation = new TableColumn(table, SWT.NONE);
        creation.setWidth(100);
        creation.setText("creation"); //$NON-NLS-1$

        TableColumn complement = new TableColumn(table, SWT.NONE);
        complement.setWidth(150);
        complement.setText("complement"); //$NON-NLS-1$

        TableColumn empty = new TableColumn(table, SWT.NONE);
        empty.setWidth(100);

        // createToolbar(composite);
    }

    /**
     * DOC hcw Comment method "createToolbar".
     *
     * @param composite
     */
    private void createToolbarActions(Composite composite) {
        ToolBar toolBar = new ToolBar(composite, SWT.NONE);
        toolBar.setLayoutData(new GridData(157, SWT.DEFAULT));

        ToolItem addToolItem = new ToolItem(toolBar, SWT.PUSH);
        addToolItem.setImage(SWTResourceManager.getPluginImage(ScdPlugin.getDefault(), "icons/add.gif")); //$NON-NLS-1$
        addToolItem.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                addButtonClick();
            }
        });

        ToolItem removeToolItem = new ToolItem(toolBar, SWT.PUSH);
        removeToolItem.setImage(SWTResourceManager.getPluginImage(ScdPlugin.getDefault(), "icons/delete.gif")); //$NON-NLS-1$
        removeToolItem.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                removeButtonClick();
            }

        });

        ToolItem moveUpToolItem = new ToolItem(toolBar, SWT.PUSH);
        moveUpToolItem.setImage(SWTResourceManager.getPluginImage(ScdPlugin.getDefault(), "icons/up.gif")); //$NON-NLS-1$
        moveUpToolItem.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                moveUpButtonClick();
            }

        });

        ToolItem moveDownToolItem = new ToolItem(toolBar, SWT.PUSH);
        moveDownToolItem.setImage(SWTResourceManager.getPluginImage(ScdPlugin.getDefault(), "icons/down.gif")); //$NON-NLS-1$
        moveDownToolItem.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                moveDownButtonClick();
            }

        });

    }

    public Table getTable() {
        return table;
    }

    /**
     * DOC hcw Comment method "moveDownButtonClick".
     */
    protected void moveDownButtonClick() {
        table.setRedraw(false);
        List<Integer> updateIndices = new ArrayList<Integer>(); // indices of item which will be updated
        int[] newSelectionIndices = adjustIndicesDown(table.getSelectionIndices(), tableModel, updateIndices);
        // move selection
        table.setSelection(newSelectionIndices);

        // remove editors
        for (int index : updateIndices) {
            editorManager.removeEditors(table.getItem(index));
        }
        // rebuild table item
        for (int index : updateIndices) {
            initTableItem(tableModel.get(index), table.getItem(index));
        }

        table.setRedraw(true);

    }

    /**
     * DOC hcw Comment method "moveUpButtonClick".
     */
    protected void moveUpButtonClick() {
        table.setRedraw(false);
        List<Integer> updateIndices = new ArrayList<Integer>(); // indices of item which will be updated
        int[] newSelectionIndices = adjustIndicesUp(table.getSelectionIndices(), tableModel, updateIndices);
        // move selection
        table.setSelection(newSelectionIndices);

        // remove editors
        for (int index : updateIndices) {
            editorManager.removeEditors(table.getItem(index));
        }
        // rebuild table item
        for (int index : updateIndices) {
            initTableItem(tableModel.get(index), table.getItem(index));
        }

        table.setRedraw(true);
    }

    /**
     * DOC hcw Comment method "removeButtonClick".
     */
    private void removeButtonClick() {
        table.setRedraw(false);
        TableItem[] items = table.getSelection();
        for (TableItem item : items) {
            // SurrogateKey key = (SurrogateKey) item.getData();
            editorManager.removeEditors(item);
            tableModel.remove(item.getData());
        }

        table.remove(table.getSelectionIndices());
        table.setRedraw(true);
    }

    public List<SurrogateKey> getTableData() {
        return tableModel;
    }

    public void setTableInput(List<SurrogateKey> input) {
        tableModel = new ArrayList<SurrogateKey>();
        if (input == null) {
            return;
        }
        for (SurrogateKey key : input) {
            createNewItem(key);
            surrogateManager.addSurrogateKey(key);
        }
    }

    @Override
    public List<String> getUsedFields() {
        List<String> fields = new ArrayList<String>();
        for (SurrogateKey key : tableModel) {
            if (key.getCreation() == SurrogateCreationType.INPUT_FIELD) {
                if (StringUtils.isNotEmpty(key.getComplement())) {
                    fields.add(key.getComplement());
                }
            }
        }
        return fields;
    }

    /**
     * DOC hcw Comment method "addButtonClick".
     */
    protected void addButtonClick() {
        SurrogateKey key = surrogateManager.createSurrogateKey();
        createNewItem(key);
    }

    public void createNewItem(final SurrogateKey key) {
        tableModel.add(key);
        table.setRedraw(false);
        final TableItem item = new TableItem(table, SWT.NONE);

        initTableItem(key, item);

        table.setRedraw(true);
    }

    /**
     * DOC hcw Comment method "initTableItem".
     *
     * @param key
     * @param item
     */
    private void initTableItem(final SurrogateKey key, final TableItem item) {
        item.setData(key);

        // text editor
        TableEditor editor = editorManager.createTextEditor(table, key.getColumn(), item, COLUMN_INDEX,
                new IPropertySetter<String>() {

                    public void set(String value) {
                        key.setColumn(value);
                    }
                });

        // combo editor
        // final List<String> outputColumns = scdManager.getOutputColumnNames();
        // int index = editorManager.getComboIndex(outputColumns, key.getColumn());
        // final TableEditor editor = editorManager.createComboEditor(table,
        // outputColumns.toArray(new String[outputColumns.size()]), item, COLUMN_INDEX, index,
        // new IPropertySetter<Integer>() {
        //
        // public void set(Integer value) {
        // key.setColumn(outputColumns.get(value));
        // }
        // });
        editor.layout();

        TableEditor comboEditor = editorManager.createComboEditor(table, getScdManager().getSurrogateCreationTypeNames(), item,
                CREATION_INDEX, key.getCreation().getIndex(), new IPropertySetter<Integer>() {

                    public void set(Integer value) {
                        key.setCreation(SurrogateCreationType.getTypeByIndex(value));
                        // remove value from model
                        key.setComplement(""); //$NON-NLS-1$
                        creationComplement(value, key, item);
                    }
                });

        creationComplement(key.getCreation().getIndex(), key, item);
    }

    /**
     * DOC hcw Comment method "markItemAsSelected".
     *
     * @param item
     * @param checked
     */
    protected void setItemChecked(TableItem item, boolean checked) {
        for (int i = 0; i < table.getItemCount(); i++) {
            if (item == table.getItem(i)) {

                if (checked) {
                    table.setSelection(add(table.getSelectionIndices(), i));
                } else {
                    table.setSelection(remove(table.getSelectionIndices(), i));
                }

                break;
            }
        }
    }

    /**
     * DOC hcw Comment method "remove".
     *
     * @param selectionIndices
     * @param i
     * @return
     */
    private int[] remove(int[] arr, int value) {
        int found = indexOf(arr, value);

        if (found > -1) {
            int[] newArr = copyOfArray(arr, arr.length - 1);
            if (found < newArr.length) {
                newArr[found] = arr[arr.length - 1]; // replace with the last element
            }
            return newArr;
        } else {
            return copyOfArray(arr, arr.length);
        }
    }

    public static int[] copyOfArray(int[] original, int newLength) {
        int[] copy = new int[newLength];
        System.arraycopy(original, 0, copy, 0, Math.min(original.length, newLength));
        return copy;
    }

    /**
     * DOC hcw Comment method "add".
     *
     * @param selectionIndices
     * @param i
     * @return
     */
    private int[] add(int[] arr, int value) {
        int found = indexOf(arr, value);

        if (found > -1) {
            return copyOfArray(arr, arr.length);
        } else {
            int[] newArr = copyOfArray(arr, arr.length + 1);
            newArr[arr.length] = value;
            return newArr;
        }
    }

    /**
     * DOC hcw Comment method "find".
     *
     * @param arr
     * @param value
     * @return
     */
    private int indexOf(int[] arr, int value) {
        for (int i = 0; i < arr.length; i++) {
            if (arr[i] == value) {
                return i;
            }
        }
        return -1;
    }

    /**
     * DOC hcw Comment method "isItemSelected".
     *
     * @param item
     * @return
     */
    private boolean isItemSelected(TableItem item) {
        for (TableItem i : table.getSelection()) {
            if (i == item) {
                return true;
            }
        }
        return false;
    }

    /**
     * DOC hcw Comment method "onCreationChange".
     *
     * @param value
     * @param key
     * @param item
     */
    protected void creationComplement(Integer value, final SurrogateKey key, final TableItem item) {

        // dispose editor
        TableEditor editor = editorManager.getEditor(item, COMPLEMENT_INDEX);
        editorManager.disposeEditor(editor);

        if (value == SurrogateCreationType.ROUTINE.getIndex()) {
            // create routine editor
            editor = editorManager.createTextEditor(editor, table, key.getComplement(), item, COMPLEMENT_INDEX,
                    new IPropertySetter<String>() {

                        public void set(String value) {
                            key.setComplement(value);
                        }
                    });
            // add content proposal
            TalendProposalUtils.installOn(editor.getEditor(), null);
        } else if (value == SurrogateCreationType.DB_SEQUENCE.getIndex()) {
            editor = editorManager.createTextEditor(editor, table, key.getComplement(), item, COMPLEMENT_INDEX,
                    new IPropertySetter<String>() {

                        public void set(String value) {
                            key.setComplement(value);
                        }
                    });
            // add content proposal
            TalendProposalUtils.installOn(editor.getEditor(), null);
        } else if (value == SurrogateCreationType.INPUT_FIELD.getIndex()) {
            // combo editor
            // final List<String> inputColumns = scdManager.getInputColumnNames();
            // inputColumns.add(0, "");
            // int index = editorManager.getComboIndex(inputColumns, key.getComplement());
            // editor = editorManager.createComboEditor(table, inputColumns.toArray(new String[inputColumns.size()]),
            // item,
            // COMPLEMENT_INDEX, index, new IPropertySetter<Integer>() {
            //
            // public void set(Integer value) {
            // key.setComplement(inputColumns.get(value));
            // Color color = value == 0 ? ERROR_COLOR : null;
            // editorManager.setComboColor(item, COMPLEMENT_INDEX, color);
            // scdManager.fireFieldChange();
            // }
            // });
            //
            // if (StringUtils.isEmpty(key.getComplement())) {
            // // display as error status, this field must not be null
            // editorManager.setComboColor(item, COMPLEMENT_INDEX, ERROR_COLOR);
            // }

            // label editor
            editor = editorManager.createLabelEditor(editor, table, key.getComplement(), item, COMPLEMENT_INDEX);
            final Label text = (Label) editor.getEditor();
            if (StringUtils.isEmpty(key.getComplement())) {
                // display as error status, this field must not be null
                text.setBackground(ERROR_COLOR);
            }
            // add drag and drop support
            // IDragDropDelegate delegate = createDragDropDelegate(key, text);
            // dragDropManager.addDragSupport(text, delegate);
            // dragDropManager.addDropSupport(text, delegate);
        }
    }

    // private IDragDropDelegate createDragDropDelegate(final SurrogateKey key, final Label text) {
    // return new IDragDropDelegate() {
    //
    // public String getDragItemsAsText() {
    //                return "1|" + text.getText(); //$NON-NLS-1$
    // }
    //
    // public void onDropItems(String data, Point position) {
    //                String[] items = data.split("\\|"); //$NON-NLS-1$
    // text.setText(items[1]);
    // key.setComplement(items[1]);
    // text.setBackground(null);
    // }
    //
    // public void removeDragItems() {
    //                text.setText(""); //$NON-NLS-1$
    //                key.setComplement(""); //$NON-NLS-1$
    // // display as error status, this field must not be null
    // text.setBackground(ERROR_COLOR);
    // // don't remove table item here, it can be removed by the delete button
    // }
    //
    // public boolean isDropAllowed(String data) {
    // // only allow single selection
    //                return StringUtils.isEmpty(key.getComplement()) && data.startsWith("1|"); //$NON-NLS-1$
    // }
    //
    // };
    //
    // }

    /**
     * DOC chuang Comment method "addContextHelp".
     *
     * @param scdDialog
     */
    public void addContextHelp(AbstractScdDialog scdDialog) {
        scdDialog.addContextHelp(getTable(), "org.talend.designer.scd.surrogateKey"); //$NON-NLS-1$
    }

}
