// ============================================================================
//
// Copyright (C) 2006-2019 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.designer.dbmap.ui.visualmap.table;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.viewers.ICellEditorListener;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.talend.commons.ui.runtime.swt.celleditor.ComboxCellEditorImproved;
import org.talend.commons.ui.runtime.swt.tableviewer.CellEditorValueAdapterFactory;
import org.talend.commons.ui.runtime.swt.tableviewer.TableViewerCreatorColumnNotModifiable;
import org.talend.commons.ui.runtime.swt.tableviewer.TableViewerCreatorColumnNotModifiable.ALIGNMENT;
import org.talend.commons.ui.runtime.swt.tableviewer.TableViewerCreatorNotModifiable;
import org.talend.commons.ui.runtime.swt.tableviewer.behavior.IColumnColorProvider;
import org.talend.commons.ui.runtime.swt.tableviewer.data.ModifiedObjectInfo;
import org.talend.commons.ui.runtime.swt.tableviewer.tableeditor.TableEditorContentNotModifiable;
import org.talend.commons.ui.runtime.ws.WindowSystem;
import org.talend.commons.ui.swt.tableviewer.TableViewerCreator;
import org.talend.commons.ui.swt.tableviewer.TableViewerCreatorColumn;
import org.talend.commons.ui.swt.tableviewer.behavior.DefaultCellModifier;
import org.talend.commons.ui.swt.tableviewer.tableeditor.CheckboxTableEditorContent;
import org.talend.commons.utils.data.bean.IBeanPropertyAccessors;
import org.talend.designer.abstractmap.model.tableentry.ITableEntry;
import org.talend.designer.core.model.components.EParameterName;
import org.talend.designer.dbmap.i18n.Messages;
import org.talend.designer.dbmap.language.AbstractDbLanguage.JOIN;
import org.talend.designer.dbmap.language.IJoinType;
import org.talend.designer.dbmap.language.operator.IDbOperator;
import org.talend.designer.dbmap.language.operator.IDbOperatorManager;
import org.talend.designer.dbmap.managers.MapperManager;
import org.talend.designer.dbmap.managers.ProblemsManager;
import org.talend.designer.dbmap.model.table.InputTable;
import org.talend.designer.dbmap.model.tableentry.InputColumnTableEntry;
import org.talend.designer.dbmap.ui.image.ImageInfo;
import org.talend.designer.dbmap.ui.image.ImageProviderMapper;
import org.talend.designer.dbmap.ui.visualmap.zone.Zone;

/**
 * DOC amaumont class global comment. Detailled comment <br/>
 *
 * $Id: InputDataMapTableView.java 1782 2007-02-03 07:57:38Z bqian $
 *
 */
public class InputDataMapTableView extends DataMapTableView {

    public static final Color READONLY_CELL_BG_COLOR = Display.getCurrent().getSystemColor(SWT.COLOR_GRAY);

    private ToolItem dropDownItem;

    private InputTableCellModifier cellModifier;

    private Menu menu = null;

    public InputDataMapTableView(Composite parent, int style, InputTable inputTable, MapperManager mapperManager) {
        super(parent, style, inputTable, mapperManager);
    }

