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
package org.talend.designer.dbmap.ui.visualmap.table;

import java.util.List;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.ToolItem;
import org.talend.commons.ui.runtime.image.EImage;
import org.talend.commons.ui.runtime.image.ImageProvider;
import org.talend.commons.ui.runtime.swt.tableviewer.TableViewerCreatorColumnNotModifiable;
import org.talend.commons.ui.runtime.swt.tableviewer.TableViewerCreatorNotModifiable.LAYOUT_MODE;
import org.talend.commons.ui.runtime.swt.tableviewer.behavior.DefaultTableLabelProvider;
import org.talend.commons.ui.runtime.swt.tableviewer.behavior.ITableCellValueModifiedListener;
import org.talend.commons.ui.runtime.swt.tableviewer.behavior.TableCellValueModifiedEvent;
import org.talend.commons.ui.runtime.swt.tableviewer.tableeditor.ButtonPushImageTableEditorContent;
import org.talend.commons.ui.runtime.ws.WindowSystem;
import org.talend.commons.ui.swt.advanced.dataeditor.commands.ExtendedTableRemoveCommand;
import org.talend.commons.ui.swt.extended.table.AbstractExtendedTableViewer;
import org.talend.commons.ui.swt.extended.table.ExtendedTableModel;
import org.talend.commons.ui.swt.proposal.ExtendedTextCellEditorWithProposal;
import org.talend.commons.ui.swt.tableviewer.ModifiedBeanEvent;
import org.talend.commons.ui.swt.tableviewer.TableViewerCreator;
import org.talend.commons.ui.swt.tableviewer.TableViewerCreatorColumn;
import org.talend.commons.utils.data.bean.IBeanPropertyAccessors;
import org.talend.core.model.components.IComponent;
import org.talend.designer.abstractmap.model.table.IDataMapTable;
import org.talend.designer.abstractmap.model.tableentry.ITableEntry;
import org.talend.designer.dbmap.i18n.Messages;
import org.talend.designer.dbmap.managers.MapperManager;
import org.talend.designer.dbmap.managers.UIManager;
import org.talend.designer.dbmap.model.table.AbstractDataMapTable;
import org.talend.designer.dbmap.model.table.OutputTable;
import org.talend.designer.dbmap.model.tableentry.FilterTableEntry;
import org.talend.designer.dbmap.model.tableentry.OutputColumnTableEntry;
import org.talend.designer.dbmap.ui.dnd.DragNDrop;
import org.talend.designer.dbmap.ui.image.ImageInfo;
import org.talend.designer.dbmap.ui.image.ImageProviderMapper;
import org.talend.designer.dbmap.ui.visualmap.zone.Zone;

/**
 * DOC amaumont class global comment. Detailled comment <br/>
 *
 * $Id: OutputDataMapTableView.java 1782 2007-02-03 07:57:38Z bqian $
 *
 */
public class OutputDataMapTableView extends DataMapTableView {

    private ToolItem itemDropDown;

    private Menu menu;

    public OutputDataMapTableView(Composite parent, int style, IDataMapTable abstractDataMapTable, MapperManager mapperManager) {
        super(parent, style, abstractDataMapTable, mapperManager);
    }

