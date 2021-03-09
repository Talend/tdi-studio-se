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
package org.talend.designer.fileoutputxml.managers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.talend.core.model.metadata.IMetadataTable;
import org.talend.core.model.process.IConnection;
import org.talend.core.model.process.IConnectionCategory;
import org.talend.core.model.utils.NodeUtil;
import org.talend.designer.fileoutputxml.FileOutputXMLComponent;
import org.talend.metadata.managment.ui.wizard.metadata.xml.node.Attribute;
import org.talend.metadata.managment.ui.wizard.metadata.xml.node.Element;
import org.talend.metadata.managment.ui.wizard.metadata.xml.node.FOXTreeNode;
import org.talend.metadata.managment.ui.wizard.metadata.xml.node.NameSpaceNode;
import org.talend.metadata.managment.ui.wizard.metadata.xml.utils.TreeUtil;

/**
 * wzhang class global comment. Detailled comment
 */
public class MultiFOXManager extends FOXManager {

    protected Map<String, Map<String, Integer>> orderMap = new HashMap<String, Map<String, Integer>>();

    /**
     * wzhang FOXMultiManager constructor comment.
     *
     * @param foxComponent
     */
    public MultiFOXManager(FileOutputXMLComponent foxComponent) {
        super(foxComponent);
        uiManager = new MultiUIManager(this);
    }

    public List<IMetadataTable> getMultiSchemaData() {
        List<IMetadataTable> schemas = new ArrayList<IMetadataTable>();

        List<? extends IConnection> incomingConnections = NodeUtil.getIncomingConnections(foxComponent, IConnectionCategory.FLOW);
        for (IConnection connection : incomingConnections) {
            connection.getMetadataTable().setLabel(connection.getUniqueName());
            schemas.add(connection.getMetadataTable());
        }
        return schemas;
    }

