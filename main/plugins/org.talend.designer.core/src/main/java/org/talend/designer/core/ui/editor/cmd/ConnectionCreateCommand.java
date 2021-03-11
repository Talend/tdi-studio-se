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
package org.talend.designer.core.ui.editor.cmd;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.eclipse.gef.commands.Command;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.PluginChecker;
import org.talend.core.model.components.ComponentCategory;
import org.talend.core.model.metadata.IEbcdicConstant;
import org.talend.core.model.metadata.IMetadataTable;
import org.talend.core.model.metadata.MetadataTable;
import org.talend.core.model.process.EConnectionType;
import org.talend.core.model.process.EParameterFieldType;
import org.talend.core.model.process.IElementParameter;
import org.talend.core.model.process.IExternalNode;
import org.talend.core.model.process.INodeConnector;
import org.talend.core.model.utils.NodeUtil;
import org.talend.core.model.utils.TalendTextUtils;
import org.talend.core.ui.IJobletProviderService;
import org.talend.core.utils.KeywordsValidator;
import org.talend.designer.core.ICamelDesignerCoreService;
import org.talend.designer.core.i18n.Messages;
import org.talend.designer.core.model.components.EParameterName;
import org.talend.designer.core.model.components.ExternalUtilities;
import org.talend.designer.core.model.process.ConnectionManager;
import org.talend.designer.core.ui.dialog.mergeorder.ConnectionTableAndSchemaNameDialog;
import org.talend.designer.core.ui.editor.connections.Connection;
import org.talend.designer.core.ui.editor.nodes.Node;
import org.talend.designer.core.ui.editor.process.Process;
import org.talend.designer.core.ui.views.properties.ComponentSettings;
import org.talend.designer.core.utils.ConnectionUtil;

/**
 * Command that creates a new connection. <br/>
 *
 *
 * $Id$
 */
public class ConnectionCreateCommand extends Command {

    protected Connection connection;

    private String connectorName;

    private EConnectionType newLineStyle;

    protected Node source;

    protected Node target = null;

    private String metaName;

    private String connectionName;

    private IMetadataTable newMetadata;

    private static boolean creatingConnection = false;

    private boolean insertTMap;

    private Map<EParameterName, Object> connectionParameters;

    /**
     * Initialisation of the creation of the connection with a source and style of connection.
     *
     * @param nodeSource source of the connection
     * @param lineStyle line style
     * @param meta
     */
    public ConnectionCreateCommand(Node nodeSource, String connectorName, List<Object> listArgs) {
        setLabel(Messages.getString("ConnectionCreateCommand.Label")); //$NON-NLS-1$
        this.source = nodeSource;
        this.connectorName = connectorName;
        this.metaName = (String) listArgs.get(0);
        this.connectionName = (String) listArgs.get(1);
        newMetadata = (IMetadataTable) listArgs.get(2);
        if (listArgs.size() >= 4) {
        	connectionParameters = (Map<EParameterName, Object>) listArgs.get(3);
        }
    }

    public ConnectionCreateCommand(Node nodeSource, String connectorName, List<Object> listArgs, boolean insertTMap) {
        setLabel(Messages.getString("ConnectionCreateCommand.Label")); //$NON-NLS-1$
        this.source = nodeSource;
        this.connectorName = connectorName;
        this.metaName = (String) listArgs.get(0);
        this.connectionName = (String) listArgs.get(1);
        this.insertTMap = insertTMap;
        newMetadata = (IMetadataTable) listArgs.get(2);
    }

    /**
     * Set the target of the connection, the source has already been set before.
     *
     * @param target
     */
    public void setTarget(Node targetNode) {
        this.target = targetNode;
        this.target.setTakeSchema(null);
    }

