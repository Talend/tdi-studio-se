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
import org.eclipse.emf.common.notify.Adapter;
import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.notify.Notifier;
import org.eclipse.gef.ConnectionEditPart;
import org.eclipse.gef.EditPartViewer;
import org.eclipse.gef.EditPolicy;
import org.eclipse.gef.NodeEditPart;
import org.eclipse.gef.Request;
import org.eclipse.gef.RequestConstants;
import org.eclipse.gef.requests.DirectEditRequest;
import org.talend.designer.gefabstractmap.figures.anchors.FilterTreeAnchor;
import org.talend.designer.gefabstractmap.figures.cells.IWidgetCell;
import org.talend.designer.gefabstractmap.figures.treesettings.FilterTextArea;
import org.talend.designer.gefabstractmap.part.OutputTablePart;
import org.talend.designer.gefabstractmap.part.directedit.XmlMapNodeCellEditorLocator;
import org.talend.designer.xmlmap.editor.XmlMapGraphicViewer;
import org.talend.designer.xmlmap.figures.OutputXmlTreeFigure;
import org.talend.designer.xmlmap.figures.table.XmlMapTableManager;
import org.talend.designer.xmlmap.figures.treeNode.XmlmapTreeNodeFigure;
import org.talend.designer.xmlmap.model.emf.xmlmap.AbstractInOutTree;
import org.talend.designer.xmlmap.model.emf.xmlmap.InputLoopNodesTable;
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
public class OutputXmlTreeEditPart extends OutputTablePart implements NodeEditPart {

    private OutputXmlTreeFigure figure;

    private XmlMapNodeDirectEditManager directEditManager;

    XmlMapTableManager manager;

    @Override
    protected IFigure createFigure() {
        figure = new OutputXmlTreeFigure(manager);
        return figure;
    }

    @Override
    public IFigure getContentPane() {
        return ((OutputXmlTreeFigure) getFigure()).getColumnContainer();
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
    protected List getModelChildren() {
        return ((OutputXmlTree) getModel()).getNodes();
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
            case XmlmapPackage.OUTPUT_XML_TREE__NODES:
                refreshChildren();
                break;
            case XmlmapPackage.ABSTRACT_IN_OUT_TREE__FILTER_INCOMING_CONNECTIONS:
                refreshTargetConnections();
                break;
            }
            break;
        case Notification.SET:
            switch (featureId) {
            case XmlmapPackage.OUTPUT_XML_TREE__NODES:
                refreshChildren();
            case XmlmapPackage.OUTPUT_XML_TREE__REJECT:
            case XmlmapPackage.OUTPUT_XML_TREE__REJECT_INNER_JOIN:
            case XmlmapPackage.OUTPUT_XML_TREE__EXPRESSION_FILTER:
            case XmlmapPackage.OUTPUT_XML_TREE__MINIMIZED:
            case XmlmapPackage.OUTPUT_XML_TREE__ALL_IN_ONE:
            case XmlmapPackage.OUTPUT_XML_TREE__ENABLE_EMPTY_ELEMENT:
                ((OutputXmlTreeFigure) getFigure()).update(featureId);
                break;
            case XmlmapPackage.TREE_NODE__EXPRESSION:
                ((OutputXmlTreeFigure) getFigure()).update(XmlmapPackage.TREE_NODE__TYPE);
                break;
            case XmlmapPackage.ABSTRACT_IN_OUT_TREE__MULTI_LOOPS:
                // fix for TDI-20808 , clean aggreage and groups if it's multiloops
                OutputXmlTree model = (OutputXmlTree) getModel();
                boolean changed = false;
                if (model.isMultiLoops()) {
                    changed = XmlMapUtil.cleanGroup(model.getNodes());

                }
                if (model.isMultiLoops() && getParent() instanceof XmlMapDataEditPart) {
                    List childPart = ((XmlMapDataEditPart) getParent()).getChildren();
                    for (Object o : childPart) {
                        if (o instanceof InputXmlTreeEditPart) {
                            InputXmlTree inputTree = (InputXmlTree) ((InputXmlTreeEditPart) o).getModel();
                            if (inputTree.isMultiLoops() && !inputTree.isLookup()) {
                                changed = XmlMapUtil.cleanAggregate(model.getNodes()) || changed;
                                break;
                            }
                        }
                    }
                }
                if (changed) {
                    refreshChildren();
                }
            }
        }

