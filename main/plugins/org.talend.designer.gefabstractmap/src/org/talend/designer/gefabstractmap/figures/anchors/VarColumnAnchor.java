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
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.talend.designer.gefabstractmap.figures.manager.TableEntityManager;
import org.talend.designer.gefabstractmap.figures.table.entity.TableTreeEntityFigure;
import org.talend.designer.gefabstractmap.figures.var.VarEntityFigure;
import org.talend.designer.gefabstractmap.part.VarTablePart;
import org.talend.designer.gefabstractmap.utils.MapperUtils;

/**
 * DOC wchen class global comment. Detailled comment
 */
public class VarColumnAnchor extends ChopboxAnchor {

    private boolean isSource;

    private TableEntityManager entityManager;

    public VarColumnAnchor(TableEntityManager entityManager, IFigure owner, boolean isSource) {
        super(owner);
        this.isSource = isSource;
        this.entityManager = entityManager;
    }

    @Override
    public IFigure getOwner() {
        return super.getOwner();
    }

    @Override
    public Point getReferencePoint() {
        Point ref = null;

        final VarTablePart varTablePart = (VarTablePart) MapperUtils.getMapperTablePart(entityManager.getEditPart());

        if (entityManager.isTableMinimized()) {
            if (isSource) {
                ref = varTablePart.getFigure().getBounds().getRight();
            } else {
                ref = varTablePart.getFigure().getBounds().getLeft();
            }
        } else if (getOwner() instanceof TableTreeEntityFigure) {
            TableTreeEntityFigure nodeFigure = (TableTreeEntityFigure) getOwner();
            // normal column
            if (nodeFigure.getTreeBranch() == null) {
                if (isSource) {
                    ref = getOwner().getBounds().getRight();
                } else {
                    if (nodeFigure.getElement() != null) {
                        ref = nodeFigure.getElement().getBounds().getLeft();
                    } else {
                        ref = getOwner().getBounds().getLeft();
                    }
                }
                getOwner().translateToAbsolute(ref);

            }
        } else if (getOwner() instanceof VarEntityFigure) {
            VarEntityFigure varNodeFigure = (VarEntityFigure) getOwner();
            if (isSource) {
                ref = getOwner().getBounds().getRight();
                ref.x = varTablePart.getFigure().getBounds().getRight().x;
            } else {
                if (varNodeFigure.getExpression() != null) {
                    ref = varNodeFigure.getExpression().getBounds().getLeft();
                } else {
                    ref = getOwner().getBounds().getLeft();
                }
            }
            getOwner().translateToAbsolute(ref);

        } else {
            ref = getOwner().getBounds().getCenter();
            getOwner().translateToAbsolute(ref);
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
        return new Point(referencePoint.x, referencePoint.y);

    }

    @Override
    protected Rectangle getBox() {
        Rectangle copy = getOwner().getBounds().getCopy();

        return super.getBox();
    }
}
