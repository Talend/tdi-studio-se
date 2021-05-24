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

import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.talend.core.model.process.Element;
import org.talend.core.model.process.IElementParameter;
import org.talend.core.model.process.TraceData;
import org.talend.designer.core.model.components.EParameterName;

/**
 * Label object of a connection. This is the model part of the Gef item. <br/>
 *
 * $Id$
 *
 */
public class ConnectionTrace extends Element {

    private Point offset = new Point();

    public static final String TRACE_PROP = "traceChange"; //$NON-NLS-1$

    private TraceData trace = null; //$NON-NLS-1$

    private Connection connection = null;

    private boolean collapse = false;

    private Dimension size;

    public ConnectionTrace(Connection connection) {
        this.connection = connection;
    }

    public void setTrace(TraceData trace) {
        this.trace = trace;
        firePropertyChange(TRACE_PROP, null, null);
    }

    public TraceData getTrace() {
        return trace;
    }

    // public Dimension getSize() {
    // return this.size;
    // }

    public void setSize(Dimension size) {
        this.size = size;
    }

    /**
     * Get the connection parent of the label.
     *
     * @return Connection
     */
    public Connection getConnection() {
        return connection;
    }

    private void refreshTraceIcon(boolean traceFlag) {
        super.setPropertyValue(EParameterName.TRACES_SHOW_ENABLE.getName(), traceFlag); // maybe, not used.
        firePropertyChange(EParameterName.TRACES_SHOW_ENABLE.getName(), null, traceFlag);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.core.ui.editor.Element#setPropertyValue(java.lang.Object, java.lang.Object)
     */
    public void setPropertyValue(String id, Object value) {
        if (id.equals(EParameterName.TRACES_SHOW_ENABLE.getName())
                || id.equals(EParameterName.TRACES_CONNECTION_ENABLE.getName())
                || id.equals(EParameterName.TRACES_CONNECTION_FILTER.getName())
                || id.equals(EParameterName.ACTIVEBREAKPOINT.getName())) {
            firePropertyChange(id, null, value);
            return; // else, it will loop.
        }
        connection.setPropertyValue(id, value);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.core.ui.editor.Element#getPropertyValue(java.lang.Object)
     */
    public Object getPropertyValue(String id) {
        return connection.getPropertyValue(id);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.core.ui.editor.Element#getElementName()
     */
    @Override
    public String getElementName() {
        return connection.getElementName();
    }

    private static final int MAX_DISTANCE = 50;

    /**
     * Set the offset for the label. The offset is linked to the position of the label. The offset will be limited to a
     * distance maximum to avoid to have a label too far away from the connection.
     *
     * @param offset Point
     */
    public void setOffset(Point offset) {
        this.offset.x = offset.x;
        this.offset.y = offset.y;
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

    /**
     * Getter for collapse.
     *
     * @return the collapse
     */
    public boolean isCollapse() {
        return this.collapse;
    }

    /**
     * Sets the collapse.
     *
     * @param collapse the collapse to set
     */
    public void setCollapse(boolean collapse) {
        this.collapse = collapse;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.core.ui.editor.Element#getElementParameters()
     */
    @Override
    public List<? extends IElementParameter> getElementParameters() {
        return connection.getElementParameters();
    }

    public IElementParameter getElementParameter(String name) {
        return connection.getElementParameter(name);
    }

    public boolean isReadOnly() {
        return connection.isReadOnly();
    }

    public void setReadOnly(boolean readOnly) {
    }
}
