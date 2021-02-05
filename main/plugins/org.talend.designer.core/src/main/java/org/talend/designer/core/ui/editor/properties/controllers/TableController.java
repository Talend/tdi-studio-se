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
package org.talend.designer.core.ui.editor.properties.controllers;

import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.eclipse.gef.commands.Command;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ComboBoxCellEditor;
import org.eclipse.jface.viewers.ICellEditorListener;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Table;
import org.eclipse.ui.views.properties.tabbed.ITabbedPropertyConstants;
import org.talend.commons.ui.runtime.swt.tableviewer.TableViewerCreatorColumnNotModifiable;
import org.talend.commons.ui.swt.advanced.dataeditor.control.ExtendedPushButton;
import org.talend.commons.ui.swt.extended.table.ExtendedTableModel;
import org.talend.commons.ui.swt.tableviewer.TableViewerCreator;
import org.talend.commons.utils.data.list.IListenableListListener;
import org.talend.commons.utils.data.list.ListenableListEvent;
import org.talend.core.CorePlugin;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.ITDQPatternService;
import org.talend.core.model.process.EConnectionType;
import org.talend.core.model.process.EParameterFieldType;
import org.talend.core.model.process.IConnection;
import org.talend.core.model.process.IContext;
import org.talend.core.model.process.IContextManager;
import org.talend.core.model.process.IContextParameter;
import org.talend.core.model.process.IElement;
import org.talend.core.model.process.IElementParameter;
import org.talend.core.model.properties.ProcessItem;
import org.talend.core.model.utils.TalendTextUtils;
import org.talend.core.ui.properties.tab.IDynamicProperty;
import org.talend.designer.core.IDesignerCoreService;
import org.talend.designer.core.model.FakeElement;
import org.talend.designer.core.model.components.EParameterName;
import org.talend.designer.core.ui.editor.cmd.PropertyChangeCommand;
import org.talend.designer.core.ui.editor.connections.Connection;
import org.talend.designer.core.ui.editor.nodes.Node;
import org.talend.designer.core.ui.editor.properties.macrowidgets.tableeditor.PropertiesTableEditorModel;
import org.talend.designer.core.ui.editor.properties.macrowidgets.tableeditor.PropertiesTableEditorView;
import org.talend.designer.core.ui.editor.properties.macrowidgets.tableeditor.PropertiesTableToolbarEditorView;
import org.talend.designer.runprocess.ItemCacheManager;

/**
 * DOC yzhang class global comment. Detailled comment <br/>
 *
 * $Id: TableController.java 1 2006-12-14 下午05:44:30 +0000 (下午05:44:30) yzhang $
 *
 */
public class TableController extends AbstractElementPropertySectionController {

    /**
     *
     */
    private static final int MIN_NUMBER_ROWS = 1;

    protected static final String TOOLBAR_NAME = "_TABLE_VIEW_TOOLBAR_NAME_"; //$NON-NLS-1$

    private ITDQPatternService dqPatternService = null;

