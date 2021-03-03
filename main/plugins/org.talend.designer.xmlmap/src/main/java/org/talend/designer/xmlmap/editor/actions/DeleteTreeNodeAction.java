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
package org.talend.designer.xmlmap.editor.actions;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.gef.ui.actions.SelectionAction;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IWorkbenchPart;
import org.talend.designer.xmlmap.model.emf.xmlmap.AbstractInOutTree;
import org.talend.designer.xmlmap.model.emf.xmlmap.OutputTreeNode;
import org.talend.designer.xmlmap.model.emf.xmlmap.TreeNode;
import org.talend.designer.xmlmap.parts.TreeNodeEditPart;
import org.talend.designer.xmlmap.ui.tabs.MapperManager;
import org.talend.designer.xmlmap.util.XmlMapUtil;

/**
 * wchen class global comment. Detailled comment
 */
public class DeleteTreeNodeAction extends SelectionAction {

    private boolean input;

    private MapperManager mapperManager;

    // private List<TreeNode> nodesNeedToChangeMainStatus = new ArrayList<TreeNode>();

    public static final String ID = "org.talend.designer.xmlmap.editor.actions.DeleteTreeNodeAction";

    public DeleteTreeNodeAction(IWorkbenchPart part) {
        super(part);
        setId(ID);
        setText("Delete");
    }

    public void update(Object selection) {
        setSelection(new StructuredSelection(selection));
    }

    @Override
    protected boolean calculateEnabled() {
        // nodesNeedToChangeMainStatus.clear();
        if (getSelectedObjects().isEmpty()) {
            return false;
        } else {
            boolean enable = true;
            Object s = getSelectedObjects().get(0);
            if (s instanceof List && !((List) s).isEmpty()) {
                List selectedarts = (List) s;
                Object lastSelection = selectedarts.get(selectedarts.size() - 1);
                if (!(lastSelection instanceof TreeNodeEditPart)) {
                    return false;
                }
                for (Object obj : selectedarts) {
                    if (obj instanceof TreeNodeEditPart) {
                        TreeNodeEditPart nodePart = (TreeNodeEditPart) obj;
                        TreeNode treeNode = (TreeNode) nodePart.getModel();
                        int xPathLength = XmlMapUtil.getXPathLength(treeNode.getXpath());
                        if (xPathLength <= 2) {
                            enable = false;
                        }
                        // can't delete root
                        if (treeNode.eContainer() instanceof TreeNode
                                && XmlMapUtil.DOCUMENT.equals(((TreeNode) treeNode.eContainer()).getType())) {
                            enable = false;
                        }

                    }
                    if (!enable) {
                        return enable;
                    }
                }
            }
            return enable;
        }
    }

    @Override
    public void run() {
        try {
            TreeNode docRoot = null;
            Object s = getSelectedObjects().get(0);
            if (s instanceof List && !((List) s).isEmpty()) {
                List selectedarts = new ArrayList((List) s);
                Iterator iter = selectedarts.iterator();
                while (iter.hasNext()) {
                    Object obj = iter.next();
                    if (obj instanceof TreeNodeEditPart) {
                        TreeNodeEditPart nodePart = (TreeNodeEditPart) obj;
                        TreeNode treeNode = (TreeNode) nodePart.getModel();
                        if (treeNode.eContainer() instanceof TreeNode) {
                            TreeNode parent = (TreeNode) treeNode.eContainer();
                            if (docRoot == null) {
                                docRoot = XmlMapUtil.getTreeNodeRoot(parent);
                            }
                            XmlMapUtil.detachNodeConnections(treeNode, mapperManager.getExternalData(), true);
                            // if delete loop , clean group and main
                            if (treeNode.isLoop()) {
                                if (treeNode instanceof OutputTreeNode
                                        && XmlMapUtil.findUpGroupNode((OutputTreeNode) treeNode) != null) {
                                    XmlMapUtil.cleanSubGroup(docRoot);
                                }
                                XmlMapUtil.clearMainNode(docRoot);
                            }

                            parent.getChildren().remove(treeNode);

                            // check if tree is multiloop
                            if (docRoot != null && docRoot.eContainer() instanceof AbstractInOutTree) {
                                AbstractInOutTree tree = (AbstractInOutTree) docRoot.eContainer();
                                tree.setMultiLoops(XmlMapUtil.checkMultiLoopsStatus(tree));
                            }
                            if (input) {
                                // remove delete loops in InputLoopTable for outputs
                                List<TreeNode> subNodes = new ArrayList<TreeNode>();
                                checkSubElementIsLoop(treeNode, subNodes);
                                XmlMapUtil.removeloopInOutputTree(mapperManager, subNodes);
                            }

                        }

                    }
                }
            }

            if (mapperManager != null) {
                mapperManager.beanListModified(input);
                // if (input) {
                // mapperManager.inputTreeSchemaBeanListModified();
                // } else {
                // mapperManager.outputTreeSchemaBeanListModified();
                // }
                if (docRoot != null && docRoot.eContainer() instanceof AbstractInOutTree) {
                    mapperManager.getProblemsAnalyser().checkProblems((AbstractInOutTree) docRoot.eContainer());
                    mapperManager.getMapperUI().updateStatusBar();
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void checkSubElementIsLoop(TreeNode subTreeNode, List<TreeNode> subLoops) {
        if (subTreeNode == null) {
            return;
        }
        TreeNode e = subTreeNode;
        if (e.isLoop()) {
            subLoops.add(e);
        }
        for (TreeNode treeNode : subTreeNode.getChildren()) {
            checkSubElementIsLoop(treeNode, subLoops);
        }
    }

    public boolean isInput() {
        return input;
    }

    public void setInput(boolean input) {
        this.input = input;
    }

    public void setMapperManager(MapperManager mapperManager) {
        this.mapperManager = mapperManager;
    }
}
