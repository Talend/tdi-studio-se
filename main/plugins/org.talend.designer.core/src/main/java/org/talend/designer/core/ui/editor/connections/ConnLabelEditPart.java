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
import java.util.ArrayList;
import java.util.List;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.PolylineConnection;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.gef.DragTracker;
import org.eclipse.gef.EditPolicy;
import org.eclipse.gef.Request;
import org.eclipse.gef.RequestConstants;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.editparts.AbstractGraphicalEditPart;
import org.eclipse.gef.editpolicies.ConnectionEditPolicy;
import org.eclipse.gef.requests.GroupRequest;
import org.talend.commons.ui.utils.workbench.gef.LabelCellEditorLocator;
import org.talend.core.model.process.Element;
import org.talend.core.model.process.IConnectionCategory;
import org.talend.designer.core.ui.editor.cmd.ConnectionDeleteCommand;
import org.talend.designer.core.ui.editor.nodes.Node;
import org.talend.designer.core.ui.editor.subjobcontainer.SubjobContainer;

/**
 * Graphical part of the Gef object for the connection label. <br/>
 *
 * $Id$
 *
 */
public class ConnLabelEditPart extends AbstractGraphicalEditPart implements PropertyChangeListener {

    NodeLabelEditManager manager = null;

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.gef.editparts.AbstractGraphicalEditPart#activate()
     */
    @Override
    public void activate() {
        if (!isActive()) {
            super.activate();
            ((Element) getModel()).addPropertyChangeListener(this);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.gef.editparts.AbstractGraphicalEditPart#deactivate()
     */
    @Override
    public void deactivate() {
        if (isActive()) {
            super.deactivate();
            ((Element) getModel()).removePropertyChangeListener(this);
            manager = null;
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.gef.editparts.AbstractGraphicalEditPart#createFigure()
     */
    @Override
    public IFigure createFigure() {
        String text = ((ConnectionLabel) getModel()).getLabelText();
        Label label = new Label();
        label.setText(text);
        return label;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
     */
    public void propertyChange(PropertyChangeEvent evt) {
        String request = evt.getPropertyName();
        if (request.equals("positionChange") || request.equals("textChange")) { //$NON-NLS-1$ //$NON-NLS-2$
            refreshVisuals();
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.gef.editparts.AbstractEditPart#createEditPolicies()
     */
    @Override
    public void createEditPolicies() {
        installEditPolicy(EditPolicy.GRAPHICAL_NODE_ROLE, new ConnTextMovePolicy());
        installEditPolicy(EditPolicy.DIRECT_EDIT_ROLE, new ConnTextEditPolicy());
        installEditPolicy(EditPolicy.COMPONENT_ROLE, new ConnectionEditPolicy() {

            @Override
            protected Command getDeleteCommand(GroupRequest request) {
                if (((Connection) getHost().getParent().getModel()).isReadOnly()) {
                    return null;
                }
                List<Connection> connectionList = new ArrayList<Connection>();
                for (int i = 0; i < request.getEditParts().size(); i++) {
                    if (request.getEditParts().get(i) instanceof ConnLabelEditPart) {
                        connectionList.add(((ConnectionLabel) ((ConnLabelEditPart) request.getEditParts().get(i)).getModel())
                                .getConnection());
                    }
                }
                return new ConnectionDeleteCommand(connectionList);
            }
        });
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
        String text = ((ConnectionLabel) getModel()).getLabelText();
        Point offset = ((ConnectionLabel) getModel()).getOffset();
        Label figure = (Label) getFigure();
        figure.setText(text);
        ConnectionPart parent = (ConnectionPart) getParent();
        PolylineConnection connFigure = (PolylineConnection) parent.getFigure();
        ConnLabelConstraint constraint = new ConnLabelConstraint(text, "center", offset, connFigure); //$NON-NLS-1$
        parent.setLayoutConstraint(this, getFigure(), constraint);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.gef.editparts.AbstractEditPart#performRequest(org.eclipse.gef.Request)
     */
    @Override
    public void performRequest(Request request) {
        Connection connectionParent = (Connection) getParent().getModel();

        if (((Connection) getParent().getModel()).isReadOnly()) {
            return;
        }
        if (request.getType() == RequestConstants.REQ_DIRECT_EDIT
                && (connectionParent.getLineStyle().hasConnectionCategory(IConnectionCategory.CUSTOM_NAME))) {
            performDirectEdit();
        }
    }

    /**
     * Start the manager to edit the label.
     */
    private void performDirectEdit() {
        ((Label) getFigure()).setText(((Connection) getParent().getModel()).getName());
        if (manager == null) {
            manager = new NodeLabelEditManager(this, NodeLabelCellEditor.class, new LabelCellEditorLocator((Label) getFigure()));
        }
        manager.show();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.gef.editparts.AbstractEditPart#setSelected(int)
     */
    @Override
    public void setSelected(int value) {
        super.setSelected(value);
        if (this.getParent().getSelected() != value) {
            this.getParent().setSelected(value);
        }
    }

    /**
     * Getter for manager.
     *
     * @return the manager
     */
    public NodeLabelEditManager getDirectEditManager() {
        return this.manager;
    }

    @Override
    public boolean isSelectable() {
        ConnectionLabel connLabel = (ConnectionLabel) this.getModel();
        Node source = (Node) connLabel.getConnection().getSource();
        Node target = (Node) connLabel.getConnection().getTarget();
        SubjobContainer sourceSubjob = source.getNodeContainer().getSubjobContainer();
        SubjobContainer targetSubjob = target.getNodeContainer().getSubjobContainer();
        if (sourceSubjob != null) {
            if ((sourceSubjob == targetSubjob) && sourceSubjob.isCollapsed()) {
                return false;
            } else if (!connLabel.getConnection().isActivate() && sourceSubjob.isCollapsed()) {
                if (sourceSubjob.getSubjobStartNode().getUniqueName().equals(source.getUniqueName())) {
                    return super.isSelectable();
                }
                return false;
            }
        }

        return super.isSelectable();
    }

}
