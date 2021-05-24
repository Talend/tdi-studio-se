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
package org.talend.designer.mapper.ui.visualmap.table;

import java.util.List;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ComboBoxCellEditor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.talend.commons.ui.runtime.image.EImage;
import org.talend.commons.ui.runtime.image.ImageProvider;
import org.talend.commons.ui.runtime.swt.tableviewer.TableViewerCreatorColumnNotModifiable;
import org.talend.commons.ui.runtime.swt.tableviewer.TableViewerCreatorNotModifiable.LAYOUT_MODE;
import org.talend.commons.ui.runtime.swt.tableviewer.behavior.DefaultTableLabelProvider;
import org.talend.commons.ui.runtime.swt.tableviewer.behavior.ITableCellValueModifiedListener;
import org.talend.commons.ui.runtime.swt.tableviewer.behavior.TableCellValueModifiedEvent;
import org.talend.commons.ui.runtime.swt.tableviewer.celleditor.ExtendedSimpleTextCellEditor;
import org.talend.commons.ui.runtime.swt.tableviewer.celleditor.ExtendedTextCellEditor;
import org.talend.commons.ui.runtime.swt.tableviewer.data.ModifiedObjectInfo;
import org.talend.commons.ui.runtime.swt.tableviewer.tableeditor.ButtonPushImageTableEditorContent;
import org.talend.commons.ui.runtime.ws.WindowSystem;
import org.talend.commons.ui.swt.advanced.dataeditor.commands.ExtendedTableRemoveCommand;
import org.talend.commons.ui.swt.extended.table.AbstractExtendedTableViewer;
import org.talend.commons.ui.swt.extended.table.ExtendedTableModel;
import org.talend.commons.ui.swt.tableviewer.TableViewerCreator;
import org.talend.commons.ui.swt.tableviewer.TableViewerCreator.CELL_EDITOR_STATE;
import org.talend.commons.ui.swt.tableviewer.TableViewerCreatorColumn;
import org.talend.commons.ui.swt.tableviewer.behavior.DefaultCellModifier;
import org.talend.commons.ui.swt.tableviewer.celleditor.DialogErrorForCellEditorListener;
import org.talend.commons.utils.data.bean.IBeanPropertyAccessors;
import org.talend.core.PluginChecker;
import org.talend.core.model.metadata.IMetadataColumn;
import org.talend.core.model.process.TraceData;
import org.talend.designer.abstractmap.model.table.IDataMapTable;
import org.talend.designer.abstractmap.model.tableentry.ITableEntry;
import org.talend.designer.mapper.external.connection.IOConnection;
import org.talend.designer.mapper.i18n.Messages;
import org.talend.designer.mapper.managers.MapperManager;
import org.talend.designer.mapper.managers.UIManager;
import org.talend.designer.mapper.model.table.AbstractInOutTable;
import org.talend.designer.mapper.model.table.OutputTable;
import org.talend.designer.mapper.model.table.VarsTable;
import org.talend.designer.mapper.model.tableentry.FilterTableEntry;
import org.talend.designer.mapper.model.tableentry.GlobalMapEntry;
import org.talend.designer.mapper.model.tableentry.OutputColumnTableEntry;
import org.talend.designer.mapper.ui.dnd.DragNDrop;
import org.talend.designer.mapper.ui.image.ImageProviderMapper;
import org.talend.designer.mapper.ui.visualmap.zone.Zone;

/**
 * DOC amaumont class global comment. Detailled comment <br/>
 *
 * $Id$
 *
 */

public class OutputDataMapTableView extends DataMapTableView {

    private OutputTableCellModifier cellModifier;

    private ExtendedTextCellEditor expressionCellEditor;

