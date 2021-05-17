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
package org.talend.designer.business.diagram.custom.edit.parts;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.MouseEvent;
import org.eclipse.draw2d.MouseMotionListener;
import org.eclipse.draw2d.PositionConstants;
import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.gef.EditPart;
import org.eclipse.gmf.runtime.diagram.ui.editparts.ShapeNodeEditPart;
import org.eclipse.gmf.runtime.diagram.ui.editpolicies.EditPolicyRoles;
import org.eclipse.gmf.runtime.gef.ui.figures.NodeFigure;
import org.eclipse.gmf.runtime.notation.Node;
import org.eclipse.gmf.runtime.notation.View;
import org.talend.core.model.business.BusinessAlignment;
import org.talend.designer.business.diagram.custom.edit.policies.BusinessItemDragDropEditPolicy;
import org.talend.designer.business.diagram.custom.figures.BusinessItemNameFigure;
import org.talend.designer.business.diagram.custom.figures.BusinessItemShapeFigure;
import org.talend.designer.business.diagram.custom.figures.BusinessTooltipFigure;
import org.talend.designer.business.diagram.custom.util.ElementHelper;
import org.talend.designer.business.diagram.custom.util.ResourceDisposeUtil;
import org.talend.designer.business.model.business.BusinessItem;

/**
 * DOC mhelleboid class global comment. Detailled comment <br/>
 *
 * $Id$
 *
 */
public abstract class BusinessItemShapeEditPart extends ShapeNodeEditPart {

    private BusinessTooltipFigure tooltipFigure;

    private ElementHelper elementHelper;

    private EditPart editPart;

    /**
     * DOC mhelleboid BusinessItemShapeEditPart constructor comment.
     *
     * @param view
     */
    public BusinessItemShapeEditPart(View view) {
        super(view);
        tooltipFigure = new BusinessTooltipFigure();
        elementHelper = new ElementHelper();
        editPart = this;
    }

    @Override
    protected NodeFigure createNodeFigure() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    protected void unregisterVisuals() {
        IFigure figure2 = getFigure();
        if (figure2 instanceof BusinessItemShapeFigure) {
            ((BusinessItemShapeFigure) figure2).disposeColors();
        }
        super.unregisterVisuals();
    }

    @Override
    public void deactivate() {
        if (this.tooltipFigure != null) {
            ResourceDisposeUtil.disposeColor(this.tooltipFigure.getBackgroundColor());
        }
        super.deactivate();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.gmf.runtime.diagram.ui.editparts.ShapeNodeEditPart#createDefaultEditPolicies()
     */
    @Override
    protected void createDefaultEditPolicies() {
        super.createDefaultEditPolicies();
        installEditPolicy(EditPolicyRoles.DRAG_DROP_ROLE, new BusinessItemDragDropEditPolicy());
    }

    @Override
    protected void handleNotificationEvent(Notification notification) {
        super.handleNotificationEvent(notification);
        elementHelper.updateTooltipFigure(getNodeFigure(), tooltipFigure, this);
    }

    @Override
    public void refreshVisuals() {
        super.refreshVisuals();
        NodeFigure nodeFigure = getNodeFigure();
        for (Object obj : nodeFigure.getChildren()) {
            if (obj instanceof BusinessItemShapeFigure) {
                BusinessItemShapeFigure shapFigure = (BusinessItemShapeFigure) obj;
                for (Object figure : shapFigure.getChildren()) {
                    if (figure instanceof BusinessItemNameFigure) {
                        BusinessItemNameFigure nameFigure = (BusinessItemNameFigure) figure;
                        nameFigure.addMouseMotionListener(new MouseMotionListener() {

                            @Override
                            public void mouseMoved(MouseEvent me) {
                                // TODO Auto-generated method stub
                            }

                            @Override
                            public void mouseHover(MouseEvent me) {
                                // TODO Auto-generated method stub
                            }

                            @Override
                            public void mouseExited(MouseEvent me) {
                                // TODO Auto-generated method stub
                            }

                            @Override
                            public void mouseEntered(MouseEvent me) {
                                elementHelper.updateTooltipFigure(getNodeFigure(), tooltipFigure, editPart);
                            }

                            @Override
                            public void mouseDragged(MouseEvent me) {
                                // TODO Auto-generated method stub
                            }
                        });
                        EObject object = ((Node) getModel()).getElement();
                        if (object instanceof BusinessItem) {
                            BusinessItem item = (BusinessItem) object;
                            String hAlignment = item.getHAlignment();
                            if (hAlignment == null) {
                                hAlignment = BusinessAlignment.LEFT.toString();
                            }
                            String vAlignment = item.getVAlignment();
                            if (vAlignment == null) {
                                vAlignment = BusinessAlignment.TOP.toString();
                            }
                            if (BusinessAlignment.HCENTRE.toString().equals(hAlignment)) {
                                if (BusinessAlignment.TOP.toString().equals(vAlignment)) {
                                    nameFigure.setAlignment(PositionConstants.TOP);
                                } else if (BusinessAlignment.BOTTOM.toString().equals(vAlignment)) {
                                    nameFigure.setAlignment(PositionConstants.BOTTOM);
                                } else {
                                    nameFigure.setAlignment(PositionConstants.CENTER);
                                }
                            } else if (BusinessAlignment.VCENTRE.toString().equals(vAlignment)) {
                                if (BusinessAlignment.LEFT.toString().equals(hAlignment)) {
                                    nameFigure.setAlignment(PositionConstants.LEFT);
                                } else if (BusinessAlignment.RIGHT.toString().equals(hAlignment)) {
                                    nameFigure.setAlignment(PositionConstants.RIGHT);
                                } else {
                                    nameFigure.setAlignment(PositionConstants.CENTER);
                                }
                            } else {
                                nameFigure.setAlignment(getPosition(hAlignment, BusinessAlignment.HORIZONTAL, item)
                                        | getPosition(vAlignment, BusinessAlignment.VERTICAL, item));
                            }

                        }
                    }
                }
            }
        }
        elementHelper.updateTooltipFigure(getNodeFigure(), tooltipFigure, this);
    }

    private int getPosition(String alignment, BusinessAlignment alignmentGroup, BusinessItem item) {
        BusinessAlignment position = getAlignment(alignment);
        if (position == null) {
            if (BusinessAlignment.HORIZONTAL.equals(alignmentGroup)) {
                return PositionConstants.LEFT;
            }
            if (BusinessAlignment.VERTICAL.equals(alignmentGroup)) {
                return PositionConstants.TOP;
            }

        }
        switch (position) {
        case LEFT:
            return PositionConstants.LEFT;

        case RIGHT:
            return PositionConstants.RIGHT;

        case HCENTRE:
            return PositionConstants.LEFT_CENTER_RIGHT | PositionConstants.TOP;

        case TOP:
            return PositionConstants.TOP;

        case BOTTOM:
            return PositionConstants.BOTTOM;

        case VCENTRE:
            return PositionConstants.TOP_MIDDLE_BOTTOM | PositionConstants.LEFT;

        }
        return 0;
    }

    private BusinessAlignment getAlignment(String alignment) {
        BusinessAlignment[] alignments = BusinessAlignment.values();
        BusinessAlignment position = null;
        for (int i = 0; i < alignments.length; i++) {
            if (alignments[i].toString().equalsIgnoreCase(alignment)) {
                position = alignments[i];
                return position;
            }

        }
        return position;
    }
}
