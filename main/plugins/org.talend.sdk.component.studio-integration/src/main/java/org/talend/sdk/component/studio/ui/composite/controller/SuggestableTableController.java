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
package org.talend.sdk.component.studio.ui.composite.controller;

import java.util.ArrayList;
import java.util.Iterator;
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
import org.talend.commons.ui.swt.tableviewer.TableViewerCreator;
import org.talend.commons.utils.data.list.IListenableListListener;
import org.talend.commons.utils.data.list.ListenableListEvent;
import org.talend.core.model.process.EParameterFieldType;
import org.talend.core.model.process.IElementParameter;
import org.talend.core.ui.CoreUIPlugin;
import org.talend.core.ui.properties.tab.IDynamicProperty;
import org.talend.designer.core.model.FakeElement;
import org.talend.designer.core.ui.editor.cmd.PropertyChangeCommand;
import org.talend.designer.core.ui.editor.connections.Connection;
import org.talend.designer.core.ui.editor.nodes.Node;
import org.talend.designer.core.ui.editor.properties.controllers.ColumnListController;
import org.talend.designer.core.ui.editor.properties.controllers.TableController;
import org.talend.designer.core.ui.editor.properties.macrowidgets.tableeditor.PropertiesTableEditorModel;
import org.talend.designer.core.ui.editor.properties.macrowidgets.tableeditor.PropertiesTableEditorView;
import org.talend.sdk.component.studio.model.parameter.SuggestableTableParameter;

public class SuggestableTableController extends TableController {

    private static final String TOOLBAR_NAME = "_TABLE_VIEW_TOOLBAR_NAME_"; //$NON-NLS-1$

    private static final int TABLE_HORIZONTAL_OFFSET = -5;

    private static final int TOOLBAR_SIZE = 0;

    public SuggestableTableController(final IDynamicProperty dp) {
        super(dp);
    }

