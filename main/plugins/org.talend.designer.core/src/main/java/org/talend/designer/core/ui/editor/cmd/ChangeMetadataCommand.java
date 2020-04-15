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
package org.talend.designer.core.ui.editor.cmd;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.gef.commands.Command;
import org.eclipse.jface.dialogs.MessageDialog;
import org.talend.commons.ui.gmf.util.DisplayUtils;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.model.components.EComponentType;
import org.talend.core.model.components.IODataComponent;
import org.talend.core.model.components.IODataComponentContainer;
import org.talend.core.model.metadata.ColumnNameChanged;
import org.talend.core.model.metadata.Dbms;
import org.talend.core.model.metadata.IMetadataColumn;
import org.talend.core.model.metadata.IMetadataTable;
import org.talend.core.model.metadata.MetadataTalendType;
import org.talend.core.model.metadata.MetadataToolHelper;
import org.talend.core.model.metadata.designerproperties.RepositoryToComponentProperty;
import org.talend.core.model.param.EConnectionParameterName;
import org.talend.core.model.process.EConnectionType;
import org.talend.core.model.process.EParameterFieldType;
import org.talend.core.model.process.Element;
import org.talend.core.model.process.IConnection;
import org.talend.core.model.process.IConnectionCategory;
import org.talend.core.model.process.IElementParameter;
import org.talend.core.model.process.INode;
import org.talend.core.model.process.INodeConnector;
import org.talend.core.model.utils.TalendTextUtils;
import org.talend.core.runtime.IAdditionalInfo;
import org.talend.core.runtime.services.IGenericWizardService;
import org.talend.designer.core.i18n.Messages;
import org.talend.designer.core.model.components.EParameterName;
import org.talend.designer.core.model.components.EmfComponent;
import org.talend.designer.core.ui.editor.connections.Connection;
import org.talend.designer.core.ui.editor.nodes.Node;
import org.talend.designer.core.ui.editor.process.Process;
import org.talend.designer.core.ui.editor.properties.controllers.ColumnListController;

/**
 * Command that will change a metadata in a node.
 *
 * $Id$
 *
 */
/**
 * DOC Administrator class global comment. Detailled comment <br/>
 *
 */
public class ChangeMetadataCommand extends Command {

    private INode node, inputNode;

    protected IMetadataTable currentOutputMetadata, newOutputMetadata, oldOutputMetadata;

    private boolean outputWasRepository = false, inputWasRepository = false;

    private IMetadataTable currentInputMetadata, newInputMetadata, oldInputMetadata;

    private IODataComponentContainer inputdataContainer;

    private IODataComponentContainer outputdataContainer;

    private IODataComponent dataComponent;

    private Boolean propagate;

    private final List<ChangeMetadataCommand> propagatedChange = new ArrayList<ChangeMetadataCommand>();

    private boolean internal = false;

    private boolean repositoryMode = false;

    private IElementParameter schemaParam;

    private IElementParameter inputSchemaParam;

    private String currentConnector;

    protected boolean isNotSim = false;

    private Map<String, String> columnRenameMap;

    // Default constructor.
    public ChangeMetadataCommand() {
    }

    public ChangeMetadataCommand(INode node, IElementParameter schemaParam, INode inputNode, IMetadataTable currentInputMetadata,
            IMetadataTable newInputMetadata, IMetadataTable currentOutputMetadata, IMetadataTable newOutputMetadata) {
        this.node = node;
        this.inputNode = inputNode;
        this.schemaParam = schemaParam;
        if (schemaParam == null) {
            currentConnector = EConnectionType.FLOW_MAIN.getName();
            for (IElementParameter param : node.getElementParameters()) {
                if ((param.getFieldType().equals(EParameterFieldType.SCHEMA_TYPE)
                        || param.getFieldType().equals(EParameterFieldType.SCHEMA_REFERENCE) || param.getFieldType().equals(
                        EParameterFieldType.DCSCHEMA))
                        && param.getContext().equals(currentConnector)) {
                    this.schemaParam = param;
                }
            }
        } else {
            currentConnector = this.schemaParam.getContext();
        }
        this.currentInputMetadata = currentInputMetadata;
        if (currentInputMetadata != null) {
            oldInputMetadata = currentInputMetadata.clone();
        } else {
            oldInputMetadata = null;
        }
        this.newInputMetadata = newInputMetadata;
        this.currentOutputMetadata = currentOutputMetadata;
        if (this.currentOutputMetadata == null) {
            this.currentOutputMetadata = node.getMetadataFromConnector(currentConnector);
        }
        oldOutputMetadata = this.currentOutputMetadata.clone();
        this.newOutputMetadata = newOutputMetadata;
        initializeContainer();
        setLabel(Messages.getString("ChangeMetadataCommand.changeMetadataValues")); //$NON-NLS-1$
    }

