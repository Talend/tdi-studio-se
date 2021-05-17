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

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.talend.designer.scd.ScdManager;
import org.talend.designer.scd.model.VersionEndType;
import org.talend.designer.scd.model.VersionStartType;
import org.talend.designer.scd.model.Versioning;
import org.talend.designer.scd.util.DragDropManager;
import org.talend.designer.scd.util.IPropertySetter;
import org.talend.designer.scd.util.SWTResourceManager;
import org.talend.designer.scd.util.TableEditorManager;

/**
 * DOC hcw class global comment. Detailled comment
 */
public class Type2Section extends FieldSection {

    private static final Color ERROR_COLOR = SWTResourceManager.getColor(IColorConstants.RED);

    private static final int COMPLEMENT_INDEX = 4;

    private static final int CREATION_TYPE_INDEX = 3;

    private static final int CHECKBOX_COLUMN_INDEX = 0;

    private static final int NAME_COLUMN_INDEX = 2;

    private Table versionTable;

    private TableEditorManager editorManager;

    private DragDropManager dragDropManager;

    private Versioning versionData;

    private boolean supportCreationType = true;

    public Type2Section(Composite parent, ScdManager scdManager) {
        super(parent, scdManager, false, false);
        editorManager = new TableEditorManager();
        dragDropManager = new DragDropManager();
    }

    public Type2Section(Composite parent, ScdManager scdManager, int type) {
        super(parent, scdManager, false, false, type);
        editorManager = new TableEditorManager();
        dragDropManager = new DragDropManager();
    }

    @Override
    protected void createContents(Composite composite) {
        super.createContents(composite);
        Table table = getTableViewer().getTable();
        GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
        // gd.heightHint = this.height / 2 - 50;
        table.setLayoutData(gd);

        Label subTitle = new Label(composite, SWT.NONE); // SWT.BORDER
        subTitle.setAlignment(SWT.CENTER);
        subTitle.setFont(titleFont);
        subTitle.setText("Versioning"); //$NON-NLS-1$
        // subTitle.setBackground(SWTResourceManager.getColor(IColorConstants.
        // DARK_GREEN));
        // subTitle.pack();
        subTitle.setBackground(SWTResourceManager.getColor(255, 255, 0));
        GridDataFactory.swtDefaults().align(SWT.FILL, SWT.CENTER).applyTo(subTitle);
        createVersionTable(composite);

    }

    private void createVersionTable(Composite composite) {
        versionTable = new Table(composite, SWT.NONE);
        GridData gd = new GridData(SWT.FILL, SWT.TOP, true, false);
        // gd.heightHint = this.height / 2 + 50;
        versionTable.setLayoutData(gd);

        versionTable.setLinesVisible(true);
        versionTable.setHeaderVisible(true);
        // versionTable.setBackground(SWTResourceManager.getColor(IColorConstants
        // .LIGHT_GREEN));

        // create header
        TableColumn checkColumn = new TableColumn(versionTable, SWT.NONE);
        checkColumn.setWidth(20);

        TableColumn typeColumn = new TableColumn(versionTable, SWT.NONE);
        typeColumn.setWidth(60);
        typeColumn.setText("type"); //$NON-NLS-1$

        TableColumn nameColumn = new TableColumn(versionTable, SWT.NONE);
        nameColumn.setWidth(95);
        nameColumn.setText("name"); //$NON-NLS-1$

        TableColumn creationColumn = new TableColumn(versionTable, SWT.NONE);
        creationColumn.setWidth(120);
        creationColumn.setText("creation"); //$NON-NLS-1$

        TableColumn complementColumn = new TableColumn(versionTable, SWT.NONE);
        complementColumn.setWidth(95);
        complementColumn.setText("complement"); //$NON-NLS-1$
    }

    // for Perl, to disable some controls
    public void setSupportCreationType(boolean supportCreationType) {
        this.supportCreationType = supportCreationType;
    }

