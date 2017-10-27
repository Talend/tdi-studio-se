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

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.talend.componentdesigner.PluginConstant;
import org.talend.componentdesigner.exception.ExceptionHandler;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * DOC rli class global comment. Detailled comment
 */
public class TreePopulator {

    private static final String TEXT_CONST = "#text"; //$NON-NLS-1$

    private final Tree availableXmlTree;

    private Document readDocument;

    // private final BidiMap xPathToTreeItem = new DualHashBidiMap();

    private String filePath;

    public TreePopulator(Tree availableXmlTree, String filePath) {
        this.availableXmlTree = availableXmlTree;
        this.filePath = filePath;
    }

    public boolean populateTree() {
        InputStream inputStream = null;
        try {
            inputStream = new FileInputStream(filePath);
        } catch (IOException e) {
            // e.printStackTrace();
            ExceptionHandler.process(e);
        }
        // availableXmlTree.removeAll();
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = null;
        try {
            builder = factory.newDocumentBuilder();
            readDocument = builder.parse(inputStream);
        } catch (Exception e) {
            ExceptionHandler.process(e);
        }
        Node docmentEle = readDocument.getDocumentElement();
        // NodeList firstList = docmentEle.getChildNodes();
        populateTreeItems(availableXmlTree, docmentEle, 0, PluginConstant.COMPONENT_NODE_NAME);
        // for (int i = 0; i < firstList.getLength(); i++) {
        // Node categoryitm = firstList.item(i);
        // if (!categoryitm.getNodeName().equals(TEXT_CONST)) {
        // storeNodeAttrData(categoryitm);
        // populateTreeItems(availableXmlTree, categoryitm, 0);
        // }
        // }
        return true;
    }

    /**
     * populate tree items.
     * 
     * @param tree
     * @param node
     */
    private void populateTreeItems(Object tree, Node element, int level, String parentXPath) {
        level++;
        if (level > 10) {
            return;
        } else {
            NodeList nodeList = element.getChildNodes();
            // Object[] chidrenTreeNode = aTreeNode.getChildren();
            for (int i = 0; i < nodeList.getLength(); i++) {
                final Node subNode = nodeList.item(i);
                if (subNode.getNodeName().equals(TEXT_CONST)) {
                    continue;
                }

                TreeItem treeItem;
                if (tree instanceof Tree) {
                    treeItem = new TreeItem((Tree) tree, 0);
                } else {
                    treeItem = new TreeItem((TreeItem) tree, 0);
                }

                TreeNodeData treeNodeData = storeNodeAttrData(subNode);

                treeItem.setData(treeNodeData);
                treeItem.setText(subNode.getNodeName());

                String currentTreePath = parentXPath + "/" + treeItem.getText(); //$NON-NLS-1$
                treeNodeData.setTreePath(currentTreePath);
                treeNodeData.setTreeNode(ATreeNodeUtil.getTreeNodeByPath(currentTreePath));

                // //$NON-NLS-1$
                // xPathToTreeItem.put(currentXPath, treeItem);

                if (subNode.getChildNodes() != null && subNode.getChildNodes().getLength() > 0) {
                    populateTreeItems(treeItem, subNode, level, currentTreePath);
                }
                setExpanded(treeItem);
            }
        }
    }

    /**
     * DOC rli Comment method "storeNodeAttrData".
     * 
     * @param subNode
     * @return
     */
    private TreeNodeData storeNodeAttrData(Node subNode) {
        TreeNodeData treeNodeData = null;
        treeNodeData = new TreeNodeData();
        treeNodeData.setXMLNode(subNode);
        treeNodeData.setBodayText(subNode.getTextContent());
        NamedNodeMap attrMap = subNode.getAttributes();
        if (attrMap.getLength() > 0) {
            for (int j = 0; j < attrMap.getLength(); j++) {
                Node attrNode = attrMap.item(j);
                if (attrNode instanceof Attr) {
                    treeNodeData.putAttrValue(attrNode.getNodeName(), ((Attr) attrNode).getValue());
                }
            }

        }
        return treeNodeData;
    }

    // expand the tree
    private void setExpanded(TreeItem treeItem) {
        if (treeItem.getParentItem() != null) {
            setExpanded(treeItem.getParentItem());
        }
        treeItem.setExpanded(true);
    }

    // public TreeItem getTreeItem(String absoluteXPath) {
    // return (TreeItem) xPathToTreeItem.get(absoluteXPath);
    // }
    //
    // public String getAbsoluteXPath(TreeItem treeItem) {
    // return (String) xPathToTreeItem.getKey(treeItem);
    // }

    /**
     * Getter for filePath.
     * 
     * @return the filePath
     */
    public String getFilePath() {
        return this.filePath;
    }

    /**
     * Getter for readDocument.
     * 
     * @return the readDocument
     */
    public Document getReadDocument() {
        return readDocument;
    }

    /**
     * DOC rli Comment method "main".
     * 
     * @param args
     */
    public static void main(String[] args) {

    }

}
