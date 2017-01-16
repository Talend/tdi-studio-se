// ============================================================================
//
// Copyright (C) 2006-2016 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.codegen.avro.conversion;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;

import org.apache.avro.Schema;
import org.apache.avro.SchemaBuilder;
import org.junit.Test;
import org.talend.daikon.avro.SchemaConstants;

/**
 * Unit-tests for {@link EnforcerCreator} class
 */
public class EnforcerCreatorTest {

    /**
     * Checks {@link EnforcerCreator#createOutgoingEnforcer(Schema, Schema, boolean)} creates instance of
     * {@link DiOutgoingDynamicSchemaEnforcer}
     * in case when design schema contains dynamic field
     */
    @Test
    public void testCreateOutgoingEnforcerDynamic() {
        Schema designSchema = SchemaBuilder.builder().record("Record") //
                .prop(DiSchemaConstants.TALEND6_DYNAMIC_COLUMN_POSITION, "0") //
                .prop(SchemaConstants.INCLUDE_ALL_FIELDS, "true").fields() //
                .name("name").type().stringType().noDefault() //
                .endRecord(); //

        DiOutgoingSchemaEnforcer enforcer = EnforcerCreator.createOutgoingEnforcer(designSchema, true);
        assertThat(enforcer, instanceOf(DiOutgoingDynamicSchemaEnforcer.class));
    }

    /**
     * Checks {@link EnforcerCreator#createOutgoingEnforcer(Schema, Schema, boolean)} creates instance of
     * {@link DiOutgoingSchemaEnforcer}
     * in case when design schema doesn't contain dynamic field
     */
    @Test
    public void testCreateOutgoingEnforcer() {
        Schema designSchema = SchemaBuilder.builder().record("Record").fields() //
                .name("name").type().stringType().noDefault() //
                .endRecord(); //

        DiOutgoingSchemaEnforcer enforcer = EnforcerCreator.createOutgoingEnforcer(designSchema, true);
        assertThat(enforcer, instanceOf(DiOutgoingSchemaEnforcer.class));
    }

}
