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
package org.talend.designer.core.ui.editor.dependencies.model;

import java.util.ArrayList;
import java.util.List;

public class JobContextTreeNode implements Comparable<JobContextTreeNode> {


    private Object treeData;

    private final JobContextTreeNode parent;

    private final List<JobContextTreeNode> children = new ArrayList<JobContextTreeNode>();

    private final int orderId;

    private String name;

    public JobContextTreeNode(int orderId, Object data, JobContextTreeNode parent,
            String name) {
        this.orderId = orderId;
        this.treeData = data;
        this.parent = parent;
        if (parent != null) {
            parent.addChild(this);
        }

        this.name = name;
    }

    public JobContextTreeNode getParent() {
        return parent;
    }

    public Object getTreeData() {
        return treeData;
    }

    public void addChild(JobContextTreeNode child) {
        children.add(child);
    }

    public List<JobContextTreeNode> getChildren() {
        return children;
    }

    public JobContextTreeNode getSelf() {
        return this;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getOrderId() {
        return this.orderId;
    }

    /**
     * Comparison is based on name only
     */
    @Override
    public int compareTo(JobContextTreeNode o) {
        if (this.orderId > o.orderId) {
            return 1;
        } else if (this.orderId < o.orderId) {
            return -1;
        } else {
            return 0;
        }
    }
}
