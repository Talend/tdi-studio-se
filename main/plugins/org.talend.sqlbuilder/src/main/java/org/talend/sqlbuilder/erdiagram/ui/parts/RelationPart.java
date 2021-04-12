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
package org.talend.sqlbuilder.erdiagram.ui.parts;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.PolylineConnection;
import org.eclipse.gef.EditPolicy;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.editparts.AbstractConnectionEditPart;
import org.eclipse.gef.editpolicies.ConnectionEditPolicy;
import org.eclipse.gef.editpolicies.ConnectionEndpointEditPolicy;
import org.eclipse.gef.requests.GroupRequest;
import org.talend.sqlbuilder.erdiagram.ui.commands.RelationDeleteCommand;
import org.talend.sqlbuilder.erdiagram.ui.nodes.Relation;

/**
 * DOC qzhang class global comment. Detailled comment <br/>
 *
 * $Id: talend.epf 1 2006-09-29 17:06:40 +0000 (ææäº, 29 ä¹æ 2006) nrousseau $
 *
 */
public class RelationPart extends AbstractConnectionEditPart implements PropertyChangeListener {

    private RelationFigure primaryFigure;

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.gef.editparts.AbstractGraphicalEditPart#createFigure()
     */
    @Override
    protected IFigure createFigure() {
        primaryFigure = new RelationFigure();
        return primaryFigure;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.gef.editparts.AbstractEditPart#createEditPolicies()
     */
    @Override
    protected void createEditPolicies() {
        // Selection handle edit policy.
        // Makes the connection show a feedback, when selected by the user.
        installEditPolicy(EditPolicy.CONNECTION_ENDPOINTS_ROLE, new ConnectionEndpointEditPolicy());

        // Allows the removal of the connection model element
        installEditPolicy(EditPolicy.CONNECTION_ROLE, new ConnectionEditPolicy() {

            protected Command getDeleteCommand(GroupRequest request) {
                List<Relation> connectionList = new ArrayList<Relation>();
                for (int i = 0; i < request.getEditParts().size(); i++) {
                    if (request.getEditParts().get(i) instanceof RelationPart) {
                        connectionList.add(((Relation) ((RelationPart) request.getEditParts().get(i)).getModel()));
                    }
                }
                return new RelationDeleteCommand(connectionList);
            }
        });
    }

    /*
     * (non-Javadoc)
     *
     * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
     */
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals(Relation.PROP_ENDPOINTS)) {
            refreshVisuals();
        } else if (evt.getPropertyName().equals(Relation.PROP_SOURCE)) {
            refreshVisuals();
        } else if (evt.getPropertyName().equals(Relation.PROP_TARGET)) {
            refreshVisuals();
        }
    }

//    /*
//     * (non-Javadoc)
//     *
//     * @see org.eclipse.gef.editparts.AbstractEditPart#refreshVisuals()
//     */
//    @Override
//    protected void refreshVisuals() {
////        Point start = primaryFigure.getStart();
////        Point newStart = new Point(start.x, start.y + 15);
////        Point end = primaryFigure.getEnd();
////        Point newEnd = new Point(end.x, end.y - 15);
////        primaryFigure.setEndpoints(newStart, newEnd);
//        ((GraphicalEditPart) getParent()).setLayoutConstraint(this, getFigure(), primaryFigure.getBounds());
//    }

    /**
     * DOC admin RelationPart class global comment. Detailled comment <br/>
     *
     * $Id: talend.epf 1 2006-09-29 17:06:40 +0000 (ææäº, 29 ä¹æ 2006) nrousseau $
     *
     */
    public class RelationFigure extends PolylineConnection {

        /**
         * @generated
         */
        public RelationFigure() {
            this.setForegroundColor(ColorConstants.darkBlue);
            setSourceDecoration(createSourceDecoration());
            setTargetDecoration(createTargetDecoration());
        }

        /**
         * @generated
         */
        private org.eclipse.draw2d.PolylineDecoration createSourceDecoration() {
            org.eclipse.draw2d.PolylineDecoration df = new org.eclipse.draw2d.PolylineDecoration();

            org.eclipse.draw2d.geometry.PointList pl = new org.eclipse.draw2d.geometry.PointList();
            pl.addPoint(-1, 1);
            pl.addPoint(0, 0);
            pl.addPoint(-1, -1);
            df.setTemplate(pl);
            df.setScale(7, 3);

            return df;
        }

        /**
         * @generated
         */
        private org.eclipse.draw2d.PolylineDecoration createTargetDecoration() {
            org.eclipse.draw2d.PolylineDecoration df = new org.eclipse.draw2d.PolylineDecoration();

            org.eclipse.draw2d.geometry.PointList pl = new org.eclipse.draw2d.geometry.PointList();
            pl.addPoint(-1, 1);
            pl.addPoint(0, 0);
            pl.addPoint(-1, -1);
            df.setTemplate(pl);
            df.setScale(7, 3);

            return df;
        }

    }

}
