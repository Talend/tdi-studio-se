// ============================================================================
//
// Copyright (C) 2006-2018 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.sdk.component.studio.ui.composite.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ComboBoxCellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Table;
import org.eclipse.ui.views.properties.tabbed.ITabbedPropertyConstants;
import org.talend.commons.ui.runtime.image.ImageProvider;
import org.talend.commons.ui.runtime.swt.tableviewer.TableViewerCreatorColumnNotModifiable;
import org.talend.commons.ui.swt.advanced.dataeditor.control.ExtendedPushButton;
import org.talend.commons.ui.swt.tableviewer.TableViewerCreator;
import org.talend.commons.utils.data.list.IListenableListListener;
import org.talend.commons.utils.data.list.ListenableListEvent;
import org.talend.core.CorePlugin;
import org.talend.core.ITDQPatternService;
import org.talend.core.model.process.EParameterFieldType;
import org.talend.core.model.process.IContext;
import org.talend.core.model.process.IContextManager;
import org.talend.core.model.process.IContextParameter;
import org.talend.core.model.process.IElementParameter;
import org.talend.core.model.properties.ProcessItem;
import org.talend.core.ui.CoreUIPlugin;
import org.talend.core.ui.properties.tab.IDynamicProperty;
import org.talend.designer.core.IDesignerCoreService;
import org.talend.designer.core.model.FakeElement;
import org.talend.designer.core.model.components.EParameterName;
import org.talend.designer.core.ui.editor.cmd.PropertyChangeCommand;
import org.talend.designer.core.ui.editor.connections.Connection;
import org.talend.designer.core.ui.editor.nodes.Node;
import org.talend.designer.core.ui.editor.properties.controllers.ColumnListController;
import org.talend.designer.core.ui.editor.properties.controllers.ComponentListController;
import org.talend.designer.core.ui.editor.properties.controllers.ConnectionListController;
import org.talend.designer.core.ui.editor.properties.controllers.DbTypeListController;
import org.talend.designer.core.ui.editor.properties.controllers.ModuleListController;
import org.talend.designer.core.ui.editor.properties.controllers.TableController;
import org.talend.designer.core.ui.editor.properties.macrowidgets.tableeditor.PropertiesTableEditorModel;
import org.talend.designer.core.ui.editor.properties.macrowidgets.tableeditor.PropertiesTableEditorView;
import org.talend.designer.core.ui.editor.properties.macrowidgets.tableeditor.PropertiesTableToolbarEditorView;
import org.talend.designer.runprocess.ItemCacheManager;
import org.talend.sdk.component.studio.model.parameter.SuggestableTableParameter;

public class SuggestableTableController extends TableController {

    private static final int MIN_NUMBER_ROWS = 1;

    private static final String TOOLBAR_NAME = "_TABLE_VIEW_TOOLBAR_NAME_"; //$NON-NLS-1$

