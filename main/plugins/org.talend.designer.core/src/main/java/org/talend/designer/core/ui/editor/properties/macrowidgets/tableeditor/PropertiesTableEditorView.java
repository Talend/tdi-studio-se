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

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColorCellEditor;
import org.eclipse.jface.viewers.ComboBoxCellEditor;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.talend.commons.ui.runtime.image.EImage;
import org.talend.commons.ui.runtime.image.ImageProvider;
import org.talend.commons.ui.runtime.swt.tableviewer.TableViewerCreatorNotModifiable.LAYOUT_MODE;
import org.talend.commons.ui.runtime.swt.tableviewer.behavior.CellEditorValueAdapter;
import org.talend.commons.ui.runtime.swt.tableviewer.behavior.ColumnCellModifier;
import org.talend.commons.ui.runtime.swt.tableviewer.behavior.IColumnColorProvider;
import org.talend.commons.ui.runtime.swt.tableviewer.behavior.IColumnLabelProvider;
import org.talend.commons.ui.swt.advanced.dataeditor.AbstractDataTableEditorView;
import org.talend.commons.ui.swt.advanced.dataeditor.ExtendedToolbarView;
import org.talend.commons.ui.swt.proposal.TextCellEditorWithProposal;
import org.talend.commons.ui.swt.tableviewer.TableViewerCreator;
import org.talend.commons.ui.swt.tableviewer.TableViewerCreatorColumn;
import org.talend.commons.ui.swt.tableviewer.celleditor.EditableComboBoxCellEditor;
import org.talend.commons.ui.swt.tableviewer.tableeditor.CheckboxTableEditorContent;
import org.talend.commons.ui.utils.image.ColorUtils;
import org.talend.commons.utils.data.bean.IBeanPropertyAccessors;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.PluginChecker;
import org.talend.core.model.metadata.IEbcdicConstant;
import org.talend.core.model.metadata.IMetadataColumn;
import org.talend.core.model.metadata.IMetadataTable;
import org.talend.core.model.metadata.IRuleConstant;
import org.talend.core.model.metadata.ISAPConstant;
import org.talend.core.model.metadata.MetadataToolHelper;
import org.talend.core.model.process.EParameterFieldType;
import org.talend.core.model.process.IConnection;
import org.talend.core.model.process.IElement;
import org.talend.core.model.process.IElementParameter;
import org.talend.core.model.process.INode;
import org.talend.core.model.process.IProcess;
import org.talend.core.model.process.IProcess2;
import org.talend.core.model.properties.EbcdicConnectionItem;
import org.talend.core.runtime.maven.MavenArtifact;
import org.talend.core.runtime.maven.MavenUrlHelper;
import org.talend.core.service.IEBCDICProviderService;
import org.talend.core.service.ISAPProviderService;
import org.talend.core.ui.metadata.celleditor.ModuleListCellEditor;
import org.talend.core.ui.metadata.celleditor.RuleCellEditor;
import org.talend.core.ui.metadata.celleditor.SchemaCellEditor;
import org.talend.core.ui.metadata.celleditor.SchemaXPathQuerysCellEditor;
import org.talend.core.ui.metadata.editor.AbstractMetadataTableEditorView;
import org.talend.core.ui.proposal.TalendProposalProvider;
import org.talend.core.utils.TalendQuoteUtils;
import org.talend.designer.core.model.FakeElement;
import org.talend.designer.core.model.components.EParameterName;
import org.talend.designer.core.model.components.ElementParameter;
import org.talend.designer.core.ui.celleditor.PatternCellEditor;
import org.talend.designer.core.ui.celleditor.PatternPropertyCellEditor;
import org.talend.designer.core.ui.editor.nodes.Node;
import org.talend.designer.core.ui.editor.properties.controllers.NumberLimitTextController;
import org.talend.designer.core.ui.event.CheckColumnSelectionListener;

/**
 * MetadataTableEditorView2 must be used.
 *
 * $Id: MetadataTableEditorView.java 801 2006-11-30 16:28:36Z amaumont $
 *
 * @param <B>
 */
public class PropertiesTableEditorView<B> extends AbstractDataTableEditorView<B> {

    private final String SINGLE = "SINGLE";

    private final String STRUCTURE = "STRUCTURE";

