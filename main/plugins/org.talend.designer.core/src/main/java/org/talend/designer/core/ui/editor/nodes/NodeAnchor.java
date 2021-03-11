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
package org.talend.designer.core.ui.editor.nodes;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.draw2d.ChopboxAnchor;
import org.eclipse.draw2d.FreeformViewport;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.ScalableFreeformLayeredPane;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.talend.commons.ui.gmf.draw2d.LineSeg;
import org.talend.commons.ui.gmf.draw2d.LineSeg.KeyPoint;
import org.talend.core.model.process.EConnectionCategory;
import org.talend.core.model.process.EConnectionType;
import org.talend.core.model.process.IConnection;
import org.talend.core.ui.process.IGraphicalNode;
import org.talend.designer.core.DesignerPlugin;
import org.talend.designer.core.model.process.ConnectionManager;
import org.talend.designer.core.ui.editor.connections.Connection;
import org.talend.designer.core.ui.preferences.TalendDesignerPrefConstants;

/**
 * DOC bqian class global comment. Detailled comment
 */
public class NodeAnchor extends ChopboxAnchor {

    IGraphicalNode source, target = null;

    IConnection connection = null;

    boolean isTargetAnchor = false;

    Rectangle sourceRect, targetRect;

    Point sourceLocation, targetLocation;

    /**
     * bqian NodeAnchor constructor comment.
     *
     * @param owner
     */
    public NodeAnchor(NodeFigure owner, IGraphicalNode source, boolean isTargetAnchor) {
        super(owner);
        this.source = source;
        this.isTargetAnchor = isTargetAnchor;
    }

    public NodeAnchor(NodeFigure owner, IGraphicalNode source, IGraphicalNode target, boolean isTargetAnchor) {
        super(owner);
        this.source = source;
        this.target = target;
        this.isTargetAnchor = isTargetAnchor;
    }

