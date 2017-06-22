package org.talend.codegen.enforcer;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;

import org.apache.avro.Schema;
import org.apache.avro.SchemaBuilder;
import org.junit.Test;
import org.talend.codegen.DiSchemaConstants;
import org.talend.daikon.avro.SchemaConstants;

/**
 * Unit-tests for {@link EnforcerCreator} class
 */
public class EnforcerCreatorTest {

    /**
     * Checks {@link EnforcerCreator#createOutgoingEnforcer(Schema, Schema, boolean)} creates instance of
     * {@link OutgoingDynamicSchemaEnforcer}
     * in case when design schema contains dynamic field
     */
    @Test
    public void testCreateOutgoingEnforcerDynamic() {
        Schema designSchema = SchemaBuilder.builder().record("Record") //
                .prop(DiSchemaConstants.TALEND6_DYNAMIC_COLUMN_POSITION, "0") //
                .prop(SchemaConstants.INCLUDE_ALL_FIELDS, "true").fields() //
                .name("name").type().stringType().noDefault() //
                .endRecord(); //

        OutgoingSchemaEnforcer enforcer = EnforcerCreator.createOutgoingEnforcer(designSchema, true);
        assertThat(enforcer, instanceOf(OutgoingDynamicSchemaEnforcer.class));
    }

    /**
     * Checks {@link EnforcerCreator#createOutgoingEnforcer(Schema, Schema, boolean)} creates instance of
     * {@link OutgoingSchemaEnforcer}
     * in case when design schema doesn't contain dynamic field
     */
    @Test
    public void testCreateOutgoingEnforcer() {
        Schema designSchema = SchemaBuilder.builder().record("Record").fields() //
                .name("name").type().stringType().noDefault() //
                .endRecord(); //

        OutgoingSchemaEnforcer enforcer = EnforcerCreator.createOutgoingEnforcer(designSchema, true);
        assertThat(enforcer, instanceOf(OutgoingSchemaEnforcer.class));
    }

}
