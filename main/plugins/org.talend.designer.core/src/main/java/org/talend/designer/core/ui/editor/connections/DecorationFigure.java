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
import org.eclipse.draw2d.PolygonDecoration;
import org.eclipse.draw2d.PositionConstants;
import org.eclipse.draw2d.RotatableDecoration;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Insets;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;
import org.talend.commons.ui.utils.image.ColorUtils;
import org.talend.core.model.process.IConnection;

/**
 * DOC hwang class global comment. Detailled comment
 */
public class DecorationFigure extends PolygonDecoration implements RotatableDecoration {

    private int OFFSET = 1;

    private Dimension size = new Dimension(8, 11);

    private int alignment;

    private boolean isSource = false;

    private IConnection connection;

    private String title = "O";

    private Color black = null;

    private RGB white = new RGB(255, 255, 255);

    private Font font = new Font(Display.getDefault(), "courrier", 6, SWT.NORMAL);

    public DecorationFigure(ConnectionFigure parent, boolean isSource) {
        this.isSource = isSource;
        setAlignment(PositionConstants.CENTER);
        init(parent);
    }

    /**
     * Calculates the necessary size to display the Image within the figure's client area.
     * 
     * @see org.eclipse.draw2d.Figure#getPreferredSize(int, int)
     */
    @Override
    public Dimension getPreferredSize(int wHint, int hHint) {
        if (getInsets() == NO_INSETS) {
            return size;
        }
        Insets i = getInsets();
        return size.getExpanded(i.getWidth(), i.getHeight());
    }

    /**
     * @see org.eclipse.draw2d.Figure#paintFigure(Graphics)
     */
    @Override
    public void paintFigure(Graphics graphics) {
        super.paintFigure(graphics);
        adjustAlignment();
        Rectangle area = getClientArea();
        int y = (area.height - size.height) / 2 + area.y;
        int x = (area.width - size.width) / 2 + area.x;
        Point point = new Point(x, y);
        adjustPosition(point);
        graphics.setFont(font);
        graphics.setForegroundColor(black);
        if (isSource) {
            switch (this.connection.getLineStyle()) {
            case RUN_IF:
            case ON_SUBJOB_OK:
            case ON_SUBJOB_ERROR:
            case ON_COMPONENT_OK:
            case ON_COMPONENT_ERROR:
                title = "T"; //$NON-NLS-1$
                graphics.drawString(title, point);
                break;
            default:
                graphics.drawString("O", point);//$NON-NLS-1$
                break;
            }
            return;
        }
        graphics.drawString(title, point);
    }

    private void adjustPosition(Point point) {
        if (isSource) {
            switch (alignment) {
            case PositionConstants.NORTH:
                point.x = point.x + 2;
                point.y = point.y + 1;
                break;
            case PositionConstants.SOUTH:
                point.x = point.x + 2;
                point.y = point.y + 1;
                break;
            case PositionConstants.EAST:
                point.x = point.x + 1;
                point.y = point.y + 1;
                break;
            case PositionConstants.WEST:
                point.x = point.x + 2;
                point.y = point.y + 1;
                break;
            default:
                break;
            }
        } else if (title.equals("I")) {
            switch (alignment) {
            case PositionConstants.NORTH:
                point.x = point.x + 3;
                point.y = point.y + 2;
                break;
            case PositionConstants.SOUTH:
                point.x = point.x + 3;
                point.y = point.y + 2;
                break;
            case PositionConstants.EAST:
                point.x = point.x + 2;
                point.y = point.y + 1;
                break;
            case PositionConstants.WEST:
                point.x = point.x + 3;
                point.y = point.y + 1;
                break;
            default:
                break;
            }
        } else if (title.equals("T")) {
            switch (alignment) {
            case PositionConstants.NORTH:
                point.x = point.x + 2;
                point.y = point.y + 2;
                break;
            case PositionConstants.SOUTH:
                point.x = point.x + 2;
                point.y = point.y + 2;
                break;
            case PositionConstants.EAST:
                point.x = point.x + 1;
                point.y = point.y + 1;
                break;
            case PositionConstants.WEST:
                point.x = point.x + 2;
                point.y = point.y + 1;
                break;
            default:
                break;
            }
        } else if (title.equals("L")) {
            switch (alignment) {
            case PositionConstants.NORTH:
                point.x = point.x + 2;
                point.y = point.y + 2;
                break;
            case PositionConstants.SOUTH:
                point.x = point.x + 2;
                point.y = point.y + 2;
                break;
            case PositionConstants.EAST:
                point.x = point.x + 2;
                point.y = point.y + 1;
                break;
            case PositionConstants.WEST:
                point.x = point.x + 3;
                point.y = point.y + 1;
                break;
            default:
                break;
            }
        } else if (title.equals("M")) {
            switch (alignment) {
            case PositionConstants.NORTH:
                point.x = point.x + 1;
                point.y = point.y + 1;
                break;
            case PositionConstants.SOUTH:
                point.x = point.x + 1;
                point.y = point.y + 1;
                break;
            case PositionConstants.EAST:
                point.y = point.y + 1;
                break;
            case PositionConstants.WEST:
                point.x = point.x + 1;
                point.y = point.y + 1;
                break;
            default:
                break;
            }
        }
    }

