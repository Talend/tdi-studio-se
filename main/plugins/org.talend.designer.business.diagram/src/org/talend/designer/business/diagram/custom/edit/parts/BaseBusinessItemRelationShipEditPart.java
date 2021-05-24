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

import org.eclipse.draw2d.Connection;
import org.eclipse.draw2d.Figure;
import org.eclipse.emf.common.notify.Notification;
import org.eclipse.gmf.runtime.diagram.ui.editparts.ConnectionNodeEditPart;
import org.eclipse.gmf.runtime.diagram.ui.editpolicies.EditPolicyRoles;
import org.eclipse.gmf.runtime.notation.View;
import org.talend.designer.business.diagram.custom.edit.policies.BusinessItemDragDropEditPolicy;
import org.talend.designer.business.diagram.custom.figures.BusinessTooltipFigure;
import org.talend.designer.business.diagram.custom.util.ElementHelper;
import org.talend.designer.business.diagram.custom.util.ResourceDisposeUtil;

/**
 * DOC mhelleboid class global comment. Detailled comment <br/>
 *
 * $Id: BusinessItemShapeEditPart.java 1 2006-09-29 17:06:40 +0000 (ven, 29 sep 2006) nrousseau $
 *
 */
public abstract class BaseBusinessItemRelationShipEditPart extends ConnectionNodeEditPart {

    private BusinessTooltipFigure tooltipFigure;

    private ElementHelper elementHelper;

    /**
     * DOC mhelleboid BusinessItemShapeEditPart constructor comment.
     *
     * @param view
     */
    public BaseBusinessItemRelationShipEditPart(View view) {
        super(view);
        tooltipFigure = new BusinessTooltipFigure();
        elementHelper = new ElementHelper();
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
    protected Connection createConnectionFigure() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void deactivate() {
        if (this.tooltipFigure != null) {
            ResourceDisposeUtil.disposeColor(this.tooltipFigure.getBackgroundColor());
        }
        super.deactivate();
    }

    @Override
    protected void handleNotificationEvent(Notification notification) {
        super.handleNotificationEvent(notification);
        getFigure();
        getConnectionFigure();
        if (getConnectionFigure() instanceof Figure) {
            elementHelper.updateTooltipFigure((Figure) getConnectionFigure(), tooltipFigure, this);
        }

    }

    @Override
    public void refreshVisuals() {
        super.refreshVisuals();
        getFigure();
        getConnectionFigure();
        if (getConnectionFigure() instanceof Figure) {
            elementHelper.updateTooltipFigure((Figure) getConnectionFigure(), tooltipFigure, this);
        }
    }
}
