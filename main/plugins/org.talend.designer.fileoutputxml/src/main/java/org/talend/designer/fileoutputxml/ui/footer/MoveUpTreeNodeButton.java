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
package org.talend.designer.fileoutputxml.ui.footer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.swt.widgets.Composite;
import org.talend.commons.ui.runtime.image.EImage;
import org.talend.commons.ui.runtime.image.ImageProvider;
import org.talend.designer.fileoutputxml.managers.FOXManager;
import org.talend.metadata.managment.ui.wizard.metadata.xml.node.Attribute;
import org.talend.metadata.managment.ui.wizard.metadata.xml.node.Element;
import org.talend.metadata.managment.ui.wizard.metadata.xml.node.FOXTreeNode;
import org.talend.metadata.managment.ui.wizard.metadata.xml.node.NameSpaceNode;

/**
 * DOC talend class global comment. Detailled comment
 */
public class MoveUpTreeNodeButton extends AbstractTreeNodeButton {

    /**
     * DOC talend MoveUpTreeNodeButton constructor comment.
     *
     * @param parent
     * @param extendedViewer
     * @param tooltip
     * @param image
     */
    public MoveUpTreeNodeButton(Composite parent, FOXManager manager) {
        super(parent, manager, "Move up", ImageProvider.getImage(EImage.UP_ICON));
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.fileoutputxml.ui.footer.AbstractTreeNodeButton#handleSelectionEvent()
     */
    @Override
    protected void handleSelectionEvent(TreeSelection selection) {
        if (!selection.isEmpty()) {
            final Element parentNode = (Element) ((FOXTreeNode) selection.getFirstElement()).getParent();
            final List<? extends FOXTreeNode> attrChildren = parentNode.getAttributeChildren();
            final List<? extends FOXTreeNode> nameSpaceChildren = parentNode.getNameSpaceChildren();
            final List<FOXTreeNode> elementChildren = parentNode.getElementChildren();
            List<Integer> attrIndices = new ArrayList<Integer>();
            List<Integer> nameSpaceIndices = new ArrayList<Integer>();
            List<Integer> elementIndices = new ArrayList<Integer>();
            final Iterator iterator = selection.iterator();
            while (iterator.hasNext()) {
                final Object next = iterator.next();
                if (next instanceof Attribute) {
                    if (attrChildren.contains(next)) {
                        attrIndices.add(attrChildren.indexOf(next));
                    }
                } else if (next instanceof NameSpaceNode) {
                    if (nameSpaceChildren.contains(next)) {
                        nameSpaceIndices.add(nameSpaceChildren.indexOf(next));
                    }
                } else if (next instanceof Element) {
                    if (elementChildren.contains(next)) {
                        elementIndices.add(elementChildren.indexOf(next));
                    }
                }

            }
            Collections.sort(attrIndices);
            Collections.sort(nameSpaceIndices);
            Collections.sort(elementIndices);

            swapElements(attrChildren, attrIndices);
            swapElements(nameSpaceChildren, nameSpaceIndices);
            swapElements(elementChildren, elementIndices);

            treeViewer.refresh(parentNode);
            treeViewer.expandAll();
            manager.getUiManager().getFoxUI().redrawLinkers();
            treeViewer.setSelection(selection);
        }
    }

    private void swapElements(List<? extends FOXTreeNode> nodeList, List<Integer> indices) {
        for (int i = 0; i < indices.size(); i++) {
            int index = indices.get(i);
            if (index >= 1) {
                Collections.swap(nodeList, index, index - 1);
            }
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.fileoutputxml.ui.footer.AbstractTreeNodeButton#updateButtonStatus()
     */
    @Override
    protected void updateStatus(TreeSelection selection) {
        if (selection.isEmpty()) {
            getButton().setEnabled(false);
            return;
        }
        final TreePath[] paths = selection.getPaths();
        boolean sameSegment = true;
        for (int i = 0; i < paths.length - 1; i++) {
            if (paths[i].getSegmentCount() != paths[i + 1].getSegmentCount()) {
                sameSegment = false;
            }
        }
        if (sameSegment) {
            getButton().setEnabled(true);
        } else {
            getButton().setEnabled(false);
            return;
        }

        // if same segment ,they have the same parent and parent must be an element
        final Element parent = (Element) ((FOXTreeNode) selection.getFirstElement()).getParent();
        if (parent == null) {
            getButton().setEnabled(false);
            return;
        }
        final List<? extends FOXTreeNode> attrChildren = parent.getAttributeChildren();
        final List<? extends FOXTreeNode> nameSpaceChildren = parent.getNameSpaceChildren();
        final List<FOXTreeNode> elementChildren = parent.getElementChildren();
        final Iterator iterator = selection.iterator();
        while (iterator.hasNext()) {
            final Object next = iterator.next();
            if (next instanceof Attribute) {
                if (attrChildren.contains(next) && attrChildren.indexOf(next) == 0) {
                    getButton().setEnabled(false);
                    return;
                }
            } else if (next instanceof NameSpaceNode) {
                if (nameSpaceChildren.contains(next) && nameSpaceChildren.indexOf(next) == 0) {
                    getButton().setEnabled(false);
                    return;
                }
            } else if (next instanceof Element) {
                if (elementChildren.contains(next) && elementChildren.indexOf(next) == 0) {
                    getButton().setEnabled(false);
                    return;
                }
            }

        }

    }
}
