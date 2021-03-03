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

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Locator;
import org.eclipse.draw2d.PositionConstants;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.handles.HandleBounds;

/**
 * DOC Talend class global comment. Detailled comment
 */
public class TalendConnectionHandleLocator implements Locator {

    private IFigure reference = null;

    private Point borderPoint = new Point(0, 0);

    private int side = PositionConstants.EAST;

    public TalendConnectionHandleLocator(IFigure reference) {
        this.reference = reference;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.draw2d.Locator#relocate(org.eclipse.draw2d.IFigure)
     */
    @Override
    public void relocate(IFigure target) {
        Rectangle bounds = reference instanceof HandleBounds ? ((HandleBounds) reference).getHandleBounds().getCopy() : reference
                .getBounds().getCopy();

        Point center = bounds.getCenter();
        Point cursorPosition = new Point(center.x + bounds.width / 2, center.y);

        borderPoint.setLocation(bounds.x + bounds.width, cursorPosition.y);
        side = PositionConstants.EAST;

        Point borderPointTranslated = borderPoint.getCopy();
        reference.translateToAbsolute(bounds);
        target.translateToRelative(bounds);
        reference.translateToAbsolute(borderPointTranslated);
        target.translateToRelative(borderPointTranslated);

        int height = target.getBounds().height;
        int halfHeight = height / 2;

        if (side == PositionConstants.EAST) {
            target.setLocation(borderPointTranslated.getTranslated(new Dimension(1, -halfHeight)));
        }

    }

}
