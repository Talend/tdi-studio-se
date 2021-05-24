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
package org.talend.designer.xmlmap.ui.tabs.table;

import org.eclipse.jface.viewers.ComboBoxCellEditor;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Table;
import org.talend.commons.ui.runtime.image.EImage;
import org.talend.commons.ui.runtime.image.ImageProvider;
import org.talend.commons.ui.runtime.swt.tableviewer.CellEditorValueAdapterFactory;
import org.talend.commons.ui.runtime.swt.tableviewer.behavior.CheckColumnSelectionListener;
import org.talend.commons.ui.runtime.swt.tableviewer.behavior.ColumnCellModifier;
import org.talend.commons.ui.runtime.swt.tableviewer.behavior.IColumnColorProvider;
import org.talend.commons.ui.runtime.swt.tableviewer.behavior.IColumnLabelProvider;
import org.talend.commons.ui.swt.proposal.ContentProposalAdapterExtended;
import org.talend.commons.ui.swt.proposal.TextCellEditorWithProposal;
import org.talend.commons.ui.swt.tableviewer.TableViewerCreator;
import org.talend.commons.ui.swt.tableviewer.TableViewerCreator.CELL_EDITOR_STATE;
import org.talend.commons.ui.swt.tableviewer.TableViewerCreatorColumn;
import org.talend.commons.ui.swt.tableviewer.celleditor.DialogErrorForCellEditorListener;
import org.talend.commons.ui.swt.tableviewer.tableeditor.CheckboxTableEditorContent;
import org.talend.commons.utils.data.bean.IBeanPropertyAccessors;
import org.talend.core.model.metadata.types.JavaTypesManager;
import org.talend.core.ui.proposal.JavaSimpleDateFormatProposalProvider;
import org.talend.designer.xmlmap.model.emf.xmlmap.TreeNode;
import org.talend.designer.xmlmap.model.emf.xmlmap.XmlMapData;
import org.talend.designer.xmlmap.ui.XmlMapLabelProvider;
import org.talend.designer.xmlmap.ui.expressionutil.XmlMapExpressionManager;
import org.talend.designer.xmlmap.util.XmlMapUtil;

/**
 * DOC talend class global comment. Detailled comment
 */
public class XmlTreeSchemaTableView extends AbstractXmlTreeSchemaTableView {

    private static final String ID_COLUMN_XPATH = "xpath";

    private static final String ID_COLUMN_KEY = "key";

    private static final String ID_COLUMN_TYPE = "talend type";

    private static final String ID_COLUMN_NULLABLE = "nullable";

    private static final String ID_COLUMN_PATTERN = "pattern";

    public static final Color READONLY_CELL_BG_COLOR = Display.getCurrent().getSystemColor(SWT.COLOR_GRAY);

    public XmlTreeSchemaTableView(Composite parentComposite, int mainCompositeStyle) {
        super(parentComposite, mainCompositeStyle);
    }