    public void setVersionInput(final Versioning ver) {
        versionData = ver;
        // start row
        final TableItem startItem = new TableItem(versionTable, SWT.NONE);
        // startItem.setBackground(SWTResourceManager.getColor(IColorConstants.
        // LIGHT_GREEN));
        startItem.setText(new String[] { "", Versioning.START_LABEL, //$NON-NLS-1$
                ver.getStartName(), "" }); //$NON-NLS-1$

        // combo editor
        // final List<String> outputColumns = scdManager.getOutputColumnNames();
        // final String[] outputColumnsArray = outputColumns.toArray(new
        // String[outputColumns.size()]);
        // int index = editorManager.getComboIndex(outputColumns,
        // ver.getStartName());
        //
        // editorManager.createComboEditor(versionTable, outputColumnsArray,
        // startItem, NAME_COLUMN_INDEX, index,
        // new IPropertySetter<Integer>() {
        //
        // public void set(Integer value) {
        // ver.setStartName(outputColumns.get(value));
        //
        // }
        // });

        // text editor
        editorManager.createTextEditor(versionTable, ver.getStartName(), startItem, NAME_COLUMN_INDEX,
                new IPropertySetter<String>() {

                    public void set(String value) {
                        ver.setStartName(value);
                    }
                });

        if (supportCreationType) {
            editorManager.createComboEditor(versionTable, VersionStartType.getAllTypeNames(), startItem, CREATION_TYPE_INDEX, ver
                    .getStartType().getIndex(), new IPropertySetter<Integer>() {

                public void set(Integer value) {
                    ver.setStartType(VersionStartType.getTypeByIndex(value));
                    onStartCreationChange(value, ver, startItem);
                }
            });
            if (ver.getStartType() == VersionStartType.INPUT_FIELD) {
                createStartComplement(ver, startItem, null);
            }
        }

        // end row
        final TableItem endItem = new TableItem(versionTable, SWT.NONE);
        // endItem.setBackground(SWTResourceManager.getColor(IColorConstants.
        // LIGHT_GREEN));
        endItem.setText(new String[] { "", Versioning.END_LABEL, //$NON-NLS-1$
                ver.getEndName(), "" }); //$NON-NLS-1$

        // combo editor
        // index = editorManager.getComboIndex(outputColumns, ver.getEndName());
        // editorManager.createComboEditor(versionTable, outputColumnsArray,
        // endItem, NAME_COLUMN_INDEX, index,
        // new IPropertySetter<Integer>() {
        //
        // public void set(Integer value) {
        // ver.setEndName(outputColumns.get(value));
        //
        // }
        // });

        // text editor
        editorManager.createTextEditor(versionTable, ver.getEndName(), endItem, NAME_COLUMN_INDEX, new IPropertySetter<String>() {

            public void set(String value) {
                ver.setEndName(value);
            }
        });

        if (supportCreationType) {
            editorManager.createComboEditor(versionTable, VersionEndType.getAllTypeNames(), endItem, CREATION_TYPE_INDEX, ver
                    .getEndType().getIndex(), new IPropertySetter<Integer>() {

                public void set(Integer value) {
                    ver.setEndType(VersionEndType.getTypeByIndex(value));
                    onEndCreationChange(value, ver, endItem);
                }
            });
            if (ver.getEndType() == VersionEndType.FIXED_YEAR) {
                createEndComplement(ver, endItem, null);
            }
        }

        // version row
        TableItem versionItem = new TableItem(versionTable, SWT.NONE);
        // versionItem.setBackground(SWTResourceManager.getColor(IColorConstants.
        // LIGHT_GREEN));
        versionItem.setText(new String[] { "", Versioning.VERSION_LABEL, //$NON-NLS-1$
                ver.getVersionName(), "" }); //$NON-NLS-1$
        editorManager.createCheckboxEditor(versionTable, ver.isVersionChecked(), versionItem, CHECKBOX_COLUMN_INDEX,
                new IPropertySetter<Boolean>() {

                    public void set(Boolean value) {
                        ver.setVersionChecked(value);
                    }
                });
        // combo editor
        // index = editorManager.getComboIndex(outputColumns,
        // ver.getVersionName());
        // editorManager.createComboEditor(versionTable, outputColumnsArray,
        // versionItem, NAME_COLUMN_INDEX, index,
        // new IPropertySetter<Integer>() {
        //
        // public void set(Integer value) {
        // ver.setVersionName(outputColumns.get(value));
        //
        // }
        // });

        // text editor
        editorManager.createTextEditor(versionTable, ver.getVersionName(), versionItem, NAME_COLUMN_INDEX,
                new IPropertySetter<String>() {

                    public void set(String value) {
                        ver.setVersionName(value);
                    }
                });

        // active row
        TableItem activeItem = new TableItem(versionTable, SWT.NONE);
        // activeItem.setBackground(SWTResourceManager.getColor(IColorConstants.
        // LIGHT_GREEN));
        activeItem.setText(new String[] { "", Versioning.ACTIVE_LABEL, //$NON-NLS-1$
                ver.getActiveName(), "" }); //$NON-NLS-1$
        editorManager.createCheckboxEditor(versionTable, ver.isActiveChecked(), activeItem, CHECKBOX_COLUMN_INDEX,
                new IPropertySetter<Boolean>() {

                    public void set(Boolean value) {
                        ver.setActiveChecked(value);
                    }
                });
        // combo editor
        // index = editorManager.getComboIndex(outputColumns,
        // ver.getActiveName());
        // editorManager.createComboEditor(versionTable, outputColumnsArray,
        // activeItem, NAME_COLUMN_INDEX, index,
        // new IPropertySetter<Integer>() {
        //
        // public void set(Integer value) {
        // ver.setActiveName(outputColumns.get(value));
        //
        // }
        // });

        // text editor
        editorManager.createTextEditor(versionTable, ver.getActiveName(), activeItem, NAME_COLUMN_INDEX,
                new IPropertySetter<String>() {

                    public void set(String value) {
                        ver.setActiveName(value);
                    }
                });
    }

    public Versioning getVersionData() {
        return versionData;
    }

