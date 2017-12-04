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
public class NameSpaceNode extends HL7TreeNode {

    public NameSpaceNode() {
    }

    public NameSpaceNode(String label) {
        setLabel(label);
    }

    /**
     * (non-Javadoc)
     * 
     * @see org.talend.metadata.managment.ui.wizard.metadata.xml.node.fileoutputxml.data.FOXTreeNode#hasChildren()
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
        if (super.getLabel() != null && super.getLabel().length() > 0) {
            return "xmlns:" + super.getLabel(); //$NON-NLS-1$
        } else {
            return "xmlns:(default)"; //$NON-NLS-1$
        }
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
