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
 * Label object of a node. This is the model part of the Gef item. <br/>
 *
 * $Id$
 *
 */
public class NodeLabel extends Element {

    public static final String LOCATION = "nodeLabelLocation"; //$NON-NLS-1$

    public static final String OFFSET_CHANGE = "offsetChange"; //$NON-NLS-1$

    public static final String TEXT_CHANGE = "textChange"; //$NON-NLS-1$

    private static final long serialVersionUID = 1L;

    private static final int MAX_DISTANCE = 50;

    private static final int STANDARD_LABEL_HEIGHT = 13;

    private String labelText = ""; //$NON-NLS-1$

    private Point offset = new Point();

    private Point textOffset = new Point();

    private Dimension labelSize = new Dimension();

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
    public NodeLabel(String labelText, Node nodeParent) {
        this.labelText = labelText;
        this.node = nodeParent;
    }

    /**
     * Set the text of the label.
     *
     * @param labelText
     */
    public void setLabelText(String labelText) {
        this.labelText = labelText;
        firePropertyChange(TEXT_CHANGE, null, null); //$NON-NLS-1$
    }

    /**
     * Gives the text of the label.
     *
     * @param labelText
     */
    public String getLabelText() {
        return labelText;
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
     * Set the offset for the label. The offset is linked to the location of the label.
     *
     * @param offset
     */
    public void setOffset(Point offset) {
        if (offset.x > MAX_DISTANCE) {
            this.offset.x = MAX_DISTANCE;
        } else {
            if (offset.x < -MAX_DISTANCE) {
                this.offset.x = -MAX_DISTANCE;
            } else {
                this.offset.x = offset.x;
            }
        }

        if (offset.y > (MAX_DISTANCE - STANDARD_LABEL_HEIGHT)) {
            this.offset.y = MAX_DISTANCE - STANDARD_LABEL_HEIGHT;
        } else {
            if (offset.y < (-MAX_DISTANCE - node.getSize().height)) {
                this.offset.y = -MAX_DISTANCE - node.getSize().height;
            } else {
                this.offset.y = offset.y;
            }
        }
        firePropertyChange(OFFSET_CHANGE, null, null); //$NON-NLS-1$
    }

    /**
     * Gives the offset of the label.
     *
     * @return
     */
    public Point getOffset() {
        return offset;
    }

    /**
     * This offset will change when the text is too long, then the label will be always centered with the node.
     *
     * @param textOffset
     */
    public void setTextOffset(Point textOffset) {
        this.textOffset = textOffset;
    }

    /**
     * Gives the offset dependings on the text lenght.
     *
     * @return
     */
    public Point getTextOffset() {
        return textOffset;
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

    public Dimension getLabelSize() {
        return this.labelSize;
    }

    public void setLabelSize(Dimension labelSize) {
        this.labelSize = labelSize;
    }

    public boolean isReadOnly() {
        return node.isReadOnly();
    }

    public void setReadOnly(boolean readOnly) {
    }
}
