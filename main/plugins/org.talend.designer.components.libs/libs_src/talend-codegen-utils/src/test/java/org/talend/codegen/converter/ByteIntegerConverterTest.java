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

import org.apache.avro.Schema;
import org.junit.Assert;
import org.junit.Test;
import org.talend.daikon.avro.AvroUtils;
import org.talend.daikon.avro.converter.AvroConverter;

/**
 * Unit-tests for {@link ByteIntegerConverter}
 */
public class ByteIntegerConverterTest {

    /**
     * Checks {@link ByteIntegerConverter#getSchema()} returns int schema
     */
    @Test
    public void testGetSchema() {
        Schema expected = AvroUtils._int();
        AvroConverter<?, ?> converter = new ByteIntegerConverter();
        Schema actual = converter.getSchema();
        Assert.assertEquals(expected, actual);
    }

    /**
     * Checks {@link ByteIntegerConverter#getDatumClass()} returns {@link Byte} class
     */
    @Test
    public void testGetDatumClass() {
        Class<?> expected = Byte.class;
        AvroConverter<?, ?> converter = new ByteIntegerConverter();
        Class<?> actual = converter.getDatumClass();
        Assert.assertEquals(expected, actual);
    }

    /**
     * Checks {@link ByteIntegerConverter#convertToAvro(Byte)} converts byte to correct int
     */
    @Test
    public void testConvertToAvro() {
        int expected = 123;
        AvroConverter<Byte, Integer> converter = new ByteIntegerConverter();
        int actual = converter.convertToAvro((byte) 123);
        Assert.assertEquals(expected, actual);
    }

    /**
     * Checks {@link ByteIntegerConverter#convertToDatum(Integer)} converts int to correct byte
     */
    @Test
    public void testConvertToDatum() {
        byte expected = (byte) 123;
        AvroConverter<Byte, Integer> converter = new ByteIntegerConverter();
        byte actual = converter.convertToDatum(123);
        Assert.assertEquals(expected, actual);
    }

}