    @Override
    public List<String> getUsedFields() {
        List<String> fields = super.getUsedFields();
        if (versionData != null && versionData.getStartType() == VersionStartType.INPUT_FIELD) {
            if (StringUtils.isNotEmpty(versionData.getStartComplement())) {
                fields.add(versionData.getStartComplement());
            }
        }
        return fields;
    }

    /**
     * DOC hcw Comment method "onEndCreationChange".
     *
     * @param value
     * @param versioning
     * @param endItem
     */
    protected void onEndCreationChange(Integer value, final Versioning versioning, TableItem item) {
        versioning.setEndComplement(""); //$NON-NLS-1$

        // dispose editor
        TableEditor editor = editorManager.getEditor(item, COMPLEMENT_INDEX);
        editorManager.disposeEditor(editor);

        if (value == VersionEndType.FIXED_YEAR.getIndex()) {
            createEndComplement(versioning, item, editor);
        }

    }

    /**
     * DOC hcw Comment method "createEndComplement".
     *
     * @param versioning
     * @param item
     * @param editor
     */
    private void createEndComplement(final Versioning versioning, TableItem item, TableEditor editor) {
        editor = editorManager.createTextEditor(editor, versionTable, versioning.getEndComplement(), item, COMPLEMENT_INDEX,
                new IPropertySetter<String>() {

                    public void set(String value) {
                        versioning.setEndComplement(value);
                    }
                });
        editor.layout();
    }

    /**
     * DOC hcw Comment method "onStartCreationChange".
     *
     * @param value
     * @param versioning
     * @param startItem
     */
    protected void onStartCreationChange(Integer value, final Versioning versioning, TableItem item) {
        versioning.setStartComplement(""); //$NON-NLS-1$
        scdManager.fireFieldChange();
        // dispose editor
        TableEditor editor = editorManager.getEditor(item, COMPLEMENT_INDEX);
        editorManager.disposeEditor(editor);

        if (value == VersionStartType.INPUT_FIELD.getIndex()) {
            createStartComplement(versioning, item, editor);
        }

    }

    /**
     * DOC hcw Comment method "createStartComplement".
     *
     * @param versioning
     * @param item
     * @param editor
     */
    private void createStartComplement(final Versioning versioning, final TableItem item, TableEditor editor) {
        // combo editor
        // final List<String> inputColumns = scdManager.getInputColumnNames();
        // inputColumns.add(0, "");
        // int index = editorManager.getComboIndex(inputColumns,
        // versioning.getStartComplement());
        //
        // editor = editorManager.createComboEditor(versionTable,
        // inputColumns.toArray(new String[inputColumns.size()]),
        // item,
        // COMPLEMENT_INDEX, index, new IPropertySetter<Integer>() {
        //
        // public void set(Integer value) {
        // versioning.setStartComplement(inputColumns.get(value));
        // Color color = value == 0 ? ERROR_COLOR : null;
        // editorManager.setComboColor(item, COMPLEMENT_INDEX, color);
        // scdManager.fireFieldChange();
        // }
        // });
        //
        // if (StringUtils.isEmpty(versioning.getStartComplement())) {
        // // display as error status, this field must not be null
        // editorManager.setComboColor(item, COMPLEMENT_INDEX, ERROR_COLOR);
        // }

        // label editor
        editor = editorManager.createLabelEditor(editor, versionTable, versioning.getStartComplement(), item, COMPLEMENT_INDEX);
        final Label text = (Label) editor.getEditor();
        if (StringUtils.isEmpty(versioning.getStartComplement())) {
            // display as error status, this field must not be null
            text.setBackground(ERROR_COLOR);
        }

        // add drag and drop support
        IDragDropDelegate delegate = createDragDropDelegate(versioning, text);
        dragDropManager.addDragSupport(text, delegate);
        dragDropManager.addDropSupport(text, delegate);
    }

    /**
     * DOC chuang Comment method "createDragDropDelegate".
     *
     * @param versioning
     * @param text
     * @return
     */
    private IDragDropDelegate createDragDropDelegate(final Versioning versioning, final Label text) {
        IDragDropDelegate delegate = new IDragDropDelegate() {

            public String getDragItemsAsText() {
                return "1|" + text.getText(); //$NON-NLS-1$
            }

            public void onDropItems(String data, Point position) {
                String[] items = data.split("\\|"); //$NON-NLS-1$
                if (items.length < 2) {
                    return;
                }
                text.setText(items[1]);
                versioning.setStartComplement(items[1]);
                text.setBackground(null);
            }

            public void removeDragItems(String data) {
                versioning.setStartComplement(""); //$NON-NLS-1$
                text.setText(""); //$NON-NLS-1$
                // display as error status, this field must not be null
                text.setBackground(ERROR_COLOR);
                // don't remove table item here, it can be removed by the delete
                // button
            }

            public boolean isDropAllowed(String data) {
                // only allow single selection
                return StringUtils.isEmpty(versioning.getStartComplement()) && data.startsWith("1|"); //$NON-NLS-1$
            }

        };
        return delegate;
    }
}
