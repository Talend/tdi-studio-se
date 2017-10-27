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

/**
 * DOC hwang class global comment. Detailled comment
 */
public class Attribute extends HL7TreeNode {

    /**
     * Attribute constructor comment.
     */
    public Attribute() {
    }

    /**
     * Attribute constructor comment.
     */
    public Attribute(String label) {
        setLabel(label);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.designer.fileoutputxml.data.FOXTreeNode#hasChildren()
     */
    @Override
    public boolean hasChildren() {
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.designer.fileoutputxml.data.FOXTreeNode#getValue()
     */
    @Override
    public String getLabelForViewer() {
        return "@" + super.getLabel(); //$NON-NLS-1$
    }

    @Override
    public boolean isGroup() {
        return false;
    }

    @Override
    public boolean isRepetable() {
        return false;
    }

    @Override
    public boolean isMain() {
        return false;
    }
}
