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

import java.math.BigDecimal;

import org.apache.avro.Schema;
import org.junit.Assert;
import org.junit.Test;
import org.talend.daikon.avro.AvroUtils;
import org.talend.daikon.avro.converter.AvroConverter;

/**
 * Unit-tests for {@link BigDecimalStringDoubleConverter}
 */
public class BigDecimalStringDoubleConverterTest {

    /**
     * Checks {@link BigDecimalStringDoubleConverter#getSchema()} returns string schema
     */
    @Test
    public void testGetSchema() {
        Schema expected = AvroUtils._string();
        AvroConverter<?, ?> converter = new BigDecimalStringDoubleConverter();
        Schema actual = converter.getSchema();
        Assert.assertEquals(expected, actual);
    }

    /**
     * Checks {@link BigDecimalStringDoubleConverter#getDatumClass()} returns {@link BigDecimal} class
     */
    @Test
    public void testGetDatumClass() {
        Class<?> expected = Object.class;
        AvroConverter<?, ?> converter = new BigDecimalStringDoubleConverter();
        Class<?> actual = converter.getDatumClass();
        Assert.assertEquals(expected, actual);
    }

    /**
     * Checks {@link BigDecimalStringDoubleConverter#convertToAvro(BigDecimal)} converts BigDecimal to correct string
     */
    @Test
    public void testConvertToAvroBigDecimal() {
        String expected = "1234567890123456789.123456789";
        AvroConverter<Object, String> converter = new BigDecimalStringDoubleConverter();
        String actual = converter.convertToAvro(new BigDecimal("1234567890123456789.123456789"));
        Assert.assertEquals(expected, actual);
    }

    /**
     * Checks {@link BigDecimalStringDoubleConverter#convertToAvro(BigDecimal)} converts BigDecimal to correct string
     */
    @Test
    public void testConvertToAvroDouble() {
        String expected = "123456.78901234";
        AvroConverter<Object, String> converter = new BigDecimalStringDoubleConverter();
        String actual = converter.convertToAvro(123456.78901234);
        Assert.assertEquals(expected, actual);
    }

    /**
     * Checks {@link BigDecimalStringDoubleConverter#convertToAvro(BigDecimal)} converts BigDecimal to correct string
     */
    @Test
    public void testConvertToAvroDoubleWithE() {
        String expected = "1.23456789012345677E18";
        AvroConverter<Object, String> converter = new BigDecimalStringDoubleConverter();
        String actual = converter.convertToAvro(1234567890123456789.123456789);
        Assert.assertEquals(expected, actual);
    }

    /**
     * Checks {@link BigDecimalStringDoubleConverter#convertToDatum(String)} converts string to correct char
     */
    @Test
    public void testConvertToDatum() {
        Object expected = new BigDecimal("1234567890123456789.123456789");
        AvroConverter<Object, String> converter = new BigDecimalStringDoubleConverter();
        Object actual = converter.convertToDatum("1234567890123456789.123456789");
        Assert.assertEquals(expected, actual);
    }
}
