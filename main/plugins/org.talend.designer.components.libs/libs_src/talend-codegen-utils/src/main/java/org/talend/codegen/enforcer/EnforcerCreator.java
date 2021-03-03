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
import org.talend.daikon.avro.AvroUtils;

/**
 * Instantiates concrete class of {@link DiOutgoingSchemaEnforcer} according to incoming arguments
 */
public final class EnforcerCreator {

    /**
     * Instantiates concrete class of {@link DiOutgoingSchemaEnforcer} according to incoming arguments
     * <code>byIndex</code> parameter is used to specify type of index mapper to use with
     * {@link org.talend.codegen.enforcer.OutgoingDynamicSchemaEnforcer} For non dynamic case by index is always used (in
     * {@link org.talend.codegen.enforcer.OutgoingSchemaEnforcer} )
     *
     * @param designSchema design schema specified by user
     * @param byIndex schema fields mapper mode; true for by index mode; false is for by name mode
     * @return instance of {@link DiOutgoingSchemaEnforcer}
     */
    public static OutgoingSchemaEnforcer createOutgoingEnforcer(Schema designSchema, boolean byIndex) {

        OutgoingSchemaEnforcer enforcer = null;
        if (AvroUtils.isIncludeAllFields(designSchema)) {
            DynamicIndexMapper indexMapper = null;
            if (byIndex) {
                indexMapper = new DynamicIndexMapperByIndex(designSchema);
            } else {
                indexMapper = new DynamicIndexMapperByName(designSchema);
            }
            enforcer = new OutgoingDynamicSchemaEnforcer(designSchema, indexMapper);
        } else {
            enforcer = new OutgoingSchemaEnforcer(designSchema, new IndexMapperByIndex(designSchema));
        }

        return enforcer;
    }
}
