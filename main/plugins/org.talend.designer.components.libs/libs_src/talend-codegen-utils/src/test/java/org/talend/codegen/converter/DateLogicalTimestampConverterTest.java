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
 * Unit-tests for {@link DateLogicalTimestampConverter}
 */
public class DateLogicalTimestampConverterTest {

    /**
     * Checks {@link DateLogicalTimestampConverter#getSchema()} returns logical timestamp schema
     */
    @Test
    public void testGetSchema() {
        Schema expected = AvroUtils._logicalTimestamp();

        AvroConverter<?, ?> converter = new DateLogicalTimestampConverter();
        Schema actual = converter.getSchema();
        Assert.assertEquals(expected, actual);
    }

    /**
     * Checks {@link DateLogicalTimestampConverter#getDatumClass()} returns {@link Date} class
     */
    @Test
    public void testGetDatumClass() {
        Class<?> expected = Date.class;

        AvroConverter<?, ?> converter = new DateLogicalTimestampConverter();
        Class<?> actual = converter.getDatumClass();
        Assert.assertEquals(expected, actual);
    }

    /**
     * Checks {@link DateLogicalTimestampConverter#convertToAvro(Date)} converts timestamp in milliseconds precision to correct
     * number
     * of milliseconds
     */
    @Test
    public void testConvertToAvro() {
        long expected = 123456789l;

        AvroConverter<Date, Long> converter = new DateLogicalTimestampConverter();
        long actual = converter.convertToAvro(new Date(123456789l));
        Assert.assertEquals(expected, actual);
    }

    /**
     * Checks {@link DateLogicalTimestampConverter#convertToDatum(Integer)} converts number of milliseconds from unix epoch to
     * correct
     * timestamp
     */
    @Test
    public void testConvertToDatum() {
        Date expected = new Date(123456789l);

        AvroConverter<Date, Long> converter = new DateLogicalTimestampConverter();
        Date actual = converter.convertToDatum(123456789l);
        Assert.assertEquals(expected, actual);

    }

}
