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
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ComboBoxCellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.talend.commons.ui.runtime.swt.tableviewer.behavior.CellEditorValueAdapter;
import org.talend.commons.ui.runtime.swt.tableviewer.behavior.ColumnCellModifier;
import org.talend.commons.ui.swt.advanced.dataeditor.AbstractDataTableEditorView;
import org.talend.commons.ui.swt.advanced.dataeditor.ExtendedToolbarView;
import org.talend.commons.ui.swt.tableviewer.TableViewerCreator;
import org.talend.commons.ui.swt.tableviewer.TableViewerCreatorColumn;
import org.talend.commons.ui.swt.tableviewer.celleditor.EditableComboBoxCellEditor;
import org.talend.commons.utils.data.bean.IBeanPropertyAccessors;
import org.talend.core.model.metadata.IMetadataColumn;
import org.talend.core.model.process.IConnection;
import org.talend.core.model.process.IElement;
import org.talend.core.model.process.IElementParameter;
import org.talend.core.ui.proposal.TalendProposalProvider;
import org.talend.designer.core.ui.editor.nodes.Node;

/**
 * DOC Talend class global comment. Detailled comment
 */
public class PropertiesTableByRowEditorView<B> extends AbstractDataTableEditorView<B> {

    public PropertiesTableByRowEditorView(Composite parent, int style, PropertiesTableEditorModel model) {
        super(parent, style, model);
    }

    /**
     * DOC amaumont MetadataTableEditorView constructor comment.
     *
     * @param parentComposite
     * @param mainCompositeStyle
     * @param readOnly
     * @param toolbarVisible
     * @param labelVisible TODO
     * @param extendedTableModel
     */
    public PropertiesTableByRowEditorView(Composite parentComposite, int mainCompositeStyle, PropertiesTableEditorModel model,
            boolean toolbarVisible, boolean labelVisible) {
        super(parentComposite, mainCompositeStyle, model, model.getElemParameter().isReadOnly(), toolbarVisible, labelVisible);
    }

    /**
     * DOC amaumont MetadataTableEditorView constructor comment.
     *
     * @param parentComposite
     * @param mainCompositeStyle
     */
    public PropertiesTableByRowEditorView(Composite parentComposite, int mainCompositeStyle) {
        super(parentComposite, mainCompositeStyle);
    }

