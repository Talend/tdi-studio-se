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
package org.talend.designer.core.ui.action;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.draw2d.geometry.Point;
import org.eclipse.gef.requests.CreationFactory;
import org.eclipse.gef.ui.actions.SelectionAction;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Event;
import org.eclipse.ui.IWorkbenchPart;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.PluginChecker;
import org.talend.core.model.components.ComponentCategory;
import org.talend.core.model.metadata.IMetadataTable;
import org.talend.core.model.process.EConnectionType;
import org.talend.core.model.process.EParameterFieldType;
import org.talend.core.model.process.IConnection;
import org.talend.core.model.process.IConnectionCategory;
import org.talend.core.model.process.IElementParameter;
import org.talend.core.model.process.INodeConnector;
import org.talend.core.model.utils.TalendTextUtils;
import org.talend.core.ui.IJobletProviderService;
import org.talend.core.utils.KeywordsValidator;
import org.talend.designer.core.i18n.Messages;
import org.talend.designer.core.model.components.EParameterName;
import org.talend.designer.core.ui.dialog.mergeorder.ConnectionTableAndSchemaNameDialog;
import org.talend.designer.core.ui.editor.connections.Connection;
import org.talend.designer.core.ui.editor.nodes.Node;
import org.talend.designer.core.ui.editor.nodes.NodePart;
import org.talend.designer.core.ui.editor.process.Process;
import org.talend.designer.core.utils.ConnectionUtil;

/**
 * Action that manage to create a connection from the context menu. A connection type is used to know which kind of
 * connection will be created. <br/>
 *
 * $Id$
 *
 */
public class ConnectionCreateAction extends SelectionAction {

    public static final String ID = "org.talend.designer.core.ui.editor.action.ConnectionCreateAction"; //$NON-NLS-1$

    private final EConnectionType connecType;

    private NodePart nodePart;

    private List<String> menuList;

    private List<Object> listArgs;

    private INodeConnector curNodeConnector;

    private static final String NEW_OUTPUT = "*New Output*"; //$NON-NLS-1$

    private final String ISINPUT = "isinput";

    private final String TRUE = "true";

    /**
     * Define the type of the connection and the workbench part who will manage the connection.
     *
     * @param part
     * @param connecType
     */
    public ConnectionCreateAction(IWorkbenchPart part, EConnectionType connecType) {
        super(part);
        this.connecType = connecType;
        // setId(ID+connecType.getName());
    }

    public ConnectionCreateAction(IWorkbenchPart part, INodeConnector nodeConnector) {
        super(part);
        this.connecType = nodeConnector.getDefaultConnectionType();
        this.curNodeConnector = nodeConnector;
        // setId(ID+connecType.getName());
    }

    @Override
    protected boolean calculateEnabled() {
        return canPerformAction();
    }

