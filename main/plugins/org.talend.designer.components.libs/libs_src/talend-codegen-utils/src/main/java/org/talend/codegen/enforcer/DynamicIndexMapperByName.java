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

import java.util.ArrayList;
import java.util.List;

import org.apache.avro.Schema;
import org.apache.avro.Schema.Field;
import org.talend.codegen.DiSchemaConstants;
import org.talend.daikon.avro.AvroUtils;

/**
 * {@link DynamicIndexMapper} implementation, which match fields according their names
 */
class DynamicIndexMapperByName implements DynamicIndexMapper {

    /**
     * {@link Schema} which was specified by user during setting component properties (at design time)
     * This schema may contain di-specific properties
     */
    private final Schema designSchema;

    /**
     * A {@link List} of design schema {@link Field}s
     */
    private final List<Field> designFields;

    /**
     * Number of fields in design schema. It is less then number of fields in POJO by 1 in case of dynamic field
     */
    private final int designSchemaSize;

    /**
     * Dynamic column position in the design schema. Schema can contain 0 or 1 dynamic columns.
     */
    private final int dynamicFieldPosition;

    /**
     * Constructor sets design schema, design schema fields and size and dynamic field position
     *
     * @param designSchema design schema
     */
    DynamicIndexMapperByName(Schema designSchema) {
        this.designSchema = designSchema;
        this.designFields = designSchema.getFields();
        this.designSchemaSize = designFields.size();
        String dynamicFieldProperty = designSchema.getProp(DiSchemaConstants.TALEND6_DYNAMIC_COLUMN_POSITION);
        if (AvroUtils.isIncludeAllFields(designSchema) && dynamicFieldProperty != null) {
            dynamicFieldPosition = Integer.valueOf(dynamicFieldProperty);
        } else {
            throw new IllegalArgumentException("Design schema doesn't contain dynamic field");
        }
    }

    /**
     * {@inheritDoc}
     *
     * If there is dynamic field corresponding value has no sense, because there are several runtime fields which corresponds
     * dynamic field.
     * That's why <b>-1</b> value is set for dynamic field index.
     * If design Schema contains fields that don't present in runtime schema set index to <b>-2</b>.
     */
    @Override
    public int[] computeIndexMap(Schema runtimeSchema) {
        int[] indexMap = new int[designSchemaSize + 1];

        indexMap[dynamicFieldPosition] = DYNAMIC;
        for (int i = 0; i < designSchemaSize; i++) {
            Field designField = designFields.get(i);
            String fieldName = designField.name();
            Field runtimeField = runtimeSchema.getField(fieldName);
            if (i < dynamicFieldPosition) {
                indexMap[i] = runtimeField != null ? runtimeField.pos() : MISSING_DESIGN_FIELD;
            } else {
                indexMap[i + 1] = runtimeField != null ? runtimeField.pos() : MISSING_DESIGN_FIELD;
            }

        }
        return indexMap;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Integer> computeDynamicFieldsIndexes(Schema runtimeSchema) {
        int runtimeSchemaSize = runtimeSchema.getFields().size();
        int dynamicFieldsNumber = runtimeSchemaSize - designSchemaSize;

        ArrayList<Integer> dynamicFieldsIndexes = new ArrayList<Integer>(dynamicFieldsNumber);
        for (Field runtimeField : runtimeSchema.getFields()) {
            String fieldName = runtimeField.name();
            Field designField = designSchema.getField(fieldName);
            if (designField == null) {
                dynamicFieldsIndexes.add(runtimeField.pos());
            }
        }
        return dynamicFieldsIndexes;
    }

}
