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
package org.talend.designer.core.generic.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.gef.commands.Command;
import org.eclipse.gef.commands.CompoundCommand;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.core.model.metadata.IMetadataColumn;
import org.talend.core.model.metadata.IMetadataTable;
import org.talend.core.model.metadata.MetadataTable;
import org.talend.core.model.metadata.MetadataToolHelper;
import org.talend.core.model.process.EConnectionType;
import org.talend.core.model.process.EParameterFieldType;
import org.talend.core.model.process.IConnection;
import org.talend.core.model.process.IElementParameter;
import org.talend.core.model.process.IElementParameterDefaultValue;
import org.talend.core.model.process.INode;
import org.talend.core.model.process.INodeConnector;
import org.talend.core.model.properties.ConnectionItem;
import org.talend.core.model.properties.Item;
import org.talend.core.model.properties.Property;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.model.repository.IRepositoryViewObject;
import org.talend.core.model.utils.TalendTextUtils;
import org.talend.core.ui.metadata.dialog.MetadataDialog;
import org.talend.core.ui.metadata.dialog.MetadataDialogForMerge;
import org.talend.core.ui.properties.tab.IDynamicProperty;
import org.talend.designer.core.generic.i18n.Messages;
import org.talend.designer.core.generic.model.GenericElementParameter;
import org.talend.designer.core.generic.utils.SchemaUtils;
import org.talend.designer.core.model.components.EParameterName;
import org.talend.designer.core.model.components.EmfComponent;
import org.talend.designer.core.ui.editor.cmd.ChangeMetadataCommand;
import org.talend.designer.core.ui.editor.cmd.PropertyChangeCommand;
import org.talend.designer.core.ui.editor.cmd.RepositoryChangeMetadataCommand;
import org.talend.designer.core.ui.editor.connections.Connection;
import org.talend.designer.core.ui.editor.nodes.Node;
import org.talend.designer.core.ui.editor.properties.controllers.AbstractSchemaController;
import org.talend.designer.core.ui.editor.properties.controllers.RetrieveSchemaHelper;
import org.talend.designer.core.ui.editor.properties.controllers.SynchronizeSchemaHelper;
import org.talend.designer.core.utils.ValidationRulesUtil;
import org.talend.designer.runprocess.ItemCacheManager;
import org.talend.repository.model.RepositoryNode;
import org.talend.repository.ui.dialog.RepositoryReviewDialog;

/**
 * created by hcyi on Mar 1, 2016 Detailled comment
 *
 */
public class SchemaReferenceController extends AbstractSchemaController {

    public SchemaReferenceController(IDynamicProperty dp) {
        super(dp);
    }

    @Override
    public Control createControl(Composite subComposite, IElementParameter param, int numInRow, int nbInRow, int top,
            Control lastControl) {
        Control lastControlUsed = lastControl;
        if (elem instanceof Node) {
            lastControlUsed = super.createControl(subComposite, param, numInRow, nbInRow, top, lastControl);
        }
        lastControlUsed = addButton(subComposite, param, lastControlUsed, numInRow, top);
        return lastControlUsed;
    }