    /**
     * Test if the selected item is a node.
     *
     * @return true/false
     */
    @SuppressWarnings("unchecked")
    private boolean canPerformAction() {
        if (getSelectedObjects().isEmpty()) {
            return false;
        }
        List parts = getSelectedObjects();
        if (parts.size() == 1) {
            Object o = parts.get(0);
            if (!(o instanceof NodePart)) {
                return false;
            }
            nodePart = (NodePart) o;
            if (!(nodePart.getModel() instanceof Node)) {
                return false;
            }
            Node node = (Node) nodePart.getModel();
            if (!node.isActivate()) {
                return false;
            }

            if (node.getJobletNode() != null) {
                return false;
            }

            if (curNodeConnector != null && "VALIDATION_REJECT".equals(curNodeConnector.getName())) {
                if (node.getProcess() != null
                        && !ComponentCategory.CATEGORY_4_DI.getName().equals(node.getProcess().getComponentsType())) {
                    return false;
                }
            }

            if (connecType.hasConnectionCategory(IConnectionCategory.EXECUTION_ORDER)) {
                if (!(Boolean) node.getPropertyValue(EParameterName.STARTABLE.getName())
                        || (!node.getProcessStartNode(false).equals(node))) {
                    if (!node.isELTMapComponent()) {
                        boolean jobletOk = false;
                        if (PluginChecker.isJobLetPluginLoaded()) {
                            IJobletProviderService service = (IJobletProviderService) GlobalServiceRegister.getDefault()
                                    .getService(IJobletProviderService.class);
                            if (service != null && service.isJobletComponent(node)) {
                                jobletOk = true;
                            }
                        }
                        if (!jobletOk) {
                            return false;
                        }
                    }
                }
            }
            menuList = new ArrayList<String>();
            if (curNodeConnector == null) {
                curNodeConnector = node.getConnectorFromType(connecType);
                if (curNodeConnector == null || !curNodeConnector.isShow()) {
                    return false;
                }
            }

            if (curNodeConnector.getMaxLinkOutput() != -1) {
                if (curNodeConnector.getCurLinkNbOutput() >= curNodeConnector.getMaxLinkOutput()) {
                    return false;
                }
            }
            if (curNodeConnector.getMaxLinkOutput() == 0) {
                return false;
            }

            /**
             * judge whether to show the table link menu in ELTMap components
             */
            if (curNodeConnector.getName().equals(EConnectionType.TABLE.getName())) {
                List<? extends IConnection> tableRefs = node.getOutgoingConnections(EConnectionType.TABLE_REF);
                if (tableRefs != null && 0 < tableRefs.size()) {
                    return false;
                }
            }

            if (!curNodeConnector.isMultiSchema()) {
                // setText(curNodeConnector.getMenuName());
            }

            if (curNodeConnector.isMultiSchema()) {
                for (int i = 0; i < node.getMetadataList().size(); i++) {
                    IMetadataTable table = (node.getMetadataList().get(i));
                    String value = table.getAdditionalProperties().get(ISINPUT);
                    if (value != null && value.equals(TRUE)) {
                        continue;
                    }
                    String name = table.getTableName();
                    if (name.equals(node.getUniqueName())) {
                        continue;
                    }
                    if (table.getAttachedConnector() == null || table.getAttachedConnector().equals(curNodeConnector.getName())) {
                        if (connecType == EConnectionType.TABLE) {
                            name = table.getLabel() + " (" + name + ")"; //$NON-NLS-1$ //$NON-NLS-2$
                        }
                        boolean nameUsed = false;
                        for (Connection connec : (List<Connection>) node.getOutgoingConnections()) {
                            if (connec.getLineStyle().hasConnectionCategory(IConnectionCategory.FLOW)) {
                                if (connec.getMetadataTable() != null
                                        && connec.getMetadataTable().getTableName().equals(table.getTableName())) {
                                    nameUsed = true;
                                }
                            }
                        }
                        // if the name is not already in the process adds to the list
                        if (!nameUsed) {
                            menuList.add(name);
                        }
                    }
                }
                if ((curNodeConnector.getMaxLinkOutput() == -1 || node.getMetadataList().size() < curNodeConnector
                        .getMaxLinkOutput()) && curNodeConnector.isBuiltIn()) {
                    menuList.add(getNewOutputMenuName());
                }
            } else {
                String menuName;
                boolean addDefaultName = false;

                // get linked metadata to connector
                IMetadataTable table = null;
                for (int i = 0; i < node.getMetadataList().size(); i++) {
                    table = (node.getMetadataList().get(i));
                    if (table.getTableName().equals(node.getUniqueName())) {
                        break;
                    }
                }

                // if EBCDIC + single schema mode, only have one output maximum
                if (node.getComponent().getName().contains("EBCDIC") && node.checkSchemaForEBCDIC(table)
                        && curNodeConnector.getCurLinkNbOutput() > 0) {
                    return false;
                }
                if (connecType == EConnectionType.TABLE) {
                    addDefaultName = addDefaultName();
                    menuName = getNewOutputMenuName();
                } else {
                    menuName = curNodeConnector.getMenuName();
                }
                if (!addDefaultName) {
                    setText(menuName);
                    menuList.add(menuName);
                }
            }

            // if (!node.getConnectorFromType(connecType).isBuiltIn()) {
            // setText(EDesignerConnection.getConnection(connecType).getMenuName());
            // }
            // menuList = new ArrayList<String>();
            // if (node.getConnectorFromType(connecType).isBuiltIn()) {
            // for (int i = 0; i < node.getMetadataList().size(); i++) {
            // IMetadataTable table = ((IMetadataTable) node.getMetadataList().get(i));
            // String name = table.getTableName();
            // if (connecType.equals(EConnectionType.TABLE)) {
            // name = table.getLabel() + " (" + name + ")";
            // }
            // boolean nameUsed = false;
            // for (Connection connec : (List<Connection>) node.getOutgoingConnections()) {
            // if (connec.getMetadataTable().getTableName().equals(table.getTableName())) {
            // nameUsed = true;
            // }
            // }
            // // if the name is not already in the process adds to the list
            // if (!nameUsed) {
            // menuList.add(name);
            // }
            // }
            // menuList.add(getNewOutputMenuName());
            // } else {
            // String menuName;
            // if (connecType.equals(EConnectionType.TABLE)) {
            // menuName = getNewOutputMenuName();
            // } else {
            // menuName = EDesignerConnection.getConnection(connecType).getMenuName();
            // }
            // setText(menuName);
            // menuList.add(menuName);
            // }

            return true;
        }
        return false;
    }

