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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.PolylineConnection;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.gef.DragTracker;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPolicy;
import org.eclipse.gef.GraphicalEditPart;
import org.eclipse.gef.Request;
import org.eclipse.gef.RequestConstants;
import org.eclipse.gef.editparts.AbstractGraphicalEditPart;
import org.talend.commons.ui.gmf.util.DisplayUtils;
import org.talend.core.model.process.Element;
import org.talend.core.model.process.IElementParameter;
import org.talend.core.model.process.TraceData;
import org.talend.designer.core.model.components.EParameterName;
import org.talend.designer.core.ui.dialog.FilterColumnDialog;
import org.talend.designer.core.ui.editor.cmd.PropertyChangeCommand;

/**
 * EditPart of the NodeTrace.
 *
 * $Id$
 *
 */
public class ConnectionTraceEditPart extends AbstractGraphicalEditPart implements PropertyChangeListener {

    @Override
    public boolean isSelectable() {
        return true;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.gef.editparts.AbstractGraphicalEditPart#activate()
     */
    public void activate() {
        if (!isActive()) {
            super.activate();
            ((Element) getModel()).addPropertyChangeListener(this);
        }
    }

    protected void addChildVisual(EditPart childEditPart, int index) {
        IFigure child = ((GraphicalEditPart) childEditPart).getFigure();
        getContentPane().add(child, index);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.gef.editparts.AbstractGraphicalEditPart#deactivate()
     */
    public void deactivate() {
        if (isActive()) {
            super.deactivate();
            ((Element) getModel()).removePropertyChangeListener(this);
            unregister();
        }
    }

    public ConnectionTraceEditPart() {
        super();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.gef.editparts.AbstractGraphicalEditPart#createFigure()
     */
    @Override
    protected IFigure createFigure() {
        ConnectionTraceFigure fig = new ConnectionTraceFigure((Connection) this.getParent().getModel(), true);
        return fig;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.gef.editparts.AbstractEditPart#createEditPolicies()
     */
    @Override
    protected void createEditPolicies() {
        installEditPolicy(EditPolicy.GRAPHICAL_NODE_ROLE, new ConnTextMovePolicy());
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.gef.editparts.AbstractGraphicalEditPart#getDragTracker(org.eclipse.gef.Request)
     */
    @Override
    public DragTracker getDragTracker(Request request) {
        return new ConnTextTracker(this, (ConnectionPart) getParent());
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.gef.editparts.AbstractEditPart#refreshVisuals()
     */
    @Override
    protected void refreshVisuals() {
        ConnectionPart connectionPart;
        connectionPart = (ConnectionPart) getParent();

        Connection conn = (Connection) connectionPart.getModel();
        IElementParameter element = conn.getElementParameter(EParameterName.TRACES_CONNECTION_ENABLE.getName());
        Boolean flag = false;
        if (element != null) {
            flag = (Boolean) element.getValue();
        }

        ConnectionTraceFigure htmlFigure = (ConnectionTraceFigure) getFigure();

        ConnectionTrace connectionTrace = (ConnectionTrace) getModel();
        TraceData trace = connectionTrace.getTrace();
        if (trace == null) {
            Point offset = conn.getConnectionLabel().getOffset();
            connectionTrace.getOffset().x = offset.x;
            connectionTrace.getOffset().y = offset.y;
            this.removeEditPolicy(EditPolicy.GRAPHICAL_NODE_ROLE);
        } else {
            this.installEditPolicy(EditPolicy.GRAPHICAL_NODE_ROLE, new ConnTextMovePolicy());
        }
        htmlFigure.setTraceData(trace, flag, conn.checkTraceShowEnable());

        Dimension size = htmlFigure.getPreferredSize();
        connectionTrace.setSize(size);
        String connectionName = ((Connection) connectionPart.getModel()).getConnectionLabel().getLabelText();
        Point offset = ((ConnectionTrace) getModel()).getOffset();

        ConnectionTraceConstraint constraint = new ConnectionTraceConstraint(connectionName, size, "center", offset, //$NON-NLS-1$
                (PolylineConnection) connectionPart.getFigure());

        connectionPart.setLayoutConstraint(this, getFigure(), constraint);
    }

    /*
     * (non-Javadoc)
     *
     * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
     */
    public void propertyChange(PropertyChangeEvent evt) {
        String request = evt.getPropertyName();
        if (request.equals("positionChange") || request.equals(ConnectionTrace.TRACE_PROP)) { //$NON-NLS-1$ //$NON-NLS-2$
            refreshVisuals();
        } else if (request.equals(EParameterName.TRACES_SHOW_ENABLE.getName())
                || request.equals(EParameterName.TRACES_CONNECTION_FILTER.getName())
                || request.equals(EParameterName.ACTIVEBREAKPOINT.getName())) {
            refreshVisuals();
        } else if (request.equals(EParameterName.TRACES_CONNECTION_ENABLE.getName())) {
            ConnectionTrace connectionTrace = ((ConnectionTrace) this.getModel());
            Connection connection = connectionTrace.getConnection();
            PropertyChangeCommand pcc = new PropertyChangeCommand(connection, EParameterName.TRACES_CONNECTION_ENABLE.getName(),
                    evt.getNewValue());
            this.getViewer().getEditDomain().getCommandStack().execute(pcc);
            refreshVisuals();
        }

    }

    public void performRequest(Request req) {
        if (req.getType().equals(RequestConstants.REQ_OPEN)) {
            ConnectionTrace a = ((ConnectionTrace) this.getModel());
            FilterColumnDialog dialog = new FilterColumnDialog(DisplayUtils.getDefaultShell(false), a.getConnection(),
                    this.getViewer().getEditDomain().getCommandStack());
            dialog.open();
        }
    }

}