    public OutputDataMapTableView(Composite parent, int style, IDataMapTable abstractDataMapTable, MapperManager mapperManager) {
        super(parent, style, abstractDataMapTable, mapperManager);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.mapper.ui.visualmap.table.DataMapTableView#createContent()
     */
    @Override
    protected void createContent() {
        createTableForColumns();
    }

    @Override
    protected void createMapSettingTable() {

        ExtendedTableModel<GlobalMapEntry> tableMapSettingEntriesModel = ((OutputTable) abstractDataMapTable)
                .getTableMapSettingEntriesModel();

        extendedTableViewerForMapSetting = new AbstractExtendedTableViewer<GlobalMapEntry>(tableMapSettingEntriesModel,
                centerComposite) {

            @Override
            protected void createColumns(TableViewerCreator<GlobalMapEntry> tableViewerCreator, Table table) {
                initMapSettingColumns(tableViewerCreator);
            }

            @Override
            protected void setTableViewerCreatorOptions(TableViewerCreator<GlobalMapEntry> newTableViewerCreator) {
                super.setTableViewerCreatorOptions(newTableViewerCreator);
                newTableViewerCreator.setBorderVisible(false);
            }
        };

        if (tableMapSettingEntriesModel != null) {
            tableMapSettingEntriesModel.add(new GlobalMapEntry(abstractDataMapTable, OUTPUT_REJECT, null));
            tableMapSettingEntriesModel.add(new GlobalMapEntry(abstractDataMapTable, LOOK_UP_INNER_JOIN_REJECT, null));
            if (getOutputTable().getIsJoinTableOf() == null) {
                tableMapSettingEntriesModel.add(new GlobalMapEntry(abstractDataMapTable, SCHEMA_TYPE, null));
                if (getOutputTable().isRepository()) {
                    tableMapSettingEntriesModel.add(new GlobalMapEntry(abstractDataMapTable, SCHEMA_ID, null));
                }
            }
        }

        mapSettingViewerCreator = extendedTableViewerForMapSetting.getTableViewerCreator();
        mapSettingTable = extendedTableViewerForMapSetting.getTable();
        tableForMapSettingGridData = new GridData(SWT.FILL, SWT.FILL, true, true, 3, 1);
        mapSettingTable.setLayoutData(tableForMapSettingGridData);
        mapSettingTable.setHeaderVisible(true);
        mapSettingTable.setLinesVisible(true);

        boolean mappingSettingVisible = false;
        if (abstractDataMapTable instanceof AbstractInOutTable) {
            mappingSettingVisible = ((AbstractInOutTable) abstractDataMapTable).isActivateCondensedTool();
        }
        tableForMapSettingGridData.exclude = !mappingSettingVisible;
        mapSettingTable.setVisible(mappingSettingVisible);
        mapSettingViewerCreator.getTableViewer().setSelection(null);
        mapSettingTable.addFocusListener(new FocusListener() {

            public void focusLost(FocusEvent e) {
                mapSettingViewerCreator.getTableViewer().setSelection(null);
            }

            public void focusGained(FocusEvent e) {
            }
        });

        final TableViewer mapSettingTableViewer = mapSettingViewerCreator.getTableViewer();
        mapSettingTableViewer.addSelectionChangedListener(new ISelectionChangedListener() {

            public void selectionChanged(SelectionChangedEvent event) {
                selectThisDataMapTableView();
            }

        });
    }

    @Override
    protected IBeanPropertyAccessors<GlobalMapEntry, Object> getMapSettingValueAccess(final CellEditor cellEditor) {
        return new IBeanPropertyAccessors<GlobalMapEntry, Object>() {

            public Object get(GlobalMapEntry bean) {
                IDataMapTable parent = bean.getParent();
                OutputTable outputTable = (OutputTable) parent;
                if (cellEditor instanceof ComboBoxCellEditor) {
                    ComboBoxCellEditor functComboBox = (ComboBoxCellEditor) cellEditor;
                    functComboBox.setItems(new String[] { "true", "false" });
                    if (OUTPUT_REJECT.equals(bean.getName())) {
                        return String.valueOf(outputTable.isReject());
                    } else if (LOOK_UP_INNER_JOIN_REJECT.equals(bean.getName())) {
                        return String.valueOf(outputTable.isRejectInnerJoin());
                    } else if (SCHEMA_TYPE.equals(bean.getName())) {
                        functComboBox.setItems(new String[] { BUILT_IN, REPOSITORY });
                        return outputTable.isRepository() ? REPOSITORY : BUILT_IN;
                    }
                } else if (cellEditor instanceof CustomDialogCellEditor) {
                    CustomDialogCellEditor customDialogCellEditor = (CustomDialogCellEditor) cellEditor;
                    if (OUTPUT_REJECT.equals(bean.getName())) {
                        customDialogCellEditor.setType(CellValueType.BOOL);
                        return String.valueOf(outputTable.isReject());
                    } else if (LOOK_UP_INNER_JOIN_REJECT.equals(bean.getName())) {
                        customDialogCellEditor.setType(CellValueType.BOOL);
                        return String.valueOf(outputTable.isRejectInnerJoin());
                    } else if (SCHEMA_TYPE.equals(bean.getName())) {
                        customDialogCellEditor.setType(CellValueType.SCHEMA_TYPE);
                        enableDiaplayViewer(outputTable.isRepository());
                        return outputTable.isRepository() ? REPOSITORY : BUILT_IN;
                    } else if (SCHEMA_ID.equals(bean.getName())) {
                        customDialogCellEditor.setType(CellValueType.SCHEMA_ID);
                        return getSchemaDisplayName(outputTable.getId());
                    }
                }

                return "";
            }

            public void set(GlobalMapEntry bean, Object value) {
                if (value == null) {
                    return;
                }
                IDataMapTable parent = bean.getParent();
                OutputTable outputTable = (OutputTable) parent;
                if (OUTPUT_REJECT.equals(bean.getName())) {
                    outputTable.setReject(Boolean.valueOf(value.toString()));
                } else if (LOOK_UP_INNER_JOIN_REJECT.equals(bean.getName())) {
                    outputTable.setRejectInnerJoin(Boolean.valueOf(value.toString()));
                } else if (SCHEMA_TYPE.equals(bean.getName())) {
                    outputTable.setRepository(REPOSITORY.equals(value));
                    showSchemaIDSetting(REPOSITORY.equals(value));
                    enableDiaplayViewer(REPOSITORY.equals(value));
                } else if (SCHEMA_ID.equals(bean.getName())) {
                    outputTable.setId(String.valueOf(value));
                }

                refreshCondensedImage(outputTable, bean.getName());
            }
        };
    }

    @Override
    protected void refreshCondensedImage(AbstractInOutTable absTable, String option) {
        OutputTable table = (OutputTable) absTable;
        if (OUTPUT_REJECT.equals(option)) {
            if (mapperManager.getDefaultSetting().get(OUTPUT_REJECT).equals(table.isReject())) {
                if (changedOptions > 0) {
                    changedOptions--;
                }
            } else {
                if (changedOptions < 6) {
                    changedOptions++;
                }
            }
        } else if (LOOK_UP_INNER_JOIN_REJECT.equals(option)) {
            if (mapperManager.getDefaultSetting().get(LOOK_UP_INNER_JOIN_REJECT).equals(table.isRejectInnerJoin())) {
                if (changedOptions > 0) {
                    changedOptions--;
                }
            } else {
                if (changedOptions < 6) {
                    changedOptions++;
                }
            }
        } else if (SCHEMA_TYPE.equals(option)) {
            if (mapperManager.getDefaultSetting().get(SCHEMA_TYPE).equals(table.isRepository())) {
                if (changedOptions > 0) {
                    changedOptions--;
                }
            } else {
                if (changedOptions < 6) {
                    changedOptions++;
                }
            }
        } else if (SCHEMA_ID.equals(option)) {
            if (mapperManager.getDefaultSetting().get(SCHEMA_ID) == table.getId()) {
                if (changedOptions > 0) {
                    changedOptions--;
                }
            } else {
                if (changedOptions < 6) {
                    changedOptions++;
                }
            }
        }
        condensedItem.setImage(ImageProviderMapper.getImage(getCondencedItemImage(changedOptions)));
    }

    @Override
    protected boolean needColumnBgColor(GlobalMapEntry bean) {
        OutputTable outputTable = (OutputTable) bean.getParent();

        if (OUTPUT_REJECT.equals(bean.getName())) {
            if (!mapperManager.getDefaultSetting().get(OUTPUT_REJECT).equals(outputTable.isReject())) {
                return true;
            }
        } else if (LOOK_UP_INNER_JOIN_REJECT.equals(bean.getName())) {
            if (!mapperManager.getDefaultSetting().get(LOOK_UP_INNER_JOIN_REJECT).equals(outputTable.isRejectInnerJoin())) {
                return true;
            }
        } else if (SCHEMA_TYPE.equals(bean.getName())) {
            if (!mapperManager.getDefaultSetting().get(SCHEMA_TYPE).equals(outputTable.isRepository())) {
                return true;
            }
        } else if (SCHEMA_ID.equals(bean.getName())) {
            if (mapperManager.getDefaultSetting().get(SCHEMA_ID) != outputTable.getId()) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected void initCondensedItemImage() {
        if (!mapperManager.getDefaultSetting().get(OUTPUT_REJECT).equals(getOutputTable().isReject())) {
            if (changedOptions < 4) {
                changedOptions++;
            }
        }
        if (!mapperManager.getDefaultSetting().get(LOOK_UP_INNER_JOIN_REJECT).equals(getOutputTable().isRejectInnerJoin())) {
            if (changedOptions < 4) {
                changedOptions++;
            }
        }
        if (!mapperManager.getDefaultSetting().get(SCHEMA_TYPE).equals(getOutputTable().isRepository())) {
            if (changedOptions < 4) {
                changedOptions++;
            }
        }
        if (mapperManager.getDefaultSetting().get(SCHEMA_ID) != getOutputTable().getId()) {
            if (changedOptions < 4) {
                changedOptions++;
            }
        }

        condensedItem.setImage(ImageProviderMapper.getImage(getCondencedItemImage(changedOptions)));

    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.mapper.ui.visualmap.table.DataMapTableView#notifyFocusLost()
     */
    @Override
    public void notifyFocusLost() {
         //if (expressionCellEditor != null) {
            expressionCellEditor.focusLost();
         //}
    }

    @Override
    public void initColumnsOfTableColumns(final TableViewerCreator tableViewerCreatorForColumns) {
        IOConnection connection = ((OutputTable) getDataMapTable()).getConnection();
        TableViewerCreatorColumn column = new TableViewerCreatorColumn(tableViewerCreatorForColumns);
        column.setTitle(Messages.getString("OutputDataMapTableView.columnTitle.expression")); //$NON-NLS-1$
        column.setId(DataMapTableView.ID_EXPRESSION_COLUMN);
        expressionCellEditor = createExpressionCellEditor(tableViewerCreatorForColumns, column, new Zone[] { Zone.INPUTS,
                Zone.VARS }, false);
        column.setBeanPropertyAccessors(new IBeanPropertyAccessors<OutputColumnTableEntry, String>() {

            public String get(OutputColumnTableEntry bean) {
                return bean.getExpression();
            }

            public void set(OutputColumnTableEntry bean, String value) {
                bean.setExpression(value);
                mapperManager.getProblemsManager().checkProblemsForTableEntry(bean, true);
                tableViewerCreatorForColumns.getTableViewer().refresh(bean);
            }

        });
        column.setModifiable(!mapperManager.componentIsReadOnly());
        column.setDefaultInternalValue(""); //$NON-NLS-1$
        column.setWeight(COLUMN_EXPRESSION_SIZE_WEIGHT);

        column = new TableViewerCreatorColumn(tableViewerCreatorForColumns);
        column.setTitle(DataMapTableView.COLUMN_NAME);
        column.setId(DataMapTableView.ID_NAME_COLUMN);
        column.setBeanPropertyAccessors(new IBeanPropertyAccessors<OutputColumnTableEntry, String>() {

            public String get(OutputColumnTableEntry bean) {
                return bean.getMetadataColumn().getLabel();
            }

            public void set(OutputColumnTableEntry bean, String value) {
                bean.getMetadataColumn().setLabel(value);
            }

        });
        column.setWeight(COLUMN_NAME_SIZE_WEIGHT);

        if (PluginChecker.isTraceDebugPluginLoaded() && mapperManager.isTracesActive() && connection != null) {
            column = new TableViewerCreatorColumn(tableViewerCreatorForColumns);
            column.setTitle("Preview");
            column.setId(DataMapTableView.PREVIEW_COLUMN);
            column.setWeight(COLUMN_NAME_SIZE_WEIGHT);
            column.setBeanPropertyAccessors(new IBeanPropertyAccessors<OutputColumnTableEntry, String>() {

                public String get(OutputColumnTableEntry bean) {
                    IMetadataColumn metadataColumn = bean.getMetadataColumn();
                    if (metadataColumn != null) {
                        String label = metadataColumn.getLabel();
                        TraceData preview = bean.getPreviewValue();
                        if (preview != null && preview.getData() != null) {
                            return preview.getData().get(label);
                        }

                    }
                    return "";
                }

                public void set(OutputColumnTableEntry bean, String value) {
                    // do nothing
                }

            });

        }

        configureCellModifier(tableViewerCreatorForColumns);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.mapper.ui.visualmap.table.DataMapTableView#initTableConstraints()
     */
    @Override
    protected void initExtraTable() {
        createFiltersTable();

        List<FilterTableEntry> entries = ((OutputTable) getDataMapTable()).getFilterEntries();

        // correct partially layout problem with GTK when cell editor value is
        // applied
        tableViewerCreatorForFilters.setAdjustWidthValue(WindowSystem.isGTK() ? -20 : ADJUST_WIDTH_VALUE);

        tableViewerCreatorForFilters.init(entries);
        updateGridDataHeightForTableConstraints();

    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.mapper.ui.visualmap.table.DataMapTableView#addEntriesActionsComponents()
     */
    @Override
    protected boolean addToolItems() {
        createFiltersToolItems();
        // addToolItemSeparator();
        // createToolItems();
        return true;
    }

    protected void createFiltersTable() {

        this.extendedTableViewerForFilters = new AbstractExtendedTableViewer<FilterTableEntry>(
                ((OutputTable) abstractDataMapTable).getTableFiltersEntriesModel(), centerComposite) {

            @Override
            protected void createColumns(TableViewerCreator<FilterTableEntry> tableViewerCreator, Table table) {
                createFiltersColumns(tableViewerCreator);
            }

            /*
             * (non-Javadoc)
             *
             * @see
             * org.talend.commons.ui.swt.extended.macrotable.AbstractExtendedTableViewer#setTableViewerCreatorOptions
             * (org.talend.commons.ui.swt.tableviewer.TableViewerCreator)
             */
            @Override
            protected void setTableViewerCreatorOptions(TableViewerCreator<FilterTableEntry> newTableViewerCreator) {
                super.setTableViewerCreatorOptions(newTableViewerCreator);
                newTableViewerCreator.setColumnsResizableByDefault(false);
                newTableViewerCreator.setBorderVisible(false);
                newTableViewerCreator.setLayoutMode(LAYOUT_MODE.FILL_HORIZONTAL);
                newTableViewerCreator.setKeyboardManagementForCellEdition(true);
                // tableViewerCreatorForColumns.setUseCustomItemColoring(this.getDataMapTable()
                // instanceof
                // AbstractInOutTable);
                newTableViewerCreator.setFirstColumnMasked(true);

            }

        };
        tableViewerCreatorForFilters = this.extendedTableViewerForFilters.getTableViewerCreator();
        this.extendedTableViewerForFilters.setCommandStack(mapperManager.getCommandStack());

        tableForConstraints = tableViewerCreatorForFilters.getTable();
        tableForConstraintsGridData = new GridData(GridData.FILL_HORIZONTAL);
        tableForConstraints.setLayoutData(tableForConstraintsGridData);

        boolean tableConstraintsVisible = false;
        if (abstractDataMapTable instanceof OutputTable) {
            tableConstraintsVisible = ((OutputTable) abstractDataMapTable).getFilterEntries().size() > 0;
        }

        tableForConstraintsGridData.exclude = !tableConstraintsVisible;
        tableForConstraints.setVisible(tableConstraintsVisible);

        if (!mapperManager.componentIsReadOnly()) {
            new DragNDrop(mapperManager, tableForConstraints, false, true);
        }

        tableViewerCreatorForFilters.addCellValueModifiedListener(new ITableCellValueModifiedListener() {

            public void cellValueModified(TableCellValueModifiedEvent e) {
                unselectAllEntriesIfErrorDetected(e);
            }
        });

        final TableViewer tableViewer = tableViewerCreatorForFilters.getTableViewer();

        tableViewer.addSelectionChangedListener(new ISelectionChangedListener() {

            public void selectionChanged(SelectionChangedEvent event) {
                ISelection selection = event.getSelection();
                selectThisDataMapTableView();
                UIManager uiManager = mapperManager.getUiManager();
                uiManager.selectLinks(OutputDataMapTableView.this, uiManager.extractSelectedTableEntries(selection), false, false);
            }

        });

        tableForConstraints.addListener(SWT.KeyDown, new Listener() {

            public void handleEvent(Event event) {
                processEnterKeyDown(tableViewerCreatorForFilters, event);
            }

        });

        tableViewerCreatorForFilters.setLabelProvider(new DefaultTableLabelProvider(tableViewerCreatorForFilters) {

            @Override
            public Color getBackground(Object element, int columnIndex) {
                return getBackgroundCellColor(tableViewerCreator, element, columnIndex);
            }

            @Override
            public Color getForeground(Object element, int columnIndex) {
                return getForegroundCellColor(tableViewerCreator, element, columnIndex);
            }

        });

        initShowMessageErrorListener(tableForConstraints);
    }

    public void createFiltersColumns(final TableViewerCreator<FilterTableEntry> tableViewerCreatorForFilters) {
        TableViewerCreatorColumn column = new TableViewerCreatorColumn(tableViewerCreatorForFilters);
        column.setTitle(Messages.getString("OutputDataMapTableView.columnTitle.filterCondition")); //$NON-NLS-1$
        column.setId(DataMapTableView.ID_EXPRESSION_COLUMN);
        final ExtendedTextCellEditor expressionCellEditor = createExpressionCellEditor(tableViewerCreatorForFilters, column,
                new Zone[] { Zone.INPUTS, Zone.VARS }, true);
        column.setBeanPropertyAccessors(new IBeanPropertyAccessors<FilterTableEntry, String>() {

            public String get(FilterTableEntry bean) {
                return bean.getExpression();
            }

            public void set(FilterTableEntry bean, String value) {
                bean.setExpression(value);
                mapperManager.getProblemsManager().checkProblemsForTableEntry(bean, true);
            }

        });
        column.setModifiable(true);
        column.setDefaultInternalValue(""); //$NON-NLS-1$
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
             * org.talend.commons.ui.swt.tableviewer.tableeditor.ButtonImageTableEditorContent#selectionEvent(org.talend
             * .commons.ui.swt.tableviewer.TableViewerCreatorColumn, java.lang.Object)
             */
            @Override
            protected void selectionEvent(TableViewerCreatorColumnNotModifiable column, Object bean) {
                ExtendedTableRemoveCommand removeCommand = new ExtendedTableRemoveCommand(bean,
                        extendedTableViewerForFilters.getExtendedTableModel());
                mapperManager.removeTableEntry((ITableEntry) bean);
                mapperManager.executeCommand(removeCommand);
                tableViewerCreatorForFilters.getTableViewer().refresh();
                List list = tableViewerCreatorForFilters.getInputList();
                updateGridDataHeightForTableConstraints();
                if (list != null && list.size() == 0) {
                    showTableConstraints(false);
                } else {
                    showTableConstraints(true);
                }

            }

        };
        buttonImage.setImage(ImageProvider.getImage(EImage.MINUS_ICON));
        column.setTableEditorContent(buttonImage);

    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.mapper.ui.visualmap.table.DataMapTableView#getZone()
     */
    @Override
    public Zone getZone() {
        return Zone.OUTPUTS;
    }

    @Override
    public void unselectAllConstraintEntries() {
        tableViewerCreatorForFilters.getSelectionHelper().deselectAll();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.mapper.ui.visualmap.table.DataMapTableView#toolbarNeededToBeRightStyle()
     */
    @Override
    public boolean toolbarNeedToHaveRightStyle() {
        return false;
    }

    public OutputTable getOutputTable() {
        return (OutputTable) abstractDataMapTable;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.mapper.ui.visualmap.table.DataMapTableView#hasDropDownToolBarItem()
     */
    @Override
    public boolean hasDropDownToolBarItem() {
        return false;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.mapper.ui.visualmap.table.DataMapTableView#getValidZonesForExpressionFilterField()
     */
    @Override
    protected Zone[] getValidZonesForExpressionFilterField() {
        return new Zone[] { Zone.INPUTS };
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.mapper.ui.visualmap.table.DataMapTableView#loaded()
     */
    @Override
    public void loaded() {
        super.loaded();
        configureExpressionFilter();
        checkChangementsAfterEntryModifiedOrAdded(false);
    }

    protected void configureCellModifier(TableViewerCreator<OutputColumnTableEntry> tableViewerCreator) {
        cellModifier = new OutputTableCellModifier(tableViewerCreator);
        tableViewerCreator.setCellModifier(cellModifier);
    }

    /**
     *
     *
     * $Id: OutputTableCellModifier.java
     *
     */
    class OutputTableCellModifier extends DefaultCellModifier {

        /**
         * DOC wchen OutputTableCellModifier constructor comment.
         *
         * @param tableViewerCreator
         */
        public OutputTableCellModifier(TableViewerCreator tableViewerCreator) {
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
            if (element instanceof OutputColumnTableEntry) {
                OutputColumnTableEntry outputColumn = (OutputColumnTableEntry) element;
                if (outputColumn.getParent() instanceof OutputTable
                        && ((OutputTable) outputColumn.getParent()).isErrorRejectTable()) {
                    IMetadataColumn metadataColumn = outputColumn.getMetadataColumn();
                    if (metadataColumn != null
                            && (getMapperManager().ERROR_REJECT_MESSAGE.equals(metadataColumn.getLabel()) || getMapperManager().ERROR_REJECT_STACK_TRACE
                                    .equals(metadataColumn.getLabel()))) {
                        return false;

                    }

                }

            }

            return !mapperManager.componentIsReadOnly();

        }

    }

    @Override
    public String findUniqueName(String baseName) {
        return "\"\""; //$NON-NLS-1$
    }
}
