// ============================================================================
//
// Copyright (C) 2006-2019 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.designer.codegen.config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.talend.core.model.process.AbstractNode;
import org.talend.core.model.process.EConnectionType;
import org.talend.core.model.process.IConnection;
import org.talend.core.model.process.IConnectionCategory;
import org.talend.core.model.process.INode;
import org.talend.core.model.temp.ECodePart;
import org.talend.core.model.temp.ETypeGen;

/**
 * A process is cut out in a list of NodesSubTree. Each Subtrees are traversed to generate code.
 *
 * $Id$
 *
 */
public class NodesSubTree {

    INode rootNode;

    String name;

    List<String> beforeSubProcesses;

    List<String> afterSubProcesses;

    List<INode> nodes;

    HashMap<INode, Integer> visitedNodesBeginCode;

    HashMap<INode, Integer> visitedNodesMainCode;

    HashMap<INode, Integer> visitedNodesEndCode;

    HashMap<INode, Integer> visitedNodesFinallyCode;

    private static final boolean DEBUG = false;

    boolean isMergeSubTree = false;

    List<IConnection> allMainSubTreeConnections = null;

    List<INode> mergeBranchStarts;

    List<INode> mergeNodes;

    boolean isRefSubTree = false;// for mr only

    List<INode> refNodes; // for mr only

    /* display size of method code in comment */
    boolean methodSizeNeeded = false;

    public boolean isMethodSizeNeeded() {
        return methodSizeNeeded;
    }

    public void setMethodSizeNeeded(boolean methodSizeNeeded) {
        this.methodSizeNeeded = methodSizeNeeded;
    }

    /**
     * Constructor for a NodesSubTree.
     *
     * @param NodesSubTree Root Node
     */
    public NodesSubTree(INode node, List<? extends INode> nodes) {
        this.rootNode = node;
        this.name = node.getUniqueName();
        this.nodes = new ArrayList<INode>();
        afterSubProcesses = new ArrayList<String>();
        beforeSubProcesses = new ArrayList<String>();
        this.visitedNodesMainCode = new HashMap<INode, Integer>();
        this.visitedNodesBeginCode = new HashMap<INode, Integer>();
        this.visitedNodesEndCode = new HashMap<INode, Integer>();
        this.visitedNodesFinallyCode = new HashMap<INode, Integer>();
        this.isMergeSubTree = node.isThereLinkWithMerge();
        allMainSubTreeConnections = new ArrayList<IConnection>();

        buildSubTree(node, false);

        if (isMergeSubTree) {

            this.mergeBranchStarts = new ArrayList<INode>();
            mergeBranchStarts.add(node);

            Map<INode, Integer> mergeInfo = rootNode.getLinkedMergeInfo();
            mergeNodes = Arrays.asList(mergeInfo.keySet().toArray(new INode[mergeInfo.size()]));

            uniteMergeSubTree(nodes);
        }

    }

    public NodesSubTree(INode node, List<? extends INode> nodes, ETypeGen typeGen) {
        if (typeGen == ETypeGen.CAMEL) {
            this.rootNode = node;
            this.name = node.getUniqueName();
            this.nodes = new ArrayList<INode>();
            this.visitedNodesMainCode = new HashMap<INode, Integer>();
            allMainSubTreeConnections = new ArrayList<IConnection>();

            buildCamelSubTree(node, false);
        } else if (typeGen == ETypeGen.MR) {
            this.rootNode = node;
            this.name = node.getUniqueName();
            this.nodes = new ArrayList<INode>();
            afterSubProcesses = new ArrayList<String>();
            beforeSubProcesses = new ArrayList<String>();

            allMainSubTreeConnections = new ArrayList<IConnection>();

            buildBigDataSubTree(node);
        } else if (typeGen == ETypeGen.SPARK) {
            this.rootNode = node;
            this.name = node.getUniqueName();
            this.nodes = new ArrayList<INode>();
            this.visitedNodesMainCode = new HashMap<INode, Integer>();
            afterSubProcesses = new ArrayList<String>();
            beforeSubProcesses = new ArrayList<String>();

            allMainSubTreeConnections = new ArrayList<IConnection>();

            buildSparkSubTree(node);
        }
    }