    /**
     * DOC yzhang TableController constructor comment.
     *
     * @param dtp
     */
    public TableController(IDynamicProperty dp) {
        super(dp);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.core.ui.editor.properties2.editors.AbstractElementPropertySectionController#
     * createControl(org.eclipse.swt.widgets.Composite, org.talend.core.model.process.IElementParameter, int, int, int,
     * org.eclipse.swt.widgets.Control)
     */
    @Override
    public Control createControl(final Composite parentComposite, final IElementParameter param, final int numInRow,
            final int nbInRow, int top, final Control lastControlPrm) {
        this.curParameter = param;
        this.paramFieldType = param.getFieldType();
        final Composite container = parentComposite;

        PropertiesTableEditorModel<Map<String, Object>> tableEditorModel = new PropertiesTableEditorModel<Map<String, Object>>();

        tableEditorModel.setData(elem, param, getProcess(elem, part));
        PropertiesTableEditorView<Map<String, Object>> tableEditorView = new PropertiesTableEditorView<Map<String, Object>>(
                parentComposite, SWT.NONE, tableEditorModel, !param.isBasedOnSchema(), false);
        tableEditorView.getExtendedTableViewer().setCommandStack(getCommandStack());
        boolean editable = !param.isReadOnly() && (elem instanceof FakeElement || !param.isRepositoryValueUsed());
        tableEditorView.setReadOnly(!editable);
        tableEditorModel.setModifiedBeanListenable(tableEditorView.getTableViewerCreator());
        tableEditorModel.addModifiedBeanListenerForAggregateComponent();

        final Table table = tableEditorView.getTable();

        table.setToolTipText(VARIABLE_TOOLTIP + param.getVariableName());
        
        ExtendedTableModel<Map<String, Object>> extendedTableModel = tableEditorView.getExtendedTableModel();
        if (extendedTableModel != null) {
            TableViewer tableViewer = extendedTableModel.getTableViewer();
            if (tableViewer != null) {
                CellEditor[] cellEditors = tableViewer.getCellEditors();
                if (cellEditors != null && cellEditors.length > 0) {
                    for (CellEditor c : cellEditors) {
                        if (c != null) {
                            c.addListener(new ICellEditorListener() {

                                @Override
                                public void editorValueChanged(boolean oldValidState, boolean newValidState) {
                                }

                                @Override
                                public void applyEditorValue() {
                                    if (elem != null) {
                                        Object propertyValue = elem.getPropertyValue("DRIVER_JAR_IMPLICIT_CONTEXT");
                                        if (propertyValue != null) {
                                            Command cmd = new PropertyChangeCommand(elem, "DRIVER_JAR_IMPLICIT_CONTEXT",
                                                    propertyValue);
                                            executeCommand(cmd);
                                        }
                                    }
                                }

                                @Override
                                public void cancelEditor() {

                                }
                            });
                        }
                    }
                }

            }

        }

        
        
        // add listener to tableMetadata (listen the event of the toolbars)
        tableEditorView.getExtendedTableModel().addAfterOperationListListener(new IListenableListListener() {

            @Override
            public void handleEvent(ListenableListEvent event) {
                if (elem instanceof Node) {
                    Node node = (Node) elem;
                    node.checkAndRefreshNode();
                }
            }
        });
        final Composite mainComposite = tableEditorView.getMainComposite();

        CLabel labelLabel2 = getWidgetFactory().createCLabel(container, param.getDisplayName());
        FormData formData = new FormData();
        if (lastControlPrm != null) {
            formData.left = new FormAttachment(lastControlPrm, 0);
        } else {
            formData.left = new FormAttachment((((numInRow - 1) * MAX_PERCENT) / nbInRow), 0);
        }
        formData.top = new FormAttachment(0, top);
        labelLabel2.setLayoutData(formData);
        if (numInRow != 1) {
            labelLabel2.setAlignment(SWT.RIGHT);
        }
        // *********************
        formData = new FormData();
        int currentLabelWidth2 = STANDARD_LABEL_WIDTH;
        GC gc2 = new GC(labelLabel2);
        Point labelSize2 = gc2.stringExtent(param.getDisplayName());
        gc2.dispose();

        boolean needOffset = true;
        if ((labelSize2.x + ITabbedPropertyConstants.HSPACE) > currentLabelWidth2) {
            currentLabelWidth2 = labelSize2.x + ITabbedPropertyConstants.HSPACE;
            needOffset = false;
        }

        int tableHorizontalOffset = -5;
        if (numInRow == 1) {
            if (lastControlPrm != null) {
                if (needOffset) {
                    formData.left = new FormAttachment(lastControlPrm, currentLabelWidth2 + tableHorizontalOffset);
                } else {
                    formData.left = new FormAttachment(lastControlPrm, currentLabelWidth2);
                }
            } else {
                if (needOffset) {
                    formData.left = new FormAttachment(0, currentLabelWidth2 + tableHorizontalOffset);
                } else {
                    formData.left = new FormAttachment(0, currentLabelWidth2);
                }
            }
        } else {
            formData.left = new FormAttachment(labelLabel2, 0 + tableHorizontalOffset, SWT.RIGHT);
        }
        formData.right = new FormAttachment((numInRow * MAX_PERCENT) / nbInRow, 0);
        formData.top = new FormAttachment(0, top);

        int toolbarSize = 0;
        if (!param.isBasedOnSchema()) {
            Point size = tableEditorView.getExtendedToolbar().getToolbar().computeSize(SWT.DEFAULT, SWT.DEFAULT);
            toolbarSize = size.y + 5;
        }
        int currentHeightEditor = table.getHeaderHeight() + ((List) param.getValue()).size() * table.getItemHeight()
                + table.getItemHeight() + toolbarSize;
        int minHeightEditor = table.getHeaderHeight() + getNumberLines(param) * table.getItemHeight() + table.getItemHeight()
                + toolbarSize;
        int ySize2 = Math.max(currentHeightEditor, minHeightEditor);

        ySize2 = Math.min(ySize2, 500);
        formData.bottom = new FormAttachment(0, top + ySize2);
        mainComposite.setLayoutData(formData);

        hashCurControls.put(param.getName(), tableEditorView.getExtendedTableViewer().getTableViewerCreator());
        hashCurControls.put(TOOLBAR_NAME, tableEditorView.getToolBar());
        updateTableValues(param);

        this.dynamicProperty.setCurRowSize(ySize2 + ITabbedPropertyConstants.VSPACE);

        if (isInWizard()) {
            labelLabel2.setAlignment(SWT.RIGHT);
            if (lastControlPrm != null) {
                formData.right = new FormAttachment(lastControlPrm, 0);
            } else {
                formData.right = new FormAttachment(100, -ITabbedPropertyConstants.HSPACE);
            }
            formData.left = new FormAttachment((((nbInRow - numInRow) * MAX_PERCENT) / nbInRow), currentLabelWidth2
                    + ITabbedPropertyConstants.HSPACE);

            formData = (FormData) labelLabel2.getLayoutData();
            formData.right = new FormAttachment(mainComposite, 0);
            formData.left = new FormAttachment((((nbInRow - numInRow) * MAX_PERCENT) / nbInRow), 0);

            return labelLabel2;
        }

        return mainComposite;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.talend.designer.core.ui.editor.properties.controllers.AbstractElementPropertySectionController#estimateRowSize
     * (org.eclipse.swt.widgets.Composite, org.talend.core.model.process.IElementParameter)
     */
    @Override
    public int estimateRowSize(Composite subComposite, IElementParameter param) {
        PropertiesTableEditorModel<Map<String, Object>> tableEditorModel = new PropertiesTableEditorModel<Map<String, Object>>();

        updateTableValues(param);

        tableEditorModel.setData(elem, param, part.getProcess());
        PropertiesTableEditorView<Map<String, Object>> tableEditorView = new PropertiesTableEditorView<Map<String, Object>>(
                subComposite, SWT.NONE, tableEditorModel, !param.isBasedOnSchema(), false);
        tableEditorView.getExtendedTableViewer().setCommandStack(getCommandStack());
        tableEditorView.setReadOnly(param.isReadOnly());
        final Table table = tableEditorView.getTable();
        int toolbarSize = 0;
        if (!param.isBasedOnSchema()) {
            Point size = tableEditorView.getExtendedToolbar().getToolbar().computeSize(SWT.DEFAULT, SWT.DEFAULT);
            toolbarSize = size.y + 5;
        }
        int currentHeightEditor = table.getHeaderHeight() + ((List) param.getValue()).size() * table.getItemHeight()
                + table.getItemHeight() + toolbarSize;
        int minHeightEditor = table.getHeaderHeight() + getNumberLines(param) * table.getItemHeight() + table.getItemHeight()
                + toolbarSize;

        tableEditorView.getMainComposite().dispose();

        int ySize2 = Math.max(currentHeightEditor, minHeightEditor);
        return ySize2 + ITabbedPropertyConstants.VSPACE;
    }

    /**
     * ftang Comment method "getNumberRows".
     *
     * @param param
     * @return
     */
    protected final int getNumberLines(IElementParameter param) {
        int numlines = param.getNbLines();
        return numlines < MIN_NUMBER_ROWS ? MIN_NUMBER_ROWS : numlines;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
     */
    @Override
    public void propertyChange(PropertyChangeEvent arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public void refresh(IElementParameter param, boolean check) {
        TableViewerCreator tableViewerCreator = (TableViewerCreator) hashCurControls.get(param.getName());
        if (tableViewerCreator == null || tableViewerCreator.getTable() == null || tableViewerCreator.getTable().isDisposed()) {
            return;
        }
        updateContextList(param);
        Object value = param.getValue();
        if (value instanceof List) {
            // updateTableValues(param);
            // (bug 5365)
            checkAndSetDefaultValue(param);
            if (tableViewerCreator != null) {
                if (!tableViewerCreator.getInputList().equals(value)) {
                    tableViewerCreator.init((List) value);
                }
                tableViewerCreator.getTableViewer().refresh();
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void checkAndSetDefaultValue(IElementParameter param) {
        if (param != null && param.getFieldType() == EParameterFieldType.TABLE) {
            updateColumnList(param);

            Object[] itemsValue = param.getListItemsValue();
            if (itemsValue != null && param.getValue() != null && param.getValue() instanceof List) {
                List<Map<String, Object>> values = (List<Map<String, Object>>) param.getValue();
                for (Object element : itemsValue) {
                    if (element instanceof IElementParameter) {
                        IElementParameter columnParam = (IElementParameter) element;
                        if (columnParam.getFieldType() == EParameterFieldType.COLUMN_LIST
                                || columnParam.getFieldType() == EParameterFieldType.PREV_COLUMN_LIST
                                || columnParam.getFieldType() == EParameterFieldType.LOOKUP_COLUMN_LIST) {
                            for (Map<String, Object> columnMap : values) {
                                Object column = columnMap.get(columnParam.getName());
                                if (column == null || "".equals(column)) { //$NON-NLS-1$
                                    columnMap.put(columnParam.getName(), columnParam.getDefaultClosedListValue());
                                }
                                if (columnParam.getListItemsValue() != null) {
                                    // @see bug 5433(Display and value is not match.)
                                    if (!Arrays.asList(columnParam.getListItemsValue()).contains(column)) {
                                        columnMap.put(columnParam.getName(), columnParam.getDefaultClosedListValue());
                                    }
                                }
                            }
                        }

                        if (columnParam.getFieldType() == EParameterFieldType.CLOSED_LIST) {
                            overideDQPatternList(columnParam);
                        }
                    }
                }
            }
        }
    }

    /**
     * Overide default pattern list value by them which comes from DQ repository view
     *
     * @param param the element parameter
     * @param dqPatternService extended service for DQ pattern retrievement.
     * @return
     */
    private void overideDQPatternList(IElementParameter param) {
        // For dq patterns
        if (isDQPatternList(param)) {
            if (dqPatternService == null) { // get pattern service
                dqPatternService = getDQPatternService();
            }
            if (dqPatternService != null && elem instanceof Node) {
                Node node = (Node) elem;
                IElementParameter typeParam = node.getElementParameter("TYPE"); //$NON-NLS-1$
                // Customized value
                Object[] customizedValue = param.getListItemsValue();
                String[] customizedDisplayCodeName = param.getListItemsDisplayCodeName();
                String[] customizedDisplayName = param.getListItemsDisplayName();
                String[] customizedNotShowIfs = param.getListItemsNotShowIf();
                String[] customizedShowIfs = param.getListItemsShowIf();
                dqPatternService.overridePatternList(typeParam, param);
                // Add the customized value:
                param.setListItemsValue(mergeWithoutDuplicate(param.getListItemsValue(), customizedValue));
                param.setListItemsDisplayCodeName((String[]) mergeWithoutDuplicate(param.getListItemsDisplayCodeName(),
                        customizedDisplayCodeName));
                param.setListItemsDisplayName((String[]) mergeWithoutDuplicate(param.getListItemsDisplayName(),
                        customizedDisplayName));
                param.setListItemsNotShowIf(mergeWithDuplicate(new String[param.getListItemsShowIf().length],
                        customizedNotShowIfs));
                param.setListItemsShowIf(mergeWithDuplicate(new String[param.getListItemsShowIf().length], customizedShowIfs));
            }
        }
    }

    /**
     * Adds all the elements of "b" arrays into "a" array without the duplicate one in "a", and return "a".
     *
     * @param a
     * @param b
     * @return
     */
    private Object[] mergeWithoutDuplicate(Object[] a, Object[] b) {
        if (b == null || b.length == 0) {
            return a;
        }
        for (Object valueB : b) {
            if (!ArrayUtils.contains(a, valueB)) {
                a = ArrayUtils.add(a, valueB);
            }
        }
        return a;
    }

    private String[] mergeWithDuplicate(String[] a, String[] b) {
        if (b == null || b.length == 0) {
            return a;
        }
        for (String valueB : b) {
            a = (String[]) ArrayUtils.add(a, valueB);
        }
        return a;
    }

    private boolean isDQPatternList(IElementParameter param) {
        String paramName = param.getName();
        boolean isPatternList = StringUtils.equals(paramName, "DEFAULT_PATTERN"); //$NON-NLS-1$
        return isPatternList;

    }

    private ITDQPatternService getDQPatternService() {
        ITDQPatternService service = null;
        try {
            service = GlobalServiceRegister.getDefault().getService(ITDQPatternService.class);
        } catch (RuntimeException e) {
            // nothing to do
        }
        return service;
    }

    protected final void updateTableValues(IElementParameter param) {
        if (elem instanceof Node) {
            DbTypeListController.updateDbTypeList((Node) elem, null);
            ModuleListController.updateModuleList((Node) elem);
        } else if (elem instanceof Connection) {
            DbTypeListController.updateDbTypeList(((Connection) elem).getSource(), null);
        }
        updateColumnList(param);
        updateContextList(param);
        updateConnectionList(param);
        updateComponentList(param);
        // updateSubjobStarts(elem, param);
    }

    /**
     * DOC nrousseau Comment method "updateSubjobStarts".
     *
     * @param param
     */
    public static void updateSubjobStarts(IElement element, IElementParameter param) {
        if (!param.isBasedOnSubjobStarts() || !(element instanceof Node)) {
            return;
        }
        // Each time one link of the type SUBJOB_START_ORDER will be connected or disconnected
        // it will update the value of this table.

        List<String> uniqueNameStarts = new ArrayList<String>();

        Node node = (Node) element;
        List<IConnection> incomingSubjobStartsConn = (List<IConnection>) node.getIncomingConnections(EConnectionType.SYNCHRONIZE);
        for (IConnection connection : incomingSubjobStartsConn) {
            uniqueNameStarts.add(connection.getSource().getUniqueName());
        }

        List<Map<String, Object>> paramValues = (List<Map<String, Object>>) param.getValue();
        List<Map<String, Object>> newParamValues = new ArrayList<Map<String, Object>>();
        String[] codes = param.getListItemsDisplayCodeName();
        for (String currentUniqueNameStart : uniqueNameStarts) {
            Map<String, Object> newLine = null;
            boolean found = false;
            for (int k = 0; k < paramValues.size() && !found; k++) {
                Map<String, Object> currentLine = paramValues.get(k);
                if (currentLine.get(codes[0]).equals(currentUniqueNameStart)) {
                    found = true;
                    newLine = currentLine;
                }
            }

            if (!found) {
                newLine = TableController.createNewLine(param);
                newLine.put(codes[0], currentUniqueNameStart);
            }
            newParamValues.add(newLine);
        }

        paramValues.clear();
        paramValues.addAll(newParamValues);
    }

    public void updateColumnList(IElementParameter param) {
        if (elem instanceof Node) {
            ColumnListController.updateColumnList((Node) elem, null);
        } else if (elem instanceof Connection) {
            ColumnListController.updateColumnList(((Connection) elem).getSource(), null);
        }

        TableViewerCreator tableViewerCreator = (TableViewerCreator) hashCurControls.get(param.getName());
        Object[] itemsValue = param.getListItemsValue();
        if (tableViewerCreator != null) {
            List colList = tableViewerCreator.getColumns();
            for (int j = 0; j < itemsValue.length; j++) {
                if (itemsValue[j] instanceof IElementParameter) {
                    IElementParameter tmpParam = (IElementParameter) itemsValue[j];
                    if (tmpParam.getFieldType() == EParameterFieldType.COLUMN_LIST
                            || tmpParam.getFieldType() == EParameterFieldType.PREV_COLUMN_LIST
                            || tmpParam.getFieldType() == EParameterFieldType.LOOKUP_COLUMN_LIST) {
                        if ((j + 1) >= colList.size()) {
                            break;
                        }
                        TableViewerCreatorColumnNotModifiable column = (TableViewerCreatorColumnNotModifiable) colList.get(j + 1);
                        CellEditor cellEditor = column.getCellEditor();
                        String[] oldItems = null;
                        if (cellEditor instanceof ComboBoxCellEditor) {
                            CCombo combo = (CCombo) cellEditor.getControl();
                            oldItems = combo.getItems();
                            combo.setItems(tmpParam.getListItemsDisplayName());
                        }
                        List<Map<String, Object>> paramValues = (List<Map<String, Object>>) param.getValue();
                        String[] items = param.getListItemsDisplayCodeName();

                        for (int currentIndex = 0; currentIndex < paramValues.size(); currentIndex++) {
                            Map<String, Object> currentLine = paramValues.get(currentIndex);
                            Object o = currentLine.get(items[j]);
                            if (o instanceof Integer) {
                                Integer nb = (Integer) o;
                                if ((nb >= oldItems.length) || (nb == -1)) {
                                    nb = new Integer(tmpParam.getIndexOfItemFromList((String) tmpParam
                                            .getDefaultClosedListValue()));
                                    currentLine.put(items[j], nb);
                                } else {
                                    nb = new Integer(tmpParam.getIndexOfItemFromList(oldItems[nb]));
                                    currentLine.put(items[j], nb);
                                }
                            }
                        }
                    }

                }
            }
        }
    }

    private void updateConnectionList(IElementParameter param) {
        // update table values
        TableViewerCreator tableViewerCreator = (TableViewerCreator) hashCurControls.get(param.getName());
        Object[] itemsValue = param.getListItemsValue();
        if (tableViewerCreator != null) {
            List colList = tableViewerCreator.getColumns();
            for (int j = 0; j < itemsValue.length; j++) {
                if ((j + 1) >= colList.size()) {
                    break;
                }
                if (itemsValue[j] instanceof IElementParameter) {
                    IElementParameter tmpParam = (IElementParameter) itemsValue[j];
                    if (tmpParam.getFieldType() == EParameterFieldType.CONNECTION_LIST) {
                        String[] contextParameterNames = null;

                        ConnectionListController.updateConnectionList(elem, tmpParam);
                        contextParameterNames = tmpParam.getListItemsDisplayName();
                        tmpParam.setListItemsDisplayCodeName(contextParameterNames);
                        // tmpParam.setListItemsDisplayName(contextParameterNames);
                        // tmpParam.setListItemsValue(contextParameterNames);
                        if (contextParameterNames.length > 0) {
                            tmpParam.setDefaultClosedListValue(contextParameterNames[0]);
                        } else {
                            tmpParam.setDefaultClosedListValue(""); //$NON-NLS-1$
                        }
                        // j + 1 because first column is masked
                        TableViewerCreatorColumnNotModifiable column = (TableViewerCreatorColumnNotModifiable) colList.get(j + 1);

                        CCombo combo = (CCombo) column.getCellEditor().getControl();
                        String[] oldItems = combo.getItems();
                        combo.setItems(contextParameterNames);

                        List<Map<String, Object>> paramValues = (List<Map<String, Object>>) param.getValue();
                        String[] items = param.getListItemsDisplayCodeName();

                        for (int currentIndex = 0; currentIndex < paramValues.size(); currentIndex++) {
                            Map<String, Object> currentLine = paramValues.get(currentIndex);
                            Object o = currentLine.get(items[j]);
                            if (o instanceof Integer) {
                                Integer nb = (Integer) o;
                                if ((nb >= oldItems.length) || (nb == -1)) {
                                    nb = new Integer(tmpParam.getIndexOfItemFromList((String) tmpParam
                                            .getDefaultClosedListValue()));
                                    currentLine.put(items[j], nb);
                                } else {
                                    nb = new Integer(tmpParam.getIndexOfItemFromList(oldItems[nb]));
                                    currentLine.put(items[j], nb);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private void updateComponentList(IElementParameter param) {
        // update table values
        TableViewerCreator tableViewerCreator = (TableViewerCreator) hashCurControls.get(param.getName());
        Object[] itemsValue = param.getListItemsValue();
        if (tableViewerCreator != null) {
            List colList = tableViewerCreator.getColumns();
            for (int j = 0; j < itemsValue.length; j++) {
                if ((j + 1) >= colList.size()) {
                    break;
                }
                if (itemsValue[j] instanceof IElementParameter) {
                    IElementParameter tmpParam = (IElementParameter) itemsValue[j];
                    if (tmpParam.getFieldType() == EParameterFieldType.COMPONENT_LIST) {
                        String[] contextParameterNames = null;
                        ComponentListController.updateComponentList(elem, tmpParam);
                        contextParameterNames = tmpParam.getListItemsDisplayName();
                        tmpParam.setListItemsDisplayCodeName(contextParameterNames);
                        // tmpParam.setListItemsDisplayName(contextParameterNames);
                        // tmpParam.setListItemsValue(contextParameterNames);
                        if (contextParameterNames.length > 0) {
                            tmpParam.setDefaultClosedListValue(contextParameterNames[0]);
                        } else {
                            tmpParam.setDefaultClosedListValue(""); //$NON-NLS-1$
                        }
                        // j + 1 because first column is masked
                        TableViewerCreatorColumnNotModifiable column = (TableViewerCreatorColumnNotModifiable) colList.get(j + 1);

                        CCombo combo = (CCombo) column.getCellEditor().getControl();
                        String[] oldItems = combo.getItems();
                        combo.setItems(contextParameterNames);

                        List<Map<String, Object>> paramValues = (List<Map<String, Object>>) param.getValue();
                        String[] items = param.getListItemsDisplayCodeName();

                        for (int currentIndex = 0; currentIndex < paramValues.size(); currentIndex++) {
                            Map<String, Object> currentLine = paramValues.get(currentIndex);
                            Object o = currentLine.get(items[j]);
                            if (o instanceof Integer) {
                                Integer nb = (Integer) o;
                                if ((nb >= oldItems.length) || (nb == -1)) {
                                    nb = new Integer(tmpParam.getIndexOfItemFromList((String) tmpParam
                                            .getDefaultClosedListValue()));
                                    currentLine.put(items[j], nb);
                                } else {
                                    nb = new Integer(tmpParam.getIndexOfItemFromList(oldItems[nb]));
                                    currentLine.put(items[j], nb);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private void updateContextList(IElementParameter param) {
        List<String> contextParameterNamesList = new ArrayList<String>();

        IElementParameter processTypeParam = elem.getElementParameterFromField(EParameterFieldType.PROCESS_TYPE);
        if (processTypeParam == null) {
            processTypeParam = elem.getElementParameterFromField(EParameterFieldType.ROUTE_INPUT_PROCESS_TYPE);
            if (processTypeParam == null) {
                return;
            }
        }
        boolean haveContextParamList = false;
        for (Object valueParam : param.getListItemsValue()) {
            if (valueParam instanceof IElementParameter) {
                IElementParameter vParam = (IElementParameter) valueParam;
                if (vParam.getFieldType() == EParameterFieldType.CONTEXT_PARAM_NAME_LIST) {
                    haveContextParamList = true;
                    break;
                }
            }
        }
        if (!haveContextParamList) {
            return;
        }
        IElementParameter jobElemParam = processTypeParam.getChildParameters().get(EParameterName.PROCESS_TYPE_PROCESS.getName());
        IElementParameter jobVersionParam = processTypeParam.getChildParameters().get(
                EParameterName.PROCESS_TYPE_VERSION.getName());

        IElementParameter contextElemParam = processTypeParam.getChildParameters().get(
                EParameterName.PROCESS_TYPE_CONTEXT.getName());
        // get context list
        String processId = (String) jobElemParam.getValue();
        String contextName = (String) contextElemParam.getValue();
        if (contextName == null) {
            contextName = new String();
        }

        if (processId == null || contextName == null) {
            revertToolBarButtonState(false);
            return;
        }
        IElementParameter useDynamic = elem.getElementParameter("USE_DYNAMIC_JOB");
        if (useDynamic != null && Boolean.valueOf(String.valueOf(useDynamic.getValue()))) {
            String[] split = processId.split(";");
            processId = split[0];
        }

        ProcessItem processItem = ItemCacheManager.getProcessItem(processId, (String) jobVersionParam.getValue());
        String[] contextParameterNames = null;
        if (processItem != null) {
            // achen modify to fix bug 0006107
            IDesignerCoreService service = CorePlugin.getDefault().getDesignerCoreService();
            // process = new Process(processItem.getProperty());
            // process.loadXmlFile();
            IContextManager contextManager = service.getProcessContextFromItem(processItem);
            if (contextManager != null) {
                IContext context = contextManager.getContext(contextName);

                for (IContextParameter contextParam : context.getContextParameterList()) {
                    contextParameterNamesList.add(contextParam.getName());
                }
            }

            contextParameterNames = contextParameterNamesList.toArray(new String[0]);
        }

        if (contextParameterNames == null || contextParameterNames.length == 0) {
            contextParameterNamesList.clear();
            // in case the job is opened but childjob are missing, or if there is a problem when retrieve the child job
            // we rerebuild the list here from what was saved in the job before
            for (HashMap<String, Object> values : (List<HashMap<String, Object>>) param.getValue()) {
                String name = (String) values.get("PARAM_NAME_COLUMN"); //$NON-NLS-1$
                contextParameterNamesList.add(name);
            }
            contextParameterNames = contextParameterNamesList.toArray(new String[0]);
        }

        // update table values
        TableViewerCreator tableViewerCreator = (TableViewerCreator) hashCurControls.get(param.getName());
        Object[] itemsValue = param.getListItemsValue();
        if (tableViewerCreator != null) {
            List colList = tableViewerCreator.getColumns();
            for (int j = 0; j < itemsValue.length; j++) {
                if ((j + 1) >= colList.size()) {
                    break;
                }
                if (itemsValue[j] instanceof IElementParameter) {
                    IElementParameter tmpParam = (IElementParameter) itemsValue[j];
                    if (tmpParam.getFieldType() == EParameterFieldType.CONTEXT_PARAM_NAME_LIST) {
                        tmpParam.setListItemsDisplayCodeName(contextParameterNames);
                        tmpParam.setListItemsDisplayName(contextParameterNames);
                        tmpParam.setListItemsValue(contextParameterNames);
                        // TDI-35251 won't set default, if not fount, keep error
                        // if (contextParameterNames.length > 0) {
                        // tmpParam.setDefaultClosedListValue(contextParameterNames[0]);
                        // } else {
                        tmpParam.setDefaultClosedListValue(""); //$NON-NLS-1$
                        // }
                        // j + 1 because first column is masked
                        TableViewerCreatorColumnNotModifiable column = (TableViewerCreatorColumnNotModifiable) colList.get(j + 1);

                        CCombo combo = (CCombo) column.getCellEditor().getControl();
                        String[] oldItems = combo.getItems();
                        combo.setItems(contextParameterNames);

                        List<Map<String, Object>> paramValues = (List<Map<String, Object>>) param.getValue();
                        String[] items = param.getListItemsDisplayCodeName();

                        for (int currentIndex = 0; currentIndex < paramValues.size(); currentIndex++) {
                            Map<String, Object> currentLine = paramValues.get(currentIndex);
                            Object o = currentLine.get(items[j]);
                            if (o instanceof Integer) {
                                Integer nb = (Integer) o;
                                if ((nb >= oldItems.length) || (nb == -1)) {
                                    currentLine.put(items[j], tmpParam.getDefaultClosedListValue());
                                } else {
                                    currentLine.put(items[j], oldItems[nb]);
                                }
                            } else {
                                if (o instanceof String) {
                                    Integer nb = new Integer(tmpParam.getIndexOfItemFromList((String) o));
                                    if (nb == -1 && !"".equals(tmpParam.getDefaultClosedListValue())) {
                                        currentLine.put(items[j], tmpParam.getDefaultClosedListValue());
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        // (bug 3740)
        boolean checked = contextParameterNames != null && contextParameterNames.length > 0;
        revertToolBarButtonState(checked);

    }

    public static Map<String, Object> createNewLine(IElementParameter param) {
        Map<String, Object> line = new LinkedHashMap<String, Object>();
        String[] items = param.getListItemsDisplayCodeName();
        Object[] itemsValue = param.getListItemsValue();
        IElementParameter tmpParam;
        if (itemsValue.length == 0) {
            return line;
        }

        tmpParam = (IElementParameter) itemsValue[0];
        switch (tmpParam.getFieldType()) {
        case CONTEXT_PARAM_NAME_LIST:
            line.put(items[0], tmpParam.getDefaultClosedListValue());
            break;
        case CLOSED_LIST:
        case COLUMN_LIST:
        case COMPONENT_LIST:
        case CONNECTION_LIST:
        case DBTYPE_LIST:
        case LOOKUP_COLUMN_LIST:
        case PREV_COLUMN_LIST:
            line.put(items[0], new Integer(tmpParam.getIndexOfItemFromList((String) tmpParam.getDefaultClosedListValue())));
            break;
        case SCHEMA_TYPE:
        case SAP_SCHEMA_TYPE:
        case COLOR:
        case CHECK:
            line.put(items[0], tmpParam.getValue());
            break;
        default: // TEXT
            if ((tmpParam.getValue() == null) || (tmpParam.getValue().equals(""))) { //$NON-NLS-1$
                line.put(items[0], new String(TalendTextUtils.addQuotes("newLine"))); //$NON-NLS-1$
            } else {
                line.put(items[0], tmpParam.getValue());
            }
        }

        for (int i = 1; i < items.length; i++) {
            tmpParam = (IElementParameter) itemsValue[i];
            switch (tmpParam.getFieldType()) {
            case CONTEXT_PARAM_NAME_LIST:
            case CLOSED_LIST:
            case DBTYPE_LIST:
            case COLUMN_LIST:
            case COMPONENT_LIST:
            case CONNECTION_LIST:
            case LOOKUP_COLUMN_LIST:
            case PREV_COLUMN_LIST:
                line.put(items[i], new Integer(tmpParam.getIndexOfItemFromList((String) tmpParam.getDefaultClosedListValue())));
                break;
            default: // TEXT or CHECK or COLOR (means String or Boolean)
                line.put(items[i], tmpParam.getValue());
            }
        }
        return line;
    }

    /**
     *
     * ggu Comment method "revertAllButton".
     *
     * if flag is false, will set the button for unenabled state. (bug 3740)
     */
    private void revertToolBarButtonState(boolean flag) {

        PropertiesTableToolbarEditorView toolBar = (PropertiesTableToolbarEditorView) hashCurControls.get(TOOLBAR_NAME);
        if (toolBar != null) {
            for (ExtendedPushButton btn : toolBar.getButtons()) {
                if (flag) {
                    btn.getButton().setEnabled(btn.getEnabledState());
                } else {
                    btn.getButton().setEnabled(false);
                }
            }
        }
    }

    /**
     *
     * DOC YeXiaowei Comment method "isNeedAddAllButton".
     *
     * @param param
     * @return
     */
    public static boolean isNeedAddAllButton(IElementParameter param) {
        Object[] itemsValue = param.getListItemsValue();
        IElementParameter tmpParam;
        // enable the "add all" button works when the COLUMN_LIST in the table no matter its position is the first or
        // not
        if (itemsValue.length > 0) {
            boolean b = false;
            for (Object element : itemsValue) {
                tmpParam = (IElementParameter) element;
                if (tmpParam != null) {
                    b = tmpParam.getFieldType() == EParameterFieldType.COLUMN_LIST;
                    if (b) {
                        return true;
                    }
                }
            }
        }
        return false;

    }
}
