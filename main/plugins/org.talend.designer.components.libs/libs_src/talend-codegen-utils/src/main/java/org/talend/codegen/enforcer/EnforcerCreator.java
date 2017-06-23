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

import org.apache.avro.Schema;
import org.talend.daikon.avro.AvroUtils;

/**
 * Instantiates concrete class of {@link OutgoingSchemaEnforcer} according to incoming arguments
 */
public final class EnforcerCreator {

    /**
     * Instantiates concrete class of {@link OutgoingSchemaEnforcer} according to incoming arguments
     * <code>byIndex</code> parameter is used to specify type of index mapper to use with
     * {@link org.talend.daikon.di.DiOutgoingDynamicSchemaEnforcer} For non dynamic case by index is always used (in
     * {@link org.talend.daikon.di.DiOutgoingSchemaEnforcer} )
     *
     * @param designSchema design schema specified by user
     * @param byIndex schema fields mapper mode; true for by index mode; false is for by name mode
     * @return instance of {@link OutgoingSchemaEnforcer}
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
            enforcer = new OutgoingDynamicSchemaEnforcer(indexMapper);
        } else {
            enforcer = new OutgoingSchemaEnforcer(new IndexMapperByIndex(designSchema));
        }

        return enforcer;
    }
}
