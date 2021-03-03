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
package org.talend.repository.ui.utils;

import org.talend.core.model.metadata.MetadataToolHelper;

/**
 * This class is used for column name validation.
 *
 * @author ftang, 2007-06-13 <br/>
 * @deprecated use MetadataToolHelper
 */
@Deprecated
public class ColumnNameValidator {

    /**
     * Validates column name to avoid special characters.
     *
     * qli modified to fix the bug 4456.
     *
     * @param columnName
     * @param index
     */
    public static String validateColumnNameFormat(String columnName, int index) {
        return MetadataToolHelper.validateColumnName(columnName, index);
    }
}
