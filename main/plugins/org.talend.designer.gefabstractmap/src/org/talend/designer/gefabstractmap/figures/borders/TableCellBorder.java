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
package org.talend.designer.gefabstractmap.figures.borders;

import org.eclipse.draw2d.AbstractBorder;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Insets;

/**
 * wchen class global comment. Detailled comment
 */
public class TableCellBorder extends AbstractBorder {

    private boolean drawColumnSeparate = true;

    public Insets getInsets(IFigure figure) {
        return new Insets(0, 6, 0, 1);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.draw2d.Border#paint(org.eclipse.draw2d.IFigure, org.eclipse.draw2d.Graphics,
     * org.eclipse.draw2d.geometry.Insets)
     */
    public void paint(IFigure figure, Graphics graphics, Insets insets) {
        graphics.setForegroundColor(ColorConstants.menuBackground);
        if (drawColumnSeparate) {
            tempRect.setBounds(getPaintRectangle(figure, new Insets(0, 0, 0, 1)));
            graphics.drawLine(tempRect.getTopRight(), tempRect.getBottomRight());
        } else {
            tempRect.setBounds(getPaintRectangle(figure, new Insets()));
        }
        graphics.drawLine(tempRect.getTopLeft(), tempRect.getTopRight());
    }

    public boolean isDrawColumnSeparate() {
        return drawColumnSeparate;
    }

    public void setDrawColumnSeparate(boolean drawColumnSeparate) {
        this.drawColumnSeparate = drawColumnSeparate;
    }

}
