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
package org.talend.designer.mapper.ui.visualmap.table;

import java.util.ArrayList;
import java.util.List;

import org.apache.oro.text.regex.MalformedPatternException;
import org.apache.oro.text.regex.Pattern;
import org.apache.oro.text.regex.Perl5Compiler;
import org.apache.oro.text.regex.Perl5Matcher;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ComboBoxCellEditor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ArmEvent;
import org.eclipse.swt.events.ArmListener;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.ToolItem;
import org.talend.commons.ui.runtime.image.EImage;
import org.talend.commons.ui.runtime.image.ImageProvider;
import org.talend.commons.ui.runtime.swt.tableviewer.TableViewerCreatorColumnNotModifiable;
import org.talend.commons.ui.runtime.swt.tableviewer.TableViewerCreatorNotModifiable.LAYOUT_MODE;
import org.talend.commons.ui.runtime.swt.tableviewer.TableViewerCreatorNotModifiable.SHOW_ROW_SELECTION;
import org.talend.commons.ui.runtime.swt.tableviewer.behavior.DefaultHeaderColumnSelectionListener;
import org.talend.commons.ui.runtime.swt.tableviewer.behavior.DefaultTableLabelProvider;
import org.talend.commons.ui.runtime.swt.tableviewer.behavior.IColumnImageProvider;
import org.talend.commons.ui.runtime.swt.tableviewer.behavior.ITableCellValueModifiedListener;
import org.talend.commons.ui.runtime.swt.tableviewer.behavior.TableCellValueModifiedEvent;
import org.talend.commons.ui.runtime.swt.tableviewer.celleditor.ExtendedTextCellEditor;
import org.talend.commons.ui.runtime.swt.tableviewer.tableeditor.ButtonPushImageTableEditorContent;
import org.talend.commons.ui.runtime.ws.WindowSystem;
import org.talend.commons.ui.swt.advanced.dataeditor.commands.ExtendedTableRemoveCommand;
import org.talend.commons.ui.swt.extended.table.AbstractExtendedTableViewer;
import org.talend.commons.ui.swt.extended.table.ExtendedTableModel;
import org.talend.commons.ui.swt.tableviewer.TableViewerCreator;
import org.talend.commons.ui.swt.tableviewer.TableViewerCreatorColumn;
import org.talend.commons.ui.swt.tableviewer.behavior.DefaultCellModifier;
import org.talend.commons.utils.data.bean.IBeanPropertyAccessors;
import org.talend.core.PluginChecker;
import org.talend.core.model.metadata.IMetadataColumn;
import org.talend.core.model.process.Problem;
import org.talend.core.model.process.TraceData;
import org.talend.core.utils.TalendQuoteUtils;
import org.talend.designer.abstractmap.model.table.IDataMapTable;
import org.talend.designer.abstractmap.model.tableentry.ITableEntry;
import org.talend.designer.mapper.i18n.Messages;
import org.talend.designer.mapper.managers.MapperManager;
import org.talend.designer.mapper.managers.UIManager;
import org.talend.designer.mapper.model.table.AbstractInOutTable;
import org.talend.designer.mapper.model.table.IUILookupMode;
import org.talend.designer.mapper.model.table.IUIMatchingMode;
import org.talend.designer.mapper.model.table.IUIMenuOption;
import org.talend.designer.mapper.model.table.InputTable;
import org.talend.designer.mapper.model.table.TMAP_LOOKUP_MODE;
import org.talend.designer.mapper.model.table.TMAP_MATCHING_MODE;
import org.talend.designer.mapper.model.tableentry.GlobalMapEntry;
import org.talend.designer.mapper.model.tableentry.InputColumnTableEntry;
import org.talend.designer.mapper.ui.dialog.ListStringValueDialog;
import org.talend.designer.mapper.ui.dnd.DragNDrop;
import org.talend.designer.mapper.ui.footer.StatusBar.STATUS;
import org.talend.designer.mapper.ui.image.ImageInfo;
import org.talend.designer.mapper.ui.image.ImageProviderMapper;
import org.talend.designer.mapper.ui.visualmap.zone.Zone;
import org.talend.designer.mapper.utils.problems.ProblemsAnalyser;

/**
 * DOC amaumont class global comment. Detailled comment <br/>
 *
 * $Id$
 *
 */
public class InputDataMapTableView extends DataMapTableView {

    private IUIMatchingMode previousMatchingModeSelected = getInputTable().getMatchingMode() != null
            && getInputTable().getMatchingMode() != TMAP_MATCHING_MODE.ALL_ROWS ? getInputTable().getMatchingMode()
            : (getMapperManager().isMRProcess() ? TMAP_MATCHING_MODE.ALL_MATCHES : TMAP_MATCHING_MODE.UNIQUE_MATCH);

    private boolean previousStateAtLeastOneHashKey;

    private boolean innerJoinCheckItemEditable = false;

    private boolean previousInnerJoinSelection = getInputTable().isInnerJoin();

    private ExtendedTextCellEditor expressionCellEditor;

    private boolean persistentCheckEditable;

    protected boolean previousStatePersistentCheckFilter;

    protected boolean previousValidPersistentMode;

    private static final String INNER_JOIN = "Inner Join";

    private static final String LEFT_OUTER_JOIN = "Left Outer Join";

