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
import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPolicy;
import org.eclipse.gef.GraphicalEditPart;
import org.eclipse.gef.NodeEditPart;
import org.eclipse.gef.Request;
import org.eclipse.gef.RequestConstants;
import org.eclipse.gef.requests.DirectEditRequest;
import org.talend.designer.gefabstractmap.figures.anchors.FilterTreeAnchor;
import org.talend.designer.gefabstractmap.figures.cells.IWidgetCell;
import org.talend.designer.gefabstractmap.figures.treesettings.FilterTextArea;
import org.talend.designer.gefabstractmap.part.InputTablePart;
import org.talend.designer.gefabstractmap.part.directedit.XmlMapNodeCellEditorLocator;
import org.talend.designer.xmlmap.editor.XmlMapGraphicViewer;
import org.talend.designer.xmlmap.figures.GlobalMapKeysEntityFigure;
import org.talend.designer.xmlmap.figures.InputXmlTreeFigure;
import org.talend.designer.xmlmap.figures.OutputXmlTreeFigure;
import org.talend.designer.xmlmap.figures.table.GlobalMapContainer;
import org.talend.designer.xmlmap.figures.table.XmlMapTableManager;
import org.talend.designer.xmlmap.figures.treeNode.XmlmapTreeNodeFigure;
import org.talend.designer.xmlmap.model.emf.xmlmap.AbstractInOutTree;
import org.talend.designer.xmlmap.model.emf.xmlmap.GlobalMapNode;
import org.talend.designer.xmlmap.model.emf.xmlmap.InputXmlTree;
import org.talend.designer.xmlmap.model.emf.xmlmap.OutputTreeNode;
import org.talend.designer.xmlmap.model.emf.xmlmap.OutputXmlTree;
import org.talend.designer.xmlmap.model.emf.xmlmap.XmlmapPackage;
import org.talend.designer.xmlmap.parts.directedit.XmlMapNodeDirectEditManager;
import org.talend.designer.xmlmap.policy.DragAndDropEditPolicy;
import org.talend.designer.xmlmap.policy.XmlDirectEditPolicy;
import org.talend.designer.xmlmap.ui.tabs.MapperManager;
import org.talend.designer.xmlmap.util.XmlMapUtil;

/**
 * wchen class global comment. Detailled comment
 */
public class InputXmlTreeEditPart extends InputTablePart implements NodeEditPart {

    private InputXmlTreeFigure figure;

    private XmlMapNodeDirectEditManager directEditManager;

    private XmlMapTableManager manager;

    @Override
    protected IFigure createFigure() {
        figure = new InputXmlTreeFigure(manager);
        return figure;
    }

    @Override
    public void setModel(Object model) {
        super.setModel(model);
        manager = new XmlMapTableManager((InputXmlTree) model, this);
    }

    @Override
    public ConnectionAnchor getSourceConnectionAnchor(ConnectionEditPart connection) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ConnectionAnchor getTargetConnectionAnchor(ConnectionEditPart connection) {
        if (connection instanceof XmlMapFilterConnectionPart) {
            return new FilterTreeAnchor(getFigure(), connection, manager);
        }
        return null;
    }

    @Override
    public ConnectionAnchor getSourceConnectionAnchor(Request request) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ConnectionAnchor getTargetConnectionAnchor(Request request) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    protected void createEditPolicies() {
        // installEditPolicy(EditPolicy.SELECTION_FEEDBACK_ROLE, new NonResizableEditPolicy());
        installEditPolicy(EditPolicy.DIRECT_EDIT_ROLE, new XmlDirectEditPolicy());
        installEditPolicy("Drag and Drop", new DragAndDropEditPolicy());
    }

    @Override
    protected List getModelTargetConnections() {
        return ((AbstractInOutTree) getModel()).getFilterIncomingConnections();
    }

    @Override
    public IFigure getContentPane() {
        return (InputXmlTreeFigure) getFigure();
    }

    @Override
    protected List getModelChildren() {
        List list = new ArrayList();
        list.addAll(((InputXmlTree) getModel()).getGlobalMapKeysValues());
        list.addAll(((InputXmlTree) getModel()).getNodes());
        return list;
    }

