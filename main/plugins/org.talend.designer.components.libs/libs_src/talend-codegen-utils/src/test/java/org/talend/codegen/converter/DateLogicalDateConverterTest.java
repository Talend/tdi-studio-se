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

import java.util.Date;

import org.apache.avro.Schema;
import org.junit.Assert;
import org.junit.Test;
import org.talend.daikon.avro.AvroUtils;
import org.talend.daikon.avro.converter.AvroConverter;

/**
 * Unit-tests for {@link DateLogicalDateConverter}
 */
public class DateLogicalDateConverterTest {

    /**
     * Checks {@link DateLogicalDateConverter#getSchema()} returns logical date schema
     */
    @Test
    public void testGetSchema() {
        Schema expected = AvroUtils._logicalDate();

        AvroConverter<?, ?> converter = new DateLogicalDateConverter();
        Schema actual = converter.getSchema();
        Assert.assertEquals(expected, actual);
    }

    /**
     * Checks {@link DateLogicalDateConverter#getDatumClass()} returns {@link Date} class
     */
    @Test
    public void testGetDatumClass() {
        Class<?> expected = Date.class;

        AvroConverter<?, ?> converter = new DateLogicalDateConverter();
        Class<?> actual = converter.getDatumClass();
        Assert.assertEquals(expected, actual);
    }

    /**
     * Checks {@link DateLogicalDateConverter#convertToAvro(Date)} converts timestamp in milliseconds precision to correct number
     * of days
     */
    @Test
    public void testConvertToAvro() {
        int expected = 12345;

        AvroConverter<Date, Integer> converter = new DateLogicalDateConverter();
        int actual = converter.convertToAvro(new Date(1066608012345l));
        Assert.assertEquals(expected, actual);
    }

    /**
     * Checks {@link DateLogicalDateConverter#convertToAvro(Date)} returns 0 if {@link Date} is passed which is less than 1 day
     * from unix epoch time
     */
    @Test
    public void testConvertToAvroZero() {
        int expected = 0;

        AvroConverter<Date, Integer> converter = new DateLogicalDateConverter();
        int actual = converter.convertToAvro(new Date(10l));
        Assert.assertEquals(expected, actual);
    }

    /**
     * Checks {@link DateLogicalDateConverter#convertToDatum(Integer)} converts number of days from unix epoch to correct
     * timestamp
     */
    @Test
    public void testConvertToDatum() {
        Date expected = new Date(1066608000000l);

        AvroConverter<Date, Integer> converter = new DateLogicalDateConverter();
        Date actual = converter.convertToDatum(12345);
        Assert.assertEquals(expected, actual);

    }

    /**
     * Checks {@link DateLogicalDateConverter#convertToDatum(Integer)} returns zero milliseconds timestamp if zero number of days
     * was passed
     */
    @Test
    public void testConvertToDatumZero() {
        Date expected = new Date(0l);

        AvroConverter<Date, Integer> converter = new DateLogicalDateConverter();
        Date actual = converter.convertToDatum(0);
        Assert.assertEquals(expected, actual);

    }
}
