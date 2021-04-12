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

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasEntry;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import org.apache.avro.Schema;
import org.apache.avro.SchemaBuilder;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.IndexedRecord;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.talend.codegen.DiSchemaConstants;
import org.talend.daikon.avro.AvroUtils;
import org.talend.daikon.avro.SchemaConstants;

/**
 * Unit-tests for {@link OutgoingDynamicSchemaEnforcer} class
 */
public class OutgoingDynamicSchemaEnforcerTest {

    /**
     * Runtime {@link Schema} instance, which is used as argument in tests
     */
    private static Schema runtimeSchema;

    /**
     * {@link IndexedRecord} instance, which is used as argument in tests
     */
    private static IndexedRecord record;

    private static Date testDate;

    /**
     * Creates runtime schema and record, which are used in all tests
     *
     * @throws ParseException
     */
    @BeforeClass
    public static void setup() throws ParseException {
        runtimeSchema = SchemaBuilder.builder().record("Record").fields() //
                .name("logicalTime").type(AvroUtils._logicalTime()).noDefault() //
                .name("logicalDate").type(AvroUtils._logicalDate()).noDefault() //
                .name("logicalTimestamp").type(AvroUtils._logicalTimestamp()).noDefault() //
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
        record.put(0, 11111);
        record.put(1, 12345);
        record.put(2, 1489061653L);

        record.put(3, 1);
        record.put(4, "User");
        record.put(5, 100);
        record.put(6, true);
        record.put(7, "Main Street");
        record.put(8, "This is a record with nine columns.");
        record.put(9, new Date(1467170137872L));

        testDate = new SimpleDateFormat("yyyy-MM-dd").parse("2003-10-20");
    }

    /**
     * Checks {@link OutgoingDynamicSchemaEnforcer#getSchema()} returns design schema, which was passed to constructor
     * without any changes
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
        OutgoingDynamicSchemaEnforcer enforcer = new OutgoingDynamicSchemaEnforcer(designSchema, indexMapper);
        Schema actualSchema = enforcer.getSchema();
        assertEquals(designSchema, actualSchema);
    }

    /**
     * Checks {@link OutgoingDynamicSchemaEnforcer#getDynamicFieldsSchema()} returns schema, which contains only
     * dynamic fields (i.e. fields which are present in runtime schema, but are not present in design schema)
     */
    @Test
    public void testGetDynamicFieldsSchema() {

        Schema expectedDynamicSchema = SchemaBuilder.builder().record("dynamic").fields() //
                .name("logicalTime").type(AvroUtils._logicalTime()).noDefault() //
                .name("logicalDate").type(AvroUtils._logicalDate()).noDefault() //
                .name("logicalTimestamp").type(AvroUtils._logicalTimestamp()).noDefault() //
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

        DynamicIndexMapper indexMapper = new DynamicIndexMapperByName(designSchema);
        OutgoingDynamicSchemaEnforcer enforcer = new OutgoingDynamicSchemaEnforcer(designSchema, indexMapper);
        enforcer.setWrapped(record);
        Schema actualDynamicSchema = enforcer.getDynamicFieldsSchema();
        assertEquals(expectedDynamicSchema, actualDynamicSchema);
    }

