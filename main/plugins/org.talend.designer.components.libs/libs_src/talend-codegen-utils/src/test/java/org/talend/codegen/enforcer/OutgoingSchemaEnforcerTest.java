package org.talend.codegen.enforcer;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertThat;

import java.math.BigDecimal;
import java.util.Date;

import org.apache.avro.Schema;
import org.apache.avro.Schema.Field;
import org.apache.avro.SchemaBuilder;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.IndexedRecord;
import org.junit.BeforeClass;
import org.junit.Test;
import org.talend.codegen.DiSchemaConstants;
import org.talend.daikon.avro.AvroUtils;
import org.talend.daikon.avro.SchemaConstants;

/**
 * Unit-tests for {@link OutgoingSchemaEnforcer} class
 */
@SuppressWarnings("nls")
public class OutgoingSchemaEnforcerTest {

    /**
     * Runtime {@link Schema} instance, which is used as argument in tests
     */
    private static Schema runtimeSchema;

    /**
     * Design {@link Schema} instance, which is used as argument in tests
     */
    private static Schema talend6Schema;

    /**
     * An actual record that a component would like to be emitted, which may or may not contain enriched schema
     * information.
     */
    private static IndexedRecord record;

    private static int NUM_DAYS = 1000;

    /**
     * 1000 days after 1970-01-01, equal to 1972-09-27.
     */
    private static Date DATE_COMPARE = new Date(NUM_DAYS * 60 * 60 * 24 * 1000L);

    /**
     * Creates runtime schema, design schema and record, which is used as test arguments
     */
    @BeforeClass
    public static void setup() throws Exception {
        runtimeSchema = SchemaBuilder.builder().record("Record").fields() //
                .name("id").type().intType().noDefault() //
                .name("name").type().stringType().noDefault() //
                .name("age").type().intType().noDefault() //
                .name("valid").type().booleanType().noDefault() //
                .name("createdDate").prop(DiSchemaConstants.TALEND6_COLUMN_TALEND_TYPE, "id_Date") //
                .prop(DiSchemaConstants.TALEND6_COLUMN_PATTERN, "yyyy-MM-dd'T'HH:mm:ss'000Z'").type().nullable().longType() //
                .noDefault() //
                .name("logicalDate").type(AvroUtils._logicalDate()).noDefault() //
                .name("logicalTime").type(AvroUtils._logicalTime()).noDefault() //
                .name("logicalTimestamp").type(AvroUtils._logicalTimestamp()).noDefault() //
                .endRecord(); //

        talend6Schema = SchemaBuilder.builder().record("Record").fields() //
                .name("id").type().intType().noDefault() //
                .name("name").type().stringType().noDefault() //
                .name("age").type().intType().noDefault() //
                .name("valid").type().booleanType().noDefault() //
                .name("createdDate").prop(DiSchemaConstants.TALEND6_COLUMN_TALEND_TYPE, "id_Date") //
                .prop(DiSchemaConstants.TALEND6_COLUMN_PATTERN, "yyyy-MM-dd'T'HH:mm:ss'000Z'").type().nullable().longType()
                .noDefault() //
                .name("logicalDate").type(AvroUtils._logicalDate()).noDefault() //
                .name("logicalTime").type(AvroUtils._logicalTime()).noDefault() //
                .name("logicalTimestamp").type(AvroUtils._logicalTimestamp()).noDefault() //
                .endRecord(); //

        record = new GenericData.Record(runtimeSchema);
        record.put(0, 1);
        record.put(1, "User");
        record.put(2, 100);
        record.put(3, true);
        record.put(4, new Date(1467170137872L));
        record.put(5, NUM_DAYS);
        record.put(6, 1000);
        record.put(7, 1467170137872L);
    }

    /**
     * Checks {@link OutgoingSchemaEnforcer#getSchema()} returns design schema, which was passed to constructor
     * without any changes
     */
    @Test
    public void testGetSchema() {
        IndexMapper indexMapper = new IndexMapperByIndex(talend6Schema);
        OutgoingSchemaEnforcer enforcer = new OutgoingSchemaEnforcer(talend6Schema, indexMapper);
        Schema actualSchema = enforcer.getSchema();

        assertThat(actualSchema, equalTo(talend6Schema));
    }

    /**
     * Checks {@link OutgoingSchemaEnforcer#get(int)} returns correct values retrieved from wrapped
     * {@link IndexedRecord} in case design and runtime schema have same order of the fields
     */
    @Test
    public void testGetByIndex() {
        IndexMapper indexMapper = new IndexMapperByIndex(talend6Schema);
        OutgoingSchemaEnforcer enforcer = new OutgoingSchemaEnforcer(talend6Schema, indexMapper);
        enforcer.setWrapped(record);

        assertThat(enforcer.get(0), equalTo((Object) 1));
        assertThat(enforcer.get(1), equalTo((Object) "User"));
        assertThat(enforcer.get(2), equalTo((Object) 100));
        assertThat(enforcer.get(3), equalTo((Object) true));
        assertThat(enforcer.get(4), equalTo((Object) new Date(1467170137872L)));
        assertThat(enforcer.get(5), equalTo((Object) DATE_COMPARE));
        assertThat(enforcer.get(6), equalTo((Object) 1000));
        assertThat(enforcer.get(7), equalTo((Object) new Date(1467170137872L)));
    }