    private String askForConnectionName(String nodeLabel, String oldName) {

        // check if the source got the ELT Table parameter, if yes, take the name by default
        IElementParameter elementParam = source.getElementParameter("ELT_TABLE_NAME"); //$NON-NLS-1$
        if (source.isELTComponent() && elementParam != null && elementParam.getFieldType().equals(EParameterFieldType.TEXT)) {
            String name2 = elementParam.getValue().toString();
            if (name2 != null) {
                name2 = TalendTextUtils.removeQuotes(name2);
                if (!name2.equals("")) { //$NON-NLS-1$
                    return name2;
                }
            }
        }

        // check if the target got the ELT Table parameter, if yes, take the name by default
        elementParam = target.getElementParameter("ELT_TABLE_NAME"); //$NON-NLS-1$
        if (target.isELTComponent() && elementParam != null && elementParam.getFieldType().equals(EParameterFieldType.TEXT)) {
            String name2 = elementParam.getValue().toString();
            if (name2 != null) {
                name2 = TalendTextUtils.removeQuotes(name2);
                if (!name2.equals("")) { //$NON-NLS-1$
                    return name2;
                }
            }
        }
        if (source.isELTComponent()) {
            InputDialog id = new InputDialog(null, nodeLabel + Messages.getString("ConnectionCreateAction.dialogTitle"), //$NON-NLS-1$
                    Messages.getString("ConnectionCreateAction.dialogMessage"), oldName, new IInputValidator() { //$NON-NLS-1$

                        @Override
                        public String isValid(String newText) {
                            if (newText != null) {
                                if (!source.getProcess().checkValidConnectionName(newText, creatingConnection)
                                        || KeywordsValidator.isKeyword(newText) || KeywordsValidator.isSqlKeyword(newText)) {

                                    return Messages.getString("ConnectionCreateCommand.inputValid"); //$NON-NLS-1$
                                }
                            }
                            return null;
                        }
                    });
            id.open();
            if (id.getReturnCode() == InputDialog.CANCEL) {
                return ""; //$NON-NLS-1$
            }
            return id.getValue();
        } else {
            InputDialog id = new InputDialog(null, nodeLabel + Messages.getString("ConnectionCreateAction.dialogTitle"), //$NON-NLS-1$
                    Messages.getString("ConnectionCreateAction.dialogMessage"), oldName, new IInputValidator() { //$NON-NLS-1$

                        @Override
                        public String isValid(String newText) {
                            if (newText != null) {
                                if (!source.getProcess().checkValidConnectionName(newText, creatingConnection)
                                        || KeywordsValidator.isKeyword(newText) || newText.equals("ErrorReject")) {
                                    return Messages.getString("ConnectionCreateCommand.inputValid"); //$NON-NLS-1$
                                }
                            }
                            return null;
                        }
                    });
            id.open();
            if (id.getReturnCode() == InputDialog.CANCEL) {
                return ""; //$NON-NLS-1$
            }
            return id.getValue();
        }

    }

    /**
     * DOC gcui Comment method "askForConnectionSchemaAndTableName".
     *
     * @param nodeLabel
     * @param oldName
     * @return
     */
    private String askForConnectionSchemaAndTableName(String nodeLabel, String oldName) {

        // check if the source got the ELT Table parameter, if yes, take the name by default
        IElementParameter elementTableParam = source.getElementParameter("ELT_TABLE_NAME"); //$NON-NLS-1$
        IElementParameter elementSchemaParam = source.getElementParameter("ELT_SCHEMA_NAME"); //$NON-NLS-1$
        String schemaName = null;
        if (source.isELTComponent() && elementTableParam != null
                && elementTableParam.getFieldType().equals(EParameterFieldType.TEXT)) {
            if (elementSchemaParam != null && elementSchemaParam.getFieldType().equals(EParameterFieldType.TEXT)) {
                schemaName = elementSchemaParam.getValue().toString();
                String name2 = elementTableParam.getValue().toString();
                if (schemaName != null && name2 != null) {
                    schemaName = TalendTextUtils.removeQuotes(schemaName);
                    name2 = TalendTextUtils.removeQuotes(name2);
                    if (!name2.equals("") && !schemaName.equals("")) { //$NON-NLS-1$ //$NON-NLS-2$
                        return schemaName + "." + name2; //$NON-NLS-1$
                    }
                }
            }
            String name2 = elementTableParam.getValue().toString();
            if (name2 != null) {
                name2 = TalendTextUtils.removeQuotes(name2);
                if (!name2.equals("")) { //$NON-NLS-1$
                    return name2;
                }
            }
        }

        // check if the target got the ELT Table parameter, if yes, take the name by default
        elementTableParam = target.getElementParameter("ELT_TABLE_NAME"); //$NON-NLS-1$
        if (target.isELTComponent() && elementTableParam != null
                && elementTableParam.getFieldType().equals(EParameterFieldType.TEXT)) {
            String name2 = elementTableParam.getValue().toString();
            if (name2 != null) {
                name2 = TalendTextUtils.removeQuotes(name2);
                if (!name2.equals("")) { //$NON-NLS-1$
                    return name2;
                }
            }
        }
        String outName = ""; //$NON-NLS-1$
        ConnectionTableAndSchemaNameDialog id = new ConnectionTableAndSchemaNameDialog(null, nodeLabel
                + Messages.getString("ConnectionCreateAction.dialogTitle"), //$NON-NLS-1$
                Messages.getString("ConnectionCreateAction.dialogMessage"), schemaName); //$NON-NLS-1$
        id.open();
        if (id.getReturnCode() == InputDialog.CANCEL) {
            return ""; //$NON-NLS-1$
        }
        if (id.getSchemaName().length() != 0 && id.getTableName().length() != 0) {
            outName = id.getSchemaName() + "." + id.getTableName(); //$NON-NLS-1$
        } else if (id.getSchemaName().length() == 0 && id.getTableName().length() != 0) {
            outName = id.getTableName();
        }

        return outName;
    }

