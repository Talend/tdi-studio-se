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

import org.eclipse.draw2d.AbstractBorder;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Insets;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;

/**
 * DOC nrousseau ConnectionTraceFigure class global comment. Detailled comment <br/>
 *
 * $Id: ConnectionTraceFigure.java 7674 2007-12-20 07:25:20Z bqian $
 *
 */
public class LineBorder extends AbstractBorder {

    private Color color;

    private int orientation;

    private int leftOffset;

    private int rightOffset;

    private int topOffset;

    private int bottomOffset;

    public LineBorder(Color color, int orientation) {
        this.color = color;
        this.orientation = orientation;
        rightOffset = -1;
        bottomOffset = -1;
    }

    public void setLeftOffset(int leftOffset) {
        this.leftOffset = leftOffset;
    }

    public void setRightOffset(int rightOffset) {
        this.rightOffset = rightOffset;
    }

    public void setTopOffset(int topOffset) {
        this.topOffset = topOffset;
    }

    public void setBottomOffset(int bottomOffset) {
        this.bottomOffset = bottomOffset;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.draw2d.Border#getInsets(org.eclipse.draw2d.IFigure)
     */
    public Insets getInsets(IFigure figure) {
        return new Insets(0, 0, 0, 0);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.draw2d.Border#paint(org.eclipse.draw2d.IFigure, org.eclipse.draw2d.Graphics,
     * org.eclipse.draw2d.geometry.Insets)
     */
    public void paint(IFigure figure, Graphics graphics, Insets insets) {
        graphics.setForegroundColor(color);
        if ((orientation & SWT.TOP) != 0) {
            graphics.drawLine(getPaintRectangle(figure, insets).getTopLeft().translate(0, topOffset), tempRect.getTopRight()
                    .translate(0, topOffset));
        }
        if ((orientation & SWT.LEFT) != 0) {
            graphics.drawLine(getPaintRectangle(figure, insets).getTopLeft().translate(leftOffset, 0), tempRect.getBottomLeft()
                    .translate(leftOffset, 0));
        }
        if ((orientation & SWT.RIGHT) != 0) {
            graphics.drawLine(getPaintRectangle(figure, insets).getTopRight().translate(rightOffset, 0), tempRect
                    .getBottomRight().translate(rightOffset, 0));
        }
        if ((orientation & SWT.BOTTOM) != 0) {
            graphics.drawLine(getPaintRectangle(figure, insets).getBottomLeft().translate(0, bottomOffset), tempRect
                    .getBottomRight().translate(0, bottomOffset));
        }
    }
}