        // change icon for set loop function button
        if (type == Notification.ADD && featureId == XmlmapPackage.OUTPUT_XML_TREE__INPUT_LOOP_NODES_TABLES) {
            final InputLoopNodesTable loopNodeTable = (InputLoopNodesTable) notification.getNewValue();
            addListenerForInputLoopNodeTable(loopNodeTable);
        }
    }

    private void addListenerForInputLoopNodeTable(final InputLoopNodesTable loopNodeTable) {
        loopNodeTable.eAdapters().add(new Adapter() {

            @Override
            public void notifyChanged(Notification notification) {
                int type = notification.getEventType();
                int featureId = notification.getFeatureID(XmlmapPackage.class);
                switch (type) {
                case Notification.ADD:
                case Notification.ADD_MANY:
                case Notification.REMOVE:
                case Notification.REMOVE_MANY:
                    EditPartViewer viewer = OutputXmlTreeEditPart.this.getViewer();
                    if (viewer instanceof XmlMapGraphicViewer) {
                        MapperManager manager = ((XmlMapGraphicViewer) viewer).getMapperManager();
                        if (manager != null && manager.getMainInputTree() != null && manager.getMainInputTree().isMultiLoops()) {
                            // change icon for loop function button
                            boolean hasDoc = XmlMapUtil.hasDocument((OutputXmlTree) getModel());
                            if (hasDoc) {
                                List<OutputTreeNodeEditPart> childLoopEditPart = getChildLoopEditPart(getChildren());
                                for (OutputTreeNodeEditPart childPart : childLoopEditPart) {
                                    OutputTreeNode outputNode = (OutputTreeNode) childPart.getModel();
                                    if (outputNode.getInputLoopNodesTable() == loopNodeTable) {
                                        if (childPart.getFigure() != null) {
                                            XmlmapTreeNodeFigure nodefigure = (XmlmapTreeNodeFigure) childPart.getFigure();
                                            nodefigure.getBranchContent().updateLoopButtonFigure();
                                        }
                                    }
                                }
                            } else {
                                ((OutputXmlTreeFigure) getFigure()).update(0);
                            }
                        }
                    }

                }
            }

            @Override
            public Notifier getTarget() {
                return null;
            }

            @Override
            public void setTarget(Notifier newTarget) {
            }

            @Override
            public boolean isAdapterForType(Object type) {
                return false;
            }

        });
    }

    private List<OutputTreeNodeEditPart> getChildLoopEditPart(List parentParts) {
        List<OutputTreeNodeEditPart> list = new ArrayList<OutputTreeNodeEditPart>();
        for (Object obj : parentParts) {
            if (obj instanceof OutputTreeNodeEditPart) {
                OutputTreeNodeEditPart parentEditPart = (OutputTreeNodeEditPart) obj;
                OutputTreeNode model = (OutputTreeNode) parentEditPart.getModel();
                if (model.isLoop()) {
                    list.add(parentEditPart);
                }
                list.addAll(getChildLoopEditPart(parentEditPart.getChildren()));
            }
        }

        return list;
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

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.gef.editparts.AbstractEditPart#setModel(java.lang.Object)
     */
    @Override
    public void setModel(Object model) {
        super.setModel(model);
        OutputXmlTree outputTree = (OutputXmlTree) getModel();
        manager = new XmlMapTableManager(outputTree, this);
        for (InputLoopNodesTable sourceLoopTable : outputTree.getInputLoopNodesTables()) {
            addListenerForInputLoopNodeTable(sourceLoopTable);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.newabstractmap.part.MapperTablePart#highLightHeader()
     */
    @Override
    public void highLightHeader(boolean highLight) {
        ((OutputXmlTreeFigure) getFigure()).highLightHeader(highLight);
    }

}
