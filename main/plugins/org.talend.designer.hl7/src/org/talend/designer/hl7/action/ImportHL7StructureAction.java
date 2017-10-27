// ============================================================================
//
// Copyright (C) 2006-2017 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.designer.hl7.action;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.emf.common.util.EList;
import org.eclipse.gef.commands.Command;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.actions.SelectionProviderAction;
import org.talend.commons.exception.ExceptionHandler;
import org.talend.core.model.metadata.IHL7Constant;
import org.talend.core.model.metadata.IMetadataTable;
import org.talend.core.model.metadata.builder.ConvertionHelper;
import org.talend.core.model.metadata.builder.connection.ConnectionFactory;
import org.talend.core.model.metadata.builder.connection.HL7Connection;
import org.talend.core.model.metadata.builder.connection.HL7FileNode;
import org.talend.core.model.metadata.builder.connection.MetadataColumn;
import org.talend.core.model.metadata.builder.connection.MetadataTable;
import org.talend.core.model.process.IConnection;
import org.talend.core.model.process.IConnectionCategory;
import org.talend.core.model.utils.NodeUtil;
import org.talend.core.runtime.CoreRuntimePlugin;
import org.talend.core.ui.metadata.command.RepositoryChangeMetadataForHL7Command;
import org.talend.designer.hl7.managers.HL7OutputManager;
import org.talend.designer.hl7.ui.HL7UI;
import org.talend.designer.hl7.ui.data.Element;
import org.talend.designer.hl7.ui.data.HL7TreeNode;
import org.talend.designer.hl7.ui.form.AbstractHL7StepForm;
import org.talend.designer.hl7.ui.header.HL7Parse;
import org.talend.designer.hl7.util.HL7PublicUtil;
import org.talend.repository.model.IProxyRepositoryFactory;

import ca.uhn.hl7v2.model.Message;

/**
 * DOC hwang class global comment. Detailled comment
 */
public class ImportHL7StructureAction extends SelectionProviderAction {

    private TreeViewer xmlViewer;

    private HL7UI hl7ui;

    private AbstractHL7StepForm form;

    private HL7Connection hl7Connection;

    private HL7PublicUtil hl7Util = new HL7PublicUtil();

    private Map<String, Integer> orderMap = new HashMap<String, Integer>();

    /**
     * Create constructor comment.
     *
     * @param provider
     * @param text
     */
    public ImportHL7StructureAction(HL7Connection hl7Connection, TreeViewer xmlViewer, String text, AbstractHL7StepForm form) {
        super(xmlViewer, text);
        this.xmlViewer = xmlViewer;
        this.form = form;
        this.hl7Connection = hl7Connection;
    }

    public ImportHL7StructureAction(TreeViewer xmlViewer, HL7UI hl7ui, String text) {
        super(xmlViewer, text);
        this.xmlViewer = xmlViewer;
        this.hl7ui = hl7ui;
    }

