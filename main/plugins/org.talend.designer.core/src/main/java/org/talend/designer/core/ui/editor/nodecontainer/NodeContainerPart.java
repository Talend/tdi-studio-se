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
package org.talend.designer.core.ui.editor.nodecontainer;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.CompoundSnapToHelper;
import org.eclipse.gef.EditPolicy;
import org.eclipse.gef.GraphicalEditPart;
import org.eclipse.gef.SnapToGrid;
import org.eclipse.gef.SnapToHelper;
import org.eclipse.gef.editparts.AbstractGraphicalEditPart;
import org.eclipse.gef.rulers.RulerProvider;
import org.talend.core.model.process.IElementParameter;
import org.talend.designer.core.model.components.EParameterName;
import org.talend.designer.core.ui.editor.nodes.Node;
import org.talend.designer.core.ui.editor.nodes.NodePart;
import org.talend.designer.core.ui.editor.process.NodeSnapToGeometry;

/**
 * EditPart linked to the NodeContainer.
 *
 * $Id$
 *
 */
public class NodeContainerPart extends AbstractGraphicalEditPart implements PropertyChangeListener, IAdaptable {

    @Override
    protected void unregisterVisuals() {
        super.unregisterVisuals();
        if (getFigure() instanceof NodeContainerFigure) {
            ((NodeContainerFigure) getFigure()).disposeColors();
        }
    }

    @Override
    public boolean isSelectable() {
        return false;
    }

    public void activate() {
        if (!isActive()) {
            super.activate();
            Node node = ((NodeContainer) getModel()).getNode();
            node.addPropertyChangeListener(this);
        }
    }

    public void deactivate() {
        if (isActive()) {
            super.deactivate();
            Node node = ((NodeContainer) getModel()).getNode();
            node.removePropertyChangeListener(this);
        }
    }

    @Override
    public void setSelected(int value) {
        super.setSelected(SELECTED_NONE);
    }

    public NodePart getNodePart() {
        Object o = this.getChildren().get(0);
        if (o instanceof NodePart) {
            return (NodePart) o;
        }
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.gef.editparts.AbstractGraphicalEditPart#createFigure()
     */
    @Override
    protected IFigure createFigure() {
        NodeContainerFigure nodeContainerFigure = new NodeContainerFigure((NodeContainer) this.getModel());
        Node node = ((NodeContainer) getModel()).getNode();
        if (node.isActivate()) {
            nodeContainerFigure.setAlpha(-1);
        } else {
            nodeContainerFigure.setAlpha(Node.ALPHA_VALUE);
        }
        IElementParameter param = node.getElementParameter(EParameterName.INFORMATION.getName());
        if (param != null) {
            boolean showInfoFlag = Boolean.TRUE.equals(param.getValue());
            nodeContainerFigure.updateStatus(node.getStatus(), showInfoFlag);
            nodeContainerFigure.setInfoHint(node.getShowHintText());
        }
        return nodeContainerFigure;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.gef.editparts.AbstractEditPart#createEditPolicies()
     */
    @Override
    protected void createEditPolicies() {
        installEditPolicy(EditPolicy.LAYOUT_ROLE, new NodeContainerLayoutEditPolicy());
    }

    protected void refreshVisuals() {
        Rectangle rectangle = ((NodeContainer) this.getModel()).getNodeContainerRectangle();
        Rectangle cleanRectangle = ((NodeContainer) this.getModel()).getNodeMarkRectangle();
        ((GraphicalEditPart) getParent()).setLayoutConstraint(this, getFigure(), rectangle);
        ((NodeContainerFigure) getFigure()).initializeNodeContainer(cleanRectangle);
    }

    protected List getModelChildren() {
        return ((NodeContainer) this.getModel()).getElements();
    }

    /*
     * (non-Javadoc)
     *
     * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
     */
    public void propertyChange(PropertyChangeEvent changeEvent) {
        if (changeEvent.getPropertyName().equals(EParameterName.VALIDATION_RULES.getName())) {
            Node node = ((NodeContainer) getModel()).getNode();
            ((NodeContainerFigure) this.getFigure()).updateValidationRuleFigure(node.isHasValidationRule());
            refreshVisuals();
        }
        if (changeEvent.getPropertyName().equals(EParameterName.HINT.getName())) {
            Node node = ((NodeContainer) getModel()).getNode();
            ((NodeContainerFigure) figure).setInfoHint(node.getShowHintText());
        }
        if (changeEvent.getPropertyName().equals(Node.UPDATE_STATUS)) {
            Node node = ((NodeContainer) getModel()).getNode();
            ((NodeContainerFigure) this.getFigure()).updateErrorFlag(node.isErrorFlag());
            ((NodeContainerFigure) this.getFigure()).setShowCompareMark(node.isCompareFlag() && !node.isErrorFlag());

            IElementParameter param = node.getElementParameter(EParameterName.INFORMATION.getName());
            if (param != null) {
                boolean showInfoFlag = Boolean.TRUE.equals(param.getValue());
                if (changeEvent.getNewValue() instanceof Integer) {
                    Integer status = (Integer) changeEvent.getNewValue();
                    if (status != null) {
                        ((NodeContainerFigure) this.getFigure()).updateStatus(status, showInfoFlag);
                    }
                    ((NodeContainerFigure) this.getFigure()).setInfoHint(node.getShowHintText());
                }
                refreshVisuals();
            }
        }
        if (changeEvent.getPropertyName().equals(EParameterName.ACTIVATE.getName())) {
            Node node = ((NodeContainer) getModel()).getNode();
            if (node.isActivate()) {
                ((NodeContainerFigure) figure).setAlpha(-1);
                ((NodeContainerFigure) figure).repaint();
                refreshVisuals();
            } else {
                ((NodeContainerFigure) figure).setAlpha(Node.ALPHA_VALUE);
                ((NodeContainerFigure) figure).repaint();
                refreshVisuals();
            }
        }
        if (changeEvent.getPropertyName().equals(Node.PERFORMANCE_DATA)) {
            refreshVisuals();
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.gef.editparts.AbstractGraphicalEditPart#getAdapter(java.lang.Class)
     */
    @Override
    public Object getAdapter(Class key) {
        if (key == SnapToHelper.class) {
            List<Object> snapStrategies = new ArrayList<Object>();
            Boolean val = (Boolean) getViewer().getProperty(RulerProvider.PROPERTY_RULER_VISIBILITY);

            val = (Boolean) getViewer().getProperty(NodeSnapToGeometry.PROPERTY_SNAP_ENABLED);
            if (val != null && val.booleanValue()) {
                snapStrategies.add(new NodeSnapToGeometry(this));
            }

            val = (Boolean) getViewer().getProperty(SnapToGrid.PROPERTY_GRID_ENABLED);
            if (val != null && val.booleanValue()) {
                snapStrategies.add(new SnapToGrid(this));
            }

            if (snapStrategies.size() == 0) {
                return null;
            }
            if (snapStrategies.size() == 1) {
                return snapStrategies.get(0);
            }

            SnapToHelper[] ss = new SnapToHelper[snapStrategies.size()];
            for (int i = 0; i < snapStrategies.size(); i++) {
                ss[i] = (SnapToHelper) snapStrategies.get(i);
            }
            return new CompoundSnapToHelper(ss);
        }

        return super.getAdapter(key);
    }
}
