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
package org.talend.designer.gefabstractmap.figures.anchors;

import org.eclipse.draw2d.ChopboxAnchor;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.PolylineConnection;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.gef.ConnectionEditPart;
import org.eclipse.swt.SWT;
import org.talend.designer.gefabstractmap.figures.manager.TableManager;
import org.talend.designer.gefabstractmap.figures.table.AbstractTableContainer;
import org.talend.designer.gefabstractmap.figures.treesettings.FilterContainer;
import org.talend.designer.gefabstractmap.part.BaseConnectionEditPart;
import org.talend.designer.gefabstractmap.part.LookupConnectionPart;

/**
 * wchen class global comment. Detailled comment
 */
public class FilterTreeAnchor extends ChopboxAnchor {

    private ConnectionEditPart connectionPart;

    private TableManager tableManager;

    public FilterTreeAnchor(IFigure owner, ConnectionEditPart connectionPart, TableManager tableManager) {
        super(owner);
        this.connectionPart = connectionPart;
        this.tableManager = tableManager;
    }

    @Override
    public Point getReferencePoint() {
        Point ref = null;

        if (tableManager.isMinimized()) {
            ref = tableManager.getEditPart().getFigure().getBounds().getLeft();

        } else {
            if (getOwner() == null) {
                return null;
            } else if (getOwner() instanceof AbstractTableContainer) {
                AbstractTableContainer treeFigure = (AbstractTableContainer) getOwner();
                FilterContainer filterContainer = treeFigure.getFilterContainer();
                if (filterContainer != null) {
                    ref = filterContainer.getBounds().getLeft();
                }
            }

        }
        if (ref != null) {
            IFigure treeFigure = tableManager.getEditPart().getFigure();
            int avialableX = treeFigure.getBounds().x;
            if (ref.x < avialableX) {
                ref.x = avialableX;
            }
        }

        if (connectionPart instanceof BaseConnectionEditPart && connectionPart.getFigure() instanceof PolylineConnection) {
            BaseConnectionEditPart baseConnectionPart = (BaseConnectionEditPart) connectionPart;
            PolylineConnection connFigure = (PolylineConnection) connectionPart.getFigure();

            org.eclipse.swt.graphics.Point avilableSize = tableManager.getGraphicalViewer().getControl().getSize();

            if (ref != null) {
                if (ref.y < 0) {
                    if (!(baseConnectionPart instanceof LookupConnectionPart)) {
                        ref.y = 0;
                    }
                    baseConnectionPart.setTargetContained(false);
                    if (baseConnectionPart.isDOTStyle()) {
                        connFigure.setLineStyle(SWT.LINE_DASHDOTDOT);
                    } else {
                        connFigure.setLineStyle(SWT.LINE_SOLID);
                    }
                } else if (ref.y > avilableSize.y) {
                    if (!(baseConnectionPart instanceof LookupConnectionPart)) {
                        ref.y = avilableSize.y;
                    }
                    baseConnectionPart.setTargetContained(false);
                    if (baseConnectionPart.isDOTStyle()) {
                        connFigure.setLineStyle(SWT.LINE_DASHDOTDOT);
                    } else {
                        connFigure.setLineStyle(SWT.LINE_SOLID);
                    }
                } else {
                    baseConnectionPart.setTargetContained(true);
                    if (!baseConnectionPart.isDOTStyle()) {
                        connFigure.setLineStyle(SWT.LINE_SOLID);
                    }
                }
            }
        }

        return ref;

    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.draw2d.ChopboxAnchor#getLocation(org.eclipse.draw2d.geometry.Point)
     */
    @Override
    public Point getLocation(Point reference) {
        Point referencePoint = getReferencePoint();
        if (referencePoint != null) {
            return new Point(referencePoint.x, referencePoint.y);
        } else {
            return new Point(0, 0);
        }

    }

}
