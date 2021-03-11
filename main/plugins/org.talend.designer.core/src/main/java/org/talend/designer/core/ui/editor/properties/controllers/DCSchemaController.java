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
package org.talend.designer.core.ui.editor.properties.controllers;

import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.gef.commands.Command;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.fieldassist.DecoratedField;
import org.eclipse.jface.fieldassist.TextControlCreator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.views.properties.tabbed.ITabbedPropertyConstants;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.model.metadata.IMetadataColumn;
import org.talend.core.model.metadata.IMetadataTable;
import org.talend.core.model.process.EConnectionType;
import org.talend.core.model.process.EParameterFieldType;
import org.talend.core.model.process.IConnection;
import org.talend.core.model.process.IContextManager;
import org.talend.core.model.process.IElement;
import org.talend.core.model.process.IElementParameter;
import org.talend.core.model.process.INode;
import org.talend.core.model.process.INodeConnector;
import org.talend.core.ui.metadata.dialog.MetadataDialog;
import org.talend.core.ui.metadata.dialog.MetadataDialogForMerge;
import org.talend.core.ui.properties.tab.IDynamicProperty;
import org.talend.designer.core.i18n.Messages;
import org.talend.designer.core.model.components.EParameterName;
import org.talend.designer.core.ui.editor.cmd.ChangeMetadataCommand;
import org.talend.designer.core.ui.editor.connections.Connection;
import org.talend.designer.core.ui.editor.nodes.Node;
import org.talend.designer.core.ui.editor.properties.controllers.creator.SelectAllTextControlCreator;

public class DCSchemaController extends AbstractElementPropertySectionController {

    public DCSchemaController(IDynamicProperty dp) {
        super(dp);
    }

    SelectionListener schemaListener = new SelectionListener() {

        @Override
        public void widgetSelected(SelectionEvent e) {
            Command cmd = createButtonCommand((Button) e.getSource());
            executeCommand(cmd);
        }

        @Override
        public void widgetDefaultSelected(SelectionEvent e) {
            widgetSelected(e);
        }
    };

    @Override
    public Control createControl(Composite subComposite, IElementParameter param, int numInRow, int nbInRow, int top,
            Control lastControl) {
        this.curParameter = param;
        this.paramFieldType = param.getFieldType();
        FormData data = new FormData();
        Control lastDbControl = null;
        Button openListTable = addButton(subComposite, param, top, numInRow, nbInRow);
        lastDbControl = openListTable;
        data.top = new FormAttachment(0, top);
        data.height = STANDARD_HEIGHT - 2;
        if (openListTable != null) {
            openListTable.setLayoutData(data);
            openListTable.setData(PARAMETER_NAME, param.getName());
            openListTable.setEnabled(!param.isReadOnly());
            openListTable.addSelectionListener(schemaListener);
            openListTable.setToolTipText("Edit Schema"); //$NON-NLS-1$
        }
        Text labelText;
        final DecoratedField dField = new DecoratedField(subComposite, SWT.BORDER, new SelectAllTextControlCreator());
        Control cLayout = dField.getLayoutControl();
        labelText = (Text) dField.getControl();
        labelText.setData(PARAMETER_NAME, param.getName());
        labelText.setText(PARAMETER_NAME);
        cLayout.setBackground(subComposite.getBackground());
        labelText.setEditable(!param.isReadOnly());
        if (elem instanceof Node) {
            labelText.setToolTipText(VARIABLE_TOOLTIP + param.getVariableName());
        }
        addDragAndDropTarget(labelText);

        CLabel labelLabel = getWidgetFactory().createCLabel(subComposite, param.getDisplayName());
        data = new FormData();
        if (lastControl != null) {
            data.left = new FormAttachment(lastControl, 0);
        } else {
            data.left = new FormAttachment((((numInRow - 1) * MAX_PERCENT) / nbInRow), 0);
        }
        data.top = new FormAttachment(0, top);
        labelLabel.setLayoutData(data);
        if (numInRow != 1) {
            labelLabel.setAlignment(SWT.RIGHT);
        }
        data = new FormData();
        int currentLabelWidth = STANDARD_LABEL_WIDTH;
        GC gc = new GC(labelLabel);
        Point labelSize = gc.stringExtent(param.getDisplayName());
        gc.dispose();
        if ((labelSize.x + ITabbedPropertyConstants.HSPACE) > currentLabelWidth) {
            currentLabelWidth = labelSize.x + ITabbedPropertyConstants.HSPACE;
        }

        if (numInRow == 1) {
            if (lastControl != null) {
                data.left = new FormAttachment(lastControl, currentLabelWidth);
            } else {
                data.left = new FormAttachment(0, currentLabelWidth);
            }

        } else {
            data.left = new FormAttachment(labelLabel, 0, SWT.RIGHT);
        }
        if (openListTable != null) {
            data.right = new FormAttachment(openListTable, -5, SWT.LEFT);
        } else {
            data.right = new FormAttachment((numInRow * MAX_PERCENT) / nbInRow, 0);
            lastDbControl = labelText;
        }
        data.top = new FormAttachment(0, top);
        cLayout.setLayoutData(data);
        hashCurControls.put(param.getName(), labelText);
        Point initialSize = dField.getLayoutControl().computeSize(SWT.DEFAULT, SWT.DEFAULT);
        dynamicProperty.setCurRowSize(initialSize.y + ITabbedPropertyConstants.VSPACE);
        return lastDbControl;
    }

