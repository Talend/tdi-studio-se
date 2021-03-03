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
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.PolylineConnection;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.gef.DragTracker;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPolicy;
import org.eclipse.gef.GraphicalEditPart;
import org.eclipse.gef.LayerConstants;
import org.eclipse.gef.Request;
import org.eclipse.gef.editparts.AbstractGraphicalEditPart;
import org.talend.core.model.process.Element;
import org.talend.designer.core.model.components.EParameterName;

/**
 * DOC Administrator class global comment. Detailled comment
 */
public class ConnectionResumingEditPart extends AbstractGraphicalEditPart implements PropertyChangeListener {

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

    public ConnectionResumingEditPart() {
        super();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.gef.editparts.AbstractGraphicalEditPart#createFigure()
     */
    @Override
    protected IFigure createFigure() {
        ConnectionResumingFigure fig = new ConnectionResumingFigure((Connection) this.getParent().getModel(), true);
        getLayer(LayerConstants.CONNECTION_LAYER).add(fig);
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
        ConnectionResumingFigure checkPointFigure = (ConnectionResumingFigure) getFigure();
        ConnectionResuming connectionResuming = (ConnectionResuming) getModel();
        String resum = connectionResuming.getResuming();
        if (resum == null) {
            Point offset = conn.getConnectionLabel().getOffset();
            connectionResuming.getOffset().x = offset.x;
            connectionResuming.getOffset().y = offset.y;
            this.removeEditPolicy(EditPolicy.GRAPHICAL_NODE_ROLE);
        } else {
            this.installEditPolicy(EditPolicy.GRAPHICAL_NODE_ROLE, new ConnTextMovePolicy());
        }
        if (conn.getPropertyValue(EParameterName.RESUMING_CHECKPOINT.getName()) == null)
            checkPointFigure.setResumingCheckPoint(false);
        else
            checkPointFigure.setResumingCheckPoint(conn.getPropertyValue(EParameterName.RESUMING_CHECKPOINT.getName()).toString()
                    .equals("true") ? true : false);
        Dimension size = checkPointFigure.getPreferredSize();
        connectionResuming.setSize(size);
        String connectionName = ((Connection) connectionPart.getModel()).getConnectionLabel().getLabelText();
        Point offset = ((ConnectionResuming) getModel()).getOffset();
        ConnectionResumingConstraint constraint = new ConnectionResumingConstraint(connectionName, size, "center", offset, //$NON-NLS-1$
                (PolylineConnection) connectionPart.getFigure());
        connectionPart.setLayoutConstraint(this, getFigure(), constraint);
        // add resum tooltip
        Label toolTipLabel = new Label();
        String toolTipText;
        if (conn.getPropertyValue(EParameterName.RESUMLABEL.getName()) == null)
            toolTipText = "";
        else
            toolTipText = conn.getPropertyValue(EParameterName.RESUMLABEL.getName()).toString();
        toolTipLabel.setText(toolTipText);
        checkPointFigure.setToolTip(toolTipLabel);

    }

    /*
     * (non-Javadoc)
     *
     * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
     */
    public void propertyChange(PropertyChangeEvent evt) {
        String request = evt.getPropertyName();
        if (request.equals(EParameterName.RESUMING_CHECKPOINT.getName())) { //$NON-NLS-1$ //$NON-NLS-2$
            refreshVisuals();
        }
        if (request.equals(EParameterName.RESUMLABEL.getName())) { //$NON-NLS-1$ //$NON-NLS-2$
            refreshVisuals();
        }
    }
}