    /**
     * unite all the relative merge nodes to this subTree
     *
     * @param node
     */
    public void uniteMergeSubTree(List<? extends INode> nodes) {

        for (INode node : nodes) {

            // if the first merge branch
            if (node == rootNode) {
                continue;
            }

            // if the node link with the same merge node
            for (INode mNode : mergeNodes) {
                if (node.isActivate() && node.isSubProcessStart() && node.getLinkedMergeInfo() != null
                        && node.getLinkedMergeInfo().get(mNode) != null) {
                    mergeBranchStarts.add(node);
                    buildSubTree(node, true);
                }
            }

        }
    }

    /**
     * Build the SubSubTrees..
     *
     * @param nodes
     */

    private void buildCamelSubTree(INode node, boolean breakWhenMerge) {
        if (DEBUG) {
            System.out.print(node.getUniqueName());
        }
        for (IConnection connection : node.getOutgoingCamelSortedConnections()) {
            if (connection.getTarget().isActivate()) {
                buildCamelSubTree(connection.getTarget(), breakWhenMerge);
            }
        }
        visitedNodesMainCode.put(node, 0);
        nodes.add(node);
    }

    private void buildBigDataSubTree(INode node) {
        if (((AbstractNode) node).isThereLinkWithRef()) {
            this.isRefSubTree = true;
            List<INode> newRefNodes = ((AbstractNode) node).getRefNodes();
            if (newRefNodes != null) {
                if (refNodes == null) {
                    refNodes = new ArrayList<INode>(newRefNodes);
                } else {
                    refNodes.addAll(((AbstractNode) node).getRefNodes());
                }
                for (INode refNode : newRefNodes) {
                    buildBigDataSubTree(refNode);
                }
            }
        }
        for (IConnection connection : node.getOutgoingSortedConnections()) {
            if (connection.getTarget().isActivate()) {

                if (connection.getLineStyle().hasConnectionCategory(IConnectionCategory.MAIN)) {
                    if (!connection.getLineStyle().hasConnectionCategory(IConnectionCategory.USE_ITERATE)) {
                        allMainSubTreeConnections.add(connection);
                    }
                    buildBigDataSubTree(connection.getTarget());
                }
                if (connection.getLineStyle().equals(EConnectionType.RUN_AFTER)) {
                    afterSubProcesses.add(connection.getTarget().getUniqueName());
                }
                if (connection.getLineStyle().equals(EConnectionType.ON_SUBJOB_OK)) {
                    beforeSubProcesses.add(connection.getTarget().getUniqueName());
                }
            }
        }

        nodes.add(node);
    }

    private void buildSparkSubTree(INode node) {
        this.visitedNodesMainCode.put(node, 0);
        if (node.getComponent().useMerge()) {
            for (IConnection connection : node.getIncomingConnections(EConnectionType.FLOW_MERGE)) {
                if (this.visitedNodesMainCode.get(connection.getSource()) != null) {
                    // The node has been visited, continue the loop.
                    continue;
                }
                INode sourceNode = connection.getSource();
                while (sourceNode.getIncomingConnections().size() > 0) {
                    sourceNode = sourceNode.getIncomingConnections().get(0).getSource();
                }
                buildSparkSubTree(sourceNode);
            }
        }
        if (node.getComponent().useLookup()) {
            for (IConnection connection : node.getIncomingConnections(EConnectionType.FLOW_REF)) {
                if (this.visitedNodesMainCode.get(connection.getSource()) != null) {
                    // The node has been visited, continue the loop.
                    continue;
                }
                INode sourceNode = connection.getSource();
                while (sourceNode.getIncomingConnections().size() > 0) {
                    sourceNode = sourceNode.getIncomingConnections().get(0).getSource();
                }
                buildSparkSubTree(sourceNode);
            }
        }
        for (IConnection connection : node.getOutgoingSortedConnections()) {
            if (connection.getTarget().isActivate() && this.visitedNodesMainCode.get(connection.getTarget()) == null) {
                if (connection.getLineStyle().hasConnectionCategory(IConnectionCategory.DATA)) {
                    allMainSubTreeConnections.add(connection);
                    buildSparkSubTree(connection.getTarget());
                }
                if (connection.getLineStyle().equals(EConnectionType.RUN_AFTER)) {
                    afterSubProcesses.add(connection.getTarget().getUniqueName());
                }
                if (connection.getLineStyle().equals(EConnectionType.ON_SUBJOB_OK)) {
                    beforeSubProcesses.add(connection.getTarget().getUniqueName());
                }
            }
        }

        nodes.add(node);
    }

