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

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.ToolItem;
import org.talend.commons.ui.runtime.exception.ExceptionHandler;
import org.talend.commons.ui.runtime.image.EImage;
import org.talend.commons.ui.runtime.image.ImageProvider;
import org.talend.commons.ui.runtime.swt.tableviewer.behavior.CellEditorValueAdapter;
import org.talend.commons.ui.runtime.swt.tableviewer.celleditor.ExtendedComboBoxCellEditor;
import org.talend.commons.ui.runtime.swt.tableviewer.celleditor.ExtendedSimpleTextCellEditor;
import org.talend.commons.ui.runtime.swt.tableviewer.celleditor.ExtendedTextCellEditor;
import org.talend.commons.ui.runtime.swt.tableviewer.data.ModifiedObjectInfo;
import org.talend.commons.ui.runtime.swt.tableviewer.selection.ILineSelectionListener;
import org.talend.commons.ui.runtime.swt.tableviewer.selection.LineSelectionEvent;
import org.talend.commons.ui.runtime.ws.WindowSystem;
import org.talend.commons.ui.swt.advanced.dataeditor.commands.ExtendedTableMoveCommand;
import org.talend.commons.ui.swt.extended.table.AbstractExtendedTableViewer;
import org.talend.commons.ui.swt.tableviewer.TableViewerCreator;
import org.talend.commons.ui.swt.tableviewer.TableViewerCreator.CELL_EDITOR_STATE;
import org.talend.commons.ui.swt.tableviewer.TableViewerCreatorColumn;
import org.talend.commons.ui.swt.tableviewer.celleditor.DialogErrorForCellEditorListener;
import org.talend.commons.ui.swt.tableviewer.tableeditor.CheckboxTableEditorContent;
import org.talend.commons.utils.data.bean.IBeanPropertyAccessors;
import org.talend.commons.utils.data.list.IListenableListListener;
import org.talend.commons.utils.data.list.ListenableListEvent;
import org.talend.core.language.ECodeLanguage;
import org.talend.core.model.metadata.MetadataTalendType;
import org.talend.core.model.metadata.types.JavaTypesManager;
import org.talend.core.model.metadata.types.PerlTypesManager;
import org.talend.core.ui.metadata.celleditor.JavaTypeComboValueAdapter;
import org.talend.designer.abstractmap.model.table.IDataMapTable;
import org.talend.designer.abstractmap.model.tableentry.IColumnEntry;
import org.talend.designer.abstractmap.model.tableentry.ITableEntry;
import org.talend.designer.mapper.i18n.Messages;
import org.talend.designer.mapper.language.ILanguage;
import org.talend.designer.mapper.language.LanguageProvider;
import org.talend.designer.mapper.managers.MapperManager;
import org.talend.designer.mapper.managers.UIManager;
import org.talend.designer.mapper.model.table.VarsTable;
import org.talend.designer.mapper.model.tableentry.VarTableEntry;
import org.talend.designer.mapper.ui.visualmap.zone.Zone;

/**
 * DOC amaumont class global comment. Detailled comment <br/>
 *
 * $Id$
 *
 */
public class VarsDataMapTableView extends DataMapTableView {

    protected ToolItem removeEntryItem;

    protected ToolItem moveUpEntryItem;

    protected ToolItem moveDownEntryItem;

    public VarsDataMapTableView(Composite parent, int style, VarsTable abstractDataMapTable, MapperManager mapperManager) {
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
    }

