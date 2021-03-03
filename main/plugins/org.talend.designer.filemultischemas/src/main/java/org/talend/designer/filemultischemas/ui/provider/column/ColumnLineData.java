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
package org.talend.designer.filemultischemas.ui.provider.column;

import org.talend.designer.filemultischemas.data.EPropertyName;
import org.talend.designer.filemultischemas.data.SchemasKeyData;

/**
 * cLi class global comment. Detailled comment
 *
 */
public class ColumnLineData {

    private EPropertyName property;

    private SchemasKeyData keyData;

    public ColumnLineData(EPropertyName property, SchemasKeyData rowData) {
        super();
        this.property = property;
        this.keyData = rowData;
    }

    public EPropertyName getProperty() {
        return this.property;
    }

    public SchemasKeyData getKeyData() {
        return this.keyData;
    }

}