    @SuppressWarnings("deprecation")
    @Override
    public int estimateRowSize(Composite subComposite, IElementParameter param) {
        DecoratedField dField = new DecoratedField(subComposite, SWT.BORDER, new TextControlCreator());
        Point initialSize = dField.getLayoutControl().computeSize(SWT.DEFAULT, SWT.DEFAULT);
        dField.getLayoutControl().dispose();
        return initialSize.y + ITabbedPropertyConstants.VSPACE;
    }

    @Override
    public void refresh(IElementParameter param, boolean checkErrorsWhenViewRefreshed) {
        Text labelText = (Text) hashCurControls.get(param.getName());
        if (labelText == null || labelText.isDisposed()) {
            return;
        }
        Object value = param.getValue();
        boolean valueChanged = false;
        if (value == null) {
            labelText.setText(""); //$NON-NLS-1$
        } else {
            if (!value.equals(labelText.getText())) {
                labelText.setText((String) value);
                valueChanged = true;
            }
        }
        if (checkErrorsWhenViewRefreshed || valueChanged) {
            checkErrorsForPropertiesOnly(labelText);
        }
        fixedCursorPosition(param, labelText, value, valueChanged);
    }

    @Override
    public void propertyChange(PropertyChangeEvent arg0) {
        // TODO Auto-generated method stub

    }

    private Button addButton(final Composite subComposite, final IElementParameter param, final int top, int numInRow,
            final int nbInRow) {
        Button openListTable = getWidgetFactory().createButton(subComposite, "", SWT.PUSH); //$NON-NLS-1$
        openListTable.setText("Edit Schema");
        openListTable.setData(PARAMETER_NAME, param.getName());
        return openListTable;
    }

