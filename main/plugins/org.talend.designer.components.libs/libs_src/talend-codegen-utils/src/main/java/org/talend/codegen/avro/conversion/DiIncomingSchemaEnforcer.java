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

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import org.apache.avro.LogicalType;
import org.apache.avro.LogicalTypes;
import org.apache.avro.Schema;
import org.apache.avro.SchemaBuilder;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.IndexedRecord;
import org.talend.daikon.avro.AvroUtils;
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
public class DiIncomingSchemaEnforcer implements DiSchemaConstants {

    /**
     * The design-time schema from the Studio that determines how incoming java column data will be interpreted.
     */
    private final Schema incomingDesignSchema;

    /**
     * The position of the dynamic column in the incoming schema. This is -1 if there is no dynamic column. There can be
     * a maximum of one dynamic column in the schema.
     */
    private final int incomingDynamicColumn;

    /**
     * The {@link Schema} of the actual runtime data that will be provided by this object. This will only be null if
     * dynamic columns exist, but they have not been finished initializing.
     */
    private Schema incomingRuntimeSchema;

    /** The fields constructed from dynamic columns. This will only be non-null during construction. */
    private List<Schema.Field> fieldsFromDynamicColumns = null;

    /** The values wrapped by this object. */
    private GenericData.Record wrapped = null;

    /**
     * Access the indexed fields by their name. We should prefer accessing them by index for performance, but this
     * complicates the logic of dynamic columns quite a bit.
     */
    private final Map<String, Integer> columnToFieldIndex = new HashMap<>();

    // TODO(rskraba): fix with a general type conversion strategy.
    private final Map<String, SimpleDateFormat> dateFormatCache = new HashMap<>();

    public DiIncomingSchemaEnforcer(Schema incoming) {
        this.incomingDesignSchema = incoming;
        this.incomingRuntimeSchema = incoming;

        // Find the dynamic column, if any.
        incomingDynamicColumn = AvroUtils.isIncludeAllFields(incoming)
                ? Integer.valueOf(incoming.getProp(DiSchemaConstants.TALEND6_DYNAMIC_COLUMN_POSITION)) : -1;
        if (incomingDynamicColumn != -1) {
            incomingRuntimeSchema = null;
            fieldsFromDynamicColumns = new ArrayList<>();
        }

        // Add all of the runtime columns except any dynamic column to the index map.
        for (Schema.Field f : incoming.getFields()) {
            if (f.pos() != incomingDynamicColumn) {
                columnToFieldIndex.put(f.name(), f.pos());
            }
        }
    }

    /**
     * Take all of the parameters from the dynamic metadata and adapt it to a field for the runtime Schema.
     */
    public void initDynamicColumn(String name, String dbName, String type, String dbType, int dbTypeId, int length, int precision,
            String format, String description, boolean isKey, boolean isNullable, String refFieldName, String refModuleName) {
        if (!needsInitDynamicColumns())
            return;

        // Add each column to the field index and the incoming runtime schema.
        // TODO(rskraba): validate all types coming from the studio and add annotations.
        Schema fieldSchema = null;
        if ("id_String".equals(type)) {
            fieldSchema = Schema.create(Schema.Type.STRING);
        } else if ("id_Boolean".equals(type)) {
            fieldSchema = Schema.create(Schema.Type.BOOLEAN);
        } else if ("id_Integer".equals(type)) {
            fieldSchema = Schema.create(Schema.Type.INT);
        } else if ("id_Long".equals(type)) {
            fieldSchema = Schema.create(Schema.Type.LONG);
        } else if ("id_Double".equals(type)) {
            fieldSchema = Schema.create(Schema.Type.DOUBLE);
        } else if ("id_Float".equals(type)) {
            fieldSchema = Schema.create(Schema.Type.FLOAT);
        } else if ("id_Byte".equals(type)) {
            fieldSchema = AvroUtils._byte();
        } else if ("id_Short".equals(type)) {
            fieldSchema = AvroUtils._short();
        } else if ("id_Character".equals(type)) {
            fieldSchema = AvroUtils._character();
        } else if ("id_BigDecimal".equals(type)) {
            fieldSchema = AvroUtils._decimal();
        } else if ("id_Date".equals(type)) {
            fieldSchema = AvroUtils._date();
        } else {
            throw new UnsupportedOperationException("Unrecognized type " + type);
        }

        if (isNullable) {
            fieldSchema = SchemaBuilder.nullable().type(fieldSchema);
        }
        Schema.Field field = new Schema.Field(name, fieldSchema, description, (Object) null);
        // Set pattern for date type
        if ("id_Date".equals(type) && format != null) {
            field.addProp(SchemaConstants.TALEND_COLUMN_PATTERN, format);
        }
        fieldsFromDynamicColumns.add(field);
    }

    /**
     * Called when dynamic columns have finished being initialized. After this call, the {@link #getDesignSchema()} can be
     * used to get the runtime schema.
     */
    public void initDynamicColumnsFinished() {
        if (!needsInitDynamicColumns())
            return;

        // Copy all of the fields that were initialized from dynamic columns into the runtime Schema.
        boolean dynamicFieldsAdded = false;
        List<Schema.Field> fields = new ArrayList<Schema.Field>();
        for (Schema.Field designField : incomingDesignSchema.getFields()) {
            // Replace the dynamic column by all of its contents.
            if (designField.pos() == incomingDynamicColumn) {
                fields.addAll(fieldsFromDynamicColumns);
                dynamicFieldsAdded = true;
            }
            // Make a complete copy of the field (it can't be reused).
            Schema.Field designFieldCopy = new Schema.Field(designField.name(), designField.schema(), designField.doc(),
                    designField.defaultVal());
            for (Map.Entry<String, Object> e : designField.getObjectProps().entrySet()) {
                designFieldCopy.addProp(e.getKey(), e.getValue());
            }
            fields.add(designFieldCopy);
        }
        if (!dynamicFieldsAdded) {
            fields.addAll(fieldsFromDynamicColumns);
        }

        incomingRuntimeSchema = Schema.createRecord(incomingDesignSchema.getName(), incomingDesignSchema.getDoc(),
                incomingDesignSchema.getNamespace(), incomingDesignSchema.isError());
        incomingRuntimeSchema.setFields(fields);

        // Map all of the fields from the runtime Schema to their index.
        for (Schema.Field f : incomingRuntimeSchema.getFields()) {
            columnToFieldIndex.put(f.name(), f.pos());
        }

        // And indicate that initialization is finished.
        fieldsFromDynamicColumns = null;
    }

