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
 * Unit-tests for {@link ShortIntegerConverter}
 */
public class ShortIntegerConverterTest {

    /**
     * Checks {@link ShortIntegerConverter#getSchema()} returns int schema
     */
    @Test
    public void testGetSchema() {
        Schema expected = AvroUtils._int();
        AvroConverter<?, ?> converter = new ShortIntegerConverter();
        Schema actual = converter.getSchema();
        Assert.assertEquals(expected, actual);
    }

    /**
     * Checks {@link ShortIntegerConverter#getDatumClass()} returns {@link Short} class
     */
    @Test
    public void testGetDatumClass() {
        Class<?> expected = Short.class;
        AvroConverter<?, ?> converter = new ShortIntegerConverter();
        Class<?> actual = converter.getDatumClass();
        Assert.assertEquals(expected, actual);
    }

    /**
     * Checks {@link ShortIntegerConverter#convertToAvro(Short)} converts byte to correct int
     */
    @Test
    public void testConvertToAvro() {
        int expected = 12345;
        AvroConverter<Short, Integer> converter = new ShortIntegerConverter();
        int actual = converter.convertToAvro((short) 12345);
        Assert.assertEquals(expected, actual);
    }

    /**
     * Checks {@link ShortIntegerConverter#convertToDatum(Integer)} converts int to correct byte
     */
    @Test
    public void testConvertToDatum() {
        short expected = (short) 12345;
        AvroConverter<Short, Integer> converter = new ShortIntegerConverter();
        short actual = converter.convertToDatum(12345);
        Assert.assertEquals(expected, actual);
    }
}
