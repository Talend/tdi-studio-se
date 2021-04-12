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
import org.talend.codegen.DiSchemaConstants;
import org.talend.daikon.avro.AvroUtils;

/**
 * {@link DynamicIndexMapper} implementation, which match fields according their indexes
 *
 * When design schema and runtime schema have fields in different order or when there are gaps between non dynamic
 * fields, {@link DynamicIndexMapperByName} should be used instead
 */
class DynamicIndexMapperByIndex implements DynamicIndexMapper {

    /**
     * Number of fields in design schema. It is less then number of fields in POJO by 1 in case of dynamic field
     */
    private final int designSchemaSize;

    /**
     * Dynamic column position in the design schema. Schema can contain 0 or 1 dynamic columns.
     */
    private final int dynamicFieldPosition;

    /**
     * Constructor sets design schema size and dynamic field position
     *
     * @param designSchema design schema
     */
    DynamicIndexMapperByIndex(Schema designSchema) {
        this.designSchemaSize = designSchema.getFields().size();
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
     * That's why -1 value is set for dynamic field index. All other fields should be shifted on dynamicFields positions forward
     */
    @Override
    public int[] computeIndexMap(Schema runtimeSchema) {
        int runtimeSchemaSize = runtimeSchema.getFields().size();
        int dynamicFieldsNumber = runtimeSchemaSize - designSchemaSize;

        int[] indexMap = new int[designSchemaSize + 1];
        for (int i = 0; i < designSchemaSize + 1; i++) {
            if (i == dynamicFieldPosition) {
                indexMap[dynamicFieldPosition] = DYNAMIC;
                continue;
            }
            if (i < dynamicFieldPosition) {
                indexMap[i] = i;
            } else {
                indexMap[i] = dynamicFieldsNumber + i - 1;
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

        ArrayList<Integer> dynamicFieldsIndexes = new ArrayList<>(dynamicFieldsNumber);
        for (int i = 0; i < dynamicFieldsNumber; i++) {
            int dynamicFieldIndex = dynamicFieldPosition + i;
            dynamicFieldsIndexes.add(dynamicFieldIndex);
        }
        return dynamicFieldsIndexes;
    }

}