    @Override
    public boolean canExecute() {

        if (target != null) {
            if (source.getConnectorFromName(connectorName) != null
                    && !ConnectionManager.canConnectToTarget(source, null, target, source.getConnectorFromName(connectorName)
                            .getDefaultConnectionType(), connectorName, connectionName)) {
                creatingConnection = false;
                return false;
            }
            newLineStyle = ConnectionManager.getNewConnectionType();
            if (newLineStyle == EConnectionType.TABLE_REF) {
                connectorName = newLineStyle.getName();
            }
        }
        creatingConnection = true;
        return true;
    }

    public boolean canExecute(boolean refactorJoblet) {

        if (target != null) {
            if (source.getConnectorFromName(connectorName) != null
                    && !ConnectionManager.canConnectToTarget(source, null, target, source.getConnectorFromName(connectorName)
                            .getDefaultConnectionType(), connectorName, connectionName, refactorJoblet)) {
                creatingConnection = false;
                return false;
            }
            newLineStyle = ConnectionManager.getNewConnectionType();

        }
        creatingConnection = true;
        return true;
    }

    /**
     * @return An IMetadataTable appropriate for use with the component in its current context.
     */
    private IMetadataTable getNewMetadataTable() {
        // All component types use the same MetadataTable implementation.
        return new MetadataTable();
    }

