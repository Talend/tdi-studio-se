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
 * Unit-tests for {@link BigDecimalStringConverter}
 */
public class BigDecimalStringConverterTest {

    /**
     * Checks {@link BigDecimalStringConverter#getSchema()} returns string schema
     */
    @Test
    public void testGetSchema() {
        Schema expected = AvroUtils._string();
        AvroConverter<?, ?> converter = new BigDecimalStringConverter();
        Schema actual = converter.getSchema();
        Assert.assertEquals(expected, actual);
    }

    /**
     * Checks {@link BigDecimalStringConverter#getDatumClass()} returns {@link BigDecimal} class
     */
    @Test
    public void testGetDatumClass() {
        Class<?> expected = BigDecimal.class;
        AvroConverter<?, ?> converter = new BigDecimalStringConverter();
        Class<?> actual = converter.getDatumClass();
        Assert.assertEquals(expected, actual);
    }

    /**
     * Checks {@link BigDecimalStringConverter#convertToAvro(BigDecimal)} converts BigDecimal to correct string
     */
    @Test
    public void testConvertToAvro() {
        String expected = "1234567890123456789.123456789";
        AvroConverter<BigDecimal, String> converter = new BigDecimalStringConverter();
        String actual = converter.convertToAvro(new BigDecimal("1234567890123456789.123456789"));
        Assert.assertEquals(expected, actual);
    }

    /**
     * Checks {@link BigDecimalStringConverter#convertToDatum(String)} converts string to correct char
     */
    @Test
    public void testConvertToDatum() {
        BigDecimal expected = new BigDecimal("1234567890123456789.123456789");
        AvroConverter<BigDecimal, String> converter = new BigDecimalStringConverter();
        BigDecimal actual = converter.convertToDatum("1234567890123456789.123456789");
        Assert.assertEquals(expected, actual);
    }
}
