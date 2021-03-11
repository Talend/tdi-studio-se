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
package org.talend.designer.core.ui.editor.connections;

import java.util.List;

import org.eclipse.draw2d.geometry.Point;
import org.talend.core.model.process.Element;
import org.talend.core.model.process.IElementParameter;

/**
 * Label object of a connection. This is the model part of the Gef item. <br/>
 *
 * $Id$
 *
 */
public class ConnectionLabel extends Element {

    private static final int MAX_DISTANCE = 50;

    private String labelText = ""; //$NON-NLS-1$

    private Point offset = new Point();

    private Connection parent = null;

    /**
     * Create a new label for a connection with a given label, position and connection.
     *
     * @param labelText
     * @param position (can be either start, end or center)
     * @param parent
     */
    public ConnectionLabel(String labelText, Connection parent) {
        this.labelText = labelText;
        this.parent = parent;
    }

    /**
     * Set the text of the label.
     *
     * @param labelText
     */
    public void setLabelText(String labelText) {
        this.labelText = labelText;
        firePropertyChange("textChange", null, null); //$NON-NLS-1$
    }

    /**
     * Get the text of the label.
     *
     * @return label
     */
    public String getLabelText() {
        return labelText;
    }

    /**
     * Get the connection parent of the label.
     *
     * @return Connection
     */
    public Connection getConnection() {
        return parent;
    }

    /**
     * Set the offset for the label. The offset is linked to the position of the label. The offset will be limited to a
     * distance maximum to avoid to have a label too far away from the connection.
     *
     * @param offset Point
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

        if (offset.y > MAX_DISTANCE) {
            this.offset.y = MAX_DISTANCE;
        } else {
            if (offset.y < -MAX_DISTANCE) {
                this.offset.y = -MAX_DISTANCE;
            } else {
                this.offset.y = offset.y;
            }
        }
        firePropertyChange("positionChange", null, null); //$NON-NLS-1$
    }

    /**
     * Return the offset of the label.
     *
     * @return
     */
    public Point getOffset() {
        return offset;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.core.ui.editor.Element#setPropertyValue(java.lang.Object, java.lang.Object)
     */
    public void setPropertyValue(String id, Object value) {
        parent.setPropertyValue(id, value);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.core.ui.editor.Element#getPropertyValue(java.lang.Object)
     */
    public Object getPropertyValue(String id) {
        return parent.getPropertyValue(id);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.core.ui.editor.Element#getElementName()
     */
    @Override
    public String getElementName() {
        return parent.getElementName();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.core.ui.editor.Element#getElementParameters()
     */
    @Override
    public List<? extends IElementParameter> getElementParameters() {
        return parent.getElementParameters();
    }

    public IElementParameter getElementParameter(String name) {
        return parent.getElementParameter(name);
    }

    public boolean isReadOnly() {
        return parent.isReadOnly();
    }

    public void setReadOnly(boolean readOnly) {
    }
}
