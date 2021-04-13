// ============================================================================
//
// Copyright (C) 2006-2019 Talend Inc. - www.talend.com
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

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import org.apache.avro.Schema;
import org.apache.avro.Schema.Field;
import org.apache.avro.SchemaBuilder;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.IndexedRecord;
import org.talend.codegen.DiSchemaConstants;
import org.talend.daikon.avro.AvroUtils;
import org.talend.daikon.avro.LogicalTypeUtils;
import org.talend.daikon.avro.SchemaConstants;

/**
 * <b>You should almost certainly not be using this class.</b>
 *
 * This class acts as a wrapper around arbitrary values to coerce the Talend 6 Studio types in a generated POJO to a
 * {@link IndexedRecord} object that can be processed in the next component..
 * <p>
 * A wrapper like this should be attached before an output component, for example, to ensure that its incoming data with
 * the constraints imposed by the Studio meet the contract of the component framework, for example:
 * <ul>
 * <li>Coercing the types of the Talend POJO objects to expected Avro schema types.</li>
 * <li>Unwrapping data in a routines.system.Dynamic column into flat fields.</li>
 * </ul>
 * <p>
 * One instance of this object can be created per incoming schema and reused.
 */
public class IncomingSchemaEnforcer {

    /**
     * Dynamic column position possible value, which means schema doesn't have dynamic column
     */
    private static final int NO_DYNAMIC_COLUMN = -1;

    /**
     * The number of milliseconds in one day
     */
    private static final long ONE_DAY = 1000 * 60 * 60 * 24;

    /**
     * The design-time schema from the Studio that determines how incoming java column data will be interpreted.
     * This schema is retrieved from downstream component's properties
     */
    private final Schema designSchema;

    /**
     * The position of the dynamic column in the incoming schema. This is -1 if there is no dynamic column. There can be
     * a maximum of one dynamic column in the schema.
     */
    private final int dynamicColumnPosition;

    /**
     * The {@link Schema} of the actual runtime data that will be provided by this object. This will only be null if
     * dynamic columns exist, but they have not been finished initializing.
     */
    private Schema runtimeSchema;

    /**
     * Collection of fields constructed from dynamic columns. This will only be non-null during construction.
     */
    private List<Schema.Field> dynamicFields = null;

    /**
     * Collection of fields constructed from incoming node columns not in designSchema.
     */
    private List<Schema.Field> incomingFields = null;

    /**
     * The values wrapped by this object - current {@link IndexedRecord}
     */
    private GenericData.Record currentRecord = null;

    /**
     * Access the indexed fields by their name. We should prefer accessing them by index for performance, but this
     * complicates the logic of dynamic columns quite a bit.
     */
    private final Map<String, Integer> columnToFieldIndex = new HashMap<>();

    // TODO(rskraba): fix with a general type conversion strategy.
    private final Map<String, SimpleDateFormat> dateFormatCache = new HashMap<>();

    /**
     * Constructor
     *
     * @param incoming design schema retrieved from downstream component properties
     */
    public IncomingSchemaEnforcer(Schema incoming) {
        designSchema = incoming;
        incomingFields = new ArrayList<>();
        // Find the dynamic column, if any.
        dynamicColumnPosition = AvroUtils.isIncludeAllFields(designSchema)
                ? Integer.valueOf(designSchema.getProp(DiSchemaConstants.TALEND6_DYNAMIC_COLUMN_POSITION)) : NO_DYNAMIC_COLUMN;
        if (dynamicColumnPosition != NO_DYNAMIC_COLUMN) {
            runtimeSchema = null;
            dynamicFields = new ArrayList<>();
        } else {
            runtimeSchema = designSchema;
        }

        // Add all of the runtime columns except any dynamic column to the index map.
        for (Schema.Field f : designSchema.getFields()) {
            if (f.pos() != dynamicColumnPosition) {
                columnToFieldIndex.put(f.name(), f.pos());
            }
        }
    }

