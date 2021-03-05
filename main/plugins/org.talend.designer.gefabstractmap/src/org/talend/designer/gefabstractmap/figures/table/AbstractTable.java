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
import java.util.List;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.ToolbarLayout;
import org.talend.designer.gefabstractmap.figures.layout.TableLayout;
import org.talend.designer.gefabstractmap.figures.manager.TableManager;

/**
 * wchen class global comment. Detailled comment
 */
public abstract class AbstractTable extends Figure implements ITable {

    private Figure tableItemContainer;

    // private int defaultTableWidth = 370;

    private List<TableColumn> columns = new ArrayList<TableColumn>();

    private List<ColumnSash> separators = new ArrayList<ColumnSash>();

    protected TableManager tableModelManager;

    protected TableLayout layoutManager;

    // the pecentage compare to the original defaultExpressionWidth
    // private double expressionPecentage = 1;

    public AbstractTable(TableManager tableModelManager) {
        this.tableModelManager = tableModelManager;
        createTableContainer();
        layoutManager = new TableLayout(columns, separators, tableItemContainer);
        this.setLayoutManager(layoutManager);

    }

    @Override
    public TableLayout getLayoutManager() {
        return (TableLayout) super.getLayoutManager();
    }

    protected abstract void createColumns();

    protected void createTableContainer() {
        // table column container
        tableItemContainer = new Figure();
        tableItemContainer.setLayoutManager(new ToolbarLayout());
        tableItemContainer.setBackgroundColor(ColorConstants.white);
        tableItemContainer.setOpaque(true);
        add(tableItemContainer);
    }

    public void addColumn(TableColumn column) {
        this.add(column);
        columns.add(column);
    }

    public void addSeparator(ColumnSash separator) {
        this.add(separator);
        separators.add(separator);
    }

    public Figure getTableItemContainer() {
        return this.tableItemContainer;
    }

    // public int getExpressionWidth() {
    // return ((TableTreeLayout) getLayoutManager()).getExpressionWidth();
    // }

    // public int getDefaultExpressionWidth() {
    // return defaultTableWidth / 2;
    // }
    //
    // public void setDefautTableWidth(int tableMinWidth) {
    // this.defaultTableWidth = tableMinWidth;
    // invalidateTree();
    // }

    // public void setExpressWidthPecentage(double pecentage) {
    // this.expressionPecentage = pecentage;
    // invalidateTree();
    // revalidate();
    // }

    // class TableTreeLayout extends AbstractHintLayout {
    //
    // private int expressionWidth;
    //
    // public TableTreeLayout() {
    //
    // }
    //
    // @Override
    // public void layout(IFigure container) {
    // Rectangle clientArea = container.getClientArea();
    // int x = clientArea.x;
    // int y = clientArea.y;
    //
    // Dimension itemContianerSize = tableItemContainer.getPreferredSize().getCopy();
    //
    // // layout table column title
    // int titleX = x;
    // Dimension nameSize = nameColumn.getPreferredSize();
    // int maxWidth = Math.max(nameSize.width, itemContianerSize.width);
    // if (maxWidth < defaultTableWidth) {
    // maxWidth = defaultTableWidth;
    // }
    // // need to adapt to parent figure also
    // maxWidth = Math.max(maxWidth, clientArea.width);
    //
    // int nameColumnWidth = maxWidth;
    // int titleHeight = nameSize.height;
    // if (expressionColumn != null) {
    // Dimension expSize = expressionColumn.getPreferredSize();
    // maxWidth = Math.max(nameSize.width + expressionWidth, itemContianerSize.width);
    // maxWidth = Math.max(maxWidth, clientArea.width);
    //
    // titleHeight = Math.max(expSize.height, nameSize.height);
    //
    // int hafSashWidth = columnSash.getSashWidth() / 2;
    // Rectangle newBounds = new Rectangle(titleX, y, expressionWidth - hafSashWidth, titleHeight);
    // expressionColumn.setBounds(newBounds);
    // titleX = titleX + newBounds.width;
    // Rectangle sashBounds = new Rectangle(titleX, y, columnSash.getSashWidth(), titleHeight);
    // columnSash.setBounds(sashBounds);
    //
    // titleX = titleX + sashBounds.width;
    // nameColumnWidth = nameColumnWidth - expressionWidth - hafSashWidth;
    // }
    // Rectangle newBounds = new Rectangle(titleX, y, nameColumnWidth, titleHeight);
    // nameColumn.setBounds(newBounds);
    //
    // // layout table items container
    // y = y + titleHeight;
    // newBounds = new Rectangle(x, y, maxWidth, clientArea.height - titleHeight);
    // tableItemContainer.setBounds(newBounds);
    // }
    //
    // @Override
    // protected Dimension calculatePreferredSize(IFigure container, int wHint, int hHint) {
    // Dimension title = new Dimension();
    //
    // if (expressionColumn != null) {
    // Dimension expSize = expressionColumn.getPreferredSize();
    // title.height = expSize.height;
    // title.union(getBorderPreferredSize(expressionColumn));
    // title.width = (int) (defaultTableWidth / 2 * expressionPecentage);
    // expressionWidth = title.width;
    // } else {
    // expressionWidth = 0;
    // }
    // Dimension childPrefSize = nameColumn.getPreferredSize().getCopy();
    // childPrefSize.union(getBorderPreferredSize(nameColumn));
    // title.width += childPrefSize.width;
    // title.height = Math.max(title.height, childPrefSize.height);
    // Dimension containerSize = tableItemContainer.getPreferredSize().getCopy();
    // if (containerSize.width < defaultTableWidth) {
    // containerSize.width = defaultTableWidth;
    // }
    //
    // return new Dimension(Math.max(title.width, containerSize.width), title.height + containerSize.height);
    // }
    //
    // public int getExpressionWidth() {
    // return expressionWidth;
    // }
    //
    // }

}
