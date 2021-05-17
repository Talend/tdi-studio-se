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
import org.apache.avro.generic.IndexedRecord;

/**
 * Provides means to map design and dynamic fields to runtime fields
 */
interface DynamicIndexMapper extends IndexMapper {

    /**
     * Computes dynamic fields indexes
     *
     * @param runtimeSchema runtime data schema, which goes along with {@link IndexedRecord}
     * @return list of dynamic fields indexes
     */
    List<Integer> computeDynamicFieldsIndexes(Schema runtimeSchema);
}
