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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.DragTracker;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPolicy;
import org.eclipse.gef.GraphicalEditPart;
import org.eclipse.gef.Request;
import org.eclipse.gef.editparts.AbstractGraphicalEditPart;
import org.talend.designer.core.model.components.EParameterName;
import org.talend.designer.core.ui.editor.nodecontainer.NodeContainer;
import org.talend.designer.core.ui.editor.nodecontainer.NodeContainerPart;
import org.talend.designer.core.ui.editor.process.ProcessPart;
import org.talend.designer.core.ui.editor.subjobcontainer.SubjobContainerPart;

/**
 * DOC Administrator class global comment. Detailled comment
 */
public class NodeProgressBarPart extends AbstractGraphicalEditPart implements PropertyChangeListener {

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.gef.editparts.AbstractGraphicalEditPart#activate()
     */
    @Override
    public void activate() {
        if (!isActive()) {
            super.activate();
            ((NodeProgressBar) getModel()).addPropertyChangeListener(this);
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
            ((NodeProgressBar) getModel()).removePropertyChangeListener(this);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.gef.editparts.AbstractGraphicalEditPart#createFigure()
     */
    @Override
    public IFigure createFigure() {
        Node node = ((NodeContainer) ((NodeContainerPart) getParent()).getModel()).getNode();
        NodeProgressBarFigure progressFig = new NodeProgressBarFigure(node);

        if (((NodeProgressBar) getModel()).isActivate()) {
            (progressFig).setAlpha(-1);
        } else {
            (progressFig).setAlpha(NodeProgressBar.ALPHA_VALUE);
        }
        ((NodeProgressBar) getModel()).setProgressSize(progressFig.getSize());

        return progressFig;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
     */
    public void propertyChange(final PropertyChangeEvent evt) {
        String request = evt.getPropertyName();
        Double extent = new Double(0);
        if (evt.getNewValue() instanceof Double) {
            extent = (Double) evt.getNewValue();
        }

        if (request.equals("UPDATE_STATUS")) { //$NON-NLS-1$
            NodeProgressBarFigure figure = (NodeProgressBarFigure) this.getFigure();
            Node node = ((NodeContainer) ((NodeContainerPart) getParent()).getModel()).getNode();
            // figure.updateVisible(true);
            figure.setProgressData(extent);
            ((NodeProgressBar) getModel()).setProgressSize((figure).getSize());
            refreshVisuals();
        }

        if (request.equals(NodeProgressBar.LOCATION)) { //$NON-NLS-1$
            refreshVisuals();
            getParent().refresh();
        }
        if (request.equals(EParameterName.ACTIVATE.getName())) {
            if (((NodeProgressBar) getModel()).isActivate()) {
                ((NodeProgressBarFigure) figure).setAlpha(-1);
                ((NodeProgressBarFigure) figure).repaint();
                refreshVisuals();
            } else {
                ((NodeProgressBarFigure) figure).setAlpha(Node.ALPHA_VALUE);
                ((NodeProgressBarFigure) figure).repaint();
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
        NodeProgressBarFigure progressFig = (NodeProgressBarFigure) this.getFigure();

        Point loc = node.getLocation().getCopy();
        NodeError nodeError = node.getNodeError();
        Dimension size = progressFig.getSize();
        loc.x = loc.x + (node.getSize().width - size.width) / 2 + size.width / 7;
        loc.y = loc.y + node.getSize().height + nodeLabel.getLabelSize().height + nodeError.getErrorSize().height;
        Rectangle rectangle = new Rectangle(loc, size);
        ((GraphicalEditPart) getParent()).setLayoutConstraint(this, getFigure(), rectangle);

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
        return (!((NodeProgressBar) getModel()).getNode().isReadOnly()) || (this.getViewer().getSelection().isEmpty());
    }

}
