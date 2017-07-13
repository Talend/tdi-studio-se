// ============================================================================
//
// Copyright (C) 2006-2017 Talend Inc. - www.talend.com
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.avro.Schema;
import org.apache.avro.Schema.Field;
import org.apache.avro.SchemaBuilder;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericData.Record;
import org.apache.avro.generic.IndexedRecord;
import org.talend.daikon.avro.SchemaConstants;
import org.talend.daikon.avro.converter.AvroConverter;
import org.talend.codegen.converter.DiConverters;
import org.talend.codegen.converter.TypeConverter;
import org.talend.codegen.DynamicFieldUtils;

/**
 * Converts data from DI to Avro format. </br>
 * 
 * It is a part of Di-to-TCOMP adapter. It is used before TCOMP component in the flow to convert DI row to TCOMP
 * {@link IndexedRecord}:
 * <ul>
 * <li>It coerce Talend POJO objects types to expected Avro schema types</li>
 * <li>It unwraps data in a routines.system.Dynamic column into flat fields</li>
 * </ul>
 * One instance of this object can be created per incoming schema and reused.
 */
public class IncomingSchemaEnforcer {

    /**
     * The design-time schema from the Studio that determines how incoming java column data will be interpreted.
     * This schema is retrieved from downstream component's properties
     */
    private final Schema designSchema;

    /**
     * The {@link Schema} of the actual runtime data that will be provided by this object. This will only be null if
     * dynamic columns exist, but they have not been finished initializing.
     */
    private Schema runtimeSchema;

    /**
     * The position of the dynamic field in the incoming schema. This equals to {@link this#NO_DYNAMIC_COLUMN} if there is no
     * dynamic field. There can be
     * only one dynamic field in the schema.
     */
    private final int dynamicFieldPosition;

    /**
     * Collection of fields constructed from dynamic columns. This will only be non-null during construction.
     */
    private List<Schema.Field> dynamicFields;

    /**
     * The values wrapped by this object - current {@link IndexedRecord}
     */
    private GenericData.Record currentRecord;

    /**
     * DI to avro converters list. Converter index corresponds to specific field in {@link Schema}, which this converter will
     * convert
     */
    @SuppressWarnings("rawtypes")
    private List<AvroConverter> converters;

    /**
     * Access the indexed fields by their name. We should prefer accessing them by index for performance, but this
     * complicates the logic of dynamic columns quite a bit.
     */
    private final Map<String, Integer> nameToIndexMap = new HashMap<>();

    /**
     * Constructor set design {@link Schema}, checks whether there is dynamic column in schema, computes its position.
     * If there is no dynamic field:
     * <ul>
     * <li>Computes field name to field index map</li>
     * <li>Initializes DI to Avro converters</li>
     * </ul>
     * If there is dynamic field above actions are done in {@link this#createRuntimeSchema()} method
     * 
     * @param incoming design schema retrieved from downstream component properties
     */
    public IncomingSchemaEnforcer(Schema incoming) {
        designSchema = incoming;

        dynamicFieldPosition = DynamicFieldUtils.getDynamicFieldPosition(designSchema);
        if (dynamicFieldPosition != DynamicFieldUtils.NO_DYNAMIC_FIELD) {
            runtimeSchema = null;
            dynamicFields = new ArrayList<>();
        } else {
            runtimeSchema = designSchema;
            computeNameToIndexMap();
            converters = DiConverters.initConverters(runtimeSchema);
        }
    }

    /**
     * Compute mapping of field name to field index. This is used in {@link this#put(String, Object)} method for quick index
     * computation
     * However, it is not clear, which approach is better:
     * 1. Use this mapping and put values by field index
     * 2. Or just use {@link Record#put(String, Object)} and put values by field name
     */
    private void computeNameToIndexMap() {
        for (Schema.Field f : runtimeSchema.getFields()) {
            nameToIndexMap.put(f.name(), f.pos());
        }
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
    public void addDynamicField(String name, String diType, String logicalType, String fieldPattern, String description,
            boolean isNullable) {
        if (areDynamicFieldsInitialized())
            return;
        Schema fieldSchema = TypeConverter.diToAvro(diType, logicalType);

        if (isNullable) {
            fieldSchema = SchemaBuilder.nullable().type(fieldSchema);
        }
        Schema.Field field = new Schema.Field(name, fieldSchema, description, (Object) null);
        // Set pattern for date type
        if ("id_Date".equals(diType) && fieldPattern != null) {
            field.addProp(SchemaConstants.TALEND_COLUMN_PATTERN, fieldPattern);
        }
        dynamicFields.add(field);
    }

    /**
     * Creates runtime schema from design schema and dynamic fields.
     * Design schema is set in Constructor during enforcer initialization.
     * Dynamic fields are recreated by calling {@link this#addDynamicField()} methods.
     * This method should be called only after all dynamic fields are recreated.
     * Also should be called before calling {@link this#put()} and {@link this#createIndexedRecord()} methods
     */
    public void createRuntimeSchema() {
        if (areDynamicFieldsInitialized()) {
            return;
        }

        // Copy all of the fields that were initialized from dynamic columns into the runtime Schema.
        boolean dynamicFieldsAdded = false;
        List<Schema.Field> fields = new ArrayList<Schema.Field>();
        for (Schema.Field designField : designSchema.getFields()) {
            // Replace the dynamic column by all of its contents.
            if (designField.pos() == dynamicFieldPosition) {
                fields.addAll(dynamicFields);
                dynamicFieldsAdded = true;
            }
            Schema.Field designFieldCopy = copyField(designField);
            fields.add(designFieldCopy);
        }
        if (!dynamicFieldsAdded) {
            fields.addAll(dynamicFields);
        }

        runtimeSchema = Schema.createRecord(designSchema.getName(), designSchema.getDoc(), designSchema.getNamespace(),
                designSchema.isError());
        runtimeSchema.setFields(fields);

        computeNameToIndexMap();

        // And indicate that initialization is finished.
        dynamicFields = null;

        // creates converters list taking into account dynamic fields
        converters = DiConverters.initConverters(runtimeSchema);
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
     * Creates new instance for {@link IndexedRecord}
     * This should be called before series of {@link this#put()} calls, which copies values from next DI data object into this
     * enforcer
     */
    public void createNewRecord() {
        currentRecord = new GenericData.Record(getRuntimeSchema());
    }

    /**
     * Converts DI data value to Avro format and put it into record by field name
     * 
     * @param name field name
     * @param diValue data value
     */
    public void put(String name, Object diValue) {
        put(nameToIndexMap.get(name), diValue);
    }

    /**
     * Converts DI data value to Avro format and put it into record by field index
     * 
     * @param index field index to put in
     * @param diValue data value in DI format
     */
    @SuppressWarnings("unchecked")
    public void put(int index, Object diValue) {
        Object avroValue = null;
        if (diValue != null) {
            avroValue = converters.get(index).convertToAvro(diValue);
        }
        currentRecord.put(index, avroValue);
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
     * Returns design schema
     * 
     * @return design schema
     */
    Schema getDesignSchema() {
        return designSchema;
    }

    /**
     * Return runtime schema
     * 
     * @return runtime schema
     */
    Schema getRuntimeSchema() {
        return runtimeSchema;
    }

}