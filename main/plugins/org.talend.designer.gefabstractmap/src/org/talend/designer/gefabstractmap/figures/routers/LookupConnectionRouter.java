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
package org.talend.designer.gefabstractmap.figures.routers;

import org.eclipse.draw2d.Connection;
import org.eclipse.draw2d.ConnectionAnchor;
import org.eclipse.draw2d.FanRouter;
import org.eclipse.draw2d.PolylineConnection;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.PointList;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.draw2d.geometry.Vector;

/**
 * DOC talend class global comment. Detailled comment
 */
public class LookupConnectionRouter extends FanRouter {

    private int offset;

    private static Vector UP = new Vector(0, -1), DOWN = new Vector(0, 1), LEFT = new Vector(-1, 0), RIGHT = new Vector(1, 0);

    public void route(Connection conn) {
        PointList points = conn.getPoints();
        points.removeAllPoints();
        Point p;
        Point startPoint = getStartPoint(conn);
        Point endPoint = getEndPoint(conn);
        conn.translateToRelative(p = startPoint);
        points.addPoint(p);

        if (conn instanceof PolylineConnection) {
            PolylineConnection polylineConn = (PolylineConnection) conn;
            // polylineConn.getta
        }

        // conn.getTargetDecoration()

        conn.translateToRelative(p = getReferrencedPoint(startPoint));
        points.addPoint(p);

        conn.translateToRelative(p = getReferrencedPoint(endPoint));
        points.addPoint(p);

        conn.translateToRelative(p = endPoint);
        points.addPoint(p);

        conn.setPoints(points);
    }

    private Point getReferrencedPoint(Point point) {
        Point refpoint = point.getCopy();
        // if (getOffset() < minOffset) {
        // setOffset(minOffset);
        // }
        refpoint.x = refpoint.x + getOffset();
        return refpoint;
    }

    protected Vector getDirection(Rectangle r, Point p) {
        int i, distance = Math.abs(r.x - p.x);
        Vector direction;

        direction = LEFT;

        i = Math.abs(r.y - p.y);
        if (i <= distance) {
            distance = i;
            direction = UP;
        }

        i = Math.abs(r.bottom() - p.y);
        if (i <= distance) {
            distance = i;
            direction = DOWN;
        }

        i = Math.abs(r.right() - p.x);
        if (i < distance) {
            distance = i;
            direction = RIGHT;
        }

        return direction;
    }

    protected Vector getEndDirection(Connection conn) {
        ConnectionAnchor anchor = conn.getTargetAnchor();
        Point p = getEndPoint(conn);
        Rectangle rect;
        if (anchor.getOwner() == null) {
            rect = new Rectangle(p.x - 1, p.y - 1, 2, 2);
        } else {
            rect = conn.getTargetAnchor().getOwner().getBounds().getCopy();
            conn.getTargetAnchor().getOwner().translateToAbsolute(rect);
        }
        return getDirection(rect, p);
    }

    public int getOffset() {
        return offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

}
