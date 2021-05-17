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
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.emf.common.notify.Notification;
import org.eclipse.gef.ConnectionEditPart;
import org.eclipse.gef.EditPolicy;
import org.eclipse.gef.NodeEditPart;
import org.eclipse.gef.Request;
import org.eclipse.gef.RequestConstants;
import org.eclipse.gef.requests.DirectEditRequest;
import org.talend.designer.gefabstractmap.figures.anchors.LookupColumnAnchor;
import org.talend.designer.gefabstractmap.figures.table.entity.TableTreeEntityFigure;
import org.talend.designer.gefabstractmap.part.TableEntityPart;
import org.talend.designer.gefabstractmap.part.directedit.XmlMapNodeCellEditorLocator;
import org.talend.designer.gefabstractmap.policy.RowSelectionEditPolicy;
import org.talend.designer.xmlmap.editor.XmlMapGraphicViewer;
import org.talend.designer.xmlmap.figures.GlobalMapKeysEntityFigure;
import org.talend.designer.xmlmap.figures.treeNode.GlobalMapNodeEntityManager;
import org.talend.designer.xmlmap.model.emf.xmlmap.GlobalMapNode;
import org.talend.designer.xmlmap.model.emf.xmlmap.XmlmapPackage;
import org.talend.designer.xmlmap.parts.directedit.XmlMapNodeDirectEditManager;
import org.talend.designer.xmlmap.policy.DragAndDropEditPolicy;
import org.talend.designer.xmlmap.policy.XmlDirectEditPolicy;

/**
 * DOC hcyi class global comment. Detailled comment
 */
public class GlobalMapNodeEditPart extends TableEntityPart implements NodeEditPart {

    private XmlMapNodeDirectEditManager directEditManager;

    private GlobalMapNodeEntityManager entityManger;

    @Override
    protected IFigure createFigure() {
        GlobalMapKeysEntityFigure globalMaEntityFigure = new GlobalMapKeysEntityFigure(entityManger);
        return globalMaEntityFigure;
    }

    @Override
    public void setModel(Object model) {
        super.setModel(model);
        entityManger = new GlobalMapNodeEntityManager((GlobalMapNode) model, this);
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
            if (getFigure() instanceof GlobalMapKeysEntityFigure) {
                figure = ((GlobalMapKeysEntityFigure) getFigure()).findFigureAt(figureLocation);
            }
            if (figure != null) {
                selectedFigure = (Figure) figure;
                directEditManager = new XmlMapNodeDirectEditManager(this, new XmlMapNodeCellEditorLocator(selectedFigure));
            }
            if (directEditManager != null) {
                directEditManager.show();
                ((XmlMapGraphicViewer) getViewer()).getMapperManager().setCurrentDirectEditManager(directEditManager);
            }
        }
    }

    @Override
    protected List getModelSourceConnections() {
        GlobalMapNode model = (GlobalMapNode) getModel();
        List list = new ArrayList();
        list.addAll(model.getLookupOutgoingConnections());
        return list;

    }

    @Override
    protected List getModelTargetConnections() {
        GlobalMapNode model = (GlobalMapNode) getModel();
        return model.getLookupIncomingConnections();
    }

    @Override
    public IFigure getContentPane() {
        return ((TableTreeEntityFigure) getFigure()).getContents();
    }

    @Override
    public ConnectionAnchor getSourceConnectionAnchor(ConnectionEditPart connection) {
        if (connection instanceof XmlMapLookupConnectionPart) {
            return new LookupColumnAnchor(getFigure(), connection, entityManger);
        }
        return null;
    }

    @Override
    public ConnectionAnchor getTargetConnectionAnchor(ConnectionEditPart connection) {
        if (connection instanceof XmlMapLookupConnectionPart) {
            return new LookupColumnAnchor(getFigure(), connection, entityManger);
        }
        return null;
    }

    @Override
    public ConnectionAnchor getSourceConnectionAnchor(Request request) {
        return null;
    }

    @Override
    public ConnectionAnchor getTargetConnectionAnchor(Request request) {
        return null;
    }

    @Override
    public List getModelChildren() {
        GlobalMapNode model = (GlobalMapNode) getModel();
        return model.getChildren();
    }

    @Override
    public void notifyChanged(Notification notification) {
        int type = notification.getEventType();
        int featureId = notification.getFeatureID(XmlmapPackage.class);
        switch (type) {
        case Notification.SET:
            switch (featureId) {
            case XmlmapPackage.GLOBAL_MAP_NODE__EXPRESSION:
                ((GlobalMapKeysEntityFigure) getFigure()).getExpression().setText(((GlobalMapNode) getModel()).getExpression());
                break;
            case XmlmapPackage.GLOBAL_MAP_NODE__NAME:
                ((GlobalMapKeysEntityFigure) getFigure()).getGlobalMapKeyFigure().setText(((GlobalMapNode) getModel()).getName());
                break;
            default:
            }
        case Notification.ADD:
        case Notification.ADD_MANY:
            switch (featureId) {
            case XmlmapPackage.GLOBAL_MAP_NODE__LOOKUP_OUTGOING_CONNECTIONS:
                refreshSourceConnections();
                break;
            case XmlmapPackage.GLOBAL_MAP_NODE__LOOKUP_INCOMING_CONNECTIONS:
                refreshTargetConnections();
                break;
            }
        case Notification.REMOVE:
        case Notification.REMOVE_MANY:
            switch (featureId) {
            case XmlmapPackage.GLOBAL_MAP_NODE__LOOKUP_OUTGOING_CONNECTIONS:
                refreshSourceConnections();
                break;
            case XmlmapPackage.GLOBAL_MAP_NODE__LOOKUP_INCOMING_CONNECTIONS:
                refreshTargetConnections();
                break;
            }
        }
    }
}
