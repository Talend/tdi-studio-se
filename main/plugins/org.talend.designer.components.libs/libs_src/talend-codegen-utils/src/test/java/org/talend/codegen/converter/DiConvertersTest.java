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
package org.talend.codegen.converter;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.hasSize;

import java.util.List;

import org.apache.avro.Schema;
import org.apache.avro.SchemaBuilder;
import org.junit.Test;
import org.talend.daikon.avro.AvroUtils;
import org.talend.daikon.avro.converter.AvroConverter;

/**
 * Unit-tests for {@link DiConverters}
 */
public class DiConvertersTest {

    /**
     * Checks {@link DiConverters#initConverters(Schema)} returns list of converters, where each converter corresponds to
     * schema field type
     */
    @Test
    public void testInitConverters() {
        Schema schema = SchemaBuilder.record("records").fields() //
                .name("logicalDate").type(AvroUtils._logicalDate()).noDefault() //
                .name("logicalTime").type(AvroUtils._logicalTime()).noDefault() //
                .name("logicalTimestamp").type(AvroUtils._logicalTimestamp()).noDefault() //
                .name("bigDecimal").type(AvroUtils._decimal()).noDefault() //
                .name("byte").type(AvroUtils._byte()).noDefault() //
                .name("character").type(AvroUtils._character()).noDefault() //
                .name("short").type(AvroUtils._short()).noDefault() //
                .name("array").type().array().items().stringType().noDefault() //
                .name("boolean").type().booleanType().noDefault() //
                .name("bytes").type().bytesType().noDefault() //
                .name("double").type().doubleType().noDefault() //
                .name("float").type().floatType().noDefault() //
                .name("int").type().intType().noDefault() //
                .name("long").type().longType().noDefault() //
                .name("string").type().stringType().noDefault() //
                .endRecord(); //

        @SuppressWarnings("rawtypes")
        List<AvroConverter> converters = DiConverters.initConverters(schema);
        assertThat(converters, hasSize(15));

        @SuppressWarnings("rawtypes")
        AvroConverter converter = null;
        converter = converters.get(0);
        assertThat(converter, instanceOf(DateLogicalDateConverter.class));
        converter = converters.get(1);
        assertThat(converter, instanceOf(IdentityConverter.class));
        converter = converters.get(2);
        assertThat(converter, instanceOf(DateLogicalTimestampConverter.class));
        converter = converters.get(3);
        assertThat(converter, instanceOf(BigDecimalStringDoubleConverter.class));
        converter = converters.get(4);
        assertThat(converter, instanceOf(ByteIntegerConverter.class));
        converter = converters.get(5);
        assertThat(converter, instanceOf(CharStringConverter.class));
        converter = converters.get(6);
        assertThat(converter, instanceOf(ShortIntegerConverter.class));
        converter = converters.get(7);
        assertThat(converter, instanceOf(IdentityConverter.class));
        converter = converters.get(8);
        assertThat(converter, instanceOf(IdentityConverter.class));
        converter = converters.get(9);
        assertThat(converter, instanceOf(IdentityConverter.class));
        converter = converters.get(10);
        assertThat(converter, instanceOf(IdentityConverter.class));
        converter = converters.get(11);
        assertThat(converter, instanceOf(IdentityConverter.class));
        converter = converters.get(12);
        assertThat(converter, instanceOf(IdentityConverter.class));
        converter = converters.get(13);
        assertThat(converter, instanceOf(IdentityConverter.class));
        converter = converters.get(14);
        assertThat(converter, instanceOf(IdentityConverter.class));
    }
}
