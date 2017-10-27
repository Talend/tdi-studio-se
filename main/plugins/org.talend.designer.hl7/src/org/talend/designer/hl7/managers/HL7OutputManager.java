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
package org.talend.designer.hl7.managers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.talend.commons.exception.ExceptionHandler;
import org.talend.core.model.metadata.IMetadataColumn;
import org.talend.core.model.metadata.IMetadataTable;
import org.talend.core.model.process.ElementParameterParser;
import org.talend.core.model.process.IConnection;
import org.talend.core.model.process.IConnectionCategory;
import org.talend.core.model.utils.NodeUtil;
import org.talend.designer.hl7.HL7InputComponent;
import org.talend.designer.hl7.ui.data.Element;
import org.talend.designer.hl7.ui.data.HL7TreeNode;

/**
 * DOC hwang class global comment. Detailled comment
 */
public class HL7OutputManager extends HL7Manager {

    private List<HL7TreeNode> treeData;

    private String currentSchema;

    private Map<String, List<HL7TreeNode>> contents = new HashMap<String, List<HL7TreeNode>>();

    private Map<String, Integer> orderMap = new HashMap<String, Integer>();

    private int order = 1;

    private Map<String, String> schemaMap = new HashMap<String, String>();

    public HL7OutputManager(HL7InputComponent hl7Component) {
        super(hl7Component);
        uiManager = new UIManager(this);
        initModel();
    }

    public void initModel() {
        List<? extends IConnection> incomingConnections = NodeUtil.getIncomingConnections(hl7Component, IConnectionCategory.FLOW);
        // HL7Root root = new HL7Root("root");
        List<Map<String, String>> maps = (List<Map<String, String>>) ElementParameterParser.getObjectValue(hl7Component,
                "__SCHEMAS__"); //$NON-NLS-1$
        List<String> schemaList = new ArrayList<String>();
        for (IMetadataTable table : hl7Component.getMetadataList()) {
            if (table.getLabel() != null) {
                schemaList.add(table.getLabel());
            }
        }

        List<Map<String, String>> rootTable = hl7Component.getTableList(HL7InputComponent.ROOT);
        Map<String, IMetadataTable> schemaNameToInputTable = new HashMap<String, IMetadataTable>();

        if (!maps.isEmpty()) {
            for (Map<String, String> map : maps) {
                String schemaName = map.get("SCHEMA");
                int first = schemaName.indexOf("_");
                int second = schemaName.lastIndexOf("_");
                if (first > 0 && first < second) {
                    schemaName = schemaName.substring(first + 1, second);
                }
                IMetadataTable metadataTable = null;
                for (IConnection connection : incomingConnections) {
                    if (connection.getUniqueName().equals(map.get("PARENT_ROW"))) {
                        metadataTable = connection.getMetadataTable();
                        metadataTable.setLabel(connection.getUniqueName());
                        schemaNameToInputTable.put(schemaName, metadataTable);
                        break;
                    }
                }
            }
        } else {
            for (String schemaName : schemaList) {
                IMetadataTable metadataTable = null;
                for (IConnection connection : incomingConnections) {
                    if (connection.getUniqueName().equals(schemaName)) {
                        metadataTable = connection.getMetadataTable();
                        metadataTable.setLabel(connection.getUniqueName());
                        schemaNameToInputTable.put(schemaName, metadataTable);
                    }
                }
            }
        }
        Map<String, HL7TreeNode> mapNodes = new HashMap<String, HL7TreeNode>();
        treeData = new ArrayList<HL7TreeNode>();

        HL7TreeNode rootNode = null;
        HL7TreeNode current = null;
        HL7TreeNode temp = null;
        String currentPath = null;
        String defaultValue = null;
        int nodeOrder = 0;
        boolean haveOrder = true;
        // build root tree
        for (Map<String, String> rootMap : rootTable) {
            String newPath = rootMap.get(HL7InputComponent.PATH);
            String columnName = rootMap.get(HL7InputComponent.COLUMN);
            defaultValue = rootMap.get(HL7InputComponent.VALUE);
            String orderValue = rootMap.get(HL7InputComponent.ORDER);
            boolean repeatable = Boolean.valueOf(rootMap.get("REPEATABLE"));
            if (orderValue == null || "".equals(orderValue)) {
                haveOrder = false;
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
            if (rootNode == null) {
                rootNode = temp;
            }
            if (rootMap.get(HL7InputComponent.ATTRIBUTE).equals("main")) { //$NON-NLS-1$
                temp.setMain(true);
            }
            current = temp;
            currentPath = newPath;
            temp.setRepetable(repeatable);
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
            }
        }
        if (rootNode == null) {
            rootNode = new Element("rootTag");
        }
        if (haveOrder) {
            orderNode(rootNode);
        }
        // the root node should not set the ColumnLabel
        if (rootNode.getRow() != null) {
            rootNode.setRow(null);
        }
        treeData.add(rootNode);
        contents.put(rootNode.getColumnLabel(), treeData);
        initCurrentSchema();
    }