    /**
     * Take all of the parameters from the dynamic metadata and adapt it to a field for the runtime Schema.
     *
     * @deprecated because it was renamed. Use {@link this#addDynamicField(String, String, String, String, boolean)} instead
     */
    @Deprecated
    public void initDynamicColumn(String name, String dbName, String type, String dbType, int dbTypeId, int length, int precision,
            String format, String description, boolean isKey, boolean isNullable, String refFieldName, String refModuleName) {
        addDynamicField(name, type, null, format, description, isNullable);
    }

    /**
     * Recreates dynamic field from parameters retrieved from DI dynamic metadata
     *
     * @param name dynamic field name
     * @param diType di column type
     * @param logicalType dynamic field logical type; could be null
     * @param fieldPattern dynamic field date format
     * @param description dynamic field description
     * @param isNullable defines whether dynamic field may contain <code>null</code> value
     * @param isKey defines whether dynamic field is key field
     */
    public void addDynamicField(String name, String diType, String logicalType, String fieldPattern, String description,
            boolean isNullable, boolean isKey) {
        if (!needsInitDynamicColumns())
            return;
        Schema fieldSchema = diToAvro(diType, logicalType);

        if (isNullable) {
            fieldSchema = SchemaBuilder.nullable().type(fieldSchema);
        }

        Schema.Field field = new Schema.Field(name, fieldSchema, description, (Object) null);
        // Set pattern for date type
        if ("id_Date".equals(diType) && fieldPattern != null) {
            field.addProp(SchemaConstants.TALEND_COLUMN_PATTERN, fieldPattern);
        }
        if (isKey) {
            field.addProp(SchemaConstants.TALEND_COLUMN_IS_KEY, "true");
        }
        dynamicFields.add(field);
    }

    /**
     * Recreates dynamic field from parameters retrieved from DI dynamic metadata
     *
     * @param name dynamic field name
     * @param diType di column type
     * @param logicalType dynamic field logical type; could be null
     * @param fieldPattern dynamic field date format
     * @param description dynamic field description
     * @param isNullable defines whether dynamic field may contain <code>null</code> value
     */
    @Deprecated
    public void addDynamicField(String name, String diType, String logicalType, String fieldPattern, String description,
            boolean isNullable) {
        addDynamicField(name, diType, logicalType, fieldPattern, description, isNullable, false);
    }

    public void addIncomingNodeField(String name, String className) {
        String diType = "id_String";
        switch (className) {
        case "java.lang.String":
            diType = "id_String";
            break;
        case "java.lang.Boolean":
            diType = "id_Boolean";
            break;
        case "java.lang.Integer":
            diType = "id_Integer";
            break;
        case "java.lang.Long":
            diType = "id_Long";
            break;
        case "java.lang.Double":
            diType = "id_Double";
            break;
        case "java.lang.Float":
            diType = "id_Float";
            break;
        case "java.lang.Byte":
            diType = "id_Byte";
            break;
        case "java.lang.Short":
            diType = "id_Short";
            break;
        case "java.lang.Character":
            diType = "id_Character";
            break;
        case "java.lang.BigDecimal":
            diType = "id_BigDecimal";
            break;
        case "java.lang.Date":
            diType = "id_Date";
        default:
            diType = "id_String";
        }
        Schema fieldSchema = diToAvro(diType, null);
        Schema.Field field = new Schema.Field(name, fieldSchema, "", (Object) null);
        incomingFields.add(field);
    }

