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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.gef.commands.Command;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableItem;
import org.talend.commons.ui.swt.advanced.dataeditor.ExtendedToolbarView;
import org.talend.commons.ui.swt.advanced.dataeditor.button.AddAllPushButton;
import org.talend.commons.ui.swt.advanced.dataeditor.button.AddAllPushButtonForExtendedTable;
import org.talend.commons.ui.swt.advanced.dataeditor.button.AddPushButton;
import org.talend.commons.ui.swt.advanced.dataeditor.button.AddPushButtonForExtendedTable;
import org.talend.commons.ui.swt.advanced.dataeditor.button.ExportPushButton;
import org.talend.commons.ui.swt.advanced.dataeditor.button.ImportPushButton;
import org.talend.commons.ui.swt.advanced.dataeditor.button.MoveDownPushButton;
import org.talend.commons.ui.swt.advanced.dataeditor.button.MoveUpPushButton;
import org.talend.commons.ui.swt.advanced.dataeditor.button.PastePushButton;
import org.talend.commons.ui.swt.advanced.dataeditor.button.PastePushButtonForExtendedTable;
import org.talend.commons.ui.swt.advanced.dataeditor.button.RemovePushButton;
import org.talend.commons.ui.swt.advanced.dataeditor.button.RemovePushButtonForExtendedTable;
import org.talend.commons.ui.swt.extended.table.AbstractExtendedTableViewer;
import org.talend.commons.ui.swt.extended.table.ExtendedButtonEvent;
import org.talend.commons.ui.swt.extended.table.ExtendedTableModel;
import org.talend.commons.ui.swt.extended.table.IExtendedButtonListener;
import org.talend.commons.ui.utils.SimpleClipboard;
import org.talend.core.model.metadata.IMetadataColumn;
import org.talend.core.model.metadata.IMetadataTable;
import org.talend.core.model.process.EParameterFieldType;
import org.talend.core.model.process.IElement;
import org.talend.core.model.process.IElementParameter;
import org.talend.core.model.process.INode;
import org.talend.designer.core.model.components.ElementParameter;
import org.talend.designer.core.ui.editor.cmd.PropertyTablePasteCommand;
import org.talend.designer.core.ui.editor.nodes.Node;
import org.talend.designer.core.ui.editor.properties.controllers.TableController;
import org.talend.designer.core.ui.editor.properties.macrowidgets.tableeditor.PromptDefaultValueDialog.ColumnInfo;

/**
 * $Id$
 *
 */
public class PropertiesTableToolbarEditorView extends ExtendedToolbarView {

    private PropertiesTableEditorModel model;

    private IExtendedButtonListener afterPropertyChangeListener;