    private List treeNodeAdapt() {
        List<HL7TreeNode> treeData = new ArrayList<HL7TreeNode>();
        FileDialog f = null;
        if (hl7ui != null) {
            f = new FileDialog(hl7ui.getHl7UIParent().getShell());
        } else if (form != null) {
            f = new FileDialog(form.getShell());
        }
        String file = f.open();
        if (file == null) {
            return treeData;
        }
        HL7Parse hl7Parse = new HL7Parse();
        List<String> msgContentList = new ArrayList<String>();

        List<Message> messageList = hl7Parse.doParse(file, "'\\u000b'", "'\\u001c'");
        List<HL7TreeNode> nodeList = hl7Util.getHL7TreeNodes(messageList);

        if (!nodeList.isEmpty()) {
            HL7TreeNode hl7TreeNode = nodeList.get(0);
            List<HL7FileNode> table = new ArrayList<HL7FileNode>();
            if (hl7Connection != null) {
                EList root = hl7Connection.getRoot();
                root.clear();
                if (hl7TreeNode != null) {
                    hl7Util.initNodeOrder(hl7TreeNode, orderMap);
                    hl7Util.tableLoader((Element) hl7TreeNode, "", root, hl7TreeNode.getDefaultValue(), orderMap);
                }
                table.addAll(root);
            } else {
                if (hl7TreeNode != null) {
                    hl7Util.initNodeOrder(hl7TreeNode, orderMap);
                    hl7Util.tableLoader((Element) hl7TreeNode, "", table, hl7TreeNode.getDefaultValue(), orderMap);
                }
            }

            List<String> schemaList = new ArrayList<String>();
            for (HL7FileNode node : table) {
                String columnName = node.getRelatedColumn();
                if (columnName.contains(":")) {
                    columnName = columnName.substring(0, columnName.indexOf(":"));
                }
                if (!schemaList.contains(columnName) && !"".equals(columnName)) {
                    schemaList.add(columnName);
                }
            }
            initXmlTreeData(schemaList, table, treeData);
        }
        return treeData;
    }

