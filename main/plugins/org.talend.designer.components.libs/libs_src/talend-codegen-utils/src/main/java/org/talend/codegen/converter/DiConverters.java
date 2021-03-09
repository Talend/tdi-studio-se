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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.avro.Schema;
import org.apache.avro.Schema.Field;
import org.apache.avro.Schema.Type;
import org.talend.daikon.avro.AvroUtils;
import org.talend.daikon.avro.LogicalTypeUtils;
import org.talend.daikon.avro.SchemaConstants;
import org.talend.daikon.avro.converter.AvroConverter;

/**
 * DI converters registry. It contains all available converters to convert data from DI to Avro format and vice versa.
 * Provides method to initialize a list of converters from Avro schema
 */
public class DiConverters {

    /**
     * A map of converters for Avro logical types. A key is logical type name and value is corresponding {@link AvroConverter}
     */
    @SuppressWarnings("rawtypes")
    private static final Map<String, AvroConverter> LOGICAL_CONVERTERS;

    /**
     * A map of converters for DI types, which have no corresponding type in Avro and require mapping. </br>
     * A key is {@link SchemaConstants#JAVA_CLASS_FLAG} and value is corresponding {@link AvroConverter}
     */
    @SuppressWarnings("rawtypes")
    private static final Map<String, AvroConverter> DI_TYPES_CONVERTERS;

    /**
     * A map of converters for types which are represented both in DI and Avro type sets
     */
    @SuppressWarnings("rawtypes")
    private static final Map<Type, AvroConverter> AVRO_TYPES_CONVERTERS;

    /**
     * Initialize all available converters
     */
    static {
        LOGICAL_CONVERTERS = new HashMap<>();
        LOGICAL_CONVERTERS.put(LogicalTypeUtils.DATE, new DateLogicalDateConverter());
        LOGICAL_CONVERTERS.put(LogicalTypeUtils.TIME_MILLIS, IdentityConverter.getInstance());
        LOGICAL_CONVERTERS.put(LogicalTypeUtils.TIMESTAMP_MILLIS, new DateLogicalTimestampConverter());

        DI_TYPES_CONVERTERS = new HashMap<>();
        DI_TYPES_CONVERTERS.put(BigDecimal.class.getCanonicalName(), new BigDecimalStringDoubleConverter());
        DI_TYPES_CONVERTERS.put(Byte.class.getCanonicalName(), new ByteIntegerConverter());
        DI_TYPES_CONVERTERS.put(Character.class.getCanonicalName(), new CharStringConverter());
        DI_TYPES_CONVERTERS.put(Short.class.getCanonicalName(), new ShortIntegerConverter());
        DI_TYPES_CONVERTERS.put(Date.class.getCanonicalName(), new DateLogicalTimestampConverter());

        AVRO_TYPES_CONVERTERS = new HashMap<>();
        AVRO_TYPES_CONVERTERS.put(Type.ARRAY, IdentityConverter.getInstance());
        AVRO_TYPES_CONVERTERS.put(Type.BOOLEAN, IdentityConverter.getInstance());
        AVRO_TYPES_CONVERTERS.put(Type.BYTES, IdentityConverter.getInstance());
        AVRO_TYPES_CONVERTERS.put(Type.DOUBLE, IdentityConverter.getInstance());
        AVRO_TYPES_CONVERTERS.put(Type.FLOAT, IdentityConverter.getInstance());
        AVRO_TYPES_CONVERTERS.put(Type.INT, IdentityConverter.getInstance());
        AVRO_TYPES_CONVERTERS.put(Type.LONG, IdentityConverter.getInstance());
        AVRO_TYPES_CONVERTERS.put(Type.STRING, IdentityConverter.getInstance());
    }

    /**
     * ArrayList is used because converters will be retrieved very often (simple array can be used instead)
     *
     * @param recordSchema
     * @return
     */
    @SuppressWarnings("rawtypes")
    public static List<AvroConverter> initConverters(Schema recordSchema) {
        ArrayList<AvroConverter> converters = new ArrayList<>();
        List<Field> fields = recordSchema.getFields();
        for (Field field : fields) {
            Schema fieldSchema = AvroUtils.unwrapIfNullable(field.schema());
            AvroConverter converter = initConverter(fieldSchema);
            converters.add(converter);
        }
        return converters;
    }

    /**
     * Initialize converter for specified <code>fieldScheam</code>
     *
     * @param fieldSchema unwrapped schema of record field
     * @return converter for specified <code>fieldScheam</code>
     */
    @SuppressWarnings("rawtypes")
    private static AvroConverter initConverter(Schema fieldSchema) {

        if (LogicalTypeUtils.getLogicalTypeName(fieldSchema) != null) {
            return LOGICAL_CONVERTERS.get(LogicalTypeUtils.getLogicalTypeName(fieldSchema));
        }
        if (fieldSchema.getProp(SchemaConstants.JAVA_CLASS_FLAG) != null) {
            return DI_TYPES_CONVERTERS.get(fieldSchema.getProp(SchemaConstants.JAVA_CLASS_FLAG));
        }

        return AVRO_TYPES_CONVERTERS.get(fieldSchema.getType());
    }
}