    public InputDataMapTableView(Composite parent, int style, InputTable inputTable, MapperManager mapperManager) {
        super(parent, style, inputTable, mapperManager);
        previousValidPersistentMode = inputTable.isPersistent();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.mapper.ui.visualmap.table.DataMapTableView#createContent()
     */
    @Override
    protected void createContent() {
        createExpressionFilter(DEFAULT_POST_MATCHING_EXPRESSION_FILTER);

        createColumnNameFilter();
        createTableForColumns();

    }

    @Override
    protected void createMapSettingTable() {

        ExtendedTableModel<GlobalMapEntry> tableMapSettingEntriesModel = ((InputTable) abstractDataMapTable)
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
            if (!getInputTable().isMainConnection()) {
                if (!getMapperManager().isMRProcess()) {
                    tableMapSettingEntriesModel.add(new GlobalMapEntry(abstractDataMapTable, LOOKUP_MODEL_SETTING, null));
                }
                tableMapSettingEntriesModel.add(new GlobalMapEntry(abstractDataMapTable, MATCH_MODEL_SETTING, null));
                tableMapSettingEntriesModel.add(new GlobalMapEntry(abstractDataMapTable, JOIN_MODEL_SETTING, null));
                if (!getMapperManager().isBigDataProcess()) {
                    tableMapSettingEntriesModel.add(new GlobalMapEntry(abstractDataMapTable, PERSISTENCE_MODEL_SETTING, null));
                }
                // remove schema type in input tables
                // tableMapSettingEntriesModel.add(new GlobalMapEntry(abstractDataMapTable, SCHEMA_TYPE, null));
            } else {
                // tableMapSettingEntriesModel.add(new GlobalMapEntry(abstractDataMapTable, SCHEMA_TYPE, null));
            }
            // if (getInputTable().isRepository()) {
            // tableMapSettingEntriesModel.add(new GlobalMapEntry(abstractDataMapTable, SCHEMA_ID, null));
            // }
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

        mapSettingViewerCreator.setCellModifier(new TableCellModifier(mapSettingViewerCreator));
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
                InputTable inputTable = (InputTable) parent;
                if (cellEditor instanceof ComboBoxCellEditor) {
                    ComboBoxCellEditor functComboBox = (ComboBoxCellEditor) cellEditor;
                    if (LOOKUP_MODEL_SETTING.equals(bean.getName())) {
                        IUILookupMode[] availableJoins = { TMAP_LOOKUP_MODE.LOAD_ONCE, TMAP_LOOKUP_MODE.RELOAD,
                                TMAP_LOOKUP_MODE.CACHE_OR_RELOAD };
                        List<String> names = new ArrayList<String>();
                        for (IUILookupMode availableJoin : availableJoins) {
                            names.add(availableJoin.getLabel());
                        }
                        functComboBox.setItems(names.toArray(new String[names.size()]));
                        final IUILookupMode lookupMode = ((InputTable) parent).getLookupMode();
                        if (lookupMode != null) {
                            return lookupMode.getLabel();
                        }
                    } else if (MATCH_MODEL_SETTING.equals(bean.getName())) {
                        IUIMatchingMode[] matchModel = getMatchModel();
                        List<String> names = new ArrayList<String>();
                        for (IUIMatchingMode element : matchModel) {
                            names.add(element.getLabel());
                        }
                        functComboBox.setItems(names.toArray(new String[names.size()]));
                        IUIMatchingMode matchingMode = ((InputTable) parent).getMatchingMode();
                        if (names.contains(matchingMode.getLabel())) {
                            return matchingMode.getLabel();
                        }

                    } else if (JOIN_MODEL_SETTING.equals(bean.getName())) {
                        String[] items = new String[] { INNER_JOIN, LEFT_OUTER_JOIN };
                        functComboBox.setItems(items);
                        boolean innerJoin = ((InputTable) parent).isInnerJoin();
                        if (innerJoin) {
                            return items[0];
                        }
                        return items[1];
                    } else if (PERSISTENCE_MODEL_SETTING.equals(bean.getName())) {
                        functComboBox.setItems(new String[] { "true", "false" });
                        boolean persistent = ((InputTable) parent).isPersistent();
                        return String.valueOf(persistent);
                    }
                } else if (cellEditor instanceof CustomDialogCellEditor) {
                    CustomDialogCellEditor customDialogCellEditor = (CustomDialogCellEditor) cellEditor;
                    if (LOOKUP_MODEL_SETTING.equals(bean.getName())) {
                        customDialogCellEditor.setType(CellValueType.LOOKUP_MODEL);
                        final IUILookupMode lookupMode = inputTable.getLookupMode();
                        if (lookupMode != null) {
                            return lookupMode.getLabel();
                        }
                    } else if (MATCH_MODEL_SETTING.equals(bean.getName())) {
                        customDialogCellEditor.setType(CellValueType.MATCH_MODEL);
                        IUIMatchingMode matchingMode = inputTable.getMatchingMode();
                        if (matchingMode != null) {
                            return matchingMode.getLabel();
                        }
                    } else if (JOIN_MODEL_SETTING.equals(bean.getName())) {
                        customDialogCellEditor.setType(CellValueType.JOIN_MODEL);
                        boolean innerJoin = inputTable.isInnerJoin();
                        return innerJoin ? INNER_JOIN : LEFT_OUTER_JOIN;
                    } else if (PERSISTENCE_MODEL_SETTING.equals(bean.getName())) {
                        customDialogCellEditor.setType(CellValueType.BOOL);
                        boolean persistent = inputTable.isPersistent();
                        return String.valueOf(persistent);
                    } else if (SCHEMA_TYPE.equals(bean.getName())) {
                        customDialogCellEditor.setType(CellValueType.SCHEMA_TYPE);
                        enableDiaplayViewer(inputTable.isRepository());
                        return inputTable.isRepository() ? REPOSITORY : BUILT_IN;
                    } else if (SCHEMA_ID.equals(bean.getName())) {
                        customDialogCellEditor.setType(CellValueType.SCHEMA_ID);
                        return getSchemaDisplayName(inputTable.getId());
                    }
                }

                return "";
            }

            public void set(GlobalMapEntry bean, Object value) {
                IDataMapTable parent = bean.getParent();
                InputTable inputTable = (InputTable) parent;
                if (LOOKUP_MODEL_SETTING.equals(bean.getName())) {
                    IUILookupMode[] availableJoins = { TMAP_LOOKUP_MODE.LOAD_ONCE, TMAP_LOOKUP_MODE.RELOAD,
                            TMAP_LOOKUP_MODE.CACHE_OR_RELOAD };
                    for (final IUILookupMode lookupMode : availableJoins) {
                        if (value != null && value.equals(lookupMode.getLabel())) {
                            inputTable.setLookupMode(lookupMode);
                            if (TMAP_LOOKUP_MODE.CACHE_OR_RELOAD == lookupMode) {
                                persistentCheckEditable = false;
                            } else {
                                persistentCheckEditable = true;
                            }
                            if (lookupMode == TMAP_LOOKUP_MODE.RELOAD || lookupMode == TMAP_LOOKUP_MODE.CACHE_OR_RELOAD) {
                                showTableGlobalMap(true);
                            } else {
                                showTableGlobalMap(false);
                            }
                            enableDisablePersistentMode((TMAP_LOOKUP_MODE) lookupMode);
                            mapperManager.getProblemsManager().checkProblemsForTableEntry(bean, true);
                            checkChangementsAfterEntryModifiedOrAdded(true);

                        }
                    }

                } else if (MATCH_MODEL_SETTING.equals(bean.getName())) {
                    for (IUIMatchingMode model : TMAP_MATCHING_MODE.values()) {
                        if (value != null && value.equals(model.getLabel())) {
                            inputTable.setMatchingMode(model);
                            previousMatchingModeSelected = model;
                        }
                    }
                } else if (JOIN_MODEL_SETTING.equals(bean.getName())) {
                    if (LEFT_OUTER_JOIN.equals(value)) {
                        ((InputTable) parent).setInnerJoin(false);
                        previousInnerJoinSelection = false;
                    } else {
                        ((InputTable) parent).setInnerJoin(true);
                        previousInnerJoinSelection = true;
                    }
                } else if (PERSISTENCE_MODEL_SETTING.equals(bean.getName())) {
                    if ("true".equals(value) || "false".equals(value)) {
                        ((InputTable) parent).setPersistent(Boolean.valueOf(value.toString()));
                        previousValidPersistentMode = Boolean.valueOf(value.toString());
                    }
                } else if (SCHEMA_TYPE.equals(bean.getName())) {
                    inputTable.setRepository(REPOSITORY.equals(value));
                    showSchemaIDSetting(REPOSITORY.equals(value));
                    enableDiaplayViewer(REPOSITORY.equals(value));
                } else if (SCHEMA_ID.equals(bean.getName())) {
                    inputTable.setId(String.valueOf(value));
                }

                refreshCondensedImage((InputTable) bean.getParent(), bean.getName());

            }
        };
    }

    @Override
    protected void refreshCondensedImage(AbstractInOutTable absTable, String option) {
        InputTable table = (InputTable) absTable;
        if (LOOKUP_MODEL_SETTING.equals(option)) {
            if (table.getLookupMode().equals(mapperManager.getDefaultSetting().get(LOOKUP_MODEL_SETTING))) {
                if (changedOptions > 0) {
                    changedOptions--;
                }
            } else {
                if (changedOptions < 6) {
                    changedOptions++;
                }
            }
        } else if (MATCH_MODEL_SETTING.equals(option)) {
            if (table.getMatchingMode().equals(mapperManager.getDefaultSetting().get(MATCH_MODEL_SETTING))) {
                if (changedOptions > 0) {
                    changedOptions--;
                }
            } else {
                if (changedOptions < 6) {
                    changedOptions++;
                }
            }
        } else if (JOIN_MODEL_SETTING.equals(option)) {
            if (Boolean.valueOf(table.isInnerJoin()).equals(mapperManager.getDefaultSetting().get(JOIN_MODEL_SETTING))) {
                if (changedOptions > 0) {
                    changedOptions--;
                }
            } else {
                if (changedOptions < 6) {
                    changedOptions++;
                }
            }

        } else if (PERSISTENCE_MODEL_SETTING.equals(option)) {
            if (Boolean.valueOf(table.isPersistent()).equals(mapperManager.getDefaultSetting().get(PERSISTENCE_MODEL_SETTING))) {
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
        if (!getInputTable().isMainConnection()) {
            checkLookupTableProblems(mapperManager.isTableHasAtLeastOneHashKey(table));
        }
    }

    @Override
    protected boolean needColumnBgColor(GlobalMapEntry bean) {
        InputTable inputTable = (InputTable) bean.getParent();

        if (LOOKUP_MODEL_SETTING.equals(bean.getName())) {
            if (!mapperManager.getDefaultSetting().get(LOOKUP_MODEL_SETTING).equals(inputTable.getLookupMode())) {
                return true;
            }
        } else if (MATCH_MODEL_SETTING.equals(bean.getName())) {
            if (!mapperManager.getDefaultSetting().get(MATCH_MODEL_SETTING).equals(inputTable.getMatchingMode())) {
                return true;
            }

        } else if (JOIN_MODEL_SETTING.equals(bean.getName())) {
            if (!mapperManager.getDefaultSetting().get(JOIN_MODEL_SETTING).equals(inputTable.isInnerJoin())) {
                return true;
            }

        } else if (PERSISTENCE_MODEL_SETTING.equals(bean.getName())) {
            if (!mapperManager.getDefaultSetting().get(PERSISTENCE_MODEL_SETTING).equals(inputTable.isPersistent())) {
                return true;
            }
        } else if (SCHEMA_TYPE.equals(bean.getName())) {
            if (!mapperManager.getDefaultSetting().get(SCHEMA_TYPE).equals(inputTable.isRepository())) {
                return true;
            }
        } else if (SCHEMA_ID.equals(bean.getName())) {
            if (mapperManager.getDefaultSetting().get(SCHEMA_ID) != inputTable.getId()) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected void initCondensedItemImage() {
        if (!mapperManager.getDefaultSetting().get(LOOKUP_MODEL_SETTING).equals(getInputTable().getLookupMode())) {
            if (changedOptions < 6) {
                changedOptions++;
            }
        }

        if (!mapperManager.getDefaultSetting().get(MATCH_MODEL_SETTING).equals(getInputTable().getMatchingMode())) {
            if (changedOptions < 6) {
                changedOptions++;
            }
        }

        if (!mapperManager.getDefaultSetting().get(JOIN_MODEL_SETTING).equals(getInputTable().isInnerJoin())) {
            if (changedOptions < 6) {
                changedOptions++;
            }
        }
        if (!mapperManager.getDefaultSetting().get(PERSISTENCE_MODEL_SETTING).equals(getInputTable().isPersistent())) {
            if (changedOptions < 6) {
                changedOptions++;
            }
        }
        if (!mapperManager.getDefaultSetting().get(SCHEMA_TYPE).equals(getInputTable().isRepository())) {
            if (changedOptions < 6) {
                changedOptions++;
            }
        }
        if (mapperManager.getDefaultSetting().get(SCHEMA_ID) != getInputTable().getId()) {
            if (changedOptions < 6) {
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
        if (expressionCellEditor != null) {
            expressionCellEditor.focusLost();
        }
    }

    @Override
    public void initColumnsOfTableColumns(final TableViewerCreator tableViewerCreatorForColumns) {
        boolean isMainConnection = ((InputTable) getDataMapTable()).isMainConnection();
        TableViewerCreatorColumn column = null;
        if (!isMainConnection) {
            column = new TableViewerCreatorColumn(tableViewerCreatorForColumns);
            column.setTitle(Messages.getString("InputDataMapTableView.columnTitle.Expr")); //$NON-NLS-1$
            column.setId(DataMapTableView.ID_EXPRESSION_COLUMN);
            expressionCellEditor = createExpressionCellEditor(tableViewerCreatorForColumns, column, new Zone[] { Zone.INPUTS },
                    false);
            column.setBeanPropertyAccessors(new IBeanPropertyAccessors<InputColumnTableEntry, String>() {

                public String get(InputColumnTableEntry bean) {
                    return bean.getExpression();
                }

                public void set(InputColumnTableEntry bean, String value) {
                    // System.out.println("value='" + value + "'");
                    bean.setExpression(value);
                    InputTable inputTable = getInputTable();
                    boolean tableHasAtLeastOneHashKey = mapperManager.isTableHasAtLeastOneHashKey(inputTable);
                    if (!tableHasAtLeastOneHashKey) {
                        IUIMatchingMode matchingMode = inputTable.getMatchingMode();
                        if (matchingMode != TMAP_MATCHING_MODE.ALL_ROWS) {
                            inputTable.setMatchingMode(TMAP_MATCHING_MODE.ALL_ROWS);
                            refreshCondensedImage(inputTable, MATCH_MODEL_SETTING);
                        }
                    } else {
                        IUIMatchingMode matchingMode = inputTable.getMatchingMode();
                        if (matchingMode == TMAP_MATCHING_MODE.ALL_ROWS) {
                            IUIMatchingMode newMatchModel = previousMatchingModeSelected == null
                                    || previousMatchingModeSelected == TMAP_MATCHING_MODE.ALL_ROWS ? TMAP_MATCHING_MODE.UNIQUE_MATCH
                                    : previousMatchingModeSelected;
                            inputTable.setMatchingMode(newMatchModel);
                            refreshCondensedImage(inputTable, MATCH_MODEL_SETTING);
                        }
                        mapperManager.getUiManager().setStatusBarValues(STATUS.EMPTY, null);
                    }
                    mapperManager.getProblemsManager().checkProblemsForTableEntry(bean, true, true);
                    if (!mapperManager.isCheckSyntaxEnabled()) {
                        mapperManager.getUiManager().applyActivatedCellEditors(tableViewerCreatorForColumns);
                        Display.getDefault().asyncExec(new Runnable() {

                            public void run() {
                                mapperManager.getProblemsManager().checkLookupExpressionProblem();
                            }
                        });
                    }
                }

            });

            column.setModifiable(!mapperManager.componentIsReadOnly());
            column.setDefaultInternalValue(""); //$NON-NLS-1$
            column.setWeight(COLUMN_EXPRESSION_SIZE_WEIGHT);
            column.setImageProvider(new IColumnImageProvider<InputColumnTableEntry>() {

                public Image getImage(InputColumnTableEntry bean) {
                    if (bean.getExpression() != null && !bean.getExpression().trim().equals("")) { //$NON-NLS-1$
                        if (mapperManager.isAdvancedMap()) {
                            return ImageProviderMapper.getImage(ImageInfo.LOOKUP_KEY_ICON);
                        } else {
                            return null;
                        }
                    } else {
                        return null;
                    }
                }

            });
        }

        column = new TableViewerCreatorColumn(tableViewerCreatorForColumns);
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

        if (isMainConnection && PluginChecker.isTraceDebugPluginLoaded() && mapperManager.isTracesActive()) {
            column = new TableViewerCreatorColumn(tableViewerCreatorForColumns);
            column.setTitle("Preview");
            column.setId(DataMapTableView.PREVIEW_COLUMN);
            column.setWeight(COLUMN_NAME_SIZE_WEIGHT);
            column.setBeanPropertyAccessors(new IBeanPropertyAccessors<InputColumnTableEntry, String>() {

                public String get(InputColumnTableEntry bean) {
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

                public void set(InputColumnTableEntry bean, String value) {
                    // do nothing
                }

            });
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.mapper.ui.visualmap.table.DataMapTableView#initTableConstraints()
     */
    @Override
    protected void initExtraTable() {

        createGlobalMapTable();

        List<GlobalMapEntry> entries = ((InputTable) getDataMapTable()).getGlobalMapEntries();

        // correct partially layout problem with GTK when cell editor value is
        // applied
        tableViewerCreatorForGlobalMap.setAdjustWidthValue(WindowSystem.isGTK() ? -20 : ADJUST_WIDTH_VALUE);

        tableViewerCreatorForGlobalMap.init(entries);
        updateGridDataHeightForTableGlobalMap();
        //
        // if (WindowSystem.isGTK()) {
        // tableViewerCreatorForGlobalMap.layout();
        // }

    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.mapper.ui.visualmap.table.DataMapTableView#addEntriesActionsComponents()
     */
    @Override
    protected boolean addToolItems() {

        // TODO: unlock this tentatively.
        if (!getInputTable().isMainConnection()) {

            final InputTable table = getInputTable();
            // condensed Item
            condensedItem = new ToolItem(toolBarActions, SWT.CHECK);
            // condensedItem.setEnabled(!mapperManager.componentIsReadOnly());
            condensedItem.setSelection(table.isActivateCondensedTool());
            condensedItem.setToolTipText("tMap settings");
            initCondensedItemImage();
            condensedItem.addSelectionListener(new SelectionAdapter() {

                @Override
                public void widgetSelected(SelectionEvent e) {
                    table.setActivateCondensedTool(condensedItem.getSelection());
                    showTableMapSetting(condensedItem.getSelection());
                }

            });

            if (mapperManager.isPersistentMap()) {
                TMAP_LOOKUP_MODE lookupMode = (TMAP_LOOKUP_MODE) table.getLookupMode();

                switch (lookupMode) {
                case LOAD_ONCE:
                case LOAD_ONCE_AND_UPDATE:
                case RELOAD:
                    persistentCheckEditable = true;
                    previousValidPersistentMode = table.isPersistent();
                    break;
                case CACHE_OR_RELOAD:
                    persistentCheckEditable = false;
                    getInputTable().setPersistent(false);

                    break;
                default:
                    break;
                }
            }

        }

        createActivateFilterCheck();
        createColumnNameFilterCheck();

        return true;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.mapper.ui.visualmap.table.DataMapTableView#getZone()
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
     * @see org.talend.designer.mapper.ui.visualmap.table.DataMapTableView#toolbarNeededToBeRightStyle()
     */
    @Override
    public boolean toolbarNeedToHaveRightStyle() {
        return !getInputTable().isMainConnection();
    }

    private IUIMatchingMode[] getMatchModel() {
        IUIMatchingMode[] allMatchingModel = TMAP_MATCHING_MODE.values();
        List<IUIMatchingMode> avilable = new ArrayList<IUIMatchingMode>();
        for (IUIMatchingMode matchingMode : allMatchingModel) {
            final String text = matchingMode.getLabel();
            if (matchingMode == TMAP_MATCHING_MODE.LAST_MATCH) {
                continue;
            }
            if (getMapperManager().isBigDataProcess() && matchingMode == TMAP_MATCHING_MODE.FIRST_MATCH) {
                continue;
            }
            if (text.length() != 0) {
                if (getMapperManager().isMRProcess() && matchingMode == TMAP_MATCHING_MODE.ALL_MATCHES) {
                    // set ALL_MATCHES as default value for m/r process
                    avilable.add(0, matchingMode);
                } else {
                    avilable.add(matchingMode);
                }

            }
        }

        return avilable.toArray(new IUIMatchingMode[avilable.size()]);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.dbmap.ui.visualmap.table.DataMapTableView#hasDropDownToolBarItem()
     */
    @Override
    public boolean hasDropDownToolBarItem() {
        return !getInputTable().isMainConnection() && mapperManager.isAdvancedMap();
    }

    private void layoutToolBar() {
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
     * @see org.talend.designer.dbmap.ui.visualmap.table.DataMapTableView#checkChangementsAfterNewExpressionApplied()
     */
    @Override
    public void checkChangementsAfterEntryModifiedOrAdded(boolean forceEvaluation) {
        super.checkChangementsAfterEntryModifiedOrAdded(forceEvaluation);
        if (!getInputTable().isMainConnection() && mapperManager.isAdvancedMap()) {
            boolean stateAtLeastOneHashKey = mapperManager.isTableHasAtLeastOneHashKey(getInputTable());
            if (forceEvaluation || previousStateAtLeastOneHashKey != stateAtLeastOneHashKey) {
                if (stateAtLeastOneHashKey) {
                    IUIMatchingMode matchingMode = getInputTable().getMatchingMode();
                    if (matchingMode == TMAP_MATCHING_MODE.ALL_ROWS) {
                        IUIMatchingMode newMatchModel = previousMatchingModeSelected == null
                                || previousMatchingModeSelected == TMAP_MATCHING_MODE.ALL_ROWS ? TMAP_MATCHING_MODE.UNIQUE_MATCH
                                : previousMatchingModeSelected;
                        getInputTable().setMatchingMode(newMatchModel);
                        refreshCondensedImage(getInputTable(), MATCH_MODEL_SETTING);
                    }

                } else {
                    if (getInputTable().getLookupMode() == TMAP_LOOKUP_MODE.CACHE_OR_RELOAD) {
                        String errorMessage = Messages.getString("InputDataMapTableView.invalidConfiguration", getInputTable() //$NON-NLS-1$
                                .getName());
                        mapperManager.getUiManager().setStatusBarValues(STATUS.ERROR, errorMessage);

                    } else {
                        mapperManager.getUiManager().setStatusBarValues(STATUS.EMPTY, null);
                        // selectMatchingModeMenuItem(matchingMode);
                    }

                    IUIMatchingMode matchingMode = getInputTable().getMatchingMode();
                    if (matchingMode != TMAP_MATCHING_MODE.ALL_ROWS) {
                        getInputTable().setMatchingMode(TMAP_MATCHING_MODE.ALL_ROWS);
                        refreshCondensedImage(getInputTable(), MATCH_MODEL_SETTING);
                    }

                    innerJoinCheckItemEditable = false;
                }

                previousStateAtLeastOneHashKey = stateAtLeastOneHashKey;
            }
            // if user didn't trigger save mapper action ,and click ok to close the window diretly, then it will cause
            // closeMapper first then here checkChangementsAfterEntryModifiedOrAdded , the Composite will be disposed.
            // so when try to checkLookupTableProblems on a disposed Widget , will cause org.eclipse.swt.SWTException:
            // Widget is disposed
            if (headerComposite != null && !headerComposite.isDisposed()) {
                checkLookupTableProblems(stateAtLeastOneHashKey);
            }
            mapSettingViewerCreator.refresh();
        }

    }

    private void checkLookupTableProblems(boolean stateAtLeastOneHashKey) {
        ProblemsAnalyser problemAnalyser = new ProblemsAnalyser(mapperManager);
        List<Problem> lookupTableProblem = problemAnalyser.getLookupTableProblem(getInputTable(), stateAtLeastOneHashKey);
        if (!lookupTableProblem.isEmpty()) {
            String problemTxt = "";
            for (Problem problem : lookupTableProblem) {
                problemTxt = problemTxt + problem.getDescription();
            }
            warningLabel.setToolTipText(problemTxt);
            nameLabel.setToolTipText(problemTxt);
            // headerComposite.setBackground(ColorProviderMapper.getColor(ColorInfo.COLOR_ENTRY_SEARCH_HIGHLIGHTED));
            headerComposite.setToolTipText(problemTxt);
            warningLabel.setVisible(true);
            warnLabelData.exclude = false;
        } else {
            warningLabel.setVisible(false);
            nameLabel.setToolTipText(getInputTable().getName());
            headerComposite.setToolTipText(null);
            // headerComposite.setBackground(this.getBackground());
            warnLabelData.exclude = true;
        }
        layoutToolBar();
    }

    /**
     * DOC amaumont Comment method "updateViewAfterChangeInnerJoinCheck".
     */
    private void updateViewAfterChangeInnerJoinCheck() {

        // if (LanguageProvider.getCurrentLanguage() instanceof PerlLanguage) {
        // if (innerJoinCheck.getSelection() || getInputTable().getMatchingMode() == TMAP_MATCHING_MODE.ALL_ROWS) {
        // getActivateFilterCheck().setSelection(isPreviousStateCheckFilter());
        // getActivateFilterCheck().setEnabled(true);
        // } else {
        // getActivateFilterCheck().setSelection(false);
        // getActivateFilterCheck().setEnabled(false);
        // }
        // }
        updateExepressionFilterTextAndLayout(false);

        // enableDisablePersistentMode((TMAP_MATCHING_MODE) getInputTable().getMatchingMode());
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

    /**
     * DOC amaumont Comment method "init".
     */
    @Override
    public void loaded() {

        super.loaded();

        if (mapperManager.isAdvancedMap()) {
            configureExpressionFilter();
            if (!getInputTable().isMainConnection()) {
                checkChangementsAfterEntryModifiedOrAdded(true);
            }
        }

    }

    protected void createGlobalMapTable() {

        this.extendedTableViewerForGlobalMap = new AbstractExtendedTableViewer<GlobalMapEntry>(
                ((InputTable) abstractDataMapTable).getTableGlobalMapEntriesModel(), centerComposite) {

            @Override
            protected void createColumns(TableViewerCreator<GlobalMapEntry> tableViewerCreator, Table table) {
                createGlobalMapColumns(tableViewerCreator);
            }

            /*
             * (non-Javadoc)
             *
             * @see
             * org.talend.commons.ui.swt.extended.macrotable.AbstractExtendedTableViewer#setTableViewerCreatorOptions
             * (org.talend.commons.ui.swt.tableviewer.TableViewerCreator)
             */
            @Override
            protected void setTableViewerCreatorOptions(TableViewerCreator<GlobalMapEntry> newTableViewerCreator) {
                super.setTableViewerCreatorOptions(newTableViewerCreator);
                newTableViewerCreator.setColumnsResizableByDefault(true);
                newTableViewerCreator.setShowLineSelection(SHOW_ROW_SELECTION.FULL);
                newTableViewerCreator.setBorderVisible(false);
                newTableViewerCreator.setLayoutMode(LAYOUT_MODE.FILL_HORIZONTAL);
                newTableViewerCreator.setKeyboardManagementForCellEdition(true);
                newTableViewerCreator.setHorizontalScroll(false);
                newTableViewerCreator.setVerticalScroll(false);
                // tableViewerCreatorForColumns.setUseCustomItemColoring(this.getDataMapTable()
                // instanceof
                // AbstractInOutTable);
                newTableViewerCreator.setFirstColumnMasked(true);

            }

        };
        tableViewerCreatorForGlobalMap = this.extendedTableViewerForGlobalMap.getTableViewerCreator();
        if (mapperManager.componentIsReadOnly()) {
            tableViewerCreatorForGlobalMap.setReadOnly(true);
        }
        this.extendedTableViewerForGlobalMap.setCommandStack(mapperManager.getCommandStack());

        tableForGlobalMap = tableViewerCreatorForGlobalMap.getTable();
        tableForGlobalMapGridData = new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1);
        tableForGlobalMap.setLayoutData(tableForGlobalMapGridData);

        boolean tableGlobalMapVisible = false;
        if (abstractDataMapTable instanceof InputTable) {
            IUILookupMode lookupMode = ((InputTable) abstractDataMapTable).getLookupMode();
            tableGlobalMapVisible = lookupMode == TMAP_LOOKUP_MODE.RELOAD || lookupMode == TMAP_LOOKUP_MODE.CACHE_OR_RELOAD;
        }

        tableForGlobalMapGridData.exclude = !tableGlobalMapVisible;
        tableForGlobalMap.setVisible(tableGlobalMapVisible);

        if (!mapperManager.componentIsReadOnly()) {
            new DragNDrop(mapperManager, tableForGlobalMap, false, true);
        }

        tableViewerCreatorForGlobalMap.addCellValueModifiedListener(new ITableCellValueModifiedListener() {

            public void cellValueModified(TableCellValueModifiedEvent e) {
                unselectAllEntriesIfErrorDetected(e);
            }
        });

        final TableViewer tableViewer = tableViewerCreatorForGlobalMap.getTableViewer();

        tableViewer.addSelectionChangedListener(new ISelectionChangedListener() {

            public void selectionChanged(SelectionChangedEvent event) {
                ISelection selection = event.getSelection();
                selectThisDataMapTableView();
                UIManager uiManager = mapperManager.getUiManager();
                uiManager.selectLinks(InputDataMapTableView.this, uiManager.extractSelectedTableEntries(selection), false, false);
            }

        });

        tableForGlobalMap.addListener(SWT.KeyDown, new Listener() {

            public void handleEvent(Event event) {
                processEnterKeyDown(tableViewerCreatorForGlobalMap, event);
            }

        });

        tableViewerCreatorForGlobalMap.setLabelProvider(new DefaultTableLabelProvider(tableViewerCreatorForGlobalMap) {

            @Override
            public Color getBackground(Object element, int columnIndex) {
                return getBackgroundCellColor(tableViewerCreator, element, columnIndex);
            }

            @Override
            public Color getForeground(Object element, int columnIndex) {
                return getForegroundCellColor(tableViewerCreator, element, columnIndex);
            }

        });

        initShowMessageErrorListener(tableForGlobalMap);
    }

    public void createGlobalMapColumns(final TableViewerCreator<GlobalMapEntry> tableViewerCreatorForGlobalMap) {
        TableViewerCreatorColumn<GlobalMapEntry, String> column = new TableViewerCreatorColumn<GlobalMapEntry, String>(
                tableViewerCreatorForGlobalMap);
        //        column.setTitle(Messages.getString("InputDataMapTableView.columnTitle.globalMapVar")); //$NON-NLS-1$
        column.setTitle("Expr."); //$NON-NLS-1$
        column.setId(DataMapTableView.ID_EXPRESSION_COLUMN);
        final ExtendedTextCellEditor expressionCellEditor = createExpressionCellEditor(tableViewerCreatorForGlobalMap, column,
                new Zone[] { Zone.INPUTS }, false);
        column.setBeanPropertyAccessors(new IBeanPropertyAccessors<GlobalMapEntry, String>() {

            public String get(GlobalMapEntry bean) {
                return bean.getExpression();
            }

            public void set(GlobalMapEntry bean, String value) {
                bean.setExpression(value);
                mapperManager.getProblemsManager().checkProblemsForTableEntry(bean, true);
            }

        });
        column.setModifiable(true);
        column.setDefaultInternalValue(""); //$NON-NLS-1$
        column.setWeight(COLUMN_EXPRESSION_SIZE_WEIGHT);
        column.setMoveable(false);
        column.setResizable(true);

        column = new TableViewerCreatorColumn<GlobalMapEntry, String>(tableViewerCreatorForGlobalMap);
        column.setTitle(Messages.getString("InputDataMapTableView.globalMapKey")); //$NON-NLS-1$
        column.setId(DataMapTableView.ID_NAME_COLUMN);
        column.setModifiable(true);
        column.setBeanPropertyAccessors(new IBeanPropertyAccessors<GlobalMapEntry, String>() {

            public String get(GlobalMapEntry bean) {
                return bean.getName();
            }

            public void set(GlobalMapEntry bean, String value) {
                bean.setName(value);
            }

        });
        column.setWeight(COLUMN_NAME_SIZE_WEIGHT);
        final TextCellEditor cellEditor = new TextCellEditor(tableViewerCreatorForGlobalMap.getTable());
        column.setCellEditor(cellEditor);

        // Column with remove button
        column = new TableViewerCreatorColumn(tableViewerCreatorForGlobalMap);
        column.setTitle(""); //$NON-NLS-1$
        column.setDefaultDisplayedValue(""); //$NON-NLS-1$
        column.setToolTipHeader(Messages.getString("InputDataMapTableView.addNewGlobalMapVar.tooltip")); //$NON-NLS-1$
        column.setWeight(10);
        column.setWidth(25);
        column.setMoveable(false);
        column.setResizable(true);
        column.setImageHeader(org.talend.commons.ui.runtime.image.ImageProvider
                .getImage(org.talend.commons.ui.runtime.image.ImageProvider.getImageDesc(EImage.ADD_ICON)));
        column.setTableColumnSelectionListener(new DefaultHeaderColumnSelectionListener(column, tableViewerCreatorForGlobalMap) {

            /*
             * (non-Javadoc)
             *
             * @see org.eclipse.swt.events.SelectionListener#widgetSelected(org.eclipse.swt.events.SelectionEvent)
             */
            @Override
            public void widgetSelected(SelectionEvent arg0) {
                if (!mapperManager.componentIsReadOnly()) {
                    getInputTable().addGlobalMapEntry(
                            new GlobalMapEntry(getInputTable(), "\"" + findUniqueName("myKey") + "\"", "")); //$NON-NLS-1$ //$NON-NLS-2$//$NON-NLS-3$
                    updateGridDataHeightForTableGlobalMap();
                    resizeAtExpandedSize();
                }
            }

        });
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

                ITableEntry tableEntry = (ITableEntry) bean;
                if (!mapperManager.componentIsReadOnly()) {
                    boolean removeEntry = MessageDialog.openConfirm(getShell(),
                            Messages.getString("InputDataMapTableView.removeGlobalMapVar.Title"), //$NON-NLS-1$
                            Messages.getString("InputDataMapTableView.removeGlobalMapVar.Message", tableEntry.getName())); //$NON-NLS-1$
                    if (removeEntry) {
                        ExtendedTableRemoveCommand removeCommand = new ExtendedTableRemoveCommand(bean,
                                extendedTableViewerForGlobalMap.getExtendedTableModel());
                        mapperManager.removeTableEntry((ITableEntry) bean);
                        mapperManager.executeCommand(removeCommand);
                        tableViewerCreatorForGlobalMap.getTableViewer().refresh();
                        List list = tableViewerCreatorForGlobalMap.getInputList();
                        updateGridDataHeightForTableGlobalMap();
                        resizeAtExpandedSize();
                    }
                }
            }

        };
        buttonImage.setImage(ImageProvider.getImage(EImage.MINUS_ICON));
        column.setTableEditorContent(buttonImage);

    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.mapper.ui.visualmap.table.DataMapTableView#unselectAllGlobalMapEntries()
     */
    @Override
    public void unselectAllGlobalMapEntries() {
        tableViewerCreatorForGlobalMap.getSelectionHelper().deselectAll();
    }

    private void addMenuItemListener(final MenuItem menuItem) {

        menuItem.addArmListener(new ArmListener() {

            /*
             * (non-Javadoc)
             *
             * @see org.eclipse.swt.events.ArmListener#widgetArmed(org.eclipse.swt.events.ArmEvent)
             */
            public void widgetArmed(ArmEvent e) {
                IUIMenuOption data = (IUIMenuOption) menuItem.getData();
                mapperManager.getUiManager().setStatusBarValues(STATUS.INFO, data.getTooltipText());
            }

        });

    }

    private void enableDisablePersistentMode(TMAP_LOOKUP_MODE lookupMode) {
        if (mapperManager.isPersistentMap()) {
            boolean previous = getInputTable().isPersistent();
            switch (lookupMode) {
            case LOAD_ONCE:
            case LOAD_ONCE_AND_UPDATE:
            case RELOAD:
                persistentCheckEditable = true;
                getInputTable().setPersistent(previousValidPersistentMode);
                if (mapperManager.getDefaultSetting().get(PERSISTENCE_MODEL_SETTING).equals(previous)
                        && !mapperManager.getDefaultSetting().get(PERSISTENCE_MODEL_SETTING).equals(previousValidPersistentMode)) {
                    if (changedOptions < 4) {
                        changedOptions++;
                    }
                }

                break;
            case CACHE_OR_RELOAD:
                persistentCheckEditable = false;

                getInputTable().setPersistent(false);
                if (!mapperManager.getDefaultSetting().get(PERSISTENCE_MODEL_SETTING).equals(previous)) {
                    if (changedOptions > 0) {
                        changedOptions--;
                    }
                }

                break;
            default:
                break;
            }
        }
    }

    @Override
    protected Object openCustomCellDialog(Shell shell, CellValueType type) {
        if (type == CellValueType.LOOKUP_MODEL) {
            IUILookupMode[] availableJoins = { TMAP_LOOKUP_MODE.LOAD_ONCE, TMAP_LOOKUP_MODE.RELOAD,
                    TMAP_LOOKUP_MODE.CACHE_OR_RELOAD };
            List<String> names = new ArrayList<String>();
            for (IUILookupMode availableJoin : availableJoins) {
                names.add(availableJoin.getLabel());
            }
            ListStringValueDialog<String> dialog = new ListStringValueDialog<String>(shell, names);
            if (dialog.open() == IDialogConstants.OK_ID) {
                return dialog.getSelectStr();
            }
        } else if (type == CellValueType.MATCH_MODEL) {
            IUIMatchingMode[] matchModel = getMatchModel();
            List<String> names = new ArrayList<String>();
            for (IUIMatchingMode element : matchModel) {
                names.add(element.getLabel());
            }
            ListStringValueDialog<String> dialog = new ListStringValueDialog<String>(shell, names);
            if (dialog.open() == IDialogConstants.OK_ID) {
                return dialog.getSelectStr();
            }
        } else if (type == CellValueType.JOIN_MODEL) {
            String[] items = new String[] { INNER_JOIN, LEFT_OUTER_JOIN };
            ListStringValueDialog<String> dialog = new ListStringValueDialog<String>(shell, items);
            if (dialog.open() == IDialogConstants.OK_ID) {
                return dialog.getSelectStr();
            }
        }

        return null;
    }

    /**
     *
     *
     * $Id: TableCellModifier.java
     *
     */
    class TableCellModifier extends DefaultCellModifier {

        /**
         * DOC talend TableCellModifier constructor comment.
         *
         * @param tableViewerCreator
         */
        public TableCellModifier(TableViewerCreator tableViewerCreator) {
            super(tableViewerCreator);
        }

        @Override
        public boolean canModify(Object bean, String idColumn) {
            if (bean instanceof GlobalMapEntry) {
                GlobalMapEntry column = (GlobalMapEntry) bean;
                if (column.getParent() instanceof InputTable) {
                    if (JOIN_MODEL_SETTING.equals(column.getName())) {
                        return true;
                    } else if (PERSISTENCE_MODEL_SETTING.equals(column.getName())) {
                        return persistentCheckEditable;
                    }

                }
            }

            return true;
        }

    }

    /**
     * Manage to find a unique name with the given name.
     *
     * @param titleName
     */
    @Override
    public String findUniqueName(String baseName) {
        if (baseName == null) {
            throw new IllegalArgumentException(Messages.getString("InputDataMapTableView.baseNameCannotNull")); //$NON-NLS-1$
        }
        String uniqueName = baseName + 1;

        int counter = 1;
        boolean exists = true;
        while (exists) {
            exists = !checkValidName(uniqueName);
            if (!exists) {
                break;
            }
            uniqueName = baseName + counter++;
        }
        return uniqueName;
    }

    /**
     * Check if the given name will be unique in the process. If already exists with that name, false will be returned.
     *
     * @param uniqueName
     * @return true if the name is unique
     */
    public boolean checkValidName(String name) {
        for (ITableEntry entry : getInputTable().getGlobalMapEntries()) {
            if (TalendQuoteUtils.removeQuotesIfExist(entry.getName()).equals(name)) {
                return false;
            }
        }
        Perl5Matcher matcher = new Perl5Matcher();
        Perl5Compiler compiler = new Perl5Compiler();
        Pattern pattern;
        try {
            pattern = compiler.compile("^[A-Za-z_][A-Za-z0-9_]*$"); //$NON-NLS-1$
            if (!matcher.matches(name, pattern)) {
                return false;
            }
        } catch (MalformedPatternException e) {
            throw new RuntimeException(e);
        }
        return true;
    }
}