    /**
     * @return true only if there is a dynamic column and they haven't been finished initializing yet. When this returns
     * true, the enforcer can't be used yet and {@link #getDesignSchema()} is guaranteed to return null.
     */
    public boolean needsInitDynamicColumns() {
        return fieldsFromDynamicColumns != null;
    }

    public Schema getRuntimeSchema() {
        return incomingRuntimeSchema;
    }

    public Schema getDesignSchema() {
        return incomingDesignSchema;
    }

    public void put(String name, Object v) {
        put(columnToFieldIndex.get(name), v);
    }

    public void put(int i, Object v) {
        if (wrapped == null)
            wrapped = new GenericData.Record(getRuntimeSchema());

        if (v == null) {
            wrapped.put(i, null);
            return;
        }

        // TODO(rskraba): check type validation for correctness with studio objects.
        Schema.Field f = incomingRuntimeSchema.getFields().get(i);
        Schema fieldSchema = AvroUtils.unwrapIfNullable(f.schema());

        Object datum = null;

        boolean isLogicalDate = false;
        LogicalType logicalType = fieldSchema.getLogicalType();
        if (logicalType != null) {
            if (logicalType == LogicalTypes.date() || logicalType == LogicalTypes.timestampMillis()) {
                isLogicalDate = true;
            }
        }

        // TODO(rskraba): This is pretty rough -- fix with a general type conversion strategy.
        String talendType = f.getProp(DiSchemaConstants.TALEND6_COLUMN_TALEND_TYPE);
        String javaClass = fieldSchema.getProp(SchemaConstants.JAVA_CLASS_FLAG);
        if (isLogicalDate || "id_Date".equals(talendType) || "java.util.Date".equals(javaClass)) {
            if (v instanceof Date) {
                datum = v;
            } else if (v instanceof Long) {
                datum = new Date((long) v);
            } else if (v instanceof String) {
                String pattern = f.getProp(DiSchemaConstants.TALEND6_COLUMN_PATTERN);
                String vs = (String) v;

                if (pattern == null || pattern.equals("yyyy-MM-dd'T'HH:mm:ss'000Z'")) {
                    if (!vs.endsWith("000Z")) {
                        throw new RuntimeException("Unparseable date: \"" + vs + "\""); //$NON-NLS-1$ //$NON-NLS-2$
                    }
                    pattern = "yyyy-MM-dd'T'HH:mm:ss";
                    vs.substring(0, vs.lastIndexOf("000Z"));
                }

                SimpleDateFormat df = dateFormatCache.get(pattern);
                if (df == null) {
                    df = new SimpleDateFormat(pattern);
                    df.setTimeZone(TimeZone.getTimeZone("UTC"));
                    dateFormatCache.put(pattern, df);
                }

                try {
                    datum = df.parse((String) v);
                } catch (ParseException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        if ("id_BigDecimal".equals(talendType) || "java.math.BigDecimal".equals(javaClass)) {
            if (v instanceof BigDecimal) {
                datum = v;
            } else if (v instanceof String) {
                datum = new BigDecimal((String) v);
            }
        }

        if (datum == null) {
            switch (fieldSchema.getType()) {
            case ARRAY:
                break;
            case BOOLEAN:
                if (v instanceof Boolean)
                    datum = v;
                else
                    datum = Boolean.valueOf(String.valueOf(v));
                break;
            case FIXED:
            case BYTES:
                if (v instanceof byte[])
                    datum = v;
                else
                    datum = String.valueOf(v).getBytes();
                break;
            case DOUBLE:
                if (v instanceof Number)
                    datum = ((Number) v).doubleValue();
                else
                    datum = Double.valueOf(String.valueOf(v));
                break;
            case ENUM:
                break;
            case FLOAT:
                if (v instanceof Number)
                    datum = ((Number) v).floatValue();
                else
                    datum = Float.valueOf(String.valueOf(v));
                break;
            case INT:
                if (v instanceof Number)
                    datum = ((Number) v).intValue();
                else
                    datum = Integer.valueOf(String.valueOf(v));
                break;
            case LONG:
                if (v instanceof Number)
                    datum = ((Number) v).longValue();
                else
                    datum = Long.valueOf(String.valueOf(v));
                break;
            case MAP:
                break;
            case NULL:
                datum = null;
            case RECORD:
                break;
            case STRING:
                datum = String.valueOf(v);
                break;
            case UNION:
                break;
            default:
                break;
            }
        }

        wrapped.put(i, datum);
    }

    /**
     * @return An IndexedRecord created from the values stored in this enforcer and clears out any existing values.
     */
    public IndexedRecord createIndexedRecord() {
        // Send the data to a new instance of IndexedRecord and clear out the existing values.
        IndexedRecord copy = wrapped;
        wrapped = null;
        return copy;
    }
}