    @Override
    protected void addChildVisual(EditPart childEditPart, int index) {
        IFigure child = ((GraphicalEditPart) childEditPart).getFigure();
        if (childEditPart instanceof TreeNodeEditPart) {
            ((InputXmlTreeFigure) getFigure()).getColumnContainer().add(child);
        } else if (childEditPart instanceof GlobalMapNodeEditPart) {
            GlobalMapContainer globalMapContainer = (GlobalMapContainer) ((InputXmlTreeFigure) getFigure())
                    .getGlobalMapContainer();
            if (globalMapContainer != null && globalMapContainer.getTableFigure() != null) {
                globalMapContainer.getTableFigure().getTableItemContainer().add(child);
            }
        } else {
            super.addChildVisual(childEditPart, index);
        }
    }

    @Override
    protected void removeChildVisual(EditPart childEditPart) {
        IFigure child = ((GraphicalEditPart) childEditPart).getFigure();
        if (childEditPart instanceof TreeNodeEditPart) {
            ((InputXmlTreeFigure) getFigure()).getColumnContainer().remove(child);
        } else if (childEditPart instanceof GlobalMapNodeEditPart) {
            GlobalMapContainer globalMapContainer = (GlobalMapContainer) ((InputXmlTreeFigure) getFigure())
                    .getGlobalMapContainer();
            if (globalMapContainer != null && globalMapContainer.getTableFigure() != null) {
                globalMapContainer.getTableFigure().getTableItemContainer().remove(child);
                if (child instanceof GlobalMapKeysEntityFigure) {
                    MapperManager manager = ((XmlMapGraphicViewer) childEditPart.getViewer()).getMapperManager();
                    GlobalMapNode globalMapNode = ((GlobalMapKeysEntityFigure) child).getGlobalMapNode();
                    if (manager != null && globalMapNode != null) {
                        XmlMapUtil.detachLookupSource(globalMapNode, manager.getExternalData());
                    }
                }
            }
        } else {
            super.removeChildVisual(childEditPart);
        }
    }

    @Override
    public void notifyChanged(Notification notification) {
        int type = notification.getEventType();
        int featureId = notification.getFeatureID(XmlmapPackage.class);
        switch (type) {
        case Notification.ADD:
        case Notification.REMOVE:
        case Notification.REMOVE_MANY:
            switch (featureId) {
            case XmlmapPackage.INPUT_XML_TREE__NODES:
            case XmlmapPackage.INPUT_XML_TREE__GLOBAL_MAP_KEYS_VALUES:
                refreshChildren();
                break;
            case XmlmapPackage.ABSTRACT_IN_OUT_TREE__FILTER_INCOMING_CONNECTIONS:
                refreshTargetConnections();
                break;
            }
            break;
        case Notification.SET:
            switch (featureId) {
            case XmlmapPackage.INPUT_XML_TREE__NODES:
            case XmlmapPackage.INPUT_XML_TREE__GLOBAL_MAP_KEYS_VALUES:
                refreshChildren();
                break;
            case XmlmapPackage.INPUT_XML_TREE__ACTIVATE_CONDENSED_TOOL:
                getFigure().validate();
                break;
            case XmlmapPackage.INPUT_XML_TREE__LOOKUP_MODE:
            case XmlmapPackage.INPUT_XML_TREE__MATCHING_MODE:
            case XmlmapPackage.INPUT_XML_TREE__INNER_JOIN:
            case XmlmapPackage.INPUT_XML_TREE__PERSISTENT:
            case XmlmapPackage.INPUT_XML_TREE__MINIMIZED:
            case XmlmapPackage.INPUT_XML_TREE__EXPRESSION_FILTER:
                ((InputXmlTreeFigure) getFigure()).update(featureId);
                break;
            case XmlmapPackage.ABSTRACT_IN_OUT_TREE__MULTI_LOOPS:
                if (getParent() instanceof XmlMapDataEditPart) {
                    List childPart = ((XmlMapDataEditPart) getParent()).getChildren();
                    for (Object o : childPart) {
                        if (o instanceof OutputXmlTreeEditPart) {
                            OutputXmlTreeEditPart outputPart = (OutputXmlTreeEditPart) o;
                            boolean hasDocument = XmlMapUtil.hasDocument((OutputXmlTree) outputPart.getModel());
                            if (!hasDocument) {
                                OutputXmlTreeFigure outputTreeFigure = (OutputXmlTreeFigure) outputPart.getFigure();
                                outputTreeFigure.update(XmlmapPackage.ABSTRACT_IN_OUT_TREE__MULTI_LOOPS);
                            } else {
                                List nodeChild = outputPart.getChildren();
                                for (Object nc : nodeChild) {
                                    if (nc instanceof OutputTreeNodeEditPart) {
                                        refreshOutputNodeLoopFunctionBtn((OutputTreeNodeEditPart) nc);
                                    }
                                }
                            }
                        }
                    }
                    boolean changed = clearOutputAggregateIfNeeded();
                    if (changed) {
                        ((XmlMapDataEditPart) getParent()).refresh();
                    }
                    ((XmlMapGraphicViewer) getViewer()).getMapperManager().getProblemsAnalyser().checkProblems();
                    ((XmlMapGraphicViewer) getViewer()).getMapperManager().getMapperUI().updateStatusBar();
                }
            }
        }
    }

