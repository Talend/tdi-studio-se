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
package org.talend.designer.filemultischemas.managers;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ComboBoxCellEditor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TreeEditor;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;
import org.talend.core.model.metadata.MultiSchemasUtil;
import org.talend.designer.filemultischemas.data.EPropertyName;
import org.talend.designer.filemultischemas.data.ExternalMultiSchemasUIProperties;
import org.talend.designer.filemultischemas.data.MultiMetadataColumn;
import org.talend.designer.filemultischemas.data.SchemasKeyData;
import org.talend.designer.filemultischemas.ui.provider.SchemaDetailsProvider;
import org.talend.designer.filemultischemas.ui.provider.column.ColumnLineData;
import org.talend.designer.filemultischemas.ui.provider.column.SchemaDetailsColumnMouseAdapter;
import org.talend.designer.filemultischemas.ui.provider.column.SchemaDetailsColumnsProvider;
import org.talend.designer.filemultischemas.ui.provider.property.SchemaDetailsCheckBoxCellEditor;
import org.talend.designer.filemultischemas.ui.provider.property.SchemaDetailsPropertiesCellModifier;
import org.talend.designer.filemultischemas.ui.provider.property.SchemaDetailsPropertiesProvider;
import org.talend.designer.filemultischemas.ui.provider.property.SchemaDetailsTextCellEditor;
import org.talend.designer.filemultischemas.ui.provider.property.SchemaDetailsViewerSorter;
import org.talend.designer.filemultischemas.ui.provider.property.SchemaDetailsViewerSorterListener;
import org.talend.repository.model.RepositoryConstants;

/**
 * cLi class global comment. Detailled comment
 */
public class UIManager {

    private MultiSchemasManager multiSchemaManager;

    private TreeEditor columnTreeEditor;

    private MouseListener columnMouseListener;

    private int dialogResponse = SWT.NONE;

    public UIManager(MultiSchemasManager multiSchemaManager) {
        super();
        this.multiSchemaManager = multiSchemaManager;
    }

    public int getDialogResponse() {
        return this.dialogResponse;
    }

    public void setDialogResponse(int dialogResponse) {
        this.dialogResponse = dialogResponse;
    }

    // hywang add this method for feature 7373
    public int getSelectedColumnIndex() {
        return multiSchemaManager.getSelectedColumnIndex();
    }

    /**
     *
     * cLi Comment method "refreshSchemasDetailView".
     *
     * refresh schema details view by different provider.
     */
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

        SchemaDetailsProvider provider = null;
        if (model) { // is properties model
            provider = new SchemaDetailsPropertiesProvider(this);

            List<String> columnProperties = new ArrayList<String>();
            List<CellEditor> cellEidors = new ArrayList<CellEditor>();

            for (EPropertyName pName : EPropertyName.values()) {
                columnProperties.add(pName.name());
                TreeColumn pColumn = new TreeColumn(tree, SWT.NONE);
                pColumn.setWidth(100);
                pColumn.setText(pName.getName());

                final CellEditor cellEditor;
                switch (pName) {
                case NAME:
                case LENGTH:
                case TAGLEVEL:
                    // case CARD:
                case PATTERN:
                    cellEditor = new SchemaDetailsTextCellEditor(schemaDetailsViewer, pName);
                    break;
                case KEY:
                    pColumn.setToolTipText("Only one column can be set as key.");
                    cellEditor = new SchemaDetailsCheckBoxCellEditor(tree);
                    break;
                // case NULL:
                // break;
                case TYPE:
                    cellEditor = new ComboBoxCellEditor(tree, MultiSchemasUtil.getTalendTypeLabel(), SWT.READ_ONLY);
                    break;
                default:
                    cellEditor = null;
                }
                if (cellEditor != null) {
                    cellEidors.add(cellEditor);
                }
            }
            schemaDetailsViewer.setColumnProperties(columnProperties.toArray(new String[0]));
            schemaDetailsViewer.setCellEditors(cellEidors.toArray(new CellEditor[0]));
            schemaDetailsViewer.setCellModifier(new SchemaDetailsPropertiesCellModifier(schemaDetailsViewer, this));

            // set sorter
            TreeColumn sorterColumn = tree.getColumn(0);
            sorterColumn.addListener(SWT.Selection, new SchemaDetailsViewerSorterListener(schemaDetailsViewer));
            tree.setSortColumn(sorterColumn);
            tree.setSortDirection(SWT.UP);
            schemaDetailsViewer.setSorter(new SchemaDetailsViewerSorter(schemaDetailsViewer, sorterColumn, tree
                    .getSortDirection() == SWT.UP));

        } else { // is column model

            provider = new SchemaDetailsColumnsProvider(this);
            // first columm is fixed.
            TreeColumn propertyColumn = new TreeColumn(tree, SWT.NONE);
            propertyColumn.setWidth(80);
            propertyColumn.setResizable(false);

            //
            columnTreeEditor = new TreeEditor(tree);
            columnTreeEditor.horizontalAlignment = SWT.LEFT;
            columnTreeEditor.grabHorizontal = true;

            columnMouseListener = new SchemaDetailsColumnMouseAdapter(schemaDetailsViewer, columnTreeEditor, this);
            tree.addMouseListener(columnMouseListener);

        }
        schemaDetailsViewer.setContentProvider(provider);
        schemaDetailsViewer.setLabelProvider(provider);

