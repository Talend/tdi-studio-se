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
package org.talend.designer.xmlmap.ui.tabs.table;

import org.talend.designer.xmlmap.model.emf.xmlmap.TreeNode;

/**
 * wchen class global comment. Detailled comment
 */
public class TreeSchemaTableEntry {

    private TreeNode treeNode;

    public TreeSchemaTableEntry(TreeNode treeNode) {
        this.treeNode = treeNode;
    }

    public void setTreeNode(TreeNode treeNode) {
        this.treeNode = treeNode;
    }

    public String getXPath() {
        return treeNode.getXpath();
    }

    public void setXPath(String xPath) {
        this.treeNode.setXpath(xPath);
    }

    public boolean isKey() {
        return treeNode.isKey();
    }

    public String getType() {
        return treeNode.getType();
    }

    public boolean isNullable() {
        return treeNode.isNullable();
    }

    public String getPattern() {
        return treeNode.getPattern();
    }

    public void setKey(boolean key) {
        this.treeNode.setKey(key);
    }

    public void setType(String type) {
        this.treeNode.setType(type);
    }

    public void setNullable(boolean isNullable) {
        this.treeNode.setNullable(isNullable);
    }

    public void setPattern(String pattern) {
        this.treeNode.setPattern(pattern);
    }

    public String getName() {
        return this.treeNode.getName();
    }

    public void setName(String name) {
        this.treeNode.setName(name);
    }

    public TreeNode getTreeNode() {
        return this.treeNode;
    }

}