    /**
     * DOC qzhang Comment method "addDefaultName".
     *
     * @param node
     */
    private boolean addDefaultName() {
        String removeQuotes = getDefaultTableName();
        String temp = removeQuotes.replaceAll(" ", "");
        if ("".equals(temp)) {
            removeQuotes = temp;
        }
        if (removeQuotes != null && !"".equals(removeQuotes)) {
            menuList.add(removeQuotes);
            // setText(removeQuotes);
            return true;
        }
        return false;
    }

    /**
     * DOC qzhang Comment method "getDefaultTableName".
     *
     * @param node
     * @param removeQuotes
     * @return
     */
    private String getDefaultTableName() {
        Node node = (Node) nodePart.getModel();
        StringBuffer removeQuotes = new StringBuffer();
        IElementParameter elementParam = node.getElementParameter("ELT_TABLE_NAME"); //$NON-NLS-1$
        IElementParameter schemaParam = node.getElementParameter("ELT_SCHEMA_NAME");//$NON-NLS-1$
        if (node.isELTComponent() && elementParam != null && elementParam.getFieldType().equals(EParameterFieldType.TEXT)) {
            String name2 = elementParam.getValue().toString();
            if (schemaParam != null) {
                String schema = schemaParam.getValue().toString();
                if (schema != null) {
                    schema = TalendTextUtils.removeQuotes(schema);
                    if (!"".equals(schema)) { //$NON-NLS-1$
                        removeQuotes.append(schema);
                        removeQuotes.append(".");//$NON-NLS-1$
                    }
                }
            }
            if (name2 != null) {
                name2 = TalendTextUtils.removeQuotes(name2);
                List<IMetadataTable> metaList = node.getMetadataList();
                if (metaList != null) {

                    for (IMetadataTable metadataTable : metaList) {
                        if (metadataTable != null) {
                            String tName = metadataTable.getTableName();
                            tName = TalendTextUtils.removeQuotes(tName);
                            if (tName.equals(name2)) {
                                String tableLable = metadataTable.getLabel();
                                if (tableLable != null) {
                                    tableLable = TalendTextUtils.removeQuotes(tableLable);
                                    if (!"".equals(tableLable)) {
                                        name2 = tableLable;
                                    }
                                }
                            }
                        }
                    }
                }
                String temp = name2.replaceAll(" ", "");
                if ("".equals(temp)) {
                    name2 = temp;
                }
                if (!"".equals(name2)) { //$NON-NLS-1$
                    removeQuotes.append(name2);
                    removeQuotes.append(" (");
                    removeQuotes.append(curNodeConnector.getMenuName());
                    removeQuotes.append(")");
                    //                    removeQuotes = name2 + " (" + curNodeConnector.getMenuName() + ")"; //$NON-NLS-1$ // //$NON-NLS-2$
                }

            }
            //            if (removeQuotes != null && node.isELTComponent() && node.getComponent().getName().equals("tELTOracleInput")) { //$NON-NLS-1$
            // if (getDefaultSchemaName() != null) {
            // String temp = removeQuotes.toString();
            // removeQuotes.append(getDefaultSchemaName());
            // removeQuotes.append(".");
            // removeQuotes.append(temp);
            //                    //                    removeQuotes = getDefaultSchemaName() + "." + removeQuotes; //$NON-NLS-1$
            // }
            // }
        }
        return removeQuotes.toString();
    }

