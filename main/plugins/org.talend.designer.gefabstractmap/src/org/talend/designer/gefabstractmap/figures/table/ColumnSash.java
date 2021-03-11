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
package org.talend.designer.gefabstractmap.figures.table;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Cursors;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.MouseEvent;
import org.eclipse.draw2d.MouseMotionListener;
import org.eclipse.draw2d.PositionConstants;
import org.eclipse.draw2d.Shape;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.graphics.Cursor;
import org.talend.designer.gefabstractmap.figures.sash.ISash;

/**
 * DOC talend class global comment. Detailled comment
 */
public class ColumnSash extends Shape implements ISash {

    // this one should be an even number
    private int SEPARATOR_WIDTH = 10;

    private int ZONE_MIN_SIZE = 5;

    private boolean isMouseHover = false;

    private ITable table;

    private TableColumn leftColumn;

    private TableColumn rightColumn;

    public ColumnSash(ITable table) {
        this.table = table;
        this.addMouseMotionListener(new MouseMotionListener() {

            @Override
            public void mouseDragged(MouseEvent me) {

            }

            @Override
            public void mouseEntered(MouseEvent me) {
                isMouseHover = true;
                final Cursor directionalCursor = Cursors.getDirectionalCursor(PositionConstants.EAST, isMirrored());
                setCursor(directionalCursor);
                repaint();
            }

            @Override
            public void mouseExited(MouseEvent me) {
                isMouseHover = false;
                repaint();
            }

            @Override
            public void mouseHover(MouseEvent me) {

            }

            @Override
            public void mouseMoved(MouseEvent me) {

            }

        });

    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.draw2d.Shape#fillShape(org.eclipse.draw2d.Graphics)
     */
    @Override
    protected void fillShape(Graphics graphics) {
        graphics.setBackgroundColor(ColorConstants.menuBackground);
        Rectangle bounds = getBounds();
        graphics.fillRectangle(bounds.x, bounds.y, bounds.width, bounds.height);

    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.draw2d.Shape#outlineShape(org.eclipse.draw2d.Graphics)
     */
    @Override
    protected void outlineShape(Graphics graphics) {
        float lineInset = Math.max(1.0f, getLineWidthFloat()) / 2.0f;
        int inset1 = (int) Math.floor(lineInset);
        int inset2 = (int) Math.ceil(lineInset);

        Rectangle r = Rectangle.SINGLETON.setBounds(getBounds());
        r.x += inset1;
        r.y += inset1;
        r.width -= inset1 + inset2;
        r.height -= inset1 + inset2;

        if (isMouseHover) {
            graphics.setForegroundColor(ColorConstants.orange);
        } else {
            graphics.setForegroundColor(ColorConstants.buttonLightest);
        }

        int X = r.x + r.width / 2;
        graphics.drawLine(X, r.y, X, r.y + r.height);

    }

    public int getSashWidth() {
        return SEPARATOR_WIDTH;
    }

    public int getZoneMinSize() {
        return ZONE_MIN_SIZE;
    }

    public ITable getTable() {
        return this.table;
    }

    public TableColumn getLeftColumn() {
        return leftColumn;
    }

    public void setLeftColumn(TableColumn leftColumn) {
        this.leftColumn = leftColumn;
    }

    public TableColumn getRightColumn() {
        return rightColumn;
    }

    public void setRightColumn(TableColumn rightColumn) {
        this.rightColumn = rightColumn;
    }

}
