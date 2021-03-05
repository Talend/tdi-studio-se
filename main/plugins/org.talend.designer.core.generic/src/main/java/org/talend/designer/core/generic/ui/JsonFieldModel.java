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
package org.talend.designer.core.generic.ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.widgets.Table;
import org.talend.commons.ui.swt.extended.table.ExtendedTableModel;

/**
 *
 * created by ycbai on 2015年1月4日 Detailled comment
 *
 */
public class JsonFieldModel extends ExtendedTableModel<Map<String, Object>> {

    public JsonFieldModel() {
        this(new ArrayList<Map<String, Object>>());
    }

    public JsonFieldModel(List<Map<String, Object>> rowList) {
        super();
        setRows(rowList);
    }

    public void setRows(List<Map<String, Object>> rowList) {
        registerDataList(rowList);
    }

    public Map<String, Object> createJsonRow() {
        return new HashMap<>();
    }

    @Override
    public void addAll(final Integer index, List<Map<String, Object>> beans, boolean fireBefore, boolean fireAfter) {
        super.addAll(index, beans, fireBefore, fireAfter);
        TableViewer tableViewer = getTableViewer();
        if (tableViewer != null) {
            Table table = tableViewer.getTable();
            table.pack();
            table.layout();
        }
    }
}
