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
package org.talend.designer.xmlmap.util.problem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.emf.common.util.EList;
import org.talend.core.model.process.Problem;
import org.talend.core.model.process.Problem.ProblemStatus;
import org.talend.designer.xmlmap.model.emf.xmlmap.AbstractInOutTree;
import org.talend.designer.xmlmap.model.emf.xmlmap.Connection;
import org.talend.designer.xmlmap.model.emf.xmlmap.InputLoopNodesTable;
import org.talend.designer.xmlmap.model.emf.xmlmap.InputXmlTree;
import org.talend.designer.xmlmap.model.emf.xmlmap.LookupConnection;
import org.talend.designer.xmlmap.model.emf.xmlmap.OutputTreeNode;
import org.talend.designer.xmlmap.model.emf.xmlmap.OutputXmlTree;
import org.talend.designer.xmlmap.model.emf.xmlmap.TreeNode;
import org.talend.designer.xmlmap.model.emf.xmlmap.XmlMapData;
import org.talend.designer.xmlmap.ui.tabs.MapperManager;
import org.talend.designer.xmlmap.util.XmlMapUtil;

/**
 *
 * DOC wchen class global comment. Detailled comment
 */
public class ProblemsAnalyser {

    private MapperManager mapperManager;

    private Map<AbstractInOutTree, List<Problem>> treeAndProblems = new HashMap<AbstractInOutTree, List<Problem>>();

    private static final String ERROR_MESSAGE_START = "Require loop element for Document :";

    boolean isMultipleDocType = false;

    private static final String ERROR_MESSAGE_MULTIPLE_DOC_TYPE = "Multiple document type in the same schema :";

    /**
     * DOC wchen ProblemsAnalyser constructor comment.
     */
    public ProblemsAnalyser(MapperManager mapperManager) {
        super();
        this.mapperManager = mapperManager;
    }

    public List<Problem> checkProblems() {
        treeAndProblems.clear();

        final XmlMapData copyOfMapData = mapperManager.getExternalData();
        if (copyOfMapData != null) {

            // check problems for InputLoopTable in output
            InputXmlTree mainInputTree = mapperManager.getMainInputTree();
            for (OutputXmlTree outputTree : copyOfMapData.getOutputTrees()) {
                checkInputLoopTablesProblem(outputTree, mainInputTree);
            }

            for (InputXmlTree inputTree : copyOfMapData.getInputTrees()) {
                isMultipleDocType = false;
                checkTreeNodesProblem(inputTree, inputTree.getNodes());
            }
            for (OutputXmlTree outputTree : copyOfMapData.getOutputTrees()) {
                isMultipleDocType = false;
                checkTreeNodesProblem(outputTree, outputTree.getNodes());
            }

        }

        return getProblems();
    }

    private void checkInputLoopTablesProblem(OutputXmlTree outputTree, InputXmlTree mainInputTree) {
        if (mainInputTree == null) {
            return;
        }
        if (mainInputTree.isMultiLoops()) {
            if (!XmlMapUtil.hasDocument(outputTree)) {
                if (outputTree.getInputLoopNodesTables().isEmpty()
                        || outputTree.getInputLoopNodesTables().get(0).getInputloopnodes().isEmpty()) {
                    String message = outputTree.getName() + " have no source loop";
                    addProblem(outputTree, new Problem(null, message, ProblemStatus.ERROR));
                }
            } else {
                List<TreeNode> loopNodes = new ArrayList<TreeNode>();
                XmlMapUtil.getChildLoops(loopNodes, outputTree.getNodes());
                if (!loopNodes.isEmpty()) {
                    for (TreeNode node : loopNodes) {
                        InputLoopNodesTable inutLoopTable = ((OutputTreeNode) node).getInputLoopNodesTable();
                        if (inutLoopTable == null || inutLoopTable.getInputloopnodes().isEmpty()) {
                            String message = node.getXpath() + " have no source loop";
                            addProblem(outputTree, new Problem(null, message, ProblemStatus.ERROR));
                        }

                    }

                }

            }
        }

    }