    public List<Map<String, String>> getRootTable() {
        List<Map<String, String>> result = new ArrayList<Map<String, String>>();
        for (HL7TreeNode rootNode : this.getOriginalNodes()) {
            // if (!(rootNode instanceof HL7Root)) {
            initNodeOrder(rootNode);
            order = 1;
            // }
            tableLoader((Element) rootNode, "", result, rootNode.getDefaultValue()); //$NON-NLS-1$
        }
        return result;
    }

    public List<Map<String, String>> getLoopTable() {
        List<Map<String, String>> result = new ArrayList<Map<String, String>>();
        for (HL7TreeNode rootNode : this.getOriginalNodes()) {
            Element loopNode = (Element) getLoopNode(rootNode);
            if (loopNode != null) {
                String path = getPath(loopNode);
                tableLoader(loopNode, path.substring(0, path.lastIndexOf("/")), result, loopNode.getDefaultValue()); //$NON-NLS-1$
            }
        }
        return result;
    }

    public String getPath(HL7TreeNode treeNode) {
        StringBuffer path = new StringBuffer();
        HL7TreeNode tmp = treeNode;
        while (tmp != null) {
            path.insert(0, "/" + tmp.getLabel()); //$NON-NLS-1$
            tmp = tmp.getParent();
        }
        return path.toString();
    }

    public HL7TreeNode getLoopNode(HL7TreeNode root) {
        if (root != null && root instanceof Element) {
            Element e = (Element) root;
            if (e.isRepetable()) {
                return e;
            }
            for (HL7TreeNode child : e.getElementChildren()) {
                HL7TreeNode loopNode = getLoopNode(child);
                if (loopNode != null) {
                    return loopNode;
                }
            }
        }
        return null;
    }

    // public void initNodeOrder(HL7TreeNode node) {
    // if (node == null) {
    // return;
    // }
    // HL7TreeNode parent = node.getParent();
    // if (parent == null) {
    // node.setOrder(1);
    // String path = TreeUtil.getPath(node);
    // orderMap.put(path, order);
    // order++;
    // }
    // List<HL7TreeNode> childNode = node.getChildren();
    // for (HL7TreeNode child : childNode) {
    // child.setOrder(order);
    // String path = TreeUtil.getPath(child);
    // orderMap.put(path, order);
    // order++;
    // if (child.getChildren().size() > 0) {
    // initNodeOrder(child);
    // }
    // }
    //
    // }

