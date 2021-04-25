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
package org.talend.sqlbuilder.erdiagram.ui.editor;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.editparts.GridLayer;
import org.eclipse.swt.graphics.Color;

/**
 * DOC qzhang class global comment. Detailled comment <br/>
 *
 * $Id: talend.epf 1 2006-09-29 17:06:40 +0000 (ææäº, 29 ä¹æ 2006) nrousseau $
 *
 */
public class ErDiagramGridLayer extends GridLayer {

    public static final Color GRID_COLOR = ColorConstants.black;

    /**
     * DOC admin SqlBuilderGridLayer constructor comment.
     */
    public ErDiagramGridLayer() {
        super();
        setForegroundColor(GRID_COLOR);
    }

    @Override
    protected void paintGrid(Graphics g) {
        paintGrid(g, this, origin, gridX, gridY);
    }

    protected void paintGrid(Graphics g, IFigure f, org.eclipse.draw2d.geometry.Point origin, int distanceX,
            int distanceY) {
        Rectangle clip = g.getClip(Rectangle.SINGLETON);

        if (distanceX > 0 && distanceY > 0) {
            if (origin.x >= clip.x) {
                while (origin.x - distanceX >= clip.x) {
                    origin.x -= distanceX;
                }
            } else {
                while (origin.x < clip.x) {
                    origin.x += distanceX;
                }
            }

            if (origin.y >= clip.y) {
                while (origin.y - distanceY >= clip.y) {
                    origin.y -= distanceY;
                }
            } else {
                while (origin.y < clip.y) {
                    origin.y += distanceY;
                }
            }

            for (int i = origin.x; i < clip.x + clip.width; i += distanceX) {
                for (int j = origin.y; j < clip.y + clip.height; j += distanceY) {
                    g.drawPoint(i, j);
                }
            }
        }
    }
}
