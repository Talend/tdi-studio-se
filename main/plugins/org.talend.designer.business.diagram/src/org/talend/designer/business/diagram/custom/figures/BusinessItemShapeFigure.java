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
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.LineBorder;
import org.eclipse.draw2d.geometry.Insets;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gmf.runtime.gef.ui.figures.DefaultSizeNodeFigure;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;
import org.talend.designer.business.diagram.custom.util.ResourceDisposeUtil;

/**
 * DOC mhelleboid class global comment. Detailled comment <br/>
 *
 * $Id$
 *
 */
public class BusinessItemShapeFigure extends DefaultSizeNodeFigure {

    private boolean drawFrame;

    private IFigure nameFigure;

    static Color borderColor = new Color(Display.getCurrent(), 255, 110, 0);

    public LineBorder border = new LineBorder() {

        @Override
        public void paint(IFigure figure, Graphics graphics, Insets insets) {
            graphics.setLineStyle(Graphics.LINE_DOT);
            super.paint(figure, graphics, insets);
            graphics.restoreState();
        }

    };

    /**
     * DOC mhelleboid BusinessItemShapeFigure constructor comment.
     */
    public BusinessItemShapeFigure() {
        super(50, 50);
        border.setColor(borderColor);
        border.setWidth(3);
    }

    /**
     * DOC mhelleboid Comment method "getInnerBounds".
     */
    protected Rectangle getInnerBounds() {
        Rectangle innerBounds = new Rectangle();

        innerBounds.x = bounds.x;
        innerBounds.y = bounds.y;
        innerBounds.width = bounds.width - 1;
        innerBounds.height = bounds.height - 1;

        return innerBounds;
    }

    protected Rectangle getSmallBounds() {
        Rectangle outerBounds = getInnerBounds();
        Rectangle smallBounds = new Rectangle();
        smallBounds.x = outerBounds.x + 5;
        smallBounds.y = outerBounds.y + 5;
        smallBounds.width = outerBounds.width - 10;
        smallBounds.height = outerBounds.height - 10;
        return smallBounds;
    }

    public boolean getDrawFrame() {
        return this.drawFrame;
    }

    public void setDrawFrame(boolean drawFrame) {
        this.drawFrame = drawFrame;
    }

    public void disposeColors() {
        ResourceDisposeUtil.disposeColor(border.getColor());
    }
}
