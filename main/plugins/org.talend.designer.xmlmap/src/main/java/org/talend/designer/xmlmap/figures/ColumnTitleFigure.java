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
package org.talend.designer.xmlmap.figures;

import java.util.List;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.MarginBorder;
import org.eclipse.draw2d.PositionConstants;
import org.eclipse.draw2d.ToolbarLayout;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Insets;
import org.eclipse.draw2d.geometry.Rectangle;
import org.talend.designer.gefabstractmap.part.MapperTablePart;
import org.talend.designer.xmlmap.model.emf.xmlmap.AbstractInOutTree;
import org.talend.designer.xmlmap.model.emf.xmlmap.InputXmlTree;

/**
 * WCHEN talend class global comment. Detailled comment
 */
public class ColumnTitleFigure extends Figure {

    private MapperTablePart xmlTreePart;

    public ColumnTitleFigure(MapperTablePart xmlTreePart) {
        this.xmlTreePart = xmlTreePart;
        AbstractInOutTree xmlTree = (AbstractInOutTree) xmlTreePart.getModel();
        if (xmlTree instanceof InputXmlTree) {
            InputXmlTree inputTree = (InputXmlTree) xmlTree;
            if (inputTree.isLookup()) {
                Label expression = new Label();
                expression.setText("Exp.key");
                expression.setBorder(new MarginBorder(3, 10, 3, -1));
                expression.setLabelAlignment(PositionConstants.LEFT);
                this.add(expression);
            }
        } else {
            Label expression = new Label();
            expression.setText("Expression");
            expression.setBorder(new MarginBorder(3, 10, 3, -1));
            expression.setLabelAlignment(PositionConstants.LEFT);
            this.add(expression);

        }

        Label column1 = new Label();
        column1.setText("Column");
        column1.setBorder(new MarginBorder(3, 10, 3, -1));
        column1.setLabelAlignment(PositionConstants.LEFT);
        this.add(column1);

        setLayoutManager(new ColumnTitleLayout());

        setBackgroundColor(ColorConstants.menuBackground);
        setOpaque(true);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.draw2d.Figure#getPreferredSize(int, int)
     */
    @Override
    public Dimension getPreferredSize(int wHint, int hHint) {
        return super.getPreferredSize(wHint, hHint);
    }

    class ColumnTitleLayout extends ToolbarLayout {

        /*
         * (non-Javadoc)
         *
         * @see org.eclipse.draw2d.ToolbarLayout#layout(org.eclipse.draw2d.IFigure)
         */
        @Override
        public void layout(IFigure parent) {
            final Rectangle clientArea = parent.getClientArea();
            int x = clientArea.x;
            int y = clientArea.y;
            final List children = parent.getChildren();
            int numChildren = children.size();

            int maxHeight = 0;
            for (int i = 0; i < numChildren; i++) {
                IFigure child = (IFigure) children.get(i);
                Dimension prefSize = getChildPreferredSize(child, clientArea.width, -1);
                maxHeight = Math.max(maxHeight, prefSize.height);
            }
            maxHeight = Math.max(maxHeight, clientArea.height);

            if (numChildren == 1) {
                IFigure child = (IFigure) children.get(0);
                Dimension prefSize = child.getPreferredSize(clientArea.width, -1);
                prefSize.width = Math.max(prefSize.width, clientArea.width);
                Rectangle newBounds = new Rectangle(x, y, prefSize.width, maxHeight);
                child.setBounds(newBounds);
            } else if (numChildren == 2) {
                int initExpressionWidth = (xmlTreePart.getViewer().getControl().getSize().x / 3 - 80 - 10) / 2;
                IFigure child = (IFigure) children.get(0);
                Rectangle newBounds = new Rectangle(x, y, initExpressionWidth, maxHeight);
                child.setBounds(newBounds);

                child = (IFigure) children.get(1);
                Dimension prefSize = child.getPreferredSize(clientArea.width, -1);
                prefSize.width = Math.max(prefSize.width, clientArea.width - initExpressionWidth);
                newBounds = new Rectangle(x + initExpressionWidth, y, prefSize.width, maxHeight);
                child.setBounds(newBounds);
            }

        }

        /*
         * (non-Javadoc)
         *
         * @see org.eclipse.draw2d.ToolbarLayout#calculatePreferredSize(org.eclipse.draw2d.IFigure, int, int)
         */
        @Override
        protected Dimension calculatePreferredSize(IFigure container, int wHint, int hHint) {
            Insets insets = container.getInsets();
            Dimension prefSize = calculateChildrenSize(container.getChildren(), wHint, hHint);

            return prefSize.expand(insets.getWidth(), insets.getHeight()).union(getBorderPreferredSize(container));
        }

        private Dimension calculateChildrenSize(List children, int wHint, int hHint) {
            Dimension childSize;
            IFigure child;
            int height = 0, width = 0;
            for (int i = 0; i < children.size(); i++) {
                child = (IFigure) children.get(i);
                childSize = getChildPreferredSize(child, wHint, hHint);
                height = Math.max(height, childSize.height);
                width = Math.max(width, childSize.width);
            }
            return new Dimension(width, height);
        }

    }

}