    /**
     * Converts DI type to Avro field schema
     *
     * @param diType data integration native type
     * @param logicalType avro logical type
     * @return field schema
     * @throws {@link UnsupportedOperationException} in case of unsupported di type or logical type
     */
    Schema diToAvro(String diType, String logicalType) {
        Schema fieldSchema = LogicalTypeUtils.getSchemaByLogicalType(logicalType);
        if (fieldSchema != null) {
            return fieldSchema;
        }

        if ("id_String".equals(diType)) {
            fieldSchema = Schema.create(Schema.Type.STRING);
        } else if ("id_Boolean".equals(diType)) {
            fieldSchema = Schema.create(Schema.Type.BOOLEAN);
        } else if ("id_Integer".equals(diType)) {
            fieldSchema = Schema.create(Schema.Type.INT);
        } else if ("id_Long".equals(diType)) {
            fieldSchema = Schema.create(Schema.Type.LONG);
        } else if ("id_Double".equals(diType)) {
            fieldSchema = Schema.create(Schema.Type.DOUBLE);
        } else if ("id_Float".equals(diType)) {
            fieldSchema = Schema.create(Schema.Type.FLOAT);
        } else if ("id_Byte".equals(diType)) {
            fieldSchema = AvroUtils._byte();
        } else if ("id_Short".equals(diType)) {
            fieldSchema = AvroUtils._short();
        } else if ("id_Character".equals(diType)) {
            fieldSchema = AvroUtils._character();
        } else if ("id_BigDecimal".equals(diType)) {
            fieldSchema = AvroUtils._decimal();
        } else if ("id_Date".equals(diType)) {
            fieldSchema = AvroUtils._date();
        } else if ("id_byte[]".equals(diType)) {
            fieldSchema = AvroUtils._bytes();
        } else {
            throw new UnsupportedOperationException("Unrecognized type " + diType);
        }
        return fieldSchema;
    }

    /**
     * Called when dynamic columns have finished being initialized. After this call, the {@link #getDesignSchema()} can be
     * used to get the runtime schema.
     *
     * @deprecated because it was renamed. Use {@link this#recreateRuntimeSchema()} instead
     */
    @Deprecated
    public void initDynamicColumnsFinished() {
        createRuntimeSchema();
    }

    /**
     * Creates runtime schema from design schema and dynamic fields.
     * Design schema is set in Constructor during enforcer initialization.
     * Dynamic fields are recreated by calling {@link this#addDynamicField()} methods.
     * This method should be called only after all dynamic fields are recreated.
     * Also should be called before calling {@link this#put()} and {@link this#createIndexedRecord()} methods
     */
    public void createRuntimeSchema() {
        if (areDynamicFieldsInitialized() && incomingFields.size() == 0) {
            return;
        }

        // Copy all of the fields that were initialized from dynamic columns into the runtime Schema.
        boolean dynamicFieldsAdded = false;
        List<Schema.Field> fields = new ArrayList<Schema.Field>();
        for (Schema.Field designField : designSchema.getFields()) {
            // Replace the dynamic column by all of its contents.
            if (designField.pos() == dynamicColumnPosition) {
                fields.addAll(dynamicFields);
                dynamicFieldsAdded = true;
            }
            Schema.Field designFieldCopy = copyField(designField);
            fields.add(designFieldCopy);
        }

        // Copy fields from incoming node
        fields.addAll(incomingFields);

        if (!dynamicFieldsAdded && dynamicFields != null) {
            fields.addAll(dynamicFields);
        }

        runtimeSchema = Schema.createRecord(designSchema.getName(), designSchema.getDoc(), designSchema.getNamespace(),
                designSchema.isError());
        runtimeSchema.setFields(fields);

        // Map all of the fields from the runtime Schema to their index.
        for (Schema.Field f : runtimeSchema.getFields()) {
            columnToFieldIndex.put(f.name(), f.pos());
        }

        // reset incoming fields
        incomingFields.clear();
        // And indicate that initialization is finished.
        dynamicFields = null;
    }

    /**
     * Creates copy of Avro schema field
     * Field from one schema can't be reused in another
     *
     * @param original original field
     * @return field copy
     */
    private Field copyField(Field original) {
        Field copy = new Schema.Field(original.name(), original.schema(), original.doc(), original.defaultVal());
        for (Map.Entry<String, Object> e : original.getObjectProps().entrySet()) {
            copy.addProp(e.getKey(), e.getValue());
        }
        return copy;
    }

