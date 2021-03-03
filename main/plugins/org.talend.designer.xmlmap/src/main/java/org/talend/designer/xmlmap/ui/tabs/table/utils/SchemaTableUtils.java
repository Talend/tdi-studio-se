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
package org.talend.designer.xmlmap.ui.tabs.table.utils;

import java.util.ArrayList;
import java.util.List;

import org.talend.designer.xmlmap.model.emf.xmlmap.TreeNode;

/**
 * created by Administrator on 2013-1-28 Detailled comment
 *
 */
public class SchemaTableUtils {

    public static List<TreeNode> getTreeSchemaEnties(TreeNode docNode) {
        List<TreeNode> tableModel = new ArrayList<TreeNode>();
        for (TreeNode node : docNode.getChildren()) {
            // avoid to edit choice and subs in schema editor
            if (!node.isChoice() && !node.isSubstitution()) {
                tableModel.add(node);
            }
            if (!node.getChildren().isEmpty()) {
                tableModel.addAll(getTreeSchemaEnties(node));
            }
        }
        return tableModel;

    }

}
