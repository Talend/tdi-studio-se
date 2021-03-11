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
import org.talend.daikon.avro.AvroUtils;
import org.talend.daikon.avro.converter.AbstractAvroConverter;

/**
 * Converts DI Date to Avro logical Timestamps millis and vice versa
 */
public class DateLogicalTimestampConverter extends AbstractAvroConverter<Date, Long> {

    /**
     * Constructor sets {@link Date} class, which is type of DI data and logical timestamp {@link Schema} as Avro type
     */
    public DateLogicalTimestampConverter() {
        super(Date.class, AvroUtils._logicalTimestamp());
    }

    /**
     * Converts long, which is a number of milliseconds from unix epoch, to timestamp with millisecond precision
     */
    @Override
    public Date convertToDatum(Long value) {
        return new Date(value);
    }

    /**
     * Converts timestamp ({@link Date}) with millisecond precision to long value, which is a number of milliseconds from unix
     * epoch
     */
    @Override
    public Long convertToAvro(Date value) {
        return value.getTime();
    }

}
