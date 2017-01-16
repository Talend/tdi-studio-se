// ============================================================================
//
// Copyright (C) 2006-2016 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.codegen.avro.conversion;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasEntry;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import java.util.Date;
import java.util.Map;

import org.apache.avro.Schema;
import org.apache.avro.SchemaBuilder;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.IndexedRecord;
import org.junit.BeforeClass;
import org.junit.Test;
import org.talend.daikon.avro.SchemaConstants;

/**
 * Unit-tests for {@link DiOutgoingDynamicSchemaEnforcer} class
 */
public class DiOutgoingDynamicSchemaEnforcerTest {

    /**
     * Runtime {@link Schema} instance, which is used as argument in tests
     */
    private static Schema runtimeSchema;

    /**
     * {@link IndexedRecord} instance, which is used as argument in tests
     */
    private static IndexedRecord record;

    /**
     * Creates runtime schema and record, which are used in all tests
     */
    @BeforeClass
    public static void setup() {
        runtimeSchema = SchemaBuilder.builder().record("Record").fields() //
                .name("id").type().intType().noDefault() //
                .name("name").type().stringType().noDefault() //
                .name("age").type().intType().noDefault() //
                .name("valid").type().booleanType().noDefault() //
                .name("address").type().stringType().noDefault() //
                .name("comment").prop(DiSchemaConstants.TALEND6_COLUMN_LENGTH, "255").type().stringType().noDefault() //
                .name("createdDate").prop(DiSchemaConstants.TALEND6_COLUMN_TALEND_TYPE, "id_Date") //
                .prop(DiSchemaConstants.TALEND6_COLUMN_PATTERN, "yyyy-MM-dd'T'HH:mm:ss'000Z'").type().nullable().longType() //
                .noDefault() //
                .endRecord(); //

        record = new GenericData.Record(runtimeSchema);
        record.put(0, 1);
        record.put(1, "User");
        record.put(2, 100);
        record.put(3, true);
        record.put(4, "Main Street");
        record.put(5, "This is a record with six columns.");
        record.put(6, new Date(1467170137872L));
    }

    /**
     * Checks {@link DiOutgoingDynamicSchemaEnforcer#getSchema()} returns design schema, which was passed to constructor without
     * any changes
     */
    @Test
    public void testGetSchema() {

        Schema designSchema = SchemaBuilder.builder().record("Record") //
                .prop(DiSchemaConstants.TALEND6_DYNAMIC_COLUMN_POSITION, "0") //
                .prop(SchemaConstants.INCLUDE_ALL_FIELDS, "true") //
                .fields() //
                .name("address").type().intType().noDefault() //
                .name("comment").type().stringType().noDefault() //
                .name("createdDate").type().intType().noDefault() //
                .endRecord(); //

        DynamicIndexMapper indexMapper = new DynamicIndexMapperByIndex(designSchema);
        DiOutgoingDynamicSchemaEnforcer enforcer = new DiOutgoingDynamicSchemaEnforcer(designSchema, indexMapper);
        Schema actualSchema = enforcer.getSchema();
        assertEquals(designSchema, actualSchema);
    }

    /**
     * Checks {@link DiOutgoingDynamicSchemaEnforcer#getDynamicFieldsSchema()} returns schema, which contains only dynamic fields
     * (i.e. fields which are present in runtime schema, but are not present in design schema)
     */
    @Test
    public void testGetDynamicFieldsSchema() {

        Schema expectedDynamicSchema = SchemaBuilder.builder().record("dynamic").fields() //
                .name("id").type().intType().noDefault() //
                .name("name").type().stringType().noDefault() //
                .name("age").type().intType().noDefault() //
                .name("valid").type().booleanType().noDefault() //
                .endRecord();

        Schema designSchema = SchemaBuilder.builder().record("Record") //
                .prop(DiSchemaConstants.TALEND6_DYNAMIC_COLUMN_POSITION, "0") //
                .prop(SchemaConstants.INCLUDE_ALL_FIELDS, "true") //
                .fields() //
                .name("address").type().intType().noDefault() //
                .name("comment").type().stringType().noDefault() //
                .name("createdDate").type().intType().noDefault() //
                .endRecord(); //

        DynamicIndexMapper indexMapper = new DynamicIndexMapperByIndex(designSchema);
        DiOutgoingDynamicSchemaEnforcer enforcer = new DiOutgoingDynamicSchemaEnforcer(designSchema, indexMapper);
        enforcer.setWrapped(record);
        Schema actualDynamicSchema = enforcer.getDynamicFieldsSchema();
        assertEquals(expectedDynamicSchema, actualDynamicSchema);
    }

    /**
     * Checks {@link DiOutgoingDynamicSchemaEnforcer#get()} returns correct ordinary and dynamic values,
     * when dynamic field is in the start of design schema
     */
    @Test
    public void testGetDynamicAtStart() {

        Schema designSchema = SchemaBuilder.builder().record("Record") //
                .prop(DiSchemaConstants.TALEND6_DYNAMIC_COLUMN_POSITION, "0") //
                .prop(SchemaConstants.INCLUDE_ALL_FIELDS, "true") //
                .fields() //
                .name("name").type().intType().noDefault() //
                .name("valid").type().stringType().noDefault() //
                .name("createdDate").type().intType().noDefault() //
                .endRecord(); //

        DynamicIndexMapper indexMapper = new DynamicIndexMapperByName(designSchema);
        DiOutgoingDynamicSchemaEnforcer enforcer = new DiOutgoingDynamicSchemaEnforcer(designSchema, indexMapper);
        enforcer.setWrapped(record);

        assertThat(enforcer.get(1), equalTo((Object) "User"));
        assertThat(enforcer.get(2), equalTo((Object) true));
        assertThat(enforcer.get(3), equalTo((Object) new Date(1467170137872L)));

        Map<String, Object> dynamicValues = (Map<String, Object>) enforcer.get(0);
        assertThat(dynamicValues.size(), equalTo(4));
        assertThat(dynamicValues, hasEntry("id", (Object) 1));
        assertThat(dynamicValues, hasEntry("age", (Object) 100));
        assertThat(dynamicValues, hasEntry("address", (Object) "Main Street"));
        assertThat(dynamicValues, hasEntry("comment", (Object) "This is a record with six columns."));
    }

