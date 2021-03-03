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
package org.talend.codegen;

import org.apache.avro.Schema;
import org.talend.daikon.avro.AvroUtils;

/**
 * Utility methods for DI dynamic field
 */
public final class DynamicFieldUtils {

    /**
     * Dynamic column position possible value, which means schema doesn't have dynamic column
     */
    public static final int NO_DYNAMIC_FIELD = -1;

    private DynamicFieldUtils() {
        // Class provides static utility methods and shouldn't be instantiated
    }

    /**
     * Computes dynamic field position in design schema.
     * Returns dynamic field position or {@link this#NO_DYNAMIC_COLUMN} if there is no dynamic field in schema
     *
     * @return dynamic field position
     */
    public static int getDynamicFieldPosition(Schema schema) {
        return AvroUtils.isIncludeAllFields(schema)
                ? Integer.valueOf(schema.getProp(DiSchemaConstants.TALEND6_DYNAMIC_COLUMN_POSITION)) : NO_DYNAMIC_FIELD;
    }
}
