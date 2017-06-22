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

import static org.talend.codegen.enforcer.IndexMapper.DYNAMIC;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.avro.Schema;
import org.apache.avro.Schema.Field;
import org.apache.avro.generic.IndexedRecord;

/**
 * This class acts as a wrapper around an arbitrary Avro {@link IndexedRecord} to transform output avro-styled values to the exact
 * Java objects expected by the Talend 6 Studio (which will copy the fields into a POJO in generated code).
 * <p>
 * A wrapper like this should be attached to an input component, for example, to ensure that its outgoing data meets the
 * Schema constraints imposed by the Studio, including:
 * <ul>
 * <li>Coercing the types of the returned objects to *exactly* the type required by the Talend POJO.</li>
 * <li>Placing all of the unresolved columns between the wrapped schema and the output schema in the Dynamic column.</li>
 * </ul>
 * <p>
 * It extends {@link OutgoingSchemaEnforcer} and provides handling for dynamic fields
 */
public class OutgoingDynamicSchemaEnforcer extends OutgoingSchemaEnforcer {

    /**
     * {@link List} of runtime schema {@link Field} names
     */
    private List<String> runtimeFieldsNames;

    /**
     * Contains indexes of dynamic fields (i.e. fields which are present in runtime schema, but are not present in design schema)
     */
    private List<Integer> dynamicFieldsIndexes;

    /**
     * {@link Schema}, which describes dynamic fields (i.e. fields which are present in runtime schema, but are not present in
     * design schema)
     */
    private Schema dynamicFieldsSchema;

    /**
     * Constructor sets values related to dynamic fields handling
     * 
     * @param indexMapper tool, which computes correspondence between design and runtime fields
     */
    public OutgoingDynamicSchemaEnforcer(DynamicIndexMapper indexMapper) {
        super(indexMapper);
    }

    /**
     * Returns dynamic fields schema
     * This method could be called only after {@ling DiOutgoingDynamicSchemaEnforcer#setWrapped(IndexedRecord)} was called
     * 
     * @return dynamic fields schema
     */
    public Schema getDynamicFieldsSchema() {
        return dynamicFieldsSchema;
    }

    /**
     * {@inheritDoc}
     * 
     * Could be called only after first record was wrapped
     * 
     * @param pojoIndex index of required value. Could be from 0 to designSchemaSize
     */
    public Object get(int pojoIndex) {
        int runtimeIndex = indexMap[pojoIndex];
        if (runtimeIndex == DYNAMIC) {
            return getDynamicValues();
        }
        Object avroValue = wrappedRecord.get(runtimeIndex);
        return convertValue(avroValue, runtimeIndex);
    }

    /**
     * Computes {@link this#indexMap}, initializes {@link this#converters}
     * Computes runtime fields and creates dynamic fields schema
     * 
     * @param record first incoming {@link IndexedRecord}
     */
    @Override
    protected void processFirstRecord(IndexedRecord record) {
        super.processFirstRecord(record);
        Schema runtimeSchema = record.getSchema();
        initRuntimeFieldsNames(runtimeSchema);
        this.dynamicFieldsIndexes = ((DynamicIndexMapper) indexMapper).computeDynamicFieldsIndexes(runtimeSchema);
        createDynamicFieldsSchema(record.getSchema());
    }

    /**
     * Retrieves dynamic fields values and returns them as map.
     * Map key is dynamic field name
     * Map value is dynamic field value, transformed to Talend type
     * 
     * @return map with dynamic values
     */
    private Map<String, Object> getDynamicValues() {
        Map<String, Object> dynamicValues = new LinkedHashMap<>();
        for (int dynamicIndex : dynamicFieldsIndexes) {
            Object avroValue = wrappedRecord.get(dynamicIndex);
            Object diValue = convertValue(avroValue, dynamicIndex);
            dynamicValues.put(runtimeFieldsNames.get(dynamicIndex), diValue);
        }
        return dynamicValues;
    }

    /**
     * Creates {@link Schema} of dynamic fields. It is used to create Dynamic Metadatas in DI
     * 
     * @param runtimeSchema Runtime Avro {@link Schema}, it comes with {@link IndexedRecord}
     */
    private void createDynamicFieldsSchema(Schema runtimeSchema) {
        List<Field> dynamicFields = new ArrayList<>();
        for (int dynamicIndex : dynamicFieldsIndexes) {
            Field dynamicField = runtimeSchema.getFields().get(dynamicIndex);
            Field dynamicFieldCopy = new Schema.Field(dynamicField.name(), dynamicField.schema(), dynamicField.doc(),
                    dynamicField.defaultVal());
            Map<String, Object> fieldProperties = dynamicField.getObjectProps();
            for (Map.Entry<String, Object> entry : fieldProperties.entrySet()) {
                Object propValue = entry.getValue();
                if (propValue != null) {
                    dynamicFieldCopy.addProp(entry.getKey(), propValue);
                }
            }
            dynamicFields.add(dynamicFieldCopy);
        }

        dynamicFieldsSchema = Schema.createRecord("dynamic", null, null, false);
        dynamicFieldsSchema.setFields(dynamicFields);
    }

    /**
     * Initializes runtime fields names
     * These fields are used as keys in generated {@link Map} with dynamic values
     * 
     * @param runtimeSchema Runtime Avro {@link Schema}, it comes with {@link IndexedRecord}
     */
    private void initRuntimeFieldsNames(Schema runtimeSchema) {
        runtimeFieldsNames = new ArrayList<>(runtimeSchema.getFields().size());
        for (Field field : runtimeSchema.getFields()) {
            runtimeFieldsNames.add(field.name());
        }
    }
}
