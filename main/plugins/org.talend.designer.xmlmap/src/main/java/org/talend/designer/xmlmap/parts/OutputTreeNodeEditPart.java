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

import java.util.List;

import org.eclipse.emf.common.notify.Notification;
import org.eclipse.gef.EditPart;
import org.talend.designer.gefabstractmap.part.MapperTablePart;
import org.talend.designer.gefabstractmap.utils.MapperUtils;
import org.talend.designer.xmlmap.editor.XmlMapGraphicViewer;
import org.talend.designer.xmlmap.figures.OutputXmlTreeFigure;
import org.talend.designer.xmlmap.figures.treeNode.XmlmapTreeNodeFigure;
import org.talend.designer.xmlmap.model.emf.xmlmap.AbstractInOutTree;
import org.talend.designer.xmlmap.model.emf.xmlmap.OutputTreeNode;
import org.talend.designer.xmlmap.model.emf.xmlmap.TreeNode;
import org.talend.designer.xmlmap.model.emf.xmlmap.XmlmapPackage;
import org.talend.designer.xmlmap.util.XmlMapUtil;

/**
 * wchen class global comment. Detailled comment
 */
public class OutputTreeNodeEditPart extends TreeNodeEditPart {

    @Override
    public List getModelChildren() {
        TreeNode model = (TreeNode) getModel();
        return model.getChildren();
    }

    @Override
    protected List getModelTargetConnections() {
        OutputTreeNode model = (OutputTreeNode) getModel();
        return model.getIncomingConnections();
    }

    @Override
    protected void addChildVisual(EditPart childEditPart, int index) {
        super.addChildVisual(childEditPart, index);
    }

    @Override
    public void notifyChanged(Notification notification) {
        super.notifyChanged(notification);
        int type = notification.getEventType();
        int featureId = notification.getFeatureID(XmlmapPackage.class);
        switch (type) {
        case Notification.SET:
            switch (featureId) {
            case XmlmapPackage.TREE_NODE__TYPE:
                if (XmlMapUtil.DOCUMENT.equals(notification.getOldValue())
                        || !XmlMapUtil.DOCUMENT.equals(notification.getOldValue())
                        && XmlMapUtil.DOCUMENT.equals(notification.getNewValue())) {
                    MapperTablePart mapperTablePart = MapperUtils.getMapperTablePart(this);
                    if (mapperTablePart.getFigure() instanceof OutputXmlTreeFigure) {
                        ((OutputXmlTreeFigure) mapperTablePart.getFigure()).update(XmlmapPackage.TREE_NODE__TYPE);
                    }
                }
                break;
            case XmlmapPackage.TREE_NODE__LOOP:
                ((XmlmapTreeNodeFigure) getFigure()).getBranchContent().updateLoopButtonFigure();
                AbstractInOutTree abstractInOutTree = XmlMapUtil.getAbstractInOutTree((OutputTreeNode) getModel());
                if (abstractInOutTree != null) {
                    ((XmlMapGraphicViewer) getViewer()).getMapperManager().getProblemsAnalyser().checkProblems(abstractInOutTree);
                    ((XmlMapGraphicViewer) getViewer()).getMapperManager().getMapperUI().updateStatusBar();
                }
            }
            break;
        case Notification.REMOVE:
        case Notification.REMOVE_MANY:
            switch (featureId) {
            // reomve auto map function for InputLoopTable
            }
            // case XmlmapPackage.ABSTRACT_NODE__INCOMING_CONNECTIONS:
            // // remove source loop node in parent loop of output node if needed .
            // Object oldObject = notification.getOldValue();
            // if (getViewer() instanceof XmlMapGraphicViewer) {
            // InputXmlTree mainInputTree = ((XmlMapGraphicViewer) getViewer()).getMapperManager().getMainInputTree();
            // if (oldObject instanceof Connection) {
            // List list = new ArrayList();
            // list.add(oldObject);
            // InputLoopTableUtil.removeSourceLoopFromInputLoopTable(list, (OutputTreeNode) getModel(), mainInputTree);
            // } else if (oldObject instanceof List) {
            // InputLoopTableUtil.removeSourceLoopFromInputLoopTable((List) oldObject, (OutputTreeNode) getModel(),
            // mainInputTree);
            // }
            // }
            // }
        case Notification.ADD:
        case Notification.ADD_MANY:
            switch (featureId) {
            case XmlmapPackage.INPUT_LOOP_NODES_TABLE__INPUTLOOPNODES:
            case XmlmapPackage.OUTPUT_TREE_NODE__INPUT_LOOP_NODES_TABLE:
                XmlmapTreeNodeFigure outputFigure = (XmlmapTreeNodeFigure) getFigure();
                if (outputFigure.getElement() != null) {
                    outputFigure.getBranchContent().updateLoopButtonFigure();
                }
                break;
            default:
                break;
            }
        }
    }
}
