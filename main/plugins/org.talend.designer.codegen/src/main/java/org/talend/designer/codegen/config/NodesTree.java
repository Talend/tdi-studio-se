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
package org.talend.designer.codegen.config;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.talend.core.model.process.AbstractNode;
import org.talend.core.model.process.INode;
import org.talend.core.model.process.IProcess;
import org.talend.core.model.temp.ETypeGen;

/**
 * A NodesTree is the Code Gerator Implementation of a process. A NodesTree is built using the Nodes of the Process.
 * It's made of a list of NodesSubTree.
 *
 * $Id$
 */
public class NodesTree {

    private List<NodesSubTree> subTrees;

    private List<? extends INode> nodes;

    private List<INode> rootNodes;

    /**
     * Constuctor for NodesTree. Note: the param init=false, when it is called in generateComponentCodeWithRows().
     *
     * @param List of Available Nodes in this tree.
     * @param execute init method or not
     */
    public NodesTree(IProcess process, List<? extends INode> treeNodes, boolean init) {
        this.nodes = treeNodes;
        buildRootNodes(process);
        if (init) {
            buildSubTrees(true);
        }
    }

    public NodesTree(IProcess process, List<? extends INode> treeNodes, boolean init, ETypeGen typeGen) {
        this.nodes = treeNodes;
        buildRootNodes(process);
        if (init) {
            if (typeGen == ETypeGen.CAMEL) {
                buildCamelSubTrees(true);
            } else if (typeGen == ETypeGen.MR || typeGen == ETypeGen.STORM) {
                buildBigDataSubTrees(typeGen);
            } else if (typeGen == ETypeGen.SPARK) {
                buildSparkSubTrees(typeGen);
            }
        }
    }

    /**
     * Build SubTrees List. Note: the param init=false, when it is called in generateComponentCodeWithRows().
     */

    public void buildCamelSubTrees(boolean init) {
        subTrees = new ArrayList<NodesSubTree>();
        for (INode node : nodes) {
            if (((node.isStart()) && (node.isActivate())) || (rootNodes.contains(node))) {

                subTrees.add(new NodesSubTree(node, nodes, ETypeGen.CAMEL));

            }
        }
    }

    public void buildSubTrees(boolean init) {
        subTrees = new ArrayList<NodesSubTree>();
        for (INode node : nodes) {
            if (((node.isSubProcessStart()) && (node.isActivate())) || (rootNodes.contains(node))) {

                // need to unite the merge branches to one subStree
                if (node.isThereLinkWithMerge() && init) {
                    Map<INode, Integer> mergeInfo = node.getLinkedMergeInfo();
                    if (mergeInfo != null && mergeInfo.values().toArray()[0].equals(1)) {
                        // add the first merge branch
                        subTrees.add(new NodesSubTree(node, nodes));
                    }

                } else {

                    subTrees.add(new NodesSubTree(node, nodes));
                }

            }
        }
    }

    private void buildBigDataSubTrees(ETypeGen typeGen) {
        subTrees = new ArrayList<NodesSubTree>();
        for (INode node : nodes) {
            if (((node.isSubProcessStart()) && (node.isActivate()) && !((AbstractNode) node).isRefNode())
                    || (rootNodes.contains(node))) {
                subTrees.add(new NodesSubTree(node, nodes, typeGen));
            }
        }
    }

    private void buildSparkSubTrees(ETypeGen typeGen) {
        subTrees = new ArrayList<NodesSubTree>();
        for (INode node : nodes) {
            if (((node == node.getSubProcessStartNode(false))
                    && (node.getDesignSubjobStartNode() == null || node == node.getDesignSubjobStartNode())
                    && (node.isActivate()) && !((AbstractNode) node).isThereLinkWithHash())) {
                subTrees.add(new NodesSubTree(node, nodes, typeGen));
            }
        }
    }

    /**
     * Build Root Nodes List.
     *
     * @return
     */
    @SuppressWarnings("unchecked")
    public void buildRootNodes(IProcess process) {
        rootNodes = new ArrayList<INode>();

        List<INode> preJobsNode = (List<INode>) process.getNodesOfType("tPrejob"); //$NON-NLS-1$
        for (INode node : preJobsNode) {
            if (node.isActivate()) {
                rootNodes.add(node);
            }
        }

        for (INode node : nodes) {
            String componentName = node.getComponent().getName();
            if ((node.isStart()) && (node.isActivate()) && (node.isSubtreeStart() || node.isELTComponent())
                    && (!componentName.equals("tPrejob"))//$NON-NLS-1$
                    && (!componentName.equals("tPostjob"))) { //$NON-NLS-1$
                rootNodes.add(node);
            }
        }

        List<INode> postJobsNode = (List<INode>) process.getNodesOfType("tPostjob"); //$NON-NLS-1$
        for (INode node : postJobsNode) {
            if (node.isActivate()) {
                rootNodes.add(node);
            }
        }
    }

    /**
     * Getter for RootNodes.
     *
     * @return
     */
    public List<INode> getRootNodes() {
        return rootNodes;
    }

    public void addRootNode(INode node) {
        if (rootNodes != null) {
            rootNodes.add(node);
        }
    }

    /**
     * Getter for subTrees.
     *
     * @return the subTrees
     */
    public List<NodesSubTree> getSubTrees() {
        return this.subTrees;
    }
}
