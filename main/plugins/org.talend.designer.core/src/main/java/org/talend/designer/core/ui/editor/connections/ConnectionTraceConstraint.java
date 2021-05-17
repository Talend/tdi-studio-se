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

import org.eclipse.draw2d.FigureUtilities;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Locator;
import org.eclipse.draw2d.PolylineConnection;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;

/**
 * Object that will define the position ofthe differents label for a connection. <br/>
 *
 * $Id$
 *
 */
public class ConnectionTraceConstraint implements Locator {

    String name;

    Dimension traceSize;

    String position;

    Point offset;

    PolylineConnection connFigure;

    /**
     * Sets the items that will be use to set the position of the label.
     *
     * @param text
     * @param position
     * @param offset
     * @param connFigure
     */
    public ConnectionTraceConstraint(String name, Dimension traceSize, String position, Point offset,
            PolylineConnection connFigure) {
        this.name = name;
        this.traceSize = traceSize;
        this.position = position;
        this.offset = offset;
        this.connFigure = connFigure;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.draw2d.Locator#relocate(org.eclipse.draw2d.IFigure)
     */
    public void relocate(IFigure figure) {
        Dimension nameSize = FigureUtilities.getTextExtents(name, figure.getFont());
        figure.setSize(traceSize);
        Point location;
        if (position.equals("start")) { //$NON-NLS-1$
            location = connFigure.getStart();
        } else if (position.equals("end")) { //$NON-NLS-1$
            location = connFigure.getEnd();
        } else {
            location = connFigure.getPoints().getMidpoint();
        }
        Point offsetCopy = offset.getCopy();
        offsetCopy.translate(location);
        if ((connFigure.getStart().y == connFigure.getEnd().y)
                || (Math.abs(connFigure.getEnd().y - connFigure.getStart().y) < 70)) {
            offsetCopy.translate(-(traceSize.width / 2), nameSize.height + 16);
        } else {
            offsetCopy.translate(-(traceSize.width / 2), -(traceSize.height / 2) + 16);
        }
        figure.setLocation(offsetCopy);
    }
}
