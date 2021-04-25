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
package org.talend.designer.gefabstractmap.figures.layout;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.draw2d.AbstractHintLayout;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.LayoutManager;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Insets;
import org.eclipse.draw2d.geometry.Rectangle;
import org.talend.designer.gefabstractmap.figures.table.AbstractTable;
import org.talend.designer.gefabstractmap.figures.table.AbstractTableContainer;
import org.talend.designer.gefabstractmap.figures.table.entity.TableEntityElement;

/**
 * DOC talend class global comment. Detailled comment
 */
public class RowLayout extends AbstractHintLayout {

    private Dimension cachedPreferredHint = new Dimension(-1, -1);

    protected Map constraints = new HashMap();

    private static final int FIXED_ROW_HEIGHT = 20;

    private final int defaultTableWidth = 370;

    private TableEntityElement rowFigure;

    public RowLayout() {
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.draw2d.XYLayout#layout(org.eclipse.draw2d.IFigure)
     */
    @Override
    public void layout(IFigure parent) {
        TableLayout tableLayout = getTableLayout(parent);
        int X = parent.getClientArea().x;
        int Y = parent.getClientArea().y;

        List children = parent.getChildren();
        for (int i = 0; i < children.size(); i++) {
            IFigure child = (IFigure) children.get(i);
            Rectangle columnBounds = tableLayout.getColumnBounds(i);
            columnBounds.height = FIXED_ROW_HEIGHT;
            columnBounds.x = X;
            columnBounds.y = Y;
            child.setBounds(columnBounds);
            X += columnBounds.width;

        }

    }

    private TableLayout getTableLayout(IFigure figure) {
        if (figure.getParent() instanceof AbstractTable) {
            return ((AbstractTable) figure.getParent()).getLayoutManager();
        } else if (figure.getParent() != null) {
            return getTableLayout(figure.getParent());
        }
        return null;
    }

    private IFigure getTableContainer(IFigure figure) {
        if (figure instanceof AbstractTableContainer) {
            return figure;
        } else if (figure.getParent() != null) {
            return getTableContainer(figure.getParent());
        }
        return null;
    }

    @Override
    protected Dimension calculatePreferredSize(IFigure container, int wHint, int hHint) {
        TableLayout tableLayout = getTableLayout(container);
        IFigure containerFigure = getTableContainer(container);
        wHint = -1;
        hHint = -1;
        Insets insets = container.getInsets();
        List children = container.getChildren();
        Dimension prefSize = calculateChildrenSize(children, wHint, hHint, containerFigure.getBounds().width,
                tableLayout.getWeight());
        prefSize.height = FIXED_ROW_HEIGHT;
        return prefSize.expand(insets.getWidth(), insets.getHeight()).union(getBorderPreferredSize(container));
    }

    private Dimension calculateChildrenSize(List children, int wHint, int hHint, int tableContainerWidth,
            Map<Integer, Double> weight) {
        Dimension childSize;
        IFigure child;
        int height = 0, width = 0;
        for (int i = 0; i < children.size(); i++) {
            child = (IFigure) children.get(i);
            childSize = getChildPreferredSize(child, wHint, hHint);

            if (weight != null && weight.containsKey(i) && tableContainerWidth > 0) {
                childSize.width = (int) (tableContainerWidth * weight.get(i));
            }
            // height += childSize.height;
            width += childSize.width;
        }
        return new Dimension(width, height);
    }

    protected Dimension getChildPreferredSize(IFigure child, int wHint, int hHint) {
        return child.getPreferredSize(wHint, hHint);
    }

    @Override
    public void remove(IFigure figure) {
        super.remove(figure);
    }

    /**
     * Sets the layout constraint of the given figure. The constraints can only be of type {@link Rectangle}.
     *
     * @see LayoutManager#setConstraint(IFigure, Object)
     * @since 2.0
     */
    @Override
    public void setConstraint(IFigure figure, Object newConstraint) {
        super.setConstraint(figure, newConstraint);
    }

    @Override
    public void invalidate() {
        super.invalidate();
    }

}
