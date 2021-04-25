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

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.MarginBorder;
import org.eclipse.draw2d.PositionConstants;

/**
 * wchen class global comment. Detailled comment
 */
public class TableColumn extends Label {

    private String column_key;

    public TableColumn(String key) {
        column_key = key;
        setBorder(new MarginBorder(3, 10, 3, -1));
        setLabelAlignment(PositionConstants.LEFT);
        setBackgroundColor(ColorConstants.menuBackground);
        setOpaque(true);
    }

    public String getColumnKey() {
        return this.column_key;
    }

}