    private void buildSubTree(INode node, boolean breakWhenMerge) {
        if (DEBUG) {
            System.out.print(node.getUniqueName());
        }
        for (IConnection connection : node.getOutgoingSortedConnections()) {
            if (connection.getTarget().isActivate()) {
                // Old FLOW Check
                // if (connection.getLineStyle().getCategory().equals(EConnectionCategory.MAIN)) {
                // if (DEBUG) {
                // System.out.print(" -> ");
                // }
                // buildSubTree((INode) connection.getTarget());
                // }
                if (breakWhenMerge && connection.getLineStyle().hasConnectionCategory(IConnectionCategory.MERGE)) {
                    continue;
                }

                if (connection.getLineStyle().hasConnectionCategory(IConnectionCategory.MAIN)) {
                    if (!connection.getLineStyle().hasConnectionCategory(IConnectionCategory.USE_ITERATE)) {
                        allMainSubTreeConnections.add(connection);
                    }
                    if (DEBUG) {
                        System.out.print(" -> "); //$NON-NLS-1$
                    }
                    buildSubTree(connection.getTarget(), breakWhenMerge);
                }
                if (connection.getLineStyle().equals(EConnectionType.RUN_AFTER)) {
                    afterSubProcesses.add(connection.getTarget().getUniqueName());
                }
                if (connection.getLineStyle().equals(EConnectionType.ON_SUBJOB_OK)) {
                    beforeSubProcesses.add(connection.getTarget().getUniqueName());
                }
            }
        }
        if (DEBUG) {
            System.out.println(""); //$NON-NLS-1$
        }

        visitedNodesMainCode.put(node, 0);
        visitedNodesBeginCode.put(node, 0);
        visitedNodesEndCode.put(node, 0);
        visitedNodesFinallyCode.put(node, 0);
        nodes.add(node);
    }

    /**
     * Tells if this Node is marked for this specific code part in this SubTree.
     *
     * @param node
     * @return
     */
    public Boolean isMarkedNode(INode node, ECodePart part) {
        Boolean result = null;
        switch (part) {
        case MAIN:
            Integer countMain = visitedNodesMainCode.get(node);
            if (countMain == null) {
                return null;
            } else {
                return false;
            }
        case BEGIN:
            Integer countBegin = visitedNodesBeginCode.get(node);
            if (countBegin == null) {
                return null;
            } else {
                return false;
            }
        case END:
            Integer countEnd = visitedNodesEndCode.get(node);
            if (countEnd == null) {
                return null;
            } else {
                return false;
            }
        case FINALLY:
            Integer countFinally = visitedNodesFinallyCode.get(node);
            if (countFinally == null) {
                return null;
            } else {
                return false;
            }
        default:
            return result;
        }

    }

    /**
     * Mark a Node of this SubTree as Visited for this specific code part.
     *
     * @param node
     */
    public void markNode(INode node, ECodePart part) {
        switch (part) {
        case MAIN:
            visitedNodesMainCode.put(node, visitedNodesMainCode.get(node) + 1);
            break;
        case BEGIN:
            visitedNodesBeginCode.put(node, visitedNodesBeginCode.get(node) + 1);
            break;
        case END:
            visitedNodesEndCode.put(node, visitedNodesEndCode.get(node) + 1);
            break;
        case FINALLY:
            visitedNodesFinallyCode.put(node, visitedNodesFinallyCode.get(node) + 1);
        default:
            // do nothing
        }
    }

    /**
     * Getter for rootNode.
     *
     * @return the rootNode
     */
    public INode getRootNode() {
        return this.rootNode;
    }

    /**
     * Getter for name.
     *
     * @return the name
     */
    public String getName() {
        return this.name;
    }