    @Override
    protected ExtendedToolbarView initToolBar() {
        return new PropertiesTableToolbarEditorView(getMainComposite(), SWT.NONE, this.getExtendedTableViewer(),
                (PropertiesTableEditorModel) getExtendedTableModel());
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.talend.commons.ui.swt.advanced.dataeditor.AbstractDataTableEditorView#createColumns(org.talend.commons.ui
     * .swt.tableviewer.TableViewerCreator, org.eclipse.swt.widgets.Table)
     */
    @Override
    protected void createColumns(TableViewerCreator<B> tableViewerCreator, Table table) {
        // there's two lists of values, one that will be in the table
        // and the other will be stored as the current value in the property
        // there is two lists because of the undo / redo capabilities
        PropertiesTableEditorModel model = getModel();

        TalendProposalProvider processProposalProvider = new TalendProposalProvider(model.getProcess());

        String[] titles = null;
        // final Element elem = model.getElement();
        final IElementParameter param = model.getElemParameter();
        final IElement element = model.getElement();
        if (element instanceof Node) {
            List<IConnection> listConnection = (List<IConnection>) ((Node) element).getInputs();
            List<String> names = new ArrayList<String>();
            for (IConnection con : listConnection) {
                names.add(con.getName());
            }
            titles = new String[names.size()];
            for (int i = 0; i < names.size(); i++) {
                titles[i] = names.get(i);
            }
        }
        if (titles != null && titles.length > 0) {
            for (int i = 0; i < titles.length; i++) {
                final int curCol = i;
                boolean toDisplay = true;
                if (toDisplay) {
                    final TableViewerCreatorColumn column = new TableViewerCreatorColumn(tableViewerCreator);
                    column.setTitle(titles[i]);
                    column.setModifiable(true);
                    column.setMinimumWidth(100);
                    column.setWeight(20);

                    String[] stringToDisplay = null;
                    if (element instanceof Node) {
                        List<IConnection> listConnection = (List<IConnection>) ((Node) element).getInputs();
                        for (IConnection con : listConnection) {
                            if (con.getName().equals(titles[i])) {
                                List<IMetadataColumn> columns = con.getMetadataTable().getListColumns();
                                stringToDisplay = new String[columns.size()];
                                for (int j = 0; j < columns.size(); j++) {
                                    stringToDisplay[j] = columns.get(j).getLabel();
                                }
                            }
                        }
                    }
                    final EditableComboBoxCellEditor cellEditor = new EditableComboBoxCellEditor(table, stringToDisplay);

                    // column.setLabelProvider(new IColumnLabelProvider() {
                    //
                    // @Override
                    // public String getLabel(Object bean) {
                    // if (bean instanceof Map) {
                    // Map valueMap = (Map) bean;
                    // String columnName = column.getTitle();
                    // if (valueMap.containsKey(columnName)) {
                    // String value = (String) valueMap.get(columnName);
                    // return value;
                    // }
                    // }
                    //                            return ""; //$NON-NLS-1$
                    // }
                    // });
                    // column.setCellEditor(cellEditor);
                    column.setCellEditor(cellEditor, new CellEditorValueAdapter() {

                        @Override
                        public String getColumnText(CellEditor cellEditor, Object bean, Object cellEditorValue) {
                            Map valueMap = (Map) bean;
                            String columnName = column.getTitle();
                            if (valueMap.containsKey(columnName)) {
                                String value = (String) valueMap.get(columnName);
                                return value;
                            }
                            return "";
                        }

                        @Override
                        public Object getOriginalTypedValue(CellEditor cellEditor, Object cellEditorTypedValue) {
                            Object returnedValue = null;
                            CCombo combo = (CCombo) cellEditor.getControl();
                            int rowNumber = ((Table) combo.getParent()).getSelectionIndex();
                            String[] listToDisplay = getItem(element, column.getTitle());
                            if (!Arrays.equals(listToDisplay, ((ComboBoxCellEditor) cellEditor).getItems())) {
                                ((ComboBoxCellEditor) cellEditor).setItems(listToDisplay);
                            }
                            if (cellEditorTypedValue != null && cellEditorTypedValue instanceof String) {
                                return cellEditorTypedValue;
                            }
                            return "";
                        };

                        @Override
                        public Object getCellEditorTypedValue(CellEditor cellEditor, Object originalTypedValue) {
                            CCombo combo = (CCombo) cellEditor.getControl();
                            int rowNumber = ((Table) combo.getParent()).getSelectionIndex();
                            String[] listToDisplay = getItem(element, column.getTitle());
                            if (!Arrays.equals(listToDisplay, ((ComboBoxCellEditor) cellEditor).getItems())) {
                                ((ComboBoxCellEditor) cellEditor).setItems(listToDisplay);
                            }
                            Object returnedValue = 0;
                            if (originalTypedValue != null) {
                                String[] namesSet = listToDisplay;
                                for (int j = 0; j < namesSet.length; j++) {
                                    if (namesSet[j].equals(originalTypedValue)) {
                                        returnedValue = j;
                                        break;
                                    }
                                }
                            }
                            return returnedValue;
                        };

                        private String[] getItem(IElement element, String columnName) {
                            List<String> stringToDisplay = new ArrayList<String>();
                            if (element instanceof Node) {
                                List<IConnection> listConnection = (List<IConnection>) ((Node) element).getInputs();
                                for (IConnection con : listConnection) {
                                    if (con.getName().equals(columnName) && con.getMetadataTable() != null) {
                                        List<IMetadataColumn> columns = con.getMetadataTable().getListColumns();
                                        for (IMetadataColumn column : columns) {
                                            stringToDisplay.add(column.getLabel());
                                        }
                                    }
                                }
                            }
                            String[] listToDisplay = stringToDisplay.toArray(new String[0]);
                            return listToDisplay;
                        }
                    });

                    column.setColumnCellModifier(new ColumnCellModifier(column) {

                        @Override
                        public boolean canModify(Object bean) {
                            boolean canModify = super.canModify(bean);
                            return canModify;
                        }

                        @Override
                        public Object getValue(Object bean) {
                            Map valueMap = (Map) bean;
                            String columnName = column.getTitle();
                            if (valueMap.containsKey(columnName)) {
                                String value = (String) valueMap.get(columnName);
                                return value;
                            }
                            return "";
                        }

                    });

                    column.setBeanPropertyAccessors(new IBeanPropertyAccessors<B, Object>() {

                        @Override
                        public Object get(B bean) {
                            Map valueMap = (Map) bean;
                            String columnName = column.getTitle();
                            if (valueMap.containsKey(columnName)) {
                                return valueMap.get(columnName);
                            }
                            return "";
                        }

                        @Override
                        public void set(B bean, Object value) {
                            if (value == null) {
                                return;
                            }
                            Map valueMap = (Map) bean;
                            String columnName = column.getTitle();
                            if (valueMap.containsKey(columnName)) {
                                valueMap.remove(columnName);
                                valueMap.put(columnName, value);
                            } else {
                                valueMap.put(columnName, value);
                            }
                        }
                    });
                }
            }
        }
    }

    public PropertiesTableToolbarEditorView getToolBar() {
        return (PropertiesTableToolbarEditorView) getExtendedToolbar();
    }

    public PropertiesTableEditorModel getModel() {
        return (PropertiesTableEditorModel) getExtendedTableModel();
    }
}