    /**
     * Checks {@link DiOutgoingDynamicSchemaEnforcer#get()} returns correct ordinary and dynamic values,
     * when dynamic field is in the middle of design schema
     */
    @Test
    public void testGetDynamicAtMiddle() {

        Schema designSchema = SchemaBuilder.builder().record("Record") //
                .prop(DiSchemaConstants.TALEND6_DYNAMIC_COLUMN_POSITION, "2") //
                .prop(SchemaConstants.INCLUDE_ALL_FIELDS, "true").fields() //
                .name("id").type().intType().noDefault() //
                .name("name").type().stringType().noDefault() //
                .name("createdDate").prop(DiSchemaConstants.TALEND6_COLUMN_TALEND_TYPE, "id_Date") //
                .prop(DiSchemaConstants.TALEND6_COLUMN_PATTERN, "yyyy-MM-dd'T'HH:mm:ss'000Z'").type().nullable().longType() //
                .noDefault() //
                .endRecord(); //

        DynamicIndexMapper indexMapper = new DynamicIndexMapperByIndex(designSchema);
        DiOutgoingDynamicSchemaEnforcer enforcer = new DiOutgoingDynamicSchemaEnforcer(designSchema, indexMapper);
        enforcer.setWrapped(record);

        assertThat(enforcer.get(0), equalTo((Object) 1));
        assertThat(enforcer.get(1), equalTo((Object) "User"));
        assertThat(enforcer.get(3), equalTo((Object) new Date(1467170137872L)));

        Map<String, Object> dynamicValues = (Map<String, Object>) enforcer.get(2);
        assertThat(dynamicValues.size(), equalTo(4));
        assertThat(dynamicValues, hasEntry("age", (Object) 100));
        assertThat(dynamicValues, hasEntry("valid", (Object) true));
        assertThat(dynamicValues, hasEntry("address", (Object) "Main Street"));
        assertThat(dynamicValues, hasEntry("comment", (Object) "This is a record with six columns."));
    }

    /**
     * Checks {@link DiOutgoingDynamicSchemaEnforcer#get()} returns correct ordinary and dynamic values,
     * when dynamic field is in the end of design schema
     */
    @Test
    public void testGetDynamicAtEnd() {

        Schema designSchema = SchemaBuilder.builder().record("Record") //
                .prop(DiSchemaConstants.TALEND6_DYNAMIC_COLUMN_POSITION, "3") //
                .prop(SchemaConstants.INCLUDE_ALL_FIELDS, "true") //
                .fields() //
                .name("id").type().intType().noDefault() //
                .name("name").type().stringType().noDefault() //
                .name("age").type().intType().noDefault() //
                .endRecord(); //

        DynamicIndexMapper indexMapper = new DynamicIndexMapperByIndex(designSchema);
        DiOutgoingDynamicSchemaEnforcer enforcer = new DiOutgoingDynamicSchemaEnforcer(designSchema, indexMapper);
        enforcer.setWrapped(record);

        assertThat(enforcer.get(0), equalTo((Object) 1));
        assertThat(enforcer.get(1), equalTo((Object) "User"));
        assertThat(enforcer.get(2), equalTo((Object) 100));

        Map<String, Object> dynamicValues = (Map<String, Object>) enforcer.get(3);
        assertThat(dynamicValues.size(), equalTo(4));
        assertThat(dynamicValues, hasEntry("valid", (Object) true));
        assertThat(dynamicValues, hasEntry("address", (Object) "Main Street"));
        assertThat(dynamicValues, hasEntry("comment", (Object) "This is a record with six columns."));
        assertThat(dynamicValues, hasEntry("createdDate", (Object) new Date(1467170137872L)));
    }

    /**
     * Checks {@link DiOutgoingDynamicSchemaEnforcer#get()} returns correct ordinary and dynamic values,
     * when dynamic field is in the end of design schema
     */
    @Test(expected = IndexOutOfBoundsException.class)
    public void testGetOutOfBounds() {

        Schema designSchema = SchemaBuilder.builder().record("Record") //
                .prop(DiSchemaConstants.TALEND6_DYNAMIC_COLUMN_POSITION, "3").prop(SchemaConstants.INCLUDE_ALL_FIELDS, "true")
                .fields() //
                .name("id").type().intType().noDefault() //
                .name("name").type().stringType().noDefault() //
                .name("age").type().intType().noDefault() //
                .endRecord(); //

        DynamicIndexMapper indexMapper = new DynamicIndexMapperByIndex(designSchema);
        DiOutgoingDynamicSchemaEnforcer enforcer = new DiOutgoingDynamicSchemaEnforcer(designSchema, indexMapper);
        enforcer.setWrapped(record);

        enforcer.get(4);
    }

}
