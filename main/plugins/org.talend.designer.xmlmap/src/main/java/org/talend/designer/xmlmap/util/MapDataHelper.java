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
package org.talend.designer.xmlmap.util;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.emf.common.util.EList;
import org.talend.core.model.metadata.IMetadataColumn;
import org.talend.core.model.metadata.IMetadataTable;
import org.talend.core.model.process.EConnectionType;
import org.talend.core.model.process.IConnection;
import org.talend.designer.xmlmap.XmlMapComponent;
import org.talend.designer.xmlmap.model.emf.xmlmap.InputXmlTree;
import org.talend.designer.xmlmap.model.emf.xmlmap.NodeType;
import org.talend.designer.xmlmap.model.emf.xmlmap.OutputTreeNode;
import org.talend.designer.xmlmap.model.emf.xmlmap.OutputXmlTree;
import org.talend.designer.xmlmap.model.emf.xmlmap.TreeNode;
import org.talend.designer.xmlmap.model.emf.xmlmap.XmlMapData;
import org.talend.designer.xmlmap.model.emf.xmlmap.XmlmapFactory;
import org.talend.designer.xmlmap.model.tree.XML_MAP_LOOKUP_MODE;
import org.talend.designer.xmlmap.model.tree.XML_MAP_MATCHING_MODE;

/**
 * DOC talend class global comment. Detailled comment
 */
public class MapDataHelper {

    public void rebuildXmlMapData(XmlMapData mapData, XmlMapComponent xmlMapComponent) {
        if (xmlMapComponent != null) {
            rebuildModelInputs(xmlMapComponent.getIncomingConnections(), mapData);
            rebuildModelOutputs(xmlMapComponent.getMetadataList(), mapData);
        }
    }

    public void rebuildModelInputs(List<? extends IConnection> inputConn, XmlMapData mapData) {
        // remove no used input tree
        if (mapData.getInputTrees().size() != inputConn.size()) {
            List treesToRemove = new ArrayList();
            for (InputXmlTree inputTree : mapData.getInputTrees()) {
                boolean found = false;
                for (IConnection connection : inputConn) {
                    if (inputTree.getName().equals(connection.getName())) {
                        found = true;
                    }
                }
                if (!found) {
                    for (TreeNode treeNode : inputTree.getNodes()) {
                        XmlMapUtil.detachNodeConnections(treeNode, mapData, true);
                    }
                    treesToRemove.add(inputTree);
                    XmlMapUtil.detachFilterSource(inputTree, mapData);
                }
            }

            mapData.getInputTrees().removeAll(treesToRemove);
        }

        for (IConnection inData : inputConn) {
            String name = inData.getName();
            InputXmlTree inputTree = null;
            for (InputXmlTree in : mapData.getInputTrees()) {
                if (in.getName() != null && in.getName().equals(name)) {
                    inputTree = in;
                    break;
                }
            }
            if (inputTree == null) {
                inputTree = XmlmapFactory.eINSTANCE.createInputXmlTree();
                inputTree.setName(name);
                inputTree.setLookup(EConnectionType.FLOW_MAIN != inData.getLineStyle());
                mapData.getInputTrees().add(inputTree.isLookup() ? mapData.getInputTrees().size() : 0, inputTree);
            } else {
                inputTree.setLookup(EConnectionType.FLOW_MAIN != inData.getLineStyle());
            }
            if (inputTree.getLookupMode() == null) {
                inputTree.setLookupMode(XML_MAP_LOOKUP_MODE.LOAD_ONCE.toString());
            }
            if (inputTree.getMatchingMode() == null) {
                inputTree.setMatchingMode(XML_MAP_MATCHING_MODE.ALL_ROWS.toString());
            }
            rebuildInputTree(inputTree, inData.getMetadataTable(), mapData);

        }

    }

