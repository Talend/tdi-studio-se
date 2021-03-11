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
package org.talend.designer.gefabstractmap.dnd;

import org.eclipse.draw2d.LayoutManager;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.EditPartViewer;
import org.eclipse.gef.dnd.AbstractTransferDropTargetListener;
import org.eclipse.gef.dnd.TemplateTransfer;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.talend.designer.gefabstractmap.figures.layout.XmlMapDataLayout;
import org.talend.designer.gefabstractmap.figures.sash.ISash;
import org.talend.designer.gefabstractmap.figures.sash.SashSeparator;
import org.talend.designer.gefabstractmap.figures.table.AbstractTable;
import org.talend.designer.gefabstractmap.figures.table.ColumnSash;
import org.talend.designer.gefabstractmap.figures.table.ITable;
import org.talend.designer.gefabstractmap.figures.table.TableColumn;

/**
 * created by wchen on 2013-1-16 Detailled comment
 *
 */
public abstract class MapperDropTargetListener extends AbstractTransferDropTargetListener {

    public MapperDropTargetListener(EditPartViewer viewer) {
        super(viewer, TemplateTransfer.getInstance());
    }

    protected void handleSashDrag(DropTargetEvent event, ISash s) {
        if (s instanceof SashSeparator) {
            SashSeparator separator = (SashSeparator) s;
            final Point dropLocation = getDropLocation();
            Rectangle leftBounds = separator.getLeftFigure().getBounds().getCopy();
            Rectangle rightBounds = separator.getRightFigure().getBounds().getCopy();
            if (dropLocation.x < (leftBounds.x + separator.getZoneMinSize() + separator.getSashWidth())
                    || dropLocation.x > (rightBounds.x + rightBounds.width - separator.getZoneMinSize() - separator
                            .getSashWidth())) {
                event.detail = DND.DROP_NONE;
                return;
            }

            final LayoutManager layoutManager = separator.getParentFigure().getLayoutManager();
            if (layoutManager instanceof XmlMapDataLayout) {
                XmlMapDataLayout layout = (XmlMapDataLayout) layoutManager;
                int X = getDropLocation().x;
                final Rectangle separatorBounds = separator.getBounds().getCopy();
                separatorBounds.x = X;
                layout.setConstraint(separator, separatorBounds);

                // left figure width
                int wL = X - leftBounds.x;

                // right figure width
                int wR = rightBounds.getTopRight().x - X - separator.getSashWidth();
                int xR = X + separator.getSashWidth();

                if (wL < separator.getZoneMinSize()) {
                    wL = separator.getZoneMinSize();
                    wR = rightBounds.getTopRight().x - leftBounds.x - separator.getZoneMinSize() - separator.getSashWidth();
                    xR = leftBounds.x + separator.getZoneMinSize() + separator.getSashWidth();
                }
                if (wR < separator.getZoneMinSize()) {
                    wL = rightBounds.getTopRight().x - separator.getZoneMinSize() - separator.getSashWidth() - leftBounds.x;
                    wR = separator.getZoneMinSize();
                    xR = rightBounds.getTopRight().x - separator.getZoneMinSize();
                }
                leftBounds.width = wL;
                layout.setConstraint(separator.getLeftFigure(), leftBounds.getCopy());
                separator.getLeftFigure().invalidateTree();

                rightBounds.x = xR;
                rightBounds.width = wR;
                layout.setConstraint(separator.getRightFigure(), rightBounds.getCopy());
                separator.getRightFigure().invalidateTree();
                separator.getParentFigure().revalidate();
            }

        } else if (s instanceof ColumnSash) {
            ColumnSash columnSash = (ColumnSash) s;
            ITable t = columnSash.getTable();
            // if (t instanceof AbstractTable) {
            // AbstractTable table = (AbstractTable) t;
            // Rectangle tableBounds = table.getBounds();
            // int X = getDropLocation().x;
            // if (X < tableBounds.x + columnSash.getSashWidth()
            // || X > tableBounds.x + tableBounds.width - columnSash.getSashWidth()) {
            // event.detail = DND.DROP_NONE;
            // return;
            // }
            //
            // double newExpressionWidth = X - tableBounds.x;
            //
            // double defaultExpressionWidth = table.getDefaultExpressionWidth();
            // double pecnetage = newExpressionWidth / defaultExpressionWidth;
            // table.setExpressWidthPecentage(pecnetage);
            // } else if (t instanceof Table) {
            AbstractTable table = (AbstractTable) t;
            Rectangle tableBounds = table.getBounds();
            int X = getDropLocation().x;
            if (X < tableBounds.x + 10 || X > tableBounds.x + tableBounds.width - 10) {
                event.detail = DND.DROP_NONE;
                return;
            }

            TableColumn leftColumn = columnSash.getLeftColumn();
            TableColumn rightColumn = columnSash.getRightColumn();
            Rectangle leftBounds = leftColumn.getBounds();
            Rectangle rightBounds = rightColumn.getBounds();

            int LW = X - leftBounds.x;
            int RW = rightBounds.x + rightBounds.width - X;

            if (LW < 10) {
                LW = 10;
            }
            if (RW < 10) {
                RW = 10;
            }
            double LP = (double) LW / (double) tableBounds.width;
            double RP = (double) RW / (double) tableBounds.width;

            table.getLayoutManager().setColumnPercentage(leftColumn.getColumnKey(), LP);
            table.getLayoutManager().setColumnPercentage(rightColumn.getColumnKey(), RP);
            table.invalidateTree();
            table.revalidate();
        }
        // }

    }

    @Override
    public void handleDragOver() {
        getCurrentEvent().detail = DND.DROP_NONE;
        getCurrentEvent().feedback = DND.FEEDBACK_NONE;
        updateTargetRequest();
        updateTargetEditPart();

        DropTargetEvent event = getCurrentEvent();

        Object object = TemplateTransfer.getInstance().getObject();
        if (object == null) {
            event.detail = DND.DROP_NONE;
            return;
        }

        // dnd the sash
        if (object instanceof ISash) {
            handleSashDrag(event, (ISash) object);
            return;
        }

        // dnd the tree node
        if (!(object instanceof TransferedObject)) {
            event.detail = DND.DROP_NONE;
            return;
        }

        showTargetFeedback();
    }

}
