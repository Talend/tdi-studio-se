// ============================================================================
//
// Copyright (C) 2006-2019 Talend Inc. - www.talend.com
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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.ImageFigure;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.DragTracker;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPolicy;
import org.eclipse.gef.GraphicalEditPart;
import org.eclipse.gef.Request;
import org.eclipse.gef.editparts.AbstractGraphicalEditPart;
import org.eclipse.swt.graphics.Image;
import org.talend.commons.ui.gmf.util.DisplayUtils;
import org.talend.commons.ui.runtime.image.ECoreImage;
import org.talend.commons.ui.runtime.image.ImageProvider;
import org.talend.designer.core.model.components.EParameterName;
import org.talend.designer.core.ui.dialog.mergeorder.ErrorMessageDialog;
import org.talend.designer.core.ui.editor.nodecontainer.NodeContainer;
import org.talend.designer.core.ui.editor.nodecontainer.NodeContainerPart;
import org.talend.designer.core.ui.editor.process.ProcessPart;
import org.talend.designer.core.ui.editor.subjobcontainer.SubjobContainerPart;

/**
 * DOC hwang class global comment. Detailled comment
 */
public class NodeErrorEditPart extends AbstractGraphicalEditPart implements PropertyChangeListener {

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.gef.editparts.AbstractGraphicalEditPart#activate()
     */
    @Override
    public void activate() {
        if (!isActive()) {
            super.activate();
            ((NodeError) getModel()).addPropertyChangeListener(this);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.gef.editparts.AbstractGraphicalEditPart#getAdapter(java.lang.Class)
     */
    @Override
    public Object getAdapter(final Class key) {
        return super.getAdapter(key);
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
            ((NodeError) getModel()).removePropertyChangeListener(this);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.gef.editparts.AbstractGraphicalEditPart#createFigure()
     */
    @Override
    public IFigure createFigure() {
        NodeErrorFigure errorFig = new NodeErrorFigure();

        if (((NodeError) getModel()).isActivate()) {
            (errorFig).setAlpha(-1);
        } else {
            (errorFig).setAlpha(NodeError.ALPHA_VALUE);
        }
        ((NodeError) getModel()).setErrorSize(errorFig.getSize());

        return errorFig;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
     */
    public void propertyChange(final PropertyChangeEvent evt) {
        String request = evt.getPropertyName();
        if (request.equals("UPDATE_STATUS")) { //$NON-NLS-1$
            NodeErrorFigure figure = (NodeErrorFigure) this.getFigure();
            Node node = ((NodeContainer) ((NodeContainerPart) getParent()).getModel()).getNode();
            figure.updateVisible(node.isErrorFlag(), node.getErrorInfo());
            ((NodeError) getModel()).setErrorSize((figure).getSize());
            refreshVisuals();
        }

        if (request.equals(NodeError.LOCATION)) { //$NON-NLS-1$
            refreshVisuals();
            getParent().refresh();
        }
        if (request.equals(EParameterName.ACTIVATE.getName())) {
            if (((NodeError) getModel()).isActivate()) {
                ((NodeErrorFigure) figure).setAlpha(-1);
                ((NodeErrorFigure) figure).repaint();
                refreshVisuals();
            } else {
                ((NodeErrorFigure) figure).setAlpha(Node.ALPHA_VALUE);
                ((NodeErrorFigure) figure).repaint();
                refreshVisuals();
            }
        }
        EditPart editPart = getParent();
        if (editPart != null) {
            while ((!(editPart instanceof ProcessPart)) && (!(editPart instanceof SubjobContainerPart))) {
                editPart = editPart.getParent();
            }
            if (editPart instanceof SubjobContainerPart) {
                editPart.refresh();
            }
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.gef.editparts.AbstractEditPart#createEditPolicies()
     */
    @Override
    public void createEditPolicies() {
        // installEditPolicy(EditPolicy.DIRECT_EDIT_ROLE, new NodeTextEditPolicy());
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.gef.editparts.AbstractGraphicalEditPart#getDragTracker(org.eclipse.gef.Request)
     */
    @Override
    public DragTracker getDragTracker(final Request request) {
        return new NodeTextTracker(this, this.getParent());
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.gef.editparts.AbstractEditPart#refreshVisuals()
     */
    @Override
    protected void refreshVisuals() {
        Node node = ((NodeContainer) ((NodeContainerPart) getParent()).getModel()).getNode();
        NodeLabel nodeLabel = node.getNodeLabel();
        NodeErrorFigure errorFig = (NodeErrorFigure) this.getFigure();
        Point loc = node.getLocation().getCopy();
        Dimension size = errorFig.getSize();
        loc.x = loc.x + (node.getSize().width - size.width) / 2;
        loc.y = loc.y + node.getSize().height + (nodeLabel.getLabelSize().height);
        Rectangle rectangle = new Rectangle(loc, size);
        ((GraphicalEditPart) getParent()).setLayoutConstraint(this, getFigure(), rectangle);

    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.gef.editparts.AbstractEditPart#performRequest(org.eclipse.gef.Request)
     */
    @Override
    public void performRequest(final Request request) {
        if (request.getType().equals("open")) { //$NON-NLS-1$
            Node node = ((NodeContainer) ((NodeContainerPart) getParent()).getModel()).getNode();
            if (node.isErrorFlag()) {
                ErrorMessageDialog dialog = new ErrorMessageDialog(DisplayUtils.getDefaultShell(false), node);
                dialog.open();
            }
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.gef.editparts.AbstractEditPart#setSelected(int)
     */
    @Override
    @SuppressWarnings("unchecked")//$NON-NLS-1$
    public void setSelected(final int value) {
        if (value != SELECTED_NONE) {
            List<EditPart> listEditParts = this.getViewer().getSelectedEditParts();
            if (listEditParts.size() != 1) {
                // getParent().removeEditPolicy(EditPolicy.LAYOUT_ROLE);
            } else {
                // getParent().installEditPolicy(EditPolicy.LAYOUT_ROLE, new NodeContainerLayoutEditPolicy());
                super.setSelected(value);
            }
        } else {
            getParent().removeEditPolicy(EditPolicy.LAYOUT_ROLE);
            super.setSelected(value);
        }
    }

    @Override
    public boolean isSelectable() {
        return (!((NodeError) getModel()).getNode().isReadOnly()) || (this.getViewer().getSelection().isEmpty());
    }

}