    private IMetadataTable getMetadataTableFromXml(Node node) {
        IElementParameter param = node.getElementParameterFromField(EParameterFieldType.DCSCHEMA);
        if (param.getValue() instanceof IMetadataTable) {
            IMetadataTable table = (IMetadataTable) param.getValue();
            return table;
        }
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.talend.designer.core.ui.editor.properties.controllers.AbstractRepositoryController#createButtonCommand(org
     * .eclipse.swt.widgets.Button)
     */
    @SuppressWarnings("unchecked")
    protected Command createButtonCommand(Button button) {
        Button inputButton = button;
        Map<INode, Map<IMetadataTable, Boolean>> inputInfos = new HashMap<INode, Map<IMetadataTable, Boolean>>();
        Node node;
        if (elem instanceof Node) {
            node = (Node) elem;
        } else { // else instanceof Connection
            node = (Node) ((Connection) elem).getSource();
        }
        String componentName = node.getComponent().getName();
        initConnectionParameters();
        IContextManager manager;
        if (part == null) {
            manager = new EmptyContextManager();
        } else {
            manager = part.getProcess().getContextManager();
        }
        IElement oldElement = elem;
        if (isUseExistingConnection()) {
            this.elem = connectionNode;
            initConnectionParametersWithContext(connectionNode, manager.getDefaultContext());
        } else {
            initConnectionParametersWithContext(elem, manager.getDefaultContext());
        }
        this.elem = oldElement;
        if (!GlobalServiceRegister.getDefault().isServiceRegistered(IDCService.class)) {
            return null;
        }
        IDCService restService = (IDCService) GlobalServiceRegister.getDefault().getService(IDCService.class);
        restService.setupRestHelperInstance(connParameters.getHost(), connParameters.getPort(), connParameters.getUserName(),
                connParameters.getPassword(), connParameters.getDirectory(), connParameters.isHttps());
        String entityName = elem.getPropertyValue("ENTITY").toString().replaceAll("\"", "");
        IMetadataTable inputMetadata = null, inputMetaCopy = null;
        Connection inputConec = null;
        String propertyName = (String) inputButton.getData(PARAMETER_NAME);
        IElementParameter param = node.getElementParameter(propertyName);
        IElementParameter connectionParam = param.getChildParameters().get(EParameterName.CONNECTION.getName());

        String connectionName = null;
        if (connectionParam != null) {
            connectionName = (String) connectionParam.getValue();
        }
        boolean inputReadOnly = false, outputReadOnly = false, inputReadOnlyNode = false, inputReadOnlyParam = false;

        for (Connection connec : (List<Connection>) node.getIncomingConnections()) {
            if (connec.isActivate()
                    && (EConnectionType.FLOW_MAIN.equals(connec.getLineStyle())
                            || EConnectionType.TABLE.equals(connec.getLineStyle())
                            || EConnectionType.FLOW_MERGE.equals(connec.getLineStyle()) || EConnectionType.FLOW_REF.equals(connec
                            .getLineStyle()))) {
                if (connectionName != null && !connec.getName().equals(connectionName)) {
                    continue;
                }
                inputMetadata = connec.getMetadataTable();
                inputMetaCopy = inputMetadata.clone();
                inputConec = connec;

                if (connec.getSource().isReadOnly()) {
                    inputReadOnlyNode = true;
                } else {
                    for (IElementParameter curParam : connec.getSource().getElementParameters()) {
                        if (curParam.getFieldType() == EParameterFieldType.SCHEMA_TYPE
                                || curParam.getFieldType() == EParameterFieldType.DCSCHEMA) {
                            if (curParam.isReadOnly()) {
                                inputReadOnlyParam = true;
                            }
                        }
                    }
                }

                // check if the inputMetadata is readonly
                if (inputMetadata != null) {
                    for (IMetadataColumn column : inputMetadata.getListColumns()) {
                        IMetadataColumn columnCopied = inputMetaCopy.getColumn(column.getLabel());
                        columnCopied.setCustom(column.isCustom());
                        columnCopied.setReadOnly(column.isReadOnly());
                    }
                    inputMetaCopy.setReadOnly(inputMetadata.isReadOnly());
                    inputReadOnly = prepareReadOnlyTable(inputMetaCopy, inputReadOnlyParam, inputReadOnlyNode);
                }

                // store the value for Dialog
                Map<IMetadataTable, Boolean> oneInput = new HashMap<IMetadataTable, Boolean>();
                oneInput.put(inputMetaCopy, inputReadOnly);
                inputInfos.put(connec.getSource(), oneInput);
            }
        }

        if (connectionParam != null && inputMetadata == null) {
            MessageDialog.openError(button.getShell(), Messages.getString("SchemaTypeController.inputNotSet"), //$NON-NLS-1$
                    Messages.getString("SchemaTypeController.connectionNotAvaliable")); //$NON-NLS-1$
            return null;
        }

        IMetadataTable originaleMetadataTable = getMetadataTableFromXml(node);
        // check if the outputMetadata is readonly
        IMetadataTable originaleOutputTable = node.getMetadataFromConnector(param.getContext());
        IMetadataTable outputMetaCopy = originaleOutputTable.clone();
        for (IMetadataColumn column : originaleOutputTable.getListColumns()) {
            IMetadataColumn columnCopied = outputMetaCopy.getColumn(column.getLabel());
            columnCopied.setCustom(column.isCustom());
            columnCopied.setReadOnly(column.isReadOnly());
        }
        outputMetaCopy.setReadOnly(originaleOutputTable.isReadOnly()
                || param.isReadOnly(node.getElementParametersWithChildrens()));
        outputMetaCopy.sortCustomColumns();

        // create the MetadataDialog
        MetadataDialog metaDialog = null;
        if (inputMetadata != null) {
            if (inputInfos != null && inputInfos.size() > 1 && connectionName == null) {
                MetadataDialogForMerge metaDialogForMerge = new MetadataDialogForMerge(composite.getShell(), inputInfos,
                        outputMetaCopy, node, getCommandStack());
                metaDialogForMerge.setText(Messages.getString("SchemaController.schemaOf") + node.getLabel()); //$NON-NLS-1$
                metaDialogForMerge.setInputReadOnly(inputReadOnly);
                metaDialogForMerge.setOutputReadOnly(outputReadOnly);
                if (metaDialogForMerge.open() == MetadataDialogForMerge.OK) {
                    outputMetaCopy = metaDialogForMerge.getOutputMetaData();

                    // check if the metadata is modified
                    boolean modified = false;
                    if (!outputMetaCopy.sameMetadataAs(originaleOutputTable, IMetadataColumn.OPTIONS_NONE)) {
                        modified = true;
                    } else {
                        if (inputMetadata != null) {
                            // Notice: the Map inputInfos maybe is modified by the dialog.
                            Set<INode> inputNodes = inputInfos.keySet();
                            for (INode inputNode : inputNodes) {
                                Map<IMetadataTable, Boolean> oneInput = inputInfos.get(inputNode);
                                inputMetaCopy = (IMetadataTable) oneInput.keySet().toArray()[0];
                                if (!inputMetaCopy.sameMetadataAs(inputNode.getMetadataList().get(0),
                                        IMetadataColumn.OPTIONS_NONE)) {
                                    modified = true;
                                }
                            }

                        }
                    }

                    // create the changeMetadataCommand
                    if (modified) {
                        Command changeMetadataCommand = null;

                        // only output, no input
                        if (inputInfos.isEmpty()) {
                            changeMetadataCommand = new ChangeMetadataCommand(node, param, null, null, null,
                                    originaleOutputTable, outputMetaCopy);

                        } else {
                            Set<INode> inputNodes = inputInfos.keySet();
                            int count = 0;
                            for (INode inputNode : inputNodes) {
                                Map<IMetadataTable, Boolean> oneInput = inputInfos.get(inputNode);
                                inputMetaCopy = (IMetadataTable) oneInput.keySet().toArray()[0];
                                if (count == 0) {
                                    changeMetadataCommand = new ChangeMetadataCommand(node, param, inputNode, inputNode
                                            .getMetadataList().get(0), inputMetaCopy, originaleOutputTable, outputMetaCopy);
                                } else {
                                    changeMetadataCommand = changeMetadataCommand.chain(new ChangeMetadataCommand(node, param,
                                            inputNode, inputNode.getMetadataList().get(0), inputMetaCopy,
                                            originaleOutputTable, outputMetaCopy));
                                }
                                count++;
                            }
                        }
                        return changeMetadataCommand;

                    }
                }

            } else {
                Node inputNode = (Node) (inputConec.getSource());
                if (inputMetaCopy.getAttachedConnector() == null) {
                    INodeConnector mainConnector;
                    if (inputNode.isELTComponent()) {
                        mainConnector = inputNode.getConnectorFromType(EConnectionType.TABLE);
                    } else {
                        mainConnector = inputNode.getConnectorFromType(EConnectionType.FLOW_MAIN);
                    }
                    inputMetaCopy.setAttachedConnector(mainConnector.getName());
                }
                metaDialog = new MetadataDialog(composite.getShell(), inputMetaCopy, inputNode, outputMetaCopy, node,
                        getCommandStack());
            }
        } else {
            metaDialog = new MetadataDialog(composite.getShell(), outputMetaCopy, node, getCommandStack());
        }

        /*
         * Datacert: Populating data in the edit schema dialog
         *
         * @author : virtusa
         */
        List<IMetadataColumn> columnList = new ArrayList<IMetadataColumn>();
        String prefix = "";
        if (componentName.equalsIgnoreCase(Messages.getString("DataCertController.tDataCertDenormalize.componentName"))) {
            String denorCol = elem.getPropertyValue("DC_COLUMN_DENORMALIZE").toString().replaceAll("\"", "");
            ;
            prefix = denorCol;
            IMetadataTable inputTable = null;
            if (node.getIncomingConnections() != null && node.getIncomingConnections().size() > 0) {
                IConnection inputConn = node.getIncomingConnections().get(0);
                if (inputConn != null) {
                    inputTable = inputConn.getMetadataTable();
                }
            }
            if (inputTable != null) {
                for (IMetadataColumn inCol : inputTable.getListColumns()) {
                    if (!inCol.getLabel().equals(denorCol)) {
                        columnList.add(inCol);
                    }
                }
            }
        }

        restService.addColumnsToSchema(entityName, componentName, prefix, columnList);
        outputMetaCopy.setListColumns(columnList);
        outputMetaCopy.setComment("Attribute Metadata");
        outputMetaCopy.setLabel("Attribute Metadata");
        outputMetaCopy.setTableName(entityName);
        if (metaDialog != null) {
            metaDialog.setText(Messages.getString("AbstractSchemaController.schema.title", node.getLabel())); //$NON-NLS-1$
            metaDialog.setInputReadOnly(false);
            metaDialog.setOutputReadOnly(false);
            if (metaDialog.open() == MetadataDialog.OK) {
                outputMetaCopy = metaDialog.getOutputMetaData();
                boolean modified = false;
                if (!outputMetaCopy.sameMetadataAs(originaleOutputTable, IMetadataColumn.OPTIONS_NONE)) {
                    modified = true;
                }
                if (modified) {
                    Node inputNode = null;
                    if (inputConec != null) {
                        inputNode = (Node) inputConec.getSource();
                    }
                    ChangeMetadataCommand changeMetadataCommand = new ChangeMetadataCommand(node, param, inputNode,
                            inputMetadata, inputMetaCopy, originaleOutputTable, outputMetaCopy);

                    return changeMetadataCommand;

                }
            }
        }
        return null;
    }

    private boolean prepareReadOnlyTable(IMetadataTable table, boolean readOnlyParam, boolean readOnlyElement) {
        boolean isCustom = false;
        if (table.isReadOnly()) {
            return true;
        }
        for (IMetadataColumn column : table.getListColumns()) {
            if (column.isCustom() && !column.isReadOnly()) {
                isCustom = true;
            }
        }
        if (!isCustom) {
            return readOnlyParam || readOnlyElement;
        }
        for (IMetadataColumn column : table.getListColumns()) {
            if (!column.isCustom()) {
                column.setReadOnly(table.isReadOnly());
            }
        }
        return readOnlyElement;
    }
}
