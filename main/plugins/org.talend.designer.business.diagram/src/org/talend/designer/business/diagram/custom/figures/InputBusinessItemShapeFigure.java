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
import org.eclipse.draw2d.geometry.PointList;
import org.eclipse.draw2d.geometry.Rectangle;

/**
 * DOC mhelleboid class global comment. Detailled comment <br/>
 *
 * $Id$
 *
 */
public class InputBusinessItemShapeFigure extends BusinessItemShapeFigure {

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

        int offset = (int) (r.height * 0.2);

        PointList pointList = new PointList();
        pointList.removeAllPoints();
        pointList.addPoint(r.x, r.y + offset);
        pointList.addPoint(r.x + r.width, r.y);
        pointList.addPoint(r.x + r.width, r.y + r.height);
        pointList.addPoint(r.x, r.y + r.height);

        graphics.fillPolygon(pointList);
        graphics.drawPolygon(pointList);
    }
}
