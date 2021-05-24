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

import java.util.List;

import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.talend.core.model.process.EConnectionCategory;
import org.talend.core.model.process.EConnectionType;
import org.talend.core.ui.process.IGraphicalNode;
import org.talend.designer.core.model.process.ConnectionManager;
import org.talend.designer.core.ui.editor.connections.Connection;

/**
 * DOC Talend class global comment. Detailled comment
 */
public class DummyNodeAnchor extends NodeAnchor {

    IGraphicalNode source, target = null;

    boolean isTargetAnchor = false;

    public DummyNodeAnchor(NodeFigure owner, IGraphicalNode source, boolean isTargetAnchor) {
        super(owner, source, isTargetAnchor);
        this.source = source;
        this.isTargetAnchor = isTargetAnchor;
    }

    public DummyNodeAnchor(NodeFigure owner, IGraphicalNode source, IGraphicalNode target, boolean isTargetAnchor) {
        super(owner, source, target, isTargetAnchor);
        this.source = source;
        this.target = target;
        this.isTargetAnchor = isTargetAnchor;
    }

    @Override
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
            return getSourceAnchorLocation(reference);
        } else {
            return getLocationForMultipleConnections(connectionId);
        }
    }

    public Point getSourceAnchorLocation(Point reference) {
        Point sourceCenter = new Rectangle(source.getLocation(), source.getSize()).getCenter();
        Point sourcePoint = getDirectionPosition(sourceCenter, reference);

        if (!isTargetAnchor && sourcePoint == null) {
            return super.getLocation(reference);
        }
        if (sourcePoint != null) {
            if (!isTargetAnchor) {
                return sourcePoint;
            }

        }
        if (target != null) {
            Point targetCenter = new Rectangle(target.getLocation(), target.getSize()).getCenter();
            Point targetPoint = getDirectionPosition(targetCenter, reference);

            if (isTargetAnchor && targetPoint == null) {
                return super.getLocation(reference);
            }
            if (targetPoint != null) {
                if (isTargetAnchor) {
                    return targetPoint;
                }

            }
        }
        return super.getLocation(reference);
    }

    private Point getDirectionPosition(Point figCenter, Point reference) {
        Dimension nodeSize = this.source.getSize();
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

            if (!isTargetAnchor) {
                result.x = figCenter.x + nodeSize.width / 2;
            } else {
                result.x = figCenter.x - nodeSize.width / 2;
            }

            return result;
        } else if (category == EConnectionCategory.OTHER
                && (lineStyle == EConnectionType.FLOW_REF || lineStyle == EConnectionType.TABLE_REF)) {
            if (target != null) {
                int sourceY = this.source.getPosY();
                int targetY = this.target.getPosY();
                Rectangle sourceBounds = new Rectangle(this.source.getLocation(), this.source.getSize());
                Rectangle targetBounds = new Rectangle(this.target.getLocation(), this.target.getSize());
                if (!isTargetAnchor) {

                    if (target.equals(source)) {
                        Point tempPoint = new Point(reference.x - nodeSize.width / 2, reference.y - nodeSize.height / 2);
                        targetBounds = new Rectangle(tempPoint, this.target.getSize());
                        targetY = reference.y - nodeSize.height / 2;
                    }

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
                //
                if (sourceY < targetY) {
                    result.y = figCenter.y - nodeSize.height / 2;
                } else {
                    result.y = figCenter.y + nodeSize.height / 2;
                }
                return result;
            }
        }
        return null;
    }

}