    /**
     * Sets the name.
     *
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Getter for afterSubProcesses.
     *
     * @return the afterSubProcesses
     */
    public List<String> getAfterSubProcesses() {
        return this.afterSubProcesses;
    }

    /**
     * Getter for beforeSubProcesses.
     *
     * @return the beforeSubProcesses
     */
    public List<String> getBeforeSubProcesses() {
        return this.beforeSubProcesses;
    }

    /**
     * Getter for nodes.
     *
     * @return the nodes
     */
    public List<INode> getNodes() {
        return this.nodes;
    }

    /**
     * Sets the nodes.
     *
     * @param nodes the nodes to set
     */
    public void setNodes(List<INode> nodes) {
        this.nodes = nodes;
    }

    /**
     * Return true if this subtree contains the node node.
     *
     * @param node the node to search
     * @return true if the node exists , false otherwise
     */
    public boolean containsNode(INode node) {
        for (INode toCompareNode : this.nodes) {
            if (toCompareNode.getUniqueName().equals(node.getUniqueName())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Return node from uniqueNameNode.
     *
     * @param uniqueNameNode
     * @return node if exists, null otherwise
     */
    public INode getNode(String uniqueNameNode) {
        for (INode toCompareNode : this.nodes) {
            if (toCompareNode.getUniqueName().equals(uniqueNameNode)) {
                return toCompareNode;
            }
        }
        return null;
    }

    /**
     * sort method, make sure the first one is the real start of the merge job
     *
     * @return
     */
    public List<INode> getSortedMergeBranchStarts() {

        if (mergeBranchStarts != null) {

            Collections.sort(mergeBranchStarts, new Comparator<INode>() {

                @Override
                public int compare(INode node1, INode node2) {
                    Map<INode, Integer> mergeInfo1 = node1.getLinkedMergeInfo();
                    Map<INode, Integer> mergeInfo2 = node2.getLinkedMergeInfo();
                    for (INode mNode : mergeNodes) {
                        if (mergeInfo1.get(mNode) != null && mergeInfo2.get(mNode) != null) {
                            if (mergeInfo1.get(mNode) > mergeInfo2.get(mNode)) {
                                return 1;
                            } else {
                                return -1;
                            }
                        }
                        if (mergeInfo1.get(mNode) != null && mergeInfo2.get(mNode) == null) {
                            return -1;
                        }
                        if (mergeInfo1.get(mNode) == null && mergeInfo2.get(mNode) != null) {
                            return 1;
                        }
                    }
                    return -1;

                }
            });
        }

        return new ArrayList<>(mergeBranchStarts);
    }

    public boolean isMergeSubTree() {
        return this.isMergeSubTree;
    }

    public List<INode> getMergeNodes() {
        return mergeNodes;
    }

    // public INode getMergeNode() {
    // return this.mergeNode;
    // }

    /**
     * Getter for allMainSubTreeConnections.
     *
     * @return the allMainSubTreeConnections
     */
    public List<IConnection> getAllMainSubTreeConnections() {
        return allMainSubTreeConnections;
    }

    /**
     * Sets the allMainSubTreeConnections.
     *
     * @param allMainSubTreeConnections the allMainSubTreeConnections to set
     */
    public void setAllMainSubTreeConnections(List<IConnection> allMainSubTreeConnections) {
        this.allMainSubTreeConnections = allMainSubTreeConnections;
    }

    /**
     * Getter for isRefSubTree.
     *
     * @return the isRefSubTree
     */
    public boolean isRefSubTree() {
        return this.isRefSubTree;
    }

    /**
     * Sets the isRefSubTree.
     *
     * @param isRefSubTree the isRefSubTree to set
     */
    public void setRefSubTree(boolean isRefSubTree) {
        this.isRefSubTree = isRefSubTree;
    }

    /**
     * Getter for refNodes.
     *
     * @return the refNodes
     */
    public List<INode> getRefNodes() {
        return this.refNodes;
    }

    /**
     * Sets the refNodes.
     *
     * @param refNodes the refNodes to set
     */
    public void setRefNodes(List<INode> refNodes) {
        this.refNodes = refNodes;
    }
}