    @Override
    protected void addListeners() {
        super.addListeners();

        tableViewerCreatorForColumns.getSelectionHelper().addAfterSelectionListener(new ILineSelectionListener() {

            public void handle(LineSelectionEvent e) {
                if (!getMapperManager().componentIsReadOnly()) {
                    boolean atLeastOneItemIsSelected = tableViewerCreatorForColumns.getTable().getSelectionCount() > 0;
                    removeEntryItem.setEnabled(atLeastOneItemIsSelected);
                    moveUpEntryItem.setEnabled(atLeastOneItemIsSelected);
                    moveDownEntryItem.setEnabled(atLeastOneItemIsSelected);
                }
            }

        });

        getExtendedTableViewerForColumns().getExtendedTableModel().addAfterOperationListListener(new IListenableListListener() {

            public void handleEvent(ListenableListEvent event) {
                if (event.type == ListenableListEvent.TYPE.SWAPED) {
                    DataMapTableView varsDataMapTableView = mapperManager
                            .retrieveDataMapTableView(getExtendedTableViewerForColumns().getTable());
                    UIManager uiManager = mapperManager.getUiManager();
                    uiManager.parseAllExpressions(varsDataMapTableView, false);
                    mapperManager.getProblemsManager().checkProblemsForAllEntries(varsDataMapTableView, true);
                    uiManager.refreshBackground(true, false);
                    List<ITableEntry> list = uiManager.extractSelectedTableEntries(varsDataMapTableView
                            .getTableViewerCreatorForColumns().getTableViewer().getSelection());

                    uiManager.selectLinks(varsDataMapTableView, list, false, false);
                }
            }

        });

    }

    private ExtendedTextCellEditor expressionCellEditor;

    private ExtendedComboBoxCellEditor comboBoxCellEditor;

    private ExtendedSimpleTextCellEditor textCellEditor;

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.mapper.ui.visualmap.table.DataMapTableView#notifyFocusLost()
     */
    @Override
    public void notifyFocusLost() {
        expressionCellEditor.focusLost();
        comboBoxCellEditor.focusLost();
        textCellEditor.focusLost();
    }

