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
public class ColumnBorder extends AbstractBorder {

    public Insets getInsets(IFigure figure) {
        return new Insets(1, 1, 1, 1);
    }

    public void paint(IFigure figure, Graphics graphics, Insets insets) {
        tempRect.setBounds(getPaintRectangle(figure, insets));
        tempRect.width--;
        tempRect.height--;
        graphics.setForegroundColor(ColorConstants.menuBackground);
        graphics.drawLine(tempRect.getTopRight(), tempRect.getBottomRight());
    }

}
