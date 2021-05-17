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
package org.talend.designer.xmlmap.figures.treeNode;

import org.talend.designer.gefabstractmap.figures.ExpressionFigure;
import org.talend.designer.gefabstractmap.figures.table.entity.TableEntityElement;
import org.talend.designer.gefabstractmap.figures.table.entity.TableTreeEntityFigure;
import org.talend.designer.gefabstractmap.figures.table.entity.TreeBranch;
import org.talend.designer.gefabstractmap.resource.ColorInfo;
import org.talend.designer.gefabstractmap.resource.ColorProviderMapper;
import org.talend.designer.xmlmap.model.emf.xmlmap.InputXmlTree;
import org.talend.designer.xmlmap.model.emf.xmlmap.OutputTreeNode;
import org.talend.designer.xmlmap.model.emf.xmlmap.TreeNode;
import org.talend.designer.xmlmap.util.XmlMapUtil;

/**
 * DOC hshen class global comment. Detailled comment
 */
public class XmlmapTreeNodeFigure extends TableTreeEntityFigure {

    private ExpressionFigure expression;

    private XmlmapBranchContent branchContent;

    private TreeNode treeNode;

    public XmlmapTreeNodeFigure(TreeNodeEntityManager entityManager, boolean isRoot) {
        super(entityManager, isRoot);
    }

    @Override
    protected TreeNodeEntityManager getEntityManager() {
        return (TreeNodeEntityManager) super.getEntityManager();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.newabstractmap.figures.table.entity.TableEntityFigure#createEntityItems()
     */
    @Override
    protected void createEntityItems(TableEntityElement entityElement) {
        this.treeNode = getEntityManager().getModel();

        boolean isLookup = false;
        if (treeNode instanceof OutputTreeNode) {
            isLookup = true;
        } else {
            TreeNode inputTreeNodeRoot = XmlMapUtil.getTreeNodeRoot(treeNode);
            if (inputTreeNodeRoot != null && inputTreeNodeRoot.eContainer() instanceof InputXmlTree) {
                isLookup = ((InputXmlTree) inputTreeNodeRoot.eContainer()).isLookup();
            }
        }

        if (isLookup) {
            expression = new ExpressionFigure();
            expression.setText(treeNode.getExpression());
            if (!XmlMapUtil.isExpressionEditable(treeNode)) {
                expression.setOpaque(true);
                expression.setBackgroundColor(ColorProviderMapper.getColor(ColorInfo.COLOR_EXPREESION_DISABLE));
            } else {
                expression.setOpaque(false);
            }

            entityElement.add(expression);
        }

        branchContent = new XmlmapBranchContent(getEntityManager());

        TreeBranch treeBranch = new TreeBranch(branchContent, !isRoot());
        entityElement.add(treeBranch);
    }

    public ExpressionFigure getExpressionFigure() {
        return this.expression;

    }

    public XmlmapBranchContent getBranchContent() {
        return this.branchContent;
    }

    public void updateExpression() {
        if (expression != null) {
            // TDI-18185
            if (XmlMapUtil.DOCUMENT.equals(treeNode.getType())) {
                expression.setText("");
            } else {
                expression.setText(treeNode.getExpression());
            }
        }
    }

    public TreeNode getTreeNode() {
        return this.treeNode;
    }
}