    private void adjustAlignment() {
        if (this.getPoints().size() < 5) {
            return;
        }
        Point first = this.getPoints().getFirstPoint();
        Point middle = this.getPoints().getMidpoint();
        Point last = this.getPoints().getLastPoint();
        if (first.x == last.x) {
            if (middle.x > first.x) {
                setAlignment(PositionConstants.EAST);
            } else {
                setAlignment(PositionConstants.WEST);
            }
        }
        if (first.y == last.y) {
            if (middle.y > first.y) {
                setAlignment(PositionConstants.SOUTH);
            } else {
                setAlignment(PositionConstants.NORTH);
            }
        }

    }

    /**
     * Sets the alignment of the Image within this Figure. The alignment comes into play when the ImageFigure is larger
     * than the Image. The alignment could be any valid combination of the following:
     * 
     * <UL>
     * <LI>PositionConstants.NORTH</LI>
     * <LI>PositionConstants.SOUTH</LI>
     * <LI>PositionConstants.EAST</LI>
     * <LI>PositionConstants.WEST</LI>
     * <LI>PositionConstants.CENTER or PositionConstants.NONE</LI>
     * </UL>
     * 
     * @param flag A constant indicating the alignment
     */
    public void setAlignment(int flag) {
        alignment = flag;
    }

    public void init(ConnectionFigure parent) {
        Color whiteColor = ColorUtils.getCacheColor(white);
        black = ColorUtils.getCacheColor(new RGB(0, 0, 0));
        this.setBackgroundColor(whiteColor);
        this.setForegroundColor(black);
        this.connection = parent.getConnection();
        if (this.connection == null) {
            return;
        }
        switch (this.connection.getLineStyle()) {
        case FLOW_MAIN:
            String comName = this.connection.getTarget().getComponent().getName();
            if (comName.equals("tMap") || comName.equals("tXMLMap")) {
                title = "M"; //$NON-NLS-1$
            } else {
                title = "I";
            }
            break;
        case FLOW_REF:
        case TABLE_REF:
            title = "L"; //$NON-NLS-1$
            break;
        case RUN_IF:
        case ON_SUBJOB_OK:
        case ON_SUBJOB_ERROR:
        case ON_COMPONENT_OK:
        case ON_COMPONENT_ERROR:
            title = "T"; //$NON-NLS-1$
            break;
        default:
            title = "I";//$NON-NLS-1$
            break;
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.draw2d.RotatableDecoration#setReferencePoint(org.eclipse.draw2d.geometry.Point)
     */
    @Override
    public void setReferencePoint(Point p) {
        super.setReferencePoint(p);
        // if (isSource) {
        // setRotation(Math.PI);
        // } else {
        // EConnectionCategory category = connection.getLineStyle().getCategory();
        // if (category == EConnectionCategory.MAIN && connection.getLineStyle() != EConnectionType.FLOW_REF) {
        // setRotation(0.0);
        // }
        // }
    }

    public void disposeResource() {
        // if (this.getBackgroundColor() != null && !this.getBackgroundColor().isDisposed()) {
        // this.getBackgroundColor().dispose();
        // }
        // if (black != null && !black.isDisposed()) {
        // black.dispose();
        // }
        if (font != null && !font.isDisposed()) {
            font.dispose();
        }
    }

}