    protected boolean clearOutputAggregateIfNeeded() {
        // clear aggregate in output table if input main and output are both multiloops
        if (((InputXmlTree) getModel()).isMultiLoops() && getParent() instanceof XmlMapDataEditPart) {
            List childPart = ((XmlMapDataEditPart) getParent()).getChildren();
            for (Object o : childPart) {
                if (o instanceof OutputXmlTreeEditPart) {
                    OutputXmlTree outputModel = (OutputXmlTree) ((OutputXmlTreeEditPart) o).getModel();
                    if (outputModel.isMultiLoops()) {
                        return XmlMapUtil.cleanAggregate(outputModel.getNodes());
                    }
                }
            }
        }
        return false;
    }

    private void refreshOutputNodeLoopFunctionBtn(OutputTreeNodeEditPart outputNodePart) {
        OutputTreeNode outputNode = (OutputTreeNode) outputNodePart.getModel();
        if (outputNode.isLoop()) {
            XmlmapTreeNodeFigure nodeFigure = (XmlmapTreeNodeFigure) outputNodePart.getFigure();
            if (nodeFigure != null) {
                nodeFigure.getBranchContent().updateLoopButtonFigure();
            }
        } else if (!outputNodePart.getChildren().isEmpty()) {
            for (Object nc : outputNodePart.getChildren()) {
                if (nc instanceof OutputTreeNodeEditPart) {
                    refreshOutputNodeLoopFunctionBtn((OutputTreeNodeEditPart) nc);
                }
            }
        }
    }

    @Override
    public void performRequest(Request req) {
        if (RequestConstants.REQ_DIRECT_EDIT.equals(req.getType())) {
            DirectEditRequest drequest = (DirectEditRequest) req;
            Point figureLocation = drequest.getLocation();
            IFigure findFigureAt = getFigure().findFigureAt(figureLocation.x, figureLocation.y);
            if (findFigureAt != null && findFigureAt instanceof IWidgetCell) {
                directEditManager = new XmlMapNodeDirectEditManager(this, new XmlMapNodeCellEditorLocator((Figure) findFigureAt));
                directEditManager.show();
            }
            if (directEditManager != null) {
                if (findFigureAt != null && findFigureAt instanceof FilterTextArea) {
                    if (figure.containsPoint(figureLocation)) {
                        directEditManager.show();
                        ((XmlMapGraphicViewer) getViewer()).getMapperManager().setCurrentDirectEditManager(directEditManager);
                    }
                }
            }
        }
    }

    @Override
    public void highLightHeader(boolean highLight) {
        ((InputXmlTreeFigure) getFigure()).highLightHeader(highLight);
    }

}
