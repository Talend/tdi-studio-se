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
import org.talend.designer.xmlmap.model.emf.xmlmap.AbstractInOutTree;
import org.talend.designer.xmlmap.model.emf.xmlmap.Connection;
import org.talend.designer.xmlmap.model.emf.xmlmap.InputLoopNodesTable;
import org.talend.designer.xmlmap.model.emf.xmlmap.InputXmlTree;
import org.talend.designer.xmlmap.model.emf.xmlmap.LookupConnection;
import org.talend.designer.xmlmap.model.emf.xmlmap.OutputTreeNode;
import org.talend.designer.xmlmap.model.emf.xmlmap.OutputXmlTree;
import org.talend.designer.xmlmap.model.emf.xmlmap.TreeNode;
import org.talend.designer.xmlmap.model.emf.xmlmap.XmlmapFactory;

/**
 * DOC wchen class global comment. Detailled comment
 *
 * @deprecated don't do any auto map for InputLoopTable
 */
@Deprecated
public class InputLoopTableUtil {

    public static void addSourceLoopToInputLoopTable(TreeNode connectionSource, OutputTreeNode targetOutputNode,
            InputXmlTree mainInputTree) {
        // create InputLoopTable nodes only when main table is multiloops
        if (mainInputTree == null || !mainInputTree.isMultiLoops()) {
            return;
        }

        InputLoopNodesTable inputLoopNodesTable = null;
        AbstractInOutTree abstractTree = XmlMapUtil.getAbstractInOutTree(targetOutputNode);
        List<InputLoopNodesTable> listInputLoopNodesTablesEntry = ((OutputXmlTree) abstractTree).getInputLoopNodesTables();
        if (!XmlMapUtil.hasDocument(abstractTree)) {
            if (listInputLoopNodesTablesEntry.size() == 0) {
                inputLoopNodesTable = XmlmapFactory.eINSTANCE.createInputLoopNodesTable();
                listInputLoopNodesTablesEntry.add(inputLoopNodesTable);
            } else if (listInputLoopNodesTablesEntry != null && listInputLoopNodesTablesEntry.size() == 1) {
                inputLoopNodesTable = listInputLoopNodesTablesEntry.get(0);
            }
        } else {
            OutputTreeNode loopParentOutputTreeNode = (OutputTreeNode) XmlMapUtil.getLoopParentNode(targetOutputNode);
            if (loopParentOutputTreeNode != null) {
                inputLoopNodesTable = loopParentOutputTreeNode.getInputLoopNodesTable();
                if (inputLoopNodesTable == null) {
                    inputLoopNodesTable = XmlmapFactory.eINSTANCE.createInputLoopNodesTable();
                    loopParentOutputTreeNode.setInputLoopNodesTable(inputLoopNodesTable);
                    listInputLoopNodesTablesEntry.add(inputLoopNodesTable);
                }
            }
        }
        if (inputLoopNodesTable == null) {
            return;
        }
        List<TreeNode> soruceLoops = new ArrayList<TreeNode>();

        getSourceLoopsFromConnectionSource(connectionSource, soruceLoops, mainInputTree);

        for (TreeNode sourceLoop : soruceLoops) {
            if (!inputLoopNodesTable.getInputloopnodes().contains(sourceLoop)) {
                inputLoopNodesTable.getInputloopnodes().add(sourceLoop);
            }
        }
    }

    private static void getSourceLoopsFromConnectionSource(TreeNode connectionSource, List<TreeNode> soruceLoops,
            InputXmlTree mainInputTree) {
        InputXmlTree inputTree = (InputXmlTree) XmlMapUtil.getAbstractInOutTree(connectionSource);
        TreeNode loopParentTreeNode = null;

        if (inputTree == mainInputTree) {
            loopParentTreeNode = XmlMapUtil.getLoopParentNode(connectionSource);
            if (loopParentTreeNode != null) {
                soruceLoops.add(loopParentTreeNode);
            }
        } else {
            getLoopSourceFromLookupNode(connectionSource, mainInputTree, soruceLoops);
        }
    }

    private static InputLoopNodesTable getInputLoopNodesTable(OutputTreeNode outputNode) {
        InputLoopNodesTable inputLoopNodesTable = null;
        AbstractInOutTree abstractTree = XmlMapUtil.getAbstractInOutTree(outputNode);
        List<InputLoopNodesTable> listInputLoopNodesTablesEntry = ((OutputXmlTree) abstractTree).getInputLoopNodesTables();
        if (!XmlMapUtil.hasDocument(abstractTree)) {
            if (listInputLoopNodesTablesEntry.size() == 0) {
                inputLoopNodesTable = XmlmapFactory.eINSTANCE.createInputLoopNodesTable();
                listInputLoopNodesTablesEntry.add(inputLoopNodesTable);
            } else if (listInputLoopNodesTablesEntry != null && listInputLoopNodesTablesEntry.size() == 1) {
                inputLoopNodesTable = listInputLoopNodesTablesEntry.get(0);
            }
        } else {
            OutputTreeNode loopParentOutputTreeNode = (OutputTreeNode) XmlMapUtil.getLoopParentNode(outputNode);
            if (loopParentOutputTreeNode != null) {
                inputLoopNodesTable = loopParentOutputTreeNode.getInputLoopNodesTable();
                if (inputLoopNodesTable == null) {
                    inputLoopNodesTable = XmlmapFactory.eINSTANCE.createInputLoopNodesTable();
                    loopParentOutputTreeNode.setInputLoopNodesTable(inputLoopNodesTable);
                    listInputLoopNodesTablesEntry.add(inputLoopNodesTable);
                }
            }
        }
        return inputLoopNodesTable;
    }