    public ChangeMetadataCommand(INode node, IElementParameter schemaParam, IMetadataTable currentOutputMetadata,
            IMetadataTable newOutputMetadata, IElementParameter inputSchemaParam) {
        this.inputSchemaParam = inputSchemaParam;
        init(node, schemaParam, currentOutputMetadata, newOutputMetadata);
    }

    public ChangeMetadataCommand(INode node, IElementParameter schemaParam, IMetadataTable currentOutputMetadata,
            IMetadataTable newOutputMetadata) {
        init(node, schemaParam, currentOutputMetadata, newOutputMetadata);
    }

    public void init(INode node, IElementParameter schemaParam, IMetadataTable currentOutputMetadata,
            IMetadataTable newOutputMetadata) {
        this.node = node;
        this.schemaParam = schemaParam;
        if (schemaParam == null) {
            if (newOutputMetadata.getAttachedConnector() != null) {
                currentConnector = newOutputMetadata.getAttachedConnector();
            } else {
                if (node.isELTComponent()) {
                    currentConnector = EConnectionType.TABLE.getName();
                } else {
                    currentConnector = EConnectionType.FLOW_MAIN.getName();
                }
            }
            for (IElementParameter param : node.getElementParameters()) {
                if ((param.getFieldType().equals(EParameterFieldType.SCHEMA_TYPE) || param.getFieldType().equals(
                        EParameterFieldType.SCHEMA_REFERENCE))
                        && param.getContext().equals(currentConnector)) {
                    this.schemaParam = param;
                }
            }
        } else {
            currentConnector = this.schemaParam.getContext();
        }

        this.inputNode = null;
        this.currentInputMetadata = null;
        this.newInputMetadata = null;
        oldInputMetadata = null;
        this.currentOutputMetadata = currentOutputMetadata;
        if (this.currentOutputMetadata == null) {
            this.currentOutputMetadata = node.getMetadataFromConnector(currentConnector);
        }
        if (currentOutputMetadata == null && newOutputMetadata != null) {
            currentOutputMetadata = newOutputMetadata.clone(true);
        }
        oldOutputMetadata = this.currentOutputMetadata.clone(true);
        this.newOutputMetadata = newOutputMetadata.clone(true);
        this.newOutputMetadata.setReadOnly(this.currentOutputMetadata.isReadOnly());
        initializeContainer();
        setLabel(Messages.getString("ChangeMetadataCommand.changeMetadataValues")); //$NON-NLS-1$
    }

    public void setRepositoryMode(boolean repositoryMode) {
        this.repositoryMode = repositoryMode;
    }

    private void initializeContainer() {
        outputdataContainer = new IODataComponentContainer();
        for (Connection connec : (List<Connection>) node.getIncomingConnections()) {
            if (connec.getLineStyle().equals(EConnectionType.FLOW_MAIN)) {
                IODataComponent input = null;
                if (newInputMetadata == null) {
                    input = new IODataComponent(connec);
                } else {
                    if (connec.getMetaName().equals(newInputMetadata.getTableName())) {
                        input = new IODataComponent(connec, newInputMetadata);
                    }
                }
                if (input != null) {
                    outputdataContainer.getInputs().add(input);
                }

            }
        }
        for (Connection connec : (List<Connection>) node.getOutgoingConnections()) {
            if (connec.getLineStyle().equals(EConnectionType.FLOW_MAIN) || isinputContainerOutput(connec)
                    || ((connec.getLineStyle().equals(EConnectionType.FLOW_MERGE) && (connec.getInputId() == 1)))) {
                if ((!connec.getSource().getConnectorFromType(connec.getLineStyle()).isMultiSchema())
                        || (connec.getMetaName().equals(newOutputMetadata.getTableName()))) {
                    IODataComponent output = new IODataComponent(connec, newOutputMetadata);
                    outputdataContainer.getOuputs().add(output);
                }
            }
        }

        if (inputNode != null) {
            inputdataContainer = new IODataComponentContainer();
            for (Connection connec : (List<Connection>) inputNode.getOutgoingConnections()) {
                if (connec.getTarget().equals(node)) {
                    if ((!connec.getSource().getConnectorFromType(connec.getLineStyle()).isMultiSchema())
                            || (connec.getMetaName().equals(newInputMetadata.getTableName()))) {
                        IODataComponent output = new IODataComponent(connec, newInputMetadata);
                        inputdataContainer.getOuputs().add(output);
                    }
                }
            }
        }
    }

    private boolean isinputContainerOutput(Connection connec) {
        boolean isREF = connec.getLineStyle().equals(EConnectionType.FLOW_REF);
        if (isREF && (connec.getTarget().getOutgoingConnections().size() == 1)) {
            if (connec.getTarget().getOutgoingConnections().get(0).getLineStyle().equals(EConnectionType.FLOW_REF)) {
                return true;
            }
        }
        return false;
    }

    private void setInternal(boolean internal) {
        this.internal = internal;
    }