    /**
     * Checks {@link OutgoingDynamicSchemaEnforcer#get()} returns correct ordinary and dynamic values, when dynamic
     * field is in the start of design schema
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
        OutgoingDynamicSchemaEnforcer enforcer = new OutgoingDynamicSchemaEnforcer(designSchema, indexMapper);
        enforcer.setWrapped(record);

        // non-Dynamic fields
        assertThat(enforcer.get(1), equalTo((Object) "User"));
        assertThat(enforcer.get(2), equalTo((Object) true));
        assertThat(enforcer.get(3), equalTo((Object) new Date(1467170137872L)));

        // Dynamic fields
        Map<String, Object> dynamicValues = (Map<String, Object>) enforcer.get(0);
        assertThat(dynamicValues.size(), equalTo(7));
        assertThat(dynamicValues, hasEntry("logicalTime", (Object) 11111));
        assertThat(dynamicValues, hasEntry("logicalDate", (Object) testDate)); // Mon Oct 20 03:00:00
        // EEST 2003 is
        // 1066608000000 ms
        assertThat(dynamicValues, hasEntry("logicalTimestamp", (Object) new Date(1489061653L)));
        assertThat(dynamicValues, hasEntry("id", (Object) 1));
        assertThat(dynamicValues, hasEntry("age", (Object) 100));
        assertThat(dynamicValues, hasEntry("address", (Object) "Main Street"));
        assertThat(dynamicValues, hasEntry("comment", (Object) "This is a record with nine columns."));

    }

    /**
     * Checks {@link OutgoingDynamicSchemaEnforcer#get()} returns correct ordinary and dynamic values, when dynamic
     * field is in the middle of design schema
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

        DynamicIndexMapper indexMapper = new DynamicIndexMapperByName(designSchema);
        OutgoingDynamicSchemaEnforcer enforcer = new OutgoingDynamicSchemaEnforcer(designSchema, indexMapper);
        enforcer.setWrapped(record);

        assertThat(enforcer.get(0), equalTo((Object) 1));
        assertThat(enforcer.get(1), equalTo((Object) "User"));
        assertThat(enforcer.get(3), equalTo((Object) new Date(1467170137872L)));

        Map<String, Object> dynamicValues = (Map<String, Object>) enforcer.get(2);
        assertThat(dynamicValues.size(), equalTo(7));
        assertThat(dynamicValues, hasEntry("logicalTime", (Object) 11111));
        assertThat(dynamicValues, hasEntry("logicalDate", (Object) testDate));
        assertThat(dynamicValues, hasEntry("logicalTimestamp", (Object) new Date(1489061653L)));
        assertThat(dynamicValues, hasEntry("age", (Object) 100));
        assertThat(dynamicValues, hasEntry("valid", (Object) true));
        assertThat(dynamicValues, hasEntry("address", (Object) "Main Street"));
        assertThat(dynamicValues, hasEntry("comment", (Object) "This is a record with nine columns."));
    }

    /**
     * Checks {@link OutgoingDynamicSchemaEnforcer#get()} returns correct ordinary and dynamic values, when dynamic
     * field is in the end of design schema
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

        DynamicIndexMapper indexMapper = new DynamicIndexMapperByName(designSchema);
        OutgoingDynamicSchemaEnforcer enforcer = new OutgoingDynamicSchemaEnforcer(designSchema, indexMapper);
        enforcer.setWrapped(record);

        assertThat(enforcer.get(0), equalTo((Object) 1));
        assertThat(enforcer.get(1), equalTo((Object) "User"));
        assertThat(enforcer.get(2), equalTo((Object) 100));

        Map<String, Object> dynamicValues = (Map<String, Object>) enforcer.get(3);
        assertThat(dynamicValues.size(), equalTo(7));
        assertThat(dynamicValues, hasEntry("logicalTime", (Object) 11111));
        assertThat(dynamicValues, hasEntry("logicalDate", (Object) testDate));
        assertThat(dynamicValues, hasEntry("logicalTimestamp", (Object) new Date(1489061653L)));
        assertThat(dynamicValues, hasEntry("valid", (Object) true));
        assertThat(dynamicValues, hasEntry("address", (Object) "Main Street"));
        assertThat(dynamicValues, hasEntry("comment", (Object) "This is a record with nine columns."));
        assertThat(dynamicValues, hasEntry("createdDate", (Object) new Date(1467170137872L)));
    }

    /**
     * Checks {@link OutgoingDynamicSchemaEnforcer#get()} returns correct ordinary and dynamic values, when dynamic
     * field is in the end of design schema
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
        OutgoingDynamicSchemaEnforcer enforcer = new OutgoingDynamicSchemaEnforcer(designSchema, indexMapper);
        enforcer.setWrapped(record);

        enforcer.get(4);
    }

    @Test
    public void testFieldsThatNotPresentInRuntime() {
        Schema designSchema = SchemaBuilder.builder().record("Record") //
                .prop(DiSchemaConstants.TALEND6_DYNAMIC_COLUMN_POSITION, "3").prop(SchemaConstants.INCLUDE_ALL_FIELDS, "true")
                .fields() //
                .name("id").type().intType().noDefault() //
                .name("name").type().stringType().noDefault() //
                .name("phoneNumber").type().intType().noDefault() //
                .endRecord(); //

        DynamicIndexMapper indexMapper = new DynamicIndexMapperByName(designSchema);
        OutgoingDynamicSchemaEnforcer enforcer = new OutgoingDynamicSchemaEnforcer(designSchema, indexMapper);
        enforcer.setWrapped(record);

        Assert.assertNull(enforcer.get(2));
    }

}