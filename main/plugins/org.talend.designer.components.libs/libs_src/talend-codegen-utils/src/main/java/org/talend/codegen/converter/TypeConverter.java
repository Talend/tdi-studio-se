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

import org.apache.avro.Schema;
import org.talend.daikon.avro.AvroUtils;
import org.talend.daikon.avro.LogicalTypeUtils;
import org.talend.daikon.avro.SchemaConstants;

/**
 * Converts DI metadata type to avro schema type and vice versa
 */
public final class TypeConverter {

    /**
     * Di types
     */
    public static final String BOOLEAN = "id_Boolean";

    public static final String BYTE = "id_Byte";

    public static final String BYTE_ARRAY = "id_byte[]";

    public static final String CHARACTER = "id_Character";

    public static final String DATE = "id_Date";

    public static final String DOUBLE = "id_Double";

    public static final String FLOAT = "id_Float";

    public static final String BIGDECIMAL = "id_BigDecimal";

    public static final String INTEGER = "id_Integer";

    public static final String LONG = "id_Long";

    public static final String SHORT = "id_Short";

    public static final String STRING = "id_String";

    public static final String LIST = "id_List";

    public static final String OBJECT = "id_Object";

    private TypeConverter() {
        // Class provides static utility methods and shouldn't be instantiated
    }

    /**
     * Converts DI type to Avro field schema
     *
     * @param diType data integration native type
     * @param logicalType avro logical type
     * @return field schema
     * @throws {@link UnsupportedOperationException} in case of unsupported di type or logical type
     */
    public static Schema diToAvro(String diType, String logicalType) {
        Schema fieldSchema = LogicalTypeUtils.getSchemaByLogicalType(logicalType);
        if (fieldSchema != null) {
            return fieldSchema;
        }

        switch (diType) {
        case STRING:
            return Schema.create(Schema.Type.STRING);
        case BOOLEAN:
            return Schema.create(Schema.Type.BOOLEAN);
        case INTEGER:
            return Schema.create(Schema.Type.INT);
        case LONG:
            return Schema.create(Schema.Type.LONG);
        case DOUBLE:
            return Schema.create(Schema.Type.DOUBLE);
        case FLOAT:
            return Schema.create(Schema.Type.FLOAT);
        case BYTE:
            return AvroUtils._byte();
        case SHORT:
            return AvroUtils._short();
        case CHARACTER:
            return AvroUtils._character();
        case BIGDECIMAL:
            return AvroUtils._decimal();
        case DATE:
            return AvroUtils._logicalTimestamp();
        default:
            throw new UnsupportedOperationException("Unrecognized type " + diType);
        }
    }

    /**
     * Converts Avro field type to DI type (aka talendType)
     * Conversion strategy is following:
     * 1. check Avro logical type
     * 2. check java-class property
     * 3. if above things are null, converty according schema type
     * Avro type doesn't uniquely identify DI type. Several DI types may correspond to the same Avro type.
     * Thus, logical type and java-class are checked first as they uniquely identify DI type
     * This is used in DI codegen to simplify codegen code and have tests
     *
     * @param fieldSchema Avro field schema
     * @return corresponding DI type
     */
    public static String avroToDi(Schema fieldSchema) {
        Schema typeSchema = AvroUtils.unwrapIfNullable(fieldSchema);
        String logicalType = LogicalTypeUtils.getLogicalTypeName(typeSchema);

        if (logicalType != null) {
            return getDiByLogicalType(logicalType);
        }

        String javaClass = typeSchema.getProp(SchemaConstants.JAVA_CLASS_FLAG);
        if (javaClass != null) {
            return getDiByJavaClass(javaClass);
        }

        return getDiByAvroType(typeSchema.getType());
    }

    /**
     * Returns DI metadata type which corresponds to Avro logical type
     *
     * @param logicalType Avro logical type
     * @return DI type
     */
    private static String getDiByLogicalType(String logicalType) {
        switch (logicalType) {
        case LogicalTypeUtils.DATE:
            return DATE;
        case LogicalTypeUtils.TIME_MICROS:
            return LONG;
        case LogicalTypeUtils.TIME_MILLIS:
            return INTEGER;
        case LogicalTypeUtils.TIMESTAMP_MICROS:
            return DATE;
        case LogicalTypeUtils.TIMESTAMP_MILLIS:
            return DATE;
        default:
            throw new UnsupportedOperationException("Unrecognized type " + logicalType);
        }
    }

    /**
     * Returns DI metadata type which corresponds to java-class property flag
     *
     * @param javaClass java-class property value
     * @return DI type
     */
    private static String getDiByJavaClass(String javaClass) {
        switch (javaClass) {
        case "java.math.BigDecimal":
            return BIGDECIMAL;
        case "java.lang.Byte":
            return BYTE;
        case "java.lang.Character":
            return CHARACTER;
        case "java.lang.Short":
            return SHORT;
        case "java.util.Date":
            return DATE;
        case "java.lang.Object":
            return OBJECT;
        default:
            throw new UnsupportedOperationException("Unrecognized java class " + javaClass);
        }
    }

    /**
     * Returns DI metadata type which corresponds to avro type
     *
     * @param type avro schema type
     * @return DI type
     */
    private static String getDiByAvroType(Schema.Type type) {
        switch (type) {
        case ARRAY:
            return LIST;
        case BYTES:
            return BYTE_ARRAY;
        case INT:
            return INTEGER;
        case LONG:
            return LONG;
        case FLOAT:
            return FLOAT;
        case DOUBLE:
            return DOUBLE;
        case BOOLEAN:
            return BOOLEAN;
        case STRING:
            return STRING;
        default:
            throw new UnsupportedOperationException("Unsupported avro type " + type);
        }
    }
}
