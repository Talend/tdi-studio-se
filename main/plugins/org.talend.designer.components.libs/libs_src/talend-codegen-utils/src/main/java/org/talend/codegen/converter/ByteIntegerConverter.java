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
import org.talend.daikon.avro.AvroUtils;
import org.talend.daikon.avro.converter.AbstractAvroConverter;

/**
 * Converts DI byte to Avro int and vice versa
 */
public class ByteIntegerConverter extends AbstractAvroConverter<Byte, Integer> {

    /**
     * Constructor sets {@link Byte} class, which is type of DI data and int {@link Schema} as Avro type
     */
    public ByteIntegerConverter() {
        super(Byte.class, AvroUtils._int());
    }

    /**
     * Converts Avro int value to DI byte value
     */
    @Override
    public Byte convertToDatum(Integer value) {
        return value.byteValue();
    }

    /**
     * Converts byte DI value to Avro int value
     */
    @Override
    public Integer convertToAvro(Byte value) {
        return value.intValue();
    }
}
