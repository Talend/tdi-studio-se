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
package org.talend.codegen.enforcer;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasEntry;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Date;
import java.util.Map;

import org.apache.avro.Schema;
import org.apache.avro.SchemaBuilder;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.IndexedRecord;
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

    private static byte[] bytes;

    /**
     * Creates runtime schema and record, which are used in all tests
     */
    @BeforeClass
    public static void setup() {
        bytes = new byte[] { 0, 1, 2, 3 };

        runtimeSchema = SchemaBuilder.builder().record("Record").fields() //
                .name("Boolean").type().nullable().booleanType().noDefault() //
                .name("Byte").type(AvroUtils.wrapAsNullable(AvroUtils._byte())).noDefault() //
                .name("Short").type(AvroUtils.wrapAsNullable(AvroUtils._short())).noDefault() //
                .name("Integer").type().nullable().intType().noDefault() //
                .name("LogicalDate").type(AvroUtils.wrapAsNullable(AvroUtils._logicalDate())).noDefault() //
                .name("LogicalTimeMillis").type(AvroUtils.wrapAsNullable(AvroUtils._logicalTime())).noDefault() //
                .name("Long").type().nullable().longType().noDefault() //
                .name("Date").type(AvroUtils.wrapAsNullable(AvroUtils._date())).noDefault() //
                .name("LogicalTimestampMillis").type(AvroUtils.wrapAsNullable(AvroUtils._logicalTimestamp())).noDefault() //
                .name("Float").type().nullable().floatType().noDefault() //
                .name("Double").type().nullable().doubleType().noDefault() //
                .name("Bytes").type().nullable().bytesType().noDefault() //
                .name("Decimal").type(AvroUtils.wrapAsNullable(AvroUtils._decimal())).noDefault() //
                .name("Character").type(AvroUtils.wrapAsNullable(AvroUtils._character())).noDefault() //
                .name("String").type().nullable().stringType().noDefault() //
                .name("Array").type().nullable().array().items().stringType().noDefault() //
                .endRecord(); //

        record = new GenericData.Record(runtimeSchema);
        // boolean
        record.put(0, true);
        // byte
        record.put(1, 123);
        // short
        record.put(2, 12345);
        // integer
        record.put(3, 123456789);
        // logical date
        record.put(4, 1234);
        // logical time-millis
        record.put(5, 12345);
        // long
        record.put(6, 123456789012l);
        // deprecated DI date
        record.put(7, 123456789012l);
        // logical timestamp-millis
        record.put(8, 123456789012l);
        // float
        record.put(9, 12.34f);
        // double
        record.put(10, 1234.5678);
        // bytes
        record.put(11, bytes);
        // DI Decimal
        record.put(12, "1234.5678");
        // DI Character
        record.put(13, "s");
        // String
        record.put(14, "str");
        // Array
        record.put(15, Arrays.asList("one", "two", "three"));
    }

    /**
     * Checks {@link OutgoingDynamicSchemaEnforcer#getDynamicFieldsSchema()} returns schema, which contains only
     * dynamic fields (i.e. fields which are present in runtime schema, but are not present in design schema)
     */
    @Test
    public void testGetDynamicFieldsSchema() {

        Schema expectedDynamicSchema = SchemaBuilder.builder().record("dynamic").fields() //
                .name("Boolean").type().nullable().booleanType().noDefault() //
                .name("Byte").type(AvroUtils.wrapAsNullable(AvroUtils._byte())).noDefault() //
                .name("Short").type(AvroUtils.wrapAsNullable(AvroUtils._short())).noDefault() //
                .name("Integer").type().nullable().intType().noDefault() //
                .name("LogicalDate").type(AvroUtils.wrapAsNullable(AvroUtils._logicalDate())).noDefault() //
                .name("LogicalTimeMillis").type(AvroUtils.wrapAsNullable(AvroUtils._logicalTime())).noDefault() //
                .endRecord();

        Schema designSchema = SchemaBuilder.builder().record("Record") //
                .prop(DiSchemaConstants.TALEND6_DYNAMIC_COLUMN_POSITION, "0") //
                .prop(SchemaConstants.INCLUDE_ALL_FIELDS, "true") //
                .fields() //
                .name("Long").type().nullable().longType().noDefault() //
                .name("Date").type(AvroUtils.wrapAsNullable(AvroUtils._date())).noDefault() //
                .name("LogicalTimestampMillis").type(AvroUtils.wrapAsNullable(AvroUtils._logicalTimestamp())).noDefault() //
                .name("Float").type().nullable().floatType().noDefault() //
                .name("Double").type().nullable().doubleType().noDefault() //
                .name("Bytes").type().nullable().bytesType().noDefault() //
                .name("Decimal").type(AvroUtils.wrapAsNullable(AvroUtils._decimal())).noDefault() //
                .name("Character").type(AvroUtils.wrapAsNullable(AvroUtils._character())).noDefault() //
                .name("String").type().nullable().stringType().noDefault() //
                .name("Array").type().nullable().array().items().stringType().noDefault() //
                .endRecord(); //

        DynamicIndexMapper indexMapper = new DynamicIndexMapperByName(designSchema);
        OutgoingDynamicSchemaEnforcer enforcer = new OutgoingDynamicSchemaEnforcer(indexMapper);
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
                .name("Long").type().nullable().longType().noDefault() //
                .name("Date").type(AvroUtils.wrapAsNullable(AvroUtils._date())).noDefault() //
                .name("LogicalTimestampMillis").type(AvroUtils.wrapAsNullable(AvroUtils._logicalTimestamp())).noDefault() //
                .name("Float").type().nullable().floatType().noDefault() //
                .name("Double").type().nullable().doubleType().noDefault() //
                .name("Bytes").type().nullable().bytesType().noDefault() //
                .name("Decimal").type(AvroUtils.wrapAsNullable(AvroUtils._decimal())).noDefault() //
                .name("Character").type(AvroUtils.wrapAsNullable(AvroUtils._character())).noDefault() //
                .name("String").type().nullable().stringType().noDefault() //
                .name("Array").type().nullable().array().items().stringType().noDefault() //
                .endRecord(); //

        DynamicIndexMapper indexMapper = new DynamicIndexMapperByName(designSchema);
        OutgoingDynamicSchemaEnforcer enforcer = new OutgoingDynamicSchemaEnforcer(indexMapper);
        enforcer.setWrapped(record);

        // non-Dynamic fields
        assertEquals(123456789012l, enforcer.get(1));
        assertEquals(new Date(123456789012l), enforcer.get(2));
        assertEquals(new Date(123456789012l), enforcer.get(3));
        assertEquals(12.34f, enforcer.get(4));
        assertEquals(1234.5678, enforcer.get(5));
        assertEquals(bytes, enforcer.get(6));
        assertEquals(new BigDecimal("1234.5678"), enforcer.get(7));
        assertEquals('s', enforcer.get(8));
        assertEquals("str", enforcer.get(9));
        assertEquals(Arrays.asList("one", "two", "three"), enforcer.get(10));

        // Dynamic fields
        @SuppressWarnings("unchecked")
        Map<String, Object> dynamicValues = (Map<String, Object>) enforcer.get(0);
        assertThat(dynamicValues.size(), equalTo(6));
        assertThat(dynamicValues, hasEntry("Boolean", (Object) true));
        assertThat(dynamicValues, hasEntry("Byte", (Object) (byte) 123));
        assertThat(dynamicValues, hasEntry("Short", (Object) (short) 12345));
        assertThat(dynamicValues, hasEntry("Integer", (Object) 123456789));
        // 106617600000 = 1234 days in milliseconds
        assertThat(dynamicValues, hasEntry("LogicalDate", (Object) new Date(106617600000l)));
        assertThat(dynamicValues, hasEntry("LogicalTimeMillis", (Object) 12345));
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
                .name("Boolean").type().nullable().booleanType().noDefault() //
                .name("Byte").type(AvroUtils.wrapAsNullable(AvroUtils._byte())).noDefault() //
                .name("LogicalTimestampMillis").type(AvroUtils.wrapAsNullable(AvroUtils._logicalTimestamp())).noDefault() //
                .name("Float").type().nullable().floatType().noDefault() //
                .name("Double").type().nullable().doubleType().noDefault() //
                .name("Bytes").type().nullable().bytesType().noDefault() //
                .name("Decimal").type(AvroUtils.wrapAsNullable(AvroUtils._decimal())).noDefault() //
                .name("Character").type(AvroUtils.wrapAsNullable(AvroUtils._character())).noDefault() //
                .name("String").type().nullable().stringType().noDefault() //
                .name("Array").type().nullable().array().items().stringType().noDefault() //
                .endRecord(); //

        DynamicIndexMapper indexMapper = new DynamicIndexMapperByName(designSchema);
        OutgoingDynamicSchemaEnforcer enforcer = new OutgoingDynamicSchemaEnforcer(indexMapper);
        enforcer.setWrapped(record);

        // non-Dynamic fields
        assertEquals(true, enforcer.get(0));
        assertEquals((byte) 123, enforcer.get(1));
        assertEquals(new Date(123456789012l), enforcer.get(3));
        assertEquals(12.34f, enforcer.get(4));
        assertEquals(1234.5678, enforcer.get(5));
        assertEquals(bytes, enforcer.get(6));
        assertEquals(new BigDecimal("1234.5678"), enforcer.get(7));
        assertEquals('s', enforcer.get(8));
        assertEquals("str", enforcer.get(9));
        assertEquals(Arrays.asList("one", "two", "three"), enforcer.get(10));

        // Dynamic fields
        @SuppressWarnings("unchecked")
        Map<String, Object> dynamicValues = (Map<String, Object>) enforcer.get(2);
        assertThat(dynamicValues.size(), equalTo(6));
        assertThat(dynamicValues, hasEntry("Short", (Object) (short) 12345));
        assertThat(dynamicValues, hasEntry("Integer", (Object) 123456789));
        // 106617600000 = 1234 days in milliseconds
        assertThat(dynamicValues, hasEntry("LogicalDate", (Object) new Date(106617600000l)));
        assertThat(dynamicValues, hasEntry("LogicalTimeMillis", (Object) 12345));
        assertThat(dynamicValues, hasEntry("Long", (Object) 123456789012l));
        assertThat(dynamicValues, hasEntry("Date", (Object) new Date(123456789012l)));
    }

    /**
     * Checks {@link OutgoingDynamicSchemaEnforcer#get()} returns correct ordinary and dynamic values, when dynamic
     * field is in the end of design schema
     */
    @Test
    public void testGetDynamicAtEnd() {

        Schema designSchema = SchemaBuilder.builder().record("Record") //
                .prop(DiSchemaConstants.TALEND6_DYNAMIC_COLUMN_POSITION, "10") //
                .prop(SchemaConstants.INCLUDE_ALL_FIELDS, "true") //
                .fields() //
                .name("Boolean").type().nullable().booleanType().noDefault() //
                .name("Byte").type(AvroUtils.wrapAsNullable(AvroUtils._byte())).noDefault() //
                .name("Short").type(AvroUtils.wrapAsNullable(AvroUtils._short())).noDefault() //
                .name("Integer").type().nullable().intType().noDefault() //
                .name("LogicalDate").type(AvroUtils.wrapAsNullable(AvroUtils._logicalDate())).noDefault() //
                .name("LogicalTimeMillis").type(AvroUtils.wrapAsNullable(AvroUtils._logicalTime())).noDefault() //
                .name("Long").type().nullable().longType().noDefault() //
                .name("Date").type(AvroUtils.wrapAsNullable(AvroUtils._date())).noDefault() //
                .name("LogicalTimestampMillis").type(AvroUtils.wrapAsNullable(AvroUtils._logicalTimestamp())).noDefault() //
                .name("Float").type().nullable().floatType().noDefault() //
                .endRecord(); //

        DynamicIndexMapper indexMapper = new DynamicIndexMapperByName(designSchema);
        OutgoingDynamicSchemaEnforcer enforcer = new OutgoingDynamicSchemaEnforcer(indexMapper);
        enforcer.setWrapped(record);

        // non-Dynamic fields
        assertEquals(true, enforcer.get(0));
        assertEquals((byte) 123, enforcer.get(1));
        assertEquals((short) 12345, enforcer.get(2));
        assertEquals(123456789, enforcer.get(3));
        // 106617600000 = 1234 days in milliseconds
        assertEquals(new Date(106617600000l), enforcer.get(4));
        assertEquals(12345, enforcer.get(5));
        assertEquals(123456789012l, enforcer.get(6));
        assertEquals(new Date(123456789012l), enforcer.get(7));
        assertEquals(new Date(123456789012l), enforcer.get(8));
        assertEquals(12.34f, enforcer.get(9));

        // Dynamic fields
        @SuppressWarnings("unchecked")
        Map<String, Object> dynamicValues = (Map<String, Object>) enforcer.get(10);
        assertThat(dynamicValues.size(), equalTo(6));
        assertThat(dynamicValues, hasEntry("Double", (Object) 1234.5678));
        assertThat(dynamicValues, hasEntry("Bytes", (Object) bytes));
        assertThat(dynamicValues, hasEntry("Decimal", (Object) new BigDecimal("1234.5678")));
        assertThat(dynamicValues, hasEntry("Character", (Object) 's'));
        assertThat(dynamicValues, hasEntry("String", (Object) "str"));
        assertThat(dynamicValues, hasEntry("Array", (Object) Arrays.asList("one", "two", "three")));
    }

    /**
     * Checks {@link OutgoingDynamicSchemaEnforcer#get()} correctly handles null values
     */
    @Test
    public void testGetAllNulls() {

        Schema designSchema = SchemaBuilder.builder().record("Record") //
                .prop(DiSchemaConstants.TALEND6_DYNAMIC_COLUMN_POSITION, "10") //
                .prop(SchemaConstants.INCLUDE_ALL_FIELDS, "true") //
                .fields() //
                .name("Boolean").type().nullable().booleanType().noDefault() //
                .name("Byte").type(AvroUtils.wrapAsNullable(AvroUtils._byte())).noDefault() //
                .name("Short").type(AvroUtils.wrapAsNullable(AvroUtils._short())).noDefault() //
                .name("Integer").type().nullable().intType().noDefault() //
                .name("LogicalDate").type(AvroUtils.wrapAsNullable(AvroUtils._logicalDate())).noDefault() //
                .name("LogicalTimeMillis").type(AvroUtils.wrapAsNullable(AvroUtils._logicalTime())).noDefault() //
                .name("Long").type().nullable().longType().noDefault() //
                .name("Date").type(AvroUtils.wrapAsNullable(AvroUtils._date())).noDefault() //
                .name("LogicalTimestampMillis").type(AvroUtils.wrapAsNullable(AvroUtils._logicalTimestamp())).noDefault() //
                .name("Float").type().nullable().floatType().noDefault() //
                .endRecord(); //

        IndexedRecord nullRecord = new GenericData.Record(runtimeSchema);
        // boolean
        nullRecord.put(0, null);
        // byte
        nullRecord.put(1, null);
        // short
        nullRecord.put(2, null);
        // integer
        nullRecord.put(3, null);
        // logical date
        nullRecord.put(4, null);
        // logical time-millis
        nullRecord.put(5, null);
        // long
        nullRecord.put(6, null);
        // deprecated DI date
        nullRecord.put(7, null);
        // logical timestamp-millis
        nullRecord.put(8, null);
        // float
        nullRecord.put(9, null);
        // double
        nullRecord.put(10, null);
        // bytes
        nullRecord.put(11, null);
        // DI Decimal
        nullRecord.put(12, null);
        // DI Character
        nullRecord.put(13, null);
        // String
        nullRecord.put(14, null);
        // Array
        nullRecord.put(15, null);

        DynamicIndexMapper indexMapper = new DynamicIndexMapperByName(designSchema);
        OutgoingDynamicSchemaEnforcer enforcer = new OutgoingDynamicSchemaEnforcer(indexMapper);
        enforcer.setWrapped(nullRecord);

        // non-Dynamic fields
        assertEquals(null, enforcer.get(0));
        assertEquals(null, enforcer.get(1));
        assertEquals(null, enforcer.get(2));
        assertEquals(null, enforcer.get(3));
        assertEquals(null, enforcer.get(4));
        assertEquals(null, enforcer.get(5));
        assertEquals(null, enforcer.get(6));
        assertEquals(null, enforcer.get(7));
        assertEquals(null, enforcer.get(8));
        assertEquals(null, enforcer.get(9));

        // Dynamic fields
        @SuppressWarnings("unchecked")
        Map<String, Object> dynamicValues = (Map<String, Object>) enforcer.get(10);
        assertThat(dynamicValues.size(), equalTo(6));
        assertThat(dynamicValues, hasEntry("Double", null));
        assertThat(dynamicValues, hasEntry("Bytes", null));
        assertThat(dynamicValues, hasEntry("Decimal", null));
        assertThat(dynamicValues, hasEntry("Character", null));
        assertThat(dynamicValues, hasEntry("String", null));
        assertThat(dynamicValues, hasEntry("Array", null));
    }

    /**
     * Checks {@link OutgoingDynamicSchemaEnforcer#get()} throws {@link IndexOutOfBoundsException} if passed index > then number
     * of fields in design schema
     */
    @Test(expected = IndexOutOfBoundsException.class)
    public void testGetOutOfBounds() {

        Schema designSchema = SchemaBuilder.builder().record("Record") //
                .prop(DiSchemaConstants.TALEND6_DYNAMIC_COLUMN_POSITION, "10") //
                .prop(SchemaConstants.INCLUDE_ALL_FIELDS, "true") //
                .fields() //
                .name("Boolean").type().nullable().booleanType().noDefault() //
                .name("Byte").type(AvroUtils.wrapAsNullable(AvroUtils._byte())).noDefault() //
                .name("Short").type(AvroUtils.wrapAsNullable(AvroUtils._short())).noDefault() //
                .name("Integer").type().nullable().intType().noDefault() //
                .name("LogicalDate").type(AvroUtils.wrapAsNullable(AvroUtils._logicalDate())).noDefault() //
                .name("LogicalTimeMillis").type(AvroUtils.wrapAsNullable(AvroUtils._logicalTime())).noDefault() //
                .name("Long").type().nullable().longType().noDefault() //
                .name("Date").type(AvroUtils.wrapAsNullable(AvroUtils._date())).noDefault() //
                .name("LogicalTimestampMillis").type(AvroUtils.wrapAsNullable(AvroUtils._logicalTimestamp())).noDefault() //
                .name("Float").type().nullable().floatType().noDefault() //
                .endRecord(); //

        DynamicIndexMapper indexMapper = new DynamicIndexMapperByIndex(designSchema);
        OutgoingDynamicSchemaEnforcer enforcer = new OutgoingDynamicSchemaEnforcer(indexMapper);
        enforcer.setWrapped(record);

        enforcer.get(11);
    }

}