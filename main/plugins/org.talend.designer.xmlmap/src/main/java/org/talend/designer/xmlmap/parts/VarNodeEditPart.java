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
package org.talend.designer.xmlmap.parts;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.draw2d.ConnectionAnchor;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.ImageFigure;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.emf.common.notify.Notification;
import org.eclipse.gef.ConnectionEditPart;
import org.eclipse.gef.EditPolicy;
import org.eclipse.gef.NodeEditPart;
import org.eclipse.gef.Request;
import org.eclipse.gef.RequestConstants;
import org.eclipse.gef.requests.DirectEditRequest;
import org.talend.commons.ui.runtime.image.EImage;
import org.talend.commons.ui.runtime.image.ImageProvider;
import org.talend.designer.gefabstractmap.figures.anchors.VarColumnAnchor;
import org.talend.designer.gefabstractmap.figures.var.VarEntityFigure;
import org.talend.designer.gefabstractmap.part.TableEntityPart;
import org.talend.designer.gefabstractmap.part.directedit.XmlMapNodeCellEditorLocator;
import org.talend.designer.gefabstractmap.policy.RowSelectionEditPolicy;
import org.talend.designer.xmlmap.editor.XmlMapGraphicViewer;
import org.talend.designer.xmlmap.figures.varnode.VarEntityManager;
import org.talend.designer.xmlmap.figures.varnode.VarNodeFigure;
import org.talend.designer.xmlmap.model.emf.xmlmap.VarNode;
import org.talend.designer.xmlmap.model.emf.xmlmap.XmlmapPackage;
import org.talend.designer.xmlmap.parts.directedit.XmlMapNodeDirectEditManager;
import org.talend.designer.xmlmap.policy.DragAndDropEditPolicy;
import org.talend.designer.xmlmap.policy.XmlDirectEditPolicy;

/**
 * DOC hywang class global comment. Detailled comment
 */
public class VarNodeEditPart extends TableEntityPart implements NodeEditPart {

    private VarEntityFigure varNodeFigure;

    private XmlMapNodeDirectEditManager directEditManager;

    private VarEntityManager manager;

    @Override
    protected IFigure createFigure() {
        varNodeFigure = new VarNodeFigure(manager);
        return varNodeFigure;
    }

    @Override
    public void setModel(Object model) {
        super.setModel(model);
        manager = new VarEntityManager((VarNode) model, this);
    }

    @Override
    public void createEditPolicies() {
        installEditPolicy(EditPolicy.SELECTION_FEEDBACK_ROLE, new RowSelectionEditPolicy());
        installEditPolicy("Drag and Drop", new DragAndDropEditPolicy());
        installEditPolicy(EditPolicy.DIRECT_EDIT_ROLE, new XmlDirectEditPolicy());
    }

    @Override
    public void performRequest(Request req) {
        IFigure figure = null;
        Figure selectedFigure = null;
        if (RequestConstants.REQ_DIRECT_EDIT.equals(req.getType())) {
            DirectEditRequest drequest = (DirectEditRequest) req;
            Point figureLocation = drequest.getLocation();
            if (getFigure() instanceof VarEntityFigure) {
                figure = ((VarEntityFigure) getFigure()).findFigureAt(figureLocation);
            }
            // if (directEditManager == null) {
            // if (figure != null ) {
            if (figure != null) {
                // if (figure instanceof VariableContainerFigure) {
                // figure = ((VariableContainerFigure) figure).getVariableLabel();
                // }
                selectedFigure = (Figure) figure;
                directEditManager = new XmlMapNodeDirectEditManager(this, new XmlMapNodeCellEditorLocator(selectedFigure));
            }
            // }
            if (directEditManager != null) {
                directEditManager.show();
                ((XmlMapGraphicViewer) getViewer()).getMapperManager().setCurrentDirectEditManager(directEditManager);
            }
        }
    }

    @Override
    protected List getModelSourceConnections() {
        List modelSourceConnection = new ArrayList();
        modelSourceConnection.addAll(((VarNode) getModel()).getOutgoingConnections());
        modelSourceConnection.addAll(((VarNode) getModel()).getFilterOutGoingConnections());
        return modelSourceConnection;
    }

    @Override
    protected List getModelTargetConnections() {
        return ((VarNode) getModel()).getIncomingConnections();
    }

    @Override
    public ConnectionAnchor getSourceConnectionAnchor(ConnectionEditPart connection) {
        return new VarColumnAnchor(manager, getFigure(), true);
    }

    @Override
    public ConnectionAnchor getTargetConnectionAnchor(ConnectionEditPart connection) {
        return new VarColumnAnchor(manager, getFigure(), false);
    }

    @Override
    public ConnectionAnchor getSourceConnectionAnchor(Request request) {
        return new VarColumnAnchor(manager, getFigure(), true);
    }

    @Override
    public ConnectionAnchor getTargetConnectionAnchor(Request request) {
        return new VarColumnAnchor(manager, getFigure(), false);
    }

    @Override
    public void notifyChanged(Notification notification) {
        int type = notification.getEventType();
        int featureId = notification.getFeatureID(XmlmapPackage.class);
        switch (type) {
        case Notification.SET:
            switch (featureId) {
            case XmlmapPackage.VAR_NODE__EXPRESSION:
                ((VarEntityFigure) getFigure()).getExpression().setText(((VarNode) getModel()).getExpression());
                break;
            case XmlmapPackage.VAR_NODE__NULLABLE:
                boolean newBoolean = notification.getNewBooleanValue();
                ImageFigure checkImage = ((VarEntityFigure) getFigure()).getCheckImage();
                if (newBoolean) {
                    checkImage.setImage(ImageProvider.getImage(EImage.CHECKED_ICON));
                } else if (!newBoolean) {
                    checkImage.setImage(ImageProvider.getImage(EImage.UNCHECKED_ICON));
                }
                break;
            case XmlmapPackage.VAR_NODE__NAME:
                ((VarEntityFigure) getFigure()).getVariableLabel().setText(((VarNode) getModel()).getName());
                break;
            case XmlmapPackage.VAR_NODE__TYPE:
                ((VarEntityFigure) getFigure()).updateVarNodeType(((VarNode) getModel()).getType(),
                        ((VarNode) getModel()).isNullable());
                // refreshChildren();
                // refreshVisuals();
                break;
            default:
                // refreshChildren();
                // refreshVisuals();
            }
        case Notification.ADD:
            switch (featureId) {
            case XmlmapPackage.VAR_NODE__INCOMING_CONNECTIONS:
                refreshTargetConnections();
                break;
            case XmlmapPackage.VAR_NODE__OUTGOING_CONNECTIONS:
            case XmlmapPackage.VAR_NODE__FILTER_OUT_GOING_CONNECTIONS:
                refreshSourceConnections();
                break;
            }
        case Notification.REMOVE:
        case Notification.REMOVE_MANY:
            switch (featureId) {
            case XmlmapPackage.VAR_NODE__INCOMING_CONNECTIONS:
                refreshTargetConnections();
                break;
            case XmlmapPackage.VAR_NODE__OUTGOING_CONNECTIONS:
            case XmlmapPackage.VAR_NODE__FILTER_OUT_GOING_CONNECTIONS:
                refreshSourceConnections();
                break;
            }
        }

    }

    public XmlMapDataEditPart getMapDataEditPart() {
        List children2 = getViewer().getRootEditPart().getChildren();
        if (children2.size() == 1 && children2.get(0) instanceof XmlMapDataEditPart) {
            return (XmlMapDataEditPart) children2.get(0);
        }
        return null;
    }
}
