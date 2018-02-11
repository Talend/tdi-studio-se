// ============================================================================
//
// Copyright (C) 2006-2018 Talend Inc. - www.talend.com
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

import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.PolygonShape;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;
import org.talend.commons.ui.utils.image.ColorUtils;
import org.talend.core.model.process.INodeConnector;

/**
 * DOC hwang class global comment. Detailled comment
 */
public class PentagonShape extends PolygonShape {

    private INodeConnector mainConnector;

    private Dimension size = new Dimension(8, 11);

    private Color black = null;

    private static Font font = new Font(Display.getDefault(), "courrier", 6, SWT.NORMAL);

    public PentagonShape(INodeConnector mainConnector) {
        this.mainConnector = mainConnector;
        black = ColorUtils.getCacheColor(new RGB(0, 0, 0));
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.draw2d.Shape#paintFigure(org.eclipse.draw2d.Graphics)
     */
    @Override
    public void paintFigure(Graphics graphics) {
        super.paintFigure(graphics);
        Rectangle area = getClientArea();
        int y = (area.height - size.height) / 2 + area.y;
        int x = (area.width - size.width) / 2 + area.x;
        Point point = new Point(x, y);
        point.x = point.x + 1;
        point.y = point.y + 1;

        graphics.setFont(font);
        graphics.setForegroundColor(black);

        switch (this.mainConnector.getDefaultConnectionType()) {
        case RUN_IF:
        case ON_SUBJOB_OK:
        case ON_SUBJOB_ERROR:
        case ON_COMPONENT_OK:
        case ON_COMPONENT_ERROR:
            graphics.drawString("T", point);//$NON-NLS-1$
            break;
        default:
            graphics.drawString("O", point);//$NON-NLS-1$
            break;
        }

    }

}
