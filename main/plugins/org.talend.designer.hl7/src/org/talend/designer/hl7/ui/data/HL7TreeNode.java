// ============================================================================
//
// Copyright (C) 2006-2017 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.designer.hl7.ui.data;

import java.util.ArrayList;
import java.util.List;

import org.talend.core.model.metadata.IMetadataColumn;
import org.talend.core.model.metadata.IMetadataTable;

/**
 * DOC hwang class global comment. Detailled comment
 */
public abstract class HL7TreeNode {

    private IMetadataTable table = null;

    private IMetadataColumn column = null;

    private String row = null;

    private String label;

    private String defaultValue;

    private List<HL7TreeNode> children = null;

    private HL7TreeNode parent = null;

    private boolean isRepetableNode = false;

    private boolean isGroupNode = false;

    private boolean isMainNode = false;

    private int order;

    private boolean isAttribute = false;

    private boolean isNameSpace = false;

    private String columnName = null;

    public boolean hasLink() {
        return column != null;
    }

    /**
     * HL7TreeNode constructor comment.
     */
    public HL7TreeNode() {
        children = new ArrayList<HL7TreeNode>();
    }

    /**
     * HL7TreeNode constructor comment.
     */
    public HL7TreeNode(String label) {
        children = new ArrayList<HL7TreeNode>();
        this.label = label;
    }

    /**
     * HL7TreeNode constructor comment.
     */
    public HL7TreeNode(String label, String defaultValue) {
        children = new ArrayList<HL7TreeNode>();
        this.label = label;
        this.defaultValue = defaultValue;
    }

    /**
     * Getter for children.
     *
     * @return the children
     */
    public List<HL7TreeNode> getChildren() {
        return this.children;
    }

    /**
     * Getter for parent.
     *
     * @return the parent
     */
    public HL7TreeNode getParent() {
        return this.parent;
    }

    /**
     * Sets the parent.
     *
     * @param parent the parent to set
     */
    public void setParent(HL7TreeNode parent) {
        this.parent = parent;
    }

    /**
     * DOC ke Comment method "getColumnLabel".
     *
     * @return
     */
    public String getColumnLabel() {
        if (column != null) {
            if (getRow() != null) {
                return getRow() + ":" + this.column.getLabel(); //$NON-NLS-1$
            }
            return this.column.getLabel();
        } else if (columnName != null) {
            if (getRow() != null) {
                return getRow() + ":" + this.getColumnName();
            }
            return this.getColumnName();

        } else {
            if (getRow() != null) {
                return getRow();
            }
            return ""; //$NON-NLS-1$
        }

        // if (column == null) {
        // if (getRow() != null) {
        // return getRow();
        // }
        //            return ""; //$NON-NLS-1$
        // } else {
        // if (getRow() != null) {
        //                return getRow() + ":" + this.column.getLabel(); //$NON-NLS-1$
        // }
        // return this.column.getLabel();
        // }
    }

    /**
     * Getter for schema.
     *
     * @return the schema
     */
    public IMetadataColumn getColumn() {
        return column;
    }

    /**
     * Sets the schema.
     *
     * @param schema the schema to set
     */
    public void setColumn(IMetadataColumn column) {
        this.column = column;
    }

    /**
     * Getter for value.
     *
     * @return the value
     */
    public String getLabel() {
        return this.label;
    }

    /**
     * Getter for value.
     *
     * @return the value
     */
    public String getLabelForViewer() {
        return this.label;
    }

    /**
     * Getter for defaultValue.
     *
     * @return the value
     */
    public String getDefaultValue() {
        return this.defaultValue;
    }

    /**
     * Sets the defaultValue.
     *
     * @param value the defaultValue to set
     */
    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    /**
     * Sets the value.
     *
     * @param value the value to set
     */
    public void setLabel(String label) {
        this.label = label;
    }

    /**
     * DOC ke Comment method "addChild".
     *
     * @param child
     */
    public void addChild(HL7TreeNode child) {
        children.add(child);
        child.setParent(this);
    }

    /**
     *
     * hwang Comment method "addChild".
     *
     * @param index
     * @param child
     */
    public void addChild(int index, HL7TreeNode child) {
        if (index < children.size()) {
            children.add(index, child);
        } else {
            children.add(child);
        }
        child.setParent(this);

    }

    /**
     * DOC ke Comment method "removeChild".
     *
     * @param child
     */
    public void removeChild(HL7TreeNode child) {
        children.remove(child);
        child.setParent(null);
    }

    /**
     * DOC ke Comment method "hasChildren".
     *
     * @return
     */
    public boolean hasChildren() {
        return children.size() > 0;
    }

    public boolean isRepetable() {
        return this.isRepetableNode;
    }

    public void setRepetable(boolean b) {
        this.isRepetableNode = b;
    }

    /**
     * DOC ke Comment method "isGroup".
     *
     * @return
     */
    public boolean isGroup() {
        return this.isGroupNode;
    }

    /**
     * DOC ke Comment method "setGroup".
     *
     * @param b
     */
    public void setGroup(boolean b) {
        this.isGroupNode = b;
    }

    /**
     * Getter for isMainNode.
     *
     * @return the isMainNode
     */
    public boolean isMain() {
        return this.isMainNode;
    }

    /**
     * Sets the isMainNode.
     *
     * @param isMainNode the isMainNode to set
     */
    public void setMain(boolean isMainNode) {
        this.isMainNode = isMainNode;
    }

    /**
     *
     * hwang Comment method "getTable".
     *
     * @return
     */
    public IMetadataTable getTable() {
        return this.table;
    }

    /**
     *
     * hwang Comment method "setTable".
     *
     * @param table
     */
    public void setTable(IMetadataTable table) {
        this.table = table;
    }

    /**
     *
     * hwang Comment method "getRow".
     *
     * @return
     */
    public String getRow() {
        return this.row;
    }

    /**
     *
     * hwang Comment method "setRow".
     *
     * @param row
     */
    public void setRow(String row) {
        this.row = row;
    }

    /**
     * hwang Comment method "getOrder".
     *
     * @return
     */
    public int getOrder() {
        return this.order;
    }

    /**
     * hwang Comment method "setOrder".
     *
     * @param order
     */
    public void setOrder(int order) {
        this.order = order;
    }

    public boolean isAttribute() {
        return this.isAttribute;
    }

    public void setAttribute(boolean isAttribute) {
        this.isAttribute = isAttribute;
    }

    public boolean isNameSpace() {
        return this.isNameSpace;
    }

    public void setNameSpace(boolean isNameSpace) {
        this.isNameSpace = isNameSpace;
    }

    /**
     * Getter for columnName.
     *
     * @return the columnName
     */
    public String getColumnName() {
        return this.columnName;
    }

    /**
     * Sets the columnName.
     *
     * @param columnName the columnName to set
     */
    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }
}
