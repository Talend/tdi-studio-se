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
package org.talend.designer.hl7.ui.edit;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.graphics.Image;
import org.talend.designer.hl7.ui.data.Attribute;
import org.talend.designer.hl7.ui.data.HL7TreeNode;
import org.talend.designer.hl7.ui.data.NameSpaceNode;

/**
 * DOC hwang class global comment. Detailled comment
 */
public class HL7TargetTreeViewerProvider extends LabelProvider implements ITableLabelProvider, ITreeContentProvider {

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.viewers.ITreeContentProvider#getChildren(java.lang.Object)
     */
    public Object[] getChildren(Object parentElement) {
        HL7TreeNode treeNode = (HL7TreeNode) parentElement;
        List<HL7TreeNode> children = treeNode.getChildren();
        if (children.size() >= 0) {
            treeNode.setMain(true);
        }
        return children.toArray();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.viewers.ITreeContentProvider#getParent(java.lang.Object)
     */
    public Object getParent(Object element) {
        HL7TreeNode treeNode = (HL7TreeNode) element;
        return treeNode.getParent();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.viewers.ITreeContentProvider#hasChildren(java.lang.Object)
     */
    public boolean hasChildren(Object element) {
        HL7TreeNode treeNode = (HL7TreeNode) element;
        return !treeNode.getChildren().isEmpty();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java.lang.Object)
     */
    public Object[] getElements(Object inputElement) {
        if (inputElement instanceof HL7TreeNode) {
            List list = new ArrayList();
            list.add(inputElement);
            return list.toArray();
        }
        List list = (List) inputElement;
        return list.toArray();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer, java.lang.Object,
     * java.lang.Object)
     */
    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
        // TODO Auto-generated method stub

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnImage(java.lang.Object, int)
     */
    public Image getColumnImage(Object element, int columnIndex) {
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnText(java.lang.Object, int)
     */
    public String getColumnText(Object element, int columnIndex) {
        HL7TreeNode treeNode = (HL7TreeNode) element;
        switch (columnIndex) {
        case 0:
            return treeNode.getLabelForViewer();
        case 1:
            return treeNode.getColumnLabel();
        case 2:
            if (treeNode instanceof Attribute) {
                return "-"; //$NON-NLS-1$
            } else if (treeNode instanceof NameSpaceNode) {
                return "-"; //$NON-NLS-1$
            } else if (treeNode.isGroup()) {
                return "Group element"; //$NON-NLS-1$
            } else if (treeNode.isRepetable()) {
                return "repetable element";
            }
            // else if (TreeUtil.isSubLoopNode(treeNode)) {
            //                return "group"; //$NON-NLS-1$
            // }
            else {
                return ""; //$NON-NLS-1$
            }
        case 3:
            return treeNode.getDefaultValue();
        default:
            return ""; //$NON-NLS-1$
        }
    }

}