    public static boolean askPropagate() {
        Boolean needPropagate = MessageDialog.openQuestion(DisplayUtils.getDefaultShell(false),
                Messages.getString("ChangeMetadataCommand.messageDialog.propagate"), //$NON-NLS-1$
                Messages.getString("ChangeMetadataCommand.messageDialog.questionMessage")); //$NON-NLS-1$
        return needPropagate;
    }

    private boolean getPropagate() {
        if (propagate == null) {
            return askPropagate();
        }
        return propagate;
    }

    @SuppressWarnings("unchecked")
    protected void updateColumnList(IMetadataTable oldTable, IMetadataTable newTable) {
        final List<ColumnNameChanged> columnNameChanged = MetadataToolHelper.getColumnNameChanged(oldTable, newTable);

        if (inputNode != null) {
            List<IElementParameter> eps = (List<IElementParameter>) inputNode.getElementParameters();
            if (eps != null) {
                boolean end = false;
                for (int i = 0; i < eps.size() && !end; i++) {
                    IElementParameter parameter = eps.get(i);
                    if (parameter.getFieldType() == EParameterFieldType.TABLE) {
                        end = true;
                        if (parameter != null) {
                            List<Map<String, Object>> map2 = (List<Map<String, Object>>) parameter.getValue();
                            if (map2 != null && inputNode.getMetadataList().get(0).getListColumns().size() != map2.size()) {
                                ColumnListController.updateColumnList(inputNode, columnNameChanged);
                            }
                        }
                    }
                }
            }
            inputNode.setPropertyValue(EParameterName.UPDATE_COMPONENTS.getName(), Boolean.TRUE);
        }
        node.setPropertyValue(EParameterName.UPDATE_COMPONENTS.getName(), Boolean.TRUE);
    }

    public void execute(Boolean propagateP) {
        this.propagate = propagateP;
        if (currentOutputMetadata == null && node != null) {
            currentOutputMetadata = node.getMetadataFromConnector(currentConnector);
        }
        setInternal(true);
        execute();
    }

