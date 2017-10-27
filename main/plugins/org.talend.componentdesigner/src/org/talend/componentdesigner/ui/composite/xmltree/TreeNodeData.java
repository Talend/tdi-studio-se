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
package org.talend.componentdesigner.ui.composite.xmltree;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.talend.componentdesigner.PluginConstant;
import org.talend.datatools.xml.utils.ATreeNode;
import org.w3c.dom.Node;

/**
 * DOC rli class global comment. Detailled comment
 */
public class TreeNodeData {

    private static final String TEXT_CONST = "#text"; //$NON-NLS-1$

    private String bodayText;

    private Map<String, String> attrMap = new HashMap<String, String>();

    private Node xmlNode;

    private ATreeNode aTreeNode;

    private String treePath;

    public String putAttrValue(String attrName, String attr) {
        return attrMap.put(attrName, attr);
    }

    public String getAttrValue(String attrName) {
        return attrMap.get(attrName);
    }

    public String removeAttrValue(String attrName) {
        return attrMap.remove(attrName);
    }

    public Object[] getTreeAttrNames() {
        Object[] childrenNode = this.aTreeNode.getChildren();
        List<Object> treeNodeAttrList = new ArrayList<Object>();
        for (Object child : childrenNode) {
            if (((ATreeNode) child).getType() == ATreeNode.ATTRIBUTE_TYPE) {
                treeNodeAttrList.add(((ATreeNode) child).getValue());
            }
        }
        return treeNodeAttrList.toArray();
    }

    public boolean isHasTreeAttr() {
        return getTreeAttrNames().length != 0;
    }

    public boolean isHasChildTreeNode() {
        Object[] childrenNode = this.aTreeNode.getChildren();
        for (Object child : childrenNode) {
            if (((ATreeNode) child).getType() == ATreeNode.ELEMENT_TYPE) {
                return true;
            }
        }
        return false;

        // NodeList nodeList = this.xmlNode.getChildNodes();
        // for (int i = 0; i < nodeList.getLength(); i++) {
        // final Node subNode = nodeList.item(i);
        // if (!subNode.getNodeName().equals(TEXT_CONST)) {
        // return true;
        // }
        // }
        // return false;
    }

    /**
     * Getter for bodayText.
     * 
     * @return the bodayText
     */
    public String getBodayText() {
        return bodayText == null ? PluginConstant.EMPTY_STRING : bodayText;
    }

    /**
     * Sets the bodayText.
     * 
     * @param bodayText the bodayText to set
     */
    public void setBodayText(String bodayText) {
        this.bodayText = bodayText;
    }

    /**
     * Getter for node.
     * 
     * @return the node
     */
    public Node getXMLNode() {
        return xmlNode;
    }

    /**
     * Sets the node.
     * 
     * @param node the node to set
     */
    public void setXMLNode(Node node) {
        this.xmlNode = node;
    }

    public Object[] getChildNodeNames() {
        Object[] nodeList = this.aTreeNode.getChildren();
        List<Object> nameList = new ArrayList<Object>();
        for (Object node : nodeList) {
            ATreeNode treeNode = (ATreeNode) node;
            if (treeNode.getType() == treeNode.ELEMENT_TYPE) {
                nameList.add(treeNode.getValue());
            }
        }
        // for (int i = 0; i < nodeList.getLength(); i++) {
        // final Node childNode = nodeList.item(i);
        // if (nameList.contains(childNode.getNodeName())) {
        // continue;
        // }
        // if (childNode.getNodeName().equals(TEXT_CONST)) {
        // continue;
        // } else {
        // nameList.add(childNode.getNodeName());
        // }
        // }
        return nameList.toArray();
    }

    /**
     * Getter for treeNode.
     * 
     * @return the treeNode
     */
    public ATreeNode getTreeNode() {
        return aTreeNode;
    }

    /**
     * Sets the treeNode.
     * 
     * @param treeNode the treeNode to set
     */
    public void setTreeNode(ATreeNode treeNode) {
        this.aTreeNode = treeNode;
    }

    /**
     * Getter for treePath.
     * 
     * @return the treePath
     */
    public String getTreePath() {
        return treePath;
    }

    /**
     * Sets the treePath.
     * 
     * @param treePath the treePath to set
     */
    public void setTreePath(String treePath) {
        this.treePath = treePath;
    }
}