    /**
     * DOC gcui Comment method "getDefaultSchemaName".
     *
     * @param node
     * @param removeQuotes
     * @return
     */
    private String getDefaultSchemaName() {
        Node node = (Node) nodePart.getModel();
        String schemaNameRemoveQuotes = null;
        IElementParameter elementParam = node.getElementParameter("ELT_SCHEMA_NAME"); //$NON-NLS-1$
        if (node.isELTComponent() && elementParam != null && elementParam.getFieldType().equals(EParameterFieldType.TEXT)) {
            String name2 = elementParam.getValue().toString();
            if (name2 != null) {
                name2 = TalendTextUtils.removeQuotes(name2);
                if (!"".equals(name2)) { //$NON-NLS-1$
                    schemaNameRemoveQuotes = name2;
                }
            }
        }
        return schemaNameRemoveQuotes;
    }

    public List<INodeConnector> getConnectors() {
        List<INodeConnector> list = new ArrayList<INodeConnector>();
        if (getSelectedObjects().isEmpty()) {
            return list;
        }
        List parts = getSelectedObjects();
        if (parts.size() == 1) {
            Object o = parts.get(0);
            if (!(o instanceof NodePart)) {
                return list;
            }
            nodePart = (NodePart) o;
            if (!(nodePart.getModel() instanceof Node)) {
                return list;
            }
            Node node = (Node) nodePart.getModel();
            if (!node.isActivate()) {
                return list;
            }
            List<INodeConnector> nodeConnectorList = new ArrayList<INodeConnector>(node.getConnectorsFromType(connecType));
            List<INodeConnector> toRemove = new ArrayList<INodeConnector>();
            for (INodeConnector connector : nodeConnectorList) {
                if (!connector.isShow()
                        || ((connector.getMaxLinkOutput() != -1) && (connector.getCurLinkNbOutput() >= connector
                                .getMaxLinkOutput()))) {
                    toRemove.add(connector);
                } else {
                    if (PluginChecker.isJobLetPluginLoaded()) {
                        IJobletProviderService service = (IJobletProviderService) GlobalServiceRegister.getDefault().getService(
                                IJobletProviderService.class);
                        if (service != null) {
                            if (service.isJobletComponent(node) && !service.isBuiltTriggerConnector(node, connector)) {
                                toRemove.add(connector);
                            }
                            // for bug 10973
                            List<? extends IConnection> outgoingConnections = node.getOutgoingConnections();
                            if (service.isTriggerInputNode(node) && outgoingConnections != null
                                    && outgoingConnections.size() >= 1) {
                                toRemove.add(connector);
                            }
                        }
                    }
                }
            }
            nodeConnectorList.removeAll(toRemove);
            return nodeConnectorList;
        }
        return list;
    }

    private String getNewOutputMenuName() {
        return NEW_OUTPUT + " (" + curNodeConnector.getMenuName() + ")"; //$NON-NLS-1$ //$NON-NLS-2$
    }

