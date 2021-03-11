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

import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.ToolbarLayout;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Rectangle;
import org.talend.designer.gefabstractmap.figures.table.Table;
import org.talend.designer.gefabstractmap.figures.table.TableColumn;

/**
 * DOC wchen class global comment. Detailled comment
 */
public class TableItemLayout extends ToolbarLayout {

    public TableItemLayout() {
        setVertical(false);
    }

    @Override
    public void layout(IFigure parent) {
        if (parent.getParent() == null || !(parent.getParent().getParent() instanceof Table)) {
            throw new RuntimeException("Can't find table");
        }

        Table table = (Table) parent.getParent().getParent();
        Rectangle clientArea = parent.getClientArea();

        int X = clientArea.x;
        int Y = clientArea.y;

        List<TableColumn> columns = table.getColumns();
        List children = parent.getChildren();
        if (columns.size() != children.size()) {
            throw new RuntimeException("Table items size don't match column size");
        }
        int rowheight = 0;
        for (int i = 0; i < children.size(); i++) {
            Figure figure = (Figure) children.get(i);
            Dimension size = figure.getPreferredSize();
            rowheight = Math.max(rowheight, size.height);
        }

        for (int i = 0; i < columns.size(); i++) {
            TableColumn tableColumn = columns.get(i);
            Figure figure = (Figure) children.get(i);
            int columnWidth = table.getColumnWidth(tableColumn.getColumnKey());
            Rectangle newBounds = new Rectangle(X, Y, columnWidth, rowheight);
            figure.setBounds(newBounds);
            X += columnWidth;
        }

    }

}