    /**
     * DOC yzhang TableController constructor comment.
     *
     * @param dtp
     */
    public SuggestableTableController(IDynamicProperty dp) {
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

        // Create table widget
        PropertiesTableEditorModel<Map<String, Object>> tableEditorModel = new PropertiesTableEditorModel<Map<String, Object>>();

        tableEditorModel.setData(elem, param, getProcess(elem, part));
        PropertiesTableEditorView<Map<String, Object>> tableEditorView = new PropertiesTableEditorView<Map<String, Object>>(
                parentComposite, SWT.NONE, tableEditorModel, false, false);
        tableEditorView.getExtendedTableViewer().setCommandStack(getCommandStack());
        boolean editable = !param.isReadOnly() && (elem instanceof FakeElement || !param.isRepositoryValueUsed());
        tableEditorView.setReadOnly(!editable);
        tableEditorModel.setModifiedBeanListenable(tableEditorView.getTableViewerCreator());
        tableEditorModel.addModifiedBeanListenerForAggregateComponent();

        final Table table = tableEditorView.getTable();

        table.setToolTipText(VARIABLE_TOOLTIP + param.getVariableName());

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


        // Create property label which is displayed on the left side
        CLabel labelLabel2 = getWidgetFactory().createCLabel(parentComposite, param.getDisplayName());
        setupLabelLayout(labelLabel2, numInRow, nbInRow, top, lastControlPrm);

        // Create edit button
        final Button button = createEditButton(parentComposite, param);
        setupButtonLayout(button, numInRow, nbInRow, labelLabel2);

        // Set table widget layout
        int currentLabelWidth2 = STANDARD_LABEL_WIDTH;
        GC gc2 = new GC(labelLabel2);
        Point labelSize2 = gc2.stringExtent(param.getDisplayName());
        gc2.dispose();

        boolean needOffset = true;
        if ((labelSize2.x + ITabbedPropertyConstants.HSPACE) > currentLabelWidth2) {
            currentLabelWidth2 = labelSize2.x + ITabbedPropertyConstants.HSPACE;
            needOffset = false;
        }

        final Composite tableComposite = tableEditorView.getMainComposite();
        FormData tableFormData = new FormData();
        int tableHorizontalOffset = -5;
        if (numInRow == 1) {
            if (lastControlPrm != null) {
                if (needOffset) {
                    tableFormData.left = new FormAttachment(lastControlPrm, currentLabelWidth2 + tableHorizontalOffset);
                } else {
                    tableFormData.left = new FormAttachment(lastControlPrm, currentLabelWidth2);
                }
            } else {
                if (needOffset) {
                    tableFormData.left = new FormAttachment(0, currentLabelWidth2 + tableHorizontalOffset);
                } else {
                    tableFormData.left = new FormAttachment(0, currentLabelWidth2);
                }
            }
        } else {
            tableFormData.left = new FormAttachment(labelLabel2, 0 + tableHorizontalOffset, SWT.RIGHT);
        }
        tableFormData.right = new FormAttachment(button, 0);
        tableFormData.top = new FormAttachment(0, top);

        int toolbarSize = 0;
        int currentHeightEditor = table.getHeaderHeight() + ((List) param.getValue()).size() * table.getItemHeight()
                + table.getItemHeight() + toolbarSize;
        int minHeightEditor = table.getHeaderHeight() + getNumberLines(param) * table.getItemHeight() + table.getItemHeight()
                + toolbarSize;
        int ySize2 = Math.max(currentHeightEditor, minHeightEditor);

        ySize2 = Math.min(ySize2, 500);
        tableFormData.bottom = new FormAttachment(0, top + ySize2);
        tableComposite.setLayoutData(tableFormData);

        hashCurControls.put(param.getName(), tableEditorView.getExtendedTableViewer().getTableViewerCreator());
        hashCurControls.put(TOOLBAR_NAME, tableEditorView.getToolBar());
        updateTableValues(param);

        this.dynamicProperty.setCurRowSize(ySize2 + ITabbedPropertyConstants.VSPACE);

        // Set table widget layout in case of wizard
        if (isInWizard()) {
            labelLabel2.setAlignment(SWT.RIGHT);
            if (lastControlPrm != null) {
                tableFormData.right = new FormAttachment(lastControlPrm, 0);
            } else {
                tableFormData.right = new FormAttachment(100, -ITabbedPropertyConstants.HSPACE);
            }
            tableFormData.left = new FormAttachment((((nbInRow - numInRow) * MAX_PERCENT) / nbInRow), currentLabelWidth2
                    + ITabbedPropertyConstants.HSPACE);

            tableFormData = (FormData) labelLabel2.getLayoutData();
            tableFormData.right = new FormAttachment(tableComposite, 0);
            tableFormData.left = new FormAttachment((((nbInRow - numInRow) * MAX_PERCENT) / nbInRow), 0);

            return labelLabel2;
        }

        return tableComposite;
    }

