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

import org.apache.avro.Schema;

/**
 * {@link IndexMapper} implementation, which match fields according their indexes
 */
class IndexMapperByIndex implements IndexMapper {

    /**
     * Number of fields in design schema. It is also equaled number of fields of POJO class in case there is no dynamic fields
     */
    private final int designSchemaSize;

    /**
     * Constructor sets design schema size
     *
     * @param designSchema design schema
     */
    IndexMapperByIndex(Schema designSchema) {
        designSchemaSize = designSchema.getFields().size();
    }

    /**
     * {@inheritDoc}
     *
     * If there is no dynamic fields runtime indexes equal design indexes
     * <code>runtimeSchema</code> parameter is not used here
     */
    @Override
    public int[] computeIndexMap(Schema runtimeSchema) {
        int[] indexMap = new int[designSchemaSize];
        for (int i = 0; i < designSchemaSize; i++) {
            indexMap[i] = i;
        }
        return indexMap;
    }

}
