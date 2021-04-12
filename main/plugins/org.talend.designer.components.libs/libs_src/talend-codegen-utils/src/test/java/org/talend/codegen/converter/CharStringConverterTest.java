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
 * Unit-tests for {@link CharStringConverter}
 */
public class CharStringConverterTest {

    /**
     * Checks {@link CharStringConverter#getSchema()} returns string schema
     */
    @Test
    public void testGetSchema() {
        Schema expected = AvroUtils._string();
        AvroConverter<?, ?> converter = new CharStringConverter();
        Schema actual = converter.getSchema();
        Assert.assertEquals(expected, actual);
    }

    /**
     * Checks {@link CharStringConverter#getDatumClass()} returns {@link Character} class
     */
    @Test
    public void testGetDatumClass() {
        Class<?> expected = Character.class;
        AvroConverter<?, ?> converter = new CharStringConverter();
        Class<?> actual = converter.getDatumClass();
        Assert.assertEquals(expected, actual);
    }

    /**
     * Checks {@link CharStringConverter#convertToAvro(Character)} converts char to correct string
     */
    @Test
    public void testConvertToAvro() {
        String expected = "s";
        AvroConverter<Character, String> converter = new CharStringConverter();
        String actual = converter.convertToAvro('s');
        Assert.assertEquals(expected, actual);
    }

    /**
     * Checks {@link CharStringConverter#convertToDatum(String)} converts string to correct char
     */
    @Test
    public void testConvertToDatum() {
        char expected = 's';
        AvroConverter<Character, String> converter = new CharStringConverter();
        char actual = converter.convertToDatum("s");
        Assert.assertEquals(expected, actual);
    }
}