    private static void getLoopSourceFromLookupNode(TreeNode connectionSource, InputXmlTree mainTable, List<TreeNode> soruceLoops) {
        EList<LookupConnection> lookupIncomingConnections = connectionSource.getLookupIncomingConnections();
        for (LookupConnection lookupConn : lookupIncomingConnections) {
            TreeNode sourceTreeNode = (TreeNode) lookupConn.getSource();
            AbstractInOutTree sourceTree = XmlMapUtil.getAbstractInOutTree(sourceTreeNode);
            if (sourceTree == mainTable) {
                TreeNode loopParentTreeNode = XmlMapUtil.getLoopParentNode(sourceTreeNode);
                if (loopParentTreeNode != null && !soruceLoops.contains(loopParentTreeNode)) {
                    soruceLoops.add(loopParentTreeNode);
                }
            } else {
                getLoopSourceFromLookupNode(sourceTreeNode, mainTable, soruceLoops);
            }
        }

    }

    public static void removeSourceLoopFromInputLoopTable(List removedConnections, OutputTreeNode outputNode,
            InputXmlTree mainInputTree) {
        if (mainInputTree == null || !mainInputTree.isMultiLoops()) {
            return;
        }

        List<TreeNode> neededSource = new ArrayList<TreeNode>();

        InputLoopNodesTable inputLoopNodesTable = null;
        AbstractInOutTree abstractTree = XmlMapUtil.getAbstractInOutTree(outputNode);
        List<InputLoopNodesTable> listInputLoopNodesTablesEntry = ((OutputXmlTree) abstractTree).getInputLoopNodesTables();
        if (!XmlMapUtil.hasDocument(abstractTree)) {
            if (listInputLoopNodesTablesEntry.size() == 0) {
                inputLoopNodesTable = XmlmapFactory.eINSTANCE.createInputLoopNodesTable();
                listInputLoopNodesTablesEntry.add(inputLoopNodesTable);
            } else if (listInputLoopNodesTablesEntry != null && listInputLoopNodesTablesEntry.size() == 1) {
                inputLoopNodesTable = listInputLoopNodesTablesEntry.get(0);
            }
            OutputXmlTree outputTree = (OutputXmlTree) abstractTree;
            getSourceLoop(neededSource, outputTree.getNodes(), mainInputTree);
        } else {
            OutputTreeNode loopParentOutputTreeNode = (OutputTreeNode) XmlMapUtil.getLoopParentNode(outputNode);
            if (loopParentOutputTreeNode != null) {
                inputLoopNodesTable = loopParentOutputTreeNode.getInputLoopNodesTable();
                if (inputLoopNodesTable == null) {
                    inputLoopNodesTable = XmlmapFactory.eINSTANCE.createInputLoopNodesTable();
                    loopParentOutputTreeNode.setInputLoopNodesTable(inputLoopNodesTable);
                    listInputLoopNodesTablesEntry.add(inputLoopNodesTable);
                }
                List<OutputTreeNode> nodes = new ArrayList<OutputTreeNode>();
                nodes.add(loopParentOutputTreeNode);
                getSourceLoop(neededSource, nodes, mainInputTree);
            }
        }
        if (inputLoopNodesTable == null) {
            return;
        }

        List<TreeNode> sourceLoopsToRemove = new ArrayList<TreeNode>();
        for (Object object : removedConnections) {
            if (object instanceof Connection) {
                Connection connection = (Connection) object;
                if (connection.getSource() instanceof TreeNode) {
                    getSourceLoopsFromConnectionSource((TreeNode) connection.getSource(), sourceLoopsToRemove, mainInputTree);
                }
            }
        }

        for (TreeNode sourceToRemove : sourceLoopsToRemove) {
            if (!neededSource.contains(sourceToRemove)) {
                inputLoopNodesTable.getInputloopnodes().remove(sourceToRemove);
            }
        }
    }

    private static void getSourceLoop(List<TreeNode> neededSource, List<? extends TreeNode> outputTreeNode,
            InputXmlTree mainInputTree) {
        for (TreeNode node : outputTreeNode) {
            for (Connection connection : node.getIncomingConnections()) {
                if (connection.getSource() instanceof TreeNode) {
                    getSourceLoopsFromConnectionSource((TreeNode) connection.getSource(), neededSource, mainInputTree);
                }
            }
            if (!node.getChildren().isEmpty()) {
                getSourceLoop(neededSource, node.getChildren(), mainInputTree);
            }
        }
    }

}
