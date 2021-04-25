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
import org.talend.daikon.avro.AvroUtils;
import org.talend.daikon.avro.converter.AbstractAvroConverter;

/**
 * Converts DI BigDecimal to Avro string and vice versa
 *
 * This is patched converter, which can convert both BigDecimal and Double to Avro representation of BigDecimal
 * This is required to pass following TUJ: TDI38068_tSnowflake_Special_data_mapping
 */
public class BigDecimalStringDoubleConverter extends AbstractAvroConverter<Object, String> {

    /**
     * Constructor sets {@link BigDecimal} class, which is type of DI data and string {@link Schema} as Avro type
     */
    public BigDecimalStringDoubleConverter() {
        super(Object.class, AvroUtils._string());
    }

    /**
     * Converts Avro string value to DI BigDecimal value
     */
    @Override
    public Object convertToDatum(String value) {
        return new BigDecimal(value);
    }

    /**
     * Converts BigDecimal DI value to Avro string value
     */
    @Override
    public String convertToAvro(Object value) {
        if (value instanceof Double) {
            return String.valueOf(value);
        } // else instance of BigDecimal

        return value.toString();
    }

}