    public List<String> getMenuList() {
        // // gcui:remove *New Output* if have defaultTable name.
        // if (menuList.size() > 1 && getDefaultTableName() != null) {
        // menuList.remove(getNewOutputMenuName());
        // }
        return menuList;
    }

    public void setMenuList(final List<String> menuList) {
        this.menuList = menuList;
    }

    private String askForConnectionName(String nodeLabel, String oldName) {
        final Node node = (Node) nodePart.getModel();
        InputDialog id = new InputDialog(getWorkbenchPart().getSite().getShell(), nodeLabel
                + Messages.getString("ConnectionCreateAction.dialogTitle"), //$NON-NLS-1$
                Messages.getString("ConnectionCreateAction.dialogMessage"), oldName, new IInputValidator() { //$NON-NLS-1$

                    @Override
                    public String isValid(String newText) {
                        if (newText != null) {
                            if (!node.getProcess().checkValidConnectionName(newText, isListenerAttached())
                                    || KeywordsValidator.isKeyword(newText) || KeywordsValidator.isSqlKeyword(newText)) {
                                return "Input is invalid."; //$NON-NLS-1$
                            }
                            return null;
                        } else {
                            return null;
                        }

                    }
                });
        id.open();
        if (id.getReturnCode() == InputDialog.CANCEL) {
            return ""; //$NON-NLS-1$
        }
        IElementParameter elementParam = node.getElementParameter("ELT_TABLE_NAME"); //$NON-NLS-1$
        if (elementParam != null) {
            String paraValue = id.getValue();
            elementParam.setValue(paraValue);
        }
        return TalendTextUtils.removeQuotes(id.getValue());
    }

