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
package org.talend.designer.filemultischemas.ui.provider.column;

import java.util.List;

import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.custom.TreeEditor;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.talend.core.model.metadata.MultiSchemasUtil;
import org.talend.designer.filemultischemas.data.EPropertyName;
import org.talend.designer.filemultischemas.data.MultiMetadataColumn;
import org.talend.designer.filemultischemas.managers.UIManager;

/**
 * cLi class global comment. Detailled comment
 */
public class SchemaDetailsColumnMouseAdapter extends MouseAdapter {

    private TreeEditor treeEditor;

    private TreeViewer schemaDetailsViewer;

    private final Color redColor;

    private Point currentPoint;

    // hywang add for feature 7373
    private UIManager uiManager;

    public SchemaDetailsColumnMouseAdapter(TreeViewer schemaDetailsViewer, TreeEditor treeEditor, UIManager uiManager) {
        super();
        this.schemaDetailsViewer = schemaDetailsViewer;
        this.treeEditor = treeEditor;
        this.redColor = schemaDetailsViewer.getTree().getDisplay().getSystemColor(SWT.COLOR_RED);
        this.uiManager = uiManager;
    }

    protected Control getEditor() {
        return treeEditor.getEditor();
    }

    protected TreeEditor getTreeEditor() {
        return this.treeEditor;
    }

    protected TreeViewer getSchemaDetailsViewer() {
        return this.schemaDetailsViewer;
    }

    protected Tree getTree() {
        return this.schemaDetailsViewer.getTree();
    }

    protected ViewerCell getViewerCell() {
        return getSchemaDetailsViewer().getCell(currentPoint);
    }

    protected int getColumnIndex() {
        return getViewerCell().getColumnIndex();
    }

    @Override
    public void mouseDown(MouseEvent e) {
        currentPoint = new Point(e.x, e.y);
        // Clean up any previous editor control
        Control oldEditor = getEditor();
        if (oldEditor != null) {
            oldEditor.dispose();
        }
        if (getViewerCell() == null) {
            return;
        }
        // Identify the selected row
        TreeItem item = (TreeItem) getViewerCell().getViewerRow().getItem();
        if (item == null) {
            return;
        }
        if (getColumnIndex() < 1) { // ingore first
            return;
        }
        final Object data = item.getData();
        if (!(data instanceof ColumnLineData)) {
            return;
        }
        ColumnLineData lineData = (ColumnLineData) data;
        List<MultiMetadataColumn> metadataColumnsInModel = lineData.getKeyData().getMetadataColumnsInModel();
        final MultiMetadataColumn multiMetadataColumn = metadataColumnsInModel.get(getColumnIndex() - 1);
        Control newEditor = null;
        final EPropertyName property = lineData.getProperty();
        if (UIManager.isFirstForColumnModel(property)) {
            newEditor = createTextControl(item, property, multiMetadataColumn);
        } else {
            switch (property) {
            case NAME:
            case TAGLEVEL:
                break;
            case TYPE:
                newEditor = createComboControl(item, multiMetadataColumn);
                break;
            case LENGTH:
                // case CARD:
            case PATTERN:
                newEditor = createTextControl(item, property, multiMetadataColumn);
                break;
            case KEY:
                /*
                 * ingore record type(first column);
                 *
                 * if existed key column, should not edit other key.
                 */
                if (getColumnIndex() == uiManager.getSelectedColumnIndex() + 1
                        || UIManager.existedKeyColumn(metadataColumnsInModel, multiMetadataColumn)) {
                    break;
                }
                newEditor = createCheckBoxControl(item, property, multiMetadataColumn);
                break;
            }
        }
        if (newEditor != null) {
            newEditor.addFocusListener(new FocusAdapter() {

                public void focusLost(FocusEvent e) {
                    if (getEditor() != null && !getEditor().isDisposed()) {
                        getEditor().dispose();
                    }
                }
            });
            newEditor.setFocus();
            getTreeEditor().setEditor(newEditor, item, getColumnIndex());
        }
    }

    private Control createComboControl(final TreeItem item, final MultiMetadataColumn multiMetadataColumn) {
        final CCombo newEditor = new CCombo(getTree(), SWT.READ_ONLY);

        newEditor.setItems(MultiSchemasUtil.getTalendTypeLabel());
        final int index = MultiSchemasUtil.getTalendTypeIndex(multiMetadataColumn.getTalendType());
        newEditor.select(index);

        newEditor.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                CCombo combo = (CCombo) getEditor();
                final String talendType = MultiSchemasUtil.getTalendTypeByIndex(combo.getSelectionIndex());
                if (talendType != null) {
                    multiMetadataColumn.setTalendType(talendType);
                    getSchemaDetailsViewer().update(item.getData(), null);
                }
            }

        });

        return newEditor;
    }

    private Control createTextControl(final TreeItem item, final EPropertyName pName,
            final MultiMetadataColumn multiMetadataColumn) {
        final Text newEditor = new Text(getTree(), SWT.NONE);
        if (UIManager.isFirstForColumnModel(pName)) {
            newEditor.setText(MultiSchemasUtil.validateValue(multiMetadataColumn.getLabel()));
        } else {
            switch (pName) {
            case LENGTH:
                newEditor.setText(MultiSchemasUtil.getAndCheckIntgerValue(multiMetadataColumn.getLength()));
                break;
            // case CARD:
            // newEditor.setText(MultiSchemasUtil.validateValue(multiMetadataColumn.getCard()));
            // break;
            case PATTERN:
                newEditor.setText(MultiSchemasUtil.validateValue(multiMetadataColumn.getPattern()));
                break;
            }
        }
        newEditor.addModifyListener(new ModifyListener() {

            public void modifyText(ModifyEvent e) {
                Text text = (Text) getEditor();

                final String value = text.getText();
                if (UIManager.isFirstForColumnModel(pName)) {
                    if (value != null && !"".equals(value.trim()) //$NON-NLS-1$
                            && UIManager.checkSchemaDetailsValue(getSchemaDetailsViewer(), multiMetadataColumn, pName, value)) {
                        multiMetadataColumn.setLabel(value.trim());
                        text.setBackground(null);
                    } else {
                        text.setBackground(redColor);
                        return;
                    }
                } else {
                    switch (pName) {
                    case LENGTH:
                        if (!"".equals(value)) { //$NON-NLS-1$
                            if (UIManager.checkSchemaDetailsValue(getSchemaDetailsViewer(), multiMetadataColumn, pName, value)) {
                                multiMetadataColumn.setLength(Integer.parseInt((String) value));
                                text.setBackground(null);
                            } else {// else, don't change it.
                                text.setBackground(redColor);
                                return;
                            }
                        } else {
                            multiMetadataColumn.setLength(null);
                        }
                        break;
                    // case CARD:
                    // multiMetadataColumn.setCard(value);
                    // break;
                    case PATTERN:
                        multiMetadataColumn.setPattern(value);
                        break;
                    }
                }
                getSchemaDetailsViewer().update(item.getData(), null);
            }
        });
        newEditor.selectAll();
        return newEditor;
    }

    private Control createCheckBoxControl(final TreeItem item, final EPropertyName pName,
            final MultiMetadataColumn multiMetadataColumn) {
        final Button newEditor = new Button(getTree(), SWT.CHECK);
        newEditor.setSelection(multiMetadataColumn.isKey());

        newEditor.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                multiMetadataColumn.setKey(newEditor.getSelection());
                getSchemaDetailsViewer().update(item.getData(), null);
            }

        });
        return newEditor;
    }
}