    /**
     * DOC amaumont MetadataTableEditorView constructor comment.
     *
     * @param parent
     * @param style
     * @param model
     */
    public PropertiesTableEditorView(Composite parent, int style, PropertiesTableEditorModel model) {
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
    public PropertiesTableEditorView(Composite parentComposite, int mainCompositeStyle, PropertiesTableEditorModel model,
            boolean toolbarVisible, boolean labelVisible) {
        super(parentComposite, mainCompositeStyle, model, model.getElemParameter().isReadOnly(), toolbarVisible, labelVisible);
    }

    /**
     * DOC amaumont MetadataTableEditorView constructor comment.
     *
     * @param parentComposite
     * @param mainCompositeStyle
     */
    public PropertiesTableEditorView(Composite parentComposite, int mainCompositeStyle) {
        super(parentComposite, mainCompositeStyle);
    }

    public void setMetadataTableEditor(PropertiesTableEditorModel model) {
        setExtendedTableModel(model);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.commons.ui.swt.advanced.dataeditor.AbstractDataTableEditorView#initToolBar()
     */
    @Override
    protected ExtendedToolbarView initToolBar() {
        return new PropertiesTableToolbarEditorView(getMainComposite(), SWT.NONE, this.getExtendedTableViewer(),
                (PropertiesTableEditorModel) getExtendedTableModel());
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.talend.commons.ui.swt.advanced.dataeditor.AbstractDataTableEditorView#setTableViewerCreatorOptions(org.talend
     * .commons.ui.swt.tableviewer.TableViewerCreator)
     */
    @Override
    protected void setTableViewerCreatorOptions(TableViewerCreator<B> newTableViewerCreator) {
        super.setTableViewerCreatorOptions(newTableViewerCreator);
        newTableViewerCreator.setLayoutMode(LAYOUT_MODE.DEFAULT);
        newTableViewerCreator.setLazyLoad(TableViewerCreator.getRecommandLazyLoad());
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.talend.commons.ui.swt.extended.macrotable.AbstractExtendedTableViewer#createColumns(org.talend.commons.ui
     * .swt.tableviewer.TableViewerCreator, org.eclipse.swt.widgets.Table)
     */
    @SuppressWarnings("unchecked")
    @Override
    protected void createColumns(final TableViewerCreator<B> tableViewerCreator, final Table table) {

        // there's two lists of values, one that will be in the table
        // and the other will be stored as the current value in the property
        // there is two lists because of the undo / redo capabilities
        PropertiesTableEditorModel model = getModel();
        TalendProposalProvider processProposalProvider = new TalendProposalProvider(model.getProcess());
        String[] titles = model.getTitles();
        final Object[] itemsValue = model.getItemsValue();
        final String[] items = model.getItems();
        // final Element elem = model.getElement();
        final IElementParameter param = model.getElemParameter();
        final IElement element = model.getElement();
        for (int i = 0; i < titles.length; i++) {
            final int curCol = i;
            final IElementParameter currentParam = (IElementParameter) itemsValue[i];
            // if all is empty, show it always.
            boolean toDisplay = currentParam.isShow(currentParam.getShowIf(), currentParam.getNotShowIf(),
                    element.getElementParameters());
            if (!toDisplay) {
                List<Map<String, Object>> fullTable = (List<Map<String, Object>>) param.getValue();
                for (int curLine = 0; curLine < fullTable.size(); curLine++) {
                    ((ElementParameter) currentParam).setCurrentRow(curLine);
                    if (currentParam.isShow(element.getElementParameters())) {
                        toDisplay = true;
                        break;
                    }
                }
            }

            if (toDisplay) {
                final TableViewerCreatorColumn column = new TableViewerCreatorColumn(tableViewerCreator);
                column.setTitle(titles[i]);
                column.setModifiable(true);
                column.setMinimumWidth(100);
                column.setWeight(20);

                switch (currentParam.getFieldType()) {
                case CONTEXT_PARAM_NAME_LIST:
                case CLOSED_LIST:
                case LOOKUP_COLUMN_LIST:
                case COLUMN_LIST:
                case CONNECTION_LIST:
                case DBTYPE_LIST:
                case COMPONENT_LIST:
                case PREV_COLUMN_LIST:
                    final ComboBoxCellEditor cellEditor = new ComboBoxCellEditor(table, currentParam.getListItemsDisplayName());
                    final IElementParameter copyOfTmpParam = currentParam;
                    ((CCombo) cellEditor.getControl()).setEditable(false);
                    ((CCombo) cellEditor.getControl())
                            .setEnabled(!(param.isRepositoryValueUsed() || param.isReadOnly() || currentParam.isReadOnly()));
                    column.setCellEditor(cellEditor, new CellEditorValueAdapter() {

                        @Override
                        public String getColumnText(CellEditor cellEditor, Object bean, Object cellEditorValue) {
                            return (String) cellEditorValue;
                        }

                        @Override
                        public Object getOriginalTypedValue(CellEditor cellEditor, Object cellEditorTypedValue) {
                            Object returnedValue = null;
                            if (cellEditorTypedValue != null && cellEditorTypedValue instanceof Integer) {
                                int index = (Integer) cellEditorTypedValue;
                                String[] namesSet = ((CCombo) cellEditor.getControl()).getItems();
                                if (namesSet.length > 0 && index > -1 && index < namesSet.length) {
                                    returnedValue = namesSet[index];
                                } else {
                                    returnedValue = null;
                                }
                            } else {
                                returnedValue = null;
                            }
                            return returnedValue;
                        };

                        @Override
                        public Object getCellEditorTypedValue(CellEditor cellEditor, Object originalTypedValue) {
                            CCombo combo = (CCombo) cellEditor.getControl();
                            int rowNumber = ((Table) combo.getParent()).getSelectionIndex();
                            String[] listToDisplay = getItemsToDisplay(element, copyOfTmpParam, rowNumber);
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
                    });
                    break;
                case OPENED_LIST:
                    final EditableComboBoxCellEditor editCellEditor = new EditableComboBoxCellEditor(table,
                            currentParam.getListItemsDisplayName());
                    table.addSelectionListener(new SelectionAdapter() {

                        @Override
                        public void widgetSelected(SelectionEvent e) {
                            String oldValue = null;
                            if (editCellEditor != null) {
                                if (!(editCellEditor.getValue() instanceof String)) {
                                    return;
                                }
                                oldValue = (String) editCellEditor.getValue();
                            }
                            String[] columnItems = null;
                            if (table.getSelection() != null && table.getSelection().length > 0) {
                                TableItem tableItem = table.getSelection()[0];
                                if (tableItem.getData() instanceof Map) {
                                    Map map = (Map) tableItem.getData();
                                    if (currentParam.getFilter() != null && map.get(currentParam.getFilter()) instanceof String) {
                                        String value = (String) map.get(currentParam.getFilter());
                                        if (element instanceof Node) {
                                            List<IConnection> listConnection = (List<IConnection>) ((Node) element).getInputs();
                                            for (IConnection con : listConnection) {
                                                if (con.getName().equals(value) && con.getMetadataTable() != null) {
                                                    List<IMetadataColumn> columns = con.getMetadataTable().getListColumns();
                                                    columnItems = new String[columns.size()];
                                                    for (int i = 0; i < columns.size(); i++) {
                                                        columnItems[i] = columns.get(i).getLabel();
                                                    }
                                                    if (editCellEditor != null) {
                                                        List<String> ret = new ArrayList<String>();
                                                        for (String columnItem : columnItems) {
                                                            ret.add(columnItem);
                                                        }
                                                        for (int i = 0; i < currentParam.getListItemsDisplayName().length; i++) {
                                                            ret.add(currentParam.getListItemsDisplayName()[i]);
                                                        }
                                                        editCellEditor.setItems(ret.toArray(new String[0]));
                                                        editCellEditor.setValue(oldValue);
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    });
                    column.setCellEditor(editCellEditor);
                    break;
                case MODULE_LIST:
                    column.setModifiable((param.getElement() instanceof FakeElement || !param.isRepositoryValueUsed()) && (!param.isReadOnly())
                            && (!currentParam.isReadOnly()));
                    ModuleListCellEditor moduleEditor = new ModuleListCellEditor(table, currentParam, param);
                    moduleEditor.setTableEditorView(this);
                    column.setCellEditor(moduleEditor);
                    column.setLabelProvider(new ModuleTableLabelProvider());
                    break;
                case COLOR:
                    column.setModifiable((!param.isRepositoryValueUsed()) && (!param.isReadOnly())
                            && (!currentParam.isReadOnly()));
                    // column.setDisplayedValue("");

                    column.setLabelProvider(null);
                    column.setCellEditor(new ColorCellEditor(table) {

                        @Override
                        protected void doSetValue(Object value) {
                            if (value instanceof String) {
                                super.doSetValue(ColorUtils.stringToRGB((String) value));
                            } else {
                                super.doSetValue(value);
                            }
                        }

                        @Override
                        protected void updateContents(Object value) {
                            if (value != null) {
                                if (value instanceof String) {
                                    super.updateContents(ColorUtils.stringToRGB((String) value));
                                } else {
                                    super.updateContents(value);
                                }
                            }
                        }

                    });
                    column.setColorProvider(new IColumnColorProvider() {

                        @Override
                        public Color getBackgroundColor(Object bean) {
                            Object value = ((Map<String, Object>) bean).get(items[curCol]);
                            if (value == null || (!(value instanceof String))) {
                                return Display.getCurrent().getSystemColor(SWT.COLOR_WHITE);
                            }
                            return new Color(null, ColorUtils.stringToRGB((String) value));
                        }

                        @Override
                        public Color getForegroundColor(Object bean) {
                            return null;
                        }

                    });
                    break;
                case CHECK:
                    column.setModifiable((!param.isRepositoryValueUsed()) && (!param.isReadOnly())
                            && (!currentParam.isReadOnly()));
                    CheckColumnSelectionListener tableColumnSelectionListener = new CheckColumnSelectionListener(column,
                            tableViewerCreator, currentParam);
                    if (!currentParam.isReadOnly()) {
                        column.setTableColumnSelectionListener(tableColumnSelectionListener);
                    }
                    column.setTableEditorContent(new CheckboxTableEditorContent());
                    Boolean curValue = (Boolean) currentParam.getValue();
                    if (curValue == null) {
                        curValue = Boolean.FALSE;
                    }
                    tableColumnSelectionListener.setChecked(curValue.booleanValue());
                    if (curValue.booleanValue()) {
                        column.setImageHeader(ImageProvider.getImage(EImage.CHECKED_ICON));
                    } else {
                        column.setImageHeader(ImageProvider.getImage(EImage.UNCHECKED_ICON));
                    }
                    column.setDisplayedValue(""); //$NON-NLS-1$
                    break;
                case SCHEMA_TYPE:
                case SCHEMA_REFERENCE:
                    column.setModifiable((!param.isRepositoryValueUsed()) && (!param.isReadOnly())
                            && (!currentParam.isReadOnly()));
                    final INode node = (INode) element;
                    // List<IMetadataTable> tables = node.getMetadataList();

                    // if (isEBCDICNode(node)) { // ebcdic
                    column.setLabelProvider(new IColumnLabelProvider() {

                        @Override
                        public String getLabel(Object bean) {
                            if (bean instanceof Map) {
                                Map<String, Object> valueMap = (Map<String, Object>) bean;
                                String value = (String) valueMap.get(IEbcdicConstant.FIELD_SCHEMA);
                                if (value != null && !"".equals(value)) { //$NON-NLS-1$
                                    IMetadataTable metadataTable = MetadataToolHelper.getMetadataTableFromNodeTableName(node,
                                            value);
                                    if (metadataTable != null) {
                                        if (isEBCDICNode(node)) {
                                            if (isRepositorySchemaLine(node, valueMap)) {
                                                return "Repository (" + metadataTable.getTableName() + ")"; //$NON-NLS-1$ //$NON-NLS-2$
                                            } else {
                                                return "Built-In (" + metadataTable.getTableName() + ")"; //$NON-NLS-1$ //$NON-NLS-2$
                                            }
                                        } else {
                                            return metadataTable.getTableName();
                                        }
                                    } else {
                                        return value;
                                    }
                                }
                            }
                            return ""; //$NON-NLS-1$
                        }
                    });
                    // } else {
                    // column.setLabelProvider(null);
                    // }
                    SchemaCellEditor schemaEditor = new SchemaCellEditor(table, node);
                    schemaEditor.setTableEditorView(this);
                    column.setCellEditor(schemaEditor);
                    break;

                case SAP_SCHEMA_TYPE:
                    column.setModifiable((!param.isRepositoryValueUsed()) && (!param.isReadOnly())
                            && (!currentParam.isReadOnly()));
                    final INode sapNode = (INode) element;

                    column.setLabelProvider(new IColumnLabelProvider() {

                        @Override
                        public String getLabel(Object bean) {
                            if (bean instanceof Map) {
                                Map<String, Object> valueMap = (Map<String, Object>) bean;
                                String value = (String) valueMap.get(IEbcdicConstant.FIELD_SCHEMA);
                                if (value != null && !"".equals(value)) { //$NON-NLS-1$
                                    IMetadataTable metadataTable = MetadataToolHelper.getMetadataTableFromNodeTableName(sapNode,
                                            value);
                                    if (metadataTable != null) {
                                        if (isEBCDICNode(sapNode)) {
                                            if (isRepositorySchemaLine(sapNode, valueMap)) {
                                                return "Repository (" + metadataTable.getTableName() + ")"; //$NON-NLS-1$ //$NON-NLS-2$
                                            } else {
                                                return "Built-In (" + metadataTable.getTableName() + ")"; //$NON-NLS-1$ //$NON-NLS-2$
                                            }
                                        } else if (isSAPNode(sapNode)) {
                                            Object type = valueMap.get(ISAPConstant.TYPE);
                                            if (type instanceof Integer) {
                                                return "";
                                            }
                                            if (type.toString().equals(SINGLE) || type.toString().equals(STRUCTURE)) {
                                                List<IMetadataColumn> columns = metadataTable.getListColumns(true);
                                                StringBuffer values = new StringBuffer();
                                                values.append(metadataTable.getTableName() + ":");
                                                if (metadataTable.getListColumns(true).size() > 0) {
                                                    for (IMetadataColumn column : columns) {
                                                        values.append(column.getDefault() + ",");
                                                    }
                                                    String ret = values.toString();
                                                    return ret.substring(0, ret.length() - 1);
                                                }
                                            } else {
                                                return metadataTable.getTableName();
                                            }
                                        } else {
                                            return metadataTable.getTableName();
                                        }
                                    } else {
                                        return value;
                                    }
                                }
                            }
                            return ""; //$NON-NLS-1$
                        }
                    });

                    schemaEditor = new SchemaCellEditor(table, sapNode);
                    schemaEditor.setTableEditorView(this);
                    column.setCellEditor(schemaEditor);
                    break;

                // hywang add for feature 6484
                case RULE_TYPE:
                    column.setTitle("Rule"); //$NON-NLS-1$
                    column.setModifiable((!param.isRepositoryValueUsed()) && (!param.isReadOnly())
                            && (!currentParam.isReadOnly()));
                    final INode node1 = (INode) element;
                    column.setLabelProvider(new IColumnLabelProvider() {

                        @Override
                        public String getLabel(Object bean) {
                            if (bean instanceof Map) {
                                Map<String, Object> valueMap = (Map<String, Object>) bean;
                                String value = (String) valueMap.get(IRuleConstant.FIELD_RULE);
                                if (value != null && !"".equals(value)) { //$NON-NLS-1$
                                    IMetadataTable metadataTable = MetadataToolHelper.getMetadataTableFromNodeTableName(node1,
                                            value);
                                    if (metadataTable != null) {
                                        return metadataTable.getTableName();
                                    } else {
                                        return value;
                                    }
                                }
                            }
                            return ""; //$NON-NLS-1$
                        }
                    });
                    RuleCellEditor ruleEditor = new RuleCellEditor(table, node1);
                    ruleEditor.setTableEditorView(this);
                    column.setCellEditor(ruleEditor);
                    break;

                case SCHEMA_XPATH_QUERYS:

                    column.setModifiable((!param.isRepositoryValueUsed()) && (!param.isReadOnly())
                            && (!currentParam.isReadOnly()));
                    final INode node2 = (INode) element;
                    SchemaXPathQuerysCellEditor schemaXPathEditor = new SchemaXPathQuerysCellEditor(table, node2);
                    schemaXPathEditor.setTableEditorView(this);
                    column.setCellEditor(schemaXPathEditor);

                    break;


                case MULTI_PATTERN:
                    column.setModifiable(true);
                    PatternCellEditor patternEditor = new PatternCellEditor(table,element);
                    patternEditor.setTableEditorView(this);
                    column.setCellEditor(patternEditor);
                    break;

                case PATTERN_PROPERTY:
                    column.setModifiable(true);
                    PatternPropertyCellEditor patternPropertyEditor = new PatternPropertyCellEditor(table,element);
                    patternPropertyEditor.setTableEditorView(this);
                    column.setCellEditor(patternPropertyEditor);
                    break;

                default: // TEXT
                    TextCellEditor tcEditor = null;
                    if (((i == 0) && (param.isBasedOnSchema() || param.isBasedOnSubjobStarts()))
                            || (param.isRepositoryValueUsed()) || (param.isReadOnly()) || currentParam.isReadOnly()) {
                        // read only cell
                        if (!param.getElement().isReadOnly()
                                && (param.getName().equals("HADOOP_ADVANCED_PROPERTIES")
                                        || param.getName().equals("SPARK_ADVANCED_PROPERTIES") || param.getName().equals(
                                        "HBASE_PARAMETERS"))) {
                            if (currentParam.isNoContextAssist()) {
                                tcEditor = new TextCellEditor(table);
                            } else {
                                TextCellEditorWithProposal textCellEditor = new TextCellEditorWithProposal(table, column);
                                textCellEditor.setContentProposalProvider(processProposalProvider);
                                tcEditor = textCellEditor;
                            }
                        }
                    } else {
                        // writable cell
                        if (currentParam.isNoContextAssist()) {
                            tcEditor = new TextCellEditor(table);
                        } else {
                            TextCellEditorWithProposal textCellEditor = new TextCellEditorWithProposal(table, column);
                            textCellEditor.setContentProposalProvider(processProposalProvider);
                            tcEditor = textCellEditor;
                        }
                    }
                    if (tcEditor != null) {
                        column.setCellEditor(tcEditor);
                        column.setModifiable((!param.isRepositoryValueUsed()) && (!param.isReadOnly())
                                && (!currentParam.isReadOnly()));
                    }
                }
                // for all kinds of column, check if read only or not when edit the field.
                column.setColumnCellModifier(new ColumnCellModifier(column) {

                    @Override
                    public boolean canModify(Object bean) {
                        if (param.getName().equals("HADOOP_ADVANCED_PROPERTIES")
                                || param.getName().equals("SPARK_ADVANCED_PROPERTIES")
                                || param.getName().equals("HBASE_PARAMETERS")) {
                            boolean canModify = super.canModify(bean);
                            if (canModify) {
                                Map<String, Object> valueMap = (Map<String, Object>) bean;
                                List<Map<String, Object>> fullValues = (List<Map<String, Object>>) param.getValue();
                                ((ElementParameter) currentParam).setCurrentRow(fullValues.indexOf(valueMap));
                                if (currentParam.isReadOnly(element.getElementParameters())) {
                                    return false;
                                }
                            } else {
                                Map<String, Object> valueMap = (Map<String, Object>) bean;
                                if (valueMap.get("BUILDIN") != null && valueMap.get("BUILDIN").equals("TRUE")) {
                                    return true;
                                } else {
                                    return false;
                                }
                            }
                            return canModify;
                        }
                        boolean canModify = super.canModify(bean);
                        if (canModify) {
                            Map<String, Object> valueMap = (Map<String, Object>) bean;
                            List<Map<String, Object>> fullValues = (List<Map<String, Object>>) param.getValue();
                            ((ElementParameter) currentParam).setCurrentRow(fullValues.indexOf(valueMap));
                            if (currentParam.isReadOnly(element.getElementParameters())) {
                                return false;
                            }
                        }
                        return canModify;
                    }

                });
                column.setColorProvider(new IColumnColorProvider<B>() {

                    @Override
                    public Color getBackgroundColor(B bean) {
                        Map<String, Object> valueMap = (Map<String, Object>) bean;
                        List<Map<String, Object>> fullValues = (List<Map<String, Object>>) param.getValue();

                        // hyWang add varriable index for bug 7294
                        int index = fullValues.indexOf(valueMap);
                        if (index >= 0) {
                            ((ElementParameter) currentParam).setCurrentRow(index);
                            if (currentParam.isReadOnly(element.getElementParameters())) {
                                return AbstractMetadataTableEditorView.READONLY_CELL_BG_COLOR;
                            }
                        }
                        if (param.getName().equals("HADOOP_ADVANCED_PROPERTIES")
                                || param.getName().equals("SPARK_ADVANCED_PROPERTIES")
                                || param.getName().equals("HBASE_PARAMETERS")) {
                            if (valueMap.get("BUILDIN") == null || valueMap.get("BUILDIN") != null
                                    && valueMap.get("BUILDIN").equals("")) {
                                return Display.getCurrent().getSystemColor(SWT.COLOR_GRAY);
                            }
                        }

                        if (currentParam.getFieldType() == EParameterFieldType.CONTEXT_PARAM_NAME_LIST) {
                            Object value = ((Map<String, Object>) bean).get(items[curCol]);
                            boolean found = false;
                            Object[] items = currentParam.getListItemsValue();
                            for (Object item : items) {
                                if (item.equals(value)) {
                                    found = true;
                                    break;
                                }
                            }
                            if (!found) {
                                return Display.getCurrent().getSystemColor(SWT.COLOR_RED);
                            }
                        }
                        return null;
                    }

                    @Override
                    public Color getForegroundColor(B bean) {
                        return null;
                    }

                });
                column.setBeanPropertyAccessors(new IBeanPropertyAccessors<B, Object>() {

                    @Override
                    public Object get(B bean) {
                        Object value = ((Map<String, Object>) bean).get(items[curCol]);
                        if (value == null) {
                            return ""; //$NON-NLS-1$
                        }
                        if (itemsValue[curCol] instanceof IElementParameter) {
                            IElementParameter tmpParam = (IElementParameter) itemsValue[curCol];
                            boolean hideValue = false;
                            if (!tmpParam.isReadOnly()) {
                                if ((tmpParam.getReadOnlyIf() != null || tmpParam.getNotReadOnlyIf() != null)
                                        && tmpParam.isReadOnly(element.getElementParameters())) {
                                    hideValue = true;
                                }
                            }

                            switch (tmpParam.getFieldType()) {
                            case CONTEXT_PARAM_NAME_LIST:
                            case CLOSED_LIST:
                            case COMPONENT_LIST:
                            case COLUMN_LIST:
                            case CONNECTION_LIST:
                            case LOOKUP_COLUMN_LIST:
                            case PREV_COLUMN_LIST:
                            case DBTYPE_LIST:
                                if (hideValue) {
                                    return "";//$NON-NLS-1$
                                }
                                String[] namesSet = tmpParam.getListItemsDisplayName();
                                if (namesSet.length == 0) {
                                    return tmpParam.getDefaultClosedListValue();
                                }
                                if (value instanceof String) {
                                    boolean found = false;
                                    int index = 0;
                                    if (currentParam.getFieldType() == EParameterFieldType.CONTEXT_PARAM_NAME_LIST) {
                                        index = -1; // if not found, won't use first(index 0) instead
                                    }
                                    Object[] items = currentParam.getListItemsValue();
                                    for (int j = 0; j < items.length && !found; j++) {
                                        if (items[j].equals(value)) {
                                            found = true;
                                            index = j;
                                        }
                                    }
                                    value = new Integer(index);
                                }
                                if (value != null && ((Integer) value) >= 0) {
                                    return namesSet[(Integer) value];
                                }
                                return null;
                            case OPENED_LIST:
                                if (hideValue) {
                                    return "";//$NON-NLS-1$
                                }
                                String[] listItemsValue = tmpParam.getListItemsDisplayName();
                                if (listItemsValue.length == 0) {
                                    return value;
                                }
                                int index = -1;
                                if (value instanceof String) {
                                    boolean found = false;
                                    Object[] items = ((IElementParameter) itemsValue[curCol]).getListItemsValue();
                                    for (int j = 0; j < items.length && !found; j++) {
                                        if (items[j].equals(value)) {
                                            found = true;
                                            index = j;
                                        }
                                    }
                                }
                                Integer count = new Integer(index);
                                if (count >= 0) {
                                    return listItemsValue[count];
                                } else if (count < 0) {
                                    return value;
                                }
                                return value;

                            case CHECK:
                                if (hideValue) {
                                    return false;
                                }
                                if (value instanceof String) {
                                    return new Boolean((String) value);
                                }
                                return value;
                            case RADIO:
                                if (hideValue) {
                                    return false;
                                }
                                if (value instanceof String) {
                                    return new Boolean((String) value);
                                }
                                return value;
                            case COLOR:
                                if (value instanceof String) {
                                    return ColorUtils.stringToRGB((String) value);
                                }
                                return value; // already RGB
                            default: // TEXT
                                if (hideValue) {
                                    return "";//$NON-NLS-1$
                                }
                                return value;
                            }
                        }
                        return value;
                    }

                    @Override
                    public void set(B bean, Object value) {
                        Object finalValue = value;
                        IElementParameter tmpParam = (IElementParameter) itemsValue[curCol];
                        // TODO should test if this parameter is contained in any other show if / not show if, etc..
                        boolean included = false;
                        for (Object object : param.getListItemsValue()) {
                            if (object instanceof IElementParameter) {
                                if (((IElementParameter) object).getShowIf() != null
                                        && ((IElementParameter) object).getShowIf().contains(tmpParam.getName())) {
                                    included = true;
                                    break;
                                }
                                if (((IElementParameter) object).getNotShowIf() != null
                                        && ((IElementParameter) object).getNotShowIf().contains(tmpParam.getName())) {
                                    included = true;
                                    break;
                                }
                                if (((IElementParameter) object).getReadOnlyIf() != null
                                        && ((IElementParameter) object).getReadOnlyIf().contains(tmpParam.getName())) {
                                    included = true;
                                    break;
                                }
                                if (((IElementParameter) object).getNotReadOnlyIf() != null
                                        && ((IElementParameter) object).getNotReadOnlyIf().contains(tmpParam.getName())) {
                                    included = true;
                                    break;
                                }
                            }
                        }
                        if (included) {
                            IElementParameter param = element.getElementParameter(EParameterName.UPDATE_COMPONENTS.getName());
                            if (param != null) {
                                param.setValue(Boolean.TRUE);
                            }
                        }

                        boolean isNeedReCheck = false;
                        switch (tmpParam.getFieldType()) {
                        case CONTEXT_PARAM_NAME_LIST:
                        case CLOSED_LIST:
                        case COLUMN_LIST:
                        case COMPONENT_LIST:
                        case CONNECTION_LIST:
                        case LOOKUP_COLUMN_LIST:
                        case PREV_COLUMN_LIST:
                            isNeedReCheck = true;
                            if (value instanceof String) {
                                Object[] itemNames = ((IElementParameter) itemsValue[curCol]).getListItemsDisplayName();
                                Object[] itemValues = ((IElementParameter) itemsValue[curCol]).getListItemsValue();
                                boolean found = false;
                                int index = 0;
                                for (int j = 0; j < itemNames.length && !found; j++) {
                                    if (itemNames[j].equals(value)) {
                                        found = true;
                                        index = j;
                                    }
                                }
                                if (value != null && (index >= 0)) {
                                    finalValue = itemValues[new Integer(index)];
                                }
                            }
                            break;
                        case OPENED_LIST:
                            if (value instanceof String) {
                                Object[] itemNames = ((IElementParameter) itemsValue[curCol]).getListItemsDisplayName();
                                Object[] itemValues = ((IElementParameter) itemsValue[curCol]).getListItemsValue();
                                boolean found = false;
                                int index = -1;
                                for (int j = 0; j < itemNames.length && !found; j++) {
                                    if (itemNames[j].equals(value)) {
                                        found = true;
                                        index = j;
                                    }
                                }
                                if (value != null && (index >= 0)) {
                                    finalValue = itemValues[new Integer(index)];
                                } else if (value != null && (index < 0)) {
                                    finalValue = value;
                                }
                            }
                            break;
                        case COLOR:
                            if (value instanceof RGB) {
                                RGB rgb = (RGB) value;
                                finalValue = rgb.red + ";" + rgb.green + ";" + rgb.blue; //$NON-NLS-1$ //$NON-NLS-2$
                            }
                            break;
                        case NUMBERLIMITTEXT:
                            
                            if(value==null||StringUtils.isBlank(value.toString()) ) {
                                finalValue ="0";
                                break;
                            }
                            String finalStrValue=value.toString();
                            if(finalStrValue.startsWith(NumberLimitTextController.CONTEXPREFIX)||NumberLimitTextController.CONTEXPREFIX.startsWith(finalStrValue)) {
                              //context mode no any limit
                              finalValue =value;
                              break;
                            }else if(finalStrValue.length()>=9){
                                //keep 6 digit after the Decimal point
                                finalStrValue =finalStrValue.trim().substring(0, 8);
                            }else if(finalStrValue.contains("-")) {
                                //'-' is invalid input 
                                finalStrValue =finalStrValue.trim().replaceAll("-", "");
                            }

                            try {
                                double num=Double.valueOf(finalStrValue);
                                if(num>1||num<0) {
                                    finalValue="1";
                                    break;
                                }else if(num<0) {
                                    finalValue="0";
                                    break;
                                }
                                finalValue=finalStrValue;
                            } catch (Exception e1) {
                                // any exception then reset result be 0
                                finalValue="0";
                            }
                            
                        default:
                        }
                        ((Map<String, Object>) bean).put(items[curCol], finalValue);
                        resetValuesIfNeeded(element, param, (Map<String, Object>) bean);
                        /*
                         * TDI-6568, in fact, no need reset the value. just want to enable
                         * "firePropertyChange(RETURNS_CHANGED, null, null)" in Node.
                         */
                        if (param.getFieldType().equals(EParameterFieldType.TABLE)) {
                            element.setPropertyValue(param.getName(), param.getValue());
                        }

                        if (isNeedReCheck && element instanceof Node) {
                            IProcess process = ((Node) element).getProcess();
                            if (process instanceof IProcess2) {
                                ((IProcess2) process).checkProcess();
                            }
                            // enable to refresh component setting after change modules.
                            // so far, for cMessagingEndpoint (TUP-1119)
                            final IElementParameter copyOfTmpParam = currentParam;
                            if (element != null && "LIBPATH".equals(copyOfTmpParam.getName())) { //$NON-NLS-1$
                                IElementParameter updateComponentsParam = element
                                        .getElementParameter(EParameterName.UPDATE_COMPONENTS.getName());
                                if (updateComponentsParam != null) {
                                    updateComponentsParam.setValue(Boolean.TRUE);
                                }
                            }
                        }
                    }
                });
            }
        }
    }

    private boolean isEBCDICNode(INode node) {
        if (PluginChecker.isEBCDICPluginLoaded()) {
            IEBCDICProviderService service = (IEBCDICProviderService) GlobalServiceRegister.getDefault().getService(
                    IEBCDICProviderService.class);
            if (service != null) {
                return service.isEbcdicNode(node);
            }
        }
        return false;
    }

    private boolean isSAPNode(INode node) {
        if (PluginChecker.isSAPWizardPluginLoaded()) {
            ISAPProviderService service = (ISAPProviderService) GlobalServiceRegister.getDefault().getService(
                    ISAPProviderService.class);
            if (service != null) {
                return service.isSAPNode(node);
            }
        }
        return false;
    }

    private boolean isRepositorySchemaLine(INode node, Map<String, Object> lineValue) {
        if (PluginChecker.isEBCDICPluginLoaded()) {
            IEBCDICProviderService service = (IEBCDICProviderService) GlobalServiceRegister.getDefault().getService(
                    IEBCDICProviderService.class);
            if (service != null) {
                EbcdicConnectionItem repositoryItem = service.getRepositoryItem(node);
                return repositoryItem != null && service.isRepositorySchemaLine(node, lineValue);
            }
        }
        return false;
    }

    public PropertiesTableToolbarEditorView getToolBar() {
        return (PropertiesTableToolbarEditorView) getExtendedToolbar();
    }

    public PropertiesTableEditorModel getModel() {
        return (PropertiesTableEditorModel) getExtendedTableModel();
    }

    private void resetValuesIfNeeded(IElement element, IElementParameter mainParam, Map<String, Object> currentLine) {
        List<Map<String, Object>> tableValues = (List<Map<String, Object>>) mainParam.getValue();
        int rowNumber = tableValues.indexOf(currentLine);
        if (rowNumber != -1) {
            for (Object item : mainParam.getListItemsValue()) {
                if (item instanceof IElementParameter) {
                    IElementParameter curParam = (IElementParameter) item;
                    switch (curParam.getFieldType()) {
                    case CLOSED_LIST:
                        String[] itemsToDisplay = getItemsToDisplay(element, curParam, rowNumber);
                        Object currentValue = currentLine.get(curParam.getName());
                        String currentDisplay = null;
                        if (currentValue instanceof Integer) {
                            if (((Integer) currentValue) < curParam.getListItemsDisplayName().length && (Integer)currentValue >= 0) {
                                currentDisplay = curParam.getListItemsDisplayName()[(Integer) currentValue];
                            }
                        } else if (currentValue instanceof String) {
                            int index = ArrayUtils.indexOf(curParam.getListItemsValue(), currentValue);
                            if (index != -1) {
                                currentDisplay = curParam.getListItemsDisplayName()[index];
                            }
                        }

                        if (currentDisplay != null) {
                            // if the current displayed shouldn't be used anymore, reset the value.
                            if (!ArrayUtils.contains(itemsToDisplay, currentDisplay)) {
                                String newValue = ""; //$NON-NLS-1$
                                if (itemsToDisplay.length > 0) {
                                    int index = ArrayUtils.indexOf(curParam.getListItemsDisplayName(), itemsToDisplay[0]);
                                    newValue = (String) curParam.getListItemsValue()[index];
                                }
                                currentLine.put(curParam.getName(), newValue);
                            }
                        }else if(itemsToDisplay.length >0){
                        	  currentLine.put(curParam.getName(), 0);
                        }
                        break;
                    default:
                    }

                }
            }
        }
    }

    /**
     * DOC nrousseau Comment method "getItemsToDisplay".
     *
     * @param element
     * @param param
     * @param rowNumber
     * @return
     */
    private String[] getItemsToDisplay(final IElement element, final IElementParameter param, int rowNumber) {
        if (param instanceof ElementParameter) {
            ((ElementParameter) param).setCurrentRow(rowNumber);
        }
        String[] originalList = param.getListItemsDisplayName();
        List<String> stringToDisplay = new ArrayList<String>();
        String[] itemsShowIf = param.getListItemsShowIf();
        if (itemsShowIf != null && (itemsShowIf.length > 0)) {
            String[] itemsNotShowIf = param.getListItemsNotShowIf();
            for (int i = 0; i < originalList.length; i++) {
                if (param.isShow(itemsShowIf[i], itemsNotShowIf[i], element.getElementParameters())) {
                    stringToDisplay.add(originalList[i]);
                }
            }
        } else {
            for (String element2 : originalList) {
                stringToDisplay.add(element2);
            }
        }
        String[] listToDisplay = stringToDisplay.toArray(new String[0]);
        return listToDisplay;
    }

    class ModuleTableLabelProvider implements IColumnLabelProvider {

        private final String[] KEYS = new String[] { "drivers", "JAR_NAME" };

        @Override
        public String getLabel(Object bean) {
            if (bean instanceof Map) {
                Map<String, String> valueMap = (Map<String, String>) bean;
                for (String key : KEYS) {
                    if (valueMap.containsKey(key)) {
                        String value = valueMap.get(key);
                        return getModuleName(value);
                    }
                }
            }
            return "newLine";
        }
    }

    private static String getModuleName(String jarPath) {
        if (jarPath != null) {
            jarPath = TalendQuoteUtils.removeQuotes(jarPath);
            if (jarPath.startsWith(MavenUrlHelper.MVN_PROTOCOL)) {
                MavenArtifact art = MavenUrlHelper.parseMvnUrl(jarPath);
                return art.getFileName();
            }
        }
        return jarPath;
    }

}