    private void initXmlTreeData(List<String> schemaList, List<HL7FileNode> root, List<HL7TreeNode> treeData) {
        Map<String, HL7TreeNode> mapNodes = new HashMap<String, HL7TreeNode>();
        List<? extends IConnection> incomingConnections = new ArrayList<IConnection>();
        if (hl7ui != null) {
            if (hl7ui.gethl7Manager() instanceof HL7OutputManager) {
                ((HL7OutputManager) hl7ui.gethl7Manager()).getContents().clear();
                incomingConnections = NodeUtil.getIncomingConnections(hl7ui.gethl7Manager().getHl7Component(),
                        IConnectionCategory.FLOW);
            }
        }
        IProxyRepositoryFactory factory = CoreRuntimePlugin.getInstance().getProxyRepositoryFactory();
        List<MetadataTable> iMetadataTables = new ArrayList<MetadataTable>();
        HL7TreeNode rootNode = null;
        Map<String, IMetadataTable> schemaNameToInputTable = new HashMap<String, IMetadataTable>();
        Map<String, MetadataTable> schemaNameToOutputTable = new HashMap<String, MetadataTable>();
        for (String schemaName : schemaList) {
            IMetadataTable metadataTable = null;
            for (IConnection connection : incomingConnections) {
                if (connection.getUniqueName().equals(schemaName)) {
                    metadataTable = connection.getMetadataTable();
                    metadataTable.setLabel(connection.getUniqueName());
                    schemaNameToInputTable.put(schemaName, metadataTable);
                    break;
                }
            }

            MetadataTable targetMetadataTable = ConnectionFactory.eINSTANCE.createMetadataTable();
            targetMetadataTable.setId(factory.getNextId());
            schemaNameToOutputTable.put(schemaName, targetMetadataTable);
            targetMetadataTable.setLabel(schemaName);
            iMetadataTables.add(targetMetadataTable);
        }

        HL7TreeNode current = null;
        HL7TreeNode temp = null;
        String currentPath = null;
        String defaultValue = null;
        int nodeOrder = 0;
        boolean haveOrder = true;
        // build root tree
        for (int i = 0; i < root.size(); i++) {
            HL7FileNode node = root.get(i);
            String newPath = node.getFilePath();
            defaultValue = node.getDefaultValue();
            String columnName = node.getRelatedColumn();
            // String type = node.getType();
            String orderValue = String.valueOf(node.getOrder());
            if (orderValue == null || "".equals(orderValue)) {
                haveOrder = false;
            }
            if (haveOrder) {
                nodeOrder = node.getOrder();
            }
            String rowName = columnName;
            if (columnName != null && columnName.contains(":")) {
                String[] names = columnName.split(":");
                rowName = names[0];
                columnName = names[1];
            } else {
                columnName = null;
            }
            temp = this.addElement(current, currentPath, newPath, defaultValue, mapNodes);
            if (temp == null) {
                // should not happen
                continue;
            }
            // temp.setDataType(type);
            if (rootNode == null) {
                rootNode = temp;
            }
            if (node.getAttribute().equals("main")) {
                temp.setMain(true);
            }
            current = temp;
            currentPath = newPath;
            if (haveOrder) {
                temp.setOrder(nodeOrder);
            }
            if (rowName != null && rowName.length() > 0) {
                temp.setRow(rowName);
            }

            if (columnName != null) {
                IMetadataTable metadataTable = schemaNameToInputTable.get(rowName);
                // group node can not get the metadata table
                if (metadataTable == null) {
                    IMetadataTable metadataTableTemp = null;
                    for (IConnection connection : incomingConnections) {
                        metadataTableTemp = connection.getMetadataTable();
                        String connectionName = metadataTableTemp.getLabel();
                        if (connectionName == null) {
                            connectionName = connection.getUniqueName();
                        }
                        if (columnName.startsWith(connectionName)) {
                            break;
                        }
                    }
                    temp.setColumnName(columnName);
                    if (metadataTableTemp != null) {
                        temp.setColumn(metadataTableTemp.getColumn(columnName));
                        temp.setTable(metadataTableTemp);
                    }
                } else {
                    temp.setColumnName(columnName);
                    temp.setColumn(metadataTable.getColumn(columnName));
                    temp.setTable(metadataTable);
                }
                //
                if (!temp.isMain()) {
                    MetadataColumn newColumn = ConnectionFactory.eINSTANCE.createMetadataColumn();
                    newColumn.setLabel(columnName);
                    newColumn.setName(temp.getLabel());
                    newColumn.setLength(226);
                    newColumn.setTalendType("id_String");
                    schemaNameToOutputTable.get(rowName).getColumns().add(newColumn);
                }
            }
        }
        if (rootNode == null) {
            rootNode = new Element("rootTag");
        }
        if (haveOrder) {
            orderNode(rootNode);
        }
        if (rootNode != null) {
            treeData.add(rootNode);
        }
        if (hl7ui != null) {
            if (hl7ui.gethl7Manager() instanceof HL7OutputManager) {
                ((HL7OutputManager) hl7ui.gethl7Manager()).getContents().put(rootNode.getColumnLabel(), treeData);
            }

        } else if (form != null) {
            for (HL7TreeNode hl7Node : treeData) {
                form.getContents().put(rootNode.getColumnLabel(), hl7Node);
            }
        }
        if (hl7ui != null) {
            // execute the commands,initialize the propertiesView .
            List<Command> commands = new ArrayList<Command>();
            for (MetadataTable tableTemp : iMetadataTables) {
                Command hl7Cmd = new RepositoryChangeMetadataForHL7Command(hl7ui.gethl7Manager().getHl7Component(),
                        IHL7Constant.TABLE_SCHEMAS, tableTemp.getLabel(), ConvertionHelper.convert(tableTemp));
                commands.add(hl7Cmd);
            }
            for (Command command : commands) {
                command.execute();
            }
        }
    }

    private void orderNode(HL7TreeNode node) {
        // reset the order.
        if (node != null) {
            List<HL7TreeNode> firstSubChildren = node.getChildren();
            HL7TreeNode foundNode = null;
            for (HL7TreeNode childen : firstSubChildren) {
                if (childen.isRepetable()) {
                    foundNode = childen;
                    sortOrder(foundNode, node);
                    break;
                } else if (childen.isGroup()) {
                    foundNode = childen;
                    sortOrder(foundNode, node);
                    orderNode(childen);
                } else {
                    orderNode(childen);
                }
            }
        }
    }