    @Override
    public void execute() {
        canExecute();
        if (connectionName == null) {
            // ask for the name if there is no

            final INodeConnector mainConnector;

            EConnectionType connecType;
            if (source.isELTComponent()) {
                connecType = EConnectionType.TABLE;
            } else if (ComponentCategory.CATEGORY_4_CAMEL.getName().equals(source.getComponent().getType())) {
                connecType = EConnectionType.ROUTE;
                if (GlobalServiceRegister.getDefault().isServiceRegistered(ICamelDesignerCoreService.class)) {
                    ICamelDesignerCoreService camelService = (ICamelDesignerCoreService) GlobalServiceRegister.getDefault()
                            .getService(ICamelDesignerCoreService.class);
                    connecType = camelService.getTargetConnectionType(source);
                } else {
                    connecType = EConnectionType.ROUTE;
                }
            } else {
                connecType = EConnectionType.FLOW_MAIN;
            }
            mainConnector = NodeUtil.getValidConnector(source);

            if (source.getConnectorFromName(connectorName).isMultiSchema()) {
                boolean connectionOk = false;
                while (!connectionOk) {
                    connectionName = askForConnectionName(source.getLabel(), connectionName);
                    if (StringUtils.isEmpty(connectionName)) {
                        creatingConnection = false;
                        connectionName = null;
                        dispose();
                        return;
                    }
                    metaName = connectionName;
                    newMetadata = getNewMetadataTable();
                    newMetadata.setTableName(connectionName);
                    newMetadata.setLabel(connectionName);
                    newMetadata.setAttachedConnector(connectorName);
                    if ((connecType.equals(EConnectionType.TABLE) || source.getProcess().checkValidConnectionName(connectionName))
                            && (ConnectionManager.canConnectToTarget(source, null, target,
                                    source.getConnectorFromName(connectorName).getDefaultConnectionType(), connectorName,
                                    connectionName))) {
                        connectionOk = true;
                    } else {
                        String message = Messages.getString("ConnectionCreateAction.errorCreateConnectionName", //$NON-NLS-1$
                                connectionName);
                        MessageDialog.openError(null, Messages.getString("ConnectionCreateAction.error"), message); //$NON-NLS-1$
                    }
                }

                // add for feature TDI-17358
                IElementParameter elementParameter = source.getElementParameter("SCHEMAS"); //$NON-NLS-1$
                if (elementParameter != null) {
                    Map<String, Object> map = new HashMap<String, Object>();
                    Object[] itemsValue = elementParameter.getListItemsValue();
                    String[] items = elementParameter.getListItemsDisplayCodeName();
                    map.put(IEbcdicConstant.FIELD_CODE, newMetadata.getTableName());
                    map.put(IEbcdicConstant.FIELD_SCHEMA, newMetadata.getTableName());
                    for (int i = 1; i < items.length; i++) {
                        map.put(items[i], ((IElementParameter) itemsValue[i]).getValue());
                    }
                    Object value = elementParameter.getValue();
                    if (value instanceof List) {
                        List list = (List) value;
                        list.add(map);
                    }
                    ComponentSettings.switchToCurComponentSettingsView();
                }

            } else {
                newMetadata = null;

                if (source.isELTComponent()) {
                    if (source.getElementParameter("ELT_SCHEMA_NAME") != null) {
                        connectionName = askForConnectionSchemaAndTableName(source.getLabel(), connectionName);
                    } else {
                        connectionName = askForConnectionName(source.getLabel(), connectionName);
                    }
                } else if (ComponentCategory.CATEGORY_4_CAMEL.getName().equals(source.getComponent().getType())) {
                    connectionName = ConnectionUtil.generateUniqueConnectionName(connecType, source.getProcess(),
                            source.getConnectorFromType(connecType));
                } else {
                    IMetadataTable metaTable = source.getMetadataFromConnector(mainConnector.getName());
                    if (metaTable != null) {
                        metaName = metaTable.getTableName();
                    }
                    String baseName = source.getConnectionName();
                    if (source.getProcess().checkValidConnectionName(baseName)) {
                        connectionName = source.getProcess().generateUniqueConnectionName(baseName);
                    }
                }
            }

        }

        if (insertTMap) {
            metaName = connectionName;
            newMetadata = getNewMetadataTable();
            newMetadata.setTableName(connectionName);
            newMetadata.setLabel(connectionName);
            newMetadata.setAttachedConnector(connectorName);
        }

        boolean monitorConnection = false; // Default not monitor the connection
        if (newLineStyle == null) {
            newLineStyle = source.getConnectorFromName(connectorName).getDefaultConnectionType();
        }
        if (connection == null) {
            if (newMetadata != null) {
                // add for eltmap
                if (source instanceof Node && source.isELTMapComponent()) {
                    for (IMetadataTable metaTable : source.getMetadataList()) {
                        String tableName = metaTable.getTableName();
                        if (tableName != null && source.getProcess().checkValidConnectionName(tableName)) {
                            source.getProcess().addUniqueConnectionName(tableName);
                        }
                    }
                }
				List<IMetadataTable> metaList = source.getMetadataList();
				boolean isFind = false;
				for (IMetadataTable table : metaList) {
					if (table.getTableName().equals(newMetadata.getTableName())) {
						isFind = true;
						break;
					}
				}
				if (!isFind) {
					source.getMetadataList().add(newMetadata);
				}
                this.connection = new Connection(source, target, newLineStyle, connectorName, metaName, connectionName,
                        monitorConnection);
            } else {
                this.connection = new Connection(source, target, newLineStyle, connectorName, metaName, connectionName, metaName,
                        monitorConnection, this.connectionParameters);
            }
        } else { // in case of redo, reuse the same instance
            if (newMetadata != null) {
				List<IMetadataTable> metaList = source.getMetadataList();
				boolean isFind = false;
				for (IMetadataTable table : metaList) {
					if (table.getTableName().equals(newMetadata.getTableName())) {
						isFind = true;
						break;
					}
				}
				if (!isFind) {
					source.getMetadataList().add(newMetadata);
				}
            }
            connection.reconnect(source, target, newLineStyle);
        }
        if (source.isExternalNode()) {
            IExternalNode externalNode = ExternalUtilities.getExternalNodeReadyToOpen((Node)source);
            externalNode.addOutput(connection);
            ExternalNodeChangeCommand cmd = new ExternalNodeChangeCommand((Node) source, externalNode);
            cmd.execute();
        }
        if (target.isExternalNode()) {
            IExternalNode externalNode = ExternalUtilities.getExternalNodeReadyToOpen((Node)target);
            externalNode.addInput(connection);
            ExternalNodeChangeCommand cmd = new ExternalNodeChangeCommand((Node) target, externalNode);
            cmd.execute();
        }
        INodeConnector nodeConnectorSource, nodeConnectorTarget;
        nodeConnectorSource = connection.getSourceNodeConnector();
        nodeConnectorSource.setCurLinkNbOutput(nodeConnectorSource.getCurLinkNbOutput() + 1);
        nodeConnectorTarget = connection.getTargetNodeConnector();
        nodeConnectorTarget.setCurLinkNbInput(nodeConnectorTarget.getCurLinkNbInput() + 1);

        creatingConnection = false;
        ((Process) source.getProcess()).checkStartNodes();
        source.checkAndRefreshNode();
        boolean isJoblet = false;
        if (PluginChecker.isJobLetPluginLoaded()) {
            IJobletProviderService jobletService = (IJobletProviderService) GlobalServiceRegister.getDefault().getService(
                    IJobletProviderService.class);
            if (jobletService != null && jobletService.isJobletComponent(target)) {
                jobletService.upateJobletComonentList(target);
                isJoblet = true;
            }
        }
        if (!isJoblet) {
            target.checkAndRefreshNode();
        }

    }

