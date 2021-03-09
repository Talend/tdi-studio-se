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
 * DOC hcyi class global comment. Detailled comment
 */
public class SearchZoneToolBarLayout extends ToolbarLayout {

    @Override
    public void layout(IFigure parent) {
        List children = parent.getChildren();
        int numChildren = children.size();
        Rectangle clientArea = parent.getClientArea();
        int x = clientArea.x + spacing;
        int y = clientArea.y;

        Dimension prefSizes[] = new Dimension[numChildren];
        Dimension minSizes[] = new Dimension[numChildren];

        int wHint = -1;
        int hHint = -1;
        if (isHorizontal()) {
            hHint = parent.getClientArea(Rectangle.SINGLETON).height;
        } else {
            wHint = parent.getClientArea(Rectangle.SINGLETON).width;
        }

        IFigure child;
        int totalHeight = 0;
        int totalMinHeight = 0;
        int maxHeight = 0;

        for (int i = 0; i < numChildren; i++) {
            child = (IFigure) children.get(i);
            prefSizes[i] = getChildPreferredSize(child, wHint, hHint);
            minSizes[i] = getChildMinimumSize(child, wHint, hHint);

            totalHeight += prefSizes[i].height;
            totalMinHeight += minSizes[i].height;
            maxHeight = Math.max(maxHeight, prefSizes[i].height);
        }
        totalHeight += (numChildren - 1) * spacing;
        totalMinHeight += (numChildren - 1) * spacing;

        y = y + (clientArea.height - maxHeight) / 2;

        for (int i = 0; i < numChildren; i++) {
            int prefHeight = prefSizes[i].height;
            int minHeight = minSizes[i].height;
            int prefWidth = prefSizes[i].width;
            if (i == 1) {
                prefWidth = 200;
            }
            Rectangle newBounds = new Rectangle(x, y, prefWidth, prefHeight);
            child = (IFigure) children.get(i);

            child.setBounds(newBounds);
            x = x + newBounds.width + spacing;
        }
    }
}