    private void sortOrder(HL7TreeNode treeNode, HL7TreeNode node) {
        if (node != null) {
            List<HL7TreeNode> children = node.getChildren();
            if (treeNode != null) {
                int tmpOrder = 0;
                int attrNsCount = 0;
                for (HL7TreeNode child : children) {
                    if (child.getOrder() < treeNode.getOrder()) {
                        tmpOrder++;
                    }
                    if (child.isAttribute() || child.isNameSpace()) {
                        attrNsCount++;
                    }
                }
                if (tmpOrder > -1) {
                    int oldOrder = children.indexOf(treeNode);
                    if (oldOrder != -1 && oldOrder != tmpOrder) {
                        node.removeChild(treeNode);
                        if (children.size() > tmpOrder) {
                            node.addChild(tmpOrder - attrNsCount, treeNode);
                        } else {
                            node.addChild(treeNode);
                        }
                    }
                }
            }
        }

    }

    protected HL7TreeNode addElement(HL7TreeNode current, String currentPath, String newPath, String defaultValue,
            Map<String, HL7TreeNode> mapNodes) {
        HL7TreeNode temp = mapNodes.get(newPath);
        if (temp == null) {
            // if node is not existing, create it.
            String name = newPath.substring(newPath.lastIndexOf("/") + 1); //$NON-NLS-1$
            temp = new Element(name, defaultValue);
            if (current == null) {// root node
                mapNodes.put(newPath, temp);
                return temp;
            }
            mapNodes.put(newPath, temp);
            String parentPath = newPath.substring(0, newPath.lastIndexOf("/")); //$NON-NLS-1$
            HL7TreeNode parentNode = mapNodes.get(parentPath);
            if (parentNode != null) {
                parentNode.addChild(temp);
            } else {
                ExceptionHandler.log("Error when parsing the HL7 data, parent not existing for:" + parentPath);
            }
        }
        return temp;
    }

    /**
     *
     * wzhang Comment method "getSelectedSchema".
     *
     * @return
     */
    private String getSelectedSchema() {
        TreeItem[] selection = xmlViewer.getTree().getSelection();
        if (selection.length > 0) {
            Object data = selection[0].getData();
            if (data instanceof HL7TreeNode) {
                return ((HL7TreeNode) data).getRow();
            }
        }
        return "";// hl7ui.gethl7Manager().getCurrentSchema();
    }

    public void init() {
        this.setEnabled(true);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.action.Action#run()
     */
    @Override
    public void run() {
        List<HL7TreeNode> newInput = treeNodeAdapt();
        if (newInput.size() == 0) {
            return;
        }
        List<HL7TreeNode> treeData = null;
        if (form != null) {
            treeData = form.getTreeData();

        } else if (hl7ui != null) {
            if (hl7ui.gethl7Manager() instanceof HL7OutputManager) {
                treeData = ((HL7OutputManager) hl7ui.gethl7Manager()).getTreeData();
            }
        }
        treeData.clear();
        treeData.addAll(newInput);
        xmlViewer.setInput(treeData);
        // TreeUtil.guessAndSetLoopNode((HL7TreeNode) xmlViewer.getTree().getItem(0).getData());
        xmlViewer.refresh();
        xmlViewer.expandAll();
        // hl7ui.updateStatus();
        // hl7ui.redrawLinkers();
        if (hl7ui != null) {
            hl7ui.redrawLinkers();
        } else if (form != null) {
            form.refreshLinks();
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.eclipse.ui.actions.SelectionProviderAction#selectionChanged(org.eclipse.jface.viewers.IStructuredSelection)
     */
    @Override
    public void selectionChanged(IStructuredSelection selection) {
        this.setEnabled(true);
        HL7TreeNode node = (HL7TreeNode) this.getStructuredSelection().getFirstElement();
        if (node != null && form != null) {
            // hl7ui.setSelectedText(node.getLabel());
            // Added by Marvin Wang on Sep. 11, 2012 for bug TDI-20702.
            form.setSelectedText(node.getLabel());
        }
    }

}
