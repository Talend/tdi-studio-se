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

import org.eclipse.draw2d.Connection;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.PointList;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.Request;
import org.eclipse.gef.requests.CreateConnectionRequest;
import org.eclipse.gef.requests.ReconnectRequest;
import org.eclipse.gmf.runtime.draw2d.ui.figures.PolylineConnectionEx;
import org.eclipse.gmf.runtime.draw2d.ui.internal.routers.BorderItemRectilinearRouter;
import org.talend.core.model.process.EConnectionCategory;
import org.talend.core.model.process.EConnectionType;
import org.talend.core.model.process.IConnection;
import org.talend.designer.core.model.process.ConnectionManager;
import org.talend.designer.core.ui.editor.nodes.Node;
import org.talend.designer.core.ui.editor.process.ProcessPart;

/**
 * DOC hwang class global comment. Detailled comment
 */
public class TalendBorderItemRectilinearRouter extends BorderItemRectilinearRouter {

    private final static int OFFSET = 16;

    private Request request;

    public TalendBorderItemRectilinearRouter() {

    }

    public TalendBorderItemRectilinearRouter(Request request) {
        this.request = request;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.eclipse.gmf.runtime.draw2d.ui.internal.routers.ObliqueRouter#routeBendpoints(org.eclipse.draw2d.Connection)
     */
    @Override
    public void routeBendpoints(Connection conn) {
        super.routeBendpoints(conn);
        boolean horizontalHandle = manualHorizontalScrolBarCase(conn);
        manualPosition(conn, horizontalHandle);
    }

    private boolean manualHorizontalScrolBarCase(Connection conn) {
        PointList points = conn.getPoints();
        Point start = getStartPoint(conn);
        Point end = getEndPoint(conn);
        boolean handle = false;
        boolean processHandle = false;
        if ((request != null) && (request instanceof CreateConnectionRequest)) {
            start = conn.getSourceAnchor().getReferencePoint();
            CreateConnectionRequest createRequest = (CreateConnectionRequest) request;
            if (createRequest.getTargetEditPart() != null
                    && createRequest.getTargetEditPart() != createRequest.getSourceEditPart()
                    && createRequest.getTargetEditPart().getModel() instanceof Node) {
                PointList pointList = new PointList();
                conn.translateToRelative(start);
                pointList.addPoint(start);
                Point targetPoint = conn.getTargetAnchor().getReferencePoint();
                conn.translateToRelative(targetPoint);
                pointList.addPoint(targetPoint);
                conn.setPoints(pointList);
                handle = true;
            } else if (createRequest.getTargetEditPart() != null
                    && createRequest.getTargetEditPart() != createRequest.getSourceEditPart()
                    && createRequest.getTargetEditPart() instanceof ProcessPart) {
                processHandle = true;
            }
        }
        if (conn instanceof ConnectionFigure) {
            IConnection connection = ((ConnectionFigure) conn).getConnection();
            EConnectionType lineStyle = connection.getLineStyle();
            EConnectionCategory category = lineStyle.getCategory();
            if (category == EConnectionCategory.OTHER
                    && (lineStyle == EConnectionType.ON_COMPONENT_ERROR || lineStyle == EConnectionType.ON_COMPONENT_OK
                            || lineStyle == EConnectionType.ON_SUBJOB_ERROR || lineStyle == EConnectionType.ON_SUBJOB_OK || lineStyle == EConnectionType.RUN_IF)) {
                return handle || processHandle;
            }
        }

        if (!handle) {
            conn.translateToRelative(start);
            points.setPoint(start, 0);
            conn.translateToRelative(end);
            points.setPoint(end, points.size() - 1);
            conn.setPoints(points);
        }
        return handle || processHandle;
    }

    private boolean manualPosition(Connection conn, boolean horizontalHandle) {
        boolean isCreation = false;
        boolean isReconnect = false;
        boolean alreadyHandle = false;
        if (!(conn instanceof PolylineConnectionEx)) {
            return false;
        }
        if (this.request != null) {
            if (request instanceof CreateConnectionRequest) {
                isCreation = true;
            } else if (request instanceof ReconnectRequest) {
                isReconnect = true;
            }
        }
        IConnection connection = null;
        EConnectionCategory category = null;
        EConnectionType lineStyle = null;
        Rectangle sourceBounds = null;
        Rectangle targetBounds = null;
        if (conn instanceof ConnectionFigure) {
            connection = ((ConnectionFigure) conn).getConnection();
            if (!(connection.getSource() instanceof Node)) {
                return false;
            }
            lineStyle = connection.getLineStyle();
            category = lineStyle.getCategory();
            sourceBounds = new Rectangle(((Node) connection.getSource()).getLocation(), ((Node) connection.getSource()).getSize());
            targetBounds = new Rectangle(((Node) connection.getTarget()).getLocation(), ((Node) connection.getTarget()).getSize());
        }

        if (lineStyle == null) {
            lineStyle = ConnectionManager.getNewConnectionType();
            category = lineStyle.getCategory();
        }

        if (isCreation) {
            if (((CreateConnectionRequest) request).getSourceEditPart() == null) {
                return false;
            }

            if ((((CreateConnectionRequest) request).getTargetEditPart() == null) || horizontalHandle) {
                if (conn.getPoints().size() <= 2) {
                    makeUpConnection(conn);
                }
                return false;
            }
        }

        if ((sourceBounds == null) && isCreation) {
            Node source = (Node) ((CreateConnectionRequest) request).getSourceEditPart().getModel();
            if (source != null) {
                sourceBounds = new Rectangle(source.getLocation(), source.getSize());
            }
        }

        if ((targetBounds == null) && isCreation) {
            Object obj = ((CreateConnectionRequest) request).getTargetEditPart().getModel();
            if (obj instanceof Node) {
                Node target = (Node) obj;
                if (target != null) {
                    targetBounds = new Rectangle(target.getLocation(), target.getSize());
                }
            } else {
                Point lastPoint = conn.getPoints().getLastPoint();
                targetBounds = new Rectangle(lastPoint, new Dimension(2 * OFFSET, 2 * OFFSET));
            }

        }
        if (isReconnect) {
            if (((ReconnectRequest) request).getTarget() != null) {
                Object obj = ((ReconnectRequest) request).getTarget().getModel();
                if (obj instanceof Node) {
                    Node target = (Node) obj;
                    if (target != null) {
                        targetBounds = new Rectangle(target.getLocation(), target.getSize());
                    }
                }
            } else {
                Point lastPoint = conn.getPoints().getLastPoint();
                targetBounds = new Rectangle(lastPoint, new Dimension(2 * OFFSET, 2 * OFFSET));
            }
        }

        if (targetBounds == null) {
            return false;
        }

        ((PolylineConnectionEx) conn).setRoundedBendpointsRadius(32);
        PointList points = conn.getPoints();
        PointList pointList = new PointList();
        Point firstpoint = points.getFirstPoint();
        Point lastpoint = points.getLastPoint();
        if (category == EConnectionCategory.MAIN && lineStyle != EConnectionType.FLOW_REF) {
            if ((sourceBounds.x > targetBounds.x) && (sourceBounds.y == targetBounds.y)) {
                pointList.addPoint(firstpoint);
                pointList.addPoint(firstpoint.x + OFFSET, firstpoint.y);
                pointList.addPoint(firstpoint.x + OFFSET, firstpoint.y + 2 * OFFSET);

                pointList.addPoint(lastpoint.x - OFFSET, lastpoint.y + 2 * OFFSET);
                pointList.addPoint(lastpoint.x - OFFSET, lastpoint.y);
                pointList.addPoint(lastpoint);
                alreadyHandle = true;
            } else if ((sourceBounds.x > targetBounds.x)
                    || (sourceBounds.getTopRight().x == targetBounds.getTopLeft().x)
                    || ((sourceBounds.getTopLeft().x == targetBounds.getTopRight().x) && (sourceBounds.getTopLeft().y != targetBounds
                            .getTopRight().y))) {
                pointList.addPoint(firstpoint);
                pointList.addPoint(firstpoint.x + OFFSET, firstpoint.y);
                pointList.addPoint(firstpoint.x + OFFSET, (sourceBounds.getCenter().y + targetBounds.getCenter().y) / 2);

                pointList.addPoint(lastpoint.x - OFFSET, (sourceBounds.getCenter().y + targetBounds.getCenter().y) / 2);
                pointList.addPoint(lastpoint.x - OFFSET, lastpoint.y);
                pointList.addPoint(lastpoint);
                alreadyHandle = true;
            } else if (sourceBounds.x == targetBounds.x) {
                pointList.addPoint(firstpoint);
                pointList.addPoint(firstpoint.x + 2 * OFFSET, firstpoint.y);
                pointList.addPoint(firstpoint.x + 2 * OFFSET, (sourceBounds.getCenter().y + targetBounds.getCenter().y) / 2);

                pointList.addPoint(lastpoint.x - 2 * OFFSET, (sourceBounds.getCenter().y + targetBounds.getCenter().y) / 2);
                pointList.addPoint(lastpoint.x - 2 * OFFSET, lastpoint.y);
                pointList.addPoint(lastpoint);
                alreadyHandle = true;
            } else if (Math.abs(sourceBounds.getCenter().y - targetBounds.getCenter().y) == 4 * OFFSET) {
                if ((firstpoint.x + lastpoint.x) / 2 - OFFSET <= firstpoint.x) {
                    return false;
                }
                ((PolylineConnectionEx) conn).setRoundedBendpointsRadius(16);
            }
        } else if (category == EConnectionCategory.OTHER
                && (lineStyle == EConnectionType.FLOW_REF || lineStyle == EConnectionType.TABLE_REF)) {
            if (targetBounds.y == sourceBounds.y) {
                pointList.addPoint(firstpoint);
                pointList.addPoint(firstpoint.x, firstpoint.y + OFFSET);

                pointList.addPoint(lastpoint.x, firstpoint.y + OFFSET);
                pointList.addPoint(lastpoint);
                alreadyHandle = true;
            } else if ((targetBounds.getTopRight().y == sourceBounds.getBottomLeft().y)) {
                pointList.addPoint(firstpoint);
                pointList.addPoint(firstpoint.x, firstpoint.y - OFFSET);

                pointList.addPoint(lastpoint.x, firstpoint.y - OFFSET);
                pointList.addPoint(lastpoint);
                alreadyHandle = true;
            } else if (targetBounds.getBottomLeft().y == sourceBounds.getTopRight().y) {
                pointList.addPoint(firstpoint);
                pointList.addPoint(firstpoint.x, firstpoint.y + OFFSET);

                pointList.addPoint(lastpoint.x, firstpoint.y + OFFSET);
                pointList.addPoint(lastpoint);
                alreadyHandle = true;
            } else if (Math.abs(sourceBounds.x - targetBounds.x) == 4 * OFFSET) {
                ((PolylineConnectionEx) conn).setRoundedBendpointsRadius(16);
            }
        }
        if (alreadyHandle && pointList.size() > 0) {
            conn.setPoints(pointList);
        }

        Point midpoint = conn.getPoints().getMidpoint();
        if ((midpoint.x == lastpoint.x && midpoint.y == firstpoint.y) || pointList.size() == 0) {
            makeUpConnection(conn);
        }
        return alreadyHandle;
    }

    private void makeUpConnection(Connection conn) {
        Point firstPoint = conn.getPoints().getFirstPoint();
        Point lastPoint = conn.getPoints().getLastPoint();
        PointList pointList = new PointList();
        pointList.addPoint(firstPoint);
        pointList.addPoint((lastPoint.x + firstPoint.x) / 2, firstPoint.y);

        pointList.addPoint((lastPoint.x + firstPoint.x) / 2, (lastPoint.y + firstPoint.y) / 2);

        pointList.addPoint((lastPoint.x + firstPoint.x) / 2, lastPoint.y);
        pointList.addPoint(lastPoint);

        conn.setPoints(pointList);
    }
}
