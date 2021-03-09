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
package org.talend.sqlbuilder.erdiagram.ui.figures;

import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;

/**
 * DOC qzhang class global comment. Detailled comment <br/>
 *
 * $Id: talend.epf 1 2006-09-29 17:06:40 +0000 (ææäº, 29 ä¹æ 2006) nrousseau $
 *
 */
public class CustomTableFigure extends GenericFigure {

    private IFigure figure;

    public static final Color BG_COLOR = new Color(null, 50, 100, 180);

    public static final Color START_COLOR = new Color(null, new RGB(0xB0, 0xE7, 0));
    public CustomTableFigure() {
        setTitle(this);
        // this.setForegroundColor(ColorConstants.darkBlue);
        this.setForegroundColor(BG_COLOR);
        // this.repaint();
    }

    protected void setTitle(IFigure title) {
        figure = title;
    }

    protected void fillShape(Graphics graphics) {
        Rectangle r = getBounds();
        Rectangle.SINGLETON.setBounds(r);
        Rectangle.SINGLETON.y += 1;
        Rectangle.SINGLETON.height -= 1;

        int shadowDepth = 0;
        int rectangleWidth = r.width - shadowDepth;
        int rectangleHeight = r.height - shadowDepth;

        Color foreground = graphics.getForegroundColor();
        Color background = graphics.getBackgroundColor(), shadow = ColorUtilities.darker(foreground);

        int x = r.x + shadowDepth;
        int y = r.y + shadowDepth;

        /**
         * trace shadow
         */
        for (int i = shadowDepth - 1; i >= 0; i--) {
            shadow = lighter(background, i + 1, shadowDepth + 1);
            graphics.setBackgroundColor(shadow);
            graphics.fillRectangle(x, y, rectangleWidth, rectangleHeight);
            if (i > 0) {
                x--;
                y--;
            }
        }

        graphics.setBackgroundColor(background);
        graphics.setForegroundColor(foreground);

        graphics.fillRectangle(x, y, rectangleWidth, rectangleHeight);
        // Gradient
        graphics.setForegroundColor(foreground);
        graphics.fillGradient(x, y - 20, rectangleWidth, figure.getBounds().height + 25, true);
        graphics.setForegroundColor(foreground);

        graphics.drawRectangle(x, y - 1, rectangleWidth - 1, rectangleHeight);
    }

    protected void outlineShape(Graphics graphics) {

    }
}
