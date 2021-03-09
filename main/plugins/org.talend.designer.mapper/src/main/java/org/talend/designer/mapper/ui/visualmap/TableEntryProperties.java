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
package org.talend.designer.mapper.ui.visualmap;

import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.TableItem;

/**
 * DOC amaumont class global comment. Detailled comment <br/>
 *
 * $Id$
 *
 */
public class TableEntryProperties {

    public Point position;

    private TableItem tableItem;

    public TableItem getTableItem() {
        return this.tableItem;
    }

    public void setTableItem(TableItem tableItem) {
        this.tableItem = tableItem;
    }

}