    /**
     * DOC gcui Comment method "askForConnectionNameAndSchema".
     *
     * @param nodeLabel
     * @param oldName
     * @return
     */
    private String askForConnectionNameAndSchema(String nodeLabel, String oldName) {
        final Node node = (Node) nodePart.getModel();
        String outName = ""; //$NON-NLS-1$
        ConnectionTableAndSchemaNameDialog id = new ConnectionTableAndSchemaNameDialog(getWorkbenchPart().getSite().getShell(),
                nodeLabel + Messages.getString("ConnectionCreateAction.dialogTitle"), //$NON-NLS-1$
                Messages.getString("ConnectionCreateAction.dialogMessage"), oldName); //$NON-NLS-1$
        id.open();
        if (id.getReturnCode() == InputDialog.CANCEL) {
            return ""; //$NON-NLS-1$
        }
        if (id.getSchemaName().length() != 0 && id.getTableName().length() != 0) {
            IElementParameter elementParam = node.getElementParameter("ELT_TABLE_NAME"); //$NON-NLS-1$
            IElementParameter schemaParam = node.getElementParameter("ELT_SCHEMA_NAME");//$NON-NLS-1$
            if (schemaParam != null) {
                String schemaValue = id.getSchemaName();
                schemaParam.setValue(schemaValue);
            }
            if (elementParam != null) {
                String tableValue = id.getTableName();
                elementParam.setValue(tableValue);
            }

            outName = TalendTextUtils.removeQuotes(id.getSchemaName()) + "." + TalendTextUtils.removeQuotes(id.getTableName()); //$NON-NLS-1$
        } else if (id.getSchemaName().length() == 0 && id.getTableName().length() != 0) {
            IElementParameter elementParam = node.getElementParameter("ELT_TABLE_NAME"); //$NON-NLS-1$
            if (elementParam != null) {
                String tableValue = id.getTableName();
                elementParam.setValue(tableValue);
            }
            outName = TalendTextUtils.removeQuotes(id.getTableName());
        }

        return outName;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.jface.action.Action#run()
     */
    @Override
    public void run() {
        IMetadataTable meta = null;
        IMetadataTable newMetadata = null;
        String connectionName = null;

        if (getSelectedObjects().isEmpty()) {
            return;
        }
        List parts = getSelectedObjects();
        if (parts.size() == 1) {
            Object o = parts.get(0);
            if (!(o instanceof NodePart)) {
                return;
            }
            nodePart = (NodePart) o;
        } else {
            return;
        }

        Node node = (Node) nodePart.getModel();
        if (curNodeConnector.isMultiSchema()) {
            if (getText().equals(getNewOutputMenuName())) {
                // boolean nameOk = false;
                // while (!nameOk) {
                //
                // if (node.isELTComponent()) {
                // connectionName = "Default";
                // } else {
                // connectionName = askForConnectionName(node.getLabel(), connectionName);
                // }
                // if (connectionName.equals("")) { //$NON-NLS-1$
                // return;
                // }
                // if (connecType.equals(EConnectionType.TABLE) ||
                // node.getProcess().checkValidConnectionName(connectionName)) {
                // nameOk = true;
                // } else {
                // String message = Messages.getString("ConnectionCreateAction.errorCreateConnectionName",
                // connectionName); //$NON-NLS-1$
                // MessageDialog.openError(getWorkbenchPart().getSite().getShell(), Messages
                // .getString("ConnectionCreateAction.error"), message); //$NON-NLS-1$
                // }
                // }
                //
                // if (connecType.equals(EConnectionType.TABLE)) {
                // meta = new MetadataTable();
                // meta.setTableName(connectionName);
                // meta.setLabel(connectionName);
                // // meta.setTableId(node.getMetadataList().size());
                // newMetadata = meta;
                // } else {
                // boolean metaExist = false;
                // for (int i = 0; i < node.getMetadataList().size(); i++) {
                // if ((node.getMetadataList().get(i)).getTableName().equals(connectionName)) {
                // metaExist = true;
                // }
                // }
                // if (!metaExist) {
                // meta = new MetadataTable();
                // meta.setTableName(connectionName);
                // newMetadata = meta;
                // }
                // }
            } else {
                String tableName;
                // int tableId = -1;
                if (connecType == EConnectionType.TABLE) {
                    int end = getText().length() - 1;
                    int start = getText().lastIndexOf("(") + 1; //$NON-NLS-1$
                    tableName = getText().substring(start, end);
                    // table = Integer.parseInt(stringId);
                    // tableName = getText().substring(0, start - 2);
                    meta = node.getMetadataTable(tableName);
                    // meta = (IMetadataTable) node.getMetadataList().get(tableId);
                    connectionName = meta.getLabel();
                } else {
                    tableName = getText();
                    // tableId = -1;

                    meta = node.getMetadataTable(tableName);
                    // for (int i = 0; i < node.getMetadataList().size(); i++) {
                    // IMetadataTable table = (IMetadataTable) node.getMetadataList().get(i);
                    // if (table.getTableName().equals(tableName)) {
                    // meta = (IMetadataTable) node.getMetadataList().get(i);
                    // }
                    // }
                    connectionName = meta.getTableName();
                }
            }
            // for built-in only:
            if (meta != null) {
                meta.setAttachedConnector(curNodeConnector.getName());
            }
        } else {
            if (connecType == EConnectionType.TABLE) {
                if (getText().equals(getDefaultTableName())) {
                    int end = getText().lastIndexOf("(") - 1;//$NON-NLS-1$
                    int start = 0;
                    if (end >= start) {
                        connectionName = getText().substring(start, end);
                        meta = node.getMetadataList().get(0);
                        meta.setAttachedConnector(curNodeConnector.getName());
                    } else if (getText().endsWith(".")) {
                        connectionName = askForConnectionName(node.getLabel(), null);
                        if (connectionName != null && !"".equals(connectionName)) {
                            connectionName = getText() + connectionName;
                        }
                        meta = node.getMetadataList().get(0);
                        meta.setAttachedConnector(curNodeConnector.getName());
                    }
                } else if (getText().equals(getNewOutputMenuName()) && getDefaultTableName() != null) {
                    if (node.getComponent().getName().equals("tELTOracleInput")) { //$NON-NLS-1$
                        connectionName = askForConnectionNameAndSchema(node.getLabel(), null);
                    } else {
                        connectionName = askForConnectionName(node.getLabel(), null);
                    }
                } else {
                    // gcui:see bug 6781.if is tELT*Input then add a schema name.
                    // if (node.isELTComponent() && node.getComponentName().endsWith("Input"))
                    if (node.getComponent().getName().equals("tELTOracleInput")) { //$NON-NLS-1$
                        connectionName = askForConnectionNameAndSchema(node.getLabel(), getDefaultSchemaName());
                    } else {
                        connectionName = askForConnectionName(node.getLabel(), null);
                    }

                }
            } else {
                if (connecType.hasConnectionCategory(IConnectionCategory.FLOW)) {
                    connectionName = node.getProcess().generateUniqueConnectionName(Process.DEFAULT_ROW_CONNECTION_NAME);
                } else if (connecType.hasConnectionCategory(IConnectionCategory.CAMEL)) {
                    connectionName = ConnectionUtil.generateUniqueConnectionName(connecType, node.getProcess(), curNodeConnector);
                } else {
                    connectionName = curNodeConnector.getLinkName();
                }
            }
            if (node.getMetadataList().size() == 0) {
                meta = null;
            } else {
                meta = node.getMetadataFromConnector(curNodeConnector.getName());
            }
        }

        /**
         * Create a mouse down event that thinks it is over the blob and dispatch it. This is a bit of a fudge to mimic
         * what the user ought to do.
         */
        Point point = null;
        point = nodePart.getFigure().getClientArea().getCenter();

        Point p = point;
        nodePart.getFigure().translateToAbsolute(p);

        Canvas canvas = (Canvas) nodePart.getViewer().getControl();
        Event event = new Event();
        event.button = 1;
        event.count = 0;
        event.detail = 0;
        event.end = 0;
        event.height = 0;
        event.keyCode = 0;
        event.start = 0;
        event.stateMask = 0;
        event.time = 9516624; // any old time... doesn't matter
        event.type = 3;
        event.widget = canvas;
        event.width = 0;
        event.x = p.x + 3;
        event.y = p.y + 3;
        /**
         * Set the connection tool to be the current tool
         */

        listArgs = new ArrayList<Object>();
        if (connecType.equals(EConnectionType.FLOW_MAIN) || connecType.equals(EConnectionType.FLOW_REF)
                || connecType.equals(EConnectionType.TABLE)) {
            if (meta == null) {
                listArgs.add(null);
            } else {
                listArgs.add(meta.getTableName());
            }
        } else {
            listArgs.add(node.getUniqueName());
        }

        String baseName = node.getConnectionName();
        String fromConnectionName = null;
        if (node.getProcess().checkValidConnectionName(baseName)) {
            fromConnectionName = node.getProcess().generateUniqueConnectionName(baseName);
        }
        if (fromConnectionName != null && connecType.hasConnectionCategory(IConnectionCategory.FLOW)
                && node.getProcess().checkValidConnectionName(fromConnectionName, false) && !curNodeConnector.isMultiSchema()) {

            listArgs.add(fromConnectionName);

        } else {

            listArgs.add(connectionName);

        }

        listArgs.add(newMetadata);
        TalendConnectionCreationTool myConnectTool = new TalendConnectionCreationTool(new CreationFactory() {

            @Override
            public Object getNewObject() {
                return listArgs;
            }

            @Override
            public Object getObjectType() {
                return curNodeConnector.getName();
            }
        }, true);
        myConnectTool.performConnectionStartWith(nodePart);
        nodePart.getViewer().getEditDomain().setActiveTool(myConnectTool);

        canvas.notifyListeners(3, event);
    }
}