    /**
     * DOC amaumont MetadataToolbarEditorView constructor comment.
     *
     * @param parent
     * @param style
     * @param extendedTableViewer
     */
    public PropertiesTableToolbarEditorView(Composite parent, int style, AbstractExtendedTableViewer extendedTableViewer,
            PropertiesTableEditorModel model) {
        super(parent, style, extendedTableViewer);
        this.model = model;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.ui.extended.ExtendedToolbarView#createComponents(org.eclipse.swt.widgets.Composite)
     */
    @Override
    protected void createComponents(Composite parent) {
        super.createComponents(parent);

    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.ui.extended.ExtendedToolbarView#createAddPushButton()
     */
    @Override
    protected AddPushButton createAddPushButton() {
        final AddPushButton addPushButton = new AddPushButtonForExtendedTable(this.toolbar, getExtendedTableViewer()) {

            @Override
            public boolean getEnabledState() {
                if (model != null
                        && !model.getElemParameter().getElement().isReadOnly()
                        && (model.getElemParameter().getName().equals("HADOOP_ADVANCED_PROPERTIES")
                                || model.getElemParameter().getName().equals("HBASE_PARAMETERS") || model.getElemParameter()
                                .getName().equals("SPARK_ADVANCED_PROPERTIES"))
                        && !model.getElemParameter().isRepositoryValueUsed()) {
                    return true;
                }
                return super.getEnabledState() && (model == null || !model.getElemParameter().isBasedOnSubjobStarts());
            }

            @Override
            protected Object getObjectToAdd() {

                PropertiesTableEditorModel tableEditorModel = (PropertiesTableEditorModel) getExtendedTableViewer()
                        .getExtendedControlModel();
                if (!tableEditorModel.isButtonEnabled()) {
                    MessageDialog.openInformation(tableEditorModel.getTableViewer().getControl().getShell(), "Information",
                            "All output columns have been added already");
                    return null;
                }

                Object newEntry = tableEditorModel.createNewEntry();
                if (tableEditorModel.isAggregateRow() && newEntry instanceof Map) {
                    List<ColumnInfo> tableInputs = new ArrayList<ColumnInfo>();

                    String[] displayNames = tableEditorModel.getElemParameter().getListItemsDisplayName();
                    Object[] itemsValue = tableEditorModel.getElemParameter().getListItemsValue();
                    String paramColumnsName = "COLUMN";// default name //$NON-NLS-1$
                    for (int i = 0; i < itemsValue.length; i++) {
                        if (itemsValue[i] instanceof IElementParameter) {
                            IElementParameter tableParameter = (IElementParameter) itemsValue[i];
                            if (tableParameter.getFieldType().equals(EParameterFieldType.COLUMN_LIST)) {
                                paramColumnsName = tableParameter.getName();
                            } else {
                                ColumnInfo row = new ColumnInfo();
                                row.name = displayNames[i];
                                row.parameter = tableParameter;
                                row.defaultValue = "";
                                tableInputs.add(row);
                            }
                        }
                    }

                    Node node = (Node) tableEditorModel.getElement();
                    String outputTableName = null;
                    if (node.getMetadataList() != null && !node.getMetadataList().isEmpty()) {
                        IMetadataTable metadata = node.getMetadataList().get(0);
                        if (metadata.getListColumns() != null) {
                            for (IMetadataColumn column : metadata.getListColumns()) {
                                if (!tableEditorModel.exist(column.getLabel())) {
                                    outputTableName = column.getLabel();
                                    break;
                                }
                            }
                        }
                    }
                    if (outputTableName != null) {
                        Map mapObject = (Map) newEntry;
                        if (mapObject.containsKey(paramColumnsName)) {
                            mapObject.put(paramColumnsName, outputTableName);
                        }
                        for (ColumnInfo col : tableInputs) {
                            Object defaultValue = col.defaultValue;
                            Object found = findDefaultName(outputTableName, col);
                            if (found != null && !"".equals(found)) {
                                defaultValue = found;
                            }
                            if (defaultValue != null && !"".equals(defaultValue)) {
                                mapObject.put(col.parameter.getName(), defaultValue);
                            }
                        }
                    }
                }

                return newEntry;
            }

        };
        // TDI-6568, after added, fire change
        addPushButton.addListener(getPropertyChangeListener(), false);
        return addPushButton;
    }

    private IExtendedButtonListener getPropertyChangeListener() {
        if (afterPropertyChangeListener == null) {
            afterPropertyChangeListener = new IExtendedButtonListener() {

                @Override
                public void handleEvent(ExtendedButtonEvent event) {
                    PropertiesTableEditorModel tableEditorModel = (PropertiesTableEditorModel) getExtendedTableViewer()
                            .getExtendedControlModel();
                    IElement node = tableEditorModel.getElement();
                    IElementParameter param = tableEditorModel.getElemParameter();
                    /*
                     * TDI-6568, in fact, no need reset the value. just want to enable
                     * "firePropertyChange(RETURNS_CHANGED, null, null)" in Node.
                     */
                    if (param.getFieldType().equals(EParameterFieldType.TABLE)) {
                        boolean isTacokit = false;
                        if (param instanceof ElementParameter) {
                            Object sourceName = ((ElementParameter) param).getTaggedValue("org.talend.sdk.component.source"); //$NON-NLS-1$
                            isTacokit = "tacokit".equalsIgnoreCase(String.valueOf(sourceName)); //$NON-NLS-1$
                        }
                        if (isTacokit) {
                            Object paramValue = param.getValue();
                            if (paramValue != null && paramValue instanceof List) {
                                List<Map<String, Object>> beansList = new ArrayList<Map<String, Object>>();
                                beansList.addAll(tableEditorModel.getBeansList());
                                node.setPropertyValue(param.getName(), tableEditorModel.getBeforeChangeBeansList());
                                node.setPropertyValue(param.getName(), beansList);
                            } else {
                                node.setPropertyValue(param.getName(), paramValue);
                            }
                        } else {
                            node.setPropertyValue(param.getName(), param.getValue());
                        }
                    }
                }
            };
        }
        return afterPropertyChangeListener;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.commons.ui.swt.advanced.dataeditor.ExtendedToolbarView#createAddAllPushButton()
     */
    @Override
    protected AddAllPushButton createAddAllPushButton() {

        final PropertiesTableEditorModel tableEditorModel = (PropertiesTableEditorModel) getExtendedTableViewer()
                .getExtendedControlModel();

        if (!TableController.isNeedAddAllButton(tableEditorModel.getElemParameter())) {
            return null;
        }

        return new AddAllPushButtonForExtendedTable(this.toolbar, getExtendedTableViewer()) {

            @Override
            public boolean getEnabledState() {
                return super.getEnabledState() && (model == null || !model.getElemParameter().isBasedOnSubjobStarts());
            }

            @Override
            protected List<Object> getObjectToAdd() {
                if (!tableEditorModel.isButtonEnabled()) {
                    MessageDialog.openInformation(tableEditorModel.getTableViewer().getControl().getShell(), "Information",
                            "All output columns have been added already");
                    return new ArrayList<Object>();
                }

                IElement element = tableEditorModel.getElement();
                if (element != null && element instanceof INode) {
                    List<ColumnInfo> tableInputs = null;
                    IElementParameter param = tableEditorModel.getElemParameter();
                    // diplay a dialog for setting default values. see 0005416: When click Add All in a table, add
                    // the
                    // possibility to setup the default parameters value.
                    tableInputs = promptForDefaultValue(tableEditorModel.getTableViewer().getControl().getShell(), param);

                    if (tableInputs == null) {
                        return new ArrayList<Object>();
                    }

                    String paramColumnsName = "COLUMN";// default name //$NON-NLS-1$
                    String paramSizeName = "SIZE"; // default name //$NON-NLS-1$
                    for (Object object : param.getListItemsValue()) {
                        if (object instanceof IElementParameter) {
                            IElementParameter tableParameter = (IElementParameter) object;
                            if (tableParameter.getFieldType().equals(EParameterFieldType.COLUMN_LIST)) {
                                paramColumnsName = tableParameter.getName();
                            }
                            if (tableParameter.getContext() != null && tableParameter.getContext().equals("LENGTH")) { //$NON-NLS-1$
                                paramSizeName = tableParameter.getName();
                            }
                        }
                    }
                    INode node = (INode) element;
                    if (node.getMetadataList() != null && !node.getMetadataList().isEmpty()) {
                        IMetadataTable metadata = node.getMetadataList().get(0);
                        if (metadata.getListColumns() != null && !metadata.getListColumns().isEmpty()) {

                            List<Object> objects = new ArrayList<Object>();
                            for (IMetadataColumn column : metadata.getListColumns()) {
                                if (tableEditorModel.exist(column.getLabel())) {
                                    continue;
                                }

                                Object entry = tableEditorModel.createNewEntry();
                                if (!(entry instanceof Map)) {
                                    continue;
                                }

                                Map mapObject = (Map) entry;
                                if (mapObject.containsKey(paramColumnsName)) {
                                    mapObject.put(paramColumnsName, column.getLabel());
                                }
                                if (mapObject.containsKey(paramSizeName)) {
                                    if (column.getLength() != null) {
                                        mapObject.put(paramSizeName, Integer.toString(column.getLength()));
                                    }
                                }
                                // set default values
                                for (ColumnInfo col : tableInputs) {
                                    Object defaultValue = col.defaultValue;
                                    Object found = findDefaultName(column.getLabel(), col);
                                    if (found != null && !"".equals(found)) {
                                        defaultValue = found;
                                    }
                                    mapObject.put(col.parameter.getName(), defaultValue);
                                }

                                objects.add(entry);
                            }
                            return objects;
                        }
                    }
                }
                return Collections.EMPTY_LIST;
            }

            private List<ColumnInfo> promptForDefaultValue(Shell shell, IElementParameter param) {
                List<ColumnInfo> tableInputs = new ArrayList<ColumnInfo>();

                Object[] listItemsValue = param.getListItemsValue();
                String[] displayNames = param.getListItemsDisplayName();

                for (int i = 0; i < listItemsValue.length; i++) {
                    if (listItemsValue[i] instanceof IElementParameter) {
                        IElementParameter colParam = (IElementParameter) listItemsValue[i];
                        if (colParam.getFieldType().equals(EParameterFieldType.COLUMN_LIST)) {
                            continue;
                        }

                        ColumnInfo row = new ColumnInfo();
                        row.name = displayNames[i];
                        row.parameter = colParam;
                        tableInputs.add(row);
                    }
                }

                PromptDefaultValueDialog dialog = new PromptDefaultValueDialog(shell, tableInputs);
                if (dialog.open() == Window.OK) {
                    return tableInputs;
                } else {
                    return null;
                }
            }

        };
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.ui.extended.ExtendedToolbarView#createPastButton()
     */
    @Override
    public PastePushButton createPastePushButton() {
        PastePushButton pastePushButton = new PastePushButtonForExtendedTable(toolbar, extendedTableViewer) {

            @Override
            public boolean getEnabledState() {
                PropertiesTableEditorModel tableEditorModel = (PropertiesTableEditorModel) getExtendedTableViewer()
                        .getExtendedControlModel();

                Object data = SimpleClipboard.getInstance().getData();
                if (data == null || !(data instanceof List) || tableEditorModel.getElemParameter() == null
                        || tableEditorModel.getElemParameter().getListItemsDisplayCodeName() == null) {
                    return false;
                }
                @SuppressWarnings("rawtypes")
                List dataList = (List) data;
                boolean sameNumberOfParamAssSourceTable = true;
                if (dataList.isEmpty() || !(dataList.get(0) instanceof HashMap)) {
                    // only accept data from another property (list of HashMap)
                    return false;
                }
                @SuppressWarnings("rawtypes")
                HashMap sourceMap = (HashMap) dataList.get(0);
                int sourceColumnNumber = sourceMap.size();
                Object[] sourceArray = sourceMap.keySet().toArray();
                ArrayList<Object> sourceList = new ArrayList<Object>(Arrays.asList(sourceArray));
                String[] listItemsDisplayCodeName = tableEditorModel.getElemParameter().getListItemsDisplayCodeName();
                List<String> itemDisCodeNameList = null;
                int colNum = 0;
                if (listItemsDisplayCodeName != null) {
                    itemDisCodeNameList = Arrays.asList(listItemsDisplayCodeName);
                    colNum = listItemsDisplayCodeName.length;
                }
                ArrayList<String> list = new ArrayList<String>(itemDisCodeNameList);
                list.removeAll(sourceList);
                if (data != null) {
                    if (colNum <= sourceColumnNumber && list.size() == 0) {
                        sameNumberOfParamAssSourceTable = true;
                    } else {
                        sameNumberOfParamAssSourceTable = false;
                    }
                }
                return super.getEnabledState() && (model == null || !model.getElemParameter().isBasedOnSubjobStarts())
                        && sameNumberOfParamAssSourceTable;
            }

            @Override
            protected Command getCommandToExecute(ExtendedTableModel extendedTableModel, Integer indexWhereInsert) {
                return new PropertyTablePasteCommand<Map<String, Object>>(extendedTableModel, indexWhereInsert);
            }

        };
        // TDI-6568, after paste, fire change
        pastePushButton.addListener(getPropertyChangeListener(), false);
        return pastePushButton;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.ui.extended.ExtendedToolbarView#createExportPushButton()
     */
    @Override
    protected ExportPushButton createExportPushButton() {
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.ui.extended.ExtendedToolbarView#createPastButton()
     */
    @Override
    public ImportPushButton createImportPushButton() {
        return null;
    }

    @Override
    protected RemovePushButton createRemovePushButton() {
        RemovePushButtonForExtendedTable removePushButton = new RemovePushButtonForExtendedTable(this.toolbar,
                getExtendedTableViewer()) {

            @Override
            public boolean getEnabledState() {
                if (model != null
                        && !model.getElemParameter().getElement().isReadOnly()
                        && (model.getElemParameter().getName().equals("HADOOP_ADVANCED_PROPERTIES")
                                || model.getElemParameter().getName().equals("SPARK_ADVANCED_PROPERTIES") || model
                                .getElemParameter().getName().equals("HBASE_PARAMETERS"))) {
                    if (getExtendedTableViewer().getTable().getSelectionIndex() > -1) {
                        TableItem item = extendedTableViewer.getTable().getSelection()[0];
                        HashMap<String, String> itemMap = (HashMap<String, String>) item.getData();
                        List<HashMap<String, String>> parameterValue = (List<HashMap<String, String>>) model.getElemParameter()
                                .getValue();
                        for (HashMap<String, String> parameterValueMap : parameterValue) {
                            if (parameterValueMap != null && itemMap != null && parameterValueMap.get("PROPERTY") != null
                                    && itemMap.get("PROPERTY") != null) {
                                if (parameterValueMap.get("PROPERTY").equals(itemMap.get("PROPERTY"))
                                        && parameterValueMap.get("VALUE").equals(itemMap.get("VALUE"))) {
                                    if (parameterValueMap.get("BUILDIN") != null
                                            && parameterValueMap.get("BUILDIN").equals("TRUE")) {
                                        return true;
                                    }
                                }
                            }
                        }
                    }
                }
                return super.getEnabledState() && (model == null || !model.getElemParameter().isBasedOnSubjobStarts());
            }
        };
        // TDI-6568, after remove, fire change
        removePushButton.addListener(getPropertyChangeListener(), false);
        return removePushButton;
    }

    private Object findDefaultName(String outputName, ColumnInfo col) {
        if (col.parameter != null && outputName != null) {
            Object[] values = col.parameter.getListItemsValue();
            if (values != null) {
                for (Object o : values) {
                    if (outputName.equals(o)) {
                        return o;
                    }
                }
            }
        }
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.commons.ui.swt.advanced.dataeditor.ExtendedToolbarView#createMoveUpPushButton()
     */
    @Override
    protected MoveUpPushButton createMoveUpPushButton() {
        MoveUpPushButton moveUpPushButton = super.createMoveUpPushButton();
        // TDI-6568, after move, fire change
        moveUpPushButton.addListener(getPropertyChangeListener(), false);
        return moveUpPushButton;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.commons.ui.swt.advanced.dataeditor.ExtendedToolbarView#createMoveDownPushButton()
     */
    @Override
    protected MoveDownPushButton createMoveDownPushButton() {
        MoveDownPushButton moveDownPushButton = super.createMoveDownPushButton();
        // TDI-6568, after move, fire change
        moveDownPushButton.addListener(getPropertyChangeListener(), false);
        return moveDownPushButton;
    }

}