    /**
     * @deprecated because it was renamed. Use {@link this#areDynamicFieldsInitialized()} instead
     * @return true only if there is a dynamic column and they haven't been finished initializing yet. When this returns
     * true, the enforcer can't be used yet and {@link #getDesignSchema()} is guaranteed to return null.
     */
    @Deprecated
    public boolean needsInitDynamicColumns() {
        return !areDynamicFieldsInitialized();
    }

    /**
     * Checks whether dynamic fields were already initialized.
     * Dynamic fields are initialized using parameters from the first incoming data object.
     * Thus, this method returns <code>false</code>, if dynamic fields were not initialized yet (before first data object).
     * It returns <code>true</code>, if dynamic fields were initialized (after first data object)
     *
     * @return true, if dynamic fields were initialized; false - otherwise
     */
    public boolean areDynamicFieldsInitialized() {
        return dynamicFields == null;
    }

    /**
     * Return runtime schema
     *
     * @return runtime schema
     */
    public Schema getRuntimeSchema() {
        return runtimeSchema;
    }

    /**
     * Returns design schema
     *
     * @return design schema
     */
    public Schema getDesignSchema() {
        return designSchema;
    }

    /**
     * Converts DI data value to Avro format and put it into record by field name
     *
     * @param name field name
     * @param diValue data value
     */
    public void put(String name, Object diValue) {
        put(columnToFieldIndex.get(name), diValue);
    }

