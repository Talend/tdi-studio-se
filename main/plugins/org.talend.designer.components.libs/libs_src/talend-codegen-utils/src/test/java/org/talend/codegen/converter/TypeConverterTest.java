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
package org.talend.codegen.converter;

import static org.junit.Assert.assertEquals;

import org.apache.avro.LogicalType;
import org.apache.avro.Schema;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.talend.daikon.avro.AvroUtils;
import org.talend.daikon.avro.SchemaConstants;

/**
 * Unit-tests for {@link TypeConverter}
 */
public class TypeConverterTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    /**
     * Checks {@link TypeConverter#diToAvro(String, String)} returns String avro schema in case "id_String" di type is
     * passed
     */
    @Test
    public void testDiToAvroString() {
        Schema expectedSchema = AvroUtils._string();
        assertEquals(expectedSchema, TypeConverter.diToAvro("id_String", null));
    }

    /**
     * Checks {@link TypeConverter#diToAvro(String, String)} returns Boolean avro schema in case "id_Boolean" di type
     * is passed
     */
    @Test
    public void testDiToAvroBoolean() {
        Schema expectedSchema = AvroUtils._boolean();
        assertEquals(expectedSchema, TypeConverter.diToAvro("id_Boolean", null));
    }

    /**
     * Checks {@link TypeConverter#diToAvro(String, String)} returns Integer avro schema in case "id_Integer" di type
     * is passed
     */
    @Test
    public void testDiToAvroInteger() {
        Schema expectedSchema = AvroUtils._int();
        assertEquals(expectedSchema, TypeConverter.diToAvro("id_Integer", null));
    }

    /**
     * Checks {@link TypeConverter#diToAvro(String, String)} returns Long avro schema in case "id_Long" di type is
     * passed
     */
    @Test
    public void testDiToAvroLong() {
        Schema expectedSchema = AvroUtils._long();
        assertEquals(expectedSchema, TypeConverter.diToAvro("id_Long", null));
    }

    /**
     * Checks {@link TypeConverter#diToAvro(String, String)} returns Double avro schema in case "id_Double" di type is
     * passed
     */
    @Test
    public void testDiToAvroDouble() {
        Schema expectedSchema = AvroUtils._double();
        assertEquals(expectedSchema, TypeConverter.diToAvro("id_Double", null));
    }

    /**
     * Checks {@link TypeConverter#diToAvro(String, String)} returns Float avro schema in case "id_Float" di type is
     * passed
     */
    @Test
    public void testDiToAvroFloat() {
        Schema expectedSchema = AvroUtils._float();
        assertEquals(expectedSchema, TypeConverter.diToAvro("id_Float", null));
    }

    /**
     * Checks {@link TypeConverter#diToAvro(String, String)} returns Integer avro schema with
     * "java-class"=java.lang.Byte
     * in case "id_Byte" di type is passed
     */
    @Test
    public void testDiToAvroByte() {
        Schema expectedSchema = AvroUtils._byte();
        assertEquals(expectedSchema, TypeConverter.diToAvro("id_Byte", null));
    }

    /**
     * Checks {@link TypeConverter#diToAvro(String, String)} returns Integer avro schema with
     * "java-class"=java.lang.Short
     * in case "id_Short" di type is passed
     */
    @Test
    public void testDiToAvroShort() {
        Schema expectedSchema = AvroUtils._short();
        assertEquals(expectedSchema, TypeConverter.diToAvro("id_Short", null));
    }

    /**
     * Checks {@link TypeConverter#diToAvro(String, String)} returns String avro schema with
     * "java-class"=java.lang.Character
     * in case "id_Character" di type is passed
     */
    @Test
    public void testDiToAvroCharacter() {
        Schema expectedSchema = AvroUtils._character();
        assertEquals(expectedSchema, TypeConverter.diToAvro("id_Character", null));
    }

    /**
     * Checks {@link TypeConverter#diToAvro(String, String)} returns String avro schema with
     * "java-class"=java.math.BigDecimal
     * in case "id_BigDecimal" di type is passed
     */
    @Test
    public void testDiToAvroBigDecimal() {
        Schema expectedSchema = AvroUtils._decimal();
        assertEquals(expectedSchema, TypeConverter.diToAvro("id_BigDecimal", null));
    }

    /**
     * Checks {@link TypeConverter#diToAvro(String, String)} returns Long avro schema with "java-class"=java.util.Date
     * in case "id_Date" di type is passed
     */
    @Test
    public void testDiToAvroDate() {
        Schema expectedSchema = AvroUtils._logicalTimestamp();
        assertEquals(expectedSchema, TypeConverter.diToAvro("id_Date", null));
    }

    /**
     * Checks {@link TypeConverter#diToAvro(String, String)} throws {@link UnsupportedOperationException}
     * in case unsupported type is passed
     */
    @Test
    public void testDiToAvroNotSupporter() {
        thrown.expect(UnsupportedOperationException.class);
        TypeConverter.diToAvro("Unsupported", null);
    }

    /**
     * Checks {@link TypeConverter#diToAvro(String, String)} returns logical date avro schema with in case "date"
     * logical type is passed
     */
    @Test
    public void testDiToAvroLogicalDate() {
        Schema expectedSchema = AvroUtils._logicalDate();
        assertEquals(expectedSchema, TypeConverter.diToAvro(null, "date"));
    }

    /**
     * Checks {@link TypeConverter#diToAvro(String, String)} returns logical time-millis avro schema with in case
     * "time-millis" logical type is passed
     */
    @Test
    public void testDiToAvroLogicalTime() {
        Schema expectedSchema = AvroUtils._logicalTime();
        assertEquals(expectedSchema, TypeConverter.diToAvro(null, "time-millis"));
    }

    /**
     * Checks {@link TypeConverter#diToAvro(String, String)} returns logical timestamp-millis avro schema with in case
     * "timestamp-millis" logical type is passed
     */
    @Test
    public void testDiToAvroLogicalTimestamp() {
        Schema expectedSchema = AvroUtils._logicalTimestamp();
        assertEquals(expectedSchema, TypeConverter.diToAvro(null, "timestamp-millis"));
    }

    /**
     * Checks {@link TypeConverter#avroToDi(Schema)} converts boolean avro type to "id_Boolean" di type
     */
    @Test
    public void testAvroToDiBoolean() {
        String expectedType = "id_Boolean";
        Schema fieldSchema = AvroUtils._boolean();
        assertEquals(expectedType, TypeConverter.avroToDi(fieldSchema));
    }

    /**
     * Checks {@link TypeConverter#avroToDi(Schema)} converts int avro type with java-class flag "java.lang.Byte" to "id_Byte"
     * di type
     */
    @Test
    public void testAvroToDiByte() {
        String expectedType = "id_Byte";
        Schema fieldSchema = AvroUtils._byte();
        assertEquals(expectedType, TypeConverter.avroToDi(fieldSchema));
    }

    /**
     * Checks {@link TypeConverter#avroToDi(Schema)} converts int avro type with java-class flag "java.lang.Short" to "id_Short"
     * di type
     */
    @Test
    public void testAvroToDiShort() {
        String expectedType = "id_Short";
        Schema fieldSchema = AvroUtils._short();
        assertEquals(expectedType, TypeConverter.avroToDi(fieldSchema));
    }

    /**
     * Checks {@link TypeConverter#avroToDi(Schema)} converts int avro type to "id_Integer" di type
     */
    @Test
    public void testAvroToDiInteger() {
        String expectedType = "id_Integer";
        Schema fieldSchema = AvroUtils._int();
        assertEquals(expectedType, TypeConverter.avroToDi(fieldSchema));
    }

    /**
     * Checks {@link TypeConverter#avroToDi(Schema)} converts logical date avro type to "id_Date" di type
     */
    @Test
    public void testAvroToDiLogicalDate() {
        String expectedType = "id_Date";
        Schema fieldSchema = AvroUtils._logicalDate();
        assertEquals(expectedType, TypeConverter.avroToDi(fieldSchema));
    }

    /**
     * Checks {@link TypeConverter#avroToDi(Schema)} converts logical time-millis avro type to "id_Integer" di type
     */
    @Test
    public void testAvroToDiLogicalTimeMillis() {
        String expectedType = "id_Integer";
        Schema fieldSchema = AvroUtils._logicalTime();
        assertEquals(expectedType, TypeConverter.avroToDi(fieldSchema));
    }

    /**
     * Checks {@link TypeConverter#avroToDi(Schema)} converts logical time-micros avro type to "id_Long" di type
     */
    @Test
    public void testAvroToDiLogicalTimeMicros() {
        String expectedType = "id_Long";
        Schema fieldSchema = AvroUtils._logicalTimeMicros();
        assertEquals(expectedType, TypeConverter.avroToDi(fieldSchema));
    }

    /**
     * Checks {@link TypeConverter#avroToDi(Schema)} converts long avro type to "id_Long" di type
     */
    @Test
    public void testAvroToDiLong() {
        String expectedType = "id_Long";
        Schema fieldSchema = AvroUtils._long();
        assertEquals(expectedType, TypeConverter.avroToDi(fieldSchema));
    }

    /**
     * Checks {@link TypeConverter#avroToDi(Schema)} converts long avro type with java-class flag "java.util.Date" to "id_Date"
     * di type
     */
    @Test
    public void testAvroToDiDate() {
        String expectedType = "id_Date";
        Schema fieldSchema = AvroUtils._date();
        assertEquals(expectedType, TypeConverter.avroToDi(fieldSchema));
    }

    /**
     * Checks {@link TypeConverter#avroToDi(Schema)} converts logical timestamp-millis avro type to "id_Date" di type
     */
    @Test
    public void testAvroToDiLogicalTimestampMillis() {
        String expectedType = "id_Date";
        Schema fieldSchema = AvroUtils._logicalTimestamp();
        assertEquals(expectedType, TypeConverter.avroToDi(fieldSchema));
    }

    /**
     * Checks {@link TypeConverter#avroToDi(Schema)} converts logical timestamp-micros avro type to "id_Date" di type
     */
    @Test
    public void testAvroToDiLogicalTimestampMicros() {
        String expectedType = "id_Date";
        Schema fieldSchema = AvroUtils._logicalTimestampMicros();
        assertEquals(expectedType, TypeConverter.avroToDi(fieldSchema));
    }

    /**
     * Checks {@link TypeConverter#avroToDi(Schema)} converts float avro type to "id_Float" di type
     */
    @Test
    public void testAvroToDiFloat() {
        String expectedType = "id_Float";
        Schema fieldSchema = AvroUtils._float();
        assertEquals(expectedType, TypeConverter.avroToDi(fieldSchema));
    }

    /**
     * Checks {@link TypeConverter#avroToDi(Schema)} converts double avro type to "id_Double" di type
     */
    @Test
    public void testAvroToDiDouble() {
        String expectedType = "id_Double";
        Schema fieldSchema = AvroUtils._double();
        assertEquals(expectedType, TypeConverter.avroToDi(fieldSchema));
    }

    /**
     * Checks {@link TypeConverter#avroToDi(Schema)} converts bytes avro type to "id_byte[]" di type
     */
    @Test
    public void testAvroToDiBytes() {
        String expectedType = "id_byte[]";
        Schema fieldSchema = AvroUtils._bytes();
        assertEquals(expectedType, TypeConverter.avroToDi(fieldSchema));
    }

    /**
     * Checks {@link TypeConverter#avroToDi(Schema)} converts string avro type with java-class flag "java.math.BigDecimal" to
     * "id_BigDecimal" di type
     */
    @Test
    public void testAvroToDiBigDecimal() {
        String expectedType = "id_BigDecimal";
        Schema fieldSchema = AvroUtils._decimal();
        assertEquals(expectedType, TypeConverter.avroToDi(fieldSchema));
    }

    /**
     * Checks {@link TypeConverter#avroToDi(Schema)} converts string avro type with java-class flag "java.lang.String" to
     * "id_Character" di type
     */
    @Test
    public void testAvroToDiCharacter() {
        String expectedType = "id_Character";
        Schema fieldSchema = AvroUtils._character();
        assertEquals(expectedType, TypeConverter.avroToDi(fieldSchema));
    }

    /**
     * Checks {@link TypeConverter#avroToDi(Schema)} converts string avro type to "id_String" di type
     */
    @Test
    public void testAvroToDiString() {
        String expectedType = "id_String";
        Schema fieldSchema = AvroUtils._string();
        assertEquals(expectedType, TypeConverter.avroToDi(fieldSchema));
    }

    /**
     * Checks {@link TypeConverter#avroToDi(Schema)} converts array avro type to "id_List" di type
     */
    @Test
    public void testAvroToDiArray() {
        String expectedType = "id_List";
        Schema fieldSchema = Schema.createArray(Schema.create(Schema.Type.STRING));
        assertEquals(expectedType, TypeConverter.avroToDi(fieldSchema));
    }

    /**
     * Checks {@link TypeConverter#avroToDi(Schema)} throws {@link UnsupportedOperationException} with following message
     * "Unrecognized type",
     * when unknown logical type is passed
     */
    @Test
    public void testAvroToDiUnsupportedLogicalType() {
        thrown.expect(UnsupportedOperationException.class);
        thrown.expectMessage("Unrecognized type unsupported");
        LogicalType unsupported = new LogicalType("unsupported");
        Schema fieldSchema = unsupported.addToSchema(AvroUtils._string());
        TypeConverter.avroToDi(fieldSchema);
    }

    /**
     * Checks {@link TypeConverter#avroToDi(Schema)} throws {@link UnsupportedOperationException} with following message
     * "Unrecognized java class",
     * when unsupported java-class flag is passed
     */
    @Test
    public void testAvroToDiUnsupportedJavaClass() {
        thrown.expect(UnsupportedOperationException.class);
        thrown.expectMessage("Unrecognized java class java.lang.Unsupported");
        Schema fieldSchema = AvroUtils._string();
        fieldSchema.addProp(SchemaConstants.JAVA_CLASS_FLAG, "java.lang.Unsupported");
        TypeConverter.avroToDi(fieldSchema);
    }

    /**
     * Checks {@link TypeConverter#avroToDi(Schema)} throws {@link UnsupportedOperationException} with following message
     * "Unsupported avro type",
     * when unsupported avro type is passed
     */
    @Test
    public void testAvroToDiUnsupportedAvroType() {
        thrown.expect(UnsupportedOperationException.class);
        thrown.expectMessage("Unsupported avro type MAP");
        Schema fieldSchema = Schema.createMap(Schema.create(Schema.Type.STRING));
        TypeConverter.avroToDi(fieldSchema);
    }

}
