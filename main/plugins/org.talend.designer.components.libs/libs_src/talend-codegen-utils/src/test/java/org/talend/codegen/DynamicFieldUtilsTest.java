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

import static org.junit.Assert.assertEquals;

import org.apache.avro.Schema;
import org.apache.avro.SchemaBuilder;
import org.junit.Test;
import org.talend.daikon.avro.SchemaConstants;

/**
 * Unit-tests for {@link DynamicFieldUtils}
 */
public class DynamicFieldUtilsTest {

    /**
     * Checks {@link DynamicFieldUtils#getDynamicFieldPosition()} computes dynamic field position correctly
     */
    @Test
    public void testGetDynamicFieldPosition() {
        Schema designSchema = SchemaBuilder.builder().record("Record") //
                .prop(DiSchemaConstants.TALEND6_DYNAMIC_COLUMN_POSITION, "4") //
                .prop(SchemaConstants.INCLUDE_ALL_FIELDS, "true") //
                .fields() //
                .name("id").type().intType().noDefault() //
                .endRecord();

        assertEquals(4, DynamicFieldUtils.getDynamicFieldPosition(designSchema));
    }

    /**
     * Checks {@link DynamicFieldUtils#getDynamicFieldPosition()} returns -1, when there is no dynamic field in schema
     */
    @Test
    public void testGetDynamicFieldPositionNoDynamic() {
        Schema designSchema = SchemaBuilder.builder().record("Record") //
                .fields() //
                .name("id").type().intType().noDefault() //
                .endRecord();

        assertEquals(-1, DynamicFieldUtils.getDynamicFieldPosition(designSchema));
    }
}
