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
 * Converts DI short to Avro int and vice versa
 */
public class ShortIntegerConverter extends AbstractAvroConverter<Short, Integer> {

    /**
     * Constructor sets {@link Short} class, which is type of DI data and int {@link Schema} as Avro type
     */
    public ShortIntegerConverter() {
        super(Short.class, AvroUtils._int());
    }

    /**
     * Converts Avro int value to DI short value
     */
    @Override
    public Short convertToDatum(Integer value) {
        return value.shortValue();
    }

    /**
     * Converts short DI value to Avro int value
     */
    @Override
    public Integer convertToAvro(Short value) {
        return value.intValue();
    }

}
