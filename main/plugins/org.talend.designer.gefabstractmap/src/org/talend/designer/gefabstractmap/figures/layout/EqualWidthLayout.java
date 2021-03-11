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

import java.util.List;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.ToolbarLayout;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Rectangle;

/**
 * wchen talend class global comment. Detailled comment
 */
public class EqualWidthLayout extends ToolbarLayout {

    public EqualWidthLayout() {
        setVertical(false);
    }

    @Override
    public void layout(IFigure parent) {
        List children = parent.getChildren();
        int numChildren = children.size();
        Rectangle clientArea = parent.getClientArea();
        int x = clientArea.x;
        int y = clientArea.y;

        Dimension prefSizes[] = new Dimension[numChildren];
        Dimension minSizes[] = new Dimension[numChildren];

        int wHint = -1;
        int hHint = -1;
        hHint = parent.getClientArea(Rectangle.SINGLETON).height;
        wHint = parent.getClientArea(Rectangle.SINGLETON).width;

        int devideWidth = (wHint - (numChildren - 1) * spacing) / numChildren;

        IFigure child;
        int totalHeight = 0;
        int totalMinHeight = 0;
        int prefMinSumHeight = 0;

        int maxHeightInRow = 0;

        for (int i = 0; i < numChildren; i++) {
            child = (IFigure) children.get(i);

            prefSizes[i] = getChildPreferredSize(child, wHint, hHint);
            minSizes[i] = getChildMinimumSize(child, wHint, hHint);

            maxHeightInRow = Math.max(maxHeightInRow, prefSizes[i].height);

            totalHeight += prefSizes[i].height;
            totalMinHeight += minSizes[i].height;
        }
        totalHeight += (numChildren - 1) * spacing;
        totalMinHeight += (numChildren - 1) * spacing;
        prefMinSumHeight = totalHeight - totalMinHeight;

        for (int i = 0; i < numChildren; i++) {
            int amntShrinkCurrentHeight = 0;
            int prefHeight = prefSizes[i].height;
            int minHeight = minSizes[i].height;
            int prefWidth = prefSizes[i].width;
            Rectangle newBounds = new Rectangle(x, y, prefWidth, maxHeightInRow);

            child = (IFigure) children.get(i);

            int width = Math.min(prefWidth, child.getMaximumSize().width);
            if (matchWidth)
                width = child.getMaximumSize().width;

            width = Math.min(devideWidth, Math.min(clientArea.width, width));
            newBounds.width = width;

            int adjust = clientArea.width - width;
            switch (minorAlignment) {
            case ALIGN_TOPLEFT:
                adjust = 0;
                break;
            case ALIGN_CENTER:
                adjust /= 2;
                break;
            case ALIGN_BOTTOMRIGHT:
                break;
            }
            newBounds.x += adjust;
            newBounds.height -= amntShrinkCurrentHeight;
            child.setBounds(newBounds);

            prefMinSumHeight -= (prefHeight - minHeight);

            x = x + newBounds.width + spacing;
        }
    }

    // protected Dimension calculatePreferredSize(IFigure container, int wHint, int hHint) {
    // Insets insets = container.getInsets();
    // if (isHorizontal()) {
    // wHint = -1;
    // if (hHint >= 0)
    // hHint = Math.max(0, hHint - insets.getHeight());
    // } else {
    // hHint = -1;
    // if (wHint >= 0)
    // wHint = Math.max(0, wHint - insets.getWidth());
    // }
    //
    // List children = container.getChildren();
    // Dimension prefSize = calculateChildrenSize(children, wHint, hHint, true);
    // // Do a second pass, if necessary
    // if (wHint >= 0 && prefSize.width > wHint) {
    // prefSize = calculateChildrenSize(children, prefSize.width, hHint, true);
    // } else if (hHint >= 0 && prefSize.width > hHint) {
    // prefSize = calculateChildrenSize(children, wHint, prefSize.width, true);
    // }
    //
    // if (!isHorizontal()) {
    // prefSize.height += Math.max(0, children.size() - 1) * spacing;
    // }
    //
    // return transposer.t(prefSize).expand(insets.getWidth(),
    // insets.getHeight()).union(getBorderPreferredSize(container));
    // }
    //
    // protected Dimension calculateChildrenSize(List children, int wHint, int hHint, boolean preferred) {
    // Dimension childSize;
    // IFigure child;
    // int height = 0, width = 0;
    // for (int i = 0; i < children.size(); i++) {
    // child = (IFigure) children.get(i);
    // childSize = transposer.t(preferred ? getChildPreferredSize(child, wHint, hHint) : getChildMinimumSize(child,
    // wHint,
    // hHint));
    // width = Math.max(width, childSize.width);
    //
    // if (isHorizontal()) {
    // height = Math.max(height, childSize.height);
    // } else {
    // height += childSize.height;
    // }
    // }
    // return new Dimension(width, height);
    // }
    //
    // public void setVertical(boolean flag) {
    // if (horizontal != flag)
    // return;
    // invalidate();
    // horizontal = !flag;
    // }

}