    @Override
    public void initColumnsOfTableColumns(final TableViewerCreator tableViewerCreatorForColumns) {

        ECodeLanguage codeLanguage = LanguageProvider.getCurrentLanguage().getCodeLanguage();

        TableViewerCreatorColumn column = new TableViewerCreatorColumn(tableViewerCreatorForColumns);
        column.setTitle(Messages.getString("VarsDataMapTableView.columnTitle.expression")); //$NON-NLS-1$
        column.setId(DataMapTableView.ID_EXPRESSION_COLUMN);
        expressionCellEditor = createExpressionCellEditor(tableViewerCreatorForColumns, column, new Zone[] { Zone.INPUTS,
                Zone.VARS }, false);
        column.setBeanPropertyAccessors(new IBeanPropertyAccessors<VarTableEntry, String>() {

            public String get(VarTableEntry bean) {
                return bean.getExpression();
            }

            public void set(VarTableEntry bean, String value) {
                bean.setExpression(value);
                mapperManager.getProblemsManager().checkProblemsForTableEntry(bean, true);
            }

        });
        column.setModifiable(!mapperManager.componentIsReadOnly());
        column.setDefaultInternalValue(""); //$NON-NLS-1$
        if (codeLanguage == ECodeLanguage.JAVA) {
            column.setWeight(40);
        } else {
            column.setWeight(COLUMN_EXPRESSION_SIZE_WEIGHT);
        }

        if (codeLanguage == ECodeLanguage.JAVA) {

            String[] arrayTalendTypes = new String[0];
            try {
                arrayTalendTypes = MetadataTalendType.getTalendTypesLabels();
                arrayTalendTypes = this.talendTypeFilter.filter(arrayTalendTypes);
            } catch (NoClassDefFoundError e) {
                // shouln't be happend
                // e.printStackTrace();
                ExceptionHandler.process(e);
            } catch (ExceptionInInitializerError e) {
                // shouln't be happend
                // e.printStackTrace();
                ExceptionHandler.process(e);
            }

            IBeanPropertyAccessors<VarTableEntry, Boolean> nullableAccessors = new IBeanPropertyAccessors<VarTableEntry, Boolean>() {

                public Boolean get(VarTableEntry bean) {
                    return bean.isNullable() ? Boolean.TRUE : Boolean.FALSE;
                }

                public void set(VarTableEntry bean, Boolean value) {
                    bean.setNullable(value.booleanValue());
                }

            };

            CellEditorValueAdapter comboValueAdapter = new JavaTypeComboValueAdapter(JavaTypesManager.getDefaultJavaType(),
                    nullableAccessors, this.talendTypeFilter);

            column = new TableViewerCreatorColumn(tableViewerCreatorForColumns);
            column.setTitle(Messages.getString("VarsDataMapTableView.columnTitle.type")); //$NON-NLS-1$
            column.setBeanPropertyAccessors(new IBeanPropertyAccessors<VarTableEntry, String>() {

                public String get(VarTableEntry bean) {
                    return bean.getType();
                }

                public void set(VarTableEntry bean, String value) {
                    bean.setType(value);
                    mapperManager.getProblemsManager().checkProblemsForAllEntriesOfAllTables(true);
                }

            });
            comboBoxCellEditor = new ExtendedComboBoxCellEditor(tableViewerCreatorForColumns.getTable(), arrayTalendTypes);
            column.setModifiable(!mapperManager.componentIsReadOnly());
            column.setWeight(18);
            column.setCellEditor(comboBoxCellEditor, comboValueAdapter);

            column = new TableViewerCreatorColumn(tableViewerCreatorForColumns);
            column.setTitle("Nullable"); //$NON-NLS-1$
            column.setBeanPropertyAccessors(new IBeanPropertyAccessors<VarTableEntry, Boolean>() {

                public Boolean get(VarTableEntry bean) {
                    return bean.isNullable();
                }

                public void set(VarTableEntry bean, Boolean value) {
                    bean.setNullable(value);
                    mapperManager.getProblemsManager().checkProblemsForAllEntriesOfAllTables(true);
                }

            });
            column.setModifiable(!mapperManager.componentIsReadOnly());
            column.setWidth(WindowSystem.isWIN32() ? 12 : 20);
            column.setDisplayedValue(""); //$NON-NLS-1$
            column.setResizable(false);
            CheckboxTableEditorContent checkboxTableEditorContent = new CheckboxTableEditorContent();
            String nullable = Messages.getString("VarsDataMapTableView.nullable"); //$NON-NLS-1$
            checkboxTableEditorContent.setToolTipText(nullable);
            column.setTableEditorContent(checkboxTableEditorContent);
            column.setToolTipHeader(nullable);

        }

        column = new TableViewerCreatorColumn(tableViewerCreatorForColumns);
        column.setTitle(Messages.getString("VarsDataMapTableView.columnTitle.variable")); //$NON-NLS-1$
        column.setId(DataMapTableView.ID_NAME_COLUMN);
        column.setBeanPropertyAccessors(new IBeanPropertyAccessors<VarTableEntry, String>() {

            public String get(VarTableEntry bean) {
                return bean.getName();
            }

            public void set(VarTableEntry bean, String value) {
                bean.setName(value);
            }

        });
        column.setModifiable(!mapperManager.componentIsReadOnly());
        if (codeLanguage == ECodeLanguage.JAVA) {
            column.setWeight(25);
        } else {
            column.setWeight(COLUMN_NAME_SIZE_WEIGHT);
        }
        textCellEditor = new ExtendedSimpleTextCellEditor(tableViewerCreatorForColumns.getTable());
        textCellEditor.addListener(new DialogErrorForCellEditorListener(textCellEditor, column) {

            @Override
            public void newValidValueTyped(int itemIndex, Object previousValue, Object newValue, CELL_EDITOR_STATE state) {
                if (state == CELL_EDITOR_STATE.APPLYING) {
                    ModifiedObjectInfo modifiedObjectInfo = tableViewerCreatorForColumns.getModifiedObjectInfo();
                    String originalValue = (String) modifiedObjectInfo.getOriginalPropertyBeanValue();
                    Object currentModifiedBean = modifiedObjectInfo.getCurrentModifiedBean();
                    mapperManager.getUiManager().processColumnNameChanged(originalValue.toString(), newValue.toString(),
                            VarsDataMapTableView.this, (ITableEntry) currentModifiedBean);
                }
            }

            @Override
            public String validateValue(String newValue, int beanPosition) {
                return ((VarsTable) getDataMapTable()).validateColumnName(newValue, beanPosition);
            }

        });
        column.setCellEditor(textCellEditor);

    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.mapper.ui.visualmap.table.DataMapTableView#initTableConstraints()
     */
    @Override
    protected void initExtraTable() {
        // no table constraint
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.mapper.ui.visualmap.table.DataMapTableView#addEntriesActionsComponents()
     */
    @Override
    protected boolean addToolItems() {
        createToolItems();
        return true;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.mapper.ui.visualmap.table.DataMapTableView#getZone()
     */
    @Override
    public Zone getZone() {
        return Zone.VARS;
    }

    @Override
    public void unselectAllColumnEntries() {
        super.unselectAllColumnEntries();
        if (removeEntryItem != null) {
            removeEntryItem.setEnabled(false);
            moveUpEntryItem.setEnabled(false);
            moveDownEntryItem.setEnabled(false);
        }
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

    @Override
    protected void createToolItems() {

        // /////////////////////////////////////////////////////////////////
        ToolItem addEntryItem = new ToolItem(toolBarActions, SWT.PUSH);
        addEntryItem.setEnabled(!getMapperManager().componentIsReadOnly());
        addEntryItem.setToolTipText(Messages.getString("VarsDataMapTableView.entryItemTooltip.addVariable")); //$NON-NLS-1$
        addEntryItem.setImage(org.talend.commons.ui.runtime.image.ImageProvider
                .getImage(org.talend.commons.ui.runtime.image.ImageProvider.getImageDesc(EImage.ADD_ICON)));

        addEntryItem.addSelectionListener(new SelectionListener() {

            public void widgetDefaultSelected(SelectionEvent e) {
            }

            public void widgetSelected(SelectionEvent e) {

                TableViewerCreator<IColumnEntry> tableViewerCreator = getExtendedTableViewerForColumns().getTableViewerCreator();

                tableViewerCreator.applyActivatedCellEditor();

                Table table = getExtendedTableViewerForColumns().getTable();

                int[] indices = table.getSelectionIndices();
                int indexInsert = table.getItemCount();
                if (indices.length > 0) {
                    indexInsert = indices[indices.length - 1] + 1;
                }
                IDataMapTable dataMapTable = VarsDataMapTableView.this.getDataMapTable();
                String varName = null;
                if (dataMapTable instanceof VarsTable) {
                    varName = ((VarsTable) dataMapTable).findUniqueColumnName("var"); //$NON-NLS-1$
                } else {
                    throw new UnsupportedOperationException(Messages
                            .getString("VarsDataMapTableView.exceptionMessage.caseNotFound")); //$NON-NLS-1$
                }

                ILanguage currentLanguage = LanguageProvider.getCurrentLanguage();

                String type = null;
                if (currentLanguage.getCodeLanguage() == ECodeLanguage.JAVA) {
                    type = JavaTypesManager.STRING.getId();
                } else {
                    // fix bug 0018996 ,when add a var row,there is no default type,this bug exsit for long time
                    type = PerlTypesManager.STRING;
                }

                mapperManager.addNewVarEntry(VarsDataMapTableView.this, varName, indexInsert, type);
                tableViewerCreatorForColumns.getTableViewer().refresh();
                if (canBeResizedAtPreferedSize()) {
                    VarsDataMapTableView.this.changeSize(VarsDataMapTableView.this.getPreferredSize(true, true, false), true,
                            true);
                    changeMinimizeState(false);
                }
                mapperManager.getUiManager().refreshBackground(true, false);
                table.setSelection(indexInsert);
                removeEntryItem.setEnabled(true);
                moveUpEntryItem.setEnabled(true);
                moveDownEntryItem.setEnabled(true);
            }

        });
        // /////////////////////////////////////////////////////////////////

        // /////////////////////////////////////////////////////////////////
        removeEntryItem = new ToolItem(toolBarActions, SWT.PUSH);
        removeEntryItem.setEnabled(false);
        removeEntryItem.setImage(org.talend.commons.ui.runtime.image.ImageProvider
                .getImage(org.talend.commons.ui.runtime.image.ImageProvider.getImageDesc(EImage.MINUS_ICON)));
        removeEntryItem.setToolTipText(Messages.getString("VarsDataMapTableView.entryItemTooltip.removeVariable")); //$NON-NLS-1$

        removeEntryItem.addSelectionListener(new SelectionListener() {

            public void widgetDefaultSelected(SelectionEvent e) {
            }

            public void widgetSelected(SelectionEvent e) {
                IStructuredSelection selection = (IStructuredSelection) tableViewerCreatorForColumns.getTableViewer()
                        .getSelection();
                List<ITableEntry> selectedBeans = selection.toList();

                for (ITableEntry entry : selectedBeans) {
                    mapperManager.removeTableEntry(entry);
                }
                if (selectedBeans.size() > 0) {
                    tableViewerCreatorForColumns.getTableViewer().refresh();
                    mapperManager.getUiManager().refreshBackground(true, false);
                    if (canBeResizedAtPreferedSize()) {
                        resizeAtExpandedSize();
                    }
                }
                removeEntryItem.setEnabled(false);
            }

        });
        // /////////////////////////////////////////////////////////////////

        // /////////////////////////////////////////////////////////////////
        moveUpEntryItem = new ToolItem(toolBarActions, SWT.PUSH);
        moveUpEntryItem.setEnabled(false);
        moveUpEntryItem.setImage(ImageProvider.getImage(EImage.UP_ICON));
        moveUpEntryItem.setToolTipText(Messages.getString("VarsDataMapTableView.entryItemTooltip.moveUpVariable")); //$NON-NLS-1$

        moveUpEntryItem.addSelectionListener(new SelectionListener() {

            public void widgetDefaultSelected(SelectionEvent e) {
            }

            public void widgetSelected(SelectionEvent e) {

                AbstractExtendedTableViewer viewer = getExtendedTableViewerForColumns();

                ExtendedTableMoveCommand moveCommand = new ExtendedTableMoveCommand(viewer.getExtendedTableModel(), true, viewer
                        .getTableViewerCreator().getTable().getSelectionIndices());

                viewer.getTableViewerCreator().applyActivatedCellEditor();

                viewer.executeCommand(moveCommand);
            }

        });
        // /////////////////////////////////////////////////////////////////

        // /////////////////////////////////////////////////////////////////
        moveDownEntryItem = new ToolItem(toolBarActions, SWT.PUSH);
        moveDownEntryItem.setEnabled(false);
        moveDownEntryItem.setImage(ImageProvider.getImage(EImage.DOWN_ICON));
        moveDownEntryItem.setToolTipText(Messages.getString("VarsDataMapTableView.entryItemTooltip.movedownVariable")); //$NON-NLS-1$

        moveDownEntryItem.addSelectionListener(new SelectionListener() {

            public void widgetDefaultSelected(SelectionEvent e) {
            }

            public void widgetSelected(SelectionEvent e) {

                AbstractExtendedTableViewer viewer = getExtendedTableViewerForColumns();

                ExtendedTableMoveCommand moveCommand = new ExtendedTableMoveCommand(viewer.getExtendedTableModel(), false, viewer
                        .getTableViewerCreator().getTable().getSelectionIndices());

                viewer.getTableViewerCreator().applyActivatedCellEditor();

                viewer.executeCommand(moveCommand);
            }

        });
        // /////////////////////////////////////////////////////////////////

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
        throw new UnsupportedOperationException();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.mapper.ui.visualmap.table.DataMapTableView#loaded()
     */
    @Override
    public void loaded() {
        super.loaded();
    }

    @Override
    public String findUniqueName(String baseName) {
        return "\"\""; //$NON-NLS-1$
    }
}
