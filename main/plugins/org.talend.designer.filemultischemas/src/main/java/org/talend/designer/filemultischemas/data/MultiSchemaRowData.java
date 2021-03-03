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
package org.talend.designer.filemultischemas.data;

import java.util.ArrayList;
import java.util.List;

/**
 * cLi class global comment. Detailled comment
 *
 * for row data.
 */
public class MultiSchemaRowData {

    private String key;

    private SchemasKeyData parent;

    private List<MultiSchemasMetadataColumn> columnsData = new ArrayList<MultiSchemasMetadataColumn>();

    public MultiSchemaRowData(String key) {
        super();
        this.key = key;
    }

    public String getKeyName() {
        return this.key;
    }

    public SchemasKeyData getParent() {
        return this.parent;
    }

    public void setParent(SchemasKeyData parent) {
        this.parent = parent;
    }

    public List<MultiSchemasMetadataColumn> getColumnsData() {
        return this.columnsData;
    }

    public void addColumnData(MultiSchemasMetadataColumn column) {
        if (column != null) {
            this.columnsData.add(column);
        }
    }
}
