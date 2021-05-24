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

import org.eclipse.emf.ecore.EObject;
import org.eclipse.jface.window.Window;
import org.talend.commons.ui.gmf.util.DisplayUtils;
import org.talend.designer.xmlmap.model.emf.xmlmap.AbstractInOutTree;
import org.talend.designer.xmlmap.model.emf.xmlmap.AbstractNode;
import org.talend.designer.xmlmap.model.emf.xmlmap.Connection;
import org.talend.designer.xmlmap.model.emf.xmlmap.FilterConnection;
import org.talend.designer.xmlmap.model.emf.xmlmap.InputXmlTree;
import org.talend.designer.xmlmap.model.emf.xmlmap.LookupConnection;
import org.talend.designer.xmlmap.model.emf.xmlmap.OutputTreeNode;
import org.talend.designer.xmlmap.model.emf.xmlmap.OutputXmlTree;
import org.talend.designer.xmlmap.model.emf.xmlmap.TreeNode;
import org.talend.designer.xmlmap.model.emf.xmlmap.VarNode;
import org.talend.designer.xmlmap.model.emf.xmlmap.XmlMapData;
import org.talend.designer.xmlmap.model.emf.xmlmap.XmlmapFactory;
import org.talend.designer.xmlmap.ui.dialog.PrefixChangeDialog;
import org.talend.designer.xmlmap.ui.expressionutil.TableEntryLocation;
import org.talend.designer.xmlmap.ui.expressionutil.XmlMapExpressionManager;

/**
 *
 * DOC wchen Utils class to deal with connections inside xmlmap ui
 */
public class XmlMapConnectionBuilder {

    private boolean applyForAll = false;

    private boolean cancelForAll = false;

    private boolean checkRootNodePrefix = false;

    public void rebuildLinks(TreeNode docNode, XmlMapData mapData) {
        if (docNode == null || mapData == null) {
            return;
        }
        AbstractInOutTree inputTree = XmlMapUtil.getAbstractInOutTree(docNode);

        int index = mapData.getInputTrees().indexOf(inputTree);
        if (index == -1) {
            index = 0;
        }
        index = index + 1;
        List<TreeNode> nodes = new ArrayList<TreeNode>();
        nodes.add(docNode);
        rebuildLink(index, nodes, mapData);
    }

    private void rebuildLink(int inputTreeIndex, List<TreeNode> children, XmlMapData mapData) {
        for (TreeNode treeNode : children) {
            if (XmlMapUtil.isDragable(treeNode)) {
                String expression = XmlMapUtil.convertToExpression(treeNode.getXpath());
                XmlMapExpressionManager expressionManager = new XmlMapExpressionManager();
                TableEntryLocation sourceLocation = expressionManager.parseTableEntryLocation(expression).get(0);
                // LOOKUP ,FILTER
                for (int i = inputTreeIndex; i < mapData.getInputTrees().size(); i++) {
                    InputXmlTree treeTarget = mapData.getInputTrees().get(i);
                    if (hasMaptchedLocation(expressionManager, sourceLocation, treeTarget, ExpressionType.EXPRESSION_FILTER)) {
                        createFilterConnection(treeNode, treeTarget, mapData);
                    }
                    checkTargetChildren(expressionManager, treeTarget.getNodes(), treeNode, sourceLocation, mapData);
                }
                // VAR
                for (VarNode varNode : mapData.getVarTables().get(0).getNodes()) {
                    if (hasMaptchedLocation(expressionManager, sourceLocation, varNode, ExpressionType.EXPRESSION)) {
                        createConnection(treeNode, varNode, mapData);
                    }
                }
                // OUTPUT,FILTER
                for (int i = 0; i < mapData.getOutputTrees().size(); i++) {
                    OutputXmlTree outputTree = mapData.getOutputTrees().get(i);
                    if (hasMaptchedLocation(expressionManager, sourceLocation, outputTree, ExpressionType.EXPRESSION_FILTER)) {
                        createFilterConnection(treeNode, outputTree, mapData);
                    }
                    checkTargetChildren(expressionManager, outputTree.getNodes(), treeNode, sourceLocation, mapData);
                }
            }
            if (!treeNode.getChildren().isEmpty()) {
                rebuildLink(inputTreeIndex, treeNode.getChildren(), mapData);
            }
        }
    }

    private void checkTargetChildren(XmlMapExpressionManager expressionManager, List<? extends TreeNode> children,
            TreeNode sourceNode, TableEntryLocation sourceLocation, XmlMapData mapData) {
        for (TreeNode targetNode : children) {
            if (XmlMapUtil.isExpressionEditable(targetNode)) {
                if (hasMaptchedLocation(expressionManager, sourceLocation, targetNode, ExpressionType.EXPRESSION)) {
                    if (targetNode instanceof OutputTreeNode) {
                        createConnection(sourceNode, targetNode, mapData);
                    } else {
                        createLookupConnection(sourceNode, targetNode, mapData);
                    }
                }
            }
            if (!targetNode.getChildren().isEmpty()) {
                checkTargetChildren(expressionManager, targetNode.getChildren(), sourceNode, sourceLocation, mapData);
            }
        }
    }