    protected void tableLoader(Element element, String parentPath, List<Map<String, String>> table, String defaultValue) {
        Map<String, String> newMap = new HashMap<String, String>();
        String currentPath = parentPath + "/" + element.getLabel(); //$NON-NLS-1$
        newMap.put(HL7InputComponent.PATH, currentPath);
        newMap.put(HL7InputComponent.COLUMN, element.getColumnLabel());
        newMap.put(HL7InputComponent.ATTRIBUTE, element.isMain() ? "main" : "branch");
        newMap.put(HL7InputComponent.VALUE, defaultValue);
        newMap.put(HL7InputComponent.ORDER, String.valueOf(getNodeOrder(element)));
        newMap.put("REPEATABLE", String.valueOf(element.isRepetable()));

        table.add(newMap);
        for (HL7TreeNode att : element.getAttributeChildren()) {
            newMap = new HashMap<String, String>();
            newMap.put(HL7InputComponent.PATH, att.getLabel());
            newMap.put(HL7InputComponent.COLUMN, att.getColumnLabel());
            newMap.put(HL7InputComponent.ATTRIBUTE, "attri"); //$NON-NLS-1$
            newMap.put(HL7InputComponent.VALUE, att.getDefaultValue());
            newMap.put(HL7InputComponent.ORDER, String.valueOf(getNodeOrder(att)));
            newMap.put("REPEATABLE", String.valueOf(att.isRepetable()));
            table.add(newMap);
        }
        for (HL7TreeNode att : element.getNameSpaceChildren()) {
            newMap = new HashMap<String, String>();
            newMap.put(HL7InputComponent.PATH, att.getLabel());
            newMap.put(HL7InputComponent.COLUMN, att.getColumnLabel());
            newMap.put(HL7InputComponent.ATTRIBUTE, "ns"); //$NON-NLS-1$
            newMap.put(HL7InputComponent.VALUE, att.getDefaultValue());
            newMap.put("REPEATABLE", String.valueOf(att.isRepetable()));
            newMap.put(HL7InputComponent.ORDER, String.valueOf(getNodeOrder(att)));
            table.add(newMap);
        }
        List<HL7TreeNode> children = element.getElementChildren();
        for (HL7TreeNode child : children) {
            // if (!child.isGroup() && !child.isRepetable()) {// && !child.isRepetable()
            tableLoader((Element) child, currentPath, table, child.getDefaultValue());
            // }
        }
    }

