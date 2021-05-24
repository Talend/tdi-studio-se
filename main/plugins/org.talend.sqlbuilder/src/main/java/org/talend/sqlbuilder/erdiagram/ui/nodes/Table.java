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
package org.talend.sqlbuilder.erdiagram.ui.nodes;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.emf.common.util.EList;
import org.talend.core.model.metadata.builder.connection.MetadataColumn;
import org.talend.core.model.metadata.builder.connection.MetadataTable;

/**
 * DOC qzhang class global comment. Detailled comment <br/>
 *
 * $Id: Table.java 1 2006-12-25 下午02:55:50 +0000 (ææäº, 29 ä¹æ 2006) yzhang $
 *
 */
public class Table extends Element {

    public static final String PROP_ERDIAGRAM = "erDiagram"; //$NON-NLS-1$

    public static final String PROP_SIZE = "size"; //$NON-NLS-1$

    public static final String PROP_NAME = "name"; //$NON-NLS-1$

    public static final String PROP_COLUMNS = "columns"; //$NON-NLS-1$

    public static final String PROP_LOCATION = "location"; //$NON-NLS-1$

    private static final long serialVersionUID = 1L;

    private MetadataTable metadataTable;

    private List columns;

    private ErDiagram erDiagram;

    private Point location = new Point(10, 10);

    private Dimension size = new Dimension(0, 20);

    private int maxHeight = 0;

    private int maxWidth = 0;

    public List getColumns() {
        return this.columns;
    }

    public Table() {
        columns = new ArrayList();
    }

    /**
     * Form the columns within table depends on the metadata columns in metadata table.
     *
     * yzhang Comment method "formColumns". If null, means select all column.
     *
     * @param selectedColumns
     */
    private void formColumns(List<MetadataColumn> selectedColumns) {
        if (metadataTable != null) {
            EList metadataColumns = metadataTable.getColumns();
            Column col = new Column();
            col.setElementName("*"); //$NON-NLS-1$
            col.setSelected(true);
            col.setTable(this);
            addColumn(col);
            Iterator iterator = metadataColumns.iterator();
            while (iterator.hasNext()) {
                MetadataColumn metadataColumn = (MetadataColumn) iterator.next();
                Column column = new Column();
                column.setMetadataColumn(metadataColumn);
                if (selectedColumns == null) {
                    column.setSelected(true);
                } else if (selectedColumns.contains(metadataColumn)) {
                    column.setSelected(true);
                }

                column.setTable(this);
                addColumn(column);
            }
        }

    }

    /**
     * DOC yzhang Comment method "addColumn".
     *
     * @param column
     */
    @SuppressWarnings("unchecked")//$NON-NLS-1$
    private void addColumn(Object column) {
        columns.add(column);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.sqlbuider.erdiagram.model.Element#getElementName()
     */
    @Override
    public String getElementName() {
        return metadataTable.getSourceName();
    }

    /**
     * Sets the metadataTable for this table. The selectedColumns contains the selected columns. if selectedColumns is
     * null, means select all.
     *
     * @param metadataTable
     * @param selectedColumns
     */
    public void setMetadataTable(MetadataTable metadataTable, List<MetadataColumn> selectedColumns) {
        this.metadataTable = metadataTable;
        formColumns(selectedColumns);
        size.height = 17;
        String sourceName = this.metadataTable.getSourceName();
        size.width = (sourceName == null ? 0 : sourceName.length()) * 7 + 6;
        for (Object object : columns) {
            if (object instanceof Column) {
                Dimension dimension = ((Column) object).getSize();
                size.height += dimension.height;
                if (dimension.width > size.width) {
                    size.width = dimension.width;
                }
            }
        }
        maxHeight = size.height;
        maxWidth = size.width;
        if (maxHeight > 220) {
            size.height = 220;
        }

        fireStructureChange(PROP_COLUMNS, this.columns);
    }

    public ErDiagram getErDiagram() {
        return erDiagram;
    }

    public void setErDiagram(ErDiagram erDiagram) {
        this.erDiagram = erDiagram;
        if (this.erDiagram != null && !this.erDiagram.getTables().isEmpty()) {
            Table lastTable = this.erDiagram.getTables().get(this.erDiagram.getTables().size() - 1);
            int x = lastTable.getLocation().x + lastTable.getSize().width + 30;
            int y = lastTable.getLocation().y;
            if (x > 600) {
                // int mxH = 0;
                // for (Object obj : this.erDiagram.getTables()) {
                // if (obj instanceof Table) {
                // if (mxH < ((Table) obj).getSize().height) {
                // mxH = ((Table) obj).getSize().height;
                // }
                // }
                // }
                // if (mxH < 250) {
                // y = y + mxH + 30;
                // } else {
                y = y + 250;
                // }
                x = 10;
            }
            this.location.x = x;
            this.location.y = y;
        }

        fireStructureChange(PROP_ERDIAGRAM, this.erDiagram);
    }

    public Point getLocation() {
        return location;
    }

    public void setLocation(Point location) {
        this.location = location;
        fireStructureChange(PROP_LOCATION, this.location);
    }

    public void setLocation(int x, int y) {
        this.location.setLocation(x, y);
        fireStructureChange(PROP_LOCATION, this.location);
    }

    public Dimension getSize() {
        return this.size;
    }

    public void setSize(Dimension size) {
        this.size = size;
        if (size.height > maxHeight) {
            this.size.height = maxHeight;
        }
        if (size.width < maxWidth) {
            this.size.width = maxWidth;
        }
        fireStructureChange(PROP_SIZE, this.size);
    }
}
