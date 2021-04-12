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

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.MarginBorder;
import org.eclipse.draw2d.geometry.Insets;
import org.eclipse.draw2d.geometry.Rectangle;

/**
 * wchen class global comment. Detailled comment
 */
public class RowBorder extends MarginBorder {

    public RowBorder(Insets insets) {
        super(insets);
    }

    public RowBorder(int t, int l, int b, int r) {
        super(t, l, b, r);
    }

    public RowBorder() {
        super(new Insets(1, 0, 0, 0));
    }

    public void paint(IFigure figure, Graphics graphics, Insets insets) {
        graphics.setForegroundColor(ColorConstants.menuBackground);
        graphics.drawLine(getRectangleToPaint(figure).getTopLeft(), tempRect.getTopRight());
    }

    public Rectangle getRectangleToPaint(IFigure figure) {
        tempRect.setBounds(figure.getBounds());
        return tempRect;
    }

}