    @Override
    protected void createColumns(final TableViewerCreator<TreeNode> tableViewerCreator, Table table) {
        TableViewerCreatorColumn column = new TableViewerCreatorColumn(tableViewerCreator);
        column.setTitle("XPath");
        column.setId(ID_COLUMN_XPATH);
        column.setWeight(20);
        column.setModifiable(true);
        column.setBeanPropertyAccessors(new IBeanPropertyAccessors<TreeNode, Object>() {

            @Override
            public Object get(TreeNode bean) {
                return bean.getXpath();
            }

            @Override
            public void set(TreeNode bean, Object value) {
                if (isValidName) {
                    bean.setName((String) value);
                    // String xPath = bean.getXPath();
                    // xPath = xPath.substring(0, xPath.lastIndexOf(XmlMapUtil.XPATH_SEPARATOR) + 1);
                    // NodeType nodeType = bean.getTreeNode().getNodeType();
                    // String typedValue = null;
                    // if (NodeType.ATTRIBUT.equals(nodeType)) {
                    // typedValue = xPath + XmlMapUtil.XPATH_ATTRIBUTE + bean.getName();
                    // } else if (NodeType.NAME_SPACE.equals(nodeType)) {
                    // typedValue = xPath + XmlMapUtil.XPATH_NAMESPACE + bean.getName();
                    // } else {
                    // typedValue = xPath + bean.getName();
                    // }
                    // bean.setXPath(typedValue);
                    XmlMapData mapperData = XmlMapUtil.getXmlMapData(bean);
                    XmlMapExpressionManager expressionManager = new XmlMapExpressionManager();
                    XmlMapUtil.updateXPathAndExpression(mapperData, expressionManager, bean, bean.getName(),
                            XmlMapUtil.getXPathLength(bean.getXpath()), true);
                    if (!bean.getChildren().isEmpty()) {
                        refresh();
                    }
                }
            }
        });
        final TextCellEditor cellEditor = new TextCellEditor(tableViewerCreator.getTable());
        cellEditor.addListener(new DialogErrorForCellEditorListener(cellEditor, column) {

            @Override
            public void newValidValueTyped(int itemIndex, Object previousValue, Object newValue, CELL_EDITOR_STATE state) {
            }

            @Override
            public String validateValue(String newValue, int beanPosition) {
                return validateXPath(newValue, beanPosition);
            }

        });
        column.setCellEditor(cellEditor);
        column.setColumnCellModifier(new ColumnCellModifier(column) {

            @Override
            public Object getValue(Object bean) {
                TreeNode entry = (TreeNode) bean;
                return entry.getName();
            }

            @Override
            public boolean modify(Object bean, Object value) {
                return false;
            }
        });

        column = new TableViewerCreatorColumn(tableViewerCreator);
        column.setTitle("Key");
        column.setToolTipHeader("Key");
        column.setId(ID_COLUMN_KEY);
        column.setDisplayedValue(""); //$NON-NLS-1$
        column.setWeight(10);
        column.setModifiable(true);
        CheckboxTableEditorContent checkbox = new CheckboxTableEditorContent();
        column.setTableEditorContent(checkbox);
        column.setBeanPropertyAccessors(new IBeanPropertyAccessors<TreeNode, Boolean>() {

            @Override
            public Boolean get(TreeNode bean) {
                return bean.isKey();
            }

            @Override
            public void set(TreeNode bean, Boolean value) {
                bean.setKey(value);
            }
        });

        column = new TableViewerCreatorColumn(tableViewerCreator);
        column.setTitle("Type");
        column.setToolTipHeader("Type");
        column.setId(ID_COLUMN_TYPE);
        column.setBeanPropertyAccessors(getTalendTypeAccessor());
        column.setModifiable(true);
        column.setWeight(20);

        TreeSchemaJavaTypeComboValueAdapter comboValueAdapter = new TreeSchemaJavaTypeComboValueAdapter<TreeNode>(
                JavaTypesManager.getDefaultJavaType(), getNullableAccessor());

        ComboBoxCellEditor typeComboEditor = new ComboBoxCellEditor(tableViewerCreator.getTable(),
                comboValueAdapter.getTalendTypesLabels(), SWT.READ_ONLY);
        CCombo typeCombo = (CCombo) typeComboEditor.getControl();
        typeCombo.setEditable(false);
        column.setCellEditor(typeComboEditor, comboValueAdapter);

        column = new TableViewerCreatorColumn(tableViewerCreator);
        column.setTitle("Nullable");
        column.setToolTipHeader("Nullable");
        column.setId(ID_COLUMN_NULLABLE);
        column.setBeanPropertyAccessors(getNullableAccessor());
        column.setWeight(20);
        column.setDisplayedValue(""); //$NON-NLS-1$
        column.setModifiable(true);
        column.setTableColumnSelectionListener(new CheckColumnSelectionListener(column, tableViewerCreator));
        column.setImageHeader(ImageProvider.getImage(EImage.CHECKED_ICON));
        CheckboxTableEditorContent nullableCheckbox = new CheckboxTableEditorContent();
        column.setTableEditorContent(nullableCheckbox);

        column = new TableViewerCreatorColumn(tableViewerCreator);
        column.setTitle("Pattern");
        column.setId(ID_COLUMN_PATTERN);
        column.setWeight(20);
        column.setBeanPropertyAccessors(new IBeanPropertyAccessors<TreeNode, String>() {

            @Override
            public String get(TreeNode bean) {
                return bean.getPattern();
            }

            @Override
            public void set(TreeNode bean, String value) {
                bean.setPattern(value);
            }
        });
        final ColumnCellModifier columnCellModifier = new ColumnCellModifier(column) {

            @Override
            public boolean canModify(Object bean) {
                boolean typeIsDate = currentBeanHasJavaDateType(bean) && !isReadOnly();
                return typeIsDate;
            }

        };
        column.setColorProvider(new IColumnColorProvider() {

            @Override
            public Color getBackgroundColor(Object bean) {
                if (!columnCellModifier.canModify(bean)) {
                    return READONLY_CELL_BG_COLOR;
                }
                return null;
            }

            @Override
            public Color getForegroundColor(Object bean) {
                return null;
            }

        });
        column.setLabelProvider(new IColumnLabelProvider() {

            /*
             * (non-Javadoc)
             *
             * @see org.talend.commons.ui.swt.tableviewer.behavior.IColumnLabelProvider#getLabel(java.lang.Object)
             */
            @Override
            public String getLabel(Object bean) {
                if (!currentBeanHasJavaDateType(bean)) {
                    return ""; //$NON-NLS-1$
                }
                return null;
            }

        });
        column.setColumnCellModifier(columnCellModifier);
        JavaSimpleDateFormatProposalProvider proposalProvider = new JavaSimpleDateFormatProposalProvider();
        TextCellEditorWithProposal patternCellEditor = new TextCellEditorWithProposal(tableViewerCreator.getTable(), column);
        ContentProposalAdapterExtended contentProposalAdapter = patternCellEditor.getContentProposalAdapter();
        contentProposalAdapter.setFilterStyle(ContentProposalAdapterExtended.FILTER_NONE);
        contentProposalAdapter.setProposalAcceptanceStyle(ContentProposalAdapterExtended.PROPOSAL_INSERT);
        patternCellEditor.setContentProposalProvider(proposalProvider);
        column.setCellEditor(patternCellEditor, CellEditorValueAdapterFactory.getNullToEmptyStringTextAdapater());

        tableViewerCreator.setCellModifier(new XmlCellModifier(tableViewerCreator));
    }