    /**
     * Converts DI data value to Avro format and put it into record by field index
     *
     * @param index field index to put in
     * @param diValue data value in DI format
     */
    public void put(int index, Object diValue) {
        // TODO(igonchar): client should call createNewRecord by himself. createNewRecord() call
        // will be removed after changing codegen in Studio
        if (currentRecord == null) {
            createNewRecord();
        }

        if (diValue == null) {
            currentRecord.put(index, null);
            return;
        }

        // TODO(rskraba): check type validation for correctness with studio objects.
        Schema.Field field = runtimeSchema.getFields().get(index);
        Schema fieldSchema = AvroUtils.unwrapIfNullable(field.schema());

        Object avroValue = null;

        // TODO(rskraba): This is pretty rough -- fix with a general type conversion strategy.
        String talendType = field.getProp(DiSchemaConstants.TALEND6_COLUMN_TALEND_TYPE);
        String javaClass = fieldSchema.getProp(SchemaConstants.JAVA_CLASS_FLAG);

        // TODO(igonchar): This is wrong. However I left it as is. We have to fix it after release
        // Seems, talendType is added by Studio to schema
        //seems only a safe check for Date with Long and String type input,in fact, will not enter them, so consider remove the code below,
        //or it works for some old job?
        if ("java.util.Date".equals(javaClass) || "id_Date".equals(talendType)) {
            if (diValue instanceof Date) {
                avroValue = diValue;
            } else if (diValue instanceof Long) {
                // TODO(igonchar): This is wrong. Avro date value should be stored as Long, not Date
                avroValue = new Date((long) diValue);
            } else if (diValue instanceof String) {
                String pattern = field.getProp(DiSchemaConstants.TALEND6_COLUMN_PATTERN);
                String vs = (String) diValue;

                if (pattern == null || pattern.equals("yyyy-MM-dd'T'HH:mm:ss'000Z'")) {
                    if (!vs.endsWith("000Z")) {
                        throw new RuntimeException("Unparseable date: \"" + vs + "\""); //$NON-NLS-1$ //$NON-NLS-2$
                    }
                    pattern = "yyyy-MM-dd'T'HH:mm:ss";
                }

                SimpleDateFormat df = dateFormatCache.get(pattern);
                if (df == null) {
                    df = new SimpleDateFormat(pattern);
                    df.setTimeZone(TimeZone.getTimeZone("UTC"));
                    dateFormatCache.put(pattern, df);
                }

                try {
                    avroValue = df.parse((String) diValue);
                } catch (ParseException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        if (LogicalTypeUtils.isLogicalDate(fieldSchema)) {
            // (igonchar): diValue MUST be of java.util.Date
            // (dchmyga): we need to count days according to OUR date, and not timestamp in UTC.
            Date diDate = (Date) diValue;
            OffsetDateTime odt = LocalDateTime.ofInstant(diDate.toInstant(), ZoneId.systemDefault()).atOffset(ZoneOffset.UTC);
            int avroDays = (int) ChronoUnit.DAYS.between(Instant.EPOCH, odt);
            currentRecord.put(index, avroDays);
            return;
        }

        if (LogicalTypeUtils.isLogicalTimestampMillis(fieldSchema)) {
            // (igonchar): diValue MUST be of java.util.Date
            Date diDate = (Date) diValue;
            long avroTimestamp = diDate.getTime();
            currentRecord.put(index, avroTimestamp);
            return;
        }

        if (LogicalTypeUtils.isLogicalTimeMillis(fieldSchema)) {
            //the writer in snowflakewriter can process int and date both, snowflakewriter is the unique writer which studio use for logicalTime type.
            currentRecord.put(index, diValue);
            return;
        }

        // TODO(igonchar): I'm not sure it is correct. For me avro value should be string. Conversion to BigDecimal may be
        // delegated to component. Component should decide whether convert to BigDecimal
        if ("id_BigDecimal".equals(talendType) || "java.math.BigDecimal".equals(javaClass)) {
            if (diValue instanceof BigDecimal) {
                avroValue = diValue;
            } else if (diValue instanceof String) {
                avroValue = new BigDecimal((String) diValue);
            }
        }

        if (avroValue == null) {
            switch (fieldSchema.getType()) {
            case ARRAY:
                break;
            case BOOLEAN:
                if (diValue instanceof Boolean)
                    avroValue = diValue;
                else
                    avroValue = Boolean.valueOf(String.valueOf(diValue));
                break;
            case FIXED:
            case BYTES:
                if (diValue instanceof byte[] || "java.lang.Object".equals(javaClass))
                    avroValue = diValue;
                else
                    avroValue = String.valueOf(diValue).getBytes();
                break;
            case DOUBLE:
                if (diValue instanceof Number)
                    avroValue = ((Number) diValue).doubleValue();
                else
                    avroValue = Double.valueOf(String.valueOf(diValue));
                break;
            case ENUM:
                break;
            case FLOAT:
                if (diValue instanceof Number)
                    avroValue = ((Number) diValue).floatValue();
                else
                    avroValue = Float.valueOf(String.valueOf(diValue));
                break;
            case INT:
                if (diValue instanceof Number)
                    avroValue = ((Number) diValue).intValue();
                else
                    avroValue = Integer.valueOf(String.valueOf(diValue));
                break;
            case LONG:
                if (diValue instanceof Number)
                    avroValue = ((Number) diValue).longValue();
                else
                    avroValue = Long.valueOf(String.valueOf(diValue));
                break;
            case MAP:
                break;
            case NULL:
                avroValue = null;
                break;
            case RECORD:
                break;
            case STRING:
                avroValue = String.valueOf(diValue);
                break;
            case UNION:
                break;
            default:
                break;
            }
        }

        currentRecord.put(index, avroValue);
    }

    /**
     * @return An IndexedRecord created from the values stored in this enforcer and clears out any existing values.
     */
    public IndexedRecord createIndexedRecord() {
        // Send the data to a new instance of IndexedRecord and clear out the existing values.
        IndexedRecord copy = currentRecord;
        currentRecord = null;
        return copy;
    }

    /**
     * Returns current {@link IndexedRecord}
     *
     * @return current {@link IndexedRecord}
     */
    public IndexedRecord getCurrentRecord() {
        return currentRecord;
    }

    /**
     * Creates new instance for {@link IndexedRecord}
     * This should be called before series of {@link this#put()} calls, which copies values from next DI data object into this
     * enforcer
     */
    public void createNewRecord() {
        currentRecord = new GenericData.Record(getRuntimeSchema());
    }

}