    private void setupLabelLayout(final CLabel label, final int numInRow, final int nbInRow, final int top, final Control lastControl) {
        final FormData data = new FormData();
        if (lastControl != null) {
            data.left = new FormAttachment(lastControl, 0);
        } else {
            data.left = new FormAttachment((((numInRow - 1) * MAX_PERCENT) / nbInRow), 0);
        }
        data.top = new FormAttachment(0, top);
        label.setLayoutData(data);
        if (numInRow != 1) {
            label.setAlignment(SWT.RIGHT);
        }
    }

    private Button createEditButton(final Composite parent, final IElementParameter param) {
        final Button editButton = getWidgetFactory().createButton(parent, "", SWT.PUSH);
        editButton.setImage(ImageProvider.getImage(CoreUIPlugin.getImageDescriptor(DOTS_BUTTON)));
        editButton.setEnabled(!param.isRepositoryValueUsed());
        editButton.addSelectionListener(createOnButtonClickedListener(param));
        return editButton;
    }

    /**
     * Creates listener, which opens value selection dialog each time, when user pushes a "..." button near table
     *
     * @param parameter SuggestableTableParameter
     * @return listener
     */
    private SelectionListener createOnButtonClickedListener(final IElementParameter parameter) {
        return new SelectionAdapter() {

            private final Job job;

            {
                job = new Job("Retrieve possible values") {

                    @Override
                    protected IStatus run(IProgressMonitor monitor) {
                        monitor.subTask("Retrieve schema column names");
                        final List<Map<String, Object>> suggestedValues = ((SuggestableTableParameter) parameter).getSuggestionValues();
                        if (monitor.isCanceled()) {
                            return Status.CANCEL_STATUS;
                        }
                        monitor.subTask("Open Selection Dialog");
                        Display.getDefault().asyncExec(new Runnable() {
                            public void run() {
                                final String labelsColumn = ((SuggestableTableParameter) parameter).getColumnKey();
                                final List<Map<String, Object>> chosenValues = (List<Map<String, Object>>) parameter.getValue();
                                final TableValueSelectionDialog dialog = new TableValueSelectionDialog(composite.getShell(),
                                        labelsColumn,
                                        suggestedValues,
                                        chosenValues);

                                if (dialog.open() == IDialogConstants.OK_ID) {
                                    final PropertyChangeCommand command = new PropertyChangeCommand(elem, parameter.getName(),
                                            dialog.getChosenValues());
                                    executeCommand(command);
                                    refresh(parameter, false);
                                }
                            }
                        });
                        monitor.done();
                        return Status.OK_STATUS;
                    }

                };
                job.setUser(true);
            }

            @Override
            public void widgetSelected(final SelectionEvent e) {
                job.schedule();
            }
        };
    }

    private void setupButtonLayout(final Button button, final int numInRow, final int nbInRow, final CLabel label) {
        final FormData data = new FormData();
        data.left = new FormAttachment(((numInRow * MAX_PERCENT) / nbInRow), -STANDARD_BUTTON_WIDTH);
        data.right = new FormAttachment(((numInRow * MAX_PERCENT) / nbInRow), 0);
        data.top = new FormAttachment(label, 0, SWT.CENTER);
        data.height = STANDARD_HEIGHT - 2;
        button.setLayoutData(data);
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
                subComposite, SWT.NONE, tableEditorModel, false, false);
        tableEditorView.getExtendedTableViewer().setCommandStack(getCommandStack());
        tableEditorView.setReadOnly(param.isReadOnly());
        final Table table = tableEditorView.getTable();
        int toolbarSize = 0;
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
    private int getNumberLines(IElementParameter param) {
        int numlines = param.getNbLines();
        return numlines < MIN_NUMBER_ROWS ? MIN_NUMBER_ROWS : numlines;
    }

    private void updateTableValues(IElementParameter param) {
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

    private void updateColumnList(IElementParameter param) {
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

    /**
     * ggu Comment method "revertAllButton".
     * <p>
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

}