    @Override
    protected Command createButtonCommand(Button button) {
        if (checkForRepositoryShema(button)) {
            return null;
        }
        Button inputButton = button;
        IElementParameter switchParam = elem.getElementParameter(EParameterName.REPOSITORY_ALLOW_AUTO_SWITCH.getName());

        if (inputButton.getData(NAME).equals(SCHEMA)) {
            // this map wil hold the all input connection for the tUnite component
            Map<INode, Map<IMetadataTable, Boolean>> inputInfos = new HashMap<INode, Map<IMetadataTable, Boolean>>();

            INode node;
            if (elem instanceof Node) {
                node = (INode) elem;
            } else { // else instanceof Connection
                node = ((IConnection) elem).getSource();
            }

            IMetadataTable inputMetadata = null, inputMetaCopy = null;
            Connection inputConec = null;
            String propertyName = (String) inputButton.getData(PARAMETER_NAME);
            IElementParameter param = node.getElementParameter(propertyName);

            IElementParameter connectionParam = param.getChildParameters().get(EParameterName.CONNECTION.getName());
            String connectionName = null;
            if (connectionParam != null) {
                connectionName = (String) connectionParam.getValue();
            }
            Object obj = button.getData(FORCE_READ_ONLY);
            boolean forceReadOnly = false;
            if (obj != null) {
                forceReadOnly = (Boolean) obj;
            }
            boolean inputReadOnly = false, outputReadOnly = false, inputReadOnlyNode = false, inputReadOnlyParam = false;

            for (Connection connec : (List<Connection>) node.getIncomingConnections()) {
                if (connec.isActivate()
                        && (connec.getLineStyle().equals(EConnectionType.FLOW_MAIN)
                                || connec.getLineStyle().equals(EConnectionType.TABLE)
                                || connec.getLineStyle().equals(EConnectionType.FLOW_MERGE) || connec.getLineStyle() == EConnectionType.FLOW_REF)) {
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
                            if (curParam.getFieldType() == EParameterFieldType.SCHEMA_REFERENCE) {
                                if (curParam.isReadOnly()) {
                                    inputReadOnlyParam = true;
                                }
                            }
                        }
                    }
                    // check if the inputMetadata is readonly
                    if (inputMetadata != null) {
                        for (IMetadataColumn column : inputMetadata.getListColumns(true)) {
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
                MessageDialog.openError(button.getShell(), Messages.getString("SchemaReferenceController.inputNotSet"), //$NON-NLS-1$
                        Messages.getString("SchemaReferenceController.connectionNotAvaliable")); //$NON-NLS-1$
                return null;
            }

            IMetadataTable originaleMetadataTable = getMetadataTableFromXml(node);
            // check if the outputMetadata is readonly
            IMetadataTable originaleOutputTable = node.getMetadataFromConnector(param.getContext());
            // when several schema_type button ,need get the right one which is opening
            IElementParameter schemaParam = param.getChildParameters().get("SCHEMA_TYPE");//$NON-NLS-1$
            // need setRepository here
            if (!param.getContext().equals(schemaParam.getContext())) {
                schemaParam = param.getChildParameters().get("SCHEMA_TYPE");//$NON-NLS-1$
            }
            if (schemaParam != null && EmfComponent.REPOSITORY.equals(schemaParam.getValue())) {
                if (originaleOutputTable != null && originaleOutputTable instanceof MetadataTable) {
                    ((MetadataTable) originaleOutputTable).setRepository(true);
                }
            } else if (schemaParam != null && EmfComponent.BUILTIN.equals(schemaParam.getValue())) {
                if (originaleOutputTable != null && originaleOutputTable instanceof MetadataTable) {
                    ((MetadataTable) originaleOutputTable).setRepository(false);
                }
            }
            IMetadataTable outputMetaCopy = originaleOutputTable.clone(true);
            for (IMetadataColumn column : originaleOutputTable.getListColumns(true)) {
                IMetadataColumn columnCopied = outputMetaCopy.getColumn(column.getLabel());
                columnCopied.setCustom(column.isCustom());
                columnCopied.setReadOnly(column.isReadOnly());
            }
            outputMetaCopy.setReadOnly(originaleOutputTable.isReadOnly()
                    || param.isReadOnly(node.getElementParametersWithChildrens()));
            IElementParameter schemaTypeParam = param.getChildParameters().get("SCHEMA_TYPE"); //$NON-NLS-1$
            List<IElementParameterDefaultValue> defaultValues = schemaTypeParam.getDefaultValues();
            for (IElementParameterDefaultValue elementParameterDefaultValue : defaultValues) {
                if (elementParameterDefaultValue.getDefaultValue() instanceof MetadataTable) {
                    MetadataTable table = (MetadataTable) elementParameterDefaultValue.getDefaultValue();
                    outputMetaCopy.setReadOnlyColumnPosition(table.getReadOnlyColumnPosition());
                    break;
                }
            }

            outputMetaCopy.sortCustomColumns();
            if (!forceReadOnly) {
                outputReadOnly = prepareReadOnlyTable(outputMetaCopy, param.isReadOnly(), node.isReadOnly());
            } else {
                outputReadOnly = true;
            }
            MetadataDialog metaDialog = null;
            if (inputMetadata != null) {
                if (inputInfos != null && inputInfos.size() > 1 && connectionName == null) {
                    MetadataDialogForMerge metaDialogForMerge = new MetadataDialogForMerge(composite.getShell(), inputInfos,
                            outputMetaCopy, node, getCommandStack());
                    metaDialogForMerge.setText(Messages.getString("SchemaReferenceController.schemaOf") + node.getLabel()); //$NON-NLS-1$
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
                            if (switchParam != null) {
                                switchParam.setValue(Boolean.FALSE);
                            }
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
                                        changeMetadataCommand = changeMetadataCommand.chain(new ChangeMetadataCommand(node,
                                                param, inputNode, inputNode.getMetadataList().get(0), inputMetaCopy,
                                                originaleOutputTable, outputMetaCopy));
                                    }
                                    count++;
                                }
                            }
                            return changeMetadataCommand;
                        }
                    }

                } else {
                    INode inputNode = (inputConec.getSource());
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

            if (metaDialog != null) {
                metaDialog.setText(Messages.getString("SchemaReferenceController.schema.title", node.getLabel())); //$NON-NLS-1$
                metaDialog.setInputReadOnly(inputReadOnly);
                metaDialog.setOutputReadOnly(outputReadOnly);

                if (metaDialog.open() == MetadataDialog.OK) {
                    inputMetaCopy = metaDialog.getInputMetaData();
                    outputMetaCopy = metaDialog.getOutputMetaData();
                    boolean modified = false;
                    if (!outputMetaCopy.sameMetadataAs(originaleOutputTable, IMetadataColumn.OPTIONS_NONE)) {
                        modified = true;
                    } else {
                        if (inputMetadata != null) {
                            if (!inputMetaCopy.sameMetadataAs(inputMetadata, IMetadataColumn.OPTIONS_NONE)) {
                                modified = true;
                            }
                        }
                    }
                    if (modified) {
                        if (switchParam != null) {
                            switchParam.setValue(Boolean.FALSE);
                        }
                        INode inputNode = null;
                        if (inputConec != null) {
                            inputNode = inputConec.getSource();
                        }
                        // update the component schema
                        if (param instanceof GenericElementParameter) {
                            GenericElementParameter genericElementParameter = (GenericElementParameter) param;
                            String paramName = genericElementParameter.getName();
                            ComponentProperties componentProperties = node.getComponentProperties();
                            if (componentProperties != null) {
                                org.talend.daikon.properties.property.Property schemaProperty = componentProperties
                                        .getValuedProperty(paramName);
                                if (schemaProperty != null) {
                                    SchemaUtils.updateComponentSchema(node, outputMetaCopy, null);
                                }
                            }
                        }
                        if (node.getComponent().isSchemaAutoPropagated()) {
                            ChangeMetadataCommand changeMetadataCommand = new ChangeMetadataCommand(node, param, inputNode,
                                    inputMetadata, inputMetaCopy, originaleOutputTable, outputMetaCopy);
                            return changeMetadataCommand;
                        } else {
                            ChangeMetadataCommand changeMetadataCommand = new ChangeMetadataCommand(node, param, inputNode,
                                    inputMetadata, inputMetaCopy, originaleOutputTable, outputMetaCopy);
                            changeMetadataCommand.setPropagate(Boolean.FALSE);
                            return changeMetadataCommand;
                        }
                    }
                }
            }
        } else if (inputButton.getData(NAME).equals(RETRIEVE_SCHEMA)) {
            Node node = (Node) elem;
            // String propertyName = (String) inputButton.getData(PARAMETER_NAME);
            final Command cmd = RetrieveSchemaHelper.retrieveSchemasCommand(node);
            if (switchParam != null) {
                switchParam.setValue(Boolean.FALSE);
            }
            return cmd;
        } else if (inputButton.getData(NAME).equals(RESET_COLUMNS)) {
            Node node = (Node) elem;

            String propertyName = (String) inputButton.getData(PARAMETER_NAME);
            IElementParameter param = node.getElementParameter(propertyName);

            final Command cmd = SynchronizeSchemaHelper.createCommand(node, param);
            if (switchParam != null) {
                switchParam.setValue(Boolean.FALSE);
            }
            return cmd;
        } else if (button.getData(NAME).equals(REPOSITORY_CHOICE)) {
            String paramName = (String) button.getData(PARAMETER_NAME);
            IElementParameter schemaParam = elem.getElementParameter(paramName);

            ERepositoryObjectType type = ERepositoryObjectType.METADATA_CON_TABLE;
            String filter = schemaParam.getFilter();
            RepositoryReviewDialog dialog = new RepositoryReviewDialog(button.getShell(), type, filter);
            if (dialog.open() == RepositoryReviewDialog.OK) {
                RepositoryNode node = dialog.getResult();
                while (node.getObject().getProperty().getItem() == null
                        || (!(node.getObject().getProperty().getItem() instanceof ConnectionItem))) {
                    node = node.getParent();
                }

                IRepositoryViewObject object = dialog.getResult().getObject();
                Property property = object.getProperty();
                String id = property.getId();
                String name = object.getLabel();// The name is Table Name.
                if (name != null) {
                    if (elem instanceof Node) {
                        String value = id + " - " + name; //$NON-NLS-1$
                        paramName = paramName + ":" + EParameterName.REPOSITORY_SCHEMA_TYPE.getName();//$NON-NLS-1$
                        Command selectorCommand = new PropertyChangeCommand(elem, paramName, TalendTextUtils.addQuotes(value));
                        executeCommand(selectorCommand);
                    }
                }
                String value = id + " - " + name; //$NON-NLS-1$
                String fullParamName = paramName + ":" + getRepositoryChoiceParamName(); //$NON-NLS-1$

                org.talend.core.model.metadata.builder.connection.Connection connection = null;
                if (elem instanceof Node) {
                    IMetadataTable repositoryMetadata = MetadataToolHelper.getMetadataFromRepository(value);
                    connection = MetadataToolHelper.getConnectionFromRepository(value);
                    // For validation rule.
                    boolean isValRulesLost = false;
                    IRepositoryViewObject currentValRuleObj = ValidationRulesUtil.getCurrentValidationRuleObjs(elem);
                    if (currentValRuleObj != null) {
                        List<IRepositoryViewObject> valRuleObjs = ValidationRulesUtil.getRelatedValidationRuleObjs(value);
                        if (!ValidationRulesUtil.isCurrentValRuleObjInList(valRuleObjs, currentValRuleObj)) {
                            if (!MessageDialog.openConfirm(button.getShell(),
                                    Messages.getString("SchemaReferenceController.validationrule.title.confirm"), //$NON-NLS-1$
                                    Messages.getString("SchemaReferenceController.validationrule.selectMetadataMsg"))) { //$NON-NLS-1$
                                return null;
                            } else {
                                isValRulesLost = true;
                            }
                        }
                    }

                    if (repositoryMetadata == null) {
                        repositoryMetadata = new MetadataTable();
                    }
                    if (switchParam != null) {
                        switchParam.setValue(Boolean.FALSE);
                    }

                    CompoundCommand cc = new CompoundCommand();
                    RepositoryChangeMetadataCommand changeMetadataCommand = new RepositoryChangeMetadataCommand((Node) elem,
                            fullParamName, value, repositoryMetadata, null, null);
                    changeMetadataCommand.setConnection(connection);
                    cc.add(changeMetadataCommand);

                    if (isValRulesLost) {
                        ValidationRulesUtil.appendRemoveValidationRuleCommands(cc, elem);
                    }
                    return cc;
                }
            }
        } else if (button.getData(NAME).equals(COPY_CHILD_COLUMNS)) {
            String paramName = (String) button.getData(PARAMETER_NAME);
            IElementParameter param = elem.getElementParameter(paramName);
            IElementParameter processParam = elem.getElementParameterFromField(EParameterFieldType.PROCESS_TYPE);
            IElementParameter processIdParam = processParam.getChildParameters().get(
                    EParameterName.PROCESS_TYPE_PROCESS.getName());
            String id = (String) processIdParam.getValue();
            Item item = ItemCacheManager.getProcessItem(id);
            Node node = (Node) elem;
            copySchemaFromChildJob(node, item);
            // pop up the schema dialog
            MetadataDialog metaDialog = new MetadataDialog(composite.getShell(), node.getMetadataList().get(0), node,
                    getCommandStack());
            metaDialog.setText(Messages.getString("SchemaReferenceController.schemaOf") + node.getLabel()); //$NON-NLS-1$
            if (metaDialog.open() == MetadataDialog.OK) {
                IMetadataTable outputMetaData = metaDialog.getOutputMetaData();
                return new ChangeMetadataCommand(node, param, null, outputMetaData);
            }
        }
        return null;
    }

    @Override
    public IMetadataTable getMetadataTableFromXml(INode node) {
        IElementParameter param = node.getElementParameterFromField(EParameterFieldType.SCHEMA_REFERENCE);
        if (param.getValue() instanceof IMetadataTable) {
            IMetadataTable table = (IMetadataTable) param.getValue();
            return table;
        }
        return null;
    }
}