    public void orderNode(HL7TreeNode node) {
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

    public void sortOrder(HL7TreeNode treeNode, HL7TreeNode node) {
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

    public boolean isRepetable() {
        return false;
    }

    public HL7TreeNode getRootHL7TreeNode(HL7TreeNode node) {
        if (node != null) {
            HL7TreeNode parent = node.getParent();
            if (parent == null) {
                return node;
            }
            return getRootHL7TreeNode(parent);
        }
        return null;
    }

    @Override
    public List<HL7TreeNode> getTreeData(String curSchema) {
        if (currentSchema == null) {
            if (treeData == null) {
                return new ArrayList<HL7TreeNode>();
            }
            return treeData;
        } else {
            if (!contents.containsKey(curSchema)) {
                return new ArrayList<HL7TreeNode>();
            }
            return contents.get(curSchema);
        }

    }

    public List<HL7TreeNode> getTreeData() {
        if (currentSchema == null) {
            return treeData;
        } else {
            return getOriginalNodes();
        }
    }

    /**
     * Sets the treeData.
     *
     * @param treeData the treeData to set
     */
    public void setTreeData(List<HL7TreeNode> treeData) {
        this.treeData = treeData;
        contents.put(currentSchema, treeData);
    }

    /**
     *
     * DOC hwang Comment method "getOriginalNodes".
     *
     * @return
     */
    protected List<HL7TreeNode> getOriginalNodes() {
        List<HL7TreeNode> tmpTreeData = new ArrayList<HL7TreeNode>();
        Set<Entry<String, List<HL7TreeNode>>> set = contents.entrySet();
        Iterator<Entry<String, List<HL7TreeNode>>> iterator = set.iterator();
        while (iterator.hasNext()) {
            Entry<String, List<HL7TreeNode>> entry = iterator.next();
            List<HL7TreeNode> list = entry.getValue();
            tmpTreeData.addAll(list);
        }
        // List<? extends IConnection> incomingConnections = NodeUtil.getIncomingConnections(getHl7Component(),
        // IConnectionCategory.FLOW);
        // if (incomingConnections.size() > 0) {
        // for (IConnection conn : incomingConnections) {
        // String uniqueName = conn.getUniqueName();
        // List<HL7TreeNode> list = contents.get(uniqueName);
        // tmpTreeData.addAll(list);
        // }
        // }
        return tmpTreeData;
    }

    /**
     * Sets the currentSchema.
     *
     * @param currentSchema the currentSchema to set
     */
    public void setCurrentSchema(String currentSchema) {
        this.currentSchema = currentSchema;
        List<Map<String, String>> maps = (List<Map<String, String>>) ElementParameterParser.getObjectValue(hl7Component,
                "__SCHEMAS__"); //$NON-NLS-1$
        for (Map<String, String> map : maps) {
            if (map.containsValue(currentSchema)) {
                if (map.get("PARENT_ROW") != null && map.get("PARENT_ROW").equals(currentSchema)) {
                    String schemaName = map.get("SCHEMA");
                    int first = schemaName.indexOf("_");
                    int second = schemaName.lastIndexOf("_");
                    if (first > 0 && first < second) {
                        schemaName = schemaName.substring(first + 1, second);
                    }
                    schemaMap.put(currentSchema, schemaName);
                    break;
                }

            }
        }
    }

    @Override
    public String getCurrentSchema(boolean sign) {
        if (sign && schemaMap.get(currentSchema) != null && !"".equals(schemaMap.get(currentSchema))) {
            return schemaMap.get(currentSchema);
        }
        return this.currentSchema;
    }

    public String initCurrentSchema() {
        List<? extends IConnection> incomingConnections = NodeUtil.getIncomingConnections(hl7Component, IConnectionCategory.FLOW);
        if (incomingConnections.size() <= 0) {
            return this.currentSchema;
        }
        List<String> connNameList = new ArrayList<String>();
        for (IConnection connection : incomingConnections) {
            connNameList.add(connection.getUniqueName());
        }
        // if (connNameList.contains(this.currentSchema)) {
        // return this.currentSchema;
        // }
        this.currentSchema = connNameList.get(0);
        return this.currentSchema;
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

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.designer.hl7.managers.HL7Manager#saveDataToComponent()
     */
    @Override
    public boolean saveDataToComponent() {
        boolean result = false;
        List<Map<String, String>> root = new ArrayList<Map<String, String>>();

        root.addAll(getRootTable());

        if (hl7Component.setTableElementParameter(root, HL7InputComponent.ROOT)) {
            result = true;
        }
        return result;
    }

    public void initNodeOrder(HL7TreeNode node) {
        if (node == null) {
            return;
        }
        HL7TreeNode parent = node.getParent();
        if (parent == null) {
            node.setOrder(1);
            String path = getPath(node);
            orderMap.put(path, order);
            order++;
        }
        List<HL7TreeNode> childNode = node.getChildren();
        for (HL7TreeNode child : childNode) {
            child.setOrder(order);
            String path = getPath(child);
            orderMap.put(path, order);
            order++;
            if (child.getChildren().size() > 0) {
                initNodeOrder(child);
            }
        }
    }

    public int getNodeOrder(HL7TreeNode node) {
        if (node != null) {
            String path = getPath(node);
            return orderMap.get(path);
        }
        return 0;
    }

    public Map<String, List<HL7TreeNode>> getContents() {
        return this.contents;
    }

    @Override
    public List<IMetadataColumn> getSchemaData(String currentSchema) {
        List<? extends IConnection> incomingConnections = NodeUtil.getIncomingConnections(hl7Component, IConnectionCategory.FLOW);
        for (IConnection connection : incomingConnections) {
            IMetadataTable metadataTable = connection.getMetadataTable();
            if (metadataTable.getLabel() != null && currentSchema != null && metadataTable.getLabel().equals(currentSchema)) {
                return metadataTable.getListColumns();
            }
        }
        return new ArrayList<IMetadataColumn>();
    }

}
