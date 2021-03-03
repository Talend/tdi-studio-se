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

import java.util.List;

import org.apache.avro.Schema;
import org.apache.avro.Schema.Field;

/**
 * {@link IndexMapper} implementation, which match fields according their names
 */
class IndexMapperByName implements IndexMapper {

    /**
     * {@link Schema} which was specified by user during setting component properties (at design time)
     * This schema may contain di-specific properties
     */
    private final Schema designSchema;

    /**
     * Constructor sets design schema
     *
     * @param designSchema design schema
     */
    IndexMapperByName(Schema designSchema) {
        this.designSchema = designSchema;
    }

    /**
     * {@inheritDoc}
     *
     * For each design field it finds corresponding runtime field by name and then uses runtime field's position
     */
    @Override
    public int[] computeIndexMap(Schema runtimeSchema) {
        int designSchemaSize = designSchema.getFields().size();
        int[] indexMap = new int[designSchemaSize];
        List<Field> designFields = designSchema.getFields();
        for (int i = 0; i < designSchemaSize; i++) {
            Field designField = designFields.get(i);
            String fieldName = designField.name();
            Field runtimeField = runtimeSchema.getField(fieldName);
            indexMap[i] = runtimeField.pos();
        }
        return indexMap;
    }

}