    @Override
    public void undo() {
        if (connection == null) {
            return;
        }
        connection.disconnect();
        INodeConnector nodeConnectorSource, nodeConnectorTarget;
        nodeConnectorSource = connection.getSourceNodeConnector();
        if (nodeConnectorSource != null) {
            nodeConnectorSource.setCurLinkNbOutput(nodeConnectorSource.getCurLinkNbOutput() - 1);
        }
        nodeConnectorTarget = connection.getTargetNodeConnector();
        if (nodeConnectorTarget != null) {
            nodeConnectorTarget.setCurLinkNbInput(nodeConnectorTarget.getCurLinkNbInput() - 1);
        }
        if (source.isExternalNode()) {
            IExternalNode externalNode = ExternalUtilities.getExternalNodeReadyToOpen((Node)source);
            externalNode.removeOutput(connection);
            ExternalNodeChangeCommand cmd = new ExternalNodeChangeCommand((Node) source, externalNode);
            cmd.execute();
        }
        if (target.isExternalNode()) {
            IExternalNode externalNode = ExternalUtilities.getExternalNodeReadyToOpen((Node)target);
            externalNode.removeInput(connection);
            ExternalNodeChangeCommand cmd = new ExternalNodeChangeCommand((Node) target, externalNode);
            cmd.execute();
        }
        if (newMetadata != null) {
            source.getMetadataList().remove(newMetadata);
        }
        ((Process) source.getProcess()).checkStartNodes();
        source.checkAndRefreshNode();
        target.checkAndRefreshNode();
        // ((Process) source.getProcess()).checkProcess();
    }

    @Override
    public void redo() {
        execute();
    }

    /**
     * Getter for creatingConnection.
     *
     * @return the creatingConnection
     */
    public static boolean isCreatingConnection() {
        return creatingConnection;
    }

    /**
     * Sets the creatingConnection.
     *
     * @param creatingConnection the creatingConnection to set
     */
    public static void setCreatingConnection(boolean creatingConnection) {
        ConnectionCreateCommand.creatingConnection = creatingConnection;
    }

}