        //
    }

    public void refreshSchemasDetailView(TreeViewer schemaTreeViewer, TreeViewer schemaDetailsViewer, boolean model) {
        if (schemaTreeViewer == null || schemaTreeViewer.getTree().isDisposed() || schemaDetailsViewer == null
                || schemaDetailsViewer.getTree().isDisposed()) {
            return;
        }
        IStructuredSelection selection = (IStructuredSelection) schemaTreeViewer.getSelection();
        Object element = selection.getFirstElement();
        if (element == null || !(element instanceof SchemasKeyData)) {
            return;
        }
        final Tree tree = schemaDetailsViewer.getTree();
        tree.setRedraw(false);

        SchemasKeyData schemasData = (SchemasKeyData) element;

        final List<MultiMetadataColumn> metadataColumns = schemasData.getMetadataColumnsInModel();

        if (model) {
            schemaDetailsViewer.setInput(metadataColumns);
            //
            tree.setRedraw(true);
            // type
            final TreeColumn typeColumn = tree.getColumns()[EPropertyName.indexOf(EPropertyName.TYPE)];
            typeColumn.pack();
        } else {

            final int colSize = metadataColumns.size();

            if (colSize > 0) {
                //
                List<String> columnProperties = new ArrayList<String>();
                // first column
                columnProperties.add(new Integer(-1).toString());

                final int columnNum = tree.getColumnCount() - 1;
                for (int i = 0; i < colSize; i++) {
                    TreeColumn dataColumn = null;
                    if (i < columnNum) {// existed column, only update data.
                        dataColumn = tree.getColumns()[i + 1];
                    } else { // create new column
                        dataColumn = new TreeColumn(tree, SWT.NONE);
                        dataColumn.setWidth(100);
                    }
                    // MultiSchemasMetadataColumn metadataColumn = metadataColumns.get(i);
                    // dataColumn.setText(metadataColumn.getLabel());
                    // dataColumn.setData(metadataColumn);

                    columnProperties.add(new Integer(i).toString());
                }

                schemaDetailsViewer.setColumnProperties(columnProperties.toArray(new String[0]));

                schemaDetailsViewer.setInput(schemasData);
            } else {
                schemaDetailsViewer.setInput(null);
            }
            //
            tree.setRedraw(true);
            updateColumns(tree, colSize);
        }
    }

    /**
     *
     * cLi Comment method "updateColumns".
     *
     * resize columns or dispose the unused columns.
     */
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

    public static boolean existedKeyColumn(List<MultiMetadataColumn> columns, MultiMetadataColumn currentColumn) {
        if (columns != null) {
            for (MultiMetadataColumn col : columns) {
                if (currentColumn == col) {
                    continue;
                }
                if (col.isKey()) {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean checkSchemaDetailsValue(TreeViewer schemaDetailsViewer, MultiMetadataColumn multiMetadataColumn,
            EPropertyName property, Object input) {
        return validSchemaDetailsColumns(schemaDetailsViewer, multiMetadataColumn, property, input) == null;
    }

    public static String validSchemaDetailsColumns(final TreeViewer schemaDetailsViewer,
            final MultiMetadataColumn multiMetadataColumn, final EPropertyName property, final Object input) {
        String mess = null;
        if (input != null && input instanceof String) {
            final String value = (String) input;
            final String existedMessage = "The column name have existed.";

            if (isFirstForColumnModel(property)) { // for column model
                IStructuredSelection selection = (IStructuredSelection) schemaDetailsViewer.getSelection();
                if (selection != null) {
                    final Object firstElement = selection.getFirstElement();
                    if (firstElement != null && firstElement instanceof ColumnLineData) {
                        ColumnLineData lineData = (ColumnLineData) firstElement;
                        for (MultiMetadataColumn column : lineData.getKeyData().getMetadataColumnsInModel()) {
                            if (multiMetadataColumn != column && value.equals(column.getLabel())) {
                                mess = existedMessage;
                                break;
                            }
                        }

                    }
                }
            } else {
                final String invalidInput = "Invalid input.";
                switch (property) {
                case NAME: // for property model.
                    IStructuredSelection selection = (IStructuredSelection) schemaDetailsViewer.getSelection();
                    if (selection != null) {
                        final Object firstElement = selection.getFirstElement();
                        if (firstElement != null && firstElement instanceof MultiMetadataColumn) {
                            MultiMetadataColumn column = (MultiMetadataColumn) firstElement;

                            for (TreeItem item : schemaDetailsViewer.getTree().getItems()) {
                                final Object data = item.getData();
                                if (data != column && data instanceof MultiMetadataColumn) {
                                    MultiMetadataColumn other = (MultiMetadataColumn) data;
                                    if (value.equals(other.getLabel())) {
                                        mess = existedMessage;
                                        break;
                                    }
                                }
                            }

                        }
                    }
                    if (mess == null && !value.matches(RepositoryConstants.CONTEXT_AND_VARIABLE_PATTERN)) {
                        mess = invalidInput;
                    }
                    break;
                case LENGTH:
                    if (!value.matches(ExternalMultiSchemasUIProperties.NUMBER_PATTERN)) {
                        mess = invalidInput;
                    }
                    break;
                }
            }
        }
        return mess;
    }

    /**
     *
     * cLi Comment method "isFirstForColumnModel".
     *
     * for column model.
     */
    public static boolean isFirstForColumnModel(EPropertyName property) {
        return property == null;
    }

    public boolean enableMovedRecord(TreeViewer schemaTreeViewer, boolean left) {
        if (schemaTreeViewer == null) {
            return false;
        }
        ISelection selection = schemaTreeViewer.getSelection();
        if (selection instanceof IStructuredSelection) {
            IStructuredSelection structuredSelection = (IStructuredSelection) selection;
            if (structuredSelection.size() == 1) {
                Object element = structuredSelection.getFirstElement();
                if (element instanceof SchemasKeyData) {
                    SchemasKeyData data = (SchemasKeyData) element;
                    SchemasKeyData parent = data.getParent();
                    int index = parent.getChildren().indexOf(data);
                    if (left) {
                        SchemasKeyData grandfather = parent.getParent();
                        if (grandfather == null) { // in root.
                            return false;
                        }
                        if (index != parent.getChildren().size() - 1) { // not the end of children.
                            return false;
                        }
                    } else {
                        if (index == 0) { // in first of children.
                            return false;
                        }
                    }
                    return true;
                }
            }
        }
        return false;
    }

    public void moveRecord(TreeViewer schemaTreeViewer, boolean left) {
        if (schemaTreeViewer == null) {
            return;
        }
        ISelection selection = schemaTreeViewer.getSelection();
        if (selection instanceof IStructuredSelection) {
            Object element = ((IStructuredSelection) selection).getFirstElement();
            if (element instanceof SchemasKeyData) {
                SchemasKeyData data = (SchemasKeyData) element;
                SchemasKeyData parent = data.getParent();
                if (left) {
                    SchemasKeyData grandfather = parent.getParent();
                    if (grandfather != null) { // not in root.
                        List<SchemasKeyData> children = grandfather.getChildren();
                        int index = -1;
                        for (int i = 0; i < children.size(); i++) {
                            if (children.get(i) == parent) {
                                index = i;
                                break;
                            }
                        }
                        if (index > -1) {
                            int index2 = index + 1;
                            if (index2 > children.size()) {
                                grandfather.addChild(data);
                            } else {
                                grandfather.addChild(index2, data);
                            }
                        }
                    }
                } else {
                    SchemasKeyData sibling = null;
                    for (SchemasKeyData skd : parent.getChildren()) {
                        if (skd == data) {
                            break;
                        } else {
                            sibling = skd;
                        }
                    }
                    if (sibling != null) {
                        sibling.addChild(data);
                    }
                }
                schemaTreeViewer.refresh();
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
