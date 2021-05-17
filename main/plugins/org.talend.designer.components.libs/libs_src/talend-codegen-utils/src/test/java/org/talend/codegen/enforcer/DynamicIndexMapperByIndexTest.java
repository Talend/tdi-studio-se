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
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertArrayEquals;

import java.util.Arrays;
import java.util.List;

import org.apache.avro.Schema;
import org.apache.avro.SchemaBuilder;
import org.hamcrest.collection.IsIterableContainingInOrder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.talend.codegen.DiSchemaConstants;
import org.talend.daikon.avro.SchemaConstants;

/**
 * Unit-tests for {@link DynamicIndexMapperByIndex} class
 */
public class DynamicIndexMapperByIndexTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    /**
     * Checks {@link DynamicIndexMapperByIndex#DynamicIndexMapperByIndex(Schema)} throws {@link IllegalArgumentException}
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

        new DynamicIndexMapperByIndex(designSchemaWithoutIncludeAllFields);
    }

    /**
     * Checks {@link DynamicIndexMapperByIndex#DynamicIndexMapperByIndex(Schema)} throws {@link IllegalArgumentException}
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

        new DynamicIndexMapperByIndex(designSchemaWithoutDynamicColumnPosition);
    }

    /**
     * Checks {@link DynamicIndexMapperByIndex#DynamicIndexMapperByIndex(Schema)} throws {@link IllegalArgumentException}
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

        new DynamicIndexMapperByIndex(designSchemaWithoutDynamic);
    }

    /**
     * Checks {@link DynamicIndexMapperByIndex#computeIndexMap()} returns int array, which size equals n+1 and
     * with values which equal indexes of corresponding fields in runtime schema, where n - number of fields in design schema
     * and dynamic field position is at the start
     */
    @Test
    public void testComputeIndexMapStart() {
        int[] expectedIndexMap = { -1, 2, 3, 4 };

        Schema designSchema = SchemaBuilder.builder().record("Record") //
                .prop(DiSchemaConstants.TALEND6_DYNAMIC_COLUMN_POSITION, "0").prop(SchemaConstants.INCLUDE_ALL_FIELDS, "true")
                .fields() //
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

        DynamicIndexMapperByIndex indexMapper = new DynamicIndexMapperByIndex(designSchema);
        int[] actualIndexMap = indexMapper.computeIndexMap(runtimeSchema);
        assertArrayEquals(expectedIndexMap, actualIndexMap);
    }

    /**
     * Checks {@link DynamicIndexMapperByIndex#computeIndexMap()} returns int array, which size equals n+1 and
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

        DynamicIndexMapperByIndex indexMapper = new DynamicIndexMapperByIndex(designSchema);
        int[] actualIndexMap = indexMapper.computeIndexMap(runtimeSchema);
        assertArrayEquals(expectedIndexMap, actualIndexMap);
    }

    /**
     * Checks {@link DynamicIndexMapperByIndex#computeIndexMap()} returns int array, which size equals n+1 and
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

        DynamicIndexMapperByIndex indexMapper = new DynamicIndexMapperByIndex(designSchema);
        int[] actualIndexMap = indexMapper.computeIndexMap(runtimeSchema);
        assertArrayEquals(expectedIndexMap, actualIndexMap);
    }

    /**
     * This test-case shows that {@link DynamicIndexMapperByIndex#computeIndexMap()} can't be used, when fields of
     * design schema are organized in different order in runtime schema. For such case
     * {@link DynamicIndexMapperByName#computeIndexMap()} should be used
     */
    @Test
    public void testComputeIndexMapDiffOrder() {
        int[] expectedIndexMap = { 1, 0, 2, -1 };

        Schema designSchema = SchemaBuilder.builder().record("Record") //
                .prop(DiSchemaConstants.TALEND6_DYNAMIC_COLUMN_POSITION, "3").prop(SchemaConstants.INCLUDE_ALL_FIELDS, "true")
                .fields() //
                .name("col1").type().intType().noDefault() //
                .name("col0").type().stringType().noDefault() //
                .name("col2").type().intType().noDefault() //
                .endRecord(); //

        Schema runtimeSchema = SchemaBuilder.builder().record("Record").fields() //
                .name("col0").type().intType().noDefault() //
                .name("col1").type().intType().noDefault() //
                .name("col2").type().intType().noDefault() //
                .name("col3_1").type().stringType().noDefault() //
                .name("col3_2").type().intType().noDefault() //
                .endRecord(); //

        DynamicIndexMapperByIndex indexMapper = new DynamicIndexMapperByIndex(designSchema);
        int[] actualIndexMap = indexMapper.computeIndexMap(runtimeSchema);
        assertThat(actualIndexMap, not(equalTo(expectedIndexMap)));
    }

    /**
     * Checks {@link DynamicIndexMapperByIndex#DynamicIndexMapperByIndex(Schema, Schema)} throws {@link IllegalArgumentException}
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

        DynamicIndexMapperByIndex indexMapper = new DynamicIndexMapperByIndex(designSchema);
    }

    /**
     * Checks {@link DynamicIndexMapperByIndex#computeDynamicFieldsIndexes()} returns list, which contains indexes of runtime
     * dynamic fields
     * (i.e. fields, which are present in runtime schema, but are not present in design schema)
     */
    @Test
    public void testComputeDynamicFieldsIndexes() {
        List<Integer> expectedIndexes = Arrays.asList(3, 4);

        Schema designSchema = SchemaBuilder.builder().record("Record") //
                .prop(DiSchemaConstants.TALEND6_DYNAMIC_COLUMN_POSITION, "3") //
                .prop(SchemaConstants.INCLUDE_ALL_FIELDS, "true").fields() //
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

        DynamicIndexMapperByIndex indexMapper = new DynamicIndexMapperByIndex(designSchema);
        List<Integer> actualIndexes = indexMapper.computeDynamicFieldsIndexes(runtimeSchema);
        assertThat(actualIndexes, IsIterableContainingInOrder.contains(expectedIndexes.toArray()));
    }

}