    private void propagateDatas(boolean isExecute) {
        // update currentConnector when flow main type
        if (schemaParam != null
                && (schemaParam.getFieldType().equals(EParameterFieldType.SCHEMA_TYPE) || schemaParam.getFieldType().equals(
                        EParameterFieldType.SCHEMA_REFERENCE))
                && EConnectionType.FLOW_MAIN.getDefaultMenuName().toUpperCase().equals(schemaParam.getContext())) {
            currentConnector = EConnectionType.FLOW_MAIN.getName();
        }
        String baseConnectorForCurrentNode = node.getConnectorFromName(currentConnector).getBaseSchema();

        // Propagate :
        if (outputdataContainer != null
                && (!outputdataContainer.getInputs().isEmpty() || !outputdataContainer.getOuputs().isEmpty())) {
            for (IODataComponent currentIO : outputdataContainer.getInputs()) {
                INode sourceNode = currentIO.getSource();
                if (currentIO.hasChanged()
                        && (sourceNode.getConnectorFromName(currentIO.getConnection().getConnectorName()).getBaseSchema()
                                .equals(baseConnectorForCurrentNode))) {
                    sourceNode.metadataOutputChanged(currentIO, currentIO.getName());
                    if (isExecute) {
                        currentIO.setTable(oldInputMetadata);
                        currentIO.setColumnNameChanged(null);
                    } else {
                        currentIO.setTable(newInputMetadata);
                        currentIO.setColumnNameChanged(null);
                    }
                }
            }
            Boolean doPropagate = null;
            for (IODataComponent currentIO : outputdataContainer.getOuputs()) {
                INodeConnector nodeConnector = null;
                String baseConnector = null;

                Node sourceNode = (Node) currentIO.getSource();
                nodeConnector = sourceNode.getConnectorFromName(currentIO.getConnection().getConnectorName());
                baseConnector = nodeConnector.getBaseSchema();

                INode targetNode = currentIO.getTarget();

                boolean sourceIsBuiltIn = ((Node) currentIO.getSource()).getConnectorFromType(
                        currentIO.getConnection().getLineStyle()).isMultiSchema();

                boolean targetIsBuiltIn = ((Node) targetNode).getConnectorFromType(currentIO.getConnection().getLineStyle())
                        .isMultiSchema();
                boolean isJoblet = ((Node) targetNode).isJoblet();
                if (!isJoblet
                        && baseConnector.equals(baseConnectorForCurrentNode)
                        && (targetIsBuiltIn || (targetNode.getMetadataFromConnector(baseConnector) != null && !targetNode
                                .getMetadataFromConnector(baseConnector).sameMetadataAs(newOutputMetadata)))) {

                    targetNode.metadataInputChanged(currentIO, currentIO.getUniqueName());
                    if (isExecute) {
                        if (targetNode instanceof Node) {
                            if(doPropagate == null){
                                doPropagate = getPropagate();
                            }
                            if (((Node) targetNode).getComponent().isSchemaAutoPropagated() && doPropagate
                                    && targetNode.getMetadataList().size() > 0) {
                                IMetadataTable tmpClone;
                                if (sourceIsBuiltIn) {
                                    IMetadataTable tab = node.getMetadataTable(currentIO.getConnection().getMetadataTable()
                                            .getTableName());
                                    if (tab == null && node.getJobletNode() != null) {
                                        tab = node.getJobletNode().getMetadataTable(
                                                currentIO.getConnection().getMetadataTable().getTableName());
                                    }
                                    tmpClone = tab.clone(true);
                                } else {
                                    IMetadataTable tab = node.getMetadataFromConnector(currentIO.getConnection()
                                            .getConnectorName());
                                    if (tab == null && node.getJobletNode() != null) {
                                        tab = node.getJobletNode().getMetadataFromConnector(
                                                currentIO.getConnection().getConnectorName());
                                    }
                                    tmpClone = tab.clone(true);
                                }
                                IMetadataTable toCopy = newOutputMetadata.clone();

                                // wzhang modify to add feature 7611

                                String dbmsId = null;
                                IMetadataTable copy;
                                if (((Node) targetNode).getMetadataFromConnector(baseConnector) != null) {
                                    dbmsId = targetNode.getMetadataFromConnector(baseConnector).getDbms();
                                    MetadataToolHelper.copyTable(dbmsId, toCopy, tmpClone);
                                    toCopy = tmpClone;

                                    // only if the target node have exactly the same connector
                                    copy = ((Node) targetNode).getMetadataFromConnector(baseConnector).clone(true);
                                } else {
                                    final String mainConnector = "FLOW"; // can only be FLOW right now for this case. //$NON-NLS-1$

                                    dbmsId = targetNode.getMetadataFromConnector(mainConnector).getDbms();
                                    MetadataToolHelper.copyTable(dbmsId, toCopy, tmpClone);
                                    toCopy = tmpClone;
                                    // if don't have the same connector, take the main connector of the component.

                                    copy = ((Node) targetNode).getMetadataFromConnector(mainConnector).clone(true);
                                }
                                // MetadataTool.copyTable(toCopy, copy);
                                // wzhang modify to add feature 7611
                                MetadataToolHelper.copyTable(dbmsId, toCopy, copy);
                                ChangeMetadataCommand cmd = new ChangeMetadataCommand(targetNode, null, null, copy,
                                        inputSchemaParam);
                                if (outputdataContainer.getOuputs().size() > 0) {
                                    List<ColumnNameChanged> columnNameChanged = outputdataContainer.getOuputs().get(0)
                                            .getColumnNameChanged();
                                    for (IODataComponent dataComp : cmd.outputdataContainer.getOuputs()) {
                                        dataComp.setColumnNameChanged(columnNameChanged);
                                    }
                                }
                                cmd.setRepositoryMode(repositoryMode);
                                cmd.execute(true);
                                propagatedChange.add(cmd);
                            }
                        }
                        currentIO.setTable(oldOutputMetadata);
                        currentIO.setColumnNameChanged(null);
                    } else {
                        if (targetNode instanceof Node) {
                            if(doPropagate == null){
                                doPropagate = getPropagate();
                            }
                            if (!targetIsBuiltIn && doPropagate) {
                                if (((Node) targetNode).getComponent().isSchemaAutoPropagated()) {
                                    if (outputdataContainer.getOuputs().size() > 0) {
                                        List<ColumnNameChanged> columnNameChanged = outputdataContainer.getOuputs().get(0)
                                                .getColumnNameChanged();
                                        for (ChangeMetadataCommand cmd : propagatedChange) {
                                            for (IODataComponent dataComp : cmd.outputdataContainer.getOuputs()) {
                                                dataComp.setColumnNameChanged(columnNameChanged);
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        currentIO.setTable(newOutputMetadata);
                        currentIO.setColumnNameChanged(null);
                    }
                }
            }
        } else if (dataComponent != null) {
            for (IConnection outgoingConnection : node.getOutgoingConnections()) {
                if (outgoingConnection.getConnectorName().equals(currentConnector)) {
                    outgoingConnection.getTarget().metadataInputChanged(dataComponent, outgoingConnection.getName());
                }
            }
        } else {
            if (!node.getOutgoingConnections().isEmpty()) {
                IMetadataTable relativeOldOutputMetadata = null;
                IMetadataTable relativeNewOutputMetadata = null;
                if (isExecute) {
                    relativeOldOutputMetadata = oldOutputMetadata;
                    relativeNewOutputMetadata = newOutputMetadata;
                } else {
                    relativeOldOutputMetadata = newOutputMetadata;
                    relativeNewOutputMetadata = oldOutputMetadata;
                }
                for (IConnection outgoingConnection : node.getOutgoingConnections()) {
                    if (!outgoingConnection.getLineStyle().hasConnectionCategory(IConnectionCategory.DATA)) {
                        continue;
                    }
                    final Node target = (Node) outgoingConnection.getTarget();
                    if (target != null && target.getExternalNode() != null) {
                        List<IMetadataColumn> oldListColumns = relativeOldOutputMetadata.getListColumns();
                        List<IMetadataColumn> newListColumns = relativeNewOutputMetadata.getListColumns();
                        List<ColumnNameChanged> columnNameChanges = new ArrayList<ColumnNameChanged>();
                        int size = oldListColumns.size();
                        int newSize = newListColumns.size();
                        if (newSize < size) {
                            size = newSize;
                        }
                        IODataComponent output = new IODataComponent(outgoingConnection, relativeNewOutputMetadata);
                        if (newListColumns != null) {
                            List<ColumnNameChanged> newColumnsList = output.getNewMetadataColumns();
                            // new added columns list
                            Set<String> newAddedColumns = new HashSet<String>();
                            // newest columns after user changed
                            Set<String> newestColumns = new HashSet<String>();

                            // init
                            if (newColumnsList != null) {
                                for (ColumnNameChanged columnChanged : newColumnsList) {
                                    newAddedColumns.add(columnChanged.getNewName());
                                }
                            }
                            for (IMetadataColumn metadataColumn : newListColumns) {
                                newestColumns.add(metadataColumn.getLabel());
                            }

                            // check
                            for (int i = 0; i < size; i++) {
                                IMetadataColumn oldMetadataColumn = oldListColumns.get(i);
                                String columnName = oldMetadataColumn.getLabel();
                                // if this column(before changing) is not exists in the new columns(after changing),
                                // there are two possible truth: 1. this column has been renamed; 2. this column has
                                // been removed
                                if (!newestColumns.contains(columnName)) {
                                    IMetadataColumn newMetadataColumn = newListColumns.get(i);
                                    String newColumnNameAtThisIndex = newMetadataColumn.getLabel();
                                    // if the column at the same position in new table is a new column(two possible
                                    // truth: 1. an old column's name has been changed; 2. user add a new column);
                                    // For now, Seems it is very hard to judge whether it is a renamed column or a new
                                    // column, so we suppose the more possible truth is that it is a renamed column
                                    if (newAddedColumns.contains(newColumnNameAtThisIndex)) {
                                        columnNameChanges.add(new ColumnNameChanged(columnName, newColumnNameAtThisIndex));
                                    }
                                }
                            }
                        }

                        output.setColumnNameChanged(columnNameChanges);
                        target.metadataInputChanged(output, outgoingConnection.getName());

                    }
                }
            }
        }

        if (inputdataContainer != null) {
            for (IODataComponent currentIO : inputdataContainer.getOuputs()) {
                if (currentIO.hasChanged()
                        && (currentIO.getSource().getConnectorFromName(currentIO.getConnection().getConnectorName())
                                .getBaseSchema().equals(currentConnector))) {
                    INode targetNode = currentIO.getTarget();
                    targetNode.metadataInputChanged(currentIO, currentIO.getUniqueName());
                    if (isExecute) {
                        currentIO.setTable(oldInputMetadata);
                        currentIO.setColumnNameChanged(null);
                    } else {
                        currentIO.setTable(newInputMetadata);
                        currentIO.setColumnNameChanged(null);
                    }
                }
            }
        }
        // End propagate
    }

    @Override
    public void execute() {
        propagatedChange.clear();

        propagateDatas(true);
        if (currentInputMetadata != null) {
            if (!currentInputMetadata.sameMetadataAs(newInputMetadata, IMetadataColumn.OPTIONS_NONE)) {
                if (!currentInputMetadata.sameMetadataAs(newInputMetadata, IMetadataColumn.OPTIONS_IGNORE_USED)) {
                    String type = (String) inputNode.getPropertyValue(EParameterName.SCHEMA_TYPE.getName());
                    if (type != null) {
                        if (type.equals(EmfComponent.REPOSITORY)) {
                            inputWasRepository = true;
                            inputNode.setPropertyValue(EParameterName.SCHEMA_TYPE.getName(), EmfComponent.BUILTIN);
                        }
                    }
                }
                MetadataToolHelper.copyTable(newInputMetadata, currentInputMetadata);
                currentOutputMetadata.setOriginalColumns(newOutputMetadata.getOriginalColumns());
            }
        }

        if (!currentOutputMetadata.sameMetadataAs(newOutputMetadata, IMetadataColumn.OPTIONS_NONE, true)) {
            if (!currentOutputMetadata.sameMetadataAs(newOutputMetadata, IMetadataColumn.OPTIONS_IGNORE_USED, true)) {
                String type = (String) node.getPropertyValue(EParameterName.SCHEMA_TYPE.getName());
                if (type != null && type.equals(EmfComponent.REPOSITORY) && !repositoryMode) {
                    // for one node has several schema_type,set mode for the current one
                    if ((node.getElementParameter("SCHEMA_TYPE").getContext()
                            .equals(currentOutputMetadata.getAttachedConnector()))) {
                        outputWasRepository = true;
                        node.setPropertyValue(EParameterName.SCHEMA_TYPE.getName(), EmfComponent.BUILTIN);
                    }
                }
            }
            MetadataToolHelper.copyTable(newOutputMetadata, currentOutputMetadata);
            currentOutputMetadata.setOriginalColumns(newOutputMetadata.getOriginalColumns());
        }
        if (inputSchemaParam != null
                && inputSchemaParam.getChildParameters().get(EParameterName.SCHEMA_TYPE.getName()) != null
                && EmfComponent.REPOSITORY.equals(inputSchemaParam.getChildParameters().get(EParameterName.SCHEMA_TYPE.getName())
                        .getValue())) {
            // add by wzhang to fix bug 7898.
            IElementParameter elementParameter = node.getElementParameter(EParameterName.MAPPING.getName());
            if (elementParameter != null) {
                if (elementParameter.getValue() instanceof String) {
                    String value = (String) elementParameter.getValue();
                    if (!isDBComponent(value)) {
                        schemaParam.getChildParameters().get(EParameterName.SCHEMA_TYPE.getName())
                                .setValue(EmfComponent.REPOSITORY);
                        schemaParam
                                .getChildParameters()
                                .get(EParameterName.REPOSITORY_SCHEMA_TYPE.getName())
                                .setValue(
                                        inputSchemaParam.getChildParameters()
                                                .get(EParameterName.REPOSITORY_SCHEMA_TYPE.getName()).getValue());
                    }
                }
            }
        }

        for (INodeConnector connector : node.getListConnector()) {
            if (hasSameSchema(connector)) {
                // if there is some other schema dependant of this one, modify them
                MetadataToolHelper.copyTable(newOutputMetadata, node.getMetadataFromConnector(connector.getName()));
                updateComponentSchema(node, node.getMetadataFromConnector(connector.getName()));
            }
        }

        updateComponentSchema(node, currentOutputMetadata);

        List<ColumnNameChanged> columnNameChanged = MetadataToolHelper.getColumnNameChanged(oldOutputMetadata, newOutputMetadata);
        ColumnListController.updateColumnList(node, columnNameChanged, true);

        if (inputNode != null) {
            updateComponentSchema(inputNode, currentInputMetadata);
            List<ColumnNameChanged> inputColumnNameChangedExt = MetadataToolHelper.getColumnNameChangedExt(inputNode,
                    oldInputMetadata, newInputMetadata);
            ColumnListController.updateColumnList(node, inputColumnNameChangedExt);
        }
        //
        List<ColumnNameChanged> outputColumnNameChangedExt = MetadataToolHelper.getColumnNameChangedExt(node, oldOutputMetadata,
                newOutputMetadata);
        syncOutputNodeColumnsList(outputColumnNameChangedExt);

        setTableMAPPING();

        if (!internal) {
            if (!oldOutputMetadata.sameMetadataAs(newOutputMetadata, IMetadataColumn.OPTIONS_NONE)) {
                updateColumnList(oldOutputMetadata, newOutputMetadata);
                ((Process) node.getProcess()).checkProcess();
            }
        }
        refreshMetadataChanged();
    }

    /**
     * Checks whether {@code connector} has the same base schema as {@link #currentConnector}.
     * This is the case for FLOW and REJECT connectors as REJECT schema depends on FLOW schema.
     * It contains all columns of FLOW schema and additional error columns. However, this is
     * true only for javaject and TCOMP components, but not for Tacokit. FLOW and REJECT schemas
     * are independent for Tacokit component. Thus, method always return false for Tacokit components.
     *
     * @param connector node connector to check
     * @return true, if connector has the same base schema as current connector
     */
    private boolean hasSameSchema(final INodeConnector connector) {
        return (!connector.getName().equals(currentConnector)) && connector.getBaseSchema().equals(currentConnector)
                && !isTacokit(connector);
    }

    /**
     * Checks whethere specified INodeConnector is Tacokit connector
     *
     * @param connector node connecto to check
     * @return true if it is Tacokit connector
     */
    private boolean isTacokit(final INodeConnector connector) {
        return (IAdditionalInfo.class.isInstance(connector))
                && "tacokit".equals(IAdditionalInfo.class.cast(connector).getInfo("CONNECTOR_TYPE"));
    }

    private void updateComponentSchema(INode selectedNode, IMetadataTable table) {
        IGenericWizardService wizardService = null;
        if (GlobalServiceRegister.getDefault().isServiceRegistered(IGenericWizardService.class)) {
            wizardService = (IGenericWizardService) GlobalServiceRegister.getDefault().getService(IGenericWizardService.class);
        }
        if (wizardService != null) {
            wizardService.updateComponentSchema(selectedNode, table);
        }
    }

    private org.talend.core.model.metadata.builder.connection.Connection connection;

    /**
     * wzhang Comment method "isDBComponent".
     */
    private boolean isDBComponent(String dbmsId) {
        if (dbmsId != null) {
            try {
                Dbms dbms = MetadataTalendType.getDbms(dbmsId);
                if (dbms != null) {
                    return true;
                }
            } catch (Exception e) {
                // nothing to do
            }
        }
        return false;
    }

    /*
     * use to synchronize column list for output connections.
     */
    private void syncOutputNodeColumnsList(List<ColumnNameChanged> columnNameChanged) {
        if (outputdataContainer == null) {
            return;
        }
        for (IConnection conn : node.getOutgoingConnections()) {
            INode targetNode = conn.getTarget();
            EConnectionType connStyle = conn.getLineStyle();
            if (EConnectionType.FLOW_MAIN.equals(connStyle) || EConnectionType.FLOW_MERGE.equals(connStyle)
                    || EConnectionType.FLOW_REF.equals(connStyle)) {
                ColumnListController.updateColumnList(targetNode, columnNameChanged);
                // fix for TDI-23202
                // reset value in order to call "firePropertyChange(RETURNS_CHANGED, null, null)"
                IElementParameter defaultMap = targetNode.getElementParameter(EParameterName.DEFAULT_MAP.getName());
                if (defaultMap != null) {
                    if ("tFlowToIterate".equals(targetNode.getComponent().getName())) {
                        // update target properties incase any old columns are removed
                        IElementParameter elementParameter = targetNode.getElementParameter("MAP");
                        if (elementParameter != null) {
                            Object value = elementParameter.getValue();
                            if (value instanceof List) {
                                for (Object obj : (List) value) {
                                    if (obj instanceof Map) {
                                        Object object = ((Map) obj).get("VALUE");
                                        if (newOutputMetadata != null && !newOutputMetadata.getListColumns().isEmpty()) {
                                            boolean found = false;
                                            for (IMetadataColumn column : newOutputMetadata.getListColumns()) {
                                                if (column.getLabel().equals(object)) {
                                                    found = true;
                                                }
                                            }
                                            if (!found) {
                                                ((Map) obj).put("VALUE", newOutputMetadata.getListColumns().get(0).getLabel());
                                            }
                                        } else {
                                            ((Map) obj).put("VALUE", "");
                                        }
                                    }
                                }
                            }
                        }
                    }
                    targetNode.setPropertyValue(EParameterName.DEFAULT_MAP.getName(), defaultMap.getValue());
                }
            }
        }

    }

    protected void setTableMAPPING() {
        if (getConnection() != null) {
            String componentName = node.getComponent().getName();
            for (IElementParameter parameter : node.getElementParameters()) {
                if (parameter.getFieldType() == EParameterFieldType.TABLE) {
                    if (parameter.getRepositoryValue() != null && parameter.getRepositoryValue().equals("XML_MAPPING")) { //$NON-NLS-1$
                        List<Map<String, Object>> value2 = (List<Map<String, Object>>) parameter.getValue();
                        RepositoryToComponentProperty.getTableXMLMappingValue(getConnection(), value2, newOutputMetadata,
                                getColumnRenameMap());
                        IElementParameter elementParameter = node.getElementParameter(EParameterName.PROPERTY_TYPE.getName());
                        if (elementParameter != null) {
                            if (EmfComponent.BUILTIN.equals(elementParameter.getValue())) {
                                parameter.setRepositoryValueUsed(false);
                            } else {
                                parameter.setRepositoryValueUsed(true);
                            }
                        }
                    } else if (componentName != null
                            && (componentName.startsWith("tHBase") || componentName.startsWith("tMapRDB"))//$NON-NLS-1$//$NON-NLS-2$
                            && ("MAPPING".equals(parameter.getName()) || "FAMILIES".equals(parameter.getName()))) {//$NON-NLS-1$//$NON-NLS-2$
                        Object value = RepositoryToComponentProperty.getColumnMappingValue(connection, newOutputMetadata);
                        if (value != null) {
                            parameter.setValue(value);
                        }
                    }
                }
            }
        }
    }

    public Map<String, String> getColumnRenameMap() {
        return this.columnRenameMap;
    }

    public void setColumnRenameMap(Map<String, String> columnRenameMap) {
        this.columnRenameMap = columnRenameMap;
    }

    @Override
    public void undo() {
        propagateDatas(false);

        if (currentInputMetadata != null) {
            if (!currentInputMetadata.sameMetadataAs(oldInputMetadata, IMetadataColumn.OPTIONS_NONE)) {
                currentInputMetadata.setListColumns(oldInputMetadata.getListColumns());
                if (inputWasRepository) {
                    inputNode.setPropertyValue(EParameterName.SCHEMA_TYPE.getName(), EmfComponent.REPOSITORY);
                }
            }
        }
        if (!currentOutputMetadata.sameMetadataAs(oldOutputMetadata, IMetadataColumn.OPTIONS_NONE)) {
            List<IMetadataColumn> currentColumns = new ArrayList<IMetadataColumn>(oldOutputMetadata.getListColumns());
            currentOutputMetadata.setListColumns(currentColumns);
            MetadataToolHelper.copyTable(oldOutputMetadata, currentOutputMetadata);
        }

        for (INodeConnector connector : node.getListConnector()) {
            if ((!connector.getName().equals(currentConnector)) && connector.getBaseSchema().equals(currentConnector)) {
                MetadataToolHelper.copyTable(oldOutputMetadata, node.getMetadataFromConnector(connector.getName()));
            }
        }
        if (outputWasRepository) {
            node.setPropertyValue(EParameterName.SCHEMA_TYPE.getName(), EmfComponent.REPOSITORY);
        }
        for (ChangeMetadataCommand cmd : propagatedChange) {
            cmd.undo();
        }

        List<ColumnNameChanged> columnNameChanged = MetadataToolHelper.getColumnNameChanged(oldOutputMetadata, newOutputMetadata);
        ColumnListController.updateColumnList(node, columnNameChanged, true);
        // newOutputMetadata.setListColumns(new ArrayList<IMetadataColumn>(oldOutputMetadata.getListColumns()));

        if (!internal) {
            updateColumnList(newOutputMetadata, oldOutputMetadata);
            ((Process) node.getProcess()).checkProcess();
        }
        refreshMetadataChanged();
    }

    /**
     * Getter for connection.
     *
     * @return the connection
     */
    public org.talend.core.model.metadata.builder.connection.Connection getConnection() {
        return this.connection;
    }

    /**
     * Sets the connection.
     *
     * @param connection the connection to set
     */
    public void setConnection(org.talend.core.model.metadata.builder.connection.Connection connection) {
        this.connection = connection;
    }

    /**
     * qzhang Comment method "setDBTableFieldValue".
     */
    protected void setDBTableFieldValue(Element curNode, String newdbTableName, String olddbTableName) {
        // add the code for the DBTable Field. bug 1304.
        if (curNode != null) {
            String uniqueName = ((Node) curNode).getUniqueName();
            IElementParameter dbTableElementField = curNode.getElementParameterFromField(EParameterFieldType.DBTABLE);
            if (dbTableElementField == null) {
                dbTableElementField = curNode.getElementParameter(EParameterName.TABLE.getName());
            }
            if(((Node) curNode).getComponent().getComponentType() == EComponentType.GENERIC){
                dbTableElementField = curNode.getElementParameter(EConnectionParameterName.GENERIC_TABLENAME.getDisplayName());
            }
            changeTableNameParameter(newdbTableName, olddbTableName, uniqueName, dbTableElementField);
            if (((Node) curNode).isELTComponent()) {
                IElementParameter eltTablename = curNode.getElementParameter("ELT_TABLE_NAME"); //$NON-NLS-1$
                changeTableNameParameter(newdbTableName, olddbTableName, uniqueName, eltTablename);
            }
        }
    }

    protected void setSAPFunctionName(Element curNode, String functionName) {
        if (functionName == null) {
            return;
        }

        //        IElementParameter parameter = curNode.getElementParameter("SAP_FUNCTION"); //$NON-NLS-1$
        // if (parameter != null) {
        // parameter.setValue(TalendTextUtils.addQuotes(functionName));
        // }
    }

    /**
     * DOC qzhang Comment method "changeTableNameParameter".
     *
     * @param newdbTableName
     * @param olddbTableName
     * @param uniqueName
     * @param dbTableElementField
     */
    private void changeTableNameParameter(String newdbTableName, String olddbTableName, String uniqueName,
            IElementParameter dbTableElementField) {
        if (dbTableElementField != null) {
            if (isNotSim) {
                newdbTableName = newdbTableName == null ? "" : newdbTableName; //$NON-NLS-1$
                dbTableElementField.setValue(TalendTextUtils.addQuotes(newdbTableName));
                return;
            }
            Object value = dbTableElementField.getValue();
            String removeQuotes = TalendTextUtils.removeQuotes((String) value);
            boolean b = value == null || removeQuotes.equals(""); //$NON-NLS-1$
            // add the code for table name equals uniqueName
            b = b || uniqueName.equals(olddbTableName);
            b = b || value != null && removeQuotes.toString().equals(olddbTableName);

            if (b) {
                newdbTableName = newdbTableName == null ? "" : newdbTableName; //$NON-NLS-1$
                dbTableElementField.setValue(TalendTextUtils.addQuotes(newdbTableName));
            }
        }
    }

    private void refreshMetadataChanged() {
        if (inputNode != null && inputNode.getExternalNode() != null) {
            inputNode.getExternalNode().metadataOutputChanged(currentInputMetadata);
        }
        if (node != null && node.getExternalNode() != null) {
            node.getExternalNode().metadataOutputChanged(currentOutputMetadata);
        }
    }

    /**
     * Sets the propagate.
     *
     * @param propagate the propagate to set
     */
    public void setPropagate(Boolean propagate) {
        this.propagate = propagate;
    }

    /**
     * Return the flag of propagation
     */
    public Boolean isPropagate() {
        return this.propagate;
    }

    /**
     * Sets the currentConnector.
     *
     * @param currentConnector the currentConnector to set
     */
    public void setCurrentConnector(String currentConnector) {
        this.currentConnector = currentConnector;
    }

}