    public void rebuildInputTree(InputXmlTree inputTree, IMetadataTable metadataTable, XmlMapData mapData) {
        if (metadataTable != null && metadataTable.getListColumns() != null) {
            List<IMetadataColumn> listColumns = metadataTable.getListColumns();
            EList<TreeNode> nodes = inputTree.getNodes();
            for (int i = 0; i < listColumns.size(); i++) {
                IMetadataColumn column = listColumns.get(i);
                TreeNode found = null;
                int j = 0;
                for (; j < nodes.size(); j++) {
                    TreeNode node = nodes.get(j);
                    if (node.getName() != null && node.getName().equals(column.getLabel())) {
                        found = node;
                        break;
                    }
                }
                if (found != null) {
                    // set in case talend type changed in metadata
                    found.setType(column.getTalendType());
                    if (i != j) {
                        // do switch to keep the same sequence
                        TreeNode temp = nodes.get(j);
                        nodes.remove(j);
                        nodes.add(i, temp);
                    }
                } else {
                    found = XmlmapFactory.eINSTANCE.createTreeNode();
                    found.setName(column.getLabel());
                    found.setType(column.getTalendType());
                    found.setNullable(column.isNullable());
                    found.setXpath(XmlMapUtil.getXPath(inputTree.getName(), found.getName(), found.getNodeType()));

                    nodes.add(i, found);
                }

                // add a default root for document
                if (XmlMapUtil.DOCUMENT.equals(found.getType())) {
                    EList<TreeNode> children = found.getChildren();
                    // if type is changed from a non-document to document
                    if (children.isEmpty()) {
                        XmlMapUtil.detachNodeConnections(found, mapData, true);
                        TreeNode treeRoot = XmlmapFactory.eINSTANCE.createTreeNode();
                        treeRoot.setName("root");
                        treeRoot.setType(XmlMapUtil.DEFAULT_DATA_TYPE);
                        treeRoot.setNodeType(NodeType.ELEMENT);
                        treeRoot.setXpath(XmlMapUtil.getXPath(found.getXpath(), treeRoot.getName(), treeRoot.getNodeType()));
                        treeRoot.setLoop(true);
                        treeRoot.setMain(true);
                        children.add(treeRoot);
                    }
                }
                // remove children and connections for children if not document
                else {
                    EList<TreeNode> children = found.getChildren();
                    if (!children.isEmpty()) {
                        XmlMapUtil.detachNodeConnections(found, mapData, true);
                        List<TreeNode> copyOfChildren = new ArrayList<TreeNode>(found.getChildren());
                        found.getChildren().clear();
                        if (!inputTree.isLookup() && inputTree.isMultiLoops()) {
                            List<TreeNode> oldLoops = new ArrayList<TreeNode>();
                            XmlMapUtil.getChildLoops(oldLoops, copyOfChildren);
                            inputTree.setMultiLoops(XmlMapUtil.checkMultiLoopsStatus(inputTree));
                            XmlMapUtil.removeloopInOutputTree(mapData, inputTree, oldLoops);
                        }
                    }
                }

            }

            if (nodes.size() > listColumns.size()) {
                List unUsed = new ArrayList();
                for (int i = listColumns.size(); i < nodes.size(); i++) {
                    XmlMapUtil.detachNodeConnections(nodes.get(i), mapData, true);
                    unUsed.add(nodes.get(i));
                }
                nodes.removeAll(unUsed);
            }

        }

        // re-build the connections in case any unnecessary connections are created because of previous bugs and can't
        // be deleted
        if (inputTree.isLookup()) {
            rebuildInputNodesConnections(inputTree.getNodes(), mapData);
        }
    }

    private void rebuildInputNodesConnections(List<TreeNode> treeNodes, XmlMapData mapData) {
        for (TreeNode node : treeNodes) {
            if (node != null) {
                if (!node.getLookupIncomingConnections().isEmpty()) {
                    if (!XmlMapUtil.isExpressionEditable(node)) {
                        node.setExpression("");
                        XmlMapUtil.detachLookupSource(node, mapData, false);
                    }
                }
                if (!node.getChildren().isEmpty()) {
                    rebuildInputNodesConnections(node.getChildren(), mapData);
                }
            }
        }
    }

