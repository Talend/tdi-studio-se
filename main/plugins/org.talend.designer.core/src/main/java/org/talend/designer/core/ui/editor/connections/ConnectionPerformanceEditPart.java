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
package org.talend.designer.core.ui.editor.connections;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.PolylineConnection;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.gef.editparts.AbstractGraphicalEditPart;
import org.talend.commons.ui.utils.workbench.gef.SimpleHtmlFigure;
import org.talend.commons.utils.workbench.preferences.GlobalConstant;

/**
 * Edit part of connection performance.
 *
 * yzhang class global comment. Detailled comment <br/>
 *
 * $Id: ConnectionPerformanceEditPart.java 下午02:24:35 2007-6-8 +0000 (2007-6-8) yzhang $
 *
 */
public class ConnectionPerformanceEditPart extends AbstractGraphicalEditPart implements PropertyChangeListener {

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.gef.editparts.AbstractGraphicalEditPart#getLayer(java.lang.Object)
     */
    @Override
    protected IFigure getLayer(Object layer) {
        // TODO Auto-generated method stub
        return super.getLayer(layer);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.gef.editparts.AbstractConnectionEditPart#createFigure()
     */
    @Override
    protected IFigure createFigure() {
        SimpleHtmlFigure label = new SimpleHtmlFigure() {

            /*
             * (non-Javadoc)
             *
             * @see org.talend.commons.utils.workbench.gef.SimpleHtmlFigure#paint(org.eclipse.draw2d.Graphics)
             */
            @Override
            public void paint(Graphics graphics) {
                // see bug 2074
                if (GlobalConstant.generatingScreenShoot) {
                    return;
                }

                super.paint(graphics);

            }

        };
        return label;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.gef.editparts.AbstractGraphicalEditPart#activate()
     */
    @Override
    public void activate() {
        super.activate();
        ((ConnectionPerformance) getModel()).addPropertyChangeListener(this);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.gef.editparts.AbstractGraphicalEditPart#deactivate()
     */
    @Override
    public void deactivate() {
        super.deactivate();
        ((ConnectionPerformance) getModel()).removePropertyChangeListener(this);
    }

    /*
     * (non-Javadoc)
     *
     * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
     */
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals(ConnectionPerformance.LABEL_PROP)) {
            refreshVisuals();
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.gef.editparts.AbstractEditPart#createEditPolicies()
     */
    @Override
    protected void createEditPolicies() {
        // do nothing.
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.gef.editparts.AbstractEditPart#refreshVisuals()
     */
    @Override
    protected void refreshVisuals() {

        SimpleHtmlFigure htmlFigure = (SimpleHtmlFigure) getFigure();
        ConnectionPerformance pefModel = (ConnectionPerformance) getModel();
        ConnectionPart parent = (ConnectionPart) getParent();
        PolylineConnection connFigure = (PolylineConnection) parent.getFigure();

        htmlFigure.setText(pefModel.getLabel());

        Point offset = pefModel.getOffset();

        ConnLabelConstraint constraint = new ConnLabelConstraint(htmlFigure.getPreferredSize(), "middle", offset, connFigure); //$NON-NLS-1$
        parent.setLayoutConstraint(this, htmlFigure, constraint);
    }
}