    public void initModel() {
        int i = 0;
        List<? extends IConnection> incomingConnections = NodeUtil.getIncomingConnections(foxComponent, IConnectionCategory.FLOW);
        for (IConnection connection : incomingConnections) {
            IMetadataTable metadataTable = connection.getMetadataTable();
            metadataTable.setLabel(connection.getUniqueName());
            treeData = new ArrayList<FOXTreeNode>();
            if (i == 0)// the first schema as current
                currentSchema = metadataTable.getLabel();
            FOXTreeNode rootNode = null;
            FOXTreeNode current = null;
            FOXTreeNode temp = null;
            FOXTreeNode mainNode = null;
            String mainPath = null;
            String currentPath = null;
            String defaultValue = null;
            int nodeOrder = 0;
            boolean haveOrder = true;

            String schemaId = metadataTable.getLabel() + ":"; //$NON-NLS-1$

            // build root tree
            List<Map<String, String>> rootTable = (List<Map<String, String>>) foxComponent
                    .getTableList(FileOutputXMLComponent.ROOT);
            if (rootTable != null) {
                for (Map<String, String> rootMap : rootTable) {
                    String newPath = rootMap.get(FileOutputXMLComponent.PATH);
                    String columnName = rootMap.get(FileOutputXMLComponent.COLUMN);
                    defaultValue = rootMap.get(FileOutputXMLComponent.VALUE);
                    String orderValue = rootMap.get(FileOutputXMLComponent.ORDER);
                    if (orderValue == null || "".equals(orderValue)) {
                        haveOrder = false;
                    }
                    if (haveOrder) {
                        nodeOrder = Integer.valueOf(rootMap.get(FileOutputXMLComponent.ORDER)).intValue();
                    }
                    String flag = columnName + ":"; //$NON-NLS-1$
                    if (columnName != null && columnName.length() > 0 && !flag.startsWith(metadataTable.getLabel() + ":")) { //$NON-NLS-1$
                        continue;
                    }
                    if (rootMap.get(FileOutputXMLComponent.ATTRIBUTE).equals("attri")) { //$NON-NLS-1$
                        temp = new Attribute(newPath);
                        temp.setDefaultValue(defaultValue);
                        temp.setAttribute(true);
                        current.addChild(temp);
                    } else if (rootMap.get(FileOutputXMLComponent.ATTRIBUTE).equals("ns")) { //$NON-NLS-1$
                        temp = new NameSpaceNode(newPath);
                        temp.setDefaultValue(defaultValue);
                        temp.setNameSpace(true);
                        current.addChild(temp);
                    } else {
                        temp = addElement(current, currentPath, newPath, defaultValue);
                        if (rootNode == null) {
                            rootNode = temp;
                        }
                        if (rootMap.get(FileOutputXMLComponent.ATTRIBUTE).equals("main")) { //$NON-NLS-1$
                            temp.setMain(true);
                            mainNode = temp;
                            mainPath = newPath;
                        }
                        current = temp;
                        currentPath = newPath;
                    }
                    if (haveOrder) {
                        temp.setOrder(nodeOrder);
                    }
                    temp.setRow(metadataTable.getLabel());
                    if (columnName != null && columnName.length() > 0 && columnName.startsWith(schemaId)) {
                        columnName = columnName.replace(schemaId, ""); //$NON-NLS-1$
                        temp.setColumn(metadataTable.getColumn(columnName));
                        temp.setTable(metadataTable);
                    }
                }
            }

            // build group tree
            current = mainNode;
            currentPath = mainPath;
            boolean isFirst = true;
            List<Map<String, String>> groupTable = (List<Map<String, String>>) foxComponent
                    .getTableList(FileOutputXMLComponent.GROUP);
            if (groupTable != null) {
                for (Map<String, String> groupMap : groupTable) {
                    String newPath = groupMap.get(FileOutputXMLComponent.PATH);
                    String columnName = groupMap.get(FileOutputXMLComponent.COLUMN);
                    defaultValue = groupMap.get(FileOutputXMLComponent.VALUE);
                    String orderValue = groupMap.get(FileOutputXMLComponent.ORDER);
                    if (orderValue == null || "".equals(orderValue)) {
                        haveOrder = false;
                    }
                    if (haveOrder) {
                        nodeOrder = Integer.valueOf(groupMap.get(FileOutputXMLComponent.ORDER)).intValue();
                    }
                    String flag = columnName + ":"; //$NON-NLS-1$
                    if (columnName != null && columnName.length() > 0 && !flag.startsWith(metadataTable.getLabel() + ":")) { //$NON-NLS-1$
                        continue;
                    }
                    if (groupMap.get(FileOutputXMLComponent.ATTRIBUTE).equals("attri")) { //$NON-NLS-1$
                        temp = new Attribute(newPath);
                        temp.setDefaultValue(defaultValue);
                        temp.setAttribute(true);
                        current.addChild(temp);
                    } else if (groupMap.get(FileOutputXMLComponent.ATTRIBUTE).equals("ns")) { //$NON-NLS-1$
                        temp = new NameSpaceNode(newPath);
                        temp.setDefaultValue(defaultValue);
                        temp.setNameSpace(true);
                        current.addChild(temp);
                    } else {
                        temp = this.addElement(current, currentPath, newPath, defaultValue);
                        if (groupMap.get(FileOutputXMLComponent.ATTRIBUTE).equals("main")) { //$NON-NLS-1$
                            temp.setMain(true);
                            mainNode = temp;
                            mainPath = newPath;
                        }
                        if (isFirst) {
                            temp.setGroup(true);
                            isFirst = false;
                        }
                        current = temp;
                        currentPath = newPath;
                    }
                    if (haveOrder) {
                        temp.setOrder(nodeOrder);
                    }
                    temp.setRow(metadataTable.getLabel());
                    if (columnName != null && columnName.length() > 0 && columnName.startsWith(schemaId)) {
                        columnName = columnName.replace(schemaId, ""); //$NON-NLS-1$
                        temp.setColumn(metadataTable.getColumn(columnName));
                        temp.setTable(metadataTable);
                    }
                }
            }

            // build loop tree
            current = mainNode;
            currentPath = mainPath;
            isFirst = true;
            List<Map<String, String>> loopTable = (List<Map<String, String>>) foxComponent
                    .getTableList(FileOutputXMLComponent.LOOP);
            if (loopTable != null) {
                for (Map<String, String> loopMap : loopTable) {
                    String newPath = loopMap.get(FileOutputXMLComponent.PATH);
                    String columnName = loopMap.get(FileOutputXMLComponent.COLUMN);
                    defaultValue = loopMap.get(FileOutputXMLComponent.VALUE);
                    String orderValue = loopMap.get(FileOutputXMLComponent.ORDER);
                    if (orderValue == null || "".equals(orderValue)) {
                        haveOrder = false;
                    }
                    if (haveOrder) {
                        nodeOrder = Integer.valueOf(loopMap.get(FileOutputXMLComponent.ORDER)).intValue();
                    }
                    String flag = columnName + ":"; //$NON-NLS-1$
                    if (columnName != null && columnName.length() > 0 && !flag.startsWith(metadataTable.getLabel() + ":")) { //$NON-NLS-1$
                        continue;
                    }
                    if (loopMap.get(FileOutputXMLComponent.ATTRIBUTE).equals("attri")) { //$NON-NLS-1$
                        temp = new Attribute(newPath);
                        temp.setDefaultValue(defaultValue);
                        temp.setAttribute(true);
                        current.addChild(temp);
                    } else if (loopMap.get(FileOutputXMLComponent.ATTRIBUTE).equals("ns")) { //$NON-NLS-1$
                        temp = new NameSpaceNode(newPath);
                        temp.setDefaultValue(defaultValue);
                        temp.setNameSpace(true);
                        current.addChild(temp);
                    } else {
                        temp = this.addElement(current, currentPath, newPath, defaultValue);
                        if (loopMap.get(FileOutputXMLComponent.ATTRIBUTE).equals("main")) { //$NON-NLS-1$
                            temp.setMain(true);
                        }
                        if (isFirst) {
                            temp.setLoop(true);
                            isFirst = false;
                        }
                        current = temp;
                        currentPath = newPath;
                    }
                    if (haveOrder) {
                        temp.setOrder(nodeOrder);
                    }
                    temp.setRow(metadataTable.getLabel());
                    if (columnName != null && columnName.length() > 0 && columnName.startsWith(schemaId)) {
                        columnName = columnName.replace(schemaId, ""); //$NON-NLS-1$
                        temp.setColumn(metadataTable.getColumn(columnName));
                        temp.setTable(metadataTable);
                    }
                }

                if (rootNode == null) {
                    rootNode = new Element("rootTag"); //$NON-NLS-1$
                }

                rootNode.setParent(null);
                if (haveOrder) {
                    orderNode(rootNode);
                }
                treeData.add(rootNode);
                rootNode.setRow(metadataTable.getLabel());
                contents.put(metadataTable.getLabel(), treeData);
                i++;
            }
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.fileoutputxml.managers.FOXManager#saveDataToComponent()
     */
    @Override
    public boolean saveDataToComponent() {
        boolean result = false;
        List<Map<String, String>> root = new ArrayList<Map<String, String>>();
        List<Map<String, String>> loop = new ArrayList<Map<String, String>>();
        List<Map<String, String>> group = new ArrayList<Map<String, String>>();

        root.addAll(getRootTable());
        loop.addAll(getLoopTable());
        group.addAll(getGroupTable());

        if (foxComponent.setTableElementParameter(root, FileOutputXMLComponent.ROOT)) {
            result = true;
        }
        if (foxComponent.setTableElementParameter(loop, FileOutputXMLComponent.LOOP)) {
            result = true;
        }
        if (foxComponent.setTableElementParameter(group, FileOutputXMLComponent.GROUP)) {
            result = true;
        }
        return result;
    }

    @Override
    public List<Map<String, String>> getGroupTable() {
        List<Map<String, String>> result = new ArrayList<Map<String, String>>();
        for (FOXTreeNode rootNode : this.getOriginalNodes()) {
            Element groupNode = (Element) TreeUtil.getGroupNode(rootNode);
            if (groupNode != null) {
                String path = TreeUtil.getPath(groupNode);
                tableLoader(groupNode, path.substring(0, path.lastIndexOf("/")), result, groupNode.getDefaultValue()); //$NON-NLS-1$
            }
        }
        return result;

    }

    @Override
    public List<Map<String, String>> getLoopTable() {
        List<Map<String, String>> result = new ArrayList<Map<String, String>>();
        for (FOXTreeNode rootNode : this.getOriginalNodes()) {
            Element loopNode = (Element) org.talend.metadata.managment.ui.wizard.metadata.xml.utils.TreeUtil
                    .getLoopNode(rootNode);
            if (loopNode != null) {
                String path = TreeUtil.getPath(loopNode);
                tableLoader(loopNode, path.substring(0, path.lastIndexOf("/")), result, loopNode.getDefaultValue()); //$NON-NLS-1$
            }
        }
        return result;
    }

    @Override
    public List<Map<String, String>> getRootTable() {
        List<Map<String, String>> result = new ArrayList<Map<String, String>>();
        for (FOXTreeNode rootNode : this.getOriginalNodes()) {
            initNodeOrder(rootNode);
            order = 1;
            tableLoader((Element) rootNode, "", result, rootNode.getDefaultValue()); //$NON-NLS-1$
        }
        return result;
    }

    protected void tableLoader(Element element, String parentPath, List<Map<String, String>> table, String defaultValue) {
        Map<String, String> newMap = new HashMap<String, String>();
        String currentPath = parentPath + "/" + element.getLabel(); //$NON-NLS-1$
        newMap.put(FileOutputXMLComponent.PATH, currentPath);
        newMap.put(FileOutputXMLComponent.COLUMN, element.getColumnLabel());
        newMap.put(FileOutputXMLComponent.ATTRIBUTE, element.isMain() ? "main" : "branch"); //$NON-NLS-1$ //$NON-NLS-2$
        newMap.put(FileOutputXMLComponent.VALUE, defaultValue); //$NON-NLS-1$
        newMap.put(FileOutputXMLComponent.ORDER, String.valueOf(getNodeOrder(element)));

        table.add(newMap);
        for (FOXTreeNode att : element.getAttributeChildren()) {
            newMap = new HashMap<String, String>();
            newMap.put(FileOutputXMLComponent.PATH, att.getLabel());
            newMap.put(FileOutputXMLComponent.COLUMN, att.getColumnLabel());
            newMap.put(FileOutputXMLComponent.ATTRIBUTE, "attri"); //$NON-NLS-1$
            newMap.put(FileOutputXMLComponent.VALUE, att.getDefaultValue()); //$NON-NLS-1$
            newMap.put(FileOutputXMLComponent.ORDER, String.valueOf(getNodeOrder(att)));
            table.add(newMap);
        }
        for (FOXTreeNode att : element.getNameSpaceChildren()) {
            newMap = new HashMap<String, String>();
            newMap.put(FileOutputXMLComponent.PATH, att.getLabel());
            newMap.put(FileOutputXMLComponent.COLUMN, att.getColumnLabel());
            newMap.put(FileOutputXMLComponent.ATTRIBUTE, "ns"); //$NON-NLS-1$
            newMap.put(FileOutputXMLComponent.VALUE, att.getDefaultValue()); //$NON-NLS-1$
            newMap.put(FileOutputXMLComponent.ORDER, String.valueOf(getNodeOrder(att)));
            table.add(newMap);
        }
        List<FOXTreeNode> children = element.getElementChildren();
        for (FOXTreeNode child : children) {
            if (!child.isGroup() && !child.isLoop()) {
                tableLoader((Element) child, currentPath, table, child.getDefaultValue());
            }
        }
    }

    public void initNodeOrder(FOXTreeNode node) {
        if (node == null) {
            return;
        }
        FOXTreeNode parent = node.getParent();
        if (parent == null) {
            node.setOrder(1);
            String path = TreeUtil.getPath(node);
            Map<String, Integer> xpathMap = orderMap.get(node.getRow());
            if (xpathMap == null) {
                xpathMap = new HashMap<String, Integer>();
                orderMap.put(node.getRow(), xpathMap);
            }
            xpathMap.put(path, order);
            order++;
        }
        List<FOXTreeNode> childNode = node.getChildren();
        for (FOXTreeNode child : childNode) {
            child.setOrder(order);
            String path = TreeUtil.getPath(child);

            Map<String, Integer> xpathMap = orderMap.get(child.getRow());
            if (xpathMap == null) {
                xpathMap = new HashMap<String, Integer>();
                orderMap.put(child.getRow(), xpathMap);
            }
            xpathMap.put(path, order);
            order++;
            if (child.getChildren().size() > 0) {
                initNodeOrder(child);
            }
        }

    }

    public int getNodeOrder(FOXTreeNode node) {
        if (node != null) {
            String path = TreeUtil.getPath(node);
            Map<String, Integer> xpathMap = orderMap.get(node.getRow());
            if (xpathMap != null) {
                return xpathMap.get(path);
            }
            return 0;
        }
        return 0;
    }

}
