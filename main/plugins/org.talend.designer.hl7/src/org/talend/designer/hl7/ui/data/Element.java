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

/**
 * DOC hwang class global comment. Detailled comment
 */
public class Element extends HL7TreeNode {

    private List<Attribute> attributes = new ArrayList<Attribute>();

    private List<NameSpaceNode> nameSpaces = new ArrayList<NameSpaceNode>();

    /**
     * Element constructor comment.
     */
    public Element() {
        super();
    }

    /**
     * Element constructor comment.
     */
    public Element(String label) {
        super(label);
    }

    public Element(String label, String defauleValue) {
        super(label, defauleValue);
    }

    public void removeChild(HL7TreeNode child) {
        child.setParent(null);
        if (child instanceof Attribute) {
            Attribute element = (Attribute) child;
            attributes.remove(element);
            return;
        }
        if (child instanceof NameSpaceNode) {
            NameSpaceNode element = (NameSpaceNode) child;
            nameSpaces.remove(element);
            return;
        }
        super.removeChild(child);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.designer.fileoutputxml.data.HL7TreeNode#hasChildren()
     */
    @Override
    public boolean hasChildren() {
        return getChildren().size() + attributes.size() > 0;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.talend.designer.fileoutputxml.data.HL7TreeNode#addChild(org.talend.designer.fileoutputxml.data.HL7TreeNode)
     */
    @Override
    public void addChild(HL7TreeNode child) {
        if (child instanceof Attribute) {
            Attribute element = (Attribute) child;
            attributes.add(element);
            child.setParent(this);
            return;
        }
        if (child instanceof NameSpaceNode) {
            NameSpaceNode element = (NameSpaceNode) child;
            nameSpaces.add(element);
            child.setParent(this);
            return;
        }
        super.addChild(child);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.designer.fileoutputxml.data.HL7TreeNode#getChildren()
     */
    @Override
    public List<HL7TreeNode> getChildren() {
        List<HL7TreeNode> list = new ArrayList<HL7TreeNode>();
        list.addAll(attributes);
        list.addAll(nameSpaces);
        list.addAll(super.getChildren());

        return list;
    }

    public List<HL7TreeNode> getElementChildren() {
        return super.getChildren();
    }

    public List<HL7TreeNode> getAttributeChildren() {
        List<HL7TreeNode> list = new ArrayList<HL7TreeNode>();
        list.addAll(attributes);
        return list;
    }

    public List<HL7TreeNode> getNameSpaceChildren() {
        List<HL7TreeNode> list = new ArrayList<HL7TreeNode>();
        list.addAll(nameSpaces);
        return list;
    }
}