    public void setTarget(IGraphicalNode target) {
        this.target = target;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.draw2d.ChopboxAnchor#getLocation(org.eclipse.draw2d.geometry.Point)
     */
    @Override
    public Point getLocation(Point reference) {
        if (target != null && !target.equals(source)) {
            sourceLocation = new Point(source.getLocation());
            targetLocation = new Point(target.getLocation());
            Point diff = new Point(0, 0);
            if (!isTargetAnchor) {
                if (((reference.y - (target.getSize().height / 2)) != targetLocation.y)
                        || ((reference.x - (target.getSize().width / 2)) != targetLocation.x)) {
                    FreeformViewport viewport = getViewport();
                    if (viewport != null) {
                        int y = viewport.getVerticalRangeModel().getValue();
                        int x = viewport.getHorizontalRangeModel().getValue();
                        diff = new Point(-x, -y);
                    }
                }
            } else {
                if (((reference.y - (source.getSize().height / 2)) != sourceLocation.y)
                        || ((reference.x - (source.getSize().width / 2)) != sourceLocation.x)) {
                    FreeformViewport viewport = getViewport();
                    if (viewport != null) {
                        int y = viewport.getVerticalRangeModel().getValue();
                        int x = viewport.getHorizontalRangeModel().getValue();
                        diff = new Point(-x, -y);
                    }
                }
            }
            ScalableFreeformLayeredPane pane = getScalableFreeform();

            sourceRect = new Rectangle(sourceLocation, source.getSize());
            targetRect = new Rectangle(targetLocation, target.getSize());
            if (pane != null) {
                double scale = pane.getScale();
                sourceLocation.performScale(scale);
                targetLocation.performScale(scale);
                sourceRect.performScale(scale);
                targetRect.performScale(scale);
            }
            sourceLocation = sourceLocation.getTranslated(diff);
            targetLocation = targetLocation.getTranslated(diff);
            sourceRect = sourceRect.getTranslated(diff);
            targetRect = targetRect.getTranslated(diff);

            int nb = 0;
            int connectionId = 0;
            for (Connection connection : (List<Connection>) source.getOutgoingConnections()) {
                if (connection.getTarget().equals(target)) {
                    nb++;
                    if (connection.equals(this.connection)) {
                        connectionId = nb;
                    }
                }
            }
            if (nb <= 1) {
                return getLocationForSimpleConnection(reference);
            } else {
                return getLocationForMultipleConnections(connectionId);
            }
        }

        return super.getLocation(reference);
    }

    /**
     * DOC nrousseau Comment method "getViewport".
     *
     * @return
     */
    private FreeformViewport getViewport() {
        IFigure figure = getOwner();
        while (!(figure instanceof FreeformViewport)) {
            figure = figure.getParent();
            if (figure == null) {
                return (FreeformViewport) figure;
            }
        }
        return (FreeformViewport) figure;
    }

    private ScalableFreeformLayeredPane getScalableFreeform() {
        IFigure figure = getOwner();
        while (!(figure instanceof ScalableFreeformLayeredPane)) {
            figure = figure.getParent();
            if (figure == null) {
                return (ScalableFreeformLayeredPane) figure;
            }
        }
        return (ScalableFreeformLayeredPane) figure;
    }

    public Point getLocationForSimpleConnection(Point reference) {
        // if (!isTargetAnchor) {
        // if (connection != null) {
        // System.out.println("Simple: refresh source anchor of:" + source + " to:" + target + " connection:"
        // + connection.getName());
        // } else {
        // System.out.println("Simple: refresh source anchor of:" + source + " to:" + target);
        // }
        // } else {
        // if (connection != null) {
        // System.out.println("Simple: refresh target anchor of:" + source + " to:" + target + " connection:"
        // + connection.getName());
        // } else {
        // System.out.println("Simple: refresh target anchor of:" + source + " to:" + target);
        // }
        // }
        boolean curvedStyle = DesignerPlugin.getDefault().getPreferenceStore()
                .getBoolean(TalendDesignerPrefConstants.EDITOR_LINESTYLE);
        Point sourcePoint = null, targetPoint = null;
        sourcePoint = getDirectionPosition(this.connection, sourceRect.getCenter(), true);
        if ((sourcePoint == null) && !curvedStyle) {
            if ((sourceLocation.y < targetRect.getCenter().y)
                    && (targetRect.getCenter().y < (sourceLocation.y + sourceRect.height))) {
                // contains
                sourcePoint = new Point(sourceRect.getCenter().x, targetRect.getCenter().y);
            }

            if ((sourceLocation.x < targetRect.getCenter().x)
                    && (targetRect.getCenter().x < (sourceLocation.x + sourceRect.width))) {
                // contains
                sourcePoint = new Point(targetRect.getCenter().x, sourceRect.getCenter().y);
            }
        }

        targetPoint = getDirectionPosition(this.connection, targetRect.getCenter(), false);
        if (targetPoint == null) {
            targetPoint = targetRect.getCenter();
        }
        if ((sourcePoint == null) && !curvedStyle) {
            if ((targetLocation.y < sourceRect.getCenter().y)
                    && (sourceRect.getCenter().y < (targetLocation.y + targetRect.height))) {
                // contains
                sourcePoint = new Point(sourceRect.getCenter().x, sourceRect.getCenter().y);
                targetPoint = new Point(targetRect.getCenter().x, sourceRect.getCenter().y);
            }

            if ((targetLocation.x < sourceRect.getCenter().x)
                    && (sourceRect.getCenter().x < (targetLocation.x + targetRect.width))) {
                // contains
                sourcePoint = new Point(sourceRect.getCenter().x, sourceRect.getCenter().y);
                targetPoint = new Point(sourceRect.getCenter().x, targetRect.getCenter().y);
            }
        }

        if (!isTargetAnchor && sourcePoint == null) {
            return super.getLocation(reference);
        }
        if (sourcePoint != null && targetPoint != null) {
            if (!DesignerPlugin.getDefault().getPreferenceStore().getBoolean(TalendDesignerPrefConstants.EDITOR_LINESTYLE)) {
                return calculateLocationFromRef(sourcePoint, targetPoint);
            }
            if (!isTargetAnchor) {
                return sourcePoint;
            } else {
                return targetPoint;
            }

        }
        return super.getLocation(reference);
    }

    private Point getDirectionPosition(IConnection connection, Point figCenter, boolean isSource) {
        if (!DesignerPlugin.getDefault().getPreferenceStore().getBoolean(TalendDesignerPrefConstants.EDITOR_LINESTYLE)) {
            return null;
        }
        Dimension nodeSize = null;
        if (isSource) {
            nodeSize = this.source.getSize();
        } else {
            nodeSize = this.target.getSize();
        }

        EConnectionCategory category = null;
        EConnectionType lineStyle = null;
        if (connection != null) {
            lineStyle = connection.getLineStyle();
            category = lineStyle.getCategory();
        } else {
            lineStyle = ConnectionManager.getNewConnectionType();
            category = lineStyle.getCategory();
        }

        Point result = new Point(figCenter);
        if (category == EConnectionCategory.MAIN && lineStyle != EConnectionType.FLOW_REF) {
            if (isSource) {
                result.x = figCenter.x + nodeSize.width / 2;
            } else {
                result.x = figCenter.x - nodeSize.width / 2;
            }
            return result;
        } else if (category == EConnectionCategory.OTHER
                && (lineStyle == EConnectionType.FLOW_REF || lineStyle == EConnectionType.TABLE_REF)) {
            Rectangle sourceBounds = new Rectangle(this.source.getLocation(), this.source.getSize());
            Rectangle targetBounds = new Rectangle(this.target.getLocation(), this.target.getSize());

            if (isSource) {
                int sourceY = this.source.getPosY();
                int targetY = this.target.getPosY();
                if (sourceY <= targetY) {
                    if ((targetBounds.getTopRight().y == sourceBounds.getBottomLeft().y)) {
                        result.y = figCenter.y - nodeSize.height / 2;
                    } else {
                        result.y = figCenter.y + nodeSize.height / 2;
                    }
                } else {
                    if (targetBounds.getBottomLeft().y == sourceBounds.getTopRight().y) {
                        result.y = figCenter.y + nodeSize.height / 2;
                    } else {
                        result.y = figCenter.y - nodeSize.height / 2;
                    }
                }
                return result;
            }

            int sourceY = this.source.getPosY();
            int targetY = this.target.getPosY();
            if (sourceY < targetY) {
                result.y = figCenter.y - nodeSize.height / 2;
            } else {
                result.y = figCenter.y + nodeSize.height / 2;
            }
            return result;
        }
        return null;
    }

    /**
     * DOC nrousseau Comment method "calculateLocationFromRef".
     *
     * @param sourceRect
     * @param targetRect
     * @param sourcePoint
     * @param targetPoint
     */
    private Point calculateLocationFromRef(Point sourcePoint, Point targetPoint) {
        LineSeg lineSourceToTarget = new LineSeg(sourcePoint, targetPoint);
        List<LineSeg> lineList = new ArrayList<LineSeg>();
        if (!isTargetAnchor) {
            lineList.add(new LineSeg(sourceRect.getTopLeft(), sourceRect.getTopRight()));
            lineList.add(new LineSeg(sourceRect.getTopLeft(), sourceRect.getBottomLeft()));
            lineList.add(new LineSeg(sourceRect.getTopRight(), sourceRect.getBottomRight()));
            lineList.add(new LineSeg(sourceRect.getBottomLeft(), sourceRect.getBottomRight()));
        } else {
            lineList.add(new LineSeg(targetRect.getTopLeft(), targetRect.getTopRight()));
            lineList.add(new LineSeg(targetRect.getTopLeft(), targetRect.getBottomLeft()));
            lineList.add(new LineSeg(targetRect.getTopRight(), targetRect.getBottomRight()));
            lineList.add(new LineSeg(targetRect.getBottomLeft(), targetRect.getBottomRight()));
        }
        int tolerance = 0;
        Point inter = null;
        while (inter == null && (tolerance < 10)) {
            for (LineSeg line : lineList) {
                inter = lineSourceToTarget.intersect(line, tolerance);
                if (inter != null) {
                    return inter;
                }
            }
            tolerance++;
        }
        return super.getLocation(getReferencePoint());
    }

    public Point getLocationForMultipleConnections(int connectionId) {
        // if (!isTargetAnchor) {
        // if (connection != null) {
        // System.out.println("Multiple: refresh source anchor of:" + source + " to:" + target + " connection:"
        // + connection.getName());
        // } else {
        // System.out.println("Multiple: refresh source anchor of:" + source + " to:" + target);
        // }
        // } else {
        // if (connection != null) {
        // System.out.println("Multiple: refresh target anchor of:" + source + " to:" + target + " connection:"
        // + connection.getName());
        // } else {
        // System.out.println("Multiple: refresh target anchor of:" + source + " to:" + target);
        // }
        // }

        if ((sourceLocation.y >= targetLocation.y)
                && ((sourceLocation.y + sourceRect.height) <= (targetLocation.y + targetRect.height))) {
            targetRect = new Rectangle(new Point(targetLocation.x, sourceLocation.y), new Dimension(targetRect.width,
                    sourceRect.height));
        } else if ((sourceLocation.x >= targetLocation.x)
                && ((sourceLocation.x + sourceRect.width) <= (targetLocation.x + targetRect.width))) {
            targetRect = new Rectangle(new Point(sourceLocation.x, targetLocation.y), new Dimension(sourceRect.width,
                    sourceRect.height));
        }

        // will calculate the numerator and denominator for the function to place the points.
        // example: 1/2; 3/4; 1/4; 5/8; 3/8; 7/8; 1/8; 9/16; 7/16; 11/16; 5/16; 13/16; 3/16...
        int num = 1;
        int numPair = 1;
        int numImpair = 1;
        int denom = 2;
        for (int i = 1; i < connectionId + 1; i++) {
            if ((i % 2) == 0) {
                // pair
                if ((numPair + 2) > denom) {
                    denom = denom * 2;
                    numPair = (denom / 2) + 1;
                    numImpair = numPair;
                } else {
                    numPair += 2;
                }

                num = numPair;
            } else {
                // impair
                numImpair -= 2;
                if (numImpair > 0) {
                    num = numImpair;
                }
            }
        }

        LineSeg lineSource, lineTarget;
        if ((sourceLocation.x < targetRect.getCenter().x) && (targetRect.getCenter().x < (sourceLocation.x + sourceRect.width))) {
            lineSource = new LineSeg(sourceRect.getLeft(), sourceRect.getRight());
            lineTarget = new LineSeg(targetRect.getLeft(), targetRect.getRight());
        } else {
            lineSource = new LineSeg(sourceRect.getTop(), sourceRect.getBottom());
            lineTarget = new LineSeg(targetRect.getTop(), targetRect.getBottom());
        }
        Double length = (lineSource.length() * num / denom);
        Point pointSource = new Point();
        lineSource.pointOn(length.longValue(), KeyPoint.ORIGIN, pointSource);

        length = (lineTarget.length() * num / denom);
        Point pointTarget = new Point();
        lineTarget.pointOn(length.longValue(), KeyPoint.ORIGIN, pointTarget);

        if ((sourceLocation.y < pointTarget.y) && (pointTarget.y < (sourceLocation.y + sourceRect.height))) {
            // contains
            pointSource.y = pointTarget.y;
        }

        if ((sourceLocation.x < pointTarget.x) && (pointTarget.x < (sourceLocation.x + sourceRect.width))) {
            // contains
            pointSource.x = pointTarget.x;
        }

        return calculateLocationFromRef(pointSource, pointTarget);
    }

    /**
     * Sets the connection.
     *
     * @param connection the connection to set
     */
    public void setConnection(Connection connection) {
        this.connection = connection;
    }

}
