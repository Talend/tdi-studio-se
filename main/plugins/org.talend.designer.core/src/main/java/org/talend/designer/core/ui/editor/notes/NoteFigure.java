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
package org.talend.designer.core.ui.editor.notes;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.PointList;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;
import org.talend.designer.core.utils.ResourceDisposeUtil;

/**
 */
public class NoteFigure extends Figure {

    private static final int CORNER_SIZE = 12;

    private Label label = new Label();

    public NoteFigure() {
        setBackgroundColor(new Color(Display.getCurrent(), 255, 255, 203));
        setForegroundColor(ColorConstants.orange);

        add(label);
        label.setForegroundColor(ColorConstants.black);
    }

    protected void paintFigure(Graphics graphics) {
        Rectangle rect = getBounds().getCopy();

        graphics.translate(getLocation());

        // fill the note
        if (isOpaque()) {
            PointList outline = new PointList();

            outline.addPoint(0, 0);
            outline.addPoint(rect.width - CORNER_SIZE, 0);
            outline.addPoint(rect.width - 1, CORNER_SIZE);
            outline.addPoint(rect.width - 1, rect.height - 1);
            outline.addPoint(0, rect.height - 1);

            graphics.fillPolygon(outline);
        }

        // draw the inner outline
        PointList innerLine = new PointList();

        innerLine.addPoint(rect.width - CORNER_SIZE - 1, 0);
        innerLine.addPoint(rect.width - CORNER_SIZE - 1, CORNER_SIZE);
        innerLine.addPoint(rect.width - 1, CORNER_SIZE);
        innerLine.addPoint(rect.width - CORNER_SIZE - 1, 0);
        innerLine.addPoint(0, 0);
        innerLine.addPoint(0, rect.height - 1);
        innerLine.addPoint(rect.width - 1, rect.height - 1);
        innerLine.addPoint(rect.width - 1, CORNER_SIZE);

        graphics.drawPolygon(innerLine);

        graphics.translate(getLocation().getNegated());
        label.setLocation(new Point(getLocation().x + CORNER_SIZE, getLocation().y + CORNER_SIZE));
        label.setSize(getSize().width - CORNER_SIZE * 2, getSize().height - CORNER_SIZE * 2);

    }

    public String getText() {
        return label.getText();
    }

    public void setText(String text) {
        label.setText(text);
    }

    /**
     * Getter for label.
     *
     * @return the label
     */
    public Label getLabel() {
        return this.label;
    }

    public void disposeColors() {
        ResourceDisposeUtil.disposeColor(this.getBackgroundColor());
        // if (this.getForegroundColor() != ColorConstants.orange)
        // ResourceDisposeUtil.disposeColor(this.getForegroundColor());
        // if (this.getLabel() != null) {
        // if (this.getLabel().getForegroundColor() != ColorConstants.black)
        // ResourceDisposeUtil.disposeColor(this.getLabel().getForegroundColor());
        // }
    }
}