    /**
     * Creates Suggestable Table Control which consists of following parts:
     * <ol>
     * <li>parameter label</li>
     * <li>table with disabled toolbar</li>
     * <li>"..." button, which opens value selection dialog </li>
     * </ol>
     *
     * @param parentComposite parent composite on which all created controls will be located
     * @param param           ElementParameter
     * @param numInRow        number of control to be created in current row
     * @param nbInRow         total number of all controls in the current row
     * @param top             offset from top of the composite. It is computed as row size of all previously added controls + spaces
     *                        between controls
     * @param lastControlPrm  previously added control in the current row. If it is null, then current control will be
     *                        the first control in the row
     * @return Suggestable Table Control
     */
    @Override
    public Control createControl(final Composite parentComposite, final IElementParameter param, final int numInRow,
                                 final int nbInRow, final int top, final Control lastControlPrm) {
        this.curParameter = param;
        this.paramFieldType = param.getFieldType();

        // Create table widget
        final PropertiesTableEditorModel<Map<String, Object>> tableEditorModel = new PropertiesTableEditorModel<Map<String, Object>>();

        tableEditorModel.setData(elem, param, getProcess(elem, part));
        final PropertiesTableEditorView<Map<String, Object>> tableEditorView = new PropertiesTableEditorView<Map<String, Object>>(
                parentComposite, SWT.NONE, tableEditorModel, false, false);
        tableEditorView.getExtendedTableViewer().setCommandStack(getCommandStack());
        final boolean editable = !param.isReadOnly() && (elem instanceof FakeElement || !param.isRepositoryValueUsed());
        tableEditorView.setReadOnly(!editable);
        tableEditorModel.setModifiedBeanListenable(tableEditorView.getTableViewerCreator());
        tableEditorModel.addModifiedBeanListenerForAggregateComponent();

        final Table table = tableEditorView.getTable();

        table.setToolTipText(VARIABLE_TOOLTIP + param.getVariableName());

        // add listener to tableMetadata (listen the event of the toolbars)
        tableEditorView.getExtendedTableModel().addAfterOperationListListener(new IListenableListListener() {

            @Override
            public void handleEvent(final ListenableListEvent event) {
                if (elem instanceof Node) {
                    final Node node = (Node) elem;
                    node.checkAndRefreshNode();
                }
            }
        });

        final CLabel labelLabel2 = getWidgetFactory().createCLabel(parentComposite, param.getDisplayName());
        setupLabelLayout(labelLabel2, numInRow, nbInRow, top, lastControlPrm);

        final Button button = createEditButton(parentComposite, param);
        setupButtonLayout(button, numInRow, nbInRow, labelLabel2);

        // Setup table widget layout
        int currentLabelWidth2 = STANDARD_LABEL_WIDTH;
        final GC gc2 = new GC(labelLabel2);
        final Point labelSize2 = gc2.stringExtent(param.getDisplayName());
        gc2.dispose();

        boolean needOffset = true;
        if ((labelSize2.x + ITabbedPropertyConstants.HSPACE) > currentLabelWidth2) {
            currentLabelWidth2 = labelSize2.x + ITabbedPropertyConstants.HSPACE;
            needOffset = false;
        } else {

        }

        final Composite tableComposite = tableEditorView.getMainComposite();
        final FormData tableFormData = new FormData();
        if (numInRow == 1) {
            if (lastControlPrm != null) {
                if (needOffset) {
                    tableFormData.left = new FormAttachment(lastControlPrm, currentLabelWidth2 + TABLE_HORIZONTAL_OFFSET);
                } else {
                    tableFormData.left = new FormAttachment(lastControlPrm, currentLabelWidth2);
                }
            } else {
                if (needOffset) {
                    tableFormData.left = new FormAttachment(0, currentLabelWidth2 + TABLE_HORIZONTAL_OFFSET);
                } else {
                    tableFormData.left = new FormAttachment(0, currentLabelWidth2);
                }
            }
        } else {
            tableFormData.left = new FormAttachment(labelLabel2, 0 + TABLE_HORIZONTAL_OFFSET, SWT.RIGHT);
        }
        tableFormData.right = new FormAttachment(button, 0);
        tableFormData.top = new FormAttachment(0, top);

        final int currentHeightEditor = table.getHeaderHeight() + ((List) param.getValue()).size() * table.getItemHeight()
                + table.getItemHeight() + TOOLBAR_SIZE;
        int minHeightEditor = table.getHeaderHeight() + getNumberLines(param) * table.getItemHeight() + table.getItemHeight()
                + TOOLBAR_SIZE;
        int ySize2 = Math.max(currentHeightEditor, minHeightEditor);

        ySize2 = Math.min(ySize2, 500);
        tableFormData.bottom = new FormAttachment(0, top + ySize2);
        tableComposite.setLayoutData(tableFormData);

        hashCurControls.put(param.getName(), tableEditorView.getExtendedTableViewer().getTableViewerCreator());
        hashCurControls.put(TOOLBAR_NAME, tableEditorView.getToolBar());
        updateTableValues(param);

        this.dynamicProperty.setCurRowSize(ySize2 + ITabbedPropertyConstants.VSPACE);
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
                    protected IStatus run(final IProgressMonitor monitor) {
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
    public int estimateRowSize(final Composite subComposite, final IElementParameter param) {
        final PropertiesTableEditorModel<Map<String, Object>> tableEditorModel = new PropertiesTableEditorModel<Map<String, Object>>();

        updateTableValues(param);

        tableEditorModel.setData(elem, param, part.getProcess());
        final PropertiesTableEditorView<Map<String, Object>> tableEditorView = new PropertiesTableEditorView<Map<String, Object>>(
                subComposite, SWT.NONE, tableEditorModel, false, false);
        tableEditorView.getExtendedTableViewer().setCommandStack(getCommandStack());
        tableEditorView.setReadOnly(param.isReadOnly());
        final Table table = tableEditorView.getTable();
        final int currentHeightEditor = table.getHeaderHeight() + ((List) param.getValue()).size() * table.getItemHeight()
                + table.getItemHeight() + TOOLBAR_SIZE;
        final int minHeightEditor = table.getHeaderHeight() + getNumberLines(param) * table.getItemHeight() + table.getItemHeight()
                + TOOLBAR_SIZE;

        tableEditorView.getMainComposite().dispose();

        final int ySize2 = Math.max(currentHeightEditor, minHeightEditor);
        return ySize2 + ITabbedPropertyConstants.VSPACE;
    }

    @Override
    public void updateColumnList(IElementParameter param) {
        List<String> prevColumnList = new ArrayList<String>();
        if (elem instanceof Node) {
            ColumnListController.updateColumnList((Node) elem, null);
            prevColumnList.addAll(ColumnListController.getPrevColumnList((Node) elem, null));
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
                                    nb = new Integer(
                                            tmpParam.getIndexOfItemFromList((String) tmpParam.getDefaultClosedListValue()));
                                    currentLine.put(items[j], nb);
                                } else {
                                    nb = new Integer(tmpParam.getIndexOfItemFromList(oldItems[nb]));
                                    currentLine.put(items[j], nb);
                                }
                            }
                        }
                    } else if (tmpParam.getFieldType() == EParameterFieldType.TEXT) {
                        List<Map<String, Object>> paramValues = (List<Map<String, Object>>) param.getValue();
                        String[] items = param.getListItemsDisplayCodeName();
                        Iterator<Map<String, Object>> iterator = paramValues.iterator();
                        while (iterator.hasNext()) {
                            Map<String, Object> currentLine = iterator.next();
                            Object o = currentLine.get(items[j]);
                            if (!prevColumnList.contains(o)) {
                                iterator.remove();
                            }
                        }
                    }
                }
            }
        }
    }
}