    @Override
    public void initColumnsOfTableColumns(final TableViewerCreator tableViewerCreatorForColumns) {
        TableViewerCreatorColumn column = new TableViewerCreatorColumn(tableViewerCreatorForColumns);
        column.setTitle(Messages.getString("OutputDataMapTableView.columnTitle.expression")); //$NON-NLS-1$
        column.setId(DataMapTableView.ID_EXPRESSION_COLUMN);
        column.setBeanPropertyAccessors(new IBeanPropertyAccessors<OutputColumnTableEntry, String>() {

            @Override
            public String get(OutputColumnTableEntry bean) {
                return bean.getExpression();
            }

            @Override
            public void set(OutputColumnTableEntry bean, String value) {
                bean.setExpression(value);
                mapperManager.getProblemsManager().checkProblemsForTableEntry(bean, true);
                tableViewerCreatorForColumns.getTableViewer().refresh(bean);
            }

        });
        column.setModifiable(true);
        column.setDefaultInternalValue(""); //$NON-NLS-1$
        createExpressionCellEditor(tableViewerCreatorForColumns, column, new Zone[] { Zone.INPUTS, Zone.OUTPUTS }, false);
        column.setWeight(COLUMN_EXPRESSION_SIZE_WEIGHT);

        column = new TableViewerCreatorColumn(tableViewerCreatorForColumns);
        column.setTitle(DataMapTableView.COLUMN_NAME);
        column.setId(DataMapTableView.ID_NAME_COLUMN);
        column.setBeanPropertyAccessors(new IBeanPropertyAccessors<OutputColumnTableEntry, String>() {

            @Override
            public String get(OutputColumnTableEntry bean) {
                return bean.getMetadataColumn().getLabel();
            }

            @Override
            public void set(OutputColumnTableEntry bean, String value) {
                bean.getMetadataColumn().setLabel(value);
            }

        });
        column.setWeight(COLUMN_NAME_SIZE_WEIGHT);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.dbmap.ui.visualmap.table.DataMapTableView#initTableConstraints()
     */
    @Override
    protected void initTableFilters(Composite parent) {
        /**
         * create whereFiltersTable
         */
        // createFiltersTable(parent);
        filterColumnLabel = Messages.getString("OutputDataMapTableView.columnTitle.AdditionalWhereClauses"); //$NON-NLS-1$
        extendedTableViewerForFilters = new ExtendedTableViewerForFilters(
                ((OutputTable) abstractDataMapTable).getWhereTableFiltersEntriesModel(), parent) {

            /*
             * (non-Javadoc)
             *
             * @see org.talend.designer.dbmap.ui.visualmap.table.OutputDataMapTableView.ExtendedTableViewerForFilters#
             * getTableConstraintSize()
             */
            @Override
            protected int getTableConstraintSize() {
                int retSize = 0;
                if (abstractDataMapTable instanceof OutputTable) {
                    retSize = ((OutputTable) abstractDataMapTable).getWhereFilterEntries().size();
                }
                return retSize;
            }

            @Override
            protected void setTextCellEditor(ExtendedTextCellEditorWithProposal textEditor) {
                if (textEditor != null) {
                    whereConstraintExpressionTextEditor = textEditor.getTextControl();
                }
            }
        };

        // correct partially layout problem with GTK when cell editor value is applied
        ((ExtendedTableViewerForFilters) extendedTableViewerForFilters).createFiltersTable();
        TableViewerCreator tableViewerCreatorForWhereClauses = extendedTableViewerForFilters.getTableViewerCreator();
        tableViewerCreatorForWhereClauses.setAdjustWidthValue(WindowSystem.isGTK() ? -20 : ADJUST_WIDTH_VALUE);
        List<FilterTableEntry> whereEntries = ((OutputTable) getDataMapTable()).getWhereFilterEntries();
        tableViewerCreatorForWhereClauses.init(whereEntries);

        /**
         * create otherFiltersTable
         */
        IComponent component = getMapperManager().getComponent().getComponent();
        if (component != null && "tELTOracleMap".equals(component.getName())) { //$NON-NLS-1$
            filterColumnLabel = Messages.getString("OutputDataMapTableView.columnTitle.AdditionalOtherClauses.oracle"); //$NON-NLS-1$
        } else {
            filterColumnLabel = Messages.getString("OutputDataMapTableView.columnTitle.AdditionalOtherClauses"); //$NON-NLS-1$
        }
        entendedTableViewerForOtherClauses = new ExtendedTableViewerForFilters(
                ((OutputTable) abstractDataMapTable).getOtherTableFiltersEntriesModel(), parent) {

            /*
             * (non-Javadoc)
             *
             * @see org.talend.designer.dbmap.ui.visualmap.table.OutputDataMapTableView.ExtendedTableViewerForFilters#
             * getTableConstraintSize()
             */
            @Override
            public int getTableConstraintSize() {
                int retSize = 0;
                if (abstractDataMapTable instanceof OutputTable) {
                    retSize = ((OutputTable) abstractDataMapTable).getOtherFilterEntries().size();
                }
                return retSize;
            }

            @Override
            protected void setTextCellEditor(ExtendedTextCellEditorWithProposal textEditor) {
                if (textEditor != null) {
                    otherConstraintExpressionTextEditor = textEditor.getTextControl();
                }
            }
        };
        ((ExtendedTableViewerForFilters) entendedTableViewerForOtherClauses).createFiltersTable();
        TableViewerCreator tableViewerCreatorForOtherClauses = entendedTableViewerForOtherClauses.getTableViewerCreator();
        tableViewerCreatorForOtherClauses.setAdjustWidthValue(WindowSystem.isGTK() ? -20 : ADJUST_WIDTH_VALUE);
        List<FilterTableEntry> otherEntries = ((OutputTable) getDataMapTable()).getOtherFilterEntries();
        tableViewerCreatorForOtherClauses.init(otherEntries);
        // updateGridDataHeightForTableConstraints(entendedTableViewerForOtherClauses.getTable());

        updateGridDataHeightForTableConstraints();
        if (WindowSystem.isGTK()) {
            tableViewerCreatorForWhereClauses.layout();
            entendedTableViewerForOtherClauses.getTableViewerCreator().layout();
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.dbmap.ui.visualmap.table.DataMapTableView#addEntriesActionsComponents()
     */
    @Override
    protected boolean addToolItems() {
        // createFiltersToolItems();
        createFiltersDropDown();
        // addToolItemSeparator();
        // createToolItems();
        return true;
    }

    private void createFiltersDropDown() {
        // itemDropDown = new ToolItem(toolBarActions, SWT.PUSH);
        itemDropDown = new ToolItem(toolBarActions, SWT.ARROW);
        itemDropDown.setToolTipText(Messages.getString("DataMapTableView.buttonTooltip.addFilterRow")); //$NON-NLS-1$
        itemDropDown.setImage(ImageProviderMapper.getImage(ImageInfo.ADD_FILTER_ICON));
        menu = new Menu(this);
        itemDropDown.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                Rectangle bounds = itemDropDown.getBounds();
                Point point = toolBarActions.toDisplay(bounds.x, bounds.y + bounds.height);
                menu.setLocation(point);
                menu.setVisible(true);
            }

        });
        MenuItem addWhereClauses = new MenuItem(menu, SWT.PUSH);
        addWhereClauses.setText(Messages.getString("DataMapTableView.buttonTooltip.menu.addWhereClauses")); //$NON-NLS-1$
        addWhereClauses.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {

                onFiltersToolItemsSelected(e, extendedTableViewerForFilters.getTableViewerCreator(),
                        FilterTableEntry.WHERE_FILTER);

            }
        });

        MenuItem addOtherClauses = new MenuItem(menu, SWT.PUSH);
        addOtherClauses.setText(Messages.getString("DataMapTableView.buttonTooltip.menu.addOtherClauses")); //$NON-NLS-1$
        addOtherClauses.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {

                onFiltersToolItemsSelected(e, entendedTableViewerForOtherClauses.getTableViewerCreator(),
                        FilterTableEntry.OTHER_FILTER);

            }
        });
    }

    @Override
    public void updateGridDataHeightForTableConstraints() {
        super.updateGridDataHeightForTableConstraints();
        if (entendedTableViewerForOtherClauses != null) {
            updateGridDataHeightForTableConstraints(entendedTableViewerForOtherClauses.getTable());
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.dbmap.ui.visualmap.table.DataMapTableView#getZone()
     */
    @Override
    public Zone getZone() {
        return Zone.OUTPUTS;
    }

    @Override
    public void unselectAllConstraintEntries() {
        extendedTableViewerForFilters.getTableViewerCreator().getSelectionHelper().deselectAll();
        entendedTableViewerForOtherClauses.getTableViewerCreator().getSelectionHelper().deselectAll();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.dbmap.ui.visualmap.table.DataMapTableView#toolbarNeededToBeRightStyle()
     */
    @Override
    public boolean toolbarNeedToHaveRightStyle() {
        return false;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.dbmap.ui.visualmap.table.DataMapTableView#hasDropDownToolBarItem()
     */
    @Override
    public boolean hasDropDownToolBarItem() {
        return false;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.dbmap.ui.visualmap.table.DataMapTableView#getTitle()
     */
    @Override
    protected String getTitle() {
        return ((AbstractDataMapTable) abstractDataMapTable).getTitle();
    }

    @Override
    protected void onOutputTableFiltersModified(ModifiedBeanEvent<FilterTableEntry> event,
            AbstractExtendedTableViewer<FilterTableEntry> _extendedTableViewerForFilters) {
        super.onOutputTableFiltersModified(event, _extendedTableViewerForFilters);

        TableViewerCreator tableViewerCreator = entendedTableViewerForOtherClauses.getTableViewerCreator();
        ITableEntry tableEntry = event.bean;

        parseExpressionIfNeeded(event, tableViewerCreator, tableEntry);
    }

    protected abstract class ExtendedTableViewerForFilters extends AbstractExtendedTableViewer<FilterTableEntry> {

        /**
         * DOC cmeng ExtendedTableViewerForFilters constructor comment.
         *
         * @param extendedTableModel
         * @param parent
         */
        public ExtendedTableViewerForFilters(ExtendedTableModel<FilterTableEntry> extendedTableModel, Composite parent) {
            super(extendedTableModel, parent);
        }

        @Override
        protected void createColumns(TableViewerCreator<FilterTableEntry> tableViewerCreator, Table table) {
            createFiltersColumns(tableViewerCreator);
        }

        protected abstract int getTableConstraintSize();

        protected abstract void setTextCellEditor(ExtendedTextCellEditorWithProposal textEditor);

        /*
         * (non-Javadoc)
         *
         * @see org.talend.commons.ui.swt.extended.macrotable.AbstractExtendedTableViewer#setTableViewerCreatorOptions
         * (org.talend.commons.ui.swt.tableviewer.TableViewerCreator)
         */
        @Override
        protected void setTableViewerCreatorOptions(TableViewerCreator<FilterTableEntry> newTableViewerCreator) {
            super.setTableViewerCreatorOptions(newTableViewerCreator);
            newTableViewerCreator.setHeaderVisible(true);
            newTableViewerCreator.setColumnsResizableByDefault(false);
            newTableViewerCreator.setBorderVisible(false);
            newTableViewerCreator.setLayoutMode(LAYOUT_MODE.FILL_HORIZONTAL);
            newTableViewerCreator.setKeyboardManagementForCellEdition(false);
            // tableViewerCreatorForColumns.setUseCustomItemColoring(this.getDataMapTable() instanceof
            // AbstractInOutTable);
            newTableViewerCreator.setFirstColumnMasked(true);

        }

        public void createFiltersColumns(final TableViewerCreator<FilterTableEntry> tableViewerCreatorForFilters) {
            TableViewerCreatorColumn column = new TableViewerCreatorColumn(tableViewerCreatorForFilters);
            column.setTitle(filterColumnLabel);
            column.setId(DataMapTableView.ID_EXPRESSION_COLUMN);
            column.setBeanPropertyAccessors(new IBeanPropertyAccessors<FilterTableEntry, String>() {

                @Override
                public String get(FilterTableEntry bean) {
                    return bean.getExpression();
                }

                @Override
                public void set(FilterTableEntry bean, String value) {
                    bean.setExpression(value);
                    mapperManager.getProblemsManager().checkProblemsForTableEntry(bean, true);
                }

            });
            column.setModifiable(true);
            column.setDefaultInternalValue(""); //$NON-NLS-1$
            setTextCellEditor(createExpressionCellEditor(tableViewerCreatorForFilters, column, new Zone[] { Zone.INPUTS,
                    Zone.OUTPUTS }, true));
            column.setWeight(99);
            column.setMoveable(false);
            column.setResizable(false);

            // Column with remove button
            column = new TableViewerCreatorColumn(tableViewerCreatorForFilters);
            column.setTitle(""); //$NON-NLS-1$
            column.setDefaultDisplayedValue(""); //$NON-NLS-1$
            column.setWidth(16);
            column.setMoveable(false);
            column.setResizable(false);
            ButtonPushImageTableEditorContent buttonImage = new ButtonPushImageTableEditorContent() {

                /*
                 * (non-Javadoc)
                 *
                 * @see
                 * org.talend.commons.ui.swt.tableviewer.tableeditor.ButtonImageTableEditorContent#selectionEvent(org
                 * .talend .commons.ui.swt.tableviewer.TableViewerCreatorColumn, java.lang.Object)
                 */
                @Override
                protected void selectionEvent(TableViewerCreatorColumnNotModifiable column, Object bean) {
                    ExtendedTableRemoveCommand removeCommand = new ExtendedTableRemoveCommand(bean,
                            ExtendedTableViewerForFilters.this.getExtendedTableModel());
                    mapperManager.removeTableEntry((ITableEntry) bean);
                    mapperManager.executeCommand(removeCommand);
                    tableViewerCreatorForFilters.getTableViewer().refresh();
                    List list = tableViewerCreatorForFilters.getInputList();
                    updateGridDataHeightForTableConstraints(getTable());
                    if (list != null && list.size() == 0) {
                        showTableConstraints(false, getTableViewerCreator());
                    } else {
                        showTableConstraints(true, getTableViewerCreator());
                    }

                }

            };
            buttonImage.setImage(ImageProvider.getImage(EImage.MINUS_ICON));
            column.setTableEditorContent(buttonImage);

        }

        public void createFiltersTable() {

            final TableViewerCreator _tableViewerCreatorForFilters = getTableViewerCreator();
            setCommandStack(mapperManager.getCommandStack());

            Table _tableForConstraints = _tableViewerCreatorForFilters.getTable();
            GridData _tableForConstraintsGridData = new GridData(GridData.FILL_HORIZONTAL);
            _tableForConstraints.setLayoutData(_tableForConstraintsGridData);

            boolean tableConstraintsVisible = 0 < getTableConstraintSize();

            _tableForConstraintsGridData.exclude = !tableConstraintsVisible;
            _tableForConstraints.setVisible(tableConstraintsVisible);

            new DragNDrop(mapperManager, _tableForConstraints, false, true);

            _tableViewerCreatorForFilters.addCellValueModifiedListener(new ITableCellValueModifiedListener() {

                @Override
                public void cellValueModified(TableCellValueModifiedEvent e) {
                    unselectAllEntriesIfErrorDetected(e);
                }
            });

            final TableViewer tableViewer = _tableViewerCreatorForFilters.getTableViewer();

            tableViewer.addSelectionChangedListener(new ISelectionChangedListener() {

                @Override
                public void selectionChanged(SelectionChangedEvent event) {
                    ISelection selection = event.getSelection();
                    selectThisDataMapTableView();
                    UIManager uiManager = mapperManager.getUiManager();
                    uiManager.selectLinks(OutputDataMapTableView.this, uiManager.extractSelectedTableEntries(selection), true,
                            false);
                }

            });

            _tableForConstraints.addListener(SWT.KeyDown, new Listener() {

                @Override
                public void handleEvent(Event event) {
                    processEnterKeyDown(_tableViewerCreatorForFilters, event);
                }

            });

            _tableViewerCreatorForFilters.setLabelProvider(new DefaultTableLabelProvider(_tableViewerCreatorForFilters) {

                @Override
                public Color getBackground(Object element, int columnIndex) {
                    return getBackgroundCellColor(tableViewerCreator, element, columnIndex);
                }

                @Override
                public Color getForeground(Object element, int columnIndex) {
                    return getForegroundCellColor(tableViewerCreator, element, columnIndex);
                }

            });

            initShowMessageErrorListener(_tableForConstraints);
        }
    }
}
