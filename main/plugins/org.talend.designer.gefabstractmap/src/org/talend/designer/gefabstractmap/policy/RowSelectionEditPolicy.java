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
package org.talend.designer.gefabstractmap.policy;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Locator;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.GraphicalEditPart;
import org.eclipse.gef.editpolicies.NonResizableEditPolicy;
import org.eclipse.gef.handles.MoveHandle;
import org.eclipse.gef.handles.MoveHandleLocator;
import org.talend.designer.gefabstractmap.figures.table.entity.TableEntityFigure;
import org.talend.designer.gefabstractmap.figures.var.VarEntityFigure;
import org.talend.designer.gefabstractmap.part.MapperTablePart;
import org.talend.designer.gefabstractmap.part.TableEntityPart;
import org.talend.designer.gefabstractmap.resource.ColorInfo;
import org.talend.designer.gefabstractmap.resource.ColorProviderMapper;
import org.talend.designer.gefabstractmap.utils.MapperUtils;

/**
 * WCHEN class global comment. Detailled comment
 */
public class RowSelectionEditPolicy extends NonResizableEditPolicy {

    @Override
    protected List createSelectionHandles() {
        List list = new ArrayList();
        list.add(new RowMoveHandler((GraphicalEditPart) getHost()));
        return list;
    }

    private IFigure getLocatorFigure(GraphicalEditPart owner) {
        IFigure figure = owner.getFigure();
        if (owner instanceof TableEntityPart) {
            TableEntityFigure treeNodeFigure = (TableEntityFigure) figure;
            // table figure to restrict the width
            MapperTablePart abstractInOutTreePart = MapperUtils.getMapperTablePart((TableEntityPart) owner);
            IFigure parentFigure = ((GraphicalEditPart) abstractInOutTreePart).getFigure();
            Rectangle treeBounds = parentFigure.getBounds();
            Rectangle rowBounds = treeNodeFigure.getElement().getBounds();
            Rectangle treeNodeBounds = figure.getBounds();
            figure = new Figure();
            if (treeBounds.x + 1 != treeNodeBounds.x || treeNodeBounds.width > treeBounds.width) {
                figure.setBounds(new Rectangle(treeBounds.x + 1, treeNodeBounds.y, treeBounds.width - 2, rowBounds.height));
                return figure;
            } else {
                figure.setBounds(owner.getFigure().getBounds().getCopy());
                figure.getBounds().height = rowBounds.height;
                return figure;
            }
        } else if (figure instanceof VarEntityFigure) {
            Rectangle copy = figure.getBounds().getCopy();
            figure = new Figure();
            figure.setBounds(copy);
            return figure;
        }

        return owner.getFigure();

    }

    class RowMoveHandler extends MoveHandle {

        public RowMoveHandler(GraphicalEditPart owner) {
            super(owner, null);
        }

        @Override
        public Locator getLocator() {
            setLocator(new MoveHandleLocator(getLocatorFigure(getOwner())));
            return super.getLocator();
        }

        /*
         * (non-Javadoc)
         *
         * @see org.eclipse.draw2d.Figure#paint(org.eclipse.draw2d.Graphics)
         */
        @Override
        public void paint(Graphics graphics) {
            // super.paint(graphics);
            graphics.setAlpha(40);
            Rectangle r = getBounds().getCopy();
            r.shrink(1, 1);
            graphics.setBackgroundColor(ColorProviderMapper.getColor(ColorInfo.COLOR_COLUMN_SELECTION));
            // graphics.setBackgroundColor(ColorConstants.blue);
            graphics.fillRectangle(r.x, r.y, r.width, r.height);
            // graphics.setForegroundColor(ColorProviderMapper.getColor(ColorInfo.COLOR_COLUMN_SELECTION));
            // graphics.drawRectangle(r.x, r.y, r.width, r.height);
        }

    }

}
