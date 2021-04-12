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
package org.talend.designer.gefabstractmap.figures.table;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.draw2d.AbstractHintLayout;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.ToolbarLayout;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Rectangle;

/**
 * DOC talend class global comment. Detailled comment
 */
public class Table extends Figure implements ITable {

    private List<TableColumn> columns = new ArrayList<TableColumn>();

    private List<ColumnSash> separators = new ArrayList<ColumnSash>();

    private Figure tableItemContainer;

    public Table() {
        setLayoutManager(new TableLayout());
        tableItemContainer = new Figure();
        tableItemContainer.setLayoutManager(new ToolbarLayout());
        tableItemContainer.setBackgroundColor(ColorConstants.white);
        tableItemContainer.setOpaque(true);
        this.add(tableItemContainer);

    }

    public void addColumn(TableColumn column) {
        this.add(column);
        columns.add(column);
    }

    public List<ColumnSash> getSeparators() {
        return this.separators;
    }

    public List<TableColumn> getColumns() {
        return this.columns;
    }

    public void addSeparator(ColumnSash separator) {
        this.add(separator);
        separators.add(separator);
    }

    public void setColumnPercentage(String key, Double value) {
        ((TableLayout) getLayoutManager()).setColumnPercentage(key, value);
    }

    public Figure getTableItemContainer() {
        return this.tableItemContainer;
    }

    public int getColumnWidth(String key) {
        return ((TableLayout) getLayoutManager()).getColumnWidth(key);
    }

    class TableLayout extends AbstractHintLayout {

        /*
         * the percentage =columnWidth/tableWidth , the sum of all percentages will be 1
         */
        private Map<String, Double> keyAndPercentage = new HashMap<String, Double>();

        private Map<String, Integer> keyAndLength = new HashMap<String, Integer>();

        private boolean isPercanteageInit;

        @Override
        public void layout(IFigure container) {
            Rectangle clientArea = container.getClientArea();
            int columnNum = columns.size();

            int toltalWidth = 0;

            Dimension prefSizes[] = new Dimension[columnNum];
            int titleHeight = 0;
            for (int i = 0; i < columnNum; i++) {
                TableColumn column = columns.get(i);
                prefSizes[i] = column.getPreferredSize();
                titleHeight = Math.max(titleHeight, prefSizes[i].height);
            }

            int X = clientArea.x;
            int Y = clientArea.y;
            int previousSashOccupied = 0;
            for (int i = 0; i < columns.size(); i++) {
                TableColumn currentColumn = columns.get(i);
                ColumnSash sash = null;
                if (i < columns.size() - 1) {
                    sash = separators.get(i);
                }
                Double percent = keyAndPercentage.get(currentColumn.getColumnKey());
                int width = (int) Math.round(clientArea.width * percent);
                if (sash != null) {
                    int halfSashWidth = sash.getSashWidth() / 2;
                    toltalWidth += width;
                    Rectangle newBounds = new Rectangle(X, Y, width - halfSashWidth - previousSashOccupied, titleHeight);
                    currentColumn.setBounds(newBounds);
                    keyAndLength.put(currentColumn.getColumnKey(), width);
                    X += newBounds.width;

                    // don't add sash width to total width
                    newBounds = new Rectangle(X, Y, sash.getSashWidth(), titleHeight);
                    sash.setBounds(newBounds);
                    X += newBounds.width;
                    previousSashOccupied = halfSashWidth;
                } else {
                    Rectangle newBounds = new Rectangle(X, Y, width - previousSashOccupied, titleHeight);
                    currentColumn.setBounds(newBounds);
                    keyAndLength.put(currentColumn.getColumnKey(), width);
                    toltalWidth += width;
                }

            }

            // in case some blank width
            if (toltalWidth != 0) {
                int diff = clientArea.width - toltalWidth;
                if (diff < 0) {
                    diff = 0;
                }
                int avg = diff / columnNum;
                int remainder = diff % columnNum;
                if (avg != 0) {
                    for (int i = 0; i < columnNum; i++) {
                        TableColumn tableColumn = columns.get(i);
                        Rectangle copy = tableColumn.getBounds().getCopy();
                        if (i > 0) {
                            copy.x += avg * i;
                        }
                        copy.width += avg;
                        tableColumn.setBounds(copy);
                        keyAndLength.put(tableColumn.getColumnKey(), keyAndLength.get(tableColumn.getColumnKey()) + avg);
                        if (i < separators.size()) {
                            ColumnSash separator = separators.get(i);
                            copy = separator.getBounds().getCopy();
                            copy.x += avg * (i + 1);
                            separator.setBounds(copy);
                        }
                    }

                }
                if (remainder != 0) {
                    TableColumn lastChild = columns.get(columns.size() - 1);
                    final Rectangle bounds = lastChild.getBounds();
                    bounds.width = bounds.width + remainder;
                    lastChild.setBounds(bounds);
                    keyAndLength.put(lastChild.getColumnKey(), keyAndLength.get(lastChild.getColumnKey()) + remainder);
                }
            }

            Dimension itemContainerSize = tableItemContainer.getPreferredSize();
            Rectangle newBounds = new Rectangle(clientArea.x, clientArea.y + titleHeight, clientArea.width,
                    itemContainerSize.height);
            tableItemContainer.setBounds(newBounds);

        }

        private void initColumnPercentage() {
            if (!isPercanteageInit) {
                double initPercentage = (double) 1 / (double) columns.size();
                for (int i = 0; i < columns.size(); i++) {
                    TableColumn tableColumn = columns.get(i);
                    keyAndPercentage.put(tableColumn.getColumnKey(), initPercentage);
                }
                isPercanteageInit = true;
            }

        }

        @Override
        protected Dimension calculatePreferredSize(IFigure container, int wHint, int hHint) {
            initColumnPercentage();
            // title
            Dimension dimension = new Dimension();
            for (int i = 0; i < columns.size(); i++) {
                Dimension prefSize = columns.get(i).getPreferredSize();
                dimension.width += prefSize.width;
                dimension.height = Math.max(dimension.height, prefSize.height);
            }
            Dimension itemContainerSize = tableItemContainer.getPreferredSize();
            dimension.width = Math.max(dimension.width, itemContainerSize.width);
            dimension.height += itemContainerSize.height;

            return dimension;
        }

        @Override
        protected Dimension calculateMinimumSize(IFigure container, int wHint, int hHint) {
            // title
            Dimension dimension = new Dimension();
            for (int i = 0; i < columns.size(); i++) {
                Dimension minSize = columns.get(i).getMinimumSize();
                dimension.width += minSize.width;
                dimension.height = Math.max(minSize.height, minSize.height);
            }
            Dimension itemContainerSize = tableItemContainer.getMinimumSize();
            dimension.width = Math.max(dimension.width, itemContainerSize.width);
            dimension.height += itemContainerSize.height;

            return dimension;
        }

        public int getColumnWidth(String key) {
            return keyAndLength.get(key);
        }

        public void setColumnPercentage(String key, Double value) {
            keyAndPercentage.put(key, value);
        }
    }

}
