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
package org.talend.designer.core.ui.editor.nodes;

import java.util.List;

import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.talend.core.model.process.Element;
import org.talend.core.model.process.IElementParameter;
import org.talend.designer.core.model.components.EParameterName;

/**
 * DOC hwang class global comment. Detailled comment
 */
public class NodeError extends Element {

    public static final String LOCATION = "nodeLabelLocation"; //$NON-NLS-1$

    private static final long serialVersionUID = 1L;

    private Dimension errorSize = new Dimension();

    private Node node = null;

    protected Point location = new Point(0, 0);

    // true if this node is activated.
    private boolean activate = true;

    /**
     * Create a new label for a node with a given label and node.
     *
     * @param labelText
     * @param nodeParent
     */
    public NodeError(Node nodeParent) {
        this.node = nodeParent;
    }

    /**
     * Set the location of the label.
     *
     * @param location
     */
    public void setLocation(Point location) {
        if (this.location.equals(location)) {
            return;
        }
        this.location = location;

        firePropertyChange(LOCATION, null, location);
    }

    /**
     * Gives the location of the label (must add the offset to have the correct position).
     *
     * @return Point
     */
    public Point getLocation() {
        return location;
    }

    /**
     * Gives the node parent of this label.
     *
     * @return
     */
    public Node getNode() {
        return node;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.core.ui.editor.Element#setPropertyValue(java.lang.Object, java.lang.Object)
     */
    public void setPropertyValue(String id, Object value) {
        node.setPropertyValue(id, value);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.core.ui.editor.Element#getPropertyValue(java.lang.Object)
     */
    public Object getPropertyValue(String id) {
        return node.getPropertyValue(id);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.core.ui.editor.Element#getElementName()
     */
    @Override
    public String getElementName() {
        return node.getElementName();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.core.ui.editor.Element#getElementParameters()
     */
    @Override
    public List<? extends IElementParameter> getElementParameters() {
        return node.getElementParameters();
    }

    public IElementParameter getElementParameter(String name) {
        return node.getElementParameter(name);
    }

    public boolean isActivate() {
        return this.activate;
    }

    public void setActivate(boolean activate) {
        this.activate = activate;
        firePropertyChange(EParameterName.ACTIVATE.getName(), null, null);
    }

    public Dimension getErrorSize() {
        return this.errorSize;
    }

    public void setErrorSize(Dimension errorSize) {
        this.errorSize = errorSize;
    }

    public boolean isReadOnly() {
        return node.isReadOnly();
    }

    public void setReadOnly(boolean readOnly) {
    }

    public void updateState(final String id, Object value) {
        // if (id.equals("UPDATE_STATUS")) {
        firePropertyChange("UPDATE_STATUS", null, null); //$NON-NLS-1$
        // }
    }

}