    public void rebuildModelOutputs(List<IMetadataTable> outputMetadataTables, XmlMapData mapData) {
        for (IMetadataTable meatadataTable : outputMetadataTables) {
            String name = meatadataTable.getTableName();
            OutputXmlTree outputTree = null;
            for (OutputXmlTree out : mapData.getOutputTrees()) {
                if (out.getName() != null && out.getName().equals(name)) {
                    outputTree = out;
                    break;
                }
            }
            if (outputTree == null) {
                outputTree = XmlmapFactory.eINSTANCE.createOutputXmlTree();
                outputTree.setName(name);
                mapData.getOutputTrees().add(outputTree);
            }

            List<IMetadataColumn> listColumns = meatadataTable.getListColumns();
            if (listColumns != null) {
                EList<OutputTreeNode> nodes = outputTree.getNodes();
                for (int i = 0; i < listColumns.size(); i++) {
                    IMetadataColumn column = listColumns.get(i);
                    OutputTreeNode found = null;
                    int j = 0;
                    for (; j < nodes.size(); j++) {
                        OutputTreeNode node = nodes.get(j);
                        if (node.getName() != null && node.getName().equals(column.getLabel())) {
                            found = node;
                            break;
                        }
                    }
                    if (found != null) {
                        // set in case talend type changed in metadata
                        found.setType(column.getTalendType());
                        if (i != j) {
                            // do switch to keep the same sequence
                            OutputTreeNode temp = nodes.get(j);
                            nodes.remove(j);
                            nodes.add(i, temp);
                        }
                    } else {
                        found = XmlmapFactory.eINSTANCE.createOutputTreeNode();
                        found.setName(column.getLabel());
                        found.setType(column.getTalendType());
                        found.setNullable(column.isNullable());
                        found.setXpath(XmlMapUtil.getXPath(outputTree.getName(), found.getName(), found.getNodeType()));

                        nodes.add(i, found);
                    }

                    // add a default root for document
                    if (XmlMapUtil.DOCUMENT.equals(found.getType())) {
                        EList<TreeNode> children = found.getChildren();
                        if (children.isEmpty()) {
                            XmlMapUtil.detachConnectionsSouce(found, mapData);
                            TreeNode treeRoot = XmlmapFactory.eINSTANCE.createOutputTreeNode();
                            treeRoot.setName("root");
                            treeRoot.setType(XmlMapUtil.DEFAULT_DATA_TYPE);
                            treeRoot.setNodeType(NodeType.ELEMENT);
                            treeRoot.setXpath(XmlMapUtil.getXPath(found.getXpath(), treeRoot.getName(), treeRoot.getNodeType()));
                            treeRoot.setLoop(true);
                            treeRoot.setMain(true);
                            children.add(treeRoot);
                        }
                    }
                    // remove children and connections for children if not document
                    else {
                        EList<TreeNode> children = found.getChildren();
                        if (!children.isEmpty()) {

                            XmlMapUtil.detachNodeConnections(found, mapData, true);
                            found.getChildren().clear();
                        }
                    }

                }

                if (nodes.size() > listColumns.size()) {
                    List unUsed = new ArrayList();
                    for (int i = listColumns.size(); i < nodes.size(); i++) {
                        OutputTreeNode node = nodes.get(i);
                        XmlMapUtil.detachConnectionsSouce(node, mapData);
                        unUsed.add(node);
                    }
                    nodes.removeAll(unUsed);
                }

            }
            mapData.getOutputTrees().add(outputTree);

            // re-build the connections in case any unnecessary connections are created because of previous bugs and
            // can't be deleted
            rebuildOutputNodesConnections(outputTree.getNodes(), mapData);
        }
    }

    private void rebuildOutputNodesConnections(List<? extends TreeNode> treeNodes, XmlMapData mapData) {

        for (TreeNode node : treeNodes) {
            if (node != null) {
                if (!node.getIncomingConnections().isEmpty()) {
                    if (!XmlMapUtil.isExpressionEditable(node)) {
                        node.setExpression("");
                        XmlMapUtil.detachConnectionsSouce(node, mapData, false);
                    }
                }
                if (!node.getChildren().isEmpty()) {
                    rebuildOutputNodesConnections(node.getChildren(), mapData);
                }
            }
        }

    }

}