    private boolean currentBeanHasJavaDateType(Object element) {
        String talendType = getTalendTypeAccessor().get((TreeNode) element);
        boolean typeIsDate = JavaTypesManager.DATE.getId().equals(talendType);
        return typeIsDate;
    }

    private IBeanPropertyAccessors<TreeNode, String> getTalendTypeAccessor() {
        return new IBeanPropertyAccessors<TreeNode, String>() {

            @Override
            public String get(TreeNode bean) {
                return bean.getType();
            }

            @Override
            public void set(TreeNode bean, String value) {
                bean.setType(value);
                if (currentBeanHasJavaDateType(bean)) {
                    bean.setPattern(new JavaSimpleDateFormatProposalProvider().getProposals(null, 0)[0].getContent());
                }
            }
        };
    }

    private IBeanPropertyAccessors<TreeNode, Boolean> getNullableAccessor() {
        return new IBeanPropertyAccessors<TreeNode, Boolean>() {

            @Override
            public Boolean get(TreeNode bean) {
                return bean.isNullable();
            }

            @Override
            public void set(TreeNode bean, Boolean value) {
                bean.setNullable(value);
            }
        };
    }

    @Override
    protected void setTableViewerCreatorOptions(TableViewerCreator<TreeNode> newTableViewerCreator) {
        super.setTableViewerCreatorOptions(newTableViewerCreator);
        newTableViewerCreator.setLabelProvider(new XmlMapLabelProvider(newTableViewerCreator));
    }
}
