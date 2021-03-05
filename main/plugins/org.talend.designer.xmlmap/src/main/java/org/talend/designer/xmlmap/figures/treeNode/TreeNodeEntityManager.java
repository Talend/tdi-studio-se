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

import org.talend.designer.gefabstractmap.figures.manager.TableEntityManager;
import org.talend.designer.xmlmap.model.emf.xmlmap.AbstractInOutTree;
import org.talend.designer.xmlmap.model.emf.xmlmap.TreeNode;
import org.talend.designer.xmlmap.parts.TreeNodeEditPart;

/**
 * DOC wchen class global comment. Detailled comment
 */
public class TreeNodeEntityManager extends TableEntityManager {

    public TreeNodeEntityManager(TreeNode treeNode, TreeNodeEditPart nodePart) {
        super(treeNode, nodePart);
    }

    @Override
    public TreeNode getModel() {
        return (TreeNode) super.getModel();
    }

    @Override
    public AbstractInOutTree getMapperTable() {
        return (AbstractInOutTree) super.getMapperTable();
    }

    @Override
    public boolean isTableMinimized() {
        return getMapperTable().isMinimized();
    }

}