    private boolean hasMaptchedLocation(XmlMapExpressionManager expressionManager, TableEntryLocation sourceLocation,
            EObject targetNodeOrTree, ExpressionType type) {
        String targetExpression = null;
        AbstractNode targetNode = null;
        AbstractInOutTree targetTree = null;
        switch (type) {
        case EXPRESSION:
            targetNode = (AbstractNode) targetNodeOrTree;
            targetExpression = targetNode.getExpression();
            break;
        case EXPRESSION_FILTER:
            targetTree = (AbstractInOutTree) targetNodeOrTree;
            targetExpression = targetTree.getExpressionFilter();
        default:
            break;
        }
        if (!"".equals(targetExpression) && targetExpression != null) {
            List<TableEntryLocation> targetLocations = expressionManager.parseTableEntryLocation(targetExpression);
            for (TableEntryLocation target : targetLocations) {
                if (sourceLocation.equals(target)) {
                    return true;
                } else if (checkRootNodePrefix && !cancelForAll) {
                    StringBuffer bf = new StringBuffer();
                    String prefix = null;
                    String nodeName = null;
                    final String[] split = sourceLocation.toString().split("/");
                    if (split.length > 2) {
                        for (int i = 0; i < split.length; i++) {
                            String value = split[i];
                            if (i == 1) {
                                int indexOf = split[1].indexOf(":");
                                if (indexOf != -1) {
                                    prefix = split[1].substring(0, indexOf);
                                    nodeName = split[1].substring(indexOf + 1, split[1].length());
                                    value = nodeName;
                                } else {
                                    break;
                                }
                            }
                            bf.append(value);
                            if (i < split.length - 1) {
                                bf.append("/");
                            }
                        }
                    }
                    if (bf.toString().equals(targetExpression)) {
                        if (applyForAll) {
                            // reset the target expression with prefix
                            if (targetNode != null) {
                                targetNode.setExpression(
                                        expressionManager.replaceExpression(targetExpression, target, sourceLocation));
                            }
                            if (targetTree != null) {
                                targetTree.setExpressionFilter(
                                        expressionManager.replaceExpression(targetExpression, target, sourceLocation));
                            }
                            return true;
                        } else {
                            PrefixChangeDialog dialog = new PrefixChangeDialog(DisplayUtils.getDefaultShell(false));
                            dialog.setPrefix(prefix);
                            dialog.setRootNodeName(nodeName);
                            dialog.setSourceExpression(sourceLocation.toString());
                            dialog.setTargetExpression(target.toString());
                            if (dialog.open() == Window.OK) {
                                applyForAll = dialog.isApplyAll();
                                cancelForAll = dialog.isCancelAll();
                                if (cancelForAll) {
                                    return false;
                                }
                                // reset the target expression with prefix
                                if (targetNode != null) {
                                    targetNode.setExpression(
                                            expressionManager.replaceExpression(targetExpression, target, sourceLocation));
                                }
                                if (targetTree != null) {
                                    targetTree.setExpressionFilter(
                                            expressionManager.replaceExpression(targetExpression, target, sourceLocation));
                                }
                                return true;
                            }
                        }
                    }
                }
            }
        }
        return false;
    }

    public void createConnection(AbstractNode sourceNode, AbstractNode targetNode, XmlMapData mapData) {
        Connection conn = XmlmapFactory.eINSTANCE.createConnection();
        conn.setSource(sourceNode);
        conn.setTarget(targetNode);
        targetNode.getIncomingConnections().add(conn);
        sourceNode.getOutgoingConnections().add(conn);
        mapData.getConnections().add(conn);
    }

    public void createLookupConnection(TreeNode sourceNode, TreeNode targetNode, XmlMapData mapData) {
        LookupConnection conn = XmlmapFactory.eINSTANCE.createLookupConnection();
        conn.setSource(sourceNode);
        conn.setTarget(targetNode);
        targetNode.getLookupIncomingConnections().add(conn);
        sourceNode.getLookupOutgoingConnections().add(conn);
        mapData.getConnections().add(conn);
    }

    public void createFilterConnection(AbstractNode sourceNode, AbstractInOutTree targetTree, XmlMapData mapData) {
        FilterConnection connection = XmlmapFactory.eINSTANCE.createFilterConnection();
        connection.setSource(sourceNode);
        connection.setTarget(targetTree);
        targetTree.getFilterIncomingConnections().add(connection);
        sourceNode.getFilterOutGoingConnections().add(connection);
        mapData.getConnections().add(connection);
    }

    /**
     * Sets the checkRootNodePrefix.
     *
     * @param checkRootNodePrefix the checkRootNodePrefix to set
     */
    public void setCheckRootNodePrefix(boolean checkRootNodePrefix) {
        this.checkRootNodePrefix = checkRootNodePrefix;
    }

    enum ExpressionType {
        EXPRESSION,
        EXPRESSION_FILTER;
    }

}
