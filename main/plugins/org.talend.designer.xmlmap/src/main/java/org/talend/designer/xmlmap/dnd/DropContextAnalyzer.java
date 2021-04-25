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
package org.talend.designer.xmlmap.dnd;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.gef.editparts.AbstractGraphicalEditPart;
import org.eclipse.swt.dnd.DND;
import org.talend.designer.gefabstractmap.dnd.TransferdType;
import org.talend.designer.gefabstractmap.dnd.TransferedObject;
import org.talend.designer.gefabstractmap.figures.ExpressionFigure;
import org.talend.designer.gefabstractmap.figures.treesettings.FilterTextArea;
import org.talend.designer.xmlmap.model.emf.xmlmap.AbstractInOutTree;
import org.talend.designer.xmlmap.model.emf.xmlmap.InputXmlTree;
import org.talend.designer.xmlmap.model.emf.xmlmap.NodeType;
import org.talend.designer.xmlmap.model.emf.xmlmap.OutputTreeNode;
import org.talend.designer.xmlmap.model.emf.xmlmap.OutputXmlTree;
import org.talend.designer.xmlmap.model.emf.xmlmap.TreeNode;
import org.talend.designer.xmlmap.model.emf.xmlmap.VarNode;
import org.talend.designer.xmlmap.model.emf.xmlmap.VarTable;
import org.talend.designer.xmlmap.model.emf.xmlmap.XmlMapData;
import org.talend.designer.xmlmap.parts.TreeNodeEditPart;
import org.talend.designer.xmlmap.util.XmlMapUtil;

public class DropContextAnalyzer {

    private TransferedObject objects;

    private AbstractGraphicalEditPart targetEditPart;

    private Point dropLocation;

    private boolean isDropValid;

    private IFigure targetFigure;

    private DropType dropType;

    private int detail;

    private int feedback;

    public DropContextAnalyzer(TransferedObject objects, AbstractGraphicalEditPart targetEditPart, Point dropLocation) {
        this.objects = objects;
        this.targetEditPart = targetEditPart;
        this.dropLocation = dropLocation;
        dropType = DropType.DROP_EXPRESSION;
        isDropValid = checkDropIsValid();
        analyzeForDetailFeedback();
    }

    private void analyzeForDetailFeedback() {

        int dropOperation = DND.DROP_NONE;
        if (isDropValid) {
            feedback = DND.FEEDBACK_SCROLL | DND.FEEDBACK_EXPAND;
            if (dropType == DropType.DROP_EXPRESSION || dropType == DropType.DROP_FILTER) {
                dropOperation = DND.DROP_LINK;
            } else {
                dropOperation = DND.DROP_COPY;
            }
        }
        detail = dropOperation;
    }

    private boolean checkDropIsValid() {
        if (objects == null || objects.getToTransfer().isEmpty() || targetEditPart == null) {
            return false;
        }
        Object targetModel = targetEditPart.getModel();
        targetFigure = targetEditPart.getFigure().findFigureAt(dropLocation.x, dropLocation.y);
        boolean isTragetOutputNode = targetModel instanceof OutputTreeNode;
        boolean isTragetInputNode = targetModel instanceof TreeNode && !(targetModel instanceof OutputTreeNode);
        boolean isVar = targetModel instanceof VarNode;
        boolean isDropInputTree = targetModel instanceof InputXmlTree;
        boolean isDropOutputTree = targetModel instanceof OutputXmlTree;
        boolean isDropVarTable = targetModel instanceof VarTable;

        // drop expression
        if (targetFigure instanceof ExpressionFigure) {
            dropType = DropType.DROP_EXPRESSION;
            if (isTragetOutputNode) {
                // INPUT => OUTPUT
                return XmlMapUtil.isExpressionEditable((TreeNode) targetModel);
            } else if (isTragetInputNode) {
                // INPUT => INPUT ,can't dnd in the same tree
                boolean isValid = checkDropInputValid(targetModel);
                if (isValid) {
                    isValid = XmlMapUtil.isExpressionEditable((TreeNode) targetModel);
                }
                return isValid;
            }
            return true;
        }
        // drop tree filter
        else if (targetFigure instanceof FilterTextArea) {
            dropType = DropType.DROP_FILTER;
            return true;
        } else {
            if (objects.getType() == TransferdType.INPUT) {
                if (isTragetOutputNode) {
                    return checkDropOutputDocChildValid(targetModel);
                } else if (isVar || isDropVarTable) {
                    dropType = DropType.DROP_INSERT_VAR;
                    return true;
                } else if (isDropInputTree) {
                    dropType = DropType.DROP_INSERT_INPUT;
                    return checkDropInputValid(targetModel);
                } else if (isDropOutputTree) {
                    dropType = DropType.DROP_INSERT_OUTPUT;
                    return true;
                }

            } else if (objects.getType() == TransferdType.VAR) {
                if (isTragetOutputNode) {
                    return checkDropOutputDocChildValid(targetModel);
                } else if (isDropOutputTree) {
                    dropType = DropType.DROP_INSERT_OUTPUT;
                    return true;
                }

            }

        }

        return false;
    }

    private boolean checkDropOutputDocChildValid(Object targetModel) {
        OutputTreeNode outputNode = (OutputTreeNode) targetModel;
        NodeType nodeType = outputNode.getNodeType();
        if (outputNode.eContainer() instanceof OutputTreeNode && nodeType != NodeType.ATTRIBUT && nodeType != NodeType.NAME_SPACE) {
            dropType = DropType.DROP_OUTPUT_DOC_CHILD;
            return true;
        } else if (outputNode.eContainer() instanceof OutputXmlTree && !XmlMapUtil.DOCUMENT.equals(outputNode.getType())) {
            dropType = DropType.DROP_INSERT_OUTPUT;
            return true;
        }
        return false;

    }

    private boolean checkDropInputValid(Object target) {
        if (objects.getType() == TransferdType.INPUT) {
            for (Object obj : objects.getToTransfer()) {
                TreeNodeEditPart part = (TreeNodeEditPart) obj;
                AbstractInOutTree srouceTree = XmlMapUtil.getAbstractInOutTree((TreeNode) part.getModel());
                AbstractInOutTree targetTree = null;
                if (target instanceof InputXmlTree) {
                    targetTree = (InputXmlTree) target;
                } else {

                    targetTree = XmlMapUtil.getAbstractInOutTree((TreeNode) target);
                }
                if (srouceTree == targetTree) {
                    return false;
                }
                if (srouceTree.eContainer() instanceof XmlMapData) {
                    XmlMapData mapdata = ((XmlMapData) srouceTree.eContainer());
                    int indexSource = mapdata.getInputTrees().indexOf(srouceTree);
                    int indexTarget = mapdata.getInputTrees().indexOf(targetTree);
                    if (indexTarget < indexSource) {
                        return false;
                    }
                }
            }

        }
        return true;
    }

    public boolean isDropValid() {
        return isDropValid;
    }

    public DropType getDropType() {
        return dropType;
    }

    public int getDetail() {
        return detail;
    }

    public int getFeedback() {
        return feedback;
    }
}
