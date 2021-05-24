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
package org.talend.sqlbuilder.dbstructure;

import org.eclipse.jface.viewers.ITableColorProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.talend.sqlbuilder.dbstructure.nodes.INode;
import org.talend.sqlbuilder.util.ImageUtil;

/**
 * Label provider for database structure outline.
 *
 * @author Davy Vanherbergen
 */
public class DBTreeLabelProvider extends LabelProvider implements ITableLabelProvider, ITreeContentProvider,
        ITableColorProvider {

    // For Label Provider
    private Image pDefaultNodeImage = ImageUtil.getImage("Images.DefaultNodeIcon"); //$NON-NLS-1$

    private Image pDefaultParentNodeImage = ImageUtil.getImage("Images.DefaultParentNodeIcon"); //$NON-NLS-1$

    /**
     * dispose.
     */
    public void dispose() {

        super.dispose();
        ImageUtil.disposeImage("Images.DefaultNodeIcon"); //$NON-NLS-1$
        ImageUtil.disposeImage("Images.DefaultParentNodeIcon"); //$NON-NLS-1$

    }

    /**
     * Return the image used for the given INode. If the INode does not have an image, default images are returned.
     *
     * @param element Node.
     * @return Image
     * @see org.eclipse.jface.viewers.ILabelProvider#getImage(java.lang.Object)
     */
    public Image getImage(Object element) {

        INode node = (INode) element;

        // return expanded image if node is expanded and we have an image
        if (node.isExpanded() && node.getExpandedImage() != null && node.getChildNodes() != null
                && node.getChildNodes().length != 0) {
            return node.getExpandedImage();
        }

        // return custom image
        if (node.getImage() != null) {
            return node.getImage();
        }

        // return one of the default images
        if (node.hasChildNodes()) {
            return pDefaultParentNodeImage;
        } else {
            return pDefaultNodeImage;
        }

    }

    /**
     * Return the text to display the INode.
     *
     * @param element Node.
     * @return Text.
     * @see org.eclipse.jface.viewers.ILabelProvider#getText(java.lang.Object)
     */
    public String getText(Object element) {

        INode node = (INode) element;
        String text = node.getLabelText();

        // return default if no label is provided
        if (text == null) {
            text = node.toString();
        }

        // if (node.getLabelDecoration() != null) {
        // text = text + " [" + node.getLabelDecoration() + "]";
        // }

        return text;
    }

    /**
     * @param element Node
     * @param columnIndex Column Index
     * @return Column Image
     */
    public Image getColumnImage(Object element, int columnIndex) {

        if (columnIndex == 0) {
            return getImage(element);
        } else if (columnIndex == 1) {
            return ((INode) element).getImageAtColumn(columnIndex);
        }

        return null;
    }

    /**
     * @param element Node
     * @param columnIndex column index
     * @return Column text.
     */
    public String getColumnText(Object element, int columnIndex) {
        return ((INode) element).getLabelAtColumn(columnIndex);
    }

    /**
     * Return all the children of an INode element.
     *
     * @param parentElement parent node
     * @return children.
     * @see org.eclipse.jface.viewers.ITreeContentProvider#getChildren(java.lang.Object)
     */
    public Object[] getChildren(Object parentElement) {

        Object[] children = ((INode) parentElement).getChildNodes();
        return children;
    }

    /**
     * Return all the children of an INode element.
     *
     * @param inputElement inputElement.
     * @return elements
     * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java.lang.Object)
     */
    public Object[] getElements(Object inputElement) {

        return getChildren(inputElement);
    }

    /**
     * Return the parent of an INode element.
     *
     * @param element element.
     * @return Parent.
     * @see org.eclipse.jface.viewers.ITreeContentProvider#getParent(java.lang.Object)
     */
    public Object getParent(Object element) {

        Object parent = ((INode) element).getParent();
        return parent;
    }

    /**
     * Returns true if the INode has children.
     *
     * @param element element.
     * @return hasChildren.
     * @see org.eclipse.jface.viewers.ITreeContentProvider#hasChildren(java.lang.Object)
     */
    public boolean hasChildren(Object element) {

        return ((INode) element).hasChildNodes();
    }

    /**
     * We don't do anything here..
     *
     * @param viewer Viewer.
     * @param oldInput oldInput
     * @param newInput newInput
     * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer, java.lang.Object,
     * java.lang.Object)
     */
    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
    }

    // For color provider
    Color red = Display.getDefault().getSystemColor(SWT.COLOR_RED);

    Color gray = Display.getDefault().getSystemColor(SWT.COLOR_GRAY);

    /**
     * @param element element.
     * @param columnIndex column index.
     * @return Background.
     */
    public Color getBackground(Object element, int columnIndex) {
        return ((INode) element).getBackground();
    }

    /**
     * @param element element.
     * @param columnIndex column index.
     * @return Foreground
     */
    public Color getForeground(Object element, int columnIndex) {
        return ((INode) element).getForeground();
    }
}
