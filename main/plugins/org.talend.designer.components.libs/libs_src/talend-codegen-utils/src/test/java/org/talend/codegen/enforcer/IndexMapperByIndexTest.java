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
package org.talend.codegen.enforcer;

import static org.junit.Assert.assertArrayEquals;

import org.apache.avro.Schema;
import org.apache.avro.SchemaBuilder;
import org.junit.Test;

/**
 * Unit-tests for {@link IndexMapperByIndex} class
 */
public class IndexMapperByIndexTest {

    /**
     * Checks {@link IndexMapperByIndex#computeIndexMap()} returns int array, which size equals n and
     * with values: consecutive numbers from 0 to n-1, where n - number of fields in design schema
     */
    @Test
    public void testComputeIndexMap() {
        int[] expectedIndexMap = { 0, 1, 2, 3, 4 };

        Schema designSchema = SchemaBuilder.builder().record("Record").fields() //
                .name("col0").type().intType().noDefault() //
                .name("col1").type().stringType().noDefault() //
                .name("col2").type().intType().noDefault() //
                .name("col3").type().booleanType().noDefault() //
                .name("col4").type().stringType().noDefault() //
                .endRecord(); //

        IndexMapperByIndex indexMapper = new IndexMapperByIndex(designSchema);
        int[] actualIndexMap = indexMapper.computeIndexMap(null);
        assertArrayEquals(expectedIndexMap, actualIndexMap);
    }

    /**
     * Checks {@link IndexMapperByIndex#computeIndexMap()} returns empty int array, if design schema has no fields
     */
    @Test
    public void testComputeIndexMapZero() {
        int[] expectedIndexMap = {};

        Schema designSchema = SchemaBuilder.record("Record").fields().endRecord();

        IndexMapperByIndex indexMapper = new IndexMapperByIndex(designSchema);
        int[] actualIndexMap = indexMapper.computeIndexMap(null);
        assertArrayEquals(expectedIndexMap, actualIndexMap);
    }
}
