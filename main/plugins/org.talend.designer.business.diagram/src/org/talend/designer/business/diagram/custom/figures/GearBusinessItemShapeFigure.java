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
package org.talend.designer.business.diagram.custom.figures;

import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.PointList;
import org.eclipse.draw2d.geometry.Rectangle;

/**
 * DOC mhelleboid class global comment. Detailled comment <br/>
 *
 * $Id: ActionBusinessItemShapeFigure.java 1 2006-09-29 17:06:40 +0000 (ven, 29 sep 2006) nrousseau $
 *
 */
public class GearBusinessItemShapeFigure extends BusinessItemShapeFigure {

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.gmf.runtime.gef.ui.figures.NodeFigure#paintFigure(org.eclipse.draw2d.Graphics)
     */
    @Override
    protected void paintFigure(Graphics graphics) {

        if (getDrawFrame()) {
            setDefaultSize(60, 60);
            setBorder(border);
            drawFigure(getSmallBounds(), graphics);
        } else {
            if (getBorder() != null) {
                setBorder(null);
            }
            drawFigure(getInnerBounds(), graphics);
        }

    }

    private void drawFigure(Rectangle r, Graphics graphics) {

        int nbTeeth = 8;
        double teethAngle = 0.23;
        double innerExternalRatio = 0.80;

        Point center = new Point(r.x + r.width / 2, r.y + r.height / 2);
        int horizontalExternalRadius = r.width / 2;
        int verticalExternalRadius = r.height / 2;
        int horizontalInnerRadius = (int) (horizontalExternalRadius * innerExternalRatio);
        int verticalInnerRadius = (int) (verticalExternalRadius * innerExternalRatio);

        PointList pointList = new PointList();

        boolean externalFirst = false;
        for (double i = 0; i < 2 * Math.PI; i = i + Math.PI * 2 / (nbTeeth * 2)) {
            double j = (externalFirst ? i - Math.PI / nbTeeth * teethAngle : i + Math.PI / nbTeeth * teethAngle);
            Point externalPoint = center.getTranslated((int) (Math.cos(j) * horizontalExternalRadius),
                    (int) (Math.sin(j) * verticalExternalRadius));
            Point internalPoint = center.getTranslated((int) (Math.cos(i) * horizontalInnerRadius),
                    (int) (Math.sin(i) * verticalInnerRadius));
            if (externalFirst) {
                pointList.addPoint(externalPoint);
                pointList.addPoint(internalPoint);
            } else {
                pointList.addPoint(internalPoint);
                pointList.addPoint(externalPoint);
            }
            externalFirst = !externalFirst;
        }

        graphics.drawPolygon(pointList);
        graphics.fillPolygon(pointList);
    }
}
