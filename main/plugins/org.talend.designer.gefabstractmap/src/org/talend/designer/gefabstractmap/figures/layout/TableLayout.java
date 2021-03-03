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
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Rectangle;
import org.talend.designer.gefabstractmap.figures.table.AbstractTableContainer;
import org.talend.designer.gefabstractmap.figures.table.ColumnSash;
import org.talend.designer.gefabstractmap.figures.table.TableColumn;

/**
 * DOC wchen class global comment. Detailled comment
 */
public class TableLayout extends AbstractHintLayout {

    private List<TableColumn> columns;

    private List<ColumnSash> separators;

    private Figure tableItemContainer;

    Map<Integer, Double> weight = new HashMap<Integer, Double>();

    public TableLayout(List<TableColumn> columns, List<ColumnSash> separators, Figure tableItemContainer) {
        this.columns = columns;
        this.separators = separators;
        this.tableItemContainer = tableItemContainer;
    }

    private boolean ajustToDefaultWidth = true;

    /*
     * the percentage =columnWidth/tableWidth , the sum of all percentages will be 1
     */
    private Map<String, Double> keyAndPercentage = new HashMap<String, Double>();

    private Map<String, Integer> keyAndLength = new HashMap<String, Integer>();

    private boolean isPercanteageInit;

    private int previousClientWidth = 0;

    @Override
    public void layout(IFigure container) {
        Rectangle clientArea = container.getClientArea();
        IFigure tableContainer = getTableContainer(container);
        int containerWidth = tableContainer.getBounds().width;
        int clientWidth = clientArea.width;
        if (ajustToDefaultWidth) {
            clientWidth = containerWidth;
        }

        initColumnPercentage(clientWidth, containerWidth);
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
            int width = (int) Math.round(clientWidth * percent);
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
            int diff = clientWidth - toltalWidth;
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
        Rectangle newBounds = new Rectangle(clientArea.x, clientArea.y + titleHeight, clientWidth, itemContainerSize.height);
        tableItemContainer.setBounds(newBounds);

    }

    private IFigure getTableContainer(IFigure figure) {
        if (figure instanceof AbstractTableContainer) {
            return figure;
        } else if (figure.getParent() != null) {
            return getTableContainer(figure.getParent());
        }
        return null;
    }

    private void initColumnPercentage(double clientWidth, double tableContainerWidth) {
        if (!isPercanteageInit || Math.abs(clientWidth - previousClientWidth) > 5) {
            double initPercentage = (double) 1 / (double) columns.size();
            if (weight.keySet().isEmpty()) {
                for (int i = 0; i < columns.size(); i++) {
                    TableColumn tableColumn = columns.get(i);
                    keyAndPercentage.put(tableColumn.getColumnKey(), initPercentage);
                }
            } else {
                double ocupied = 0;
                for (Integer columnInex : weight.keySet()) {
                    if (columnInex >= 0 && columnInex < columns.size()) {
                        TableColumn tableColumn = columns.get(columnInex);
                        Double columnWight = weight.get(columnInex);
                        Double columnWidth = tableContainerWidth * columnWight;
                        double value = columnWidth / clientWidth;
                        keyAndPercentage.put(tableColumn.getColumnKey(), value);
                        ocupied += value;
                    }
                }
                initPercentage = (1 - ocupied) / (columns.size() - weight.size());
                for (int i = 0; i < columns.size(); i++) {
                    TableColumn tableColumn = columns.get(i);
                    if (!weight.keySet().contains(i)) {
                        keyAndPercentage.put(tableColumn.getColumnKey(), initPercentage);
                    }
                }
            }
            previousClientWidth = (int) clientWidth;
            isPercanteageInit = true;
        }

    }

    @Override
    protected Dimension calculatePreferredSize(IFigure container, int wHint, int hHint) {
        IFigure tableContainer = getTableContainer(container);
        int containerWidth = tableContainer.getBounds().width;
        // title
        Dimension dimension = new Dimension();
        for (int i = 0; i < columns.size(); i++) {
            Dimension prefSize = columns.get(i).getPreferredSize();
            Double columnWeight = weight.get(i);
            if (columnWeight != null && columnWeight != 0 && containerWidth != 0) {
                dimension.width += columnWeight * containerWidth;
            } else {
                dimension.width += prefSize.width;
            }
            dimension.height = Math.max(dimension.height, prefSize.height);
        }
        Dimension itemContainerSize = tableItemContainer.getPreferredSize();
        dimension.width = Math.max(dimension.width, itemContainerSize.width);
        dimension.height += itemContainerSize.height;
        return dimension;
    }

    @Override
    protected Dimension calculateMinimumSize(IFigure container, int wHint, int hHint) {
        IFigure tableContainer = getTableContainer(container);
        int containerWidth = tableContainer.getBounds().width;
        // title
        Dimension dimension = new Dimension();
        for (int i = 0; i < columns.size(); i++) {
            Dimension minSize = columns.get(i).getMinimumSize();
            Double columnWeight = weight.get(i);
            if (columnWeight != null && columnWeight != 0 && containerWidth != 0) {
                dimension.width += columnWeight * containerWidth;
            } else {
                dimension.width += minSize.width;
            }
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

    /**
     *
     * DOC used for columnsash to change size of column
     *
     * @param key
     * @param value
     */
    public void setColumnPercentage(String key, Double value) {
        keyAndPercentage.put(key, value);
    }

    public Rectangle getColumnBounds(int index) {
        Rectangle copy = columns.get(index).getBounds().getCopy();
        int ajustSashWidth = 0;
        if (index == 0 && separators.size() > 0) {
            ajustSashWidth = (int) (separators.get(0).getSashWidth() / (double) 2);
        } else if (index > 0 && index == separators.size()) {
            ajustSashWidth = (int) (separators.get(index - 1).getSashWidth() / (double) 2);
        } else if (index > 0 && index < separators.size()) {
            ajustSashWidth = (int) (separators.get(index - 1).getSashWidth() / (double) 2 + separators.get(index).getSashWidth()
                    / (double) 2);
        }
        copy.width += ajustSashWidth;
        return copy;
    }

    public int getColumnSize() {
        return columns.size();
    }

    /**
     *
     * DOC used to init the weight compares to table container
     *
     * @param column
     * @param weight
     */
    public void setWeight(Integer columnIndex, double weight) {
        this.weight.put(columnIndex, weight);
    }

    public Map<Integer, Double> getWeight() {
        return weight;
    }

    /**
     * Sets the ajustToTableWidth.
     *
     * @param ajustToTableWidth the ajustToTableWidth to set
     */
    public void setAjustToTableWidth(boolean ajustToTableWidth) {
        this.ajustToDefaultWidth = ajustToTableWidth;
    }
}
