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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertArrayEquals;

import java.util.Arrays;
import java.util.List;

import org.apache.avro.Schema;
import org.apache.avro.SchemaBuilder;
import org.apache.commons.lang3.ArrayUtils;
import org.hamcrest.collection.IsIterableContainingInOrder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.talend.codegen.DiSchemaConstants;
import org.talend.daikon.avro.SchemaConstants;

/**
 * Unit-tests for {@link DynamicIndexMapperByName} class
 */
public class DynamicIndexMapperByNameTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    /**
     * Checks {@link DynamicIndexMapperByName#DynamicIndexMapperByName(Schema)} throws {@link IllegalArgumentException}
     * in case when incoming design schema doesn't contain dynamic field
     * Case#1 INCLUDE_ALL_FIELDS is not present
     */
    @Test
    public void testConstructorDynamicNotPresent1() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Design schema doesn't contain dynamic field");

        Schema designSchemaWithoutIncludeAllFields = SchemaBuilder.builder().record("Record") //
                .prop(DiSchemaConstants.TALEND6_DYNAMIC_COLUMN_POSITION, "0").fields() //
                .name("col1").type().intType().noDefault() //
                .name("col2").type().stringType().noDefault() //
                .name("col3").type().intType().noDefault() //
                .endRecord(); //

        new DynamicIndexMapperByName(designSchemaWithoutIncludeAllFields);
    }

    /**
     * Checks {@link DynamicIndexMapperByName#DynamicIndexMapperByName(Schema)} throws {@link IllegalArgumentException}
     * in case when incoming design schema doesn't contain dynamic field
     * Case#2 TALEND6_DYNAMIC_COLUMN_POSITION is not present
     */
    @Test
    public void testConstructorDynamicNotPresent2() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Design schema doesn't contain dynamic field");

        Schema designSchemaWithoutDynamicColumnPosition = SchemaBuilder.builder().record("Record") //
                .prop(SchemaConstants.INCLUDE_ALL_FIELDS, "true").fields() //
                .name("col1").type().intType().noDefault() //
                .name("col2").type().stringType().noDefault() //
                .name("col3").type().intType().noDefault() //
                .endRecord(); //

        new DynamicIndexMapperByName(designSchemaWithoutDynamicColumnPosition);
    }

    /**
     * Checks {@link DynamicIndexMapperByName#DynamicIndexMapperByName(Schema)} throws {@link IllegalArgumentException}
     * in case when incoming design schema doesn't contain dynamic field
     * Case#3 both TALEND6_DYNAMIC_COLUMN_POSITION and INCLUDE_ALL_FIELDS are not present
     */
    @Test
    public void testConstructorDynamicNotPresent3() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Design schema doesn't contain dynamic field");

        Schema designSchemaWithoutDynamic = SchemaBuilder.builder().record("Record").fields() //
                .name("col1").type().intType().noDefault() //
                .name("col2").type().stringType().noDefault() //
                .name("col3").type().intType().noDefault() //
                .endRecord(); //

        new DynamicIndexMapperByName(designSchemaWithoutDynamic);
    }

    /**
     * Checks {@link DynamicIndexMapperByName#computeIndexMap()} returns int array, which size equals n+1 and
     * with values which equal indexes of corresponding fields in runtime schema, where n - number of fields in design schema
     * and dynamic field position is at the start in case of schemas contain fields in the same order
     */
    @Test
    public void testComputeIndexMapStartSameOrder() {
        int[] expectedIndexMap = { -1, 2, 3, 4 };

        Schema designSchema = SchemaBuilder.builder().record("Record") //
                .prop(DiSchemaConstants.TALEND6_DYNAMIC_COLUMN_POSITION, "0") //
                .prop(SchemaConstants.INCLUDE_ALL_FIELDS, "true").fields() //
                .name("col1").type().intType().noDefault() //
                .name("col2").type().stringType().noDefault() //
                .name("col3").type().intType().noDefault() //
                .endRecord(); //

        Schema runtimeSchema = SchemaBuilder.builder().record("Record").fields() //
                .name("col0_1").type().intType().noDefault() //
                .name("col0_2").type().intType().noDefault() //
                .name("col1").type().intType().noDefault() //
                .name("col2").type().stringType().noDefault() //
                .name("col3").type().intType().noDefault() //
                .endRecord(); //

        DynamicIndexMapperByName indexMapper = new DynamicIndexMapperByName(designSchema);
        int[] actualIndexMap = indexMapper.computeIndexMap(runtimeSchema);
        assertArrayEquals(expectedIndexMap, actualIndexMap);
    }

    /**
     * Checks {@link DynamicIndexMapperByName#computeIndexMap()} returns int array, which size equals n+1 and
     * with values which equal indexes of corresponding fields in runtime schema, where n - number of fields in design schema
     * and dynamic field position is at the start in case of schemas contain fields in the different order
     */
    @Test
    public void testComputeIndexMapStartDiffOrder() {
        int[] expectedIndexMap = { -1, 4, 2, 0 };

        Schema designSchema = SchemaBuilder.builder().record("Record") //
                .prop(DiSchemaConstants.TALEND6_DYNAMIC_COLUMN_POSITION, "0") //
                .prop(SchemaConstants.INCLUDE_ALL_FIELDS, "true").fields() //
                .name("col1").type().intType().noDefault() //
                .name("col2").type().stringType().noDefault() //
                .name("col3").type().intType().noDefault() //
                .endRecord(); //

        Schema runtimeSchema = SchemaBuilder.builder().record("Record").fields() //
                .name("col3").type().intType().noDefault() //
                .name("col0_1").type().intType().noDefault() //
                .name("col2").type().stringType().noDefault() //
                .name("col0_2").type().intType().noDefault() //
                .name("col1").type().intType().noDefault() //
                .endRecord(); //

        DynamicIndexMapperByName indexMapper = new DynamicIndexMapperByName(designSchema);
        int[] actualIndexMap = indexMapper.computeIndexMap(runtimeSchema);
        assertArrayEquals(expectedIndexMap, actualIndexMap);
    }

    /**
     * Checks {@link DynamicIndexMapperByName#computeIndexMap()} returns int array, which size equals n+1 and
     * with values which equal indexes of corresponding fields in runtime schema, where n - number of fields in design schema
     * and dynamic field position is in the middle
     */
    @Test
    public void testComputeIndexMapMiddle() {
        int[] expectedIndexMap = { 0, -1, 3, 4 };

        Schema designSchema = SchemaBuilder.builder().record("Record") //
                .prop(DiSchemaConstants.TALEND6_DYNAMIC_COLUMN_POSITION, "1").prop(SchemaConstants.INCLUDE_ALL_FIELDS, "true")
                .fields() //
                .name("col0").type().intType().noDefault() //
                .name("col2").type().stringType().noDefault() //
                .name("col3").type().intType().noDefault() //
                .endRecord(); //

        Schema runtimeSchema = SchemaBuilder.builder().record("Record").fields() //
                .name("col0").type().intType().noDefault() //
                .name("col1_1").type().intType().noDefault() //
                .name("col1_2").type().intType().noDefault() //
                .name("col2").type().stringType().noDefault() //
                .name("col3").type().intType().noDefault() //
                .endRecord(); //

        DynamicIndexMapperByName indexMapper = new DynamicIndexMapperByName(designSchema);
        int[] actualIndexMap = indexMapper.computeIndexMap(runtimeSchema);
        assertArrayEquals(expectedIndexMap, actualIndexMap);
    }

    /**
     * Checks {@link DynamicIndexMapperByName#computeIndexMap()} returns int array, which size equals n+1 and
     * with values which equal indexes of corresponding fields in runtime schema, where n - number of fields in design schema
     * and dynamic field position is in the end
     */
    @Test
    public void testComputeIndexMapEnd() {
        int[] expectedIndexMap = { 0, 1, 2, -1 };

        Schema designSchema = SchemaBuilder.builder().record("Record") //
                .prop(DiSchemaConstants.TALEND6_DYNAMIC_COLUMN_POSITION, "3").prop(SchemaConstants.INCLUDE_ALL_FIELDS, "true")
                .fields() //
                .name("col0").type().intType().noDefault() //
                .name("col1").type().stringType().noDefault() //
                .name("col2").type().intType().noDefault() //
                .endRecord(); //

        Schema runtimeSchema = SchemaBuilder.builder().record("Record").fields() //
                .name("col0").type().intType().noDefault() //
                .name("col1").type().intType().noDefault() //
                .name("col2").type().intType().noDefault() //
                .name("col3_1").type().stringType().noDefault() //
                .name("col3_2").type().intType().noDefault() //
                .endRecord(); //

        DynamicIndexMapperByName indexMapper = new DynamicIndexMapperByName(designSchema);
        int[] actualIndexMap = indexMapper.computeIndexMap(runtimeSchema);
        assertArrayEquals(expectedIndexMap, actualIndexMap);
    }

    /**
     * Checks {@link DynamicIndexMapperByName#computeIndexMap()} in case when design schema has only 1 dynamic field
     * and no more other fields. Method should return int array, which size is 1 and the only element = -1
     * Test-case related to https://jira.talendforge.org/browse/TDI-37866
     */
    @Test
    public void testComputeIndexMapOnlyDynamic() {
        int[] expectedIndexMap = { -1 };

        Schema designSchema = SchemaBuilder.builder().record("Record") //
                .prop(DiSchemaConstants.TALEND6_DYNAMIC_COLUMN_POSITION, "0") //
                .prop(SchemaConstants.INCLUDE_ALL_FIELDS, "true").fields() //
                .endRecord(); //

        Schema runtimeSchema = SchemaBuilder.builder().record("Record").fields() //
                .name("col0").type().intType().noDefault() //
                .name("col1").type().intType().noDefault() //
                .name("col2").type().stringType().noDefault() //
                .endRecord(); //

        DynamicIndexMapperByName indexMapper = new DynamicIndexMapperByName(designSchema);
        int[] actualIndexMap = indexMapper.computeIndexMap(runtimeSchema);
        assertArrayEquals(expectedIndexMap, actualIndexMap);
    }

    /**
     * Checks {@link DynamicIndexMapperByName#DynamicIndexMapperByName(Schema, Schema)} throws {@link IllegalArgumentException}
     * if design schema argument doesn't contain dynamic field properties
     */
    @Test(expected = IllegalArgumentException.class)
    @SuppressWarnings("unused")
    public void testIndexMapperConstructorThrowsException() {
        Schema designSchema = SchemaBuilder.builder().record("Record").fields() //
                .name("col0").type().intType().noDefault() //
                .name("col1").type().stringType().noDefault() //
                .name("col2").type().intType().noDefault() //
                .endRecord(); //

        DynamicIndexMapperByName indexMapper = new DynamicIndexMapperByName(designSchema);
    }

    /**
     * Checks {@link DynamicIndexMapperByName#computeDynamicFieldsIndexes()} returns list, which contains indexes of runtime
     * dynamic fields
     * (i.e. fields, which are present in runtime schema, but are not present in design schema)
     */
    @Test
    public void testComputeDynamicFieldsIndexesDiffOrder() {
        List<Integer> expectedIndexes = Arrays.asList(1, 3);

        Schema designSchema = SchemaBuilder.builder().record("Record") //
                .prop(DiSchemaConstants.TALEND6_DYNAMIC_COLUMN_POSITION, "3") //
                .prop(SchemaConstants.INCLUDE_ALL_FIELDS, "true").fields() //
                .name("col0").type().intType().noDefault() //
                .name("col1").type().stringType().noDefault() //
                .name("col2").type().intType().noDefault() //
                .endRecord(); //

        Schema runtimeSchema = SchemaBuilder.builder().record("Record").fields() //
                .name("col0").type().intType().noDefault() //
                .name("col3_1").type().intType().noDefault() //
                .name("col2").type().stringType().noDefault() //
                .name("col3_2").type().intType().noDefault() //
                .name("col1").type().intType().noDefault() //
                .endRecord(); //

        DynamicIndexMapperByName indexMapper = new DynamicIndexMapperByName(designSchema);
        List<Integer> actualIndexes = indexMapper.computeDynamicFieldsIndexes(runtimeSchema);
        assertThat(actualIndexes, IsIterableContainingInOrder.contains(expectedIndexes.toArray()));
    }

    @Test
    public void testComputeFieldsIndexesForMissingFields() {
        List<Integer> expectedIndexes = Arrays.asList(0, IndexMapper.MISSING_DESIGN_FIELD, IndexMapper.MISSING_DESIGN_FIELD,
                IndexMapper.DYNAMIC);

        Schema designSchema = SchemaBuilder.builder().record("Record") //
                .prop(DiSchemaConstants.TALEND6_DYNAMIC_COLUMN_POSITION, "3") //
                .prop(SchemaConstants.INCLUDE_ALL_FIELDS, "true").fields() //
                .name("col0").type().intType().noDefault() //
                .name("col1").type().stringType().noDefault() //
                .name("col2").type().intType().noDefault() //
                .endRecord(); //

        Schema runtimeSchema = SchemaBuilder.builder().record("Record").fields() //
                .name("col0").type().intType().noDefault() //
                .name("col3_1").type().intType().noDefault() //
                .name("col3_2").type().intType().noDefault() //
                .endRecord(); //

        DynamicIndexMapperByName indexMapper = new DynamicIndexMapperByName(designSchema);
        List<Integer> actualIndexes = Arrays.asList(ArrayUtils.toObject(indexMapper.computeIndexMap(runtimeSchema)));
        assertThat(actualIndexes, IsIterableContainingInOrder.contains(expectedIndexes.toArray()));
    }
}
