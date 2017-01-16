package org.talend.codegen.avro.conversion;

import org.apache.avro.Schema;
import org.talend.daikon.avro.AvroUtils;

/**
 * Instantiates concrete class of {@link DiOutgoingSchemaEnforcer} according to incoming arguments
 */
public final class EnforcerCreator {

    /**
     * Instantiates concrete class of {@link DiOutgoingSchemaEnforcer} according to incoming arguments
     * 
     * @param designSchema design schema specified by user
     * @param byIndex schema fields mapper mode; true for by index mode; false is for by name mode
     * @return instance of {@link DiOutgoingSchemaEnforcer}
     */
    public static DiOutgoingSchemaEnforcer createOutgoingEnforcer(Schema designSchema, boolean byIndex) {

        DiOutgoingSchemaEnforcer enforcer = null;
        if (AvroUtils.isIncludeAllFields(designSchema)) {
            DynamicIndexMapper indexMapper = null;
            if (byIndex) {
                indexMapper = new DynamicIndexMapperByIndex(designSchema);
            } else {
                indexMapper = new DynamicIndexMapperByName(designSchema);
            }
            enforcer = new DiOutgoingDynamicSchemaEnforcer(designSchema, indexMapper);
        } else {
            IndexMapper indexMapper = null;
            if (byIndex) {
                indexMapper = new IndexMapperByIndex(designSchema);
            } else {
                indexMapper = new IndexMapperByName(designSchema);
            }
            enforcer = new DiOutgoingSchemaEnforcer(designSchema, indexMapper);
        }

        return enforcer;
    }
}