    @Override
    public void initColumnsOfTableColumns(final TableViewerCreator tableViewerCreatorForColumns) {
        final TableViewerCreatorColumn columnJoin = new TableViewerCreatorColumn(tableViewerCreatorForColumns);
        String useInJoinTitle = Messages.getString("InputDataMapTableView.columnTitle.ExplicitJoin"); //$NON-NLS-1$
        columnJoin.setTitle(useInJoinTitle);
        columnJoin.setId(ID_EXPLICIT_JOIN);
        columnJoin.setBeanPropertyAccessors(new IBeanPropertyAccessors<InputColumnTableEntry, Boolean>() {

            public Boolean get(InputColumnTableEntry bean) {
                return bean.isJoin();
            }

            public void set(InputColumnTableEntry bean, Boolean value) {
                TableEditorContentNotModifiable tableEditorContent = columnJoin.getTableEditorContent();
                if (tableEditorContent != null && tableEditorContent instanceof CheckboxTableEditorContent) {
                    CheckboxTableEditorContent checkBoxEditor = (CheckboxTableEditorContent) tableEditorContent;
                    Button check = checkBoxEditor.getCheck();
                    bean.setCheckbox(check);
                }
                bean.setJoin(value);
                boolean enable = true;
                if (dropDownItem != null && !dropDownItem.isDisposed()) {
                    enable = dropDownItem.isEnabled();
                }
                if (enable && value
                        && mapperManager.getCurrentLanguage().unuseWithExplicitJoin().contains(getInputTable().getJoinType())) {
                    if (menu != null) {
                        MenuItem[] menuItems = menu.getItems();

                        for (MenuItem mi : menuItems) {
                            if (mi.getImage() == null) {
                                continue;
                            }
                            if (mi.getText().equals(getInputTable().getJoinType().getLabel())) {
                                mi.setImage(null);
                            }
                        }
                        menuItems[1].setImage(ImageProviderMapper.getImage(ImageInfo.CHECKED_ICON));
                    }
                    getInputTable().setJoinType(JOIN.INNER_JOIN);
                    refreshLabelForJoinDropDown();
                    mapperManager.getUiManager().refreshSqlExpression();
                }
            }

        });
        columnJoin.setModifiable(!mapperManager.componentIsReadOnly());
        // column.setWidth(12);
        columnJoin.setWidth(65);
        columnJoin.setDisplayedValue(""); //$NON-NLS-1$
        // column.setResizable(false);
        CheckboxTableEditorContent checkboxTableEditorContent = new CheckboxTableEditorContent();
        checkboxTableEditorContent.setToolTipText(useInJoinTitle);
        columnJoin.setTableEditorContent(checkboxTableEditorContent);
        columnJoin.setToolTipHeader(useInJoinTitle);

        TableViewerCreatorColumn column = new TableViewerCreatorColumn(tableViewerCreatorForColumns);
        column.setTitle(DataMapTableView.COLUMN_NAME);
        column.setId(DataMapTableView.ID_NAME_COLUMN);
        column.setBeanPropertyAccessors(new IBeanPropertyAccessors<InputColumnTableEntry, String>() {

            public String get(InputColumnTableEntry bean) {
                return bean.getMetadataColumn().getLabel();
            }

            public void set(InputColumnTableEntry bean, String value) {
                bean.getMetadataColumn().setLabel(value);
            }

        });
        column.setWeight(COLUMN_NAME_SIZE_WEIGHT);

        final TableViewerCreatorColumn columnOperator = new TableViewerCreatorColumn(tableViewerCreatorForColumns);
        columnOperator.setTitle(Messages.getString("InputDataMapTableView.columnTitle.Operator")); //$NON-NLS-1$
        columnOperator.setId(DataMapTableView.ID_OPERATOR);
        columnOperator.setToolTipHeader(Messages.getString("InputDataMapTableView.Operator")); //$NON-NLS-1$
        columnOperator.setBeanPropertyAccessors(new IBeanPropertyAccessors<InputColumnTableEntry, String>() {

            public String get(InputColumnTableEntry bean) {
                return bean.getOperator();
            }

            public void set(InputColumnTableEntry bean, String value) {
                bean.setOperator(value);
                mapperManager.getProblemsManager().checkProblemsForTableEntry(bean, true);
            }

        });
        columnOperator.setWidth(85);
        final IDbOperatorManager operatorsManager = mapperManager.getCurrentLanguage().getOperatorsManager();
        IDbOperator[] operators = operatorsManager.getOperators();
        String[] arrayOperators = new String[operators.length + 1];
        arrayOperators[0] = ""; //$NON-NLS-1$
        for (int i = 0; i < operators.length; i++) {
            arrayOperators[i + 1] = operators[i].getOperator();
        }
        final ComboxCellEditorImproved typeComboEditor = new ComboxCellEditorImproved(tableViewerCreatorForColumns.getTable(),
                arrayOperators, SWT.NONE);
        typeComboEditor.addListener(new ICellEditorListener() {

            public void applyEditorValue() {
                ModifiedObjectInfo modifiedObjectInfo = tableViewerCreatorForColumns.getModifiedObjectInfo();
                InputColumnTableEntry currentInputEntry = (InputColumnTableEntry) modifiedObjectInfo.getCurrentModifiedBean();
                currentInputEntry.setOriginalExpression(null);
                CCombo combo = (CCombo) typeComboEditor.getControl();
                String selectedText = combo.getText();
                IDbOperator operatorFromValue = operatorsManager.getOperatorFromValue(selectedText);
                if (operatorFromValue != null && operatorFromValue.isMonoOperand()) {
                    currentInputEntry.setExpression(""); //$NON-NLS-1$
                }
            }

            public void cancelEditor() {
                ModifiedObjectInfo modifiedObjectInfo = tableViewerCreatorForColumns.getModifiedObjectInfo();
                InputColumnTableEntry currentInputEntry = (InputColumnTableEntry) modifiedObjectInfo.getCurrentModifiedBean();
                // currentInputEntry.setExpression(currentInputEntry.getOriginalExpression());
            }

            public void editorValueChanged(boolean oldValidState, boolean newValidState) {
                ModifiedObjectInfo modifiedObjectInfo = tableViewerCreatorForColumns.getModifiedObjectInfo();
                InputColumnTableEntry currentInputEntry = (InputColumnTableEntry) modifiedObjectInfo.getCurrentModifiedBean();
                if (modifiedObjectInfo.getCurrentModifiedColumn() == columnOperator) {

                    if (currentInputEntry != modifiedObjectInfo.getPreviousModifiedBean()) {
                        currentInputEntry.setOriginalExpression(currentInputEntry.getExpression());
                    }
                    CCombo combo = (CCombo) typeComboEditor.getControl();
                    String selectedText = combo.getText();
                    if (!selectedText.equals("") //$NON-NLS-1$
                            && (currentInputEntry.getExpression() == null || currentInputEntry.getExpression().trim().length() == 0)) {
                        IDbOperator operatorFromValue = operatorsManager.getOperatorFromValue(selectedText);
                        if (operatorFromValue.getAssociatedExpression() != null) {
                            currentInputEntry.setExpression(operatorFromValue.getAssociatedExpression());
                        }
                    }

                }
            }

        });
        // typeCombo.setEditable(true);
        columnOperator.setCellEditor(typeComboEditor, CellEditorValueAdapterFactory.getComboAdapterForComboCellEditorImproved());
        columnOperator.setAlignment(ALIGNMENT.CENTER);
        columnOperator.setModifiable(!mapperManager.componentIsReadOnly());
        
        final TableViewerCreatorColumn columnExpression = new TableViewerCreatorColumn(tableViewerCreatorForColumns);
        columnExpression.setTitle(Messages.getString("InputDataMapTableView.columnTitle.Expr")); //$NON-NLS-1$
        columnExpression.setId(DataMapTableView.ID_EXPRESSION_COLUMN);
        columnExpression.setBeanPropertyAccessors(new IBeanPropertyAccessors<InputColumnTableEntry, String>() {

            public String get(InputColumnTableEntry bean) {
                return bean.getExpression();
            }

            public void set(InputColumnTableEntry bean, String value) {
                bean.setExpression(value);
                // mapperManager.getProblemsManager().checkProblemsForTableEntry(bean, true);
            }

        });
        columnExpression.setModifiable(!mapperManager.componentIsReadOnly());
        columnExpression.setDefaultInternalValue(""); //$NON-NLS-1$
        createExpressionCellEditor(tableViewerCreatorForColumns, columnExpression, new Zone[] { Zone.INPUTS }, false);
        columnExpression.setWeight(COLUMN_EXPRESSION_SIZE_WEIGHT);
        columnExpression.setColorProvider(new IColumnColorProvider() {

            public Color getBackgroundColor(Object bean) {
                if (!cellModifier.canModify(bean, columnExpression.getId())) {
                    return READONLY_CELL_BG_COLOR;
                }
                return null;
            }

            public Color getForegroundColor(Object bean) {
                return null;
            }

        });

        configureCellModifier(tableViewerCreatorForColumns);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.dbmap.ui.visualmap.table.DataMapTableView#initTableConstraints()
     */
    @Override
    protected void initTableFilters(Composite parent) {
        // nothing
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.dbmap.ui.visualmap.table.DataMapTableView#addEntriesActionsComponents()
     */
    @Override
    protected boolean addToolItems() {

        // DROP DOWN
        dropDownItem = new ToolItem(toolBarActions, SWT.DROP_DOWN | SWT.BORDER);
        if (WindowSystem.isGTK()) {
            refreshLabelForJoinDropDown();
        }
        dropDownItem.addSelectionListener(new DropDownSelectionListener());
        dropDownItem.setEnabled(!mapperManager.componentIsReadOnly());

        // createActivateFilterCheck();
        createColumnNameFilterCheck();
        return true;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.dbmap.ui.visualmap.table.DataMapTableView#getZone()
     */
    @Override
    public Zone getZone() {
        return Zone.INPUTS;
    }

    public InputTable getInputTable() {
        return (InputTable) abstractDataMapTable;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.dbmap.ui.visualmap.table.DataMapTableView#toolbarNeededToBeRightStyle()
     */
    @Override
    public boolean toolbarNeedToHaveRightStyle() {
        return true;
    }

    /**
     * Listens to widgetSelected() events on SWT.DROP_DOWN type ToolItems and opens/closes a menu when appropriate.
     */
    class DropDownSelectionListener extends SelectionAdapter {

        private boolean visible = false;

        public void widgetSelected(SelectionEvent event) {
            // Create the menu if it has not already been created
            if (menu == null) {
                // Lazy create the menu.
                menu = new Menu(getShell());

                IJoinType[] availableJoins = mapperManager.getCurrentLanguage().getAvailableJoins();

                // TDI-22916 Add by plv:Remove the full outer join for tELTMysqlMap
                if (mapperManager.getComponent().getElementParameter(EParameterName.COMPONENT_NAME.getName()).getValue()
                        .equals("tELTMysqlMap")) {
                    List<IJoinType> joinTypes = new ArrayList<IJoinType>();
                    for (IJoinType joinType : availableJoins) {
                        if (!joinType.equals(JOIN.FULL_OUTER_JOIN)) {
                            joinTypes.add(joinType);
                        }
                    }
                    availableJoins = (IJoinType[]) joinTypes.toArray(new IJoinType[joinTypes.size()]);
                }

                for (int i = 0; i < availableJoins.length; ++i) {
                    final String text = availableJoins[i].getLabel();
                    if (text.length() != 0) {
                        MenuItem menuItem = new MenuItem(menu, SWT.NONE);
                        menuItem.setData(availableJoins[i]);
                        menuItem.setText(text);
                        if (availableJoins[i] == getInputTable().getJoinType()) {
                            menuItem.setImage(ImageProviderMapper.getImage(ImageInfo.CHECKED_ICON));
                        }

                        /*
                         * Add a menu selection listener so that the menu is hidden when the user selects an item from
                         * the drop down menu.
                         */
                        menuItem.addSelectionListener(new SelectionAdapter() {

                            public void widgetSelected(SelectionEvent e) {
                                MenuItem menuItem = (MenuItem) e.widget;
                                MenuItem[] menuItems = menu.getItems();
                                for (int j = 0; j < menuItems.length; j++) {
                                    menuItems[j].setImage(null);
                                }
                                menuItem.setImage(ImageProviderMapper.getImage(ImageInfo.CHECKED_ICON));
                                IJoinType joinType = (IJoinType) menuItem.getData();
                                if (mapperManager.getCurrentLanguage().unuseWithExplicitJoin().contains(joinType)) {
                                    if (getInputTable().getColumnEntries() != null
                                            && !getInputTable().getColumnEntries().isEmpty()) {
                                        Iterator iterator = getInputTable().getColumnEntries().iterator();
                                        while (iterator.hasNext()) {
                                            InputColumnTableEntry inputColumnJoin = (InputColumnTableEntry) iterator.next();
                                            if (inputColumnJoin.isJoin()) {
                                                inputColumnJoin.setJoin(false);
                                            }
                                        }
                                        getTableViewerCreatorForColumns().getTableEditorManager().refresh();
                                    }
                                }
                                getInputTable().setJoinType(joinType);
                                setMenuVisible(false);
                                refreshLabelForJoinDropDown();
                                mapperManager.getUiManager().refreshSqlExpression();
                            }

                        });
                    } else {
                        new MenuItem(menu, SWT.SEPARATOR);
                    }
                }
            }

            /**
             * A selection event will be fired when a drop down tool item is selected in the main area and in the drop
             * down arrow. Examine the event detail to determine where the widget was selected.
             */

            /*
             * The drop down arrow was selected.
             */
            if (visible) {
                // Hide the menu to give the Arrow the appearance of being a toggle button.
                setMenuVisible(false);
            } else {
                // Position the menu below and vertically aligned with the the drop down tool button.
                final ToolItem toolItem = (ToolItem) event.widget;
                final ToolBar toolBar = toolItem.getParent();

                Rectangle toolItemBounds = toolItem.getBounds();
                Point point = toolBar.toDisplay(new Point(toolItemBounds.x, toolItemBounds.y));
                menu.setLocation(point.x, point.y + toolItemBounds.height);
                setMenuVisible(true);
            }
        }

        private void setMenuVisible(boolean visible) {
            menu.setVisible(visible);
            this.visible = visible;
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.dbmap.ui.visualmap.table.DataMapTableView#hasDropDownToolBarItem()
     */
    @Override
    public boolean hasDropDownToolBarItem() {
        return true;
    }

    public void refreshLabelForJoinDropDown() {
        String text = getInputTable().getJoinType().getLabel();
        dropDownItem.setText(text);
        dropDownItem.setToolTipText(text);

        Point sizeToolBar = toolBarActions.computeSize(SWT.DEFAULT, SWT.DEFAULT);
        GridData gridData = (GridData) toolBarActions.getLayoutData();
        if (gridData != null) {
            gridData.widthHint = sizeToolBar.x;
            // gridData.widthHint -= 60;
            toolBarActions.getParent().layout();
        }

    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.dbmap.ui.visualmap.table.DataMapTableView#getTitle()
     */
    @Override
    protected String getTitle() {
        return ((InputTable) abstractDataMapTable).getTitle();
    }

    protected void configureCellModifier(TableViewerCreator<InputColumnTableEntry> tableViewerCreator) {
        cellModifier = new InputTableCellModifier(tableViewerCreator);
        tableViewerCreator.setCellModifier(cellModifier);
    }

    /**
     * . <br/>
     *
     * $Id: MetadataTableEditorView.java 2016 2007-02-12 15:36:11Z amaumont $
     *
     */
    class InputTableCellModifier extends DefaultCellModifier {

        /**
         * DOC amaumont MetadataTableCellModifier constructor comment.
         *
         * @param tableViewerCreator
         */
        public InputTableCellModifier(TableViewerCreator tableViewerCreator) {
            super(tableViewerCreator);
        }

        /*
         * (non-Javadoc)
         *
         * @see org.talend.commons.ui.swt.tableviewer.behavior.DefaultCellModifier#canModify(java.lang.Object,
         * java.lang.String)
         */
        @Override
        public boolean canModify(Object element, String property) {
            TableViewerCreatorColumnNotModifiable columnOperator = getTableViewerCreator()
                    .getColumn(DataMapTableView.ID_OPERATOR);
            String operator = (String) columnOperator.getBeanPropertyAccessors().get(element);
            IDbOperatorManager operatorsManager = mapperManager.getCurrentLanguage().getOperatorsManager();
            IDbOperator operatorFromValue = operatorsManager.getOperatorFromValue(operator);
            boolean columnIsExpression = DataMapTableView.ID_EXPRESSION_COLUMN.equals(property);
            return super.canModify(element, property)
                    && (columnIsExpression && operatorFromValue != null && !operatorFromValue.isMonoOperand()
                            || !columnIsExpression || operatorFromValue == null);
        }

    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.talend.designer.dbmap.ui.visualmap.table.DataMapTableView#getBackgroundCellColor(org.talend.commons.ui.swt
     * .tableviewer.TableViewerCreator, java.lang.Object, int)
     */
    @Override
    protected Color getBackgroundCellColor(TableViewerCreatorNotModifiable tableViewerCreator, Object element, int columnIndex) {
        return getCellColor(tableViewerCreator, element, columnIndex, true);
    }

    /**
     * DOC amaumont Comment method "getCellColor".
     *
     * @param tableViewerCreator
     * @param element
     * @param columnIndex
     * @param isBackground TODO
     * @return
     */
    protected Color getCellColor(TableViewerCreator tableViewerCreator, Object element, int columnIndex, boolean isBackground) {
        ITableEntry entry = (ITableEntry) element;
        TableViewerCreatorColumnNotModifiable column = (TableViewerCreatorColumnNotModifiable) tableViewerCreator.getColumns()
                .get(columnIndex);
        if (column.getId().equals(ID_OPERATOR)) {
            return getExpressionColorProvider().getColor(isBackground, entry.getProblems(), ProblemsManager.KEY_OPERATOR_EMPTY);
        }
        if (column.getId().equals(ID_NAME_COLUMN)) {
            return getExpressionColorProvider().getColor(isBackground, entry.getProblems(), ProblemsManager.KEY_NO_MATCHING);
        }
        return super.getCellColor(tableViewerCreator, element, columnIndex, isBackground);
    }

    public void setEnableJoinTypeDropDown(boolean enabled) {
        dropDownItem.setEnabled(enabled);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.mapper.ui.visualmap.table.DataMapTableView#notifyFocusLost()
     */
    @Override
    public void notifyFocusLost() {
        // if (expressionCellEditor != null) {
        // expressionCellEditor.focusLost();
        // }
    }

}
