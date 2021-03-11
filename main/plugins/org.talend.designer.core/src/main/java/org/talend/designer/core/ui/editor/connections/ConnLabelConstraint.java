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
import org.talend.commons.ui.utils.workbench.gef.SimpleHtmlFigure;

/**
 * Object that will define the position ofthe differents label for a connection. <br/>
 *
 * $Id$
 *
 */
public class ConnLabelConstraint implements Locator {

    String text;

    String position;

    Point offset;

    PolylineConnection connFigure;

    Dimension minimum;

    /**
     * Sets the items that will be use to set the position of the label.
     *
     * @param text
     * @param position
     * @param offset
     * @param connFigure
     */
    public ConnLabelConstraint(String text, String position, Point offset, PolylineConnection connFigure) {
        this.text = text;
        this.position = position;
        this.offset = offset;
        this.connFigure = connFigure;

    }

    public ConnLabelConstraint(Dimension minimum, String position, Point offset, PolylineConnection connFigure) {
        this.position = position;
        this.offset = offset;
        this.connFigure = connFigure;
        this.minimum = minimum;

    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.draw2d.Locator#relocate(org.eclipse.draw2d.IFigure)
     */
    public void relocate(IFigure figure) {
        if (!(figure instanceof SimpleHtmlFigure)) {
            this.minimum = FigureUtilities.getTextExtents(text, figure.getFont());
        }

        figure.setSize(minimum);
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
        offsetCopy.translate(-(minimum.width / 2), 0);
        figure.setLocation(offsetCopy);
    }
}