    private boolean checkNodeInputLookTableProblem(OutputTreeNode outputNode, InputXmlTree mainInputTree, boolean checkChildren) {
        for (Connection connection : outputNode.getIncomingConnections()) {
            if (connection.getSource() instanceof TreeNode) {
                TreeNode source = (TreeNode) connection.getSource();
                InputXmlTree abstractInOutTree = (InputXmlTree) XmlMapUtil.getAbstractInOutTree(source);
                if (abstractInOutTree == mainInputTree) {
                    return true;
                } else {
                    EList<LookupConnection> lookupIncomingConnections = source.getLookupIncomingConnections();
                    for (LookupConnection lookupConn : lookupIncomingConnections) {
                        TreeNode sourceNode = (TreeNode) lookupConn.getSource();
                        AbstractInOutTree abstractInOutTree2 = XmlMapUtil.getAbstractInOutTree(sourceNode);
                        if (abstractInOutTree2 == mainInputTree) {
                            return true;
                        }
                    }
                }

                if (checkChildren && !outputNode.getChildren().isEmpty()) {
                    for (TreeNode child : outputNode.getChildren()) {
                        if (checkNodeInputLookTableProblem((OutputTreeNode) child, mainInputTree, checkChildren)) {
                            return true;
                        }
                    }
                }

            }
        }
        return false;
    }

    public void checkProblems(AbstractInOutTree abstractTree) {
        // clear problems for the tree before recheck it
        if (treeAndProblems.get(abstractTree) != null) {
            treeAndProblems.get(abstractTree).clear();
        }
        // check problems for InputLoopTable in output
        final XmlMapData copyOfMapData = mapperManager.getExternalData();
        // check problems for InputLoopTable in output
        InputXmlTree mainInputTree = mapperManager.getMainInputTree();
        if (abstractTree instanceof OutputXmlTree) {
            checkInputLoopTablesProblem((OutputXmlTree) abstractTree, mainInputTree);
        }

        isMultipleDocType = false;
        List<? extends TreeNode> nodes = null;
        if (abstractTree instanceof InputXmlTree) {
            nodes = ((InputXmlTree) abstractTree).getNodes();
        } else if (abstractTree instanceof OutputXmlTree) {
            nodes = ((OutputXmlTree) abstractTree).getNodes();
        }
        if (nodes != null) {
            checkTreeNodesProblem(abstractTree, nodes);
        }

        if (treeAndProblems.get(abstractTree) == null || treeAndProblems.get(abstractTree).isEmpty()) {
            treeAndProblems.remove(abstractTree);
        }
    }

    private void checkTreeNodesProblem(AbstractInOutTree treeToCheck, List<? extends TreeNode> treeNodes) {
        for (TreeNode treeNode : treeNodes) {
            if (XmlMapUtil.DOCUMENT.equals(treeNode.getType())) {
                if (!hasDocumentLoop(treeNode)) {
                    String message = ERROR_MESSAGE_START + treeNode.getXpath();
                    addProblem(treeToCheck, new Problem(null, message, ProblemStatus.ERROR));
                }
                if (!isMultipleDocType) {
                    isMultipleDocType = true;
                } else {
                    String message = ERROR_MESSAGE_MULTIPLE_DOC_TYPE + treeNode.getXpath();
                    addProblem(treeToCheck, new Problem(null, message, ProblemStatus.ERROR));
                }
            }
        }
    }

    private boolean hasDocumentLoop(TreeNode treeNode) {
        if (treeNode.isLoop()) {
            return true;
        } else {
            if (!treeNode.getChildren().isEmpty()) {
                for (TreeNode child : treeNode.getChildren()) {
                    if (hasDocumentLoop(child)) {
                        return true;
                    }
                }
            } else {
                return false;
            }
        }
        return false;
    }

    public List<Problem> getProblems() {
        List<Problem> problemsList = new ArrayList<Problem>();
        for (List<Problem> problems : treeAndProblems.values()) {
            problemsList.addAll(problems);
        }
        return new ArrayList<Problem>(problemsList);
    }

    private void addProblem(AbstractInOutTree tree, Problem problem) {
        if (problem != null) {
            List<Problem> list = treeAndProblems.get(tree);
            if (list == null) {
                list = new ArrayList<Problem>();
                list.add(problem);
                treeAndProblems.put(tree, list);
            } else {
                list.add(problem);
            }
        }
    }

    public void clearProblemsForTree(AbstractInOutTree tree) {
        if (treeAndProblems.containsKey(tree)) {
            treeAndProblems.get(tree).clear();
        }
    }

    public String getErrorMessage() {
        String errorMessage = "";
        final List<Problem> problems = getProblems();
        if (problems.isEmpty()) {
            return null;
        } else {
            for (Problem problem : problems) {
                final String description = problem.getDescription();
                if (description != null) {
                    errorMessage = errorMessage + description + ",";
                }
            }
        }

        return errorMessage.substring(0, errorMessage.length() - 1);
    }

}