    /**
     * Checks {@link OutgoingSchemaEnforcer#get(int)} returns correct values retrieved from wrapped
     * {@link IndexedRecord} in case design and runtime schema have different order of the fields
     */
    @Test
    public void testGetByName() {
        Schema talend6Schema = SchemaBuilder.builder().record("Record").fields() //
                .name("valid").type().booleanType().noDefault() //
                .name("name").type().stringType().noDefault() //
                .name("id").type().intType().noDefault() //
                .name("createdDate").prop(DiSchemaConstants.TALEND6_COLUMN_TALEND_TYPE, "id_Date") //
                .prop(DiSchemaConstants.TALEND6_COLUMN_PATTERN, "yyyy-MM-dd'T'HH:mm:ss'000Z'").type().nullable().longType() //
                .noDefault() //
                .name("age").type().intType().noDefault() //
                .endRecord(); //

        IndexMapper indexMapper = new IndexMapperByName(talend6Schema);
        OutgoingSchemaEnforcer enforcer = new OutgoingSchemaEnforcer(talend6Schema, indexMapper);
        enforcer.setWrapped(record);

        assertThat(enforcer.get(0), equalTo((Object) true));
        assertThat(enforcer.get(1), equalTo((Object) "User"));
        assertThat(enforcer.get(2), equalTo((Object) 1));
        assertThat(enforcer.get(3), equalTo((Object) new Date(1467170137872L)));
        assertThat(enforcer.get(4), equalTo((Object) 100));
    }

    /**
     * Checks {@link OutgoingSchemaEnforcer#get(int)} throws {@link IndexOutOfBoundsException} in case of incoming
     * index less than 0 or more than (designSchemaSize - 1)
     */
    @Test(expected = IndexOutOfBoundsException.class)
    public void testGetOutOfBounds() {
        IndexMapper indexMapper = new IndexMapperByIndex(talend6Schema);
        OutgoingSchemaEnforcer enforcer = new OutgoingSchemaEnforcer(talend6Schema, indexMapper);
        enforcer.setWrapped(record);

        enforcer.get(10);
    }

    /**
     * Checks {@link OutgoingSchemaEnforcer#transformValue(Object, Field)} transforms {@link Date} value correctly
     * using Talend type property
     */
    @Test
    public void testTransformValueToDateByTalendType() {
        Date expectedDate = new Date(1L);

        IndexMapper indexMapper = new IndexMapperByIndex(talend6Schema);
        OutgoingSchemaEnforcer enforcer = new OutgoingSchemaEnforcer(talend6Schema, indexMapper);

        Field dateField = new Field("createdDate", Schema.create(Schema.Type.LONG), null, null);
        dateField.addProp(DiSchemaConstants.TALEND6_COLUMN_TALEND_TYPE, "id_Date");
        dateField.addProp(DiSchemaConstants.TALEND6_COLUMN_PATTERN, "yyyy-MM-dd'T'HH:mm:ss'000Z'");

        Object transformedValue = enforcer.transformValue(1L, dateField);

        assertThat(transformedValue, equalTo((Object) expectedDate));
    }

    /**
     * Checks {@link OutgoingSchemaEnforcer#transformValue(Object, Field)} transforms {@link Date} value correctly
     * using Java class
     */
    @Test
    public void testTransformValueToDateByJavaClass() {
        Date expectedDate = new Date(1L);

        IndexMapper indexMapper = new IndexMapperByIndex(talend6Schema);
        OutgoingSchemaEnforcer enforcer = new OutgoingSchemaEnforcer(talend6Schema, indexMapper);

        Field dateField = new Field("createdDate", Schema.create(Schema.Type.LONG), null, null);
        dateField.schema().addProp(SchemaConstants.JAVA_CLASS_FLAG, "java.util.Date");

        Object transformedValue = enforcer.transformValue(1L, dateField);

        assertThat(transformedValue, equalTo((Object) expectedDate));
    }

    /**
     * Checks {@link OutgoingSchemaEnforcer#transformValue(Object, Field)} transforms {@link BigDecimal} value
     * correctly using Java class
     */
    @Test
    public void testTransformValueToDecimal() {
        BigDecimal expectedDecimal = new BigDecimal("10.20");

        IndexMapper indexMapper = new IndexMapperByIndex(talend6Schema);
        OutgoingSchemaEnforcer enforcer = new OutgoingSchemaEnforcer(talend6Schema, indexMapper);

        Field decimalField = new Field("decimal", AvroUtils._decimal(), null, null);

        Object transformedValue = enforcer.transformValue("10.20", decimalField);

        assertThat(transformedValue, equalTo((Object) expectedDecimal));
    }

    /**
     * Checks {@link OutgoingSchemaEnforcer#transformValue(Object, Field)} transforms {@link Character} value
     * correctly using Java class
     */
    @Test
    public void testTransformValueToCharacter() {
        char expectedChar = 'A';

        IndexMapper indexMapper = new IndexMapperByIndex(talend6Schema);
        OutgoingSchemaEnforcer enforcer = new OutgoingSchemaEnforcer(talend6Schema, indexMapper);

        Field characterField = new Field("character", AvroUtils._character(), null, null);

        Object transformedValue = enforcer.transformValue("A", characterField);

        assertThat(transformedValue, equalTo((Object) expectedChar));
    }

    /**
     * Checks case when designSchema and runtimeSchema are different
     */
    @Test
    public void testSetWrapped() throws Exception {
        int[] expectedIndexMap = { 0 };

        Schema designSchema = SchemaBuilder.builder().record("Record").fields().name("col0").type(AvroUtils._date()).noDefault()
                .endRecord();

        Schema runtimeSchema = SchemaBuilder.builder().record("Record").fields().name("field0").type().longType().noDefault()
                .endRecord();

        IndexedRecord indexedRecord = new GenericData.Record(runtimeSchema);
        indexedRecord.put(0, 1467170137872L);

        OutgoingSchemaEnforcer enforcer = EnforcerCreator.createOutgoingEnforcer(designSchema, false);
        enforcer.setWrapped(indexedRecord);
        int[] actualIndexMap = enforcer.indexMap;
        assertArrayEquals(expectedIndexMap, actualIndexMap);
    }
}
